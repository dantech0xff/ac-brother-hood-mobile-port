package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 398 — the ax4 crate FSM `aj()` (i.javap `aj()` @0-1065) and the wisp
 * spawner `m(int)` it calls, re-read from the raw bytecode:
 *
 * - **S5/S7 gate** @72-248: the first branch is the STATIC `g.b(k.aS.S)` @78 —
 *   the aerial/action set {18-20,22-25,35,36,43,150,157,165,233,242,243,
 *   263-266} (the same set slice 397 pinned for the S16 door tap) — NOT the
 *   no-arg attack list. In it, a BODY overlap cracks the crate open
 *   (`i(S+1); k.A(14)` @185-248); outside it the context bubble (`k.a/k.c`,
 *   `k.m/k.k`) and the `a()` push-past run. The attack hitbox test @217 is
 *   shared by both. The port gated on `isAttackState`, so a diving player
 *   bounced off the crate and a ground combo skipped the prompt + push-past.
 * - **S6/S8 wisp loop** @249-352: the first wisp, then `while (m > 0)` spawns
 *   EVERY remaining wisp in the same tick. The port stopped after two while
 *   `k.aq += m` (initDestructible, slice 389) had counted all of them in the
 *   HUD total — the m>2 crates of missions 2/3/5/6 (m = 3..7) could never be
 *   emptied.
 * - **`m(int)` @107-109**: `aload_0; iconst_1; putfield aG` — the "has
 *   spawned" flag lands on the SPAWNER (`this`), not on the wisp; the wisp's
 *   orbit-end arm reads it back as `af.aG != 0` → the second `k.A(15)`.
 */
class Slice398Test {
    private fun crateWorld(s: Int): Pair<Level0World, Entity> {
        val w = world()
        val d = w.npcs.first { it.ax == 4 && it.S == s }
        d.refreshBoxes()
        parkPlayer(w)
        return w to d
    }

    /** Player far from everything: body, hitbox and ctx bubble all outside. */
    private fun parkPlayer(w: Level0World) {
        val p = w.player
        intArrayOf(-4000, -4000, -3980, -3960).copyInto(p.W)
        intArrayOf(-3000, -3000, -2980, -2960).copyInto(p.X)
        intArrayOf(-2000, -2000, -1980, -1960).copyInto(w.kM)
    }

    private fun wispCount(w: Level0World) = w.pendingInsert.count { it.ax == 74 }

    // ------------------------------------------------------------ S5 / S7 gate
    private val airSet = intArrayOf(18, 19, 20, 22, 23, 24, 25, 35, 36, 43,
        150, 157, 165, 233, 242, 243, 263, 264, 265, 266)
    private val attackList = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115,
        183, 184, 216, 217, 286, 287)

    @Test fun `S5 and S7 - a body overlap in any g_b(I) anim cracks the crate`() {
        for (crate in intArrayOf(5, 7)) for (s in airSet) {
            val (w, d) = crateWorld(crate)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(p.W)                                    // body on the crate
            w.npcFsm.tickDestructible(d, p)
            assertEquals(crate + 1, d.S, "crate S$crate, player S$s ∈ g.b(I)")
            assertTrue(14 in w.sfxLog, "k.A(14) @243")
        }
    }

    @Test fun `S5 and S7 - the attack-list anims are not in the aerial set, body alone does nothing`() {
        for (crate in intArrayOf(5, 7)) for (s in attackList) {
            val (w, d) = crateWorld(crate)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(p.W)
            w.npcFsm.tickDestructible(d, p)
            assertEquals(crate, d.S, "S$s ∉ g.b(I): the body arm is skipped")
            assertFalse(14 in w.sfxLog)
        }
    }

    @Test fun `S5 and S7 - the attack hitbox cracks it from any state`() {
        for (crate in intArrayOf(5, 7)) for (s in intArrayOf(0, 67, 43)) {
            val (w, d) = crateWorld(crate)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(p.X)                                    // @217 a(aS.X, W)
            w.npcFsm.tickDestructible(d, p)
            assertEquals(crate + 1, d.S, "S$s X ∩ W")
            assertTrue(14 in w.sfxLog)
        }
    }

    @Test fun `S5 - inside the aerial set neither the bubble nor the push-past run`() {
        // ctx bubble over the crate, body clear of it: grounded claims, aerial skips
        for (s in intArrayOf(0, 1, 5)) {
            val (w, d) = crateWorld(5)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(w.kM)                                   // k.M ∩ W
            w.npcFsm.tickDestructible(d, p)
            assertSame(d, w.claimed, "S$s ∉ g.b(I): the bubble claims (k.a prio 5)")
        }
        for (s in intArrayOf(43, 22, 157)) {
            val (w, d) = crateWorld(5)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(w.kM)
            w.npcFsm.tickDestructible(d, p)
            assertNull(w.claimed, "S$s ∈ g.b(I): the bubble arm is not entered")
            assertEquals(5, d.S, "no body overlap → no crack either")
        }
    }

    @Test fun `S5 and S7 - a ground combo runs the bubble arm (the old attack-list gate skipped it)`() {
        for (crate in intArrayOf(5, 7)) for (s in attackList) {
            val (w, d) = crateWorld(crate)
            val p = w.player
            p.setAnim(s)
            d.W.copyInto(w.kM)                                   // k.M ∩ W, body clear
            w.npcFsm.tickDestructible(d, p)
            assertSame(d, w.claimed, "crate S$crate, player S$s ∉ g.b(I): k.a(this,5,W) @119-125")
            assertEquals(crate, d.S)
        }
    }

    // ------------------------------------------------------------ S6 / S8 loop
    private fun driveToFinish(d: Entity) {
        var guard = 0
        while (!d.animFinished() && guard++ < 400) d.advanceAnim()
        assertTrue(d.animFinished(), "fixture: the anim must end")
    }

    @Test fun `S6 and S8 - every remaining wisp spawns in the tick the anim ends`() {
        for (open in intArrayOf(6, 8)) for (m in intArrayOf(0, 1, 2, 3, 5, 7)) {
            val (w, d) = crateWorld(open - 1)
            d.setAnim(open); d.m = m
            val apBase = w.kAp[5]; val azBase = w.kAz; val base = wispCount(w)
            val p = w.player
            // mid-anim: nothing yet
            w.npcFsm.tickDestructible(d, p)
            if (!d.animFinished()) assertEquals(base, wispCount(w), "r() @253 gates the burst")
            driveToFinish(d)
            w.npcFsm.tickDestructible(d, p)                      // the ONE tick
            assertEquals(base + m, wispCount(w), "S$open m=$m: all $m wisps this tick")
            assertEquals(apBase + m, w.kAp[5], "k.o(5) once per wisp")
            assertEquals(azBase + m, w.kAz, "k.s() once per wisp")
            assertEquals(0, d.m)
            assertTrue(d in w.pendingRemove, "k.c(this) @349")
        }
    }

    @Test fun `S6 and S8 - leave no claim behind (k_m before k_c)`() {
        // The explicit `k.m()` @335-345 and the port's removeEntity release
        // both reach the same end state (the original's k.c() itself never
        // touches k.L — the claim only feeds the touch hit-test k.j(II) and
        // the debug overlay, neither of which the port consumes).
        val (w, d) = crateWorld(5)
        d.setAnim(6); d.m = 3
        w.claim(d, 5, d.W)
        assertSame(d, w.claimed)
        driveToFinish(d)
        w.npcFsm.tickDestructible(d, w.player)
        assertNull(w.claimed, "k.L.aw == aw → k.m() @335-345")
        assertEquals(6, w.claimPrio, "k.co back to 6 (unclaimed)")
    }

    @Test fun `every shipped crate drops exactly the wisps its record counted`() {
        var sawBig = false
        for (aj in 0..7) {
            val w = world(aj = aj)
            val crates = w.npcs.filter { it.ax == 4 && (it.S == 5 || it.S == 7) }
            for (d in crates) {
                val m = d.m
                if (m > 2) sawBig = true
                d.setAnim(d.S + 1)
                driveToFinish(d)
                val base = wispCount(w)
                w.npcFsm.tickDestructible(d, w.player)
                assertEquals(base + m, wispCount(w), "mission $aj crate aw=${d.aw} m=$m")
            }
        }
        assertTrue(sawBig, "fixture: the census has crates with m > 2")
    }

    // ------------------------------------------------------------ m(int) aG flag
    @Test fun `m(int) flags the spawner, not the wisp - and the orbit end plays the second sfx 15`() {
        val w = world()
        val src = w.npcs.first { it.ax == 4 }
        assertEquals(0, src.aG, "fixture: a fresh crate")
        w.spawnWisp(src)
        val wisp = w.pendingInsert.last { it.ax == 74 }
        assertEquals(1, src.aG, "@107-109 this.aG = 1")
        assertEquals(0, wisp.aG, "the wisp's own aG stays 0")
        assertSame(src, wisp.af)
        val base = w.sfxLog.count { it == 15 }
        var t = 0
        while (wisp.S != 2 && t++ < 60) w.npcFsm.tickAx74(wisp, w, w.player)
        assertEquals(2, wisp.S, "fixture: the orbit ends")
        assertEquals(base + 1, w.sfxLog.count { it == 15 }, "af.aG != 0 → k.A(15) @157-162")
        assertNull(wisp.af, "@165-167 af = null")
    }
}
