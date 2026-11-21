package com.punhanaghayev.bimmerpro.core.protocol

/**
 * Shared ELM327 / STN2255 AT response parser & ISO 15765-2 multi-frame helper
 * used by Wi-Fi STN, BLE GATT, and Classic Bluetooth SPP OBD adapters.
 */
object ElmIsoTpHelper {

    fun parseVoltage(raw: String): Double? {
        val match = Regex("(\\d{1,2}\\.\\d{1,2})").find(raw) ?: return null
        return match.groupValues[1].toDoubleOrNull()
    }

    fun isFirstFrameResponse(raw: String): Boolean {
        val frames = extractRawCanFrames(raw)
        val first = frames.firstOrNull() ?: return false
        return first.isNotEmpty() && ((first[0].toInt() and 0xF0) ushr 4) == 1
    }

    fun parseUdsResponse(raw: String): ByteArray {
        val lines = raw.replace(">", "").split(Regex("[\r\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (lines.any { it.contains("NO DATA", ignoreCase = true) || it.contains("ERROR", ignoreCase = true) }) {
            return byteArrayOf(0x7F, 0x22, 0x31)
        }

        val allFrames = extractRawCanFrames(raw)
        if (allFrames.isEmpty()) return byteArrayOf(0x7F, 0x22, 0x31)

        // Filter out intermediate NRC 0x78 (Response Pending) frames if a subsequent frame arrived
        val finalFrames = if (allFrames.size > 1) {
            val filtered = allFrames.filterNot { f ->
                (f.size >= 4 && (f[0].toInt() and 0xFF) <= 0x07 && (f[1].toInt() and 0xFF) == 0x7F && (f[3].toInt() and 0xFF) == 0x78) ||
                    (f.size >= 3 && (f[0].toInt() and 0xFF) == 0x7F && (f[2].toInt() and 0xFF) == 0x78)
            }
            filtered.ifEmpty { allFrames }
        } else {
            allFrames
        }

        return if (finalFrames.size > 1) {
            IsoTpCodec.reassemble(finalFrames)
        } else {
            val single = finalFrames.first()
            val pci = (single[0].toInt() and 0xF0) ushr 4
            val sfLen = single[0].toInt() and 0x0F
            if (pci == 0 && sfLen in 1 until single.size) {
                single.copyOfRange(1, 1 + sfLen)
            } else {
                single
            }
        }
    }

    private fun extractRawCanFrames(raw: String): List<ByteArray> {
        val lines = raw.replace(">", "").split(Regex("[\r\n]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return lines.mapNotNull { line ->
            val rawTokens = line.split(Regex("\\s+")).filter { it.isNotBlank() }
            val hasCanIdHeader = rawTokens.firstOrNull()?.let {
                it.length == 3 && it.all { c -> c.isDigit() || c.uppercaseChar() in 'A'..'F' }
            } == true
            val byteTokens = (if (hasCanIdHeader) rawTokens.drop(1) else rawTokens)
                .filter { it.length == 2 && it.all { c -> c.isDigit() || c.uppercaseChar() in 'A'..'F' } }
            if (byteTokens.isEmpty()) return@mapNotNull null
            val bytes = byteTokens.map { it.toInt(16).toByte() }.toByteArray()
            when {
                hasCanIdHeader && bytes.size > 2 && ((bytes[0].toInt() and 0xFF) == 0xF4 || (bytes[0].toInt() and 0xFF) == 0xF1) ->
                    bytes.copyOfRange(1, bytes.size)
                bytes.size > 3 && (bytes[0].toInt() and 0xFF) == 0x06 && (bytes[1].toInt() and 0xFF) == 0xF1 ->
                    bytes.copyOfRange(2, bytes.size)
                bytes.size > 2 && ((bytes[0].toInt() and 0xF0) ushr 4) == 6 &&
                    (bytes[1].toInt() and 0xFF) == 0xF4 ->
                    bytes.copyOfRange(2, bytes.size)
                else -> bytes
            }
        }
    }
}
