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

// -- Slice 94 — draw-pass overlay tail predicates (k.java:2931-2990) ----
class Slice94Test {
    private fun ent(w: Level0World, ax: Int, al: Int = 50, x: Int = 100): Entity {
        val e = Entity(ax, null)
        val px = w.camX + x; val py = w.camY + al
        e.ak = px; e.al = py
        e.W[0] = px; e.W[1] = py; e.W[2] = px + 20; e.W[3] = py + 20
        return e
    }

    @Test fun `h shows bar for alive alerted ax11`() {
        val w = world()
        val e = ent(w, 11); e.aA = 1; e.aB = 40
        assertTrue(w.showsHpBar(e))                     // i.java:20813
    }

    @Test fun `h false for dead ax11`() {
        val w = world()
        val e = ent(w, 11); e.aA = 1; e.aB = 0
        assertFalse(w.showsHpBar(e))                    // !P() fails
    }

    @Test fun `h false for unalerted ax11`() {
        val w = world()
        val e = ent(w, 11); e.aA = 0; e.aB = 40
        assertFalse(w.showsHpBar(e))                    // aA>=1 required
    }

    @Test fun `h true for ax17 and ax50 regardless of alert`() {
        val w = world()
        assertTrue(w.showsHpBar(ent(w, 17)))            // i.java:20817
        assertTrue(w.showsHpBar(ent(w, 50)))
    }

    @Test fun `h false for other ax and off-camera`() {
        val w = world()
        assertFalse(w.showsHpBar(ent(w, 14)))           // default arm
        val off = ent(w, 17, al = 0, x = 100); off.ak = w.camX + 1000
        off.W[0] = off.ak; off.W[2] = off.ak + 20
        assertFalse(w.showsHpBar(off))                  // a(W, ac) fails
    }

    @Test fun `h rejects degenerate W`() {
        val w = world()
        val e = ent(w, 17)
        e.W[0] = e.W[2]; e.W[1] = e.W[3]                // point rect
        assertFalse(w.showsHpBar(e))                    // i.a point-reject
    }

    @Test fun `claimAb needs bound ca armed cK and cd0 clear`() {
        val e = Entity(5, null)
        e.ca = 2; e.cd[0] = false; e.scriptStep = 0
        assertTrue(e.claimAb())                         // i.java:18914
        e.cd[0] = true; assertFalse(e.claimAb()); e.cd[0] = false
        e.ca = -1; assertFalse(e.claimAb()); e.ca = 0
        e.scriptStep = -1; assertFalse(e.claimAb())
    }

    @Test fun `khypot matches k h approximation`() {
        assertEquals(0, Trig.khypot(0, 0))              // k.java:5302
        assertEquals(4, Trig.khypot(4, 0))
        assertEquals(6, Trig.khypot(3, 4))              // 7 - 1 - 0 + 0
        assertEquals(6, Trig.khypot(-3, -4))            // abs first
    }

    @Test fun `bu bv are the per-difficulty HP scales`() {
        assertTrue(Entity.WEAPON_DMG.contentEquals(intArrayOf(300, 400, 500)))
        assertTrue(Entity.NPC_HP_BV.contentEquals(intArrayOf(100, 140, 200)))
    }
}
