package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Slice 377 — the restart `a(z2)` (structured k.java:5139-5232) step by
 * step: `ap[1]` counts `a(true)` retries (`o(1)`, :5175), the checkpoint
 * arm restores `ap[0,3,4,5]` and `ap[2] = bA[40] << 4` from `bA` and keeps
 * `ap[1]` and `dg`, the other arms run `L(); F(aj)`; the tail refills the
 * meter (`g.e(ax)`), runs `T()`, `l(8)` and `B()`; the head clears
 * `i.bW/bX` (and `i.bV` on `a(false)`) and carries the alert (`aA|=256`).
 * The fresh `aS` has the ax0 init (`az=100`, `aA=2`, i.java:1971-1981).
 */
class Slice377Test {
    private fun withCheckpoint(prep: (Level0World) -> Unit = {}): Level0World {
        val w = world()
        val cp = w.checkpoints.first()
        prep(w)
        // the Slice1Test overlapCheckpoint recipe: boxes refreshed after
        // the teleport, the record force-ticked (P|16)
        w.player.setPositionPx(cp.ak, cp.al + 5); w.player.refreshBoxes()
        w.npcs.firstOrNull { it.ax == 2 && it.aw == cp.aw }?.let { it.P = it.P or 16 }
        repeat(2) { w.tick(emptyList()) }
        assertNotNull(w.checkpointSnap, "precondition: the checkpoint fired")
        return w
    }

    @Test fun `the death screen does not count and a checkpoint retry does`() {
        val w = withCheckpoint()
        val n = w.kAp[1]
        w.stateL(12)
        assertEquals(n, w.kAp[1], "l(12) leaves ap[1] alone (k.java:1656)")
        w.resetLevel(true)
        assertEquals(n + 1, w.kAp[1], "o(1); the restore arm skips ap[1]")
        w.resetLevel(true)
        assertEquals(n + 2, w.kAp[1])
    }

    @Test fun `without a checkpoint and on a full restart L() zeroes the stats`() {
        val w = world()
        settleIntro(w)
        w.kAp[1] = 3; w.kDg = 50
        w.resetLevel(true)
        assertEquals(0, w.kAp[1], "o(1) then L() (k.java:5207)")
        assertEquals(0, w.kDg)
        w.kAp[1] = 3; w.kDg = 50
        w.resetLevel(false)
        assertEquals(0, w.kAp[1]); assertEquals(0, w.kDg)
    }

    @Test fun `the checkpoint arm keeps dg and reads ap from bA`() {
        val w = withCheckpoint()
        w.kDg = 500
        w.kBA[36] = 4; w.kBA[38] = 2; w.kBA[40] = 7; w.kBA[42] = 1
        w.kBA[52 + (w.kAj shl 1)] = 33
        w.kAp[2] = 999
        w.resetLevel(true)
        assertEquals(500, w.kDg, "no L() on the checkpoint arm")
        assertEquals(4, w.kAp[0]); assertEquals(2, w.kAp[3])
        assertEquals(7 shl 4, w.kAp[2], "ap[2] = bA[40] << 4 (k.java:5189)")
        assertEquals(1, w.kAp[4]); assertEquals(33, w.kAp[5])
    }

    @Test fun `a retry refills the meter to ax`() {
        val w = withCheckpoint { it.player.x1 = 7 }
        assertEquals(7, w.checkpointSnap!!.x1, "precondition: low meter at the checkpoint")
        w.resetLevel(true)
        assertEquals(w.kAx, w.player.x1, "g.e(ax) (k.java:5225)")
    }

    @Test fun `the alert carries as 256 onto a fresh stance`() {
        val w = withCheckpoint()
        w.player.aA = 16 or 4
        w.resetLevel(true)
        assertEquals(2 or 256, w.player.aA,
            "fresh aA=2 (i.java:1975), |256 for the old 16 (k.java:5153-5155)")
        val w2 = world()
        settleIntro(w2)
        w2.player.aA = 256
        w2.resetLevel(false)
        assertEquals(2, w2.player.aA, "a(false) does not carry it")
    }

    @Test fun `the player spawns at draw depth 100`() {
        val w = world()
        assertEquals(100, w.player.az, "i.java:1972")
    }

    @Test fun `the phase flags clear on every restart and bV on a full one`() {
        val w = world()
        settleIntro(w)
        w.iBW = true; w.iBX = 5; w.iBV = 2
        w.resetLevel(true)
        assertFalse(w.iBW); assertEquals(0, w.iBX)
        assertEquals(2, w.iBV, "a(true) keeps bV (k.java:5142-5146)")
        w.iBW = true; w.iBX = 5
        w.resetLevel(false)
        assertFalse(w.iBW); assertEquals(0, w.iBX); assertEquals(0, w.iBV)
    }

    @Test fun `B() restarts the mission music when bG is set`() {
        val w = world()
        settleIntro(w)
        w.kBg = 0
        w.resetLevel(true)
        assertEquals(w.kEE[w.kAj], w.audioTrack, "z(ee[aj]) (k.java:1619-1625)")
        w.kBg = -1
        w.resetLevel(true)
        assertEquals(-1, w.audioTrack, "e.b() twice, no B()")
    }

    @Test fun `l(8) clears the input latches`() {
        val w = world()
        settleIntro(w)
        w.pad.e(Pad.M_CONTEXT); w.pad.commit()
        assertTrue(w.pad.v(Pad.M_CONTEXT))
        w.resetLevel(true)
        assertFalse(w.pad.v(Pad.M_CONTEXT), "l(8) → v() (k.java:1740)")
        assertEquals(8, w.jC)
    }

    @Test fun `the stopwatch slides back in when the checkpoint holds a timer`() {
        val w = withCheckpoint { it.kAL = 44 }
        assertEquals(44, w.checkpointSnap!!.kAL)
        w.kAJ = 0; w.kAK = 0
        w.resetLevel(true)
        assertEquals(1, w.kAJ, "aL != -1 → aJ=1; aK=-40 (k.java:5203-5206)")
        assertEquals(-40, w.kAK)
    }

    @Test fun `W() clears the phase flags and de`() {
        val w = world()
        settleIntro(w)
        w.iBW = true; w.iBX = 5; w.iBV = 2; w.kDe = true
        w.stateL(15)
        w.tick(emptyList())                       // M() j.g==1 → W()
        assertFalse(w.iBW); assertEquals(0, w.iBX); assertEquals(0, w.iBV)
        assertFalse(w.kDe, "de = false (k.java:5131)")
    }
}
