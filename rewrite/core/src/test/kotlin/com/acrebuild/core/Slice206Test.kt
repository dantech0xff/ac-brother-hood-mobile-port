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

class Slice206Test {

    // -- k.bJ split-brain fix: producer (kBj) feeds consumer (flash arm) --

    @Test fun `grab lose latch drives the damage flash ramp`() {
        val w = world()
        w.kBj = 6                                // i.java:31851 grab-lose arm
        w.tick(emptyList())
        assertEquals(5, w.kBj, "I() ticks bJ-- once per tick")
        assertTrue(w.kDe, "de latches on while the flash runs")
        assertEquals((255 shl 24) or (75 shl 16) or (75 shl 8) or 75,
            w.kDf, "df = ARGB(255, 120·5/8, 120·5/8, 120·5/8)")
        w.tick(emptyList())
        assertEquals(4, w.kBj)
        assertEquals((255 shl 24) or (60 shl 16) or (60 shl 8) or 60,
            w.kDf, "the ramp decays with the counter")
    }

    @Test fun `flash stops when the latch empties`() {
        val w = world()
        w.kBj = 2
        repeat(3) { w.tick(emptyList()) }
        assertEquals(0, w.kBj)
        assertTrue(w.kDe, "de holds the last ramp value until f() clears")
    }

    @Test fun `idle latch leaves the flash dark`() {
        val w = world()
        assertEquals(0, w.kBj)
        w.tick(emptyList())
        assertEquals(-1, w.kDf, "no producer → df stays at its -1 init")
        assertFalse(w.kDe)
    }
}
