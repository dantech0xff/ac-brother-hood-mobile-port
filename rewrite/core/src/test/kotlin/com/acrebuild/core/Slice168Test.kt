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

/**
 * Slice 168 — level-0 traversal blockers: the S107/108/109 directional
 * edge-vault arm (g.java L12de), the sticky `g.z` latch (`l()` head arms it
 * once per call — g.java:2982; airborne `cq=0; z=0` arms clear it), the
 * `b()` busy-anim guard inside `ap()` (g.java L28-L33), and the ax4 crate
 * block→break loop (i.java `a()` push + `tickDestructible` S5 arm).
 */
class Slice168Test {
    // -- helpers --------------------------------------------------------------
    private fun armed(): Pair<Level0World, Entity> {
        val w = world()
        w.stateL(8)
        w.player.gI = 1
        w.player.gJ = w.player.gJ or w.kF0Do[0]
        w.rebuildEquip()
        // run out the intro claim (~69t) then a little more
        for (t in 0 until 150) w.tick(listOf())
        return w to w.player
    }
    private fun holdRight(w: Level0World) =
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 95, 181)))
    private fun attackTap(w: Level0World) =
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 305, 200),
                      InputQueue.Event(1, InputQueue.Type.UP, 305, 200)))

    /** g.java L12de: running into the 3-cell wall at x~351 enters S109 and
     *  on anim end vaults `al -= 60`, `ak += 20`, then `a(0,9)` snaps `ak`
     *  to the cell center. */
    @Test fun `run into wall enters S109 and vaults 3 cells on anim end`() {
        val (w, p) = armed()
        holdRight(w)
        var vaulted = false
        var saw109 = false
        for (i in 0 until 400) {
            w.tick(listOf())
            if (p.S == 109) saw109 = true
            if (saw109 && p.S == 0 && p.al <= 890) { vaulted = true; break }
        }
        assertTrue(saw109, "player should enter S109 at the wall edge")
        assertTrue(vaulted, "vault should settle S0 above the wall (al<=890)")
        assertTrue(p.ak >= 365, "ak advanced through the wall cell, got ${p.ak}")
    }

    /** e()'s head clears `z` every tick (g.java:1285-1301), then EVERY
     *  non-transition S12 path falls through `L17c9 → L17cc → l()`
     *  (g.java:3560-3720 proven), whose head re-arms `cp/cq/z`
     *  (g.java:5049+ l() head). So `z` is armed on active-run ticks too —
     *  a run never dead-ends input. */
    @Test fun `z stays armed during S12 run and settle`() {
        val (w, p) = armed()
        holdRight(w)
        var sawArmedRun = false; var sawArmedStop = false
        var prevAk = p.ak
        for (i in 0 until 400) {
            w.tick(listOf())
            // a blocked tick: the run did not advance (the frame now ends
            // after e() re-arms ag — G12 — so ag==0 is not observable)
            val blocked = p.ak == prevAk || p.aO != 0
            if (p.S == 12 && !blocked && p.z) sawArmedRun = true
            if (p.S == 12 && blocked && p.z) sawArmedStop = true
            prevAk = p.ak
        }
        assertTrue(sawArmedRun, "active-run ticks arm z via L17c9→l()")
        assertTrue(sawArmedStop, "stopped/blocked ticks re-arm z via L17c9")
    }

    /** `cq=0; z=0` fling-exit pairs + the head clear: entering a fall
     *  leaves `z` cleared so ap() can't fire mid-air. */
    @Test fun `z clears when leaving the ground`() {
        val (w, p) = armed()
        for (i in 0 until 30) w.tick(listOf())
        assertTrue(p.z, "settled ground arms z via l()")
        p.enterFall(0, w); p.z = false; p.cq = false   // a(0) fling arm pair
        w.tick(listOf())
        assertFalse(p.z, "airborne tick keeps z cleared")
    }

    /** `ap()` guard `ac.ax==10 || b()`: a context tap during an attack anim
     *  must NOT re-fire i(67) — the combo chains via aj() only. */
    @Test fun `context tap during attack anim does not re-enter swing`() {
        val (w, p) = armed()
        // settle on ground, arm the swing
        for (i in 0 until 30) w.tick(listOf())
        attackTap(w)
        for (i in 0 until 3) w.tick(listOf())
        assertEquals(67, p.S, "expected S67 swing, got S=${p.S}")
        val t0 = p.T
        attackTap(w)
        w.tick(listOf())
        assertTrue(p.S != 67 || p.T > t0,
            "b() guard: tap during S67 must not restart the swing (S=${p.S} T=${p.T} was $t0)")
    }

    /** ax4 crate block→break: hold right into the aw10 crate — pushOut
     *  pins the runner (faithful), a context slash breaks the crate and
     *  the path clears. */
    @Test fun `crate blocks run then breaks under attack`() {
        val (w, p) = armed()
        holdRight(w)
        var pinned = false
        var prevAk = p.ak
        for (i in 0 until 300) {
            w.tick(listOf())
            // pinned: running but not advancing (G12: the frame ends after
            // e() re-arms ag, so read the stalled position, not ag==0)
            if (p.S == 12 && p.ak == prevAk && p.ak >= 500) { pinned = true; break }
            prevAk = p.ak
        }
        assertTrue(pinned, "runner should pin at the aw10 crate (~ak504)")
        for (k in 0 until 12) {
            attackTap(w)
            for (i in 0 until 15) w.tick(listOf())
        }
        assertTrue(w.npcs.none { it.aw == 10 } ||
                   w.npcs.first { it.aw == 10 }.S != 5,
            "crate should break (anim advanced or entity removed)")
        assertTrue(p.ak > 546, "player passed the crate, ak=${p.ak}")
    }
}
