package com.acrebuild.gdx

import com.acrebuild.core.Command
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/** The tick-failure boundary with an injected throwing world. */
class TickDriverTest {
    private class Boom : RuntimeException("boom")

    private class FakeWorld(val failAt: Int) {
        var ticks = 0
        val pending = ArrayList<Command>()
        fun tick() {
            ticks++
            pending += Command.PlaySfx(ticks)
            pending += Command.PersistBA(ByteArray(320))
            if (ticks == failAt) throw Boom()
        }
        fun drain(): List<Command> = ArrayList(pending).also { pending.clear() }
    }

    @Test fun `healthy ticks hand their commands back`() {
        val w = FakeWorld(failAt = -1)
        val d = TickDriver({ w.tick() }, w::drain, { error("no failure expected") })
        val c = d.tick(emptyList())
        assertEquals(2, c.size)
        assertFalse(d.quarantined)
    }

    @Test fun `the first failure quarantines the world and drops its commands`() {
        val w = FakeWorld(failAt = 3)
        val fatal = ArrayList<Throwable>()
        val d = TickDriver({ w.tick() }, w::drain, { fatal += it })
        d.tick(emptyList()); d.tick(emptyList())
        val failed = d.tick(emptyList())
        assertTrue(failed.isEmpty(), "no PersistBA, no audio from the failed tick")
        assertTrue(d.quarantined)
        assertIs<Boom>(d.failure)
        assertTrue(w.pending.isEmpty(), "the failed tick's queue was drained and dropped")
        repeat(5) { assertTrue(d.tick(emptyList()).isEmpty()) }
        assertEquals(3, w.ticks, "no further ticks")
        assertEquals(1, fatal.size, "onFatal runs once")
        w.pending += Command.StopAudio
        assertTrue(d.drainIdle().isEmpty(), "nothing executes once quarantined")
    }
}
