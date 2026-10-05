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

class Slice236Test {

    @Test fun `ax37 triggers take the L633 bound init`() {
        // L633 (i.java:8074): Z[0..3]=r8[15..18], P|=0x200, and P|=0x10
        // when the record's P bits lack 0x20. L1bea SKIPS ax37 — the
        // entity must not take its record anim (S stays the ctor head's -1).
        val w0 = world(aj = 0)
        val t = w0.npcs.firstOrNull { it.ax == 37 && it.aw == 96 }
            ?: error("level0 ax37 aw=96 missing")
        assertEquals(4, t.Z[0]); assertEquals(0, t.Z[1])
        assertEquals(-1, t.Z[2]); assertEquals(0, t.Z[3])
        assertTrue((t.P and 512) != 0)
        assertTrue((t.P and 16) != 0)                       // r8[6]=0 → flag set
        assertEquals(-1, t.S)                               // L1bea skips ax37
        // level2 aw=119 carries r8[6]=32 → the P|=0x10 arm must not fire.
        val w2 = world(aj = 2)
        val gated = w2.npcs.firstOrNull { it.ax == 37 && it.aw == 119 }
            ?: error("level2 ax37 aw=119 missing")
        assertTrue((gated.P and 512) != 0)
        assertTrue((gated.P and 16) == 0)
        assertEquals(109, gated.Z[2])                       // r8[17]
    }

    @Test fun `else arm records take zero Z fill`() {
        // L1bc7→L1bea (i.java:11476/11489): unknown-routed types get the
        // `i(r8[5])` finish only — Z stays the ctor's zero array. The
        // earlier generic Z-fill wrote r8[7+i] — unfaithful, removed.
        val w = world(aj = 1)
        val stray = w.npcs.firstOrNull { it.ax == 65 }
            ?: error("level1 ax65 missing")
        assertEquals(0, stray.S)                            // r8[5]=0 → i(0)
        for (i in 0..3) assertEquals(0, stray.Z[i],
            "ax65 Z[$i] should stay ctor-zero")
    }
}
