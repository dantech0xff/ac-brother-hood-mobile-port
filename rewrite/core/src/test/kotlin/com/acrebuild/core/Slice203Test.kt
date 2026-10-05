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

class Slice203Test {
    /** MarkerWorld + queueInsert tracking for the grab aK marker. */
    class GrabWorld(cell: Int = 0, cellFn: ((Int, Int) -> Int)? = null) :
        Slice128Test.MarkerWorld(cell, cellFn) {
        val inserts = mutableListOf<Entity>()
        override fun queueInsert(e: Entity) { inserts += e }
    }

    private fun mk(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    // -- L3a9d cv → aF producer (g.java:8253-8279, proven) --------------

    @Test fun `cv armed fall with up held arms the grab intent`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43
        val pad = Pad(); pad.held = Pad.M_UP
        fsm.tick(p, pad)
        assertEquals(1, p.aF, "cv && u|v(16388|8|2) → aF=1")
    }

    @Test fun `aF survives the per tick head clear`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.aF = 1
        fsm.tick(p, Pad())
        assertEquals(1, p.aF, "aF is not in the e() head clear set")
    }

    // -- L2231/L2298 fall-arm wall grab (g.java:5049-5210, proven) ------

    @Test fun `fall arm grabs a cell twenty side wall into cling`() {
        // right strip (10,4)=20 → aU=20/bc via the head collideSides
        // tall wall (10,3)+(10,4)=20 — the lip probes' `e(i2,i4-1)>0`
        // pocket check fails, so the i(101) grab isn't remounted by the
        // ct consumer's ledge mount in the same tail.
        val w = GrabWorld(cellFn = { cx, cy ->
            if (cx == 10 && (cy == 3 || cy == 4)) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.av = false; p.aF = 1
        val pad = Pad(); pad.held = Pad.M_TAP_R
        fsm.tick(p, pad)
        assertEquals(101, p.S, "L2298 grab → i(101)")
        // aF is consumed by the grab then immediately re-armed by the
        // postTail cv producer — the tap is still held toward the wall.
        assertEquals(1, p.aF, "cv && held TAP_R re-arms aF for next tick")
        assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
        assertEquals(1, w.inserts.size, "a(8,5,17,201) marker spawned")
        assertEquals(8, w.inserts[0].ax)
        assertEquals(17, w.inserts[0].S)
        assertEquals(512, w.inserts[0].P)
    }

    @Test fun `fall arm without intent drifts at 512`() {
        // same wall + tap input, aF==0 → grab gate fails → L2361 drift.
        // S35 keeps ag (S43's arm zeroes it at the head — use S35).
        // tall wall (10,3)+(10,4)=20 — the lip probes' `e(i2,i4-1)>0`
        // pocket check fails, so the i(101) grab isn't remounted by the
        // ct consumer's ledge mount in the same tail.
        val w = GrabWorld(cellFn = { cx, cy ->
            if (cx == 10 && (cy == 3 || cy == 4)) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 35; p.av = false; p.ag = 900
        val pad = Pad(); pad.held = Pad.M_TAP_R
        fsm.tick(p, pad)
        assertNotEquals(101, p.S, "aF==0 → no grab")
        assertEquals(512, p.ag, "L2361: ag>0 → +512")
    }

    // -- L1e3e/L1e94 air-arm wall grab (g.java:4578-4730, proven) -------

    @Test fun `air arm grabs the side wall with armed intent`() {
        // tall wall (10,3)+(10,4)=20 — the lip probes' `e(i2,i4-1)>0`
        // pocket check fails, so the i(101) grab isn't remounted by the
        // ct consumer's ledge mount in the same tail.
        val w = GrabWorld(cellFn = { cx, cy ->
            if (cx == 10 && (cy == 3 || cy == 4)) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 22; p.av = false; p.aF = 1
        fsm.tick(p, Pad())
        assertEquals(101, p.S, "L1e94 grab → i(101)")
        assertEquals(0, p.aF)
        assertFalse(p.av, "S22 keeps facing — the flip is S215-only")
        assertEquals(1, w.inserts.size)
    }

    @Test fun `S215 grabs without intent and flips facing`() {
        // fallback g.java:4596-4609 (proven): the S215 corner check is
        // INVERTED vs the cv&&aF path — the mount throws the player
        // backward so the grab face is the one they fly INTO: `av →
        // aU==20`, `!av → aT==20`. West wall (9,3)+(9,4)=20 with av=false
        // (facing east, flying west) → aT==20 → grab. The lip probes'
        // `e(i2,i4-1)>0` pocket check fails on this tall wall, so the
        // i(101) grab isn't remounted by the ct consumer's ledge mount
        // in the same tail.
        val w = GrabWorld(cellFn = { cx, cy ->
            if (cx == 9 && (cy == 3 || cy == 4)) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 215; p.av = false; p.aF = 0
        fsm.tick(p, Pad())
        assertEquals(101, p.S)
        assertTrue(p.av, "S215 → av = !av inside the grab")
    }

    // -- L3a2d ct → ledge mount (g.java:8199-8234, proven) --------------

    @Test fun `ct armed falling player mounts a ledge lip`() {
        // left strip (9,4)=20 lip cell; pocket cells all open → i(60)
        val w = GrabWorld(cellFn = { cx, cy ->
            if (cx == 9 && cy == 4) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.av = true
        fsm.tick(p, Pad())
        assertEquals(60, p.S, "ledgeLipGrab → i(60)")
        assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
    }

    // -- L3a71 cu → ledge drop (g.java:8235-8252, proven) ---------------

    @Test fun `S60 down edge drops via the fast fling`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 60; p.Q = 60
        val pad = Pad(); pad.queuePress(Pad.M_DOWN); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(43, p.S, "cu && v(33024) → a(2560)")
        assertEquals(2560, p.ah)
        assertEquals(110, p.al, "a(2560) drops +10")
    }

    @Test fun `S60 down edge releases an ax14 held link`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 60; p.Q = 60
        val held = Entity(14, null)
        p.ab = held
        val pad = Pad(); pad.queuePress(Pad.M_DOWN); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(43, p.S)
        assertNull(p.ab, "ab.ax==14 → i.H() drops the link")
    }

    // -- L3ad8 cw → ceiling grab (g.java:8279-8303, proven) -------------

    @Test fun `cw armed head cell 5 grabs the ceiling`() {
        // head cell (10,4)=5 → aO=5 via the head probeCells
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 5 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43; p.ag = 900; p.ah = 700
        fsm.tick(p, Pad())
        assertEquals(280, p.S, "cw && aO==5 → i(280)")
        assertEquals(90, p.al, "al = (W[1]/20)*20+10")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.aj); assertEquals(0, p.ai)
    }

    // -- L3852 aO 7/9 → cq=0 (g.java:7968-7974, proven) -----------------

    @Test fun `head cell seven clears the jump latch before the gate`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else if (cx == 10 && cy == 4) 7 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertFalse(p.cq, "aO==7 → cq=0 ahead of the L38b8 jump gate")
        assertTrue(p.S != 21 && p.S != 233 && p.S != 22,
            "the cleared latch suppresses the jump")
    }

    @Test fun `open head cell lets the jump gate fire`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertTrue(p.S == 21 || p.S == 233 || p.S == 22,
            "no head cell → cq survives → jump fires")
    }
}
