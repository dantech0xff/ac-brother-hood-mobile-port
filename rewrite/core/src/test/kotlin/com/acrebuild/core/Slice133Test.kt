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

/** Slice 133 — `i.a(boolean)` side-strip rescan, `i.M()` floor probe,
 *  `i.al()` wide ledge mount, `i.H()` drop-held, and the S9/S10 stagger
 *  arm (g.java:1265-1290). */
class Slice133Test {
    private val clip by lazy { Clip.load(asset("clips/clip7/clip.acpk")) }
    private fun playerAt(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al; p.av = av
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `a() clears flags and rescans both columns i829`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val p = playerAt(200, 100)
        p.bb = true; p.bc = true; p.ba = true; p.aT = 30; p.aU = 30
        p.collideSides(w, false)
        assertFalse(p.bb); assertFalse(p.bc); assertFalse(p.ba)
        assertEquals(0, p.aT); assertEquals(0, p.aU)
        assertEquals((p.W[0] + p.W[2]) shr 1, p.tc)
        assertEquals((p.W[1] + p.W[3]) shr 1, p.uc)
    }

    @Test fun `a() wall column flags bb i862`() {
        // column left of the box (i=W[0]-1) is solid 19 → bb + aT>=18
        val w = Slice128Test.MarkerWorld { cx, cy -> if (cx <= 9) 19 else 0 }
        val p = playerAt(200, 100)          // i = 189 → col 9
        p.collideSides(w, false)
        assertTrue(p.bb)
        assertTrue(p.aT >= 18)
    }

    @Test fun `a() both walls clears both flags i894`() {
        val w = Slice128Test.MarkerWorld(cell = 19)
        val p = playerAt(200, 100)
        p.clip = clip                         // keep W across t()
        p.collideSides(w, true)
        assertFalse(p.bb); assertFalse(p.bc)   // bb == bc → both cleared
    }

    @Test fun `floorAhead reads the cell one column into the facing i5849`() {
        val w = Slice128Test.MarkerWorld { cx, cy -> if (cx == 11 && cy == 5) 19 else 0 }
        val p = playerAt(200, 100)               // av=false → +1 col → 11, al/20 = 5
        assertTrue(p.floorAhead(w))
        val w2 = Slice128Test.MarkerWorld(cell = 0)
        assertFalse(p.floorAhead(w2))
    }

    @Test fun `ledgeHangGrab snaps and mounts into S61 g209`() {
        val w = Slice128Test.MarkerWorld { cx, cy ->
            if (cx == 12 && cy == 4) 19 else 0   // av=false → i2 = (W[2]+20)/20+1 = 12
        }
        val p = playerAt(200, 100)
        assertTrue(p.ledgeHangGrab(w))
        assertEquals(240, p.ak)                  // i2*20
        assertEquals(79, p.al)                   // i4*20-1
        assertEquals(61, p.S)
        assertEquals(40, p.aC)
    }

    @Test fun `ledgeHangGrab cell 21 defers the mount g228`() {
        val w = Slice128Test.MarkerWorld { cx, cy ->
            if (cx == 12 && cy == 4) 21 else 0   // ladder cell
        }
        val p = playerAt(200, 100)
        assertTrue(p.ledgeHangGrab(w))
        assertEquals(0, p.S, "ladder cell → bare true, no i(61)")
    }

    @Test fun `ledgeHangGrab blocked pocket fails g221`() {
        val w = Slice128Test.MarkerWorld { cx, cy ->
            if (cx == 12 && cy == 4) 19 else if (cx == 11 && cy == 4) 3 else 0
        }
        val p = playerAt(200, 100)
        assertFalse(p.ledgeHangGrab(w))          // e(i3,i4) > 0 → fail
        assertEquals(0, p.S)
    }

    @Test fun `dropHeld cascades the ab link and clears it i3684`() {
        val p = playerAt(200, 100)
        val held = Entity(14, null); held.W.fill(7)
        p.ab = held
        p.dropHeld()
        assertNull(p.ab)
        assertEquals(0, held.W[0], "ab.p() clears the held box")
    }

    @Test fun `S9 stagger decays momentum and recovers to S1 g1265`() {
        val w = Slice128Test.MarkerWorld { cx, cy -> if (cx == 11 && cy == 5) 19 else 0 }
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 9; p.ag = 512
        fsm.tick(p, Pad())
        assertEquals(256, p.ag, "ag /= 2 decay (g.java:1267)")
        assertEquals(1, p.S, "r() → i(1) (g.java:1280)")
    }

    @Test fun `S9 r() drops the ab link and the ae indicator g1277`() {
        val w = Slice128Test.MarkerWorld { cx, cy -> if (cx == 11 && cy == 5) 19 else 0 }
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 9
        p.ab = Entity(14, null)
        p.ae = Entity(6, null)
        fsm.tick(p, Pad())
        assertNull(p.ab, "H() drops ab")
        assertNull(p.ae, "G() releases ae")
    }

    @Test fun `S9 r() with no floor ahead falls g1283`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 9
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "!M() && a==null → a(0) fall (g.java:1283)")
    }

    @Test fun `S9 from idle pulses ah and rescans sides g1288`() {
        val w = Slice128Test.MarkerWorld { cx, cy -> if (cx == 11 && cy == 5) 19 else 0 }
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 9; p.Q = 0                          // came from S0
        fsm.tick(p, Pad())
        assertEquals(0, p.ah, "ah=1 pulse is transient (g.java:1290)")
    }
}
