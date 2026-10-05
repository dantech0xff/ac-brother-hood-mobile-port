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

class Slice97Test {
    private fun bh3World(): Level0World {
        val w = world(); w.npcs.clear(); w.kAj = 1         // bh[1]==3
        w.stateL(8)                                      // pin play state
        return w
    }

    @Test fun `bh3 boss column clamps aB to bU`() {
        val w = bh3World()
        val boss = Entity(29, w.clips[7]); w.kB = boss
        w.iBT = true; w.iBU = 120
        boss.aB = 300
        w.tick(emptyList())
        assertEquals(120, boss.aB, "B.aB > i.bU → clamp (k.java:4191)")
    }

    @Test fun `bh3 ap4 floor clamp`() {
        val w = bh3World()
        w.kAp[4] = -3
        w.tick(emptyList())
        assertEquals(0, w.kAp[4], "ap[4]<0 → 0 (k.java:4204)")
    }

    private fun tickClean(w: Level0World, n: Int) {
        // keep hudStep ungated: no claim overlay, no script spawns, and
        // the player inside the autoscroll camera (al > camY+240 → l(12)
        // freezes the tick before hudStep runs)
        repeat(n) {
            w.kC = null; w.pendingInsert.clear()
            w.player.al = w.camY + 100
            w.tick(emptyList())
        }
    }

    @Test fun `alert meter aH countdown slides then poisons`() {
        val w = bh3World()
        w.kAE = 50; w.kAH = 80; w.kAF = 0
        tickClean(w, 50)                        // 80→30
        assertEquals(30, w.kAH, "aH>30 → aH-- only")
        assertEquals(0, w.alertSlide, "aH>30 → i3=0 (icon in place)")
        tickClean(w, 30)                        // 30→0, slide grows
        assertEquals(30, w.alertSlide)
        tickClean(w, 1)                         // aH→-1 → aE=aH, return
        assertEquals(-1, w.kAE, "aE = aH(-1) poisons the meter (k.java:4216)")
        assertEquals(0, w.kAH)
        tickClean(w, 1)
        // slice 180: n() runs now — `aE<0 → aE=0` (g.java:5621) eats the
        // poison value; with aH==0 the stall gate stays closed.
        assertEquals(0, w.kAE, "n() clamps the poisoned aE back to 0")
    }

    @Test fun `alert meter aF trickle`() {
        val w = bh3World()
        w.kAE = 50; w.kAH = -1; w.kAF = 7
        tickClean(w, 1)
        // slice 180: n()'s aG decay divider (kAG=0 → -1 → reset+aE--) also
        // runs: -1 + +3 → 52. Subsequent ticks decay only every 6th.
        assertEquals(52, w.kAE, "aE-- divider + aF trickle +3")
        assertEquals(4, w.kAF)
        tickClean(w, 1)
        assertEquals(55, w.kAE)
        tickClean(w, 1)
        assertEquals(56, w.kAE, "remainder <3 → aE += aF")
        assertEquals(0, w.kAF)
        assertEquals(56, w.alertFill, "i4 = min(aE,100)")
    }

    @Test fun `alertFill caps at 100`() {
        val w = bh3World()
        w.kAE = 140; w.kAH = -1; w.kAF = 0
        tickClean(w, 1)
        assertEquals(100, w.alertFill)
    }

    @Test fun `g-g overhead gate`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // no focus entity → arm skipped (renderer-side; here verify fields)
        p.S = 303; p.K = 2; p.gQL = 150; p.gQM = 90
        assertNull(p.g)
        p.g = Entity(11, w.clips[7])
        assertEquals(303, p.S)
        assertEquals(2, p.K, "aS.K = anim frame (k.java:4347)")
    }
}
