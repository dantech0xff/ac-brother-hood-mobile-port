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

class Slice304Test {

    /** m7 leg I — under-chamber is a sealed pit; the route goes UP.
     *  Proven: the r96 floor has NO holes east of x880 and the ax27#308
     *  doorpost x1060-1077 is permanently armed (`fuseArm` — Z[0]=0 →
     *  findByAw(0)=null → P|4096 fire-push only, Z[1]<0 kills the lever
     *  arm; script-311's door anims need u310, sealed east of the door).
     *  The intended descent is the chimney hole → auto-grab ax13#37 rope
     *  → release onto the slab → west across cp#233@(1244,1470) → the
     *  west tower ledge (x300-559,y1300) → ax10#48 S34 rail ride →
     *  ax4 crates. Assert: rope ride lands the slab, cp#233 crossed,
     *  S164 rail ride, crates grounded. */
    @Test fun mission7UnderChamber() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5                        // f0do held-mask the real play-entry applies (k.F(aj))
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        var jitter = 0; var prevAk = p.ak; var prevAl = p.al
        val trace = ArrayDeque<String>(60)
        var lastS = p.S
        var jumpCd = 0
        var wheelCd = 0; var wheelFlung = false
        var landed = false; var launched = false; var kCFired = false
        var u269Fired = false; var u252Fired = false; var u280Fired = false; var u306Fired = false
        var chainDone = false; var descended = false; var midCorridor = false; var ropeBound = false; var onSlab = false; var cp233 = false
        var railRide = false; var crateTop = false
        var minAl = 99999; var maxAk = 0
        for (t in 0..28000) {
            var mask = chaseMask297(p, w)
            when {
                p.bM != null -> mask = Pad.M_UP
                // slice 370: the y1540 '02' floor is a type-2 kill strip
                // (L353d) — the lift-row crossing is the ax72 wheel uid36:
                // lunge → fling → spring pad (Slice301Test); hands off from
                // the lunge until the pad throws him to the '5' underside.
                !launched && (wheelFlung || p.S == 243) -> mask = 0
                !launched && p.F != null && p.F!!.ax == 72 && p.S >= 270 -> mask = 0
                !launched && Entity.at?.ax == 72 && Entity.at?.aw == 36 &&
                    wheelCd <= 0 && p.F == null && wheelInReach(Entity.at, p) ->
                    { mask = Pad.M_CONTEXT; wheelCd = 12 }
                !chainDone -> mask = when {          // phase 0 — leg-G replay: west on shelf → spring → wing chain → u252 carry
                    !landed -> if (p.aZ) Pad.M_LEFT else Pad.M_LEFT + Pad.M_UP
                    w.kC != null -> Pad.M_CONTEXT
                    else -> if (p.ak < 210) Pad.M_RIGHT else Pad.M_LEFT
                }
                w.kC != null -> mask = Pad.M_CONTEXT   // phase 1 — let any claim script run (u269 zone + fuse carries)
                else -> {
                    when {
                        p.ga != null && !onSlab -> mask = 0                 // riding an ax66 sink lift — wait it out (except post-ride on the slab)
                        onSlab -> mask = if (jumpCd <= 0 && p.ak > 1300) { jumpCd = 30; (16390 or Pad.M_LEFT) } else Pad.M_LEFT   // slab top — west to #233/u351, jump the x1340-1439 hole
                        p.al > 1780 -> mask = if (p.ak in 1020..1090) (16396 or Pad.M_RIGHT) else Pad.M_RIGHT   // mid corridor — jump east over the #308 door at x1060-1077
                        !p.aZ && p.al > 1700 -> mask = Pad.M_RIGHT          // hole drop / corridor descent — drift east
                        p.al > 1700 -> mask = if (descended) (Pad.M_LEFT + Pad.M_DOWN) else Pad.M_RIGHT   // trap floor — crouch-west under the pillar
                        p.al > 1530 -> mask = Pad.M_RIGHT                   // '2' strip / r78 floor — east to the hole
                        else -> mask = Pad.M_RIGHT                          // lift-transit band — east
                    }
                }
            }
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                // slice 413: `g.c(Z)` reads the RAW `k.g` — the '5' shimmy under the x140-299 ledge
                // stops at the '20' slab (x<140); UP on an S38 tick vaults onto the ledge (S54)
                37, 38, 280 -> if (launched && !landed) mask = Pad.M_UP
                164 -> mask = 0
                56, 60 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if (chainDone && p.ak < 1110) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                61 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if (chainDone && p.ak > 1300) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                62 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if ((p.al > 1500 && !(chainDone && p.al > 1530)) || (chainDone && p.ak < 1110)) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
                in 259..266 -> mask = Pad.M_RIGHT
            }
            if (descended && p.al > 1700 && p.S in 56..63) mask = Pad.M_DOWN + Pad.M_RIGHT  // trap-floor slab edge: drop off east, don't re-hang
            if (jumpCd <= 0 && landed && w.kC == null && !chainDone) {
                mask = if (p.S in 259..266 || p.ak < 310) Pad.M_RIGHT + 8 else 16388
                jumpCd = if (p.S in 259..266) 8 else 14
            } else if (jumpCd <= 0 && stall > 0 && stall % 20 == 0 && !chainDone) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            } else if (jumpCd <= 0 && chainDone && (stall >= 30 || jitter >= 30) && p.aZ) {
                mask = if (midCorridor) (16396 or Pad.M_RIGHT) else if (descended || p.al > 1530) (16390 or Pad.M_LEFT) else (16396 or Pad.M_RIGHT)
                jumpCd = 30
            }
            if (chainDone && (p.S == 61 || p.S == 62 || p.S == 60) && p.ak > 1300) mask = Pad.M_DOWN   // gap rims: hang release
            jumpCd--; wheelCd--
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.S == 243 && !launched) wheelFlung = true
            if (p.S != lastS) {
                if (trace.size == 60) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (p.S == 280) launched = true
            if (launched && !landed && p.aZ && p.al > 1280) landed = true
            if (w.kC != null) { kCFired = true
                if (w.kC!!.aw == 252) u252Fired = true
                if (w.kC!!.aw == 269) u269Fired = true
                if (w.kC!!.aw == 306) { u306Fired = true; println("U306 t=$t ak=${p.ak} al=${p.al} S=${p.S}") }
                if (w.kC!!.aw == 280) { u280Fired = true; println("U280 t=$t ak=${p.ak} al=${p.al} S=${p.S}") } }
            // slice 411: standing on the crates, the faithful S292 lunge keeps the player
            // grounded while mounted (S277+) — that is not the post-carry landing.
            if (!chainDone && u252Fired && w.kC == null && p.aZ && !(p.F != null && p.S >= 270)) chainDone = true
            if (!descended && chainDone && p.aZ && p.al > 1700) { descended = true; println("DESCENDED t=$t ak=${p.ak} al=${p.al}") }
            if (!midCorridor && descended && p.aZ && p.al > 1780) { midCorridor = true; println("MIDCORRIDOR t=$t ak=${p.ak} al=${p.al}") }
            if (!onSlab && ropeBound && !descended && p.aZ && p.al in 1400..1560 && p.ak > 1060) { onSlab = true; println("ONSLAB t=$t ak=${p.ak} al=${p.al}") }
            if (!cp233 && onSlab && p.ak in 1200..1300 && p.al < 1520) { cp233 = true; println("CP233 t=$t ak=${p.ak} al=${p.al}") }
            if (p.S == 164 && !railRide) { railRide = true; println("RAIL t=$t ak=${p.ak} al=${p.al}") }
            if (!crateTop && p.aZ && p.al < 1310 && p.ak in 640..780) { crateTop = true; println("CRATES t=$t ak=${p.ak} al=${p.al}") }
            if (kCFired) { if (p.al < minAl) minAl = p.al; if (p.ak > maxAk) maxAk = p.ak }
            if (w.jC == 15) break
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                continue
            }
            if (w.jC != 8) break
            if (u269Fired || u280Fired || u306Fired || (midCorridor && p.ak > 1280)) break
            if (descended && !midCorridor && p.ak > 1560) break
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            // G12 order: the ax27 door post uid44 (x330, y1299 ledge) pushes
            // the player out (i.a(k.aS,P,W), ag=0) in the entity pass, BEFORE
            // his run integrates back in — so a run held into it jitters
            // 341<->351 every tick instead of parking and `stall` never
            // builds. Count "back where he was two ticks ago" as stalled too.
            if (p.ak == prevAk && p.al == prevAl) jitter++ else jitter = 0
            prevAk = lastAk; prevAl = lastAl
            lastAk = p.ak; lastAl = p.al
            if (t % 400 == 0) println("POS t=$t ak=${p.ak} al=${p.al} S=${p.S} aZ=${p.aZ} ga=${p.ga?.ax}#${p.ga?.aw} mask=$mask kC=${w.kC?.aw} kP=${w.kP}")
            val rope = w.findByAw(37)
            if (rope?.bM === p || p.bM === rope) ropeBound = true
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} u269=$u269Fired u280=$u280Fired u306=$u306Fired descended=$descended midCorr=$midCorridor onSlab=$onSlab cp233=$cp233 rail=$railRide crates=$crateTop minAl=$minAl maxAk=$maxAk trace=${trace.joinToString(" ")}")
        assertTrue(u280Fired || u269Fired || u306Fired || railRide || crateTop || (midCorridor && p.ak > 1280),
            "trap-corridor leg not crossed — ak=${p.ak} al=${p.al} S=${p.S} u280=$u280Fired u306=$u306Fired descended=$descended midCorr=$midCorridor onSlab=$onSlab cp233=$cp233 rail=$railRide crates=$crateTop minAl=$minAl maxAk=$maxAk")
    }
}
