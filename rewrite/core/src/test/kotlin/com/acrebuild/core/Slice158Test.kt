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

/** Slice 158 — `k.bk[]` wired: ax67 offscreen-score clip-rate pick
 *  (i.java:588-592): bk[kind]==49 → /400+/240, ==27 → /800+/240,
 *  else → /400+/120. kBk now returns the real table, not 0. */
class Slice158Test {

    @Test fun `ax67 offscreen score uses bk clip rates`() {
        val w = world()
        fun prop(kind: Int): Entity {
            val e = Entity(67, null)
            e.Z[0] = kind
            e.ak = w.kO + 200 + 800
            e.al = w.kP + 120 + 240
            e.offscreenScore(w)
            return e
        }
        // bk[12]=49 → 800/400 + 240/240 = 3
        assertEquals(3, prop(12).au)
        // bk[1]=27 → 800/800 + 240/240 = 2
        assertEquals(2, prop(1).au)
        // bk[0]=24 → 800/400 + 240/120 = 4
        assertEquals(4, prop(0).au)
    }
}
