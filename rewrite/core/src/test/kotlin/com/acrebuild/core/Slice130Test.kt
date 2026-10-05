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

// ---- Slice 130: S375-377 ax61 aura knockback slide + i.f tails ----
class Slice130Test {

    @Test fun `S375 slides ±1280 with the scroll-wall tail g4245`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 375; p.T = 0; p.av = false
        fsm.tick(p, Pad())
        // raw bytes @2164-2182 (slice 416): `ag = 1280; if (!av) ag = -1280`
        assertEquals(-1280, p.ag, "S375 skid -1280 facing right (the boss is to the right: away from it)")
        assertTrue(p in w.clampCalls, "i.f(this) tail (g.java:4253)")
    }

    @Test fun `S375 left-facing skids +1280`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 375; p.T = 0; p.av = true
        fsm.tick(p, Pad())
        assertEquals(1280, p.ag, "av = left-facing (toward the boss) → pushed right, away from it")
    }

    @Test fun `S375-377 chain skids to halt then falls g4245`() {
        val w = Slice128Test.MarkerWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 375
        // drive animFinished by emptying the current anim's tick count:
        // Entity.animFinished checks T against clip frame count — a null
        // clip entity reports finished, so the chain advances per tick.
        p.S = 376
        fsm.tick(p, Pad())
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertTrue(p in w.clampCalls)
        p.S = 377
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "S377 r() → a(0) enterFall (g.java:4306)")
    }
}
