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

class Slice204Test {
    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    /** Flat floor under the feet row (cy==5 ↔ r02=101) so aZ resolves
     *  grounded on tick 1 — these arms are grounded-input semantics. */
    private fun floorWorld(): Slice128Test.MarkerWorld =
        Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy == 5) 12 else 0 })

    // -- L3868 g.A autowalk latch (g.java:7979-7992, proven) ------------

    @Test fun `autowalk latch forces scripted walk and skips the tail`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0; p.ag = -700; p.av = false
        p.gA = true; p.gB = true
        val pad = Pad(); pad.held = Pad.M_TAP_R
        pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(148, p.S, "g.A → E(); av=B; ag=0; i(148)")
        assertTrue(p.av, "av = g.B (walk facing left)")
        assertEquals(0, p.ag)
        assertFalse(p.gA, "the latch consumes itself")
        assertTrue(p.S != 233 && p.S != 21 && p.S != 22,
            "early return skips the jump gate")
    }

    @Test fun `autowalk latch faces right when gB is clear`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0; p.av = true
        p.gA = true; p.gB = false
        fsm.tick(p, Pad())
        assertEquals(148, p.S)
        assertFalse(p.av, "av = g.B (walk facing right)")
    }

    // -- L388a isHolding → cq=0 (g.java:7993-7997, proven) --------------

    @Test fun `hands full drops the jump latch`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0; p.av = false
        // `ci` clears unconditionally at az()'s head (fallback
        // g.java:12917-12942) and rebinds via the L144+ scan — which only
        // admits npcKind targets — so the held entity must be an ax11
        // victim in the world list (aA=1 arms the interact offer past
        // the L547 gate), not a free-standing local object.
        val held = mk(240, 100).let { Entity(11, null).apply {
            ak = it.ak; al = it.al; W[0] = it.W[0]; W[2] = it.W[2]
            W[1] = it.W[1]; W[3] = it.W[3] } }
        held.aB = 1; held.aA = 1
        w.npcs.add(held)
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(held, p.ci, "L275 rebind — same-tick after the clear")
        assertFalse(p.cq, "g.f() → cq=0")
        assertTrue(p.S != 233 && p.S != 21 && p.S != 22,
            "carried hands suppress the jump")
    }

    @Test fun `held entity out of reach keeps the jump latch`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0; p.av = false
        p.ci = mk(400, 100)                     // |dx|=200 ≥ 120 → f() false
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertTrue(p.S == 233 || p.S == 21 || p.S == 22,
            "out-of-range held entity does not suppress the jump")
    }

    // -- L3895 ac∈{51,66} → cq=0 (g.java:7998-8014, proven) --------------

    @Test fun `standing on crate drops the jump latch`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0
        p.ac = Entity(51, null)
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertFalse(p.cq, "ac.ax==51 → cq=0")
        assertTrue(p.S != 233 && p.S != 21 && p.S != 22)
    }

    @Test fun `standing on moving platform drops the jump latch`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0
        p.ac = Entity(66, null)
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertFalse(p.cq, "ac.ax==66 → cq=0")
        assertTrue(p.S != 233 && p.S != 21 && p.S != 22)
    }

    @Test fun `standing on a soldier keeps the jump latch`() {
        val w = floorWorld()
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 0
        p.ac = Entity(11, null)
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertTrue(p.S == 233 || p.S == 21 || p.S == 22,
            "ac.ax==11 falls through → jump fires")
    }
}
