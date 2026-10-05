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

class Slice67Test {

    /** ax76 record `[76,aw,x,y,type,anim,P,az,aC,o]` — L361 arm shape. */
    private fun ax76At(w: Level0World, x: Int, y: Int,
                       type: Int = 0, anim: Int = 0, az: Int = 50,
                       aC: Int = 0, o: Int = -1): Entity {
        val rec = listOf(76, 9, x, y, type, anim, 0, az, aC, o)
        val e = Entity(76, null).apply { aw = 9 }   // bi[76]=56 → empty slot
        w.npcFsm.initAx76(e, rec)
        e.setPositionPx(x, y)
        e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    // -- init (i.java:3546 L361, proven) ------------------------------------
    @Test fun `ax76 init reads record fields`() {
        val w = world()
        val e = ax76At(w, 300, 200, type = 1, anim = 0, az = 41, aC = 7, o = 55)
        assertEquals(41, e.az)                       // az = f[7]
        assertEquals(7, e.aC)                        // aC = f[8]
        assertEquals(55, e.oId)                      // o = f[9]
        assertEquals(1, e.Z[1])                      // Z[1] = f[4]
        assertEquals(0, e.S)
    }

    // -- S0: arm (i.java:21412 L5, proven) ----------------------------------
    @Test fun `ax76 S0 arms on standing overlap`() {
        val w = world()
        val p = w.player
        p.aZ = true
        p.refreshBoxes()                             // give p.W real edges
        val e = ax76At(w, p.ak, p.al, type = 1)
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(1, e.S)                         // i(1)
        assertTrue((p.aA and 8) != 0)                // aA |= 8
        assertEquals(e.az - 1, p.az)                 // player pushed below
        assertSame(e, p.ge)                          // g.e = this
    }

    @Test fun `ax76 S0 no arm when interact target held`() {
        val w = world()
        val p = w.player
        p.aZ = true
        p.g = Entity(19, null)                       // g.g != null → no arm
        p.refreshBoxes()
        val e = ax76At(w, p.ak, p.al, type = 1)
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(0, e.S)
        assertNull(p.ge)
    }

    @Test fun `ax76 S0 arms without bind when type 0`() {
        val w = world()
        val p = w.player
        p.aZ = true
        p.refreshBoxes()
        val e = ax76At(w, p.ak, p.al, type = 0)      // Z[1]==0
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(1, e.S)                         // still i(1)
        assertEquals(0, p.aA and 8)                  // no aA bit
        assertNull(p.ge)                             // no owner claim
    }

    @Test fun `ax76 S0 release only when owner`() {
        val w = world()
        val p = w.player
        p.aZ = false                                 // arm conditions fail
        val e = ax76At(w, p.ak - 500, p.al, type = 1)
        val other = Entity(76, null)
        p.ge = other                                 // someone else's slot
        p.aA = p.aA or 8; p.az = 20
        w.npcFsm.tickAx76(e, w, p)
        assertSame(other, p.ge)                      // untouched
        assertTrue((p.aA and 8) != 0)
        p.ge = e                                     // now owned → release
        w.npcFsm.tickAx76(e, w, p)
        assertNull(p.ge)
        assertEquals(0, p.aA and 8)
        assertEquals(100, p.az)                      // az = 100 restore
    }

    // -- S1: disarm (i.java:21421 L21, proven) ------------------------------
    @Test fun `ax76 S1 disarms on exit`() {
        val w = world()
        val p = w.player
        val e = ax76At(w, p.ak, p.al, type = 1, anim = 1)
        e.W[0] = 9999; e.W[1] = 9999                 // no overlap
        e.W[2] = 1; e.W[3] = 1
        p.ge = e; p.aA = p.aA or 8; p.az = 20
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(0, e.S)                         // i(0)
        assertNull(p.ge)                             // released
        assertEquals(100, p.az)
    }

    @Test fun `ax76 S1 stays while overlapping`() {
        val w = world()
        val p = w.player
        p.refreshBoxes()
        val e = ax76At(w, p.ak, p.al, type = 1, anim = 1)
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(1, e.S)                         // stays armed
    }

    // -- S3/S5 → S4/S6 (i.java:21444 L29, proven) ---------------------------
    @Test fun `ax76 S3 advances on hitbox overlap`() {
        val w = world()
        val p = w.player
        p.refreshBoxes()
        p.X[0] = p.ak - 5; p.X[1] = p.al - 5         // non-degenerate X
        p.X[2] = p.ak + 40; p.X[3] = p.al + 40
        val e = ax76At(w, p.ak, p.al, anim = 3)
        e.W[0] = p.X[0] - 5; e.W[1] = p.X[1] - 5
        e.W[2] = p.X[2] + 5; e.W[3] = p.X[3] + 5
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(4, e.S)                         // i(S+1)
    }

    @Test fun `ax76 S5 waits without hitbox overlap`() {
        val w = world()
        val e = ax76At(w, 0, 0, anim = 5)
        w.npcFsm.tickAx76(e, w, w.player)
        assertEquals(5, e.S)
    }

    // -- S4/S6 despawn (i.java:21448 L33, proven) ---------------------------
    @Test fun `ax76 S4 despawns at anim end`() {
        val w = world()
        val e = ax76At(w, 300, 200, anim = 4)        // clipless → r() true
        w.npcFsm.tickAx76(e, w, w.player)
        assertTrue(e in w.pendingRemove)
    }

    // -- S2: damage cycle (i.java:21452 L37, proven) -------------------------
    @Test fun `ax76 S2 damages player each tick`() {
        val w = world()
        val p = w.player
        val hp0 = p.x1
        p.refreshBoxes()
        val e = ax76At(w, p.ak, p.al, anim = 2)
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(hp0 - w.GU[w.weaponSlot], p.x1) // g.d(g.u[k.au])
        w.npcFsm.tickAx76(e, w, p)                   // g.t=10 iframes block
        assertEquals(hp0 - w.GU[w.weaponSlot], p.x1)
        p.gt = 0
        w.npcFsm.tickAx76(e, w, p)
        assertEquals(hp0 - 2 * w.GU[w.weaponSlot], p.x1)
    }

    @Test fun `ax76 S2 counts down then chains sibling`() {
        val w = world()
        val p = w.player
        val e = ax76At(w, p.ak - 500, p.al, anim = 2, aC = 2, o = 9001)
        val sib = Entity(76, null).apply { aw = 9001 }
        sib.setAnim(0)
        w.npcs.add(sib)
        w.npcFsm.tickAx76(e, w, p)                   // aC 2→1
        assertEquals(1, e.aC)
        assertEquals(0, sib.S)
        w.npcFsm.tickAx76(e, w, p)                   // aC 1→0 → chain
        assertEquals(0, e.aC)
        assertEquals(2, sib.S)                       // r0.i(2)
        w.npcFsm.tickAx76(e, w, p)                   // aC==0: sibling already
        assertEquals(2, sib.S)                       // in S2 → skipped
    }

    @Test fun `ax76 S2 no chain when o is -1 or non-ax76`() {
        val w = world()
        val p = w.player
        val e = ax76At(w, p.ak - 500, p.al, anim = 2, aC = 0, o = -1)
        w.npcFsm.tickAx76(e, w, p)                   // oId==-1 ��� skip
        val sib = Entity(11, null).apply { aw = 9002 } // ax!=76 → skip
        w.npcs.add(sib)
        val e2 = ax76At(w, p.ak - 500, p.al, anim = 2, aC = 0, o = 9002)
        w.npcFsm.tickAx76(e2, w, p)
        assertEquals(0, sib.S)
    }
}
