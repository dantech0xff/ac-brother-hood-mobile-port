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

/** Slice 156 — `i.y()` wall-contact-on-travel-side (i.java:918-927) wired
 *  into the a() push-out arm (i.java:749-758): the push only fires when
 *  the player is NOT wall-blocked on the side they're travelling toward;
 *  `a(true)` rescan + `ag=0` run on every overlapping tick either way. */
class Slice156Test {

    @Test fun `wallOnFacingSide picks bb for left travel bc otherwise`() {
        val e = Entity(41, null)
        e.bb = true; e.bc = false
        e.ag = -3                                        // moving left
        assertTrue(e.hitWall())
        e.ag = 0; e.av = true                            // stopped facing left
        assertTrue(e.hitWall())
        e.ag = 0; e.av = false                           // stopped facing right
        assertFalse(e.hitWall())
        e.ag = 4
        assertFalse(e.hitWall())
        e.bc = true
        assertTrue(e.hitWall())                 // bc read for right
    }

    private fun propAt(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(41, w.clips[7])
        e.setPositionPx(x, y); e.refreshBoxes()
        e.S = 3
        w.npcs.add(e)
        return e
    }

    @Test fun `pushOut skips push when player wall-blocked on travel side`() {
        val w = world(); w.npcs.clear()
        val e = propAt(w, 300, 150)
        val p = w.player
        p.setPositionPx(e.ak - 5, e.al); p.refreshBoxes() // overlap, ak < e.ak
        p.ag = 0; p.av = true; p.bb = true               // wall on the left
        val x0 = p.ak
        w.npcFsm.tickKnockable(e, w, p)
        assertEquals(x0, p.ak)                           // no push
        assertEquals(0, p.ag)                            // L63 still zeroes
        assertTrue(p.v)                                  // a(true) ran
    }

    @Test fun `pushOut pushes unblocked player to volume edge`() {
        val w = world(); w.npcs.clear()
        val e = propAt(w, 300, 150)
        val p = w.player
        p.setPositionPx(e.ak - 5, e.al); p.refreshBoxes()
        p.ag = 0; p.av = true; p.bb = false              // no wall
        val pw = p.W[2] - p.W[0]; val ew = e.W[2] - e.W[0]
        w.npcFsm.tickKnockable(e, w, p)
        assertEquals(e.ak - pw / 2 - ew / 2, p.ak)
        assertEquals(0, p.ag)
        assertTrue(p.v)
    }
}
