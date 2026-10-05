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

class Slice114Test {

    private fun armDialog(w: Level0World, u: Int, v: Int, w_: Int,
                          vararg lines: String): Level0World {
        w.autoDismissDialog = false                    // real u-machine, not the harness tap
        w.stateL(21)
        w.dlgU = u; w.dlgV = v; w.dlgW = w_
        lines.forEachIndexed { i, s -> w.dlgBM[i] = s }
        w.dlgBT = -1                                   // fully revealed
        return w
    }

    @Test fun `u9 single-page confirm exits via C-Z + l-8`() {
        val w = armDialog(world(), 9, 0, 0, "Stay hidden?")
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(8, w.jC, "v==w && u==9 → C.Z(); l(8) (:985-989)")
    }

    @Test fun `u9 multi-page press advances a page then retypes it`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(1, w.dlgV, "press && u!=8 → x=48; D(v+1) (:3604)")
        assertEquals(0, w.dlgBT, "D() → A() → z() — the new page retypes")
        assertEquals(21, w.jC)
    }

    @Test fun `u9 reveal-then-advance reaches the last page and exits`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())     // v=1, retyping
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())     // reveal (bT=-1)
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())     // v=2=w → exit
        assertEquals(8, w.jC, "v==w && u==9 → C.Z(); l(8)")
    }

    @Test fun `u8 auto-advances pages on the 48-frame countdown`() {
        // the world runs under a u8 dialog (slice 380): settle the m0
        // intro claim first, or it opens its own dialog over this one
        val w = armDialog(world().also { settleIntro(it) }, 8, 0, 2, "p1", "p2", "p3")
        repeat(49) { w.tick(emptyList()) }             // x6=48→…→x6=0 → D(v+1)
        assertEquals(1, w.dlgV, "x<=0 → x=48; D(v+1) (:964-968)")
        assertEquals(48, w.kDlgX)
    }

    @Test fun `u8 fire press while counting also advances nothing but l-8 at last page`() {
        val w = armDialog(world(), 8, 2, 2, "p1", "p2", "p3")
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(8, w.jC, "v==w && u==8 → cz=true; l(8) (:979-982)")
    }

    @Test fun `u10 tap advances a page and latches cz`() {
        val w = armDialog(world(), 10, 0, 2, "p1", "p2", "p3")
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(1, w.dlgV, "v(65568) → D(v+1) (:956-958)")
        assertEquals(21, w.jC)
    }

    @Test fun `u3 exit routes to win-stats or credits by mission`() {
        val w = armDialog(world(), 3, 0, 0, "done")
        w.kAj = 7                                      // finale mission
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(24, w.jC, "u==3 && aj==7 → l(24) (:994-996)")
        val w2 = armDialog(world(), 3, 0, 0, "done")
        w2.pad.e(Pad.M_CONTEXT); w2.tick(emptyList())
        assertEquals(15, w2.jC, "u==3 && aj!=7 → l(15) (:991-993)")
    }

    @Test fun `u1 post-game exits to menu when aj==8`() {
        val w = armDialog(world(), 1, 0, 0, "the end")
        w.kAj = 8
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(2, w.jC, "u==1 && aj==8 → aj=0;W();l(2) (:1001-1004)")
        assertEquals(0, w.kAj)
        val w2 = armDialog(world(), 1, 0, 0, "x")
        w2.pad.e(Pad.M_CONTEXT); w2.tick(emptyList())
        assertEquals(8, w2.jC, "u==1 && aj!=8 → l(8)")
    }

    @Test fun `u0-4-5-7 full-screen panel exits`() {
        val w7 = armDialog(world(), 7, 0, 0, "x")
        w7.pad.e(Pad.M_CONTEXT); w7.tick(emptyList())
        assertEquals(2, w7.jC, "u==7 → l(2) (:894-895)")
        val w5 = armDialog(world(), 5, 0, 0, "x")
        w5.pad.e(Pad.M_CONTEXT); w5.tick(emptyList())
        assertEquals(15, w5.jC, "u==5 → l(15) (:896-897)")
        val w0 = armDialog(world(), 0, 0, 0, "x")
        w0.pad.e(Pad.M_CONTEXT); w0.tick(emptyList())
        assertEquals(8, w0.jC, "u==0 → l(8)")
    }

    @Test fun `typing fire press reveals the page`() {
        val w = armDialog(world(), 6, 0, 2, "reveal me")
        w.dlgBT = 3                                    // mid-typing
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertEquals(-1, w.dlgBT, "v(65568) while typing → bT=-1 (:953)")
        assertEquals(21, w.jC, "v<w → dialog persists (:975)")
    }

    @Test fun `fS marquee crawls one char per two frames then resets`() {
        val w = armDialog(world(), 9, 0, 2, "x")     // v<w → stays up
        w.kFS = 0
        repeat(4) { w.tick(emptyList()) }
        assertTrue(w.tipStr.length in 1..2, "one char per two ticks")
        val s = w.d0(111)!!
        w.kFS = s.length + 11
        w.tick(emptyList())
        assertEquals(-1, w.kFS, "fS >= len+10 → -1 (:1034-1035)")
    }
}
