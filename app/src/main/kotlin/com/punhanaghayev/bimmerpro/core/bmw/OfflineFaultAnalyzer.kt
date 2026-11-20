package com.punhanaghayev.bimmerpro.core.bmw

import com.punhanaghayev.bimmerpro.core.i18n.AppStrings

enum class SubsystemKey {
    ENGINE_TURBO,
    DPF_EXHAUST,
    ZF8_TRANSMISSION,
    BATTERY_IBS
}

data class SubsystemHealthScore(
    val name: String,
    val scorePercent: Int,
    val note: String,
    val key: SubsystemKey = SubsystemKey.ENGINE_TURBO,
    val isNominal: Boolean = true,
    val paramValue: Int = 0
) {
    fun localizedName(strings: AppStrings): String = when (key) {
        SubsystemKey.ENGINE_TURBO -> strings.subsystemEngineTurbo
        SubsystemKey.DPF_EXHAUST -> strings.subsystemDpfExhaust
        SubsystemKey.ZF8_TRANSMISSION -> strings.subsystemZf8
        SubsystemKey.BATTERY_IBS -> strings.subsystemBatteryIbs
    }

    fun localizedNote(strings: AppStrings): String = when (key) {
        SubsystemKey.ENGINE_TURBO -> if (isNominal) strings.healthNominal else strings.healthFaultCheck
        SubsystemKey.DPF_EXHAUST -> if (isNominal) strings.healthDpfClean else strings.healthDpfHigh
        SubsystemKey.ZF8_TRANSMISSION -> strings.zfClutchDevLabel.format(paramValue)
        else -> note
    }
}

enum class DiagnosticRecommendationType {
    CRITICAL_DTCS,
    WARNING_DTCS,
    DPF_SOOT_HIGH,
    BATTERY_LOW,
    HEALTHY
}

data class DiagnosticRecommendation(
    val type: DiagnosticRecommendationType,
    val count: Int = 0,
    val value: Double = 0.0,
    val rawFallback: String = ""
) {
    fun localized(strings: AppStrings): String = when (type) {
        DiagnosticRecommendationType.CRITICAL_DTCS -> strings.recCriticalDtcs.format(count)
        DiagnosticRecommendationType.WARNING_DTCS -> strings.recWarningDtcs.format(count)
        DiagnosticRecommendationType.DPF_SOOT_HIGH -> strings.recDpfSootHigh.format(value)
        DiagnosticRecommendationType.BATTERY_LOW -> strings.recBatteryLow.format(value)
        DiagnosticRecommendationType.HEALTHY -> strings.recHealthy
    }
}

data class OfflineDiagnosticReport(
    val overallScore: Int,
    val subsystemScores: List<SubsystemHealthScore>,
    val recommendationItems: List<DiagnosticRecommendation> = emptyList(),
    val recommendations: List<String> = emptyList()
)

/**
 * Offline fault correlation & predictive maintenance analyzer combining DTCs, live channels, and IBS health.
 */
object OfflineFaultAnalyzer {

    fun analyze(
        dtcs: List<DtcFault>,
        liveChannels: List<LiveSensorChannel>,
        ibsHealth: IbsBatteryHealth?,
        batteryVoltage: Double,
        zfAdaptations: List<ZfClutchAdaptation>
    ): OfflineDiagnosticReport {
        val criticalCount = dtcs.count { it.severity == DtcSeverity.CRITICAL }
        val warningCount = dtcs.count { it.severity == DtcSeverity.WARNING }

        val enginePenalty = dtcs.count { it.ecuAddress == EcuAddress.DME_DDE && it.severity != DtcSeverity.SHADOW } * 18
        val engineScore = (100 - enginePenalty).coerceIn(25, 100)

        val dpfSoot = liveChannels.firstOrNull { it.id == "dpf_soot" }?.currentValue ?: 0.0
        val dpfDtcPenalty = if (dtcs.any { it.code.startsWith("24") }) 25 else 0
        val dpfSootPenalty = if (dpfSoot > 32.0) 20 else if (dpfSoot > 24.0) 10 else 0
        val dpfScore = (100 - dpfDtcPenalty - dpfSootPenalty).coerceIn(20, 100)

        val maxClutchPressure = zfAdaptations.maxOfOrNull { kotlin.math.abs(it.pressureMbar) } ?: 0
        val zfPenalty = if (maxClutchPressure > 250) 25 else if (maxClutchPressure > 100) 10 else 0
        val transScore = (100 - zfPenalty).coerceIn(30, 100)

        val battBase = ibsHealth?.stateOfHealthPercent ?: 85
        val voltPenalty = if (batteryVoltage < 12.2) 25 else 0
        val battScore = (battBase - voltPenalty).coerceIn(15, 100)

        val overall = (engineScore + dpfScore + transScore + battScore) / 4

        val recItems = buildList {
            if (criticalCount > 0) add(DiagnosticRecommendation(DiagnosticRecommendationType.CRITICAL_DTCS, count = criticalCount))
            if (warningCount > 0) add(DiagnosticRecommendation(DiagnosticRecommendationType.WARNING_DTCS, count = warningCount))
            if (dpfSoot > 32.0) add(DiagnosticRecommendation(DiagnosticRecommendationType.DPF_SOOT_HIGH, value = dpfSoot))
            if (batteryVoltage < 12.2) add(DiagnosticRecommendation(DiagnosticRecommendationType.BATTERY_LOW, value = batteryVoltage))
            if (isEmpty()) add(DiagnosticRecommendation(DiagnosticRecommendationType.HEALTHY))
        }

        val recs = buildList {
            if (criticalCount > 0) add("$criticalCount kritik arıza kodu aktif — sürüş öncesi kontrol edin.")
            if (warningCount > 0) add("$warningCount uyarı seviyesinde arıza kaydı mevcut.")
            if (dpfSoot > 32.0) add("DPF kurum yükü yüksek (%.1f g) — uzun yol veya rejenerasyon önerilir.".format(dpfSoot))
            if (batteryVoltage < 12.2) add("Akü voltajı düşük (%.1f V) — kodlama öncesi şarj cihazı bağlayın.".format(batteryVoltage))
            if (isEmpty()) add("Araç genel sağlığı çok iyi — kritik mekanik veya elektriksel bulgu yok.")
        }

        return OfflineDiagnosticReport(
            overallScore = overall,
            subsystemScores = listOf(
                SubsystemHealthScore(
                    name = "Motor & Turbo",
                    scorePercent = engineScore,
                    note = if (engineScore >= 85) "Nominal" else "Arıza Kontrolü Gerekli",
                    key = SubsystemKey.ENGINE_TURBO,
                    isNominal = engineScore >= 85
                ),
                SubsystemHealthScore(
                    name = "DPF & Egzoz",
                    scorePercent = dpfScore,
                    note = if (dpfScore >= 80) "Temiz / Normal" else "Kurum / Basınç Yüksek",
                    key = SubsystemKey.DPF_EXHAUST,
                    isNominal = dpfScore >= 80
                ),
                SubsystemHealthScore(
                    name = "ZF8 Şanzıman",
                    scorePercent = transScore,
                    note = "Maks. kavrama sapması $maxClutchPressure mbar",
                    key = SubsystemKey.ZF8_TRANSMISSION,
                    isNominal = transScore >= 80,
                    paramValue = maxClutchPressure
                ),
                SubsystemHealthScore(
                    name = "Akü & Enerji (IBS)",
                    scorePercent = battScore,
                    note = "%.1f V • SOH %d%%".format(batteryVoltage, ibsHealth?.stateOfHealthPercent ?: 85),
                    key = SubsystemKey.BATTERY_IBS,
                    isNominal = battScore >= 75
                )
            ),
            recommendationItems = recItems,
            recommendations = recs
        )
    }
}
