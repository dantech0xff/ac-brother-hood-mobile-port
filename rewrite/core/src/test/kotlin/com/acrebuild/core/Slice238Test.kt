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

// Slice 238 — i.B() flying canyon-wall collide (i.java:3844-4204, proven):
// i.w latch, dead-drag arm, k.ai/i.e gate → L36 (camera band + extrude),
// L149 (cell-21 kill / below-screen k.l(12) / extrude). Helpers:
// b() L10a slide-LEFT, c(r7,r8,r9) L10f slide-RIGHT, c() no-arg L32 probe.
class Slice238Test {

    private fun flyWorld(): Level0World = world().also {
        it.kAj = 1                                   // MISSION_BH[1]==3 — flying
        it.iW = false                                // fresh latch
    }

    private fun placeW0W1(p: Entity, x0: Int, y1: Int) {
        p.refreshBoxes()
        p.setPositionPx(p.ak + (x0 - p.W[0]), p.al + (y1 - p.W[1]))
        p.refreshBoxes()
    }

    @Test fun `first call latches iW and returns false`() {
        val w = flyWorld()
        val p = w.player
        p.setPositionPx(1500, 900); p.refreshBoxes()
        assertFalse(p.canyonCollide(w))
        assertTrue(w.iW, "Lc latch i.w set on first call (i.java:3849)")
        // second call runs the real logic — quiet path over open sky → 0
        assertFalse(p.canyonCollide(w))
    }

    @Test fun `director pass clamps ak into the kO camera band`() {
        val w = flyWorld()
        val p = w.player
        p.canyonCollide(w)                           // latch
        w.kAi = true                                 // L36 gate
        w.kO = 1000; w.rebuildCamRect()
        p.setPositionPx(1600, 400); p.refreshBoxes()
        p.canyonCollide(w)
        assertEquals(1400, p.ak, "ak >= kO+400 clamps to kO+400 (L59)")
        p.setPositionPx(500, 400); p.refreshBoxes()
        p.canyonCollide(w)
        assertEquals(1000, p.ak, "ak <= kO clamps to kO (L55)")
    }

    @Test fun `cell 21 top corner arms iBe and plays i34`() {
        val w = flyWorld()
        val p = w.player
        // level-0's only reachable cell-21: (133,23) → px (2660,460); the
        // (86,26) cell sits below the %260 wrap floor — proven dead.
        placeW0W1(p, 2660, 460)
        p.canyonCollide(w)                           // latch
        assertEquals(21, w.collisionCell(p.W[0] / 20, (p.W[1] % 260 + 260) / 20))
        val alBefore = p.al
        p.canyonCollide(w)
        assertTrue(w.iBe, "i.be armed on cell-21 contact (L1e2)")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(alBefore - w.kX, p.al, "dead-drag al -= k.X (L1d9)")
        assertEquals(34, p.S, "i(34) splat anim (L1e9)")
    }

    @Test fun `below screen in quiet path runs k_l_12`() {
        val w = flyWorld()
        val p = w.player
        p.canyonCollide(w)                           // latch
        w.kP = 0; w.rebuildCamRect()
        p.setPositionPx(5000, 500); p.refreshBoxes() // Y outside cam rect
        assertFalse(p.canyonCollide(w))
        assertEquals(12, w.jC, "L1f7: !v() && al > k.P+240 → k.l(12)")
    }

    @Test fun `iE decays once per world tick`() {
        val w = flyWorld()
        w.iE = 30
        w.tick(emptyList())
        assertEquals(29, w.iE, "i.e-- per frame (i.java:13192)")
    }

    @Test fun `iE resets in the world D block`() {
        val w = flyWorld()
        w.iE = 30; w.iW = true
        w.loadMission(1)                             // rebuild runs i.D()
        assertEquals(0, w.iE)
        assertFalse(w.iW)
    }

    @Test fun `ax24 S20 shrine arm sets iE to 30`() {
        // i.java:39329 — the L36 gate's only producer: overlap arms the
        // 30-frame post-impact collide window.
        val w = flyWorld()
        val p = w.player
        p.setPositionPx(1500, 900); p.refreshBoxes()
        val shrine = Entity(24, null)
        shrine.S = 20
        shrine.setPositionPx(1500, 900)
        shrine.W[0] = p.W[0] - 5; shrine.W[1] = p.W[1] - 5
        shrine.W[2] = p.W[2] + 5; shrine.W[3] = p.W[3] + 5
        w.npcs.add(0, shrine)
        w.npcFsm.tickAx24(shrine, w, w.player)
        assertEquals(30, w.iE, "L36 gate armed via ax24-S20 overlap")
    }

    @Test fun `dead drag arm runs when iBe already set`() {
        val w = flyWorld()
        val p = w.player
        w.iW = true                                  // latch already consumed
        w.iBe = true
        p.setPositionPx(1500, 900); p.refreshBoxes()
        p.ag = 99; p.ah = 99
        val alBefore = p.al
        assertFalse(p.canyonCollide(w))
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(alBefore - w.kX, p.al)
    }
}
