package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 352 — `j`'s math tables verbatim from archive `/16`.
 *
 * `k` boots the tables with `j.a("/16", 0, 1)` (structured/k.java:4009):
 * `T = f(0)`, `U = f(1)` (structured/j.java:303-307). Entry 0 is a
 * **cosine** quarter table, so `j.b(θ)` = cos θ; the port had generated
 * a sine table, which put every orbit, throw, rope and projectile a
 * quarter turn off. Entry 1's `U[0]` is 0, not the 256 the port read
 * one byte early. `j.b(x,y)` is the `V[]` octant atan, not a rounded
 * float atan2 (structured/j.java:371-416).
 */
class Slice352Test {
    /** `j.a(String)` + `j.m(int)` + `j.f(int)` over archive /16: `af`
     *  entries, `ah` parts, `ai[ah]`, the `ag[af+1]` absolute offsets;
     *  each entry is a marker byte (< 127 → stored) and a typed object
     *  (structured/j.java:637-655, :685-736, :803-814, :988-1062). */
    private fun archive16(): List<IntArray> {
        val b = java.io.File("../../reconstructed-project/resources/archive/16").readBytes()
        fun u8(i: Int) = b[i].toInt() and 255
        fun u16(i: Int) = u8(i) or (u8(i + 1) shl 8)
        fun s32(i: Int) = u16(i) or (u16(i + 2) shl 16)
        val af = u16(0)
        val ah = u16(2)
        assertEquals(2, af, "two entries: T and U")
        assertEquals(1, ah, "one part")
        val agAt = 4 + 2 * ah
        val ag = IntArray(af + 1) { s32(agAt + 4 * it) }
        assertEquals(b.size, ag[af], "the last offset closes the file")
        return (0 until af).map { e ->
            var q = ag[e]
            assertTrue(u8(q) < 127, "marker < 127: stored, not LZMA"); q++
            val hdr = u8(q); q++
            assertEquals(2, hdr and 7, "type int[]")
            assertEquals(1, hdr shr 4, "16-bit elements")
            val cnt = if ((hdr and 8) != 0) u16(q).also { q += 2 } else u8(q).also { q++ }
            IntArray(cnt) { u16(q + 2 * it).toShort().toInt() }.also {
                q += 2 * cnt
                assertEquals(ag[e + 1], q, "entry $e fills its slot")
            }
        }
    }

    @Test fun `T and U are archive 16 verbatim`() {
        val (t, u) = archive16()
        assertContentEquals(t, Trig.T)
        assertContentEquals(u, Trig.U)
    }

    @Test fun `T is the trunc cosine quarter table and U the floor sqrt table`() {
        assertEquals(65, Trig.T.size)
        for (i in 0..64) {
            assertEquals((256.0 * StrictMath.cos(StrictMath.PI * i / 128.0)).toInt(), Trig.T[i], "T[$i]")
        }
        assertEquals(256, Trig.U.size)
        for (i in 0..255) {
            assertEquals((16.0 * StrictMath.sqrt(i.toDouble())).toInt(), Trig.U[i], "U[$i]")
        }
        assertEquals(0, Trig.U[0], "the old U[0]=256 was the count's high byte")
    }

    @Test fun `j b is cos over the whole circle`() {
        assertEquals(256, Trig.cos(0))
        assertEquals(0, Trig.cos(Trig.N))
        assertEquals(-256, Trig.cos(128))
        assertEquals(0, Trig.cos(Trig.O))
        assertEquals(181, Trig.cos(32))
        assertEquals(-181, Trig.cos(96))
        assertEquals(-181, Trig.cos(160))
        assertEquals(181, Trig.cos(224))
        for (a in -600..600) {
            assertEquals(Trig.cos(a), Trig.cos(-a), "even at $a")
            assertEquals(Trig.cos(a), Trig.cos(a + 256), "periodic at $a")
        }
        // j.b(n − θ) is sin θ: 0 at θ=0, +256 at θ=64, −256 at θ=192
        assertEquals(0, Trig.cos(Trig.N - 0))
        assertEquals(256, Trig.cos(Trig.N - 64))
        assertEquals(-256, Trig.cos(Trig.N - 192))
    }

    @Test fun `j c is tan with MAX_VALUE where cos is 0`() {
        assertEquals(0, Trig.tan(0))
        assertEquals(256, Trig.tan(32))
        assertEquals(Int.MAX_VALUE, Trig.tan(64))
        assertEquals(Int.MAX_VALUE, Trig.tan(192))
        assertEquals(-256, Trig.tan(96))
    }

    @Test fun `j b x y is the V table octant atan`() {
        // (x, y) → j.b(x, y); the port writes it atan2(y, x).
        val vectors = listOf(
            intArrayOf(1, 0, 0), intArrayOf(0, 1, 64), intArrayOf(-1, 0, 128),
            intArrayOf(0, -1, 192), intArrayOf(0, 0, 0),
            intArrayOf(1, 1, 32), intArrayOf(-1, 1, 96), intArrayOf(-1, -1, 160),
            intArrayOf(1, -1, 224),
            intArrayOf(100, 3, 1), intArrayOf(3, 100, 63), intArrayOf(-100, 3, 127),
            intArrayOf(-3, 100, 65), intArrayOf(-100, -3, 129), intArrayOf(-3, -100, 191),
            intArrayOf(100, -3, 255), intArrayOf(3, -100, 193),
            intArrayOf(256, 100, 15), intArrayOf(-50, 70, 89), intArrayOf(7, -9, 219),
            intArrayOf(12345, 6789, 20),
            // X − V[0]: the original returns 256, not 0, just below +x
            intArrayOf(1000, -1, 256), intArrayOf(-1, -1000, 192)
        )
        for ((x, y, want) in vectors) {
            assertEquals(want, Trig.atan2(y, x), "j.b($x, $y)")
        }
    }

    @Test fun `V is the floor-side first octant atan`() {
        // V[k] = the angle whose tan brackets k/256 (j.java:359-377)
        assertEquals(0, Trig.atan2(5, 256), "V[5] = 0")
        assertEquals(1, Trig.atan2(6, 256), "V[6] = 1")
        assertEquals(10, Trig.atan2(64, 256), "V[64]")
        assertEquals(18, Trig.atan2(128, 256), "V[128]")
        assertEquals(26, Trig.atan2(192, 256), "V[192]")
        assertEquals(31, Trig.atan2(255, 256), "V[255]")
        assertEquals(32, Trig.atan2(256, 256), "V[256]")
    }

    @Test fun `j d reads U verbatim so d(0) is 0`() {
        assertEquals(0, Trig.sqrt(0))
        assertEquals(1, Trig.sqrt(1))
        assertEquals(16, Trig.sqrt(256))
        assertEquals(1020, Trig.sqrt(1048575))
        assertEquals(46080, Trig.sqrt(Int.MAX_VALUE))
    }

    // -- consumers -----------------------------------------------------

    @Test fun `at() hangs the rider straight below the pivot at cy = o`() {
        val p = Entity(0, null)
        p.cH = 300; p.cI = 200; p.cB = 75; p.cy = Trig.O
        p.orbitPosition()                       // g.at(), g.java:4753-4758
        assertEquals(300, p.ak, "no sideways offset")
        assertEquals(275, p.al, "cB below the pivot")
    }

    @Test fun `c() angle and at() put the rider back where the lunge started`() {
        // g.c(): cz = target.x − X0, cA = target.cy − X1, cB = h(cz,cA),
        // cy = j.b(−cz, cA) (g.java:4615-4626); at the arc end au() pins
        // (cH,cI) on the target and at() must reproduce the start point.
        val starts = listOf(100 to 200, 260 to 140, 180 to 320, 330 to 260)
        val tx = 220; val ty = 230
        for ((px, py) in starts) {
            val p = Entity(0, null)
            val cz = tx - px; val cA = ty - py
            p.cB = p.h(cz, cA)
            p.cy = Trig.atan2(cA, -cz)
            p.cH = tx; p.cI = ty
            p.orbitPosition()
            // k.h overestimates by up to ~6% and the tables truncate, so
            // allow dist/8 + 2; a quarter-turn error would be ~1.4·dist.
            val tol = p.cB / 8 + 2
            assertTrue(Math.abs(p.ak - px) <= tol && Math.abs(p.al - py) <= tol,
                "start ($px,$py) → orbit (${p.ak},${p.al}), tol $tol")
        }
    }

    @Test fun `ax13 rope rests hanging down with bP 0`() {
        // aW() integrates only while bO|bP != 0 (i.java:13238-13241);
        // the rope init leaves bP = 0 (i.java:662/1916).
        val w = world()
        for (sel in 0..3) {                      // r8[4] → aG {0,1,2,4}
            val e = Entity(13, null)
            e.setPositionPx(100, 100)
            val f = mutableListOf(13, 0, 100, 100, sel, 0, 0)
            for (i in 7..15) f += 0
            w.npcFsm.initAx13(e, f)
            assertEquals(0, e.bP, "aG=${e.aG}: no invented start angle")
            assertEquals(0, e.bO)
        }
        // with j.b = cos the restoring torque at θ=0 is 0 and at θ>0 negative
        assertEquals(0, Trig.cos(Trig.N - 0) shl 1)
        assertTrue((Trig.cos(Trig.N - 20) shl 1) > 0, "bO -= 2·sin θ pulls back")
    }
}
