package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 387 — removal, the `bt()` sweep and two draw-pass/dispatch arms:
 * - `k.c(i)` (bytecode @26-91): a pool member is `p()`'d at once — boxes and
 *   `ab`/`ad`/`ae`/`af`/`c` dropped — and its slot freed; a second `k.c`
 *   finds no slot.
 * - `bt()` (@95-98 `P(); ifne next`): the sweep kills the ALIVE hostiles
 *   (`!P()`); the port skipped the alive ones.
 * - `bu()` S10 (@54-69): `P|=16; bt()` falls straight into the S6/S8 body
 *   (`z2 = S==10 && Z[0]!=-1`, @134-162; `aM() || z2` settle, @1125-1148).
 * - `b(Z)` ax21 S1 (@1910-1966): `C==null || u!=9` clears `ad.P&=-65`, then
 *   `ad.s()` runs unconditionally.
 */
class Slice387Test {
    /** A free-standing cell column: air at the cell the block's foot probes. */
    private fun airCell(w: Level0World): Pair<Int, Int> {
        for (cy in 2 until w.level.worldH / 20 - 2)
            for (cx in 2 until w.level.worldW / 20 - 2) {
                val v = w.collisionCell(cx, cy)
                if (v < 5) return cx to cy
            }
        error("no air cell in the fixture")
    }

    /** A clipless ax15 block staged by hand so its foot probes an air cell. */
    private fun airBlock(w: Level0World, s: Int, z0: Int): Entity {
        val (cx, cy) = airCell(w)
        val e = Entity(15, null)
        val f = mutableListOf(15, 0, cx * 20 + 10, cy * 20 + 5, 0, s, 0, 0, z0)
        for (i in 9..15) f += 0
        w.npcFsm.initAx15(e, f, w)
        e.setPositionPx(cx * 20 + 10, cy * 20 + 5)
        // the foot (W[3]+1)/20 lands in cell row `cy`; both edges in column `cx`
        intArrayOf(cx * 20 + 2, cy * 20 - 30, cx * 20 + 17, cy * 20 + 5).copyInto(e.W)
        w.npcs += e
        assertFalse(e.supportedByGround(w), "fixture: the block's foot is over air")
        return e
    }

    private fun farPlayer(w: Level0World) {
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
    }

    // ------------------------------------------------------------ k.c(i) → p()

    @Test fun `k-c() p()s a pool member at once and a second k-c does nothing`() {
        val w = world(); w.npcs.clear()
        val e = Entity(11, null)
        intArrayOf(10, 10, 30, 30).copyInto(e.W); intArrayOf(5, 5, 35, 35).copyInto(e.X)
        e.ae = Entity(14, null); e.af = Entity(24, null); e.c = Entity(51, null)
        e.ab = Entity(14, null); e.ad = Entity(68, null).also { intArrayOf(1, 2, 3, 4).copyInto(it.W) }
        val ad = e.ad!!
        w.npcs += e
        w.removeEntity(e)
        assertTrue(e in w.pendingRemove)
        assertEquals(listOf(0, 0, 0, 0), e.W.toList(), "p(): W = null")
        assertEquals(listOf(0, 0, 0, 0), e.X.toList())
        assertNull(e.ae); assertNull(e.af); assertNull(e.c); assertNull(e.ab); assertNull(e.ad)
        assertEquals(listOf(0, 0, 0, 0), ad.W.toList(), "p() cascades into ad")
        // k.c(e) again: bb[i] is null by now — nothing is found, nothing is p()'d
        e.ae = Entity(14, null)
        w.removeEntity(e)
        assertTrue(e.ae != null, "a second k.c finds no slot (bytecode @35-41)")
    }

    @Test fun `k-c() leaves an entity that is not in the pool alone`() {
        val w = world(); w.npcs.clear()
        val marker = Entity(14, null)                    // aS.a(n,x,y): never k.b-inserted
        intArrayOf(10, 10, 30, 30).copyInto(marker.W)
        w.removeEntity(marker)
        assertEquals(listOf(10, 10, 30, 30), marker.W.toList())
    }

    @Test fun `k-c() on a same-frame spawn dequeues it so the drain never inserts it`() {
        val w = world(); w.npcs.clear()
        val e = Entity(11, null)
        intArrayOf(10, 10, 30, 30).copyInto(e.W)
        w.queueInsert(e)                                 // k.b(e): the port queues it for the drain
        w.removeEntity(e)                                // k.c(e) in the same frame
        assertEquals(listOf(0, 0, 0, 0), e.W.toList(), "p() ran on the queued spawn")
        assertFalse(e in w.pendingInsert, "k.b put it in bb[]; k.c freed that slot")
        w.tick(emptyList())                              // the frame-end drains
        assertFalse(e in w.npcs, "a removed spawn must not reappear after the drain")
    }

    @Test fun `a removed entity is inert in the rest of the frame's bd scans`() {
        val w = world(); w.npcs.clear()
        val sweeper = Entity(16, null)
        intArrayOf(100, 100, 140, 140).copyInto(sweeper.X)
        val n = Entity(19, null); n.S = 2
        intArrayOf(115, 115, 125, 125).copyInto(n.W)
        w.npcs += n
        w.paint(n)
        sweeper.sweepNeighbors(w)
        assertEquals(3, n.S, "control: a drawn ax19 S2 in X → i(3)")
        n.S = 2
        w.removeEntity(n)                                // stale bd slot, W = null
        sweeper.sweepNeighbors(w)
        assertEquals(2, n.S, "p() left the stale slot with no box (bd() @43)")
    }

    // ------------------------------------------------------------ bt()

    private fun hostile(w: Level0World, ax: Int, aB: Int, s: Int): Entity {
        val e = Entity(ax, null).apply { this.aB = aB; S = s }
        intArrayOf(100, 100, 140, 140).copyInto(e.W)
        w.npcs += e
        return e
    }

    private fun faller(): Entity = Entity(78, null).apply {
        ah = 512; intArrayOf(110, 90, 130, 130).copyInto(W)
    }

    @Test fun `bt() kills the alive hostiles under a falling block`() {
        val w = world(); w.npcs.clear()
        val sol = hostile(w, 11, 100, 3)
        val gua = hostile(w, 17, 100, 3)
        val bru = hostile(w, 23, 100, 3)
        val arc = hostile(w, 50, 100, 3)
        faller().sweepHostiles(w)
        assertEquals(0, sol.aB); assertEquals(0, sol.S, "ax11 → i(0)")
        assertEquals(0, gua.aB); assertEquals(69, gua.S, "ax17 → i(69)")
        assertEquals(0, bru.aB); assertEquals(79, bru.S, "ax23 → i(79)")
        assertEquals(0, arc.aB); assertEquals(3, arc.S, "ax50: as() has no anim arm")
    }

    @Test fun `bt() passes over corpses, other types and a resting block`() {
        val w = world(); w.npcs.clear()
        val corpse = hostile(w, 11, 0, 7)
        val crate = hostile(w, 51, 100, 3)
        val alive = hostile(w, 11, 100, 3)
        val rest = faller().apply { ah = 0 }
        rest.sweepHostiles(w)
        assertEquals(100, alive.aB, "bt(): ah == 0 → return")
        faller().sweepHostiles(w)
        assertEquals(7, corpse.S, "P() true → next (@98 ifne)")
        assertEquals(100, crate.aB, "ax51 is not in {17,11,23,50}")
        assertEquals(0, alive.aB)
    }

    @Test fun `an ax78 rock in flight kills the soldier it lands on`() {
        // clip63's S2 box is an anchor point (`i.a` rejects it), so the
        // rock's W is staged by hand on a clipless entity
        val w = world(); w.npcs.clear()
        val (cx, cy) = airCell(w)
        val rock = Entity(78, null); rock.setPositionPx(cx * 20 + 10, cy * 20 + 5)
        w.npcFsm.initAx78(rock, listOf(78, 0, cx * 20 + 10, cy * 20 + 5, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), w)
        w.npcs += rock
        rock.S = 2; rock.ah = 1024
        intArrayOf(cx * 20, cy * 20 - 40, cx * 20 + 20, cy * 20 + 5).copyInto(rock.W)
        val sol = Entity(11, null).apply { aB = 100; S = 3; rock.W.copyInto(W) }
        w.npcs += sol
        w.npcFsm.tickAx78(rock, w, w.player)
        assertEquals(0, sol.aB, "S2 `bt()` sweep (ax78 bA())")
        assertEquals(0, sol.S)
    }

    // ------------------------------------------------------------ bu() S10

    @Test fun `ax15 S10 runs bt() and then the S6-S8 body`() {
        val w = world(); w.npcs.clear(); farPlayer(w)
        val e = airBlock(w, 10, z0 = -1)
        val sol = hostile(w, 11, 100, 3)
        intArrayOf(e.W[0], e.W[1], e.W[2], e.W[3]).copyInto(sol.W)
        e.ah = 256
        w.npcFsm.tickAx15(e, w, w.player)
        assertEquals(0, sol.aB, "@66 bt(): the alive soldier under the block dies")
        assertTrue(e.P and 16 != 0, "@54 P|=16")
        assertEquals(4096, e.ah,
            "@1095-1122: unsupported, s==null, !z2 → ag=0, ah=4096 — the body ran")
    }

    @Test fun `a linked S10 block settles instead of falling (z2)`() {
        val w = world(); w.npcs.clear(); farPlayer(w)
        val e = airBlock(w, 10, z0 = 77)                 // Z[0] != -1 → z2
        e.ah = 256; e.ai = 9
        w.npcFsm.tickAx15(e, w, w.player)
        assertEquals(0, e.ah, "@1125-1148: aM() || z2 → bd=true, ai=0, ah=0")
        assertEquals(0, e.ai)
        assertTrue(e.bd)
    }

    @Test fun `an S6 block over air still falls and S9 skips the body`() {
        val w = world(); w.npcs.clear(); farPlayer(w)
        val e = airBlock(w, 6, z0 = -1)
        w.npcFsm.tickAx15(e, w, w.player)
        assertEquals(4096, e.ah, "control: S6 over air falls")
        val s9 = airBlock(w, 9, z0 = -1)
        s9.ah = 0
        w.npcFsm.tickAx15(s9, w, w.player)
        assertEquals(0, s9.ah, "S9: `P|=16; goto 1707` — no body")
        assertTrue(s9.P and 16 != 0)
    }

    // ------------------------------------------------------------ b() ax21 S1

    /** An ax21 S1 director on screen with an `ad` one tick before its next frame. */
    private fun director(w: Level0World): Pair<Entity, Entity> {
        val d = Entity(21, null).apply { S = 1; aw = 9_700 }
        d.setPositionPx(w.camX + 200, w.camY + 120)
        intArrayOf(w.camX + 100, w.camY + 60, w.camX + 300, w.camY + 180).copyInto(d.Y)
        intArrayOf(w.camX + 100, w.camY + 60, w.camX + 300, w.camY + 180).copyInto(d.W)
        val ad = Entity(30, w.clips[7]).apply { aw = 9_701; setAnim(0) }
        val c = ad.clip!!
        ad.T = 0; ad.U = maxOf(1, c.frameDuration(0, 0)) - 1   // the next s() steps a frame
        d.ad = ad
        w.npcs += d
        return d to ad
    }

    @Test fun `ax21 S1 ad steps and unfreezes under a claim over a non-u9 dialog`() {
        val w = world(); w.npcs.clear(); settleIntro(w)
        val (d, ad) = director(w)
        w.kC = Entity(5, null).apply { aw = 9_702 }
        w.dlgU = 1
        ad.P = ad.P or 64
        val before = ad.T to ad.U
        w.drawStylePass()
        assertTrue(d in w.drawn, "fixture: the director is in the draw list")
        assertEquals(0, ad.P and 64, "C != null, u != 9 → ad.P &= -65 (@1948-1959)")
        assertNotEquals(before, ad.T to ad.U, "ad.s() ran (@1966)")
    }

    @Test fun `ax21 S1 ad keeps its freeze under a claim over a u9 dialog`() {
        val w = world(); w.npcs.clear(); settleIntro(w)
        val (_, ad) = director(w)
        w.kC = Entity(5, null).apply { aw = 9_703 }
        w.dlgU = 9
        ad.P = ad.P or 64
        val before = ad.T to ad.U
        w.drawStylePass()
        assertEquals(64, ad.P and 64, "C != null && u == 9 → the bit stays (@1945 → 1962)")
        assertEquals(before, ad.T to ad.U, "s() still runs but its own P&64 gate holds it")
    }

    @Test fun `ax21 S1 ad steps with no claim`() {
        val w = world(); w.npcs.clear(); settleIntro(w)
        val (_, ad) = director(w)
        w.kC = null
        ad.P = ad.P or 64
        val before = ad.T to ad.U
        w.drawStylePass()
        assertEquals(0, ad.P and 64)
        assertNotEquals(before, ad.T to ad.U)
    }
}
