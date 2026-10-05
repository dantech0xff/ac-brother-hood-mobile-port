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

class Slice213Test {

    /**
     * ax22 capture zones spawn with `b=1` per the `I()` preamble
     * (i.java:15250) and rebuild W from the clip-14 rect via the L1f35
     * shared tail (`if (b) t()`, i.java:18904) — never from record fields.
     * Before this fix `W` stayed `[0,0,0,0]` and `aN()`'s overlap arm
     * could never fire: the (1214,636)->(1316,568) hopscotch was dead.
     */

    private fun ax22s(w: Level0World) = w.npcs.filter { it.ax == 22 }

    @Test fun `ax22 record init fills Z from Le87 not the generic tail`() {
        val w = world()
        val e = ax22s(w)
        assertTrue(e.isNotEmpty(), "level-0 carries ax22 capture zones")
        for (z in e) {
            assertEquals(1, z.az, "Le87: az = 1")
            assertTrue(z.P and 512 != 0, "Le87: P|512")
            // Le87: Z = {0, r8[4], r8[7], r8[11]} — Z[0] always 0.
            assertEquals(0, z.Z[0], "Z[0] = 0 verbatim")
        }
    }

    @Test fun `ax22 W rebuilds from the clip rect on the shared tail`() {
        val w = world()
        var t = 0
        while (w.player.ah != 0 && t++ < 600) w.tick(emptyList())
        w.tick(emptyList())
        val zones = ax22s(w).filter { it.S == 0 }
        assertTrue(zones.isNotEmpty())
        zones.forEach(::keepLive)
        w.tick(emptyList())          // one I() so the shared tail rebuilds W
        for (z in zones) {
            // clip14 frame rect = [-6,-10,34,33]; `t()` folds the frame's
            // per-frame anchor (dx sign flips on av) so pin the dims and
            // an anchor band — the regression was the all-zero rect.
            assertEquals(34, z.W[2] - z.W[0], "clip14 rect w=34 (uid ${z.aw})")
            assertEquals(33, z.W[3] - z.W[1], "clip14 rect h=33 (uid ${z.aw})")
            assertTrue(z.W[0] in (z.ak - 40)..(z.ak + 40) &&
                z.W[1] in (z.al - 40)..(z.al + 40),
                "W sits at the anchor (uid ${z.aw}: W=${z.W.toList()})")
        }
    }

    @Test fun `real ax22 record captures the player end to end`() {
        val w = world()
        var t = 0
        while (w.player.ah != 0 && t++ < 600) w.tick(emptyList())
        // hopscotch anchor (1214,636) — the wall-face capture zone.
        val e = ax22s(w).firstOrNull { it.ak == 1214 && it.al == 636 }
            ?: error("no ax22 record at (1214,636)")
        keepLive(e)
        val p = w.player
        // a free-anim standing state inside the zone's clip rect
        p.setPositionPx(e.ak, e.al)
        p.S = 0; p.ah = 0; p.ag = 0; p.refreshBoxes()   // a finished frame's t()
        var captured = false
        repeat(40) {
            w.tick(emptyList())
            if (p.S == 65) { captured = true; return@repeat }
        }
        assertTrue(captured, "overlap -> snap + i(65); p.S=${p.S} e.S=${e.S}")
        assertEquals(e.ak, p.ak); assertEquals(e.al, p.al)
        assertEquals(1, e.S, "zone -> S1")
    }

    @Test fun `ax22 vault exit fires through the real tick path`() {
        val w = world()
        var t = 0
        while (w.player.ah != 0 && t++ < 600) w.tick(emptyList())
        val e = ax22s(w).firstOrNull { it.ak == 1214 && it.al == 636 }
            ?: error("no ax22 record at (1214,636)")
        keepLive(e)
        val p = w.player
        p.setPositionPx(e.ak, e.al); p.S = 0; p.ah = 0; p.ag = 0
        p.refreshBoxes()                               // a finished frame's t()
        var t2 = 0
        while (p.S != 65 && t2++ < 40) w.tick(emptyList())
        assertEquals(65, p.S)
        // Z[2] = r8[7] = 1 on this record -> the 16396 east-vault edge.
        // (An upstream entity's `k.v()` clears the pad before the zone's
        // own tick sees real-input edges, so drive the arm directly like
        // the slice-39 arm tests.)
        w.pad.commit(16396)
        w.npcFsm.tickZoneInteract(e, w, p)
        assertEquals(19, p.S, "vault edge -> i(19); S=${p.S}")
        assertEquals(3328, p.ag); assertEquals(-3840, p.ah)
        assertEquals(0, e.S, "zone reset -> S0")
    }
}
