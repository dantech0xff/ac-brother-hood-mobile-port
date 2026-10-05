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

class Slice77Test {

    @Test fun `l12 plays track 7 sting`() {
        val w = world()
        w.screenL(12)
        val cmds = w.drainCommands()
        assertEquals(7, w.audioTrack)
        assertEquals(7, (cmds.single() as Command.PlaySfx).slot)
        assertTrue(w.drainCommands().isEmpty())      // drain empties
    }

    @Test fun `l15 plays track 6 when entering from non-play`() {
        val w = world()
        w.screenL(17)
        w.drainCommands()
        w.screenL(15)
        assertTrue(w.drainCommands().any { (it as? Command.PlaySfx)?.slot == 6 })
    }

    @Test fun `l15 from play runs the win jingle`() {
        val w = world()                            // jC==8 → ex!=10/22
        w.screenL(15)
        assertTrue(w.drainCommands().any { (it as? Command.PlaySfx)?.slot == 6 })
        assertEquals(6, w.audioTrack)
    }

    @Test fun `l2 plays track 0 on quit arm`() {
        val w = world()
        w.screenL(2)                               // jC=10 not exempt
        assertTrue(w.drainCommands().any { (it as? Command.PlaySfx)?.slot == 0 })
        assertEquals(0, w.audioTrack)
    }

    @Test fun `l8 from screen 9 plays mission music ee-aj`() {
        val w = world()
        w.screenL(9); w.drainCommands()
        w.screenL(8)                               // B(): ee[0]=5
        assertTrue(w.drainCommands().any { (it as? Command.PlaySfx)?.slot == 5 })
    }

    @Test fun `l8 with aJ 1 plays track 9 override`() {
        val w = world()
        w.kAJ = 1
        w.screenL(9); w.drainCommands()
        w.screenL(8)
        assertTrue(w.drainCommands().any { (it as? Command.PlaySfx)?.slot == 9 })
    }

    @Test fun `channel gate bE blocks tracks under 10`() {
        val w = world()
        w.kBE = false
        w.screenL(12)                              // z(7) gated out
        assertTrue(w.drainCommands().isEmpty())
        assertEquals(-1, w.audioTrack)
    }

    @Test fun `l27 stops audio`() {
        val w = world()
        w.screenL(12)                              // audioTrack=7
        w.screenL(27)                              // e.b() only
        assertEquals(-1, w.audioTrack)
    }

    @Test fun `jG is the render counter reset by l`() {
        val w = world()
        w.tick(emptyList())
        assertEquals(1L, w.jG)                     // increments per tick
        w.screenL(12)
        assertEquals(0L, w.jG)                     // j.g=0 on state entry
        w.tick(emptyList())
        assertEquals(1L, w.jG)                     // frames count even frozen
        assertTrue(w.tickIndex > 1L)               // tickIndex did not reset
    }
}
