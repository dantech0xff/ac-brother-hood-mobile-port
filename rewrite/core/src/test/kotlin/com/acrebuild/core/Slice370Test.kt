package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 370 — the type-2 kill and the type-6 hit at the head of `g.e()`'s
 * shared tail L353d (g.javap.txt e() 13662-13736, proven):
 *
 *     13662  if ((aR == 2 || aO == 2 || L()) && g.a == null) {
 *                ah = 0; aj = 0; g.e(0); i(50); return }      // 13711
 *     13712  if (aO == 6 || aR == 6) a(18, 0, 0, this)
 *
 * `L()` = `e(ak/20, al/20) == 2` (i.javap.txt L() 0-25). The slice-368
 * port never killed and fired the hit only for `aO == 6` outside type-2.
 * Plus the `g.c(i)` ax72 arm the m7 capstone re-route runs into
 * (g.javap.txt c(i) 346-460, proven).
 */
class Slice370Test {
    private fun player(ak: Int, al: Int, s: Int): Entity {
        val p = Entity(0, null)
        p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10; p.W[1] = al - 20; p.W[3] = al
        p.S = s
        return p
    }

    /** The resting case: a landing puts the anchor in the row above the
     *  floor (`d()`: al = ((W[3]+1)/20)*20 - 1). A type-2 strip there is
     *  read by `L()` alone (aR is the floor, aO the open head cell). */
    @Test fun `L() kills on a type-2 strip on top of the floor`() {
        // row 5 = type-2 strip, row 6 = floor; anchor al=119 (row 5)
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy ->
            if (cy == 5) 2 else if (cy >= 6) 20 else 0 })
        val p = player(200, 119, 0)
        p.W[3] = 119
        PlayerFsm(w).tick(p, Pad())
        assertEquals(50, p.S, "L() → i(50)")
        assertEquals(0, p.x1)
    }

    /** `aO == 2` (the head cell) kills too. */
    @Test fun `a type-2 head cell kills`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy ->
            if (cy == 4) 2 else if (cy >= 6) 20 else 0 })
        val p = player(200, 119, 0)              // W = [190, 99, 210, 119]
        PlayerFsm(w).tick(p, Pad())
        assertEquals(50, p.S, "aO == 2 → i(50)")
    }

    /** `g.a != null` (standing on a ride) spares the player; the type-6
     *  hit still runs. */
    @Test fun `a mounted player is spared`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy ->
            if (cy == 5) 2 else if (cy >= 6) 20 else 0 })
        val p = player(200, 119, 0)
        val lift = Entity(66, null); lift.S = 6
        p.ga = lift
        PlayerFsm(w).tick(p, Pad())
        assertNotEquals(50, p.S, "g.a != null → no kill")
        assertTrue(p.x1 > 0)
    }

    /** g.javap.txt c(i) 346-460: on an ax72 target, Z[0]∈{1,2} — both
     *  wheel configs — reset the orbit phase `cM = 0` (435-457); only
     *  Z[0]∈{0,3,4} take the Z[1]/Z[2] cF/cx overrides (355-432); other
     *  Z[0] values do neither. The port reset cM for Z[0]==1 only, so
     *  the m7 re-route's second Z0==2 wheel (uid49 after uid36) skipped
     *  its spin-in. */
    @Test fun `grabLunge resets the wheel phase for both wheel configs`() {
        val w = world()
        val p = w.player
        for (z0 in 0..5) {
            p.setPositionPx(300, 150); p.refreshBoxes(); p.S = 0
            p.cM = 1
            val m = Entity(72, null)
            m.setPositionPx(340, 150)
            m.W[0] = 330; m.W[1] = 140; m.W[2] = 350; m.W[3] = 160
            m.Z[0] = z0; m.Z[1] = 9000; m.Z[2] = 30
            p.grabLunge(m, w)
            val wheel = z0 == 1 || z0 == 2
            val params = z0 == 0 || z0 == 3 || z0 == 4
            assertEquals(if (wheel) 0 else 1, p.cM, "Z0=$z0 cM")
            assertEquals(if (params) 9000 else 5120, p.cF, "Z0=$z0 cF")
            assertEquals(if (params) (30 * Trig.M) / 360 else (8 * Trig.M) / 360,
                p.cx, "Z0=$z0 cx")
        }
    }

    /** 13712-13736: `aO == 6 || aR == 6` — a type-6 cell under the feet
     *  fires `a(18,0,0,this)` (the port checked only `aO == 6`). */
    @Test fun `a type-6 cell under the feet fires the op18 hit`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 6 else 0 })
        val p = player(200, 100, 8)              // W = [190, 80, 210, 100]
        val x1Before = p.x1
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "op18 → i(43)")
        assertTrue(p.x1 < x1Before, "g.a() pays u[au]")
    }
}
