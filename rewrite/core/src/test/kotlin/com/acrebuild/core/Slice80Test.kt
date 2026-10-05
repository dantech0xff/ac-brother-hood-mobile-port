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

// ---------------------------------------------------------------------------
// slice 80 — M() win-stats proc (k.java:3280-3445) + ap[] unification
// ---------------------------------------------------------------------------
class Slice80Test {

    private fun openStats(w: Level0World): Level0World {
        w.kBA[15] = 1                          // checkpoint → no L68-77 redirect
        w.screenL(15)
        return w
    }

    @Test fun `state15 commits jC and M() replaces the sim`() {
        val w = openStats(world())
        assertEquals(15, w.jC)
        assertFalse(w.kAl)                          // 15 not frozen — M() runs
        val s0 = w.player.S
        repeat(3) { w.tick(emptyList()) }
        assertEquals(s0, w.player.S, "sim must not run under j.c==15")
    }

    @Test fun `jG==1 arm writes best time into bA 52+aj`() {
        val w = openStats(world())
        w.kAp[5] = 777
        repeat(2) { w.tick(emptyList()) }            // jG hits 1
        assertEquals(777, w.kBA[52 + (w.kAj shl 1)])
    }

    @Test fun `panel grows eE to 140 then jG resets to 1`() {
        val w = openStats(world())
        repeat(14) { w.tick(emptyList()) }           // eE 10*14 = 140
        assertEquals(140, w.kEe)
        assertEquals(177, w.kEf)
        assertEquals(1L, w.jG)
    }

    @Test fun `score math kills dh collects di deaths and bonus`() {
        val w = openStats(world())
        w.kAu = 1                                    // difficulty mid
        w.kAp[0] = 10; w.kAp[3] = 5; w.kAp[1] = 2; w.kAp[5] = 3
        repeat(26) { w.tick(emptyList()) }           // panel cap + jG>10
        // base 0 (aj<7) + 10*200 + 5*200 - 2*300 + 3*30 = 2490
        assertEquals(2490, w.statsScore)
        assertTrue(w.statsScoreVisible)
    }

    @Test fun `aj==7 base 3000 and bh3 reads ap4 not ap5`() {
        val w = openStats(world())
        w.kAj = 7; w.kAu = 0
        w.kAp[4] = 5; w.kAp[5] = 99                  // bh[7]=4 → ap5 path
        repeat(26) { w.tick(emptyList()) }
        assertEquals(3000 + 99 * 30, w.statsScore)
    }

    @Test fun `bh3 mission reads ap4 for the bonus`() {
        val w = openStats(world())
        w.kAj = 1                                    // bh[1]=3
        w.kAp[4] = 7
        repeat(26) { w.tick(emptyList()) }
        assertEquals(7 * 30, w.statsScore)
    }

    @Test fun `overtime penalty caps at -1000`() {
        val w = openStats(world())
        w.kDg = 16 * 700                             // 700 s → cap 1000
        w.kAp[0] = 15
        repeat(26) { w.tick(emptyList()) }
        assertEquals(1500 - 1000, w.statsScore)      // kAj=0 au=0 → dh=100
    }

    @Test fun `score floor is zero`() {
        val w = openStats(world())
        w.kAp[1] = 4                                 // -1200 deaths
        repeat(26) { w.tick(emptyList()) }
        assertEquals(0, w.statsScore)
    }

    @Test fun `fire before jG10 only skips the reveal`() {
        val w = openStats(world())
        repeat(15) { w.tick(emptyList()) }           // jG=2
        w.pad.queuePress(458784); w.tick(emptyList())
        assertEquals(10L, w.jG)
        assertEquals(0, w.kBA[44], "persist must not run on the skip")
    }

    @Test fun `fire after jG10 persists bA and dB-dF`() {
        val w = openStats(world())
        w.kAp[0] = 2; w.kAu = 0
        w.kAx = 30; w.kAy = 31; w.kAN = 4; w.kAz = 9
        repeat(26) { w.tick(emptyList()) }
        w.pad.queuePress(458784); w.tick(emptyList())
        // 458784 == 327712|131072: any fire also passes the confirm arm —
        // kAj advanced on the same tick, so read the pre-nav slot (aj=0).
        assertEquals(200, w.kBA[81])
        assertEquals(30, w.kBA[44]); assertEquals(31, w.kBA[46])
        assertEquals(4, w.kBA[48]); assertEquals(9, w.kBA[32])
        assertEquals(0, w.kBA[36])
        assertEquals(30, w.kDB); assertEquals(31, w.kDC)
        assertEquals(4, w.kDF); assertEquals(9, w.kDD)
    }

    @Test fun `confirm advances to next mission and l(30)`() {
        val w = openStats(world())
        w.kAp[0] = 1
        repeat(26) { w.tick(emptyList()) }
        w.pad.queuePress(458784 or 327712); w.tick(emptyList())
        assertEquals(1, w.kAj)
        assertEquals(1, w.kBA[14])
        assertEquals(30, w.jC, "eg[aj] true → l(30)")
    }

    @Test fun `right-soft skip advances mission without confirm`() {
        val w = openStats(world())
        w.kAp[0] = 1
        repeat(26) { w.tick(emptyList()) }
        // back/skip only: 131072 is inside 458784 but not 327712 —
        // v(327712) stays false so the confirm arm can't pre-empt it.
        w.pad.queuePress(131072); w.tick(emptyList())
        assertEquals(1, w.kAj)
        assertEquals(2, w.jC, "v(131072) && aj<7 → l(2)")
    }

    @Test fun `finale confirm on aj7 zeroes progress and l(24)`() {
        val w = world()
        w.kAj = 7; w.kBA[15] = 1
        w.screenL(15)
        repeat(26) { w.tick(emptyList()) }
        w.pad.queuePress(458784 or 327712); w.tick(emptyList())
        assertEquals(0, w.kAj); assertEquals(0, w.kBA[14])
        assertEquals(1, w.kBA[15])
        assertEquals(24, w.jC)
    }

    @Test fun `the death screen does not feed ap1`() {
        // `ap[1]` counts `a(true)` retries — `o(1)` (k.java:5175); `l(12)`
        // has no ap write (k.java:1656-1660). Slice 377 (Slice377Test).
        val w = world()
        w.stateL(12)
        assertEquals(0, w.kAp[1])
    }

    @Test fun `typewriter emits the next-mission string`() {
        val w = openStats(world())
        repeat(26) { w.tick(emptyList()) }
        assertEquals(62, w.statsTypeNext)
        assertTrue(w.typewriterText.isNotEmpty())
    }
}
