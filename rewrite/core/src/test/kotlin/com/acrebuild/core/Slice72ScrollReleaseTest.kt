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

class Slice72ScrollReleaseTest {
    /** i.java:7053-7159 al(): the k.ah holder clears R/S/T/U each tick and
     *  releases the slot (k.n()) when its zone stops firing — camera must
     *  not stay pinned after the player leaves the trigger. */
    @Test fun `bound release on zone exit frees the camera`() {
        val w = world()
        w.tick(emptyList())
        assertEquals(true, w.kAh != null)
        assertTrue(w.boundMinX > 0 || w.boundMinY > 0 || w.boundMaxX > 0 || w.boundMaxY > 0)
        val p = w.player
        // Walk the player out of the spawn-strip zone (zone ends x≈249).
        repeat(200) {
            p.ak = 600
            w.tick(emptyList())
        }
        assertEquals(null, w.kAh)
        assertTrue(w.camX > 9)   // camera followed right instead of pinning at the 9px floor
    }

    /** Regression: spawn-trigger bound=[9,756,41,979] used to arm kAh.aF=1
     *  with the bound rect, pinning camA between the wall ceiling (41-400)
     *  and the kR floor (9). ax37 records carry aF=0 → wall clamp skips. */
    @Test fun `spawn wall does not pin camera`() {
        val w = world()
        val p = w.player
        p.ak = 3000   // teleport past the wall as the agent did
        p.refreshBoxes()   // entities tick first (G12): no stale spawn boxes
        repeat(30) { w.tick(emptyList()) }
        assertTrue(w.camX > 9)
    }
}
