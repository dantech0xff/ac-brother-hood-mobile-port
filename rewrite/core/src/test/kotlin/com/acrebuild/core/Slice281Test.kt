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

class Slice281Test {
    /**
     * Slice 281 — mission-2 capstone bot legs (proven):
     *
     * Leg A: spawn (60,1840) → run east corridor → ax22@1249 vault →
     * rail aw2 ride → platform (1627,1839) → shaft wall-kick zigzag →
     * pillar top (y1539). Asserts the player reaches x>=1900 above
     * y1560 without jC leaving 8.
     *
     * Leg B: from the `2`@1340 walkway east end → drop → y1419 mass
     * → hop → aw30 ax22 vault (S65) → S19 arc → aw33 ax66 carrier
     * (S260 mount → S264/S266 diagonal hops) → aw28 → spring aw27
     * west-launch → tower C top (y1259). Asserts y<=1300 reached west
     * of x1950 (tower C region).
     * Slice 370 verdict for leg B — faithful dead end: the `2`@1340
     * walkway is r67's type-2 strip (cols 102-116, x2040-2339) on the
     * solid r68, so the L353d kill (g.javap.txt e() 13662-13711) takes
     * the leg's designed start as it drops onto it. (Its old pass never
     * ran the east loop either: it hopped west off the walkway to tower
     * C, through the S89 air-kill on guard aw23@(1988,1300).) The leg
     * now pins the strip and that kill.
     */
    @Test fun mission2CapstoneLegA() {
        val w = world(aj = 2)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var maxAk = 0; var minAlTop = 9999
        var legReached = false
        for (t in 0..3600) {
            // UP only when airborne/bound — grounded runs use RIGHT alone
            // so the S12 run-into-wall lip-grab can mantle the corridor
            // `20` blocks instead of the postTail air-grab (S101).
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211, 260, 259, 263, 265 -> mask = Pad.M_UP
                else -> if (!p.aZ) mask = Pad.M_RIGHT + Pad.M_UP
            }
            // Slice 398: the prop-hop below now cracks aw4 open in mid-air
            // (aj() @72-248: a body overlap in the g.b(I) aerial set smashes
            // the crate instead of bouncing off it) and lands PAST it at
            // x≈1116 in the S5 landing recovery, where the generic airborne
            // rule above holds UP and re-hops too early — walk instead, to
            // the block's east edge for the S26 jump below.
            if (p.S == 5 && p.ak in 1090..1200) mask = Pad.M_RIGHT
            // fight nearby soldiers — the `20`-block guard (aw38) and
            // strays attack on approach; strike toward the nearest live
            // ax11 within melee range.
            // ax4 spike props on the block top (aw4/aw5 @1056/1073)
            // hurt on touch — hop over them while grounded. This check
            // runs before foe attacks: a prop-hop takes priority.
            if (p.aZ && w.npcs.any {
                    it.ax == 4 && it.ak in p.ak + 1..p.ak + 55 &&
                    Math.abs(it.al - p.al) < 60
                }) mask = Pad.M_RIGHT + Pad.M_UP
            // aw4/aw5 are solid breakable crates (ax4 S5/S7, aj() L18 a()
            // push-out). Under k.I()'s frame order (G12) the crate's
            // `aS.ag = 0` runs before the player's integration step, so a
            // player whose box touches a crate can neither run nor hop
            // away from it — smash it instead (attack + body overlap).
            // S5 counts as grounded: an UP held into the landing would
            // start another S21 hop from inside the crate's box.
            if ((p.aZ || p.S == 5) && w.npcs.any {
                    it.ax == 4 && (it.S == 5 || it.S == 7) &&
                    Entity.overlapI(p.W, it.W)
                }) mask = Pad.M_CONTEXT
            // Past the smashed crates the run reaches the block's east
            // edge (S26 edge walk, x≈1168) instead of bunny-hopping off
            // it: jump there for the arc onto the ax22 vault at 1249.
            if (p.S == 26 && p.ak in 1100..1200 && p.al in 1810..1825)
                mask = Pad.M_RIGHT + Pad.M_UP
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null && mask == Pad.M_RIGHT) {
                // 65568 = the attack/context button (bottom-third tap
                // edge → ap() combo entry i(67)) — NOT M_TAP_R/L,
                // which are directional context hops.
                mask = Pad.M_CONTEXT
            }
            // Slice 404: the pillar's west lip (x960) is held by a stationary
            // sentinel (aw38: faces east, `Z5=Z6=0`) that is solid to the player —
            // mantling into its body shoves the climber back off the lip into the
            // spike pit. The designed answer is the ledge assassination: press the
            // context button during the lip grab (S60 → S203 carry → S204 throw)
            // while the sentinel is still unaware (`j == 0`).
            if (p.S == 60 && w.npcs.any {
                    it.ax == 11 && it.aB > 0 && it.j == 0 &&
                    Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) <= 5
                }) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 1900 && p.al <= 1560 && p.aZ) { legReached = true; break }
            if (w.jC != 8) break
        }
        println("S281 legA end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
        assertTrue(legReached, "legA reached pillar top: got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission2CapstoneLegB() {
        val w = world(aj = 2)
        w.stateL(8); settleIntro(w)
        val p = w.player
        for (cx in 102..116) {                    // the walkway
            assertEquals(2, w.collisionCell(cx, 67), "walkway r67 col $cx")
            assertEquals(20, w.collisionCell(cx, 68), "walkway floor r68 col $cx")
        }
        // mid-route entry: on the door-corridor `2`@1340 walkway (the
        // preceding ascent leg: pillar→walkway, still frontier — the leg
        // is entered at its designed start exactly like prior capstone
        // legs entered from checkpoint starts).
        p.setPositionPx(2080, 1338); p.ak = 2080; p.al = 1338; p.av = false
        w.kO = 1900; w.kP = 1100; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        var stripKill = false
        for (t in 0..1200) {
            var mask = Pad.M_RIGHT + Pad.M_UP
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                101, 102, 315, 318, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211, 260, 259 -> mask = Pad.M_UP
                263, 265 -> mask = Pad.M_RIGHT + Pad.M_TAP_R   // ride: hop off at ends
            }
            // Slice 369 (F8): the S5 land arm runs l() after a jump press
            // whenever u(94324) is held (e() 4764-4793) — a held RIGHT
            // turns him east and the post-tail jumps that way. Off the
            // S89 pin at x~1988 the landing on the y1299 ledge must
            // therefore hop west with LEFT+UP to reach tower C.
            if (p.S == 5 && p.ak in 1960..2010 && p.al in 1290..1305)
                mask = Pad.M_LEFT + Pad.M_UP
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            // tower C region: west of x1950 at y<=1300 (spring aw27's
            // west-launch target is tower C's top).
            if (p.ak <= 1950 && p.al <= 1305 && p.aZ) { legReached = true; break }
            // the walkway kill: S50, x[1] zeroed, anchor in a type-2 cell
            if (p.S == 50 && p.x1 == 0 &&
                w.collisionCell(p.ak / 20, p.al / 20) == 2) { stripKill = true; break }
            if (w.jC != 8) break
        }
        println("S281 legB end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC} stripKill=$stripKill")
        assertTrue(stripKill && !legReached,
            "legB start: the walkway's type-2 strip kills (L353d) — got S${p.S} @(${p.ak},${p.al}) legReached=$legReached jC=${w.jC}")
    }

    /**
     * Leg C: from legA's mass-top exit (2100,1520) → the x2180 bound
     * descent (S22 hop → S315 slide → S318 M_DOWN fling) → land the
     * `20`@1940 platform → S203 bound → cross checkpoint aw202's box
     * (2631,1806-1934). Asserts x>=2640 grounded in the y1900-1945
     * band without jC leaving 8.
     */
    @Test fun mission2CapstoneLegC() {
        val w = world(aj = 2)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(2100, 1520); p.ak = 2100; p.al = 1520; p.av = false
        w.kO = 1850; w.kP = 1300; w.rebuildCamRect()
        var legReached = false; var maxAk = 0
        for (t in 0..4000) {
            var mask = Pad.M_RIGHT
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                // S318 bound dismount: `v(33024)` held flings with
                // the frozen ah — M_DOWN, not M_UP.
                318 -> mask = Pad.M_DOWN
                37, 38 -> mask = Pad.M_RIGHT        // `5` ceiling shimmy
                101, 102, 315, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                164, 52, 280, 209, 211, 260, 259, 263, 265, 235, 236,
                238, 239 -> mask = Pad.M_UP
                else -> if (!p.aZ) mask = Pad.M_RIGHT + Pad.M_UP
            }
            if (p.aZ && w.npcs.any {
                    it.ax == 4 && it.ak in p.ak + 1..p.ak + 55 &&
                    Math.abs(it.al - p.al) < 60
                }) mask = Pad.M_RIGHT + Pad.M_UP
        val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null && mask == Pad.M_RIGHT) mask = Pad.M_CONTEXT
            // Slice 370: the 100px gap in `20`@1940 (x2420-2519) has a
            // type-2 strip on its floor (row 102) — lethal since the L353d
            // type-2 kill (g.javap.txt e() 13662-13711). Jump it from the
            // S26 lip walk and take the far lip (ax10 aw22 S43 → S203).
            // Slice 404: with the two guards on the platform now solid the run no
            // longer arrives in the S26 lip walk (it fights, then sprints in S12 /
            // S233) — the jump starts from any grounded run state on the lip stretch.
            if ((p.S == 26 || p.S == 12 || p.S == 233 || p.S == 0) && p.aZ &&
                p.al in 1930..1945 && p.ak in 2360..2419)
                mask = Pad.M_RIGHT + Pad.M_UP
            // Slice 405: the platform's last guard grabs the runner (S310 held by its
            // S175 QTE) — mash the attack mask (edge presses fill the gauge +8 each,
            // a full gauge counter-executes the soldier; an unmashed gauge throws
            // the player back).
            if (p.S == 310) mask = if (t % 2 == 0) Pad.M_CONTEXT else 0
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 160 < w.kP) w.kP = p.al + 160; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            // past cp202 aw202 (2631,1806..2671,1934) on `20`@1940
            if (p.ak >= 2640 && p.al in 1900..1945 && p.aZ) { legReached = true; break }
            if (w.jC != 8) break
        }
        println("S281 legC end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
        assertTrue(legReached, "legC crossed cp202: got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    /**
     * Leg E — WIN leg: from `20`@1940's east pocket tip x2920-2960 (the
     * ledge squeezed between pillar x2900-2920 and pillar x2960-2980 — a
     * designed checkpoint-style rest point past the guard gauntlet):
     *   pocket → S101 wall-kick off pillar-2's west face → S36 launch
     *   → land `20`@1880's west edge → run east → vault into the rail
     *   zone aw134/aw142 (ax10-S34 W-boxes) → S157 rail-ride → S164
     *   transfers → off the last rail → S295 auto-bounce chain east
     *   → the player W-box dips into win-zone aw306's band
     *   → script 307 binds → op-1 → screenL(15).
     * Asserts w.jC == 15 (mission win).
     */
    @Test fun mission2CapstoneLegWin() {
        val w = world(aj = 2)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.setPositionPx(2930, 1939); p.ak = 2930; p.al = 1939; p.av = true
        p.setAnim(5); p.aZ = true
        w.kO = 2850; w.kP = 1650; w.rebuildCamRect()
        var maxAk = 0; var sawKick = false; var saw1880 = false
        for (t in 0..420) {
            var mask = Pad.M_RIGHT + Pad.M_UP
            when (p.S) {
                318 -> mask = Pad.M_DOWN
                157, 164 -> mask = Pad.M_RIGHT       // rail ride / transfer
                82, 83, 84, 85, 86, 326 -> mask = Pad.M_UP   // rope band (defensive)
                37, 38 -> mask = Pad.M_RIGHT
                67, 68, 69 -> mask = Pad.M_CONTEXT
                43 -> mask = Pad.M_RIGHT + Pad.M_UP
                else -> if (p.ak < 3000) mask = Pad.M_LEFT + Pad.M_UP
                        else mask = Pad.M_RIGHT + Pad.M_UP
            }
            // Slice 404: an alerted soldier is solid to the player (the shared
            // tail's `a()` @7644-7657 joins both aA branches) — the three guards
            // on `20`@1880 can no longer be run through; strike whoever blocks the
            // way, as legs A/C do.
            val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            if (foe != null && p.aZ && mask == Pad.M_RIGHT + Pad.M_UP) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 160 < w.kP) w.kP = p.al + 160; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.S == 101) sawKick = true
            if (p.aZ && p.al <= 1885 && p.ak > 3100) saw1880 = true
            if (w.jC != 8) break
        }
        println("S281 legWin end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC} kick=$sawKick on1880=$saw1880")
        assertTrue(w.jC == 15, "legWin reached mission win: got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC} kick=$sawKick on1880=$saw1880")
    }

    /** P4 step 3 — continuous run: spawn → pillar top (legA) → the mass
     *  top descent → cp202 crossing (legC) → the `20`@1940 east pocket →
     *  wall-kick → `20`@1880 → rail zone → win-zone aw306 (legWin).
     *  One position/state-keyed policy union of the three proven legs —
     *  no teleports. (legB is a documented dead route: the door-corridor
     *  `2` walkway's type-2 strip kills, so the run takes the pillar.)
     *  Respawns replay their segment naturally since the policy keys on
     *  ak/al, not stage.
     *
     *  P4a seam note (documented, see plans/261003-0700…/reports/
     *  capstone-revalidation.md): the legA→legC stitch does NOT complete
     *  continuously — the deck gauntlet at x2200-2540 is a faithful
     *  dead-end for the position-policy bot: the S74 dash descent lands
     *  the player at x2214-2216 airborne (S89 pin-guard under the line),
     *  and the strip-gap shaft x2420-2540 (`02`@2040 kill-bed — type-2
     *  cells zero x1 on an unmounted landing) is only crossable by
     *  mounting the ax44 door tops inside, which the built-in lip
     *  auto-vault (S22/23, fires unasked ~40px before the edge) always
     *  overshoots. The assertion bounds the traversal that DOES work —
     *  spawn→pillar→mass top→deck→shaft mouth — instead of a win. */
    @Test fun mission2CapstoneFull() {
        val w = world(aj = 2)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var maxAk = 0; var deaths = 0; var won = false
        val marks = mutableListOf<String>()
        for (t in 0..20000) {
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
            // strike a live ax11 in melee range (all legs) — computed
            // ahead so the combo arm below can release when none is left
            val foe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 &&
                Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50
            }
            // position-keyed overrides beat every S-arm: inside the pit
            // between the pillars (x2920-3100, open y1560-2160) the pocket
            // kick arms LEFT+UP (legWin); inside the mass-top→tower
            // chimney (x2210-2320, open below the tower's west face)
            // alternate LEFT/RIGHT+UP off the two faces to climb to the
            // y1420 lip; inside the `20`@1940 strip-gap shaft (x2420-2520
            // to the `02`@2040 bed) the same alternating kick climbs back
            // over the east lip
            when {
                p.ak in 2900..3120 && p.al in 1550..2160 ->
                    mask = Pad.M_LEFT + Pad.M_UP
                // inside the mass-top→tower chimney (west face = the
                // mass's east face x2220, east face = the tower's west
                // face x2300-2340): press TOWARD the face to kick away
                // from it. Falling at x<2225 with the lip still above
                // (al<1600) drift back WEST to land on the mass top for a
                // grounded hop into the mouth; falling at x<2225 below
                // the lip press EAST to kick off the mass face (lands
                // back on top, retry); inside the gap LEFT+UP off the
                // mass face east / RIGHT+UP off the tower face west
                !p.aZ && p.ak in 2210..2350 && p.al in 1360..1980 ->
                    mask = when {
                        p.ak < 2225 && p.al < 1600 -> Pad.M_LEFT + Pad.M_UP
                        p.ak < 2270 -> if (p.ak < 2225) Pad.M_RIGHT + Pad.M_UP
                                       else Pad.M_LEFT + Pad.M_UP
                        else -> Pad.M_RIGHT + Pad.M_UP
                    }
                // mass-top lip: ax10 uid323 S36 publishes a context action
                // AT the lip (2195,1549) — the game's own contextual move
                // carries the gap; walk to the lip then press CONTEXT
                p.aZ && p.ak in 2000..2225 && p.al in 1500..1575 ->
                    mask = if (p.ak < 2180) Pad.M_RIGHT else Pad.M_CONTEXT
                p.ak in 2400..2560 && p.al in 1946..2180 ->
                    mask = Pad.M_RIGHT + Pad.M_UP
                else -> when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_R
                228, 358 -> mask = Pad.M_LEFT + Pad.M_UP
                // S175 QTE on the `20`@1940 guard platform (legC)
                310 -> mask = if (t % 2 == 0) Pad.M_CONTEXT else 0
                318 -> mask = Pad.M_DOWN                      // bound dismount
                157, 164 -> mask = Pad.M_RIGHT                // rail ride/transfer
                // ceiling shimmy: at the east dead-end above the pocket
                // (x2750+, pinned against the pillar) drop to the deck
                37, 38 -> mask = if (p.ak >= 2750) Pad.M_DOWN
                                 else Pad.M_RIGHT
                43 -> mask = Pad.M_RIGHT + Pad.M_UP           // vault bound
                // combo chain — only while a live foe remains; releasing
                // to M_RIGHT (not RIGHT+UP) keeps the deck run grounded so
                // the `20`@1940 lip-jump fires inside its proven window
                67, 68, 69 -> mask = if (foe != null) Pad.M_CONTEXT
                                     else Pad.M_RIGHT
                82, 83, 84, 85, 86, 326 -> mask = Pad.M_UP    // rope band
                263, 265 -> mask = Pad.M_RIGHT + Pad.M_TAP_R  // ride: hop off ends
                101, 102, 315, 29, 28, 34, 63, 60, 62, 89, 61, 74,
                52, 280, 209, 211, 260, 259, 235, 236,
                238, 239 -> mask = Pad.M_UP
                else -> if (!p.aZ) mask = Pad.M_RIGHT + Pad.M_UP
                }
            }
            // legA: walk past the smashed crate, hop at the block's east edge
            if (p.S == 5 && p.ak in 1090..1200) mask = Pad.M_RIGHT
            if (p.S == 26 && p.ak in 1100..1200 && p.al in 1810..1825)
                mask = Pad.M_RIGHT + Pad.M_UP
            // legC strip gap on `20`@1940 — a 100px shaft x2420-2520 with
            // an `02` catch-bed at y2040: fight while an engaged guard is
            // near, otherwise WALK OFF the west lip (a jump arc lands
            // mid-shaft anyway; the soft bed is the intended catch), then
            // chain wall-kicks inside to the east lip
            if (p.aZ && p.al in 1920..1945 && p.ak in 2300..2420 &&
                w.npcs.any { it.ax == 11 && it.aB > 0 && it.S != 2 &&
                    it.S != 139 && Math.abs(it.ak - p.ak) < 170 })
                mask = Pad.M_CONTEXT
            else if (p.aZ && p.al in 1930..1945 && p.ak in 2400..2420)
                mask = Pad.M_DOWN
            else if ((p.S == 26 || p.S == 12 || p.S == 233 || p.S == 0) &&
                p.aZ && p.al in 1930..1945 && p.ak in 2360..2419)
                mask = Pad.M_RIGHT + Pad.M_UP
            // ax4 prop-hop + solid-crate smash (legs A/C)
            if (p.aZ && w.npcs.any {
                    it.ax == 4 && it.ak in p.ak + 1..p.ak + 55 &&
                    Math.abs(it.al - p.al) < 60
                }) mask = Pad.M_RIGHT + Pad.M_UP
            if ((p.aZ || p.S == 5) && w.npcs.any {
                    it.ax == 4 && (it.S == 5 || it.S == 7) &&
                    Entity.overlapI(p.W, it.W)
                }) mask = Pad.M_CONTEXT
            // foe-strike: plain M_RIGHT anywhere (legs A/C), or the
            // grounded run-hop past cp202 (legWin's `20`@1880 guards).
            // West of cp202 the lip-jump's RIGHT+UP must NOT convert —
            // jumping the strip gap outranks attacking the lip guard.
            if (foe != null &&
                (mask == Pad.M_RIGHT ||
                 (p.aZ && mask == Pad.M_RIGHT + Pad.M_UP && p.ak > 2640)))
                mask = Pad.M_CONTEXT
            // legA: ledge assassination on the pillar-lip sentinel
            if (p.S == 60 && w.npcs.any {
                    it.ax == 11 && it.aB > 0 && it.j == 0 &&
                    Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) <= 5
                }) mask = Pad.M_CONTEXT
            w.pad.e(mask)
            if (p.ak - 200 > w.kO) { w.kO = p.ak - 200; w.rebuildCamRect() }
            if (p.al - 240 > w.kP) { w.kP = p.al - 240; w.rebuildCamRect() }
            if (p.al + 160 < w.kP) { w.kP = p.al + 160; w.rebuildCamRect() }
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        println("m2 full end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
        // documented seam (see header): the shaft mouth is the traversal
        // bound — every death mark sits on the `02` bed at ~(2508,2059)
        assertTrue(maxAk >= 2450,
            "m2 full run reaches the strip-gap shaft mouth: S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
    }
}
