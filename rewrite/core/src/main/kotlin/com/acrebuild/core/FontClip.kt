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
        drawChars(str, i4, i5, 0, str.length, drawGlyph)
    }

    /** Shared char-cursor loop over `str[start,end)` — used by `draw`
     *  and by `drawWrapped` per line. Escape state (`underline`,
     *  `bold`, `palette`) persists across calls exactly like the
     *  orig's instance fields (`proven` — :1721 keeps them live
     *  between its per-line `a()` calls). */
    fun drawChars(
        str: String, i4: Int, i5base: Int, start: Int, end: Int,
        drawGlyph: (glyph: Int, x: Int, y: Int, palette: Int) -> Unit,
    ) {
        val saved = palette                              // H = aH; restore at end (z=true path)
        var i6 = i4
        var i7 = i5base
        var idx = start
        while (idx < end) {
            val c2 = str[idx]
            if (c2 == '\\') {
                idx++
                if (idx >= end) break
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
                        if (idx >= end) break
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

    /** `k(i)` (b.java:1609, proven): `i*J + (i-1)*K` — pixel height of
     *  i wrapped lines. */
    fun linesHeight(i: Int): Int = i * baseJ + (i - 1) * baseK

    /** `a(String, w, false)` (b.java:1618-1760, proven): word-wrap —
     *  returns the `U[]` line table: `U[0]` = line count, then
     *  `{endIndex, width}` pairs at `U[1],U[2]`, `U[3],U[4]`, …
     *  (`V` = previous line's end, `W` = this line's end). */
    fun wrap(str: String, w: Int): IntArray {
        val u = IntArray(250)
        var s2 = 0                                // running line width
        var s3 = 1                                // write cursor
        var s4 = 0                                // last space index
        var z2 = underline
        var z3 = false
        var s5 = 0                                // carried-word width
        var z4 = bold
        var iS = 0
        var i2 = 0
        val length = str.length
        while (i2 < length) {
            val c = str[i2]
            if (c == ' ') {
                s2 += spaceL; s4 = i2; z2 = z4; z3 = true; s5 = 0
                if (s2 > w) {
                    z3 = false
                    var i3 = s4
                    while (i3 >= 0 && str[i3] == ' ') { s2 -= spaceL; i3-- }
                    while (s4 < length && str[s4] == ' ') s4++
                    s4--
                    i2 = s4
                    z4 = z2
                    u[s3] = s4 + 1; s3++; u[s3] = s2; s3++
                    s2 = 0
                }
            } else if (c == '\\') {
                i2++
                if (i2 < length && str[i2] == '^') z4 = !z4
            } else if (c == '\n') {
                u[s3] = i2; s3++; u[s3] = s2; s3++
                s2 = 0; s5 = 0
            } else {
                if (c.code >= ' '.code) {
                    iS = s(c.code)
                } else if (c.code == 1) {
                    i2++
                } else if (c.code == 2) {
                    i2++
                    iS = if (i2 < length) str[i2].code else 0
                }
                if (iS > clip.objPlaceStart.size) iS = 0   // c()
                var i4 = advanceOf(iS)
                if (z4) i4++
                s5 += i4
                s2 += i4
                if (s2 > w && z3) {
                    z3 = false
                    var i5 = s4
                    while (i5 >= 0 && str[i5] == ' ') { s2 -= spaceL; i5-- }
                    u[s3] = s4 + 1; s3++; u[s3] = s2 - s5; s3++
                    s2 = 0; i2 = s4; z4 = z2
                }
            }
            i2++
        }
        val s11 = s3 + 1                        // verbatim tail (:1748)
        u[s3] = length; u[s11] = s2
        u[0] = (s11 + 1) / 2
        return u
    }

    /** `a(Graphics, str, U, x, y, i3, i4, i5, i6)` (b.java:1721-1769,
     *  proven): wrapped-lines renderer — draws `i4` lines starting at
     *  line `i3` (i4=-1 → to the end, clamped), align bits on each
     *  line's own width, shared escape state across lines. `limit` =
     *  `i6`: char budget — chars past `firstLineStart + limit` are
     *  clipped (the `X = s3 + i6` cap at :1736-1739) — the dialog
     *  typewriter's `bT`. */
    fun drawWrapped(
        str: String, u: IntArray, x: Int, yIn: Int, i3: Int, i4in: Int,
        align: Int, limit: Int = -1,
        drawGlyph: (glyph: Int, x: Int, y: Int, palette: Int) -> Unit,
    ) {
        var y = yIn
        val s2 = u[0]
        var i4 = i4in
        var i9 = i3
        if (i4 == -1) i4 = s2
        if (i9 + i4 > s2) i4 = s2 - i9
        val i8 = baseK + baseJ
        if ((align and 32) != 0) y -= i8 * (i4 - 1)
        else if ((align and 2) != 0) y -= (i8 * (i4 - 1)) shr 1
        val xEnd = if (limit >= 0)
            (if (i3 > 0) u[((i3 - 1) shl 1) + 1] else 0) + limit
        else Int.MAX_VALUE                                        // (:1736-1739)
        var i10 = 0
        while (i9 < s2 && i10 <= i4 - 1) {
            var v = if (i9 > 0) u[((i9 - 1) shl 1) + 1] else 0
            val w = minOf(u[(i9 shl 1) + 1], xEnd)
            if (v < str.length && str[v] == '\n') v++
            var i11 = x
            var i12 = y + i10 * i8
            if ((align and 43) != 0) {
                if ((align and 8) != 0) i11 -= u[(i9 + 1) shl 1]
                else if ((align and 1) != 0) i11 -= u[(i9 + 1) shl 1] shr 1
                if ((align and 32) != 0) i12 -= baseJ
                else if ((align and 2) != 0) i12 -= baseJ shr 1
            }
            drawChars(str, i11, i12, v, w, drawGlyph)
            i9++; i10++
        }
    }
}
