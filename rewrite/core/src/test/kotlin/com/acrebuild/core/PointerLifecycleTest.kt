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

/** Pointer-field lifecycle regression (Devin Review, PR #52): `k.H/k.I`
 *  are the pointer-RELEASE point held for exactly one frame
 *  (k.java:548-553 writes them on pointerReleased; k.java:1874-77 copies
 *  and clears `ch/ci` every frame). A DOWN-only tap leaves no residue —
 *  stale taps must not resolve later `k.c`/`k.j` polls. */
class PointerLifecycleTest {
    @Test fun `k H,I fill on release and clear at tick end`() {
        val w = world()
        // DOWN alone must not populate the release fields
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 100, 100)))
        assertEquals(-1, w.lastTouchX)
        assertEquals(-1, w.lastTouchY)
        // even when set mid-tick (release path), fields clear at tick end
        w.lastTouchX = 50; w.lastTouchY = 60
        w.tick(emptyList())
        assertEquals(-1, w.lastTouchX)
        assertEquals(-1, w.lastTouchY)
    }

    @Test fun `k J,K persist while touching and clear after release`() {
        val w = world()
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 30, 40)))
        assertEquals(30, w.lastMoveX); assertEquals(40, w.lastMoveY)
        w.tick(emptyList())                       // held: position kept
        assertEquals(30, w.lastMoveX)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.UP, 50, 60)))
        // k.java:1509-1514 — J=cj samples the release point, THEN `cl`
        // clears cj/ck for the following frame.
        assertEquals(50, w.lastMoveX); assertEquals(60, w.lastMoveY)
        w.tick(emptyList())
        assertEquals(-1, w.lastMoveX)
        assertEquals(-1, w.lastMoveY)
    }
}
