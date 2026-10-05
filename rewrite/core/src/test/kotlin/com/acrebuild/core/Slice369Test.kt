package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 369 — the `g.e()` follow-ups of slice 365 (F2-F13), each read off
 * g.javap.txt e() (offsets in the test names / KDoc). Every test here
 * fails with the slice-368 code path of its item put back (checked item by
 * item while the fixes were made: 34 of 35 fail) — except the F11 one,
 * which pins a ported step that is not observable there.
 */
class Slice369Test {
    companion object {
        /** The converted player clip (k.z[75] pack-3 bank). */
        val clip0: Clip by lazy {
            Clip.load(java.io.File("../generated/clips/clip0/clip.acpk").readBytes())
        }

        /**
         * A synthetic ACPK: [anims] anims of [frames] frames each, every
         * frame `dur` ticks and one object whose W rect is 20×40 above the
         * anchor (`W = [ak-10, al-40, ak+10, al]`). The shipped clip0 has
         * 5-6 frames in S67-69 and one in S112-115, so `T` never reaches the
         * cj/ck window frames (5/6/9/100) — the window paths need longer
         * anims to show.
         */
        fun synthClip(anims: Int = 400, frames: Int = 10, dur: Int = 2,
                      tall: Set<Int> = emptySet()): Clip {
            val b = java.io.ByteArrayOutputStream()
            fun u8(v: Int) = b.write(v and 0xFF)
            fun u16(v: Int) { u8(v); u8(v shr 8) }
            fun u32(v: Int) { u16(v); u16(v shr 16) }
            b.write("ACPK".toByteArray(Charsets.US_ASCII)); u8(1)
            u16(2); u16(20); u16(40); u8(0); u16(20); u16(60); u8(0)   // 2 modules
            u16(anims)
            for (a in 0 until anims) { u16(a * frames); u16(frames) }
            u32(anims * frames)
            for (a in 0 until anims) repeat(frames) {
                u16(if (a in tall) 1 else 0); u8(dur); u16(0); u16(0); u8(0)
            }
            u16(2)                                                // objects
            u16(0); u16(2); u16(0); u16(0)                        // 0: rects 0..1
            u16(2); u16(2); u16(0); u16(0)                        // 1: rects 2..3
            u32(4)
            u16(-10); u16(-40); u16(20); u16(40)                  // obj 0 W
            u16(-10); u16(-40); u16(20); u16(40)                  // obj 0 X
            u16(-10); u16(-60); u16(20); u16(60)                  // obj 1 W
            u16(-10); u16(-60); u16(20); u16(60)                  // obj 1 X
            u32(2)
            u16(-10); u16(-40); u16(20); u16(40)                  // bounds 0
            u16(-10); u16(-60); u16(20); u16(60)                  // bounds 1
            u32(0)                                                // placements
            return Clip.load(b.toByteArray())
        }
        val synth: Clip by lazy { synthClip() }
    }

    /** MarkerWorld plus the statics the `e()` head and the J&4 block read. */
    open class HeadWorld(cell: Int = 0, cellFn: ((Int, Int) -> Int)? = null) :
        Slice128Test.MarkerWorld(cell, cellFn) {
        override var kAm = false
        override var kDd = false
        override var gR = false
        override var kC: Entity? = null
        override val kM = IntArray(4)
        override var iAH = false
        override var kAw = 0
        /** `k.cm == 1` — `k.k()`. */
        var touchPad = false
        override val mounted: Boolean get() = touchPad
        override fun setMounted() { touchPad = true }   // Level0World: `cm = 1`
        var touchOnHand = false
        override fun touchNearView(e: Entity, r: Int) = touchOnHand
        override val camRect = intArrayOf(0, 0, 400, 240)
        val sfxCalls = mutableListOf<Int>()
        override fun sfx(id: Int) { sfxCalls += id }
    }

    private fun resetStatics() {
        Entity.grabLatch = false; Entity.gq = false; Entity.gf = null
        Entity.gE = false; Entity.icu = false; Entity.entBq = 0
        Entity.at = null
    }

    /** Player at (ak, al); with a clip the box comes from it, else W is
     *  staged as `[ak-10, al-20, ak+10, al]`. */
    private fun playerAt(ak: Int, al: Int, clip: Clip? = null, s: Int = 0): Entity {
        val p = Entity(0, clip)
        p.ak = ak; p.al = al; p.av = false
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        if (clip != null) p.setAnim(s) else p.S = s
        p.T = 0; p.U = 0
        if (clip != null) p.refreshBoxes()
        return p
    }

    /** A floor of solid 20 from row [row] down. */
    private fun floorFrom(row: Int): (Int, Int) -> Int = { _, cy -> if (cy >= row) 20 else 0 }

    private fun press(mask: Int): Pad = Pad().also { it.bB = mask; it.bC = mask }
    private fun hold(mask: Int): Pad = Pad().also { it.bC = mask }

    // ---- F2: S311/S312 (13423-13450) ------------------------------------

    /** 13425 is `invokevirtual #282 i.a:(Z)V` — the side rescan. The arm
     *  holds S311 until `r()`; the port flung the player into S43. */
    @Test fun `F2 S311 rescans with a(true) and holds until r()`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, synth, 311)
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(311, p.S, "a(true), not g.a(1) → S43")
        assertEquals(100, p.al, "no g.a(int) al += 10")
    }

    /** 13428-13446: `r() → ah = ag = 0; l()` — the grounded input helper
     *  runs (RIGHT held → ax() → S12); the port's `l()` was "dead". */
    @Test fun `F2 S312 at r() runs l()`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, s = 312)               // feet in row 4, floor row 5
        p.ag = 700
        PlayerFsm(w).tick(p, hold(Pad.M_RIGHT))
        assertEquals(12, p.S, "l() → u(8256) facing right → ax() → i(12)")
        assertEquals(2560, p.ag)
    }

    // ---- F3: S217 (12312-12365) -----------------------------------------

    /** 12332-12334 `bd = 1; a(true)` — the same `#282` rescan; the port
     *  called `flingAirborne(1)`. */
    @Test fun `F3 S217 on footing rescans instead of flinging`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 217)
        p.T = 1; p.P = 64
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(217, p.S, "a(true) keeps S217 (no S43)")
        assertEquals(99, p.al, "no al += 10")
        assertEquals(0, p.P and 64, "P &= ~64")
        assertEquals(0, p.ah); assertEquals(0, p.aj)
    }

    // ---- F4: a(0) is g.a(int), not i(0) -----------------------------------

    /** S235 unbound, 5515-5517: `r() → a(0)` = the masked S43 fall. */
    @Test fun `F4 S235 unbound end falls through a(0)`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 235)              // no clip: r() holds
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "a(0) → i.a(43,32)")
        assertEquals(110, p.al, "g.a(int): al += 10")
        assertEquals(1536, p.aj)
    }

    /** S236 above an S8 crate, 5770-5776: `g.a = null; a(0)`. */
    @Test fun `F4 S236 above its crate releases through a(0)`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 236)
        val crate = Entity(51, null); crate.S = 8; crate.al = 120
        p.bindAc(crate)
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "a(0), not i(0)")
        assertNull(p.ac, "g.a(int) clears ac")
        assertNull(p.ga)
    }

    /** S228 end, 12746-12755: `a(0); i(252)` — the fall's `al += 10`,
     *  `aj = 1536` and link drop survive into S252. */
    @Test fun `F4 S228 end falls through a(0) before i(252)`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 228)
        val crate = Entity(51, null); crate.S = 8
        p.bindAc(crate)
        PlayerFsm(w).tick(p, Pad())
        assertEquals(252, p.S)
        assertEquals(110, p.al, "g.a(int): al += 10")
        assertEquals(1536, p.aj, "g.a(int): aj = 1536")
        assertNull(p.ac)
    }

    /** S257 end, 12853-12855: the drop ends in `a(0)` (S43), not S0. */
    @Test fun `F4 S257 drop ends in a(0)`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, clip0, 257)
        p.T = clip0.frameCount(257) - 1; p.U = clip0.frameDuration(257, p.T) - 1
        assertTrue(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "S257 r() → a(0)")
        assertEquals(1536, p.aj)
    }

    // ---- F5: the combo arm (10217-10616) ----------------------------------

    /** 10598: with nothing queued the end calls `l()` (RIGHT held →
     *  run), not `i(0)`. */
    @Test fun `F5 combo end with nothing queued runs l()`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, s = 67)                // no clip: r() holds
        p.T = 4
        PlayerFsm(w).tick(p, hold(Pad.M_RIGHT))
        assertEquals(12, p.S, "l() → ax() → i(12)")
    }

    /** 10603-10613: `T==2 → k.A(10)` runs outside the `cl || r()` block. */
    @Test fun `F5 T2 combo sfx plays mid-anim`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 67)
        p.T = 2
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(67, p.S)
        assertTrue(10 in w.sfxCalls, "k.A(10) at T==2")
    }

    /** 10420-10433: `aj()` runs every tick — a queued `R` re-reads
     *  `cl = T >= 6` (cj row 0) and fires at the window without a tap. */
    @Test fun `F5 a queued R fires at the window without a tap`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 67)
        p.T = 6; p.R = 68; p.cl = false
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(68, p.S, "cl = T >= 6 → i(R)")
        assertEquals(-1, p.R)
    }

    /** g.javap.txt aj() 0-24: `ck` is tried only when `cj` matched
     *  nothing. With R queued, cj's row 0 sets `cl = T >= 6`; the port
     *  then ran ck too, which reset `cl = T >= 9`. */
    @Test fun `F5 aj tries ck only when cj fails`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 67)
        p.T = 7; p.R = 68
        PlayerFsm(w).tick(p, press(Pad.M_CONTEXT))
        assertEquals(68, p.S, "cj row 0: cl = 7 >= 6 → i(68)")
    }

    /** 10450-10480: `R == 112` on an ax51 crate inside the window waits
     *  for `r()` — `goto 13629` keeps R, cl and the anim. */
    @Test fun `F5 R112 on a crate holds until r()`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 69)
        p.T = 7; p.R = 112
        val crate = Entity(51, null); crate.S = 0
        crate.W[0] = 150; crate.W[1] = 100; crate.W[2] = 250; crate.W[3] = 140
        p.ga = crate
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(69, p.S, "!r() → goto 13629: no i(112)")
        assertEquals(112, p.R)
        assertTrue(p.cl)
    }

    /** 10240-10258: the footing loss is `g.a(0)` — `i.a(43,32)` with its
     *  mask-32 re-centre on the hitbox midline, not a bare `i(43)`. */
    @Test fun `F5 combo footing loss is the masked a(0)`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        // S43's box is 60 tall, S67's 40: the mask-32 re-centre moves al
        // by the midline difference (80 - 70 = 10) before the +10.
        val p = playerAt(200, 100, synthClip(tall = setOf(43)), 67)
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S)
        assertEquals(120, p.al, "a(43,32): al += u - mid(W43) = +10, then al += 10")
    }

    // ---- F6: S0 enters at 6089 i.O() ----------------------------------------

    @Test fun `F6 S0 runs i_O, S1 does not`() {
        resetStatics()
        for ((s, cleared) in listOf(0 to true, 1 to false)) {
            val w = HeadWorld(cellFn = floorFrom(5))
            w.iAH = true; w.kAw = 7
            val p = playerAt(200, 99, s = s)
            PlayerFsm(w).tick(p, Pad())
            assertEquals(!cleared, w.iAH, "S$s: i.O() clears aH")
        }
    }

    // ---- F7: the grounded arm (6092-6357) -----------------------------------

    /** 6092-6116: embedded → `i(79); goto 13629` — no `l()`: `bM`, `aF`
     *  and the `cp/cq/z` latches stay as the head left them. */
    @Test fun `F7 embedded grounded arm ends at i(79) without l()`() {
        resetStatics()
        val w = HeadWorld(cell = 20)
        val p = playerAt(200, 100, s = 0)
        val held = Entity(13, null)
        p.bM = held; p.aF = 1
        PlayerFsm(w).tick(p, Pad())
        assertEquals(79, p.S)
        assertSame(held, p.bM, "l() did not run (it clears bM)")
        assertEquals(1, p.aF, "l() did not run (it clears aF)")
        assertFalse(p.cq, "no l() → cq stays clear")
    }

    /** A thin floor: row [row] solid, everything else open. */
    private fun thinFloor(row: Int): (Int, Int) -> Int = { _, cy -> if (cy == row) 20 else 0 }

    /** 6119-6267 then 6336: the S79 vault-drop `a(257,8)` does not end the
     *  arm; `l()` runs on (S257, DOWN held, `ag == 0`) into `aw()`, whose
     *  `!aZ` reads the shifted probe (`aR == 0`) → `a(0)`. The port
     *  returned after the vault. */
    @Test fun `F7 the S79 vault runs on into l() and falls`() {
        resetStatics()
        val p = playerAt(200, 100, synth, 79)            // W = [190,60,210,100]
        val w = HeadWorld(cellFn = thinFloor((p.W[3] + 1) / 20))
        PlayerFsm(w).tick(p, press(Pad.M_DOWN))
        assertEquals(43, p.S, "a(257,8) → l() → aw() → !aZ → a(0)")
    }

    /** The other grounded states vault only through `l()` → `aw()`: a
     *  running S12 with DOWN slides to S32 (`ag != 0 → i(32)`). The port's
     *  shared `ledgeDrop257` vaulted any grounded state. */
    @Test fun `F7 a running S12 DOWN press slides instead of vaulting`() {
        resetStatics()
        val p = playerAt(200, 100, synth, 12)
        p.ag = 2560
        val w = HeadWorld(cellFn = thinFloor((p.W[3] + 1) / 20))
        PlayerFsm(w).tick(p, press(Pad.M_DOWN))
        assertEquals(32, p.S, "l(): u(33024) && ag != 0 → i(32)")
    }

    /** 6336-6357: `!l()` → `ab = null; cq = 0; a(0)` — `z` keeps `l()`'s
     *  1 and `ab` is dropped. */
    @Test fun `F7 the airborne l() exit drops ab`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 79)
        val ab = Entity(16, null); ab.S = 0
        p.ab = ab
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S)
        assertNull(p.ab, "6343 ab = null")
    }

    // ---- F8: S5 (4689-4827) -------------------------------------------------

    /** 4689-4694: `i.O(); bM = null` at the head. */
    @Test fun `F8 S5 runs i_O and drops bM`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.iAH = true
        val p = playerAt(200, 99, synth, 5)
        p.bM = Entity(13, null)
        PlayerFsm(w).tick(p, Pad())
        assertFalse(w.iAH, "i.O()")
        assertNull(p.bM)
        assertEquals(5, p.S)
    }

    /** 4764-4793: the jump press `i(21); ab = null` falls into `u(94324)
     *  → l()` (UP held), whose `aw()` settles the player; the post-tail
     *  then takes the UP edge into `i(233)`. */
    @Test fun `F8 the S5 jump press runs on into l() and the post-tail`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, s = 5)
        p.ab = Entity(16, null)
        PlayerFsm(w).tick(p, press(Pad.M_UP))
        assertNull(p.ab, "4770 ab = null")
        assertEquals(233, p.S, "l() arms cq; the jump tail takes the UP edge")
    }

    /** 4736-4761: `v(2) → av = true`, else `v(8) → av = false`. */
    @Test fun `F8 S5 tap edges pick the facing with else-if`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 5)
        PlayerFsm(w).tick(p, press(Pad.M_TAP_L or Pad.M_TAP_R))
        assertTrue(p.av, "v(2) wins")
        assertEquals(21, p.S)
    }

    // ---- F9: e() head 533-631 -----------------------------------------------

    @Test fun `F9 g_i re-arms on every non-S43 tick`() {
        resetStatics()
        for ((s, armed) in listOf(0 to true, 43 to false)) {
            val w = HeadWorld(cellFn = floorFrom(5))
            w.iFlag = false
            val p = playerAt(200, 99, s = s)
            PlayerFsm(w).tick(p, Pad())
            assertEquals(armed, w.iFlag, "S$s: 621-631 S != 43 → g.i = true")
        }
    }

    /** 533-595: `k.am` outside S183/184/311 → `k.p(); i.O()` and the held
     *  `i.aN` is reset (`aB = 0; i.d(aN); aN = null`). */
    @Test fun `F9 k_am is released at the head outside the finishers`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.kAm = true; w.iAH = true
        val v = Entity(11, null); v.S = 144; v.aB = 50
        w.lockTarget = v
        val p = playerAt(200, 99, s = 1)                 // S1: no 6089 i.O() of its own
        PlayerFsm(w).tick(p, Pad())
        assertFalse(w.kAm, "k.p()")
        assertFalse(w.iAH, "i.O()")
        assertNull(w.lockTarget)
        assertEquals(0, v.aB); assertEquals(0, v.S, "i.d(ax11) → i(0)")

        val w2 = HeadWorld(cellFn = floorFrom(5))
        w2.kAm = true
        // the S184 arm's own end-gate is `r() || aN == null` (raw bytes @4496-4509, slice 416): with a
        // live victim and the anim still running it keeps the lock, without one it releases it
        val v2 = Entity(11, null); v2.S = 107; v2.aB = 50
        w2.lockTarget = v2
        val p2 = playerAt(200, 99, synth, 184)
        PlayerFsm(w2).tick(p2, Pad())
        assertTrue(w2.kAm, "S184 keeps the lock")
    }

    /** 598-614: a dead `i.aN` (`P()`: aB <= 0) is dropped at the head. */
    @Test fun `F9 a dead lock target is dropped at the head`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val v = Entity(11, null); v.aB = 0
        w.lockTarget = v
        PlayerFsm(w).tick(playerAt(200, 99, s = 1), Pad())
        assertNull(w.lockTarget)
    }

    // ---- F10: the J&4 block (13739-14348) -----------------------------------

    private fun victimInFront(w: HeadWorld, p: Entity): Entity {
        val g = Entity(11, null)
        g.ak = p.ak + 30; g.al = p.al; g.aB = 100; g.Z[19] = 1
        g.W[0] = g.ak - 10; g.W[1] = g.al - 40; g.W[2] = g.ak + 10; g.W[3] = g.al
        p.g = g
        return g
    }

    /** 14140-14158: the press is `v(65568) || (!k.k() && V())` — with the
     *  touch pad off, a touch on the hand fires the lunge. */
    @Test fun `F10 a touch on the hand fires the J4 lunge`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.touchPad = false; w.touchOnHand = true
        val p = playerAt(200, 99, synth, 0)              // c() divides by the anim length
        val g = victimInFront(w, p)
        p.gJ = 4
        p.ae = Entity(14, null)                          // the hand
        PlayerFsm(w).tick(p, Pad())
        assertSame(g, p.F, "c(g) lunge")
    }

    /** 13889-13962: the Z[0]==4 cart arm needs `|ak - at.ak| >=
     *  (W[2]-W[0]) << 1`; closer, r2 = 0 and nothing is offered. */
    @Test fun `F10 the Z4 cart mount needs a run-up`() {
        resetStatics()
        for ((dx, offered) in listOf(30 to false, 50 to true)) {
            resetStatics()
            val w = HeadWorld(cellFn = floorFrom(5))
            val p = playerAt(200, 99, s = 0)            // W width 20 → band 40
            val m = Entity(72, null)
            m.ak = p.ak + dx; m.al = 99; m.Z[0] = 4; m.Z[4] = 0
            m.W[0] = m.ak - 10; m.W[1] = 60; m.W[2] = m.ak + 10; m.W[3] = 99
            m.Y[0] = m.ak - 10; m.Y[1] = 60; m.Y[2] = m.ak + 10; m.Y[3] = 99
            Entity.at = m
            p.gJ = 4
            PlayerFsm(w).tick(p, Pad())
            assertEquals(offered, p.gcm, "dx=$dx: r98 → g.cm")
        }
        resetStatics()
    }

    /** 14262-14312: with `k.k()`, `T() → U()` and then always `a(8, ak,
     *  al-85)` — a showing hand is swapped for the marker. */
    @Test fun `F10 the touch-pad r98 tail replaces a showing hand`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.touchPad = true
        val p = playerAt(200, 99, s = 0)
        victimInFront(w, p)
        p.gJ = 4
        val hand = Entity(14, null)                      // clipFor(74) == null → T()
        p.ae = hand
        PlayerFsm(w).tick(p, Pad())
        assertNotNull(p.ae)
        assertNotSame(hand, p.ae, "U() released the hand, a(8,…) spawned the marker")
    }

    /** 14315 is `putstatic #46 g.cm` — the r98 tail leaves `k.cm` alone. */
    @Test fun `F10 the r98 tail sets g_cm, not k_cm`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.touchPad = false
        val p = playerAt(200, 99, s = 0)
        victimInFront(w, p)
        p.gJ = 4
        PlayerFsm(w).tick(p, Pad())
        assertTrue(p.gcm, "g.cm = 1")
        assertFalse(w.touchPad, "k.cm untouched")
    }

    // ---- F11: S199 a(S,4) (6385-6410) ---------------------------------------

    /** Not an observable divergence: the mask-4 snap moves `ak` by
     *  `t - (W[0]+W[2])/2`, and `t` is the box centre the head's
     *  `a(an())` wrote (i.a(Z) 689-710) with nothing in between moving
     *  `ak`. Pinned: the wall stop leaves `ak` where it was. */
    @Test fun `F11 the S199 wall-stop snap is a no-op after the head rescan`() {
        resetStatics()
        val p = playerAt(209, 99, synth, 199)            // W = [199,59,219,99]
        val w = HeadWorld(cellFn = { cx, cy -> if (cy >= 5 || cx == 11) 20 else 0 })
        p.ag = 2560
        PlayerFsm(w).tick(p, Pad())
        assertEquals(0, p.ag, "y() && ag != 0 → ag = 0")
        assertEquals(209, p.ak, "a(S,4): ak += t - box centre = 0")
        assertEquals(209, p.tc)
    }

    // ---- F12: S21/S233 (7222-7362) ------------------------------------------

    @Test fun `F12 S233 jumps at once`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, synth, 233)
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        assertEquals(22, p.S, "S233 → i(22) without waiting for r()")
        assertEquals(-5120, p.ah); assertEquals(2048, p.ag)
    }

    @Test fun `F12 the crate half-jump and the ax43 claim push`() {
        resetStatics()
        val w = HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 21)              // no clip: r() holds
        val crate = Entity(51, null)
        p.ga = crate
        PlayerFsm(w).tick(p, Pad())
        assertEquals(-2560, p.ah, "g.a ax51 → ah = -2560")

        resetStatics()
        val p2 = playerAt(200, 100, s = 21)
        val ride = Entity(43, null)
        ride.ca = 0; ride.scriptStep = 0                 // i.ab(): claim running
        p2.ga = ride
        PlayerFsm(HeadWorld(cell = 0)).tick(p2, Pad())
        assertEquals(1024, p2.ag, "g.a ax43 && ab() → ag = ±1024")
        assertEquals(-5120, p2.ah)
    }

    // ---- F13: e() head 0-163 ------------------------------------------------

    /** 0-19: a bound `k.C` stops `e()` unless the player has `P & 512`. */
    @Test fun `F13a a bound k_C stops e() at its head`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.kC = Entity(5, null)
        val p = playerAt(200, 99, s = 0)
        PlayerFsm(w).tick(p, hold(Pad.M_RIGHT))
        assertEquals(0, p.S, "e() returned before the switch")
        assertEquals(0, w.kM[2], "and before k.l()")

        p.P = p.P or 512
        PlayerFsm(w).tick(p, hold(Pad.M_RIGHT))
        assertEquals(12, p.S, "P & 512 lets it run")
    }

    /** 154: `k.l()` writes `k.M` from the head position and facing. */
    @Test fun `F13b k_l writes k_M at the e() head`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, s = 0)
        PlayerFsm(w).tick(p, Pad())
        assertEquals(listOf(200, 39, 260, 99), w.kM.toList())
        p.av = true
        PlayerFsm(w).tick(p, Pad())
        assertEquals(listOf(140, 39, 200, 99), w.kM.toList())
    }

    /** NPCs read `k.M` as the player's last `e()` left it: the ax4 crate
     *  claims from `k.M`, not from the player's live position. */
    @Test fun `F13b the ax4 claim reads k_M, not the live player`() {
        val w = world()
        val d = w.npcs.first { it.ax == 4 && it.S == 5 }
        d.refreshBoxes()
        w.player.setPositionPx(d.W[0] - 10, d.W[3])
        w.player.av = false; w.player.setAnim(0); w.player.refreshBoxes()
        w.kM.fill(0)                                     // k.M not written yet
        w.npcFsm.tickDestructible(d, w.player)
        assertNull(w.kL, "k.M (zeros) misses the crate")
        w.playerFsm.eHeadReturns(w.player)               // k.l()
        w.npcFsm.tickDestructible(d, w.player)
        assertSame(d, w.kL)
    }

    /** 157-163: `g.r` (the aP S7 grab-QTE) returns after `k.l()`. */
    @Test fun `F13c g_r freezes e() after k_l()`() {
        resetStatics()
        val w = HeadWorld(cellFn = floorFrom(5))
        w.gR = true
        val p = playerAt(200, 99, s = 0)
        PlayerFsm(w).tick(p, hold(Pad.M_RIGHT))
        assertEquals(0, p.S, "no switch")
        assertEquals(260, w.kM[2], "k.l() ran first")
    }
}
