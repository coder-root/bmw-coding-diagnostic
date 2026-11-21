package com.punhanaghayev.bimmerpro.core.protocol

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

/**
 * Classic Bluetooth RFCOMM SPP transport for OBDLink MX+, vLinker BT/MC+, Vgate iCar Pro,
 * UniCarScan 3000, and Konnwei SPP adapters using standard SPP UUID 00001101.
 */
class ClassicBtObdTransportAdapter(
    private val context: Context,
    private val deviceAddress: String,
    override val adapterType: AdapterType = AdapterType.OBDLINK_MX_PLUS,
    private val timeoutMs: Int = 4500,
    private val onAtLog: (String) -> Unit = {}
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var socket: BluetoothSocket? = null
    private var input: InputStream? = null
    private var output: OutputStream? = null

    var lastReadVoltage: Double? = null
        private set

    var isStnChip: Boolean = false
        private set

    @SuppressLint("MissingPermission")
    override suspend fun connect() = withContext(Dispatchers.IO) {
        _connectionState.value = ConnectionState.Connecting("${adapterType.displayName} ($deviceAddress)")
        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter: BluetoothAdapter? = manager?.adapter
        if (adapter == null || !adapter.isEnabled) {
            val msg = "Bluetooth kapalı veya cihaz desteklemiyor."
            _connectionState.value = ConnectionState.Error(msg)
            throw IllegalStateException(msg)
        }

        val device = adapter.getRemoteDevice(deviceAddress)
        runCatching { adapter.cancelDiscovery() }

        val rfcomm = try {
            device.createRfcommSocketToServiceRecord(SPP_UUID).also { it.connect() }
        } catch (primaryEx: Exception) {
            val msg = "Classic BT SPP bağlantısı kurulamadı ($deviceAddress): ${primaryEx.localizedMessage ?: "Bağlantı hatası"}"
            _connectionState.value = ConnectionState.Error(msg)
            throw primaryEx
        }

        socket = rfcomm
        input = rfcomm.inputStream
        output = rfcomm.outputStream
        delay(120)
        drainInput()

        sendAtCommand("ATZ", waitMs = 1200)
        val chipId = sendAtCommand("STI", waitMs = 500)
        isStnChip = chipId.contains("STN", ignoreCase = true) || chipId.contains("OBDLink", ignoreCase = true)
        onAtLog("BT-SPP CHIP: ${if (isStnChip) "STN ($chipId)" else "ELM327 ($chipId)"}")

        sendAtCommand("ATE0")
        sendAtCommand("ATL0")
        sendAtCommand("ATS0")
        sendAtCommand("ATH1")
        sendAtCommand("ATSP6")
        sendAtCommand("ATCAF1")
        sendAtCommand("ATAL")
        sendAtCommand("ATAT2")
        if (isStnChip) {
            sendAtCommand("STCSEGR 1", waitMs = 400)
        }
        lastReadVoltage = ElmIsoTpHelper.parseVoltage(sendAtCommand("ATRV"))
        _connectionState.value = ConnectionState.Connected(adapterType, "SPP:$deviceAddress")
    }

    suspend fun configureKLineKwp2000() = withContext(Dispatchers.IO) {
        sendAtCommand("ATSPB")
        sendAtCommand("ATAT1")
        sendAtCommand("ATST96")
        onAtLog("BT-SPP K-LINE: ATSPB 10.4 kbps KWP2000 yapılandırıldı")
    }

    override suspend fun disconnect() = withContext(Dispatchers.IO) {
        runCatching { socket?.close() }
        socket = null
        input = null
        output = null
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun transceive(frame: ByteArray): ByteArray = withContext(Dispatchers.IO) {
        val hsfz = HsfzFrame.decode(frame)
        val targetEcu = hsfz.targetAddress and 0xFF
        val hexPayload = hsfz.payload.joinToString("") { "%02X".format(it) }

        val udsBytes = if (isStnChip) {
            val raw = sendAtCommand("STPX h:6F1, d:$hexPayload, r:1, t:350")
            ElmIsoTpHelper.parseUdsResponse(raw)
        } else {
            sendAtCommand("ATSH6F1")
            sendAtCommand("ATCEA%02X".format(targetEcu))
            sendAtCommand("ATCRA6%02X".format(targetEcu))
            val raw = sendIsoTpPayload(hsfz.payload)
            ElmIsoTpHelper.parseUdsResponse(raw)
        }

        HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = udsBytes
        ).encode()
    }

    private fun sendIsoTpPayload(payload: ByteArray): String {
        if (payload.size <= 7) {
            val hex = payload.joinToString("") { "%02X".format(it) }
            val firstResp = sendAtCommand(hex)
            if (ElmIsoTpHelper.isFirstFrameResponse(firstResp)) {
                val fcHex = IsoTpCodec.flowControlFrame().take(3).joinToString("") { "%02X".format(it) }
                val cfResp = sendAtCommand(fcHex)
                return "$firstResp\n$cfResp"
            }
            return firstResp
        }
        val segments = IsoTpCodec.segment(payload)
        var lastRaw = ""
        segments.forEach { seg ->
            val segHex = seg.joinToString("") { "%02X".format(it) }
            lastRaw = sendAtCommand(segHex)
        }
        return lastRaw
    }

    private fun drainInput() {
        val inp = input ?: return
        runCatching {
            while (inp.available() > 0) {
                val skip = ByteArray(inp.available().coerceAtMost(512))
                inp.read(skip)
            }
        }
    }

    private fun sendAtCommand(command: String, waitMs: Int = timeoutMs): String {
        val out = output ?: error("Classic BT SPP socket bağlı değil")
        val inp = input ?: error("Classic BT SPP input stream yok")
        drainInput()
        out.write("$command\r".toByteArray(StandardCharsets.US_ASCII))
        out.flush()

        val sb = StringBuilder()
        val buf = ByteArray(512)
        val deadline = System.currentTimeMillis() + waitMs
        while (System.currentTimeMillis() < deadline) {
            if (inp.available() > 0) {
                val read = runCatching { inp.read(buf) }.getOrDefault(-1)
                if (read <= 0) break
                sb.append(String(buf, 0, read, StandardCharsets.US_ASCII))
                if (sb.contains(">")) break
            } else {
                Thread.sleep(15)
            }
        }
        val response = sb.toString()
        onAtLog("BT-AT [$command] -> ${response.replace("\r", " ").replace("\n", " ").trim()}")
        return response
    }

    companion object {
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

        @SuppressLint("MissingPermission")
        fun scanBondedClassicDevices(context: Context): List<BleAdapterDevice> {
            val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager ?: return emptyList()
            val bonded = runCatching { manager.adapter?.bondedDevices }.getOrNull() ?: return emptyList()
            return bonded.mapNotNull { dev ->
                val name = dev.name ?: return@mapNotNull null
                if (
                    name.contains("OBD", true) ||
                    name.contains("vLinker", true) ||
                    name.contains("UniCar", true) ||
                    name.contains("Vgate", true) ||
                    name.contains("Konnwei", true) ||
                    name.contains("Bimmer", true) ||
                    name.contains("ELM", true)
                ) {
                    BleAdapterDevice("$name [SPP]", dev.address, -45)
                } else {
                    null
                }
            }
        }
    }
}
