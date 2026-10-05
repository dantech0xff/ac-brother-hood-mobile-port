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

class Slice111Test {

    @Test fun `jc9 holds until load counter passes 164`() {
        val w = world()
        w.stateL(9)
        assertEquals(9, w.jC)
        // release the 65568 bit too early — j.g<=164 keeps loading
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); w.pad.releaseFlush()
        w.tick(emptyList())
        assertEquals(9, w.jC, "j.g=2 — still loading, release ignored")
        repeat(163) { w.tick(emptyList()) }          // j.g → 165
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); w.pad.releaseFlush()
        w.tick(emptyList())                          // eM=65568 → w() → l(8)
        assertEquals(8, w.jC, "load done + context release → play")
    }

    @Test fun `jc9 restores mission state from save bytes on entry to play`() {
        val w = world()
        w.kDB = 30; w.kDC = 44; w.kDF = 7            // dB/dC/dF save bytes
        w.stateL(9)
        repeat(165) { w.tick(emptyList()) }          // j.g → 165
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); w.pad.releaseFlush()
        w.tick(emptyList())
        assertEquals(8, w.jC)
        assertEquals(30, w.kAx); assertEquals(44, w.kAy); assertEquals(7, w.kAN,
            "ax=dB; ay=dC; aN=dF (:1075-1077)")
        assertEquals(120, w.kDz); assertEquals(0, w.kAw)
        // l(8) fires z(23), then the play-entry tail `bG>=0 → B()`
        // (k.java:5229) starts mission music ee[aj]=5 — audioTrack is
        // the LAST z() issued.
        assertEquals(w.kEE[w.kAj], w.audioTrack,
            "bG>=0 → B() starts ee[aj] on play entry")
    }

    @Test fun `jc9 play-strip tap also enters play`() {
        val w = world()
        w.stateL(9)
        repeat(165) { w.tick(emptyList()) }
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.UP, 200, 100)))
        assertEquals(8, w.jC, "j() tap → l(8)")
    }
}
