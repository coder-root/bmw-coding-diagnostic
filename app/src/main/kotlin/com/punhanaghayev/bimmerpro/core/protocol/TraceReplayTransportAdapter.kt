package com.punhanaghayev.bimmerpro.core.protocol

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class RecordedUdsFrame(
    val ecuAddress: Int,
    val requestSid: Int?,
    val responseBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecordedUdsFrame) return false
        return ecuAddress == other.ecuAddress &&
            requestSid == other.requestSid &&
            responseBytes.contentEquals(other.responseBytes)
    }

    override fun hashCode(): Int {
        var result = ecuAddress
        result = 31 * result + (requestSid ?: 0)
        result = 31 * result + responseBytes.contentHashCode()
        return result
    }
}

/**
 * Offline Record/Replay UDS Transport Engine.
 * Parses exported `.txt` trace logs from [SessionTraceRecorder] (or raw `[TX]/[RX] 0xXX | ... | HEX`)
 * and replays matching ECU responses deterministically without connecting to a physical car.
 */
class TraceReplayTransportAdapter(
    traceText: String,
    override val adapterType: AdapterType = AdapterType.VLINKER_BM_WIFI
) : TransportAdapter {

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    override val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    val recordedFrames: List<RecordedUdsFrame> = parseTraceText(traceText)
    private var cursorMap = mutableMapOf<Int, Int>()

    override suspend fun connect() {
        cursorMap.clear()
        _connectionState.value = ConnectionState.Connected(adapterType, "replay://offline-trace (${recordedFrames.size} RX)")
    }

    override suspend fun disconnect() {
        _connectionState.value = ConnectionState.Disconnected
    }

    override suspend fun transceive(frame: ByteArray): ByteArray {
        val hsfz = HsfzFrame.decode(frame)
        val targetEcu = hsfz.targetAddress and 0xFF
        val reqSid = hsfz.payload.firstOrNull()?.let { it.toInt() and 0xFF } ?: 0

        val matching = recordedFrames.filter {
            it.ecuAddress == targetEcu && (it.requestSid == null || it.requestSid == reqSid)
        }
        val payload = if (matching.isNotEmpty()) {
            val key = (targetEcu shl 8) or reqSid
            val idx = cursorMap.getOrDefault(key, 0)
            cursorMap[key] = (idx + 1) % matching.size
            matching[idx].responseBytes
        } else {
            byteArrayOf(0x7F, reqSid.toByte(), 0x11)
        }

        return HsfzFrame(
            controlWord = HsfzFrame.CONTROL_DIAGNOSTIC_DATA,
            sourceAddress = targetEcu,
            targetAddress = HsfzFrame.TESTER_ADDRESS,
            payload = payload
        ).encode()
    }

    companion object {
        private val inlineRxRegex = Regex("""(?:\[RX]|RX)\s+(?:ECU=)?0x([0-9A-Fa-f]{2})\s*\|\s*[^|]*\|\s*(?:HEX:\s*)?([0-9A-Fa-f\s]+)""", RegexOption.IGNORE_CASE)
        private val headerRxRegex = Regex("""\[RX]\s+(?:ECU=)?0x([0-9A-Fa-f]{2})\b""", RegexOption.IGNORE_CASE)

        fun parseTraceText(traceText: String): List<RecordedUdsFrame> {
            val out = mutableListOf<RecordedUdsFrame>()
            val lines = traceText.lines()
            var pendingRxEcu: Int? = null

            lines.forEach { rawLine ->
                val line = rawLine.trim()
                val inlineMatch = inlineRxRegex.find(line)
                if (inlineMatch != null) {
                    val ecuAddr = inlineMatch.groupValues[1].toIntOrNull(16)
                    val bytes = parseHexBytes(inlineMatch.groupValues[2])
                    if (ecuAddr != null && bytes.isNotEmpty()) {
                        out.add(buildRecordedFrame(ecuAddr, bytes))
                    }
                    pendingRxEcu = null
                    return@forEach
                }

                val hdrMatch = headerRxRegex.find(line)
                if (hdrMatch != null) {
                    pendingRxEcu = hdrMatch.groupValues[1].toIntOrNull(16)
                    return@forEach
                }

                val ecu = pendingRxEcu
                if (ecu != null && line.startsWith("HEX:", ignoreCase = true)) {
                    val hexPart = line.substringAfter(":").trim()
                    val bytes = parseHexBytes(hexPart)
                    if (bytes.isNotEmpty()) {
                        out.add(buildRecordedFrame(ecu, bytes))
                    }
                    pendingRxEcu = null
                } else if (line.isNotEmpty() && !line.startsWith("#")) {
                    pendingRxEcu = null
                }
            }
            return out
        }

        private fun parseHexBytes(hexStr: String): ByteArray =
            hexStr.trim()
                .split(Regex("\\s+"))
                .filter { it.length == 2 }
                .mapNotNull { it.toIntOrNull(16)?.toByte() }
                .toByteArray()

        private fun buildRecordedFrame(ecuAddr: Int, bytes: ByteArray): RecordedUdsFrame {
            val first = bytes[0].toInt() and 0xFF
            val reqSid = when {
                first == 0x7F && bytes.size >= 2 -> bytes[1].toInt() and 0xFF
                first >= 0x40 -> first - 0x40
                else -> null
            }
            return RecordedUdsFrame(ecuAddr, reqSid, bytes)
        }
    }
}
