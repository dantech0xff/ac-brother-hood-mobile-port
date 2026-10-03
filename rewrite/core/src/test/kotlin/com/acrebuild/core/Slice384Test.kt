package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 384 — leftovers from slice 371:
 * - `bB()` S28 (i.javap.txt bB() @438-537): an `ae` that is not a 71 is
 *   released and replaced by a fresh 71 (`aS.G(); aS.a(71,ak,al)`), then
 *   pinned — no fall-through to the shove check.
 * - `i.p()` (i.java:214-227) is one function: zero boxes, `ab = null`,
 *   `ad.p()`, `ae = af = c = null`, then `aS()` (the `cr` pool drop).
 * - `u()` has one implementation.
 */
class Slice384Test {
    @Test fun `S28 replaces a foreign ae with a pinned 71`() {
        val w = world()
        val e = Entity(67, null)
        w.npcFsm.initDecor(e, listOf(67, 903, 500, 500, 0, 28, 0, 5, 0))
        e.setPositionPx(500, 500)
        intArrayOf(490, 480, 510, 520).copyInto(e.W)
        intArrayOf(490, 480, 510, 520).copyInto(e.X)
        w.player.setPositionPx(e.X[0] + 1, (e.X[1] + e.X[3]) / 2)
        w.player.refreshBoxes()
        val foreign = w.spawnPickup(40, 0, 0)
        w.player.ae = foreign
        val s0 = w.player.S
        w.npcFsm.tickDecor(e, w.player)
        val ae = w.player.ae!!
        assertNotSame(foreign, ae)
        assertEquals(71, ae.S, "aS.a(71, ak, al) (@467-480)")
        assertEquals(0, foreign.W[0], "aS.G() released the old one")
        val edge = if (w.player.ak - ((e.W[0] + e.W[2]) shr 1) >= 0) 20 else 380
        assertEquals(w.camX + edge, ae.ak)
        assertEquals(s0, w.player.S, "no shove (i(309)) on this path")
    }

    @Test fun `i-p() releases ab and c and drops the cr pool`() {
        val w = world()
        val e = Entity(11, w.clips[7])
        val ab = Entity(14, null).apply { intArrayOf(1, 2, 3, 4).copyInto(W) }
        val ad = Entity(14, null).apply { intArrayOf(1, 2, 3, 4).copyInto(W) }
        e.ab = ab; e.ad = ad; e.c = Entity(51, null)
        e.cr = arrayOf(arrayOf(Entity(11, null)))
        intArrayOf(5, 6, 7, 8).copyInto(e.W)
        e.deactivate()
        assertNull(e.ab); assertNull(e.ad); assertNull(e.c); assertNull(e.cr)
        assertEquals(0, e.W[2])
        assertEquals(0, ad.W[2], "ad.p() cascades")
        assertEquals(4, ab.W[3], "ab is dropped, not released")
    }

    @Test fun `u() has one implementation`() {
        val w = world()
        val e = Entity(21, null).apply { setPositionPx(w.camX + 2000, w.camY + 900) }
        e.offscreenScore(w)
        val a = e.au
        e.recomputeAu(w.camX, w.camY, w::kBk)
        assertEquals(a, e.au)
        assertTrue(a > 0)
    }
}
