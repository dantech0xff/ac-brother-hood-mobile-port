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
// slice 82 — e(z2) /ASBR record persistence (k.java:5557)
// ---------------------------------------------------------------------------
class Slice82Test {

    @Test fun `saveFlush emits kBA as little-endian shorts`() {
        val w = world()
        w.kBA[8] = 0x123
        w.kBA[14] = 0x56
        w.saveFlush()
        val rec = (w.drainCommands()
            .filterIsInstance<com.acrebuild.core.Command.PersistBA>()
            .single()).record
        assertEquals(w.kBA.size * 2, rec.size)
        assertEquals(0x23, rec[16].toInt() and 0xFF)
        assertEquals(0x01, rec[17].toInt() and 0xFF)
        assertEquals(0x56, rec[28].toInt() and 0xFF)
        assertEquals(0x00, rec[29].toInt() and 0xFF)
        assertTrue(w.hasSaveRecord)
    }

    @Test fun `saveLoad round-trips kBA through the record`() {
        val w = world(); val w2 = world()
        w.kBA[8] = 2; w.kBA[14] = 3; w.kBA[44] = 777; w.kBA[10] = 1
        w.saveFlush()
        val rec = (w.drainCommands()
            .filterIsInstance<com.acrebuild.core.Command.PersistBA>()
            .single()).record
        w2.kBA[8] = 99
        w2.saveLoad(rec)
        assertEquals(3, w2.kBA[14])
        assertEquals(777, w2.kBA[44])
        assertTrue(w2.kEJ, "eJ = bA[10]!=0")
        assertTrue(w2.hasSaveRecord)
    }

    @Test fun `boot arm clamps au modulo 3 and au2 without bA69`() {
        val w = world()
        w.kBA[8] = 5                                   // 5%3 = 2
        w.kBA[69] = 0
        w.saveFlush()
        val rec = (w.drainCommands()
            .filterIsInstance<com.acrebuild.core.Command.PersistBA>()
            .single()).record
        val w2 = world()
        w2.saveLoad(rec)
        assertEquals(0, w2.kAu, "au==2 && bA[69]==0 → 0")
        w.kBA[69] = 1; w.saveFlush()
        val rec2 = (w.drainCommands()
            .filterIsInstance<com.acrebuild.core.Command.PersistBA>()
            .single()).record
        w2.saveLoad(rec2)
        assertEquals(2, w2.kAu)
    }

    @Test fun `e(false) nop-on-empty keeps defaults`() {
        val w = world()
        w.kAu = 1; w.kBA[14] = 4
        w.saveLoad(null)
        w.saveLoad(ByteArray(1))
        assertEquals(1, w.kAu)
        assertEquals(4, w.kBA[14])
        assertFalse(w.hasSaveRecord)
    }
}
