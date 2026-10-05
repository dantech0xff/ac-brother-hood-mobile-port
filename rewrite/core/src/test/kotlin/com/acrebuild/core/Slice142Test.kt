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

class Slice142Test {
    class S142World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell) {
        override var volPaintRect: IntArray? = null      // k.aQ (Image)
        override fun findByAw(aw: Int): Entity? =
            if (aw == -1) null
            else if (player.aw == aw) player
            else npcs.firstOrNull { it.aw == aw }
    }

    private fun trig(s: Int, uid: Int = 7, pv: Int = 0): Entity {
        val z = Entity(10, null); z.S = s; z.oId = uid; z.pv = pv
        z.W[0] = 180; z.W[1] = 100; z.W[2] = 220; z.W[3] = 140
        return z
    }

    private fun overlapped(w: S142World): Entity {
        val p = w.player; p.Y[0] = 190; p.Y[1] = 110; p.Y[2] = 210; p.Y[3] = 130
        return p
    }

    // S47 nulls `k.aQ` — the ONE `static Image aQ` (k.javap.txt:2850,
    // aV() @538 `putstatic k.aQ:Image`), i.e. the eagle-view inset the
    // port keeps as `volPaintRect` (slice 371 — this asserted a write-only
    // `kAQ: Entity?` duplicate that had no reader).
    @Test fun `S47 clears the claim slot every tick L219`() {
        val w = S142World(); w.volPaintRect = intArrayOf(1, 2, 3, 4)
        NpcFsm(w).tickTrigger(trig(47), w, w.player, Pad())
        assertNull(w.volPaintRect)
    }

    // S48/S49 test the LINKED target's own Y: `i.a(r8.Y, W)` with
    // r8 = k.q(o) (aV() offsets 358-381 / 444-467, `aload_1`) — slice 364.
    private fun targetIn(t: Entity) {
        t.Y[0] = 190; t.Y[1] = 110; t.Y[2] = 210; t.Y[3] = 130
    }

    @Test fun `S48 copies pv onto the linked entity aG L166`() {
        val w = S142World()
        val boss = Entity(29, null); boss.aw = 7; targetIn(boss)
        w.npcs += boss
        NpcFsm(w).tickTrigger(trig(48, pv = 12), w, w.player, Pad())
        assertEquals(12, boss.aG)
        assertEquals(1, w.removed.size)
    }

    @Test fun `S48 requires the target's Y overlap, not the player's`() {
        val w = S142World(); overlapped(w)
        val boss = Entity(29, null); boss.aw = 7
        w.npcs += boss
        NpcFsm(w).tickTrigger(trig(48, pv = 12), w, w.player, Pad())
        assertEquals(0, boss.aG); assertTrue(w.removed.isEmpty())
    }

    @Test fun `S49 advances the linked entity to S20 L1bc`() {
        val w = S142World()
        val t = Entity(21, null); t.aw = 7; targetIn(t)
        w.npcs += t
        NpcFsm(w).tickTrigger(trig(49), w, w.player, Pad())
        assertEquals(20, t.S)
        assertEquals(1, w.removed.size)
    }

    @Test fun `S54 advances S29 target to S30 on overlap L136`() {
        val w = S142World(); overlapped(w)
        val t = Entity(11, null); t.aw = 7; t.S = 29
        w.npcs += t
        NpcFsm(w).tickTrigger(trig(54), w, w.player, Pad())
        assertEquals(30, t.S)
        assertEquals(1, w.removed.size)
    }

    @Test fun `S54 holds while the target is not at S29`() {
        val w = S142World(); overlapped(w)
        val t = Entity(11, null); t.aw = 7; t.S = 3
        w.npcs += t
        NpcFsm(w).tickTrigger(trig(54), w, w.player, Pad())
        assertEquals(3, t.S); assertTrue(w.removed.isEmpty())
    }
}

// ============================================================ slice 145
// ax10 S24 — rope/grab trigger zone (L21e, i.java:9361-9467): ax27-S0
// mechanics cloned into an ax10 zone (marker pinned at al-15 vs al-85).
