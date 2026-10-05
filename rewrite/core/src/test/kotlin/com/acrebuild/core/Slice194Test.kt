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

class Slice194Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S86 corpse handoff enters S326 on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 86
        fsm.tick(p, Pad())
        assertEquals(326, p.S, "r() → i(326) — L297b")
    }

    @Test fun `S89 h1 consumes pending J grab and sleeps the body`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 89
        p.gJ = 1                               // J&1 grab request pending
        p.ai = 7; p.aj = 7; p.ag = 512; p.ah = -512
        fsm.tick(p, Pad())
        assertEquals(0, p.ai); assertEquals(0, p.aj)
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertTrue(p.P and 64 != 0, "r() → P|=64 body sleep — L25eb")
        assertEquals(1, p.gI, "h(1) folds J&1 into g.I — g.java:12278")
        assertEquals(1, w.actionLock, "h(1) forces k.at=1")
    }

    @Test fun `S89 without J bit still settles and sleeps`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 89
        p.gJ = 0; p.gI = 4
        p.ag = 512
        fsm.tick(p, Pad())
        assertEquals(0, p.ag)
        assertTrue(p.P and 64 != 0, "r() → P|=64")
        assertEquals(4, p.gI, "J&1 absent → g.I untouched")
        assertEquals(0, w.actionLock, "k.at untouched")
    }

    @Test fun `S110 pinned settle sleeps on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 110
        fsm.tick(p, Pad())
        assertTrue(p.P and 64 != 0, "r() → P|=64 — L2de0")
        assertEquals(110, p.S, "no state change")
    }

    @Test fun `S165 launch runs capped gravity then flings airborne`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 165
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "r() → a(0) masked fling → S43 — L2df5")
        assertEquals(110, p.al, "a(0) adds al+=10")
        assertEquals(1536, p.aj, "aj=1536 set by the arm")
        assertEquals(0, p.ah, "a(0) zeroes ah")
    }
}
