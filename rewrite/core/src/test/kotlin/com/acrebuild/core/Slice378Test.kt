package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Slice 378 — `b()`'s full-screen fill between the tile blit and the
 * entity loop (structured k.java:2848-2859): `i.bQ > 0` → white when
 * `bQ % 4 <= 2`, else red, then `bQ--`; else `i.ce` → white. Script
 * sub-op 6 sets `bQ` (i.java:18241), sub-ops 13/14 set/clear `ce`
 * (i.java:18144-18148). The port armed both and never stepped or drew
 * either.
 */
class Slice378Test {
    private val white = -1
    private val red = -65536

    @Test fun `bQ steps once per frame, white then red on every fourth`() {
        val w = world()
        settleIntro(w)
        w.iBQ = 5
        val seen = ArrayList<Int>()
        repeat(6) { w.tick(emptyList()); seen += w.backdropFill }
        // bQ 5→white, 4→white, 3→red, 2→white, 1→white, 0→none
        assertEquals(listOf(white, white, red, white, white, 0), seen)
        assertEquals(0, w.iBQ)
    }

    @Test fun `ce paints the backdrop white until cleared, under a running flash`() {
        val w = world()
        settleIntro(w)
        w.iCe = true
        w.tick(emptyList())
        assertEquals(white, w.backdropFill)
        w.iBQ = 3
        w.tick(emptyList())
        assertEquals(red, w.backdropFill, "bQ takes precedence (k.java:2848)")
        w.iBQ = 0
        w.iCe = false
        w.tick(emptyList())
        assertEquals(0, w.backdropFill)
    }

    @Test fun `b(true) frames step the flash too`() {
        val w = world()
        settleIntro(w)
        w.iBQ = 10
        w.stateL(14)                           // l(14) from play: b(true)
        assertEquals(9, w.iBQ)
        w.tick(emptyList())                    // case 14: b(true) per frame
        assertEquals(8, w.iBQ)
    }
}
