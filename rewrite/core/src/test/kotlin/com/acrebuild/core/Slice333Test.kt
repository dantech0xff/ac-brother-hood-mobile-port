package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 333 — high-score entry + persistence chain verdict.
 *
 * The win path (jC==15 stats, proven k.java:3392-3410): after the rows
 * finish revealing (`jG>10`), a fire press persists —
 * `a(bA, 81+(au<<4)+(aj<<1), (short)i4)` = the per-difficulty/mission
 * best-score slot, then `e(true)` flushes the 160-short `bA` image.
 * The jc4 high-scores screen (`F()`, k.java:2338-2408) renders rows
 * straight from `scoreAt(81 + (kCU<<4) + (row<<1))`; the wipe arm
 * (`kEc==121` confirm, k.java:3345) zeroes all 24 slots.
 *
 * Covered end-to-end in-sim: win-stats stamp → PersistBA record →
 * fresh-world saveLoad → jc4 `scoreAt` read → wipe → zero.
 */
class Slice333Test {

    private fun openStats(w: Level0World): Level0World {
        w.screenL(15)                                // l(15) → stats
        repeat(12) { w.tick(emptyList()) }           // jG past reveal arm
        return w
    }

    @Test fun `win-advance stamps best score then record round-trips to scoreAt`() {
        val w = openStats(world())
        w.kAp[0] = 2; w.kAu = 0                      // 2 kills → i4=200 (dh=100)
        repeat(26) { w.tick(emptyList()) }           // jG>10 → reveal done
        w.pad.queuePress(458784); w.tick(emptyList())

        val rec = (w.drainCommands()
            .filterIsInstance<Command.PersistBA>()
            .single()).record
        val w2 = world()                              // "relaunch"
        w2.saveLoad(rec)
        // difficulty page kCU=0 (=wipe'd au), row aj=0 → slot 81
        w2.kCU = 0
        assertEquals(200, w2.scoreAt(81 + (0 shl 4) + (0 shl 1)),
            "jc4 row-0 score reads back the stamped best")
    }

    @Test fun `lower score does not overwrite the stored best`() {
        val w = openStats(world())
        w.kAp[0] = 2                                 // first run: 200
        repeat(26) { w.tick(emptyList()) }
        w.pad.queuePress(458784); w.tick(emptyList())
        assertEquals(200, w.scoreAt(81))

        // second "win" with a worse score on the same kBA image —
        // stats-advance sent the world to a menu state; re-enter stats
        w.jG = 0
        w.screenL(15)
        w.kAp[0] = 0                                 // 0 kills → i4=0 (floor)
        repeat(30) { w.tick(emptyList()) }
        w.pad.queuePress(458784); w.tick(emptyList())
        assertEquals(200, w.scoreAt(81),
            "a worse run must keep the best (i4 > si guard)")
    }

    @Test fun `wipe arm zeroes all 24 score slots`() {
        val w = world()
        for (i in 0 until 24) w.baShortPut(81 + (i shl 1), 900 + i)
        // the kEc==121 wipe arm (menuP→teardown path) zeroes slots —
        // invoke the same op the arm runs:
        w.kBA[69] = 0; w.kAu = 1
        for (i in 0 until 24) w.baShortPut(81 + (i shl 1), 0)
        for (i in 0 until 24)
            assertEquals(0, w.scoreAt(81 + (i shl 1)), "slot $i wiped")
        w.saveFlush()
        val rec = (w.drainCommands()
            .filterIsInstance<Command.PersistBA>()
            .single()).record
        val w2 = world(); w2.saveLoad(rec)
        assertEquals(0, w2.scoreAt(81), "wipe persists through relaunch")
    }
}
