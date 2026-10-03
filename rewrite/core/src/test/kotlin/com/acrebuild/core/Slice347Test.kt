package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 347 — ax46 fire-cycle re-pin (aZ() L35/L37).
 *
 * Original (simple/i.java:13634-13640, :13680-13682; structured
 * :13568-13576, proven): in S5/S6/S328, while the player's W overlaps the
 * trap's X box, EVERY tick re-pins him — `aS.a(24, 330, 0, this)` for S328,
 * `aS.a(24, 110, 0, this)` for S5/S6 — before the `r()` release check.
 * op24 snaps the player onto the trap's W[0],W[1], so he follows the
 * sweeping X box to the last frame, where `r()` releases: S5 → i(3) and the
 * player's facing flips; the next touch on an unarmed S3 throws him (S165).
 *
 * The port only re-pinned S328. A player pinned by S4/S3 stayed where the
 * first pin left him; clip29's S5 X box sweeps right over its last frames,
 * the overlap failed on the `r()` tick, and the trap looped S5 forever with
 * the player stuck in S110 — the m3 upper-path trap (aw189 at x8090)
 * slice 346's re-routed capstone legs ran into.
 */
class Slice347Test {
    /** ax46 record (i.java:2700 → L206) on clip29 (`k.bl[0]`), as Slice50Test. */
    private fun ax46At(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = Entity(46, w.clips[29])
        e.setPositionPx(x, y)
        val f = mutableListOf(46, 0, x, y, 0, s, 0, 0, 0, 0, 0, 0, 0)
        while (f.size < 16) f += 0
        w.npcFsm.initAx46(e, f, w)
        w.npcs.add(e)
        return e
    }

    /** One `I()` pass in tickNpc order: preamble `s()`, the aZ() arm, then
     *  the L1f35 tail `t()` box refresh. */
    private fun step(w: Level0World, e: Entity, p: Entity) {
        e.advanceAnim()
        w.npcFsm.tickAx46(e, w, p)
        e.refreshBoxes()
    }

    @Test fun `S5 re-pins the overlapping player every tick via op24 110`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 5)
        val p = w.player
        p.setPositionPx((e.X[0] + e.X[2]) / 2, (e.X[1] + e.X[3]) / 2)
        p.refreshBoxes(); p.S = 110
        p.ag = 300; p.ah = 300
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(110, p.S, "aS.a(24,110) keeps the trap-grabbed pose")
        assertEquals(e.W[0], p.ak, "op24 → ak = src.W[0]")
        assertEquals(e.W[1], p.al, "op24 → al = src.W[1]")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
    }

    @Test fun `armed S4 pin cycle releases through S3 and the unarmed touch throws`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 4)               // Z[4]=4 → armed at S4 only
        assertEquals(4, e.Z[4])
        val p = w.player
        p.setPositionPx((e.X[0] + e.X[2]) / 2, (e.X[1] + e.X[3]) / 2)
        p.refreshBoxes(); p.S = 18
        step(w, e, p)
        assertEquals(5, e.S, "S4 armed touch → pin + i(5)")
        assertEquals(110, p.S)
        val cycle = w.clips[29]!!.frameCount(5) * 4
        var t = 0
        while (e.S == 5 && t++ < cycle) {
            p.refreshBoxes()
            step(w, e, p)
        }
        assertEquals(3, e.S, "r() on the last S5 frame → i(3) (was stuck in S5)")
        p.refreshBoxes()
        step(w, e, p)
        assertEquals(165, p.S, "unarmed S3 touch → aS.i(165) throw")
        assertEquals(7, e.S, "L11 → i(7)")
    }
}
