package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 402 — the ax11 `I()` arms re-read from the raw bytecode (i.javap
 * `I()` @1644-7691), arm by arm: where each one EXITS (`goto 7691` = L849, the
 * dispatch tail only; `goto 7292` = the shared L777 tail), which of the
 * `r4/r5/r6` flags it sets, and the helpers behind them.
 *
 * Verified equal: S168/S169/S142/S143/S138/S145/S133/S134/S99/S85 (arms and
 * exits), S174, S176/S177/S140, patrol S2/S3/S92, S5, S11, S16, S1, S117, S179,
 * S184, S139, the shared tail @7292-7660 (apart from the parked alerted-solid
 * finding, plan 401), `h()`/`i()`/`aB()`/`aD()`/`aI()`/`m(II)`/`g(II)`.
 *
 * Fixed:
 * - **S21** (`k()`'s stealth-kill victim state) had no arm: a stabbed soldier
 *   looped its death anim forever at full HP, still targetable. @6699-6879 it
 *   bleeds, then `aB=0; aA=2; P&=-17; P|=64|32` (inactive, not drawn);
 * - **S0/106/107/135** @6141: a wave member (`aw >= 5000`) is simply removed when
 *   the death anim ends — no corpse state, no kill tally;
 * - **S175** @3709-3955: the prompt marker is the SOLDIER's (`r2.a(8,…)`, the
 *   player's own `ae` held it forever), and every live path ends `goto 7292` —
 *   only the `g.g()` dead release returns;
 * - **S17** @5230-5339: a landed counter always `goto 7691` (S8 or not);
 * - **S144** @5342-5451: no `h()/i()` engage in the arm — the soldier blocks with
 *   the arm's own `aS.i(8)` recoil and stays in S144 (the port's "precedence"
 *   threw it into S17 → S11 → S12);
 * - **S12** @4807-4947: `r6 = 0`, and the `aN=this; g.E=1; b(2)` bind runs for
 *   `Z0==2 || (Z0==0 && aB <= bu/2)`; every counter-bind `goto 7691`;
 * - **S4/22** @4679 and **S23** @4604: `r6 = 0` (stealth-kill gate OFF);
 * - **S24** @6077-6125: the release picks the OPEN side (`aT < 12` → left,
 *   else `aU < 12` → right) — both compares were inverted;
 * - **S25** @5714-5877: only a crate/carrier under the soldier (`aD()`) reaches
 *   the tail; the free fall `goto 7691` (the tail's open-cell gate replayed
 *   `k.A(24)` every tick of a fall);
 * - **S27** @6923-6946: the tail runs while the anim plays;
 * - **S180-183** @7048-7078: `goto 7691` (no tail);
 * - **S6** @5468: `aF()` is the trailing-side ledge probe (`crateEdge73`) —
 *   `Entity.aF` had the cell test inverted;
 * - **S18** @5030-5227: `a(true)` once; the anim-end block ends `goto 7292`
 *   (the port looped back into the offer: a second `nextInt()` on the last tick);
 * - **S20** @6893: `aB > 0 → aB = 0` (the port's store was a no-op);
 * - **`k()`** @260-375: the marker link drops when the player does NOT face the
 *   soldier OR the soldier faces the player, and the stab window is
 *   `(aS.g(this) && !this.g(aS)) || aS.S == 38` — the hanging-ledge kill is
 *   offered whatever the facing (the port released instead).
 */
class Slice402Test {
    // ------------------------------------------------------------ fixtures
    private fun solid20(w: Level0World, cx: Int, cy: Int) = w.collisionCell(cx, cy) == 20
    private fun airy(w: Level0World, cx: Int, cy: Int, rows: Int = 4) =
        (1..rows).all { w.collisionCell(cx, cy - it) < 12 }

    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..8).all { solid20(w, cx + it, cy) && airy(w, cx + it, cy) })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    /** Open air: a 7x9 block of empty cells (`< 12`, also not support `>= 5`). */
    private fun airSpot(w: Level0World): Pair<Int, Int> {
        for (cy in 12 until 80) for (cx in 8 until 390) {
            if ((-3..3).all { dx -> (-6..2).all { dy -> w.collisionCell(cx + dx, cy + dy) < 5 } })
                return (cx * 20 + 10) to (cy * 20)
        }
        error("no open air in the level-0 map")
    }

    private fun soldier(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = w.npcs.first { it.ax == 11 }
        e.setPositionPx(x, y)
        e.Z[0] = 0; e.cq = false; e.aB = 300; e.aA = 1; e.s = null; e.ae = null
        e.setAnim(s); e.refreshBoxes()
        return e
    }

    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    private fun place(w: Level0World, x: Int, y: Int, s: Int = 0) {
        val p = w.player
        p.setPositionPx(x, y); p.S = s; p.refreshBoxes()
    }

    // ------------------------------------------------------------ S175
    @Test fun `S175 - the prompt marker is the soldier's, released with it`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 175); e.bl = 40
        val p = w.player
        place(w, gx, gy, 310)
        w.npcFsm.tick(e, p)
        assertNotNull(e.ae, "r2.a(8,…) @3735-3753: the receiver is the soldier")
        assertNull(p.ae, "…not the player")
        e.bl = 0                                           // the gauge ran dry
        w.npcFsm.tick(e, p)
        assertEquals(177, e.S, "@3831-3868 the emptied gauge releases")
        assertNull(e.ae, "G() frees the marker with the grab")
        assertNull(p.ae)
    }

    @Test fun `S175 - a live hold runs the shared tail, the dead release does not`() {
        val (ax, ay) = airSpot(world())
        // hold in open air: the L777 open-cell gate (i(25) + k.A(24)) is reached
        val w = world()
        val e = soldier(w, ax, ay, 175); e.bl = 40
        place(w, ax, ay, 310)
        w.sfxLog.clear()
        w.npcFsm.tick(e, w.player)
        assertEquals(25, e.S, "@3898 → 7292: the tail's fall gate fires")
        assertTrue(24 in w.sfxLog)
        // the player dead (`g.g()` @3709) → i(177) and straight out, tail skipped
        val w2 = world()
        val e2 = soldier(w2, ax, ay, 175); e2.bl = 40
        place(w2, ax, ay, 310)
        w2.player.x1 = 0
        w2.sfxLog.clear()
        w2.npcFsm.tick(e2, w2.player)
        assertEquals(177, e2.S, "@3715-3732 i(177) → 7691")
        assertFalse(24 in w2.sfxLog, "no tail, no fall gate")
    }

    // ------------------------------------------------------------ S17 / S144
    private fun swingAt(w: Level0World, e: Entity) {
        val p = w.player
        place(w, e.ak + 5, e.al, 67)                       // S67 T1 = the real box
        p.av = true; p.T = 1; p.gI = 1
        p.refreshBoxes()
        assertTrue(Entity.overlapStrict(p.X, e.W), "fixture: the swing reaches the soldier")
    }

    @Test fun `S17 - a landed counter always leaves the arm`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 17); e.aC = 16
        swingAt(w, e)
        finish(e)                                          // r() would send it to S23
        w.npcFsm.tick(e, w.player)
        assertEquals(8, w.player.S, "aS.i(8) @5282-5287")
        assertEquals(17, e.S, "@5304 goto 7691 — the r() check @5307 is skipped")
        // no swing in reach: the arm runs on to r() → i(23)
        val w2 = world()
        val e2 = soldier(w2, gx, gy, 17); e2.aC = 16
        place(w2, gx + 300, gy)
        finish(e2)
        w2.npcFsm.tick(e2, w2.player)
        assertEquals(23, e2.S)
    }

    @Test fun `S144 - the weakened block recoils the player and stays put`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 144); e.Z[0] = 2; e.aC = 0
        swingAt(w, e)
        w.npcFsm.tick(e, w.player)
        assertEquals(8, w.player.S, "the arm's own counter @5395-5425")
        assertEquals(144, e.S, "no h()/i() engage: still S144, not S17")
        assertNotEquals(16, e.aC, "aC=16 belongs to i()'s engage")
        // the block ends by itself: r() → i(23) + aS.G()
        place(w, gx + 300, gy)
        finish(e)
        w.npcFsm.tick(e, w.player)
        assertEquals(23, e.S)
    }

    // ------------------------------------------------------------ S12
    @Test fun `S12 - the counter-bind runs for Z0==0 only at half hp, then always exits`() {
        for (half in booleanArrayOf(true, false)) {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 12)
            e.aB = if (half) 150 else 300                  // bu = 300
            e.T = 4; e.refreshBoxes()
            val p = w.player
            place(w, e.ak, e.al, 6)
            p.av = e.ak < p.ak
            w.npcFsm.tick(e, p)
            assertEquals(18, e.S, "the bind always lands i(18)")
            assertEquals(half, w.lockTarget === e, "aN = this only for aB <= bu/2")
            assertEquals(half, w.iAH, "b(2) slow-mo only then")
        }
    }

    // ------------------------------------------------------------ r6 = 0
    /** The player stands on the soldier's left, facing it (`aS.g(e)` true); the
     *  soldier looks away (`e.av = false` → `e.g(aS)` false) with `j == 0`:
     *  the window of `k()` @344-375 is open and the stab edge is pressed. */
    private fun stabs(s: Int): Boolean {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, s)
        val p = w.player
        place(w, gx - 30, gy, 0)
        p.av = false
        e.av = false
        e.j = 0
        w.pad.commit(65568)
        w.npcFsm.tick(e, p)
        return e.S == 21
    }

    @Test fun `r6 - the chase, the windup retreat and the strike have the stealth kill off`() {
        assertTrue(stabs(2), "control: patrol (r6 = 1) is stabbed from behind")
        for (s in intArrayOf(12, 23, 4, 22))
            assertFalse(stabs(s), "S$s sets r6 = 0 (@${if (s == 12) 4813 else if (s == 23) 4604 else 4682})")
    }

    /** `k()` is not even entered with r6 = 0: its @260-284 marker drop (`aS.g(this)
     *  == 0 || this.g(aS)` → `G()`) is the observable — S4/S22/S23 turn to face the
     *  player first (`Q()`), so `k()`'s stab window is shut either way, but with r6
     *  left at the head default the facing soldier's `ae` marker would be dropped. */
    @Test fun `r6 - k() never runs for the chase and the retreat, so a held marker survives`() {
        fun keeps(s: Int, soldierAv: Boolean): Boolean {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, s)
            val p = w.player
            place(w, gx - 60, gy, 0)
            p.av = false                                   // faces the soldier
            e.av = soldierAv
            e.spawnMarker(w, 8, e.ak, e.al - 85)
            val marker = e.ae
            w.npcFsm.tick(e, p)
            return e.ae === marker
        }
        // head default r6 = !g(aS) = ON while the soldier looks away; the arm clears it
        for (s in intArrayOf(4, 22, 23))
            assertTrue(keeps(s, soldierAv = false), "S$s: r6 = 0 — k() skipped, marker kept")
        // control: patrol sets r6 = 1 itself; facing the player, k() @260-284 drops the link
        assertFalse(keeps(2, soldierAv = true), "S2: k() runs → G() drops the marker")
    }

    // ------------------------------------------------------------ S21
    @Test fun `S21 - the stabbed soldier dies, goes inactive and is not tallied`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 21)
        place(w, gx + 400, gy)
        val kills = w.kAp[0]
        var n = 0
        while (e.P and 32 == 0 && n++ < 200) {
            w.npcFsm.tick(e, w.player)
            e.advanceAnim()
        }
        assertEquals(0, e.aB, "@6794-6803 aB = 0")
        assertEquals(2, e.aA)
        assertEquals(0, e.P and 16, "P &= -17")
        assertEquals(64, e.P and 64); assertEquals(32, e.P and 32)
        assertEquals(21, e.S, "no corpse state follows")
        assertEquals(kills, w.kAp[0], "the arm tallies nothing (@6699-6879 has no k.e)")
    }

    // ------------------------------------------------------------ S0 group
    @Test fun `S0 - a wave member is removed when its death anim ends, anyone else turns corpse`() {
        for (wave in booleanArrayOf(true, false)) {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 0)
            e.aw = if (wave) 5000 + 3 else 700 + 1
            e.aB = 0
            place(w, gx + 400, gy)
            finish(e)
            val kills = w.kAp[0]
            w.npcFsm.tick(e, w.player)
            if (wave) {
                assertTrue(e in w.pendingRemove, "@6141-6162 aw >= 5000 && r() → k.c(this)")
                assertEquals(0, e.S)
                assertEquals(kills, w.kAp[0], "no tally")
            } else {
                assertEquals(139, e.S, "everyone else: i(139) corpse")
                assertEquals(kills + 1, w.kAp[0], "k.e(0,aw)")
            }
        }
    }

    // ------------------------------------------------------------ S24
    /** A flat spot where the S24 side probes `(W[0]-pw, W[1])` / `(W[2]+pw, W[1])`
     *  read the wanted solidity. */
    private fun sideSpot(w: Level0World, leftSolid: Boolean, rightSolid: Boolean): Pair<Int, Int> {
        val pw = 40
        for (cy in 6 until 90) for (cx in 8 until 390) {
            if (!(-1..1).all { solid20(w, cx + it, cy) }) continue
            val x = cx * 20 + 10; val y = cy * 20 - 1
            val e = w.npcs.first { it.ax == 11 }
            e.setPositionPx(x, y); e.setAnim(24); e.refreshBoxes()
            val pwReal = w.player.W[2] - w.player.W[0]
            val lx = (e.W[0] - maxOf(pw, pwReal)) / 20
            val rx = (e.W[2] + maxOf(pw, pwReal)) / 20
            val ey = e.W[1] / 20
            val l = w.collisionCell(lx, ey); val r = w.collisionCell(rx, ey)
            if ((l >= 12) == leftSolid && (r >= 12) == rightSolid) return x to y
        }
        error("no side spot left=$leftSolid right=$rightSolid")
    }

    private fun dropSide(leftSolid: Boolean, rightSolid: Boolean): Triple<Int, Boolean, Int> {
        val w = world()
        val (x, y) = sideSpot(w, leftSolid, rightSolid)
        val e = soldier(w, x, y, 24); e.aC = 0; e.j = 0
        val p = w.player
        place(w, x, y, 89)
        w.npcFsm.tick(e, p)
        return Triple(p.ak - e.ak, p.av, e.S)
    }

    @Test fun `S24 - the release lands the player on the open side`() {
        val (dxL, avL, sL) = dropSide(leftSolid = false, rightSolid = true)
        assertEquals(5, sL, "i(5) @6128-6135")
        assertTrue(dxL < 0 && !avL, "left open → left of the soldier, facing right: dx=$dxL av=$avL")
        val (dxR, avR, _) = dropSide(leftSolid = true, rightSolid = false)
        assertTrue(dxR > 0 && avR, "left solid, right open → right: dx=$dxR av=$avR")
        val (dxB, _, _) = dropSide(leftSolid = true, rightSolid = true)
        assertTrue(Math.abs(dxB) < 5, "both solid: no placement (stays on the pin) dx=$dxB")
    }

    // ------------------------------------------------------------ S25
    @Test fun `S25 - a free fall skips the tail, a crate under the soldier does not`() {
        val (ax, ay) = airSpot(world())
        val w = world()
        val e = soldier(w, ax, ay, 25)
        place(w, ax + 600, ay)
        w.sfxLog.clear()
        repeat(3) { w.npcFsm.tick(e, w.player) }
        assertEquals(25, e.S)
        assertEquals(0, w.sfxLog.count { it == 24 },
            "@5877 goto 7691: the open-cell gate (k.A(24)) is not replayed while falling")
    }

    // ------------------------------------------------------------ S27 / S180-183
    @Test fun `S27 - the anim end drops into the fall (the mid-anim tail exit is unreachable with clip 7)`() {
        // I() @6923-6946: `r() → i(25); k.A(24); goto 7691`, else `ifeq 7292` (the
        // tail). Clip 7's S27 has ONE frame, so `r()` is true from the first tick
        // and the shared-tail exit can never run with the shipped data (the fix is
        // a faithful no-op here; mutation: equivalent).
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 27); place(w, gx + 600, gy)
        assertEquals(1, e.clip!!.frameCount(27), "fixture: S27 is a single frame")
        w.sfxLog.clear()
        w.npcFsm.tick(e, w.player)
        assertEquals(25, e.S, "r() → i(25)")
        assertTrue(24 in w.sfxLog, "k.A(24)")
    }

    @Test fun `S180 to S183 - the mount throw arms end goto 7691`() {
        val (ax, ay) = airSpot(world())
        for (s in intArrayOf(180, 181, 182, 183)) {
            val w = world()
            val e = soldier(w, ax, ay, s)
            place(w, ax + 600, ay)
            w.npcFsm.tick(e, w.player)
            assertEquals(s, e.S, "S$s @7070/@7078: no L777 tail, so no fall gate")
        }
    }

    // ------------------------------------------------------------ S6 aF()
    @Test fun `S6 - the lunge stops at a ledge behind it, not over ground`() {
        // aF(): av → probe (W[2]/20+1, (W[3]+10)/20) — the cell on the side the soldier does NOT face
        fun lunge(openBehind: Boolean): Int {
            val w = world()
            val (gx, gy) = if (openBehind) {
                var r: Pair<Int, Int>? = null
                search@ for (cy in 4 until 90) for (cx in 5 until 400) {
                    if ((-3..0).all { solid20(w, cx + it, cy) && airy(w, cx + it, cy) } &&
                        w.collisionCell(cx + 1, cy) == 0 && w.collisionCell(cx + 2, cy) == 0) {
                        r = (cx * 20 + 10) to (cy * 20 - 1); break@search
                    }
                }
                r!!
            } else flatGround(w)
            val e = soldier(w, gx, gy, 6)
            e.av = true                                    // faces left → trailing side is the right
            e.ag = -1024
            place(w, gx - 200, gy)
            w.npcFsm.tick(e, w.player)
            return e.ag
        }
        assertNotEquals(0, lunge(openBehind = false), "ground behind: aF() false, the lunge keeps its speed")
        assertEquals(0, lunge(openBehind = true), "ledge behind: aF() true → ag = 0 @5468-5483")
    }

    // ------------------------------------------------------------ S18
    @Test fun `S18 - the finisher offer is taken once, on the anim's last tick too`() {
        // I() @5203-5227: `r() → g.E=0; i(23); G(); O(); goto 7292` — the arm ends
        // there. The port looped back into the offer, so a press landing on the
        // anim's last tick drew a second `nextInt()` for the 183/184 pick.
        fun draws(): Int {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 18); e.Z[0] = 2
            val p = w.player
            place(w, gx + 40, gy, 0); p.aZ = true
            w.lockTarget = e
            finish(e)
            w.pad.commit(65568)
            w.npcFsm.tick(e, p)
            assertTrue(p.S == 183 || p.S == 184, "the offer fired: S${p.S}")
            assertEquals(23, e.S, "r() → i(23)")
            // how many values of the (identically seeded) stream the tick consumed
            val next = w.rng.nextInt()
            val ref = world()
            var n = 0
            while (n < 8 && ref.rng.nextInt() != next) n++
            return n
        }
        assertEquals(1, draws(), "one nextInt() for the 183/184 pick")
    }

    // ------------------------------------------------------------ S20
    @Test fun `S20 - the anim end zeroes a positive aB`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 20); e.aB = 40
        place(w, gx + 400, gy)
        finish(e)
        w.npcFsm.tick(e, w.player)
        assertEquals(0, e.aB, "@6893 ifle: positive aB → 0")
        assertEquals(139, e.S)
    }

    // ------------------------------------------------------------ k()
    @Test fun `k - the ledge kill is offered at S38 whatever the facing, the plain stab is not`() {
        // aS.g(e) = (e.ak < p.ak) == p.av → true with p.av = false (it faces right);
        // e.g(aS) = (p.ak < e.ak) == e.av → true only when e.av = true
        fun stabbed(playerS: Int, soldierFacesPlayer: Boolean): Boolean {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 2)
            val p = w.player
            if (playerS == 38) place(w, gx - 5, gy + 10, 38)   // hanging just below the feet row
            else place(w, gx - 30, gy, playerS)
            p.av = false
            e.av = soldierFacesPlayer
            e.j = 0
            w.pad.commit(65568)
            w.npcFsm.tick(e, p)
            return e.S == 21
        }
        // the marker link: `aS.g(this) == 0 → G()`, else `this.g(aS) != 0 → G()` @260-284
        run {
            val w = world(); val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 2)
            val p = w.player
            place(w, gx - 30, gy, 0)
            p.av = false                                   // faces the soldier
            e.av = true                                    // …and the soldier faces the player
            e.spawnMarker(w, 8, e.ak, e.al - 85)
            assertNotNull(e.ae)
            w.npcFsm.tick(e, p)
            assertNull(e.ae, "the soldier faces the player → G() drops the marker")
        }
        assertTrue(stabbed(0, soldierFacesPlayer = false), "control: from behind, S0 → stabbed")
        assertFalse(stabbed(0, soldierFacesPlayer = true), "facing me: no window at S0")
        assertTrue(stabbed(38, soldierFacesPlayer = true),
            "@364 `aS.S != 38 → skip` only skips OTHER states: the hanging kill is offered facing or not")
    }
}
