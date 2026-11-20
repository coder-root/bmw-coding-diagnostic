package com.punhanaghayev.bimmerpro.core.protocol

/**
 * ISO 15765-2 (ISO-TP) segmentation and multi-frame reassembly codec for CAN / vLinker / BLE OBD.
 * Handles:
 * - Single Frame (SF): 0x0L [data...]
 * - First Frame (FF): 0x1H LL [data...]
 * - Consecutive Frame (CF): 0x2N [data...] (sequence nibble 1..F wrapping to 0)
 * - Flow Control (FC): 0x30 0x00 0x00 (ContinueToSend, BS=0, STmin=0)
 */
object IsoTpCodec {

    fun flowControlFrame(): ByteArray = byteArrayOf(0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)

    fun segment(payload: ByteArray): List<ByteArray> {
        if (payload.size <= 7) {
            val sf = ByteArray(1 + payload.size)
            sf[0] = (payload.size and 0x0F).toByte()
            System.arraycopy(payload, 0, sf, 1, payload.size)
            return listOf(sf)
        }
        val frames = mutableListOf<ByteArray>()
        val totalLen = payload.size
        val ff = ByteArray(8)
        ff[0] = (0x10 or ((totalLen shr 8) and 0x0F)).toByte()
        ff[1] = (totalLen and 0xFF).toByte()
        System.arraycopy(payload, 0, ff, 2, 6)
        frames.add(ff)

        var offset = 6
        var seq = 1
        while (offset < totalLen) {
            val chunk = minOf(7, totalLen - offset)
            val cf = ByteArray(1 + chunk)
            cf[0] = (0x20 or (seq and 0x0F)).toByte()
            System.arraycopy(payload, offset, cf, 1, chunk)
            frames.add(cf)
            offset += chunk
            seq = (seq + 1) and 0x0F
        }
        return frames
    }

    /**
     * Reassembles one or more raw CAN frames (with optional 2-byte/3-byte CAN ID stripped) into a UDS PDU.
     */
    fun reassemble(canFrames: List<ByteArray>): ByteArray {
        if (canFrames.isEmpty()) return byteArrayOf()
        val first = canFrames.first()
        if (first.isEmpty()) return byteArrayOf()
        val pciType = (first[0].toInt() and 0xF0) ushr 4

        return when (pciType) {
            0 -> {
                val len = (first[0].toInt() and 0x0F).coerceAtMost(first.size - 1)
                first.copyOfRange(1, 1 + len)
            }
            1 -> {
                if (first.size < 2) return byteArrayOf()
                val expectedLen = ((first[0].toInt() and 0x0F) shl 8) or (first[1].toInt() and 0xFF)
                val out = ArrayList<Byte>(expectedLen)
                for (i in 2 until first.size) out.add(first[i])
                for (frameIdx in 1 until canFrames.size) {
                    val cf = canFrames[frameIdx]
                    if (cf.isNotEmpty() && ((cf[0].toInt() and 0xF0) ushr 4) == 2) {
                        for (i in 1 until cf.size) {
                            if (out.size < expectedLen) out.add(cf[i])
                        }
                    }
                }
                out.toByteArray()
            }
            else -> first
        }
    }
}
