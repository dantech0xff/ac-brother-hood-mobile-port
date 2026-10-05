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

/** Slice 152 — bA[] checkpoint serializer completion: writeIX stamps
 *  bA[28..50]/[52+aj*2]/[68]/[79]; resetLevel restores gJ/gI/kAp +
 *  mission globals + aZ/bn from the snapshot (k.a(z2) arm). */
class Slice152Test {

    @Test fun `checkpoint write stamps bA bytes`() {
        // i.java:13488-13500 — the full serializer arm
        val w = world()
        val cp = w.checkpoints.first()
        w.kAx = 77; w.kAy = 12; w.kAz = 9; w.kAN = 3; w.kAL = 44
        w.kAj = 2; w.kAp[5] = 91
        w.kAZ = true; w.iBn = true
        overlapCheckpoint(w, cp)
        repeat(2) { w.tick(emptyList()) }
        // bytes are stamped from live globals at write time — compare to
        // the snapshot the checkpoint captured
        val s = w.checkpointSnap!!
        assertEquals(cp.aw, w.kBA[16], "bA[16] = checkpoint record id")
        assertEquals(0, w.kBA[15], "aY() must not arm the has-save flag")
        // bA[18]/[20] = the PLAYER's ak/al at write time (i.java:18634).
        // The overlap spot is airborne → l()→aw()→a(0) = enterFall drops
        // him during the post-write ticks, so compare the stamp to the
        // snapshot's own write-time position, not the live one.
        assertEquals(s.ak, w.kBA[18]); assertEquals(s.al, w.kBA[20])
        assertEquals(s.gJ, w.kBA[24]); assertEquals(s.gI, w.kBA[26])
        assertEquals(s.kAx, w.kBA[28]); assertEquals(s.kAy, w.kBA[30])
        assertEquals(s.kAz, w.kBA[32]); assertEquals(s.kAN, w.kBA[34])
        assertEquals(s.ap[0], w.kBA[36]); assertEquals(s.ap[3], w.kBA[38])
        assertEquals(s.ap[2] / 16, w.kBA[40]); assertEquals(s.ap[4], w.kBA[42])
        assertEquals(44, w.kBA[50])
        assertEquals(s.ap[5], w.kBA[52 + (2 shl 1)])
        assertEquals(1, w.kBA[68]); assertEquals(1, w.kBA[79])
        for (i in 0..2) assertEquals(1, w.kBA[76 + i], "i.br[i] pending=1")
    }

    @Test fun `reload restores snapshot globals and flags`() {
        // k.java:5185-5203 — the k.a(z2) restore arm on resetLevel
        val w = world()
        val cp = w.checkpoints.first()
        overlapCheckpoint(w, cp)
        w.player.gJ = 9; w.player.gI = 2; w.kAp[3] = 8
        w.kAx = 55; w.kAz = 4; w.kAZ = true; w.iBn = true
        repeat(2) { w.tick(emptyList()) }
        // ticks mutate globals before the write — compare restore vs the
        // snapshot the checkpoint actually captured
        val s = w.checkpointSnap!!
        w.player.gJ = 0; w.player.gI = 0; w.kAp[3] = 0
        w.kAx = 0; w.kAz = 0; w.kAZ = false; w.iBn = false
        w.resetLevel(true)
        assertEquals(s.gJ, w.player.gJ); assertEquals(s.gI, w.player.gI)
        assertEquals(8, w.kAp[3])
        assertEquals(s.kAx, w.kAx); assertEquals(s.kAz, w.kAz)
        assertTrue(w.kAZ); assertTrue(w.iBn)
    }

    @Test fun `checkpoint bA flags reflect write-time state`() {
        // aZ=false / bn=false stamp zeros — no carry-over
        val w = world()
        val cp = w.checkpoints.first()
        w.kAZ = false; w.iBn = false
        overlapCheckpoint(w, cp)
        repeat(2) { w.tick(emptyList()) }
        assertEquals(0, w.kBA[68]); assertEquals(0, w.kBA[79])
    }
}
