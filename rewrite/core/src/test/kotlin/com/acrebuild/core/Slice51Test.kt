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

class Slice51Test {

    /** ax7 record: `[7, aw, x, y, aE, S, P, az(r8[7]), r8[8]]` — the
     *  slot has no Z; init stores `az` then `i(r8[5])+t()`. */
    private fun ax7At(w: Level0World, x: Int, y: Int, s: Int,
                      az: Int = 0, r8: Int = 1): Entity {
        val e = Entity(7, w.clips[60])
        e.setPositionPx(x, y)
        val f = mutableListOf(7, 0, x, y, 0, s, 0, az, r8)
        while (f.size < 16) f += 0
        w.npcFsm.initAx7(e, f, w)
        w.npcs.add(e)
        return e
    }

    @Test fun `init stores az and binds clip60`() {
        val w = world(); w.npcs.clear()
        val e = ax7At(w, 1397, 506, 0, az = 1)
        assertSame(w.clips[60], e.clip, "bi[7]=60")
        assertEquals(0, e.S); assertEquals(1, e.az)
        assertTrue(e.W[2] > e.W[0], "clip60 S0 has a real box")
    }

    @Test fun `S0 swallows the overlapping player into the slot`() {
        val w = world(); w.npcs.clear()
        val e = ax7At(w, 1397, 506, 0)
        e.av = false
        val p = w.player
        p.setPositionPx((e.W[0] + e.W[2]) / 2, (e.W[1] + e.W[3]) / 2)
        p.refreshBoxes(); p.S = 18; p.ah = 400; p.ag = -300
        w.npcFsm.tickAx7(e, w, p)
        assertEquals(1, e.S, "L90 → i(1)")
        assertEquals(313, p.S, "aS.i(313)")
        assertTrue(p.P and 64 != 0, "aS.P |= 64")
        assertEquals((e.W[0] + e.W[2]) shr 1, p.ak, "center snap x")
        assertEquals((e.W[1] + e.W[3]) shr 1, p.al, "center snap y")
        assertFalse(p.av, "aS.av = e.av")
        assertEquals(0, p.ah); assertEquals(0, p.ag)
        assertEquals(0, p.aj); assertEquals(0, p.ai)
    }

    @Test fun `S0 skips a player holding a marker`() {
        val w = world(); w.npcs.clear()
        val e = ax7At(w, 1397, 506, 0)
        val p = w.player
        p.setPositionPx((e.W[0] + e.W[2]) / 2, (e.W[1] + e.W[3]) / 2)
        p.refreshBoxes(); p.S = 18
        // p.isHolding() == true: ci in front, |Δak|<120, |Δal|<20
        val c = Entity(14, null)
        c.setPositionPx(p.ak + 10, p.al)
        p.ci = c; p.av = false
        w.npcFsm.tickAx7(e, w, p)
        assertEquals(0, e.S, "L90 k.aS.f() gate → no swallow")
        assertEquals(18, p.S)
    }

    @Test fun `S1 holds the player center-synced then ejects`() {
        val w = world(); w.npcs.clear()
        val e = ax7At(w, 1397, 506, 1)
        e.av = true
        val p = w.player
        p.setPositionPx(500, 500); p.refreshBoxes(); p.S = 313
        // mid-anim: still held
        e.T = 0; e.U = 0
        w.npcFsm.tickAx7(e, w, p)
        assertEquals((e.W[0] + e.W[2]) shr 1, p.ak, "L94 re-snap each tick")
        assertEquals((e.W[1] + e.W[3]) shr 1, p.al)
        assertEquals(e.T, p.T, "aS.T = this.T frame-sync")
        assertTrue(p.P and 64 != 0)
        assertEquals(1, e.S, "r()==false → still swallowing")
        // anim end: eject
        e.T = w.clips[60]!!.frameCount(1) - 1
        e.U = w.clips[60]!!.frameDuration(1, e.T) - 1
        w.npcFsm.tickAx7(e, w, p)
        assertEquals(0, e.S, "L94 r() → i(0)")
        assertEquals(-2048, p.ag, "av → -2048 eject")
    }

    @Test fun `S1 ejects rightward when the slot faces left-off`() {
        val w = world(); w.npcs.clear()
        val e = ax7At(w, 1397, 506, 1)
        e.av = false
        e.T = w.clips[60]!!.frameCount(1) - 1
        e.U = w.clips[60]!!.frameDuration(1, e.T) - 1
        w.npcFsm.tickAx7(e, w, w.player)
        assertEquals(2048, w.player.ag, "av=false → +2048")
    }
}
