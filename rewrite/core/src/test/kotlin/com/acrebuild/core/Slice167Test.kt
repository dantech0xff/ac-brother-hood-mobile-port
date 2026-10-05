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

class Slice167Test {

    @Test fun `duration gate drops SFX while music is live`() {
        val w = world()
        w.sfx(0)                                       // music track 0 (h.a=38958ms)
        w.drainCommands()
        w.sfx(12)                                      // SFX while music within duration
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx },
            "both options on + live slot → request skipped (e.java:62-84)")
        assertEquals(0, w.audioTrack)
    }

    @Test fun `request plays once current slot expires`() {
        val w = world()
        w.sfx(10)                                      // SFX, h.a[10]=218ms
        w.drainCommands()
        for (i in 0 until 5) w.tick(listOf())          // 5×62=310ms > 218
        w.sfx(11)
        val cmds = w.drainCommands()
        assertTrue(cmds.any { it is com.acrebuild.core.Command.PlaySfx &&
                              it.slot == 11 }, "expired slot frees the channel")
        assertTrue(cmds.any { it is com.acrebuild.core.Command.StopAudio },
            "preempt emits e.b() first")
    }

    @Test fun `empty slots 22 26 27 never play`() {
        val w = world()
        w.sfx(22); w.sfx(26); w.sfx(27)
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx }, "a[i]==null → nop")
        assertEquals(-1, w.audioTrack)
    }

    @Test fun `audioStop emits StopAudio and clears the channel`() {
        val w = world()
        w.sfx(10)
        w.drainCommands()
        w.audioStop()                                  // e.b()
        assertEquals(-1, w.audioTrack)
        assertTrue(w.drainCommands().any {
            it is com.acrebuild.core.Command.StopAudio })
        assertFalse(w.audioPlaying(), "e.a() false after stop")
    }

    @Test fun `audioPlaying mirrors the h_a duration window`() {
        val w = world()
        w.sfx(10)                                      // 218ms → ~4 ticks
        assertTrue(w.audioPlaying())
        for (i in 0 until 4) w.tick(listOf())          // 248ms > 218
        assertFalse(w.audioPlaying(), "expired after h.a[10]")
    }

    @Test fun `SFX-option off still lets music through the gate`() {
        val w = world()
        w.kBF = false                                  // SFX option off
        w.sfx(5)                                       // music <10 unaffected
        assertTrue(w.drainCommands().any {
            it is com.acrebuild.core.Command.PlaySfx && it.slot == 5 })
        w.sfx(12)                                      // SFX gated out
        assertTrue(w.drainCommands().none {
            it is com.acrebuild.core.Command.PlaySfx && it.slot == 12 })
    }
}
