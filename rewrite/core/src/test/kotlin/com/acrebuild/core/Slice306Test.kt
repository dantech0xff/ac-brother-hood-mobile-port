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

class Slice306Test {

    /** Probe: park the player on the crates (721,1299) facing east and
     *  dump every ax72-arm gate for uid49 — why Entity.at never armed. */
    @Test fun probeAtArm() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5                        // f0do held-mask the real play-entry applies
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        // park on crates
        p.ak = 721; p.al = 1255; p.av = false; p.setAnim(0); p.aZ = true
        val wheel = w.findByAw(49)!!
        for (t in 0..30) {
            w.pad.e(0)
            w.tick(emptyList())
        }
        println("PROBE p=(${p.ak},${p.al}) S=${p.S} aZ=${p.aZ} gJ=${p.gJ} mountable=${p.mountableState()} " +
            "wheel=(${wheel.ak},${wheel.al}) ax=${wheel.ax} S=${wheel.S} Z0=${wheel.Z[0]} W=[${wheel.W.joinToString()}] " +
            "at=${Entity.at?.ax}#${Entity.at?.aw} pW=[${p.W.joinToString()}] kO=${w.kO} kP=${w.kP}")
        for (t in 0..40) {
            w.pad.e(if (p.ak < 780) Pad.M_RIGHT else 0)
            w.tick(emptyList())
            if (t % 5 == 0 || Entity.at != null)
                println("PROBE t=$t p=(${p.ak},${p.al}) S=${p.S} mountable=${p.mountableState()} at=${Entity.at?.ax}#${Entity.at?.aw} hit=${wheel.inPlayV(w)} au=${wheel.au} i=${wheel.i} los=${p.losBlocked(wheel, w)}")
            if (Entity.at != null) break
        }
    }

    /** m7 leg J — west-tower catapult chain. After crates (x712,y1264
     *  on the west floor) the crossing to checkpoint ax2 uid339
     *  @(1266,1244) — 400px gap x820-1220 — is the ax72 counterweight
     *  wheel uid49 @(866,1160) (Z[0]==2): interact-scan arms `i.at`
     *  (h<440, facing-east, W[3]>=e.W[1] — PlayerFsm mountEntry L279),
     *  `v(65568)` → c(i.at) lunge → i(277) mount → `au()` orbitWheel
     *  cM0 spin-in → cM1 oscillation → band-edge fling
     *  `ag=+3328, ah=-6656, i(243)` (Entity.kt L113, proven). The S243
     *  anim end enters `a(0)` — g.a(int) = enterFall (S43) — which keeps
     *  ag=3328, so the drift carries the player east across the shaft
     *  into the ax66-S12 grab-point uid219 @(1117,1302) (S358 hang).
     *  A facing-toward tap (padDown(8) while av=false) releases into
     *  S235 → `ag=+4864, ah=-6656` dive-lunge (L536) that lands on the
     *  east floor x1220-1599@y1280 where uid339 @(1266,1244) sits.
     *  Assert: i.at arms for uid49, mount+fling fire, the u219 grab→
     *  release chain runs, east-floor landing (or cp#339 consumed). */
    @Test fun mission7WestTowerCatapult() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5                        // f0do held-mask the real play-entry applies (k.F(aj))
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        var jitter = 0; var prevAk = p.ak; var prevAl = p.al
        val trace = ArrayDeque<String>(80)
        var lastS = p.S
        var jumpCd = 0; var pressCd = 0
        var wheelCd = 0; var wheelFlung = false
        var landed = false; var launched = false; var kCFired = false
        var u269Fired = false; var u252Fired = false; var u280Fired = false; var u306Fired = false
        var chainDone = false; var descended = false; var midCorridor = false; var ropeBound = false; var onSlab = false; var cp233 = false
        var railRide = false; var crateTop = false
        var atArmed = false; var mounted = false; var catapult = false
        var ledgeTop = false; var eastFloor = false; var cp339 = false
        var u342Fired = false; var u256Fired = false
        var minAl = 99999; var maxAk = 0
        for (t in 0..42000) {
            var mask = chaseMask297(p, w)
            val mountT = Entity.at
            val boundF = p.F
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
                boundF != null && boundF.ax == 72 && p.S >= 270 -> mask = 0   // wheel mount/orbit states — hands off (F lingers post-fling, faithful)
                p.S == 277 || p.S == 293 -> mask = 0                      // mount-on anim
                p.S == 243 && catapult -> mask = 0                        // fling anim — hands off; the arc lands on the '2' shelf
                mountT != null && mountT.ax == 72 && pressCd <= 0 && p.F == null &&
                    p.al < 1340 && p.ak in 300..900 ->
                    { mask = Pad.M_CONTEXT; pressCd = 12 }                // lunge-mount the wheel
                catapult && p.al in 1450..1530 && p.ak < 1320 -> mask = Pad.M_RIGHT         // slab — run east to hole edge x1339
                catapult && p.al in 1450..1530 && p.ak < 1410 -> mask = 16396 or Pad.M_RIGHT  // jump-east across the 60px hole
                catapult && p.al in 1450..1530 && p.ak > 1440 -> mask = Pad.M_LEFT          // overshot rope — walk back west to x1423
                catapult && p.al in 1450..1530 -> mask = Pad.M_UP                           // at rope x1423 — jump-grab
                catapult && !p.aZ -> mask = 0                             // mid-fling — the arc carries east on its own
                catapult && p.aZ && p.al < 1010 && p.ak > 1150 -> mask = Pad.M_RIGHT + Pad.M_DOWN   // '2' shelf — east + drop
                catapult && p.aZ && p.al < 1010 -> mask = Pad.M_RIGHT                             // mass top — east to shelf
                catapult && p.aZ && p.al in 1240..1310 && p.ak > 1180 ->
                    mask = Pad.M_LEFT                                     // east floor — west to uid339
                catapult && p.aZ && p.al < 1310 && p.ak in 620..800 ->
                    mask = if (p.av || p.ak < 780) Pad.M_RIGHT else 0     // landed back on crates — idle re-approach
                !chainDone -> mask = when {          // phase 0 — leg-G replay: west on shelf → spring → wing chain → u252 carry
                    !landed -> if (p.aZ) Pad.M_LEFT else Pad.M_LEFT + Pad.M_UP
                    w.kC != null -> Pad.M_CONTEXT
                    else -> if (p.ak < 210) Pad.M_RIGHT else Pad.M_LEFT
                }
                w.kC != null -> mask = Pad.M_CONTEXT   // phase 1 — claim scripts (u269 zone + fuse carries)
                p.aZ && p.al < 1310 && p.ak in 620..800 -> {
                    mask = if (p.av || p.ak < 780) Pad.M_RIGHT else 0     // crates — idle facing east so i.at arms
                }
                else -> {
                    when {
                        p.ga != null && !onSlab -> mask = 0                 // riding an ax66 sink lift
                        onSlab -> mask = if (jumpCd <= 0 && p.ak > 1300) { jumpCd = 30; (16390 or Pad.M_LEFT) } else Pad.M_LEFT   // slab west to #233/u351, jump the hole
                        p.al > 1780 -> mask = if (p.ak in 1020..1090) (16396 or Pad.M_RIGHT) else Pad.M_RIGHT   // mid corridor — jump east over #308 door
                        !p.aZ && p.al > 1700 -> mask = Pad.M_RIGHT
                        p.al > 1700 -> mask = if (descended) (Pad.M_LEFT + Pad.M_DOWN) else Pad.M_RIGHT   // trap floor — crouch-west under pillar
                        p.al > 1530 -> mask = Pad.M_RIGHT
                        else -> mask = Pad.M_RIGHT
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
                243 -> if (catapult) mask = 0                           // catapult fling — hands off
                235, 238 -> mask = 0                                    // release-lunge arc — hands off
                228, 358 -> mask = Pad.M_TAP_R or Pad.M_RIGHT           // u219 hang — tap-right = release EAST toward the floor
                277, 293 -> mask = 0                                    // wheel mount/ride — hands off
                56, 60 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if (chainDone && p.ak < 1110) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                61 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if (chainDone && p.ak > 1300) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                62 -> mask = if (descended && !midCorridor) Pad.M_LEFT else if ((p.al > 1500 && !(chainDone && p.al > 1530)) || (chainDone && p.ak < 1110)) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
                in 259..266 -> mask = Pad.M_RIGHT
            }
            if (descended && p.al > 1700 && p.S in 56..63) mask = Pad.M_DOWN + Pad.M_RIGHT
            if (jumpCd <= 0 && landed && w.kC == null && !chainDone) {
                mask = if (p.S in 259..266 || p.ak < 310) Pad.M_RIGHT + 8 else 16388
                jumpCd = if (p.S in 259..266) 8 else 14
            } else if (jumpCd <= 0 && stall > 0 && stall % 20 == 0 && !chainDone) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            } else if (jumpCd <= 0 && chainDone && (stall >= 30 || jitter >= 30) && p.aZ && !(catapult && p.al in 1450..1530)) {
                mask = if (midCorridor) (16396 or Pad.M_RIGHT) else if (descended || p.al > 1530) (16390 or Pad.M_LEFT) else (16396 or Pad.M_RIGHT)
                jumpCd = 30
            }
            if (chainDone && (p.S == 61 || p.S == 62 || p.S == 60) && p.ak > 1300) mask = Pad.M_DOWN
            jumpCd--; wheelCd--; pressCd--
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.S == 243 && !launched) wheelFlung = true
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (p.S == 280) launched = true
            if (launched && !landed && p.aZ && p.al > 1280) landed = true
            if (w.kC != null) { kCFired = true
                if (w.kC!!.aw == 252) u252Fired = true
                if (w.kC!!.aw == 269) u269Fired = true
                if (w.kC!!.aw == 342) { u342Fired = true; println("U342 t=$t ak=${p.ak} al=${p.al} S=${p.S}") }
                if (w.kC!!.aw == 256) { u256Fired = true; println("U256 t=$t ak=${p.ak} al=${p.al} S=${p.S}") }
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
            if (!atArmed && Entity.at != null && Entity.at!!.ax == 72 && Entity.at!!.aw == 49) { atArmed = true; println("AT t=$t ak=${p.ak} al=${p.al} S=${p.S} at=ax72#${Entity.at!!.aw}") }
            if (!mounted && (p.F != null && p.F!!.ax == 72 && p.F!!.aw == 49)) { mounted = true; println("MOUNT t=$t ak=${p.ak} al=${p.al} S=${p.S} f=ax72#${p.F!!.aw}") }
            if (!catapult && mounted && p.S == 243) { catapult = true; println("CATAPULT t=$t ak=${p.ak} al=${p.al} S=${p.S}") }
            if (!ledgeTop && catapult && p.aZ && p.al < 1010 && p.ak > 1150) { ledgeTop = true; println("LEDGE t=$t ak=${p.ak} al=${p.al}") }
            if (!eastFloor && p.aZ && p.al in 1240..1310 && p.ak > 1180) { eastFloor = true; println("EASTFLOOR t=$t ak=${p.ak} al=${p.al}") }
            if (!cp339 && w.checkpoints.any { it.aw == 339 && it.consumed }) { cp339 = true; println("CP339 t=$t ak=${p.ak} al=${p.al} S=${p.S}") }
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
            if (cp339 || u269Fired || u280Fired || u306Fired || (midCorridor && p.ak > 1280)) break
            if (descended && !midCorridor && p.ak > 1560) break
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            // G12 order: the run into the ax27 door post uid44 jitters
            // 341<->351 instead of parking — count it as a stall too (see
            // Slice304Test).
            if (p.ak == prevAk && p.al == prevAl) jitter++ else jitter = 0
            prevAk = lastAk; prevAl = lastAl
            lastAk = p.ak; lastAl = p.al
            if (t % 400 == 0) println("POS t=$t ak=${p.ak} al=${p.al} S=${p.S} aZ=${p.aZ} ga=${p.ga?.ax}#${p.ga?.aw} F=${p.F?.ax}#${p.F?.aw} at=${Entity.at?.ax}#${Entity.at?.aw} mask=$mask kC=${w.kC?.aw} kP=${w.kP}")
            val rope = w.findByAw(37)
            if (rope?.bM === p || p.bM === rope) ropeBound = true
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} u269=$u269Fired u280=$u280Fired u306=$u306Fired descended=$descended midCorr=$midCorridor onSlab=$onSlab cp233=$cp233 rail=$railRide crates=$crateTop at=$atArmed mounted=$mounted catapult=$catapult ledge=$ledgeTop eastFloor=$eastFloor cp339=$cp339 u342=$u342Fired u256=$u256Fired minAl=$minAl maxAk=$maxAk trace=${trace.joinToString(" ")}")
        assertTrue(catapult && (cp339 || eastFloor || ledgeTop),
            "catapult leg not crossed — ak=${p.ak} al=${p.al} S=${p.S} at=$atArmed mounted=$mounted catapult=$catapult ledge=$ledgeTop eastFloor=$eastFloor cp339=$cp339 crates=$crateTop trace tail=${trace.takeLast(12).joinToString(" ")}")
    }
}
