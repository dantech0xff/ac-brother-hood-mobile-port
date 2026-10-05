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

/** Slice 197 — ax8 `ar()` (i.java:21786) + `i.aw()` waypoint materialization
 *  (i.java:23083) + `k.aX` pool exposure (`i.av()` i.java:22155) + `i.cu`
 *  world-freeze gate (i.java:15294) + `i.b(int)`/`i.O()` slow-mo bh==3 arm
 *  (i.java:21728/21749). All proven. */
class Slice197Test {
    class Ax8World : Slice128Test.MarkerWorld() {
        val c59 = Clip.load(asset("clips/clip0/clip.acpk"))
        val other = Clip.load(asset("clips/clip7/clip.acpk"))
        val removed = mutableListOf<Entity>()
        override fun removeEntity(e: Entity) { removed += e }
        override fun sfx(id: Int) { sfxLog += id }
        override fun clipFor(index: Int): Clip? = when (index) {
            59 -> c59
            else -> other
        }
    }

    private fun finished(e: Entity, s: Int) {
        e.S = s
        val c = e.clip ?: return          // null clip → animFinished() true
        e.T = c.frameCount(s) - 1
        e.U = c.frameDuration(s, e.T) - 1
    }

    @Test fun `ax8 gf flash sets P64`() {
        val w = Ax8World()
        val e = Entity(8, null)
        e.aw = -1
        Entity.gf = e                                     // g.f == this
        try {
            NpcFsm(w).tickAx8(e, w, w.player)
            assertTrue(e.P and 64 != 0, "g.f==this && r() → P|=64 (i.java:21789)")
        } finally { Entity.gf = null }
    }

    @Test fun `ax8 clip59 S2 finishes to flash bits not removal`() {
        val w = Ax8World()
        val e = Entity(8, w.c59)
        e.aw = -1
        finished(e, 2)
        NpcFsm(w).tickAx8(e, w, w.player)
        assertTrue(e.P and 32 != 0 && e.P and 64 != 0,
            "clip59 S∈{2,6,8} r() → P|=32|64 (i.java:21799)")
        assertTrue(w.removed.isEmpty(), "S∈{2,6,8} never removes")
    }

    @Test fun `ax8 clip59 other S removes on finish`() {
        val w = Ax8World()
        val e = Entity(8, w.c59)
        e.aw = -1
        finished(e, 0)
        NpcFsm(w).tickAx8(e, w, w.player)
        assertSame(e, w.removed.firstOrNull(), "r() && aw==-1 → k.c(this) (i.java:21822)")
    }

    @Test fun `ax8 non59 S4 overlapping player strikes`() {
        val w = Ax8World()
        val e = Entity(8, w.other)                        // aa != clip59
        e.aw = -1
        finished(e, 4)
        e.ak = 15; e.al = 15                   // refreshBoxes rebuilds W here
        w.player.W[0] = 0; w.player.W[2] = 100
        w.player.W[1] = 0; w.player.W[3] = 100
        // player S==9 → the op4 `S!=9` counter branch skips `c(attacker)`,
        // leaving this ax8 finished → removal fires (otherwise the counter
        // staggers it into S9 first — also faithful).
        w.player.S = 9
        NpcFsm(w).tickAx8(e, w, w.player)
        assertTrue(18 in w.sfxLog, "aS.a(4,…) hurt sfx at ar() tail (i.java:21830)")
        assertSame(e, w.removed.firstOrNull(), "r() && aw==-1 → removed after the strike")
    }

    @Test fun `ax8 script bound aw nonzero does nothing`() {
        val w = Ax8World()
        val e = Entity(8, w.other)
        e.aw = 42                                          // script-bound
        finished(e, 0)
        NpcFsm(w).tickAx8(e, w, w.player)
        assertTrue(w.removed.isEmpty() && e.P and 96 == 0,
            "aw != -1 → no arm, no removal (i.java:21788)")
    }

    @Test fun `aw materializes Z waypoint refs`() {
        val w = Slice128Test.MarkerWorld()
        w.waypoints.add(intArrayOf(0, 7, 100, 50, 0, 0, 0, 0, -1))
        val e = Entity(54, null)
        e.Z[1] = 7; e.Z[2] = 999; e.Z[3] = -1; e.Z[4] = 7
        materializeWaypoints(e, w)
        assertEquals(10000, e.Z[1], "hit → c.j mint uid (i.java:23095)")
        assertEquals(999, e.Z[2], "miss → untouched")
        assertEquals(-1, e.Z[3], "negative → untouched")
        assertEquals(10001, e.Z[4], "second hit → next mint uid")
        assertEquals(2, e.runnerC, "C++ per materialized node")
        assertEquals(100, w.waypoints.find(10000)!!.a - e.ak,
            "derived copy is entity-x-shifted (c.a(node,this))")
    }

    @Test fun `pooled shots allocate free slots only`() {
        val w = world()
        val fsm = NpcFsm(w)
        val e = Entity(24, null)
        fsm.initAx24(e, listOf(0, 0, 0, 0, 0, 0), w)       // rf(5)==0 → seed
        assertEquals(0, w.allocPooledShot(), "P&128 set → free (i.java:22155)")
        val slot0 = w.pooledShots!![0]!!
        slot0.P = slot0.P and -129                          // arm → live
        assertEquals(1, w.allocPooledShot())
        for (s in w.pooledShots!!) s!!.P = s!!.P and -129
        assertEquals(-1, w.allocPooledShot(), "all live → -1")
    }

    @Test fun `icu freeze skips non ax10 entities`() {
        val w = world()
        val guard = Entity(11, w.clipFor(11))
        guard.ak = 400; guard.al = 400; guard.S = 3
        guard.ag = 1024                                    // patrol drift
        w.npcs.add(0, guard)
        standOn(w, guard)
        val before = guard.ak
        Entity.icu = true
        try {
            repeat(3) { w.tick(emptyList()) }
            assertEquals(before, guard.ak, "i.cu → I() L109 early-out (i.java:15294)")
        } finally { Entity.icu = false }
        repeat(3) { w.tick(emptyList()) }
        assertTrue(guard.ak != before, "cleared → normal tick resumes")
    }

    class WarpWorld : Slice128Test.MarkerWorld() {
        override var iAH = false
        override var iAI = 0
        override var iAJ = 0
        override var kAw = 0
        override var kX = 0
        override var kW = 0
        override var kAj = 0
        override var kAu = 0
    }

    @Test fun `timewarp scales kX on flying mission only`() {
        val w = WarpWorld()
        val e = Entity(0, null)
        w.kAj = 1                                          // bh[1]==3 flying
        w.kX = 8
        e.timewarp(w, 2)
        assertTrue(w.iAH && w.iAI == 2 && w.kAw == 0, "flag writes unconditional")
        assertEquals(4, w.kX, "k.X /= r3 (i.java:21744)")
        assertEquals(8, w.iAJ, "saved divisor")
        e.timewarpOff(w)
        assertEquals(8, w.kX, "restored from aJ (i.java:21766)")
        assertEquals(0, w.iAJ)
        val w2 = WarpWorld(); w2.kAj = 0; w2.kX = 8        // bh[0]==4
        e.timewarp(w2, 2)
        assertEquals(8, w2.kX, "bh!=3 → k.X untouched")
        e.timewarpOff(w2)
    }
}
