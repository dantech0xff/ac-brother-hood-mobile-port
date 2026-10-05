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

// ---- Slice 136: g.j unification (w.gj → Entity.grabLatch) + ax11 aD() ----
class Slice136Test {

    private fun soldierAt(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(11, w.clips[7])
        // aB>0 required: `I()` head (i.java:4024) sends aB<=0 soldiers to
        // i(0); initSoldier always sets aB=BU[0].
        e.aB = 50
        e.setPositionPx(x, y); e.refreshBoxes()
        e.Z[0] = 0; e.aA = 1
        w.npcs.add(e)
        return e
    }

    private fun crateAt(w: Level0World, x: Int, y: Int, uid: Int): Entity {
        val c = Entity(51, w.clips[7])
        c.aw = uid; c.setPositionPx(x, y); c.refreshBoxes()
        w.npcs.add(c)
        return c
    }

    @Test fun `aD binds s to the Z7-linked crate on overlap i7167`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val c = crateAt(w, p.ak, p.al - 20, 7)
        val e = soldierAt(w, p.ak, p.al - 20)
        e.Z[7] = 7; e.S = 4
        assertTrue(w.npcFsm.crateRide11(e), "s bound")
        assertSame(c, e.s)
        assertEquals(2, e.S, "i(2) on bind (S!=3)")
        assertEquals(0, e.aA)
        assertEquals(c.W[1] + 1, e.al, "al = s.W[1]+1")
    }

    @Test fun `aD tracks the crate top and faces the player off-latch i7180`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.ak = 9999                                // player right of soldier
        val c = crateAt(w, p.ak - 60, p.al - 40, 7)
        val e = soldierAt(w, p.ak - 60, p.al - 40)
        e.Z[7] = 0; e.s = c; e.av = true
        Entity.grabLatch = false
        w.npcFsm.crateRide11(e)
        assertEquals(c.W[1] + 3, e.al, "al = s.W[1]+3")
        assertFalse(e.av, "Q() → av = aS.ak < ak → false")
    }

    @Test fun `aD latch gate suppresses the Q face-player flip i7180`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.ak = 9999
        val c = crateAt(w, p.ak - 60, p.al - 40, 7)
        val e = soldierAt(w, p.ak - 60, p.al - 40)
        e.Z[7] = 0; e.s = c; e.av = true
        Entity.grabLatch = true                    // g.j held by S146/147
        w.npcFsm.crateRide11(e)
        assertTrue(e.av, "!g.j gate — Q() suppressed while latched")
        Entity.grabLatch = false
    }

    @Test fun `aD drops i96 while the crate plays S2 i7167`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val c = crateAt(w, p.ak, p.al - 40, 7)
        c.S = 2
        val e = soldierAt(w, p.ak, p.al - 40)
        e.Z[7] = 0; e.s = c
        w.npcFsm.crateRide11(e)
        assertEquals(96, e.S, "s.ax==51 && s.S==2 → i(96)")
    }

    @Test fun `aD unbinds when the overlap is lost i7202`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val c = crateAt(w, p.ak + 4000, p.al - 40, 7)
        val e = soldierAt(w, p.ak, p.al - 40)
        e.Z[7] = 0; e.s = c
        assertFalse(w.npcFsm.crateRide11(e), "lost overlap → s=null")
        assertNull(e.s)
    }

    @Test fun `w gj delegates to the Entity grabLatch companion var`() {
        val w = world()
        Entity.grabLatch = false
        assertFalse(w.gj)
        w.gj = true
        assertTrue(Entity.grabLatch, "w.gj writes the real g.j latch")
        Entity.grabLatch = false
        assertFalse(w.gj, "w.gj reads the real g.j latch")
    }

    @Test fun `patrolArm runs the ride helper and feeds crate velocity i4168`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val c = crateAt(w, p.ak, p.al - 40, 7)
        c.ag = 4096
        val e = soldierAt(w, p.ak, p.al - 40)
        e.Z[7] = 7; e.S = 2; e.k = true; e.ag = 0
        e.Z[3] = e.ak; e.Z[5] = 40; e.Z[6] = 40
        w.npcFsm.tick(e, p)
        assertSame(c, e.s, "crate bound through the patrol arm")
        assertEquals(4096, e.ag, "s.ag feeds ride velocity")
    }
}
