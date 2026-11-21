package com.punhanaghayev.bimmerpro.core.simulator

import com.punhanaghayev.bimmerpro.core.bmw.CodingCapability
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.DtcStatus
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType
import com.punhanaghayev.bimmerpro.core.protocol.BmwSecurityAccess
import com.punhanaghayev.bimmerpro.core.protocol.ConnectionState
import com.punhanaghayev.bimmerpro.core.protocol.HsfzFrame
import com.punhanaghayev.bimmerpro.core.protocol.TransportAdapter
import com.punhanaghayev.bimmerpro.core.protocol.UdsNegativeResponseCode
import com.punhanaghayev.bimmerpro.core.protocol.UdsServiceId
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.charset.StandardCharsets

/**
 * Virtual BMW Gateway & ECU Simulator speaking authentic HSFZ + UDS (ISO 14229) byte frames.
 * Enables full end-to-end protocol testing across F10, G30, and E90 profiles without a physical car.
 */
class SimulatorTransportAdapter(
    override val adapterType: AdapterType
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    var bundle: SimulatedVehicleBundle = BmwSimulatorCatalog.buildBundle(adapterType)
        private set

    private val dtcStore = mutableListOf<DtcFault>()
    private val writtenDids = mutableMapOf<Int, ByteArray>()
    private var activeSession: Int = 0x01

    override suspend fun connect() {
        _connectionState.value = ConnectionState.Connecting(adapterType.displayName)
        delay(350)
        bundle = BmwSimulatorCatalog.buildBundle(adapterType)
        dtcStore.clear()
        dtcStore.addAll(bundle.dtcs)
        writtenDids.clear()
        activeSession = 0x01
        _connectionState.value = ConnectionState.Connected(
            adapterType = adapterType,
            endpoint = "sim://${bundle.profile.chassis.code.lowercase()}.local:6801"
        )
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }

    fun currentDtcs(): List<DtcFault> = dtcStore.toList()

    fun setSimulatedBatteryVoltage(voltage: Double) {
        bundle = bundle.copy(
            profile = bundle.profile.copy(batteryVoltage = voltage)
        )
    }

    override suspend fun transceive(frame: ByteArray): ByteArray {
        check(_connectionState.value is ConnectionState.Connected) { "Simulator is not connected" }
        delay(25)

        val frameHex = frame.joinToString("") { "%02X".format(it) }
        val traceResp = SimulatorTraceCatalog.findResponse(frameHex)
        if (traceResp != null && traceResp.length % 2 == 0) {
            val respBytes = ByteArray(traceResp.length / 2) { i ->
                traceResp.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }
            return respBytes
        }

        val request = HsfzFrame.decode(frame)
        val uds = request.payload
        val targetEcu = request.targetAddress
        val sid = uds[0].toInt() and 0xFF

        val responsePayload = when (sid) {
            UdsServiceId.DIAGNOSTIC_SESSION_CONTROL -> {
                activeSession = if (uds.size > 1) uds[1].toInt() and 0xFF else 0x01
                byteArrayOf((sid + 0x40).toByte(), activeSession.toByte(), 0x00, 0x32, 0x01, 0xF4.toByte())
            }

            UdsServiceId.READ_DATA_BY_ID -> handleReadDid(targetEcu, uds)
            UdsServiceId.READ_DTC_INFO -> handleReadDtc(targetEcu, uds)
            UdsServiceId.CLEAR_DIAGNOSTIC_INFO -> handleClearDtc(targetEcu)
            UdsServiceId.WRITE_DATA_BY_ID -> handleWriteDid(targetEcu, uds)
            UdsServiceId.IO_CONTROL_BY_ID -> byteArrayOf((sid + 0x40).toByte(), uds.getOrElse(1) { 0 }, uds.getOrElse(2) { 0 }, 0x03)
            UdsServiceId.ROUTINE_CONTROL -> handleRoutineControl(uds)
            UdsServiceId.SECURITY_ACCESS -> handleSecurityAccess(targetEcu, uds)
            UdsServiceId.TESTER_PRESENT -> byteArrayOf((sid + 0x40).toByte(), 0x00)
            else -> negativeResponse(sid, UdsNegativeResponseCode.SERVICE_NOT_SUPPORTED)
        }

        return HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = responsePayload
        ).encode()
    }

    private fun handleReadDid(targetEcu: Int, uds: ByteArray): ByteArray {
        if (uds.size < 3) return negativeResponse(UdsServiceId.READ_DATA_BY_ID, UdsNegativeResponseCode.INCORRECT_MESSAGE_LENGTH)
        val did = ((uds[1].toInt() and 0xFF) shl 8) or (uds[2].toInt() and 0xFF)
        val prefix = byteArrayOf((UdsServiceId.READ_DATA_BY_ID + 0x40).toByte(), uds[1], uds[2])

        return when (did) {
            DID_VIN -> prefix + bundle.profile.vin.toByteArray(StandardCharsets.US_ASCII)
            DID_I_LEVEL -> prefix + bundle.profile.iLevel.toByteArray(StandardCharsets.US_ASCII)
            DID_BATTERY_MV -> {
                val mv = (bundle.profile.batteryVoltage * 1000).toInt()
                prefix + byteArrayOf(((mv shr 8) and 0xFF).toByte(), (mv and 0xFF).toByte())
            }
            DID_ODOMETER -> {
                val ecuKm = bundle.ecus.firstOrNull { it.address == targetEcu }?.reportedMileageKm ?: bundle.profile.odometerKm
                prefix + byteArrayOf(
                    ((ecuKm shr 16) and 0xFF).toByte(),
                    ((ecuKm shr 8) and 0xFF).toByte(),
                    (ecuKm and 0xFF).toByte()
                )
            }
            else -> {
                val stored = writtenDids[(targetEcu shl 16) or did]
                prefix + (stored ?: byteArrayOf(0x00, 0x01))
            }
        }
    }

    private fun handleReadDtc(targetEcu: Int, uds: ByteArray): ByteArray {
        val subfunction = if (uds.size > 1) uds[1].toInt() and 0xFF else 0x02
        val statusMask = if (uds.size > 2) uds[2].toInt() and 0xFF else 0xFF
        val ecuMatches = dtcStore.filter { targetEcu == EcuAddress.ZGW_BDC || it.ecuAddress == targetEcu }
        val matching = when (subfunction) {
            0x15 -> ecuMatches.filter { it.memoryType == DtcMemoryType.PERMANENT_MEMORY }
            0x02 -> when (statusMask) {
                0x04 -> ecuMatches.filter { it.memoryType == DtcMemoryType.INFO_MEMORY }
                0x09 -> ecuMatches.filter { it.memoryType != DtcMemoryType.INFO_MEMORY }
                else -> ecuMatches
            }
            0x04 -> {
                val fault = ecuMatches.firstOrNull() ?: return negativeResponse(
                    UdsServiceId.READ_DTC_INFO,
                    UdsNegativeResponseCode.REQUEST_OUT_OF_RANGE
                )
                val numeric = fault.code.toIntOrNull(16) ?: 0x100001
                val km = fault.freezeFrame.mileageKm
                val tempByte = (fault.freezeFrame.coolantTempC + 40).coerceIn(0, 255)
                val voltByte = (fault.freezeFrame.batteryVoltage * 10).toInt().coerceIn(0, 255)
                return byteArrayOf(
                    (UdsServiceId.READ_DTC_INFO + 0x40).toByte(),
                    0x04,
                    ((numeric shr 16) and 0xFF).toByte(),
                    ((numeric shr 8) and 0xFF).toByte(),
                    (numeric and 0xFF).toByte(),
                    0x2F,
                    0x01, // Snapshot record #1
                    0x03, // Number of DIDs
                    0x10, 0x0C, ((km shr 16) and 0xFF).toByte(), ((km shr 8) and 0xFF).toByte(), (km and 0xFF).toByte(),
                    0xF4.toByte(), 0x05, tempByte.toByte(),
                    0xF4.toByte(), 0x42, voltByte.toByte()
                )
            }
            else -> ecuMatches
        }

        // Standard 0x59 <subfunction> <statusAvailabilityMask> + [DTC_High, DTC_Mid, DTC_Low, Status] per fault
        val out = ArrayList<Byte>(3 + matching.size * 4)
        out.add((UdsServiceId.READ_DTC_INFO + 0x40).toByte())
        out.add(subfunction.toByte())
        out.add(0xFF.toByte())
        matching.forEach { fault ->
            val numeric = fault.code.toIntOrNull(16) ?: 0x100001
            val statusByte = when {
                fault.memoryType == DtcMemoryType.INFO_MEMORY -> 0x04
                fault.status == DtcStatus.ACTIVE -> 0x2F
                else -> 0x08
            }
            out.add(((numeric shr 16) and 0xFF).toByte())
            out.add(((numeric shr 8) and 0xFF).toByte())
            out.add((numeric and 0xFF).toByte())
            out.add(statusByte.toByte())
        }
        return out.toByteArray()
    }

    private fun handleClearDtc(targetEcu: Int): ByteArray {
        if (targetEcu == EcuAddress.ZGW_BDC) {
            dtcStore.clear()
        } else {
            dtcStore.removeAll { it.ecuAddress == targetEcu }
        }
        return byteArrayOf((UdsServiceId.CLEAR_DIAGNOSTIC_INFO + 0x40).toByte())
    }

    private fun handleWriteDid(targetEcu: Int, uds: ByteArray): ByteArray {
        if (bundle.profile.batteryVoltage < 12.2) {
            return negativeResponse(UdsServiceId.WRITE_DATA_BY_ID, UdsNegativeResponseCode.CONDITIONS_NOT_CORRECT)
        }
        val targetModule = bundle.ecus.firstOrNull { it.address == targetEcu }
        if (targetModule?.codingCapability == CodingCapability.SECURE_CODING_LOCKED) {
            return negativeResponse(UdsServiceId.WRITE_DATA_BY_ID, UdsNegativeResponseCode.SECURITY_ACCESS_DENIED)
        }
        if (uds.size < 4) {
            return negativeResponse(UdsServiceId.WRITE_DATA_BY_ID, UdsNegativeResponseCode.INCORRECT_MESSAGE_LENGTH)
        }
        val did = ((uds[1].toInt() and 0xFF) shl 8) or (uds[2].toInt() and 0xFF)
        writtenDids[(targetEcu shl 16) or did] = uds.copyOfRange(3, uds.size)
        return byteArrayOf((UdsServiceId.WRITE_DATA_BY_ID + 0x40).toByte(), uds[1], uds[2])
    }

    private fun handleRoutineControl(uds: ByteArray): ByteArray {
        if (uds.size < 4) {
            return negativeResponse(UdsServiceId.ROUTINE_CONTROL, UdsNegativeResponseCode.INCORRECT_MESSAGE_LENGTH)
        }
        if (bundle.profile.batteryVoltage < 11.8) {
            return negativeResponse(UdsServiceId.ROUTINE_CONTROL, UdsNegativeResponseCode.CONDITIONS_NOT_CORRECT)
        }
        return byteArrayOf(
            (UdsServiceId.ROUTINE_CONTROL + 0x40).toByte(),
            uds[1],
            uds[2],
            uds[3],
            0x00 // Routine completed OK
        )
    }

    private var lastGeneratedSeed: ByteArray = byteArrayOf(0x3A, 0x9F.toByte(), 0x12, 0x8C.toByte())
    private val unlockedEcus = mutableSetOf<Int>()

    private fun handleSecurityAccess(targetEcu: Int, uds: ByteArray): ByteArray {
        if (uds.size < 2) return negativeResponse(UdsServiceId.SECURITY_ACCESS, UdsNegativeResponseCode.INCORRECT_MESSAGE_LENGTH)
        val subfunction = uds[1].toInt() and 0xFF
        return when (subfunction) {
            0x01, 0x03 -> {
                if (targetEcu in unlockedEcus) {
                    byteArrayOf((UdsServiceId.SECURITY_ACCESS + 0x40).toByte(), subfunction.toByte(), 0x00, 0x00, 0x00, 0x00)
                } else {
                    val s0 = (targetEcu and 0xFF).toByte()
                    val s1 = ((targetEcu xor 0xA5) and 0xFF).toByte()
                    lastGeneratedSeed = byteArrayOf(s0, s1, 0x12, 0x8C.toByte())
                    byteArrayOf((UdsServiceId.SECURITY_ACCESS + 0x40).toByte(), subfunction.toByte()) + lastGeneratedSeed
                }
            }
            0x02, 0x04 -> {
                if (uds.size < 6) return negativeResponse(UdsServiceId.SECURITY_ACCESS, UdsNegativeResponseCode.INCORRECT_MESSAGE_LENGTH)
                val keyReceived = uds.copyOfRange(2, 6)
                val expectedKey = BmwSecurityAccess.calculateKey(targetEcu, lastGeneratedSeed, subfunction - 1)
                if (keyReceived.contentEquals(expectedKey)) {
                    unlockedEcus.add(targetEcu)
                    byteArrayOf((UdsServiceId.SECURITY_ACCESS + 0x40).toByte(), subfunction.toByte())
                } else {
                    negativeResponse(UdsServiceId.SECURITY_ACCESS, UdsNegativeResponseCode.INVALID_KEY)
                }
            }
            else -> negativeResponse(UdsServiceId.SECURITY_ACCESS, UdsNegativeResponseCode.SUB_FUNCTION_NOT_SUPPORTED)
        }
    }

    private fun negativeResponse(serviceId: Int, nrc: UdsNegativeResponseCode): ByteArray =
        byteArrayOf(UdsServiceId.NEGATIVE_RESPONSE.toByte(), serviceId.toByte(), nrc.code.toByte())

    companion object {
        const val DID_VIN = 0xF190
        const val DID_I_LEVEL = 0xF150
        const val DID_BATTERY_MV = 0x100A
        const val DID_ODOMETER = 0x100C
    }
}
