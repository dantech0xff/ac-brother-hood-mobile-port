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

class Slice307Test {

    /** m7 leg K — cp339 east floor → WEST descent: corridor pit →
     *  deep floor y1500 → '5' underside shimmy → far-west y1559.
     *  Proven this slice: cp339 (script-341 dialog) fires at
     *  (1266,1244); the corridor's east end is permanently sealed by
     *  the one-way fuse box uid51 (W=[1462,1199,1479,1284] — arms
     *  S10→8→4, auto-vault loop at x1449; passable only descending
     *  the chimney at y1259). West along the corridor floor y1280 →
     *  pit x1040-1219 → deep floor y1500 → west → claim-QTE kC=240
     *  at x791 (mount S273-277 carries west to x637) → hop up to the
     *  '5' ledge x140-299@y1300 (S22 → S280 hang → S38) → UP vaults onto
     *  its top (S54 → S0 @(270,1298)) → west along the slab top to the map
     *  edge x18@y1299.
     *  Slice 413 verdict — the far-west pocket (x0-99, floor y1559) is
     *  sealed, so the old "west region floor" ending was an artifact. `g.c(Z)`
     *  (g.javap.txt @40) tests the shimmy's facing cell with the RAW `k.g`
     *  read, not `i.e()` (whose S37 pass-through let the old port walk the
     *  shimmy through the slab): cols 0-6 of rows 65-66 are `20`, the wall
     *  cols 5-6 run down rows 67-78, the floor row 78 closes the pocket —
     *  the shimmy stops at x≈164 and the only way on is the UP vault.
     *  Remaining legs for the boss/win (next
     *  slice): west floor → mid-block/lift chain (uid221-223@y940-976
     *  catch falling riders, dive `ag=±4864,ah=-6656` onto ledge
     *  x420-779@y820) → pillar x340-359 top y540 → band y560 →
     *  x1040 vault → arena floor → cp347@(1131,489) → uid281 script-316
     *  + ax10-S55 uid315@(1380,520) boss-reposition → boss-3 ax29
     *  uid307@(1377,238) → fuse uid303/uid271 script 304 → missionWon. */
    @Test fun mission7WestDescent() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        println("POSTDRIVE @(${p.ak},${p.al}) S=${p.S} aZ=${p.aZ} ag=${p.ag} ah=${p.ah} F=${p.F?.ax}#${p.F?.aw} bM=${p.bM?.ax}#${p.bM?.aw}")
        val log = StringBuilder("CELLS cy55-80:\n")
        for (cy in 55..80) {
            val runs = StringBuilder()
            var runStart = -1; var last = -1
            for (cx in 45..80) {
                if (w.collisionCell(cx, cy) != 0) {
                    if (runStart < 0) runStart = cx
                    last = cx
                } else if (runStart >= 0) {
                    runs.append("[${runStart * 20}-${last * 20 + 19}] "); runStart = -1
                }
            }
            if (runStart >= 0) runs.append("[${runStart * 20}-${last * 20 + 19}] ")
            if (runs.isNotEmpty()) log.append("  cy$cy y${cy * 20}-${cy * 20 + 19}: $runs\n")
        }
        println(log.toString())
        // park on the east floor where the catapult chain lands —
        // release the rope bind first or bM teleports him back
        val rope37 = p.bM; p.bM = null; rope37?.bM = null; p.aA = 0
        p.ak = 1270; p.al = 1250; p.av = false; p.setAnim(0); p.aZ = true
        p.ag = 0; p.ah = 0; p.F = null
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        val trace = ArrayDeque<String>(80)
        var lastS = p.S
        var jumpCd = 0; var pressCd = 0; var catchCd = 0
        var cp339 = false; var pitDeep = false; var shimmyWest = false
        var westEnd = false; var deaths = 0
        for (t in 0..6000) {
            var mask = chaseMask297(p, w)
            when {
                p.bM != null -> mask = Pad.M_UP                         // rope-bound — climb
                p.S == 358 -> mask = if (!p.av) Pad.M_LEFT else Pad.M_UP // hang release: face-away + M_UP → S235 west ride
                p.S in 259..266 || p.S == 235 || p.S == 238 -> mask = 0   // bound carries — hands off
                p.S in listOf(37, 38, 280) -> mask = if (p.ak < 300) Pad.M_UP else Pad.M_LEFT  // '5' underside shimmy — west, then UP onto the ledge (slice 413)
                p.S in listOf(33, 34, 101, 102, 146, 147) -> mask = Pad.M_LEFT or Pad.M_UP
                else -> mask = Pad.M_LEFT                                 // descent goes west throughout
            }
            // slice 395: the counterweight catch is the grab prompt — `i.at`
            // armed (`aY()` @1555: idle/falling, facing it, in view) and the
            // player in a mountable state; a human taps it the moment the
            // indicator shows. The old bot only tapped while the uid240 claim
            // held, so the catch depended on how its combo taps happened to
            // line up with the walk (a few ticks of duel/walk drift lost it).
            // Slice 410: the catch has its OWN cooldown — the claim-QTE taps below
            // (every 10 ticks while the uid240 claim holds) used to share one, so a
            // hit-free duel that ended a few ticks earlier or later left the claim's
            // cooldown running exactly when the prompt showed and the bot fell past.
            if (Entity.at?.ax == 72 && p.mountableState() && catchCd <= 0) {
                mask = Pad.M_CONTEXT; catchCd = 10
            }
            // claim-QTE: any prompt → CONTEXT
            else if (w.kC != null && pressCd <= 0) { mask = Pad.M_CONTEXT; pressCd = 10 }
            pressCd--; catchCd--
            w.pad.e(mask)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (!cp339 && p.ak in 1200..1330 && p.al in 1200..1300) { cp339 = true; println("CP339 t=$t @(${p.ak},${p.al})") }
            if (!pitDeep && p.al in 1450..1520) { pitDeep = true; println("PITDEEP t=$t @(${p.ak},${p.al})") }
            if (!shimmyWest && p.ak < 300 && p.al in 1280..1360) { shimmyWest = true; println("SHIMMY t=$t @(${p.ak},${p.al})") }
            if (!westEnd && p.aZ && p.ak < 60 && p.al in 1280..1320) { westEnd = true; println("WESTEND t=$t @(${p.ak},${p.al})"); break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                deaths++
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                println("RESPAWN t=$t deaths=$deaths @(${p.ak},${p.al})")
                continue
            }
            if (w.jC != 8) break
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            if (stall == 150) println("STALL t=$t @(${p.ak},${p.al}) S=${p.S} aZ=${p.aZ} mask=$mask")
            lastAk = p.ak; lastAl = p.al
            if (t % 500 == 0 || t < 400) println("POS t=$t @(${p.ak},${p.al}) S=${p.S} aZ=${p.aZ} mask=$mask kC=${w.kC?.aw} ga=${p.ga?.ax}#${p.ga?.aw} at=${Entity.at?.ax}#${Entity.at?.aw}")
        }
        println("END @(${p.ak},${p.al}) S=${p.S} westEnd=$westEnd deaths=$deaths cp339=$cp339 pit=$pitDeep shimmy=$shimmyWest trace tail=${trace.takeLast(15).joinToString(" ")}")
        // the pocket below is sealed (slice 413): slab cols 0-6 on rows 65-66, wall cols 5-6, floor row 78
        for (cx in 0..6) { assertEquals(20, w.collisionCell(cx, 65)); assertEquals(20, w.collisionCell(cx, 66)) }
        for (cy in 67..77) { assertEquals(20, w.collisionCell(5, cy)); assertEquals(20, w.collisionCell(6, cy)) }
        for (cx in 0..6) assertEquals(20, w.collisionCell(cx, 78))
        assertTrue(cp339 && pitDeep && shimmyWest && westEnd,
            "m7 west descent not completed — @(${p.ak},${p.al}) S=${p.S} deaths=$deaths cp339=$cp339 pit=$pitDeep shimmy=$shimmyWest westEnd=$westEnd trace tail=${trace.takeLast(15).joinToString(" ")}")
    }
}
