package com.punhanaghayev.bimmerpro.core.report

import android.content.Context
import com.punhanaghayev.bimmerpro.core.bmw.LiveSensorChannel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TelemetryCsvExporter {

    fun exportCsv(
        context: Context,
        chassisCode: String,
        channels: List<LiveSensorChannel>
    ): File {
        val reportsDir = File(context.cacheDir, "reports").apply { mkdirs() }
        val stamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val outFile = File(reportsDir, "BimmerPro_Telemetry_${chassisCode}_$stamp.csv")

        val maxRows = channels.maxOfOrNull { it.history.size } ?: 0
        val sb = StringBuilder()
        sb.append("SampleIndex")
        channels.forEach { ch ->
            sb.append(",${ch.name.replace(",", " ")} (${ch.unit})")
        }
        sb.appendLine()

        for (row in 0 until maxRows) {
            sb.append(row)
            channels.forEach { ch ->
                val value = ch.history.getOrNull(row) ?: ch.currentValue.toFloat()
                sb.append(",$value")
            }
            sb.appendLine()
        }

        outFile.writeText(sb.toString())
        return outFile
    }
}
