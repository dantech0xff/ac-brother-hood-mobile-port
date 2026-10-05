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

class Slice66Test {

    /** ax74 record `[74,aw,x,y,0,anim,P]` + Z tail — i.java:3029 L116 init. */
    private fun ax74At(w: Level0World, x: Int, y: Int,
                       anim: Int = 0, z: IntArray = IntArray(0)): Entity {
        val rec = (intArrayOf(74, 9, x, y, 0, anim, 0) + z).toList()
        val e = Entity(74, w.clips[54]).apply { aw = 9 }
        w.npcFsm.initAx74(e, rec, w)
        e.setPositionPx(x, y)
        w.npcs.add(e)
        return e
    }

    // -- init (i.java:3029 L116 arm, proven) -------------------------------
    @Test fun `ax74 init sets flags and counters`() {
        val w = world()
        val kaq0 = w.kAq                             // level-0's own anim-0
                                                   // records already counted
        val e = ax74At(w, 300, 200, anim = 0,
                       z = intArrayOf(42, 0, 0, 0, 0, 0, 0, 0))
        assertTrue((e.P and 512) != 0)               // P |= 512
        assertEquals(42, e.az)                       // az = f[7]
        assertEquals(kaq0 + 1, w.kAq)                // k.aq++ (anim==0)
        assertEquals(0, e.S)                         // i(0)
    }

    @Test fun `ax74 init anim skips kaq`() {
        val w = world()
        val kaq0 = w.kAq
        val e = ax74At(w, 300, 200, anim = 2)
        assertEquals(kaq0, w.kAq)                    // r8[5]!=0 → no k.aq++
        assertEquals(2, e.S)
    }

    // -- S0: collect (i.java:21284 L4-17, proven) ---------------------------
    @Test fun `ax74 S0 collects within 20`() {
        val w = world()
        val p = w.player
        val e = ax74At(w, p.ak + 15, p.al)
        w.npcFsm.tickAx74(e, w, p)                   // dist=15 ≤ 20
        assertEquals(2, e.S)                         // i(2)
        assertEquals(1, w.kAp[5])                    // k.o(5) — bh!=3
        assertEquals(1, w.kAz)                       // k.s() streak
        assertEquals(p.az + 1, e.az)                 // az = player.az+1
        assertTrue(15 in w.sfxLog)                   // k.A(15)
    }

    @Test fun `ax74 S0 waits beyond 20`() {
        val w = world()
        val p = w.player
        val e = ax74At(w, p.ak + 200, p.al)
        w.npcFsm.tickAx74(e, w, p)
        assertEquals(0, e.S)                         // stays S0
        assertEquals(0, w.kAp[5])
    }

    @Test fun `ax74 S0 releases owner with extra sfx`() {
        val w = world()
        val p = w.player
        val e = ax74At(w, p.ak + 10, p.al)
        val owner = Entity(4, w.clips[0])
        owner.aG = 1                                 // af.aG != 0 → +k.A(15)
        e.af = owner
        w.npcFsm.tickAx74(e, w, p)
        assertNull(e.af)                             // af = null
        assertEquals(2, w.sfxLog.count { it == 15 }) // k.A(15) twice
    }

    // -- S1: polar spiral (i.java:21300 L20-41, proven) ---------------------
    @Test fun `ax74 S1 orbit spirals in then collects`() {
        val w = world()
        val e = ax74At(w, 300, 200)
        e.setAnim(1)
        e.aD = 0; e.aE = 30; e.aC = 1
        e.aq = w.player.ak + 30; e.ar = w.player.al
        e.j = 0
        w.npcFsm.tickAx74(e, w, w.player)            // j+=15 → j=15 <aE
        assertEquals(15, e.j)
        assertEquals(1, e.S)
        w.npcFsm.tickAx74(e, w, w.player)            // j=30 ≥ aE → aC-- =0
        assertEquals(30, e.j)
        w.npcFsm.tickAx74(e, w, w.player)            // j≥aE && aC≤0 → i(2)
        assertEquals(2, e.S)
        assertEquals(0, e.aC); assertEquals(30, e.aE); assertEquals(0, e.j)
    }

    @Test fun `ax74 S1 keeps orbiting while aC positive`() {
        val w = world()
        val e = ax74At(w, 300, 200)
        e.setAnim(1)
        e.aD = 0; e.aE = 15; e.aC = 3; e.aq = 300; e.ar = 200
        e.j = 15                                     // j already ≥ aE
        w.npcFsm.tickAx74(e, w, w.player)            // aC-- → 2 >0 → orbit on
        assertEquals(1, e.S)
        assertEquals(2, e.aC)
    }

    @Test fun `ax74 S1 polar coordinates`() {
        val w = world()
        val e = ax74At(w, 300, 200)
        e.setAnim(1)
        e.aD = 90; e.aE = 15; e.aC = 1; e.aq = 400; e.ar = 300
        e.j = 0
        w.npcFsm.tickAx74(e, w, w.player)            // j=15, aF=90*256/360=64
        assertEquals(64, e.aF)
        assertEquals(400 + ((Trig.cos(64) * 15) shr 8), e.ak)
        assertEquals(300 + ((Trig.cos(0) * 15) shr 8), e.al)
        assertTrue((e.P and 16) != 0)                // P |= 16
    }

    // -- S2: attach anim (i.java:21337 L43, proven) -------------------------
    @Test fun `ax74 S2 pins to player then despawns`() {
        val w = world()
        val e = ax74At(w, 500, 100)
        e.setAnim(2)
        e.P = e.P or 16
        w.npcFsm.tickAx74(e, w, w.player)
        assertEquals(0, e.P and 16)                  // P &= -17
        assertEquals(w.player.ak, e.ak)
        assertEquals(w.player.al - 30, e.al)
        e.T = w.clips[54]!!.frameCount(e.S) - 1
        e.U = w.clips[54]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx74(e, w, w.player)
        assertTrue(e in w.pendingRemove)             // r() → k.c(this)
    }

    // -- S5: fall→bezier setup (i.java:21331 L48, proven) -------------------
    @Test fun `ax74 S5 sets bezier endpoints`() {
        val w = world()
        val e = ax74At(w, 400, 120)
        e.setAnim(5)
        e.T = w.clips[54]!!.frameCount(5) - 1
        e.U = w.clips[54]!!.frameDuration(5, e.T) - 1
        e.Z[2] = 500; e.Z[3] = 60                    // fixed end (view space)
        e.Z[7] = 10                                  // duration
        w.npcFsm.tickAx74(e, w, w.player)            // r() → setup → i(3)
        assertEquals(3, e.S)
        assertEquals(0, e.ah); assertEquals(0, e.ag) // vel cleared
        assertEquals(e.ak - w.kO, e.Z[0])            // start = view-space pos
        assertEquals(e.al - w.kP, e.Z[1])
        val cx = (e.Z[0] + e.Z[2]) shr 1
        assertTrue(e.Z[4] in (cx - 80)..(cx + 80))   // Z4 = cx ± ≤80
        assertEquals((e.Z[1] + e.Z[3]) shr 1, e.Z[5])
    }

    // -- S3/S6: bezier flight (i.java:21341 L51, proven) --------------------
    @Test fun `ax74 S3 flies bezier then S4`() {
        val w = world()
        val e = ax74At(w, 0, 0)
        e.setAnim(3)
        e.Z[0] = 100; e.Z[1] = 200                   // start (view space)
        e.Z[2] = 300; e.Z[3] = 100                   // end
        e.Z[4] = 200; e.Z[5] = 50                    // control
        e.Z[6] = 0; e.Z[7] = 4                       // 4-tick flight
        w.npcFsm.tickAx74(e, w, w.player)            // t=0 → the start point + cam
        // j.a/j.b raw bytes (slice 409): a·(i-t)² + 2b·(i-t)t + c·t² — t=0 is Z[0], Z[1].
        assertEquals(e.Z[0] + w.kO, e.ak)
        assertEquals(e.Z[1] + w.kP, e.al)
        assertEquals(1, e.Z[6])
        repeat(3) { w.npcFsm.tickAx74(e, w, w.player) }
        assertEquals(4, e.S)                         // Z6 ≥ Z7 → i(4)
    }

    @Test fun `ax74 S6 skips first frame when Q6`() {
        val w = world()
        val e = ax74At(w, 0, 0)
        e.setAnim(6)
        e.Q = 6
        e.Z[6] = 0; e.Z[7] = 1
        w.npcFsm.tickAx74(e, w, w.player)
        assertEquals(4, e.S)
        assertEquals(1, e.T)                         // Q==6 → T=1
    }

    // -- S4: end (i.java:21355 L58, proven) ---------------------------------
    @Test fun `ax74 S4 despawns at anim end`() {
        val w = world()
        val e = ax74At(w, 300, 200)
        e.setAnim(4)
        e.T = w.clips[54]!!.frameCount(4) - 1
        e.U = w.clips[54]!!.frameDuration(4, e.T) - 1
        w.npcFsm.tickAx74(e, w, w.player)
        assertTrue(e in w.pendingRemove)             // r() → k.c(this)
    }

    // -- kCount/kCollectStreak (k.java:4304/5338, proven) --------------------
    @Test fun `kCount slot3 gated on aj7`() {
        val w = world()
        w.kAj = 7
        w.kCount(3)
        assertEquals(0, w.kAp[3])                    // aj==7 → skip
        w.kAj = 0
        w.kCount(3)
        assertEquals(1, w.kAp[3])
    }

    @Test fun `collectStreak tier lifts meter floor`() {
        val w = world()
        w.kAx = 30                                   // below tier-1 floor
        w.player.x1 = 20
        repeat(100) { w.kCollectStreak() }           // az=100 → tier 1
        assertEquals(100, w.kAz)
        assertEquals(45, w.kAx)                      // 30 + 1*15
        assertEquals(45, w.player.x1)                // g.e(ax): old<new → up
    }

    @Test fun `collectStreak lower tier does not drop x1`() {
        val w = world()
        w.kAx = 90                                   // above the 45 rewrite
        w.player.x1 = 20                             // 45 → g.e skipped
        repeat(100) { w.kCollectStreak() }
        assertEquals(45, w.kAx)                      // ax rewritten verbatim
        assertEquals(20, w.player.x1)                // min(20,45) unchanged
    }
}

// ============================================================= Slice 67 —
// ax76 `bO()` damage/hazard volume (i.java:21407, proven). `bi[76]=56` —
// pack-3 slot 56 is a zero-size entry → clipless invisible trigger. Covers:
// L361 init (az/aC/oId/Z[1]), S0 arm (overlap + aZ + g==null → S1 + bind
// aA|8 + az-push + g.e owner when Z[1]==1), S0/S1 release, S3/S5 hitbox-exit
// advance, S4/S6 despawn, S2 per-tick damage + aC sibling chain via k.q(o).
