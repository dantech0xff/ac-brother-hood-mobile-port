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

/**
 * Slice 148 — the `aV()` small-zone batch (all proven):
 * S4/S5/S6/S7/S12/S13/S14/S21/S22/S23/S28/S29/S32/S41/S42/S44/S45 —
 * dialog/kAZ/checkpoint/audio/link/release/balance/meter/fail arms.
 * S9/S19/S39 are verbatim bare-return arms (documented in the port's
 * when-fallthrough; no test needed).
 */
class Slice148Test {
    /** World: adds the k.b/e.a/k.aZ/k.n/k.x/k.V/k.W seams the small
     *  arms consume (level strings, audio, checkpoint mark, latches). */
    class S148World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        private val cam = intArrayOf(0, 0, 400, 240)
        override val camRect: IntArray get() = cam
        override fun findByAw(aw: Int): Entity? =
            if (aw == -1) null
            else if (player.aw == aw) player
            else npcs.firstOrNull { it.aw == aw }
        var strings = mapOf<Int, String>()
        override fun levelString(level: Int, idx: Int): String? = strings[idx]
        override var kAj = 0
        override var kAB: String? = null
        override var kAC = 0
        var uMask = 0
        override fun padDown(mask: Int): Boolean = (uMask and mask) != 0
        val kBMarkCalls = mutableListOf<Int>()
        override fun kBMark(slot: Int, level: Int, row: Int, span: Int): Boolean {
            kBMarkCalls += row
            return row != -1
        }
        var audioTrack = -2
        override fun audioStop() { audioTrack = -1 }
        override fun audioTrackPlay(n: Int) { audioTrack = n }
        override var kAZ = false
        override var kX = 0
        override var kDlgX = 0
        override var kV = 0
        override var kW = 0
        val kNSetCalls = mutableListOf<Int>()
        override fun kNSet(n: Int) { kNSetCalls += n }
        var bh = 0
        override fun missionBh(): Int = bh
        var latchClears = 0
        override fun clearLatches() { latchClears++ }
        override var iBn = false
    }

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 6; p.W[1] = al - 16; p.W[2] = ak + 6; p.W[3] = al
        return p
    }

    private fun zone(s: Int, cfg: (Entity) -> Unit = {}): Entity {
        val z = Entity(10, null); z.S = s
        z.W[0] = 180; z.W[1] = 100; z.W[2] = 220; z.W[3] = 140
        cfg(z); return z
    }

    @Test fun `S4 overlap publishes dialog string L15b9`() {
        val w = S148World(); w.strings = mapOf(3 to "hello")
        w.kAj = 1
        val z = zone(4) { it.aF = 3 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals("hello", w.kAB); assertEquals(-1, w.kAC)
    }

    @Test fun `S4 leaving clears kAB when aC spent L15dd`() {
        val w = S148World(); w.kAB = "old"; w.kAC = 0
        val z = zone(4)
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertNull(w.kAB)
    }

    @Test fun `S5 overlap + fire edges to i22 L1625`() {
        val w = S148World(); w.uMask = 16388
        val z = zone(5)
        val p = mk(200, 120)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(22, p.S)
    }

    @Test fun `S6 overlap sets kAZ true then S7 clears it`() {
        val w = S148World()
        NpcFsm(w).tickTrigger(zone(6), w, mk(200, 120), Pad())
        assertTrue(w.kAZ)
        NpcFsm(w).tickTrigger(zone(7), w, mk(200, 120), Pad())
        assertFalse(w.kAZ)
    }

    @Test fun `S12 overlap runs kNSet + removes L175c`() {
        val w = S148World()
        val z = zone(12) { it.aF = 7 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(listOf(7), w.kNSetCalls); assertTrue(z in w.removed)
    }

    @Test fun `S13 always removes itself L1778`() {
        val w = S148World()
        val z = zone(13)
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertTrue(z in w.removed)
    }

    @Test fun `S14 ax69 Z0=1 pins player ak to target center L1704`() {
        val w = S148World()
        val t = Entity(69, null); t.Z[0] = 1
        t.W[0] = 190; t.W[2] = 210                      // center 200
        t.W[1] = 110; t.W[3] = 130                      // inside zone
        val p = mk(200, 120); p.af = t; p.ak = 0
        val z = zone(14)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(200, p.ak); assertEquals(0, p.S)
        assertNull(p.af); assertTrue(z in w.removed)
    }

    @Test fun `S14 ax69 Z0=0 hurls player L1726`() {
        val w = S148World()
        val t = Entity(69, null); t.Z[0] = 0
        t.W[0] = 190; t.W[2] = 210; t.W[1] = 110; t.W[3] = 130
        val p = mk(200, 120); p.af = t; p.av = true
        val z = zone(14)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(-3328, p.ag); assertEquals(-6656, p.ah)
        assertEquals(243, p.S); assertTrue(z in w.removed)
    }

    @Test fun `S14 ignores a wrong-ax or out-of-zone target`() {
        val w = S148World()
        val t = Entity(11, null)
        val p = mk(200, 120); p.af = t
        val z = zone(14)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(t, p.af); assertTrue(z !in w.removed)
    }

    @Test fun `S21 overlap marks checkpoint + l21 + kDlgX48 L1855`() {
        val w = S148World(); w.kAj = 2
        val z = zone(21) { it.aF = 5; it.pv = 9 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(listOf(5), w.kBMarkCalls)
        assertEquals(48, w.kDlgX, "k.x (u8 page timer), not k.X (:12851)")
        assertEquals(0, w.kX, "k.X crossfade untouched")
        assertTrue(z in w.removed)
    }

    @Test fun `S22 overlap plays or stops the audio track L157d`() {
        val w = S148World()
        NpcFsm(w).tickTrigger(zone(22) { it.aF = 6 }, w, mk(200, 120), Pad())
        assertEquals(6, w.audioTrack)
        val w2 = S148World()
        NpcFsm(w2).tickTrigger(zone(22) { it.aF = -1 }, w2, mk(200, 120), Pad())
        assertEquals(-1, w2.audioTrack)
    }

    @Test fun `S23 publishes alive flag onto linked ax11 L1889`() {
        val w = S148World()
        val t = Entity(11, null); t.aw = 42; t.aB = 100
        t.W[0] = 190; t.W[2] = 210; t.W[1] = 100; t.W[3] = 140
        w.npcs += t
        val z = zone(23) { it.oId = 42 }
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertTrue(t.cq)
        // dead target (aB<=0 → P() true) → cq = false
        t.aB = 0
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertFalse(t.cq)
    }

    @Test fun `S28 enter sets aA8 + z0, exit clears L1aa7`() {
        val w = S148World()
        val p = mk(200, 120); p.gg = null; p.aA = 0
        val z = zone(28)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(8, p.aA and 8); assertFalse(p.z); assertEquals(1, z.Z[0])
        p.W[0] = 0; p.W[2] = 5                        // leave
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, p.aA and 8); assertTrue(p.z); assertEquals(0, z.Z[0])
    }

    @Test fun `S28 held gate blocks the enter arm`() {
        val w = S148World()
        val p = mk(200, 120); p.gg = Entity(43, null)   // g.g != null
        val z = zone(28)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, p.aA and 8); assertEquals(0, z.Z[0])
    }

    @Test fun `S29 releases the group once members are done L12e4`() {
        val w = S148World()
        val t = Entity(11, null); t.aw = 7
        t.P = t.P or 32                               // flagged member
        t.setAnim(200)                                // off the at() set
        w.npcs += t
        val z = zone(29) { it.oId = 7; it.pv = -1; it.aG = -1; it.ay = -1 }
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertEquals(0, t.P and 32); assertTrue(z in w.removed)
    }

    @Test fun `S29 waits while a member is not done`() {
        val w = S148World()
        val t = Entity(11, null); t.aw = 7
        t.P = t.P or 32
        t.aB = 100                                    // alive
        t.setAnim(0)                                  // S0 → at() false
        w.npcs += t
        val z = zone(29) { it.oId = 7; it.pv = 7; it.aG = -1; it.ay = -1 }
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertEquals(32, t.P and 32); assertTrue(z !in w.removed)
    }

    @Test fun `S32 binds ac + arcs player onto the beam L18d1`() {
        val w = S148World()
        val z = zone(32)
        val p = mk(200, 120); p.ac = null
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(p.ac === z); assertEquals(38, p.S); assertEquals(1, w.latchClears)
        // center → al = zone cy (dx=0 → +10*hw/hw)
        assertEquals(((z.W[1] + z.W[3]) shr 1) + 10, p.al)
        p.W[0] = 0; p.W[2] = 5                        // leave
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertNull(p.ac)
    }

    @Test fun `S41 removes on held ax51 overlap L1a03`() {
        val w = S148World()
        val p = mk(0, 0); p.ga = Entity(51, null)
        p.ga!!.W[0] = 190; p.ga!!.W[2] = 210; p.ga!!.W[1] = 110; p.ga!!.W[3] = 130
        val z = zone(41)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(z in w.removed)
    }

    @Test fun `S42 overlap removes L1d75`() {
        val w = S148World()
        val z = zone(42)
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertTrue(z in w.removed)
    }

    @Test fun `S44 writes kW = kV - aE L1a59`() {
        val w = S148World(); w.kV = 60
        val z = zone(44) { it.aE = 15 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(45, w.kW); assertTrue(z in w.removed)
    }

    @Test fun `S45 overlap hurts when bh3 else fails L1a79`() {
        val w = S148World(); w.bh = 3
        val p = mk(200, 120)
        NpcFsm(w).tickTrigger(zone(45), w, p, Pad())
        assertEquals(34, p.S)
        val w2 = S148World(); w2.bh = 0
        val z2 = zone(45)
        NpcFsm(w2).tickTrigger(z2, w2, mk(200, 120), Pad())
        assertTrue(z2 in w2.removed)
    }
}
