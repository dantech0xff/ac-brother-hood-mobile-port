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

// =========================================================================
// Slice 62 — ax17 = aA() civilian FSM (i.java:8708-8847 + init :3004 L113).
// =========================================================================
class Slice17Test {

    /** 15-field ax17 record: [17, uid, x, y, 0, S, 0|1, 0, 0, -1, 0, 0, -1,
     *  100, -1] (level-records.json packs 8/9/11, proven). */
    private fun ax17At(w: Level0World, x: Int, y: Int, anim: Int = 57): Entity {
        val e = Entity(17, w.clips[7])
        val rec = mutableListOf(
            17, 152, x, y, 0, anim, 0, 0, 0, -1, 0, 0, -1, 100, -1)
        e.setPositionPx(x, y)
        w.npcFsm.initAx17(e, rec)
        w.npcs.add(e)
        return e
    }

    /** Place the player and give it real boxes; S reset to a non-attack anim. */
    private fun placePlayer(w: Level0World, x: Int, y: Int) {
        w.player.setPositionPx(x, y)
        w.player.refreshBoxes()
        w.player.S = 0
    }

    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }

    @Test fun `init — L113 maps fields, L395 sets anim, corpse link Z21`() {
        val w = world()
        val e = ax17At(w, 200, 150, 64)
        assertEquals(64, e.S, "i(r8[5])")
        assertEquals(100, e.aB, "aB = bv[k.au] = bv[0] = 100")
        assertEquals(100, e.az, "az = r8[13]")
        assertEquals(-1, e.Z[21], "Z[21] = r8[14] corpse link")
        assertEquals(-1, e.Z[2], "Z[2] = -1")
        assertEquals(200, e.aF, "aF = r8[2] = x")
        assertEquals(0, e.aD, "aD = r8[7]")
        assertEquals(0, e.m, "m = r8[8]")
        assertEquals(-1, e.oId, "o = r8[9]")
        assertEquals(0, e.aG, "aG = r8[4]")
    }

    @Test fun `S57 on-screen notice — sfx 16, faces player, panic pick`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect() // camera [0,0,400,240]
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)                        // same spot → overlap
        w.npcFsm.tickAx17(e, w, w.player)
        assertTrue(16 in w.sfxLog, "k.A(16) notice sfx")
        assertEquals(61, e.S, "X-overlap + vert-overlap → i(61)")
        assertEquals(0, e.ag); assertEquals(0, e.ah)
    }

    @Test fun `S57 civilian off camera stays idle, no sfx`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 2000, 150)                    // W outside [0,400]
        placePlayer(w, 2100, 150)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S)
        assertTrue(16 !in w.sfxLog)
    }

    @Test fun `S57 alert flag bn suppresses the panic`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        w.iBn = true                                    // bA[79] checkpoint flag
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S)
        assertTrue(16 !in w.sfxLog)
    }

    @Test fun `S57 player S9 or S50 suppresses the panic`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)
        w.player.S = 9
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "S9 climb suppresses")
        w.player.S = 50
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "S50 riding suppresses")
    }

    @Test fun `S57 quadrant pick — below+X-sep 67, above+X-sep 66`() {
        val w = world()
        // slice 350: S57 runs the full l() — LOS e(aS) included — so both
        // placements stay inside level0's open top-left block (cells
        // 0..28 x 0..24); y=120 put the "above" player off the map (y<0,
        // OOB cells read solid). Camera kP=100 keeps W inside k.ac.
        w.kO = 0; w.kP = 100; w.rebuildCamRect()
        val e = ax17At(w, 150, 320)
        // player fully right of W (X-separated) and well below/above it —
        // p.W carries large anim-box offsets so the margin must clear them
        placePlayer(w, e.W[2] + 200, e.W[3] + 200)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(67, e.S, "X-separated + player below → i(67)")
        e.setAnim(57); e.T = 0
        placePlayer(w, e.W[2] + 200, e.W[1] - 200)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(66, e.S, "X-separated + player above → i(66)")
    }

    @Test fun `S57 quadrant pick — below+overlap 63, above+overlap 62`() {
        val w = world()
        w.kO = 0; w.kP = 100; w.rebuildCamRect() // see the X-sep case
        val e = ax17At(w, 150, 320)
        placePlayer(w, e.ak, e.W[3] + 200)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(63, e.S, "X-overlap + player below → i(63)")
        e.setAnim(57); e.T = 0
        placePlayer(w, e.ak, e.W[1] - 200)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(62, e.S, "X-overlap + player above → i(62)")
    }

    @Test fun `panic flail strikes the player at T==3 (op4 sfx 18)`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)
        e.setAnim(61); e.T = 3
        w.npcFsm.tickAx17(e, w, w.player)
        assertTrue(18 in w.sfxLog, "aS.a(4,0,0,this) → player hurt sfx")
    }

    @Test fun `panic flail r returns to S57 — S68 also returns`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 300, 150)
        e.setAnim(61); finish(e)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "r() → i(57)")
        e.setAnim(68); finish(e)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(57, e.S, "S68 r() → i(57)")
    }

    @Test fun `sword intake — normal hit aB-=50, finisher aB-=100`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)
        // p.X non-degenerate, overlapping W; player in attack anim 67
        w.player.S = 67
        w.player.X[0] = e.W[0]; w.player.X[1] = e.W[1]
        w.player.X[2] = e.W[2]; w.player.X[3] = e.W[3]
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(50, e.aB, "H[0]=50 for normal attack anims")
        // finisher 183: reset entity, drain once more
        val e2 = ax17At(w, 200, 150)
        w.player.S = 183
        w.npcFsm.tickAx17(e2, w, w.player)
        assertEquals(0, e2.aB, "J[0]=100 finisher kills a 100-HP civilian")
    }

    @Test fun `dead-check routes to S69 collapse on the next tick`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 200, 150)
        e.aB = 0
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(69, e.S, "L7 dead-check → i(69)")
    }

    @Test fun `S69 collapse — finish releases locks and drops flags`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        placePlayer(w, 300, 150)
        e.aB = 0
        e.P = e.P or 16
        w.lockTarget = e
        w.playerLinkB = e
        e.setAnim(69); finish(e)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(0, e.P and 16, "P&=-17")
        assertTrue(e.P and 32 != 0 && e.P and 64 != 0, "P|=32|64")
        assertNull(w.lockTarget, "aN released")
        assertNull(w.playerLinkB, "g.b released")
        assertEquals(2, e.aA, "aA=2")
    }

    @Test fun `S129 dead-on-spot — r() zeroes aB and drops flags`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        e.aB = 1
        e.setAnim(129); finish(e)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(0, e.aB)
        assertTrue(e.P and 32 != 0 && e.P and 64 != 0)
        assertEquals(129, e.S, "stays dead (aB<=0 early-return, no i(69))")
    }

    @Test fun `S170 knockdown — al+=10, wall probe, aZ → S129`() {
        val w = world()
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        val e = ax17At(w, 200, 150)
        val y0 = e.al
        e.setAnim(170)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(y0 + 10, e.al, "al += 10")
        assertTrue(e.S == 129 || e.S == 170,
            "aZ wall contact → i(129), else keep falling")
    }
}
