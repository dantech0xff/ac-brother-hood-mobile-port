package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 375 — `b(true)` is the whole world pass, not just its head.
 *
 * `b(z2)` (simple k.java:3450) = the window/veil head + the draw-list build
 * + every entity's `F()`; only the `ad()` bubbles depend on `z2`. The pause
 * (jc14, k.java:1123), death/end (jc12/13 unless `j.i()`, :1110), jc31
 * (:1454) and help-from-pause (G() :2415) frames call `b(true)`, as do the
 * l(12)/l(13) and l(14)-from-play transitions (:1657, :1792). The port ran
 * only the head there, so `F()` effects (the cG hit flash, candle fades,
 * parked-entity `s()`, linked-FX `ae.s()`) froze behind those screens.
 */
class Slice375Test {
    private fun flasher(w: Level0World): Entity {
        val p = w.player
        val e = Entity(30, w.clips[7]).apply {
            aw = 9_001; setAnim(0); setPositionPx(p.ak + 40, p.al)
        }
        e.refreshBoxes()
        w.npcs += e
        assertTrue(e.inPlayV(w), "precondition: in the view, so in the draw list")
        return e
    }

    @Test fun `F() runs on the pause transition and behind the pause menu`() {
        val w = world()
        w.tick(emptyList())
        val e = flasher(w)
        e.cGCount = 6
        assertEquals(8, w.jC)
        w.stateL(14)
        assertEquals(5, e.cGCount, "l(14) from play paints b(true) once (:1790-1792)")
        w.tick(emptyList())
        assertEquals(4, e.cGCount, "case 14 paints b(true) every frame (:1123)")
    }

    @Test fun `F() runs on the death transition and the death screen`() {
        val w = world()
        w.tick(emptyList())
        val e = flasher(w)
        e.cGCount = 6
        w.stateL(12)
        assertEquals(5, e.cGCount, "l(12) paints b(true) (:1656-1657)")
        w.tick(emptyList())
        assertEquals(4, e.cGCount, "jc12 frames paint b(true) while !j.i() (:1109-1110)")
    }
}
