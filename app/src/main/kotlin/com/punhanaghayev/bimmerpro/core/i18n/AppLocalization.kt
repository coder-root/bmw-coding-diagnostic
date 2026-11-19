package com.punhanaghayev.bimmerpro.core.i18n

import android.content.Context
import androidx.compose.runtime.compositionLocalOf
import com.punhanaghayev.bimmerpro.core.protocol.AdapterType

enum class AppLanguage(val code: String, val nativeName: String, val flagCode: String) {
    AZ("az", "Azərbaycan", "AZ"),
    TR("tr", "Türkçe", "TR"),
    EN("en", "English", "EN"),
    RU("ru", "Русский", "RU");

    companion object {
        fun fromCode(code: String?): AppLanguage =
            entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: TR
    }
}

data class AppStrings(
    val tabGarage: String,
    val tabDiagnostics: String,
    val tabCoding: String,
    val tabLiveService: String,
    val tabDeveloper: String,

    val heroSubtitle: String,
    val heroSubtitleLine2: String,
    val disconnectedHint: String,
    val connectionHubTitle: String,
    val scanHardwareBtn: String,
    val adapterDropdownLabel: String,
    val connectVehicleBtn: String,
    val reconnectBtn: String,
    val disconnectBtn: String,
    val connectingText: String,
    val discoveredDevicesTitle: String,
    val quickDiagnostics: String,
    val quickCoding: String,
    val quickLiveService: String,

    val scanAllEcusBtn: String,
    val scanningText: String,
    val expertisePdfBtn: String,
    val mileageCheckTitle: String,
    val clearAllDtcBtn: String,
    val noDtcInFilter: String,
    val scannedEcusTitle: String,

    val codingEasyMode: String,
    val codingExpertCafd: String,
    val codingFaVoBackup: String,
    val batterySafeBanner: String,
    val batteryLockedBanner: String,

    val liveTelemetryTitle: String,
    val startBtn: String,
    val stopBtn: String,
    val serviceRoutinesTitle: String,
    val runRoutineBtn: String,
    val serviceHistoryTitle: String,
    val writeRecordBtn: String,

    val languageTitle: String,
    val otaTitle: String,
    val checkUpdatesBtn: String,
    val changelogTitle: String,
    val supportedArchTitle: String,
    val packetInspectorTitle: String,
    val packetInspectorSub: String,
    val clearBtn: String,
    val themeTitle: String,
    val savedVehiclesTitle: String,
    val exportJsonBtn: String,
    val importJsonBtn: String,
    val ecuResetTitle: String,
    val gatewaySleepBtn: String,
    val hardResetBtn: String,
    val disconnectedDiagTitle: String,
    val disconnectedDiagSub: String,
    val disconnectedCodingTitle: String,
    val disconnectedCodingSub: String,
    val disconnectedLiveTitle: String,
    val disconnectedLiveSub: String,
    val goToGarageBtn: String,
    val filterAll: String,
    val filterWarn: String,
    val filterShadow: String,
    val clearEcuDtcBtn: String,
    val ecuResetBtn: String,
    val statusClean: String,
    val statusFault: String,
    val searchCodingHint: String,
    val searchCafdHint: String,
    val faVoTitle: String,
    val codeLabel: String,
    val descLabel: String,
    val addVoBtn: String,
    val autoBackupsTitle: String,
    val noBackupsHint: String,
    val rollbackBtn: String,
    val currentValueLabel: String,
    val perfTimerTitle: String,
    val sportGaugeTitle: String,
    val zfAdaptTitle: String,
    val resetBtn: String,
    val injectorTitle: String,
    val codeBtn: String,
    val selectVersionLabel: String,
    val language: AppLanguage
) {
    fun adapterTitle(type: AdapterType): String = when (language) {
        AppLanguage.AZ -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "Vgate vLinker BM Wi-Fi"
            AdapterType.ENET_WIFI -> "Wi-Fi ENET (Thor / MHD / BM3)"
            AdapterType.ENET_USB -> "USB-C to ENET Kabel"
            AdapterType.BLE_OBD -> "OBDLink CX / UniCarScan BLE"
            AdapterType.USB_KDCAN -> "USB OTG K+DCAN Kabel"
            AdapterType.SIMULATOR_F10 -> "BMW F10 520d Simulyasiyası"
            AdapterType.SIMULATOR_F30 -> "BMW F30 328i Simulyasiyası"
            AdapterType.SIMULATOR_G30 -> "BMW G30 530i Simulyasiyası"
            AdapterType.SIMULATOR_G20 -> "BMW G20 M340i Simulyasiyası"
            AdapterType.SIMULATOR_E90 -> "BMW E90 335i Simulyasiyası"
        }
        AppLanguage.TR -> type.displayName
        AppLanguage.EN -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "Vgate vLinker BM Wi-Fi"
            AdapterType.ENET_WIFI -> "Wi-Fi ENET (Thor / MHD / BM3)"
            AdapterType.ENET_USB -> "USB-C to ENET Cable"
            AdapterType.BLE_OBD -> "OBDLink CX / UniCarScan BLE"
            AdapterType.USB_KDCAN -> "USB OTG K+DCAN Cable"
            AdapterType.SIMULATOR_F10 -> "BMW F10 520d Simulation"
            AdapterType.SIMULATOR_F30 -> "BMW F30 328i Simulation"
            AdapterType.SIMULATOR_G30 -> "BMW G30 530i Simulation"
            AdapterType.SIMULATOR_G20 -> "BMW G20 M340i Simulation"
            AdapterType.SIMULATOR_E90 -> "BMW E90 335i Simulation"
        }
        AppLanguage.RU -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "Vgate vLinker BM Wi-Fi"
            AdapterType.ENET_WIFI -> "Wi-Fi ENET (Thor / MHD / BM3)"
            AdapterType.ENET_USB -> "Кабель USB-C — ENET"
            AdapterType.BLE_OBD -> "OBDLink CX / UniCarScan BLE"
            AdapterType.USB_KDCAN -> "Кабель USB OTG K+DCAN"
            AdapterType.SIMULATOR_F10 -> "Симуляция BMW F10 520d"
            AdapterType.SIMULATOR_F30 -> "Симуляция BMW F30 328i"
            AdapterType.SIMULATOR_G30 -> "Симуляция BMW G30 530i"
            AdapterType.SIMULATOR_G20 -> "Симуляция BMW G20 M340i"
            AdapterType.SIMULATOR_E90 -> "Симуляция BMW E90 335i"
        }
    }

    fun adapterDesc(type: AdapterType): String = when (language) {
        AppLanguage.AZ -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "STN2255 Wi-Fi TCP 192.168.0.10:35000 (F/G/E)"
            AdapterType.ENET_WIFI -> "HSFZ / DoIP TCP:6801"
            AdapterType.ENET_USB -> "HSFZ / DoIP 100Mbps Ethernet"
            AdapterType.BLE_OBD -> "CAN üzərindən ISO-TP (BLE GATT)"
            AdapterType.USB_KDCAN -> "E-Seriyası K-Line / D-CAN"
            AdapterType.SIMULATOR_F10 -> "Virtual HSFZ / ENET + UDS"
            AdapterType.SIMULATOR_F30 -> "Virtual HSFZ / ENET + N20"
            AdapterType.SIMULATOR_G30 -> "Virtual DoIP / HSFZ + Secure Coding"
            AdapterType.SIMULATOR_G20 -> "Virtual DoIP / B58 + iDrive 7"
            AdapterType.SIMULATOR_E90 -> "Virtual K+DCAN / KM Yoxlaması Demo"
        }
        AppLanguage.TR -> type.protocolSummary
        AppLanguage.EN -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "STN2255 Wi-Fi TCP 192.168.0.10:35000 (F/G/E)"
            AdapterType.ENET_WIFI -> "HSFZ / DoIP TCP:6801"
            AdapterType.ENET_USB -> "HSFZ / DoIP 100Mbps Ethernet"
            AdapterType.BLE_OBD -> "ISO-TP over CAN (BLE GATT)"
            AdapterType.USB_KDCAN -> "E-Series K-Line / D-CAN"
            AdapterType.SIMULATOR_F10 -> "Virtual HSFZ / ENET + UDS"
            AdapterType.SIMULATOR_F30 -> "Virtual HSFZ / ENET + N20"
            AdapterType.SIMULATOR_G30 -> "Virtual DoIP / HSFZ + Secure Coding"
            AdapterType.SIMULATOR_G20 -> "Virtual DoIP / B58 + iDrive 7"
            AdapterType.SIMULATOR_E90 -> "Virtual K+DCAN / Tamper Alert Demo"
        }
        AppLanguage.RU -> when (type) {
            AdapterType.VLINKER_BM_WIFI -> "STN2255 Wi-Fi TCP 192.168.0.10:35000 (F/G/E)"
            AdapterType.ENET_WIFI -> "HSFZ / DoIP TCP:6801"
            AdapterType.ENET_USB -> "HSFZ / DoIP 100 Мбит/с Ethernet"
            AdapterType.BLE_OBD -> "ISO-TP по CAN (BLE GATT)"
            AdapterType.USB_KDCAN -> "E-серия K-Line / D-CAN"
            AdapterType.SIMULATOR_F10 -> "Виртуальный HSFZ / ENET + UDS"
            AdapterType.SIMULATOR_F30 -> "Виртуальный HSFZ / ENET + N20"
            AdapterType.SIMULATOR_G30 -> "Виртуальный DoIP / HSFZ + Secure Coding"
            AdapterType.SIMULATOR_G20 -> "Виртуальный DoIP / B58 + iDrive 7"
            AdapterType.SIMULATOR_E90 -> "Виртуальный K+DCAN / Тест пробега"
        }
    }
}

object AppLocalization {

    private const val PREFS_NAME = "bmw_expert_pro_prefs"
    private const val KEY_LANGUAGE = "app_language"

    fun loadSavedLanguage(context: Context): AppLanguage {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return AppLanguage.fromCode(prefs.getString(KEY_LANGUAGE, AppLanguage.TR.code))
    }

    fun saveLanguage(context: Context, language: AppLanguage) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, language.code)
            .apply()
    }

    fun stringsFor(language: AppLanguage): AppStrings = when (language) {
        AppLanguage.AZ -> AppStrings(
            tabGarage = "Qaraj",
            tabDiagnostics = "Diaqnostika",
            tabCoding = "Kodlama",
            tabLiveService = "Canlı/Servis",
            tabDeveloper = "Developer",
            heroSubtitle = "Tam BMW Kodlama",
            heroSubtitleLine2 = "Diaqnostika və Ekspertiza",
            disconnectedHint = "Qoşulmayıb — Siyahıdan adapter seçin",
            connectionHubTitle = "OBD / ENET Bağlantı Mərkəzi",
            scanHardwareBtn = "Avadanlıq Axtar (ENET / BLE / USB)",
            adapterDropdownLabel = "Adapter və ya Simulyator Seçin",
            connectVehicleBtn = "Avtomobilə Qoşul",
            reconnectBtn = "Seçilmiş Adapterlə Yenidən Qoşul",
            disconnectBtn = "Bağlantını Kəs",
            connectingText = "Qoşulur və VIN oxunur...",
            discoveredDevicesTitle = "Tapılan Fiziki Cihazlar",
            quickDiagnostics = "Diaqnostika",
            quickCoding = "Kodlama",
            quickLiveService = "Canlı & Servis",
            scanAllEcusBtn = "Bütün ECU-ları Skan Et",
            scanningText = "Skan edilir...",
            expertisePdfBtn = "Ekspertiza PDF",
            mileageCheckTitle = "Çoxmodullu Yürüş (KM) Yoxlaması",
            clearAllDtcBtn = "Hamısını Sil",
            noDtcInFilter = "Seçilmiş filtrdə xəta kodu tapılmadı.",
            scannedEcusTitle = "Skan Edilən ECU Modulları",
            codingEasyMode = "Sadə Rejim",
            codingExpertCafd = "Expert CAFD",
            codingFaVoBackup = "FA/VO & Ehtiyat",
            batterySafeBanner = "Akkumulyator %.1f V (Təhlükəsiz) • Avtomatik CAFD ehtiyat nüsxəsi aktivdir.",
            batteryLockedBanner = "KODLAMA KİLİDLİ: Akkumulyator %.1f V (< 12.2 V)! Şarj cihazı qoşun.",
            liveTelemetryTitle = "Canlı Sensor Telemetriyası",
            startBtn = "Başlat",
            stopBtn = "Dayandır",
            serviceRoutinesTitle = "Servis və Baxım Funksiyaları (UDS 0x31)",
            runRoutineBtn = "Funksiyanı İşə Sal",
            serviceHistoryTitle = "iDrive Rəqəmsal Servis Tarixçəsi",
            writeRecordBtn = "Qeyd Yaz",
            languageTitle = "Tətbiq Dili (Dil Seçimi)",
            otaTitle = "Versiya İzləmə və OTA Yeniləmə",
            checkUpdatesBtn = "Yoxla",
            changelogTitle = "Versiya Tarixçəsi (Changelog)",
            supportedArchTitle = "Dəstəklənən BMW Arxitekturası və Protokollar",
            packetInspectorTitle = "Canlı HSFZ / UDS Paket İzləyici",
            packetInspectorSub = "Real vaxt TX / RX hex çərçivələri",
            clearBtn = "Təmizlə",
            themeTitle = "Tətbiq Teması",
            savedVehiclesTitle = "Qaraj Tarixçəsi / Saxlanılan VIN-lər",
            exportJsonBtn = "JSON İxrac",
            importJsonBtn = "JSON İdxal",
            ecuResetTitle = "ECU Sıfırlama / Yuxu Rejimi (UDS 0x11)",
            gatewaySleepBtn = "Yuxu Rejimi (Sleep)",
            hardResetBtn = "Gateway Reset",
            disconnectedDiagTitle = "Diaqnostika Hazır Deyil",
            disconnectedDiagSub = "Bütün ECU-ları skan etmək və ekspertiza hesabatı almaq üçün əvvəlcə avtomobilə qoşulun.",
            disconnectedCodingTitle = "Kodlama Mühərriki Gözləmədə",
            disconnectedCodingSub = "Sadə Rejim, Expert CAFD/FDL və FA/VO kodlama üçün əvvəlcə avtomobilə qoşulun.",
            disconnectedLiveTitle = "Canlı Məlumat və Servis Gözləmədə",
            disconnectedLiveSub = "Sensor qrafikləri, DPF regenerasiyası və akkumulyator qeydi üçün əvvəlcə qoşulun.",
            goToGarageBtn = "Qaraj və Bağlantı Ekranına Keç",
            filterAll = "Hamısı",
            filterWarn = "Kritik / Xəbərdarlıq",
            filterShadow = "Kölgə",
            clearEcuDtcBtn = "ECU Xəta Sil",
            ecuResetBtn = "ECU Reset",
            statusClean = "TƏMİZ",
            statusFault = "XƏTA",
            searchCodingHint = "Kodlama axtar...",
            searchCafdHint = "CAFD / Funksiya axtar (BMW_LOGO, MSA)",
            faVoTitle = "Avtomobil Sifariş Kodları (FA / VO)",
            codeLabel = "Kod",
            descLabel = "Açıqlama",
            addVoBtn = "VO Əlavə Et və Kodla",
            autoBackupsTitle = "Avtomatik Kodlama Ehtiyatları",
            noBackupsHint = "Hələ kodlama edilməyib. Hər yazmadan əvvəl bura bərpa nöqtəsi əlavə olunur.",
            rollbackBtn = "Geri Al",
            currentValueLabel = "Cari Dəyər:",
            perfTimerTitle = "0–100 km/saat Performans Ölçən",
            sportGaugeTitle = "M-Sport Canlı Panel & Güc Təxmini",
            zfAdaptTitle = "ZF8 EGS Mufta Adaptasiyaları",
            resetBtn = "Sıfırla",
            injectorTitle = "İnjektor IMA Kalibrasiya Kodları",
            codeBtn = "Kodla",
            selectVersionLabel = "Buraxılış Versiyası Seçin",
            language = AppLanguage.AZ
        )

        AppLanguage.TR -> AppStrings(
            tabGarage = "Garaj",
            tabDiagnostics = "Diagnostik",
            tabCoding = "Kodlama",
            tabLiveService = "Canlı/Servis",
            tabDeveloper = "Developer",
            heroSubtitle = "Full BMW Coding",
            heroSubtitleLine2 = "Diagnostics & Inspection",
            disconnectedHint = "Bağlı Değil — Dropdown'dan adaptör seçin",
            connectionHubTitle = "OBD / ENET Bağlantı Merkezi",
            scanHardwareBtn = "Donanım Tara (ENET / BLE / USB)",
            adapterDropdownLabel = "Bağlantı Adaptörü / Simülatör Seçin",
            connectVehicleBtn = "Araca Bağlan",
            reconnectBtn = "Seçili Adaptörle Yeniden Bağlan",
            disconnectBtn = "Araç Bağlantısını Kes",
            connectingText = "Bağlanıyor & VIN Okunuyor...",
            discoveredDevicesTitle = "Bulunan Fiziksel Aygıtlar",
            quickDiagnostics = "Diagnostik",
            quickCoding = "Kodlama",
            quickLiveService = "Canlı & Servis",
            scanAllEcusBtn = "Tüm ECU Tara",
            scanningText = "Taranıyor...",
            expertisePdfBtn = "Ekspertiz PDF",
            mileageCheckTitle = "Çoklu Modül KM Doğrulama",
            clearAllDtcBtn = "Sil",
            noDtcInFilter = "Seçili filtrede arıza kaydı bulunmuyor.",
            scannedEcusTitle = "Tarama Yapılan ECU Modülleri",
            codingEasyMode = "Easy Mode",
            codingExpertCafd = "Expert CAFD",
            codingFaVoBackup = "FA/VO & Yedek",
            batterySafeBanner = "Akü %.1f V (Güvenli) • Her kodlama öncesi otomatik CAFD yedeği alınır.",
            batteryLockedBanner = "KODLAMA KİLİTLİ: Akü %.1f V (< 12.2 V)! Şarj cihazı bağlayın veya motoru çalıştırın.",
            liveTelemetryTitle = "Canlı Sensör Telemetrisi",
            startBtn = "Başlat",
            stopBtn = "Durdur",
            serviceRoutinesTitle = "Servis & Bakım Fonksiyonları (UDS 0x31)",
            runRoutineBtn = "Rutini Çalıştır",
            serviceHistoryTitle = "iDrive Dijital Servis Geçmişi",
            writeRecordBtn = "Kayıt Yaz",
            languageTitle = "Uygulama Dili",
            otaTitle = "Versiyon Takibi & OTA Güncelleme",
            checkUpdatesBtn = "Kontrol Et",
            changelogTitle = "Sürüm Geçmişi (Changelog)",
            supportedArchTitle = "Desteklenen BMW Mimarisi & Protokol Yığını",
            packetInspectorTitle = "Canlı HSFZ / UDS Paket İnceleyici",
            packetInspectorSub = "Gerçek zamanlı TX / RX hex çerçeveleri",
            clearBtn = "Temizle",
            themeTitle = "Uygulama Teması",
            savedVehiclesTitle = "Kayıtlı Araçlarım / Garaj Geçmişi",
            exportJsonBtn = "JSON Dışa Aktar",
            importJsonBtn = "JSON İçe Aktar",
            ecuResetTitle = "ECU Reset & Uyku Modu (UDS 0x11)",
            gatewaySleepBtn = "Uyku Modu (Sleep)",
            hardResetBtn = "Gateway Reset",
            disconnectedDiagTitle = "Diagnostik Hazır Değil",
            disconnectedDiagSub = "Tüm ECU'ları taramak ve ekspertiz raporu almak için önce araç bağlantısı kurun.",
            disconnectedCodingTitle = "Kodlama Motoru Beklemede",
            disconnectedCodingSub = "Easy Mode, Expert CAFD/FDL ve FA/VO kodlama için önce araca bağlanın.",
            disconnectedLiveTitle = "Canlı Veri & Servis Beklemede",
            disconnectedLiveSub = "Sensör grafikleri, DPF rejenerasyonu ve akü tanıtma işlemleri için önce araca bağlanın.",
            goToGarageBtn = "Garaj & Bağlantı Ekranına Git",
            filterAll = "Tümü",
            filterWarn = "Kritik / Uyarı",
            filterShadow = "Gölge",
            clearEcuDtcBtn = "ECU Hata Sil",
            ecuResetBtn = "ECU Reset",
            statusClean = "TEMİZ",
            statusFault = "HATA",
            searchCodingHint = "Kodlama ara...",
            searchCafdHint = "CAFD / Fonksiyon ara (BMW_LOGO, MSA)",
            faVoTitle = "Araç Sipariş Kodları (FA / VO)",
            codeLabel = "Kod",
            descLabel = "Açıklama",
            addVoBtn = "VO Opsiyonu Ekle & Kodla",
            autoBackupsTitle = "Otomatik Kodlama Yedekleri",
            noBackupsHint = "Henüz kodlama yapılmadı. Her yazma işleminde buraya geri yükleme noktası eklenir.",
            rollbackBtn = "Geri Al",
            currentValueLabel = "Mevcut Değer:",
            perfTimerTitle = "0–100 km/h Performans Ölçer",
            sportGaugeTitle = "M-Sport Canlı Kadran & Güç Tahmini",
            zfAdaptTitle = "ZF8 EGS Kavrama Adaptasyonları",
            resetBtn = "Sıfırla",
            injectorTitle = "Enjektör IMA Kalibrasyon Kodları",
            codeBtn = "Kodla",
            selectVersionLabel = "Sürüm Seçin",
            language = AppLanguage.TR
        )

        AppLanguage.EN -> AppStrings(
            tabGarage = "Garage",
            tabDiagnostics = "Diagnostics",
            tabCoding = "Coding",
            tabLiveService = "Live/Service",
            tabDeveloper = "Developer",
            heroSubtitle = "Full BMW Coding",
            heroSubtitleLine2 = "Diagnostics & Inspection",
            disconnectedHint = "Disconnected — Select an adapter from dropdown",
            connectionHubTitle = "OBD / ENET Connection Hub",
            scanHardwareBtn = "Scan Hardware (ENET / BLE / USB)",
            adapterDropdownLabel = "Select Adapter or Simulator",
            connectVehicleBtn = "Connect Vehicle",
            reconnectBtn = "Reconnect Selected Adapter",
            disconnectBtn = "Disconnect Vehicle",
            connectingText = "Connecting & Reading VIN...",
            discoveredDevicesTitle = "Discovered Physical Devices",
            quickDiagnostics = "Diagnostics",
            quickCoding = "Coding",
            quickLiveService = "Live & Service",
            scanAllEcusBtn = "Scan All ECUs",
            scanningText = "Scanning...",
            expertisePdfBtn = "Inspection PDF",
            mileageCheckTitle = "Cross-ECU Mileage Verification",
            clearAllDtcBtn = "Clear",
            noDtcInFilter = "No fault codes in selected filter.",
            scannedEcusTitle = "Scanned ECU Modules",
            codingEasyMode = "Easy Mode",
            codingExpertCafd = "Expert CAFD",
            codingFaVoBackup = "FA/VO & Backups",
            batterySafeBanner = "Battery %.1f V (Safe) • Auto CAFD backup enabled before write.",
            batteryLockedBanner = "CODING LOCKED: Battery %.1f V (< 12.2 V)! Connect a charger.",
            liveTelemetryTitle = "Live Sensor Telemetry",
            startBtn = "Start",
            stopBtn = "Stop",
            serviceRoutinesTitle = "Service & Maintenance Routines (UDS 0x31)",
            runRoutineBtn = "Run Routine",
            serviceHistoryTitle = "iDrive Digital Service History",
            writeRecordBtn = "Write Entry",
            languageTitle = "App Language",
            otaTitle = "Version Tracking & OTA Updates",
            checkUpdatesBtn = "Check",
            changelogTitle = "Release History (Changelog)",
            supportedArchTitle = "Supported BMW Architecture & Protocol Stack",
            packetInspectorTitle = "Live HSFZ / UDS Packet Inspector",
            packetInspectorSub = "Real-time TX / RX hex frames",
            clearBtn = "Clear",
            themeTitle = "App Theme",
            savedVehiclesTitle = "Saved Vehicles / Garage History",
            exportJsonBtn = "Export JSON",
            importJsonBtn = "Import JSON",
            ecuResetTitle = "ECU Reset & Sleep Mode (UDS 0x11)",
            gatewaySleepBtn = "Sleep Mode",
            hardResetBtn = "Gateway Reset",
            disconnectedDiagTitle = "Diagnostics Not Ready",
            disconnectedDiagSub = "Connect to your BMW first to scan all ECUs and generate an inspection report.",
            disconnectedCodingTitle = "Coding Engine Standby",
            disconnectedCodingSub = "Connect to your vehicle first for Easy Mode, Expert CAFD/FDL, and FA/VO coding.",
            disconnectedLiveTitle = "Live Data & Service Standby",
            disconnectedLiveSub = "Connect to your vehicle first for live sensors, DPF regen, and battery registration.",
            goToGarageBtn = "Go to Garage & Connection",
            filterAll = "All",
            filterWarn = "Critical / Warn",
            filterShadow = "Shadow",
            clearEcuDtcBtn = "Clear ECU DTC",
            ecuResetBtn = "ECU Reset",
            statusClean = "CLEAN",
            statusFault = "FAULT",
            searchCodingHint = "Search coding...",
            searchCafdHint = "Search CAFD / Function (BMW_LOGO, MSA)",
            faVoTitle = "Vehicle Order Options (FA / VO)",
            codeLabel = "Code",
            descLabel = "Description",
            addVoBtn = "Add VO Option & Code",
            autoBackupsTitle = "Automatic Coding Backups",
            noBackupsHint = "No coding applied yet. A restore snapshot is saved here before each write.",
            rollbackBtn = "Rollback",
            currentValueLabel = "Current Value:",
            perfTimerTitle = "0–100 km/h Performance Timer",
            sportGaugeTitle = "M-Sport Live Gauges & Power Est.",
            zfAdaptTitle = "ZF8 EGS Clutch Adaptations",
            resetBtn = "Reset",
            injectorTitle = "Injector IMA Calibration Codes",
            codeBtn = "Write",
            selectVersionLabel = "Select Version",
            language = AppLanguage.EN
        )

        AppLanguage.RU -> AppStrings(
            tabGarage = "Гараж",
            tabDiagnostics = "Диагностика",
            tabCoding = "Кодирование",
            tabLiveService = "Данные/Сервис",
            tabDeveloper = "Разработчик",
            heroSubtitle = "Полное кодирование BMW",
            heroSubtitleLine2 = "Диагностика и проверка",
            disconnectedHint = "Не подключено — выберите адаптер",
            connectionHubTitle = "Центр подключения OBD / ENET",
            scanHardwareBtn = "Поиск адаптеров (ENET / BLE / USB)",
            adapterDropdownLabel = "Выберите адаптер или симулятор",
            connectVehicleBtn = "Подключиться к авто",
            reconnectBtn = "Переподключить адаптер",
            disconnectBtn = "Отключить авто",
            connectingText = "Подключение и чтение VIN...",
            discoveredDevicesTitle = "Найденные устройства",
            quickDiagnostics = "Диагностика",
            quickCoding = "Кодирование",
            quickLiveService = "Данные и сервис",
            scanAllEcusBtn = "Скан всех ЭБУ",
            scanningText = "Сканирование...",
            expertisePdfBtn = "Отчёт PDF",
            mileageCheckTitle = "Проверка пробега по всем ЭБУ",
            clearAllDtcBtn = "Очистить",
            noDtcInFilter = "Ошибок по выбранному фильтру не найдено.",
            scannedEcusTitle = "Просканированные ЭБУ",
            codingEasyMode = "Простой режим",
            codingExpertCafd = "Эксперт CAFD",
            codingFaVoBackup = "FA/VO и бэкап",
            batterySafeBanner = "АКБ %.1f V (Безопасно) • Автобэкап CAFD перед записью.",
            batteryLockedBanner = "КОДИРОВАНИЕ ЗАБЛОКИРОВАНО: АКБ %.1f V (< 12.2 V)!",
            liveTelemetryTitle = "Живая телеметрия датчиков",
            startBtn = "Старт",
            stopBtn = "Стоп",
            serviceRoutinesTitle = "Сервисные функции (UDS 0x31)",
            runRoutineBtn = "Выполнить",
            serviceHistoryTitle = "Цифровая сервисная книга iDrive",
            writeRecordBtn = "Записать ТО",
            languageTitle = "Язык приложения",
            otaTitle = "Версия и OTA обновления",
            checkUpdatesBtn = "Проверить",
            changelogTitle = "История версий (Changelog)",
            supportedArchTitle = "Поддерживаемые кузова BMW и протоколы",
            packetInspectorTitle = "Инспектор пакетов HSFZ / UDS",
            packetInspectorSub = "TX / RX hex-фреймы в реальном времени",
            clearBtn = "Очистить",
            themeTitle = "Тема оформления",
            savedVehiclesTitle = "История гаража / Сохранённые VIN",
            exportJsonBtn = "Экспорт JSON",
            importJsonBtn = "Импорт JSON",
            ecuResetTitle = "Сброс ЭБУ и спящий режим (UDS 0x11)",
            gatewaySleepBtn = "Спящий режим",
            hardResetBtn = "Сброс Gateway",
            disconnectedDiagTitle = "Диагностика не готова",
            disconnectedDiagSub = "Сначала подключитесь к автомобилю для сканирования ЭБУ и отчёта.",
            disconnectedCodingTitle = "Кодирование в ожидании",
            disconnectedCodingSub = "Сначала подключитесь к авто для простого, CAFD/FDL и FA/VO кодирования.",
            disconnectedLiveTitle = "Данные и сервис в ожидании",
            disconnectedLiveSub = "Сначала подключитесь к авто для живых датчиков, регенерации DPF и прописки АКБ.",
            goToGarageBtn = "Перейти в Гараж и подключение",
            filterAll = "Все",
            filterWarn = "Ошибки",
            filterShadow = "Теневые",
            clearEcuDtcBtn = "Стереть ошибки ЭБУ",
            ecuResetBtn = "Сброс ЭБУ",
            statusClean = "ЧИСТО",
            statusFault = "ОШИБКА",
            searchCodingHint = "Поиск кодировок...",
            searchCafdHint = "Поиск CAFD / функции (BMW_LOGO, MSA)",
            faVoTitle = "Коды комплектации (FA / VO)",
            codeLabel = "Код",
            descLabel = "Описание",
            addVoBtn = "Добавить VO и закодировать",
            autoBackupsTitle = "Автоматические бэкапы кодировок",
            noBackupsHint = "Кодирований пока нет. Перед каждой записью здесь создаётся точка отката.",
            rollbackBtn = "Откатить",
            currentValueLabel = "Текущее значение:",
            perfTimerTitle = "Замер разгона 0–100 км/ч",
            sportGaugeTitle = "M-Sport Приборы и Мощность",
            zfAdaptTitle = "Адаптации сцеплений АКПП ZF8",
            resetBtn = "Сбросить",
            injectorTitle = "IMA-коды форсунок",
            codeBtn = "Записать",
            selectVersionLabel = "Выберите версию",
            language = AppLanguage.RU
        )
    }
}

val LocalAppStrings = compositionLocalOf { AppLocalization.stringsFor(AppLanguage.TR) }
