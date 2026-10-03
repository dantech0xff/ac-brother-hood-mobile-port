package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 380 — case 8 / case 21 is one body (k.java:859-1063): `I()` on
 * jC 8 and under a u==8 dialog, else `H()` on a flying mission; `b(false)`;
 * the dialog switch whenever `j.c == 21` (also on the frame whose sim
 * opened the dialog); then the tail — the mission timer (dialog time
 * counts unless u==9 or a claim runs), the `fS` marquee and `J()`.
 */
class Slice380Test {
    private fun tip(w: Level0World, u: Int) {
        w.autoDismissDialog = false
        w.dlgU = u
        w.stateL(21)
        w.dlgV = 0; w.dlgW = 2
        w.dlgBM[0] = "p1"; w.dlgBM[1] = "p2"; w.dlgBM[2] = "p3"
        w.dlgBT = -1
    }

    @Test fun `the player can walk under a u8 tip`() {
        val w = world()
        settleIntro(w)
        tip(w, 8)
        val x0 = w.player.ak
        repeat(10) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()) }
        assertEquals(21, w.jC)
        assertTrue(w.player.ak > x0, "I() runs under u==8 (k.java:861-863)")
    }

    @Test fun `the world stands still under a line dialog`() {
        val w = world()
        settleIntro(w)
        tip(w, 1)
        val x0 = w.player.ak
        repeat(10) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()) }
        assertEquals(21, w.jC)
        assertEquals(x0, w.player.ak, "no I() on a ground mission (k.java:864)")
    }

    @Test fun `dialog time counts on the mission timer unless u9`() {
        val w = world()
        settleIntro(w)
        w.player.P = w.player.P and 512.inv()     // aS.P&512 counts regardless
        tip(w, 1)
        val dg = w.kDg
        repeat(5) { w.tick(emptyList()) }
        assertEquals(dg + 5, w.kDg, "k.java:1022-1026 runs on jc21 frames")
        tip(w, 9)
        val dg9 = w.kDg
        repeat(3) { w.tick(emptyList()) }
        assertEquals(dg9, w.kDg, "u9 stops it")
    }

    @Test fun `a dialog opened by the sim is switched in the same frame`() {
        val w = world()
        settleIntro(w)
        w.autoDismissDialog = false
        // an ax10 S21 tip zone under the player: k.b(8,…) + l(21), k.x=48
        val p = w.player
        val z = Entity(10, w.clips[6]).apply {
            aw = 9_100; setAnim(21); setPositionPx(p.ak, p.al); P = P or 16
        }
        z.W[0] = p.W[0] - 5; z.W[1] = p.W[1] - 5; z.W[2] = p.W[2] + 5; z.W[3] = p.W[3] + 5
        w.npcs += z
        w.tick(emptyList())
        assertEquals(21, w.jC, "the zone opened the tip this frame")
        assertEquals(8, w.dlgU)
        // the switch ran this frame: the page is typing (`bQ` from k.b(),
        // k.java:370) and the typewriter stepped once (:947)
        assertEquals(1, w.dlgBR)
    }

    @Test fun `a flying mission's shots move behind a line dialog`() {
        val w = world(aj = 1)
        w.stateL(8)
        val shot = Entity(24, w.clips[0]).apply {
            aw = 9_200; setAnim(9); setPositionPx(w.player.ak, w.player.al); ag = 1024
        }
        w.npcs += shot
        tip(w, 1)
        val x0 = shot.N
        w.tick(emptyList())
        assertNotEquals(x0, shot.N, "H() ticks ax24 S8/9/10 (k.java:2507-2513)")
    }
}
