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

class Slice218Test {
    /**
     * Slice-218 verdict — the "ax4 crates need p.gI==8" blocker is
     * misplaced: `gI==8` only gates the interact-scan prompt (PlayerFsm
     * L200, proven) — and bit-8 never exists on level 0 (`kF0Do` all-5s,
     * k.java:23861). Crates break via the ATTACK path — `isAttackState`
     * body overlap or the `player.X` hitbox arming S5→S6 (i.java:6733,
     * proven) — which needs no equip state. This test drives it through
     * the real tick + input path, not a direct FSM call.
     */
    @Test fun `sword tap breaks the real uid16 crate through the tick path`() {
        val w = world()
        settleIntro(w)
        val d = w.npcs.first { it.ax == 4 && it.aw == 16 }   // (1607,795) S5
        assertEquals(5, d.S)
        val p = w.player
        // stand beside the crate facing it; pin the camera there too so
        // the au-gate keeps the crate live (slice-216 gate, verbatim).
        // The crate sits against the x1400-1579 solid block (columns
        // 70-78 are '20' on every row), so its only open side is east:
        // 40px west put the player inside the block, where g.e()'s
        // grounded arm ends at `aO>12 && aR>12 → i(79)` without `l()`
        // (g.javap.txt e() 6092-6116, slice 369) — no context dispatch.
        p.setPositionPx(d.ak + 40, d.al)
        p.av = true
        w.kO = p.ak - 200; w.kP = p.al - 120; w.rebuildCamRect()
        // context tap -> 65568 -> ap() I==1 -> i(67) sword swing
        var brokenAt = -1
        repeat(120) { t ->
            val (cx, cy) = w.cellPoint(4)
            w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, cx, cy),
                          InputQueue.Event(1, InputQueue.Type.UP, cx, cy)))
            w.kO = p.ak - 200; w.kP = p.al - 120; w.rebuildCamRect()
            if (brokenAt < 0 && !w.npcs.contains(d)) brokenAt = t
        }
        assertTrue(brokenAt > 0, "uid16 crate should arm S5→S6, burst, and remove (brokenAt=$brokenAt, dS=${d.S})")
        // wisps burst on break (m=2 -> up to two m(-1) spawns, i.java:6760)
        assertTrue(w.kAp[5] >= 2, "wisp counter should pay on break (kAp[5]=${w.kAp[5]})")
    }
}
