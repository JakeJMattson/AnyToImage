package me.jakejmattson.anytoimage.utils

/**
 * Extract bytes from an integer.
 */
fun Int.extractBytes(count: Int): ByteArray {
    val bytes = ByteArray(count)

    for (i in 0 until count)
        bytes[i] = (this shr (count - i - 1) * 8 and 0x0ff).toByte()

    return bytes
}

/**
 * Shift bytes together to form an integer.
 */
fun ByteArray.bytesToInt(): Int {
    val unsigned = this.map { it.toInt() and 0xFF }

    return when (unsigned.size) {
        3 -> unsigned[0] shl 16 or (unsigned[1] shl 8) or unsigned[2]
        4 -> unsigned[0] shl 24 or (unsigned[1] shl 16) or (unsigned[2] shl 8) or unsigned[3]
        else -> -1
    }
}