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

class Slice146Test {
    class S146World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        val inserts = mutableListOf<Entity>()
        override fun queueInsert(e: Entity) { inserts += e }
        private var cam = 0
        override var kO: Int get() = cam; set(v) { cam = v }
        private val byAw = mutableMapOf<Int, Entity>()
        fun register(e: Entity) { byAw[e.aw] = e }
        override fun findByAw(aw: Int): Entity? = byAw[aw]
    }

    private fun zone(w: S146World, z1: Int, z2: Int, z6: Int, z7: Int = 0): Entity {
        val z = Entity(10, null); z.S = 30
        z.W[0] = 90; z.W[2] = 130; z.W[1] = 80; z.W[3] = 120
        z.Z[1] = z1; z.Z[2] = z2; z.Z[3] = -20; z.Z[4] = 55
        z.Z[5] = 0; z.Z[6] = z6; z.Z[7] = z7
        w.player.ak = 110; w.player.al = 100
        w.player.W[0] = 104; w.player.W[2] = 116
        w.player.W[1] = 84; w.player.W[3] = 100
        return z
    }

    @Test fun `S30 no overlap does nothing`() {
        val w = S146World()
        val z = zone(w, 2, 2, 0)
        w.player.W[0] = -100; w.player.W[2] = -50
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertNull(z.cr); assertEquals(0, w.inserts.size)
    }

    @Test fun `S30 finite fill allocates grid and positions row0`() {
        val w = S146World()
        val z = zone(w, 2, 2, 0)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val cr = z.cr ?: error("no pool")
        assertEquals(2, cr.size); assertEquals(2, cr[0].size)
        assertTrue(z.P and 16 != 0)
        assertEquals(0, z.aA); assertEquals(2, z.pv); assertEquals(2, z.aG)
        val m00 = cr[0][0]
        assertEquals(11, m00.ax); assertEquals(4, m00.S)
        assertEquals(Entity.WEAPON_DMG[0], m00.aB)
        assertEquals(5000, m00.aw)
        assertEquals(-20, m00.ak)                      // Z5==0 → left of cam
        assertEquals(-20, m00.aq)                      // Z3+kO
        assertEquals(55, m00.ar); assertEquals(55, m00.al)
        assertEquals(m00.ak, m00.Z[3])
        assertEquals(1, m00.Z[2].let { -it })          // Z[2]==-1
        val m01 = cr[0][1]
        assertEquals(5001, m01.aw); assertEquals(-40, m01.ak)
        // row 1 members exist but stay dormant (no position/register)
        assertEquals(0, cr[1][0].ak); assertEquals(5002, cr[1][0].aw)
        assertEquals(2, w.inserts.size)                // row0 only
    }

    @Test fun `S30 Z6=3 wave config forces 1x3 and leader col0`() {
        val w = S146World()
        val z = zone(w, 5, 5, 3)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val cr = z.cr ?: error("no pool")
        assertEquals(1, cr.size); assertEquals(3, cr[0].size)
        assertEquals(-1, z.Z[1]); assertEquals(3, z.Z[2])
        assertEquals(1, cr[0][0].Z[0])                  // col0 engaged
        // slice 397 (i.javap aV() @3222-3243): `col >= 1 → Z[5] = 1`, col 0 → 0 —
        // the leader spawns LEFT (and faces right), the rest spawn right
        assertFalse(cr[0][0].av)
        assertEquals(0, cr[0][1].Z[0]); assertTrue(cr[0][1].av)
        assertEquals(-20, cr[0][0].ak)                  // Z5=0 → left of the camera
        assertEquals(440, cr[0][1].ak)                  // Z5=1 → right: 400 + 20*2
        assertEquals(460, cr[0][2].ak)
    }

    @Test fun `S30 Z6=3 respawns dead member when cooldown clear`() {
        val w = S146World()
        val z = zone(w, 0, 0, 3)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val cr = z.cr ?: error("no pool")
        val old = cr[0][1]
        old.aB = 0                                       // killed
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val nw = z.cr!![0][1]
        assertFalse(nw === old)
        assertEquals(11, nw.ax); assertEquals(4, nw.S)
        assertEquals(Entity.WEAPON_DMG[0], nw.aB)
        assertEquals(5001, nw.aw)                        // 5000+aA*pv+col
        assertEquals(0, nw.Z[0])                         // col0 still engaged
        // slice 397 (@4001-4022): `r9 >= 1 → Z[5] = 1` → spawns right
        assertEquals(440, nw.ak)
        assertTrue(nw.av)
    }

    @Test fun `S30 Z6=3 cooldown gates respawn then ticks down`() {
        val w = S146World()
        val z = zone(w, 0, 0, 3, z7 = 2)                   // aC=2 after fill
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val cr = z.cr ?: error("no pool")
        cr[0][1].aB = 0
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())       // aC=2 → skip, aC--
        assertEquals(1, z.aC)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())       // aC=1 → skip, aC--
        assertEquals(0, z.aC)
        val dead = z.cr!![0][1]
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())       // aC=0 → respawn
        assertFalse(z.cr!![0][1] === dead)
    }

    @Test fun `S30 finite row wipe advances aA and positions next row`() {
        val w = S146World()
        val z = zone(w, 2, 2, 0)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val cr = z.cr ?: error("no pool")
        cr[0][0].aB = 0; cr[0][1].aB = 0                   // row0 wiped
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(1, z.aA)
        val m = z.cr!![1][0]
        assertEquals(-20, m.ak); assertEquals(-20, m.aq)   // repositioned
        assertEquals(m.ak, m.Z[3])
        assertEquals(4, w.inserts.size)                    // 2 + row of 2
    }

    @Test fun `S30 finite all dead clears link flags and removes zone`() {
        val w = S146World()
        val z = zone(w, 1, 1, 0); z.Z[0] = 77
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val t = Entity(11, null); t.aw = 77; t.P = t.P or 32 or 128
        w.register(t)
        z.cr!![0][0].aB = 0
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertNull(z.cr)                                   // aS()
        assertTrue(w.removed.contains(z))                  // k.c()
        assertEquals(0, t.P and 32)                        // bi[11]!=-1 → ~128 too
        assertEquals(0, t.P and 128)
    }

    @Test fun `S30 Z1=-1 wipe drains and refills via goto L0`() {
        val w = S146World()
        val z = zone(w, -1, 2, 0)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val old = z.cr!![0][0]
        z.cr!![0][0].aB = 0; z.cr!![0][1].aB = 0
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())       // drain + re-fill
        val nw = z.cr ?: error("pool not refilled")
        assertFalse(nw[0][0] === old)
        assertEquals(0, z.aA)
        assertTrue(w.removed.isEmpty())                    // not a cleanup
    }
}

// ============================================================ slice 147
// ax10 S17 — balance zone (L1b44, i.java:12794-12951): center-pin on
// overlap, v(2)/v(8) side-leaps, v(33024) jump-up fling, leave→aA=2,
// aZ||g.a→reset, claim-busy freeze.
