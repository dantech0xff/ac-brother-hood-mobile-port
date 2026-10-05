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

/** Slice 88 — `j.t` + `j.i()` fail/win frame skip (j.java:105-345,
 *  k.java:1109, proven). Slice 376 corrected the reading: the pointer
 *  handlers never touch `j.t` (k.java:486-516); only the veil latch
 *  (bit 0) and `K()` (bit 4) set it. */
class Slice88Test {

    @Test fun `pad presses and releases leave jT alone`() {
        val w = world()
        val (x, y) = w.cellPoint(0)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y)))
        assertEquals(0, w.jT, "pointerPressed has no j.t write (k.java:486-494)")
        w.tick(listOf(InputQueue.Event(1, InputQueue.Type.UP, x, y)))
        assertEquals(0, w.jT)
    }

    @Test fun `fail screen skips its first frame after K()`() {
        val w = world()
        w.jT = 16                                // K() at load (k.java:2674)
        w.stateL(12)
        // the skipped frame: `j.i()` true → `j.t=0`, no b(true)/L()/Q()
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 165),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 165)))
        assertEquals(0, w.jT)
        assertEquals(12, w.jC, "the tap on the skipped frame is not read")
        // next frames dispatch normally (NO row → menu)
        w.tick(listOf(
            InputQueue.Event(2, InputQueue.Type.DOWN, 200, 165),
            InputQueue.Event(3, InputQueue.Type.UP, 200, 165)))
        assertEquals(2, w.jC)
    }

    @Test fun `a manually latched bit flushes on the next frame`() {
        val w = world()
        w.stateL(12)
        w.jT = 1 shl 2                        // any bit: `j.i()` = `t != 0`
        w.tick(emptyList())
        // `j.i()` true → `j.t=0`, frame skipped; next tap dispatches
        assertEquals(0, w.jT)
        assertEquals(12, w.jC)
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 165),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 165)))
        assertEquals(2, w.jC)
    }
}
