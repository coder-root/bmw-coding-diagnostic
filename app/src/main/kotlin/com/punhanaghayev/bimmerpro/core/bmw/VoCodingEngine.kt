package com.punhanaghayev.bimmerpro.core.bmw

import com.punhanaghayev.bimmerpro.core.protocol.CafdBitmaskEngine

data class VoCodingDiffItem(
    val ecuAddress: Int,
    val ecuName: String,
    val cafdId: String,
    val functionName: String,
    val byteOffset: Int,
    val bitMask: Int,
    val beforeByteHex: String,
    val afterByteHex: String,
    val targetOptionLabel: String
)

data class VoCodingPlan(
    val voCode: String,
    val description: String,
    val diffs: List<VoCodingDiffItem>,
    val affectedEcuNames: List<String>
)

enum class FscCertificateStatus(val label: String) {
    ACCEPTED("Accepted (Aktif)"),
    REJECTED("Rejected (Geçersiz)"),
    NOT_LOADED("Not Loaded (Yok)"),
    EXPIRED("Expired (Süresi Doldu)")
}

data class FscCertificateEntry(
    val appIdHex: String,
    val upgradeIndex: String,
    val title: String,
    val ecuName: String,
    val ecuAddress: Int,
    val status: FscCertificateStatus
)

/**
 * E-Sys style FA/VO -> CAFD Coding Calculator with binary/hex byte diff preview,
 * plus FSC (Freischaltcode) activation status decoder for HU_NBT / HU_MGU / KAFAS.
 */
object VoCodingEngine {

    private data class VoCafdRule(
        val ecuAddress: Int,
        val ecuName: String,
        val cafdId: String,
        val functionName: String,
        val byteOffset: Int,
        val bitMask: Int,
        val newValue: Int,
        val optionLabel: String
    )

    private val voRules: Map<String, List<VoCafdRule>> = mapOf(
        "2TB" to listOf(
            VoCafdRule(EcuAddress.EGS, "EGS", "CAFD_0000023F_016_016", "SPORTSCHALTER / SAT_ACTIVATION", 0x04, 0x03, 0x03, "aktiv (Sport+ & Launch Control)"),
            VoCafdRule(EcuAddress.KOMBI, "KOMBI", "CAFD_00000069_009_002", "SPORT_ANZEIGE_ENABLE", 0x08, 0x01, 0x01, "aktiv (S1-S8 Gear Display)"),
            VoCafdRule(EcuAddress.DSC, "DSC", "CAFD_000002A3_004_001", "C_Funktion_Sport_Plus", 0x05, 0x01, 0x01, "aktiv")
        ),
        "6WB" to listOf(
            VoCafdRule(EcuAddress.KOMBI, "KOMBI", "CAFD_00000069_009_002", "KOMBI_HIGH_ENABLE", 0x02, 0x0F, 0x0A, "aktiv (6WB Multifunctional)"),
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT", "CAFD_00000DED_003_015", "KOMBI_CIC / MOST_EXTENDED", 0x10, 0x03, 0x02, "aktiv (MOST Display Sync)")
        ),
        "6CP" to listOf(
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT/MGU", "CAFD_00000DED_003_015", "CARPLAY_FULLSCREEN_WIFI", 0x14, 0x03, 0x03, "aktiv (Apple CarPlay)"),
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT/MGU", "CAFD_00000DED_003_015", "WLAN_STATUS_CARPLAY", 0x15, 0x01, 0x01, "aktiv")
        ),
        "552" to listOf(
            VoCafdRule(EcuAddress.FEM_CAS, "FEM/BDC", "CAFD_00000794_014_040", "C_HBA_ENA / ADAPTIVE_LED", 0x06, 0x03, 0x03, "aktiv (Adaptive Full LED)"),
            VoCafdRule(EcuAddress.KOMBI, "KOMBI", "CAFD_00000069_009_002", "HIGH_BEAM_ASSIST_ICON", 0x09, 0x01, 0x01, "aktiv")
        ),
        "5AC" to listOf(
            VoCafdRule(EcuAddress.FEM_CAS, "FEM/BDC", "CAFD_00000794_014_040", "FLA_AUTO_HIGHBEAM", 0x07, 0x01, 0x01, "aktiv"),
            VoCafdRule(EcuAddress.KAFAS_CAMERA, "KAFAS", "CAFD_00001148_001_008", "FLA_GLARE_FREE_MATRIX", 0x03, 0x03, 0x02, "aktiv")
        ),
        "3AG" to listOf(
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT/MGU", "CAFD_00000DED_003_015", "CAM_ECU / PDC_REAR_CAM", 0x12, 0x01, 0x01, "aktiv (TRSVC/ICAM)"),
            VoCafdRule(EcuAddress.REM, "REM/PDC", "CAFD_000007A1_005_041", "REAR_CAMERA_GUIDELINES", 0x04, 0x03, 0x03, "aktiv")
        ),
        "4UR" to listOf(
            VoCafdRule(EcuAddress.FEM_CAS, "FEM/BDC", "CAFD_00000794_014_040", "AMBIENTE_NACHFUEHRUNG", 0x0B, 0x07, 0x05, "aktiv (Multi-Color)"),
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT/MGU", "CAFD_00000DED_003_015", "COLOUR_SWITCH_ENSEMBLE", 0x18, 0x01, 0x01, "aktiv")
        ),
        "6NS" to listOf(
            VoCafdRule(EcuAddress.HU_MEDIA, "HU_NBT/MGU", "CAFD_00000DED_003_015", "ENHANCED_BT_AUDIO_2PHONE", 0x1A, 0x03, 0x03, "aktiv (6NS Enhanced BT)")
        )
    )

    fun buildCodingPlan(
        voCode: String,
        description: String = "",
        ecuBuffers: Map<Int, ByteArray> = emptyMap()
    ): VoCodingPlan {
        val clean = voCode.trim().uppercase().removePrefix("S")
        val resolvedDescription = description.ifBlank { VoDependencyMapper.lookupOptionDescription(clean) }
        val rules = voRules[clean] ?: VoDependencyMapper.affectedEcusFor(clean).mapIndexed { idx, (addr, name) ->
            VoCafdRule(
                ecuAddress = addr,
                ecuName = name,
                cafdId = "CAFD_%04X_VO_%s".format(addr, clean),
                functionName = "VO_OPTION_${clean}_ENABLE",
                byteOffset = (0x04 + idx).coerceAtMost(0x1E),
                bitMask = 0x01,
                newValue = 0x01,
                optionLabel = "aktiv (+S$clean)"
            )
        }

        val diffs = rules.map { rule ->
            val buf = ecuBuffers[rule.ecuAddress] ?: ByteArray(64) { 0x00 }
            val safeOffset = rule.byteOffset.coerceIn(0, buf.lastIndex)
            val beforeByte = buf[safeOffset].toInt() and 0xFF
            val afterBuf = CafdBitmaskEngine.writeMaskedValue(buf, safeOffset, rule.bitMask, rule.newValue)
            val afterByte = afterBuf[safeOffset].toInt() and 0xFF
            VoCodingDiffItem(
                ecuAddress = rule.ecuAddress,
                ecuName = rule.ecuName,
                cafdId = rule.cafdId,
                functionName = rule.functionName,
                byteOffset = safeOffset,
                bitMask = rule.bitMask,
                beforeByteHex = "0x%02X".format(beforeByte),
                afterByteHex = "0x%02X".format(afterByte),
                targetOptionLabel = rule.optionLabel
            )
        }

        return VoCodingPlan(
            voCode = clean,
            description = resolvedDescription,
            diffs = diffs,
            affectedEcuNames = diffs.map { it.ecuName }.distinct()
        )
    }

    fun defaultFscCertificates(chassis: BmwChassis): List<FscCertificateEntry> {
        val isGOrU = chassis.family == SeriesFamily.G_SERIES || chassis.family == SeriesFamily.I_SERIES
        return listOf(
            FscCertificateEntry("0x00DE", "0001", "Navigation Map Europe/Next/EVO", if (isGOrU) "HU_MGU" else "HU_NBT", EcuAddress.HU_MEDIA, FscCertificateStatus.ACCEPTED),
            FscCertificateEntry("0x0143", "0001", "Apple CarPlay Fullscreen Preparation", if (isGOrU) "HU_MGU" else "HU_NBT", EcuAddress.HU_MEDIA, if (isGOrU) FscCertificateStatus.ACCEPTED else FscCertificateStatus.NOT_LOADED),
            FscCertificateEntry("0x0017", "0001", "Voice Recognition (Sprachverarbeitung)", if (isGOrU) "HU_MGU" else "HU_NBT", EcuAddress.HU_MEDIA, FscCertificateStatus.ACCEPTED),
            FscCertificateEntry("0x007E", "0001", "Speed Limit Info (SLI / Trafik İşareti)", "KAFAS", EcuAddress.KAFAS_CAMERA, if (chassis == BmwChassis.E90) FscCertificateStatus.NOT_LOADED else FscCertificateStatus.ACCEPTED),
            FscCertificateEntry("0x009C", "0001", "BMW Apps / ConnectedDrive Telematics", if (isGOrU) "HU_MGU" else "HU_NBT", EcuAddress.HU_MEDIA, FscCertificateStatus.ACCEPTED)
        )
    }

    /**
     * Parses UDS 0x22 0x3008 / 0x31 0x0F1F FSC status response payload:
     * repeated 4-byte entries: [appIdHi, appIdLo, upgradeIdx, statusCode (0=Accepted, 1=Rejected, 2=NotLoaded, 3=Expired)]
     */
    fun parseFscStatusPayload(
        ecuName: String,
        ecuAddress: Int,
        payload: ByteArray
    ): List<FscCertificateEntry> {
        if (payload.size < 6) return emptyList()
        val titles = mapOf(
            0x00DE to "Navigation Map License",
            0x0143 to "Apple CarPlay (6CP)",
            0x0017 to "Voice Recognition",
            0x007E to "Speed Limit Info (SLI)",
            0x009C to "BMW Apps / Live"
        )
        val out = mutableListOf<FscCertificateEntry>()
        var idx = 2
        while (idx + 3 < payload.size) {
            val appId = ((payload[idx].toInt() and 0xFF) shl 8) or (payload[idx + 1].toInt() and 0xFF)
            val upgrade = payload[idx + 2].toInt() and 0xFF
            val stByte = payload[idx + 3].toInt() and 0xFF
            val status = when (stByte) {
                0x00 -> FscCertificateStatus.ACCEPTED
                0x01 -> FscCertificateStatus.REJECTED
                0x03 -> FscCertificateStatus.EXPIRED
                else -> FscCertificateStatus.NOT_LOADED
            }
            out.add(
                FscCertificateEntry(
                    appIdHex = "0x%04X".format(appId),
                    upgradeIndex = "%04X".format(upgrade),
                    title = titles[appId] ?: "FSC App 0x%04X".format(appId),
                    ecuName = ecuName,
                    ecuAddress = ecuAddress,
                    status = status
                )
            )
            idx += 4
        }
        return out
    }
}
