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

// ---- Slice 127: i.e() cell-read overrides + i.bq crate-top level ----
class Slice127Test {

    @Test fun `e() vault states read solid cells as empty`() {
        val w = world()
        val p = w.player
        // find any solid-20 cell
        var sx = -1; var sy = -1
        outer@ for (c in 0 until w.kBp) for (r in 0 until w.kBq) {
            if (w.collisionCell(c, r) == 20) { sx = c; sy = r; break@outer }
        }
        assertTrue(sx >= 0, "level must contain a cell-20")
        p.setAnim(37)                                     // vault
        assertEquals(0, p.e(w, sx, sy),
            "S37: solid cell reads empty (i.java:14842)")
        p.setAnim(257)                                    // climb
        assertEquals(0, p.e(w, sx, sy))
        p.setAnim(0)
        assertEquals(20, p.e(w, sx, sy), "grounded: raw cell")
    }

    @Test fun `e() OOB returns 20 and cy below zero passes through`() {
        val w = world()
        val p = w.player
        assertEquals(20, p.e(w, -1, 5))
        assertEquals(20, p.e(w, w.kBp, 5))
        assertEquals(20, p.e(w, 5, w.kBq))
        // cy<0 is NOT an OOB arm in i.e() — the level read decides (20 here)
        assertEquals(w.collisionCell(5, -1), p.e(w, 5, -1))
    }

    @Test fun `e() crate-top band standing on ax51 blanks far rows`() {
        val w = world()
        val p = w.player
        val crate = Entity(51, null).apply { ak = p.ak; al = p.al }
        p.standingOn = crate
        p.refreshBoxes()
        val i3 = (p.W[3] + 1) / 20
        // rows at/around the feet band still read raw cells; far rows → 0
        for (cy in listOf(i3 - 3, i3 + 3)) {
            if (cy in 0 until w.kBq)
                assertEquals(0, p.e(w, p.ak / 20, cy),
                    "m() on crate → row $cy outside {i3-1..i3+1} reads 0")
        }
        // entBq (crate-top level) also arms m() without the entity
        p.standingOn = null
        Entity.entBq = p.al + 20
        if (i3 - 3 >= 0 && i3 - 3 < w.kBq)
            assertEquals(0, p.e(w, p.ak / 20, i3 - 3))
        Entity.entBq = 0
        if (i3 - 3 >= 0 && i3 - 3 < w.kBq)
            assertEquals(w.collisionCell(p.ak / 20, i3 - 3),
                p.e(w, p.ak / 20, i3 - 3), "entBq=0 → raw cell read")
    }

    @Test fun `entBq sets on crate landing and clears on grounded state`() {
        val w = world()
        val p = w.player
        // Move past the spawn-intro ax5 claim zone [49,863,116,941] —
        // its script targets the player uid (findByAw) and hijacks the
        // S when the UP press binds it, so the vault arm never runs.
        p.setPositionPx(300, 940); p.refreshBoxes()
        val crate = Entity(51, null).apply { ak = p.ak; al = p.al }
        p.standingOn = crate
        p.setAnim(0)
        // press → i(233) arm records crate-top level
        w.pad.e(Pad.M_UP); w.tick(emptyList()); w.pad.releaseFlush()
        assertEquals(233, p.S)
        assertEquals(p.al + 20, Entity.entBq, "g.java:805 i.bq = al+20")
        // grounded state → clear next tick
        p.setAnim(0)
        w.tick(emptyList())
        assertEquals(0, Entity.entBq, "g.java:594 c(S) → i.bq = 0")
        Entity.entBq = 0
    }
}
