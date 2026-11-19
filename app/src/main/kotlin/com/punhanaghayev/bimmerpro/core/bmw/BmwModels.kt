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
    SUSPICIOUS_MISMATCH("KM Uyumsuzluğu Tespit Edildi!")
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
)

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
)

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
