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

class Slice196Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S59 bare goto leaves state alone on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 59
        fsm.tick(p, Pad())
        assertEquals(59, p.S, "bare goto L353d — no settle")
    }

    @Test fun `S164 bare goto leaves state alone`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 164
        fsm.tick(p, Pad())
        assertEquals(164, p.S)
    }

    @Test fun `S209 without a mount flings airborne`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 209
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "g.a==null → a(0)")
    }

    @Test fun `S209 rides the mount and centers on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 209
        val a = Entity(66, null); a.S = 9
        a.W[0] = 150; a.W[1] = 300; a.W[2] = 350; a.W[3] = 400
        p.standingOn = a
        fsm.tick(p, Pad())
        assertEquals(301, p.al, "non-S11/12 mount → al = W[1]+1")
        assertEquals(250, p.ak, "ak centers on mount W")
        assertEquals(0, p.S, "anim end → i(0)")
    }

    @Test fun `S209 ax66 S12 exit picks S228 on positive Z0`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 209
        val a = Entity(66, null); a.S = 12; a.Z[0] = 3; a.al = 500
        p.standingOn = a
        fsm.tick(p, Pad())
        assertEquals(228, p.S, "ax66 S12 Z[0]>0 → i(228)")
    }

    @Test fun `S209 ax66 S12 exit picks S358 on zero Z0`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 209
        val a = Entity(66, null); a.S = 12; a.Z[0] = 0
        p.standingOn = a
        fsm.tick(p, Pad())
        assertEquals(358, p.S, "ax66 S12 Z[0]<=0 → i(358)")
    }

    @Test fun `S216 windup drives ag during frames two three`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 216; p.T = 3; p.av = true
        fsm.tick(p, Pad())
        assertEquals(217, p.S, "r() → i(217)")
        assertTrue(p.P and 64 != 0, "P|=64")
        assertEquals(-2560, p.ag, "ag=-5120 then >>1 on r()")
    }

    @Test fun `S216 outside frames two three zeroes ag`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 216; p.T = 5; p.av = false
        fsm.tick(p, Pad())
        assertEquals(0, p.ag, "T∉[2,3] → ag=0 (then halved)")
        assertEquals(217, p.S)
    }

    @Test fun `S217 floor land zeroes meter and lands in S50`() {
        val w = Slice128Test.MarkerWorld(cell = 2)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 217; p.T = 1
        fsm.tick(p, Pad())
        assertEquals(0, p.ah); assertEquals(0, p.aj)
        assertEquals(0, p.x1, "e(0) → x[1]=0")
        assertEquals(50, p.S, "floor land → i(50)")
    }

    @Test fun `S217 on aZ support rescans with a(true) and clears P64`() {
        val w = Slice128Test.MarkerWorld(cell = 5)
        val fsm = PlayerFsm(w)
        // real player clip so r() is false mid-anim (clipless entities
        // would fall through to the `r() → i(0)` below).
        val p = Entity(0, Clip.load(asset("clips/clip0/clip.acpk")))
        p.ak = 200; p.al = 100
        p.W[0] = 190; p.W[2] = 210; p.W[1] = 80; p.W[3] = 100
        p.S = 217; p.T = 1; p.P = 64
        fsm.tick(p, Pad())
        // g.javap.txt e() 12329-12334: `bd = 1; a(true)` — #282 is
        // i.a(Z)V, the side rescan (its x() rewrites bd), not g.a(int):
        // the dive stays in S217 (slice 369).
        assertEquals(217, p.S, "a(true) keeps S217 — no S43 fling")
        assertEquals(100, p.al, "no g.a(int) al += 10")
        // i.javap.txt x() 29-42: `bd = (ah != 0)` — the `bd = 1` set just
        // before a(true) is overwritten by its x() (ah == 0 here)
        assertFalse(p.bd, "a(true)'s x() rewrites bd = ah != 0")
        assertTrue(p.P and 64 == 0, "P&=~64")
        assertEquals(0, p.ah); assertEquals(0, p.aj)
    }

    @Test fun `S258 up edge jumps to S259`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 258
        val pad = Pad(); pad.bB = 16388
        fsm.tick(p, pad)
        assertEquals(259, p.S)
    }

    @Test fun `S258 left edge faces left into S261`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 258; p.av = false
        val pad = Pad(); pad.bB = 4112
        fsm.tick(p, pad)
        assertTrue(p.av, "left edge → av=1")
        assertEquals(261, p.S)
    }

    @Test fun `S258 down edge flings with double drop`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 258; p.al = 100
        val pad = Pad(); pad.bB = 33024
        fsm.tick(p, pad)
        assertEquals(43, p.S)
        assertEquals(120, p.al, "a(0)+10 then +10 again — verbatim")
    }

    @Test fun `S259 anim end launches straight up`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 259; p.ag = 999
        fsm.tick(p, Pad())
        assertEquals(263, p.S)
        assertEquals(0, p.ag); assertEquals(-7680, p.ah)
    }

    @Test fun `S261 anim end launches sideways`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 261; p.av = false
        fsm.tick(p, Pad())
        assertEquals(264, p.S)
        assertEquals(3072, p.ag); assertEquals(-7680, p.ah)
    }

    @Test fun `S260 anim end returns to perch idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 260
        fsm.tick(p, Pad())
        assertEquals(258, p.S, "r() → i(258)")
    }

    @Test fun `S263 arms cw and upgrades to S265 on rise end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 263; p.ah = 1
        fsm.tick(p, Pad())
        assertTrue(p.cw, "cw=1")
        assertEquals(2560, p.aj)
        assertEquals(265, p.S, "ah>=0 → 263→i(265)")
    }

    @Test fun `S264 upgrades to S266 on rise end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 264; p.ah = 0
        fsm.tick(p, Pad())
        assertEquals(266, p.S)
    }

    @Test fun `S265 anim end flings airborne`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 265
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "r() → a(0)")
    }

    @Test fun `S267 without a carry target bails to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 267
        fsm.tick(p, Pad())
        assertEquals(0, p.gt, "ae() fails → g.t=0")
        assertEquals(0, p.S, "→ i(0)")
    }

    @Test fun `S267 walks toward a distant ax27 target`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 267; p.aZ = true
        val t = Entity(27, null); t.X[0] = 300; t.X[2] = 320
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(1024, p.ag, "dx>4 → walk +1024")
        assertFalse(p.av)
        assertEquals(100, p.gt)
    }

    @Test fun `S267 snaps into S268 inside four px`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 267; p.aZ = true
        val t = Entity(27, null); t.X[0] = 196; t.X[2] = 206
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(201, p.ak, "snap to target centre")
        assertEquals(268, p.S)
        assertEquals(1, t.S, "af.i(1)")
    }

    @Test fun `S267 ax10 target goes to S291`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 267; p.aZ = true
        val t = Entity(10, null); t.W[0] = 196; t.W[2] = 206
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(291, p.S, "ax10 → i(291)")
    }

    @Test fun `S268 mid anim only zeroes velocity`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 268; p.T = 0
        p.ag = 500; p.ah = 500
        val t = Entity(27, null); t.S = 2
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(268, p.S, "not near anim end → stay")
    }

    @Test fun `S268 orders the victim into S2 once`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 268; p.T = 0
        val t = Entity(27, null); t.S = 0
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(2, t.S, "af.i(2)")
        assertEquals(-2, p.az, "az=-2 consumed")
    }

    @Test fun `S269 drops the carry and releases af`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 269
        val t = Entity(27, null); t.S = 0
        p.af = t
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "r() → i(0)")
        assertEquals(2, t.S, "af.i(2)")
        assertNull(p.af, "af released")
    }

    @Test fun `S270 binds the grab and advances on anim end`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 270
        val gg = Entity(11, null); gg.S = 0
        p.g = gg
        fsm.tick(p, Pad())
        assertEquals(271, p.S, "r() → i(271)")
        assertEquals(145, gg.S, "g.g → i(145) after the S133 force")
        assertEquals(5, p.bl)
    }

    @Test fun `S270 without a grab target falls to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 270
        fsm.tick(p, Pad())
        assertEquals(0, p.S)
    }

    @Test fun `S271 mash win releases and credits the kill`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 271; p.bl = 11; p.aw = 5; p.P = 64
        val gg = Entity(11, null); gg.S = 134
        p.g = gg
        val pad = Pad(); pad.bB = 0      // no v(65568) — bl decays
        fsm.tick(p, pad)
        assertEquals(0, p.S, "bl>=10 → i(0)")
        assertEquals(135, gg.S, "g.g → i(135)")
        assertEquals(0, gg.aB)
        assertEquals(1, w.kAp[0], "k.e(0,aw) kill credit")
        assertEquals(1, w.kAp[3], "k.o(3)")
        assertTrue(p.P and 64 == 0, "P&=~64")
    }

    @Test fun `S271 mash decay below zero releases the victim`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 271; p.bl = 0
        val gg = Entity(11, null); gg.S = 134
        p.g = gg
        fsm.tick(p, Pad())
        assertEquals(0, p.S)
        assertEquals(5, gg.S, "bl<0 → g.g.i(5)")
        assertEquals(1, gg.aA)
    }

    @Test fun `S280 settles into S38`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 280
        fsm.tick(p, Pad())
        assertEquals(38, p.S)
    }

    @Test fun `S286 fire edge flips to S287`() {
        val w = Slice128Test.MarkerWorld(cell = 5)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 286
        val pad = Pad(); pad.bB = 65568
        fsm.tick(p, pad)
        assertEquals(287, p.S, "R=287 → i(R)")
        assertEquals(-1, p.R)
    }

    @Test fun `S287 anim end without fire returns to idle`() {
        val w = Slice128Test.MarkerWorld(cell = 5)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 287
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "R=-1 → i(0)")
    }

    @Test fun `S286 airborne without mount flings`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 286; p.aZ = false
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "!aZ && no mount → a(0)")
    }

    @Test fun `S291 near end hands off on fire edge`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 291; p.T = 0
        val t = Entity(10, null); t.S = 0
        p.af = t
        val gg = Entity(11, null); gg.ak = p.ak + 10
        // a live, level target: az() runs in e()'s head (g.javap.txt e()
        // 617, slice 365) before this arm and drops a g with aB <= 0 or
        // |Δal| >= 60.
        gg.aB = 1; gg.al = p.al
        p.g = gg
        // S291 is one of the az() rebind states (@66-101: `g = null`, then
        // the scan binds the last candidate in `k.bd` order within 440px,
        // facing or not) — the target has to be on the draw list.
        w.npcs.add(gg)
        val pad = Pad(); pad.bB = 65568
        fsm.tick(p, pad)
        assertEquals(270, p.S, "r9&&v(65568)&&g.g → i(270)")
        assertNull(p.af, "af released")
    }

    @Test fun `S291 non softkey edge exits to S285`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 291; p.T = 0
        val t = Entity(10, null); t.S = 0
        p.af = t
        val pad = Pad(); pad.bB = 16388
        fsm.tick(p, pad)
        assertEquals(285, p.S, "k.u() edge → i(285)")
        assertNull(p.af)
    }

    @Test fun `S313 bare return does nothing`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 313; p.ag = 777
        fsm.tick(p, Pad())
        assertEquals(313, p.S)
        assertEquals(777, p.ag)
    }
}
