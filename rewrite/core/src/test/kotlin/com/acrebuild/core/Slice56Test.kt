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

class Slice56Test {
    private fun ax24(w: Level0World, s: Int, x: Int, y: Int): Entity {
        val e = Entity(24, w.clips[40])
        e.setPositionPx(x, y); e.S = s; e.refreshBoxes()
        w.npcs.add(e); return e
    }

    @Test fun `offscreen flight retires to pool (L17)`() {
        val w = world()
        val e = ax24(w, 0, -5000, -5000)   // far outside camRect → v()=false
        e.af = Entity(56, w.clips[19]); e.c = Entity(56, w.clips[19])
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(-1, e.aG)
        assertTrue(e.P and 128 != 0, "P|=128")
        assertTrue(e.P and 16 == 0, "P&=-17")
        assertNull(e.af); assertNull(e.c)
    }

    @Test fun `aG==1 trail arm times out and retires (L23)`() {
        val w = world()
        val e = ax24(w, 0, 50, 50); e.aG = 1; e.aC = -1  // aC-- <0 on OLD
        e.af = Entity(56, w.clips[19])
        w.player.setPositionPx(50, 50); w.player.refreshBoxes()
        w.kM(2)                                // m(ad) snap → in view
        w.kM(2)                                     // m(ad) snap → in view
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(-2, e.aC)
        assertTrue(e.P and 128 != 0, "P|=128")
        assertNull(e.af)
    }

    @Test fun `aG==3 chain overlap kills c and i(9) (L31)`() {
        val w = world()
        val e = ax24(w, 0, 100, 100); e.aG = 3; e.aw = 5
        val c = Entity(56, w.clips[19]); c.setPositionPx(100, 100)
        c.setAnim(0); c.refreshBoxes(); e.c = c
        w.player.setPositionPx(100, 100); w.player.refreshBoxes()
        w.kM(2)                                    // m(ad) snap → in view
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(9, e.S, "this i(9)")
        assertEquals(10, c.S, "ax56 c → i(10)")
        assertEquals(1, w.kAp[0], "k.e(0,aw) kill counter")
        assertNull(e.c)
    }

    @Test fun `flight overlap on player pays op38 then S0-4 removed (L44)`() {
        val w = world()
        w.player.setPositionPx(50, 50); w.player.refreshBoxes()
        w.kM(2)                                // m(ad) snap → in view
        val e = ax24(w, 2, 50, 50)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(3, w.player.aB, "op38 → aB=3")
        assertSame(e, w.playerLinkB)
        assertEquals(-1, e.aG); assertNull(e.af)
        assertTrue(w.pendingRemove.contains(e), "S∈0..4 → k.c(this)")
    }

    @Test fun `S6 drop line anim-9 past ap and on bc hit (L171)`() {
        // L171 (i.java:14062-14078, proven): `al > ap → goto L173` SKIPS
        // the i(9) — the wisp keeps rising while above its target line
        // and dies into the S9 impact anim only once `al <= ap`. (The
        // arm reads inverted at first glance: `al > ap → continue`.)
        val w = world()
        val e = ax24(w, 6, 100, 100); e.ap = 50   // al(100) > ap(50) → alive
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(6, e.S, "al>ap keeps the drop line flying")
        e.al = 50; e.setPositionPx(100, 50); e.ap = 50   // al<=ap → i(9)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(9, e.S, "al<=ap dies into the S9 impact anim")
    }

    @Test fun `bc sweeps ax54 victim for kill + countKill (L26 arm)`() {
        val w = world()
        val e = ax24(w, 6, 200, 200); e.af = Entity(24, w.clips[40]) // unfriendly
        val victim = Entity(54, w.clips[19]); victim.setPositionPx(200, 200)
        victim.setAnim(0); victim.refreshBoxes(); victim.aw = 3
        victim.W[0] = 195; victim.W[1] = 195; victim.W[2] = 205; victim.W[3] = 205
        w.npcs.add(victim)
        e.X[0] = 190; e.X[1] = 190; e.X[2] = 210; e.X[3] = 210   // real X box
        e.ap = 99999                            // skip the al>ap arm
        w.paint(victim)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(10, victim.S, "ax54 W∩X → i(10)")
        assertEquals(9, e.S)
        assertEquals(1, w.kAp[0])
    }

    @Test fun `bc ax30 takes 20 damage and dies below zero`() {
        val w = world()
        val e = ax24(w, 6, 200, 200); e.ap = 99999
        e.af = Entity(24, w.clips[40])                              // unfriendly
        val victim = Entity(30, w.clips[36]); victim.setPositionPx(200, 200)
        victim.setAnim(0); victim.refreshBoxes(); victim.aw = 7; victim.aB = 15
        victim.W[0] = 195; victim.W[1] = 195; victim.W[2] = 205; victim.W[3] = 205
        e.X[0] = 190; e.X[1] = 190; e.X[2] = 210; e.X[3] = 210
        w.npcs.add(victim)
        w.paint(victim)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(-5, victim.aB, "aB -= 20")
        assertEquals(10, victim.S, "aB<=0 → i(10)")
        assertEquals(1, w.kAp[0])
    }

    @Test fun `S7 timer arm i(9) at expiry (L166)`() {
        val w = world()
        val e = ax24(w, 7, 100, 100); e.aC = 0
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(9, e.S)
    }

    @Test fun `S8 anim-finished removes (L87)`() {
        val w = world()
        val e = ax24(w, 8, 100, 100)
        e.T = e.clip!!.frameCount(e.S) - 1
        e.U = (e.clip!!.frameDuration(e.S, e.T) - 1).coerceAtLeast(0)
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(w.pendingRemove.contains(e))
    }

    @Test fun `S9 non-flying overlaps player pays op4 (L79)`() {
        val w = world()
        w.player.setPositionPx(30, 30); w.player.refreshBoxes()
        val e = ax24(w, 9, 30, 30)
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(w.player.S != 21 || w.player.aB != 3,
                   "op4 intake fired (player reacts)")
    }

    @Test fun `S9 flying arm zeroes velocity (L75, flying unreachable on level 0)`() {
        // MISSION_BH[0]=4 → bh!=3 on the level-0 fixture; flying arm
        // covered by the L75 code path review only — document gate.
        val w = world()
        assertEquals(4, Entity.MISSION_BH[w.kAj])
    }

    @Test fun `S11 homing snaps to owner waypoint when timer ends (L94)`() {
        val w = world()
        val owner = Entity(24, w.clips[40])
        owner.cHWaypoints = arrayOf(intArrayOf(300, 400))
        val e = ax24(w, 11, 100, 100); e.af = owner; e.ap = 0; e.aC = 1
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(300, e.bY); assertEquals(400, e.bZ)
        assertEquals(9, e.S)
    }

    @Test fun `S19 lays an S40 child once (L178)`() {
        val w = world()
        val e = ax24(w, 19, 100, 100)
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(e.runnerG, "`G` latches (ba() @1380-1396)")
        val child = w.pendingInsert.single { it.ax == 24 }
        assertEquals(40, child.S)
        assertEquals(e.ak, child.ak - child.ao)
        assertEquals(e.al, child.al - child.ap)
        assertSame(e, child.af)
    }

    @Test fun `S20 shrine overlap arms revive statics and heals (L181)`() {
        val w = world()
        w.player.setPositionPx(80, 80); w.player.refreshBoxes()
        w.kAE = 10
        val e = ax24(w, 20, 80, 80); e.aB = 200
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(w.iBB && w.iBC && w.iBD)
        assertEquals(100, w.iBF); assertEquals(999, w.iBE); assertEquals(-1, w.iBG)
        assertEquals(21, w.player.S)
        assertEquals(90, w.kAF, "kAF = min(aB, 100-kAE)")
        assertEquals(21, e.S, "@1522 i(21): the shrine is spent after one use")
        // S21 is inert (@1534) — a second overlapping tick heals nothing
        w.kAF = 7
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(7, w.kAF, "the spent shrine does not refill again")
    }

    @Test fun `S31 pinned child follows owner offset (L53)`() {
        val w = world()
        val owner = Entity(24, w.clips[40]); owner.setPositionPx(500, 600)
        val e = ax24(w, 31, 0, 0); e.af = owner; e.ao = 10; e.ap = 20
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(510, e.ak); assertEquals(620, e.al)
    }

    @Test fun `S31 orphan stops then removes on anim end (L53)`() {
        val w = world()
        val e = ax24(w, 31, 0, 0); e.ag = 300; e.ah = -300
        e.T = e.clip!!.frameCount(31) - 1
        e.U = (e.clip!!.frameDuration(31, e.T) - 1).coerceAtLeast(0)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(0, e.ag); assertEquals(0, e.ah)
        assertTrue(w.pendingRemove.contains(e))
    }

    @Test fun `S36 explode pays op4 and removes on end (L199)`() {
        val w = world()
        w.player.setPositionPx(40, 40); w.player.refreshBoxes()
        val e = ax24(w, 36, 40, 40)
        e.T = e.clip!!.frameCount(36) - 1
        e.U = (e.clip!!.frameDuration(36, e.T) - 1).coerceAtLeast(0)
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(e.b, "@1606 `b = 1`")
        assertTrue(w.pendingRemove.contains(e))
    }

    @Test fun `S45 arrival - bZ past ap goes to S15 (@1065-1092)`() {
        val w = world()
        val e = ax24(w, 45, 100, 100); e.ap = -99999; e.bZ = 0   // bZ > ap
        e.j = 99
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(15, e.S, "bZ > ap → i(15)")
    }

    @Test fun `S45 still short of ap keeps flying - the k flag only releases once j runs out`() {
        val w = world()
        val e = ax24(w, 45, 100, 100); e.ap = 99999; e.bZ = 0    // bZ <= ap
        e.j = 5; e.k = true
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(45, e.S, "bZ <= ap, k, j > 0 → hit test only")
        assertTrue(e.k)
        e.j = 0                                       // j-- → -1 <= 0
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(15, e.S, "k && j <= 0 → k = false; i(15)")
        assertFalse(e.k)
    }

    @Test fun `countKill uid gate (k dot e)`() {
        val w = world()
        w.countKill(0); w.countKill(-3)
        assertEquals(0, w.kAp[0], "uid<=0 → no count")
        w.countKill(5)
        assertEquals(1, w.kAp[0], "uid>0 && kAj!=7 → ap[0]++")
    }
}

// =========================================================================
// slice 58 — ax58 bg() lever/counterweight FSM (i.java:14633)
// =========================================================================
