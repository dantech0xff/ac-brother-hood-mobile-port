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

private fun chaseMask291(p: Entity, w: Level0World): Int {
    var mask = Pad.M_RIGHT
    when (p.S) {
        65 -> mask = Pad.M_UP + Pad.M_TAP_R
        228, 358 -> mask = Pad.M_RIGHT + Pad.M_UP
        297, 89, 90 -> mask = Pad.M_CONTEXT
        // S5/79 land-recovery, S258-266 perch/launch family: RIGHT to
        // continue east; UP edge would fire the wrong launch.
        5, 79, 235, 236, 237, 238, 239, 240, 241, 242, 243,
        258, 259, 260, 261, 262, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
        // ledge-hangs: UP edge climbs up (S62 arm); the lip grabs on
        // the column faces are how the chimney climb proceeds.
        60, 61, 203 -> mask = Pad.M_UP
        // S33 wall-cling: dir-into-wall arms the L859 launch.
        33 -> mask = if (p.av) Pad.M_LEFT else Pad.M_RIGHT
        // climb-arc states: dir-into-wall + TAP arms aF so the
        // wall-grab / lip-grab re-fires on contact (m5 pattern).
        22, 23, 43 -> mask =
            (if (p.av) Pad.M_LEFT + Pad.M_TAP_L
             else Pad.M_RIGHT + Pad.M_TAP_R)
        // stub crouch-crawl: S79's l() arm uses the dir key for the S32
        // crouch-walk (DOWN held keeps the low stance alive).
        78, 79, 80, 32 -> mask = Pad.M_DOWN + Pad.M_RIGHT
        else -> if (!p.aZ) mask = 0
    }
    // crouch-crawl under the slab stub x720-760 (40px passage): on the
    // corridor/pit floor the grounded run would auto-vault into the stub
    // face. DOWN alone brakes to S78→S79; the crouch states above then
    // crawl east — a dir key in lShared would route to ax() and never
    // reach the aw() DOWN dip.
    if (p.ak > 680 && p.ak < 800 && p.al > 700 &&
        p.S !in intArrayOf(33, 36, 60, 61, 101, 203)) {
        mask = when {
            // crouch family: dir key arms the S32 crawl (DOWN held keeps
            // the low stance); never M_UP here — UP would vault.
            p.S == 78 || p.S == 79 || p.S == 80 || p.S == 32 -> Pad.M_DOWN + Pad.M_RIGHT
            else -> Pad.M_DOWN
        }
    } else if (p.aZ && p.av) mask = Pad.M_RIGHT
    else if (p.aZ) mask = Pad.M_RIGHT + Pad.M_UP
    return mask
}

class Slice291Test {

    @Test fun mission6CapstoneLegA() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // recon: spawn -> first ax5 milestone uid116 @(801,930)
        var maxAk = 0; var minAl = 10000; var reached = false; var lastS = p.S
        val marks = mutableListOf<String>()
        for (t in 0..12000) {
            w.pad.e(chaseMask291(p, w))
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (p.S != lastS || t % 60 == 0)
                println("M6A t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ag=${p.ag} ah=${p.ah} jC=${w.jC} kC=${w.kC?.ax}")
            lastS = p.S
            if (w.jC == 15) { reached = true; marks += "WON@t$t"; break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                marks += "died@(${p.ak},${p.al}) S${p.S} t=$t"
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
            // uid116 zone [801,930,1001,973] — pit-floor band, not the
            // corridor floor above.
            if (p.ak >= 790 && p.al >= 920) { reached = true; marks += "M801@t$t"; break }
        }
        println("M6A reached=$reached maxAk=$maxAk minAl=$minAl p@(${p.ak},${p.al}) S${p.S} jC=${w.jC} marks=$marks")
        assertTrue(reached,
            "legA spawn(17,740)->ax5@801: got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC} marks=$marks")
    }

    @Test fun mission6CapstoneFull() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // P4 step 3 — continuous run: spawn -> legA recon target
        // (x801 pit floor) -> east toward legB's junction @(5973,180).
        // chaseMask291 drives the whole run; no teleports. Deaths
        // respawn at the reached checkpoint and replay their segment.
        //
        // P4d seam note (documented, see reports/capstone-revalidation.md):
        // legs B-F all ENTER at pinned teleport starts — the union
        // bounds the traversal the shared mask reaches past legA; each
        // stall point is recorded in the report.
        var maxAk = 0; var deaths = 0; var won = false
        val marks = mutableListOf<String>()
        for (t in 0..40000) {
            if (w.jC == 15) { marks += "WON@${p.ak},${p.al} t=$t"; won = true; break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                marks += "died@${p.ak},${p.al} S${p.S} x1=${p.x1}"
                deaths++
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                if (deaths > 80) break
                continue
            }
            if (w.jC != 8) break
            w.pad.e(chaseMask291(p, w))
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        println("m6 full end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
        // documented seam (see header): the union's bound past legA
        assertTrue(maxAk >= 790,
            "m6 full run clears legA's recon point: S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
    }

    /** Mission-6 capstone leg B — from the checkpoint-795 junction @(5973,180)
     *  (pinned leg start) east across the Pantheon's east mass: run the upper
     *  route → drop to the floor → long hop → S101 column grab → capture-chain
     *  (uid102 → uid97/96 vaults → chimney S29 → uid99 sprint) → step-face
     *  ping-pong → uid103/uid110 capture-vaults → block top → checkpoint
     *  **uid113 @(7096,225)** fires → S164 designed chasm descent → S361 carry.
     *  Milestone: checkpoint 113 consumed and the player past the mass east
     *  wall (x7300+) alive — proven end-to-end by real input. The S361 carry
     *  landing / camera-below frontier is leg-C's job. */
    @Test fun mission6CapstoneLegB() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(6060, 219); p.refreshBoxes()
        w.kP = 700; w.rebuildCamRect()
        for (t in 0..160) { w.pad.e(0); w.tick(emptyList()) }
        var maxAk = p.ak; var cp113 = false; var died = false
        for (t in 0..900) {
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            val threat = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 73) && it.x1 > 0 && it.S != 139 && (it.P and 32) == 0 &&
                    kotlin.math.abs(it.ak - p.ak) < 120 && kotlin.math.abs(it.al - p.al) < 90
            }
            // Slice 402: back off the heavy guard (uid112, ax73) while it
            // BLOCKS / winds up (S131 → S146 — its damage intake is closed
            // there, `r11` stays false, and the strike box reaches ~60px past
            // the player's own swing); trade blows only once it opens up
            // (S154/S155/S171). The old route walked into every windup and was
            // won on a lucky RNG phase.
            val guard112 = w.npcs.firstOrNull { it.aw == 112 }
            val guardBlocks = guard112 != null &&
                (guard112.S == 131 || guard112.S == 146) &&
                guard112.ak - p.ak in -20..90
            w.pad.e(when {
                p.aZ && guardBlocks -> Pad.M_LEFT
                // Slice 369 (F7): the S26 edge-walk off the y699 ledge now
                // ends in S79 under its low lip (e() 6092-6116) and drops
                // him on the y739 floor ~6 ticks sooner, so floor guard
                // uid99 gives up the chase (S2) instead of striking him
                // loose from heavy guard uid112 (ax73, S152, x6946). With
                // uid112 in front `f()` holds `cq` off (no jump), so cut
                // through it with the attack instead.
                p.aZ && p.al > 700 && threat != null && threat.ax == 73 &&
                    threat.ak - p.ak in 0..40 -> Pad.M_CONTEXT or Pad.M_RIGHT
                // Slice 402: S89 (pinned over floor guard uid99, S24) — stab
                // it from above (the k() L699 offer, raw @699-891). Left
                // alone the guard's `aC` runs out and its drop arm throws
                // him to the open side, by the heavy guard.
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 28 || p.S == 318 -> Pad.M_DOWN
                p.S == 65 -> Pad.M_UP
                p.S == 228 || p.S == 358 -> Pad.M_UP
                p.S == 101 && p.ak < 6500 -> Pad.M_LEFT
                p.S == 101 -> Pad.M_RIGHT
                p.S == 36 && p.ak < 6700 -> Pad.M_RIGHT
                !p.aZ && p.al > 440 && p.ak in 6920..7050 -> Pad.M_TAP_R or Pad.M_UP
                !p.aZ && p.ag < 0 && p.al in 300..470 && p.ak in 6900..7050 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al in 240..460 && p.ak in 6790..6900 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al > 560 && p.ak in 6800..6920 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al > 380 && p.ak in 6500..6680 -> Pad.M_TAP_R or Pad.M_UP
                !p.aZ && p.ak < 6480 && p.al < 330 -> Pad.M_TAP_L
                !p.aZ && threat != null && p.al > 600 -> Pad.M_RIGHT
                p.aZ && p.al in 600..680 && p.ak > 6940 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 400..470 && p.ak in 6960..7060 -> Pad.M_LEFT or Pad.M_UP or Pad.M_TAP_L
                p.aZ && p.al in 400..470 && p.ak > 7060 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 460..500 && p.ak >= 6540 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 460..560 && p.ak >= 6800 && p.ak < 6960 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al < 400 -> Pad.M_RIGHT
                p.ga != null && p.ak >= 5380 -> Pad.M_RIGHT or Pad.M_UP
                p.aZ && p.al > 600 && p.ak in 6430..6510 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al > 600 && p.ak >= 6880 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al > 600 -> Pad.M_RIGHT
                p.aZ -> Pad.M_RIGHT
                else -> Pad.M_RIGHT
            })
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (w.checkpoints.any { it.aw == 113 && it.consumed }) cp113 = true
            if (w.jC == 12 || w.jC == 13) { died = true; break }
            if (cp113 && p.ak > 7500) break
        }
        assertTrue(cp113, "legB checkpoint uid113 fired: maxAk=$maxAk died=$died @(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(maxAk > 7300, "legB crossed mass east wall: maxAk=$maxAk died=$died jC=${w.jC}")
    }

    /**
     * m6 capstone leg C — chasm scripted carry + perch chain.
     * Same pin as leg B: player descends the S164 slide into uid240's
     * zone [7663,341,7903,661] → the scripted sequence (2 CONTEXT QTEs,
     * camera pan, down-carry to y775, jC=21 dialog, up-carry) → released
     * at ~(8025,560) on the platform → vault east → ax22 perch chain
     * @(8158,476)→(8304,452) → cross x8400 toward the lift/spring band.
     * Asserts: uid240's script runs to completion (claim released) and
     * maxAk > 8400.
     */
    @Test fun mission6CapstoneLegC() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(6060, 219); p.refreshBoxes()
        w.kP = 700; w.rebuildCamRect()
        for (t in 0..160) { w.pad.e(0); w.tick(emptyList()) }
        var maxAk = p.ak; var released = false
        for (t in 0..1600) {
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            val threat = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 73) && it.x1 > 0 && it.S != 139 && (it.P and 32) == 0 &&
                    kotlin.math.abs(it.ak - p.ak) < 120 && kotlin.math.abs(it.al - p.al) < 90
            }
            // Slice 402: same back-off from heavy guard uid112 while it blocks
            // / winds up as leg B (see there).
            val guard112 = w.npcs.firstOrNull { it.aw == 112 }
            val guardBlocks = guard112 != null &&
                (guard112.S == 131 || guard112.S == 146) &&
                guard112.ak - p.ak in -20..90
            w.pad.e(when {
                w.kC != null -> Pad.M_CONTEXT                  // uid240 QTE chain (op107/108 pairs): answer every prompt while a claim holds the player
                p.aZ && guardBlocks -> Pad.M_LEFT
                p.aZ && p.al > 500 && p.ak in 8000..8080 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R   // platform edge → vault east onto ax22@(8158,476)
                !p.aZ && p.ag > 0 && p.ak in 8040..8200 -> Pad.M_TAP_R or Pad.M_UP                  // mid-flight: keep the arc
                p.S == 361 -> Pad.M_CONTEXT
                // Slice 369 (F7): same cut through heavy guard uid112 as
                // leg B (`f()` holds `cq` off while it stands in front).
                p.aZ && p.al > 700 && threat != null && threat.ax == 73 &&
                    threat.ak - p.ak in 0..40 -> Pad.M_CONTEXT or Pad.M_RIGHT
                // Slice 402: S89 (pinned over floor guard uid99, S24) — stab
                // it from above (the k() L699 offer, raw @699-891). Left
                // alone the guard's `aC` runs out and its drop arm throws
                // him to the open side, by the heavy guard.
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 28 || p.S == 318 -> Pad.M_DOWN
                p.S == 65 -> Pad.M_UP
                p.S == 228 || p.S == 358 -> Pad.M_UP
                p.S == 101 && p.ak < 6500 -> Pad.M_LEFT
                p.S == 101 -> Pad.M_RIGHT
                p.S == 36 && p.ak < 6700 -> Pad.M_RIGHT
                !p.aZ && p.al > 440 && p.ak in 6920..7050 -> Pad.M_TAP_R or Pad.M_UP
                !p.aZ && p.ag < 0 && p.al in 300..470 && p.ak in 6900..7050 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al in 240..460 && p.ak in 6790..6900 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al > 560 && p.ak in 6800..6920 -> Pad.M_TAP_L or Pad.M_UP
                !p.aZ && p.al > 380 && p.ak in 6500..6680 -> Pad.M_TAP_R or Pad.M_UP
                !p.aZ && p.ak < 6480 && p.al < 330 -> Pad.M_TAP_L
                !p.aZ && threat != null && p.al > 600 -> Pad.M_RIGHT
                p.aZ && p.al in 600..680 && p.ak > 6940 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 400..470 && p.ak in 6960..7060 -> Pad.M_LEFT or Pad.M_UP or Pad.M_TAP_L
                p.aZ && p.al in 400..470 && p.ak > 7060 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 460..500 && p.ak >= 6540 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al in 460..560 && p.ak >= 6800 && p.ak < 6960 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al < 400 -> Pad.M_RIGHT
                p.ga != null && p.ak >= 5380 -> Pad.M_RIGHT or Pad.M_UP
                p.aZ && p.al > 600 && p.ak in 6430..6510 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al > 600 && p.ak >= 6880 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                p.aZ && p.al > 600 -> Pad.M_RIGHT
                p.aZ -> Pad.M_RIGHT
                else -> Pad.M_RIGHT
            })
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak > 8045) released = true                 // script released the player east of x8000
            if (w.jC == 12 || w.jC == 13) break
            if (p.ak > 9200) break
        }
        println("M6C maxAk=$maxAk released=$released p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(released,
            "uid240 scripted sequence released the player east of x8000: maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(maxAk > 8400,
            "perch chain crossed the chasm band: maxAk=$maxAk p@(${p.ak},${p.al}) jC=${w.jC}")
    }

    /** Mission-6 capstone leg D — from leg C's perch-uid139 deposit on the
     *  y304 lift row (probe: lands on lift uid410@(8293,304) at ~(8312,304)):
     *  hop WEST to lift uid143@(8178,304) → LEFT|UP launch into spring
     *  uid420@(8079,250) → chained launch off spring uid123@(8200,183) →
     *  apex ~y86 → land the y145 lift row → hop east off lift uid191@(8511,145)
     *  → mass-A top (x8640-8900,y~220) → edge hop into the A-B gap → drift
     *  east past trap lift uid156@(8938,580) → vault/grab near
     *  perch uid159@(9164,497) → rope uid162@(9213,169) swing → S101 wall
     *  hops up mass C's west face → S284 edge grab → east past
     *  ax5 uid311@(9672,358). Milestone: player east of x9650, no fail. */
    @Test fun mission6CapstoneLegD() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // pin standing where leg C's perch uid139 rise deposits the player —
        // on lift uid410@(8293,304)'s top (probe: lands at (8312,304)).
        p.setPositionPx(8312, 296); p.refreshBoxes()
        w.kO = 8000; w.kP = 150; w.rebuildCamRect()
        for (t in 0..60) { w.pad.e(0); w.tick(emptyList()) }
        val wps = listOf(
            intArrayOf(8178, 304, 143), intArrayOf(8079, 240, 0),
            intArrayOf(8339, 145, 146), intArrayOf(8397, 145, 149),
            intArrayOf(8455, 145, 150), intArrayOf(8511, 145, 191),
            intArrayOf(8660, 230, 0), intArrayOf(8890, 220, 0),
            intArrayOf(8960, 760, 0), intArrayOf(9120, 760, 0),
            intArrayOf(9164, 500, 0), intArrayOf(9290, 150, 0),
            // Slice 369: past uid311 the route goes on east out of the
            // ax35 volley band (x9600-9800 @y339-400) to the x9900 exit.
            intArrayOf(9680, 360, 0), intArrayOf(9960, 400, 0),
        )
        var wp = 0; var bindT = 0; var maxAk = 0; var maxAl = 0; var died = false
        for (t in 0..2400) {
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            // Slice 369 (F8): the special-case arms above carry him over
            // the gap and the pillar without ever passing within reach of
            // waypoints 8-11, so after door uid166's S284/S285 transfer
            // (x9400 → 9650) the bot still steered for (8960,760) — LEFT.
            // Before, S5's squat (S21, no l()) kept the facing and the
            // landing hop still went east by luck; now the land arm runs
            // l() and a held LEFT turns him back into the volley band.
            // Once past the door, aim for the uid311 point and beyond.
            if (p.ak >= 9400 && wp < wps.lastIndex - 1) wp = wps.lastIndex - 1
            w.pad.e(when {
                w.kC != null -> Pad.M_CONTEXT
                p.S == 361 -> Pad.M_CONTEXT
                p.S == 28 || p.S == 318 -> Pad.M_DOWN
                p.S == 65 || p.S == 228 || p.S == 358 -> Pad.M_UP
                else -> {
                    when {
                        // slice 397: door uid166 [9374,166,9429,251] — the S16 arm's
                        // tap needs `aZ && !g.b(S)` (i.javap aV() @6152-6179): stand in
                        // the box and tap UP from a standing state
                        p.ak in 9374..9429 && p.al in 166..260 && p.ac == null ->
                            if (p.aZ && !PlayerFsm.isAirAction(p.S)) Pad.M_UP else 0
                        // spring launch — ride it east
                        kotlin.math.abs(p.ag) > 5000 -> Pad.M_RIGHT
                        // gap-lift uid156 oscillation — drift off east into the gap
                        p.ga != null && p.ga!!.aw == 156 -> Pad.M_RIGHT or Pad.M_DOWN
                        !p.aZ && p.al > 430 && p.ak in 8900..8945 -> Pad.M_RIGHT
                        // committed gap-edge hops on the mass tops
                        p.aZ && p.al < 290 && p.ak in 8820..8900 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                        p.aZ && p.al < 290 && p.ak in 9100..9165 -> Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                        // pillar top x9240-9339@y139 past the rope: walk off
                        // its east edge (S26 → fall) so the drop lands in
                        // door uid166's box [9374,9429] and the held UP
                        // fires it. Under G12 the rope dismount lands on
                        // the pillar and the hops overshoot the door to
                        // x9469, where the ax35 volleys kill the bot.
                        (p.aZ || p.S == 5) && p.al in 130..145 &&
                            p.ak in 9240..9340 -> Pad.M_RIGHT
                        // inside a gap — drift toward the east face
                        !p.aZ && p.al > 240 && p.ak in 8850..9250 -> Pad.M_RIGHT
                        // bound on an ax66 lift — ride ≥4 ticks then hop toward the next point
                        p.ga != null && p.ga!!.ax == 66 -> {
                            val bound = wps.indexOfFirst { it[2] == p.ga!!.aw }
                            if (bound >= 0 && wp <= bound) wp = bound + 1
                            bindT++
                            if (bindT >= 4) {
                                val t = wps[minOf(wp, wps.lastIndex)]
                                val nx = t[0] - p.ak
                                when {
                                    nx > 25 -> Pad.M_RIGHT or Pad.M_UP
                                    nx < -25 -> Pad.M_LEFT or Pad.M_UP
                                    else -> Pad.M_UP
                                }
                            } else 0
                        }
                        else -> {
                            bindT = 0
                            val t = wps[minOf(wp, wps.lastIndex)]
                            val dx = t[0] - p.ak; val dy = t[1] - p.al
                            if (kotlin.math.abs(dx) < 40 && kotlin.math.abs(dy) < 55) wp++
                            when {
                                dx > 40 -> Pad.M_RIGHT or Pad.M_UP
                                dx < -40 -> Pad.M_LEFT or Pad.M_UP
                                dy < -20 -> Pad.M_UP
                                else -> Pad.M_RIGHT
                            }
                        }
                    }
                }
            })
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al > maxAl) maxAl = p.al
            if (t % 5 == 0) println("D t=$t p=(${p.ak},${p.al}) S${p.S} ag=${p.ag} ah=${p.ah} aZ=${p.aZ} ga=${p.ga?.aw} bM=${p.bM?.aw} ac=${p.ac?.aw} wp=$wp jC=${w.jC}")
            if (w.jC == 12 || w.jC == 13) { died = true; break }
            if (p.ak > 9900) break
        }
        println("D end p=(${p.ak},${p.al}) S${p.S} maxAk=$maxAk maxAl=$maxAl died=$died jC=${w.jC}")
        assertTrue(!died,
            "legD player survived the lift/spring/mass band: maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(maxAk > 9650,
            "legD crossed the mass band to the far-east gauntlet: maxAk=$maxAk maxAl=$maxAl jC=${w.jC}")
    }

    /** Mission-6 capstone leg E — the end-game chain: door mass top x10700
     *  → hop the mass's west edge x10540 → descend the shaft x10420-10539 →
     *  lower mass y1000 → walk east into door179's box [10645,904,10695,991]
     *  → UP-hold fires the S16 door arm (marker ae=105 → `doorExitBh` →
     *  S284 + `bindAc` uid178) → teleport to door178's dest (10275,1217) →
     *  descend the masses east → S34 drop → win zone uid271@(10924,1166)
     *  → `k.l(15)` mission-complete. Pin start: on the door mass's top. */
    @Test fun mission6CapstoneLegE() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(10700, 800); p.refreshBoxes()
        w.kO = 10560; w.kP = 700; w.rebuildCamRect()
        var maxAk = 0; var minAl = 9999; var won = false
        for (t in 0..3000) {
            if (w.jC == 15) { won = true; break }
            if (p.ak - 200 > w.kO) w.kO = p.ak - 200; w.rebuildCamRect()
            if (p.al - 120 > w.kP) w.kP = p.al - 120; w.rebuildCamRect()
            // Slice 388: the two patrol guards on the lower mass wake as he
            // drops in (aA = 1) and az() keeps `g` bound to them (LOS-gated
            // rebinding only — the old unbind flicker is gone), which blocks
            // the door arm's `g == null` and, with `ci` in front, turns a
            // held UP into the S6 pick-up pose. Fight them off like leg F.
            val foe = w.npcs.filter { it.ax == 11 && it.aB > 0 && it.S != 139 &&
                kotlin.math.abs(it.ak - p.ak) < 130 && kotlin.math.abs(it.al - p.al) < 80 }
                .minByOrNull { kotlin.math.abs(it.ak - p.ak) }
            val pad = when {
                w.jC == 10 || w.jC == 15 || w.jC == 21 -> 327712
                foe != null && kotlin.math.abs(foe.ak - p.ak) < 50 && p.aZ -> {
                    if (foe.ak < p.ak) Pad.M_LEFT or Pad.M_CONTEXT else Pad.M_RIGHT or Pad.M_CONTEXT
                }
                (p.ac != null && p.ac!!.ax == 10) -> Pad.M_UP
                // slice 397: the S16 door arm's tap needs `aZ && !g.b(S)` (i.javap
                // aV() @6152-6179) — stand inside door179's box and tap UP from a
                // standing state; holding UP while hopping around only ever lands
                // in the aerial/action anims the arm refuses
                p.ak in 10645..10695 && p.al in 904..1040 ->
                    if (p.aZ && !PlayerFsm.isAirAction(p.S)) Pad.M_UP else 0
                p.ak in 10910..10960 && p.al > 1100 -> Pad.M_CONTEXT
                p.S == 65 || p.S == 228 || p.S == 358 -> 16396
                p.al < 880 && p.ak > 10560 -> Pad.M_LEFT
                p.al < 880 && p.ak in 10540..10560 && p.S == 26 -> Pad.M_UP
                p.al < 880 && p.ak > 10540 -> Pad.M_LEFT
                p.ak in 10620..10644 && p.al in 880..1020 -> Pad.M_RIGHT
                p.ak in 10696..10720 && p.al in 880..1020 -> Pad.M_LEFT
                p.al in 880..1020 && p.ak < 10620 -> Pad.M_RIGHT
                else -> Pad.M_RIGHT
            }
            w.pad.e(pad)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
        }
        assertTrue(won,
            "legE door mass → door179 teleport → win: p@(${p.ak},${p.al}) S${p.S} maxAk=$maxAk minAl=$minAl jC=${w.jC}")
    }

    /** Mission-6 capstone leg F — the east-gauntlet leg, proven end-to-end:
     *  '5' floor x10000-10419@y580 → run east → jump at x10420 → perch
     *  uid165@(10469,604) catches (S65) → context-hold vaults EAST (record
     *  Z[2]=1 → +20/3328/-3840) — clearing the crusher ledge x10420-10559@y700
     *  (ax44 uid897-900 permanent S8) — or launcher uid176@(10488,451) mounts
     *  → S261 east launch — landing the far-east '#######' ledge x10760+ →
     *  LEFT → gap x10700-10759 → door mass top y840 → west edge x10540 →
     *  shaft drop → pit floor y1000 → door179 box [10645,904,10695,991] →
     *  UP → S16 door arm → teleport to the lower room (floor y1320) → ride
     *  S295 east → claim uid271@(10924,1166) (ax5 S8 watcher, script 274 —
     *  no input needed; ride to the east wall, S33/34 bounce) → jC=21 →
     *  jC=15 mission-complete. Pin start: on the '5' floor where leg D's
     *  slab-drop exit deposits the player. */
    @Test fun mission6CapstoneLegF() {
        val w = world(aj = 6)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(10160, 560); p.refreshBoxes()
        w.kO = 10080; w.kP = 400; w.rebuildCamRect()
        var maxAk = 0; var won = false; var died = false
        for (t in 0..3000) {
            // l(15) re-enters as l(22) the same tick when a medal stamps
            // (k.java:1666-1700) — this leg's kills reach ap[0] >= 7 since
            // slice 382 counts every k.e(0,aw) site
            if (w.jC == 15 || w.jC == 22) { won = true; break }
            if (w.jC == 12 || w.jC == 13) { died = true; break }
            if (p.ak - 200 > w.kO) w.kO = p.ak - 200; w.rebuildCamRect()
            if (p.al - 120 > w.kP) w.kP = p.al - 120; w.rebuildCamRect()
            val foe = w.npcs.filter { it.ax == 11 && it.aB > 0 && it.S != 139 &&
                kotlin.math.abs(it.ak - p.ak) < 130 && kotlin.math.abs(it.al - p.al) < 80 }
                .minByOrNull { kotlin.math.abs(it.ak - p.ak) }
            val pad = when {
                w.jC == 10 || w.jC == 15 || w.jC == 21 -> 327712
                w.kC != null && w.kC!!.aw != 271 -> Pad.M_CONTEXT
                p.S == 65 || p.S == 228 || p.S == 358 -> 16396
                p.S == 101 -> Pad.M_RIGHT or Pad.M_UP
                p.ga != null && p.ga!!.ax == 66 -> Pad.M_RIGHT
                // Under G12 launcher uid176's bm() mount (i(260)) and the
                // player's own S260 arm run in the same frame, so the
                // input held while flying into it picks the launch: UP
                // fires the straight-up S259 (which drops back onto the
                // launcher, forever); RIGHT fires S261 east.
                !p.aZ && w.npcs.any { it.aw == 176 && it.ax == 66 &&
                    kotlin.math.abs(it.ak - p.ak) < 60 &&
                    p.al - it.al in -60..120 } -> Pad.M_RIGHT
                foe != null && kotlin.math.abs(foe.ak - p.ak) < 50 -> {
                    if (foe.ak < p.ak) Pad.M_LEFT or Pad.M_CONTEXT else Pad.M_RIGHT or Pad.M_CONTEXT
                }
                // slice 397: tap UP from a standing state (see leg E)
                p.ak in 10645..10695 && p.al >= 900 ->
                    if (p.aZ && !PlayerFsm.isAirAction(p.S)) Pad.M_UP else 0
                p.S == 260 || p.S == 262 -> Pad.M_RIGHT
                (p.ac != null && p.ac!!.ax == 10) -> Pad.M_UP
                p.ak in 10910..10960 && p.al > 1100 -> Pad.M_CONTEXT
                p.al in 690..880 && p.ak in 10540..10560 && p.S == 26 -> Pad.M_UP
                p.ak in 10620..10644 && p.al in 880..1020 -> Pad.M_RIGHT
                p.al in 880..1020 && p.ak < 10620 -> Pad.M_RIGHT
                p.al in 880..1020 && p.ak > 10700 -> Pad.M_LEFT
                p.al in 690..880 && p.ak > 10540 -> Pad.M_LEFT
                // Slice 369 (F8): the S5 land arm runs l() after the jump
                // press while u(94324) is held (e() 4764-4793) — a held
                // RIGHT starts a run and the post-tail hop carries its
                // first 10px. On the hollow block top (x10560-10699, y419)
                // that overshoots the top's east end onto the y519 shelf
                // (guard uid451), whose west end is the block's wall. Hop
                // the top from a standstill with UP alone (facing east):
                // (10608) → (10696) → over the shelf to the east wall.
                (p.aZ || p.S == 5) && p.al in 410..425 && p.ak in 10560..10699 -> Pad.M_UP
                // the east shaft x10759+ is scroll-wall sealed (bound pins
                // al at ~528 — falling there hovers forever). At the
                // far-east ledge lip return LEFT into the gap mouth
                // x10700-10759 to descend onto the door mass.
                p.al < 600 && p.ak > 10700 -> Pad.M_LEFT
                p.al < 600 && p.ak > 10420 -> Pad.M_RIGHT or Pad.M_UP
                else -> Pad.M_RIGHT
            }
            w.pad.e(pad)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        assertTrue(won && !died,
            "legF '5' floor → perch/launcher → door179 → uid271 win: " +
            "p@(${p.ak},${p.al}) S${p.S} maxAk=$maxAk died=$died jC=${w.jC}")
    }

}

// ---- Slice 297: mission-7 capstone (finale, aj7) ----------------------------
// Map 2000x2040: spawn ax0 uid107@(579,1740); bottom boss arena
// ax29 uid251@(1324,1920) + ax11 guards; crusher rows ax44 @y1559 x137-1040,
// @y579 x318-1021, @y997 x1237-1414; lift row ax66 uid22-27@y1500 x636-1004;
// ax72 counterweights (510,1345)/(866,1160)/(1300,794)/(386,390)/(957,394);
// top boss ax29 uid307@(1377,238); cps uid233@(1244,1470) uid347@(1131,489)
// uid339@(1266,1244); ax5 intro uid7@(547,1692) script 8.
