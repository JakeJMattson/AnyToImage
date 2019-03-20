package io.github.jakejmattson.anytoimage.utils

/**
 * Limited utility class for handling bytes and conversions.
 *
 * @author JakeJMattson
 */
object ByteUtils {

    /**
     * Split integer into bytes.
     *
     * @param pixel
     * Integer to be split
     * @param count
     * Number of bytes desired
     *
     * @return Array of bytes
     */
    fun intToBytes(pixel: Int, count: Int): ByteArray {

        val bytes = ByteArray(count)

        for (i in 0 until count)
            bytes[i] = (pixel shr (count - i - 1) * 8 and 0x0ff).toByte()

        return bytes
    }

    /**
     * Combine values into an integer.
     *
     * @param bytes
     * Values to be shifted
     *
     * @return Resulting integer
     */
    fun bytesToInt(bytes: ByteArray): Int {
        val unsigned = bytes.map { it.toInt() and 0xFF }

        return when {
            unsigned.size == 3 -> unsigned[0] shl 16 or (unsigned[1] shl 8) or unsigned[2]
            unsigned.size == 4 -> unsigned[0] shl 24 or (unsigned[1] shl 16) or (unsigned[2] shl 8) or unsigned[3]
            else -> -1
        }
    }
}