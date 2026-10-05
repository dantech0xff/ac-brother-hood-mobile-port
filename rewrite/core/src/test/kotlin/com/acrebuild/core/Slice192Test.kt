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
 * Slice 192 — shared fall case completed: S16/35/150 routed to
 * `fallArm` (g.java:1400 `case 16/35/43/150/252`, proven), plus the
 * remaining arm body — `I==4 → z=true` (:1414), the L1425 bound catch
 * (`gk!=-1 && al<go`: gk==0 → a(29,32)+snap gn; gk==1 → snap gd + i(315)),
 * the L1430 `y()&&dir` wall-grip tap block (bound gate → exit; else
 * `ag=±512`), and the `:1465` tail `S∉{43,150,35,29,252} && r() → i(35)`
 * (S16 door-glide → loop-fall).
 */
class Slice192Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S16 routes to the shared fall arm`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 16
        fsm.tick(p, Pad())
        assertTrue(p in w.clampCalls, "i.f(this) head — S16 shares the case")
        assertTrue(p.cv && p.cp && p.ct && p.cw, "flag latch set")
        assertEquals(1536, p.aj, "else-branch gravity arm")
    }

    @Test fun `S16 anim end resolves to i35`() {
        // clipless → r()=true → S16 ∉ {43,150,35,29,252} → i(35)
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 16
        fsm.tick(p, Pad())
        assertEquals(35, p.S, "S16 → i(35) on anim end — g.java:1465")
    }

    @Test fun `S35 loop-fall keeps its state`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 35
        fsm.tick(p, Pad())
        assertEquals(35, p.S, "S35 is in the exclude set — no self-transition")
    }

    @Test fun `bound catch gk0 enters a29 masked state`() {
        // gk==0, go==0 (no gate) → a(29,32); gn snaps ak; velocities parked
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43
        p.gk = 0; p.go = 0; p.gn = 260
        fsm.tick(p, Pad())
        assertEquals(29, p.S, "gk==0 → a(29,32) ride state")
        assertEquals(260, p.ak, "ak snapped to zone center gn")
        assertEquals(0, p.ag); assertEquals(1536, p.ah); assertEquals(0, p.aj)
    }

    @Test fun `bound catch gk1 snaps onto the owner entity`() {
        // real clips needed — the :1465 tail re-checks r() on the NEW
        // S315, and clipless entities always report anim-ended. Lift the
        // player off the floor so the bound-catch else-branch runs.
        val w = world()
        val p = w.player
        p.S = 43; p.al = 60
        p.W[1] = 40; p.W[3] = 60
        val d = Entity(43, null)
        d.av = true; d.W[0] = 300; d.W[2] = 340; d.W[1] = 90; d.W[3] = 110
        p.gk = 1; p.go = 0; p.gd = d
        w.playerFsm.tick(p, Pad())
        assertEquals(315, p.S, "gk==1 → i(315) carrier pose")
        assertTrue(p.av, "av copied from gd")
        assertEquals(340, p.ak, "ak = gd.W[2] (av-facing)")
    }

    @Test fun `go gate skips the bound catch`() {
        // gk==0 but al >= go → condition fails → plain fall continues
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43
        p.gk = 0; p.go = 50; p.gn = 260   // al=100 ≥ go=50
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "no catch — fall continues")
        assertEquals(1536, p.aj); assertEquals(200, p.ak)
    }

    @Test fun `wall grip tap drags fall drift to 512`() {
        // y()=hitWall via left-strip 20 + UP held → ag pulled to +512
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 9) 20 else 0 })
        val fsm = PlayerFsm(w)
        // S35 — for S43 the head's `y()` arm zeroes ag first, so the
        // drag's `ag != 0` test can never fire there (proven ordering).
        // ag<0 → hitWall reads bb (the left-strip 20 contact).
        val p = mk(200, 100); p.S = 35; p.ag = -4096; p.av = true
        val pad = Pad(); pad.held = Pad.M_UP
        fsm.tick(p, pad)
        assertEquals(-512, p.ag, "y()&&dir → ag = ∓512 grip drag — g.java:1454")
    }

    @Test fun `I4 arms the grounded flag mid-fall`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.gI = 4
        fsm.tick(p, Pad())
        assertTrue(p.z, "I==4 → z = true — g.java:1414")
    }
}
