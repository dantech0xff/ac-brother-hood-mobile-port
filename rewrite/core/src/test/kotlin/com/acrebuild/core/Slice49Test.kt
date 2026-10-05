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

class Slice49Test {

    /** ax15 record (i.java:2669 → L362): `[15, aw, x, y, aE, S, P,
     *  az(r8[7]), z0(r8[8]), ...]` → `az=r8[7]`, Z-load `r8[8]/ak/al`. */
    private fun ax15At(w: Level0World, x: Int, y: Int, s: Int,
                       z7: Int = 0, z8: Int = -1): Entity {
        val e = Entity(15, w.clips[25])
        e.setPositionPx(x, y)
        val f = mutableListOf(15, 0, x, y, 0, s, 0, z7, z8)
        for (i in 9..15) f += 0
        w.npcFsm.initAx15(e, f, w)
        w.npcs.add(e)
        return e
    }

    private fun box(e: Entity, x0: Int, y0: Int, x1: Int, y1: Int) {
        e.W[0] = x0; e.W[1] = y0; e.W[2] = x1; e.W[3] = y1
    }

    private fun animEnd(e: Entity, w: Level0World) {
        e.T = w.clips[25]!!.frameCount(e.S) - 1
        e.U = w.clips[25]!!.frameDuration(e.S, e.T) - 1
    }


    @Test fun `init S9 arms the eight-slot Z with sentinel -1`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 9, z7 = 3, z8 = 42)
        assertEquals(3, e.az, "L362 az=r8[7]")
        assertEquals(-1, e.Z[4], "L362 Z[4]=-1 for S9")
        assertEquals(42, e.Z[0]); assertEquals(400, e.Z[1])
        assertEquals(700, e.Z[2]); assertEquals(0, e.Z[3])
        assertEquals(9, e.S)
    }

    @Test fun `init S6 keeps the four-slot Z`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6, z8 = 7)
        assertEquals(0, e.Z[4], "S6 Z stays int[4] — no -1 sentinel")
        assertEquals(7, e.Z[0]); assertEquals(400, e.Z[1]); assertEquals(700, e.Z[2])
    }

    @Test fun `S9 tick only marks P16`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 9)
        w.npcFsm.tickAx15(e, w, w.player)
        assertTrue(e.P and 16 != 0, "L4 → P|=16")
        assertEquals(9, e.S)
    }

    @Test fun `clip25 leaves the S6 box empty like the original`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6)
        assertEquals(e.ak, e.W[0]); assertEquals(e.al, e.W[1])
        assertEquals(e.W[0], e.W[2]); assertEquals(e.W[1], e.W[3])
    }

    @Test fun `S7 respawn arm returns the block to Z1-Z2 as S6`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6)             // Z loaded at init
        e.setAnim(7)
        e.ak += 40; e.al += 30                    // moved off its home
        animEnd(e, w)
        w.npcFsm.tickAx15(e, w, w.player)
        assertEquals(6, e.S, "L202 → i(6) when Z[3]==0")
        assertEquals(400, e.ak); assertEquals(700, e.al)
        assertFalse(e in w.pendingRemove)
    }

    @Test fun `S7 with Z3 marked removes the entity`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6)
        e.setAnim(7)
        e.Z[3] = 1
        animEnd(e, w)
        w.npcFsm.tickAx15(e, w, w.player)
        assertTrue(e in w.pendingRemove, "L203 → k.c(this)")
    }

    @Test fun `contact arms stay dormant on the empty S6 rect`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6)
        val p = w.player
        p.setPositionPx(400, 700); p.refreshBoxes()
        p.S = 18; p.av = false                    // free anim, on the anchor
        w.npcFsm.tickAx15(e, w, w.player)
        assertNull(p.ga, "empty rect → no claim (clip25 ao spans 1:1)")
        assertEquals(18, p.S)
        assertEquals(6, e.S)
    }

    @Test fun `release frees an externally claimed player`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 400, 700, 6)
        val p = w.player
        p.setPositionPx(300, 400); p.refreshBoxes()
        p.S = 18; p.ga = e                        // e.g. a claim-script bind
        w.npcFsm.tickAx15(e, w, w.player)
        assertNull(p.ga, "L119 → g.a=null (point rect never overlaps)")
    }

    @Test fun `unsupported block falls at 4096 while grounded settles`() {
        val w = world(); w.npcs.clear()
        // anchor over two air cells (point-W probes under the anchor)
        var cx = -1; var cy = -1
        outer@ for (yy in 1 until w.level.rows - 1)
            for (xx in 1 until w.level.cols - 2) {
                val a = w.level.collisionCell(xx, yy)
                if (a < 12 && a != 5) { cx = xx; cy = yy; break@outer }
            }
        assertTrue(cx > 0, "fixture: found an air cell")
        val air = ax15At(w, cx * 20 + 10, cy * 20 + 10, 6)
        w.npcFsm.tickAx15(air, w, w.player)
        assertEquals(4096, air.ah, "L130 unsupported → ah=4096")
        // one solid cell (>=12 or ==5) beneath the anchor
        var sx = -1; var sy = -1
        outer@ for (yy in 1 until w.level.rows - 1)
            for (xx in 1 until w.level.cols - 1) {
                val a = w.level.collisionCell(xx, yy)
                if (a >= 12 || a == 5) { sx = xx; sy = yy; break@outer }
            }
        assertTrue(sx > 0, "fixture: found a solid cell")
        val grounded = ax15At(w, sx * 20 + 10, sy * 20 - 1, 6)
        w.npcFsm.tickAx15(grounded, w, w.player)
        assertTrue(grounded.bd, "L137 grounded → bd=true")
        assertEquals(0, grounded.ah)
    }

    @Test fun `ax66 mount snaps the anchor then unmounts same-tick`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 130, 135, 6)             // anchor inside platform box
        val m = Entity(66, null); m.setPositionPx(130, 140)
        box(m, 90, 130, 170, 160)
        w.npcs.add(m)
        w.paint(e, m)
        w.npcFsm.tickAx15(e, w, w.player)
        assertEquals(131, e.al, "L167 mount → al=n.W[1]+1")
        assertNull(e.s,
            "L172: point-W never strict-overlaps → same-tick unmount")
    }

    @Test fun `S10 arms the real clip rect and sweeps hostiles`() {
        val w = world(); w.npcs.clear()
        val e = ax15At(w, 200, 200, 10)
        // proven bounds: clip25 rect0 = [-31,-50,57,51]
        assertEquals(200 - 31, e.W[0]); assertEquals(200 - 50, e.W[1])
        assertEquals(200 - 31 + 57, e.W[2]); assertEquals(200 - 50 + 51, e.W[3])
        e.ah = 512                                  // bt() requires ah!=0
        val n = Entity(11, null); n.setPositionPx(200, 175)
        box(n, 180, 160, 200, 180)
        n.aB = 100; n.S = 3                         // ALIVE: bt() sweeps `!P()` (slice 387)
        w.npcs.add(n)
        w.npcFsm.tickAx15(e, w, w.player)
        assertTrue(e.P and 16 != 0, "L5 → P|=16")
        assertEquals(0, n.aB, "bt() → as() zeroes the alive soldier's aB")
        assertEquals(0, n.S, "bt() → as() → ax11 i(0)")
    }
}

// =====================================================================
// Slice 50 — ax46 aZ() spring/trap (i.java:13568-13726, proven).
// =====================================================================
