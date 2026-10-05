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

class Slice242Test {
    // slice 242 — `i.aU()` L437: the ax10-S31 claim-QTE zone draw arm
    // (i.java:32653-32840): overlap+P&128-clear gate, `n` latch checks,
    // cyan progress bar (`j.b` fill + `j.c` outline), `bA[4-aD..3]`
    // card row at y=160.

    private fun zone(w: Level0World): Entity {
        val e = Entity(10, null)
        e.S = 31
        e.setPositionPx(w.kO + 200, w.kP + 100)
        // ax10 is a zone: refreshBoxes() leaves W staged — set the
        // overlap rect directly over the player.
        e.W[0] = w.kO + 100; e.W[2] = w.kO + 300
        e.W[1] = w.kP + 40;  e.W[3] = w.kP + 140
        w.npcs.add(e)
        w.player.setPositionPx(w.kO + 200, w.kP + 100)
        w.player.refreshBoxes()                    // player inside W
        return e
    }

    @Test fun `overlap gate + hidden bit both block the draw`() {
        val w = world()
        val e = zone(w)
        e.aD = 1; e.Z[2] = 50
        w.fxRects.clear(); w.fxOutlines.clear(); w.fxPrompts.clear()
        w.player.setPositionPx(w.kO + 390, w.kP + 5)  // outside W
        w.player.refreshBoxes()
        e.drawStyleF(w)
        assertEquals(0, w.fxRects.size, "no overlap → L452 return")
        assertEquals(0, w.fxPrompts.size)
        w.player.setPositionPx(w.kO + 200, w.kP + 100)
        w.player.refreshBoxes()
        e.P = 128                                  // hidden → return
        e.drawStyleF(w)
        assertEquals(0, w.fxRects.size, "P&128 → L452 return")
    }

    @Test fun `armed zone draws bar and positions the card row`() {
        val w = world()
        val e = zone(w)
        e.aD = 2                                   // slots 2,3
        e.Z[2] = 100; e.aB = 25                    // bar: 360*75/100=270
        val pr2 = ScriptPrompt().apply { anim.e = 0 }
        val pr3 = ScriptPrompt().apply { anim.e = 0 }
        Entity.scriptPrompts[2] = pr2
        Entity.scriptPrompts[3] = pr3
        try {
            w.fxRects.clear(); w.fxOutlines.clear(); w.fxPrompts.clear()
            assertEquals(1, e.drawStyleF(w))
            assertEquals(intArrayOf(20, 200, 270, 10, 0x33ebf4).toList(),
                         w.fxRects[0].toList(), "j.b cyan fill")
            assertEquals(intArrayOf(20, 200, 360, 10, -1).toList(),
                         w.fxOutlines[0].toList(), "j.c white outline")
            assertEquals(listOf(2, 3), w.fxPrompts.toList())
            val colW = 400 / 3
            assertEquals(colW - 10, pr2.a); assertEquals(160, pr2.b)
            assertEquals(colW - 10 + colW, pr3.a); assertEquals(160, pr3.b)
        } finally {
            Entity.scriptPrompts[2] = null
            Entity.scriptPrompts[3] = null
        }
    }

    @Test fun `m-over-10 stopped card latches n and returns`() {
        val w = world()
        val e = zone(w)
        e.aD = 1; e.m = 12
        Entity.scriptPrompts[2] = ScriptPrompt()   // e=-1 → stopped
        try {
            w.fxPrompts.clear()
            e.drawStyleF(w)
            assertEquals(1, e.nl, "L4d5: bA[m-10].b() → n = 1")
            assertEquals(0, w.fxPrompts.size, "early return — no card row")
        } finally { Entity.scriptPrompts[2] = null }
    }

    @Test fun `aA==Z3 with bh3 latches n directly`() {
        val w = world()
        val e = zone(w)
        e.aD = 1; e.m = 0
        e.aA = e.Z[3]
        w.kAj = 1                                  // MISSION_BH[1] == 3
        w.fxPrompts.clear()
        e.drawStyleF(w)
        assertEquals(1, e.nl, "bh==3 → n = 1")
        assertEquals(0, w.fxPrompts.size)
    }

    @Test fun `aA==Z3 non-bh3 still needs the card done`() {
        val w = world()
        val e = zone(w)
        e.aD = 1; e.m = 0
        e.aA = e.Z[3]
        w.kAj = 0                                  // MISSION_BH[0] == 4
        Entity.scriptPrompts[0] = ScriptPrompt()   // stopped → n=1
        try {
            e.drawStyleF(w)
            assertEquals(1, e.nl, "bA[m].b() → n = 1")
        } finally { Entity.scriptPrompts[0] = null }
    }
}
