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

/** Slice 164 — claim-field correctness: `claimAb` reads `i.cK`
 *  (`scriptStep`), `claimKC` deleted in favor of the full `N()`
 *  (`bindContext`), `cellForLos` routes `e()` for the ax0 overrides. */
class Slice164Test {

    @Test fun `claimAb follows scriptStep release not the lunge snapshot`() {
        val w = world()
        val e = Entity(5, null).apply {
            ca = 0; cd[0] = false
            scriptStep = 4          // ab() armed
            cK = 77                 // lunge X[1] snapshot — unrelated field
        }
        assertTrue(e.claimAb(), "armed claim should read active")
        // bI() normal path writes cK=-2 (scriptStep); the lunge cK is
        // untouched — before the fix claimAb() stayed true here.
        e.releaseClaim(w)
        assertEquals(-2, e.scriptStep)
        assertFalse(e.claimAb(), "claim must not read the lunge snapshot cK")
    }

    @Test fun `bindContext evicts the previous claimer like N()`() {
        val w = world()
        val old = Entity(5, null).apply {
            ca = 0; cd[0] = false; scriptStep = 3; aw = 111
        }
        val new = Entity(5, null).apply { aw = 222; aG = -1 }
        w.npcs += old; w.npcs += new
        w.kC = old
        new.bindContext(w)
        assertSame(new, w.kC)
        assertTrue(old in w.pendingRemove, "k.c(old) — evicted claimer removed")
        assertEquals(-2, old.scriptStep, "old claimer released via bI()")
        assertTrue(new.P and 16 != 0)
    }

    @Test fun `e cell read honors ax0 S37 override only for the player`() {
        val w = world()
        // find an in-bounds cell that reads solid (20)
        var cx = -1; var cy = -1
        outer@ for (y in 0 until w.kBq) for (x in 0 until w.kBp) {
            if (w.collisionCell(x, y) == 20) { cx = x; cy = y; break@outer }
        }
        assertTrue(cx >= 0, "level 0 must contain a cell-20 block")
        val p = w.player                      // ax == 0
        p.S = 37
        assertEquals(0, p.e(w, cx, cy), "S37 reads cell-20 as open")
        p.S = 0
        assertEquals(20, p.e(w, cx, cy), "grounded read is raw")
        val npc = Entity(11, null)
        assertEquals(20, npc.e(w, cx, cy), "ax!=0 never gets the override")
    }

    @Test fun `losBlocked routes through e so a player walker sees through S37 cells`() {
        val w = world()
        // find a cell-20 block with OPEN horizontal neighbors so the
        // Bresenham walk starts in air and crosses the solid cell
        var cx = -1; var cy = -1
        outer@ for (y in 0 until w.kBq) for (x in 1 until w.kBp - 1) {
            if (w.collisionCell(x, y) == 20 &&
                w.collisionCell(x - 1, y) < 12 &&
                w.collisionCell(x + 1, y) < 12) { cx = x; cy = y; break@outer }
        }
        assertTrue(cx > 0, "level 0 must contain an isolated cell-20 block")
        val p = w.player                     // ax == 0 — center in cell cx-1
        p.W[0] = (cx - 1) * 20 + 6; p.W[1] = cy * 20 + 6
        p.W[2] = (cx - 1) * 20 + 14; p.W[3] = cy * 20 + 14
        val t = Entity(0, null).apply {      // target center in cell cx+1
            W[0] = (cx + 1) * 20 + 6; W[1] = cy * 20 + 6
            W[2] = (cx + 1) * 20 + 14; W[3] = cy * 20 + 14
        }
        p.S = 37
        assertFalse(p.losBlocked(t, w), "player S37 sees through cell-20")
        p.S = 0
        assertTrue(p.losBlocked(t, w), "grounded player is blocked")
        val npc = Entity(11, null).apply {
            W[0] = p.W[0]; W[1] = p.W[1]; W[2] = p.W[2]; W[3] = p.W[3]
        }
        assertTrue(npc.losBlocked(t, w), "npc LOS still blocked")
    }
}
