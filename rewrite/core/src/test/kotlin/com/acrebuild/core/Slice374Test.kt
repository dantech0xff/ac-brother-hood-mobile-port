package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 374 — the dialog typewriter and the dialog frame's `b(false)` pass
 * run in the world, once per frame.
 *
 * - The typewriter (`bQ && !A()` → `bR++; bT=(bR*bS)/16`, k.java:947-955)
 *   is part of the case-21 frame; the port stepped it from the renderer,
 *   so text typed at the display rate on device and never headless.
 * - `b(false)` runs on every jC 21 frame (k.java:867) and builds the draw
 *   list and calls each entity's `F()` (simple k.java:3450-3760); the
 *   port ran that pass only on play frames. The renderer also re-ran
 *   `F()`'s mutations (cG flash, ax9 `ad=null`, candle `P|=64`/removal)
 *   per rendered frame on top of the sim-side `drawStyleF`.
 */
class Slice374Test {
    private fun lineDialog(w: Level0World) {
        w.autoDismissDialog = false
        w.stateL(21)
        w.dlgU = 1; w.dlgV = 0; w.dlgW = 2
        w.dlgBM[0] = "A LONG FIRST PAGE THAT TAKES A WHILE TO TYPE OUT"
        w.dlgBM[1] = "SECOND"
        w.dlgBQ = true; w.dlgBS = 30; w.dlgBR = 0; w.dlgBT = 0
    }

    @Test fun `the typewriter steps once per frame in the dialog block`() {
        val w = world()
        lineDialog(w)
        repeat(5) { w.tick(emptyList()) }
        assertEquals(5, w.dlgBR, "one bR++ per frame (k.java:947)")
        assertEquals((5 * 30) / 16, w.dlgBT, "bT = (bR*bS)/16")
        repeat(100) { w.tick(emptyList()) }
        assertEquals(-1, w.dlgBT, "past the page length → revealed")
        assertEquals(21, w.jC, "typing alone never advances the page")
    }

    @Test fun `the F() pass runs on dialog frames`() {
        val w = world()
        w.tick(emptyList())
        val p = w.player
        val e = Entity(30, w.clips[7]).apply {
            aw = 9_000; setAnim(0); setPositionPx(p.ak + 40, p.al)
        }
        e.refreshBoxes()
        w.npcs += e
        assertTrue(e.inPlayV(w), "precondition: in the view, so in the draw list")
        lineDialog(w)
        e.cGCount = 6
        w.tick(emptyList())
        assertEquals(5, e.cGCount, "F()'s cG flash steps behind the dialog too")
    }
}
