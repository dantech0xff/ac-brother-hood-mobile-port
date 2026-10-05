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

class Slice76Test {

    @Test fun `l12 commits fail state and freeze flag`() {
        val w = world()
        w.screenL(12)
        assertEquals(12, w.jC); assertTrue(w.failed)
        assertTrue(w.kAl); assertFalse(w.inPlay)
        assertEquals(8, w.kCy)                     // ex state (play)
        assertEquals(1, w.deaths)                  // first entry counts
    }

    @Test fun `l12 reentry does not recount deaths`() {
        val w = world()
        w.screenL(12); w.screenL(12)
        assertEquals(1, w.deaths)                  // `i==12 && ex!=12` guard
    }

    @Test fun `l12 clears kAD and runs the banner tail`() {
        val w = world(charmap = charmap())
        w.kAD = w.player
        w.screenL(12)
        assertNull(w.kAD)
        assertEquals(25, w.kEc); assertEquals(59, w.kEb)   // L17 (high-conf)
        assertEquals(3, w.kBv); assertEquals(-1, w.kBw)    // K(3): bw=-1
        assertEquals(2, w.kEy)                             // eA[3].size = 2
        // kEd = linesHeight(wrapPage(d0(25),206)[0]) — real font metrics
        assertEquals(14, w.kEd)                            // eC text-height
    }

    @Test fun `l13 remaps to 31 when bx is set`() {
        val w = world()
        w.kBx = 56
        w.screenL(13)
        assertEquals(31, w.jC); assertTrue(w.won)
        assertTrue(w.kAl); assertTrue(w.missionWon)
    }

    @Test fun `l13 keeps 13 when bx negative`() {
        val w = world()
        w.kBx = -1
        w.screenL(13)
        assertEquals(13, w.jC); assertTrue(w.won)
    }

    @Test fun `l15 commits complete state without freeze`() {
        val w = world()
        w.kBA[15] = 1                              // checkpoint → no L68-77
        w.screenL(15)
        assertEquals(15, w.jC); assertTrue(w.missionWon)
        assertFalse(w.kAl)                         // 15 not in freeze set
        assertEquals(8, w.kCy)                     // cy = play state
        assertEquals(37, w.kEf)                    // eF=37 preamble
    }

    @Test fun `l15 medal stamp re-enters as 22`() {
        val w = world()
        w.kAp[0] = 7                               // L35 condition: >=7
        w.screenL(15)
        assertEquals(1, w.kCc[0])
        assertEquals(1, w.kBA[130])                // stamp persisted
        assertEquals(22, w.jC)                     // L56-64 → i=22 re-entry
        assertFalse(w.kAl)                         // 22 not in freeze set —
                                                 // medal screen runs live
    }

    @Test fun `l15 next-mission redirect needs unlocked and ex not 10`() {
        val w = world()
        w.kAj = 1                                  // aj+1=2 ∈ fP={0,2,5,7}
        w.screenL(17)                              // ex=17 when l(15) runs
        w.screenL(15)
        // ap[0]<7 → no stamp; kAj+1=2 in fP && bA[15]==0 && ex==17
        assertEquals(10, w.jC)                     // L68-77 redirect → play
    }

    @Test fun `l15 checkpoint flag prevents redirect`() {
        val w = world()
        w.screenL(17)
        w.kBA[15] = 1                              // bA[15]==1 → no redirect
        w.screenL(15)
        assertEquals(15, w.jC)
    }

    @Test fun `l21 arms dialog and freezes`() {
        val w = world()
        w.screenL(21)
        assertEquals(21, w.jC); assertTrue(w.dialogModal)
        assertTrue(w.kAl)                          // dlgU=0 not in {8,9}
    }

    @Test fun `l21 with mode 8 stays unfrozen`() {
        val w = world()
        w.dlgU = 8
        w.screenL(21)
        assertEquals(21, w.jC)
        assertFalse(w.kAl)                         // u==8 dialogs run world
    }

    @Test fun `l8 with cy 17 redirects to 17`() {
        val w = world()
        w.screenL(17); w.screenL(16)               // cy now 17
        w.screenL(8)
        assertEquals(17, w.jC)
    }

    @Test fun `l6 timer honors dx cheat flag`() {
        val w = world()
        w.screenL(6); assertEquals(60, w.kFd)
        w.kDx = true; w.screenL(6); assertEquals(240, w.kFd)
    }

    @Test fun `l23 writes its screen constants`() {
        val w = world()
        w.screenL(23)
        assertEquals(19, w.kEc); assertEquals(70, w.kEb); assertEquals(-1, w.kBw)
    }

    @Test fun `l4 stashes au into cU`() {
        val w = world()
        w.kAu = 42; w.screenL(4); assertEquals(42, w.kCU)
    }

    @Test fun `l14 resets equip flags`() {
        val w = world()
        w.kAo = true; w.kAn = true
        w.screenL(14)
        assertFalse(w.kAo); assertFalse(w.kAn)
        assertEquals(1, w.kBv); assertEquals(-1, w.kFi)
    }

    @Test fun `commit tail clears input edge`() {
        val w = world()
        w.pad.edge = 5
        w.screenL(12)
        assertEquals(0, w.pad.edge)                // v() input reset
    }

    @Test fun `context edge on fail screen reloads to play`() {
        val w = world()
        w.screenL(12)
        w.pad.queuePress(Pad.M_CONTEXT); w.tick(emptyList())   // cursor → 0
        w.pad.queuePress(Pad.M_CONTEXT); w.tick(emptyList())   // YES → a(true)
        assertEquals(8, w.jC); assertFalse(w.kAl)
        assertTrue(w.inPlay)
    }

    @Test fun `freeze states 16 17 31 all set kAl`() {
        val w = world()
        for (s in listOf(16, 17, 31)) {
            w.screenL(s)
            assertTrue(w.kAl, "state $s frozen")
        }
    }
}

// =========================================================================
// Slice 77 — z()/e.b() audio port (e.java:50-87, k.java:7363): track
//            queue, channel gates, ee[] mission music, j.g counter.
// =========================================================================
