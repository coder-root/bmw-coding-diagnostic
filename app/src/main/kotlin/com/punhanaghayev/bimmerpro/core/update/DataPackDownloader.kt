package com.punhanaghayev.bimmerpro.core.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

@Serializable
data class ExternalDtcDto(
    val code: String,
    val title: String,
    val detail: String,
    val severity: String = "WARNING",
    val guidance: String = ""
)

@Serializable
data class ExternalDtcPackDto(
    val packVersion: String = "1.0",
    val entries: List<ExternalDtcDto> = emptyList()
)

/**
 * Downloads and caches modular BMW DTC & CAFD JSON data packs from GitHub/CDN
 * so the APK remains lightweight (~1.5 MB) while supporting thousands of codes.
 */
object DataPackDownloader {

    private const val DTC_PACK_URL =
        "https://raw.githubusercontent.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}/main/datapacks/bmw_dtc_pack.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun loadCachedDtcPack(filesDir: File): ExternalDtcPackDto? {
        val file = File(File(filesDir, "datapacks"), "bmw_dtc_pack.json")
        if (!file.exists()) return null
        return runCatching { json.decodeFromString<ExternalDtcPackDto>(file.readText()) }.getOrNull()
    }

    suspend fun downloadAndCacheDtcPack(filesDir: File): ExternalDtcPackDto = withContext(Dispatchers.IO) {
        val conn = (URL(DTC_PACK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 6000
            readTimeout = 8000
            setRequestProperty("User-Agent", "BMWExpertPRO-DataPack")
        }
        try {
            val code = conn.responseCode
            if (code !in 200..299) error("DataPack HTTP $code")
            val raw = conn.inputStream.bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString<ExternalDtcPackDto>(raw)
            val dir = File(filesDir, "datapacks").apply { mkdirs() }
            File(dir, "bmw_dtc_pack.json").writeText(raw)
            parsed
        } finally {
            conn.disconnect()
        }
    }
}
