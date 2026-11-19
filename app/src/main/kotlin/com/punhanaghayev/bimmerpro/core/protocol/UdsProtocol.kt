package com.punhanaghayev.bimmerpro.core.protocol

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UdsServiceId {
    const val DIAGNOSTIC_SESSION_CONTROL = 0x10
    const val ECU_RESET = 0x11
    const val CLEAR_DIAGNOSTIC_INFO = 0x14
    const val READ_DTC_INFO = 0x19
    const val READ_DATA_BY_ID = 0x22
    const val SECURITY_ACCESS = 0x27
    const val WRITE_DATA_BY_ID = 0x2E
    const val ROUTINE_CONTROL = 0x31
    const val TESTER_PRESENT = 0x3E
    const val NEGATIVE_RESPONSE = 0x7F
}

enum class UdsSessionType(val code: Byte) {
    DEFAULT(0x01),
    PROGRAMMING(0x02),
    EXTENDED(0x03),
    CODING(0x41)
}

enum class UdsNegativeResponseCode(val code: Int, val description: String) {
    GENERAL_REJECT(0x10, "General reject"),
    SERVICE_NOT_SUPPORTED(0x11, "Service not supported by ECU"),
    SUB_FUNCTION_NOT_SUPPORTED(0x12, "Sub-function not supported"),
    INCORRECT_MESSAGE_LENGTH(0x13, "Incorrect message length or format"),
    CONDITIONS_NOT_CORRECT(0x22, "Conditions not correct (check ignition/voltage)"),
    REQUEST_SEQUENCE_ERROR(0x24, "Request sequence error"),
    REQUEST_OUT_OF_RANGE(0x31, "Request out of range (DID/RID unsupported)"),
    SECURITY_ACCESS_DENIED(0x33, "Security access denied (Seed/Key or Secure Coding 2.0 locked)"),
    INVALID_KEY(0x35, "Invalid security key"),
    EXCEEDED_ATTEMPTS(0x36, "Exceeded number of security attempts"),
    RESPONSE_PENDING(0x78, "ECU processing request (response pending)"),
    UNKNOWN(0xFF, "Unknown negative response");

    companion object {
        fun fromCode(code: Int): UdsNegativeResponseCode =
            entries.firstOrNull { it.code == (code and 0xFF) } ?: UNKNOWN
    }
}

sealed interface UdsResponse {
    data class Positive(val serviceId: Int, val payload: ByteArray) : UdsResponse {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Positive) return false
            return serviceId == other.serviceId && payload.contentEquals(other.payload)
        }

        override fun hashCode(): Int = 31 * serviceId + payload.contentHashCode()
    }

    data class Negative(val requestedServiceId: Int, val nrc: UdsNegativeResponseCode) : UdsResponse
}

object UdsCodec {
    fun buildReadDataById(did: Int): ByteArray = byteArrayOf(
        UdsServiceId.READ_DATA_BY_ID.toByte(),
        ((did shr 8) and 0xFF).toByte(),
        (did and 0xFF).toByte()
    )

    fun buildWriteDataById(did: Int, data: ByteArray): ByteArray =
        byteArrayOf(
            UdsServiceId.WRITE_DATA_BY_ID.toByte(),
            ((did shr 8) and 0xFF).toByte(),
            (did and 0xFF).toByte()
        ) + data

    fun buildSessionControl(session: UdsSessionType): ByteArray =
        byteArrayOf(UdsServiceId.DIAGNOSTIC_SESSION_CONTROL.toByte(), session.code)

    fun buildReadDtcByStatusMask(statusMask: Int = 0xFF): ByteArray =
        byteArrayOf(UdsServiceId.READ_DTC_INFO.toByte(), 0x02, statusMask.toByte())

    fun buildClearAllDtc(): ByteArray =
        byteArrayOf(UdsServiceId.CLEAR_DIAGNOSTIC_INFO.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())

    fun buildEcuReset(resetType: Int = 0x01): ByteArray =
        byteArrayOf(UdsServiceId.ECU_RESET.toByte(), resetType.toByte())

    fun buildRoutineControl(subFunction: Int, routineId: Int, optionBytes: ByteArray = byteArrayOf()): ByteArray =
        byteArrayOf(
            UdsServiceId.ROUTINE_CONTROL.toByte(),
            subFunction.toByte(),
            ((routineId shr 8) and 0xFF).toByte(),
            (routineId and 0xFF).toByte()
        ) + optionBytes

    fun parseResponse(rawUds: ByteArray): UdsResponse {
        require(rawUds.isNotEmpty()) { "Empty UDS response" }
        val sid = rawUds[0].toInt() and 0xFF
        if (sid == UdsServiceId.NEGATIVE_RESPONSE) {
            val reqSid = if (rawUds.size > 1) rawUds[1].toInt() and 0xFF else 0
            val nrcByte = if (rawUds.size > 2) rawUds[2].toInt() and 0xFF else 0xFF
            return UdsResponse.Negative(reqSid, UdsNegativeResponseCode.fromCode(nrcByte))
        }
        val originalSid = sid - 0x40
        val payload = if (rawUds.size > 1) rawUds.copyOfRange(1, rawUds.size) else byteArrayOf()
        return UdsResponse.Positive(originalSid, payload)
    }
}

data class ProtocolLogEntry(
    val timestamp: String,
    val direction: String,
    val ecuAddress: Int,
    val summary: String,
    val rawHex: String,
    val isError: Boolean = false
)

class UdsClient(
    private val transportProvider: () -> TransportAdapter
) {
    private val _logs = MutableStateFlow<List<ProtocolLogEntry>>(emptyList())
    val logs: StateFlow<List<ProtocolLogEntry>> = _logs.asStateFlow()

    private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    suspend fun sendUds(ecuAddress: Int, udsPayload: ByteArray, summary: String): UdsResponse {
        val adapter = transportProvider()
        val requestFrame = HsfzFrame.diagnosticRequest(ecuAddress, udsPayload).encode()
        appendLog("TX", ecuAddress, summary, requestFrame.toHexString())

        val rawResponse = adapter.transceive(requestFrame)
        val decodedFrame = HsfzFrame.decode(rawResponse)
        val response = UdsCodec.parseResponse(decodedFrame.payload)

        when (response) {
            is UdsResponse.Positive -> appendLog(
                "RX",
                ecuAddress,
                "ACK 0x%02X (%d bytes)".format(response.serviceId + 0x40, response.payload.size),
                rawResponse.toHexString(),
                isError = false
            )
            is UdsResponse.Negative -> appendLog(
                "RX",
                ecuAddress,
                "NRC 0x%02X: ${response.nrc.description}".format(response.nrc.code),
                rawResponse.toHexString(),
                isError = true
            )
        }
        return response
    }

    fun clearLogs() {
        _logs.value = emptyList()
    }

    fun logInfo(direction: String, ecuAddress: Int, summary: String, rawHex: String = "") {
        appendLog(direction, ecuAddress, summary, rawHex, isError = false)
    }

    private fun appendLog(
        direction: String,
        ecuAddress: Int,
        summary: String,
        rawHex: String,
        isError: Boolean = false
    ) {
        val entry = ProtocolLogEntry(
            timestamp = timeFormat.format(Date()),
            direction = direction,
            ecuAddress = ecuAddress,
            summary = summary,
            rawHex = rawHex,
            isError = isError
        )
        _logs.update { current -> (listOf(entry) + current).take(120) }
    }
}
