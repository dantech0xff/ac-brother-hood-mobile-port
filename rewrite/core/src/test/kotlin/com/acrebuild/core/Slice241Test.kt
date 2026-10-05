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

class Slice241Test {
    // slice 241 — `i.by()` ax40 zipline rope (i.java:48822-49163):
    // rider scan (ax40 bd-entry === player.ac on the same rope `s`),
    // marker `al` writes (sag vs rest), and the rope-line draws
    // (5-arg `j.a` = drawLine, j.java:2792).

    private fun rope(w: Level0World, x: Int, y: Int): Entity {
        val r = Entity(40, null)
        r.S = 2
        r.setPositionPx(x, y); r.refreshBoxes()
        r.Z[1] = y + 20                            // rope line y
        r.Z[3] = x + 400                           // far anchor x
        w.npcs.add(r)
        return r
    }

    private fun marker(rope: Entity, w: Level0World, x: Int): Entity {
        val m = Entity(40, null)
        m.s = rope
        m.setPositionPx(x, 0); m.refreshBoxes()
        m.Z[1] = 600                               // marker rest row
        w.npcs.add(m)
        return m
    }

    private fun drawSlot(w: Level0World, vararg es: Entity) {
        es.forEachIndexed { i, e -> w.drawList[i] = e }
        w.drawCount = es.size
    }

    @Test fun `parked markers rest below the rope and draw two lines`() {
        val w = world()
        val r = rope(w, 1000, 500)
        val m1 = marker(r, w, 1100)
        val m2 = marker(r, w, 1300)
        drawSlot(w, m1, m2)
        w.player.ac = null
        w.fxLines.clear()
        r.drawStyleF(w)
        assertEquals(r.al + 1, m1.al, "L17b: parked al = rope.al+1")
        assertEquals(m1.al, m1.Z[1], "L17b: Z[1] = al")
        assertEquals(r.al + 1, m2.al)
        assertEquals(2, w.fxLines.size, "L29d: 2 straight rope lines")
        val l = w.fxLines[0]
        assertEquals(r.ak - w.kO, l[0]); assertEquals(r.Z[1] - w.kP, l[1])
        assertEquals(r.Z[3] - w.kO, l[2]); assertEquals(r.Z[1] - w.kP, l[3])
        assertEquals(-3584205, l[4], "rope color 0xFFC94F33")
        assertEquals(r.Z[1] - w.kP + 1, w.fxLines[1][1], "2px rope")
    }

    @Test fun `rider sags markers and draws four lines`() {
        val w = world()
        val r = rope(w, 1000, 500)                 // ak=1000, Z1=520, Z3=1400
        val m1 = marker(r, w, 1100)                // left of rider
        val m2 = marker(r, w, 1300)                // right of rider
        val g = Entity(40, null)                   // the gondola = aS.ac
        g.s = r
        g.setPositionPx(1200, 550); g.refreshBoxes()
        g.Z[1] = 30                                // gondola hang offset
        w.npcs.add(g)
        w.player.ac = g
        w.player.S = 164                           // zipline-hang anim
        drawSlot(w, g, m1, m2)
        w.fxLines.clear()
        r.drawStyleF(w)
        // left formula: Z[1] + (m.ak-r.ak)*(g.al-g.Z[1])/(g.ak-r.ak)
        assertEquals(600 + 100 * (550 - 30) / 200, m1.al, "L119-left sag")
        // right formula: Z[1] + (r.Z3-m.ak)*(g.al-g.Z[1])/(r.Z3-g.ak)
        assertEquals(600 + 100 * (550 - 30) / 200, m2.al, "L119-right sag")
        assertEquals(4, w.fxLines.size, "L1ac: 4 sagged rope lines")
        val gx = g.ak - w.kO; val gy = g.W[1] - w.kP
        assertEquals(intArrayOf(r.ak - w.kO, r.Z[1] - w.kP, gx, gy,
                     -3584205).toList(), w.fxLines[0].toList())
        assertEquals(intArrayOf(r.Z[3] - w.kO, r.Z[1] - w.kP, gx, gy,
                     -3584205).toList(), w.fxLines[1].toList())
        assertEquals(intArrayOf(r.ak - w.kO, r.Z[1] - w.kP + 1, gx,
                     gy + 1, -3584205).toList(), w.fxLines[2].toList())
    }

    @Test fun `gate requires S2 and nonzero Z3`() {
        val w = world()
        val r = rope(w, 1000, 500)
        val m1 = marker(r, w, 1100)
        drawSlot(w, m1)
        w.fxLines.clear()
        r.S = 3                                    // wrong state
        r.drawStyleF(w)
        assertEquals(0, w.fxLines.size)
        assertEquals(0, m1.al, "no marker write outside S2")
        r.S = 2; r.Z[3] = 0                        // no far anchor
        r.drawStyleF(w)
        assertEquals(0, w.fxLines.size)
    }

    @Test fun `rider on rope but player not hanging falls back parked`() {
        val w = world()
        val r = rope(w, 1000, 500)
        val m1 = marker(r, w, 1100)
        val g = Entity(40, null)
        g.s = r
        g.setPositionPx(1200, 550); g.refreshBoxes()
        w.npcs.add(g)
        w.player.ac = g
        w.player.S = 0                             // standing, not S164
        drawSlot(w, g, m1)
        w.fxLines.clear()
        r.drawStyleF(w)
        assertEquals(r.al + 1, m1.al, "r9=0 → L17b rest, not sag")
        assertEquals(2, w.fxLines.size)
    }
}
