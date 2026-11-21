package com.punhanaghayev.bimmerpro.core.report

import com.punhanaghayev.bimmerpro.BuildConfig
import com.punhanaghayev.bimmerpro.core.protocol.ProtocolLogEntry
import com.punhanaghayev.bimmerpro.data.BimmerSessionState
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a single-click diagnostic support bundle (vehicle identity, ECU SVT table,
 * 3-layer DTCs, and recent UDS/AT protocol traces) for troubleshooting.
 */
object SupportBundleExporter {

    fun exportBundle(
        cacheDir: File,
        session: BimmerSessionState,
        logs: List<ProtocolLogEntry>
    ): File {
        val dir = File(cacheDir, "support").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val vinSuffix = session.vehicleProfile?.vin?.takeLast(7) ?: "NO_VIN"
        val out = File(dir, "BMW_Support_Bundle_${vinSuffix}_$stamp.txt")
        val now = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US).format(Date())

        val sb = StringBuilder()
        sb.appendLine("=== BMW Expert PRO Support & Diagnostic Bundle ===")
        sb.appendLine("Timestamp: $now")
        sb.appendLine("App Version: ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})")
        sb.appendLine("Adapter: ${session.selectedAdapter.displayName} (${session.selectedAdapter.transportKind})")
        sb.appendLine("Connection State: ${session.connectionState}")
        sb.appendLine("Safe Mode: ${session.isPhysicalSafeMode}, Write Unlocked: ${session.isPhysicalWriteUnlocked}")
        sb.appendLine()

        session.vehicleProfile?.let { p ->
            sb.appendLine("--- Vehicle Profile ---")
            sb.appendLine("Model: ${p.modelDisplayName} (${p.chassis.code} / ${p.chassis.family.label})")
            sb.appendLine("VIN: ${p.vin}")
            sb.appendLine("Engine: ${p.engineCode}")
            sb.appendLine("I-Level: ${p.iLevel} (Factory: ${p.factoryILevel})")
            sb.appendLine("Battery: ${"%.2f".format(p.batteryVoltage)} V | Odometer: ${p.odometerKm} km")
            sb.appendLine()
        }

        sb.appendLine("--- ECU SVT Tree (${session.ecus.size}) ---")
        session.ecus.forEach { ecu ->
            sb.appendLine(
                "${ecu.addressHex} ${ecu.shortName} | HW=${ecu.hardwareVersion} | SW=${ecu.softwareVersion} | " +
                    "CAFD=${ecu.cafdId ?: "-"} | Active=${ecu.activeDtcCount} Shadow=${ecu.shadowDtcCount}"
            )
        }
        sb.appendLine()

        sb.appendLine("--- DTC Fault Memories (${session.dtcs.size}) ---")
        session.dtcs.forEach { dtc ->
            sb.appendLine(
                "[${dtc.memoryType.name}] ${dtc.code} (${dtc.ecuName}) | ${dtc.severity} | ${dtc.status} | ${dtc.title}"
            )
        }
        sb.appendLine()

        sb.appendLine("--- Recent Protocol Logs (${logs.size}) ---")
        logs.reversed().forEach { entry ->
            val err = if (entry.isError) " [ERR]" else ""
            val ecuHex = "0x%02X".format(entry.ecuAddress)
            sb.appendLine("${entry.timestamp} [${entry.direction}]$err $ecuHex | ${entry.summary}")
            if (entry.rawHex.isNotBlank()) sb.appendLine("  HEX: ${entry.rawHex}")
        }

        out.writeText(sb.toString())
        return out
    }
}
