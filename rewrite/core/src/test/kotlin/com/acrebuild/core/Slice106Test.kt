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

class Slice106Test {
    @Test fun `ax9 record with r8-5==0 binds kAV`() {
        val w = world()
        assertNotNull(w.kAV, "uid-108 ax9 (fields[5]==0) must bind kAV")
        assertEquals(108, w.kAV!!.aw)
        assertEquals(6177, w.kAV!!.ak)
    }

    @Test fun `armed Z0 fires screenL-13 while the goal is ahead`() {
        val w = world()
        repeat(30) { w.tick(emptyList()) }          // intro settles
        val g = w.kAV!!
        assertFalse(w.won)
        g.Z[0] = 1                                  // arg-op (37,1,4) arm
        w.tick(emptyList())
        // goal 6177 sits >200px past the camera right edge → w()==2 →
        // bx=56; l(13); bw=0 (k.java:3346-3350, proven). l() remaps 13→31
        // for the milestone variant — both latch `won`.
        assertTrue(w.jC == 13 || w.jC == 31, "expected the win screen")
        assertEquals(56, w.kBx)
        assertEquals(0, w.kBw)
        assertTrue(w.won)
    }

    @Test fun `goal in the 200px band pulses goalTicker on even ticks`() {
        val w = world()
        repeat(30) { w.tick(emptyList()) }
        val g = w.kAV!!
        g.Z[0] = 1
        // w()==1 → goalTicker = j.f%2 — blink parity (k.java:3328, proven)
        var seen = mutableSetOf<Boolean>()
        for (i in 0 until 6) {
            g.ak = w.camRect[2] + 100               // re-anchor: band vs live camera
            w.tick(emptyList())
            seen += w.goalTicker
            if (w.won) break
        }
        assertFalse(w.won, "band case must not win")
        assertEquals(setOf(true, false), seen,
            "ticker must alternate with tickIndex parity")
    }
}
