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

class Slice159Test {

    private fun launchTick(p: Entity, w: Level0World, gp: Int): Entity {
        w.gP = gp
        p.setAnim(90)
        p.ae = Entity(14, null)
        w.playerFsm.tick(p, w.pad)
        return p
    }

    @Test fun `S90 launch arg one flings right g2647`() {
        val w = world()
        val p = launchTick(w.player, w, 1)
        assertEquals(4096, p.ag); assertFalse(p.av)
        assertEquals(-768, p.ah); assertEquals(157, p.S)
        assertEquals(0, w.gP); assertTrue(p.gD); assertNull(p.ae)
    }

    @Test fun `S90 launch arg two flings left g2651`() {
        val w = world()
        val p = launchTick(w.player, w, 2)
        assertEquals(-4096, p.ag); assertTrue(p.av)
        assertEquals(-768, p.ah); assertEquals(157, p.S)
        assertEquals(0, w.gP); assertTrue(p.gD)
    }

    @Test fun `S90 anim end near edge settles i0 g2658`() {
        val w = world()
        w.gP = 0
        w.player.setAnim(90)
        w.player.clip = null               // r() -> animFinished
        standOn(w, w.player)               // real ground → aR≥20 after rescan
        w.playerFsm.tick(w.player, w.pad)
        assertEquals(0, w.player.S); assertTrue(w.player.gD)
    }

    @Test fun `S90 anim end mid cell flings a0 g2660`() {
        // airborne cells — the head rescan leaves aR/aS at 0 → a(0) fling
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 90
        fsm.tick(p, Pad())
        assertEquals(43, p.S); assertEquals(1536, p.aj)
    }

    @Test fun `S12 entry clears gD g1316`() {
        val w = world()
        w.player.gD = true
        w.player.setAnim(12)
        w.player.ag = 0
        w.playerFsm.tick(w.player, w.pad)
        assertFalse(w.player.gD)
    }
}
