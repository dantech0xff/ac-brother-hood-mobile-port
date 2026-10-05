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

class Slice58Test {
    private fun lever(w: Level0World, s: Int, x: Int, y: Int): Entity {
        val e = Entity(58, w.clips[20])
        e.S = s
        e.setPositionPx(x, y)
        e.W[0] = x - 10; e.W[1] = y - 10; e.W[2] = x + 10; e.W[3] = y + 10
        w.npcs.add(0, e)
        return e
    }

    // be(): player overlap short-circuits true; else marks the first drawn
    // overlapping ax11/15 P|16 and reports true (slice 385).
    @Test fun `be() occupied by player`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        val e = lever(w, 0, 300, 150)
        p.setPositionPx(300, 150); p.refreshBoxes()
        w.npcFsm.tickAx58(e, w, p)
        assertEquals(1, e.S, "S0 + occupied -> i(1)")
        assertTrue(21 in w.sfxLog, "k.A(21)")
    }

    @Test fun `be() empty zone stays armed`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 0, 300, 150)
        w.npcFsm.tickAx58(e, w, w.player)
        assertEquals(0, e.S, "be()==false -> return, stays S0")
        assertTrue(21 !in w.sfxLog)
    }

    @Test fun `be() ax11 on the plate marks P16 and opens`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 0, 300, 150)
        val guard = Entity(11, w.clips[7])
        guard.setPositionPx(300, 150); guard.refreshBoxes()
        guard.W[0] = 295; guard.W[1] = 145; guard.W[2] = 305; guard.W[3] = 155
        w.npcs.add(guard)
        w.paint(e, guard)
        w.npcFsm.tickAx58(e, w, w.player)
        assertTrue(guard.P and 16 != 0, "k.bd[] overlap -> P|=16")
        assertEquals(1, e.S)
    }

    @Test fun `L9 bind consumes Z0 and claims kC`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes()
        val e = lever(w, 0, 300, 150)
        e.Z[0] = 4242                                    // bound uid (eH miss -> -1)
        w.npcFsm.tickAx58(e, w, p)
        assertEquals(1, e.S)
        assertTrue(w.kC === e, "k.C = this while binding")
        assertEquals(-1, e.Z[0], "Z[0] consumed to -1")
    }

    @Test fun `L9 without Z0 still advances but binds nothing`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes()
        val e = lever(w, 5, 300, 150)                    // S5 odd -> i(6)
        e.Z[0] = 0
        w.npcFsm.tickAx58(e, w, p)
        assertEquals(6, e.S)
        assertTrue(w.kC !== e, "Z[0]<=0 -> no k.C bind")
        assertEquals(0, e.Z[0])
    }

    @Test fun `L16 open state closes when zone clears`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 1, 300, 150)
        e.clip = null                                    // r() -> animFinished
        w.npcFsm.tickAx58(e, w, w.player)
        assertEquals(0, e.S, "be()==false -> i(S-1)")
    }

    @Test fun `L16 open state holds while occupied`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes()
        val e = lever(w, 1, 300, 150)
        w.npcFsm.tickAx58(e, w, p)
        assertEquals(1, e.S, "occupied -> no i(S-1)")
    }

    @Test fun `L16 anim-end occupied latches P64`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes()
        val e = lever(w, 1, 300, 150)
        e.clip = null                                    // r() true, no i(S-1) while occupied
        w.npcFsm.tickAx58(e, w, p)
        assertTrue(e.P and 64 != 0, "r() && !P64 -> P|=64 hold-open latch")
        assertEquals(1, e.S)
    }

    @Test fun `S2 crush arm fires i3 on player X overlap`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.S = 0
        p.ga = Entity(58, w.clips[20])                   // a() exits: g.a!=null
        p.setPositionPx(300, 150); p.refreshBoxes()
        p.X[0] = 295; p.X[1] = 145; p.X[2] = 310; p.X[3] = 160   // idle X is degenerate
        val e = lever(w, 2, 300, 150)
        e.W[0] = p.X[0] - 2; e.W[1] = p.X[1] - 2
        e.W[2] = p.X[2] + 2; e.W[3] = p.X[3] + 2         // W fully covers X
        w.npcFsm.tickAx58(e, w, p)
        assertEquals(3, e.S, "W∩aS.X && aS.S!=22 -> i(3)")
        assertTrue(21 in w.sfxLog)
    }

    @Test fun `S2 releases own kL claim and drops ae`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 2, 300, 150)
        e.aw = 77
        w.kL = e                                         // k.L == this
        e.ae = Entity(14, w.clips[9])
        w.npcFsm.tickAx58(e, w, w.player)
        assertNull(w.kL, "k.L.aw==aw -> k.m()")
        assertNull(e.ae, "G() releases ae")
    }

    @Test fun `S2 keeps foreign kL claim`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 2, 300, 150)
        e.aw = 77
        val other = Entity(58, w.clips[20]); other.aw = 88
        w.kL = other
        w.npcFsm.tickAx58(e, w, w.player)
        assertTrue(w.kL === other, "k.L!=this -> no release")
    }

    @Test fun `S3 anim end goes S4 and runs the Z0 bind tail`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 3, 300, 150)
        e.clip = null                                    // r() -> animFinished
        e.Z[0] = 4242
        w.npcFsm.tickAx58(e, w, w.player)
        assertEquals(4, e.S, "r() -> i(4)")
        assertEquals(-1, e.Z[0], "bind tail consumes Z[0]")
    }

    @Test fun `S4 parks with P32`() {
        val w = world()
        w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = lever(w, 4, 300, 150)
        w.npcFsm.tickAx58(e, w, w.player)
        assertTrue(e.P and 32 != 0, "L36 -> P|=32")
        assertEquals(4, e.S)
    }
}
