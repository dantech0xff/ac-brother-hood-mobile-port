package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 379 — the death screen waits for the death animation. Nothing in
 * `k.I()` reads `x[1]`: the player's head does — `g() → i(50)` on the
 * ground (g.java:576-578) — and the S50/S241 arm calls `k.l(12)` when the
 * animation ends (`r()`, g.java:2200-2219). The port opened the death
 * screen the frame the meter reached 0, so S50 never showed.
 */
class Slice379Test {
    @Test fun `the S50 death anim plays out before the fail screen`() {
        val w = world()
        settleIntro(w)
        repeat(20) {
            w.player.applyHit(18, 0, null, w)
            w.player.gt = 0; w.iBh = 0
        }
        assertEquals(0, w.player.x1)
        w.tick(emptyList())
        assertEquals(50, w.player.S, "g() → i(50) at the next head")
        assertFalse(w.failed, "no knockout check in k.I()")
        var n = 0
        while (!w.failed && n < 200) {
            assertEquals(50, w.player.S, "the anim runs to its end")
            w.tick(emptyList()); n++
        }
        assertTrue(w.failed, "S50 r() → k.l(12)")
        val c = w.player.clip!!
        var frames = 0
        for (f in 0 until c.frameCount(50)) frames += maxOf(1, c.frameDuration(50, f))
        assertTrue(n >= frames - 2, "the whole anim played ($n ticks for $frames)")
    }
}
