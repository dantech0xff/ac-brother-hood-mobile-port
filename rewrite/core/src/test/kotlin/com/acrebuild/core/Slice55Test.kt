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

// ============================================================ slice 55 tests
class Slice55Test {

    /** ax56 record helper: f = {56, uid, x, y, mode, S, P, ?, Z[1..12]…}. */
    private fun ax56At(w: Level0World, x: Int, y: Int, mode: Int,
                       vararg z: Int): Entity {
        val e = Entity(56, w.clips[19])
        e.setPositionPx(x, y)
        val f = mutableListOf(56, 0, x, y, mode, 0, 0, 0)
        f += z.toList()
        while (f.size < 22) f += 0
        w.npcFsm.initAx56(e, f, w)
        w.npcs.add(e)
        keepLive(e)
        return e
    }

    @Test fun `ax56 init verbatim fields (L235)`() {
        val w = world()
        // Z[1..12] = r8[8..19]: volley aF=30, aC timer=50, Z3=0,Z4=0→Z5=1,
        // burst count 4, aF base 6, cooldown 900, n=7, mode 1, offset 200,
        // speed 1280
        val e = ax56At(w, 100, 200, 0, 0, 50, 0,0, 0, 4, 6, 900, 7, 1, 200, 1280)
        assertEquals(100, e.az); assertEquals(300, e.aB)
        assertEquals(0, e.Z[0])
        assertEquals(50, e.Z[2]); assertEquals(1, e.Z[5], "Z[4]==0 → Z[5]=1")
        assertEquals(4, e.Z[6]); assertEquals(6, e.Z[7])
        assertEquals(900, e.Z[8]); assertEquals(7, e.Z[9])
        assertEquals(1, e.Z[10]); assertEquals(200, e.Z[11]); assertEquals(1280, e.Z[12])
        assertEquals(50, e.aC); assertEquals(4, e.aD); assertEquals(6, e.aF)
        assertEquals(7, e.nl, "n = Z[9]")
        assertEquals(300, e.aq, "aq = ak + Z[11] (mode 1 x-shift)")
        assertEquals(200, e.ar)
    }

    @Test fun `ax56 init mode 2 shifts ar not aq (L253)`() {
        val w = world()
        val e = ax56At(w, 100, 200, 0, 0, 50, 0,0, 0, 4, 6, 900, 7, 2, 77, 1280)
        assertEquals(100, e.aq); assertEquals(277, e.ar)
    }

    @Test fun `ax56 Z4==1 arms burst defaults (L239-L247)`() {
        val w = world()
        // Z3=0 → Z3=3; Z5=Z6=1; Z7=0; (Z3==1&&Z4==1 → Z4=0 unreachable)
        val e = ax56At(w, 100, 200, 0, 0, 50, 0,1, 0, 0,0, 900, 7, 0, 0, 1280)
        assertEquals(3, e.Z[3]); assertEquals(1, e.Z[5])
        assertEquals(1, e.Z[6]); assertEquals(0, e.Z[7])
    }

    @Test fun `ax56 latch is al greater than kP without offset`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax56At(w, 0, w.kP - 100, 0)      // above camera → unarmed
        w.tick(emptyList())
        assertFalse(e.runnerBz)
        e.al = w.kP + 1
        w.tick(emptyList())
        assertTrue(e.runnerBz)
    }

    @Test fun `ax56 decrements Z8 every armed tick (L18)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax56At(w, 100, w.kP + 10, 0, 0, 50, 0,0, 0, 4, 6, 900, 7, 0, 0, 1280)
        e.runnerBz = true
        w.tick(emptyList())
        assertEquals(899, e.Z[8])
    }

    @Test fun `ax56 mode-1 travels toward aq at Z12 speed (L79)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax56At(w, 100, w.kP + 10, 0, 0, 0, 0,0, 0, 4, 6, 900, 7, 1, 5000, 1280)
        e.runnerBz = true
        e.setAnim(13)
        w.tick(emptyList())
        assertEquals(1280 shl 8, e.ag, "ag = Z[12]<<8 toward aq(+5000)")
        // near-target clamp: when |aq-ak| < Z[12] → ag = exact delta.
        // Clear residual ag first: the I()-head integrator (i.java:3886)
        // applies velocity BEFORE the arm evaluates position — a stale
        // 1280<<8 would overshoot the waypoint ahead of the check.
        e.ak = e.aq - 10; e.ag = 0
        w.tick(emptyList())
        assertEquals(10 shl 8, e.ag)
    }

    @Test fun `ax56 arrival stops and idles (L70)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        // mode 1, offset 0 → aq == ak spawn pos → az() false at S13
        val e = ax56At(w, 100, w.kP + 10, 0, 0, 0, 0,0, 0, 4, 6, 900, 7, 1, 0, 1280)
        e.runnerBz = true
        // Velocity stays 0: the I()-head integrator runs before the arm
        // (i.java:3886) — a nonzero ag would drift ak off aq=ak+0 first
        // and the arrival check would miss.
        e.ag = 0; e.ah = 0
        e.setAnim(13)
        w.tick(emptyList())
        assertEquals(0, e.ag); assertEquals(0, e.ah)
        assertFalse(e.av, "av = aq < ak → aq==ak → false")
    }

    @Test fun `ax56 attack window is frame T==3 U==0 (L130)`() {
        val w = world()
        val seed = Entity(24, w.clips[40])
        w.npcFsm.initAx24(seed, listOf(24, 0, 0, 0, 0, 0, 0, 0), w)
        // Z5=2 shots, Z6=2 bursts, Z7=1 aF → fires on first window tick
        val e = ax56At(w, 100, w.kP + 10, 0, 0, 0, 0,0, 2, 2, 1, 900, 7, 0, 0, 1280)
        e.runnerBz = true
        e.setAnim(5)
        e.T = 2; e.U = e.clip!!.frameDuration(5, 2) - 1   // s() wraps → T=3,U=0
        w.player.setPositionPx(400, 500); w.player.refreshBoxes()
        w.tick(emptyList())
        assertNotNull(w.projectilePool)
        assertEquals(1, e.aF, "aF reset to Z[7]=1")
        assertEquals(1, e.aD, "aD-- after volley")
    }

    @Test fun `ax56 exit arm S10 (L152)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax56At(w, 0, w.kP + 10, 0)
        e.runnerBz = true
        e.setAnim(10)
        val clip = e.clip!!
        e.T = clip.frameCount(10) - 2                    // s() wraps → T=last,U=0
        e.U = clip.frameDuration(10, e.T) - 1
        w.tick(emptyList())
        assertEquals(-1, e.az)
        assertEquals(64, e.P and 64, "r() → P|=64")
    }
}
