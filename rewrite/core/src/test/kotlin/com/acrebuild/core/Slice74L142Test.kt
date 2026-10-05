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

// =========================================================================
// Slice 74 — k.I() L142 tail (k.java:3321-3358): goal-arm win check,
// claimer fast-forward step, bh3 camera-target snap.
// =========================================================================
class Slice74L142Test {
    @Test fun `armed goal past the camera band fires scripted win`() {
        val w = world()
        val goal = Entity(9, null)
        goal.setPositionPx(w.camX + 700, w.camY)            // ak > ac2+200
        goal.refreshBoxes()
        goal.Z[0] = 1                                       // L146 script arm
        w.kAV = goal
        w.tick(emptyList())
        // L159: w()==2 → bx=56; l(13); bw=0
        assertTrue(w.won, "goal past cam-right+200 should fire l(13)")
        assertEquals(56, w.kBx)
        assertEquals(0, w.kBw)
    }

    @Test fun `goal inside the milestone band does not win`() {
        val w = world()
        val goal = Entity(9, null)
        goal.setPositionPx(w.camX + 500, w.camY)            // ak in (ac2, ac2+200)
        goal.refreshBoxes()
        goal.Z[0] = 1
        w.kAV = goal
        w.tick(emptyList())
        assertFalse(w.won, "w()==1 stamps the ticker, no win")
    }

    @Test fun `disarmed goal never fires`() {
        val w = world()
        val goal = Entity(9, null)
        goal.setPositionPx(w.camX + 700, w.camY)
        goal.refreshBoxes()
        goal.Z[0] = 0                                       // L147 disarmed
        w.kAV = goal
        w.tick(emptyList())
        assertFalse(w.won)
    }

    @Test fun `L142 steps a fast-forwarded claimer script once per tick`() {
        val w = world()
        w.npcs.clear()                            // no real claimer can rebind kC
        val c = Entity(0, null).apply { ca = 0; scriptStep = 0 }
        c.cd[1] = true; c.cd[2] = true                      // ff + claimed
        w.kC = c
        w.tick(emptyList())
        // aa() ran: cd[5] latches on entry (L22); the consumed block's
        // release tail then cleared cd[2]/kC and parked scriptStep at -2.
        assertTrue(c.cd[5], "cd[2]&&cd[1]&&ab() → C.aa() ran a script step")
    }

    @Test fun `claimer without cd1 fast-forward is not stepped`() {
        val w = world()
        w.npcs.clear()
        val c = Entity(0, null).apply { ca = 0; scriptStep = 0 }
        c.cd[2] = true                                      // claimed, no ff
        w.kC = c
        w.tick(emptyList())
        // the tail gate `cd[2] && cd[1] && ab()` is false → aa() never ran
        assertFalse(c.cd[5], "no L142 step without cd[1]")
    }
}


// =========================================================================
// Slice 76 — k.l(int) screen-state machine (k.java:2031-2300 simple /
//            :1637 structured): `al` freeze flag, 13→31 remap, cy/cz
//            commit tail, medal stamps, mission redirects.
// =========================================================================
