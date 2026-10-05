package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 390 — `i(short[])` binds `aa = k.r(table[rec])` per record
 * (i.javap.txt ctor @270-470): ax67 → `k.bk[r8[7]]`, ax46 → `k.bl[r8[10]]`,
 * ax7 → `k.bm[r8[8]]`, ax56 → `k.bj[r8[7]]`, ax9 → `k.bn[r8[8]]`, else
 * `k.bi[ax]` when != -1; `k.r(n)` is null for an entry pack 3 lacks.
 * The port bound one clip per type and had not converted pack-3 entries
 * 24/34/37/41/65/66/67/69/72, so 230 decor records (mission 5 mostly), the
 * ax7 mouths aw=12/30 and the ax9 pair spawned on the wrong sprite or none.
 */
class Slice390Test {
    // k.<clinit> tables (the interpreter's result, retyped here as the spec)
    private val bi = intArrayOf(0, -1, 1, 2, 3, 1, 4, 60, 5, 47, 6, 7, 8, 61, 9, 25, 10, 7, -1, 11,
        -1, 13, 14, 7, 40, 16, 15, 48, -1, 52, 36, 44, 36, -1, 42, 62, -1, -1, -1, -1, 45, 30, -1,
        31, 32, 33, 29, 7, 13, -1, 7, 28, -1, -1, 19, -1, 19, -1, 20, -1, 21, 71, -1, -1, 22, -1,
        23, -1, 26, 38, 43, -1, 51, 7, 54, 55, 56, -1, 63, 0, 57)
    private val bj = intArrayOf(19, 68)
    private val bk = intArrayOf(24, 27, 27, 27, 34, 35, 37, 41, 64, 64, 65, 67, 49, 69, 70)
    private val bl = intArrayOf(29, 0)
    private val bm = intArrayOf(60, 66)
    private val bn = intArrayOf(47, 72)
    /** pack-3 entries present in reconstructed-project/resources/decoded/pack-3. */
    private val pack3 = setOf(0, 1, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 21, 23,
        24, 25, 26, 27, 28, 29, 30, 31, 32, 34, 35, 36, 37, 38, 39, 40, 41, 42, 44, 45, 46, 47, 48,
        50, 51, 52, 54, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 69, 71, 72, 73, 74)

    private fun expected(f: IntArray): Int? {
        val i = when (f[0]) {
            67 -> bk.getOrNull(f[7])
            46 -> bl.getOrNull(f[10])
            7 -> bm.getOrNull(f[8])
            56 -> bj.getOrNull(f[7])
            9 -> bn.getOrNull(f[8])
            else -> bi.getOrNull(f[0])?.takeIf { it >= 0 }
        }
        return i?.takeIf { it in pack3 }
    }

    @Test fun `entityClipIndex follows the per-type tables`() {
        fun rec(ax: Int, vararg kv: Pair<Int, Int>): IntArray {
            val f = IntArray(22); f[0] = ax
            for ((k, v) in kv) f[k] = v
            return f
        }
        assertEquals(60, Level0World.entityClipIndex(7, rec(7, 8 to 0)))
        assertEquals(66, Level0World.entityClipIndex(7, rec(7, 8 to 1)))
        assertEquals(47, Level0World.entityClipIndex(9, rec(9, 8 to 0)))
        assertEquals(72, Level0World.entityClipIndex(9, rec(9, 8 to 1)))
        assertEquals(29, Level0World.entityClipIndex(46, rec(46, 10 to 0)))
        assertEquals(0, Level0World.entityClipIndex(46, rec(46, 10 to 1)))
        assertEquals(19, Level0World.entityClipIndex(56, rec(56, 7 to 0)))
        assertEquals(68, Level0World.entityClipIndex(56, rec(56, 7 to 1)))
        assertEquals(34, Level0World.entityClipIndex(67, rec(67, 7 to 4)))
        assertEquals(69, Level0World.entityClipIndex(67, rec(67, 7 to 13)))
        assertNull(Level0World.entityClipIndex(67, rec(67, 7 to 15)), "out of range: the original would throw")
        assertEquals(7, Level0World.entityClipIndex(11, rec(11)))
        assertNull(Level0World.entityClipIndex(42, rec(42)), "bi[42] = -1")
        assertNull(Level0World.entityClipIndex(71, rec(71)), "bi[71] = -1 (the old map had a phantom 71 -> 26)")
        assertNull(Level0World.ENTITY_CLIP[71])
        assertEquals(26, Level0World.ENTITY_CLIP[68])
    }

    @Test fun `every shipped record binds the table clip and the app has the art for it`() {
        var decor = 0; var alt = 0
        for (aj in 0..7) {
            val w = world(aj = aj)
            var n = 0
            for (f in w.level.entities) {
                if (f.isEmpty() || f[0] == 0 || f[0] == 25 || f[0] == 55 || f.size < 7) continue
                val e = w.npcs[n++]
                val idx = expected(f)
                if (idx == null) assertNull(e.clip, "aj$aj ax${f[0]} aw=${f[1]} has no pack-3 clip")
                else assertSame(w.clips[idx], e.clip, "aj$aj ax${f[0]} aw=${f[1]} clip$idx")
                if (f[0] == 67 && idx != null && idx in intArrayOf(24, 34, 37, 41, 65, 67, 69)) decor++
                if ((f[0] == 7 && f[8] == 1) || (f[0] == 9 && f[8] == 1)) alt++
            }
        }
        assertEquals(230, decor, "the decor records that used to spawn invisible")
        assertEquals(6, alt, "ax7 aw=12/30/… and ax9 with the second clip")
    }

    @Test fun `the level-0 mouths take clip 66 and the clip sets their capture box`() {
        val w = world()
        val m = w.npcs.filter { it.ax == 7 && it.aw in intArrayOf(12, 30) }
        assertEquals(2, m.size)
        for (e in m) {
            assertSame(w.clips[66], e.clip)
            assertNotNull(e.clip)
        }
        assertEquals(listOf(1392, 472, 1408, 488), m.first { it.aw == 12 }.W.toList())
    }
}
