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

class Slice112Test {

    private fun driveToCu5(w: Level0World) {
        w.stateL(20)
        while (w.kCu < 5) w.tick(emptyList())        // each cu arm to the cu4→5 edge
    }

    @Test fun `cu5 ticks the scroll panel — fd drifts at fe=-1`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        driveToCu5(w)
        assertEquals(5, w.kCu)
        assertEquals(w.kEz - 1, w.kFd, "transition a() ticks once: fe→-1 (:1273)")
        w.tick(emptyList())
        assertEquals(w.kEz - 2, w.kFd)
        w.tick(emptyList())
        assertEquals(w.kEz - 3, w.kFd, "fd += fe every frame (:5685)")
    }

    @Test fun `DOWN press accelerates the scroll and floors at -5`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        driveToCu5(w)
        val before = w.kFd
        w.pad.e(Pad.M_DOWN); w.tick(emptyList())
        assertEquals(-2, w.kFe); assertEquals(before - 2, w.kFd)
        repeat(4) { w.pad.e(Pad.M_DOWN); w.tick(emptyList()) }
        assertEquals(-5, w.kFe, "fe floors at -5 (:5632)")
    }

    @Test fun `UP press is a no-op for the unwrapped panel`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        driveToCu5(w)
        w.pad.e(Pad.M_UP); w.tick(emptyList())
        assertEquals(-1, w.kFe, "wrap=false forces fe<0 (:5652-5653)")
    }

    @Test fun `fd below the top edge wraps to 240 then keeps ticking`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        driveToCu5(w)
        w.kFd = -1000
        w.tick(emptyList())
        assertEquals(239, w.kFd, "fd=240 wrap-restart then fd+=fe (:5680-5685)")
    }

    @Test fun `l-20 resets the scroll state`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        driveToCu5(w)
        w.stateL(20)
        assertEquals(-1, w.kFd); assertEquals(0, w.kFe); assertEquals(0, w.kDw)
    }
}
