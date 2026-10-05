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

class Slice318Test {
    /** The S25 soldier-fall arm (i.java:4711-4736, proven) — the landing
     *  resolution the previous bare fall-through lacked: forced terminal
     *  fall (ah=5120, aj=1536) then, once the ANCHOR cell turns solid
     *  (>=18 / 2|3), snap al to the cell top + resume via m() — soft drop
     *  (<=80px from the Z[2] fall-start marker) → i(2) patrol for a Z0=0
     *  guard; hard drop → i(0) kill. Without this arm a guard that fell
     *  (ledge knock, or a record-stamped S25 like m2's aw313 wall perch)
     *  froze in S25 forever — unfightable, the live repro's perch-guard. */
    private fun fallingGuard(w: Level0World, dropPx: Int, flat: Boolean = false): Entity {
        var g = w.npcs.first { it.ax == 11 }
        if (flat) {
            // a slope landing (iE 2|3) always resolves hard — the soft
            // resume needs a flat >=18 cell under the anchor column.
            g = w.npcs.filter { it.ax == 11 }.firstOrNull {
                standOn(w, it)
                w.collisionCell(it.ak / 20, it.al / 20 + 1) >= 18 ||
                    w.collisionCell(it.ak / 20, it.al / 20) >= 18
            } ?: g
        }
        standOn(w, g)                                   // real ground
        val groundAl = g.al
        g.setPositionPx(g.ak, groundAl - dropPx)        // suspend mid-air
        g.refreshBoxes()
        g.setAnim(25)
        return g
    }

    @Test fun `S25 fall resumes — soft drop returns to patrol`() {
        val w = world()
        val g = fallingGuard(w, 40, flat = true)        // <=80px, flat cell
        var ticks = 0
        while (g.S == 25 && ticks++ < 200) w.npcFsm.tick(g, w.player)
        assertEquals(2, g.S, "soft landing → i(2) patrol (m(40,2) Z0==0)")
        assertEquals(0, g.ah); assertEquals(0, g.aj)
        assertEquals(-1, g.Z[2], "fall marker consumed")
    }

    @Test fun `S25 fall resumes — hard drop kills the guard`() {
        val w = world()
        val g = fallingGuard(w, 120)                    // >80px drop
        var ticks = 0
        while (g.S == 25 && ticks++ < 200) w.npcFsm.tick(g, w.player)
        assertEquals(0, g.S, "hard landing → i(0) (m(43,0) Z0==0)")
        assertEquals(0, g.ah); assertEquals(0, g.aj)
    }

    @Test fun `airborne soldier enters S25 via the tail gate and resumes`() {
        // the full real-world path: an arm-less state knocked airborne →
        // the L777 open-cell gate fires i(25) → the S25 arm falls it,
        // lands it at the anchor cell, and resumes — the loop that left
        // the reported wall-perch guard unfightable before this arm.
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setPositionPx(g.ak, g.al - 40)
        g.refreshBoxes()
        g.setAnim(50)                                   // no arm → tail gate
        var ticks = 0
        while (g.S != 25 && ticks++ < 20) w.npcFsm.tick(g, w.player)
        assertEquals(25, g.S, "open cells → tail gate fires i(25)")
        ticks = 0
        while (g.S == 25 && ticks++ < 200) w.npcFsm.tick(g, w.player)
        assertTrue(g.S != 25, "S25 arm landed + resumed (S=${g.S})")
    }

    @Test fun `S152 posted perch guard holds its post — verdict`() {
        // m2's wall-perch trio aw311/312/313 are record-stamped ax11
        // S=152 — a posted-guard anim, not a fall. In the original's
        // soldier dispatch S152 hits the grouped arm (i.java:4462
        // `default:` — Q==6/n(44,0)/S!=85/!h/!z5 shell) → the shared
        // tail: j() damage intake + aB() melee + the open-cell fall
        // gate. h() reads the perch cell under the feet as ground →
        // the guard stays posted (it falls only when the perch opens
        // or when knocked). NOTE: this calls npcFsm.tick directly —
        // the entity-level tick gate skips P|32 sentries entirely
        // (see the dormant-park verdict below), so in live play the
        // shared tail only runs once a wake trigger un-parks them.
        val w = world(aj = 2)
        val g = w.npcs.first { it.aw == 313 }
        assertEquals(11, g.ax); assertEquals(152, g.S)
        repeat(400) { w.npcFsm.tick(g, w.player) }
        assertEquals(152, g.S, "posted guard holds S152 on its perch (no spurious fall)")
        assertEquals(300, g.aB, "posted guard untouched while idle")
    }

    @Test fun `S152 posted guard is dormant while parked — verdict`() {
        // aw311/312/313 spawn ax11 S=152 with P=33 (posted bit-0 +
        // dormant-park bit-5). The entity tick gate (non-bh3 arm
        // Level0World.kt:4966-4973, k.java L215 proven) drops a P|32
        // sentry in BOTH branches — au<2 && P&32 without P&16 → skip,
        // au>=2 without P&16 → skip — so a parked sentry NEVER ticks.
        // The shared tail's j()/k() intake cannot run: the guard is
        // invulnerable AND unresponsive while parked, by design — the
        // same au-park dormant-sentry family as the slice-219 street
        // soldiers. The original's wake is an external force-tick
        // `P|=16` (i.java:2269+) or P&~32 from alert/director arms.
        val w = world(aj = 2)
        val g = w.npcs.first { it.aw == 313 }
        assertTrue(g.P and 32 != 0, "sentry spawns parked (P|32)")
        repeat(60) {
            w.player.setPositionPx(g.ak + 20, g.al)
            w.player.av = g.av
            if (w.player.S !in intArrayOf(67, 68, 69, 112, 113, 114, 115))
                w.player.setAnim(67)
            w.tick(emptyList())
        }
        assertEquals(152, g.S, "parked sentry holds the posted anim")
        assertEquals(300, g.aB, "parked sentry never ticks → no intake → invulnerable")
    }

    @Test fun `woken posted guard is beatable — verdict`() {
        // Once the park bit clears (the external wake trigger) and the
        // camera arrives with the player (au<2 — real play satisfies it
        // because the tracker follows the player to the guard), the
        // sentry joins the tick path → j() intake lands blind-side
        // strikes → it dies. This is the actual fightable path the
        // "unfightable posted guard" repro was missing: the sentry was
        // still parked, not broken.
        // slice 357: the woken guard runs the real aC() scheduler, so it
        // `Q()`-faces the player and backs off in S23 — the scripted
        // player now faces the guard (it struck the guard's back before,
        // which only worked while S23 never turned it around).
        val w = world(aj = 2)
        val g = w.npcs.first { it.aw == 313 }
        g.P = g.P and -33                                  // wake: P&~32
        var ticks = 0
        while (g.aB > 0 && g.S != 139 && ticks++ < 800) {
            w.player.setPositionPx(g.ak + 20, g.al)
            w.player.av = true                             // face the guard (west)
            if (w.player.S !in intArrayOf(67, 68, 69, 112, 113, 114, 115))
                w.player.setAnim(67)
            w.tick(emptyList())
        }
        assertTrue(g.aB <= 0 || g.S == 139,
            "woken sentry beatable (aB=${g.aB}, S=${g.S}, ticks=$ticks)")
        assertTrue(w.player.x1 > 0, "player survives (x1=${w.player.x1})")
    }
}
