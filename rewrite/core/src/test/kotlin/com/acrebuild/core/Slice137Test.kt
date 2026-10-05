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

/** Slice 137 — wall-run family: `g.q`/`g.f` statics, ax10 S10 zone arm,
 *  S317 wall-run dash, S102 wall-cling, S332 wall-shimmy, S17 wall-kick. */
class Slice137Test {

    private fun mk(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al; p.av = av
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S102 cling grounded runs l consume g2737`() {
        Entity.gq = false; Entity.gf = null
        // feet cell 12 → aR=12 → aZ stays true through the head rescan
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 102; p.aC = 18
        fsm.tick(p, Pad())
        assertEquals(1, p.S, "l() → aw() settle → i(1) idle")
        assertEquals(18, p.aC, "aC untouched when grounded")
    }

    @Test fun `S102 cling countdown expiry flings g2741`() {
        Entity.gq = false
        // feet cell 4 → aR=4 (wall edge marker); <10 non-5 → aZ false
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 4 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 102; p.aC = 0
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "aC<=0 → a(0) fling")
        assertEquals(131, p.al, "fling's +10 plus the arm's +21 drop")
        assertEquals(-1, p.aC, "aC decremented (i15-1)")
    }

    @Test fun `S102 cling dir-held enters shimmy g2745`() {
        Entity.gq = false
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 4 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 102; p.aC = 18
        val pad = Pad(); pad.held = Pad.M_LEFT
        fsm.tick(p, pad)
        assertEquals(332, p.S)
        assertTrue(p.av, "av = true for LEFT")
    }

    @Test fun `S102 cling up-edge kicks to S17 in E-zone g2751`() {
        Entity.gq = false; Entity.gE = true   // inside S31 zone → !E tail off
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 4 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 102; p.aC = 18
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(17, p.S, "g.E set → arm's i(17) survives the tail")
        Entity.gE = false
    }

    @Test fun `S102 cling up-edge outside E-zone jumps S233 g788`() {
        Entity.gq = false; Entity.gE = false
        // feet cell 12 → aZ survives the rescan → the arm calls l()
        // which arms cp/cq/z — postTail's jump gate fires faithfully
        // (e()'s head cleared the flags; l() re-arms them this tick).
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 102; p.aC = 18
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(233, p.S, "no E-zone → cq&&!E tail overrides i(17)")
    }

    @Test fun `S332 shimmy anim-end re-enters cling g4124`() {
        Entity.gq = false
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 4 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 332
        p.ag = 999; p.ah = 999
        fsm.tick(p, Pad())
        assertEquals(102, p.S, "r() → i(102)")
        assertEquals(18, p.aC)
        assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
    }

    @Test fun `S332 shimmy up-edge kicks to S17 in E-zone g4113`() {
        Entity.gq = false; Entity.gE = true
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 4 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 332
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(17, p.S)
        Entity.gE = false
    }

    @Test fun `S17 wall-kick winds up then launches g1490`() {
        Entity.gq = false
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100, av = false); p.S = 17
        p.ag = 999; p.ah = 999
        fsm.tick(p, Pad())                      // null clip → r() fires
        assertEquals(18, p.S)
        assertEquals(2048, p.ag, "ag = av?-2048:2048")
        assertEquals(-5120, p.ah)
    }

    @Test fun `S317 dash velocities and UP hop g4065`() {
        Entity.gq = false; Entity.gf = null; Entity.gE = true
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100, av = true); p.S = 317
        p.aR = 4; p.bb = false; p.bc = false
        p.ah = 9999
        val pad = Pad(); pad.queuePress(Pad.M_UP); pad.commit(0)
        fsm.tick(p, pad)
        assertEquals(-2560, p.ag, "ag = av?-2560:2560")
        assertEquals(-2048, p.ah, "v(16388) hop overrides the 512 cap")
        assertEquals(128, p.aj)
        assertEquals(317, p.S, "no wall — stays in run")
        Entity.gE = false
    }

    @Test fun `S317 wall-hit stops run and releases link out-of-zone g4080`() {
        Entity.gq = false; Entity.gf = null
        // feet cell 12 → aR=12 ≥12 → wall/contact arm fires
        val w = Slice134Test.PassWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100, av = true); p.S = 317
        val marker = Entity(14, null); p.ae = marker
        fsm.tick(p, Pad())
        assertEquals(0, p.ag, "ag zeroed on wall contact")
        assertNull(p.ae, "!q → G() releaseAe")
        assertNull(Entity.gf, "clip30 unconverted → no spawn (k.z[30]==null)")
        assertEquals(317, p.S, "no marker → no i(50) yet")
    }

    @Test fun `ax10 S46 zone latches and clears gq i9343`() {
        Entity.gq = false
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = NpcFsm(w)
        // L1e1 is aV() case 46, not S10 — slice 141 corrected the label.
        val zone = Entity(10, null); zone.S = 46
        zone.W[0] = 190; zone.W[2] = 230; zone.W[1] = 60; zone.W[3] = 140
        val p = mk(200, 100)
        fsm.tickTrigger(zone, w, p, Pad())
        assertTrue(Entity.gq, "overlap → g.q")
        assertSame(zone, p.gd, "g.d = zone")
        // leave the zone
        p.ak = 900; p.W[0] = 890; p.W[2] = 910
        fsm.tickTrigger(zone, w, p, Pad())
        assertFalse(Entity.gq, "left + still owner → g.q cleared")
        assertNull(p.gd)
    }

    @Test fun `ax10 S31 zone latches and clears gE i9830`() {
        Entity.gE = false
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = NpcFsm(w)
        val zone = Entity(10, null); zone.S = 31
        zone.W[0] = 190; zone.W[2] = 230; zone.W[1] = 60; zone.W[3] = 140
        val p = mk(200, 100)
        fsm.tickTrigger(zone, w, p, Pad())
        assertTrue(Entity.gE, "overlap → g.E")
        assertEquals(0, zone.P and 128, "P &= ~128 while overlapped")
        p.ak = 900; p.W[0] = 890; p.W[2] = 910
        fsm.tickTrigger(zone, w, p, Pad())
        assertFalse(Entity.gE, "left → g.E cleared")
        assertEquals(128, zone.P and 128, "P |= 128 while empty")
    }

    @Test fun `ax10 S31 Z0 nonzero sets icu i9857`() {
        Entity.gE = false; Entity.icu = false
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = NpcFsm(w)
        val zone = Entity(10, null); zone.S = 31
        zone.W[0] = 190; zone.W[2] = 230; zone.W[1] = 60; zone.W[3] = 140
        zone.Z[0] = 1
        val p = mk(200, 100)
        fsm.tickTrigger(zone, w, p, Pad())
        assertTrue(Entity.icu, "Z[0]!=0 → i.cu")
        Entity.icu = false
    }
}
