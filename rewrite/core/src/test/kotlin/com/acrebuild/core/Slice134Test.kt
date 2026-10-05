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

// ---- Slice 134: g.j grab latch + default e() arm + S146/147 wall sequence ----
class Slice134Test {

    /** MarkerWorld + the k.bG/k.bH/k.bA/k.az/k.x()/k.a(z2) members the
     *  S147 arm drives (g.java:2898-2901). */
    class PassWorld(cell: Int = 3,
                    cellFn: ((Int, Int) -> Int)? = null) :
        Slice128Test.MarkerWorld(cell, cellFn) {
        override var kBg = -1
        override var kBH = 7
        override var kAz = 0
        override val kBA = IntArray(160).also { it[32] = 42 }
        var musicOn = true
        var resetFull = -1
        val sfxCalls = mutableListOf<Int>()
        override fun musicActive() = musicOn
        override fun resetLevel(full: Boolean) { resetFull = if (full) 1 else 0 }
        override fun sfx(id: Int) { sfxCalls += id }
    }

    private fun playerAt(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al; p.av = av
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `default arm airborne held dir with no support flings a(0) g1146`() {
        // !l() only fires when l() leaves the state untouched — for a
        // non-79 state that's `ax()`'s `!aZ && standingOn==null` return:
        // a direction held while airborne with nothing underfoot.
        Entity.grabLatch = false
        val w = PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 200                              // a default-family state
        p.aZ = false; p.standingOn = null
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertEquals(43, p.S, "r() && !j && !l() → a(0) = flingAirborne")
        assertEquals(110, p.al, "al += 10 (g.a(int))")
        assertEquals(0, p.ah)
        assertEquals(1536, p.aj)
    }

    @Test fun `default arm grounded no-input settles via aw() g1146`() {
        // l() L124 (proven): non-11 + no dir → aw(); grounded → the
        // settle tail (tick100 → i(1)). l() returns true so the tail's
        // own a(0) stays suppressed — faithful to the original ordering.
        Entity.grabLatch = false
        val w = PassWorld(cellFn = { _, cy -> if (cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 200
        fsm.tick(p, Pad())
        assertEquals(1, p.S, "l() → aw() settle → i(1) idle")
    }

    @Test fun `default arm respects the grab latch g1146`() {
        Entity.grabLatch = true                // S146/147 claimed it
        val w = PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 200; p.aZ = false; p.standingOn = null
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertEquals(200, p.S, "!j gate — no fling while latched")
        Entity.grabLatch = false
    }

    @Test fun `S146 drives then steps down into S147 with latch g2859`() {
        Entity.grabLatch = false
        val w = PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 146                              // clip null → r() fires
        fsm.tick(p, Pad())
        assertEquals(2048, p.ag, "ag = av?-2048:2048")
        assertEquals(120, p.al, "al += 20 on the handoff")
        assertEquals(147, p.S)
        assertTrue(Entity.grabLatch, "g.j = true (:2865)")
        Entity.grabLatch = false
    }

    @Test fun `S146 passes when the facing strip leaves the wall g2862`() {
        Entity.grabLatch = false
        val w = PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100, av = true)
        p.S = 146; p.aT = 12                   // av && aT != 3 → handoff
        fsm.tick(p, Pad())
        assertEquals(-2048, p.ag)
        assertEquals(147, p.S)
        assertTrue(Entity.grabLatch)
        Entity.grabLatch = false
    }

    @Test fun `S147 latches and runs the transition reset g2885`() {
        Entity.grabLatch = false
        val w = PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 147
        fsm.tick(p, Pad())
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, p.ai); assertEquals(0, p.aj)
        assertTrue(Entity.grabLatch, "g.j = true (:2898)")
        assertEquals(7, w.kBg, "k.bG = k.bH while a track plays")
        assertTrue(18 in w.sfxCalls, "k.A(18) sfx fired")
        assertEquals(1, w.resetFull, "k.a(true) level reset ran")
        assertEquals(42, w.kAz, "k.az = bA[32] short read restored")
        Entity.grabLatch = false
    }

    @Test fun `S147 clears the music slot when nothing plays g2899`() {
        Entity.grabLatch = false
        val w = PassWorld(cell = 0)
        w.musicOn = false
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 147
        fsm.tick(p, Pad())
        assertEquals(-1, w.kBg, "k.bG = -1 when e.a() is false")
        Entity.grabLatch = false
    }
}
