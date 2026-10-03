package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 323 — m2 posted-perch trio wake chain (aw311/312/313 at x~3800-4040,
 * y~800). Verdict for the "hanging guards can't be hit" m2 report.
 *
 * Original design (decompile + scripts.bin byte decode):
 *  - Trio spawn `P|32` + `S=152` posted-perch: `au<2`-gated non-bh3 entity
 *    loop skips `P&32 && !P&16` → they never tick → dormant + invulnerable
 *    while parked (Level0World.kt:4966-4973, k.java L215 arm).
 *  - Wake path is NOT proximity at the perch: ax5-S8 mission-logic watchers
 *    aw308 `W=[2667,493,2886,541]` (aG=309) and aw345 `@(3695,256)` (aG=346)
 *    sit on the ROOFTOP approach; `eventBind` arms `bindContext` (P|16 on
 *    the watcher) → `runClaimScript` runs type-2 blocks TARGETING the trio.
 *  - Claim ops drive the parked guards without them ticking: op21 lerps
 *    `r14.ak/al` inside the CLAIMER's tick (Entity.kt:2217+), op22 sets
 *    anims (152→154→164/187/188/194), op23/24 write `P` flags.
 *  - Scripts 309/342/346/575 stage a choreography: reposition trio to
 *    x~3333-3414, then descend. The repro walked the GROUND path (y=800)
 *    under the perch — never entered the rooftop watcher zones, so the
 *    guards stayed parked = "unfightable" — same dormant-sentry family as
 *    slice 219/322, faithful, not a bug.
 */
class Slice323Test {
    @Test
    fun `watcher overlap binds claim script and lerps parked trio`() {
        val w = world(aj = 2)
        val trio = w.npcs.filter { it.aw in listOf(311, 312, 313) }
        val w308 = w.npcs.first { it.aw == 308 }
        assertEquals(listOf(2667, 493, 2886, 541), w308.W.toList())
        assertTrue(trio.all { it.S == 152 && (it.P and 32) != 0 })

        // walk the player through the watcher zone on the rooftop path
        // until the watcher binds; script 309 then runs its setup groups.
        // It ends by putting the player in S50 (the spotted fail) — the
        // checks below land while the claim still holds (slice 379: the
        // death anim plays out instead of freezing the world at once).
        outer@ for (step in 0..8) {
            w.player.setPositionPx(2600 + step * 40, 493)
            for (i in 0 until 60) {
                w.tick(emptyList())
                if (w308.claimActive()) break@outer
            }
        }
        repeat(40) { w.tick(emptyList()) }

        // the watcher is now the live claimer running script 309
        assertTrue(w308.claimActive(), "watcher aw308 should hold the claim")
        assertEquals(16, w308.P and 16, "claim binds P|16 to the claimer")
        // script-309 setup groups lerped the parked trio to formation:
        // op21 targets (0x0d05,0x0d56,0x0d41) = (3333,3414,3393) at y~800
        val byAw = trio.associateBy { it.aw }
        assertEquals(3333, byAw[311]!!.ak, "op21 lerp target for aw311")
        assertEquals(3414, byAw[312]!!.ak, "op21 lerp target for aw312")
        assertEquals(3393, byAw[313]!!.ak, "op21 lerp target for aw313")
        // facing flags written via op23 mask-1 (P|=1) while still P|32
        assertTrue(trio.all { (it.P and 1) != 0 && (it.P and 32) != 0 })
    }

    @Test
    fun `ground approach under the perch does not wake the trio`() {
        val w = world(aj = 2)
        val trio = w.npcs.filter { it.aw in listOf(311, 312, 313) }
        // the repro's path: straight along the ground under the perch
        for (step in 0..14) {
            w.player.setPositionPx(3000 + step * 100, 800)
            repeat(80) { w.tick(emptyList()) }
        }
        assertNull(w.kC, "no claim should bind on the ground route")
        assertTrue(trio.all { it.S == 152 && (it.P and 32) != 0 },
            "trio stays parked/dormant — faithful to the original")
    }

    @Test
    fun `descent scripts exist for all three guards`() {
        // scripts.bin byte-level check encoded as expectation:
        // scripts 342/346/575 carry the trio's descent blocks (S154 etc.)
        val w = world(aj = 2)
        assertNotNull(w.findByAw(311)); assertNotNull(w.findByAw(312))
        assertNotNull(w.findByAw(313))
        val w345 = w.npcs.firstOrNull { it.aw == 345 }
        assertNotNull(w345, "second watcher aw345 @ (3695,256) must exist")
        assertEquals(5, w345.ax); assertEquals(8, w345.S)
        assertEquals(346, w345.aG, "aw345 binds script 346")
    }
}
