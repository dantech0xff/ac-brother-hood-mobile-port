package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Slice 406 — the ax51 crate `bs()` and the ax66 platform `bm()` re-read from the
 * raw bytes (i.javap `bs()` @0-1492, `bm()` @0-2067; helpers `bo/bp/bq/br`, `m(i)`,
 * `n(i)` verified equal).
 *
 * Fixed:
 * - **ax51 board nudge** @684-753: `getfield av; ifne 726` — a RIGHT-facing player
 *   (`av == false`) whose left edge sticks out past the crate's is shoved +20 toward
 *   it; a LEFT-facing one whose right edge sticks out, -20. The port had the two arms
 *   swapped;
 * - **ax66 S7/S25 ride** @315-337: S43 AND S34 both land (`aS.al = al; aS.i(0)`); the
 *   port released the link on S34 instead;
 * - **ax66 S20** @1725: its own arm — `if (r()) { i(19); aC = Z[1] }`. The port ran S20
 *   through the S19/21/22 grab arm and sent it to S18, so the timed return S19 (and
 *   its Z[1] countdown) was unreachable;
 * - **ax66 S11/12/13** @1011-1052: a linked S16 kill-crate makes the grab arm fling the
 *   player and `goto 2026` (end of the method); the port carried on and re-bound him.
 */
class Slice406Test {
    private fun crate(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = Entity(51, w.clips[7])
        e.setPositionPx(x, y); e.refreshBoxes(); e.S = s
        w.npcs.add(e)
        return e
    }

    private fun platform(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = Entity(66, w.clips[7])
        e.setPositionPx(x, y); e.refreshBoxes(); e.S = s
        w.npcs.add(e)
        return e
    }

    /** Boarding a 100-px-wide crate [200,300] from S236; the player's box is
     *  [x0, x0+40] — i.e. sticks out left when x0 < 200, right when x0 + 40 > 300. */
    private fun boardShift(av: Boolean, x0: Int): Int {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = crate(w, 250, p.al, 0)
        e.W[0] = 200; e.W[2] = 300
        p.setPositionPx(x0 + 20, e.al); p.S = 236; p.T = 0; p.refreshBoxes()
        p.W[0] = x0; p.W[2] = x0 + 40
        p.av = av; p.ac = e
        val before = p.ak
        w.npcFsm.tickPushable(e, w, p)
        return p.ak - before
    }

    @Test fun `ax51 board nudge - a right-facing player sticking out on the left is shoved right`() {
        assertEquals(20, boardShift(av = false, x0 = 190), "av false: W[0] < crate W[0] → +20")
        assertEquals(0, boardShift(av = false, x0 = 280), "av false never looks at the right edge")
    }

    @Test fun `ax51 board nudge - a left-facing player sticking out on the right is shoved left`() {
        assertEquals(-20, boardShift(av = true, x0 = 280), "av true: W[2] > crate W[2] → -20")
        assertEquals(0, boardShift(av = true, x0 = 190), "av true never looks at the left edge")
    }

    @Test fun `ax66 S34 lands on the riding platform like S43`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = platform(w, p.ak, p.al, 7)
        e.Z[1] = 5
        p.setPositionPx(e.ak, e.al - 30); p.S = 34; p.T = 0; p.refreshBoxes()
        p.ga = e
        w.npcFsm.tickPlatform(e, w, p)
        assertSame(e, p.ga, "g.a = this @266, no release")
        assertEquals(0, p.S, "aS.i(0)")
        assertEquals(e.al, p.al, "aS.al = al")
    }

    @Test fun `ax66 S20 anim-end is the countdown hand-off to S19 (aC = Z1)`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = platform(w, p.ak, p.al, 20)
        e.Z[1] = 9
        e.T = e.clip!!.frameCount(20) - 1
        w.npcFsm.tickPlatform(e, w, p)
        assertEquals(19, e.S)
        assertEquals(9, e.aC)
    }

    @Test fun `ax66 S20 mid-anim does nothing, and never grabs the player`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = platform(w, p.ak, p.al, 20)
        p.setPositionPx(e.ak, e.al); p.S = 67; p.refreshBoxes()      // an attacking player on top
        e.T = 0; e.U = 0
        w.npcFsm.tickPlatform(e, w, p)
        assertEquals(20, e.S)
        assertNull(p.ga, "S20 has no attack-grab arm (that is S19/21/22)")
    }

    @Test fun `ax66 S12 with a linked S16 kill-crate flings the player and leaves him unbound`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val linked = platform(w, p.ak + 200, p.al, 16); linked.aw = 88
        val e = platform(w, p.ak, p.al, 12)
        e.Z[4] = 88
        p.setPositionPx(e.ak, e.al - 5); p.S = 236; p.T = 0; p.refreshBoxes(); p.ac = null
        w.npcFsm.tickPlatform(e, w, p)
        assertNull(p.ga, "@1011: aS.a(ah); g.a = null; goto 2026 — the board arm never runs")
        assertEquals(43, p.S, "flung")
    }
}
