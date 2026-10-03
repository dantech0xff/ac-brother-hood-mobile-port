package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 358 — two helpers made verbatim while porting `aC()` (G5):
 *  - `v()`'s overlaps are `i.a(int[],int[])` (structured/i.java:540-558):
 *    a point box never overlaps, so an entity whose `Y` collapsed to a
 *    point is not in play;
 *  - `aG()` (structured/i.java:7258-7274) is one function: an ax11 riding
 *    a crate (`s.ax==51`) tests the crate's facing edge, any other support
 *    means no edge — the ax11 copy only had the tile test.
 */
class Slice358Test {
    @Test fun `a point Y box is not in play`() {
        val w = world()
        val e = Entity(11, null)
        e.setPositionPx(w.camX + 200, w.camY + 120)
        intArrayOf(e.ak - 10, e.al - 40, e.ak + 10, e.al).copyInto(e.Y)
        e.i = 99
        assertTrue(e.inPlayV(w))
        intArrayOf(e.ak, e.al, e.ak, e.al).copyInto(e.Y)          // collapsed
        assertFalse(e.inPlayV(w), "i.a rejects a point box")
    }

    @Test fun `an ax11 chasing on a crate stops at the crate edge`() {
        val w = world(); w.npcs.clear()
        val crate = Entity(51, null)
        intArrayOf(4000, 980, 4100, 1000).copyInto(crate.W)
        val e = Entity(11, w.clips[7])
        e.setPositionPx(4090, 980); e.aB = 300; e.k = true
        e.setAnim(4); e.av = false                                // facing east
        e.s = crate
        e.refreshBoxes()
        w.npcs += e
        val p = w.player
        p.setPositionPx(4300, 980); p.refreshBoxes()              // chase east
        w.npcFsm.tick(e, p)
        assertEquals(3, e.S, "aG(): within 20px of the crate's east edge → i(k?3:2)")

        // mid-crate: riding a support → no edge, whatever the tiles say
        val w2 = world(); w2.npcs.clear()
        val e2 = Entity(11, w2.clips[7])
        e2.setPositionPx(4040, 980); e2.aB = 300; e2.k = true
        e2.setAnim(4); e2.av = false; e2.s = crate
        e2.refreshBoxes()
        w2.npcs += e2
        w2.player.setPositionPx(4300, 980); w2.player.refreshBoxes()
        w2.npcFsm.tick(e2, w2.player)
        assertEquals(4, e2.S, "aG() false on the crate → the chase goes on")
    }
}
