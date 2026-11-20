package com.punhanaghayev.bimmerpro.core.update

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

@Serializable
data class DataPackMeta(
    val packVersion: String,
    val entryCount: Int,
    val updatedAt: String
)

sealed interface DataPackStatus {
    data object NotInstalled : DataPackStatus
    data object Downloading : DataPackStatus
    data class Applied(
        val packVersion: String,
        val addedCount: Int,
        val totalCount: Int,
        val updatedAt: String
    ) : DataPackStatus

    data class UpToDate(
        val packVersion: String,
        val totalCount: Int,
        val updatedAt: String
    ) : DataPackStatus

    data class Error(val message: String) : DataPackStatus
}

/**
 * Downloads and caches modular BMW DTC, SALAPA (FA/VO), and CAFD Coding JSON data packs from GitHub/CDN
 * so the APK remains lightweight while supporting thousands of authentic codes.
 */
object DataPackDownloader {

    private const val DTC_PACK_URL =
        "https://raw.githubusercontent.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}/main/datapacks/bmw_dtc_pack.json"

    private const val SALAPA_PACK_URL =
        "https://raw.githubusercontent.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}/main/datapacks/bmw_salapa_pack.json"

    private const val CODING_PACK_URL =
        "https://raw.githubusercontent.com/${OtaUpdateManager.GITHUB_OWNER}/${OtaUpdateManager.GITHUB_REPO}/main/datapacks/bmw_coding_pack.json"

    private val json = Json { ignoreUnknownKeys = true }

    fun loadCachedDtcPack(filesDir: File): ExternalDtcPackDto? {
        val file = File(File(filesDir, "datapacks"), "bmw_dtc_pack.json")
        if (!file.exists()) return null
        return runCatching { json.decodeFromString<ExternalDtcPackDto>(file.readText()) }.getOrNull()
    }

    fun loadCachedMeta(filesDir: File): DataPackMeta? {
        val metaFile = File(File(filesDir, "datapacks"), "pack_meta.json")
        if (!metaFile.exists()) return null
        return runCatching { json.decodeFromString<DataPackMeta>(metaFile.readText()) }.getOrNull()
    }

    fun saveCachedMeta(filesDir: File, packVersion: String, totalCount: Int): DataPackMeta {
        val stamp = SimpleDateFormat("dd.MM.yyyy", Locale.US).format(Date())
        val meta = DataPackMeta(packVersion = packVersion, entryCount = totalCount, updatedAt = stamp)
        val dir = File(filesDir, "datapacks").apply { mkdirs() }
        File(dir, "pack_meta.json").writeText(json.encodeToString(meta))
        return meta
    }

    suspend fun downloadAndCacheDtcPack(filesDir: File): ExternalDtcPackDto = withContext(Dispatchers.IO) {
        val conn = (URL(DTC_PACK_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 12000
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

    suspend fun downloadOptionalPacks(filesDir: File) = withContext(Dispatchers.IO) {
        val dir = File(filesDir, "datapacks").apply { mkdirs() }
        for ((urlStr, fileName) in listOf(SALAPA_PACK_URL to "bmw_salapa_pack.json", CODING_PACK_URL to "bmw_coding_pack.json")) {
            runCatching {
                val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 8000
                    readTimeout = 15000
                    setRequestProperty("User-Agent", "BMWExpertPRO-DataPack")
                }
                if (conn.responseCode in 200..299) {
                    val content = conn.inputStream.bufferedReader().use { it.readText() }
                    File(dir, fileName).writeText(content)
                }
                conn.disconnect()
            }
        }
    }
}
