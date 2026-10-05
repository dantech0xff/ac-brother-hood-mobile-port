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

class Slice98Test {
    private fun tickClean(w: Level0World, n: Int) {
        repeat(n) {
            w.kC = null; w.pendingInsert.clear()
            w.player.al = w.camY + 100
            w.tick(emptyList())
        }
    }

    @Test fun `fadeOut ramps nine stripes then one black frame`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.fadeOut()                                       // k.B(26)
        assertTrue(w.kAn); assertFalse(w.kAo)
        assertEquals(0, w.kBI); assertEquals(26, w.kFk)
        tickClean(w, 9)
        assertEquals(234, w.kBI, "bI += 26 x9 = 234")
        assertEquals(9, w.kFn, "fn grows one stripe per in-ramp step")
        assertTrue(w.kAn, "still fading — 234 <= 255-26 is false next tick")
        tickClean(w, 1)
        assertFalse(w.kAn, "bI>255-fk → an=false (k.java:3171)")
        assertTrue(w.fadeSolidFrame, "single solid-black frame armed")
        tickClean(w, 1)
        assertFalse(w.fadeSolidFrame, "for one tick (slice 381)")
    }

    @Test fun `fadeIn drains bI and recedes stripes`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.kFn = 9
        w.fadeIn()                                        // k.C(26)
        assertTrue(w.kAo); assertFalse(w.kAn)
        assertEquals(255, w.kBI)
        tickClean(w, 1)
        assertEquals(229, w.kBI); assertEquals(8, w.kFn)
        tickClean(w, 8)
        assertEquals(21, w.kBI); assertEquals(0, w.kFn)
        tickClean(w, 1)
        assertFalse(w.kAo, "bI<fk → ao=false (k.java:3185)")
        assertEquals(0, w.kFn, "fn untouched once ao clears")
    }

    @Test fun `fadeIn during fadeOut swaps arms`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.fadeOut()
        w.fadeIn()
        assertTrue(w.kAo); assertFalse(w.kAn); assertEquals(255, w.kBI)
    }

    @Test fun `vignette fs wraps at zero`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.iBh = 20                            // outlasts fs's 8-tick cycle
        tickClean(w, 1)
        assertEquals(70, w.kFs, "fs -= 10 (k.java:3193)")
        tickClean(w, 7)
        assertEquals(80, w.kFs, "fs<=0 → wrap to 80 (:3196)")
    }

    @Test fun `vignette gated off play state`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.iBh = 8; w.stateL(12)
        // l(12)'s b(true) (k.java:1657) runs before `j.c = 12` is
        // committed, so its tail still sees j.c==8 and steps fs once
        // (:3190-3196, slice 376)
        assertEquals(70, w.kFs, "the transition's b(true) steps fs")
        tickClean(w, 1)
        assertEquals(70, w.kFs, "j.c!=8 → fs frozen")
    }

    @Test fun `letterbox dz opens via av and chases aw`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.kDz = 0; w.kAv = true
        tickClean(w, 2)
        assertEquals(40, w.kDz, "av → dz += 20/tick (:3207)")
        w.kAv = false; w.kAw = 0
        tickClean(w, 2)
        assertEquals(0, w.kDz, "dz chases aw by -20")
        w.kAw = 55; w.kDz = 60
        tickClean(w, 1)
        assertEquals(55, w.kDz, "|diff|<20 → snap dz=aw")
        w.stateL(14)
        w.kAw = 0; w.kDz = 55
        tickClean(w, 1)
        assertEquals(55, w.kDz, "j.c==14 → letterbox frozen")
    }

    @Test fun `i-bJ flicker toggles bH to bI then zeroes with skip`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.iBJ = 1                                          // == i.bH
        tickClean(w, 1)
        assertEquals(2, w.iBJ, "bJ==bH && bL>=0 → bJ=bI (k.java:3227)")
        assertFalse(w.tailSkipFrame)
        tickClean(w, 1)
        assertEquals(0, w.iBJ, "bJ==bI && bL<=20 → bJ=0 (:3234)")
        assertTrue(w.tailSkipFrame, "early-return frame skips aU bar")
        tickClean(w, 1)
        assertFalse(w.tailSkipFrame, "latch is per-frame")
    }

    @Test fun `i-bJ bL negative holds bH`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        w.iBJ = 1; w.iBL = -5
        tickClean(w, 3)
        assertEquals(1, w.iBJ, "bL<0 → bH arm not taken")
    }

    @Test fun `kAU meter gate fields`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val q = Entity(61, w.clips[7]); w.kAU = q; w.iBy = 1
        q.P = q.P or 32
        // renderer gate `(P&32)==0 && iBy>0 && !tailSkipFrame` — world side
        assertEquals(32, q.P and 32, "P&32 clears the bar draw")
        q.P = q.P and 32.inv()
        assertEquals(0, q.P and 32)
        assertEquals(1, w.iBy)
    }
}
