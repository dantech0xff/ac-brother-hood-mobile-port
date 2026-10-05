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

/** Slice 200 — `am()` L5d arm (g.java:687-712, proven): ag==0 standing
 *  inside a type-19 wall cell with an open side → face the open side,
 *  snap `ak` to that grid edge, enter the S63 climb-up (DOWN-held gated
 *  at the call site, L1893). Plus iface provenance sweep: `jRand`/
 *  `jNextInt` (already wired to the Java-LCG `rng`), `kBk`,
 *  bannerK `Y()`/`Z()` (now decoded to menuHasSave/menuShopCheck). */
class Slice200Test {
    private val clip by lazy { Clip.load(asset("clips/clip7/clip.acpk")) }

    @Test fun `standing in wall cell climbs on down hold`() {
        var pcx = -1; var feetCy = -1
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == pcx && cy == feetCy) 19 else 0
        })
        val fsm = PlayerFsm(w)
        val p = Entity(0, clip)
        p.setAnim(0); p.ak = 215; p.al = 100; p.refreshBoxes()
        pcx = p.ak / 20
        feetCy = (p.W[3] + 1) / 20
        p.S = 0; p.ag = 0
        val pad = Pad(); pad.commit(33024)     // DOWN held (L1893 gate)
        fsm.tick(p, pad)
        assertEquals(63, p.S, "L5d arm → i(63) climb-up")
        assertEquals(pcx * 20 + 20, p.ak,
            "both sides open → av=1 → snap to right grid edge")
        assertEquals(0, p.ah)
    }

    @Test fun `standing in wall cell without down hold does not climb`() {
        var pcx = -1; var feetCy = -1
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == pcx && cy == feetCy) 19 else 0
        })
        val fsm = PlayerFsm(w)
        val p = Entity(0, clip)
        p.setAnim(0); p.ak = 215; p.al = 100; p.refreshBoxes()
        pcx = p.ak / 20
        feetCy = (p.W[3] + 1) / 20
        p.S = 0; p.ag = 0
        fsm.tick(p, Pad())
        assertNotEquals(63, p.S, "u(33024) gate blocks am() without DOWN")
    }
}
