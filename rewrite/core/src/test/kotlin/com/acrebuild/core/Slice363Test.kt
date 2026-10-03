package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 363 — the below-the-camera kill is the flying player's only.
 *
 * `!v() && al > k.P + 240 → k.l(12)` sits in `i.B()` (structured
 * i.java:1034-1035), called only from `g.n()` (g.javap.txt:17406), which
 * is called only from `I()`'s `case 25` (i.javap.txt:19687 → 19877). The
 * port also ran it at the end of every frame in every mission, so a ground
 * player whose fall outran a bounded camera died where the original's
 * lives on.
 */
class Slice363Test {
    @Test fun `a ground player below a held camera lives on`() {
        val w = world(); settleIntro(w)
        val p = w.player
        w.kU = w.camY + 240                       // k.U bounds the camera bottom
        p.setPositionPx(p.ak, w.camY + 400)
        p.refreshBoxes()
        w.tick(emptyList())
        assertTrue(p.al > w.camY + 240 && !p.inPlayV(w),
            "the kill's precondition holds: al=${p.al} camY=${w.camY}")
        assertNotEquals(12, w.jC, "case 0 → aS.e() never runs i.B()")
    }

    @Test fun `the flying player below the camera still dies in i B`() {
        val w = world(); w.npcs.clear(); w.kAj = 1   // bh[1] == 3
        w.stateL(8)
        w.tick(emptyList())                          // i.w first-call latch
        val p = w.player
        p.setPositionPx(p.ak, w.camY + 400)
        p.refreshBoxes()
        w.tick(emptyList())
        assertEquals(12, w.jC, "g.n() → i.B() L1f7 → k.l(12)")
    }
}
