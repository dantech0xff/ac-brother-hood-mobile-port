package com.acrebuild.core

/**
 * `k.by`/`k.bz`/`k.eH` — the per-mission claim-script table loaded by
 * the pack-type-8 branch of `k`'s loader (k.java:6196-6320, proven for
 * the format; the `by`/`bz` build loop is proven transcription).
 *
 * Source = `j.e(7)` of the mission pack (`ec[aj]`, k.java:8434):
 * `assassins_creed` pack-6 `entry-007-marker-003.bin` for level 0.
 *
 * Stream layout (verified to consume the 4371-byte entry exactly):
 * ```
 * u8                 scriptCount
 * per script:        [eH:u16 script uid][u8 blockCount][u16 pad]
 * per block:         [u8 type][u8 pad][u16 targetUid (type 2/3 only)]
 *                    [u16 groupCount]
 *                    per group: [key:u16 step][u8 opCount][opCount ops]
 * op payload sizes:  11/12/21/25/31/41 → 4B (x,y)
 *                    13 → 5B | 22/32/42 → 2B (anim) | 23/24/43/44 → 4B
 *                    34/35/36 → 2 + 2*argc (uid + args)
 *                    37/38/39 → 2*argc (args only)
 *                    >=100 → eI[op-100]
 * ```
 * `by[s][b]` = the raw block bytes + a 2-byte tail = `u16(bz[s][b])` —
 * the byte offset of the first step-group inside the block (4 for type
 * 0/1, 6 for type 2/3). PROVEN from javap: `iload_8 - iload_6` at
 * k.javap.txt:23619 = header-end minus block-start; both decompilers
 * merged the two locals and printed `r72 - r07` = block size instead.
 * The interpreter's done-check `cL[b] >= len-2` compares against the
 * buffer size, so the tail's VALUE is dead — but it is `bz` in the
 * original, and `bz` is also what `bJ()` seeds the per-block PC with.
 *
 * `bz[s][b]` = that same first-group offset (proven — see above; a
 * size-seeded PC would complete every block instantly).
 */
class ScriptTables(
    /** `k.eH` — script uid per script index; `k.s(uid)` scans this. */
    val eH: IntArray,
    /** `k.by` — op-blocks per script, each block = raw bytes + size tail. */
    val by: Array<Array<ByteArray>>,
    /** `k.bz` — first-group offset per block (seed for `i.cL`). */
    val bz: Array<IntArray>,
) {
    /** `k.s(uid)` (k.java:7149, proven): linear scan of `eH`, -1 miss. */
    fun s(uid: Int): Int = eH.indexOf(uid)

    /** `k.t(op)` (k.java:7162, proven): `eI[op-100]` payload length. */
    fun t(op: Int): Int = EI[op - 100]

    companion object {
        /** `k.eI` (k.java:8466, proven): payload lengths for ops ≥100. */
        val EI = intArrayOf(6, 2, 4, 2, 2, 4, 9, 2, 4, 8, 4, 9, 6, 4, 4)

        /** Payload length for ops <100 (0 for unlisted — k.java:6242-L80). */
        private fun smallOpLen(op: Int): Int = when (op) {
            11, 12, 21, 25, 31, 41 -> 4
            13 -> 5
            22, 32, 42 -> 2
            23, 24, 43, 44 -> 4
            34, 35, 36 -> 2 + 2 * ((op - 34) % 3 + 1)
            37, 38, 39 -> 2 * ((op - 34) % 3 + 1)
            else -> 0
        }

        /** Verbatim port of the `by`/`bz`/`eH` build loop (k.java:6196). */
        fun load(d: ByteArray): ScriptTables {
            var pc = 1
            val count = d[0].toInt() and 0xFF
            val eH = IntArray(count)
            val by = Array(count) { arrayOf<ByteArray>() }
            val bz = Array(count) { IntArray(0) }
            fun u16(p: Int) = (d[p].toInt() and 0xFF) or
                ((d[p + 1].toInt() and 0xFF) shl 8)
            for (s in 0 until count) {
                eH[s] = u16(pc)
                val blockCount = d[pc + 2].toInt() and 0xFF
                pc += 5                                       // eH + count + pad16
                by[s] = Array(blockCount) { ByteArray(0) }
                bz[s] = IntArray(blockCount)
                for (b in 0 until blockCount) {
                    val start = pc
                    val type = d[pc].toInt() and 0xFF
                    val header = if (type == 2 || type == 3) 6 else 4
                    val groupCount = u16(pc + header - 2)
                    pc += header
                    bz[s][b] = header                          // first-group PC
                    for (g in 0 until groupCount) {
                        pc += 3                                // key16 + count8
                        val opCount = d[pc - 1].toInt() and 0xFF
                        for (o in 0 until opCount) {
                            val op = d[pc].toInt() and 0xFF
                            pc += 1 + if (op >= 100) EI[op - 100]
                                      else smallOpLen(op)
                        }
                    }
                    val len = pc - start
                    val block = ByteArray(len + 2)
                    d.copyInto(block, 0, start, start + len)
                    block[len] = (header and 0xFF).toByte()    // u16(bz)
                    block[len + 1] = ((header shr 8) and 0xFF).toByte()
                    by[s][b] = block
                }
            }
            require(pc == d.size) { "trailing ${d.size - pc} bytes in script table" }
            return ScriptTables(eH, by, bz)
        }
    }
}
