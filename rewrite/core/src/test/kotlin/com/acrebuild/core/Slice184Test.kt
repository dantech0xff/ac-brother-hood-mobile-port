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

// =====================================================================
// Slice 184 — remaining i.I() victim/dispatch arms: S11 windup-2, S12
// counter-bind (L478 full), S24 thrown-release, S99 kill-touch, S133/134/
// 145 carry-tracking, S138 hostage-free, S142/143 drop/land, S168/169
// hostage-secure/carry-rise, S139 death-wisp. Verbatim i.java:5427-5868.
// =====================================================================
class Slice184Test {

    private fun guard(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(11, w.clips[7])
        e.aB = 50; e.aA = 1
        e.setPositionPx(x, y); e.refreshBoxes()
        standOn(w, e)
        w.npcs.add(0, e)
        return e
    }

    private fun finish(e: Entity) {
        e.T = (e.clip?.frameCount(e.S) ?: 1) - 1
        e.U = (e.clip?.frameDuration(e.S, e.T) ?: 1) - 1
    }

    private fun park(w: Level0World) {
        w.player.setPositionPx(3000, 3000); w.player.refreshBoxes()
    }

    // -- S12 counter-bind (L478, i.java:5580-5640) ------------------------

    @Test fun `S12 player roll into the bind claims the lock`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.Z[0] = 2
        // The tick's own t() rebuilds X from clip7 S12: T0/T6 carry an
        // anchor-point X (rejected by `i.a`, slice 364), T1-T5 the real
        // strike box — start on T2 so the I() preamble's s() stays inside.
        e.setAnim(12); e.T = 2; e.refreshBoxes()
        val p = w.player
        // park the player inside e's X attack box, facing it, rolling
        p.setAnim(6)
        p.setPositionPx(e.ak, e.al); p.refreshBoxes()
        p.av = e.ak < p.ak                            // player faces e
        w.npcFsm.tick(e, p)
        assertEquals(18, e.S, "counter-bind → i(18) finisher-offer")
        assertSame(e, w.lockTarget, "aN = this")
        assertEquals(true, Entity.gE, "g.E = true")
        assertEquals(true, w.iAH, "b(2) slowmo armed")
        assertEquals(2, w.iAI)
    }

    @Test fun `S12 non-weakened roll-bind exits to L849`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.Z[0] = 0
        e.aB = BU73_HALF - 10                          // aB <= bu/2 → L495
        // The strike box exists on T1-T5 only (slice 364): on T4 the bind
        // lands and the L495 tail runs without an anim-end r().
        e.setAnim(12); e.T = 4; e.refreshBoxes()
        val p = w.player
        p.setAnim(6); p.setPositionPx(e.ak, e.al); p.refreshBoxes()
        p.av = e.ak < p.ak
        w.npcFsm.tick(e, p)
        assertEquals(18, e.S, "bind still lands i(18)")
        // I() @4870-4941 (slice 402): `Z0==2 || (Z0==0 && aB <= bu/2)` runs
        // the `aN = this; g.E = 1; b(2)` bind (only the marker is Z0==2's)
        assertSame(e, w.lockTarget, "half-HP Z0==0 still claims aN")
        assertEquals(true, Entity.gE, "g.E = true")
        assertEquals(true, w.iAH, "b(2) slowmo armed")
        assertEquals(null, e.ae, "the offer marker is a Z0==2 extra")
        // On the last frame (T6) the strike box is an anchor point: no
        // bind, and the anim-end r() routes to i(23) (L495).
        val w2 = world(); w2.npcs.clear()
        val e2 = guard(w2, 300, 150); e2.Z[0] = 0
        e2.aB = BU73_HALF - 10
        e2.setAnim(12); e2.refreshBoxes()
        val p2 = w2.player
        p2.setAnim(6); p2.setPositionPx(e2.ak, e2.al); p2.refreshBoxes()
        p2.av = e2.ak < p2.ak
        finish(e2)
        w2.npcFsm.tick(e2, p2)
        assertEquals(23, e2.S, "T6 point X: no bind, r() → i(23)")
    }

    // -- S11 windup-2 (L475, i.java:5578-5585) ----------------------------

    @Test fun `S11 anim end releases ae and strikes S12`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.av = false
        w.player.setPositionPx(e.ak - 40, e.al); w.player.refreshBoxes()
        e.setAnim(11); finish(e)
        w.npcFsm.tick(e, w.player)
        assertEquals(12, e.S, "S11 r() → i(12) strike")
        assertEquals(true, e.av, "Q() faces the player (left)")
    }

    // -- S24 thrown-release (L623, i.java:5720-5746) ----------------------

    @Test fun `S24 pins the player on the top edge while aC counts down`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150)
        e.setAnim(24); e.aC = 5; e.refreshBoxes()
        // the pinned victim is in S89 (the S99 arm's `aS.i(89)` bounce) — the tail's
        // `a()` push @265 skips `aS.S > 43`; at S0 it would shove him off the pin
        w.player.setAnim(89)
        w.npcFsm.tick(e, w.player)
        assertEquals(60, w.kAA, "k.aA = 60")
        assertEquals(0, w.player.ah)
        assertEquals(0, w.player.ag)
        assertEquals(e.W[1], w.player.al, "player pinned at top edge")
        assertEquals((e.W[0] + e.W[2]) shr 1, w.player.ak)
        assertEquals(4, e.aC, "aC decremented once")
        assertEquals(24, e.S, "still holding (no release)")
    }

    @Test fun `S24 expired counter flings the player and frees i5`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150)
        e.setAnim(24); e.aC = 0; e.refreshBoxes()
        w.npcFsm.tick(e, w.player)
        assertEquals(5, e.S, "release → i(5)")
        assertEquals(1, e.aA, "aA = 1")
    }

    // -- S99 kill-touch (L299 + aE, i.java:5442/9144) ---------------------

    @Test fun `S99 tumbling entity bounces a low-apex flying player`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150)
        e.setAnim(99); e.j = 6; e.refreshBoxes()
        val p = w.player
        p.setAnim(43)                                   // falling state
        p.gy = e.al - 300                               // apex only 15 cells up
        p.setPositionPx(e.ak, e.al - 40); p.refreshBoxes()
        // overlap: player feet above e's mid-line
        e.W[1] = p.W[3] + 10; e.W[3] = p.W[3] + 90
        w.npcFsm.tick(e, p)
        assertEquals(89, p.S, "close apex → aS.i(89) bounce")
        assertEquals(24, e.S, "tumbler → i(24) pin-down hold")
        assertEquals(30, e.aC, "aC=30 pin window")
        assertTrue(kotlin.math.abs(p.al - e.W[1]) <= 12,
            "player pinned near the top edge (e.W resnaps on anim)")
    }

    @Test fun `S99 tumbling entity plummets a far-apex flying player`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150)
        e.setAnim(99); e.j = 6; e.refreshBoxes()
        val p = w.player
        p.setAnim(43)
        p.gy = e.al - 600                               // 30 cells up → kill
        p.setPositionPx(e.ak, e.al - 40); p.refreshBoxes()
        e.W[1] = p.W[3] + 10; e.W[3] = p.W[3] + 90
        w.npcFsm.tick(e, p)
        assertEquals(0, p.x1, "g.x[1] = 0")
        assertEquals(e.al, p.al, "aS.al = this.al")
        assertEquals(20, e.S, "victim → i(20)")
    }

    // -- S133/134/145 carry-tracking (L293/297/289) ------------------------

    @Test fun `S133 tracks the carrier during carry-pickup`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(133)
        val p = w.player
        p.setAnim(270); p.ak = 777; p.al = 555
        standOn(w, p)          // L295 → L777: mid-air carrier → the copy
                               // falls; real carriers walk on ground
        w.npcFsm.tick(e, p)
        assertEquals(777, e.ak, "ak tracks carrier")
        assertEquals(p.al, e.al)
        assertEquals(133, e.S, "still carried")
    }

    @Test fun `S134 lets go when the carrier leaves carry-walk`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(134)
        w.player.setAnim(0)                             // not 271
        w.npcFsm.tick(e, w.player)
        assertEquals(135, e.S, "carrier dropped → i(135)")
    }

    @Test fun `S145 tracks during carry-walk`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(145)
        val p = w.player
        p.setAnim(271); p.ak = 100; p.al = 200
        w.npcFsm.tick(e, p)
        assertEquals(100, e.ak); assertEquals(200, e.al)
    }

    // -- S138 hostage-free (L280) -----------------------------------------

    @Test fun `S138 frees the af hostage link on anim end`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(138)
        val af = guard(w, 500, 150)
        e.af = af
        finish(e)
        w.npcFsm.tick(e, w.player)
        assertNull(e.af, "af released")
        assertTrue(af.S == 3 || af.S == 10, "af.i(k.bK?3:10)")
        assertTrue(e.P and 64 != 0, "P|=64 freeze")
    }

    // -- S142/143 drop + land (L271/L276) ----------------------------------

    @Test fun `S142 airborne drop stays falling`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 3000); e.setAnim(142)   // airborne row
        e.aZ = false
        val before = e.al
        w.npcFsm.tick(e, w.player)
        assertEquals(before + 10, e.al, "falls 10px")
    }

    @Test fun `S143 anim end freezes dead`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(143); finish(e)
        w.npcFsm.tick(e, w.player)
        assertEquals(0, e.aB, "aB = 0")
        assertTrue(e.P and 64 != 0, "P|=64")
    }

    // -- S168/169 hostage-secure + rise (L264/L267) ------------------------

    @Test fun `S168 secures the hostage and starts the rise`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(168); e.aw = 4
        val p = w.player; p.al = 900
        val kAp3 = w.kAp[3]
        finish(e)
        w.npcFsm.tick(e, p)
        assertEquals(169, e.S, "i(169) carry-rise")
        assertEquals(0, e.aB)
        assertEquals(293, p.S, "aS.i(293) carry anim")
        assertEquals(900, e.ar, "ar = aS.al")
        assertEquals(301, e.az)
        assertEquals(kAp3 + 1, w.kAp[3], "k.o(3) tally")
    }

    @Test fun `S169 rises while the top edge stays above ar`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(169); e.ar = e.W[1] - 100
        w.npcFsm.tick(e, w.player)
        assertEquals(-2048, e.ah, "rising thrust")
        assertTrue(e.P and 512 != 0, "P|=512 lock")
    }

    @Test fun `S169 at ar freezes the rise`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(169)
        e.ar = e.W[1] + 100                             // W[1] <= ar → freeze
        w.npcFsm.tick(e, w.player)
        assertEquals(0, e.ah, "rise ends")
        assertTrue(e.P and 64 != 0, "P|=64 freeze")
    }

    // -- S139 death wisp (L699) -------------------------------------------

    @Test fun `S139 corpse end spawns the wisp under bK`() {
        val w = world(); w.npcs.clear()
        val e = guard(w, 300, 150); e.setAnim(139)
        w.kBK = true
        finish(e)
        w.npcFsm.tick(e, w.player)
        assertTrue(e.P and 64 != 0, "P|=64 freeze")
        assertTrue(w.pendingInsert.any { it.ax == 8 && it.S == 2 },
            "death wisp a(8,59,2,av,ak,al,az-1) queued")
    }

    private companion object {
        const val BU73_HALF = 150                       // bu[0]/2 = 300/2
    }
}

// file-scope tuning knob for the capstone gauntlet dwell (swept 0..6)
private const val POLE_WAIT_TICKS = 6
