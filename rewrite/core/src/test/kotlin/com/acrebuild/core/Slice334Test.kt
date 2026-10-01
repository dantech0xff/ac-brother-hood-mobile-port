package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 334 — Devin Review fixes on the bv4 scroll affordance.
 *
 * The port-added `menuScrollDy` drag had two real defects (both
 * port-side, the J2ME original had no touch scroll):
 *   - a drag CLAMPED at the scroll limit produced no `menuScrollDy`
 *     delta, so the release still fired the row tap under the finger —
 *     suppression now tracks finger travel, not the (clamped) delta;
 *   - `menuRowAt` hit-tested rects clipped away by the scroll viewport,
 *     so taps on the panel chrome selected invisible rows — the hit
 *     region is now the drawn row ∩ [viewTop, 235], the same window
 *     the renderer scissors to (Level0Renderer clipViewport).
 */
class Slice334Test {

    private fun scrollableMenu(): Level0World {
        val w = world(aj = 0)
        w.stateL(3); w.kBv = 4; w.kEy = 8
        assertTrue(w.menuScrollMax() > 0, "fixture must overflow the canvas")
        return w
    }

    @Test
    fun `clamped drag release does not fire the row tap`() {
        val w = scrollableMenu()
        val max = w.menuScrollMax()
        w.menuScrollDy = max                      // already at the limit
        // a flick batched in one tick: drag 60px up (clamped at max —
        // scrollDy never moves), released ONTO a visible row
        val rects = w.menuRowRects()
        val viewTop = w.menuPanelRect()[1] + 10 + (if (w.menuPanelZ3()) 40 else 0)
        val row = rects.first { it[1] > viewTop + 40 && it[1] + it[3] < 230 }
        val ry = row[1] + 2
        w.kCb = false
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 100, ry + 60),
                      InputQueue.Event(1, InputQueue.Type.MOVE, 100, ry),
                      InputQueue.Event(2, InputQueue.Type.UP, 100, ry)))
        assertEquals(max, w.menuScrollDy, "drag clamped — no scroll delta")
        assertTrue(!w.kCb,
                   "travel > 6px suppresses the release tap even when clamped")
        // counterfactual: without suppression the release point maps to
        // a real row — the tap would have fired
        w.lastTouchX = 100; w.lastTouchY = ry
        assertEquals(rects.indexOf(row), w.menuRowAt(ry),
                     "release point sits on a visible row")
    }

    @Test
    fun `plain tap on a visible row still resolves`() {
        val w = scrollableMenu()
        val r1 = w.menuRowRects()[1]
        val tx = r1[0] + 5; val ty = r1[1] + 2
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, tx, ty),
                      InputQueue.Event(1, InputQueue.Type.UP, tx, ty)))
        assertEquals(1, w.kBw, "stationary tap selects row 1")
    }

    @Test
    fun `menuRowAt skips rows clipped above the scroll viewport`() {
        val w = scrollableMenu()
        w.menuScrollDy = w.menuScrollMax()
        val viewTop = w.menuPanelRect()[1] + 10 + (if (w.menuPanelZ3()) 40 else 0)
        val r0 = w.menuRowRects()[0]
        assertTrue(r0[1] + r0[3] <= viewTop,
                   "row 0 scrolled fully above the viewport")
        // tap inside row 0's clipped rect — lands on the panel chrome
        w.lastTouchX = r0[0] + 5; w.lastTouchY = r0[1] + r0[3] - 1
        assertEquals(-1, w.menuRowAt(w.lastTouchY),
                     "clipped row must not hit")
    }

    @Test
    fun `menuRowAt still hits a visible row`() {
        val w = scrollableMenu()
        w.menuScrollDy = w.menuScrollMax()
        val rects = w.menuRowRects()
        val last = rects.last()
        assertTrue(last[1] in 0..235, "bottom row enters the canvas")
        w.lastTouchX = last[0] + 5; w.lastTouchY = last[1] + 2
        assertEquals(rects.size - 1, w.menuRowAt(w.lastTouchY),
                     "visible row still resolves")
    }
}
