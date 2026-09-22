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
class NpcFsm(val world: LevelCellSource) {

    private val scratch = IntArray(4)

    /**
     * `au()` (i.java:7738, proven): a dead ax∈{11,17,73} teleports its
     * `Z[21]`-linked entity onto the corpse and clears its P&32
     * (inactive) flag — the drop pickup activates. One-shot via Z[21]=-1.
     */
    fun corpseDrop(e: Entity) {
        if (e.ax != 11 && e.ax != 17 && e.ax != 73) return
        if (e.aB > 0 || e.Z[21] == -1) return
        val link = world.npcs.firstOrNull { it.aw == e.Z[21] }
        if (link != null) {
            link.ak = e.ak; link.al = e.al; link.P = link.P and -33
        }
        e.Z[21] = -1
    }

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
        /** `k.bk` — ax67 prop kind→clip table (k.java:8444, proven). */
        private val BK = intArrayOf(
            24, 27, 27, 27, 34, 35, 37, 41, 64, 64, 65, 67, 49, 69, 70)
        /** ax67 clip resolved by kind (i.java:2633 `aa = k.r(bk[r8[7]])`). */
        fun decorClip(kind: Int): Int =
            if (kind >= 0 && kind < BK.size) BK[kind] else -1
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
        e.Z[21] = rf(19)              // corpse-drop linked uid (i.java:3061)
        e.setAnim(0)
    }

    /**
     * ax44 door/gate record init (i.java:3574 L377, proven):
     * `az=r8[7]` (display depth), `Z[0]=r8[8]` mode when >0 else 0,
     * `Z[1]=r8[9]` open timer, `Z[2]=r8[10]` closed timer, `Z[3]=0`
     * countdown, `Z[4]=r8[5]` base state, `Z[5]=r8[11]` linked ax58 uid.
     * Initial anim is the generic `i(r8[5])` (i.java:3733) — records with
     * f5==8 spawn in the static-crusher bank S8-13.
     */
    fun initDoor(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(7)
        e.Z[0] = if (rf(8) > 0) rf(8) else 0
        e.Z[1] = rf(9)
        e.Z[2] = rf(10)
        e.Z[3] = 0
        e.Z[4] = rf(5)
        e.Z[5] = rf(11)
        e.setAnim(rf(5))
    }

    /**
     * `bv()` door/gate FSM (i.java:16927, proven). Z[0] mode:
     * 0 = timed cycle (link absent or `ac.bf()` running), 1 = slaved to
     * the linked ax58's anim, 2 = proximity auto (player inside keeps the
     * countdown reloaded). States S0-7 = two cycling banks anchored at
     * Z[4] (closed/opening/open/closing); S8-13 = static crusher bank.
     * Crush arm (L64 + L75): player W ∩ door W while closed/closing/
     * static → `k.aS.aj=0; ah=0; i(50)` — S50 zeroes x[1] → k.l(12).
     */
    fun tickDoor(e: Entity, player: Entity) {
        // the original's t() recomputes W on every query; our cached copy
        // needs a per-tick refresh since doors never take the probe paths.
        e.refreshBoxes()
        when (e.S) {
            0, 2, 4, 6 -> {
                e.P = e.P or 16
                // link arm (L4): resolve Z[5] → ac once; ax58 with
                // S∈{0,5,7} promotes Z[0] to slaved mode.
                if (e.ac == null && e.Z[5] != -1)
                    e.ac = world.npcs.firstOrNull { it.aw == e.Z[5] }
                val ac = e.ac
                if (ac != null && ac.ax == 58 && (ac.S == 0 || ac.S == 5 || ac.S == 7))
                    e.Z[0] = 1
                when (e.Z[0]) {
                    0 -> if (ac == null || bf(ac)) {
                        if (e.Z[2] < 999 && e.Z[3] != -1) {
                            e.Z[3]--
                            if (e.Z[3] < 0) e.setAnim(e.S + 1)
                        }
                    }
                    1 -> if (ac != null) {
                        if (e.Z[4] == e.S) { if (!bf(ac)) e.setAnim(e.S + 1) }
                        else if (bf(ac)) e.setAnim(e.Z[4])
                    }
                    2 -> if (e.Z[4] == e.S) {
                        if (e.Z[3] == 0 && rectsOverlap(player.W, e.W))
                            e.Z[3] = if (e.S == 0 || e.S == 4) e.Z[2] else e.Z[1]
                        if (e.Z[3] > 0) { e.Z[3]--; if (e.Z[3] <= 0) e.setAnim(e.S + 1) }
                    }
                }
                if (e.S == 0 || e.S == 4) crush(e, player)
            }
            1, 5 -> if (e.animFinished()) { e.setAnim(e.S + 1); e.Z[3] = e.Z[1] }
            3, 7 -> {
                if (e.animFinished()) { e.setAnim(e.S - 3); e.Z[3] = e.Z[2] }
                crush(e, player)
            }
            in 8..13 -> crush(e, player)
        }
        // P&16 keeps the door unintegrated (static prop); anims still tick.
        e.advanceAnim()
    }

    /** `bf()` (i.java:14608, proven): ax58 anim-running — true iff its S is
     * one of {1,3,4,6,8,10,12}. */
    private fun bf(e: Entity): Boolean =
        e.ax == 58 && (e.S == 1 || e.S == 3 || e.S == 4 ||
                       e.S == 6 || e.S == 8 || e.S == 10 || e.S == 12)

    private fun rectsOverlap(a: IntArray, b: IntArray): Boolean =
        a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1]

    private fun crush(e: Entity, player: Entity) {
        if (!rectsOverlap(player.W, e.W)) return
        player.aj = 0; player.ah = 0
        player.setAnim(50)
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
            9 -> {
                // counter-stagger (from c(player)): decay the ±1536 knockback;
                // anim end → weakened offer if at/below the counter line,
                // else resume chase/patrol (arm shape inferred)
                e.ag = (e.ag * 3) shr 2
                if (e.animFinished()) {
                    if (e.ax == 11 && e.aB <= H0 && e.aB > 0) {
                        e.Z[0] = 2; e.setAnim(144)
                    } else {
                        e.setAnim(if (e.aA != 0) 4 else 3)
                    }
                }
            }
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
                        e.aB <= 0 -> { e.setAnim(139); corpseDrop(e) }
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

    // ==================================================================
    // ax10 — `i.aV()` TriggerController (i.java:11800-13181; semantics
    // reconstructed in docs/i-av-reconstruction.md). Level-0 carries 15
    // records using S ∈ {16,33,34,36,43,53}; this slice ports init L96
    // (i.java:2882) + the zone arms {33,34,36,43,53}. S16 (rope-attach,
    // L625) needs the unported g.a/k.an/k.ao/k.bI machinery — stubbed.

    /** Init arm L96 (i.java:2882): `aB=0; P|=512; az=0` then S-switch. */
    fun initTrigger(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.aB = 0
        e.P = e.P or 512
        e.az = 0
        // generic tail L395→L400→L414→L419 (i.java:3728-3705): S = i(r8[5])
        // and W = [ak+r8[7], al+r8[8], +r8[9], +r8[10]] (no X bound for ax10).
        e.setAnim(rf(5))
        e.W[0] = e.ak + rf(7); e.W[1] = e.al + rf(8)
        e.W[2] = e.W[0] + rf(9); e.W[3] = e.W[1] + rf(10)
        when (e.S) {
            // L102 (i.java:2961): Z = {r8[11], r8[13]}; P |= 16
            34 -> {
                e.Z[0] = rf(11); e.Z[1] = rf(13)
                e.P = e.P or 16
            }
            // L104 (i.java:2975): Z = {0} — our Z is already zeroed
            43 -> e.Z[0] = 0
            // L110 (i.java:2986): Z = {r8[20]} → falls through to L111
            16 -> { e.Z[0] = rf(20); l111(e, f) }
            // switch default → L111 (covers S33/S36/S53 and every unlisted S)
            else -> l111(e, f)
        }
    }

    /** L111 (i.java:2989): the common record-field map. */
    private fun l111(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.aE = rf(4); e.aF = rf(11); e.oId = rf(12)
        e.pv = rf(13); e.aG = rf(14); e.ay = rf(15)
    }

    /**
     * `aV()` zone arms used by level-0 records (i.java:12887+):
     * - S33 (L706): player ∩ W && player.S ∉ {148,149,150} → publish
     *   `g.B=av`, `g.l=(aG!=0 ? (aG-player.ak)<<8)/11 : 0)`, `g.A=true`;
     *   else `g.A=false`.
     * - S34: direct return (no-op state).
     * - S36 (L722): overlap publishes context zone `g.n/g.o/g.k/g.d` and
     *   sets `P|=16`; leaving clears only when this trigger still owns.
     * - S43 (L744): overlap && player.S ∈ {60,61} → `i(203)`.
     * - S53 (L886): `g.D && overlap && player.S ∈ {0,1,5}` → `i(360)`,
     *   `ag=ah=0`, `k.c(this)` remove. (g.D producer arm unported.)
     */
    fun tickTrigger(e: Entity, player: Entity) {
        when (e.S) {
            33 -> if (rectsOverlap(player.W, e.W)) {
                if (player.S != 148 && player.S != 149 && player.S != 150) {
                    player.gB = e.av
                    player.gL = if (e.aG != 0)
                        ((e.aG - player.ak) shl 8) / 11 else 0
                    player.gA = true
                }
            } else player.gA = false
            36 -> if (rectsOverlap(player.W, e.W)) {
                player.gn = e.W[0] + ((e.W[2] - e.W[0]) shr 1)
                player.go = e.W[3]
                if (e.aE >= 0) player.gk = e.aE
                player.gd = e
                e.P = e.P or 16
            } else if (player.gd == e) {
                player.gd = null
                player.gn = 0; player.go = 0; player.gk = -1
                e.P = e.P and -17
            }
            43 -> if (rectsOverlap(player.W, e.W) &&
                      (player.S == 60 || player.S == 61))
                player.setAnim(203)
            53 -> if (player.gD && rectsOverlap(player.W, e.W) &&
                      (player.S == 0 || player.S == 1 || player.S == 5)) {
                player.setAnim(360)
                player.ag = 0; player.ah = 0
                world.removeEntity(e)
            }
            // S16 (L625 rope-attach) and every other aV() state: unported.
        }
    }

    // ============================================================ ax4 = aj()
    // Destructible volume / attack hitbox (dispatch i.java:6682, proven).
    // Init arm L161 (i.java:3132): az=r8[11]; aD=r8[4]; aE=r8[5]; aF=r8[7];
    // n=r8[8]; m=r8[9]; p=r8[10]. r8[5]==5 → k.aq+=m (L165) + i=2 (L166);
    // r8[5]==7 → az=1, Z[4], P|=512 (L169); r8[5]==33 → aA=0, p<<=8.
    // Level 0: S ∈ {5×2, 6×1, 7×3, 9×14, 21×1} — 9/21 hit the default
    // no-op; only the aw-paired {5,7} claims and one 6 do anything.

    fun initDestructible(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(11); e.aD = rf(4); e.aE = rf(5); e.aF = rf(7)
        e.nl = rf(8); e.m = rf(9); e.pv = rf(10)
        // arm reads raw r8[5] — S isn't assigned until the L392 tail.
        if (rf(5) == 5) world.aq += e.m        // L165 (k.aq stat counter)
        if (rf(5) != 7) {                      // L164 → L166
            e.i = 2
            if (rf(5) == 33) { e.aA = 0; e.pv = e.pv shl 8 }
        } else {                               // L169: Z=int[4] alloc —
            e.az = 1                           // port's Z pre-exists; the
            e.P = e.P or 512                   // r8[5]!=0 fill is skipped.
        }
        e.setAnim(rf(5))                       // L392 tail: i(r8[5])
    }

    private val ctxZone = IntArray(4)

    /** `k.l()` (k.java:802): the 60px context bubble leading the player's
     *  facing — M = [ak±60 by av, al-60, +60, al]. */
    private fun ctxZone(p: Entity) {
        val x = if (p.av) p.ak - 60 else p.ak
        ctxZone[0] = x; ctxZone[1] = p.al - 60
        ctxZone[2] = x + 60; ctxZone[3] = p.al
    }

    fun tickDestructible(e: Entity, player: Entity) {
        e.advanceAnim()   // universal s() in the outer tick (i.java:6407)
        // W comes from clip3 rects via t() — refresh like ax44 (slice-19
        // pitfall: volumes never take the probe paths that recompute it).
        e.refreshBoxes()
        when (e.S) {
            5, 7 -> {
                // L5 (i.java:6733): player mid-attack + body overlap arms it.
                if (PlayerFsm.isAttackState(player.S)) {
                    if (rectsOverlap(player.W, e.W)) {
                        e.setAnim(e.S + 1); world.sfx(14); return
                    }
                } else {
                    ctxZone(player)
                    if (player.S == 37 || player.S == 38 ||
                        !rectsOverlap(e.W, ctxZone)) {
                        // L14: zone exit releases our claim (k.m()) and
                        // clears the marker popup (k.k(aw)).
                        if (world.claimed === e) {
                            world.clearClaim(); world.clearMarker(e.aw)
                        }
                    } else {
                        // in k.M: bid prio 5 (k.a) + marker popup (k.c).
                        world.claim(e, 5, e.W)
                        world.setMarker(e.ak, e.al - 85, e.aw)
                    }
                    pushOut(e, player)       // L18: a() solid-side helper
                }
                // L24: the player's attack hitbox reaching W also arms it.
                if (rectsOverlap(player.X, e.W)) {
                    e.setAnim(e.S + 1); world.sfx(14)
                }
            }
            6, 8 -> {
                // L28 (i.java:6760): on anim end, up to two `m(-1)` wisp
                // bursts per tick while m>0 (+k.o(5) stat, +k.s() shake),
                // then release the claim if we hold it and k.c(self).
                if (!e.animFinished()) return
                if (e.m > 0) {
                    world.spawnWisp(e); e.m--
                    world.apStats[5]++; world.shake()
                    if (e.m > 0) {
                        world.spawnWisp(e)
                        world.apStats[5]++; world.shake()
                        e.m--
                    }
                }
                if (world.claimed === e) world.clearClaim()
                world.removeEntity(e)
            }
            // S29/30/33 and every other aj() state: unported — level-0's
            // S9/S21 records fall to the same default no-op as upstream.
        }
    }

    // ============================================================ ax74 = bN()
    // Wisp/particle (i.java:21280). Only the arms a `m(-1)` burst reaches:
    // S1 (L20→L28): polar flight — radius j += 15/tick toward aE px along
    // angle aD (aF = aD*256/360 table index), anchored at (aq,ar); once the
    // radius saturates, aC drains then i(2) (sfx 15 when af.aG!=0).
    // S2 (L43): anchor on the player's head; die on anim end.
    /** `a()` side-push (i.java:914+, L48-63 ax4 path, proven): while the
     *  player is grounded (S<=43) and overlapping the volume, clamp their
     *  `ak` to its edge (dead ±1 `ag` nudge kept verbatim, L63 zeroes it).
     *  Guards that can't fire here omitted; `aS.y()` unported → treated
     *  false (inferred). */
    private fun pushOut(e: Entity, p: Entity) {
        if (e.S == 139) return
        if (e.S == 18 && p.S == 12) return
        if (e.S == 131 || e.S == 146) return
        if (!rectsOverlap(p.W, e.W)) return
        if (p.ga != null) return
        if (p.S > 43) return
        val pw = p.W[2] - p.W[0]; val ew = e.W[2] - e.W[0]
        if (p.ak <= e.ak) {
            if (p.ag >= 0) { p.ak = e.ak - pw / 2 - ew / 2; p.ai = 0; p.ag = -1 }
        } else if (p.ag <= 0) {
            p.ak = e.ak + pw / 2 + ew / 2; p.ai = 0; p.ag = 1
        }
        p.ag = 0                     // L63: aS.ag = 0 every overlapping tick
    }

    fun tickWisp(e: Entity, player: Entity) {
        e.advanceAnim()   // universal s()
        when (e.S) {
            1 -> {
                if (e.aA != 0) return            // L28 gate (aA==0 arm only)
                if (e.j >= e.aE) e.aC-- else e.j += 15
                e.aF = (e.aD * Trig.M) / 360
                e.setPositionPx(
                    e.aq + ((Trig.sin(e.aF) * e.j) shr 8),
                    e.ar + ((Trig.sin(Trig.N - e.aF) * e.j) shr 8))
                if (e.j >= e.aE && e.aC <= 0) {
                    e.setAnim(2)
                    e.af?.let { if (it.aG != 0) world.sfx(15); e.af = null }
                    e.aC = 0; e.aE = e.j; e.j = 0
                }
            }
            2 -> {
                e.P = e.P and -17
                e.setPositionPx(player.ak, player.al - 30)
                if (e.animFinished()) world.removeEntity(e)
            }
        }
    }

    private fun overlap(a: IntArray, b: IntArray): Boolean =
        a[0] < b[2] && a[2] > b[0] && a[1] < b[3] && a[3] > b[1]

    // ============================================================ ax41 = n()
    // Knockable prop (i.java:6414, proven): vases/crates the player knocks
    // into enemies. S3 → shared a() interact (pushOut); S4 settle — vel0,
    // sweep touching entities {0→i(9), 11→s-chain, 51→i(2)}, k.ae = aS at
    // T==frames-2, r() → k.c; S6 tumble — aj=1536, ah<=2560, wall-bounce,
    // entity impact → i(4); S5/default → L92 dropped label → no-op.

    /** `i(i)` — i.java:6655 (proven): W-overlap always; when the other is a
     *  "carrier" (player `g.a.ax==51`, or ax11 `s.ax==51`) also requires
     *  |al diff| <= 20. */
    private fun knockOverlap(e: Entity, o: Entity): Boolean {
        val carrier = when {
            o.ax == 0 -> o.ga?.ax == 51
            o.ax == 11 -> o.s?.ax == 51
            else -> false
        }
        if (carrier && kotlin.math.abs(o.al - e.al) > 20) return false
        return Entity.overlapI(o.W, e.W)
    }

    fun tickKnockable(e: Entity, w: Level0World, p: Entity) {
        when (e.S) {
            3 -> pushOut(e, p)                                        // L44 a()
            4 -> {                                                    // L4 settle
                e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
                // k.bd includes the player — w.npcs does not, append it.
                for (o in w.npcs + p) {
                    if (o === e) continue
                    val r0 = o.ax
                    if (r0 != 0 && r0 != 11 && r0 != 51 && r0 != 41 && r0 != 4)
                        continue                                    // L36
                    // `bd.W==null || this.W==null` — port W is never null;
                    // an all-zero box fails the overlap identically.
                    if (!Entity.overlapI(o.W, e.W)) continue
                    when (r0) {
                        0 -> o.setAnim(9)                             // L25
                        11 -> {                                       // L27
                            val s = o.s
                            if (s == null) o.setAnim(0)               // L30
                            else if (s.ax != 51 && e.S == 6) o.setAnim(7) // L34
                        }
                        51 -> o.setAnim(2)                            // L31
                        // 41 → L36 box-check only; 4 → inner default → L36.
                        // Inner case 15 is unreachable: the outer filter
                        // admits r0==4, not 15 — likely a decompiler
                        // constant swap; ported verbatim (inferred).
                    }
                }
                val clip = e.clip
                if (clip != null && e.T == clip.frameCount(e.S) - 2)
                    w.aeRef = p                                       // k.ae = k.aS
                if (e.animFinished()) w.removeEntity(e)
            }
            6 -> {                                                    // L46 tumble
                e.aj = 1536
                if (e.ah >= 2560) { e.ah = 2560; e.aj = 0 }
                e.bd = true
                e.collideSides(w, true)                               // a(true)
                if (e.bb) e.ag = -e.ag
                e.av = e.ag < 0                                       // L55-58
                val moving = e.ag != 0 || e.ah != 0
                if (knockOverlap(e, p) && moving) { e.setAnim(4); return }
                for (o in w.npcs) {
                    if (o === e) continue
                    if (o.ax != 51 && o.ax != 11 && o.ax != 41 && o.ax != 15)
                        continue                                      // L91
                    if (!knockOverlap(e, o) || !moving) continue
                    e.setAnim(4); return                              // L88 i(4)
                }
            }
            // case 5 + default → L92 dropped label → no-op (flagged).
        }
    }

    // ============================================================ ax66 = bm()
    // Moving platform / crate-ride-grab FSM (i.java:15899, proven).
    // S is the clip-anim index: pairs (6,24) board → (7,25) ride →
    // (8,26) stop → (9,27) sink → (10,28) reset; (11,12,13) is the
    // L113 crate-ride/grab arm; (14,15,16,18..23) the linked-kill and
    // timed return arms. `g.a` → `p.ga` (player's platform link),
    // `aS` → `p`, `b` = i.b ridden latch.

    /** `i.m(i)` (i.java:16311, proven): grab-eligible — ax51
     *  `S∈{0,1,6,8}` or ax66 `S∈{11,12,13}`. */
    private fun grabEligible(o: Entity): Boolean =
        (o.ax == 51 && (o.S == 0 || o.S == 1 || o.S == 6 || o.S == 8)) ||
        (o.ax == 66 && (o.S == 11 || o.S == 12 || o.S == 13))

    /** `i.n(i)` (i.java:16337, proven): player faces `o` and is within
     *  `|dx| < 180` (140 for ax66), `|dy| < 80`. */
    private fun grabInReach(o: Entity, p: Entity): Boolean {
        if (if (p.av) p.ak <= o.ak else p.ak >= o.ak) return false
        val dxLim = if (o.ax == 66) 140 else 180
        return Math.abs(p.ak - o.ak) < dxLim &&
               Math.abs(p.al - o.al) < 80
    }

    /** `i.bq()` (i.java:16363, proven): self is holding (ax51 S8 /
     *  ax66 S13) AND the player overlaps W. */
    private fun grabZone(e: Entity, p: Entity): Boolean {
        if (!((e.ax == 51 && e.S == 8) || (e.ax == 66 && e.S == 13)))
            return false
        return Entity.overlapI(p.W, e.W)
    }

    /** `i.bn()` (i.java:~16272, proven): player carries a claim →
     *  spawn the aid marker above this platform. */
    private fun platformSpawnAid(e: Entity, w: LevelCellSource, p: Entity) {
        if (p.ac == null) return
        e.spawnAeMarker(w, 1, e.ak, e.al - 60)
    }

    /** `i.br()` (i.java:~16395, proven): grab-check — refresh/steal the
     *  player's `ac` claim while overlapping or holding. `k.bd` scan →
     *  `w.npcs + p`. */
    private fun platformGrabCheck(e: Entity, w: LevelCellSource,
                                  p: Entity): Boolean {
        var r5 = false
        if (!Entity.overlapI(p.W, e.W) && !grabZone(e, p)) return false
        if (!grabEligible(e)) return false
        if (p.ac != null) {
            // L11/L13/L18 — ac==this or dead claim → release then scan;
            // live claim on ANOTHER eligible entity → register it.
            val ac = p.ac!!
            if (ac !== e && grabEligible(ac) && grabInReach(ac, p)) {
                w.registerClaim(ac, 1, ac.W)
                return true
            }
            p.ac = null                                          // L18
            if (w.kL != null && w.kL!!.aw == e.aw) w.claimReset()
        }
        // L23 — k.bd sweep
        for (o in w.npcs + p) {
            if (o === e || !grabEligible(o) || !grabInReach(o, p)) continue
            if (p.ac != null &&
                !((o.ax == 51 && o.S == 8) || (o.ax == 66 && o.S == 13)))
                continue                                            // L41
            // L45 — same-type-in-holding skip
            if (o.ax == 51 && o.S == 8 && e.ax == 51 && e.S == 8) continue
            if (o.ax == 66 && o.S == 13 && e.ax == 66 && e.S == 13) continue
            if (p.S != 252) p.ac = o                              // L61
            if (o.ax == 51) { if (o.S == 8) return r5 }             // L66
            else r5 = true                                          // L67
        }
        return r5                                                   // L70
    }

    /**
     * `i.bm()` (i.java:15899, proven) — the ax66 S-switch, transcribed
     * per label. The S11/12/13 L113 arm is the crate-ride/grab state
     * (~110 lines in the original).
     */
    fun tickPlatform(e: Entity, w: LevelCellSource, p: Entity) {
        run {
        when (e.S) {
            // -- L5 board: overlap + alive + not mid-throw → ride -----
            6, 24 -> {
                if (!Entity.overlapI(p.W, e.W)) return@run
                if (p.x1 <= 0) return@run                          // g.g()
                if (p.S == 50) return@run
                e.setAnim(if (e.S == 6) 7 else 25)                  // L14
                p.ga = e
                p.ag = e.ag; p.ah = e.ah; p.ai = e.ai; p.aj = e.aj
                if (p.S != 12 && p.S != 11) { p.al = e.al; p.setAnim(0) }
                e.Z[1] = e.Z[0]                                     // L19
            }
            // -- L21 ride: bind, clamp upward vel, S43 snap, timer ---
            7, 25 -> {
                if (!Entity.overlapI(p.W, e.W)) {
                    if (p.ga === e) p.ga = null                     // L34
                } else {
                    p.ga = e
                    if (p.ah > 0) p.ah = 0                          // L26
                    if (p.aj > 0) p.aj = 0                          // L29
                    if (p.S == 43) { p.al = e.al; p.setAnim(0) }    // L32
                    else if (p.S == 34 && p.ga === e) p.ga = null   // L34
                }
                e.Z[1]--                                            // L36
                if (e.Z[1] < 0 && e.animFinished())
                    e.setAnim(if (e.S == 7) 8 else 26)              // L44
            }
            // -- L46 stop: anim done → sink + release ---------------
            8, 26 -> {
                if (!e.animFinished()) return@run
                e.setAnim(if (e.S == 8) 9 else 27)                  // L51
                if (p.ga === e) { p.ga = null; p.flingAirborne(0, w) }
            }
            // -- L55 sink until the floor cell ----------------------
            9, 27 -> {
                if (e.e(w, ((e.Y[0] + e.Y[2]) shr 1) / 20,
                        e.al / 20 + 1) >= 12) {                     // L57
                    e.setAnim(if (e.S == 9) 10 else 28)
                    e.ah = 0; e.aj = 0
                } else {
                    e.al += 10; e.ah = 2560; e.aj = 1536
                    if (p.ga === e) p.flingAirborne(2560, w)
                }
            }
            // -- L65 reset: teleport home → board state -------------
            10, 28 -> {
                if (!e.animFinished()) return@run
                if (p.ga === e) p.ga = null                         // L69
                e.ak = e.Z[2]; e.al = e.Z[3]
                e.setAnim(if (e.S == 10) 6 else 24)                 // L73
            }
            // -- L113 crate-ride / grab arm (S11/12/13) -------------
            11, 12, 13 -> {
                if (p.S == 252 && p.ga === e) { p.ga = null; return } // L115
                platformGrabCheck(e, w, p)                          // br()
                // L121 — falling into the grab zone mid-236/239 → aid
                if (Entity.overlapI(p.W, e.X) && p.ah > 0 &&
                    (p.S == 236 || p.S == 239)) platformSpawnAid(e, w, p)
                val r8 = if (e.Z[4] != -1) w.findByAw(e.Z[4]) else null
                // L131/L143 — player boards/attacks onto the crate
                if (p.ga !== e && e.S != 13 &&
                    Entity.overlapI(p.W, e.W) &&
                    (w.playerAttacking() || p.S == 236 || p.S == 239)) {
                    if (r8 != null && r8.ax == 66 && r8.S == 16) {
                        p.flingAirborne(p.ah, w); p.ga = null       // L143
                    }
                    if (p.al > e.W[3]) {                            // L150
                        p.setAnim(209); p.ag = 0; p.ah = 0; p.ak = e.ak
                    } else {                                        // L152
                        if (p.S == 236 || p.S == 239) {
                            p.setAnim(if (p.S == 236) 237 else 240)
                            p.ah = 0; p.ak = e.ak                   // L159
                        } else {
                            p.ag = 0; p.ah = 0
                            if (e.S == 12) {
                                p.ak = e.ak; p.al = e.al
                                p.setAnim(if (e.Z[0] > 0) 228 else 358)
                            } else p.setAnim(0)                     // L166
                        }
                    }
                    p.al = e.al                                     // L168
                    if (e.S != 13) p.ga = e                         // (S13 gate)
                    platformSpawnAid(e, w, p)                       // L171
                } else if (e.S == 13 &&                             // L173
                    Entity.pointInBox(p.ak, p.al, e.W) &&
                    (p.ac == null || p.ac === e)) {
                    if (p.S == 236 || p.S == 239)                   // L184
                        p.flingAirborne(p.ah, w)
                }
                // L186 — grab/release input (16388 or dir-toward-av)
                val grabKey = w.padDown(16388) ||
                    (p.av && w.padDown(2)) || (!p.av && w.padDown(8))
                if (grabKey &&
                    (p.ga === e || grabZone(e, p))) {               // L196
                    // L200/L204 — eligibility gates
                    val acNullBlocked = p.ac == null && e.S == 13
                    val blocked = acNullBlocked ||
                        p.S == 238 || p.S == 235 || p.S == 239 ||
                        p.S == 236 || p.S == 43 || p.S == 252 ||
                        p.S == 147 || p.isHolding()
                    if (!blocked) {
                        // L224-L227 — face the claim when one exists
                        val ok = p.ac == null ||
                            p.av == (p.ac!!.ak < p.ak)
                        if (ok) {
                            p.setAnim(if (p.S == 237) 238 else 235) // L227
                            e.releaseAe()                           // L230 G()
                        }
                    }
                }
                // L232 — drift release (ran in every path)
                if (p.ga === e && !Entity.overlapI(p.W, e.X) &&
                    p.S != 209) { p.ga = null; e.releaseAe() }
            }
            // -- L241 → S20 after aC countdown ----------------------
            14 -> { e.aC--; if (e.aC <= 0) e.setAnim(20) }
            // -- L75 link-check: target crate holds player → arm ---
            15 -> {
                if (!Entity.overlapI(p.W, e.W)) return@run
                if (e.Z[0] == -1) return@run
                val r03 = w.findByAw(e.Z[0])
                if (r03 == null || r03.ax != 66 || r03.S != 12) return@run
                if (p.ga !== r03) return@run
                e.setAnim(16)
            }
            // -- L88 kill-linked: flag + drain the linked crate -----
            16 -> {
                val r0 = if (e.Z[0] != -1) w.findByAw(e.Z[0]) else null
                if (r0 != null && r0.ax == 66 && r0.S == 12 &&
                    p.ga !== r0) w.removeEntity(r0)                 // L98 arm
                if (!e.animFinished()) return@run
                if (r0 != null && r0.ax == 66 && r0.S == 12) {      // L102
                    if (p.ga === r0) p.ga = null
                    w.removeEntity(r0)
                }
                e.P = e.P or 64; e.P = e.P or 32                    // L111
            }
            17, 23 -> { /* → L288 tail only */ }
            // -- L239: arm the aC=Z[0] countdown --------------------
            18 -> if (e.animFinished()) { e.setAnim(14); e.aC = e.Z[0] }
            // -- L248: timed return / attack-grab (S19 timed, 20-22
            //    anim-done → i(18) + release) ------------------------
            19, 20, 21, 22 -> {
                if (p.ga !== e) {                                   // L254
                    if (w.playerAttacking() &&
                        Entity.overlapI(p.W, e.W)) {
                        p.setAnim(if (p.S == 264) 262 else 260)     // L260
                        p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0      // L261
                        p.ak = e.ak; p.al = e.al; p.ga = e
                    }
                }
                if (p.ga === e && !Entity.overlapI(p.W, e.W) &&     // L263
                    p.S != 261 && p.S != 259) p.ga = null
                if (e.S == 19) {                                    // L274
                    if (e.Z[1] >= 0) { e.aC--; if (e.aC <= 0) e.setAnim(21) }
                } else if (e.animFinished() && e.S != 22) {         // L281
                    e.setAnim(18)
                    if (p.ga === e) { p.ga = null; p.flingAirborne(2560, w) }
                }
            }
            else -> { /* default → L288 tail */ }
        }
        }
        if (e.S in 6..10 || e.S in 24..28) e.b = false         // L288/L295
    }

    // ============================================================ ax51 = bs()
    // Pushable crate (i.java:16440, proven): land-mount arm, edge-input
    // grab (k.v — EDGE unlike bm()'s held k.u), carry clamps, S2 death.

    /** `i.bo()` (i.java:16274, proven): `g.j` latch && overlap &&
     *  `g.a == null` — player pressed against the crate while j-set. */
    private fun crateContact(e: Entity, w: LevelCellSource,
                             p: Entity): Boolean =
        w.gj && Entity.overlapI(p.W, e.W) && p.ga == null

    /** `i.bp()` (i.java:16288, proven): `g.j` && `aS.S∈{43,35}` &&
     *  `ah>0` && `W[0] < aS.ak < W[2]` && `aS.al <= W[3]` — falling onto
     *  the crate's top while j-set. */
    private fun crateLandSpot(e: Entity, w: LevelCellSource,
                              p: Entity): Boolean =
        w.gj && (p.S == 43 || p.S == 35) && p.ah > 0 &&
        p.ak > e.W[0] && p.ak < e.W[2] && p.al <= e.W[3]

    fun tickPushable(e: Entity, w: LevelCellSource, p: Entity) {
        if (p.S == 284) return                                     // L6
        if (e.claimActive()) e.runClaimScript(w)                   // ab()→aa()
        // L9/L22 — walked off while carried → release
        if (p.ga === e && !Entity.overlapI(p.W, e.W) &&
            p.S != 235 && p.S != 238 && p.S != 50 && p.S != 9) {
            p.ga = null; e.releaseAe()
        }
        // L22/L30 — land-mount arm (g.j clears it entirely)
        if (!w.gj &&
            (p.al < e.W[3] || (p.ah > 0 && w.gc === e))) {         // L22
            if (p.ga == null &&
                p.S != 146 && p.S != 236 && p.S != 239 &&
                p.S != 235 && p.S != 238 && p.S != 237 &&
                p.S != 240 && p.S != 0 && p.S != 277 &&
                Entity.overlapI(p.W, e.W) && e.S != 8) {           // L30
                if (p.S == 16 || p.Q == 16 ||
                    ((e.al - p.gy) / 20) < 20 || w.gH()) {         // L62
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                    p.setAnim(0); w.iBq = 0; p.ga = e
                } else {                                          // mount
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                    p.al = e.W[1] + 4
                    p.applyHit(21, 0, e, w)
                    p.ga = e
                }
            }
        }
        // L64/L72/L80/L82 — br() gate: runs when aS not mid-grab-set, or
        // when riding+claiming this crate; br()==false -> G() release
        val grabSet = p.S == 236 || p.S == 239 || p.S == 235 || p.S == 238
        if (!grabSet || (p.ga === e && p.ac === e)) {
            if (platformGrabCheck(e, w, p))
                e.spawnAeMarker(w, 1, e.ak, e.al - 85)             // L80
            else e.releaseAe()                                   // L82
        }
        // L84/L94 — board arm: ac null or claimed-on-this + S236/239
        if (p.ga !== e && e.S != 8 && (p.ac == null || p.ac === e) &&
            Entity.overlapI(p.W, e.W) &&
            (p.S == 236 || p.S == 239)) {
            p.setAnim(if (p.S == 236) 237 else 240)                // L103
            p.ah = 0; p.ag = 0; p.ga = e
            if (p.av) {                                           // L113
                if (p.W[0] < e.W[0]) p.ak += 20
            } else if (p.W[2] > e.W[2]) p.ak -= 20
        }
        // L116/L126 — EDGE-input grab (k.v, unlike bm()'s k.u)
        val grabEdge = w.padHeld(16388) ||
            (p.av && w.padHeld(2)) || (!p.av && w.padHeld(8))
        if (grabEdge && (p.ga === e || grabZone(e, p))) {          // L126
            if (p.S != 238 && p.S != 235 && p.S != 239 &&
                p.S != 236 && p.ac != null &&
                p.av == (p.ac!!.ak < p.ak) &&                     // L142
                !p.isHolding() &&
                (p.S == 0 || p.S == 1 || p.S == 12)) {             // L153
                p.setAnim(if (p.S == 237) 238 else 235)            // dead-237 kept
            }
        }
        // L157 — carry: shift player with the crate + side pin
        if (p.ga === e) {
            e.refreshBoxes()                                       // t()
            p.ak += e.ag shr 8
            if (p.S == 7 || p.S == 32 || p.S == 12 || p.S == 0) {
                if (p.av) {                                       // L167
                    if (p.W[0] < e.W[0]) {
                        p.ag = 0; p.ak = (p.ak - p.W[0]) + e.W[0]
                    }
                } else if (p.W[2] > e.W[2]) {                     // L172
                    p.ag = 0; p.ak = (p.ak - p.W[2]) + e.W[2]
                }
            }
        }
        // L175 — S-switch
        when (e.S) {
            0, 1 -> {                                             // L177
                // bp/bo bookkeeping on g.c
                if (!crateLandSpot(e, w, p) && !crateContact(e, w, p)) {
                    if (w.gc === e) w.gc = null                   // L184
                } else if (w.gc == null || w.gc !== e) {          // L188
                    if (p.P and 1024 == 0) w.gc = e               // L192
                }
                if (p.ga === e) {                                 // L195/L197
                    if (!Entity.overlapI(p.W, e.W) &&
                        p.S != 50 && p.S != 9) {
                        e.P = e.P and -17; p.ga = null; e.releaseAe()
                        return
                    }
                    if (p.S != 236 && p.S != 239 && p.S != 235 &&
                        p.S != 238) {                             // L204-210
                        e.refreshBoxes()
                        p.ak += e.ag shr 8
                        p.al = e.W[1] + 4
                    }
                }
            }
            2 -> {                                                // L213
                e.releaseAe()
                if (!Entity.overlapI(p.W, e.W)) {
                    e.P = e.P and -17; p.ga = null; e.releaseAe()
                }
                if (e.animFinished()) { e.P = e.P or 64 or 32 }   // L217
            }
        }
    }

    // ============================================================ ax22 = aN()
    // Capture zone (i.java:10167, proven): overlap + g.b(S) anim gate
    // snaps the player to the anchor + enters anim 65; S1 pins vel and
    // exits on edge input — vault out ±3328/-3840 by Z[2], or (Z[1])
    // drop-through on down-edge to the zone floor. Z[3] facing:
    // -1 keep, 0 → av=true, else av=false.

    fun tickZoneInteract(e: Entity, w: LevelCellSource, p: Entity) {
        if (!Entity.overlapI(p.W, e.W) && e.S != 0) e.setAnim(0)
        when (e.S) {
            0 -> {                                        // L10
                if (!p.gB()) return                       // g.b(S) gate
                if (!Entity.overlapI(p.W, e.W)) return    // L49 (dropped)
                p.ak = e.ak; p.al = e.al                  // L12 snap
                p.ah = 0; p.ag = 0
                if (e.Z[3] != -1) p.av = e.Z[3] == 0      // L14/L18/L19
                p.setAnim(65)                             // L20
                e.setAnim(1)
                return
            }
            1 -> {                                        // L22
                p.ah = 0; p.ag = 0
                val r7 = if (e.Z[2] != 0) 16396 else 16390  // L25
                if (w.padHeld(r7)) {                      // L27 edge vault
                    p.ak += if (e.Z[2] != 0) 20 else -20  // L31/L32
                    p.al -= 20
                    p.ag = if (e.Z[2] != 0) 3328 else -3328  // L35/L36
                    p.ah = -3840
                    p.av = e.Z[2] == 0                    // L39/L40
                    p.setAnim(19)
                    e.setAnim(0)
                    w.clearLatches()                      // k.v()
                }
                // L42 — down-exit arm (Z[1] gated)
                if (e.Z[1] == 0) return
                if (w.padHeld(33024)) {                   // L44
                    p.flingAirborne(0, w)                 // aS.a(0)
                    p.refreshBoxes()                      // t()
                    p.al = (e.W[3] + (p.al - p.W[1])) + 2 // floor snap
                    e.setAnim(0)
                    return
                }
                return                                    // L51
            }
        }
    }

    // ============================================================ ax5  = aq()
    // Mission-logic entity (i.java:7343-7605, proven transcription): the
    // script/milestone host — invisible clip (bi[5]=1, no module bitmaps).
    // S arms:
    //   3  — countdown event: proximity arms `b(n)` → `aC = aF·n` ticks;
    //        expiry runs `O()` disarm + P|32 + k.c removal.
    //   4  — kill zone: `aS.S!=50 && !g.g() && overlap` → k.l(15) mission win.
    //   8  — Z[1]-mode watcher on linked entity `k.q(Z[2])`: P-bit/mode
    //        checks → L169 resolve (`!ab → ao()` bind, `ab → aa()` step).
    //   9  — Z[1]==16 claim stepper: overlap + `k.q(Z[2])==k.C && ab` → bI.
    //   10 — k.l(12) mission-fail zone.
    // Record init (i.java:3162 L180, `case 5 → L180` at i.java:2659):
    //   az=300, aE=r8[4], aF=r8[7], n=r8[8], aG=r8[9], aD=r8[10], m=r8[11],
    //   P|=128, Z[0..3]=r8[14..17]; Z[3]!=-1 → P|16; S==8 && aG!=-1 &&
    //   Z[1]<16 → h(k.s(aG)) pre-bind + P|512; S==9 → P|512. i(r8[5]) tail.

    /** `i.j(i)` (i.java:7318, proven): linked-watch predicate — `r` null →
     *  true; ax ∈ {11,17,29,27} && `!P()` (still alive) → true. */
    private fun iJ(r: Entity?): Boolean {
        if (r == null) return true
        if ((r.ax == 11 || r.ax == 17 || r.ax == 29 || r.ax == 27) &&
            !r.deadRelease()) return true
        return false
    }

    /** `i.ao()` (i.java:7249, proven): context bind — player-overlap +
     *  aG!=-1 + Z[0] gate (Z0==1 requires the 65568 edge); sets `aS.P`
     *  facing bit by `aS.av`, then the `k.C` slot claim = `N()` body. */
    private fun eventBind(e: Entity, w: LevelCellSource, p: Entity) {
        if (!Entity.overlapI(p.W, e.W)) return               // L5
        if (e.aG == -1) return                               // L7
        when (e.Z[0]) {                                      // L7/L11
            0 -> {}
            1 -> { if (!w.padHeld(65568)) return }
            else -> return
        }
        if (p.av) p.P = p.P or 1 else p.P = p.P and -2       // L13/L15/L17
        e.bindContext(w)                                     // L19/L21 = N()
    }

    /** `i.ap()` (i.java:7300, proven): forward the `k.s(Z[3])` script to
     *  the `k.q(aG)`-linked entity when its `cd[5]` flag allows; on fire
     *  the zone removes itself (`k.c(this)`). */
    private fun forwardScript(e: Entity, w: LevelCellSource) {
        val r0 = w.findByAw(e.aG) ?: return                  // L5
        if (w.kSIndex(e.Z[3]) == -1) return                  // L7
        if (r0.cd[5]) {                                      // L9/L15
            r0.bindScript(w.kSIndex(e.Z[3]), w)
            r0.scriptKeyStep(w.kSIndex(e.Z[3]), w)
            w.removeEntity(e)
        }
    }

    /** `aq()` L169/L172 (i.java:7592, proven): the resolution tail —
     *  `!ab → ao()` binds the context, `ab → aa()` steps the script. */
    private fun missionResolve(e: Entity, w: LevelCellSource, p: Entity) {
        if (!e.claimActive()) eventBind(e, w, p)             // ao()
        if (e.claimActive()) e.runClaimScript(w)             // aa()
    }

    /**
     * ax5 record init — L180 arm (i.java:3162, proven). `rf(i)` = record
     * field i (bounds-guarded 0). `i(r8[5])` anim + the S8 pre-bind
     * (`h(k.s(aG))` + P|512) and the S9 P|512 arm.
     */
    fun initMissionLogic(e: Entity, f: List<Int>, w: LevelCellSource) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = 300
        e.aE = rf(4)
        e.aF = rf(7)
        e.eventN = rf(8)
        e.aG = rf(9)
        e.aD = rf(10)
        e.m = rf(11)
        e.P = e.P or 128
        for (i in 0..3) e.Z[i] = rf(14 + i)
        if (e.Z[3] != -1) e.P = e.P or 16
        // L395→L414 ax5 arm (i.java:3719-3727, proven): `i(r8[5])` then
        // `W = [ak, al, ak+r8[12], al+r8[13]]` — ax5 gets its OWN W fill
        // (not the ax10/37/42 r8[7..10] arm, not t()'s clip rects).
        e.setAnim(rf(5))
        e.W[0] = e.ak; e.W[1] = e.al
        e.W[2] = e.W[0] + rf(12); e.W[3] = e.W[1] + rf(13)
        if (e.S == 8 && e.aG != -1 && e.Z[1] < 16) {         // L184
            e.bindScript(w.kSIndex(e.aG), w)
            e.P = e.P or 512
        }
        if (e.S == 9) e.P = e.P or 512                       // L191
    }

    /** `i.aq()` (i.java:7343, proven transcription) — the ax5 S-switch. */
    fun tickMissionLogic(e: Entity, w: LevelCellSource, p: Entity) {
        when (e.S) {
            // ---------- S3 — countdown event ----------
            3 -> {                                           // L5
                if (w.iAH) {                                 // armed → tick
                    e.aC--
                    if (e.aC > 0) return                     // L223
                    e.eventDisarm(w)                         // O()
                    e.P = e.P or 32
                    w.removeEntity(e)                        // k.c(this)
                    return
                }
                // L11 — proximity arm
                if (!Entity.overlapI(p.W, e.W)) return       // L224
                e.eventArm(e.eventN, w)                      // b(this.n)
                if (w.iAI <= 0) w.iAI = 1                    // L15
                e.aC = e.aF * e.eventN
                return
            }
            // ---------- S4 — kill zone ----------
            4 -> {                                           // L178
                if (p.S == 50) return                        // L180
                if (w.gG()) return                           // player dead
                if (!Entity.overlapI(p.W, e.W)) return       // L228
                w.screenL(15)                                // k.l(15)
                return
            }
            // ---------- S10 — mission-fail zone ----------
            10 -> { w.screenL(12); return }                  // L175
            // ---------- S9 — claim stepper ----------
            9 -> {                                           // L186
                if (e.Z[1] != 16) return                     // L229
                if (!Entity.overlapI(p.W, e.W)) return       // L188
                if (e.Z[2] == -1) return                     // L231
                val r02 = w.findByAw(e.Z[2]) ?: return       // L194
                if (r02 !== w.kC) return                     // L196 gate
                if (r02.claimActive()) r02.releaseClaim(w)   // L196/L234
                return
            }
            // ---------- S8 — Z[1]-mode linked watcher ----------
            8 -> {                                           // L18
                if (e.Z[2] == -1) { missionResolve(e, w, p); return }  // L169
                val r0 = w.findByAw(e.Z[2])
                when (e.Z[1]) {
                    0 -> {                                   // L22/L29
                        if (r0 != null && !iJ(r0) && r0.animFinished())
                            return                           // L22 fall-out
                        if (r0 == null || r0.ax == 73 || r0.ax == 17 ||
                            r0.ax == 11 || r0.ax == 29)
                            missionResolve(e, w, p)          // L29→L169
                        return
                    }
                    1 -> {                                   // L40/L42
                        if (r0 == null) return
                        if (r0.P and 16 != 0) missionResolve(e, w, p)
                        return
                    }
                    2 -> {                                   // L45/L47
                        if (r0 == null) return
                        if (r0.P and 32 != 0) missionResolve(e, w, p)
                        return
                    }
                    3 -> {                                   // L50/L65
                        if (e.claimActive()) { missionResolve(e, w, p); return }
                        if (r0 == null || r0.ax == 73 || r0.ax == 17 ||
                            r0.ax == 11 || r0.ax == 29 || r0.ax == 27) {
                            if (r0 == null ||
                                (iJ(r0) && r0.animFinished()))  // L69/L71
                                eventBind(e, w, p)           // L72 ao()
                        }
                        return
                    }
                    4 -> {                                   // L75/L79
                        if (e.claimActive()) { missionResolve(e, w, p); return }
                        if (r0 == null) return               // L205
                        if (r0.P and 16 != 0) eventBind(e, w, p)
                        return
                    }
                    5 -> {                                   // L83/L87
                        if (e.claimActive()) { missionResolve(e, w, p); return }
                        if (r0 == null) return               // L207
                        if (r0.P and 32 != 0) eventBind(e, w, p)
                        return
                    }
                    10 -> {                                  // L91/L93
                        if (r0 != null &&
                            (!iJ(r0) || !r0.animFinished()))
                            missionResolve(e, w, p)          // L169
                        return
                    }
                    11 -> {                                  // L100
                        if (r0 == null || r0.P and 16 == 0)
                            missionResolve(e, w, p)
                        return
                    }
                    12 -> {                                  // L105
                        if (r0 == null || r0.P and 32 == 0)
                            missionResolve(e, w, p)
                        return
                    }
                    13 -> {                                  // L110/L118
                        if (r0 == null || (iJ(r0) && r0.animFinished())) {
                            if (e.claimActive()) e.releaseClaim(w)  // bI()
                        }
                        missionResolve(e, w, p)              // → L169
                        return
                    }
                    14 -> {                                  // L121
                        if (r0 != null && r0.P and 16 != 0 &&
                            e.claimActive()) e.releaseClaim(w)
                        missionResolve(e, w, p)              // → L169
                        return
                    }
                    15 -> {                                  // L128
                        if (r0 != null && r0.P and 32 != 0 &&
                            e.claimActive()) e.releaseClaim(w)
                        missionResolve(e, w, p)              // → L169
                        return
                    }
                    17 -> {                                  // L135/L143
                        if (r0 == null || (iJ(r0) && r0.animFinished())) {
                            if (e.aG != -1 && e.Z[3] != -1)
                                forwardScript(e, w)          // ap()
                        }
                        return                               // L213
                    }
                    18 -> {                                  // L149/L153
                        if (r0 == null || r0.P and 16 == 0) return
                        if (e.aG != -1 && e.Z[3] != -1)
                            forwardScript(e, w)              // ap()
                        return                               // L217
                    }
                    19 -> {                                  // L159/L163
                        if (r0 == null || r0.P and 32 == 0) return
                        if (e.aG != -1 && e.Z[3] != -1)
                            forwardScript(e, w)              // ap()
                        return                               // L221
                    }
                    else -> missionResolve(e, w, p)          // 6,7,8,9,16,
                                                           // default → L169
                }
            }
            else -> return                                   // L222 — 5,6,7
        }                                                    //  + default
    }

    // ============================================================ ax27 = bL()
    // Timed/interactive prop (i.java:20843-21068, proven transcription):
    // fuse barrel + message trigger family. `k.aD` = the HUD fuse-bar
    // singleton (`120*(Z[1]-Z[2])/Z[1]` drawn at k.java:4078); `k.aO`/`k.aP`
    // = HUD message countdown + string (`aO -= j.f` per tick, k.java:5527).
    // Record init (L197, i.java:3213): az=1, aA=r8[7], aB=30,
    // Z={r8[8], r8[9]*1000, 0, r8[10]}; S==4 → P|4096; S==15 falls into
    // the L203 (ax29) arm → az=100, aB=800, aD=2, m=2, aC=30, aF=30,
    // n=60, Z={r8[4],r8[7],r8[8],r8[9],r8[10]} + k.aU=this.
    // Level-0 records: 4×S16 + 2×S18 (message display + hold arms).
    // Clip: bi[27]=48 (k.java:8442).

    /** `i.h(i)` (i.java:20791, proven): dangerous on-screen enemy —
     *  `W ∩ k.ac` && (ax∈{17,50} → true; ax∈{11,73} → `!P() && aA>=1`). */
    private fun enemyDanger(r3: Entity, w: LevelCellSource): Boolean {
        if (!Entity.overlapI(r3.W, w.camRect)) return false
        return when (r3.ax) {
            17, 50 -> true
            11, 73 -> !r3.deadRelease() && r3.aA >= 1
            else -> false
        }
    }

    /** `i.ae()` (i.java:20819, proven): `g.a != null` or any on-screen
     *  dangerous ax∈{17,11,23,50,73} (ax23 always fails `h()`'s switch). */
    private fun enemiesAlert(w: LevelCellSource, p: Entity): Boolean {
        if (p.ga != null) return true
        for (n in w.npcs) {
            if (n.ax != 17 && n.ax != 11 && n.ax != 23 &&
                n.ax != 50 && n.ax != 73) continue
            if (enemyDanger(n, w)) return true
        }
        return false
    }

    /** `i.a(i,int,int[])` (i.java:15324, proven): when `P&4096` and
     *  `r7.W ∩ r9`, push `r7` out of `this`'s rect — vertical exit when
     *  entering from above/below (velocity-sign gated), else horizontal.
     *  `ax==27` callers skip the vertical inner gates (fire entity). */
    private fun pushApart(r7: Entity, P: Int, r9: IntArray, self: Entity) {
        if (P and 4096 == 0) return                          // L5
        if (!Entity.overlapI(r7.W, r9)) return               // L7
        // L7 head → L11b: from-above landing runs when r7 rises or self
        // falls; its W gates fall through to L24 on failure.
        if (r7.ah > 0 || self.ah < 0) {
            if (r7.W[1] < r9[1] && r7.W[3] < r9[3] &&
                r7.ak > r9[0] && r7.ak < r9[2]) {
                if (r7.ah > 0) { r7.aj = 0; r7.ah = 0 }      // L11b→L21
                r7.al = r9[1] - 5                            // land on top
                return
            }
        }
        // L24 head → L28: from-below exit when r7 falls or self rises
        if (r7.ah < 0 || self.ah > 0) {
            if (r7.W[3] > r9[3] && r7.W[1] > r9[1] &&
                r7.ak > r9[0] && r7.ak < r9[2]) {
                if (r7.ah < 0) { r7.aj = 0; r7.ah = 0 }      // L39 head
                if (!r7.aZ || self.ah <= 0) {                // L39/L41
                    r7.al = r9[3] + (r7.al - r7.W[1]) + 5    // L43
                    return
                }
            }
        }
        // L46 → L52 left-exit (r7 moving right into r9's left face, or
        // self moving left; ax27 skips the velocity gate entirely)
        if (r7.ag > 0 || self.ag < 0 || self.ax == 27) {
            if (r7.W[2] < r9[2]) {                           // L52 head
                if (r7.ag > 0) { r7.ai = 0; r7.ag = 0 }
                r7.ak = r9[0] - (r7.W[2] - r7.ak) - 5        // L56
                return
            }                                                // else → L59
        }
        // L59 → L65/L67 right-exit
        if (!(r7.ag < 0 || self.ag > 0 || self.ax == 27)) return
        if (r7.W[0] <= r9[0]) return                         // L65 head
        if (r7.ag < 0) { r7.ai = 0; r7.ag = 0 }              // L67
        r7.ak = r9[2] + (r7.ak - r7.W[0]) + 5                // L69
    }

    /** ax27 record init — L197 arm (i.java:3213, proven) + the generic
     *  `i(r8[5])` tail (L395) and `t()` box fill (L427). The `r8[5]==15`
     *  record shares the L203 ax29 arm (`k.aU = this` — proven). */
    fun initAx27(e: Entity, f: List<Int>, w: LevelCellSource) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = 1
        e.aA = rf(7)
        e.aB = 30
        e.Z[0] = rf(8)
        e.Z[1] = rf(9) * 1000
        e.Z[2] = 0
        e.Z[3] = rf(10)
        if (rf(5) == 4) {
            e.P = e.P or 4096                                // L201
        } else if (rf(5) == 15) {                            // L200→L203
            e.az = 100; e.aB = 800; e.aD = 2; e.m = 2
            e.aC = 30; e.aF = 30; e.eventN = 60
            // L203 reallocates Z to 5 slots; the fixed 22-slot array is a
            // superset — values overwritten below are the only reads.
            e.Z[0] = rf(4); e.Z[1] = rf(7); e.Z[2] = rf(8)
            e.Z[3] = rf(9); e.Z[4] = rf(10)
            if (rf(5) != 30) w.kAU = e                       // k.aU (shared)
        }
        e.setAnim(rf(5))                                     // L395
        e.refreshBoxes()                                     // L427 t()
    }

    /** `i.bL()` (i.java:20843, proven transcription). */
    fun tickAx27(e: Entity, w: LevelCellSource, p: Entity) {
        when (e.S) {
            // ---------- S0 — idle / interact-arm ----------
            0 -> {                                           // L10/L12
                var engage = false
                if (p.S != 267) {
                    if (p.S == 89 || p.S == 90) {            // L12→L20
                    } else if (!Entity.overlapI(p.W, e.W)) {
                    } else if (enemiesAlert(w, p)) {
                    } else engage = true
                }
                if (!engage) { e.releaseAe(); fuseTail(e, w); return }
                // L24 — tap 16388 (context) binds the entity to the player
                if (w.padHeld(16388)) {
                    p.af = e; p.ag = 0; p.ah = 0
                    p.setAnim(267)
                    e.releaseAe()
                    w.clearLatches()
                    fuseTail(e, w); return
                }
                // L27 — aA==1 arms the pickup marker at (ak, al-85)
                if (e.aA != 1) { fuseTail(e, w); return }
                if (e.ae == null) {
                    e.releaseAe()                            // L32 G()
                    e.spawnMarker(w, 7, e.ak, e.al - 85)     // a(7,…)
                }
                val ae = e.ae
                if (ae != null && ae.S == 7) {               // L31→L33 pin
                    ae.ak = e.ak; ae.al = e.al - 85
                }
            }
            // ---------- S1/S21 — mount-in anim ----------
            1, 21 -> {                                       // L35
                e.releaseAe()                                // G()
                if (e.animFinished()) {                      // L38→L41
                    e.T = e.clip!!.frameCount(e.S) - 1; e.U = 0
                    if (p.az == -2) { p.setAnim(269); p.az = 100 }
                } else if (p.az == -2 && e.T == e.clip!!.frameCount(e.S) - 1) {
                    // L38/L41 pin — falls into L58 below
                } else { fuseTail(e, w); return }
                fuseArm(e, w); fuseTail(e, w); return        // → L58 arm
            }
            // ---------- S2/S22 — hold player on the prop ----------
            2, 22 -> {                                       // L45
                if (!e.animFinished() &&
                    e.T != e.clip!!.frameCount(e.S) - 1) {   // L45→L121
                    fuseTail(e, w); return
                }
                if (p.af !== e) { e.setAnim(0); fuseTail(e, w); return } // L49
                p.ak = e.ak                                  // L51 pin
                e.T = e.clip!!.frameCount(e.S) - 1; e.U = 0
            }
            // ---------- S3 — release the player ----------
            3 -> {                                           // L53
                if (p.af === e) { p.af = null; p.az = 100 }  // L53 (decompiler
                                                           // label garble:
                                                           // release when af
                                                           // IS this entity)
                if (!e.animFinished()) { fuseTail(e, w); return }
                e.T = e.clip!!.frameCount(e.S) - 1; e.U = 0
            }
            // ---------- S4/S23 — active/fire arm (link watch) ----------
            4, 23 -> fuseArm(e, w)                           // L58
            // ---------- S5 — interpose → S15 ----------
            5 -> {                                           // L100
                if (!e.animFinished()) { fuseTail(e, w); return }
                e.setAnim(15)
            }
            // ---------- S6 — fuse tick ----------
            6 -> {                                           // L78
                if (!e.animFinished()) { fuseTail(e, w); return }
                e.P = e.P or 64; e.P = e.P and -4097         // L78/L82
                if (e.Z[1] != 0) {
                    e.Z[2] += 62                             // j.f
                    if (e.Z[2] >= e.Z[1]) {
                        e.setAnim(8); e.Z[2] = 0; w.sfx(23)  // k.A(23)
                    }
                }
            }
            // ---------- S7 — pin last frame ----------
            7 -> {                                           // L5
                if (e.animFinished() ||
                    e.T == e.clip!!.frameCount(e.S) - 1) {
                    e.T = e.clip!!.frameCount(e.S) - 1; e.U = 0
                } else { fuseTail(e, w); return }
            }
            // ---------- S8/S20 — explode → S4 ----------
            8, 20 -> {                                       // L85
                e.P = e.P or 4096
                if (!e.animFinished()) { fuseTail(e, w); return }
                if (w.kAD === e) w.kAD = null                // L90
                e.setAnim(4)
            }
            // ---------- S12 — burn out ----------
            12 -> {                                          // L103
                if (!e.animFinished()) { fuseTail(e, w); return }
                if (w.kAD === e) w.kAD = null
                e.setAnim(13)
                e.P = e.P and -4097
                e.P = e.P and -17
            }
            // ---------- S15 — damage arm ----------
            15 -> {                                          // L91
                e.P = e.P or 4096
                if (p.X[0] != p.X[2] &&
                    Entity.overlapI(e.W, p.X)) {             // L91-L94
                    e.aB -= 10
                    if (e.aB > 0) e.setAnim(5) else e.setAnim(12)
                }
            }
            // ---------- S16 — load/show message ----------
            16 -> {                                          // L110
                if (w.kAO >= 0) {
                    w.kAP =
                        if (e.Z[3] < 0) null                 // L114
                        else w.levelString(1 + w.kAj, e.Z[3])
                } else w.kAP = null                          // L114
            }
            // ---------- S17 — wait → S18 ----------
            17 -> {                                          // L119
                if (!e.animFinished()) { fuseTail(e, w); return }
                e.setAnim(18)
            }
            // ---------- S19 — arm message timer ----------
            19 -> {                                          // L116
                if (!e.animFinished()) { fuseTail(e, w); return }
                e.setAnim(16); w.kAO = 4000
            }
            else -> {}                                       // 9,10,11,13,14,
        }                                                    // 18 → L121
        fuseTail(e, w)
    }

    /** `bL()` L58 arm (S4/S23, i.java:20892): linked ax58 `k.q(Z[0])`
     *  state gate → `i(6)` + `P|16` + claim `k.aD`. The decompiler's
     *  odd-S/even-S switch collapses to `r0.S ∈ {1,4,6,8,10,12}` —
     *  the even-state set. */
    private fun fuseArm(e: Entity, w: LevelCellSource) {
        e.P = e.P or 4096
        if (e.Z[0] == -1 || e.Z[1] < 0) return               // L58 gates
        val r0 = w.findByAw(e.Z[0]) ?: return
        if (r0.ax != 58) return
        if (r0.S !in intArrayOf(1, 4, 6, 8, 10, 12)) return  // L68 set
        e.setAnim(6)
        e.P = e.P or 16
        if (w.kAD == null) w.kAD = e                         // L75
    }

    /** `bL()` L121 tail (i.java:20960): push overlapping ax11s out of
     *  the entity's box while `P&4096` (the fire/hazard bit). `k.bd[]`
     *  ↔ `w.npcs` filtered; `a(i,P,W)` = the push-apart routine. */
    private fun fuseTail(e: Entity, w: LevelCellSource) {
        for (n in w.npcs) {
            if (n.ax != 11) continue
            if (Math.abs(e.ak - n.ak) > 50) continue
            pushApart(n, e.P, e.W, e)
        }
    }

    // ============================================================ ax40 = bx()
    // Rideable zipline/gondola (tick `bx()` i.java:17189, draw `by()`
    // i.java:17360, record init L208 i.java:3259). Records carry
    // `[40,uid,x,y,0,S,0,az,scriptEnt,startAnchor,endAnchor]`: the S0
    // gondola hangs from a cable spanned by its two S2 endpoint records;
    // `by()` draws the wire from the S2 record (two line segments to the
    // player's hang point while ridden). Level 0 has two runs: gondola
    // 935 (endpoints 936@7480 → 937@7947) and gondola 49 (endpoints
    // 51@9137 → 50@9700).
    //
    // Z layout (int[7]): [0]=script-entity uid, [1]=cable y (home al),
    // [2]/[3]=resolved travel endpoints (anchor ak), [4]/[5]=anchor uids
    // (-1 = resolved or none), [6]=home ak.
    //
    // Horizontal travel is owned by the claim script — `ab()`→`aa()`
    // runs `k.by` ops each tick while a claim is bound; `runClaimScript`
    // is still a stub, so `ak` never advances and the gondola parks
    // (inferred — bx() itself never writes `ak` except the S1 reset).
    // `ah`/`aj` are the *vertical* departure-fall speed+gravity: once
    // moving, `M()` probes the side cell until a wall ends the fall and
    // `i(1)` resets the gondola to (Z[6], Z[1]).

    /** ax40 record init — L208 arm (i.java:3259, proven) + the generic
     *  `i(r8[5])` + `t()` tail. Z re-allocates to int[7]; the fixed
     *  22-slot array is a superset. */
    fun initAx40(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.Z[0] = rf(8)
        e.Z[6] = e.ak
        e.Z[1] = e.al
        e.Z[2] = 0
        e.Z[3] = 0
        e.Z[4] = rf(9)
        e.Z[5] = rf(10)
        e.P = e.P or 16
        e.az = rf(7)
        e.setAnim(rf(5))                                     // L392
        e.refreshBoxes()                                     // t()
    }

    /** `i.bx()` (i.java:17189, proven transcription). */
    fun tickAx40(e: Entity, w: LevelCellSource, p: Entity) {
        // ---- anchor resolution arms (run once each, then latch -1) --
        if (e.Z[4] != -1 && e.S == 0) {                      // L7
            val r0 = w.findByAw(e.Z[4])
            if (r0 != null) { e.Z[2] = r0.ak; e.s = r0 }
            e.Z[4] = -1
        }
        if (e.Z[5] != -1) {                                  // L11
            val r02 = w.findByAw(e.Z[5])
            if (r02 != null) e.Z[3] = r02.ak
            e.Z[5] = -1
        }
        if (e.claimActive()) e.runClaimScript(w)             // L17 ab()→aa()
        when (e.S) {
            // -- L114: reset arm — wait out anim, respawn at start ----
            1 -> {
                e.aj = 0; e.ah = 0
                if (!e.animFinished()) return
                e.setAnim(0)
                e.ak = e.Z[6]; e.al = e.Z[1]
                e.claimLatchX = -1; e.scriptOps = null; e.scriptStep = 0
                return
            }
            0 -> {}                                          // → L22
            else -> return                                   // L123 default
        }
        // ---- L22-L53 S0 ride/board head -----------------------------
        if (Entity.overlapI(p.W, e.W) && e.ah == 0) {        // L22+L24
            if (p.ac === e) {                                // riding
                val hop = w.padHeld(16388) ||                // L28 context tap
                    (p.av && w.padHeld(2)) ||                // L30 tap left
                    (!p.av && w.padHeld(8))                  // L34 tap right
                if (hop) {                                   // L37 dismount
                    p.setAnim(157)
                    p.al = p.W[1] - 20
                    p.ag = (if (p.av) -1 else 1) shl 11
                    p.ah = -2560
                    p.ac = null                              // aS.a(null)
                    e.eventDisarm(w)                         // O()
                    e.releaseClaim(w)                        // bI()
                    e.ca = -1
                    e.ah = 512; e.aj = 1536
                } else if (p.S != 43) {                      // L43 ride pin
                    val r04 = Math.abs(e.ak - ((e.Z[3] + e.Z[2]) shr 1))
                    val r05 = (e.Z[3] - e.Z[2]) shr 1
                    e.al = e.Z[1] + (10 * (r05 - r04)) / r05 // catenary sag
                    p.av = e.Z[2] >= e.Z[3]                  // L46/L47 dir
                    if (p.S != 164) {                        // L49 mount anim
                        p.aj = 0; p.ah = 0; p.ag = 0
                        p.setAnim(164); p.refreshBoxes()
                    }
                    p.ak = e.ak                              // L51 pin
                    p.al = (e.al + 25) + (p.W[3] - p.W[1])
                }
            }
            // L53 — board arm (bound/moving/script-blocked → L64)
            if (p.ac !== e && e.ah == 0 && e.scriptStep != -2) {
                val old = p.ac
                if (old != null && old.ax == 40)             // L61 swap
                    old.releaseClaim(w)
                p.aj = 0; p.ah = 0; p.ag = 0                 // L63 bind
                p.ac = e                                     // aS.a(this)
                p.setAnim(164); p.refreshBoxes()
                p.ak = e.ak
                p.al = (e.al + 25) + (p.W[3] - p.W[1])
                e.bindScript(w.kSIndex(e.Z[0]), w)           // h(k.s(Z[0]))
                e.scriptKeyStep(e.ca, w)                     // k(ca)
            }
        }
        gondolaTail(e, w, p)                                 // L64 tail
    }

    /** `bx()` L64-L113 tail (i.java:17261, proven): departure arm,
     *  player walk-off unlink, gravity fall, side-wall reset and the
     *  ax11 crush scan. Reached from every S0 path (riding or not). */
    private fun gondolaTail(e: Entity, w: LevelCellSource, p: Entity) {
        // L64/L70 — r7 = ak is past the far endpoint in the travel
        // direction (Z2<Z3 → left→right; Z2>Z3 → right→left).
        val r7 = if (e.Z[2] <= e.Z[3]) e.ak > e.Z[3] else e.ak < e.Z[3]
        // L75 → L81: past the far end, or script-forced (cK == -2 while
        // a claim is bound) → kick the departure fall and eject a rider
        // still hanging in anim 164.
        if (r7 || (e.ca != -1 && e.scriptStep == -2)) {
            if (e.ah == 0) {                                 // L81
                e.ca = -1
                e.ah = 512
                if (p.ac === e && p.S == 164) {
                    e.P = e.P and 256.inv()                  // aS.ac.P &= -257
                    p.flingAirborne(0, w)                    // aS.a(0)
                }
            }
        }
        // L88 — player walked off the platform → unlink.
        if (p.ac === e && !Entity.overlapI(p.W, e.W)) p.ac = null
        // L93 — parked → done.
        if (e.ah == 0) return
        // L95 — departure fall: gravity, capped.
        e.aj = 1536
        if (e.ah > 2048) e.ah = 2048
        // L99 — side-cell wall reached → reset arm.
        if (frontCellBlocked(e, w)) { e.setAnim(1); return }
        // L102 — crush any ax11 the falling gondola overlaps.
        for (n in w.npcs) {
            if (n.ax != 11) continue
            if (!Entity.overlapI(n.W, e.W)) continue
            killByType(n)                                    // d(k.bd[r8])
            e.setAnim(1)
            return
        }
    }

    /** `i.M()` (i.java:7198, proven) + inner `h(cx,cy)` (i.java:7212):
     *  probe the cell one column ahead in the `av` direction at the
     *  entity's row; `e(cx,cy) >= 5` counts as blocking. */
    private fun frontCellBlocked(e: Entity, w: LevelCellSource): Boolean {
        val cx = e.ak / 20 + (if (e.av) -1 else 1)
        return e.e(w, cx, e.al / 20) >= 5
    }

    /** `i.d(i)` (i.java:1642, proven): kill-by-type — ax11 → `i(0)`
     *  (death chain), ax23 → `i(79)`; anything else is a no-op. */
    private fun killByType(r: Entity) {
        when (r.ax) {
            11 -> r.setAnim(0)
            23 -> r.setAnim(79)
        }
    }

    // ============================================================ ax67 = bB()
    // Decor/interactive props (i.java:17584). Clip binds at record init to
    // `k.r(bk[kind])` — the prop's OWN kind→clip table, NOT `bi[67]`
    // (i.java:2633, proven). bk = kind→clip table (k.java:8444, proven):
    //   {24,27,27,27,34,35,37,41,64,64,65,67,49,69,70}
    // Level-0's 253 ax67 records are all kind 9 → bk=64 (pure decor —
    // clip64 has 35 anims/0 rects so every interaction arm is naturally
    // dead). The bk==27 (springboard) and kind-5 (S28 spawner/shove,
    // S12 hide spot) arms are transcribed verbatim for other levels;
    // deps they need that aren't modeled yet are flagged inline.

    /**
     * ax67 record init — L347 arm (i.java:3530, proven) + the generic
     * aB arm (i.java:2635): `az=f8`; `Z[0]=f7` (kind); `bk==37 → P|16`
     * (physics flag); `bk==35 && f5==12 → Z[1]=f4`; `bk==35 && f5==28 →
     * P|16`; `aB = f5==39 ? 2 : 0`; then `i(f5)`. `t()` box fill follows
     * in the caller (clip64 carries no rects → W/X stay zero).
     */
    fun initDecor(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(8)
        val kind = rf(7)
        if (decorClip(kind) == 37) {
            e.P = e.P or 16
        } else {
            if (decorClip(kind) == 35 && rf(5) == 12) e.Z[1] = rf(4)
            if (decorClip(kind) == 35 && rf(5) == 28) e.P = e.P or 16
        }
        e.Z[0] = kind
        e.aB = if (rf(5) == 39) 2 else 0     // bh[aj]==3 arm (2634)
        e.setAnim(rf(5))
        e.refreshBoxes()
    }

    /**
     * ax67 `bB()` (i.java:17584, proven transcription). Overlap checks use
     * `i.a()` — INCLUSIVE edges (i.java:632, differs from `overlap`).
     * - bk==27 springboard: S∈{19,21,23,32,35,38} armed; W∩playerW →
     *   `ah=768+k.Y` + op40 grab + `i(S+1)`; then for S∈{19,21,23} the
     *   `bd[]` scan arms ax68-linked children (`ad.i(2)`, `d(8,…)`
     *   floatie — unported, `r0.i(10)`); even S∈{20,22,24,33,36,39}
     *   despawn on `r()`; S∈{25..31,34,37} are dead.
     * - Z[0]==5 (bk=35) interactive: S28 — `X∩playerW && !v()` →
     *   spawn/re-pin the ae pickup (`i.a(71,ak,al)` at the view edge);
     *   `W∩playerW` → shove `i(309)`/`aC=5`/`ag=±2560`/`g.a=null`;
     *   else `aS.G()`. S12 — hide spot (`Z[1]==1 && g.g==null &&
     *   W∩playerW` → `aA|=8`,`g.e`,`az-1`; release arm restores aA/az).
     */
    fun tickDecor(e: Entity, player: Entity) {
        e.advanceAnim()   // universal s()
        e.refreshBoxes()
        if (decorClip(e.Z[0]) == 27) {
            when (e.S) {
                19, 21, 23, 32, 35, 38 -> {
                    // L7: player body reaches the pad → bounce + grab
                    if (Entity.overlapI(e.W, player.W)) {
                        player.ah = 768 + world.kY
                        player.applyHit(40, 0, e, world)   // a(40,0,0,this)
                        if (e.S != 39) e.setAnim(e.S + 1)
                    }
                    // L12/L14/L94: S∈{32,35,38} skip the child scan
                    if (e.S == 32 || e.S == 35 || e.S == 38) return
                    // L19: `k.bd[]` scan for an ax68-linked child hitting W
                    for (r0 in world.npcs) {
                        val ad = r0.ad ?: continue
                        if (ad.ax != 68) continue
                        if (!Entity.overlapI(e.W, ad.W)) continue
                        if (e.S != 39) e.setAnim(e.S + 1)
                        ad.setAnim(2)
                        // d(8, ad.ak, ad.al) floatie spawner — unported
                        r0.setAnim(10)
                        return
                    }
                    return
                }
                20, 22, 24, 33, 36, 39 -> {
                    if (e.animFinished()) world.removeEntity(e)   // L39 k.c(this)
                    return
                }
                else -> return     // L91 — dead bank (25..31,34,37, default)
            }
        }
        if (e.Z[0] != 5) return          // L43 gate — kind-9 decor exits
        when (e.S) {
            28 -> {
                // L46: r02 = player side vs prop centerline
                val r02 = player.ak - ((e.W[0] + e.W[2]) shr 1)
                if (Entity.overlapI(e.X, player.W) &&
                    !e.wasHitRecently(world)) {
                    val ae = player.ae
                    if (ae == null) {
                        player.releaseAe()               // aS.G() then a(71)
                        player.ae = world.spawnPickup(71, e.ak, e.al)
                        pinAe(player.ae!!, r02, e.al); return
                    }
                    if (ae.S == 71) {                    // L53→L56 re-pin
                        pinAe(ae, r02, e.al); return
                    }
                    // ae exists but isn't a 71 → fall to the shove check
                }
                // L62: player inside the body → hard shove out
                if (Entity.overlapI(e.W, player.W)) {
                    player.setAnim(309)
                    player.aC = 5
                    if (r02 >= 0) { player.ag = 2560; player.av = true }
                    else { player.ag = -2560; player.av = false }
                    player.ga = null                     // g.a = null
                    return
                }
                player.releaseAe()                       // L69 aS.G()
                return
            }
            12 -> {
                if (e.Z[1] != 1) return                  // L72
                // L74: un-guarded + overlap → bind (aA|=8, g.e, az-1)
                if (player.gg == null && Entity.overlapI(e.W, player.W)) {
                    player.aA = player.aA or 8
                    player.ge = e
                    player.az = e.az - 1
                    return
                }
                // L80: not ours → end; ours → release (aA&=-9, az=100)
                if (player.ge !== e) return
                player.aA = player.aA and -9
                player.ge = null
                player.az = 100
                return
            }
            else -> return                               // L98
        }
    }

    /** L56-L59 (i.java): pin `ae` to the view edge on the prop's side. */
    private fun pinAe(ae: Entity, r02: Int, y: Int) {
        ae.ak = if (r02 >= 0) world.kO + 20 else world.kO + 400 - 20
        ae.al = y
    }

    /**
     * ax14 record init — L88 arm (i.java:2868, proven): `az=200`, `aD=f7`,
     * `aE=f8` (arming threshold), `o=f11` (linked entity `aw`, -1=none),
     * `j=f12` (watch state), `P|=512`, `o!=-1 → P|=128` (hidden until
     * armed), `f4==1 → aA=1` (persistent marker). Then the generic L419
     * fill (i.java:3699): `W = [ak+f7, al+f8, +f9, +f10]` — record
     * pickups get a live box; `a()`-spawned ones keep W empty → their
     * `aX()` is a no-op (inert visuals like the S71 edge arrow).
     */
    fun initPickup(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = 200
        e.aD = rf(7); e.aE = rf(8)
        e.oId = rf(11); e.j = rf(12)
        e.P = e.P or 512
        if (e.oId != -1) e.P = e.P or 128
        if (rf(4) == 1) e.aA = 1
        e.setAnim(rf(5))
        e.W[0] = e.ak + rf(7); e.W[1] = e.al + rf(8)
        e.W[2] = e.W[0] + rf(9); e.W[3] = e.W[1] + rf(10)
    }

    /**
     * ax14 `aX()` (i.java:13410, proven) — pickup/marker lifecycle:
     * - head: `W==null → return` (spawned visuals are inert). Our W is
     *   always allocated, so the all-zero W stands in for null.
     * - `a(aS.Y,W) && !aS.f()` → `aF=1`, `P&~128` (armed + unhidden).
     * - `S==107` → follow the player (`ak/al = aS.ak/al`).
     * - linked (`o!=-1`, resolved via `k.q` = scan by `aw`): in-play waits
     *   `bZ>=aE`, then unhides, `ah=k.Y`, `aC=15` countdown → `k.c(this)`;
     *   waits while target `P&32` (held) or `P&128` (hidden). Off-play:
     *   hide when the player leaves (`P|=128`), consume when `r5.S==j`
     *   and `aA!=1`.
     * - unlinked: once armed (`aF==1`), player leaving W → `aA==1` hides
     *   (`P|=128`), otherwise `k.c(this)` — the pickup is collected.
     */
    fun tickPickup(e: Entity, player: Entity) {
        e.advanceAnim()
        if (e.W.contentEquals(Entity.ZERO_RECT)) return      // L6 W==null
        if (Entity.overlapI(player.Y, e.W) && !player.isHolding()) {
            e.aF = 1
            e.P = e.P and -129
        }
        if (e.S == 107) { e.ak = player.ak; e.al = player.al }
        val r5 = if (e.oId != -1) world.findByAw(e.oId) else null
        if (r5 != null) {
            if (world.inPlay) {
                if (e.bZ < e.aE) return                      // L17 wait arm
                e.P = e.P and -129
                e.ah = world.kY
                if (e.aC <= 0) e.aC = 15
                e.P = e.P or 16
                if (r5.P and 32 != 0) return                 // held → wait
                if (r5.P and 128 != 0) return                // hidden → wait
                e.aC--
                if (e.aC <= 0) world.removeEntity(e)
                return
            }
            if (e.aF == 1 && !Entity.overlapI(player.Y, e.W))
                e.P = e.P or 128
            if (r5.S != e.j) return                          // watch state
            if (e.aA == 1) return                            // persistent
            world.removeEntity(e)
            return
        }
        if (e.aF != 1) return                                // never touched
        if (Entity.overlapI(player.Y, e.W)) return           // still inside
        if (e.aA == 1) { e.P = e.P or 128; return }          // hide, keep
        world.removeEntity(e)                                // collected
    }

    /**
     * ax16 `bb()` head arms (i.java:14088-14110, proven): the request-
     * marker entity — on `a(k.aS.W, W)` overlap it ORs an action bit into
     * `g.J` via `g.g()`, then consumes itself:
     * - `S==30` → `g.g(2)` + `k.aS.h(2)` + `k.aS.i(91)` + `k.A(15)` +
     *   `k.aS.E()` + `k.c(this)` (hurt-mark request)
     * - `S==38` → same with bit 8
     * - `S==39` → `g.g(4)` + `k.c(this)` (mount request — feeds the az()
     *   ax72 arm's `J&4` gate)
     * `i.bb()` (i.java:14088, proven): the ax16 request-marker FSM —
     *  collect arms (S30/38/39 → `g.g(N)` + `h(N)` + `i(91)` + A(15) +
     *  `E()` + `k.c(this)`), then the L21 switch: flight/rest projectile
     *  arms (S15/16/17), ceiling props (S22/23/25), and the stealth-kill
     *  prompt markers (S31/32/33 → `aS.i(216/214)` on a 65568 tap).
     */
    fun tickRequestMarker(e: Entity, player: Entity, pad: Pad) {
        e.advanceAnim()
        when (e.S) {
            30, 38 -> {                   // L9/L15 — equip pickups
                if (!Entity.overlapI(player.W, e.W)) return
                val bit = if (e.S == 30) 2 else 8
                player.requestAction(bit, world)
                player.requestH(bit, world)
                player.setAnim(91)
                world.sfx(15)
                player.settleToGround(world)
                world.removeEntity(e)
            }
            39 -> {                       // L21 — mount-request pickup
                if (!Entity.overlapI(player.W, e.W)) return
                player.requestAction(4, world)
                world.removeEntity(e)
            }
            else -> markerArm(e, player, pad)
        }
    }

    // `bb()` L21 switch (i.java:14113+): the non-collect marker states.
    private fun markerArm(e: Entity, player: Entity, pad: Pad) {
        when (e.S) {
            // L23 — flight end: `t() + bd() + i(9)`
            15 -> {
                if (!e.animFinished()) return
                e.refreshBoxes()
                e.sweepNeighbors(world)
                e.setAnim(9)
            }
            // L27 — rest/decay: `T==1&&U==0 → A(12)`; `t()`; then the
            // `k.bh[k.aj]==3` settle arm else the `bd()+op4` damage arm;
            // `af=null; r() → k.c(this)`
            16 -> {
                if (e.T == 1 && e.U == 0) world.sfx(12)
                e.refreshBoxes()
                if (world.missionBh() == 3) {
                    e.ah = 0; e.ag = 0
                    if (e.af === player) e.sweepNeighborsB(world)
                    else if (Entity.overlapI(e.X, player.W))
                        player.applyHit(38, 0, e, world)
                } else {
                    e.sweepNeighbors(world)
                    if (Entity.overlapI(e.X, player.W))
                        player.applyHit(4, 0, e, world)
                }
                e.af = null
                if (e.animFinished()) world.removeEntity(e)
            }
            // L48 — the thrown-knife flight: `aj=512`; `ah==0&&ag!=0` →
            // `a(4,ak,al-60)` trail marker; X-overlap on the player →
            // `g.b()` attack → the L58 bounce (snap outside the X box,
            // flip av, `ag=k.e(afΔy, ah>>8, 2, afΔx)` arc-solve to the
            // thrower, `bR` set); else `aS.a(4)`. `bR&&af.ax==23&&overlap
            // → af.aB=0,i(79)`. r9 → `i(16)+G()`. Then wall-bounce scan
            // (av-side column, `e>=12` → snap+flip+`ag=-ag/2`), floor hit
            // (`e<12` keeps flying): `G()` + `ah>2048` → bounce `-ah/2`,
            // else `k.h(ag,ah)>=1024 → ah=0`, else `i(16)` rest.
            17 -> {
                e.aj = 512
                if (e.ah == 0 && e.ag != 0) e.spawnMarker(world, 4, e.ak, e.al - 60)
                var r9 = false
                if (Entity.overlapI(player.W, e.X)) {
                    if (!world.playerAttacking()) {
                        player.applyHit(4, 0, e, world); r9 = true
                    } else if (!e.bR) {
                        if (e.ag > 0) {
                            e.ak = player.X[0] - (e.X[2] - e.X[0])
                            e.al = player.X[1] - (e.X[3] - e.X[1])
                        } else if (e.ag < 0) {
                            e.ak = player.X[2] + (e.X[2] - e.X[0])
                            e.al = player.X[1] - (e.X[3] - e.X[1])
                        }
                        e.av = !e.av
                        val r0 = -e.ah / 2
                        val f = e.af                       // af.al read — null
                        val r92 = if (f != null) {         // guard inferred
                            var v = Entity.arcSolve(
                                f.al - e.al, e.ah shr 8, f.ak - e.ak)
                            if (v == -1) -e.ag else v
                        } else -e.ag
                        e.ag = r92; e.ah = r0; e.bR = true
                    }
                }
                if (e.bR && e.af?.ax == 23 &&
                    Entity.overlapI(e.W, e.af!!.W)) {
                    e.af!!.aB = 0; e.af!!.setAnim(79); r9 = true
                }
                if (r9) {
                    e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
                    e.setAnim(16); e.releaseAe()
                }
                if (e.ag != 0 && !e.W.contentEquals(Entity.ZERO_RECT)) {
                    val r04 = if (e.av) e.W[0] else e.W[2]
                    val r06 = r04 / 20 + (if (e.av) -1 else 1)
                    var r10 = e.W[1] / 20
                    var hit = false
                    while (r10 <= e.W[3] / 20) {
                        if (e.e(world, r06, r10) >= 12) { hit = true; break }
                        r10++
                    }
                    if (hit) {
                        e.ak = if (e.av) (e.ak / 20) * 20 else ((e.ak / 20) + 1) * 20
                        e.av = !e.av
                        e.ag = -e.ag / 2
                    }
                }
                if (e.ah < 0) return
                if (e.e(world, e.ak / 20, e.al / 20) < 12) return
                e.releaseAe()                              // G()
                if (e.ah > 2048) { e.ah = -e.ah / 2; e.ag /= 2; return }
                if (Entity.magApprox(e.ag, e.ah) < 1024) {
                    e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
                    e.setAnim(16); return
                }
                e.ah = 0
            }
            // L131 — ceiling props: `W==null → k.c`; `W∩aS.W → a(4)+k.c`;
            // S22 under-player ceiling cell==20 → `i(25)` rest; S23 →
            // `v()` view check.
            22, 23 -> {
                if (e.W.contentEquals(Entity.ZERO_RECT)) { world.removeEntity(e); return }
                if (Entity.overlapI(player.W, e.W)) {
                    player.applyHit(4, 0, e, world)
                    world.removeEntity(e); return
                }
                if (e.S == 22 &&
                    e.e(world, e.W[2] / 20, e.W[3] / 20) == 20 &&
                    e.W[3] > player.W[1]) {
                    e.setAnim(25); e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
                }
                if (e.S == 23 && !e.markerVisible(world)) world.removeEntity(e)
            }
            // L152 — anim end → k.c
            25 -> if (e.animFinished()) world.removeEntity(e)
            // L156 — stealth-kill prompt: leaves area → `G()`; while the
            // player overlaps, not mid-kill (S216/50), not attacking
            // (`g.b()`), and `J&2` armed → spawn the `a(8,ak,al-85)`
            // indicator; a 65568 tap teleports the player into `i(216)`
            // + A(29).
            31 -> {
                if (!Entity.overlapI(player.W, e.W)) { e.releaseAe(); return }
                if (player.S == 216 || player.S == 50 || world.playerAttacking() ||
                    (player.gJ and 2) == 0) { e.releaseAe(); return }
                e.spawnMarker(world, 8, e.ak, e.al - 85)
                if (pad.v(Pad.M_CONTEXT)) {
                    player.ak = e.ak; player.al = e.al
                    player.ah = 0; player.ag = 0; player.aj = 0; player.ai = 0
                    player.setAnim(216); world.sfx(29)
                }
            }
            // L172 — left/right kill prompts: same shape → `i(214)`;
            // S32 pins `aS.ak = ak-45, av=false`; S33 `ak+45, av=true`.
            32, 33 -> {
                if (!Entity.overlapI(player.W, e.W)) { e.releaseAe(); return }
                if (player.S == 214 || player.S == 215 || player.S == 50 ||
                    (player.gJ and 2) == 0) { e.releaseAe(); return }
                e.spawnMarker(world, 8, e.ak, e.al - 85)
                if (pad.v(Pad.M_CONTEXT)) {
                    player.al = e.al
                    if (e.S == 32) { player.ak = e.ak - 45; player.av = false }
                    else { player.ak = e.ak + 45; player.av = true }
                    player.ah = 0; player.ag = 0; player.aj = 0; player.ai = 0
                    player.setAnim(214); world.sfx(29)
                }
            }
            else -> {}                   // L195 — inert
        }
    }
}

// =========================================================================
// ax21 — `bD()` mission director (i.java:18054-18629, proven)
// =========================================================================

/** `c` (c.java, proven): mission-waypoint node — `k`=id, `a`/`b`=target
 *  px, `c`=event-type, `d`=countdown nodes, `e`=per-node delay, `f`=arrive
 *  tolerance+flags (bit7 = consumed), `g`=next node id, `h`/`i` spare. */
class WaypointNode {
    var id = 0            // c.k
    var a = 0             // x px
    var b = 0             // y px (waypoint space — +k.P in world)
    var cFlag = 0         // c.c — node event 0..3
    var d = 0             // c.d — `j` countdown (nodes to consume here)
    var e = 0             // c.e — `aC` per-node delay
    var f = 0             // c.f — byte: arrive tolerance | flag bits (&127)
    var g = 0             // c.g — next node id (-1 end)
}

/** `c.m`/`c.l`/`c.j` (c.java): 400-slot pool; `c.a(short[])` parses a record
 *  row; `c.a(c,i)` appends a derived node (`id=j++` from 10000, `a` off the
 *  entity); `c.a(int)` finds by `k`; `c.a()` clears + resets `j=10000`. */
class WaypointPool {
    private val nodes = arrayOfNulls<WaypointNode>(400)
    var count = 0                 // c.l
    var nextDerived = 10000       // c.j

    fun add(row: IntArray) {      // c.a(short[])
        if (count >= nodes.size) return
        val r0 = WaypointNode()
        r0.id = row[1]; r0.a = row[2]; r0.b = row[3]
        r0.cFlag = row[4]; r0.d = row[5]; r0.e = row[6]
        r0.f = row[7]; r0.g = row[8]
        nodes[count] = r0
        count++
    }

    fun addDerived(src: WaypointNode, e: Entity) {   // c.a(c,i)
        if (count >= nodes.size) return
        val r0 = WaypointNode()
        r0.id = nextDerived; nextDerived++
        r0.a = src.a + e.ak; r0.b = src.b
        r0.cFlag = src.cFlag; r0.d = src.d; r0.e = src.e
        r0.f = src.f; r0.g = src.g
        nodes[count] = r0
        count++
    }

    fun find(id: Int): WaypointNode? {             // c.a(int)
        if (id < 0) return null
        for (i in 0 until count) {
            val n = nodes[i]
            if (n != null && n.id == id) return n
        }
        return null
    }

    fun reset() {                  // c.a()
        nodes.fill(null)
        count = 0
        nextDerived = 10000
    }
}

/** `i.bD()` (i.java:18054, proven): mission-phase director. `aA` = phase:
 *  0 arm+attach, 1 single-pursuit monitor, 2→6, 3 dual-respawn+waypoint,
 *  4 dual monitor, 5 single monitor Z[3], 6 kill-bitmap router,
 *  7 finale (`k.l(15)` or floatie), 8 waypoint travel. The `L237` tail —
 *  linked-entity anim watcher + attach-sync + charge gauge — runs every
 *  tick the phase arm doesn't `return` early. */
fun NpcFsm.tickDirector(e: Entity, player: Entity, pad: Pad) {
    val w = world
    e.advanceAnim()
    // pre-switch (L0-L6): chase-progress row while the player is airborne
    if (player.al < 260) w.kAR = (w.kBu / 20 - 1) - player.al / 400
    var tail = true
    when (e.aA) {
        // ---- L7-L50: arm + attach linked entities -------------------------
        0 -> {
            w.kAi = true
            w.kR = -1
            w.sfx(1)                                   // k.A(1)
            for (r9 in 0 until 5) {
                var r8 = if (e.Z[r9] == -1) null else w.findByAw(e.Z[r9])
                if (r8 != null) {
                    r8.P = e.P
                    r8.aq = r8.ak - e.ak
                    r8.ar = r8.al - e.al
                    r8.az = e.az + 1
                    r8.l = 0; r8.j = 0
                    if (r9 != 4) r8.nl = 0
                    else if (w.iBV > 0) r8.P = r8.P or 128
                }
            }
            if (e.Z[15] != -1) {
                val r0 = w.findByAw(e.Z[15])
                if (r0 != null) {
                    r0.P = e.P
                    r0.aq = r0.ak - e.ak
                    r0.ar = r0.al - e.al
                    r0.az = e.az + 1
                    r0.l = 0; r0.nl = 0; r0.j = 0
                }
            }
            w.iCC = 0; w.iCD = -1; w.iCE = -1; w.dirWp = null
            if (w.iBV > 0) {
                // L30-L38: pursuit mode — respawner home-positions into
                // Z[16..19] for later reuse
                if (w.iBV == 1) e.l = e.l or 2
                else if (w.iBV == 2) { e.l = e.l or 62; e.l = e.l and -17 }
                val r02 = w.findByAw(e.Z[13])
                if (r02 != null) {
                    e.Z[16] = r02.Z[15]; e.Z[17] = r02.Z[16]; r02.bs = 0
                }
                val r03 = w.findByAw(e.Z[14])
                if (r03 != null) {
                    e.Z[18] = r03.Z[15]; e.Z[19] = r03.Z[16]; r03.bs = 0
                }
            } else {
                w.dirWp = w.waypoints.find(e.Z[12])
            }
            // L44-L50: shared arming tail — cB set → waypoint chase (8),
            // else the kill-bitmap router (6); bV>0 also lands on 6
            if (w.dirWp != null) {
                w.dirWp!!.f = w.dirWp!!.f and 127
                e.aq = w.dirWp!!.a; e.ar = w.dirWp!!.b
                e.aA = 8
            } else e.aA = 6
            if (w.kAE > 0) w.kAH = 80
            e.P = e.P or 16
            w.iBT = true
        }
        // ---- L67-L83: single-pursuit monitor on Z[0] -----------------------
        1 -> {
            var r92 = false
            val r04 = w.findByAw(e.Z[0])
            if (r04 == null) {
                r92 = true
            } else if (r04.aB > 0) {
                r04.respawnAttack(w)
            } else if (r04.S == 16) {
                r92 = true; r04.l = r04.l and -2
            }
            if (r92) { e.l = e.l or 2; e.aA = 6 }
            else if (r04 != null && r04.cIDone && r04.cJDone) {
                r04.cIDone = false; e.aA = 6
            }
        }
        // ---- L83: phase-2 → 6 ----------------------------------------------
        2 -> e.aA = 6
        // ---- L84-L135: dual-respawn + waypoint travel ----------------------
        3 -> {
            for (r93 in 0 until 2) {
                val r05 = w.findByAw(e.Z[93 + 13]) ?: continue
                // L92 gate: P&16 set → respawn needs S10 && !v() && cC!=6
                val gate = (r05.P and 16) == 0 ||
                    (r05.S == 10 && !r05.inPlayV(w) && w.iCC != 6)
                if (gate) {
                    r05.setAnim(4)
                    r05.ak = e.Z[(r93 shl 1) + 16]
                    r05.al = w.kP + e.Z[(r93 shl 1) + 17]
                    // `aG < 10000 → r05.aw()` — i.aw() unmined (inferred)
                    r05.iE = false; r05.bs = 0
                    r05.P = r05.P or 16
                    r05.aC = r05.Z[6]
                    r05.ad?.setAnim(1)
                }
            }
            if (directorChase(e, true)) {
                if (w.dirWp!!.g == -1) w.dirWp = null
                else w.dirWp = w.waypoints.find(w.dirWp!!.g)
                if (w.dirWp != null) {
                    w.dirWp!!.f = w.dirWp!!.f and 127
                    e.bY = e.aq; e.bZ = e.ar
                    e.aq = w.dirWp!!.a; e.ar = w.dirWp!!.b
                    e.aC = w.dirWp!!.e; e.j = w.dirWp!!.d
                }
                if (w.dirWp == null) e.aA = 6
                else if (w.dirWp!!.cFlag == 1) {
                    // c==1 event node — skips the entity loop AND the
                    // aA=6 route; L135: pv==aA -> aG++ + popup
                    if (e.pv == e.aA) {
                        e.aG++
                        val aK = e.spawnChildFx(w, 19, 11, 17, e.az + 1)
                        aK.ak = w.kO + w.jRand(80, 160)
                        aK.al = w.kP - 20
                        aK.ah = -5
                        aK.az = 200
                        w.queueInsert(aK)
                    }
                } else {
                    for (r94 in 0 until 2) {
                        val r08 = w.findByAw(e.Z[94 + 13]) ?: continue
                        if (r08.S == 10) {
                            r08.P = r08.P and -17
                            r08.P = r08.P and -33
                            r08.iE = false; r08.aC = 100
                        } else if (r08.inPlayV(w)) {
                            if (r08.bs < 4 && r08.Z[r08.bs + 1] != -1) r08.bs++
                            r08.bs--
                        }
                    }
                    e.aA = 6
                }
            }
        }
        // ---- L137-L163: dual-pursuit monitor on Z[1],Z[2] ------------------
        4 -> {
            var r95 = false
            val r09 = w.findByAw(e.Z[1])
            if (r09 == null) { e.l = e.l or 4; r95 = true }
            else if (r09.aB > 0) {
                r09.aG = 1
                r09.respawnAttack(w)
                if (r09.cIDone && r09.cJDone) {
                    r09.cIDone = false; e.aA = 6
                }
            } else if (r09.S == 20) {
                e.l = e.l or 4; r95 = true; r09.l = r09.l and -2
            }
            val r010 = w.findByAw(e.Z[2])
            if (r010 == null) { e.l = e.l or 8; r95 = true }
            else if (r010.aB > 0) {
                r95 = false; r010.aG = 2; r010.respawnAttack(w)
            } else if (r010.S == 20) {
                e.l = e.l or 8; r95 = true; r010.l = r010.l and -2
            }
            if (r95) e.aA = 6
            else if (r010 != null && r010.cIDone && r010.cJDone) {
                r010.cIDone = false; e.aA = 6
            }
        }
        // ---- L165-L181: single-pursuit monitor on Z[3] ----------------------
        5 -> {
            var r96 = false
            val r011 = w.findByAw(e.Z[3])
            if (r011 == null) r96 = true
            else if (r011.aB > 0) r011.respawnAttack(w)
            else if (r011.S == 26) r96 = true
            if (r96) { e.l = e.l or 16; e.aA = 6 }
            else if (r011 != null && r011.cIDone && r011.cJDone) {
                r011.cIDone = false; e.aA = 6
            }
        }
        // ---- L182-L226: kill-bitmap router ----------------------------------
        6 -> {
            e.k = true
            val mask = e.l and 62
            if (mask == 62) {
                w.findByAw(e.Z[13])?.let { w.removeEntity(it) }
                w.findByAw(e.Z[14])?.let { w.removeEntity(it) }
                w.statTally(e.aw)                        // k.e(0,aw)
                e.aA = 7
            } else {
                val cC = when (mask) {
                    2, 14, 18 -> 6
                    12 -> 2
                    16 -> 3
                    46 -> 5
                    else -> w.iCC
                }
                w.iCC = cC
                if (w.iCD != cC && (mask == 2 || mask == 46)) {
                    w.iBW = true; w.iBX = cC
                }
                // L214 — waypoint-set on phase change
                if (w.iCD != cC) {
                    w.iCD = cC
                    w.dirWp = null
                    if (e.Z[5 + cC] != -1)
                        w.dirWp = w.waypoints.find(e.Z[5 + cC])
                    if (w.dirWp == null) { tail = false; return }
                    w.dirWp!!.f = w.dirWp!!.f and 127
                    e.aq = w.dirWp!!.a; e.ar = w.dirWp!!.b
                    e.aC = w.dirWp!!.e; e.j = w.dirWp!!.d
                }
                // L222 — monitor the Z[3] pursuer
                val r015 = w.findByAw(e.Z[3])
                if (r015 == null) directorChase(e, false)
                // inferred: L225's `S!=22` fall-through lands on L237 (the
                // physically-following L197 chain would re-run the router
                // forever — a decompiler drop)
            }
        }
        // ---- L227-L235: finale ----------------------------------------------
        7 -> {
            e.ag = 0; e.ah = 0; e.ad = null
            w.iBj = true; w.iBT = false
            player.ag = 0
            if (e.inPlayV(w)) {
                player.ah = w.kY
                player.settleToGround(w)
                if (player.inPlayV(w)) {
                    val aK = e.spawnChildFx(w, 24, 40, 9, e.az + 1)
                    aK.ag = 0; aK.ah = 0; aK.av = false
                    aK.ak = (e.W[0] + e.W[2]) / 2
                    aK.al = (e.W[1] + e.W[3]) / 2
                    aK.P = aK.P or 16
                    aK.af = player
                    w.queueInsert(aK)
                } else w.missionComplete()             // k.l(15)
                return
            }
            // L230 — director off-play: `aS.ah = k.Y - 2560` then the
            // L237 tail still runs
            player.ah = w.kY - 2560
        }
        // ---- L52-L66: waypoint travel + next-node ---------------------------
        8 -> {
            if (directorChase(e, true)) {
                if (w.dirWp!!.g == -1) w.dirWp = null
                else w.dirWp = w.waypoints.find(w.dirWp!!.g)
                if (w.dirWp == null) {
                    e.aA = 6
                    val r021 = w.findByAw(e.Z[13])
                    if (r021 != null) {
                        e.Z[16] = r021.ak; e.Z[17] = r021.al; r021.bs = 0
                    }
                    val r022 = w.findByAw(e.Z[14])
                    if (r022 != null) {
                        e.Z[18] = r022.ak; e.Z[19] = r022.al; r022.bs = 0
                    }
                } else {
                    w.dirWp!!.f = w.dirWp!!.f and 127
                    e.bY = e.aq; e.bZ = e.ar
                    e.aq = w.dirWp!!.a; e.ar = w.dirWp!!.b
                    e.aC = w.dirWp!!.e; e.j = w.dirWp!!.d
                }
            }
        }
    }
    if (!tail) return

    // ===== L237 tail — every non-returning tick =============================
    // linked-entity anim watcher (r10 = 0..4 over Z[0..4])
    for (r10 in 0 until 5) {
        val r016 = w.findByAw(e.Z[r10]) ?: continue
        if (!r016.animFinished()) continue
        when (r10) {
            0 -> if (r016.aB > 0) r016.setAnim(13)
                 else { r016.setAnim(16); e.l = e.l or 2 }
            1 -> if (r016.aB > 0) r016.setAnim(17)
                 else { r016.setAnim(20); e.l = e.l or 4 }
            2 -> if (r016.aB > 0) r016.setAnim(17)
                 else { r016.setAnim(20); e.l = e.l or 8 }
            3 -> {
                if (r016.aB <= 0) { r016.setAnim(26); e.l = e.l or 16 }
                else if (r016.S == 22) { r016.setAnim(23); r016.k = true }
                else if (((e.l.inv()) and 62) == 16 && r016.k) r016.setAnim(23)
                else r016.setAnim(21)
            }
            4 -> {
                if (r016.aB <= 0) { r016.setAnim(36); e.l = e.l or 32 }
                else {
                    if (e.k && (r016.P and 128) != 0) {
                        r016.P = r016.P and -129
                        r016.setAnim(37)
                        e.k = false
                    }
                    if (r016.S == 37) r016.setAnim(33)
                }
            }
        }
    }
    // L282-L307: 5th pursuer respawn/attack + attach position sync
    val r017 = w.findByAw(e.Z[4])
    if (r017 != null && (r017.P and 128) == 0 && r017.inPlayV(w)) {
        if (r017.aB <= 0 && r017.S == 36) {
            e.l = e.l or 32
            if ((e.l and 62) == 14) e.aA = 6
        }
        when (r017.S) {
            30, 31, 32 -> r017.respawnAttack(w)
            33, 34, 35 -> {
                if (r017.aC > 0) r017.setAnim(r017.pickAttackAnim(w))
                else if (r017.aF > 0) r017.setAnim(r017.pickAttackAnim(w))
                else { r017.respawnAttack(w); r017.aF = r017.nl }
                r017.aC--; r017.aF--
            }
        }
    }
    // L307: attached entities ride the director (aq/ar offsets bound at arm)
    for (r102 in 0 until 5) {
        val r97 = if (e.Z[r102] == -1) null else w.findByAw(e.Z[r102])
        if (r97 != null) {
            r97.ak = e.ak + r97.aq
            r97.al = e.al + r97.ar
        }
    }
    if (e.Z[15] != -1) {
        val r018 = w.findByAw(e.Z[15])
        if (r018 != null) {
            r018.ak = e.ak + r018.aq
            r018.al = e.al + r018.ar
            if (r018.S == 3 && r018.animFinished()) {
                r018.P = r018.P and -17
                r018.P = r018.P or 32
                r018.P = r018.P or 128
            }
        }
    }
    e.syncAd(w)                                          // b(true)
    // L326-L391: charge gauge — i.q mirrors linked hp / else sums it
    if (w.iQ) {
        val r019 = w.findByAw(e.Z[3]) ?: return
        if (w.cFFlag) e.aB = r019.aB
        else {
            e.aB += 5
            if (e.aB > w.iBU) { e.aB = w.iBU; w.cFFlag = true }
        }
    } else {
        e.aB = 0
        for (r103 in 0 until 5) {
            if (r103 == 3) continue
            e.aB += w.findByAw(e.Z[r103])?.aB ?: 0
        }
    }
}

/** `i.d(boolean)` (i.java:18717, proven): waypoint-chase movement for the
 *  director — `|Δx|/|Δy|` vs node tolerance `|cB.f|`: both within → ARRIVED
 *  (aC--, zero vel, snap `bY/bZ=aq/ar`, `bE()`); `flag=true` then skips the
 *  node-event arm (caller advances `cB` itself) → `ah+=k.Y; bE()`.
 *  `flag=false` → arrival runs the node-event arm: while `aC<0 && j>0`:
 *  `j--; aC=cB.e; r7=cB.c` remapped (c==0&&l&2 → cC==5?3:2; then r7==3&&
 *  cC!=5 → cC==2?0:2; cC==5 forces 3), `cB.c=r7` then `switch(cB.c)`:
 *  0→aA=1+p(-2,-240)+((l&32)==0→p(0,-76)); 1→aA=3; 2→aA=4 + l-bit popups
 *  (-22,-15)/(21,-15)/(0,-76); 3→aA=5+p(0,-180). `j<=0` → advance `cB` to
 *  `c.a(cB.g)`. Otherwise velocity `±(f<<8)` on the major axis +
 *  proportional `(minor<<8)/major·f` on the minor. */
private fun NpcFsm.directorChase(e: Entity, flag: Boolean): Boolean {
    val w = world
    e.ag = 0; e.ah = 0
    val r0 = e.aq - e.bY
    val r02 = e.ar - e.bZ
    val node = w.dirWp ?: return false
    val r03 = node.f
    val r04 = kotlin.math.abs(r0)
    val r05 = kotlin.math.abs(r02)
    val r06 = kotlin.math.abs(r03)
    e.pv = e.aA
    if (r04 <= r06 && r05 <= r06) {
        // arrived
        e.aC--
        e.ag = 0; e.ah = 0
        e.bY = e.aq; e.bZ = e.ar
        e.posFromWaypoint(w)
        if (flag) {
            e.ah += w.kY
            e.posFromWaypoint(w)
            return true
        }
        // node-event arm (L9-L59)
        if (e.aC < 0) {
            if (e.j <= 0) {
                // L59 — advance to the next node
                val next = w.waypoints.find(node.g)
                if (next != null) {
                    w.dirWp = next
                    next.f = next.f and 127
                    e.bY = e.aq; e.bZ = e.ar
                    e.aq = next.a; e.ar = next.b
                    e.aC = next.e; e.j = next.d
                }
            } else {
                e.j--
                e.aC = node.e
                var r7 = node.cFlag
                if (r7 == 0 && (e.l and 2) != 0) r7 = if (w.iCC == 5) 3 else 2
                if (r7 == 3 && w.iCC != 5) r7 = if (w.iCC == 2) 0 else 2
                if (r7 != 1 && w.iCC == 5) r7 = 3
                node.cFlag = r7
                when (node.cFlag) {
                        0 -> {
                            e.aA = 1
                            if (e.directorGate(e.aA, w)) {
                                e.popupDmg(w, -2, -240)
                                if ((e.l and 32) == 0) e.popupDmg(w, 0, -76)
                            }
                        }
                        1 -> { e.aA = 3; e.directorGate(e.aA, w) }
                        2 -> {
                            e.aA = 4
                            if (e.directorGate(e.aA, w)) {
                                var r52 = false
                                if ((e.l and 4) == 0) { e.popupDmg(w, -22, -15); r52 = true }
                                if ((e.l and 8) == 0) { e.popupDmg(w, 21, -15); r52 = true }
                                if (r52 && (e.l and 32) == 0) e.popupDmg(w, 0, -76)
                            }
                        }
                        3 -> {
                            e.aA = 5
                            if (e.directorGate(e.aA, w)) e.popupDmg(w, 0, -180)
                        }
                }
            }
        }
        e.ah += w.kY
        e.posFromWaypoint(w)
        return true
    }
    // movement — diagonal-limited: `±(f<<8)` major + proportional minor
    if (r04 < r05) {
        if (r05 > r06) e.ah = if (r02 >= 0) (r03 shl 8) else -(r03 shl 8)
        else e.bZ = e.ar
        var r53 = 0
        if (e.ah != 0) r53 = (r04 shl 8) / r05
        if (r04 <= (r53 shr 8)) e.bY = e.aq
        else e.ag = if (r0 >= 0) r53 * r03 else -(r53 * r03)
    } else {
        if (r04 > r06) e.ag = if (r0 >= 0) (r03 shl 8) else -(r03 shl 8)
        else e.bY = e.aq
        var r54 = 0
        if (e.ag != 0) r54 = (r05 shl 8) / r04
        if (r05 <= (r54 shr 8)) e.bZ = e.ar
        else e.ah = if (r02 >= 0) r54 * r03 else -(r54 * r03)
    }
    e.ah += w.kY                                   // L96 tail — all paths
    e.posFromWaypoint(w)
    return false
}

// ============================================================================
// Slice 34 — i.aP() ax29 boss duel FSM (i.java:10328-11076)
// ============================================================================

/** `d.a` (d.java:9, proven): the `aQ()` attack-pick table —
 *  `{16,15,7,17,9,8,5,14,10,33}`; by3 remaps value 5→40, 6→39
 *  (i.java:11159-11169). */
private val BOSS_PICK_TABLE = intArrayOf(16, 15, 7, 17, 9, 8, 5, 14, 10, 33)

/** `i.aQ()` (i.java:11108-11170, proven): the boss attack picker.
 *  `r9` bands: by0 → table[7]; by3 `ci[2]>=160` idle-window → 3;
 *  `ci[3]>=80 && v() && aB<=500` → finisher arm (by1 first-time → 9 +
 *  `e(17)` + `cm`, else 7); `r0>100` → `ci[1]>=48` idle-window → 2,
 *  else `4|8(by3)`; `60<r0<=100` → `ci[1]>=48&&aS.aZ` → 6 +
 *  `a(true,0)`, `ci[0]>=32` → 1; `r0<=60` → `ci[0]>=32` → 0 else 5.
 *  `r02<0||>=10` → L124 `i(0)`. */
private fun NpcFsm.bossPick(e: Entity) {
    val w = world
    val p = w.player
    val ci = w.iCi
    val r0 = kotlin.math.abs(p.ak - e.ak)
    var r9 = -1
    var r02 = -1
    val idleSet = p.S == 0 || p.S == 1 || p.S == 7 || p.S == 12 ||
        p.S == 79 || p.S == 32 || p.S == 6
    if (w.iBy == 0) {
        e.ah = 0; e.ag = 0
        r02 = 7
    } else {
        var done = false
        // L6 — by3 counter-window (ci[2]>=160, player idle, in-play)
        if (w.iBy == 3 && (ci?.get(2) ?: 0) >= 160 && e.inPlayV(w) &&
            idleSet && p.S != 9 && p.S != 375) {
            e.ah = 0; e.ag = 0
            ci?.let { it[2] = 0 }
            r02 = 3
        }
        // L31 — finisher/barrage arm (ci[3]>=80 && v() && aB<=500)
        if ((ci?.get(3) ?: 0) >= 80 && e.inPlayV(w) && e.aB <= 500) {
            e.ah = 0; e.ag = 0
            var r92 = 7
            if (w.iBy == 1 && !w.iCm) {
                r92 = 9
                e.bossAura(w, 17, e.ak, e.al, e.az - 1)
                w.iCm = true
            }
            ci?.let { it[3] = 0 }
            r02 = r92
            done = true
        }
        if (!done) {
            // L44 — distance bands
            if (r0 > 100) {
                if ((ci?.get(1) ?: 0) >= 48 && e.inPlayV(w) &&
                    idleSet && p.S != 9 && p.S != 375) {
                    e.ah = 0; e.ag = 0
                    r9 = 2
                    ci?.let { it[1] = 0 }
                    w.iCj = false
                } else {
                    r9 = if (w.iBy == 3) 8 else 4
                }
            } else if (r0 > 60) {
                if ((ci?.get(1) ?: 0) >= 48 && p.aZ) {
                    e.ah = 0; e.ag = 0
                    r9 = 6
                    e.startTrail()
                }
                if ((ci?.get(0) ?: 0) >= 32) {
                    e.ah = 0; e.ag = 0
                    r9 = 1
                    ci?.let { it[0] = 0 }
                }
            } else {
                if ((ci?.get(0) ?: 0) >= 32) {
                    e.ah = 0; e.ag = 0
                    r9 = 0
                    ci?.let { it[0] = 0 }
                } else {
                    r9 = 5
                }
            }
            r02 = r9                                          // L89
        }
    }
    // L90 — table lookup; L124 = i(0) for r02<0 or >=len
    if (r02 < 0 || r02 >= BOSS_PICK_TABLE.size) {
        e.ah = 0; e.ag = 0
        e.setAnim(0)
        return
    }
    w.iCi?.let { it[4] = 0 }
    var r8 = BOSS_PICK_TABLE[r02]
    if (w.iBy == 3) {
        if (r8 == 5) r8 = 40
        if (r8 == 6) r8 = 39
    }
    e.setAnim(r8)
}

/** `i.aP()` (i.java:10328-11076, proven): the ax29 boss duel FSM.
 *  Re-pins `k.aU`/`P|=16` every tick; `by==2` dormant. The by-switch
 *  arms `r0` (counter eligibility): by0 `false`; by1 while
 *  `S∈{8,0,9,11}` or `S==7&&T>9`; by3 unconditional `true`. L23
 *  `r0 && W∩aS.X` runs the
 *  by-specific counter/stagger arm. Tail: `ci[5]` counters, `cn` ramp,
 *  by3 exhaust, S8 settle, arena clamp, then the L215 S-switch. */
fun NpcFsm.tickBoss(e: Entity, player: Entity, pad: Pad) {
    val w = world
    val p = player
    w.kAU = e
    e.P = e.P or 16
    if (w.iBy == 2) return
    // by-switch → r0 (L6-L22, i.java:11057-11075 — the decompiler merges
    // `goto L23` after each arm: L7 by0 r0=false; L9 by1 state-gated;
    // L21 by3 `r0 = true` unconditional; L22 default r0=false)
    var r0 = false
    when (w.iBy) {
        0 -> r0 = false                       // L7 — by0 has no counter arm anyway
        1 -> {
            if (e.S == 11 || e.S == 8 || e.S == 0 || e.S == 9) r0 = true
            else if (e.S == 7 && e.T > 9) r0 = true
        }
        3 -> r0 = true                        // L21 — unconditional
        else -> r0 = false
    }
    // L23 — counter/stagger eligibility (box overlap + r0)
    if (r0 && Entity.overlapI(e.W, p.X)) {
        when (w.iBy) {
            1 -> {
                // L29 — RNG counter vs stagger (200+cn / 1000)
                if (w.jRand(0, 1000) <= 200 + w.iCn) {
                    // L32 r02 arm — counter pose + player-punish
                    e.ah = 0; e.ag = 0
                    e.setAnim(21)
                    if (p.X[0] != p.X[2] && Entity.overlapI(e.W, p.X) &&
                        w.playerAttacking() && p.S != 8) {
                        p.setAnim(8)
                        w.kE?.let { it.P = it.P or 128 }
                    }
                } else {
                    // L42 — stagger: save S, S20, spark, aB-40
                    w.iCo = e.S
                    e.ah = 0; e.ag = 0
                    e.endTrail()
                    e.setAnim(20)
                    w.sfx(32)                                   // k.A(32)
                    e.spawnFx8(w, 50, 1, e.av, e.ak, e.al - 40, 300)
                    e.aB -= 40
                    if (e.aB <= 300) {
                        e.setAnim(13)
                        // q(Z[3]) ax10 S55 → face flip
                        val r03 = w.npcs.firstOrNull { it.aw == e.Z[3] }
                            ?: if (p.aw == e.Z[3]) p else null
                        if (r03 != null && r03.ax == 10 && r03.S == 55) {
                            e.av = r03.ak < e.ak
                        }
                    }
                }
            }
            3 -> {
                // L56 — S28/4 stagger arm; else attack-window counter
                if (e.S == 28 || e.S == 4) {
                    w.iCo = e.S
                    e.ah = 0; e.ag = 0
                    e.endTrail()
                    w.iCk?.let { it.P = it.P or 128; it.P = it.P or 32 }
                    e.setAnim(20)
                    w.sfx(32)
                    e.spawnFx8(w, 50, 1, e.av, e.ak, e.al - 40, 300)
                    e.aB -= 40
                    if (e.aB <= 0) {
                        e.setAnim(13)
                        val r04 = w.npcs.firstOrNull { it.aw == e.Z[4] }
                            ?: if (p.aw == e.Z[4]) p else null
                        if (r04 != null && r04.ax == 10 && r04.S == 55) {
                            e.av = r04.ak < e.ak
                        }
                    }
                } else if (e.S != 20 && e.S != 26) {
                    if (p.X[0] != p.X[2] && Entity.overlapI(e.W, p.X) &&
                        w.playerAttacking() && p.S != 8) {
                        p.setAnim(8)
                        w.kE?.let { it.P = it.P or 128 }
                    }
                }
            }
        }
    }
    // L89-L104 — r05 gate: S∈{14,35,15,16,7,17,4} skips ci increment
    val r05 = e.S == 14 || e.S == 35 || e.S == 15 || e.S == 16 ||
        e.S == 7 || e.S == 17 || e.S == 4
    if (!r05 && w.iBy >= 1) {
        var ci = w.iCi
        if (ci == null || ci.size < 5) {
            ci = IntArray(5)
            w.iCi = ci
        }
        when (w.iBy) {
            1 -> { ci[0]++; ci[1]++; ci[3]++ }
            3 -> { ci[0]++; ci[1]++; ci[2]++; ci[3]++ }
        }
    }
    // L123/L130 — cn ramp: S20@T0 +100 cap 800; S21 clears
    if (e.S == 20 && e.T == 0) {
        w.iCn = w.iCn + 100
        if (w.iCn >= 800) w.iCn = 800
    }
    if (e.S == 21) w.iCn = 0
    // L133 — a() (push-past/mount check) skipped for attack states
    if (e.S != 2 && e.S != 38 && e.S != 4 && e.S != 5 && e.S != 40 &&
        e.S != 13 && e.S != 26 && e.S != 17 && e.S != 8 && e.S != 39) {
        bossPushPast(e)
    }
    // L154-L169 — by3 exhaust: cp>=48 → S28 i(0) / S20 i(16)+e(5)
    if (w.iBy == 3 && (e.S == 28 || e.S == 20)) {
        w.iCp = w.iCp + 1
        if (w.iCp >= 48) {
            if (e.S == 28) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
                w.iCp = 0
            } else if (e.S == 20) {
                e.ah = 0; e.ag = 0
                e.setAnim(16)
                e.bossAura(w, 5, e.ak, e.al, e.az + 1)
                w.iCi?.let { it[0] = 0 }
                w.iCp = 0
            }
        }
    }
    // L169-L184 — S8 falls straight into L184; the arena clamp arm
    // covers S∈{8,39,20,21&&r(),5,40,4}, each starting `a(true);t()`.
    val arenaArm = e.S == 8 || e.S == 39 || e.S == 20 ||
        (e.S == 21 && e.animFinished()) || e.S == 5 || e.S == 40 || e.S == 4
    if (arenaArm) {
        e.settleToGround(w)
        e.refreshBoxes()
        var r12 = w.kR
        var r13 = w.kSBound
        // L187: S∈{5,4,40} clamp against the `k.ac[]` arena rect instead
        if (e.S == 5 || e.S == 4 || e.S == 40) {
            r12 = w.kAc?.get(0) ?: r12
            r13 = w.kAc?.get(2) ?: r13
        }
        if (e.W[0] <= r12 || e.W[2] >= r13) {
            if (!e.av) e.ak = r12 + (e.ak - e.W[0])
            else e.ak = r13 + (e.ak - e.W[2])
            if (e.S == 5 || e.S == 4 || e.S == 40) {
                e.ah = 0; e.ag = 0
                w.iCk?.let { it.P = it.P or 128; it.P = it.P or 32 }
            } else {
                e.ah = 0; e.ag = 0
                if (w.iBy == 3) e.setAnim(38) else e.setAnim(2)
                e.startTrail()
            }
        }
    }
    // L215 — the S-switch
    when (e.S) {
        // L216 — S13 stagger-retreat (away from facing)
        13 -> { e.ag = if (e.av) -1280 else 1280 }
        // L220 — S14/35 barrage: face, punish-check, spawn knives
        14, 35 -> {
            e.facePlayer(w)
            if (p.X[0] != p.X[2] && Entity.overlapI(e.W, p.X) &&
                w.playerAttacking() && p.S != 8) {
                p.setAnim(8)
                w.kE?.let { it.P = it.P or 128 }
            }
            val r122 = if (w.iBy == 3) 3 else 1
            if (e.X[0] != e.X[2]) {
                for (r132 in 0 until r122) {
                    var r14 = p.al
                    if (!p.aZ) {
                        // i.e(cx,cy) cell read — player-specific arms
                        // unreachable on the boss (inferred collisionCell)
                        val r016 = w.collisionCell(p.ak / 20, r14 / 20)
                        if (r016 < 12 && r016 != 5 && r016 != 3) r14 += 10
                    }
                    var r15 = p.ak
                    if (r122 == 3) r15 = p.ak + (r132 - 1) * 100
                    e.spawnPathFx(w, e.X[0], e.X[1], r15, r14, 61, 71, 8, p.az + 1)
                    e.spawnBossFx(w, 9, r15, r14, e.az)
                }
            }
            if (e.animFinished()) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
            }
        }
        // L269 — S20 stagger walk (toward facing), ci[4]++
        20 -> {
            e.ag = if (e.av) 1024 else -1024
            w.iCi?.let { it[4]++ }
            if (e.animFinished()) {
                e.ah = 0; e.ag = 0
                if (w.iBy != 3) e.setAnim(0)
                else if (w.iCo == 28) e.setAnim(28) else e.setAnim(0)
            }
        }
        // L300+L302 — S15/16/6: inert + punish-check + r()→i(0)
        15, 16, 6 -> {
            e.ah = 0; e.ag = 0
            if (p.X[0] != p.X[2] && Entity.overlapI(e.W, p.X) &&
                w.playerAttacking() && p.S != 8) {
                p.setAnim(8)
                w.kE?.let { it.P = it.P or 128 }
            }
            if (e.animFinished()) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
            }
        }
        // L314 — S7 grab-QTE
        7 -> {
            e.ah = 0; e.ag = 0
            if (e.T <= 6) {
                w.gR = true
                p.setAnim(1)
                p.ah = 0; p.ag = 0
                w.kE?.let { it.P = it.P or 128 }
                p.av = !e.av
                if (pad.v(16388)) w.iCj = true
                if (!w.iCj) {
                    e.spawnMarker(w, 21, p.ak, p.al - 60)
                    e.ae?.let { it.ak = p.ak; it.al = p.al - 100 }
                } else {
                    e.releaseAe()
                }
            }
            if (e.T == 7) {
                w.gR = false
                e.timewarp(w, 4)                                 // b(4)
                e.lockInput(w)                                   // k.o()
                if (p.ad == null) {
                    p.ad = e.spawnChildFx(w, -999, 9, 100, 300)
                }
                p.ad?.let {
                    it.av = e.av
                    it.ak = if (e.av) e.W[0] else e.W[2]
                    it.al = e.al - 50
                    it.ag = if (e.av) -38400 else 38400
                }
                if (w.iCj) {
                    p.setAnim(10)
                    p.ag = if (p.av) 4096 else -4096
                } else {
                    p.applyHit(4, 0, e, w)      // aS.a(4,0,0,this) — arg pair collapsed
                }
                e.releaseAe()                                    // G()
                w.sfx(16)                                        // k.A(16)
            }
            if (e.T == 9) {
                e.timewarpOff(w)                                 // O()
                if (w.iCj) {
                    e.ah = 0; e.ag = 0
                    if (w.iBy == 3) {
                        e.facePlayer(w)
                        e.ah = 0; e.ag = 0
                        e.setAnim(3)
                        e.bossAura(w, 0, e.ak, e.al, e.az + 1)
                    }
                    w.iCj = false
                    e.unlockInput(w)                             // k.p()
                }
            }
            if (e.animFinished()) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
                w.iCj = false
                e.unlockInput(w)
            }
        }
        // L373 — S8/39 stalk (keep walking while |dx|<=60)
        8, 39 -> {
            e.facePlayer(w)
            e.ag = if (e.av) 1024 else -1024
            if (kotlin.math.abs(p.ak - e.ak) > 60) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
            }
        }
        // L363 — S9/37 retreat-stalk: |dx|<100 → i(0); r()→Q+aQ
        9, 37 -> {
            e.facePlayer(w)
            e.ag = if (e.av) -1536 else 1536
            if (kotlin.math.abs(p.ak - e.ak) < 100) {
                e.ah = 0; e.ag = 0
                e.setAnim(0)
            } else if (e.animFinished()) {
                e.facePlayer(w)
                bossPick(e)
            }
        }
        // L388 — S4 strike: lunge, W∩aS.W → i(6)+e(2); r()→trail-end+i(0)
        4 -> {
            e.ag = if (e.av) 2560 else -2560
            if (Entity.overlapI(e.W, p.W)) {
                e.ah = 0; e.ag = 0
                e.setAnim(6)
                e.bossAura(w, 2, e.ak, e.al, 300)
            }
            if (e.animFinished()) {
                e.endTrail()
                e.ah = 0; e.ag = 0
                e.setAnim(0)
                w.iCk?.let { it.P = it.P or 128; it.P = it.P or 32 }
            }
        }
        // L442 — S28: just face
        28 -> e.facePlayer(w)
        // L458 — S26 block: k.E arm, marker-80, 65568-counter via ax5
        26 -> {
            e.facePlayer(w)
            w.kE?.let { it.P = it.P or 128 }
            e.spawnMarker(w, 80, e.ak, e.al - 85)
            if (Entity.overlapI(e.W, p.W) || Entity.overlapI(e.W, p.X)) {
                if (pad.v(65568)) {
                    p.ah = 0; p.ag = 0
                    e.releaseAe()
                    val r133 = e.Z[if (w.iBy == 3) 2 else 1]
                    val r023 = w.npcs.firstOrNull { it.aw == r133 }
                        ?: if (p.aw == r133) p else null
                    if (r023 != null && r023.ax == 5) {
                        r023.P = r023.P or 16
                        r023.claimKC(w)
                    }
                }
            }
            if (e.animFinished()) {
                e.releaseAe()
                e.setAnim(27)
            }
        }
        // L255 — S21 counter pose: punish-check + r()→Q+aQ
        21 -> {
            if (p.X[0] != p.X[2] && Entity.overlapI(e.W, p.X) &&
                w.playerAttacking() && p.S != 8) {
                p.setAnim(8)
                w.kE?.let { it.P = it.P or 128 }
            }
            if (e.animFinished()) {
                e.facePlayer(w)
                if (e.animFinished()) bossPick(e)
            }
        }
        // L285 — S0/36 idle: cl ax61 add + ci[4]>=16 → 41|aQ
        0, 36 -> {
            if (w.iCl == null) {
                val aK = e.spawnChildFx(w, 61, 71, 19, 99)
                aK.ak = e.ak; aK.al = e.al
                w.iCl = aK
                w.queueInsert(aK)
            }
            w.iCl?.let {
                it.az = e.az - 1
                it.P = it.P and -129
                it.P = it.P or 16
                it.P = it.P or 512
            }
            e.endTrail()
            e.facePlayer(w)
            w.iCi?.let { it[4]++ }
            if ((w.iCi?.get(4) ?: 0) >= 16) {
                if (w.iBy == 3) e.setAnim(41) else bossPick(e)
            }
        }
        // L381 — S41 by3 opener: r()→aQ
        41 -> {
            if (e.animFinished()) bossPick(e)
        }
        // L385 — S3 windup: r()→Q+i(4)+e(1)+a(true,0)
        3 -> {
            if (e.animFinished()) {
                e.facePlayer(w)
                e.ah = 0; e.ag = 0
                e.setAnim(4)
                e.bossAura(w, 1, e.ak, e.al, 300)
                e.startTrail()
            }
        }
        // L401 — S2/38/5/40 advance: vel by S, r()→idle-check→grab|idle
        2, 38, 5, 40 -> {
            if (e.S == 2 || e.S == 38) e.ag = if (e.av) -5120 else 5120
            else e.ag = if (e.av) 3840 else -3840
            if (e.animFinished()) {
                e.endTrail()
                e.ah = 0; e.ag = 0
                e.facePlayer(w)
                val idle = p.S == 0 || p.S == 1 || p.S == 7 || p.S == 12 ||
                    w.playerAttacking() || p.S == 79 || p.S == 32 || p.S == 6
                if (idle && e.inPlayV(w) && p.S != 9 && p.S != 375) {
                    e.setAnim(7)
                    w.iCi?.let { it[1] = 0 }
                } else {
                    e.ah = 0; e.ag = 0
                    e.setAnim(0)
                }
                w.iCj = false
            }
        }
        // L445 — S17 finisher: T==len-3 snap player + i(370) + d(11)
        17 -> {
            val clip = e.clip
            if (clip != null && e.T == clip.frameCount(e.S) - 3) {
                p.ah = 0; p.ag = 0
                p.aj = 0; p.ai = 0
                p.al = e.al
                p.setAnim(370)
                e.spawnBossFx(w, 11, p.ak, p.al, 99)
            }
            if (e.animFinished()) e.P = e.P or 64
        }
        // L452 — S25 outro: r()→k.E arm + i(26)
        25 -> {
            if (e.animFinished()) {
                w.kE?.let { it.P = it.P or 128 }
                e.ah = 0; e.ag = 0
                e.setAnim(26)
            }
        }
        // L480 — S27: r()→aB+=160 + i(0)
        27 -> {
            if (e.animFinished()) {
                e.aB += 160
                e.setAnim(0)
            }
        }
        // L484 — S33: r()→i(14)+ci[3]=0
        33 -> {
            if (e.animFinished()) {
                e.setAnim(14)
                w.iCi?.let { it[3] = 0 }
            }
        }
        // L495 — all other states inert
        else -> {}
    }
}

/** `i.a()` (i.java:914, reachable subset for ax29): the push-past arm —
 *  `W∩aS.W` while `aS.S<=43` and the player walks into the boss pushes
 *  the player out to the box edge (`ai=0`, `ag=0` via the L63 tail).
 *  The ax15-mount / S131/146 / `g.a` / `S==139` guards stay as
 *  early-outs; `k.aS.S==6&&ax==11` and `S==18&&aS.S==12` are proven
 *  skips. `y()` = wall-in-motion-direction. */
private fun NpcFsm.bossPushPast(e: Entity) {
    val w = world
    val p = w.player
    if (e.S == 139) return
    if (p.S == 6 && e.ax == 11) return
    if (e.S == 18 && p.S == 12) return
    if (e.S == 131 || e.S == 146) return
    if (!Entity.overlapI(p.W, e.W)) return
    if (p.S > 43) return
    val pHalf = (p.W[2] - p.W[0]) / 2
    val eHalf = (e.W[2] - e.W[0]) / 2
    if (p.ak < e.ak && p.ag < 0 && !p.forwardWall()) {
        p.ak = e.ak - pHalf - eHalf
        p.ai = 0
        p.ag = -1
    } else if (p.ak > e.ak && p.ag > 0 && !p.forwardWall()) {
        p.ak = e.ak + pHalf + eHalf
        p.ai = 0
        p.ag = 1
    } else {
        return
    }
    p.settleToGround(w)
    p.ag = 0
}

// ---------------------------------------------------------------------------
// ax35 — `i.bQ()` (i.java:21594-22296): the scripted multi-tool entity —
// dove/crow messenger, pickup grabber, bird-wave director, flying-aim proxy.
// Init arm (i.java:3614) uses a sparse Z map; records carrying ONLY [az]
// (skip Z1-17) still run bQ on the default Zs, which hands them the generic
// aC=10→"fire+expire" arms 27/31.
// ---------------------------------------------------------------------------

/** `k.bd[]` sweep hit types (i.java:21744-21749 filter). */
private val AX35_SWEEP_AX = intArrayOf(17, 11, 23, 47, 50, 73)
/** `i.cX` (i.java:22362, proven) — per-wave-count gap delays {0,1,2,3,4}. */
private val AX35_WAVE_GAP = intArrayOf(0, 1, 2, 3, 4)

/**
 * `initAx35` — ax35 init arm (i.java:3614, proven). Sparse record map:
 * `az=r8[7]; Z[0..6]=r8[8..14]; Z[10]=r8[15]; Z[11]=r8[16]; Z[13]=r8[17]`,
 * then `Z[7]=0; Z[12]=1` — NOT the generic `Z[i]=f[7+i]` layout.
 */
fun NpcFsm.initAx35(e: Entity, f: List<Int>, w: LevelCellSource) {
    e.az = if (f.size > 7) f[7] else 0
    for (i in 0..6) e.Z[i] = if (f.size > 8 + i) f[8 + i] else 0
    e.Z[10] = if (f.size > 15) f[15] else 0
    e.Z[11] = if (f.size > 16) f[16] else 0
    e.Z[13] = if (f.size > 17) f[17] else 0
    e.Z[7] = 0
    e.Z[12] = 1
    e.setAnim(if (f.size > 5) f[5] else 0)                    // L395 i(r8[5])
    e.refreshBoxes()                                          // t()
}

/** The ax35 `a(x,y,w,h,bool)` debug vol-paint (i.java:22224, proven
 *  signature): rasterizes the `ak+Z[0], al+Z[1], Z[2]xZ[3]` rect plus
 *  overlapping ax{11,73,35,79} entities into the `k.aQ` debug Image.
 *  The r14 flag is dead (never read) — the port records the painted
 *  rect (`inferred`: `k.aQ`'s only reader is a HUD blit). */
private fun ax35VolPaint(e: Entity, w: LevelCellSource) {
    w.volPaintRect = intArrayOf(e.ak + e.Z[0], e.al + e.Z[1],
        e.Z[2], e.Z[3])
}

/** `i.bR()` (i.java:22194, proven): quadrant pick — the sequential
 *  first-match ladder `aG∈[18,36]∪[144,162]→1; [36,54]∪[126,144]→2;
 *  [54,72]∪[108,126]→3; else 0`. Boundaries follow the source order
 *  (36→1, 54→2, 72→3, 126→2, 144→1). */
private fun ax35Quadrant(aG: Int): Int = when {
    aG in 18..36 || aG in 144..162 -> 1
    aG in 36..54 || aG in 126..144 -> 2
    aG in 54..72 || aG in 108..126 -> 3
    else -> 0
}

/** `i.f(int,int,int,int)` (i.java:22174, proven): wave-child spawn —
 *  `e(r7/20,r8/20)==20` aborts; `aG=r10` on THIS entity; child is
 *  `a(35,62,6+bR(),300)` with `ak=r7, al=r8, P|=16, av=(r7>r9), aG=r10`,
 *  inserted via `k.b(aK)`. */
private fun ax35WaveChild(e: Entity, w: LevelCellSource, r7: Int, r8: Int,
                          r9: Int, r10: Int) {
    if (w.collisionCell(r7 / 20, r8 / 20) == 20) return
    e.aG = r10
    val c = e.spawnChildFx(w, 35, 62, 6 + ax35Quadrant(e.aG), 300)
    c.ak = r7; c.al = r8; c.P = c.P or 16; c.av = r7 > r9; c.aG = r10
    w.queueInsert(c)
}

/** The bare `a(35,62,S,300)` + `k.b` spawn shared by the wave/director
 *  arms — position written after the copy-position helper. */
private fun ax35SpawnMarker(e: Entity, w: LevelCellSource, S: Int,
                            x: Int, y: Int) {
    val c = e.spawnChildFx(w, 35, 62, S, 300)
    c.ak = x; c.al = y
    w.queueInsert(c)
}

/** The `af==null → (Z[13]>0 → af=q(Z[13]))` resolve shared by the
 *  af-gated arms (i.java:21842/21876/21963/22033/22047/22074). */
private fun ax35AfResolve(e: Entity, w: LevelCellSource) {
    if (e.af == null && e.Z[13] > 0) e.af = w.findByAw(e.Z[13])
}

/** The L240/L242-linked `af.P &= -129; af.P |= 16` pair — runs only
 *  when the af link was just resolved non-null (i.java:22051/22078). */
private fun ax35AfArmFlags(e: Entity) {
    e.af?.let { it.P = (it.P and -129) or 16 }
}

/** The L97 wave-director arm shared by arms 2/27/31 (i.java:21650-
 *  21728, proven): vol-paint → `r()` → `P|=64; aC--` → `aC>0` tail; else
 *  `aC=2; Z[7]++` → `Z[7]>Z[12]` tail; `r8 = Z[12]>2?3:2`;
 *  `r04=(Z[7]-r8)*Z[6]`; fire-x `r9 = S∈{27,31} ? Z[8] : r04+Z[8]`;
 *  march `r11=r9; r122=Z[9]; do {r11-=r05; r122-=r06} while r122>=k.P`
 *  (`r05/r06 = 20·sin/cos(Z[5]·M/360)>>8`); S∈{2,31}→L124 spawn the
 *  S18 marker at `(r9+36·sin, Z[9]+36·cos)` + `f()` child (+S31 clears
 *  P64 & `i(29)`); else (S27) `f()` + `aC=10; P&=-65; i(25)`; all paths
 *  end at the L129 tail. */
private fun ax35WaveStep(e: Entity, w: LevelCellSource) {
    ax35VolPaint(e, w)
    if (!e.animFinished()) return                               // L368
    e.P = e.P or 64; e.aC--
    if (e.aC > 0) { ax35WaveTail(e, w); return }
    e.aC = 2; e.Z[7]++
    if (e.Z[7] > e.Z[12]) { ax35WaveTail(e, w); return }
    val r8 = if (e.Z[12] > 2) 3 else 2                          // L110
    val r04 = (e.Z[7] - r8) * e.Z[6]
    val r9 = if (e.S == 27 || e.S == 31) e.Z[8] else r04 + e.Z[8]
    val th = e.Z[5] * Trig.M / 360
    val r05 = (20 * Trig.sin(th)) shr 8
    val r06 = (20 * Trig.sin(Trig.N - th)) shr 8   // j.b(j.n-θ) = cos
    var r11 = r9
    var r122 = e.Z[9]
    do {                                                        // L118
        r11 -= r05; r122 -= r06
    } while (r122 >= w.kP)
    if (e.S == 2 || e.S == 31) {                                // L124
        ax35SpawnMarker(e, w, 18,
            r9 + ((36 * Trig.sin(th)) shr 8),
            e.Z[9] + ((36 * Trig.sin(Trig.N - th)) shr 8))
        ax35WaveChild(e, w, r11, r122, r9, e.Z[5])
        if (e.S == 31) { e.P = e.P and -65; e.setAnim(29) }
    } else {                                                    // L123 (S27)
        ax35WaveChild(e, w, r11, r122, r9, e.Z[5])
        e.aC = 10; e.P = e.P and -65; e.setAnim(25)
    }
    ax35WaveTail(e, w)                                          // →L129
}

/** L129 (i.java:21709-21728, proven): wave tail — `Z[7]<=Z[12]` returns;
 *  else `aC=10; P&=-65; aF++`; `Z[12]>=5 → Z[12]=5` (saturate) else
 *  `aF < cX[Z[12]]` gap → same anim pick; else `aF=0; Z[12]++` → pick.
 *  L137 anim: `S==31 → i(29)` else `i(0)`. */
private fun ax35WaveTail(e: Entity, w: LevelCellSource) {
    if (e.Z[7] <= e.Z[12]) return
    e.aC = 10
    e.P = e.P and -65
    e.aF++
    if (e.Z[12] >= 5) {
        e.Z[12] = 5                                             // L137
    } else if (e.aF >= AX35_WAVE_GAP[e.Z[12]]) {                // L134
        e.aF = 0
        e.Z[12]++
    }
    if (e.S == 31) e.setAnim(29) else e.setAnim(0)              // L137/L140
}

/** The `ae!=null` cleanup shared by arms 22/23 (i.java:22104-22109 +
 *  22156-22161): `g.a==this → G()` else `ae.P &= -129`. */
private fun ax35AeCleanup(e: Entity, p: Entity) {
    val ae = e.ae ?: return
    if (p.ga === e) e.releaseAe() else ae.P = ae.P and -129
}

/**
 * `i.bQ()` (i.java:21594-22296, proven) — ax35 multi-tool:
 * S0 L5 (af resolve; af dead/absent → `k.aQ=null; P=32`; else `af.P|=16`
 *   + paint + `aC--` → `i(1); af.i(191)` at expiry);
 * S29 L26 (same resolve/dead check; alive → `k.aQ=null` hold);
 * S1·30 L39 (af absent/dead → `S30?i(29):i(0)`; else paint + `r()` →
 *   capture `Z[8]=aS.ak; Z[9]=aS.W-midY; Z[7]=0; aC=0`; S30→`Z[12]=1;
 *   i(31)` else `i(2)`; `k.A(27)`);
 * S25 L61 (resolve → paint + `aC--` → `i(26)` + spawn `a(35,62,28,300)`
 *   aim-child at `k.ae`(ax10) or `k.O+200,k.P+120`, `aK.af=this; c=aK`);
 * S2·31 L88 (af absent/dead → `S31?i(29):i(0)`; else L97 wave director);
 * S27→L97; S6-9 L142 (W-box player-hit: `p.S∈{0,1,7}→aS.i(9)` then
 *   `p.S==12` shared L267 deflect arm else `g.d(GU[au]);g.t=5`; `k.bd`
 *   sweep → `as()` on overlap; `aG`-march `20·sin/cos` commit-if-free
 *   → `i(10+bR());aC=20` on cell20; edge despawn `±20` past `k.O`);
 * S10-13 L196 (`r()→i(14+bR())`); S14-17 L200 (`!b(Y,k.ac)→k.c`);
 * S18/19 L204 (`r()→k.c`); S20 L275 (vol-edge overlap → `aC=Z[11]`
 *   + `a(7,ak-60,al+20)` marker + `ae.P|=128` + `q(Z[10])→{i(4);P=16}`
 *   + `a(35,62,19,300)` + `am=ak-420; an/ao/ap=al/ak/al; ak=am; i(21)`);
 * S21 L283 (P&128 → `q(Z[10])` gate {S==3→P=16; null→aC--→P=16} else
 *   `ak+=50`; `ak>=ao→ak=ao;i(22)`); S22 L301 (ae cleanup + `r()→i(23)`);
 * S23 L311 (ae cleanup + `t()` + `g.a!=this→L323` grab-scan
 *   `g.b(S)&&a(W,W)→(S==264?i(262):i(260))+vel0+aS.al=al;g.a=this`;
 *   `g.a==this && !a(W,W)&&S∉{261,259}→g.a=null`; `r()→i(24);G()`);
 * S24 L347 (`g.a==this→{g.a=null; aS.a(2560)}`; `r()→P=128;i(20)`);
 * S26 L79 (paint + `r()→aC=0;Z[7]=0;Z[12]=1;i(27);k.A(27)`);
 * S3 L240 (af resolve → `af.P&=-129|16` when newly resolved + paint);
 * S4 L253 (same + `r()→i(5);k.A(27)`); S5 L267 (paint + `r()→i(3)`);
 * S28 L208 (`!k.aT→k.c`; else-if `k.u` D-pad ±10 clamps to the view
 *   band; L232 `af!=null→af.Z[8]=ak;af.Z[9]=al` else `r()→k.c`).
 */
fun NpcFsm.tickAx35(e: Entity, w: LevelCellSource, p: Entity) {
    when (e.S) {
        0 -> {                                                  // L5
            ax35AfResolve(e, w)
            val af = e.af
            if (af == null || af.deadRelease()) {               // L13
                w.volPaintRect = null; e.P = 32; return
            }
            af.P = af.P or 16                                   // L12
            ax35VolPaint(e, w)
            e.aC--
            if (e.aC < 0) { e.setAnim(1); af.setAnim(191) }
        }
        29 -> {                                                 // L26
            ax35AfResolve(e, w)
            val af = e.af
            if (af == null || af.deadRelease()) {               // L34
                w.volPaintRect = null; e.P = 32
            } else {
                w.volPaintRect = null                           // L33
            }
        }
        1, 30 -> {                                              // L39
            val af = e.af
            if (af == null || af.deadRelease()) {               // L43/L46
                if (e.S == 30) e.setAnim(29) else e.setAnim(0)
                return
            }
            ax35VolPaint(e, w)
            if (e.animFinished()) {
                e.aC = 0
                e.Z[8] = p.ak
                e.Z[9] = (p.W[1] + p.W[3]) shr 1
                e.Z[7] = 0
                if (e.S == 30) { e.Z[12] = 1; e.setAnim(31) }   // L57
                else e.setAnim(2)
                w.sfx(27)                                       // L58 k.A(27)
            }
        }
        25 -> {                                                 // L61
            ax35AfResolve(e, w)                                 // L63→L65
            ax35VolPaint(e, w)
            e.aC--
            if (e.aC < 0) {
                e.setAnim(26)
                if (e.c == null) {                              // L366
                    val c = e.spawnChildFx(w, 35, 62, 28, 300)
                    val m = w.kAe
                    if (m != null && m.ax == 10) { c.ak = m.ak; c.al = m.al }
                    else { c.ak = w.kO + 200; c.al = w.kP + 120 }
                    c.av = e.av; c.af = e; e.c = c
                    w.queueInsert(c)
                }
            }
        }
        2, 31 -> {                                              // L88
            val af = e.af
            if (af == null || af.deadRelease()) {               // L92
                if (e.S == 31) e.setAnim(29) else e.setAnim(0)
                return
            }
            ax35WaveStep(e, w)                                  // L97
        }
        27 -> ax35WaveStep(e, w)                                // L97
        in 6..9 -> {                                            // L142
            e.refreshBoxes()
            if (p.gt <= 0 &&
                    Entity.overlapStrict(e.W, p.W) && !w.kAn) {  // L145
                if (p.S == 0 || p.S == 1 || p.S == 7) {
                    p.setAnim(9)                                 // L156→L157
                }
                if (p.S == 12) {                                 // L267 arm
                    ax35VolPaint(e, w)
                    if (e.animFinished()) e.setAnim(3)
                } else {
                    p.gDrain(w.GU[w.weaponSlot], w); p.gt = 5    // L157
                }
            }
            for (o in w.npcs) {                                  // L160 sweep
                if (o.ax !in AX35_SWEEP_AX) continue
                if (o.deadRelease()) continue
                if (Entity.overlapStrict(e.W, o.W)) o.instantKill()
            }
            val th = e.aG * Trig.M / 360                         // L181 march
            val nx = e.ak + ((20 * Trig.sin(th)) shr 8)
            val ny = e.al + ((20 * Trig.sin(Trig.N - th)) shr 8)
            if (w.collisionCell(nx / 20, ny / 20) == 20) {
                e.setAnim(10 + ax35Quadrant(e.aG)); e.aC = 20
            } else {
                e.ak = nx; e.al = ny                             // L184
            }
            if (e.av) {                                          // L186
                if (e.ak < w.kO - 20) { w.removeEntity(e); e.aC = 0 }
            } else if (e.ak > w.kO + 420) {
                w.removeEntity(e); e.aC = 0
            }
        }
        in 10..13 -> {                                          // L196
            if (e.animFinished()) e.setAnim(14 + ax35Quadrant(e.aG))
        }
        in 14..17 -> {                                          // L200
            val cam = w.kAc
            if (cam == null || !Entity.containRect(e.Y, cam)) w.removeEntity(e)
        }
        18, 19 -> if (e.animFinished()) w.removeEntity(e)       // L204
        20 -> {                                                 // L275
            e.refreshBoxes()
            val x0 = e.ak + e.Z[0]; val y0 = e.al + e.Z[1]
            if (Entity.edgeRectOverlap(x0, y0,
                    x0 + e.Z[2], y0 + e.Z[3], p.W)) {
                e.aC = e.Z[11]
                e.spawnAeMarker(w, 7, e.ak - 60, e.al + 20)     // a(7,…)
                e.ae?.let { it.P = it.P or 128 }
                w.findByAw(e.Z[10])?.let { it.setAnim(4); it.P = 16 }
                ax35SpawnMarker(e, w, 19, e.ak, e.al)
                e.am = (e.ak - 400) - 20
                e.an = e.al
                e.ao = e.ak; e.ap = e.al
                e.ak = e.am
                e.setAnim(21)
            }
        }
        21 -> {                                                 // L283
            if (e.P and 128 != 0) {
                val l = w.findByAw(e.Z[10])
                if (l == null) {                                // L290
                    e.aC--
                    if (e.aC <= 0) e.P = 16
                } else if (l.S == 3) e.P = 16                   // L297 via
            } else {
                e.ak += 50                                      // L295
            }
            if (e.ak >= e.ao) { e.ak = e.ao; e.setAnim(22) }    // L297
        }
        22 -> {                                                 // L301
            ax35AeCleanup(e, p)
            if (e.animFinished()) e.setAnim(23)
        }
        23 -> {                                                 // L311
            ax35AeCleanup(e, p)                                  // L313/L315
            e.refreshBoxes()                                     // L316 t()
            if (p.ga === e) {                                    // L332
                if (!Entity.overlapStrict(p.W, e.W) &&
                        p.S != 261 && p.S != 259) p.ga = null
            } else if (p.S in Entity.GRABBABLE_STATES &&         // L323
                    Entity.overlapStrict(p.W, e.W)) {
                if (p.S == 264) p.setAnim(262) else p.setAnim(260)  // L329
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                p.al = e.al
                p.ga = e
            }
            if (e.animFinished()) { e.setAnim(24); e.releaseAe() }  // L343
        }
        24 -> {                                                 // L347
            if (p.ga === e) { p.ga = null; p.flingAirborne(2560, w) }
            if (e.animFinished()) { e.P = 128; e.setAnim(20) }
        }
        26 -> {                                                 // L79
            ax35VolPaint(e, w)
            if (e.animFinished()) {
                e.aC = 0; e.Z[7] = 0; e.Z[12] = 1
                e.setAnim(27); w.sfx(27)
            }
        }
        3 -> {                                                  // L240
            if (e.af == null) {                                  // L242
                if (e.Z[13] > 0) {
                    e.af = w.findByAw(e.Z[13])
                    ax35AfArmFlags(e)
                }
            }
            ax35VolPaint(e, w)                                   // L246
        }
        4 -> {                                                  // L253
            if (e.af == null) {                                  // L255
                if (e.Z[13] > 0) {
                    e.af = w.findByAw(e.Z[13])
                    ax35AfArmFlags(e)
                }
            }
            ax35VolPaint(e, w)                                   // L259
            if (e.animFinished()) { e.setAnim(5); w.sfx(27) }
        }
        5 -> {                                                  // L267
            ax35VolPaint(e, w)
            if (e.animFinished()) e.setAnim(3)
        }
        28 -> {                                                 // L208
            if (!w.kAT) { w.removeEntity(e); return }
            if (w.padDown(16388)) {                              // k.u held
                e.al -= 10; if (e.al < w.kP) e.al = w.kP
            } else if (w.padDown(33024)) {
                e.al += 10; if (e.al > w.kP + 240) e.al = w.kP + 240
            } else if (w.padDown(4112)) {
                e.ak -= 10; if (e.ak < w.kO) e.ak = w.kO
            } else if (w.padDown(8256)) {
                e.ak += 10; if (e.ak > w.kO + 400) e.ak = w.kO + 400
            }
            val af = e.af                                        // L232
            if (af != null) { af.Z[8] = e.ak; af.Z[9] = e.al }
            else if (e.animFinished()) w.removeEntity(e)
        }
    }
}

// ---------------------------------------------------------------------------
// ax61 — Cesare multi-tool (i.aR, i.java:11276-11529): aura follower,
// param-curve projectile (S8), knife-volley impact shell (S10), the
// grab-struggle QTE overlay (S11/12/13), and the boss-aura pair (S18/19).
// The S18 tail's `goto L171` is a decompiler-dropped label = method end.
// ---------------------------------------------------------------------------

/** `i.aR()` (i.java:11276-11529, proven) — the ax61 S-switch. */
fun NpcFsm.tickAx61(e: Entity, w: Level0World, p: Entity) {
    e.refreshBoxes()                                            // t()
    when (e.S) {
        // L4 — param-curve projectile: quadratic Bezier (j.a/j.b,
        // j.java:505/515 — `(P0·(i-t)² + 2·Pc·t(i-t) + P1·t²) >> 16`,
        // i = 65536) from Z[0..1] through ctrl Z[4..5] to screen-space
        // dest Z[8]-k.O / Z[9]-k.P over Z[7] ticks → land + i(10).
        8 -> {
            val t = (e.Z[6] * 65536) / e.Z[7]
            val ti = 65536 - t
            val tc = ti * t
            val ti2 = ti * ti
            val t2 = t * t
            e.ak = ((e.Z[0] * ti2 + 2 * e.Z[4] * tc +
                    (e.Z[8] - w.kO) * t2) shr 16) + w.kO
            e.al = ((e.Z[1] * ti2 + 2 * e.Z[5] * tc +
                    (e.Z[9] - w.kP) * t2) shr 16) + w.kP
            e.Z[6]++
            if (e.Z[6] >= e.Z[7]) {                              // L155 fall
                e.ak = e.Z[8]
                e.al = e.Z[9]
                e.setAnim(10)
                w.sfx(12)                                        // k.A(12)
            }
        }
        // L9 — die at anim end
        9 -> if (e.animFinished()) w.removeEntity(e)             // k.c(this)
        // L13 — contact-harm shell (S10)
        10 -> {
            if (e.X[0] != e.X[2] && Entity.overlapI(e.X, p.W)) {
                p.applyHit(4, 0, e, w)                           // aS.a(4,0,0,this)
            }
            if (e.animFinished()) w.removeEntity(e)
        }
        // L22 — S4/S5: T9 whiff sfx, then the shared harm arm
        4, 5 -> {
            if (e.T == 9) w.sfx(31)                              // k.A(31)
            ax61HarmArm(e, w, p)
        }
        // L25 — S2/S17: the shared harm arm directly
        2, 17 -> ax61HarmArm(e, w, p)
        // L50 — S15 catch: Y-overlap grabs the player into S375 + the
        // weapon-table drain `g.d(g.u[k.au])`; S0/S6 → r()→despawn only.
        0, 6, 15 -> {
            if (e.S == 15 && Entity.overlapI(e.Y, p.Y) && p.S != 375) {
                p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                p.setAnim(375)
                p.al = e.al
                p.gDrain(w.GU[w.weaponSlot], w)                  // g.d(g.u[k.au])
            }
            if (e.animFinished()) e.P = e.P or 128 or 32         // L57
        }
        // L61 — aura: glue to the boss (null boss → frozen in place)
        1 -> w.kAU?.let { e.ak = it.ak; e.al = it.al }
        // L65 — QTE intro: anim end → input lock + i(12)
        11 -> if (e.animFinished()) {
            e.setAnim(12)
            e.lockInput(w)                                       // k.o()
        }
        // L69-L99 — the grab-struggle QTE overlay
        12 -> {
            // L69/L71: touch-mode (k.k() = cm==1) spawns the clip9
            // marker; pad mode tracks the hand indicator over the
            // player — holding the touch point near it fills bl +9.
            if (w.mounted) {
                e.spawnAeMarker(w, 11, e.ak, e.al)               // a(11,ak,al)
            } else {
                e.spawnHand(w, p.ak, p.al - 85)                  // c(x,y)
                e.moveHand(w, p.ak, p.al - 85)                   // d(x,y)
                if (e.indicatorNearTouch(w)) e.bl += 9           // V() → +9
            }
            // L75: alternating-mash (or the touch fill) wins the QTE
            if (e.mashQte(w)) {
                // L79 — win: free the player, release the boss to S28
                if (!w.mounted) e.dropIndicator(w)               // U()
                e.P = e.P or 128 or 32
                w.removeEntity(e)                                // k.c(this)
                e.bl = 0
                e.releaseAe()                                    // G()
                p.al -= 20
                p.flingAirborne(0, w)                            // aS.a(0)
                w.kAU?.let {
                    e.unlockInput(w)                             // k.p()
                    it.setAnim(28)
                    it.P = it.P and -65
                }
            } else if (p.S == 370 || p.S == 371) {
                // L84→L92 — player still grabbed: wait for anim end,
                // then the lose path → S13 throw + k.bJ latch
                if (e.animFinished()) {
                    if (w.kBj <= 0) w.kBj = 6
                    e.releaseAe()                                // G()
                    p.setAnim(374)
                    e.setAnim(13)
                    w.sfx(12)
                }
            } else {
                // L84 — player broke the grab anims: remove + boss reset
                e.P = e.P or 128 or 32
                w.removeEntity(e)
                e.bl = 0
                e.releaseAe()
                w.kAU?.let {
                    e.unlockInput(w)
                    it.setAnim(28)
                    it.P = it.P and -65
                }
            }
        }
        // L99 — S13: grab-failed tail — the throw lands on anim end
        13 -> if (e.animFinished()) {
            p.applyHit(4, 0, e, w)                               // aS.a(4,0,0,this)
            w.removeEntity(e)
            w.kAU?.let {
                e.unlockInput(w)
                it.setAnim(28)
                it.P = it.P and -65
            }
        }
        // L105 — aura arm 1 (S19): despawn when the boss is gone, its
        // ax5 counter-claim is actively working (k.C.ab()), or the boss
        // sits in S18; otherwise glue on and promote to S18 once the
        // boss enters the active-anim set {36..41}.
        19 -> {
            val b = w.kAU
            if (b == null || (w.kC?.claimActive() == true) || b.S == 18) {
                e.P = e.P or 128
            } else {
                e.P = e.P and -129
                e.ak = b.ak; e.al = b.al
                if (b.S in 36..41) e.setAnim(18)
            }
        }
        // L131 — aura arm 2 (S18): same glue; reverts to S19 when the
        // boss leaves {36..41} — S41 exits via the dropped L171 label
        // (= method end → stay), so {36..41} all hold S18.
        18 -> {
            val b = w.kAU
            if (b == null || b.S == 18) {
                e.P = e.P or 128
            } else {
                e.P = e.P and -129
                e.ak = b.ak; e.al = b.al
                if (b.S !in 36..41) e.setAnim(19)
            }
        }
        // L154 — S3/7/14/16/default: inert
    }
}

/** `aR` L25-L46 (i.java:11330-11357, proven) — the shared contact-harm
 *  arm for S∈{2,4,5,17}: X-box overlap vs the player, skipped while the
 *  player is in {9,375,376,377}; player LEFT of the boss (`ak < aU.ak`)
 *  escapes unharmed with av=false (L39), else `a(4,0,0,this)` and —
 *  S2 only — the grab snap `i(375)` + vel0 + boss-y (L44); S17 instead
 *  lands at L39 after the hit (av=false, no snap). r() → P|=128|32. */
fun NpcFsm.ax61HarmArm(e: Entity, w: Level0World, p: Entity) {
    if (e.X[0] != e.X[2] && Entity.overlapI(e.X, p.W)) {
        if (p.S != 9 && p.S != 375 && p.S != 376 && p.S != 377) {
            if (p.ak < (w.kAU?.ak ?: 0)) {
                p.av = false                                       // L39
            } else {
                p.av = true                                        // L40
                p.applyHit(4, 0, e, w)
                if (e.S == 2) {                                    // L44
                    p.setAnim(375)
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                    p.al = w.kAU?.al ?: p.al
                } else if (e.S == 17) {                            // L43→L39
                    p.av = false
                }
            }
        }
    }
    if (e.animFinished()) e.P = e.P or 128 or 32                   // L46
}


// ====================================================================
// slice 48 — ax9 `bM()` (i.java:21069-21196) + init arm L50 (:2781-2793)
// ===========================================================================

/** `k.bn` (k.java:8447, proven): ax9 record field r8[8] → clip-index table —
 *  {47,72}; the record stores `bn[r8[8]]` in Z[2] verbatim (entity clip
 *  binding itself stays `bi[9]=47`). */
private val K_BN = intArrayOf(47, 72)

/** `i.<init>` ax9 arm (i.java:2663 `case 9` → L50 :2781, proven) + shared
 *  L392/L427 tail (`i(r8[5])` + `t()`):
 *  `aB=10; az=99; r8[5]==0 → k.aV=this; r8[5]==34 → P|=512 (Z skipped,
 *  hidden variant); else Z={0,r8[7],bn[r8[8]]}, aG=0`. */
fun NpcFsm.initAx9(e: Entity, f: List<Int>, w: LevelCellSource) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.aB = 10                                                  // L50
    e.az = 99
    if (rf(5) == 0) w.kAV = e                                  // L54
    if (rf(5) == 34) {                                         // L53→L56
        e.P = e.P or 512
        e.az = 99
    } else {                                                   // L55
        e.Z[0] = 0
        e.Z[1] = rf(7)                                         // link uid
        e.Z[2] = K_BN.getOrElse(rf(8)) { 0 }
        e.aG = 0
    }
    e.setAnim(rf(5))                                           // L392
    e.refreshBoxes()                                           // L427 t()
}

/** `i.bM()` (i.java:21069-21196, proven) — ax9 contact block:
 *  ride-linked blocks (ax51 crate / ax43 overlay) + the S-arm switch. */
fun NpcFsm.tickAx9(e: Entity, w: LevelCellSource, p: Entity) {
    // ---- preamble: link + ride (skipped for S>=35) -------------------------
    if (e.S < 35) {
        if (e.Z[1] > 0 && e.s == null) {
            val r0 = w.findByAw(e.Z[1])
            if (r0 != null) {
                e.refreshBoxes()                               // t()
                if ((r0.ax == 51 || r0.ax == 43) &&
                    Entity.overlapStrict(e.Y, r0.W)) {          // L17
                    e.s = r0
                    e.al = r0.W[1] - (e.Y[3] - e.Y[1]) + 5
                    e.ak += r0.ag shr 8
                }
            }
        }
        e.s?.takeIf { it.ax == 51 }?.let { s ->                 // L21
            e.az = s.az + 1
            e.al = s.W[1] - (e.Y[3] - e.Y[1]) + 5
        }
    }
    when (e.S) {
        // -- L28/L30/L32: contact arm — overlap → i(2) press then a() push --
        0, 1, 6, 7, 10, 11, 14, 15 -> {
            if (!Entity.overlapStrict(e.W, p.X) || p.S == 32) {
                e.pushContact(w)
            } else {
                e.setAnim(2)                                   // L30
                e.pushContact(w)                               // L32
            }
        }
        // -- L34: wind-down — anim end → i(3) -------------------------------
        2, 8, 12, 16 -> {
            e.aB = 0
            if (e.animFinished()) e.setAnim(3)
        }
        // -- L38: settle — anim end → solid|passive -------------------------
        3, 9, 13, 17, 38 -> {
            e.aB = 0
            if (e.animFinished()) { e.P = e.P or 32; e.P = e.P and -17 }
        }
        18 -> e.adChildOverlay(w, 7)                           // L42 l(7)
        // -- L45-L51: driven slide — k.ae vel + aG kick, /aI when slow-mo --
        19 -> {
            if (e.aG == 0) e.aG = 10                           // L48
            val ae = w.kAe ?: return
            e.ak += if (w.iAH) ((ae.ag shr 8) + e.aG) / w.iAI
                    else (ae.ag shr 8) + e.aG
            e.adChildOverlay(w, 1)                             // L51 l(1)
        }
        // -- L54-L57: same slide + l(4) overlay, anim end → i(19) ----------
        20 -> {
            val ae = w.kAe ?: return
            e.ak += if (w.iAH) ((ae.ag shr 8) + e.aG) / w.iAI
                    else (ae.ag shr 8) + e.aG
            e.adChildOverlay(w, 4)
            if (e.animFinished()) e.setAnim(19)
        }
        21 -> {                                                // L61
            e.ad = null
            if (e.animFinished()) e.setAnim(22)
        }
        22 -> e.ad = null                                      // L65
        else -> {}                                             // L67 (S23-37,
        //    35-37 passive; S>=35 also skips the preamble above)
    }
}

// =====================================================================
// ax15 — bu() (i.java:16693, proven): grapple/hang volume. bi[15]=25.
// S6/S8 run the body: player pushed out horizontally, captured into a
// hang (e(r1) → aS.i(108) + g.a claim), or claimed by the key arms.
// S9 → P|=16 (passive marker). S10 → P|=16 + bt() falling sweep.
// S7 → respawn-or-remove. Level-0 record aw=124 is the S9 marker.
// =====================================================================

/** init arm — `case 15` (i.java:2669) → L362 (:3553, proven):
 *  `az = r8[7]`; r8[5] ∈ {9,10} → `Z = int[8], Z[4] = -1`;
 *  r8[5] ∈ {6,8} → `Z = int[4]`; both then `Z[0]=r8[8], Z[1]=ak,
 *  Z[2]=al, Z[3]=0`; other r8[5] → no Z. Shared `i(r8[5]) + t()`. */
fun NpcFsm.initAx15(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    when (rf(5)) {
        9, 10 -> { e.Z.fill(0); e.Z[4] = -1 }
        6, 8 -> { }                                          // Z=int[4]: same
        else -> { }
    }
    if (rf(5) == 9 || rf(5) == 10 || rf(5) == 6 || rf(5) == 8) {
        e.Z[0] = rf(8); e.Z[1] = e.ak; e.Z[2] = e.al; e.Z[3] = 0
    }
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `bu()` verbatim (i.java:16693-16924, proven). S6/S8 → the L7 body;
 *  S9 → `P|=16`; S10 → `P|=16` + `bt()`; S7 → `L198`; else → `L204`
 *  (`b = false` tail on every arm except S7's remove-return). */
fun NpcFsm.tickAx15(e: Entity, w: Level0World, p: Entity) {
    when (e.S) {
        9 -> { e.P = e.P or 16; e.b = false }                          // L4
        10 -> { e.P = e.P or 16; e.sweepHostiles(w); e.b = false }     // L5
        7 -> {                                                       // L198
            if (e.animFinished()) {
                if (e.Z[3] == 1) { w.removeEntity(e); return }       // L203 early
                e.setAnim(6); e.ak = e.Z[1]; e.al = e.Z[2]
            }
            e.b = false
        }
        6, 8 -> { ax15Body(e, w, p); e.b = false }                   // L7→L204
        else -> e.b = false                                          // L204
    }
}

/** L7-L195 body of `bu()` (proven; the decompiler's L103/L99 goto
 *  loop is an artifact — side pushout applies each matching side
 *  once, `high-confidence` on that collapse). */
private fun ax15Body(e: Entity, w: Level0World, p: Entity) {
    // L9: claimable ax43 overlay overlapping → collapse to S7 + mark.
    if (e.S == 6) {
        val g = p.ga
        if (g != null && g.ax == 43 && g.S == 1 &&
            Entity.overlapStrict(g.W, e.W)) {
            e.setAnim(7); e.Z[3] = 1
        }
    }
    // L17
    val r1 = e.S
    val r9 = r1 == 10 && e.Z[0] != -1                              // L20-L23
    e.collideSides(w, true)                                        // a(true)
    if (p.S == 50) { p.ac = null; e.ag = 0; return }               // L27-ish
    if (e.P and 128 != 0) { w.removeEntity(e); return }            // L29 cull
    e.ag = 0
    if (p.ga !== e && Math.abs(p.ak - e.ak) < 60) {
        if (Math.abs(p.al - e.al) < 30) e.pushContact(w)           // L34 a()
        // L37 hang-below path — in front + below midY + |Δal|<20
        if (p.inFrontOf(e) &&
            p.al > (e.W[1] + e.W[3]) shr 1 &&
            Math.abs(p.al - e.al) < 20) {
            if (p.S == 77) e.hangOnEdge(w, r1)
            else {
                // L46 — locomotion-state capture/pushout
                if (p.gC()) {
                    if (Entity.overlapStrict(p.W, e.W)) {
                        // L51-L57: horizontal pushout + hang attempt
                        p.ag = 0; p.ah = 0
                        if (p.ak - e.ak < 0) {
                            p.ak = e.ak - ((e.W[2] - e.W[0]) shr 1) -
                                    (p.W[2] - p.ak)
                        } else if (p.ak - e.ak > 0) {
                            p.ak = e.ak + ((e.W[2] - e.W[0]) shr 1) +
                                    (p.ak - p.W[0])
                        }
                        e.hangOnEdge(w, r1)                        // L57
                    } else if (p.ac === e) {
                        ax15Capture(e, w, p)                       // L50→L59
                    }
                }
            }
        } else {
            ax15Capture(e, w, p)                                   // L59
        }
    }
    // L119 — release check when the block claimed the player
    if (p.ga === e && p.S !in 107..109 && p.S != 209 &&
        !Entity.overlapStrict(p.W, e.W)) {
        p.ga = null
    }
    // L130 — unsupported + unridden + no pending link → fall
    if (!e.supportedByGround(w) && e.s == null && !r9) {
        e.ag = 0; e.ah = 4096
    }
    // L137 — grounded → settle flags
    if (e.supportedByGround(w)) { e.bd = true; e.ai = 0; e.ah = 0 }
    // L141 — neighbor sweep over k.bd (= w.npcs)
    for (n in w.npcs) {
        // L156 ax44 door overlapping an S6 block → i(7) collapse anim
        if (n.ax == 44 && n.S == 0 && r1 == 6 &&
            Entity.overlapStrict(n.X, e.W)) {
            e.setAnim(7)
        }
        // ax66 platform ride — mount on overlap, unmount on leave,
        // carry by its velocity (L167/L172/L195)
        if (n.ax == 66) {
            if (Entity.pointInBox(e.ak, e.al, n.W) && e.s !== n) {
                e.s = n; e.al = n.W[1] + 1; e.ag = 0; e.ah = 0
            }
            if (e.s === n && !Entity.overlapStrict(e.W, n.W)) e.s = null
            if (e.s === n) { e.ak += n.ag shr 8; e.al += n.ah shr 8 }
        }
        // ax11 soldier shove — i(2) + lateral pushout (L185/L190)
        if (n.ax == 11 && Entity.overlapStrict(e.W, n.W) &&
            (n.S == 3 || n.S == 4 || n.S == 23 || n.S == 22)) {
            n.setAnim(2); n.aA = 0
            n.ak = if (n.ak > e.ak) {
                e.W[2] + (n.W[2] - n.W[0]) / 2 + 2
            } else {
                e.W[0] - (n.W[2] - n.W[0]) / 2 - 2
            }
            n.ag = 0; n.ah = 0
        }
    }
}

/** L59-L119 capture arm of `bu()` (proven except the L94/L103/L99
 *  pushout-fling — `high-confidence`, decompiler label loop collapsed
 *  to the symmetric two-side form). */
private fun ax15Capture(e: Entity, w: Level0World, p: Entity) {
    // L59 gates
    if (p.ga != null) return                                       // →L130
    if (p.al >= (e.W[1] + e.W[3]) shr 1) return
    if (!Entity.pointInBox(p.ak, p.al, e.W)) return
    if (p.S == 20 && p.T <= 1) return                              // mid-rise skip
    // L69 — free state (or S34) → capture attempt
    if (p.gB() || p.S == 34) {
        // L73 — inside the span → claim
        if (p.ak <= e.W[0] || p.ak >= e.W[2]) {
            // L94/L99 — outside span: side pushout + a(2560) fling
            if (p.ak <= e.ak && p.ag > 0) {
                p.ak = e.ak - (p.W[2] - p.W[0]) / 2 - (e.W[2] - e.W[0]) / 2
                p.ai = 0; p.ag = 0
                p.flingAirborne(2560, w)
            } else if (p.ak > e.ak && p.ag < 0) {
                p.ak = e.ak + (p.W[2] - p.W[0]) / 2 + (e.W[2] - e.W[0]) / 2
                p.ai = 0; p.ag = 0
                p.flingAirborne(2560, w)
            }
            return
        }
        p.ga = e                                                   // g.a = this
        p.setAnim(5); p.al = e.W[1] + 1
        p.ag = 0; p.ah = 0; p.aj = 0
        if (w.padHeld(16388) || w.padDown(16388) ||
            (p.av && (w.padHeld(2) || w.padDown(2))) ||
            (!p.av && (w.padHeld(8) || w.padDown(8)))) {
            p.setAnim(22)                                          // L92 vault
        }
    } else if (p.S == 62) {
        p.flingAirborne(2560, w)                                   // L108 arm a
    } else if (p.S == 63) {
        // wall-climb nudge: hop ±10 through the block, flip, reset
        p.ak += if (p.av) -10 else 10
        p.av = !p.av
        p.setAnim(0)
    }
}

// =====================================================================
// ax46 — aZ() (i.java:13568-13726, proven): spring/trap prop. bi[46]=29
// but the anim clip rebinds per-record: `aa = k.r(k.bl[r8[10]])` with
// k.bl = {29, 0} — bl[1]=0 = the mega clip (clip0) whose 300-range
// anims host the armed-trap states 327/328/329. Head pins palette Z[5].
// =====================================================================

/** init arm — `case 46` (i.java:2700) → L206 (:3245, proven):
 *  `Z=int[8]`; `Z[4]=r8[5]` (armed marker); `Z[0]=r8[8]<<8`,
 *  `Z[1]=r8[9]<<8` (throw velocities); `Z[5]=r8[11]` (palette pin);
 *  `Z[6]=k.bl[r8[10]]`; `Z[7]=r8[12]`; `Z[3]=30`. The L15 head binds
 *  the anim clip `aa = k.r(k.bl[r8[10]])`; shared `i(r8[5])+t()`. */
fun NpcFsm.initAx46(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    val bl = intArrayOf(29, 0)                        // k.bl (k.java:8445)
    val bi10 = rf(10)
    e.clip = w.clips[if (bi10 in bl.indices) bl[bi10] else 0]
    e.Z[4] = rf(5); e.Z[0] = rf(8) shl 8; e.Z[1] = rf(9) shl 8
    e.Z[5] = rf(11); e.Z[6] = if (bi10 in bl.indices) bl[bi10] else 0
    e.Z[7] = rf(12); e.Z[3] = 30
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `aZ()` verbatim (proven): head `aa.l(Z[5])` = palette pin; then the
 *  S dispatch — spring pads (0/11/1/12), touch traps (3/4/327), the
 *  fire cycle (5/6/328), settle (7/8), dead (2), pusher (10), and the
 *  re-arm (329). */
fun NpcFsm.tickAx46(e: Entity, w: Level0World, p: Entity) {
    e.palette = e.Z[5]                                          // aa.l(Z[5])
    when (e.S) {
        0, 11 -> ax46Spring(e, w, p)                            // L79
        1, 12 -> {                                              // L71
            if (e.animFinished()) e.setAnim(if (e.S == 1) 0 else 11)
            ax46Spring(e, w, p)
        }
        2 -> {}                                                 // L7 dead
        3, 4, 327 -> ax46Touch(e, w, p)                         // L9
        5, 6, 328 -> ax46Cycle(e, w, p)                         // L33
        7, 8 -> { if (e.animFinished()) e.setAnim(2) }          // L63/L107
        10 -> ax46Pusher(e, p)                                  // L92
        329 -> { if (e.animFinished()) e.setAnim(327) }         // L67
        else -> {}                                              // L103
    }
}

/** L79 spring arm (proven): player-W overlap + falling (`ah>=0`) +
 *  quarter-point above the pad top → `aS.a(11,0,0,this)` (op11 intake:
 *  pin + Z-launch) then `i(1)`/`i(12)` sprung anim. */
private fun ax46Spring(e: Entity, w: Level0World, p: Entity) {
    if (!Entity.overlapStrict(p.W, e.W)) return                 // L80
    if (p.ah < 0) return                                        // L82 still rising
    if ((((p.W[1] + (p.W[1] + p.W[3])) shr 1) shr 1) >= e.W[1]) return
    p.applyHit(11, 0, e, w)                                     // L83
    e.setAnim(if (e.S == 11) 12 else 1)                         // L88/L102
}

/** L9 touch arm (proven): X-box (attack rect) overlap → armed records
 *  (`Z[4]==S`) pin the player via `a(24,…)` and advance the fire cycle;
 *  unarmed contacts throw the player off (`aS.i(165)` + `ag=∓2048`,
 *  `ah=-5120`, `i(7)`). */
private fun ax46Touch(e: Entity, w: Level0World, p: Entity) {
    if (!Entity.overlapStrict(p.W, e.X)) return                 // L10
    if (e.Z[4] == e.S) {                                        // L13 armed
        if (e.S == 327) {
            p.applyHit(24, 330, e, w); e.setAnim(328)
        } else {
            p.applyHit(24, 110, e, w)
            e.setAnim(if (e.S == 3) 6 else 5)
        }
        return
    }
    // L11 touch throw
    e.P = e.P and -65
    e.releaseAe()                                               // G()
    p.setAnim(165)
    p.av = e.av                                                 // L25/L26 face = e.av
    p.ag = if (p.av) -2048 else 2048
    p.ah = -5120
    e.setAnim(7)
}

/** L33 fire-cycle arm (proven): X overlap → S328 pins again; on anim
 *  end S5 resets to i(3)+face-invert, S328 throws off to i(329),
 *  S6 winds down to i(4). `Z[2]=Z[3]` rearms the 30-tick counter. */
private fun ax46Cycle(e: Entity, w: Level0World, p: Entity) {
    if (!Entity.overlapStrict(p.W, e.X)) return                 // L34
    if (e.S == 328) p.applyHit(24, 330, e, w)                   // L37
    if (!e.animFinished()) return                               // L40
    when (e.S) {
        5 -> { e.setAnim(3); p.av = !e.av }                     // L41/L59 flip
        328 -> {                                                // L46 throw-off
            e.setAnim(329); e.P = e.P and -65
            p.setAnim(165)
            p.av = e.av                                         // L50/L51
            p.ag = if (p.av) -2048 else 2048
            p.ah = -5120
        }
        else -> { e.setAnim(4); p.av = e.av }                   // L56/L59
    }
    e.Z[2] = e.Z[3]                                             // L60
}

/** L92 pusher arm (proven): W overlap → `ag=Z[0], ah=Z[1]` +
 *  `av=!e.av` + `i(242)` — the directional air-jet push. */
private fun ax46Pusher(e: Entity, p: Entity) {
    if (!Entity.overlapStrict(e.W, p.W)) return                 // L93
    p.ag = e.Z[0]; p.ah = e.Z[1]
    p.av = !e.av                                                // L98/L99
    p.setAnim(242)
}

// =====================================================================
// ax7 — ejection slot (i.java tick-dispatch case 7 → L88/L90/L94,
// :5110-5149, proven): the swallow-and-eject warp volume. Records carry
// no Z — init is just `az = r8[7]` + shared `i(r8[5])+t()`.
// =====================================================================

/** init arm — `case 7` (i.java:2661) → L112 (:3001): `az = r8[7]` then
 *  the shared `i(r8[5])` + `t()` tail. */
fun NpcFsm.initAx7(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** ax7 tick (proven):
 *  - S0 (L90): `W∩playerW && !aS.f()` → `i(1)` + `aS.i(313)` swallow:
 *    `P|=64` slot-hold, center-snap, `av=e.av`, all velocities zeroed.
 *  - S1 (L94): keep `P|=64` + `t()` + re-snap each tick + `aS.T=this.T`
 *    frame-sync; on `r()` → `i(0)` + `aS.a(0)` (resume) + throw
 *    `ag=∓2048` by the slot's facing. */
fun NpcFsm.tickAx7(e: Entity, w: Level0World, p: Entity) {
    when (e.S) {
        0 -> {
            if (!Entity.overlapStrict(p.W, e.W)) return
            if (p.isHolding()) return
            e.setAnim(1)
            p.setAnim(313)
            p.P = p.P or 64
            p.ak = (e.W[0] + e.W[2]) shr 1
            p.al = (e.W[1] + e.W[3]) shr 1
            p.av = e.av
            p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
        }
        1 -> {
            p.P = p.P or 64
            e.refreshBoxes()
            p.ak = (e.W[0] + e.W[2]) shr 1
            p.al = (e.W[1] + e.W[3]) shr 1
            p.T = e.T
            if (!e.animFinished()) return
            e.setAnim(0)
            p.flingAirborne(0, w)
            p.ag = if (e.av) -2048 else 2048
        }
    }
}


// ---------------------------------------------------------------------------
// ax6 `an()` (i.java:7220-7248) — overlap-trigger marker (one-shot flags).
// ax19 `aO()` (i.java:10261-10299) — meter-restore pickup + fx burst.
// ---------------------------------------------------------------------------

/** Shared generic record init (L111 map + `i(r8[5])` + `t()`). */
fun NpcFsm.initAx6(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.aE = rf(4); e.aF = rf(11); e.oId = rf(12)
    e.pv = rf(13); e.aG = rf(14); e.ay = rf(15)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `an()`: S3/S5 armed — player W-overlap fires `i(7)`/`i(6)`;
 *  S6/S7 wind down `r()` → `k.c(this)` removal. */
fun NpcFsm.tickAx6(e: Entity, w: LevelCellSource, p: Entity) {
    when (e.S) {
        3 -> if (Entity.overlapStrict(p.W, e.W)) e.setAnim(7)    // L13
        5 -> if (Entity.overlapStrict(p.W, e.W)) e.setAnim(6)    // L5
        6, 7 -> if (e.animFinished()) w.removeEntity(e)            // L9
        else -> {}
    }
}

fun NpcFsm.initAx19(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.aE = rf(4); e.aF = rf(11); e.oId = rf(12)
    e.pv = rf(13); e.aG = rf(14); e.ay = rf(15)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `aO()`: `b=true` each tick. S17/19 idle — when the player is
 *  interact-eligible (`g.c(aS.S)` or `bh[aj]==3`) and overlaps:
 *  `i(18)`, `k.A(17)` sfx, 5x clip-54 burst sparks to screen (5,5).
 *  S18/20 consume — `r()` → `x[1]=k.ax` meter restore (only while the
 *  player is alive) then `k.c(this)`. */
fun NpcFsm.tickAx19(e: Entity, w: LevelCellSource, p: Entity) {
    e.b = true
    when (e.S) {
        17, 19 -> {                                                 // L5
            if (INTERACTABLE_STATES.contains(p.S) || w.missionBh() == 3) {
                if (Entity.overlapStrict(p.W, e.W)) {
                    e.setAnim(18)
                    w.sfx(17)                                       // k.A(17)
                    repeat(5) {
                        e.spawnFlyBurst(w, e.ak, e.al,
                                        5 + w.kO, 5 + w.kP,
                                        74, 54, 5, 300)
                    }
                }
            }
        }
        18, 20 -> {                                                 // L16
            if (e.animFinished()) {
                if (!w.playerDead()) p.x1 = w.kAx                   // g.e(k.ax)
                w.removeEntity(e)
            }
        }
        else -> {}
    }
}

// ---------------------------------------------------------------------------
// ax42 `bz()` (i.java:17428-17523) — fuse/timer zone + `k.F` claim slot.
// ---------------------------------------------------------------------------

/** Init arm L382 (i.java:3589) + L419 W-fill (i.java:3698): `P|=16|512`,
 *  `Z[3] = {kind r8[4], uid r8[11], secs r8[12]}` and the zone rect
 *  `W = [ak+r8[7], al+r8[8], +r8[9], +r8[10]]`. `bi[42]=-1` (no clip) and
 *  no `i()` call reaches this arm in the ctor — `S` stays -1, so `bz()`'s
 *  `S==0` gate keeps the fuse dormant until a claim-script `i(0)` arms it
 *  (faithful; same shape as ax6's degenerate-W dormancy). */
fun NpcFsm.initAx42(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.P = e.P or 16 or 512
    e.S = -1                               // ctor S=-1; no i() reaches ax42
    e.Z.fill(0)                            // Z = new int[3] (val array)
    e.Z[0] = rf(4); e.Z[1] = rf(11); e.Z[2] = rf(12)
    e.W[0] = e.ak + rf(7); e.W[1] = e.al + rf(8)
    e.W[2] = e.W[0] + rf(9); e.W[3] = e.W[1] + rf(10)
}

/** `bz()`: every tick `P|=16|512` and `k.F = this` (the fuse registers
 *  itself as the claim-locked entity), then the `k.aJ` phase machine —
 *  all of it gated on `S == 0` (`ifne 422`, i.javap ~:58379).
 *
 *  aJ==0 (L5): `Z[0]` kind 0/1 lazily binds `s = k.q(Z[1])` and fires
 *  `aJ=1, aK=-40, aL=Z[2], s=null, A(9)` when `s.P()` expired (Z0==0) or
 *  `s.P&32==0` (Z0==1); kind 2 fires the same countdown when `Z[1]>0`
 *  (no `s` clear / sfx on that arm).
 *  aJ==2 (L36): `aM += 50` per tick. Kind 0/1 — `aL*1000 <= aM` expires
 *  to `bw=-1, bx = aj==7?56:58, l(13)` + `aL=-1,aM=0`; still ticking and
 *  player∩W collects → `k.c(this)`, aJ=3. Kind 2 — expiry binds +
 *  step-resets `h(s(Z[1]))`/`k(s(Z[1]))`, aJ=3.
 *  aJ==3 (L60): kind 2 only — `ab()` → `aa()` while the bound script is
 *  active, else `k.c(this)`, `aL=-1, aM=0, bw=2`. */
fun NpcFsm.tickAx42(e: Entity, w: LevelCellSource, p: Entity) {
    e.P = e.P or 16 or 512
    w.kF = e
    if (e.S != 0) return
    when (w.kAJ) {
        0 -> when (e.Z[0]) {
            2 -> if (e.Z[1] > 0) {                              // L32
                w.kAJ = 1; w.kAK = -40; w.kAL = e.Z[2]
            }
            0, 1 -> {                                           // L11
                if (e.s == null && e.Z[1] != -1) {
                    val r0 = w.findByAw(e.Z[1])
                    if (r0 != null) e.s = r0
                }
                val s = e.s ?: return
                // L20/L24/L26 — fire via expired (Z0==0) or !(s.P&32) (Z0==1)
                val fire = (s.deadRelease() && e.Z[0] == 0) ||
                        ((s.P and 32) == 0 && e.Z[0] == 1)
                if (fire) {                                     // L27
                    w.kAJ = 1; w.kAK = -40; w.kAL = e.Z[2]
                    e.s = null
                    w.sfx(9)                                    // k.A(9)
                }
            }
            else -> {}
        }
        2 -> {
            w.kAM += 50                                         // L36
            when (e.Z[0]) {
                2 -> if (w.kAL * 1000 <= w.kAM) {               // L56 expired
                    e.bindScript(w.kSIndex(e.Z[1]), w)               // i.h(int)
                    e.scriptKeyStep(w.kSIndex(e.Z[1]), w)            // i.k(int)
                    w.kAJ = 3
                }
                0, 1 -> if (w.kAL * 1000 <= w.kAM) {            // L42 expired
                    w.kBw = -1
                    w.kBx = if (w.kAj == 7) 56 else 58
                    w.screenL(13)                               // k.l(13)
                    w.kAL = -1; w.kAM = 0
                } else if (Entity.overlapStrict(p.W, e.W)) {    // L44 collect
                    w.removeEntity(e)                           // k.c(this)
                    w.kAJ = 3
                }
                else -> {}
            }
        }
        3 -> if (e.Z[0] == 2) {                                 // L62
            if (e.claimActive()) e.runClaimScript(w)            // ab()→aa()
            else {
                w.removeEntity(e)                               // k.c(this)
                w.kAL = -1; w.kAM = 0
                w.kBw = 2
            }
        }
    }
}

// ---------------------------------------------------------------------------
// ax13 `aW()` (i.java:13182-13367) — swinging rope/vine entity.
// `bO`/`bP` = pendulum velocity/angle (8.8); `bN` segments of `Z[1]` max;
// `bM` = bound entity; aG variants {1 boost×4, 2 ab-marker, 4 door-spawner}.
// ---------------------------------------------------------------------------

/** Init arm: the shared `L111` record map + `i(r8[5])` + `t()` tail. */
fun NpcFsm.initAx13(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.aE = rf(4); e.aF = rf(11); e.oId = rf(12)                    // L111
    e.pv = rf(13); e.aG = rf(14); e.ay = rf(15)
    e.setAnim(rf(5))                                              // L395
    e.refreshBoxes()                                              // t()
}

fun NpcFsm.tickAx13(e: Entity, w: LevelCellSource, p: Entity) {
    // ---- pendulum integrator (L6) — runs while bO!=0 or bP!=0 ----
    var integrated = false
    if (e.bO != 0 || e.bP != 0) {                                   // L5→L6
        val r0 = e.bO
        e.bP += e.bO shl 1
        e.bO -= Trig.sin(Trig.N - (e.bP shr 8)) shl 1               // j.b(n-θ)
        integrated = true
        if (e.aA == 1) {                                            // L9
            if (e.bM === p && e.aG == 1 && r0 * e.bO < 0) {
                p.av = e.bP < 0                                     // L17/L18
                p.releaseRope(w)                                    // aS.j()
            }
        }
    }
    // L19: swing-side latch — sign flip of bP damps bO by 1/8
    if (integrated) {
        val r02 = e.j
        e.j = if (e.bP > 0) 1 else -1                               // L22/L23
        if (r02 != e.j) e.bO -= e.bO shr 3
    }
    // ---- L27: bound-side (aA==1) — drive input + park the rider ----
    if (e.aA == 1) {
        if (e.bM === p) p.ropeInput(w)                              // aS.k()
        val b = e.bM
        if (b != null) {
            e.ropeArcPlace(b)                                       // l(bM)
            if (e.aG == 2) {
                // L40-41: (re)bind the player's ab marker to the rope tip
                if (p.ab == null) {
                    p.ab = p.spawnChildFx(w, 14, 9, 11, 302)      // a(14,9,11,302)
                }
                p.ab?.let { m ->
                    m.al = (p.W[1] + p.W[3]) / 2
                    m.ak = p.ak
                    m.av = false
                }
            }
        }
    }
    // ---- L43: grab-scan — player overlap on the swing-arc box ----
    if (p.bM !== e && (p.aA and 64) == 0 && e.aA == 0 &&
        Entity.GRABBABLE_STATES.contains(p.S) && p.O < e.O) {
        val r05 = 3072 * e.Z[1]
        var r04 = -1
        if (p.N <= e.N + r05 && p.N >= e.N - r05 &&
            p.O <= e.O + r05 + 16384) {                             // L53-60
            p.refreshBoxes()                                        // r06 = r12.t()
            val r06 = p.W
            if (e.av) { r06[0] = (p.N shr 8) - 24; r06[2] = p.N shr 8 }
            else      { r06[0] = p.N shr 8; r06[2] = (p.N shr 8) + 24 }
            val r07 = 3072 * e.bN
            val r122 = (3072 * (e.bN - 4)).coerceAtLeast(1)         // L66
            val r09 = e.bP shr 8
            val r010 = (r07 * Trig.sin(Trig.N - r09)) shr 8
            val r011 = (r07 * Trig.sin(r09)) shr 8
            val r012 = (r122 * Trig.sin(Trig.N - r09)) shr 8
            val r013 = (r122 * Trig.sin(r09)) shr 8
            if (r010 > 0) {
                e.W[0] = ((e.N + r012) shr 8) - 4; e.W[2] = ((e.N + r010) shr 8) + 4
            } else {
                e.W[0] = ((e.N + r010) shr 8) - 4; e.W[2] = ((e.N + r012) shr 8) + 4
            }
            e.W[1] = (e.O + r013) shr 8
            e.W[3] = ((e.O + r011) + r05 - r07) shr 8               // L73
            if (Entity.overlapStrict(r06, e.W)) {                   // a(r06,W)
                // L76-88: pick the grab segment r04 on the arc
                var r8 = (p.O - e.O) / ((3072 * Trig.sin(r09)) shr 8)
                if (r8 < 0) r8 = 0
                if (r8 > e.Z[1] - 2) r8 = e.Z[1] - 2
                if (r8 >= 0) r04 = if (r8 == 0) e.bN - 4 else r8 and 65534
            }
        }
        if (r04 >= 0) {                                             // L89 latch
            p.bindScript(1, w)                                      // aS.h(1)
            w.clearLatches()                                        // k.v()
            e.bN = r04; e.ropeGrabSeg = r04
            e.aA = 1; e.bM = p; p.bM = e; p.az = 101
            if (p.av) { e.bP -= 256; e.bO -= 512 }                  // L94
            else      { e.bP += 256; e.bO += 512 }
            if (e.aG == 1) { e.bP = e.bP shl 2; e.bO = e.bO shl 2 } // L96
            else if (e.aG == 4) { e.bO = 0; e.bP = 0 }              // L99
            e.ropeArcPlace(p)                                       // l(k.aS)
            p.aA = p.aA or 64
            p.setAnim(326)
        }
    }
    // ---- L103: aG==4 door-linked segment spawner ----
    if (e.aG == 4) {
        val r015 = if (e.Z[6] != -1) w.findByAw(e.Z[6]) else null
        if (e.Z[6] == -1 || (r015 != null && r015.isBf())) {
            if (e.bN < e.Z[1]) e.bN++                               // L111
        }
    }
    // ---- L114-126: angle clamp + zero-snap ----
    if (e.bP < -20480) { e.bP = -20480; e.bO = 0 }
    if (e.bP > 20480) { e.bP = 20480; e.bO = 0 }
    if ((e.bP and -128) == 0 && (e.bO and -128) == 0) {
        e.bO = 0; e.bP = 0                                          // L122
    }
}


// =====================================================================
// ax72 + ax78 + ax79 — the counterweight pair + palette prop
// (i.java init L384/L153/L386; ax78 tick `bA()` :17525, proven).
// =====================================================================

/** ax72 init (L384 :3605, proven): `az=r8[7]` + `Z=int[5]` —
 *  `Z[0]=r8[8]`, `Z[1]=r8[9]<<8` (fixed-point drop offset),
 *  `Z[2]=r8[10]`, `Z[3]=r8[11]`, `Z[4]=r8[12]` (linked ax78's `aw`).
 *  Tick dispatch `case 72 → L897` — static, never ticks. */
fun NpcFsm.initAx72(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    e.Z[0] = rf(8); e.Z[1] = rf(9) shl 8; e.Z[2] = rf(10)
    e.Z[3] = rf(11); e.Z[4] = rf(12)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** ax78 init (L153 :3114, proven): `az=r8[7]` + shared tail. */
fun NpcFsm.initAx78(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** ax79 init (L386 :3630, proven): `Z=int[2]` — `Z[0]=r8[7]` (palette),
 *  `Z[1]=r8[8]`; `aa.l(Z[0])` pins the palette when a clip is bound.
 *  No tick arm — static. */
fun NpcFsm.initAx79(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.Z[0] = rf(7); e.Z[1] = rf(8)
    e.palette = e.Z[0]
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** ax78 `bA()` (i.java:17525, proven): the suspended counterweight.
 *  `r0` = support probe `h(ak/20,(al+10)/20)` — cell≥5 counts.
 *  - S0: unsupported → `al+=2` settle; supported → `i(1)` armed.
 *  - S1: supported → hold; unsupported → `aj=1536` gravity → `i(2)`.
 *  - S2 falling: `bt()` crush-sweep; `T==4&&U==0` → `k.A(14)` whoosh;
 *    supported → `a(true)` land + `i(3)` + zeroed vel; else sub-step
 *    settle loop (halve `ah` until `h` clears → `i(3)`).
 *  - S3: crushed/settled — `bt()` sweep + same land/drop logic. */
fun NpcFsm.tickAx78(e: Entity, w: Level0World, p: Entity) {
    fun sup(cx: Int, cy: Int): Boolean {                   // h(cx,cy) v>=5
        val v = w.collisionCell(cx, cy)
        return if (v < 12) v >= 5 else true
    }
    val r0 = sup(e.ak / 20, (e.al + 10) / 20)              // L5 head
    when (e.S) {
        0 -> { if (r0) e.setAnim(1) else e.al += 2 }         // L5/L8
        1 -> {                                             // L11
            if (r0) return
            e.aj = 1536; e.setAnim(2)
        }
        2 -> {                                             // L14
            e.sweepHostiles(w)
            if (e.T == 4 && e.U == 0) w.sfx(14)
            if (r0) {
                e.collideSides(w, true); e.setAnim(3)
                e.aj = 0; e.ah = 0; return
            }
            e.aj = 1536
            var r7 = sup(e.ak / 20, (e.al + ((e.ah + e.aj) shr 8)) / 20)
            while (r7) {                                   // L25 sub-settle
                e.aj = 0; e.ah = e.ah / 2
                r7 = sup(e.ak / 20, (e.al + (e.ah shr 8)) / 20)
                e.setAnim(3)
            }
        }
        3 -> {                                             // L28
            e.sweepHostiles(w)
            if (r0) {
                e.collideSides(w, true); e.aj = 0; e.ah = 0; return
            }
            e.aj = 1536
        }
        else -> {}                                         // L35
    }
}


/** ax80 init (L389 :2734→ :3636, proven): `az = r8[7]` + shared tail —
 *  identical shape to ax7/ax78. Tick hits `default → L897` (:4986-4987),
 *  i.e. static, never ticks. Records exist only in pack-12 (`S=4`,
 *  `az=300`). */
fun NpcFsm.initAx80(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

// ============================================================ ax54 / ax30
// = ax() waypoint runner (i.java:8170) — the flying-level enemy (packs 7,10)
// + ax24 = projectile type whose S==0 record seeds the shared k.aX[50] pool
// (i.java:2842 arm under `case 24 → L75`). All proven.

/** `i(short[])` L214 arm (i.java:3281, proven) — shared ax54/ax30 init. */
fun NpcFsm.initAx54(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = 100
    e.aB = 300
    e.Z.fill(0)
    e.Z[15] = rf(2)                                   // spawn x
    e.Z[16] = rf(3)                                   // spawn y
    e.Z[0] = rf(4)                                    // mode (0/1/2/3)
    for (i in 1..14) e.Z[i] = rf(i + 6)               // Z[1..14] = r8[7..20]
    if (e.Z[9] == 0) e.Z[10] = 1                      // L218
    else if (e.Z[9] == 1 && e.Z[8] == 0) e.Z[8] = 3   // L218→L222
    else if (e.Z[8] == 1 && e.Z[9] == 1) e.Z[9] = 0   // L226
    e.aC = e.Z[6]; e.aD = e.Z[11]; e.aF = e.Z[12]
    e.nl = e.Z[14]
    if (e.Z[0] == 0) return                            // L392 — no child
    e.az = if (e.ax == 30) 99 else -1                  // L233/L234
    // r8[0] = 68; ad = new i(r8): ax68 child spawned from the same record
    // fields, bound `af = this` (i.java:3296-3300, proven)
    val cf = f.toMutableList(); cf[0] = 68
    val child = Entity(68, w.clips[26])                // bi[68]=26
    child.aw = e.aw
    child.setPositionPx(cf[2], cf[3])
    child.P = cf[6]; child.av = (cf[6] and 1) != 0
    for (i in child.Z.indices) if (7 + i < cf.size) child.Z[i] = cf[7 + i]
    child.refreshBoxes()
    child.av = false
    child.af = e
    e.ad = child
    w.queueInsert(child)
}

/** `i.aw()` (i.java:8127, proven) — resolve up to 4 waypoint uids in
 *  Z[1..4] into entity-shifted pool copies; `C` = resolved count.
 *  Called once post-load by `R()` (i.java:8131) for ax54/ax30. */
fun NpcFsm.resolveRunnerWaypoints(e: Entity, w: Level0World) {
    for (i in 0..3) {
        val src = w.waypointPool.find(e.Z[1 + i]) ?: continue
        val copy = w.waypointPool.copyShifted(src, e)
        e.Z[1 + i] = copy.k
        e.runnerC++
    }
}

/** `i.Q()` (i.java:7771, proven): face the player — `av = ak > aS.ak`. */
private fun NpcFsm.runnerFacePlayer(e: Entity, p: Entity) { e.av = e.ak > p.ak }

/** `i.d(int)` (i.java:7782, proven): player within 400/400 && `v()`. */
private fun NpcFsm.runnerNearPlayer(e: Entity, w: Level0World): Boolean {
    if (kotlin.math.abs(w.player.ak - e.ak) > 400) return false
    if (kotlin.math.abs(w.player.al - e.al) > 400) return false
    return e.inPlayV(w)
}

/** `i.j(int,int)` (i.java:8067, proven): walk-anim picker toward (x,y). */
private fun NpcFsm.runnerWalkAnim(e: Entity, x: Int, y: Int) {
    if (y < e.al) {                                  // target above
        e.setAnim(1)
        if (kotlin.math.abs(x - e.ak) > 20) e.setAnim(0)
    } else {
        e.setAnim(3)
        if (kotlin.math.abs(x - e.ak) > 20) e.setAnim(4)
        if (kotlin.math.abs(y - e.al) > 20) e.setAnim(2)
        if (e.ax == 30) e.setAnim(4)                 // L14 ax30 remap
    }
}

/** `i.k(int,int)` (i.java:8088, proven): arrive-anim picker toward (x,y). */
private fun NpcFsm.runnerArriveAnim(e: Entity, x: Int, y: Int) {
    if (y < e.al) {
        e.setAnim(6)
        if (kotlin.math.abs(x - e.ak) > 20) e.setAnim(5)
    } else {
        e.setAnim(8)
        if (kotlin.math.abs(x - e.ak) > 20) e.setAnim(9)
        if (kotlin.math.abs(y - e.al) > 20) e.setAnim(7)
        if (e.ax == 30) e.setAnim(9)
    }
}

/** `i.i(int,int)` (i.java:7795, proven): anim-set selector —
 *  0→i(r6), 1→i(r6+23), 2→i(22), 3→i(r6). */
private fun NpcFsm.animVariant(e: Entity, set: Int, base: Int) {
    when (set) { 1 -> e.setAnim(base + 23); 2 -> e.setAnim(22); else -> e.setAnim(base) }
}

/** `i.b(int,int,int,int)` (i.java:8026, proven): dominant-direction index
 *  {1,2,3} from (x1,y1)→(x2,y2) — 5120px dominance threshold. */
private fun NpcFsm.dirVariant(x1: Int, y1: Int, x2: Int, y2: Int): Int {
    val dx = x2 - x1; val dy = y2 - y1
    if (dx == 0) return 2
    if (kotlin.math.abs(dx) <= 5120) return 2
    val slope = (dy * 100) / dx
    if (slope == 0) return 2
    if (kotlin.math.abs(dy) <= 5120) return 2
    return if (slope < 0) (if (dx > 0) 1 else 3)
           else (if (dx > 0) 3 else 1)
}

/** `i.a(int, boolean)` (i.java:7826, proven): projectile volley — arms
 *  `count` free `k.aX` slots (av() = first with P&128 clear). Launch point
 *  `am/an` = X-center<<7 for ax54/30/56 else W-center<<7; `copyAim` keeps
 *  e.ao/ap, else aim = wpF point or the player W-center<<7. Angular fan
 *  spreads count>1 volleys ±r02 around atan2 aim; speed 2048 (ax54 Z[9]==2
 *  → 1280; ax30 Z[9]==2&&count==3&&first → 2560). Velocity =
 *  (dir*speed)/dist + gravity. Facing av/P|1, subpixel seed N/O, anim
 *  i(Z[8],dirVar) on ax54 / i(Z[3],dirVar) on ax30, aG = Z[9] (both),
 *  af = owner, aC = n. First shot of a volley: ax30 → sfx27 else sfx16. */
private fun NpcFsm.runnerBurst(e: Entity, count: Int, copyAim: Boolean,
                               w: Level0World) {
    val pool = w.projectilePool ?: return             // unseeded → inert (inferred)
    val r0 = if (count > 1) count - 1 else -1         // L6 arm (inferred —
    // jadx dropped the assignment; -1 → r02=0 = no fan for single shots)
    val r02 = r0 * Trig.M / 360
    for (r10 in 0 until count) {
        val slot = w.projectileAlloc()               // av()
        if (slot == -1) return
        val r04 = pool[slot]!!
        // L32/L35: launch point — X-center for ax54/30/56 else W-center
        if (e.ax == 54 || e.ax == 30 || e.ax == 56) {
            r04.am = (e.X[0] + e.X[2]) shl 7
            r04.an = (e.X[1] + e.X[3]) shl 7
        } else {
            r04.am = (e.W[0] + e.W[2]) shl 7
            r04.an = (e.W[1] + e.W[3]) shl 7
        }
        if (copyAim) { r04.ao = e.ao; r04.ap = e.ap } // L38
        else {                                       // L40
            if (e.ax == 54 || e.ax == 30 || e.ax == 56 || e.ax == 64) {
                val f = e.wpF
                if (f != null) { r04.ao = f.a shl 8; r04.ap = f.b shl 8 }
                else {
                    r04.ao = (w.player.W[0] + w.player.W[2]) shl 7
                    r04.ap = (w.player.W[1] + w.player.W[3]) shl 7
                }
            } else { r04.ao = r04.am + 25600; r04.ap = r04.an }
            e.ao = r04.ao; e.ap = r04.ap
        }
        var r12 = r04.ao - r04.am
        var r13 = r04.ap - r04.an
        var r14 = e.h(r12, r13)
        if (count > 1) {                             // fan math (L54-L80)
            var r05 = Trig.atan2(r13, r12)  // j.b(r12,r13) = atan2(y=r13,x=r12)
            if (r10 == 0 && (count % 2) != 0) {
                // first shot of odd volley goes straight — L80 re-aim
                r12 = r04.ao - r04.am; r13 = r04.ap - r04.an
                r14 = e.h(r12, r13)
            } else if ((r10 % 2) != 0) {             // odd slot → +side
                val r15 = if ((count % 2) == 0)
                    r05 + (r02 * ((r10 % 2) + (r10 / 2)))
                else if (r10 == 1)
                    r05 + (r02 * ((r10 % 2) + (r10 / 2))) / 2
                else r05 + (r02 * ((r10 % 2) + (r10 / 2)))
                r04.ao = r04.am + ((r14 * Trig.sin(r15)) shr 8)
                r04.ap = r04.an + ((r14 * Trig.sin(Trig.N - r15)) shr 8)
                r12 = r04.ao - r04.am; r13 = r04.ap - r04.an
                r14 = e.h(r12, r13)
            } else if (r10 != 0) {                   // even slot → -side
                val r152 = if ((count % 2) == 0)
                    r05 - (r02 * (r10 / 2))
                else if (r10 == 0)
                    r05 - ((r02 * ((r10 / 2) + 1)) / 2)
                else r05 - (r02 * ((r10 / 2) + 1))
                r04.ao = r04.am + ((r14 * Trig.sin(r152)) shr 8)
                r04.ap = r04.an + ((r14 * Trig.sin(Trig.N - r152)) shr 8)
                r12 = r04.ao - r04.am; r13 = r04.ap - r04.an
                r14 = e.h(r12, r13)
            }
        }
        if (r14 != 0) {                              // L82 velocity
            var speed = 2048
            if (e.ax == 54 && e.Z[9] == 2) speed = 1280
            else if (e.ax == 30 && e.Z[9] == 2 && count == 3 && r10 == 0)
                speed = 2560
            r04.ag = (r12 * speed) / r14
            r04.ah = ((r13 * speed) / r14) + w.kY
        }
        // L104: facing + flags
        r04.av = r04.ao < r04.am
        if (!r04.av) r04.P = r04.P or 1 else r04.P = r04.P and -2
        r04.N = r04.am; r04.O = r04.an
        r04.ak = r04.am shr 8; r04.al = r04.an shr 8
        r04.P = r04.P and -129; r04.P = r04.P and -33; r04.P = r04.P or 16
        val r06 = dirVariant(r04.am, r04.an, r04.ao, r04.ap)
        when (e.ax) {
            54 -> animVariant(r04, e.Z[8], r06)      // i(Z[8], r06)
            30, 56 -> animVariant(r04, e.Z[3], r06)  // i(Z[3], r06)
            else -> animVariant(r04, 0, r06)         // i(0, r06)
        }
        r04.refreshBoxes()                           // t()
        r04.aG = if (e.ax == 54 || e.ax == 30) e.Z[9]
                 else if (e.ax == 56) e.Z[4] else -1
        r04.af = e                                   // owner link
        r04.aC = e.nl
        if (e.ax == 54 && e.Z[0] == 0) return        // L129 early-exit
        if (r10 == 0) w.sfx(if (e.ax == 30) 27 else 16)  // L139/L140
    }
}

/** `i.ax()` (i.java:8170, proven transcription) — the flying-level
 *  waypoint runner FSM. `bY/bZ` are never written on this path (stay 0):
 *  the homing delta is the waypoint's position in the scroll frame, so
 *  `bt.a/b` act as a direction vector × `bt.f` speed — verbatim. */
fun NpcFsm.tickAx54(e: Entity, w: Level0World, p: Entity) {
    if (!e.runnerBz) e.runnerBz = e.al > w.kP + e.Z[7]          // L7 latch
    val chainDone = e.bs >= e.runnerC
    if (e.runnerBz && e.Z[0] != 3 && chainDone && !e.inPlayV(w)) {
        w.removeEntity(e); return                              // L10 k.c(this)
    }
    if (e.runnerBz)                                            // L26
        e.wpBt = if (!chainDone) w.waypointPool.find(e.Z[e.bs + 1]) else null
    e.Z[13] = e.Z[13] - 1                                      // L29 lifetime
    if (e.wpF == null) {                                       // companion bind
        e.wpF = w.waypointPool.find(e.Z[5])
        e.wpF?.let { it.h = it.a - e.ak; it.i = it.b - e.al }
    }
    if (!e.runnerB && !e.iE && e.wpBt != null) {               // L35 arm leg
        e.runnerB = true
        val bt = e.wpBt!!
        val dx = (bt.a - e.bY) shl 8; val dy = (bt.b - e.bZ) shl 8
        val dist = e.h(dx, dy)
        if (dist > 0) {
            e.ag = (dx * (bt.f shl 8)) / dist
            e.ah = (dy * (bt.f shl 8)) / dist
        }
        e.ah += w.kY
        e.av = bt.a < e.bY                                     // L46/L47
        e.ad?.setAnim(1)
    }
    when (e.S) {                                               // L51
        10 -> {                                                // L113 exit
            e.ah = 0; e.ag = 0; e.az = -1
            e.iE = false; e.runnerB = false; e.bs = 4
            if (e.animFinished()) e.P = e.P or 64
            if (e.al > w.kP + 240 && e.Z[0] != 3) { w.removeEntity(e); return }
        }
        in 0..4 -> {                                           // L53 walk
            val f = e.wpF
            if (f == null) runnerFacePlayer(e, p)
            if (e.S != 4) {
                if (e.S == 0 && f != null) e.av = f.a < e.ak   // L56/L58
            } else e.av = false                                // L64
            if (e.Z[0] != 2) {
                val r14 = e.aC; e.aC = r14 - 1
                if (r14 < 0 && e.Z[13] < 0) {                  // L66 timers out
                    if (f != null) {
                        if (e.inPlayV(w)) runnerArriveAnim(e, f.a, f.b)
                    } else if (runnerNearPlayer(e, w)) {
                        runnerArriveAnim(e, p.ak, p.al)        // L77
                    }
                }
            }
        }
        in 5..9 -> {                                           // L80 attack
            val f = e.wpF
            if (f == null) runnerFacePlayer(e, p)
            if (e.S == 9 && f != null) e.av = f.a < e.ak       // L83
            else if (e.S == 5) e.av = false                    // L91
            if (e.animFinished() && e.U == 0) {                // L93
                e.P = e.P or 64
                val r16 = e.aF - 1; e.aF = r16
                if (r16 <= 0) {
                    e.aF = e.Z[12]
                    runnerBurst(e, e.Z[10], e.aD < e.Z[11], w) // a(Z[10],flag)
                    e.aD--
                    if (e.aD <= 0) e.P = e.P and -65
                }
            }
            if ((e.P and 64) == 0 && e.animFinished()) {       // L106
                e.aC = e.Z[6]; e.aF = e.Z[12]; e.aD = e.Z[11]
                if (f != null) runnerWalkAnim(e, f.a, f.b)
                else runnerWalkAnim(e, p.ak, p.al)
            }
        }
        else -> {}
    }
    // L123: homing caps — clamp velocity toward the waypoint vector
    if (e.runnerB && e.wpBt != null) {
        val bt = e.wpBt!!
        if ((kotlin.math.abs(bt.a - e.bY) shl 8) <= kotlin.math.abs(e.ag))
            e.ag = (bt.a - e.bY) shl 8
        if ((kotlin.math.abs(bt.b - e.bZ) shl 8) <= kotlin.math.abs(e.ah))
            e.ah = w.kY + ((bt.b - e.bZ) shl 8)
        if (bt.a == e.bY && bt.b == e.bZ) {                    // L131 arrive
            e.ah = 0; e.ag = 0; e.runnerB = false
            e.runnerD = bt.d; e.iE = true
            e.ad?.setAnim(0)
        }
    }
    e.wpF?.let { it.a = e.ak + it.h; it.b = e.al + it.i }      // L138 pin F
    if (e.iE) {                                                // L141 dwell
        val r19 = e.runnerD - 1; e.runnerD = r19
        if (r19 < 0) { e.bs++; e.iE = false }
        else {
            e.ah = w.kY
            e.wpF?.let { it.a = e.ak + it.h; it.b = e.al + it.i }
        }
    }
    e.integrate()                                              // L148 b(true)
    // b(true) runs bF() every tick on flying levels — bh[k.aj]==3
    // (i.java:4906, proven): waypoint coords = ak / al-kP.
    if (Entity.MISSION_BH[w.kAj] == 3) e.posToWaypoint(w)
}

/** ax24 init (L75/L84 arms of `i(short[])`, i.java:2837, proven) —
 *  `aB = r8[7]`; the S(r8[5])==0 record additionally seeds the shared
 *  projectile pool: `k.aX = new i[aW(50)]` filled with `a(24, bi[24]=40,
 *  S0, az200)` children — `P = this.P`, `aG = -1`, inserted via `k.b`.
 *  The ax24 tick FSM (`ba()`, i.java:13742) is a separate slice. */
fun NpcFsm.initAx24(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    if (rf(5) == 0) {
        e.P = e.P or 640
        if (w.projectilePool == null)
            w.projectilePool = arrayOfNulls(50)               // k.aW = 50
        val pool = w.projectilePool ?: return
        for (i in 0 until 50) {
            val child = e.spawnChildFx(w, 24, 40, 0, 200)     // a(24,40,0,200)
            pool[i] = child
            child.P = e.P
            w.queueInsert(child)                            // k.b(aK)
            child.aG = -1
        }
    }
    e.aB = rf(7)
}

// ============================================================ ax56 = ay()
// Flyer variant (i.java:8385, proven): same clip/burst family as ax54 but
// waypoint-free — lerps to (aq,ar) at Z[12] speed, S13-15 travel anims,
// universal Z[8] attack cooldown, Z[10] move modes {0 hold,1 x,2 y},
// Z[11] target offset applied at init. All proven.

/** `i.i(short[])` L235 arm (i.java:3336, proven) — ax56 init. */
fun NpcFsm.initAx56(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = 100
    e.aB = 300
    e.Z.fill(0)
    e.Z[0] = rf(4)
    for (i in 1..12) e.Z[i] = rf(i + 7)               // Z[1..12] = r8[8..19]
    if (e.Z[4] == 0) {                                // L245 path
        e.Z[5] = 1
    } else if (e.Z[4] == 1) {                         // L239
        if (e.Z[3] == 0) e.Z[3] = 3                   // L243
        e.Z[5] = 1; e.Z[6] = 1; e.Z[7] = 0
        if (e.Z[3] == 1 && e.Z[4] == 1) e.Z[4] = 0    // L247 (dead — Z3!=0 now)
    }
    e.aC = e.Z[2]; e.aD = e.Z[6]; e.aF = e.Z[7]
    e.nl = e.Z[9]
    e.aq = e.ak; e.ar = e.al
    if (e.Z[10] == 1) e.aq += e.Z[11]                 // L253 x-shift
    else if (e.Z[10] == 2) e.ar += e.Z[11]            // y-shift
}

/** `i.az()` (i.java:8600+, proven): still traveling — pos != (aq,ar)
 *  && Z[10]!=0 && !G. */
private fun NpcFsm.runnerTraveling(e: Entity): Boolean {
    if (e.aq == e.ak && e.ar == e.al) return false    // at target → false
    if (e.Z[10] == 0) return false                    // mode 0 → false
    if (e.runnerG) return false                       // pattern done → false
    return true
}

/** `i.l(int,int)` (i.java:8109, proven): travel anim — 15/14 when the
 *  target is above/below, then 13 while x-mismatch. */
private fun NpcFsm.runnerTravelAnim(e: Entity, x: Int, y: Int) {
    if (y < e.al) e.setAnim(15) else e.setAnim(14)
    if (x != e.ak) e.setAnim(13)
}

/** `i.ay()` (i.java:8385, proven transcription). Differences vs ax():
 *  latch is `al > k.P` (no offset); L18 decrements Z[8] every armed tick
 *  (attack cooldown, not lifetime); removal only when offscreen AND below
 *  k.P+240; no waypoint chain — (aq,ar) destination + az() travel check;
 *  burst fires on frame `T==3 && U==0` with timers Z[7]/Z[6]. */
fun NpcFsm.tickAx56(e: Entity, w: Level0World, p: Entity) {
    if (!e.runnerBz) e.runnerBz = e.al > w.kP                 // L7 latch
    if (!e.runnerBz) return                                 // L10 unarmed
    if (!e.inPlayV(w) && e.al > w.kP + 240) {               // L14 offscreen
        w.removeEntity(e); return
    }
    e.Z[8] = e.Z[8] - 1                                     // L18 cooldown
    if (e.wpF == null) {                                    // companion bind
        e.wpF = w.waypointPool.find(e.Z[5])
        e.wpF?.let { it.h = it.a - e.ak; it.i = it.b - e.al }
    }
    when (e.S) {
        10 -> {                                             // L152 exit
            e.ah = 0; e.ag = 0; e.az = -1
            if (e.animFinished()) e.P = e.P or 64
            if (e.al > w.kP + 240) { w.removeEntity(e); return }
        }
        in 0..4 -> {                                        // L26 walk
            val f = e.wpF
            if (f == null) runnerFacePlayer(e, p)
            if (e.S == 4) e.av = false                      // L37
            else if (e.S == 0 && f != null)                 // L36→L29
                e.av = f.a < e.ak
            if (e.Z[0] == 2) return                         // L160 mode-2 hold
            val r12 = e.aC; e.aC = r12 - 1
            if (r12 >= 0 || e.Z[8] >= 0) {                  // L55/L59 windup
                if (runnerTraveling(e)) runnerTravelAnim(e, e.aq, e.ar)
            } else {                                        // Z[8]<0 → attack
                if (f != null) {
                    if (e.inPlayV(w)) runnerArriveAnim(e, f.a, f.b)
                } else if (runnerNearPlayer(e, w)) {
                    runnerArriveAnim(e, p.ak, p.al)         // L51
                }
            }
        }
        in 5..9 -> {                                        // L117 attack
            val f = e.wpF
            if (f == null) runnerFacePlayer(e, p)
            if (e.S == 5) e.av = false                      // L128
            else if (e.S == 9 && f != null)                 // L127→L120
                e.av = f.a < e.ak
            if (e.T == 3 && e.U == 0) {                     // L130 frame-3
                e.P = e.P or 64
                val r17 = e.aF - 1; e.aF = r17
                if (r17 <= 0) {
                    e.aF = e.Z[7]
                    runnerBurst(e, e.Z[5], e.aD < e.Z[6], w)
                    e.aD--
                    if (e.aD <= 0) e.P = e.P and -65
                }
            }
            if ((e.P and 64) == 0 && e.animFinished()) {    // L143-L145
                e.aC = e.Z[2]; e.aF = e.Z[7]; e.aD = e.Z[6]
                if (f != null) runnerWalkAnim(e, f.a, f.b)
                else runnerWalkAnim(e, p.ak, p.al)
            }
        }
        in 13..15 -> {                                      // L63 travel
            e.av = e.aq < e.ak                              // L65
            if (!runnerTraveling(e) || e.runnerG) {         // L70 arrived
                e.ah = 0; e.ag = 0
                if (e.wpF != null) runnerWalkAnim(e, e.wpF!!.a, e.wpF!!.b)
                else runnerWalkAnim(e, p.ak, p.al)          // L74
            } else {
                if (e.Z[10] == 1) {                         // L79 x-move
                    e.ag = if (e.aq > e.ak) e.Z[12] shl 8
                           else -(e.Z[12] shl 8)
                    if (kotlin.math.abs(e.aq - e.ak) < e.Z[12])
                        e.ag = (e.aq - e.ak) shl 8
                } else {                                    // L88 y-move
                    e.ah = if (e.ar > e.al) e.Z[12] shl 8
                           else -(e.Z[12] shl 8)
                    if (kotlin.math.abs(e.ar - e.al) < e.Z[12])
                        e.ah = (e.ar - e.al) shl 8
                }
                // L94: cooldown → attack-transition
                val r15 = e.aC; e.aC = r15 - 1
                if (r15 < 0 && e.Z[8] < 0) {
                    val f = e.wpF
                    if (f != null) {
                        e.av = f.a < e.ak                     // L102
                        if (e.inPlayV(w)) runnerArriveAnim(e, f.a, f.b)
                    } else {
                        runnerFacePlayer(e, p)                // L99 Q()
                        if (runnerNearPlayer(e, w)) runnerArriveAnim(e, p.ak, p.al)
                    }
                    e.ah = 0; e.ag = 0                        // L114
                }
            }
        }
        else -> {}                                          // L159 default
    }
    e.integrate()
    if (Entity.MISSION_BH[w.kAj] == 3) e.posToWaypoint(w)   // bF() tail
}

// ============================================================ ax24 = ba()
// Projectile FSM (i.java:13742, proven) — flight arm for S∈0..4|22..28
// (retire to pool on offscreen; aG 1=trail / 3=chain / else hit), then a
// 45-state per-S table: impact anims, homing legs, lobbed arcs, the S20
// heal-shrine arm, pinned S31/40 children, explode S35/36.
// `bc()`/`bd()` = projectile-vs-entity sweeps (i.java:14396/14538).

/** `i.p(int,int)` (i.java:18031, proven): lay a child ax24 at an offset —
 *  S19 parents spawn S40, others S31. */
private fun NpcFsm.projLay(e: Entity, dx: Int, dy: Int, w: Level0World) {
    val s = if (e.ax == 24 && e.S == 19) 40 else 31
    val child = e.spawnChildFx(w, 24, 40, s, e.az + 10)
    child.av = false
    child.ak = e.ak + dx; child.al = e.al + dy
    child.ao = dx; child.ap = dy
    child.ag = 0; child.ah = 0
    child.refreshBoxes()
    child.P = child.P or 16
    child.af = e
    w.queueInsert(child)
}

/** `i.bc()` (i.java:14396, proven): the projectile-vs-hostiles sweep —
 *  iterates `k.bd[]` (= all npcs); `this.X` is the attack box. Per-ax hit
 *  semantics; `d(8,…)` floatie spawns remain unported (noted). */
private fun NpcFsm.projSweepBc(e: Entity, w: Level0World): Boolean {
    var hit = false
    for (r0 in w.npcs) {
        if (e.X == null) break
        when (r0.ax) {
            54 -> {                                        // L26-fallback arm
                if (e.af == null || e.af!!.ax == 54 || e.af!!.ax == 30) continue
                r0.ad?.let { if (Entity.overlapStrict(it.W, e.X)) {
                    it.setAnim(2); w.countKill(r0.aw); r0.setAnim(10)
                    hit = true } }
                if (Entity.overlapStrict(r0.W, e.X)) { r0.setAnim(10)
                    w.countKill(r0.aw); hit = true }
                if (hit) { e.setAnim(9); return true }
            }
            30 -> {
                if (e.af == null || e.af!!.ax == 54 || e.af!!.ax == 30 ||
                    e.af!!.ax == 56) continue
                if (!Entity.overlapStrict(r0.W, e.X)) continue
                r0.aB -= 20; r0.cGCount = 6
                if (r0.aB <= 0) {
                    r0.cGCount = 0; r0.setAnim(10); w.countKill(r0.aw)
                    r0.ad?.setAnim(2)
                }
                e.setAnim(9); hit = true; return true
            }
            56 -> {
                if (e.af == null || e.af!!.ax == 54 || e.af!!.ax == 56 ||
                    e.af!!.ax == 30) continue
                if (!Entity.overlapStrict(r0.W, e.X)) continue
                r0.setAnim(10); w.countKill(r0.aw)
                e.setAnim(9); hit = true; return true
            }
            67 -> {
                if (r0.S != 19 && r0.S != 21 && r0.S != 23 && r0.S != 32 &&
                    r0.S != 35 && r0.S != 38 && r0.S != 41 && r0.S != 43) continue
                if (!Entity.overlapStrict(r0.W, e.X)) continue
                if (e.S != 9) r0.aB--
                if (r0.aB <= 0) r0.setAnim(r0.S + 1)
                e.setAnim(9); hit = true
            }
            24 -> {
                if (r0.S != 19 || !Entity.overlapStrict(r0.W, e.X)) continue
                r0.setAnim(20); e.setAnim(9); hit = true
            }
            32 -> {
                if ((r0.l and 1) == 0) continue
                if (r0.S in 21..27 && !w.iCF) break          // cF gate → abort sweep
                if (r0.S == 20 || !Entity.overlapStrict(r0.W, e.X)) continue
                if (r0.aB > 0) {
                    r0.aB -= Entity.WEAPON_K[w.weaponSlot]
                    when (r0.iP) {
                        0 -> if (r0.aB > 0) r0.cGCount = 6
                             else { r0.setAnim(15); r0.cGCount = 0 }
                        2 -> if (r0.aB > 0) r0.cGCount = 6
                             else { r0.setAnim(19); r0.cGCount = 0 }
                        3 -> if (r0.aB > 0) r0.cGCount = 6
                             else { r0.setAnim(25); r0.cGCount = 0 }
                        4 -> if (r0.aB > 0) r0.cGCount = 6
                             else { r0.setAnim(36); r0.cGCount = 0 }
                    }
                }
                hit = true
            }
        }
    }
    return hit
}

/** `i.bd()` (i.java:14538, proven): non-flying sweep — ax19 S2→i(3),
 *  ax17/ax23 damage `H[k.au]` via `aB` with death anims. */
private fun NpcFsm.projSweepBd(e: Entity, w: Level0World): Boolean {
    var hit = false
    for (r0 in w.npcs) {
        if (e.X == null) break
        if (!Entity.overlapStrict(r0.W, e.X)) continue
        if (r0.ax == 19 && r0.S == 2) { r0.setAnim(3); hit = true }
        if (e.S != 17) continue
        when (r0.ax) {
            17 -> { if (r0.S != 69) {
                r0.aB -= Entity.WEAPON_H[w.weaponSlot]
                if (r0.aB <= 0) r0.setAnim(129) else r0.setAnim(68)
                hit = true } }
            23 -> { if (r0.S == 79) {
                r0.aB -= Entity.WEAPON_H[w.weaponSlot]
                if (r0.aB <= 0) r0.setAnim(79) else r0.setAnim(73)
                hit = true } }
        }
    }
    return hit
}

/** `i.ba()` (i.java:13742, proven transcription). */
fun NpcFsm.tickAx24(e: Entity, w: Level0World, p: Entity) {
    val inFlight = (e.S in 0..4) || (e.S in 22..28)      // L7/L15 gate
    if (inFlight && (e.P and 128) == 0) {                // live projectiles
        if (!e.inPlayV(w)) {                             // L17 offscreen retire
            e.P = e.P or 128; e.P = e.P and -17
            e.aG = -1; e.af = null; e.c = null
        } else when (e.aG) {
            1 -> {                                       // L23 trail type
                val r1 = e.aC; e.aC = r1 - 1
                if (r1 < 0) {
                    runnerBurst(e, 9, false, w)          // a(9,false)
                    // if (e.S == 22) → d(9,ak,al) floatie — unported
                    e.P = e.P or 128; e.P = e.P and -17; e.af = null
                }
            }
            3 -> {                                       // L31 chain type
                val c = e.c
                if (c != null && Entity.overlapStrict(e.W, c.W)) {
                    e.aG = -1; e.setAnim(9)
                    when (c.ax) { 56 -> c.setAnim(10); 64 -> c.setAnim(5) }
                    w.countKill(e.aw); e.c = null
                }
            }
            else -> {                                    // L44 player hit
                if (Entity.overlapStrict(e.W, p.W)) {
                    p.applyHit(38, 0, e, w)              // k.aS.a(38,0,0,this)
                    e.aG = -1; e.af = null
                    if (e.S in 0..4) { w.removeEntity(e); return }
                }
            }
        }
    }
    when (e.S) {
        6 -> {                                           // L171 drop line
            if (e.al > e.ap) e.setAnim(9)
            e.ap += w.kX
            if (projSweepBc(e, w)) e.setAnim(9)
        }
        7 -> { val r = e.aC - 1; e.aC = r; if (r < 0) e.setAnim(9) }  // L166
        8 -> if (e.animFinished()) { w.removeEntity(e); return }    // L87
        9, 10 -> {                                       // L65 impact anim
            if (e.T == 1 && e.U == 0) w.sfx(12)          // L67 sfx
            e.refreshBoxes()                             // L69 t()
            if (Entity.MISSION_BH[w.kAj] == 3) {
                e.ah = 0; e.ag = 0
                if (e.af == p) projSweepBc(e, w)
                else if (e.X != null && Entity.overlapStrict(e.X, p.W))
                    p.applyHit(38, 0, e, w)              // L75
            } else {
                projSweepBd(e, w)                        // L79 bd()
                if (e.X != null && Entity.overlapStrict(e.X, p.W))
                    p.applyHit(4, 0, e, w)
            }
            if (e.animFinished()) { e.af = null; w.removeEntity(e); return }
        }
        11 -> {                                          // L94 homing leg
            val af = e.af ?: return
            val cH = af.cHWaypoints ?: return
            e.aC--
            if (e.aC <= 0) {
                e.ag = 0; e.ah = w.kY
                e.bY = cH[e.ap][0]; e.bZ = cH[e.ap][1]
                e.posFromWaypoint(w); e.setAnim(9)
            } else {
                val r0 = cH[e.ap][0] - e.bY; val r02 = cH[e.ap][1] - e.bZ
                e.ag = (r0 shl 8) / e.aC; e.ah = ((r02 shl 8) / e.aC) + w.kY
            }
        }
        12 -> { e.aC--; if (e.aC < 0) { w.removeEntity(e); return } } // L90
        13 -> { if (e.animFinished()) e.setAnim(14); e.j--            // L101
            if (e.bZ <= e.ap - 30) e.setAnim(45)
            else if (e.projK) e.setAnim(45)
            else if (e.X != null && Entity.overlapStrict(e.X, p.W)) {
                e.setAnim(9); p.applyHit(38, 0, e, w) }
        }
        14 -> { e.j--                                    // L103 lobbed
            if (e.bZ <= e.ap - 30) e.setAnim(45)
            else if (e.projK) e.setAnim(45)
            else if (e.X != null && Entity.overlapStrict(e.X, p.W)) {
                e.setAnim(9); p.applyHit(38, 0, e, w) }
        }
        15 -> {                                          // L134
            if (e.T == 1 && e.U == 0) w.sfx(12)
            if (e.animFinished()) w.removeEntity(e)
            if (e.X != null && Entity.overlapStrict(e.X, p.W)) {
                e.setAnim(9); p.applyHit(38, 0, e, w) }
        }
        in 16..18, in 41..43 -> {                        // L148
            if (!e.inPlayV(w)) { w.removeEntity(e); return }
            if (e.X != null && Entity.overlapStrict(e.X, p.W)) {
                if (e.S in 16..18) { w.removeEntity(e); return }
                p.applyHit(38, 0, e, w)
            }
        }
        19 -> {                                          // L178 lay-child
            if (!e.projB) { projLay(e, 0, 0, w); e.projB = true }
        }
        20 -> {                                          // L181 heal shrine
            e.az = 100; e.P = e.P and -129
            if (Entity.overlapStrict(p.W, e.W)) {
                w.iBB = true; w.iBC = true; w.iBD = true
                w.iBF = 100; w.iBE = 999; w.iBG = -1
                p.setAnim(21)
                val r03 = 100 - w.kAE
                w.kAF = if (e.aB >= r03) r03 else e.aB
                w.iBh = 0                                  // bh = 0 (i.bh)
                // e = 30 — i.e static unmapped (script var)
                if (w.kAJ != 0) { w.kX = w.kAJ; w.kAJ = 0 }
                w.sfx(25)
            }
        }
        31, 40 -> {                                      // L53 pinned child
            val af = e.af
            if (af == null) {
                e.ag = 0; e.ah = 0
                if (e.animFinished()) { w.removeEntity(e); return }
            } else {
                e.ak = af.ak + e.ao; e.al = af.al + e.ap
                if (af.ax == 24 && af.S == 19) { e.ag = 0; e.ah = 0 }
            }
        }
        35 -> {                                          // L193 explode-arm
            e.projB = true
            if (Entity.overlapStrict(e.W, p.W) ||
                e.e(w, e.ak / 20, e.al / 20) >= 20) {
                e.setAnim(36); e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
            }
        }
        36 -> {                                          // L199 explode
            e.projB = true
            if (e.X != null && Entity.overlapStrict(e.X, p.W)) p.applyHit(4, 0, e, w)
            if (e.animFinished()) { w.removeEntity(e); return }
        }
        44 -> if (e.animFinished()) { w.removeEntity(e); return }   // L163
        45 -> {                                          // L117 lobbed sib
            e.j--
            if (e.bZ <= e.ap) {
                if (e.projK && e.j <= 0) { e.projK = false; e.setAnim(15) }
                else if (!e.projK) e.setAnim(15)
            }
            if (e.X != null && Entity.overlapStrict(e.X, p.W)) {
                e.setAnim(9); p.applyHit(38, 0, e, w) }
        }
        else -> {}                                       // L206 inert
    }
    e.integrate()
}

// =========================================================================
// ax58 — `bg()` lever/switch block (i.java:14633-14697, proven)
// =========================================================================

/**
 * `i.be()` (i.java:14585, proven): "someone standing on the lever zone" —
 * `W ∩ aS.W` short-circuits true; otherwise scans `k.bd[]` for ax∈{11,15}
 * entities overlapping `W`, marks each `P|=16` (pressure flash) and stays
 * true while any overlap holds. Loop marks ALL overlapping (no break).
 */
private fun leverOccupied(e: Entity, w: Level0World, p: Entity): Boolean {
    if (Entity.overlapStrict(e.W, p.W)) return true
    var hit = false
    for (n in w.npcs) {                                   // k.bd[] scan
        if (n.ax != 15 && n.ax != 11) continue
        if (Entity.overlapStrict(e.W, n.W)) { n.P = n.P or 16; hit = true }
    }
    return hit
}

/**
 * `i.bg()` (i.java:14633, proven): ax58 lever/counterweight FSM.
 * `ab()` (claimActive) hands the tick to `aa()` (the claim-script VM).
 * Odd states {0,5,7,9,11} wait for `be()` (lever zone occupied) →
 * `i(S+1)` + one-shot bind: `k.C=this`, `h(k.s(Z0)); k(k.s(Z0))` arms the
 * bound claim script, `Z0=-1`, `k.A(21)`. Even states {1,6,8,10,12} latch
 * `P|64` once the anim ends and fall back `i(S-1)` when the zone clears.
 * S2 runs the shared `a()` interact sweep, a crush arm (`W∩aS.X` while the
 * player isn't in S22 → `i(3)` + sfx21), releases the `k.L` claim when it's
 * ours (`k.m()`), then `G()` drops `ae`. S3 (played once) → `i(4)` + the
 * same bind tail. S4 parks (`P|32`).
 */
fun NpcFsm.tickAx58(e: Entity, w: Level0World, p: Entity) {
    e.advanceAnim()
    if (e.claimActive()) { e.runClaimScript(w); return }  // ab() → aa()
    when (e.S) {
        0, 5, 7, 9, 11 -> {                               // L9 — armed wait
            if (!leverOccupied(e, w, p)) return           // be()
            e.setAnim(e.S + 1)
            if (e.Z[0] > 0) {
                w.kC = e                                // k.C = this
                e.bindScript(w.kSIndex(e.Z[0]), w)        // h(k.s(Z0))
                e.scriptKeyStep(w.kSIndex(e.Z[0]), w)     // k(k.s(Z0))
                e.Z[0] = -1
            }
            w.sfx(21)                                   // k.A(21)
        }
        1, 6, 8, 10, 12 -> {                            // L16 — open latch
            if (e.animFinished() && (e.P and 64) == 0) e.P = e.P or 64
            if (!leverOccupied(e, w, p)) e.setAnim(e.S - 1)
        }
        2 -> {                                          // L24 — active lever
            e.pushContact(w)                            // a()
            if (Entity.overlapStrict(e.W, p.X) && p.S != 22) {
                e.setAnim(3); w.sfx(21)
            }
            if (w.claimed != null && w.claimed!!.aw == e.aw) w.clearClaim()  // k.m()
            e.releaseAe()                               // G()
        }
        3 -> {                                          // L39 — fired
            if (!e.animFinished()) return
            e.setAnim(4)                                // L41 (S==3 always)
            if (e.Z[0] > 0) {                           // L44 bind tail
                e.bindScript(w.kSIndex(e.Z[0]), w)
                e.scriptKeyStep(w.kSIndex(e.Z[0]), w)
                e.Z[0] = -1
            }
        }
        4 -> e.P = e.P or 32                            // L36 — parked
        else -> {}                                      // L47 inert
    }
}
