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

class Slice303Test {

    /** m7 leg H — upper wing descent: from the u252 chain release
     *  (~x410,y1299) ride the ax66 sink lifts to r78, crouch-walk west
     *  under the lift bind zones, drop past the r87 gap to the r96
     *  under-chamber — assert reaching the boss-2/top route
     *  (u269 claim zone fired, or the under-chamber gate al>1760 with
     *  minAl<1200 already satisfied).
     *  Slice 370 verdict — the under-chamber gate is a faithful dead end:
     *  the descent's ride ends on the r78 floor at y1559, i.e. on r77's
     *  type-2 strip (cols 7-52, every column where r78 is one cell thick
     *  — r79 cols 6-50 are open), and the L353d kill fires there
     *  (g.javap.txt e() 13662-13711). The old route stood on the strip
     *  and vaulted through r78 (S257). The other way down, the slab hole
     *  x1380-1439, drops into the duel arena, whose ax37 uid366 holder
     *  (W x1181-1601, Z0=11: left/right/bottom walls) pins him east of
     *  x1181 on the 2-thick r87-88 floor; slice 305 had already found the
     *  mid corridor a sealed trap pit. So the leg now asserts the faithful
     *  outcome: the wing chain (minAl<1200) and the strip kill on the
     *  descent — or u269, should a route reach it. Phase 0 crosses to the
     *  spring by the ax72 wheel uid36 (Slice301Test). */
    @Test fun mission7UpperWing() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5                        // f0do held-mask the real play-entry applies (k.F(aj))
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        val trace = ArrayDeque<String>(60)
        var lastS = p.S
        var jumpCd = 0
        var wheelCd = 0; var wheelFlung = false
        var landed = false; var launched = false; var kCFired = false
        var u269Fired = false; var u252Fired = false; var chainDone = false
        var minAl = 99999; var maxAk = 0
        var stripKill = false; var killAt = ""
        for (t in 0..24000) {
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
                        p.ga != null -> mask = 0                            // riding an ax66 sink lift — wait it out
                        p.al > 1760 -> mask = 0                             // under-chamber — done
                        // G12: he now steps off onto sink lift u24 a tick
                        // earlier, and the airborne LEFT drift turns him west
                        // on it — so on the y1559 landing S5's land arm turns a
                        // held LEFT into a run (S12) that bounces off the lip
                        // back into S5. Hold DOWN alone through S5 here.
                        // Slice 369 (F7): the grounded arm's a(257,8) drop
                        // (e() 6119-6267) is S79-only, and from S0 a held
                        // LEFT wins in l() (run, S12) — so DOWN stays alone in
                        // S0 too: l()'s DOWN arm → aw() vaults a(257,8) where
                        // the cell two over is open (x911), else crouches
                        // (S78→S79) and DOWN+LEFT then crouch-walks.
                        (p.S == 5 || p.S == 0) && p.al > 1530 && p.ak in 620..1100 -> mask = Pad.M_DOWN
                        !p.aZ -> mask = Pad.M_LEFT                          // airborne: always drift west — lands '2'/r78, never the x1120-1499 hole
                        p.al > 1530 && p.ak in 620..1100 -> mask = Pad.M_DOWN + Pad.M_LEFT   // lift bind zone — crouch-walk under: 36px box clears the y1500-1520 hover
                        p.al > 1530 -> mask = Pad.M_LEFT                    // '2'/r78/r87 — west to r78's x0 edge → drop to r96 (r87 east is walled: towers at x1020/x1500)
                        else -> mask = Pad.M_RIGHT                          // lift-transit band — east to the next sink lift
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
                56, 60 -> mask = if (chainDone && p.ak < 1110) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP  // tower face: release back to r87, else climb
                61 -> mask = if (chainDone && p.ak > 1300) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP
                62 -> mask = if ((p.al > 1500 && !(chainDone && p.al > 1530)) || (chainDone && p.ak < 1110)) Pad.M_DOWN else Pad.M_LEFT + Pad.M_UP  // hang: release on the tower face, climb elsewhere
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
                in 259..266 -> mask = Pad.M_RIGHT
            }
            if (jumpCd <= 0 && landed && w.kC == null && !chainDone) {
                mask = if (p.S in 259..266 || p.ak < 310) Pad.M_RIGHT + 8 else 16388
                jumpCd = if (p.S in 259..266) 8 else 14
            } else if (jumpCd <= 0 && stall > 0 && stall % 20 == 0 && !chainDone) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            } else if (jumpCd <= 0 && chainDone && stall >= 30 && p.aZ) {
                mask = if (p.al > 1530) (16390 or Pad.M_LEFT) else (16396 or Pad.M_RIGHT)  // '2'/r78/r87: hop west; lift band: east
                jumpCd = 30
            }
            if (chainDone && (p.S == 61 || p.S == 62 || p.S == 60) && p.ak > 1300) mask = Pad.M_DOWN   // gap rims: hang release beats every nudge
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
                if (w.kC!!.aw == 269) u269Fired = true }
            // slice 411: standing on the crates, the faithful S292 lunge keeps the player
            // grounded while mounted (S277+) — that is not the post-carry landing.
            if (!chainDone && u252Fired && w.kC == null && p.aZ && !(p.F != null && p.S >= 270)) chainDone = true
            if (kCFired) { if (p.al < minAl) minAl = p.al; if (p.ak > maxAk) maxAk = p.ak }
            // the descent ends on r77's type-2 strip: S50 with the anchor
            // in a type-2 cell of row 77 and x[1] zeroed (g.e(0))
            if (chainDone && p.S == 50 && p.x1 == 0 && p.al / 20 == 77 &&
                w.collisionCell(p.ak / 20, p.al / 20) == 2) {
                stripKill = true; killAt = "(${p.ak},${p.al})"; break
            }
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
            if (u269Fired) break
            if (p.al > 1760 && minAl < 1200) break
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            lastAk = p.ak; lastAl = p.al
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} u269=$u269Fired launched=$launched landed=$landed minAl=$minAl maxAk=$maxAk stripKill=$stripKill@$killAt trace=${trace.joinToString(" ")}")
        assertTrue(u269Fired || (minAl < 1200 && stripKill),
            "wing chain + strip-sealed descent not shown — ak=${p.ak} al=${p.al} S=${p.S} u269=$u269Fired minAl=$minAl maxAk=$maxAk stripKill=$stripKill")
    }
}
