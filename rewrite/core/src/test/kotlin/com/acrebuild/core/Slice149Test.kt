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

/** Slice 149 — the last two `aV()` arms: S8 (L152b alert-toggle) and
 *  S18 (L1c98 ax4 offscreen spawn-release). All proven. */
class Slice149Test {
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

    @Test fun `S8 overlap toggles iBn and animates L152b`() {
        val w = Slice148Test.S148World(cell = 12)     // standable: E() stays put
        val p = mk(200, 120)
        val z = zone(8)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(w.iBn); assertTrue(z in w.removed)
        assertEquals(79, p.S); assertFalse(p.z)         // i(79), g.z=0
        // second trigger flips back: i(80), g.z=1, latches cleared
        // eSettle may have walked `al` — re-pin the rect for call 2
        p.W[0] = p.ak - 6; p.W[1] = 100; p.W[2] = p.ak + 6; p.W[3] = 140
        val z2 = zone(8)
        NpcFsm(w).tickTrigger(z2, w, p, Pad())
        assertFalse(w.iBn)
        assertEquals(80, p.S); assertTrue(p.z)
        assertTrue(w.latchClears > 0)
    }

    @Test fun `S8 ignores non-overlap`() {
        val w = Slice148Test.S148World()
        val z = zone(8)
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertFalse(w.iBn); assertTrue(z !in w.removed)
    }

    @Test fun `S18 releases ax4 to the right offscreen edge L1c98`() {
        val w = Slice148Test.S148World()
        val t = Entity(4, null); t.aw = 9; t.setAnim(33); t.pv = 300
        t.W[0] = 0; t.W[2] = 20                         // width 20
        t.ak = 250                                      // right of player
        t.P = t.P or 32 or 128
        w.npcs += t
        val z = zone(18) { it.oId = 9 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(16, t.P and 16)
        assertEquals(0, t.P and 32); assertEquals(0, t.P and 128)
        assertEquals(-300, t.ag); assertTrue(t.av)      // ag=-p, av=1
        assertEquals(w.camRect[2] + 20, t.ak)           // camR + width
        assertEquals(w.camRect[1] + 70, t.al)           // camT + 70
        assertEquals(1, t.aA)
    }

    @Test fun `S18 releases ax4 to the left offscreen edge L1d3f`() {
        val w = Slice148Test.S148World()
        val t = Entity(4, null); t.aw = 9; t.setAnim(33); t.pv = 300
        t.W[0] = 0; t.W[2] = 20
        t.ak = 150                                      // left of player
        w.npcs += t
        val z = zone(18) { it.oId = 9 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(300, t.ag); assertFalse(t.av)      // ag=p, av=0
        assertEquals(w.camRect[0] - 20, t.ak)           // camL - width
        assertEquals(1, t.aA)
    }

    @Test fun `S18 gates on ax4 S33 aA0-or-2`() {
        val w = Slice148Test.S148World()
        val t = Entity(4, null); t.aw = 9; t.setAnim(33); t.aA = 1
        w.npcs += t
        val z = zone(18) { it.oId = 9 }
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(1, t.aA)                           // untouched
        t.aA = 2                                        // aA==2 passes
        NpcFsm(w).tickTrigger(z, w, mk(200, 120), Pad())
        assertEquals(1, t.aA)
    }
}
