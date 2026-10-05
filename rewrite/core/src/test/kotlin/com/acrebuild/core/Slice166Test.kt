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

class Slice166Test {

    @Test fun `sfx emits PlaySfx through z and sets audioTrack`() {
        val w = world()
        w.sfx(12)                                      // k.A(12) → z(12)
        assertEquals(12, w.audioTrack, "audioTrack = n (:7363)")
        val cmds = w.drainCommands()
        assertTrue(cmds.any { it is com.acrebuild.core.Command.PlaySfx &&
                              it.slot == 12 }, "PlaySfx(12) queued")
        assertTrue(w.sfxLog.contains(12), "sfxLog records the request")
    }

    @Test fun `sfx respects the kBF SFX-option gate`() {
        val w = world()
        w.kBF = false                                  // SFX option off
        w.sfx(13)                                      // slot >= 10 gated
        assertTrue(w.sfxLog.contains(13), "request still logged pre-gate")
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx }, "kBF=false → no SFX play")
        assertEquals(-1, w.audioTrack)
    }

    @Test fun `sfx respects the kBE music-option gate`() {
        val w = world()
        w.kBE = false                                  // music option off
        w.sfx(5)                                       // slot < 10 gated
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx }, "kBE=false → no music play")
    }

    @Test fun `sfx ignores out-of-range slots like z`() {
        val w = world()
        w.sfx(34); w.sfx(-1)
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx }, "n<0||n>=34 → nop")
    }
}
