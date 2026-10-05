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

class Slice280Test {
    /**
     * Mission-1 (flying canyon / bh3) capstone bot — rides the full shaft:
     * five ax24 shrine refills, the ax10-S10 wall-perch climb gate, then the
     * S31 claim-QTE at the top. Everything below is real-input bot code —
     * no state pinning.
     *
     * Route: the mid channel is the only continuous lane (the col-24..35
     * divider is unbroken rows ~200-498, so the right channel is
     * unreachable; verified on pack-7 entry-001 et layer). The perch box
     * [295,521]x[6959,7019] spans the whole narrowed channel at the
     * rows-347-358 choke — every flyer crossing overlaps it, so the
     * S10 bind+climb is a MANDATORY gate, not a shortcut.
     */
    @Test
    fun `capstone mission-1 full-shaft climb to claim-QTE win`() {
        val w = world(aj = 1)
        settleIntro(w)
        val p = w.player
        val route = listOf(
            Triple(433, 9840, "shrine1"),
            Triple(397, 8440, "shrine2"),
            // S10 perch zone W=[295,6959,521,7019], Z={180,50,0,90,30,...}
            // (NpcFsm.kt:1279-1361): hover the head in-band
            // `dy = W[1]-zone.W[3] ∈ (Z[1],Z[0])` and press mask-1 → `iBB`
            // binds → `iBi` latches the flight arm into its scripted branch
            // (PlayerFsm.kt:2437 — `ah = kY`, the conveyor owns the sim) and
            // the sequence drags the player through the box. The iBF hang
            // oscillator (Entity.kt:4660) allows the overlap only while
            // iBF ∈ [Z2,Z3]=[0,90] — L4b2 resets iBF=100/iBE=Z[4]=30 once
            // the zone ticks, so binding close (dy<~90) keeps the ~36-tick
            // budget ahead of the ~10-tick scripted crossing. The zone
            // self-removes on `dy<0` once the feet clear its top (L573).
            Triple(295, 7100, "perch"),
            Triple(222, 6020, "shrine3"),
            Triple(475, 2928, "shrine4"),
            Triple(599, 1555, "shrine5"),
            Triple(560, 590, "claim"),
        )
        var leg = 0; var t = 0; var won = false; var prevS = p.S; var prevAE = w.kAE
        var prevCamY = w.kP; var stallT = 0; var prevAL = p.al
        while (t++ < 60000) {
            when {
                // `stateL(15)` re-enters as i=22 (medals) the same tick when
                // any medal stamps — missionWon is the win latch
                // (Level0World.kt:2432, stateL(15|31|13)).
                w.missionWon -> { won = true; break }
                w.jC == 12 || w.jC == 13 -> {
                    // mission-fail → restart prompt → respawn at checkpoint
                    var guard = 0
                    while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                        w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                        w.tick(emptyList())
                    }
                    continue
                }
                w.jC == 21 -> { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; w.tick(emptyList()); continue }
                w.jC != 8 -> { if (w.jG >= 10 && t % 40 == 0) w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                Entity.gE && leg < route.size - 1 -> { w.tick(emptyList()); continue }
            }
            val (tx, ty, tag) = if (leg < route.size) route[leg] else route.last()
            val zn = w.npcs.filter { it.ax == 10 && it.S == 10 }
                .minByOrNull { Math.abs(it.al - ty) }
            var held = 0
            if (tag == "claim") {
                // claim-QTE (i.java:33500+): overlap arms the lane
                // (X=[0,0,0,2] → pv=2 → CS[2]=16388=M_UP) → ONE M_UP edge →
                // aA=Z[3]=8 → bindScript(8) → win. Strays / aB>=20 consume
                // WITHOUT aA → press only when armed (aB>0). Hover the
                // leash band [camY+117,camY+230] so the zone ticks.
                val z31 = w.npcs.firstOrNull { it.ax == 10 && it.S == 31 }
                if (z31 != null && z31.aB > 0 && z31.aA != 8 && (held and Pad.M_UP) == 0) {
                    held = Pad.M_UP            // fresh edge — pad.v(16388)
                } else {
                    if (p.al < w.kP + 130) held = Pad.M_DOWN
                    else if (p.al > w.kP + 230) held = Pad.M_UP
                }
            }
            else if (tag == "perch") {
                if (zn != null) {
                    // steer into the box's x-center — the channel narrows
                    // to cols 18-23 inside the box x-range anyway.
                    val cx = (zn.W[0] + zn.W[2]) / 2
                    if (p.ak < cx - 10) held = held or Pad.M_RIGHT
                    else if (p.ak > cx + 10) held = held or Pad.M_LEFT
                    // q = p.al - camY is the flight leash: UP-climb needs
                    // q>117; q>=230 velocity-clamps to the conveyor (kQ
                    // arm, Level0World.kt:2244). All hovering rides the
                    // leash, never an absolute altitude.
                    val q = p.al - w.kP
                    if (!w.iBB) {
                        // unbound — hover head ~dy 55-90 (just below the
                        // box, still inside the catch band dy∈(50,180))
                        // and press 1 → bind → scripted crossing. Bound:
                        // hands off — `iBi` makes pad input dead.
                        val hover = minOf(zn.W[3] + 80, w.kP + 195)
                        if (p.al > hover + 8 && q > 125) held = Pad.M_UP
                        else if (p.al < hover - 15) held = Pad.M_DOWN
                        val dy = p.W[1] - zn.W[3]
                        if (dy > zn.Z[1] && dy < zn.Z[0]) held = held or 1
                    }
                }
            }
            else {
                // Shrine leg. Conveyor running → RIDE the leash band: the
                // box sweeps onto the player inside the au<2 window and the
                // S20 arm fires. Conveyor stalled (shaft-bottom clamp) → the
                // world is frozen so the box never approaches — climb/hover
                // absolute. UP-climbing with the conveyor running outruns
                // the camera → shrine off-screen → no fire → fuel-out.
                if (stallT > 30 || p.al in ty - 60..ty + 80) {
                    if (p.al > ty) held = Pad.M_UP
                    else if (p.al < ty - 10) held = Pad.M_DOWN else held = 0
                }
                else if (p.al > w.kP + 200) held = Pad.M_UP
                else if (p.al < w.kP + 140) held = Pad.M_DOWN else held = 0
                // shrine legs advance on FIRE, not position — handled below
            }
            // L1f35 fresh-W: the 6020→2928 shaft pinches into a diagonal
            // west edge (x<360@y5000 → x<400@4400 → x<480@4300 →
            // x<560@4250 → x<640@4120 → x<720@4000). x=471 collides with
            // the wall face mid-gap — steer ~40px west of the west face.
            val steTx = if (tag.startsWith("shrine") && p.al in ty - 400..ty + 600) {
                // final approach — column-align with the shrine's x so the
                // S20 arm's box-overlap check actually fires.
                tx
            } else if (tag.startsWith("shrine") && leg == 4) {
                when {
                    p.al > 4500 -> 320
                    p.al > 4380 -> 360
                    p.al > 4300 -> 400
                    p.al > 4240 -> 440
                    p.al > 4180 -> 480
                    p.al > 4100 -> 540
                    else -> 620
                }
            } else tx
            if (p.ak < steTx - 12) held = held or Pad.M_RIGHT else if (p.ak > steTx + 12) held = held or Pad.M_LEFT
            val preAF = w.kAF
            prevS = p.S; prevAE = w.kAE
            w.pad.e(held); w.tick(emptyList())
            if (p.al - prevAL > 1000) {
                // checkpoint respawn put the player back down the shaft —
                // rewind the route to the first shrine not yet passed so
                // refills re-arm in order (fuel can't span the full climb).
                val rewind = route.indexOfFirst { (_, rty, rtag) -> rtag.startsWith("shrine") && p.al > rty - 300 }
                if (rewind >= 0 && rewind < leg) leg = rewind
            }
            prevAL = p.al
            if (w.kP == prevCamY) stallT++ else stallT = 0; prevCamY = w.kP
            // shrine fire = S20 arm overlap → p.setAnim(21) + kAF trickle.
            // With a full tank kAF stays 0, so detect the S21 heal-anim edge
            // (or kAF edge for partial tanks).
            val justFired = (prevS != 21 && p.S == 21 && w.jC == 8) ||
                (preAF == 0 && w.kAF > 0 && w.jC == 8)
            if (leg < route.size && p.al < ty - 60 && !tag.startsWith("shrine")) leg++
            if (leg < route.size && tag.startsWith("shrine") && justFired) leg++
            // safety: if a shrine never fires but we're far past it, advance
            if (leg < route.size && tag.startsWith("shrine") && p.al < ty - 400) leg++
        }
        println("M1CAP leg=$leg won=$won t=$t pos=${p.ak},${p.al} S=${p.S} camY=${w.kP} kAF=${w.kAF} iBB=${w.iBB} iBi=${w.iBi}")
        var n37 = 0
        for (e in w.npcs) if (e.ax == 37) n37++
        println("BOUNDS ax37count=$n37 kT=${w.kT} kU=${w.kU} kX=${w.kX} kZ=${w.kZ} pW=${p.W.joinToString()} camRect=${w.camRect.joinToString()}")
        assertTrue(won, "m1 capstone — leg=$leg pos=${p.ak},${p.al} S=${p.S} camY=${w.kP}")
    }
}
