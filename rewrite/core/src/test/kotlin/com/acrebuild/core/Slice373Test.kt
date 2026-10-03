package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 373 — per-frame draw-pass state belongs to the world tick.
 *
 * - `i.ad()` (speech bubble) has a single call site: `b(false)`'s entity
 *   loop, `(ax!=11 && ax!=17) || aB>0 → ad()` (bytecode k.javap.txt:14699,
 *   `b(Z)` offset 2118; structured k.java:2927-2929 = simple :3740-3749).
 *   The port ticked it per sim tick in `tickNpc` (ax!=11/17, visible or
 *   not) AND per rendered frame from the renderer, so bubble timers
 *   depended on the display rate, live soldiers' bubbles never stepped
 *   headless, and the renderer consumed the descriptor after one frame.
 * - The selected-row band (`fI += fH; fH += 8`, k.java:6005-6013) and the
 *   jc20 tall-text `eZ` slide (k.java:1289-1296) were stepped/written by
 *   the renderer at the render rate.
 */
class Slice373Test {
    private fun ev(type: InputQueue.Type, x: Int, y: Int, seq: Long = 0) =
        InputQueue.Event(seq, type, x, y)

    private fun arm(e: Entity, timer: Int) {
        e.cQ = IntArray(10) { -1 }; e.cT = e
        val q = e.cQ!!
        q[5] = 7; q[6] = 7; q[2] = -1; q[3] = timer   // one page, refill on first ad()
    }

    /** A real soldier moved next to the player, inside the view. */
    private fun soldierInView(w: Level0World): Entity {
        val p = w.player
        val s = w.npcs.first { it.ax == 11 && it.aB > 0 }
        s.ag = 0; s.ah = 0
        s.setPositionPx(p.ak + 60, p.al)
        s.refreshBoxes()
        assertTrue(s.inPlayV(w), "precondition: the soldier is in the view")
        return s
    }

    @Test fun `a live soldier's bubble steps once per tick in the draw pass`() {
        val w = world()
        w.tick(emptyList())
        val s = soldierInView(w)
        arm(s, 30)
        w.tick(emptyList())
        assertEquals(29, s.cQ!![2], "refill (q[2]=q[3]) then one L28 step")
        assertSame(s, w.bubbleOwner, "the renderer draws it after this entity")
        assertTrue(w.bubbleDraw != null)
        repeat(4) { w.tick(emptyList()) }
        assertEquals(25, s.cQ!![2], "exactly one ad() per frame")
    }

    @Test fun `a dead soldier's bubble does not step`() {
        val w = world()
        w.tick(emptyList())
        val s = soldierInView(w)
        arm(s, 30)
        s.aB = 0
        s.cQ!![2] = 10
        w.tick(emptyList())
        assertEquals(10, s.cQ!![2], "ax11 with aB<=0 is outside the ad() gate")
    }

    @Test fun `the descriptor lasts the whole tick and clears with the bubble`() {
        val w = world()
        w.tick(emptyList())
        val s = soldierInView(w)
        arm(s, 30)
        w.tick(emptyList())
        val d = w.bubbleDraw
        assertTrue(d != null)
        assertSame(d, w.bubbleDraw, "nothing consumes it between ticks")
        s.cQ = null                                       // bubble gone
        w.tick(emptyList())
        assertNull(w.bubbleDraw, "the next draw pass re-derives it")
        assertNull(w.bubbleOwner)
    }

    @Test fun `the row band steps once per frame while its row is hovered`() {
        val w = world()
        w.stateL(14)
        w.tick(emptyList())
        val r = w.menuRowRects().first()
        val cx = r[0] + r[2] / 2; val cy = r[1] + r[3] / 2
        w.kFI = 1; w.kFH = 0
        w.tick(listOf(ev(InputQueue.Type.DOWN, cx, cy)))   // finger rests on row 0
        // the DOWN sample lands this tick; the band reads J/K from it
        val seen = ArrayList<Int>()
        seen += w.menuBandDraw
        repeat(4) { w.tick(emptyList()); seen += w.menuBandDraw }
        // fI: 1 → 1 (+fH 0) → 9 (+8) → 25 (+16) → 49 ≥ i4 → 0
        assertEquals(listOf(1, 1, 9, 25, 0), seen.take(5),
            "drawn heights, one step per frame (k.java:6005-6013)")
        assertEquals(0, w.kFI)
    }

    @Test fun `the jc20 tall-text slide is world state`() {
        val cm = java.io.File("../generated/fonts/charmap.bin").readBytes()
        val w = world(charmap = cm)
        w.stateL(20)
        w.kFb = (1..30).joinToString("\n") { "LINE $it" }
        w.kCu = 3; w.kEY = 200
        w.tick(emptyList())
        assertTrue(w.kEz < 85, "a block taller than 120px slides eZ up (k.java:1293-1294)")
    }
}
