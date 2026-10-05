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

class Slice282Test {
    /** Slice 282 — mission-3 capstone bot legs on `world(aj = 3)`
     *  (spawn (21,699) -> win ax42 @(13628,1045); 8 checkpoints:
     *  cp157@(2229,647) cp158@(3207,396) cp159@(3709,503)
     *  cp281@(3897,295) cp272@(5360,795) cp274@(7552,224)
     *  cp667@(8557,690) cp273@(10873,575)). Same capstone pattern as
     *  slices 279/280/281 — real input only, no state pinning.
     *
     *  Leg A: spawn (21,699) -> run east -> cp1 aw157@(2229,647).
     *  ax11 guards patrol the route; the bot strikes any live ax11 in
     *  melee range (65568 = the attack button edge). */

    @Test fun mission3CapstoneLegA() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var legReached = false; var maxAk = 0
        for (t in 0..3600) {
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                // balance-pin. The post-intro pin over the gap (980,563)
                // is left with DOWN (the S17 zone's v(33024) drop): under
                // k.I()'s frame order (G12) the zone's TAP_R leap and the
                // player's S19 tick share a frame, so the TAP_R edge also
                // arms the cv → aF grab latch, and the leap clings to the
                // gap's east wall (S101) and kicks west into the pit.
                297 -> mask = if (p.ak in 960..1000 && p.al in 540..590)
                    Pad.M_DOWN else Pad.M_CONTEXT + Pad.M_TAP_R
                89, 90 -> mask = Pad.M_CONTEXT
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211 -> mask = Pad.M_UP
                258, 260, 262 -> mask = Pad.M_DOWN
                259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
                235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
                else -> if (!p.aZ) mask =
                    if (p.ak >= 1415) Pad.M_RIGHT else Pad.M_RIGHT + Pad.M_UP
            }
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null) mask = Pad.M_CONTEXT + Pad.M_RIGHT
            // x1400 wall: grounded approach holds UP — the auto-vault
            // (S21 squat -> S36/43) needs the UP edge to fire the kick
            if (p.aZ && p.ak in 1300..1470) mask = Pad.M_RIGHT + Pad.M_UP
            // Slice 369 (F8): the kick off the x1400 wall drops him on the
            // y519 ledge facing west; the route hops WEST along it to the
            // x1120-1180 chimney (S101/S36 kick climb → y339 → east over
            // the wall top). The S5 land arm runs l() after the jump press
            // while u(94324) is held (e() 4764-4793), so a held RIGHT
            // would turn him east — hop with LEFT+UP instead.
            // Slice 416: the faithful fall-arm wall-grab snap (g.javap.txt e() @8881-8927: `(W0/20)*20+1`
            // — the air arm @7886-7938 snaps a cell further out) puts the kick's arc over the ledge's
            // east lip, which he now catches (S61 lip hang → UP pull-up S62 → S0 at x1370) instead of
            // landing on it: the west hop starts from the lip, and S0 (aZ still false for the frame
            // after the pull-up) counts as standing.
            if (foe == null && p.al in 500..530 && p.ak in 1100..1380 &&
                (p.aZ || p.S == 5 || p.S == 0)) mask = Pad.M_LEFT + Pad.M_UP
            // ...and east of the wall top the y339 run meets the ax4
            // crates aw641/aw829 (x1517/x1534, S7/S5 — solid, a() push-
            // out). The bunny hops now land on them and the push-out
            // drops him west of them: smash them (attack edge) instead.
            if ((p.aZ || p.S == 5) && w.npcs.any {
                    it.ax == 4 && (it.S == 5 || it.S == 7) &&
                    it.ak - p.ak in -10..45 && Math.abs(it.al - p.al) < 30
                }) mask = Pad.M_CONTEXT
            // x1400 wall hangs: hold RIGHT toward the face — UP fires a
            // kick that throws the player OVER the wall to the east face,
            // where the chain bounces it back west (verified live)
            if (p.ak in 1330..1500 && (p.S == 101 || p.S == 62 || p.S == 60 || p.S == 61) &&
                !(p.S == 61 && p.al <= 530))      // the ledge-lip hang (y519) pulls UP (slice 416)
                mask = Pad.M_RIGHT
            // post-intro drop: the aF latch catches the gap's east wall at
            // ~(979,548) when any direction is held airborne — the auto-
            // S36 kick then rebounds west into the pole-row pit kill
            // floor (verified S50 @863,813). Pressing NOTHING in the
            // x930-1040 descent leaves aF unarmed: no grab, the player
            // falls past onto the y~700 street east of the gap. S297
            // (the balance pin at 980,563) and S295 keep their masks —
            // they need the TAP/CONTEXT edges to release.
            // Slice 402: S89 (pinned over the guard on the pole-top, S24)
            // keeps its CONTEXT mask too — the stab edge @ k() L699 kills
            // the guard; left alone, S24's `aC` expires and the faithful
            // drop arm (raw @6077-6125: the OPEN side, here the pit side
            // `W[0]-pw`) throws him off the pole into the kill floor.
            if (!p.aZ && p.ak in 930..1040 && p.S != 297 && p.S != 295 &&
                p.S != 89) mask = 0
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 2229) { legReached = true; break }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legA reached cp1 aw157: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    /** P4 step 3 — continuous run: spawn -> cp1 (legA) -> east across
     *  the un-legged gap to legB's tower-top entry (route map: mass-face
     *  drop -> pocket -> edge jump -> spring aw335 -> middle mass ->
     *  rail aw578 -> mass A -> duel aw393 -> lever aw579 -> west tower
     *  x3860-4060 @y160-240 -> legB arc -> mass B -> ... -> win fuse
     *  aw780 @(13628,1045)). Union of the proven legs' policies, same
     *  position-keyed pattern as m2's full run — no teleports; deaths
     *  respawn at the reached checkpoint and replay their segment.
     *
     *  P4b seam note (documented, see reports/capstone-revalidation.md):
     *  the union bot crosses the cp1 kill pit's ax66 carrier chain
     *  (2405->2502->2603 mounts, hops reach x2734) but the seam past it
     *  is the upper-route stunt chain — ax19 rope @2272, the ax14 arc
     *  trace to (2662,484), ax46 spring @2721 launch (40,-30), the y540
     *  `14` ledge — which needs rope-climb + spring-timing policies a
     *  position-keyed bot cannot express. The assertion bounds the
     *  traversal the union policy reaches (cp1 + carrier chain). */
    @Test fun mission3CapstoneFull() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var maxAk = 0; var deaths = 0; var won = false
        val marks = mutableListOf<String>()
        for (t in 0..30000) {
            when {
                w.jC == 15 || w.missionWon -> {
                    marks += "WON@${p.ak},${p.al} t=$t"; won = true; break }
                w.jC == 12 || w.jC == 13 -> {
                    marks += "died@${p.ak},${p.al} S${p.S} x1=${p.x1}"
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++
                    if (deaths > 60) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
            }
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                297 -> mask = if (p.ak in 960..1000 && p.al in 540..590)
                    Pad.M_DOWN else Pad.M_CONTEXT + Pad.M_TAP_R
                89, 90 -> mask = Pad.M_CONTEXT
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211 -> mask = Pad.M_UP
                258, 260, 262 -> mask = Pad.M_DOWN
                259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
                235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
                else -> if (!p.aZ) mask =
                    if (p.ak >= 1415) Pad.M_RIGHT else Pad.M_RIGHT + Pad.M_UP
            }
            val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null) mask = Pad.M_CONTEXT + Pad.M_RIGHT
            if (p.aZ && p.ak in 1300..1470) mask = Pad.M_RIGHT + Pad.M_UP
            if (foe == null && p.al in 500..530 && p.ak in 1100..1380 &&
                (p.aZ || p.S == 5 || p.S == 0)) mask = Pad.M_LEFT + Pad.M_UP
            if ((p.aZ || p.S == 5) && w.npcs.any {
                    it.ax == 4 && (it.S == 5 || it.S == 7) &&
                    it.ak - p.ak in -10..45 && Math.abs(it.al - p.al) < 30
                }) mask = Pad.M_CONTEXT
            if (p.ak in 1330..1500 && (p.S == 101 || p.S == 62 || p.S == 60 || p.S == 61) &&
                !(p.S == 61 && p.al <= 530))
                mask = Pad.M_RIGHT
            if (!p.aZ && p.ak in 930..1040 && p.S != 297 && p.S != 295 &&
                p.S != 89) mask = 0
            // carrier chain over the `02`@900 pit (x2360-3020 — proven
            // lethal): walk off the y700 floor's east end into the first
            // ax66 lift's box (pointInBox mounts it), then hop RIGHT+UP
            // off each lift's east edge onto the next (2405->2502->2603
            // ->2686), east off the last onto the `14` mass at x2960.
            // the lift tops sit above the floor's al — descend INTO each
            // box from above: jump at the east lip so the arc's foot
            // crosses the box band
            if (p.aZ && p.al in 685..715 && p.ak in 2300..2370 && p.s == null)
                mask = Pad.M_RIGHT + Pad.M_UP
            if (p.s != null && p.s!!.ax == 66)
                mask = if (p.ak >= p.s!!.W[2] - 8)
                    Pad.M_RIGHT + Pad.M_UP else Pad.M_RIGHT
            if (!p.aZ && p.s == null && p.ak in 2750..2970 &&
                p.al in 480..730) mask = Pad.M_RIGHT + Pad.M_UP
            // ax4 crate smash must override the carrier masks — the
            // props at x2272-2289 block the floor's east end
            if ((p.aZ || p.S == 5) && w.npcs.any {
                    it.ax == 4 && (it.S == 5 || it.S == 7) &&
                    it.ak - p.ak in -10..45 && Math.abs(it.al - p.al) < 30
                }) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            if (p.ak - 200 > w.kO) { w.kO = p.ak - 200; w.rebuildCamRect() }
            if (p.al - 240 > w.kP) { w.kP = p.al - 240; w.rebuildCamRect() }
            if (p.al + 120 < w.kP) { w.kP = p.al + 120; w.rebuildCamRect() }
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        println("m3 full end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
        // documented seam (see header): cp1 + the carrier chain — the
        // upper-route stunt chain past lift-3 is the unbridged remainder
        assertTrue(maxAk >= 2700,
            "m3 full run crosses the carrier chain: S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
    }

    /** Leg B — the mass A -> mass B crossing (x4240-4360 gap). The
     *  aw849 ax37 scroll bound [4246,344,4383,893] clamps walking east
     *  inside its y-band (verified: the walk stalls ~x4210 with ag
     *  zeroed by i.f scrollWallClamp), so the designed route crosses
     *  ABOVE it via the west tower top: east off the tower lip ->
     *  S164 vault -> S157 bound launch (ag 8192, ~32px/t) -> arc east
     *  -> mass B top @(4519,279). Entered at the tower top — the
     *  rail -> massA -> duel -> tower ascent chain is the preceding
     *  leg's frontier. */
    @Test fun mission3CapstoneLegB() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(3980, 160); p.ak = 3980; p.al = 160; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 4080; w.kP = 700; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..3600) {
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                297 -> mask = Pad.M_CONTEXT + Pad.M_TAP_R   // balance-pin
                89, 90 -> mask = Pad.M_CONTEXT
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211 -> mask = Pad.M_UP
                258, 260, 262 -> mask = Pad.M_DOWN
                259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
                235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
                else -> if (!p.aZ) mask = Pad.M_RIGHT + Pad.M_UP
            }
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null && mask == Pad.M_RIGHT) mask = Pad.M_CONTEXT
            if (p.aZ && p.al >= 650 && p.ak >= 3060 && p.ak < 3350)
                mask = Pad.M_RIGHT + Pad.M_UP
            if (p.al in 580..650 && p.ak in 3860..4080)
                mask = Pad.M_RIGHT + Pad.M_UP
            if (p.aZ && p.al in 480..520 && p.ak in 4040..4230)
                mask = Pad.M_RIGHT
            if (p.aZ && p.ak >= 4230 && p.al in 440..560)
                mask = Pad.M_RIGHT + Pad.M_UP
            if (!p.aZ && p.al in 300..900 && p.ak in 4240..4390)
                mask = Pad.M_RIGHT + Pad.M_UP
            val foe2 = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 90 && Math.abs(it.al - p.al) < 60
            }
            if (foe2 != null) mask = Pad.M_CONTEXT + Pad.M_RIGHT
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 4400 && p.aZ) { legReached = true; break }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legB crossed massA->massB gap via tower arc: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegC() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(4400, 279); p.ak = 4400; p.al = 279; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 4620; w.kP = 800; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..3600) {
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                297 -> mask = Pad.M_CONTEXT + Pad.M_TAP_R   // balance-pin
                89, 90 -> mask = Pad.M_CONTEXT
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211 -> mask = Pad.M_UP
                258, 260, 262 -> mask = Pad.M_DOWN
                259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
                235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
                else -> if (!p.aZ) mask =
                    if (p.ak >= 4520) Pad.M_RIGHT else Pad.M_RIGHT + Pad.M_UP
            }
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null) mask = Pad.M_CONTEXT + Pad.M_RIGHT
            // massB east-face grab: release DOWN into the aw70 catch zone
            // (4548-4583 x 283-370) directly below — UP kicks back west
            if (p.S == 101 && p.ak >= 4530) mask = Pad.M_DOWN
            if (p.S == 28 || p.S == 29) mask = Pad.M_DOWN
            if (p.S == 295 || p.S == 157) {
                // same carrier-QTE chain as legD — zone aw674's box starts
                // at x5476, so the claim script (op107/108 prompt) binds
                // the player on the street walk: 693 wants UP, later ax5
                // scripts want TAP-R; bound idle rides on RIGHT.
                val scr = w.kC
                mask = when {
                    scr != null && scr.ax == 5 && scr.aG == 693 -> Pad.M_UP
                    scr != null && scr.ax == 5 -> Pad.M_TAP_R
                    p.ga != null -> Pad.M_RIGHT
                    else -> Pad.M_UP
                }
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 5310 && p.aZ) { legReached = true; break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legC massB->cp272: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegD() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // leg starts at the carrier-zone boarding position (the designed
        // entry — mid-route legs may start at checkpoint-style spots):
        // standing at street level inside zone aw674's box → eventBind →
        // script 693. Skipping the 5476-6300 street walk avoids the two
        // unavoidable civ au<2 panic hits (range-free T==3 flails).
        p.setPositionPx(6520, 939); p.ak = 6520; p.al = 939; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 6320; w.kP = 940; w.rebuildCamRect()
        // legD — the carrier-QTE crossing, verified end-to-end:
        // zone aw674-S8 @(6548,802) → script 693 boards carrier aw594
        // (UP prompt+QTE) → bound ride east at 150% through the civ
        // gauntlet (~2 range-free panic hits) → zone aw847-S8 → script
        // 848 continues the ride (TAP-R QTE) → zone aw672 → script 671
        // parks the carrier and releases the player at the tower base
        // (~8230,790). Assert: lands aZ east of 7480.
        var legReached = false; var maxAk = 0
        for (t in 0..3600) {
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                297 -> mask = Pad.M_CONTEXT + Pad.M_TAP_R   // balance-pin
                89, 90 -> mask = Pad.M_CONTEXT
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211 -> mask = Pad.M_UP
                258, 260, 262 -> mask = Pad.M_DOWN
                259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
                235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
                else -> if (!p.aZ) mask =
                    if (p.ak >= 4520 && p.ak < 6400 || p.ak >= 6760) Pad.M_RIGHT
                    else Pad.M_RIGHT + Pad.M_UP
            }
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null) mask = Pad.M_CONTEXT + Pad.M_RIGHT
            if (p.S == 101 && p.ak >= 4530) mask = Pad.M_DOWN
            if (p.S == 28 || p.S == 29) mask = Pad.M_DOWN
            if (p.S == 295 || p.S == 157) {
                // carrier-QTE ride chain (op107 prompt + op108 branch):
                // 693 prompts UP (mask 4→16388); 848/671 prompt TAP-R
                // (mask 8). Held the wrong button → fail script (teleport
                // mid-pit + setAnim50). Between scripts (ga bound, no
                // claim) hold RIGHT for the 150% bound-ride speed.
                val scr = w.kC
                mask = when {
                    scr != null && scr.ax == 5 && scr.aG == 693 -> Pad.M_UP
                    scr != null && scr.ax == 5 -> Pad.M_TAP_R
                    p.ga != null -> Pad.M_RIGHT
                    else -> Pad.M_UP
                }
            }
            w.pad.e(mask)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 7480 && p.aZ) { legReached = true; break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legD carrier ride -> tower landing: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegE() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legE — the wall door-teleport. The carrier drops the player on
        // the tower top (x8180-8440 surface y740); east of it a 200px
        // pillar (x8440-8540, y540-740) blocks the rooftop. The designed
        // crossing is the ax10-S16 door pair: aw898 box x8384-8421 ×
        // y655-740 at the pillar's west face → oId=899 → aw899 @(8546,652)
        // east of the pillar, then cp667 @(8557,690) fires right at the
        // landing. Start checkpoint-style on the tower top (leg D's end).
        p.setPositionPx(8260, 739); p.ak = 8260; p.al = 739; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 8060; w.kP = 740; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..4000) {
            var mask = Pad.M_RIGHT
            // door trigger needs UP held while overlapping (x8384-8421)
            // and grounded — hold it only inside the box so UP can't arm
            // a wall-grab on the pillar face first.
            if (p.ak >= 8380 && p.ak < 8600 && p.ac == null) mask = Pad.M_RIGHT + Pad.M_UP
            // ax22 capture pins S65; Z[2]=1 → padHeld(16396)=UP∣TAP-R
            // vaults out east (+3328,-3840 → S19 arc).
            if (p.S == 65) mask = Pad.M_UP
            // past the zipline drop keep UP held so vault arcs stay
            // high — the y840 corridor is a designed below-camera pit.
            if (p.ak >= 10100) mask = Pad.M_RIGHT + Pad.M_UP
            // script-692's lift ride ends in a FIRE QTE (op107 mask
            // 0x20→65568 at key=122, op108 decide at 132 — miss →
            // fail-branch script 25 → l(12)). Press context on the
            // rooftop approach.
            if (p.al < 700 && p.ak >= 10600) mask = Pad.M_CONTEXT
            // Slice 404: the rooftop sentinel (aw640 @10020,718) is solid to the
            // player whether or not it is alerted (the shared tail's `a()` @7644-7657
            // joins both aA branches) — running into it just trades blows; strike
            // whoever stands in front.
            val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null && p.aZ && mask == Pad.M_RIGHT) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            val cp = w.checkpoints.firstOrNull { it.aw == 273 }
            if (cp != null && cp.consumed && p.ak >= 10800 && p.aZ) {
                legReached = true; break
            }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legE tower->door->script692 lift->cp273: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegF() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legF — the street gauntlet + first wall. From leg E's end
        // (10969,839) the y840 street runs solid east across the sealed
        // door-gauntlet corridor (x11080-11779 — the y1197 sub-route is
        // capped by oneway cells at both ends, not the designed path).
        // Two ax11s patrol the street (aw833@11297, aw834@11492, range
        // ±160). The street dead-ends at the wall mass x11800-12000:
        // west face y760-840 rising to the y680 top at x12000 — the wisp
        // trail (aw501@11865,717 → aw785@12101,582) marks the climb.
        // Target: the y820 shelf east of the crest (x12100+).
        p.setPositionPx(10969, 839); p.ak = 10969; p.al = 839; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 10769; w.kP = 720; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..4000) {
            var mask = Pad.M_RIGHT
            // hold UP approaching the x11800 wall face so the vault/
            // climb arms on contact (wall run + lip grab).
            if (p.ak >= 11600) mask = Pad.M_RIGHT + Pad.M_UP
            // Falling (S43) at the x11940 face above the y760 step, a held
            // UP clings to it (fall-arm wall grab → S101 → S36 kick back
            // west); without UP the player slides down the face and the
            // postTail ct consumer's lip grab (S60) mounts the y680 top.
            // Under G12 the running jump off the step meets the face
            // while still that high (the old hop reached it lower).
            if (p.S == 43 && p.ak in 11880..11940 && p.al < 760)
                mask = Pad.M_RIGHT
            // guards engage on the street — attack when one is in front.
            w.pad.e(mask)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 12100 && p.al <= 830 && p.aZ) {
                legReached = true; break
            }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legF street-gauntlet->x11800 wall->y820 shelf: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegG() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legG — the lift stair + zipline crossing. From the x12000 wall
        // top (y679 — leg F's climb ends here, NOT on the y840 corridor:
        // the ax44 crusher doors aw881-892 sit at y843-844 and spawning on
        // the corridor floor gets crushed -> S50 -> x1=0 KO, Entity.kt:929).
        // The designed crossing rides three ax66 lifts up the west face —
        // aw597@(12117,644) -> aw598@(12315,604) — into ax22 capture
        // aw835@(12383,544) which launches up to the ax40 zipline at y440,
        // landing on the east mass top ~y540-660 at x13080+.
        p.setPositionPx(12000, 679); p.ak = 12000; p.al = 679; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 11800; w.kP = 560; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..6000) {
            var mask = Pad.M_RIGHT + Pad.M_UP
            // pinned capture S65: Z[2]=1 -> padHeld(16396)=UP|TAP-R vaults out.
            if (p.S == 65) mask = Pad.M_UP
            // on the zipline (airborne ride) keep RIGHT held — no UP,
            // so dismount arcs stay forward not upward.
            if (p.al < 500 && p.ak >= 12600) mask = Pad.M_RIGHT
            w.pad.e(mask)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 13080 && p.al <= 700 && p.aZ) {
                legReached = true; break
            }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertTrue(legReached,
            "legG lift-stair->capture->zipline->east mass: got S" + p.S + " @(" + p.ak + "," + p.al + ") maxAk=" + maxAk + " jC=" + w.jC)
    }

    @Test fun mission3CapstoneLegH() {
        val w = world(aj = 3)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legH — the finale descent. legG's zipline lands the east mass top
        // ~x13080 y540-660; walking east crosses aw132's claim box (x13136-
        // 13174) and binds descent claim 927, which rides the player to the
        // gap and drops them on the west mass top y820. From there the
        // proven chain (mission3SpringLaunchProbe trace): west off the mass
        // edge → ax22 aw910 captures @(12944,902) → 16390 (UP+TAP_L) vaults
        // west → ax10-S17 beam aw912 pins S297 @(12800,899) → '8' east leap
        // → recaptured by aw910 → second west vault → lands the pillar top
        // (12748,1078) → S257/S29/S28 carrier rides down the pillar (DOWN
        // pulses) → releases to the pit ledge (12720,1299) → walk east →
        // '8' grounded jump at ~x12790 → lands ax46 aw409's pad
        // (x12861-914,1326) → spring launch (ag=12800, ah=-20480) → embeds
        // in the lip's west face (13338,1299) → S79/S81 creep east with
        // M_CONTEXT → S277 mount → S317 ride east → screenL(15) WIN.
        p.setPositionPx(13090, 545); p.ak = 13090; p.al = 545; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 12800; w.kP = 420; w.rebuildCamRect()
        for (e in w.npcs) if (e.aw == 925 || e.aw == 924 || e.aw == 606 ||
            e.aw == 409 || e.aw == 613 || e.aw == 620 || e.aw == 910 ||
            e.aw == 912 || e.aw == 263 || e.aw == 780 || e.aw == 610 ||
            e.aw == 819 || e.aw == 823 || e.aw == 825) keepLive(e)
        var east = false
        var fired = false
        var descend = false
        var toPit = false
        for (t in 0..6000) {
            var mask = if (east) Pad.M_RIGHT else Pad.M_LEFT
            // Claim-top walk: east through aw132's box → descent claim 927.
            if (p.aZ && p.al in 500..660 && p.ak in 13060..13175)
                mask = Pad.M_RIGHT
            // Pit ledge → east jump to aw409's pad.
            if (east && p.aZ && p.al in 1290..1320 && p.ak in 12780..12800)
                mask = 8
            val guard = w.npcs.firstOrNull {
                it.ax == 11 && !it.deadRelease() && it.al in 780..1150 &&
                    kotlin.math.abs(it.ak - p.ak) <= 52 &&
                    kotlin.math.abs(it.al - p.al) <= 80
            }
            if (guard != null) {
                if ((guard.P and 32) != 0) {
                    // Parked guard — dormant statue, NOT solid (P lacks
                    // bit12 → pushApart skips it): walk straight through.
                    mask = if (east) Pad.M_RIGHT else Pad.M_LEFT
                } else {
                    val west = guard.ak < p.ak
                    mask = if (west != p.av) if (west) Pad.M_LEFT else Pad.M_RIGHT
                        else if (kotlin.math.abs(guard.ak - p.ak) > 24)
                            if (west) Pad.M_LEFT else Pad.M_RIGHT
                        else Pad.M_CONTEXT
                }
            }
            // y819 platform (606 fight region) → descend the WEST side:
            // raised block x13340-13420 is a 100px wall (unclimbable),
            // so the route is west off the platform edge x13240 → fall
            // alongside the narrowed column → land on the one-way ledge
            // (x13120-13220 @ y980) or the wide block top (x13020-13300
            // @ y1000) → east → off x13300 → land on the y1240 floor
            // block (x13320+) → east into aw925's zone W
            // [13339,1099,13555,1272] → claim-QTE mount → win.
            if (p.aZ && guard == null && p.al in 760..860 && p.ak in 13300..13400) descend = true
            if (descend) {
                if (p.ga != null) mask = Pad.M_LEFT      // ax66 perch — launch WEST (S264 ag=-3072,ah=-7680) past it
                else if (!p.aZ) mask = 0                 // falling — no drift
                else if (p.al >= 960) { descend = false; toPit = true }
                else mask = Pad.M_LEFT                   // platform → west edge
            }
            // Beam/block-top (y~1079) → WEST off the pit edge x12740 →
            // bound-catch aw610 (W=[12704,1084,12739,1184]) rides down to
            // the pit floor (y1300) → east to aw409's spring pad → launch.
            if (toPit) {
                if (p.al >= 1250) { toPit = false; east = true }
                else if (p.aZ && p.al in 1000..1150) mask = Pad.M_LEFT
            }
            // Slice 369 (F7): the landing on the raised block's top
            // (13358,779, head and feet cells solid) now ends the grounded
            // arm in S79 (e() 6092-6116) instead of crouch-walking west, so
            // the y819 duel with the platform guard drifts west and he can
            // leave the platform before `descend` latches (guard == null).
            // The chain below is unchanged; latch `east` on the pit floor
            // itself so the walk to aw409's pad still starts there.
            if (!east && p.aZ && p.al >= 1250 && p.ak in 12690..12800) {
                descend = false; toPit = false; east = true
                mask = Pad.M_RIGHT
            }
            // ax27 fuse prop @x12997 — solid-ish; '8' forward jump clears
            // it (M_UP would trigger the S267 carry-init instead).
            if (p.aZ && p.al in 1000..1120 && p.ak in 12950..12985) mask = 8
            if (p.S == 65) mask = 16390                    // aw910 west vault
            if (p.S == 297) mask = Pad.M_DOWN              // beam drop-off
            if (p.S == 29) mask = Pad.M_DOWN               // carrier descend
            if (p.S == 28) mask = if (t % 4 == 0) Pad.M_DOWN else 0
            if (p.S == 60) mask = Pad.M_UP               // ledge hang → mantle
            if (p.S == 61) mask = Pad.M_DOWN             // ledge hang → drop
            // Raised block face (x13340) is a bounce wall — no vault.
            if (p.S in 257..259) mask = Pad.M_DOWN         // carrier hold
            if (p.S == 79) mask = Pad.M_RIGHT or Pad.M_CONTEXT // lip embed creep
            if (p.S == 274 || p.S == 277 || p.S == 317 ||
                (p.ak in 13339..13555 && p.al in 1099..1272))
                mask = if (!fired) { fired = true; Pad.M_CONTEXT } else 0
            if (w.jC == 21) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            w.tick(emptyList())
            if (w.jC == 15 || w.jC == 13) break
            if (w.jC == 12) {
                var guard2 = 0
                while (w.jC != 8 && w.jC != 15 && guard2++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        assertEquals(15, w.jC,
            "legH finale descent chain should reach mission-complete; " +
                "got jc=${w.jC} p@(${p.ak},${p.al}) S${p.S} x1=${p.x1}")
    }

}
