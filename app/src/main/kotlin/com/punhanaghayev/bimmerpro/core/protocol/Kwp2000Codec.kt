package com.punhanaghayev.bimmerpro.core.protocol

/**
 * KWP2000 (ISO 14230) codec for BMW E-Series (E60, E90, E70) over K-Line / D-CAN.
 */
object Kwp2000Codec {

    const val SID_START_DIAGNOSTIC_SESSION = 0x10
    const val SID_CLEAR_DTC = 0x14
    const val SID_READ_DTC_BY_STATUS = 0x18
    const val SID_READ_ECU_ID = 0x1A
    const val SID_READ_LOCAL_ID = 0x21
    const val SID_NEGATIVE_RESPONSE = 0x7F

    fun buildReadDtcByStatus(groupHigh: Int = 0xFF, groupLow: Int = 0x00): ByteArray =
        byteArrayOf(
            SID_READ_DTC_BY_STATUS.toByte(),
            0x02, // Report all DTCs with status
            groupHigh.toByte(),
            groupLow.toByte()
        )

    fun buildReadLocalId(localId: Int): ByteArray =
        byteArrayOf(SID_READ_LOCAL_ID.toByte(), localId.toByte())

    fun buildReadEcuIdentification(optionByte: Int = 0x80): ByteArray =
        byteArrayOf(SID_READ_ECU_ID.toByte(), optionByte.toByte())

    /**
     * Parses KWP2000 0x58 (0x18 + 0x40) DTC response:
     * byte[0] = 0x58, byte[1] = count, followed by 3-byte records [DTC_Hi, DTC_Lo, Status].
     */
    fun parseDtcResponse(raw: ByteArray): List<Pair<String, Int>> {
        if (raw.size < 5 || (raw[0].toInt() and 0xFF) != 0x58) return emptyList()
        val count = raw[1].toInt() and 0xFF
        val out = mutableListOf<Pair<String, Int>>()
        var idx = 2
        while (out.size < count && idx + 2 < raw.size) {
            val hi = raw[idx].toInt() and 0xFF
            val lo = raw[idx + 1].toInt() and 0xFF
            val status = raw[idx + 2].toInt() and 0xFF
            out.add("%02X%02X".format(hi, lo) to status)
            idx += 3
        }
        return out
    }
}
