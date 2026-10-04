package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 391 — which original sites run `E()` (the settle-sink) and which
 * only run `t()` (box refresh).
 *
 * `i.E()` = `loop { ah=1; b=1; a(1); ah=0; aR∈{≥12,5,3} → return; al+=10 }`.
 * The original calls it from 19 sites (`g.e()` ×6, `g.d(int)`, `i.s()`,
 * the ctor tail ×3, `aU()` ×2, `aV()` ×3, `bb()` ×2, `bC()`); every other
 * place that "settled" in the port ran the old `settleToGround` stub — a
 * `t()` followed by an `al += 10` sink with no `a(1)` — and so dropped
 * mid-air children, floaties, knives, the follower mirror, the boss at the
 * arena clamp and the director's flung player to the floor.
 *
 * Verified against `i.javap.txt`: `a(IIII)` (4799), `p(II)` (18031),
 * `g(I)` (18922), `b(Z)` (6635) and the `bG()` knives end `aK.t()`;
 * `bb()` S30/38, `aU()` ×2, `bC()` S244, `g.d(int)` and `i.s()`'s wrap tail
 * call `k.aS.E()`; `aP()`'s arena arm is `a(1); t()`; `bD()` case 7 ends
 * `aS.t(); … return` (+ a random-point floatie, never the centre).
 * Also pinned here: `bG()`'s S18 knife draws `j.a(0, (j.c(30·j.m/360)·r2)>>8)`
 * (`j.m = 256`, the port had `j.c(0)` = MAX) and the `j==3` block ends
 * `ah = k.Y + 128` on every path.
 */
class Slice391Test {
    /** A column of open air above level 0's ground at x=300 (the same spot
     *  Slice1Test's settle test uses). */
    private fun airAt(w: Level0World, ax: Int = 11, clip: Int = 7): Entity {
        val e = Entity(ax, w.clips[clip])
        e.aw = 900
        e.setPositionPx(300, 100); e.refreshBoxes()
        return e
    }

    /** Where `E()` stands a player-shaped entity dropped at (300,100): a
     *  scratch copy of the player body (own clip, own box) run through the
     *  real settle loop. */
    private fun floorY(w: Level0World): Int {
        val probe = Entity(0, w.player.clip)
        probe.setAnim(0)
        probe.setPositionPx(300, 100); probe.refreshBoxes(); probe.eSettle(w)
        return probe.al
    }

    private fun lastQueued(w: Level0World): Entity = w.pendingInsert.last()

    // ------------------------------------------------------------ a(IIII)
    @Test fun `spawnChildFx leaves the child on the parent pixel - a(IIII) ends t() not E()`() {
        val w = world(); w.npcs.clear()
        assertTrue(floorY(w) > 160, "fixture: there is floor well below y=100")
        val e = airAt(w)
        e.av = true
        val c = e.spawnChildFx(w, 24, 40, 5, 77)
        assertEquals(300, c.ak); assertEquals(100, c.al, "no sink")
        assertEquals(77, c.az); assertEquals(-1, c.aw); assertEquals(0, c.au)
        assertTrue(c.av, "facing copied")
        assertEquals(5, c.S)
    }

    @Test fun `spawnChildFx refreshes the child box at the parent position`() {
        val w = world(); w.npcs.clear()
        val e = airAt(w)
        val c = e.spawnChildFx(w, 24, 40, 5, 77)
        val ref = Entity(24, w.clips[40]); ref.setPositionPx(300, 100); ref.setAnim(5)
        ref.refreshBoxes()
        assertTrue(ref.W.contentEquals(c.W), "t() ran: W=${c.W.toList()} vs ${ref.W.toList()}")
    }

    // ------------------------------------------------------------ p(II)
    @Test fun `popupDmg floatie stays at the offset above the enemy`() {
        val w = world(); w.npcs.clear()
        val e = airAt(w)
        e.popupDmg(w, 5, -30)
        val f = lastQueued(w)
        assertEquals(305, f.ak); assertEquals(70, f.al, "ak+r8, al+r9 — not dropped to the floor")
        assertEquals(5, f.ao); assertEquals(-30, f.ap)
        assertEquals(16, f.P and 16); assertSame(e, f.af)
        assertEquals(31, f.S)
    }

    // ------------------------------------------------------------ g(I)
    @Test fun `spawnBarrage knife spawns at the parent feet and stays there`() {
        val w = world(); w.npcs.clear()
        val e = airAt(w)
        e.spawnBarrage(w, 1)
        val k = lastQueued(w)
        assertEquals(e.ak, k.ak); assertEquals(e.W[3], k.al, "al = W[3], no E()")
        assertEquals(256, k.ah); assertEquals(0, k.ag)
        assertFalse(k.bR); assertSame(e, k.af)
    }

    // ------------------------------------------------------------ b(Z)
    @Test fun `syncAd mirrors the parent without sinking the follower`() {
        val w = world(); w.npcs.clear()
        val e = airAt(w)
        val ad = Entity(11, w.clips[7]); ad.setPositionPx(0, 0)
        e.ad = ad
        e.ag = 3; e.ah = -4
        ad.av = true
        e.syncAd(w)
        assertEquals(300, ad.ak); assertEquals(100, ad.al, "ad.al = al, then t() only")
        assertEquals(3, ad.ag); assertEquals(-4, ad.ah)
        assertEquals(1, ad.P and 1, "ad.av → P |= 1")
        ad.av = false
        e.syncAd(w)
        assertEquals(0, ad.P and 1, "!ad.av → P &= -2")
    }

    // ------------------------------------------------------------ bG knives
    @Test fun `bG pv4 knife and its fuse sibling spawn at W3 and keep it`() {
        val w = world(); w.npcs.clear()
        val e = airAt(w); e.pv = 4
        e.setAnim(30); e.T = 0
        e.respawnAttack(w)
        val n = w.pendingInsert.size
        assertTrue(n >= 2, "knife + a(24,40,44) sibling, got $n")
        val knife = w.pendingInsert[n - 2]; val sib = w.pendingInsert[n - 1]
        assertEquals(e.W[3], knife.al, "knife al = W[3]")
        assertEquals(e.W[3], sib.al, "sibling al = W[3]")
        assertEquals(256, knife.ah)
    }

    @Test fun `bG S18 knife launch speed comes from the j_c(21) range, not MAX`() {
        val w = world(); w.npcs.clear()
        var maxAbs = 0; var seen = 0
        for (round in 0 until 40) {
            val e = airAt(w); e.pv = 2; e.aG = 0
            e.setAnim(18); e.j = 0
            // r(): clip-less entities read as finished
            e.clip = null
            e.respawnAttack(w)
            val k = lastQueued(w)
            seen++
            val r9 = maxOf(60, 240 - k.bZ)
            val rng = (Trig.tan(30 * Trig.M / 360) * r9) shr 8
            val bound = ((rng - 1) * (512 + w.kY)) / r9
            assertTrue(kotlin.math.abs(k.ag) <= kotlin.math.abs(bound) + 1,
                "round $round: |ag|=${kotlin.math.abs(k.ag)} must stay within $bound (r9=$r9 rng=$rng)")
            maxAbs = maxOf(maxAbs, kotlin.math.abs(k.ag))
        }
        assertEquals(40, seen)
        assertTrue(maxAbs > 0, "the draw is not constant zero")
        assertEquals(145, Trig.tan(21), "j.c(21) = tan(29.5deg) in 8.8")
    }

    @Test fun `bG S18 j3 block ends ah = k_Y + 128 on every path and mirrors ag for aG1 and aG2`() {
        val w = world(); w.npcs.clear()
        for (aG in 0..2) for (round in 0 until 30) {
            val e = airAt(w); e.pv = 2; e.aG = aG
            e.setAnim(18); e.j = 3; e.clip = null
            e.respawnAttack(w)
            val k = lastQueued(w)
            assertEquals(w.kY + 128, k.ah, "aG$aG round $round: @520 closes the block")
            assertTrue(k.k, "aK.k = true")
            if (aG == 1) assertTrue(k.ag <= 0, "aG1: r5>0 is mirrored (ag=${k.ag})")
            if (aG == 2) assertTrue(k.ag >= 0, "aG2: r5<0 is mirrored (ag=${k.ag})")
        }
    }

    // ------------------------------------------------------------ E() sites
    @Test fun `g_d death release runs the real E() - b set and the player stands snapped`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        p.b = false; p.aZ = false; p.standingOn = null
        p.x1 = 5
        p.gDrain(10, w)
        assertEquals(0, p.x1)
        assertTrue(p.b, "E() pins b=1 each pass")
        assertEquals(floorY(w), p.al, "E() ends on the same line the settle probe finds")
        assertEquals(0, p.ah, "E() unpins ah")
    }

    @Test fun `g_d death release skips E() while ridden or grounded`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        p.b = false; p.aZ = true; p.standingOn = null
        p.x1 = 5
        p.gDrain(10, w)
        assertEquals(100, p.al, "aZ → no E()")
        assertFalse(p.b)
    }

    @Test fun `bb S30 pickup runs the real E() on the player`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes(); p.b = false
        val m = Entity(16, w.clips[10]).apply {
            S = 30; aB = 10; setPositionPx(300, 100)
            W[0] = 290; W[1] = 90; W[2] = 310; W[3] = 110
        }
        w.npcs.add(0, m)
        w.npcFsm.tickRequestMarker(m, p, Pad())
        assertEquals(91, p.S, "i(91)")
        assertTrue(p.b, "k.aS.E() pins b")
        assertEquals(floorY(w), p.al)
    }

    @Test fun `bb S38 pickup runs the real E() too`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes(); p.b = false
        val m = Entity(16, w.clips[10]).apply {
            S = 38; aB = 10; setPositionPx(300, 100)
            W[0] = 290; W[1] = 90; W[2] = 310; W[3] = 110
        }
        w.npcs.add(0, m)
        w.npcFsm.tickRequestMarker(m, p, Pad())
        assertTrue(p.b); assertEquals(floorY(w), p.al)
    }

    @Test fun `i_s jc21 wrap tail runs E() for an airborne g_b state`() {
        val w = world(); settleIntro(w)
        w.kC = Entity(5, null).apply { aw = 9_301 }
        w.autoDismissDialog = false; w.dlgU = 1; w.stateL(21)
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        p.aZ = false; p.b = false
        p.setAnim(18)                         // g.b(18) — hang/ledge family
        assertTrue(p.gB(), "fixture: S18 is a g.b state")
        val e = Entity(30, w.clips[7]).apply { aw = 9_300; setAnim(0) }
        e.T = e.clip!!.frameCount(0) - 1
        e.U = maxOf(1, e.clip!!.frameDuration(0, e.T)) - 1
        e.advanceAnim()                       // the cycle wraps → i.s() tail
        assertEquals(0, p.S, "i(0)")
        assertTrue(p.b, "E()")
        assertEquals(floorY(w), p.al)
    }

    /** `aU()` @500-556 / @656-704: the exits probe `x()` 40 px lower; only
     *  `aR > 20` (a slope / special cell, not plain ground) reaches the
     *  `E()` leg — a world whose every cell reads 24 puts the probe there. */
    private fun railWorld(held: Int) = object : Slice128Test.MarkerWorld(cellFn = { _, _ -> 24 }) {
        override fun padHeld(mask: Int): Boolean = mask == held
    }

    private fun boundRail(w: Slice128Test.MarkerWorld): Pair<Entity, Entity> {
        val rail = Entity(10, null).apply {
            S = 34; Z[0] = 0; Z[1] = 0
            W[0] = 0; W[1] = 100; W[2] = 400; W[3] = 140
        }
        val p = w.player
        p.ak = 200; p.al = 150
        p.W[0] = 190; p.W[1] = 110; p.W[2] = 210; p.W[3] = 170
        return rail to p
    }

    @Test fun `aU rail jump-off over a slope cell (aR gt 20) runs E()`() {
        val w = railWorld(33024)
        val (rail, p) = boundRail(w)
        p.b = false
        aUDraw(rail, w, p)
        assertEquals(null, p.af, "jump-off drops the rail link")
        assertTrue(p.b, "al-=40; E() — the settle leg pins b")
        assertEquals(24, p.aR)
    }

    @Test fun `aU rail attack-off over a slope cell runs E() with the fling cleared`() {
        val w = railWorld(16388)
        val (rail, p) = boundRail(w)
        p.b = false
        aUDraw(rail, w, p)
        assertEquals(null, p.af)
        assertTrue(p.b, "ah=0; ag=0; E()")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
    }

    // ------------------------------------------------------------ bC S244
    @Test fun `bC S4 kill landing runs the real E() on the player`() {
        val w = world(); w.npcs.clear()
        val e = Entity(69, w.clips[38])
        val rec = mutableListOf(69, 152, 100, 200, 0, 0, 0, -1)
        while (rec.size < 22) rec += 0
        e.setPositionPx(100, 200)
        w.npcFsm.initAx69(e, rec.toList(), w)
        w.npcs.add(e)
        e.setAnim(4); e.aA = 0
        e.T = e.clip!!.frameCount(e.S) - 1
        e.U = e.clip!!.frameDuration(e.S, e.T) - 1
        val p = w.player
        p.setAnim(244); p.P = p.P or 64; p.af = e
        p.setPositionPx(100, 163); p.refreshBoxes(); p.b = false
        w.npcFsm.tickAx69(e, w, p)
        assertEquals(12, e.S)
        assertTrue(p.b, "aS.E()")
    }

    // ------------------------------------------------------------ aP arena arm
    @Test fun `boss arena clamp runs a(1) then t() - it no longer drops an airborne boss`() {
        val w = world(); w.npcs.clear()
        w.boundMaxX = 100000
        val b = Entity(29, w.clips[7])
        b.aw = 60; b.aB = 500
        b.setPositionPx(300, 100); b.refreshBoxes()
        w.npcs.add(b)
        b.setAnim(39)                         // arena-arm member (S8/39/20/21&r/5/40/4)
        w.iBy = 1
        w.npcFsm.tickBoss(b, w.player, Pad())
        assertTrue(b.al < floorY(w) - 40, "boss al=${b.al} must stay near y=100 (floor ${floorY(w)})")
    }

    // ------------------------------------------------------------ bD finale
    private fun director(w: Level0World): Entity {
        val e = Entity(21, w.clips[7])
        e.aA = 0; e.aB = 100; e.aw = 50
        e.Z.fill(-1)
        e.setPositionPx(500, 200); e.refreshBoxes()
        w.npcs.add(0, e)
        return e
    }

    private fun cameraOnPlayer(w: Level0World) {
        w.tick(emptyList())                   // puts the tracker on the player
    }

    @Test fun `bD finale spawns the S9 floatie at a random point of W - x then y draw`() {
        val w = world(); w.npcs.clear(); cameraOnPlayer(w)
        val d = director(w)
        d.aA = 7; d.S = 2                     // v() true
        d.W[0] = 400; d.W[1] = 150; d.W[2] = 440; d.W[3] = 260
        val seen = HashSet<Int>()
        for (round in 0 until 12) {
            w.pendingInsert.clear()
            d.aA = 7
            w.npcFsm.tickDirector(d, w.player, Pad())
            val f = w.pendingInsert.lastOrNull() ?: error("round $round: no floatie")
            assertTrue(f.ak in 400..440, "x=${f.ak} inside [W0,W2]")
            assertTrue(f.al in 150..260, "y=${f.al} inside [W1,W3]")
            assertSame(w.player, f.af)
            assertEquals(0, f.ag); assertEquals(0, f.ah); assertFalse(f.av)
            assertEquals(9, f.S)
            seen.add(f.ak * 1000 + f.al)
        }
        assertTrue(seen.size > 3, "j.a(W0,W2)/j.a(W1,W3) draw — not the centre (${seen.size} distinct)")
    }

    @Test fun `bD finale with the director off play still refreshes aS and finishes - no L237 tail`() {
        val w = world(); w.npcs.clear(); cameraOnPlayer(w)
        val d = director(w)
        d.aA = 7; d.S = 0                     // ax21 v(): S<2 → u()/au test → off camera
        d.setPositionPx(9000, 200); d.refreshBoxes()
        d.W[0] = 8990; d.W[1] = 190; d.W[2] = 9010; d.W[3] = 210
        // a linked pursuer whose finished anim the L237 tail would re-pose
        val l0 = Entity(11, null).apply { aw = 60; aB = 10; S = 1; setPositionPx(600, 200) }
        w.npcs.add(l0); d.Z[0] = 60
        val before = l0.S
        w.player.ag = 77
        w.npcFsm.tickDirector(d, w.player, Pad())
        assertFalse(d.inPlayV(w), "fixture: director is off play")
        assertEquals(w.kY - 2560, w.player.ah, "v() false → aS.ah = k.Y - 2560")
        assertEquals(0, w.player.ag)
        assertEquals(before, l0.S, "case 7 returns: the L237 watcher never ran")
        // the player is still in play → the floatie, not k.l(15)
        assertTrue(w.player.inPlayV(w))
        assertFalse(w.missionWon, "aS.v() still true")
        assertTrue(w.pendingInsert.isNotEmpty(), "floatie queued")
    }

    @Test fun `bD finale completes the mission once aS leaves the play area`() {
        val w = world(); w.npcs.clear(); cameraOnPlayer(w)
        val d = director(w)
        d.aA = 7; d.S = 2
        // aS.v() false → `k.l(15)` and `return` — in both director branches
        w.player.setPositionPx(6000, 200); w.player.refreshBoxes()
        w.npcFsm.tickDirector(d, w.player, Pad())
        assertTrue(w.missionWon, "k.l(15)")
        assertTrue(w.pendingInsert.isEmpty(), "no floatie")
        val w2 = world(); w2.npcs.clear(); cameraOnPlayer(w2)
        val d2 = director(w2)
        d2.aA = 7; d2.S = 0
        d2.setPositionPx(9000, 200); d2.refreshBoxes()           // v() false too
        w2.player.setPositionPx(6000, 200); w2.player.refreshBoxes()
        w2.npcFsm.tickDirector(d2, w2.player, Pad())
        assertTrue(w2.missionWon, "the off-play director branch reaches the same test")
    }

    @Test fun `bD finale does not sink the flung player - aS t() only`() {
        val w = world(); w.npcs.clear(); cameraOnPlayer(w)
        val d = director(w)
        d.aA = 7; d.S = 2
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes(); p.b = false
        w.kM(2)
        w.npcFsm.tickDirector(d, p, Pad())
        assertEquals(100, p.al, "no E()")
        assertFalse(p.b)
    }
}
