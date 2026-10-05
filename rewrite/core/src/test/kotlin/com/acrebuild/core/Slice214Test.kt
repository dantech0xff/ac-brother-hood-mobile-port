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

class Slice214Test {

    /**
     * `I()` preamble (i.java:15165-15260, proven): before the dispatch
     * every entity runs the y-freeze countdown / `s()` advance (gated
     * `!k.al || aa==z[12]`), then `m()` (`y=0`) unless claim-suspended,
     * then `b=1`, then the L108 gate (P|512 / claimer / ax8/ax24 exempt
     * from claim+u9 suspension). Before this port, claimed procs never
     * advanced their anim clocks — `r()`-gated arms deadlocked (ax7's
     * mouth-plant froze at the vault apex, ax46 traps stuck mid-cycle).
     */

    private fun ax22s(w: Level0World) = w.npcs.filter { it.ax == 22 }

    private fun settle(w: Level0World) {
        var t = 0
        while (w.player.ah != 0 && t++ < 600) w.tick(emptyList())
    }

    @Test fun `ax7 mouthplant swallows and releases through the real tick`() {
        val w = world()
        settle(w)
        val e = w.npcs.firstOrNull { it.ax == 7 }
            ?: error("level-0 carries no ax7 mouth-plant")
        keepLive(e)
        val p = w.player
        // the S0 arm needs player-W ∩ plant-W + !holding — the plant's
        // trigger rect sits off-anchor, so re-pin the player inside it
        // each tick while the world settles.
        var swallowed = false
        for (i in 0 until 20) {
            p.setPositionPx((e.W[0] + e.W[2]) / 2, (e.W[1] + e.W[3]) / 2)
            p.S = 0; p.ah = 0; p.ag = 0; p.refreshBoxes()   // the plant ticks first (G12)
            w.tick(emptyList())
            // stop re-pinning once swallowed (`return@repeat` only skipped
            // to the next pass, which re-forced S0 under the capture)
            if (e.S == 1) { swallowed = true; break }
        }
        assertTrue(swallowed, "overlap -> i(1) swallow; e.S=${e.S}")
        assertTrue(p.P and 64 != 0, "player slot-held P|64 while swallowed")
        // The S1 arm releases on `r()` — only reachable because the I()
        // preamble's s() now advances e.T for claimed procs.
        var released = false
        var thrownAg = 0
        for (i in 0 until 200) {
            w.tick(emptyList())
            if (e.S == 0) { released = true; thrownAg = p.ag; break }
        }
        assertTrue(released, "animFinished -> i(0) release + throw; e.S=${e.S} T=${e.T}")
        assertTrue(released, "S1 arm completed the swallow cycle")
        assertTrue(thrownAg == 2048 || thrownAg == -2048 || p.S != 313,
            "release ejects the player (ag=${thrownAg} S=${p.S})")
    }

    @Test fun `y latch freezes s once then m resets it next tick`() {
        val w = world()
        settle(w)
        val p = w.player
        val e = ax22s(w).firstOrNull { it.ak == 1214 && it.al == 636 }
            ?: error("no ax22 record at (1214,636)")
        keepLive(e)
        p.setPositionPx(e.ak, e.al); p.S = 0; p.ah = 0; p.ag = 0
        p.refreshBoxes()           // no stale spawn boxes for the intro claim (G12)
        w.tick(emptyList())
        // aOp's >=100 sentinel: skip s() this tick, then m() (not claim-
        // suspended) resets y to 0 so the next tick advances again.
        e.y = 101
        val t0 = e.T
        w.tick(emptyList())
        assertEquals(t0, e.T, "y>=100 holds s() for this tick")
        assertEquals(0, e.y, "m() clears y when not claim-suspended")
        w.tick(emptyList())
        assertTrue(e.T != t0 || e.S != 0,
            "s() resumes the tick after m() clears y (T=${e.T} S=${e.S})")
    }

    /** A fabricated claim suspension: `k.C` whose `ab()` is true
     *  (`ca>=0 && !cd[0] && scriptStep>=0`, i.java claim model). */
    private fun suspendWorld(w: Level0World): Entity =
        Entity(5, null).also { it.ca = 0; it.scriptStep = 0; w.kC = it }

    @Test fun `claim suspension skips dispatch and the shared tail`() {
        val w = world()
        settle(w)
        // a non-exempt npc (ax4 destructible — no P|512): corrupt W must
        // stay corrupt while k.C.ab() suspends it.
        val e = w.npcs.firstOrNull { it.ax == 4 && (it.P and 512) == 0 }
            ?: error("no non-exempt ax4 on level-0")
        e.W[0] = -9999; e.W[2] = -9998; e.W[1] = -9997; e.W[3] = -9996
        suspendWorld(w)
        w.tick(emptyList())
        assertEquals(-9999, e.W[0], "suspended: no dispatch, no t() tail")
        w.kC = null
        w.tick(emptyList())
        assertTrue(e.W[0] != -9999 || e.S != 0,
            "resume -> dispatch + L1f35 rebuild (W=${e.W.toList()} S=${e.S})")
    }

    @Test fun `P512 zones still dispatch under claim suspension`() {
        val w = world()
        settle(w)
        val e = ax22s(w).firstOrNull { it.ak == 1214 && it.al == 636 }
            ?: error("no ax22 record at (1214,636)")
        keepLive(e)
        assertTrue(e.P and 512 != 0, "ax22 is P|512 exempt")
        val p = w.player
        // S=43 (airborne) — the capture arm's `g.b(S)` gate (i.java:10185)
        // rejects grounded S=0; suspension freezes S, so pin a capturable
        // state the way a falling player would arrive.
        p.setPositionPx(e.ak, e.al); p.S = 43; p.ah = 0; p.ag = 0
        p.refreshBoxes()   // suspended player skips t() — stage W here
        suspendWorld(w)
        var captured = false
        repeat(40) {
            w.tick(emptyList())
            if (p.S == 65) { captured = true; return@repeat }
        }
        assertTrue(captured, "P|512 zone still captures under suspension")
    }
}
