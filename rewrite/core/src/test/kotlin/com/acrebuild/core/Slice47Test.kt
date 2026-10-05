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
// Slice 47 — ax42 `bz()` fuse/timer zone + k.F claim slot
// ===========================================================================
class Slice47Test {

    private fun ax42At(w: Level0World, x: Int, y: Int, kind: Int, uid: Int, secs: Int): Entity {
        val e = Entity(42, null)
        e.setPositionPx(x, y)
        val f = mutableListOf(42, 0, x, y, kind, 0, 0, 0, 0, 20, 20, uid, secs)
        w.npcFsm.initAx42(e, f)
        w.npcs.add(e)
        return e
    }

    @Test fun `ax42 records claim kF + P bits every tick and S gates the aJ machine`() {
        val w = world(); w.npcs.clear()
        val e = ax42At(w, 0, 0, 0, 0, 30)
        // slice 389: the ctor tail's i(r8[5]) DOES reach ax42 (clipless —
        // `aa == null` skips the anim-range test), so S = r8[5] = 0.
        assertEquals(0, e.S, "tail i(r8[5]) runs on the clipless fuse")
        e.setAnim(1)                                          // park it
        w.kAJ = 0; w.kAM = 0
        w.npcFsm.tickAx42(e, w, w.player)
        assertSame(e, w.kF, "k.F = this")
        assertTrue(e.P and 16 != 0 && e.P and 512 != 0, "P|=16|512")
        assertEquals(0, w.kAJ, "S!=0 → aJ machine skipped")
        assertEquals(0, w.kAM)
    }

    @Test fun `ax42 kind0 fires when the bound entity expires`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax42At(w, p.ak, p.al, 0, 77, 30)
        e.setAnim(0)                                          // arm: S==0
        // bound entity uid 77, dead (aB<=0 → P()==true)
        val dead = Entity(6, null); dead.aw = 77; dead.aB = 0
        w.npcs.add(dead)
        w.sfxLog.clear()
        w.npcFsm.tickAx42(e, w, p)
        assertEquals(1, w.kAJ); assertEquals(-40, w.kAK); assertEquals(30, w.kAL)
        assertNull(e.s, "s=null after L27 fire")
        assertTrue(9 in w.sfxLog, "k.A(9) on fire")
    }

    @Test fun `ax42 kind0 waits while the bound entity lives`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax42At(w, p.ak, p.al, 0, 77, 30)
        e.setAnim(0)
        val alive = Entity(6, null); alive.aw = 77; alive.aB = 10; alive.P = 32
        w.npcs.add(alive)
        w.npcFsm.tickAx42(e, w, p)
        assertEquals(0, w.kAJ, "s.P&32 set → no fire")
        assertSame(alive, e.s, "s stays bound")
    }

    @Test fun `ax42 kind1 fires on bind even while s lives`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax42At(w, p.ak, p.al, 1, 77, 45)
        e.setAnim(0)
        val alive = Entity(6, null); alive.aw = 77; alive.aB = 10; alive.P = 0
        w.npcs.add(alive)
        w.npcFsm.tickAx42(e, w, p)
        assertEquals(1, w.kAJ, "Z0==1 && s.P&32==0 → L27 fire")
        assertEquals(45, w.kAL)
    }

    @Test fun `ax42 aJ2 expiry fails the mission`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax42At(w, p.ak + 9000, p.al + 9000, 0, 77, 0)   // aL=0 → expires immediately
        e.setAnim(0)
        w.kAJ = 2; w.kAL = 0; w.kAM = 0; w.kBw = 0; w.kBx = 0
        w.npcFsm.tickAx42(e, w, p)
        assertEquals(-1, w.kBw, "bw=-1")
        assertEquals(58, w.kBx, "aj!=7 → bx=58")
        assertEquals(-1, w.kAL); assertEquals(0, w.kAM)
        assertEquals(2, w.kAJ, "this arm doesn't write aJ")
    }

    @Test fun `ax42 aJ2 overlap while ticking collects`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax42At(w, p.ak, p.al, 0, 77, 10)                // aL=10s
        e.setAnim(0)
        w.kAJ = 2; w.kAL = 10; w.kAM = 0
        p.refreshBoxes(); e.refreshBoxes()
        w.npcFsm.tickAx42(e, w, p)
        assertTrue(e in w.pendingRemove, "player∩W → k.c(this)")
        assertEquals(3, w.kAJ)
        assertEquals(0, w.kBw, "no fail write")
    }

    @Test fun `ax42 kind2 expiry binds script then aa-steps it`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // uid absent from k.eH → kSIndex = -1 → bindScript(-1) leaves
        // ca=-1 → claimActive false (the aJ==3 remove+reset tail).
        w.kEh = intArrayOf(999)
        val e = ax42At(w, p.ak + 9000, p.al, 2, 1234, 0)
        e.setAnim(0)
        w.kAJ = 2; w.kAL = 0; w.kAM = 0; w.kBw = 0
        w.npcFsm.tickAx42(e, w, p)
        assertEquals(3, w.kAJ)
        assertEquals(-1, e.ca, "h(k.s(Z[1])) → bindScript(-1)")
        w.npcFsm.tickAx42(e, w, p)
        assertTrue(e in w.pendingRemove, "!claimActive → k.c(this)")
        assertEquals(-1, w.kAL); assertEquals(0, w.kAM); assertEquals(2, w.kBw)
    }
}
