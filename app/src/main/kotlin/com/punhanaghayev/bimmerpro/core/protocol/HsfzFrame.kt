package com.punhanaghayev.bimmerpro.core.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BMW High-Speed Fahrzeugzugang (HSFZ) frame used over ENET (TCP port 6801) for F/G/I-Series ECUs.
 *
 * Frame layout:
 * - Bytes 0..3: Payload length (Big-Endian uint32; includes source + target + UDS payload)
 * - Bytes 4..5: Control word (Big-Endian uint16; 0x0001 = Diagnostic Data)
 * - Bytes 6..n: Payload (for 0x0001: [0] = sourceAddr, [1] = targetAddr, [2..] = UDS PDU)
 */
data class HsfzFrame(
    val controlWord: Int,
    val sourceAddress: Int,
    val targetAddress: Int,
    val payload: ByteArray
) {
    fun encode(): ByteArray {
        val bodyLength = when (controlWord) {
            CONTROL_DIAGNOSTIC_DATA, CONTROL_ACK -> 2 + payload.size
            else -> payload.size
        }
        val buffer = ByteBuffer.allocate(HEADER_SIZE + bodyLength).order(ByteOrder.BIG_ENDIAN)
        buffer.putInt(bodyLength)
        buffer.putShort(controlWord.toShort())
        if (controlWord == CONTROL_DIAGNOSTIC_DATA || controlWord == CONTROL_ACK) {
            buffer.put(sourceAddress.toByte())
            buffer.put(targetAddress.toByte())
        }
        buffer.put(payload)
        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HsfzFrame) return false
        return controlWord == other.controlWord &&
            sourceAddress == other.sourceAddress &&
            targetAddress == other.targetAddress &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = controlWord
        result = 31 * result + sourceAddress
        result = 31 * result + targetAddress
        result = 31 * result + payload.contentHashCode()
        return result
    }

    companion object {
        const val HEADER_SIZE = 6
        const val TESTER_ADDRESS = 0xF4
        const val CONTROL_DIAGNOSTIC_DATA = 0x0001
        const val CONTROL_ACK = 0x0002
        const val CONTROL_ALIVE_CHECK = 0x0012

        fun diagnosticRequest(targetAddress: Int, udsPayload: ByteArray, sourceAddress: Int = TESTER_ADDRESS): HsfzFrame =
            HsfzFrame(
                controlWord = CONTROL_DIAGNOSTIC_DATA,
                sourceAddress = sourceAddress,
                targetAddress = targetAddress,
                payload = udsPayload
            )

        fun decode(raw: ByteArray): HsfzFrame {
            require(raw.size >= HEADER_SIZE) { "HSFZ frame shorter than 6-byte header (${raw.size} bytes)" }
            val buffer = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN)
            val bodyLength = buffer.int
            val controlWord = buffer.short.toInt() and 0xFFFF
            require(bodyLength >= 0 && raw.size >= HEADER_SIZE + bodyLength) {
                "Incomplete HSFZ frame: expected $bodyLength body bytes, got ${raw.size - HEADER_SIZE}"
            }
            return if (controlWord == CONTROL_DIAGNOSTIC_DATA || controlWord == CONTROL_ACK) {
                require(bodyLength >= 2) { "Diagnostic HSFZ frame missing source/target addresses" }
                val source = buffer.get().toInt() and 0xFF
                val target = buffer.get().toInt() and 0xFF
                val uds = ByteArray(bodyLength - 2)
                buffer.get(uds)
                HsfzFrame(controlWord, source, target, uds)
            } else {
                val data = ByteArray(bodyLength)
                buffer.get(data)
                HsfzFrame(controlWord, 0, 0, data)
            }
        }
    }
}

fun ByteArray.toHexString(): String = joinToString(" ") { "%02X".format(it) }
