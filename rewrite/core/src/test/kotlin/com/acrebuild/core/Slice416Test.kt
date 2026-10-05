package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Slice 416 — the findings of eight parallel read-only auditors (a frozen snapshot of the port against
 * the raw `javap` bytes: g.e() head / arms 2164-15123, i-core, i-misc, the `D()` reset), each one
 * re-proven in the raw instructions before it was applied.
 *
 * Offsets below are `g.javap.txt` / `i.javap.txt` / `k.javap.txt` byte offsets.
 */
class Slice416Test {
    private val synth get() = Slice369Test.synth

    private fun resetStatics() {
        Entity.grabLatch = false; Entity.gq = false; Entity.gf = null
        Entity.gE = false; Entity.icu = false; Entity.entBq = 0
        Entity.at = null; Entity.L = -1; Entity.M = -1
    }

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

    private fun floorFrom(row: Int): (Int, Int) -> Int = { _, cy -> if (cy >= row) 20 else 0 }
    private fun hold(mask: Int): Pad = Pad().also { it.bC = mask }

    // ------------------------------------------------------------------ S184 / S205 finisher end-gate

    @Test fun `S184 without a victim ends the finisher and releases the input lock`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        w.kAm = true
        val p = playerAt(200, 99, synth, 184)
        assertFalse(p.animFinished())
        PlayerFsm(w).tick(p, Pad())
        // @4496-4509: `r() || aN == null` → k.p(); i.O(); i(0)
        assertEquals(0, p.S, "aN == null → i(0)")
        assertFalse(w.kAm, "k.p()")
    }

    // ------------------------------------------------------------------ S50 / S241 frame-1 drain = the static g.d(I)V

    @Test fun `S50 frame-1 drain is the static g_d - the global hit lock`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, s = 50)
        p.T = 1; p.U = 0; p.x1 = 90; p.gt = 0
        PlayerFsm(w).tick(p, Pad())
        assertEquals(8, w.iBh, "@4580-4583 invokestatic g.d(I)V: `i.bh = 8` (the old drainMeter set an instance copy)")
        assertEquals(0, p.x1)
    }

    // ------------------------------------------------------------------ S22 owns the hide-spot slot `g.e`

    @Test fun `S22 releases the hide-spot owner before the shared air family`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cellFn = floorFrom(8))
        val p = playerAt(200, 100, synth, 22)
        p.ge = Entity(10, null); p.aA = 8 or 2; p.az = 5
        PlayerFsm(w).tick(p, Pad())
        // @7365-7393: `aA &= -9; az = 100; g.e = null`
        assertNull(p.ge, "g.e = null")
        assertEquals(0, p.aA and 8, "aA &= -9")
        assertEquals(100, p.az, "az = 100")
    }

    // ------------------------------------------------------------------ air-family tail

    /** `i.f(this)` is the LAST step of the air arm (@8296): the wall clamp sees the post-fall state. */
    class ClampWorld : Slice369Test.HeadWorld(cellFn = { _, cy -> if (cy >= 8) 20 else 0 }) {
        var sAtClamp = -1
        override fun scrollWallClamp(p: Entity) { sAtClamp = p.S }
    }

    @Test fun `air arm - the scroll wall clamp runs after the anim-end fall`() {
        resetStatics()
        val w = ClampWorld()
        val p = playerAt(200, 100, null, 19)            // clipless: r() holds
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "r() && S != 215 && S != 22 → a(0)")
        assertEquals(43, w.sAtClamp, "@8296 i.f(this) last — it saw S43, not the pre-fall S19")
    }

    @Test fun `S215 that has turned without floor contact hands over to the fall - not to a landing`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, synth, 215)
        p.ah = 1; p.av = false
        PlayerFsm(w).tick(p, Pad())
        // @8119-8223: `land test ? d(false) : (S == 215 ? g.a(0); av = !av : …)`
        assertEquals(43, p.S, "g.a(0)")
        assertTrue(p.av, "av = !av")
    }

    @Test fun `S215 with floor contact lands - d(false) is for every S`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 215)           // a clip, so r() stays false after the landing
        p.ah = 1; p.av = false
        PlayerFsm(w).tick(p, Pad())
        assertFalse(p.av, "the landing leaves the facing alone (the port flipped it and fell on)")
        assertEquals(5, p.S, "d(false): the land squat — not the S43 hand-over")
    }

    @Test fun `S25 against a wall with no horizontal speed is still pushed out`() {
        resetStatics()
        // wall from column 10 on: the player's right strip (x = 211) is wall; row 8 is the floor
        val w = Slice369Test.HeadWorld(cellFn = { cx, cy -> if (cy >= 8 || cx >= 10) 20 else 0 })
        val p = playerAt(200, 100, null, 25)
        p.ag = 0
        PlayerFsm(w).tick(p, Pad())
        // @8068-8112: no `ag != 0` guard — `ag = ag > 0 ? 512 : -512`
        assertEquals(-512, p.ag, "idle S25 against a wall: ag = -512")
    }

    // ------------------------------------------------------------------ wall-grab snap: two byte sequences

    private fun grabWorld() = Slice369Test.HeadWorld(cellFn = { cx, cy -> if (cx <= 9) 20 else if (cy >= 12) 20 else 0 })

    @Test fun `the fall-arm wall grab snaps with W0 over 20 - one cell nearer than the air arm`() {
        resetStatics()
        val w = grabWorld()
        val p = playerAt(206, 100, synth, 43)
        p.av = true; p.aF = 1; p.cv = true
        val pad = hold(Pad.M_UP)
        PlayerFsm(w).tick(p, pad)
        assertEquals(101, p.S, "cv && aF != 0 && aT == 20 → i(101)")
        // @8881-8927: `ak = (W[0] / 20) * 20 + 1` (the air site @7886-7938 is `((W[0] + 20) / 20) * 20 + 1`)
        assertEquals(p.W[0] / 20 * 20 + 1 - 10 + 10, p.ak, "fall-site snap, W0=${p.W[0]}")
        assertEquals(201, p.ak)
    }

    // ------------------------------------------------------------------ the S92 / S101 wall-bounce

    @Test fun `S92 bounce off an open head cell re-enters the wall state - a solid one falls`() {
        resetStatics()
        val open = Slice369Test.HeadWorld(cell = 0)
        val p1 = playerAt(200, 100, null, 92); p1.aO = 7
        PlayerFsm(open).tick(p1, Pad())
        assertEquals(36, p1.S, "ah != 0 → a(36,36)")
        val solid = Slice369Test.HeadWorld(cell = 20)
        val p2 = playerAt(200, 100, null, 92)
        PlayerFsm(solid).tick(p2, Pad())
        assertEquals(43, p2.S, "@6809-6832: ah == 0 → g.a(0)")
    }

    // ------------------------------------------------------------------ S375

    @Test fun `S375 skids away from the boss the player faces`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, null, 375)
        p.av = true
        PlayerFsm(w).tick(p, Pad())
        assertEquals(1280, p.ag, "@2164-2182: `ag = 1280; if (!av) ag = -1280`")
    }

    // ------------------------------------------------------------------ post-tail ap() gate: i.bn and g.E

    private fun contextPad(): Pad = Pad().also { it.bB = Pad.M_CONTEXT; it.bC = Pad.M_CONTEXT }

    @Test fun `the context attack is blocked by the live g_E static and the live i_bn flag`() {
        fun swings(setup: (Level0World) -> Unit): Boolean {
            Entity.gE = false
            val w = world(); w.stateL(8); settleIntro(w); w.npcs.clear()
            setup(w)
            val p = w.player
            p.gI = 1; p.gJ = 1
            var swung = false
            repeat(4) {
                w.pad.e(Pad.M_CONTEXT)
                w.tick(emptyList())
                if (p.S == 67) swung = true
            }
            Entity.gE = false
            return swung
        }
        assertTrue(swings { }, "control: the context press swings the sword")
        assertFalse(swings { Entity.gE = true }, "@14408 `g.E` (the static `Entity.gE`) blocks ap()")
        assertFalse(swings { it.iBn = true }, "@14402 `i.bn` blocks ap()")
        resetStatics()
    }

    @Test fun `the knife is ignored in the S79 stance and a running player stops dead to throw`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cellFn = floorFrom(5))
        val p = playerAt(200, 99, synth, 79)
        p.gI = 2; p.gJ = 2
        p.contextDispatch(w, contextPad())
        assertEquals(79, p.S, "@200-235: I == 2 && S != 79")
        val q = playerAt(200, 99, synth, 12)
        q.gI = 2; q.gJ = 2; q.ag = 2560; q.ai = 77
        q.contextDispatch(w, contextPad())
        assertEquals(286, q.S)
        assertEquals(0, q.ag, "ai = ag = 0")
        assertEquals(0, q.ai)
    }

    // ------------------------------------------------------------------ g.c(i) clears the crate-top static

    @Test fun `a lunge clears the crate-top level that g_m reads`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, synth, 20)
        Entity.entBq = 140
        val target = Entity(11, null)
        target.setPositionPx(260, 100); target.W[0] = 250; target.W[1] = 60; target.W[2] = 270; target.W[3] = 100
        p.grabLunge(target, w)
        assertEquals(0, Entity.entBq, "g.c(i) @5-6 `putstatic i.bq = 0`")
        resetStatics()
    }

    // ------------------------------------------------------------------ the gauge point and the throws

    @Test fun `the knife throws leave from the player's own g_L g_M`() {
        resetStatics()
        val w = world(); w.stateL(8); settleIntro(w)
        w.npcs.clear()
        val p = w.player
        val target = Entity(11, w.clips[7]).apply { setPositionPx(p.ak + 40, p.al); refreshBoxes(); aB = 100 }
        p.g = target
        p.gQL = 321; p.gQM = 123
        Entity.L = 7; Entity.M = 8                                   // the i.L / i.M touch anchor
        p.K = 6
        p.interactAction(w, Pad())
        val shot = w.pendingInsert.firstOrNull { it.ax == 8 }
        assertNotNull(shot, "the throw spawned the ax8 knife projectile")
        assertEquals(321, shot!!.ak, "@254-263 `aload_0; getfield g.L` — not the static i.L")
        assertEquals(123, shot.al)
        resetStatics()
    }

    // ------------------------------------------------------------------ the weapon-cycle lock k.at

    @Test fun `the weapon cycle works again once the HUD tail has released the lock`() {
        val w = world(); settleIntro(w); w.npcs.clear()
        val p = w.player
        p.gJ = 1 or 2 or 8; w.rebuildEquip()
        assertTrue(w.equipCount > 1)
        p.gI = 1
        w.player.aZ = true
        val pad = Pad()
        pad.bB = Pad.M_CYCLE
        assertTrue(p.cycleEquip(w, pad), "first press cycles")
        assertEquals(1, w.actionLock, "g.ao() sets k.at")
        assertFalse(p.cycleEquip(w, pad), "locked")
        w.tick(emptyList())                                         // HUD tail @1159-1167: at == 1 → 0
        assertEquals(0, w.actionLock, "the gate holds on the ground in play")
        assertTrue(p.cycleEquip(w, pad), "second press cycles again")
    }

    @Test fun `the weapon corner is gated by g_o - airborne players do not release the lock`() {
        val w = world(); settleIntro(w); w.npcs.clear()
        w.player.aZ = false; w.player.standingOn = null
        w.actionLock = 1
        assertFalse(w.weaponCornerArmed(), "@1092-1098 `invokevirtual g.o:()Z`")
        w.player.aZ = true
        assertTrue(w.weaponCornerArmed())
    }

    // ------------------------------------------------------------------ i.J(): the dialog kind k.u, not the held word

    @Test fun `the companion overlay stays visible in a tip dialog of kind 8`() {
        val w = world(); settleIntro(w)
        val overlay = Entity(71, w.clips[0])
        overlay.S = 0; overlay.T = 0
        w.tutorialHint(0)                                         // the tip dialog: j.c = 21
        assertEquals(21, w.jC)
        // jC == 21 && k.u == 8 → return (no P|128); k.u != 8 → hide
        val pAll = w.player
        overlay.P = 0
        w.dlgU = 8
        overlay.followJ(pAll, w)
        assertEquals(0, overlay.P and 128, "@74-79 `k.u == 8 → return`")
        overlay.P = 0
        w.dlgU = 9
        overlay.followJ(pAll, w)
        assertEquals(128, overlay.P and 128, "k.u != 8 inside a dialog hides it")
    }

    // ------------------------------------------------------------------ the flying player's tick is g.n(), not g.e()

    @Test fun `the flying tick leaves the e() head alone - no aA stance fix, and bh counts down only past the dead-drag return`() {
        val w = world(aj = 1); w.stateL(8); settleIntro(w); w.npcs.clear()
        assertTrue(w.bh3)
        val p = w.player
        p.aA = 0
        w.iBh = 5; w.iBe = false
        w.playerFsm.tick(p, Pad())
        assertEquals(0, p.aA, "g.n() has no `aA` stance fix (the e() head raised 0/1 to 2)")
        assertEquals(4, w.iBh, "@47-61 i.bh--")
        w.iBh = 5; w.iBe = true
        w.playerFsm.tick(p, Pad())
        assertEquals(5, w.iBh, "the dead-drag arm returns at @46, before i.bh--")
    }

    @Test fun `the flying level-out to S4 waits for the pad - a held direction keeps the bank`() {
        val w = world(aj = 1); w.stateL(8); settleIntro(w); w.npcs.clear()
        w.kQ = 230
        val p = w.player
        p.setAnim(4); p.ag = 0; p.ah = w.kY
        val held = Pad(); held.bC = 4112
        p.setAnim(4)
        w.playerFsm.tick(p, held)
        assertTrue(p.S != 4 || p.Q == 4, "the left bank pose was entered")
        val idle = Pad()
        p.setAnim(33)
        p.T = 0; p.U = 0
        w.playerFsm.tick(p, idle)
        assertEquals(4, p.S, "@1654-1684 `bB == 0 && bC == 0 && r()` → i(4)")
        p.setAnim(33)
        p.T = 0; p.U = 0
        w.playerFsm.tick(p, held)
        assertEquals(33, p.S, "bC != 0 → no level-out")
    }

    // ------------------------------------------------------------------ the raw `ac` stores

    @Test fun `a fling keeps P|256 on the old link target - only a(Li) releases it`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val zone = Entity(32, null)
        val p = playerAt(200, 100, null, 43)
        p.bindAc(zone)
        assertEquals(256, zone.P and 256, "a(Li;)V: P |= 256")
        p.flingAirborne(0, w)
        assertNull(p.ac, "g.a(I)V @31-37 raw putfield ac = null")
        assertEquals(256, zone.P and 256, "…which leaves the old target frozen")
        val zone2 = Entity(32, null)
        p.bindAc(zone2)
        p.bindAc(null)
        assertEquals(0, zone2.P and 256, "a(null) releases it")
    }

    // ------------------------------------------------------------------ S90 flushes all six pad words

    @Test fun `S90 flushes every input word - k_v clears all six`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, synth, 90)
        val pad = Pad().also { it.eK = 1; it.eL = 2; it.eN = 4; it.eM = 8; it.bB = 16; it.bC = 32 }
        PlayerFsm(w).tick(p, pad)
        assertEquals(listOf(0, 0, 0, 0, 0, 0), listOf(pad.eK, pad.eL, pad.eN, pad.eM, pad.bB, pad.bC),
            "@9768 k.v() wipes eL bC bB eM eK eN (the port cleared eL only)")
    }

    // ------------------------------------------------------------------ S38 unbound over an open cell ends the arm

    @Test fun `S38 unbound over an open head cell drops and skips the DOWN handler`() {
        resetStatics()
        val w = Slice369Test.HeadWorld(cell = 0)
        val p = playerAt(200, 100, null, 38)
        PlayerFsm(w).tick(p, hold(Pad.M_DOWN))
        // @11282-11306: `al = W[3]; i(43); goto 13629` — the DOWN handler's `al = W[3] + 10` never runs
        assertEquals(43, p.S)
        assertEquals(100, p.al, "al = W[3], not W[3] + 10")
    }

    // ------------------------------------------------------------------ i.bn is the one live flag

    @Test fun `the alert flag turns the crouch walk into the S199 creep`() {
        fun walkS(bn: Boolean): Int {
            val w = world(); w.stateL(8); settleIntro(w); w.npcs.clear()
            val p = w.player
            p.setAnim(79); p.av = true; w.iBn = bn
            w.playerFsm.l(p, hold(Pad.M_LEFT))
            return p.S
        }
        assertEquals(199, walkS(true), "g.l() @56 `i(bn ? 199 : 32)` reads the live i.bn (the port's own copy never moved)")
        assertEquals(32, walkS(false), "control: no alert → the plain S32")
    }

    // ------------------------------------------------------------------ the D() reset: shake, hints, letterbox target, input lock, stale links

    @Test fun `a respawn re-arms the hints and clears the shake the letterbox target the lock and the stale links`() {
        val w = world(); w.stateL(8); settleIntro(w)
        w.tutorialHint(0)                                      // consumes the mission-0 hint 0
        assertEquals(21, w.jC)
        w.stateL(8)
        w.tutorialHint(0)
        assertEquals(8, w.jC, "the hint is one-shot (i.br[0])")
        w.kCO = 30; w.kCP = true                               // a shake in progress
        w.kAw = 20                                             // the flying letterbox target
        w.kAm = true                                           // an input lock (the flying g.n() never releases it)
        w.kF = Entity(11, null)
        Entity.at = Entity(72, null)
        w.resetLevel(true)                                     // a(true): V(); d(true) → D()
        assertEquals(0, w.kCO, "D() @299-300 `k.n(-1)`: cO = 1 — the reload's camera step (C()) consumed it; the old wall-release overload left 30 → 29")
        assertFalse(w.kCP, "cP = false")
        assertEquals(0, w.kAw, "D() @335 i.O(): k.aw = 0 (and @384 k.r())")
        assertFalse(w.kAm, "D() @338 k.p()")
        assertNull(w.kF, "D() @17 k.F = null")
        assertNull(Entity.at, "D() @173 i.at = null")
        w.tutorialHint(0)
        assertEquals(21, w.jC, "D() @192-211 re-arms every i.br[]")
        Entity.at = null
    }

    // ------------------------------------------------------------------ i.t(): the director's camera-space W

    @Test fun `the ax21 director's W is translated by bY minus the camera and bZ - not by its own ak al`() {
        val w = world(); w.stateL(8); settleIntro(w)
        w.kO = 300
        val d = Entity(21, w.clips[13]); d.setPositionPx(700, 90); d.bY = 500; d.bZ = 40
        d.refreshBoxes()
        val ref = Entity(23, w.clips[13]); ref.setPositionPx(500 - 300, 40)    // a generic box at (bY - k.O, bZ)
        ref.refreshBoxes()
        assertEquals(ref.W.toList(), d.W.toList(), "@1103-1172 `W[0] += bY - k.O; W[1] += bZ`")
    }

    // ------------------------------------------------------------------ i.B(): the canyon slides and the L36 return

    private fun flyWorld(): Level0World = world().also {
        it.kAj = 1
        it.iW = true
    }

    /** Solid block around the player's wrapped probe rows: [cx0, cx1] x the rows `B()` reads. */
    private fun solidBlock(w: Level0World, p: Entity, cx0: Int, cx1: Int) {
        val et = w.level.layers.first { it.id == 0 }
        val r0 = (p.W[1] % 260 + 260) / 20 - 2
        val r1 = (p.W[3] % 260 + 260) / 20 + 2
        for (r in 0 until et.rows) for (c in 0 until et.cols) et.cells[r * et.cols + c] = 0
        for (r in r0..r1) for (c in cx0..cx1) et.cells[r * et.cols + c] = 20
    }

    @Test fun `the canyon left slide steps a whole cell per extra pass - the edge is re-read after every t()`() {
        val w = flyWorld()
        val p = w.player
        p.setAnim(0)
        p.setPositionPx(1000, 500); p.refreshBoxes()
        w.kO = 700; w.kP = 300                                  // in view
        val dx0 = p.W[0] - p.ak; val dx2 = p.W[2] - p.ak
        solidBlock(w, p, (p.W[0] - 30) / 20 - 1, p.W[2] / 20 + 8)   // free ring only on the left
        // the byte-exact model: r2 = W[2] re-read after each t(); up to four passes
        var ak = p.ak; var r2 = p.W[2]; var n = 3; var aT = 10
        while (aT >= 10 && n-- >= 0) {
            ak -= (r2 % 20) + 1
            r2 = ak + dx2
            aT = w.collisionCell((ak + dx0) / 20, (p.W[1] % 260 + 260) / 20)
        }
        assertTrue(aT < 10, "fixture: the byte model leaves the wall (aT=$aT)")
        val expected = ak
        p.canyonCollide(w)
        assertEquals(expected, p.ak, "i.b(III)V @33-83: r1/r2 re-read from W after each t()")
        assertTrue(p.ak < 1000 - 30, "a pass of 20px moves it out in far fewer than four 1-px-edge shifts")
    }

    @Test fun `the canyon right slide steps a whole cell per extra pass - the edge is re-read after every t()`() {
        val w = flyWorld()
        val p = w.player
        p.setAnim(0)
        // a first-pass shift of 1 px (W0 % 20 == 19): the old port repeated it, the bytes step 20 from pass 2
        var x = 1000
        while (true) {
            p.setPositionPx(x, 500); p.refreshBoxes()
            if (p.W[0] % 20 == 19) break
            x++
        }
        w.kO = 700; w.kP = 300                                  // in view
        val dx0 = p.W[0] - p.ak; val dx2 = p.W[2] - p.ak
        val row = (p.W[1] % 260 + 260) / 20
        solidBlock(w, p, p.W[0] / 20 - 8, p.W[2] / 20 + 2)      // free ring only on the right
        var ak = p.ak; var r1 = p.W[0]; var n = 3; var aU = 10
        while (aU >= 10 && n-- >= 0) {
            ak += 20 - ((r1 + 20) % 20)
            r1 = ak + dx0
            aU = w.collisionCell((ak + dx2) / 20, row)
        }
        assertTrue(aU < 10, "fixture: the byte model leaves the wall (aU=$aU)")
        val expected = ak
        p.canyonCollide(w)
        assertEquals(expected, p.ak, "i.c(III)V @33-84: r1/r2 re-read from W after each t()")
    }

    @Test fun `the L36 embedded-corner arm returns without the trailing t()`() {
        val w = flyWorld()
        val p = w.player
        p.setAnim(0)
        p.setPositionPx(1600, 500); p.refreshBoxes()
        w.kAi = true; w.kO = 1000; w.kP = 300
        solidBlock(w, p, (p.W[0] - 30) / 20 - 1, p.W[2] / 20 + 8)
        val staleW0 = p.W[0]
        assertTrue(p.canyonCollide(w))
        assertEquals(1400, p.ak, "ak clamped into [kO, kO + 400] (@64-105)")
        assertEquals(staleW0, p.W[0], "@278 `iconst_1; ireturn`: no `t()` — W keeps the pre-clamp box")
    }

    // ------------------------------------------------------------------ the raw `ac` stores: g.as() / g.au() / the S228 release

    @Test fun `the lunge onto a cart links its track without flagging it, and the orbit release leaves it alone`() {
        val w = world(); w.stateL(8); settleIntro(w); w.npcs.clear()
        val p = w.player
        val track = Entity(66, w.clips[5]); track.aw = 900; w.npcs.add(track)
        val cart = Entity(72, null)
        cart.Z[0] = 4; cart.Z[4] = 900
        cart.setPositionPx(p.ak + 30, p.al); cart.refreshBoxes()
        Entity.at = cart; p.g = null
        p.cE = 1; p.cC = 0; p.cD = 0
        p.X[0] = p.ak; p.X[1] = p.al
        p.lungeTick(w)
        Entity.at = null
        assertTrue(cart.ac === track, "g.as() @248 `F.ac = k.q(F.Z[4])`")
        assertEquals(0, track.P and 256, "…a raw putfield: the track is NOT frozen out of the entity loop")
    }
}
