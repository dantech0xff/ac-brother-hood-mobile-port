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

/**
 * Slice 190 — `g.e()` head fidelity: `a(an())` per-tick wall rescan
 * (g.java:615, proven) now runs in `tick()` before dispatch, refreshing
 * `bb`/`bc`/`aT`/`aU`/`aO`/`aR`/`aZ` every tick for every state — the
 * `an()` whitelist (g.java:335, `rescanEligible`) only gates the wall-push
 * resolve inside `i.a(z2)`. Also the head terminal-velocity clamp
 * (g.java:602-607): `ah>5120 → ah=5120; cn++` else `cn=0`.
 */
class Slice190Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `rescanEligible follows the an whitelist`() {
        val p = Entity(0, null)
        // g.java:335 verbatim — S<=43, 150, 67-69, 199, 216, 217, 298,
        // plus the redundant 20/49/243 and 259-266 blocks.
        intArrayOf(0, 11, 43, 150, 67, 68, 69, 199, 216, 217, 298,
                   20, 49, 243, 259, 260, 266).forEach {
            p.S = it
            assertTrue(p.rescanEligible(), "S$it inside an()")
        }
        intArrayOf(44, 46, 66, 70, 102, 151, 218, 267, 297, 300,
                   317, 332).forEach {
            p.S = it
            assertFalse(p.rescanEligible(), "S$it outside an()")
        }
    }

    @Test fun `head rescan refreshes strip flags before the arm`() {
        // left strip column solid ≥18 → bb/aT refresh in the head;
        // a stale pin cannot hide it from the dispatch.
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 9) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0; p.aT = 9; p.bb = false
        fsm.tick(p, Pad())
        assertEquals(20, p.aT, "aT recomputed from the strip column")
        assertTrue(p.bb, "bb fresh from the same rescan")
    }

    @Test fun `an whitelist gates the push not the probes`() {
        // same left-column wall for both states: S34 (inside an()) gets
        // the z2 wall-push (+10), S102 (outside) keeps ak but still gets
        // fresh strip flags — i.java:870-925.
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 9) 20 else 0 })
        val fsm = PlayerFsm(w)
        val s34 = mk(200, 100); s34.S = 34; s34.av = true
        fsm.tick(s34, Pad())
        assertEquals(210, s34.ak, "S34 inside an() → wall push +10")
        val s102 = mk(200, 100); s102.S = 102; s102.aC = 18
        fsm.tick(s102, Pad())
        assertEquals(200, s102.ak, "S102 outside an() → no push")
        assertTrue(s102.bb, "…but probes still refreshed bb")
    }

    @Test fun `terminal clamp caps ah and counts capped ticks`() {
        Entity.gCn = 0
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.ah = 6000
        fsm.tick(p, Pad())
        assertEquals(5120, p.ah, "ah capped at the head (g.java:602)")
        assertEquals(1, Entity.gCn, "cn++ on the capped tick")
        p.ah = 7000
        fsm.tick(p, Pad())
        assertEquals(2, Entity.gCn, "cn counts consecutive capped ticks")
        p.ah = 100
        fsm.tick(p, Pad())
        assertEquals(0, Entity.gCn, "cn resets the tick ah drops below")
        Entity.gCn = 0
    }
}
