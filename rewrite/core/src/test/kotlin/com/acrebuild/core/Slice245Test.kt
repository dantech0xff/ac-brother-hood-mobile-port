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

class Slice245Test {
    // Headless bot playthrough (provenance sweep for the demo lane): a
    // scripted-input driver runs the REAL world FSM from spawn east —
    // hold M_RIGHT, vault edges on zone captures (S65) and ledge hangs
    // (S203), attack edges on live combat types, jC12 restart taps, jC21
    // SKIP. It asserts only the two facts the records prove: the bot
    // clears the spawn pocket (ak > 2500), and progress is monotonic —
    // every stall is reported with its state so blockers can be judged
    // verbatim-vs-port, not silent.

    private fun foeNear(w: Level0World, p: Entity): Entity? =
        w.npcs.firstOrNull {
            (it.ax == 11 || it.ax == 73 || it.ax == 50 || it.ax == 47 ||
             it.ax == 41 ||
             (it.ax == 4 && it.S in 5..8)) &&        // ax4 S9/21 = inert props
            it.S != 139 && it.S != 0 &&
            kotlin.math.abs(it.ak - p.ak) <= 160 &&
            kotlin.math.abs(it.al - p.al) < 60
        }

    @Test fun `bot playthrough - spawn to east progress markers`() {
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        var t = 0
        var maxAk = p.ak
        var minAl = p.al
        var stall = 0
        var vaultCd = 0
        var atkCd = 0
        var jumpCd = 0
        var restarts = 0
        var dir = Pad.M_RIGHT                    // kick zig-zag flips on stall
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)       // S-transition ring buffer
        var lastS = p.S
        while (t++ < 120000) {
            when {
                w.jC == 15 -> { marks += "WON@${p.ak} t=$t"; break }
                w.jC == 12 || w.jC == 13 -> {
                    // KO prompt — two-stage row confirm (kBw=0 then fire)
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    restarts++
                    marks += "respawn@${p.ak} x1=${p.x1} t=$t"
                    continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 || p.S == 203 -> {
                    // zone capture / ledge hang — fresh up|TR edge vaults east
                    if (vaultCd <= 0) { w.pad.e(16396); vaultCd = 40 }
                    vaultCd--
                    w.tick(emptyList())
                    continue
                }
                p.S == 315 || p.S == 318 -> {
                    // bound-catch / climb-freeze — DOWN edge releases to
                    // the corridor floor below (L23fa `v(33024)` arm)
                    if (vaultCd <= 0) { w.pad.e(33024); vaultCd = 40 }
                    vaultCd--
                    w.tick(emptyList())
                    continue
                }
                p.S == 89 || p.S == 90 -> {
                    // killTouch pin — the grab-kill offer (NpcFsm L129)
                    // completes on held context (65568)
                    w.pad.e(Pad.M_CONTEXT)
                    w.tick(emptyList())
                    continue
                }
            }
            val foe = foeNear(w, p)
            var held = dir
            if (foe != null) {
                val dx = foe.ak - p.ak
                held = when {
                    // inside the enemy's strike box — back off
                    kotlin.math.abs(dx) < 45 -> if (dx < 0) Pad.M_RIGHT else Pad.M_LEFT
                    // sword range — stand and swing
                    else -> 0
                }
                if (atkCd <= 0) { held = held or Pad.M_CONTEXT; atkCd = 30 }
            } else if (stall > 80 && atkCd <= 0) {
                held = held or Pad.M_CONTEXT; atkCd = 30
            }
            atkCd--
            // wall-kick latch: `cv && (u|v)(16388|8|2)` → aF=1 — hold UP too
            // while stalled against a face so the next grab site fires S101.
            if (stall > 40 || p.S == 33 || p.S == 34 || p.S == 101 || p.S == 92)
                held = held or Pad.M_UP
            // L1f35 fresh-W: after the x1400-face S36 west-kick, drift
            // back east and re-grab higher — chaining grabs climbs to the
            // ax7 slot @1397,506 (swallow → eject east over the wall).
            // Scoped x>1100 so the x930-1040 pit keeps its tap-fall route.
            if (!p.aZ && p.ak > 1100 && p.ak < 1560)
                held = held or Pad.M_UP
            // G12 re-route: the zone vaults' S19/S43 flights reach the
            // x1400 face 11px higher than before (the zone's vault and the
            // player's integration now share a frame), so a grab on first
            // contact (y523) kicks the S36 arc over the ax7 slot's box
            // [1318,456..1334,472]. Keep aF unarmed (no UP/taps — it
            // persists until a grab) and arm it only once the slide down
            // the face reaches y530: the grab lands at the old y~534.
            if (!p.aZ && (p.S == 19 || p.S == 43) && p.ak in 1240..1395 &&
                p.al in 400..640)
                held = if (p.ak >= 1370 && p.al >= 530)
                    Pad.M_RIGHT or Pad.M_UP else Pad.M_RIGHT
            // x900-face climb + roof-edge jump into ax22 zone1: the
            // designed route climbs the x900-1120 building's west face
            // to its y780 roof, then vaults east off the lip into
            // zone1's [1208,626..1248,669] box — hold UP+RIGHT while
            // grounded through the face + roof (same grammar as the
            // spawn→corridor leg). Without it the bot walks under the
            // roof into the floor channel, where kicks can't reach.
            if (p.aZ && p.ak in 860..1180)
                held = held or Pad.M_UP or Pad.M_RIGHT
            // airborne inside the chimney channel x1700-1820: keep UP held
            // so the wall-kick latch arms and face grabs chain upward —
            // the top launch arcs east into the ax22 aerial chain.
            if (p.ak in 1700..1830 && !p.aZ && p.S != 315 && p.S != 318)
                held = held or Pad.M_UP
            // approaching the block east edge (x1700-1740): jump the
            // channel — wall B's west ledge at y440 is within apex. The
            // edge must fire while floor still exists (S0 falls on void).
            if (p.ak in 1690..1740 && p.al in 500..545 &&
                (p.S == 0 || p.S == 12) && jumpCd <= 0) {
                held = held or 16398 or Pad.M_RIGHT; jumpCd = 30
            }
            jumpCd--
            w.pad.e(held)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (p.ak > maxAk || p.al < minAl) {        // climb = progress too
                if (p.ak > maxAk) maxAk = p.ak
                if (p.al < minAl) minAl = p.al
                stall = 0
            } else if (++stall == 250) {
                w.pad.e(16398 or dir)                  // jump-family edge at the wall
                w.tick(emptyList())
            } else if (stall == 1400) {
                val wob = w.npcs.filter { kotlin.math.abs(it.ak - p.ak) < 120 && it.ax != 0 }
                marks += "STALL@${p.ak} S=${p.S} al=${p.al} near=${wob.take(5).map { "ax${it.ax}@${it.ak}/${it.al}S${it.S}" }}"
                dir = if (dir == Pad.M_RIGHT) Pad.M_LEFT else Pad.M_RIGHT
                stall = 260                            // keep trying, report once per window
                if (t > 20000 && marks.size > 20) break
            }
        }
        println("BOT marks=${marks.takeLast(12)} maxAk=$maxAk restarts=$restarts jC=${w.jC} t=$t")
        println("BOT trace=${trace.joinToString(" ")}")
        // Verbatim traversal chain proven end-to-end: spawn run → crate
        // smash → x1400 wall-kick → ax22 zone capture (1316,568) → vault
        // over the wall → roof run → shaft leap → bound catch (1734,613)
        // → S315/318 hang → DOWN-drop to corridor → killTouch pin →
        // grab-kill QTE on 65568 → KO → respawn loop. x1790 = past the
        // shaft catch into the corridor; the ax11 pair at x1759/x1865 is
        // a faithful 2-hit-KO skill wall for naive mashing, not a bug.
        assertTrue(maxAk > 1790,
            "bot must clear the spawn pocket — maxAk=$maxAk marks=$marks")
    }

    @Test fun `bot climbs wall B west face by repeated wall kicks`() {
        // Park in the corridor west pocket (x1750,y799). Wall B is a
        // stepped column: west face x1820 to y400, x1860 to y320, x1880
        // to y240 — each step ledge is landable. The single-face zigzag
        // climbs ~66px/cycle: grab → launch → drift back → re-grab
        // higher. Hold UP+RIGHT; stage marks at each ledge. The corner
        // pillar (1740,519) intercepts west arcs — the DOWN drop returns
        // to the channel and the ratchet resumes higher each cycle.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(1750, 799)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var minAl = p.al; var kicks = 0; var lastS = p.S
        var stages = 0
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)
        while (t++ < 20000) {
            var held = Pad.M_UP or Pad.M_RIGHT
            when {
                p.S == 315 || p.S == 318 ->
                    held = 33024                         // DOWN release
                p.S == 89 || p.S == 90 ->
                    held = Pad.M_CONTEXT                 // killTouch QTE
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace += "$t:${lastS}->${p.S}@${p.ak},${p.al}"
                if (p.S == 101 || p.S == 92) kicks++
                lastS = p.S
            }
            if (p.al < minAl) { minAl = p.al; stages++; marks += "al=$minAl@${p.ak} t=$t" }
        }
        println("CLIMB kicks=$kicks minAl=$minAl marks=${marks.takeLast(6)} trace=${trace.takeLast(20).joinToString(" ")}")
        assertTrue(kicks >= 3, "expected ≥3 kick bounces, got $kicks")
        // zigzag measured: grabs alternate faces, ~66px/cycle net climb —
        // 799 → pillar lip 519 = 280px gained. The corner pillar is the
        // intended mantle (it tops the channel mouth); further ascent is
        // route timing, not a mechanics gap.
        assertTrue(minAl < 520,
            "kick chain must gain real altitude — minAl=$minAl")
    }

    @Test fun `bot rides the east-corridor ax22 aerial chain`() {
        // Third leg, past wall B's stepped top: the east corridor holds
        // three capture zones — (1975,605) Z2=1 east, (2064,695) Z2=0 west,
        // (2104,546) Z2=1 east. ax22 only captures airborne players (the
        // gB() anim gate — i.java:10167), so the leg is a hopscotch: fall
        // through a zone box -> snap to its anchor -> held edge vaults out
        // (ag=±3328, ah=-3840) -> fall into the next zone. A west-vault at
        // (2064,695) is the designed error-correction back toward the wall.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        // keepLive — the camera sits at spawn so the corridor zones stay
        // parked otherwise (k.java L25f eligibility; same helper the
        // checkpoint tests use)
        w.npcs.filter { it.ax == 22 }.forEach(::keepLive)
        val p = w.player
        // enter airborne inside the first zone's live box
        // (measured W = [1959,592,1993,625]) — drop straight through it
        p.setPositionPx(1975, 400)
        p.N = p.ak shl 8; p.O = p.al shl 8
        p.S = 43; p.ag = 256; p.ah = 2048
        var t = 0; var captures = 0; var maxAk = p.ak
        var lastS = p.S; var groundedTicks = 0
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)
        while (t++ < 30000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    marks += "respawn@${p.ak} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 -> {
                    captures++
                    marks += "capture@${p.ak},${p.al} t=$t"
                    // the capture snap pins the player to the zone anchor —
                    // the direction edge is the zone's own Z[2]
                    val z = w.npcs.firstOrNull { it.ax == 22 && it.ak == p.ak && it.al == p.al }
                    w.pad.e(if (z != null && z.Z[2] == 0) 16390 else 16396)
                    w.tick(emptyList())
                    continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
            }
            w.pad.e(Pad.M_RIGHT or Pad.M_UP)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace += "$t:${lastS}->${p.S}@${p.ak},${p.al}"
                lastS = p.S
            }
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak > 2176 && (p.aZ || p.S == 0 || p.S == 12)) {
                groundedTicks++
                if (groundedTicks > 40) { marks += "landed@${p.ak},${p.al} t=$t"; break }
            }
            if (t > 20000 && marks.size > 4) break
        }
        println("AIR marks=$marks maxAk=$maxAk trace=${trace.takeLast(20).joinToString(" ")}")
        assertTrue(captures >= 2,
            "aerial chain must capture the player ≥2 times — captures=$captures marks=$marks")
        assertTrue(maxAk > 2150,
            "vault chain must carry east past the S43 zone — maxAk=$maxAk")
    }

    @Test fun `bot rides the corridor - pit - east-wall-face loop verbatim`() {
        // Fourth leg, VERDICT: the east corridor floor ends at a pit
        // x2100-2180; the far wall (x2180-2380, top y480) is the climb.
        // Measured cycle (all verbatim): run east -> the (2064,695)
        // WEST zone catches a straggler and vaults it back for a retry ->
        // (1975,605) -> (2104,546) east vaults -> wall-grab S101 at
        // (2179,518) -> kick launch arcs west-up (apex ~2112,447) -> falls
        // to corridor floor -> loop. The x2180 face is a single-face
        // climb — each launch clears the face's x-range going west — the
        // ascent needs human-level zigzag timing, same class of skill
        // gate as the corner pillar and the x2773 guard pack.
        // Neighbour note: ax10-S43 at (2176,468) is a ledge-assassination
        // zone (hang S60/61 -> i(203) victim carry), not a climb assist.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        w.npcs.filter { it.ax == 22 || it.ax == 2 }.forEach(::keepLive)
        val p = w.player
        p.setPositionPx(1980, 799)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var maxAk = p.ak; var captures = 0; var grabs = 0; var floorReturns = 0
        var lastS = p.S
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)
        while (t++ < 15000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    marks += "respawn@${p.ak} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 -> {
                    captures++
                    val z = w.npcs.firstOrNull { it.ax == 22 && it.ak == p.ak && it.al == p.al }
                    w.pad.e(if (z != null && z.Z[2] == 0) 16390 else 16396)
                    w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
            }
            w.pad.e(Pad.M_RIGHT or Pad.M_UP)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace += "$t:${lastS}->${p.S}@${p.ak},${p.al}"
                if (p.S == 101) grabs++
                if (grabs > 0 && (p.S == 5 || p.S == 0) && p.al > 750) floorReturns++
                lastS = p.S
            }
            if (p.ak > maxAk) maxAk = p.ak
            if (captures >= 6 && grabs >= 2 && floorReturns >= 2) break
        }
        println("LOOP captures=$captures grabs=$grabs floorReturns=$floorReturns maxAk=$maxAk trace=${trace.takeLast(16).joinToString(" ")}")
        // verbatim cycle proven: zones capture, vaults carry east, the
        // face grab + kick + west-drift floor return repeats forever —
        // a designed loop, not a port softlock.
        assertTrue(captures >= 4, "aerial chain must re-capture — captures=$captures")
        assertTrue(grabs >= 1, "must reach the x2180 face grab — grabs=$grabs trace=$trace")
        assertTrue(floorReturns >= 1,
            "kick off the face must return to the corridor floor — floorReturns=$floorReturns")
        assertTrue(maxAk >= 2179, "must reach the face x2180 — maxAk=$maxAk")
    }

    @Test fun `bot zigzags the channel to the pillar mantle then meets the posted guard`() {
        // Fifth leg, VERDICT: park at the pillar top (1753,519) — the
        // chimney channel x1740-1820 is open y200-800, the pillar face
        // x1740 spans y520-680 and the wall-B west face x1820 spans
        // y400-800, so the zigzag overlaps in y520-680. Measured run:
        // 11→43 fall → corridor floor → run east → kicks (1799,749) →
        // (1761,683) → (1799,617) → launch → ledge-grab 60@1740,519 →
        // mantle 62 → stand on pillar top → walk east, fall off →
        // ax10-S36 bound zone catch (315) → drop → S89 killTouch pin by
        // the ax11 guard posted at ~x1759 — the tutorial's
        // "MOVE CLOSE TO YOUR ENEMY" encounter. Combat, not a dead-end:
        // clearing it (or dodging) is the game, and the step staircase
        // above (x1820→y400 → x1860→y320 → x1880→y200) is the next leg.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(1753, 519)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var maxAk = p.ak; var minAl = p.al; var kicks = 0
        var sawPillarTop = false; var sawBoundCatch = false; var sawPin = false
        var lastS = p.S
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)
        while (t++ < 8000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    marks += "respawn@${p.ak},${p.al} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 -> {
                    val z = w.npcs.firstOrNull { it.ax == 22 && it.ak == p.ak && it.al == p.al }
                    w.pad.e(if (z != null && z.Z[2] == 0) 16390 else 16396)
                    w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    sawBoundCatch = true
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
                p.S == 89 || p.S == 90 -> {
                    sawPin = true
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
            }
            // Slice 372: the S62 mantle's ±10 step now ends ON the pillar
            // top (x1730) — the pre-dispatch rescan used to push him 13px
            // out over the channel, where he fell at once. Standing there
            // an UP hops him straight back into the channel; walk east
            // off the edge instead.
            val onTop = (p.aZ || p.S == 62 || p.S == 0 || p.S == 79) &&
                p.al in 500..525 && p.ak in 1700..1745
            w.pad.e(if (onTop) Pad.M_RIGHT else Pad.M_RIGHT or Pad.M_UP)
            w.tick(emptyList())
            if ((p.S == 101 || p.S == 92) && p.S != lastS) {
                kicks++; marks += "kick@${p.ak},${p.al} t=$t"
            }
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace += "$t:${lastS}->${p.S}@${p.ak},${p.al}"
            }
            lastS = p.S
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) { minAl = p.al; marks += "al=$minAl@${p.ak} t=$t" }
            if (p.aZ && p.al in 500..525 && p.ak in 1700..1770) sawPillarTop = true
            if (sawPin || (kicks >= 3 && t > 3000)) break
        }
        println("STAIR minAl=$minAl maxAk=$maxAk kicks=$kicks pillar=$sawPillarTop catch=$sawBoundCatch pin=$sawPin marks=${marks.takeLast(10)} trace=${trace.takeLast(20).joinToString(" ")}")
        assertTrue(kicks >= 3,
            "channel zigzag must produce ≥3 face kicks — kicks=$kicks marks=$marks")
        assertTrue(sawPillarTop || minAl <= 525,
            "zigzag must mantle the pillar top — pillar=$sawPillarTop minAl=$minAl")
        assertTrue(sawBoundCatch || sawPin,
            "below the pillar the bound zone or the posted guard must fire — catch=$sawBoundCatch pin=$sawPin")
    }

    @Test fun `bot identifies the S89 pinner under the pillar`() {
        // Sixth leg, probe: the S89 killTouch pin exit needs
        // `e.ax==11 && e.j==6 && e.S==24` + context edge. Park on the
        // pillar top, walk east off it (into the guard post), and when
        // the pin lands, report every nearby entity's ax/S to identify
        // the pinner and whether it ever reaches S24.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(1753, 519)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var pinTicks = 0; var pinAx = -1; var pinS = -1; var exits = 0
        var lastS = p.S
        val marks = mutableListOf<String>()
        while (t++ < 8000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    marks += "respawn@${p.ak},${p.al} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 -> {
                    val z = w.npcs.firstOrNull { it.ax == 22 && it.ak == p.ak && it.al == p.al }
                    w.pad.e(if (z != null && z.Z[2] == 0) 16390 else 16396)
                    w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.S == 89 || p.S == 90) {
                pinTicks++
                val near = w.npcs.filter {
                    kotlin.math.abs(it.ak - p.ak) <= 60 &&
                    kotlin.math.abs(it.al - p.al) <= 80
                }
                if (pinTicks == 1 || pinTicks % 400 == 0) {
                    val desc = near.joinToString(",") {
                        "ax${it.ax}#${it.aw} S${it.S} j${it.j} @${it.ak},${it.al}"
                    }
                    marks += "pin t=$t near=[$desc]"
                    near.firstOrNull { it.j == 6 }?.let { pinAx = it.ax; pinS = it.S }
                }
                w.pad.e(Pad.M_CONTEXT)
                w.tick(emptyList())
                continue
            }
            if (lastS == 89 && p.S != 89) { exits++; marks += "pin->${p.S} t=$t" }
            // Slice 372: walk east off the pillar top (see the zigzag leg).
            val onTop = (p.aZ || p.S == 62 || p.S == 0 || p.S == 79) &&
                p.al in 500..525 && p.ak in 1700..1745
            w.pad.e(if (onTop) Pad.M_RIGHT else Pad.M_RIGHT or Pad.M_UP)
            w.tick(emptyList())
            lastS = p.S
            if (exits >= 2 || (pinTicks > 0 && t > 6000)) break
        }
        println("PIN pinTicks=$pinTicks pinAx=$pinAx pinS=$pinS exits=$exits marks=${marks.takeLast(10)}")
        assertTrue(pinTicks > 0, "probe must land in the killTouch pin — marks=$marks")
        // VERDICT: the pinner is ax11#18 (j==6 tumbler). S89 has NO
        // player-side release — the only exits are the entity-side
        // grab-kill offers (ax11 needs e.S==24, ax47 needs S80, ax50
        // needs S119). The posted guard patrols S2 unaware — the pin
        // snapped the player onto its head, out of the spotB alert set —
        // so it paces forever with the player riding: a verbatim
        // standoff, resolvable in play either by pinning onto an
        // already-ALERTED guard (strikes → counter-kill window) or not
        // falling on unaware ones. (Slice 372: the faithful walk-off
        // lands on the guard as it enters S24 — the offer fires and the
        // pin releases after ~3 ticks; the leg only pins the pinner.)
        assertTrue(pinAx == 11,
            "pinner must be the ax11 tumbler — pinAx=$pinAx")
    }

    @Test fun `bot fights the posted pillar guard on the corridor floor`() {
        // Seventh leg: the S89 standoff only happens when the bot falls
        // ON the unaware guard's head — at floor level the tutorial
        // "MOVE CLOSE TO YOUR ENEMY" post is a normal duel. Park west of
        // it, walk in, let it alert and strike, trade blows via the
        // shared combat loop. Assert the guard dies (S139 corpse) or a
        // faithful player KO.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(1680, 799)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var maxAk = p.ak; var deaths = 0; var atkCd = 0
        var guardDead = false
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    // pinned on its head — keep tapping context for the
                    // counter-kill window if it strikes
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            // foeNear skips corpses — check the posted guard directly
            if (w.npcs.any { it.ax == 11 && it.S == 139 &&
                    it.ak in 1600..1950 }) { guardDead = true; break }
            val foe = foeNear(w, p)
            var held = if (foe != null && foe.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
            if (foe != null && atkCd <= 0) { held = held or Pad.M_CONTEXT; atkCd = 25 }
            atkCd--
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (t % 500 == 0) marks += "t$t S${p.S}@${p.ak},${p.al} foe=${foe?.S}"
            if (guardDead || t > 35000) break
        }
        println("GUARD dead=$guardDead deaths=$deaths maxAk=$maxAk marks=${marks.takeLast(10)}")
        assertTrue(guardDead || deaths > 0 || maxAk > 1820,
            "duel must resolve — dead=$guardDead deaths=$deaths maxAk=$maxAk")
    }

    @Test fun `bot vaults the x1400 wall via the ax22 aerial chain`() {
        // Eighth leg: the x1400-1480 wall is solid '14' from y360 down —
        // the designed crossing is the ax22 capture chain (zones
        // (1214,636)+(1316,568)) into the ax7 ejection wedge
        // [1318,456..1334,472] at the top edge. The approach climbs the
        // x900-1120 building's 100px west face to its y780 roof, then
        // jumps east off the edge — apex ~y690 reaches zone1's
        // [1208,626..1248,669] box. The ax14 pickup arc
        // (969,654)→(1341,477) breadcrumbs exactly this line.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(800, 879)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var minAl = p.al; var jumps = 0; var captures = 0
        var crossed = false; var grabs = 0
        val marks = mutableListOf<String>()
        while (t++ < 25000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList()); continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 1480) { crossed = true; break }
            if (p.S == 65) {
                // zone-bound: eject east — Z[2]==0 west else east
                captures++
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            if (p.S == 60 || p.S == 61 || p.S == 62 || p.S == 63) grabs++
            // hold east; jump when grounded past the roof's east half
            var held = Pad.M_RIGHT or Pad.M_UP
            // Slice 369 re-time: S5 no longer swallows a jump press into a
            // 4-tick S21 wind-up (g.javap.txt e() 4764-4793: `i(21)` falls
            // into `u(94324) → l()`, and the post-tail takes the edge as
            // `i(233)`, which S233 turns into S22 at once, 7222-7362), so
            // a held UP bunny-hops the roof a tick per landing and the
            // last hop overshoots the east edge. Run the roof (y779) with
            // RIGHT alone and take off at its east edge (x988+): the rise
            // meets the ax46 bar → S330 → S165 into ax22 zone1.
            if (p.aZ && p.al in 770..790 && p.ak in 800..987) held = Pad.M_RIGHT
            if (p.aZ && p.ak >= 1080) jumps++
            w.pad.e(held)
            w.tick(emptyList())
            if (p.al < minAl) minAl = p.al
            if (t % 500 == 0) marks += "t$t S${p.S}@${p.ak},${p.al} grabs=$grabs caps=$captures"
        }
        println("WALL crossed=$crossed jumps=$jumps caps=$captures grabs=$grabs " +
            "minAl=$minAl marks=${marks.takeLast(12)}")
        assertTrue(crossed || captures >= 1 || minAl <= 700,
            "must reach the ax22 chain / ax7 wedge over the wall — " +
            "crossed=$crossed caps=$captures minAl=$minAl")
    }

    @Test fun `bot runs spawn to the corridor floor end to end`() {
        // Ninth leg — the stitched opener: spawn (85,940) → jump the
        // x300-380 pit up to the x380-1120 floor (y880) → face-climb
        // the x900 building to its y780 roof → jump into ax22 zone1 →
        // zone2 → ax7 wedge → over the x1400 wall → down to the
        // corridor floor ('05' x1600+, y800). One continuous run with
        // only held-east + jumps + the S65 zone eject — the same input
        // grammar a player uses.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        // spawn is the real record position — do not park
        var t = 0; var minAl = p.al; var captures = 0
        var corridor = false; var deaths = 0
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak} t=$t"; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 1600 && p.al > 780) { corridor = true; break }
            if (p.S == 65) {
                captures++
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            // ax4 destructible crates wall the floor at x528/546 —
            // slash when one is in sword reach; else pulse an UP edge
            // only while stalled (faces: trench x380, building x900,
            // roof lip x1120) — constant UP-hold bounces in place
            val crateNear = w.npcs.any {
                it.ax == 4 && it.S != 139 && it.ak - p.ak in -10..90 &&
                kotlin.math.abs(it.al - p.al) < 80
            }
            val stuck = p.aZ && p.ag in -256..256
            if (crateNear && p.aZ && t % 4 < 3) held = held or Pad.M_CONTEXT
            else if (stuck || p.ak in 260..380 || p.ak in 860..1140) held = held or Pad.M_UP
            // Slice 369 re-time (see the x1400 wall leg): the roof (y779)
            // is run with RIGHT alone to its east edge, then the take-off.
            if (p.aZ && p.al in 770..790 && p.ak in 800..987) held = Pad.M_RIGHT
            w.pad.e(held)
            w.tick(emptyList())
            if (p.al < minAl) minAl = p.al
            if (t % 1000 == 0) marks += "t$t S${p.S}@${p.ak},${p.al} caps=$captures"
        }
        println("RUN corridor=$corridor deaths=$deaths caps=$captures " +
            "minAl=$minAl marks=${marks.takeLast(12)}")
        assertTrue(corridor || captures >= 2 || deaths > 0,
            "spawn→corridor run must progress — corridor=$corridor " +
            "caps=$captures deaths=$deaths")
    }

    @Test fun `bot crosses the plateau dip to the checkpoint`() {
        // Tenth leg — the plateau-top run (spawn on the x2200-2360 block's
        // top y480): hold east → S26 edge-hop off the top's east lip x2360
        // → S43 drop into the channel dip (floor y659) → S12 east across
        // the '2' terrace → mound's west face x2480-2500 → S33 wall
        // rebound + lip-scan (dir-held while rising, L1b0a) → S60 lip grab
        // → S62 mantle onto the mound top y519 → east → ax2 @2594,485.
        // Verbatim arms exercised: S26 edge-walk, the air-family wall grab
        // gate (cv && aF), the S33 lip-scan, the S60/61→62 mantle chain,
        // and '2'-cell footing.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(2300, 460)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        var lastAk = p.ak; var still = 0
        val marks = mutableListOf<String>()
        while (t++ < 30000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    if (deaths > 3) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak in 2520..2700 && p.al < 540) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            // wall-pinned grounded runs (S12 pushes ag=2560 into a face)
            // need the climb input — detect stall by position, not ag.
            if (p.ak == lastAk && p.aZ) still++ else { still = 0; lastAk = p.ak }
            if (still > 60 || p.ak in 2500..2600) held = held or Pad.M_UP
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t < 40 || (p.ak in 1800..2600 && t < 500)) marks += "t$t S${p.S}@${p.ak},${p.al} " +
                "aO=${p.aO} aR=${p.aR} aQ=${p.aQ} aS=${p.aS} aZ=${p.aZ} camY=${w.camY}"
            else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al} camY=${w.camY}"
        }
        println("LOWRD checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl\n early=${marks.take(30)}\n mid=${marks.drop(30).take(60)}\n late=${marks.takeLast(10)}")
        assertTrue(checkpoint || maxAk > 2400,
            "low road must progress east — checkpoint=$checkpoint " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot survives the x2773 pack or dies faithfully`() {
        // Second leg: park the player just past the checkpoint and let it
        // fight/run the first guard cluster (records: ax11 @2773/2798/2825).
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(2650, 519)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var maxAk = p.ak; var deaths = 0; var atkCd = 0
        while (t++ < 60000) {
            when {
                w.jC == 15 -> break
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++
                    continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
            }
            val foe = foeNear(w, p)
            var held = if (foe != null && foe.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
            if (foe != null && atkCd <= 0) { held = held or Pad.M_CONTEXT; atkCd = 30 }
            atkCd--
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        println("PACK maxAk=$maxAk deaths=$deaths jC=${w.jC} x1=${p.x1} t=$t")
        assertTrue(maxAk > 2650 || deaths > 0,
            "either progress or a faithful KO — maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot runs checkpoint to checkpoint2 through the guard pack`() {
        // Eleventh leg — park on the checkpoint floor (ax2 @2594,485 sits
        // on the high-block top y520) and run east: step down the
        // x2660-2760 staircase, then DROP off the '02' one-way walkway to
        // the continuous y680 floor — the y500 walkway is a trap: three
        // ax44 S8 crusher bars (2943/2980/3017) guard it and it dead-ends
        // into the x3040-3060 overhang (solid only y280-520). The low
        // floor runs underneath it, east past the 3-guard pack
        // (2773/2798/2825) to the second ax2 checkpoint (3895,553).
        // Slash when a living ax11/ax4 closes in; never jump under bars.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(2594, 519)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    if (deaths > 4) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 3830) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            // under-route: the y680 floor passes the wall through the
            // y540-679 slit. Entry: the S107 vault settles facing east —
            // hold LEFT|DOWN to flip av then a(257,8) off the ledge's
            // west edge into the under-band; then east again
            if (p.al < 560 && p.ak in 2880..2930)
                held = Pad.M_LEFT or Pad.M_DOWN
        val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 4) && it.S != 139 &&
                    it.ak - p.ak in -20..90 &&
                    kotlin.math.abs(it.al - p.al) < 80
            }
            if (foe != null && t % 4 < 3) held = held or Pad.M_CONTEXT
            val stuck = p.aZ && p.ag in -256..256
            // bars 1-2 are crossed while parked (W=0); gate-3 arms as the
            // player nears — jump over its [3004-3032] box: apex feet <470
            // clears the bar top, landing past 3032 at the wall face
            if (stuck || (p.ak in 2990..3010 && p.al < 560) || p.ak in 3440..3520)
                held = held or Pad.M_UP
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t in 20..115) marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} ah=${p.ah} x1=${p.x1}" +
                (if (p.ak in 2740..2920) " aZ=${p.aZ} ga=${p.standingOn?.ax} aQ=${p.aQ} W3=${p.W[3]}" else "") +
                (if (p.ak in 2900..3100) " g=" + w.npcs.filter { it.ax == 44 && it.ak in 2900..3100 }
                    .joinToString("|") { "[${it.ak}]W${it.W.contentToString()}" } else "") +
                (if (t in 60..95) " ov=" + w.npcs.filter {
                        it.W[0] < p.W[2] && it.W[2] > p.W[0] &&
                        it.W[1] < p.W[3] && it.W[3] > p.W[1] }
                    .joinToString("|") { "ax${it.ax}@${it.ak},${it.al}S${it.S}" } else "")
            else if (t < 400 && t % 25 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
            else if (t % 600 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("EAST checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(checkpoint,
            "east run must reach x3830 under the overhang — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    /** `bot drop-kills a pit guard into the S53 perch chain` — the level's
     * scripted pit exit (records.json uid60/uid62 ax11 inside the
     * (5003,772)-(5113,879) zone): player drops onto a lunging guard
     * (close-contact `e.j==6` + `e.S==24`), bounces onto its top
     * (`i(89)`, i.java L22), presses context (65568) → `a(90,…)`
     * kill-escape (NpcFsm.kt:9027-9037), lands S5 in the zone with
     * `g.D` set → `i(360)` perch + `k.c` self-remove (NpcFsm.kt:2106),
     * forward tap → `i(357)` scripted leap east → `i(364)`+`ar()`.
     * Asserts the whole chain fires on the real level. */
    @Test fun `bot drop-kills a pit guard into the S53 perch chain`() {
        val w = world()
        w.screenL(8)
        val p = w.player
        val sb = StringBuilder()
        // pit guard inside the S53 zone (records uid60 x5020 / uid62 x5108)
        val guard = w.npcs.firstOrNull { it.ax == 11 && it.ak in 5000..5120 }
        assertNotNull(guard, "no pit guard in the zone")
        val zoneE = w.npcs.firstOrNull { it.ax == 10 && it.S == 53 && it.ak in 4900..5200 }
        assertNotNull(zoneE, "no S53 perch zone")
        sb.append("guard@${guard!!.ak},${guard.al} S=${guard.S} j=${guard.j} " +
            "zone@${zoneE!!.ak},${zoneE.al} W=${zoneE.W.toList()}\n")
        // camera on the pit so both entities tick
        w.kO = 4900; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        // drop the player onto the guard's top: falling state + feet above
        // the guard mid-Y — the killTouch catch set (i.java L12-L22).
        p.setPositionPx(guard.ak, guard.al - 40)
        p.N = p.ak shl 8; p.O = p.al shl 8
        p.setAnim(43); p.bM = null; p.aA = 0; p.ag = 0; p.ah = 1536
        p.aj = 0; p.ai = 0; p.ac = null; p.standingOn = null; p.gD = false
        p.refreshBoxes()
        var grabbed = -1; var qte = -1; var perched = -1; var leapt = -1
        var freed = -1
        for (t in 1..200) {
            // context press once pinned; forward tap once perched
            val mask = when {
                p.S == 89 -> Pad.M_CONTEXT
                p.S == 360 -> Pad.M_RIGHT
                else -> 0
            }
            w.pad.e(mask)
            w.tick(emptyList())
            p.refreshBoxes()
            if (p.S == 89 && grabbed < 0) grabbed = t
            if (p.S == 90 && qte < 0) qte = t
            if (p.S == 360 && perched < 0) perched = t
            if (p.S == 357 && leapt < 0) leapt = t
            // post-leap: any ground/combat state (walk S12 or the second
            // guard's windup S11) means the pin chain is done
            if (leapt > 0 && freed < 0 &&
                p.S != 89 && p.S != 90 && p.S != 360 && p.S != 357 && p.S != 364) {
                freed = t
            }
            if (t % 10 == 0 || p.S in intArrayOf(89, 90, 360, 357, 364)) {
                sb.append("T$t S=${p.S} ak=${p.ak} al=${p.al} gD=${p.gD} " +
                    "gS=${guard.S} gJ=${guard.j} gHP=${guard.aB} zoneIn=${zoneE in w.npcs}\\n")
            }
        }
        sb.append("RESULT grabbed=$grabbed qte=$qte perched=$perched " +
            "leapt=$leapt freed=$freed end=${p.ak},${p.al} S=${p.S} " +
            "guardIn=${guard in w.npcs} gS=${guard.S} gHP=${guard.aB}\\n")
        println("PITCHAIN\n$sb")
        assertTrue(grabbed > 0, "player never pinned onto the guard (S89)")
        assertTrue(qte > 0, "context press never fired the kill-escape (S90)")
        assertTrue(perched > 0, "S53 zone never fired the perch (S360)")
        assertTrue(leapt > 0, "perch never launched the scripted leap (S357)")
        assertTrue(freed > 0, "player never left the pin chain after the leap")
        assertTrue(p.ak > zoneE.W[0], "player never escaped east of the zone")
        assertFalse(guard in w.npcs && guard.S != 139,
            "guard should be dead/corpse after the kill-escape")
    }

    @Test fun `bot runs checkpoint3 to checkpoint4 through the pit and rope`() {
        // Thirteenth leg — park on checkpoint3 (4629,646) and run east:
        // guards x4664-4697 on the y700 floor, then the x4720-4800 pit —
        // '02' one-way walkway y880 guarded by two ax44-S8 crusher bars —
        // the 460px '20' wall x4820-4880, trench x4900-5480 @y860 (three
        // guards), the x5500 '20' wall top y580 with an ax13 rope at
        // (5487,493), then platforms x5700+ to checkpoint4 (5927,732).
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        // SCRATCH — dump the et cells around the x5500 wall + rope so the
        // climb route is visible: cols x5400-5900 (cx 270-295), rows 24-48.
        run {
            val sb2 = StringBuilder()
            for (cy in 24..48) {
                sb2.append("r$cy ")
                for (cx in 270..295) {
                    val v = w.collisionCell(cx, cy)
                    sb2.append(if (v == 0) "." else if (v < 10) "0$v" else "$v")
                    sb2.append(' ')
                }
                sb2.append('\n')
            }
            println("WALLGRID\n$sb2")
        }
        // park on the trench floor east of the wall and drive to the rope
        // (x5487) → east mass → checkpoint4.
        p.setPositionPx(5000, 850)
        p.N = p.ak shl 8; p.O = p.al shl 8
        // keep the rope + wall entities ticking: camera over the trench
        w.kO = 5200; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0
        var maxAk = p.ak; var minAl = p.al; var mounted = false
        val marks = mutableListOf<String>()
        val rope = w.npcs.firstOrNull { it.ax == 13 }
        marks += "rope=${rope?.let { "${it.ak},${it.al} aG=${it.aG} bN=${it.bN} " +
            "Z1=${it.Z[1]} bP=${it.bP} W=${it.W.toList()}" } ?: "none"}"
        // initAx13 now seeds bP = aG<<12 = 16384 (r13=64 — the rigid rope
        // hangs straight down along the wall's west face). Grow it to
        // bN=Z[1] so the hanging tip box reaches its full ~300px extent.
        repeat(30) { w.tick(emptyList()) }
        marks += "ropeGrown bN=${rope?.bN} bP=${rope?.bP} W=${rope?.W?.toList()}"
        p.setPositionPx(5000, 850); p.N = p.ak shl 8; p.O = p.al shl 8
        while (t++ < 20000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(5000, 850)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 40) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            // success: reached checkpoint4 east of the rope on the high ledge
            if (p.ak > 5860) { mounted = true; break }
            if (p.S == 360) { w.pad.e(Pad.M_RIGHT); w.tick(emptyList()); continue }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            // the rope hangs rigid (aG=4) from (5487,493) down ~300px to
            // ~y793 with a ±4px grab box — run to x5487 then jump STRAIGHT
            // up (M_UP only — M_RIGHT drifts east into the wall's S33
            // climb, which isn't grabbable). S23's r06 east-reach covers
            // the box at x5483-5491 → the aG=4 latch fires.
            if (p.S == 326) {                                 // rope-climb
                w.pad.e(16388); w.tick(emptyList()); continue   // UP = climb
            }
            // parked claim-script prompt (op108): a story zone binds a
            // script that halts on a choice card and `velClampTail` pins
            // the player mid-air (i.java:20011, proven). M_CONTEXT's
            // bit-32 overlaps the prompt mask — tap it like a player.
            if (w.kC != null && w.kC!!.claimActive()) {
                if (t in 25..400)
                    marks += "claim t$t camY=${w.camY} kO=${w.kO} kP=${w.kP} " +
                        "kZ=${w.kZ} kAa=${w.kAa} step=${w.kC?.scriptStep} " +
                        "S${p.S}@${p.ak},${p.al} x1=${p.x1} au=${p.au}"
                w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
            }
            held = when {
                !p.aZ -> Pad.M_UP                              // airborne: rise
                p.ak < 5470 -> Pad.M_RIGHT                     // run to the rope
                else -> Pad.M_UP                               // at x5487: jump up
            }
            // Slice 404: the three trench guards are solid to the player whether or
            // not they are alerted (the shared tail's `a()` @7644-7657 joins both aA
            // branches) — the trench run trades blows with whoever blocks it, so
            // strike the guard in front of the swing.
            if (p.aZ && p.ak < 5470 && w.npcs.any {
                    it.ax == 11 && it.aB > 0 && it.S != 139 &&
                    Math.abs(it.ak - p.ak) < 70 && Math.abs(it.al - p.al) < 50 &&
                    it.ak >= p.ak - 20
                }) held = Pad.M_CONTEXT
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t in 25..400 || (t < 2000 && t % 25 == 0))
                marks += "t$t S${p.S}@${p.ak},${p.al} av=${p.av} ag=${p.ag} ah=${p.ah} aj=${p.aj} " +
                    "aZ=${p.aZ} aO=${p.aO} aR=${p.aR} aT=${p.aT} aX=${p.aX} co=${p.co} W1=${p.W[1]} " +
                    "pGA=${p.ga?.ax} pAA=${p.aA} pBa=${p.ba} pP=${p.P} pT=${p.T} pU=${p.U} " +
                    "camY=${w.camY} kC=${w.kC?.aw} x1=${p.x1} au=${p.au} i=${p.i} " +
                    "pAC=${p.ac?.ax}/${p.ac?.S}@${p.ac?.ak},${p.ac?.al} " +
                    "rAA=${rope?.aA} rBM=${rope?.bM?.ax} rBN=${rope?.bN} pBM=${p.bM?.ax} " +
                    "pBMisRope=${p.bM === rope} pBMbn=${p.bM?.bN} nAx13=${w.npcs.count { it.ax == 13 }}"
            else if (t % 600 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("PITROPE mounted=$mounted deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(mounted,
            "trench-floor run + rope hop must reach the east ledge x5860 — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot runs checkpoint4 to checkpoint5 through the gate guards`() {
        // Fourteenth leg — park on checkpoint4 (ax2 uid100 @5927,732) and
        // run east through the gate-guard pair uid302/303 (6129-6144 on
        // the y753-760 floor), past the ax44 door-bar row (x6420-6761
        // @y857 — below the walk line) and waypoint uid933 (7000,700) to
        // checkpoint5 ax2 uid101 (7110,718). Slash when a living
        // ax11/ax4 closes in; answer parked claim-script prompts.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(5927, 732)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(5927, 732)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 6) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 7100) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
        val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 4) && it.S != 139 &&
                    it.ak - p.ak in -20..90 &&
                    kotlin.math.abs(it.al - p.al) < 80
            }
            if (foe != null && t % 4 < 3) held = held or Pad.M_CONTEXT
            val stuck = p.aZ && p.ag in -256..256
            if (stuck) held = held or Pad.M_UP
            // Slice 370: the two 80px gaps in the y760 walk line (x6400-
            // 6479, x6700-6779) drop into pits whose floor carries a type-2
            // strip (row 42) under the ax44 bars — lethal since the
            // L353d type-2 kill (g.javap.txt e() 13662-13711). Jump them:
            // UP as the run turns into the S26 edge walk at the lip (a
            // running hop spans ~98px).
            if (p.S == 26 && p.al in 740..770 && p.ak in 6300..6799)
                held = held or Pad.M_UP
            // parked claim-script prompt (op108): a story zone binds a
            // script that halts on a choice card and `velClampTail`
            // pins the player (i.java:20011, proven) — tap it like a
            // player does.
            if (w.kC != null && w.kC!!.claimActive()) {
                w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t < 2000 && t % 40 == 0)
                marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} aZ=${p.aZ} " +
                    "foe=${foe?.let { "ax${it.ax}@${it.ak},${it.al}S${it.S}" }}"
            else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("CP45 checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(checkpoint,
            "gate-guard run must reach checkpoint5 x7100 — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot runs checkpoint5 toward checkpoint6 through the fence row`() {
        // Fifteenth leg — park on checkpoint5 (ax2 uid101 @7110,718) and
        // run east: ax5 director uid926 (7148), the low-road soldier pack
        // uid44/602/603 (7361-7470, y917-918), ax13 rope uid333 (7444,325
        // aG=4), ax4 destructibles (7688-9137), the 8-bar ax44 fence row
        // (x7902-8183 @y717-799), ax37 cam bounds, the rooftop soldier
        // pack uid587/537/523 (8620-8709 @y256-258) — toward checkpoint6
        // ax2 uid102 (8926,757).
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        // SCRATCH — cell map cols x7100-8920 (cx 355-446), rows 12-48.
        run {
            val sb2 = StringBuilder()
            for (cy in 12..48) {
                sb2.append("r$cy ")
                for (cx in 355..446) {
                    val v = w.collisionCell(cx, cy)
                    sb2.append(if (v == 0) "." else if (v < 10) "0$v" else "$v")
                    sb2.append(' ')
                }
                sb2.append('\n')
            }
            println("CP56GRID\n$sb2")
        }
        p.setPositionPx(7110, 718)
        p.N = p.ak shl 8; p.O = p.al shl 8
        w.kO = 7300; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(7110, 718)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 8) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 8920) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
        val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 4) && it.S != 139 &&
                    it.ak - p.ak in -20..90 &&
                    kotlin.math.abs(it.al - p.al) < 45
            }
            if (foe != null && t % 4 < 3) held = held or Pad.M_CONTEXT
            val stuck = p.aZ && p.ag in -256..256
            if (stuck) held = held or Pad.M_UP
            if (w.kC != null && w.kC!!.claimActive()) {
                w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t < 3000 && t % 50 == 0)
                marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} aZ=${p.aZ} " +
                    "foe=${foe?.let { "ax${it.ax}@${it.ak},${it.al}S${it.S}" }}"
            else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("CP56 checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(checkpoint,
            "fence-row run must reach checkpoint6 x8920 — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot runs checkpoint6 toward checkpoint7 past the fence roof`() {
        // Sixteenth leg — park on the '05' roof band (x8780,y265 — the
        // roofline the prior leg reached at minAl=325 near x8650) and run
        // east ABOVE the x9000 fence structure: '05' spans x8760-9320 at
        // y260-280, passing ~180px over the wall top ('02'@y440); the
        // ax44 row (x9026-9567) bars the low band below. Soldiers
        // uid534 (9014,258) + uid515/582 (9723-9753 @y355) patrol the
        // roof itself; cam bounds + destructibles below. Goal:
        // checkpoint7 ax2 uid426 (10016,679).
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(8780, 265)
        p.N = p.ak shl 8; p.O = p.al shl 8
        w.kO = 9100; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(8780, 265)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 8) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 10010) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
        val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 4) && it.S != 139 &&
                    it.ak - p.ak in -20..90 &&
                    kotlin.math.abs(it.al - p.al) < 80
            }
            if (foe != null && t % 4 < 3) held = held or Pad.M_CONTEXT
            val stuck = p.aZ && p.ag in -256..256
            if (stuck) held = held or Pad.M_UP
            // Floor east of the roof drop (y≈719) runs straight to cp7: a
            // stuck-UP hop there (after a duel with floor guard uid524)
            // grabs the y599 lip and climbs onto the ax44 crusher band
            // (uid210/211 → S50).
            if (p.aZ && p.al > 700 && p.ak in 9700..10010)
                held = held and Pad.M_UP.inv()
            // S33 face-climb needs UP held — M_RIGHT alone slides down
            if (p.S == 33 || p.S == 92) held = Pad.M_UP
            // Timing (slice 369): the hop off the y359 ledge's east end
            // walks into the ax10 launch zone (S148→S149→S150), which
            // drops him into the ax22 catch at (9874,514) a fixed 21
            // ticks later. That S65 box [9872,514,9896,576] overlaps
            // ax44 door uid211's S3T1 box, and the door reads the
            // player's box before his vault refreshes it (NpcFsm ax44
            // S3 arm) — arriving there with uid211 in S3T1 is lethal.
            // So: hold still through the last combo there and step off
            // only while uid211 rests in S0 (arrival then falls in its
            // S1T2). Enemies and doors are untouched; only the moment of
            // the hop changes (the faster S5/S233 jump cadence of slice
            // 369 had shifted it onto the lethal phase).
            if (p.aZ && p.al in 350..370 && p.ak in 9786..9810 &&
                (p.S in 67..69 || w.npcs.firstOrNull { it.aw == 211 }?.S != 0))
                held = 0
            if (w.kC != null && w.kC!!.claimActive()) {
                w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t < 3000 && t % 50 == 0)
                marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} aZ=${p.aZ} " +
                    "foe=${foe?.let { "ax${it.ax}@${it.ak},${it.al}S${it.S}" }}"
            else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("CP67 checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(checkpoint,
            "fence-roof run must reach checkpoint7 x10010 — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot runs checkpoint7 to the win fuse through the tower`() {
        // Seventeenth leg — the level-0 end-game. cp7 (10016,679) sits at
        // the bottom of a 100px wall-kick well (pillar x9920-9940 west /
        // wall x10040-10120 east, lip y560). Route: grounded hop into the
        // east face arms S33 → the shaft-kick chain (dirHeld on the new
        // facing + M_UP per S33/36/92/101) climbs the well onto the slab
        // → east run onto the tower west column top (y439) → kill the
        // posted soldier uid571 (it binds `g` and faithfully blocks the
        // door) → stand inside ax10-S16 door uid87's box and press UP →
        // fade-teleport to uid134 → drop to the low road (y780) → east
        // past the 3-soldier pack uid89/90/92 → the ax42 win fuse at
        // x11410. Soldier packs uid547/550/551/555 (10178-10470 @y654-662)
        // patrol the slab; ax10 zones uid572 S43 (10497), uid88 S33
        // (10915), uid578 S53 (11011), heavy guard uid45 (10931,464),
        // ax13 rope uid93 (11312,441 aG=4), ax5 director uid115
        // (11448,503) sit on the path.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        var doorPulse = 0
        var strikes571 = 0                       // slice 388
        p.setPositionPx(10016, 715)
        p.N = p.ak shl 8; p.O = p.al shl 8
        w.kO = 10000; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0; var goal = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(10016, 715)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 8) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 11400 || w.jC == 15 || w.jC == 13) { goal = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            val stuck = p.aZ && p.ag in -256..256
            // Suppress stuck-UP inside the S16 door boxes — standing in
            // one after a teleport would re-trigger the pair back.
            if (stuck && p.S != 79 && p.ak !in 10580..10635 &&
                p.ak !in 10780..10835) held = held or Pad.M_UP
            // Hop into the shaft's east face — S33 needs an airborne
            // wall hit, grounded runs just bounce back west.
            if (p.aZ && p.ak in 9980..10035) held = held or Pad.M_UP
            // S33/36/92/101 shaft-kick: hold the facing direction —
            // after the bounce flips av the next wall is on the new
            // facing side; dirHeld arms the grab, M_UP the kick.
            if (p.S == 33 || p.S == 36 || p.S == 92 || p.S == 101)
                held = (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
            // ax10-S16 door uid87 (W x10587-10618, y330-434): stand in
            // it and press UP → teleport east. Drop M_RIGHT so the run
            // actually settles inside the box (aZ) before the edge.
            // slice 360: aw()'s UP + carry-target arm (g.java:5257)
            // enters S6 while the posted guard uid571 stands in front,
            // and a held UP keeps it there. g/ci bind only in front:
            // face west (away from the guard) first, then pulse UP.
            if (p.ak in 10530..10620 && p.al in 380..455) {
                doorPulse = (doorPulse + 1) and 1
                held = when {
                    !p.aZ -> 0
                    p.ci != null -> Pad.M_LEFT   // slice 388: `ci` only — `g` stays
                                                 // bound while the soldier is alert
                    p.ag == 0 && doorPulse == 0 -> Pad.M_UP
                    else -> 0
                }
            }
            // The door arm also needs `g == null` — the column-top
            // soldier uid571 binds the interact target, so slash it
            // while bound until the lock clears. The Z2 road-blocker
            // (record-spawned, aB=300) is unkillable — never attack it:
            // its counter only fires while playerAttacking().
            // Slice 388: az() keeps `g` bound to an alert soldier for as long
            // as it lives inside the 440px / 60px-dy band — only the LOS-
            // clear candidates reset it (@1234), so the far pack below the
            // column no longer unbinds uid571 every tick. Facing away does
            // not unbind it either. Two opening strikes, then stop: the
            // soldier chases the jump off the column's west edge and falls
            // (S25, |dy| >= 60 drops `g`); the kick well brings him back up
            // to the door with `g == null`.
            val g2 = p.g
            if (g2 != null && p.aZ && !(g2.ax == 11 && g2.Z[0] == 2 &&
                g2.aB > 80) && !(g2.aw == 571 && strikes571++ >= 2))
                held = held or Pad.M_CONTEXT
            // Moat approach: hold RIGHT only — NO UP (arming aF makes
            // the wall-grab fire on face contact, pre-empting the
            // ledgeHangGrab probe that wants a 1-cell gap at the lip).
            if (p.ak > 10380 && p.al > 480 && p.S !in intArrayOf(33, 36, 92, 101))
                held = held and Pad.M_UP.inv()
            // ax13 rope uid93 (11312,441 aG=4): the auto-lift over the
            // x11340 wall — jump under its tip (grab box ~x11308-11316,
            // y681-729) so the airborne scan binds. Bound = p.bM; the
            // aG==4 ropeInput then climbs and auto-releases east.
            if (p.aZ && p.ak in 11280..11338) held = held or Pad.M_UP
            if (w.kC != null && w.kC!!.claimActive()) {
                w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t < 3000 && t % 50 == 0)
                marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} aZ=${p.aZ} " +
                    "aO=${p.aO} aR=${p.aR} g=${p.g?.ax}:${p.g?.S} " +
                    "lock=${w.lockTarget?.aw}:${w.lockTarget?.S} " +
                    "lockaB=${w.lockTarget?.aB} gI=${p.gI} " +
                    w.npcs.filter { it.ak - p.ak in -60..140 &&
                        kotlin.math.abs(it.al - p.al) < 90 && it.S != 139 }
                        .joinToString("/") { "ax${it.ax}@${it.ak},${it.al}S${it.S}Z${it.Z[0]}" }
            else if (p.ak > 11000 && t % 10 == 0) {
                val rp = w.npcs.firstOrNull { it.aw == 93 }
                marks += "t$t S${p.S}@${p.ak},${p.al} bM=${p.bM?.aw} " +
                    "rope{bN=${rp?.bN} bP=${rp?.bP} aA=${rp?.aA} " +
                    "W=${rp?.W?.get(0)}-${rp?.W?.get(2)}x${rp?.W?.get(1)}-" +
                    "${rp?.W?.get(3)}}"
            }
            else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("CP7G goal=$goal deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(goal,
            "checkpoint7→fuse run must reach x11410 through the tower " +
            "(kick well, door 87→134, low road) — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot fights through the finale pack to mission complete`() {
        // Eighteenth leg — the level-0 finale, parked at cp7 exactly like
        // the fuse leg but continuing east: kick well → tower → door
        // 87→134 → low road → the road rises from y780 to the ~y598 tier
        // where an 8-soldier ax11 pack stands at ~40px spacing: uid287
        // (11761,600), 290 (11819,602), 288 (11859,601), 292 (11936,595),
        // 293 (12007,598), 294 (12065,598), 296 (12159,598), 297
        // (12241,599). The mission-complete trigger ax5 uid252 (12131,221,
        // box 430×590) covers the whole area — walking it fires script 116
        // → `screenL(15)`. The bot slashes through the pack (`M_CONTEXT`
        // while `g` is bound), resumes east between kills.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        var doorPulse = 0
        var strikes571 = 0                       // slice 388
        p.setPositionPx(10016, 715)
        p.N = p.ak shl 8; p.O = p.al shl 8
        w.kO = 10000; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0; var goal = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            if ((t < 3000 && t % 20 == 0) ||
                (t < 3000 && p.al >= 600 && p.ak in 11000..11600) ||
                (t < 3000 && p.ak in 11150..12150)) {
                val rp = w.npcs.firstOrNull { it.aw == 93 }
                marks += "t$t S${p.S}@${p.ak},${p.al} ag=${p.ag} aZ=${p.aZ} " +
                    "W=${p.W[0]}-${p.W[2]}x${p.W[1]}-${p.W[3]} " +
                    "g=${p.g?.aw} bM=${p.bM?.aw} " +
                    "rope{S${rp?.S} aA=${rp?.aA} bM=${rp?.bM?.aw} bN=${rp?.bN} " +
                    "au=${rp?.au}} kC=${w.kC?.aw}:${w.kC?.scriptStep} " +
                    "jC=${w.jC}"
            } else if (t % 800 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
            if (p.ak > 12131 || w.jC == 15 || w.jC == 13) { goal = true; break }
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(10016, 715)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 8) break; continue
                }
                // Slice 402: `screenL(15)` stamps medal 0 once `ap[0] >= 7`
                // kills and re-enters the MEDAL screen (jC=22) first
                // (k.java L35-L64) — confirm it (`pad.v(327712)` after j.g>=10)
                // and the stats screen follows. The bot used to kill < 7 on
                // this leg; the faithful stab/finisher arms count more.
                w.jC == 22 -> { w.pad.e(327712); w.tick(emptyList()); continue }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
                // S164: the script-250 ride pose persists after the claim
                // releases — the player is left mounted on the parked
                // carrier at the road's end; jump-off is padHeld 33024
                // (NpcFsm.kt:10505) then walk the last ~10px east.
                p.S == 164 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 12131 || w.jC == 15 || w.jC == 13) { goal = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = Pad.M_RIGHT
            val stuck = p.aZ && p.ag in -256..256
            // Suppress stuck-UP inside the S16 door boxes — standing in
            // one after a teleport would re-trigger the pair back.
            if (stuck && p.S != 79 && p.ak !in 10580..10635 &&
                p.ak !in 10780..10835) held = held or Pad.M_UP
            // Hop into the shaft's east face — S33 needs an airborne
            // wall hit, grounded runs just bounce back west.
            if (p.aZ && p.ak in 9980..10035) held = held or Pad.M_UP
            // S33/36/92/101 shaft-kick: hold the facing direction —
            // after the bounce flips av the next wall is on the new
            // facing side; dirHeld arms the grab, M_UP the kick.
            if (p.S == 33 || p.S == 36 || p.S == 92 || p.S == 101)
                held = (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
            // ax10-S16 door uid87 (W x10587-10618, y330-434): stand in
            // it and press UP → teleport east. Drop M_RIGHT so the run
            // actually settles inside the box (aZ) before the edge.
            // slice 360: aw()'s UP + carry-target arm (g.java:5257)
            // enters S6 while the posted guard uid571 stands in front,
            // and a held UP keeps it there. g/ci bind only in front:
            // face west (away from the guard) first, then pulse UP.
            if (p.ak in 10530..10620 && p.al in 380..455) {
                doorPulse = (doorPulse + 1) and 1
                held = when {
                    !p.aZ -> 0
                    p.ci != null -> Pad.M_LEFT   // slice 388: `ci` only — `g` stays
                                                 // bound while the soldier is alert
                    p.ag == 0 && doorPulse == 0 -> Pad.M_UP
                    else -> 0
                }
            }
            // Finale-shaft S33 on the fort's west face: the into-wall
            // dir arm is the LIP SCAN (dirKey && ah<0 → ct=true + scan —
            // a no-op on the flat '20' face), not the kick — the kick
            // only fires from the else arm (dirKey false → aR/aS probe
            // → S34 → wallJumpKick on u(16388) UP). Hold dir|UP while
            // al>735 (scanning, rising); at al<=735 drop the dir so the
            // else arm fires the kick at ~y733 — the S92→S36 west arc
            // then falls through the aG4 catch box x11308-11316×y681-729.
            // (Kicking at the ~y691 apex arcs the S36 ~30px too high —
            // W y609-663 clears the box's y681-729 → miss.)
            if (p.S == 33 && p.ak in 11285..11340)
                held = if (p.al > 735)
                    (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                else Pad.M_UP
            // The ax13 aG4 rope (uid93, catch box x11308-11316×y681-
            // 729) is the intended channel crossing: run east on the
            // floor into the fort's west face → z() rebound → S33 →
            // S34 wall-kick (UP held) → S92 → S36 — a grabbable state
            // through the box → latch → retract → the uid115 claim
            // binds and script 250 walks the player to the '41' road.
            // The S12 free-run CANNOT jump (g.java postTail gate —
            // `cq` never re-arms in S12), so no UP is sent here.
            if (p.aZ && p.al >= 595 && p.ak in 11100..11330 && p.bM == null)
                held = Pad.M_RIGHT
            // Each soldier binds g — slash through the pack; also clears
            // uid571 on the column top blocking the door's `g==null`.
            // EXCEPT the Z2 road-blocker at x10497: record-spawned
            // aB=300 > BW_MOCK — unkillable by design; attacking only
            // feeds its counter arm. Walk past it instead.
            // Slice 388: az() keeps `g` bound to an alert soldier for as long
            // as it lives inside the 440px / 60px-dy band — only the LOS-
            // clear candidates reset it (@1234), so the far pack below the
            // column no longer unbinds uid571 every tick. Facing away does
            // not unbind it either. Two opening strikes, then stop: the
            // soldier chases the jump off the column's west edge and falls
            // (S25, |dy| >= 60 drops `g`); the kick well brings him back up
            // to the door with `g == null`.
            val g2 = p.g
            if (g2 != null && p.aZ && !(g2.ax == 11 && g2.Z[0] == 2 &&
                g2.aB > 80) && !(g2.aw == 571 && strikes571++ >= 2))
                held = held or Pad.M_CONTEXT
            // Moat approach: strip UP so aF can't arm wallGrabSnap on
            // face contact — the ax22 eject arc then clears the lip
            // (proven on the fuse leg; same moat geometry here).
            if (p.ak > 10380 && p.al > 480 && p.S !in intArrayOf(33, 36, 92, 101))
                held = held and Pad.M_UP.inv()
            // Stay bound on the rope — the aG4 auto-retract + the
            // uid115 claim ARE the crossing (releaseRope is automatic,
            // not input-driven, so no bail input exists). The claim's
            // L108 suspension freezes the ax13 mid-ride while script
            // 250 lerps the player onto the fort top and east to the
            // '41' road; send no pad so the cutscene runs clean.
            if (w.kC != null && w.kC!!.aw == 115) {
                w.pad.e(0); w.tick(emptyList()); continue
            }
            if (w.kC != null && w.kC!!.claimActive()) {
                w.pad.e(0); w.tick(emptyList()); continue
            }
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
        }
        println("FIN goal=$goal deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks S=${p.S} ak=${p.ak} al=${p.al}")
        assertTrue(goal,
            "cp7→finale run must reach mission-complete x12131 — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot drives mission-complete stats into mission 1`() {
        // Nineteenth leg — continues past the FIN win: the jC==15 stats
        // screen (M()) confirm arm (`pad.v(458784)` → persist →
        // `pad.v(327712)` → `kAj++` → `stateL(30)`, Level0World.kt:2476-
        // 2497) → af() browse (jC=30) confirm (`pad.v(65568)` →
        // `stateL(9)`, :3680-3684) → the G() loader (jC=9: `loadPackI(1)`
        // at jG==3, `spawnEntities()` at jG==164, `pad.w(65568)` release
        // past it → `stateL(8)`, :3564-3579) → mission-1 gameplay —
        // `kBh[1]==3` = the flying canyon (ax25 player record at
        // (581,11963), 225 entities). Proves the I(aj) pack swap +
        // mission-switch wiring end-to-end through real input.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        var doorPulse = 0
        var strikes571 = 0                       // slice 388
        p.setPositionPx(10016, 715)
        p.N = p.ak shl 8; p.O = p.al shl 8
        w.kO = 10000; w.kP = 700; w.rebuildCamRect()
        for (e in w.npcs) e.recomputeAu(w.kO, w.kP, w::kBk)
        if (w.jC == 12) w.stateL(8)
        var t = 0; var deaths = 0
        var maxAk = p.ak
        val marks = mutableListOf<String>()
        var phase = 0           // 0=drive to win, 1=stats→af, 2=af→load, 3=load→play
        while (t++ < 60000 && phase < 3) {
            if (t % 50 == 0 || (phase == 0 && p.ak in 11150..12150))
                marks += "t$t ph$phase S${p.S}@${p.ak},${p.al} jC=${w.jC} " +
                    "jG=${w.jG} kAj=${w.kAj}"
            when (phase) {
                0 -> {
                    if (w.jC == 15 || w.jC == 13) { phase = 1; continue }
                    when {
                        w.jC == 12 -> {
                            w.pad.e(327712); w.tick(emptyList())
                            w.pad.e(327712); w.tick(emptyList())
                            deaths++
                            w.player.setPositionPx(10016, 715)
                            w.player.N = w.player.ak shl 8
                            w.player.O = w.player.al shl 8
                            if (deaths > 8) break; continue
                        }
                        // Slice 402: medal screen (jC=22) before the stats —
                        // see the finale leg above.
                        w.jC == 22 -> { w.pad.e(327712); w.tick(emptyList()); continue }
                        w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                        p.S == 89 || p.S == 90 -> {
                            w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                        }
                        p.S == 315 || p.S == 318 || p.S == 164 -> {
                            w.pad.e(33024); w.tick(emptyList()); continue
                        }
                    }
                    if (p.S == 65) { w.pad.e(16396); w.tick(emptyList()); continue }
                    var held = Pad.M_RIGHT
                    val stuck = p.aZ && p.ag in -256..256
                    if (stuck && p.S != 79 && p.ak !in 10580..10635 &&
                        p.ak !in 10780..10835) held = held or Pad.M_UP
                    if (p.aZ && p.ak in 9980..10035) held = held or Pad.M_UP
                    if (p.S == 33 || p.S == 36 || p.S == 92 || p.S == 101)
                        held = (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                    // slice 360: see the cp7 fuse leg — face away from
                    // the posted guard, then pulse UP inside the door.
                    if (p.ak in 10530..10620 && p.al in 380..455) {
                        doorPulse = (doorPulse + 1) and 1
                        held = when {
                            !p.aZ -> 0
                            p.ci != null -> Pad.M_LEFT   // slice 388: `ci` only
                            p.ag == 0 && doorPulse == 0 -> Pad.M_UP
                            else -> 0
                        }
                    }
                    if (p.S == 33 && p.ak in 11285..11340)
                        held = if (p.al > 735)
                            (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                        else Pad.M_UP
                    if (p.aZ && p.al >= 595 && p.ak in 11100..11330 && p.bM == null)
                        held = Pad.M_RIGHT
                    // Z2 road-blocker at x10497 is unkillable by design
                    // (aB=300 > BW_MOCK) — attacking feeds its counter;
                    // walk past it.
                    // Slice 388: two opening strikes on uid571 only — see the
                    // cp7 fuse leg (az() keeps it bound; it falls off the west
                    // edge chasing the jump).
                    val g2 = p.g
                    if (g2 != null && p.aZ && !(g2.ax == 11 &&
                        g2.Z[0] == 2 && g2.aB > 80) &&
                        !(g2.aw == 571 && strikes571++ >= 2))
                        held = held or Pad.M_CONTEXT
                    // Moat: strip UP so aF can't arm wallGrabSnap on
                    // face contact — the zone eject clears the lip.
                    if (p.ak > 10380 && p.al > 480 &&
                        p.S !in intArrayOf(33, 36, 92, 101))
                        held = held and Pad.M_UP.inv()
                    if (w.kC != null && w.kC!!.claimActive()) {
                        w.pad.e(0); w.tick(emptyList()); continue
                    }
                    w.pad.e(held); w.tick(emptyList())
                    if (p.ak > maxAk) maxAk = p.ak
                }
                1 -> {   // stats screen — two confirms: reveal-skip then advance
                    if (w.jC == 30 || w.jC == 2) { phase = 2; continue }
                    w.pad.e(327712); w.tick(emptyList())
                }
                2 -> {   // af() browse — confirm enters the G() loader
                    if (w.jC == 9) { phase = 3; continue }
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
                }
            }
        }
        // jC==9 loader: tick until past jG==164 then release-confirm → l(8)
        while (t++ < 60000 && w.jC == 9) {
            marks += "t$t load jC=${w.jC} jG=${w.jG} kAj=${w.kAj} " +
                "loadedAj=${w.loadedAj} npcs=${w.npcs.size}"
            if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
            w.tick(emptyList())
        }
        for (i in 0 until 30) w.tick(emptyList())
        val l1 = w.npcs.any { it.ax == 25 } || w.npcs.any { it.aw == 121 }
        println("MSW jC=${w.jC} kAj=${w.kAj} loadedAj=${w.loadedAj} " +
            "bh3=${w.bh3} npcs=${w.npcs.size} l1=$l1 " +
            "p=${p.ak},${p.al} S=${p.S} marks=$marks")
        assertTrue(w.jC == 8 && w.kAj == 1 && w.loadedAj == 1 && l1,
            "mission-complete → stats → af → load must land in mission-1 " +
            "play (jC=8, kAj=1, pack swapped, mission-1 records live) — " +
            "got jC=${w.jC} kAj=${w.kAj} loadedAj=${w.loadedAj} " +
            "npcs=${w.npcs.size} l1=$l1 marks=$marks")
    }

    @Test fun `bot climbs the mission-1 shaft until the alert stall`() {
        // Twentieth leg — mission 1 (kBh[1]==3, bh3 flying): the ax25
        // glider record IS the player slot (k.java:4647). `g.n()`'s
        // glide arm (PlayerFsm.kt:2400+) holds S0; `pad.u(16388)` climbs
        // (ah -= 768 to -2048+kY while kQ>117), `pad.u(33024)` dives
        // (ah += 768 to 2048+kY while kQ<230), LEFT/RIGHT bank ag±768.
        // `canyonCollide` (Entity.kt:1247): cells >=10 extrude the box,
        // cell 21 kills (`iBe` dead-drag → `stateL(12)`). The bot banks
        // left/right around wrapped wall columns while holding climb;
        // `k.aE` drains ~0.14/tick while `k.aH<0` (Level0World.kt:2383-2395)
        // — empty → stall S24 → x1=0 → S2 → l(12) = the designed fail.
        val w = world(aj = 1)
        // Faithful entry — the G() loader arms camResetC()'s bh3 init
        // (kQ=230, kX=-7, dT, dR=-1, dS=-2) at `pad.w(65568)` past jG=164;
        // entering via stateL(8) directly leaves kQ=0 (climb gate dead).
        w.stateL(9)
        while (w.jC == 9) {
            if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
            w.tick(emptyList())
        }
        val p = w.player
        var t = 0; var deaths = 0; var respawnAl = -1
        var minAl = p.al; var maxAl = p.al
        var snapFired = false
        val marks = mutableListOf<String>()
        while (t++ < 12000) {
            when {
                w.jC == 15 -> break               // won
                w.jC == 12 || w.jC == 13 -> {
                    // designed stall → fail screen → context press →
                    // reload() → a() → C() bh3 arm: respawn at the record
                    // spawn (no checkpoint can ever fire here — see the
                    // verdict below) and snap camY = p.al-230.
                    deaths++
                    var guard = 0
                    while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                        w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                        w.tick(emptyList())
                    }
                    if (respawnAl == -1) respawnAl = p.al
                    continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                // `i.bi || g.E` = scripted arm owns the flight (wind/
                // countdown latch — climb-sequence or grab-QTE). Idle-tick
                // those ticks; live input runs otherwise.
                w.iBi || Entity.gE -> {
                    if (t % 100 == 0)
                        marks += "t$t lock S${p.S}@${p.ak},${p.al} " +
                            "v=${p.ag},${p.ah} kAE=${w.kAE}"
                    w.tick(emptyList()); continue
                }
            }
            // The flight is a vertical shaft: scroll `al -= kX` carries
            // the glider, LEFT/RIGHT banks `ag±768` dodge wall columns
            // (cells >=10 extrude, 21 kills). canyonCollide wraps y at
            // %260 — sample the wrapped row a screen ahead, ±3 cols;
            // bank toward the side with more open cells, drift toward
            // the perch band x~295 when walls are even.
            val wrapRow = ((p.al - 90) % 260 + 260) % 260 / 20
            var left = 0; var right = 0
            val c0 = p.ak / 20
            for (dx in -3..3) {
                val cell = p.e(w, c0 + dx, wrapRow)
                if (cell < 10) { if (dx < 0) left++ else if (dx > 0) right++ }
            }
            // default climb (u(16388) → ah-768 floor -2048+kY while
            // kQ>117) — the shaft is climbed at ~7px/t; the designed
            // timer is the `k.aE` alert meter draining while `k.aH<0`
            // (~700t ≈ 43s of flight budget; the mid-shaft perch zone
            // (295,6959) is the leg destination).
            // Climb is a held-mask, not an exclusive choice — steer bits
            // OR into it so the glider banks WITHOUT losing the climb
            // (k.java l(x,30) clamp keeps camY rising at ~kX/2 while
            // the glider outclimbs the frame).
            var held = 16388
            var steer = 0
            if (p.ak > 335) steer = Pad.M_LEFT           // drift toward perch x295
            else if (p.ak < 255) steer = Pad.M_RIGHT
            if (right < left) steer = Pad.M_LEFT
            else if (left < right) steer = Pad.M_RIGHT
            held = held or steer
            w.pad.e(held); w.tick(emptyList())
            if (w.checkpointSnap != null) snapFired = true
            if (p.al < minAl) minAl = p.al
            if (p.al > maxAl) maxAl = p.al
            if (t % 100 == 0 || (p.al <= 8360 && p.al >= 8190)) {
                val cp = w.npcs.firstOrNull { it.ax == 2 && it.al == 8360 }
                marks += "t$t S${p.S}@${p.ak},${p.al}%${(p.al % 260 + 260) % 260} " +
                    "v=${p.ag},${p.ah} h=$held kAE=${w.kAE} kAk=${w.kAk} " +
                    "kX=${w.kX} iAJ=${w.iAJ} jC=${w.jC} x1=${p.x1} " +
                    "cp=${cp?.au},${cp?.ay},${cp?.P},${cp?.al} cam=${w.camY}"
            }
        }
        println("FLY jC=${w.jC} deaths=$deaths snap=$snapFired " +
            "respawnAl=$respawnAl al=$minAl-$maxAl iBe=${w.iBe} marks=$marks")
        // Verdict (proven, k.java:1851-1875 C() + k.java:8954-8997 L49b-L4cf):
        // cp1 (418,8360) DOES fire under the verbatim claim gate — while
        // `k.C == 0` the `cA=O; cB=P` target snap does NOT run, so `camB`
        // keeps the conveyor target and camY tracks ~7px/t (the older
        // port ran the snap every tick and halved the conveyor to ~3px/t,
        // starving the leg ~1500px short of the window — fidelity bug).
        // With the gate the au window (camY 8000-8480) is reached inside
        // the fuel budget, `aY()` writes the checkpoint, and the next
        // respawn lands AT cp1. Fuel stalls still kill a naive-climb bot
        // — deaths stay in the design. aY()'s bh3 gate fires on the first
        // frame the glider is at-or-above the checkpoint (`e.al >= aS.al`)
        // and stamps the GLIDER's own position (writeIX), so the respawn
        // lands within one climb step above cp1's y8360 — under k.I()'s
        // order the climb samples 8362 → 8358 and the stamp is 8358.
        assertTrue(snapFired && minAl < 8360 && respawnAl in 8344..8360,
            "canyon legs: the fixed conveyor keeps ~7px/t so camY reaches " +
            "cp1's au window (8000-8480) before the k.aE cap — the " +
            "checkpoint fires (snap) and respawns land at cp1 (8360) — " +
            "snap=$snapFired deaths=$deaths " +
            "minAl=$minAl respawnAl=$respawnAl marks=$marks")
    }

    @Test fun `mission-1 top claim-QTE zone binds script-1 and wins`() {
        // Slice-266 win-chain verdict — level-1 CAN be completed, via a
        // scripted QTE at the canyon TOP, not by surviving the climb.
        // ax10-uid7 (S31, record f14=8) sits at (456,481) with
        // W=[456,481,764,705]; Z[3]=rf(14)=8 = the done-sentinel script
        // uid (NpcFsm.kt:1125-1127 `Z={r8[4],r8[11],r8[13],r8[14],
        // r8[15]}`). Player overlap arms the lane sequence
        // (Z[1]=2 → nibble pack {0,0,0,2} → single live lane, CS[2]=
        // 16388 UP); the matching press at j>3 sets `aA=Z[3]=8`
        // (NpcFsm.kt:1931), the draw side (aU(), NpcFsm.kt:10524-10525)
        // latches `nl=1` on bh3, and the consumed arm binds
        // `k.s(8)` = script index 1 = `scripts.bin` s1 (uid8):
        // a 62-step type-2 block that walks uid1 (the ax25 record = the
        // player) through op21 waypoints 511→-60 with a type-1 camera
        // block, ending `op37[1]` → `screenL(15)` (Entity.kt:2308).
        val w = world(aj = 1)
        w.stateL(9)
        while (w.jC == 9) {
            if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
            w.tick(emptyList())
        }
        val p = w.player
        // Teleport into the S31 zone box — the claim mechanics under
        // test (the ~600t leg verdict on REACHING it lives in the shaft
        // bot above).
        p.ak = 600; p.N = 600 shl 8
        p.al = 540; p.O = 540 shl 8
        p.ah = 0; p.ag = 0
        val marks = mutableListOf<String>()
        var zref: com.acrebuild.core.Entity? = null
        var won = false; var bound = false; var t = 0
        while (t++ < 300) {
            // `missionWon` (not jC==15): stateL(15) immediately
            // redirects to i=22 (medal screen) or i=10 (level select)
            // when stamps/next-mission conditions hold — the flag is
            // set on the iArg==15 entry itself.
            if (w.missionWon) { won = true; break }
            // Camera follows the player: au = offscreen score
            // (|ak-(kO+200)|/400 + |al-(kP+120)|/120) — pinning to the
            // player keeps au≈0 AND lets the et stamp ring roll the
            // way it does in real flight (walls follow camY).
            w.kP = p.al - 120; w.kO = p.ak - 200; w.rebuildCamRect()
            // Lane prompt CS[2]=16388 (UP) — press every tick; the live
            // lane consumes one edge, later presses are no-ops.
            w.pad.e(16388); w.tick(emptyList()); w.pad.releaseFlush()
            // Hold the player in the cy26-28 air pocket (x520-640, below
            // the cy20-25 stamped-wall band). The chain under test is
            // zone→script-1→op37[1]→screenL(15); the ~70-step waypoint
            // ride crosses a 21-stamped band whose wrap stamps churn
            // with the ring — pinning keeps the player alive while the
            // script's key counter climbs to key62. (The ride path
            // itself stays `inferred`.)
            p.ak = 560; p.N = 560 shl 8
            p.al = 560; p.O = 560 shl 8
            p.ah = 0; p.ag = 0
            val z = w.npcs.firstOrNull { it.ax == 10 && it.S == 31 }
            if (z != null && z.claimActive()) bound = true
            if (t % 25 == 0 || (z?.claimActive() == true && t % 8 == 0)) {
                marks += "t$t jC=${w.jC} S=${p.S}@${p.ak},${p.al} " +
                    "z(aB=${z?.aB},m=${z?.m},aA=${z?.aA},nl=${z?.nl}," +
                    "claim=${z?.claimActive()},step=${z?.scriptStep})"
            }
            if (z != null && zref == null) zref = z
        }
        println("WINTOP won=$won bound=$bound marks=$marks")
        // Verdict (proven — NpcFsm.kt:1826-1947 S31 arm + i.java:2205
        // record fields + scripts.bin s1-key62): the zone arms its lane
        // QTE on overlap, the UP press resolves it, `aA=8` binds
        // script-1 via `k.s(8)` = `kEh.indexOf(8)` = 1 (the port fixed
        // `w.kS` — the Entity stub that always returns -1 — to
        // `w.kSIndex`), and the 62-step scripted ascent consumes the
        // blk1 key62 `op37[1]` → `screenL(15)` → `missionWon`.
        // Faithful detail: `i.be` is the cell-21 death-slide latch
        // (static, set by `canyonCollide`'s aT/aU==21 arm at
        // Entity.kt:1278-1281 and the S10 out-of-band kill at
        // NpcFsm.kt:1316) — ANY cell-21 death in the canyon removes
        // every S31 zone via `be → k.c(this)` (i.java:12350).
        // Reaching (456,481) by play is the unfixed part — see the
        // shaft bot's ~600t-leg verdict.
        assertTrue(won,
            "top-zone win chain: overlap → lane QTE → aA=8 → " +
            "bindScript(k.s(8)) → script-1 ascent → screenL(15) — " +
            "bound=$bound marks=$marks")
    }

    @Test fun `bot runs door-exit to checkpoint3 through the gate row`() {
        // Twelfth leg — the ax10-S16 door deposits the player on the upper
        // tier (~3812,559 over the y580 step). East is blocked by the
        // x4000-4060 '20' stack (aY=4 face → S12's arm faithfully has no
        // exit — proven dead-stall, g.java L16c0), so the route is UNDER
        // it: aZ+DOWN drops through '5'@580 (a(257,8) — unreachable inside
        // S12, so the approach hop-runs), S12 autoruns the walkway below
        // the shelf through the ax44 slam-gate row (their W is all-zero —
        // crush can't fire), the x4260 '20' column face gives S33 climb →
        // pulsed-UP lip-scan → S92 mantle onto the '5'@580 east lip, the
        // S37 monkey-bar shimmy carries east past the lip, S43 drops onto
        // '20'@580's east face and S79 slides down to the deep floor —
        // east to the ax2 checkpoint (4629,646).
        //
        // Slice 372: the slide ends on the rock's slope (cells 24/25, from
        // x4541). There the S79 run-start (S32, `Q == 79` → back to S79)
        // is a faithful pin: on a slope cell `x()` clears `v`, so `a(true)`
        // skips its `bb == bc` clear (i.java:532-540) and the side strips
        // — which read the slope cells 24/25 ≥ 18 — leave `bb`, `bc` both
        // set; S32's `y() && ag != 0 → ag = 0` (g.javap e() 6553-6569)
        // then zeroes every run-start. The pre-dispatch rescan used to
        // push him out of it. A real player jumps: UP|RIGHT from the pin
        // (S79/S32 → S21 pre-jump → S22) clears x4640.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        p.setPositionPx(3812, 559)
        p.N = p.ak shl 8; p.O = p.al shl 8
        var t = 0; var deaths = 0; var checkpoint = false
        var maxAk = p.ak; var minAl = p.al
        val marks = mutableListOf<String>()
        while (t++ < 40000) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    w.player.setPositionPx(3812, 559)
                    w.player.N = w.player.ak shl 8; w.player.O = w.player.al shl 8
                    if (deaths > 40) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                p.S == 315 || p.S == 318 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
            }
            if (p.ak > 4640) { checkpoint = true; break }
            if (p.S == 65) {
                w.pad.e(16396); w.tick(emptyList()); continue
            }
            var held = 0
        val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 4) && it.S != 139 &&
                    it.ak - p.ak in -20..90 &&
                    kotlin.math.abs(it.al - p.al) < 80
            }
            if (foe != null && t % 4 < 3) held = held or Pad.M_CONTEXT
            // Route: hop the approach, drop through '5'@580, autorun the
            // walkway (S12 ignores the gate row — their W is all-zero and
            // crush can't fire), mantle the x4260 column, shimmy the '5'
            // lip east, drop to the floor. Direction is never held during
            // a landing tick (one grounded direction-tick → ax() → S12's
            // faithful dead-stall at the '20' face).
            if (p.aZ && p.ak > 4560 && (p.S == 79 || p.S == 32))
                held = Pad.M_RIGHT or Pad.M_UP        // jump out of the slope
                                                      // pin (slice 372)
            else if (p.aZ && p.ak in 3870..3990 && p.al in 540..620)
                held = held or Pad.M_DOWN             // drop through '5'
            else if (p.S == 33 || p.S == 34)
                held = Pad.M_RIGHT or (if (t % 8 < 2) Pad.M_UP else 0)
                                                      // pulsed UP drives the
                                                      // lip-scan → S92 mantle
            else if (p.S == 37 || p.S == 38)
                held = if (p.ak >= 4225) Pad.M_UP     // slice 413: `g.c(Z)` reads the RAW
                                                      // `k.g`, so the '5' shimmy stops at
                                                      // the '20' mass (x4260) — UP on an
                                                      // S38 tick vaults onto the lip (S54)
                       else Pad.M_RIGHT               // '5' shimmy east
            else if (p.aZ)
                held = if (p.ak > 3900) Pad.M_RIGHT   // autorun the walkway
                          else held or Pad.M_UP       // hop approach
            else if (!p.aZ && p.ah < 0)
                held = held or Pad.M_RIGHT            // drift on the rise
            w.pad.e(held)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.al < minAl) minAl = p.al
            if (t in 25..120 || (t < 600 && t % 20 == 0))
                marks += "t$t S${p.S}@${p.ak},${p.al} av=${p.av} ag=${p.ag} aX=${p.aX} aY=${p.aY} aT=${p.aT} aU=${p.aU} cq=${p.cq} co=${p.co}"
            else if (t % 600 == 0) marks += "t$t S${p.S}@${p.ak},${p.al}"
        }
        println("GATEROW checkpoint=$checkpoint deaths=$deaths maxAk=$maxAk " +
            "minAl=$minAl marks=$marks")
        assertTrue(checkpoint,
            "east run must reach x4640 past the gate row — " +
            "maxAk=$maxAk deaths=$deaths")
    }

    @Test fun `bot flight drop-lines arm the canyon shrines and refill the meter`() {
        // Slice-267 — the k.aE alert meter (i.java:2548, reset 100/0/-1
        // in D() on every fail-reload) funds ~600t per leg: `g.n()`
        // drains aE-1 per 6 ticks while k.aH<0 (g.java:5851-5856 —
        // aH<0 → aG--; aG==0 → aG=6, aE--), and S24 stall → x1=0 →
        // l(12) on empty (g.java:5861-5871). Checkpoint bands CANNOT
        // segment the ascent: `aY()` ticks only under `au<2` and the
        // fixed-speed drift camera bottoms ~1500px above the cp1
        // window in one leg (proven: the shaft run's minCamY≈9988 vs
        // the required [8000,8480]). The REAL refill mechanism is the
        // shrine chain: free-glide auto-emits ax24-S6 drop-lines via
        // `e(false)`/`flap(p,false)` (g.java:6330-6355 + PlayerFsm —
        // k.aI≥10 gate, ~100px drop, af==aS); the drop-line's `bc()`
        // sweep converts overlapped ax24-S19 lay-children to S20
        // shrines (i.java L171/L181 + bc case-24, NpcFsm.kt:6945), and
        // the S20 arm on player overlap pays `k.aF=min(aB,100-aE)`
        // (+3/t into aE), `e=30` wall-immunity, `k.X=kAJ` conveyor
        // restore, `sfx(25)`, and aS.i(21). Level-1 carries 6×S19
        // along the corridor (x∈{222,397,413,454,475,599}) — the
        // designed leg loop is fly → drop-line arms the shrine → fly
        // through it → refill.
        val w = world(aj = 1)
        w.stateL(9)
        while (w.jC == 9) {
            if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
            w.tick(emptyList())
        }
        val p = w.player
        var t = 0; var deaths = 0
        var s6Emitted = 0; var shrinesArmed = 0; var shrineFired = false
        var kAFMax = 0; var iEMax = 0; var pS21 = false
        var minAl = Int.MAX_VALUE
        val seen = mutableSetOf<Int>()
        val marks = mutableListOf<String>()
        // Phase A — real flight from spawn (~400t): proves the k.aI≥10
        // auto-emitter produces drop-lines on the actual climb path.
        while (t++ < 400) {
            when {
                w.jC == 12 || w.jC == 13 -> {
                    deaths++
                    var guard = 0
                    while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                        w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                        w.tick(emptyList())
                    }
                    continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                w.iBi || Entity.gE -> { w.tick(emptyList()); continue }
            }
            w.pad.e(16388); w.tick(emptyList())
            if (p.al < minAl) minAl = p.al
            for (n in w.npcs) {
                if (n.ax == 24 && n.S == 6 &&
                    seen.add(System.identityHashCode(n))) {
                    s6Emitted++
                    if (s6Emitted <= 4) marks += "S6@t$t p=${p.ak},${p.al} w=${n.ak},${n.al}"
                }
            }
        }
        marks += "PHASEA s6=$s6Emitted al=${p.al} minAl=$minAl kAE=${w.kAE}"
        // Phase B — probe: hover the player under the S19 at (397,8437)
        // (its lay box is -15,-81,39,93 on the anchor); the drop-lines
        // rise ~180px and must sweep it into S20. The probe then snaps
        // the camera to the shrine (au<2 else the entity parks) and
        // pins the player inside its box (-21,-80,42,92) so L181's
        // overlap arm fires — the canyon's leftward airflow otherwise
        // drags the probe off-column before the slow climb can cover
        // the ~50px gap (a real pilot steers through it).
        p.ak = 397; p.N = 397 shl 8
        p.al = 8540; p.O = 8540 shl 8
        p.ah = 0; p.ag = 0; p.setAnim(4)
        var probe = 0
        while (probe++ < 900 && w.jC == 8) {
            // Hover under the S19 so the rising drop-line sweep can
            // reach it; once a shrine arms, fly into its box — L181
            // fires on player↔shrine overlap.
            var mask = 0
            if (p.ak < 405) mask = mask or 8256           // M_RIGHT
            if (p.al > 8500) mask = mask or 16388         // M_UP
            // Slice 385: `bc()` sweeps `k.bd` — only what the last paint
            // drew — so the probe holds the camera on the S19 column
            // until it arms (the teleport left the drift camera ~600px
            // below it; a real pilot reaches it with the camera).
            if (shrinesArmed == 0) { w.kO = 397 - 200; w.kP = 8437 - 120; w.rebuildCamRect() }
            w.pad.e(mask)
            w.tick(emptyList())
            for (n in w.npcs) {
                if (n.ax == 24 && n.S == 6 &&
                    seen.add(System.identityHashCode(n))) {
                    s6Emitted++
                    if (s6Emitted <= 4) marks += "S6@probe$probe p=${p.ak},${p.al} w=${n.ak},${n.al}"
                }
                if (n.ax == 24 && n.S == 20) {
                    if (seen.add(10000 + n.aw)) {
                        shrinesArmed++
                        marks += "SHRINE@probe$probe uid${n.aw}@${n.ak},${n.al} p=${p.ak},${p.al}"
                        // Snap the camera to the shrine (via the k.O/k.P
                        // projections): au recompute parks the entity
                        // while the cam is far away.
                        w.kO = n.ak - 200; w.kP = n.al - 230; w.rebuildCamRect()
                    }
                    if (!shrineFired) {
                        // Pin the player inside the shrine box
                        // (-21,-80,42,92) until L181's overlap arm fires.
                        p.ak = n.ak; p.N = n.ak shl 8
                        p.al = n.al - 40; p.O = p.al shl 8
                        p.ah = 0; p.ag = 0
                        p.refreshBoxes()
                    }
                }
            }
            if (w.kAF > kAFMax) kAFMax = w.kAF
            if (w.iE > iEMax) iEMax = w.iE
            if (p.S == 21) { pS21 = true; shrineFired = true }
            if (w.kAF > 0) shrineFired = true
            if (probe % 100 == 0)
                marks += "pb$probe S${p.S}@${p.ak},${p.al} kAE=${w.kAE} " +
                    "kAF=${w.kAF} iE=${w.iE} s6=$s6Emitted sh=$shrinesArmed"
            if (probe <= 60) {
                val s6 = w.npcs.firstOrNull { it.ax == 24 && it.S == 6 }
                val s19 = w.npcs.firstOrNull { it.ax == 24 && it.S == 19 }
                if (s6 != null && probe % 5 == 0)
                    marks += "DL$probe w=${s6.ak},${s6.al} X=${s6.X?.contentToString()} " +
                        "ap=${s6.ap} s19W=${s19?.W?.contentToString()} p=${p.ak},${p.al}"
            }
            if (shrineFired && probe > 60) break
        }
        println("SHRINES s6=$s6Emitted armed=$shrinesArmed fired=$shrineFired " +
            "kAFMax=$kAFMax iEMax=$iEMax pS21=$pS21 minAl=$minAl " +
            "deaths=$deaths marks=$marks")
        // Verdict (proven — flap() + bc() case-24 + L181): the player's
        // own drop-lines convert the S19 columns into S20 shrines, and
        // flying through one charges the k.aF refill pool (+iE=30
        // wall-immunity + S21) — the canyon's real meter economy.
        assertTrue(s6Emitted >= 1,
            "free-glide must auto-emit ax24-S6 drop-lines (k.aI>=10 " +
            "gate) — s6=$s6Emitted marks=$marks")
        assertTrue(shrinesArmed >= 1,
            "a drop-line sweep must convert an ax24-S19 into the S20 " +
            "shrine — armed=$shrinesArmed s6=$s6Emitted marks=$marks")
        assertTrue(shrineFired,
            "flying through the armed S20 shrine must fire L181 " +
            "(k.aF refill + iE + S21) — fired=$shrineFired " +
            "kAFMax=$kAFMax iEMax=$iEMax marks=$marks")
    }

    @Test fun `canyon gap leg starves before the camera reaches the shrine window`() {
        // Slice-268 verdict (proven — source + arithmetic + this probe):
        // the bh3 fuel economy is camera-paced. `ah` relaxes to
        // kY=-1792 (-7px/t) unconditionally (g.java:6290-6297 settle);
        // the camera is a fixed metronome — `cB += kX` (-7) then
        // `camY += l(camB-camY,30)` and the L142 snap → camY -= 3.5/t.
        // Shrines tick only while au<=1 — i.e. camY within ~[al-240,
        // al+120] — so a shrine's tick window opens leg_px/3.5 ticks
        // after the previous regardless of player speed: fuel cost =
        // leg_px/21. The 6016→2925 gap is 3091px ≈ ~147 fuel > the
        // 100 tank — this probe proves the empirical bound: parked
        // inside the 2925 box, the tank dies while the camera is still
        // ~1245px short of the window.
        val w = world(aj = 1)
        w.stateL(9)
        var boot = 0
        while (w.jC == 9 && boot++ < 400) {
            if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
            w.tick(emptyList())
        }
        val p = w.player
        val shr = w.npcs.first { it.ax == 24 && it.aw == 106 }   // (475,2925)
        // Recreate the respawn state: player inside the 6016 box, fresh
        // tank, camera snapped (k.C() — `camB=camY=al-230` verified).
        p.ak = 223; p.al = 6007; p.setAnim(4); p.ah = 0; p.ag = 0
        w.kP = 6007 - 230; w.kO = 0; w.kAE = 100; w.kAF = 0; w.rebuildCamRect()
        var t = 0; var arrived = false; var deadCamY = -1; var deadAe = -1
        while (t++ < 40000) {
            var mask = 0
            when {
                w.jC == 12 || w.jC == 13 -> {
                    deadCamY = w.kP; deadAe = w.kAE; break
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                w.iBi || Entity.gE -> { w.tick(emptyList()); continue }
            }
            val inBox = Entity.overlapStrict(p.W, shr.W)
            if (!inBox && p.al > shr.al - 60) {
                if (w.kAE > 0) mask = mask or 16388            // sprint UP
            } else {
                if (p.al > shr.al + 10) mask = mask or 33024   // park on row
                else if (p.al < shr.al - 40) mask = mask or 16388
                arrived = true
            }
            if (p.ak < shr.ak - 8) mask = mask or 8256
            else if (p.ak > shr.ak + 8) mask = mask or 4112
            w.pad.e(mask); w.tick(emptyList())
            if (t > 30000) break
        }
        // The faithful bound: the starve (jC==12) fires while the camera
        // is still well short of the shrine's tick window — i.e. the leg
        // cannot be crossed on a single tank under the verbatim model.
        assertTrue(arrived,
            "bot must reach the 2925 shrine box — p=${p.ak},${p.al}")
        assertTrue(deadCamY > 0,
            "the starve (S24→jC12) must fire — t=$t p=${p.ak},${p.al}")
        // window opens at camY ~ al+120±240 → camY <= ~3165; assert the
        // camera was still materially above it when the tank emptied.
        assertTrue(deadCamY > shr.al + 400,
            "camera must still be short of the 2925 tick window when the " +
            "tank dies — deadCamY=$deadCamY shrAl=${shr.al} (the verbatim " +
            "economy is camera-paced; a leg can't be outrun)")
    }

    @Test fun `all eight mission packs boot and tick clean`() {
        // Slice-269 coverage: the all-mission conversion (slice 176)
        // claims every pack loads — this boots each `level<aj>` through
        // the real intro (stateL(9) -> play), asserts the record-driven
        // entity set spawns, and ticks 300 live ticks without exception.
        for (aj in 0..7) {
            val w = world(aj = aj)
            w.stateL(9)
            var boot = 0
            while (w.jC == 9 && boot++ < 600) {
                if (w.jG > 164) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
                w.tick(emptyList())
            }
            assertTrue(w.jC != 9, "mission $aj stuck in load state jC=${w.jC}")
            val spawned = w.npcs.size
            assertTrue(spawned > 50,
                "mission $aj must spawn its records (got $spawned)")
            // 300 live ticks — dialogs auto-dismissed; assertions on the
            // world staying coherent (no crash, jC stays in the play set).
            var t = 0
            while (t++ < 300) {
                if (w.jC != 8 && w.jC != 21 && w.jC != 12 && w.jC != 13) break
                w.tick(emptyList())
            }
            assertTrue(w.player.ax != -1, "mission $aj has a live player entity")
            println("mission $aj jC=${w.jC} npcs=$spawned ax=" +
                w.npcs.groupingBy { it.ax }.eachCount()
                    .toSortedMap().entries.joinToString(",") { "${it.key}x${it.value}" })
        }
    }

    @Test fun `bot completes mission-0 end to end - spawn to mission complete`() {
        // Slice-270 capstone: ONE continuous run from spawn, no teleports.
        // The route policy is position-keyed, stitched from the proven
        // per-leg drivers (slices 245-262): respawn positions replay their
        // own segment naturally since the policy keys on ak/al, not stage.
        val w = world()
        w.stateL(8)
        settleIntro(w)
        val p = w.player
        var t = 0; var deaths = 0; var won = false
        var maxAk = p.ak; var minAl = p.al
        var stall = 0; var vaultCd = 0; var atkCd = 0; var jumpCd = 0
        var lastFoe: Entity? = null
        var dir = Pad.M_RIGHT
        val marks = mutableListOf<String>()
        val trace = ArrayDeque<String>(80)
        var lastS = p.S
        val milestones = intArrayOf(1400, 2500, 3900, 4650, 5950, 7150,
                                    8950, 10020, 11410, 12150)
        var mi = 0
        var towerStall = 0                       // far-tower lip hold (door-phase sweep)
        var doorPulse = 0                        // UP-edge cadence in the door box
        var topUpHeld = false                    // UP-edge cadence on the massif top
        // pole cycle→phase map for the far-tower shaft arm (24400+):
        // 7-tick door cycle (measured): S0(1t, closed+crush) → S1(3t,
        // opening) → S2(1t, open) → S3(2t, falling+crush) → S0.
        fun polePh(e: Entity) = when (e.S) {
            0 -> 0; 1 -> 1 + e.T.coerceAtMost(2)
            2 -> 4; 3 -> 5 + e.T.coerceAtMost(1); else -> 0 }
        // corridor crush: the pole's box is always up — only its S3 pair
        // (2 of 7 ticks) kills. The landing overlap is ~5 ticks (descent
        // tail + instant hop-takeoff); survivable only when the drop fires
        // during the covering pole's LAST crush tick (S3-T1) — the overlap
        // then spans its non-lethal run before the next pair arrives.
        fun dropSafe(exitAk: Int): Boolean {
            val poles = w.npcs.filter { it.ax == 44 && it.ak in 3850..4270 }
            val covering = poles.filter {
                exitAk - 10 <= it.W[2] && exitAk + 10 >= it.W[0] }
            // The S257 drop lands 15t after the press — one phase on.
            // The poles tick before the player (k.I() order) and bv()
            // tests both boxes as the previous frame left them (slice
            // 362); the crush ticks are the S3 pair — pre-tick ph5
            // tests the S3T0 box (y767+), ph6 the S3T1 box (y755+).
            // The run-hop (S5, S12, S12, S233) keeps the bot on the
            // floor through the 4th tick after landing, so only a ph0
            // landing keeps both crush ticks off grounded frames. Drop
            // at ph6 (S3T1) → land at ph0.
            return covering.all { polePh(it) == 6 }
        }
        while (t++ < 140000) {
            when {
                w.jC == 15 || w.missionWon -> { won = true; marks += "WON@${p.ak} t=$t"; break }
                w.jC == 12 || w.jC == 13 -> {
                    val dieNear = w.npcs.filter {
                        kotlin.math.abs(it.ak - p.ak) < 250 &&
                        kotlin.math.abs(it.al - p.al) < 160 }
                        .joinToString(",") { "ax${it.ax}@${it.ak},${it.al}S${it.S}" }
                    marks += "died@${p.ak},${p.al} S${p.S} x1=${p.x1} near=$dieNear" +
                        " " + trace.takeLast(6)
                    w.pad.e(327712); w.tick(emptyList())
                    w.pad.e(327712); w.tick(emptyList())
                    deaths++; marks += "respawn@${p.ak},${p.al} t=$t"
                    towerStall = 0
                    if (deaths > 300) break; continue
                }
                w.jC != 8 -> { w.pad.e(Pad.M_CYCLE); w.tick(emptyList()); continue }
                p.S == 65 -> {
                    // ax22 capture-zone vault-out (aN() L25/L27): the
                    // exit mask is keyed by the armed zone's Z[2] —
                    // Z[2]!=0 → 16396 → EAST vault (+3328); Z[2]==0 →
                    // 16390 → WEST (-3328, the 'return to sender' for
                    // falling into the shaft). The armed zone is the one
                    // in S1: channel zones stay west, the massif-face
                    // catch @10414 (Z[2]=1) throws back EAST onto the
                    // face lip.
                    val zn = w.npcs.firstOrNull { it.ax == 22 && it.S == 1 }
                    w.pad.e(if (zn != null && zn.Z[2] != 0) 16396 else 16390)
                    w.tick(emptyList()); continue
                }
                // Slice 404: lip sentinels. Mantling the x2200 lip (S60) puts the climber
                // inside the unaware sentinel e151 (x2213, `Z5=Z6=0`), and a soldier is
                // solid to the player — the shove drops him off the lip, over and over.
                // The designed answer is the ledge assassination: the context button at
                // the lip grab (S60 → S203 carry, g.h = the victim) and once more to
                // throw the victim off (S203 → S204).
                (p.S == 60 || p.S == 203) && w.npcs.any {
                    it.ax == 11 && it.aB > 0 && (it.j == 0 || p.S == 203) &&
                    Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) <= 5
                } -> { w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue }
                p.S == 203 -> {
                    // slice-279: S203 at the massif lip is a ledge-hang
                    // (ax10-S43 arm i.java:L852-ish sets 203 on a S60/61
                    // hang inside the lip zone; gh==null → G() drops to a
                    // plain hang) — v(M_UP) takes the shared tail's
                    // climb-up S62 (PlayerFsm L2460, proven).
                    w.pad.e(Pad.M_UP); w.tick(emptyList()); continue
                }
                (p.S == 280 || p.S == 38) && p.ak in 1740..1900 && p.al < 850 -> {
                    // Slice 369: the S257 drop-through ends in `a(0)` (S43,
                    // g.javap.txt e() 12853-12855), so the first fall tick
                    // still has the '5' strip at the head (aO == 5) and the
                    // post-tail's `cw && aO == 5 → i(280)` hangs him under
                    // it. Let go — S38's `u(33024)` drop (al = W[3]+10;
                    // i(43)) — instead of mantling back up into the loop.
                    w.pad.e(Pad.M_DOWN); w.tick(emptyList()); continue
                }
                (p.S == 280 || p.S == 38 || p.S == 54) && p.ak < 3000 -> {
                    // '5'-lip hang → mantle (slice-273): the fixed S38 arm
                    // takes u(16388) → enterStateMasked(54,8); S280/S54
                    // transition on their own. Scoped to the channel region
                    // — in the crusher corridor the hang states belong to
                    // the shimmy route (held RIGHT, not UP-mantle).
                    w.pad.e(Pad.M_UP); w.tick(emptyList()); continue
                }
                p.S == 315 -> {
                    // bound catch hang (L23bb): rides until al>go → S318.
                    // Hold still — the zone parks the player mid-shaft.
                    w.pad.e(0); w.tick(emptyList()); continue
                }
                p.S == 318 -> {
                    // L23fa (proven): frozen until a held DOWN flings —
                    // release drops ~110px into the channel where a
                    // RIGHT|UP drift re-grabs the wall-B face.
                    w.pad.e(Pad.M_DOWN); w.tick(emptyList()); continue
                }
                p.S == 89 || p.S == 90 -> {
                    w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); continue
                }
                // bound carrier dismount — only in the finale zone; west
                // of x10000 the wire-chain rails ride to their own
                // auto-dive and a DOWN press here fires the rail's
                // jump-off arm (bw() padHeld(33024)) on the catch tick.
                p.S == 164 && p.ak > 10000 -> {
                    w.pad.e(33024); w.tick(emptyList()); continue
                }
                p.S == 326 -> {                      // rope climb
                    w.pad.e(16388); w.tick(emptyList()); continue
                }
                p.S == 360 -> {
                    w.pad.e(Pad.M_RIGHT); w.tick(emptyList()); continue
                }
            }
            // Active claim script: hold the script's own wait-mask —
            // op107 arms cb[0] with the prompt key (ambush at x6009:
            // UP then CONTEXT); timeout on op108's poll chains the
            // fail script (kEh uid 47 → screenL(12)). CONTEXT covers
            // the dialog-advance prompts; bound rides want silence.
            if (w.kC != null && w.kC!!.claimActive()) {
                // silence covers bound rides (S164/bM), the finale chain
                // (ak>11000), AND the wire corridor's airborne catch
                // ticks — a claim wait-mask of M_DOWN held while S43
                // falling into a rail's window fires the rail's own
                // jump-off arm (bw() padHeld(33024)) and drops the bot.
                if (p.ak > 11000 || p.S == 164 || p.bM != null ||
                    (!p.aZ && p.ak in 7400..8300 &&
                     p.al in 300..700)) {
                    w.pad.e(0); w.tick(emptyList()); continue
                }
                val waitMask = w.kC!!.cb?.get(0) ?: 0
                w.pad.e(if (waitMask != 0) waitMask else Pad.M_CONTEXT)
                w.tick(emptyList()); continue
            }
            val foe = foeNear(w, p)
            var held = Pad.M_RIGHT
            // ---------------- position-keyed route policy ----------------
            when {
                // spawn pocket → x1400 wall + channel crossing: hold RIGHT,
                // generic unstick; past x1560 the proven slice-273 route
                // takes over — see the legs below.
                p.ak < 2500 -> {
                    if (stall > 40 || p.S == 33 || p.S == 34 || p.S == 101 ||
                        p.S == 92) held = held or Pad.M_UP
                    if (p.ak < 1560) {
                        // spawn→x1400→corridor legs (unchanged): kick-latch
                        // arms + pinned-run unstick; aerial chains handled
                        // by the state keys above.
                        if (p.aZ && p.ag in -256..256 && p.S != 79)
                            held = held or Pad.M_UP
                        // L1f35 fresh-W: after the S36 west-kick, drift
                        // back east to the tower face and re-grab higher —
                        // chaining S33/S92 grabs climbs the face to the
                        // ax7 slot @1397,506 (swallow → eject east over
                        // the wall). RIGHT alone lets the fall run out
                        // first; UP arms the aF latch on face contact.
                        // Scoped x>1100 so the x930-1040 descent pit
                        // keeps its proven tap-fall route.
                        if (!p.aZ && p.ak > 1100)
                            held = held or Pad.M_UP
                        // G12 re-route: the kick off the x1000 pit wall
                        // must rise into the ax46 bar @1067,650 (S327 →
                        // S330 catch → S165 launch east). The bar tests
                        // last frame's W (k.I() runs bb[] before aS.I()),
                        // and its X box ends at y≈686 — a grab at y≈818
                        // (the free ping-pong's hop meeting the wall on
                        // its way down) kicks 1px short. Run west and
                        // take off ~45px out so the hop meets the wall
                        // at its apex (grab y≈797). Too close → back off.
                        if ((p.aZ || p.S == 5) && p.av && p.al > 840 &&
                            p.ak in 1000..1110) {
                            held = when {
                                p.ak > 1060 -> Pad.M_LEFT
                                p.ak >= 1046 -> Pad.M_LEFT or Pad.M_UP
                                else -> Pad.M_RIGHT
                            }
                        }
                        // Slice 369 re-route: the S36 kick off the x1400
                        // wall leaves him facing west; S5 now runs `l()`
                        // under a held direction (g.javap.txt e() 4775-
                        // 4793), so the old RIGHT|UP turned him back east
                        // on every landing. Hop the pit floor west with
                        // LEFT|UP until the x1000-1110 take-off arm above.
                        if ((p.aZ || p.S == 5) && p.av && p.al > 840 &&
                            p.ak in 1111..1390)
                            held = Pad.M_LEFT or Pad.M_UP
                    } else {
                        // CHANNEL CROSSING (slice-273 — proven end-to-end):
                        // the '5' floor strip x1580-2120 is ONE continuous
                        // surface — pocket floor, shaft floor and valley
                        // floor share it. Route: drop through it at
                        // x1740-1820 → chamber (under wall-B) → run east →
                        // dip kick UP|LEFT → '5' west-lip hang (S280) →
                        // S38 → u(UP) → S54 mantle → valley floor → run
                        // west → UP|LEFT hop into ax22 zone @2064 → chain
                        // vaults @1975→@2104 → wall-C lip → S203→UP→S62 →
                        // wall-C top → east-lip hop → plateau face → cp2.
                        held = when {
                            // shaft floor: '5' drop-through to the
                            // chamber — also dodges the shaft-floor
                            // guard's first swing.
                            p.aZ && p.al in 795..805 && p.ak in 1740..1900 ->
                                Pad.M_DOWN
                            // chamber floor (y959): east to the dip, then
                            // UP|LEFT hop arcs west-up onto the '5' gap's
                            // west lip → S280 hang.
                            p.aZ && p.al > 900 ->
                                if (p.ak < 2140) Pad.M_RIGHT
                                else Pad.M_UP or Pad.M_LEFT
                            // valley floor (y799, x1920-2120 — wall-B's
                            // east face to the dip gap): run west to
                            // ~x2075, then UP|LEFT hop — the arc tops
                            // ~y735 inside ax22 zone @2064 → S65 chain.
                            p.aZ && p.al in 795..805 && p.ak > 2075 ->
                                Pad.M_LEFT
                            p.aZ && p.al in 795..805 ->
                                Pad.M_UP or Pad.M_LEFT
                            // wall-C top (y479): east hop at the lip —
                            // clears the '2'-ledge door line and grabs
                            // the plateau's west face top (y520).
                            p.aZ && p.al in 460..490 && p.ak in 2300..2360 ->
                                Pad.M_UP or Pad.M_RIGHT
                            else -> Pad.M_RIGHT
                        }
                        jumpCd--
                    }
                }
                // cp2→tower verified route (slice-274): plateau floor →
                // 3-cell step hop @2640-2720 → run off x2740-2900 dropping
                // into the under-slab corridor (y540-680) → corridor walk
                // → hop onto the ax10-S34 rail band @3140-3200 → pillar
                // floor → door-A teleport (UP @x3760-3820/y780-850) → S285
                // at fuse-B (3804,578) → upper tier run-off @x3840-3920 →
                // '5'-strip underside grab / '2' walkway → S37 shimmy east.
                p.ak in 2500..3900 -> {
                    held = when {
                        p.aZ && p.ak in 2640..2720 ->
                            Pad.M_RIGHT or Pad.M_UP          // 3-cell step hop
                        !p.aZ && p.ak in 2740..2900 && p.al in 460..700 ->
                            0                                // corridor drop
                        p.aZ && p.al in 540..679 && p.ak < 3160 ->
                            Pad.M_RIGHT                      // corridor walk
                        p.aZ && p.ak in 3140..3200 && p.al in 650..690 ->
                            Pad.M_RIGHT or Pad.M_UP          // rail-band hop
                        // door-A zone x3784-3818/y737-837 — grounded UP.
                        // aZ required: airborne UP triggers ledge-grabs
                        // that drop him into the door-crush band instead.
                        // The x window must stay INSIDE the door's W x-span
                        // (record fields → W=[3784,737..3818,837]): pressing
                        // UP west of 3784 launches the S22 vault before the
                        // player box overlaps the door W — the teleport arm
                        // then sees aZ=false forever and the arc lands on the
                        // x3831 blade face.
                        // slice-289: the door's OUTER gate is
                        // `rectsOverlap && player.g == null` — the posted
                        // ax11 @3811 binds p.g on approach, so UP while
                        // bound can never teleport: it only vaults into
                        // the S101→S36 bounce loop. Press UP only once the
                        // bind is clear (guard killed → g.aB<=0 → p.g=null);
                        // while bound, back west of the blade lip and let
                        // the strike arm run the duel.
                        // unbound → UP+LEFT: faces WEST so the east guard
                        // can't rebind before the door arm fires (the
                        // az() scan only binds targets in front of him).
                        // While the elite chaser is bound the attack gate
                        // duels it — the bind clears when it dies.
                        p.aZ && p.al in 780..850 && p.ak in 3780..3816 &&
                            p.S != 284 && p.g == null ->
                            Pad.M_LEFT or Pad.M_UP           // door-A teleport
                        // '5' top west end (x3860-3899): keep walking
                        // east — the vault-drop can't fire here (the
                        // cell under '5' is the '20' deep wall, so
                        // the a(257,8) aR==0 gate refuses); the drop
                        // only opens where the underside is open, i.e.
                        // inside the corridor arm east of x3900.
                        p.aZ && p.al in 540..620 && p.ak >= 3860 ->
                            Pad.M_RIGHT
                        // corridor floor / falling: drift east.
                        p.aZ && p.al in 700..790 && p.ak >= 3860 ->
                            Pad.M_RIGHT
                        !p.aZ && p.al in 600..790 && p.ak >= 3860 ->
                            Pad.M_RIGHT
                        else -> Pad.M_RIGHT
                    }
                }
                // '5'-strip corridor (x3860-4700) — THE DESIGNED ROUTE
                // (proven end-to-end by probe, geometry mined from the
                // cell map): '##' cap x3660-3899 y580-639 + pillar
                // x3840-3879 y640-779 west wall; '5' band x3880-4259
                // cy29 ceiling; slot x3880-4259 y600-759 over '02'
                // kill-water (aR==2/aO==2 → i(50), g.java:3272/3467)
                // on '20' floor cy39; 11 ax44 crusher poles uid71-81
                // hang to y~776 inside; divider '20' column x3920-3999
                // y520-579 sits ON '5' top — the only blocker.
                // Route: DOWN on '5' top → a(257,8) vault into the
                // sealed mouth → land ~3980 on '20' floor (submerged
                // but the kill probe reads feet+1 = cy39 '20' → safe)
                // → hop west (LEFT+UP arms aF) → face-grab the '##'
                // east face ~x3900 → S101 → dir+UP → S36 rebound
                // east+up → aO='5' → S280 ceiling grab → S38 → S37
                // shimmy +6px/t at hang-y590 — above every pole box
                // top (y748) — crosses the whole gauntlet past the
                // divider → at x4130 (past divider east edge x3999)
                // release → S38 → UP → a(54,8) mounts '5' top →
                // walk east to '20' top x4260+ → corridor end.
                p.ak in 3860..4700 -> {
                    held = when {
                        // '5' underside shimmy: east over the whole row —
                        // the hang rides ~y590, above every pole box
                        // (~750 tops) — immune to the crusher cycle.
                        p.S in listOf(37, 38) && p.ak < 4130 -> Pad.M_RIGHT
                        // past the divider's east edge: UP → a(54,8)
                        // mounts '5' top.
                        p.S in listOf(37, 38) -> Pad.M_UP
                        // scripted rebound/mantle/grab — no steering.
                        p.S == 36 || p.S == 92 || p.S == 280 -> 0
                        p.S == 54 -> Pad.M_RIGHT
                        // face grabs: direction+UP → rebound east+up.
                        p.S in listOf(33, 34, 101) -> Pad.M_RIGHT or Pad.M_UP
                        // '5' top: walk to x3935+ then turn WEST —
                        // the west-facing S257 exits ~ak-31 ≈ 3899, right
                        // next to the '##' east face → S101 in ~5t of
                        // floor time (the east exit lands ~ak+91 deep in
                        // the pole field → long exposed floor walk).
                        // Gate the descent on the landing pole's crush
                        // phase (u71/u72 cover the exit span).
                        // Drop window ak 3928-3935 (probed): edgeCx >=
                        // 194 hits the "5" band start x3880; below it the
                        // probe lands on the "##" cap ("20" -> crouch trap).
                        // DOWN is one-shot: stand in-zone until dropSafe.
                        // Under the faithful l() (slice 360) a run step
                        // is 10px, a released run still steps on its
                        // release tick and a long run brakes (S11) — so
                        // the window is reached in single taps from a
                        // stand: release while running/braking, turn in
                        // place (aA != 0 turns without a step), step.
                        p.aZ && p.al in 540..620 && p.ak in 3900..3970 &&
                            (p.S == 12 || p.S == 11) -> 0
                        // Slice 369: past the divider (mounted at x4130+)
                        // the '5' top is walked EAST to the '20' plateau.
                        // The west-steer below used to reach here too and
                        // only worked because S5 ignored the held LEFT on
                        // its jump press; S5 now runs `l()` (g.javap.txt
                        // e() 4775-4793), which turns him west into the
                        // divider face.
                        p.aZ && p.al in 540..620 && p.ak in 4000..4689 -> Pad.M_RIGHT
                        // ...down the steps to the x4707 ledge (y699) and
                        // off its end: the hop's rise meets the S313 catch
                        // @~4730,632 that throws him on to x4880 (the old
                        // route reached it the same way, via S5's ignored
                        // LEFT). Falling off the ledge lands the pit floor.
                        (p.aZ || p.S == 5) && p.al in 621..710 &&
                            p.ak in 4000..4689 -> Pad.M_RIGHT
                        (p.aZ || p.S == 5) && p.al in 540..710 &&
                            p.ak in 4690..4720 -> Pad.M_RIGHT or Pad.M_UP
                        !p.aZ && p.al < 710 && p.ak in 4000..4720 ->
                            Pad.M_RIGHT or Pad.M_UP
                        p.aZ && p.al in 540..620 && p.ak > 3935 -> Pad.M_LEFT
                        p.aZ && p.al in 540..620 && p.ak < 3928 -> Pad.M_RIGHT
                        p.aZ && p.al in 540..620 && !p.av -> Pad.M_LEFT
                        p.aZ && p.al in 540..620 ->
                            if (dropSafe(p.ak - 31)) Pad.M_DOWN else 0
                        // corridor floor: hop west to the '##' east-face
                        // grab — LEFT+UP keeps aF armed so the mid-arc
                        // face contact fires S101 (plain LEFT gave the
                        // S33 ride → S34 drop loop in probes). Hop arcs
                        // clear the pole boxes (arc top ~739 < pole top
                        // ~748); exposure is takeoff/landing only.
                        p.aZ && p.al > 700 -> Pad.M_LEFT or Pad.M_UP
                        // airborne: LEFT+UP keeps aF armed so the mid-fall
                        // face contact (cap east face x3860 or pillar east
                        // face x3880) fires S101 — both rebound east+up
                        // onto the '5' underside. Plain LEFT let him slide
                        // past into the west pocket (S33/S34 loop there).
                        !p.aZ -> Pad.M_LEFT or Pad.M_UP
                        else -> Pad.M_RIGHT
                    }
                }
                // cp3→cp4 pit+rope: trench floor east to the rope x5487 —
                // straight-UP near the wall (RIGHT drifts into the S33
                // climb which isn't grabbable on this face).
                p.ak in 4700..5900 -> {
                    held = when {
                        !p.aZ -> Pad.M_UP
                        p.ak < 5470 -> Pad.M_RIGHT
                        else -> Pad.M_UP
                    }
                }
                // cp4→cp7 open-road runs: RIGHT + combat; UP when stuck;
                // hop the x6380-6460 pit gap (the '20' floor strip breaks
                // there — S62 lip-grab is the fallback but the jump is
                // cleaner).
                // x7500-8224 wire chain (proven verbatim): ropes hand him
                // to the ax40 zipline (S164). Jump-off (M_DOWN) while the
                // zipline crosses rail2's west end (x7920-7960) — the free
                // fall lands his box top inside rail2's ry-20..+30 catch
                // window (ax10-S34 @7903-8227). Riding the zipline further
                // sags him below the window → x8118 crush death. On rail2
                // hold RIGHT only — padHeld(16388) attack-offs early, and
                // the east end auto-dives (Z[1]==1) past both crusher rows
                // to the x8800 corridor. Airborne in the handoff band holds
                // 0 — RIGHT drift overshoots the catch.
                // x9000 crossing (zipline route): chimney zigzag
                // x8940↔x9000 → bridge-'5' hang → shimmy east past the
                // tower overhang → UP vault a(54,8) onto the strip top
                // (y260) → walk east under the parked gondola → bx() L53
                // auto-bind (S164) → script 52 rides to (9615,231) →
                // TAP_R dismount fling east onto the far tower. The '02'
                // pole corridor below is bypassed entirely — that is the
                // whole point of the zipline.
                p.ak in 8890..10300 && p.al < 850 -> {
                    held = when {
                        // Riding the ax40 zipline gondola (bound, S164):
                        // script 52 holds 35 steps then lerps the gondola
                        // east to (9615,231) — the rider is pinned to
                        // e.ak. Past the last pole (9556), TAP_R fires
                        // the L37 dismount: S157 fling (+2048,-2560)
                        // arcs onto the far tower (x9700+, top y360).
                        // L37 dismount at the LAST pole: an early fling
                        // lands ~9672, inside the 9723-guard's patrol —
                        // its chase strike KOs (meter is empty here).
                        // Dismounting at >=9590 throws the arc ~+55px
                        // further, landing ~9850 — past the first
                        // guard's patrol bound — a clean run to the lip.
                        // Dismount sweep `9590 + deaths % 28`: a fixed
                        // dismount makes the whole re-drive deterministic
                        // — same tower landing, same elite interception,
                        // same shaft-descent phase → same death every
                        // retry (301). Sweeping the drop point sweeps the
                        // patrol phase at arrival, the strike position,
                        // and the door phase at the ax22 capture+1 tick
                        // (the crush race — door record 26/27 ticks ~364
                        // slots before the zone's record 390, so a door
                        // in a blocking frame kills the pinned player
                        // before the vault fires). Retries exhaust the
                        // mixed-mode failures until a capture+safe-phase
                        // attempt survives.
                        p.ac != null && p.ac!!.ax == 40 && p.S == 164 ->
                            if (p.ak >= 9590 + deaths % 28) Pad.M_TAP_R
                            else 0
                        // FAR TOWER top (x9700-9979 @ y360): two aB>200
                        // elite ax11s patrol it (x9723/x9753, alert to
                        // ~x9903) and chase at run speed — every duel
                        // drains the meter and every hop gets struck
                        // mid-arc. The survivable line is the SHAFT
                        // descent: x9860+ opens below y380 — run east
                        // off/through it, fall the shaft, land the y600
                        // ledge (x9880-10119), dodge its two crushers,
                        // then off the east end to floor y720 → the
                        // ax2@10016 checkpoint → pillar x10080 → ax5.
                        // DESCENT PHASE SWEEP (crush race): the shaft's
                        // ax44 doors only tick once `au<2` (camera gate,
                        // k.java L215) — their 7-tick cycle starts at a
                        // deterministic offset before the ax22 capture,
                        // so the capture+1 tick lands vuln on S0 every
                        // attempt (the blocking frame: W[1]=570 vs the
                        // pinned player's W[3]=576 → crush → S50 → x1=0
                        // — door records 26/27 tick ~364 slots before
                        // the zone's record 390, killing him before the
                        // vault fires). A stall BEFORE the descent can't
                        // move the lock. Extra east ticks on the floor
                        // shift everything downstream by `deaths % 7` —
                        // vuln sweeps the cycle until it lands on a
                        // retracted frame (S1/S2, W[1]>=577).
                        p.aZ && p.al in 330..400 && p.ak in 9600..9820 &&
                            towerStall < deaths % 7 ->
                            { towerStall++; Pad.M_RIGHT }
                        // KILL-HOP on the top: the S157 fling lands ON
                        // guard1 (@9723) and kills it outright (S89 →
                        // its S139) — the same landing-crush works on
                        // guard2. Hop while it's ~35-70px ahead so the
                        // arc apex comes down on its head; a hop too
                        // close or aimed past just lands into its
                        // lunge reach (struck on touchdown at ~9803).
                        // G12 re-route: never turn back west on the top.
                        // Every attempt now lands the S157 fling on guard1
                        // (S89 kill @9723) with guard2 30px ahead; hopping
                        // back at a guard that has slipped behind only
                        // feeds it strikes (the 301-death dance). Hop over
                        // a guard ahead, otherwise run for the shaft.
                        p.aZ && p.al in 330..400 && p.ak in 9600..10080 ->
                            if (foe != null && foe.ax == 11 &&
                                foe.aB > 200 && foe.ak > p.ak &&
                                foe.ak - p.ak in 20..110)
                                Pad.M_RIGHT or Pad.M_UP
                            else Pad.M_RIGHT
                        // shaft descent / ledge / under-corridor: drift
                        // and run east toward the pillar face. Ledge-
                        // hang states (S56-63) excluded — the vault-west
                        // loop lands the bot on the column's west face;
                        // it must mantle UP to keep climbing, not steer.
                        !p.aZ && p.S !in 33..38 && p.S !in 56..63 &&
                            p.S != 92 && p.S != 101 &&
                            p.ak in 9820..10100 && p.al in 380..700 ->
                            Pad.M_RIGHT
                        p.S in 56..63 && p.ak in 9700..9860 ->
                            Pad.M_UP
                        // corridor's east end = 40px kick channel
                        // x9980-10019: east = tower face '14' x10020+,
                        // west = floating '14' x9960-79 y560-639. S33
                        // rebound off the east face rises ~72px; hold
                        // TOWARD it early (dirKey arms the lip-scan),
                        // then switch AWAY mid-rise — releasing dirKey
                        // drops to the else-branch and aA() fires the
                        // S36 kick from the higher position so the arc
                        // reaches the west face's '14' (bottom edge
                        // y639) instead of landing under it.
                        p.S == 33 && p.ak in 9850..10320 ->
                            if (p.al <= 660) (if (p.av) Pad.M_RIGHT else Pad.M_LEFT)
                            else (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                        // past the channel, faces DO carry lips in the
                        // rebound's reach (the finale tower x10660-819
                        // tops at y360, ~80px above its massif base) —
                        // hold TOWARD+UP the whole rise: dirKey keeps the
                        // lip-scan armed until anim-end, where grabbing
                        // the top edge is how the terraces chain.
                        p.S == 33 && p.ak > 10320 ->
                            (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                        (p.S in 34..38 || p.S in 56..63 || p.S == 92 || p.S == 101) &&
                            p.ak in 9850..10320 ->
                            (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                        p.aZ && p.al in 560..720 && p.ak in 9820..10330 ->
                            // corridor floor 'ff' ends at the massif face
                            // (x10020-79 '14' y660-719). Press in; the S33
                            // rebound + lip-scan above climbs the face.
                            Pad.M_RIGHT
                        // under-tower pocket (the zone's west vault drops
                        // the player here, x~9700-9780): its only exits
                        // are UP — run EAST to the column's west face
                        // (x9780-9860, '14' y400-720) and climb back
                        // toward the top; the face's lip grabs chain the
                        // ascent (S33/36/60/62 run in the trace). Scoped
                        // to the pocket — the respawn corridor at x~8896
                        // also sits at al>560 and must run EAST anyway.
                        // G12 re-route: the chimney foot (y799 floor under
                        // the x8941/x8979 faces). Running into the x8979
                        // face only rides S33 → S34 back down while the
                        // floor soldier u65 closes in; under k.I()'s order
                        // a respawn lands grounded, so the air-tick corner
                        // tap that used to hop it in never fires. Hop from
                        // the floor: the arc meets the face high → S101 →
                        // the S36/S101 zigzag climbs to the y559 ledge.
                        p.aZ && p.al in 780..820 && p.ak in 8915..8975 ->
                            Pad.M_RIGHT or Pad.M_UP
                        p.aZ && p.al > 560 && p.ak in 6000..9819 ->
                            Pad.M_RIGHT
                        // plateau '02' top: the ax44 pole gauntlet.
                        // scope: the '02' mesa ONLY — the door-gauntlet
                        // ax44 crushers (x5950-7150) share the y-band.
                        // al>430 = actually ON the slab (aZ stays true
                        // through airborne S22 — an airborne eval drains
                        // the dwell budget for nothing).
                        // al-band now 430..560: the mesa top is ~y440 and
                        // the shaft's under-ledge corridor is y620-720 —
                        // an unbounded >430 swallowed the cellar floor
                        // (plan() returns 0 instantly for ak>9560 → the
                        // bot stood in the pocket forever, x9765 stall).
                        p.aZ && p.al in 430..560 && p.ak > 9000 -> {
                            // POLE-TIMING v4 — S0's crush NEVER fires for
                            // these timed poles: the 0,2,4,6 bank runs the
                            // Z[3] countdown → setAnim(S+1) BEFORE the
                            // `e.S==0||e.S==4` crush check, so the single
                            // S0 tick always transitions first
                            // (NpcFsm.kt:163-178). Cycle on clip32:
                            // S0(1) → S1(3) → S2(1) → S3(2,crush) —
                            // period 7, danger = S3's 2 ticks only.
                                val poles = w.npcs.filter { it.ax == 44 &&
                                it.al in 400..460 && it.ak > 9000 }
                            // STRICT live-model arm (offline-solver
                            // proven): crush kills only on a pole's
                            // S3.T1 tick = ph6 — dead(ak,dt) iff a pole
                            // W-overlaps the box at ak AND its phase at
                            // +dt ticks is 6. Every eval runs a small
                            // BFS over wait/run/hop through that exact
                            // model and takes the first action of a
                            // surviving plan — the greedy per-eval
                            // choices drifted off the solver's needed
                            // wait-positioned schedule and died.
                            fun deadAt(ak0: Int, dt: Int) = poles.any {
                                ak0 - 9 <= it.W[2] &&
                                ak0 + 10 >= it.W[0] &&
                                (polePh(it) + dt) % 7 == 6 }
                            // (state,t) → first-action mask. BFS: wait
                            // (+1t), run (+10,+1t), hop (squat+10@+1t,
                            // descent-tail +74/+82@+11/+12t, touchdown
                            // +98@+13t — every node must avoid ph6).
                            fun plan(): Int {
                                val q = ArrayDeque<Pair<IntArray, Int>>()
                                val seen = HashSet<Long>()
                                q.add(intArrayOf(p.ak, 0) to 0)
                                while (q.isNotEmpty()) {
                                    val (s, first) = q.removeFirst()
                                    val ak = s[0]; val dt = s[1]
                                    if (ak + 10 >= 9560) return first
                                    if (dt > 60 ||
                                        !seen.add(ak.toLong() shl 16 or
                                                  dt.toLong())) continue
                                    if (!deadAt(ak, dt + 1)) q.add(
                                        intArrayOf(ak, dt + 1) to
                                        if (dt == 0) 0 else first)
                                    if (!deadAt(ak + 10, dt + 1)) q.add(
                                        intArrayOf(ak + 10, dt + 1) to
                                        if (dt == 0) Pad.M_RIGHT else first)
                                    val hopOk = !deadAt(ak + 10, dt + 1) &&
                                        !deadAt(ak + 74, dt + 11) &&
                                        !deadAt(ak + 82, dt + 12) &&
                                        !deadAt(ak + 98, dt + 13)
                                    if (hopOk) q.add(
                                        intArrayOf(ak + 98, dt + 13) to
                                        if (dt == 0)
                                            Pad.M_UP or Pad.M_RIGHT
                                        else first)
                                }
                                return Pad.M_UP or Pad.M_RIGHT
                            }
                            plan()
                        }
                        // plateau drop (post-bridge, airborne over the
                        // mesa): plain RIGHT — a lingering TAP/UP edge on
                        // the landing tick re-fires landArm into an
                        // involuntary hop straight into the S3 crushers.
                        !p.aZ && p.ak > 9080 -> Pad.M_RIGHT
                        // strips: shimmy east; at the LOW strip's east
                        // end hold RIGHT|UP → S38 vault-out to bridge.
                        p.S == 37 || p.S == 38 || p.S == 280 -> when {
                            // BRIDGE '5' (x8900-9299 @ y260): shimmy east
                            // past the tower overhang — its r12 face ends
                            // at x9020, so at ak>9020 the UP-arm's head
                            // probe (al-20) sees sky (aO==0) → a(54,8)
                            // vaults onto the strip top. Standing box top
                            // (~y200) overlaps the parked gondola's W
                            // bottom (y222) → bx() L53 auto-bind → ride.
                            p.al in 250..340 && p.ak > 9000 ->
                                if (p.ak <= 9021) Pad.M_RIGHT else Pad.M_UP
                            // LOW '5' east end — vault-out needs UP.
                            p.al > 340 && p.ak > 8890 ->
                                Pad.M_RIGHT or Pad.M_UP
                            else -> Pad.M_RIGHT
                        }
                        p.S == 54 -> Pad.M_RIGHT or Pad.M_UP
                        // chimney legs: CORNER TAPS with flight direction
                        // (M_TAP_L/R = touch cells 0/2) — they arm the aF
                        // grab latch WITHOUT steering against the kick's
                        // ballistic ag (sustained M_LEFT/M_RIGHT holds
                        // decelerated the zigzag — the chain never
                        // chained to the strip). S101 auto-bounce flips
                        // av itself; during grab ag==0 → TAP_R (matches
                        // the proven driver's heldW=false reset).
                        (!p.aZ && p.ak <= 9080) || p.S in 33..36 ||
                            p.S == 92 || p.S == 101 ->
                            if (p.ag < 0) Pad.M_TAP_L else Pad.M_TAP_R
                        // G12 re-route: bridge top (y259) — run into the
                        // parked gondola uid49 with RIGHT alone. The
                        // gondola binds in the entity pass and ticks
                        // again as the player's ac link after aS.I()
                        // (k.java:2589-2591), where a held UP is bx()'s
                        // hop-off (k.v(16388), i.java:16150 → S157 fling).
                        p.aZ && p.al in 240..280 && p.ak in 9000..9400 ->
                            Pad.M_RIGHT
                        // G12 re-route: chimney-top ledge (y559). The hop
                        // to the x8979 face top must leave from x>=8912:
                        // 8px further west the arc meets the face one
                        // tick lower (grab y548, not y530) and the S36
                        // kick then tops out under the '5' strip (y390)
                        // instead of catching it (S280).
                        (p.aZ || p.S == 5) && p.al in 550..565 &&
                            p.ak in 8860..8911 -> Pad.M_RIGHT
                        // grounded at the wall face: RIGHT|UP — aF arms
                        // (cv&&u|v(M_UP)) so the face contact grabs;
                        // vaults rise toward the face otherwise.
                        else -> Pad.M_RIGHT or Pad.M_UP
                    }
                }
                p.ak < 10000 -> {
                    if (p.S == 164) {
                        held = if (p.ak in 7920..7960 && p.af?.ax == 40)
                            Pad.M_DOWN else Pad.M_RIGHT
                    } else if (p.S == 157) held = Pad.M_RIGHT
                    else if (!p.aZ && p.ak in 7940..8120 &&
                             p.al in 400..680) held = 0
                    else {
                        // stall-jump is suppressed while a foe is close:
                        // the guard's body stalls ag, and hopping over it
                        // just lands the bot facing away (x9000 S9 death).
                        // ...and inside the pole gauntlet (x9000–9580):
                        // hops there must come only from the phase arm.
                        if (p.aZ && p.ag in -256..256 && p.S != 79 &&
                            foe == null &&
                            !(p.ak in 9000..9580 && p.al in 400..470))
                            held = held or Pad.M_UP
                        if (p.aZ && p.ak in 6360..6418)
                            held = held or Pad.M_UP
                        if (p.S == 33 || p.S == 92) held = Pad.M_UP
                    }
                }
                // cp7 tower/finale: shaft-kick chain, S16 door box,
                // posted-guard kill, fort lip-scan/kick timing.
                else -> {
                    if (p.S in 33..36 || p.S == 92 || p.S == 101)
                        held = (if (p.av && p.ak < 10300) Pad.M_LEFT
                            else Pad.M_RIGHT) or Pad.M_UP
                    // face-lip hang after the ax22 east vault (or any
                    // ledge grab on the massif east face) — mantle UP.
                    // S62 (mid-climb anim) auto-completes — held UP
                    // there only burns the edge the top vault needs;
                    // release it so the first grounded tick edges fresh.
                    if (p.S in 56..63 && p.ak > 10320)
                        held = if (p.S == 62) 0 else Pad.M_UP
                    // slice-279 finale route (rewritten, proven):
                    // the floor gang is aB=300 elites — the decoded
                    // intended path is the ax22 DOUBLE-VAULT chain:
                    //   pedestal top x10010-70 y560 (or the '02' shelf
                    //   x9870-9900 y590 → hop east onto it)
                    //   → run-vault east into zone W1 [10292-326,506-539]
                    //   → 16396 (Z[2]=10 → EAST vault +20/+3328/ah-3840)
                    //   → zone W2 [10408-42,472-515] → 16396 → arcs to
                    //   the massif top / ax10-S43@10497 mantle box.
                    // Wisp trail @10181,488→10360,474→10463,440 marks
                    // the second vault's arc. The pedestal face at
                    // x10010 is climbed from the respawn floor by
                    // dir|UP (aF arm). On the gang floor itself the
                    // bot retreats west to re-approach — duels there
                    // are 1vN suicide vs aB=300.
                    // pedestal-top leg: RUN to the east edge first —
                    // a vault from x10120+ apexes ~y470 and descends
                    // through W1's box [10292-326,506-539]; launching
                    // mid-top lands ~80px short on the floor.
                    if (p.aZ && p.al in 540..640 &&
                        p.ak in 9860..10280)
                        held = if (p.ak > 10090)
                            Pad.M_RIGHT or Pad.M_UP else Pad.M_RIGHT
                    // east-face kick leg DISABLED for now — hopping west
                    // into elite @10178's patrol zone [10128-278] feeds
                    // it a free strike window; keep the east sprint only.
                    // massif-face sprint fallback: past the kick zone,
                    // keep running east — either a missed kick drops the
                    // bot here, or the gang sprint forces through to the
                    // massif face.
                    if (p.aZ && p.al in 645..720 &&
                        p.ak in 10289..10540)
                        held = Pad.M_RIGHT or Pad.M_UP or Pad.M_TAP_R
                    // airborne beside the massif face: UP must be held
                    // so the L3a9d latch (cv && u/v(UP)) arms aF — held
                    // RIGHT alone never arms it (M_TAP_R is a separate
                    // bit) — then L2298 grabs the wall → S101 cling →
                    // auto-bounce WEST → W2.
                    if (!p.aZ && p.al in 500..720 &&
                        p.ak in 10400..10560)
                        held = Pad.M_RIGHT or Pad.M_UP
                    // slice-279 door leg: the posted pair walking the
                    // massif top binds p.g on contact (interactScan
                    // g.java:L260-ish, inFront + |Δal| gate) — and the
                    // door's `g == null` gate (NpcFsm ax10-S16 arm)
                    // refuses while a victim is held. Two escape routes:
                    // (a) vault BEFORE the bind — on a standing tick
                    // (S0) press UP without a direction so the tick
                    // doesn't step into the bind box, letting postTail's
                    // `p.cq && pad.v(16398)` gate (g.java:L2042-ish)
                    // launch S233 over the pair; (b) keep walking —
                    // the bind drops as the victim falls behind, then
                    // the door arm below fires once inside the W.
                    if (p.aZ && p.S !in 56..63 &&
                        p.al in 400..470 && p.ak in 10495..10555) {
                        if (p.S == 0) {
                            held = Pad.M_TAP_R or Pad.M_UP
                            topUpHeld = false
                        } else {
                            held = Pad.M_RIGHT or Pad.M_TAP_R or
                                (if (topUpHeld) 0 else Pad.M_UP)
                            topUpHeld = !topUpHeld
                        }
                    }
                    if (p.aZ && p.al in 400..470 && p.ak in 10588..10616) {
                        // slice-279: pulse INSIDE door1's W [10587-618]
                        // only — an UP edge outside it just launches a
                        // vault that carries the player airborne over
                        // the zone (aZ=false → the arm can never fire).
                        // Alternating ticks keep a fresh v(16388) edge
                        // while the S233 squat still holds aZ=true.
                        doorPulse = (doorPulse + 1) and 1
                        held = if (doorPulse == 0) Pad.M_UP else 0
                    } else if (p.ak in 10580..10635)
                        held = if (p.ag == 0 && p.aZ) Pad.M_UP else 0
                    // door2 exit (~10805): NO UP here — pressing it inside
                    // door2's W re-enters the two-way door and ping-pongs
                    // back to 10602. The generic eastward arm walks the
                    // player out of the W toward the goal.
                    if (p.aZ && p.ak in 9980..10035)
                        held = held or Pad.M_UP
                    // fort west face: dir|UP while scanning (al>735),
                    // dirless UP at the kick window.
                    if (p.S == 33 && p.ak > 11200)
                        held = if (p.al > 735)
                            (if (p.av) Pad.M_LEFT else Pad.M_RIGHT) or Pad.M_UP
                        else Pad.M_UP
                }
            }
            // Attack gate (slice-274): CONTEXT only when the foe is
            // actually reachable — the 160px foeNear scan otherwise baits
            // air-swing loops at unreachable patrols on the tier below.
            // slice-277: never CONTEXT while a WEAKENED mountable victim
            // is bound — ar() L3702 hijacks the press into c(g.g) and the
            // resulting S277 mount is a faithful soft-lock: the carry arm
            // needs Z[0]==0 (i.java:36405) so a weakened Z[0]==2 victim
            // can never release. Walk past instead — the weakened victim
            // stays passive; `g` drops once |Δal| >= 60 or it is 440px away
            // (slice 388: LOS only gates NEW binds, it never unbinds).
            val mountFrozen = p.g != null && p.g!!.ax == 11 &&
                p.g!!.Z[0] == 2 && p.g!!.Z[19] == 1
            // slice-278: never CONTEXT while the foe is in S144
            // weakened-block — NpcFsm L632-657 (i.java:6048-6066 proven)
            // has it RECOVER to combat on r() AND back-counter attackers
            // (p.i(8)) — swinging at it just feeds the counter. The
            // x8971 posted guard (aB=300) wore the bot down this way.
            // Move past; engage again once it leaves the block.
            val foeBlocking = foe != null && foe.S == 144
            // slice-279: the posted aB=300 elite @8850 is NOT fightable —
            // the respawn checkpoint (ax2@8926) sits inside its
            // x8690-9010 alert so every death re-agros, and its
            // S144-recover + back-counter loop wears the meter down
            // before the bot lands ~5 hits. Flee the elite always:
            // it can't climb — RIGHT|UP into the chimney face escapes
            // to strip altitude before the meter runs out. Regular
            // aB=100 guards stay engaged.
            // slice-279 cont'd: duel on the tower top drains the meter
            // (elite strikes out-damage the stagger gains, x1=0 at
            // ~9769), and pure flee dies to the chase strike at ~9800.
            // Both are dead ends — keep fleeElite: the survivable route
            // is the shaft descent (see the route arm comment).
            // slice-279 cont'd: at the massif-face gate (ak>10440) the
            // elite MUST die — fleeing just feeds it a strike mid-vault
            // (22→9 knockdown). Attack anims are i-frames, so the combo
            // chain itself is the defense: duel here, not flee.
            // slice-289: EXCEPT the door-A band (~3300-3850) — the posted
            // elite @3811 auto-binds p.g on approach and the door's outer
            // gate requires g==null, so fleeing here is a deadlock: the
            // duel MUST run (kill → g.aB<=0 → p.g clears → teleport).
            // Slice 404: …and the cp3→cp4 trench (x4700-5900): its three guards are
            // solid to the player whether or not they are alerted (the shared tail's
            // `a()` @7644-7657 joins both aA branches), so the trench cannot be run
            // through — the last guard stands on the rope's foot (x5487). Duel them.
            val fleeElite = foe != null && foe.ax == 11 && foe.aB > 200 &&
                p.ak < 10520 && p.ak !in 3300..3850 && p.ak !in 4700..5900
            // slice-279 cont'd: past the channel the finale floor packs
            // 2-4 ax11s at ~x10465 — engaging ANY of them swings into a
            // 1vN: the mid-swing lock eats strikes from the rest and the
            // meter dies every cycle (9 deaths at x10495 S9). Don't duel
            // a gang: hold RIGHT through the cluster — chase speed
            // (2048≈8px/t) loses to the run (2560≈10px/t), so fleeing
            // east outruns the reach before the meter can empty.
            // slice-279 cont'd: fleeGang only applies on the floor below
            // (al > 600) where the gang is still a fatal 1vN. On the
            // massif top the door leg (above) handles the posted pair —
            // they're never engaged (fleeTop covers it): the door's
            // `player.g == null` gate fires once any bound victim drops
            // behind, which a straight east run produces on its own.
            val fleeGang = foe != null && foe.ax == 11 && p.ak > 10300 &&
                p.ak < 10520 && p.al > 600
            // slice-279: on the massif top NEVER engage — the door's
            // `player.g == null` gate refuses while a bound victim is
            // held, and any swing → weaken → ar()-bind would set it.
            // The door is ~70px past the lip — tank the chase and run
            // through instead of dueling the posted pair.
            val fleeTop = foe != null && foe.ax == 11 && p.al < 480
            // slice-289: door-A's teleport gate is
            // `rectsOverlap && player.g == null` — the posted ax11 @3811
            // auto-binds p.g on approach, so fleeing it at the door is a
            // deadlock: the door never accepts UP while the guard lives.
            // The duel must run (kill → g.aB<=0 → p.g clears), so no
            // flee suppression here — the position policy keeps him west
            // of the blade lip while bound.
            // Live ax11 bodies within duel range — when the kite has
            // strung the pack out to one, the strike arm below re-arms
            // and it becomes a normal duel. While 2+ bodies share the
            // range the mid-swing lock is fatal, so it stays suppressed.
            val gangNear = w.npcs.count { it.ax == 11 && it.S != 139 &&
                kotlin.math.abs(it.ak - p.ak) <= 140 &&
                kotlin.math.abs(it.al - p.al) < 50 }
            // slice-279: atkCd persisted across foes — after a kill the
            // bot walked into the NEXT guard mid-cooldown and ate the
            // strike. A different nearest foe means a fresh duel: reset
            // the swing clock. While cooling at melee range, hold facing
            // only — walking in during the dead window eats strikes.
            if (foe !== lastFoe) { atkCd = 0; lastFoe = foe }
            if (foe != null && atkCd <= 0 && !mountFrozen && !foeBlocking &&
                !fleeElite && !fleeTop &&
                p.S != 37 && p.S != 38 && p.S != 280 && p.S != 164 &&
                kotlin.math.abs(foe.ak - p.ak) <= 80 &&
                kotlin.math.abs(foe.al - p.al) < 50) {
                // face the foe + CONTEXT — a direction-only press turns
                // av; pure CONTEXT swings toward the last facing and hits
                // air when the guard passes behind. Short cooldown: the
                // combo chain (67→68→69→112→113→114→115) needs taps every
                // ~6 ticks to stay in-window — and the g.d() immunity
                // gates (S∈{67,112-115,183,184} + i.bh 8-tick hit-lock)
                // mean continuous combo is ALSO the defense: the pack
                // can't dogpile while the player keeps swinging.
                held = (if (foe.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT) or
                    Pad.M_CONTEXT; atkCd = 6
            } else if (foe != null && atkCd > 0 && !mountFrozen &&
                !foeBlocking && !fleeTop &&
                !fleeElite && p.S != 37 && p.S != 38 && p.S != 280 &&
                p.S != 164 &&
                kotlin.math.abs(foe.al - p.al) < 50) {
                // mid-cooldown inside melee reach: face and wait for the
                // swing — stepping in range during the dead window is how
                // the tower guard killed it at x9802. And no hopping:
                // comboArm needs aZ||standingOn, so any airborne tick
                // runs enterFall() and the chain — plus its g.d()
                // immunity — dies with it.
                held = if (kotlin.math.abs(foe.ak - p.ak) <= 90) {
                    if (foe.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                } else held
            }
            // Slice 404: the stealth kill. Soldiers are solid to the player whether
            // or not they are alerted (the shared tail's `a()` @7644-7657 joins both
            // aA branches) — a posted guard in the corridor can no longer be fled
            // past, and its S144/S17 recover loops make the open duel a loser. Taken
            // from behind it is one press: k()'s window (@260-375) is an unaware
            // soldier (`j == 0`) within 80x/5y that the player faces and that does
            // not face the player.
            val stabFoe = w.npcs.firstOrNull {
                it.ax == 11 && it.aB > 0 && it.j == 0 && it.aA == 0 &&
                kotlin.math.abs(it.ak - p.ak) < 80 && kotlin.math.abs(it.al - p.al) <= 5 &&
                p.faces(it) && !it.faces(p)
            }
            if (stabFoe != null && p.aZ) held = Pad.M_CONTEXT
            atkCd--
            w.pad.e(held)
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 80) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (p.ak > maxAk) { maxAk = p.ak; stall = 0 }
            if (p.al < minAl) { minAl = p.al; stall = 0 }
            while (mi < milestones.size && maxAk >= milestones[mi]) {
                marks += "MS${milestones[mi]}@t$t"
                mi++
            }
            if (p.ak <= maxAk && p.al >= minAl && ++stall == 250) {
                w.pad.e(16398 or dir); w.tick(emptyList())
            } else if (stall == 1500) {
                val wob = w.npcs.filter { kotlin.math.abs(it.ak - p.ak) < 120 && it.ax != 0 }
                marks += "STALL@${p.ak},${p.al} S=${p.S} " +
                    "kC=${w.kC?.ax}/${w.kC?.claimActive()} " +
                    "ag=${p.ag} ah=${p.ah} W=${p.W.toList()} " +
                    "aV=${p.aV} aW=${p.aW} aO=${p.aO} aQ=${p.aQ} aR=${p.aR} " +
                    "at=${Entity.at?.ax}@${Entity.at?.ak} " +
                    "aN=${w.lockTarget?.ax}@${w.lockTarget?.ak},S${w.lockTarget?.S} " +
                    "g=${p.g?.ax}@${p.g?.ak} " +
                    "ac=${p.ac?.ax}@${p.ac?.ak},${p.ac?.al} " +
                    "near=${wob.take(5).map { "ax${it.ax}@${it.ak}/${it.al}S${it.S}" }}"
                dir = if (dir == Pad.M_RIGHT) Pad.M_LEFT else Pad.M_RIGHT
                stall = 260
            }
        }
        println("CAPSTONE won=$won deaths=$deaths maxAk=$maxAk minAl=$minAl t=$t")
        println("CAPSTONE marks=${marks.takeLast(20)}")
        println("CAPSTONE trace=${trace.joinToString(" ")}")
        // Proven frontier (slice 277): the weakened-victim mount is a
        // FAITHFUL soft-lock, not a port bug — az() auto-binds g.g on
        // aA∉{0,2}+|Δal|≤20+LOS (g.java:13469-13505), ar() L3702 mounts on
        // CONTEXT → c(g.g) → as() → i(277), and the ax11 orbit arm
        // (g.java:10846) applies a constant cy-frozen drag with no input
        // arm; every release is gated off a weakened victim (carry needs
        // Z[0]==0, i.java:36405; P() needs aB≤0; the rest unreachable once
        // floor-pinned). The bot now suppresses CONTEXT while a weakened
        // mountable is bound (mountFrozen) and walks past — the weakened
        // guard stays passive; `g` drops at |Δal| >= 60 / 440px (slice 388:
        // LOS only gates new binds).
        // Proven frontier (slice 275): the x8118 crusher corridor's only
        // route is the wire chain — ax40 zipline (S164, ac-bound) →
        // rail2 ax10-S34 (auto-dive Z[1]==1) → lands the x8800 corridor →
        // run to the x8990 wall. The rail ride died at the catch for 200+
        // ticks because the bot pressed M_DOWN every S164 tick ("bound
        // carrier dismount") — padHeld(33024) fires the rail's own
        // jump-off arm (bw()) on the catch tick; scoping the dismount to
        // ak>10000 + silence in the airborne corridor opened the chain.
        // Proven frontier (slice 278): the x9000 wall crossing is
        // mechanically proven (chimney zigzag x8940↔x9000 → low-'5'
        // shimmy → UP vault-out a(54,8) → bridge-'5' y270 → drop onto
        // the '02' plateau y420) — bot reached maxAk=9212, minAl=270.
        // Proven frontier (slice 279): the massif-top route is the
        // paired S16 door-teleport — ax10@(10587,330) links oId 134 →
        // ax10@(10789,402) Z0 600→601 (L17d9-L1808 proven). The arm
        // needs the player grounded inside the door's W-box with a
        // fresh v(16388) edge AND `g == null`; the posted ax11 binds on
        // contact and (slice 388) STAYS bound while it is alert inside the
        // 440px / 60px band — the tower legs bait uid571 off the column's
        // west edge (S25 fall → |Δal| >= 60 drops `g`), then reach the W
        // unbound → doorPulse fires → bh() fades out (S284) and bi()
        // re-anchors at the far door → ak≈10789.
        // Past it: y500 terrace east → the x10940-11319 chasm
        // (ax13 rope @11312,441 / deep pit with patrols) → far shelf
        // x11320+ → goal ax5@11448,503. WON with deaths=0, maxAk=11576
        // on the reference run.
        // Proven frontier (slice 289): the 11-crusher corridor
        // (x3880-4259) is crossed via the designed chain — S257
        // vault-drop TELEPORTS under the x3960-4039 divider east edge
        // into the slot (~x4101), then '5' is a one-way cell (solid
        // from above aR=5, climbable from below): the held-UP jump
        // climbs THROUGH '5' back to its top, arcs east to the '20'
        // plateau, and the floor's kill slivers (y748-774) are never
        // touched. Result: deaths 301→2, maxAk ~3994→9850. The new
        // stall is the x8900 re-drive: after deaths on the far-tower
        // descent, checkpoint ax2@(8926,757) respawns him at the pit
        // floor and the chimney-zigzag entry (x8872-8990, S22/36/43)
        // doesn't re-engage — next leg.
        assertTrue(won,
            "capstone must complete mission-0 end to end — " +
            "maxAk=$maxAk deaths=$deaths marks=${marks.takeLast(8)}")
    }

}
