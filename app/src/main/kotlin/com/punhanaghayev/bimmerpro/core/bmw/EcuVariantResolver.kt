package com.punhanaghayev.bimmerpro.core.bmw

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.charset.StandardCharsets

@Serializable
data class EcuCatalogEntryDto(
    val series: String = "",
    val ecuDescriptionFile: String = "",
    val identifier: String = "",
    val group: String = "",
    val name: String = ""
)

data class SvkInfo(
    val hwel: String,
    val btld: String,
    val swfl: String,
    val cafd: String?,
    val resolvedVariant: String
) {
    fun summaryText(): String = buildString {
        append("HWEL: $hwel • BTLD: $btld • SWFL: $swfl")
        if (!cafd.isNullOrBlank()) append(" • CAFD: $cafd")
    }
}

/**
 * Resolves physical ECU SVK (UDS 0x22 0xF101) part identifiers and maps ECU addresses
 * to exact BMW SGBD/PRG variants (e.g. MEVD1724, DDE73, BDC_G05, HU_MGU) via bmw_ecu_catalog.json.
 */
object EcuVariantResolver {

    private val json = Json { ignoreUnknownKeys = true }
    private var catalogEntries: List<EcuCatalogEntryDto> = emptyList()

    fun initializeFromDataPack(datapacksDir: File) {
        val file = File(datapacksDir, "bmw_ecu_catalog.json")
        if (!file.exists()) return
        runCatching {
            catalogEntries = json.decodeFromString<List<EcuCatalogEntryDto>>(file.readText())
        }
    }

    fun initializeFromAssetStream(input: java.io.InputStream) {
        runCatching {
            val raw = input.bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString<List<EcuCatalogEntryDto>>(raw)
            if (parsed.isNotEmpty()) catalogEntries = parsed
        }
    }

    fun catalogSize(): Int = catalogEntries.size

    fun parseSvk(
        ecuAddress: Int,
        chassis: BmwChassis,
        payload: ByteArray
    ): SvkInfo? {
        if (payload.size < 6) return null
        val data = if (payload.size >= 4 && (payload[0].toInt() and 0xFF) == 0xF1 && (payload[1].toInt() and 0xFF) == 0x01) {
            payload.copyOfRange(2, payload.size)
        } else {
            payload
        }

        // Try ASCII SVK payload first (e.g. "HWEL_00001234|BTLD_00001001|SWFL_000024C1|CAFD_000001A7")
        val ascii = String(data, StandardCharsets.US_ASCII)
        val hwelMatch = Regex("HWEL[_-]?[0-9A-F]{4,8}", RegexOption.IGNORE_CASE).find(ascii)?.value
        val btldMatch = Regex("BTLD[_-]?[0-9A-F]{4,8}", RegexOption.IGNORE_CASE).find(ascii)?.value
        val swflMatch = Regex("SWFL[_-]?[0-9A-F]{4,8}", RegexOption.IGNORE_CASE).find(ascii)?.value
        val cafdMatch = Regex("CAFD[_-]?[0-9A-F]{8}(?:_\\d{3}_\\d{3}_\\d{3})?", RegexOption.IGNORE_CASE).find(ascii)?.value

        if (hwelMatch != null || swflMatch != null) {
            val variant = resolveVariant(ecuAddress, chassis, swflMatch ?: hwelMatch ?: "")
            return SvkInfo(
                hwel = hwelMatch ?: "HWEL_LIVE",
                btld = btldMatch ?: "BTLD_LIVE",
                swfl = swflMatch ?: "SWFL_LIVE",
                cafd = cafdMatch,
                resolvedVariant = variant
            )
        }

        // Binary SVK fallback: 4-byte hex chunks [processClass + id]
        if (data.size >= 8) {
            val hwelHex = "%02X%02X%02X%02X".format(data[0], data[1], data[2], data[3])
            val swflHex = "%02X%02X%02X%02X".format(data[4], data[5], data[6], data[7])
            val variant = resolveVariant(ecuAddress, chassis, swflHex)
            return SvkInfo(
                hwel = "HWEL_$hwelHex",
                btld = "BTLD_%02X%02X".format(data[2], data[3]),
                swfl = "SWFL_$swflHex",
                cafd = null,
                resolvedVariant = variant
            )
        }
        return null
    }

    fun resolveVariant(ecuAddress: Int, chassis: BmwChassis, swHint: String = ""): String {
        val addrHex = "%02X".format(ecuAddress and 0xFF)
        val seriesPrefix = when (chassis.family) {
            SeriesFamily.E_SERIES -> chassis.code
            SeriesFamily.F_SERIES -> "F"
            SeriesFamily.G_SERIES -> "G"
            SeriesFamily.I_SERIES -> "I"
        }

        val catalogMatch = catalogEntries.firstOrNull { entry ->
            entry.identifier.startsWith(addrHex, ignoreCase = true) &&
                (entry.series.equals(chassis.code, true) || entry.series.startsWith(seriesPrefix, true))
        }
        if (catalogMatch != null && catalogMatch.ecuDescriptionFile.isNotBlank()) {
            return "${catalogMatch.ecuDescriptionFile} (${catalogMatch.name})"
        }

        return when (ecuAddress and 0xFF) {
            EcuAddress.DME_DDE -> when {
                chassis == BmwChassis.E90 || chassis == BmwChassis.E60 -> "MSD80 / MSV80 / DDE6"
                chassis == BmwChassis.F10 && swHint.contains("24C", true) -> "DDE73 (N47/N57)"
                chassis.family == SeriesFamily.F_SERIES -> "MEVD1724 / DDE7"
                chassis == BmwChassis.U11 || chassis == BmwChassis.U06 -> "CCU_P1 / DME9"
                else -> "DME8FF / DME8 (B48/B58)"
            }
            EcuAddress.ZGW_BDC -> when (chassis.family) {
                SeriesFamily.E_SERIES -> "JBBF / KGM"
                SeriesFamily.F_SERIES -> if (chassis == BmwChassis.F10) "ZGW2_4SK" else "FEM_20 / ZGW"
                else -> if (chassis == BmwChassis.U11 || chassis == BmwChassis.U06) "BCP_SP21" else "BDC_G05 / BDC3"
            }
            EcuAddress.FEM_CAS -> if (chassis.family == SeriesFamily.E_SERIES) "CAS3" else "CAS4 / FEM_BODY"
            EcuAddress.HU_MEDIA -> when {
                chassis.family == SeriesFamily.E_SERIES -> "CIC / CCC"
                chassis.family == SeriesFamily.F_SERIES -> "HU_NBT / NBT_EVO"
                chassis == BmwChassis.U11 || chassis == BmwChassis.U06 -> "HU_OS9 / IDC23"
                else -> "HU_MGU (iDrive 7/8)"
            }
            EcuAddress.EGS -> "EGS_8HP (GS8HP)"
            EcuAddress.KOMBI -> if (chassis.family == SeriesFamily.G_SERIES) "DKOMBI4 / Live Cockpit" else "KOMBI_L7 / 6WB"
            EcuAddress.DSC -> if (chassis.family == SeriesFamily.G_SERIES) "DSC_SP15 / MK100" else "DSC_10"
            EcuAddress.ACSM -> if (chassis.family == SeriesFamily.G_SERIES) "ACSM5" else "ACSM4"
            EcuAddress.KAFAS_CAMERA -> if (chassis.family == SeriesFamily.G_SERIES) "KAFAS4" else "KAFAS2"
            EcuAddress.SME_HV_BATTERY -> "SME_SP21 (HV Battery)"
            EcuAddress.ASD -> "ASD_10 / IconicSounds"
            else -> BmwEcuAndPidCatalog.allBmwEcus.firstOrNull { it.address == ecuAddress }?.shortName ?: "ECU_0x$addrHex"
        }
    }
}
