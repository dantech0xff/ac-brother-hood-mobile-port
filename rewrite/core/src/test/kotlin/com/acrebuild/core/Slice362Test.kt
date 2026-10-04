package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertSame

/**
 * Slice 362 — the ax44 door FSM is `bv()` (bytecode i.javap.txt bv()
 * offsets 0-590).
 *
 * The port refreshed the door's box at the top of every tick; `bv()`
 * reads `W` as the previous frame's `I()` tail `t()` left it, one anim
 * step behind. It also re-checked the ax58 promotion every tick (the
 * original only on the resolving tick), had mode 1's `bf()` tests
 * inverted, used a plain overlap instead of `i.a` (which rejects point
 * boxes), and zeroed the player's `aj`/`ah` on the S3/S7 and static-bank
 * crush, which only call `aS.i(50)`.
 */
class Slice362Test {
    private fun door(w: Level0World): Entity {
        val d = w.npcs.first { it.ax == 44 && it.Z[4] == 0 && it.S in 0..3 }
        d.Z[0] = 0; d.Z[2] = 999; d.Z[5] = -1
        return d
    }

    private fun standIn(p: Entity, box: IntArray) {
        p.setPositionPx((box[0] + box[2]) / 2, box[3] - 1)
        p.refreshBoxes()
    }

    @Test fun `the crush tests the box the previous frame left`() {
        val w = world()
        val d = door(w)
        d.S = 3; d.T = 0
        d.refreshBoxes()
        val live = d.W.copyOf()
        val p = w.player
        standIn(p, live)
        intArrayOf(live[0] - 400, live[1], live[2] - 400, live[3]).copyInto(d.W)
        w.npcFsm.tickDoor(d, p)
        assertNotEquals(50, p.S, "bv() never calls t(): last frame's box is clear")
        d.refreshBoxes()
        w.npcFsm.tickDoor(d, p)
        assertEquals(50, p.S, "the I() tail t() box overlaps")
    }

    @Test fun `init leaves the ctor box`() {
        val w = world()
        val d = w.npcs.first { it.ax == 44 && it.S in 8..13 }
        val ctor = d.W.copyOf()
        d.refreshBoxes()
        assertEquals(d.W.toList(), ctor.toList(), "initDoor ends with t()")
    }

    @Test fun `mode 1 opens while the ax58 runs and returns once it stops`() {
        val w = world()
        val d = door(w)
        val lever = Entity(58, null)
        d.ac = lever
        d.Z[0] = 1; d.S = 0
        lever.S = 1                                          // bf() true
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(1, d.S, "Z[4]==S && ac.bf() → i(S+1)")
        d.S = 2
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(2, d.S, "running: no return to base")
        lever.S = 2                                          // bf() false
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(0, d.S, "Z[4]!=S && !ac.bf() → i(Z[4])")
        lever.S = 2; d.S = 0
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(0, d.S, "idle link: the base state holds")
    }

    @Test fun `the ax58 promotion runs only on the resolving tick`() {
        val w = world()
        val d = door(w)
        val lever = Entity(58, null).apply { aw = 9_999; S = 1 }
        w.npcs += lever
        d.ac = null; d.Z[5] = 9_999; d.Z[0] = 0; d.S = 0; d.Z[3] = 5
        w.npcFsm.tickDoor(d, w.player)
        assertSame(lever, d.ac)
        assertEquals(256, lever.P and 256, "a(i) binds P|256")
        assertEquals(0, d.Z[0], "S1 at resolve: no promotion")
        lever.S = 0
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(0, d.Z[0], "already linked: never re-checked")
    }

    @Test fun `a resting S0 lever promotes on resolve`() {
        val w = world()
        val d = door(w)
        val lever = Entity(58, null).apply { aw = 9_998; S = 7 }
        w.npcs += lever
        d.ac = null; d.Z[5] = 9_998; d.Z[0] = 0; d.S = 0
        w.npcFsm.tickDoor(d, w.player)
        assertEquals(1, d.Z[0])
    }

    @Test fun `S3 crush keeps the player's velocity, S0 zeroes it`() {
        val w = world()
        val d = door(w)
        val p = w.player
        d.S = 3; d.T = 0
        d.refreshBoxes()
        standIn(p, d.W)
        p.ah = 1234; p.aj = 567
        w.npcFsm.tickDoor(d, p)
        assertEquals(50, p.S)
        assertEquals(listOf(1234, 567), listOf(p.ah, p.aj), "S3: aS.i(50) alone")
        val w2 = world()
        val d2 = door(w2)
        val p2 = w2.player
        d2.S = 0; d2.T = 0; d2.Z[3] = 50
        d2.refreshBoxes()
        standIn(p2, d2.W)
        p2.ah = 1234; p2.aj = 567
        w2.npcFsm.tickDoor(d2, p2)
        assertEquals(50, p2.S)
        assertEquals(listOf(0, 0), listOf(p2.ah, p2.aj), "S0: aj = ah = 0 first")
    }

    @Test fun `a point box never crushes`() {
        val w = world()
        val d = w.npcs.first { it.ax == 44 && it.S in 8..13 }
        val p = w.player
        d.refreshBoxes()
        standIn(p, d.W)
        val cx = (p.W[0] + p.W[2]) / 2; val cy = (p.W[1] + p.W[3]) / 2
        intArrayOf(cx, cy, cx, cy).copyInto(d.W)
        w.npcFsm.tickDoor(d, p)
        assertNotEquals(50, p.S, "i.a rejects a point box")
    }
}
