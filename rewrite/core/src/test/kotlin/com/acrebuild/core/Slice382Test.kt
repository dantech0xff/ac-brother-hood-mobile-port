package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Slice 382 — one `k.e(i,i2)` (k.java:3264-3269: `ap[0]++` unless
 * `i2 <= 0 || aj == 7`) and one `k.o(n)` (k.java:3257-3262: `ap[n]++`,
 * except `n == 3` on `aj == 7`). Six NPC kill sites fed a duplicate
 * counter nothing read, so those kills never reached the stats screen's
 * "ENEMIES KILLED" row; four `o(3)` sites skipped the mission-7 guard.
 */
class Slice382Test {
    @Test fun `a soldier's S20 death counts on the stats screen`() {
        val w = world()
        settleIntro(w)
        val e = w.npcs.first { it.ax == 11 && it.aB > 0 }
        assertEquals(true, e.aw > 0, "precondition: a record uid")
        e.setAnim(20)
        e.T = (e.clip?.frameCount(20) ?: 1) - 1
        e.U = (e.clip?.frameDuration(20, e.T) ?: 1) - 1
        val before = w.kAp[0]
        w.npcFsm.tick(e, w.player)
        assertEquals(139, e.S, "S20 r() → i(139) corpse (i.java:6195-6203)")
        assertEquals(before + 1, w.kAp[0], "k.e(0,aw) → ap[0]++")
    }

    @Test fun `mission 7 counts neither kills nor silent kills`() {
        val w = world()
        settleIntro(w)
        val e = w.npcs.first { it.ax == 11 && it.aB > 0 }
        w.kAj = 7
        val k0 = w.kAp[0]; val k3 = w.kAp[3]
        e.applyHit6(49, e.ak, null, w)
        assertEquals(k0, w.kAp[0], "k.e: aj == 7 → no count")
        assertEquals(k3, w.kAp[3], "k.o(3): aj == 7 → no count")
        w.kAj = 0
        e.applyHit6(49, e.ak, null, w)
        assertEquals(k0 + 1, w.kAp[0])
        assertEquals(k3 + 1, w.kAp[3])
    }
}
