package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 366 — app pause/resume is `k.c()`/`k.d()` (hideNotify/showNotify,
 * structured k.java:5767-5836).
 *
 * The port only stashed the current track on pause (`bG = fi`) and replayed
 * it on resume. The original also clears the input latches, pauses a `cd[6]`
 * claim script and stops the channel on hide; its stash is always -1 (`bH` and
 * `fj` are only written by `<clinit>`), so nothing replays on show — instead,
 * in play, show opens the pause menu (`l(14)`); with the channel stopped its
 * entry clears `fi`, so the track is gone until something plays again.
 */
class Slice366Test {
    private fun playing(): Level0World {
        val w = world(); w.stateL(8); settleIntro(w)
        w.drainCommands()
        return w
    }

    @Test fun `hide clears the latches, stops the channel and stashes nothing`() {
        val w = playing()
        w.sfx(12)                                    // a live channel
        assertEquals(12, w.audioTrack)
        w.pad.commit(Pad.M_RIGHT)
        w.hideNotify()
        assertEquals(-1, w.audioTrack, "e.b()")
        assertTrue(w.drainCommands().any { it is Command.StopAudio })
        assertEquals(-1, w.kBg, "bG = … ? -1 : bH(-1)")
        assertFalse(w.pad.v(Pad.M_RIGHT), "v() cleared the latches")
    }

    @Test fun `hide pauses a cd6 claim script in play`() {
        val w = playing()
        val c = Entity(5, null); c.cd[6] = true; c.cd[0] = false
        w.kC = c
        w.hideNotify()
        assertTrue(c.cd[0], "C.Y()")
    }

    @Test fun `show in play opens the pause menu and replays nothing`() {
        val w = playing()
        w.kFi = 3
        w.hideNotify(); w.drainCommands()
        w.showNotify()
        assertEquals(14, w.jC, "J() → l(14)")
        assertFalse(w.drainCommands().any { it is Command.PlaySfx }, "bG = -1: no z(bG)")
        // l(14)'s `if (!e.a()) fi = -1` (k.java:5180) runs after hide's e.b():
        // the original loses the track across an interruption.
        assertEquals(-1, w.kFi, "no live track at l(14) → fi = -1")
        assertFalse(w.kFy)
    }

    @Test fun `show on the pause menu resets the cursor`() {
        val w = playing()
        w.stateL(14); w.kBw = 2
        w.hideNotify(); w.showNotify()
        assertEquals(0, w.kBw)
        assertEquals(14, w.jC)
    }

    @Test fun `show on a yes-no prompt resets bw`() {
        val w = playing()
        w.stateL(14)
        w.kBv = 3; w.kEc = 13; w.kBw = 1
        w.hideNotify(); w.showNotify()
        assertEquals(-1, w.kBw)
    }

    @Test fun `hide and show latch once`() {
        val w = playing()
        w.showNotify()
        assertEquals(8, w.jC, "no show without a hide")
        w.hideNotify(); w.drainCommands()
        w.sfx(12); w.hideNotify()
        assertEquals(12, w.audioTrack, "a second hide is a no-op")
    }
}
