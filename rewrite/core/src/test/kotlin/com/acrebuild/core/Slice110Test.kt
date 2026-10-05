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

class Slice110Test {

    @Test fun `l-20 wraps the story text and resets cu`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        w.stateL(20)
        assertEquals(20, w.jC); assertEquals(0, w.kCu, "l() resets cu")
        assertTrue(w.kFb.startsWith("IN AN ATTACK"), "fb = wrapped d(0,27)")
        assertTrue(w.kFb.contains('\n'), "wrapJoin inserts soft breaks")
        assertTrue(w.kEz <= 85, "eZ = 85 or shifted up for tall text")
    }

    @Test fun `jc20 types the text then NEXT exits to l-9`() {
        val w = world(charmap = asset("fonts/charmap.bin"))
        w.stateL(20)
        w.tick(emptyList())                          // cu0 → cu1 (cT=10)
        assertEquals(1, w.kCu)
        repeat(25) { w.tick(emptyList()) }           // cT +10 → ≥255 → cu2
        assertEquals(2, w.kCu)
        assertEquals(200, w.kEY); assertEquals(0, w.kFc)
        w.tick(emptyList())                          // typewriter 1 char
        assertTrue(w.kFa.isNotEmpty() && w.kFc > 0, "fc typewriter grows fa")
        repeat(w.kFb.length + 5) { w.tick(emptyList()) }
        assertEquals(3, w.kCu, "fc >= len-1 → cu3")
        repeat(26) { w.tick(emptyList()) }           // eY -=4 → 100 → cu4
        assertEquals(4, w.kCu); assertEquals(100, w.kEY)
        while (w.kCu < 5) w.tick(emptyList())        // cT→255 → cu5 (fd=eZ-1)
        assertEquals(5, w.kCu); assertEquals(w.kEz - 1, w.kFd)
        w.pad.queuePress(Pad.M_CYCLE)                // NEXT v(131072) → l(9)
        w.tick(emptyList())
        assertEquals(9, w.jC)
        assertEquals(23, w.audioTrack, "z(23) on exit")
    }

    @Test fun `jc20 pause skip fast-forwards each phase`() {
        val w = world()
        w.stateL(20)
        w.tick(emptyList())                          // cu1
        w.pad.queuePress(Pad.M_PAUSE); w.tick(emptyList())  // cu1 → cu2
        assertEquals(2, w.kCu)
        w.pad.queuePress(Pad.M_PAUSE); w.tick(emptyList())  // cu2 → cu3
        assertEquals(3, w.kCu)
        w.pad.queuePress(Pad.M_PAUSE); w.tick(emptyList())  // cu3 → cu4
        assertEquals(4, w.kCu)
        // cu4 pause → cu5 + tail check (v(262144)&&cu==5) exits same tick
        w.pad.queuePress(Pad.M_PAUSE); w.tick(emptyList())
        assertEquals(9, w.jC, "cu4→5 + same-frame tail → l(9)")
    }

    @Test fun `jc20 NEXT at any phase exits to load screen`() {
        val w = world()
        w.stateL(20)
        w.tick(emptyList())                          // cu1
        w.pad.queuePress(Pad.M_CYCLE)                // v(131072) — NEXT
        w.tick(emptyList())
        assertEquals(9, w.jC)
    }
}
