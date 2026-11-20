package com.punhanaghayev.bimmerpro.core.bmw

data class SubsystemHealthScore(
    val name: String,
    val scorePercent: Int,
    val note: String
)

data class OfflineDiagnosticReport(
    val overallScore: Int,
    val subsystemScores: List<SubsystemHealthScore>,
    val recommendations: List<String>
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
                SubsystemHealthScore("Motor & Turbo", engineScore, if (engineScore >= 85) "Nominal" else "Arıza Kontrolü Gerekli"),
                SubsystemHealthScore("DPF & Egzoz", dpfScore, if (dpfScore >= 80) "Temiz / Normal" else "Kurum / Basınç Yüksek"),
                SubsystemHealthScore("ZF8 Şanzıman", transScore, "Maks. kavrama sapması $maxClutchPressure mbar"),
                SubsystemHealthScore("Akü & Enerji (IBS)", battScore, "%.1f V • SOH %d%%".format(batteryVoltage, ibsHealth?.stateOfHealthPercent ?: 85))
            ),
            recommendations = recs
        )
    }
}
