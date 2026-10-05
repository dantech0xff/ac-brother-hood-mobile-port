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

/** Slice 78 — the menu machine: `eA` item tables (k.java:8458), `K()`
 *  menu init (:5386), `L()` cursor nav (:5489), `m()` resolve (:5472),
 *  `Q()` back/confirm dispatch (:3576-3940), `O()`/`P()` state stack
 *  (:3561-3573), and the `a()`-proc frozen-menu frame (:1775/:1788). */
class Slice78Test {

    @Test fun `eA table is the proven six-row corpus`() {
        val w = world()
        assertEquals(listOf(2, 1, 3, 32), w.kEA[0].toList())
        assertEquals(listOf(11, 12, 4, 6, 0, 8), w.kEA[1].toList())
        assertEquals(listOf(35, 36, 37), w.kEA[2].toList())
        assertEquals(listOf(14, 15), w.kEA[3].toList())
        assertEquals(listOf(83, 84, 123, 97, 5, 113, 7, 87), w.kEA[4].toList())
        assertEquals(listOf(106, 107, 108, 109), w.kEA[5].toList())
    }

    @Test fun `fail screen K(3) arms YES NO rows with eB 59 eC 25`() {
        val w = world()
        w.stateL(12)
        assertTrue(w.kAl)
        assertEquals(3, w.kBv)
        assertEquals(2, w.kEy)
        assertEquals(25, w.kEc)
        assertEquals(59, w.kEb)
        assertEquals(-1, w.kBw)
        assertTrue(w.menuVisible)
        assertEquals("MISSION FAILED", w.menuTitle())
        assertEquals("DO YOU WANT TO RESTART?", w.menuPrompt())
        assertEquals(listOf("YES" to false, "NO" to false), w.menuRows())
    }

    @Test fun `first confirm press only moves cursor to row 0`() {
        val w = world()
        w.stateL(12)
        // v(327712) arm: `bw == -1 → bw = 0; return` (structured :3639)
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(0, w.kBw)
        assertEquals(12, w.jC)               // still on the fail screen
        assertTrue(w.menuRows()[0].second)   // YES row selected
    }

    @Test fun `confirm on YES reloads the level`() {
        val w = world()
        w.stateL(12)
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())                  // cursor → 0
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())                  // confirm YES → a(true) → reload
        assertEquals(8, w.jC)
        assertFalse(w.kAl)
        assertEquals(1, w.deaths)            // fail arm counted it once
    }

    @Test fun `tap on NO row exits to menu state 2`() {
        val w = world()
        w.stateL(12)
        val npcCount = w.npcs.size
        // verbatim b() layout: the 2-row dialog stacks — NO is the
        // second rect (93,150,214,30) (k.java:7706-7720, proven)
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 165),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 165)))
        assertEquals(2, w.jC)                // W() teardown + l(2)
        assertEquals(-1, w.kBx)
        assertTrue(w.npcs.size < npcCount)   // teardown cleared entities
    }

    @Test fun `YES row tap dispatches restart directly`() {
        val w = world()
        w.stateL(12)
        // YES is the top rect (93,117,214,30)
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 150, 120),
            InputQueue.Event(1, InputQueue.Type.UP, 150, 120)))
        assertEquals(8, w.jC)                // reload ran
        assertFalse(w.kAl)
    }

    @Test fun `cursor nav clamps and plays the blip`() {
        val w = world()
        w.stateL(12)
        w.audioStop()                         // clear mission-music track —
                                              // e.a() guard would eat z(23)
        w.pad.queuePress(Pad.M_UP)
        w.tick(emptyList())
        assertEquals(0, w.kBw)               // up from -1 clamps at 0
        assertEquals(23, w.audioTrack)       // z(23) nav blip fired
        // e.a() guard: a track is "playing" → next nav press skips z(23)
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(1, w.kBw)               // still moves the cursor
        assertEquals(23, w.audioTrack)       // but no second blip
    }

    @Test fun `pause menu bv1 lists the six pause items`() {
        val w = world()
        w.stateL(14)                          // l(14) → K(1) pause menu
        assertEquals(1, w.kBv)
        assertEquals(6, w.kEy)
        assertEquals(72, w.kEb)
        assertEquals(listOf("RESUME", "RESTART", "OPTIONS", "HELP",
                            "MAIN MENU", "EXIT"),
                     w.menuRows().map { it.first })
    }

    @Test fun `state 14 is a menu screen and does not tick the world`() {
        val w = world()
        w.stateL(14)
        val t0 = w.tickIndex
        w.tick(emptyList())
        assertTrue(w.tickIndex > t0)          // frame advances…
        assertEquals(14, w.jC)                // …but no sim ran (menu frame)
    }

    @Test fun `Y() picks CONTINUE in eA0 row0 when save exists`() {
        val w = world()
        w.kBA[15] = 1
        w.stateL(2)                           // l(2) → K(0) main menu
        assertEquals(2, w.kEA[0][0])
        assertEquals("CONTINUE", w.menuRows()[0].first)
    }

    @Test fun `Y() falls back to QUICK PLAY id117 with no save`() {
        val w = world()
        w.stateL(2)
        assertEquals(117, w.kEA[0][0])
        assertEquals("QUICK PLAY", w.menuRows()[0].first)
    }

    @Test fun `MAIN MENU item pushes state and YES pops it back`() {
        val w = world()
        w.stateL(14)                          // pause menu
        assertEquals(0, w.kDs)
        // confirm row 4 = MAIN MENU → O() push + K(3) quit-confirm
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())                   // cursor → 0
        repeat(4) {                             // nav down ×4 → bw=4
            w.audioStop()
            w.pad.queuePress(Pad.M_DOWN)
            w.tick(emptyList())
        }
        assertEquals(4, w.kBw)
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(1, w.kDs)                // O() pushed (14,bv1,bw4)
        assertEquals(73, w.kEc)               // "DO YOU WANT TO QUIT?"
        assertEquals(3, w.kBv)
        // YES → eC==73 → P() pops + W() teardown + l(2)
        // verbatim: jc14 bv3 panel (93,67,214,z3) → YES row (93,117)
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 150, 120),
            InputQueue.Event(1, InputQueue.Type.UP, 150, 120)))
        assertEquals(2, w.jC)                 // l(2) overrides P()'s restore
        assertEquals(0, w.kDs)
    }

    @Test fun `stats screen 31 confirm lands on 13 with bx reset`() {
        val w = world()
        w.kBx = 43                            // d(0,43) = "SCORE"
        w.stateL(13)                          // 13 + bx>=0 remaps to 31
        assertEquals(31, w.jC)
        assertTrue(w.statsVisible)
        assertEquals("SCORE", w.statsText())
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())                    // v(65568) → l(13); bx=-1
        w.tick(emptyList())                    // bx<0 arm → l(13) settles
        assertEquals(13, w.jC)
        assertEquals(-1, w.kBx)
    }

    @Test fun `stats screen with bx -1 self-exits to 13`() {
        val w = world()
        w.kBx = -1
        w.stateL(13)                          // stays 13 (no remap)
        assertEquals(13, w.jC)
        assertFalse(w.statsVisible)
    }

    @Test fun `options toggles flip bE bF au cm verbatim`() {
        val w = world()
        w.stateL(14)
        // tap each options row directly — bv=4 options menu via item 4
        w.stateL(14)
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        w.audioStop()
        // nav to bw=2 = OPTIONS → confirm → K(4)
        repeat(2) {
            w.pad.queuePress(Pad.M_DOWN)
            w.tick(emptyList()); w.audioStop()
        }
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(4, w.kBv)
        // row 0 = MUSIC (83) — confirm toggles kBE
        val e0 = w.kBE
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 110),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 110)))
        assertEquals(!e0, w.kBE)
        // row 3 = CONTROL (97) — cycles kAu mod 3 (bA[69]==0 skips 2)
        w.kAu = 0
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 110 + 3 * 30),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 110 + 3 * 30)))
        assertEquals(1, w.kAu)
        assertEquals(1, w.kBA[8])
        // row 2 = STYLE (123) — flips kCm
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 200, 110 + 2 * 30),
            InputQueue.Event(1, InputQueue.Type.UP, 200, 110 + 2 * 30)))
        assertEquals(0, w.cm)
        assertEquals(0, w.kBA[80])
    }

    @Test fun `back edge pops the menu stack`() {
        val w = world()
        w.stateL(14)
        // push via MAIN MENU then back at the quit-confirm (bv=3)
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        repeat(5) {
            w.audioStop()
            w.pad.queuePress(Pad.M_DOWN)
            w.tick(emptyList())
        }
        w.pad.queuePress(Pad.M_CONTEXT)
        w.tick(emptyList())
        assertEquals(3, w.kBv)
        // v(131072) back on bv=3: `ex==8 || j.c==14` → P() pop (jC=14)
        w.pad.queuePress(Pad.M_CYCLE)
        w.tick(emptyList())
        assertEquals(14, w.jC)
        assertEquals(0, w.kDs)
    }

    @Test fun `jG counter runs during menu screens`() {
        val w = world()
        w.stateL(12)
        val g0 = w.jG
        w.tick(emptyList())
        assertEquals(g0 + 1, w.jG)
    }
}
