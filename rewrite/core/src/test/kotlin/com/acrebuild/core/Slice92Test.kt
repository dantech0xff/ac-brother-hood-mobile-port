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

private fun charmapFont(): FontClip =
    FontClip(Clip.load(asset("clips/clip92/clip.acpk")),
             FontClip.loadCharmap(charmap()), 4)

/** Slice 92 — `G()` jc5 help/instructions scroller (k.java:2412-2488,
 *  proven) + `b.a(String,w,z)` wrap (b.java:1618-1760, proven). */
class Slice92Test {

    @Test fun `G init builds 4 pages from strings 47-50`() {
        val w = world(charmap = charmap())
        w.stateL(5)
        assertNotNull(w.kCV[0])
        assertNotNull(w.kCV[3])
        assertTrue(w.kCZ > 0)                       // total screens > 0
        assertEquals(1, w.kCY)
        assertEquals(37 + w.kEe, w.kEf)             // `eF = 37 + eE`
        // page 1 carries the cW-padded suffix (verbatim)
        assertTrue(w.kCV[1]?.startsWith("\n\n") == true)
    }

    @Test fun `G right advances screens then wraps the page`() {
        val w = world(charmap = charmap())
        w.stateL(5)
        // a page with >1 screens (cX[bw] > 1) advances cY
        w.kCX[0] = 3
        w.pad.queuePress(Pad.M_RIGHT)
        w.tick(emptyList())
        assertEquals(2, w.kCY)
        assertEquals(0, w.kBw)
        w.pad.queuePress(Pad.M_RIGHT); w.tick(emptyList())
        w.pad.queuePress(Pad.M_RIGHT); w.tick(emptyList())
        // cY hit cX[0] → cY=1, bw=(0+1)%4=1
        assertEquals(1, w.kCY)
        assertEquals(1, w.kBw)
    }

    @Test fun `G left at screen 1 wraps to the previous page end`() {
        val w = world(charmap = charmap())
        w.stateL(5)
        w.kCX[3] = 2                                // page 3 has 2 screens
        w.pad.queuePress(Pad.M_LEFT)
        w.tick(emptyList())
        assertEquals(3, w.kBw)                      // (0-1+4)%4
        assertEquals(2, w.kCY)                      // cY = cX[bw]
    }

    @Test fun `G chevron taps inject the nav masks`() {
        val w = world(charmap = charmap())
        w.stateL(5)
        w.kCX[0] = 2
        val iK = w.menuGIK()
        // right chevron box (305, iK-15, 50, 30) — the injected E()
        // mask commits one frame later (deferred vs orig, inferred)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 320, iK),
                      InputQueue.Event(0, InputQueue.Type.UP, 320, iK)))
        w.tick(emptyList())
        assertEquals(2, w.kCY)                      // c() → E(8256)
    }

    @Test fun `G back returns to cy`() {
        val w = world(charmap = charmap())
        w.stateL(8)
        w.stateL(5)                                 // cy = 8
        w.pad.queuePress(Pad.M_CYCLE)
        w.tick(emptyList())
        assertEquals(8, w.jC)                       // `l(cy)` + `cY=1`
        assertEquals(1, w.kCY)
    }

    @Test fun `G footer is empty-left plus BACK`() {
        val w = world()
        w.stateL(5)
        assertEquals("" to w.d0(17), w.menuFooter())
    }

    @Test fun `wrap produces the U table layout`() {
        val f = charmapFont()
        // a long line wraps into {count, end0, w0, end1, w1, ...}
        val u = f.wrap("AAAA BBBB CCCC DDDD", 40)
        assertTrue(u[0] >= 2)                       // ≥2 lines
        // U layout: U[0]=count, pairs {end,width} at U[1],U[2],U[3],U[4]
        val line0End = u[1]; val line0w = u[2]
        assertTrue(line0End in 1..("AAAA BBBB CCCC DDDD").length)
        assertTrue(line0w in 1..40)                 // width ≤ wrap bound
    }

    @Test fun `linesHeight matches k(i) semantics`() {
        val f = charmapFont()
        assertEquals(f.baseJ, f.linesHeight(1))
        assertEquals(2 * f.baseJ + f.baseK, f.linesHeight(2))
    }
}
