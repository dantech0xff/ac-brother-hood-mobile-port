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

class Slice162Test {
    /** An ax37 record on the entity path (slice 355): zone (4900,4900)-
     *  (5100,5100), bound (0,0)-(0,0), mask 15, link mode `cond` on
     *  `uid`, containment mode. */
    private fun tr(w: Level0World, cond: Int, uid: Int): Entity {
        val f = listOf(37, 9000 + cond, 5000, 5000, 0, 0, 0,
            -100, -100, 200, 200, -5000, -5000, 0, 0, 15, cond, uid, 0)
        val e = Entity(37, null).apply { aw = f[1]; setPositionPx(5000, 5000); P = f[6] }
        w.npcFsm.initAx37(e, f)
        w.npcs += e
        return e
    }

    private fun playerAt(w: Level0World, x: Int, y: Int) {
        w.player.setPositionPx(x, y); w.player.refreshBoxes()
    }

    @Test fun `ax37 linkCond3 removes trigger when link dead`() {
        val w = world()
        val t = tr(w, 3, 4242)
        w.npcs += Entity(11, null).apply { aw = 4242; aB = 0 }   // dead link
        playerAt(w, 5000, 5050)                                  // inside the zone
        w.scrollTriggerAl(t)
        assertTrue(t in w.pendingRemove, "k.n() + k.c(this)")
    }

    @Test fun `ax37 linkCond3 tests the zone before the link`() {
        // al() returns at the zone miss (i.java:5749-5763) before it
        // reaches the Z[2] gate: a player outside never triggers removal.
        val w = world()
        val t = tr(w, 3, 4243)
        w.npcs += Entity(11, null).apply { aw = 4243; aB = 0 }
        playerAt(w, 300, 300)
        w.scrollTriggerAl(t)
        assertFalse(t in w.pendingRemove)
    }

    @Test fun `ax37 linkCond3 keeps trigger while link alive`() {
        val w = world()
        val t = tr(w, 3, 4244)
        w.npcs += Entity(11, null).apply { aw = 4244; aB = 1 }
        playerAt(w, 5000, 5050)
        w.scrollTriggerAl(t)
        assertFalse(t in w.pendingRemove)
        assertSame(t, w.kAh, "containment claims k.ah")
    }

    @Test fun `ax37 linkCond1 flagged link skips claim`() {
        val w = world()
        val t = tr(w, 1, 4245)
        w.npcs += Entity(80, null).apply { aw = 4245; P = P or 32 }
        playerAt(w, 5000, 5050)
        w.scrollTriggerAl(t)
        assertNull(w.kAh)                              // gate skipped claim
    }

    @Test fun `ax37 linkCond1 unflagged link allows claim`() {
        val w = world()
        val t = tr(w, 1, 4246)
        w.npcs += Entity(80, null).apply { aw = 4246 }
        playerAt(w, 5000, 5050)
        w.scrollTriggerAl(t)
        assertSame(t, w.kAh)
    }
}
