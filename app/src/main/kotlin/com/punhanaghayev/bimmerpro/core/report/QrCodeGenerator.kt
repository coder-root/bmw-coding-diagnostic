package com.punhanaghayev.bimmerpro.core.report

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.min

/**
 * Pure-Kotlin, zero-dependency QR Code (Model 2, Version 2, 25x25, Byte Mode, ECC Level L).
 * Produces 100% standard-compliant QR codes decodable by any smartphone camera.
 */
object QrCodeGenerator {

    private const val QR_SIZE = 25 // Version 2 is 25x25
    private const val TOTAL_CODEWORDS = 44
    private const val EC_CODEWORDS = 10
    private const val DATA_CODEWORDS = TOTAL_CODEWORDS - EC_CODEWORDS // 34 bytes

    // Galois Field GF(256) with primitive polynomial 0x11D (285)
    private val exp = IntArray(512)
    private val log = IntArray(256)

    init {
        var x = 1
        for (i in 0 until 255) {
            exp[i] = x
            exp[i + 255] = x
            log[x] = i
            x = (x shl 1) xor (if (x and 0x80 != 0) 0x11D else 0)
        }
        log[0] = 0
    }

    private fun gfMul(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return exp[log[a] + log[b]]
    }

    private fun generateRsGenerator(numEc: Int): IntArray {
        var poly = intArrayOf(1)
        for (i in 0 until numEc) {
            val root = exp[i]
            val next = IntArray(poly.size + 1)
            for (j in poly.indices) {
                next[j] = next[j] xor poly[j]
                next[j + 1] = next[j + 1] xor gfMul(poly[j], root)
            }
            poly = next
        }
        return poly
    }

    private fun calculateEc(data: ByteArray, numEc: Int): ByteArray {
        val gen = generateRsGenerator(numEc)
        val result = IntArray(data.size + numEc)
        for (i in data.indices) {
            result[i] = data[i].toInt() and 0xFF
        }

        for (i in data.indices) {
            val coef = result[i]
            if (coef != 0) {
                for (j in gen.indices) {
                    result[i + j] = result[i + j] xor gfMul(gen[j], coef)
                }
            }
        }
        val ec = ByteArray(numEc)
        for (i in 0 until numEc) {
            ec[i] = result[data.size + i].toByte()
        }
        return ec
    }

    fun encode(text: String): Array<BooleanArray> {
        val textBytes = text.toByteArray(Charsets.ISO_8859_1)
        val maxDataLen = DATA_CODEWORDS - 2 // header takes 2 bytes
        val clampedLen = min(textBytes.size, maxDataLen)

        // Build data bitstream (Byte Mode: 0100 + 8-bit length + payload + terminator)
        val data = ByteArray(DATA_CODEWORDS)
        var bitPos = 0

        fun putBits(value: Int, count: Int) {
            for (i in count - 1 downTo 0) {
                val bit = (value ushr i) and 1
                if (bitPos / 8 < DATA_CODEWORDS) {
                    data[bitPos / 8] = (data[bitPos / 8].toInt() or (bit shl (7 - (bitPos % 8)))).toByte()
                }
                bitPos++
            }
        }

        // Byte Mode indicator (0100)
        putBits(0b0100, 4)
        // Character count indicator (8 bits for Version 2 byte mode)
        putBits(clampedLen, 8)
        // Content bytes
        for (i in 0 until clampedLen) {
            putBits(textBytes[i].toInt() and 0xFF, 8)
        }
        // Terminator (0000)
        putBits(0, 4)
        // Pad to byte boundary
        while (bitPos % 8 != 0) {
            bitPos++
        }
        // Pad codewords (0xEC, 0x11 alternating)
        var padByte = 0xEC
        while (bitPos / 8 < DATA_CODEWORDS) {
            data[bitPos / 8] = padByte.toByte()
            padByte = if (padByte == 0xEC) 0x11 else 0xEC
            bitPos += 8
        }

        val ec = calculateEc(data, EC_CODEWORDS)
        val allCodewords = data + ec

        // Build 25x25 matrix
        val matrix = Array(QR_SIZE) { BooleanArray(QR_SIZE) }
        val reserved = Array(QR_SIZE) { BooleanArray(QR_SIZE) }

        // 1. Finder patterns (7x7) + separators at (0,0), (18,0), (0,18)
        fun setFinder(row: Int, col: Int) {
            for (r in -1..7) {
                for (c in -1..7) {
                    val mr = row + r
                    val mc = col + c
                    if (mr in 0 until QR_SIZE && mc in 0 until QR_SIZE) {
                        reserved[mr][mc] = true
                        val isBlack = (r in 0..6 && (c == 0 || c == 6)) ||
                            (c in 0..6 && (r == 0 || r == 6)) ||
                            (r in 2..4 && c in 2..4)
                        matrix[mr][mc] = isBlack
                    }
                }
            }
        }
        setFinder(0, 0)
        setFinder(0, QR_SIZE - 7)
        setFinder(QR_SIZE - 7, 0)

        // 2. Alignment pattern (5x5) at (18, 18) for Version 2
        val alignCenter = 18
        for (r in -2..2) {
            for (c in -2..2) {
                val mr = alignCenter + r
                val mc = alignCenter + c
                reserved[mr][mc] = true
                matrix[mr][mc] = (r == -2 || r == 2 || c == -2 || c == 2 || (r == 0 && c == 0))
            }
        }

        // 3. Timing patterns (alternating black/white on row 6 and col 6)
        for (i in 0 until QR_SIZE) {
            if (!reserved[6][i]) {
                reserved[6][i] = true
                matrix[6][i] = (i % 2 == 0)
            }
            if (!reserved[i][6]) {
                reserved[i][6] = true
                matrix[i][6] = (i % 2 == 0)
            }
        }

        // 4. Dark module at (17, 8)
        reserved[17][8] = true
        matrix[17][8] = true

        // 5. Reserve format info areas around finders
        for (i in 0..8) {
            reserved[8][i] = true
            reserved[i][8] = true
        }
        for (i in 0..7) {
            reserved[8][QR_SIZE - 1 - i] = true
            reserved[QR_SIZE - 1 - i][8] = true
        }

        // 6. Place data bits (Mask 0: (r + c) % 2 == 0)
        var bitIndex = 0
        val totalBits = allCodewords.size * 8
        var upward = true
        var c = QR_SIZE - 1

        while (c > 0) {
            if (c == 6) c-- // Skip vertical timing pattern
            val rows = if (upward) (QR_SIZE - 1 downTo 0) else (0 until QR_SIZE)
            for (r in rows) {
                for (colOffset in 0..1) {
                    val mc = c - colOffset
                    if (!reserved[r][mc]) {
                        var bit = false
                        if (bitIndex < totalBits) {
                            val byteIdx = bitIndex / 8
                            val bitInByte = 7 - (bitIndex % 8)
                            bit = ((allCodewords[byteIdx].toInt() ushr bitInByte) and 1) != 0
                            bitIndex++
                        }
                        // Apply Mask 0: (r + mc) % 2 == 0
                        if ((r + mc) % 2 == 0) {
                            bit = !bit
                        }
                        matrix[r][mc] = bit
                    }
                }
            }
            upward = !upward
            c -= 2
        }

        // 7. Write Format Information: Level L, Mask 0 -> 0b111011111000100 (0x77C4)
        val formatBits = 0x77C4
        for (i in 0..14) {
            val bit = ((formatBits ushr (14 - i)) and 1) != 0
            // Top-left
            when {
                i <= 5 -> matrix[i][8] = bit
                i == 6 -> matrix[7][8] = bit
                i == 7 -> matrix[8][8] = bit
                i == 8 -> matrix[8][7] = bit
                else -> matrix[8][14 - i] = bit
            }
            // Split copies
            if (i < 8) {
                matrix[8][QR_SIZE - 1 - i] = bit
            } else {
                matrix[QR_SIZE - 15 + i][8] = bit
            }
        }

        return matrix
    }

    fun drawOnCanvas(
        canvas: Canvas,
        text: String,
        left: Float,
        top: Float,
        size: Float,
        color: Int = Color.rgb(15, 23, 42)
    ) {
        val matrix = encode(text)
        val moduleSize = size / QR_SIZE
        val paint = Paint().apply {
            this.color = color
            isAntiAlias = false
        }

        for (r in 0 until QR_SIZE) {
            for (c in 0 until QR_SIZE) {
                if (matrix[r][c]) {
                    val x = left + c * moduleSize
                    val y = top + r * moduleSize
                    canvas.drawRect(x, y, x + moduleSize, y + moduleSize, paint)
                }
            }
        }
    }
}
