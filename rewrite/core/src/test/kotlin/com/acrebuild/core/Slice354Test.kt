package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 354 — the pickup sparkle field in `i.F()` (bytecode i.javap.txt
 * F() offsets 2709-3238, structured/i.java:3233-3283).
 *
 * After a pickup (`i.e = 30`) every frame draws `j.j.nextInt()` once per
 * slot and spawns only when the draw is `>= 0` with its low 7 bits clear;
 * `g = r%40 + 60` (no abs, 21..99), `h = |r%60| + 60`. Each live slot draws
 * one white radial line at i° from radius g+f to h+f, then `f += 15`
 * (reset with `g = 0` at 100). The port skipped the sign gate (twice the
 * draws on the shared stream for 30 frames after every pickup), wrapped
 * `g` in abs, drew a four-dot rosette and never advanced `f`.
 */
class Slice354Test {
    /** `java.util.Random`'s LCG, resumed from a world's RNG state. */
    private class Lcg(var s: Long) {
        fun next(): Int {
            s = (s * 0x5DEECE66DL + 0xBL) and ((1L shl 48) - 1)
            return (s ushr 16).toInt()
        }
    }

    @Test fun `thirty frames match the bytecode draw for draw and ray for ray`() {
        val w = world()
        val p = w.player
        p.setPositionPx(400, 300); p.refreshBoxes()
        w.iE = 30
        val ref = Lcg(w.rng.state())
        val f = IntArray(360); val g = IntArray(360); val h = IntArray(360)
        var spawned = 0; var negMod = 0
        repeat(30) { frame ->
            // reference frame (offsets 2837-2951, 2988-3238)
            for (i in 0 until 360) {
                val r = ref.next()
                if (r >= 0 && (r and 127) == 0 && g[i] == 0) {
                    g[i] = ref.next() % 40 + 60
                    h[i] = Math.abs(ref.next() % 60) + 60
                    f[i] = 0
                    spawned++; if (g[i] < 60) negMod++
                }
            }
            val ox = p.ak - w.kO
            val oy = ((p.W[1] + p.W[3]) shr 1) - w.kP
            val want = ArrayList<List<Int>>()
            for (i in 0 until 360) {
                if (g[i] > 15) {
                    val th = i * 256 / 360
                    val sn = Trig.cos(Trig.N - th); val cs = Trig.cos(th)
                    want += listOf(ox + ((sn * (g[i] + f[i])) shr 8),
                                   oy + ((cs * (g[i] + f[i])) shr 8),
                                   ox + ((sn * (h[i] + f[i])) shr 8),
                                   oy + ((cs * (h[i] + f[i])) shr 8), -1)
                    f[i] += 15
                    if (f[i] >= 100) { f[i] = 0; g[i] = 0 }
                }
            }
            w.fxLines.clear()
            p.drawStyleF(w)
            assertEquals(ref.s, w.rng.state(), "frame $frame: same j.j draws")
            assertEquals(want, w.fxLines.map { it.toList() }, "frame $frame rays")
            assertContentEquals(g, w.iG, "frame $frame g")
            assertContentEquals(f, w.iF, "frame $frame f")
        }
        assertEquals(0, w.iE)
        assertTrue(spawned > 0, "the field actually spawned")
        assertTrue(negMod > 0, "some g came from a negative r%40 (21..59)")
    }

    @Test fun `a ray lives exactly seven frames`() {
        val w = world()
        val p = w.player
        p.setPositionPx(400, 300); p.refreshBoxes()
        w.iF = IntArray(360)
        w.iG = IntArray(360) { 1 }                      // g=1: no spawn, no draw
        w.iH = IntArray(360)
        w.iG!![90] = 30; w.iH!![90] = 80                 // one live slot at 90°
        var draws = 0
        for (frame in 0 until 12) {
            w.iE = 1                                     // keep the block running
            w.fxLines.clear()
            p.drawStyleF(w)
            for (l in w.fxLines) {
                // θ = 64: x = j.b(n−θ)·r = r, y = j.b(θ)·r = 0 → a ray to the right
                assertEquals(l[1], l[3]); assertTrue(l[2] > l[0]); assertEquals(-1, l[4])
            }
            draws += w.fxLines.size
            if (w.iG!![90] == 0) break
        }
        assertEquals(7, draws, "f = 0..90 draws, the 7th pass resets at 105")
        assertEquals(0, w.iF!![90])
    }
}
