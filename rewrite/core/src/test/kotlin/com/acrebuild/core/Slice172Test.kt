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

class Slice172Test {
    // The real level-0 mission-complete path (proven from pack-6 data +
    // the ported ax5/claim-script chain): S8 watcher aw252 sits at the
    // level end with W=[12131,221,12561,811] and aG=116. Player overlap
    // -> missionResolve -> bindContext -> script uid 116 -> its single
    // op `op37(1)` (r013=1, r12=1) -> `screenL(15)` + `kStat(0)`.
    // Verified: the zone fires on player entry and consumes itself.

    @Test fun `aw252 watcher spawns with its record fields`() {
        val w = world()
        w.stateL(8)
        val zone = w.npcs.firstOrNull { it.aw == 252 }
        assertTrue(zone != null)
        assertEquals(8, zone.S)
        assertEquals(116, zone.aG)
        assertEquals(listOf(12131, 221, 12561, 811), zone.W.toList())
    }

    @Test fun `player entering the end zone fires mission-complete screen`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 60) w.tick(listOf())
        val p = w.player
        val zone = w.npcs.firstOrNull { it.aw == 252 }!!
        keepLive(zone)
        p.ak = 12300; p.al = 500
        p.N = p.ak shl 8; p.O = p.al shl 8
        p.ag = 0; p.ah = 0
        p.refreshBoxes()                      // entities tick first (G12)
        w.tick(listOf())
        assertEquals(15, w.jC)                // screenL(15) fired
        assertFalse(w.npcs.contains(zone))    // zone consumed itself
    }

    @Test fun `zone does not fire while player outside its box`() {
        val w = world()
        w.stateL(8)
        for (t in 0 until 120) w.tick(listOf())
        val zone = w.npcs.firstOrNull { it.aw == 252 }!!
        assertNotEquals(15, w.jC)
        assertTrue(w.npcs.contains(zone))
    }
}
