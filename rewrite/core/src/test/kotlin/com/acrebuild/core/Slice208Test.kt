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

class Slice208Test {

    // -- g.a unification: `standingOn` delegates to `ga` (one J2ME field) --

    @Test fun `standingOn and ga share the one link slot`() {
        val p = Entity(0, null)
        val crate = Entity(51, null)
        p.standingOn = crate
        assertSame(crate, p.ga, "support reads bind the same g.a field")
        p.ga = null
        assertNull(p.standingOn, "clears land on the same slot")
        p.ga = Entity(43, null)
        assertSame(p.ga, p.standingOn, "ride binds are visible to support reads")
    }

    @Test fun `o gate sees a crate bound through ga`() {
        val p = Entity(0, null)
        assertFalse(p.groundOrVehicle(), "no ground, no link")
        p.ga = Entity(51, null)                      // g.a = crate
        assertTrue(p.groundOrVehicle(), "g.o() reads a.ax==51")
        p.ga = Entity(43, null)
        assertFalse(p.groundOrVehicle(), "ax43 carrier excluded verbatim")
    }

    @Test fun `enterFall clears the link once`() {
        val w = world(); val p = w.player
        p.ga = Entity(66, null)
        p.enterFall(0, w)
        assertNull(p.ga, "g.a = 0 on the fall arm")
        assertNull(p.standingOn)
    }
}

// ============================================================================
// Slice 209 — equip-cycle verbatim: `k.q()` at-reset + `k.p(I)` bit index
// ============================================================================
