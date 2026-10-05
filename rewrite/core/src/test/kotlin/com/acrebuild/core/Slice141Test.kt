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

class Slice141Test {
    class S141World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        override fun spawnPickup(anim: Int, x: Int, y: Int): Entity =
            Entity(14, null).also {
                it.setAnim(anim); it.az = 302; it.av = false
                it.ak = x; it.al = y
            }
        override var iBB = false
        override var iBi = false
        override var iBC = false
        override var iBD = false
        override var iBE = 0
        override var iBF = -1
        override var iBG = -1
        override var kDd = false
        private val cam = intArrayOf(0, 0, 400, 240)
        override val camRect: IntArray get() = cam
        override fun findByAw(aw: Int): Entity? =
            if (aw == -1) null
            else if (player.aw == aw) player
            else npcs.firstOrNull { it.aw == aw }
    }

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 6; p.W[1] = al - 16; p.W[2] = ak + 6; p.W[3] = al
        return p
    }

    private fun climbZone(s: Int = 10, cfg: (Entity) -> Unit = {}): Entity {
        // W[3]=140 → dy = p.W[1]-140; record-style band Z[1]=50..Z[0]=180
        // (proven records carry Z[0]>Z[1] — level1 {180,50,0,90,30} —
        // smali: dy<=Z[1] → out, dy>=Z[0] → out; the window is the
        // player's top sitting 50..180 px BELOW the zone bottom, the
        // approach-from-below perch).
        val z = Entity(10, null); z.S = s
        z.W[0] = 180; z.W[1] = 100; z.W[2] = 220; z.W[3] = 140
        z.Z[0] = 180; z.Z[1] = 50; z.Z[2] = 10; z.Z[3] = 90; z.Z[4] = 7
        cfg(z); return z
    }

    @Test fun `S10 iBe suppresses the zone L318`() {
        val w = S141World(); w.iBe = true
        val z = climbZone()
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertSame(z, w.removed.last())
    }

    @Test fun `S10 in-band locks input and parks the hand L323`() {
        val w = S141World()
        val z = climbZone()
        val p = mk(200, 250)                     // W[1]=234 → dy=94 ∈ (50,180)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(w.kAm, "k.o() input lock")
        assertNotNull(p.ae, "hand indicator spawned at view center")
        assertEquals(0, p.ae!!.S, "hand card idles at S0")
        assertFalse(w.iBB, "not armed until the press")
    }

    @Test fun `S10 pad press arms the climb L3b3`() {
        val w = S141World()
        val z = climbZone()
        val p = mk(200, 250)                     // dy=94 ∈ band
        val pad = Pad(); pad.queuePress(1); pad.commit(0)
        NpcFsm(w).tickTrigger(z, w, p, pad)
        assertTrue(w.iBB, "i.bB armed")
        assertEquals(-1, w.iBF); assertEquals(-1, w.iBG)
        assertEquals(4, p.S, "player i(4) climb enter")
        assertTrue(w.iBi, "i.bi climb-active")
        assertEquals(199, p.az)
        assertNull(p.ae, "hand released after the press")
    }

    @Test fun `S10 overlap unarmed aborts with marker 71 L46b`() {
        val w = S141World()
        val z = climbZone()
        val p = mk(200, 130)
        p.W[1] = 70                              // dy=-70 < Z[0] → outside
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertNotNull(p.ae); assertEquals(71, p.ae!!.S, "marker 71")
        assertTrue(w.iBe, "i.be suppression latch")
        assertEquals(34, p.S, "player i(34)")
    }

    @Test fun `S10 overlap armed tracks grip marker 35 L42c`() {
        val w = S141World(); w.iBB = true; w.iBF = 50   // bF ∈ [Z2,Z3]=[10,90]
        val z = climbZone()
        val p = mk(200, 130); p.W[1] = 70        // outside band, overlapping
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertNotNull(p.ae); assertEquals(35, p.ae!!.S, "grip marker")
        assertEquals(p.ak, p.ae!!.ak)
        assertEquals(p.al - 85, p.ae!!.al)
    }

    @Test fun `S10 bF outside the window aborts L46b`() {
        val w = S141World(); w.iBB = true; w.iBF = 95   // > Z[3]=90
        val z = climbZone()
        val p = mk(200, 130); p.W[1] = 70        // outside band, overlapping
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(71, p.ae!!.S)
        assertTrue(w.iBe); assertEquals(34, p.S)
    }

    @Test fun `S10 off-zone climb finish resets progress L4b2`() {
        val w = S141World(); w.iBB = true; w.iBi = true; w.iBF = -1
        val z = climbZone()
        val p = mk(200, 350)                     // dy=194 ≥ Z[0], no overlap
        p.W[0] = 300; p.W[2] = 320               // outside zone W
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(28, p.S)
        assertFalse(w.iBC); assertFalse(w.iBD)
        assertEquals(100, w.iBF); assertEquals(7, w.iBE)
        assertEquals(25, w.sfxCalls.last())
    }

    @Test fun `S10 player above the zone removes it L564`() {
        val w = S141World(); w.iBB = true; w.iBi = true; w.iBF = 50
        w.kAm = true                             // still locked
        val z = climbZone()
        val p = mk(200, 100)
        p.S = 28; p.W[1] = 70                    // dy=-70 < Z[0] → climbed out
        p.W[0] = 300; p.W[2] = 320               // no overlap
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertSame(z, w.removed.last(), "k.c(this)")
        assertFalse(w.iBi)
        assertEquals(27, p.S, "S28 → exit anim 27")
        assertFalse(w.kAm, "k.p() unlock")
    }

    @Test fun `S10 below zone unlocks without removing L573`() {
        val w = S141World(); w.iBB = true; w.iBF = 95; w.kAm = true
        val z = climbZone()
        val p = mk(200, 350)
        p.W[0] = 300; p.W[2] = 320               // no overlap, dy=194 ≥ Z[0]
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(w.removed.isEmpty())
        assertFalse(w.kAm)
    }

    @Test fun `S3 clears the mask on the target L150a`() {
        val w = S141World()
        val t = Entity(11, null); t.aw = 7; t.P = t.P or 512 or 4
        w.npcs += t
        val z = climbZone(3) {
            it.Z[0] = 0; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(4, t.P and 516, "512 cleared, 4 kept")
        assertSame(z, w.removed.last())
    }

    @Test fun `S3 ax21 target also gets the re-activate bit L1520`() {
        val w = S141World()
        val t = Entity(21, null); t.aw = 7; t.P = t.P or 512
        w.npcs += t
        val z = climbZone(3) {
            it.Z[0] = 0; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(0, t.P and 512)
        assertEquals(16, t.P and 16, "director re-activate")
    }

    @Test fun `S3 Z0==1 requires overlap L14bb`() {
        val w = S141World()
        val t = Entity(11, null); t.aw = 7; t.P = t.P or 512
        w.npcs += t
        val z = climbZone(3) {
            it.Z[0] = 1; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, mk(900, 500), Pad())
        assertEquals(512, t.P and 512, "no overlap → mask stays")
        assertTrue(w.removed.isEmpty())
    }
}

// ============================================================ slice 142
// ax10 small linked-entity triggers: S47 claim-clear (L219), S48 boss
// speed (L166), S49 →S20 (L1bc), S54 S29→S30 advance (L136).
