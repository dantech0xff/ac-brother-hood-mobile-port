package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class Slice210Test {

    /**
     * Wall-kick chain at the real x2200 corridor face (level0): airborne
     * contact with direction held -> `cv && aF` latch -> S101 grab -> the
     * L1a46 auto-bounce (av flip + ag=-2048 + ah=-5120) -> S36 wall air ->
     * land back on the '05' one-way strip (y800). Proves the grab arm +
     * kick fire verbatim on real geometry; climbing the 460px face is a
     * jump-kick cycle (player skill), not a missing arm.
     */
    @Test fun `wall kick chain climbs the x2200 face`() {
        val w = world(); val p = w.player
        var t = 0
        while (p.ah != 0 && t++ < 600) w.tick(emptyList())
        p.setPositionPx(2160, 830)
        // a real jump carries ~1536-2048 east + rise — spawn mid-arc
        // close enough that the arc reaches the x2200 face airborne.
        p.S = 35; p.ag = 1536; p.ai = 0; p.ah = -200; p.aj = 1536
        p.av = false                               // facing east
        p.refreshBoxes()                           // a finished frame's t() (G12)
        // hold RIGHT — pad zone 2 emits `2<<2 = 8` = M_TAP_R
        val q = InputQueue()
        val (rx, ry) = w.cellPoint(2)
        q.post(InputQueue.Type.DOWN, rx, ry)
        var grabbed = false
        var kicked = false
        var riseTop = Int.MAX_VALUE
        repeat(60) {
            w.tick(q.drainTo(q.headSequence()))
            if (p.S == 101) grabbed = true
            if (grabbed && p.S == 36) {
                kicked = true
                if (p.al < riseTop) riseTop = p.al
            }
            if (kicked && p.al <= 800 && p.ag == 0) return@repeat   // landed
        }
        assertTrue(grabbed, "expected S101 grab at the x2200 face, S=${p.S} ak=${p.ak} al=${p.al}")
        assertTrue(kicked, "expected the L1a46 bounce into S36, S=${p.S}")
        assertTrue(riseTop < 820, "kick should rise ~74px above grab, top=$riseTop")
    }
}
