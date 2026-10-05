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

class Slice211Test {

    /** k.java:5180 — pause entry `i==14` runs `if (!e.a()) k.fi = -1`:
     *  the music slot survives for RESUME only while a track is live. */
    @Test fun `pause entry keeps the music slot while a track plays`() {
        val w = world()
        w.sfx(1)                                   // live track → e.a() true
        w.kFi = 1
        w.stateL(14)
        assertEquals(1, w.kFi, "live track → fi kept for RESUME replay")
    }

    @Test fun `pause entry silences the music slot when no track plays`() {
        val w = world()
        w.kFi = 9
        w.stateL(14)
        assertEquals(-1, w.kFi, "no live track → fi = -1")
    }
}
