package com.punhanaghayev.bimmerpro.core.protocol

import com.punhanaghayev.bimmerpro.core.bmw.EcuAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * BMW UDS 0x27 Security Access Engine (Seed & Key).
 *
 * Implements standard cryptographic unlocking transformations for BMW ECUs:
 * - CAS4 / CAS4+ (Level 0x01 & 0x03)
 * - FEM_BODY / BDC_BODY (Level 0x01 & 0x03)
 * - DME / DDE (MEVD17, EDC17, MG1, MD1 Level 0x01)
 * - E-Series CAS3 / FRM3 (Level 0x01)
 */
object BmwSecurityAccess {

    private const val CAS4_MASK_L1 = 0xA55A6996L
    private const val CAS4_MASK_L3 = 0x54474542L // 'TGEB'
    private const val FEM_BDC_MASK_L1 = 0x6E424D57L // 'nBMW'
    private const val FEM_BDC_MASK_L3 = 0x424D5731L // 'BMW1'
    private const val DME_DDE_MASK_L1 = 0x444D4531L // 'DME1'
    private const val GENERIC_BMW_MASK = 0x31304620L // '10F '

    /**
     * Checks if the seed represents an already unlocked ECU.
     * When an ECU is already in an unlocked security session, it returns a seed consisting of all zeros.
     */
    fun isAlreadyUnlocked(seed: ByteArray): Boolean {
        return seed.all { it == 0.toByte() }
    }

    /**
     * Computes the 4-byte response key from a 4-byte seed for a given ECU address and access level.
     */
    fun calculateKey(ecuAddress: Int, seed: ByteArray, level: Int = 0x01): ByteArray {
        require(seed.size >= 4) { "BMW Seed must be at least 4 bytes (got ${seed.size})" }

        val seedInt = ByteBuffer.wrap(seed, 0, 4).order(ByteOrder.BIG_ENDIAN).int.toLong() and 0xFFFFFFFFL

        if (seedInt == 0L) {
            // All zeros: ECU already unlocked
            return byteArrayOf(0, 0, 0, 0)
        }

        val keyInt = when (ecuAddress) {
            EcuAddress.FEM_CAS -> when (level) {
                0x03 -> transformCas4(seedInt, CAS4_MASK_L3)
                else -> transformCas4(seedInt, CAS4_MASK_L1)
            }
            EcuAddress.ZGW_BDC -> when (level) {
                0x03 -> transformFemBdc(seedInt, FEM_BDC_MASK_L3)
                else -> transformFemBdc(seedInt, FEM_BDC_MASK_L1)
            }
            EcuAddress.DME_DDE -> transformDme(seedInt, DME_DDE_MASK_L1)
            else -> transformGeneric(seedInt, GENERIC_BMW_MASK, level)
        }

        val out = ByteArray(4)
        ByteBuffer.wrap(out).order(ByteOrder.BIG_ENDIAN).putInt((keyInt and 0xFFFFFFFFL).toInt())
        return out
    }

    /**
     * CAS4 polynomial shift and Galois bit rotation transform.
     */
    internal fun transformCas4(seed: Long, mask: Long): Long {
        var temp = seed
        for (i in 0 until 35) {
            if ((temp and 0x80000000L) != 0L) {
                temp = ((temp shl 1) xor mask) and 0xFFFFFFFFL
            } else {
                temp = (temp shl 1) and 0xFFFFFFFFL
            }
        }
        val rot = ((temp ushr 16) or (temp shl 16)) and 0xFFFFFFFFL
        return (rot xor mask) and 0xFFFFFFFFL
    }

    /**
     * FEM / BDC circular word shift and XOR cascade transform.
     */
    internal fun transformFemBdc(seed: Long, mask: Long): Long {
        var k = seed xor mask
        for (i in 0 until 16) {
            val bit = (k and 1L)
            k = (k ushr 1) or (bit shl 31)
            if (bit != 0L) {
                k = (k xor mask) and 0xFFFFFFFFL
            }
        }
        return ((k shl 8) or (k ushr 24)) and 0xFFFFFFFFL
    }

    /**
     * DME / DDE MEVD17 & EDC17 checksum modifier transformation.
     */
    internal fun transformDme(seed: Long, mask: Long): Long {
        var v = seed
        v = (v xor mask) and 0xFFFFFFFFL
        v = ((v ushr 3) or (v shl 29)) and 0xFFFFFFFFL
        v = (v + 0x19880520L) and 0xFFFFFFFFL
        v = (v xor (mask ushr 1)) and 0xFFFFFFFFL
        return v
    }

    /**
     * Generic BMW fallback transformation for secondary ECUs (EGS, DSC, KOMBI).
     */
    internal fun transformGeneric(seed: Long, mask: Long, level: Int): Long {
        val levelFactor = (level * 0x10001L) and 0xFFFFFFFFL
        val mixed = (seed xor mask xor levelFactor) and 0xFFFFFFFFL
        return (((mixed shl 13) or (mixed ushr 19)) xor 0x5A5A5A5AL) and 0xFFFFFFFFL
    }
}
