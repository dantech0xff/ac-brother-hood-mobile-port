package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 414 — the camera and the collect-streak meter re-read from the raw bytes
 * (`k.javap.txt`: `s()V` @0-113, `D()V`, `m(I)V` @0-2680).
 *
 * - `k.s()` @8-16: `if (ax >= 105) return` right after `az++` — a full meter never re-tiers (the port
 *   fell through and could lower the cap to `30 + tier * 15`).
 * - `k.D()` (the flying / chase camera): the director's row scan stops at the SECOND `22` cell
 *   (@300 `goto 440`; the port went on narrowing with every later one), and `ae = aS` runs every tick
 *   (@93).
 * - `k.m(int)` (the main camera): every bound clamp is an if / else-if chain — the scroll holder's
 *   `W` (@1954-2084), the `R/S` and `T/U` walls (@2120-2235) and the focus box (@1204-1277) — so a
 *   box narrower than the view keeps its LEFT / TOP edge (the port let the right / bottom clamp
 *   override it: level-0's 350 × 180 ax37 holder pushed the camera 50-60 px off); the ax43 carrier's
 *   mirror branch (`ae.av == false`) picks its speed from `dx > 200 || (dx > 100 && ag == z1 << 8)`,
 *   `50 < dx <= 100 && ag == z1 << 8`, `dx <= 50 && ag <= z1 << 8` — else the old speed is kept;
 *   a non-null `g.c` wins the `cA` pick over the rope `g.a` (@349); the `ab` / rope snap-x override
 *   (@2428-2499) also runs after the snap arm.
 */
class Slice414Test {
    // ---------------------------------------------------------------- k.s()

    @Test fun `s - a full meter returns before the tier lookup`() {
        val w = world()
        w.kAx = 105; w.kAz = 149                       // az = 150 after the increment: tier 1 would be ax 45
        w.player.x1 = 100
        w.kCollectStreak()
        assertEquals(105, w.kAx, "@8-16 ax >= 105 → return")
        assertEquals(100, w.player.x1)
    }

    @Test fun `s - a tier raises the cap and the meter, a lower tier never lowers them`() {
        val w = world()
        w.kAx = 30; w.kAz = 99                         // az = 100 → tier 1
        w.player.x1 = 30
        w.kCollectStreak()
        assertEquals(45, w.kAx, "ax = 30 + 1 * 15")
        assertEquals(45, w.player.x1, "g.e(ax): the meter follows the raised cap")
        w.kAz = 99; w.kAx = 75; w.player.x1 = 80       // az 100 → tier 1: ax = 45 < 75 → g.f clamps only
        w.kCollectStreak()
        assertEquals(45, w.kAx)
        assertEquals(45, w.player.x1, "g.f(ax): x[1] > ax → clamp (no raise: old 75 >= new 45)")
    }

    // ---------------------------------------------------------------- k.D()

    @Test fun `D - the director corridor scan stops at the second 22`() {
        val w = world(aj = 1)
        w.stateL(8); settleIntro(w)
        val et = w.level.layers.first { it.id == 0 }
        val row = w.player.al / 20
        for (cx in 0 until et.cols) et.cells[row * et.cols + cx] = 0
        for (cx in intArrayOf(10, 40, 41)) et.cells[row * et.cols + cx] = 22
        w.kAi = true; w.boundMinX = -1; w.boundMaxX = -1
        w.kD()
        assertEquals(200, w.boundMinX, "first 22: R = 10 * 20")
        assertEquals(820, w.boundMaxX, "second 22: S = (40 + 1) * 20 — the third (col 41) is never read")
    }

    @Test fun `D - the camera focus is reset to the player every tick`() {
        val w = world(aj = 1)
        w.stateL(8); settleIntro(w)
        w.kAe = Entity(43, null)
        w.kD()
        assertSame(w.player, w.kAe, "@93 k.ae = k.aS")
    }

    // ---------------------------------------------------------------- k.m(int)

    private fun holder(l: Int, t: Int, r: Int, b: Int, wall: Boolean) =
        Entity(37, null).apply { W[0] = l; W[1] = t; W[2] = r; W[3] = b; aF = if (wall) 1 else 0 }

    private fun settle(w: Level0World, n: Int = 300) = repeat(n) { w.kM(0) }

    @Test fun `m - a scroll holder narrower than the view keeps its left and top edge`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(1100, 620); p.setAnim(0); p.aZ = true; p.refreshBoxes()
        w.kAe = p
        w.kAh = holder(1000, 500, 1350, 680, wall = true)       // 350 x 180 < the 400 x 240 view
        w.kO = 900; w.kP = 450; w.rebuildCamRect()
        try {
            settle(w)
            assertTrue(w.kO in 999..1000, "@1954-2049 cA < W0 → cA = W0 (the lerp settles 1 short), the right clamp is the else: ${w.kO}")
            assertTrue(w.kP in 499..500, "cB < W1 → cB = W1, the bottom clamp is the else: ${w.kP}")
        } finally { w.kAh = null }
    }

    @Test fun `m - the R and S bound walls are an else-if too`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(1100, 620); p.setAnim(0); p.aZ = true; p.refreshBoxes()
        w.kAe = p
        w.kAh = holder(0, 0, 100000, 100000, wall = false)      // keeps R/S/T/U alive (n() runs when ah == null)
        w.kR = 1000; w.kSBound = 1350                           // S - 400 = 950 < R
        w.kO = 900; w.rebuildCamRect()
        try {
            settle(w)
            assertTrue(w.kO in 999..1000, "@2120-2182 cA < R → cA = R; `cA > S - 400` is the else: ${w.kO}")
        } finally { w.kAh = null; w.kR = 0; w.kSBound = 0 }
    }

    @Test fun `m - the ax43 carrier mirror branch keeps its speed outside the three arms`() {
        fun camXAfter(dx: Int, ag: Int): Int {
            val w = world(); w.npcs.clear()
            val p = w.player
            p.setPositionPx(300, 400); p.refreshBoxes()
            val car = Entity(43, null).apply {
                setPositionPx(dx, 400); S = 1; av = false; Z[1] = 8; this.ag = ag
            }
            w.kO = 0; w.kP = 300; w.rebuildCamRect()
            System.arraycopy(intArrayOf(10, 310, 390, 500), 0, p.Y, 0, 4)      // inside the view: i.b(aS.Y, ac)
            w.kAe = car
            w.kM(0)
            return w.kO
        }
        assertEquals(12, camXAfter(250, 0), "dx > 200 → 150 % (z1 * 150 / 100) whatever ag is")
        assertEquals(0, camXAfter(150, 0), "100 < dx <= 200, ag != z1 << 8 → the old speed (0) stays")
        assertEquals(12, camXAfter(150, 8 shl 8), "100 < dx <= 200, ag == z1 << 8 → 150 %")
        assertEquals(8, camXAfter(80, 8 shl 8), "50 < dx <= 100, ag == z1 << 8 → z1")
        assertEquals(0, camXAfter(80, 0), "50 < dx <= 100, ag != z1 << 8 → sticky")
        assertEquals(4, camXAfter(30, 0), "dx <= 50, ag <= z1 << 8 → 50 %")
        assertEquals(0, camXAfter(30, 9 shl 8), "dx <= 50, ag > z1 << 8 → sticky")
    }

    @Test fun `m - a non-null g_c beats the rope g_a for the x target`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val rope = Entity(43, null).apply { setPositionPx(1500, 300) }
        val crate = Entity(51, null).apply { setPositionPx(900, 300) }
        p.setPositionPx(1000, 300); p.setAnim(0); p.refreshBoxes()
        p.ga = rope
        w.gc = crate
        w.kAe = p
        val saved = Entity.at
        Entity.at = null
        w.kO = 0; w.rebuildCamRect()
        try {
            settle(w)
            assertTrue(w.kO in 699..700, "@349-: `g.c != null → cA = g.c.ak - 200` (the rope would give 1300): ${w.kO}")
        } finally { Entity.at = saved; w.gc = null; p.ga = null }
    }

    @Test fun `m - the ab snap override also runs after the snap arm`() {
        val w = world(); w.npcs.clear()
        w.kAb = true
        w.kM(w.kAd)                                    // `r5 & ad` → snap
        assertFalse(w.kAb, "@2428-2499: ab is cleared on the snap tick")
    }
}
