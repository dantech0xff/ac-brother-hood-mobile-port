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

// =========================================================================
// Slice 71 — k.l(13) win screen (k.java:2031 r6==13 → L12 → j.c=13).
// =========================================================================
class Slice71WinTest {
    @Test fun `screenL(13) freezes world and context edge reloads`() {
        val w = world()
        w.tick(emptyList())
        w.screenL(13)                                   // script op105 → k.l(13)
        assertTrue(w.won, "k.l(13) must raise the win screen")
        assertTrue(w.missionWon)
        val pos = w.player.ak to w.player.al
        repeat(5) { w.tick(emptyList()) }
        assertEquals(pos, w.player.ak to w.player.al, "world frozen while won")
        assertFalse(w.inPlay)
        // v(327712) confirm: press 1 → bw=0, press 2 → YES → a(true) reload
        w.pad.queuePress(Pad.M_CONTEXT); w.tick(emptyList())
        w.pad.queuePress(Pad.M_CONTEXT); w.tick(emptyList())
        assertFalse(w.won, "YES on the win dialog should advance")
        assertEquals(w.kAx, w.player.x1, "reload refills the meter: g.e(ax) (k.java:5225)")
    }

    @Test fun `screenL(13) is idempotent`() {
        val w = world()
        w.screenL(13); w.screenL(13)
        assertTrue(w.won); assertTrue(w.missionWon)
    }
}
