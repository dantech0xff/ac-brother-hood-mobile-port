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

// ---------------------------------------------------------------------------
// slice 46 — ax6 `an()` trigger marker + ax19 `aO()` meter pickup
// ---------------------------------------------------------------------------
class Slice46Test {

    private fun axAt(w: Level0World, ax: Int, x: Int, y: Int, s: Int): Entity {
        val e = Entity(ax, w.clips[if (ax == 6) 4 else 11])
        e.setPositionPx(x, y)
        val f = mutableListOf(ax, 0, x, y, 0, s, 0)
        for (i in 7..15) f += 0
        if (ax == 6) w.npcFsm.initAx6(e, f) else w.npcFsm.initAx19(e, f)
        w.npcs.add(e)
        return e
    }

    @Test fun `ax6 armed marker fires on player overlap then removes`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 6, p.ak, p.al, 5)                       // S5 armed
        p.refreshBoxes(); e.refreshBoxes()
        w.npcFsm.tickAx6(e, w, p)
        assertEquals(6, e.S, "S5 + overlap → i(6)")
        // wind down: anim end → k.c(this) removal
        e.T = w.clips[4]!!.frameCount(e.S) - 1
        e.U = w.clips[4]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx6(e, w, p)
        assertTrue(e in w.pendingRemove, "S6 r() → k.c(this)")
    }

    @Test fun `ax6 armed S3 stays armed without overlap`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 6, p.ak + 4000, p.al + 4000, 3)         // far away
        w.npcFsm.tickAx6(e, w, p)
        assertEquals(3, e.S, "no overlap → still armed")
        // clip4 anim3's object carries ZERO rects → degenerate W → the
        // i.a() point-box reject keeps S3 dormant even under the player
        // (faithful: the same arm is dead in the original too).
        e.setPositionPx(p.ak, p.al); e.refreshBoxes(); p.refreshBoxes()
        w.npcFsm.tickAx6(e, w, p)
        assertEquals(3, e.S, "S3 + degenerate W → i.a() reject")
    }

    @Test fun `ax19 pickup overlap fires i(18) + sfx17 + 5 sparks`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 19, p.ak, p.al, 17)                     // S17 idle
        p.setAnim(0)                                           // g.c(0) ok
        p.refreshBoxes(); e.refreshBoxes()
        w.npcFsm.tickAx19(e, w, p)
        assertEquals(18, e.S, "overlap → i(18)")
        assertTrue(17 in w.sfxLog, "k.A(17) sfx")
        assertEquals(5, w.pendingInsert.size, "5x a(74,54,5,300) sparks")
        assertTrue(e.b, "b=true latched")
        assertTrue(w.pendingInsert.all { it.ax == 74 && it.P == 528 })
    }

    @Test fun `ax19 consume restores x1 from kAx then removes`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 19, p.ak, p.al, 18)                     // consuming
        p.x1 = 40; w.kAx = 75
        e.T = w.clips[11]!!.frameCount(e.S) - 1
        e.U = w.clips[11]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx19(e, w, p)
        assertEquals(75, p.x1, "g.e(k.ax) → x1=kAx")
        assertTrue(e in w.pendingRemove)
        // dead player: no restore
        val e2 = axAt(w, 19, p.ak, p.al, 18)
        p.x1 = 0
        e2.T = w.clips[11]!!.frameCount(e2.S) - 1
        e2.U = w.clips[11]!!.frameDuration(e2.S, e2.T) - 1
        w.npcFsm.tickAx19(e2, w, p)
        assertEquals(0, p.x1, "g.g() dead → no restore")
    }

    @Test fun `ax19 spark burst table + velocity`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 19, p.ak, p.al, 17)
        p.setAnim(0); p.refreshBoxes(); e.refreshBoxes()
        w.npcFsm.tickAx19(e, w, p)
        val s = w.pendingInsert[0]
        assertTrue(s.clip === w.clips[54], "bi[74]=54 clip map")
        assertEquals(5, s.S); assertEquals(300, s.az)
        assertTrue(s.ag in (-1536..1536) && s.ag and 255 == 0,
                   "ag = j.a(-6,6)<<8")
        assertEquals(0, s.Z[6]); assertTrue(s.Z[7] in 10..15,
                   "Z[7]=j.a(10,16) lifetime")
    }

    @Test fun `ax19 gated on interact-eligible player states`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = axAt(w, 19, p.ak, p.al, 17)
        p.setAnim(67)                                          // mid-attack —
        p.refreshBoxes(); e.refreshBoxes()                     // NOT g.c()
        w.npcFsm.tickAx19(e, w, p)
        assertEquals(17, e.S, "non-g.c state → no pickup")
    }
}
