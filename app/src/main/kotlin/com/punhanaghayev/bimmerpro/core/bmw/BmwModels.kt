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
    E70("E70", "X5 (2006-2013)", SeriesFamily.E_SERIES),
    E87("E87", "1 Serisi (2004-2013)", SeriesFamily.E_SERIES)
}

object EcuAddress {
    const val ACSM = 0x01
    const val ZGW_BDC = 0x10
    const val DME_DDE = 0x12
    const val EGS = 0x18
    const val DSC = 0x29
    const val FEM_CAS = 0x40
    const val KOMBI = 0x60
    const val HU_MEDIA = 0x63
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
    val reportedMileageKm: Int? = null
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
    val istaGuidance: String
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
    STEERING_ANGLE_CALIBRATION
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
    val previousValues: Map<String, Int>
)
