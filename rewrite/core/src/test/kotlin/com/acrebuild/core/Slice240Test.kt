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

class Slice240Test {
    // slice 240 — `i.bk()` ax60 lift cable (i.java:42549-42920): the
    // S→direction dispatch moved into `liftCableArm()` (the renderer
    // owns the setClip regions + link-frame loops); plus the ax43
    // `j.a(g,…,1)` re-read as setClip, not a fill (j.java:2731).

    private fun ent(ax: Int, w: Level0World): Entity {
        val e = Entity(ax, null)
        e.aw = 7100 + ax
        w.npcs.add(e)
        return e
    }

    @Test fun `liftCableArm maps the verbatim S dispatch`() {
        val w = world()
        val e = ent(60, w)
        for (s in intArrayOf(9, 10)) { e.S = s
            assertEquals(1, e.liftCableArm(), "S$s → L4a (up)") }
        for (s in intArrayOf(16, 17)) { e.S = s
            assertEquals(2, e.liftCableArm(), "S$s → Lca (down)") }
        for (s in intArrayOf(13, 15)) { e.S = s
            assertEquals(3, e.liftCableArm(), "S$s → L169 (left)") }
        for (s in intArrayOf(11, 14)) { e.S = s
            assertEquals(4, e.liftCableArm(), "S$s → L1e9 (right)") }
        for (s in intArrayOf(0, 1, 8, 12, 18, 34, -1)) { e.S = s
            assertEquals(0, e.liftCableArm(), "S$s → L267 (no cable)") }
    }

    @Test fun `ax43 drawStyleF emits no fill - jcall is setClip`() {
        // L733/L784 + L10d8 (i.java:12769-12860/13985-14005): the three
        // `j.a(g,…,1)` calls are Graphics.setClip (j.java:2731, proven)
        // — clip regions + restore, renderer-owned. No fxRects, ever.
        val w = world()
        val cv = ent(51, w)                          // any cv carrier
        cv.W[0] = 100; cv.W[1] = 0
        cv.W[2] = 200; cv.W[3] = 240
        w.cv = cv
        val e = ent(43, w)
        e.setPositionPx(w.kO + 150, w.kP + 100)
        e.refreshBoxes()
        val before = w.fxRects.size
        e.drawStyleF(w)
        assertEquals(before, w.fxRects.size,
            "ax43 arms emit clip regions, not fills — renderer-owned")
        assertEquals(1, e.drawStyleF(w), "F() still returns 1 to draw")
    }
}
