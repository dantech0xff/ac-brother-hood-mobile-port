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
        /** `k.bk` — ax67 prop kind→clip table (k.java:8444, proven). */
        private val BK = intArrayOf(
            24, 27, 27, 27, 34, 35, 37, 41, 64, 64, 65, 67, 49, 69, 70)
        /** ax67 clip resolved by kind (i.java:2633 `aa = k.r(bk[r8[7]])`). */
        fun decorClip(kind: Int): Int =
            if (kind >= 0 && kind < BK.size) BK[kind] else -1
    }

    /** ax11/73 record init (i.java:2230-2271 + ctor tail :2860-2900,
     *  proven). `f` = the entity record fields; `w` for the Z[13] claim
     *  bind + `a(true)`/`E()` settle tails. */
    fun initSoldier(e: Entity, f: List<Int>, w: LevelCellSource) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = rf(17)
        // aB = bu[k.au] — difficulty max HP (kAu, wired slice 197)
        e.aB = BU[w.kAu]
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
        e.Z[20] = 0
        // hardened/weakened records double HP (proven :2256-2258)
        if (e.Z[0] == 1 || e.ax == 73) e.aB = e.aB shl 1
        e.Z[13] = rf(16)
        // script-claim bind (proven :2263-2267): h/k(k.s(Z[13])) +
        // d=true + cd[7] — bindScript allocates cd + sets cd[7] itself.
        if (e.Z[13] != -1) {
            e.bindScript(w.kSIndex(e.Z[13]), w)
            e.scriptKeyStep(w.kSIndex(e.Z[13]), w)
            e.scriptBound = true
        }
        if (rf(5) == 33) e.P = e.P or 16                       // :2269
        e.setAnim(rf(5))                                        // i(sArr[5])
        e.refreshBoxes()                                        // t()
        // ctor tail for ax11 (proven :2895-2898): a(true); E()
        e.collideSides(w, true)
        e.settleToGround(w)
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

    /** `b(k.aS)` simplified: alert-zone box + facing + same band. The
     *  original's `a(this.W, aS.W)` is a rect-overlap test between the
     *  Z[9..12] zone and the player's collision box — not a point test —
     *  so a player hugging the zone edge still alerts. */
    private fun seesPlayer(e: Entity, player: Entity): Boolean {
        // same-row within one cell of height
        if (abs(player.al - e.al) / 20 > 1) return false
        if (player.W[2] < e.Z[9] || player.W[0] > e.Z[10]) return false
        if (player.W[3] < e.Z[11] || player.W[1] > e.Z[12]) return false
        // facing must cover the player
        if (e.av && player.ak > e.ak) return false
        if (!e.av && player.ak < e.ak) return false
        return true
    }

    // -- per-tick ------------------------------------------------------------

    /** `aH()` corpse-family set (i.java:7287-7298, proven): states that
     *  are already on the death path — exempt from the `aB<=0 → i(0)`
     *  re-entry at `I()` head. */
    private val AH_STATES = intArrayOf(0, 20, 21, 106, 107, 117, 139, 168, 169, 176)

    fun tick(e: Entity, player: Entity) {
        // `I()` covers only the shared soldier family {11,17,23,47,50,73}
        // (i.java dispatch :15493, proven); every other ax reaches the
        // dispatch's `default:` label L1f35 — a small tail, not a no-op.
        if (e.ax != 11 && e.ax != 17 && e.ax != 23 && e.ax != 47 &&
            e.ax != 50 && e.ax != 73) { defaultArm(e, player); return }
        // L70-L78 (i.java:4886-4898, proven): the fixed-point integrator
        // runs at the TOP of I() for every entity — under `aH` slow-mo it
        // swaps to the aI-divided variant (L72→L75, i.java:6370-6384).
        e.integrate(if (world.iAH) maxOf(1, world.iAI) else 1)
        e.collideSides(world, true)
        // `I()` head (i.java:4024, proven): aB<=0 on any live state →
        // i(0) death entry. Without this an S85/SC hurt soldier recovered
        // at aB=0 instead of dying.
        if (e.aB <= 0 && e.S != 184 && e.S !in AH_STATES) e.setAnim(0)
        // L777 tail flags (i.java:5221-5232, proven): r12 forces the
        // knockdown close, r13 gates aB() melee (default ON — an arm may
        // clear it), r14 gates j() intake, r15 gates k() kill driver.
        // L256-L259: with the player ALIVE r14+r15 default ON for every
        // tail-reaching state (unaware guards still take stealth kills;
        // the per-arm overrides then narrow it).
        // tail = [r12, r13, r14, r15]
        val tail = booleanArrayOf(false, true, false, false)
        if (!world.gG()) { tail[2] = true; tail[3] = true }   // g(aS)==false
        // L259-L261: `S!=24 → j = d()` sight priority; `e()` rebuilds
        // the Z[9..12] alert rect (ak+Z15, al+Z16, +Z17, +Z18).
        if (e.S != 24) e.j = sightPriorityD(e, player, world)
        e.Z[9] = e.ak + e.Z[15]; e.Z[11] = e.al + e.Z[16]
        e.Z[10] = e.ak + e.Z[15] + e.Z[17]
        e.Z[12] = e.al + e.Z[16] + e.Z[18]
        armsAndL777(e, player, tail)
        // L849 (i.java:5197) → L897 (:6355-6372) → s() (:6397-6411):
        // every path — the L777 fall-through and every `goto L849`
        // arm alike — runs au() corpse-drop, t() box refresh, the
        // facing bit, the a(k.aS,P,W) push, then s() anim advance
        // (skipped while cu-held, S<0, or mid slow-mo interval).
        corpseDrop(e)                                        // au()
        e.refreshBoxes()                                     // t()
        if (e.av) e.P = e.P or 1 else e.P = e.P and -2       // L900-902
        pushL897(e, player)                                  // a(k.aS,P,W)
        if (!e.cu && e.S >= 0 &&
            (!world.iAH || world.jG % maxOf(1, world.iAI) == 0L)) {
            e.advanceAnim()                                  // s()
        }
    }

    // `case 11/17/23/47/50 → L104` — the shared soldier dispatch:
    // `when(S)` arms; `return` = `goto L849` (skips the L777 checks but
    // still reaches the shared tail back in tick()). Arms that fall
    // through run the L777 shared checks below.
    private fun armsAndL777(e: Entity, player: Entity, tail: BooleanArray) {
        when (e.S) {
            2, 3, 92 -> patrolArm(e, player, tail)
            4, 22 -> {
                // L451-L474 (i.java:5536-5562, proven): edge → drop
                // alert → L849; else L457 chase-continue → r14 → j().
                if (!chaseArm(e, player)) return            // aG() → L849
                tail[2] = true
            }
            5 -> {
                // L438 (i.java:5507, proven): `aL=this` claim + `r14`
                // (j() intake) then `r()` → i(4)+aC=0 → L777.
                Entity.aL = e; tail[2] = true
                if (e.animFinished()) { e.setAnim(4); e.aC = 0 }
            }
            23 -> {
                // L444 windup-approach: ag=∓512 toward player; aF() done →
                // strike anim 12 (the contact arm at L478 runs next).
                facePlayer(e, player)
                e.collideSides(world, true)
                e.ag = if (e.av) -512 else 512
                if (e.animFinished()) e.setAnim(12)
            }
            12 -> {
                // L478-L502 (i.java:5580-5640, proven): prelude
                // `r13=true; r14=true; r15=false` — the shared tail runs
                // j() + push (r13/r14). Player rolling (aS.S==6) through
                // own X while facing us → the counter-bind:
                // `aS.a(34,0,0,this)` + `i(18)`; Z0==2 →
                // L490 (aN=this + g.E=true + b(2) + marker) → L849;
                // Z0==0 && aB>bu/2 → L849; Z0==0 && aB<=bu/2 → L495 tail.
                // r() → g.g()? i(2) : i(23)+aC=10 → L777.
                tail[2] = true
                if (Entity.overlapI(player.W, e.X) &&
                    player.inFrontOf(e) && player.S == 6) {
                    player.applyHit(34, 0, e, world)            // aS.a(34,0,0,this)
                    e.setAnim(18)
                    if (e.Z[0] == 2) {
                        world.lockTarget = e                    // aN = this
                        Entity.gE = true                        // g.E = true
                        e.eventArm(2, world)                    // b(2) slowmo
                        e.spawnMarker(world, 8, e.ak, e.al - 85)
                        return                                  // → L849
                    }
                    if (e.Z[0] != 0 || e.aB > BU73[world.weaponSlot] / 2)
                        return                                  // → L849
                }
                if (e.animFinished()) {                         // L495 r()
                    if (world.gG()) e.setAnim(2)
                    else { e.setAnim(23); e.aC = 10 }
                }
            }
            11 -> {
                // L475 (i.java:5578-5585, proven): windup-2 — Q() face
                // player + `r14=true`; r() → G() + i(12) strike. → L777
                // (r14 → j()).
                e.av = player.ak < e.ak                         // Q()
                tail[2] = true
                if (e.animFinished()) {
                    e.releaseAe()                               // G()
                    e.setAnim(12)
                }
            }
            24 -> {
                // L623 (i.java:5720-5746, proven): `r15=true` — thrown-
                // victim drop; k() tail live. Holds the player pinned at own
                // top-edge (aS.ah=ag=0, al=W[1], ak=W mid, O/N snap) while
                // `aC-- > 0`; on expiry `aS.a(0)` airborne fling +
                // `aS.G()`, then picks the nearer OPEN side edge:
                // aT=e(W[0]-pw, W[1])>=12 → aS.ak=left open, av=false;
                // aU=e(W[2]+pw, W[1])>=12 → aS.ak=right open, av=true;
                // `aA=1` + `i(5)`. → L777.
                tail[3] = true
                world.kAA = 60                                  // k.aA = 60
                player.ah = 0; player.ag = 0
                player.al = e.W[1]
                player.ak = (e.W[0] + e.W[2]) shr 1
                player.O = player.al shl 8
                player.N = player.ak shl 8
                if (e.aC-- > 0) {
                    // window live → L777 (the shared tail below)
                } else {
                    player.flingAirborne(0, world)              // aS.a(0)
                    player.releaseAe()                          // aS.G()
                    val pw = player.W[2] - player.W[0]
                    val lx = e.W[0] - pw
                    val rx = e.W[2] + pw
                    val ey = e.W[1]
                    e.aT = e.e(world, lx / 20, ey / 20)
                    e.aU = e.e(world, rx / 20, ey / 20)
                    if (e.aT >= 12) {
                        player.ak = lx; player.av = false
                    } else if (e.aU >= 12) {
                        player.ak = rx; player.av = true
                    }
                    e.aA = 1
                    e.setAnim(5)
                }
            }
            99 -> {
                // L299 (i.java:5442-5445, proven): `r15=true` — the tail
                // runs k(); `aE()` (kill-touch) fires for side effects —
                // it always returns false so `r13=false` is dead code.
                tail[3] = true
                killTouch(e, world, player)
            }
            133 -> {
                // L293 (i.java:5850-5856, proven): carried — track the
                // carrier while aS.S==270 (carry-pickup), else i(135).
                if (player.S == 270) {
                    e.ak = player.ak; e.al = player.al
                    e.T = player.T; e.U = player.U
                } else { e.setAnim(135); return }               // → L849
            }
            134 -> {
                // L297 (i.java:5860-5863, proven): carried-hold — no-op
                // while aS.S==271 (carry-walk), else i(135) → L849.
                if (player.S != 271) { e.setAnim(135); return }
            }
            138 -> {
                // L280-L287 (i.java:5826-5842, proven): hostage-free —
                // r() && af!=null → af.i(k.bK?3:10) + k.A(18) + af=null
                // + P|=32|64 → L777; af==null → L777 (no-op).
                if (e.animFinished() && e.af != null) {
                    e.af!!.setAnim(if (world.kBK) 3 else 10)
                    world.sfx(18)
                    e.af = null
                    e.P = e.P or 32 or 64
                }
            }
            142 -> {
                // L271 (i.java:5436-5441, proven): dropped — falls 10px
                // per tick + a(true); grounded → i(143). → L849.
                e.al += 10
                e.collideSides(world, true)
                if (!e.aZ) return                               // still falling
                e.setAnim(143)
                return                                          // → L849
            }
            143 -> {
                // L276 (i.java:5820-5825, proven): landed-dead —
                // r() → aB=0 + P|=32|64 → L849.
                if (e.animFinished()) {
                    e.aB = 0
                    e.P = e.P or 32 or 64
                }
                return                                          // → L849
            }
            145 -> {
                // L289 (i.java:5840-5849, proven): carried — track the
                // carrier while aS.S==271 (carry-walk), else i(135).
                if (player.S == 271) {
                    e.ak = player.ak; e.al = player.al
                    e.T = player.T; e.U = player.U
                } else { e.setAnim(135); return }               // → L849
            }
            168 -> {
                // L264 (i.java:5808-5818, proven): hostage
                // secure — r() → G() + aB=0 + i(169) + k.e(0,aw) +
                // k.o(3) + aS.i(293) + aS.P&=-65 + ar=aS.al + az=301.
                if (e.animFinished()) {
                    e.releaseAe()                               // G()
                    e.aB = 0
                    e.setAnim(169)
                    world.statTally(e.aw)                       // k.e(0,aw)
                    if (world.kAj != 7) world.kAp[3]++          // k.o(3)
                    player.setAnim(293)                         // aS.i(293)
                    player.P = player.P and -65                 // aS.P&=-65
                    e.ar = player.al                            // ar=aS.al
                    e.az = 301
                }
                return                                          // → L849
            }
            169 -> {
                // L267 (i.java:5427-5435, proven): carried-rise —
                // P|=512 + t() + ah=-2048; once W[1] passes ar the
                // rise ends → ah=0 + P|=32|64 → L849.
                e.P = e.P or 512
                e.refreshBoxes()                                // t()
                e.ah = -2048
                if (e.W[1] > e.ar) return                       // still rising
                e.ah = 0
                e.P = e.P or 32 or 64
                return                                          // → L849
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
                // L302 (i.java:5447-5476, proven): hit-react — G(); the
                // b() corner-support probe: while moving (ag!=0), facing
                // accel ai=∓1280; any corner cell >=12 → velocity-reversal
                // kick (ag=-ag∓2560, N+=ag) + snap ak=N>>8 + full freeze;
                // unsupported (all corners open) → keep drifting via
                // ak=N>>8. r() → i(174) grab lunge + az=aS.az+1 → L777.
                e.releaseAe()                                   // G()
                val r05 = e.cornerSupported(world)              // b()
                if (e.ag != 0) {
                    if (e.av) {
                        e.ai = -1280
                        if (r05) { e.ag = -e.ag - 2560; e.N += e.ag }
                    } else {
                        e.ai = 1280
                        if (r05) { e.ag = -e.ag + 2560; e.N += e.ag }
                    }
                    e.ak = e.N shr 8
                    if (r05) { e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0 }
                }
                if (e.animFinished()) {                         // L316 r()
                    e.ah = 0; e.ag = 0
                    e.setAnim(174)
                    e.az = player.az + 1
                }
            }
            1 -> {
                // L584 (i.java:6068-6083, proven): stun-step — aA==0 →
                // av=(aS.ak>ak); ag=av?2048:-2048; r() → i(25). → L849.
                if (e.aA == 0) e.av = player.ak > e.ak
                e.ag = if (e.av) 2048 else -2048
                if (e.animFinished()) e.setAnim(25)
                return
            }
            6 -> {
                // L563 (i.java:5641-5697, proven): the grab approach-lunge
                // — Q() face player, G(), a(true); aF()||y() → ag=0, else
                // accel ai=∓1280; T==1 blood markers (59 on k.bK); aA=1;
                // r() → i(23). Whole state → L849 (skips the L777 tail).
                e.av = player.ak < e.ak                         // Q()
                e.releaseAe()                                   // G()
                e.collideSides(world, true)                     // a(true)
                if (e.aF(world) || e.yWall()) e.ag = 0
                if (e.ag != 0) e.ai = if (e.av) -1280 else 1280
                if (e.T == 1) {
                    e.spawnFx8(world, 50, 1, e.av, e.ak, e.al - 40, 300)
                    if (world.kBK)
                        e.spawnFx8(world, 59, 0, e.av, e.ak, e.al - 40, 300)
                }
                e.aA = 1
                if (e.animFinished()) e.setAnim(23)
                return
            }
            16 -> {
                // L504 (i.java:6030-6034, proven): engage windup —
                // r() → i(17) + aC=16.
                if (e.animFinished()) { e.setAnim(17); e.aC = 16 }
            }
            17 -> {
                // L532-L546 (i.java:6036-6050, proven): counter-engage —
                // aS.X non-degenerate ∩ W + g.b() → aS.i(8)+k.E.P|=128
                // (aS.S==8 → L849); r() → Z0==2 ? i(11) : i(23).
                val counter = player.X[0] != player.X[2] &&
                    Entity.overlapStrict(e.W, player.X) &&
                    world.playerAttacking()
                if (counter) {
                    if (player.S == 8) return                   // L849
                    player.setAnim(8)
                    world.kE?.let { it.P = it.P or 128 }
                }
                if (e.animFinished())
                    e.setAnim(if (e.Z[0] == 2) 11 else 23)      // L542→L777
            }
            18 -> {
                // L506 (i.java:5546, proven): `a(true)` + `r14=true` —
                // the offer arm runs j() intake in the shared tail.
                e.collideSides(world, true)
                tail[2] = true
                grabOfferArm18(e, player, world)
            }
            20 -> {
                // L728 (i.java:6195-6203, proven): r() → aB<=0→aB=0,
                // H() consume, i(139) corpse, k.e(0,aw) tally. → L849.
                if (e.animFinished()) {
                    if (e.aB <= 0) e.aB = 0
                    e.consumeH()                                // H()
                    e.setAnim(139)
                    world.statTally(e.aw)                       // k.e(0,aw)
                }
                return
            }
            27 -> {
                // L734 (i.java:5753-5758, proven): ab=null; r() → i(25)
                // + k.A(24) impact sfx. → L849.
                e.ab = null
                if (e.animFinished()) { e.setAnim(25); world.sfx(24) }
                return
            }
            96 -> {
                // L737 (i.java:5759-5769, proven): P|=512; r() → release
                // the g.h holder link + k.c(this) remove. → L849.
                e.P = e.P or 512
                if (e.animFinished()) {
                    if (world.grabHolder === e) world.grabHolder = null // g.h
                    world.removeEntity(e)                       // k.c(this)
                }
                return
            }
            117 -> {
                // L751 (i.java:5770-5776, proven): aB=0; r() → tally +
                // k.o(3) + remove. → L849.
                e.aB = 0
                if (e.animFinished()) {
                    world.statTally(e.aw)                       // k.e(0,aw)
                    world.kAp[3]++                              // k.o(3)
                    world.removeEntity(e)                       // k.c(this)
                }
                return
            }
            140 -> {
                // L354 (i.java:5916-5919, proven): grab-release settle —
                // r() → i(23). → L777.
                if (e.animFinished()) e.setAnim(23)
            }
            144 -> {
                // L548-L559 (i.java:6048-6066, proven): weakened block —
                // T==3 → c(1); aS.X ∩ W + g.b() + aS.S!=8 → counter
                // aS.i(8)+k.E.P|=128; L559: a() body-push; r() → i(23) +
                // aS.G(). Whole state → L849. Original ordering: h()→i()
                // engage (i.java:4184) runs BEFORE the switch — when it
                // applies, the soldier lands at i(17) and this arm's own
                // counter is shadowed. Our hGate/iEngage run post-when,
                // so reproduce the precedence: engage first, own counter
                // only when h()'s posed-immunity excludes the engage.
                if (e.T == 3) world.tutorialHint(1)             // c(1)
                val counter = player.X[0] != player.X[2] &&
                    Entity.overlapStrict(e.W, player.X) &&
                    world.playerAttacking() && player.S != 8
                if (counter) {
                    if (e.Z[0] != 3 && hGate(e, player)) {
                        iEngage(e, player, world)          // aS.i(8)+i(17)+aC=16
                        return                           // → L849
                    }
                    player.setAnim(8)
                    world.kE?.let { it.P = it.P or 128 }
                }
                e.pushContact(world)                            // L559 a()
                if (e.animFinished()) {
                    e.setAnim(23)
                    player.releaseAe()                          // aS.G()
                }
                return                                          // → L849
            }
            174 -> {
                // L318-L327 (i.java:5478-5515, proven): grab approach —
                // c(0) tutorial; player-W ∩ W → i(175) + aS.i(310) (held)
                // + player freeze/snap (ah=ag=aj=ai=0, ak=this.ak,
                // av=!this.av) + al=aS.al + G() + bl=40 + bx=this +
                // ah=ag=0 → L849; else r() → i(140) → L777.
                world.tutorialHint(0)                           // c(0)
                if (Entity.overlapStrict(e.W, player.W)) {
                    e.setAnim(175)
                    player.setAnim(310)
                    player.ah = 0; player.ag = 0
                    player.aj = 0; player.ai = 0
                    player.ak = e.ak
                    player.av = !e.av
                    e.al = player.al
                    e.releaseAe()                               // G()
                    e.bl = 40; world.iBx = e
                    e.ah = 0; e.ag = 0
                    return                                      // → L849
                }
                if (e.animFinished()) e.setAnim(140)            // L326→L777
            }
            175 -> { grabHoldArm175(e, player, world); return }  // → L849
            176 -> {
                // L348 (i.java:5907-5911, proven): counter-execute wind —
                // r() → i(139) corpse + k.e(0,aw). → L777.
                if (e.animFinished()) {
                    e.setAnim(139)
                    world.statTally(e.aw)
                }
            }
            177 -> {
                // L351 (i.java:5912-5915, proven): grab-release — r() →
                // i(140). → L777.
                if (e.animFinished()) e.setAnim(140)
            }
            179 -> {
                // L748 (i.java:6205-6208, proven): mount-grab release —
                // r() → i(2). → L849.
                if (e.animFinished()) e.setAnim(2)
                return
            }
            180, 181 -> {
                // L756 (i.java:6209-6213, proven): mount lunge — r() →
                // i(S+2) (→182/183); every tick aI() throw attempt.
                // Falls into L777.
                if (e.animFinished()) e.setAnim(e.S + 2)
                e.throwFromGrab(world)                          // aI()
            }
            182, 183 -> {
                // L759 (i.java:5777-5778, proven): aI() throw attempt.
                // Falls into L777.
                e.throwFromGrab(world)                          // aI()
            }
            184 -> {
                // L760 (i.java:5780-5802, proven): post-throw landing —
                // ab=null, ag=0, aj=1536, aL=null; when the current cell
                // is solid (∈{>=18,2,3}) snap al to the cell edge
                // (above-cell >=18 → snap up; 2/3 → snap down; open →
                // snap up), zero all velocity, i(0), T=aa.b(S)/2. The
                // original's `System.out.println("check phy right")`
                // debug remnant is verbatim — noted, not carried.
                e.ab = null; e.ag = 0; e.aj = 1536; Entity.aL = null
                val r015 = e.e(world, e.ak / 20, e.al / 20)
                val r016 = e.e(world, e.ak / 20, e.al / 20 - 1)
                if (r015 >= 18 || r015 == 2 || r015 == 3) {
                    e.al = if (r016 >= 18 || r016 == 2 || r016 == 3)
                        ((e.al / 20) - 1) * 20 + 1
                    else (e.al / 20) * 20 + 1
                    e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
                    e.setAnim(0)
                    e.T = (e.clip?.frameCount(e.S) ?: 0) / 2    // aa.b(S)/2
                }
                return                                          // → L849
            }
            139 -> {
                // L699 (i.java:6158-6167, proven): corpse rest —
                // r() → P&=-17 + P|=32|64 freeze; k.bK → death wisp
                // a(8,59,2,av,ak,al,az-1). → L849.
                if (e.animFinished()) {
                    e.P = e.P and -17; e.P = e.P or 32 or 64
                    if (world.kBK) e.spawnFx8(world, 59, 2, e.av,
                        e.ak, e.al, e.az - 1)
                }
                return                                          // → L849
            }
            0, 106, 107, 135 -> {
                // Shared death/dormant arm (i.java:4044-4096, proven):
                // a(true), G(), P|=512, ab=null, ag=ah=0 (+crate ride),
                // aA=2, hit-frame blood fx on 106/107, r() → corpse or freeze.
                e.collideSides(world, true)                  // a(true)
                e.releaseAe()                                // G()
                e.P = e.P or 512
                if (e.ab != null) e.ab = null
                e.ag = 0; e.ah = 0
                if (e.s != null && e.s!!.ax == 51 && e.s!!.ag != 0) e.ag = e.s!!.ag
                e.aA = 2
                if (e.S == 106 && (e.T == 1 || e.T == 5 || e.T == 7)) {
                    e.spawnFx8(world, 50, 1, e.av, e.ak, e.al - 40, 300)
                    if (world.kBK) e.spawnFx8(world, 59, 1, e.av, e.ak, e.al - 40, 300)
                }
                if (e.S == 107 && (e.T == 2 || e.T == 5)) {
                    e.spawnFx8(world, 50, 1, e.av, e.ak, e.al - 40, 300)
                    if (world.kBK) e.spawnFx8(world, 59, 1, e.av, e.ak, e.al - 40, 300)
                }
                if (e.animFinished()) {                       // r()
                    if (e.aB > 0) e.aB = 0
                    if (world.lockTarget === e) world.lockTarget = null   // aN
                    if (world.player.gb === e) world.player.gb = null     // g.b
                    if (e.S != 106 && e.S != 107 && e.S != 135) {
                        e.setAnim(139)
                        world.statTally(e.aw)                 // k.e(0,aw)
                    } else {
                        e.P = e.P and -17; e.P = e.P or 32 or 64
                        if (world.kBK) e.spawnFx8(world, 59, 2, e.av,
                            if (e.av) e.ak + 18 else e.ak - 18, e.al, e.az - 1)
                    }
                }
            }
            else -> {
                if (e.aA == 0 && e.animFinished()) e.setAnim(3)  // inferred
            }
        }
        // ---- L777 shared tail (i.java:6213-6250, proven) ----------------
        // The per-S arms set flags at their preludes (I() head defaults
        // `r12=false, r13=true, r14=false, r15=false` — :5223-5226);
        // `return` inside an arm = `goto L849` (skip tail), else fall
        // through here.
        //
        // L777 head: `Q==6 → ab=null`; `n(44,0) && S!=20 → aB=0+r12+i(20)`;
        // `S!=85 → aD()` crate-ride; `!h(ak/20,al/20) && !h(ak/20,al/20+1)
        // && s==null → i(25)+k.A(24)`; `r12 → aL=null; aS.aA>1 → L849;
        // else k.aA=0 → L849`.
        if (e.Q == 6) e.ab = null
        if (e.S != 20 && doorScan73(e, world)) {              // n(44,0)
            e.aB = 0; tail[0] = true; e.setAnim(20)
        }
        if (e.S != 85) {
            crateRide11(e)                              // aD() support finder
            val s0 = e.s
            if (s0 != null && s0.ax == 51 && s0.ag != 0) e.ag = s0.ag
        }
        if (e.s == null && e.standingOn == null &&
            !h(e.ak / 20, e.al / 20) && !h(e.ak / 20, e.al / 20 + 1)) {
            e.setAnim(25)                               // open cells → fall
            world.sfx(24)                               // k.A(24)
        }
        if (tail[0]) {
            Entity.aL = null
            if (player.aA > 1) return                   // → L849
            world.kAA = 0
            return                                      // → L849
        }
        // `h() → i()` counter-engage (i.java:1271 + :1278, proven; call
        // sites :7629 in aJ() and :4184 + :6228 in I() — `if (Z[0]!=3 &&
        // h()) i()` runs BEFORE `j()`): the NPC catches an in-flight
        // attack — player forced to S8, companion `k.E` hidden, NPC →
        // aC=16 + i(17) (ax73 i(167), verbatim `S!=17` guard). h()
        // immunity: S216/217 finishers never engage; ax11-Z0==2 engages
        // unless posed {11,12,6}; else only ax73 during S69.
        if (e.ax == 11 && e.Z[0] != 3 && hGate(e, player)) {
            iEngage(e, player, world)
        }
        // `r15 → k()` kill driver (i.java:6230)
        if (tail[3] && contextK(e, world, player)) return   // → L849
        // `r14 → j()` player→NPC damage intake (i.java:6234; port :1305-1403)
        e.refreshBoxes(); player.refreshBoxes()
        if (tail[2] && jIntake(e, player, world)) return    // → L849
        // `k.C != null && k.C.ab() → r13=false` (i.java:6238-6240, proven):
        // a live script-claim suppresses the melee application.
        val kc = world.kC
        if (kc != null && kc.claimLive()) tail[1] = false
        // `r13 → aB()` (i.java:6244; ax11/ax73 arms :8845-8918)
        if (tail[1]) meleeApply(e, player, world)
        // L827+ aA-branch (i.java:6245-6250): non-attacker drops the
        // actor flag, and either pushes the player (aS.aA<=2 + !aA&8 →
        // a()) or alerts on LOS while the player attacks (l() →
        // aS.a(32)); attacker raises it and counters a blocking player
        // (aS.aA&1 + aS.g(this) + j!=0 → aS.a(32)).
        if (e.aA == 0) {
            e.P = e.P and -17
            if (player.aA <= 2) {
                if (player.aA and 8 == 0) e.pushContact(world)  // a()
            } else if (losL(e, player, world)) {
                player.applyHit(32, 0, e, world)        // aS.a(32,0,0,this)
            }
        } else {
            e.P = e.P or 16
            if ((player.aA and 1) != 0 && player.inFrontOf(e) && e.j != 0)
                player.applyHit(32, 0, e, world)
        }
    }

    /** `a(k.aS, this.P, this.W)` at L897 (i.java:15324-15380, proven):
     *  while `P&4096` and the entity's W overlaps the player's, resolve
     *  the penetration — player falling or entity rising → feet land on
     *  the entity's top edge; player rising into it → pushed under; else
     *  side-push to the nearer open edge with velocity zeroed. */
    /**
     * `I()` `default:` arm L1f35→L1f76 (i.java:18904-18939, proven): every
     * ax without a case — the 26 no-op types PLUS the four unreachable
     * labels L1e35/L1f1d/L1f13/L1f27 (ax 28/31/75/45, which `goto L1f35`
     * too, proven) — still gets the shared tail: `if (b) t()` box refresh
     * on the dirty flag, the `av` facing bit into `P|1`, then the
     * `a(k.aS, P, W)` player push (pushL897's own `ax!=0`/`P&4096` gates
     * cover the original's `if (ax==0) skip`). No `s()` advance —
     * unclaimed entities never animate.
     */
    fun defaultArm(e: Entity, player: Entity) {
        if (e.b) e.refreshBoxes()
        if (e.av) e.P = e.P or 1 else e.P = e.P and -2
        pushL897(e, player)
    }

    private fun pushL897(e: Entity, p: Entity) {
        if (e.ax == 0) return                                    // L909
        if (e.P and 4096 == 0) return                            // L5
        val r9 = e.W
        if (!Entity.overlapI(p.W, r9)) return                    // L7
        if (p.ah > 0 || e.ah < 0) {                              // → L11
            if (p.W[1] < r9[1] && p.W[3] < r9[3] &&
                p.ak > r9[0] && p.ak < r9[2]) {
                if (p.ah > 0) { p.aj = 0; p.ah = 0 }
                p.al = r9[1] - 5
                return
            }
        }
        if (p.ah < 0 || e.ah > 0) {                              // → L28
            if (p.W[3] > r9[3] && p.W[1] > r9[1] &&
                p.ak > r9[0] && p.ak < r9[2]) {
                if (p.ah < 0) { p.aj = 0; p.ah = 0 }
                if (p.aZ && e.ah > 0) return                     // L41
                p.al = r9[3] + (p.al - p.W[1]) + 5
                return
            }
        }
        if (p.ag > 0 || e.ag < 0 || e.ax == 27) {                // → L52
            if (p.W[2] < r9[2]) {                                // left push
                if (p.ag > 0) { p.ai = 0; p.ag = 0 }
                p.ak = r9[0] - (p.W[2] - p.ak) - 5
                return
            }
        }
        if (p.ag < 0 || e.ag > 0 || e.ax == 27) {                // → L65
            if (p.W[0] > r9[0]) {                                // right push
                if (p.ag < 0) { p.ai = 0; p.ag = 0 }
                p.ak = r9[2] + (p.ak - p.W[0]) + 5
            }
        }
    }

    // -- L357 patrol (proven structure) ----------------------------------------
    private fun patrolArm(e: Entity, player: Entity, tail: BooleanArray) {
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
        // i.java:4165-4170 (proven): the crate/carrier ride helper runs
        // every patrol tick while not in hit-react — a moving crate feeds
        // its velocity into the soldier.
        if (e.S != 85) {
            crateRide11(e)
            val s = e.s
            if (s != null && s.ax == 51 && s.ag != 0) e.ag = s.ag
        }
        // L410-L436 flag/kill arms (i.java:6010-6040, proven) — runs
        // every patrol tick:
        //   `cq==true → r14=false` (a claim-held guard can't take sword
        //   hits); `cq==false → b(aS)` spot → `aA=1` (the real b(), no
        //   longer the zone+facing subset).
        if (e.cq) tail[2] = false
        else if (spotB(e, player, world)) e.aA = 1
        tail[3] = true                                // L415: r15 → k()
        // Z[14]∈{1,6,7} → `aE()→r13=false` — aE() always returns false
        // in the original (dead suppression, verbatim).
        if ((e.Z[14] == 1 || e.Z[14] == 6 || e.Z[14] == 7) &&
            killTouch(e, world, player)) tail[1] = false
        // L425-L435 context-kill offer: player mid-kill-anim (S297)
        // within 8px → marker `a(8)` + HELD-65568 → `i(2)` +
        // `aS.i(298)` + `at=this` + `G()` + `aS.c(this)`; S297 ≥8px
        // away → `G()`; prev-anim Q==297 → `G()`.
        if (player.S == 297) {
            if (Math.abs(e.ak - player.ak) >= 8) e.releaseAe()
            else {
                e.spawnMarker(world, 8, e.ak, e.al - 85)
                if (world.padDown(65568)) {
                    e.setAnim(2); player.setAnim(298)
                    Entity.at = e; e.releaseAe()
                    player.counteredBy(e, world)        // aS.c(at)
                }
            }
        } else if (player.Q == 297) e.releaseAe()
    }

    /** `i.aD()` (i.java:7167-7205, proven): ax11's crate/carrier ride
     *  helper — binds `s` to a Z[7]-linked ax51/ax43 overlapping entity
     *  (`k.q(Z[7])`), tracks its top (`al = s.W[1]+3`, `ak += s.ag>>8` on
     *  bind), plays `i(96)` while the crate is in S2, and when the cell
     *  beside the feet is open (`!h(cx,cy)` → `e() < 5`) and still
     *  patrolling (`aA != 0`) snaps to the edge with `i(22)/i(23)`.
     *  `!g.j && aA != 0 → Q()` — the grab-latch (g.j) suppresses the
     *  face-player flip while the wall sequence holds it (i.java:7180,
     *  a `g.j` reader). `Q()` = `av = aS.ak < ak` (i.java:6207). */
    fun crateRide11(e: Entity): Boolean {
        val s = e.s
        if (s == null) {
            if (e.Z[7] > 0) {
                val q = world.findByAw(e.Z[7])
                if (q != null && (q.ax == 51 || q.ax == 43) &&
                    Entity.overlapI(e.W, q.W)) {
                    e.s = q
                    if (e.S != 3) { e.setAnim(2); e.aA = 0 }
                    e.al = q.W[1] + 1
                    e.ak += q.ag shr 8
                }
            }
        } else if (s.ax == 51 || s.ax == 43) {
            if (s.ax == 51 && s.S == 2) {
                e.setAnim(96)
            } else if (Entity.overlapI(e.W, s.W)) {
                if (!Entity.grabLatch && e.aA != 0) {       // !g.j (:7180)
                    e.av = world.player.ak < e.ak           // Q() (:6207)
                }
                e.al = s.W[1] + 3
                val sideX = e.ak / 20 + (if (e.av) -1 else 1)
                if (e.e(world, sideX, e.al / 20) < 5 && e.aA != 0) {
                    if (e.ag > 0 && e.W[2] > s.W[2]) {
                        e.ak = s.W[2] - ((e.W[2] - e.W[0]) shr 1)
                        e.setAnim(if (e.av) 22 else 23)
                    } else if (e.ag < 0 && e.W[0] < s.W[0]) {
                        e.ak = s.W[0] + ((e.W[2] - e.W[0]) shr 1)
                        e.setAnim(23)                       // both arms 23
                    }
                }
            } else {
                e.s = null                                  // lost overlap
            }
        }
        return e.s != null
    }

    // -- L451 chase (i.java:5536-5562, proven core) -------------------------
    /** Returns false when the arm exits to L849 (`aG()` edge → drop
     *  alert), true when the chase continues at L457 (`r14` → j()). */
    private fun chaseArm(e: Entity, player: Entity): Boolean {
        facePlayer(e, player)                           // Q()
        if (edgeAhead(e)) {
            // aG() → k.aA=0 + aA=0 + i(k?3:2) → L849
            world.kAA = 0
            e.aA = 0
            e.setAnim(if (e.k) 3 else 2)
            return false
        }
        e.collideSides(world, true)                     // L457 a(true)
        e.ag = if (e.S == 4) (if (e.av) -2048 else 2048)
               else (if (e.av) -512 else 512)
        // a(aS.W,W) → af==null → aC=3 + i(23) attack windup (:5562)
        player.refreshBoxes()
        if (e.af == null && overlap(player.W, e.W)) {
            e.aC = 3
            e.setAnim(23)
        }
        // aC() subset: chase timeout — lose the player for 60 ticks → patrol
        e.P = e.P or 16
        if (e.aC > 0) e.aC--
        else if (!seesPlayer(e, player) && e.aA != 0) {
            e.aC = 60; e.aA = 0; e.k = false; e.setAnim(2)
        }
        return true
    }

    // ===========================================================    // ax10 — `i.aV()` TriggerController (i.java:11800-13181; semantics
    // reconstructed in docs/i-av-reconstruction.md). Level-0 carries 15
    // records using S ∈ {16,33,34,36,43,53}; this slice ports init L96
    // (i.java:2882) + the zone arms {33,34,36,43,53}. S16 (door-teleport,
    // L177d) ported in slice 99; the rope-attach reading in this comment
    // was wrong — the arm is a door/teleport pair, not a rope zone.

    /** ax22 record init Le87 (i.java:9339-9381, proven): `az=1`, Z stays
     *  the fresh `int[4]` (records with S-field `r8[5] != 0` leave it all
     *  zero); when `r8[5] == 0` the fill is `Z = {0, r8[4], r8[7], r8[11]}`.
     *  `P |= 512` → L1bea shared tail. W is NOT record-derived: the `I()`
     *  preamble's `b=1` + the L1f35 `if (b) t()` tail rebuild it from the
     *  clip-14 rect every tick ([-6,-10,34,33] around the anchor). */
    fun initAx22(e: Entity, f: List<Int>) {
        fun rf(i: Int) = if (i < f.size) f[i] else 0
        e.az = 1
        if (rf(5) == 0) {
            e.Z[1] = rf(4); e.Z[2] = rf(7); e.Z[3] = rf(11)
        }
        e.P = e.P or 512
    }

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
            // L59 (i.java:2119): Z = {r8[4], r8[12], r8[13], r8[14]} —
            // Z0 = spawn-S (gate selector), Z[1] = target uid, Z[2] = guard
            // uid, Z[3] = P-flag mask. NOTE: this arm does NOT run L111.
            2, 3 -> {
                e.Z[0] = rf(4); e.Z[1] = rf(12)
                e.Z[2] = rf(13); e.Z[3] = rf(14)
            }
            // L95 (i.java:2166): Z = {r8[11..15]} — likewise no L111 tail.
            10 -> {
                e.Z[0] = rf(11); e.Z[1] = rf(12); e.Z[2] = rf(13)
                e.Z[3] = rf(14); e.Z[4] = rf(15)
            }
            // L102 (i.java:2961): Z = {r8[11], r8[13]}; P |= 16
            34 -> {
                e.Z[0] = rf(11); e.Z[1] = rf(13)
                e.P = e.P or 16
            }
            // L104 (i.java:2975): Z = {0} — our Z is already zeroed
            43 -> e.Z[0] = 0
            // L110 (i.java:2986): Z = {r8[20]} → falls through to L111
            16 -> { e.Z[0] = rf(20); l111(e, f) }
            // i.java:2173 S11 arm: Z = {r8[0]} — single field, no L111.
            11 -> e.Z[0] = rf(0)
            // i.java:2187 S24 arm: aA = r8[11] — script uid only, no L111.
            24 -> e.aA = rf(11)
            // i.java:2190 S28 arm: Z = {0} — no L111.
            28 -> e.Z[0] = 0
            // i.java:2194 S30 arm: Z = {r8[12..19]} — no L111.
            30 -> {
                e.Z[0] = rf(12); e.Z[1] = rf(13); e.Z[2] = rf(14)
                e.Z[3] = rf(15); e.Z[4] = rf(16); e.Z[5] = rf(17)
                e.Z[6] = rf(18); e.Z[7] = rf(19)
            }
            // i.java:2205 S31 arm: Z = {r8[4], r8[11], r8[13], r8[14],
            // r8[15]} — Z0 flags, Z[1] lane-type nibble pack, Z[2] required
            // presses, Z[3] done-sentinel aA, Z[4] initial script uid.
            // No L111 tail.
            31 -> {
                e.Z[0] = rf(4); e.Z[1] = rf(11)
                e.Z[2] = rf(13); e.Z[3] = rf(14); e.Z[4] = rf(15)
            }
            // i.java:2219 S39 arm: P&32==0 → P|=16 — no L111.
            39 -> { if (e.P and 32 == 0) e.P = e.P or 16 }
            // switch default → L111 (covers S0/S1/S33/S36/S53 and every
            // unlisted S)
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
     *   `ag=ah=0`, `k.c(this)` remove. (g.D set by the S90 launch arm.)
     * - S16 (L177d→L1850): door-teleport — `bh()` exit + `bi()` arrival
     *   driving `k.B(26)`/`k.C(26)` fades. (The slice-20 "rope-attach"
     *   stub label was wrong — this is the paired-door transition.)
     */
    fun tickTrigger(e: Entity, w: LevelCellSource, player: Entity, pad: Pad) {
        when (e.S) {
            // `aV()` S0 arm (i.java:9772-9800 L5ac-L5e0, proven) — camera
            // focus zone: while the player overlaps a visible zone the
            // tracker reads k.af/k.ag as focus offsets (camA = ak-200+af,
            // camB = al-120+ag; k.java:1959-1965). Params are sticky when
            // zero: `p==0 → L5d1` skips the af write (keeps prior value),
            // `aG==0 → L1ec7` bare-returns keeping ag. A non-overlapping or
            // invisible zone clears BOTH globals — verbatim quirk: with
            // several S0 zones the last-ticked loser wins the wipe.
            0 -> {
                if (rectsOverlap(player.W, e.W) && e.wasHitRecently(w)) {
                    if (e.pv != 0) w.camAf = e.pv                    // L5c7
                    if (e.aG == 0) return                          // → L1ec7
                    w.camAg = e.aG                                 // L5d7
                    return
                }
                w.camAf = 0; w.camAg = 0                           // L5e0
            }
            // `aV()` S2 arm (i.java:11834-11976 L13a1-L14a2, proven) —
            // one-shot flag-apply trigger. Gates: Z0∈{1,2} → player must
            // overlap; Z0==2 → additionally the Z[2]-uid guard must be
            // gone (null or off-screen `!v()`). Then the Z[1]-uid target
            // gets `P |= Z[3]` (bit0 → `av=true` facing); if the target is
            // the player and `g.a` exists, bit-512 propagates to it. The
            // L1451 sweep then sets bit-512 on every ax11 soldier whose
            // linked `.s` already has it (squad arming). One-shot: the
            // zone removes itself via `k.c(this)` either way.
            2 -> {
                if (e.Z[0] == 1 || e.Z[0] == 2) {                  // L13b5
                    if (!rectsOverlap(player.W, e.W)) return       // → L1ec7
                }
                if (e.Z[0] == 2) {                                 // L13c5
                    if (e.Z[2] == 0) return                        // → L1ec7
                    val gate = w.findByAw(e.Z[2])                  // k.q(Z[2])
                    if (gate != null && gate.wasHitRecently(w)) return
                }
                val t = w.findByAw(e.Z[1])                         // L13ed k.q
                if (t != null) {
                    t.P = t.P or e.Z[3]                            // L13f5
                    if (t.P and 1 != 0) t.av = true                // L1412
                    if (t === player && player.ga != null) {       // L1418
                        if (e.Z[3] and 512 != 0)
                            player.ga!!.P = player.ga!!.P or 512   // L1432
                        else
                            player.ga!!.P = player.ga!!.P and 512.inv()
                    }                                              // L1443
                    for (b in w.npcs) {                            // L1451
                        if (b.ax != 11) continue
                        val bs = b.s ?: continue
                        if (bs.P and 512 == 0) continue
                        b.P = b.P or 512                           // L1496
                    }
                }
                w.removeEntity(e); return                          // L14a2
            }
            // `aV()` S3 arm (i.java:11975-12050 L14a7-L1526, proven) —
            // S2's mirror: same Z0 overlap gate and Z0==2 guard check,
            // then the Z[1]-uid target gets `P &= ~Z[3]` (flags CLEARED);
            // `t.ax==21` (mission director) also gets `P |= 16` — the
            // director "re-activate" bit. One-shot: self-removes.
            3 -> {
                if (e.Z[0] == 1 || e.Z[0] == 2) {                  // L14bb
                    if (!rectsOverlap(player.W, e.W)) return       // → L1ec7
                }
                if (e.Z[0] == 2) {                                 // L14cb
                    if (e.Z[2] == 0) return                        // → L1ec7
                    val gate = w.findByAw(e.Z[2])
                    if (gate != null && gate.wasHitRecently(w)) return
                }
                val t = w.findByAw(e.Z[1])                         // L14f3
                if (t != null) {
                    t.P = t.P and e.Z[3].inv()                     // L150a
                    if (t.ax == 21) t.P = t.P or 16                // L1520
                }
                w.removeEntity(e); return                          // L1526
            }
            // `aV()` small linked-entity triggers (proven):
            // S47 = L219: clears the global claim slot every tick (k.aQ
            // = the `i`-typed claim owner, NOT the Image aQ at k.java:79
            // — JADX letter collision).
            47 -> { w.kAQ = null; return }                         // L219
            // S48 = L166: when the player's tight rect overlaps, copy
            // `p` (record f13 = "boss speed") onto the o-uid target's
            // aG and remove. Debug print "Set Boss Speed=" omitted
            // (proven-dead dev trace).
            48 -> {
                val t = w.findByAw(e.oId) ?: return
                if (!rectsOverlap(player.Y, e.W)) return
                t.aG = e.pv                                        // L19d
                w.removeEntity(e); return                          // L1bb
            }
            // S49 = L1bc: overlap → linked entity `i(20)`, remove.
            49 -> {
                val t = w.findByAw(e.oId) ?: return
                if (!rectsOverlap(player.Y, e.W)) return
                t.setAnim(20)                                      // L1d8
                w.removeEntity(e); return                          // L1e0
            }
            // S54 = L136: overlap + linked entity still at S29 →
            // `i(30)`, remove. One-shot mission-step advance.
            54 -> {
                val t = w.findByAw(e.oId) ?: return                // L165
                if (t.S != 29) return
                if (!rectsOverlap(player.Y, e.W)) return
                t.setAnim(30)
                w.removeEntity(e); return
            }
            // `aV()` S10 arm (i.java:9480-9772 L318-L5a8, proven) — the
            // scripted wall-climb/column sequence: while the player's top
            // sits inside the band `dy = aS.W[1]-W[3] ∈ [Z[0],Z[1]]` it
            // locks input (k.o()), parks a clip-74 hand card at view
            // center, and on pad-mask-1 press OR finger-on-card (V())
            // arms the climb (`i.bB`, `i.bi`, player S4, az=199). Outside
            // the band: overlapping + unarmed → abort (marker 71, i.be
            // latch, player S34); overlapping + armed with `i.bF` inside
            // `[Z[2],Z[3]]` → grip marker 35 tracked above the head; not
            // overlapping → the L4b2 "climb finished" reset (S28, bC/bD
            // off, bF=100, bE=Z[4], sfx 25); `dy<0` (player above) → the
            // zone self-removes + exit anim S27 + k.p() unlock.
            10 -> {
                if (w.iBe) { w.removeEntity(e); return }              // L318
                val dy = player.W[1] - e.W[3]                         // L323
                // `dy<=Z[1] → L3e7; dy>=Z[0] → L3e7` (proven smali):
                // in-band is Z[1] < dy < Z[0] — records carry
                // Z[0]>Z[1] (level1 {180,50,…}), so an inclusive
                // `Z[0]<=dy<=Z[1]` read would be unsatisfiable.
                if (dy > e.Z[1] && dy < e.Z[0]) {                     // in band
                    if (w.iBB) return                                 // → L1ec7
                    e.lockInput(w)                                    // k.o()
                    val handUp = player.ae != null &&
                        player.ae!!.clip === w.clipFor(74) &&
                        player.ae!!.S == 0
                    if (!handUp) {                                    // L377
                        player.releaseAe()
                        player.spawnHand(w, w.kO + 200, w.kP + 120)
                    }
                    // L390
                    player.moveHand(w, w.kO + 200, w.kP + 120)
                    if (!pad.v(1) && !player.indicatorNearTouch(w))
                        return                                        // wait
                    // L3b3 — confirmed: arm the climb
                    player.releaseAe()
                    w.iBB = true; w.iBF = -1; w.iBG = -1
                    pad.clearLatches()                                // k.v()
                    if (player.S != 4) player.setAnim(4)
                    w.iBi = true
                    player.az = 199
                    return
                }
                // L3e7 — outside the band
                if (rectsOverlap(player.W, e.W)) {
                    if (!w.iBB || w.iBF < e.Z[2] || w.iBF > e.Z[3]) {
                        // L46b — abort: marker 71 + suppress + S34
                        player.releaseAe()
                        player.spawnAeMarker(w, 71,
                            player.ak, player.al - 85)
                        w.iBe = true
                        player.setAnim(34)
                        return
                    }
                    // L42c/L449 — marker 35 tracked above the head
                    if (player.ae == null || player.ae!!.S != 35) {
                        player.releaseAe()
                        player.spawnAeMarker(w, 35,
                            player.ak, player.al - 85)
                    }
                    player.ae!!.ak = player.ak
                    player.ae!!.al = player.al - 85
                    return
                }
                // L495 — not overlapping
                if (player.ae != null && player.ae!!.S == 39)
                    player.releaseAe()
                if (w.iBB && w.iBi && w.iBF == -1) {                  // L4b2
                    player.setAnim(28)
                    w.iBC = false; w.iBD = false; w.iBF = 100
                    w.iBE = e.Z[4]
                    w.sfx(25)
                }
                // L4e8
                if (dy > 0 && dy < e.Z[0]) {
                    if (w.iBF >= e.Z[2] && w.iBF <= e.Z[3]) {         // L525
                        if (player.ae == null || player.ae!!.S != 35) {
                            player.releaseAe()
                            player.spawnAeMarker(w, 35,
                                player.ak, player.al - 85)
                        }
                        player.ae!!.ak = player.ak                  // L542
                        player.ae!!.al = player.al - 85
                    }
                    return                                            // L563
                }
                // L564 — dy<=0 or dy>=Z[0]: sequence end
                if (player.ae != null) player.releaseAe()
                if (dy < 0) {                                         // L573
                    w.iBi = false
                    w.removeEntity(e)
                    if (player.S == 26 || player.S == 28 ||
                        player.S == 29) player.setAnim(27)            // L5a0
                }
                player.unlockInput(w)                                 // L5a8 k.p()
            }
            // `aV()` S46 arm (i.java:9338-9363 L1e1, proven) — wall-run
            // zone: sets `g.q`/`g.d` while the player overlaps; clears
            // both when this zone still owns the link after contact ends.
            // (Slice 137/138 called this arm "S10" — the real S10 is L318
            // above; L1e1 is the dispatch's `case 46` target.)
            46 -> {
                if (rectsOverlap(player.W, e.W)) {
                    Entity.gq = true; player.gd = e
                }
                if (!rectsOverlap(player.W, e.W) && player.gd === e) {
                    Entity.gq = false; player.gd = null
                }
            }
            // ---- `aV()` small-arm batch (all proven) -----------------
            // S4 = L15b9 (i.java:12121-12137): dialog-text zone —
            // overlap publishes `k.aB = k.d(1+k.aj, aF)` (level string)
            // + `k.aC = -1`; leaving clears `k.aB` when `aC <= 0`.
            4 -> {
                if (rectsOverlap(e.W, player.W)) {
                    w.kAB = w.levelString(1 + w.kAj, e.aF)          // L15b9
                    w.kAC = -1
                } else if (w.kAC <= 0) w.kAB = null                 // L15dd
                return
            }
            // S5 = L15e8 (i.java:12144-12167): auto-jump zone — overlap +
            // (fire `u(16388)`/`v(16388)` OR direction-held matching
            // facing: `av ? u(2) : u(8)`) → player `i(22)` rise anim.
            5 -> {
                if (rectsOverlap(e.W, player.W) &&
                    (w.padDown(16388) || pad.v(16388) ||
                     (player.av && w.padDown(2)) ||
                     (!player.av && w.padDown(8))))
                    player.setAnim(22)                              // L1625
                return
            }
            // S6/S7 = L162e/L1643 (i.java:12173-12194): persisted flag
            // zones — overlap writes `k.aZ = 1` / `k.aZ = 0` (save byte
            // 68 — event-done toggles).
            6 -> { if (rectsOverlap(e.W, player.W)) w.kAZ = true; return }
            7 -> { if (rectsOverlap(e.W, player.W)) w.kAZ = false; return }
            // S8 = L152b (i.java:12052-12076): alert-toggle zone —
            // overlap flips `i.bn` and self-removes; `bn` now true →
            // `i(0)` + `E()` + `i(79)` + `g.z=0`; now false → `i(80)` +
            // `g.z=1` + `k.v()`.
            8 -> {
                if (rectsOverlap(player.W, e.W)) {
                    w.iBn = !w.iBn                                  // L1546
                    w.removeEntity(e)
                    if (w.iBn) {
                        player.setAnim(0)                           // L1563
                        player.eSettle(w)                           // E()
                        player.setAnim(79)
                        player.z = false                            // g.z = 0
                    } else {
                        player.setAnim(80)                          // L157a
                        player.z = true                             // g.z = 1
                        w.clearLatches()                            // k.v()
                    }
                }
                return
            }
            // S9 = L1658, S19 = L15b8, S39 = L1a02: bare `return` arms —
            // no port (dead states; the dispatch records them verbatim
            // here for the table's completeness).
            // S12 = L175c (i.java:12309-12321): one-shot screen
            // transition `k.n(aF)` (cO/cP) + self-remove.
            12 -> {
                if (rectsOverlap(e.W, player.W)) {
                    w.kNSet(e.aF)                                   // k.n(aF)
                    w.removeEntity(e)
                }
                return
            }
            // S13 = L1778 (i.java:12322-12325): unconditional
            // `k.c(this)` — the "remove me" marker state.
            13 -> { w.removeEntity(e); return }
            // S14 = L1659-L1750 (i.java:12195-12308): assassination-
            // target release zone. Player's `af` must be a live ax69
            // (Z[0]∈{0,1,2}) whose rect overlaps the zone: zero the
            // target's velocities, `i(0)` + `P&=~64` + `E()` settle the
            // player, `G()` the target's link; Z[0]∈{1,2} pins `ak` to
            // the target's center-x; Z[0]==0 hurls the player
            // (`ag=∓3328` by `av`, `ah=-6656`, `i(243)`). Then `af=null`
            // + `k.c(this)`.
            14 -> {
                val t = player.af ?: return                         // L1659
                if (t.ax != 69) return
                if (t.Z[0] != 0 && t.Z[0] != 1 && t.Z[0] != 2) return
                if (!rectsOverlap(t.W, e.W)) return                 // L169c
                t.ah = 0; t.ag = 0
                player.setAnim(0)
                player.P = player.P and -65                         // ~64
                player.eSettle(w)                                   // E()
                t.releaseAe()                                       // af.G()
                if (t.Z[0] == 1 || t.Z[0] == 2)
                    player.ak = (t.W[0] + t.W[2]) shr 1             // L1704
                else {
                    player.ag = if (player.av) -3328 else 3328      // L1726
                    player.ah = -6656
                    player.setAnim(243)
                }
                player.af = null                                    // L1750
                w.removeEntity(e)
                return
            }
            // S18 = L1c98 (i.java:12952-13070): spawn-release zone —
            // overlap + `o`→ax4 with `S==33` and `aA∈{0,2}` → `P&=~32`,
            // `P&=~128`, `P|=16`; then spawn the target just offscreen
            // on its side of the player (`ak>player.ak` → right:
            // `ag=-p`, `av=1`, `ak=camR+width`; else `ag=p`, `av=0`,
            // `ak=camL-width`), `al=camT+70`, `aA=1`.
            18 -> {
                if (!rectsOverlap(player.W, e.W)) return            // L1c98
                if (e.oId == -1) return
                val t = w.findByAw(e.oId) ?: return                 // L1cb9
                if (t.ax != 4 || t.S != 33) return                  // L1cd2
                if (t.aA != 0 && t.aA != 2) return                  // L1cdb
                t.P = t.P and -33; t.P = t.P and -129
                t.P = t.P or 16                                     // L1cff
                if (t.ak > player.ak) {
                    t.ag = -t.pv; t.av = true                       // L1d1e
                    t.ak = w.camRect[2] + (t.W[2] - t.W[0])         // L1d2c
                } else {
                    t.ag = t.pv; t.av = false                       // L1d47
                    t.ak = w.camRect[0] - (t.W[2] - t.W[0])         // L1d5c
                }
                t.al = w.camRect[1] + 70                            // L1d68
                t.aA = 1                                            // L1d6f
                return
            }
            // S21 = L1855 (i.java:12432-12456): checkpoint-write zone —
            // overlap → `k.b(8, 1+k.aj, aF, p)` (mark map region);
            // true → `k.l(21)` dialog screen; then `k.x = 48` +
            // self-remove regardless.
            21 -> {
                if (rectsOverlap(player.W, e.W)) {
                    if (w.kBMark(8, 1 + w.kAj, e.aF, e.pv))
                        w.screenL(21)                               // L187f
                    w.kX = 48
                    w.removeEntity(e)
                }
                return
            }
            // S22 = L157d (i.java:12090-12117): checkpoint-audio zone —
            // overlap → `aF<0 → k.w()` (e.b() stop); `aF==2 → k.z(2)`;
            // else `k.A(aF)` = `z(aF)` (the ==2 branch is verbatim
            // redundant — `A` delegates to `z`). Then `k.c(this)`.
            22 -> {
                if (rectsOverlap(e.W, player.W)) {
                    if (e.aF < 0) w.audioStop()                     // k.w()
                    else w.audioTrackPlay(e.aF)                     // z/A(aF)
                    w.removeEntity(e)
                }
                return
            }
            // S23 = L1889 (i.java:12457-12489): linked-entity watch —
            // `o` uid → `k.q`; ax11 + rect-overlap → `cq = alive ? 1:0`
            // (`P()` side-effects the dead release); else `cq = 0`;
            // `o == -1` or link missing → return.
            23 -> {
                if (e.oId == -1) return                             // L1889
                val t = w.findByAw(e.oId) ?: return                 // L18c7
                if (t.ax == 11 && rectsOverlap(t.W, e.W))
                    t.cq = !t.deadRelease()                         // P()→cq
                else t.cq = false
                return
            }
            // `aV()` S17 arm (i.java:12794-12951 L1b44-L1c92, proven;
            // dispatch `case 17: goto L1b44` i.java:9182) — balance
            // zone. `aA` phases: 0 → wait for overlap (claim-busy freezes
            // while centered); on overlap center-pins the player
            // (`i(297)`, `ak/al` = zone center, all velocities 0);
            // 1 → while centered and player S∉{298,293} `v(2)`/`v(8)`
            // side-leaps (`i(19)`, `ag=∓3328`, `ah=-3840`, `av`) or
            // `v(33024)` jump-up (`a(2560)` airborne fling) → `aA=2`;
            // leaving the rect also → `aA=2`; 2 → `aZ || g.a` (landed
            // or grabbed) resets `aA=0`.
            17 -> {
                if (w.kC != null && w.kC!!.claimAb() && e.aA == 1)
                    return                                          // L1b44
                if (e.aA == 0) {
                    if (rectsOverlap(player.W, e.W)) {              // entry
                        e.aA = 1
                        player.setAnim(297)
                        player.ak = (e.W[0] + e.W[2]) shr 1         // pin x
                        player.al = (e.W[1] + e.W[3]) shr 1         // pin y
                        player.ah = 0; player.ag = 0
                        player.aj = 0; player.ai = 0                // L1b9a
                    }
                } else if (e.aA == 1 && !rectsOverlap(player.W, e.W))
                    e.aA = 2                                        // L1bca
                if (e.aA == 1 && player.S != 298 && player.S != 293) {
                    when {                                          // L1be7
                        pad.v(2) -> { player.setAnim(19)
                            player.ag = -3328; player.ah = -3840
                            player.av = true; e.aA = 2; return }
                        pad.v(8) -> { player.setAnim(19)
                            player.ag = 3328; player.ah = -3840
                            player.av = false; e.aA = 2; return }
                        pad.v(33024) -> { player.flingAirborne(2560, w)
                            e.aA = 2; return }
                    }
                }
                if (e.aA == 2 && (player.aZ || player.ga != null))
                    e.aA = 0                                        // L1c7b
                return
            }
            // `aV()` S24 arm (i.java:9361-9467 L21e-L313, proven;
            // dispatch `case 24: goto L21e` i.java:9193) — rope/grab
            // trigger zone. Eligible = overlap + player not already
            // bound to this zone + player.S ∉ {291,270,271,90,89,43}
            // + `!ae()` (no live ax 17/11/23/50/73 humanoid near the
            // player — `enemiesAlert`). Eligible + `v(16388)` press →
            // bind `aS.af=e`, `i(0)`, settle `i.E()`, zero velocities,
            // `i(267)` grab pose, `G()`, clear latches. Eligible without
            // press + record `aA==1` → keep marker-7 pinned at
            // (ak, al-15). Ineligible → `G()` drops the marker link.
            // Same mechanics as ax27's S0 arm (`tickAx27`) but owned by
            // an ax10 zone with marker offset al-15 (vs al-85).
            24 -> {
                val eligible = player.af !== e &&
                    player.S != 291 && player.S != 270 && player.S != 271 &&
                    player.S != 90 && player.S != 89 && player.S != 43 &&
                    rectsOverlap(player.W, e.W) && !enemiesAlert(w, player)
                if (!eligible) {                               // L313
                    e.releaseAe()                              // G()
                    return
                }
                if (pad.v(16388)) {                            // L288 press
                    player.af = e                              // aS.af = e
                    player.setAnim(0)                          // i(0)
                    player.eSettle(w)                          // E()
                    player.ag = 0                              // zero vel
                    player.ah = 0
                    player.setAnim(267)                        // i(267)
                    e.releaseAe()                              // G()
                    w.clearLatches()                           // k.v()
                    return
                }
                if (e.aA == 1) {                               // L2c9 marker
                    if (e.ae == null || e.ae!!.S != 7) {
                        e.releaseAe()                          // G()
                        e.spawnMarker(w, 7, e.ak, e.al - 15)   // a(7,…)
                    }
                    e.ae!!.ak = e.ak                           // pin
                    e.ae!!.al = e.al - 15
                }
                return
            }
            // `aV()` S30 arm (i.java:10427-11736 La72-L12e3, proven;
            // dispatch `case 30: goto La72` i.java:9199) — pursuer-pool
            // wave spawner. While the player overlaps: `P|=16`;
            // `Z[6]==3` forces the infinite-wave config (Z[1]=-1, Z[2]=3);
            // `pv=Z[1]` rows (-1→1), `aG=Z[2]` cols.
            // First tick (`cr==null`): allocate the `pv×aG` grid; each
            // member gets `aw=5000+row*pv+col`, `au=0`, fresh `Z(22)`,
            // flavor by `Z[6]` (0→ax11 `i(4)`/`bu[au]`; 1→ax17 `i(59)`/
            // `bu[au]`; 2→ax23 `i(71)`/`bv[au]`; 3→ax11 `i(4)`/`bu[au]`
            // + col0→Z[0]=1 engaged / col≥1→0, col0→zone Z[5]=1 spawn
            // side / col≥1→0, `aC=Z[7]`); then the Lcb7 common tail
            // (`az=100`, `P|=16`, `Z[1]=0`, `Z[2]=-1`, `Z[14]=0`,
            // `av=zone Z[5]!=0`, `Z[15]=-160`, `Z[16]=-64`, `Z[17]=320`,
            // `Z[18]=100`); row-0 members only get position+registration
            // (`aq=Z[3]+k.O`, `ar=Z[4]`, `ak` offscreen ±`20*(col+1)`
            // by side, `al=Z[4]`, `ag=ah=0`, `Z[3]=ak`, `Z[4]=al`,
            // `k.b()`); ends `aA=0`.
            // Later ticks (`cr!=null`): `Z[1]!=-1 && aT()` (all dead) →
            // clear the linked uid `k.q(Z[0])` (`P&=~32`; `bi[ax]!=-1` →
            // `P&=~128`), `k.c(this)`, `aS()`. Z[6]==3 → per dead member
            // respawn (cooldown `aC`: `aC>0` skips + `aC--`; respawned
            // member `Z[0]=1` iff no other engaged member; same spawn
            // tail + `aC=Z[7]` reload). Else — finite pool: current row
            // wiped → `Z[1]==-1` → `aS()` + re-enter `aV()` (`goto L0`);
            // else `aA++` (≥pv → done) and re-position the new row
            // (also zeroes `ai/aj`) + `k.b()`.
            30 -> {
                while (true) {
                    if (!rectsOverlap(e.W, player.W)) return        // La72
                    e.P = e.P or 16
                    if (e.Z[6] == 3) { e.Z[1] = -1; e.Z[2] = 3 }
                    e.pv = e.Z[1]; e.aG = e.Z[2]                     // p/aG
                    if (e.pv == -1) e.pv = 1
                    else if (e.pv <= 0 || e.aG <= 0) return          // Lad9
                    if (e.cr == null) {
                        e.cr = Array(e.pv) { Array(e.aG) { Entity(0, null) } }
                        for (r9 in 0 until e.pv) for (r8 in 0 until e.aG) {
                            // `new i()` + field writes — ctor carries the
                            // flavor's ax/clip since `ax` is immutable.
                            val m = when (e.Z[6]) {
                                0 -> Entity(11, w.clipFor(7)).also {
                                       it.setAnim(4)
                                       it.aB = Entity.WEAPON_DMG[w.weaponSlot] }
                                1 -> Entity(17, w.clipFor(7)).also {
                                       it.setAnim(59)
                                       it.aB = Entity.WEAPON_DMG[w.weaponSlot] }
                                2 -> Entity(23, w.clipFor(7)).also {
                                       it.setAnim(71)
                                       it.aB = Entity.NPC_HP_BV[w.weaponSlot] }
                                3 -> Entity(11, w.clipFor(7)).also {
                                       it.setAnim(4)
                                       it.aB = Entity.WEAPON_DMG[w.weaponSlot]
                                       it.Z[0] = if (r8 != 0) 0 else 1  // Lc88
                                       e.Z[5] = if (r8 >= 1) 0 else 1   // Lc96
                                       e.aC = e.Z[7] }                  // Lcac
                                else -> Entity(0, null)                 // Lcb7
                            }
                            e.cr!![r9][r8] = m
                            m.aw = 5000 + r9 * e.pv + r8             // uid
                            m.au = 0
                            // `Z = new int[22]` — fresh Entity already
                            // carries a zeroed IntArray(22).
                            // ---- Lcb7 common tail ----
                            m.az = 100
                            m.P = m.P or 16
                            m.Z[1] = 0; m.Z[2] = -1; m.Z[14] = 0
                            m.av = e.Z[5] != 0                       // Ld16
                            m.Z[15] = -160; m.Z[16] = -64
                            m.Z[17] = 320; m.Z[18] = 100
                            if (r9 == 0) {                           // row-0 only
                                m.aq = e.Z[3] + w.kO                 // Lda0
                                m.ar = e.Z[4]
                                m.ak = if (e.Z[5] == 0)              // Lda7
                                    w.kO - 20 * (r8 + 1)
                                else w.kO + 400 + 20 * (r8 + 1)
                                m.al = e.Z[4]
                                m.ag = 0; m.ah = 0
                                m.Z[3] = m.ak; m.Z[4] = m.al         // Ldc0
                                w.queueInsert(m)                     // k.b()
                            }
                        }
                        e.aA = 0                                     // Le30
                        return
                    }
                    // ---- steady state: cr != null (Le36) ----
                    if (e.Z[1] != -1 && e.poolAllDead()) {           // aT()
                        val t = w.findByAw(e.Z[0])                   // k.q(Z[0])
                        if (t != null) {
                            t.P = t.P and -33                        // P&=~32
                            if (Entity.AX_CLIP_BI[t.ax] != -1)
                                t.P = t.P and -129                   // P&=~128
                        }
                        w.removeEntity(e)                            // k.c()
                        e.poolDrain()                                // aS()
                        return
                    }
                    if (e.Z[6] == 3) {
                        // ---- pursuer-wave respawn (Le8e) ----
                        var r9 = 0
                        while (e.aC <= 0 && r9 < e.aG) {
                            val row = e.cr!![e.aA]
                            if (row[r9].deadRelease()) {             // P()
                                var engaged = false                  // r8
                                for (r10 in 0 until e.aG)
                                    if (r10 != r9 && row[r10].Z[0] != 0) {
                                        engaged = true; break
                                    }
                                val m = Entity(11, w.clipFor(7))
                                row[r9] = m
                                m.aw = 5000 + e.aA * e.pv + r9
                                m.au = 0
                                m.setAnim(4)
                                m.aB = Entity.WEAPON_DMG[w.weaponSlot]
                                m.Z[0] = if (engaged) 0 else 1       // Lf90
                                e.Z[5] = if (r9 >= 1) 0 else 1       // Lfa1
                                m.az = 100
                                m.P = m.P or 16
                                m.Z[1] = 0; m.Z[2] = -1; m.Z[14] = 0
                                m.av = e.Z[5] != 0
                                m.Z[15] = -160; m.Z[16] = -64
                                m.Z[17] = 320; m.Z[18] = 100
                                m.aq = e.Z[3] + w.kO
                                m.ar = e.Z[4]
                                m.ak = if (e.Z[5] == 0)
                                    w.kO - 20 * (r9 + 1)
                                else w.kO + 400 + 20 * (r9 + 1)
                                m.al = e.Z[4]
                                m.ag = 0; m.ah = 0
                                m.Z[3] = m.ak; m.Z[4] = m.al
                                w.queueInsert(m)                     // k.b()
                                e.aC = e.Z[7]                        // reload
                            }
                            r9++
                        }
                        if (e.aC > 0) e.aC--                         // L1173
                        return
                    }
                    // ---- finite pool (L1185): row wipe → advance ----
                    val row = e.cr!![e.aA]
                    for (m in row) if (!m.deadRelease()) return      // L11a1
                    if (e.Z[1] == -1) { e.poolDrain(); continue }    // → L0
                    e.aA++
                    if (e.aA >= e.pv) return                         // L1ec7
                    for (r9 in 0 until e.aG) {                       // L11cf
                        val m = e.cr!![e.aA][r9]
                        m.aq = e.Z[3] + w.kO
                        m.ar = e.Z[4]
                        m.ak = if (e.Z[5] == 0)
                            w.kO - 20 * (r9 + 1)
                        else w.kO + 400 + 20 * (r9 + 1)
                        m.al = e.Z[4]
                        m.ag = 0; m.ah = 0; m.ai = 0; m.aj = 0
                        m.Z[3] = m.ak; m.Z[4] = m.al
                        w.queueInsert(m)
                    }
                    return                                           // L12e3
                }
            }
            // S28 = L1aa7 (i.java:12720-12793): grab-guard zone —
            // enter: `g.g==null && (aA&8)==0 && overlap && Z[0]==0` →
            // `aA|=8`, `g.z=0`, `Z[0]=1`; `i.bn` → `az=-1`. Exit:
            // `aA&8 && !overlap && Z[0]==1` → `aA&=~8`, `g.z=1`,
            // `Z[0]=0`; `i.bn` → `az=100`. (First tick skips the exit
            // arm via `goto L1be7` — folded in by ordering.)
            28 -> {
                if (player.gg == null && (player.aA and 8) == 0 &&
                    rectsOverlap(player.W, e.W) && e.Z[0] == 0) {
                    player.aA = player.aA or 8                        // L1aba
                    player.z = false                                // g.z = 0
                    e.Z[0] = 1
                    if (w.iBn) player.az = -1                       // L1b0b
                }
                if ((player.aA and 8) != 0 && !rectsOverlap(player.W, e.W)
                    && e.Z[0] == 1) {
                    player.aA = player.aA and -9                    // ~8
                    player.z = true                                 // g.z = 1
                    e.Z[0] = 0
                    if (w.iBn) player.az = 100                      // L1b71
                }
                return
            }
            // S29 = L12e4 (i.java:11732-11833): release-zone — waits on
            // the `o`-linked group: `k.q(o)` → null or member with
            // neither P32 nor P128 → `k.c(this)`; else set P32|P128,
            // clear P16; `p`/`aG`/`ay` uids (skip -1) must each resolve
            // to an entity with `at()` done (else return — keep
            // waiting); then `P&=~32`, `G()`, `bi[ax]!=-1 → P&=~128`,
            // `k.c(this)`.
            29 -> {
                val t = if (e.oId == -1) null else w.findByAw(e.oId)  // L12fd
                if (t == null || ((t.P and 32) == 0 && (t.P and 128) == 0)) {
                    w.removeEntity(e); return                        // L1314
                }
                t.P = t.P or 32 or 128                               // L1330
                t.P = t.P and -17                                    // ~16
                // L1395-L14c0: pv/aG/ay member uids must all be done.
                for (uid in intArrayOf(e.pv, e.aG, e.ay)) {
                    if (uid == -1) continue
                    val m = w.findByAw(uid)
                    if (m != null && !m.atDone()) return
                }
                t.P = t.P and -33                                    // ~32 L1434
                t.releaseAe()                                        // G()
                if (Entity.AX_CLIP_BI[t.ax] != -1) t.P = t.P and -129// ~128
                w.removeEntity(e); return                            // L14cd
            }
            // `aV()` S31 arm (i.java:9804-10466 L5ea-La66, proven;
            // dispatch `case 31: goto L5ea` i.java:9200) — the
            // claim-QTE zone: 4 pad lanes from Z[1] nibbles (types index
            // i.cs[]/i.ct[]), `aB` ticks vs Z[2] (required presses),
            // `aA` = bound script uid (Z[4]→Z[3] when the sequence ends),
            // bA[] prompt cards (clip9 keys when `k.k()`, clip74 touch
            // zones otherwise). `m` lane cursor; `m>=10` = lane resolved.
            // `n` is set externally (claim script) → the next overlap tick
            // runs the consumed reset (P|=8192) and the zone removes itself.
            31 -> {
                if (w.iBe) { w.removeEntity(e); return }               // L5ea
                if (e.claimAb()) { e.runClaimScript(w); return }       // L5f5
                if (e.P and 8192 != 0) { w.removeEntity(e); return }   // L601
                e.P = e.P or 128                                       // L611
                if (!rectsOverlap(e.W, player.W)) {
                    Entity.gE = false; return
                }
                val kc = w.kC                                          // L632
                if (kc != null && kc.claimAb()) {
                    kc.releaseClaim(w)
                    w.removeEntity(kc)
                    w.kC = null
                }
                e.P = e.P and -129                                     // L651
                Entity.gE = true
                if (e.Z[0] != 0) Entity.icu = true
                if (e.aB != 0 && e.nl != 0) {                          // L66e
                    e.P = e.P or 8192                                  // consumed
                    e.aB = 0; e.nl = 0
                    e.X.fill(0)   // X=null → lazy realloc; the port's
                                  // fixed IntArray(4) gets refilled from
                                  // Z[1] on the next armed pass
                    Entity.scriptPrompts.fill(null)
                    Entity.gE = false
                    if (e.Z[0] != 0) Entity.icu = false
                    if (e.aA > 0) {                                    // L6bb
                        e.bindScript(w.kS(e.aA), w)
                        e.P = e.P or 512 or 16
                        w.kC = e
                        e.bindScript(w.kS(e.aA), w)   // verbatim double h()
                        e.scriptKeyStep(w.kS(e.aA), w)
                        e.P = e.P or 128
                    } else if (w.missionBh() == 3) {
                        w.removeEntity(e)                              // L70d
                    }
                    if (w.missionBh() == 3) {                          // L71c
                        pad.y(65568); e.unlockInput(w); e.timewarpOff(w)
                    } else {
                        pad.clearLatches()                             // L733
                    }
                    return
                }
                if (e.nl != 0) return                                  // L737
                if (w.missionBh() == 3 &&
                    (w.iAH || !w.kAm)) {                               // L73f
                    e.timewarp(w, 2); e.lockInput(w)                   // L756
                }
                if (e.aB == 0) {                                       // L75d
                    if (w.missionBh() == 3) pad.y(65568)
                    else pad.clearLatches()
                    // L77a — arm the lane sequence
                    e.P = e.P and -129
                    e.P = e.P or 16
                    e.aA = e.Z[4]; e.j = 0; e.pv = 0
                    e.aD = 0; e.az = 300
                    e.m = 0                                            // L7bf
                    for (r9 in 0..3) {                                 // L7c6
                        e.X[r9] = (e.Z[1] shr ((3 - r9) shl 2)) and 15
                        if (r9 != 3 && e.m == 0 && e.X[r9] == 0) continue
                        e.m = 1; e.aD++                                // L7f5
                        var pr = Entity.scriptPrompts[r9]
                        if (pr == null) {
                            pr = ScriptPrompt()
                            Entity.scriptPrompts[r9] = pr
                        }
                        if (w.mounted) {                               // L839
                            pr.attach(9, w.clipFor(9))
                            pr.setState(Entity.CT[e.X[r9]], -1)
                        } else {                                       // L818
                            pr.attach(74, w.clipFor(74))
                            pr.setState(0, -1)
                        }
                    }
                    e.m = 0; e.nl = 0                                  // L860
                    while (e.pv == 0 && e.j < 4) {                     // L86a
                        e.pv = e.X[e.j]; e.j++
                    }
                    e.m = e.j - 1; e.aE = e.m                          // L893
                }
                // L8a5 — lane scan
                if (e.aB >= e.Z[2] || e.m >= 10 || e.aA == e.Z[3]) {
                    // La1f — sequence complete / lane timed out
                    if (e.aB < e.Z[2] || e.m >= 10) return
                    val pr = Entity.scriptPrompts[e.m] ?: return
                    if (w.mounted) pr.setState(Entity.CT[e.X[e.m]] + 2, 1)
                    else pr.setState(-1, 1)
                    e.m = 10 + e.m                                     // La66
                    return
                }
                e.aB++                                                 // progress
                val pr0 = Entity.scriptPrompts[e.m]
                if (!w.mounted && pr0 != null && pr0.e != -1 &&
                    w.pointerMoveIn(pr0.a - 35, pr0.b - 35, 70, 70)) {
                    pr0.setState(1, 1)                                 // hover
                }
                val hit = pad.v(Entity.CS[e.pv]) ||                    // L91c
                    (!w.mounted && pr0 != null &&
                        w.pointerDownIn(pr0.a - 35, pr0.b - 35, 70, 70))
                if (hit) {                                             // L956
                    if (pr0 != null) {
                        if (w.mounted) pr0.setState(Entity.CT[e.X[e.m]] + 1, 1)
                        else pr0.setState(-1, 1)
                    }
                    if (e.j > 3) {                                     // L987
                        e.aA = e.Z[3]; w.sfx(25); return
                    }
                    e.m = e.j; e.pv = e.X[e.j]; e.j++                  // L99f
                    return
                }
                val stray = if (w.mounted)                             // L9bf
                    w.pointerStrip() || pad.bB != 0                    // k.t()
                else w.pointerStrip()
                if (stray) {
                    if (e.aE == e.m) return                            // L9d7
                    if (pr0 != null) {
                        if (w.mounted) pr0.setState(Entity.CT[e.X[e.m]] + 2, 1)
                        else pr0.setState(-1, 1)
                    }
                    e.m = 10 + e.m                                     // La13
                }
            }
            // `aV()` S55 arm (i.java:9227-9253 Lf4, proven; dispatch
            // `case 55: goto Lf4` i.java:9224) — boss-reposition zone:
            // when the bound boss (k.aU — ax29 binds itself at init,
            // i.java:2468 `sArr[5]!=30 → k.aU=this`) sits in S13 and its
            // interact-rect `Y` overlaps this zone's `W`, force anim 25,
            // snap boss `ak` to the zone's `ak`, and zero both velocities.
            // The `aB`=800 HP pool is untouched.
            55 -> {
                val boss = w.kAU ?: return                            // k.aU
                if (boss.S != 13) return                              // Lf4 gate
                if (!rectsOverlap(boss.Y, e.W)) return                // a(aU.Y, W)
                boss.setAnim(25)                                      // aU.i(25)
                boss.ak = e.ak                                        // aU.ak ← zone x
                boss.ah = 0                                           // zero velocities
                boss.ag = 0
                return
            }
            // S32 = L18d1 (i.java:12477-12520): balance-hold zone —
            // enter: overlap + `g.ac==null` → `g.ac=this`, `i(38)`,
            // `k.v()` clear latches. Exit: `!overlap && ac==this` →
            // `ac=null`. While bound (`ac==this`): arc `al` toward the
            // zone's center — `al = cy + 10*(halfW-|dx|)/halfW`,
            // `ah = 0`.
            32 -> {
                if (rectsOverlap(player.W, e.W)) {
                    if (player.ac == null) {
                        player.bindAc(e)                               // aS.a(r7) — bind + P|256
                        player.setAnim(38)                             // i(38)
                        w.clearLatches()                               // k.v()
                    }
                } else if (player.ac === e) player.bindAc(null)        // aS.a(null) — unbind + P&~256
                if (player.ac === e) {
                    val dx = Math.abs(player.ak - ((e.W[0] + e.W[2]) shr 1))
                    val hw = (e.W[2] - e.W[0]) shr 1
                    player.al = ((e.W[1] + e.W[3]) shr 1) +
                        (10 * (hw - dx)) / hw                          // L1a02
                    player.ah = 0
                }
                return
            }
            // S41 = L1a03 (i.java:12647-12671): `g.a` driver zone —
            // player's held entity `ga` is ax51 and overlaps → remove.
            41 -> {
                val ga = player.ga ?: return
                if (ga.ax != 51 || !rectsOverlap(ga.W, e.W)) return
                w.removeEntity(e); return
            }
            // S42 = L1d75 (i.java:13074): overlap → `k.c(this)`
            // (one-shot remove trigger).
            42 -> { if (rectsOverlap(player.W, e.W)) w.removeEntity(e); return }
            // S44 = L1a59 (i.java:12683-12696): meter-write zone —
            // overlap → `k.W = k.V - aE`, remove.
            44 -> {
                if (rectsOverlap(player.W, e.W)) {
                    w.kW = w.kV - e.aE                                 // L1a67
                    w.removeEntity(e)
                }
                return
            }
            // S45 = L1a79 (i.java:12698-12718): hurt-prop zone —
            // overlap → `bh[k.aj]==3` → player `i(34)` (hit react);
            // else `k.l(12)` mission-fail + remove.
            45 -> {
                if (rectsOverlap(player.W, e.W)) {
                    if (w.missionBh() == 3) player.setAnim(34)           // L1a97
                    else { w.screenL(12); w.removeEntity(e) }          // L1aad
                }
                return
            }
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
            // `aU()` case-34 (i.java:9031-9139, proven): slope-rail ride.
            // The original runs this inside i.a(Graphics) — per-frame,
            // i.e. the same cadence as our tick — so the mutating part
            // lives here; the rail LINE draws in the renderer. `aV()`'s
            // own S34 tick arm is genuinely empty (the earlier comment
            // was right) — aU() is where the S34 work happens.
            34 -> {
                e.P = e.P or 16
                val wa = e.W
                val fwd = e.Z[0] == 0
                val free = player.af == null || player.af === e
                val near = player.S != 9 &&
                    ((player.af === e &&
                      player.ak > wa[0] && player.ak < wa[2]) ||
                     rectsOverlap(player.W, wa))
                if (near) {
                    val inside = player.ak >= wa[0] && player.ak <= wa[2]
                    val slope = ((wa[3] - wa[1]) shl 8) / (wa[2] - wa[0])
                    val ry = wa[1] + (if (fwd)
                        (slope * (player.ak - wa[0])) shr 8
                    else
                        (slope * (wa[2] - player.ak)) shr 8)
                    if (free && inside &&
                        player.W[1] >= ry - 20 && player.W[1] <= ry + 30) {
                        player.af = e
                        player.al = ry + 10 + 65          // i13+=10; al=i13+65
                        player.ah = (slope * 1024) shr 8
                        player.av = !fwd
                        player.ag = if (fwd) 2560 else -2560
                        if (player.S == 50 || player.gt != 0) player.al += 40
                        else if (player.S != 164) player.setAnim(164)
                        if (w.padHeld(33024)) {           // jump-off
                            player.af = null; player.al += 40
                            player.probeCells(world)      // aS.x()
                            if (player.aR <= 20) player.setAnim(43)
                            else { player.al -= 40
                                   player.settleToGround(world) }
                        } else if (w.padHeld(16388) ||
                                   (fwd && w.padHeld(8)) ||
                                   (!fwd && w.padHeld(2))) {  // attack-off
                            player.af = null; player.al -= 40
                            player.setAnim(157)
                            player.ag = if (fwd) 8192 else -8192
                            player.ah = -2560
                            player.probeCells(world)
                            if (player.aR <= 20) player.setAnim(157)
                            else { player.ah = 0; player.ag = 0
                                   player.settleToGround(world) }
                        }
                    } else if (player.af === e && player.S == 164) {
                        player.af = null; player.setAnim(43)
                        player.ag = 0; player.ah = 0
                    }
                } else if (free) {
                    if (player.af === e && player.S == 164) {
                        if (e.Z[1] == 1) {                // dismount-jump
                            player.al -= 40; player.setAnim(157)
                            player.ag = if (fwd) 8192 else -8192
                            player.ah = -2560
                        } else {
                            player.setAnim(43); player.ag = 0; player.ah = 0
                        }
                    }
                    player.af = null
                }
            }
            // S16 door-teleport (i.java:12326-12431 = L177d→L1850,
            // proven). `g.a != null` gate; `az=300`; `r8 = k.q(Z[0])`
            // = the destination-door entity. Bound player (`aS.ac ==
            // this`) → `bi()` arrival when fades idle, or the
            // `ao && bI > 13` mid-fade-out arm `r8.i(19)` + `aS.a(0)`
            // fling. Unbound: `o == -1` no-op; overlap + `g.g == null`
            // → `aS.a(105,cx,al)` marker + `k.v(16388)` edge +
            // `!g.b(aS.S)` + `aS.aZ` → `G()` + `bh()` + `i(284)`;
            // fallthrough `an && bI > 13 → r8.i(17)`; no-overlap → `G()`.
            16 -> {
                if (player.ga != null) return                          // L177d
                e.az = 300                                             // L1784
                val r8 = w.findByAw(e.Z[0])                            // k.q(Z[0])
                if (player.ac === e) {
                    if (!w.kAn && !w.kAo) {                            // fades idle
                        doorArriveBi(e, w, player); return             // bi()
                    }
                    if (w.kAo && w.kBI > 13) {                         // L17b0
                        r8?.setAnim(19)
                        player.flingAirborne(0, w)                     // aS.a(0)
                    }
                    return                                             // L17cf
                }
                if (e.oId == -1) return                                // L17d0
                if (rectsOverlap(player.W, e.W) && player.g == null) { // L17d9
                    player.spawnMarker(w, 105,
                        (e.W[0] + e.W[2]) shr 1, e.al)                 // aS.a(105,…)
                    if (w.padHeld(16388) && !w.playerAttacking() &&
                        player.aZ) {
                        e.dropAeLink()                                 // G()
                        doorExitBh(e, w, player)                       // bh()
                        player.setAnim(284)
                    }
                    if (w.kAn && w.kBI > 13) r8?.setAnim(17)           // L1837
                } else {
                    e.dropAeLink()                                     // L1850
                }
            }
            50 -> {                                             // L852 rope-dismount
                val ga = player.ga
                if (ga != null && ga.ax == 43 &&
                    (ga.S == 1 || ga.S == 4)) {                  // L854/L860
                    if (rectsOverlap(player.W, e.W)) {
                        e.spawnMarker(world, 7, player.ak, player.al - 50)
                        e.ae?.let { it.ak = player.ak; it.al = player.al - 50 }
                        player.cFlag = false                     // g.C = false
                        if (world.padHeld(16388)) {              // k.v(16388) edge
                            e.releaseAe()
                            player.ag = if (player.av) -3328 else 3328
                            player.ah = -6656
                            player.setAnim(243)
                            player.ga = null
                            player.cFlag = true                  // g.C = true
                            world.removeEntity(e)
                        }
                    }
                }
            }
            51 -> {                                             // L874/L880 cv register
                if (world.cv !== e && Entity.overlapStrict(world.kAc, e.W))
                    world.cv = e
                else if (world.cv === e && !Entity.overlapStrict(world.kAc, e.W))
                    world.cv = null
            }
            53 -> if (player.gD && rectsOverlap(player.W, e.W) &&
                      (player.S == 0 || player.S == 1 || player.S == 5)) {
                player.setAnim(360)
                player.ag = 0; player.ah = 0
                world.removeEntity(e)
            }
            // aV() table closed (i.java:9160-9260 audit): every remaining
            // state is a proven dead arm or a no-op — nothing left to port.
        }
    }

    /**
     * `i.bh()` (i.java:14421, proven) — the door-EXIT arm: zero player
     *  velocity fields, re-center `ak` on the door's W, then bind the
     *  player to the destination door via `k.q(this.o)` + `aS.a(iVar)`
     *  (`P|=256`), and arm the fade-IN `k.B(26)`. The destination door's
     *  own S16 tick sees `aS.ac == this` and runs `bi()`.
     */
    private fun doorExitBh(e: Entity, w: LevelCellSource, p: Entity) {
        p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
        p.ak = (e.W[0] + e.W[2]) shr 1
        val q = w.findByAw(e.oId)                        // k.q(this.o)
        if (q != null) p.bindAc(q)                       // aS.a(iVarQ)
        w.fadeIn()                                       // k.B(26)
    }

    /**
     * `i.bi()` (i.java:14437, proven) — the door-ARRIVAL arm (runs on the
     *  DESTINATION door once `aS.ac == this` and fades are idle): facing
     *  `av = (aD&1)!=0`, player bottom-center at `(W0+W2)>>1, W[3]`,
     *  `i(285)` + `t()`, then `k.ah?.I()` + `k.m(k.ad)` camera snap +
     *  fade-OUT `k.C(26)`.
     */
    private fun doorArriveBi(e: Entity, w: LevelCellSource, p: Entity) {
        if (p.ac !== e) return
        p.av = (e.aD and 1) != 0
        p.ak = (e.W[0] + e.W[2]) shr 1
        p.al = e.W[3]
        p.setAnim(285)
        p.refreshBoxes()                                 // t()
        w.refreshScrollBounds()                          // k.ah?.I()
        w.kM(w.kAd)                                      // camera snap
        w.fadeOut()                                      // k.C(26)
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
                    pushOut(e, player, world) // L18: a() solid-side helper
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
                    world.kAp[5]++; world.kCollectStreak()
                    if (e.m > 0) {
                        world.spawnWisp(e)
                        world.kAp[5]++; world.kCollectStreak()
                        e.m--
                    }
                }
                if (world.claimed === e) world.clearClaim()
                world.removeEntity(e)
            }
            29 -> {
                // L50 (i.java:5527-5568, proven): blast sweep — player
                // body overlap → `aS.a(4,0,0,this)` damage; then the bd[]
                // draw-list sweep over ax∈{4,11,17,73,15,23,29} ∩ X with
                // per-type effects (ax4-S30→i(29) chain, ax29≠S20 → -50hp
                // +i(20), melee set → `-bu[au]<<1`, ax15-S6 → i(7)+Z[3]=1).
                if (rectsOverlap(player.W, e.X)) player.applyHit(4, 0, e, world)
                for (o in world.npcs) {
                    val hit = when (o.ax) {
                        4, 11, 17, 73, 15, 23, 29 -> rectsOverlap(o.W, e.X)
                        else -> false
                    }
                    if (!hit) continue
                    if (o.ax == 4 && o.S == 30) o.setAnim(29)
                    if (o.ax == 29 && o.S != 20) { o.aB -= 50; o.setAnim(20) }
                    if (o.ax == 11 || o.ax == 17 || o.ax == 73 || o.ax == 23) {
                        if (o.aB > 0) o.aB -= Entity.WEAPON_DMG[world.weaponSlot] shl 1
                    } else if (o.ax == 15 && o.S == 6) {
                        o.setAnim(7); o.Z[3] = 1
                    }
                }
                if (e.T == 6 && e.U == 0) world.sfx(12)
                if (e.animFinished()) world.removeEntity(e)
            }
            30 -> {
                // L78 (i.java:5570-5581, proven): blast proximity re-arm —
                // player hitbox ∩W or mid-S295 (body ∩W || grapple link
                // ∩W) → i(29) restarts the sweep.
                if (rectsOverlap(player.X, e.W)) e.setAnim(29)
                if (player.S == 295 &&
                    (rectsOverlap(player.W, e.W) ||
                     (player.ga != null && rectsOverlap(player.ga!!.W, e.W)))) {
                    e.setAnim(29)
                }
            }
            33 -> {
                // L91 (i.java:5583-5601, proven): fly-out prop — `b=true`;
                // aA==1 off-camera exit → aA=2 + P&=-17|32|128; when the
                // player is within ±20px and the previous spark is done
                // (`af==null || af.S==36`), spawn the a(24,40,35,200)
                // clip-40 child, latch af, k.b-insert.
                e.b = true
                if (e.aA == 1 &&
                    ((e.ag > 0 && e.W[0] > world.camRect[2]) ||
                     (e.ag < 0 && e.W[2] < world.camRect[0]))) {
                    e.aA = 2; e.P = e.P and -17; e.P = e.P or 32; e.P = e.P or 128
                }
                if (Math.abs(player.ak - e.ak) <= 20 &&
                    (e.af == null || e.af!!.S == 36)) {
                    val aK = e.spawnChildFx(world, 24, 40, 35, 200)
                    e.ai = 0; aK.ag = 0; aK.ah = 768; aK.aj = 1536
                    aK.refreshBoxes()
                    e.af = aK
                    world.queueInsert(aK)
                }
            }
            // Every other aj() state is a proven dead arm or a no-op
            // (level-0's S9/S21 records hit the same default as upstream).
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
     *  Guards that can't fire here omitted; `aS.y()` → `hitWall()`
     *  false (inferred). */
    private fun pushOut(e: Entity, p: Entity, w: LevelCellSource) {
        if (e.S == 139) return
        if (e.S == 18 && p.S == 12) return
        if (e.S == 131 || e.S == 146) return
        if (!rectsOverlap(p.W, e.W)) return
        if (p.ga != null) return
        if (p.S > 43) return
        val pw = p.W[2] - p.W[0]; val ew = e.W[2] - e.W[0]
        // i.java:749-758 (proven): the push only fires when the player is
        // NOT wall-blocked on the travel side — `!aS.y()`.
        if (p.ak <= e.ak && p.ag >= 0 && !p.hitWall()) {
            p.ak = e.ak - pw / 2 - ew / 2; p.ai = 0; p.ag = -1
        } else if (p.ak > e.ak && p.ag <= 0 && !p.hitWall()) {
            p.ak = e.ak + pw / 2 + ew / 2; p.ai = 0; p.ag = 1
        }
        p.collideSides(w, true)      // a(true) side-strip rescan + snap
        p.ag = 0                     // L63: aS.ag = 0 every overlapping tick
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
            3 -> pushOut(e, p, w)                                     // L44 a()
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

    /** `i.bo()` (i.java:15563, proven): `g.j` latch && overlap &&
     *  `g.a == null` — player pressed against the crate while j-set. */
    private fun crateContact(e: Entity, w: LevelCellSource,
                             p: Entity): Boolean =
        w.gj && Entity.overlapI(p.W, e.W) && p.ga == null

    /** `i.bp()` (i.java:15567, proven): `if (g.j) return false` — then
     *  `aS.S∈{43,35}` && `ah>0` && `W[0] < aS.ak < W[2]` &&
     *  `aS.al <= W[3]` — falling onto the crate's top while j-clear. */
    private fun crateLandSpot(e: Entity, w: LevelCellSource,
                              p: Entity): Boolean =
        !w.gj && (p.S == 43 || p.S == 35) && p.ah > 0 &&
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
                    p.setAnim(0); Entity.entBq = 0; p.ga = e
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
    // runs `k.by` ops each tick while a claim is bound (runClaimScript
    // ported, slice 43b; e.g. op25 drives `ak`). If the level's bound
    // scripts don't advance it, the gondola parks (inferred — bx()
    // itself never writes `ak` except the S1 reset).
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
                        e.spawnFloatie(world, 8, ad.ak, ad.al)   // d(8,…)
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

// ==================================================================// ax21 — `bD()` mission director (i.java:18054-18629, proven)
// ==================================================================
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
    var h = 0; var i = 0  // c.h/c.i — entity-relative offsets written on
                          // bound `F` nodes (i.java:8198/8405); records +0
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
                    // L92: `r8.Z[1] < 10000 → r8.aw()` (i.java:51396)
                    if (r05.Z[1] < 10000) materializeWaypoints(r05, w)
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

    // ===== L237 tail — every non-returning tick ======================    // linked-entity anim watcher (r10 = 0..4 over Z[0..4])
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

// =====================================================================// Slice 34 — i.aP() ax29 boss duel FSM (i.java:10328-11076)
// =====================================================================
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
                        r023.bindContext(w)                // N() — i.java:8561
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


// =============================================================// slice 48 — ax9 `bM()` (i.java:21069-21196) + init arm L50 (:2781-2793)
// ====================================================================
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
        // -- L15b9/L15dd (i.java:12121-12142, proven): S4 hint-banner
        //    zone — while player box overlaps, `k.aB` = level string aF
        //    + `k.aC = -1` (hold); leaving clears only once aC is out
        //    (`aC>0` → L1ec7 = bare return, keeps the banner ticking).
        4 -> if (Entity.overlapStrict(p.W, e.W)) {
            w.kAB = w.levelString(1 + w.kAj, e.aF)
            w.kAC = -1
        } else if (w.kAC <= 0) w.kAB = null
        // -- L15e8-L1625 (i.java:12144-12171, proven): S5 context pad —
        //    overlap + (up 16388 held/edge) OR facing-tap (av→u(2),
        //    else u(8)) → `aS.i(22)` player rise.
        5 -> if (Entity.overlapStrict(p.W, e.W) &&
            (w.padDown(16388) || w.padHeld(16388) ||
             (p.av && w.padDown(2)) || (!p.av && w.padDown(8))))
            p.setAnim(22)
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

// ==============================================================// ax15 — bu() (i.java:16693, proven): grapple/hang volume. bi[15]=25.
// S6/S8 run the body: player pushed out horizontally, captured into a
// hang (e(r1) → aS.i(108) + g.a claim), or claimed by the key arms.
// S9 → P|=16 (passive marker). S10 → P|=16 + bt() falling sweep.
// S7 → respawn-or-remove. Level-0 record aw=124 is the S9 marker.
// ==============================================================
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

// ==============================================================// ax46 — aZ() (i.java:13568-13726, proven): spring/trap prop. bi[46]=29
// but the anim clip rebinds per-record: `aa = k.r(k.bl[r8[10]])` with
// k.bl = {29, 0} — bl[1]=0 = the mega clip (clip0) whose 300-range
// anims host the armed-trap states 327/328/329. Head pins palette Z[5].
// ==============================================================
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

// ==============================================================// ax7 — ejection slot (i.java tick-dispatch case 7 → L88/L90/L94,
// :5110-5149, proven): the swallow-and-eject warp volume. Records carry
// no Z — init is just `az = r8[7]` + shared `i(r8[5])+t()`.
// ==============================================================
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


// ==============================================================// ax72 + ax78 + ax79 — the counterweight pair + palette prop
// (i.java init L384/L153/L386; ax78 tick `bA()` :17525, proven).
// ==============================================================
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
    e.setAnim(rf(5))                                   // L392 tail: i(r8[5])
    e.refreshBoxes()                                   // L427 tail: t()
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
    e.advanceAnim()                                            // e() s() preamble
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
    val bj = intArrayOf(19, 68)                        // k.bj (k.java:8443)
    val sel = rf(7)
    e.clip = w.clips[if (sel in bj.indices) bj[sel] else 19]  // L21 aa=k.r(bj[r8[7]])
    e.setAnim(rf(5))                                   // L392 tail: i(r8[5])
    e.refreshBoxes()                                   // L427 tail: t()
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
    e.advanceAnim()                                            // e() s() preamble
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
                    it.setAnim(2)
                    e.spawnFloatie(w, 8, it.ak, it.al)          // d(8,…)
                    w.countKill(r0.aw); r0.setAnim(10)
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
                    r0.ad?.let { it.setAnim(2)
                        e.spawnFloatie(w, 8, it.ak, it.al) }    // d(8,…)
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
                    if (e.S == 22) e.spawnFloatie(w, 9, e.ak, e.al)  // d(9,…)
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

// ==================================================================// ax58 — `bg()` lever/switch block (i.java:14633-14697, proven)
// ==================================================================
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

// ============================================================ ax60 = bj()
/**
 * `i.bj()` (proven transcription). S9/16 = vertical lifts, S10/17 =
 * vertical travel, S11/14 = horizontal pair member (parked), S13/15 =
 * horizontal mover (`c(true)`), anything else = zone-scan only.
 * `Z[4]` drive mode: 0/2 = timed/auto-bounce, 1 = ax60 pair, 3 = ax58 lever.
 */
fun NpcFsm.tickAx60(e: Entity, w: Level0World, p: Entity) {
    e.refreshBoxes()                                       // t() (L2)
    e.b = false                                            // ridden latch off
    when (e.S) {
        9, 16 -> {                                         // L4 idle/arm
            e.aC = 0; e.ah = 0; e.ag = 0
            if (e.Z[0] != -1) {
                val r0 = w.findByAw(e.Z[0])                // k.q(Z[0])
                if (r0 != null && r0.ax == 58) {
                    e.s = r0; e.Z[4] = 3                   // lever-linked
                }
                if (r0 == null) {
                    e.setAnim(if (e.S == 9) 10 else 17)    // L16/L17
                    e.Z[0] = -1; e.aC = e.Z[2]
                }
            }
            if (e.Z[4] == 2) {                             // L19 auto-bounce
                e.setAnim(if (e.S == 9) 10 else 17)
                e.ah = e.Z[1] shl 8
            }
            ax60Ride(e, w, p); ax60Zones(e, w)             // →L26 →L177
        }
        10, 17 -> { ax60Ride(e, w, p); ax60Zones(e, w) }   // L26
        11, 14 -> { ax60Mount(e, w, p, false); ax60Zones(e, w) }  // L176
        13, 15 -> {                                        // L168
            if (ax60Mount(e, w, p, true)) {                // c(true) → L171
                if (e.Z[4] == 1) {                         // pair handoff
                    val ac = e.ac!!
                    if (ac.ak <= ac.Z[3]) {
                        e.ak = ac.Z[3] - 50
                        e.ag = (-e.Z[1]) shl 8
                    }
                    ac.ak = e.ak + 50
                }
                ax60Zones(e, w)
            }                                              // else return
        }
        else -> ax60Zones(e, w)                            // L177 default
    }
}

/**
 * `bj()` L26 arm (i.java:14845-14980, proven) — vertical mover body:
 * rider anims + side push-out, then the Z[4]-mode drive tail.
 * `r72` = vertical pair (S9/10) — S16/17 take the same arm with r72=false.
 */
private fun NpcFsm.ax60Ride(e: Entity, w: Level0World, p: Entity) {
    val r72 = e.S == 9 || e.S == 10                        // L28-L31
    if (Entity.overlapStrict(p.W, e.W)) {
        if (p.ak >= e.W[0] && p.ak <= e.W[2] &&
            p.W[1] <= e.W[3] && p.W[3] >= e.W[3] && r72 && p.aZ) {
            // L48-L56: standing on the lift — ride anims while it moves
            if (p.S != 79 && p.S != 78 && p.S != 50) p.setAnim(78)
            if (e.ah != 0) p.setAnim(50)
            else if (e.ag != 0) ax60PushOut(e, w, p)       // L55→L58
        } else ax60PushOut(e, w, p)                        // L58
    }
    // L69+: drive tail
    if (e.Z[4] == 2) {
        // L69-L78: auto-bounce — probe one cell past the travel edge;
        // solid (≥12) or type-5 → reverse to -|Z[1]|; otherwise keep
        // pressing toward Z[5] with +|Z[1]| once below it.
        val r03 = e.e(w, e.ak / 20, ((if (r72) e.W[3] else e.W[1]) + 1) / 20)
        if (r03 >= 12 || r03 == 5) {
            e.ah = -Math.abs(e.Z[1] shl 8)
        } else if (e.al <= e.Z[5]) {
            e.ah = Math.abs(e.Z[1] shl 8)
        }
        return
    }
    if (e.Z[4] == 3 && e.s != null) {
        // L86: lever-driven — ax58 bf() starts/stops the motion
        if (e.s!!.isBf()) {                                // L94 lever on
            if (e.k) { e.ah = 0; return }                  // latched → hold
            e.setAnim(if (r72) 10 else 17)
            e.ah = (if (r72) 1 else -1) * (e.Z[1] shl 8)
            e.runnerBz = true                              // i.bz moving
        } else {                                           // L103 lever idle
            if (e.k) {
                e.ah = (if (r72) -1 else 1) * (e.Z[1] shl 8)
                e.setAnim(if (r72) 10 else 17)
                e.k = false
            } else if (!e.runnerBz) return                 // L114 hold
        }
        // L116-L146: bounds + support latch
        val r8 = if (e.Z[1] < 0) !r72 else r72
        if (r8 && e.al < e.Z[3] && e.ah < 0) {             // L135 clamp top
            e.ah = 0; e.al = e.Z[3]
            e.setAnim(if (r72) 9 else 16)
            e.runnerBz = false
        }
        if (!r8 && e.al > e.Z[3] && e.ah > 0) {            // L100 reverse
            e.ah = -(e.Z[1] shl 8); e.runnerBz = true
        } else {
            // L141/L146: probe the support side — solid → latch `k`
            val probe = if (r8) e.e(w, e.ak / 20, ((e.al + 28) / 20) + 1)
                        else e.e(w, e.ak / 20, (e.al / 20) - 1)
            if (probe >= 12) e.k = true
        }
        return
    }
    // L149: free run — aC countdown then sink until the probe lands
    val r15 = e.aC; e.aC = r15 - 1
    if (r15 >= 0) return
    e.ah = e.Z[1] shl 8
    val r04 = e.e(w, e.ak / 20, ((if (r72) e.W[3] else e.W[1]) + 1) / 20)
    if ((r04 >= 12 || r04 == 5) && e.ah >= 0) {            // L160
        e.ah = 0
        e.setAnim(if (r72) 9 else 16)
    }
}

/** `bj()` L58 arm (proven): horizontal push-out for a clipped player. */
private fun ax60PushOut(e: Entity, w: Level0World, p: Entity) {
    if (p.S != 277) return
    if (p.W[2] < e.W[2]) {
        p.ak = e.W[0] - (p.W[2] - p.ak) - 5
    } else if (p.W[0] > e.W[0]) {
        p.ak = e.W[2] + (p.ak - p.W[0]) + 5
    }
    p.ag = 0
    if (!p.aZ) p.flingAirborne(0, w)                       // aS.a(0)
}

/**
 * `i.c(boolean)` (i.java:15060-15260, proven) — ax60 mount + pair-motion
 * helper for S11/13/14/15. Returns false = "motion still pending/stopped"
 * (the caller returns early); true = ran through the end-probe.
 */
private fun NpcFsm.ax60Mount(e: Entity, w: Level0World, p: Entity,
                             r7: Boolean): Boolean {
    // L0-L20: lazy link — Z[0] → ac via a(i); pair/lever flags
    if (e.ac == null && e.Z[0] != -1) {
        val r0 = w.findByAw(e.Z[0])                        // k.q(Z[0])
        if (r0 != null) e.ac = r0                          // a(r0): P|256
    }
    e.ac?.let { ac ->
        if (e.S == 13 && ac.ax == 60 && ac.S == 11) {
            e.Z[4] = 1; ac.Z[4] = 1                        // pair latch
        }
        if (ac.ax == 58) e.Z[4] = 3                        // lever-driven
    }
    // L20-L73: player mount / push / dismount arms
    if (Entity.overlapStrict(p.W, e.W)) {
        if (p.ga != e &&
            (p.gB() || p.S == 209 || p.S == 34)) {
            if (p.al <= e.W[3]) {                          // L30 top mount
                p.setAnim(0); p.al = e.W[1] + 1
                p.ag = 0; p.ah = 0; p.ga = e               // g.a = this
            } else if ((p.ak <= e.W[0] && !p.av) ||
                       (p.ak >= e.W[2] && p.av)) {         // L33 walk-into
                p.setAnim(209)
                if (p.ak <= e.W[0]) p.ak = e.W[0]
                else if (p.ak >= e.W[2]) p.ak = e.W[2]
                p.ag = 0; p.ah = 0; p.ga = e
            }
        }
        if (p.ga != e && p.gC()) {                         // L48 side clip
            if (p.W[2] < e.W[2]) {
                p.ak = e.W[0] - (p.W[2] - p.ak) - 5; p.ag = 0
                if (!p.aZ && p.ga == null) p.flingAirborne(0, w)
            } else if (p.W[0] > e.W[0]) {                  // L59
                p.ak = e.W[2] + (p.ak - p.W[0]) + 5; p.ag = 0
                if (!p.aZ && p.ga == null) p.flingAirborne(0, w)
            }
        } else if (p.ga == e) {                            // L66 drift-off
            if (p.al > e.W[3] && p.S != 209 && p.S != 50)
                p.flingAirborne(0, w)
        }
        if (p.ga == e) {                                   // L78 ride carry
            p.ak += e.ag shr 8
            if (w.playerAttacking()) p.ak -= (e.ag shl 1) shr 8
        }
        // L83-L90: moving contact anim through the X attack box
        if ((e.ag != 0 || e.ah != 0) &&
            Entity.overlapStrict(p.W, e.X)) p.setAnim(50)
    } else if (p.ga == e && p.S != 209) {                  // L73 walked off
        p.ga = null
    }
    // L92-L109: S13 X-overlap → hand velocity to the S11 pair member
    if (e.ag != 0 && e.S == 13) {
        for (n in w.npcs) {
            if (n.ax == 60 && n.S == 11 &&
                Entity.overlapStrict(n.X, e.X)) {
                e.ag = (-e.Z[1]) shl 8
                n.ag = e.Z[1] shl 8
            }
        }
    }
    // L109-L136: lever-driven horizontal motion
    if (e.Z[4] == 3) {
        val ac = e.ac
        if (ac != null && ac.isBf()) {
            e.P = e.P or 16
            if (e.k) { e.ag = 0; return false }
            if (e.aC <= 0) e.ag = (if (r7) 1 else -1) * (e.Z[1] shl 8)
            e.runnerBz = true
        } else {
            if (e.k) {                                   // L125 unlatch
                e.ag = (if (r7) -1 else 1) * (e.Z[1] shl 8)
                e.k = false
            } else if (!e.runnerBz) return false         // L132 hold
        }
    }
    if (e.ag == 0) {                                     // L136 cooldown
        val r13 = e.aC; e.aC = r13 - 1
        if (r13 > 0) return false
    }
    // L141-L190: bounds + end-probe latch
    val r82 = if (e.Z[4] == 3 && e.Z[1] < 0) !r7 else r7
    val pastBound = (e.ag < 0 && e.ak < e.Z[3] && r82) ||
                    (e.ag > 0 && e.ak > e.Z[3] && !r82)
    if (pastBound) {                                     // L164 clamp
        e.ak = e.Z[3]
        if (e.ag != 0 && e.Z[4] == 3 && e.runnerBz) {
            e.ag = 0; e.runnerBz = false; e.aC = e.Z[2]
            return false
        }
        e.ag = (if (r7) 1 else -1) * (e.Z[1] shl 8)      // L173/L175
    }
    if (e.ag != 0) {
        // L178/L186: probe one cell past the travel edge
        val r04 = if (r82)
            e.e(w, ((e.ak + 22) / 20) + 1, (e.al / 20) - 1)
        else
            e.e(w, (e.ak / 20) - 1, (e.al / 20) - 1)
        if (r04 < 12) return true
        if (e.Z[4] == 3) { e.k = true; return true }       // L182/L188
        e.ag = if (r82) (-e.Z[1]) shl 8                  // L184 bounce
              else e.Z[1] shl 8                          // L190
        return true
    }
    return true
}

/** `bj()` L177 tail (proven): ax10 S=39 zone overlap → bounce/latch/idle. */
private fun ax60Zones(e: Entity, w: Level0World) {
    for (n in w.npcs) {
        if (n.ax != 10 || n.S != 39) continue
        if (!Entity.overlapStrict(n.W, e.W)) continue
        when (e.S) {
            10, 17 -> when (e.Z[4]) {
                3 -> e.k = true                            // L200
                2 -> e.ah = -e.ah                          // L191 bounce
                else -> e.setAnim(if (e.S == 10) 9 else 16)
            }
            14, 15 -> e.k = true                           // L200
        }
    }
}

/** ax60 init arm (i.java:3394, L259 block, proven). */
fun NpcFsm.initAx60(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = 1
    e.Z.fill(0)
    e.Z[0] = rf(7)                                         // link uid
    e.Z[1] = rf(4)                                         // speed
    e.Z[2] = rf(8)                                         // delay
    e.Z[4] = 0
    if (rf(5) == 9 || rf(5) == 16 || rf(5) == 10 || rf(5) == 17)
        e.P = e.P or 4096                                  // L262-L267
    e.Z[3] = if (rf(5) == 9 || rf(5) == 16) e.al else e.ak // L269-L272
    if (rf(9) == 1) e.Z[4] = 2                             // auto-bounce
    e.Z[5] = e.Z[3]
    if (rf(5) == 14 || rf(5) == 15 || rf(5) == 16) {
        // L281: horizontal bound probe — scan from ak toward solid while
        // Z[1] < 0; S15 scans left, 14/16 scan right; S15 adds one cell.
        if (e.Z[1] < 0) {
            var r92 = e.ak / 20
            val r05 = (e.al / 20) - 1
            while (w.collisionCell(r92, r05) < 12)
                r92 += if (rf(5) == 15) -1 else 1
            e.Z[5] = r92 * 20
            if (rf(5) == 15) e.Z[5] += 20
        }
    } else if (rf(5) == 9) {
        // L297: vertical bound probe — scan UP to the first solid row.
        var r03 = e.ak / 20
        var r10 = e.al / 20
        while (w.collisionCell(r03, r10) < 12) r10 -= 1
        e.Z[5] = r10 * 20 + 20
    }
    if (rf(5) == 6 || rf(5) == 11 || rf(5) == 13) {        // L308/L310→L315
        e.az = 0; e.P = e.P or 16
    }
    if (rf(5) in 11..15) e.aC = e.Z[2]                     // L317
    e.setAnim(rf(5))                                       // L392 tail
    e.refreshBoxes()                                       // t()
}
// ==================================================================// ax43 = bw() — ride/swing carrier (dispatch i.java:5081; bw():17086-17187,
// all proven). Bound by the grapple-offer `o(i)` (:17038-17085) — the ax15
// arm's r6 == 43 check: `g.a=r6, i(1), k.ae=r6, A(19), ag=Z[1]<<8, G(),
// g.C=true`. While bound (`g.a==this`, S∈{1,4}): pins the player at its
// own X center, forces the hang-anim family (295/304-307), moves at
// `Z[1]<<8` modulated ×150% same-dir / ×50% reverse by held pads
// u(4112)/u(8256). `v(16388)&&g.C → i(4)`; `g.g()` attack → `i(7)` cut.
// `cv` (i.cv): the ax10-S51 rail zone under k.ac — full containment of
// own Y inside cv.W → detach + aS.a(0) + k.c(this). S7 = cut → vel0 +
// unlink ae/ga + o(this) re-offer. L75 (unbound): k.ae=aS + o(this).
// ==================================================================
/**
 * `i.o(i)` (i.java:17038-17085, proven): the ax43 grapple-attach offer —
 * non-43 callsites no-op (ax40's `o(this)` at :17185 is dead). `t()` then:
 * `g.a != null` or `g.g()` → `G()` + return. Else `aS.Y ∩ r6.Y` required;
 * `aS.S ∈ {243,24,22}` bails to `G()`; `r6.Z[2]>0` auto-binds, else the
 * `a(8,ak,al-85)` prompt + `v(65568)` tap path. `r7 && g.i` → bind:
 * `g.a=r6, i(1), k.ae=r6, A(19), r6.ag=Z[1]<<8, G(), g.C=true`.
 */

fun NpcFsm.grappleOffer(r6: Entity, w: Level0World) {
    if (r6.ax != 43) return                             // L7: non-43 → dead call
    r6.refreshBoxes()                                   // t()
    val p = w.player
    if (p.ga != null) { r6.releaseAe(); return }        // L34
    if (w.playerAttacking()) { r6.releaseAe(); return } // L12→L34
    if (!Entity.overlapStrict(p.Y, r6.Y)) {             // →L27
        r6.releaseAe(); return
    }
    if (p.S == 243 || p.S == 24 || p.S == 22) {         // L16 rejects
        r6.releaseAe(); return
    }
    var r7 = false
    if (r6.Z[2] > 0) r7 = true                          // auto-proximity
    else {                                              // L24 prompt + tap
        r6.spawnMarker(w, 8, r6.ak, r6.al - 85)
        if (w.padHeld(65568)) r7 = true
    }
    if (!r7 || !w.iFlag) return                         // L29/L38
    p.ga = r6                                           // L31 bind
    r6.setAnim(1)
    w.kAe = r6
    w.sfx(19)
    r6.ag = r6.Z[1] shl 8
    r6.releaseAe()
    p.cFlag = true                                      // g.C = true
}

fun NpcFsm.tickAx43(e: Entity, w: Level0World, p: Entity) {
    e.advanceAnim()
    if (e.claimActive()) { e.runClaimScript(w); return }        // ab()→aa()
    when (e.S) {
        7 -> {                                                  // L7 cut/re-offer
            e.ag = 0; e.ah = 0
            if (w.kAe === e) w.kAe = p
            if (p.ga === e) p.ga = null
            grappleOffer(e, w)
            return
        }
        1, 4 -> {
            // L16 cv arm — Y fully inside the S51 rail zone → detach+remove
            val cv = w.cv
            if (cv != null) {
                cv.refreshBoxes()
                if (cv.W != null && Entity.containRect(e.Y, cv.W)) {
                    if (p.ga === e) {
                        p.ga = null
                        p.flingAirborne(0, w)             // k.aS.a(0)
                        if (w.kAe === e) w.kAe = p
                    }
                    w.removeEntity(e)
                    return
                }
            }
            // L29 — player attack cuts the bind → i(7)
            if (w.playerAttacking() && p.ga === e) {
                p.ga = null
                e.setAnim(7)
                return
            }
            if (p.ga !== e) {                             // L75 unbound path
                if (p.ga == null) {
                    w.kAe = p                             // k.ae = aS
                    e.ag = e.Z[1] shl 8
                    grappleOffer(e, w)
                }
                if (!e.animFinished()) return             // L78
                e.setAnim(1)
                return
            }
            // L35 bound ride
            e.refreshBoxes()                              // t()
            e.az = p.az - 1
            e.ag = e.Z[1] shl 8
            if (w.padHeld(16388) && p.cFlag) e.setAnim(4) // L42 release variant
            if (e.S == 1) {                               // L48-L60 input mod
                val base = e.Z[1] shl 8
                e.ag = when {
                    (e.av && w.padDown(4112)) ||
                    (!e.av && w.padDown(8256)) -> base * 150 / 100
                    (e.av && w.padDown(8256)) ||
                    (!e.av && w.padDown(4112)) -> base * 50 / 100
                    else -> base
                }
            }
            if (w.kAe !== e) w.kAe = e                    // L62/L65
            if (p.S < 304) p.setAnim(295)                 // L72
            if (p.S < 308) {                              // L73 pin (≥308 → L75→L78)
                p.av = e.av
                p.ag = 0; p.ah = 0
                p.ak = (e.X[0] + e.X[2]) shr 1
                p.al = (e.X[1] + e.X[3]) shr 1
            }
            if (e.animFinished()) e.setAnim(1)            // L78
            return
        }
        else -> return                                    // L81
    }
}

// -- file-scope tables (companion-private members can't be seen by
//    top-level extensions; these live here for the ax17 arms) ----------
/** `i.bv` — civilian max-HP table {100,140,200} (i.java:22317, proven;
 *  `aB = bv[k.au]` at :3011 — `kAu` wired slice 197). */
private val BV = intArrayOf(100, 140, 200)
/** `i.J` — finisher/heavy damage {100,100,100} (i.java static{}, proven). */
private val JD = intArrayOf(100, 100, 100)
/** `i.H[0]` — counter/weakened line (proven value, index au=0). */
private const val H0 = 50
/** `g.b()` player-attack anim set (proven, j() L9/L10). */
private val ATTACK_ANIMS = intArrayOf(
    67, 68, 69, 81, 112, 113, 114, 115,
    183, 184, 216, 217, 286, 287)
/** `i.I` — ax73 damage vs knife swings {20,20,20} (i.java:169, proven). */
private val II73 = intArrayOf(20, 20, 20)

/** `i.h()` (i.java:1271-1276, proven): counter-engage immunity gate —
 *  S216/217 finishers never engage; ax11-Z0==2 (weakened) engages unless
 *  posed {11,12,6} (player S68/69 heavy swings override the immunity);
 *  every other type engages only ax73 during S69. */
private fun hGate(e: Entity, p: Entity): Boolean {
    if (p.S == 216 || p.S == 217) return false
    return if (e.ax == 11 && e.Z[0] == 2)
        !(e.S == 11 || e.S == 12 || e.S == 6) || p.S == 68 || p.S == 69
    else
        e.ax == 73 && p.S == 69
}

/** `i.i()` (i.java:1278-1303, proven): the counter-engage — non-
 *  degenerate player attackbox ∩ NPC body + player mid-attack (`g.b()`)
 *  → player forced to S8, companion `k.E` hidden (`P|=128`), and the
 *  NPC takes `aC=16` then `i(17)` (ax73 `i(167)` — the `S != 17` check
 *  is verbatim, 167 for ax73). Always returns false like the original:
 *  its value is the side-effect — leaving ATTACK_ANIMS preempts this
 *  tick's `j()` damage exactly like the source. */
private fun iEngage(e: Entity, p: Entity, w: LevelCellSource): Boolean {
    if (p.X[0] == p.X[2] || !Entity.overlapI(e.W, p.X) ||
        p.S !in ATTACK_ANIMS) return false
    if (p.S != 8) {
        p.setAnim(8)
        w.kE?.let { it.P = it.P or 128 }
    }
    when (e.ax) {
        11 -> if (e.S != 17) { e.aC = 16; e.setAnim(17) }
        73 -> if (e.S != 17) { e.aC = 16; e.setAnim(167) }
    }
    return false
}

/** `i.j()` for ax11 (i.java:1305-1403, proven): the player→NPC strike
 *  resolution — `P()` dead gate (`G()` on the corpse), then:
 *  Z0==2 weakened claims the `aN` finisher lock (`aN==null||aN.S!=18`)
 *  and gates on `g.b()` alone; every other ax11 needs `g.b()` + the
 *  ±160/±20 proximity + the `bf` first-swing latch (`aS.S==67` claims
 *  `aN` when free-or-self). `aS.X` non-degenerate ∩ `W` then `Q()`:
 *  {183,184,216,217} → `aB -= J[au]`, and ax11×{216,217} launches
 *  (`aB=0`, `ag=±5120`, `ai=∓2560`, marker `a(8,50,…)` + `bK→a(8,59,…)`,
 *  `c(85,157)` react, Z0==0 keeps `aB=30`, `g()` nudge, `k.A(13)`);
 *  weakened Z0==2 → `aB -= bw[au]`; ax!=73 → the half-HP every-3rd-hit
 *  engage (`x++ %3` on `aB==bu[au]/2` → aS.i(8) + `k.E.P|=128` +
 *  `aC=16` + `i(17)` + `g()`, no damage) else `aB -= H[au]`; ax73 →
 *  `{286,287} ? I[au] : bw[au]`. Tail: `ax!=11 || Z0!=0 || aB>H[au] ||
 *  S==85` → `C()`; else the weaken-line marker + `c(85,157)` + `g()` +
 *  `k.A(13)`. */
private fun jIntake(e: Entity, p: Entity, w: LevelCellSource): Boolean {
    if (e.aB <= 0) { e.releaseAe(); return false }              // P() → G()

    if (e.ax == 11 && e.Z[0] == 2) {
        if (w.lockTarget !== e && (w.lockTarget == null ||
            w.lockTarget!!.S != 18)) w.lockTarget = e
        if (p.S !in ATTACK_ANIMS) return false
    } else {
        if (p.S !in ATTACK_ANIMS) return false
        if (kotlin.math.abs(e.ak - p.ak) > 160 ||
            kotlin.math.abs(e.al - p.al) > 20) return false
        if (!w.iBf && (w.lockTarget == null || w.lockTarget === e) &&
            p.S == 67) {
            if (w.lockTarget == null) w.lockTarget = e
            w.iBf = true
        }
    }
    if (p.X[0] == p.X[2] || !Entity.overlapI(e.W, p.X)) return false
    e.av = p.ak < e.ak                                          // Q()
    val au = w.weaponSlot
    if (p.S == 183 || p.S == 184 || p.S == 216 || p.S == 217) {
        e.aB -= JD[au]
        if (e.ax == 11 && (p.S == 216 || p.S == 217)) {
            e.aB = 0
            e.ag = if (e.av) 5120 else -5120
            e.ai = if (e.av) -2560 else 2560
            if (e.S == 85) return true
            e.spawnFx8(w, 50, 1, e.av, e.ak, e.al - 40, 300)
            if (w.kBK) e.spawnFx8(w, 59, 1, e.av, e.ak, e.al - 40, 300)
            e.hitAnimByType(85, 157)
            if (e.Z[0] == 0) e.aB = 30
            e.resolvePush(w)
            w.sfx(13)
            return true
        }
    } else if (e.ax == 11 && e.Z[0] == 2) {
        e.aB -= BW73[au]
    } else if (e.ax != 73) {
        if (p.S == 67) e.av = p.ak < e.ak
        if (e.ax == 11 && e.Z[0] == 0 && e.aB == BU73[au] / 2) {
            w.iX++
            if (w.iX % 3 == 0) {
                if (p.S != 8) { p.setAnim(8); w.kE?.let { it.P = it.P or 128 } }
                if (e.S == 17) return false
                e.aC = 16; e.setAnim(17); e.resolvePush(w)
                return false
            }
        }
        e.aB -= H0                                              // H[au]
    } else if (p.S == 286 || p.S == 287) {
        e.aB -= II73[au]
    } else {
        e.aB -= BW73[au]
    }
    if (e.ax != 11 || e.Z[0] != 0 || e.aB > H0 || e.S == 85) {
        return e.hitReact(w)                                    // C()
    }
    e.spawnFx8(w, 50, 1, e.av, e.ak, e.al - 40, 300)
    if (w.kBK) e.spawnFx8(w, 59, 1, e.av, e.ak, e.al - 40, 300)
    e.hitAnimByType(85, 157)
    e.resolvePush(w)
    w.sfx(13)
    return true
}

// ============================================================ ax17 = aA()
// The civilian (16 records across packs 8/9/11; shares clip-7 with the
// soldier family — bi[17]=7). All arms below transcribed verbatim from
// i.java:8708-8847 (`aA()`), :3004 L113 (init), :2385 L70 (l() ax17 arm),
// :1803 (j() ax17 damage arm). Civilians are immune to the damage-op
// dispatcher (`a(int,int,int,i)` case 17 → L141 return, :4584) — only the
// player's sword reaches them through j(). The `k.bK` HAS-BLOOD gate
// (GloftASBR.java:36 → false in this JAR) covers both corpse-fx spawns.
/** ax17 init arm (i.java:3004 L113 + L392 shared tail, proven):
 *  `Z=int[22]; Z[1]=0; Z[2]=-1; aG=r8[4]; az=r8[13]; Z[21]=r8[14];
 *   aB=bv[k.au]; aF=r8[2]; aD=r8[7]; m=r8[8]; o=r8[9]` → `i(r8[5])` + `t()`.
 *  The second-ctor dispatch at :2902 (keyed on `r8[5]` not ax) has a
 *  `case 17 → L111` arm but never fires for real records — they carry
 *  `r8[5] ∈ {57,64,120}` (level-records.json, proven). */
fun NpcFsm.initAx17(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.Z.fill(0)
    e.Z[1] = 0; e.Z[2] = -1
    e.aG = rf(4); e.az = rf(13); e.Z[21] = rf(14)
    e.aB = BV[world.kAu]                                    // bv[k.au]
    e.aF = rf(2); e.aD = rf(7); e.m = rf(8); e.oId = rf(9)
    e.setAnim(rf(5))                                        // L395 i(r8[5])
    e.refreshBoxes()                                        // L427 t()
}
/** `i.b(int[],int[])` (i.java:666, proven): strict containment —
 *  r4 inside r5 on all four edges. The `l()` ax17 notice uses it as
 *  `b(this.W, k.ac)` = "fully inside the camera view". */
private fun insideOf(a: IntArray, b: IntArray): Boolean =
    a[0] >= b[0] && a[1] >= b[1] && a[2] <= b[2] && a[3] <= b[3]
/** `i.aA()` (i.java:8708-8847, proven) — the civilian tick.
 *  S57 idle→panic (l() = `!bn && b(W, camRect)`; player S9/S50 suppress),
 *  anims 60-68 = directional panic flail (picked by player-vs-W quadrant,
 *  strikes the player with op4 at T==3, `r()` → back to S57),
 *  S69 collapse (HAS-BLOOD-gated fx, lock releases), S129 dead-on-spot,
 *  S170 knockdown (`al+=10; a(true)` wall probe → aZ → S129).
 *  L87 tail: j() intake → S==69 → return; S!=129 → a() physics.
 *  Dispatch then falls into `au()` (:7738) = the shared corpse-drop — the
 *  port's `corpseDrop` already covers ax17 verbatim. */
fun NpcFsm.tickAx17(e: Entity, w: Level0World, p: Entity) {
    if (e.aB <= 0 && e.S != 69 && e.S != 129) e.setAnim(69)  // L7 dead-check
    when (e.S) {
        57 -> {                                              // L12 idle
            e.ah = 0; e.ag = 0
            // l() ax17 arm (i.java:2385 L70): !bn && b(W, k.ac) — fully
            // on-screen; bn is the bA[79] checkpoint alert flag.
            val notice = !w.iBn && insideOf(e.W, w.camRect)
            if (notice && p.S != 9 && p.S != 50) {           // L14/L16 gates
                w.sfx(16)                                    // k.A(16) → z()
                e.av = p.ak < e.ak                           // L21 face player
                // L23-L41 directional panic pick: anims 61-63/66/67 by
                // the player's quadrant vs W (X-separated or not, above/
                // below/overlapping). 60/64/65/68 aren't reachable through
                // the pick but live in the same L45 arm.
                val pw = p.W; val rw = e.W
                val xSeparated = pw[0] >= rw[2] || pw[2] <= rw[0]
                val r1 = when {
                    pw[1] > rw[3] -> if (xSeparated) 67 else 63  // below
                    pw[3] < rw[1] -> if (xSeparated) 66 else 62  // above
                    else -> 61                                   // overlap
                }
                e.setAnim(r1)
            }
        }
        in 60..67 -> {                                       // L45 panic-flail
            if (e.T == 3) p.applyHit(4, 0, e, w)             // aS.a(4,0,0,this)
            if (e.animFinished()) e.setAnim(57)              // r() → i(57)
        }
        68 -> {                                              // L51
            if (e.animFinished()) e.setAnim(57)
        }
        69 -> {                                              // L61 collapse
            e.P = e.P or 512
            e.ab = null
            e.ag = 0; e.ah = 0
            val s = e.s                                      // L67 ride-crate
            if (s != null && s.ax == 51 && s.ag != 0) e.ag = s.ag
            e.aA = 2
            if (e.animFinished()) {                          // L71/L76
                if (w.kBK) e.spawnFx8(w, 59, 2, e.av, e.ak, e.al, e.az - 1)
                e.P = e.P and -17; e.P = e.P or 32 or 64
                if (w.lockTarget === e) w.lockTarget = null      // aN release
                if (w.playerLinkB === e) w.playerLinkB = null    // g.b release
            }
        }
        129 -> {                                             // L57 dead-on-spot
            if (e.animFinished()) {
                e.aB = 0
                e.P = e.P and -17; e.P = e.P or 32 or 64
                if (w.kBK) e.spawnFx8(w, 59, 2, e.av, e.ak, e.al, e.az - 1)
            }
        }
        170 -> {                                             // L53 knockdown
            e.al += 10
            e.wallProbe(w)                                   // a(true)
            if (e.aZ) e.setAnim(129)
        }
    }
    // L87 tail (proven): j() → return; S==69 → return; S!=129 → a();
    // the I() dispatcher then falls through to au() (:7738) = corpseDrop.
    // j() P() arm (:7699): aB<=0 → G()+true — corpses skip the rest.
    if (e.aB <= 0) { corpseDrop(e); return }
    // j() intake, ax17 arm (:1803, proven): player attackbox ∩ W + g.b()
    // anim → Q() face + aB -= J=100 for {183,184,216,217}, H=50 otherwise.
    // No S85 react, no weaken/lock arms (ax11/73-only) — always false.
    if (p.X[0] != p.X[2] && p.S in ATTACK_ANIMS &&
        Entity.overlapStrict(e.W, p.X)) {
        e.av = p.ak < e.ak                                   // Q() face
        e.aB -= if (p.S == 183 || p.S == 184 || p.S == 216 || p.S == 217)
            JD[0] else H0
    }
    if (e.S == 69) { corpseDrop(e); return }
    // a() (i.java:914) — the solid-body player push, already ported.
    if (e.S != 129) e.pushContact(w)
    corpseDrop(e)                                            // au() L849
}

// ==================================================================// ax69 = bC() — air-assassination target zone (dispatch i.java:5096;
// bC():17721-18000; init arm :3191 (L194) + shared tail :3680 (L392);
// all proven). bi[69]=38 → clip38.
//
// The perch/kill flow: S0 idle binds the player when a `g.b(S)`-grabbable
// anim overlaps W → `aS.i(250)` hang, `af=k.q(Z[1])` (link uid), `i(7|1)`.
// S7 armed: the preamble (S∈{6,7} && aS.af==this && af==null) scans k.bd
// for an ax11 soldier NOT facing the player (`!g(aS)`), Y-overlapped with
// the zone, within 40px of the zone's right edge → `af` = victim +
// `c/d(ak+52, al-85)` clip-74 hand marker. The L39-L50 keep-alive drops
// `af` (`G()`) when it drifts out of reach.
// S7 kill arm: `Z[0]==0` requires prompt eligibility (victim inside the
// 40px reach OR `af.g(aS)` — victim facing away) → marker at
// (W[2]+35, W[1]-35); `Z[0]!=0` auto-eligible. `v(65568)||V()` (tap or
// marker-touch) → `aS.i(244)` leap, `P|=64`, snap to the zone's
// right-center, `aS.h(1)` (bind claim slot 1), `af.i(117)` death-anim,
// `af.az=-1`, `i(2)` windup, `G()`.
// S6 carry-drift (`a(true)` side-probe first): d-pad `u(8256|4112)`
// drifts zone+player ±1536 together; `w(12368)` (released pad edge)
// → `i(7)`; `y()` wall edge → ag=0 both.
// Tail anims pick by `k.bK` = the HAS-BLOOD build flag (GloftASBR:36 —
// JAR manifest lacks the property → false → censored S10/11/12 set):
//  S2 →r()→ bK?3:10 (re-center player X); S3/S10 →r()→ bK?4:11 +
//  `af=null; G()`; S4/S11: `aA==1` →r()→ `bw=-1,bx=57,l(13)` (mission
//  advance, unported); `aA!=1`: Z[0]==0 →r()→ `aS.S==244` → bK?5:12 →
//  `aS.i(0), P&=-65, E(), af=null`, else park on overlap or
//  `aS.az=100; G(); P|=32|64`; Z[0]==1 →r()→ `i(7)`.
//  S5/S12: Z[0]==0 && r() → bK?4:11, then same park/release tail.
// ==================================================================
/**
 * `i.bC()` (proven transcription). `Z[0]` = zone flavor from r8[4]:
 * 0 = armed-scan (records: all three spawns), 1 = auto-eligible,
 * 2 = perch variant; `Z[1]` = the `af` link uid (r8[7]).
 */
fun NpcFsm.tickAx69(e: Entity, w: Level0World, p: Entity) {
    // ---- preamble: bind / keep-alive the ax11 victim (L2-L50) ----
    if (e.Z[0] != 0 && e.Z[0] != 2 && e.aA != 1 && (e.S == 6 || e.S == 7) &&
        p.af === e) {
        if (e.af == null) {
            for (n in w.npcs) {                             // L19 k.bd[] scan
                if (n.ax != 11) continue                    // L22
                if (n.faces(p)) continue                    // !g(aS) L24
                if (n.W[3] <= p.af!!.W[1]) continue         // L27 Y-overlap
                if (n.W[1] >= p.af!!.W[3]) continue
                // L33 right-reach then L36 bind, else L37 left-reach
                if ((n.ak - e.W[2]) < 40 && n.W[0] > e.W[2]) {
                    e.af = n
                    e.spawnMarker(w, 0, e.ak + 52, e.al - 85)   // c()
                    e.markerPoint(e.ak + 52, e.al - 85)          // o() inside c()
                    e.moveMarker(w, e.ak + 52, e.al - 85)        // d()
                    break
                }
                if ((e.W[0] - n.ak) < 40 && n.W[2] < e.W[0]) {
                    e.af = n
                    e.spawnMarker(w, 0, e.ak + 52, e.al - 85)
                    e.markerPoint(e.ak + 52, e.al - 85)
                    e.moveMarker(w, e.ak + 52, e.al - 85)
                    break
                }
            }
        }
        if (e.af != null) {                                 // L39 keep-alive
            val v = e.af!!
            if ((v.ak - e.W[2]) > 40 && v.W[0] > e.W[2]) { e.af = null; e.releaseAe() }
            else if ((e.W[0] - v.ak) > 40 && v.W[2] < e.W[0]) { e.af = null; e.releaseAe() }
            else if (Entity.overlapStrict(e.W, v.W)) { e.af = null; e.releaseAe() }  // L49 overlap → drop
        }
    }

    when (e.S) {
        0 -> when (e.Z[0]) {                              // L54 idle bind-scan
            0 -> {                                        // L56
                if (!p.gB()) return                       // g.b(aS.S) gate
                if (!Entity.overlapStrict(p.W, e.W)) return // L58/L187
                p.setAnim(250); p.av = false; p.az = -1
                e.setAnim(7)
                e.af = w.findByAw(e.Z[1])                 // k.q(Z[1])
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                p.al = (e.W[1] + e.W[3]) shr 1
                p.ak = (e.W[0] + e.W[2]) shr 1
                p.af = e
            }
            1, 2 -> {                                     // L61
                if (!p.gB()) return
                if (!Entity.overlapStrict(p.W, e.W)) return
                if (p.W[3] < ((e.W[1] + e.W[3]) shr 1)) return // L65/L190
                p.setAnim(250); p.av = false; p.az = -1
                e.setAnim(1)
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                p.al = (e.W[1] + e.W[3]) shr 1
                p.ak = (e.W[0] + e.W[2]) shr 1
                p.af = e
            }
            else -> return                                // L185
        }
        1 -> if (e.animFinished()) e.setAnim(7)           // L69
        2 -> {                                            // L117
            if (e.animFinished()) {
                e.setAnim(if (w.kBK) 3 else 10)      // L119/L121
                p.ak = (e.W[0] + e.W[2]) shr 1            // L122
            }
        }
        3, 10 -> {                                        // L125
            if (e.animFinished()) {
                e.setAnim(if (w.kBK) 4 else 11)      // L127/L129
                e.af = null; e.releaseAe()                // L130
            }
        }
        4, 11 -> {                                        // L133
            if (e.aA == 1) {
                if (e.animFinished()) {                   // L135
                    w.kBw = -1; w.kBx = 57                // L137-L138
                    w.screenL(13)
                }
                return
            }
            when (e.Z[0]) {                               // L139
                0 -> {
                    if (!e.animFinished()) return         // L143 gate
                    if (p.S == 244) {                     // L145 kill landing
                        e.setAnim(if (w.kBK) 5 else 12) // L147
                        p.setAnim(0); p.P = p.P and -65   // L148
                        p.settleToGround(w)               // aS.E()
                        p.af = null
                    } else if (!Entity.overlapStrict(p.W, e.W)) { // L151→L153
                        if (p.az == -1) {                 // L153/L201
                            p.az = 100; e.releaseAe()
                            e.P = e.P or 32; e.P = e.P or 64
                        }
                    }
                }
                1 -> if (e.animFinished()) e.setAnim(7)   // L157/L159
                else -> return                            // Z[0]==2 → L203
            }
        }
        5, 12 -> {                                        // L163
            if (e.Z[0] == 0 && e.animFinished()) {
                e.setAnim(if (w.kBK) 4 else 11)      // L167/L169
            }
            if (!Entity.overlapStrict(p.W, e.W)) {        // L171/L173
                if (p.az == -1) {
                    p.az = 100; e.releaseAe()
                    e.P = e.P or 32; e.P = e.P or 64
                }
            }
        }
        6 -> {                                            // L103 carry-drift
            e.collideSides(w, true)                       // a(true)
            if (w.padDown(8256)) { e.ag = 1536; p.ag = 1536 }  // L105
            else if (w.padDown(4112)) { e.ag = -1536; p.ag = -1536 } // L107
            else if (w.padRelease(12368)) {               // L110 w()
                e.ag = 0; p.ag = 0; e.setAnim(7)
            }
            if (e.edgeFlag()) { e.ag = 0; p.ag = 0 }      // L113/L195
        }
        7 -> {                                            // L71 armed
            if (e.aA == 1) return                         // L75
            var r8 = false
            if (e.af != null) {
                if (e.Z[0] != 0) r8 = true                // L79 auto-eligible
                else {                                    // L79-L84 prompt test
                    val v = e.af!!
                    if ((v.ak - e.W[2]) < 40 && !v.faces(p)) r8 = true
                    if (r8) {                             // L84 marker
                        e.spawnMarker(w, 0, e.W[2] + 35, e.W[1] - 35)
                        e.markerPoint(e.W[2] + 35, e.W[1] - 35)
                        e.moveMarker(w, e.W[2] + 35, e.W[1] - 35)
                    } else e.releaseAe()                  // L86 G()
                }
            }
            if (e.af != null && r8 &&
                (w.padHeld(65568) || e.markerTouched(w))) {     // L89/L93 kill
                p.setAnim(244)
                p.P = p.P or 64
                p.al = (e.W[1] + e.W[3]) shr 1
                p.ak = e.W[2]
                p.bindScript(1, w)                        // aS.h(1)
                e.af!!.setAnim(117)                       // af.i(117)
                e.af!!.az = -1
                e.setAnim(2)
                e.releaseAe()                             // G()
            }
            if (e.Z[0] == 1 || e.Z[0] == 2) {             // L96/L100 arm→S6
                if (w.padDown(12368)) e.setAnim(6)
            }
        }
        else -> return                                    // L191 (S8/S9)
    }
}

/** ax69 init arm (i.java:3191, L194 + L392 shared tail, proven). */
fun NpcFsm.initAx69(e: Entity, f: List<Int>, w: Level0World) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = 0
    e.Z.fill(0)
    e.Z[0] = rf(4)                                        // zone flavor
    e.Z[1] = rf(7)                                        // af link uid
    e.aA = 0
    e.setAnim(rf(5))                                      // L395 i(r8[5])
    e.refreshBoxes()                                      // L427 t()
}

// ============================================================ ax73 = aJ()
// The heavy guard — the third combat NPC of the clip-7 family (bi[73]=7,
// 10 records across packs 8/9/11/12). Transcribed verbatim from:
//   aJ()           i.java:9331-9905   (the FSM body)
//   init dispatch  i.java:2727 (case 73 → L120), :3038-3088 (L120 full /
//                  L123 → L134 minimal), :3042-3066 (Z-field seeds)
//   d()  i.java:1466-1538  — awareness tier (shared, ax73 path)
//   e()  i.java:1539-1545  — sight-rect recompute
//   b(i) i.java:1546-1560+ — engage gate
//   l()  i.java:2255-2330  — sight check, ax73 arm L62-L68
//   h()  i.java:1735-1765  — counter window, ax73 arm L29-L32
//   i()  i.java:1767-1800  — counter trigger, ax73 arm L17
//   j()  i.java:1803-1950  — player→NPC damage intake (L24 path for ax73)
//   C()  i.java:1951-2060  — react/enrage/die, ax73 arms L13/L29/L61
//   g()  i.java:1678-1734  — hit knockback/wall-snap
//   aB() i.java:8845-8893  — strike executor, ax73 arm L17-L40
//   aC() i.java:8893-9144  — attack scheduler (j-tier dispatch + aq arm)
//   aE() i.java:9144-9192  — ceiling ambush
//   aF() i.java:9192-9220  — trailing-edge probe (crate + tile)
//   aG() i.java:9220-9253  — forward-edge probe (crate + tile)
//   am() i.java:7167-7182  — upward ceiling probe (3 cells, facing side)
//   v()  i.java:730-785    — "active" check; ax73 falls through to L85
//   h(int,int) i.java:7219 — tile-blocking predicate (e>=12 || e<5)
//   k.h  k.java:6839       — approx distance ((dx+dy)-min/2-min/4+min/8)
//   k.a  k.java:627        — h()-distance <= r7
//   k.l() k.java:804       — k.M 60x60 reach box in front of player
//   k.a(i,int,int[]) k.java:816 — interact claim (registerClaim)
//   k.m() k.java:863       — claim reset (claimReset)
//   c/d/T/U/V/o i.java:9840-9910 — clip-74 "ae" marker ops
//   n()  i.java:9792-9810  — door-corridor scan (bd[] ax44 S0 X)
//   i.g(int,int) i.java:6881 — press-gauge (65568 taps → bl≥80)
//   statics: aN=lockTarget, bf=iBf, bx=iBx, x=iX, aK wisp-pool (unused here)
//   damage tables: bu={300,400,500}, bw={80,80,80}, I={20,20,20},
//   J={100,100,100} (i.java:22315, proven)
//
// Confidence: PROVEN for every arm transcribed; the level-0 fixture has no
// ax73 records (packs 8/9/11/12 only) so tests construct synthetic ones.

    /**
     * `i.aE()` (i.java:9144-9185, proven): kill-touch — only while the
     * entity is mid-tumble (`j==6`), only into a flying/falling player
     * (`aS.S∈{24,22,43,150,35,157}`), and only when the player's feet
     * sit above own mid-line (`aS.W[3] < (W[1]+W[3])>>1`). Within 20
     * cells of the apex marker `g.y` → the flying-kill: `g.x[1]=0`,
     * `aS.h(1)`, player vel0, `aS.al=al`, `i(20)`. Past it → the bounce:
     * `aS.i(89)`, own vel0, player snapped onto own top edge + `O/N`
     * refresh. Always returns false.
     */
    private fun killTouch(e: Entity, w: LevelCellSource, p: Entity): Boolean {
        if (e.j != 6) return false
        if (p.S != 24 && p.S != 22 && p.S != 43 &&
            p.S != 150 && p.S != 35 && p.S != 157) return false
        if (p.W[3] >= ((e.W[1] + e.W[3]) shr 1)) return false
        if ((e.al - p.gy) / 20 < 20) {
            // <20 cells past the apex → the bounce (L22): the tumbler
            // lands the player onto its own top edge
            p.setAnim(89)
            e.ah = 0; e.ag = 0
            p.ah = 0; p.ag = 0
            p.al = e.W[1]
            p.ak = (e.W[0] + e.W[2]) shr 1
            p.O = p.al shl 8
            p.N = p.ak shl 8
        } else {
            // ≥20 cells → the flying kill: victim plummets
            p.x1 = 0                                        // g.x[1] = 0
            p.requestH(1, w)                                // aS.h(1)
            p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
            p.al = e.al
            e.setAnim(20)
        }
        return false
    }

private val BU73 = intArrayOf(300, 400, 500)
private val BW73 = intArrayOf(80, 80, 80)
private val IH73 = intArrayOf(20, 20, 20)

/** `case 73 → L120 → (Z[0]!=1 && ax==73) → L134` (i.java:2727/3038-3088,
 *  proven): every shipped ax73 record carries r8[10]=0 → `Z=int[1]`
 *  minimal — here `Z` is fixed IntArray(22), so "minimal" = Z[0]=r8[4]
 *  with the rest 0. That reproduces the J2ME OOB→0 reads the S152 ambush
 *  arm relies on (inferred reconciliation — the original would AIOOBE on
 *  Z[14] for a true 1-element Z, so shipped records must never arm the
 *  Z[14] paths; zeroed fields give exactly that). */
fun NpcFsm.initAx73(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(17)                                       // az = r8[17]
    e.aB = BU73.getOrElse(e.au) { BU73[0] }             // aB = bu[au]
    e.Z.fill(0)
    e.Z[0] = rf(4)                                      // L134: Z[0]=r8[4]
    e.setAnim(rf(5))                                    // L395: i(r8[5])
    e.refreshBoxes()                                    // L427: t()
}

/** `i.d()` (i.java:1466, proven) — awareness tier for ax73. 0 = unaware,
 *  6/7 = contact-alerted, 4/3/1 = sight tiers by |dx|. The g.a grapple
 *  sub-arm and aA&256 upgrade are shared (not ax73-specific). */
private fun awareness73(e: Entity, w: LevelCellSource, p: Entity): Int {
    if (e.aB <= 0) return 0                               // P()
    if (w.gG()) return 0                                  // g.g() dead player
    if (p.S == 268 || p.S == 267 || p.S == 291) return 0
    if ((p.aA and 8) != 0) return 0                       // player scripted
    var r5 = p.ak - e.ak
    var r6 = p.al - e.al
    if (Entity.overlapStrict(p.W, e.W) || Entity.overlapStrict(p.X, e.W)) {
        // L25: grapple-latched far-below player doesn't alert.
        val ga = p.ga
        if (ga != null && ga.ax == 15 && Math.abs(r6) > 15) return 0
        if ((p.aA and 256) != 0) { p.aA = p.aA and -257; p.aA = p.aA or 16 }
        return 6
    }
    if (e.Z[12] <= p.W[1]) return 0                       // sight bottom above player top
    if (e.Z[11] >= p.W[3]) return 0                       // player below sight rect
    val facing = r5 <= 0                                  // r1: player left = true
    if (e.av != facing) return 0                          // must face the player
    if (r5 < 0) r5 = -r5
    if (r6 < 0) r6 = -r6
    if (r6 <= 15) {
        if (r5 <= 55) return 4
        if (e.aA == 6) return 7
    }
    if (r5 <= 100) return 3
    if (r5 <= 180) return 1
    return 0
}

/** `i.e()` (i.java:1539, proven): recompute the absolute sight rect
 *  Z[9..12] from stored offsets Z[15..18]. Minimal records → degenerate. */
private fun sightRect73(e: Entity) {
    e.Z[9] = e.ak + e.Z[15]
    e.Z[11] = e.al + e.Z[16]
    e.Z[10] = e.ak + e.Z[15] + e.Z[17]
    e.Z[12] = e.al + e.Z[16] + e.Z[18]
}

/** `i.l()` ax73 arm (i.java:2255-2330 L62-L68 + shared tail L83-L101,
 *  proven): contact → true; else point-in-sight-rect + LOS + player not
 *  in S284/285; `aS.aA&8` blinds; `ai()` (camera-lock marker ax10 S52)
 *  fast-path true. */
private fun sightCheck73(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (w.kAe?.ax == 10 && w.kAe?.S == 52) return true    // ai()
    if ((p.aA and 8) != 0) return false                   // scripted → blind
    val r0: Boolean
    if (Entity.overlapStrict(p.W, e.W)) {
        r0 = true
    } else {
        // sight rect rel extents: Z[10]-ak (right) or ak-Z[9] (left)
        val r11 = e.Z[11] - e.al                          // top extent
        val r12 = if (e.av) e.ak - e.Z[9] else e.Z[10] - e.ak
        val r13 = e.Z[12] - e.al                          // bottom extent
        val r = intArrayOf(
            e.ak + if (e.av) -r12 else 0,
            e.al + r11,
            e.ak + if (e.av) 0 else r12,
            e.al + r13)
        r0 = Entity.pointInBox(p.ak, (p.W[1] + p.W[3]) shr 1, r)
    }
    if (!r0) return false
    if (e.losBlocked(p, w)) return false                  // e(aS)==true → blocked
    if (p.S == 284 || p.S == 285) return false
    return true
}

/** `i.b(i)` (i.java:1546, proven): engage gate — player not in a
 *  cutscene anim, not mounted (`g.j`), clear Bresenham LOS. */
private fun canEngage73(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (p.S == 268 || p.S == 267 || p.S == 291) return false
    if (w.gj) return false                                // g.j mounted
    return !e.losBlocked(p, w)
}

/** `i.h()` ax73 arm (i.java:1735 L29-L32, proven): counter window opens
 *  only against player anim 69. */
private fun counterWindow73(e: Entity, p: Entity): Boolean {
    if (p.S == 216 || p.S == 217) return false
    return p.S == 69                                      // L29-L32
}

/** `i.i()` ax73 arm (i.java:1767 L17, proven): player attackbox non-
 *  degenerate + overlapping my W + mid-attack → player i(8) + E.P|=128 +
 *  my counter stance i(167) with aC=16. Returns false always (L27). */
private fun counterStrike73(e: Entity, w: LevelCellSource, p: Entity) {
    if (p.X[0] == p.X[2]) return                          // degenerate X
    if (!Entity.overlapStrict(e.W, p.X)) return
    if (!(p.gI in 1..2 && p.S in ATTACK_ANIMS)) return    // g.b()
    if (p.S != 8) { p.setAnim(8); w.kE?.let { it.P = it.P or 128 } }
    if (e.S != 17) { e.aC = 16; e.setAnim(167) }          // L17-L27
}

/** `i.j()` ax73 path (i.java:1803 L24-L135, proven): dist-gated engage
 *  lock (bf/aN), then finisher/heavy/sword damage and C() react. */
private fun damageIntake73(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (!(p.gI in 1..2 && p.S in ATTACK_ANIMS)) return false   // L24 g.b()
    val r02 = Math.abs(e.ak - p.ak) <= 160 && Math.abs(e.al - p.al) <= 20
    if (!r02) return false
    // L38-L48: bf/aN engage lock — armed once when player winds up S67.
    if (!w.iBf) {
        val aN = w.lockTarget
        if (aN == null || aN === e) {
            if (p.S == 67) { if (aN == null) w.lockTarget = e; w.iBf = true }
        }
    }
    if (p.X[0] == p.X[2]) return false                    // L50-L52
    if (!Entity.overlapStrict(e.W, p.X)) return false     // L52-L144
    e.av = p.ak < e.ak                                    // Q()
    when {
        p.S == 183 || p.S == 184 || p.S == 216 || p.S == 217 ->
            e.aB -= JD.getOrElse(e.au) { JD[0] }          // L61 finisher
        p.S == 286 || p.S == 287 ->
            e.aB -= IH73.getOrElse(e.au) { IH73[0] }      // L99 heavy anims
        else -> e.aB -= BW73.getOrElse(e.au) { BW73[0] }  // L98 sword
    }
    // L122 → ax!=11 → L135 → C()
    return reactOrEnrage73(e, w, p)
}

/** `i.C()` ax73 arms (i.java:1951 L13/L29/L61, proven): dead → i(164);
 *  wounded-normal (Z0==0 && aB<=bu) → ENRAGE Z0=3 + i(155) + aq=±60 +
 *  player i(8) + E.P|=128; else hit-react i(156) + sfx 13 (sword/heavy
 *  anims also knockback via g()). */
private fun reactOrEnrage73(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (e.aB <= 0) {                                      // L64 death
        e.setAnim(164); e.ah = 0; e.ag = 0; e.aj = 0; e.ai = 0
        return true
    }
    if (e.Z[0] == 0 && e.aB <= BU73.getOrElse(e.au) { BU73[0] }) {
        e.Z[0] = 3                                        // L13-L22 enrage
        e.av = p.ak < e.ak                                // Q()
        e.setAnim(155)
        e.aq = e.ak + if (e.av) -60 else 60               // retreat marker
        p.setAnim(8); w.kE?.let { it.P = it.P or 128 }
        return true
    }
    // L29-L42: hit-react — g() knockback on sword/heavy anims only.
    if (p.S == 67 || p.S == 68 || p.S == 69 || p.S == 286 || p.S == 287)
        hitKnockback73(e, w, p)
    e.setAnim(156); w.sfx(13)
    return true
}

/** `i.g()` no-arg (i.java:1678, proven): hit knockback — zero velocity;
 *  no-op on crate edge; snap to the player's attackbox edge unless a
 *  wall sits behind (e()>=12 in the facing-back cell) — then fling off. */
private fun hitKnockback73(e: Entity, w: LevelCellSource, p: Entity) {
    e.ag = 0; e.ai = 0
    if (crateEdge73(e, w)) return                         // aF()
    val behind = if (e.av) 1 else -1                      // trailing side
    if (e.e(w, e.ak / 20 + behind, e.al / 20 - 1) >= 12) return
    if (!p.av) {
        if (e.ak >= p.ak) {                               // L16-L20
            val keep = e.ak
            e.ak = p.X[2] + (e.ak - e.W[0])
            if (e.e(w, e.ak / 20, e.al / 20) == 20 ||
                e.e(w, e.ak / 20, e.al / 20) == 0) {
                e.ak = keep; e.ag = 2560; e.ai = -1280
            }
        }
    } else {
        if (e.ak <= p.ak) {                               // L27-L31
            val keep = e.ak
            e.ak = p.X[0] - (e.W[2] - e.ak)
            if (e.e(w, e.ak / 20, e.al / 20) == 20 ||
                e.e(w, e.ak / 20, e.al / 20) == 0) {
                e.ak = keep; e.ag = -2560; e.ai = 1280
            }
        }
    }
}

/** `i.aB()` ax73 arm (i.java:8852 L17-L40, proven): strike when the
 *  attackbox overlaps the player's W — S165 grab arm consumes Z[8],
 *  S146 strikes unconditionally, else skipped while the player rolls S6. */
private fun strikePlayer73(e: Entity, w: LevelCellSource, p: Entity) {
    if (e.X[0] == e.X[2]) return                          // degenerate X
    if (!Entity.overlapStrict(p.W, e.X)) return
    when {
        e.S == 165 -> {                                   // grab-drain arm
            e.releaseAe()                                 // G()
            p.applyHit(4, e.l, e, w)
            if (e.Z[8] == 0) e.Z[8] = 1
        }
        e.S == 146 -> p.applyHit(4, e.l, e, w)
        p.S == 6 -> Unit
        else -> p.applyHit(4, e.l, e, w)
    }
}

/** `i.v()` ax73 fallthrough (i.java:730-785 L85, proven): "active" iff
 *  the support box overlaps the camera rect. */
private fun onscreen73(e: Entity, w: LevelCellSource): Boolean =
    w.kAc?.let { Entity.overlapStrict(it, e.Y) } ?: false

/** `i.aF()` (i.java:9192, proven): TRAILING-side ledge probe — a crate
 *  edge (s.ax==51) within 20px of ak on either side, or the cell behind
 *  the facing direction is neither 20 (void) nor 5. */
private fun crateEdge73(e: Entity, w: LevelCellSource): Boolean {
    val s = e.s
    if (s != null && s.ax == 51) {
        if (s.W[2] > e.ak && s.W[2] - 20 < e.ak) return true
        if (s.W[0] < e.ak && s.W[0] + 20 > e.ak) return true
        return false                                       // s!=null → skip tile arms
    }
    if (s != null) return false
    if (e.av) {
        val c = e.e(w, e.W[2] / 20 + 1, (e.W[3] + 10) / 20)
        return c != 20 && c != 5
    }
    val c = e.e(w, e.W[0] / 20 - 1, (e.W[3] + 10) / 20)
    if (c == 20) return false
    return c != 5
}

/** `i.aG()` (i.java:9220, proven): FORWARD ledge probe — facing-aware
 *  crate edge, or the cell ahead of the feet is neither 20 nor 5. */
private fun edgeAhead73(e: Entity, w: LevelCellSource): Boolean {
    val s = e.s
    if (s != null) {
        if (s.ax != 51) return false
        if (e.av) {
            if (s.W[0] < e.ak && s.W[0] + 20 > e.ak) return true
        } else {
            if (s.W[2] > e.ak && s.W[2] - 20 < e.ak) return true
        }
        return false
    }
    if (!e.av) {
        val c = e.e(w, e.W[2] / 20 + 1, (e.W[3] + 10) / 20)
        return c != 20 && c != 5
    }
    val c = e.e(w, e.W[0] / 20 - 1, (e.W[3] + 10) / 20)
    if (c == 20) return false
    return c != 5
}

/** `i.am()` (i.java:7167, proven): ceiling probe — the 3 cells above the
 *  facing-adjacent column contain a solid tile (e>=12). Requires s==null. */
private fun ceilingProbe73(e: Entity, w: LevelCellSource): Boolean {
    if (e.s != null) return false
    val x0 = e.ak / 20 + if (e.av) -1 else 1
    for (r7 in 1..3) if (e.e(w, x0, e.al / 20 - r7) >= 12) return true
    return false
}

/** `i.aE()` (i.java:9144, proven): ceiling ambush — contact tier + player
 *  in {24,22,43,150,35,157} + player bottom above my mid-Y. ≥20 cells
 *  from `g.y` → drop-kill arm (x1=0, gI=1, snap to my al, i(20));
 *  else pin: player i(89) snapped to my top-center + me i(24) aC=30. */
private fun ceilingAmbush73(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (e.j != 6) return false
    if (p.S != 24 && p.S != 22 && p.S != 43 && p.S != 150 &&
        p.S != 35 && p.S != 157) return false
    if (p.W[3] >= (e.W[1] + e.W[3]) shr 1) return false
    return if ((e.al - p.gy) / 20 < 20) {
        p.setAnim(89)
        p.ah = 0; p.ag = 0
        p.al = e.W[1]
        p.ak = (e.W[0] + e.W[2]) shr 1
        p.O = p.al shl 8; p.N = p.ak shl 8
        e.setAnim(24); e.aC = 30
        true
    } else {
        p.x1 = 0                                          // g.x[1] = 0
        p.bindScript(1, w)                                // aS.h(1)
        p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
        p.al = e.al
        e.setAnim(20)
        false
    }
}

/** `i.aC()` (i.java:8893-9144, proven): the attack scheduler — `P|=16`,
 *  `k.aA=60`, face the player; `aq!=0` → leap-landing arm (ax69 bound
 *  i(138) + Z0==3 ceiling leap to i(165)); else the j-tier dispatch —
 *  0 stalk, 6/4 windup→S131, 3/1 approach→S154 via aG() edge check. */
private fun attackScheduler73(e: Entity, w: LevelCellSource, p: Entity) {
    e.P = e.P or 16
    w.kAA = 60
    e.av = p.ak < e.ak                                    // Q()
    if (e.aq != 0) {
        // L5: leap-landing arm — bound ax69 kill or Z0==3 ceiling probe.
        val af = e.af
        if (af != null && af.ax == 69 && af.aA == 1 &&
            Entity.overlapStrict(e.W, af.W)) { e.setAnim(138); return }
        val r7 = Math.abs(e.aq - e.ak); val r8 = Math.abs(e.ar - e.al)
        e.av = e.aq < e.ak                                // face the marker
        if (e.Z[0] == 3) {                                // L20-L42 leaper
            var r03 = false
            if (r7 >= 10 && Math.abs(p.ak - e.ak) < 140) {
                val x0 = e.ak / 20 + if (e.av) -1 else 1
                for (r9 in 1..3)
                    if (e.e(w, x0, e.al / 20 - r9) >= 12) { r03 = true; break }
            }
            if (r03 || crateEdge73(e, w)) return          // wall/edge → stay
        }
        // L42-L46: arrived → i(165) leap (Z0==3) or i(152) reset.
        if (r7 < 10) {
            if (e.S == 155) {
                e.ar = 0; e.aq = 0; w.kAA = 0
                e.setAnim(165); e.am = e.ak; w.gZ = false
            }
            if (e.Z[0] != 3) { e.ar = 0; e.aq = 0; w.kAA = 0; e.aA = 0; e.setAnim(152) }
        }
        return
    }
    val r7 = Math.abs(p.ak - e.ak); val r8 = Math.abs(p.al - e.al)
    when (e.j) {
        // L50: unaware → stalk the player until close, else countdown back
        // to S152 idle.
        0 -> if (r7 > 180 || r8 > 70) {
            if (e.aC == 0) { e.aC = 60; e.setAnim(155) }
            else {
                e.aC--
                if (e.aC <= 0 || !onscreen73(e, w)) {
                    e.P = e.P and -17; w.kAA = 0; e.aA = 0
                    e.setAnim(152)                        // L72 ax73 arm
                }
            }
        }
        6 -> {                                            // L76 contact tier
            e.setAnim(155)
            val was = e.aC; e.aC = was - 1
            if (was <= 0) e.setAnim(131)                  // c(11,131) block
        }
        4 -> {                                            // L82 close tier
            if (++e.l <= 0) e.l = 1
            val was = e.aC; e.aC = was - 1
            if (was >= 0) e.setAnim(155) else e.setAnim(131)
        }
        3 -> {                                            // L93 mid tier
            val was = e.aC; e.aC = was - 1
            if (was <= 0 && !edgeAhead73(e, w)) e.setAnim(154)
        }
        1 -> if (!edgeAhead73(e, w)) e.setAnim(154)       // L101 far tier
    }
}

/** `i.c(int,int)` (i.java:9840, proven): spawn/position the clip-74 `ae`
 *  marker (ax14, S0, az302) and latch `i.L`/`i.M`. */
private fun markerSpawn74(e: Entity, w: LevelCellSource, x: Int, y: Int) {
    val m = e.ae ?: Entity(14, w.clipFor(74)).also { m ->
        m.setAnim(0); m.az = 302; m.au = 0
        e.ae = m
    }
    m.ak = x; m.al = y
    m.refreshBoxes()                                      // t()
    w.iL = x; w.iM = y                                    // o(r5,r6)
}

/** `i.d(int,int)` (i.java:9861, proven): reposition the marker; when
 *  idle (T) re-latch `L`/`M`; flip anim 1 while the touch point is within
 *  70px of its screen position. */
private fun markerMove74(e: Entity, w: LevelCellSource, x: Int, y: Int) {
    val m = e.ae ?: return
    m.ak = x; m.al = y
    if (markerIdle74(e, w)) { w.iL = x; w.iM = y }
    m.setAnim(if (m.markerTouched(w)) 1 else 0)
}

/** `i.T()` (i.java:9878, proven): marker is the clip-74 indicator in a
 *  rest anim (S0/S1). */
private fun markerIdle74(e: Entity, w: LevelCellSource): Boolean {
    val m = e.ae ?: return false
    return m.ax == 14 && (m.S == 0 || m.S == 1)
}

/** `i.V()` (i.java:9902, proven): touch point within 70px of the
 *  marker's screen position. */
private fun markerTapped74(e: Entity, w: LevelCellSource): Boolean {
    val m = e.ae ?: return false
    return w.touchNearView(m, 70)                          // k.a(H,I,ak-O,al-P,70)
}

/** `i.n(44,0)` (i.java:9792, proven): any open-door corridor (ax44 S==0
 *  with an X box) overlapping my W. */
private fun doorScan73(e: Entity, w: LevelCellSource): Boolean {
    for (n in w.npcs) {
        if (n.ax == 44 && n.S == 0 && n.X.let { it[0] != it[2] } &&
            Entity.overlapStrict(e.W, n.X)) return true
    }
    return false
}

/** `i.g(int,int)` (i.java:6881, proven): press-gauge — `k.v(mask)` press
 *  edges +8, decay −1 per tick, clamped [0,80]; true at 80. */
private fun pressGauge73(e: Entity, w: LevelCellSource, mask: Int): Boolean {
    if (w.padHeld(mask)) e.bl += 8 else e.bl--
    if (e.bl < 0) e.bl = 0
    if (e.bl >= 80) { e.bl = 80; return true }
    return false
}

/** `k.h` (k.java:6839, proven): Manhattan-ish distance
 *  `(|dx|+|dy|) - m/2 - m/4 + m/8`, m = min(|dx|,|dy|). */
private fun kDist73(dx: Int, dy: Int): Int {
    if (dx == 0 && dy == 0) return 0
    val a = Math.abs(dx); val b = Math.abs(dy)
    val m = if (a <= b) a else b
    return (a + b) - (m shr 1) - (m shr 2) + (m shr 3)
}

/** `k.l()` (k.java:804, proven): the 60×60 reach box in front of the
 *  player — `k.M`. */
private fun reachRect73(p: Entity): IntArray {
    val x = if (p.av) p.ak - 60 else p.ak
    return intArrayOf(x, p.al - 60, x + 60, p.al)
}

/** `i.P()` (i.java:7699, proven): dead → release the marker. */
private fun heavyDead73(e: Entity): Boolean =
    if (e.aB <= 0) { e.releaseAe(); true } else false

/** `i.aJ()` (i.java:9331-9905, proven): the heavy-guard tick. */
fun NpcFsm.tickAx73(e: Entity, w: LevelCellSource, p: Entity) {
    // -- head: P() or out-of-reach → L8 release claim; in reach → claim --
    if (heavyDead73(e) || !Entity.overlapStrict(e.W, reachRect73(p))) {
        if (w.kL?.aw == e.aw) w.claimReset()               // k.m()
    } else {
        w.registerClaim(e, 0, e.W)                         // k.a(this,0,W)
    }
    var r10 = true; var r11 = false
    if (e.aB <= 0 && e.S != 164) e.setAnim(164)            // L12-L21
    if (!p.faces(e)) r11 = true                            // g(aS)==false
    e.j = awareness73(e, w, p)                             // j = d()
    sightRect73(e)                                         // e()
    when (e.S) {
        131 -> {                                           // L106 block/counter
            e.ah = 0; e.ag = 0
            if (Entity.overlapStrict(p.W, e.X) && p.faces(e) && p.S == 6) {
                p.applyHit(34, 0, e, w); w.lockTarget = e
            }
            if (e.animFinished()) { e.setAnim(146); return }
        }
        146 -> { e.ah = 0; e.ag = 0                        // L117
            if (e.animFinished()) e.setAnim(171)
        }
        147 -> {                                           // L201 grab-QTE hold
            if (markerTapped74(e, w)) e.bl += 9            // V()→bl+=9
            if (pressGauge73(e, w, 65568)) {               // g(65568,80)
                e.bl = 0; e.releaseAe(); e.setAnim(164)
                p.setAnim(287); e.aB = 0; w.iBx = null
            } else if (e.bl == 0) {                        // timeout release
                e.setAnim(149); w.grabResolve(p); e.releaseAe(); w.iBx = null
            }
        }
        148 -> {                                           // L209 throw
            r11 = false; r10 = false
            if (e.animFinished()) e.setAnim(151)
        }
        149 -> {                                           // L212
            r11 = false; r10 = false
            if (e.animFinished()) {
                e.aq = e.ak + if (e.av) -60 else 60
                e.setAnim(155)
            }
        }
        152 -> {                                           // L32 idle-reset
            e.releaseAe(); e.ah = 0; e.ag = 0; e.Z[0] = 0
            if (!e.cq) { if (canEngage73(e, w, p)) e.aA = 1 } else r11 = false
            if (e.Z[14] == 1 || e.Z[14] == 6 || e.Z[14] == 7)
                if (ceilingAmbush73(e, w, p)) r10 = false
        }
        153, 154 -> {                                      // L48 chase
            if (edgeAhead73(e, w)) { e.aC = 10; e.setAnim(155) }
            else {
                e.wallProbe(w)                             // a(true)
                r11 = true; r10 = false
                e.ag = if (e.S == 153) (if (e.av) -2048 else 2048)
                       else (if (e.av) -512 else 512)
                if (Entity.overlapStrict(p.W, e.W) && e.af == null) {
                    e.aC = 3; e.setAnim(155)
                }
                attackScheduler73(e, w, p)
            }
        }
        155 -> {                                           // L68 attack commit
            e.wallProbe(w)
            e.ab?.deactivate(); e.ab = null                // H()
            if (e.Z[0] == 3) {
                e.ag = if (e.av) 1536 else -1536
                r11 = false; r10 = false
            } else {
                e.ag = if (e.av) 512 else -512
                r11 = true
            }
            attackScheduler73(e, w, p)
            if (crateEdge73(e, w)) { e.ai = 0; e.ag = 0 }  // L81 aF()
        }
        156 -> {                                           // L83 leap
            e.releaseAe(); e.wallProbe(w)
            if (e.ag != 0) e.ai = if (e.av) -1280 else 1280
            if (e.T == 1) {
                e.spawnFx8(w, 50, 1, e.av, e.ak, e.al - 40, 300)
                if (w.kBK) e.spawnFx8(w, 59, 0, e.av, e.ak, e.al - 40, 300)
            }
            e.aA = 1
            if (e.animFinished()) e.setAnim(155)
        }
        157 -> {                                           // L97 stagger step
            e.releaseAe(); e.wallProbe(w); e.ah = 0; e.ag = 0
            if (e.animFinished()) e.setAnim(158)
        }
        158 -> if (e.animFinished()) {                     // L104
            e.aC = 20; e.setAnim(155)
        }
        165 -> {                                           // L147 grab approach
            if (e.animFinished()) e.P = e.P or 64
            r11 = false
            if (!p.faces(e)) {                             // L152-L156
                if ((e.av xor p.av) && Math.abs(p.al - e.al) < 10)
                    markerSpawn74(e, w, p.ak, p.al - 85)   // c()
            }
            markerMove74(e, w, p.ak, p.al - 85)            // d()
            e.ag = if (e.av) -2560 else 2560
            if (p.faces(e)) e.releaseAe()                  // L163 G()
            val r02 = kDist73(p.ak - e.ak, p.al - e.al)
            val grab = ((e.av xor p.av) &&
                Entity.overlapStrict(p.W, e.X) && p.aZ &&
                w.padDown(16388) &&                       // k.u held
                Math.abs(p.al - e.al) < 10 &&
                e.Z[8] == 0 && r02 >= 60) || markerTapped74(e, w)
            if (grab) {                                    // L179
                p.setAnim(294); e.P = e.P and -65; e.setAnim(147)
                e.ah = 0; e.ag = 0; p.ah = 0; p.ag = 0
                e.bl = 40; e.Z[8] = 0; w.iBx = e
                e.ak = if (e.av) p.ak + 80 else p.ak - 80
                return
            }
            // L178-L199 abort chain: overshot (facing-side ≥140 past the
            // player) → L199; else L194: wandered ≥140 off the latch `am`
            // or a ledge ahead → L199; `am()` clear → L234 (keep walking);
            // `am()` blocked → L159/L160 re-arm `ag=2560` (verbatim — the
            // decompiled L159 sets +2560 regardless of facing).
            var abort = when {
                e.av && e.ak - p.ak <= -140 -> true
                !e.av && e.ak - p.ak >= 140 -> true
                else -> false
            }
            if (!abort) {
                if (Math.abs(e.am - e.ak) > 140) abort = true
                else if (edgeAhead73(e, w)) abort = true
                else if (ceilingProbe73(e, w)) e.ag = 2560
            }
            if (abort) {                                   // L199
                e.P = e.P and -65; e.setAnim(171); e.Z[8] = 0
                e.releaseAe()
            }
        }
        167 -> { e.ah = 0; e.ag = 0                        // L120 counter stand
            if (e.animFinished()) { e.aC = 20; e.setAnim(155) }
        }
        171 -> {                                           // L123 knockdown-rise
            e.ah = 0; e.ag = 0; r11 = true
            if (e.animFinished()) {
                if (e.Z[0] != 3) { e.aC = 20; e.setAnim(155) }   // L126
                else if (p.W[1] > e.Z[12] || p.W[3] < e.Z[11]) {
                    e.setAnim(152); e.aA = 0; return       // L135 disengage
                } else if (edgeAhead73(e, w) && p.faces(e)) {
                    // L134→L209: ledge + player visible → the S148
                    // codepath (r11=r10=false; anim → i(151)).
                    r11 = false; r10 = false
                    e.setAnim(151)
                } else {
                    e.av = p.ak < e.ak                     // L137 Q()
                    if (ceilingProbe73(e, w)) e.av = !e.av // L140 flip
                    e.setAnim(165); e.am = e.ak; w.gZ = false // L144
                }
            }
        }
        190 -> { e.ah = 0; e.ag = 0 }                      // L25 dormant idle
        191 -> if (e.animFinished()) e.setAnim(192)        // L27
        192 -> if (e.animFinished()) e.setAnim(190)        // L30
        164 -> {                                           // L220 death
            if (w.kBK && e.T == 3)
                e.spawnFx8(w, 59, 0, e.av, e.ak, e.al - 40, 300)
            if (e.animFinished()) {
                if (w.kBK) e.spawnFx8(w, 59, 2, e.av, e.ak, e.al, e.az - 1)
                e.P = e.P and -17; e.P = e.P or 32; e.P = e.P or 64
                if ((p.gJ and 2) == 0) {
                    p.gJ = p.gJ or 2                       // g.g(2)
                    p.bindScript(2, w)                     // aS.h(2)
                }
            } else return
        }
    }
    // -- L234 tail ------------------------------------------------------
    if (e.Z[0] != 3 && counterWindow73(e, p)) counterStrike73(e, w, p)
    if (r11 && e.Z[0] != 3 && damageIntake73(e, w, p)) return
    if (r10) strikePlayer73(e, w, p)
    if (e.aA == 0) {
        e.P = e.P and -17
        if (p.aA > 2 && sightCheck73(e, w, p)) p.applyHit(32, 0, e, w)
    } else {
        e.P = e.P or 16
        if ((p.aA and 1) != 0 && p.faces(e) && e.j != 0)
            p.applyHit(32, 0, e, w)
    }
    if ((p.aA and 8) != 0) return
    if (e.S == 165) return
    if (p.S == 6) return
    if (e.Z[0] == 3 && e.S == 171) return
    e.pushContact(w)
}

// =====================================================================// Slice 64 — ax47 `aK()` ledge sentinel + ax50 `aL()` pouncer + the shared
// stealth-kill driver `i.k()` (i.java:2057-2255) both FSM tails funnel into.
//
//  aK() i.java:9910-10007 — S120 perch (ceiling-grab on the player drop-kill
//      set → S119; else l() seen → 3x3 quadrant pounce pick 121-128);
//      S121-128 pounce (T==1 sfx16, T==3 op4 on player, r()→S120);
//      S119/S129/default → k();j();  S130 r()→k.c(this) despawn.
//  aL() i.java:10008-10099 — S80/93 perch (93 = ceiling-grab variant +
//      registerClaim), S81 countdown→S82→S80, S83 air-walk via M() → S84 →
//      S0, S94 P|=512 + claim-release → despawn, default → k();j().
//  k()  i.java:2057-2255  — backstab window L81 (player faces me, I don't
//      face player, <80px, S38 split): prompt k.c(ak,al-85,aw) + 65568 edge
//      → op6 victim anim (49 mid / 283 near); ax11 ceiling-kill arm (S24+
//      j==6+p.S89); ax47 arm (S80+p.S89 → i94); ax50 arm (S119+p.S89 →
//      i130). ax47/50 get r11=21 set but their L172 tail is `else→true`
//      — the sentinel plays no anim on the L81 backstab (verbatim quirk).
//  j()  i.java:1803-1955  — shared intake; ax47/50 take the L24 path →
//      L50 X-overlap → aB -= J[au] (finishers) else H[au] (50) → C().
//  C()  i.java:1956-2053  — ax47/50 arms: survive → return true with no
//      react anim; dead → ax50 i(129), ax47 nothing (verbatim).
//  init i.java:3082 L134 (ax47 `az=r8[17]`) / :3017 L114 (ax50 `az=r8[13]`)
//      — minimal inits; records arrive via retype ax11+S∈{80,93}→47 and
//      ax17+S==120→50 (i.java:2644/2651) applied in Level0World.initNpcs.
//  l()  i.java:2255 — ax47 → L83 default arm (r11/r12/r13 stay 0 →
//      degenerate [ak,al,ak,al] sight rect = point-pierce only); ax50 →
//      L77 `bn→false; b(W,k.ac)` W⊆cam rect; shared L88-L101 tail
//      (LOS clear + player ∉ {284,285}).
//  M()/h() i.java:7198/7207 — facing-adjacent cell >=5 (floor-ahead probe).
// =====================================================================
private val HDM47 = intArrayOf(50, 50, 50)        // i.H counter line (:22315)
private val DROP_KILL = intArrayOf(24, 22, 43, 150, 35, 157)
private val KILL_EXIT = intArrayOf(268, 267, 271, 270, 291)
private val KILL_SKIP = intArrayOf(203, 204, 310, 311)
private val KILL_ANIM = intArrayOf(49, 283, 357, 360)
private val KILL_HOLD = intArrayOf(203, 89, 271, 297)

/** `case 47 → L134` (i.java:3082, proven): `az=r8[17]; aB=bu[au];
 *  Z[0]=r8[4]; i(r8[5])` + shared `t()` tail. Records reach ax47 only via
 *  the `ax11 && r8[5]∈{80,93} → ax=47` retype (i.java:2644) applied in
 *  Level0World.initNpcs before dispatch. Minimal-Z semantics identical to
 *  initAx73 (inferred — fixed IntArray(22) zero-reads). */
fun NpcFsm.initAx47(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(17)
    e.aB = BU73.getOrElse(e.au) { BU73[0] }
    e.Z.fill(0)
    e.Z[0] = rf(4)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `case 50 → L114` (i.java:3017, proven): `az=r8[13]; aB=bu[au];
 *  Z[0]=r8[4]; i(r8[5])` — same minimal shape, different az index.
 *  Records reach ax50 via `ax17 && r8[5]==120 → ax=50` (i.java:2651). */
fun NpcFsm.initAx50(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(13)
    e.aB = BU73.getOrElse(e.au) { BU73[0] }
    e.Z.fill(0)
    e.Z[0] = rf(4)
    e.setAnim(rf(5))
    e.refreshBoxes()
}

/** `i.l()` ax47 arm (i.java:2255-2340, proven): `ai()` fast-path → true;
 *  `aA&8` → false. L83 default arm: r11/r12/r13 stay the head-initialized
 *  zeros (only ax11's arm populates them) → sight rect degenerates to
 *  `[ak,al,ak,al]` — a point-pierce check on the player mid-point. Then
 *  the shared tail: LOS clear (e(aS)==false) + player ∉ {284,285}. */
private fun seen47(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (w.kAe?.ax == 10 && w.kAe?.S == 52) return true      // ai()
    if ((p.aA and 8) != 0) return false                   // L10 → r0=false
    val r0 = Entity.pointInBox(
        p.ak, (p.W[1] + p.W[3]) shr 1,
        intArrayOf(e.ak, e.al, e.ak, e.al))
    if (!r0) return false                                 // L88
    if (e.losBlocked(p, w)) return false                  // L90
    if (p.S == 284) return false                          // L92
    return p.S != 285                                     // L94-L101
}

/** `i.l()` ax50 arm (i.java:2395 L77, proven): `bn→false` (iBn kill-
 *  disable); else `b(W,k.ac)` — my W strictly inside the camera rect
 *  (i.java:666: `r4[0]>=r5[0] && r4[1]>=r5[1] && r4[2]<=r5[2] &&
 *  r4[3]<=r5[3]` — proper containment, W ⊆ ac). Then the shared tail. */
private fun seen50(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (w.kAe?.ax == 10 && w.kAe?.S == 52) return true      // ai()
    if ((p.aA and 8) != 0) return false
    if (w.iBn) return false                               // L77 bn gate
    val ac = w.kAc
    val r0 = ac != null &&
        e.W[0] >= ac[0] && e.W[1] >= ac[1] &&
        e.W[2] <= ac[2] && e.W[3] <= ac[3]
    if (!r0) return false
    if (e.losBlocked(p, w)) return false
    if (p.S == 284) return false
    return p.S != 285
}

/** `i.M()` + `i.h(x,y)` (i.java:7198/7207, proven): cell facing-adjacent
 *  (`ak/20 ± 1` by `av`, row `al/20`) ≥5 = "floor ahead" — S83's landing
 *  probe. `av==false → +1` (right), `av==true → -1` (left). */
private fun floorAheadM(e: Entity, w: LevelCellSource): Boolean =
    e.e(w, e.ak / 20 + if (e.av) -1 else 1, e.al / 20) >= 5

/** `i.S()` (i.java:9253, proven): `repeat(3){ m(-1); k.o(5); k.s() }` —
 *  wisp burst + stat + shake, shared by every k() kill arm. */
private fun wispBurst(e: Entity, w: LevelCellSource) {
    repeat(3) { w.spawnWisp(e); w.kAp[5]++; w.kCollectStreak() }
}

/**
 * `i.I()` S18 arm (i.java:5620-5655, L506-L529/L513-L527, proven): the
 * weakened finisher-offer. `a(true)` then a label machine:
 *  - entry: `Z0==2 → L513`; `Z0!=0 → L529`; `aB<=bu[au]/2 → L513`; else L529.
 *  - L513: `Z0==2 && T==3 → c(2)`; → L518.
 *  - L518: `aN!=this → L529`; `g.E=false`; `!(aS.aZ && v(65568)) → L529`;
 *    else `aS.i(183|184)` (|nextInt|%2) then L527 `k.o();G();g.E=false;O()`
 *    → L529.
 *  - L529: `r()==false` → exit to L777; else `g.E=false;i(23);G();O()` and
 *    falls through into L513 (one more offer pass; exits via L777 since the
 *    fresh clip's r() is false, or re-arms the finisher edge).
 */
private fun grabOfferArm18(e: Entity, p: Entity, w: LevelCellSource) {
    e.collideSides(w, true)                              // a(true)
    var label = when {
        e.Z[0] == 2 -> 513
        e.Z[0] != 0 -> 529
        e.aB <= BU73.getOrElse(w.weaponSlot) { BU73[0] } / 2 -> 513
        else -> 529
    }
    while (label != 777) {
        when (label) {
            529 -> {
                if (!e.animFinished()) label = 777
                else {
                    Entity.gE = false; e.setAnim(23)
                    e.releaseAe(); e.eventDisarm(w)
                    label = 513                        // L529 → L513
                }
            }
            513 -> {
                if (e.Z[0] == 2 && e.T == 3) w.tutorialHint(2)   // c(2)
                label = 518
            }
            518 -> {
                if (w.lockTarget !== e) { label = 529; continue }
                Entity.gE = false
                if (!p.aZ || !w.padHeld(65568)) { label = 529; continue }
                p.setAnim(if (abs(w.jNextInt()) % 2 == 0) 183 else 184)
                e.lockInput(w); e.releaseAe()                   // L527
                Entity.gE = false; e.eventDisarm(w)
                label = 529
            }
        }
    }
}

/**
 * `i.I()` S175 arm (i.java:5864-5906, L329-L347, proven): the grab-hold
 * mash-QTE — player dead (`g.g()`) → i(177) release; else prompt marker
 * `a(8, aS.ak, aS.al-85)` + `g(65568,80)` mash gauge: FILLED → the counter-
 * execute (`bl=0; G(); aB=0; i(176); k.A(24); S() wisps; aS.i(311);
 * aN=null; az=100; k.o(); bx=null`); EMPTY (`bl==0`) → i(177) + aS.i(312)
 * throw + `aS.ag=∓1280` (aS.av polarity); MID → L341: hold while
 * `aS.S∈{310,311,312}`, else i(177)+G()+az=100+bx=null. All paths → L849.
 */
private fun grabHoldArm175(e: Entity, p: Entity, w: LevelCellSource) {
    if (w.playerDead()) {                                // L329 g.g()
        e.setAnim(177); e.releaseAe(); e.az = 100
        return
    }
    p.spawnMarker(w, 8, p.ak, p.al - 85)                 // L331 a(8,·)
    if (e.mashGauge(65568, w)) {                         // g(65568,80) filled
        e.bl = 0; e.releaseAe(); e.aB = 0                // L331
        e.setAnim(176); w.sfx(24)                        // k.A(24)
        wispBurst(e, w)                                  // S()
        p.setAnim(311)
        w.lockTarget = null                              // aN = null
        e.aB = 0; e.az = 100; e.lockInput(w)             // k.o()
        w.iBx = null
    } else if (e.bl == 0) {                              // L335 empty gauge
        e.setAnim(177); e.releaseAe(); p.setAnim(312)
        e.az = 100; w.iBx = null
        p.ag = if (p.av) 1280 else -1280                 // throw fling
    }
    // L341: hold while the player sits in a grab state, else release.
    if (p.S == 310 || p.S == 311 || p.S == 312) return   // → L777 hold
    e.setAnim(177); e.releaseAe(); e.az = 100; w.iBx = null
}

/** `i.k()` (i.java:2057-2255, proven): the shared stealth-kill driver.
 *  Head exits: `g.g()` / `aH()` (S∈{0,20,21,106,107,117,139,168,169,176})
 *  / player S297 / own S175 → false. The player-anim exit set {268,267,
 *  271,270,291} + `bn` → `G()+k.k(aw)` (release ae + prompt) then false.
 *  ax11 Z[8]==999 → false (chase-director entities are not killable).
 *
 *  Distance gate L63: `|p.ak-ak|>=80 || |dy|>=r9 (5, or 20 for p.S==38)`
 *  or p.S∈{203,204,310,311} → L120 (G() unless S∈{203,89,271,297}) → L129.
 *  L81 backstab window: `p.faces(me) && !me.faces(p)` → prompt + edge:
 *  kill → `r11=21`, S38 face-sync, `S()` burst, op6 victim anim
 *  (49 when `40<|dx|<80` or p.S==38; 283 when |dx|<40; |dx|==40 skips).
 *  L129 per-ax grab-kill arms + L172 tail (`ag=0; ax11→i(r11)`;
 *  `ax17→i(68)+sfx13`; else `true`). */
private fun contextK(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (w.gG()) return false                              // L7
    if (e.S in intArrayOf(0, 20, 21, 106, 107, 117, 139, 168, 169, 176))
        return false                                      // aH()
    if (p.S == 297) return false                          // L13
    if (e.S == 175) return false                          // L17
    if (p.S in KILL_EXIT || w.iBn) {                      // L19-L28
        e.releaseAe(); w.clearPrompt(e.aw); return false
    }
    if (e.ax == 11 && e.Z[8] == 999) return false         // L33
    val r0 = p.ak - e.ak
    var r8 = p.W[3] - e.al
    var r9 = 5
    if (p.S == 38) { r8 = p.W[1] - e.al; r9 = 20 }        // L40
    val r10 = Math.abs(r0)
    val r82 = Math.abs(r8)
    var r11 = -1
    if ((p.aA and 8) != 0 && w.iBn) return false          // L47-L54
    if (p.S == 284) return false                          // L54
    // L58-L61: player not facing me → release my marker link.
    if (!p.faces(e)) e.releaseAe()
    // L63-L120: out-of-range / busy-state arm → marker clear then L129.
    val outOfRange = r10 >= 80 || r82 >= r9 || p.S in KILL_SKIP
    val facingBlocked = !p.faces(e) || e.faces(p)
    if (outOfRange || (facingBlocked && p.S == 38)) {     // L79/L120
        if (p.S !in KILL_HOLD) e.releaseAe()
    } else if (!facingBlocked) {
        // L81 backstab window — player behind, within reach.
        if (e.j == 0 && p.S !in KILL_ANIM) {
            w.showPrompt(e.ak, e.al - 85, e.aw)           // k.c(x,y,aw)
            if (w.padHeld(65568)) {                       // k.v edge
                w.clearPrompt(e.aw); e.releaseAe(); e.ab = null
                r11 = 21
                if (p.S == 38) {                          // L95 face-sync
                    if (p.ak - e.ak > 0) p.av = true
                    else if (p.ak - e.ak < 0) p.av = false
                    e.av = p.av
                }
                wispBurst(e, w)                           // S()
                // L101: 40<|dx|<80 (or mounted) → op6 anim 49; |dx|<40 →
                // 283; |dx|==40 with S!=38 → L129 (skips — verbatim).
                val anim = when {
                    p.S == 38 -> 49                       // L106→L107
                    r10 in 41 until 80 -> 49
                    r10 < 40 -> 283
                    else -> -1
                }
                if (anim != -1) {
                    val snapX = e.ak + if (r0 >= 0) -20 else 20
                    p.applyHit6(anim, snapX, e, w)        // a(6,anim,x,this)
                }
            }
        }
    }
    // L129 chain — per-ax grab-kill offers (each: prompt + 65568 edge).
    if (e.ax == 11 && e.j == 6 && p.S == 89 && e.S == 24) {
        w.showPrompt(e.ak, e.al - 85, e.aw)
        if (w.padHeld(65568)) {
            e.aA = 2; e.aB = 0
            w.kN?.setAnim(55); w.kN?.let { it.P = it.P and -65 }
            p.consumeH()
            r11 = 20
            wispBurst(e, w)
            p.applyHit6(90, e.ak, e, w)
            when (e.Z[14]) { 6 -> w.gP = 1; 7 -> w.gP = 2 }
        }
    }
    // L151 ax47: player grabbed from below (S89) while I perch (S80).
    if (e.ax == 47 && p.S == 89 && e.S == 80) {
        w.showPrompt(e.ak, e.al - 85, e.aw)
        if (w.padHeld(65568)) {
            e.aA = 2; e.aB = 0
            e.setAnim(94)
            w.kN?.setAnim(55); w.kN?.let { it.P = it.P and -65 }
            p.consumeH()
            p.applyHit6(90, e.ak, e, w)
            w.gP = e.Z[0]
        }
    }
    // L160 ax50: same offer while I perch at S119.
    if (e.ax == 50 && p.S == 89 && e.S == 119) {
        w.showPrompt(e.ak, e.al - 85, e.aw)
        if (w.padHeld(65568)) {
            e.aA = 2; e.aB = 0
            e.setAnim(130)
            w.kN?.setAnim(55); w.kN?.let { it.P = it.P and -65 }
            p.consumeH()
            p.applyHit6(90, e.ak, e, w)
            w.gP = e.Z[0]
        }
    }
    // L169-L182 tail.
    if (r11 == -1) return false
    e.ag = 0
    return when (e.ax) {
        11 -> { e.setAnim(r11); true }
        17 -> { e.setAnim(68); w.sfx(13); true }
        else -> true                                      // 47/50/23
    }
}

/** `i.j()` for ax47/50 (i.java:1803-1955, proven): `P()` head (aB<=0 →
 *  releaseAe + false); `g.b()` gate; L24-L38 proximity (|dx|<=160 &&
 *  |dy|<=20) + the `aN/bf` engage latch on p.S==67; L50 X-overlap → Q()
 *  (face player) then damage — `aS.S∈{183,184,216,217}` → J=100 else
 *  L102-L120 → `aB -= H[au]` (50; S67 re-runs Q()). `C()` arm:
 *  survived → `true` with no react; dead → ax50 `i(129)`, ax47 none. */
private fun damageIntakeSentinel(e: Entity, w: LevelCellSource, p: Entity): Boolean {
    if (e.aB <= 0) { e.releaseAe(); return false }        // P()
    if (!(p.gI in 1..2 && p.S in ATTACK_ANIMS)) return false   // g.b()
    val r02 = Math.abs(e.ak - p.ak) <= 160 && Math.abs(e.al - p.al) <= 20
    if (!r02) return false
    if (!w.iBf) {                                         // L38-L48 latch
        val aN = w.lockTarget
        if (aN == null || aN === e) {
            if (p.S == 67) { if (aN == null) w.lockTarget = e; w.iBf = true }
        }
    }
    if (p.X[0] == p.X[2]) return false
    if (!Entity.overlapStrict(e.W, p.X)) return false
    e.av = p.ak < e.ak                                    // Q()
    if (p.S == 183 || p.S == 184 || p.S == 216 || p.S == 217)
        e.aB -= JD.getOrElse(e.au) { JD[0] }              // L61 finisher
    else {
        if (p.S == 67) e.av = p.ak < e.ak                 // L102 re-Q()
        e.aB -= HDM47.getOrElse(e.au) { HDM47[0] }        // L120 counter line
    }
    // L135 → C(): ax47/50 arms (i.java:2024-2052).
    if (e.aB > 0) return true                             // L25→L51: survive
    if (e.ax == 50) e.setAnim(129)                        // L70 death anim
    return true                                           // ax47 → L86
}

/** `i.aK()` (i.java:9910-10007, proven): ledge-sentinel FSM — transcribed
 *  arm for arm. `r7 = l()` runs first every tick. */
fun NpcFsm.tickAx47(e: Entity, w: LevelCellSource, p: Entity) {
    var r7 = seen47(e, w, p)                              // l()
    when (e.S) {
        120 -> {
            // L6: ceiling-grab on the player drop-kill set.
            if (p.S in DROP_KILL) {
                // L18
                if (Entity.overlapStrict(p.W, e.W) && p.W[3] < e.W[3]) {
                    p.setAnim(89)
                    e.ah = 0; e.ag = 0
                    p.ah = 0; p.ag = 0
                    p.al = e.W[1]
                    p.ak = e.ak
                    p.O = p.al shl 8; p.N = p.ak shl 8
                    e.setAnim(119)
                    e.aC = 30
                    r7 = false
                }
            }
            // L23: still seen → 3x3 quadrant pounce pick.
            if (r7) {
                val r1 = when {
                    p.W[0] > e.W[2] ->                    // player right
                        when {
                            p.W[1] <= e.W[3] ->
                                if (p.W[3] >= e.W[1]) 124 else 125
                            else -> 126
                        }
                    p.W[2] < e.W[0] ->                    // player left
                        when {
                            p.W[1] <= e.W[3] ->
                                if (p.W[3] >= e.W[1]) 121 else 122
                            else -> 123
                        }
                    else ->                               // overlapping column
                        if (p.W[3] >= e.W[1]) 128 else 127
                }
                e.setAnim(r1)
            }
        }
        in 121..128 -> {
            // L48 pounce
            if (!r7) e.setAnim(120)
            else {
                if (e.T == 1) w.sfx(16)                   // k.A(16)
                if (e.T == 3) p.applyHit(4, 0, e, w)
                if (e.animFinished()) e.setAnim(120)
            }
        }
        130 -> if (e.animFinished()) w.removeEntity(e)    // L60 k.c(this)
    }
    // L62 — every arm (120's pick, the pounce, 130's despawn) falls
    // through to the shared `k(); j()` tail.
    contextK(e, w, p)
    damageIntakeSentinel(e, w, p)
}

/** `i.aL()` (i.java:10008-10099, proven): pouncer FSM — transcribed arm
 *  for arm. */
fun NpcFsm.tickAx50(e: Entity, w: LevelCellSource, p: Entity) {
    when (e.S) {
        94 -> {
            // L43: claim-hold then release-and-despawn.
            e.P = e.P or 512
            if (e.animFinished()) {
                if (w.kL != null && w.kL === e && w.kL?.aw == e.aw) {
                    w.claimReset()                        // k.m()
                }
                w.removeEntity(e)                         // k.c(this)
            }
        }
        0 -> return                                       // L52 idle terminal
        81 -> {
            // L6 countdown perch: r()→P|=64; post-decrement aC<0 → i(82)
            // +P&=-65 (original reads the OLD value — fires one tick after
            // the counter crosses below zero).
            if (e.animFinished()) e.P = e.P or 64
            if (e.aC-- < 0) { e.setAnim(82); e.P = e.P and -65 }
            contextK(e, w, p); damageIntakeSentinel(e, w, p)
        }
        82 -> {
            if (e.animFinished()) e.setAnim(80)           // L12 re-perch
            contextK(e, w, p); damageIntakeSentinel(e, w, p)
        }
        83 -> {
            // L15 air-walk: floor ahead → i(84)+vel0; else ah=1536 (fall).
            if (floorAheadM(e, w)) { e.setAnim(84); e.ah = 0; e.aj = 0 }
            else e.ah = 1536
            if (e.animFinished()) e.P = e.P or 64         // L19
            contextK(e, w, p); damageIntakeSentinel(e, w, p)
        }
        84 -> if (e.animFinished()) { e.setAnim(0); return }  // L22 → S0
        93 -> {
            // L26 ceiling-grab variant — same snap as aK()'s L18 but into
            // S80 with `registerClaim(this,0,W)` (k.a) arming the kill.
            if (p.S in DROP_KILL &&
                Entity.overlapStrict(p.W, e.W) && p.W[3] < e.W[3]) {
                p.setAnim(89)
                e.ah = 0; e.ag = 0
                p.ah = 0; p.ag = 0
                p.al = e.W[1]
                p.ak = e.ak
                p.O = p.al shl 8; p.N = p.ak shl 8
                e.setAnim(80)
                e.aC = 30
                w.registerClaim(e, 0, e.W)                // k.a(this,0,W)
            }
            return                                        // L26 tail: return
        }
        else -> { contextK(e, w, p); damageIntakeSentinel(e, w, p) } // L53
    }
}

// ====================================================================// Slice 65 — ax64 `bl()` grabber/harrier NPC (i.java:15391-15897, proven
// transcription; marker-clip / pool-visual paths labeled `inferred`).
//
// Record (L321, i.java:3474): Z[0..9] = r8[7..16]; Z[1] = r8[8] + 7;
// aC = Z[8]; az = 301; then L395 `i(r8[5])` + L427 `t()`.
// bi[64] = -1 (k.java static table) → record-spawned ax64 is clipless.
// Proven-dead in this build: no `new i` with `ax=64` exists anywhere in
// the bytecode (the only gameplay bindings are `i.a(ax,clip,…)` aK and
// the `cr[][]` pool respawns at i.java:34240-34840 — none carry ax64),
// and no ax64 record appears in any of packs 6-13's records.json.
// `aa` staying null (as `k.r(bi[64])`→`k.r(-1)`) is faithful.
// Z[2..5] = waypoint uids (patrol chain), Z[6] = death-anim pick (S4/S5),
// Z[7] = post-arrival hop (S6) vs drop (S3), Z[8] = stalk budget (S7),
// Z[9] = barrage interval.
// ====================================================================
/** (proven i.java:15400-15436) — `p.S` states the S7 stalk may latch onto:
 *  switch arms send {0,4,5,17,18,30,31,32,33} → r04=true, else false. */
private val AX64_VULN = intArrayOf(0, 4, 5, 17, 18, 30, 31, 32, 33)

/** `i.b(int,int,int,int)` (i.java:8026, proven): direction index 0-4 from
 *  a (x0,y0)→(x1,y1) subpixel vector — the pooled-shot's `i(0, dir)` pick. */
private fun dirIndex5(x0: Int, y0: Int, x1: Int, y1: Int): Int {
    val dx = x1 - x0; val dy = y1 - y0
    if (dx != 0 && abs(dx) > 5120) {
        val slope = dy * 100 / dx
        if (slope == 0 || abs(dy) <= 5120) return 2
        return if (slope >= 0) (if (dx > 0) 3 else 1)
               else (if (dx > 0) 1 else 3)
    }
    return if (dy < 0) 0 else 4
}

/** `bl()` record init — L321 (i.java:3474, proven). `f` = the full record
 *  (f[0]=ax …); Z[0..9] read r8[7..16] = f[7..16]. */
fun NpcFsm.initAx64(e: Entity, f: List<Int>) {
    e.az = 301
    for (i in 0 until 10) e.Z[i] = if (7 + i < f.size) f[7 + i] else 0
    e.Z[1] += 7                                        // r8[8] - (-7)
    e.aC = e.Z[8]
    e.setAnim(if (f.size > 5) f[5] else 0)             // L395 tail: i(r8[5])
    e.refreshBoxes()                                   // L427 tail: t()
}

/** `i.a(k.aS, this, 1, false)` (i.java:8637, proven): the tether shot —
 *  a pooled `k.aX` slot lobbed player→harrier at speed 2048 + `k.Y` bias
 *  (ax64 arm, L15), anim = `dirIndex5`, `P` flags {&-129,&-33,|16,|1 on
 *  left}, af=spawner, c=player, aC=`n`, `k.A(16)` sfx. `av()`==-1 → skip. */
private fun ax64Tether(e: Entity, p: Entity, w: LevelCellSource) {
    val si = w.allocPooledShot()                     // i.java:8641 L33: av()
    if (si == -1) return
    val s = w.pooledShots?.get(si) ?: return
    s.am = (p.W[0] + p.W[2]) shl 7                   // W-center <<8 (sum<<7)
    s.an = (p.W[1] + p.W[3]) shl 7
    s.ao = (e.W[0] + e.W[2]) shl 7
    s.ap = (e.W[1] + e.W[3]) shl 7
    e.ao = s.ao; e.ap = s.ap                         // owner caches target
    val dx = s.ao - s.am; val dy = s.ap - s.an
    val d = e.h(dx, dy)
    if (d != 0) {                                    // L12-15
        s.ag = dx * 2048 / d
        s.ah = dy * 2048 / d + w.kY
    }
    s.av = s.ao < s.am                               // L17-20
    s.P = if (s.av) s.P or 1 else s.P and -2         // L23
    s.N = s.am; s.O = s.an
    s.ak = s.am shr 8; s.al = s.an shr 8
    s.P = s.P and -129 and -33 or 16                 // L24
    s.setAnim(dirIndex5(s.am, s.an, s.ao, s.ap))     // L27: i(0, b(…))
    s.refreshBoxes()                                 // L28: t()
    s.aG = 0                                         // L31 (ax64 arm)
    s.af = e; s.c = p
    s.aC = e.nl                                      // aC = this.n
    w.sfx(16)                                        // k.A(16)
}

/** `i.a(int, boolean)` (i.java:7826, proven): the barrage shot — pooled
 *  slot, `am/an` = this W-center <<8 (the ax64 arm's `L32` writes are
 *  elided by the decompiler — W-center is the consistent source;
 *  `inferred`), `ao/ap` = `r8` ? owner-aim `ao/ap` : `F` or the player's
 *  W-center (ax64's `F` is never bound → player arm; `inferred`),
 *  speed 1280 + `k.Y` (L101→L102), anim = `dirIndex5`, `aG = -1` (L128),
 *  `k.A(16)`. count spread arc (r7>1) unused by ax64's `a(1,false)` —
 *  `inferred` for the r7>1 geometry which is omitted here. */
private fun ax64Barrage(e: Entity, p: Entity, w: LevelCellSource) {
    val si = w.allocPooledShot()                     // L140: av() pool empty
    if (si == -1) return
    val s = w.pooledShots?.get(si) ?: return
    s.am = (e.W[0] + e.W[2]) shl 7
    s.an = (e.W[1] + e.W[3]) shl 7
    s.ao = (p.W[0] + p.W[2]) shl 7                   // r8=false → player arm
    s.ap = (p.W[1] + p.W[3]) shl 7
    val dx = s.ao - s.am; val dy = s.ap - s.an
    val d = e.h(dx, dy)
    if (d != 0) {                                    // L82→L101→L102
        s.ag = dx * 1280 / d
        s.ah = dy * 1280 / d + w.kY
    }
    s.av = s.ao < s.am                               // L104-107
    s.P = if (s.av) s.P or 1 else s.P and -2         // L110
    s.N = s.am; s.O = s.an
    s.ak = s.am shr 8; s.al = s.an shr 8
    s.P = s.P and -129 and -33 or 16                 // L111
    s.setAnim(dirIndex5(s.am, s.an, s.ao, s.ap))     // L119: i(0, b(…))
    s.refreshBoxes()                                 // L120: t()
    s.aG = -1                                        // L128 (ax64 arm)
    s.af = e; s.aC = e.nl                            // L129: af=this,aC=n
    w.sfx(16)                                        // L139: k.A(16)
}

/** `u()` (i.java:700) + `v()` (i.java:730) reduced to the ax64 path
 *  (proven gates that apply; ax64 hits none of the special-cases):
 *  `au = |ak-(kO+200)|/400 + |al-(kP+120)|/120`; alive while `au<=i` or
 *  the `Y` foot box overlaps the `k.ac` view rect. */
private fun ax64Alive(e: Entity, w: LevelCellSource): Boolean {
    e.au = abs(e.ak - (w.kO + 200)) / 400 + abs(e.al - (w.kP + 120)) / 120
    if (e.au <= e.i) return true
    val view = w.kAc ?: return false
    return Entity.overlapStrict(view, e.Y)
}

/** `bl()` — the ax64 harrier FSM (i.java:15391-15897). Head arm runs only
 *  at S7; then the S-switch. */
fun NpcFsm.tickAx64(e: Entity, w: LevelCellSource, p: Entity) {
    if (e.aA < 0) e.aA = 0                           // L6

    if (e.S == 7) ax64StalkArm(e, w, p)              // L6-86 head

    when (e.S) {                                     // L87
        0 -> ax64S0(e, w, p)
        1 -> ax64S1(e, w, p)
        2 -> ax64S2(e, w, p)
        3, 4, 5 -> ax64S345(e, w, p)
        6 -> ax64S6(e, w)
        7 -> ax64S7(e, w, p)
        else -> {}                                   // L313
    }
}

/** L6-86 (proven): the S7 stalk head — latch a pad mask (`bl`) onto the
 *  directional grab marker (`p.ae`), then bind on `k.v(bl)` EDGE. The
 *  `ae.S==66 → L19` re-entry loop in the decompile (i.java:15863-15867)
 *  can only spin — collapsed to the same reposition arm as S60
 *  (`inferred`). */
private fun ax64StalkArm(e: Entity, w: LevelCellSource, p: Entity) {
    val dx = abs(e.ak - p.ak)
    val dy = abs(e.al - p.al)
    val dist = e.h(dx, dy)                           // k.h (k.java:6839)
    // L11-29: vulnerable player anim, not already bound, harrier strictly
    // below the player (L19 ax64 arm), then the range gates. The r02<=100
    // check routes ax64 to the same L29 arm either way (proven).
    var arm = false
    if (p.S in AX64_VULN && !e.runnerG &&
        e.al > p.al && dx > 25 && dist < 200) {
        val m = p.ae
        if (m == null) {
            p.releaseAe()                            // L5/L11: G() on null
            when {
                // L33-37: above-side markers — dead code for ax64 (L19
                // gate forces al>p.al) but kept verbatim.
                e.al < p.al ->
                    if (e.ak < p.ak) { p.ae = w.spawnPickup(42, e.ak + 20, e.al); e.bl = 2 }
                    else { p.ae = w.spawnPickup(48, e.ak - 20, e.al); e.bl = 8 }
                e.al > p.al ->                     // L39-43
                    if (e.ak < p.ak) { p.ae = w.spawnPickup(60, e.ak, e.al - 40); e.bl = 128 }
                    else { p.ae = w.spawnPickup(66, e.ak, e.al - 40); e.bl = 512 }
                else -> p.releaseAe()              // L39 equal-y arm
            }
            arm = true                               // L51
        } else {                                     // L45 (ax64 arm)
            if (m.S == 60 || m.S == 66) {            // L50 / L19-loop (see hdr)
                m.ak = e.ak; m.al = e.al - 40
            }
            arm = true                               // L51
        }
    }
    // L53-67 (proven): stale/mismatched marker cleanup — left side
    // releases {42,60}, right side releases {48}; ak>p.ak && S==66 jumps
    // L10→L76 (skips the bind/reposition block entirely).
    var tailOnly = false
    val m = p.ae
    if (m == null) arm = false
    else when {
        e.ak < p.ak ->
            if (m.S == 42 || m.S == 60) { p.releaseAe(); arm = false }
            else arm = false                         // L61: ak<=p.ak → L67
        e.ak == p.ak -> arm = false                  // L61 → L67
        else ->                                      // ak > p.ak
            if (m.S == 48) { p.releaseAe(); arm = false }
            else if (m.S == 66) tailOnly = true      // → L10 → L76
            else arm = false
    }
    if (!tailOnly) {
        // L68-72 (proven): armed + unbound + pad EDGE on the latch mask →
        // spawn the tether shot and latch G.
        if (arm && !e.runnerG && w.padHeld(e.bl)) {
            ax64Tether(e, p, w)
            e.runnerG = true
        }
        // L74 (proven): pin the marker to (ak, al-20) every armed tick.
        if (arm) p.ae?.let { it.ak = e.ak; it.al = e.al - 20 }
    }
    // L76-86 (proven): tail cleanup — left releases S60, right releases
    // S66.
    val m2 = p.ae
    if (m2 != null) {
        if (e.ak < p.ak && m2.S == 60) p.releaseAe()
        else if (e.ak > p.ak && m2.S == 66) p.releaseAe()
    }
}

/** L88-183 (proven): S0 — lob-window check, then waypoint patrol crawl. */
private fun ax64S0(e: Entity, w: LevelCellSource, p: Entity) {
    val dy = p.al - e.al                             // L88 r06
    if (dy in 151..189 && abs(e.ak - p.ak) < 100) {  // L96-101 lob setup
        e.cx = if (dy != 0) (abs(e.ak - p.ak) * 5 shl 8) / dy else 1280
        e.aq = p.ak; e.ar = p.al + 32
        e.cz = dy / 5; if (dy % 5 != 0) e.cz++
        e.ar += e.cz * (-7)
        e.cA = 0
        e.setAnim(1); e.cy = 0                       // L103: i(1); cy=false
        return
    }
    if (e.aA >= 4) { ax64DiveTarget(e, p); return }  // L109
    val wp = w.waypoints.find(e.Z[2 + e.aA])         // c.a(Z[2+aA])
    if (wp == null) {                                // L179: vel0 + aA++ + Z0 facing
        e.ag = 0; e.ah = 0; e.aA++
        when (e.Z[0]) { 0 -> e.av = true; 2 -> e.av = false }
        return                                       // L312/L324/L181/L183
    }
    e.Z[1] = (wp.f and 127) - w.kX                   // waypoint pace
    val wdx = wp.a - e.ak; val wdy = wp.b - e.al
    if (abs(wdx) > e.Z[1] || abs(wdy) > e.Z[1]) {    // L150: crawl toward
        if (abs(wdx) > abs(wdy)) {                   // x-dominant
            if (wdx >= 0) { e.av = false; e.ag = e.Z[1] shl 8 }
            else { e.av = true; e.ag = -(e.Z[1] shl 8) }
            if (abs(wdy) < e.Z[1]) e.ah = 0
            else e.ah = if (wdy >= 0) abs(wdy) * abs(e.ag) / abs(wdx)
                        else -(abs(wdy) * abs(e.ag) / abs(wdx))
        } else {                                     // y-dominant (L166-177)
            e.ah = if (wdy >= 0) e.Z[1] shl 8 else -(e.Z[1] shl 8)
            if (abs(wdx) < e.Z[1]) e.ag = 0
            // VERBATIM quirk (L174/L177): the |wdx| terms cancel, so ag
            // is ±|ah| — not proportional. Preserved as decompiled.
            else if (wdx >= 0) { e.av = true; e.ag = abs(e.ah) }
            else { e.av = false; e.ag = -abs(e.ah) }
        }
        return
    }
    // arrived inside the waypoint square — L140-148 (proven)
    e.aA++
    // aA<4 → re-fetch the NEXT waypoint and check its `c` hop flag;
    // aA>=4 → the OLD node stays in hand (verbatim L144).
    val next = if (e.aA < 4) w.waypoints.find(e.Z[2 + e.aA]) else wp
    if (next == null) return                         // L144
    if (next.cFlag != 0) { e.setAnim(1); e.cy = 1 }  // L146-148: hop entry
    // c==0 → L315 return
}

/** L109-132 (proven): the aA>=4 dive target — anchor ±100/±190 toward the
 *  player (clamped), +32 bias, cz = |dy|/5 ceil, cx = (|dx|·5<<8)/|dy|. */
private fun ax64DiveTarget(e: Entity, p: Entity) {
    e.setAnim(1); e.cy = 0                           // i(1); cy=false
    e.aq = if (e.ak < p.ak) minOf(e.ak + 100, p.ak) else maxOf(e.ak - 100, p.ak)
    e.ar = if (e.al < p.al) minOf(e.al + 190, p.al) else maxOf(e.al - 190, p.al)
    e.ar += 32
    val adx = abs(e.ak - e.aq); val ady = abs(e.al - e.ar)
    e.cx = if (ady != 0) (adx * 5 shl 8) / ady else 1280
    e.cz = ady / 5; if (ady % 5 != 0) e.cz++
    e.ar += e.cz * (-7)
    e.cA = 0
}

/** L186-232 (proven): S1 — waypoint-hop (`cy`) or target-dive flight. */
private fun ax64S1(e: Entity, w: LevelCellSource, p: Entity) {
    if (e.cy != 0) {                                 // L186-190
        val wp = w.waypoints.find(e.Z[2 + e.aA]) ?: return  // r016
        val ddx = wp.a - e.ak; val ddy = wp.b - e.al
        val framesLeft = (e.clip?.frameCount(e.S) ?: 0) - 1 - e.T  // r019
        if (e.animFinished()) {                      // r() → arrive
            e.aA++; e.ak = wp.a; e.al = wp.b
            e.setAnim(0); e.ag = 0; e.ah = 0
            return
        }
        if (framesLeft != 0) {                       // L190: lerp
            e.ag = (ddx shl 8) / framesLeft
            e.ah = (ddy shl 8) / framesLeft
        }
        return
    }
    val tdx = e.aq - e.ak; val tdy = e.ar - e.al     // L192
    e.cA++
    if (e.cA > e.cz) { e.cA = e.cz; e.aq = -1; e.ar = -1 }  // timeout
    if (e.aq != -1 || e.ar != -1) {                // L200 fly
        if (abs(tdx) < 5) { e.ag = 0; e.ak = e.aq }
        else e.ag = if (tdx > 0) e.cx else -e.cx   // L203-205
        if (abs(tdy) < 5) { e.ah = 0; e.al = e.ar }
        else e.ah = if (tdy > 0) 1280 else -1280   // L210-212
    }
    // L214 (proven): snap+grab on W overlap (cutscene-gated by g.s).
    if (!w.gS && Entity.overlapStrict(e.W, p.W)) {
        e.ak = p.ak; e.al = p.al
        e.setAnim(2); e.aC = 30; e.aq = -1; e.ar = -1
        return
    }
    // L220-226: arrival when position==target, or target cleared.
    if ((e.ak != e.aq || e.al != e.ar) && (e.aq != -1 || e.ar != -1)) return
    // L227-231 (proven): arrival tail — hop (Z7>0 → S6) or drop (S3).
    e.ag = 0; e.ah = 0; e.aq = -1; e.ar = -1
    if (e.Z[7] > 0) { e.setAnim(6); e.ah = w.kX + 1 } else e.setAnim(3)
    e.cz = e.clip?.frameCount(e.S) ?: 0            // cz = aa.b(S)
    e.cA = 0
}

/** L263-322 (proven): S2 — grab hold. Mirror player velocity + y; keep the
 *  clip-74 prompt alive; hold expiry kills the player (g.d(999)); the
 *  `i.f(4112,8256)` mash escape releases every S2-bound ax64 sibling. */
private fun ax64S2(e: Entity, w: LevelCellSource, p: Entity) {
    e.ag = p.ag; e.ah = p.ah; e.al = p.al          // mirror (L263)
    val m = p.ae
    // L266-269 (proven; marker's clip-identity check relaxed to ax14/S —
    // our markers spawn on clip9, `inferred`): bad marker → re-spawn it at
    // the view centre.
    val markerOk = m != null && m.ax == 14 && m.S == 0
    if (!markerOk) {
        p.releaseAe()
        p.spawnAeMarker(w, 0, w.kO + 200, w.kP + 120)   // p.c(k.O+200,k.P+120)
    }
    if (e.aC <= 0) {                               // L271: hold expired
        p.gDrain(999, w)                           // g.d(999)
        p.releaseAe(); w.iBi = false
        e.setAnim(if (e.Z[6] != 0) 5 else 4)       // L276
        return
    }
    e.aC--                                         // L278
    w.iBi = true                                   // i.bi = true
    p.setAnim(15)                                  // p.i(15)
    if (w.iBB) { w.iBC = false; w.iBD = false; w.iBE = 999; w.iBG = 100 }
    p.moveMarker(w, w.kO + 200, w.kP + 120)        // p.d(k.O+200, k.P+120)
    if (p.markerTouched(w)) p.bl += 9              // L281-285: touch → +9
    if (!p.mashQte(w)) return                      // L285→L322: not escaped
    // escape (proven, L285-298): release + anim 9 + reset S2 siblings.
    p.bl = 0; w.kStatE(e.aw); w.iBi = false
    p.releaseAe(); p.setAnim(9)
    for (n in w.npcs) {
        if (n.ax != 64 || n.S != 2) continue
        n.ag = 0; n.ah = 0
        n.setAnim(if (n.Z[6] != 0) 5 else 4)
        n.cz = n.clip?.frameCount(n.S) ?: 0        // cz = aa.b(S)
        n.cA = 0
    }
}

/** L300-306 (proven): S3/4/5 (+ S2's bad-marker arm) — despawn tail. */
private fun ax64S345(e: Entity, w: LevelCellSource, p: Entity) {
    // L300 zeros `aS.bl` only on the S4/S5/S2-fail entry; S3 enters at L301
    // and skips that arm (proven — i.java:15801/15803).
    if (e.S != 3) p.bl = 0
    e.ag = 0; e.ah = 0; e.cA = e.T                 // L301
    if (!e.animFinished()) return                  // r() gate
    if (p.S == 15) p.setAnim(4)                    // L304: revive held-hurt
    w.removeEntity(e)                              // L306: k.c(this)
}

/** L234-237 (proven): S6 — anim end → S7 stalk; below cam-bottom → sink. */
private fun ax64S6(e: Entity, w: LevelCellSource) {
    if (e.animFinished()) e.setAnim(7)             // r() → i(7)
    if (e.al > w.kP - 20) e.ah = w.kY
}

/** L240-258 (proven): S7 — stalk budget + barrage + hover + despawn. */
private fun ax64S7(e: Entity, w: LevelCellSource, p: Entity) {
    e.Z[8]--
    e.aC--
    if (e.aC <= 0) { e.aC = e.Z[9]; ax64Barrage(e, p, w) }  // a(1,false)
    e.ah = w.kY shr 1                              // L243 hover
    e.ag = if (abs(e.ak - p.ak) <= 10) 0
           else if (e.ak < p.ak) 512 else -512     // L246-249
    if (e.runnerG) e.ag = 0                        // L252: bound → still
    if (e.Z[8] < 0) e.ah = w.kY shr 1              // L255
    if (e.al > w.kP - 20) e.ah = w.kY              // L258
    if (!ax64Alive(e, w)) w.removeEntity(e)        // L261→L306: v()==false
}

// ============================================================ ax74 = bN()
// Wisp/collectible (i.java:21280, proven): spawned clipless-record (bi[74]=54)
// or by the `m(-1)` burst (`a(74,54,1,az)`, spawnWisp). S0 proximity collect
// feeds `ap[4|5]` + the `k.s()` streak meter; S1 polar spiral-in orbit; S2
// attach anim pinned above the player → despawn; S5 fall→bezier; S3/S6
// quadratic-bezier view-space flight; S4 end. `j.a(6-arg)` param curve at
// j.java:515 — `b(a,b,c, t(1-t), (1-t)², t²)>>16` blend (verbatim weight
// order).

/** `j.a(x0,y0,x1,y1,x2,y2,t)` (j.java:515, proven) — quadratic bezier in
 *  the 256-param domain; returns [x,y]. `b(6-arg)` = `a·t(1-t) +
 *  2b·(1-t)² + c·t²` scaled >>16 (verbatim — the weight order is NOT the
 *  textbook Bernstein row). */
private fun jBezier(x0: Int, y0: Int, x1: Int, y1: Int,
                    x2: Int, y2: Int, t: Int): IntArray {
    val tt = t * t
    val om = 256 - t
    val om2 = om * om
    val omt = om * t
    fun blend(a: Int, b: Int, c: Int) = (a * omt + 2 * b * om2 + c * tt) shr 16
    return intArrayOf(blend(x0, x1, x2), blend(y0, y1, y2))
}

/** Record init (L116 at i.java:3029, proven): `P|=512`, `az = r8[7]`,
 *  `k.aq++` when `r8[5]==0`, then the shared L392 tail `i(r8[5]) + t()`. */
fun NpcFsm.initAx74(e: Entity, f: List<Int>, w: LevelCellSource) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.P = e.P or 512
    e.az = rf(7)
    if (rf(5) == 0) w.kAq++                        // anim-0 wisp counter
    e.setAnim(rf(5))                               // L392 tail
    e.refreshBoxes()                               // t()
}

fun NpcFsm.tickAx74(e: Entity, w: LevelCellSource, p: Entity) {
    // s() ordering (i.java:6407 L25-L34, proven): the universal anim
    // advance runs in the shared per-tick TAIL — after the FSM arm — so
    // an arm's r() still sees last-frame state before s() wraps T.
    try {
    when (e.S) {                                   // bN() switch (proven)
        0 -> {                                     // L4-17: collect scan
            val d = e.h(e.ak - p.ak, e.al - p.al)  // k.h octagonal
            if (!Entity.overlapStrict(p.W, e.Y) && d > 20) return
            if (w.missionBh() != 3) { w.kCount(5); w.kCollectStreak() }
            else w.kCount(4)                       // flying → ap[4]
            w.sfx(15)
            e.setAnim(2)
            e.az = p.az + 1
            e.af?.let { if (it.aG != 0) w.sfx(15); e.af = null }   // L15-17
            return
        }
        1 -> {                                     // L20-41: polar spiral
            e.P = e.P or 16
            e.az = p.az + 1
            if (e.aA != 0) return                  // aA!=0 → L62
            if (e.j >= e.aE) e.aC-- else e.j += 15 // L28/L30
            e.aF = (e.aD * 256) / 360              // aF = aD·m/360
            e.ak = e.aq + ((Trig.sin(e.aF) * e.j) shr 8)
            e.al = e.ar + ((Trig.sin(Trig.N - e.aF) * e.j) shr 8)
            if (e.j < e.aE) return
            if (e.aC > 0) return                   // L64: keep orbiting
            e.setAnim(2)
            e.af?.let { if (it.aG != 0) w.sfx(15); e.af = null }   // L38-40
            e.aC = 0; e.aE = e.j; e.j = 0          // L41
            return
        }
        2 -> {                                     // L43: attach anim
            e.P = e.P and -17
            e.ak = p.ak; e.al = p.al - 30
            if (e.animFinished()) w.removeEntity(e)
            return
        }
        3, 6 -> {                                  // L51: bezier flight
            val t = if (e.Z[7] != 0) (e.Z[6] * 256) / e.Z[7] else 0
            val xy = jBezier(e.Z[0], e.Z[1], e.Z[4], e.Z[5],
                             e.Z[2], e.Z[3], t)
            e.ak = xy[0] + w.kO
            e.al = xy[1] + w.kP
            e.Z[6]++
            if (e.Z[6] >= e.Z[7]) {
                e.setAnim(4)
                if (e.Q == 6) e.T = 1              // L70: skip frame 0
            }
            return
        }
        5 -> {                                     // L48: fall→bezier setup
            if (!e.animFinished()) return
            e.ah = 0; e.ag = 0
            w.jRand(0, 40)                         // verbatim dead RNG draw
            e.Z[0] = e.ak - w.kO
            e.Z[1] = e.al - w.kP
            val cx = (e.Z[0] + e.Z[2]) shr 1
            val cy2 = (e.Z[1] + e.Z[3]) shr 1
            e.Z[4] = cx + w.jRand(-80, 80)
            e.Z[5] = cy2
            e.setAnim(3)
            return
        }
        4 -> {                                     // L58: end
            if (e.animFinished()) w.removeEntity(e)
            return
        }
        else -> return                             // L65
    }
    } finally {
        e.advanceAnim()                            // universal s() tail (i.java:6407)
    }
}

// ============================================================ ax76 = bO()
// Damage/hazard volume (i.java:21407, proven). `bi[76]=56` — pack-3 slot 56
// is a zero-size entry (pack-3 metadata, proven) → `aa==null` clipless,
// invisible trigger. S0 idle→arm on player overlap while `aZ` standing and
// no interact target (`g.g`); binds via `k.aS.aA|=8` + z-order push
// (`az=this.az-1`) + `g.e` owner slot when `Z[1]==1`. S1 disarm on exit.
// S2 damage cycle: `aC` sibling-chain countdown (`k.q(o)` → `i(2)` chain),
// per-tick `g.d(g.u[k.au])` while overlapping. S3/S5 → i(S+1) once the
// player hitbox leaves `X`; S4/S6 end anims → `k.c`.

/** Record init (L361 at i.java:3546, proven): `az=r8[7]`, `aC=r8[8]`
 *  (chain countdown), `o=r8[9]` (sibling aw link), `Z=new int[2]` +
 *  `Z[1]=r8[4]` (trap-type flag) — the Z re-alloc to 2 slots is folded
 *  into the shared 22-slot ctor array (bO never reads Z[2+]). */
fun NpcFsm.initAx76(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.az = rf(7)
    e.aC = rf(8)
    e.oId = rf(9)                                  // `o` → oId (O clash)
    e.Z[1] = rf(4)
    e.setAnim(rf(5))                               // L392 tail
    e.refreshBoxes()                               // t()
}

/** `g.e` release arm shared by the L15/L17 + L25 tails (i.java:21421): only
 *  fires while this trap still owns the slot, and only for `Z[1]==1`. */
private fun ax76Release(e: Entity, p: Entity) {
    if (p.ge !== e) return
    if (e.Z[1] != 1) return
    p.aA = p.aA and -9                             // aA &= ~8
    p.az = 100
    p.ge = null
}

fun NpcFsm.tickAx76(e: Entity, w: LevelCellSource, p: Entity) {
    when (e.S) {
        0 -> {                                     // L5 arm / L15 release
            if (Entity.overlapStrict(e.W, p.W) && p.aZ && p.g == null) {
                e.setAnim(1)
                if (e.Z[1] == 1) {                 // L55 arm
                    p.aA = p.aA or 8
                    p.az = e.az - 1
                    p.ge = e
                }
            } else ax76Release(e, p)
        }
        1 -> {                                     // L21 disarm on exit
            if (!Entity.overlapStrict(e.W, p.W)) {
                e.setAnim(0)
                ax76Release(e, p)                  // L25
            }
        }
        3, 5 -> {                                  // L29: left hitbox → next
            if (Entity.overlapStrict(e.W, p.X)) e.setAnim(e.S + 1)
        }
        4, 6 -> {                                  // L33: end → despawn
            if (e.animFinished()) w.removeEntity(e)
        }
        2 -> {                                     // L37: damage cycle
            if (e.aC > 0) e.aC--
            if (e.aC == 0 && e.oId != -1) {        // L40: sibling chain
                val r0 = w.findByAw(e.oId)
                if (r0 != null && r0.ax == 76 && r0.S != 2) r0.setAnim(2)
            }
            // L51: damage every tick while overlapping (verbatim — no aC
            // gate on the damage arm).
            if (Entity.overlapStrict(e.W, p.W))
                p.gDrain(w.GU[w.weaponSlot], w)    // g.d(g.u[k.au])
        }
        else -> {}
    }
}

// ============================================================================
// ax34 — k.D player-follower overlay (i.ak, i.java:6914-7000, proven)
// Spawned once by the player init arm (L43 :2748-2763): `k.D = new i();
// ax=34; aa=k.r(42); i(0); az=player.az; ak/al=player pos; av=false; t();
// P|=16|512; k.b`. ak() every tick: snap to k.aS (player) and apply the
// visibility rule — `P|=128` hidden unless the carrier is "grounded",
// the carrier's anim is not in the hide-list, and `i.z` holds.
// ============================================================================

/** i.java:6926-6976 — `k.aS.S` hide-list: L23's early `268` check plus
 *  the L25 chain (any match → r03=false → hidden). Order preserved. */
private val AX34_HIDE_STATES = intArrayOf(
    268,
    291, 61, 203, 204, 277, 276, 164, 183, 184, 317, 250, 244, 351, 353,
    354, 355, 356, 372, 373, 24, 326, 150, 82, 74, 334, 83, 315, 318,
    157, 156, 149, 310, 311, 43, 105,
)

/**
 * `i.ak()` (proven). `this` = the ax34 follower; `k.aS` = the player.
 * Position: `az=99; ak=aS.ak; al=aS.al` then per attach state:
 *  - `g.a` bound (player riding/attached): ax51 → grounded stays false
 *    (hidden); ax15 → `al=aS.al+1; az=100; grounded=true`; any other ax
 *    leaves grounded false (verbatim — L10 only arms on 15).
 *  - no `g.a`: `aS.aZ` → `al+1; grounded=true`; else scan ≤5 cells down
 *    for cell-20 → `al=(row)*20; grounded=true`; nothing → hidden.
 * Visibility: `P|=128` unless `grounded && S∉hide-list && i.z`.
 */
fun NpcFsm.tickAx34(e: Entity, w: LevelCellSource, p: Entity) {
    e.az = 99                                               // L3
    e.ak = p.ak; e.al = p.al                                // L4-L5
    val r0 = e.ak / 20                                      // L6
    val r02 = e.al / 20                                     // L7
    var grounded = false                                    // r8
    val ga = p.ga                                           // g.a
    if (ga == null) {                                       // L13
        if (p.aZ) { e.al = p.al + 1; grounded = true }
        else {                                              // L15-L20
            for (r92 in 0 until 5) {
                if (e.e(w, r0, r02 + r92) == 20) {
                    e.al = (r02 + r92) * 20
                    grounded = true
                    break
                }
            }
        }
    } else if (ga.ax == 15) {                               // L10
        e.al = p.al + 1; grounded = true; e.az = 100
    }
    // ga.ax == 51 or any other ax → grounded stays false (L23).
    val shown = grounded && p.S !in AX34_HIDE_STATES && w.iZ
    e.P = if (shown) e.P and -129 else e.P or 128           // L96-L102
}

// ============================================================================
// i.ad() — in-world dialog bubble (i.java:20624-20755, proven state machine;
// draw ops emitted as w.bubbleDraw for the renderer)
// ============================================================================

/** Draw descriptor emitted while a `cQ` dialog is presenting (the arm
 *  gated on `cQ[2] > 0`). `bh3` selects the filled (`k.f`) vs outlined
 *  (`j.a/b`) bubble style; `tailUp` is the `r18` flip when the bubble
 *  would clip the top edge; `flip` is `r17` (facing/mount override). */
class BubbleDraw(
    val x: Int, val y: Int, val w: Int, val h: Int,
    val pageStart: Int, val lines: Int,
    val flip: Boolean, val tailUp: Boolean, val bh3: Boolean,
    val text: String, val textX: Int, val textY: Int,
)

/**
 * `i.bK()` (i.java:20602, proven): refill the bubble with the next
 * dialog string `k.d(1+k.aj, cQ[5])`, reset the page timer
 * `cQ[2]=cQ[3]`, re-wrap at 120px into `cS`, and recompute the page
 * fields: `cQ[4]=min(3, cS[0])`, `cQ[0]=0`, `cQ[1]=cS[0]-1` (clamped).
 */
private fun NpcFsm.bubbleRefill(e: Entity, w: LevelCellSource) {
    val q = e.cQ ?: return
    e.cR = (w.levelString(1 + w.kAj, q[5]) ?: "") + "\n"        // L2-L3
    q[2] = q[3]                                               // L4
    e.cS = w.wrapDialogText(e.cR, 120)                        // L5
    q[4] = minOf(3, e.cS!![0])                                // L6
    q[0] = 0                                                  // L7
    q[1] = e.cS!![0] - 1                                      // L8
    if (q[0] + q[4] > q[1]) q[4] = q[1] - q[0]                // L10
}

/**
 * `i.ad()` (i.java:20624, proven): per-entity speech-bubble tick. Called
 * for entities with `ax != 11 && ax != 17` in the frame loop
 * (k.java:3740-3749). Font/draw calls map to `w.bubbleDraw` +
 * `w.wrapDialogText`/`w.dialogAdvance` hooks.
 */
fun NpcFsm.tickBubble(e: Entity, w: LevelCellSource) {
    val q = e.cQ ?: return                                    // L6
    if (q[5] < 0) return                                      // L8
    if (q[5] > q[6]) return                                   // L11
    if (q[7] == 1) {                                          // L14-L19
        val kc = w.kC
        if (kc == null || !kc.claimActive()) {
            q[4] = 0; e.cS = null; e.cR = ""; q[1] = -1
            e.cT = null; e.cQ = null
            return
        }
    }
    if (q[2] == -1) {                                         // L22-L27
        if (q[5] <= q[6]) bubbleRefill(e, w)                  // L27 → bK()
        else { q[6] = -1; e.cR = ""; return }
    }
    q[2] = q[2] - 1                                           // L28
    if (q[2] > 0) {
        // L31-L84 draw arm — emit the descriptor; re-wrap for cS.
        val bh3 = Entity.MISSION_BH[w.kAj] == 3              // k.bh[k.aj]==3
        val r02 = w.dialogAdvance(q[4]) + 10                  // k.y.k(n)+10
        var r14 = e.ak - w.kO                                 // screen x
        val r03 = e.al - w.kP - 70                            // bubble top base
        var r15 = r03
        var r16 = r03 - r02
        var r17 = e.av                                        // flip flag
        var r18 = false                                       // tail-up flag
        if (bh3) {                                            // L56
            r17 = false
            if (r16 < 0) {                                    // L60-L63
                r18 = true
                val r04 = e.al - w.kP
                r16 = r04; r15 = r04
            }
            if (r17) r14 -= 120                               // L66 (dead)
            // L68: k.f(r14, r16, 120, r02) filled bubble
        } else {
            // L56: style flag cQ[8]==1 overrides the facing flip
            if (q[8] == 1) r17 = !r17                         // L60-L61
            if (r17) r14 -= 120                               // L66
            // L68: white outline + black fill bubble rects
        }
        // tail (a(5-arg) interpolates the 2-line wedge; tailUp flips it)
        w.bubbleDraw = BubbleDraw(
            x = r14, y = r16, w = 120, h = r02,
            pageStart = q[0], lines = q[4],
            flip = r17, tailUp = r18, bh3 = bh3,
            text = e.cR, textX = r14 + 60, textY = r16 + 5,
        )
        // k.y.l(1) — typewriter advance (inferred; no core op)
        e.cS = w.wrapDialogText(e.cR, 120)                    // L81
        if (q[0] + q[4] > q[1]) q[4] = q[1] - q[0]            // L84
        // L84 tail: k.y.a(bg, cR, cS, x+60, y+5, q[0], q[4], 17, -1)
        return
    }
    // L31+: page/string advance on timer expiry
    if (q[0] + q[4] == q[1]) {                                // last line
        q[5] = q[5] + 1                                       // next string idx
        if (q[5] <= q[6]) bubbleRefill(e, w)                  // L43 → bK()
        else {                                                // done
            q[4] = 0; e.cS = null; e.cR = ""; q[1] = -1; e.cT = null
            if (q[9] == 1) {                                  // release flag
                val kc = w.kC
                if (kc != null && kc.claimActive()) {
                    kc.cd[2] = true; kc.cd[1] = true          // cd arms
                    if (Entity.MISSION_BH[w.kAj] != 3) w.kM(w.kAd)  // k.m(k.ad)
                }
            }
        }
    } else {
        q[0] = q[0] + q[4]                                    // L44 next page
    }
    if (q[0] + q[4] > q[1]) q[4] = q[1] - q[0]                // L46
    q[2] = q[3]                                               // L48 re-arm
}

// ============================================================
// slice 185 — L777 shared-tail helpers (i.java, proven)
// ============================================================

/** `i.aB()` (i.java:8845-8918, proven): melee application — own X box
 *  overlapping the player's W applies a hit.
 *  ax11: player S∈{43,22} → op20 (aerial knock), else op4.
 *  ax73: S==165 → G() + op4 + Z[8] one-shot latch; S==146 → op4; a
 *  rolling player (S6) is immune; else op4. Other ax: no-op. */
private fun meleeApply(e: Entity, p: Entity, w: LevelCellSource) {
    when (e.ax) {
        11 -> {
            if (e.X[0] == e.X[2]) return                       // degenerate X
            if (!Entity.overlapI(p.W, e.X)) return             // a(aS.W, X)
            if (p.S == 43 || p.S == 22) p.applyHit(20, e.l, e, w)
            else p.applyHit(4, e.l, e, w)
        }
        73 -> {
            if (e.X[0] == e.X[2]) return
            if (!Entity.overlapI(p.W, e.X)) return
            when {
                e.S == 165 -> {
                    e.releaseAe()                              // G()
                    p.applyHit(4, e.l, e, w)
                    if (e.Z[8] == 0) e.Z[8] = 1                // L39 latch
                }
                e.S == 146 -> p.applyHit(4, e.l, e, w)
                p.S == 6 -> {}                                 // L31 roll-immune
                else -> p.applyHit(4, e.l, e, w)
            }
        }
        else -> {}
    }
}

/** `i.e(i)` (i.java:2408-2452, proven): Bresenham walk between the two
 *  W-box centers in CELL space — returns true when the sight line hits a
 *  solid cell (`e(cx,cy) >= 12`) before reaching the far box. The start
 *  cell itself is not tested; the walk stops on the destination cell.
 *  Both W boxes must be non-null (else false — nothing to block). */
private fun losBlocked(e: Entity, p: Entity, w: LevelCellSource): Boolean {
    var x0 = ((e.W[0] + e.W[2]) shr 1) / 20
    var y0 = ((e.W[1] + e.W[3]) shr 1) / 20
    val x1 = ((p.W[0] + p.W[2]) shr 1) / 20
    val y1 = ((p.W[1] + p.W[3]) shr 1) / 20
    var dx = x1 - x0; var sx = 1
    if (dx < 0) { dx = -dx; sx = -1 }
    var dy = y1 - y0; var sy = 1
    if (dy < 0) { dy = -dy; sy = -1 }
    if (dx > dy) {
        var err = dx shr 1
        while (x0 != x1) {
            if (e.e(w, x0, y0) >= 12) return true
            x0 += sx; err += dy
            if (err > dx) { y0 += sy; err -= dx }
        }
    } else {
        var err = dy shr 1
        while (y0 != y1) {
            if (e.e(w, x0, y0) >= 12) return true
            y0 += sy; err += dx
            if (err > dy) { x0 += sx; err -= dy }
        }
    }
    return false
}

/** Point-in-rect `i.a(int,int,int[])` (i.java:684, proven). */
private fun pointInRect(x: Int, y: Int, r: IntArray): Boolean =
    x >= r[0] && x <= r[2] && y >= r[1] && y <= r[3]

/** `i.d()` (i.java:1466-1537, proven): the sight/stagger priority the
 *  head assigns to `j` every tick (skipped for S24).
 *  Guards: own aB<=0 (`P()` → G()+0), player dead (`g.g()`), player
 *  S∈{267,268,291}, `aS.aA&8` blind-bit → 0.
 *  Overlap branch (player W or X touching own W, or zone-bottom Z[12]
 *  past the player's top): a live ax15 `g.a` link with |dy|>15 → 0;
 *  `aS.aA&256` → clear it, set aA|16, return 6; else 6.
 *  Far branch (zone-top Z[11] at/below player bottom → 0; facing must
 *  cover the player): |dy|<=15+|dx|<=55 → 4; aA==6 → 7; |dx|<=100 → 3;
 *  |dx|<=180 → 1; else 0. */
private fun sightPriorityD(e: Entity, p: Entity, w: LevelCellSource): Int {
    if (e.aB <= 0) { e.releaseAe(); return 0 }        // P() → G()+0
    if (w.gG()) return 0                              // g.g()
    if (p.S == 268 || p.S == 267 || p.S == 291) return 0
    var r5 = p.ak - e.ak
    var r6 = p.al - e.al
    if ((p.aA and 8) != 0) return 0
    // L21: player W or X touching own W → the L25 close branch.
    if (Entity.overlapI(p.W, e.W) || Entity.overlapI(p.X, e.W)) {
        val ga = p.ga                                 // g.a — ax15 link
        if (ga != null && ga.ax == 15 && Math.abs(r6) > 15) return 0
        if ((p.aA and 256) != 0) {
            p.aA = p.aA and -257
            p.aA = p.aA or 16
        }
        return 6
    }
    if (e.Z[12] <= p.W[1]) return 0                   // L21 fallthrough → 0
    if (e.Z[11] >= p.W[3]) return 0                   // L40→L76
    val faces = if (r5 > 0) !e.av else e.av           // facing covers player
    if (!faces) return 0
    if (r5 < 0) r5 = -r5
    if (r6 < 0) r6 = -r6
    if (r6 <= 15) {
        if (r5 <= 55) return 4
        if (e.aA == 6) return 7
    }
    if (r5 <= 100) return 3
    if (r5 <= 180) return 1
    return 0
}

/** `i.l()` (i.java:2255-2404, proven): sight check against the player —
 *  1. `ai()` static override (i.java:22284): `k.ae` is an ax10 camera-
 *     focus zone in S52 → always seen (cinematic alert).
 *  2. `aS.aA & 8` set → blind (returns false; the per-ax arms are skipped).
 *  3. Per-ax zone:
 *     - ax11: S∈{267,268,291} never seen; >1-cell vertical gap never;
 *       player riding an ax69 (`af`) → zone-vs-af.W rect (af.S==6) or
 *       flat checks (af.S∈{1,7} blind, af.Z[0]==0 + own S117 → seen);
 *       else W-overlap → seen, else the Z[9..12] rect vs player center.
 *     - ax73: W-overlap → seen, else the same Z[9..12] rect.
 *     - ax17/23/50: `bn` alert flag → blind; own W on camera (`k.ac`) → seen.
 *  4. `e(aS)` LOS clear (Bresenham above) and player S∉{284,285}.
 *  Result feeds `aS.a(32)` in the L827 tail branch — the counter-alerts. */
private fun losL(e: Entity, p: Entity, w: LevelCellSource): Boolean {
    val ae = w.kAe
    if (ae != null && ae.ax == 10 && ae.S == 52) return true   // ai()
    if ((p.aA and 8) != 0) return false                        // L10 → blind
    val inZone: Boolean = when (e.ax) {
        11 -> {
            if (p.S == 268 || p.S == 267 || p.S == 291) false
            else if (Math.abs(e.al - p.al) / 20 > 1) false
            else {
                // L22-L52: player riding an ax69 (`af`) — af.Z[0]==0 +
                // player S∈{250,150} → blind; own S117 only PRIMES r0
                // (the af.S checks below and L54 can overwrite it —
                // faithful). af.S∈{7,1} → blind; af.S==6 → the zone
                // rect vs af.W; else falls to L54.
                val af = p.af
                var resolved = false
                var r0 = false
                if (af != null && af.ax == 69) {
                    if (af.Z[0] == 0) {
                        if (p.S == 250 || p.S == 150) resolved = true
                        else if (e.S == 117) r0 = true
                    }
                    if (!resolved) {
                        if (af.S == 7 || af.S == 1) { resolved = true; r0 = false }
                        else if (af.S == 6) {
                            resolved = true
                            r0 = Entity.overlapI(
                                intArrayOf(
                                    if (e.av) e.Z[9] else e.ak, e.Z[11],
                                    if (e.av) e.ak else e.Z[10], e.Z[12]),
                                af.W)
                        }
                    }
                }
                if (resolved) r0
                else {
                    // L54/L83: own W overlap → seen; else Z[9..12] rect
                    // vs the player's box center.
                    if (Entity.overlapI(p.W, e.W)) true
                    else pointInRect(
                        p.ak, (p.W[1] + p.W[3]) shr 1,
                        intArrayOf(
                            if (e.av) e.Z[9] else e.ak, e.Z[11],
                            if (e.av) e.ak else e.Z[10], e.Z[12]))
                }
            }
        }
        73 -> {
            if (Entity.overlapI(p.W, e.W)) true
            else pointInRect(
                p.ak, (p.W[1] + p.W[3]) shr 1,
                intArrayOf(
                    if (e.av) e.Z[9] else e.ak, e.Z[11],
                    if (e.av) e.ak else e.Z[10], e.Z[12]))
        }
        17, 23, 50 -> {
            // L70/L77: bn → blind; own W on the camera rect → seen.
            if (w.iBn) false
            else {
                val ac = w.kAc
                ac != null && Entity.overlapI(e.W, ac)
            }
        }
        else -> false                                          // L83 default
    }
    if (!inZone) return false
    if (losBlocked(e, p, w)) return false                    // L90
    if (p.S == 284 || p.S == 285) return false                 // L92/L94
    return true
}

/** `i.b(i)` (i.java:1546-1615; structured :1164-1250, proven): the spot
 *  check feeding the L412 `aA=1` alert — dead-anim player
 *  (S∈{267,268,291}), grab latch `g.j`, LOS block `e(r8)`, `aA&8 + bn`
 *  blind, `v()` out-of-play, `l()` zone miss, or own `aA ∉ {0,1}` →
 *  false. On spot: alive player → `av = !av` (verbatim facing flip);
 *  `bn` → `aS.aA&=-9` + `aS.b(aY[0].Z[4],0,0,-1,-1)` projectile (the
 *  `k.aY` pool is unported → inert); `aA=1` + ax11 `i(5)` (ax73
 *  `Z0==3 → i(155)+aq=ak∓60` else `i(154)`); then `af.ax==69 &&
 *  af.S∈{6,2}` → the ax69 bind (freeze both, `af.i(7)+aA=1`,
 *  `aq=af.ak`) else true. */
private fun spotB(e: Entity, p: Entity, w: LevelCellSource): Boolean {
    if (p.S == 268 || p.S == 267 || p.S == 291) return false
    if (Entity.grabLatch) return false
    if (losBlocked(e, p, w)) return false
    if ((p.aA and 8) != 0 && w.iBn) return false
    if (!e.inPlayV(w)) return false
    if (!losL(e, p, w)) return false
    if (e.aA != 0 && e.aA != 1) return false
    if (!w.gG()) e.av = !e.av                       // verbatim flip
    if (w.iBn) {
        p.aA = p.aA and -9
        // `aS.b(aY[0].Z[4],0,0,-1,-1)` (i.java:1589-1590) — proven-dead
        // call path: `k.aY` is allocated but never filled (k.java:8425;
        // nulled at i.java:2541), so `kAyAt(0)` is always null and the
        // throw never fires. Verbatim for parity.
        w.kAyAt(0)?.let { p.spawnKnife(w, it.Z[4]) }
    }
    e.aA = 1
    when (e.ax) {
        11 -> e.setAnim(5)
        73 -> {
            if (e.Z[0] == 3) {
                e.setAnim(155)
                e.aq = e.ak + (if (e.av) -60 else 60)
            } else e.setAnim(154)
        }
    }
    val af = p.af ?: return true
    if (af.ax != 69 || (af.S != 6 && af.S != 2)) return true
    e.af = af
    af.ah = 0; af.ag = 0; af.aj = 0; af.ai = 0
    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
    af.setAnim(7); af.aA = 1; e.aq = af.ak
    return true
}

// ============================================================
// slice 197 — ax8 ar() + i.aw() waypoint materialization
// ============================================================

/** `ar()` (i.java:21786, proven) — ax8 helper volume.
 *  `g.f == this && r()` → `P|=64` (marker echo-fade).
 *  Clip-59 variant (`aa == k.r(59)`): `S∈{2,6,8} && r() && aw==-1` →
 *  `P|=32|P|=64`; any other S with `r() && aw==-1` → `k.c(this)`.
 *  Non-59 clip: `S==4 && W∩aS.W && aS.S != 317` → `aS.a(4,0,0,this)`
 *  player damage; `r() && aw==-1` → `k.c(this)`. */
fun NpcFsm.tickAx8(e: Entity, w: LevelCellSource, p: Entity) {
    if (Entity.gf === e && e.animFinished()) e.P = e.P or 64     // L9
    if (e.clip === w.clipFor(59)) {                             // L19: aa == k.r(59)
        if (e.S == 2 || e.S == 6 || e.S == 8) {                 // L3f
            if (e.animFinished() && e.aw == -1) e.P = e.P or 32 or 64
            return
        }
        if (e.animFinished() && e.aw == -1) w.removeEntity(e)   // L65
        return
    }
    if (e.S == 4) {                                             // L79
        e.refreshBoxes()                                        // t()
        if (Entity.overlapStrict(e.W, p.W) && p.S != 317)
            p.applyHit(4, 0, e, w)                              // aS.a(4,0,0,this)
    }
    if (e.animFinished() && e.aw == -1) w.removeEntity(e)       // Lac
}

/** `i.aw()` (i.java:23083, proven): materialize the Z[1..4] waypoint
 *  refs — `c.a(uid)` lookup; on a hit `Z[i+1] = c.j` (the uid the copy
 *  is about to mint), `c.a(node,this)` = entity-x-shifted clone
 *  appended to the pool, `C++` (resolved-chain length). */
fun materializeWaypoints(e: Entity, w: LevelCellSource) {
    for (i in 0 until 4) {
        val node = w.waypoints.find(e.Z[i + 1]) ?: continue
        e.Z[i + 1] = w.waypoints.nextDerived                    // c.j
        w.waypoints.addDerived(node, e)                         // c.a(node, this)
        e.runnerC++                                             // C++
    }
}

/** `i(short[])` case-8 → L94b (i.java:8204→:8220, proven): the shared
 *  record-field map arm — `aE=f[4]`, `aF=f[11]`, `o=f[12]`, `p=f[13]`,
 *  `aG=f[14]`, `ay=f[15]` — then the L1bea tail `i(r8[5])` + L1d58
 *  `t()` (ax8 skips every special-case). */
fun NpcFsm.initAx8(e: Entity, f: List<Int>) {
    fun rf(i: Int) = if (i < f.size) f[i] else 0
    e.aE = rf(4); e.aF = rf(11); e.oId = rf(12)
    e.pv = rf(13); e.aG = rf(14); e.ay = rf(15)
    e.setAnim(rf(5))
    e.refreshBoxes()
}
