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

class Slice113Test {

    @Test fun `jc24 iris then credits scroll then NEXT-skip to l-25`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        w.kDz = 0                                    // M()'s l(24) caller arms dz=0 (:3433)
        w.stateL(24)
        assertEquals(24, w.jC)
        repeat(6) { w.tick(emptyList()) }            // iris: dz 0→120 at +20/frame
        assertEquals(120, w.kDz)
        assertTrue(w.kDy!!.startsWith("THE END"), "dy = d(0,28)+\\n*11+d(0,55)")
        assertTrue(w.kDy!!.endsWith("EVERY MOVEMENT..."))
        assertEquals(110, w.kFd, "fd armed 110 during iris")
        w.tick(emptyList()); assertEquals(1, w.kDw)  // dw=0 → dw=1
        w.tick(emptyList()); assertEquals(2, w.kDw)
        repeat(20) { w.tick(emptyList()) }
        assertTrue(w.kDw >= 21, "title slide (1-10) + hold (11-20) done")
        val fd0 = w.kFd
        w.tick(emptyList())
        assertTrue(w.kFd < fd0, "credits scroll: fd drifts up at fe=-1")
        w.pad.e(Pad.M_CYCLE); w.tick(emptyList())
        assertEquals(160, w.kDw, "v(131072) skip → dw=160")
        assertEquals(23, w.audioTrack, "z(23) on skip")
        repeat(14) { w.tick(emptyList()) }           // dw += 20 → >415
        assertEquals(25, w.jC, "dw>415 → dy=null; dz=0; l(25)")
    }

    @Test fun `jc25 first entry latches dx and redirects to l-6`() {
        val w = world()
        w.kDx = false
        w.stateL(25)
        w.tick(emptyList())
        assertTrue(w.kDx, "dx=true latched")
        assertEquals(6, w.jC, "l(6) redirect (:1392)")
        assertEquals(0, w.audioTrack, "z(0) on redirect")
    }

    @Test fun `jc25 re-entry with dx returns to main menu`() {
        val w = world()
        w.kDx = true
        w.stateL(25)
        w.tick(emptyList())
        assertEquals(2, w.jC, "!Z() → l(2) — Z() is the IGP check, false here")
    }

    @Test fun `jc24 footer arms the SKIP pill only`() {
        val w = world()
        w.kDz = 0; w.stateL(24)
        repeat(7) { w.tick(emptyList()) }            // iris done + dw=1
        val f = w.menuFooter()
        assertNull(f.first); assertEquals("SKIP", f.second, "a(null,d(0,18))")
    }
}
