package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 401 — the ax11 `I()` head default flags (i.javap.txt `I()` @2131-2146,
 * raw bytes `aload_2; getstatic aS; invokevirtual g:(Li;)Z; ifne 2147; iconst_1;
 * istore 6; iconst_1; istore 5`): `r6 = r5 = !r2.g(k.aS)`.
 *
 * `g(i o)` = "o is on my facing side" with the SOLDIER as the receiver, so the
 * intake gate (`r5` → `j()`) and the stealth-kill gate (`r6` → `k()`) default ON
 * only while the soldier does NOT face the player. The states that leave the
 * flags alone (S85 flinch, S9, S25 fall, …) therefore take no hit from the
 * front — one hit per flinch cycle, no combo juggling. The port tested
 * `g.g()` (player alive), so every default state stayed hittable from the front.
 * The arms that set the flags explicitly (S2/3/92 patrol, S4/22 chase, S5, S11,
 * S12, S18, S23 …) are unchanged.
 */
class Slice401Test {
    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..8).all { w.collisionCell(cx + it, cy) == 20 &&
                    (1..4).all { r -> w.collisionCell(cx + it, cy - r) < 12 } })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    private fun hitOn(s: Int, looksAway: Boolean): Int {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = w.npcs.first { it.ax == 11 }
        e.setPositionPx(gx, gy)
        e.Z[0] = 0; e.cq = false; e.aB = 300; e.aA = 1
        e.setAnim(s); e.refreshBoxes()
        val p = w.player
        // the S67 swing has its real attack box on T1 (T0/T2-T4 are anchor
        // points — slice 364); the tail refreshes the player's boxes before j()
        p.setPositionPx(e.ak + 5, e.al); p.av = true
        p.setAnim(67); p.T = 1; p.gI = 1
        p.refreshBoxes()
        e.refreshBoxes()
        assertTrue(Entity.overlapStrict(p.X, e.W), "fixture: the swing reaches the soldier")
        e.av = looksAway                          // the player stands on its right
        w.npcFsm.tick(e, p)
        return e.aB
    }

    @Test fun `S85 - a flinching soldier that faces the player takes no hit, one looking away does`() {
        assertEquals(300, hitOn(85, looksAway = false), "r5 stays down: the intake is skipped")
        assertTrue(hitOn(85, looksAway = true) < 300, "r5 = r6 = !g(aS): hit from behind lands")
    }

    @Test fun `S9 - the stagger (no arm) follows the same default`() {
        assertEquals(300, hitOn(9, looksAway = false))
        assertTrue(hitOn(9, looksAway = true) < 300)
    }

    @Test fun `states that set the flag explicitly still take the hit from the front`() {
        for (s in intArrayOf(11, 12, 23, 4)) {
            assertTrue(hitOn(s, looksAway = false) < 300, "S$s sets r14 itself")
        }
    }
}
