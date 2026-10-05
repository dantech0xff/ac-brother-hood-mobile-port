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

// --------------------------------------------------------------- slice 108
// jC==18 title tick + jc23 boot sound-prompt confirm (k.a() cases 18/23,
// k.java:1146-1175,1310-1324). Completes the screen-state coverage for the
// boot flow — reachable via stateL(23)'s confirm arm or the title itself.
class Slice108Test {

    @Test fun `jc18 context press latches cS then exits to main menu`() {
        val w = world()
        w.stateL(18)
        assertEquals(18, w.jC)
        w.pad.queuePress(Pad.M_CONTEXT)      // v(65568) edge (:1156)
        w.tick(emptyList())
        assertTrue(w.kCS, "press → cS")
        assertEquals(100, w.kCT)
        w.pad.queuePress(0)
        w.tick(emptyList())                  // cS arm: cT-=10 → l(2)
        assertEquals(2, w.jC, "cS → l(2) → main menu")
        assertEquals(0, w.kCT)
    }

    @Test fun `jc18 play-area tap also confirms via k-j pointerStrip`() {
        val w = world()
        w.stateL(18)
        // j() (k.java:523) reads k.H/k.I — the last pointer RELEASE
        // point — so drive it via an UP event inside the play strip.
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.UP, 200, 100)))
        assertTrue(w.kCS, "tap → cS")
    }

    @Test fun `jc23 sound prompt YES arms audio flags and lands on title`() {
        val w = world()
        w.stateL(23)                          // boot sound prompt (l(23) arm)
        assertEquals(19, w.kEc)
        w.kBw = 0                             // YES selected (eA[3][0]=14)
        w.kBE = false; w.kBF = false          // pre-clear to observe the set
        w.pad.queuePress(327712)              // v(327712) confirm (:1311)
        w.tick(emptyList())
        assertTrue(w.kBE && w.kBF, "bw==0 → bE=bF=true")
        assertEquals(18, w.jC, "confirm → l(18)")
    }

    @Test fun `jc23 NO clears both audio flags before l-18`() {
        val w = world()
        w.stateL(23)
        w.kBw = 1                             // NO selected (eA[3][1]=15)
        w.pad.queuePress(327712)
        w.tick(emptyList())
        assertFalse(w.kBE); assertFalse(w.kBF)
        assertEquals(18, w.jC)
    }

    @Test fun `jc23 without confirm keeps running ae`() {
        val w = world()
        w.stateL(23)
        w.pad.queuePress(0)
        w.tick(emptyList())
        assertEquals(23, w.jC, "no confirm → ae() only, stays on jc23")
    }

    @Test fun `jc23 YES row tap surfaces bw to the L102 else — audio on`() {
        val w = world()
        w.stateL(23)
        w.kBE = false; w.kBF = false          // pre-clear to observe the set
        // panel d(93,120,214): YES row at (93,130,214,30) — the b() loop's
        // per-row `c()` hit arms `bw=i13` + `E(32)` (k.java:7699-7704) and
        // YES/NO's action lives in the dispatch else (menuItem has no
        // kEc==19 arm), so the tap must reach the else as `bw`.
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 150, 145),
            InputQueue.Event(1, InputQueue.Type.UP, 150, 145)))
        assertTrue(w.kBE && w.kBF, "YES row tap → bw=0 → bE=bF=true")
        assertEquals(18, w.jC, "confirm → l(18)")
    }

    @Test fun `jc23 YES row tap spanning two ticks still confirms on release`() {
        val w = world()
        w.stateL(23)
        w.kBE = false; w.kBF = false
        // A tap that releases on a later tick than its DOWN: the arm
        // must hit-test the release point on the UP tick as well.
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 150, 145)))
        w.tick(listOf(InputQueue.Event(1, InputQueue.Type.UP, 150, 145)))
        assertTrue(w.kBE && w.kBF, "YES row release → bw=0 → bE=bF=true")
        assertEquals(18, w.jC)
    }

    @Test fun `jc23 YES NO rows stack vertically — tap NO picks NO`() {
        val w = world()
        w.stateL(23)
        w.kBE = true; w.kBF = true            // pre-set to observe the clear
        // bv=3 → no col split: YES (93,130,214,30), NO (93,163,214,30).
        // Tap center of NO → bw=1 → audio off.
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 178),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 178)))
        assertFalse(w.kBE); assertFalse(w.kBF, "NO row → bw=1 → audio off")
        assertEquals(18, w.jC)
    }
}
