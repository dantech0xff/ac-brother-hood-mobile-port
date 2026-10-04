package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

/**
 * Slice 405 — `I()` has no universal side-collide.
 *
 * The whole method carries `a(Z)V` (`i.a(boolean)`, the wall/floor resolver) at exactly
 * six offsets (i.javap `I()`): 3085 (S142), 4574 (S23), 4676 (S4/22), 5032 (S18),
 * 5464 (S6), 6167 (S0/106/107/135). The port ran it in front of the arm switch for every
 * soldier-family state except S25, so a patrolling, flinching or fighting soldier was
 * extruded out of walls (and re-seated on slopes) every tick — which the original never
 * does. The arms that carry their own `a(true)` still do.
 */
class Slice405Test {
    /** A wall face with open floor to its left: `(wall x, floor y)`. */
    private fun wallBesideFloor(w: Level0World): Pair<Int, Int> {
        for (cy in 6 until 90) for (cx in 8 until 400) {
            if (w.collisionCell(cx, cy - 1) == 20 && w.collisionCell(cx, cy - 2) == 20 &&
                w.collisionCell(cx, cy - 3) == 20 &&
                (-4..-1).all { w.collisionCell(cx + it, cy) == 20 &&
                    (1..4).all { r -> w.collisionCell(cx + it, cy - r) < 12 } })
                return (cx * 20) to (cy * 20 - 1)
        }
        error("no wall beside open floor in the level-0 map")
    }

    private fun soldier(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = w.npcs.first { it.ax == 11 }
        e.setPositionPx(x, y)
        e.Z[0] = 0; e.cq = false; e.aB = 300; e.aA = 1; e.s = null; e.ae = null
        e.setAnim(s); e.refreshBoxes()
        return e
    }

    private fun akAfterOneTick(s: Int): Pair<Int, Int> {
        val w = world(); val (wx, wy) = wallBesideFloor(w)
        val start = wx - 3                      // its right half sits ~7 px inside the wall
        val e = soldier(w, start, wy, s)
        w.player.setPositionPx(start - 300, wy); w.player.refreshBoxes()   // far away
        w.npcFsm.tick(e, w.player)
        return start to e.ak
    }

    @Test fun `states without their own a(true) are not extruded from a wall`() {
        for (s in intArrayOf(11, 5, 85)) {
            val (start, after) = akAfterOneTick(s)
            assertEquals(start, after, "S$s: no side-collide in the arm — position untouched")
        }
    }

    @Test fun `states that carry a(true) still resolve the wall`() {
        for (s in intArrayOf(23, 18)) {
            val (start, after) = akAfterOneTick(s)
            assertNotEquals(start, after, "S$s calls a(true) itself (@4574 / @5032)")
        }
    }
}
