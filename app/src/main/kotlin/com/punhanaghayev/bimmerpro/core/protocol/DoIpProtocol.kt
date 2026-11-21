package com.punhanaghayev.bimmerpro.core.protocol

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.StandardCharsets

/**
 * ISO 13400 (Diagnostic over IP - DoIP) Frame & Protocol Definitions for modern BMWs (F/G/I Series).
 */
data class DoIpMessage(
    val protocolVersion: Int = DoIpProtocol.PROTOCOL_VERSION,
    val payloadType: Int,
    val payload: ByteArray
) {
    fun encode(): ByteArray {
        val buffer = ByteBuffer.allocate(DoIpProtocol.HEADER_SIZE + payload.size).order(ByteOrder.BIG_ENDIAN)
        buffer.put(protocolVersion.toByte())
        buffer.put((protocolVersion xor 0xFF).toByte())
        buffer.putShort(payloadType.toShort())
        buffer.putInt(payload.size)
        buffer.put(payload)
        return buffer.array()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DoIpMessage) return false
        return protocolVersion == other.protocolVersion &&
            payloadType == other.payloadType &&
            payload.contentEquals(other.payload)
    }

    override fun hashCode(): Int {
        var result = protocolVersion
        result = 31 * result + payloadType
        result = 31 * result + payload.contentHashCode()
        return result
    }
}

object DoIpProtocol {

    const val PROTOCOL_VERSION = 0x02
    const val INVERSE_VERSION = 0xFD
    const val HEADER_SIZE = 8
    const val DEFAULT_DOIP_PORT = 13400
    const val BMW_TESTER_LOGICAL_ADDRESS = 0x0E00
    const val BMW_TESTER_ALT_ADDRESS = 0x0E80

    // Payload Types
    const val TYPE_HEADER_NACK = 0x0001
    const val TYPE_VEHICLE_IDENT_REQUEST = 0x0002
    const val TYPE_VEHICLE_ANNOUNCEMENT = 0x0004
    const val TYPE_ROUTING_ACTIVATION_REQUEST = 0x0005
    const val TYPE_ROUTING_ACTIVATION_RESPONSE = 0x0006
    const val TYPE_ALIVE_CHECK_REQUEST = 0x0007
    const val TYPE_ALIVE_CHECK_RESPONSE = 0x0008
    const val TYPE_DIAGNOSTIC_MESSAGE = 0x8001
    const val TYPE_DIAGNOSTIC_POS_ACK = 0x8002
    const val TYPE_DIAGNOSTIC_NEG_ACK = 0x8003

    // Routing Activation Codes
    const val ROUTING_SUCCESS = 0x10
    const val ROUTING_ALREADY_ACTIVE = 0x11

    fun createVehicleIdentRequest(): DoIpMessage =
        DoIpMessage(payloadType = TYPE_VEHICLE_IDENT_REQUEST, payload = ByteArray(0))

    fun createRoutingActivationRequest(
        sourceAddress: Int = BMW_TESTER_LOGICAL_ADDRESS,
        activationType: Byte = 0x00
    ): DoIpMessage {
        val payload = ByteBuffer.allocate(7).order(ByteOrder.BIG_ENDIAN)
        payload.putShort(sourceAddress.toShort())
        payload.put(activationType)
        payload.putInt(0x00000000) // Reserved (ISO 13400-2)
        return DoIpMessage(payloadType = TYPE_ROUTING_ACTIVATION_REQUEST, payload = payload.array())
    }

    fun createDiagnosticMessage(
        sourceAddress: Int = BMW_TESTER_LOGICAL_ADDRESS,
        targetAddress: Int,
        udsPayload: ByteArray
    ): DoIpMessage {
        val payload = ByteBuffer.allocate(4 + udsPayload.size).order(ByteOrder.BIG_ENDIAN)
        payload.putShort(sourceAddress.toShort())
        payload.putShort(targetAddress.toShort())
        payload.put(udsPayload)
        return DoIpMessage(payloadType = TYPE_DIAGNOSTIC_MESSAGE, payload = payload.array())
    }

    fun createAliveCheckResponse(sourceAddress: Int = BMW_TESTER_LOGICAL_ADDRESS): DoIpMessage {
        val payload = ByteBuffer.allocate(2).order(ByteOrder.BIG_ENDIAN)
        payload.putShort(sourceAddress.toShort())
        return DoIpMessage(payloadType = TYPE_ALIVE_CHECK_RESPONSE, payload = payload.array())
    }

    fun decode(raw: ByteArray): DoIpMessage {
        require(raw.size >= HEADER_SIZE) { "DoIP frame shorter than $HEADER_SIZE bytes (${raw.size} bytes)" }
        val buffer = ByteBuffer.wrap(raw).order(ByteOrder.BIG_ENDIAN)
        val version = buffer.get().toInt() and 0xFF
        val invVersion = buffer.get().toInt() and 0xFF
        val payloadType = buffer.short.toInt() and 0xFFFF
        val payloadLength = buffer.int
        require(payloadLength >= 0 && raw.size >= HEADER_SIZE + payloadLength) {
            "Incomplete DoIP frame: payloadLength=$payloadLength, raw.size=${raw.size}"
        }
        val payload = ByteArray(payloadLength)
        buffer.get(payload)
        return DoIpMessage(protocolVersion = version, payloadType = payloadType, payload = payload)
    }

    fun parseVehicleAnnouncement(payload: ByteArray): Triple<String?, Int?, String?> {
        if (payload.size < 25) return Triple(null, null, null)
        val buffer = ByteBuffer.wrap(payload).order(ByteOrder.BIG_ENDIAN)
        val vinBytes = ByteArray(17)
        buffer.get(vinBytes)
        val vin = String(vinBytes, StandardCharsets.US_ASCII).trim()
        val logicalAddress = buffer.short.toInt() and 0xFFFF
        val eidBytes = ByteArray(6)
        buffer.get(eidBytes)
        val eidHex = eidBytes.joinToString(":") { "%02X".format(it) }
        return Triple(vin, logicalAddress, eidHex)
    }
}
