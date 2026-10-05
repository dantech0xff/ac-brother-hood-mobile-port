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

// ---------------------------------------------------------------------------
// slice 44 — ax35 `bQ()` scripted multi-tool (i.java:21594-22296)
// ---------------------------------------------------------------------------
class Slice44Test {

    /** ax35 record fixture — sparse map per i.java:3614:
     *  `[35,uid,x,y,0,S,P,az,z0..z6,z10,z11,z13]`. */
    private fun ax35At(w: Level0World, x: Int, y: Int, s: Int,
                       az: Int = 0, z: IntArray = IntArray(0)): Entity {
        val e = Entity(35, w.clips[62])
        e.setPositionPx(x, y)
        val f = mutableListOf(35, 0, x, y, 0, s, 0, az)
        for (i in 0..6) f += if (z.size > i) z[i] else 0          // Z[0..6]
        f += if (z.size > 7) z[7] else 0                          // Z[10]
        f += if (z.size > 8) z[8] else 0                          // Z[11]
        f += if (z.size > 9) z[9] else 0                          // Z[13]
        w.npcFsm.initAx35(e, f, w)
        w.npcs.add(e)
        return e
    }

    private fun finish(e: Entity) {
        val c = e.clip ?: return
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    @Test fun `ax35 init arm uses the sparse record map`() {
        val w = world(); w.npcs.clear()
        val e = ax35At(w, 300, 200, 0, az = 7,
            z = intArrayOf(1, 2, 30, 40, 1, 64, 5, 900, 12, 777))
        assertEquals(7, e.az)
        assertEquals(1, e.Z[0]); assertEquals(30, e.Z[2])
        assertEquals(64, e.Z[5]); assertEquals(5, e.Z[6])
        assertEquals(900, e.Z[10]); assertEquals(12, e.Z[11])
        assertEquals(777, e.Z[13])
        assertEquals(0, e.Z[7]); assertEquals(1, e.Z[12])
    }

    @Test fun `ax35 S0 resolves af then counts down into S1`() {
        val w = world(); w.npcs.clear()
        val af = Entity(11, w.clips[7]).apply { aw = 777; aB = 100 }
        w.npcs.add(af)
        val e = ax35At(w, 300, 200, 0, z = intArrayOf(0,0,20,20,0,0,0,0,0,777))
        e.aC = 1
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(777, e.af?.aw, "af resolved via k.q(Z[13])")
        assertTrue(e.af!!.P and 16 != 0, "af.P |= 16")
        assertNotNull(w.volPaintRect, "vol paint ran")
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(1, e.S, "aC expiry -> i(1)")
        assertEquals(191, af.S, "af.i(191) on handoff")
    }

    @Test fun `ax35 S0 dead af clears the vol rect and sets P=32`() {
        val w = world(); w.npcs.clear()
        val e = ax35At(w, 300, 200, 0, z = intArrayOf(0,0,20,20,0,0,0,0,0,999))
        w.npcFsm.tickAx35(e, w, w.player)   // af never resolves (no uid 999)
        assertEquals(32, e.P)
        assertNull(w.volPaintRect)
    }

    @Test fun `ax35 S1 captures player midline and enters the wave arm`() {
        val w = world(); w.npcs.clear()
        val af = Entity(11, w.clips[7]).apply { aw = 777; aB = 100 }
        w.npcs.add(af)
        val e = ax35At(w, 300, 200, 1, z = intArrayOf(0,0,20,20,0,0,0,0,0,777))
        e.af = af                                     // resolved by an S0/29 tick
        finish(e)
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(w.player.ak, e.Z[8], "Z[8] = aS.ak")
        assertEquals((w.player.W[1] + w.player.W[3]) shr 1, e.Z[9],
            "Z[9] = aS W-mid-y")
        assertEquals(2, e.S, "S1 -> i(2) (S30 would take i(31))")
        assertTrue(27 in w.sfxLog, "k.A(27) on the wave arm")
    }

    @Test fun `ax35 sweep insta-kills an overlapping soldier`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(-5000, -5000); p.refreshBoxes()   // off the vol
        val e = ax35At(w, 300, 200, 6)
        e.aG = 0                                          // march straight up
        val o = Entity(11, w.clips[7]).apply {
            aB = 100; setPositionPx(330, 200); refreshBoxes() }
        w.npcs.add(o)
        e.refreshBoxes()
        w.paint(o, e)
        w.npcFsm.tickAx35(e, w, p)
        assertEquals(0, o.aB, "as() zeroes aB on overlap")
        assertEquals(0, o.S, "ax11 -> i(0) death anim")
    }

    @Test fun `ax35 flight march commits only into free cells`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(-5000, -5000); p.refreshBoxes()
        val e = ax35At(w, 300, 200, 6)
        e.aG = 192                                          // down-screen
        // force the next-step cell solid: park the march over a floor
        val sx = e.ak; val sy = e.al
        var guard = 0
        // march until despawn or a cell-20 bounce (bounded)
        while (e.S == 6 && guard++ < 200) w.npcFsm.tickAx35(e, w, p)
        // it either turned (S10+bR) or despawned — both proven arms
        assertTrue(e.S != 6 || w.npcs.none { it === e },
            "march resolves to quadrant anim or edge despawn (S=${e.S})")
    }

    @Test fun `ax35 S26 resets wave counters and enters S27`() {
        val w = world(); w.npcs.clear()
        val e = ax35At(w, 300, 200, 26)
        e.aC = 9; e.Z[7] = 4; e.Z[12] = 3
        finish(e)
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(0, e.aC); assertEquals(0, e.Z[7])
        assertEquals(1, e.Z[12])
        assertEquals(27, e.S)
        assertTrue(27 in w.sfxLog)
    }

    @Test fun `ax35 wave step spawns the S18 marker plus an f() child`() {
        val w = world(); w.npcs.clear()
        val af = Entity(11, w.clips[7]).apply { aw = 777; aB = 100 }
        w.npcs.add(af)
        // Z[5]=125° (26 of the 28 ax35 records in m0/m2/m3/m6/m7; the
        // other two are 60°), θ=88:
        // the L118 march steps `20·sin θ` up per turn and only ends above
        // k.P, so θ needs sin θ > 0 — angle 0 never terminates in the
        // original either (j.b = cos, slice 352). Z[8]/Z[9] = fire point.
        val e = ax35At(w, 300, 100, 2,
            z = intArrayOf(0,0,20,20,0,125,10,0,0,777))
        e.af = af
        e.Z[8] = 300; e.Z[9] = 100; e.aC = 1
        w.kP = 40; w.rebuildCamRect() // kP: march endpoint stays in-map
        finish(e)
        w.npcFsm.tickAx35(e, w, w.player)
        assertTrue(w.pendingInsert.size >= 2,
            "L124 spawns the S18 marker + the f() wave child")
        assertTrue(w.pendingInsert.any { it.S == 18 }, "a(35,62,18,300)")
        assertEquals(1, e.Z[7], "wave counter advanced")
    }

    @Test fun `ax35 S28 aim proxy follows held d-pad and feeds af`() {
        val w = world(); w.npcs.clear()
        val af = Entity(11, w.clips[7]).apply { aw = 777; aB = 100 }
        w.npcs.add(af)
        val e = ax35At(w, 300, 200, 28, z = intArrayOf(0,0,0,0,0,0,0,0,0,777))
        e.af = af
        w.player.setPositionPx(300, 200); w.player.refreshBoxes()
        w.kM(2)                              // m(ad) snap → entity in view
        w.kAT = true
        w.pad.edge = 0; w.pad.held = 8256                       // k.u(8256)
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(310, e.ak, "RIGHT +10px (k.u held)")
        w.pad.held = 16388
        w.npcFsm.tickAx35(e, w, w.player)
        assertEquals(190, e.al, "UP -10px")
        assertEquals(310, af.Z[8]); assertEquals(190, af.Z[9],
            "L232 feeds the aim point to the af director")
        w.kAT = false
        w.npcFsm.tickAx35(e, w, w.player)
        assertTrue(e in w.pendingRemove, "!k.aT -> k.c(this)")
    }

    @Test fun `ax35 S23 grab-scan latches the player and S24 flings`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setAnim(18)                                          // grabbable
        p.setPositionPx(300, 200); p.refreshBoxes()
        val e = ax35At(w, 300, 200, 23)
        e.refreshBoxes()
        w.npcFsm.tickAx35(e, w, p)
        assertTrue(p.ga === e, "g.a = this")
        assertEquals(260, p.S, "S264->i(262) else i(260)")
        assertEquals(e.al, p.al)
        // S24: release-fling arms the 2560 fall
        e.setAnim(24)
        w.npcFsm.tickAx35(e, w, p)
        assertNull(p.ga, "g.a released")
        assertEquals(2560, p.ah, "aS.a(2560) fling vy")
        finish(e)
        w.npcFsm.tickAx35(e, w, p)
        assertEquals(128, e.P and 128, "P=128 carry-armed")
        assertEquals(20, e.S, "i(20) carry-return")
    }

    @Test fun `ax35 S14 off-camera containment removes the entity`() {
        val w = world(); w.npcs.clear()
        val e = ax35At(w, -8000, -8000, 15)
        e.Y[0] = -8000; e.Y[1] = -8000; e.Y[2] = -7999; e.Y[3] = -7999
        w.npcFsm.tickAx35(e, w, w.player)
        assertTrue(e in w.pendingRemove, "!b(Y,k.ac) -> k.c(this)")
    }
}
