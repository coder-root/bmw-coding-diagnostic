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
            tagName = "v1.3.0",
            versionName = "1.3.0",
            title = "BMW Expert PRO v1.3.0 — Çok Dilli Destek & UI Yenilemesi",
            changelog = """
                • Çok dilli destek eklendi: Azərbaycan, Türkçe, English, Русский
                • Bağlantı ve tarama butonları tek satır ikonlu tasarımla yenilendi
                • Desteklenen BMW Mimarisi kartı alt alta okunaklı formata geçirildi
                • Developer kartında Punhan Aghayev, Lead Architect / BMW Diagnostics Developer & cwghost@hotmail.com güncellendi
            """.trimIndent(),
            publishedDate = "2026-11-18",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.2.0",
            versionName = "1.2.0",
            title = "BMW Expert PRO v1.2.0 — Vgate vLinker BM Wi-Fi & Yeni Bağlantı",
            changelog = """
                • Uygulama adı ve proje başlığı "BMW Expert PRO" olarak güncellendi
                • Vgate vLinker BM Wi-Fi (STN2255 / 192.168.0.10:35000) tam protokol desteği eklendi
                • Bağlantı sayfası Dropdown (açılır liste) seçici ve adaptör kartları ile yenilendi
            """.trimIndent(),
            publishedDate = "2026-11-18",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.1.0",
            versionName = "1.1.0",
            title = "BimmerPro v1.1.0 — iDrive Servis Geçmişi & F30/G20",
            changelog = """
                • iDrive Dijital Servis Geçmişi (Service History) yazma sihirbazı
                • BMW F30 328i (N20) ve G20 M340i (B58) simülatörleri eklendi
                • Yeni Easy Mode kodlamalar: 5x Sinyal, Geri Ayna Açısı, Spor Göstergeler
                • OTA Güncelleme & GitHub Releases Changelog takibi
            """.trimIndent(),
            publishedDate = "2026-11-18",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        ),
        AppReleaseInfo(
            tagName = "v1.0.0",
            versionName = "1.0.0",
            title = "BimmerPro İlk Sürüm (Sprint 1–3)",
            changelog = """
                • HSFZ / ENET (TCP 6801) ve UDS ISO 14229 protokol motoru
                • F10 520d, G30 530i ve E90 335i sanal ECU simülatörleri
                • BLE OBD-II (OBDLink/UniCarScan) ve USB K+DCAN sürücü katmanı
                • ENET UDP 6811 otomatik Gateway keşfi
                • Easy Mode, Expert CAFD/FDL bitmask editörü ve FA/VO kodlama
                • 12.2V akü güvenlik kilidi, kalıcı yedekleme ve tek tıkla geri alma
                • Çoklu modül KM doğrulama + A4 PDF Ekspertiz Raporu & CSV telemetri
                • Foreground Coding Service & Developer: Punhan Aghayev
            """.trimIndent(),
            publishedDate = "2026-11-18",
            apkDownloadUrl = null,
            apkSizeBytes = 0L
        )
    )

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
                    publishedDate = dto.publishedAt.take(10).ifBlank { "—" },
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
            setRequestProperty("User-Agent", "BimmerPro-Android-OTA")
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
        val outFile = File(updatesDir, "BimmerPro-$version.apk")
        val conn = (URL(downloadUrl).openConnection() as HttpURLConnection).apply {
            instanceFollowRedirects = true
            connectTimeout = 8000
            readTimeout = 15000
            setRequestProperty("User-Agent", "BimmerPro-Android-OTA")
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
