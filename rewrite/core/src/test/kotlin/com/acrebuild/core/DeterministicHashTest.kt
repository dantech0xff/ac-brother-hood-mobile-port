package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class DeterministicHashTest {

    private fun script(queue: InputQueue) {
        // Deterministic input script: steer right, release, steer up-left.
        queue.post(InputQueue.Type.DOWN, 300, 120)
        queue.post(InputQueue.Type.MOVE, 320, 110)
        queue.post(InputQueue.Type.UP, 320, 110)
        queue.post(InputQueue.Type.DOWN, 100, 60)
        queue.post(InputQueue.Type.UP, 100, 60)
    }

    private fun replay(seed: Long): List<String> {
        val queue = InputQueue()
        script(queue)
        val engine = TickEngine(SpikeWorld(DeterministicRandom(seed)), queue)
        val hashes = ArrayList<String>()
        // ~5 seconds of render deltas at mixed cadence.
        var ms = 0L
        while (ms < 5_000) {
            for (t in engine.advance(16)) hashes.add(t.hashHex)
            for (t in engine.advance(47)) hashes.add(t.hashHex)
            ms += 63
        }
        return hashes
    }

    @Test
    fun `same seed and input script produce identical hash sequence`() {
        val a = replay(0xC0FFEE)
        val b = replay(0xC0FFEE)
        assertEquals(a.size, b.size)
        assertTrue(a.size > 70) // ~5s at 62ms ticks
        assertEquals(a, b)
    }

    @Test
    fun `different seed produces different hash sequence`() {
        assertNotEquals(replay(1L), replay(2L))
    }

    @Test
    fun `save and restore reproduce subsequent hashes`() {
        val queue = InputQueue()
        script(queue)
        val engine = TickEngine(SpikeWorld(DeterministicRandom(7L)), queue)
        engine.advance(62 * 30)
        val mid = engine.currentTickIndex()

        val saved = engine.world.snapshot()
        val bytes = saved.encode()
        val restored = TickEngine(SpikeWorld(DeterministicRandom(0)), InputQueue())
        restored.restore(SaveSnapshot.decode(bytes))
        assertEquals(mid, restored.world.snapshot().tickIndex)

        val tail1 = ArrayList<String>()
        val tail2 = ArrayList<String>()
        repeat(20) {
            tail1.addAll(engine.advance(62).map { it.hashHex })
            tail2.addAll(restored.advance(62).map { it.hashHex })
        }
        assertEquals(tail1, tail2)
    }
}
