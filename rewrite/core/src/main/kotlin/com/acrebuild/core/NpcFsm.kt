package com.acrebuild.core

import kotlin.math.abs

/**
 * Slice-2 NPC FSM: the ax11 soldier patrol subset of `i.I()` (simple
 * decompile `reconstructed-project/src/simple/i.java`), plus the shared
 * physics tail.
 *
 * Record → Z init (proven, `i.java` L120 arm, ax 11 and 73 share it):
 *   az=r8[17] (hp), aB=az, Z[14]=r8[4] (script), Z[0]=r8[10] (variant),
 *   Z[1]=0, Z[2]=-1, Z[3]=ak (patrol home), Z[4]=al, Z[5]=r8[7],
 *   Z[6]=r8[8] (patrol range cells left/right of home), Z[7]=r8[9] (link),
 *   Z[8]=r8[11], Z[15..18]=r8[12..15] (alert zone rect rel),
 *   Z[9..12]=absolute alert box (ak+Z15 .. ak+Z15+Z17, al+Z16 .. al+Z16+Z18),
 *   Z[19]=r8[18].  Initial anim `i(0)` (i.java `d(i)` case 11).
 *
 * Arms ported verbatim:
 * - L357 (S∈{2,3,92} patrol): S3 sets `k=true`, `ag=av?-512:512`, `aC=20`;
 *   while `k`, ledge-ahead `am()`/edge-ahead `aG()` route to the 3px nudge
 *   path; else the home-range check `((ak-Z[3])/20)` vs `Z[5]`/`Z[6]` gates
 *   `i(2)` + the 20-tick leg timer → `i(3)` + `av=!av` ping-pong.  Player
 *   spot (`b(k.aS)` simplified — see below) → `aA=1` + ax11 `i(5)`.
 * - L451 (S∈{4,22} chase): `Q()` face player; `aG()` edge → drop alert back
 *   to `i(k?3:2)`; `a(true)`; S4 runs at `ag=±2048`, others at `ag=±512`;
 *   hitbox overlap with player W → `aC=3; i(23)` (attack windup).
 * - L438 (S5 attack): on `r()` → `aq==0 → i(4)`; `aC=0`.
 * - L444 (S23 windup-approach): `ag=±512`; anim end → back to chase.
 * - L475 (S11?): `r()` → `i(12)`.  (S12 arm = contact/attack — replaced by
 *   windup→chase fallback; player damage via `aB()` requires the
 *   damage-floatie spawner — deferred.)
 * - L777 common tail reduced to: `h()` ledge-fall (`i(25)` when no ground
 *   under the anchor row or the row below while no platform link),
 *   gravity via `aj=1536` when `!aZ`.
 *
 * Simplifications (flagged): `b(i)` (LOS) → same-row within ±1 cell AND
 * player inside the Z[9..12] alert box AND NPC facing covers the player —
 * the original also gates on player stealth states not yet ported.
 * `aC()` chase-timeout: ported as the 60-tick countdown → `i(k?3:2)`.
 * `aE()` assassination QTE, `j()`/`k()` damage/stealth-kill intake, `aD()`
 * platform links — omitted this slice.
 */
class NpcFsm(private val world: LevelCellSource) {

    private val scratch = IntArray(4)

    companion object {
        // i clinit difficulty tables (proven, i.java static{}):
        // bu = {300,400,500} max hp, bw = {80,80,80} normal-hit dmg,
        // J = {100,100,100} assassin/heavy dmg, H = {50,50,50} counter line
        private val BU = intArrayOf(300, 400, 500)
        private val BW = intArrayOf(80, 80, 80)
        private val JD = intArrayOf(100, 100, 100)
        /** `H[0]` — counter/weakened line (proven value, index au=0). */
        private const val H0 = 50
        /** `g.b()` player-attack anim set (proven, L9/L10). */
        private val ATTACK_ANIMS = intArrayOf(
            67, 68, 69, 81, 112, 113, 114, 115,
            183, 184, 216, 217, 286, 287)
    }

    /** ax11/73 record init (L120). `f` = the entity record fields. */
    fun initSoldier(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(17)
        // original: aB = bu[k.au] — difficulty max HP (k.au unmined → index 0)
        e.aB = BU[0]
        e.Z[14] = rf(4)
        e.Z[0] = rf(10)
        e.Z[1] = 0; e.Z[2] = -1
        e.Z[3] = e.ak; e.Z[4] = e.al
        e.Z[5] = rf(7); e.Z[6] = rf(8)
        e.Z[7] = rf(9)
        e.Z[8] = rf(11)
        e.Z[15] = rf(12); e.Z[16] = rf(13); e.Z[17] = rf(14); e.Z[18] = rf(15)
        e.Z[9] = e.ak + e.Z[15]
        e.Z[10] = e.ak + e.Z[15] + e.Z[17]
        e.Z[11] = e.al + e.Z[16]
        e.Z[12] = e.al + e.Z[16] + e.Z[18]
        e.Z[19] = rf(18)
        e.setAnim(0)
    }

    // -- helpers ------------------------------------------------------------

    /** `Q()` — face the player. */
    private fun facePlayer(e: Entity, player: Entity) { e.av = player.ak < e.ak }

    /** `h(cx,cy)` — ground exists at cell (proven): <12 && >=5 → one-way;
     *  <5 → empty; else solid. */
    private fun h(cx: Int, cy: Int): Boolean {
        val v = world.collisionCell(cx, cy)
        return if (v < 12) v >= 5 else true
    }

    /** `am()` (i.java, proven): wall/ledge within 3 cells above foot row. */
    private fun wallAhead(e: Entity): Boolean {
        val cx = e.ak / 20 + (if (e.av) -1 else 1)
        for (r in 1..3) if (e.e(world, cx, e.al / 20 - r) >= 12) return true
        return false
    }

    /** `aG()` (i.java, proven core; platform `s` arms omitted): edge ahead. */
    private fun edgeAhead(e: Entity): Boolean {
        val cy = (e.W[3] + 10) / 20
        return if (!e.av) {
            // right side: W2/20+1 is 20 (OOB sentinel) or 5 → not an edge
            e.e(world, e.W[2] / 20 + 1, cy) != 20 &&
                e.e(world, e.W[2] / 20 + 1, cy) != 5
        } else {
            e.e(world, e.W[0] / 20 - 1, cy) != 20 &&
                e.e(world, e.W[0] / 20 - 1, cy) != 5
        }
    }

    /** `b(k.aS)` simplified: alert-zone box + facing + same band. */
    private fun seesPlayer(e: Entity, player: Entity): Boolean {
        // same-row within one cell of height
        if (abs(player.al - e.al) / 20 > 1) return false
        if (player.ak < e.Z[9] || player.ak > e.Z[10]) return false
        if (player.al < e.Z[11] || player.al > e.Z[12]) return false
        // facing must cover the player
        if (e.av && player.ak > e.ak) return false
        if (!e.av && player.ak < e.ak) return false
        return true
    }

    // -- per-tick ------------------------------------------------------------

    fun tick(e: Entity, player: Entity) {
        e.collideSides(world, true)
        when (e.S) {
            2, 3, 92 -> patrolArm(e, player)
            4, 22 -> chaseArm(e, player)
            5 -> if (e.animFinished()) { e.setAnim(4); e.aC = 0 }
            23 -> {
                // L444 windup-approach: ag=∓512 toward player; aF() done →
                // strike anim 12 (the contact arm at L478 runs next).
                facePlayer(e, player)
                e.collideSides(world, true)
                e.ag = if (e.av) -512 else 512
                if (e.animFinished()) e.setAnim(12)
            }
            12 -> {
                // L478 contact/counter arm (subset): while my X attackbox
                // overlaps the player's W hitbox, apply the melee ops
                // `aB()` issues — op 4 grounded / op 20 airborne. On anim end
                // back to chase (i(23)+aC=10 re-approach in the original).
                if (e.animFinished()) { e.setAnim(23); e.aC = 10 }
            }
            25 -> { /* fall — shared tail below */ }
            85 -> {
                // hit-react (i.java j() `c(85,157)`); anim end → resume
                // chase if alerted else patrol (subset of the real chain).
                e.ag = 0
                if (e.animFinished()) e.setAnim(if (e.aA != 0) 4 else 3)
            }
            139 -> {
                // corpse (proven L689): P&=~16 removes actor flag;
                // P|=32|64 freezes the last frame
                e.P = e.P and -17
                if (e.animFinished()) e.P = e.P or 32 or 64
            }
            144 -> {
                // C() weakened/block stance (i.java:1955): hold in place;
                // the player tap now queues the 183/184 finisher.
                e.ag = 0; e.ah = 0; e.P = e.P or 512; e.aA = 2
                if (e.aB <= 0) e.setAnim(0)
            }
            106, 107 -> {
                // victim of the assassination finisher (S183/184 player arm
                // drags us). r() → corpse flags; the player's arm sets aB=0.
                e.ag = 0; e.ah = 0; e.P = e.P or 512; e.aA = 2
                if (e.animFinished() || e.aB <= 0) {
                    e.P = e.P and -17; e.P = e.P or 32 or 64
                    if (e.aB <= 0) e.setAnim(0)
                    if (world.lockTarget === e) world.lockTarget = null
                }
            }
            0 -> {
                // L633 dormant/death arm (live portion): a(true), G(), P|=512,
                // ag=ah=0, aA=2. aB<=0 → corpse chain i(139) (proven L685).
                e.ag = 0; e.ah = 0; e.P = e.P or 512
                e.aA = 2
                if (e.animFinished()) {
                    when {
                        e.aB <= 0 -> e.setAnim(139)             // die (proven)
                        else -> e.setAnim(3)                    // inferred activation
                    }
                }
            }
            else -> {
                if (e.aA == 0 && e.animFinished()) e.setAnim(3)  // inferred
            }
        }
        // `j()` player→NPC damage intake (subset): live NPC + player in an
        // attack anim (`g.b()` set) + player X attackbox ∩ my W → Q() face +
        // aB -= dmg (216 insta-kill+launch, {183,184,217} J=100, else bw=80),
        // hit-react `i(85)`, aB<=0 → i(0) (death path goes through the L633
        // dormant arm → i(139) corpse on anim end).
        // lock claim (j() L7-L18, proven): a weakened ax11 (Z[0]==2)
        // registers itself as i.aN when no live target holds the lock
        // lock claim (j() L7-L18 uses Z[0]==2; level-0 soldiers carry
        // Z0==0 — inferred equivalent: HP at/below the H[au]=50 counter line)
        if (e.ax == 11 && e.aB > 0 && e.aB <= H0 &&
            (world.lockTarget == null || world.lockTarget!!.S == 18)) {
            world.lockTarget = e
        }
        if (world.lockTarget === e && e.aB <= 0) world.lockTarget = null
        e.refreshBoxes(); player.refreshBoxes()
        // S144 = block stance: immune to normal melee until the finisher
        // (inferred — that's the purpose of the counter-offer state)
        val blocking = e.S == 144
        if (!blocking && e.aB > 0 && player.X[0] != player.X[2] &&
            player.S in ATTACK_ANIMS && overlap(e.W, player.X)) {
            facePlayer(e, player)
            when (player.S) {
                // {183,184,216,217} assassin/heavy path: 216 launches and
                // zeroes HP; J[au]=100 damage otherwise (proven).
                216 -> {
                    e.aB = 0
                    e.ag = if (e.av) 5120 else -5120
                    e.ai = if (e.av) -2560 else 2560
                }
                183, 184, 217 -> {
                    e.aB -= JD[0]
                    if (e.ax == 11 && e.Z[0] == 0) e.aB = 30   // L83 survive
                }
                else -> e.aB -= BW[0]
            }
            // hit-react only for normal strikes; finisher anims (183/184/
            // 216/217) keep the victim pinned in its own arm (106/107/launch)
            if (player.S !in intArrayOf(183, 184, 216, 217)) e.setAnim(85)
            if (e.aB <= 0) e.setAnim(0)                          // aB<=0 → i(0)
            // C() weakened offer (proven shape, i.java:1955 — original
            // gates on Z[0]==1; level-0 soldiers use 0 → inferred same
            // transition for the low-HP weakened state)
            else if (e.ax == 11 && e.aB <= H0) {
                e.Z[0] = 2
                e.setAnim(144)
            }
        }
        // `aB()` melee application (subset): non-degenerate attackbox X
        // overlapping the player's W → `k.aS.a(player-falling?20:4, l,0,this)`
        if (e.X[0] != e.X[2] && overlap(player.W, e.X)) {
            player.applyHit(if (player.S == 43) 20 else 4, e.l, e, world)
        }
        // L777 common tail (subset)
        if (e.standingOn == null && !h(e.ak / 20, e.al / 20) &&
            !h(e.ak / 20, e.al / 20 + 1) && e.aZ) {
            e.setAnim(25)
        }
        if (!e.aZ) e.aj = 1536 else e.aj = 0
        e.integrate()
        // ground snap on landing
        if (e.ah >= 0) {
            e.probeCells(world)
            if (e.aR >= 12) {
                e.refreshBoxes()
                val top = ((e.W[3] + 1) / 20) * 20
                if (e.al > top - 1 - 20) {
                    e.al = top - 1; e.O = e.al shl 8; e.ah = 0; e.aj = 0
                }
            }
        }
        e.advanceAnim()
    }

    // -- L357 patrol (proven structure) ----------------------------------------
    private fun patrolArm(e: Entity, player: Entity) {
        if (e.S == 3) {
            e.k = true
            e.ag = if (e.av) -512 else 512
            e.aC = 20
        }
        if (e.k) {
            val home = e.Z[3]; val rangeL = e.Z[5]; val rangeR = e.Z[6]
            val inWalk: Boolean
            if (wallAhead(e) || edgeAhead(e)) {
                // L370: edge path — nudge away then still allowed to walk
                if (edgeAhead(e) && e.ag != 0) e.ak += if (e.av) -3 else 3
                inWalk = true
            } else {
                inWalk = if (((e.ak - home) / 20) >= -rangeL && e.av) true
                    else if (((e.ak - home) / 20) > rangeR) e.av
                    else false
            }
            if (inWalk) {
                if (e.platform == null || e.Z[7] <= 0) {
                    e.setAnim(2)
                    if (e.aC > 0) e.aC--
                    else { e.aC = 20; e.setAnim(3); e.av = !e.av }
                }
            }
        }
        // L412: player spot → alert (b() simplified)
        if (seesPlayer(e, player)) {
            e.aA = 1
            e.setAnim(5)
        }
    }

    // -- L451 chase (proven core) ---------------------------------------------
    private fun chaseArm(e: Entity, player: Entity) {
        facePlayer(e, player)
        if (edgeAhead(e)) {
            // edge ahead → drop alert, back to patrol
            e.aA = 0
            e.setAnim(if (e.k) 3 else 2)
            return
        }
        e.collideSides(world, true)
        e.ag = if (e.S == 4) (if (e.av) -2048 else 2048)
               else (if (e.av) -512 else 512)
        // overlap → attack windup
        player.refreshBoxes()
        if (overlap(player.W, e.W)) {
            e.aC = 3
            e.setAnim(23)
        }
        // aC() subset: chase timeout — lose the player for 60 ticks → patrol
        e.P = e.P or 16
        if (e.aC > 0) e.aC--
        else if (!seesPlayer(e, player) && e.aA != 0) {
            e.aC = 60; e.aA = 0; e.k = false; e.setAnim(2)
        }
    }

    private fun overlap(a: IntArray, b: IntArray): Boolean =
        a[0] < b[2] && a[2] > b[0] && a[1] < b[3] && a[3] > b[1]
}
