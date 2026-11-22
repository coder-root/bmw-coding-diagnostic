package com.punhanaghayev.bimmerpro.core.bmw

enum class EngineDidProfile(
    val displayName: String,
    val ecuFamilyTag: String
) {
    DDE7_DIESEL("F-Serisi Dizel (N47 / N57 / B47)", "DDE7 / DDE73"),
    MEVD17_F_PETROL("F-Serisi Benzin Turbo (N20 / N55 / S55)", "MEVD1724 / MEVD17"),
    MG1_MD1_G_SERIES("G/U-Serisi Modern (B48 / B58 / B47 / S58)", "DME8 / MG1CS / MD1"),
    MSD80_E_SERIES("E-Serisi Pre-LCI / LCI (N52 / N54 / M57)", "MSD80 / MSV80 / DDE6")
}

/**
 * Engine-family specific BMW UDS 0x22 DID profile resolver.
 * Adapts live telemetry and Pro-Tuner DIDs dynamically based on SVK variant, engine code, and chassis.
 */
object BmwMotorDidProfiles {

    private val profileDidOverrides: Map<EngineDidProfile, Map<String, Int>> = mapOf(
        EngineDidProfile.DDE7_DIESEL to mapOf(
            "rpm" to 0xF40C,
            "boost" to 0x5812,
            "turbo_target" to 0x5813,
            "rail" to 0x5820,
            "dpf_diff" to 0x5821,
            "dpf_soot" to 0x5822,
            "dpf_regen_km" to 0x5825,
            "inj_1" to 0x5831,
            "inj_2" to 0x5832,
            "inj_3" to 0x5833,
            "inj_4" to 0x5834
        ),
        EngineDidProfile.MEVD17_F_PETROL to mapOf(
            "rpm" to 0xF40C,
            "boost" to 0xF40B,
            "turbo_target" to 0x5840,
            "timing_corr" to 0x5860,
            "timing_corr_2" to 0x5868,
            "timing_corr_3" to 0x5869,
            "timing_corr_4" to 0x586A,
            "timing_corr_5" to 0x586B,
            "timing_corr_6" to 0x586C,
            "vanos_in_target" to 0x586D,
            "vanos_in" to 0x5864,
            "hpfp_target" to 0x5865,
            "hpfp_actual" to 0x5866,
            "lambda_afr" to 0x5863
        ),
        EngineDidProfile.MG1_MD1_G_SERIES to mapOf(
            "rpm" to 0x420C,
            "speed" to 0x420D,
            "boost" to 0x421B,
            "turbo_target" to 0x421C,
            "oil_temp" to 0x4225,
            "timing_corr" to 0x5910,
            "timing_corr_2" to 0x5911,
            "timing_corr_3" to 0x5912,
            "timing_corr_4" to 0x5913,
            "timing_corr_5" to 0x5914,
            "timing_corr_6" to 0x5915,
            "vanos_in_target" to 0x5920,
            "vanos_in" to 0x5921,
            "hpfp_target" to 0x5930,
            "hpfp_actual" to 0x5931,
            "lambda_afr" to 0x5940,
            "misfire_total" to 0x5950
        ),
        EngineDidProfile.MSD80_E_SERIES to mapOf(
            "rpm" to 0xF40C,
            "boost" to 0xF40B,
            "coolant" to 0xF405,
            "oil_temp" to 0x5805,
            "timing_corr" to 0x2011,
            "timing_corr_2" to 0x2012,
            "timing_corr_3" to 0x2013,
            "timing_corr_4" to 0x2014,
            "timing_corr_5" to 0x2015,
            "timing_corr_6" to 0x2016,
            "hpfp_target" to 0x2040,
            "hpfp_actual" to 0x2041,
            "lambda_afr" to 0xF424
        )
    )

    fun detectProfile(
        engineCode: String,
        chassis: BmwChassis,
        ecus: List<EcuModule> = emptyList()
    ): EngineDidProfile {
        val dmeVariant = ecus.firstOrNull { it.address == EcuAddress.DME_DDE }?.variantName.orEmpty().uppercase()
        val eng = engineCode.uppercase()

        return when {
            dmeVariant.contains("DDE7") || eng.contains("N47") || eng.contains("N57") ||
                (eng.contains("B47") && chassis.family == SeriesFamily.F_SERIES) -> EngineDidProfile.DDE7_DIESEL
            dmeVariant.contains("MSD8") || dmeVariant.contains("MSV8") || dmeVariant.contains("DDE6") ||
                chassis.family == SeriesFamily.E_SERIES -> EngineDidProfile.MSD80_E_SERIES
            dmeVariant.contains("MG1") || dmeVariant.contains("MD1") || dmeVariant.contains("DME8") ||
                dmeVariant.contains("DME9") || chassis.family == SeriesFamily.G_SERIES ||
                eng.contains("B48") || eng.contains("B58") || eng.contains("S58") -> EngineDidProfile.MG1_MD1_G_SERIES
            else -> EngineDidProfile.MEVD17_F_PETROL
        }
    }

    fun overrideDidFor(pidId: String, profile: EngineDidProfile): Int? =
        profileDidOverrides[profile]?.get(pidId)

    fun resolveLiveCatalogFor(profile: EngineDidProfile): List<BmwLivePidDefinition> {
        val overrides = profileDidOverrides[profile].orEmpty()
        return BmwEcuAndPidCatalog.livePidDefinitions.map { def ->
            val mappedDid = overrides[def.id]
            if (mappedDid != null) def.copy(did = mappedDid) else def
        }
    }
}
