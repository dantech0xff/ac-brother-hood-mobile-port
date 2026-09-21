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
class PlayerFsm(private val world: LevelCellSource) {

    var bn = false   // i.bn — blend/unlock flag (false in slice 2)

    fun tick(p: Entity, pad: Pad) {
        p.cp = true; p.cq = true; p.ct = true; p.cw = true; p.zz = true
        dispatch(p, pad)
        postTail(p, pad)
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
            67, 68, 69, 112, 113, 114, 115 -> comboArm(p, pad)  // L1341 family
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
    }

    // -- grounded family tail (L682) ----------------------------------------
    private fun groundedTail(p: Entity, pad: Pad) {
        if (p.aO <= 12 || p.aR <= 12) {
            if (p.S == 79) {
                p.ag = 0; p.ah = 0
                // S79 + down → ledge drop a(257,8): requires the masked
                // enterState + below-side probes — omitted this slice.
            }
            if (p.S == 11) p.ag = (p.ag shl 1) / 3
            // ap() attack entry (proven subset): v(65568) tap + I==1 sword →
            // ag=ah=aj=0 then i(67), or i(81) from crouch S79.
            if (pad.v(Pad.M_CONTEXT) && p.gI == 1) {
                p.ag = 0; p.ah = 0; p.aj = 0
                p.setAnim(if (p.S == 79) 81 else 67)
                return
            }
            // u(33024)||!am() — am() stubbed false (no ledge-hang port)
            if (p.hitWall()) { p.ag = 1; p.collideSides(world, true); p.ag = 0 }
            if (!l(p, pad)) {
                p.cq = false
                p.enterFall(0, world)
            }
        } else {
            p.setAnim(79)
        }
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
        if (pad.v(Pad.M_CONTEXT)) { comboMatch(p, CJ, true, pad); comboMatch(p, CK, false, pad) }
        if (p.R != -1 && (p.cl || p.animFinished())) {
            p.setAnim(p.R); p.R = -1; p.cl = false
        } else if (p.animFinished()) {
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

    companion object {
        /** `g.b()` no-arg attack table (proven, L9→L10 in g.java). */
        private val ATTACK = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115, 183, 184, 216, 217, 286, 287)
        fun isAttackState(s: Int): Boolean = s in ATTACK

        /** Combo chain tables `cj`/`ck` from g clinit (proven):
         *  4 rows × {anim, then next-anim at +1, min-frame at +n, key at +2n}. */
        private val CJ = intArrayOf(67, 68, 69, 112, 6, 5, 100, 100, 65568, 65568, 65568, 65568)
        private val CK = intArrayOf(112, 113, 114, 115, 9, 5, 5, 100, 65568, 65568, 65568, 65568)
    }
}
