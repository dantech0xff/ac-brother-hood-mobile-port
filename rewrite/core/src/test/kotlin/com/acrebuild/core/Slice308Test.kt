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

class Slice308Test {
    /** m7 capstone leg L — arena approach: pillar top (349,540) → run
     *  east + jump → catch sky-lift uid225@(723,521) → S235/S236 ride
     *  homing → mid-ride M_UP jump-release → arc lands on band top y560
     *  → run east → x1040 40px step vault → arena floor y519 → uid303
     *  win-fuse claim fires (kC=303 → script 304 consumes boss-3 uid307
     *  intro).
     *  Slice 370 verdict — faithful dead end. The leg's start and its
     *  band are type-2 strips on solid rows: the pillar top x300-359 is
     *  r27 cols 15-17 on r28, the band y560 is r28 cols 18-51 on r29.
     *  Standing on either is the L353d kill (g.javap.txt e() 13662-13711:
     *  `(aR==2 || aO==2 || L()) && g.a == null → ah=aj=0; g.e(0); i(50);
     *  return`, `L()` = e(ak/20, al/20) == 2), so the designed start dies
     *  on its first tick and the band landing would too. The test pins
     *  that; the arena chain past the band runs from the arena lip in
     *  Slice310Test. */
    @Test fun mission7ArenaApproach() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        for (cx in 15..17) {                       // pillar top
            assertEquals(2, w.collisionCell(cx, 27), "pillar top r27 col $cx")
            assertEquals(20, w.collisionCell(cx, 28), "pillar top floor r28 col $cx")
        }
        for (cx in 18..51) {                       // band
            assertEquals(2, w.collisionCell(cx, 28), "band r28 col $cx")
            assertEquals(20, w.collisionCell(cx, 29), "band floor r29 col $cx")
        }
        val rope = p.bM; p.bM = null; rope?.bM = null; p.aA = 0
        p.ak = 349; p.al = 540; p.N = 349 shl 8; p.av = false; p.setAnim(12)
        w.pad.e(Pad.M_RIGHT)                       // the leg's first input
        w.tick(emptyList())
        assertEquals(50, p.S, "pillar-top start: L353d type-2 kill → i(50)")
        assertEquals(0, p.x1, "g.e(0)")
        assertEquals(349, p.ak, "dies where it stands")
    }
}
