package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 400 — the ax73 heavy-guard FSM `aJ()` (i.javap `aJ()` @0-2250,
 * i.javap.txt:32593) and its helpers re-read from the raw bytecode.
 *
 * Verified equal: S131/146/147/148/149/156/157/158/167/190-192 arms, the head
 * claim bid, `d()` awareness tiers, `e()`, `l()` sight check, `i()`/`h()`
 * counter window, `j()` intake + `C()` reaction + `g()` knockback, `aB()` strike,
 * `aG()`, `am()`, `aE()`, `aC()` scheduler (post/pre-decrement forms included),
 * the tail (`aA` block, push-past), `g(II)` press gauge, `V()`, `T()`, `G()`/`H()`.
 *
 * Fixed:
 * - every `this.g(k.aS)` was read through the PLAYER's facing (`p.faces(e)`):
 *   @103-114 the r2 intake gate, @1289-1350 / @1392-1403 the S165 hand-marker
 *   offer + release (also inverted), @1179-1202 the S171 ledge disengage;
 * - S152 ran a three-line stub instead of the real `b(i)` — the guard never
 *   started the chase on its own (it only fought after being hit);
 * - `b(i)`'s facing flip tested `g.g()` (player dead) instead of `!this.g(aS)`
 *   — also hit every ax11 soldier that spotted the player;
 * - S153/154: the overlap shortcut `aC = 3; i(155)` still ran `aC()`;
 * - S164: the arm always returns @2041 — the port fell into the tail once;
 * - S165 abort: a wall ahead (`am()`) aborts @1705-1711 — the port re-armed `ag`;
 * - S171: the ledge-ahead case is the same disengage as the band miss — the port
 *   ran a phantom `i(151)`;
 * - `aF()` has no `s != null` bail-out (@93-236) — a rider never saw the ledge;
 * - `a(true)` was `wallProbe` (the PLAYER's `g.av()` unstick: lifts `al` 20 px
 *   and leaves `W` stale, flings S43 under a low ceiling) instead of
 *   `collideSides` in S153-157 and in ax17's S170;
 * - `c(II)` only acts when `ae == null` (@0-4); `C()` variant 2 → `i(6)`; the
 *   `aE()` pin also zeroes the guard's own `ah/ag` (unobservable: S152 zeroes
 *   them at the start of every tick).
 */
class Slice400Test {
    // ------------------------------------------------------------ fixtures
    private fun guard(w: Level0World, x: Int, y: Int, s: Int, z0: Int = 0): Entity {
        val e = Entity(73, w.clips[7])
        e.aw = 9000 + w.npcs.size
        val rec = mutableListOf(73, e.aw, x, y, 0, s)
        while (rec.size < 20) rec += -1
        rec[10] = z0
        e.setPositionPx(x, y)
        w.npcFsm.initAx73(e, rec)
        w.npcs.add(e)
        return e
    }

    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    private fun solid20(w: Level0World, cx: Int, cy: Int) = w.collisionCell(cx, cy) == 20
    private fun airy(w: Level0World, cx: Int, cy: Int, rows: Int = 4) =
        (1..rows).all { w.collisionCell(cx, cy - it) < 12 }

    /** Flat run of `20` cells with open air above: `(x, feet y)` of its middle. */
    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..8).all { solid20(w, cx + it, cy) && airy(w, cx + it, cy) })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    /** Floor ending in void on its right: guard stands on the last `20` cell. */
    private fun edgeSpot(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..0).all { solid20(w, cx + it, cy) && airy(w, cx + it, cy) } &&
                w.collisionCell(cx + 1, cy) == 0 && w.collisionCell(cx + 2, cy) == 0 &&
                airy(w, cx + 1, cy) && airy(w, cx + 2, cy))
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no ledge in the level-0 map")
    }

    /** Floor with a wall one cell to the right (a solid cell 2-4 rows up, floor kept). */
    private fun wallSpot(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..1).all { solid20(w, cx + it, cy) } && (-3..0).all { airy(w, cx + it, cy) } &&
                (w.collisionCell(cx + 1, cy - 2) >= 12 || w.collisionCell(cx + 1, cy - 3) >= 12 ||
                    w.collisionCell(cx + 1, cy - 4) >= 12))
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no wall in the level-0 map")
    }

    private fun camOn(w: Level0World, x: Int, y: Int) { w.kO = x - 200; w.kP = y - 120; w.rebuildCamRect() }

    private fun place(w: Level0World, x: Int, y: Int) {
        w.player.setPositionPx(x, y); w.player.refreshBoxes(); w.player.S = 0
    }

    // ------------------------------------------------------------ head: this.g(aS)
    @Test fun `a guard facing the player is not hurt in S146, one looking away is`() {
        for (looksAway in booleanArrayOf(false, true)) {
            val w = world()
            val (gx, gy) = flatGround(w)
            val e = guard(w, gx, gy, 146)
            e.s = null
            val before = e.aB
            place(w, e.ak + 10, e.al)
            val p = w.player
            p.S = 68; p.gI = 1
            e.W.copyInto(p.X)
            p.X[0] -= 10; p.X[2] += 10
            e.av = looksAway                      // player on the right: av=false faces it
            w.npcFsm.tickAx73(e, w, p)
            if (looksAway) assertTrue(e.aB < before, "@103-114: r2 set → j() lands the hit")
            else assertEquals(before, e.aB, "the guard faces the player → r2 clear, j() skipped")
        }
    }

    // ------------------------------------------------------------ b(i) and the S152 spot
    @Test fun `S152 - a guard that spots the player turns to it, alerts and starts the chase`() {
        for (looksAway in booleanArrayOf(false, true)) {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val e = guard(w, gx, gy, 152)
            place(w, e.ak + 5, e.al)              // contact: l() @443-458 → true
            e.av = looksAway
            w.npcFsm.tickAx73(e, w, w.player)
            assertEquals(1, e.aA, "b(aS) @174 aA = 1")
            assertEquals(154, e.S, "@248-252 Z[0] != 3 → i(154)")
            assertFalse(e.av, "@101-124: faces the player after the spot (flips only when it looked away)")
        }
    }

    @Test fun `S152 - out of sight, a claim-held or an unaware guard stays idle`() {
        run {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val e = guard(w, gx, gy, 152)
            place(w, e.ak + 600, e.al)
            w.npcFsm.tickAx73(e, w, w.player)
            assertEquals(152, e.S); assertEquals(0, e.aA)
        }
        run {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val e = guard(w, gx, gy, 152)
            e.cq = true                           // @460-485: cq → no b(aS)
            place(w, e.ak + 5, e.al)
            w.npcFsm.tickAx73(e, w, w.player)
            assertEquals(152, e.S); assertEquals(0, e.aA)
        }
    }

    @Test fun `b(i) flips only a soldier that does not face the player - ax11 too`() {
        for (looksAway in booleanArrayOf(false, true)) {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val s = w.npcs.first { it.ax == 11 }
            s.setPositionPx(gx, gy); s.setAnim(2); s.refreshBoxes()
            s.aA = 0; s.cq = false
            place(w, s.ak + 5, s.al)
            s.av = looksAway
            assertTrue(spotB(s, w.player, w), "contact, LOS clear, on screen")
            assertFalse(s.av, "!this.g(aS) → av = !av; a facing soldier keeps av")
            assertEquals(5, s.S, "ax11 → i(5) @188-190")
        }
    }

    // ------------------------------------------------------------ S153/154
    @Test fun `S153 - the overlap shortcut sets aC=3 and does not run the scheduler`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = guard(w, gx, gy, 153)
        e.av = false
        place(w, e.ak, e.al)                      // body on the guard, e.af == null
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(155, e.S, "@642-654 aC = 3; i(155)")
        assertEquals(3, e.aC, "goto 2042 — aC() @657 is NOT called (it would post-decrement to 2)")
    }

    // ------------------------------------------------------------ S164
    @Test fun `S164 - the finishing tick returns, it never falls into the tail`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = guard(w, gx, gy, 164)
        w.kBK = false
        w.player.gJ = 0
        e.aA = 1                                  // the tail would set P|16 again
        place(w, e.ak, e.al)
        val pAk = w.player.ak
        finish(e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertTrue(e.P and 32 != 0 && e.P and 64 != 0)
        assertEquals(0, e.P and 16, "@2041 return: the aA block's `P |= 16` never runs")
        assertEquals(pAk, w.player.ak, "…and neither does the a() push-past")
    }

    @Test fun `S164 - the death grants bit 2 with g_g and requests it with g_h, no claim script (slice 413)`() {
        // @2021-2040: `if ((g.J & 2) == 0) { g.g(2); k.aS.h(2) }` — `g.g(int)` is `J |= 2; k.q()`,
        // `k.aS.h(2)` is `g.h(I)Z` (the equip request: I = 2, k.at = 1), NOT the claim-script bind
        // `i.h(I)V` the port called
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = guard(w, gx, gy, 164)
        w.kBK = false
        val p = w.player
        p.gJ = 0; p.gI = 4; p.ca = -1
        w.equipList.fill(-1); w.equipCount = 0
        place(w, e.ak, e.al)
        finish(e)
        w.npcFsm.tickAx73(e, w, p)
        assertEquals(2, p.gJ and 2, "g.g(2): J |= 2")
        assertEquals(2, p.gI, "g.h(2): I = 2")
        assertEquals(2, w.equipList[0], "g.g's k.q(): the ar[] equip list now holds the new bit")
        assertEquals(1, w.equipCount)
        assertEquals(-1, p.ca, "no claim script is bound on the player")
    }

    @Test fun `S152 - the ceiling ambush drop-kill requests equip 1 with g_h, no claim script (slice 413)`() {
        // aE() @129: `g.x[1] = 0; k.aS.h(1); ah = ag = aj = ai = 0; al = this.al; i(20)` — h is `g.h(I)Z`
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = guard(w, gx, gy, 152)
        e.Z[14] = 1
        val p = w.player
        p.gJ = 1; p.gI = 4; p.ca = -1
        place(w, e.ak, e.al - 45)                  // feet above the guard's mid-line, boxes overlap
        p.setAnim(43); p.refreshBoxes()
        p.gy = e.al - 400                          // a fall of 20+ cells → the drop-kill arm
        w.npcFsm.tickAx73(e, w, p)
        assertEquals(20, e.S, "the guard goes to S20")
        assertEquals(1, p.gI, "g.h(1): I = 1")
        assertEquals(-1, p.ca, "no claim script is bound on the player")
        assertEquals(0, p.x1, "g.x[1] = 0")
    }

    // ------------------------------------------------------------ S165
    @Test fun `S165 - the hand marker is offered while the guard faces the player, released when it does not`() {
        run {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val e = guard(w, gx, gy, 165)
            e.av = false; e.am = e.ak
            place(w, e.ak + 90, e.al)
            w.player.av = true; w.player.aZ = false
            w.npcFsm.tickAx73(e, w, w.player)
            val m = e.ae
            assertNotNull(m, "@1289-1350: this.g(aS) && (av ^ aS.av) && |dy| < 10 → c()")
            assertEquals(14, m.ax)
        }
        run {
            val w = world()
            val (gx, gy) = flatGround(w)
            camOn(w, gx, gy)
            val e = guard(w, gx, gy, 165)
            e.av = true; e.am = e.ak                 // looks left, the player stands on its right
            e.ae = Entity(14, null)
            place(w, e.ak + 90, e.al)
            w.player.av = false; w.player.aZ = false
            w.npcFsm.tickAx73(e, w, w.player)
            assertNull(e.ae, "@1392-1403: !this.g(aS) → G()")
        }
    }

    @Test fun `S165 - c() leaves an existing marker alone`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        camOn(w, gx, gy)
        val e = guard(w, gx, gy, 165)
        e.av = false; e.am = e.ak
        val stub = Entity(14, w.clips[9])         // a clip-9 bubble, not the clip-74 hand: T() is false
        e.ae = stub
        place(w, e.ak + 90, e.al)
        w.player.av = true; w.player.aZ = false
        w.iL = -1; w.iM = -1
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(stub, e.ae)
        assertEquals(-1, w.iL, "@0-4: `ae != null → return` — o(x,y) never pins L/M for it")
    }

    @Test fun `S165 - a wall ahead aborts the chase to S171`() {
        val w = world()
        val (gx, gy) = wallSpot(w)
        val e = guard(w, gx, gy, 165)
        e.av = false; e.am = e.ak                  // faces the wall on its right
        place(w, e.ak - 60, e.al)
        w.player.av = false; w.player.aZ = false
        assertTrue(ceilingProbe73(e, w), "fixture: am() sees the wall")
        assertFalse(edgeAhead73(e, w), "fixture: no ledge ahead")
        e.P = e.P or 64; e.Z[8] = 1
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(171, e.S, "@1705-1711: am() != 0 falls into the abort block")
        assertEquals(0, e.P and 64, "P &= -65")
        assertEquals(0, e.Z[8])
    }

    // ------------------------------------------------------------ S171
    @Test fun `S171 - variant 3, a ledge ahead while the guard faces the player, disengages`() {
        val w = world()
        val (gx, gy) = edgeSpot(w)
        val e = guard(w, gx, gy, 171, z0 = 3)
        e.av = false                               // faces right, the void side
        e.s = null
        place(w, e.ak + 40, e.al)                  // inside the sight band, on its facing side
        assertTrue(edgeAhead73(e, w), "fixture: aG() sees the ledge")
        finish(e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(152, e.S, "@1189-1200: aG() && this.g(aS) → i(152); aA = 0; return")
        assertEquals(0, e.aA)
    }

    // ------------------------------------------------------------ aF()
    @Test fun `aF - a rider on a crate still reads the tile ledge behind it`() {
        val w = world()
        val (gx, gy) = edgeSpot(w)
        val e = guard(w, gx, gy, 155)
        e.av = true                                // aF's av arm: the cell on the RIGHT (void)
        e.refreshBoxes()
        e.s = null
        assertTrue(crateEdge73(e, w), "baseline: on the ledge")
        e.s = Entity(51, null)                     // riding a crate whose edge is far away
        assertTrue(crateEdge73(e, w), "@93-164: no `s != null` bail-out — the tile arms run")
        assertFalse(edgeAhead73(e, w), "…whereas aG() @107 returns 0 for a rider")
    }

    // ------------------------------------------------------------ a(true)
    @Test fun `S155 - the side collide refreshes the box, so no phantom ledge zeroes the walk`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = guard(w, gx, gy, 155)
        e.av = false; e.aq = 0; e.j = 0
        place(w, e.ak + 1000, e.al)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(-512, e.ag, "walks on flat ground")
        val box = e.W.copyOf()
        e.refreshBoxes()
        assertContentEquals(e.W, box, "a(true) ends with t(): W matches the entity position")
    }

    @Test fun `ax17 S170 - a(true) is the side collide, the box stays in step with al`() {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = Entity(17, w.clips[7])
        val rec = mutableListOf(17, 152, gx, gy, 0, 57, 0, 0, 0, -1, 0, 0, -1, 100, -1)
        e.setPositionPx(gx, gy)
        w.npcFsm.initAx17(e, rec)
        w.npcs.add(e)
        e.al = gy - 80; e.O = e.al shl 8            // falling through open air: S170 stays
        e.setAnim(170); e.refreshBoxes()
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(170, e.S, "still falling")
        val box = e.W.copyOf()
        e.refreshBoxes()
        assertContentEquals(e.W, box, "@464-: `a(1)` ends with t() — W matches al")
    }

    // ------------------------------------------------------------ small arms
    @Test fun `C() - variant 2 reacts with i(6), the others with i(156)`() {
        for (z0 in intArrayOf(2, 1)) {
            val w = world()
            val (gx, gy) = flatGround(w)
            val e = guard(w, gx, gy, 146, z0 = z0)
            e.s = null
            e.av = true                              // looks away: r2 set
            place(w, e.ak + 10, e.al)
            val p = w.player
            p.S = 68; p.gI = 1                       // (S69 would open the i() counter window first)
            e.W.copyInto(p.X)
            p.X[0] -= 10; p.X[2] += 10
            e.aB = 600
            w.npcFsm.tickAx73(e, w, p)
            assertEquals(if (z0 == 2) 6 else 156, e.S, "@194-213 / @274-290")
        }
    }
}
