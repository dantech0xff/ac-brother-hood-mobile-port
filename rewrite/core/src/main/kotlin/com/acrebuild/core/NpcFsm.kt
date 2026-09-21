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

    /** ax11/73 record init (L120). `f` = the entity record fields. */
    fun initSoldier(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(17); e.aB = e.az
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
            0 -> {
                // L633 dormant/death arm (live portion): a(true), G(), P|=512,
                // ag=ah=0, aA=2. In the original the patrol activation comes
                // from the mission director/script (unmined); inferred slice-2
                // activation: a live soldier goes to patrol-walk i(2) once its
                // idle anim completes.
                e.ag = 0; e.ah = 0; e.P = e.P or 512
                e.aA = 2
                if (e.aB > 0 && e.animFinished()) e.setAnim(3)   // inferred (S3 sets k)
            }
            else -> {
                if (e.aA == 0 && e.animFinished()) e.setAnim(3)  // inferred
            }
        }
        // `aB()` melee application (subset): non-degenerate attackbox X
        // overlapping the player's W → `k.aS.a(player-falling?20:4, l,0,this)`
        player.refreshBoxes()
        e.refreshBoxes()
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
