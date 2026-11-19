package com.punhanaghayev.bimmerpro.data

import com.punhanaghayev.bimmerpro.core.bmw.CodingBackupSnapshot
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

@Serializable
private data class BackupJsonDto(
    val id: String,
    val timestamp: String,
    val ecuName: String,
    val ecuAddress: Int,
    val cafdFile: String,
    val summary: String,
    val previousValues: Map<String, Int>
)

class BackupStorage(private val storageDir: File) {

    private val backupFile = File(storageDir, "coding_backups.dat")
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    fun exportToJson(backups: List<CodingBackupSnapshot>): String {
        val dtos = backups.map {
            BackupJsonDto(it.id, it.timestamp, it.ecuName, it.ecuAddress, it.cafdFile, it.summary, it.previousValues)
        }
        return json.encodeToString(dtos)
    }

    fun importFromJson(rawJson: String): List<CodingBackupSnapshot> {
        val dtos = json.decodeFromString<List<BackupJsonDto>>(rawJson)
        return dtos.map {
            CodingBackupSnapshot(it.id, it.timestamp, it.ecuName, it.ecuAddress, it.cafdFile, it.summary, it.previousValues)
        }
    }

    fun loadBackups(): List<CodingBackupSnapshot> {
        if (!backupFile.exists()) return emptyList()
        return runCatching {
            backupFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { decodeLine(it) }
        }.getOrDefault(emptyList())
    }

    fun saveBackups(backups: List<CodingBackupSnapshot>) {
        storageDir.mkdirs()
        val content = backups.take(50).joinToString("\n") { encodeLine(it) }
        backupFile.writeText(content)
    }

    private fun encodeLine(b: CodingBackupSnapshot): String {
        val prevEncoded = b.previousValues.entries.joinToString(",") { (k, v) ->
            "${encodeB64(k)}=$v"
        }
        return listOf(
            encodeB64(b.id),
            encodeB64(b.timestamp),
            encodeB64(b.ecuName),
            b.ecuAddress.toString(),
            encodeB64(b.cafdFile),
            encodeB64(b.summary),
            prevEncoded
        ).joinToString("|")
    }

    private fun decodeLine(line: String): CodingBackupSnapshot? {
        val parts = line.split("|")
        if (parts.size < 7) return null
        val prevMap = if (parts[6].isBlank()) {
            emptyMap()
        } else {
            parts[6].split(",").mapNotNull { entry ->
                val kv = entry.split("=")
                if (kv.size == 2) decodeB64(kv[0]) to (kv[1].toIntOrNull() ?: 0) else null
            }.toMap()
        }
        return CodingBackupSnapshot(
            id = decodeB64(parts[0]),
            timestamp = decodeB64(parts[1]),
            ecuName = decodeB64(parts[2]),
            ecuAddress = parts[3].toIntOrNull() ?: return null,
            cafdFile = decodeB64(parts[4]),
            summary = decodeB64(parts[5]),
            previousValues = prevMap
        )
    }

    private fun encodeB64(value: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(value.toByteArray(Charsets.UTF_8))

    private fun decodeB64(encoded: String): String =
        String(Base64.getUrlDecoder().decode(encoded), Charsets.UTF_8)
}
