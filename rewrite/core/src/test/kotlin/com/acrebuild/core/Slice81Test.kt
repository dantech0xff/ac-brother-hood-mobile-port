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

// ---------------------------------------------------------------------------
// slice 81 — M() stats-screen surface (row strings + fmtJ + mm:ss)
// ---------------------------------------------------------------------------
class Slice81Test {

    private fun openStats(w: Level0World): Level0World {
        w.kBA[15] = 1
        w.screenL(15)
        return w
    }

    @Test fun `row strings fill as each jG gate passes`() {
        val w = openStats(world())
        w.kAu = 0
        w.kAp[0] = 9; w.kAp[3] = 4; w.kAp[1] = 6; w.kAp[5] = 2
        w.kDg = 16 * 195                              // 3:15
        repeat(26) { w.tick(emptyList()) }
        assertEquals("9", w.statsRowText[0])
        assertEquals("4", w.statsRowText[1])
        assertEquals("6", w.statsRowText[2], "row draws raw deaths (unclamped)")
        assertEquals("2", w.statsRowText[3])
        assertEquals("3:15", w.statsRowText[4])
    }

    @Test fun `time row zero-pads seconds and abs-mod`() {
        val w = openStats(world())
        w.kDg = 16 * 65                               // 1:05
        repeat(26) { w.tick(emptyList()) }
        assertEquals("1:05", w.statsRowText[4])
    }

    @Test fun `rows hidden until their gate passes`() {
        val w = openStats(world())
        repeat(20) { w.tick(emptyList()) }            // jG=7: gates >0,>2,>4,>6
        assertTrue(w.statsRowText[0].isNotEmpty())
        assertTrue(w.statsRowText[1].isNotEmpty())
        assertTrue(w.statsRowText[2].isNotEmpty())
        assertTrue(w.statsRowText[3].isNotEmpty())
        assertEquals("", w.statsRowText[4], "time row needs jG>8")
        assertFalse(w.statsScoreVisible)
    }

    @Test fun `fmtJ groups thousands with comma`() {
        val w = openStats(world())
        assertEquals("999", w.fmtJ(999))
        assertEquals("1,234", w.fmtJ(1234))
        assertEquals("1,234,567", w.fmtJ(1234567))
    }

    @Test fun `negative bonus clamps to zero in row and score`() {
        val w = openStats(world())
        w.kAp[5] = -7
        repeat(26) { w.tick(emptyList()) }
        assertEquals("0", w.statsRowText[3])
        assertEquals(0, w.statsScore)
    }
}
