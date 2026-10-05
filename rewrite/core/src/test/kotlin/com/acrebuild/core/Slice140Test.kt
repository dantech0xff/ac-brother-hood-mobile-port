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

class Slice140Test {
    class S140World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        override var camAf = 0
        override var camAg = 0
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

    private fun zone(s: Int, cfg: (Entity) -> Unit = {}): Entity {
        val z = Entity(10, null); z.S = s
        z.ak = 200; z.al = 120
        z.W[0] = 180; z.W[1] = 100; z.W[2] = 220; z.W[3] = 140
        cfg(z); return z
    }

    @Test fun `S0 overlap publishes focus offsets L5c7`() {
        val w = S140World()
        val z = zone(0) { it.pv = 10; it.aG = 20 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(10, w.camAf); assertEquals(20, w.camAg)
    }

    @Test fun `S0 leave clears both offsets L5e0`() {
        val w = S140World()
        val z = zone(0) { it.pv = 10; it.aG = 20 }
        val p = mk(200, 120)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        p.ak = 2000; p.W[0] = 1994; p.W[2] = 2006
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, w.camAf); assertEquals(0, w.camAg)
    }

    @Test fun `S0 zero params keep prior offsets L5d1`() {
        val w = S140World(); w.camAf = 55; w.camAg = 99
        val z = zone(0) { it.pv = 0; it.aG = 0 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(55, w.camAf, "p==0 → af write skipped")
        assertEquals(99, w.camAg, "aG==0 → L1ec7 bare return")
    }

    @Test fun `S0 offscreen zone clears despite overlap`() {
        val w = S140World()
        val z = zone(0) { it.pv = 10; it.aG = 20 }
        z.W[0] = 900; z.W[2] = 940                          // outside camRect
        w.camAf = 7; w.camAg = 8
        // player.W still overlaps the moved zone
        val p = mk(920, 120)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, w.camAf); assertEquals(0, w.camAg)
    }

    @Test fun `S2 Z0==1 no overlap holds fire L13b5`() {
        val w = S140World()
        val t = Entity(11, null); t.aw = 7; w.npcs += t
        val z = zone(2) {
            it.Z[0] = 1; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, mk(900, 500), Pad())
        assertEquals(0, t.P and 512, "no overlap → no mask")
        assertTrue(w.removed.isEmpty(), "zone survives")
    }

    @Test fun `S2 Z0==1 overlap applies mask and removes L13f5`() {
        val w = S140World()
        val t = Entity(11, null); t.aw = 7; w.npcs += t
        val z = zone(2) {
            it.Z[0] = 1; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 513
        }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(513, t.P and 513)
        assertTrue(t.av, "bit0 → av=true")
        assertSame(z, w.removed.last(), "one-shot k.c(this)")
    }

    @Test fun `S2 Z0==2 onscreen guard holds the gate L13c5`() {
        val w = S140World()
        val guard = Entity(11, null); guard.aw = 9
        guard.ak = 210; guard.al = 120
        guard.Y[0] = 200; guard.Y[1] = 100; guard.Y[2] = 220; guard.Y[3] = 140
        val t = Entity(11, null); t.aw = 7
        w.npcs += guard; w.npcs += t
        val z = zone(2) {
            it.Z[0] = 2; it.Z[1] = 7; it.Z[2] = 9; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(0, t.P and 512, "guard visible → blocked")
        assertTrue(w.removed.isEmpty())
    }

    @Test fun `S2 Z0==2 absent guard lets it fire`() {
        val w = S140World()
        val t = Entity(11, null); t.aw = 7; w.npcs += t
        val z = zone(2) {
            it.Z[0] = 2; it.Z[1] = 7; it.Z[2] = 9; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(512, t.P and 512)
        assertSame(z, w.removed.last())
    }

    @Test fun `S2 target player propagates bit9 to ga L1418`() {
        val w = S140World()
        w.player.aw = 3
        val held = Entity(0, null); w.player.ga = held
        val z = zone(2) {
            it.Z[0] = 0; it.Z[1] = 3; it.Z[2] = 0; it.Z[3] = 512
        }
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(512, w.player.P and 512)
        assertEquals(512, held.P and 512, "g.a gets the 512 mask")
        assertSame(z, w.removed.last())
    }

    @Test fun `S2 squad sweep arms soldiers by link L1451`() {
        val w = S140World()
        val t = Entity(11, null); t.aw = 7; w.npcs += t
        val armed = Entity(11, null)
        val link = Entity(0, null); link.P = link.P or 512
        armed.s = link
        val plain = Entity(11, null)                      // no s link
        w.npcs += armed; w.npcs += plain
        val z = zone(2) {
            it.Z[0] = 0; it.Z[1] = 7; it.Z[2] = 0; it.Z[3] = 4
        }
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(512, armed.P and 512, "s.P&512 → soldier armed")
        assertEquals(0, plain.P and 512, "no link → skipped")
    }

    @Test fun `initTrigger S2 record fills the gate Z quad L59`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 2, 2, 0, 0, 0, 0, 0, 0, 77, 88, 513)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(2, e.S)
        assertEquals(2, e.Z[0]); assertEquals(77, e.Z[1])
        assertEquals(88, e.Z[2]); assertEquals(513, e.Z[3])
        assertEquals(0, e.aE, "L59 arm does not run L111")
    }

    @Test fun `initTrigger S10 record fills the five-slot Z L95`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 10, 10, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(10, e.S)
        assertEquals(listOf(1, 2, 3, 4, 5), e.Z.take(5))
        assertEquals(0, e.pv, "L95 arm does not run L111")
    }

    // slice 143 — remaining per-S init arms (i.java:2173-2229); each
    // replaces the `default → L111` field map for its S value.

    @Test fun `initTrigger S31 record fills the QTE Z quintet i2205`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 7, 31, 0, 0, 0, 0, 0, 0x1234, 0, 3, 9, 42)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(31, e.S)
        assertEquals(7, e.Z[0]); assertEquals(0x1234, e.Z[1])
        assertEquals(3, e.Z[2]); assertEquals(9, e.Z[3]); assertEquals(42, e.Z[4])
        assertEquals(0, e.aE, "S31 arm does not run L111")
    }

    @Test fun `initTrigger S30 record fills the eight-slot Z i2194`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(30, e.S)
        assertEquals(listOf(1, 2, 3, 4, 5, 6, 7, 8), e.Z.take(8),
            "Z = f12..f19")
    }

    @Test fun `initTrigger S24 record binds aA from f11 i2187`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 0, 24, 0, 0, 0, 0, 0, 66)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(66, e.aA, "aA = f11")
        assertEquals(0, e.aE, "S24 arm does not run L111")
    }

    @Test fun `initTrigger S11 record Z0 from f0 i2173`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 0, 11)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(10, e.Z[0], "Z[0] = f0")
        assertEquals(0, e.aE, "S11 arm does not run L111")
    }

    @Test fun `initTrigger S28 skips L111 field map i2190`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 55, 28, 0, 0, 0, 0, 0, 77)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(0, e.Z[0]); assertEquals(0, e.aE); assertEquals(0, e.pv)
    }

    @Test fun `initTrigger S39 arms P16 unless P32 set i2219`() {
        val w = S140World()
        val e = Entity(10, null)
        val f = listOf(10, 0, 0, 0, 0, 39)
        NpcFsm(w).initTrigger(e, f)
        assertEquals(16, e.P and 16, "P&32==0 → P|=16")
        val e2 = Entity(10, null); e2.P = 32
        NpcFsm(w).initTrigger(e2, f)
        assertEquals(0, e2.P and 16, "P&32!=0 → unchanged")
    }
}

// ============================================================ slice 141
// ax10 S3 flag-clear trigger (L14a7-L1526 — S2's mirror) + the real S10
// arm (L318-L5a8, scripted wall-climb sequence); the wall-run latch arm
// previously ported as "S10" is case 46 (L1e1) — relabeled.
