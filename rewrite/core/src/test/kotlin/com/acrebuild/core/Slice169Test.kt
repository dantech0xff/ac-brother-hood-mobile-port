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

class Slice169Test {
    /** Slice 169 (proven, g.java L1ab3-L1c33): S33 wall-rebound arms —
     *  dirHeld && still-rising (ah<0) arms the lip-scan (ct=true, no
     *  probe/kick); ANY other case probes -> fall-or-kick. The old port
     *  had the two branches inverted: held-right during the rise forced
     *  the kick/fall probe and froze S33 when falling with no dir hold. */
    @Test fun `S33 dirHeld while rising arms lip scan not kick`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val p = w.player
        // plateau edge: x791,y864 (pit lip ahead, wall column at x840)
        p.ak = 791; p.al = 864; p.N = p.ak shl 8; p.O = p.al shl 8
        p.setAnim(33); p.ag = 0; p.ah = -4096
        w.tick(listOf(InputQueue.Event(7, InputQueue.Type.DOWN, 95, 180)))
        assertEquals(33, p.S, "still rebounding while rising")
        assertTrue(p.ct, "ct armed by the dirHeld+rise lip-scan branch")
    }

    @Test fun `S33 dirHeld while falling probes kick or fall`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val p = w.player
        p.ak = 791; p.al = 864; p.N = p.ak shl 8; p.O = p.al shl 8
        p.setAnim(33); p.ag = 0; p.ah = 256
        w.tick(listOf(InputQueue.Event(7, InputQueue.Type.DOWN, 95, 180)))
        assertTrue(p.S != 33 || p.ct.not(),
            "falling+held probes: left S33 for fall/kick (S=" + p.S + ")")
    }

    @Test fun `S33 no hold falling still resolves`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val p = w.player
        p.ak = 791; p.al = 864; p.N = p.ak shl 8; p.O = p.al shl 8
        p.setAnim(33); p.ag = 0; p.ah = 256
        w.tick(listOf())
        assertTrue(p.S != 33, "no-hold falling still probes out of S33 (S=" + p.S + ")")
    }
}
