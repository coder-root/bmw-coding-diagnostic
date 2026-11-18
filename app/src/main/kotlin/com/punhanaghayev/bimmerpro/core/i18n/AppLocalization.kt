package com.punhanaghayev.bimmerpro.core.i18n

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

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
    val clearBtn: String
)

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
            heroSubtitle = "Tam BMW Kodlama, Diaqnostika və Ekspertiza",
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
            clearBtn = "Təmizlə"
        )

        AppLanguage.TR -> AppStrings(
            tabGarage = "Garaj",
            tabDiagnostics = "Diagnostik",
            tabCoding = "Kodlama",
            tabLiveService = "Canlı/Servis",
            tabDeveloper = "Developer",
            heroSubtitle = "Full BMW Kodlama, Diagnostik & Ekspertiz",
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
            clearBtn = "Temizle"
        )

        AppLanguage.EN -> AppStrings(
            tabGarage = "Garage",
            tabDiagnostics = "Diagnostics",
            tabCoding = "Coding",
            tabLiveService = "Live/Service",
            tabDeveloper = "Developer",
            heroSubtitle = "Full BMW Coding, Diagnostics & Inspection",
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
            clearBtn = "Clear"
        )

        AppLanguage.RU -> AppStrings(
            tabGarage = "Гараж",
            tabDiagnostics = "Диагностика",
            tabCoding = "Кодирование",
            tabLiveService = "Данные/Сервис",
            tabDeveloper = "Разработчик",
            heroSubtitle = "Полное кодирование, диагностика и проверка BMW",
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
            clearBtn = "Очистить"
        )
    }
}

val LocalAppStrings = compositionLocalOf { AppLocalization.stringsFor(AppLanguage.TR) }
