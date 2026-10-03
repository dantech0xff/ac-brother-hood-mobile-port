package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 360 — `g.l()` and `g.aw()` follow the bytecode (g.javap.txt l()
 * offsets 0-588; structured g.java:5158-5283).
 *
 * The port's `l()` had no hold-to-turn (`k.bD > 4 && !x(dir) → av`), so a
 * player standing with the wrong facing could only turn through S79, an
 * `aA` alert or a double-tap; it sent a released S11 brake into the
 * right-arm tail (a misread of the simple view), so the brake was cut to
 * one tick (two, with an extra run tick, facing east); and it skipped the
 * `k.aT` freeze, the `bM` clear and the ax73 `Z[0]==3` `cq` clear.
 * `aw()` lacked the UP + `f()` → `i(6)` arm and the `aS == 9 → a(122,8)`
 * arm, and dropped through a plain fall where the original runs
 * `a(257,8)`. `k.bD` was also split into two counters.
 */
class Slice360Test {
    private fun settled(): Pair<Level0World, Entity> {
        val w = world(); settleIntro(w)
        val p = w.player
        p.S = 0; p.ag = 0; p.aA = 0
        return w to p
    }

    @Test fun `a broken stance turns only once the hold passes four ticks`() {
        val (w, p) = settled()
        p.av = true; p.aA = 0                            // op32 broke the stance
        val pad = Pad(); pad.commit(Pad.M_RIGHT)
        pad.bD = 4
        w.playerFsm.l(p, pad)
        assertTrue(p.av, "k.bD <= 4: no turn")
        pad.bD = 5
        w.playerFsm.l(p, pad)
        assertFalse(p.av, "k.bD > 4 && !x(8256) → av = false")
    }

    @Test fun `the e head raises a calm stance to 2`() {
        val (w, p) = settled()
        w.kAA = 0; p.aA = 1
        w.playerFsm.tick(p, Pad())
        assertEquals(2, p.aA, "k.aA <= 0 && aA <= 1 → aA = 2")
        w.kAA = 5; p.aA = 0
        w.playerFsm.tick(p, Pad())
        assertEquals(1, p.aA, "alert: no raise; the tail's aA==0 → 1")
    }

    @Test fun `aA bit 2 skips the context dispatch for one tick`() {
        val (w, p) = settled()
        w.kAA = 0
        p.aA = 2 or 4
        val pad = Pad(); pad.commit(Pad.M_CONTEXT)
        w.playerFsm.tick(p, pad)
        assertEquals(2, p.aA and 7, "bit 2 cleared")
        assertTrue(p.S != 67, "ap() skipped while bit 2 was set (S=${p.S})")
    }

    private fun brakeTrace(dir: Int): List<Int> {
        val (w, p) = settled()
        repeat(15) { w.pad.e(dir); w.tick(emptyList()) }
        assertEquals(12, p.S, "running")
        w.pad.releaseFlush()
        val ss = mutableListOf<Int>()
        repeat(20) { w.tick(emptyList()); ss += p.S }
        return ss
    }

    @Test fun `a released run brakes in S11 and never runs on, both ways`() {
        for (dir in listOf(Pad.M_RIGHT, Pad.M_LEFT)) {
            val ss = brakeTrace(dir)
            assertEquals(11, ss[0], "co >= 4 → i(11)")
            assertFalse(12 in ss, "no run tick after the release: $ss")
            assertEquals(0, ss.last(), "then aw() settles once r(): $ss")
        }
    }

    @Test fun `k aT freezes the grounded input`() {
        val (w, p) = settled()
        p.av = true; p.aA = 1                            // would turn at once
        w.kAT = true
        val pad = Pad(); pad.bD = 9
        pad.commit(Pad.M_RIGHT)
        assertTrue(w.playerFsm.l(p, pad))
        assertTrue(p.av, "no turn while the camera holds another target")
    }

    @Test fun `l clears the bM link`() {
        val (w, p) = settled()
        p.bM = Entity(13, null)
        w.playerFsm.l(p, Pad())
        assertNull(p.bM)
    }

    @Test fun `aw DOWN on an aS 9 cell enters S122`() {
        val (w, p) = settled()
        p.aZ = true; p.aS = 9
        val pad = Pad(); pad.commit(Pad.M_DOWN)
        w.playerFsm.l(p, pad)
        assertEquals(122, p.S, "a(122, 8)")
    }

    @Test fun `aw UP with a carry target in reach enters S6`() {
        val (w, p) = settled()
        p.aZ = true; p.av = false
        val c = Entity(11, w.clips[7])
        c.setPositionPx(p.ak + 40, p.al)
        p.ci = c
        val pad = Pad(); pad.commit(Pad.M_UP)
        w.playerFsm.l(p, pad)
        assertEquals(6, p.S, "S != 6 && u(16388) && f() → i(6)")
    }

    @Test fun `aw DOWN at a thin edge runs a(257,8), not a plain fall`() {
        val w = world(); w.stateL(8)
        var px = -1; var py = -1
        outer@ for (y in 2 until w.level.rows - 2) {
            for (x in 2 until w.level.cols - 3) {
                if (w.level.collisionCell(x, y) >= 19 &&
                    w.level.collisionCell(x, y + 1) == 0 &&
                    w.level.collisionCell(x, y - 1) == 0 &&
                    w.level.collisionCell(x, y - 2) == 0 &&
                    w.level.collisionCell(x + 2, y + 1) < 12) { px = x; py = y; break@outer }
            }
        }
        assertTrue(px >= 0, "no thin platform edge in level0")
        val p = w.player
        p.setPositionPx(px * 20 + 10, py * 20 - 1)
        p.S = 0; p.av = false; p.ag = 0
        p.refreshBoxes(); p.probeCells(w); p.aZ = true; p.aS = 0
        val pad = Pad(); pad.commit(Pad.M_DOWN)
        w.playerFsm.l(p, pad)
        assertEquals(257, p.S)
    }

    @Test fun `k bD is one counter`() {
        val w = world()
        w.kBD = 7
        assertEquals(7, w.pad.bD)
        w.pad.bD = 3
        assertEquals(3, w.kBD)
    }
}
