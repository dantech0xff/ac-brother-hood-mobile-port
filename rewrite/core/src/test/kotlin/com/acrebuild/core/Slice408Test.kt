package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 408 — the ax27 fuse/barrel `bL()` (`i.javap.txt` @0-1203) and the push helper
 * `a(i, int, int[])` (@51313) it shares with the `I()` dispatch tail (@8032-8051).
 *
 * - S0 @283-356: `if (ae == null || ae.S != 7) { G(); a(7, ak, al-85) }` and the pin of
 *   whatever `ae` is now (the port respawned only for a null `ae`).
 * - S1/S21 @366-458: `G(); if (r() || (aS.az == -2 && T == last)) { T = last; U = 0;
 *   if (aS.az == -2) { aS.i(269); aS.az = 100 } }` and then the shared tail @1119 — never the
 *   S4 fuse arm (the port ran it, so a lifted barrel next to an open ax58 started its fuse).
 * - S4/S23 @745-759: `k.aD = this` replaces an earlier owner.
 * - helper @179-191: `if (r1.aZ && ah > 0) return` after the from-below reset — the port kept a
 *   private copy for the ax27 tail that did not return and fell into the horizontal exits.
 */
class Slice408Test {
    private fun ax27At(w: Level0World, x: Int, y: Int, s: Int, z0: Int = -1, z1: Int = 0,
                       aA: Int = 0): Entity {
        val e = Entity(27, w.clips[48])
        e.setPositionPx(x, y)
        val f = listOf(27, 9, x, y, 0, s, 0, aA, z0, z1, -1)
        w.npcFsm.initAx27(e, f, w)
        w.npcs.add(e)
        return e
    }

    private fun last(e: Entity) = e.clip!!.frameCount(e.S) - 1

    // ------------------------------------------------------------------ S1 / S21

    @Test fun `S1 never runs the S4 fuse arm - a lifted barrel next to an open ax58 keeps its anim`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val door = Entity(58, null).apply { aw = 77; S = 1 }      // S1 ∈ the even-state set
        w.npcs.add(door)
        val e = ax27At(w, 300, 300, 1, z0 = 77, z1 = 3)
        e.T = last(e); e.U = e.clip!!.frameDuration(e.S, e.T) - 1
        assertTrue(e.animFinished(), "fixture: the anim has finished")
        p.az = 100
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(1, e.S, "@458 goto 1119: the S4 arm @598 is not entered from S1")
        assertTrue(w.kAD !== e, "k.aD untouched")
        e.setAnim(21); e.T = last(e); e.U = e.clip!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(21, e.S, "S21 shares the arm")
    }

    @Test fun `S1 hands the player over (i 269, az 100) through the pinned-last-frame door as well`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = ax27At(w, 300, 300, 1)
        e.T = last(e); e.U = 0
        assertFalse(e.animFinished(), "fixture: pinned on the last frame, not finished")
        p.az = -2
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(269, p.S, "@430-458: aS.i(269)")
        assertEquals(100, p.az)
        assertEquals(0, e.U)
    }

    @Test fun `S1 waits while the player is not az -2 and the anim runs`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = ax27At(w, 300, 300, 1)
        e.T = 0; e.U = 0
        p.az = 100
        val s0 = p.S
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(s0, p.S)
        assertEquals(100, p.az)
    }

    @Test fun `S1 finished with az not -2 only pins the last frame`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = ax27At(w, 300, 300, 1)
        e.T = last(e); e.U = e.clip!!.frameDuration(e.S, e.T) - 1
        p.az = 100
        val s0 = p.S
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(s0, p.S, "@433-438: az != -2 → no hand-over")
        assertEquals(0, e.U)
    }

    // ------------------------------------------------------------------ S4 / S23

    @Test fun `S4 takes k_aD over from an earlier owner (745-759)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val door = Entity(58, null).apply { aw = 77; S = 4 }
        w.npcs.add(door)
        val other = ax27At(w, 100, 100, 8)
        w.kAD = other
        val e = ax27At(w, 300, 300, 4, z0 = 77, z1 = 0)
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(6, e.S, "linked ax58 in an even state → i(6)")
        assertTrue(e.P and 16 != 0)
        assertSame(e, w.kAD, "k.aD = this, the earlier owner is replaced")
    }

    @Test fun `S4 with an odd ax58 state does nothing`() {
        val w = world(); w.npcs.clear(); val p = w.player
        val door = Entity(58, null).apply { aw = 77; S = 3 }
        w.npcs.add(door)
        val e = ax27At(w, 300, 300, 4, z0 = 77, z1 = 0)
        w.npcFsm.tickAx27(e, w, p)
        assertEquals(4, e.S)
        assertTrue(w.kAD !== e)
    }

    // ------------------------------------------------------------------ S0 marker

    @Test fun `S0 aA1 respawns a marker that has left S7 and pins it at (ak, al-85)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        p.refreshBoxes()
        val e = ax27At(w, p.ak, p.al, 0, aA = 1)
        val stale = w.spawnPickup(3, e.ak, e.al); e.ae = stale
        w.npcFsm.tickAx27(e, w, p)
        assertNotSame(stale, e.ae, "@291-310: ae.S != 7 → G(); a(7, ak, al-85)")
        assertEquals(7, e.ae!!.S)
        assertEquals(e.ak, e.ae!!.ak); assertEquals(e.al - 85, e.ae!!.al)
    }

    @Test fun `S0 aA1 keeps a marker that is still in S7 and re-pins it`() {
        val w = world(); w.npcs.clear(); val p = w.player
        p.refreshBoxes()
        val e = ax27At(w, p.ak, p.al, 0, aA = 1)
        val live = w.spawnPickup(7, e.ak + 40, e.al); e.ae = live
        w.npcFsm.tickAx27(e, w, p)
        assertSame(live, e.ae)
        assertEquals(e.ak, live.ak); assertEquals(e.al - 85, live.al)
    }

    // ------------------------------------------------------------------ shared push helper

    private fun pushed(aZ: Boolean, viaDispatchTail: Boolean): Pair<Int, Int> {
        val w = world(); w.npcs.clear()
        val p = w.player
        val box = intArrayOf(90, 100, 130, 140)
        val e = Entity(27, w.clips[48]).apply {
            setPositionPx(110, 140); S = 18; P = P or 4096
            ah = 512
            for (i in 0..3) W[i] = box[i]
        }
        w.npcs.add(e)
        val v = Entity(11, null).apply {
            setPositionPx(110, 160)
            W[0] = 100; W[1] = 120; W[2] = 120; W[3] = 160
            ah = -100; this.aZ = aZ
        }
        if (viaDispatchTail) {
            // `I()` @8032: `if (ax != 0) a(k.aS, P, W)` with the player as the victim
            p.setPositionPx(110, 160); p.refreshBoxes()
            for (i in 0..3) p.W[i] = v.W[i]
            p.ah = -100; p.aZ = aZ
            w.npcFsm.defaultArm(e, p)
            return p.ak to p.al
        }
        w.paint(v)
        w.npcFsm.tickAx27(e, w, p)
        return v.ak to v.al
    }

    @Test fun `from-below exit returns without moving a grounded-rider when the pusher falls (179-191)`() {
        val (ak, al) = pushed(aZ = true, viaDispatchTail = false)
        assertEquals(110, ak, "no horizontal exit after the early return")
        assertEquals(160, al, "no vertical exit either")
    }

    @Test fun `from-below exit lifts the victim below the box when it is not a rider`() {
        val (ak, al) = pushed(aZ = false, viaDispatchTail = false)
        assertEquals(110, ak)
        assertEquals(140 + (160 - 120) + 5, al, "@194: al = r3[3] + (al - W[1]) + 5")
    }

    @Test fun `the dispatch tail and the ax27 tail share the helper`() {
        val a = pushed(aZ = true, viaDispatchTail = true)
        assertEquals(110, a.first); assertEquals(160, a.second)
        val b = pushed(aZ = false, viaDispatchTail = true)
        assertEquals(185, b.second)
    }

    // ------------------------------------------------------------------ ax54 / ax56 facing

    private fun runner54(w: Level0World, s: Int, ax: Int = 54): Entity {
        val e = Entity(ax, w.clips[19])
        val f = MutableList(22) { 0 }
        f[0] = ax; f[2] = w.kO + 160; f[3] = w.kP + 60
        w.npcFsm.initAx54(e, f, w)
        e.setPositionPx(w.kO + 160, w.kP + 60)
        e.setAnim(s); e.refreshBoxes()
        e.runnerBz = true; e.runnerC = 1                       // armed, chain not done
        e.P = e.P or 16
        w.npcs.add(e)
        return e
    }

    private fun companion(w: Level0World, uid: Int, x: Int, y: Int): Waypoint {
        w.waypointPool.load(listOf(55, uid, x, y, 0, 0, 0, 0, 0))
        return w.waypointPool.find(uid)!!
    }

    private fun facing54(s: Int, companionLeft: Boolean?, avBefore: Boolean): Boolean {
        val w = world(); w.npcs.clear(); val p = w.player
        p.setPositionPx(w.kO + 300, w.kP + 60); p.refreshBoxes()      // player to the right
        val e = runner54(w, s)
        e.av = avBefore
        e.wpF = companionLeft?.let { left ->
            companion(w, 61, e.ak + if (left) -50 else 50, e.al).also { it.h = it.a - e.ak; it.i = 0 }
        }
        w.npcFsm.tickAx54(e, w, p)
        return e.av
    }

    @Test fun `ax54 walk S1-S3 turn toward the companion (440-476)`() {
        for (s in 1..3) {
            assertTrue(facing54(s, companionLeft = true, avBefore = false), "S$s F.a < ak → av")
            assertFalse(facing54(s, companionLeft = false, avBefore = true), "S$s F.a >= ak → !av")
        }
    }

    @Test fun `ax54 walk S0 and S4 force av false after the companion test (477-496)`() {
        for (s in intArrayOf(0, 4)) {
            assertFalse(facing54(s, companionLeft = true, avBefore = true), "S$s with F")
            assertFalse(facing54(s, companionLeft = null, avBefore = true), "S$s without F (player is right → Q() says av)")
        }
    }

    @Test fun `ax54 walk without a companion faces the player (Q)`() {
        // player at ak+140 → `av = ak > aS.ak` = false; flip the player to the left → true
        val w = world(); w.npcs.clear(); val p = w.player
        p.setPositionPx(w.kO + 20, w.kP + 60); p.refreshBoxes()
        val e = runner54(w, 2)
        e.av = false
        w.npcFsm.tickAx54(e, w, p)
        assertTrue(e.av, "S2: Q() → ak > aS.ak")
    }

    @Test fun `ax54 attack S6-S8 turn toward the companion, S5 and S9 force av false (595-653)`() {
        for (s in 6..8) assertTrue(facing54(s, companionLeft = true, avBefore = false), "S$s")
        for (s in intArrayOf(5, 9)) assertFalse(facing54(s, companionLeft = true, avBefore = true), "S$s")
    }

    private fun facing56(s: Int, companionLeft: Boolean?, avBefore: Boolean): Boolean {
        val w = world(); w.npcs.clear(); val p = w.player
        p.setPositionPx(w.kO + 300, w.kP + 60); p.refreshBoxes()
        val e = Entity(56, w.clips[19])
        val f = MutableList(22) { 0 }
        f[0] = 56; f[2] = w.kO + 160; f[3] = w.kP + 60
        w.npcFsm.initAx56(e, f, w)
        e.setPositionPx(w.kO + 160, w.kP + 60)
        e.setAnim(s); e.refreshBoxes()
        e.runnerBz = true; e.P = e.P or 16
        e.aq = e.ak; e.ar = e.al
        w.npcs.add(e)
        e.av = avBefore
        e.wpF = companionLeft?.let { left ->
            companion(w, 61, e.ak + if (left) -50 else 50, e.al).also { it.h = it.a - e.ak; it.i = 0 }
        }
        w.npcFsm.tickAx56(e, w, p)
        return e.av
    }

    @Test fun `ax56 walk and attack facing follow the same shape (220-276, 823-881)`() {
        for (s in 1..3) assertTrue(facing56(s, true, false), "walk S$s")
        for (s in intArrayOf(0, 4)) assertFalse(facing56(s, true, true), "walk S$s forced")
        for (s in 6..8) assertTrue(facing56(s, true, false), "attack S$s")
        for (s in intArrayOf(5, 9)) assertFalse(facing56(s, true, true), "attack S$s forced")
    }

    // ------------------------------------------------------------------ ax54 companion pin

    @Test fun `ax54 pins the companion to the runner only while it travels (938-1159)`() {
        val w = world(); w.npcs.clear(); val p = w.player
        p.setPositionPx(w.kO + 300, w.kP + 60); p.refreshBoxes()
        // not travelling: no chain waypoint → B stays false → the companion is left alone
        val e = runner54(w, 1)
        val c = companion(w, 62, 5, 6); c.h = 7; c.i = 9
        e.wpF = c
        w.npcFsm.tickAx54(e, w, p)
        assertFalse(e.runnerB)
        assertEquals(5, c.a); assertEquals(6, c.b)
        // travelling: a far waypoint arms the leg → B true → pinned at ak + h, al + i
        val w2 = world(); w2.npcs.clear(); val p2 = w2.player
        p2.setPositionPx(w2.kO + 300, w2.kP + 60); p2.refreshBoxes()
        val e2 = runner54(w2, 1)
        w2.waypointPool.load(listOf(55, 31, 5000, 5000, 0, 0, 0, 5, 0))
        e2.Z[1] = 31
        val c2 = companion(w2, 62, 5, 6); c2.h = 7; c2.i = 9
        e2.wpF = c2
        val ak0 = e2.ak; val al0 = e2.al                  // the pin reads the pre-integration position
        w2.npcFsm.tickAx54(e2, w2, p2)
        assertTrue(e2.runnerB, "fixture: the leg arm engaged")
        assertEquals(ak0 + 7, c2.a); assertEquals(al0 + 9, c2.b)
    }

    // ------------------------------------------------------------------ ax46 spring pad

    private fun pad46(w: Level0World, s: Int): Entity {
        val e = Entity(46, w.clips[29])
        val f = MutableList(16) { 0 }
        f[0] = 46; f[2] = 300; f[3] = 300; f[5] = s
        w.npcFsm.initAx46(e, f, w)
        e.setPositionPx(300, 300); e.setAnim(s)
        e.W[0] = 280; e.W[1] = 280; e.W[2] = 320; e.W[3] = 300
        w.npcs.add(e)
        return e
    }

    /** One `aZ()` tick with the player falling onto the pad's upper part; returns the pad's anim. */
    private fun springS(s: Int, steps: Int = 1): Int = springST(s, 0, steps).first

    private fun springST(s: Int, t: Int, steps: Int = 1): Pair<Int, Int> {
        val w = world(); w.npcs.clear(); val p = w.player
        val e = pad46(w, s)
        p.setPositionPx(300, 290); p.refreshBoxes()
        p.W[0] = 290; p.W[1] = 270; p.W[2] = 310; p.W[3] = 292     // overlaps, feet above the top quarter
        p.ah = 100
        e.T = t; e.U = 0
        repeat(steps) { w.npcFsm.tickAx46(e, w, p) }
        return e.S to e.T
    }

    @Test fun `ax46 spring - S0 starts S1 and S11 starts S12 on a landing`() {
        assertEquals(1, springS(0))
        assertEquals(12, springS(11))
    }

    @Test fun `ax46 spring - a pad that is already springing keeps its anim (694-739)`() {
        assertEquals(1, springS(1), "S1: not restarted into itself nor re-targeted")
        assertEquals(12, springS(12), "S12: the 11-type pad must not turn into S1")
        val w = world(); val frames = pad46(w, 1).clip!!.frameCount(1)
        assertTrue(frames > 1, "fixture: S1 has more than one frame")
        assertEquals(1 to 1, springST(1, 1), "S1 keeps playing: setAnim(1) would reset T to 0")
    }
}
