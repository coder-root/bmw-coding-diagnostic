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

    fun extractVin(payload: ByteArray): String? {
        if (payload.isEmpty()) return null
        val ascii = String(payload, StandardCharsets.US_ASCII)
            .replace(Regex("[^A-Za-z0-9]"), "")
            .uppercase()
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
            modelCode.startsWith("VB") || modelCode.startsWith("VA") || modelCode.startsWith("PG") -> BmwChassis.E90
            modelCode.startsWith("NA") || modelCode.startsWith("NE") || modelCode.startsWith("NU") -> BmwChassis.E60
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
}
