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

class Slice237Test {

    @Test fun `ax2 init takes the Ld7f arm verbatim`() {
        // i.java:9177 — `az=300; P|=0x80; Z=new int[1]; Z[0]=r8[7]`.
        // Level-0's records carry r8[7]=-1 (dead director link) and
        // r8[5]=0 → S=0; clip1 (bi[2]=1) gives the real 40x128 W box
        // the `aY()` overlap gate reads.
        val w = world()
        val e = w.npcs.firstOrNull { it.ax == 2 && it.aw == 98 }
            ?: error("level0 ax2 aw=98 missing")
        assertEquals(300, e.az)
        assertTrue((e.P and 128) != 0, "Ld7f arms P|0x80")
        assertEquals(-1, e.Z[0])
        assertEquals(0, e.S)
        assertEquals(e.ak, e.W[0])
        assertEquals(e.al - 100, e.W[1])
        assertEquals(e.ak + 40, e.W[2])
        assertEquals(e.al + 28, e.W[3])
    }

    @Test fun `aY fires through the entity tick and self-removes`() {
        // i.java:38143 — `k.c(this)` inside aY(): the fired checkpoint
        // entity tombstones its slot AND leaves bb[] — it cannot
        // re-fire, and the record does not respawn on reload.
        val w = world()
        val cp = w.checkpoints.first()
        val e = w.npcs.first { it.ax == 2 && it.aw == cp.aw }
        overlapCheckpoint(w, cp)
        w.tick(emptyList())
        assertTrue(cp.consumed)
        assertTrue(w.kFS in 0..1, "fS armed; the frame's tail may type one char")
        assertNotNull(w.checkpointSnap)
        w.tick(emptyList())                    // drain pendingRemove
        assertNull(w.npcs.firstOrNull { it === e },
            "k.c(this) removes the fired checkpoint entity")
    }
}
