package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

// ---- slice 43a — script tables `by`/`bz`/`eH` (k.java:6196-6320) -------
class ScriptTablesTest {

    private fun tables() = ScriptTables.load(
        java.io.File("../generated/level0/scripts.bin").readBytes())

    @Test fun `loader consumes the whole entry`() {
        val t = tables()
        assertEquals(13, t.eH.size, "pack-6 entry-007 carries 13 scripts")
    }

    @Test fun `eH carries the level-0 script uids`() {
        val t = tables()
        assertEquals(938, t.eH[11], "gondola script uid 938 at index 11")
        assertEquals(listOf(116, 47, 250, 300, 313, 532, 105, 104, 927,
            579, 327, 938, 52), t.eH.toList())
    }

    @Test fun `s resolves script uid to index or -1`() {
        val t = tables()
        assertEquals(11, t.s(938))
        assertEquals(0, t.s(116))
        assertEquals(-1, t.s(9999))
    }

    @Test fun `t returns eI op payload lengths`() {
        val t = tables()
        assertEquals(6, t.t(100)); assertEquals(2, t.t(101))
        assertEquals(9, t.t(106)); assertEquals(8, t.t(109))
        assertEquals(4, t.t(114))
    }

    @Test fun `bz seeds the PC on the first step-group`() {
        val t = tables()
        for (s in t.by.indices) for (b in t.by[s].indices) {
            val blk = t.by[s][b]
            val type = blk[0].toInt() and 0xFF
            val expect = if (type == 2 || type == 3) 6 else 4
            assertEquals(expect, t.bz[s][b],
                "script $s block $b type $type header")
            // The offset must land on a group key — before the size tail.
            assertTrue(t.bz[s][b] < blk.size - 2,
                "script $s block $b: PC inside content")
        }
    }

    @Test fun `script 938 blocks parse to plausible ops`() {
        val t = tables()
        val by = t.by[11]
        assertTrue(by.isNotEmpty(), "gondola script has blocks")
        // Every op byte in every block must be a known opcode shape —
        // re-walk the groups to prove the layout parses cleanly.
        for (blk in by) {
            var pc = t.bz[11][t.by[11].indexOf(blk)]
            val groups = (blk[pc - 2].toInt() and 0xFF) or
                ((blk[pc - 1].toInt() and 0xFF) shl 8)
            var seen = 0
            while (pc < blk.size - 2 && seen < groups) {
                pc += 3                            // key16 + count8
                val nops = blk[pc - 1].toInt() and 0xFF
                for (o in 0 until nops) {
                    val op = blk[pc].toInt() and 0xFF
                    pc += 1 + if (op >= 100) ScriptTables.EI[op - 100]
                              else when (op) {
                                  11, 12, 21, 25, 31, 41 -> 4
                                  13 -> 5
                                  22, 32, 42 -> 2
                                  23, 24, 43, 44 -> 4
                                  34, 35, 36 -> 2 + 2 * ((op - 34) % 3 + 1)
                                  37, 38, 39 -> 2 * ((op - 34) % 3 + 1)
                                  else -> 0
                              }
                }
                seen++
            }
            assertEquals(blk.size - 2, pc, "block content ends at the tail")
        }
    }

    @Test fun `world exposes the tables through LevelCellSource`() {
        val w = world()
        assertEquals(13, w.kEh.size)
        assertEquals(938, w.kEh[11])
        assertEquals(11, w.kSIndex(938))
        assertEquals(-1, w.kSIndex(9999))
        assertNotNull(w.claimOps(11), "claimOps → kBz[11] group offsets")
        assertNull(w.claimOps(99))
        assertEquals(6, w.kT(100))
    }

    @Test fun `bindScript latches cd7 only on first alloc`() {
        val w = world()
        val e = Entity(11, w.clips[7])
        e.setPositionPx(100, 100)
        w.npcs.add(e)
        e.bindScript(11, w)
        assertEquals(11, e.ca)
        assertTrue(e.cd[7])
        assertNotNull(e.scriptOps, "reloadScriptOps seeds cL from kBz")
        // a second bind (post-release) must NOT re-set cd[7] semantics —
        // the original's `cd == null` alloc check fires exactly once.
        e.cd[7] = false
        e.claimLatchX = -1                     // simulate bI() release
        e.bindScript(5, w)
        assertFalse(e.cd[7], "cd[7] writes only on the first alloc")
        assertEquals(5, e.ca)
    }
}

// =====================================================================
// Slice 43b — i.aa() claim-script interpreter (the k.by VM).
// =====================================================================
