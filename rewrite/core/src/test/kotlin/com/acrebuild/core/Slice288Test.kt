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

class Slice288Test {
    /**
     * Mission-4 (pack-10, bh3 flying canyon — second vertical ESCAPE)
     * capstone bot. Same machinery as mission-1 (ax25 flyer, conveyor leash,
     * ax24-S19 shrines, ax10-S10 perch gates, ax10-S31 claim-QTE), different
     * geometry: FOUR QTE gates (aw326/330/332 mid-shaft → gate scripts
     * 325/329/331; aw338 top → aA=337) instead of one.
     *
     * Route (bottom→top, y decreasing):
     *   perch aw328 W[285,10246,603,10296] → cp 10137 → shrine (476,9560)
     *   → gate aw326 (y8842, lane M_TAP_L=2) → gate aw330 (y8389, lane
     *   M_TAP_R=8) → gate aw332 (y8031, lane M_TAP_L=2) → cp 7894
     *   → shrine (553,6199) → cp 5146 → shrine (241,4281)
     *   → perch aw324 W[99,3804,410,3859] → top claim aw338 (y2384,
     *   lane M_UP=16388, aA=337) → top arena ~(583,1350).
     *
     * Lane decode (proven, Entity.kt:3354 CS + NpcFsm.kt:1828-1918): the
     * zone's armed lane type is `Z[1] shr ((3-r9)<<2) and 15`; for these
     * single-lane records Z[1] itself is the lane type → press CS[Z[1]&15].
     */
    private val m4Cs = intArrayOf(1, 2, 16388, 8, 4112, 65568, 8256, 128, 33024, 512)

    @Test
    fun `capstone mission-4 full-shaft climb to claim-QTE win`() {
        val w = world(aj = 4)
        settleIntro(w)
        val p = w.player
        val route = listOf(
            Triple(444, 10270, "perch"),
            Triple(476, 9560, "shrine1"),
            Triple(417, 8900, "gate"),
            Triple(514, 8450, "gate"),
            Triple(472, 8090, "gate"),
            Triple(553, 6199, "shrine2"),
            Triple(241, 4281, "shrine3"),
            Triple(255, 3850, "perch"),
            Triple(594, 2450, "claim"),
            Triple(583, 1350, "top"),
        )
        var leg = 0; var t = 0; var won = false
        var prevCamY = w.kP; var stallT = 0; var prevS = p.S
        var minAl = p.al
        var dodgeT = 0; var lastShots = 0
        var armedSeen = 0; var firedN = 0
        while (t++ < 120000) {
            when {
                w.missionWon -> { won = true; break }
                w.jC == 12 || w.jC == 13 -> {
                    var guard = 0
                    while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                        w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                        w.tick(emptyList())
                    }
                    // The respawn puts the player back at the last
                    // checkpoint, below legs already flown: resume the
                    // route at the first leg above it. (Under G12 the
                    // bot reaches the top claim on x1 5 and dies there;
                    // left in "claim" mode it never re-bound the aw324
                    // perch and fell behind the camera on every retry.)
                    val back = route.indexOfFirst { p.al > it.second - 60 }
                    if (back in 0 until leg) leg = back
                    continue
                }
                w.jC == 21 -> {
                    if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }
                    w.tick(emptyList()); continue
                }
                w.jC != 8 -> {
                    if (w.jG >= 10 && t % 40 == 0) w.pad.e(Pad.M_CYCLE)
                    w.tick(emptyList()); continue
                }
                Entity.gE && leg < route.size - 1 -> {
                    // S31 QTE: press the armed zone's lane (CS[pv] press
                    // edge — aB counts the window, a miss = lane timeout).
                    // Pick the zone the player is INSIDE — a stale armed
                    // zone elsewhere in the list would steal the press.
                    val gz = w.npcs.firstOrNull {
                        it.ax == 10 && it.S == 31 && it.aB > 0 &&
                            it.W[0] <= p.W[2] && it.W[2] >= p.W[0] &&
                            it.W[1] <= p.W[3] && it.W[3] >= p.W[1]
                    }
                    if (gz != null) { w.pad.e(m4Cs[gz.Z[1] and 15]); w.pad.releaseFlush() }
                    w.tick(emptyList()); continue
                }
            }
            val (tx, ty, tag) = if (leg < route.size) route[leg] else route.last()
            var held = 0
            val z31 = w.npcs.filter { it.ax == 10 && it.S == 31 }
                .minByOrNull { Math.abs(it.al - p.al) }
            if (tag == "claim" || tag == "gate" ||
                (z31 != null && z31.aB > 0 &&
                    p.al in z31.W[1] - 40..z31.W[3] + 40)) {
                // any armed S31 zone in reach → press its lane once
                if (z31 != null && z31.aB > 0) {
                    armedSeen++
                    held = m4Cs[z31.Z[1] and 15]
                } else if (z31 != null) {
                    // steer into the zone's W band so `aV()` can arm it
                    val zc = (z31.W[1] + z31.W[3]) / 2
                    val xc = (z31.W[0] + z31.W[2]) / 2
                    if (p.al > zc + 20) held = Pad.M_UP
                    else if (p.al < zc - 20) held = Pad.M_DOWN
                    if (p.ak < xc - 15) held = held or Pad.M_RIGHT
                    else if (p.ak > xc + 15) held = held or Pad.M_LEFT
                } else {
                    if (p.al < w.kP + 130) held = Pad.M_DOWN
                    else if (p.al > w.kP + 230) held = Pad.M_UP
                }
            }
            else if (tag == "perch" ||
                (p.al in 3600..4500 &&
                    w.npcs.any { it.ax == 10 && it.S == 10 })) {
                // perch bind runs whenever an S10 zone is in reach —
                // after a knockdown the player re-enters the band while
                // the leg pointer has already advanced past "perch".
                val zn = w.npcs.filter { it.ax == 10 && it.S == 10 }
                    .minByOrNull { Math.abs(it.al - p.al) }
                if (zn != null) {
                    val cx = (zn.W[0] + zn.W[2]) / 2
                    if (p.ak < cx - 10) held = held or Pad.M_RIGHT
                    else if (p.ak > cx + 10) held = held or Pad.M_LEFT
                    val q = p.al - w.kP
                    if (!w.iBB) {
                        val hover = minOf(zn.W[3] + 80, w.kP + 195)
                        if (p.al > hover + 8 && q > 125) held = Pad.M_UP
                        else if (p.al < hover - 15) held = Pad.M_DOWN
                        val dy = p.W[1] - zn.W[3]
                        if (dy > zn.Z[1] && dy < zn.Z[0]) held = held or 1
                    }
                }
            }
            else {
                if (stallT > 30 || p.al in ty - 60..ty + 80) {
                    if (p.al > ty) held = Pad.M_UP
                    else if (p.al < ty - 10) held = Pad.M_DOWN else held = 0
                }
                else if (p.al > w.kP + 200) held = Pad.M_UP
                else if (p.al < w.kP + 140) held = Pad.M_DOWN else held = 0
            }
            // Escort volley dodge: shots aim at the player's W-center at
            // fire time (i.a(int,boolean) fan, structured/i.java:6245 —
            // ±25-45° cone), so while a live in-flight shot is near, dive
            // below it — the cone converges on the stale aim point.
            val liveShots = w.projectilePool?.count {
                it != null && (it.P and 128) == 0 && it.af != null &&
                    (it.S in 0..4 || it.S in 22..28)
            } ?: 0
            if (liveShots > lastShots && leg >= 8) dodgeT = 60
            lastShots = liveShots
            if (dodgeT > 0 && leg >= 8) {
                dodgeT--
                held = held and Pad.M_UP.inv() or Pad.M_DOWN
            }
            // arena wall duel: puffs only damage a wall they overlap, so
            // steer the player's lane onto the live wall's x-band.
            val wallT = w.npcs.firstOrNull {
                (it.aw == 11 || it.aw == 15 || it.aw == 16 ||
                    it.aw == 17 || it.aw == 154) &&
                    it.aB > 0 && (it.l and 1) != 0
            }
            // slice 396: the faithful pv0 thrower fires six volleys with its
            // box collapsed to a point (S28 windup, S14 is one call long) and
            // only then recoils in S14/S13 with a real box. During the burst
            // the centre knife (S17) falls down the thrower's own x line and
            // the fans sweep its bottom edge: keep off the lane and low; in
            // the recoil window fly the lane and shoot.
            val burst = wallT != null && wallT.aw == 11 &&
                wallT.W[2] - wallT.W[0] < 20
            val txEff = if (burst) wallT!!.ak + (if (p.ak >= wallT.ak) 34 else -34)
                else if (wallT != null) (wallT.W[0] + wallT.W[2]) / 2 else tx
            if (tag != "claim" && tag != "gate" &&
                p.ak < txEff - 12) held = held or Pad.M_RIGHT
            else if (tag != "claim" && tag != "gate" &&
                p.ak > txEff + 12) held = held or Pad.M_LEFT
            // shield line: park just under the live wall — the volley's
            // puffs fall on the player but hit the aligned wall's W first
            // (a wall-hit puff dies in S9, so the wall tanks its own
            // damage while covering us).
            if (wallT != null && tag != "claim" && tag != "gate") {
                val lo = if (burst) 110 else 40
                val hi = if (burst) 160 else 90
                if (p.al < wallT.W[3] + lo) held = held or Pad.M_DOWN
                else if (p.al > wallT.W[3] + hi) held = held or Pad.M_UP
            }
            // Predictive dodge (slice 353): with the volleys aimed and
            // fanned as the bytecode has them, a live escort shot is
            // projected 24 ticks ahead; when it would pass within 30px
            // of the drifting player, take the move that keeps the most
            // clearance.
            dodgeMask288(w, p)?.let { held = it }
            knifeDodge288(w, p, held)?.let { held = it }
            val preAF = w.kAF
            prevS = p.S
            w.pad.e(held); w.tick(emptyList())
            if (w.kP == prevCamY) stallT++ else stallT = 0; prevCamY = w.kP
            if (p.al < minAl) minAl = p.al
            val justFired = (prevS != 21 && p.S == 21 && w.jC == 8) ||
                (preAF == 0 && w.kAF > 0 && w.jC == 8)
            if (leg < route.size && !tag.startsWith("shrine") &&
                p.al < ty - 60) leg++
            if (leg < route.size && tag.startsWith("shrine") && justFired) leg++
            if (leg < route.size && tag.startsWith("shrine") &&
                p.al < ty - 400) leg++
        }
        val dir = w.npcs.firstOrNull { it.ax == 21 }
        println("M4CAP won=$won t=$t leg=$leg minAl=$minAl p@(${p.ak},${p.al}) " +
            "dirAA=${dir?.aA} dirl=${dir?.l} jC=${w.jC} kP=${w.kP} " +
            "armed=$armedSeen fired=$firedN")
        assertTrue(won,
            "mission-4 capstone should reach missionWon; " +
                "got leg=$leg p@(${p.ak},${p.al}) S${p.S} jC=${w.jC} kP=${w.kP} " +
                "kX=${w.kX} iBe=${w.iBe} iBB=${w.iBB} aE=${p.aE} kAH=${w.kAH}")
    }
}

/** Mission-4 bot dodge: each live enemy pool shot (`af` set, in-flight
 *  anim) is stepped `ak += ag/256, al += ah/256` for 24 ticks against the
 *  player's W-centre drifting at the scroll speed `kY`; inside 30px the
 *  bot picks the 8px/tick move (left/right, plus up/down while inside the
 *  view band) with the largest minimum clearance. */
/** Mission-4 bot dodge for the pursuers' own knives (slice 396): the pv0
 *  thrower's fan (`g(0..2)` = ax24 S16/S17/S18) and the pv4 gunner's shots
 *  (S41-S43) are plain ax24 entities whose hit box is the tall `X` rect
 *  (`~8 x 49`, hanging BELOW the knife) — the faithful `bG` fires the pv0
 *  fan every ~13 ticks (six volleys; the S14 fire frame is one call long and
 *  the recoil only follows the sixth). Each candidate move (8 px/tick, the
 *  scroll drift added) is projected `h` ticks against every knife's `X` box
 *  swept by its velocity; the bot keeps its own intent unless that collides
 *  within 12 ticks, then takes the move that postpones the first collision
 *  the most. Inputs only — no state is touched. */
private fun knifeDodge288(w: Level0World, p: Entity, held: Int): Int? {
    val knives = w.npcs.filter {
        it.ax == 24 && (it.S in 16..18 || it.S in 41..43) &&
            (it.P and 128) == 0 && it.af?.ax == 32
    }
    if (knives.isEmpty()) return null
    val drift = w.kY / 256.0
    val h = 18
    fun firstHit(dx: Double, dy: Double): Int {
        for (t in 1..h) {
            val px0 = p.W[0] + dx * t - 3; val px1 = p.W[2] + dx * t + 3
            val py0 = p.W[1] + (drift + dy) * t - 3; val py1 = p.W[3] + (drift + dy) * t + 3
            for (k in knives) {
                val kx = k.ag * t / 256.0; val ky = k.ah * t / 256.0
                if (k.X[0] + kx <= px1 && k.X[2] + kx >= px0 &&
                    k.X[1] + ky <= py1 && k.X[3] + ky >= py0) return t
            }
        }
        return h + 1
    }
    val intentDx = (if ((held and Pad.M_LEFT) != 0) -8.0 else 0.0) +
        (if ((held and Pad.M_RIGHT) != 0) 8.0 else 0.0)
    val intentDy = (if ((held and Pad.M_UP) != 0) -8.0 else 0.0) +
        (if ((held and Pad.M_DOWN) != 0) 8.0 else 0.0)
    if (firstHit(intentDx, intentDy) > 12) return null
    val q = p.al - w.kP
    val moves = mutableListOf(
        0 to (0.0 to 0.0),
        Pad.M_LEFT to (-8.0 to 0.0), Pad.M_RIGHT to (8.0 to 0.0),
        (Pad.M_LEFT + Pad.M_UP) to (-8.0 to -8.0), (Pad.M_RIGHT + Pad.M_UP) to (8.0 to -8.0),
        (Pad.M_LEFT + Pad.M_DOWN) to (-8.0 to 8.0), (Pad.M_RIGHT + Pad.M_DOWN) to (8.0 to 8.0),
        Pad.M_UP to (0.0 to -8.0), Pad.M_DOWN to (0.0 to 8.0))
    if (q <= 90) moves.removeAll { it.second.second < 0 }
    if (q >= 190) moves.removeAll { it.second.second > 0 }
    return moves.maxByOrNull { firstHit(it.second.first, it.second.second) }?.first
}

private fun dodgeMask288(w: Level0World, p: Entity): Int? {
    val pool = w.projectilePool ?: return null
    val shots = pool.filter {
        it != null && (it.P and 128) == 0 && it.af != null &&
            (it.S in 0..4 || it.S in 22..28)
    }
    if (shots.isEmpty()) return null
    val drift = w.kY / 256.0
    val cy0 = (p.W[1] + p.W[3]) / 2.0
    fun clearance(dx: Double, dy: Double): Double {
        var best = Double.MAX_VALUE
        for (s in shots) for (t in 1..24) {
            val sx = s!!.ak + s.ag * t / 256.0
            val sy = s.al + s.ah * t / 256.0
            val d = Math.hypot(sx - (p.ak + dx * t), sy - (cy0 + (drift + dy) * t))
            if (d < best) best = d
        }
        return best
    }
    if (clearance(0.0, 0.0) > 30) return null
    val q = p.al - w.kP
    val opts = mutableListOf(Pad.M_LEFT to (-8.0 to 0.0), Pad.M_RIGHT to (8.0 to 0.0))
    if (q > 90) opts += Pad.M_UP to (0.0 to -8.0)
    if (q < 190) opts += Pad.M_DOWN to (0.0 to 8.0)
    return opts.maxByOrNull { clearance(it.second.first, it.second.second) }?.first
}
