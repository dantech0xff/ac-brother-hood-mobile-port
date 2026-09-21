package com.acrebuild.core

/**
 * Versioned binary save snapshot. Format: magic, schema version, tick index,
 * actor fixed position/velocity, held-touch flag, RNG state, checksum.
 * Serialization stays inside core; adapters only move bytes (SavePort).
 */
data class SaveSnapshot(
    val tickIndex: Long,
    val posX: Int,
    val posY: Int,
    val velX: Int,
    val velY: Int,
    val touchHeld: Boolean,
    val touchX: Int,
    val touchY: Int,
    val rngState: Long,
) {
    fun encode(): ByteArray {
        val body = ByteArray(SIZE)
        var o = 0
        o = putInt(body, o, MAGIC)
        o = putShort(body, o, SCHEMA_VERSION)
        o = putShort(body, o, 0) // reserved
        o = putLong(body, o, tickIndex)
        o = putInt(body, o, posX)
        o = putInt(body, o, posY)
        o = putInt(body, o, velX)
        o = putInt(body, o, velY)
        body[o] = if (touchHeld) 1 else 0
        o += 1
        o = putShort(body, o, touchX)
        o = putShort(body, o, touchY)
        o = putLong(body, o, rngState)
        val crc = checksum(body, 0, o)
        putInt(body, o, crc)
        return body
    }

    companion object {
        const val MAGIC = 0x41435253 // "ACRS"
        const val SCHEMA_VERSION = 1
        const val SIZE = 49

        fun decode(bytes: ByteArray): SaveSnapshot {
            require(bytes.size >= SIZE) { "snapshot too short: ${bytes.size}" }
            var o = 0
            val magic = readInt(bytes, o); o += 4
            require(magic == MAGIC) { "bad magic ${magic.toString(16)}" }
            val version = readShort(bytes, o); o += 2
            require(version == SCHEMA_VERSION) { "unsupported save version $version" }
            o += 2 // reserved
            val tick = readLong(bytes, o); o += 8
            val px = readInt(bytes, o); o += 4
            val py = readInt(bytes, o); o += 4
            val vx = readInt(bytes, o); o += 4
            val vy = readInt(bytes, o); o += 4
            val held = bytes[o] != 0.toByte(); o += 1
            val tx = readShort(bytes, o); o += 2
            val ty = readShort(bytes, o); o += 2
            val rng = readLong(bytes, o); o += 8
            val crc = readInt(bytes, o)
            require(crc == checksum(bytes, 0, o)) { "snapshot checksum mismatch" }
            return SaveSnapshot(tick, px, py, vx, vy, held, tx, ty, rng)
        }

        private fun checksum(b: ByteArray, from: Int, to: Int): Int {
            var c = 0x811C9DC5.toInt() // FNV-1a, deterministic and stable.
            for (i in from until to) {
                c = c xor (b[i].toInt() and 0xFF)
                c *= 0x01000193
            }
            return c
        }

        private fun putInt(b: ByteArray, o: Int, v: Int): Int {
            b[o] = (v ushr 24).toByte(); b[o + 1] = (v ushr 16).toByte()
            b[o + 2] = (v ushr 8).toByte(); b[o + 3] = v.toByte()
            return o + 4
        }

        private fun putShort(b: ByteArray, o: Int, v: Int): Int {
            b[o] = (v ushr 8).toByte(); b[o + 1] = v.toByte()
            return o + 2
        }

        private fun putLong(b: ByteArray, o: Int, v: Long): Int {
            for (i in 7 downTo 0) b[o + (7 - i)] = (v ushr (i * 8)).toByte()
            return o + 8
        }

        private fun readInt(b: ByteArray, o: Int): Int =
            ((b[o].toInt() and 0xFF) shl 24) or ((b[o + 1].toInt() and 0xFF) shl 16) or
                ((b[o + 2].toInt() and 0xFF) shl 8) or (b[o + 3].toInt() and 0xFF)

        private fun readShort(b: ByteArray, o: Int): Int =
            ((b[o].toInt() and 0xFF) shl 8) or (b[o + 1].toInt() and 0xFF)

        private fun readLong(b: ByteArray, o: Int): Long {
            var v = 0L
            for (i in 0 until 8) v = (v shl 8) or (b[o + i].toLong() and 0xFF)
            return v
        }
    }
}
