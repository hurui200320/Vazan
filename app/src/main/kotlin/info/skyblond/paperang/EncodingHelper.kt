package info.skyblond.paperang

/**
 * Encoding [Short] to [ByteArray], using little endian.
 * */
internal fun Short.toLittleEndian(): ByteArray {
    val result = ByteArray(Short.SIZE_BYTES)
    this.toLittleEndian(result, 0)
    return result
}

/**
 * Encoding [Short] to [ByteArray], using little endian.
 * */
internal fun Short.toLittleEndian(out: ByteArray, offset: Int) {
    var value = this.toInt()
    val mask = 0xFF
    for (i in 0 until Short.SIZE_BYTES) {
        out[offset + i] = (value and mask).toByte()
        value = value shr Byte.SIZE_BITS
    }
}

/**
 * Restore from two [Byte]s to [Short]
 * */
internal fun bytesToShort(lb: Byte, hb: Byte): Short {
    var result = 0
    result = result or lb.toInt()
    result = result or (hb.toInt() shl Byte.SIZE_BITS)
    return result.toShort()
}

