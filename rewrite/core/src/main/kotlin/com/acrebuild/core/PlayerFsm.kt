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
        // g.z is a STICKY latch, not per-arm state (proven): `l()`'s head
        // arms `cp=1; cq=1; z=1` unconditionally (g.java L-l head), and the
        // latch persists across states until a `cq=0; z=0` airborne arm or
        // a grab/knockdown clears it (g.java grab sites + i.java ax-zone
        // `aA|=8; z=0` pairs). A per-tick clear here starved ap() in every
        // non-grounded state — e.g. S12 pinned at an ax4 crate, where the
        // original still lets the player slash out of the pin.
        // (no clear — armed in groundedTail, cleared at the cq=0 sites)
        // i.java:4072-4073 (proven): per-tick iframe + hit-flash decay
        if (p.gt > 0) p.gt--
        if (world.iBh > 0) world.iBh--       // g.java:572 — i.bh lock
        if (p.bh > 0) p.bh--
        // g.java:594 (proven): `i.bq` crate-top level clears when the
        // player drops below it, or enters a `c(S)` grounded state.
        if ((Entity.entBq != 0 && p.al > Entity.entBq) || p.S in GROUNDED_C)
            Entity.entBq = 0
        // g.java:535-583 (proven) — the pre-dispatch L33-L88 block:
        // `k.E.J()` companion overlay follow (:535-537, i.java:7002), the
        // `aA|256` alert window + `aA|16` cooldown (:538-575), dead →
        // `i(50)` (:576), `cn++` (:580) + `k.aA`-gated `aA|=1` (:581-583).
        world.kE?.followJ(p, world)
        if ((p.aA and 256) != 0) {
            if (p.Z[1] >= 120) {
                // proven-dead call path — `k.aY` is allocated but never
                // filled (k.java:8425; only nulled at i.java:2541), so
                // the knife throw never fires; ported verbatim so the
                // |256 → |16 flip still runs.
                if (world.iBn) world.kAyAt(0)?.let { p.spawnKnife(world, it.Z[4]) }
                p.aA = (p.aA and -257) or 16
            }
            // L48-L53: `Z[1]` decays unless the alert is blind (`i.bn`
            // && player `aA&8`); the Z[1]>=120 throw above also decays
            // on the same gate verbatim (the L46 arm falls into L48).
            if (!(world.iBn && (p.aA and 8) != 0)) {
                p.Z[1]--
                if (p.Z[1] <= 0) p.Z[1] = 0
            }
        }
        if ((p.aA and 16) != 0) {
            if (p.Z[0] <= 0) p.Z[0] = 3000
            p.Z[0] -= 62                                  // `Z[0] -= j.f`
            if (p.Z[0] <= 0) p.aA = (p.aA and -17) or 256
        }
        if (world.playerDead()) p.setAnim(50)               // g.java:576
        if (world.kAA > 0 && p.aA > 1) p.aA = p.aA or 1   // g.java:581-583
        // g.java:602-607 (proven): the terminal-velocity clamp lives in the
        // e() head — `cn` counts consecutive capped ticks and resets the
        // moment `ah` falls below 5120. (Write-only in the original —
        // debug counter, no live consumer.)
        if (p.ah > 5120) { p.ah = 5120; Entity.gCn++ } else Entity.gCn = 0
        // g.java:615 (proven): `a(an())` — the per-tick wall rescan. The
        // `an()` whitelist only gates the resolve half of `i.a(z2)`;
        // `bb`/`bc`/`aT`/`aU` refresh on every state.
        p.collideSides(world, p.rescanEligible())
        if (world.bh3) flightTick(p, pad)                      // g.n() bh3 arms
        else { dispatch(p, pad); postTail(p, pad) }
    }

    private fun dispatch(p: Entity, pad: Pad) {
        when (p.S) {
            // grounded family — L682 tail
            0, 1, 7, 11, 26, 79 -> groundedTail(p, pad)
            78, 80 -> {                       // L771
                if (p.animFinished()) p.setAnim(if (p.S == 78) 79 else 1)
            }
            // g.java:3011-3024 L1301 (proven) — S8 hit-connect lock:
            // the struck victim drives `k.aS.i(8)` on W∩X overlap
            // (i.java:1777/1914/1976/6040/6065/8090/10363/10425); the
            // player back-steps ±1280 opposite `av`, plays the footstep
            // `k.A(11)` at frame 1 (`T==1&&U==0`), and on `r()` sets
            // `P|=64` then `l()`/`aw()` resume. `i.f(this)` clamps the
            // scroll wall; the arm falls into the L1926 tail below.
            8 -> {
                if (p.T == 1 && p.U == 0) world.sfx(11)          // L1303
                p.aj = 0; p.ah = 0
                p.ag = if (p.av) 1280 else -1280
                if (p.animFinished()) {                          // r()
                    p.P = p.P or 64
                    if (!l(p, pad)) aw(p, pad)
                }
                world.scrollWallClamp(p)                         // i.f(this)
            }
            // g.java:1313-1352 (proven) — dismount/swing: `D=false`,
            // `co++`, then inside `ag!=0 && aO==0`: `A()` ladder snap →
            // i(74); side-strip ∈[19,24) → edge-count i8 picks i(107/
            // 108/109); else `co>2 && z()` wall face → `ak()` lip grab or
            // the S33 wall-rebound (`i(33); ah=-4096`).
            12 -> {
                p.gD = false                      // D = false
                p.co++
                if (p.ag != 0 && p.aO == 0) {
                    val i8 = if (p.av) p.aX else p.aY
                    val i9 = if (p.av) p.aT else p.aU
                    if (p.ladderCell(world)) {                 // A()
                        p.ai = 0; p.ag = 0; p.al = p.W[1]
                        p.ak += if (p.av) -20 else 20
                        p.setAnim(74)
                    } else if (i9 >= 19 && i9 < 24) {
                        if (i8 == 1) { p.ag = 0; p.ah = 0; p.setAnim(107) }
                        else if (i8 == 2) { p.ag = 0; p.ah = 0; p.setAnim(108) }
                        else if (i8 == 3) { p.ag = 0; p.ah = 0; p.setAnim(109) }
                        else if (p.co > 2 && p.pushColumnBlocked(world)) {
                            if (p.ledgeLipGrab(world)) {       // ak()
                                p.aj = 0; p.ah = 0; p.ag = 0
                            } else {
                                p.setAnim(33); p.ag = 0; p.ah = -4096
                            }
                        }
                    }
                }
                // (the case `break`s to the shared post-switch code —
                //  the l() input handler does NOT run for S12)
            }
            // g.java L12de (proven, fallback decompile): the 107/108/109
            // directional edge-vault anims — on `r()` (anim end) the player
            // steps onto the edge: `al -= (S - 107 + 1) * 20` (vault up
            // 1/2/3 cells by tier), `ak += ±20` (one cell into the wall by
            // facing `av`), then `a(0, 9)` — `i.a(int,int)` bit0+bit8 =
            // snap `ak` to the cell center with no anim change. Without
            // this arm S107-109 fell to the default fling and the player
            // re-armed the vault forever at a wall edge.
            107, 108, 109 -> {
                if (p.animFinished()) {                          // r()
                    p.al -= (p.S - 107 + 1) * 20
                    p.ak += if (p.av) -20 else 20
                    p.enterStateMasked(0, 9, world)              // a(0, 9)
                }
            }
            // g.java:2641-2660 (proven) — kill-QTE big launch: per tick
            // `ae=null` + `ah=0` + `G()` (releaseAe) + `v()` input flush +
            // `D=true`; the `g.p` launch arg is consumed once (1 →
            // `ag=4096/av=false`, 2 → `ag=-4096/av=true`, `ah=-768`, then
            // `i(157)` — S157 has no e() arm so the flight anim exits via
            // the default `a(0)` fling); else on `r()` near-edge →
            // `i(0)`+`E()` settle else `a(0)`.
            90 -> {
                p.ae = null
                p.ah = 0
                p.releaseAe()                          // G()
                pad.eL = 0                             // k.v() = eL=0 (:5710)
                p.gD = true                            // D = true
                if (world.gP != 0) {
                    if (world.gP == 1) { p.ag = 4096; p.av = false }
                    else if (world.gP == 2) { p.ag = -4096; p.av = true }
                    p.ah = -768
                    p.setAnim(157)
                    world.gP = 0
                } else if (p.animFinished()) {         // r()
                    if (p.aR >= 20 || p.aS >= 20) {
                        p.setAnim(0)
                        p.eSettle(world)               // E()
                    } else {
                        p.flingAirborne(0, world)      // a(0)
                    }
                }
            }
            // g.java L297b (proven) — S86 corpse-handoff: `r()` → i(326).
            // Arms no latch flags — clears like the other pinned arms.
            86 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) p.setAnim(326)
            }
            // g.java L25eb (proven) — S89 grab-pinned: `h(1)` eats the
            // pending J&1 grab request into `I=1` + `k.at=1` (and releases
            // an ax16 request link); `ai=aj=ag=ah=0`; `r()` → `P|=64`
            // (sleep the body, keep the corpse drawn).
            89 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.requestH(1, world)               // h(1) — side effects
                p.ai = 0; p.aj = 0; p.ag = 0; p.ah = 0
                if (p.animFinished()) p.P = p.P or 64
            }
            // g.java L2de0 (proven) — S110 pinned settle: `r()` → `P|=64`.
            110 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) p.P = p.P or 64
            }
            // g.java L2df5 (proven) — S165 launch/leap: `aj=1536` capped
            // gravity; `r()` → `a(0)` (the masked fling — resolves to
            // S43 + `al+=10`).
            165 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.aj = 1536
                if (p.animFinished()) p.flingAirborne(0, world)
            }
            // g.java L1ce4 (proven) — S19/S36 air variants: `cv=1` then
            // the shared L1ce8 air-family tail (same body as airFamily).
            19, 36 -> {
                p.cv = true
                airFamily(p, pad)
            }
            // g.java L1ce8 (proven) — S24/S157: the shared air tail
            // without the `cv` arm.
            24, 157 -> airFamily(p, pad)
            // g.java L3078 (proven) — S49 slide-settle: `T>9 → ag=ah=0`
            // (frame-9 stop), `r() → i(0); E()`; early `return`.
            49 -> {
                if (p.T > 9) { p.ag = 0; p.ah = 0 }
                if (p.animFinished()) { p.setAnim(0); p.eSettle(world) }
                return
            }
            // g.java L169c (proven) — S74 leap-dash: `r() → ak±40; a(0)`;
            // early `return`.
            74 -> {
                if (p.animFinished()) {
                    p.ak += if (p.av) -40 else 40
                    p.flingAirborne(0, world)
                }
                return
            }
            // g.java L2989→L2de0 (proven) — S82-85/S326 corpse-sleep:
            // `r() → P|=64` (the L2989 label is empty and falls through
            // to L2de0's arm).
            82, 83, 84, 85, 326 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) p.P = p.P or 64
            }
            // g.java L30a9 (proven) — S91 settle: zero all four velocity
            // fields; `r() → i(0)`; early `return`.
            91 -> {
                p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                if (p.animFinished()) p.setAnim(0)
                return
            }
            // g.java L1a46 (proven) — S92/S101 wall-bounce: zero all,
            // `r() → av=!av; ag=∓2048 (new facing); aO==20 → ah=0 else
            // ah=-5120`, then `a(36,36)` re-enters the wall state.
            92, 101 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    p.av = !p.av
                    p.ag = if (p.av) -2048 else 2048
                    p.ah = if (p.aO == 20) 0 else -5120
                    p.enterStateMasked(36, 36, world)
                }
            }
            // g.java L11c2 (proven) — S122 bind-prep: `r() → a(53,1032)`.
            122 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) p.enterStateMasked(53, 1032, world)
            }
            // g.java L2eb9 (proven) — S148 knockback-launch: `ah=-5120`;
            // `r() → i(149)`.
            148 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.ah = -5120
                if (p.animFinished()) p.setAnim(149)
            }
            // g.java L2ed1 (proven) — S149 fling carry: `ag = g.l ? g.l :
            // ∓1024`; `aj=1536`; `r() → g.l=0; ag=0; i(150)`.
            149 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.ag = if (p.gL != 0) p.gL else (if (p.av) -1024 else 1024)
                p.aj = 1536
                if (p.animFinished()) {
                    p.gL = 0; p.ag = 0; p.setAnim(150)
                }
            }
            // g.java L2f42 (proven) — S152 slide: `ag=∓1024`; `r() →
            // i(0)`; early `return`.
            152 -> {
                p.ag = if (p.av) -1024 else 1024
                if (p.animFinished()) p.setAnim(0)
                return
            }
            // g.java L2f13 (proven) — S156 launch-prep: zero all; `r() →
            // ag=g.l; ah=0; i(157)`; early `return`.
            156 -> {
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    p.ag = p.gL; p.ah = 0; p.setAnim(157)
                }
                return
            }
            // g.java L2596 (proven) — S204 victim-dump loop: `r() →
            // i(203)`; early `return` (skips the shared tail).
            204 -> {
                if (p.animFinished()) p.setAnim(203)
                return
            }
            // g.java L305d (proven) — S214 knockback-rise: `ah=-768`;
            // `r() → ah=0; i(215)`; early `return`.
            214 -> {
                p.ah = -768
                if (p.animFinished()) { p.ah = 0; p.setAnim(215) }
                return
            }
            // g.java L2f66/L311a/L311b (proven) — S225/244/250 fully
            // inert: the arm is a bare `return` — no flags, no tail.
            225, 244, 250 -> return
            // g.java L2b6c (proven) — S282 pinned: `r() → i(38)`; early
            // `return`.
            282 -> {
                if (p.animFinished()) p.setAnim(38)
                return
            }
            // g.java L309c (proven) — S283: `r() → i(0)`; early `return`.
            283 -> {
                if (p.animFinished()) p.setAnim(0)
                return
            }
            // g.java:1938-2015 + L1ab3-L1c33 (proven) — wall-rebound:
            // `cp=true`, `aj=512`; `A()` → ledge snap i(74);
            // direction-toward-av HELD && `ah<0` (still rising) → ct=true +
            // the r()-gated i4/i5 wall-column pocket scan → ak() lip grab;
            // ANY other case (no dir-hold, or holding while falling) →
            // x() probe, `aR|aS∈{5,20}` → a(43,32) fall else
            // a(34,36)+aA() wall-kick.
            33 -> {
                p.cp = true; p.aj = 512
                if (p.ladderCell(world)) {                     // A()
                    p.aj = 0; p.ah = 0; p.ai = 0; p.ag = 0
                    p.al = p.W[1]
                    p.ak = if (p.av) p.W[0] - 10 else p.W[2] + 10
                    p.setAnim(74)
                } else {
                    val dirKey = if (!p.av) pad.u(Pad.M_RIGHT)
                                 else pad.u(Pad.M_LEFT)
                    // (proven, g.java L1b0a-L1b8f — branch order matters:
                    //  holding toward the wall WHILE RISING arms the lip
                    //  scan; any other case probes for the kick/fall.)
                    if (dirKey && p.ah < 0) {
                        p.ct = true
                        if (p.animFinished()) {                          // r()
                            var z4 = false
                            var i10 = (p.W[1] + 10) / 20
                            val i4: Int; val i5: Int
                            if (p.av) { i4 = (p.W[0] - 5) / 20; i5 = i4 + 1 }
                            else { i4 = (p.W[2] + 5) / 20; i5 = i4 - 1 }
                            var i13 = 0
                            while (i13 < 2) {           // JADX while(true)
                                i10 -= i13              // elided else-break
                                if (p.e(world, i4, i10) < 19 ||
                                    p.e(world, i4, i10 - 1) > 0 ||
                                    p.e(world, i5, i10) > 0 ||
                                    p.e(world, i5, i10 - 1) > 0) i13++
                                else { z4 = true; break }
                            }
                            if (z4 && p.ledgeLipGrab(world)) {
                                p.aj = 0; p.ah = 0; p.ag = 0
                            }
                        }
                    } else {
                        p.probeCells(world)                            // x()
                        if (p.aR == 5 || p.aR == 20 ||
                            p.aS == 5 || p.aS == 20) {
                            p.ah = 0; p.ag = 0
                            p.enterStateMasked(43, 32, world)
                        } else {
                            p.ct = false; p.ag = 0; p.ah = 1536
                            p.enterStateMasked(34, 36, world)
                            wallJumpKick(p, world, pad)                       // aA()
                        }
                    }
                }
            }
            // g.java:2018-2032 (proven) — wall-jump rise partner: `aA()`
            // every tick, `(av?aT:aU)!=20 → a(0)`, `aR>=19||==5 →
            // l()+k.v()`.
            34 -> {
                wallJumpKick(p, world, pad)                                   // aA()
                if ((if (p.av) p.aT else p.aU) != 20)
                    p.flingAirborne(0, world)                          // a(0)
                if (p.aR >= 19 || p.aR == 5) {
                    l(p, pad)
                    world.clearLatches()                               // k.v()
                }
            }
            32 -> {                           // case 32 — run-start end → settle
                world.scrollWallClamp(p)      // g.java:1895 head — i.f(this)
                if (p.animFinished()) {
                    p.ag = 0
                    p.setAnim(if (p.Q == 79) 79 else 0)
                }
            }
            // `e()` case 37 (L1560, proven — fallback g.java:6461+): the
            // grapple-climb step. Bound to an ax10-S32 grapple zone or
            // grounded (aO==5): dir-held into facing + open facing cell →
            // i(37) + ag=∓1536 step; opposite-dir input flips `av`; else
            // `r()` → i(38) bound stance. Non-bound without ground falls.
            37 -> {
                val bound = p.ac != null && p.ac!!.ax == 10 && p.ac!!.S == 32
                if (bound || p.aO == 5) {
                    if (!bound) p.al = ((p.W[1] / 20) * 20) + 10
                    p.ag = 0
                    val dirHeld = if (p.av) pad.u(Pad.M_LEFT) else pad.u(Pad.M_RIGHT)
                    if (dirHeld && p.facingCellOpen(world)) {
                        p.setAnim(37)
                        p.ag = if (p.av) -1536 else 1536
                    } else {
                        val oppDir = if (p.av) pad.v(Pad.M_RIGHT) else pad.u(Pad.M_LEFT)
                        if (oppDir) p.av = !p.av
                        else if (p.animFinished()) p.setAnim(38)
                    }
                } else {
                    p.al = p.W[3]
                    p.setAnim(43)
                }
            }
            // `e()` case 38 (L1502, proven — structured g.java:2101+):
            // the bound stance (grapple zone ax10-S32, crate, door or
            // carrier `ac`). Grapple-bound: `u(16388)` or dir-tap into
            // facing → `G(); al-=20; i(23); ah=2560` spring-jump with
            // horizontal launch on dir keys + `cq`. Else-grounded:
            // snap to cell + `u(16388)` → `G()` + `a(54,8)` vault-out on
            // open head cell. `u(33024)` → drop-check → `i(43)` fall;
            // same-facing held → i(37) climb; opposite → `av` flip.
            38 -> {
                p.ag = 0
                val grapple = p.ac != null && p.ac!!.ax == 10 && p.ac!!.S == 32
                if (grapple) {
                    if (pad.u(Pad.M_UP) || (if (p.av) pad.u(Pad.M_TAP_L) else pad.u(Pad.M_TAP_R))) {
                        p.releaseAe()                                     // G()
                        p.al -= 20
                        p.setAnim(23)
                        p.ah = 2560
                        if (pad.u(Pad.M_TAP_L) || pad.u(Pad.M_TAP_R)) {
                            p.ag = if (p.av) -4096 else 4096
                        }
                        p.cq = true
                    }
                } else if (p.aO != 5) {
                    p.al = p.W[3]
                    p.setAnim(43)
                } else {
                    p.al = ((p.W[1] / 20) * 20) + 10
                    if (pad.u(Pad.M_UP)) {
                        p.releaseAe()                                     // G()
                        p.al -= 20
                        p.probeCells(world)                               // x()
                        p.al += 20
                        if (p.aO == 0) p.enterStateMasked(54, 8, world)
                    }
                }
                if (pad.u(Pad.M_DOWN)) {
                    p.al += 20
                    p.probeCells(world)                                   // x()
                    p.al -= 20
                    if (p.aO == 0) {
                        p.al = p.W[3] + 10
                        p.setAnim(43)
                    }
                } else if (if (p.av) !pad.u(Pad.M_LEFT) else !pad.u(Pad.M_RIGHT)) {
                    p.setAnim(37)
                } else if (if (p.av) pad.u(Pad.M_LEFT) else pad.v(Pad.M_RIGHT)) {
                    p.av = !p.av
                }
            }
            // `e()` case 27 (fallback g.java:5811-5818 L27da, proven):
            // link lost → `a(0)` fling; the rest is the shared tail.
            27 -> { if (p.ac == null) p.flingAirborne(0, world) }
            // `e()` case 28/318 (L23fa, proven): freeze then a held
            // dir-key (`v(33024)`) flings with the current `ah` (0).
            28, 318 -> {
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                if (world.padHeld(33024)) p.flingAirborne(p.ah, world)
            }
            // `e()` case 29/315 (L23bb, proven): `g.o != 0` →
            // `al > o` upgrades to the next climb anim (315→318,
            // 29→28); else `g.k == -1` → `a(ah)` fling.
            29, 315 -> {
                if (p.go != 0) {
                    if (p.al > p.go) p.setAnim(if (p.S == 315) 318 else 28)
                } else if (p.gk == -1) p.flingAirborne(p.ah, world)
            }
            // L1863 — lunge states tick the arc (g.java:886-906 dispatch)
            272, 273, 274, 275, 292 -> {
                val mount = Entity.at
                if (mount != null && mount.Z[0] == 4 && p.cE <= 0) {
                    p.mountOrbitTick(world, pad)      // L1863 tail — cart
                } else p.lungeTick(world)
            }
            298 -> {                          // L1293
                if (p.animFinished()) p.P = p.P or 64
                val mount = Entity.at
                if (mount != null && mount.S != 168) p.lungeTick(world)
            }
            // mounted/riding states — L1872 + siblings (g.java:886-925)
            277, 293 -> p.mountOrbitTick(world, pad)
            294, 310 -> { }                     // L1874/L1888 — anim only
            299, 300, 301, 302 -> {             // L1885 — windup → S303
                if (p.animFinished()) p.setAnim(303)
            }
            303 -> {                          // L1876 — cart dismount
                if (pad.u(62430)) p.setAnim(0)
                else if (p.animFinished()) {
                    p.P = p.P or 64
                    p.interactGauge(world)
                    // L1879 (g.java:3445, proven): 65568 edge -> ar()
                    if (pad.v(Pad.M_CONTEXT)) p.interactAction(world, pad)
                }
            }
            // L204 (g.java:2541, proven): the mounted-gauge loop — `J&8`
            // pending → `h(8)`; sfx19; `I==8` runs `aB()` then a 65568 tap
            // with a bound `g` → `aq()`; the `y()` edge flag drops the
            // mount (a=null) + air-throw `a(0)` + `i=false` + `k.ae=this`.
            295 -> {
                if (p.gJ and 8 != 0) p.requestH(8, world)
                world.sfx(19)
                if (p.gI == 8) {
                    p.interactGauge(world)
                    if (pad.v(Pad.M_CONTEXT) && p.g != null)
                        p.mountedInteractAction(world, pad)
                }
                if (p.edgeFlag()) {
                    world.vehicle = null            // g.a = null
                    p.flingAirborne(0, world)       // g.a(0)
                    world.iFlag = false             // g.i = false
                    world.aeRef = p                 // k.ae = this
                }
            }
            // L218 (g.java:2560, proven): mounted reach-anim end → gauge
            304, 305, 306 -> {
                if (p.animFinished()) { p.K = 0; p.cN = 0; p.setAnim(295) }
            }
            // L1926-L1947 (g.java:3455+): S297 + the S296/307-309/276/
            // 278-281/285/288-290/314/316 family all fall into the shared
            // postTail chain (ab/aR/aO bookkeeping) — no dedicated arm.
            297 -> { }
            311, 312 -> {                     // L1889
                p.collideSides(world, true)
                if (p.animFinished()) {
                    val mount = Entity.at
                    if (mount == null || mount.Z[0] != 4 || p.cE > 0) {
                        p.lungeTick(world)
                    } else p.mountOrbitTick(world, pad)
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
            // g.java:1265-1290 (proven) — the shared stagger/recoil arm:
            // `ag/=2` decay + `ab` held item tracks the player's box;
            // side strip ≥19 kills `ag`; `r()` → `bl=0`, drop `ae` (`G()`)
            // and `ab` (`H()`), `i(1)` + `a(false)` rescan, then
            // `!M() && a==null → a(0)` when no floor sits ahead; a
            // `Q∈{0,54}` (came from idle/roll) pulses `ah=1; a(true)`.
            9, 10 -> {
                p.ag /= 2; p.ah = 0; p.aj = 0
                p.ab?.let { it.av = p.av; it.ak = p.ak; it.al = p.al }
                if ((if (p.av) p.aU else p.aT) >= 19) p.ag = 0
                if (p.animFinished()) {
                    p.bl = 0
                    p.releaseAe()                        // G()
                    p.dropHeld()                         // H()
                    p.setAnim(1)
                    p.collideSides(world, false)       // a(false)
                    if (!p.floorAhead(world) && p.standingOn == null) {
                        p.flingAirborne(0, world)        // g.a(0)
                    }
                }
                if (p.Q == 0 || p.Q == 54) {
                    p.ah = 1
                    p.collideSides(world, true)        // a(true)
                    p.ah = 0
                }
                world.scrollWallClamp(p)                 // i.f(this)
            }
            21, 233 -> preJumpArm(p)          // L859
            // g.java:1490-1497 (proven) — wall-kick: hold still for the
            // wind-up anim, then `r()` → i(18) + ∓2048 horizontal,
            // -5120 vertical (kick off the wall).
            17 -> {
                p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    p.setAnim(18)
                    p.ag = if (p.av) -2048 else 2048
                    p.ah = -5120
                }
            }
            20, 22, 23, 25, 215 -> airFamily(p, pad)
            // shared fall case (g.java:1400 `case 16/35/43/150/252`)
            16, 35, 43, 150, 252 -> fallArm(p, pad)
            // g.java:2736-2757 (proven) — S102 wall-cling: grounded →
            // `l()` input-consume; else `aC` counts down — on expiry or
            // wall-contact lost (`aR!=4`) `a(0)` fling + `al+=21` drop;
            // dir-held → `av` + i(332) shimmy; UP edge / toward-wall tap
            // (av?2:8) → i(17) wall-kick.
            102 -> {
                if (p.aZ) {
                    l(p, pad)
                } else {
                    val i15 = p.aC
                    p.aC = i15 - 1
                    if (i15 <= 0 || p.aR != 4) {
                        p.flingAirborne(0, world)
                        p.al += 21
                    } else if (pad.u(Pad.M_LEFT)) {
                        p.av = true; p.setAnim(332)
                    } else if (pad.u(Pad.M_RIGHT)) {
                        p.av = false; p.setAnim(332)
                    } else if (pad.v(Pad.M_UP) ||
                               (p.av && pad.v(2)) || (!p.av && pad.v(8))) {
                        p.setAnim(17)
                    }
                }
            }
            // g.java:4065-4099 (proven) — S317 wall-run dash: `aj=128`,
            // `ah` capped 512, `ag=∓2560`; UP edge hops `ah=-2048`.
            // `y()` wall / `aR>=12 || aR==5` contact → `ag=0` + spawn the
            // clip-30 marker `g.f` (`i.a(8,30,4,av,±W,al,300)`); when its
            // anim finishes → `k.c(f)` + `i(50)` crash death. Zone latch:
            // `!g.q` → `G()` release `ae`; in-zone → `a(1,ak,al-60)`
            // dust marker. (Script/zone-entered — no i(317) call sites.)
            317 -> {
                p.aj = 128
                if (p.ah >= 512) p.ah = 512
                p.ag = if (p.av) -2560 else 2560
                if (pad.v(Pad.M_UP)) p.ah = -2048
                if (p.hitWall() || p.aR >= 12 || p.aR == 5) {
                    p.ag = 0
                    if (world.clipFor(30) != null && Entity.gf == null) {
                        Entity.gf = p.spawnFx8(world, 30, 4, p.av,
                            if (p.av) p.W[0] else p.W[2], p.al, 300)
                    }
                    val fm = Entity.gf
                    if (fm != null && fm.animFinished()) {
                        world.removeEntity(fm)              // k.c(f)
                        Entity.gf = null
                        p.aj = 0; p.ah = 0
                        p.setAnim(50)
                    }
                }
                if (!Entity.gq) {
                    p.releaseAe()                           // G()
                } else {
                    p.spawnMarker(world, 1, p.ak, p.al - 60) // a(1,ak,al-60)
                }
            }
            // g.java:4100-4138 (proven) — S332 wall-shimmy: `ag=∓2560`
            // cut at aT/aU>=12 walls; `aZ` → `l()`; `aR!=4` lost wall →
            // `a(0)` fall; UP/toward-tap → i(17); dir-held re-faces +
            // re-enters shimmy; `r()` → vel0 + `aC=18` + i(102) cling.
            332 -> {
                p.ag = if (p.av) -2560 else 2560
                if (p.aT >= 12 && p.ag < 0) p.ag = 0
                if (p.aU >= 12 && p.ag > 0) p.ag = 0
                if (p.aZ) {
                    l(p, pad)
                } else if (p.aR != 4) {
                    p.flingAirborne(0, world)
                } else if (pad.v(Pad.M_UP) ||
                           (p.av && pad.v(2)) || (!p.av && pad.v(8))) {
                    p.setAnim(17)
                } else {
                    if (pad.u(Pad.M_LEFT)) { p.av = true; p.setAnim(332) }
                    else if (pad.u(Pad.M_RIGHT)) { p.av = false; p.setAnim(332) }
                    if (p.animFinished()) {
                        p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                        p.aC = 18
                        p.setAnim(102)
                    }
                }
            }
            // S54 dismount settle (g.java:2223, proven): anim end →
            // `al-=20` then `E()` settle-sink then `i(0)`. No `i(54)`
            // call sites — script/dismount-entered, like S317.
            54 -> {
                if (p.animFinished()) {
                    p.al -= 20
                    p.eSettle(world)
                    p.setAnim(0)
                }
            }
            257 -> ledgeDropArm(p)            // L1770
            // g.java L2b3b (proven) — S56 grab-settle: `ah=ag=0`; anim end
            // → any key held (`u(127999)`) → i(65) shimmy, else i(59)
            // hang-idle.
            56 -> {
                // arm sets no latch flags — clears them to model the
                // head-clear (the tick head's universal true-set is a
                // slice-168 deviation; without this, postTail's cq-jump
                // gate would hijack edges the original routes here).
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    if (pad.u(127999)) p.setAnim(65) else p.setAnim(59)
                }
            }
            // g.java L2421 (proven) — S60 ledge-hang: when it didn't
            // arrive via the S63 climb (Q!=63) or UP/toward-wall is held
            // → i(62) climb-up; always latches `cu`.
            60 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.Q != 63 || pad.u(Pad.M_UP) ||
                    (p.av && pad.u(4114)) || (!p.av && pad.u(8264))) {
                    p.setAnim(62)
                }
                p.cu = true
            }
            // g.java L2460 (proven) — S61/S203 shared ledge-hang:
            // `ag=ah=0`. S61 counts `aC` down — the one-shot `aC==0`
            // tick, a lost front cell (`e<12`) with no `ga` link, or
            // `v(33024)` all take the drop-release `H();G();al+=W3-W1;
            // a(0)` (an ax43 carrier link blocks it). S203 (victim
            // carry) skips `aC` and instead tracks `g.h`: link gone/dead
            // → `G()`; alive → `k.c(ak,al-85,gh.aw)` marker + `v(65568)`
            // dumps the victim (`G();i(204);gh.ak=ak±10;gh=null`).
            // Shared tail: UP/toward-wall edge → `H();G();i(62)`.
            61, 203 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                p.ag = 0; p.ah = 0
                var drop = false
                if (p.S != 203) {
                    p.aC--
                    if (p.aC == 0) {
                        drop = true                            // grace expired
                    } else {
                        val cell = p.e(world,
                            (p.ak + if (p.av) -10 else 10) / 20,
                            (p.al + 10) / 20)
                        if (cell >= 12) {
                            if (pad.v(33024)) drop = true      // manual release
                        } else if (p.ga == null || pad.v(33024)) {
                            drop = true                        // edge lost / link-drop
                        }
                    }
                } else if (pad.v(33024)) {
                    drop = true
                }
                if (drop && !(p.ga != null && p.ga!!.ax == 43)) {
                    p.dropHeld()                               // H()
                    p.releaseAe()                              // G()
                    p.al += p.W[3] - p.W[1]
                    p.flingAirborne(0, world)                  // a(0)
                }
                if (p.S == 203) {
                    val h = p.gh
                    if (h == null || h.deadRelease()) {
                        p.releaseAe()                          // L2558 — G()
                    } else {
                        world.showPrompt(p.ak, p.al - 85, h.aw)// k.c(ak,al-85,aw)
                        if (pad.v(65568)) {
                            p.releaseAe()
                            p.setAnim(204)
                            h.ak = p.ak + (if (p.av) 10 else -10)
                            p.gh = null
                        }
                    }
                }
                if (pad.v(Pad.M_UP) || (p.av && pad.v(4114)) ||
                    (!p.av && pad.v(8264))) {
                    p.dropHeld(); p.releaseAe()                // H(); G()
                    p.setAnim(62)
                }
            }
            // g.java L25b5 (proven) — S62 climb-up finish: `r()` → step
            // ±10 then `a(aO>12 ? 79 : 0, 9)` settle.
            62 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) {
                    p.ak += if (p.av) -10 else 10
                    p.enterStateMasked(if (p.aO > 12) 79 else 0, 9, world)
                }
            }
            // g.java L25a5 (proven) — S63 climb anim end → i(60) hang.
            63 -> {
                p.cp = false; p.cq = false; p.ct = false; p.cw = false
                if (p.animFinished()) p.setAnim(60)
            }
            67, 68, 69, 112, 113, 114, 115 -> comboArm(p, pad)  // L1341 family
            // S357 scripted leap (g.java:4154, proven): `ag=3328` but
            // `av → ag=-1280` (asymmetric — verbatim); anim end →
            // `ag=0;K=4;i(364);ar()`.
            357 -> {
                p.ag = 3328
                if (p.av) p.ag = -1280
                if (p.animFinished()) {
                    p.ag = 0; p.K = 4
                    p.setAnim(364)
                    p.interactAction(world, pad)
                }
            }
            // S360 perch (g.java:4167, proven): respawns the lead
            // marker `a(i21,ak+i22,al-85)` each tick — (9,40) facing
            // right, (106,-40) facing left; forward press → `G();i(357)`
            // relaunch; anim end → `G();i(0)`. No `i(360)` call sites —
            // script-entered like S317.
            360 -> {
                val i21 = if (p.av) 106 else 9
                val i22 = if (p.av) -40 else 40
                p.spawnMarker(world, i21, p.ak + i22, p.al - 85)
                if ((!p.av && pad.v(Pad.M_RIGHT)) ||
                    (p.av && pad.v(Pad.M_LEFT))) {
                    p.releaseAe()
                    p.setAnim(357)
                }
                if (p.animFinished()) {
                    p.releaseAe()
                    p.setAnim(0)
                }
            }
            // S370 boss-grab windup (g.java:4185, proven): `r()` → i(371).
            370 -> {
                if (p.animFinished()) p.setAnim(371)
            }
            // S374 KO-settle (g.java:4211, proven): vel0; anim end →
            // `x[1]>0` → vel0 + `i(376)` recovery, else `k.l(12)`
            // mission-fail.
            374 -> {
                p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    if (p.x1 > 0) {
                        p.ah = 0; p.ag = 0
                        p.setAnim(376)
                    } else {
                        world.screenL(12)
                    }
                }
            }
            // g.java:4245-4309 (proven) — ax61 aura knockback slide:
            // S375 skid ±1280 → S376 halt → S377 recover → a(0).
            375 -> {
                p.ag = if (p.av) -1280 else 1280
                if (p.animFinished()) p.setAnim(376)
                world.scrollWallClamp(p)                        // i.f(this)
            }
            376 -> {
                p.ah = 0; p.ag = 0
                if (p.animFinished()) p.setAnim(377)
                world.scrollWallClamp(p)
            }
            377 -> {
                if (p.animFinished()) p.enterFall()             // a(0)
                world.scrollWallClamp(p)
            }
            // ---- case 146/147 (g.java:2859-2918, proven) — wall/pass
            // sequence arms: S146 drives forward at walk speed until the
            // anim ends or the facing side-strip is no longer wall (3),
            // then bumps `al` a cell and enters S147 which latches `g.j`,
            // restores the saved music slot, plays sfx 18, runs the full
            // `k.a(true)` level reset and restores `k.az` from bA[32].
            146 -> {
                p.ag = if (p.av) -2048 else 2048
                if (p.animFinished() ||
                    (p.av && p.aT != 3) || (!p.av && p.aU != 3)) {
                    p.al += 20
                    p.setAnim(147)
                    Entity.grabLatch = true          // g.j = true (:2865)
                }
            }
            147 -> {
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    if (p.S == 145) p.al += 20       // dead conjunct — S
                    // is 147 here (verbatim quirk, :2894)
                    Entity.grabLatch = true          // g.j = true (:2898)
                    world.kBg = if (world.musicActive()) world.kBH else -1
                    world.sfx(18)                    // k.A(18)
                    world.resetLevel(true)           // k.a(true) (:5139)
                    world.kAz = world.kBA[32]        // k.a(bA,32) short
                }
            }
            // ---- case 183 (g.java:3041-3097, proven) — assassination
            // finisher anim: while it plays the weakened `i.aN` victim is
            // dragged into its own death anim (i(106) ∓30px) with the kill
            // tally + payoff per tick; when the anim ends (or the lock is
            // gone) release — k.p() + i.O() + i(0), victim aB=0 + i.d()
            // reset, i.aN = null. The embedded `S==184` check below is a
            // verbatim dead conjunct (S is 183 in this arm, :3059).
            183 -> {
                val aN = world.lockTarget
                if (aN != null && aN.S != 106 &&
                    Math.abs(aN.al - p.al) < 20) {
                    aN.setAnim(106)                     // victim → i(106)
                    world.kStatE(p.aw)                  // k.e(0,aw)
                    aN.victimPayoff(world)              // i.aN.S() (:7276)
                    aN.ak = if (p.av) p.ak - 30 else p.ak + 30
                    aN.al = p.al
                }
                // `if (S==184 && aN!=null && aN.S!=107 && |Δal|<20)` —
                // verbatim dead inside case 183 (g.java:3059); the live
                // copy is in the 184/205 arm.
                p.ag = 0; p.ah = 0
                // `if (!r() || i.aN == null)` — JADX renders the end-gate
                // as `!r()` but that releases mid-anim; taken as the
                // `r()` end-trigger + `aN==null` early-out (g.java:3073,
                // branch-inversion noise in the mangled dump).
                if (p.animFinished() || aN == null) {
                    p.unlockInput(world)                // k.p()
                    p.eventDisarm(world)                // i.O()
                    p.setAnim(0)                        // i(0)
                    if (aN != null) {
                        aN.aB = 0
                        aN.releaseAnimReset()           // i.d(iVar)
                        world.lockTarget = null         // i.aN = null
                    }
                }
            }
            // ---- case 284 (g.java:3837-3841, proven) — rope-grab anim:
            // `r() → P|=64` (the ax13 rope FSM reads the flag and keeps
            // driving ak/al); staying in S284 until the rope hands off —
            // the default arm's anim-end `a(0)` fling must NOT run here.
            284 -> {
                if (p.animFinished()) p.P = p.P or 64
            }
            // ---- case 184/205 (g.java:3098-3140, proven) — the second
            // finisher anim: S184 drags the victim to i(107) ±35px; S205
            // shares the arm with no drag. Same `!r()`→`r()` end-gate
            // reading as case 183 (:3120).
            184, 205 -> {
                val aN = world.lockTarget
                if (p.S == 184 && aN != null && aN.S != 107 &&
                    Math.abs(aN.al - p.al) < 20) {
                    aN.setAnim(107)                     // victim → i(107)
                    world.kStatE(p.aw)                  // k.e(0,aw)
                    aN.victimPayoff(world)              // i.aN.S()
                    aN.ak = if (p.av) p.ak + 35 else p.ak - 35
                    aN.al = p.al
                }
                p.ag = 0; p.ah = 0
                if (p.animFinished()) {                 // `r()` end (:3120)
                    p.unlockInput(world)                // k.p()
                    p.eventDisarm(world)                // i.O()
                    p.setAnim(0)
                    if (aN != null) {
                        aN.aB = 0
                        aN.releaseAnimReset()
                        world.lockTarget = null
                    }
                }
            }
            // `e()` cases 50/241 (L447, proven — structured g.java:2160+):
            // crush death — frame-1 `d(999)` + `ab` drop + sfx18, then
            // `ag>>=2` damping; a bound crate pushes the corpse (`ag+=a.ag`),
            // a bound carrier snaps it (`al=a.al`); `r()` → `k.l(12)` fail.
            50, 241 -> {
                if (p.T == 1 && p.U == 0) {
                    p.drainMeter(999)                                    // d(999)
                    p.ab = null
                    world.sfx(18)                                        // k.A(18)
                }
                p.ag = p.ag shr 2; p.ah = 0; p.aj = 0
                if (p.ga != null) {
                    if (p.ga!!.ax == 51) p.ag += p.ga!!.ag
                    else if (p.ga!!.ax == 43) p.al = p.ga!!.al
                    p.ga = null
                }
                if (p.animFinished()) world.stateL(12)                   // k.l(12)
            }
            // `e()` cases 228/358 (L1737, proven — structured g.java:3282+):
            // ride/push bound stance — zero velocity; `u(16388)` or
            // dir-tap into facing → facing-vs-crate-side check, mismatch
            // releases into `i(235)` + `ac=null` + `G()` (ac==null is a
            // no-op, the stance persists); else `v(4112/8256)` → `av`.
            // `r()` on S228 → `a(0)` + `i(252)` dismount fall.
            228, 358 -> {
                p.aj = 0; p.ah = 0; p.ai = 0; p.ag = 0
                if (pad.u(Pad.M_UP) ||
                    (if (p.av) pad.u(Pad.M_TAP_L) else pad.u(Pad.M_TAP_R))) {
                    if (p.ac != null && p.av != (p.ac!!.ak < p.ak)) {
                        p.setAnim(235)
                        p.bindAc(null)                                   // ac = null
                        p.releaseAe()                                    // G()
                    }
                } else if (pad.v(Pad.M_LEFT)) {
                    p.av = true
                } else if (pad.v(Pad.M_RIGHT)) {
                    p.av = false
                }
                if (p.animFinished() && p.S == 228) {
                    p.setAnim(0)
                    p.setAnim(252)
                }
            }
            // `e()` cases 235/238 (L536, proven — structured g.java:3350+):
            // push/pull entry — `T==1`: bound ax66/ax51 → homing velocity
            // `(ac-ak)<<8 / r96` (r96 = ax66?8 : ax51?10→5 when within 60
            // of either axis); unbound/wrong-ax → `ag=±4864, ah=-6656`
            // dive. Bound → `r()` chains into push-hold (235→236, 238→239);
            // unbound → `av()` + `y()→ai=ag=0;a(0)` + `r()→a(0)`.
            235, 238 -> {
                val held = p.ac != null && (p.ac!!.ax == 66 || p.ac!!.ax == 51)
                if (p.T == 1) {
                    if (held) {
                        var i16 = if (p.ac!!.ax == 51) 10 else 8
                        if ((Math.abs(p.ac!!.ak - p.ak) < 60 ||
                            Math.abs(p.ac!!.al - p.al) < 60) && p.ac!!.ax == 51) {
                            i16 = i16 shr 1
                        }
                        p.ag = 0; p.ah = 0
                        p.ag = ((p.ac!!.ak - p.ak) shl 8) / i16
                        p.ah = ((p.ac!!.al - p.al) shl 8) / i16
                    } else {
                        p.ag = if (p.av) -4864 else 4864
                        p.ah = -6656
                    }
                }
                if (held) {
                    if (p.animFinished()) p.setAnim(if (p.S == 235) 236 else 239)
                } else {
                    p.airWallResolve(world)                              // av()
                    if (p.climbCheck()) {                                // y()
                        p.ai = 0; p.ag = 0
                        p.setAnim(0)
                    }
                    if (p.animFinished()) p.setAnim(0)
                }
            }
            // `e()` cases 236/239 (L620, proven — structured g.java:3390+):
            // push-hold — bound-crate check lost (`ac==null||ax!=51||
            // S!=8`) → `aj=512` gravity accel; bound but `al <= ac.al`
            // (player bottom at/above crate's) → `a=null; a(0)` release.
            236, 239 -> {
                if (p.ac == null || p.ac!!.ax != 51 || p.ac!!.S != 8) {
                    p.aj = 512
                } else if (p.al <= p.ac!!.al) {
                    p.ga = null                                          // a = null
                    p.setAnim(0)
                }
            }
            // `e()` cases 237/240 (L589, proven — structured g.java:3401+):
            // push-align — `r()` gate: bound ax66-S12 carrier → ride mount
            // (`Z[0]>0` → i(228) else i(358)); bound ax51 → edge-align step
            // ±20 then `i(0)`; `ag=ah=0` + non-crate link `ak=a.ak` snap.
            237, 240 -> {
                if (p.animFinished()) {
                    val a = p.ga
                    if (a == null || a.ax != 66 || a.S != 12) {
                        if (a != null && a.ax == 51) {
                            if (p.av) {
                                if (p.W[2] > a.W[2]) p.ak -= 20
                            } else if (p.W[0] < a.W[0]) {
                                p.ak += 20
                            }
                        }
                        p.setAnim(0)
                    } else if (a.Z[0] > 0) {
                        p.setAnim(228)
                    } else {
                        p.setAnim(358)
                    }
                    p.ag = 0; p.ah = 0
                    if (a != null && a.ax != 51) p.ak = a.ak
                }
            }
            // `e()` cases 242/243 (L1721, proven — structured g.java:3428+):
            // bind-release anims — `aj=1536` gravity; S242 apex (`ah>=0`) →
            // i(243); S243 `r()` → `a(0)`; `av()` edge guard then
            // `y() → a(true); ai=ag=0` settle.
            242, 243 -> {
                p.aj = 1536
                if (p.S == 242 && p.ah >= 0) p.setAnim(243)
                if (p.S == 243 && p.animFinished()) p.setAnim(0)
                p.airWallResolve(world)                                  // av()
                if (p.climbCheck()) {                                    // y()
                    p.collideSides(world, true)                          // a(true)
                    p.ai = 0; p.ag = 0
                }
            }
            else -> {
                // default arm (g.java:1145-1147, proven) — the ~150-state
                // fallthrough family (attack anims, hit-reacts, decor
                // states): when the anim ends and the latch wasn't
                // claimed and `l()` consumed no input → `a(0)` fling.
                // The doubled `!j` is verbatim — `l()` runs between the
                // two reads.
                if (p.animFinished() && !Entity.grabLatch &&
                    !l(p, pad) && !Entity.grabLatch) {
                    p.flingAirborne(0, world)
                }
            }
        }
        // L1926-L1946 shared tail (g.java:3460-3473, proven) — the
        // universal post-arm block every `goto L1926` arm flows into.
        // `ab` releases when the linked partner reaches S14 (skipped on
        // S9's own arm); `aO==6` (head cell = drop-through type) fires
        // op18 `g.a(); i(43)` unless standing in/on a type-2 cell.
        if (p.S != 9 && p.ab?.S == 14) p.ab = null              // L1926-L1933
        if (p.aR != 2 && p.aO != 2 &&
            p.e(world, p.ak / 20, p.al / 20) != 2) {           // L() i.java:7191
            if (p.aO == 6) p.applyHit(18, 0, p, world)         // a(18,0,0,this)
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
            } else if (!p.indicatorIsHand(world)) {
                // L37b6-L37c1 (fallback g.java:7882-7898, proven): mounted
                // + `!T()` → `U()` (a no-op when `T()==0` — verbatim) +
                // `a(8, ak, al-85)` clip-9 ax14 marker + `ae` pinned to
                // (ak, al-85) — the mount-confirm indicator flash.
                p.dropIndicator(world)
                p.spawnMarker(world, 8, p.ak, p.al - 85)
                p.ae?.let { it.ak = p.ak; it.al = p.al - 85 }
            }
            world.setMounted()
            p.gcm = true                            // L37eb — `g.cm = 1`
        } else if (p.gcm) {
            // L37f2 (proven): quiet tick after a mount event — clear the
            // g.cm latch and refresh the indicator: `k.k() ? G() : U()`.
            p.gcm = false
            if (world.mounted) p.releaseAe() else p.dropIndicator(world)
        }
        // L2051-L2057 aA fixups; the L380d `o()?ao()` weapon-cycle gate
        // already runs in postTail (:1021) — don't double-call it here
        // (`k.at` latches, but the port keeps one call site).
        if (p.aA == 0) p.aA = 1
        if (p.aA and 4 != 0) p.aA = p.aA and -5
    }

    // -- grounded family tail (L682) ----------------------------------------
    private fun groundedTail(p: Entity, pad: Pad) {
        p.z = true    // g.z — the locomotion arms set it (L890/1834/4976
                      // equivalents all live inside this tail)
        if (p.aO <= 12 || p.aR <= 12) {
            if (p.S == 79) {
                p.ag = 0; p.ah = 0
            }
            if (p.S == 11) p.ag = (p.ag shl 1) / 3
            // L691-692 (proven): down-held at a ledge → a(257,8) vault-drop;
            // L2886 (proven): down-edge into a wall → am() climb-up S63
            if (pad.u(Pad.M_DOWN) && wallClimb(p)) return
            if (ledgeDrop257(p, pad)) return
            // The I==1 sword arm of `ap()` moved to postTail with the rest
            // of the equip/context dispatcher (L2057 arm, g.java:3817).
            if (p.hitWall()) { p.ag = 1; p.collideSides(world, true); p.ag = 0 }
            if (!l(p, pad)) {
                p.cq = false; p.z = false      // `cq=0; z=0` airborne arm
                p.enterFall(0, world)
            }
            // g.java:824-844 (proven-DEAD, omitted): the case-0 arm
            // checks `cp && ct` (edge ledge-grab ak()||al()), `cu`
            // (down-edge pop + drop held), `cv` (dir press → aF=1), and
            // `cw` (aO==5 → i(280) one-way hang) — but e()'s head clears
            // all five flags every tick (g.java:617-623) and case 0 sets
            // none of them, so all four arms read false and can never
            // fire in the original. `al()` and `i.H()` are ported on
            // Entity for their live call sites.
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
    fun l(p: Entity, pad: Pad): Boolean {
        p.aF = 0; p.cp = true; p.cq = true; p.zz = true
        if (p.S == 79) {
            if (pad.u(Pad.M_LEFT)) {
                if (!p.av) p.av = true
                else {
                    p.setAnim(if (bn) 199 else 32)
                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else -2560
                    world.scrollWallClamp(p)   // g.java:5172 — i.f(this)
                }
            } else if (pad.u(Pad.M_RIGHT)) {
                if (p.av) p.av = false
                else {
                    p.setAnim(if (bn) 199 else 32)
                    p.ag = if (p.hitWall()) 0 else if (p.S == 199) 0 else 2560
                    world.scrollWallClamp(p)   // g.java:5182 — i.f(this)
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
        if (!p.aZ && p.standingOn == null) { p.cq = false; p.z = false; p.zz = false; return false }
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
        world.scrollWallClamp(p)               // g.java:5319 — i.f(this) tail
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
            p.cq = false; p.z = false; p.zz = false
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
                world.scrollWallClamp(p)       // g.java:3153 — i.f(this)
            } else if (pad.u(Pad.M_RIGHT)) {
                if (p.av) p.av = false else p.ag = 2560
                world.scrollWallClamp(p)       // g.java:3160 — i.f(this)
            } else if (!pad.u(Pad.M_LEFT or Pad.M_RIGHT)) {
                p.ag = 0; p.setAnim(79)
            }
        } else {
            p.cq = false; p.z = false
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
        world.scrollWallClamp(p)               // g.java:1740 — i.f(this) tail
    }

    // -- air family {20,22,23,25,215} (L889 block, proven core) ---------------
    private fun airFamily(p: Entity, pad: Pad) {
        p.cp = true; p.ct = true; p.cw = true
        if (p.gI == 4) p.z = true              // L1ce8 — I==4 arms z
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
            // g.java:1600 (proven): i.f(this) caps the !y() free-air path.
            // The cv/aF-bound climb sites (:1614/:1645) sit in unported
            // branches — those climb states have their own arms.
            world.scrollWallClamp(p)
        }
        if (p.animFinished() && p.S != 215 && p.S != 22) {
            p.enterFall(0, world)
            p.collideSides(world, true)
        }
        if (p.S == 22 && p.ah >= 0 && p.ah + p.aj < 0) p.setAnim(23)
    }

    // -- shared fall arm `case 16/35/43/150/252` (g.java:1400-1475) ----------
    private fun fallArm(p: Entity, pad: Pad) {
        world.scrollWallClamp(p)               // g.java:1406 — i.f(this) head
        p.cv = true; p.cp = true; p.ct = true; p.cw = true
        if (p.S == 43 && p.hitWall()) { p.ai = 0; p.ag = 0 }
        if (p.gI == 4) p.z = true              // g.java:1414 — I==4 arms z
        if (p.S == 43 && p.animFinished()) p.P = p.P or 64
        // L1421 — marker-3 feet cell: crate-top dismount probe
        // (g.c = world.gc contact link; i.bq = Entity.entBq)
        if (p.aQ == 3) {
            val c = world.gc
            if ((c != null && c.ax == 51 && p.al > c.W[3]) ||
                (Entity.entBq > 0 && p.al > Entity.entBq && c == null) ||
                (c == null && Entity.entBq == 0)) {
                p.setAnim(147)                                  // i(147)
                Entity.entBq = 0                                // i.bq = 0
            }
        } else if (p.aR >= 12 && p.aQ >= 12 && p.aQ != 23 ||
                   p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5 ||
                   p.aR == 4 || p.aS == 4) {
            p.land(world, p.aR == 4 || p.aS == 4)
        } else {
            p.aj = 1536
            // g.java:1428-1445 (proven) — L1425 bound catch: falling inside
            // an S36 context zone (gk!=-1 while `al<go || go==0`) re-binds
            // instead of falling. gk==0 → a(29,32) ride state (snap to gn);
            // gk==1 → snap onto the owner entity gd → i(315) carrier pose.
            if (p.gk != -1 && ((p.go != 0 && p.al < p.go) || p.go == 0)) {
                if (p.gk == 0) {
                    p.enterStateMasked(29, 32, world)           // a(29,32)
                } else if (p.gk == 1) {
                    p.gd?.let { d ->                            // g.d
                        p.ak = d.W[0]
                        p.refreshBoxes()                        // t()
                        p.setAnim(315)
                        p.av = d.av
                        p.ak = if (d.av) d.W[2] else d.W[0]
                    }
                }
                p.ag = 0
                if (p.gn != 0 && p.gk == 0) p.ak = p.gn
                p.ah = 1536; p.aj = 0
            } else if (p.hitWall() && (
                        pad.u(Pad.M_TAP_L) || pad.v(Pad.M_TAP_L) ||
                        pad.u(Pad.M_TAP_R) || pad.v(Pad.M_TAP_R) ||
                        pad.u(Pad.M_UP) || pad.v(Pad.M_UP))) {
                // g.java:1447-1455 (proven) — L1430 wall-grip tap block:
                // bound states (cv&&aF!=0&&!ba) exit the case; else the
                // tap drags fall drift to ±512.
                if (p.aF != 0 && !p.ba) return
                if (p.ag != 0) p.ag = if (p.ag > 0) 512 else -512
            }
            // g.java:1465 (proven): states outside {43,150,35,29,252}
            // sharing this arm resolve to i(35) on anim end — the S16
            // door-glide → loop-fall S35 route.
            if (p.S != 43 && p.S != 150 && p.S != 35 && p.S != 29 &&
                p.S != 252 && p.animFinished()) {
                p.setAnim(35)
            }
        }
    }

    // -- post-switch tail (g.e() L2083 block, proven) -------------------------
    private fun postTail(p: Entity, pad: Pad) {
        // g.java:788 (proven): the shared jump tail is `cq && !E` — the
        // ax10-S55 suppress zone holds `g.E` so wall-run-family arms
        // (S102/332/317) keep their own `i(17)`/`i(50)` transitions.
        if (p.cq && !Entity.gE && pad.v(Pad.M_ACTION_FAMILY)) {
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
                    // g.java:805 (proven): landing on an ax51 crate
                    // records the crate-top level `i.bq = al + 20`.
                    if (p.standingOn?.ax == 51) Entity.entBq = p.al + 20
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
        // -- L2048-L2064 equip/context arms (g.java:~3711, proven) ---------
        // L2048: `o()` gate → `ao()` weapon cycle
        if (p.groundOrVehicle()) p.cycleEquip(world, pad)
        // L2051-L2054 (proven): `aA` counter bookkeeping — `aA==0 → aA=1`,
        // then `aA&4 → aA&=-5` clears bit2 every tick.
        if (p.aA == 0) p.aA = 1
        if (p.aA and 4 != 0) p.aA = p.aA and 4.inv()
        // L2057: `z && i.bn==false && E==false → ap()` context dispatch
        if (p.z && !bn && !world.eFlag) p.contextDispatch(world, pad)
    }

    /**
     * L1341 combo arm (subset of the g.java S67/68/69 + S112..115 arms):
     * `aj()` runs the cj/ck matchers — while S==row.anim, a 65568 tap with
     * `T >= row.minFrame` opens the window (`cl`) and queues `R = next anim`;
     * the consumed `R` fires `i(R)` at the window/end boundary; otherwise the
     * arm settles to `i(0)` (the `l()` call). The `i.aN` weakened-target
     * assassination shortcut (R=183/184 via rand) is omitted — needs lock-on.
     */
    /**
     * `ay()` (g.java:5363-5421, proven) — the sword-combo advance step:
     * ledge-guard ahead of the player (never steps off the support
     * edge), ±1792 steps on the flagged anim ticks while the `i.V`
     * hit-pause is idle, the `i.aN` approach clamp (stop at the target's
     * hitbox edge), and the `i.f` scroll-wall tail. Decompiler gap: when
     * `i.aN == null` the ledge expression is unassigned — the ±1-column
     * arm is the minimal consistent read (aN!=null widens it to ±2).
     */
    private fun attackStep(p: Entity) {
        val a = p.standingOn                                   // g.a
        var z2 = true
        if (a == null || (!p.av || a.W[0] >= p.W[0]) && (p.av || a.W[2] <= p.W[2])) {
            val iE = p.e(world, p.W[2] / 20 + 1, p.W[3] / 20)
            val iE2 = p.e(world, p.W[0] / 20 - 1, p.W[3] / 20)
            // conjunct polarity (proven, g.java:5374): facing right
            // (av=false) probes the right columns, left the left —
            // the aN!=null arm widens to ±2 cells on the same side.
            z2 = (p.av || iE == 20 || iE == 5) &&
                 (!p.av || iE2 == 20 || iE2 == 5)
            val t = world.lockTarget                           // i.aN
            if (t != null) {
                val iE3 = p.e(world, p.W[2] / 20 + 2, p.W[3] / 20)
                val iE4 = p.e(world, p.W[0] / 20 - 2, p.W[3] / 20)
                z2 = (p.av || iE3 == 20 || iE3 == 5) &&
                     (!p.av || iE4 == 20 || iE4 == 5) && z2
            }
        }
        if (!z2) { p.ai = 0; p.ag = 0; return }
        when (p.S) {
            67, 69 -> p.ag = if (p.T == 1 && p.V <= 0) (if (p.av) -1792 else 1792) else 0
            68 -> p.ag = if ((p.T == 0 || p.T == 1) && p.V <= 0) (if (p.av) -1792 else 1792) else 0
        }
        val t = world.lockTarget
        if (t != null) {
            t.refreshBoxes()                                   // aN.t()
            if (p.ak >= t.ak || p.ag < 0) {
                if (p.ak > t.ak && p.ag <= 0 &&
                    (p.ak + (p.ag shr 8) + (p.W[0] - p.ak)) - 2 <= t.W[2]) {
                    p.ai = 0; p.ag = 0
                }
            } else if (p.ak + (p.ag shr 8) + (p.W[2] - p.ak) + 2 >= t.W[0]) {
                p.ai = 0; p.ag = 0
            }
        }
        world.scrollWallClamp(p)                               // i.f(this)
    }

    /**
     * `g.aA()` (g.java:5545-5565, proven): wall-kick — on the up-edge
     * (`u(16388)`) or a direction-press toward the facing side
     * (`u(8264)` av / `u(4114)` !av) → `i(92)` + spawn the ax8/clip5/
     * S17/az201 marker bound to `i.aK` (`av` copied, `P=512`, `N/O` the
     * 8.8 pos, vel zeroed, `t()`), then `k.b()` inserts it.
     */
    private fun wallJumpKick(p: Entity, w: LevelCellSource, pad: Pad) {
        val dir = (p.av && pad.u(8264)) || (!p.av && pad.u(4114))
        if (!pad.u(16388) && !dir) return
        p.setAnim(92)
        val aK = p.spawnChildFx(w, 8, 5, 17, 201)              // a(8,5,17,201)
        aK.av = p.av
        aK.P = 512
        aK.N = p.ak shl 8; aK.O = p.al shl 8
        aK.ak = p.ak; aK.al = p.al
        aK.aj = 0; aK.ai = 0; aK.ah = 0; aK.ag = 0
        aK.refreshBoxes()
        w.queueInsert(aK)
    }

    private fun comboArm(p: Entity, pad: Pad) {
        // L2469-2475 (proven): attack step → wall-stop → footing-loss
        // fall through the same case body, then the combo logic.
        attackStep(p)
        if (p.ag != 0 && p.forwardWall()) p.ag = 0
        if (!p.aZ && p.standingOn == null) { p.enterFall(); return }
        // L2480-2486 (proven): tap 65568 in S67/68 with a weakened lock
        // (ax11, Z0==2, aB<=bw, a==null) → R = rand%2 ? 184 : 183
        val t = world.lockTarget
        if (pad.v(Pad.M_CONTEXT) && (p.S == 67 || p.S == 68) &&
            p.standingOn == null &&
            t != null && t.ax == 11 && t.Z[0] == 2 && t.aB <= BW_MOCK &&
            t.aB > 0) {
            p.cl = false
            if (p.R == -1) p.R = if ((rng?.nextInt() ?: 0) % 2 != 0) 184 else 183
        } else if (pad.v(Pad.M_CONTEXT)) {
            comboMatch(p, CJ, true, pad); comboMatch(p, CK, false, pad)
            // g.java:2495: aj() then k.E?.K() — companion release on the
            // combo chain (same pose map as the attack entry).
            world.kE?.heldRelease(p)
        }
        // L2510-2525 (proven): cl||r() tail — crate interrupt, kill-cam
        // freeze, dead-target guard, then R→anim or l().
        if (p.cl || p.animFinished()) {
            if (p.R == 112 && p.standingOn?.ax == 51 && p.animFinished()) p.R = -1
            p.cl = false
            if ((t != null && t.aB > 0 && p.R == 183) || p.R == 184 || p.R == 205) {
                p.lockInput(world); p.ag = 0; p.ah = 0         // k.o()
            } else if (t != null && t.aB <= 0) {
                p.R = -1
            }
            if (p.R != -1) { p.setAnim(p.R); p.R = -1 }
            else p.setAnim(0)                                  // l()
            if (p.T == 2) world.sfx(10)                        // k.A(10)
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

    // === g.n() (g.java:5603-6113, proven) — the flying player tick =======
    // Runs instead of the grounded dispatch when `bh[aj]==3` (`I()` case 25
    // → `k.aS.n()`, i.java:5215 — the ax25 record IS the player slot,
    // k.java:4647). Flight S-space 0-33: S0 glide, S4/5 climb/dive, S17/18
    // burst-entry, S20-23 wisp-burst chain, S24 stall, S2/24 stall-fail,
    // S26/28/29 intro anims, S30-33 banks, S1/9/12/27 hit-recoveries, S3.
    //
    // JADX's switch groups are lossy (its own header warns "Can't fix
    // incorrect switch cases order") — the glide arm is transcribed for
    // S ∈ {0,4,5,17,18} so the inner `S==18`/`S==17`/`z4` checks are all
    // reachable (`inferred` group cover; the S3/S20-24/26-33 cases are
    // single-valued as printed).
    private fun flightTick(p: Entity, pad: Pad) {
        var z2 = true
        var z3 = true
        world.kAI++
        // B() (k.java:1619, proven): mission BGM — `aJ==1 → z(9)` else
        // `z(ee[aj])`; per-tick in n(), audioPlay dedups the live track.
        if (world.kAJ == 1) world.sfx(9) else { val t = world.kEE[world.kAj]; if (t != -1) world.sfx(t) }
        if (world.iBe) {
            world.kX = 0
            if (p.animFinished() || !flightAliveV(p)) { world.stateL(12); return }
            return
        }
        // `i.bh--` (n():5615) runs in tick() for both modes — not repeated.
        if (world.kAH < 0) world.kAG--
        if (world.kAG <= 0) { world.kAG = 6; world.kAE-- }
        if (world.kAE < 0) world.kAE = 0
        else if (world.kAE <= 25 && world.kAF == 0 && world.kAH < 0 && (world.iAJ == 0 || world.kW != 0)) {
            if (world.kW != 0) { world.iAJ = world.kW; world.kW = 0 } else world.iAJ = world.kX
            world.kX = world.iAJ shr 1
        }
        if (world.kAE <= 0 && world.kAH < 0) {
            if (p.S != 24) {
                world.iBB = true; world.iBC = false; world.iBD = false
                world.iBF = 90; world.iBE = 999; world.iBG = -1
                p.setAnim(24)
            }
            p.x1 = 0
        } else if (p.x1 <= 0) p.setAnim(2)
        if (world.iBB) { p.aq = -1; p.ar = -1 }

        when (p.S) {
            0, 4, 5, 17, 18 -> {
                if (p.S == 0 && p.animFinished()) p.T = (p.clip?.frameCount(0) ?: 0) - 2
                if (!world.iBi || Entity.gE) {
                    if (world.kQ >= 230) {
                        z3 = false
                        p.ah = if (world.iAH) world.kY * world.iAI else world.kY
                    }
                } else {
                    if (world.kAw == 20) world.kAw = 0            // aC()
                    if (world.iAH && world.kQ >= 230) { p.ah = world.kY shl 1; z3 = false }
                    if (!world.iBB && pad.v(1)) world.sfx(28)     // k.A(28) = z(28)
                    if (p.S == 18) { p.av = p.ak > world.kO + 200; p.setAnim(20); world.iBk = true }
                    else if (p.S == 17) p.setAnim(4)
                    val z4 = p.S != 3 && p.S != 0 && p.S != 18 && p.S != 17 && p.S != 20
                    if (!world.iBk && p.Q != 18 && world.kAI >= 10 && z4) { world.kAI = 0; flap(p, false) }
                    if (pad.u(4112)) {
                        if (p.ag > -2048) p.ag -= 768
                        if (p.ag < -2048) p.ag = -2048
                        z2 = false
                        if (z4) p.setAnim(if (world.kBD >= 15) 30 else 33)
                        p.av = false
                    }
                    if (pad.u(8256)) {
                        if (p.ag < 2048) p.ag += 768
                        if (p.ag > 2048) p.ag = 2048
                        z2 = false
                        if (z4) p.setAnim(if (world.kBD >= 15) 31 else 32)
                        p.av = false                            // verbatim quirk — right-bank also faces left
                    }
                    if (pad.u(16388) && world.kQ > 117) {
                        if (p.ah > -2048 + world.kY) p.ah -= 768
                        if (p.ah < -2048 + world.kY) p.ah = -2048 + world.kY
                        z3 = false; p.av = false
                        if (z4) p.setAnim(4)
                    }
                    if (pad.u(33024) && world.kQ < 230) {
                        if (p.ah < 2048 + world.kY) p.ah += 768
                        if (p.ah > 2048 + world.kY) p.ah = 2048 + world.kY
                        z3 = false; p.av = false
                        if (z4) p.setAnim(5)
                    }
                    if (world.kBB == 0 && world.kBC == 0 && p.animFinished() && z4) { p.av = false; p.setAnim(4) }
                    if (p.aq != -1 && p.ar != -1) {
                        p.ah = 0; p.ag = 0; z3 = false; z2 = false
                        if (p.aq < p.ak && !p.bb) {
                            p.ak -= 10
                            if (z4) { val i2 = world.kBD; world.kBD = i2 + 1; p.setAnim(if (i2 >= 15) 30 else 33) }
                        } else if (p.aq > p.ak && !p.bc) {
                            p.ak += 10
                            if (z4) { val i3 = world.kBD; world.kBD = i3 + 1; p.setAnim(if (i3 >= 15) 31 else 32) }
                        }
                        p.ar += world.kX
                        p.al += world.kX
                        if (p.ar < p.al) { p.al -= 10; if (z4) p.setAnim(4) }
                        else if (p.ar > p.al) { p.al += 10; if (z4) p.setAnim(5) }
                        if ((p.aq < p.ak && p.aT >= 10) || (p.aq > p.ak && p.aU >= 10)) p.aq = p.ak
                        if ((p.ar < p.al && world.kQ <= 117) || (p.ar > p.al && world.kQ >= 230)) p.ar = p.al
                        if (Math.abs(p.aq - p.ak) <= 10) p.ak = p.aq
                        if (Math.abs(p.ar - p.al) <= 10) p.al = p.ar
                        if (p.ak == p.aq && p.al == p.ar) { p.aq = -1; p.ar = -1 }
                    }
                }
            }
            1, 9, 12, 27 -> {
                world.kAw = 20
                if (p.animFinished()) {
                    if (world.iBB) { world.iBB = false; world.iBG = -1; p.az = 202 }
                    if (world.kAw == 20) world.kAw = 0          // aC()
                    p.setAnim(4)
                }
            }
            2, 24 -> {
                p.ag = p.ag shr 1; p.ah = p.ah shr 1
                z2 = false; z3 = false
                if (p.animFinished() || !flightAliveV(p)) world.stateL(12)
            }
            3 -> {
                if (p.animFinished()) p.setAnim(4)
                else {
                    p.av = false
                    if (p.S == 0) p.T = (p.clip?.frameCount(0) ?: 0) - 2   // proven-dead inside case 3
                    // `if (!i.bi) { if (k.Q >= 230) {} }` — empty arm, proven-dead
                }
            }
            20 -> {
                if (world.iBk) {
                    if (p.T == 5) repeat(5) { flap(p, true) }
                    world.iBB = true; world.iBE = 999
                    if (p.T <= 10) { world.iBC = true; world.iBD = true }
                    else { world.iBC = false; world.iBD = false; world.iBG = 100 }
                }
                if (p.animFinished()) { world.iBB = false; world.iBG = -1; world.iBk = false; p.setAnim(4) }
            }
            21 -> {
                world.kAw = 20
                if (p.animFinished()) { world.iBB = true; world.iBC = true; world.iBD = true; world.iBE = 999; p.setAnim(22) }
                bankSteer(p, pad)
            }
            22 -> {
                world.kAw = 20
                if (p.animFinished()) { world.iBB = true; world.iBC = false; world.iBD = false; world.iBE = 999; world.iBG = 100; p.setAnim(23) }
                bankSteer(p, pad)
            }
            23 -> {
                world.kAw = 20
                if (p.animFinished()) { world.iBB = false; world.iBG = -1; p.setAnim(4) }
            }
            26 -> {
                world.kAw = 20
                if (p.animFinished()) p.T = (p.clip?.frameCount(p.S) ?: 0) - 2
            }
            28 -> { world.kAw = 20; if (p.animFinished()) p.setAnim(29) }
            29 -> { world.kAw = 20; if (p.animFinished()) p.setAnim(26) }
            30, 31, 32, 33 -> p.av = false                      // `S==0`/`!bi` arms are empty, proven-dead
            else -> {}
        }
        if (z2) {
            if (p.ag > 768) p.ag -= 768
            else if (p.ag < -768) p.ag += 768
            else p.ag = 0
        }
        if (z3) {
            if (p.ah > 768 + world.kY) p.ah -= 768
            else if (p.ah < -768 + world.kY) p.ah += 768
            else p.ah = world.kY
        }
        if (p.ad != null) {
            p.ad!!.av = p.av; p.ad!!.P = p.P
            p.ad!!.ak = p.ak + 20
            p.ad!!.S = p.S; p.ad!!.T = p.T; p.ad!!.al = p.al
            p.ad!!.ag = p.ag; p.ad!!.ah = p.ah
            p.ad!!.ai = p.ai; p.ad!!.aj = p.aj
        }
    }

    /** `n()`'s shared steering block (g.java:5900-5916, proven) — the
     *  S21/S22 cases carry the same `u(4112)`/`u(8256)` ±768 arms. */
    private fun bankSteer(p: Entity, pad: Pad) {
        if (pad.u(4112)) {
            if (p.ag > -2048) p.ag -= 768
            if (p.ag < -2048) p.ag = -2048
            p.av = false
        }
        if (pad.u(8256)) {
            if (p.ag < 2048) p.ag += 768
            if (p.ag > 2048) p.ag = 2048
            p.av = false
        }
    }

    /** `g.e(boolean)` (g.java:6044-6079, proven) — the flap puff: spawn
     *  `a(24,40,6|7,az-1)` into the `k.b` pool, aim at scroll position
     *  (burst) or straight up-100 (flap). */
    private fun flap(p: Entity, burst: Boolean) {
        val wisp = p.spawnChildFx(world, 24, 40, if (burst) 7 else 6, p.az - 1)
        wisp.av = false
        wisp.ak = p.ak; wisp.al = p.W[1] - 3
        wisp.am = wisp.ak; wisp.ao = wisp.ak; wisp.an = wisp.al
        if (burst) {
            wisp.ao = world.kO + (rng?.nextRange(70, 330) ?: 200)
            wisp.ap = world.kP + (rng?.nextRange(110, 130) ?: 120)
            wisp.ag = ((wisp.ao - wisp.am) shl 8) / 10
            wisp.ah = ((wisp.ap - wisp.an) shl 8) / 10 + world.kY
            wisp.aC = 10
        } else {
            wisp.ap = wisp.al - 100; wisp.ag = 0; wisp.ah = -3840 + world.kX
        }
        wisp.refreshBoxes()                                        // iVar3.t()
        wisp.P = wisp.P or 16
        wisp.af = p
        wisp.bR = false
        world.iAK = wisp
        world.queueInsert(wisp)                                // k.b(aK)
    }

    /** `i.v()` ax25 tail (i.java:597-640, proven subset) — the flying
     *  player stays "alive" while its `Y` box overlaps the camera rect
     *  `k.ac` (`ax!=14 → a(k.ac, this.Y)` on bh3). The special-ax arms
     *  and the `u()`/`au>i` screen-score guard are NPC-side, unported. */
    private fun flightAliveV(p: Entity): Boolean {
        val ac = world.kAc ?: return true
        return p.Y[0] <= ac[2] && p.Y[2] >= ac[0] && p.Y[1] <= ac[3] && p.Y[3] >= ac[1]
    }

    companion object {
        /** `g.c(int)` (g.java:316, proven) — the grounded-state set the
         *  `i.bq` clear arm tests: {0,1,7,11,12,26,79}. */
        private val GROUNDED_C = setOf(0, 1, 7, 11, 12, 26, 79)
        /** `g.b()` no-arg attack table (proven, L9→L10 in g.java). */
        private val ATTACK = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115, 183, 184, 216, 217, 286, 287)
        fun isAttackState(s: Int): Boolean = s in ATTACK
        /** `g.b(int)` (g.java:288, proven) — the aerial/action anim set
         *  the `i.f` top-bound arm tests (`k.aS.a(0)` knock-off). */
        private val AIR_ACTION = intArrayOf(18, 19, 20, 22, 23, 24, 25, 35, 36, 43,
            150, 157, 165, 233, 242, 243, 263, 264, 265, 266)
        fun isAirAction(s: Int): Boolean = s in AIR_ACTION

        /** Combo chain tables `cj`/`ck` from g clinit (proven):
         *  4 rows × {anim, then next-anim at +1, min-frame at +n, key at +2n}. */
        private val CJ = intArrayOf(67, 68, 69, 112, 6, 5, 100, 100, 65568, 65568, 65568, 65568)
        private val CK = intArrayOf(112, 113, 114, 115, 9, 5, 5, 100, 65568, 65568, 65568, 65568)
        /** `bw[k.au]` normal-hit reference for the weaken check (au=0). */
        private const val BW_MOCK = 80
    }
}
