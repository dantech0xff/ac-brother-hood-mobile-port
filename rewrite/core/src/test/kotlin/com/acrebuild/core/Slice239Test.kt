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

class Slice239Test {
    // slice 239 — `i.F()` draw-style/FX proc (i.java:11782-14030): the
    // palette/remap/alpha dispatch, az layering, marker lines, the
    // player FX counters (gt--/iE--/bF oscillator/sparkles), and the
    // `drawStylePass()` second-pass driver (k.java:10032-10150).

    private fun ent(ax: Int, w: Level0World): Entity {
        val e = Entity(ax, null)
        e.aw = 7000 + ax
        w.npcs.add(e)
        return e
    }

    @Test fun `soldier alert anims sink to az 99`() {
        val w = world()
        val e = ent(11, w); e.S = 21
        e.drawStyleF(w)
        assertEquals(99, e.az, "L4a: ax11 S∈{21,…,168} → az=99")
    }

    @Test fun `player floats to az 100 in a locomotion anim`() {
        val w = world()
        val p = w.player; p.S = 0
        p.drawStyleF(w)
        assertEquals(100, p.az, "Ld0: unclaimed ax0 locomotion → az=100")
    }

    @Test fun `ax11 palette arm follows Z0 uniform`() {
        val w = world()
        val e = ent(11, w)
        e.Z[0] = 1; e.drawStyleF(w)
        assertEquals(1, e.palette, "L4d3: Z[0]==1 → l(1)")
        e.Z[0] = 0; e.drawStyleF(w)
        assertEquals(0, e.palette)
    }

    @Test fun `ax30 blink arm toggles palette per cGCount parity`() {
        val w = world()
        val e = ent(30, w)
        e.cGCount = 3
        e.drawStyleF(w)
        assertEquals(2, e.cGCount); assertEquals(0, e.palette)
        e.drawStyleF(w)
        assertEquals(1, e.cGCount); assertEquals(1, e.palette,
            "L4a2: odd cGCount → l(1) flash frame")
    }

    @Test fun `player arm decays gt under jC8 without a claim`() {
        val w = world()
        val p = w.player; p.gt = 5; w.iBB = false
        p.drawStyleF(w)
        assertEquals(4, p.gt, "La84: g.t-- when the flash cycle isn't running")
    }

    @Test fun `player arm suppresses gt decay while the flash runs`() {
        val w = world()
        val p = w.player; p.gt = 5
        w.iBB = true; w.iBF = 50; w.iBD = true
        p.drawStyleF(w)
        assertEquals(5, p.gt, "flash oscillator active → La84 skipped")
        assertEquals(55, w.iBF, "bD rise +5 (i.java:13100)")
    }

    @Test fun `iE decays and allocates the sparkle field in F`() {
        val w = world()
        w.iE = 30
        w.player.drawStyleF(w)
        assertEquals(29, w.iE, "La92: i.e-- inside the player arm")
        assertNotNull(w.iF); assertNotNull(w.iG); assertNotNull(w.iH)
    }

    @Test fun `ax15 S10 marker emits lines and the iR box`() {
        val w = world()
        // i.java:2980-3046 (proven): the S10 link must NOT be ax14.
        val link = ent(9, w); link.aw = 424242
        val m = ent(15, w); m.S = 10; m.Z[0] = 424242
        m.setPositionPx(500, 700); m.refreshBoxes()
        link.setPositionPx(300, 300)
        link.refreshBoxes()
        link.W[0] = 280; link.W[1] = 280; link.W[2] = 320; link.W[3] = 330
        m.drawStyleF(w)
        assertEquals(2, w.fxLines.size, "L1a7: two j.a marker lines")
        assertEquals(intArrayOf(290, 290, 310, 310).toList(), w.iR.toList(),
            "L2c8: i.r = link box ±10 (ak-10,al-10,ak+10,al+10)")
    }

    @Test fun `slow mo off tick returns 0`() {
        val w = world()
        w.iAH = true; w.iAI = 2
        w.jG = 1L
        val e = ent(11, w)
        assertEquals(0, e.drawStyleF(w), "L10f2: i.aH && j.g%aI!=0 → skip")
        w.jG = 2L
        assertEquals(1, e.drawStyleF(w))
    }

    @Test fun `ax24 S11 counter increments and caps at cz`() {
        val w = world()
        val e = ent(24, w); e.S = 11; e.cz = 3; e.cA = 2
        e.drawStyleF(w); assertEquals(3, e.cA)
        e.drawStyleF(w); assertEquals(3, e.cA, "Ldb0: cA++ cap cz")
    }

    @Test fun `ax29 remap arm follows iBy`() {
        val w = world()
        val e = ent(29, w)
        w.iBy = 3; e.S = 10
        e.drawStyleF(w)
        assertEquals(0, e.remapTable)
        assertTrue(e.j0Frame == 1 || e.j0Frame == 4,
            "L8f5: j0 flicker 1/4 by j.g%3")
        w.iBy = 1; e.drawStyleF(w)
        assertEquals(-1, e.remapTable, "L910: default → a(-1)")
    }

    @Test fun `ax11 speech window writes bubble and decrements Z20`() {
        val w = world()
        val e = ent(11, w)
        e.Z[20] = 40; e.Z[19] = 0
        e.setPositionPx(500, 700); e.refreshBoxes()
        e.drawStyleF(w)
        assertEquals(1, w.fxBubbles.size, "Lf2c: Z20>0 → bubble emit")
        assertEquals(39, e.Z[20], "Z[20]-- per draw")
    }

    @Test fun `ax0 outfit arm applies K_BO table`() {
        val w = world()
        w.kBL = 1                                    // outfit {3,1}
        w.player.drawStyleF(w)
        assertEquals(3, w.player.palette)
        assertEquals(1, w.player.remapTable)
    }

    @Test fun `drawStylePass drives F on bd entries plus kE`() {
        val w = world()
        w.iE = 30
        w.player.gt = 5
        w.drawStylePass()
        assertEquals(29, w.iE, "player F() via the second pass")
        assertEquals(4, w.player.gt)
        assertEquals(100, w.player.az)
    }

    @Test fun `drawStylePass skips ad F on ax76 and ax29 links`() {
        val w = world()
        val e = ent(11, w)
        e.Z[8] = 888                                 // ax11 in-play
        e.setPositionPx(w.kO + 200, w.kP + 120)      // camera center:
        e.refreshBoxes()                             // au=0 → in-play
        e.S = 21                                     // its own F → az=99
        val marker = Entity(29, null); marker.S = 0
        e.ad = marker
        w.iBy = 2                                    // ax29 arm → remap=0
        w.drawStylePass()
        assertEquals(99, e.az, "entry's own F() ran via the pass")
        assertEquals(-1, marker.remapTable,
            "ax29 ad-link is F()-excluded — remapTable untouched")
    }

    // -- slice 340 — draw-pass `s()` runs once per TICK, never per
    //    rendered frame (k.java:3696-3745 + the j.java:206-213 loop):
    //    advancing `k.E`/`ad`/`ae` clips at ~60fps played them ~4x too
    //    fast — the choppy-slash artifact. `drawStylePass()` is the
    //    tick-side mirror; `buildDrawList()` (the render-side call)
    //    must never advance. `a` counts advanceAnim invocations.

    @Test fun `drawStylePass advances kE once per tick, builder never`() {
        val w = world()
        val ke = w.kE ?: return                      // clip46 in world()
        ke.P = ke.P and -129                         // E.P&128==0 gate
        ke.S = 0; ke.T = 0; ke.U = 0; ke.a = 0
        w.drawStylePass()
        assertEquals(1, ke.a, "k.E s() once per tick pass")
        w.buildDrawList()
        assertEquals(1, ke.a, "render-side builder never advances")
        w.drawStylePass()
        assertEquals(2, ke.a)
    }

    @Test fun `drawStylePass advances player ae once, builder never`() {
        val w = world()
        val ae = Entity(14, w.clipFor(46) ?: return).apply { P = 0 }
        w.player.ae = ae
        w.player.P = w.player.P and -129             // aS.P&128==0 gate
        w.drawStylePass()
        assertEquals(1, ae.a, "aS.ae s() once per tick pass")
        w.buildDrawList()
        assertEquals(1, ae.a, "builder never advances")
    }

    // -- slice 342 — review finding: the in-play arm also steps each
    //    visible NPC's linked `ae` (k.java:3659-3666 `d(r018.ae);
    //    r018.ae.s()`) — not just the player's. Without it a visible
    //    NPC-owned FX anim (e.g. a wisp trail) steps ~4x too slow.

    @Test fun `drawStylePass advances a visible NPC ae once, builder never`() {
        val w = world()
        val e = ent(11, w)
        e.Z[8] = 888                                 // ax11 in-play
        e.setPositionPx(w.kO + 200, w.kP + 120)
        e.refreshBoxes()
        val ae = Entity(14, w.clipFor(46) ?: return).apply { P = 0 }
        e.ae = ae
        w.drawStylePass()
        assertEquals(1, ae.a, "visible NPC ae s() once per tick pass")
        w.buildDrawList()
        assertEquals(1, ae.a, "render-side builder never advances")
        w.drawStylePass()
        assertEquals(2, ae.a)
    }

    @Test fun `drawStylePass skips ae advance for an off-play NPC`() {
        val w = world()
        val e = ent(11, w)
        e.setPositionPx(w.kO - 4000, w.kP - 4000)    // off camera → v() false
        e.refreshBoxes()
        val ae = Entity(14, w.clipFor(46) ?: return).apply { P = 0 }
        e.ae = ae
        w.drawStylePass()
        assertEquals(0, ae.a, "off-play NPC ae never enters the arm")
    }

    // -- slice 341 — `bU` mapOf held a duplicate-key tail block; Kotlin
    //    last-wins clobbered 117→"NEW GAME" (should be "QUICK PLAY" per
    //    pack-14 entry-000 idx117) and 123→"CONTROL STYLE" (should be
    //    "MODE"). jc2's first row was mislabelled. Pin the verbatim
    //    values so a stray second block can't regress them.

    @Test fun `bU row labels match pack-14 entry-000 verbatim`() {
        val w = world()
        assertEquals("QUICK PLAY", w.d0(117))
        assertEquals("MODE", w.d0(123))
        assertEquals("NEW GAME", w.d0(1))
        assertEquals("THE GAME DATA HAS BEEN DELETED.", w.d0(121))
    }
}
