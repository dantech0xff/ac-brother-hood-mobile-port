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

class Slice174Test {

    // i.a(true,0) (i.java:19605-19616, proven): the trail cards arm with
    // e = S at arm time; cU[0] = entity pos, cU[1..4] parked.
    @Test fun `startTrail captures armed S and parks dots`() {
        val w = world()
        w.stateL(8)
        val p = w.player
        p.ak = 500; p.al = 300; p.setAnim(7)
        p.startTrail()
        assertTrue(p.hasTrail())
        assertEquals(7, p.trailAnim)
        val t = p.cU!!
        assertEquals(500, t[0]); assertEquals(300, t[1])
        for (i in 1..4) { assertEquals(-200, t[i * 2]); assertEquals(-120, t[i * 2 + 1]) }
        p.endTrail()
        assertFalse(p.hasTrail())
    }

    // i.af() (i.java:21513-21528, proven) + a.b(j.g) clock: ring-shift,
    // pos 0 = current, clock +1/push.
    @Test fun `pushTrail ring-shifts positions and ticks the card clock`() {
        val w = world()
        w.stateL(8)
        val p = w.player
        p.ak = 100; p.al = 50; p.setAnim(3)
        p.startTrail()
        p.ak = 110; p.pushTrail()
        p.ak = 120; p.pushTrail()
        val t = p.cU!!
        assertEquals(120, t[0]); assertEquals(110, t[2]); assertEquals(100, t[4])
        // parked dots still parked
        assertEquals(-200, t[8]); assertEquals(-120, t[9])
    }

    // a.f()/b(j.g) (a.java:60,112-142, proven): the card's frame index is
    // j.g wrapped mod total(Σdur*40), walking dur*40 thresholds — frame
    // advances when j.g crosses each threshold, wraps to 0 at j.g=total.
    @Test fun `trailFrame walks dur-times-40 thresholds and wraps`() {
        val w = world()
        w.stateL(8)
        val p = w.player
        p.setAnim(0)
        p.startTrail()
        val clip = p.clip!!
        val n = clip.frameCount(0)
        assertTrue(n > 0)
        assertEquals(0, p.trailFrame(0))
        // jg = dur0*40 - 1 -> still frame 0; jg = dur0*40 -> frame 1
        val d0 = clip.frameDuration(0, 0)
        if (d0 > 0 && n > 1) {
            assertEquals(0, p.trailFrame((d0 * 40 - 1).toLong()))
            assertEquals(1, p.trailFrame((d0 * 40).toLong()))
            // jg = total wraps back into range
            val total = (0 until n).sumOf { clip.frameDuration(0, it) } * 40
            if (total > 0) {
                assertEquals(0, p.trailFrame(total.toLong()))
            }
        }
    }

    // Re-arm while armed is a no-op (i.java:19606 `cU != null` early-out).
    @Test fun `startTrail while armed is a no-op`() {
        val w = world()
        w.stateL(8)
        val p = w.player
        p.ak = 42; p.al = 24; p.setAnim(5)
        p.startTrail()
        p.ak = 99; p.setAnim(8)
        p.startTrail()
        assertEquals(5, p.trailAnim)
        assertEquals(42, p.cU!![0])
    }
}
