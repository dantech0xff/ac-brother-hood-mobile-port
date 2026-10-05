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

// ------------------------------------------------------------------
// Slice 93 — k.b(z2) draw pass: bd[] sorted insert + visibility arms
// ------------------------------------------------------------------
class Slice93Test {
    private fun ent(w: Level0World, ax: Int, az: Int = 0,
                    al: Int = 0, x: Int = 100): Entity {
        val e = Entity(ax, null)
        e.az = az
        // ak/al = world pos; `al` is the y view-offset AND the `d()`
        // tie-break (same field verbatim). x is the x view-offset.
        val px = w.camX + x; val py = w.camY + al
        e.ak = px; e.al = py
        e.Y[0] = px; e.Y[1] = py; e.Y[2] = px + 20; e.Y[3] = py + 20
        e.W[0] = px; e.W[1] = py; e.W[2] = px + 20; e.W[3] = py + 20
        return e
    }

    @Test fun `bd sorts az descending then al ascending`() {
        val w = world()
        w.npcs.clear()
        val e1 = ent(w, 23, az = 5, al = 200)
        val e2 = ent(w, 23, az = 9, al = 100)
        val e3 = ent(w, 23, az = 5, al = 50)
        w.npcs += e1; w.npcs += e2; w.npcs += e3
        w.buildDrawList()
        // az ASCENDING: az-5 pair first (al 50 then 200), az-9 last —
        // small az draws first/behind (k.java:2494)
        val ids = (0 until w.drawCount).map { w.drawList[it]!! }
        val order = ids.filter { it !== w.player }
        assertEquals(listOf(e3, e1, e2), order)
    }

    @Test fun `P128 entities are skipped except ax10 and ax51`() {
        val w = world()
        w.npcs.clear()
        val hidden = ent(w, 23, al = 100); hidden.P = 128
        val h10 = ent(w, 10, al = 100); h10.P = 128
        val h51 = ent(w, 51, al = 100); h51.P = 128
        w.npcs += hidden; w.npcs += h10; w.npcs += h51
        w.buildDrawList()
        val ids = (0 until w.drawCount).map { w.drawList[it]!! }
        assertFalse(hidden in ids)
        assertTrue(h10 in ids)
        assertTrue(h51 in ids)
    }

    @Test fun `off-camera entity excluded by v-au-gt-i`() {
        val w = world()
        w.npcs.clear()
        val near = ent(w, 23, al = 100)
        val far = ent(w, 23, al = 20000)
        w.npcs += near; w.npcs += far
        w.buildDrawList()
        val ids = (0 until w.drawCount).map { w.drawList[it]!! }
        assertTrue(near in ids)
        assertFalse(far in ids)                       // au > i → not drawn
    }

    @Test fun `ax14 S38 forced az 301 in draw pass`() {
        val w = world()
        w.npcs.clear()
        // ax14's v() tail tests `a(aS.W, W)` — the entity rect must
        // overlap the PLAYER's, so park it on the player.
        val e = ent(w, 14, az = 0, al = w.player.al - w.camY)
        e.ak = w.player.ak
        e.W[0] = w.player.W[0]; e.W[1] = w.player.W[1]
        e.W[2] = w.player.W[2]; e.W[3] = w.player.W[3]
        e.S = 38
        w.npcs += e
        w.buildDrawList()
        assertTrue(w.drawList.take(w.drawCount).contains(e))
        assertEquals(301, e.az)                        // k.java:2871
    }

    @Test fun `player appended through the same d() path`() {
        val w = world()
        w.buildDrawList()
        assertTrue(w.drawCount >= 1)
        val ids = (0 until w.drawCount).map { w.drawList[it]!! }
        assertTrue(w.player in ids)
    }

    @Test fun `P16 arms draw off-play entities`() {
        val w = world()
        w.npcs.clear()
        val e15 = ent(w, 15, al = 20000); e15.S = 9; e15.P = 16
        val e9 = ent(w, 9, al = 20000); e9.S = 5; e9.P = 16
        val e14 = ent(w, 14, al = 20000); e14.S = 74; e14.P = 16
        val e66 = ent(w, 66, al = 20000); e66.P = 16
        val eSkip = ent(w, 15, al = 20000); eSkip.S = 3; eSkip.P = 16
        w.npcs += e15; w.npcs += e9; w.npcs += e14; w.npcs += e66; w.npcs += eSkip
        w.buildDrawList()
        val ids = (0 until w.drawCount).map { w.drawList[it]!! }
        assertTrue(e15 in ids); assertTrue(e9 in ids)
        assertTrue(e14 in ids); assertTrue(e66 in ids)
        assertFalse(eSkip in ids)                     // ax15 S3 not in {9,10}
    }
}
