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

class Slice180Test {

    /** i.java:2415-2427 (proven) — the ax25 flying player-slot init arm. */
    @Test fun `level1 spawn applies the ax25 init`() {
        val w = world(aj = 1)
        val p = w.player
        assertEquals(202, p.az, "az=202")
        assertEquals(90, p.x1, "g.e(90) sets x[1]")
        assertEquals(2, p.aA); assertEquals(3, p.aB)
        assertEquals(-2560, p.ah, "ah=-2560 entry velocity")
        assertEquals(-1, p.aq); assertEquals(-1, p.ar)
        assertNotNull(p.ad, "`ad` = retype-26 glider companion")
        assertEquals(26, p.ad!!.ax)
        assertEquals(201, p.ad!!.az, "ax26 init arm (i.java:2429): az=201")
        assertEquals(1, p.ad!!.aw, "`new i(sArr)` — ad inherits the record uid")
        assertEquals(4, p.ad!!.S, "ad i(r8[5]) before the per-tick mirror")
        assertEquals(w.clipFor(16), p.clip, "bi[25]=16 — glider-suit clip")
        val g0 = world(aj = 0)
        assertEquals(g0.clipFor(0), g0.player.clip,
            "grounded packs keep clip0")
        assertFalse(w.npcs.any { it.ax == 0 || it.ax == 25 },
            "player-slot record must not double-spawn into bb[]")
        assertNull(world(aj = 0).player.ad, "grounded levels carry no ad")
    }

    /** g.java:5603+ n() S∈{0,4,5,17,18}: no steering → `ah=kY` descent. */
    @Test fun `glide descends at kY and decays bank to zero`() {
        val w = world(aj = 1)
        w.stateL(8)
        w.kQ = 230                                        // bh3 camera init
        val p = w.player; p.setAnim(0); p.ag = -2048; p.ah = 0
        val pad = Pad()
        // input arm (iBi=0 && gE=0): z3 tail decays ah 768/tick toward
        // kY(-1792), z2 tail decays ag the same way.
        w.playerFsm.tick(p, pad)
        assertEquals(-768, p.ah, "z3 tail: ah -= 768 toward kY")
        assertEquals(-1280, p.ag, "ag decays 768 toward 0 (-2048+768)")
        w.playerFsm.tick(p, pad)
        assertEquals(-1536, p.ah)
        assertEquals(-512, p.ag)
        w.playerFsm.tick(p, pad)
        assertEquals(w.kY, p.ah, "glide settles at kY descent")
        assertEquals(0, p.ag, "bank settles to 0")
    }

    /** g.java:5746-5798: steering/climb/dive arms (verbatim quirks kept). */
    @Test fun `steering banks and climbs with verbatim clamps`() {
        val w = world(aj = 1)
        w.stateL(8)
        w.kQ = 230                                        // bh3 camera init
        // input arm runs when !iBi && !gE (g.java:14393-14396) — iBi
        // stays false: the climb/grab latches own the scripted arm.
        val p = w.player; p.setAnim(0); p.ag = 0; p.ah = 0
        val pad = Pad()
        pad.held = 4112                                   // u(4112) left
        p.setAnim(4)                                      // z4 state → bank anim applies
        w.playerFsm.tick(p, pad)
        assertEquals(-768, p.ag, "left bank -768/tick")
        // clip16's S32/S33 are 1-frame poses. The `bB == 0 && bC == 0 && r() && z4 → i(4)` recover arm
        // (g.n() @1654-1684, raw bytes, slice 416: the PAD's edge / held words) stays quiet while a
        // direction key is held — the bank pose stays up (the port read two never-written stubs
        // and blipped back to S4 on every wrap).
        assertEquals(4, p.Q, "kBD<15 → light left bank i(33) from the glide S4")
        assertEquals(33, p.S, "key held → no recover")
        pad.held = 0                                      // release — held keys re-steer
        // S4's glide case keeps decaying the banked ag (z2 tail).
        w.playerFsm.tick(p, pad)
        assertEquals(0, p.ag, "bank impulse decays via tail")
        p.setAnim(4)
        pad.held = 8256                                   // u(8256) right
        w.playerFsm.tick(p, pad)
        assertEquals(768, p.ag, "right bank +768/tick")
        assertEquals(32, p.S, "kBD<15 → light right bank i(32); the held key keeps the pose")
        // climb: u(16388) gated kQ>117 — kQ=230 on bh3. S32 has no exit
        // arm (verbatim g.java:6013-6020: `av=false` + dead ifs only) —
        // restore the glide state first.
        p.setAnim(4)
        pad.held = 16388
        p.ah = 0
        w.playerFsm.tick(p, pad)
        assertEquals(-768, p.ah, "climb ah-=768")
        repeat(4) { w.playerFsm.tick(p, pad) }
        assertEquals(-2048 + w.kY, p.ah, "climb clamps at -2048+kY")
        assertTrue(p.S == 4, "climb → i(4)")
        // dive: u(33024) gated kQ<230 — kQ==230 fails the gate (verbatim).
        // With bi set the gated-off dive leaves the z3 tail: -768/tick.
        pad.held = 33024
        p.ah = 0
        p.setAnim(4)
        w.playerFsm.tick(p, pad)
        assertEquals(-768, p.ah, "dive gated off → tail decel only")
        repeat(2) { w.playerFsm.tick(p, pad) }
        assertEquals(w.kY, p.ah, "tail settles at kY")
    }

    /** g.java:5610-5644 stall arms: aE<=0 && aH<0 → i(24) → stateL(12). */
    @Test fun `stall arms the failsafe and fails on clip end`() {
        val w = world(aj = 1)
        w.stateL(8)
        w.kQ = 230
        val p = w.player; p.setAnim(0)
        w.kAE = 0; w.kAH = -1                       // stall-grace exhausted
        w.playerFsm.tick(p, Pad())
        assertEquals(24, p.S, "kAE<=0 && kAH<0 → i(24)")
        assertEquals(0, p.x1, "stall zeroes the meter")
        assertTrue(w.iBB, "stall arms bB + the 999/kB/kG markers")
        // S24 failsafe: off-camera → !v() → stateL(12).
        p.setPositionPx(p.ak, w.camY + 4000); p.refreshBoxes()
        w.playerFsm.tick(p, Pad())
        assertEquals(12, w.jC, "stall fail → k.l(12)")
    }

    /** g.java:5837-5867 + tail: `ad` mirror + waypoint homing. */
    @Test fun `ad companion mirrors and homing steps ak al`() {
        val w = world(aj = 1)
        w.stateL(8)
        w.kQ = 230
        val p = w.player; p.setAnim(0); p.ag = 10; p.ah = w.kY
        w.iBB = true                                 // bank-lock latch
        w.playerFsm.tick(p, Pad())
        val ad = p.ad!!
        assertEquals(p.ak + 20, ad.ak, "ad.ak = ak+20")
        assertEquals(p.al, ad.al); assertEquals(p.ag, ad.ag)
        assertEquals(p.ah, ad.ah); assertEquals(p.S, ad.S)
        assertEquals(-1, p.aq); assertEquals(-1, p.ar,
            "iBB consumes the waypoint target")
        // homing arm: aq/ar set → ak/al step toward them at ±10.
        val al0 = p.al
        p.aq = p.ak + 25; p.ar = p.al - 5; w.iBB = false
        w.playerFsm.tick(p, Pad())
        assertEquals(p.aq - 15, p.ak, "ak steps +10 toward aq")
        // verbatim: ar/al both += kX(-7), then ar<al → al-=10, then
        // (ar>al && kQ>=230) → ar=al. Net: al = al0 - 7 - 10.
        assertEquals(al0 - 17, p.al, "al steps via scroll + chase")
        assertEquals(p.al, p.ar, "ar snaps to al (kQ>=230 arm)")
    }

    /** g.java:5678-5700 flap arm: cooldown + !iBk + z4 → e(false). */
    @Test fun `auto-flap spawns the puff and arms flap velocity`() {
        val w = world(aj = 1)
        w.stateL(8)
        w.kQ = 230
        // input arm (iBi=0 && gE=0): the flap lives in the aC()+input
        // arm — kAI cooldown elapsed → flap(p, false).
        val p = w.player; p.setAnim(4); w.kAI = 11   // cooldown elapsed
        p.ah = w.kY                                  // z4 glide condition
        val before = w.pendingInsert.size
        w.playerFsm.tick(p, Pad())
        val wisp = w.iAK
        assertNotNull(wisp, "flap spawns the ax24 clip-40 puff")
        assertTrue(w.pendingInsert.size > before, "puff queued via k.b")
        assertEquals(-3840 + w.kX, wisp!!.ah, "puff velocity -3840+kX")
        assertTrue(wisp.P and 16 != 0, "wisp P|=16")
        assertEquals(0, w.kAI, "cooldown reset")
    }

    /** grounded world must not run the flight tick. */
    @Test fun `bh3 gate keeps grounded dispatch`() {
        val w = world(aj = 0)
        val p = w.player; p.setAnim(0); p.ah = 0
        w.stateL(8)
        w.playerFsm.tick(p, Pad())
        // aA==2 is the grounded default since the e() head's `k.aA<=0 →
        // aA=2` (slice 360); the ax25 init's other marks stay absent.
        assertNotEquals(202, p.az, "no ax25 init (az=202)")
        assertTrue(p.clip !== w.clips[16], "grounded clip, not the glider suit")
    }
}
