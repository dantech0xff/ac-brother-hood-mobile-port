package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

/**
 * Slice 359 — ax11's `case 2/3/92` patrol arm (structured/i.java:4104-4135;
 * bytecode i.javap.txt I() offsets 4019-4523).
 *
 * The port had three divergences there:
 *  - the turn test `z4 = (cells < -Z[5] && av) || (cells > Z[6] && !av)`
 *    was ported as `av && cells >= -Z[5]`: a guard turned as soon as it
 *    faced west inside its range and never turned at the east end;
 *  - the "linked to a crate it is not riding" skip read a field nothing
 *    ever wrote (`platform`), so it never applied;
 *  - `am()` (i.java:5832-5843) answers false while riding a support — the
 *    port's copy had no `s != null` guard;
 * and the arm ran `aD()` itself although only the L777 tail does
 * (offsets 7338-7392), so a patrolling soldier rode a crate twice a tick.
 */
class Slice359Test {
    /** A flat floor run of ±8 cells with 4 open rows above: (cx, ground row). */
    private fun flatFloor(w: Level0World): Pair<Int, Int> {
        for (r in 6 until w.kBq) for (cx in 10 until w.kBp - 10) {
            val ok = (-8..8).all { dx ->
                val g = w.collisionCell(cx + dx, r)
                (g == 20 || g == 5) && (1..4).all { dy -> w.collisionCell(cx + dx, r - dy) < 5 }
            }
            if (ok) return cx to r
        }
        error("no flat floor in level 0")
    }

    /** A patrolling soldier `cells` from its home on the flat run. */
    private fun staged(cells: Int, west: Boolean): Pair<Level0World, Entity> {
        val w = world(); w.npcs.clear()
        val (cx, r) = flatFloor(w)
        val e = Entity(11, w.clips[7])
        e.aw = 1111
        e.Z[3] = cx * 20 + 10                                   // home
        e.Z[5] = 2; e.Z[6] = 2                                  // range ±2 cells
        e.setPositionPx(cx * 20 + 10 + cells * 20, r * 20 - 1)
        e.aB = 300; e.setAnim(3); e.av = west
        e.refreshBoxes()
        w.npcs += e
        w.player.setPositionPx(e.ak + 3000, e.al); w.player.refreshBoxes()
        return w to e
    }

    @Test fun `a guard facing west inside its range keeps walking`() {
        val (w, e) = staged(cells = -1, west = true)
        w.npcFsm.tick(e, w.player)
        assertEquals(3, e.S, "z4 false: no i(2)")
    }

    @Test fun `a guard past either end of its range stops to turn`() {
        val (w, e) = staged(cells = 3, west = false)
        w.npcFsm.tick(e, w.player)
        assertEquals(2, e.S, "east of +Z[6] facing east → i(2)")
        assertEquals(19, e.aC, "S3 re-armed aC=20, then aC--")
        val (w2, e2) = staged(cells = -3, west = true)
        w2.npcFsm.tick(e2, w2.player)
        assertEquals(2, e2.S, "west of -Z[5] facing west → i(2)")
    }

    @Test fun `the countdown ends in i(3) with the facing flipped`() {
        val (w, e) = staged(cells = 3, west = false)
        repeat(21) { w.npcFsm.tick(e, w.player) }
        assertEquals(3, e.S)
        assertEquals(true, e.av, "turned west")
        w.npcFsm.tick(e, w.player)
        assertEquals(3, e.S, "facing back into range: no turn")
    }

    @Test fun `a guard linked to a crate it does not ride never stops`() {
        val (w, e) = staged(cells = 3, west = false)
        val crate = Entity(51, null)
        crate.aw = 5151
        crate.setPositionPx(e.ak - 2000, e.al)                  // far: no aD() bind
        intArrayOf(crate.ak - 30, crate.al - 20, crate.ak + 30, crate.al).copyInto(crate.W)
        w.npcs += crate
        e.Z[7] = crate.aw
        w.npcFsm.tick(e, w.player)
        assertEquals(3, e.S, "s == null && k.q(Z[7]).ax == 51 → skip the stop")
    }

    @Test fun `aD() binds a crate once per tick`() {
        val (w, e) = staged(cells = 0, west = false)
        e.k = false; e.setAnim(2)
        val crate = Entity(51, null)
        crate.aw = 5152
        crate.setPositionPx(e.ak, e.al + 5)
        intArrayOf(e.ak - 30, e.al - 5, e.ak + 30, e.al + 15).copyInto(crate.W)
        crate.ag = 512
        w.npcs += crate
        e.Z[7] = crate.aw
        val ak0 = e.ak
        w.npcFsm.tick(e, w.player)
        assertSame(crate, e.s)
        assertEquals(crate.W[1] + 1, e.al, "bind: al = s.W[1]+1 (a second aD() → +3)")
        assertEquals(ak0 + 2, e.ak, "bind: ak += s.ag >> 8, once")
    }
}
