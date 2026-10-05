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
// Slice 68 — ax34 ak() player-follower overlay (k.D companion)
// ============================================================================
class Slice68Test {

    @Test fun `player init spawns kD and kE companions once`() {
        val w = world()
        val d = w.kD
        assertNotNull(d)
        assertEquals(34, d!!.ax)
        assertTrue(d.P and 16 != 0 && d.P and 512 != 0)   // P|=16|512
        assertEquals(w.player.ak, d.ak)
        assertEquals(w.player.al, d.al)
        assertTrue(w.npcs.any { it === d })               // k.b → live pool
        val e71 = w.kE
        assertNotNull(e71)
        assertEquals(71, e71!!.ax)
        assertEquals(300, e71.az)
        assertTrue(e71.P and 128 != 0)                    // spawned hidden
    }

    private fun follower(w: Level0World): Entity {
        val d = w.kD ?: Entity(34, null).also { w.kD = it }
        return d
    }

    @Test fun `ak() snaps to player and shows when grounded`() {
        val w = world()
        val p = w.player
        p.aZ = true                                        // standing
        p.ga = null
        p.setAnim(0)
        w.iZ = true
        val d = follower(w)
        w.npcFsm.tickAx34(d, w, p)
        assertEquals(p.ak, d.ak)
        assertEquals(p.al + 1, d.al)                       // L13 standing arm
        assertEquals(99, d.az)
        assertTrue(d.P and 128 == 0)                       // visible
    }

    @Test fun `ak() hides when player anim is in the hide-list`() {
        val w = world()
        val p = w.player
        p.aZ = true; p.ga = null; w.iZ = true
        val d = follower(w)
        for (s in intArrayOf(268, 183, 43, 105, 310)) {
            p.setAnim(s)
            w.npcFsm.tickAx34(d, w, p)
            assertTrue(d.P and 128 != 0, "S=$s should hide")
        }
        p.setAnim(0)
        w.npcFsm.tickAx34(d, w, p)
        assertTrue(d.P and 128 == 0)
    }

    @Test fun `ak() hides when iZ is false`() {
        val w = world()
        val p = w.player
        p.aZ = true; p.ga = null; p.setAnim(0)
        w.iZ = false
        val d = follower(w)
        w.npcFsm.tickAx34(d, w, p)
        assertTrue(d.P and 128 != 0)
    }

    @Test fun `ak() rope attach offsets and raises z`() {
        val w = world()
        val p = w.player
        p.ga = Entity(15, null)                            // g.a ax15 rope
        p.setAnim(0); w.iZ = true
        val d = follower(w)
        w.npcFsm.tickAx34(d, w, p)
        assertEquals(p.al + 1, d.al)
        assertEquals(100, d.az)
        assertTrue(d.P and 128 == 0)
    }

    @Test fun `ak() crate attach keeps grounded false then hidden`() {
        val w = world()
        val p = w.player
        p.ga = Entity(51, null)                            // g.a ax51 crate
        p.setAnim(0); w.iZ = true
        val d = follower(w)
        w.npcFsm.tickAx34(d, w, p)
        assertTrue(d.P and 128 != 0)
    }

    @Test fun `ak() other attach type leaves grounded false`() {
        val w = world()
        val p = w.player
        p.ga = Entity(13, null)                            // verbatim L10
        p.setAnim(0); w.iZ = true
        val d = follower(w)
        w.npcFsm.tickAx34(d, w, p)
        assertTrue(d.P and 128 != 0)
    }

    @Test fun `ak() airborne snaps to cell-20 floor within 5 rows`() {
        val w = world()
        val p = w.player
        p.aZ = false; p.ga = null; p.setAnim(43); w.iZ = true
        val d = follower(w)
        // place the follower over a real floor cell (row below spawn)
        val cx = 20
        var row = -1
        for (r in 0 until 55) if (w.collisionCell(cx, r) == 20) { row = r; break }
        assertTrue(row >= 0, "level must have a cell-20 row under x=20")
        d.ak = cx * 20 + 5; d.al = (row - 2) * 20          // 2 rows above floor
        p.setPositionPx(d.ak, d.al)                        // ak()/L4-5 re-snap pos
        w.npcFsm.tickAx34(d, w, p)
        assertEquals(row * 20, d.al)                       // L20 floor snap
        // S43 is in the hide-list → still hidden, but grounded proves snap
        assertTrue(d.P and 128 != 0)
    }

    @Test fun `ak() airborne with no floor stays hidden`() {
        val w = world()
        val p = w.player
        p.aZ = false; p.ga = null; p.setAnim(0); w.iZ = true
        val d = follower(w)
        // far edge column where no cell-20 exists below
        var col = -1
        for (c in 0 until 627) {
            var any = false
            for (r in 0 until 55) if (w.collisionCell(c, r) == 20) { any = true; break }
            if (!any) { col = c; break }
        }
        if (col < 0) return                                // no void column — skip
        d.ak = col * 20; d.al = 0
        p.setPositionPx(d.ak, d.al)
        w.npcFsm.tickAx34(d, w, p)
        assertTrue(d.P and 128 != 0)
    }

    @Test fun `kD and kE respawn after reload`() {
        val w = world()
        repeat(20) {                                     // KO → k.l(12)
            w.player.applyHit(18, 0, null, w)
            w.player.gt = 0; w.iBh = 0
        }
        tickUntilFailed(w)
        assertTrue(w.failed)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 200, 130),
                      InputQueue.Event(1, InputQueue.Type.UP, 200, 130)))
        assertFalse(w.failed)                              // f(false) ran
        assertNotNull(w.kD)                                // V() nulls → respawn
        assertNotNull(w.kE)
        assertTrue(w.npcs.any { it === w.kD })
    }
}
