package com.punhanaghayev.bimmerpro.core.bmw

enum class SeriesFamily(val label: String, val protocolStack: String) {
    E_SERIES("E-Serisi", "K+DCAN / KWP2000 / EDIABAS"),
    F_SERIES("F-Serisi", "ENET HSFZ / UDS ISO 14229"),
    G_SERIES("G-Serisi", "DoIP / HSFZ + NCD 2.0"),
    I_SERIES("I-Serisi", "ENET HSFZ / UDS ISO 14229")
}

enum class BmwChassis(
    val code: String,
    val modelRange: String,
    val family: SeriesFamily,
    val secureCodingNotice: String? = null
) {
    E60("E60", "5 Serisi (2003-2010)", SeriesFamily.E_SERIES),
    E90("E90", "3 Serisi (2005-2012)", SeriesFamily.E_SERIES),
    F10("F10", "5 Serisi (2010-2017)", SeriesFamily.F_SERIES),
    F30("F30", "3 Serisi (2012-2019)", SeriesFamily.F_SERIES),
    G30(
        "G30",
        "5 Serisi (2017-2023)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "LCI / iDrive 7+ bazı modüller Secure Coding 2.0 imzası gerektirir."
    ),
    G20(
        "G20",
        "3 Serisi (2019-Günümüz)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "MGU & BDC3 modüllerinde Secure Coding 2.0 kısıtlamaları kontrol edilir."
    ),
    F20("F20", "1 Serisi (2011-2019)", SeriesFamily.F_SERIES),
    F22("F22", "2 Serisi (2013-2021)", SeriesFamily.F_SERIES),
    F32("F32", "4 Serisi (2013-2020)", SeriesFamily.F_SERIES),
    F15("F15", "X5 (2013-2018)", SeriesFamily.F_SERIES),
    F25("F25", "X3 (2010-2017)", SeriesFamily.F_SERIES),
    F48("F48", "X1 (2015-2022)", SeriesFamily.F_SERIES),
    G01("G01", "X3 (2017-2024)", SeriesFamily.G_SERIES),
    G05("G05", "X5 (2018-2023)", SeriesFamily.G_SERIES),
    G11("G11", "7 Serisi (2015-2022)", SeriesFamily.G_SERIES),
    G60(
        "G60",
        "5 Serisi (2023+)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "iDrive 8.5 / OS 9 ve yeni BDC modüllerinde Secure Coding 2.0 koruması devrededir."
    ),
    G70(
        "G70",
        "7 Serisi (2022+)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "iDrive 8/8.5 Theatre Screen ve BDC3 modülü imza gerektirir."
    ),
    I01("I01", "i3 (2013-2022)", SeriesFamily.I_SERIES),
    I12("I12", "i8 (2014-2020)", SeriesFamily.I_SERIES),
    I20(
        "I20",
        "iX (2021+)",
        SeriesFamily.I_SERIES,
        secureCodingNotice = "BDC3 ve MGU ünitelerinde Secure Coding 2.0 kontrol edilir."
    ),
    U06(
        "U06",
        "2 Serisi Active Tourer (2022+)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "OS 8/9 ve BCP (Basic Central Platform) Secure Coding 2.0 imzası gerektirir."
    ),
    U11(
        "U11",
        "X1 / iX1 (2022+)",
        SeriesFamily.G_SERIES,
        secureCodingNotice = "iDrive 9 (OS 9) ve BCP modüllerinde Secure Coding 2.0 koruması devrededir."
    ),
    E70("E70", "X5 (2006-2013)", SeriesFamily.E_SERIES),
    E87("E87", "1 Serisi (2004-2013)", SeriesFamily.E_SERIES)
}

object EcuAddress {
    const val ACSM = 0x01
    const val ZGW_BDC = 0x10
    const val DME_DDE = 0x12
    const val EGS = 0x18
    const val VTG_TRANSFER = 0x19
    const val ACC_RADAR = 0x59
    const val DSC = 0x29
    const val ASD = 0x3C
    const val FEM_CAS = 0x40
    const val KAFAS_CAMERA = 0x5D
    const val KOMBI = 0x60
    const val HU_MEDIA = 0x63
    const val SME_HV_BATTERY = 0x6E
    const val FRM = 0x72
    const val REM = 0x73
    const val EHC_SUSPENSION = 0x76
    const val IHKA = 0x78
}

enum class CodingCapability(val label: String) {
    FULL_FDL_AND_VO("Tam FDL + VO Kodlama"),
    FDL_ONLY("FDL Kodlama"),
    SECURE_CODING_LOCKED("Secure Coding 2.0 Korumalı")
}

data class EcuModule(
    val address: Int,
    val shortName: String,
    val fullName: String,
    val hardwareVersion: String,
    val softwareVersion: String,
    val cafdId: String?,
    val codingCapability: CodingCapability,
    val activeDtcCount: Int,
    val shadowDtcCount: Int,
    val reportedMileageKm: Int? = null,
    val variantName: String? = null,
    val svkSummary: String? = null
) {
    val addressHex: String get() = "0x%02X".format(address)
}

data class VehicleOrderOption(
    val code: String,
    val description: String
)

enum class TamperStatus(val label: String) {
    VERIFIED("Doğrulandı (Modüller Tutarlı)"),
    MINOR_DRIFT("Normal Sapma (< 150 km)"),
    SUSPICIOUS_MISMATCH("KM Uyumsuzluğu Tespit Edildi!");

    fun localized(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (this) {
        VERIFIED -> strings.tamperVerified
        MINOR_DRIFT -> strings.tamperMinorDrift
        SUSPICIOUS_MISMATCH -> strings.tamperSuspicious
    }
}

data class MileageModuleReading(
    val moduleName: String,
    val mileageKm: Int,
    val engineHours: Int? = null
) {
    fun localizedModuleName(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when {
        moduleName.contains("KOMBI", ignoreCase = true) -> strings.moduleKombi
        moduleName.contains("DME", ignoreCase = true) || moduleName.contains("Benzin", ignoreCase = true) -> strings.moduleDmePetrol
        moduleName.contains("DDE", ignoreCase = true) || moduleName.contains("Dizel", ignoreCase = true) -> strings.moduleDdeDiesel
        moduleName.contains("EGS", ignoreCase = true) || moduleName.contains("ZF", ignoreCase = true) -> strings.moduleZf8
        else -> moduleName
    }
}

data class MileageVerificationResult(
    val readings: List<MileageModuleReading>,
    val maxDifferenceKm: Int,
    val status: TamperStatus,
    val summary: String
) {
    fun localizedSummary(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (status) {
        TamperStatus.VERIFIED -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "All ${readings.size} primary control units consistent (±$maxDifferenceKm km)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Bütün ${readings.size} əsas idarəetmə bloku uyğundur (±$maxDifferenceKm km)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Все ${readings.size} блока управления согласованы (±$maxDifferenceKm км)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> summary
        }
        else -> summary
    }
}

data class VehicleProfile(
    val vin: String,
    val chassis: BmwChassis,
    val modelDisplayName: String,
    val engineCode: String,
    val productionYear: Int,
    val iLevel: String,
    val factoryILevel: String = iLevel,
    val typeCode: String = "5C31",
    val productionDate: String = "01.03.$productionYear",
    val paintCode: String = "475 Black Sapphire Metallic",
    val upholsteryCode: String = "LCSW Dakota Leather Black",
    val batteryVoltage: Double,
    val odometerKm: Int,
    val faOptions: List<VehicleOrderOption>,
    val mileageVerification: MileageVerificationResult
)

enum class DtcSeverity(val label: String) {
    CRITICAL("Kritik"),
    WARNING("Uyarı"),
    INFO("Bilgi"),
    SHADOW("Gölge (Shadow)")
}

enum class DtcStatus(val label: String) {
    ACTIVE("Aktif"),
    STORED("Kayıtlı"),
    INTERMITTENT("Aralıklı"),
    PENDING("Beklemede"),
    SHADOW("Gölge / Bilgi");

    fun localized(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (this) {
        ACTIVE -> strings.dtcStatusActive
        STORED -> strings.dtcStatusStored
        INTERMITTENT -> strings.dtcStatusIntermittent
        PENDING -> strings.dtcStatusPending
        SHADOW -> strings.dtcStatusShadow
    }
}

enum class DtcMemoryType(val label: String) {
    ERROR_MEMORY("Hata Hafızası (Error)"),
    INFO_MEMORY("Bilgi Hafızası (Info)"),
    PERMANENT_MEMORY("Kalıcı Hafıza (Permanent)");

    fun localizedLabel(): String = label
}

data class FreezeFrameData(
    val mileageKm: Int,
    val batteryVoltage: Double,
    val coolantTempC: Int,
    val engineRpm: Int,
    val occurrenceCount: Int
)

data class DtcFault(
    val id: String,
    val code: String,
    val ecuAddress: Int,
    val ecuName: String,
    val title: String,
    val technicalDetail: String,
    val severity: DtcSeverity,
    val status: DtcStatus,
    val freezeFrame: FreezeFrameData,
    val istaGuidance: String,
    val memoryType: DtcMemoryType = DtcMemoryType.ERROR_MEMORY
) {
    fun localizedTitle(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> when (code) {
            "120308" -> "Charging Pressure Control: Pressure Too Low (Boost Underboost)"
            "101F01" -> "Throttle Valve Angle - Intake Manifold Absolute Pressure Plausibility"
            "B7F805" -> "USB Connection Port 1: Communication Line Interruption"
            "245700" -> "Particulate Filter System: Soot Mass Exceeded"
            "244800" -> "Particulate Filter System: Maximum Service Life Reached"
            "290900" -> "Air Mass System: Measured Air Mass Flow Too High (EGR)"
            "255000" -> "Fuel Rail Pressure Control: Deviation Too High"
            "26CF00" -> "Glow Plug Cylinder 1-4 Activation Error"
            "8040B9" -> "De-energize / Parasitic Sleep Current Violation"
            "8041A8" -> "Interior / Welcome Light Output Short Circuit Protection"
            "CD9304" -> "LIN Bus: Active Air Flap Communication Interruption"
            "E1C447" -> "MOST Ring Temporary Synchronization Loss"
            "D35A53" -> "DSC: Dynamic Stability Missing Engine Torque Signal"
            "480A11" -> "Brake Pad Wear Sensor (Front Axle) Reached"
            "420311" -> "EGS ZF8 Transmission: Oil Degradation / Adaptation Warning"
            "30FF" -> "Exhaust Turbocharger: Low Boost Pressure Underboost"
            "B7F8C3" -> "RAM / TCB Emergency Call Antenna High Impedance"
            "278F00" -> "Alternator Communication / BSD Bus Error"
            "28A000" -> "Throttle Valve Angle - Intake Pressure Correlation"
            "140010" -> "Combustion Misfire Detected on Multiple Cylinders"
            "P2002" -> "Diesel Particulate Filter Efficiency Below Threshold"
            else -> title
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> when (code) {
            "120308" -> "Turbo Təzyiq Nəzarəti: Təzyiq Çox Aşağıdır (Boost Low)"
            "101F01" -> "Qaz Qapağı Bucağı — Kollektor Təzyiqi Uyğunsuzluğu"
            "B7F805" -> "USB Bağlantı Portu 1 Rabitə Xətası"
            "245700" -> "DPF Partikül Filtri: Qurum Həddi Aşıldı"
            "244800" -> "DPF Partikül Filtri: Maksimal Resurs Bitdi"
            "290900" -> "Hava Kütləsi Sistemi: Ölçülən Hava Kütləsi Çox Yüksəkdir (EGR)"
            "255000" -> "Yanacaq Rail Təzyiq Nəzarəti: Meyletmə Çox Yüksəkdir"
            "26CF00" -> "Qızdırma Şamı Silindr 1-4 İdarəetmə Xətası"
            "8040B9" -> "Akkumulyator Parazit Boşalma / Yuxu Cərəyanı Xətası"
            "8041A8" -> "İç Aydınlatma Çıxışı Qısa Qapanma Qoruması"
            "CD9304" -> "LIN Bus: Aktiv Radiator Jalüzü Rabitə Xətası"
            "E1C447" -> "MOST Ring Müvəqqəti Sinxronizasiya İtkisi"
            "D35A53" -> "DSC: Çatışmayan Mühərrik Fırlanma Anı Siqnalı"
            "480A11" -> "Əyləc Bəndi Aşınma Sensoru (Ön Ox) Həddədir"
            "420311" -> "EGS ZF8: Yağ Resursu / Adaptasiya Xəbərdarlığı"
            "30FF" -> "Turbo Təzyiqi Hədəfdən Aşağıdır (Underboost)"
            "B7F8C3" -> "RAM / TCB Təcili Zəng Antenasında Yüksək Müqavimət"
            "278F00" -> "Generator / BSD Veri Yolu Xətası"
            "28A000" -> "Qaz Qapağı Bucağı - Qəbul Təzyiqi Korrelyasiyası"
            "140010" -> "Mühərrikdə Alışma Kəsilməsi (Misfire)"
            "P2002" -> "DPF Dizel Partikül Filtri Effektivliyi Aşağıdır"
            else -> title
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> when (code) {
            "120308" -> "Регулировка давления наддува: давление слишком низкое"
            "101F01" -> "Угол дроссельной заслонки — недостоверность давления во впускном коллекторе"
            "B7F805" -> "USB-разъем 1: ошибка связи"
            "245700" -> "Сажевый фильтр DPF: предельная масса сажи"
            "244800" -> "Сажевый фильтр DPF: исчерпан ресурс фильтра"
            "290900" -> "Система подачи воздуха: слишком большой расход воздуха (EGR)"
            "255000" -> "Регулировка давления в Rail: слишком большое отклонение"
            "26CF00" -> "Свеча накаливания цилиндров 1-4: ошибка активации"
            "8040B9" -> "Ток покоя: обнаружен паразитный разряд АКБ"
            "8041A8" -> "Защита от короткого замыкания цепи освещения салона"
            "CD9304" -> "Шина LIN: задержка связи с активными жалюзи радиатора"
            "E1C447" -> "Шина MOST: кратковременная потеря синхронизации"
            "D35A53" -> "DSC: отсутствует сигнал крутящего момента двигателя"
            "480A11" -> "Датчик износа тормозных колодок (передняя ось)"
            "420311" -> "АКПП ZF8: предупреждение о старении масла / адаптации"
            "30FF" -> "Турбонагнетатель: давление наддува ниже нормы (Underboost)"
            "B7F8C3" -> "Антенна экстренного вызова RAM / TCB: высокое сопротивление"
            "278F00" -> "Связь с генератором / ошибка шины BSD"
            "28A000" -> "Корреляция угла дросселя и давления во впускном коллекторе"
            "140010" -> "Пропуски зажигания в нескольких цилиндрах"
            "P2002" -> "Эффективность сажевого фильтра DPF ниже порога"
            else -> title
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
    }
}

data class LiveSensorChannel(
    val id: String,
    val name: String,
    val unit: String,
    val currentValue: Double,
    val minExpected: Double,
    val maxExpected: Double,
    val history: List<Float>
) {
    fun localizedName(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (strings.language) {
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> when (id) {
            "rpm" -> "Engine Speed"
            "speed" -> "Vehicle Speed"
            "boost", "boost_press" -> "Turbo Boost Pressure"
            "coolant" -> "Coolant Temperature"
            "oil_temp" -> "Engine Oil Temperature"
            "iat" -> "Intake Air Temperature (IAT)"
            "rail" -> "Fuel Rail Pressure"
            "load" -> "Engine Load"
            "throttle" -> "Throttle Position"
            "batt" -> "Battery / Alternator Voltage"
            "trans_temp" -> "ZF8 Transmission Fluid Temp"
            "dpf_diff" -> "DPF Differential Pressure"
            "dpf_soot" -> "DPF Soot Mass"
            "dpf_regen_km" -> "Distance Since Last Regen"
            "inj_1" -> "Injector 1 Deviation"
            "inj_2" -> "Injector 2 Deviation"
            "inj_3" -> "Injector 3 Deviation"
            "inj_4" -> "Injector 4 Deviation"
            "turbo_target" -> "Target Boost Pressure"
            "alt_load" -> "Alternator Load"
            "timing_corr" -> "Ignition Timing Correction (Cyl 1)"
            "timing_corr_2" -> "Ignition Timing Correction (Cyl 2)"
            "timing_corr_3" -> "Ignition Timing Correction (Cyl 3)"
            "timing_corr_4" -> "Ignition Timing Correction (Cyl 4)"
            "timing_corr_5" -> "Ignition Timing Correction (Cyl 5)"
            "timing_corr_6" -> "Ignition Timing Correction (Cyl 6)"
            "ltft" -> "Long Term Fuel Trim (LTFT)"
            "stft" -> "Short Term Fuel Trim (STFT)"
            "lambda_afr" -> "Air-Fuel Ratio (Lambda)"
            "vanos_in_target" -> "VANOS Intake Target Angle"
            "vanos_in" -> "VANOS Intake Actual Angle"
            "hpfp_target" -> "HPFP Target Pressure"
            "hpfp_actual" -> "HPFP Actual Pressure"
            "misfire_total" -> "Total Misfire Counter"
            else -> name
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> when (id) {
            "rpm" -> "Mühərrik Dövriyyəsi"
            "speed" -> "Avtomobil Sürəti"
            "boost", "boost_press" -> "Turbo Boost Təzyiqi"
            "coolant" -> "Soyutma Mayesi Temperaturu"
            "oil_temp" -> "Mühərrik Yağı Temperaturu"
            "iat" -> "Qəbul Havası (IAT)"
            "rail" -> "Yanacaq Rail Təzyiqi"
            "load" -> "Mühərrik Yükü"
            "throttle" -> "Qaz Qapağı Vəziyyəti"
            "batt" -> "Akkumulyator Voltajı"
            "trans_temp" -> "ZF8 Sürətlər Qutusu Yağı"
            "dpf_diff" -> "DPF Diferensial Təzyiqi"
            "dpf_soot" -> "DPF Qurum Kütləsi"
            "dpf_regen_km" -> "Son Regenerasiyadan Bəri"
            "inj_1" -> "Farsunka 1 Meyletmə"
            "inj_2" -> "Farsunka 2 Meyletmə"
            "inj_3" -> "Farsunka 3 Meyletmə"
            "inj_4" -> "Farsunka 4 Meyletmə"
            "turbo_target" -> "Hədəf Boost Təzyiqi"
            "alt_load" -> "Generator Yükü"
            "timing_corr" -> "Alışma Avansı Düzəlişi (Sil 1)"
            "timing_corr_2" -> "Alışma Avansı Düzəlişi (Sil 2)"
            "timing_corr_3" -> "Alışma Avansı Düzəlişi (Sil 3)"
            "timing_corr_4" -> "Alışma Avansı Düzəlişi (Sil 4)"
            "timing_corr_5" -> "Alışma Avansı Düzəlişi (Sil 5)"
            "timing_corr_6" -> "Alışma Avansı Düzəlişi (Sil 6)"
            "ltft" -> "Uzun Müddətli Yanacaq Trimi (LTFT)"
            "stft" -> "Qısa Müddətli Yanacaq Trimi (STFT)"
            "lambda_afr" -> "Hava-Yanacaq Nisbəti (Lambda)"
            "vanos_in_target" -> "VANOS Giriş Hədəf Bucağı"
            "vanos_in" -> "VANOS Giriş Faktiki Bucağı"
            "hpfp_target" -> "HPFP Hədəf Təzyiq"
            "hpfp_actual" -> "HPFP Faktiki Təzyiq"
            "misfire_total" -> "Ümumi Misfire Sayğacı"
            else -> name
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> when (id) {
            "rpm" -> "Обороты двигателя"
            "speed" -> "Скорость автомобиля"
            "boost", "boost_press" -> "Давление наддува"
            "coolant" -> "Температура ОЖ"
            "oil_temp" -> "Температура моторного масла"
            "iat" -> "Температура воздуха на впуске"
            "rail" -> "Давление в топливной рампе"
            "load" -> "Нагрузка на двигатель"
            "throttle" -> "Положение дроссельной заслонки"
            "batt" -> "Напряжение АКБ / генератора"
            "trans_temp" -> "Температура масла ZF8"
            "dpf_diff" -> "Перепад давления DPF"
            "dpf_soot" -> "Масса сажи в DPF"
            "dpf_regen_km" -> "Пробег с посл. регенерации"
            "inj_1" -> "Коррекция форсунки 1"
            "inj_2" -> "Коррекция форсунки 2"
            "inj_3" -> "Коррекция форсунки 3"
            "inj_4" -> "Коррекция форсунки 4"
            "turbo_target" -> "Целевое давление наддува"
            "alt_load" -> "Нагрузка генератора"
            "timing_corr" -> "Коррекция УОЗ (Цил. 1)"
            "timing_corr_2" -> "Коррекция УОЗ (Цил. 2)"
            "timing_corr_3" -> "Коррекция УОЗ (Цил. 3)"
            "timing_corr_4" -> "Коррекция УОЗ (Цил. 4)"
            "timing_corr_5" -> "Коррекция УОЗ (Цил. 5)"
            "timing_corr_6" -> "Коррекция УОЗ (Цил. 6)"
            "ltft" -> "Долгосрочная коррекция топлива (LTFT)"
            "stft" -> "Краткосрочная коррекция топлива (STFT)"
            "lambda_afr" -> "Состав смеси (Lambda)"
            "vanos_in_target" -> "Целевой угол VANOS впуск"
            "vanos_in" -> "Фактический угол VANOS впуск"
            "hpfp_target" -> "Целевое давление ТНВД (HPFP)"
            "hpfp_actual" -> "Фактическое давление ТНВД (HPFP)"
            "misfire_total" -> "Счетчик пропусков зажигания"
            else -> name
        }
        com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> name
    }
}

enum class ServiceRoutineType {
    BATTERY_REGISTRATION,
    DPF_REGENERATION,
    EPB_SERVICE_MODE,
    CBS_OIL_RESET,
    EXHAUST_VALVE_OPEN,
    STEERING_ANGLE_CALIBRATION,
    KAFAS_CAMERA_CALIBRATION,
    ACC_RADAR_CALIBRATION,
    HV_BATTERY_CONTACTOR_CHECK,
    SHORT_CIRCUIT_RESET,
    COOLANT_BLEEDING,
    DSC_ABS_BLEED,
    VTG_TRANSFER_CASE_RESET,
    VALVETRONIC_LIMIT_LEARN,
    EHC_AIR_SUSPENSION_CAL
}

data class ServiceRoutine(
    val type: ServiceRoutineType,
    val title: String,
    val targetEcu: String,
    val targetAddress: Int,
    val routineId: Int,
    val description: String,
    val warningNote: String
) {
    fun localizedTitle(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (type) {
        ServiceRoutineType.BATTERY_REGISTRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Battery Registration & Capacity (IBS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Akkumulyator Qeydiyyatı & Tutum (IBS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Регистрация АКБ и профиля (IBS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.DPF_REGENERATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Trigger Forced DPF Regeneration"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Məcburi DPF Rejenerasiyasını Başlat"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запуск принудительной регенерации DPF"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.EPB_SERVICE_MODE -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "EPB Parking Brake Service Mode"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Elektron Əyləc (EPB) Servis Rejimi"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сервисный режим стояночного тормоза (EPB)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.CBS_OIL_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "CBS Maintenance Reset (Oil / Brake)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "CBS Servis Sıfırlama (Yağ / Əyləc)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс ТО CBS (Масло / Тормоза)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.EXHAUST_VALVE_OPEN -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Exhaust Flap Actuation (Open / OEM)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Qaz Qapağının İdarəsi (Açıq / Zavod)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Управление заслонкой выхлопа (Открыть / Завод)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.STEERING_ANGLE_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Steering Angle Sensor Calibration (LWS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Sükan Bucağı Sensorunun Kalibrasiyası"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Калибровка датчика угла руля (LWS)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.KAFAS_CAMERA_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "KAFAS Windshield Camera Dynamic Calibration"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "KAFAS Ön Şüşə Kamera Dinamik Kalibrasiyası"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Динамическая калибровка камеры KAFAS на лобовом стекле"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.ACC_RADAR_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "ACC / FRR Radar Sensor Offset Reset & Calibration"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "ACC / FRR Radar Bucaq Meyletməsi Sıfırlama & Kalibrasiya"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс углового смещения и калибровка радара ACC / FRR"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.HV_BATTERY_CONTACTOR_CHECK -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "HV Battery Contactor & Insulation Diagnostic Routine"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "HV Batareya Kontaktor & İzolyasiya Diaqnostika Rutini"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Диагностическая процедура контакторов и изоляции батареи HV"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.SHORT_CIRCUIT_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "FRM / FEM Short-Circuit Counter Reset"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "FRM / FEM Qısa Qapanma Kilidinin Sıfırlanması"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс блокировки короткого замыкания FRM / FEM"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.COOLANT_BLEEDING -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Electric Water Pump Coolant Bleeding (12 Min)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Elektrikli Su Nasosu Hava Alma (12 Dəq)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Прокачка электропомпы охлаждения (12 мин)"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.DSC_ABS_BLEED -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "DSC / ABS Hydraulic Brake Bleed Routine"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "DSC / ABS Əyləc Hidravlik Hava Alma Rutini"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Прокачка тормозной системы DSC / ABS"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.VTG_TRANSFER_CASE_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "xDrive VTG Transfer Case Oil Reset & Servo Cal"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "xDrive VTG Razdatka Yağ Sıfırlama & Kalibrasiya"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс износа масла и калибровка VTG xDrive"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.VALVETRONIC_LIMIT_LEARN -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Valvetronic & Throttle Stop-Limit Learning"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Valvetronic & Qaz Qapağı Limit Öyrənmə"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Обучение упоров Valvetronic и дросселя"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
        ServiceRoutineType.EHC_AIR_SUSPENSION_CAL -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "EHC Air Suspension Ride Height Calibration"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "EHC Pnevmatik Asqı Hündürlük Kalibrasiyası"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Калибровка высоты пневмоподвески EHC"
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> title
        }
    }

    fun localizedDescription(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (type) {
        ServiceRoutineType.BATTERY_REGISTRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Resets alternator charging curve after battery replacement (e.g. 90Ah AGM)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yeni akkumulyatordan sonra şarj profilini sıfırlayır (Məs. 90Ah AGM)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс профиля зарядки после замены АКБ (напр. 90Ah AGM)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.DPF_REGENERATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Requests soot burn-off regeneration cycle in DPF filter during driving."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Sürüş zamanı DPF hissi filtrində qurumun yandırılmasını aktivləşdirir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запрос цикла прожига сажевого фильтра во время движения."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.EPB_SERVICE_MODE -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Retracts rear caliper EMF actuator pistons for brake pad replacement."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Arxa əyləc bəndlərini dəyişmək üçün kaliper porşenini geri çəkir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Разведение суппортов для замены задних тормозных колодок."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.CBS_OIL_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Resets iDrive service counter and writes log to digital service history."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "iDrive servis sayğacını sıfırlayır və rəqəmsal tarixçəyə qeyd edir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс сервисного интервала iDrive и запись в историю обслуживания."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.KAFAS_CAMERA_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Initiates dynamic camera learning following windshield replacement or LDW/SLI errors."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Ön şüşə dəyişimi və ya zolaq nəzarət xətası sonrası kameranın dinamik öyrənməsini başladır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запуск динамического обучения камеры после замены стекла или сбоя LDW/SLI."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.ACC_RADAR_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Resets radar mounting offset angles and activates alignment drive cycle."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Radar montaj meyletmə bucaqlarını sıfırlayır və kalibrasiya sürüşünü aktivləşdirir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс угловых смещений монтажа радара и запуск юстировочной поездки."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.HV_BATTERY_CONTACTOR_CHECK -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Tests main HV contactors and insulation integrity (>500 kOhm) for EV/PHEV safety."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "EV/PHEV təhlükəsizliyi üçün əsas HV kontaktorlarını və izolyasiya müqavimətini yoxlayır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Проверка главных высоковольтных контакторов и сопротивления изоляции (>500 кОм)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.SHORT_CIRCUIT_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Clears FRM/FEM/BDC short-circuit output lock counters after lamp/wiring repair."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Lampa və ya naqil təmirindən sonra FRM/FEM/BDC qısa qapanma blokunu sıfırlayır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс счетчиков блокировки выхода FRM/FEM/BDC после устранения замыкания."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.COOLANT_BLEEDING -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Activates electric coolant pump cycling (12 min) after thermostat, radiator, or pump service."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Termostat, radiator və ya nasos təmirindən sonra elektrikli su nasosunu 12 dəq dövriyyəyə salır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Запускает 12-минутный цикл прокачки электропомпы после обслуживания системы охлаждения."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.DSC_ABS_BLEED -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Cycles DSC hydraulic valves & ABS pump per wheel circuit for brake fluid bleeding."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əyləc mayesinin havasını almaq üçün DSC klapanlarını və ABS nasosunu işə salır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Активация клапанов DSC и насоса ABS для прокачки тормозных контуров."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.VTG_TRANSFER_CASE_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Resets VTG transfer case oil wear counter and runs actuator angle calibration."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "VTG razdatka yağ aşınma sayğacını sıfırlayır və servo bucaq kalibrasiyasını icra edir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сброс счетчика старения масла раздатки VTG и калибровка сервопривода."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.VALVETRONIC_LIMIT_LEARN -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Re-learns Valvetronic eccentric shaft minimum/maximum stops and throttle adaptation."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Valvetronic eksentrik valının min/maks limitlərini və qaz qapağını yenidən öyrədir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Адаптация крайних положений вала Valvetronic и дроссельной заслонки."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        ServiceRoutineType.EHC_AIR_SUSPENSION_CAL -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Calibrates left/right rear air spring ride height offsets via EHC level sensors."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "EHC səviyyə sensorları ilə sağ/sol pnevmatik asqı hündürlük dəyərlərini kalibrləyir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Калибровка датчиков уровня пневмоподвески EHC (левый/правый контур)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> description
        }
        else -> description
    }

    fun localizedWarning(strings: com.punhanaghayev.bimmerpro.core.i18n.AppStrings): String = when (type) {
        ServiceRoutineType.BATTERY_REGISTRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Selecting incorrect battery capacity or chemistry may cause premature battery failure."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Səhv akkumulyator növü/tutumu seçilərsə akkumulyator tez sıradan çıxa bilər."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Неверный выбор типа или емкости АКБ может привести к преждевременному износу."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.DPF_REGENERATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Fuel level must be > 1/4, coolant temp > 75°C, and no critical engine errors present."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yanacaq > 1/4 olmalı, soyutma mayesi > 75°C və kritik mühərrik xətası olmamalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Уровень топлива > 1/4, темп. охлаждающей жидкости > 75°C, отсутствие критических ошибок."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.EPB_SERVICE_MODE -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Vehicle must be parked on level ground with wheel chocks applied."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Avtomobil düz yerdə və təkər dayaqları qoyulmuş olmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Автомобиль должен стоять на ровной поверхности с противооткатными упорами."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.CBS_OIL_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Reset only after physical maintenance service has been performed."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yalnız xidmət işi faktiki başa çatdıqdan sonra sıfırlayın."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сбрасывайте только после фактического выполнения техобслуживания."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.EXHAUST_VALVE_OPEN -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Ignition must be ON (PAD mode) or engine idling."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Açar açıq (PAD rejimi) və ya mühərrik rölantidə olmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Зажигание должно быть включено (режим PAD) или двигатель на холостом ходу."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.STEERING_ANGLE_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Steering wheel must be kept in exact straight-ahead center position."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Sükan tam düz vəziyyətdə saxlanılmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Рулевое колесо должно удерживаться строго в центральном положении."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.KAFAS_CAMERA_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Requires a 10-15 min drive on well-marked road at 40-70 km/h."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Düz və aydın zolaqlı yolda 40-70 km/saat sürətlə 10-15 dəqiqə sürüş tələb edir."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Требуется поездка 10-15 минут по дороге с четкой разметкой со скоростью 40-70 км/ч."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.ACC_RADAR_CALIBRATION -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Radar dome must be clean and free of physical collision damage."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Radar səthi təmiz olmalı və mexaniki zədələnmə olmamalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Поверхность радара должна быть чистой и без механических повреждений."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.HV_BATTERY_CONTACTOR_CHECK -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Charging cable must be disconnected, transmission in Park (P)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Şarj kabeli ayrılmalı, ötürmə qutusu Park (P) rejimində olmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Зарядный кабель должен быть отключен, селектор КПП в положении Park (P)."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.SHORT_CIRCUIT_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Ensure bulb/wiring short circuit is physically repaired before resetting counter."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Sıfırlamadan əvvəl lampa/naqil qısa qapanmasının aradan qaldırıldığından əmin olun."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Убедитесь, что короткое замыкание устранено физически перед сбросом."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.COOLANT_BLEEDING -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Connect a battery charger (>12.5V), ignition ON, engine OFF, heater set to max temperature."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Şarj cihazı qoşun (>12.5V), açar ON, mühərrik SÖNÜLÜ, salon isitməsini maksimuma qoyun."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Подключите ЗУ (>12.5V), зажигание ВКЛ, двигатель ВЫКЛ, печка на максимум."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.DSC_ABS_BLEED -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Use a brake bleeder pressurizing reservoir and keep fluid level above MIN."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Əyləc maye qabına təzyiq verən cihaz qoşun və səviyyəni MIN üzərində saxlayın."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Используйте установку под давлением и следите, чтобы уровень жидкости не упал ниже MIN."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.VTG_TRANSFER_CASE_RESET -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Perform oil reset only after filling fresh BMW DTF-1 transfer case oil."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Yalnız yeni BMW DTF-1 razdatka yağı doldurduqdan sonra sıfırlama edin."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Сбрасывайте только после заливки свежего масла BMW DTF-1."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.VALVETRONIC_LIMIT_LEARN -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Ignition ON, engine OFF; battery voltage must remain steady above 12.2V."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Açar ON, mühərrik SÖNÜLÜ; akkumulyator gərginliyi 12.2V üzərində sabit olmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Зажигание ВКЛ, двигатель ВЫКЛ; напряжение АКБ стабильно выше 12.2V."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
        ServiceRoutineType.EHC_AIR_SUSPENSION_CAL -> when (strings.language) {
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.EN -> "Vehicle must be unloaded on a completely flat surface; doors closed."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.AZ -> "Avtomobil tam düz səthdə yüksüz olmalı və qapılar bağlı qalmalıdır."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.RU -> "Автомобиль без нагрузки на ровной площадке, все двери закрыты."
            com.punhanaghayev.bimmerpro.core.i18n.AppLanguage.TR -> warningNote
        }
    }
}

data class ServiceExecutionResult(
    val success: Boolean,
    val title: String,
    val message: String,
    val timestamp: String
)

data class ServiceHistoryRecord(
    val id: String,
    val date: String,
    val mileageKm: Int,
    val dealerCode: String,
    val performedItems: List<String>
)

data class ZfClutchAdaptation(
    val clutchName: String,
    val pressureMbar: Int,
    val fillTimeMs: Int
)

data class DpfHealthStatus(
    val sootMassGrams: Double,
    val ashMassGrams: Double,
    val backpressureMbar: Double,
    val distanceSinceLastRegenKm: Int,
    val exhaustTempPreDpfC: Int,
    val exhaustTempPostDpfC: Int,
    val isRegenActive: Boolean,
    val isRegenRequested: Boolean,
    val lastRegenDate: String
)

enum class ClutchWearLevel(val label: String) {
    OPTIMAL("Optimal"),
    MODERATE_WEAR("Hafif Aşınma"),
    CRITICAL_WEAR("Kritik Aşınma")
}

data class ZfClutchDetail(
    val clutchName: String,
    val pressureMbar: Int,
    val fillTimeMs: Int,
    val wearLevel: ClutchWearLevel,
    val diagnosticNote: String
)

data class ZfGearboxHealth(
    val clutches: List<ZfClutchDetail>,
    val oilTemperatureC: Int,
    val oilDegradationScorePercent: Int,
    val overallHealthScorePercent: Int,
    val wearSummary: String
)

data class InjectorCalibration(
    val cylinder: Int,
    val imaCode: String
)

enum class CodingCategory(val label: String) {
    COMFORT("Konfor & Kilit"),
    INSTRUMENT("Gösterge & M/Alpina"),
    LIGHTING("Aydınlatma"),
    MULTIMEDIA("iDrive & Video"),
    DRIVETRAIN("Sürüş & Start-Stop")
}

data class CodingPreset(
    val id: String,
    val title: String,
    val description: String,
    val category: CodingCategory,
    val targetEcu: String,
    val targetAddress: Int,
    val cafdKey: String,
    val options: List<String>,
    val currentOptionIndex: Int,
    val isLockedBySecureCoding: Boolean = false
)

data class CafdParameter(
    val id: String,
    val ecuName: String,
    val ecuAddress: Int,
    val cafdFile: String,
    val section: String,
    val functionName: String,
    val byteOffset: Int,
    val bitMask: Int,
    val currentHex: String,
    val availableOptions: Map<String, String>,
    val selectedOptionLabel: String,
    val isLockedBySecureCoding: Boolean = false
)

data class CodingBackupSnapshot(
    val id: String,
    val timestamp: String,
    val ecuName: String,
    val ecuAddress: Int,
    val cafdFile: String,
    val summary: String,
    val previousValues: Map<String, Int>,
    val cafdByteSnapshot: Map<Int, String> = emptyMap()
)

data class HvCellVoltage(
    val cellIndex: Int,
    val voltageMv: Int
)

data class HvBatteryHealth(
    val stateOfChargePercent: Double,
    val stateOfHealthPercent: Double,
    val grossCapacityKwh: Double,
    val netAvailableCapacityKwh: Double,
    val maxCellVoltageMv: Int,
    val minCellVoltageMv: Int,
    val cellDeltaMv: Int,
    val avgCellVoltageMv: Int,
    val cellCount: Int,
    val cellVoltages: List<HvCellVoltage>,
    val minModuleTempC: Double,
    val maxModuleTempC: Double,
    val avgModuleTempC: Double,
    val insulationResistanceKOhm: Int,
    val contactorClosed: Boolean,
    val healthAssessment: String
)

data class AdasCalibrationStatus(
    val kafasStatus: String,
    val kafasProgressPercent: Int,
    val accYawOffsetMrad: Double,
    val accPitchOffsetMrad: Double,
    val isAccWithinTolerance: Boolean
)

data class ShortCircuitChannel(
    val id: String,
    val channelName: String,
    val ecuName: String,
    val ecuAddress: Int,
    val channelIndex: Int,
    val faultCounter: Int,
    val isLocked: Boolean
)

enum class ExhaustFlapMode(val optionByte: Byte) {
    OEM_AUTO(0x00),
    FORCE_OPEN(0x01),
    FORCE_CLOSED(0x02)
}

data class SoundTuningState(
    val exhaustFlapMode: ExhaustFlapMode = ExhaustFlapMode.OEM_AUTO,
    val asdEnabled: Boolean = true,
    val asdSupported: Boolean = true,
    val asdVolumePercent: Int = 100
)

enum class EpbServiceState {
    NORMAL,
    SERVICE_MODE_OPEN
}
