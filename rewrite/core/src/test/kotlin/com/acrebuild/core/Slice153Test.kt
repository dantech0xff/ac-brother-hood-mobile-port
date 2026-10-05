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

/** Slice 153/155 — i.D() static-reset tail (i.java:1795-1865): entity-system
 *  reset clears the modeled k./i. statics AND runs before the k.a(z2)
 *  checkpoint restore so D() can't clobber restored values. */
class Slice153Test {

    @Test fun `entity reset clears k statics before restore`() {
        val w = world()
        val cp = w.checkpoints.first()
        overlapCheckpoint(w, cp)
        w.camAf = 40; w.camAg = -20; w.kAZ = true; w.iBn = true
        w.kAE = 5; w.kAF = 9; w.kAH = 3; w.iAJ = 7
        w.kAD = w.npcs.firstOrNull(); w.kAi = true
        repeat(2) { w.tick(emptyList()) }
        assertNotNull(w.checkpointSnap)
        // D() zeroes the statics; the snapshot restore then re-applies
        // only what the checkpoint captured (aZ/bn/global values).
        w.resetLevel(true)
        assertEquals(0, w.camAf); assertEquals(0, w.camAg)
        assertEquals(100, w.kAE); assertEquals(0, w.kAF)
        assertEquals(-1, w.kAH); assertEquals(0, w.iAJ)
        assertNull(w.kAD); assertFalse(w.kAi)
        assertTrue(w.iZ)
        // aZ/bn were TRUE at checkpoint time → restored, not cleared
        assertTrue(w.kAZ); assertTrue(w.iBn)
    }

    @Test fun `fresh load clears aZ and bn`() {
        // no checkpoint → snapshot restore absent → D() values stand
        val w = world()
        w.kAZ = true; w.iBn = true
        w.resetLevel(true)
        assertFalse(w.kAZ); assertFalse(w.iBn)
        assertEquals(0, w.camAf)
        assertEquals(100, w.kAE)
    }
}
