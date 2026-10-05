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

/** Slice 150 — player e() climb arms: case 27 (L27da link-lost fling),
 *  28/318 (L23fa freeze + v(33024) fling), 29/315 (L23bb go-climb /
 *  gk fling), 34 (L1a0a wall-kick + l() input + latch clear). */
class Slice150Test {

    class S150World(cell: Int = 0,
                    cellFn: ((Int, Int) -> Int)? = null) :
        Slice139Test.ClaimZoneWorld(cell, cellFn) {
        var vMask = 0
        var latchClears = 0
        override fun padHeld(mask: Int): Boolean = (vMask and mask) != 0
        override fun clearLatches() { latchClears++ }
    }

    @Test fun `S27 ac null flings to S43`() {
        // fallback g.java:5811 — ac==null → a(0)
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 27; p.ac = null
        fsm.tick(p, Pad())
        assertEquals(43, p.S)
        assertEquals(0, p.ah); assertEquals(1536, p.aj)
    }

    @Test fun `S27 ac bound keeps S27`() {
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 27; p.ac = Entity(10, null)
        fsm.tick(p, Pad())
        assertEquals(27, p.S)
    }

    @Test fun `S28 freezes then pad edge flings`() {
        // L23fa — zeroes velocity fields; v(33024) → a(ah)
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 28; p.ag = 999; p.ah = 500; p.ai = 7; p.aj = 9
        fsm.tick(p, Pad())
        assertEquals(28, p.S)                       // no key → stay
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.ai); assertEquals(0, p.aj)
        w.vMask = 33024
        fsm.tick(p, Pad())
        assertEquals(43, p.S)                       // flung
    }

    @Test fun `S29 and S315 past go upgrade anims`() {
        // L23bb — go!=0 && al>go → i(28); S315 → i(318)
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 29; p.go = 100; p.al = 200
        fsm.tick(p, Pad())
        assertEquals(28, p.S)
        val q = Entity(0, null)
        q.S = 315; q.go = 100; q.al = 200
        fsm.tick(q, Pad())
        assertEquals(318, q.S)
    }

    @Test fun `S29 no go and gk -1 flings`() {
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 29; p.go = 0; p.gk = -1; p.ah = 777
        fsm.tick(p, Pad())
        assertEquals(43, p.S)
        assertEquals(777, p.ah)
    }

    @Test fun `S34 wall cell runs l plus latch clear`() {
        // L1a0a — aT==20 keeps a(0) off; aR=5 → l() + k.v(). The head
        // rescan recomputes the flags: stage a 20 cell in the left strip
        // column and a 5 under the feet.
        val w = S150World(cellFn = { cx, cy ->
            if (cx == 9 && cy == 4) 20 else if (cx == 10 && cy == 5) 5 else 0 })
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.ak = 200; p.al = 100
        p.W[0] = 190; p.W[2] = 210; p.W[1] = 80; p.W[3] = 100
        p.S = 34; p.av = true
        fsm.tick(p, Pad())
        assertEquals(1, w.latchClears)
    }

    @Test fun `S34 non-20 side cell flings`() {
        // (av?aT:aU) != 20 → a(0)
        val w = S150World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 34; p.av = true; p.aT = 8; p.aR = 0
        fsm.tick(p, Pad())
        assertEquals(0, w.latchClears)
        assertEquals(43, p.S)
    }
}
