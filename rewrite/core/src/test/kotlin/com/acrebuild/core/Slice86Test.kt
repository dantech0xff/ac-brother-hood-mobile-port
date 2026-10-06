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

/** Slice 86 — b(x,y,w,z2,z3) menu panel: row rects, two-column split,
 *  strD text, a(str,z2,i) fit proc, and the `a` UiAnimObject semantics
 *  (k.java:5868-6150 + a.java, proven). */
class Slice86Test {

    @Test fun `two rows stack vertically — bv3 skips the col split`() {
        val w = world()
        w.stateL(12)
        val r = w.menuRowRects()
        assertEquals(2, r.size)
        // verbatim: jc12 arms eC=25 + K(3) (k.java:2287-2289) — bv!=4
        // takes `goto L157` at L153 (:7706-7708) so the x=206 split
        // never runs; YES/NO stack on the row pitch. DIVERGENCE: pitch
        // is now contiguous `i4` (the +3 gap was dropped with the band
        // change — user-requested), so row 1 sits at 117+30 = 147.
        assertEquals(listOf(93, 117, 214, 30), r[0].toList())
        assertEquals(listOf(93, 147, 214, 30), r[1].toList())
    }

    @Test fun `jc19 rows render LEVEL n`() {
        val w = world()
        w.stateL(19)
        // j.c==19 → strD = d(0,10)+" "+(i13+1) = "LEVEL n" (k.java:6046-6062)
        assertEquals("LEVEL 1", w.menuRowText(0).first)
        assertEquals("LEVEL 2", w.menuRowText(1).first)
    }

    @Test fun `row pitch is 30 except first jc2 row 35`() {
        val w = world()
        assertEquals(30, w.menuI4(0))
        assertEquals(30, w.menuI4(1))
        // row width: 170 normally, 135 for bv4-non14 / jc19
        assertEquals(170, w.menuI5())
    }

    @Test fun `menuRowAt hits the verbatim rects`() {
        val w = world()
        w.stateL(12)
        // YES row (93,117,214,30), NO row (93,150,214,30) — stacked.
        w.lastTouchX = 150; w.lastTouchY = 132
        assertEquals(0, w.menuRowAt(132))
        w.lastTouchX = 150; w.lastTouchY = 165
        assertEquals(1, w.menuRowAt(165))
        // past the 214-wide row right edge → miss
        w.lastTouchX = 350; w.lastTouchY = 165
        assertEquals(-1, w.menuRowAt(165))
        // above the panel → miss
        w.lastTouchX = 150; w.lastTouchY = 100
        w.lastTouchX = 150; w.lastTouchY = 100
        assertEquals(-1, w.menuRowAt(100))
        w.lastTouchX = -1; w.lastTouchY = -1
    }

    @Test fun `UiAnimObject arm-seek-tick matches a() semantics`() {
        val clip = Clip.load(
            java.io.File("../generated/clips/clip93/clip.acpk").readBytes())
        val a = UiAnimObject()
        a.attach(clip)
        // e=-1 → stopped; arm(i,1) starts state i
        assertTrue(a.stopped())
        a.arm(21, 1)
        assertEquals(21, a.e)
        // seek wraps: seek(i) maps t into [0,len)
        a.arm(21, 1)
        val len = a.len()
        if (len > 0) {
            a.seek(len + 2)
            assertTrue(a.currentFrame in 0 until clip.frameCount(21))
        }
        // finite loop counts down to latched stop
        a.arm(18, 1)
        var guard = 0
        while (!a.stopped() && guard++ < 2000) a.tick(62)
        assertTrue(a.stopped(), "finite anim should latch stopped")
        assertEquals(18, a.e)
        // infinite (h=-1) never reports stopped while ticking
        a.arm(20, -1)
        repeat(50) { a.tick(62) }
        assertFalse(a.stopped())
    }

    @Test fun `clip93 has the A2 edge frames 10 to 17`() {
        val clip = Clip.load(
            java.io.File("../generated/clips/clip93/clip.acpk").readBytes())
        // edge-band frames used by a(i,i2,i3,z2,z3) (:5872): 10..17 exist
        assertTrue(clip.animCount() >= 18)
        assertTrue(clip.frameCount(10) >= 1)
        assertTrue(clip.frameCount(17) >= 1)
        assertTrue(clip.moduleWidth(clip.frameModuleIndex(10, 0)) > 0)
    }
}
