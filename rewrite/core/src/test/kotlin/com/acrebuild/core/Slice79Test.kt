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

// ---------------------------------------------------------------------------
// slice 79 — verbatim pointer pipeline: E() word model, j() wheel resolver,
// soft-key literals, release flush, x() double-press (k.java:553/:576/:1594)
// ---------------------------------------------------------------------------
class Slice79Test {

    private fun down(w: Level0World, x: Int, y: Int) =
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y)))
    private fun up(w: Level0World, x: Int, y: Int) =
        w.tick(listOf(InputQueue.Event(1, InputQueue.Type.UP, x, y)))
    private fun tap(w: Level0World, cell: Int) {
        val (x, y) = w.cellPoint(cell)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y),
                      InputQueue.Event(1, InputQueue.Type.UP, x, y)))
    }

    @Test fun `wheel cells resolve to E(2 shl cell) masks`() {
        val w = world()
        for (cell in 0..8) {
            val (x, y) = w.cellPoint(cell)
            assertEquals(cell, w.resolvePadZone(x, y), "cell $cell")
        }
    }

    @Test fun `bC held persists after the edge frame`() {
        val w = world()
        val (x, y) = w.cellPoint(5)                       // MR cell
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y)))
        assertTrue(w.pad.v(64))                           // edge live
        assertTrue(w.pad.u(64))                           // sticky held
        w.tick(emptyList())
        assertFalse(w.pad.v(64))                          // edge consumed
        assertTrue(w.pad.u(64), "bC must stay held while pointerDown")
    }

    @Test fun `releaseFlush lands the eM released edge`() {
        val w = world()
        val (x, y) = w.cellPoint(5)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y)))
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.UP, x, y)))
        assertTrue(w.pad.w(64), "eM carries the released bit")
        assertFalse(w.pad.u(64))
        w.tick(emptyList())
        assertFalse(w.pad.w(64))                          // one frame only
    }

    @Test fun `x() double-press within 5 frames`() {
        val w = world()
        tap(w, 2); w.tick(emptyList())
        tap(w, 2)
        assertTrue(w.pad.x(8), "two taps of cell2 (mask 2<<2=8) inside eP<5")
    }

    @Test fun `x() expires after 5 frames`() {
        val w = world()
        tap(w, 2)
        repeat(6) { w.tick(emptyList()) }
        tap(w, 2)
        assertFalse(w.pad.x(8), "second tap too late — eP>=5")
    }

    @Test fun `margins and pause rect excluded`() {
        val w = world()
        assertEquals(-1, w.resolvePadZone(50, 220), "left margin y>=207")
        assertEquals(-1, w.resolvePadZone(350, 220), "right margin y>=207")
        assertEquals(-1, w.resolvePadZone(370, 10), "pause rect 354,0,46,37")
        assertEquals(-1, w.resolvePadZone(-1, -1), "unset pointer")
        assertEquals(-1, w.resolvePadZone(200, 240), "y >= 240")
    }

    @Test fun `pause rect tap emits M_PAUSE edge and l(14)`() {
        val w = world()
        // `J()` hit-tests the release point (`c()` reads k.H/k.I) after
        // `I()`; the E(262144) lands in bB at the next commit, so the
        // pause opens one frame after the release (slice 368).
        down(w, 370, 10)
        assertEquals(8, w.jC, "a press alone arms nothing (pointerPressed = wheel only)")
        up(w, 370, 10)
        assertEquals(8, w.jC, "release frame: E(262144) armed, v() still clear")
        w.tick(emptyList())
        assertEquals(14, w.jC, "k.java:1056 — pause icon → k.l(14)")
    }

    @Test fun `marker zone eats the tap`() {
        val w = world()
        w.cm = 0                                             // marker eat is !k() only
        val m = Entity(14, w.clips[9]).apply { setPositionPx(w.player.ak, w.player.al) }
        w.kN = m
        // tap inside the marker's 50x50 box around its position
        val mx = (m.ak - w.camX); val my = (m.al - w.camY)
        assertEquals(-1, w.resolvePadZone(mx, my))
        w.kN = null
    }

    @Test fun `interact anchor zone eats the tap`() {
        val w = world()
        w.cm = 0                                             // anchor eat is !k() only
        w.setInteractAnchor(w.player.ak, w.player.al)          // i.o()
        assertEquals(-1, w.resolvePadZone(w.player.ak - w.camX, w.player.al - w.camY))
        w.clearInteractAnchor()                                // i.U()
        // cleared → the wheel resolves again (center = MC cell4)
        assertEquals(4, w.resolvePadZone(w.player.ak - w.camX, w.player.al - w.camY))
    }

    @Test fun `mounted wheel uses 116x116 at cn-10 and radial offers`() {
        val w = world()
        w.cm = 1                                               // k() mounted
        // !bh3 radial offer cells (k.java:589-595): b(x,y,cx,cy,70) is
        // the r35 circle at the 70px box center (cx+35, cy+35)
        assertEquals(4, w.resolvePadZone(305, 200))
        assertEquals(1, w.resolvePadZone(355, 145))
        // outside the 116x116 wheel rect at (cn-10,124) → -1
        assertEquals(-1, w.resolvePadZone(300, 10))
        // inner band maps the split to 3/5 — cell4 is unreachable
        // (verbatim: the `==4 → -1` guard is dead in the original too)
        assertEquals(3, w.resolvePadZone(50, 182))
        assertEquals(5, w.resolvePadZone(100, 182))
    }

    @Test fun `bh3 remap rewrites wheel masks`() {
        val w = world()
        w.cm = 0                                             // remap gated by !k()
        w.kAj = 1                                              // bh[1]==3 autoscroll
        assertTrue(w.bh3)
        w.pad.e(2, w.jC == 8 && w.bh3 && !w.mounted)
        w.tick(emptyList())
        assertTrue(w.pad.u(20), "cell0 mask 2 → 20 under bh3")
        w.kAj = 0
    }

    @Test fun `E() last-wins replaces all six words`() {
        val w = world()
        w.pad.e(64); w.pad.e(16)
        w.tick(emptyList())
        assertTrue(w.pad.v(16)); assertFalse(w.pad.v(64))
        assertTrue(w.pad.u(16)); assertFalse(w.pad.u(64))
    }

    @Test fun `taps do nothing outside the play gate`() {
        val w = world()
        w.stateL(12)                                          // fail screen
        val (x, y) = w.cellPoint(5)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, x, y)))
        assertFalse(w.pad.u(64), "jC==12 is not the j() play gate")
    }

    @Test fun `jC 21 substate 8 keeps the wheel live`() {
        val w = world()
        w.stateL(21); w.dlgU = 8
        val (x, y) = w.cellPoint(5)
        assertEquals(5, w.resolvePadZone(x, y))
    }
}
