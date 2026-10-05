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

class Slice145Test {
    class S145World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        override fun spawnPickup(anim: Int, x: Int, y: Int): Entity =
            Entity(14, null).also {
                it.setAnim(anim); it.ak = x; it.al = y
            }
    }

    private fun zone(w: S145World): Entity {
        val z = Entity(10, null); z.S = 24
        z.W[0] = 90; z.W[2] = 130; z.W[1] = 80; z.W[3] = 120
        w.player.ak = 110; w.player.al = 100
        w.player.W[0] = 104; w.player.W[2] = 116
        w.player.W[1] = 84; w.player.W[3] = 100
        return z
    }

    @Test fun `S24 press binds af settles and poses i267`() {
        val w = S145World(12)
        val z = zone(w); z.aA = 0
        val pad = Pad(); pad.bB = 16388
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertSame(z, w.player.af)
        assertEquals(267, w.player.S)
        assertEquals(0, w.player.ag); assertEquals(0, w.player.ah)
        assertNull(z.ae)                                     // G()
    }

    @Test fun `S24 aA1 pins marker7 at al-15`() {
        val w = S145World(12)
        val z = zone(w); z.aA = 1; z.ak = 110; z.al = 100
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        val m = z.ae ?: error("no marker")
        assertEquals(7, m.S)
        assertEquals(110, m.ak); assertEquals(85, m.al)
        val same = z.ae
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertSame(same, z.ae)                               // pin only
    }

    @Test fun `S24 ineligible drops marker link`() {
        val w = S145World(12)
        val z = zone(w); z.aA = 1
        w.player.W[0] = -100; w.player.W[2] = -50            // no overlap
        z.ae = Entity(14, null)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertNull(z.ae)
    }

    @Test fun `S24 player already bound stays ineligible`() {
        val w = S145World(12)
        val z = zone(w); z.aA = 1
        w.player.af = z                                      // af == e
        val pad = Pad(); pad.bB = 16388
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(0, w.player.S)                          // no grab
        assertNull(z.ae)
    }
}

// ============================================================ slice 146
// ax10 S30 — pursuer-pool wave spawner (La72, i.java:10427-11736):
// pv×aG grid fill, flavor by Z[6], Z6==3 infinite-wave respawn with
// aC cooldown, finite-row advance, all-dead cleanup+remove, and the
// Z[1]==-1 drain+goto-L0 refill path.
