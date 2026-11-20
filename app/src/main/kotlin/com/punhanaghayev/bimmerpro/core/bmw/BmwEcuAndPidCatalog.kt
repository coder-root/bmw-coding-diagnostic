package com.punhanaghayev.bimmerpro.core.bmw

data class BmwEcuDefinition(
    val address: Int,
    val shortName: String,
    val fullName: String
)

data class BmwLivePidDefinition(
    val id: String,
    val name: String,
    val unit: String,
    val ecuAddress: Int,
    val did: Int,
    val minExpected: Double,
    val maxExpected: Double,
    val defaultSample: Double
)

/**
 * Full 28-ECU BMW physical bus address map & 20-channel UDS 0x22 / OBD live PID definitions.
 */
object BmwEcuAndPidCatalog {

    val allBmwEcus: List<BmwEcuDefinition> = listOf(
        BmwEcuDefinition(0x01, "ACSM", "Crash Safety Module (Airbag)"),
        BmwEcuDefinition(0x10, "ZGW / BDC", "Central Gateway / Body Domain Controller"),
        BmwEcuDefinition(0x12, "DME / DDE", "Engine Control Unit"),
        BmwEcuDefinition(0x17, "EKPS", "Fuel Pump Control Module"),
        BmwEcuDefinition(0x18, "EGS", "ZF8 Automatic Transmission"),
        BmwEcuDefinition(0x19, "VTG", "xDrive Transfer Case Control"),
        BmwEcuDefinition(0x1C, "ICM", "Integrated Chassis Management"),
        BmwEcuDefinition(0x29, "DSC", "Dynamic Stability Control"),
        BmwEcuDefinition(0x30, "EPS", "Electric Power Steering"),
        BmwEcuDefinition(0x40, "CAS4 / FEM", "Car Access System / Front Electronic Module"),
        BmwEcuDefinition(0x43, "FLE_L", "Frontal Light Electronics Left"),
        BmwEcuDefinition(0x44, "FLE_R", "Frontal Light Electronics Right"),
        BmwEcuDefinition(0x50, "SAS", "Optional Equipment System (ADAS)"),
        BmwEcuDefinition(0x56, "FZD", "Roof Function Center (Sunroof/Alarm)"),
        BmwEcuDefinition(0x5D, "KAFAS", "Camera-Based Driver Assistance"),
        BmwEcuDefinition(0x5F, "TRSVC", "Surround / Rear View Camera"),
        BmwEcuDefinition(0x60, "KOMBI", "Instrument Cluster"),
        BmwEcuDefinition(0x61, "TCB / ATM", "Telematic Communication Box"),
        BmwEcuDefinition(0x63, "HU_NBT / MGU", "Head Unit Navigation"),
        BmwEcuDefinition(0x64, "PDC / PMA", "Park Distance / Parking Assistant"),
        BmwEcuDefinition(0x65, "PMA2", "Parking Maneuvering Assistant"),
        BmwEcuDefinition(0x67, "AMPT", "Top HiFi / Harman Kardon Amplifier"),
        BmwEcuDefinition(0x6D, "HKFM", "Tailgate / Trunk Lid Module"),
        BmwEcuDefinition(0x6E, "SMFA", "Seat Module Driver Memory"),
        BmwEcuDefinition(0x6F, "SMBF", "Seat Module Passenger Memory"),
        BmwEcuDefinition(0x72, "FRM / LMV", "Footwell / Light Module"),
        BmwEcuDefinition(0x73, "REM", "Rear Electronic Module"),
        BmwEcuDefinition(0x78, "IHKA", "Integrated Automatic Climate Control")
    )

    val livePidDefinitions: List<BmwLivePidDefinition> = listOf(
        BmwLivePidDefinition("rpm", "Motor Devri", "rpm", 0x12, 0xF40C, 650.0, 6800.0, 780.0),
        BmwLivePidDefinition("speed", "Araç Hızı", "km/h", 0x12, 0xF40D, 0.0, 260.0, 0.0),
        BmwLivePidDefinition("boost", "Turbo Boost Basıncı", "bar", 0x12, 0xF40B, -0.5, 2.2, 0.28),
        BmwLivePidDefinition("coolant", "Soğutma Suyu Sıcaklığı", "°C", 0x12, 0xF405, 75.0, 108.0, 90.0),
        BmwLivePidDefinition("oil_temp", "Motor Yağı Sıcaklığı", "°C", 0x12, 0x5805, 70.0, 125.0, 96.0),
        BmwLivePidDefinition("iat", "Emme Havası (IAT)", "°C", 0x12, 0xF40F, 10.0, 70.0, 34.0),
        BmwLivePidDefinition("rail", "Yakıt Rail Basıncı", "bar", 0x12, 0xF423, 250.0, 1800.0, 345.0),
        BmwLivePidDefinition("load", "Motor Yükü", "%", 0x12, 0xF404, 0.0, 100.0, 18.5),
        BmwLivePidDefinition("throttle", "Gaz Kelebeği Pozisyonu", "%", 0x12, 0xF411, 0.0, 100.0, 14.2),
        BmwLivePidDefinition("batt", "Akü / Alternatör Voltajı", "V", 0x10, 0xF442, 12.2, 14.9, 14.2),
        BmwLivePidDefinition("trans_temp", "ZF8 Şanzıman Yağı", "°C", 0x18, 0x4320, 40.0, 110.0, 68.0),
        BmwLivePidDefinition("dpf_diff", "DPF Fark Basıncı", "mbar", 0x12, 0x5821, 0.0, 180.0, 14.5),
        BmwLivePidDefinition("dpf_soot", "DPF Kurum Yükü (Soot)", "g", 0x12, 0x5822, 0.0, 45.0, 22.4),
        BmwLivePidDefinition("dpf_regen_km", "Son DPF Rejenerasyonu", "km", 0x12, 0x5825, 0.0, 1200.0, 310.0),
        BmwLivePidDefinition("inj_1", "Enjektör 1 Sapması", "mg/h", 0x12, 0x5831, -2.5, 2.5, 0.12),
        BmwLivePidDefinition("inj_2", "Enjektör 2 Sapması", "mg/h", 0x12, 0x5832, -2.5, 2.5, -0.08),
        BmwLivePidDefinition("inj_3", "Enjektör 3 Sapması", "mg/h", 0x12, 0x5833, -2.5, 2.5, 0.19),
        BmwLivePidDefinition("inj_4", "Enjektör 4 Sapması", "mg/h", 0x12, 0x5834, -2.5, 2.5, -0.14),
        BmwLivePidDefinition("turbo_target", "Hedef Boost Basıncı", "bar", 0x12, 0x5840, 0.0, 2.2, 0.30),
        BmwLivePidDefinition("alt_load", "Alternatör Yükü", "%", 0x12, 0x5850, 0.0, 100.0, 38.0)
    )
}
