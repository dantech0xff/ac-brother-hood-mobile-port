package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 383 — `k.N`, the tap prompt (`k.c(III)`/`k.k(I)`, k.java:694-719),
 * is one object, ticked in `k.I()` after the player (k.java:2601-2619):
 * a release inside its 50×50 box (or the 32 bit held) plays S55 and the
 * tap fires `E(32)` — the context press `i.k()` reads, so tapping the
 * prompt above a target assassinates it; S55 two frames from its end
 * removes it; a finger held over it shows the held frame (`P|=64; q()`).
 * Only `k.k()` clears it — it outlives a reload. The port kept two copies
 * of it, never ticked it and never drew it.
 */
class Slice383Test {
    private fun ev(seq: Long, type: InputQueue.Type, x: Int, y: Int) =
        InputQueue.Event(seq, type, x, y)

    /** A prompt at screen (200,60) — clear of the pad and soft keys. */
    private fun promptWorld(): Level0World {
        val w = world()
        settleIntro(w)
        w.showPrompt(w.camX + 200, w.camY + 60, 9_999)
        return w
    }

    @Test fun `a tap on the prompt plays S55 and fires the context press`() {
        val w = promptWorld()
        val n = w.kN!!
        val sx = n.ak - w.camX; val sy = n.al - w.camY
        w.tick(listOf(ev(0, InputQueue.Type.DOWN, sx, sy), ev(1, InputQueue.Type.UP, sx, sy)))
        assertEquals(55, n.S, "N.i(55) (k.java:2603-2605)")
        w.tick(emptyList())
        assertTrue(w.pad.v(32), "E(32) — the press i.k() reads as v(65568)")
    }

    @Test fun `S55 plays out and the prompt removes itself`() {
        val w = promptWorld()
        val n = w.kN!!
        n.setAnim(55)
        val frames = n.clip!!.frameCount(55)
        var t = 0
        while (w.kN != null && t++ < 200) w.tick(emptyList())
        assertNull(w.kN, "N.S == 55 && T == len-2 → k(-1) (k.java:2609-2610)")
        assertTrue(frames >= 2)
    }

    @Test fun `a finger held over the prompt shows its held frame`() {
        val w = promptWorld()
        val n = w.kN!!
        val sx = n.ak - w.camX; val sy = n.al - w.camY
        w.tick(listOf(ev(0, InputQueue.Type.DOWN, sx, sy)))
        assertEquals(64, n.P and 64, "k(J,K) → P |= 64 (k.java:2611-2613)")
        assertEquals(n.clip!!.frameCount(n.S) - 1, n.T, "q() → last frame")
        w.tick(listOf(ev(1, InputQueue.Type.UP, 5, 5)))
        w.tick(emptyList())
        assertEquals(54, n.S)
        assertEquals(0, n.P and 64, "released elsewhere → i(54); P &= -65")
    }

    @Test fun `only k-k() clears the prompt, a reload does not`() {
        val w = promptWorld()
        w.resetLevel(true)
        assertNotNull(w.kN, "k.N is not reset by a(z2) (k.java:5139-5232)")
        w.clearPrompt(1)
        assertNotNull(w.kN, "k.k(aw) only for the bound uid")
        w.clearPrompt(9_999)
        assertNull(w.kN)
    }
}
