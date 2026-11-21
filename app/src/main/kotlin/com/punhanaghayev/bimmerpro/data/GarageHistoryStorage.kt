package com.punhanaghayev.bimmerpro.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Base64

@Serializable
data class VehicleScanSnapshot(
    val timestamp: String,
    val odometerKm: Int,
    val iLevel: String,
    val ecuCount: Int,
    val dtcCodes: List<String>,
    val healthScorePercent: Int,
    val svkHash: String = ""
)

data class VehicleInspectionDiff(
    val vin: String,
    val previousTimestamp: String,
    val currentTimestamp: String,
    val kmDelta: Int,
    val newlyAppearedDtcs: List<String>,
    val resolvedDtcs: List<String>,
    val persistentDtcs: List<String>,
    val healthScoreDelta: Int,
    val iLevelChanged: Boolean,
    val previousILevel: String,
    val currentILevel: String,
    val ecuCountDelta: Int
)

data class SavedVehicleRecord(
    val vin: String,
    val modelName: String,
    val chassisCode: String,
    val odometerKm: Int,
    val lastConnected: String,
    val adapterName: String,
    val scanSnapshots: List<VehicleScanSnapshot> = emptyList()
)

class GarageHistoryStorage(private val storageDir: File) {

    private val historyFile = File(storageDir, "garage_vehicles.dat")
    private val json = Json { ignoreUnknownKeys = true }

    fun loadVehicles(): List<SavedVehicleRecord> {
        if (!historyFile.exists()) return emptyList()
        return runCatching {
            historyFile.readLines()
                .filter { it.isNotBlank() }
                .mapNotNull { line ->
                    val p = line.split("|")
                    if (p.size < 6) return@mapNotNull null
                    val snapshots = if (p.size >= 7 && p[6].isNotBlank()) {
                        runCatching { json.decodeFromString<List<VehicleScanSnapshot>>(decode(p[6])) }
                            .getOrDefault(emptyList())
                    } else {
                        emptyList()
                    }
                    SavedVehicleRecord(
                        vin = decode(p[0]),
                        modelName = decode(p[1]),
                        chassisCode = decode(p[2]),
                        odometerKm = p[3].toIntOrNull() ?: 0,
                        lastConnected = decode(p[4]),
                        adapterName = decode(p[5]),
                        scanSnapshots = snapshots
                    )
                }
        }.getOrDefault(emptyList())
    }

    fun upsertVehicle(record: SavedVehicleRecord): List<SavedVehicleRecord> {
        val current = loadVehicles()
        val existing = current.firstOrNull { it.vin == record.vin }
        val mergedRecord = if (record.scanSnapshots.isEmpty() && existing != null) {
            record.copy(scanSnapshots = existing.scanSnapshots)
        } else {
            record
        }
        val updated = (listOf(mergedRecord) + current.filterNot { it.vin == record.vin }).take(25)
        saveVehicles(updated)
        return updated
    }

    fun appendScanSnapshot(
        vin: String,
        modelName: String,
        chassisCode: String,
        odometerKm: Int,
        adapterName: String,
        snapshot: VehicleScanSnapshot
    ): Pair<List<SavedVehicleRecord>, VehicleInspectionDiff?> {
        val existing = loadVehicles().firstOrNull { it.vin == vin }
        val prevSnapshots = existing?.scanSnapshots ?: emptyList()
        val previous = prevSnapshots.firstOrNull()
        val diff = previous?.let { computeDiff(vin, it, snapshot) }
        val updatedSnapshots = (listOf(snapshot) + prevSnapshots).take(20)
        val updatedRecord = SavedVehicleRecord(
            vin = vin,
            modelName = modelName,
            chassisCode = chassisCode,
            odometerKm = odometerKm,
            lastConnected = snapshot.timestamp,
            adapterName = adapterName,
            scanSnapshots = updatedSnapshots
        )
        return upsertVehicle(updatedRecord) to diff
    }

    fun computeLatestDiffForVin(vin: String): VehicleInspectionDiff? {
        val snapshots = loadVehicles().firstOrNull { it.vin == vin }?.scanSnapshots ?: return null
        if (snapshots.size < 2) return null
        return computeDiff(vin, snapshots[1], snapshots[0])
    }

    fun removeVehicle(vin: String): List<SavedVehicleRecord> {
        val updated = loadVehicles().filterNot { it.vin == vin }
        saveVehicles(updated)
        return updated
    }

    private fun saveVehicles(list: List<SavedVehicleRecord>) {
        storageDir.mkdirs()
        val body = list.joinToString("\n") { v ->
            val snapJson = if (v.scanSnapshots.isNotEmpty()) {
                encode(json.encodeToString(v.scanSnapshots))
            } else {
                ""
            }
            listOf(
                encode(v.vin),
                encode(v.modelName),
                encode(v.chassisCode),
                v.odometerKm.toString(),
                encode(v.lastConnected),
                encode(v.adapterName),
                snapJson
            ).joinToString("|")
        }
        historyFile.writeText(body)
    }

    private fun encode(s: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(s.toByteArray(Charsets.UTF_8))

    private fun decode(s: String): String =
        String(Base64.getUrlDecoder().decode(s), Charsets.UTF_8)

    companion object {
        fun computeDiff(
            vin: String,
            previous: VehicleScanSnapshot,
            current: VehicleScanSnapshot
        ): VehicleInspectionDiff {
            val prevSet = previous.dtcCodes.map { it.uppercase() }.toSet()
            val currSet = current.dtcCodes.map { it.uppercase() }.toSet()
            return VehicleInspectionDiff(
                vin = vin,
                previousTimestamp = previous.timestamp,
                currentTimestamp = current.timestamp,
                kmDelta = current.odometerKm - previous.odometerKm,
                newlyAppearedDtcs = (currSet - prevSet).sorted(),
                resolvedDtcs = (prevSet - currSet).sorted(),
                persistentDtcs = (currSet intersect prevSet).sorted(),
                healthScoreDelta = current.healthScorePercent - previous.healthScorePercent,
                iLevelChanged = previous.iLevel != current.iLevel,
                previousILevel = previous.iLevel,
                currentILevel = current.iLevel,
                ecuCountDelta = current.ecuCount - previous.ecuCount
            )
        }
    }
}
