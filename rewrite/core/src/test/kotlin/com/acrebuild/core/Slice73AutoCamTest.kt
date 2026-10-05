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
// Slice 73 — k.D() bh3 autoscroll camera (k.java:2721-2860) + i.X() at :3363.
// =========================================================================
class Slice73AutoCamTest {
    @Test fun `bh3 tick runs D() autoscroll not m(1)`() {
        val w = world()
        w.kAj = 1                                   // MISSION_BH[1]==3
        val p = w.player
        p.setPositionPx(1500, 900); p.refreshBoxes()
        val x0 = w.camX; val y0 = w.camY
        repeat(40) { w.tick(emptyList()) }
        // D() lerps camX toward the corridor target and camY +30/tick-cap —
        // either axis must move (m() path is skipped entirely on bh3).
        assertTrue(w.camX != x0 || w.camY != y0,
            "bh3 camera should drive via D(), got cam=${w.camX},${w.camY}")
    }

    @Test fun `wind W drains into X once then stays`() {
        val w = world()
        w.kAj = 1
        w.kW = 5
        w.tick(emptyList())
        assertEquals(5, w.kX)
        assertEquals(0, w.kW)
        w.tick(emptyList())
        assertEquals(5, w.kX, "X is a sticky counter — no re-drain")
    }

    @Test fun `iBW phase write persists snapshot and stays armed`() {
        // i.bW is never cleared (k.java:3365, proven): `if (bW) X()` re-fires
        // every camera tick while armed — the phase checkpoint re-stamps
        // continuously until class-init/reset.
        val w = world()
        w.kAj = 1
        w.kAp[0] = 7; w.kAp[3] = 2
        w.iBW = true
        w.tick(emptyList())
        assertTrue(w.iBW, "pending write stays armed (k.java:3365)")
        val s = w.checkpointSnap!!
        assertEquals(w.player.ak, s.ak); assertEquals(w.player.al, s.al)
        assertEquals(7, s.ap[0]); assertEquals(2, s.ap[3])
    }

    @Test fun `dialog modal snaps camera and returns early`() {
        val w = world()
        w.kAj = 1
        w.autoDismissDialog = false   // keep the modal armed this tick
        w.screenL(21)                 // j.c=21 → dialogModal armed
        val x0 = w.camX; val y0 = w.camY
        w.tick(emptyList())
        assertEquals(x0, w.camX); assertEquals(y0, w.camY)
    }
}
