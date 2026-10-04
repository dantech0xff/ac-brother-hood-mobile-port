package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 393 — `i.a()` (the push-past / contact resolution against `k.aS`,
 * i.javap `a()` @0-477) is one straight line; ten original methods call it
 * (`I()` ×2, `n`, `aj`, `aA`, `aJ`, `aP`, `bg`, `bu`, `bM`).
 *
 * The port had three copies:
 * - `Entity.pushContact` modelled the simple decompile's displaced S131/146
 *   gate as a loop and left the right-push arm without its `a(true); ag=0`
 *   tail;
 * - `NpcFsm.pushOut` (ax4, ax41) — faithful for those two callers only;
 * - `NpcFsm.bossPushPast` (ax29) — **inverted**: it pushed the player only
 *   when walking AWAY (`ag < 0` on the left side), never the player walking
 *   into the boss, and never zeroed `ag`.
 *
 * Now `pushContact` is the only copy and every caller uses it.
 */
class Slice393Test {
    /** An entity whose box straddles the player. [dx] > 0 puts it to the
     *  player's right. */
    private fun beside(w: Level0World, dx: Int, ax: Int = 9, s: Int = 0): Entity {
        val p = w.player
        val e = Entity(ax, w.clips[7])
        e.setPositionPx(p.ak + dx, p.al); e.S = s
        e.W[0] = p.ak - 40; e.W[1] = p.al - 40
        e.W[2] = p.ak + 40; e.W[3] = p.al + 40
        return e
    }

    private fun prepared(): Level0World {
        val w = world(); w.npcs.clear()
        w.player.refreshBoxes()
        w.player.setAnim(0); w.player.ga = null
        w.player.bb = false; w.player.bc = false
        return w
    }

    private fun half(e: Entity) = (e.W[2] - e.W[0]) / 2
    private fun pHalf(w: Level0World) = (w.player.W[2] - w.player.W[0]) / 2

    @Test fun `a player walking into the entity from the left is stopped at its left edge`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)                       // entity to the right
        p.ag = 256                                  // walking right
        e.pushContact(w)
        assertEquals(e.ak - pHalf(w) - half(e), p.ak, "@307-344")
        assertEquals(0, p.ai); assertEquals(0, p.ag, "@463-470 tail")
    }

    @Test fun `a player walking into the entity from the right is stopped at its right edge`() {
        val w = prepared(); val p = w.player
        val e = beside(w, -4)                      // entity to the left
        p.ag = -256
        e.pushContact(w)
        assertEquals(e.ak + pHalf(w) + half(e), p.ak, "@402-439")
        assertEquals(0, p.ai); assertEquals(0, p.ag, "the right arm ends on the same tail")
    }

    @Test fun `a standing player is pushed out too (ag == 0 passes the left test)`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        p.ag = 0
        val before = p.ak
        e.pushContact(w)
        assertNotEquals(before, p.ak, "ag >= 0 && ak <= entity.ak → the left block")
    }

    @Test fun `moving away is not snapped but the tail still zeroes ag`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)                       // entity right, player moving left
        p.ag = -256
        val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak, "neither block fires")
        assertEquals(0, p.ag, "@463 is the join of every path that got past the overlap")
    }

    @Test fun `a wall in the travel direction blocks the snap but not the tail`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        p.ag = 256; p.bc = true                     // y() = bc for ag > 0
        val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak, "y() true → no snap")
        assertEquals(0, p.ag)
    }

    @Test fun `every path past the overlap ends on aS_a(true) - the side rescan runs`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        p.ag = -256                                 // moving away: neither block fires
        p.v = false; p.aT = 77
        e.pushContact(w)
        assertTrue(p.v, "a(true) @463 sets v")
        assertEquals(0, p.aT.coerceAtMost(0), "side strip re-read")
        assertNotEquals(77, p.aT, "aT rewritten by the rescan")
    }

    @Test fun `the wall test guards the right block too`() {
        val w = prepared(); val p = w.player
        val e = beside(w, -4)                      // entity left, player moving left
        p.ag = -256; p.bb = true                    // y() = bb for ag < 0
        val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak, "@396-399 y() true → no snap")
        assertEquals(0, p.ag)
    }

    @Test fun `no overlap no effect at all`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        e.W[0] = p.W[2] + 200; e.W[2] = p.W[2] + 240
        p.ag = 256
        e.pushContact(w)
        assertEquals(256, p.ag, "returned at @128 before any write")
    }

    @Test fun `an airborne player (S gt 43) is left alone after the overlap gate`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        p.setAnim(44); p.ag = 256
        val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak); assertEquals(256, p.ag, "@265 returns before the tail")
    }

    @Test fun `g_a claims - an ax43 anchor returns at once, any other holder after the overlap`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4)
        p.ag = 256
        p.ga = Entity(43, null)
        e.pushContact(w)
        assertEquals(256, p.ag, "@32-49")
        p.ga = Entity(11, null)
        e.pushContact(w)
        assertEquals(256, p.ag, "@131")
    }

    @Test fun `S131 and S146 push only a crouched player that faces them`() {
        for (s in intArrayOf(131, 146)) {
            val w = prepared(); val p = w.player
            val e = beside(w, 4, s = s)
            p.ag = 256; p.av = false               // facing right = towards e (e.ak > p.ak)
            p.setAnim(0)
            val before = p.ak
            e.pushContact(w)
            assertEquals(before, p.ak, "S$s: player not in S12 → @112 return")
            assertEquals(256, p.ag)
            p.setAnim(12)
            assertTrue(p.inFrontOf(e), "fixture: aS.g(e)")
            e.pushContact(w)
            assertNotEquals(before, p.ak, "S$s: S12 + g(e) continues at @113")
            p.setAnim(12); p.ag = 256; p.ak = before; p.av = true
            assertFalse(p.inFrontOf(e))
            e.pushContact(w)
            assertEquals(before, p.ak, "S$s: facing away → @109 ifne fails")
        }
    }

    @Test fun `S18 pushes only against a player in S12`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4, s = 18)
        p.ag = 256; val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak)
        p.setAnim(12); p.ag = 256
        e.pushContact(w)
        assertNotEquals(before, p.ak)
    }

    @Test fun `a rolling player (S6) passes through an ax11`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4, ax = 11)
        p.setAnim(6); p.ag = 256; val before = p.ak
        e.pushContact(w)
        assertEquals(before, p.ak)
        assertEquals(256, p.ag)
    }

    // ------------------------------------------------------------ call sites
    @Test fun `the ax29 boss pushes the player who walks into it - and zeroes ag`() {
        val w = prepared(); val p = w.player
        w.boundMaxX = 100000
        val b = Entity(29, w.clips[7]).also {
            it.aw = 60; it.aB = 500
            it.setPositionPx(p.ak + 4, p.al); it.refreshBoxes()
            it.W[0] = p.ak - 40; it.W[1] = p.al - 40; it.W[2] = p.ak + 40; it.W[3] = p.al + 40
        }
        w.npcs.add(b)
        b.setAnim(0)                                // not one of the skip states
        w.iBy = 1
        p.X[0] = 0; p.X[1] = 0; p.X[2] = 0; p.X[3] = 0     // no punish arm
        p.ag = 256
        val before = p.ak
        w.npcFsm.tickBoss(b, p, Pad())
        assertTrue(p.ak < before, "the player walking right into the boss is pushed left (was: never)")
        assertEquals(0, p.ag)
    }

    @Test fun `ax41 knockable and ax4 destructible still push through the same method`() {
        val w = prepared(); val p = w.player
        val e = beside(w, 4, ax = 41, s = 3)
        p.ag = 256; val before = p.ak
        w.npcFsm.tickKnockable(e, w, p)
        assertTrue(p.ak < before, "n() S3 → a()")
    }
}
