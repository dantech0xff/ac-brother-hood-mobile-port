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

class Slice175Test {
    /** i.java ctor tail `i(sArr[5])`: ax11 records spawn at their record
     *  anim — the x7361 street guard (uid 44, f[5]=3) enters S3 patrol
     *  live, not the old hardcoded S0 dormant state. (proven) */
    @Test fun `ax11 record spawns at record anim S3`() {
        val w = world()
        w.stateL(8)
        val g = w.npcs.first { it.ax == 11 && it.aw == 44 }
        assertEquals(3, g.S)
        assertEquals(7361, g.ak)
    }

    /** i.java:2256-2258: Z[0]==1 (hardened record) doubles aB.
     *  x5421 uid 60 carries z0_1 → aB = BU[0]<<1 = 600. (proven) */
    @Test fun `hardened Z0==1 doubles soldier HP`() {
        val w = world()
        w.stateL(8)
        val g = w.npcs.first { it.ax == 11 && it.aw == 60 }
        assertEquals(600, g.aB)
        val soft = w.npcs.first { it.ax == 11 && it.aw == 44 }
        assertEquals(300, soft.aB)
    }

    /** Shared S0/106/107/135 arm (i.java:4044-4096): on anim-finish a
     *  plain S0 soldier goes corpse S139 + k.e(0,aw) stat tally + lock
     *  releases — NOT the old inferred S3 activation. (proven) */
    @Test fun `S0 anim finish goes corpse S139 and releases locks`() {
        val w = world()
        w.stateL(8)
        val g = w.npcs.first { it.ax == 11 && it.aw == 44 }
        keepLive(g)
        g.setAnim(0)
        w.lockTarget = g
        var ticks = 0
        while (g.S != 139 && ticks < 80) { w.tick(listOf()); ticks++ }
        assertEquals(139, g.S)
        assertEquals(0, g.aB)
        assertTrue(w.lockTarget !== g)
    }

    /** i.java:4072-4084: on r() the 106/107/135 death-drag states freeze
     *  (P&-17 |32 |64) instead of entering S139. (proven) */
    @Test fun `S106 finish freezes instead of corpse`() {
        val w = world()
        w.stateL(8)
        val g = w.npcs.first { it.ax == 11 && it.aw == 44 }
        keepLive(g)
        standOn(w, g)          // record spawns may hover over a pit;
                               // L777's !h&&!h arm then fires i(25)
        g.setAnim(106)
        var ticks = 0
        while ((g.P and 64) == 0 && ticks < 120) { w.tick(listOf()); ticks++ }
        assertTrue((g.P and 64) != 0, "S=${g.S} P=${g.P} ak=${g.ak} al=${g.al} aA=${g.aA}")
        // P&16 re-arms each tick: the L633 arm leaves aA=2 so the aA-
        // branch (i.java:6248 `aA!=0 → P|=16`) sets it again post-freeze.
        assertNotEquals(139, g.S)
    }

    /** i.java:2263-2267 + ctor :2895: Z[13]!=-1 binds the claim script
     *  and latches i.d; the tail `a(true);E()` runs at spawn. Level-0
     *  records carry -1 → unbound, but the flag itself is proven via a
     *  synthetic record. (proven wiring; synthetic record for the bind) */
    @Test fun `Z13 script claim binds at init`() {
        val w = world()
        w.stateL(8)
        val fsm = NpcFsm(w)
        val e = Entity(11, null)
        e.ak = 500; e.al = 500
        val f = listOf(11, 9999, 500, 500, 0, 2, 0,0,0,-1,0,0, -160,-64,320,100, 0,100,-1,-1)
        fsm.initSoldier(e, f, w)
        assertEquals(0, e.Z[13])
        assertTrue(e.scriptBound)
    }
}
