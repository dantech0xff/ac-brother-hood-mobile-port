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

/**
 * Slice 84 — `y`/`bW` bitmap font (`b` text path): `s()` charmap,
 * `a(str,cArr)` measure, `a(Graphics,str,...)` draw cursor with
 * escapes/underline/bold/align — against the real font clip and the
 * decoded pack-1 entry-2 charmap.
 */
class Slice84Test {

    private val clip92 by lazy { Clip.load(asset("clips/clip92/clip.acpk")) }
    private val fontY by lazy {
        FontClip(clip92, FontClip.loadCharmap(asset("fonts/charmap.bin")), 4)
    }

    @Test fun `s charmap maps and falls back`() {
        assertEquals(39, fontY.s(65))          // 'A'
        assertEquals(40, fontY.s(66))          // 'B'
        assertEquals(2, fontY.s(48))           // '0'
        assertEquals(91, fontY.s(95))          // '_' (underline glyph)
        assertEquals(97, fontY.s(260))         // overflow chain
        assertEquals(1, fontY.s(0x2600))       // unmapped -> fallback 1
    }

    @Test fun `metrics come from the placement pool`() {
        assertTrue(fontY.baseN >= 0)
        assertTrue(fontY.baseJ > 0)
        assertTrue(fontY.spaceL > 0)
    }

    @Test fun `measure sums advances and stacks lines`() {
        val (d1, e1) = fontY.measure("A")
        val (d2, e2) = fontY.measure("AA")
        assertEquals(d1 * 2, d2)
        assertEquals(e1, e2)
        val (d3, e3) = fontY.measure("A\nA")
        assertEquals(d1, d3)
        assertEquals(e1 + fontY.baseK + fontY.baseJ, e3)
    }

    @Test fun `draw emits one glyph per char with cursor`() {
        val calls = mutableListOf<Triple<Int, Int, Int>>()
        fontY.draw("AB", 100, 50, 0) { g, x, y, _ -> calls += Triple(g, x, y) }
        assertEquals(2, calls.size)
        assertEquals(Triple(39, 100, 50 + fontY.baseN), calls[0])
        assertEquals(40, calls[1].first)
        assertTrue(calls[1].second > calls[0].second)   // cursor advanced
        assertEquals(calls[0].third, calls[1].third)
    }

    @Test fun `draw newline resets x and steps y by K+J`() {
        val calls = mutableListOf<Triple<Int, Int, Int>>()
        fontY.draw("A\nB", 0, 0, 0) { g, x, y, _ -> calls += Triple(g, x, y) }
        assertEquals(2, calls.size)
        assertEquals(0, calls[1].second)
        assertEquals(calls[0].third + fontY.baseK + fontY.baseJ,
                     calls[1].third)
    }

    @Test fun `draw escapes switch palette and styles`() {
        // `\\<digit>` -> l(d)
        val pals = mutableListOf<Int>()
        fontY.draw("A\\3B", 0, 0, 0) { _, _, _, p -> pals += p }
        assertEquals(listOf(0, 3), pals)
        // out-of-range digits are ignored (l() clamps, b.java:1964)
        val palsOob = mutableListOf<Int>()
        fontY.draw("A\\9B", 0, 0, 0) { _, _, _, p -> palsOob += p }
        assertEquals(listOf(0, 0), palsOob)
        // char-1 sets palette from the embedded char code
        val pals2 = mutableListOf<Int>()
        fontY.draw("A\u0001\u0003B", 0, 0, 0) { _, _, _, p -> pals2 += p }
        assertEquals(listOf(0, 3), pals2)
        // bold draws each glyph twice at +1
        val xs = mutableListOf<Int>()
        fontY.draw("\\^AB", 0, 0, 0) { _, x, _, _ -> xs += x }
        assertEquals(4, xs.size)
        assertEquals(xs[0] + 1, xs[1])
    }

    @Test fun `draw align right shifts by -d`() {
        val calls = mutableListOf<Int>()
        fontY.draw("A", 100, 0, 8) { _, x, _, _ -> calls += x }
        val (d, _) = fontY.measure("A")
        assertEquals(100 - d, calls[0])
    }
}
