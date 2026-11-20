package com.punhanaghayev.bimmerpro.core.simulator

import com.punhanaghayev.bimmerpro.core.bmw.ActuatorTestItem
import com.punhanaghayev.bimmerpro.core.bmw.BmwActuatorCatalog
import com.punhanaghayev.bimmerpro.core.bmw.BmwChassis
import com.punhanaghayev.bimmerpro.core.bmw.BmwCodingCatalog
import com.punhanaghayev.bimmerpro.core.bmw.BmwEcuAndPidCatalog
import com.punhanaghayev.bimmerpro.core.bmw.CafdParameter
import com.punhanaghayev.bimmerpro.core.bmw.CodingCapability
import com.punhanaghayev.bimmerpro.core.bmw.CodingPreset
import com.punhanaghayev.bimmerpro.core.bmw.DtcFault
import com.punhanaghayev.bimmerpro.core.bmw.DtcSeverity
import com.punhanaghayev.bimmerpro.core.bmw.DtcStatus
import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import com.punhanaghayev.bimmerpro.core.bmw.EcuModule
import com.punhanaghayev.bimmerpro.core.bmw.FreezeFrameData
import com.punhanaghayev.bimmerpro.core.bmw.IbsBatteryHealth
import com.punhanaghayev.bimmerpro.core.bmw.InjectorCalibration
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import com.punhanaghayev.bimmerpro.core.bmw.MileageModuleReading
import com.punhanaghayev.bimmerpro.core.bmw.MileageVerificationResult
import com.punhanaghayev.bimmerpro.core.bmw.ServiceHistoryRecord
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutine
import com.punhanaghayev.bimmerpro.core.bmw.ServiceRoutineType
import com.punhanaghayev.bimmerpro.core.bmw.TamperStatus
import com.punhanaghayev.bimmerpro.core.bmw.VehicleOrderOption
import com.punhanaghayev.bimmerpro.core.bmw.VehicleProfile
import com.punhanaghayev.bimmerpro.core.bmw.ZfClutchAdaptation
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType

data class SimulatedVehicleBundle(
    val profile: VehicleProfile,
    val ecus: List<EcuModule>,
    val dtcs: List<DtcFault>,
    val presets: List<CodingPreset>,
    val cafdParameters: List<CafdParameter>,
    val liveChannels: List<LiveSensorChannel>,
    val serviceRoutines: List<ServiceRoutine>,
    val serviceHistory: List<ServiceHistoryRecord> = emptyList(),
    val zfAdaptations: List<ZfClutchAdaptation> = BmwSimulatorCatalog.defaultZfAdaptations(),
    val injectorCodes: List<InjectorCalibration> = BmwSimulatorCatalog.defaultInjectors(4),
    val actuatorTests: List<ActuatorTestItem> = BmwActuatorCatalog.defaultTests(),
    val ibsHealth: IbsBatteryHealth = BmwActuatorCatalog.defaultIbsHealth()
)

object BmwSimulatorCatalog {

    fun buildBundle(adapterType: AdapterType): SimulatedVehicleBundle = when (adapterType) {
        AdapterType.SIMULATOR_F10_528I -> buildF10528iBundle()
        AdapterType.SIMULATOR_F30 -> buildF30Bundle()
        AdapterType.SIMULATOR_G30 -> buildG30Bundle()
        AdapterType.SIMULATOR_G20 -> buildG20Bundle()
        AdapterType.SIMULATOR_E90 -> buildE90Bundle()
        else -> buildF10Bundle()
    }

    private fun buildF10Bundle(): SimulatedVehicleBundle {
        val readings = listOf(
            MileageModuleReading("CAS4 (Karosseri)", 148_420),
            MileageModuleReading("KOMBI (Gösterge)", 148_420),
            MileageModuleReading("DDE7 (Motor ECU)", 148_412, engineHours = 3_110),
            MileageModuleReading("EGS (ZF 8HP)", 148_418)
        )
        val profile = VehicleProfile(
            vin = "WBA5C31040D842910",
            chassis = BmwChassis.F10,
            modelDisplayName = "BMW 520d Sedan (F10 LCI)",
            engineCode = "N47D20O1 (184 PS)",
            productionYear = 2014,
            iLevel = "F010-19-07-530",
            factoryILevel = "F010-14-03-500",
            typeCode = "5C31",
            productionDate = "18.04.2014",
            paintCode = "475 Black Sapphire Metallic",
            upholsteryCode = "LCSW Dakota Black",
            batteryVoltage = 12.6,
            odometerKm = 148_420,
            faOptions = listOf(
                VehicleOrderOption("S2TB", "Sport Otomatik Şanzıman (ZF 8HP)"),
                VehicleOrderOption("S403", "Elektrikli Cam Sunroof"),
                VehicleOrderOption("S552", "Adaptive LED Farlar"),
                VehicleOrderOption("S609", "Navigation System Professional (NBT)"),
                VehicleOrderOption("S6WB", "Multifunctional Instrument Display (Hayalet)")
            ),
            mileageVerification = MileageVerificationResult(
                readings = readings,
                maxDifferenceKm = 8,
                status = TamperStatus.VERIFIED,
                summary = "Tüm 4 ana kontrol ünitesi tutarlı (±8 km normal senkron farkı)."
            )
        )

        val ecus = listOf(
            EcuModule(EcuAddress.ZGW_BDC, "ZGW2", "Central Gateway Module", "HW 000010F1", "SWFL_000018B2", null, CodingCapability.FDL_ONLY, 0, 1, 148_420),
            EcuModule(EcuAddress.DME_DDE, "DDE7", "Digital Diesel Electronics", "HW 00000A12", "SWFL_000024C1", "CAFD_000001A7_002_014", CodingCapability.FDL_ONLY, 1, 1, 148_412),
            EcuModule(EcuAddress.EGS, "EGS", "ZF 8HP Transmission Control", "HW 00000834", "SWFL_00001B99", "CAFD_0000023F_016_016", CodingCapability.FULL_FDL_AND_VO, 0, 0, 148_418),
            EcuModule(EcuAddress.FEM_CAS, "CAS4", "Car Access System 4", "HW 00000901", "SWFL_000011D0", "CAFD_0000000F_005_025", CodingCapability.FULL_FDL_AND_VO, 1, 0, 148_420),
            EcuModule(EcuAddress.KOMBI, "KOMBI", "6WB Instrument Cluster", "HW 00001420", "SWFL_000020A4", "CAFD_00000069_009_002", CodingCapability.FULL_FDL_AND_VO, 0, 0, 148_420),
            EcuModule(EcuAddress.HU_MEDIA, "HU_NBT", "Head Unit NBT Professional", "HW 00001810", "SWFL_000029E0", "CAFD_00000DED_003_015", CodingCapability.FULL_FDL_AND_VO, 0, 1, null),
            EcuModule(EcuAddress.DSC, "DSC", "Dynamic Stability Control", "HW 000007C2", "SWFL_000012F8", "CAFD_000002A3_004_001", CodingCapability.FDL_ONLY, 0, 0, 148_420),
            EcuModule(EcuAddress.IHKA, "IHKA", "Automatic Climate Control", "HW 00000688", "SWFL_00000F12", "CAFD_00000092_007_003", CodingCapability.FULL_FDL_AND_VO, 0, 0, null)
        )

        val dtcs = listOf(
            DtcFault(
                id = "f10-245700",
                code = "245700",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "DDE7",
                title = "Partikül Filtresi Sistemi: Sınırlı Kalan Mesafe / Kurum Yükü",
                technicalDetail = "DPF diferansiyel basınç değeri nominal aralığın üst sınırına yaklaştı (kurum yükü 34.8 g).",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(148_390, 14.1, 88, 2150, 3),
                istaGuidance = "1) Canlı veriden DPF Egzoz Karşı Basıncı ve Kül Miktarını kontrol edin.\n2) Akü/yakıt yeterliyse Servis sekmesinden zorunlu DPF Rejenerasyonu başlatın."
            ),
            DtcFault(
                id = "f10-8040b9",
                code = "8040B9",
                ecuAddress = EcuAddress.FEM_CAS,
                ecuName = "CAS4",
                title = "Akü Deşarj / Enerji Yönetimi Uyku Akımı İhlali",
                technicalDetail = "Araç kilitliyken 80 mA üzeri parazit akım çekimi tespit edildi (muhtemel Comfort Access kapı kolu).",
                severity = DtcSeverity.INFO,
                status = DtcStatus.INTERMITTENT,
                freezeFrame = FreezeFrameData(148_210, 11.9, 24, 0, 2),
                istaGuidance = "1) Kapı kolu Comfort Access sensörlerinde nem/kısa devre kontrolü yapın.\n2) Akü yaşı > 4 yıl ise IBS akü sağlık ölçümünü doğrulayın."
            ),
            DtcFault(
                id = "f10-cd9304",
                code = "CD9304",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "DDE7",
                title = "LIN Bus: Aktif Radyatör Panjuru Haberleşme Gecikmesi",
                technicalDetail = "Gölge hafıza (Shadow Memory) — geçici LIN veri yolu cevapsızlığı.",
                severity = DtcSeverity.SHADOW,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(147_980, 13.8, 72, 1400, 1),
                istaGuidance = "Mekanik takılma yoksa ve hata aktifleşmiyorsa yalnızca bilgi amaçlı gölge kaydıdır; temizlenebilir."
            ),
            DtcFault(
                id = "f10-e1c447",
                code = "E1C447",
                ecuAddress = EcuAddress.HU_MEDIA,
                ecuName = "HU_NBT",
                title = "MOST Ring Geçici Senkronizasyon Kaybı",
                technicalDetail = "Soğuk çalıştırmada MOST fiber optik halka geçici olarak yeniden başlatıldı.",
                severity = DtcSeverity.SHADOW,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(146_500, 12.1, 18, 820, 1),
                istaGuidance = "Ses/iDrive kesintisi yaşanmıyorsa temizlenip izlenebilir."
            )
        )

        return SimulatedVehicleBundle(
            profile = profile,
            ecus = ecus,
            dtcs = dtcs,
            presets = defaultPresets(isGSeries = false),
            cafdParameters = defaultCafdParameters(isGSeries = false),
            liveChannels = defaultLiveChannels(isDiesel = true),
            serviceRoutines = defaultServiceRoutines(isDiesel = true),
            serviceHistory = listOf(
                ServiceHistoryRecord("sh1", "14.10.2025", 138_200, "Borusan Oto (04120)", listOf("Motor Yağı", "Mikrofiltre")),
                ServiceHistoryRecord("sh2", "02.09.2024", 124_500, "Punhan Garage (08801)", listOf("Motor Yağı", "Fren Hidroliği", "Araç Kontrol"))
            )
        )
    }

    private fun buildF10528iBundle(): SimulatedVehicleBundle {
        val readings = listOf(
            MileageModuleReading("CAS4 (Karosseri)", 132_850),
            MileageModuleReading("KOMBI (Gösterge)", 132_850),
            MileageModuleReading("MEVD1724 (DME Benzin)", 132_844, engineHours = 2_890),
            MileageModuleReading("EGS (ZF 8HP)", 132_848)
        )
        val profile = VehicleProfile(
            vin = "WBAXG51050D518420",
            chassis = BmwChassis.F10,
            modelDisplayName = "BMW 528i Sedan (F10 N20)",
            engineCode = "N20B20A (245 PS / 350 Nm Benzin)",
            productionYear = 2013,
            iLevel = "F010-19-07-530",
            factoryILevel = "F010-13-03-500",
            typeCode = "XG51",
            productionDate = "14.05.2013",
            paintCode = "A52 Spacegrau Metallic",
            upholsteryCode = "LCDF Dakota Leather Venetobeige",
            batteryVoltage = 12.6,
            odometerKm = 132_850,
            faOptions = listOf(
                VehicleOrderOption("1CC", "Otomatik Start/Stop Fonksiyonu (Auto Start/Stop)"),
                VehicleOrderOption("205", "Steptronic 8 İleri Otomatik Şanzıman"),
                VehicleOrderOption("248", "Direksiyon Simidi Isıtma"),
                VehicleOrderOption("255", "Spor Deri Direksiyon"),
                VehicleOrderOption("403", "Cam Açılır Tavan (Sunroof)"),
                VehicleOrderOption("430", "İç ve Dış Otomatik Kararan Elektrokrom Aynalar"),
                VehicleOrderOption("456", "Konfor Koltuklar, Elektrikli Hafızalı"),
                VehicleOrderOption("508", "Park Mesafe Kontrolü (PDC Ön/Arka)"),
                VehicleOrderOption("522", "Bi-Xenon Farlar"),
                VehicleOrderOption("534", "Gelişmiş 2 Bölgeli Otomatik Klima"),
                VehicleOrderOption("609", "Navigasyon Sistemi Professional (HU_NBT)"),
                VehicleOrderOption("676", "HiFi Hoparlör Ses Sistemi")
            ),
            mileageVerification = MileageVerificationResult(
                readings = readings,
                maxDifferenceKm = 6,
                status = TamperStatus.VERIFIED,
                summary = "Tüm 4 ana kontrol ünitesi tutarlı (±6 km senkronizasyon)"
            )
        )

        val ecus = listOf(
            EcuModule(EcuAddress.FEM_CAS, "CAS4", "Car Access System 4", "HW-06", "SW-4.82", "CAFD_0000000F_005_025", CodingCapability.FULL_FDL_AND_VO, 0, 0, 132_850),
            EcuModule(EcuAddress.DME_DDE, "MEVD1724", "DME Motor Yönetimi (N20 TwinPower Turbo)", "HW-12", "SW-9.10", null, CodingCapability.FDL_ONLY, 2, 0, 132_844),
            EcuModule(EcuAddress.EGS, "EGS", "Elektronik Şanzıman Yönetimi (ZF 8HP45)", "HW-04", "SW-3.20", null, CodingCapability.FDL_ONLY, 0, 0, 132_848),
            EcuModule(EcuAddress.DSC, "DSC", "Dinamik Denge Kontrolü", "HW-05", "SW-2.40", "CAFD_0000040F_002_012", CodingCapability.FULL_FDL_AND_VO, 0, 0),
            EcuModule(0x30, "EPS", "Elektronik Güç Destekli Direksiyon", "HW-03", "SW-1.90", null, CodingCapability.FDL_ONLY, 0, 0),
            EcuModule(EcuAddress.KOMBI, "KOMBI", "Gösterge Tablosu", "HW-08", "SW-6.50", "CAFD_000009C8_006_003", CodingCapability.FULL_FDL_AND_VO, 0, 0, 132_850),
            EcuModule(EcuAddress.HU_MEDIA, "HU_NBT", "Headunit High Professional", "HW-10", "SW-18.04", "CAFD_00000DED_003_013", CodingCapability.FULL_FDL_AND_VO, 0, 1),
            EcuModule(EcuAddress.IHKA, "IHKA", "Entegre Otomatik Isıtma/Klima", "HW-04", "SW-2.80", "CAFD_000016EE_001_004", CodingCapability.FULL_FDL_AND_VO, 0, 0),
            EcuModule(0x72, "FRM", "Ayak Bölmesi Modülü (Footwell Module 3)", "HW-07", "SW-5.12", "CAFD_0000067B_001_002", CodingCapability.FULL_FDL_AND_VO, 0, 0),
            EcuModule(EcuAddress.ZGW_BDC, "ZGW", "Merkezi Ağ Geçidi (Central Gateway)", "HW-02", "SW-1.40", null, CodingCapability.FDL_ONLY, 0, 0)
        )

        val dtcs = listOf(
            DtcFault(
                id = "d1_528",
                code = "120308",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "MEVD1724 (DME)",
                title = "Şarj Basıncı Kontrolü: Basınç Çok Düşük (Boost Pressure Low)",
                technicalDetail = "İstenen basınç ile ölçülen basınç sapması > 350 mbar (N20 Diverter valf kontrolü).",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(132_400, 13.8, 92, 2850, 2),
                istaGuidance = "1) Diverter valf (hava tahliye) diyaframında yırtık kontrolü yapın.\n2) Elektronik wastegate aktüatör kolunun hareket serbestliğini doğrulayın."
            ),
            DtcFault(
                id = "d2_528",
                code = "101F01",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "MEVD1724 (DME)",
                title = "Gaz Kelebeği Açısı — Emme Manifoldu Mutlak Basıncı Tutarsızlığı",
                technicalDetail = "Hava kütlesi hesaplama modeli ile MAP sensörü okuması arasında sapma.",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(132_750, 13.9, 96, 1200, 1),
                istaGuidance = "1) Emme manifoldu ve intercooler boru bağlantılarında duman testi ile hava kaçağı arayın.\n2) Gaz kelebeği gövdesi karbon temizliği yapın."
            ),
            DtcFault(
                id = "d3_528",
                code = "B7F805",
                ecuAddress = EcuAddress.HU_MEDIA,
                ecuName = "HU_NBT",
                title = "USB Bağlantı Soketi 1 İletişim Hatası",
                technicalDetail = "Kolçak içi USB arabiriminde geçici hat kesintisi kaydedildi.",
                severity = DtcSeverity.SHADOW,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(131_900, 12.5, 78, 0, 4),
                istaGuidance = "Gölge arıza kaydı — doğrudan işlevsel arıza yoksa silinebilir."
            )
        )

        return SimulatedVehicleBundle(
            profile = profile,
            ecus = ecus,
            dtcs = dtcs,
            presets = defaultPresets(isGSeries = false),
            cafdParameters = defaultCafdParameters(isGSeries = false),
            liveChannels = defaultLiveChannels(isDiesel = false),
            serviceRoutines = defaultServiceRoutines(isDiesel = false),
            serviceHistory = listOf(
                ServiceHistoryRecord("sh1_528", "18.05.2025", 128_400, "Borusan Oto İstinye (01020)", listOf("Motor Yağı 5W-30", "Buji Seti (4x)", "Mikrofiltre")),
                ServiceHistoryRecord("sh2_528", "12.04.2024", 115_200, "Kosifler Oto (02140)", listOf("Motor Yağı", "Fren Hidroliği", "Hava Filtresi"))
            ),
            zfAdaptations = listOf(
                ZfClutchAdaptation("Kavrama A (Clutch A)", 12, 4),
                ZfClutchAdaptation("Kavrama B (Clutch B)", -18, 8),
                ZfClutchAdaptation("Kavrama C (Clutch C)", 32, -2),
                ZfClutchAdaptation("Kavrama D (Clutch D)", 4, 1),
                ZfClutchAdaptation("Kavrama E (Clutch E)", -8, 6)
            ),
            injectorCodes = listOf(
                InjectorCalibration(1, "582218"),
                InjectorCalibration(2, "579214"),
                InjectorCalibration(3, "584220"),
                InjectorCalibration(4, "580216")
            )
        )
    }

    private fun buildF30Bundle(): SimulatedVehicleBundle {
        val base = buildF10Bundle()
        return base.copy(
            profile = base.profile.copy(
                vin = "WBA3A510X0F491822",
                chassis = BmwChassis.F30,
                modelDisplayName = "BMW 328i M Sport (F30)",
                engineCode = "N20B20O0 (245 PS)",
                productionYear = 2015,
                iLevel = "F020-20-07-540",
                odometerKm = 112_640,
                mileageVerification = base.profile.mileageVerification.copy(
                    readings = listOf(
                        MileageModuleReading("FEM_BODY", 112_640),
                        MileageModuleReading("KOMBI", 112_640),
                        MileageModuleReading("DME (N20)", 112_635, engineHours = 2_410)
                    ),
                    maxDifferenceKm = 5,
                    summary = "Tüm F30 modülleri tutarlı (±5 km)."
                )
            ),
            liveChannels = defaultLiveChannels(isDiesel = false),
            serviceRoutines = defaultServiceRoutines(isDiesel = false)
        )
    }

    private fun buildG20Bundle(): SimulatedVehicleBundle {
        val base = buildG30Bundle()
        return base.copy(
            profile = base.profile.copy(
                vin = "WBA5U91090FJ72105",
                chassis = BmwChassis.G20,
                modelDisplayName = "BMW M340i xDrive (G20)",
                engineCode = "B58B30O1 (374 PS)",
                productionYear = 2021,
                iLevel = "S18A-23-11-530",
                odometerKm = 39_850,
                mileageVerification = base.profile.mileageVerification.copy(
                    readings = listOf(
                        MileageModuleReading("BDC_BODY3", 39_850),
                        MileageModuleReading("DKOMBI", 39_850),
                        MileageModuleReading("DME8 (B58)", 39_847, engineHours = 820)
                    ),
                    maxDifferenceKm = 3,
                    summary = "Tüm G20 modülleri doğrulandı (±3 km)."
                )
            )
        )
    }

    private fun buildG30Bundle(): SimulatedVehicleBundle {
        val readings = listOf(
            MileageModuleReading("BDC_BODY3", 62_180),
            MileageModuleReading("KOMBI (Live Cockpit)", 62_180),
            MileageModuleReading("DME8 (B48)", 62_176, engineHours = 1_295),
            MileageModuleReading("EGS (ZF 8HP)", 62_180)
        )
        val profile = VehicleProfile(
            vin = "WBAJA51090WC91402",
            chassis = BmwChassis.G30,
            modelDisplayName = "BMW 530i xDrive (G30)",
            engineCode = "B48B20O0 (252 PS)",
            productionYear = 2019,
            iLevel = "S15A-23-03-540",
            batteryVoltage = 12.7,
            odometerKm = 62_180,
            faOptions = listOf(
                VehicleOrderOption("S2TB", "Sport Otomatik Şanzıman"),
                VehicleOrderOption("S337", "M Sport Paket"),
                VehicleOrderOption("S5AT", "Driving Assistant Plus"),
                VehicleOrderOption("S6U3", "BMW Live Cockpit Professional (MGU)"),
                VehicleOrderOption("S710", "M Deri Direksiyon")
            ),
            mileageVerification = MileageVerificationResult(
                readings = readings,
                maxDifferenceKm = 4,
                status = TamperStatus.VERIFIED,
                summary = "Tüm G30 modülleri doğrulandı (±4 km)."
            )
        )

        val ecus = listOf(
            EcuModule(EcuAddress.ZGW_BDC, "BDC_BODY", "Body Domain Controller", "HW 00003A11", "SWFL_00005D21", "CAFD_00007083_031_020", CodingCapability.FULL_FDL_AND_VO, 0, 0, 62_180),
            EcuModule(EcuAddress.DME_DDE, "DME8", "B48 Engine Control (Secure)", "HW 00002B10", "SWFL_00006E10", "CAFD_000029B8_001_009", CodingCapability.SECURE_CODING_LOCKED, 0, 1, 62_176),
            EcuModule(EcuAddress.EGS, "EGS", "ZF 8HP50 Transmission", "HW 000019F0", "SWFL_00004C81", "CAFD_000023AB_011_004", CodingCapability.FULL_FDL_AND_VO, 0, 0, 62_180),
            EcuModule(EcuAddress.KOMBI, "DKOMBI", "Live Cockpit Cluster", "HW 00004401", "SWFL_000071A0", "CAFD_00004508_009_012", CodingCapability.FULL_FDL_AND_VO, 0, 0, 62_180),
            EcuModule(EcuAddress.HU_MEDIA, "HU_MGU", "iDrive 7 MGU Head Unit", "HW 00005112", "SWFL_000081B4", "CAFD_00003E52_007_018", CodingCapability.SECURE_CODING_LOCKED, 1, 0, null),
            EcuModule(EcuAddress.DSC, "DSC3", "Dynamic Stability Control", "HW 00002109", "SWFL_000039C2", "CAFD_000019CC_005_008", CodingCapability.FDL_ONLY, 0, 0, 62_180),
            EcuModule(EcuAddress.IHKA, "IHKA4", "4-Zone Climate Control", "HW 00001E05", "SWFL_000030D1", "CAFD_00001E84_003_007", CodingCapability.FULL_FDL_AND_VO, 0, 0, null)
        )

        val dtcs = listOf(
            DtcFault(
                id = "g30-b7f8c3",
                code = "B7F8C3",
                ecuAddress = EcuAddress.HU_MEDIA,
                ecuName = "HU_MGU",
                title = "RAM / TCB Acil Çağrı Anteni Yüksek Empedans",
                technicalDetail = "Telematik bağlantı anten devresinde geçici yüksek direnç algılandı.",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.ACTIVE,
                freezeFrame = FreezeFrameData(62_140, 14.3, 91, 1850, 4),
                istaGuidance = "Tavan köpekbalığı anten (Shark Fin) su sızdırmazlığı ve TCB yedek pil durumunu kontrol edin."
            ),
            DtcFault(
                id = "g30-1f0510",
                code = "1F0510",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "DME8",
                title = "Valvetronic Limit Pozisyonu Soğuk Öğrenme",
                technicalDetail = "Gölge hafıza — soğuk başlangıçta Valvetronic eksantrik mili kalibrasyonu yenilendi.",
                severity = DtcSeverity.SHADOW,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(61_910, 12.4, 14, 1100, 1),
                istaGuidance = "Normal adaptasyon kaydıdır; rölanti dalgalanması yoksa işlem gerekmez."
            )
        )

        return SimulatedVehicleBundle(
            profile = profile,
            ecus = ecus,
            dtcs = dtcs,
            presets = defaultPresets(isGSeries = true),
            cafdParameters = defaultCafdParameters(isGSeries = true),
            liveChannels = defaultLiveChannels(isDiesel = false),
            serviceRoutines = defaultServiceRoutines(isDiesel = false)
        )
    }

    private fun buildE90Bundle(): SimulatedVehicleBundle {
        val readings = listOf(
            MileageModuleReading("CAS3 (Car Access)", 186_400),
            MileageModuleReading("KOMBI (Gösterge)", 186_400),
            MileageModuleReading("MSD81 (N54 DME)", 264_890, engineHours = 5_410),
            MileageModuleReading("DSC MK60", 264_850)
        )
        val profile = VehicleProfile(
            vin = "WBAVB71000KT48219",
            chassis = BmwChassis.E90,
            modelDisplayName = "BMW 335i Sedan (E90)",
            engineCode = "N54B30 Twin-Turbo (306 PS)",
            productionYear = 2009,
            iLevel = "E89X-12-09-500",
            batteryVoltage = 12.1,
            odometerKm = 186_400,
            faOptions = listOf(
                VehicleOrderOption("S205", "Otomatik Şanzıman"),
                VehicleOrderOption("S337", "M Sport Paket"),
                VehicleOrderOption("S522", "Bi-Xenon Farlar"),
                VehicleOrderOption("S609", "CCC / CIC Navigation Professional")
            ),
            mileageVerification = MileageVerificationResult(
                readings = readings,
                maxDifferenceKm = 78_490,
                status = TamperStatus.SUSPICIOUS_MISMATCH,
                summary = "DİKKAT: Gösterge/CAS (186.400 km) ile DME/DSC (264.890 km) arasında 78.490 km fark tespit edildi!"
            )
        )

        val ecus = listOf(
            EcuModule(EcuAddress.DME_DDE, "MSD81", "N54 Twin-Turbo DME", "HW 0759200", "SW 7619234", "NFRM/MSD81.C08", CodingCapability.FDL_ONLY, 2, 0, 264_890),
            EcuModule(EcuAddress.FEM_CAS, "CAS3", "Car Access System 3", "HW 0000914", "SW 0000311", "CAS3.C09", CodingCapability.FULL_FDL_AND_VO, 0, 0, 186_400),
            EcuModule(EcuAddress.KOMBI, "KOMBI", "E90 Instrument Cluster", "HW 0000820", "SW 0000412", "KOMBI.C09", CodingCapability.FULL_FDL_AND_VO, 0, 0, 186_400),
            EcuModule(EcuAddress.DSC, "DSC", "Dynamic Stability Control", "HW 0000678", "SW 0000219", "DSC.C09", CodingCapability.FDL_ONLY, 0, 1, 264_850)
        )

        val dtcs = listOf(
            DtcFault(
                id = "e90-30ff",
                code = "30FF",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "MSD81",
                title = "Boost Pressure Control: Hedef Basınç Altında (Turbo Underboost)",
                technicalDetail = "İstenen 0.62 bar takviye basıncına ulaşılamadı (ölçülen 0.34 bar).",
                severity = DtcSeverity.CRITICAL,
                status = DtcStatus.ACTIVE,
                freezeFrame = FreezeFrameData(264_840, 13.7, 94, 4820, 6),
                istaGuidance = "1) Vakum hatları ve basınç dönüştürücü selenoidleri (boost solenoids) test edin.\n2) Charge-pipe çatlağı ve wastegate rattle boşluğunu kontrol edin."
            ),
            DtcFault(
                id = "e90-29f4",
                code = "29F4",
                ecuAddress = EcuAddress.DME_DDE,
                ecuName = "MSD81",
                title = "Katalitik Konvertör Verimliliği Bank 1 Eşik Altı",
                technicalDetail = "Lambda sonrası sensör salınımı katalizör yaşlanmasına işaret ediyor.",
                severity = DtcSeverity.WARNING,
                status = DtcStatus.STORED,
                freezeFrame = FreezeFrameData(264_700, 13.9, 92, 2400, 3),
                istaGuidance = "Downpipe/katalizör durumu ve arka O2 sensör tepkisini canlı veride inceleyin."
            )
        )

        return SimulatedVehicleBundle(
            profile = profile,
            ecus = ecus,
            dtcs = dtcs,
            presets = defaultPresets(isGSeries = false),
            cafdParameters = defaultCafdParameters(isGSeries = false),
            liveChannels = defaultLiveChannels(isDiesel = false),
            serviceRoutines = defaultServiceRoutines(isDiesel = false),
            injectorCodes = defaultInjectors(6)
        )
    }

    internal fun defaultZfAdaptations(): List<ZfClutchAdaptation> = listOf(
        ZfClutchAdaptation("Kavrama A", pressureMbar = -18, fillTimeMs = 12),
        ZfClutchAdaptation("Kavrama B", pressureMbar = 24, fillTimeMs = 8),
        ZfClutchAdaptation("Kavrama C", pressureMbar = -6, fillTimeMs = 15),
        ZfClutchAdaptation("Kavrama D", pressureMbar = 42, fillTimeMs = 19),
        ZfClutchAdaptation("Kavrama E", pressureMbar = -31, fillTimeMs = 11)
    )

    internal fun defaultInjectors(count: Int): List<InjectorCalibration> =
        (1..count).map { cyl ->
            val sampleCodes = listOf("7H8K9M", "8A4F2C", "6D9P3N", "7B2E5T", "9C1L4X", "6K8R2W")
            InjectorCalibration(cyl, sampleCodes[(cyl - 1) % sampleCodes.size])
        }

    private fun defaultPresets(isGSeries: Boolean): List<CodingPreset> =
        BmwCodingCatalog.buildPresets(isGSeries)

    private fun defaultCafdParameters(isGSeries: Boolean): List<CafdParameter> =
        BmwCodingCatalog.buildCafdParameters(isGSeries)

    private fun defaultLiveChannels(isDiesel: Boolean): List<LiveSensorChannel> {
        return BmwEcuAndPidCatalog.livePidDefinitions
            .filter { pid -> isDiesel || !pid.id.startsWith("dpf_") }
            .map { pid ->
                val v = pid.defaultSample
                LiveSensorChannel(
                    id = pid.id,
                    name = pid.name,
                    unit = pid.unit,
                    currentValue = v,
                    minExpected = pid.minExpected,
                    maxExpected = pid.maxExpected,
                    history = listOf(
                        (v * 0.96).toFloat(),
                        (v * 0.98).toFloat(),
                        (v * 1.01).toFloat(),
                        v.toFloat(),
                        (v * 0.99).toFloat(),
                        v.toFloat()
                    )
                )
            }
    }

    private fun defaultServiceRoutines(isDiesel: Boolean): List<ServiceRoutine> = buildList {
        add(
            ServiceRoutine(
                type = ServiceRoutineType.BATTERY_REGISTRATION,
                title = "Akü Tanıtma & Kapasite Kaydı (IBS)",
                targetEcu = "DME/DDE",
                targetAddress = EcuAddress.DME_DDE,
                routineId = 0xF010,
                description = "Yeni akü değişimi sonrası şarj profilini sıfırlar (Örn. 90Ah AGM).",
                warningNote = "Yanlış akü tipi/kapasitesi seçilirse erken akü yıpranmasına yol açabilir."
            )
        )
        if (isDiesel) {
            add(
                ServiceRoutine(
                    type = ServiceRoutineType.DPF_REGENERATION,
                    title = "Zorunlu DPF Rejenerasyonu Başlat",
                    targetEcu = "DDE7",
                    targetAddress = EcuAddress.DME_DDE,
                    routineId = 0xF022,
                    description = "Dizel Partikül Filtresi kurum yakma döngüsünü sürüş için onaylar.",
                    warningNote = "Yakıt > 1/4 olmalı, soğutma suyu > 75°C ve kritik motor hatası bulunmamalıdır."
                )
            )
        }
        add(
            ServiceRoutine(
                type = ServiceRoutineType.EPB_SERVICE_MODE,
                title = "Elektronik El Freni (EPB) Servis Modu",
                targetEcu = "DSC",
                targetAddress = EcuAddress.DSC,
                routineId = 0xF031,
                description = "Arka balata değişimi için EMF kaliper pistonlarını geri çeker.",
                warningNote = "Araç düz zeminde ve takozlu olmalıdır."
            )
        )
        add(
            ServiceRoutine(
                type = ServiceRoutineType.CBS_OIL_RESET,
                title = "CBS Bakım Sıfırlama (Yağ / Fren / Araç Kontrol)",
                targetEcu = "KOMBI",
                targetAddress = EcuAddress.KOMBI,
                routineId = 0xF040,
                description = "iDrive servis sayacını yeniler ve bakım geçmişine kayıt işler.",
                warningNote = "Yalnızca bakım işlemi fiilen tamamlandıktan sonra sıfırlayın."
            )
        )
        add(
            ServiceRoutine(
                type = ServiceRoutineType.EXHAUST_VALVE_OPEN,
                title = "Egzoz Valfi Kontrolü (Aç / Fabrika)",
                targetEcu = "DME/DDE",
                targetAddress = EcuAddress.DME_DDE,
                routineId = 0xF055,
                description = "Sağ egzoz flapını açık konumda kilitler veya fabrika haritasına döndürür.",
                warningNote = "Kontak açık (PAD modu) veya motor rölantide olmalıdır."
            )
        )
        add(
            ServiceRoutine(
                type = ServiceRoutineType.STEERING_ANGLE_CALIBRATION,
                title = "Direksiyon Açı Sensörü (SZL) Kalibrasyonu",
                targetEcu = "DSC",
                targetAddress = EcuAddress.DSC,
                routineId = 0xF061,
                description = "Rot ayarı veya akü sökümü sonrası sıfır noktası kalibrasyonu yapar.",
                warningNote = "Direksiyon tam düz konumda tutulmalıdır."
            )
        )
    }
}
