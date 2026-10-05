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

class Slice302Test {

    /** m7 leg G — west-wing landing + platform chain: the u33 spring arc
     *  lands at ~(128,1379) on the wall-top edge; the route climbs
     *  east-up — '05' one-way platform x140-280@y1300 → platforms
     *  u45/47/248 → ax4 crate column → mlogic u252@(301,1057) claim
     *  zone. The x0-100 pocket west is a dead-end (sealed shaft). */
    @Test fun mission7WestWingLanding() {
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
        var landAk = -1; var landAl = -1; var landed = false
        var launched = false; var kCFired = false; var minAl = 99999; var maxAk = 0
        for (t in 0..20000) {
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
                !landed -> mask = if (p.aZ) Pad.M_LEFT else Pad.M_LEFT + Pad.M_UP
                // after the arc — hop up into the u242 claim box
                // (x150-293, y1296-1322); once claimed, feed CONTEXT
                w.kC != null -> mask = Pad.M_CONTEXT
                else -> mask = if (p.ak < 210) Pad.M_RIGHT else Pad.M_LEFT
            }
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                // slice 413: `g.c(Z)` reads the RAW `k.g` — the '5' shimmy under the x140-299 ledge
                // stops at the '20' slab (x<140); UP on an S38 tick vaults onto the ledge (S54)
                37, 38, 280 -> if (launched && !landed) mask = Pad.M_UP
                56, 60, 61, 62 -> mask = Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
                in 259..266 -> mask = Pad.M_RIGHT   // perch — drift east while arcs resolve
            }
            if (jumpCd <= 0 && landed && w.kC == null) {
                mask = if (p.S in 259..266 || p.ak < 310) Pad.M_RIGHT + 8 else 16388
                jumpCd = if (p.S in 259..266) 8 else 14
            } else if (jumpCd <= 0 && stall > 0 && stall % 20 == 0) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            }
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
            if (w.kC != null && w.kC!!.aw == 252) kCFired = true
            if (kCFired) { if (p.al < minAl) minAl = p.al; if (p.ak > maxAk) maxAk = p.ak }
            if (launched && !landed && p.aZ && p.al > 1280) { landed = true; landAk = p.ak; landAl = p.al }
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
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            lastAk = p.ak; lastAl = p.al
            if (kCFired && w.kC == null && p.aZ) break   // carry released, grounded
            if (p.al < 1000 && kCFired) break
            if (stall == 600 || stall == 2000 || stall == 5000) {
                val near = w.npcs.filter { kotlin.math.abs(it.ak - p.ak) < 140 && kotlin.math.abs(it.al - p.al) < 140 }
                    .joinToString { "ax${it.ax}#${it.aw}@(${it.ak},${it.al})S${it.S}" }
                println("STALL t=$t ak=${p.ak} al=${p.al} S=${p.S} aZ=${p.aZ} near=$near")
            }
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} kC=${w.kC?.aw}/${w.kC?.scriptStep} launched=$launched landed=$landed@($landAk,$landAl) minAl=$minAl maxAk=$maxAk trace=${trace.joinToString(" ")}")
        assertTrue(launched && landed && kCFired && minAl < 1100 && maxAk > 350,
            "west-wing chain not climbed — ak=${p.ak} al=${p.al} S=${p.S} kC=${w.kC?.aw} minAl=$minAl maxAk=$maxAk")
    }
}
