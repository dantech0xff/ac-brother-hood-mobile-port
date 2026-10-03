package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 355 — ax37 scroll triggers run as entities (`case 37 → al()`).
 *
 * The port fired every ax37 record from a separate list built at load,
 * outside the entity loop, so the loop's gates never applied
 * (k.java:2577: `(P&256)==0 && ((au<2 && (P&32)==0) || (P&16)!=0)`):
 * the two parked records (m2 uid 119, m7 uid 14 — `P=32`, so no `P|16`
 * from initAx37) fired from the first frame, a script `k.c` removal or
 * `P|256` could not stop a trigger, and `al()` checked the link gate
 * before the zone (i.java:5739-5828 tests the zone first).
 */
class Slice355Test {
    /** A trigger centred on (cx, cy): zone ±100, bound (cx−200, cy−150)
     *  +500×500, mask 9 (k.R + k.U), no link, containment mode. */
    private fun trigger(w: Level0World, cx: Int, cy: Int, P: Int = 0, mask: Int = 9): Entity {
        val f = listOf(37, 9555, cx, cy, 0, 0, P,
            -100, -100, 200, 200, -200, -150, 500, 500, mask, 0, -1, 0)
        val e = Entity(37, null).apply { aw = f[1]; setPositionPx(cx, cy); this.P = f[6] }
        w.npcFsm.initAx37(e, f)
        w.npcs += e
        return e
    }

    /** Settled level-0 world; the trigger goes where the player stands. */
    private fun staged(P: Int = 0): Pair<Level0World, Entity> {
        val w = world()
        settleIntro(w)
        val p = w.player
        p.refreshBoxes()
        w.kM(2)                                       // camera onto the player
        return w to trigger(w, p.ak, p.al - 25, P)
    }

    @Test fun `initAx37 stages the zone and bound boxes`() {
        val w = world()
        val e = trigger(w, 5000, 5000)
        assertEquals(listOf(4900, 4900, 5100, 5100), e.W.toList())
        assertEquals(listOf(4800, 4850, 5300, 5350), e.X.toList())
        assertEquals(16, e.P and 16, "live trigger: P|16 → ticks at any au")
        val parked = trigger(w, 5000, 5000, P = 32)
        assertEquals(0, parked.P and 16, "parked: no P|16")
    }

    @Test fun `a live trigger fires from the entity loop`() {
        val (w, e) = staged()
        w.tick(emptyList())
        assertSame(e, w.kAh)
        assertEquals(e.X[0], w.kR, "mask 1 → k.R = X[0]")
        assertEquals(e.X[3], w.kU, "mask 8 → k.U = X[3]")
    }

    @Test fun `a parked trigger waits until a script clears P32`() {
        val (w, e) = staged(P = 32)
        w.tick(emptyList())
        assertFalse(w.kAh === e, "P&32 without P|16 → the loop skips it")
        assertFalse(w.kR == e.X[0])
        e.P = e.P and -33                            // a script wakes it
        w.tick(emptyList())
        assertSame(e, w.kAh, "au < 2 near the camera → it fires")
        assertEquals(e.X[0], w.kR)
    }

    @Test fun `P256 or a k_c removal stops a trigger`() {
        val (w, e) = staged()
        e.P = e.P or 256
        w.tick(emptyList())
        assertFalse(w.kAh === e, "P|256 → no tick")
        e.P = e.P and -257
        w.removeEntity(e)
        w.tick(emptyList())
        assertFalse(e in w.npcs)
        assertFalse(w.kAh === e)
    }

    @Test fun `al does nothing while the player is in S9 or S50`() {
        val (w, e) = staged()
        w.kN()
        w.player.S = 50
        w.scrollTriggerAl(e)
        assertEquals(0, w.kR)
        assertNull(w.kAh)
        w.player.S = 0
        w.scrollTriggerAl(e)
        assertSame(e, w.kAh, "containment claims k.ah — the entity itself")
        assertEquals(e.X[0], w.kR)
    }

    @Test fun `the holder is released by k_n when the player leaves the zone`() {
        val (w, e) = staged()
        w.kN()
        w.scrollTriggerAl(e)
        assertSame(e, w.kAh)
        w.player.setPositionPx(e.W[2] + 300, e.W[3] + 300); w.player.refreshBoxes()
        w.scrollTriggerAl(e)
        assertNull(w.kAh)
        assertEquals(0, w.kR); assertEquals(0, w.kU)
    }

    @Test fun `m2 uid 119 spawns parked and m7 uid 14 too`() {
        val m2 = world(aj = 2)
        val t119 = m2.npcs.first { it.ax == 37 && it.aw == 119 }
        assertEquals(32, t119.P and 32)
        assertEquals(0, t119.P and 16)
        val m7 = world(aj = 7)
        val t14 = m7.npcs.first { it.ax == 37 && it.aw == 14 }
        assertEquals(32, t14.P and 32)
        assertTrue(m7.npcs.count { it.ax == 37 && (it.P and 16) != 0 } > 0)
    }
}
