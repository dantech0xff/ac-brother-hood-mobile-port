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

class Slice171Test {
    // ax42 records spawn clipless (bi[42]=-1) so the win fuse exists in
    // the world; in the original every record spawns even without a clip.
    @Test fun `ax42 win fuse spawns clipless in level 0`() {
        val w = world()
        w.stateL(8)
        val fuse = w.npcs.firstOrNull { it.ax == 42 }
        assertTrue(fuse != null)
        // `i(r8[5]) = i(0)` runs in the ctor tail even without a clip
        // (slice 389) — the fuse is armed from spawn, not dormant.
        assertEquals(0, fuse.S)
        assertEquals(1, fuse.Z[0])                  // kind-1: fires on s.P&32 clear
        assertEquals(531, fuse.Z[1])                // watches aw531 flag pickup
    }

    // Win chain (proven): script arms the fuse (i(0)) + clears the flag's
    // bit32 (op100 arg1 / au() drop) -> kAJ 1->2 -> kAL*1000 countdown
    // -> screenL(13). Verified end-to-end on the real level-0 records.
    @Test fun `fuse arms then fires win after countdown`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val fuse = w.npcs.firstOrNull { it.ax == 42 }!!
        val flag = w.npcs.firstOrNull { it.aw == 531 }!!
        fuse.setAnim(0)                             // claim-script i(0) arm
        flag.P = flag.P and 32.inv()                // op100 arg1: flag used
        var ticks = 0
        while (ticks < 1600 && !w.won) { w.tick(listOf()); ticks++ }
        assertTrue(w.won)
        assertEquals(-1, w.kBw)
        assertEquals(58, w.kBx)
    }

    @Test fun `fuse stays dormant while flag holds bit32`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val fuse = w.npcs.firstOrNull { it.ax == 42 }!!
        fuse.setAnim(0)
        for (t in 0 until 120) w.tick(listOf())     // flag P=160 keeps bit32
        assertEquals(0, w.kAJ)
        assertFalse(w.won)
    }
}
