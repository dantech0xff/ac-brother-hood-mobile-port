package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Slice 396 — the mission director `bD()` and the pursuer script `bG()`
 * re-read from the raw bytecode (i.javap `bD()` @0-3115, `bG()` @0-2210; the
 * chase `d(Z)Z`, `f(I)Z`, `p(II)V`, `g(I)V`, `bH()`, `aw()`, `bE/bF` matched).
 *
 * `bD()`:
 * - arm 0's arming tail does NOT clear the first node's consumed bit
 *   (`f &= 127` is only on the node-advance sites);
 * - arms 1/4/5 run `bG()` on every pursuer except the exact "gone" state
 *   (`aB <= 0 && S == 16/20/26`): a DYING pursuer keeps scripting; arm 4's
 *   `r2` is set by a gone FIRST pursuer only and cleared by a live-or-dying
 *   second one — a gone second one alone no longer ends the phase;
 * - arm 3 after a node advance: an S10 or off-screen pursuer skips the `bs`
 *   walk, a visible one walks it in a LOOP, and EVERY pursuer then has its
 *   flags cleared (`P &= -17 & -33; E = 0; aC = 100`);
 * - tail case 4: `S == 37 → i(33)` is the ELSE of the `k && P&128 → i(37)` arm.
 *
 * `bG()`:
 * - pv0: `(S == 28 || S == 14) && r()` fires, otherwise `S != 28 → i(28)` — the
 *   S14 fire frame lasts one call, the windup restarts, six volleys;
 * - pv3 j==2: a finished S27 starts S24 and FALLS THROUGH into the seven
 *   homing knives (they were unreachable);
 * - pv3 j==0: the in-box point's `y` is `W[3]` on `(nextInt & 1) == 0`, else
 *   `W[1]` (inverted), and the pool cell is never marked used;
 * - pv4: `l &= -2` only when neither `kB.l & 2` nor `kB.l & 12` is set.
 */
class Slice396Test {
    private fun dirWorld(): Level0World {
        val w = world(); w.npcs.clear()
        w.player.setPositionPx(300, 200); w.player.refreshBoxes()
        w.kM(2)
        return w
    }

    private fun director(w: Level0World, aA: Int): Entity {
        val d = Entity(21, null)
        d.aw = 900; d.aA = aA
        java.util.Arrays.fill(d.Z, -1)
        d.setPositionPx(300, 200)
        w.npcs.add(d)
        return d
    }

    private fun pursuer(w: Level0World, aw: Int, pv: Int = 1, clip: Int = -1): Entity {
        val e = Entity(32, if (clip >= 0) w.clips[clip] else null)
        e.aw = aw; e.pv = pv; e.aB = 50
        e.setPositionPx(300, 200); e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    private fun tickDir(w: Level0World, d: Entity) = w.npcFsm.tickDirector(d, w.player, Pad())

    /** Put a clip-backed entity on the last frame of its current anim. */
    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        val dur = c.frameDuration(e.S, e.T)
        e.U = if (dur > 0) dur - 1 else 0
        assertTrue(e.animFinished(), "fixture: S${e.S} finished")
    }

    private fun unfinish(e: Entity) {
        e.T = 0; e.U = 0
        if (e.animFinished()) e.T = 5                  // a 1-frame anim: park past its last frame
        assertFalse(e.animFinished(), "fixture: S${e.S} still playing")
    }

    // ------------------------------------------------------------ bD arm 0
    @Test fun `arm 0 does not clear the consumed bit of the first node`() {
        val w = dirWorld()
        w.waypoints.reset()
        w.waypoints.add(intArrayOf(0, 77, 120, 80, 0, 0, 0, -3, -1))     // f = -3: bit 7 set
        val d = director(w, 0); d.Z[12] = 77
        tickDir(w, d)
        assertEquals(8, d.aA)
        assertNotNull(w.dirWp)
        assertEquals(-3, w.dirWp!!.f, "@496-534: no `f &= 127` in the arming tail")
        assertEquals(120, d.aq); assertEquals(80, d.ar)
    }

    // ------------------------------------------------------------ bD arm 1
    @Test fun `arm 1 keeps running the script of a dying pursuer - only the S16 corpse ends it`() {
        val w = dirWorld(); val d = director(w, 1); d.Z[0] = 501
        val p = pursuer(w, 501)
        p.aB = 0; p.setAnim(13)
        tickDir(w, d)
        assertTrue(p.l and 1 != 0, "bG ran on the dying pursuer (@807: aB>0 || S != 16)")
        assertEquals(1, d.aA, "still monitoring")

        val w2 = dirWorld(); val d2 = director(w2, 1); d2.Z[0] = 501
        val p2 = pursuer(w2, 501)
        p2.aB = 0; p2.setAnim(16); p2.l = 1
        tickDir(w2, d2)
        assertEquals(6, d2.aA, "corpse → router")
        assertEquals(2, d2.l and 2, "l |= 2")
        assertEquals(0, p2.l and 1, "@814-826: pursuer l &= -2, no bG")
    }

    // ------------------------------------------------------------ bD arm 4
    @Test fun `arm 4 stays while the first pursuer lives even if the second is gone`() {
        val w = dirWorld(); val d = director(w, 4); d.Z[1] = 501; d.Z[2] = 502
        val a = pursuer(w, 501); val b = pursuer(w, 502)
        b.aB = 0; b.setAnim(20); b.l = 1
        tickDir(w, d)
        assertEquals(4, d.aA, "r2 stays 0: a gone second pursuer alone does not end the phase")
        assertTrue(d.l and 8 != 0, "…but it is marked gone")
        assertEquals(0, b.l and 1, "gone: l &= -2")
        assertEquals(1, a.aG, "first pursuer: aG = 1 + bG()")
        assertTrue(a.l and 1 != 0)
    }

    @Test fun `arm 4 leaves when both pursuers are gone`() {
        val w = dirWorld(); val d = director(w, 4); d.Z[1] = 501; d.Z[2] = 502
        val a = pursuer(w, 501); val b = pursuer(w, 502)
        a.aB = 0; a.setAnim(20); b.aB = 0; b.setAnim(20)
        tickDir(w, d)
        assertEquals(6, d.aA)
        assertEquals(12, d.l and 12)
    }

    @Test fun `arm 4 keeps scripting dying pursuers with their aG`() {
        val w = dirWorld(); val d = director(w, 4); d.Z[1] = 501; d.Z[2] = 502
        val a = pursuer(w, 501); val b = pursuer(w, 502)
        a.aB = 0; a.setAnim(13); b.aB = 0; b.setAnim(13)
        tickDir(w, d)
        assertEquals(1, a.aG); assertEquals(2, b.aG)
        assertTrue(a.l and 1 != 0 && b.l and 1 != 0, "bG ran on both")
        assertEquals(4, d.aA, "the second is dying, not gone: r2 = 0")
    }

    // ------------------------------------------------------------ bD arm 5
    @Test fun `arm 5 keeps scripting a dying Z3 pursuer until it reaches S26`() {
        val w = dirWorld(); val d = director(w, 5); d.Z[3] = 503
        val p = pursuer(w, 503)
        p.aB = 0; p.setAnim(13)
        tickDir(w, d)
        assertTrue(p.l and 1 != 0, "@1636-1644: aB > 0 || S != 26 → bG()")
        assertEquals(5, d.aA)

        val w2 = dirWorld(); val d2 = director(w2, 5); d2.Z[3] = 503
        val p2 = pursuer(w2, 503)
        p2.aB = 0; p2.setAnim(26)
        tickDir(w2, d2)
        assertEquals(6, d2.aA)
        assertTrue(d2.l and 16 != 0)
    }

    // ------------------------------------------------------------ bD arm 3
    private fun arm3(configure: (Level0World, Entity) -> Unit): Entity {
        val w = dirWorld()
        w.waypoints.reset()
        w.waypoints.add(intArrayOf(0, 10, 100, 100, 0, 0, 0, 3, 11))
        w.waypoints.add(intArrayOf(0, 11, 200, 100, 0, 0, 0, 3, -1))
        val d = director(w, 3); d.Z[13] = 601
        d.aq = 100; d.ar = 100; d.bY = 100; d.bZ = 100
        w.dirWp = w.waypoints.find(10)
        val p = pursuer(w, 601, clip = 7)
        p.Z[1] = 5; p.Z[2] = 6; p.Z[3] = -1; p.bs = 0
        p.P = p.P or 16 or 32; p.aC = 7; p.iE = true
        configure(w, p)
        tickDir(w, d)
        assertEquals(6, d.aA, "fixture: the arrival ran the pursuer loop")
        return p
    }

    private fun assertFlagsCleared(p: Entity) {
        assertEquals(0, p.P and 16, "P &= -17"); assertEquals(0, p.P and 32, "P &= -33")
        assertFalse(p.iE, "E = 0"); assertEquals(100, p.aC, "aC = 100")
    }

    @Test fun `arm 3 node advance - a visible pursuer walks bs in a loop and then has its flags cleared`() {
        val p = arm3 { w, e ->
            e.setAnim(12); e.refreshBoxes()
            assertTrue(e.inPlayV(w), "fixture: visible")
        }
        assertEquals(1, p.bs, "while (bs < 4 && Z[bs+1] != -1) bs++ → 2, then bs-- (a single step ends at 0)")
        assertFlagsCleared(p)
    }

    @Test fun `arm 3 node advance - an S10 pursuer skips the bs walk but its flags are cleared`() {
        val p = arm3 { w, e ->
            e.setAnim(10); e.refreshBoxes()
            assertTrue(e.inPlayV(w), "fixture: visible (so the respawn gate stays shut)")
        }
        assertEquals(0, p.bs)
        assertFlagsCleared(p)
    }

    @Test fun `arm 3 node advance - an off-screen pursuer skips the bs walk but its flags are cleared`() {
        val p = arm3 { w, e ->
            e.setAnim(12); e.setPositionPx(3000, 200); e.refreshBoxes()
            assertFalse(e.inPlayV(w), "fixture: off screen")
        }
        assertEquals(0, p.bs)
        assertFlagsCleared(p)
    }

    // ------------------------------------------------------------ bD tail, case 4
    @Test fun `tail case 4 - the k and P128 arm starts S37, the S37 test is its else`() {
        val w = dirWorld(); val d = director(w, 2); d.Z[4] = 504
        val p = pursuer(w, 504)
        p.setAnim(36); d.k = true; p.P = p.P or 128
        tickDir(w, d)
        assertEquals(37, p.S, "i(37) — and NOT straight on to S33")
        assertFalse(d.k, "k = 0")
        assertEquals(0, p.P and 128, "P &= -129")
        d.aA = 2
        tickDir(w, d)
        assertEquals(33, p.S, "the NEXT finished S37 moves on")
    }

    // ------------------------------------------------------------ bG pv0
    @Test fun `pv0 - S28 or S14 finished fires the volley, an unfinished S14 restarts the windup`() {
        // S28 finished
        val w = dirWorld(); val e = pursuer(w, 501, pv = 0, clip = 16)
        e.setAnim(28); finish(e)
        e.respawnAttack(w)
        assertEquals(14, e.S)
        assertEquals(listOf(16, 17, 18), w.pendingInsert.takeLast(3).map { it.S }, "g(0) g(1) g(2)")
        assertEquals(1, e.j, "g(2) counts j")
        // S14 finished → fires AGAIN (`i(14)` is a no-op on S14: the tail then
        // moves the finished anim on to S13)
        w.pendingInsert.clear()
        finish(e)
        e.respawnAttack(w)
        assertEquals(3, w.pendingInsert.size, "second volley")
        assertEquals(2, e.j)
        // S14 still playing → the windup restarts
        e.setAnim(14); unfinish(e)
        w.pendingInsert.clear()
        e.respawnAttack(w)
        assertEquals(28, e.S, "@109-133: S != 28 → i(28)")
        assertTrue(w.pendingInsert.isEmpty())
        // S28 still playing → nothing
        unfinish(e)
        e.respawnAttack(w)
        assertEquals(28, e.S); assertTrue(w.pendingInsert.isEmpty())
        // any other anim → windup
        e.setAnim(13); unfinish(e)
        e.respawnAttack(w)
        assertEquals(28, e.S)
    }

    // ------------------------------------------------------------ bG pv3 j==2
    private fun pv3(w: Level0World): Entity {
        val e = pursuer(w, 701, pv = 3, clip = 16)
        e.k = true; e.j = 2
        e.cHGrid = Array(7) { intArrayOf(100 + it * 20, 50 + it) }
        w.pendingInsert.clear()
        return e
    }

    @Test fun `pv3 j2 - a finished S27 starts S24 and falls through into the seven homing knives`() {
        val w = dirWorld(); val e = pv3(w)
        e.setAnim(27); finish(e)
        e.respawnAttack(w)
        assertEquals(24, e.S)
        val knives = w.pendingInsert.filter { it.ax == 24 && it.S == 11 }
        assertEquals(7, knives.size, "@1269-1470: seven a(24,40,11) homing knives")
        assertEquals((0..6).toList(), knives.map { it.ap }, "ap = r2")
        assertEquals(0, e.j, "j = 0")
        assertTrue(e.cIDone, "cI = 1")
        val k0 = knives[0]
        assertEquals(((e.cHGrid!![0][0] - k0.bY) shl 8) / 20, k0.ag, "ag = (cH[r][0] - bY) << 8 / 20")
    }

    @Test fun `pv3 j2 - S27 still playing, S24 and every other anim return without knives`() {
        val w = dirWorld(); val e = pv3(w)
        e.setAnim(27); unfinish(e)
        e.respawnAttack(w)
        assertEquals(27, e.S); assertTrue(w.pendingInsert.isEmpty())
        e.setAnim(24)
        e.respawnAttack(w)
        assertEquals(24, e.S); assertTrue(w.pendingInsert.isEmpty())
        e.setAnim(13)
        e.respawnAttack(w)
        assertEquals(27, e.S, "@688-712: anything else starts S27"); assertTrue(w.pendingInsert.isEmpty())
        assertEquals(2, e.j, "no state change")
    }

    // ------------------------------------------------------------ bG pv3 j==0
    private class Scatter(val picks: List<IntArray>, val repeats: Boolean, val end: Long)

    /** Replica of the original's draw order: 60 pool points (x then y), then per
     *  pick `abs(nextInt() % 60)` and, for a point inside the box (the aZ arm),
     *  one parity draw choosing `W[3]` (even) or `W[1]`. The pool cell is never
     *  marked used, so a cell can come up twice. */
    private fun simulateScatter(state: Long, box: IntArray): Scatter {
        val rng = DeterministicRandom(0L); rng.restore(state)
        val pool = Array(60) { IntArray(2) }
        for (r in 0 until 6) for (c in 0 until 10) {
            pool[r * 10 + c][0] = c * 40 + 20 + rng.nextInt() % 20
            pool[r * 10 + c][1] = r * 40 + 20 + rng.nextInt() % 20
        }
        val picks = ArrayList<IntArray>(); val seen = HashSet<Int>(); var rep = false
        for (k in 0 until 7) {
            val idx = kotlin.math.abs(rng.nextInt() % 60)
            if (!seen.add(idx)) rep = true
            val x = pool[idx][0]; val y = pool[idx][1]
            if (x >= box[0] && x <= box[2] && y >= box[1] && y <= box[3]) {
                pool[idx][1] = if ((rng.nextInt() and 1) == 0) box[3] else box[1]
            }
            picks.add(intArrayOf(pool[idx][0], pool[idx][1]))
        }
        return Scatter(picks, rep, rng.state())
    }

    @Test fun `pv3 scatter - an in-box point takes W3 on an even draw and W1 on an odd one, cells are never marked used`() {
        val box = intArrayOf(0, 100, 2000, 300)
        // a start state whose seven picks hit one pool cell twice: the original
        // takes the cell again, a "used" mark would re-roll it (one more draw)
        val seed = (1L..2000L).first { simulateScatter(DeterministicRandom(it).state(), box).repeats }
        val w = dirWorld()
        val kb = Entity(0, null)
        kb.W[0] = box[0]; kb.W[1] = box[1]; kb.W[2] = box[2]; kb.W[3] = box[3]
        w.kB = kb
        w.rng.restore(DeterministicRandom(seed).state())
        val e = pursuer(w, 702, pv = 3, clip = 16)
        e.k = true; e.j = 0; e.aZ = false                 // toggled to true: the in-box arm
        e.setAnim(24); e.T = 1
        w.pendingInsert.clear()
        val sim = simulateScatter(w.rng.state(), box)
        e.respawnAttack(w)
        val knives = w.pendingInsert.filter { it.ax == 24 && it.S == 12 }
        assertEquals(7, knives.size)
        for (k in 0 until 7) {
            assertEquals(sim.picks[k][0] + w.kO, knives[k].bY, "knife $k x")
            assertEquals(sim.picks[k][1], knives[k].bZ, "knife $k y (W3 on an even draw, W1 on an odd one)")
        }
        assertEquals(1, e.j); assertEquals(30, e.nl)
        assertEquals(sim.end, w.rng.state(), "same number of draws: a repeated cell is taken again, not re-rolled")
    }

    // ------------------------------------------------------------ bG pv4
    @Test fun `pv4 clears bit 0 only when the director has no gone-bits set`() {
        for ((kbl, cleared) in listOf(0 to true, 2 to false, 4 to false, 8 to false, 12 to false, 16 to true, 32 to true)) {
            val w = dirWorld()
            val box = Entity(21, null); box.l = kbl
            w.kB = box
            val e = pursuer(w, 801, pv = 4, clip = 16)
            e.setAnim(33)
            e.respawnAttack(w)
            assertEquals(!cleared, (e.l and 1) != 0, "kB.l = $kbl → l&1 ${if (cleared) "cleared" else "kept"}")
        }
    }
}
