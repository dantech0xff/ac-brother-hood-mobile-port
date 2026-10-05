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

// ============================================================ slice 54 tests
class Slice54Test {

    /** ax54 record helper: f = {ax, uid, x, y, mode, S, P, Z[1..14]…}. */
    private fun ax54At(w: Level0World, x: Int, y: Int, mode: Int,
                       vararg z: Int, ax: Int = 54): Entity {
        val e = Entity(ax, w.clips[if (ax == 30) 36 else 19])
        val f = mutableListOf(ax, 0, x, y, mode, 0, 0)
        f += z.toList()
        while (f.size < 22) f += 0
        w.npcFsm.initAx54(e, f, w)
        e.setPositionPx(x, y)
        w.npcs.add(e)
        keepLive(e)
        return e
    }

    @Test fun `waypoint pool load find copyShifted clear (c java)`() {
        val w = world()
        val pool = w.waypointPool
        // record shape f = {55, uid, x, y, c, d, e, f, g} (c.a reads r[1..8])
        pool.load(listOf(55, 77, 120, 40, 0, 3, 0, 5, 0))
        pool.load(listOf(55, 78, 200, 60))
        assertEquals(2, pool.count)
        val wp = pool.find(77)
        assertNotNull(wp)
        assertEquals(120, wp.a); assertEquals(40, wp.b)
        assertEquals(3, wp.d); assertEquals(5, wp.f)
        assertNull(pool.find(-1), "negative uid → null (verbatim early-out)")
        val e = Entity(54, null); e.ak = 1000; e.al = 500
        val copy = pool.copyShifted(wp, e)
        assertEquals(10000, copy.k, "copies mint uid from j=10000")
        assertEquals(1120, copy.a, "a += e.ak (entity-x relative)")
        assertEquals(40, copy.b, "b unshifted")
        pool.clear()
        assertEquals(0, pool.count); assertEquals(10000, pool.nextUid)
    }

    @Test fun `ax54 init verbatim fields (L214)`() {
        val w = world()
        // f[7..20] = Z[1..14]: 4 waypoint uids(0), F uid 0, timer 50,
        // trigger offset 300, animset 0, Z9=0→Z10=1, volley 3, burst 2,
        // lifetime 900, contact n=7
        val e = ax54At(w, 100, 200, 0, 0,0,0,0, 0, 50, 300, 0,0, 1, 3, 2, 900, 7)
        assertEquals(100, e.az); assertEquals(300, e.aB)
        assertEquals(100, e.Z[15]); assertEquals(200, e.Z[16])
        assertEquals(0, e.Z[0])
        assertEquals(50, e.Z[6]); assertEquals(300, e.Z[7])
        assertEquals(1, e.Z[10], "Z[9]==0 → Z[10]=1 (L218)")
        assertEquals(3, e.Z[11]); assertEquals(2, e.Z[12])
        assertEquals(900, e.Z[13]); assertEquals(7, e.Z[14])
        assertEquals(50, e.aC); assertEquals(3, e.aD)
        assertEquals(2, e.aF); assertEquals(7, e.nl, "n = Z[14]")
        assertEquals(100, e.az, "mode 0: no child; az stays 100")
    }

    @Test fun `ax54 non-zero mode spawns bound ax68 child (L234)`() {
        val w = world()
        val e = ax54At(w, 100, 200, 1, 0,0,0,0, 0, 50, 300, 0,0, 3, 2, 0, 900, 7)
        assertNotNull(e.ad, "ad = new i(r8) with r8[0]=68")
        assertEquals(68, e.ad!!.ax)
        assertSame(e, e.ad!!.af, "ad.af = this")
        assertFalse(e.ad!!.av)
        assertEquals(-1, e.az, "ax54 non-zero mode → az = -1")
        // ax30's az arm = 99 (L233)
        val e2 = ax54At(w, 100, 200, 1, 0,0,0,0, 0, 50, 300, 0,0, 3, 2, 0, 900, 7, ax = 30)
        assertEquals(99, e2.az)
    }

    @Test fun `resolveRunnerWaypoints remaps uids via copies (aw)`() {
        val w = world()
        w.waypointPool.load(listOf(55, 10, 100, 0, 0, 0, 0, 0, 0))
        w.waypointPool.load(listOf(55, 11, 200, 0, 0, 0, 0, 0, 0))
        val e = ax54At(w, 50, 0, 0, 10, 11, 0, 0)
        w.npcFsm.resolveRunnerWaypoints(e, w)
        assertEquals(2, e.runnerC, "two resolvable uids → C=2")
        assertEquals(10000, e.Z[1]); assertEquals(10001, e.Z[2])
        assertEquals(0, e.Z[3], "unresolved uid stays raw")
        val copy = w.waypointPool.find(10000)
        assertNotNull(copy)
        assertEquals(150, copy.a, "a = src.a + e.ak = 100 + 50")
    }

    @Test fun `ax54 trigger latch and offscreen removal (L7-L10)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax54At(w, 0, w.kP + 500, 0)
        e.runnerC = 0                                             // no chain
        // offscreen → v() false; mode 0 → L10 removes after bz latches
        w.tick(emptyList())
        assertTrue(e.runnerBz, "bz = al > kP + Z[7] latches")
        assertFalse(w.npcs.contains(e), "k.c(this) — L10 removal")
    }

    @Test fun `ax54 leg arm velocity toward waypoint (L35-L47)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        w.waypointPool.load(listOf(55, 10, 300, 400, 0, 0, 0, 5, 0))  // bt.f=5
        val e = ax54At(w, 0, w.kP + 500, 0, 10)
        e.bY = 100; e.bZ = 50                                     // scroll pos
        w.npcFsm.resolveRunnerWaypoints(e, w)
        w.tick(emptyList())
        assertNotNull(e.wpBt, "bt resolves first chain uid")
        assertTrue(e.runnerB, "B arm engaged while !iE && bt != null")
        // dist = h((300-100)<<8, (400-50)<<8); ag/ah = dir * 5<<8 / dist
        val dx = (300 - 100) shl 8; val dy = (400 - 50) shl 8
        val dist = e.h(dx, dy)
        assertEquals(dx * (5 shl 8) / dist, e.ag)
        assertEquals(dy * (5 shl 8) / dist + w.kY, e.ah)
        assertFalse(e.av, "av = bt.a < bY → 300 < 100 → false")
    }

    @Test fun `ax54 arrival dwell then bs advance (L131-L141)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        // waypoint exactly at the entity's scroll pos → same-tick arrive
        w.waypointPool.load(listOf(55, 10, 100, 50, 0, 3, 0, 5, 0))   // d=3
        val e = ax54At(w, 0, w.kP + 500, 0, 10)
        e.bY = 100; e.bZ = 50
        w.npcFsm.resolveRunnerWaypoints(e, w)
        w.tick(emptyList())                                       // latch+arm
        // bY/bZ stay 100/50 (bh[0]=4 → no bF) → arrive L131 fires
        assertFalse(e.runnerB, "arrived → B cleared")
        assertTrue(e.iE)
        assertEquals(2, e.runnerD, "D = bt.d-1 — same-tick iE block decrements")
        repeat(4) { w.tick(emptyList()) }
        assertFalse(e.iE, "D counted out → iE false")
        assertEquals(1, e.bs, "bs++ after dwell")
    }

    @Test fun `ax24 S0 seeds 50-slot projectile pool (L75)`() {
        val w = world()
        val e = Entity(24, w.clips[40])
        val f = mutableListOf(24, 0, 0, 0, 0, 0, 0, 33)
        w.npcFsm.initAx24(e, f, w)
        assertEquals(33, e.aB, "aB = r8[7]")
        assertEquals(640, e.P and 640)
        assertNotNull(w.projectilePool)
        assertEquals(50, w.projectilePool!!.size)
        assertEquals(24, w.projectilePool!![0]!!.ax)
        assertEquals(0, w.projectileAlloc(), "all slots free → av() = 0")
        val armed = w.projectilePool!![0]!!
        assertEquals(e.P, armed.P and 640, "child P = seeding P")
    }

    @Test fun `ax24 non-S0 leaves pool unseeded`() {
        val w = world()
        val e = Entity(24, w.clips[40])
        w.npcFsm.initAx24(e, listOf(24, 0, 0, 0, 0, 19, 0, 77), w)
        assertEquals(77, e.aB)
        assertNull(w.projectilePool)
    }

    @Test fun `runnerBurst arms first free slot verbatim`() {
        val w = world()
        val seed = Entity(24, w.clips[40])
        w.npcFsm.initAx24(seed, listOf(24, 0, 0, 0, 0, 0, 0, 0), w)
        // Z[8]=1 anim-set, Z[9]=1 → aG, Z[10]=1 shot, Z[11]=1 burst count,
        // Z[12]=1 → aF fires on the first attack-window tick
        val e = ax54At(w, 500, 600, 1, 0,0,0,0, 0, 50, 300, 1,2, 1,1,1, 900, 7)
        e.runnerBz = true
        w.player.setPositionPx(500, 600)                           // aim target
        w.kM(2)                                // m(ad) snap → runner in view
        e.X[0] = e.ak - 10; e.X[2] = e.ak + 10
        e.X[1] = e.al - 10; e.X[3] = e.al + 10
        // S5 arm with U==0 && animFinished → burst runs Z[10]=1 shot;
        // s() runs first each tick, so stage the pre-wrap state that lands
        // T=last,U=0 (clip19 anim5 last frame has dur 1)
        e.setAnim(5)
        e.T = e.clip!!.frameCount(5) - 2
        e.U = e.clip!!.frameDuration(5, e.T) - 1
        e.refreshBoxes()                            // t() — land Y in view
        w.tick(emptyList())
        val shot = w.projectilePool!![0]!!
        assertEquals(16, shot.P and 16)
        assertSame(e, shot.af)
        assertEquals(e.nl, shot.aC)
        assertEquals(2, shot.aG, "ax54 → aG = Z[9] = 2 (1280 tier)")
        assertTrue(shot.ag != 0 || shot.ah != 0, "armed with velocity")
        assertEquals(shot.am shr 8, shot.ak)
        assertTrue(w.sfxLog.contains(16), "volley sfx16 on r10==0")
    }

    @Test fun `ax54 homing caps velocity at the waypoint vector (L123)`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        // waypoint 1px ahead of the entity's scroll pos → the cap arms fire
        w.waypointPool.load(listOf(55, 10, 101, 51, 0, 0, 0, 5, 0))
        val e = ax54At(w, 0, w.kP + 500, 0, 10)
        e.bY = 100; e.bZ = 50
        w.npcFsm.resolveRunnerWaypoints(e, w)
        w.tick(emptyList())
        assertEquals(256, e.ag, "|bt.a-bY|<<8 = 256 <= ag → capped")
        assertEquals(w.kY + 256, e.ah, "ah capped + gravity")
    }
}
