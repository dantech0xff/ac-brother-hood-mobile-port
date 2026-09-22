package com.acrebuild.core

/**
 * Slice-2 player FSM: the grounded + air locomotion arms of `g.e()`,
 * transcribed from the simple decompile (goto form) of
 * `reconstructed-project/src/simple/g.java`.
 *
 * Ported verbatim (confidence `proven` for structure, literals `proven`):
 *
 * - L682 grounded tail (shared by S∈{0,1,7,11,26,78,79,80}): when the
 *   top-of-hitbox cell OR the below-feet cell is open (`aO<=12||aR<=12`),
 *   S79 zeroes velocity, S11 decays `ag=(ag<<1)/3`, then `l()` runs unless
 *   `!am()` keeps it; when BOTH are solid the entity is embedded → `i(79)`.
 * - `l()` (g.java `public final boolean l()`): the grounded input helper.
 *   From S79: press dir → same-facing enters `i(bn?199:32)` run-start
 *   (`ag=±2560`, or 0 on first tick for S199), cross-facing flips `av`.
 *   From other states: held dir matching facing → `ax()`; double-tap →
 *   `i(10)` dash `ag=∓4096`; DOWN while moving → `i(32)` slide-stop;
 *   nothing held while running → `i(11)` brake then `aw()`.
 * - `ax()` (g.java `private boolean ax()`): sustained run — `i(12)` with
 *   `ag=±2560` (±512 when the side-strip max `aT`/`aU` is in 18..23), or the
 *   `aR>18` edge-walk `i(26)` at ±1280; slope cells 14/15 pull `ah=ag>>1`,
 *   17 pushes `ah=-ag>>1`.
 * - `aw()` (g.java `boolean aw()`): settle/idle tail — airborne → `a(0)`
 *   (state 43 fall), DOWN → `i(78)` crouch-dip, `j.g % 100 == 0` → `i(1)`
 *   idle flicker, else `i(0)`/`i(79)` settle.
 * - Post-switch tail (g.e() L2083 block): `cq && v(16398)` jump dispatch —
 *   `v(2)/v(8)` pick facing, standing states (79/32/199) enter `i(21)`
 *   pre-jump (headroom probe `(W1/20)-1`), airborne press → `i(233)`,
 *   else `i(22)`; non-jump presses clear `ag`. Opposite-direction double-tap
 *   (`x()`) with `aA>0` → `i(25)` back-dash.
 * - Air family {20,21,22,23,25,215,233} (L859/L889): `aj=1536` gravity,
 *   S21→`i(22)`+`ah=-5120` at anim end (rope -2560 skipped — no `a` refs),
 *   S22 apex (`ah>=0 && ah+aj<0`) → `i(23)`, landing probes → `d(false)`,
 *   anim end → `a(0)` re-enter fall.
 * - S43 fall arm (L1045): gravity + `d(aR==4||aS==4)` landing variant.
 * - S5 land arm (L464): jump press → `i(21)` roll-jump, direction held →
 *   `l()`, anim end → `i(aO>12 ? 79 : 0)`.
 * - S6 (L139): on `r()` clear velocities + `P|=64`; `!u(16388)` → `i(0)`.
 * - S10 dash (L1315): `ag /= 2` per tick (plus `ab` mirror — not ported).
 * - S32 arm (structured g.java `case 32`): `r()` → `ag=0; i(Q==79?79:0)`.
 * - S199 arm (case 199): grounded → wall stop, L/R hold → `ag=±2560` or
 *   flip `av`, release → `ag=0; i(79)`.
 * - S78/S80 (L771): `r()` → `i(79)` or `i(1)`.
 *
 * Explicitly not ported (flagged `inferred`/omitted): `i.bn` blend-mode
 * variant (`bn=false` path → run-start uses 32), `am()` ledge-hang check
 * (stubbed false = "no ledge"), `a(257,8)` ledge-drop, `k.f(this)` barrier
 * clamp (no barrier entity), climb `f()`/`i(6)` entry requires `ci`
 * climbable refs, and every interact/QTE/attack arm.
 */
class PlayerFsm(private val world: LevelCellSource, private val rng: DeterministicRandom? = null) {

    var bn = false   // i.bn — blend/unlock flag (false in slice 2)

    fun tick(p: Entity, pad: Pad) {
        p.cp = true; p.cq = true; p.ct = true; p.cw = true; p.zz = true
        // i.java:4072-4073 (proven): per-tick iframe + hit-flash decay
        if (p.gt > 0) p.gt--
        if (p.bh > 0) p.bh--
        dispatch(p, pad)
        postTail(p, pad)
        // g.java:578 (proven): terminal fall velocity 5120 (20px/tick =
        // one cell) — without it long descents tunnel through floors.
        if (p.ah > 5120) p.ah = 5120
    }

    private fun dispatch(p: Entity, pad: Pad) {
        when (p.S) {
            // grounded family — L682 tail
            0, 1, 7, 11, 26, 79 -> groundedTail(p, pad)
            78, 80 -> {                       // L771
                if (p.animFinished()) p.setAnim(if (p.S == 78) 79 else 1)
            }
            12 -> {                           // L639 — co++, ledge grabs skipped
                p.co++
                if (p.ag != 0 && p.aO == 0) { /* ledge-climb arms not ported */ }
                groundedTail(p, pad)
            }
            32 -> {                           // case 32 — run-start end → settle
                if (p.animFinished()) {
                    p.ag = 0
                    p.setAnim(if (p.Q == 79) 79 else 0)
                }
            }
            // L1863 — lunge states tick the arc (g.java:886-906 dispatch)
            272, 273, 274, 275, 292 -> {
                val mount = Entity.at
                if (mount != null && mount.Z[0] == 4 && p.cE <= 0) {
                    // Z[0]==4 cart, arc done -> au() orbit (unported)
                } else p.lungeTick(world)
            }
            298 -> {                          // L1293
                if (p.animFinished()) p.P = p.P or 64
                val mount = Entity.at
                if (mount != null && mount.S != 168) p.lungeTick(world)
            }
            199 -> case199(p, pad)
            5 -> landArm(p, pad)              // L464
            6 -> {                            // L139
                if (p.animFinished()) {
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0; p.P = p.P or 64
                }
                if (!pad.u(Pad.M_UP)) p.setAnim(0)
            }
            10 -> {                           // L1315 — dash decay
                p.ag /= 2; p.ah = 0; p.aj = 0
            }
            21, 233 -> preJumpArm(p)          // L859
            20, 22, 23, 25, 215 -> airFamily(p, pad)
            43 -> fallArm(p)
            257 -> ledgeDropArm(p)            // L1770
            63 -> {                           // climb-up end — inferred arm
                if (p.animFinished()) p.setAnim(0)
            }
            67, 68, 69, 112, 113, 114, 115 -> comboArm(p, pad)  // L1341 family
            183, 184 -> assassinArm(p)                          // L413/L426
            else -> {
                // attack anims play to completion then settle (inferred
                // arm — the real per-state arms are unmined)
                if (isAttackState(p.S)) {
                    if (p.animFinished()) p.setAnim(if (p.aZ) 0 else 43)
                } else if (p.animFinished()) {
                    p.setAnim(if (p.Q == 79) 79 else 0)
                }
            }
        }
        interactScan(p)     // az() — g/ci/at interact maintenance+scan
        mountEntry(p, pad)  // L1947 — mount/assassinate context arm
    }

    /**
     * g.java:3474-3562 (proven) — the L1947 block inside `g.e()`: the
     * `J&4` mount-request consumer. Two arms:
     *  - L1960-L2003 **mount**: `i.at` bound and no interact target in
     *    front → in-range check (ax72 Z[0]∈{1,3,4} gates) → a context
     *    press (v(65568)) zeroes velocity and lunges via `c(i.at)`.
     *  - L2006-L2027 **assassinate**: an ax11 in front with `Z[19]==1`
     *    (assassination window set by its FSM) and `!P()` → same lunge
     *    on `c(g)` — no sfx in this arm (victim anim plays it).
     * `r98` arms the L2029 tail: `cm=1` (mounted) plus the clip-74 hand
     * indicator at view center — `T()` gates the release so an existing
     * non-hand indicator survives. The L1994/L2020 `k.k()`+`V()` checks
     * are convergent: every exit lands at L2029 with `r98` already set
     * (proven by tracing the goto chain). L2048's `o()→ao()` tail
     * (g.java:3566) is a debug/skip arm — flagged, unported.
     */
    fun mountEntry(p: Entity, pad: Pad) {
        if (p.gJ and 4 == 0 || p.S == 50) return
        var r98 = false
        val t = Entity.at
        val bound = p.g
        if (t != null && (bound == null || !p.inFrontOf(bound))) {
            // L1960-L1989 — mount in-range (r104 starts true; gates only
            // clear it — the |ak-at.ak| band check at L1980 is dead code,
            // both exits leave r104 true for Z[0]==4)
            if (p.aZ || p.mountableState()) {
                if (t.wasHitRecently(world)) {
                    var r104 = true
                    if (t.ax == 72 && t.Z[0] == 1 &&
                        p.h(t.ak - p.ak, t.al - p.W[1]) >= t.Z[3]) r104 = false
                    if (t.ax == 72 && t.Z[0] == 4 && (t.Z[4] < 0 || !p.aZ)) r104 = false
                    if (t.ax == 72 && t.Z[0] == 3) r104 = false
                    r98 = r104
                    // L1997 press path — L1994's k.k()/V() else-checks
                    // converge on L2029 with r98 already set (proven:
                    // V()==true loops back through L1974 to the same
                    // r98=r104; V()==false lands at L2029 directly).
                    if (r104 && pad.v(Pad.M_CONTEXT)) {
                        p.ag = 0; p.ah = 0; p.aj = 0
                        if (t.Z[0] != 4) {                 // L2000-L2002
                            if (world.mounted) p.releaseAe() else p.dropIndicator(world)
                        }
                        p.grabLunge(t, world)              // c(i.at)
                        p.z = false
                        world.sfx(30)
                    }
                }
            }
        } else if (bound != null && bound.ax == 11 && bound.Z[19] == 1 &&
            !bound.deadRelease()) {
            // L2006-L2027 — assassinate window on the in-front target
            if (p.aZ || p.mountableState()) {                  // L2016
                r98 = true
                // L2023 press path — L2020's k.k()/V() else-checks are
                // convergent dead code (same L2029 landing as L1994).
                if (pad.v(Pad.M_CONTEXT)) {
                    p.ag = 0; p.ah = 0; p.aj = 0
                    if (world.mounted) p.releaseAe() else p.dropIndicator(world)
                    p.grabLunge(bound, world)                  // c(g)
                    p.z = false
                }
            }
        }
        // L2029-L2040 tail — r98 → spawn/refresh the hand at view center
        // and set cm=1 (mounted); k.k() short-circuits the refresh
        if (r98) {
            if (!world.mounted) {
                if (!p.indicatorIsHand(world)) p.releaseAe()
                p.spawnHand(world, 200 + world.kO, 120 + world.kP)
                p.moveHand(world, 200 + world.kO, 120 + world.kP)
            }
            world.setMounted()
        }
        // L2051-L2057 aA fixups; L2048 o()→ao() debug arm unported
        if (p.aA == 0) p.aA = 1
        if (p.aA and 4 != 0) p.aA = p.aA and -5
    }

    // -- grounded family tail (L682) ----------------------------------------
    private fun groundedTail(p: Entity, pad: Pad) {
        if (p.aO <= 12 || p.aR <= 12) {
            if (p.S == 79) {
                p.ag = 0; p.ah = 0
            }
            if (p.S == 11) p.ag = (p.ag shl 1) / 3
            // L691-692 (proven): down-held at a ledge → a(257,8) vault-drop;
            // L2886 (proven): down-edge into a wall → am() climb-up S63
            if (pad.u(Pad.M_DOWN) && wallClimb(p)) return
            if (ledgeDrop257(p, pad)) return
            // ap() attack entry (proven subset): v(65568) tap + I==1 sword →
            // ag=ah=aj=0 then i(67), or i(81) from crouch S79.
            if (pad.v(Pad.M_CONTEXT) && p.gI == 1) {
                p.ag = 0; p.ah = 0; p.aj = 0
                p.setAnim(if (p.S == 79) 81 else 67)
                return
            }
            if (p.hitWall()) { p.ag = 1; p.collideSides(world, true); p.ag = 0 }
            if (!l(p, pad)) {
                p.cq = false
                p.enterFall(0, world)
            }
        } else {
            p.setAnim(79)
        }
    }

    /**
     * `am()` (g.java:301, proven for the `ag != 0` half): moving into a
     * type-19 wall cell (aV/aW = cell left/right of feet) with `aR == 0`
     * snaps `ak` to the grid and enters `a(63, 16385)` (deferred i(63) +
     * `al = ((W[3]+10)/20)*20 - 1` climb-up snap). ag==0 ledge variant
     * unmined.
     */
    private fun wallClimb(p: Entity): Boolean {
        when {
            p.ag > 0 && p.aV == 19 && p.aR == 0 -> {
                p.av = true; p.ak = (p.ak / 20) * 20
            }
            p.ag < 0 && p.aW == 19 && p.aR == 0 -> {
                p.av = false; p.ak = (p.ak / 20) * 20 + 20
            }
            else -> return false
        }
        p.aj = 0; p.ah = 0; p.ag = 0
        p.enterStateMasked(63, 16385, world)
        return true
    }

    /**
     * `a(257,8)` ledge-drop (g.java:2860-2874 L692, proven): DOWN held +
     * support aQ ∈ {20,5} + aR==0 + the cell two-over in the facing
     * direction one row below is non-solid → vault down. Probes at
     * `al+20` like the original (`al+=20; x(); al-=20`).
     */
    private fun ledgeDrop257(p: Entity, pad: Pad): Boolean {
        if (!pad.v(Pad.M_DOWN)) return false
        p.al += 20; p.probeCells(world); p.al -= 20
        val aQ = p.aQ; val aR = p.aR
        val edgeCx = if (p.av) p.W[0] / 20 - 2 else p.W[2] / 20 + 2
        val belowCy = p.W[3] / 20 + 1
        p.probeCells(world)   // restore unshifted probes
        if ((aQ == 20 || aQ == 5) && aR == 0 &&
            p.e(world, edgeCx, belowCy) < 12) {
            p.enterStateMasked(257, 8, world)
            return true
        }
        return false
    }

    /** S257 exit arm (g.java:3317 L1770, proven): anim end → drop by the
     *  frame's offset (+10 y, ±dx by facing) back to a(0). */
    private fun ledgeDropArm(p: Entity) {
        if (!p.animFinished()) return
        val c = p.clip ?: return
        p.al += c.frameDy[c.frameIndex(p.S, p.T)] + 10
        p.ak += if (p.av) -c.frameDx[c.frameIndex(p.S, p.T)]
                else c.frameDx[c.frameIndex(p.S, p.T)]
        p.setAnim(0)
    }

    // -- l() grounded input helper (proven) ----------------------------------
    private fun l(p: Entity, pad: Pad): Boolean {
        p.aF = 0; p.cp = true; p.cq = true; p.zz = true
        if (p.S == 79) {
            if (pad.u(Pad.M_LEFT)) {
                if (!p.av) p.av = true
                else {
                    p.setAnim(if (bn) 199 else 32)
                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else -2560
                }
            } else if (pad.u(Pad.M_RIGHT)) {
                if (p.av) p.av = false
                else {
                    p.setAnim(if (bn) 199 else 32)
                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else 2560
                }
            }
            // L41 tail: jump press continues to the shared L55 block
            if (pad.u(Pad.M_UP)) return lShared(p, pad)
            return if (p.aZ) true else p.standingOn != null
        }
        return lShared(p, pad)
    }

    // -- l() L55 block: non-79 states ----------------------------------------
    private fun lShared(p: Entity, pad: Pad): Boolean {
        when {
            pad.u(Pad.M_LEFT) || pad.x(Pad.M_LEFT) || pad.u(Pad.M_LEFT_ALT) -> {
                if (p.av) return ax(p)
                if (p.aA != 0) { p.av = true; return aw(p, pad) }
                if (pad.x(Pad.M_LEFT)) {
                    p.setAnim(10); p.ag = -4096
                    if (p.hitWall()) p.ag = 0
                    return true
                }
                return aw(p, pad)
            }
            pad.u(Pad.M_RIGHT) || pad.x(Pad.M_RIGHT) || pad.u(Pad.M_RIGHT_ALT) -> {
                if (!p.av) return ax(p)
                if (p.aA != 0) { p.av = false; return aw(p, pad) }
                if (pad.x(Pad.M_RIGHT)) {
                    p.setAnim(10); p.ag = 4096
                    if (p.hitWall()) p.ag = 0
                    return true
                }
                return aw(p, pad)
            }
            pad.u(Pad.M_DOWN) -> {
                if (p.ag != 0) { p.ag = 0; p.setAnim(32); return true }
                return aw(p, pad)
            }
            else -> {
                // L120 fold (high-confidence): running → brake 11; braking →
                // settle on anim end; everything else → aw().
                if (p.S == 12 && p.co < 4) return aw(p, pad)
                if (p.S != 11) { p.setAnim(11); return true }
                if (p.animFinished()) return aw(p, pad)
                return true
            }
        }
    }

    // -- ax() sustained run (proven) ------------------------------------------
    private fun ax(p: Entity): Boolean {
        p.ah = 0
        if (!p.aZ && p.standingOn == null) { p.cq = false; p.zz = false; return false }
        if (p.aR > 18) {
            // edge walk: open space beside the feet cell and no wall
            if (p.av && p.aV < 12 && !p.bb) { p.setAnim(26); p.ag = -1280 }
            else if (!p.av && p.aW < 12 && !p.bc) { p.setAnim(26); p.ag = 1280 }
            else runArm12(p)
        } else runArm12(p)
        // L45: slope pull — aR 14/15 pull down, 17 pushes up
        when (p.aR) {
            14, 15 -> p.ah = p.ag shr 1
            17 -> p.ah = (-p.ag) shr 1
        }
        if (p.ah < 0) p.ah = 0
        return true
    }

    private fun runArm12(p: Entity) {
        if (p.S != 12) p.co = 0
        p.setAnim(12)
        if (p.av) {
            p.ag = if (p.aT >= 18) (if (p.aT > 23) -2560 else -512) else -2560
        } else {
            p.ag = if (p.aU >= 18) (if (p.aU > 23) 2560 else 512) else 2560
        }
    }

    // -- aw() settle tail (proven, climb/drop hooks omitted) ------------------
    private fun aw(p: Entity, pad: Pad): Boolean {
        p.ag = 0; p.ah = 0
        if (!p.aZ && p.standingOn == null) {
            p.cq = false; p.zz = false
            p.enterFall(0, world)
            return true
        }
        if (p.S == 79 && !bn) {
            // overhead-solid probe — inferred literal
            if (p.e(world, ((p.W[0] + p.W[2]) / 2) / 20, p.W[1] / 20 - 1) > 12) return true
            if (pad.u(Pad.M_UP)) { p.setAnim(80); return true }
            return true
        }
        if (p.S != 2 && pad.u(Pad.M_DOWN)) {
            p.al += 20; p.probeCells(world); p.al -= 20
            val side = if (p.av) p.W[0] / 20 - 2 else p.W[2] / 20 + 2
            val below = p.W[3] / 20 + 1
            if ((p.aQ == 20 || p.aQ == 5) && p.aR == 0 &&
                p.e(world, side, below) < 12) {
                p.enterFall(0, world)   // a(257,8) ledge-drop → plain fall
            } else {
                p.setAnim(78)
            }
            return true
        }
        if (p.tick100()) { p.setAnim(1); return true }
        if (p.S == 1 && !p.animFinished()) return true
        p.setAnim(if (p.S == 81) 79 else 0)
        return true
    }

    // -- case 199 arm (proven) ------------------------------------------------
    private fun case199(p: Entity, pad: Pad) {
        if (p.aZ || p.standingOn != null) {
            if (p.hitWall() && p.ag != 0) { p.ag = 0 /* a(S,4) vfx skipped */ }
            if (pad.u(Pad.M_LEFT)) {
                if (p.av) p.ag = -2560 else p.av = true
            } else if (pad.u(Pad.M_RIGHT)) {
                if (p.av) p.av = false else p.ag = 2560
            } else if (!pad.u(Pad.M_LEFT or Pad.M_RIGHT)) {
                p.ag = 0; p.setAnim(79)
            }
        } else {
            p.cq = false
            p.enterFall(0, world)
        }
    }

    // -- S5 land arm (L464, proven) -------------------------------------------
    private fun landArm(p: Entity, pad: Pad) {
        p.ag = 0; p.ah = 0; p.aj = 0
        if (pad.v(Pad.M_UP) || pad.v(Pad.M_TAP_L) || pad.v(Pad.M_TAP_R)) {
            if (pad.v(Pad.M_TAP_L)) p.av = true
            if (pad.v(Pad.M_TAP_R)) p.av = false
            p.setAnim(21)
            return
        }
        if (pad.u(Pad.M_ANY_DIR)) { l(p, pad); return }
        if (p.animFinished()) p.setAnim(if (p.aO > 12) 79 else 0)
    }

    // -- S21/233 arm (L859, proven; rope/platform variants omitted) ------------
    private fun preJumpArm(p: Entity) {
        p.ah = 0; p.ag = 0
        if (p.animFinished()) {
            p.setAnim(22)
            // L863-L884: free jump -5120; rope (a.ax==51) -2560; prop 43 ±1024
            p.ah = -5120
            p.ag = if (p.av) -2048 else 2048
        }
    }

    // -- air family {20,22,23,25,215} (L889 block, proven core) ---------------
    private fun airFamily(p: Entity, pad: Pad) {
        p.cp = true; p.ct = true; p.cw = true
        p.aj = 1536
        if (p.S == 22 && p.animFinished()) p.P = p.P or 64
        if (p.S == 20) {
            p.ag = if (p.av) -2048 else 2048
            p.ah = -5120
        }
        if (!p.hitWall()) {
            p.airWallResolve(world)
            if (p.ah <= 0) {
                if (p.aR >= 12 && p.aQ >= 12 && p.aQ != 23) p.land(world, false)
                else if (p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5) {
                    if (p.S == 215) { p.enterFall(0, world); p.av = !p.av }
                    else p.land(world, p.aR == 4 || p.aS == 4)
                }
            }
        }
        if (p.animFinished() && p.S != 215 && p.S != 22) {
            p.enterFall(0, world)
            p.collideSides(world, true)
        }
        if (p.S == 22 && p.ah >= 0 && p.ah + p.aj < 0) p.setAnim(23)
    }

    // -- S43 fall arm (L1045, proven core) ------------------------------------
    private fun fallArm(p: Entity) {
        p.cv = true; p.cp = true; p.ct = true; p.cw = true
        if (p.S == 43 && p.hitWall()) { p.ai = 0; p.ag = 0 }
        if (p.S == 43 && p.animFinished()) p.P = p.P or 64
        if (p.aR >= 12 && p.aQ >= 12 && p.aQ != 23) {
            p.land(world, p.aR == 4 || p.aS == 4)
        } else if (p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5 ||
                   p.aR == 4 || p.aS == 4) {
            p.land(world, p.aR == 4 || p.aS == 4)
        } else {
            p.aj = 1536
        }
    }

    // -- post-switch tail (g.e() L2083 block, proven) -------------------------
    private fun postTail(p: Entity, pad: Pad) {
        if (p.cq && pad.v(Pad.M_ACTION_FAMILY)) {
            if (pad.v(Pad.M_UP) || pad.v(Pad.M_TAP_L) || pad.v(Pad.M_TAP_R)) {
                if (pad.v(Pad.M_TAP_L)) p.av = true
                if (pad.v(Pad.M_TAP_R)) p.av = false
                p.ah = 0
                if (p.S == 79 || p.S == 32 || p.S == 199) {
                    // headroom probe at hitbox top-1 (proven literal)
                    if (p.e(world, ((p.W[0] + p.W[2]) / 2) / 20,
                            p.W[1] / 20 - 1) <= 12) p.setAnim(21)
                } else if (p.aZ || p.standingOn != null) {
                    p.setAnim(233)
                } else if (pad.v(Pad.M_UP)) {
                    p.setAnim(233)
                } else {
                    p.setAnim(22)
                }
            } else if (p.S != 233) {
                p.ag = 0
            }
        } else {
            // back-dash: opposite-direction double-tap while aA window open
            if (p.av && pad.x(Pad.M_RIGHT)) { if (pad.aA > 0) { p.setAnim(25); p.ag = 0; p.ah = 0 } }
            if (!p.av && pad.x(Pad.M_LEFT)) { if (pad.aA > 0) { p.setAnim(25); p.ag = 0; p.ah = 0 } }
        }
    }

    /**
     * L1341 combo arm (subset of the g.java S67/68/69 + S112..115 arms):
     * `aj()` runs the cj/ck matchers — while S==row.anim, a 65568 tap with
     * `T >= row.minFrame` opens the window (`cl`) and queues `R = next anim`;
     * the consumed `R` fires `i(R)` at the window/end boundary; otherwise the
     * arm settles to `i(0)` (the `l()` call). The `i.aN` weakened-target
     * assassination shortcut (R=183/184 via rand) is omitted — needs lock-on.
     */
    private fun comboArm(p: Entity, pad: Pad) {
        // L1355-L1373 (proven): tap 65568 in S67/68 with a weakened lock
        // (ax11, Z0==2, aB<=bw) → R = rand%2 ? 184 : 183 finisher
        val t = world.lockTarget
        if (pad.v(Pad.M_CONTEXT) && (p.S == 67 || p.S == 68) &&
            t != null && t.ax == 11 && t.Z[0] == 2 && t.aB <= BW_MOCK &&
            t.aB > 0) {
            p.cl = false
            if (p.R == -1) p.R = if ((rng?.nextInt() ?: 0) % 2 != 0) 184 else 183
        } else if (pad.v(Pad.M_CONTEXT)) {
            comboMatch(p, CJ, true, pad); comboMatch(p, CK, false, pad)
        }
        if (p.R != -1 && (p.cl || p.animFinished())) {
            p.setAnim(p.R); p.R = -1; p.cl = false
        } else if (p.animFinished()) {
            p.setAnim(0)
        }
    }

    /**
     * Assassination finisher arm (L413 for S183, L426 for S184, proven):
     * while playing, the locked victim is snapped beside the player and put
     * into its stagger anim (106 for 183, 107 for 184); on `r()` end →
     * `aN.aB=0`, `d(aN)` (kill), `aN=null`, `i(0)`. `k.p()`/`i.O()` cutscene
     * hooks omitted.
     */
    private fun assassinArm(p: Entity) {
        p.ag = 0; p.ah = 0
        val t = world.lockTarget
        if (t != null && t.S != 106 && t.S != 107 &&
            kotlin.math.abs(t.al - p.al) < 20) {
            t.setAnim(if (p.S == 183) 106 else 107)
            t.ak = if (p.av) p.ak - if (p.S == 183) 30 else 35
                   else p.ak + if (p.S == 183) 30 else 35
            t.al = p.al
        }
        if (p.animFinished()) {
            if (t != null) {
                t.aB = 0
                t.setAnim(139)                    // i.d(aN) → corpse
                t.P = t.P and -17 or 32 or 64
                world.lockTarget = null
            }
            p.setAnim(0)
        }
    }

    /** `a(int[], boolean)` matcher (proven shape): rows r9 < n-1; the
     *  `gateLast` (r7) variant refuses the last data row. */
    private fun comboMatch(p: Entity, t: IntArray, gateLast: Boolean, pad: Pad): Boolean {
        val n = t.size / 3
        for (r9 in 0 until n - 1) {
            if (gateLast && r9 >= n - 2) return false
            if (p.R == -1 && !(p.S == t[r9] && pad.v(t[r9 + 2 * n]))) continue
            p.cl = p.T >= t[r9 + n]
            if (p.R != -1) return true
            if (p.cl) return true
            p.R = t[r9 + 1]
            return true
        }
        return false
    }

    // j.g%100 global frame counter — owned by the world tick
    private fun Entity.tick100(): Boolean = (tickCount % 100) == 0L

    var tickCount = 0L

    /**
     * `az()` (g.java:5510-5757, proven) — per-tick interact maintenance +
     * `k.bd[]` scan that produces the three links:
     *   `g`   = interact target (NPC/prop in front, <440 octagonal px),
     *   `ci`  = carry target (NPC-kind candidate at the same spot),
     *   `i.at`= mount/assassination link (ax72 arm; also written at
     *           i.java:6007 by the ax11 grab).
     * Hidden (`aA&8`) → drop `g`/`at`, bail. Hostage-carry states
     * (S270/271) hold the links; S268 drops `g` for a fresh rebind.
     */
    fun interactScan(p: Entity) {
        // -- L5-L8: hidden → clear and bail --------------------------------
        if (p.aA and 8 != 0) { p.g = null; Entity.at = null; return }
        // -- L12-L19: carry anims preserve links; S268 rebinds g -----------
        if (p.S == 270 || p.S == 271) return
        if (p.S == 268) p.g = null
        // -- L26-L35: drop stale/dead g (ax4 exempt from the aB check) -----
        p.g?.let { g ->
            if (g.ax != 4 && g.aB <= 0) p.g = null
            else if (p.h(p.ak - g.ak, p.al - g.al) > 440 ||
                     Math.abs(p.al - g.al) >= 60) p.g = null
        }
        // -- L37-L48: ci dies or moves behind → drop (facing flow is
        //    decompiler-garbled in the original — !inFrontOf is the
        //    consistent reading, `inferred`) ---------------------------
        p.ci?.let { c -> if (c.aB <= 0 || !p.inFrontOf(c)) p.ci = null }
        // -- L50-L63: ax11 Z[19]==1 targets must stay in front ------------
        p.g?.let { g -> if (g.ax == 11 && g.Z[19] == 1 && !p.inFrontOf(g)) p.g = null }
        // -- L65-L83: `i.at` — drop when dead, far, behind, or off-level --
        Entity.at?.let { a ->
            val drop = when {
                a.ax != 11 || !a.deadRelease() -> {
                    p.h(p.ak - a.ak, p.al - a.al) > 440 ||
                    (p.ak - a.ak >= 0 && p.av) || (p.ak - a.ak <= 0 && !p.av) ||
                    p.W[3] < a.W[1]
                }
                else -> true                       // dead ax11 → L83
            }
            if (drop && p.S != 277 && p.S != 293 && p.S != 298) Entity.at = null
        }
        // -- L90-L131: kind gates — dead NPCs, ax4 pose, ax58, anim-end ----
        p.g?.let { g ->
            when (g.ax) {
                11, 17, 73, 9 -> if (g.deadRelease()) p.g = null
            }
            if (p.g != null && (p.S == 295 || p.S == 303) && p.animFinished()) {
                val g2 = p.g!!
                if (g2.ax != 4 || g2.S != 30 || !p.inFrontOf(g2)) p.g = null
            }
            p.g?.let { g2 -> if (g2.ax == 4 && g2.S != 30) p.g = null }
            p.g?.let { g2 -> if (g2.ax == 58) p.g = null }
        }
        // -- L136-L142: all bound + no pending mount bit → done -----------
        if (p.g != null && Entity.at != null && p.gJ and 4 == 0 && p.ci != null) return
        // -- L144-L350: scan k.bd[] ----------------------------------------
        var best = 440                            // r6 — narrowed by L228
        for (e in world.npcs) {
            if (e.P and 32 != 0) continue         // held
            if ((e.ax == 11 || e.ax == 17 || e.ax == 73 || e.ax == 23 || e.ax == 9)
                && e.deadRelease()) continue
            if (e.ax == 4 && e.S != 30) continue
            val npcKind = e.ax == 11 || e.ax == 17 || e.ax == 23 ||
                          e.ax == 73 || e.ax == 29 || e.ax == 9
            val pathA = npcKind || (e.ax == 4 && e.S == 30) || e.ax == 58
            if (!pathA) {
                // -- L279: ax72 mount arm only ------------------------------
                if (e.ax != 72) continue
                if (p.gJ and 4 == 0 || !p.mountableState() || !e.wasHitRecently(world) ||
                    e.Z[0] == 3) continue
                if (p.av && e.ak - p.ak >= 0) continue
                if (!p.av && e.ak - p.ak <= 0) continue
                if (p.h(p.ak - e.ak, p.al - e.al) >= 440) continue
                if (e.Z[0] == 1 && p.h(p.ak - e.ak, p.al - e.al) >= e.Z[3]) continue
                if (p.W[1] < e.W[3]) continue
                if (p.W[3] < e.W[1]) continue
                if (p.losBlocked(e, world)) continue
                if (Entity.at != null) continue
                Entity.at = e
                return
            }
            // -- L200 path: dead-check, I==8 gate for ax4/58, facing, dist --
            if (e.aB <= 0 && e.ax != 4 && e.ax != 58) continue
            if (p.gI != 8 && (e.ax == 4 || e.ax == 58)) continue
            // L212-L220: facing gate — av=false needs dx>0; av=true dx<0;
            // S∈{268,291} bypass (L220).
            val inFront = if (p.av) e.ak - p.ak < 0 else e.ak - p.ak > 0
            if (!inFront && p.S != 268 && p.S != 291) continue
            val d = p.h(p.ak - e.ak, p.al - e.al)
            if (d >= best) continue                                 // L224
            best = d                                                // r6 = r03
            p.g = null                                              // L228 boundary
            if (p.S == 268) { p.g = e; continue }                   // L231
            if (npcKind) {
                // L250: !i(e) && aA∈{0,2} → skip; otherwise binds via
                // the facing/dist path already passed
                if (!p.interactEligible(e) && (e.aA == 0 || e.aA == 2)) continue
                if (p.g == null) p.g = e                            // L260
                if (p.ci == null) p.ci = e                          // L275
            } else {
                // L256-L260: i(e) or |dy|<=20 → g-bind
                if (!p.interactEligible(e) && Math.abs(p.al - e.al) > 20) continue
                if (p.g == null) p.g = e                            // L260
            }
        }
    }

    companion object {
        /** `g.b()` no-arg attack table (proven, L9→L10 in g.java). */
        private val ATTACK = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115, 183, 184, 216, 217, 286, 287)
        fun isAttackState(s: Int): Boolean = s in ATTACK

        /** Combo chain tables `cj`/`ck` from g clinit (proven):
         *  4 rows × {anim, then next-anim at +1, min-frame at +n, key at +2n}. */
        private val CJ = intArrayOf(67, 68, 69, 112, 6, 5, 100, 100, 65568, 65568, 65568, 65568)
        private val CK = intArrayOf(112, 113, 114, 115, 9, 5, 5, 100, 65568, 65568, 65568, 65568)
        /** `bw[k.au]` normal-hit reference for the weaken check (au=0). */
        private const val BW_MOCK = 80
    }
}
