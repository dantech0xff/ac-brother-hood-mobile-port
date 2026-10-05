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

class Slice147Test {
    private fun zone(w: Slice139Test.ClaimZoneWorld): Entity {
        val z = Entity(10, null); z.S = 17
        z.W[0] = 90; z.W[2] = 130; z.W[1] = 80; z.W[3] = 120
        w.player.ak = 110; w.player.al = 100
        w.player.W[0] = 104; w.player.W[2] = 116
        w.player.W[1] = 84; w.player.W[3] = 100
        return z
    }

    @Test fun `S17 overlap center-pins player and plays S297`() {
        val w = Slice139Test.ClaimZoneWorld(12)
        val z = zone(w)
        NpcFsm(w).tickTrigger(z, w, w.player, Pad())
        assertEquals(1, z.aA)
        assertEquals(297, w.player.S)
        assertEquals(110, w.player.ak)                    // (90+130)/2
        assertEquals(100, w.player.al)                    // (80+120)/2
        assertEquals(0, w.player.ag); assertEquals(0, w.player.ah)
    }

    @Test fun `S17 v2 leaps left and v8 leaps right`() {
        val w = Slice139Test.ClaimZoneWorld(12)
        val z = zone(w); val pad = Pad()
        NpcFsm(w).tickTrigger(z, w, w.player, pad)          // → aA=1
        pad.bB = 2
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(19, w.player.S); assertEquals(-3328, w.player.ag)
        assertEquals(-3840, w.player.ah); assertTrue(w.player.av)
        assertEquals(2, z.aA)
        // second zone: v(8)
        val w2 = Slice139Test.ClaimZoneWorld(12)
        val z2 = zone(w2); val pad2 = Pad()
        NpcFsm(w2).tickTrigger(z2, w2, w2.player, pad2)
        pad2.bB = 8
        NpcFsm(w2).tickTrigger(z2, w2, w2.player, pad2)
        assertEquals(3328, w2.player.ag); assertFalse(w2.player.av)
    }

    @Test fun `S17 v33024 jump-up flings and leap skips S298 S293`() {
        val w = Slice139Test.ClaimZoneWorld(12)
        val z = zone(w); val pad = Pad()
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        pad.bB = 33024
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(2, z.aA)
        assertEquals(2560, w.player.ah)                    // a(2560) fling
        // S298 blocks the action block entirely
        val w2 = Slice139Test.ClaimZoneWorld(12)
        val z2 = zone(w2); val pad2 = Pad()
        NpcFsm(w2).tickTrigger(z2, w2, w2.player, pad2)
        w2.player.setAnim(298)
        pad2.bB = 2
        NpcFsm(w2).tickTrigger(z2, w2, w2.player, pad2)
        assertEquals(1, z2.aA)                             // still centered
        assertEquals(298, w2.player.S)
    }

    @Test fun `S17 leaving rect to aA2 then aZ resets`() {
        val w = Slice139Test.ClaimZoneWorld(12)
        val z = zone(w); val pad = Pad()
        NpcFsm(w).tickTrigger(z, w, w.player, pad)          // → aA=1
        w.player.W[0] = -100; w.player.W[2] = -50           // moved out
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(2, z.aA)
        w.player.aZ = true                                // landed
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(0, z.aA)
    }

    @Test fun `S17 claim busy freezes while centered`() {
        val w = Slice139Test.ClaimZoneWorld(12)
        val z = zone(w); val pad = Pad()
        NpcFsm(w).tickTrigger(z, w, w.player, pad)          // → aA=1
        w.kC = Entity(5, null).apply { ca = 0; scriptStep = 0 }
        w.player.W[0] = -100; w.player.W[2] = -50
        pad.bB = 2
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(1, z.aA)                             // frozen
        w.kC = null
        NpcFsm(w).tickTrigger(z, w, w.player, pad)
        assertEquals(2, z.aA)                             // unfrozen
    }
}
