package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 418 (P1b — `proven` in `i/k.javap.txt`): `i.v()` culls against
 * `k.ac`, the rect the camera phase LAST rebuilt — `k.m(int)` writes
 * `ac = {O, P, O+400, P+240}` as its very tail (@2680-2719, after the
 * `ai()` settle latch) and `k.D()` writes it in the `j.c==21`/claim
 * snap arm (@52-91) and again at its own tail (@651-690). There is no
 * other `k.ac` writer (`putstatic`/`iastore` xref). A mid-tick camera
 * write — the group-script camera lerp `r04 == 1` arm
 * (`w.kO += (r18-w.kO)/(…)`, Entity.kt:2342, proven i-side) or the
 * `kDU` world-shift (`camY = camB`, simI :5448) — therefore does NOT
 * move the cull edge until the next camera pass. `i.u()` keeps reading
 * the live `k.O/k.P` (u() @4/@17, proven) — only the `a(k.ac, …)`
 * overlap inside `v()` is stale.
 */
class Slice418Test {

    /** ax3 entity with a staged `Y` box — hits the default
     *  `a(k.ac, Y)` arm of `v()` (no early arm touches ax3). */
    private fun stageEntity(x0: Int, y0: Int, x1: Int, y1: Int) =
        Entity(3, null).also { e ->
            e.ak = (x0 + x1) / 2
            e.al = (y0 + y1) / 2
            e.Y[0] = x0; e.Y[1] = y0; e.Y[2] = x1; e.Y[3] = y1
            e.i = 10                       // `au > i` never fires
        }

    @Test
    fun `v() keeps the previous camera rect through a mid-tick write`() {
        val w = world(aj = 0)
        val cx = w.camX; val cy = w.camY
        // entity fully right of the snapshot's right edge (cx+400)
        val e = stageEntity(cx + 410, cy + 90, cx + 430, cy + 110)
        assertFalse(e.inPlayV(w), "offscreen entity is culled")

        // mid-tick camera write — the script lerp arm `w.kO += …`
        // (original: `putstatic k.O`; `k.ac` untouched until m(I)/D())
        w.kO = cx + 100
        assertEquals(cx, w.camRect[0],
            "k.ac still holds the previous camera phase's left edge")
        assertFalse(e.inPlayV(w),
            "the cull still reads the stale rect — entity stays offscreen")

        // the next camera pass rebuilds ac — m(I)'s tail (@2680-2719)
        w.kM(1)
        assertEquals(w.camX, w.camRect[0],
            "the camera phase rebuilds ac from the moved camera")
    }

    @Test
    fun `v() does not cull an entity the mid-tick camera just left`() {
        val w = world(aj = 0)
        val cx = w.camX; val cy = w.camY
        val e = stageEntity(cx + 100, cy + 90, cx + 140, cy + 110)
        assertTrue(e.inPlayV(w), "on-screen entity is seen")

        // camera lurches right mid-tick — entity now behind the left edge
        w.kO = cx + 400
        assertTrue(e.inPlayV(w),
            "stale ac still covers the entity — no mid-tick cull")

        w.kM(1)
        assertEquals(w.camX, w.camRect[0],
            "the camera phase rebuilds ac from the moved camera")
    }

    @Test
    fun `kD's autoscroll tail rebuilds the snapshot too`() {
        val w = world(aj = 0)
        // mid-tick write, then the bh3 camera pass — D()'s autoscroll
        // tail stores ac = {O,P,O+400,P+240} (@651-690, proven)
        w.kO = w.camX + 100
        w.kD()
        assertEquals(w.camX, w.camRect[0],
            "D() rebuilds ac at its tail even after mid-tick writes")
        assertEquals(w.camY, w.camRect[1])
        assertEquals(w.camX + 400, w.camRect[2])
        assertEquals(w.camY + 240, w.camRect[3])
    }

    @Test
    fun `kD's camera-claim snap arm rebuilds the snapshot`() {
        val w = world(aj = 0)
        // claim entity owns the camera (cd[0] + kZ) — D()'s snap arm
        // (@26-91: cA=O; cB=P; ac rebuild; return, proven)
        w.kC = Entity(3, null).also { it.cd[0] = true }
        w.kZ = true
        w.kO = w.camX + 77
        w.kD()
        assertEquals(w.camX, w.camRect[0],
            "the snap arm rebuilds ac from the moved camera")
    }
}
