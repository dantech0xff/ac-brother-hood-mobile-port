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
}
