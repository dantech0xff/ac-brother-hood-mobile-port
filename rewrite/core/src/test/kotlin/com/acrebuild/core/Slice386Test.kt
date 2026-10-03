package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Slice 386 — `i.s()` (i.java:293-331): on the pause screen only
 * `k.A[3]`'s anims advance; a cycle that wraps during a jc21 dialog other
 * than u8 stops the ground player (and settles an airborne one) and, with
 * a claim bound, freezes the entity on frame 0 (`P |= 64`).
 */
class Slice386Test {
    /** An entity one tick before its anim wraps. */
    private fun aboutToWrap(w: Level0World): Entity {
        val e = Entity(30, w.clips[7]).apply { aw = 9_300; setAnim(0) }
        val c = e.clip!!
        e.T = c.frameCount(0) - 1
        e.U = maxOf(1, c.frameDuration(0, e.T)) - 1
        return e
    }

    @Test fun `anims do not advance on the pause screen`() {
        val w = world()
        settleIntro(w)
        w.stateL(14)
        val e = aboutToWrap(w)
        val t = e.T; val u = e.U
        e.advanceAnim()
        assertEquals(t to u, e.T to e.U, "j.c == 14 and not A[3] (i.java:294)")
    }

    @Test fun `a wrap under a claim dialog freezes the entity and stops the player`() {
        val w = world()
        settleIntro(w)
        w.kC = Entity(5, null).apply { aw = 9_301 }
        w.autoDismissDialog = false
        w.dlgU = 1
        w.stateL(21)
        w.player.ag = 1024; w.player.ah = 512
        val e = aboutToWrap(w)
        e.advanceAnim()
        assertEquals(0, e.T)
        assertEquals(64, e.P and 64, "k.C != null → P |= 64 (i.java:328-330)")
        assertEquals(0, w.player.ag); assertEquals(0, w.player.ah)
        val t = e.T
        repeat(5) { e.advanceAnim() }
        assertEquals(t, e.T, "frozen until its state changes")
    }

    @Test fun `play frames and u8 tips keep looping`() {
        val w = world()
        settleIntro(w)
        w.kC = Entity(5, null).apply { aw = 9_302 }
        val e = aboutToWrap(w)
        e.advanceAnim()
        assertEquals(0, e.P and 64, "j.c == 8 → plain wrap")
        w.autoDismissDialog = false
        w.dlgU = 8
        w.stateL(21)
        val e2 = aboutToWrap(w)
        e2.advanceAnim()
        assertEquals(0, e2.P and 64, "u == 8 → plain wrap (i.java:315)")
    }
}
