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

// ---------------------------------------------------------------------------
// slice 83 — ag()/ah() poster card + medal viewer (k.java:6358/:6392)
// ---------------------------------------------------------------------------
class Slice83Test {

    @Test fun `jC==10 poster fields and confirm navigates to stats`() {
        val w = world()
        w.kAj = 0                                   // aj+1=1 ∉ fP → i=4 → frame 8
        w.screenL(10)
        w.tick(emptyList())
        assertTrue(w.posterVisible)
        assertEquals(8, w.posterFrame)
        assertEquals(120, w.cardOverlayY)
        assertEquals(8, w.dlgU)
        w.pad.queuePress(327712); w.tick(emptyList())
        assertEquals(15, w.jC)
    }

    @Test fun `poster frame uses the fP index for flying missions`() {
        val w = world()
        w.kAj = 4                                   // aj+1=5 → fP[2] → frame 6
        w.screenL(10)
        w.tick(emptyList())
        assertEquals(6, w.posterFrame)
    }

    @Test fun `medal screen compacts cc==1 rows with fade-in`() {
        val w = world()
        w.kCc[0] = 1; w.kCc[1] = 0; w.kCc[2] = 1
        w.kEx = 0
        w.screenL(22)
        w.tick(emptyList())                          // jG=1 <10
        assertTrue(w.medalVisible)
        assertEquals(2, w.medalRowCount)
        assertEquals(0, w.medalRowIcon[0])
        assertEquals(2, w.medalRowIcon[1])
        assertTrue(w.screenFadeAlpha > 0)
    }

    @Test fun `confirm marks cc 1 to 2 persists bA130 and navigates`() {
        val w = world()
        w.kAj = 0                                   // not in fP → l(15)
        w.kCc[0] = 1
        w.screenL(22)
        repeat(10) { w.tick(emptyList()) }           // jG>=10
        w.pad.queuePress(327712); w.tick(emptyList())
        assertEquals(2, w.kCc[0])
        assertEquals(2, w.kBA[130])
        assertEquals(15, w.jC)
    }

    @Test fun `confirm on poster-mission routes to l10`() {
        val w = world()
        w.kAj = 1                                   // aj+1=2 ∈ fP, bA[15]=0
        w.kCc[2] = 1
        w.screenL(22)
        repeat(10) { w.tick(emptyList()) }
        w.pad.queuePress(327712); w.tick(emptyList())
        assertEquals(10, w.jC)
    }

    @Test fun `ex==3 shows three fixed rows and back exits`() {
        val w = world()
        w.kEx = 3
        w.kCc[0] = 2; w.kCc[1] = 0; w.kCc[2] = 2
        w.screenL(22)
        w.tick(emptyList())
        assertEquals(3, w.medalRowCount)
        assertEquals(0, w.medalRowIcon[0])
        assertEquals(3, w.medalRowIcon[1])           // locked → frame 3
        assertEquals(2, w.medalRowIcon[2])
        assertTrue(w.medalRowDim[1])
        assertTrue(w.hintBack)
        w.pad.queuePress(131072); w.tick(emptyList())
        assertEquals(3, w.jC)
    }
}
