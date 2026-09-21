package com.acrebuild.core

/** Little-endian cursor over a converted pack byte array. */
class PackReader(private val data: ByteArray) {
    var pos: Int = 0
        private set

    fun u8(): Int = data[pos++].toInt() and 0xFF

    fun i8(): Int = data[pos++].toInt()

    fun u16(): Int {
        val v = (data[pos].toInt() and 0xFF) or ((data[pos + 1].toInt() and 0xFF) shl 8)
        pos += 2
        return v
    }

    fun i16(): Int {
        val v = u16()
        return if (v >= 0x8000) v - 0x10000 else v
    }

    fun u32(): Long {
        var v = 0L
        for (i in 0..3) v = v or ((data[pos + i].toLong() and 0xFF) shl (i * 8))
        pos += 4
        return v
    }

    fun bytes(n: Int): ByteArray {
        val out = data.copyOfRange(pos, pos + n)
        pos += n
        return out
    }

    fun magic(expected: String) {
        val got = String(bytes(4), Charsets.US_ASCII)
        require(got == expected) { "bad magic: $got (expected $expected)" }
    }
}
