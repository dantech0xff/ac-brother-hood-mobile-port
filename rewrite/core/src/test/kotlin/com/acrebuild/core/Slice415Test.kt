package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 415 — the ax5 mission-logic entity's `aq()` re-read from the raw bytes
 * (`i.javap.txt`: `aq()V` @0-902, `static j(i)Z` @0-71).
 *
 * The S8 watcher tests its linked entity (`k.q(Z[2])`) with the STATIC `i.j(i)Z`
 * (@0-71: `r == null` → true; `ax ∈ {11, 17, 29, 27} && r.P()` → true; else false) where
 * `P()` is the DEAD check (`aB <= 0` → `G()` release, true; alive → false).  So `j(r)` =
 * "gone or dead".  The port's private `iJ` answered "gone or ALIVE" (`!deadRelease()`), the
 * inverse for every live or dead linked actor — the Z[1] = 0 / 3 / 10 / 13 / 17 arms
 * (@260-278, @407-433, @486-508, @539-572, @631-675) all fired on the wrong side.
 */
class Slice415Test {
    private fun ax5At(w: Level0World, x: Int, y: Int, z1: Int, z2: Int, aG: Int, z3: Int = -1): Entity {
        val e = Entity(5, w.clips[1])
        e.setPositionPx(x, y)
        e.W[0] = x; e.W[1] = y; e.W[2] = x + 20; e.W[3] = y + 20
        e.S = 8; e.Z[0] = 0; e.Z[1] = z1; e.Z[2] = z2; e.Z[3] = z3; e.aG = aG
        w.npcs.add(e)
        return e
    }

    /** An ax11 soldier registered under `aw`: `alive` = `aB > 0`, `finished` = `r()` (anim-end). */
    private fun linked(w: Level0World, aw: Int, alive: Boolean, finished: Boolean): Entity {
        val r = Entity(11, w.clips[7])
        r.aw = aw; r.aB = if (alive) 10 else 0
        r.setPositionPx(40, 40); r.refreshBoxes()
        if (finished) repeat(400) { if (!r.animFinished()) r.advanceAnim() }
        assertEquals(finished, r.animFinished(), "fixture: r() of the linked soldier")
        w.npcs.add(r)
        return r
    }

    /** `true` when the watcher resolved into the context bind (`k.C == e`). */
    private fun resolves(z1: Int, alive: Boolean, finished: Boolean): Boolean {
        val w = world(); w.npcs.clear()
        val p = w.player; p.refreshBoxes()
        linked(w, 55, alive, finished)
        val e = ax5At(w, p.ak, p.al, z1, z2 = 55, aG = 7)
        w.npcFsm.tickMissionLogic(e, w, p)
        return w.kC === e
    }

    @Test fun `S8 Z1=0 - j(r0) is dead-or-gone so an alive finished target holds the watcher`() {
        assertFalse(resolves(0, alive = true, finished = true), "@260-278: !j(r0) && r() → return")
        assertTrue(resolves(0, alive = false, finished = true), "@265 j(r0) true → @279 ax11 → resolve")
        assertTrue(resolves(0, alive = true, finished = false), "@272 !r() → @279 ax11 → resolve")
        assertTrue(resolves(0, alive = false, finished = false))
    }

    @Test fun `S8 Z1=3 - the bind needs j(r0) and r()`() {
        assertTrue(resolves(3, alive = false, finished = true), "@415-429: j(r0) && r() → ao()")
        assertFalse(resolves(3, alive = true, finished = true), "alive → j false → return @433")
        assertFalse(resolves(3, alive = false, finished = false), "!r() → return")
        assertFalse(resolves(3, alive = true, finished = false))
    }

    @Test fun `S8 Z1=10 - resolves unless the target is dead and finished`() {
        assertTrue(resolves(10, alive = true, finished = true), "@495 j(r0) false → @750 resolve")
        assertFalse(resolves(10, alive = false, finished = true), "@495-505 j && r() → return @508")
        assertTrue(resolves(10, alive = false, finished = false), "@502 !r() → @750 resolve")
        assertTrue(resolves(10, alive = true, finished = false))
    }

    /** `true` when the watcher released its own live claim (`bI()` → `k.c(this)` for an ax5). */
    private fun releasesClaim(alive: Boolean, finished: Boolean): Boolean {
        val w = world(); w.npcs.clear()
        val p = w.player; p.refreshBoxes()
        linked(w, 55, alive, finished)
        val e = ax5At(w, p.ak + 5000, p.al, 13, z2 = 55, aG = w.kEh[6])  // far away: ao() can never re-bind; script 6 = the long cutscene
        e.bindContext(w)
        assertTrue(e.claimActive(), "fixture: the watcher holds the claim")
        w.npcFsm.tickMissionLogic(e, w, p)
        return w.pendingRemove.contains(e)
    }

    @Test fun `S8 Z1=13 - the claim is released once the target is dead and finished`() {
        assertTrue(releasesClaim(alive = false, finished = true), "@548-569: j(r0) && r() → ab() → bI()")
        assertFalse(releasesClaim(alive = true, finished = true), "alive → no release")
        assertFalse(releasesClaim(alive = false, finished = false), "!r() → no release")
    }

    /** `true` when `ap()` fired (forwarded the script and removed the ax5 — `k.c(this)`). */
    private fun forwards(alive: Boolean, finished: Boolean): Boolean {
        val w = world(); w.npcs.clear()
        val p = w.player; p.refreshBoxes()
        val r = linked(w, 55, alive, finished)
        r.cd[5] = true                                                  // ap() @9: the target's cd[5] flag
        val e = ax5At(w, p.ak + 5000, p.al, 17, z2 = 55, aG = 55, z3 = w.kEh[6])
        w.npcFsm.tickMissionLogic(e, w, p)
        return w.pendingRemove.contains(e)
    }

    @Test fun `S8 Z1=17 - the script is forwarded when the target is dead and finished`() {
        assertTrue(forwards(alive = false, finished = true), "@639-671: j(r0) && r() → ap()")
        assertFalse(forwards(alive = true, finished = true), "alive → return @675")
        assertFalse(forwards(alive = false, finished = false), "!r() → return @675")
    }

    // ---------------------------------------------------------------- ax61 aR() harm arm

    private fun harm(s: Int, playerX: Int, bossX: Int = 300): Triple<Level0World, Entity, Entity> {
        val w = world(); w.npcs.clear()
        val boss = Entity(29, w.clips[7]).apply { setPositionPx(bossX, 150); refreshBoxes() }
        w.kAU = boss
        val p = w.player
        p.setAnim(0); p.S = 0; p.av = false
        p.setPositionPx(playerX, 150); p.refreshBoxes()
        val e = Entity(61, null).apply { setPositionPx(playerX, 150); refreshBoxes(); S = s }
        e.X[0] = playerX - 40; e.X[1] = 130; e.X[2] = playerX + 40; e.X[3] = 170
        w.npcs.add(e)
        return Triple(w, e, p)
    }

    @Test fun `ax61 harm - S17 snaps the player into the grab like S2`() {
        for (px in intArrayOf(200, 340)) {                       // left and right of the boss
            val (w, e, p) = harm(17, px)
            w.npcFsm.ax61HarmArm(e, w, p)
            assertEquals(375, p.S, "@455-518: S == 2 || S == 17 → i(375), px=$px")
            assertEquals(w.kAU!!.al, p.al, "al = aU.al, px=$px")
            assertEquals(px >= 300, p.av, "av = !(ak < aU.ak), px=$px")
        }
    }

    @Test fun `ax61 harm - S4 and S5 hit from both sides without the grab snap`() {
        for (s in intArrayOf(4, 5)) for (px in intArrayOf(200, 340)) {
            val (w, e, p) = harm(s, px)
            val sfx0 = w.sfxLog.size
            w.npcFsm.ax61HarmArm(e, w, p)
            assertTrue(18 in w.sfxLog.drop(sfx0), "the hit lands (S$s, px=$px)")
            assertTrue(p.S != 375, "no grab snap for S$s")
            assertEquals(px >= 300, p.av, "turned toward the boss (S$s, px=$px)")
        }
    }

    @Test fun `ax61 harm - a degenerate X box or a player in S9 375 376 377 takes nothing`() {
        val (w, e, p) = harm(4, 200)
        e.X[2] = e.X[0]                                            // X[0] == X[2] → @347 goto 521
        val sfx0 = w.sfxLog.size
        w.npcFsm.ax61HarmArm(e, w, p)
        assertEquals(sfx0, w.sfxLog.size)
        for (ps in intArrayOf(9, 375, 376, 377)) {
            val (w2, e2, p2) = harm(4, 200)
            p2.S = ps
            val n0 = w2.sfxLog.size
            w2.npcFsm.ax61HarmArm(e2, w2, p2)
            assertEquals(n0, w2.sfxLog.size, "player S$ps is exempt")
        }
    }

    // ---------------------------------------------------------------- ax11 script-claim head

    /** Mission 6's two script-bound sentries (record Z[13] = 97 / 112 → `ca` 5 / 6; the scripts have no blocks). */
    private fun sentry(): Pair<Level0World, Entity> {
        val w = world(aj = 5)
        val e = w.npcs.first { it.ax == 11 && it.aw == 95 }
        assertTrue(e.ca != -1 && e.scriptBound && e.cd[7], "fixture: the record binds a claim script")
        w.player.setPositionPx(e.ak + 600, e.al); w.player.refreshBoxes()
        return w to e
    }

    @Test fun `ax11 script head - a bound soldier runs aa() only and stands still while the player is away`() {
        val (w, e) = sentry()
        val x0 = e.ak; val y0 = e.al
        repeat(40) { w.npcFsm.tick(e, w.player) }
        assertEquals(x0, e.ak, "@1951 ifne 7660: the patrol arm never runs")
        assertEquals(y0, e.al)
        assertEquals(2, e.S)
        assertTrue(e.P and 16 != 0, "@1887-1896 P |= 16")
        assertTrue(e.scriptBound && e.ca != -1, "no interrupt: the claim stays bound")
    }

    @Test fun `ax11 script head - the player within 40 x 50 releases the claim and the soldier turns normal`() {
        val (w, e) = sentry()
        e.av = true                                                             // the sight rect points left: a player on the right is outside it
        w.player.setPositionPx(e.ak + 30, e.al + 10); w.player.refreshBoxes()
        assertFalse(losL(e, w.player, w), "fixture: l() is false — only the 40 x 50 proximity can release it")
        w.npcFsm.tick(e, w.player)
        assertEquals(-1, e.ca, "@1908-1916 bI(); ca = -1")
        assertFalse(e.scriptBound, "@1861-1876 d = false")
        assertEquals(0, e.P and 16, "P &= -17")
        assertEquals(0, e.aA, "@1935-1947 S2 → aA = 0")
    }

    @Test fun `ax11 script head - the player's attack box on the soldier releases it too`() {
        val (w, e) = sentry()
        e.av = true
        w.player.setPositionPx(e.ak + 120, e.al); w.player.refreshBoxes()      // far outside 40 x 50, behind the sight rect
        w.player.X[0] = e.W[0]; w.player.X[1] = e.W[1]; w.player.X[2] = e.W[2]; w.player.X[3] = e.W[3]
        assertFalse(losL(e, w.player, w), "fixture: l() is false — only the attack box can release it")
        w.npcFsm.tick(e, w.player)
        assertEquals(-1, e.ca, "@1797-1811 a(aS.X, W)")
    }

    @Test fun `ax11 script head - a player in S268 S267 S291 or a hidden one does not interrupt`() {
        for (ps in intArrayOf(268, 267, 291)) {
            val (w, e) = sentry()
            w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes(); w.player.S = ps
            w.npcFsm.tick(e, w.player)
            assertTrue(e.ca != -1 && e.scriptBound, "@1825-1858: player S$ps keeps the claim")
        }
        val (w, e) = sentry()
        w.player.setPositionPx(e.ak + 10, e.al); w.player.refreshBoxes()
        w.player.aA = w.player.aA or 8                                          // hidden: the 40 x 50 proximity needs !(aA & 8)
        w.player.X.fill(0)
        w.npcFsm.tick(e, w.player)
        assertTrue(e.ca != -1, "(aA & 8) != 0 → not near, and l() is blind")
    }

    // ---------------------------------------------------------------- k.l(int) 13 → 31 re-entry

    @Test fun `l(13) with a stats text re-enters as screen 31 - no banner block and no sting`() {
        val withText = world()
        withText.kBx = 3
        withText.drainCommands()
        withText.stateL(13)
        assertEquals(31, withText.jC, "@96-99 this = 31; goto 0")
        assertTrue(withText.drainCommands().none { it == Command.PlaySfx(7) },
            "case 31 has no arm: the fail/win sting @116-120 is not played")

        val plain = world()
        plain.kBx = -1
        plain.drainCommands()
        plain.stateL(13)
        assertEquals(13, plain.jC)
        assertTrue(plain.drainCommands().any { it == Command.PlaySfx(7) }, "bx < 0 keeps the sting (control)")
    }

    // ---------------------------------------------------------------- i.L / i.M anchor statics, g.L / g.M gauge point

    @Test fun `the interact gauge parks its point on the player's own L M - not on the touch-anchor statics`() {
        val w = world(); val p = w.player
        val target = Entity(11, w.clips[7]).apply { setPositionPx(p.ak + 40, p.al); refreshBoxes() }
        p.g = target
        p.cN = 1000                                                   // past the frame duration: the step runs
        Entity.L = 7; Entity.M = 8
        try {
            p.interactGauge(w)
            assertEquals((target.W[0] + target.W[2]) shr 1, p.gQL, "g.aB() @117-137 putfield g.L")
            assertEquals(((target.W[1] + target.W[3]) shr 1) - 10, p.gQM, "putfield g.M")
            assertEquals(7, Entity.L, "the statics i.L / i.M are untouched")
            assertEquals(8, Entity.M)
        } finally { Entity.L = -1; Entity.M = -1 }
    }

    @Test fun `every writer of the i_L i_M anchor feeds the one touch hit-test`() {
        val w = world(); w.cm = 0
        val x = w.player.ak; val y = w.player.al
        try {
            w.iL = x; w.iM = y                                         // NpcFsm markerSpawn74 / markerMove74
            assertEquals(-1, w.resolvePadZone(x - w.camX, y - w.camY), "@33979-33992 b(II)Z: within 70 px of (L, M)")
            w.iL = -1; w.iM = -1
            assertEquals(4, w.resolvePadZone(x - w.camX, y - w.camY), "cleared → the wheel again")
            w.player.markerPoint(x, y)                                 // Entity.markerPoint = o(x, y)
            assertEquals(-1, w.resolvePadZone(x - w.camX, y - w.camY))
        } finally { Entity.L = -1; Entity.M = -1 }
    }
}
