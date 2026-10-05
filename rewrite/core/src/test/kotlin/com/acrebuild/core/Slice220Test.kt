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

// ============================================================ slice 220 —
// k.b(boolean) (k.java:9062-9340): the input-lock veil latch +
// the visible et-cell window (dN..dQ/dM bookkeeping kept verbatim;
// h() strip marks + the eu toroidal blit are proven-dead bookkeeping
// already covered by the renderer).
// ============================================================
class Slice220Test {

    @Test fun `input lock latches kDd once through stateL 12`() {
        val w = world()
        settleIntro(w)                             // fresh world is jC==8
        w.player.lockInput(w)                      // k.o() — kAm=1, kDd=0
        assertTrue(w.kAm); assertFalse(w.kDd)
        w.stateL(12)
        assertTrue(w.kDd, "k.b(true) latches dd once input is locked")
        w.stateL(12)
        assertTrue(w.kDd, "second pass keeps the latch")
    }

    @Test fun `unlock re-arms the veil latch`() {
        val w = world()
        settleIntro(w)
        w.player.lockInput(w); w.stateL(12); assertTrue(w.kDd)
        w.player.unlockInput(w)
        assertFalse(w.kAm); assertFalse(w.kDd)
        w.player.lockInput(w); w.stateL(12)
        assertTrue(w.kDd, "re-locking re-latches dd")
    }

    @Test fun `visible window tracks the camera in et cells`() {
        val w = world()
        settleIntro(w)
        w.kO = 420; w.kP = 260; w.rebuildCamRect() // k.O/k.P writable cam
        w.stateL(12)
        assertEquals(21, w.visX0)                  // 420/20
        assertEquals(40, w.visX1)                  // (420+399)/20
        assertEquals(13, w.visY0)                  // 260/20
        assertEquals(24, w.visY1)                  // (260+239)/20
    }

    @Test fun `negative camera y clamps on grounded missions`() {
        val w = world()
        settleIntro(w)
        w.kP = -30; w.rebuildCamRect()
        w.stateL(12)
        assertEquals(0, w.visY0, "bh[aj]!=3 clamps vy0 at 0")
    }

    @Test fun `flying missions keep a negative window top`() {
        val w = world(aj = 1)                      // kBh[1]==3 → bh3
        settleIntro(w)
        w.kP = -30; w.rebuildCamRect()
        w.stateL(12)
        // :9084 bias: sy = -30-20 = -50 → vy0 = -50/20 = -2, kept verbatim
        assertTrue(w.visY0 < 0, "bh3 leaves vy0 unclamped")
        assertEquals(9, w.visY1)                   // (-50+239)/20
    }

    @Test fun `window reset flags visDirty when the camera jumps`() {
        val w = world()
        settleIntro(w)
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        w.stateL(12)
        val x0 = w.visX0
        w.kO = 2000; w.rebuildCamRect() // disjoint jump → dM
        w.stateL(12)
        assertTrue(w.visX0 > x0 + 1, "window follows a disjoint camera jump")
        assertFalse(w.visDirty, "dM self-clears after the (dead) h() arm")
    }
}
