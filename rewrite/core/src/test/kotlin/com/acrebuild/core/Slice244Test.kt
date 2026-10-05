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

class Slice244Test {
    // slice 244 — ax9 candle S5 fade: F() owns `aC--`/`paletteAlpha`/
    // `ak,al` snap/`P|64` (i.java:3095); the renderer consumes
    // `e.paletteAlpha` (double-decrement fix).

    private fun candle(w: Level0World): Entity {
        val e = Entity(9, null)
        e.S = 5
        e.Z[2] = 47                     // candle flag
        e.P = 64                        // burn started (anim latch)
        e.aC = 5
        w.kBK = true
        w.npcs.add(e)
        return e
    }

    @Test fun `drawStyleF owns the fade - alpha write + one aC decrement`() {
        val w = world()
        val e = candle(w)
        e.drawStyleF(w)
        assertEquals(127, e.paletteAlpha, "aC*255/10 = 5*25 = 127")
        assertEquals(4, e.aC, "exactly one decrement per F() call")
        e.drawStyleF(w)
        assertEquals(3, e.aC, "second call → 3 (no renderer-side tick)")
        assertEquals(102, e.paletteAlpha)
    }

    @Test fun `aC hits zero - entity removed + alpha restored`() {
        val w = world()
        val e = candle(w)
        e.aC = 1
        e.drawStyleF(w)
        assertEquals(0, e.aC)
        assertEquals(255, e.paletteAlpha, "tail restores full alpha")
        assertTrue(e in w.pendingRemove, "fade-out → k.c(this) queue")
    }

    @Test fun `unlit candle keeps alpha 255`() {
        val w = world()
        val e = candle(w)
        e.aC = 0
        e.drawStyleF(w)
        assertEquals(255, e.paletteAlpha)
        assertEquals(0, e.aC)
    }
}
