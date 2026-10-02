package com.acrebuild.gdx

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The 62ms tick-pacing policy in `tickAccStep` (j.java:189-219
 * semantics, proven): remainder-keep holds the cadence on a
 * vsync-quantized clock — discarding it ticks every 4th frame at 60fps
 * (66.7ms ≈ 7% slow, the Devin Review finding on b9fd2f01) — and the
 * ≤1-tick clamp caps catch-up credit so a lag spike never bursts.
 */
class Level0GameTickTest {

    @Test
    fun `cadence averages 62ms at 60fps`() {
        var acc = 0L
        var ticks = 0
        // 600 frames of 16667µs ≈ 10.0002s of wall time.
        for (i in 0 until 600) {
            val (a, due) = Level0Game.tickAccStep(acc, 16_667L)
            acc = a
            if (due) ticks++
        }
        // floor(10_000_200 / 62_000) = 161 — the acc=0 discard gave 150.
        assertEquals(161, ticks)
    }

    @Test
    fun `sub-tick remainder is kept across frames`() {
        var acc = 0L
        for (i in 0 until 3) {
            val (a, due) = Level0Game.tickAccStep(acc, 16_667L)
            acc = a
            assertFalse(due)
        }
        val (a, due) = Level0Game.tickAccStep(acc, 16_667L)
        assertTrue(due)
        assertEquals(66_668L - 62_000L, a)
    }

    @Test
    fun `backlog credit clamped to a single tick`() {
        // A 5s hitch resolves exactly one tick and credits at most
        // TICK_US — no multi-tick catch-up burst.
        val (a, due) = Level0Game.tickAccStep(0L, 5_000_000L)
        assertTrue(due)
        assertEquals(Level0Game.TICK_US, a)
        // Recovery: the one credited tick fires next frame, then the
        // cadence is back to normal.
        val (a2, due2) = Level0Game.tickAccStep(a, 16_667L)
        assertTrue(due2)
        assertEquals(16_667L, a2)
        val (a3, due3) = Level0Game.tickAccStep(a2, 16_667L)
        assertFalse(due3)
        assertEquals(33_334L, a3)
    }

    @Test
    fun `sustained slow frames dilate the sim one tick per frame`() {
        // 100ms frames (10fps): the original runs one tick per loop
        // iteration regardless — slow-mo, never a burst.
        var acc = 0L
        for (i in 0 until 10) {
            val (a, due) = Level0Game.tickAccStep(acc, 100_000L)
            acc = a
            assertTrue(due)
            assertTrue(a <= Level0Game.TICK_US)
        }
    }

    @Test
    fun `no tick below the 62ms threshold`() {
        val (a, due) = Level0Game.tickAccStep(0L, 61_999L)
        assertFalse(due)
        assertEquals(61_999L, a)
    }
}
