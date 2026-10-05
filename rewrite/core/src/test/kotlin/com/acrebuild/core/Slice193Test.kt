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
 * Slice 193 — ledge-hang family verbatim (fallback g.java):
 * S56 grab-settle (L2b3b: `ah=ag=0`; `r()` → keys-held i(65) else i(59)),
 * S60 hang (L2421: `Q!=63||u(16388)||toward → i(62)` + `cu` latch),
 * S61 grip-hang (L2460: `aC` grace → drop `H+G+al+=W3-W1+a(0)`,
 * ax43 link blocks; front-cell loss / `v(33024)` → same drop),
 * S203 victim-carry variant (`g.h` arm → `k.c` marker + `v(65568)` dump),
 * S62 climb-up finish (L25b5: `r()` → `ak±10` → `a(aO>12?79:0,9)`),
 * S63 anim end → i(60) (L25a5 — fixes the inferred `i(0)`).
 */
class Slice193Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S56 settle drops to hang-idle on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 56
        fsm.tick(p, Pad())
        assertEquals(59, p.S, "no keys → i(59) hang idle — L2b3b")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
    }

    @Test fun `S56 settle with keys held enters shimmy`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 56
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertEquals(65, p.S, "u(127999) keys → i(65) — L2b3b")
    }

    @Test fun `S60 hang climbs up on UP`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 60; p.Q = 63   // arrived via S63
        val pad = Pad(); pad.held = Pad.M_UP
        fsm.tick(p, pad)
        assertEquals(62, p.S, "u(16388) → i(62) — L2421")
        assertTrue(p.cu, "cu latch set")
    }

    @Test fun `S60 hang waits when arrived via S63 with no input`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 60; p.Q = 63
        fsm.tick(p, Pad())
        assertEquals(60, p.S, "Q==63 and no press → still hanging")
        assertTrue(p.cu)
    }

    @Test fun `S61 grip expiry drops the player`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 61; p.aC = 1   // last grace tick
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "aC→0 → H+G+a(0) drop → freefall — L2460")
        // the arm's own `al += W3-W1` (+20), then a(0) = a(43,32) → i(43)
        // while still S61 adds W3-W1 again (i.java:264-266, slice 361),
        // then a(0)'s al += 10
        assertEquals(150, p.al, "al += W3-W1 twice, then a(0) al+=10")
    }

    @Test fun `S61 edge loss with no link drops immediately`() {
        val w = Slice128Test.MarkerWorld(cell = 0)   // front cell = 0
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 61; p.aC = 40
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "edge lost + no ga → drop — L2460")
    }

    @Test fun `S61 hangs while the wall holds`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy -> if (cy == 5) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 61; p.aC = 40
        p.av = false                                 // front cell x+10 → col 10, row 5
        fsm.tick(p, Pad())
        assertEquals(61, p.S, "front cell ≥12 → keeps hanging")
        assertEquals(39, p.aC, "grace counts down")
    }

    @Test fun `S61 carrier link blocks the drop`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 61; p.aC = 1
        p.ga = Entity(43, null)                      // ax43 carrier
        fsm.tick(p, Pad())
        assertEquals(61, p.S, "ga.ax==43 → no drop — L24b9")
    }

    @Test fun `S61 climb-up on UP edge`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy -> if (cy == 5) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 61; p.aC = 40; p.av = false
        val pad = Pad(); pad.bB = Pad.M_UP           // v() edge
        fsm.tick(p, pad)
        assertEquals(62, p.S, "v(16388) → H+G+i(62) — L255c")
    }

    @Test fun `S62 climb-up steps forward and settles`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 62; p.av = false; p.aO = 0
        fsm.tick(p, Pad())
        assertEquals(210, p.ak, "r() → ak += 10 — L25b5")
        assertEquals(0, p.S, "aO<=12 → a(0,9)")
    }

    @Test fun `S63 climb anim hands off to the hang`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 63
        fsm.tick(p, Pad())
        assertEquals(60, p.S, "r() → i(60) — L25a5")
    }

    @Test fun `S203 victim carry marks and dumps`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 203
        val h = Entity(11, null); h.aw = 77; h.aB = 5
        p.gh = h
        val pad = Pad(); pad.bB = 65568              // v() edge
        fsm.tick(p, pad)
        assertEquals(204, p.S, "v(65568) → G+i(204) — L24f3")
        assertNull(p.gh, "victim link cleared")
        assertEquals(190, h.ak, "victim ak = ak-10 (av=false)")
    }
}
