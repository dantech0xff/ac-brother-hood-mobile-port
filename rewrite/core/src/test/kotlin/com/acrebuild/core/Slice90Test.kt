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

/** Slice 90 — `ae()` jc23/28 screen (k.java:6204-6228, proven). */
class Slice90Test {

    @Test fun `ae 121 arm routes back to state 3`() {
        val w = world()
        w.kEc = 121
        w.stateL(28)
        w.kEc = 121                               // stateL(28) doesn't touch eC
        assertEquals("" to w.d0(17), w.menuFooter())   // `a("",d(0,17))`
        w.pad.queuePress(Pad.M_CYCLE)
        w.tick(emptyList())
        assertEquals(3, w.jC)                     // l(3) — the wipe confirm
        assertEquals(3, w.kFo)                    // `fO=3` arm
        assertEquals(255, w.kFE)
        assertEquals(-1, w.kBw)
    }

    @Test fun `ae jc23 footer hides the right label`() {
        val w = world()
        w.stateL(23)
        w.tick(emptyList())
        // verbatim `(bv==0||j.c==23||j.c==13)?"":d(0,17)` — jc23 → ""
        assertEquals(w.d0(79) to "", w.menuFooter())
        assertEquals(listOf(93, 120, 214), w.menuPanelRect().toList())
    }

    @Test fun `ae jc28 footer shows back unless bv is 0`() {
        val w = world()
        w.kBv = 2
        w.stateL(28)
        w.tick(emptyList())
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
        w.kBv = 0
        assertEquals(w.d0(79) to "", w.menuFooter())
    }
}
