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

class Slice215Test {
    /** slice 215 — the ax7 mouth-throw release chain. ERRATUM (slice 390):
     *  the earlier "verbatim wedge / original softlock" verdict (and the
     *  G12 erratum after it) was traced on the WRONG sprite. `i(short[])`
     *  binds `aa = k.r(k.bm[r8[8]])` for ax7 with `k.bm = {60, 66}`; the
     *  level-0 mouths aw=12 (1397,506) and aw=30 (4801,674) carry
     *  `r8[8] == 1` → clip 66, which the port did not yet decode, so they
     *  spawned on clip 60 — whose frame-0 rect sits 80 px west of the
     *  anchor. With clip 66 the capture box is centred on the mouth
     *  (`[1392,472,1408,488]`) and the chain is the designed wall crossing:
     *  - S0 `W∩playerW && !aS.f()` → `i(1)` + `aS.i(313)` swallow, the
     *    player pinned to the mouth's frame-W centre (1400,480);
     *  - record P=0 → `e.av=false` → at `r()` the release is `aS.ag = 2048`
     *    east, `ah = 1536` (i.java:15695-15710), 8 ticks later at ~(1529,500);
     *  - the throw carries him over the x1400 wall; he lands at ~(1569,579). */
    @Test fun `ax7 mouth throw clears the wall corner`() {
        val w = world()
        settleIntro(w)
        val e = w.npcs.first { it.ax == 7 && it.aw == 12 }
        keepLive(e)
        assertSame(w.clips[66], e.clip, "r8[8]==1 → k.bm[1] = clip 66")
        assertEquals(intArrayOf(1392, 472, 1408, 488).toList(), e.W.toList())
        assertFalse(e.av)                       // record P=0 -> throws east
        val p = w.player
        p.setPositionPx(e.W[0] + 4, e.W[1] + 4)
        p.refreshBoxes()
        var captured = false
        var released = false
        var releasePos: Pair<Int, Int>? = null
        for (t in 0 until 120) {
            w.tick(emptyList())
            if (!captured && p.S == 313) {
                captured = true
                assertEquals(64, p.P and 64)    // P|=64 slot-hold
            }
            if (!released && p.S == 43) {
                released = true
                releasePos = p.ak to p.al
                assertEquals(2048, p.ag)        // east throw by e.av=false
            }
        }
        assertTrue(captured, "mouth swallows the overlapping player")
        assertTrue(released, "r() releases at the last S1 frame")
        val (rx, ry) = releasePos!!
        assertTrue(rx in 1500..1560, "release x east of the wall: $rx")
        assertTrue(ry in 480..520, "release y: $ry")
        assertTrue(p.S != 79 && !(p.aO == 20 && p.aR == 20 && p.aP == 20),
            "no deep embed: S=${p.S} aO=${p.aO} aR=${p.aR} aP=${p.aP}")
        assertTrue(p.ak > 1500, "thrown clear of the wall: ${p.ak}")
    }
}
