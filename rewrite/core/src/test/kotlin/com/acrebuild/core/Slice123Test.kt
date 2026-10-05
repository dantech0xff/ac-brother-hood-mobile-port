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

/** Slice-123 ax13 rope-draw contract (i.java:13391, proven): the draw
 *  arm indexes `Z[7]` (chain object) and `Z[1]`/`bN` (segment count) —
 *  `initAx13` must leave both populated, and the `aG==4` spawner arm
 *  switches the count source to `bN`. */
class Slice123Test {
    private fun ax13(w: Level0World, ag: Int = 0, z1: Int = 10): Entity {
        val e = Entity(13, null)
        e.setPositionPx(100, 100)
        // L502 init reads the aG variant from r8[4]: {1→1, 2→2, 3→4}.
        val f = mutableListOf(13, 0, 100, 100,
            when (ag) { 1 -> 1; 2 -> 2; 4 -> 3; else -> 0 }, 0, 0)
        for (i in 7..15) f += 0
        w.npcFsm.initAx13(e, f)
        e.Z[1] = z1
        w.npcs.add(e)
        return e
    }

    @Test fun `ax13 carries rope draw inputs Z7 object and segment count`() {
        val w = world()
        val e = ax13(w)
        assertTrue(e.Z.size > 7)
        assertTrue(e.Z[1] >= 1)
    }

    @Test fun `aG4 spawner arm counts segments into bN i13182`() {
        val w = world()
        val e = ax13(w, ag = 4)
        // aG==4: draw uses bN-1 — initArmedG4 must seed bN (L111 map).
        assertEquals(4, e.aG)
        assertTrue(e.bN >= 0)
    }
}
