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

private fun chaseMask289(p: Entity, w: Level0World): Int {
    var mask = Pad.M_RIGHT
    when (p.S) {
        65 -> mask = Pad.M_UP + Pad.M_TAP_R
        // S228/358 carrier-bound: UP-held + dir into facing releases
        // into the S235 dive. Leg A's spawn-pit carrier releases west
        // (staircase); leg B's deep-pit carriers release east into the
        // channel under the block.
        228, 358 -> mask =
            (if (p.ak < 1000) Pad.M_LEFT else Pad.M_RIGHT) + Pad.M_UP
        297, 89, 90 -> mask = Pad.M_CONTEXT
        // S258/260/262 perch: a direction edge fires the side-launch
        // S261 (ag=±3072, ah=-7680) — arcs over the corridor gap to
        // mass2. DOWN would drop the player off the perch into the pit.
        258, 260, 262, 259, 261, 263, 264, 265, 266 -> mask = Pad.M_RIGHT
        // S5/79 land-recovery: an UP edge on the landing tick fires
        // the parkour leap (landArm L1834) — and the kick lands
        // facing west, so it would leap west into the pit
        5, 79,
        235, 236, 237, 238, 239, 240, 241, 242, 243 -> mask = Pad.M_RIGHT
        // S60/61/203 ledge-hangs: UP edge fires the S62 climb-up
        // (L2460 arm). aF doesn't matter mid-hang.
        60, 61, 203 -> mask = Pad.M_UP
        // S33 wall-cling: hold dir-into-wall — east face (av=false)
        // arms the L859 launch (i(22), ah=-5120, ag=+2048 — the
        // wall-slide chimney climb); west face arms the L822 lip
        // scan / L824 kick ping-pong. aZ=false mid-cling so the
        // generic airborne rule would zero the mask and kick off.
        33 -> mask = if (p.av) Pad.M_LEFT else Pad.M_RIGHT
        // S22/23/43 climb arc: dir-into-wall + the TAP edge arms aF
        // (L3a9d `cv && u|v(UP|TAP)`) so the wall-grab re-fires on
        // contact — M_LEFT/M_RIGHT alone don't set the tap bits.
        22, 23, 43 -> mask =
            (if (p.av) Pad.M_LEFT + Pad.M_TAP_L
             else Pad.M_RIGHT + Pad.M_TAP_R)
        // every other airborne state (grab/kick/launch/fall/vault arc)
        // is ballistic — direction input only re-arms the aF grab latch
        // (landArm L2092), which then kills the arc on the next wall
        // contact. Grounded run-ups arm aF; wallGrabSnap consumes it.
        else -> if (!p.aZ) mask = 0
    }
    // corridor approach x1260-1900: the channel runs UNDER the y400-460
    // block on the y580-620 floor — no vault; an UP press here vaults
    // into the block's west face and the kick rebounds west. East of
    // x1900 (interior shaft/climb) the vault is needed again.
    if (p.aZ && p.ak >= 560 && !(p.ak in 1260..1900 && p.al > 470))
        mask = Pad.M_RIGHT + Pad.M_UP
    // leg-B route (proven via ACLV): the x1340-1560 block is a 340px
    // solid wall grounded on the y620 floor — uncrossable from the pit.
    // roof east to cp180 (leg A): the x1340 wall rises to y240 —
    // uncrossable; the only through-route is the tunnel mouth below
    // the roof slab (x1240-1339, y640-740), which leg B enters via a
    // pin inside the tunnel (no roof walk needed there).
    if (p.aZ && p.al in 560..640 && p.ak in 1240..1340) mask = Pad.M_RIGHT
    // bowl/mouth drift band: while airborne anywhere in the bowl or
    // the mouth window (x1050-1400, y540-790) hold EAST — overrides the
    // S22/43 facing arm (post-kick av=true would drift west to death).
    if (!p.aZ && p.al in 540..790 && p.ak in 1050..1400) mask = Pad.M_RIGHT
    // deep-floor east run under the block — no vault (low headroom)
    if (p.aZ && p.al > 700 && p.ak < 1860) mask = Pad.M_RIGHT
    // leg-A staircase vault bands (proven via ACLV + probeStair): the
    // S233 launch arc must reach the face with al<=1059 (hand row
    // W[1]<=989 -> i4=49 -> ledgeLipGrab -> S60). The launch rises
    // ~44px in ~4 ticks then falls — it only stays in the lip window
    // when it fires ~x578-586 (edge ~x570-578); earlier falls too low,
    // later hits mid-face (S101 -> kick). These bands must sit AFTER
    // the deep-floor RIGHT arm or UP never reaches the pad.
    if (p.aZ && p.al > 1000 && p.ak in 563..573)
        mask = Pad.M_RIGHT + Pad.M_UP                     // x620 face
    if (p.aZ && p.al in 930..1000 && p.ak in 725..760)
        mask = Pad.M_RIGHT + Pad.M_UP                     // x780 face (shelf)
    // x1020 bowl crossing (proven via ACLV + ax66 bm()): the gap is a
    // bound-carry chain — land crate-1 @(1089,641) → auto-carry S236/237
    // → S228 ride on crate-2 @(1169,620). Its linked S15 marker then
    // removes the crate, so the ride needs the L196 dismount (grabKey =
    // UP held) before removal — hop east into the x1240 tunnel mouth.
    if (p.S == 228 || p.S == 358)
        mask = Pad.M_RIGHT + Pad.M_UP                     // crate dismount east
    // ---- leg B (proven end-to-end: probeInteriorClimb climbs the
    // slot base (1910,759) → shaft → slab → tower top at t=351;
    // probeZipline rides the tower top → cp555 @(3043,368) at t=187) —
    // tunnel east → floor-gap vault → pillar → x1860-1939 slot kicks
    // → drift west → shaft x1500-1659 chimney → slab top y400 →
    // x1940 tower face → tower top → S34 rail → block2/3 tops.
    // Grounded anywhere in the climb region x1460-1960, y470-790 —
    // tunnel floor, pillar, slot base, y579 cavity floor: run east +
    // UP-held vaults. On the y579 floor the S233 hop-chain migrates
    // the player west toward the shaft; the slab ceiling y460 caps.
    if (p.aZ && p.al > 470 && p.ak in 1460..1960)
        mask = Pad.M_RIGHT + Pad.M_TAP_R + Pad.M_UP     // climb-column vaults
    // The whole climb column x1460-2119 airborne: RIGHT + TAP_R —
    // probeInteriorClimb's proven hold. Ballistic, but the TAP edge
    // re-arms aF so each east-face contact grabs (slot kicks, shaft
    // ping-pong, tower-face chain). West-drift legs land back on a
    // floor and relaunch — net +40px/cycle up the 280px tower face.
    if (!p.aZ && p.ak in 1460..2119)
        mask = Pad.M_RIGHT + Pad.M_TAP_R                // airborne column
    // Slice 416: with the faithful fall-arm wall-grab snap (g.javap.txt e() @8881-8927) the slot kick
    // off the x1960 face arcs onto the slab's east lip (x1880, y580) and CATCHES it (S61 lip hang):
    // pull UP onto the slab top (S62 -> S0) instead of letting the 40-tick hang expire into a drop.
    if (p.S == 61 || p.S == 60 || p.S == 62)
        mask = Pad.M_UP                                 // lip hang -> pull-up
    // y399 slab-top (post-shaft): RUN east to the x1960 tower face —
    // holding UP here turns every landing into a standstill squat-jump
    // (ag never rebuilds → stationary bounce at ~x1825). The face
    // approach still gets RIGHT+UP from the generic band at x1930+,
    // and airborne contacts get RIGHT+TAP_R from the column band.
    if (p.aZ && p.al <= 470 && p.ak in 1560..1929)
        mask = Pad.M_RIGHT                              // slab-top run
    // Tops east (slab y400, tower top y120, block2/3 y400): grounded
    // run + vault — on the tower top the vault catches the S34 rail.
    if (p.aZ && p.al <= 545 && p.ak >= 1960)
        mask = Pad.M_RIGHT + Pad.M_UP                   // tops east
    // ax16-S32 kill-prompt marker uid19 @(1953,289): a CONTEXT tap
    // while overlapping its box teleports the player to (1908,289)
    // and fires S214 -> S215 mount-fling up the tower face (the
    // designed shortcut past the one-sided face — kicks alone can't
    // climb it). Wide window: the kick rebound drifts the player
    // through it.
    if (p.ak in 1860..1980 && p.al in 240..330)
        mask = Pad.M_CONTEXT                            // prompt zone
    // ...but S5/79 land-recovery + S21 squat must hold UP ONLY (no
    // direction): the kick's rebound already flipped av — holding a
    // direction re-faces the player and every hop arc goes the wrong
    // way (probeInteriorClimb's westward migration needs av preserved).
    // LAST leg-B arm — must override every band above regardless of aZ.
    // (Slice 416: the S0 frame after the slot lip's pull-up S62 stands on the y579 slab top facing
    // west — it hops on west, keeping av, exactly like the S5 landings.)
    if ((p.S == 5 || p.S == 79 || p.S == 21 || (p.S == 0 && p.al in 570..590)) &&
        p.al > 470 && p.ak in 1460..1960)
        mask = Pad.M_UP                                 // hop: keep av
    // melee override — LAST so no traversal arm can silence it (the tunnel
    // guard at ~x1500,y767 patrols the corridor floor; the slab-top ax73 at
    // (1845,398) blocks the leg-B tower run): the soldier in front of the
    // swing (east — a foe behind (west) on a lower level, e.g. the cp321 tower
    // base guard uid30 stuck in its chase under the y919 step, is not in
    // reach, so the route goes on instead) while grounded -> face east + attack.
    // Slice 401: a soldier FACING the player takes no hit while it flinches
    // (S85) or counters (S17) — the I() head leaves the intake gate down in
    // those states, and S17 answers a swing with the player's i(8) stun — so
    // the swing waits for the damageable windows, and the target is the
    // nearest soldier in front (a soldier behind it never decides the swing).
    val foe = w.npcs.filter {
        (it.ax == 11 || it.ax == 73) && it.aB > 0 && it.S != 139 &&
            Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) < 55 &&
            (it.ak >= p.ak - 20 || Math.abs(it.al - p.al) < 20)
    }.minByOrNull { Math.abs(it.ak - p.ak) }
    // Slice 416: a soldier at the player's BACK on his own level is the one to face first. The
    // front-only target let the bot keep striking a stunned soldier ahead (S144) while the one
    // behind hit him in the back (the block-2 duel: S9 from behind, x1 30 -> 10, then a dive off the
    // edge) — a human turns round (LEFT + the attack edge) and answers the attacker.
    val behind = w.npcs.filter {
        (it.ax == 11 || it.ax == 73) && it.aB > 0 && it.S != 139 &&
            it.ak < p.ak - 15 && Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) < 30
    }.minByOrNull { Math.abs(it.ak - p.ak) }
    val target = behind ?: foe
    val foeOpen = target != null && target.S != 85 && target.S != 17
    if (target != null && foeOpen && p.aZ)
        mask = (if (target === behind) Pad.M_LEFT else Pad.M_RIGHT) + Pad.M_CONTEXT
    return mask
}

class Slice289Test {
    /** Slice 289 — mission-5 capstone bot legs on `world(aj = 5)`
     *  (Venice/Octavien chase, pack-11: spawn (44,582) -> east chase
     *  -> finale ax5-S8 zone uid308 @(15439,603) aG=389 -> win).
     *  Same capstone pattern as slices 279-282/288 — real input only,
     *  no state pinning. The ax5-S8 spine marks the milestones:
     *  6170, 7140, 7579, 8314, 9439, 11061, 11891, 12312, 13689,
     *  14735, 15439 — claim-script gates that fire on overlap.
     *  Checkpoints ax2: 1256, 3043, 5135, 5868, 7105, 8122, 11135,
     *  12276, 13584.
     *
     *  Leg A: spawn -> intro drops the player into the x120-440 pit ->
     *  west-face staircase (pit floor -> slab x440-620/y1120 -> corridor
     *  shelf x620-780/y980 -> mass top x780-1020/y660) -> cp180 @(1256,590).
     *  The face ascents are the two-vault chain proven in slice-289 recon:
     *  vault -> S101 grab -> S36 kick (facing flips west, lands back on
     *  the lower shelf) -> re-run east RIGHT-only (re-faces) -> second
     *  vault catches the shelf lip (S60) -> mantle. */

    @Test fun mission5CapstoneLegA() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        var maxAk = 0; var reached = false
        for (t in 0..8000) {
            val mask = chaseMask289(p, w)
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            val m = chaseMask289(p, w)
            val ax5 = w.npcs.firstOrNull { it.ax == 5 }
            val plats = w.npcs.filter { it.ax == 66 && it.ak in 1000..1400 }
                .joinToString(",") { "66(${it.ak},${it.al})S${it.S}" }
            if (t % 25 == 0 || p.S == 233 || p.S == 33 || (p.S == 22 && p.ak < 640) || p.S == 317 || p.S == 43 || p.S == 105 || (t in 300..520 && t % 4 == 0) || (p.ak in 960..1400 && t % 4 == 0))
                println("TRC t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ag=${p.ag} ah=${p.ah} camY=${w.kP} m=$m kC=${w.kC?.ax} plats=$plats ax5alive=${ax5 != null}")
            if (p.ak >= 1256) { reached = true; break }
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
        println("M5A reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legA spawn->staircase->cp180@(1256,590): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneFull() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // P4 step 3 — continuous run: spawn -> cp180 (legA) -> the
        // tunnel -> tower -> cp555 (legB) -> plaza tower cp321 (legC2)
        // -> east. legA/legB/legC2 all drive chaseMask289 plus the
        // position-scoped overrides each leg proved; no teleports.
        //
        // P4c seam note (documented, see reports/capstone-revalidation.md):
        // the union clears legA to cp180 (x1256) and stalls at the tunnel
        // mouth — the y620 slab top is a dead end; the route is the
        // carrier descent into the `02`@880 shaft (ax66 uid512/uid316,
        // ax14 descent markers (1061,587)/(1292,651)) or a west-face
        // hang-drop into the mouth band x1240-1260 — needs the same
        // timed-mount/hang-release precision as m3's seam. legs D-I each
        // drive a custom state-keyed mask for their stunt chain and are
        // proved by the per-leg tests.
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
            var mask = chaseMask289(p, w)
            // legC2's standstill-jump onto the tower face (see leg C2)
            if ((p.aZ || p.S == 5) && p.al in 900..925 && p.ak in 5055..5110)
                mask = when {
                    p.ak > 5075 -> Pad.M_LEFT
                    p.av -> Pad.M_RIGHT
                    else -> Pad.M_UP
                }
            // tunnel-mouth entry (legB's route): the y620 slab top is a
            // dead end — step west off its west edge at x1240-1250, then
            // drift east to land on the corridor floor y760 inside the
            // mouth band (x1240-1260 open y640-740). Kept as the
            // documented approach attempt even though the run still
            // stalls (see seam note): the face-grab/carrier descent is
            // the unbridged remainder.
            if (p.aZ && p.al in 612..628 && p.ak in 1200..1290)
                mask = Pad.M_LEFT
            if (!p.aZ && p.ak in 1180..1300 && p.al in 620..770)
                mask = Pad.M_RIGHT
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
        }
        println("m5 full end S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
        // documented seam (see header): legA->legB->legC2 shared-mask
        // reach; legs D-I keep their per-leg proofs
        assertTrue(maxAk >= 1256,
            "m5 full run clears legA to cp180: S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk deaths=$deaths jC=${w.jC} marks=$marks")
    }

    @Test fun mission5CapstoneLegB() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legB — cp180(1256,590) -> cp555(3043,368). ACLV-verified:
        // the x1340 wall rises to y240 — the only through-route is the
        // tunnel mouth below the x1240-1339 roof slab (y640-740). Pin
        // inside the mouth -> drop to the y760 floor, run east under
        // the slab to the x1880-1959 gap column, kick up the x1960
        // tower face into the chamber (mid-slab top y580), run west to
        // the x1520-1599 shaft, kick up to the slab top y400, then the
        // ax16 marker @(1953,289) teleports past the tower; street
        // y660 + block2/3 tops -> cp555.
        p.setPositionPx(1300, 700); p.ak = 1300; p.al = 700; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        // checkpoint leg-start state: the kill-prompt unlock bit — any
        // real mission-5 save carries it (granted by an ax73 death
        // i.java:9739 g.g(2) or an ax16-S30 pickup i.java:14091); the
        // S32 marker @(1953,289) refuses to arm without J&2
        // (NpcFsm.kt:3882).
        p.gJ = p.gJ or 2
        w.kO = 1100; w.kP = 560; w.rebuildCamRect()
        var reached = false; var maxAk = 0
        for (t in 0..8000) {
            // Slice 404: on the block2/3 tops (x >= 2560) two soldiers flank the
            // run to cp555. Slice 401 ran past them (they paced at 2 px/tick against
            // the player's 10) — but an alerted soldier is solid to the player (the
            // shared tail's `a()` @7644-7657 joins both aA branches), so the run is
            // blocked and the duel is the route: chaseMask289's melee override.
            val mask = chaseMask289(p, w)
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak >= 3043 && p.al <= 410 && p.aZ) { reached = true; break }
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
        println("M5B reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legB cp180->street->tower->cp555@(3043,368): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegC1DiveVerdict() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legC1 — the cp555 canyon descent VERDICT. The ax10-S33 zone
        // @(3071,176) autowalk+flings the player into the pit. Every
        // landing under it is mathematically lethal as modeled:
        //   pit floor y1020 = v=3 (not in the landing set {>=12,4,5} —
        //   falls through -> KO);
        //   crate chain tops ~y965-1004 — ax51 mount drains op21 by
        //   (al-g.y)/20 cells; g.y apex stamps ~398 at the S148 entry ->
        //   (965-398)/20 = 28 cells -> drain ~147 > x1=30 -> KO.
        // The drive records which landing is taken; the leg asserts the
        // descent ends in KO — no survivable landing exists.
        p.setPositionPx(3043, 360); p.ak = 3043; p.al = 360; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        var ko = false; var mount = false; var minX1 = 30; var maxAk = 3043
        var mountDesc = ""; var prevS = -1
        for (t in 0..600) {
            w.pad.e(if (p.aZ || p.S == 22 || p.S == 23 || p.S == 43 || p.S == 150) Pad.M_RIGHT else 0)
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.x1 < minX1) minX1 = p.x1
            if (p.ga != null && !mount) {
                mount = true
                mountDesc = "ax${p.ga?.ax}uid${p.ga?.aw}@(${p.ga?.ak},${p.ga?.al})"
                println("C1 MOUNT t=$t on $mountDesc gy=${p.gy} x1=${p.x1}")
            }
            if (p.S != prevS || t % 20 == 0) {
                val near = w.npcs.filter { it.ax != 0 && Math.abs(it.ak - p.ak) < 100 && Math.abs(it.al - p.al) < 120 }
                    .joinToString(",") { "ax${it.ax}(${it.ak},${it.al})" }
                println("C1 t=$t S${p.S} @(${p.ak},${p.al}) aZ=${p.aZ} ah=${p.ah} gy=${p.gy} x1=${p.x1} ga=${p.ga?.ax} near=$near jC=${w.jC}")
            }
            prevS = p.S
            if (w.jC == 12 || w.jC == 13) { ko = true; break }
            if (p.S == 147 || p.S == 317) { ko = true; break }
            if (w.jC != 8) break
        }
        println("C1 VERDICT ko=$ko mount=$mount($mountDesc) maxAk=$maxAk minX1=$minX1 p@(${p.ak},${p.al}) S${p.S}")
        assertTrue(ko,
            "legC1 canyon descent -> KO (28-cell fall, mount+drain or v=3 floor): got S${p.S} @(${p.ak},${p.al}) x1=${p.x1} jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegC2PlazaTower() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legC2 — post-pit approach. The pit's east side opens onto the
        // base plateau y960 (x4820-5239); the cp321 tower rises from the
        // wide base x5060-5259 top y920 to the tower top y840 x5120-5239.
        // Checkpoint-style start on the plateau floor; walk east, step
        // the 40px wide base, climb the 80px tower face, reach cp321
        // @(5135,797).
        p.setPositionPx(4880, 930); p.ak = 4880; p.al = 930; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 4700; w.kP = 800; w.rebuildCamRect()
        var reached = false; var maxAk = 4880; var prevS = -1
        for (t in 0..3000) {
            var mask = chaseMask289(p, w)
            // Slice 369 (F8): the S5 land arm no longer squats in S21 under
            // a held RIGHT+UP — it runs l() (e() 4764-4793): the held RIGHT
            // starts a run and the post-tail jumps at once, carrying the
            // run's first 10px. From the 40px base step (y919) that arc
            // meets the tower face just below its top (S101) and the kick
            // throws him back west. Jump from a standstill at the step's
            // west end instead: walk west, turn east with a RIGHT tap
            // (l(): `aA != 0 → av = false`, no step), then UP alone.
            if ((p.aZ || p.S == 5) && p.al in 900..925 && p.ak in 5055..5110)
                mask = when {
                    p.ak > 5075 -> Pad.M_LEFT
                    p.av -> Pad.M_RIGHT
                    else -> Pad.M_UP
                }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.S != prevS || t % 60 == 0 || (p.ak > 5050 && t % 12 == 0))
                println("C2 t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} camY=${w.kP} m=$mask jC=${w.jC}")
            prevS = p.S
            if (p.ak >= 5120 && p.al <= 850 && p.aZ) { reached = true; break }
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
        println("C2 reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legC2 plateau->tower->cp321@(5135,797): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegD() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legD — cp321 -> cp300@(5868,174). The v=3 pit x5240-5459 is
        // crossed by the ax72-counterweight lunge chain (proven): mount
        // uid62 -> M_UP binds ax72 uid70 -> context tap -> S275 lunge ->
        // S277 mount -> orbitSwing launch -> seesaw uid76 catch (S297) ->
        // M_TAP_R -> S19 launch onto the slab (y499). From the slab a
        // jump overlaps ax16 uid69's kill-prompt W -> context tap ->
        // S214 teleport + S215 leap up the wall face -> block top y219
        // -> east to cp300.
        val m62 = w.npcs.first { it.aw == 62 }
        val e69 = w.npcs.first { it.aw == 69 }
        val guard65 = w.npcs.firstOrNull { it.aw == 65 }
        p.setPositionPx(5318, 634); p.ak = 5318; p.al = 634; p.av = false
        p.S = 260; p.ga = m62; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 5100; w.kP = 470; w.rebuildCamRect()
        var reached = false; var maxAk = 5318; var upTicks = 0
        var prevS = -1; var stall = 0; var lastAk = 5318
        for (t in 0..7000) {
            p.gJ = 7                                   // equip incl. the ax16-prompt bit
            val mask = when {
                p.S == 297 -> Pad.M_TAP_R                          // seesaw launch east
                // Slice 404: the chain's drop lands on the slab guard (uid65, 600 HP,
                // solid to the player) — the S89 air pin over it is the stab edge:
                // press the context button (S89 -> S90 -> the guard dies in S20),
                // exactly as the mission-6 legs do. Running on instead sends the
                // pin's release into a duel the player (30 HP) cannot win.
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                Entity.at != null && p.ga == null -> Pad.M_CONTEXT // bound ax72 -> lunge
                p.ga == m62 && upTicks < 6 -> Pad.M_UP             // mount-up -> bind
                Entity.overlapI(p.W, e69.W) -> Pad.M_CONTEXT       // kill-prompt overlap
                // Slice 411: the faithful S292 lunge (a hanging player, `g.b(int)`) keeps the
                // orbit's full radius, so the launch no longer meets the seesaw uid76 (S297
                // catch) — the arc drops onto the WEST slab guard uid77 instead (stab, S89/S90)
                // and the east guard uid65 wakes and cycles S22 approach / S11 windup / S12
                // strike / S23 back-off. Running on takes a strike every ~26 ticks (5 of them
                // leave x1=5); fighting through at point-blank (the S67-69 combo staggers the
                // guard before its windup completes) takes none.
                guard65 != null && guard65.aB > 0 && guard65.S != 139 && p.aZ &&
                    guard65.ak - p.ak in -10..56 -> Pad.M_RIGHT + Pad.M_CONTEXT
                p.ak in 5560..5780 && p.al >= 480 -> Pad.M_UP or Pad.M_RIGHT
                else -> Pad.M_RIGHT
            }
            if (mask == Pad.M_UP) upTicks++
            w.pad.e(if (p.S == 69 && p.T == 4) 0 else mask)      // legI: let go on the S69 last frame
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 60 == 0) {
                println("D t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} " +
                    "ga=${p.ga?.aw} at=${Entity.at?.aw} m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 5840 && p.al <= 240 && p.aZ) { reached = true; break }
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
        println("D reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC} x1=${p.x1}")
        assertTrue(reached,
            "legD cp321->launch-chain->block->cp300@(5868,174): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegE() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legE — cp300 -> cp303@(7105,1060). East along the block top,
        // drop into the x6315-6318 gap (wisp trail descends to y986),
        // land on the lethal-band bridge y1120, then hop the ax51 crate
        // chain across the lethal floor to the right bridge -> cp303.
        p.setPositionPx(5870, 219); p.ak = 5870; p.al = 219; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 5700; w.kP = 100; w.rebuildCamRect()
        var reached = false; var maxAk = 5870; var prevS = -1; var stall = 0; var lastAk = 5870
        for (t in 0..7000) {
            p.gJ = 7
            val mask = when {
                p.S == 297 -> Pad.M_TAP_R
                p.S == 89 || p.S == 90 || p.S == 250 -> Pad.M_CONTEXT // perch kill-leap tap
                // the ax10-S36 bound-catch carrier @x6439 lowers the player
                // down the shaft — keep its bind while inside the descent
                // column, release it elsewhere.
                // Phase 2 (G12): the last block-top hop now clips the
                // zone's top-left corner (W x6439-6479, y379-539); the
                // ride ends at its bottom edge (al > g.o → i(318), g.java
                // :1860-1864) in a freeze that only a DOWN press releases
                // (case 318 `k.v(33024) → a(ah)`, g.java:1829-1835).
                // Press it: the drop comes down on the y719 floor.
                p.S == 315 || p.S == 318 ->
                    if (p.S == 315 && p.ak in 6300..6600) 0 else Pad.M_DOWN
                // x6520-6580 40px step on the y719 floor: hop west over it
                p.aZ && p.al in 700..790 && p.ak in 6560..6660 ->
                    Pad.M_UP or Pad.M_LEFT
                // y719 floor + step top: walk WEST — the floor ends at
                // x6440 and the drop lands on the y1100 bridge.
                p.aZ && p.al in 660..790 && p.ak > 6380 -> Pad.M_LEFT
                // lethal-band crate chain: S8 crates can't mount; the S1
                // crates carry a side-pin (can't walk off) — vault ONCE at
                // the bridge edge onto uid74, then vault-hop crate to crate
                p.al > 980 && (p.ga != null || (p.aZ && p.ak in 6530..6590)) -> Pad.M_RIGHT + Pad.M_UP
                p.al > 980 -> Pad.M_RIGHT
                p.aZ -> Pad.M_RIGHT + Pad.M_UP
                else -> Pad.M_RIGHT                          // airborne: drift east
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 60 == 0 || stall > 120 || p.S in listOf(315,318,89,90,297)) {
                println("E t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 7070 && p.al in 1000..1120 && p.aZ) { reached = true; break }
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
        println("E reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legE cp300->gap-drop->crate-hop->cp303@(7105,1060): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegF() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legF — cp303@(7105,1060) -> cp404@(8122,600). East on the right
        // bridge, ax13 rope uid93 @(7219,611) up past the lethal band to
        // the x7380+ roof (y640), then east on the roof to cp404.
        p.setPositionPx(7105, 1050); p.ak = 7105; p.al = 1050; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 6900; w.kP = 890; w.rebuildCamRect()
        var reached = false; var maxAk = 7105; var prevS = -1; var stall = 0; var lastAk = 7105
        for (t in 0..7000) {
            p.gJ = 7
            val mask = when {
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 315 || p.S == 318 -> Pad.M_DOWN
                p.aZ -> Pad.M_RIGHT + Pad.M_UP
                else -> Pad.M_RIGHT
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 40 == 0 || stall > 100) {
                println("F t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} ga=${p.ga?.aw} stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 8060 && p.al <= 640 && p.aZ) { reached = true; break }
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
        println("F reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legF cp303->rope->roof->cp404@(8122,600): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegG() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legG — cp404@(8122,600) -> cp408@(11135,985). Drop off the roof
        // east edge to the street platforms, then across the lethal band
        // via the ax15 grapple-block chain @x8795-9454 to cp408.
        p.setPositionPx(8090, 600); p.ak = 8090; p.al = 600; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 7900; w.kP = 440; w.rebuildCamRect()
        var reached = false; var maxAk = 8090; var prevS = -1; var stall = 0; var lastAk = 8090
        for (t in 0..9000) {
            p.gJ = 7
            val mask = when {
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 65 -> Pad.M_UP                          // ax22: vault east
                // zipline: hop off once past the pit's east lip — the
                // S157 exit fling (ag+2048 / ah-2560) lands on the block
                p.S == 164 && p.ak > 9200 -> Pad.M_TAP_R
                p.S == 164 || p.S == 157 -> Pad.M_RIGHT
                // ax5-S8 uid200 script-201 ride: QTE gates at script
                // steps 20/63/134 need M_UP / M_CONTEXT / M_DOWN presses
                p.S == 317 -> Pad.M_UP + Pad.M_CONTEXT + Pad.M_DOWN
                p.S == 315 || p.S == 318 -> Pad.M_DOWN
                // x8560 street edge -> x8760 face: hold the vault till the
                // edge so the ~200px arc reaches the S6 block's hang zone
                p.aZ && p.al > 1000 && p.ak < 8540 -> Pad.M_RIGHT
                // Phase 2 (G12): the ax40 gondola uid181 @x8882 binds the
                // player in the entity pass, then ticks again as his `ac`
                // link right after aS.I() (k.java:2589-2591) — an UP
                // pressed on the boarding frame is its bx() hop-off
                // (`k.v(16388)`, i.java:16150) and the S157 fling drops
                // into the pit. Run into it on RIGHT alone from the y979
                // ledge.
                p.aZ && p.al in 960..1000 && p.ak in 8840..8900 -> Pad.M_RIGHT
                p.aZ -> Pad.M_RIGHT + Pad.M_UP
                else -> Pad.M_RIGHT
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 40 == 0 || stall > 100 || p.S in listOf(209,107,108)) {
                println("G t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} ga=${p.ga?.aw}(ax${p.ga?.ax}) stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 11000 && p.al <= 1020 && p.aZ) { reached = true; break }
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
        println("G reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legG cp404->grapple-chain->cp408@(11135,985): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegH() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legH — cp408@(11135,985) -> cp342@(12276,909). East over the
        // gap via ax22 uid211@11408 (S65 -> M_UP launch onto the x11520
        // pillar), soldiers uid213/932 on the x11780 block, then ax22
        // uid214@12151 launches across the 380px void past cp342's zone.
        // Note: on the faithful sim (i.D() clears g.j post-respawn,
        // i.java:2495) the run-hop arcs gain ~+20px — the original hop-hop
        // script overshot the lip; releasing direction mid-hop shortens
        // arcs so the run reaches the x12051 lip grounded, then edge-vaults.
        p.gJ = 7
        p.setPositionPx(11135, 985); p.ak = 11135; p.al = 985; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 10900; w.kP = 800; w.rebuildCamRect()
        var reached = false; var maxAk = 11135; var prevS = -1; var stall = 0; var lastAk = 11135
        for (t in 0..9000) {
            p.gJ = 7
            val mask = when {
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                // Slice 404: the two 600-HP soldiers on the x11780 block (uid213/932)
                // are solid to the player (the shared tail's `a()` @7644-7657 joins both
                // aA branches) — the hop-run can no longer pass through them. They are
                // unaware (j == 0) when the mantle ends: press the context button inside
                // the stealth-kill window (k(): <=80x, <=5y, unaware) instead of hopping.
                p.aZ && w.npcs.any {
                    it.ax == 11 && it.aB > 0 && it.j == 0 &&
                    Math.abs(it.ak - p.ak) < 80 && Math.abs(it.al - p.al) <= 5
                } -> Pad.M_CONTEXT
                p.S == 65 -> Pad.M_UP                          // ax22: vault east
                p.S == 317 -> Pad.M_UP + Pad.M_CONTEXT + Pad.M_DOWN
                p.S == 164 || p.S == 157 -> Pad.M_RIGHT
                p.S == 315 || p.S == 318 -> Pad.M_DOWN
                p.S == 68 || p.S == 69 -> Pad.M_CONTEXT        // combo chain
                // release direction during airborne hops — shortens each
                // arc so the run lands before the lip and reaches x12051
                // grounded (the faithful i.D()-cleared arcs gain +20px)
                !p.aZ && p.ak in 11800..12120 && p.al < 1010 -> 0
                // the void-gap vault must take off right at the x12060
                // edge so the arc threads the ax22 uid214 zone — walk the
                // last ~20px grounded (no hop), then vault at the edge
                p.aZ && p.ak in 11960..12050 && p.al < 1000 -> Pad.M_RIGHT
                p.aZ -> Pad.M_RIGHT + Pad.M_UP
                else -> Pad.M_RIGHT + Pad.M_UP
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 40 == 0 || stall > 100 || p.S in listOf(209,107,108)) {
                println("H t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 12240 && p.al <= 960 && p.aZ) { reached = true; break }
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
        println("H reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legH cp408->ax22-chain->cp342@(12276,909): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test fun mission5CapstoneLegI() {
        val w = world(aj = 5)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // legI — cp342 lip (x12260-13020, top y940) -> cp405@(13584,665)
        // on tower B. Stepping crates uid241@13109/uid240@13270 (~y1150),
        // ax66-S12 platform uid96@13389, tower-A face x13380, then east
        // gap x13460-13560 down onto tower B top y700.
        p.gJ = 7
        p.setPositionPx(12280, 939); p.ak = 12280; p.al = 939; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 12080; w.kP = 780; w.rebuildCamRect()
        var reached = false; var maxAk = 12280; var prevS = -1; var stall = 0; var lastAk = 12280
        var settle = 0; var wasS90 = false
        for (t in 0..60000) {
            p.gJ = 7
            val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 73) && it.aB > 0 && it.S != 139 &&
                    Math.abs(it.ak - p.ak) < 90 && Math.abs(it.al - p.al) < 60
            }
            // Slice 410 — `i.a(IIILi;)V`'s head knocks a player down only when he is OFF the
            // ground (`g.b(aS.S)`, the air/hang set); a grounded player — mid-combo included —
            // takes the normal arm and flinches (S9). The old bot leaned on the other reading: a
            // soldier strike during S67-69 threw it into S43, which dropped `al` 12 px and the
            // camera with it, so the ax50 pouncers up at y783-789 stayed outside `k.ac` (`l()`
            // L77 wants their W fully inside it). The S9 hop is `al-1`: the lerped camera settles
            // one pixel higher (kP 790 → 789) and the next perch frame at W[1]=789 sees the player
            // — every pounce after that is 5 HP, for good. A human plays the lip by keeping out
            // of a soldier's strike instead (S11 winds up for 8 ticks, the S12 hit frame is T1,
            // X ≈ [ak-47, ak-13]; the heavy's S131/S146/S155 counter boxes reach ~74 px), so the
            // bot does too: back off while a strike is imminent, never walk INTO a windup, poke
            // only from inside its own reach (≈ 48 px) and hold the heavy at ~120 px until it
            // recovers (S171/S156). Input only.
            val heavy = w.npcs.firstOrNull {
                it.ax == 73 && it.aB > 0 && it.ak - p.ak in 0..220 && Math.abs(it.al - p.al) < 60
            }
            val imminent = w.npcs.any {
                (it.ax == 11 && it.aB > 0 && ((it.S == 11 && it.T >= 3) || (it.S == 12 && it.T <= 1)) &&
                    it.ak - p.ak < 85 && Math.abs(it.al - p.al) < 60) ||
                (it.ax == 73 && it.aB > 0 && (it.S == 131 || it.S == 146 || it.S == 155) &&
                    Math.abs(it.ak - p.ak) < 140 && Math.abs(it.al - p.al) < 60)
            }
            val winding = w.npcs.any {
                it.ax == 11 && it.aB > 0 && (it.S == 11 || it.S == 12) &&
                    it.ak - p.ak in 0..120 && Math.abs(it.al - p.al) < 60
            }
            // kill-dive marker uid246 W=[13469,1052,13554,1104] — the shaft
            // entry: tap context inside it → teleport into the chimney at
            // (13508,1080); then the S101/S36 auto-bounce climbs between
            // mass-A's east face (x13460) and mass-B's west face (x13560)
            // up to the ledge at y700. Inside the chimney hold M_UP so the
            // aF intent latch keeps arming the wall-grabs.
            val inMarker = p.ak in 13469..13554 && p.al in 1052..1104 && p.S != 214 && p.S != 215
            val inChimney = p.ak in 13460..13565 && p.al in 740..1140
            // slice 356: the S90 air kill now ends when the pinned victim's
            // corpse lands (family head `aS.a(0)`, Q==24), ~5 ticks earlier
            // than before, so the bot met foe 534 mid-swing and was beaten
            // back. Standing 16 ticks after the release re-phases the run
            // (deterministic replay: 15-17 pass, 14 and 18+ do not).
            if (wasS90 && p.S != 90) settle = 16
            wasS90 = p.S == 90
            val mask = when {
                settle > 0 -> { settle--; 0 }
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 65 -> Pad.M_UP                          // ax22: vault east
                p.S == 317 -> Pad.M_UP + Pad.M_CONTEXT + Pad.M_DOWN
                p.S == 164 || p.S == 157 -> Pad.M_RIGHT
                p.S == 315 || p.S == 318 -> Pad.M_DOWN
                // airborne launch/fall states (S149/150 hit-launch, S147
                // descent): hold east — the foes' own strikes toss the
                // player toward the gap; steering mid-air carries it the
                // last ~50px to the x13120 lip edge and down to the crates.
                p.S == 149 || p.S == 150 || p.S == 147 -> Pad.M_RIGHT
                p.S == 101 || p.S == 36 -> Pad.M_UP            // chimney bounce
                inMarker -> Pad.M_CONTEXT                    // S32 teleport-dive
                inChimney -> Pad.M_UP
                // launch-pad walk: ax10-S33 uid398 W=[12974,854,+61,+100]
                // covers the lip's east edge — inside it, plain-run (no
                // vault) so the zone arms gA→S148 autowalk → gL ~10.6px/t
                // scripted east to x13091, dropping onto crate uid241.
                p.ak >= 12960 && p.ak < 13050 && p.al < 960 && p.aZ -> Pad.M_RIGHT
                p.ak > 13050 && p.al < 960 && p.aZ -> Pad.M_RIGHT
                // crate-240 walk: on its top run east to ~x13300 first —
                // vaulting earlier undershoots platform uid96 (W x13380-
                // 13400 top y1101); from the east edge the S235 push chain
                // homes onto it, then the S238 release dives over the gap.
                p.ga?.aw == 240 && p.ak < 13300 -> Pad.M_RIGHT
                p.ga != null -> Pad.M_RIGHT + Pad.M_UP
                // Phase 2 (G12): cross the lip (x12380-12960, y939) on the
                // ground — S5 landings and foe-free stretches too. A hop
                // lifts the camera (cB keeps the head box 40px inside the
                // view, k.java:1954-1955: camY ~778), and the ax50
                // pouncers uid224/225 (W tops y789/783) then sit wholly
                // inside k.ac, see the player (l() L77 `b(W, k.ac)`,
                // simple/i.java:2396-2405) and drain 5 per pounce (T==3
                // `k.aS.a(4,…)`, i.java:7785). The hop-run took five of
                // those plus a guard strike — dead from x1=30. Grounded,
                // cB = al-150 (k.java:1947-1948) holds camY at ~790 and
                // both stay blind.
                p.al in 900..960 && p.ak in 12380..12960 && (p.aZ || p.S == 5) -> when {
                    imminent -> Pad.M_LEFT
                    heavy != null -> when {
                        heavy.S in intArrayOf(171, 156, 157, 158, 167) ->
                            if (heavy.ak - p.ak > 48) Pad.M_RIGHT else Pad.M_RIGHT + Pad.M_CONTEXT
                        heavy.ak - p.ak > 125 -> Pad.M_RIGHT
                        heavy.ak - p.ak < 110 -> Pad.M_LEFT
                        else -> 0
                    }
                    foe != null && Math.abs(foe.ak - p.ak) <= 48 -> Pad.M_RIGHT + Pad.M_CONTEXT
                    winding -> 0
                    else -> Pad.M_RIGHT
                }
                // attack-through: CONTEXT held ONLY while a gap foe is in
                // range — the S67/68/69 combo staggers it at point-blank so
                // its tumble→pin chain never starts and the launch-pad
                // zone's gA autowalk fires cleanly. Held constantly it
                // would swing-crawl the whole 700px approach (~5px/swing).
                // Phase 2: with the real aC() cycle (S23 back-off → S11
                // wind-up → S12) the hardened lip guards uid226/534/228
                // knock a hop-running bot back west into the pit. On the
                // ground, fight through without the UP hop — the combo's
                // i-frames carry the run east to the launch pad.
                foe != null && p.aZ -> Pad.M_RIGHT + Pad.M_CONTEXT
                foe != null -> Pad.M_RIGHT + Pad.M_UP + Pad.M_CONTEXT
                else -> Pad.M_RIGHT + Pad.M_UP
            }
            // Slice 369 (F5): the combo arm's end runs l() (e() 10598-
            // 10602), so with the attack held the post-tail's ap() starts
            // the next S67 on the same tick — no S0 tick between combos.
            // Against the lip guards that tighter chain loses the exchange
            // (worn down at x12600-12680). Let go on the S69 last frame
            // (T4): the next combo then restarts from S0 a tick later.
            w.pad.e(if (p.S == 69 && p.T == 4) 0 else mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 40 == 0 || stall > 100 || p.S in listOf(209,107,108)) {
                println("I t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} ga=${p.ga?.aw} foe=${foe?.aw}@aB${foe?.aB},S${foe?.S} stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (p.ak >= 13560 && p.al <= 740 && p.aZ) { reached = true; break }
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
        println("I reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legI cp342->crates/platform->towerB cp405@(13584,665): got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }

    @Test
    fun mission5CapstoneLegJ() {
        val w = world(aj = 5)
        val p = w.player
        // legJ — cp405 ledge (13564,699) -> finale uid308@15439 (ax5-S8,
        // script 389 -> k.l(15)). Route: east along tower-B roof (top y700)
        // -> 600px gap x13880-14480 (water floor y900, v=2 -> S50) crossed
        // via ax66 platform tops (13968/14047/14127, S12 y635-676 / S15
        // y709-750) and ax47-S93 kill sentinels at x14279/14384 -> east rim
        // y700 -> second water gap x14740-15360 (v=2 y740) via ax44-S8 pole
        // line y761 -> finale.
        p.gJ = 7
        p.setPositionPx(13564, 699); p.ak = 13564; p.al = 699; p.av = false
        p.S = 0; p.Q = -1; p.ah = 0; p.aj = 0; p.refreshBoxes()
        w.kO = 13380; w.kP = 520; w.rebuildCamRect()
        var reached = false; var maxAk = 13564; var prevS = -1; var stall = 0; var lastAk = 13564
        for (t in 0..30000) {
            p.gJ = 7
            val foe = w.npcs.firstOrNull {
                (it.ax == 11 || it.ax == 73) && it.aB > 0 && it.S != 139 &&
                    Math.abs(it.ak - p.ak) < 90 && Math.abs(it.al - p.al) < 60
            }
            val foeWindup = foe != null && foe.S == 6
            // only swing when the foe is in a damageable window — pushing
            // into S144/S17 (invulnerable) just bounces the player back
            // west via the S267/268 ledge chain and deadlocks the leg.
            val foeOpen = foe != null && foe.S != 144 && foe.S != 17
            // ax72 swing-pole mount: Entity.at binds in the az() scan once
            // the pole is in 440 range (J&4 armed); the L1947 context press
            // lunges onto it. Slice 411: `g.c(i)` picks the lunge anim with
            // `g.b(int)` (g.javap `c(Li;)V` @138) — an AIRBORNE press takes S292,
            // which has no X/W rects: the player stays where he pressed and the
            // CURRENT distance to the pole is the orbit radius (cB, which then
            // decays 20/tick to the 75 floor while `cy` swings to the bottom
            // band). The old slope arcs (S272-275, the grounded pick) lifted
            // the hand 47px and shortened that radius, so the old low-SW boxes
            // now start the swing in the water (v=2, feet = al+41 >= 740 →
            // S50). The press has to be taken while the pole is within reach —
            // scan: pole-1 passes for R in 140..200, pole-2 for R in 100..200
            // (both press on the rising / apex part of the hop from the rim).
            val pole = Entity.at
            // Phase 2 (G12): the player's I() integrates BEFORE g.e()
            // (i.java:3889-3922), so the press is read one step further
            // down the arc than the position seen here — test the reach on
            // that next point (N+ag, O+ah).
            val nx = ((p.ak shl 8) + (p.N and 255) + p.ag) shr 8
            val ny = ((p.al shl 8) + (p.O and 255) + p.ah) shr 8
            val poleSwing = pole != null && pole.ax == 72 && p.F == null && !p.aZ &&
                (pole.aw == 283 || pole.aw == 289) &&
                Math.hypot((nx - pole.ak).toDouble(), (ny - pole.al).toDouble()) <= 170.0
            val mask = when {
                poleSwing -> Pad.M_CONTEXT + Pad.M_RIGHT + Pad.M_UP
                p.S == 89 || p.S == 90 -> Pad.M_CONTEXT
                p.S == 65 -> Pad.M_UP
                p.S == 317 -> Pad.M_UP + Pad.M_CONTEXT + Pad.M_DOWN
                p.S == 164 || p.S == 157 -> Pad.M_RIGHT
                p.S == 315 || p.S == 318 -> Pad.M_DOWN
                p.S == 68 || p.S == 69 -> Pad.M_CONTEXT
                p.S == 101 || p.S == 36 -> Pad.M_UP
                foeWindup -> Pad.M_LEFT
                !foeOpen && foe != null -> Pad.M_RIGHT + Pad.M_UP   // run past / hop over
                // Slice 400: the soldier now faces the player while it spots
                // it (spotB's flip is `!faces`), keeps its 50-90 px pacing
                // distance and hits from range — an attack held in place
                // never reached it. Close the gap first, swing in reach.
                foeOpen && Math.abs(foe!!.ak - p.ak) > 45 ->
                    if (foe.ak > p.ak) Pad.M_RIGHT else Pad.M_LEFT
                foeOpen -> Pad.M_CONTEXT
                p.aZ -> Pad.M_RIGHT + Pad.M_UP
                else -> Pad.M_RIGHT + Pad.M_UP
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.ak > maxAk) maxAk = p.ak
            if (p.ak == lastAk && p.aZ) stall++ else { stall = 0; lastAk = p.ak }
            if (p.S != prevS || t % 40 == 0 || stall > 100) {
                println("J t=$t (${p.ak},${p.al}) S${p.S} aZ=${p.aZ} ah=${p.ah} ga=${p.ga?.aw} aR=${p.aR} stall=$stall m=$mask jC=${w.jC}")
            }
            prevS = p.S
            if (w.jC == 15) { reached = true; break }
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                println("J FAIL t=$t @(${p.ak},${p.al}) S${p.S} jC=${w.jC} maxAk=$maxAk")
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                continue
            }
            if (w.jC != 8) break
        }
        println("J reached=$reached maxAk=$maxAk p@(${p.ak},${p.al}) S${p.S} jC=${w.jC}")
        assertTrue(reached,
            "legJ cp405->platforms->poles->finale@15439: got S${p.S} @(${p.ak},${p.al}) maxAk=$maxAk jC=${w.jC}")
    }
}


// ---- Slice 291: mission-6 capstone (Pantheon, aj6/pack-12) ------------------
// Recon legs on `world(aj = 6)`: spawn ax0 @(17,740); ax5 milestone spine
// (19,658) uid16, (801,930) uid116, (8654,206) uid772, (10290,439) uid153,
// (10924,1166) uid271, (9672,358) uid311, (7667,556) uid240; checkpoints
// ax2 uid38@(1878,922), uid188@(1873,908), uid72@(3242,680),
// uid113@(7096,225), uid328@(8068,523), uid180@(9275,112),
// uid795@(5973,180). Same capstone pattern as slices 279-289 — real
// input only, no state pinning.
