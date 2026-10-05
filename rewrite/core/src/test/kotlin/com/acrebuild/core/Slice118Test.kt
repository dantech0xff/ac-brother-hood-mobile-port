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

// =========================================================================
//  Slice 118 — jc6 ABOUT screen + bU[77] $VVV splice + dead states
//  7/32/33/34 (k.java:844-858, 3988-3991, 796-1450)
// =========================================================================
class Slice118Test {

    // -- bU[77] $VVV splice (k.java:3988-3991 + MANIFEST.MF, proven) --------

    @Test fun `d0(77) carries the credits roll`() {
        val w = world()
        val s = w.d0(77)
        assertNotNull(s)
        assertTrue(s!!.contains("ASSASSIN'S CREED BROTHERHOOD"))
        assertTrue(s.contains("GAMELOFT"))
        assertTrue(s.contains("EXECUTIVE PRODUCERS"))
    }

    @Test fun `d0(77) splices MIDlet-Version 1_2_7`() {
        val w = world()
        assertTrue(w.d0(77)!!.contains("V 1.2.7"), "\$VVV → 1.2.7")
        assertFalse(w.d0(77)!!.contains("\$VVV"), "placeholder consumed")
    }

    // -- jc6 ABOUT screen (k.java:844-858, proven) ---------------------------

    @Test fun `jc6 CYCLE exits to jc3 with sound (non-dx)`() {
        val w = world()
        w.stateL(6)
        assertEquals(6, w.jC)
        w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); w.pad.releaseFlush()
        assertEquals(3, w.jC, "v(131072) && !dx → l(3)")
        assertEquals(30, w.audioTrack, "z(30)")
    }

    @Test fun `jc6 CYCLE does NOT exit on the dx variant`() {
        val w = world()
        w.kDx = true
        w.stateL(6)
        w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); w.pad.releaseFlush()
        assertEquals(6, w.jC, "dx variant swallows the CYCLE")
    }

    @Test fun `jc6 BACK footer only on the non-dx variant`() {
        val w = world()
        w.stateL(6)
        assertEquals(Pair("", w.d0(17)), w.menuFooter())
        w.kDx = true
        assertEquals(Pair<String?, String?>(null, null), w.menuFooter())
    }

    // -- dead states 7/32/33/34 (a() has no case, :796-1450) ----------------

    @Test fun `dead screens consume ticks verbatim`() {
        for (st in intArrayOf(7, 32, 33, 34)) {
            val w = world()
            w.stateL(st)
            repeat(3) { w.tick(emptyList()) }
            assertEquals(st, w.jC, "j.c=$st holds — no a() case")
        }
    }
}
