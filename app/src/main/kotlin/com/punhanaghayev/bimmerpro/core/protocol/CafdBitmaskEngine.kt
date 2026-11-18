package com.punhanaghayev.bimmerpro.core.protocol

/**
 * Authentic BMW CAFD/FDL bit-level coding engine.
 * Modifies only the bits covered by [bitMask] at [byteOffset] while preserving all neighboring
 * bits in the same ECU coding byte, matching E-Sys / BimmerCode FDL behavior.
 */
object CafdBitmaskEngine {

    fun readMaskedValue(codingData: ByteArray, byteOffset: Int, bitMask: Int): Int {
        require(byteOffset in codingData.indices) {
            "Byte offset $byteOffset out of bounds for CAFD buffer (${codingData.size} bytes)"
        }
        val mask = bitMask and 0xFF
        require(mask != 0) { "Bitmask cannot be zero" }
        val shift = mask.countTrailingZeroBits()
        return ((codingData[byteOffset].toInt() and 0xFF) and mask) ushr shift
    }

    fun writeMaskedValue(
        codingData: ByteArray,
        byteOffset: Int,
        bitMask: Int,
        newValue: Int
    ): ByteArray {
        require(byteOffset in codingData.indices) {
            "Byte offset $byteOffset out of bounds for CAFD buffer (${codingData.size} bytes)"
        }
        val mask = bitMask and 0xFF
        require(mask != 0) { "Bitmask cannot be zero" }
        val shift = mask.countTrailingZeroBits()
        val copy = codingData.copyOf()
        val existingByte = copy[byteOffset].toInt() and 0xFF
        val cleared = existingByte and mask.inv()
        val shiftedNew = (newValue shl shift) and mask
        copy[byteOffset] = (cleared or shiftedNew).toByte()
        return copy
    }
}
