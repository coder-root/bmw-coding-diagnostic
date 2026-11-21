package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.ClutchWearLevel
import com.punhanaghayev.bimmerpro.core.bmw.DpfHealthStatus
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcMemoryType
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.DtcStatus
import com.punhanaghayev.bimmerpro.core.bmw.FreezeFrameData
import com.punhanaghayev.bimmerpro.core.bmw.IbsBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.InjectorCalibration
import com.punhanaghayev.bimmerpro.core.bmw.ShortCircuitChannel
import com.punhanaghayev.bimmerpro.core.bmw.ZfClutchDetail
import com.punhanaghayev.bimmerpro.core.bmw.ZfGearboxHealth
import java.nio.charset.StandardCharsets

/**
 * Decodes raw UDS payloads returned by physical BMW ECUs (via vLinker BM Wi-Fi, ENET, BLE, or K+DCAN).
 */
object PhysicalUdsParser {

    private val vinRegex = Regex("[A-HJ-NPR-Z0-9]{17}")
    private val bmwVinRegex = Regex("(WBA|WBS|WBX|WBY|WB1|WB2|WB3|WB4|WB5|4US|5UX|5YM|3MW|WMW)[A-HJ-NPR-Z0-9]{14}")

    fun extractVin(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        // If payload contains UDS positive response header (0x62 DID_HI DID_LO), skip the 3-byte header
        // so the positive response SID 0x62 ('b') is not confused with the VIN ASCII character 'B'.
        val data = if (payload.size >= 20 && (payload[0].toInt() and 0xFF) == 0x62) {
            payload.copyOfRange(3, payload.size)
        } else {
            payload
        }
        val ascii = String(data, StandardCharsets.US_ASCII)
            .replace(Regex("[^A-Za-z0-9]"), "")
            .uppercase()

        // Match standard BMW World Manufacturer Identifier (WMI) first
        val bmwMatch = bmwVinRegex.find(ascii)?.value
        if (bmwMatch != null) return bmwMatch

        return vinRegex.find(ascii)?.value
    }

    fun inferChassisFromVin(vin: String): BmwChassis {
        if (vin.length < 7) return BmwChassis.F10
        val modelCode = vin.substring(3, 7).uppercase()
        return when {
            modelCode.startsWith("5C") || modelCode.startsWith("FW") || modelCode.startsWith("FV") || modelCode.startsWith("XG") -> BmwChassis.F10
            modelCode.startsWith("3A") || modelCode.startsWith("3B") || modelCode.startsWith("8E") -> BmwChassis.F30
            modelCode.startsWith("JA") || modelCode.startsWith("JB") || modelCode.startsWith("JD") -> BmwChassis.G30
            modelCode.startsWith("5U") || modelCode.startsWith("5R") -> BmwChassis.G20
            modelCode.startsWith("1A") || modelCode.startsWith("1B") || modelCode.startsWith("1C") -> BmwChassis.F20
            modelCode.startsWith("KS") || modelCode.startsWith("KR") -> BmwChassis.F15
            modelCode.startsWith("WZ") || modelCode.startsWith("WX") -> BmwChassis.F25
            modelCode.startsWith("CR") || modelCode.startsWith("CY") -> BmwChassis.G05
            modelCode.startsWith("TX") || modelCode.startsWith("TY") -> BmwChassis.G01
            modelCode.startsWith("VB") || modelCode.startsWith("VA") || modelCode.startsWith("PG") -> BmwChassis.E90
            modelCode.startsWith("NA") || modelCode.startsWith("NE") || modelCode.startsWith("NU") -> BmwChassis.E60
            modelCode.startsWith("FE") || modelCode.startsWith("FF") -> BmwChassis.E70
            modelCode.startsWith("UF") || modelCode.startsWith("UE") -> BmwChassis.E87
            modelCode.startsWith("1Z") -> BmwChassis.I01
            modelCode.startsWith("21") || modelCode.startsWith("23") -> BmwChassis.U06
            modelCode.startsWith("61") || modelCode.startsWith("63") || modelCode.startsWith("71") -> BmwChassis.U11
            else -> BmwChassis.F10
        }
    }

    /**
     * Parses UDS 0x19 0x02 (reportDTCByStatusMask) or 0x19 0x15 (reportDTCWithPermanentStatus) response payload:
     * payload[0] = subfunction (0x02 or 0x15), payload[1] = statusAvailabilityMask,
     * followed by 4-byte records: [DTC_High, DTC_Mid, DTC_Low, Status].
     */
    fun parseDtcRecords(
        ecuAddress: Int,
        ecuName: String,
        payload: ByteArray,
        odometerKm: Int,
        voltage: Double,
        memoryType: DtcMemoryType = DtcMemoryType.ERROR_MEMORY
    ): List<DtcFault> {
        if (payload.size < 6) return emptyList()
        val result = mutableListOf<DtcFault>()
        var idx = 2
        while (idx + 3 < payload.size) {
            val b0 = payload[idx].toInt() and 0xFF
            val b1 = payload[idx + 1].toInt() and 0xFF
            val b2 = payload[idx + 2].toInt() and 0xFF
            val statusByte = payload[idx + 3].toInt() and 0xFF
            val codeHex = "%02X%02X%02X".format(b0, b1, b2)
            val isActive = (statusByte and 0x01) != 0
            val isConfirmed = (statusByte and 0x08) != 0
            val dict = BmwDtcDictionary.lookup(codeHex)
            val resolvedMemory = when {
                memoryType != DtcMemoryType.ERROR_MEMORY -> memoryType
                !isActive && !isConfirmed -> DtcMemoryType.INFO_MEMORY
                else -> DtcMemoryType.ERROR_MEMORY
            }
            result.add(
                DtcFault(
                    id = "phys-${resolvedMemory.name.lowercase()}-${ecuAddress}-${codeHex}",
                    code = codeHex,
                    ecuAddress = ecuAddress,
                    ecuName = ecuName,
                    title = dict?.title ?: "$ecuName Arıza Kodu ($codeHex)",
                    technicalDetail = dict?.detail ?: "Fiziksel ECU üzerinden UDS 0x19 ile okundu (Status=0x%02X, ${resolvedMemory.label}).".format(statusByte),
                    severity = dict?.severity ?: when {
                        resolvedMemory == DtcMemoryType.INFO_MEMORY -> DtcSeverity.SHADOW
                        isActive -> DtcSeverity.WARNING
                        isConfirmed -> DtcSeverity.INFO
                        else -> DtcSeverity.SHADOW
                    },
                    status = when {
                        resolvedMemory == DtcMemoryType.INFO_MEMORY -> DtcStatus.SHADOW
                        isActive -> DtcStatus.ACTIVE
                        else -> DtcStatus.STORED
                    },
                    freezeFrame = FreezeFrameData(odometerKm, voltage, 0, 0, 1),
                    istaGuidance = dict?.guidance ?: "Detaylı test planı için $ecuName canlı sensör verilerini kontrol edin.",
                    memoryType = resolvedMemory
                )
            )
            idx += 4
        }
        return result
    }

    fun parseOdometerKm(payload: ByteArray): Int? {
        if (payload.size < 5) return null
        // Payload after 2-byte DID: 3-byte big-endian km
        val km = ((payload[2].toInt() and 0xFF) shl 16) or
            ((payload[3].toInt() and 0xFF) shl 8) or
            (payload[4].toInt() and 0xFF)
        return km.takeIf { it in 1..1_500_000 }
    }

    private val iLevelRegex = Regex("[FEGSI][0-9A-Z]{2,3}-\\d{2}-\\d{2}-\\d{3}")
    private val cafdRegex = Regex("CAFD_[0-9A-F]{8}_[0-9]{3}_[0-9]{3}_[0-9]{3}")

    fun parseILevel(payload: ByteArray): String? {
        if (payload.size <= 2) return null
        val ascii = String(payload.copyOfRange(2, payload.size), StandardCharsets.US_ASCII)
            .replace(Regex("[^A-Za-z0-9._-]"), "")
        return iLevelRegex.find(ascii)?.value ?: ascii.takeIf { it.length >= 6 }
    }

    fun parseCafdId(payload: ByteArray): String? {
        if (payload.size <= 2) return null
        val ascii = String(payload.copyOfRange(2, payload.size), StandardCharsets.US_ASCII)
            .uppercase()
        return cafdRegex.find(ascii)?.value
    }

    fun parseSoftwareVersion(payload: ByteArray): String? {
        if (payload.size <= 2) return null
        val clean = String(payload.copyOfRange(2, payload.size), StandardCharsets.US_ASCII)
            .replace(Regex("[^A-Za-z0-9._-]"), "")
            .trim()
        return clean.takeIf { it.length in 3..24 }
    }

    /**
     * Decodes physical UDS 0x22 positive response payload (`[didHi, didLo, A, B, ...]`)
     * into engineering units for any PID defined in [BmwEcuAndPidCatalog].
     */
    fun parsePhysicalPidValue(pidId: String, payload: ByteArray): Double? {
        if (payload.size < 3) return null
        val a = payload[2].toInt() and 0xFF
        val b = if (payload.size > 3) payload[3].toInt() and 0xFF else 0
        val u16 = (a shl 8) or b
        val s16 = u16.toShort().toInt()
        return when (pidId) {
            "rpm" -> u16 / 4.0
            "speed" -> a.toDouble()
            "boost", "turbo_target" -> (a - 100) / 100.0
            "coolant", "oil_temp", "iat" -> (a - 40).toDouble()
            "trans_temp" -> if (payload.size >= 4) (u16 / 10.0) - 40.0 else (a - 40).toDouble()
            "rail" -> (u16 * 10.0) / 100.0
            "load", "throttle", "alt_load" -> (a * 100.0) / 255.0
            "batt" -> if (payload.size >= 4) u16 / 1000.0 else (a / 10.0)
            "dpf_diff" -> u16 / 10.0
            "dpf_soot" -> u16 / 100.0
            "dpf_regen_km" -> u16.toDouble()
            "inj_1", "inj_2", "inj_3", "inj_4" -> s16 / 100.0
            else -> null
        }
    }

    fun isNegativeResponse(payload: ByteArray): Boolean =
        payload.isNotEmpty() && (payload[0].toInt() and 0xFF) == 0x7F

    fun isSecurityAccessDenied(payload: ByteArray): Boolean =
        payload.size >= 3 && (payload[0].toInt() and 0xFF) == 0x7F && (payload[2].toInt() and 0xFF) == 0x33

    fun isResponsePending(payload: ByteArray): Boolean =
        payload.size >= 3 && (payload[0].toInt() and 0xFF) == 0x7F && (payload[2].toInt() and 0xFF) == 0x78

    /**
     * Parses KWP2000 0x18 (0x58 positive) DTC records for E-Series Pre-LCI K-Line ECUs.
     */
    fun parseKwpDtcRecords(
        ecuAddress: Int,
        ecuName: String,
        rawResponse: ByteArray,
        odometerKm: Int,
        voltage: Double
    ): List<DtcFault> {
        val pairs = Kwp2000Codec.parseDtcResponse(rawResponse)
        return pairs.map { (codeHex, statusByte) ->
            val isActive = (statusByte and 0x20) != 0 || (statusByte and 0x01) != 0
            val dict = BmwDtcDictionary.lookup(codeHex)
            DtcFault(
                id = "kwp-$ecuAddress-$codeHex",
                code = codeHex,
                ecuAddress = ecuAddress,
                ecuName = ecuName,
                title = dict?.title ?: "$ecuName KWP2000 Arıza ($codeHex)",
                technicalDetail = dict?.detail ?: "K-Line KWP2000 0x18 üzerinden okundu (Status=0x%02X).".format(statusByte),
                severity = dict?.severity ?: if (isActive) DtcSeverity.WARNING else DtcSeverity.INFO,
                status = if (isActive) DtcStatus.ACTIVE else DtcStatus.STORED,
                freezeFrame = FreezeFrameData(odometerKm, voltage, 0, 0, 1),
                istaGuidance = dict?.guidance ?: "E-Serisi K-Line modülü canlı verilerini kontrol edin.",
                memoryType = if (isActive) DtcMemoryType.ERROR_MEMORY else DtcMemoryType.INFO_MEMORY
            )
        }
    }

    /**
     * Parses UDS 0x19 0x04 (0x59 0x04 positive) Freeze-Frame snapshot payload containing DID pairs
     * (0x100C odometer, 0xF405 coolant, 0xF442 voltage, 0xF40C rpm).
     */
    fun parseDtcSnapshotFreezeFrame(payload: ByteArray, fallback: FreezeFrameData): FreezeFrameData {
        if (payload.size < 8) return fallback
        var km = fallback.mileageKm
        var coolant = fallback.coolantTempC
        var volts = fallback.batteryVoltage
        var rpm = fallback.engineRpm
        val occurrences = (payload[5].toInt() and 0xFF).coerceAtLeast(1)

        var idx = 7 // Skip [0x04, dtc3, status, recNum, didCount]
        while (idx + 2 < payload.size) {
            val did = ((payload[idx].toInt() and 0xFF) shl 8) or (payload[idx + 1].toInt() and 0xFF)
            when (did) {
                0x100C -> if (idx + 4 < payload.size) {
                    km = ((payload[idx + 2].toInt() and 0xFF) shl 16) or
                        ((payload[idx + 3].toInt() and 0xFF) shl 8) or
                        (payload[idx + 4].toInt() and 0xFF)
                    idx += 5
                } else return FreezeFrameData(km, volts, coolant, rpm, occurrences)
                0xF405 -> {
                    coolant = (payload[idx + 2].toInt() and 0xFF) - 40
                    idx += 3
                }
                0xF442 -> {
                    volts = (payload[idx + 2].toInt() and 0xFF) / 10.0
                    idx += 3
                }
                0xF40C -> if (idx + 3 < payload.size) {
                    rpm = (((payload[idx + 2].toInt() and 0xFF) shl 8) or (payload[idx + 3].toInt() and 0xFF)) / 4
                    idx += 4
                } else return FreezeFrameData(km, volts, coolant, rpm, occurrences)
                else -> idx += 3
            }
        }
        return FreezeFrameData(km, volts, coolant, rpm, occurrences)
    }

    fun parseIbsHealth(payload: ByteArray): IbsBatteryHealth? {
        if (payload.size < 6) return null
        val soc = (payload[2].toInt() and 0xFF).coerceIn(0, 100)
        val soh = (payload[3].toInt() and 0xFF).coerceIn(0, 100)
        val cap = (payload[4].toInt() and 0xFF).coerceIn(40, 120)
        val isAgm = (payload[5].toInt() and 0xFF) != 0
        val history = if (payload.size >= 11) {
            (6..10).map { (payload[it].toInt() and 0xFF).coerceIn(0, 100) }
        } else {
            listOf((soc + 3).coerceAtMost(100), (soc + 1).coerceAtMost(100), soc, (soc - 1).coerceAtLeast(0), soc)
        }
        return IbsBatteryHealth(soc, soh, cap, isAgm, history)
    }

    fun parseDpfHealth(payload: ByteArray): DpfHealthStatus? {
        if (payload.size < 10) return null
        val soot = (((payload[2].toInt() and 0xFF) shl 8) or (payload[3].toInt() and 0xFF)) / 10.0
        val ash = (((payload[4].toInt() and 0xFF) shl 8) or (payload[5].toInt() and 0xFF)) / 10.0
        val diff = (payload[6].toInt() and 0xFF).toDouble()
        val kmSince = ((payload[7].toInt() and 0xFF) shl 8) or (payload[8].toInt() and 0xFF)
        val preTemp = (payload[9].toInt() and 0xFF) * 4
        val postTemp = if (payload.size > 10) (payload[10].toInt() and 0xFF) * 4 else (preTemp - 20).coerceAtLeast(0)
        val active = payload.size > 11 && (payload[11].toInt() and 0x01) != 0
        return DpfHealthStatus(soot, ash, diff, kmSince, preTemp, postTemp, active, active, "Live ECU")
    }

    fun parseZfGearboxHealth(payload: ByteArray): ZfGearboxHealth? {
        if (payload.size < 14) return null
        val names = listOf("Kavrama A", "Kavrama B", "Kavrama C", "Kavrama D", "Kavrama E")
        val clutches = names.mapIndexed { i, name ->
            val p = (payload[2 + i * 2].toInt().toByte().toInt()) * 5
            val f = (payload[3 + i * 2].toInt().toByte().toInt()) * 2
            val level = when {
                kotlin.math.abs(p) > 250 || kotlin.math.abs(f) > 40 -> ClutchWearLevel.CRITICAL_WEAR
                kotlin.math.abs(p) > 120 || kotlin.math.abs(f) > 20 -> ClutchWearLevel.MODERATE_WEAR
                else -> ClutchWearLevel.OPTIMAL
            }
            ZfClutchDetail(name, p, f, level, if (level == ClutchWearLevel.OPTIMAL) "Nominal" else "Adaptasyon sapması")
        }
        val oilTemp = (payload[12].toInt() and 0xFF) - 40
        val oilScore = (payload[13].toInt() and 0xFF).coerceIn(10, 100)
        val overall = if (clutches.any { it.wearLevel == ClutchWearLevel.CRITICAL_WEAR }) 68 else 94
        return ZfGearboxHealth(clutches, oilTemp, oilScore, overall, "ZF8 Canlı Okuma")
    }

    fun parseInjectorIma(cylinder: Int, payload: ByteArray): InjectorCalibration? {
        if (payload.size < 4) return null
        val raw = String(payload.copyOfRange(2, payload.size), StandardCharsets.US_ASCII)
            .replace(Regex("[^A-Za-z0-9]"), "")
            .uppercase()
        return raw.takeIf { it.length >= 5 }?.let { InjectorCalibration(cylinder, it.take(8)) }
    }

    fun parseShortCircuitCounters(ecuName: String, ecuAddress: Int, payload: ByteArray): List<ShortCircuitChannel> {
        if (payload.size < 4) return emptyList()
        val channelLabels = listOf(
            "sc_low_beam_l" to "Sol Kısa Far (Low Beam L)",
            "sc_low_beam_r" to "Sağ Kısa Far (Low Beam R)",
            "sc_angel_l" to "Sol Angel Eyes / DRL Çıkışı",
            "sc_plate" to "Arka Plaka Aydınlatması",
            "sc_fog_r" to "Sağ Ön Sis Farı Çıkışı"
        )
        return channelLabels.mapIndexedNotNull { idx, (id, label) ->
            val byteIdx = 2 + idx
            if (byteIdx >= payload.size) return@mapIndexedNotNull null
            val count = (payload[byteIdx].toInt() and 0xFF).coerceAtMost(50)
            ShortCircuitChannel(id, label, ecuName, ecuAddress, idx + 1, count, count >= 50)
        }
    }

    fun parseSecuritySeed(payload: ByteArray): ByteArray? {
        if (payload.size < 6) return null
        // Positive response to 0x27: payload[0] = 0x67, payload[1] = level (0x01 or 0x03), payload[2..5] = seed
        if ((payload[0].toInt() and 0xFF) != 0x67) return null
        return payload.copyOfRange(2, 6)
    }
}
