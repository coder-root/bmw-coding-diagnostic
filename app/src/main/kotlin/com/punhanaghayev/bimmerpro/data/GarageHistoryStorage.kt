package com.punhanaghayev.bimmerpro.data

import java.io.File
import java.util.Base64

data class SavedVehicleRecord(
    val vin: String,
    val modelName: String,
    val chassisCode: String,
    val odometerKm: Int,
    val lastConnected: String,
    val adapterName: String
)

class GarageHistoryStorage(private val storageDir: File) {

    private val historyFile = File(storageDir, "garage_vehicles.dat")

    fun loadVehicles(): List<SavedVehicleRecord> {
        if (!historyFile.exists()) return emptyList()
        return runCatching {
            historyFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val p = line.split("|")
                    if (p.size < 6) return@mapNotNull null
                    SavedVehicleRecord(
                        vin = decode(p[0]),
                        modelName = decode(p[1]),
                        chassisCode = decode(p[2]),
                        odometerKm = p[3].toIntOrNull() ?: 0,
                        lastConnected = decode(p[4]),
                        adapterName = decode(p[5])
                    )
                }
        }.getOrDefault(emptyList())
    }

    fun upsertVehicle(record: SavedVehicleRecord): List<SavedVehicleRecord> {
        val updated = (listOf(record) + loadVehicles().filterNot { it.vin == record.vin }).take(25)
        saveVehicles(updated)
        return updated
    }

    fun removeVehicle(vin: String): List<SavedVehicleRecord> {
        val updated = loadVehicles().filterNot { it.vin == vin }
        saveVehicles(updated)
        return updated
    }

    private fun saveVehicles(list: List<SavedVehicleRecord>) {
        storageDir.mkdirs()
        val body = list.joinToString("\n") { v ->
            listOf(
                encode(v.vin),
                encode(v.modelName),
                encode(v.chassisCode),
                v.odometerKm.toString(),
                encode(v.lastConnected),
                encode(v.adapterName)
            ).joinToString("|")
        }
        historyFile.writeText(body)
    }

    private fun encode(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray(Charsets.UTF_8))

    private fun decode(s: String): String =
        String(Base64.getUrlDecoder().decode(s), Charsets.UTF_8)
}
