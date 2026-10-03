package com.acrebuild.core

import java.security.MessageDigest

/**
 * Save container v1 (ADR `docs/decisions/save-policy.md`). The payload is a
 * core-owned record — for the game, the port's `kBA` (160 little-endian
 * shorts = 320 B, `Level0World.saveFlush`). The envelope adds identity,
 * versions, a monotonic revision, a bounded length and a SHA-256 digest, so a
 * torn or foreign file is rejected instead of being loaded as progress.
 *
 * Layout (little-endian):
 *
 * | offset | size | field                                  |
 * |-------:|-----:|----------------------------------------|
 * |      0 |    4 | magic `ACBA`                           |
 * |      4 |    2 | envelope version ([VERSION])           |
 * |      6 |    2 | payload schema ([SCHEMA_KBA_V1], ...)  |
 * |      8 |    8 | saveRevision                           |
 * |     16 |    4 | payload length (<= [MAX_PAYLOAD])      |
 * |     20 |    n | payload                                |
 * |   20+n |   32 | SHA-256 over bytes `[0, 20+n)`         |
 *
 * A headerless file of exactly [LEGACY_KBA_SIZE] bytes is the pre-envelope
 * raw `kBA` record (v0). It decodes as [Decoded.legacy] (revision 0) for the
 * `kBA` schema only, and is rewritten as v1 on the next flush.
 */
object SaveEnvelope {
    private val MAGIC = byteArrayOf(0x41, 0x43, 0x42, 0x41)   // "ACBA"
    const val VERSION = 1
    /** The game's `kBA` record (160 × LE16). */
    const val SCHEMA_KBA_V1 = 1
    /** The toolchain spike's 49-byte `SaveSnapshot` (`ACRS`). */
    const val SCHEMA_SPIKE_ACRS = 2
    const val HEADER_SIZE = 20
    const val DIGEST_SIZE = 32
    const val MAX_PAYLOAD = 4096
    const val LEGACY_KBA_SIZE = 320

    class Decoded(val payload: ByteArray, val revision: Long, val legacy: Boolean)

    sealed class Result {
        class Ok(val save: Decoded) : Result()
        class Rejected(val reason: String) : Result()
    }

    fun encode(payload: ByteArray, revision: Long, schema: Int = SCHEMA_KBA_V1): ByteArray {
        require(payload.size <= MAX_PAYLOAD) { "payload ${payload.size} B > $MAX_PAYLOAD" }
        require(revision >= 0) { "negative revision $revision" }
        val out = ByteArray(HEADER_SIZE + payload.size + DIGEST_SIZE)
        MAGIC.copyInto(out, 0)
        putLe(out, 4, VERSION.toLong(), 2)
        putLe(out, 6, schema.toLong(), 2)
        putLe(out, 8, revision, 8)
        putLe(out, 16, payload.size.toLong(), 4)
        payload.copyInto(out, HEADER_SIZE)
        sha256(out, HEADER_SIZE + payload.size).copyInto(out, HEADER_SIZE + payload.size)
        return out
    }

    fun decode(bytes: ByteArray, schema: Int = SCHEMA_KBA_V1): Result {
        if (bytes.size >= MAGIC.size && (0 until MAGIC.size).all { bytes[it] == MAGIC[it] }) {
            if (bytes.size < HEADER_SIZE + DIGEST_SIZE)
                return Result.Rejected("truncated: ${bytes.size} B")
            val version = getLe(bytes, 4, 2).toInt()
            if (version != VERSION) return Result.Rejected("unsupported envelope v$version")
            val got = getLe(bytes, 6, 2).toInt()
            if (got != schema) return Result.Rejected("schema $got, expected $schema")
            // Bounds-check the declared length before trusting it.
            val len = getLe(bytes, 16, 4)
            if (len > MAX_PAYLOAD) return Result.Rejected("payload length $len > $MAX_PAYLOAD")
            val n = len.toInt()
            if (bytes.size != HEADER_SIZE + n + DIGEST_SIZE)
                return Result.Rejected("size ${bytes.size} B, header says ${HEADER_SIZE + n + DIGEST_SIZE}")
            val expect = sha256(bytes, HEADER_SIZE + n)
            val stored = bytes.copyOfRange(HEADER_SIZE + n, HEADER_SIZE + n + DIGEST_SIZE)
            if (!MessageDigest.isEqual(expect, stored)) return Result.Rejected("digest mismatch")
            val revision = getLe(bytes, 8, 8)
            if (revision < 0) return Result.Rejected("negative revision")
            return Result.Ok(Decoded(bytes.copyOfRange(HEADER_SIZE, HEADER_SIZE + n), revision, false))
        }
        if (schema == SCHEMA_KBA_V1 && bytes.size == LEGACY_KBA_SIZE)
            return Result.Ok(Decoded(bytes.copyOf(), 0L, true))
        return Result.Rejected("unrecognised save (${bytes.size} B, no envelope)")
    }

    private fun sha256(b: ByteArray, len: Int): ByteArray =
        MessageDigest.getInstance("SHA-256").apply { update(b, 0, len) }.digest()

    private fun putLe(b: ByteArray, off: Int, v: Long, n: Int) {
        for (i in 0 until n) b[off + i] = (v ushr (8 * i)).toByte()
    }

    private fun getLe(b: ByteArray, off: Int, n: Int): Long {
        var v = 0L
        for (i in n - 1 downTo 0) v = (v shl 8) or (b[off + i].toLong() and 0xFF)
        return v
    }
}
