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
    )
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
)

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
    INTERMITTENT("Aralıklı")
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
)

data class LiveSensorChannel(
    val id: String,
    val name: String,
    val unit: String,
    val currentValue: Double,
    val minExpected: Double,
    val maxExpected: Double,
    val history: List<Float>
)

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
