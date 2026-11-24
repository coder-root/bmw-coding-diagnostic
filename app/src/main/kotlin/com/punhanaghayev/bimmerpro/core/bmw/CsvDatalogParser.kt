package com.punhanaghayev.bimmerpro.core.bmw

/**
 * Parses exported BMW Expert PRO telemetry CSV files into multi-channel time-series data
 * for interactive oscilloscope playback and cursor inspection.
 */
data class DatalogChannelSeries(
    val name: String,
    val values: List<Double>,
    val minValue: Double,
    val maxValue: Double
)

data class DatalogReplaySession(
    val timestamps: List<String>,
    val channels: List<DatalogChannelSeries>
) {
    val frameCount: Int get() = timestamps.size

    fun valueAt(channelIndex: Int, frameIndex: Int): Double? =
        channels.getOrNull(channelIndex)?.values?.getOrNull(frameIndex)
}

object CsvDatalogParser {

    fun parse(csvContent: String): DatalogReplaySession {
        val lines = csvContent
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .toList()
        if (lines.size < 2) return DatalogReplaySession(emptyList(), emptyList())

        val delimiter = if (lines.first().count { it == ';' } > lines.first().count { it == ',' }) ';' else ','
        val headers = lines.first().split(delimiter).map { it.trim().removeSurrounding("\"") }
        if (headers.size < 2) return DatalogReplaySession(emptyList(), emptyList())

        val channelNames = headers.drop(1)
        val timestamps = mutableListOf<String>()
        val columns = List(channelNames.size) { mutableListOf<Double>() }

        for (lineIdx in 1 until lines.size) {
            val parts = lines[lineIdx].split(delimiter).map { it.trim().removeSurrounding("\"") }
            if (parts.size < 2) continue
            timestamps.add(parts[0])
            for (c in channelNames.indices) {
                val raw = parts.getOrNull(c + 1)?.replace(',', '.') ?: "0"
                columns[c].add(raw.toDoubleOrNull() ?: 0.0)
            }
        }

        val series = channelNames.mapIndexed { idx, name ->
            val vals = columns[idx]
            DatalogChannelSeries(
                name = name,
                values = vals,
                minValue = vals.minOrNull() ?: 0.0,
                maxValue = vals.maxOrNull() ?: 0.0
            )
        }
        return DatalogReplaySession(timestamps = timestamps, channels = series)
    }
}
