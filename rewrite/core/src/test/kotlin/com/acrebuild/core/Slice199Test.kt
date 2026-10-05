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

/** Slice 199 — `I()` dispatch `default:` arm L1f35 (i.java:18904,
 *  proven): every ax without a case — the no-op types AND ax 28/31/45/75
 *  (L1e35/L1f1d/L1f27/L1f13 all `goto L1f35`) — still runs `if (b) t()`,
 *  the `av` facing bit, and the `a(k.aS,P,W)` player push; no `s()`. */
class Slice199Test {
    private val clip by lazy { Clip.load(asset("clips/clip7/clip.acpk")) }

    @Test fun `default arm refreshes dirty bounds and facing bit`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = NpcFsm(w)
        val e = Entity(79, clip)              // ax79 — dispatch default
        e.setAnim(0); e.refreshBoxes()
        val w0 = e.W[0]
        e.ak += 7                              // moved without t()
        e.b = true                             // dirty flag (mover set it)
        e.av = true
        fsm.tick(e, w.player)                  // routes to L1f35
        assertEquals(w0 + 7, e.W[0], "t() re-ran on b → W tracked ak")
        assertEquals(1, e.P and 1, "av → P|=1")
        e.av = false
        fsm.tick(e, w.player)
        assertEquals(0, e.P and 1, "!av → P&=~1")
    }

    @Test fun `default arm pushes the falling player onto the prop`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = NpcFsm(w)
        val p = w.player
        val e = Entity(80, clip)              // ax80 — dispatch default
        e.setAnim(0); e.refreshBoxes()
        e.P = e.P or 4096                      // solid push flag (L5)
        // drop the player onto the prop's top edge (null-clip player:
        // stage W directly — top above prop top, bottom still above the
        // prop bottom, ak inside the x-span)
        val cx = (e.W[0] + e.W[2]) / 2
        p.ak = cx; p.al = e.W[1] - 2
        intArrayOf(cx - 2, e.W[1] - 4, cx + 2, e.W[1] + 2).copyInto(p.W)
        p.ah = 512                             // falling
        fsm.tick(e, p)
        assertEquals(e.W[1] - 5, p.al, "L11 land-on-top: al = r9[1]-5")
        assertEquals(0, p.ah, "fall stopped")
    }

    @Test fun `default arm never advances the anim`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = NpcFsm(w)
        val e = Entity(55, clip)              // ax55 waypoint node
        e.setAnim(0)
        val u0 = e.U
        e.T = 0; e.U = 0
        fsm.tick(e, w.player)
        assertEquals(u0, e.U, "no s() in the default arm")
    }
}
