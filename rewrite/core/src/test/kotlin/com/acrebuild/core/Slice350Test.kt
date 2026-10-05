package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 350 — `l()` for ax17/23/50: camera containment + the full sight
 * test (G8).
 *
 * Original `l()` (simple/i.java:2255-2406, proven): `ai()` fast path →
 * true; `aS.aA&8` → false; `case 17/23 → L70`, `case 50 → L77`, both
 * `bn → false; else b(this.W, k.ac)` — and `b(int[],int[])` (:665-676)
 * is CONTAINMENT, W fully inside the camera rect; then L88-L101: LOS
 * `e(aS)` and player ∉ {284,285}.
 *
 * Port before this slice:
 *  - `losL`'s `17, 23, 50` arm used overlap, so a half-visible ax23
 *    already spotted (ax23 then still ran the ax11 arms; since slice 356
 *    it takes `case 23 → L849` and runs none).
 *  - the civilian tick `aA()` L12 calls the full `l()`
 *    (simple/i.java:8742-8745), but `tickAx17` ported only the L70
 *    camera gate: no `ai()`, no `aA&8` blind, no LOS, no S284/285.
 *  - ax50 used a private duplicate (`seen50`); `aK` now calls `losL` too.
 */
class Slice350Test {
    /** Scan level0 for a row whose cells c±1..3 are open; `solidMid`
     *  picks whether cell c itself blocks the sight line. */
    private fun findRow(w: Level0World, solidMid: Boolean): Pair<Int, Int> {
        val lv = w.level
        for (r in 3 until lv.rows - 3) for (c in 4 until lv.cols - 4) {
            val sidesOpen = (1..3).all {
                lv.collisionCell(c - it, r) < 12 && lv.collisionCell(c + it, r) < 12
            }
            if (sidesOpen && (lv.collisionCell(c, r) >= 12) == solidMid) return r to c
        }
        error("no row with solidMid=$solidMid")
    }

    /** Put [e]'s W-box centre in cell (cx, cy). */
    private fun centreIn(e: Entity, cx: Int, cy: Int) {
        e.setPositionPx(cx * 20 + 10, cy * 20 + 10); e.refreshBoxes()
        val mx = (e.W[0] + e.W[2]) shr 1
        val my = (e.W[1] + e.W[3]) shr 1
        e.setPositionPx(e.ak + (cx * 20 + 10 - mx), e.al + (cy * 20 + 10 - my))
        e.refreshBoxes()
    }

    private fun civilian(w: Level0World): Entity {
        val e = Entity(17, w.clips[7])
        e.aB = 300; e.setAnim(57)
        w.npcs.add(e)
        return e
    }

    /** Civilian two cells left of the row's middle cell, player two right. */
    private fun stage(w: Level0World, solidMid: Boolean, mk: (Level0World) -> Entity): Entity {
        val (r, c) = findRow(w, solidMid)
        val e = mk(w)
        centreIn(e, c - 2, r)
        val p = w.player
        p.setAnim(0); p.aA = 0
        centreIn(p, c + 2, r)
        w.kO = minOf(e.W[0], p.W[0]) - 20; w.rebuildCamRect()
        w.kP = minOf(e.W[1], p.W[1]) - 20; w.rebuildCamRect()
        assertTrue(Entity.overlapI(e.W, w.camRect))
        return e
    }

    private fun noticed(e: Entity) = e.S in 60..67

    // -- ax17 civilian: aA() L12 → full l() -----------------------------------
    @Test fun `ax17 - fully on camera with clear LOS panics`() {
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = false, ::civilian)
        w.npcFsm.tickAx17(e, w, w.player)
        assertTrue(noticed(e), "l() true → panic pick, got S${e.S}")
        assertTrue(16 in w.sfxLog, "k.A(16)")
    }

    @Test fun `ax17 - a wall between civilian and player blocks the notice`() {
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = true, ::civilian)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "L90: e(aS) LOS blocked → l() false")
    }

    @Test fun `ax17 - half on camera is not seen (b() containment)`() {
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = false, ::civilian)
        w.kO = ((e.W[0] + e.W[2]) shr 1) - 400; w.rebuildCamRect() // right edge cuts W
        assertTrue(Entity.overlapI(e.W, w.camRect))
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "W not inside k.ac → L70 false")
    }

    @Test fun `ax17 - player S284, S285 or aA&8 is never noticed`() {
        for (blind in listOf<(Entity) -> Unit>(
            { it.S = 284 }, { it.S = 285 }, { it.aA = it.aA or 8 })) {
            val w = world(); w.npcs.clear()
            val e = stage(w, solidMid = false, ::civilian)
            blind(w.player)
            w.npcFsm.tickAx17(e, w, w.player)
            assertEquals(57, e.S, "L10/L92/L94 gate")
        }
    }

    @Test fun `ax17 - the ai() camera-focus zone alerts even off camera`() {
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = false, ::civilian)
        w.kO = 50_000; w.rebuildCamRect() // civilian off the view
        val zone = Entity(10, null); zone.S = 52
        w.kAe = zone
        w.npcFsm.tickAx17(e, w, w.player)
        assertTrue(noticed(e), "ai() fast path → l() true, got S${e.S}")
    }

    // -- ax23 and l() ------------------------------------------------------------
    private fun patroller(w: Level0World): Entity {
        val e = Entity(23, w.clips[7])
        e.aB = 300; e.setAnim(2); e.k = false; e.aA = 0
        w.npcs.add(e)
        return e
    }

    @Test fun `ax23 - l() needs W fully on camera`() {
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = false, ::patroller)
        assertTrue(losL(e, w.player, w), "control: W inside k.ac → b() true")

        val w2 = world(); w2.npcs.clear()
        val e2 = stage(w2, solidMid = false, ::patroller)
        w2.kO = ((e2.W[0] + e2.W[2]) shr 1) - 400; w2.rebuildCamRect() // half on camera
        assertFalse(losL(e2, w2.player, w2), "overlap is not b(W, k.ac)")
    }

    @Test fun `ax23 - runs no arm (case 23 goes straight to L849)`() {
        // slice 356: simple/i.java:5180 — the family head, then au()/L897
        val w = world(); w.npcs.clear()
        val e = stage(w, solidMid = false, ::patroller)
        w.npcFsm.tick(e, w.player)
        assertEquals(0, e.aA, "no patrol arm → never spots")
        assertEquals(2, e.S)
    }
}
