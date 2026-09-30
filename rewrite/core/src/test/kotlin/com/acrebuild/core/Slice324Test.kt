package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Slice 324 — bubble panel height fix (`k.y.k(n)` fidelity).
 *
 * The device capture (Run-23) reproduced a real defect: a 3-line bubble
 * page's bottom line clipped under the panel border. Root cause: the
 * panel height `r02 = k.y.k(q[4]) + 10` used the `dialogAdvance` default
 * `n*10` instead of the real y-font metric `n*J + (n-1)*K` (b.java:1609 —
 * J=14, K=1 on clip-92 → k(3)=44 vs the default's 30). Level0World now
 * overrides `dialogAdvance` through `footerFont.linesHeight`.
 */
class Slice324Test {
    private fun charmap(): ByteArray =
        java.io.File("../generated/fonts/charmap.bin").readBytes()

    @Test
    fun `dialogAdvance returns real y-font line metric`() {
        val w = world(charmap = charmap(), aj = 0)
        assertEquals(14, w.dialogAdvance(1), "k(1) = J = 14")
        assertEquals(29, w.dialogAdvance(2), "k(2) = 2J + K = 29")
        assertEquals(44, w.dialogAdvance(3), "k(3) = 3J + 2K = 44")
    }

    @Test
    fun `bubble panel covers a full 3-line page`() {
        val w = world(charmap = charmap(), aj = 0)
        val guard = w.npcs.first { it.aw == 325 }
        // arm the bubble like op106: strA=strB=0, timer=60, self-target
        guard.cQ = IntArray(10) { -1 }
        guard.cQ!![5] = 23; guard.cQ!![6] = 23     // one string idx
        guard.cQ!![2] = -1; guard.cQ!![3] = 60
        guard.cT = null
        repeat(5) { w.npcFsm.tickBubble(guard, w) }
        val b = w.bubbleDraw
        assertNotNull(b, "bubble should emit a draw descriptor")
        // "WHO ARE YOU? YOU ARE PROHIBITED" wraps to ~2-3 lines at 120px;
        // h must cover lines*J+(lines-1)*K + 10 pad — never undersized.
        val needed = w.dialogAdvance(b.lines) + 10
        assertEquals(needed, b.h, "panel h = k(lines)+10 — bottom line must fit")
        assertTrue(b.h >= 24, "at least one full 14px line + pad")
    }

    @Test
    fun `fallback preserved without font`() {
        val w = world(aj = 0)                     // no charmap → footerFont null
        assertEquals(30, w.dialogAdvance(3), "headless default stays n*10")
    }
}
