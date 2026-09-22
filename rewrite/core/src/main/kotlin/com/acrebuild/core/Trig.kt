package com.acrebuild.core

/**
 * `j.b(int)` (j.java:340) — the fixed-point sin lookup. The quadrant-mirror
 * logic is proven verbatim; `T[]` itself is the quarter-wave 8.8 table the
 * original deserializes from pack-2 (`j.a("/2")` → `f(0)`, j.java:311).
 * Not yet extracted, so `T[k]` is generated as `round(sin(πk/128)·256)`
 * (high-confidence — the standard 8.8 quarter-sin; a serialized-table
 * extraction can replace it later without touching consumers).
 * Constants (j.java:1776-1781, proven): m=256, n=64, W=128, o=192, X=256.
 */
object Trig {
    const val M = 256          // j.m — table scale (full circle = 256)
    const val N = 64           // j.n — 90° index
    private const val W = 128  // j.W — 180° index
    private const val O = 192  // j.o — 270° index
    private const val X = 256  // j.X — 360° index / wrap modulus
    private val T = IntArray(N + 1) {
        (StrictMath.sin(it * StrictMath.PI / 128.0) * 256.0 + 0.5).toInt()
    }

    /** `j.b(int)` — wraps |a| to [0,255] and mirrors the quarter table. */
    fun sin(a: Int): Int {
        val r = (if (a < 0) -a else a) and (X - 1)
        return when {
            r <= N -> T[r]
            r < W -> -T[W - r]
            r <= O -> -T[r - W]
            else -> T[X - r]
        }
    }
}
