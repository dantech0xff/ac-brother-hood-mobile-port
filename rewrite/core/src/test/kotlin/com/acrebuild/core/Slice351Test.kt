package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 351 — no menu panel on the "GAME DATA HAS BEEN DELETED" screen.
 *
 * `ae()` (structured/k.java:6204-6228, proven) runs for jc23 and jc28:
 * `f(false)`, then the `eC==121` arm draws only the wrapped message at
 * y=120, handles BACK, draws the `a("", d(0,17))` footer and returns —
 * it never reaches `d(93,120,214)` (the panel) or `L(ey)` (the rows).
 * The renderer drew the panel for every `panelVisible` state, so the
 * wipe-confirm's YES/NO rows ghosted under the message (device Run-27,
 * `REPORT.md:1507-1508`). `menuPanelDrawn` now gates the panel.
 */
class Slice351Test {
    @Test fun `ae eC 121 shows message and footer but no panel`() {
        val w = world()
        w.stateL(28); w.kEc = 121
        assertTrue(w.panelVisible, "the message + footer block still runs")
        assertFalse(w.menuPanelDrawn, "no d(93,120,214) panel, no L(ey) rows")
        assertEquals("" to w.d0(17), w.menuFooter())
    }

    @Test fun `every other ae and panel state still draws its panel`() {
        val w = world()
        w.stateL(28); w.kEc = 69                      // the wipe question
        assertTrue(w.menuPanelDrawn)
        w.stateL(23); w.kEc = 19                      // the sound prompt
        assertTrue(w.menuPanelDrawn)
        for (s in intArrayOf(2, 3, 14, 19, 29, 30)) {
            w.stateL(s); w.kEc = 121                  // eC only matters in ae()
            assertEquals(w.panelVisible, w.menuPanelDrawn, "jc$s")
        }
    }

    @Test fun `confirming the wipe lands on the panel-less 121 screen`() {
        val w = world()
        w.stateL(28); w.kEc = 69; w.kFG = false
        w.kBw = 0                                     // YES row focused
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(28, w.jC)
        assertEquals(121, w.kEc, "69 arm: kFG false → eC = 121")
        assertFalse(w.menuPanelDrawn)
    }
}
