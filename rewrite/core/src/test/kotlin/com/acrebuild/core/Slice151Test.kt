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

/** Slice 151 — player tail mount/vehicle helpers: `g.o()` verbatim
 *  (groundOrVehicle — ax43 exclusion), `g.d()`/`g.b(i)` dead-documented,
 *  `g.cm` latch pair (L37eb set + L37f2 drain) and the L37b6/L37c1
 *  mounted arm (U()+a(8,ak,al-85)+ae pin). */
class Slice151Test {

    class S151World(cell: Int = 0) : Slice139Test.ClaimZoneWorld(cell)

    @Test fun `groundOrVehicle gates by o verbatim`() {
        // g.java:6090 — aZ → true; a==null||ax43 → false; ax51/15/43 else
        val p = Entity(0, null)
        p.aZ = true
        assertTrue(p.groundOrVehicle())
        p.aZ = false
        assertFalse(p.groundOrVehicle())                       // a == null
        p.standingOn = Entity(43, null)
        assertFalse(p.groundOrVehicle())                       // ax43 → false
        p.standingOn = Entity(51, null)
        assertTrue(p.groundOrVehicle())                        // ax51 → true
        p.standingOn = Entity(15, null)
        assertTrue(p.groundOrVehicle())                        // ax15 → true
        p.standingOn = Entity(11, null)
        assertFalse(p.groundOrVehicle())                       // other → false
    }

    @Test fun `gcm quiet tick mounted releases ae`() {
        // L37f2 — r9==0 + g.cm → k.k()=mounted → G() release
        val w = S151World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 0; p.gJ = 4
        p.gcm = true
        p.ae = Entity(14, null).also { it.S = 0; it.av = false }
        Entity.L = 5; Entity.M = 6
        w.mountMode = true                                     // k.k() true
        fsm.mountEntry(p, Pad())
        assertNull(p.ae, "mounted → releaseAe drops ae")
        assertFalse(p.gcm, "latch drained")
        assertEquals(5, Entity.L, "G() does not touch L/M")
        Entity.L = -1; Entity.M = -1                           // statics: do not leak into later tests
    }

    @Test fun `gcm quiet tick unmounted drops indicator`() {
        // L37f2 — !k.k() → U() = T()→G() + L/M=-1
        val w = S151World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 0; p.gJ = 4
        p.gcm = true
        p.ae = Entity(14, null).also { it.S = 0; it.av = false }
        Entity.L = 5; Entity.M = 6
        w.mountMode = false
        fsm.mountEntry(p, Pad())
        assertNull(p.ae)
        assertFalse(p.gcm)
        assertEquals(-1, Entity.L, "U() clears the L/M point")
        assertEquals(-1, Entity.M)
    }

    @Test fun `gcm latch drains when mount request absent`() {
        // Slice 365 (g.javap.txt e() 13741-13755 → 14204 → 14322, proven):
        // J&4 == 0 skips only the scan (`goto 14204`); r98 stays 0, so the
        // L37f2 arm still drains `g.cm` and refreshes the indicator. The
        // slice-151 reading (return before the consumer, latch held) was
        // a misread of the jump target.
        val w = S151World(cell = 12)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 0; p.gJ = 0
        p.gcm = true
        fsm.mountEntry(p, Pad())
        assertFalse(p.gcm, "J&4==0 → goto 14204 → cm drained at 14322")
    }
}
