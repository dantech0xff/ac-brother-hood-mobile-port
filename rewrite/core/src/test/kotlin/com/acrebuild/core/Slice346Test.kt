package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 346 — ax47/ax50 FSM pairing restored.
 *
 * The original dispatch is `case 47 → aL()` (ledge sentinel, S0/80–84/93/94)
 * and `case 50 → aK()` (pouncer, S119–130) — i.java:5181-5194, bytecode
 * i.javap.txt:20024-20025 (47 → 7680 `aL`, 50 → 7687 `aK`), proven; each
 * method has no other call site. Slice 64 ran the bodies the other way
 * round, so every retyped record sat in a state its FSM had no arm for:
 * all 12 sentinels spawn at S93 (m0 4, m2 2, m3 3, m5 3) and all 8 pouncers
 * at S120 (m3 3, m5 5) — none ever grabbed or pounced.
 *
 * These tests drive the real `Level0World.tick` dispatch on the mission-3
 * pack, which carries both types.
 */
class Slice346Test {
    private fun m3(): Level0World = world(aj = 3).also { settleIntro(it) }

    /** Camera centred on [e] (W inside `k.ac`), `P|16` so the `k.I()`
     *  au/park gate ticks it regardless of LOD. */
    private fun watch(w: Level0World, e: Entity) {
        e.P = e.P or 16
        w.kO = e.ak - 200; w.kP = e.al - 120
    }

    @Test fun `mission-3 retype — sentinels spawn at S93, pouncers at S120`() {
        val w = world(aj = 3)
        val sentinels = w.npcs.filter { it.ax == 47 }
        val pouncers = w.npcs.filter { it.ax == 50 }
        assertEquals(listOf(251, 842, 923), sentinels.map { it.aw }.sorted())
        assertEquals(listOf(203, 625, 626), pouncers.map { it.aw }.sorted())
        assertTrue(sentinels.all { it.S == 93 }, "ax11 r8[5]=93 → ax47 (i.java:2644)")
        assertTrue(pouncers.all { it.S == 120 }, "ax17 r8[5]=120 → ax50 (i.java:2651)")
    }

    @Test fun `world tick runs aK for ax50 — a pouncer in view leaves its S120 perch`() {
        val w = m3()
        val e = w.npcs.first { it.aw == 203 }
        assertEquals(120, e.S)
        watch(w, e)
        // player in the pouncer's column, mid-point at its anchor
        w.player.setPositionPx(e.ak, e.al + 29)
        w.player.setAnim(0); w.player.refreshBoxes()
        w.tick(emptyList())
        assertTrue(e.S == 119 || e.S in 121..128,
            "aK L6/L23: ceiling-grab (S119) or 3x3 pounce pick (121-128), got S${e.S}")
    }

    @Test fun `world tick runs aL for ax47 — a sentinel at S93 catches a falling player`() {
        val w = m3()
        val e = w.npcs.first { it.aw == 251 }
        assertEquals(93, e.S)
        watch(w, e)
        // player falling (S43 ∈ drop-kill set) through the sentinel's box,
        // feet above its bottom edge — the aL L26/L40 grab window.
        w.player.setPositionPx(e.ak, e.al - 20)
        w.player.setAnim(43); w.player.refreshBoxes()
        w.tick(emptyList())
        assertEquals(80, e.S, "aL L40: i(80)")
        assertEquals(89, w.player.S, "aS.i(89) — caught")
        assertTrue(w.kL === e, "k.a(this,0,W) armed the claim")
    }
}
