package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 417 — trap T16 / plan P1a: "persistent `player` vs fresh `g`".
 *
 * `k.d(Z)V` runs `i.D()` (i.javap @1-400) then `new g(r)` (k.javap @614): the
 * original rebuilds the player object from scratch on every spawn, so every
 * field `i.<init>([S)V` / `g.<init>([S)V` writes must be re-armed on the port's
 * single reused `player` — and the `g` statics outside `D()`'s clear list
 * ({B,C,D,G,H,I,J,ci..cw,f,i,n,o,p,s,t,u,v,w,y,z}) must persist untouched.
 *
 * Provenance anchors:
 *  - i.<init>()V field block (i.javap @4-198)
 *  - i.<init>([S)V record load (@201-274): aw=r[1], ak=r[2], al=r[3],
 *    N=ak<<8, O=al<<8, ax=r[0], av=(r[6]&1)!=0, P=r[6]
 *  - g.<init>([S)V tail (@5-121): cl=false, cx=(8*j.m)/360, cy..cE=0,
 *    cF=5120, cG=i.av (record-facing — latched BEFORE the restore arm),
 *    cH..cM=0, K=0, cN=0, L=0, M=0
 *  - ctor tail (i.javap @7146-7199): i(r[5]) then ax==0 → E() — runs even on
 *    the checkpoint-restore path, at the record position
 *  - k.a(Z)V restore arm (k.javap @206-243): writes ak/al/av only — N/O keep
 *    the record-spawn position (desync quirk)
 */
class Slice417Test {

    private fun keepLive(e: Entity) { e.P = e.P or 16 }

    private fun fireCheckpoint(w: Level0World, cp: Level0World.Checkpoint) {
        w.player.setPositionPx(cp.ak, cp.al + 5); w.player.refreshBoxes()
        w.npcs.firstOrNull { it.ax == 2 && it.aw == cp.aw }?.let(::keepLive)
        repeat(6) { if (w.checkpointSnap == null) w.tick(emptyList()) }
    }

    private fun stageCtorFields(p: Entity) {
        p.P = 0xFFFF; p.T = 9; p.U = 9; p.a = 9; p.V = 9; p.av = true
        p.ag = 99; p.ah = 99; p.ai = 99; p.aj = 99
        p.aq = 7; p.ar = 7; p.scriptBound = true; p.au = 4; p.i = 9; p.k = true
        p.y = 5; p.bs = 9; p.runnerC = 9; p.runnerBz = true
        p.bN = 7; p.bO = 9; p.bP = 9; p.bR = true
        p.cl = true; p.cx = 1; p.cy = 1; p.cz = 1; p.cA = 1; p.cB = 1
        p.cC = 1; p.cD = 1; p.cE = 1; p.cF = 1; p.cG = true
        p.cH = 9; p.cI = 9; p.cJ = 9; p.cK = 9; p.cL = 9; p.cM = 9
        p.K = 7; p.cN = 9; p.gQL = 321; p.gQM = 123
        p.az = 1; p.aC = 9; p.aE = 9; p.aF = 9; p.aG = 9
        p.ay = 9; p.nl = 9; p.eventN = 9; p.pv = 9
        p.m = 9; p.j = 9; p.l = 9; p.am = 9; p.an = 9; p.ao = 9; p.ap = 9
        p.bY = 9; p.bZ = 9; p.bl = 9; p.co = 9
        p.aO = 9; p.aT = 9
        p.ba = true; p.bb = true; p.bc = true; p.bd = true; p.v = true; p.b = true
        p.aZ = true; p.aD = 9
        p.runnerB = true; p.runnerD = 9; p.iE = true; p.runnerG = true
        p.scriptStep = 7; p.ca = 3; p.claimLatchX = 44; p.claimLatchY = 55
        p.cP = 9; p.cR = "x"; p.cV = true; p.cW = 7
        p.cGCount = 9; p.cIDone = true; p.cJDone = true
        p.palette = 9; p.remapTable = 3; p.paletteAlpha = 7
        p.j0Frame = 2; p.hitsTaken = 3; p.trailAnim = 4
        p.homeX = 999; p.homeY = 999; p.ropeGrabSeg = 9
        p.clip = null
        val dead = Entity(4, null)
        p.af = dead; p.ab = dead; p.ad = dead; p.ae = dead; p.s = dead
        p.c = dead; p.cr = arrayOf(arrayOf(dead)); p.bM = dead; p.F = dead; p.g = dead
        p.gg = dead; p.cg = dead; p.ch = dead; p.standingOn = dead
        p.ac = dead; p.wpBt = Waypoint(); p.wpF = Waypoint()
        p.scriptOps = intArrayOf(1); p.cS = intArrayOf(1)
        p.cT = dead; p.cU = intArrayOf(1)
        p.cHWaypoints = arrayOf(intArrayOf(1)); p.cHGrid = arrayOf(intArrayOf(1))
        p.cb = intArrayOf(5); p.cc = intArrayOf(5); p.cf = intArrayOf(5)
        p.cQ = intArrayOf(5)
        p.Z[5] = 7; p.W[2] = 7; p.X[2] = 7; p.Y[2] = 7; p.cd[0] = true
    }

    private fun assertCtorState(w: Level0World) {
        val p = w.player
        val rec = w.level.entities.first { it.isNotEmpty() && (it[0] == 0 || it[0] == 25) }
        // i.<init>()V block (i.javap @4-198)
        assertEquals(-1, p.Q, "i.Q = -1")
        assertEquals(-1, p.R, "i.R = -1")
        assertEquals(0, p.aq, "i.aq = 0")
        assertEquals(0, p.ar, "i.ar = 0")
        assertFalse(p.scriptBound, "i.d = false")
        assertEquals(10, p.au, "i.au = 10")
        assertEquals(1, p.i, "i.i = 1")
        assertFalse(p.k, "i.k = false")
        assertEquals(0, p.y, "i.y = 0")
        assertEquals(0, p.bs, "i.bs = 0")
        assertEquals(0, p.runnerC, "i.C = 0")
        assertFalse(p.runnerBz, "i.bz = false")
        assertNull(p.cr, "i.cr = null")
        assertNull(p.bM, "i.bM = null")
        assertEquals(1, p.bN, "i.bN = 1")
        assertEquals(0, p.bO, "i.bO = 0")
        assertEquals(0, p.bP, "i.bP = 0")
        assertFalse(p.bR, "i.bR = false")
        assertEquals(0, p.cGCount, "i.cG = 0")
        assertNull(p.cHWaypoints, "i.cH = null")
        assertNull(p.cHGrid, "i.cH-grid = null")
        assertFalse(p.cIDone, "i.cI = false")
        assertFalse(p.cJDone, "i.cJ = false")
        assertEquals(-1, p.ca, "i.ca = -1")
        assertEquals(-1, p.claimLatchX, "i.cM = -1")
        assertEquals(-1, p.claimLatchY, "i.cN = -1")
        assertEquals(-1, p.cP, "i.cP = -1")
        assertEquals("", p.cR, "i.cR = \"\"")
        assertNull(p.cS, "i.cS = null")
        assertNull(p.cT, "i.cT = null")
        assertNull(p.cU, "i.cU = null")
        assertFalse(p.cV, "i.cV = false")
        assertEquals(0, p.cW, "i.cW = 0")
        // g.<init>([S)V tail (g.javap @5-121)
        assertFalse(p.cl, "g.cl = false")
        assertEquals((8 * Trig.M) / 360, p.cx, "g.cx = (8*j.m)/360")
        assertEquals(0, p.cy, "g.cy = 0"); assertEquals(0, p.cz, "g.cz = 0")
        assertEquals(0, p.cA, "g.cA = 0"); assertEquals(0, p.cB, "g.cB = 0")
        assertEquals(0, p.cC, "g.cC = 0"); assertEquals(0, p.cD,  "g.cD = 0")
        assertEquals(0, p.cE, "g.cE = 0")
        assertEquals(5120, p.cF, "g.cF = 5120")
        assertEquals(0, p.cH, "g.cH = 0"); assertEquals(0, p.cI, "g.cI = 0")
        assertEquals(0, p.cJ, "g.cJ = 0"); assertEquals(0, p.cK, "g.cK = 0")
        assertEquals(0, p.cL, "g.cL = 0"); assertEquals(0, p.cM, "g.cM = 0")
        assertEquals(0, p.K, "g.K = 0")
        assertEquals(0, p.cN, "g.cN = 0")
        assertEquals(0, p.gQL, "g.L = 0")
        assertEquals(0, p.gQM, "g.M = 0")
        // i.<init>([S)V record load (i.javap @201-274)
        assertEquals(rec[1], p.aw, "aw = r[1]")
        assertEquals(rec[6], p.P, "P = r[6] (i.javap @269-274)")
        assertEquals(rec[6] and 1 != 0, p.av, "av = (r[6]&1)!=0")
        assertSame(w.clips[Level0World.entityClipIndex(rec[0], rec) ?: 0],
                   p.clip, "clip = k.r(bi[ax]) (i.javap @283-468)")
        // JVM-zero fields
        assertEquals(0, p.T); assertEquals(0, p.U); assertEquals(0, p.a)
        assertEquals(0, p.V); assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.ai); assertEquals(0, p.aj)
        assertEquals(0, p.aC); assertEquals(0, p.aE); assertEquals(0, p.aF)
        assertEquals(0, p.aG); assertEquals(0, p.ay); assertEquals(0, p.nl)
        assertEquals(0, p.eventN); assertEquals(0, p.pv)
        assertEquals(0, p.m); assertEquals(0, p.j); assertEquals(0, p.l)
        assertEquals(0, p.am); assertEquals(0, p.an); assertEquals(0, p.ao)
        assertEquals(0, p.ap); assertEquals(0, p.bY); assertEquals(0, p.bZ)
        assertEquals(0, p.bl); assertEquals(0, p.co)
        // tc/uc (i.t/u) are a() hitbox outputs — E()'s probe rewrites them
        // before anything reads the ctor zero (write-before-read)
        assertEquals(0, p.aO); assertEquals(0, p.aT)
        assertFalse(p.ba); assertFalse(p.bb); assertFalse(p.bc)
        // bd is an x() probe flag — a(true) inside E() rewrites it before
        // anything reads the ctor false (write-before-read)
        assertTrue(p.b, "i.b = true — E() @7 writes it inside the ctor tail")
        assertEquals(0, p.aD)
        assertFalse(p.runnerB); assertEquals(0, p.runnerD)
        assertFalse(p.iE); assertFalse(p.runnerG)
        assertEquals(0, p.scriptStep); assertNull(p.scriptOps)
        // g.y = al is written inside i(r[5]) (Entity.kt:1032, S0 arm) — the
        // fresh ctor re-latches it at the RECORD position every spawn
        assertEquals(rec[3], p.gy, "g.y = al via i(r[5])")
        assertEquals(0, p.homeX); assertEquals(0, p.homeY)
        assertEquals(0, p.palette); assertEquals(-1, p.remapTable)
        assertEquals(255, p.paletteAlpha); assertEquals(-1, p.j0Frame)
        assertEquals(0, p.hitsTaken); assertEquals(-1, p.trailAnim)
        assertEquals(1, p.ropeGrabSeg, "bN mirror")
        // links die with the body
        assertNull(p.af); assertNull(p.ab); assertNull(p.ad); assertNull(p.ae)
        assertNull(p.s); assertNull(p.c); assertNull(p.F); assertNull(p.g)
        assertNull(p.gg); assertNull(p.cg); assertNull(p.ch)
        assertNull(p.standingOn); assertNull(p.ac)
        assertNull(p.wpBt); assertNull(p.wpF)
        assertNull(p.cS); assertNull(p.cU)
        assertTrue(p.Z.all { it == 0 }, "i.Z zeroed")
        // W/X/Y are the a()/refreshBoxes hitbox rects — E()'s probe
        // rewrites them at the spawn position (write-before-read)
        assertTrue(p.cd.all { !it }, "i.cd zeroed")
        p.cb?.let { assertTrue(it.all { v -> v == 0 }, "i.cb zeroed") }
        p.cc?.let { assertTrue(it.all { v -> v == 0 }, "i.cc zeroed") }
        p.cf?.let { assertTrue(it.all { v -> v == 0 }, "i.cf zeroed") }
        p.cQ?.let { assertTrue(it.all { v -> v == 0 }, "i.cQ zeroed") }
        assertEquals(0, p.bh, "i.bh = 0 (D() @181)")
        assertEquals(w.kAx, p.x1, "x1 = kAx (reload tail :4830)")
    }

    @Test fun `mission reload re-arms every ctor field on the reused player`() {
        val w = world()
        stageCtorFields(w.player)
        w.resetLevel(false)                     // loadMission(kAj) — snap null
        assertCtorState(w)
    }

    @Test fun `checkpoint restore keeps N and O at the record position`() {
        val w = world()
        val rec = w.level.entities.first { it.isNotEmpty() && it[0] == 0 }
        fireCheckpoint(w, w.checkpoints.first())
        val snap = w.checkpointSnap ?: error("checkpoint did not fire")
        stageCtorFields(w.player)
        w.resetLevel(true)                      // reload(true) — a(Z)V arm
        val p = w.player
        // k.a(Z)V @206-243: ak/al jump to the snapshot, av restored — but the
        // arm never writes N/O, so the fresh object's 8.8 position stays at
        // the record spawn (proven quirk).
        assertEquals(snap.ak, p.ak, "ak = snap")
        assertEquals(snap.al, p.al, "al = snap")
        assertEquals(rec[2] shl 8, p.N, "N stays at record spawn")
        assertEquals(rec[3] shl 8, p.O, "O stays at record spawn")
        assertEquals(snap.av, p.av, "av = snap facing")
        assertCtorState(w)
    }

    @Test fun `cG latches the record facing not the restored facing`() {
        val w = world()
        val rec = w.level.entities.first { it.isNotEmpty() && it[0] == 0 }
        fireCheckpoint(w, w.checkpoints.first())
        w.player.cG = true
        w.resetLevel(true)
        // g.<init> @66-70: cG = i.av — the RECORD load's facing, read before
        // the a(Z)V arm overwrites av with the snapshot.
        assertEquals(rec[6] and 1 != 0, w.player.cG, "cG = record-facing av")
    }

    @Test fun `statics outside the D() clear list persist across respawn`() {
        val w = world()
        val p = w.player
        val link = Entity(4, null)
        p.gt = 7; p.gn = 50; p.go = 60
        p.gB = true; p.gD = true
        p.gI = 2; p.gJ = 8
        p.cFlag = true; p.gcm = true; p.ci = link; p.z = true
        p.cp = true; p.cq = true; p.ct = true; p.cw = true; p.cv = true; p.cu = true
        Entity.gf = link                          // g.f marker FX — persists
        w.resetLevel(false)
        // g.{t,y,n,o,B,D,I,J,C,cm,ci,cp,cu,z,f} are NOT in i.D()'s clear list
        assertEquals(7, p.gt)
        assertEquals(50, p.gn); assertEquals(60, p.go)
        assertTrue(p.gB); assertTrue(p.gD)
        // g.I/g.J also persist — but the loadMission arm runs F(aj), which
        // writes g.I=1 and g.J|=f0do[aj] (Level0World.kt:4081-4083, proven)
        assertEquals(1, p.gI, "g.I = 1 — F(aj) rewrite, not D()-cleared")
        assertEquals(5, p.gJ, "g.J |= f0do[0]=5 — F(aj) rewrite")
        assertTrue(p.cFlag); assertTrue(p.gcm)
        assertSame(link, p.ci); assertTrue(p.z)
        assertTrue(p.cp); assertTrue(p.cq); assertTrue(p.ct)
        assertTrue(p.cw); assertTrue(p.cv); assertTrue(p.cu)
        assertSame(link, Entity.gf, "g.f survives D()")
    }

    @Test fun `g-b links and D()-cleared statics die with the body`() {
        val w = world()
        val dead = Entity(4, null)
        w.player.g = dead; w.player.gg = dead; w.player.F = dead
        w.playerLinkB = dead
        Entity.gq = true; Entity.grabLatch = true; Entity.gE = true
        w.resetLevel(false)
        assertNull(w.player.g, "g.g — D() @69")
        assertNull(w.player.gg)
        assertNull(w.player.F, "g.F — D() @105")
        assertNull(w.playerLinkB, "g.b — D() @49")
        assertFalse(Entity.gq, "g.q — D() cleared")
        assertFalse(Entity.grabLatch, "g.j — D() cleared")
        assertFalse(Entity.gE, "g.E — D() cleared")
    }
}
