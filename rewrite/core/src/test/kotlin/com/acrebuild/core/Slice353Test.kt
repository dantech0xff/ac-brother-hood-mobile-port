package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Slice 353 — the projectile volley `i.a(int, boolean)` re-checked against
 * its bytecode (i.javap.txt:27553+; the structured view of this method is
 * flagged "decompiled incorrectly").
 *
 *  - fan (offsets 458-762): odd counts keep slot 0 on the aim and step
 *    ±r02·k; even counts straddle the aim (slots 0/1 at ∓r02/2). The port
 *    had the parity inverted and left even-count slot 0 straight.
 *  - speed (764-857): every caller outside ax54/30/56 (the ax24 burst,
 *    ax64) and ax56 with Z[4]==2 fly at 1280, not 2048.
 *  - anim (1054-1119): ax30 takes Z[8] like ax54; `i(int,int)` set 3 is
 *    `i(28)`; `b(IIII)` returns 0/4 (up/down) within ±5120 of vertical.
 */
class Slice353Test {
    private fun seeded(): Level0World {
        val w = world()
        val seed = Entity(24, w.clips[40])
        w.npcFsm.initAx24(seed, listOf(24, 0, 0, 0, 0, 0, 0, 0), w)   // 50-slot pool
        return w
    }

    private fun shooter(ax: Int, x: Int = 500, y: Int = 600): Entity {
        val e = Entity(ax, null)
        e.ak = x; e.al = y
        intArrayOf(x - 10, y - 10, x + 10, y + 10).copyInto(e.W)
        intArrayOf(x - 10, y - 10, x + 10, y + 10).copyInto(e.X)
        return e
    }

    /** (ag, ah − gravity) of pool slots 0 until n. */
    private fun vel(w: Level0World, n: Int) =
        (0 until n).map { val s = w.projectilePool!![it]!!; s.ag to (s.ah - w.kY) }

    @Test fun `ax24 nine-shot burst is the 45-degree ring with slot 0 on the aim`() {
        val w = seeded()
        // ax24 aims 100px east (bytecode 360-381), so iB = j.b(25600, 0) = 0
        w.npcFsm.runnerBurst(shooter(24), 9, false, w)
        assertEquals(listOf(
            1280 to 0,          // 0: straight
            930 to 930,         // 1: +32
            930 to -930,        // 2: −32
            0 to 1280,          // 3: +64
            0 to -1280,         // 4: −64
            -930 to 930,        // 5: +96
            -930 to -930,       // 6: −96
            -1280 to 0,         // 7: +128
            -1280 to 0          // 8: −128 — the original doubles the back shot
        ), vel(w, 9))
    }

    @Test fun `even volleys straddle the aim`() {
        val w = seeded()
        w.npcFsm.runnerBurst(shooter(24), 2, false, w)   // r02 = 45° = 32
        val (s0, s1) = vel(w, 2)
        assertEquals(s0.first, s1.first, "slot 0 at −16, slot 1 at +16")
        assertEquals(-s0.second, s1.second)
        assertEquals(true, s0.second < 0 && s1.second > 0)

        val w4 = seeded()
        w4.npcFsm.runnerBurst(shooter(24), 4, false, w4) // r02 = 35° = 24
        val v = vel(w4, 4)
        assertEquals(v[0].first, v[1].first); assertEquals(-v[0].second, v[1].second)
        assertEquals(v[2].first, v[3].first); assertEquals(-v[2].second, v[3].second)
        assertEquals(true, v[2].second < v[0].second, "slot 2 at −48, outside slot 0")
    }

    @Test fun `speed tiers follow the bytecode`() {
        fun speedOf(ax: Int, prep: (Entity) -> Unit = {}): Int {
            val w = seeded()
            val e = shooter(ax); prep(e)
            // aim at the player's W-centre, 100px east of the launch point
            val p = w.player
            intArrayOf(590, 590, 610, 610).copyInto(p.W)
            w.npcFsm.runnerBurst(e, 1, false, w)
            return w.projectilePool!![0]!!.ag
        }
        assertEquals(1280, speedOf(24), "other callers → 1280")
        assertEquals(1280, speedOf(64), "ax64 → 1280")
        assertEquals(2048, speedOf(56) { it.Z[4] = 0 })
        assertEquals(1280, speedOf(56) { it.Z[4] = 2 }, "ax56 Z[4]==2 → 1280")
        assertEquals(2048, speedOf(54) { it.Z[9] = 0; it.Z[0] = 1 })
        assertEquals(1280, speedOf(54) { it.Z[9] = 2; it.Z[0] = 1 })
    }

    @Test fun `anim set comes from Z8 for ax54 and ax30 and set 3 is anim 28`() {
        fun animOf(ax: Int, prep: (Entity) -> Unit): Int {
            val w = seeded()
            val e = shooter(ax); prep(e)
            intArrayOf(590, 590, 610, 610).copyInto(w.player.W)
            w.npcFsm.runnerBurst(e, 1, false, w)
            return w.projectilePool!![0]!!.S
        }
        assertEquals(22, animOf(30) { it.Z[8] = 2; it.Z[3] = 1 }, "ax30 reads Z[8]")
        assertEquals(28, animOf(54) { it.Z[8] = 3; it.Z[0] = 1 }, "set 3 → i(28)")
        assertEquals(28, animOf(56) { it.Z[3] = 3 }, "ax56 reads Z[3]")
        assertEquals(2 + 23, animOf(56) { it.Z[3] = 1 }, "set 1 → i(dir+23), east = 2")
    }

    @Test fun `b IIII gives 0 and 4 for near-vertical shots`() {
        val w = seeded()
        w.npcFsm.runnerBurst(shooter(24), 9, false, w)
        val s = (0 until 9).map { w.projectilePool!![it]!!.S }
        assertEquals(2, s[0], "east")
        assertEquals(3, s[1], "down-right: slope ≥ 0, dx > 0")
        assertEquals(1, s[2], "up-right: slope < 0, dx > 0")
        assertEquals(4, s[3], "straight down: |dx| ≤ 5120, dy ≥ 0")
        assertEquals(0, s[4], "straight up")
    }
}
