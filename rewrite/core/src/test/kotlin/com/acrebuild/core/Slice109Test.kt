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

// --------------------------------------------------------------- slice 109
// jC==0 boot R() driver — the `cu` sub-phase FSM (k.R(), k.java:3949-4100).
// Splash dwells (2×3000ms, pause-skippable), load frames, then the 5000ms
// loading screen → `l(23)`. Timers map wall-clock onto jG ticks (49/81).
class Slice109Test {

    @Test fun `boot advances cu through load frames then logo dwell`() {
        val w = world()
        w.stateL(0)
        assertEquals(0, w.jC); assertEquals(0, w.kCu)
        w.tick(emptyList())                  // cu0 → cu1 (du arm)
        assertEquals(1, w.kCu)
        w.tick(emptyList())                  // cu1 → cu2 (asset load)
        assertEquals(2, w.kCu)
    }

    @Test fun `logo dwell holds for 49 ticks then advances`() {
        val w = world()
        w.stateL(0)
        w.tick(emptyList()); w.tick(emptyList())      // now cu2, du=jG
        repeat(47) { w.tick(emptyList()) }            // elapsed 2..48
        assertEquals(2, w.kCu, "elapsed <49 ticks < 3000ms — dwelling")
        w.tick(emptyList())                           // elapsed hits 49
        assertEquals(3, w.kCu, "elapsed >=49 ticks >= 3000ms → cu3")
    }

    @Test fun `pause edge does NOT skip the logo dwells — unskippable`() {
        // proven (k.java:4003-4054): R() cases 2/3 advance only on the
        // 3000ms `du` timer — no input check exists in any arm. The
        // port's earlier pause-skip was a non-verbatim nicety, removed.
        val w = world()
        w.stateL(0)
        w.tick(emptyList()); w.tick(emptyList())      // cu2
        w.pad.queuePress(Pad.M_PAUSE)
        w.tick(emptyList())
        assertEquals(2, w.kCu, "splash dwell ignores pause")
        repeat(49) { w.tick(emptyList()) }            // ~3000ms elapse
        assertEquals(3, w.kCu, "timer alone advances the dwell")
    }

    @Test fun `loading screen ignores pause then lands on jc23 prompt`() {
        val w = world()
        w.stateL(0)
        w.tick(emptyList()); w.tick(emptyList())      // cu2
        repeat(49) { w.tick(emptyList()) }            // cu2 → cu3
        repeat(49) { w.tick(emptyList()) }            // cu3 → cu4
        w.tick(emptyList()); w.tick(emptyList())      // cu4→5→6 (5 runs 6's check)
        assertEquals(6, w.kCu)
        w.pad.queuePress(Pad.M_PAUSE)                 // no skip on cu6
        w.tick(emptyList())
        assertEquals(6, w.kCu, "case 6 has no pause skip")
        repeat(76) { w.tick(emptyList()) }            // elapsed ..80
        assertEquals(6, w.kCu, "elapsed <81 ticks < 5000ms")
        w.tick(emptyList())                           // elapsed hits 81
        assertEquals(23, w.jC, "l(23) — sound prompt")
        assertEquals(19, w.kEc)
        assertEquals(0, w.kCu, "l() resets cu — proven k.java:1644")
    }

    @Test fun `full boot chain reaches title and main menu`() {
        val w = world()
        w.stateL(0)
        repeat(180) { w.tick(emptyList()) }           // boot: 2+48+48+2+80 ticks
        assertEquals(23, w.jC)
        w.kBw = 0                                     // YES
        w.pad.queuePress(327712)
        w.tick(emptyList())
        assertEquals(18, w.jC, "confirm → l(18) title")
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertTrue(w.kCS)
        w.tick(emptyList())
        assertEquals(2, w.jC, "title → main menu")
    }
}
