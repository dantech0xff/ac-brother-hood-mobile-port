package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 367 — the L777 melee gate reads `i.ab()`.
 *
 * `if (k.C != null && k.C.ab()) r13 = false; if (r13) aB();`
 * (simple/i.java:6251-6255, structured :4188-4192, proven). `i.ab()`
 * (bytecode i.javap.txt:68769-68793) is `ca >= 0 && !cd[0] && cK >= 0`,
 * `cK` being the claim-script step — the port's `claimAb()`. The port
 * read a helper with `cd[0]` inverted and `cK` taken from the drag-anchor
 * field, so a running cutscene claim let soldiers strike the player while
 * a paused one shielded them.
 */
class Slice367Test {
    /** A soldier mid-strike (S12 frame 1 carries the sword box) with the
     *  player standing inside that box; returns the player's HP after one
     *  `I()` of the soldier. */
    private fun strike(claim: (Entity) -> Unit): Pair<Int, Int> {
        val w = world()
        val s = w.npcs.first { it.ax == 11 }
        s.ag = 0; s.ah = 0
        s.setAnim(12); s.T = 1
        s.refreshBoxes()
        val p = w.player
        p.setPositionPx((s.X[0] + s.X[2]) / 2, s.al)
        p.setAnim(0)
        p.gt = 0
        p.refreshBoxes()
        assertTrue(Entity.overlapI(p.W, s.X), "precondition: the player stands in the sword box")
        w.kC = Entity(0, null).also(claim)
        val hp = p.x1
        w.npcFsm.tick(s, p)
        return hp to p.x1
    }

    private fun claimer(paused: Boolean): (Entity) -> Unit = { c ->
        c.ca = 0
        c.cd[0] = paused
        c.scriptStep = 0
    }

    @Test fun `a running claim suppresses the soldier's melee`() {
        val (before, after) = strike(claimer(paused = false))
        assertEquals(before, after, "C.ab() → r13 = false → no aB()")
    }

    @Test fun `a paused claim does not`() {
        val (before, after) = strike(claimer(paused = true))
        assertNotEquals(before, after, "cd[0] → ab() false → aB() lands")
    }

    @Test fun `an unbound or finished claim does not`() {
        val (b1, a1) = strike { c -> c.ca = -1; c.scriptStep = 0 }
        assertNotEquals(b1, a1, "ca < 0 → ab() false")
        val (b2, a2) = strike { c -> c.ca = 0; c.scriptStep = -1 }
        assertNotEquals(b2, a2, "cK < 0 → ab() false")
    }
}
