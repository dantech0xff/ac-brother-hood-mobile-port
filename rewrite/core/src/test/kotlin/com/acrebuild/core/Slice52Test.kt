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

// ===========================================================================
// Slice 52 — ax72/ax78 counterweight pair + ax79 palette prop
// (init L384/L153/L386; ax78 `bA()` i.java:17525)
// ===========================================================================
class Slice52Test {

    private fun ax72At(w: Level0World, x: Int, y: Int, s: Int,
                       az: Int = 0, partnerAw: Int = -1,
                       drop: Int = 0): Entity {
        val e = Entity(72, w.clips[51])
        e.setPositionPx(x, y)
        val f = mutableListOf(72, 0, x, y, 0, s, 0, az, 10, drop, 20, 30, partnerAw)
        while (f.size < 16) f += 0
        w.npcFsm.initAx72(e, f, w)
        w.npcs.add(e)
        return e
    }

    private fun ax78At(w: Level0World, x: Int, y: Int, s: Int,
                       az: Int = 0): Entity {
        val e = Entity(78, w.clips[63])
        e.setPositionPx(x, y)
        val f = mutableListOf(78, 0, x, y, 0, s, 0, az)
        while (f.size < 16) f += 0
        w.npcFsm.initAx78(e, f, w)
        w.npcs.add(e)
        return e
    }

    private fun ax79At(w: Level0World, x: Int, y: Int, s: Int,
                       palette: Int = 0): Entity {
        val e = Entity(79, w.clips[0])
        e.setPositionPx(x, y)
        val f = mutableListOf(79, 0, x, y, 0, s, 0, palette, 77)
        while (f.size < 16) f += 0
        w.npcFsm.initAx79(e, f, w)
        w.npcs.add(e)
        return e
    }

    /** first support cell (h(): v<12 ? v>=5 : true) in the fixture grid */
    private fun supportCell(w: Level0World): Pair<Int, Int> {
        for (cy in 1 until w.level.worldH / 20)
            for (cx in 1 until w.level.worldW / 20)
                if (w.collisionCell(cx, cy) >= 5) return cx to cy
        error("no support cell in fixture")
    }

    /** first non-support cell (v<5) with a support cell directly below it */
    private fun airAboveSupport(w: Level0World): Pair<Int, Int> {
        for (cy in 1 until w.level.worldH / 20 - 1)
            for (cx in 1 until w.level.worldW / 20)
                if (w.collisionCell(cx, cy) < 5 && w.collisionCell(cx, cy + 1) >= 5)
                    return cx to cy
        error("no air-above-support column in fixture")
    }

    @Test fun `ax72 init stores the partner link and fixed drop offset`() {
        val w = world(); w.npcs.clear()
        val e = ax72At(w, 2897, 445, 0, az = 3, partnerAw = 34, drop = 7)
        assertSame(w.clips[51], e.clip, "bi[72]=51")
        assertEquals(3, e.az, "L384 az=r8[7]")
        assertEquals(10, e.Z[0]); assertEquals(7 shl 8, e.Z[1], "Z[1]=r8[9]<<8")
        assertEquals(20, e.Z[2]); assertEquals(30, e.Z[3])
        assertEquals(34, e.Z[4], "Z[4]=r8[12] — linked ax78's aw")
        assertEquals(0, e.S)
    }

    @Test fun `ax78 init then S0 settles +2 until supported then arms`() {
        val w = world(); w.npcs.clear()
        val (cx, cy) = supportCell(w)
        // probe (al+10)/20 hits cy: place the weight 10px above the support row
        val e = ax78At(w, cx * 20, cy * 20 - 10, 0, az = 2)
        assertSame(w.clips[63], e.clip, "bi[78]=63")
        assertEquals(2, e.az)
        w.npcFsm.tickAx78(e, w, w.player)
        assertEquals(1, e.S, "L5/L8 supported → i(1)")
        // drop back to S0 on air: +2 settle each tick, stays S0
        val (ax, ay) = airAboveSupport(w)
        val e2 = ax78At(w, ax * 20, ay * 20 - 10, 0)
        val before = e2.al
        w.npcFsm.tickAx78(e2, w, w.player)
        assertEquals(0, e2.S, "unsupported → keep settling")
        assertEquals(before + 2, e2.al, "L8 al+=2")
    }

    @Test fun `ax78 S1 holds on support, drops to S2 when unsupported`() {
        val w = world(); w.npcs.clear()
        val (cx, cy) = supportCell(w)
        val e = ax78At(w, cx * 20, cy * 20 - 10, 1)
        w.npcFsm.tickAx78(e, w, w.player)
        assertEquals(1, e.S, "L11 supported → hold")
        val (ax, ay) = airAboveSupport(w)
        val e2 = ax78At(w, ax * 20, ay * 20 - 10, 1)
        w.npcFsm.tickAx78(e2, w, w.player)
        assertEquals(2, e2.S, "unsupported → i(2)")
        assertEquals(1536, e2.aj, "fall gravity aj=1536")
    }

    @Test fun `ax78 S2 whooshes at T4U0 then lands into S3 zeroed`() {
        val w = world(); w.npcs.clear()
        val (cx, cy) = supportCell(w)
        val e = ax78At(w, cx * 20, cy * 20 - 10, 2)
        e.T = 4; e.U = 0; e.ah = 2048; e.aj = 0
        w.sfxLog.clear()
        w.npcFsm.tickAx78(e, w, w.player)
        assertTrue(14 in w.sfxLog, "T==4&&U==0 → k.A(14) whoosh")
        assertEquals(3, e.S, "supported → i(3)")
        assertEquals(0, e.aj); assertEquals(0, e.ah, "landed velocity zeroed")
    }

    @Test fun `ax78 S2 keeps falling on air and S3 sweeps + re-drops`() {
        val w = world(); w.npcs.clear()
        val (ax, ay) = airAboveSupport(w)
        val e = ax78At(w, ax * 20, ay * 20 - 10, 2)
        e.T = 0; e.U = 0
        w.npcFsm.tickAx78(e, w, w.player)
        assertEquals(2, e.S, "still falling")
        assertEquals(1536, e.aj)
        // S3 on support: settle path keeps S3 and zeroes vel; on air aj=1536
        val (cx, cy) = supportCell(w)
        val e3 = ax78At(w, cx * 20, cy * 20 - 10, 3)
        e3.ah = 1024
        w.npcFsm.tickAx78(e3, w, w.player)
        assertEquals(3, e3.S); assertEquals(0, e3.ah); assertEquals(0, e3.aj)
        val e4 = ax78At(w, ax * 20, ay * 20 - 10, 3)
        w.npcFsm.tickAx78(e4, w, w.player)
        assertEquals(1536, e4.aj, "L28 air → gravity again")
    }

    @Test fun `ax79 init pins the palette slot`() {
        val w = world(); w.npcs.clear()
        val e = ax79At(w, 6456, 510, 79, palette = 2)
        assertSame(w.clips[0], e.clip, "bi[79]=0")
        assertEquals(2, e.palette, "L386 e.palette = Z[0] = r8[7]")
        assertEquals(2, e.Z[0]); assertEquals(77, e.Z[1])
        assertEquals(79, e.S, "S = r8[5] verbatim (record legitimately uses 79)")
    }

    @Test fun `level0 counterweight records link by aw`() {
        val w = world()
        val lever = w.npcs.find { it.ax == 72 && it.aw == 33 }
        val weight = w.findByAw(34)
        assertNotNull(lever); assertNotNull(weight)
        assertEquals(34, lever!!.Z[4], "ax72 uid33 → ax78 uid34")
        assertEquals(78, weight!!.ax)
    }
}
