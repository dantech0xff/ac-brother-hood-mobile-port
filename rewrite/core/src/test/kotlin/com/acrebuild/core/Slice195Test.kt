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

class Slice195Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S19 arms cv and runs the shared air tail`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 19; p.gI = 4
        fsm.tick(p, Pad())
        assertTrue(p.cv, "L1ce4 arms cv")
        assertTrue(p.cp && p.ct && p.cw, "L1ce8 air flags")
        assertTrue(p.z, "L1ce8 arms z when g.I==4")
        assertEquals(1536, p.aj, "aj=1536")
    }

    @Test fun `S157 runs the air tail without cv`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 157; p.cv = false
        fsm.tick(p, Pad())
        assertFalse(p.cv, "L1ce8 never arms cv")
        assertEquals(1536, p.aj)
    }

    @Test fun `S49 slide stops after frame nine and settles`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 49; p.T = 10
        p.ag = 2048; p.ah = -512
        fsm.tick(p, Pad())
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.S, "r() → i(0) — L3078")
    }

    @Test fun `S74 leap-dash flings forty px facing`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 74; p.av = true
        fsm.tick(p, Pad())
        assertEquals(160, p.ak, "r() → ak-40 — L169c")
        assertEquals(43, p.S, "a(0) masked fling → S43")
    }

    @Test fun `S82 bare goto leaves the rope-hang state alone`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 82
        fsm.tick(p, Pad())
        // g.java:1430 → L2989 → L353d — bare shared tail, no arm
        // (`r() → P|=64` belongs to case 110's L2de0, not this family)
        assertEquals(0, p.P and 64, "no P|=64 — L2989 is bare")
        assertEquals(82, p.S)
    }

    @Test fun `S326 shares the bare-goto arm`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 326
        fsm.tick(p, Pad())
        assertEquals(0, p.P and 64, "no P|=64 — L2989 is bare")
        assertEquals(326, p.S)
    }

    @Test fun `S91 zeroes velocity and settles to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 91
        p.ag = 100; p.ah = 100; p.ai = 5; p.aj = 5
        fsm.tick(p, Pad())
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.ai); assertEquals(0, p.aj)
        assertEquals(0, p.S, "r() → i(0) — L30a9")
    }

    @Test fun `S92 wall bounce flips facing and relaunches`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 92; p.av = false; p.aO = 7
        fsm.tick(p, Pad())
        assertTrue(p.av, "av flips — L1a46")
        assertEquals(-2048, p.ag, "ag = -2048 in new facing")
        assertEquals(-5120, p.ah, "aO!=20 → ah=-5120")
        assertEquals(36, p.S, "a(36,36) re-enters the wall state")
    }

    @Test fun `S92 with aO 20 drops flat`() {
        val w = Slice128Test.MarkerWorld(cell = 20)   // head probe fills aO=20
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 92
        fsm.tick(p, Pad())
        assertEquals(0, p.ah, "aO==20 → ah=0")
        // raw bytes @6809-6832 (slice 416): a solid head cell (`ah == 0`) FALLS — `g.a(0)` (S43),
        // only an open one re-enters the wall state with `a(36,36)`.
        assertEquals(43, p.S, "ah == 0 → g.a(0) fall")
    }

    @Test fun `S122 bind-prep enters masked state 53`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 122
        fsm.tick(p, Pad())
        assertEquals(53, p.S, "r() → a(53,1032) — L11c2")
    }

    @Test fun `S148 launches up then hands to S149`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 148
        fsm.tick(p, Pad())
        assertEquals(149, p.S, "r() → i(149) — L2eb9")
        assertEquals(-5120, p.ah)
    }

    @Test fun `S149 carries gl velocity then falls`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 149; p.gL = 3000
        fsm.tick(p, Pad())
        assertEquals(150, p.S, "r() → i(150) — L2ed1")
        assertEquals(0, p.gL, "g.l cleared on exit")
    }

    @Test fun `S152 slide settles to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 152; p.av = false
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "r() → i(0) — L2f42")
        assertEquals(1024, p.ag)
    }

    @Test fun `S156 picks up the gl velocity into S157`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 156; p.gL = 700
        fsm.tick(p, Pad())
        assertEquals(700, p.ag, "ag = g.l — L2f13")
        assertEquals(157, p.S, "r() → i(157)")
        assertEquals(0, p.ah)
    }

    @Test fun `S204 victim-dump loops back to S203`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 204
        fsm.tick(p, Pad())
        assertEquals(203, p.S, "r() → i(203) — L2596")
    }

    @Test fun `S214 knockback rise hands to the air family`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 214
        fsm.tick(p, Pad())
        assertEquals(215, p.S, "r() → i(215) — L305d")
        assertEquals(0, p.ah)
    }

    @Test fun `S225 inert state does nothing`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 225
        p.ag = 333
        fsm.tick(p, Pad())
        assertEquals(225, p.S)
        assertEquals(333, p.ag, "bare return — L2f66")
    }

    @Test fun `S282 pinned settles into S38`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 282
        fsm.tick(p, Pad())
        assertEquals(38, p.S, "r() → i(38) — L2b6c")
    }

    @Test fun `S283 settles to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 283
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "r() → i(0) — L309c")
    }
}
