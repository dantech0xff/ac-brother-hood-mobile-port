package com.acrebuild.core

/**
 * `j`'s fixed-point math (structured/j.java:303-429, proven): the two
 * tables `k` loads from archive `/16` on boot (`j.a("/16", 0, 1)`,
 * structured/k.java:4009 → `T = f(0); U = f(1)`, j.java:303-307) and the
 * functions built on them. Angles are in 256ths of a turn.
 *
 * Archive `/16` (sha256 `2c1e6eb6…963e46`) is a 2-entry pack; both entries
 * are marker-3 typed `int[]`s of LE shorts (headers 0x12 / 0x1A,
 * j.java:988-1062). The tables below are those bytes verbatim —
 * `Slice352Test` re-parses the archive and compares.
 *
 * `T` is a **cosine** quarter table, `trunc(256·cos(πi/128))`, so
 * `j.b(θ)` is cos θ and `j.b(j.n − θ)` is sin θ. (Before slice 352 the
 * port generated a sine table here, which moved every consumer by a
 * quarter turn and broke the mirror arms' signs.)
 *
 * Constants (j.java:89-95, proven): m=256, n=64, W=128, o=192, X=256.
 */
object Trig {
    const val M = 256          // j.m — table scale (full circle = 256)
    const val N = 64           // j.n — 90° index
    private const val W = 128  // j.W — 180° index
    const val O = 192          // j.o — 270° index (swing/orbit band edge)
    private const val X = 256  // j.X — 360° index / wrap modulus
    private const val I = 256  // j.i — V[] resolution

    /** `j.T` — archive /16 entry 0 (offset 18, 65 shorts). */
    internal val T = intArrayOf(
        256, 255, 255, 255, 254, 254, 253, 252, 251, 249, 248, 246, 244,
        243, 241, 238, 236, 234, 231, 228, 225, 222, 219, 216, 212, 209,
        205, 201, 197, 193, 189, 185, 181, 176, 171, 167, 162, 157, 152,
        147, 142, 136, 131, 126, 120, 115, 109, 103, 97, 92, 86, 80,
        74, 68, 62, 56, 49, 43, 37, 31, 25, 18, 12, 6, 0
    )

    /** `j.U` — archive /16 entry 1 (offset 151, 256 shorts):
     *  `floor(16·sqrt(i))`, so `U[0] = 0`. */
    internal val U = intArrayOf(
        0, 16, 22, 27, 32, 35, 39, 42, 45, 48, 50, 53, 55, 57, 59, 61,
        64, 65, 67, 69, 71, 73, 75, 76, 78, 80, 81, 83, 84, 86, 87, 89,
        90, 91, 93, 94, 96, 97, 98, 99, 101, 102, 103, 104, 106, 107, 108, 109,
        110, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126,
        128, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140, 141, 142,
        143, 144, 144, 145, 146, 147, 148, 149, 150, 150, 151, 152, 153, 154, 155, 155,
        156, 157, 158, 159, 160, 160, 161, 162, 163, 163, 164, 165, 166, 167, 167, 168,
        169, 170, 170, 171, 172, 173, 173, 174, 175, 176, 176, 177, 178, 178, 179, 180,
        181, 181, 182, 183, 183, 184, 185, 185, 186, 187, 187, 188, 189, 189, 190, 191,
        192, 192, 193, 193, 194, 195, 195, 196, 197, 197, 198, 199, 199, 200, 201, 201,
        202, 203, 203, 204, 204, 205, 206, 206, 207, 208, 208, 209, 209, 210, 211, 211,
        212, 212, 213, 214, 214, 215, 215, 216, 217, 217, 218, 218, 219, 219, 220, 221,
        221, 222, 222, 223, 224, 224, 225, 225, 226, 226, 227, 227, 228, 229, 229, 230,
        230, 231, 231, 232, 232, 233, 234, 234, 235, 235, 236, 236, 237, 237, 238, 238,
        239, 240, 240, 241, 241, 242, 242, 243, 243, 244, 244, 245, 245, 246, 246, 247,
        247, 248, 248, 249, 249, 250, 250, 251, 251, 252, 252, 253, 253, 254, 254, 255
    )

    /**
     * `j.a(x0,y0,xc,yc,x1,y1,t)` over `j.b(IIIIII)I` (j.javap `a(IIIIIII)V` @0-60 and
     * `b(IIIIII)I` @0-20, raw bytes, proven): the quadratic Bézier in the 256-parameter
     * domain (`t ∈ [0, j.i = 256]`) — `(p0·(i−t)² + 2·pc·(i−t)·t + p1·t²) / 65536` with
     * Java's truncating `idiv`, once for x and once for y. Slice 409: the port weighted the
     * START by `(i−t)·t` and the CONTROL by `(i−t)²` (a mislabelled `b(a,b,c,w1,w2,w3)`:
     * the bytes are `a·w2 + 2·b·w1 + c·w3`), so at t = 0 a flying wisp sat on twice its
     * control point instead of its start; ax61 S8 carried its own 65536-domain copy whose
     * `ti²` overflowed `Int` for the first third of the flight.
     */
    fun bezier(x0: Int, y0: Int, xc: Int, yc: Int, x1: Int, y1: Int, t: Int): IntArray {
        val tt = t * t
        val om = I - t
        val omt = om * t
        val om2 = om * om
        return intArrayOf(
            (x0 * om2 + 2 * xc * omt + x1 * tt) / 65536,
            (y0 * om2 + 2 * yc * omt + y1 * tt) / 65536)
    }

    /** `j.b(int)` (j.java:334-349) — wraps |a| to [0,255] and mirrors
     *  the quarter table: cos θ in 8.8. Call sites write `j.b(θ)` as
     *  `cos(θ)` and `j.b(j.n − θ)` (= sin θ) as `cos(N − θ)`. */
    fun cos(a: Int): Int {
        val r = (if (a < 0) -a else a) and (X - 1)
        return when {
            r <= N -> T[r]
            r < W -> -T[W - r]
            r <= O -> -T[r - W]
            else -> T[X - r]
        }
    }

    /** `j.c(int)` (j.java:351-357) — tan θ in 8.8 from the same table;
     *  `Int.MAX_VALUE` where cos θ is 0. */
    fun tan(a: Int): Int {
        val c = cos(a)
        if (c == 0) return Int.MAX_VALUE
        return (cos(N - a) shl 8) / c
    }

    /** `j.b(int,int,int)` (j.java:359-369) — binary search for the
     *  angle in [lo,hi] whose `tan` brackets `v`. */
    private fun search(lo0: Int, hi0: Int, v: Int): Int {
        var lo = lo0
        var hi = hi0
        while (lo + 1 < hi) {
            val mid = (lo + hi) shr 1
            if (v > tan(mid)) lo = mid else hi = mid
        }
        return if (v >= tan(hi)) hi else lo
    }

    /** `j.V` (j.java:372-377) — the first-octant atan table, built
     *  lazily on first use: `V[k] = search(0, n, k)` for k in 1..256. */
    private val V: IntArray by lazy {
        IntArray(I + 1) { k -> if (k > 0) search(0, N, (k * I) / I) else 0 }
    }

    /**
     * `j.b(int,int)` (j.java:371-416) — atan2 by octant over `V[]`,
     * in [0,255]. The original takes `(x, y)`; the port keeps the
     * `atan2(y, x)` order, so `j.b(a, b)` is written `atan2(b, a)`.
     */
    fun atan2(y: Int, x: Int): Int {
        val v = V
        if (x == 0) {
            if (y > 0) return N
            if (y == 0) return 0
            return O
        }
        if (x > 0) {
            if (y >= 0) {
                if (x >= y) return v[(y * I) / x]
                return N - v[(x * I) / y]
            }
            val ny = -y
            if (x >= ny) return X - v[(ny * I) / x]
            return O + v[(x * I) / ny]
        }
        val nx = -x
        if (y >= 0) {
            if (nx >= y) return W - v[(y * I) / nx]
            return N + v[(nx * I) / y]
        }
        val ny = -y
        if (nx >= ny) return W + v[(ny * I) / nx]
        return O - v[(nx * I) / ny]
    }

    /** `j.d(int)` (j.java:418-429) — piecewise table square root:
     *  indexes `U` by a shifting window, so large inputs quantize
     *  (`d(1048575) = U[255]<<2 = 1020`, not 1023). */
    fun sqrt(x: Int): Int = when {
        x < 0 -> 0
        x < 0x100 -> U[x] shr 4
        x < 0x400 -> U[x shr 2] shr 3
        x < 0x1000 -> U[x shr 4] shr 2
        x < 0x4000 -> U[x shr 6] shr 1
        x < 0x10000 -> U[x shr 8]
        x < 0x40000 -> U[x shr 10] shl 1
        x < 0x100000 -> U[x shr 12] shl 2
        x < 0x400000 -> U[x shr 14] shl 3
        x < 0x1000000 -> U[x shr 16] shl 4
        x < 0x4000000 -> U[x shr 18] shl 5
        x < 0x10000000 -> U[x shr 20] shl 6
        x < 0x40000000 -> U[x shr 22] shl 7
        else -> U[x shr 24] shl 8
    }

    /** `k.h(i,i2)` (k.java:5301-5313, proven): integer hypot —
     *  `(|a|+|b|) − min/2 − min/4 + min/8`. */
    fun khypot(a0: Int, b0: Int): Int {
        if (a0 == 0 && b0 == 0) return 0
        val a = if (a0 < 0) -a0 else a0
        val b = if (b0 < 0) -b0 else b0
        val m = if (a > b) b else a
        return (a + b) - (m shr 1) - (m shr 2) + (m shr 3)
    }
}
