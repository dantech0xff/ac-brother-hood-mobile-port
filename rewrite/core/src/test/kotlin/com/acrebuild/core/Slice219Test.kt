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

class Slice219Test {
    /**
     * Slice-219 verdict — "street soldiers dormant without trigger zone"
     * is the verbatim au-park gate (k.java L215 arm, slice 216), not a
     * missing trigger: an ax11 record far from the camera stays frozen
     * (`au >= 2 && !P|16 → skip`), and the moment the camera reaches it
     * (`au < 2`, on-screen LOD) it ticks again — exactly the original's
     * "parked entity" semantics. No trigger-zone wake exists or is
     * needed: proximity alone releases it.
     */
    @Test fun `far street soldier parks off-camera and wakes on camera approach`() {
        val w = world()
        settleIntro(w)
        // uid151 — the street patrol record at (2213,472), ~2100px east
        // of the x85 spawn: au stays >= 2 while the camera sits at spawn.
        val s = w.npcs.first { it.ax == 11 && it.aw == 151 }
        val s0 = s.S
        // tick 40 frames with the camera at spawn — the soldier must not
        // advance its anim (frozen by the park gate verbatim).
        repeat(40) { w.tick(emptyList()) }
        assertEquals(s0, s.S, "off-camera soldier stays parked (au=${s.au})")
        assertEquals(0, s.T, "parked soldier's anim never advances")
        // now bring the camera (and player) to it — au<2 releases the
        // freeze; the FSM runs on the very next tick.
        w.player.setPositionPx(s.ak - 40, s.al)
        w.kO = s.ak - 200; w.kP = s.al - 120; w.rebuildCamRect()
        var ticked = false
        repeat(60) {
            w.tick(emptyList())
            w.kO = s.ak - 200; w.kP = s.al - 120; w.rebuildCamRect()
            if (s.T != 0 || s.S != s0) { ticked = true; return@repeat }
        }
        assertTrue(ticked, "soldier wakes when camera makes au<2 (au=${s.au}, S=${s.S}, T=${s.T})")
    }
}
