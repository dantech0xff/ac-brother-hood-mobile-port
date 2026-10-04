package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 407 — the ax15 block `bu()`, the ax16 knife `bb()` S17 arm and the
 * mounted-rope input `g.k()`, re-read from the raw bytes (`i.javap.txt`
 * `bu()` @338-376 / @762-938, `bb()` @470-719, `g.javap.txt` `k()` @0-640).
 *
 * - `bu()` @350-376: `if (a(aS.W, W)) goto 376; if (aS.ac != this) goto 1095` — the
 *   pushout + hang runs for an overlap OR a player already claimed by the block; the
 *   port sent the claimed-but-not-overlapping player into the capture arm instead.
 * - `bu()` @929: `aS.a(2560)` is the join of both pushout `if`s — the fling after the
 *   "outside the span" test is unconditional (the port flung inside the two arms only).
 * - `bb()` S17 @470-719: `g.b() && !bR` is the bounce; every other combination — also an
 *   attacking player and a knife already bounced once — falls to `aS.a(4,0,0,r1); r2 = 1`.
 * - `g.k()` @0-25 returns on `bM.aG == 1`; the pump arms are `clamp` **or** `±512`
 *   (@183-212 / @284-313), never both; the descend arm tests `bO` only (@485-499).
 */
class Slice407Test {
    /** Level-0 ground with open air above: a spot where `hangOnEdge`'s ceiling probe is 0. */
    private fun openGround(w: Level0World): Pair<Int, Int> {
        for (cy in 12 until 90) for (cx in 8 until 400) {
            if ((-8..8).all { w.collisionCell(cx + it, cy) == 20 } &&
                (-8..8).all { dx -> (1..8).all { r -> w.collisionCell(cx + dx, cy - r) == 0 } })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no open ground in the level-0 map")
    }

    /** ax15 in S10 — the only clip-25 state with a real box: `[ak-31, al-50, ak+26, al+1]`. */
    private fun block(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(15, w.clips[25]); e.setPositionPx(x, y)
        val f = mutableListOf(15, 0, x, y, 0, 10, 0, 0, -1)
        for (i in 9..15) f += 0
        w.npcFsm.initAx15(e, f, w)
        e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    private fun place(p: Entity, x: Int, y: Int, s: Int, av: Boolean = false) {
        p.setPositionPx(x, y); p.setAnim(s); p.av = av; p.refreshBoxes()
        p.ga = null; p.ac = null; p.ag = 0; p.ah = 0
    }

    // ------------------------------------------------------------------ ax15 gC arm

    @Test fun `ax15 gC arm - a player already claimed by the block is pushed out and hung even without overlap`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        place(p, gx - 50, gy, 0)
        p.ac = e
        assertTrue(!Entity.overlapStrict(p.W, e.W), "fixture: the boxes do not touch")
        assertTrue(p.inFrontOf(e) && p.al > (e.W[1] + e.W[3]) shr 1, "fixture: L37 front/below gate")
        w.npcFsm.tickAx15(e, w, p)
        assertEquals(109, p.S, "@376 → hangOnEdge: p.i(109) for an S10 block")
        assertSame(e, p.ga, "hangOnEdge: g.a = this")
        assertEquals(e.W[0], p.ak, "hangOnEdge: the player rests on the near edge W[0]")
    }

    @Test fun `ax15 gC arm - neither overlap nor claim skips the pushout (goto 1095)`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        place(p, gx - 50, gy, 0)
        w.npcFsm.tickAx15(e, w, p)
        assertEquals(0, p.S); assertNull(p.ga); assertEquals(gx - 50, p.ak)
    }

    @Test fun `ax15 gC arm - an overlapping player hangs as before`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        place(p, gx - 30, gy, 0)
        assertTrue(Entity.overlapStrict(p.W, e.W), "fixture: overlap")
        w.npcFsm.tickAx15(e, w, p)
        assertEquals(109, p.S); assertSame(e, p.ga)
    }

    // ------------------------------------------------------------------ ax15 capture arm

    @Test fun `ax15 capture arm - the fling after the outside-span test is unconditional`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        // p.ak == W[0]: inside the box (inclusive point test) and on the span edge.
        place(p, e.W[0], gy - 35, 22)
        assertTrue(Entity.pointInBox(p.ak, p.al, e.W) && p.ag == 0)
        w.npcFsm.tickAx15(e, w, p)
        assertEquals(43, p.S, "@929 aS.a(2560) → enter 43")
        assertEquals(2560, p.ah)
        assertNull(p.ga)
    }

    @Test fun `ax15 capture arm - running into the side pushes out and flings`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        place(p, e.W[0], gy - 35, 22)
        p.ag = 512
        w.npcFsm.tickAx15(e, w, p)
        val half = ((p.W[2] - p.W[0]) / 2) + ((e.W[2] - e.W[0]) / 2)
        assertEquals(e.ak - half, p.ak, "@762-: ak = this.ak - pw/2 - ew/2")
        assertEquals(43, p.S); assertEquals(2560, p.ah)
    }

    @Test fun `ax15 capture arm - inside the span the player is claimed`() {
        val w = world(); w.npcs.clear(); val (gx, gy) = openGround(w)
        val e = block(w, gx, gy); val p = w.player
        place(p, gx, gy - 35, 22)
        w.npcFsm.tickAx15(e, w, p)
        assertSame(e, p.ga, "g.a = this")
        assertEquals(5, p.S)
        assertEquals(e.W[1] + 1, p.al)
    }

    // ------------------------------------------------------------------ ax16 S17

    private fun knife(w: Level0World, bR: Boolean): Entity {
        val m = Entity(16, w.clips[10]).apply {
            S = 17; aB = 10; setPositionPx(300, 100)
            X[0] = 250; X[1] = 50; X[2] = 350; X[3] = 150
            this.bR = bR; ag = 0; ah = 0
        }
        w.npcs.add(0, m)
        return m
    }

    private fun arm(w: Level0World, attacking: Boolean) {
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes(); p.gt = 0
        if (attacking) { p.gI = 1; p.S = 67 } else { p.gI = 0; p.S = 0 }
        assertEquals(attacking, w.playerAttacking())
    }

    @Test fun `bb S17 - a knife flying at an attacking player bounces on the first contact`() {
        val w = world(); w.npcs.clear(); val p = w.player
        arm(w, true)
        val m = knife(w, bR = false)
        w.npcFsm.tickRequestMarker(m, p, Pad())
        assertEquals(17, m.S, "g.b() && !bR → the L58 bounce, the knife keeps flying")
        assertTrue(m.bR, "bR set by the bounce")
        assertEquals(67, p.S, "no hit")
    }

    @Test fun `bb S17 - a knife that was already bounced hits the attacking player (L707)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        arm(w, true)
        val m = knife(w, bR = true)
        w.npcFsm.tickRequestMarker(m, p, Pad())
        assertEquals(16, m.S, "r2 = 1 → i(16) rest")
        assertEquals(43, p.S, "aS.a(4) on an attacking player is upgraded to op 18: knocked down")
    }

    @Test fun `bb S17 - a knife hits a player who is not attacking`() {
        val w = world(); w.npcs.clear(); val p = w.player
        arm(w, false)
        val m = knife(w, bR = false)
        w.npcFsm.tickRequestMarker(m, p, Pad())
        assertEquals(16, m.S)
    }

    // ------------------------------------------------------------------ g.k() rope input

    private fun rope(w: Level0World, aG: Int, bN: Int = 5): Entity {
        val e = Entity(13, null)
        e.setPositionPx(100, 100)
        val f = mutableListOf(13, 0, 100, 100, when (aG) { 1 -> 1; 2 -> 2; 4 -> 3; else -> 0 }, 0, 0)
        for (i in 7..15) f += 0
        w.npcFsm.initAx13(e, f)
        e.Z[1] = 10; e.bN = bN
        e.aA = 1; e.bM = w.player; w.player.bM = e
        w.player.setAnim(326)
        w.npcs.add(e)
        return e
    }

    @Test fun `g_k returns at once on an aG1 rope (bM_aG == 1 at 14-25)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = rope(w, 1)
        w.pad.held = Pad.M_UP
        p.ropeInput(w)
        assertEquals(5, e.bN, "no climb")
        assertEquals(326, p.S, "no anim change")
    }

    @Test fun `g_k right pump - clamp to -1280 OR add 512, never both (183-212)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = rope(w, 0)
        e.bP = 0; e.bO = 0
        w.pad.held = Pad.M_RIGHT
        p.ropeInput(w)
        assertEquals(-1280, e.bP); assertEquals((20480 - 1280) / 80, e.bO)
        p.ropeInput(w)
        assertEquals(-768, e.bP, "bP <= -1280 → += 512"); assertEquals(240 + (20480 - 768) / 80, e.bO)
        p.ropeInput(w)
        assertEquals(-1280, e.bP, "bP > -1280 → clamp again"); assertEquals(240 + 246 + 240, e.bO)
    }

    @Test fun `g_k left pump - clamp to 1280 OR subtract 512, never both (284-313)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = rope(w, 0)
        e.bP = 0; e.bO = 0
        w.pad.held = Pad.M_LEFT
        p.ropeInput(w)
        assertEquals(1280, e.bP); assertEquals(-((20480 - 1280) / 80), e.bO)
        p.ropeInput(w)
        assertEquals(768, e.bP, "bP >= 1280 → -= 512"); assertEquals(-240 - (20480 - 768) / 80, e.bO)
        p.ropeInput(w)
        assertEquals(1280, e.bP); assertEquals(-240 - 246 - 240, e.bO)
    }

    @Test fun `g_k descend zeroes the swing only when bO is nonzero (485-499 test bO twice)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = rope(w, 0)
        w.pad.held = Pad.M_DOWN
        e.bO = 0; e.bP = 700
        p.ropeInput(w)
        assertEquals(6, e.bN, "bO == 0 → the descent proceeds")
        assertEquals(83, p.S)
        assertEquals(700, e.bP, "bP is left alone")
        e.bO = 9; e.bP = 700
        p.ropeInput(w)
        assertEquals(6, e.bN, "bO != 0 → zero both and return")
        assertEquals(0, e.bO); assertEquals(0, e.bP)
    }

    @Test fun `g_k climb zeroes the swing when bO or bP is nonzero (364-388)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = rope(w, 0)
        w.pad.held = Pad.M_UP
        e.bO = 0; e.bP = 700
        p.ropeInput(w)
        assertEquals(5, e.bN); assertEquals(0, e.bP)
    }
}
