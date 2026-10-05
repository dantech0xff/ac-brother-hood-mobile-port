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

// ============================================================================
// Slice 70 — k.m(int) camera tracker (replaces the placeholder follow)
// ============================================================================
class Slice70CamTest {

    @Test fun `init m(ad) snaps camera onto the player`() {
        val w = world()
        // init{} ran kM(kAd): ae=aS, snapped centered
        // camX = ak-200 clamped to [0, worldW-400] by the L362 floor
        assertEquals((w.player.ak - 200).coerceIn(0, w.level.worldW - 400), w.camX)
        assertTrue(w.kAe === w.player)
        assertEquals(0, w.kR); assertEquals(0, w.kSBound)   // snap cleared walls
    }

    @Test fun `per-tick m(1) lerps camera toward the target`() {
        val w = world()
        val startX = w.camX
        // teleport far right, keep tracking per-tick
        w.player.setPositionPx(startX + 300, w.player.al)
        repeat(3) { w.tick(emptyList()) }
        val d = w.player.ak - 200 - w.camX
        assertTrue(kotlin.math.abs(d) < 300 - startX)      // caught up partially
        assertTrue(w.camX > startX)                        // moved forward
    }

    @Test fun `look-ahead margin tracks run direction`() {
        val w = world()
        val p = w.player
        // run right: !av && ag>0 → cM decays toward 133 → camera leads right
        p.av = false; p.ag = 2560; p.S = 99               // non-normal state so margin applies
        w.kM(1)
        assertEquals(180, w.javaClass.getDeclaredField("camM").let { it.isAccessible = true; it.get(w) as Int })
        // run left: av && ag<0 → cM grows toward 266
        p.av = true; p.ag = -2560
        w.kM(1)
        assertEquals(200, w.javaClass.getDeclaredField("camM").let { it.isAccessible = true; it.get(w) as Int })
    }

    @Test fun `combo anim freezes the camera (aS_c)`() {
        val w = world()
        val frozenX = w.camX
        w.player.setPositionPx(w.player.ak + 500, w.player.al)
        w.player.S = 112                                  // combo anim → aS.c()
        w.kM(1)
        assertEquals(frozenX, w.camX)                     // early return, no move
    }

    @Test fun `kAi latch freezes tracking entirely`() {
        val w = world()
        val frozenX = w.camX
        w.kAi = true
        w.player.setPositionPx(w.player.ak + 500, w.player.al)
        w.kM(1)
        assertEquals(frozenX, w.camX)
    }

    @Test fun `scroll wall clamps the target inside ah_W`() {
        val w = world()
        val wall = Entity(37, null).apply {
            // wall W >= 400 wide (narrower walls can't satisfy both clamps)
            W[0] = w.camX + 50; W[1] = 0; W[2] = w.camX + 550; W[3] = 240
            aF = 1
        }
        w.kAh = wall
        w.player.setPositionPx(w.player.ak + 800, w.player.al)
        w.kM(1)
        val a = w.javaClass.getDeclaredField("camA").let { it.isAccessible = true; it.get(w) as Int }
        assertTrue(a >= wall.W[0] && a + 400 <= wall.W[2] + 1)
    }

    @Test fun `airborne unlisted state keeps cB sticky`() {
        val w = world()
        val p = w.player
        // prime a known camB via grounded state
        p.S = 0; w.kM(1)
        val bField = w.javaClass.getDeclaredField("camB").let { it.isAccessible = true; it.get(w) as Int }
        // airborne with a state not in any list → camB must NOT change
        p.aZ = false; p.ga = null; p.S = 43               // falling, unlisted
        w.kM(1)
        val bField2 = w.javaClass.getDeclaredField("camB").let { it.isAccessible = true; it.get(w) as Int }
        assertEquals(bField, bField2)
    }

    @Test fun `m(ad) inside tick clears walls and snaps`() {
        val w = world()
        w.kR = 4000; w.kSBound = 9000
        val ae = Entity(44, null).apply { setPositionPx(5000, 300); refreshBoxes() }
        w.kAe = ae                                       // focus elsewhere
        w.kM(2)                                          // r5&ad arm
        assertTrue(w.kAe === w.player)                   // ae=aS restored
        assertEquals(0, w.kR); assertEquals(0, w.kSBound)
    }
}
