package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 376 — `j.t` is the "skip the next fail/win/stats frame" latch, and
 * `b(z2)` is one world pass.
 *
 * - Only `j.a(i,false)` with `i<5` sets `j.t` (j.java:1334-1342): bit 0
 *   from the veil latch at the head of `b()` (k.java:2697), bit 4 from
 *   `K()` (k.java:2674) in the loader's step 8 (:4834) and the checkpoint
 *   reload `a(true)` (:5174). `k.p()` (:2662-2669), `W()` (:5132),
 *   `j.h()` and the skipped jc12/13/31 frames (:1119, :1470) clear it;
 *   `j.i()` there (:1109, :1453) is its only reader. The pointer handlers
 *   never touch it (k.java:486-516). The port latched it on pad presses
 *   and dropped every input event while it was set.
 * - `b(false)` runs whole on every jC 8 and jC 21 frame: the veil head,
 *   the F() pass, the bubbles, `c()` under the claim gate (:3131), the
 *   SKIP pill, then the un-gated tail counters (:3163-3253).
 */
class Slice376Test {
    private fun ev(seq: Long, type: InputQueue.Type, x: Int, y: Int) =
        InputQueue.Event(seq, type, x, y)

    private fun lineDialog(w: Level0World) {
        w.autoDismissDialog = false
        w.dlgU = 1
        w.stateL(21)
        w.dlgV = 0; w.dlgW = 2
        w.dlgBM[0] = "A LONG FIRST PAGE THAT TAKES A WHILE TO TYPE OUT"
        w.dlgBM[1] = "SECOND"
        w.dlgBQ = true; w.dlgBS = 30; w.dlgBR = 0; w.dlgBT = 0
    }

    @Test fun `the veil latch runs on dialog frames and sets bit 0`() {
        val w = world()
        lineDialog(w)
        w.player.lockInput(w)                         // k.o()
        assertFalse(w.kDd)
        w.tick(emptyList())
        assertTrue(w.kDd, "b(false)'s head latches dd on the jc21 frame")
        assertEquals(1, w.jT, "j.a(0,false) → t |= 1 (k.java:2697)")
    }

    @Test fun `input still reaches the world under the lock`() {
        val w = world()
        settleIntro(w)
        w.player.lockInput(w)
        w.stateL(12)                                  // l(12) → b(true) → latch
        assertEquals(1, w.jT)
        w.tick(listOf(ev(0, InputQueue.Type.DOWN, 123, 45)))
        assertEquals(123, w.lastMoveX, "pointerPressed has no j.t gate")
        assertEquals(45, w.lastMoveY)
    }

    @Test fun `a fail screen opened under the lock skips one frame`() {
        val w = world()
        settleIntro(w)
        w.player.lockInput(w)
        w.stateL(12)
        w.tick(listOf(ev(0, InputQueue.Type.DOWN, 200, 165),
                      ev(1, InputQueue.Type.UP, 200, 165)))
        assertEquals(0, w.jT, "`j.t=0` on the skipped frame (k.java:1119)")
        assertEquals(12, w.jC, "no Q() on the skipped frame")
        w.tick(listOf(ev(2, InputQueue.Type.DOWN, 200, 165),
                      ev(3, InputQueue.Type.UP, 200, 165)))
        assertEquals(2, w.jC, "the next tap reaches the NO row")
    }

    @Test fun `the stats screen skips its first frame on j-t`() {
        val w = world()
        settleIntro(w)
        w.kBx = 9
        w.jT = 16
        w.stateL(13)
        assertEquals(31, w.jC, "l(13) with bx >= 0 opens the stats screen")
        w.tick(listOf(ev(0, InputQueue.Type.DOWN, 200, 120),
                      ev(1, InputQueue.Type.UP, 200, 120)))
        assertEquals(0, w.jT, "`j.t=0` (k.java:1470)")
        assertEquals(31, w.jC, "the tap on the skipped frame is not read")
        assertEquals(9, w.kBx)
        w.tick(listOf(ev(2, InputQueue.Type.DOWN, 200, 120),
                      ev(3, InputQueue.Type.UP, 200, 120)))
        // `l(13); bx = -1` (:1462-1464): l(13) still sees bx >= 0 and
        // re-enters 31; the next frame's `bx < 0` arm opens 13
        assertEquals(-1, w.kBx)
        w.tick(emptyList())
        assertEquals(13, w.jC)
    }

    @Test fun `the loader's step 8 runs K()`() {
        val w = world()
        w.stateL(9)
        repeat(8) { w.tick(emptyList()) }
        assertEquals(0, w.jT and 16, "G(1..7) leave j.t alone")
        w.tick(emptyList())
        assertEquals(16, w.jT and 16, "G(8) → K() → t |= 16 (k.java:4834)")
    }

    @Test fun `a(true) runs K() and a(false) does not`() {
        val w = world()
        settleIntro(w)
        w.resetLevel(true)
        assertEquals(16, w.jT and 16, "a(true): K() (k.java:5174)")
        val w2 = world()
        settleIntro(w2)
        w2.resetLevel(false)
        assertEquals(0, w2.jT, "a(false) has no K()")
    }

    @Test fun `W() clears bit 4 and k-p() clears bit 0 only while locked`() {
        val w = world()
        settleIntro(w)
        w.jT = 17
        w.stateL(15)
        w.tick(emptyList())                           // M() j.g==1 → W()
        assertEquals(1, w.jT, "j.b(4,false) (k.java:5132)")
        w.kAm = false
        w.player.unlockInput(w)
        assertEquals(1, w.jT, "k.p() is a no-op unless am (k.java:2663)")
        w.kAm = true; w.kDd = true
        w.player.unlockInput(w)
        assertEquals(0, w.jT, "j.b(0,false); j.i(0)")
        assertFalse(w.kAm); assertFalse(w.kDd)
    }

    @Test fun `dialog frames step the tail counters`() {
        val w = world()
        settleIntro(w)
        lineDialog(w)
        w.kAn = true; w.kBI = 0
        val fn = w.kFn
        w.tick(emptyList())
        assertEquals(w.kFk, w.kBI, "an fade-in steps behind the dialog (k.java:3163-3174)")
        assertEquals(fn + 1, w.kFn)
    }

    @Test fun `b(true) leaves the weapon-corner latch alone`() {
        val w = world()
        settleIntro(w)
        w.actionLock = 1
        w.stateL(14)                                  // l(14) from play: b(true)
        assertEquals(14, w.jC)
        assertEquals(1, w.actionLock, "c(true) skips the `!z2` weapon-corner arm (k.java:4276)")
    }
}
