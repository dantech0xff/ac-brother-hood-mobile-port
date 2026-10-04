package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 388 — `g.az()` and `g.i(i)` follow the bytecode (g.javap.txt az()
 * 0-1819, i(i) 0-188; i.javap.txt e(i) 0-287 for the LOS walk).
 *
 * The port had read the scan from the decompile and: missed the `S == 250`
 * clear and the `g != null` guard on the S270/S271 return; nulled `g` for
 * S268 only (the bytecode also rebinds on S267 and S291, binding the last
 * candidate in `k.bd` order before `best` narrows and with no LOS test);
 * applied the 440px / 60px checks to an ax4 target (the whole block skips
 * it); kept an ax11 `Z[19]==1` target only while `inFrontOf` (the bytecode
 * keeps it at dx == 0); never ran the LOS test `this.e(bd[i])` on NPC
 * candidates (a wall between hides them — and a hidden one no longer reaches
 * the `g = null` boundary at @1283); read `g.i(i)`'s `!av` facing backwards,
 * returned from inside its first block instead of falling through to the
 * state list, and dropped `S == 267`; and scanned `npcs` instead of `k.bd`.
 */
class Slice388Test {
    private fun player(ak: Int, al: Int, s: Int = 0): Entity {
        val p = Entity(0, null)
        p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10; p.W[1] = al - 40; p.W[3] = al
        p.S = s
        return p
    }

    private fun soldier(ak: Int, al: Int, aA: Int = 1): Entity {
        val e = Entity(11, null)
        e.ak = ak; e.al = al
        e.W[0] = ak - 10; e.W[2] = ak + 10; e.W[1] = al - 40; e.W[3] = al
        e.aB = 50; e.aA = aA
        return e
    }

    private fun open() = Slice128Test.MarkerWorld(cell = 0)

    @Test fun `S250 clears g and at like a hidden player`() {
        val w = open(); val p = player(200, 100, 250)
        val e = soldier(250, 100)
        w.npcs += e
        p.g = e; Entity.at = e
        PlayerFsm(w).interactScan(p)
        assertNull(p.g); assertNull(Entity.at)
        Entity.at = null
    }

    @Test fun `S270 holds a bound g but an unbound g keeps scanning`() {
        val w = open(); val p = player(200, 100, 270)
        val far = soldier(900, 100)                  // 700px away: a normal tick would drop it
        w.npcs += far
        p.g = far
        PlayerFsm(w).interactScan(p)
        assertSame(far, p.g, "g != null && S270 → return before the validity checks")
        // no bound g: the guard does not apply — the scan binds the candidate
        val q = player(200, 100, 270)
        val near = soldier(250, 100)
        w.npcs.clear(); w.npcs += near
        PlayerFsm(w).interactScan(q)
        assertSame(near, q.g, "S270 with g == null still scans")
    }

    @Test fun `S267 and S291 rebind the last candidate in draw order`() {
        for (s in intArrayOf(267, 291)) {
            val w = open(); val p = player(200, 100, s)
            val a = soldier(250, 100); val b = soldier(300, 100); val c = soldier(350, 100)
            w.npcs += listOf(a, b, c)
            p.g = a
            PlayerFsm(w).interactScan(p)
            // the rebind branch binds every candidate that is nearer than
            // `best` (440, never narrowed) — the last one in `k.bd` wins
            assertSame(c, p.g, "S$s: g = last facing candidate")
        }
    }

    @Test fun `S267 S268 and S291 drop a bound g the scan does not rebind`() {
        for (s in intArrayOf(267, 268, 291)) {
            val w = open(); val p = player(200, 100, s)
            val bound = soldier(260, 100)            // valid and near, but off the draw list
            p.g = bound
            PlayerFsm(w).interactScan(p)
            assertNull(p.g, "S$s: g = null at @66-101 and nothing rebinds it")
        }
    }

    @Test fun `S291 bypasses the facing gate, S267 does not`() {
        val behind = soldier(150, 100)
        for ((s, expectBound) in listOf(291 to true, 267 to false, 268 to true)) {
            val w = open(); val p = player(200, 100, s); p.av = false
            w.npcs += behind
            PlayerFsm(w).interactScan(p)
            assertEquals(expectBound, p.g === behind, "S$s with the candidate behind")
        }
    }

    @Test fun `an ax4 target is exempt from the aB distance and dy checks`() {
        val w = open(); val p = player(200, 100)
        val crate = Entity(4, null)
        crate.S = 30; crate.aB = 0                   // no aB
        crate.ak = 800; crate.al = 400               // 600px / 300px away
        crate.W[0] = 790; crate.W[2] = 810; crate.W[1] = 380; crate.W[3] = 400
        p.g = crate
        PlayerFsm(w).interactScan(p)
        assertSame(crate, p.g, "g.ax == 4 → the @102-182 block is skipped whole")
        // an ordinary NPC with the same numbers is dropped
        val s = soldier(800, 400); s.aB = 50
        p.g = s
        PlayerFsm(w).interactScan(p)
        assertNull(p.g)
    }

    @Test fun `an ax11 Z19 target at the player's x stays bound`() {
        val w = open(); val p = player(200, 100); p.av = true
        val e = soldier(200, 100); e.Z[19] = 1
        p.g = e
        PlayerFsm(w).interactScan(p)
        assertSame(e, p.g, "av && dx > 0 drops — dx == 0 keeps")
        e.ak = 230                                   // now strictly behind a west-facer
        PlayerFsm(w).interactScan(p)
        assertNull(p.g)
    }

    @Test fun `a wall between hides a candidate and it never reaches the g boundary`() {
        // wall column at x240-259 between the player (200) and the soldier (300)
        val wall = Slice128Test.MarkerWorld(cellFn = { cx, _ -> if (cx == 12) 20 else 0 })
        val p = player(200, 100); p.av = false
        val keep = soldier(150, 100)                 // behind: not a candidate, g survives
        val hidden = soldier(300, 100)
        wall.npcs += listOf(keep, hidden)
        p.g = keep
        PlayerFsm(wall).interactScan(p)
        assertSame(keep, p.g, "the LOS-blocked soldier never nulls or rebinds g")
        assertNull(p.ci)
        // same scene without the wall: the soldier is bound
        val clear = open()
        val q = player(200, 100); q.av = false
        val keep2 = soldier(150, 100); val seen = soldier(300, 100)
        clear.npcs += listOf(keep2, seen)
        q.g = keep2
        PlayerFsm(clear).interactScan(q)
        assertSame(seen, q.g)
        assertSame(seen, q.ci)
    }

    @Test fun `the scan walks the last paint, not the npc list`() {
        val w = world()
        w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 150); p.av = false; p.refreshBoxes()
        p.setAnim(295)
        val e = Entity(11, w.clips[7])
        e.aB = 50; e.aA = 1
        e.setPositionPx(350, 150); e.refreshBoxes()
        w.npcs.add(0, e)
        w.drawCount = 0
        w.playerFsm.interactScan(p)
        assertNull(p.g, "not painted by the last b() pass → not a candidate")
        w.paint(e)
        w.playerFsm.interactScan(p)
        assertSame(e, p.g)
    }

    // -- g.i(i) -------------------------------------------------------------------

    @Test fun `i(i) offer window faces the candidate within 200px`() {
        for (av in booleanArrayOf(false, true)) {
            val p = player(500, 100); p.av = av; p.gJ = 4
            val front = soldier(if (av) 400 else 600, 100); front.Z[19] = 1
            val behind = soldier(if (av) 600 else 400, 100); behind.Z[19] = 1
            val far = soldier(if (av) 200 else 800, 100); far.Z[19] = 1
            assertTrue(p.interactEligible(front), "av=$av: in front within 200 → offer")
            assertFalse(p.interactEligible(behind), "av=$av: behind → falls through, S0 → false")
            assertFalse(p.interactEligible(far), "av=$av: 300px → falls through")
        }
    }

    @Test fun `i(i) falls through to the state list when the offer window misses`() {
        val behind = soldier(400, 100); behind.Z[19] = 1
        for ((s, expected) in listOf(268 to true, 291 to true, 267 to true, 295 to true,
                                     357 to true, 358 to true, 299 to true, 307 to true,
                                     308 to false, 0 to false, 266 to false)) {
            val p = player(500, 100, s); p.av = false; p.gJ = 4
            assertEquals(expected, p.interactEligible(behind), "S$s with the candidate behind")
        }
    }

    @Test fun `i(i) S303 is eligible whether or not the anim has ended`() {
        // S303 sits inside the 299..307 range — `S == 303 && r()` is redundant
        val w = world()
        val p = w.player
        p.setPositionPx(300, 150); p.refreshBoxes()
        p.setAnim(303)
        assertFalse(p.animFinished(), "fresh S303 anim")
        assertTrue(p.interactEligible(Entity(11, null)))
    }
}
