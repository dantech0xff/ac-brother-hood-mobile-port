package com.acrebuild.core

/**
 * `b`'s bitmap-font path (b.java:1575-1962, proven): the font is a
 * normal clip whose glyph modules are addressed through the `Q`
 * charmap (open hash, `codepoint % bucketCount`) and whose per-module
 * metric rows live in the placement pool: `i(g,0)` =
 * `ar[aj[module]]` (advance) and `j(0,f)` = `as[aj[0]+f]` (module 0's
 * top/bottom/line metrics). Both fonts (`bW = J(1)`, `y = J(3)`)
 * share the one charmap loaded via `j.f(2)` (k.java:3974-3976).
 *
 * acpk mapping: `aj[module]` = `objPlaceStart[module]`; the pool
 * quad is `(ap, aq, ar, as)` so `ar` = slot 2 and `as` = slot 3.
 */
class FontClip(
    private val clip: Clip,
    private val buckets: Array<IntArray>,
    val paletteCap: Int,
) {
    /** `this.N` = `-j(0,0)` baseline offset (b.java:1584). */
    val baseN: Int = -asOf(0, 0)
    /** `this.J` = `N + j(0,1)` line-box top→baseline span (:1585). */
    val baseJ: Int = baseN + asOf(0, 1)
    /** `this.K` = `j(0,2) - j(0,1)` line pitch (:1586). */
    val baseK: Int = asOf(0, 2) - asOf(0, 1)
    /** `this.L` = `i(s(32),0)` space advance (:1587). */
    val spaceL: Int = advanceOf(s(32))

    /** `l(i)` (:1964) — palette/style variant select. */
    var palette = 0
        private set
    private var savedPalette = 0

    /** `l(i)` (:1964, proven): palette slot — ignored when out of range. */
    fun l(i: Int) {
        if (i in 0 until paletteCap) palette = i
    }                     // H/aH
    private var underline = false                    // O  (`\_`)
    private var bold = false                         // f  (`\^`)

    private fun asOf(module: Int, field: Int) =
        clip.placements[(clip.objPlaceStart[module] + field) * 4 + 3]

    private fun advanceOf(glyph: Int) =
        clip.placements[clip.objPlaceStart[glyph] * 4 + 2]

    companion object {
        /** Deserialize the `charmap.bin` emitted by the converter:
         *  u16 bucketCount, then per bucket u16 len + u16
         *  (codepoint, glyph) pairs — the verbatim `Q` table
         *  (base pairs first, overflow appended, duplicates kept). */
        fun loadCharmap(data: ByteArray): Array<IntArray> {
            fun u16(o: Int) =
                (data[o].toInt() and 0xFF) or
                ((data[o + 1].toInt() and 0xFF) shl 8)
            val r = u16(0)
            val buckets = Array(r) { IntArray(0) }
            var off = 2
            for (b in 0 until r) {
                val n = u16(off); off += 2
                val arr = IntArray(n)
                for (i in 0 until n) { arr[i] = u16(off); off += 2 }
                buckets[b] = arr
            }
            return buckets
        }
    }

    /** `s(i)` (:1590, proven): `Q[i%R]` chain — first pair is the base
     *  entry, overflow pairs follow; missing → 1 (fallback glyph). */
    fun s(c: Int): Int {
        val b = buckets.getOrNull(c % buckets.size) ?: return 1
        if (b.isEmpty()) return 1
        if (b[0] == c) return b[1]
        var i = 2
        while (i < b.size && b[i] != c) i += 2
        return if (i >= b.size) 1 else b[i + 1]
    }

    /** `a(str, cArr)` measure pass (:1772-1830, proven): returns
     *  `(d, e)` — widest line width and total height. The verbatim
     *  `if (d > 0) d = d` no-op is dropped (identity). */
    fun measure(str: String): IntArray {
        var d = 0
        var e = baseJ
        var i = 0
        var boldNow = bold
        var idx = 0
        while (idx < str.length) {
            val c2 = str[idx]
            if (c2 == '\\') {
                idx++
                if (idx < str.length && str[idx] == '^') boldNow = !boldNow
            } else {
                var adv: Int
                if (c2.code > ' '.code) {
                    adv = advanceOf(s(c2.code))
                } else if (c2 == ' ') {
                    i += spaceL
                    idx++
                    continue
                } else if (c2 == '\n') {
                    if (i > d) d = i
                    i = 0
                    e += baseK + baseJ
                    idx++
                    continue
                } else if (c2.code == 1) {
                    idx++
                    idx++
                    continue
                } else if (c2.code == 2) {
                    idx++
                    adv = if (idx < str.length) advanceOf(str[idx].code) else 0
                } else {
                    adv = 0
                }
                i += adv
                if (boldNow) i++
            }
            idx++
        }
        if (i > d) d = i
        return intArrayOf(d, e)
    }

    /** `a(Graphics, str, x, y, align)` (:1839-1962, proven): per-char
     *  cursor draw — `drawGlyph(glyph, x, y, palette)` per char (and
     *  the underline/bold dupes). Align bits (mask 43): 8 right
     *  (`-d`), 1 center (`-d>>1`), 32 bottom (`-e`), 2 vcenter
     *  (`-e>>1`). Escapes: `\_` underline toggle, `\^` bold toggle,
     *  `\<digit>` → `l(d)`; char 1 → `l(c)` when `c < paletteCap`,
     *  255 restores `aH`; char 2 → literal glyph embed. */
    fun draw(
        str: String, x: Int, y: Int, align: Int,
        drawGlyph: (glyph: Int, x: Int, y: Int, palette: Int) -> Unit,
    ) {
        val (d, e) = measure(str)
        var i4 = x
        var i5 = y + baseN
        if ((align and 43) != 0) {
            if ((align and 8) != 0) i4 -= d
            else if ((align and 1) != 0) i4 -= d shr 1
            if ((align and 32) != 0) i5 -= e
            else if ((align and 2) != 0) i5 -= e shr 1
        }
        val saved = palette                              // H = aH; restore at end (z=true path)
        var i6 = i4
        var i7 = i5
        var idx = 0
        while (idx < str.length) {
            val c2 = str[idx]
            if (c2 == '\\') {
                idx++
                if (idx >= str.length) break
                when (str[idx]) {
                    '_' -> underline = !underline
                    '^' -> bold = !bold
                    else -> l((str[idx].code and 255) - 48)   // l(digit) — clamps
                }
            } else {
                var i10: Int
                if (c2.code > ' '.code) {
                    i10 = s(c2.code)
                } else when {
                    c2 == ' ' -> {
                        if (underline) {
                            val us = s(95)
                            drawGlyph(us, i6 + ((spaceL - advanceOf(us)) shr 1),
                                      i7, palette)
                        }
                        i6 += spaceL
                        idx++
                        continue
                    }
                    c2 == '\n' -> {
                        i6 = i4
                        i7 += baseK + baseJ
                        idx++
                        continue
                    }
                    c2.code == 1 -> {
                        idx++
                        if (idx >= str.length) break
                        val c4 = str[idx]
                        if (c4.code < paletteCap) palette = c4.code
                        if (c4.code == 255) palette = saved
                        idx++
                        continue
                    }
                    c2.code == 2 -> {
                        idx++
                        i10 = if (idx < str.length) str[idx].code else 0
                    }
                    else -> i10 = 0
                }
                drawGlyph(i10, i6, i7, palette)
                if (underline) {
                    val us = s(95)
                    drawGlyph(us, i6 + ((advanceOf(i10) - advanceOf(us)) shr 1),
                              i7, palette)
                }
                if (bold) {
                    i6++
                    drawGlyph(i10, i6, i7, palette)
                }
                i6 += advanceOf(i10)
            }
            idx++
        }
        palette = saved
    }
}
