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

data class RootCauseCorrelation(
    val id: String,
    val title: String,
    val involvedCodes: List<String>,
    val rootCauseSummary: String,
    val actionPlan: String
) {
    fun localizedTitle(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (id) {
        "rc_dpf_dsc_torque" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Root Cause: Engine Torque Limiter (DPF/EGR) Cascading to DSC/EGS"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əsas Səbəb: Mühərrik Tork Məhdudiyyəti (DPF/EGR) → DSC/EGS"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Первопричина: Ограничение момента ДВС (DPF/EGR) → сбой DSC/EGS"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "rc_boost_leak" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Root Cause: Charge Air / Boost Leak & Throttle Correlation"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əsas Səbəb: Şarj Havası / Boost Sızması Korrelyasiyası"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Первопричина: Утечка наддува / негерметичность впуска"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "rc_ibs_bsd" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Root Cause: BSD Bus / Battery Parasitic Drain Energy Cascade"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əsas Səbəb: BSD Xətti / Akkumulyator Parazit Boşalma Kaskadı"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Первопричина: Сбой шины BSD / ток утечки АКБ"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        "rc_fuel_misfire" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Root Cause: HPFP Fuel Pressure Instability Triggering Misfires"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əsas Səbəb: HPFP Yanacaq Təzyiqi Qeyri-sabitliyi (Misfire)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Первопричина: Нестабильность давления топлива (ТНВД → пропуски)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        else -> title
    }

    fun localizedSummary(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (id) {
        "rc_dpf_dsc_torque" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "DSC (D35A53) / drivetrain warnings are secondary effects of DME/DDE torque reduction from EGR/DPF congestion."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "DSC (D35A53) xəbərdarlıqları EGR/DPF tıxanmasına görə mühərrikin torku məhdudlaşdırmasının nəticəsidir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Ошибки DSC (D35A53) вторичны и вызваны ограничением крутящего момента DME/DDE из-за DPF/EGR."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> rootCauseSummary
        }
        "rc_boost_leak" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Underboost plus throttle plausibility indicates a pressurized charge pipe, intercooler hose, or diverter valve leak."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Aşağı boost və qaz qapağı korrelyasiyası borularda və ya interkullerdə hava sızmasına işarə edir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Недодув в сочетании с ошибкой дросселя указывает на негерметичность патрубков наддува или байпаса."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> rootCauseSummary
        }
        "rc_ibs_bsd" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "BSD/alternator faults combined with quiescent current drain can trigger false CAN/LIN timeouts across comfort ECUs."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "BSD generator xətası və sükunət cərəyanı sızması digər modullarda yalançı xətalara səbəb olur."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сбои шины BSD и утечка тока покоя вызывают каскадные ошибки связи в блоках комфорта."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> rootCauseSummary
        }
        "rc_fuel_misfire" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Fuel rail pressure deviation under load directly causes cylinder misfire lockouts rather than faulty ignition coils."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yanacaq rail təzyiqinin düşməsi alışma babinlərindən deyil, yanacaq çatışmazlığından misfire yaradır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Отклонение давления в топливной рампе вызывает пропуски зажигания без неисправности катушек."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> rootCauseSummary
        }
        else -> rootCauseSummary
    }

    fun localizedAction(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (id) {
        "rc_dpf_dsc_torque" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Do NOT replace DSC/wheel speed sensors yet. Service EGR/DPF & run forced regeneration, then clear all faults."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "DSC sensorlarını dəyişməyin. Əvvəl EGR/DPF xəttini təmizləyin və rejenerasiya edin."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Не меняйте датчики DSC. Обслужите контур EGR/DPF и выполните регенерацию, затем удалите ошибки."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> actionPlan
        }
        "rc_boost_leak" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Smoke-test charge pipe, intercooler seals, and vacuum solenoid lines before replacing MAF/MAP or turbo."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Turbo/sensor dəyişməzdən əvvəl şarj borularında tüstü testi ilə sızma yoxlayın."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверьте впуск дымогенератором (патрубки, интеркулер) до замены турбины или датчиков."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> actionPlan
        }
        "rc_ibs_bsd" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Check IBS negative terminal sensor, oil level sensor/alternator on BSD line, and test AGM battery capacity."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "IBS mənfi qütb sensorunu, BSD xəttini və AGM akkumulyator tutumunu yoxlayın."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверьте датчик IBS, линию BSD (генератор/уровень масла) и емкость АКБ."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> actionPlan
        }
        "rc_fuel_misfire" -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Log HPFP target vs actual fuel pressure under acceleration and inspect injectors/LPFP supply."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Hərəkətdə HPFP hədəf vs faktiki təzyiqi yoxlayın, farsunka və alçaq təzyiq nasosunu test edin."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Снимите лог целевого/фактического давления ТНВД под нагрузкой и проверьте форсунки."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> actionPlan
        }
        else -> actionPlan
    }
}

data class OfflineDiagnosticReport(
    val overallScore: Int,
    val subsystemScores: List<SubsystemHealthScore>,
    val recommendationItems: List<DiagnosticRecommendation> = emptyList(),
    val recommendations: List<String> = emptyList(),
    val rootCauseCorrelations: List<RootCauseCorrelation> = emptyList()
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

        val correlations = correlateRootCauses(dtcs, liveChannels, batteryVoltage)

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
            recommendations = recs,
            rootCauseCorrelations = correlations
        )
    }

    private fun correlateRootCauses(
        dtcs: List<DtcFault>,
        liveChannels: List<LiveSensorChannel>,
        batteryVoltage: Double
    ): List<RootCauseCorrelation> {
        val codes = dtcs.map { it.code.uppercase() }.toSet()
        val correlations = mutableListOf<RootCauseCorrelation>()

        val hasDpfOrEgr = codes.any { it == "290900" || it == "245700" || it == "244800" || it == "P2002" }
        val hasDscTorque = codes.any { it == "D35A53" || it == "420311" }
        if (hasDpfOrEgr && hasDscTorque) {
            val matched = codes.filter { it in setOf("290900", "245700", "244800", "P2002", "D35A53", "420311") }
            correlations += RootCauseCorrelation(
                id = "rc_dpf_dsc_torque",
                title = "Kök Neden: DPF/EGR Tork Kısıtlaması -> DSC/EGS Zincirleme Uyarısı",
                involvedCodes = matched,
                rootCauseSummary = "Asıl kaynak motor tork kısıtlaması ve EGR/DPF hattıdır; DSC/şanzıman uyarıları yan etkidir.",
                actionPlan = "DSC sensörü değiştirmeyin. Önce EGR/DPF hattını temizleyip DPF rejenerasyonu yapın, ardından tüm hataları silin."
            )
        }

        val hasUnderboost = codes.any { it == "120308" || it == "30FF" }
        val hasThrottleCorr = codes.any { it == "101F01" || it == "28A000" }
        if (hasUnderboost && hasThrottleCorr) {
            correlations += RootCauseCorrelation(
                id = "rc_boost_leak",
                title = "Kök Neden: Charge Pipe / Turbo Basınç Hattı Hava Kaçağı",
                involvedCodes = codes.filter { it in setOf("120308", "30FF", "101F01", "28A000") },
                rootCauseSummary = "Düşük boost + gaz kelebeği basınç tutarsızlığı mekanik hava kaçağına işaret eder.",
                actionPlan = "Turbo veya MAF değiştirmeden önce charge pipe, intercooler contaları ve vakum hatlarında duman testi yapın."
            )
        }

        val hasBsdOrDrain = codes.any { it == "278F00" || it == "8040B9" || it == "8041A8" }
        if ((codes.contains("278F00") && codes.contains("8040B9")) || (hasBsdOrDrain && batteryVoltage < 12.1)) {
            correlations += RootCauseCorrelation(
                id = "rc_ibs_bsd",
                title = "Kök Neden: BSD Veri Yolu / Akü Parazit Deşarj Enerji Kaskadı",
                involvedCodes = codes.filter { it in setOf("278F00", "8040B9", "8041A8") },
                rootCauseSummary = "BSD şarj hattı veya düşük akü voltajı konfor modüllerinde (FEM/CAS/FRM) zincirleme hata tetikliyor.",
                actionPlan = "IBS eksi kutup sensörü, BSD hattı (alternatör/yağ seviye sensörü) ve AGM akü sağlığını test edin."
            )
        }

        val hasRailOrFuel = codes.any { it == "255000" } ||
            ((liveChannels.firstOrNull { it.id == "hpfp_target" }?.currentValue ?: 0.0) -
                (liveChannels.firstOrNull { it.id == "hpfp_actual" }?.currentValue ?: 0.0) > 25.0)
        val hasMisfire = codes.any { it == "140010" } ||
            ((liveChannels.firstOrNull { it.id == "misfire_total" }?.currentValue ?: 0.0) > 2.0)
        if (hasRailOrFuel && hasMisfire) {
            correlations += RootCauseCorrelation(
                id = "rc_fuel_misfire",
                title = "Kök Neden: HPFP Yakıt Basıncı Düşüşü -> Ateşleme Kesintisi (Misfire)",
                involvedCodes = codes.filter { it in setOf("255000", "140010") },
                rootCauseSummary = "Yüksek basınç yakıt pompası/enjektör dalgalanması buji-bobin arızası olmadan misfire üretiyor.",
                actionPlan = "Bobin değiştirmeden önce tam gazda HPFP Hedef vs Gerçek basınç ve enjektör düzeltmelerini loglayın."
            )
        }

        return correlations
    }
}
