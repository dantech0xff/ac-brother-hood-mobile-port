package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 328 — Run-27 rare-menu sweep fixes.
 *
 * Device sweep (Run-27) found the rare menu screens had real defects:
 *   - high-score stamps wrote `kBA[si] = i4` — the original's
 *     `a(bA,81+(au<<4)+(aj<<1),(short)i4)` (k.java:3403) is a LE-16
 *     write at BYTE offset si, so the hi byte must land in the next
 *     slot; the old write silently truncated every score ≥256.
 *   - `menuFooter()` had no jc3/jc15/jc22 cases and those states bypass
 *     `menuQ` → pill hit-zones never armed → dead-end screens.
 *   - `bU` lacked strings 62/70/114-116 (MENU / SOUND SET / medal names).
 *   - eA[4]'s 8 option rows sit in ONE column (the `(bv!=4&&j.c!=14)`
 *     two-col split exclusion, k.java:5942 — shipped quirk) so rows 5-7
 *     land off the 240 canvas; the touch-only port gets a drag-scroll
 *     affordance (`menuScrollDy`) to reach them.
 */
class Slice328Test {

    // -- score byte-view ------------------------------------------------

    @Test
    fun `score write at odd byte offset straddles kBA slots`() {
        val w = world(aj = 0)
        // si for (au=0, aj=0) = 81 — a byte offset; the old `kBA[si]=i4`
        // dropped the high byte for scores ≥256.
        w.baShortPut(81, 0x1234)
        assertEquals(0x1234, w.scoreAt(81))
        // LE-16 at byte offset 81 under index-preserving kBA: slot 81
        // holds the low byte, slot 82 the high byte.
        assertEquals(0x34, w.kBA[81], "byte 81 → kBA[81] = lo")
        assertEquals(0x12, w.kBA[82], "byte 82 → kBA[82] = hi")
    }

    @Test
    fun `score table round-trips all 24 slots`() {
        val w = world(aj = 0)
        // layout (k.java:3403): 81+(au<<4)+(aj<<1) for au in 0..2, aj 0..7
        for (au in 0 until 3) for (aj in 0 until 8) {
            w.baShortPut(81 + (au shl 4) + (aj shl 1), 100 + au * 10 + aj)
        }
        for (au in 0 until 3) for (aj in 0 until 8) {
            assertEquals(100 + au * 10 + aj,
                         w.scoreAt(81 + (au shl 4) + (aj shl 1)),
                         "slot au=$au aj=$aj")
        }
    }

    // -- bU strings -------------------------------------------------------

    @Test
    fun `bU carries the strings Run-27 needed`() {
        val w = world(aj = 0)
        assertEquals("MENU", w.d0(62))
        assertEquals("SOUND SET", w.d0(70))
        assertEquals("OK", w.d0(79))
        assertEquals("INCREDIBLE\nASSASSIN", w.d0(114))
        assertEquals("HARDCORE", w.d0(115))
        assertEquals("BLOOD KILLER", w.d0(116))
    }

    // -- menuFooter cases -------------------------------------------------

    @Test
    fun `menuFooter jc3 options OK and conditional BACK`() {
        val w = world(aj = 0)
        w.stateL(3)
        w.kBv = 1
        assertEquals("OK" to "BACK", w.menuFooter() as Pair<*, *>)
        w.kBv = 0
        assertEquals("OK" to "", w.menuFooter() as Pair<*, *>)
    }

    @Test
    fun `menuFooter jc15 win-stats NEXT and MENU below last mission`() {
        val w = world(aj = 0)
        w.stateL(15); w.kAj = 0
        assertEquals("NEXT" to "MENU", w.menuFooter() as Pair<*, *>)
        w.kAj = 7
        assertEquals("NEXT" to "", w.menuFooter() as Pair<*, *>)
    }

    @Test
    fun `menuFooter jc22 medal browse is BACK-only`() {
        val w = world(aj = 0)
        w.stateL(22)
        assertEquals("" to "BACK", w.menuFooter() as Pair<*, *>)
    }

    // -- overflow scroll (defensive affordance) ---------------------------
    // With the verbatim split only bv==4 menus go two-column — those fit
    // (8 rows → 2×4) — so no shipped menu overflows; the machinery is
    // exercised here on an artificial tall single-column menu.

    @Test
    fun `tall single-column menu overflows the canvas`() {
        val w = world(aj = 0)
        w.stateL(3); w.kBv = 0; w.kEy = 8
        assertTrue(w.menuScrollMax() > 0,
                   "8 single-column rows pass 235px — scroll needed")
    }

    @Test
    fun `menuScrollDy shifts row rects and clamps at max`() {
        val w = world(aj = 0)
        w.stateL(3); w.kBv = 0; w.kEy = 8
        val base = w.menuRowRects().map { it[1] }
        val max = w.menuScrollMax()
        w.menuScrollDy = max
        val scrolled = w.menuRowRects().map { it[1] }
        for (i in base.indices)
            assertEquals(base[i] - max, scrolled[i], "row $i shifted by -max")
        w.menuScrollDy = max + 50               // over-scroll clamps
        val clamped = w.menuRowRects().map { it[1] }
        assertEquals(scrolled, clamped, "offset clamps at menuScrollMax")
        // last row becomes reachable (its rect enters the canvas)
        assertTrue(clamped.last() in 0..235,
                   "last row bottom-anchored inside the view")
    }

    @Test
    fun `non-overflowing menus keep the verbatim layout`() {
        val w = world(aj = 0)
        w.stateL(3); w.kBv = 1; w.kEy = 3         // 3-row menu — fits
        assertEquals(0, w.menuScrollMax())
        w.menuScrollDy = 99                     // clamped to 0
        val a = w.menuRowRects().map { it[1] }
        w.menuScrollDy = 0
        assertEquals(a, w.menuRowRects().map { it[1] })
    }
}
