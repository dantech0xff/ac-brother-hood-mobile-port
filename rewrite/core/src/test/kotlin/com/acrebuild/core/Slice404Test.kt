package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Slice 404 — the soldier is solid to the player whether or not it is alerted.
 *
 * The shared tail of `I()` (i.javap `I()` @7545-7660, raw bytes):
 * ```
 * 7545: if (aA != 0) goto 7594
 *       P &= ~16
 * 7563: if (aS.aA <= 2) goto 7644            // no l()/a(32) below that
 *       if (l()) aS.a(32,0,0,this)
 *       goto 7644
 * 7594: P |= 16
 *       if ((aS.aA & 1) != 0 && aS.g(this) && j != 0) aS.a(32,0,0,this)
 * 7644: if ((aS.aA & 8) == 0) a()           // BOTH branches join here
 * ```
 * The port ran `a()` only in the `aA == 0 && aS.aA <= 2` corner, so an alerted
 * (attacking) soldier — and any soldier while the player's `aA` was above 2 — was
 * a ghost the player walked through. `a()` (`i.a()` @914, `pushContact`) shoves the
 * player out of the body to the nearer side and skips `aS.S > 43` states.
 */
class Slice404Test {
    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..8).all { w.collisionCell(cx + it, cy) == 20 &&
                    (1..4).all { r -> w.collisionCell(cx + it, cy - r) < 12 } })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    private fun soldier(w: Level0World, x: Int, y: Int, s: Int, aA: Int): Entity {
        val e = w.npcs.first { it.ax == 11 }
        e.setPositionPx(x, y)
        e.Z[0] = 0; e.cq = false; e.aB = 300; e.s = null; e.ae = null
        e.aA = aA
        e.setAnim(s); e.refreshBoxes()
        return e
    }

    /** Player stands 6 px left of the soldier (boxes overlap); one soldier tick. */
    private fun playerAkAfter(aA: Int, s: Int, playerAA: Int, playerS: Int = 0): Pair<Int, Int> {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, s, aA)
        val p = w.player
        p.setPositionPx(gx - 6, gy); p.setAnim(playerS); p.refreshBoxes()
        p.aA = playerAA
        e.refreshBoxes()
        assertTrue(Entity.overlapStrict(p.W, e.W), "fixture: the bodies overlap")
        val before = p.ak
        w.npcFsm.tick(e, p)
        return before to p.ak
    }

    @Test fun `an alerted soldier is solid to the player`() {
        val (before, after) = playerAkAfter(aA = 1, s = 11, playerAA = 0)
        assertTrue(after < before - 5, "@7644-7657: the aA != 0 branch reaches a() too — $before -> $after")
    }

    @Test fun `an unalerted soldier stays solid`() {
        val (before, after) = playerAkAfter(aA = 0, s = 2, playerAA = 0)
        assertTrue(after < before - 5, "aA == 0 && aS.aA <= 2 (the corner the port had) — $before -> $after")
    }

    @Test fun `a player state above 2 still meets the body (the l() branch joins a() as well)`() {
        val (before, after) = playerAkAfter(aA = 0, s = 2, playerAA = 4)
        assertTrue(after < before - 5, "aS.aA == 4: l()/a(32) then a() @7644 — $before -> $after")
    }

    @Test fun `the push is skipped while the player is scripted (aS-aA bit 8)`() {
        val (before, after) = playerAkAfter(aA = 1, s = 11, playerAA = 8)
        assertEquals(before, after, "@7644-7653: (aS.aA & 8) != 0 skips a()")
    }

    @Test fun `states above 43 are never shoved (a() @265) - the pinned victim keeps its pin`() {
        val (before, after) = playerAkAfter(aA = 1, s = 11, playerAA = 0, playerS = 89)
        assertEquals(before, after, "aS.S > 43 → return")
    }

    @Test fun `the shove puts the player half a body width plus half the player's off the soldier`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 11, aA = 1)
        val p = w.player
        // right of the soldier: pushed right (i.a() @371-399: ak + pw/2 + ew/2)
        p.setPositionPx(gx + 6, gy); p.setAnim(0); p.refreshBoxes(); p.aA = 0
        e.refreshBoxes()
        val want = e.ak + (p.W[2] - p.W[0]) / 2 + (e.W[2] - e.W[0]) / 2
        w.npcFsm.tick(e, p)
        assertEquals(want, p.ak, "p.ak=${p.ak} e.ak=${e.ak}")
    }
}
