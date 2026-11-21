package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.BmwDtcDictionary
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.DtcStatus
import com.punhanaghayev.bimmerpro.core.bmw.FreezeFrameData
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
            else -> BmwChassis.F10
        }
    }

    /**
     * Parses UDS 0x19 0x02 (reportDTCByStatusMask) response payload:
     * payload[0] = subfunction (0x02), payload[1] = statusAvailabilityMask,
     * followed by 4-byte records: [DTC_High, DTC_Mid, DTC_Low, Status].
     */
    fun parseDtcRecords(ecuAddress: Int, ecuName: String, payload: ByteArray, odometerKm: Int, voltage: Double): List<DtcFault> {
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
            result.add(
                DtcFault(
                    id = "phys-${ecuAddress}-${codeHex}",
                    code = codeHex,
                    ecuAddress = ecuAddress,
                    ecuName = ecuName,
                    title = dict?.title ?: "$ecuName Arıza Kodu ($codeHex)",
                    technicalDetail = dict?.detail ?: "Fiziksel ECU üzerinden UDS 0x19 0x02 ile okundu (Status=0x%02X).".format(statusByte),
                    severity = dict?.severity ?: when {
                        isActive -> DtcSeverity.WARNING
                        isConfirmed -> DtcSeverity.INFO
                        else -> DtcSeverity.SHADOW
                    },
                    status = if (isActive) DtcStatus.ACTIVE else DtcStatus.STORED,
                    freezeFrame = FreezeFrameData(odometerKm, voltage, 0, 0, 1),
                    istaGuidance = dict?.guidance ?: "Detaylı test planı için $ecuName canlı sensör verilerini kontrol edin."
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

    fun parseSecuritySeed(payload: ByteArray): ByteArray? {
        if (payload.size < 6) return null
        // Positive response to 0x27: payload[0] = 0x67, payload[1] = level (0x01 or 0x03), payload[2..5] = seed
        if ((payload[0].toInt() and 0xFF) != 0x67) return null
        return payload.copyOfRange(2, 6)
    }
}
