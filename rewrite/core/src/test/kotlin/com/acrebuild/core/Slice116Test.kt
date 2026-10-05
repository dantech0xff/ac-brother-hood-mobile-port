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

// ==========================================================================
// Slice 116 — jc9 play-entry arm: bG/dl/A[]/save-restore + g.e(ax) + C() +
//             T() + ef[aj]→ab() + F(aj) (k.java:802-812 proven)
// ==========================================================================
class Slice116Test {

    private fun driveJc9Exit(w: Level0World) {
        w.stateL(9)
        repeat(165) { w.tick(emptyList()) }          // j.g → 165
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); w.pad.releaseFlush()
        w.tick(emptyList())                          // w(65568) → exit arm
    }

    @Test fun `jc9 exit restores save bytes and writes x1 from g-e ax`() {
        val w = world()
        w.kDB = 30; w.kDC = 44; w.kDF = 7
        driveJc9Exit(w)
        assertEquals(8, w.jC, "l(8) → play")
        assertEquals(30, w.kAx); assertEquals(44, w.kAy); assertEquals(7, w.kAN)
        assertEquals(30, w.player.x1, "g.e(ax) → x[1] = ax (:4421)")
        assertEquals(0, w.kBA[16], "a(bA,16,(short)0)")
        assertEquals(120, w.kDz); assertEquals(0, w.kAw)
    }

    @Test fun `jc9 exit resets the music-request slot bG`() {
        val w = world()
        w.kBg = 3                                     // queued track
        driveJc9Exit(w)
        assertEquals(0, w.kBg, "bG = 0 (:1070)")
    }

    @Test fun `jc9 exit C() non-bh3 arm resets scroll and camera`() {
        val w = world()
        w.kAR = 4; w.kAk = 9; w.kX = 12; w.kO = 500; w.kP = 700; w.rebuildCamRect()
        // cam ends re-tracked by m(ad) — assert the reset fields only
        w.kZ = true; w.kAb = true; w.kQ = 55
        driveJc9Exit(w)
        assertEquals(-1, w.kAR, "aR = -1 (:1864)")
        assertEquals(0, w.kAk, "ak = 0")
        assertEquals(0, w.kX, "X = 0")
        assertEquals(0, w.kQ, "Q = 0")
        assertTrue(w.kO != 500 && w.kP != 700, "m(ad) re-snap overwrites O/P")
        assertFalse(w.kZ); assertFalse(w.kAb, "Z=false; ab=false (:1856-57)")
    }

    @Test fun `jc9 exit bh3 arm runs the flying-camera init`() {
        val w = world()
        w.kAj = 1                                     // bh[1]==3 — flying
        driveJc9Exit(w)
        assertEquals(0, w.kDU); assertEquals(-1, w.kDR)
        assertEquals(0, w.kAk); assertEquals(-1, w.kAR)
        assertEquals(-2, w.kDS, "dS = (-1)-1 (:1867)")
        assertEquals(w.kBu, w.kDT, "dT = (bu-20)-(20*-1) = bu")
        assertEquals(230, w.kQ, "Q = 230 (:1869)")
        assertEquals(w.player.ak - 200, w.kO, "cA = O = aS.ak-200")
        assertEquals(w.player.al - 230, w.kP, "cB = P = aS.al-230")
        assertEquals(0, w.kW); assertEquals(-7, w.kX); assertEquals(-7, w.kV)
    }

    @Test fun `jc9 exit spawns the T() HUD indicator entity`() {
        val w = world()
        driveJc9Exit(w)
        val d = w.kDA
        assertNotNull(d, "dA = new i() (:4167)")
        assertEquals(0, d!!.S, "dA.i(0)")
        assertEquals(0, d.ak); assertEquals(0, d.al)
        assertEquals(0, w.kAA, "aA = 0 (:4172)")
    }

    @Test fun `jc9 exit F-aj arms held mask gJ-5 and rebuilds equip`() {
        val w = world()
        w.kAj = 1                                     // aj>0 → i.j(1) arm
        w.player.gJ = 255; w.player.gI = 8
        driveJc9Exit(w)
        assertEquals(1, w.player.gI, "g.I = 1 (:3556)")
        assertEquals(5, w.player.gJ, "g.J=0 then g.g(f0do[aj]=5) → J|=5")
        assertEquals(1, w.equipList[0],
            "q() clusters bit0 (mask-1) — bit2's mask-4 is skipped")
        assertEquals(1, Entity.entBSLatch, "aj>0 → i.j(1) → bS|=1")
    }

    @Test fun `ef-aj is all-false — trailAb never arms`() {
        val w = world()
        assertTrue(w.kEfArr.all { !it }, "ef[] all-false (:264)")
        driveJc9Exit(w)
        assertEquals(8, w.jC, "exit completed with no trail setup")
    }

    @Test fun `kAx and save-byte defaults are the proven 30`() {
        val w = world()
        assertEquals(30, w.kAx, "byte ax = 30 (:223)")
        assertEquals(30, w.kDB, "byte dB = 30 (:225)")
        assertEquals(30, w.kDC, "byte dC = 30 (:226)")
    }
}
