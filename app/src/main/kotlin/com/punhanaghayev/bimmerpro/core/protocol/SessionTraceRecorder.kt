package com.punhanaghayev.bimmerpro.core.protocol

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports raw diagnostic/coding TX/RX traces (HSFZ, UDS, AT) for offline replay and F10 protocol verification.
 */
object SessionTraceRecorder {

    fun exportTraceFile(
        cacheDir: File,
        vin: String,
        adapterName: String,
        logs: List<ProtocolLogEntry>
    ): File {
        val tracesDir = File(cacheDir, "traces").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(tracesDir, "BMW_Trace_${vin.takeLast(7)}_$stamp.txt")
        val headerDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.US).format(Date())

        val sb = StringBuilder()
        sb.appendLine("# BMW Expert PRO Raw Diagnostic Trace")
        sb.appendLine("# Date: $headerDate")
        sb.appendLine("# Adapter: $adapterName")
        sb.appendLine("# VIN: $vin")
        sb.appendLine("# Entries: ${logs.size}")
        sb.appendLine("------------------------------------------------------------")
        logs.reversed().forEach { entry ->
            val errFlag = if (entry.isError) " [ERR]" else ""
            sb.appendLine("${entry.timestamp} [${entry.direction}]$errFlag ECU=0x%02X | ${entry.summary}".format(entry.ecuAddress))
            if (entry.rawHex.isNotBlank()) {
                sb.appendLine("  HEX: ${entry.rawHex}")
            }
        }
        outFile.writeText(sb.toString())
        return outFile
    }
}
