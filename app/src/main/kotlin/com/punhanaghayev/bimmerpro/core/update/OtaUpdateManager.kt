package com.punhanaghayev.bimmerpro.core.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

@Serializable
data class GitHubAssetDto(
    val name: String,
    @SerialName("browser_download_url") val browserDownloadUrl: String,
    val size: Long = 0L
)

@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    val name: String = "",
    val body: String = "",
    @SerialName("published_at") val publishedAt: String = "",
    val draft: Boolean = false,
    val prerelease: Boolean = false,
    val assets: List<GitHubAssetDto> = emptyList()
)

data class AppReleaseInfo(
    val tagName: String,
    val versionName: String,
    val title: String,
    val changelog: String,
    val publishedDate: String,
    val apkDownloadUrl: String?,
    val apkSizeBytes: Long
)

sealed interface OtaCheckState {
    data object Idle : OtaCheckState
    data object Checking : OtaCheckState
    data class UpToDate(val currentVersion: String, val releases: List<AppReleaseInfo>) : OtaCheckState
    data class UpdateAvailable(val latest: AppReleaseInfo, val releases: List<AppReleaseInfo>) : OtaCheckState
    data class Downloading(val progress: Float, val version: String) : OtaCheckState
    data class ReadyToInstall(val apkFile: File, val version: String) : OtaCheckState
    data class Error(val message: String) : OtaCheckState
}

object SemVerComparator {
    fun normalize(version: String): List<Int> =
        version.trim()
            .removePrefix("v")
            .removePrefix("V")
            .substringBefore("-")
            .split(".")
            .map { it.toIntOrNull() ?: 0 }

    fun isNewer(remoteVersion: String, currentVersion: String): Boolean {
        val remote = normalize(remoteVersion)
        val current = normalize(currentVersion)
        val maxLen = maxOf(remote.size, current.size)
        for (i in 0 until maxLen) {
            val r = remote.getOrElse(i) { 0 }
            val c = current.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}

object OtaUpdateManager {

    const val GITHUB_OWNER = "coder-root"
    const val GITHUB_REPO = "bmw-coding-diagnostic"
    private const val RELEASES_API = "https://api.github.com/repos/$GITHUB_OWNER/$GITHUB_REPO/releases"

    private val json = Json { ignoreUnknownKeys = true }

    val builtinChangelog: List<AppReleaseInfo> = listOf(
        AppReleaseInfo(
            tagName = "v1.4.0",
            versionName = "1.4.0",
            title = "BMW Expert PRO v1.4.0 — DoIP (ISO 13400), UDS 0x27 Security Access, DPF & ZF8 Derinliği, 20+ CAFD & Ekspertiz Raporu",
            changelog = """
                • ISO 13400 DoIP (Diagnostic over IP) Protokol Katmanı (Port 13400 TCP/UDP, 0x0E00/0x0E80 Yönlendirme Aktivasyonu)
                • UDS 0x27 Security Access (Seed & Key Motoru): CAS4, FEM/BDC ve DME/DDE NRC 0x33 otomatik kilit açma
                • Canlı DPF Monitörü: Kurum/kül kütlesi, egzoz geri basıncı, sıcaklıklar, son rejenerasyon ve anlık yakma tetikleme
                • ZF 8HP / 6HP Şanzıman Aşınma Analizi: A-E kavramaları dolum basınç/süre sapmaları, yağ sıcaklığı ve kondisyon puanı
                • BimmerCode Easy Mode Zenginleştirmesi: 2TB Sport Otomatik & Launch Control, Özel Ambiyans Aydınlatma, Akustik Kilit Onayı (DWA), RDC Lastik Sıcaklığı, Acil Fren Çakar Flaşör
                • Profesyonel Ekspertiz & Sağlık Raporu: Çok sayfalı yayın kalitesinde A4 PDF ve HTML formatında dışa aktarma / paylaşma
            """.trimIndent(),
            publishedDate = "21.11.2026",
            apkDownloadUrl = "https://github.com/coder-root/bmw-coding-diagnostic/releases/download/v1.4.0/BMW_Expert_PRO_1.4.0.apk",
            apkSizeBytes = 1750000L
        ),
        AppReleaseInfo(
            tagName = "v1.3.0",
            versionName = "1.3.0",
            title = "BMW Expert PRO v1.3.0 — Ayrı Canlı/Servis Ekranları, Toplu Kodlama Güvenliği ve F10 528i Simülasyonu",
            changelog = """
                • Canlı Veri (Live) ve Servis & Bakım (Service) bağımsız iki ekrana ve sekmesine ayrıldı
                • Sensör özelleştirme diyaloğundaki kapanma sorunu çözüldü
                • Çoklu parametre seçimi ve tek tıkla toplu kodlama (Batch Coding) mimarisi
                • Kodlama öncesi akü voltaj kontrolü (>=12.2V) ve onay kutusu içeren güvenlik onay mekanizması
                • BMW F10 528i (N20B20 Benzinli DME MEVD1724) simülatör profili ve benzinli telemetri PID'leri
                • Çevrimdışı arıza analizinde arıza kontrolü durum yazıları modül başlığı altına taşındı
                • Cross-ECU km doğrulama filtreleri kompakt dropdown içine toplandı, Clear All butonu düzenlendi
                • Tüm diller (EN, TR, AZ, RU) için tam yerelleştirme ve UTF-8 karakter kodlama garantisi
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = "https://github.com/coder-root/bmw-coding-diagnostic/releases/download/v1.3.0/BMW_Expert_PRO_1.3.0.apk",
            apkSizeBytes = 1652153L
        ),
        AppReleaseInfo(
            tagName = "v1.2.0",
            versionName = "1.2.0",
            title = "BMW Expert PRO v1.2.0 — BimmerCode & E-Sys CAFD Entegrasyonu ve SALAPA Veritabanı",
            changelog = """
                • Orijinal BimmerCode ve E-Sys veritabanından ayıklanan gerçek CAFD & FDL parametre haritası
                • FEM, CAS4, REM, KOMBI, HU_NBT, IHKA ve HKFM kontrol üniteleri için tam Easy Mode ve Expert Mode desteği
                • LED ampul dönüşümü & sıcak/soğuk voltaj kontrolü (arıza lambası söndürme) parametreleri
                • Otomatik ayna katlama, Angel Eyes parlaklığı, karşılama ışıkları ve Video in Motion menüleri
                • 5.640 adet BMW fabrika donanım ve retrofit opsiyonunu içeren çevrimdışı SALAPA (FA/VO) kataloğu
                • 616 adet BMW kontrol ünitesi (ECU) ve I-Level tanımlama veritabanı
                • Gerçek araçlardan kaydedilen 499 adet HSFZ UDS paket trace'i ile çevrimdışı simülasyon ve replay motoru
                • FA/VO kodlamada opsiyon kodu girildiğinde otomatik fabrika açıklaması tamamlama
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.1.1",
            versionName = "1.1.1",
            title = "BMW Expert PRO v1.1.1 — İlerleme HUD, Garage ve UI Optimizasyonları",
            changelog = """
                • Kodlama ve Diagnostik gerçek zamanlı ilerleme/bekleme HUD diyaloğu (adım adım ECU ve UDS gösterimi)
                • Garage ekranında araç teknik bilgileri açılır/kapanır (Show/Hide) kartı
                • Akü voltajı göstergesi VIN altına taşındı, model başlığı tam genişliğe kavuştu
                • Diagnostics ve Coding seçim butonları netleştirildi, alt açıklamalar kaldırıldı
                • Expert CAFD kartlarında Offset ve Mask bilgisi CAFD dosyası altına taşındı
                • Supported BMW başlığı ve modeller listesi hiyerarşisi yeniden düzenlendi
                • Sürüm geçmişi ve DTC paketi tarih gösteriminden saat kaldırıldı (sadece dd.MM.yyyy)
                • Offline DTC Veri Paketi indirme butonu tam genişliğe alındı ve durum rozeti tek satır yapıldı
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.1.0",
            versionName = "1.1.0",
            title = "BMW Expert PRO v1.1.0 — Modüler i18n, Oturum Seçimi, Read-Verify & Replay",
            changelog = """
                • Bağlantı öncesi Diagnostik/Kodlama oturum seçimi; bağlantısız gizli menüler ve ikon düzeni
                • Modüler dil dosyaları (EN varsayılan/TR/AZ/RU), ilk açılış dil seçimi ve otomatik OTA güncelleme uyarısı
                • Easy Mode & Expert CAFD modül bazlı gruplama, kategori dropdown'u ve kompakt arama kutuları
                • Canlı/Servis özelleştirilebilir modül/sensör seçimi ve sürükle-bırak kart sıralama
                • Ayrı Paket İnceleyici ve Kayıtlı Araçlar ekranları + Kırmızı, Beyaz, Siyah temalar
                • Gerçek Gateway I-Level (0xF150), CAFD Read-Modify-Write-Verify, STN adaptif zamanlama ve DTC DataPack durumu
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.4",
            versionName = "1.0.4",
            title = "BMW Expert PRO v1.0.4 — Fiziksel Güvenlik, ISO-TP, 28 ECU & Veri Paketi",
            changelog = """
                • Fiziksel Bağlantıda Salt-Okunur Güvenli Mod (Read-Only Safe Mode) ve ilk açılış yasal sorumluluk onayı
                • Tam 28 ECU BMW veri yolu haritası & 20 adet UDS 0x22 / OBD canlı sensör PID tanımı
                • IsoTpCodec (SF/FF/CF/FC çok çerçeveli CAN), UDS 0x78 bekleme ve TesterPresent (0x3E) keep-alive
                • Kodlama öncesi Önce -> Sonra fark (Diff) önizleme penceresi
                • Çevrimdışı Hata Analiz Motoru (alt sistem sağlık puanları + kestirimci bakım önerileri)
                • On-demand indirilebilir DTC Veri Paketi (DataPackDownloader) & .txt ham trace dışa aktarma
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.3",
            versionName = "1.0.3",
            title = "BMW Expert PRO v1.0.3 — Tam NCD Snapshot, KWP2000 & VO Hesaplayıcı",
            changelog = """
                • Tam ECU NCD Snapshot alma (SHA-256 doğrulamalı) ve tek tıkla geri yükleme
                • VoDependencyMapper: FA/VO opsiyon eklemede etkilenecek tüm ECU'ları otomatik hesaplama ve kodlama
                • Kwp2000Codec: E-Serisi (E60/E90/E70) KWP2000 hata & yerel veri çözümleyici
            """.trimIndent(),
            publishedDate = "20.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.2",
            versionName = "1.0.2",
            title = "BMW Expert PRO v1.0.2 — Vehicle Information, Equipment & Trouble Code",
            changelog = """
                • BimmerCode tarzı Vehicle Information kartı (Factory/Current I-Level, Tip Kodu, Üretim Tarihi, Renk, Döşeme)
                • Vehicle Equipment (FA/VO donanım listesi) görüntüleme ve tek dokunuşla kodlama
                • BimmerTool tarzı Trouble Code Sözlüğü & Anlık Hex DTC Arama
                • GitHub Release APK dosyaları "BMW Expert PRO <sürüm>.apk" formatında yayınlandı
            """.trimIndent(),
            publishedDate = "19.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.1",
            versionName = "1.0.1",
            title = "BMW Expert PRO v1.0.1 — Tam BimmerCode/BimmerLink Kataloğu & 0x2F Testler",
            changelog = """
                • BmwCodingCatalog: 29 adet BimmerCode Easy Mode kodlaması + 13 adet Expert CAFD/FDL byte/bitmask tanımı
                • BmwDtcDictionary: 22+ yaygın BMW Hex DTC arıza kodu ve ISTA test planı
                • UDS 0x2F Aktüatör & Parça Testleri (Radyatör Fanı, Yakıt Pompası, Egzoz Flapı, İbre Sweep, Angel Eyes)
                • IBS Akü Sağlığı (SOC/SOH) ve son 5 günlük akü grafiği
            """.trimIndent(),
            publishedDate = "19.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.0",
            versionName = "1.0.0",
            title = "BMW Expert PRO v1.0.0 — İlk Resmi Sürüm",
            changelog = """
                • Vgate vLinker BM Wi-Fi (Wi-Fi ağ kilidi + 35000/23 port), ENET HSFZ/DoIP, BLE OBD ve USB K+DCAN desteği
                • BimmerCode tarzı Connect / Simulator giriş ekranı (F10, F30, G30, G20, E90)
                • Tam Diagnostik, Çoklu Modül KM Doğrulama, BmwDtcDictionary ve A4 PDF Ekspertiz Raporu
                • Easy Mode, Expert CAFD/FDL bitmask editörü, FA/VO, JSON Yedek Dışa/İçe Aktarma
                • Canlı Telemetri, M-Sport Kadran, 0-100 km/h Ölçer, ZF8 Adaptasyon, Enjektör IMA & iDrive Servis Geçmişi
                • 4 Dil (AZ / TR / EN / RU), 5 Tema ve GitHub Releases OTA Güncelleme
            """.trimIndent(),
            publishedDate = "19.11.2026",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        )
    )

    internal fun formatPublishedDate(isoString: String): String {
        if (isoString.isBlank()) return "—"
        return runCatching {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val parsed = parser.parse(isoString) ?: return@runCatching isoString.take(10)
            SimpleDateFormat("dd.MM.yyyy", Locale.US).format(parsed)
        }.getOrElse {
            // Fallback for YYYY-MM-DD
            val parts = isoString.take(10).split("-")
            if (parts.size == 3) "${parts[2]}.${parts[1]}.${parts[0]}" else isoString.take(10)
        }
    }

    internal fun parseReleases(rawJson: String): List<AppReleaseInfo> {
        val dtos = json.decodeFromString<List<GitHubReleaseDto>>(rawJson)
        return dtos
            .filter { !it.draft }
            .map { dto ->
                val apkAsset = dto.assets.firstOrNull { it.name.endsWith(".apk", ignoreCase = true) }
                AppReleaseInfo(
                    tagName = dto.tagName,
                    versionName = dto.tagName.removePrefix("v").removePrefix("V"),
                    title = dto.name.ifBlank { dto.tagName },
                    changelog = dto.body.ifBlank { "Sürüm notu girilmemiş." },
                    publishedDate = formatPublishedDate(dto.publishedAt),
                    apkDownloadUrl = apkAsset?.browserDownloadUrl,
                    apkSizeBytes = apkAsset?.size ?: 0L
                )
            }
    }

    suspend fun fetchReleases(): List<AppReleaseInfo> = withContext(Dispatchers.IO) {
        val conn = (URL(RELEASES_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 5000
            readTimeout = 5000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "BMWExpertPRO-Android-OTA")
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) {
                error("GitHub API HTTP $code")
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            parseReleases(body)
        } finally {
            conn.disconnect()
        }
    }

    suspend fun downloadApk(
        downloadUrl: String,
        cacheDir: File,
        version: String,
        onProgress: (Float) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updatesDir = File(cacheDir, "updates").apply { mkdirs() }
        val outFile = File(updatesDir, "BMWExpertPRO-$version.apk")
        val conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 8000
            readTimeout = 15000
            setRequestProperty("User-Agent", "BMWExpertPRO-Android-OTA")
        }
        try {
            val total = conn.contentLengthLong.coerceAtLeast(1L)
            var downloaded = 0L
            conn.inputStream.use { input ->
                FileOutputStream(outFile).use { output ->
                    val buffer = ByteArray(8192)
                    while (true) {
                        val read = input.read(buffer)
                        if (read == -1) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        onProgress((downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f))
                    }
                }
            }
            outFile
        } finally {
            conn.disconnect()
        }
    }
}
