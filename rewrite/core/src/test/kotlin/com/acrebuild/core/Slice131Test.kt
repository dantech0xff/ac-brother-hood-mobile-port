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

// ---- Slice 131: i.c(iVar) hit-react full port (i.java:3398) ----
class Slice131Test {

    private fun victimAt(w: LevelCellSource, ak: Int, al: Int): Entity {
        val v = Entity(11, null)
        v.ak = ak; v.al = al
        v.W[0] = ak - 10; v.W[2] = ak + 10
        v.W[1] = al - 20; v.W[3] = al
        return v
    }

    @Test fun `counteredBy releases gb faces ax11 and staggers i3398`() {
        val w = Slice128Test.MarkerWorld(cell = 20)
        val v = victimAt(w, 200, 100)
        val atk = Entity(11, null); atk.ak = 100
        w.playerLinkB = v
        v.counteredBy(atk, w)
        assertNull(w.playerLinkB, "g.b released (i.java:3400)")
        assertTrue(v.av, "victim faces left toward the attacker (i.java:3410)")
        assertEquals(9, v.S, "i(9) hurt pose (i.java:3430)")
    }

    @Test fun `counteredBy vs ax61 during Q6 picks S6 i3398`() {
        val w = Slice128Test.MarkerWorld(cell = 20)
        val v = victimAt(w, 200, 100); v.Q = 6
        val atk = Entity(61, null); atk.ak = 100
        v.counteredBy(atk, w)
        assertEquals(6, v.S, "ax61 && Q==6 → i(6) (i.java:3429)")
    }

    @Test fun `counteredBy keeps av for non-NPC attackers i3398`() {
        val w = Slice128Test.MarkerWorld(cell = 20)
        val v = victimAt(w, 200, 100); v.av = false
        val atk = Entity(66, null); atk.ak = 100
        v.counteredBy(atk, w)
        assertFalse(v.av, "av untouched for ax outside {11,17,23,50,73}")
        assertEquals(9, v.S)
    }

    @Test fun `counteredBy push guard cancels on open cell i3398`() {
        // Verbatim guard chain: y() || aF() || e(next,al/20)>=12 → ag=0.
        // On a flat-air world aF() reports the side free → cancels ag.
        val w = Slice128Test.MarkerWorld(cell = 0)
        val v = victimAt(w, 200, 100)
        val atk = Entity(11, null); atk.ak = 300     // right → av=false → push -1536
        v.counteredBy(atk, w)
        assertEquals(0, v.ag, "aF() side-free arm cancels the slide (i.java:3415)")
    }
}
