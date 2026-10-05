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

// ---------------------------------------------------------------------------
// slice 95 — b(z2) tail: z[74] touch overlay + k.bJ damage flash (k.java:3142-3161, 2516-2526)
// ---------------------------------------------------------------------------
class Slice95Test {

    @Test fun `cm defaults to 1 - touch controls on (k java 159)`() {
        val w = world()
        assertEquals(1, w.cm)
        assertTrue(w.mounted, "k() = cm==1")
    }

    @Test fun `padCn is 5 normally and 50 under bh3`() {
        val w = world()
        assertEquals(5, w.padCn)
        w.kAj = 1
        assertTrue(w.bh3)
        assertEquals(50, w.padCn)
        w.kAj = 0
    }

    @Test fun `padPressed mirrors b(J,K) rect hit with -1 guard`() {
        val w = world()
        w.lastMoveX = -1; w.lastMoveY = -1
        assertFalse(w.padPressed(), "(-1,-1) is the no-touch sentinel")
        w.lastMoveX = 60; w.lastMoveY = 182          // inside (-5..111, 124..240)
        assertTrue(w.padPressed())
        w.lastMoveX = 300
        assertFalse(w.padPressed(), "x beyond cn-10+116")
        w.lastMoveX = 60; w.lastMoveY = 100
        assertFalse(w.padPressed(), "y above 124")
    }

    @Test fun `padZone classifies the 116x116 inner split mounted-style`() {
        val w = world()
        // top-left corner → cell0; center row split at x0/x1 halves → 3/5
        w.lastMoveX = 0; w.lastMoveY = 130
        assertEquals(0, w.padZone())
        w.lastMoveX = 45; w.lastMoveY = 182          // (x0, mid] → 3
        assertEquals(3, w.padZone())
        w.lastMoveX = 75; w.lastMoveY = 182          // (mid, x1) → 5
        assertEquals(5, w.padZone())
        w.lastMoveX = 55; w.lastMoveY = 130          // top-middle → 1
        assertEquals(1, w.padZone())
        w.lastMoveX = 55; w.lastMoveY = 235          // bottom-middle → 7
        assertEquals(7, w.padZone())
        // mounted mid-row only yields 3/5 — cell4 is unreachable (the
        // `iC==4 → i55=0` arm is dead in the original too, k.java:3148)
        w.lastMoveX = 40; w.lastMoveY = 182
        assertEquals(3, w.padZone())
    }

    @Test fun `padZoneFrame maps iC to the i55 object index`() {
        val w = world()
        assertEquals(0, w.padZoneFrame(-1))
        assertEquals(0, w.padZoneFrame(4))
        assertEquals(1, w.padZoneFrame(0))
        assertEquals(4, w.padZoneFrame(3))
        assertEquals(5, w.padZoneFrame(5))
        assertEquals(8, w.padZoneFrame(8))
    }

    @Test fun `padButton is the r35 circle at box center not the corner`() {
        val w = world()
        w.lastMoveX = 305; w.lastMoveY = 200          // center of (270,165) box
        assertTrue(w.padButton(270, 165))
        w.lastMoveX = 270; w.lastMoveY = 165          // corner: (35,35)≈49 > 35
        assertFalse(w.padButton(270, 165))
        w.lastMoveX = -1; w.lastMoveY = -1
        assertFalse(w.padButton(270, 165), "no-touch sentinel")
    }

    @Test fun `touchPadVisible honors k() and the screen gates`() {
        val w = world()
        assertTrue(w.touchPadVisible())
        w.cm = 0
        assertFalse(w.touchPadVisible(), "!k() hides the pad art")
        w.cm = 1; w.stateL(14)
        assertFalse(w.touchPadVisible(), "jc==14 hidden")
        w.stateL(5)
        assertFalse(w.touchPadVisible(), "jc==5 hidden")
        w.stateL(21); w.dlgU = 9
        assertFalse(w.touchPadVisible(), "jc21 u9 hidden")
        w.stateL(8); w.dlgU = 0
        assertTrue(w.touchPadVisible())
    }

    @Test fun `kBj flash decrements and ramps df until zero`() {
        val w = world()
        w.kBj = 6
        w.tick(emptyList())
        assertEquals(5, w.kBj)
        assertTrue(w.kDe)
        val c = (120 * 5) / 8                          // fp·bJ/8 (k.java:2522)
        assertEquals((255 shl 24) or (c shl 16) or (c shl 8) or c, w.kDf)
        var guard = 0                                  // l(21) dialogs eat ticks
        while (w.kBj > 0 && guard++ < 40) w.tick(emptyList())
        assertEquals(0, w.kBj)
        // f() reload clears de (k.java:5131) — driven via the fail path
        w.kBj = 6; w.kDe = true
        repeat(20) {
            w.player.applyHit(18, 0, null, w)
            w.player.gt = 0; w.iBh = 0
        }
        tickUntilFailed(w)                             // dialogs + the S50 anim
        assertTrue(w.failed)
        var g3 = 0                                     // j.t frame skip
        while (w.jT != 0 && g3++ < 10) w.tick(emptyList())
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 200, 130),
                      InputQueue.Event(1, InputQueue.Type.UP, 200, 130)))
        assertTrue(w.kDe, "de survives a(z2) — only W() clears it (k.java:5131)")
    }

    @Test fun `resolvePadZone mounted arm - circles then pad box`() {
        val w = world()
        w.stateL(8)
        assertEquals(4, w.resolvePadZone(305, 200))   // button A circle
        assertEquals(1, w.resolvePadZone(355, 145))   // button B circle
        assertEquals(3, w.resolvePadZone(45, 182))    // pad box left half
        assertEquals(5, w.resolvePadZone(55, 182))    // mid-split → 5 (4 unreachable)
        assertEquals(-1, w.resolvePadZone(250, 60))   // outside everything
    }
}
