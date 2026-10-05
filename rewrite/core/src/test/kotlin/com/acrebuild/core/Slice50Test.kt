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

class Slice50Test {

    /** ax46 record (i.java:2700 → L206): `[46, aw, x, y, aE, S, P, az,
     *  z0(r8[8]), z1(r8[9]), clipIdx(r8[10]), pal(r8[11]), z7(r8[12])]`.
     *  `k.bl = {29, 0}` rebinds the anim clip by r8[10]. */
    private fun ax46At(w: Level0World, x: Int, y: Int, s: Int,
                       z0: Int = 0, z1: Int = 0, ci: Int = 0,
                       pal: Int = 0, z7: Int = 0): Entity {
        val e = Entity(46, w.clips[29])
        e.setPositionPx(x, y)
        val f = mutableListOf(46, 0, x, y, 0, s, 0, 0, z0, z1, ci, pal, z7)
        while (f.size < 16) f += 0
        w.npcFsm.initAx46(e, f, w)
        w.npcs.add(e)
        return e
    }

    @Test fun `init binds clip29 for bl-0 records and loads Z`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 0, z0 = 50, z1 = -40, pal = 2, z7 = 9)
        assertSame(w.clips[29], e.clip, "L15 aa = k.r(bl[0]) = clip29")
        assertEquals(0, e.S)
        assertEquals(50 shl 8, e.Z[0]); assertEquals(-40 shl 8, e.Z[1])
        assertEquals(30, e.Z[3]); assertEquals(0, e.Z[4])
        assertEquals(2, e.Z[5]); assertEquals(29, e.Z[6]); assertEquals(9, e.Z[7])
    }

    @Test fun `init binds clip0 for bl-1 armed records`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 2389, 386, 327, ci = 1, pal = 2)
        assertSame(w.clips[0], e.clip, "L15 aa = k.r(bl[1]) = clip0")
        assertEquals(327, e.S, "i(r8[5]) accepts 327 on the mega clip")
        assertEquals(327, e.Z[4], "armed marker Z[4]=r8[5]")
        assertEquals(0, e.Z[6])
    }

    @Test fun `head pins the palette to Z5 each tick`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 2, pal = 2)
        w.npcFsm.tickAx46(e, w, w.player)
        assertEquals(2, e.palette, "aa.l(Z[5])")
    }

    @Test fun `spring arm launches the falling player off the pad`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 0, z0 = 50, z1 = -40)
        // clip29 S0 rect = [-27,-12,53,8] → W = [173,388,226,396]
        assertEquals(173, e.W[0]); assertEquals(388, e.W[1])
        assertEquals(226, e.W[2]); assertEquals(396, e.W[3])
        val p = w.player
        p.setPositionPx(200, 390); p.refreshBoxes()
        p.S = 43; p.ah = 512                            // falling onto it
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(388, p.al, "op11 → al = pad top")
        assertEquals(200, p.ak, "op11 → ak = pad anchor")
        assertEquals(-40 shl 8, p.ah, "op11 → ah = Z[1]")
        assertEquals(50 shl 8, p.ag, "op11 → ag = Z[0]")
        assertEquals(22, p.S, "Z[0]!=0 → i(22)")
        assertFalse(p.av, "Z[0]>0 → av=false")
        assertEquals(1, e.S, "L88 → i(1) sprung")
    }

    @Test fun `spring ignores a rising player`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 0, z0 = 50, z1 = -40)
        val p = w.player
        p.setPositionPx(200, 390); p.refreshBoxes()
        p.S = 43; p.ah = -512                            // still moving up
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(43, p.S, "L82 ah<0 → no bounce")
        assertEquals(0, e.S)
    }

    @Test fun `S1 settle returns the pad to i0 on anim end`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 1)
        e.T = w.clips[29]!!.frameCount(1) - 1
        e.U = w.clips[29]!!.frameDuration(1, e.T) - 1
        w.npcFsm.tickAx46(e, w, w.player)
        assertEquals(0, e.S, "L71 → i(0)")
    }

    @Test fun `armed S327 pins the overlapping player via op24`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 327, ci = 1, pal = 2)
        assertTrue(e.X[2] > e.X[0], "clip0 S327 carries a real attack box")
        val p = w.player
        p.setPositionPx((e.X[0] + e.X[2]) / 2, (e.X[1] + e.X[3]) / 2)
        p.refreshBoxes(); p.S = 18
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(330, p.S, "aS.a(24,330) pins anim 330")
        assertEquals(e.W[0], p.ak, "L49 → ak = src.W[0]")
        assertEquals(e.W[1], p.al, "L49 → al = src.W[1]")
        assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
        assertEquals(328, e.S, "L13 → i(328)")
    }

    @Test fun `S328 release throws the player off and rearms`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 328, ci = 1, pal = 2)
        val p = w.player
        p.setPositionPx((e.X[0] + e.X[2]) / 2, (e.X[1] + e.X[3]) / 2)
        p.refreshBoxes(); p.S = 330
        e.av = true
        e.T = w.clips[0]!!.frameCount(328) - 1
        e.U = w.clips[0]!!.frameDuration(328, e.T) - 1
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(329, e.S, "L46 → i(329)")
        assertEquals(165, p.S, "aS.i(165)")
        assertTrue(p.av, "aS.av = e.av")
        assertEquals(-2048, p.ag, "av → -2048")
        assertEquals(-5120, p.ah)
        assertEquals(30, e.Z[2], "L60 → Z[2]=Z[3]")
        // S329 → i(327) re-arm
        e.T = w.clips[0]!!.frameCount(329) - 1
        e.U = w.clips[0]!!.frameDuration(329, e.T) - 1
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(327, e.S, "L67 → i(327)")
    }

    @Test fun `unarmed touch throws the player off the trap`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 3)                    // Z[4]=3==S armed!
        // Z[4]==S here → this one is armed; spawn unarmed via record S
        val e2 = ax46At(w, 300, 400, 3)
        e2.Z[4] = 99                                    // disarm manually
        val p = w.player
        p.setPositionPx((e2.X[0] + e2.X[2]) / 2, (e2.X[1] + e2.X[3]) / 2)
        p.refreshBoxes(); p.S = 18
        w.npcFsm.tickAx46(e2, w, p)
        assertEquals(165, p.S, "L11 → aS.i(165)")
        assertEquals(if (e2.av) -2048 else 2048, p.ag)
        assertEquals(-5120, p.ah)
        assertEquals(7, e2.S, "L11 → i(7)")
    }

    @Test fun `S7 settles to the dead i2 arm`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 7)
        e.T = w.clips[29]!!.frameCount(7) - 1
        e.U = w.clips[29]!!.frameDuration(7, e.T) - 1
        w.npcFsm.tickAx46(e, w, w.player)
        assertEquals(2, e.S, "L63 → i(2)")
    }

    @Test fun `S10 pusher jets the player with record velocity`() {
        val w = world(); w.npcs.clear()
        val e = ax46At(w, 200, 400, 10, z0 = 30, z1 = -60)
        val p = w.player
        p.setPositionPx(e.W[0] + 2, e.W[1] + 2); p.refreshBoxes()
        p.S = 18
        e.av = false
        w.npcFsm.tickAx46(e, w, p)
        assertEquals(30 shl 8, p.ag, "L94 → ag = Z[0]")
        assertEquals(-60 shl 8, p.ah, "L94 → ah = Z[1]")
        assertTrue(p.av, "L98 → aS.av = !e.av")
        assertEquals(242, p.S, "L99 → aS.i(242)")
    }
}

// =====================================================================
// Slice 51 — ax7 ejection slot (i.java L88/L90/L94 :5110-5149, proven).
// =====================================================================
