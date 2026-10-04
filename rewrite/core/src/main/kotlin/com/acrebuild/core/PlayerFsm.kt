package com.acrebuild.core

/**
 * Slice-2 player FSM: the grounded + air locomotion arms of `g.e()`,
 * transcribed from the simple decompile (goto form) of
 * `reconstructed-project/src/simple/g.java`.
 *
 * Ported verbatim (confidence `proven` for structure, literals `proven`):
 *
 * - 6092 grounded arm (S∈{1,7,11,26,79}; S0 and the S12 fall-through
 *   enter at 6089 `i.O()`): embedded (`aO>12 && aR>12`) → `i(79)` and the
 *   arm ends; else S79 zeroes velocity and checks the DOWN vault-drop,
 *   S11 decays `ag=(ag<<1)/3`, `u(33024) && am()` ends it, then
 *   `y()` → `a(true)` and `l()` (airborne → `a(0)`). See [groundedTail].
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
 *   S21 on anim end / S233 at once → `i(22)` with `ah=-5120` (-2560 on an
 *   ax51 crate) and `ag=±2048` (±1024 off an ax43 running its claim),
 *   S22 apex (`ah>=0 && ah+aj<0`) → `i(23)`, landing probes → `d(false)`,
 *   anim end → `a(0)` re-enter fall.
 * - S43 fall arm (L1045): gravity + `d(aR==4||aS==4)` landing variant.
 * - S5 land arm (4689): `i.O()`, jump press → `i(21)` (and on into the
 *   checks below), direction held → `l()`, anim end → `i(aO>12 ? 79 : 0)`.
 * - S6 (L139): on `r()` clear velocities + `P|=64`; `!u(16388)` → `i(0)`.
 * - S10 dash (L1315): `ag /= 2` per tick (plus the `ab` mirror — the held
 *   prop tracks the player's box at :943).
 * - S32 arm (structured g.java `case 32`): `r()` → `ag=0; i(Q==79?79:0)`.
 * - S199 arm (case 199): grounded → wall stop, L/R hold → `ag=±2560` or
 *   flip `av`, release → `ag=0; i(79)`.
 * - S78/S80 (L771): `r()` → `i(79)` or `i(1)`.
 *
 * Coverage (as of slice 202): the full `e()` dispatch — every case arm
 * is ported, plus the latch-flag model verbatim: the head clears
 * `cp/cq/ct/cu/cv/cw/z` per tick (g.java:1285-1301), `l()`'s head re-arms
 * `cp/cq/z` (g.java:11608-11617), and the per-arm sets (S33/38/60/
 * 263/264, air & fall families) match the original labels. `am()`,
 * `a(257,8)`, `k.f(this)`, and the interact/QTE/attack arms landed in
 * earlier slices (11/16/132/135/151/153). Remaining gaps are individual
 * helper tails flagged `inferred`/`proven-dead` inline.
 */
class PlayerFsm(private val world: LevelCellSource, private val rng: DeterministicRandom? = null) {

    var bn = false   // i.bn — blend/unlock flag (false in slice 2)

    /** One player tick: `g.e()` (or `g.n()` on bh3). Tests drive this;
     *  the world splits it ([eHeadReturns] + [tickBody]) so that the
     *  integrate runs after `k.l()`'s head returns, as the original's
     *  integrate → `e()` order has it. */
    fun tick(p: Entity, pad: Pad) {
        if (eHeadReturns(p)) return
        tickBody(p, pad)
    }

    /**
     * `g.e()` offsets 0-163 (g.javap.txt:2219-2290, proven) — the steps
     * before the `k.E.J()` follow; returns `true` when `e()` returns
     * there. `g.n()` (bh3, 17392) has none of them.
     *  - 0-19 (slice 369 F13a): `k.C != null && (k.aS.P & 512) == 0 →
     *    return`. `i.I()`'s L108 gate already skips the whole tick while
     *    `k.C.ab()` holds; this one stops `e()` when a claimer is bound
     *    but not `ab()` (paused `cd[0]`, a `cK < 0` script), after the
     *    integrate has run.
     *  - 20-153: the `g.v` free-move cheat (k.java:5252 toggles it) —
     *    not ported, no cheat input in the port.
     *  - 154 (F13b): `k.l()` = the static `k.M` 60×60 box in front of the
     *    player, from `ak/al/av` as the integrate left them. NPCs read
     *    it on the next frame (they tick before the player).
     *  - 157-163 (F13c): `g.r → return` — the aP S7 grab-QTE frames.
     */
    fun eHeadReturns(p: Entity): Boolean {
        if (world.bh3) return false
        if (world.kC != null && (p.P and 512) == 0) return true     // 0-19
        val m = world.kM                                              // 154 k.l()
        m[0] = if (p.av) p.ak - 60 else p.ak
        m[1] = p.al - 60
        m[2] = m[0] + 60
        m[3] = p.al
        return world.gR                                               // 157-163
    }

    /** `g.e()` from offset 164 on (or `g.n()`). */
    fun tickBody(p: Entity, pad: Pad) {
        // e() head (g.java:1285-1301, proven): clears ALL latch flags
        // EVERY tick — `cp=cq=cr=cs=z=ct=cu=cv=cw=0`. State arms then
        // re-arm only theirs: `l()`'s head sets `cp=cq=z=1`
        // (g.java:11608-11617), the air/fall families set `cv/cp/ct/cw`
        // (+`I==4→z`), S33 `cp`+`ct`-on-rise, S38 `cq`, S60 `cu`,
        // S263/264 `cw`; `cr`/`cs` are dead in the original. (The port
        // previously true-set them at the head — that let postTail's
        // cq-jump gate fire from every state; the original only lets
        // l()-family + S38-boost-exit jump.)
        p.cp = false; p.cq = false; p.ct = false; p.cu = false
        p.cv = false; p.cw = false; p.z = false
        // `g.t--` runs inside `i.F()`'s La84 arm (i.java:13183, proven)
        // — gated on `j.c==8 && !claimed && !g.s && !(i.bB&&i.bF!=-1)`;
        // ported in `Entity.drawStyleF`, driven by `drawStylePass()`.
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
        // g.javap.txt e() 449-486 (proven): `k.aA > 0 → aA>1 ? aA|=1`;
        // otherwise a stance of 0/1 is raised to 2.
        if (world.kAA > 0) { if (p.aA > 1) p.aA = p.aA or 1 }
        else if (p.aA <= 1) p.aA = 2
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
        else {
            // g.javap.txt e() 533-595 (slice 369 F9, proven): the input
            // lock `k.am` (set by `k.o()`) is released at the head unless
            // the player sits in a finisher (S183/S184) or the grab-hold
            // counter (S311): `k.p(); i.O()`, and a held `i.aN` is
            // dropped with `aB = 0; i.d(aN)`.
            if (world.kAm && p.S != 183 && p.S != 184 && p.S != 311) {
                p.unlockInput(world)                           // k.p()
                p.timewarpOff(world)                           // i.O()
                world.lockTarget?.let {
                    it.aB = 0
                    it.releaseAnimReset()                      // i.d(aN)
                    world.lockTarget = null
                }
            }
            // 598-614: `i.aN != null && i.aN.P() → i.aN = null`.
            world.lockTarget?.let { if (it.deadRelease()) world.lockTarget = null }
            // g.javap.txt e() 617 (proven): `az()` — the interact scan is
            // the last head step before the `S` switch (after the k.am /
            // i.aN checks at 533-614), so every state runs it, the
            // returning arms too, and each arm reads this tick's g/ci/at.
            interactScan(p)
            // 621-631 (slice 369 F9): `S != 43 → g.i = true` — re-arms the
            // ax43 grapple bind that S295's mount drop clears.
            if (p.S != 43) world.iFlag = true
            // Slice 365: the post-tail runs only when the arm reached
            // L353d (`dispatch` returned true) — see `dispatch`.
            if (dispatch(p, pad)) postTail(p, pad)
        }
    }

    /**
     * The `g.e()` `S` switch (g.javap.txt e() 634 tableswitch, 0..377,
     * default 13598) plus the L353d head of its shared tail. Returns
     * `true` when the tick goes on into the post-tail (offset 14349) and
     * `false` when the original `return`s first (slice 365, proven).
     *
     * Exit map (g.javap.txt e(), proven): of the 107 arm entry offsets,
     * 41 always `return` (60 states), 58 always `goto 13629` = L353d (90
     * states), 8 do both (15 states) and the default (213 states, 13598)
     * falls into 13629. No arm jumps past L353d: every tail exit is
     * `goto 13629`. L353d itself returns at 13711 (the type-2 death,
     * slice 370); everything else falls into the post-tail at 14349. The
     * full table is in plans/261003-1900-slice365-ge-exits/plan.md.
     */
    private fun dispatch(p: Entity, pad: Pad): Boolean {
        when (p.S) {
            // grounded family — the 6092 arm. S0 enters one instruction
            // early, at 6089 `i.O()` (slice 369 F6, the S12 fall-through
            // target too); S1/7/11/26/79 enter at 6092.
            0 -> { p.timewarpOff(world); groundedTail(p, pad) }   // 6089 i.O()
            1, 7, 11, 26, 79 -> groundedTail(p, pad)
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
                // g.java L16c0 (proven): every non-transition path —
                // `ag==0`/`aO!=0`, wall strip `i10<19`/`i10>=24`, vault
                // tier i8∉{1,2,3}, `co<=2`, or `z()`/`ak()` false —
                // `goto L17c9` → `O()` + the L17cc grounded block
                // (… → `l()` input arms). So a run never dead-ends input:
                // `l()` sees dir/UP every tick — a pinned runner can still
                // turn, brake, or jump out. Only a fired transition
                // (i(107-109)/lip/S33) exits early via L353d; the A()
                // ladder snap → i(74) `return`s (g.javap.txt e() 5944,
                // slice 365) — no L353d tail, no post-tail.
                var transitioned = false
                if (p.ag != 0 && p.aO == 0) {
                    val i8 = if (p.av) p.aX else p.aY
                    val i9 = if (p.av) p.aT else p.aU
                    if (p.ladderCell(world)) {                 // A()
                        p.ai = 0; p.ag = 0; p.al = p.W[1]
                        p.ak += if (p.av) -20 else 20
                        p.setAnim(74)
                        return false                           // 5944
                    } else if (i9 >= 19 && i9 < 24) {
                        if (i8 == 1) { p.ag = 0; p.ah = 0; p.setAnim(107); transitioned = true }
                        else if (i8 == 2) { p.ag = 0; p.ah = 0; p.setAnim(108); transitioned = true }
                        else if (i8 == 3) { p.ag = 0; p.ah = 0; p.setAnim(109); transitioned = true }
                        else if (p.co > 2 && p.pushColumnBlocked(world)) {
                            if (p.ledgeLipGrab(world)) {       // ak()
                                p.aj = 0; p.ah = 0; p.ag = 0
                            } else {
                                p.setAnim(33); p.ag = 0; p.ah = -4096
                            }
                            transitioned = true
                        }
                    }
                }
                if (!transitioned) {
                    // `ag==0 || aO!=0 → L17c9`, plus every non-transition
                    // path above — `i.O()` timewarp disarm, then the
                    // shared L17cc grounded block (open head/feet → l();
                    // embedded → i(79)). Re-arms cp/cq/z on the
                    // stopped/blocked tick — e.g. pinned at a crate.
                    p.timewarpOff(world)
                    groundedTail(p, pad)
                }
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
            // Arms no latch flags.
            86 -> {
                if (p.animFinished()) p.setAnim(326)
            }
            // g.java L25eb (proven) — S89 grab-pinned: `h(1)` eats the
            // pending J&1 grab request into `I=1` + `k.at=1` (and releases
            // an ax16 request link); `ai=aj=ag=ah=0`; `r()` → `P|=64`
            // (sleep the body, keep the corpse drawn).
            89 -> {
                p.requestH(1, world)               // h(1) — side effects
                p.ai = 0; p.aj = 0; p.ag = 0; p.ah = 0
                if (p.animFinished()) p.P = p.P or 64
            }
            // g.java L2de0 (proven) — S110 pinned settle: `r()` → `P|=64`.
            110 -> {
                if (p.animFinished()) p.P = p.P or 64
            }
            // g.java L2df5 (proven) — S165 launch/leap: `aj=1536` capped
            // gravity; `r()` → `a(0)` (the masked fling — resolves to
            // S43 + `al+=10`).
            165 -> {
                p.aj = 1536
                if (p.animFinished()) p.flingAirborne(0, world)
            }
            // g.java L1ce4 (proven) — S18/S19/S36 air variants: `cv=1` then
            // the shared L1ce8 air-family tail (same body as airFamily).
            // The g.e() switch sends 18/19/23/36 to offset 7396 = L1ce4
            // (g.javap.txt:2537-2555, :5344-5346); S23 arms cv inside
            // airFamily. S18 — the S17 wall-kick flight — had no arm until
            // slice 349 and fell into the default fling.
            18, 19, 36 -> {
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
                return false
            }
            // g.java L169c (proven) — S74 leap-dash: `r() → ak±40; a(0)`;
            // early `return`.
            74 -> {
                if (p.animFinished()) {
                    p.ak += if (p.av) -40 else 40
                    p.flingAirborne(0, world)
                }
                return false
            }
            // g.java:1430-1433/:1674 → L2989 → L353d (proven) — S82-85/
            // S326 rope-hang family: L2989 is a BARE label straight into
            // the shared post-dispatch tail — no arm at all. The
            // `r() → P|=64` arm is case 110's L2de0 (g.java:6570), not
            // this family — the earlier attribution was wrong.
            82, 83, 84, 85, 326 -> {
            }
            // g.java L30a9 (proven) — S91 settle: zero all four velocity
            // fields; `r() → i(0)`; early `return`.
            91 -> {
                p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                if (p.animFinished()) p.setAnim(0)
                return false
            }
            // g.java L1a46 (proven) — S92/S101 wall-bounce: zero all,
            // `r() → av=!av; ag=∓2048 (new facing); aO==20 → ah=0 else
            // ah=-5120`, then `a(36,36)` re-enters the wall state.
            92, 101 -> {
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
                if (p.animFinished()) p.enterStateMasked(53, 1032, world)
            }
            // g.java L2eb9 (proven) — S148 knockback-launch: `ah=-5120`;
            // `r() → i(149)`.
            148 -> {
                p.ah = -5120
                if (p.animFinished()) p.setAnim(149)
            }
            // g.java L2ed1 (proven) — S149 fling carry: `ag = g.l ? g.l :
            // ∓1024`; `aj=1536`; `r() → g.l=0; ag=0; i(150)`.
            149 -> {
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
                return false
            }
            // g.java L2f13 (proven) — S156 launch-prep: zero all; `r() →
            // ag=g.l; ah=0; i(157)`; early `return`.
            156 -> {
                p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    p.ag = p.gL; p.ah = 0; p.setAnim(157)
                }
                return false
            }
            // g.java L2596 (proven) — S204 victim-dump loop: `r() →
            // i(203)`; early `return` (skips the shared tail).
            204 -> {
                if (p.animFinished()) p.setAnim(203)
                return false
            }
            // g.java L305d (proven) — S214 knockback-rise: `ah=-768`;
            // `r() → ah=0; i(215)`; early `return`.
            214 -> {
                p.ah = -768
                if (p.animFinished()) { p.ah = 0; p.setAnim(215) }
                return false
            }
            // g.java L2f66/L311a/L311b (proven) — S225/244/250 fully
            // inert: the arm is a bare `return` — no flags, no tail.
            225, 244, 250 -> return false
            // g.java L2b6c (proven) — S282 pinned: `r() → i(38)`; early
            // `return`.
            282 -> {
                if (p.animFinished()) p.setAnim(38)
                return false
            }
            // g.java L309c (proven) — S283: `r() → i(0)`; early `return`.
            283 -> {
                if (p.animFinished()) p.setAnim(0)
                return false
            }
            // ---- slice 196: the last unported g.e() arms -------------
            // g.java L2b66/L2b69/L2f63 (proven) — S59/S65/S164/S211:
            // bare `goto L353d` — no arm, no L351e settle; only the
            // shared tail runs. Empty arm so `else` can't fling them.
            // S371 (slice 349): the Cesare grab hold after S370 —
            // `371 → 2223: goto 13629` (= L353d; g.javap.txt:2890, :2924),
            // the same bare tail; ax61's S12 arm owns the release.
            59, 65, 164, 211, 371 -> { }
            // g.java L1320 (proven) — S209 vehicle ride: no mount →
            // a(0) fling. `al` rides the mount (ax66 S11/S12 → own al,
            // else W[1]+1); last-frame on ax60 snaps ak; on anim end
            // ak re-centers and ax66-S12 exits to S228 (Z[0]>0) or
            // S358; else i(0). Early `return` on every path.
            209 -> {
                val a = p.standingOn
                if (a == null) { p.flingAirborne(0, world); return false }
                p.al = if (a.ax == 66 && (a.S == 11 || a.S == 12)) a.al
                       else a.W[1] + 1
                val c = p.clip
                if (c != null && p.T == c.frameCount(p.S) - 1 &&
                    a.ax == 60) {
                    p.ak = a.ak; p.setAnim(0)
                }
                if (p.animFinished()) {
                    p.ak = if (a.ax == 66 && (a.S == 11 || a.S == 12)) a.ak
                           else (a.W[0] + a.W[2]) shr 1
                    if (a.ax == 66 && a.S == 12) {
                        p.setAnim(if (a.Z[0] > 0) 228 else 358)
                        return false
                    }
                    p.setAnim(0); return false
                }
                return false
            }
            // g.java L2f67 (proven) — S216 dive-attack windup: T∈[2,3]
            // drives `ag=∓5120`; facing-cell probe (aT/aU) → a(0)
            // fling; `r() → ag>>=1, i(217), P|=64`; early `return`.
            216 -> {
                p.ag = if (p.T in 2..3) (if (p.av) -5120 else 5120) else 0
                if ((p.av && p.aT != 0) || (!p.av && p.aU != 0)) {
                    p.flingAirborne(0, world)
                }
                if (p.animFinished()) {
                    p.ag = p.ag shr 1
                    p.setAnim(217)
                    p.P = p.P or 64
                }
                return false
            }
            // g.javap.txt e() 12248-12380 (proven) — S217 dive-attack
            // descent: T==0 → `aj=1536`; `(aR==2 || aO==2 || L()) &&
            // g.a==null` → `ah=aj=0`, `g.e(0)` (x[1]=0), `i(50)`,
            // `return`; else `aZ||aR==5` → `bd=1; a(true); P&=~64` and
            // zero velocity — 12334 is `invokevirtual #282 i.a:(Z)V`, the
            // side rescan, not `g.a(int)` (slice 369 F3: the port flung
            // the player into S43 here); `r() → i(0)`; `return`.
            217 -> {
                if (p.T == 0) p.aj = 1536
                if ((p.aR == 2 || p.aO == 2 ||
                     p.e(world, p.ak / 20, p.al / 20) == 2) &&
                    p.standingOn == null) {
                    p.ah = 0; p.aj = 0; p.x1 = 0; p.setAnim(50); return false
                }
                if (p.aZ || p.aR == 5) {
                    p.bd = true
                    p.collideSides(world, true)              // a(true)
                    p.P = p.P and -65
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0
                }
                if (p.animFinished()) p.setAnim(0)
                return false
            }
            // g.java L32b3 (proven) — S258 wall-perch: UP-edge →
            // i(259); L/R edges (2/8 taps or 4112/8256 held) → `av`
            // pick + i(261); DOWN-edge → `a(0)` + `al+=10` (a(0) itself
            // adds +10 — the double-drop is verbatim); `return`.
            258 -> {
                if (pad.v(16388)) { p.setAnim(259); return false }
                if (pad.v(2) || pad.v(8) || pad.v(4112) || pad.v(8256)) {
                    if (pad.v(2) || pad.v(4112)) p.av = true
                    else if (pad.v(8) || pad.v(8256)) p.av = false
                    p.setAnim(261); return false
                }
                if (pad.v(33024)) { p.flingAirborne(0, world); p.al += 10 }
                return false
            }
            // g.java L3334 (proven) — perch launch: `r()` → S259 goes
            // straight up (`ag=0, ah=-7680, i(263)`); S261 goes to the
            // side (`ag=∓3072, ah=-7680, i(264)`); `return`.
            259, 261 -> {
                if (p.animFinished()) {
                    if (p.S == 259) {
                        p.ag = 0; p.ah = -7680; p.setAnim(263); return false
                    }
                    p.ag = if (p.av) -3072 else 3072
                    p.ah = -7680
                    p.setAnim(264)
                }
                return false
            }
            // g.java L323b (proven) — perch-entry transitions: same
            // input fork as S258 (UP→259, dir→av+261) but falls into
            // `r() → i(258)` on anim end; `return`.
            260, 262 -> {
                if (pad.v(16388)) p.setAnim(259)
                else if (pad.v(2) || pad.v(8) || pad.v(4112) || pad.v(8256)) {
                    if (pad.v(2) || pad.v(4112)) p.av = true
                    else if (pad.v(8) || pad.v(8256)) p.av = false
                    p.setAnim(261)
                }
                if (p.animFinished()) p.setAnim(258)
                return false
            }
            // g.java L3386 (proven) — perch up-launch: `cw=1`, `aj=
            // 2560`; `ah>=0` → 264→i(266), 263→i(265); `av()` wall
            // resolve; `y()` grab → `ai=ag=0` + `a(0)`; tail.
            263, 264 -> {
                p.cw = true
                p.aj = 2560
                if (p.ah >= 0) {
                    if (p.S == 264) p.setAnim(266)
                    else if (p.S == 263) p.setAnim(265)
                }
                p.airWallResolve(world)
                if (p.climbCheck()) {
                    p.ai = 0; p.ag = 0
                    p.flingAirborne(0, world)
                }
            }
            // g.java L33da (proven) — launch descent: `r() → a(0)`;
            // `av()`; `y()` → `ai=ag=0` + `a(0)`; tail.
            265, 266 -> {
                if (p.animFinished()) p.flingAirborne(0, world)
                p.airWallResolve(world)
                if (p.climbCheck()) {
                    p.ai = 0; p.ag = 0
                    p.flingAirborne(0, world)
                }
            }
            // g.java Lc88 (proven) — S267 carry-init: `g.t=100`; if af
            // is not an ax27/ax10 target, `i.ae()` (standingOn or a
            // W∩kAc ax∈{17,11,23,50,73} carry-check) must pass or →
            // `g.t=0, i(0)`; `E()` when airborne; walks to the target
            // centre (`ag=∓1024`, av toward it) until |dx|≤4 → snap,
            // `av=0`, ax10→i(291) else i(268)+af.i(1); `return`.
            267 -> {
                p.gt = 100
                val f = p.af
                if (f == null || (f.ax != 27 && f.ax != 10)) {
                    if (!carryAvailable(p)) { p.gt = 0; p.setAnim(0); return false }
                }
                if (!p.aZ) p.eSettle(world)
                val f2 = p.af!!
                val cx = if (f2.ax == 10) (f2.W[0] + f2.W[2]) shr 1
                         else (f2.X[0] + f2.X[2]) shr 1
                val dx = cx - p.ak
                if (kotlin.math.abs(dx) <= 4) {
                    p.ak = cx; p.av = false
                    if (f2.ax == 10) p.setAnim(291)
                    else { p.setAnim(268); f2.setAnim(1) }
                } else if (dx < 0) {
                    p.av = true; p.ag = -1024
                } else {
                    p.av = false; p.ag = 1024
                }
                return false
            }
            // g.java Ld69 (proven) — S268 hostage-carry walk: `ag=ah=
            // 0`; body only when `r() || T≥len-2`; needs af ax27;
            // marker 102 (ae) hovers 85px overhead; `af.i(2)+az=-2`
            // once; anim-end → `T=len-1,U=0`; `g.g` within 60 → marker
            // 8, else release it; `k.u()` edge (+r9/g.g fork) →
            // `af.i(1), k.v(), G()`, return; `r9 && v(65568) && g.g` →
            // `G(), af snaps to player, i(3), i(270), A(13)`; `return`.
            268 -> {
                p.ag = 0; p.ah = 0
                val c = p.clip
                val nearEnd = p.animFinished() ||
                    (c != null && p.T >= c.frameCount(p.S) - 2)
                if (!nearEnd) return false
                val f = p.af
                if (f == null || f.ax != 27) return false
                if ((p.ae == null || p.ae!!.S != 102) && f.S != 1) {
                    p.releaseAe()
                    p.spawnMarker(world, 102, p.ak, p.al - 85)
                }
                p.ae?.let { it.ak = p.ak; it.al = p.al - 85 }
                if (f.S != 2 && p.az != -2) {
                    f.setAnim(2); p.az = -2; return false
                }
                p.T = (c?.frameCount(p.S) ?: 0) - 1; p.U = 0
                var r9 = 0
                val gg = p.g
                if (gg != null) {
                    r9 = if (kotlin.math.abs(gg.ak - p.ak) >= 60) 0 else 1
                    if (r9 == 1) {
                        if (p.ae == null || p.ae!!.S != 8) {
                            p.releaseAe()
                            p.spawnMarker(world, 8, p.ak, p.al - 85)
                        }
                    } else if (p.ae != null && p.ae!!.S == 8) p.releaseAe()
                } else if (p.ae != null && p.ae!!.S == 8) p.releaseAe()
                if (padAnyEdge(pad) && (r9 == 0 || p.g == null)) {
                    f.setAnim(1); world.clearLatches(); p.releaseAe(); return false
                }
                if (r9 != 0 && pad.v(65568) && p.g != null) {
                    p.releaseAe()
                    f.ak = p.ak; f.al = p.al; f.setAnim(3)
                    p.setAnim(270); world.sfx(13)
                }
                return false
            }
            // g.java Lf19 (proven) — S269 carry-drop: `g.t=0`; `r()` &&
            // af ax27 → `i(0)+E()+af.i(2)+af=null`; shared tail.
            269 -> {
                p.gt = 0
                if (p.animFinished()) {
                    val f = p.af
                    if (f != null && f.ax == 27) {
                        p.setAnim(0); p.eSettle(world)
                        f.setAnim(2); p.af = null
                    }
                }
            }
            // g.java Lf50 (proven) — S270 grab-QTE lead-in: `g.t=0`;
            // no `g.g` → `i(0)` + tail; `g.g` forced to S133; `r()` →
            // `i(271)`, `g.g.i(145)`, `bl=5`; `return` (with-gg only).
            270 -> {
                p.gt = 0
                val gg = p.g
                if (gg == null) {
                    p.setAnim(0)
                } else {
                    if (gg.S != 133) gg.setAnim(133)
                    if (p.animFinished()) {
                        p.setAnim(271); gg.setAnim(145); p.bl = 5
                    }
                    return false
                }
            }
            // g.java Lf94 (proven) — S271 grab-QTE mash: `g.t=0`; no
            // `g.g` → `i(0)` + tail; `g.g` → S134; marker 8 overhead;
            // `v(65568)` → `bl+=2` else `j.g%2==0` → `bl--`; `bl<0` →
            // release, `i(0)`, `g.g.i(5)`, `aA=1`, `Q()`; `bl>=10` →
            // release, `P&=~64`, `i(0)`, `g.g.aB=0`, `i(135)`,
            // `k.e(0,aw)` kill credit, `k.o(3)` stat; `return` (with-gg).
            271 -> {
                p.gt = 0
                val gg = p.g
                if (gg == null) {
                    p.setAnim(0)
                } else {
                    if (gg.S != 134) gg.setAnim(134)
                    if (p.ae == null || p.ae!!.S != 8) {
                        p.releaseAe()
                        p.spawnMarker(world, 8, p.ak, p.al - 85)
                    }
                    p.ae?.let { it.ak = p.ak; it.al = p.al - 85 }
                    if (pad.v(65568)) p.bl += 2
                    else if (world.jG % 2 == 0L) p.bl -= 1
                    if (p.bl < 0) {
                        p.bl = 0; p.releaseAe(); p.setAnim(0)
                        gg.setAnim(5); gg.aA = 1; gg.facePlayer(world)
                        return false
                    }
                    if (p.bl >= 10) {
                        p.bl = 0; p.releaseAe(); p.P = p.P and -65
                        p.setAnim(0)
                        gg.aB = 0; gg.setAnim(135)
                        world.countKill(p.aw); world.kCount(3)
                        return false
                    }
                    return false
                }
            }
            // g.java Lc7a (proven) — S280: `r() → i(38)`; `return`.
            280 -> {
                if (p.animFinished()) p.setAnim(38)
                return false
            }
            // g.java La41 (proven) — S286/287 knife-aim hold: airborne
            // w/o mount → `a(0)`; `v(65568)` queues `R` (286→287,
            // 287→286); `r()` → `i(R!=-1?R:0)`, `R=-1`; `return`.
            286, 287 -> {
                if (!p.aZ && p.standingOn == null) {
                    p.flingAirborne(0, world); return false
                }
                if (pad.v(65568)) {
                    p.R = -1
                    if (p.S == 286) p.R = 287
                    else if (p.S == 287) p.R = 286
                }
                if (p.animFinished()) {
                    p.setAnim(if (p.R != -1) p.R else 0)
                    p.R = -1
                }
                return false
            }
            // g.java Lb10 (proven) — S291 drag-carry: `ag=ah=0`; body
            // when `r() || T≥len-2` else tail; needs af ax10; markers
            // 102+8; `k.u()` fork → `i(285), af=null, k.v(), G()`;
            // `r9&&v(65568)&&g.g` → `G(), af=null, i(270), A(13)`;
            // every exit → shared tail (L353d).
            291 -> {
                p.ag = 0; p.ah = 0
                val c = p.clip
                if (p.animFinished() ||
                    (c != null && p.T >= c.frameCount(p.S) - 2)) {
                    val f = p.af
                    if (f != null && f.ax == 10) {
                        p.T = (c?.frameCount(p.S) ?: 0) - 1; p.U = 0
                        if (p.ae == null || p.ae!!.S != 102) {
                            p.releaseAe()
                            p.spawnMarker(world, 102, p.ak, p.al - 85)
                        }
                        p.ae?.let { it.ak = p.ak; it.al = p.al - 85 }
                        var r9 = 0
                        val gg = p.g
                        if (gg != null) {
                            r9 = if (kotlin.math.abs(gg.ak - p.ak) >= 60) 0 else 1
                            if (r9 == 1) {
                                if (p.ae == null || p.ae!!.S != 8) {
                                    p.releaseAe()
                                    p.spawnMarker(world, 8, p.ak, p.al - 85)
                                }
                            } else if (p.ae != null && p.ae!!.S == 8) {
                                p.releaseAe()
                            }
                        } else if (p.ae != null && p.ae!!.S == 8) {
                            p.releaseAe()
                        }
                        if (padAnyEdge(pad) && (r9 == 0 || p.g == null)) {
                            p.setAnim(285); p.af = null
                            world.clearLatches(); p.releaseAe()
                        } else if (r9 != 0 && pad.v(65568) && p.g != null) {
                            p.releaseAe(); p.af = null
                            p.setAnim(270); world.sfx(13)
                        }
                    }
                }
            }
            // g.java L92e (proven) — S313: bare `return`.
            313 -> return false
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
                    // g.javap.txt e() 6921 (slice 365): the ladder snap
                    // `return`s — the cp it just set never reaches the
                    // post-tail ledge consumer.
                    return false
                } else {
                    val dirKey = if (!p.av) pad.u(Pad.M_RIGHT)
                                 else pad.u(Pad.M_LEFT)
                    // L1b0a-L1b8f (fallback g.java:4150-4157, proven —
                    // branch order matters: holding toward the wall
                    // WHILE RISING arms the lip scan; any other case —
                    // no dir press, or dir press while falling — probes
                    // for the kick/fall).
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
                        // L824-L832 (proven): x() probe → aR|aS∈{5,20}
                        // → a(43,32) fall; else a(34,36)+aA() wall-kick.
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
            // `e()` case 32 (g.javap.txt 6512-6630, slice 372 — proven):
            // the run-start slide. `i.f(this)`; airborne with no ride
            // (`!aZ && g.a == null`) → `cq = 0; a(0)` fall; moving
            // (`ag != 0`) → `a(true)` side rescan, and a wall in the
            // direction of motion (`y()`) zeroes `ag`; anim end → `ag = 0;
            // i(Q==79 ? 79 : 0)`, and a settled S0 re-probes (`x()`) and
            // re-embeds into S79 when the head cell is solid (`aO > 12`).
            32 -> {
                world.scrollWallClamp(p)      // 6512 i.f(this)
                if (!p.aZ && p.standingOn == null) {                 // 6516-6538
                    p.cq = false
                    p.flingAirborne(0, world)                       // a(0)
                } else {
                    if (p.ag != 0) p.collideSides(world, true)      // 6541-6550 a(true)
                    if (p.hitWall() && p.ag != 0) p.ag = 0          // 6553-6569
                    if (p.animFinished()) {                         // 6572
                        p.ag = 0
                        p.setAnim(if (p.Q == 79) 79 else 0)
                        if (p.S == 0) {                             // 6603-6627
                            p.probeCells(world)                     // x()
                            if (p.aO > 12) p.setAnim(79)
                        }
                    }
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
                } else if (if (p.av) pad.u(Pad.M_LEFT) else pad.u(Pad.M_RIGHT)) {
                    // L2cb7 (proven): the facing-direction key held → i(37)
                    // climb — NOT the negated arm the port first carried.
                    p.setAnim(37)
                } else if (if (p.av) pad.v(Pad.M_RIGHT) else pad.u(Pad.M_LEFT)) {
                    // L2cdc (proven): away-key (v RIGHT if av, else u LEFT)
                    // turns the hang around.
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
            // Slice 365 (g.javap.txt e(), proven): the lunge/mount family
            // never reaches L353d — 272-275/292 return at 13344/13349,
            // 277/293 at 13354, 294 at 13355, 299-302 at 13421, 310 at
            // 13422, 295 at 2806, 304-306 at 2831; S303 returns at 13406
            // unless `u(62430)` sends it to i(0) + L353d (13369).
            272, 273, 274, 275, 292 -> {
                val mount = Entity.at
                if (mount != null && mount.Z[0] == 4 && p.cE <= 0) {
                    p.mountOrbitTick(world, pad)      // L1863 tail — cart
                } else p.lungeTick(world)
                return false
            }
            298 -> {                          // L1293
                if (p.animFinished()) p.P = p.P or 64
                val mount = Entity.at
                if (mount != null && mount.S != 168) p.lungeTick(world)
            }
            // mounted/riding states — L1872 + siblings (g.java:886-925)
            277, 293 -> { p.mountOrbitTick(world, pad); return false }
            294, 310 -> return false            // L1874/L1888 — anim only
            299, 300, 301, 302 -> {             // L1885 — windup → S303
                if (p.animFinished()) p.setAnim(303)
                return false
            }
            303 -> {                          // L1876 — cart dismount
                if (pad.u(62430)) p.setAnim(0)
                else {
                    if (p.animFinished()) {
                        p.P = p.P or 64
                        p.interactGauge(world)
                        // L1879 (g.java:3445, proven): 65568 edge -> ar()
                        if (pad.v(Pad.M_CONTEXT)) p.interactAction(world, pad)
                    }
                    return false
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
                return false
            }
            // L218 (g.java:2560, proven): mounted reach-anim end → gauge
            304, 305, 306 -> {
                if (p.animFinished()) { p.K = 0; p.cN = 0; p.setAnim(295) }
                return false
            }
            // L1926-L1947 (g.java:3455+): S297 + the S296/307-309/276/
            // 278-281/285/288-290/314/316 family all fall into the shared
            // postTail chain (ab/aR/aO bookkeeping) — no dedicated arm.
            297 -> { }
            // g.javap.txt e() 13423-13450 (slice 369 F2, proven) — the
            // grab-hold counter (S311) and throw (S312) anims: 13425 is
            // `invokevirtual #282 i.a:(Z)V` = `a(true)`, the side rescan
            // (slice 277 read it as `g.a(1)`, the S43 fall); the player
            // stays in S311/S312 until `r()`, then `ah = ag = 0; l()`;
            // `return` at 13450. (Slice 277 also dropped a slice-28 guess
            // that resumed lungeTick here.)
            311, 312 -> {
                p.collideSides(world, true)              // a(true)
                if (p.animFinished()) { p.ah = 0; p.ag = 0; l(p, pad) }
                return false
            }
            199 -> case199(p, pad)
            5 -> landArm(p, pad)              // L464
            6 -> {                            // L139
                if (p.animFinished()) {
                    p.ah = 0; p.ag = 0; p.aj = 0; p.ai = 0; p.P = p.P or 64
                }
                if (!pad.u(Pad.M_UP)) p.setAnim(0)
                return false                  // g.javap.txt e() 2403 (slice 365)
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
                return false                    // g.javap.txt e() 2619/2624 (slice 365)
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
            257 -> { ledgeDropArm(p); return false }  // L1770; returns at 12858 (slice 365)
            // g.java L2b3b (proven) — S56 grab-settle: `ah=ag=0`; anim end
            // → any key held (`u(127999)`) → i(65) shimmy, else i(59)
            // hang-idle.
            56 -> {
                p.ah = 0; p.ag = 0
                if (p.animFinished()) {
                    if (pad.u(127999)) p.setAnim(65) else p.setAnim(59)
                }
            }
            // g.java L2421 (proven) — S60 ledge-hang: when it didn't
            // arrive via the S63 climb (Q!=63) or UP/toward-wall is held
            // → i(62) climb-up; always latches `cu`.
            60 -> {
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
                if (p.animFinished()) {
                    p.ak += if (p.av) -10 else 10
                    p.enterStateMasked(if (p.aO > 12) 79 else 0, 9, world)
                }
            }
            // g.java L25a5 (proven) — S63 climb anim end → i(60) hang.
            63 -> {
                if (p.animFinished()) p.setAnim(60)
            }
            67, 68, 69, 112, 113, 114, 115 -> {                 // L1341 family
                if (!comboArm(p, pad)) return false
            }
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
                return false                    // g.javap.txt e() 13597 (slice 365)
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
                return false                    // g.javap.txt e() 13547 (slice 365)
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
                if (p.animFinished()) p.enterFall(0, world)     // a(0)
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
                    // g.javap.txt e() 11878 (slice 365): the reset path
                    // `return`s; only the waiting ticks reach L353d.
                    return false
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
                    world.countKill(p.aw)                  // k.e(0,aw)
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
                    world.countKill(p.aw)                  // k.e(0,aw)
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
                // g.javap.txt e() 4688 (slice 365): the death arm returns —
                // no L353d tail, no post-tail (no S148 `A` latch, no jump).
                return false
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
                    // simple g.java:L1748-1754 (proven — structured folds
                    // the ac==null arm wrongly): release fires whenever
                    // `ac==null` OR facing away from the claim.
                    if (p.ac == null || p.av != (p.ac!!.ak < p.ak)) {
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
                    p.flingAirborne(0, world)  // a(0), 12746-12748 (slice 369 F4)
                    p.setAnim(252)
                }
                // g.javap.txt e() 12758-12768 (slice 365): `S != 228 →
                // goto 13629`, else `return` — a held S228 skips the tail;
                // S358 and the 235/252 exits reach L353d.
                if (p.S == 228) return false
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
                    // 5482-5517 (slice 369 F4): both exits are `a(0)` —
                    // `invokevirtual #213 g.a:(I)V`, the S43 fall — not
                    // `i(0)`.
                    p.airWallResolve(world)                              // av()
                    if (p.climbCheck()) {                                // y()
                        p.ai = 0; p.ag = 0
                        p.flingAirborne(0, world)
                    }
                    if (p.animFinished()) p.flingAirborne(0, world)
                }
                return false                    // g.javap.txt e() 5481/5520 (slice 365)
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
                    p.flingAirborne(0, world) // a(0), 5774-5776 (slice 369 F4)
                }
                return false                    // g.javap.txt e() 5779/5787 (slice 365)
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
                return false                    // g.javap.txt e() 5716 (slice 365)
            }
            // `e()` cases 242/243 (L1721, proven — structured g.java:3428+):
            // bind-release anims — `aj=1536` gravity; S242 apex (`ah>=0`) →
            // i(243); S243 `r()` → `a(0)`; `av()` edge guard then
            // `y() → a(true); ai=ag=0` settle.
            242, 243 -> {
                p.aj = 1536
                if (p.S == 242 && p.ah >= 0) p.setAnim(243)
                // `r() → a(0)` — a(0) is g.a(int)=enterFall (S43, ag kept),
                // NOT i(0): the ax72 fling's ag must survive so the drift
                // carries the player across the u219 grab chain.
                if (p.S == 243 && p.animFinished()) p.flingAirborne(0, world)
                p.airWallResolve(world)                                  // av()
                if (p.climbCheck()) {                                    // y()
                    p.collideSides(world, true)                          // a(true)
                    p.ai = 0; p.ag = 0
                }
                return false                    // g.javap.txt e() 12569 (slice 365)
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
        // L353d = L1926 (g.javap.txt e() 13629-13738, proven) — the head
        // of the shared tail every `goto 13629` arm and the default arm
        // reach. 13629-13659: `S!=9 && ab!=null && ab.S==14 → ab = null`.
        if (p.S != 9 && p.ab?.S == 14) p.ab = null
        // 13662-13711 (slice 370, F1 of slice 365): the type-2 kill —
        // `(aR==2 || aO==2 || L()) && g.a==null → ah = aj = 0; g.e(0);
        // i(50); return`. A type-2 cell below the feet (aR), at the head
        // (aO) or at the anchor (`L()` = `e(ak/20, al/20) == 2`,
        // i.javap.txt L() 0-25) kills an unmounted player; the `return`
        // skips the J&4 block and the post-tail. The shipped type-2 cells
        // are the lethal pit bottoms: one row on top of the floor, where a
        // landing (`d()`: al = ((W[3]+1)/20)*20 - 1) puts the anchor.
        if ((p.aR == 2 || p.aO == 2 ||
                p.e(world, p.ak / 20, p.al / 20) == 2) &&       // L()
            p.standingOn == null) {                             // g.a == null
            p.ah = 0; p.aj = 0
            p.x1 = 0                                            // g.e(0)
            p.setAnim(50)
            return false                                        // 13711
        }
        // 13712-13736: `aO==6 || aR==6 → a(18,0,0,this)` — the type-6 hurt
        // cell at the head or below the feet.
        if (p.aO == 6 || p.aR == 6) p.applyHit(18, 0, p, world)
        mountEntry(p, pad)  // 13739-14348 — the J&4 mount/assassinate block
        return true
    }

    /**
     * g.javap.txt e() 13739-14348 (proven) — the L1947 block inside
     * `g.e()`: the `J&4` mount-request consumer. Two arms:
     *  - 13758-14079 **mount**: `i.at` bound and no interact target in
     *    front → in-range check (ax72 Z[0]∈{1,3,4} gates) → a press
     *    zeroes velocity and lunges via `c(i.at)`.
     *  - 14082-14201 **assassinate**: an ax11 in front with `Z[19]==1`
     *    (assassination window set by its FSM) and `!P()` → same lunge
     *    on `c(g)` — no sfx in this arm (victim anim plays it).
     * The press is `v(65568) || (!k.k() && V())` in both arms (13998-
     * 14016, 14140-14158; slice 369 F10): with the touch pad off, a touch
     * on the hand indicator fires it.
     * `r98` arms the 14204 tail: the clip-74 hand at view center
     * (`!k.k()`) or the clip-9 marker over the head (`k.k()`), then
     * `g.cm = 1` (14315 is `putstatic #46 g.cm`, not `k.cm`). The
     * `o()→ao()` weapon cycle at 14349 is the post-tail's first step
     * ([postTail] `groundOrVehicle() → cycleEquip()`).
     */
    fun mountEntry(p: Entity, pad: Pad) {
        // g.javap.txt e() 13739-13755 (proven): `r98 = 0`, then `(J&4)==0
        // || S==50 → goto 14204` — that skips the scan only; the r98/cm
        // tail at 14204 still runs, so a held `g.cm` drains on a tick
        // without the mount request (slice 365; the port used to return).
        val r98 = p.gJ and 4 != 0 && p.S != 50 && mountScan(p, pad)
        // 14204-14319 tail
        if (r98) {
            if (!world.mounted) {                                     // !k.k()
                if (!p.indicatorIsHand(world)) p.releaseAe()          // !T() → G()
                p.spawnHand(world, 200 + world.kO, 120 + world.kP)    // c(x,y)
                p.moveHand(world, 200 + world.kO, 120 + world.kP)     // d(x,y)
            } else {
                // 14262-14312 (slice 369 F10): `T() → U()`, then always
                // `a(8, ak, al-85)` + `ae` pinned to (ak, al-85). The
                // port read it as `!T() → U() + a(8,…)`, which left a
                // showing hand in place.
                if (p.indicatorIsHand(world)) p.dropIndicator(world) // T() → U()
                p.spawnMarker(world, 8, p.ak, p.al - 85)            // a(8,ak,al-85)
                p.ae?.let { it.ak = p.ak; it.al = p.al - 85 }
            }
            // 14315 `g.cm = 1`. (The port also wrote `k.cm = 1` here — the
            // touch-pad flag — a misread of the same-named static.)
            p.gcm = true
        } else if (p.gcm) {
            // L37f2 (proven): quiet tick after a mount event — clear the
            // g.cm latch and refresh the indicator: `k.k() ? G() : U()`.
            p.gcm = false
            if (world.mounted) p.releaseAe() else p.dropIndicator(world)
        }
        // The L1947 block falls straight into e()'s post-tail
        // (g.javap.txt e() 14349): the `o()→ao()` cycle and the
        // L2051-L2057 `aA` bookkeeping run there, once, in postTail.
    }

    /** `v(65568) || (!k.k() && V())` — the J&4 press (g.javap.txt e()
     *  13998-14016 / 14140-14158, slice 369 F10). */
    private fun mountPress(p: Entity, pad: Pad): Boolean =
        pad.v(Pad.M_CONTEXT) || (!world.mounted && p.indicatorNearTouch(world))

    /** The 13758-14201 scan of the J&4 block (mount arm / assassinate
     *  arm); returns `r98` (local 1). */
    private fun mountScan(p: Entity, pad: Pad): Boolean {
        var r98 = false
        val t = Entity.at
        val bound = p.g
        if (t != null && (bound == null || !p.inFrontOf(bound))) {
            // 13786-13992 — mount in-range (r2 starts true; the ax72
            // gates only clear it). Z[0]==4 (13889-13962, slice 369
            // F10): `Z[4] < 0 || !aZ || |ak - at.ak| < (W[2]-W[0])<<1 →
            // r2 = 0` — the band check falls through into `r2 = 0`; the
            // port had read it as dead code.
            if (p.aZ || p.mountableState()) {
                if (t.inPlayV(world)) {                     // g.e() @13806 i.at.v()
                    var r104 = true
                    if (t.ax == 72 && t.Z[0] == 1 &&
                        p.h(t.ak - p.ak, t.al - p.W[1]) >= t.Z[3]) r104 = false
                    if (t.ax == 72 && t.Z[0] == 4 && (t.Z[4] < 0 || !p.aZ ||
                        Math.abs(p.ak - t.ak) < ((p.W[2] - p.W[0]) shl 1))) r104 = false
                    if (t.ax == 72 && t.Z[0] == 3) r104 = false
                    r98 = r104
                    if (r104 && mountPress(p, pad)) {
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
            // 14082-14201 — assassinate window on the in-front target
            if (p.aZ || p.mountableState()) {                  // 14121-14135
                r98 = true
                if (mountPress(p, pad)) {                      // 14140-14158
                    p.ag = 0; p.ah = 0; p.aj = 0
                    if (world.mounted) p.releaseAe() else p.dropIndicator(world)
                    p.grabLunge(bound, world)                  // c(g)
                    p.z = false
                }
            }
        }
        return r98
    }

    // -- grounded family arm (g.javap.txt e() 6092-6357) ----------------------
    /**
     * The shared arm of S1/7/11/26/79 (and S0/S12 via 6089), bytecode
     * order (slice 369 F7, proven):
     *  - 6092-6116: `aO>12 && aR>12 → i(79); goto 13629` — embedded in
     *    solid cells the arm ends there: no `l()`, so `cp/cq/z` stay
     *    clear (the simple view drops this `goto`).
     *  - 6119-6267: S79 only — `ag=ah=0`; DOWN (`u|v(33024)`) → probe one
     *    cell lower (`al+=20; x(); al-=20` — the shifted probes and box
     *    are kept, nothing restores them) and `a(257,8)` the vault-drop
     *    when the floor cell is 20/5, the one below it is empty and the
     *    cell two over/one down is open. The arm then carries on.
     *  - 6270: S11 → `ag = (ag<<1)/3`.
     *  - 6291-6311: `u(33024) && am()` → `ab = null; goto 13629`.
     *  - 6314-6333: `y()` → `ag=1; a(true); ag=0`.
     *  - 6336-6357: `!l()` → `ab = null; cq = 0; a(0)`.
     * The other grounded states vault through `l()` → `aw()`'s DOWN arm.
     */
    private fun groundedTail(p: Entity, pad: Pad) {
        if (p.aO > 12 && p.aR > 12) { p.setAnim(79); return }       // 6092-6116
        if (p.S == 79) {                                              // 6119
            p.ag = 0; p.ah = 0
            if (pad.u(Pad.M_DOWN) || pad.v(Pad.M_DOWN)) {
                p.al += 20; p.probeCells(world); p.al -= 20
                val r1 = if (p.av) p.W[0] / 20 - 2 else p.W[2] / 20 + 2
                val r2 = p.W[3] / 20 + 1
                if ((p.aQ == 20 || p.aQ == 5) && p.aR == 0 &&
                    p.e(world, r1, r2) < 12) {
                    p.enterStateMasked(257, 8, world)                // a(257,8)
                }
            }
        }
        if (p.S == 11) p.ag = (p.ag shl 1) / 3                       // 6270
        if (pad.u(Pad.M_DOWN) && wallClimb(p)) { p.ab = null; return } // 6291-6311
        // The I==1 sword arm of `ap()` moved to postTail with the rest
        // of the equip/context dispatcher (L2057 arm, g.java:3817).
        if (p.hitWall()) { p.ag = 1; p.collideSides(world, true); p.ag = 0 }
        if (!l(p, pad)) {                                             // 6336-6357
            p.ab = null; p.cq = false
            p.flingAirborne(0, world)                                 // a(0)
        }
        // g.java:824-844 (proven-DEAD, omitted): the case-0 arm
        // checks `cp && ct` (edge ledge-grab ak()||al()), `cu`
        // (down-edge pop + drop held), `cv` (dir press → aF=1), and
        // `cw` (aO==5 → i(280) one-way hang) — but e()'s head clears
        // all five flags every tick (g.java:617-623) and case 0 sets
        // none of them, so all four arms read false and can never
        // fire in the original. `al()` and `i.H()` are ported on
        // Entity for their live call sites.
    }

    /**
     * `am()` (g.java:301, proven for the `ag != 0` half): moving into a
     * type-19 wall cell (aV/aW = cell left/right of feet) with `aR == 0`
     * snaps `ak` to the grid and enters `a(63, 16385)` (deferred i(63) +
     * `al = ((W[3]+10)/20)*20 - 1` climb-up snap). The `ag==0` L5d arm
     * (g.java:687-712, proven): standing inside a type-19 cell with an
     * open side (aV==0 → face left; aW==0 → face right) snaps `ak` to the
     * open side's grid edge — the ledge pull-up entry.
     * PROVEN-DEAD on shipped content: the == 19 checks are exact and no
     * level pack's `et` grid contains a type-19 cell, so the S63 entry
     * never fires; kept verbatim for parity.
     */
    private fun wallClimb(p: Entity): Boolean {
        when {
            p.ag > 0 && p.aV == 19 && p.aR == 0 -> {
                p.av = true; p.ak = (p.ak / 20) * 20
            }
            p.ag < 0 && p.aW == 19 && p.aR == 0 -> {
                p.av = false; p.ak = (p.ak / 20) * 20 + 20
            }
            p.ag == 0 && p.aR == 19 -> {
                if (p.aV == 0) p.av = false
                if (p.aW == 0) p.av = true
                p.ak = (p.ak / 20) * 20 + if (p.av) 20 else 0
            }
            else -> return false
        }
        p.aj = 0; p.ah = 0; p.ag = 0
        p.enterStateMasked(63, 16385, world)
        return true
    }

    /** S257 exit arm (g.javap.txt e() 12769-12858, proven): anim end →
     *  drop by the frame's offset (`al += aa.c(S,T)+10`, `ak ±= aa.b(S,T)`
     *  by facing), then `a(0)` — 12853-12855 is `invokevirtual #213
     *  g.a:(I)V`, the S43 fall, not `i(0)` (slice 369 F4). */
    private fun ledgeDropArm(p: Entity) {
        if (!p.animFinished()) return
        val c = p.clip ?: return
        p.al += c.frameDy[c.frameIndex(p.S, p.T)] + 10
        p.ak += if (p.av) -c.frameDx[c.frameIndex(p.S, p.T)]
                else c.frameDx[c.frameIndex(p.S, p.T)]
        p.flingAirborne(0, world)                                     // a(0)
    }

    // -- l() grounded input helper (proven) ----------------------------------
    /** `g.l()` — the grounded input helper (bytecode g.javap.txt l()
     *  offsets 0-588, proven; the simple view drops the jump after L124,
     *  which made S11 look like it fell into the right-arm tail). */
    fun l(p: Entity, pad: Pad): Boolean {
        if (world.kAT) return true                      // k.aT: camera on another target
        p.bM = null
        p.aF = 0; p.cp = true; p.cq = true; p.z = true
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
            if (bn) { p.cq = false; p.z = false; return true }
            // offsets 206-231: no jump press → done; UP continues below
            if (!pad.u(Pad.M_UP)) return p.aZ || p.standingOn != null
        }
        val g = p.g                                     // offsets 232-262
        if (g != null && g.ax == 73 && g.Z[0] == 3) p.cq = false
        when {
            pad.u(Pad.M_LEFT) || pad.x(Pad.M_LEFT) || pad.u(Pad.M_LEFT_ALT) -> {
                if (p.av) return ax(p)
                if (p.aA != 0) p.av = true
                // offsets 311-332: held past 4 ticks (k.bD) → turn west
                else if (pad.bD > 4 && !pad.x(Pad.M_LEFT)) p.av = true
                else if (pad.x(Pad.M_LEFT)) {
                    p.setAnim(10); p.ag = -4096
                    if (p.hitWall()) p.ag = 0
                    return true
                }
            }
            pad.u(Pad.M_RIGHT) || pad.x(Pad.M_RIGHT) || pad.u(Pad.M_RIGHT_ALT) -> {
                if (!p.av) return ax(p)
                if (p.aA != 0) p.av = false
                else if (pad.bD > 4 && !pad.x(Pad.M_RIGHT)) p.av = false
                else if (pad.x(Pad.M_RIGHT)) {
                    p.setAnim(10); p.ag = 4096
                    if (p.hitWall()) p.ag = 0
                    return true
                }
            }
            pad.u(Pad.M_DOWN) -> {
                if (p.ag != 0) { p.ag = 0; p.setAnim(32); return true }
            }
            // offsets 535-583: no direction — a run past 4 ticks brakes
            // (`i(11)`), and the brake holds until its anim ends.
            (p.S == 12 && p.co >= 4) || p.S == 11 -> {
                if (p.S != 11 || !p.animFinished()) { p.setAnim(11); return true }
            }
        }
        return aw(p, pad)
    }

    // -- ax() sustained run (proven) ------------------------------------------
    private fun ax(p: Entity): Boolean {
        p.ah = 0
        if (!p.aZ && p.standingOn == null) { p.cq = false; p.z = false; return false }
        if (p.aR > 18) {
            // edge walk: open space beside the feet cell and no wall
            if (p.av && p.aV < 12 && !p.bb) { p.setAnim(26); p.ag = -1280 }
            else if (!p.av && p.aW < 12 && !p.bc) { p.setAnim(26); p.ag = 1280 }
            else runArm12(p)
        } else runArm12(p)
        // L45: slope pull — aR 14/15 pull down, 16/17 push up (@224-250: `aR == 17 || aR == 16`;
        // slice 413 — the port tested 17 only. No shipped level has cells 14-17.)
        when (p.aR) {
            14, 15 -> p.ah = p.ag shr 1
            17, 16 -> p.ah = (-p.ag) shr 1
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

    // -- aw() settle tail (g.javap.txt aw() 0-386, proven — every arm
    //    ported: airborne fall, S79 overhead/UP, DOWN vault/crouch, UP
    //    carry, j.g%100 idle flicker, settle) --------------------------------
    private fun aw(p: Entity, pad: Pad): Boolean {
        p.ag = 0; p.ah = 0
        if (!p.aZ && p.standingOn == null) {
            // `k.aS.af` (g.java:5120-5124, proven): bound → return true
            // (stay); unbound → L58: `S==284 → L73 return true` (bound
            // state rides out), else `a(0)` = g.a(int) = enterFall.
            if (p.af != null) return true
            if (p.S == 284) return true
            p.enterFall(0, world)
            p.cq = false; p.z = false
            return true
        }
        if (p.S == 79 && !bn) {
            // overhead-solid probe (g.java:5130-5136, proven literal +
            // `u(16388)`): `i(80); k.v()` — consume the grab input.
            if (p.e(world, ((p.W[0] + p.W[2]) / 2) / 20, p.W[1] / 20 - 1) > 12) return true
            if (pad.u(Pad.M_UP)) { p.setAnim(80); world.clearLatches(); return true }
            return true
        }
        if (p.S != 2 && pad.u(Pad.M_DOWN)) {
            if (p.aS == 9) { p.enterStateMasked(122, 8, world); return true }
            p.al += 20; p.probeCells(world); p.al -= 20
            val side = if (p.av) p.W[0] / 20 - 2 else p.W[2] / 20 + 2
            val below = p.W[3] / 20 + 1
            if ((p.aQ == 20 || p.aQ == 5) && p.aR == 0 &&
                p.e(world, side, below) < 12) {
                p.enterStateMasked(257, 8, world)       // a(257,8) vault-drop
            } else {
                p.setAnim(78)
            }
            return true
        }
        // g.java:5257-5260: UP with a carry target in reach → i(6)
        if (p.S != 6 && pad.u(Pad.M_UP) && p.isHolding()) { p.setAnim(6); return true }
        if (p.tick100()) { p.setAnim(1); return true }
        if (p.S == 1 && !p.animFinished()) return true
        p.setAnim(if (p.S == 81) 79 else 0)
        return true
    }

    // -- case 199 arm (g.javap.txt e() 6360-6509, proven) ----------------------
    private fun case199(p: Entity, pad: Pad) {
        if (p.aZ || p.standingOn != null) {
            // 6385-6410 (slice 369 F11): `y() && ag != 0 → ag = 0; a(S, 4)`
            // — the mask-4 snap `ak += t - (W[0]+W[2])/2` (i.a(II) 58-95).
            if (p.hitWall() && p.ag != 0) {
                p.ag = 0
                p.enterStateMasked(p.S, 4, world)                    // a(S, 4)
            }
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
            p.cq = false                                              // 6373
            p.flingAirborne(0, world)                                 // a(0)
        }
    }

    // -- S5 land arm (g.javap.txt e() 4689-4827, proven) ------------------------
    /** Slice 369 F8: `i.O(); bM = null` at the head; a jump press sets
     *  `av` (`v(2)`, else `v(8)`), `i(21)` and `ab = null` and does NOT
     *  end the arm — it falls into the `u(94324) → ab = null; l()` and
     *  `r() → ab = null; i(aO>12 ? 79 : 0)` checks, all → 13629. */
    private fun landArm(p: Entity, pad: Pad) {
        p.timewarpOff(world)                                          // i.O()
        p.bM = null
        p.ag = 0; p.ah = 0; p.aj = 0
        if (pad.v(Pad.M_UP) || pad.v(Pad.M_TAP_L) || pad.v(Pad.M_TAP_R)) {
            if (pad.v(Pad.M_TAP_L)) p.av = true
            else if (pad.v(Pad.M_TAP_R)) p.av = false
            p.setAnim(21)
            p.ab = null
        }
        if (pad.u(Pad.M_ANY_DIR)) { p.ab = null; l(p, pad); return }
        if (p.animFinished()) { p.ab = null; p.setAnim(if (p.aO > 12) 79 else 0) }
    }

    // -- S21/233 arm (g.javap.txt e() 7222-7362, proven) -------------------------
    /** Slice 369 F12: `ag = ah = 0`; on `r()` — or at once in S233 —
     *  `i(22)` with `ah = -2560` on an ax51 crate (`g.a`) else `-5120`,
     *  and `ag = ±1024` when `g.a` is an ax43 with its claim `ab()`
     *  running, else `±2048`; `i.f(this)`; → 13629. */
    private fun preJumpArm(p: Entity) {
        p.ah = 0; p.ag = 0
        if (p.animFinished() || p.S == 233) {
            p.setAnim(22)
            val a = p.standingOn                                      // g.a
            p.ah = if (a != null && a.ax == 51) -2560 else -5120
            p.ag = if (a != null && a.ax == 43 && a.claimAb()) (if (p.av) -1024 else 1024)
                   else (if (p.av) -2048 else 2048)
        }
        world.scrollWallClamp(p)               // g.java:1740 — i.f(this) tail
    }

    // -- air family {20,22,23,25,215} (L889 block, proven core) ---------------
    /** `i.ae()` (i.java:59138, proven) — carry-available scan:
     * `g.a != null` → true; else any entity with ax∈{17,11,23,50,73}
     * passing `i.h()`. `i.h()` (i.java:59097): `W∩k.ac` overlap, then
     * per-type: ax11/73 → `!P() && aA≥1`; ax17/50 → true; else false. */
    private fun carryAvailable(p: Entity): Boolean {
        if (p.standingOn != null) return true
        for (e in world.npcs) {
            when (e.ax) {
                17, 11, 23, 50, 73 -> if (carryClaimable(e)) return true
            }
        }
        return false
    }

    private fun carryClaimable(e: Entity): Boolean {
        val r = world.kAc ?: return false
        if (!Entity.overlapStrict(e.W, r)) return false      // h(i) @13 i.a(W, k.ac)
        return when (e.ax) {
            11, 73 -> !e.deadRelease() && e.aA >= 1
            17, 50 -> true
            else -> false
        }
    }

    /** `k.u()` (k.java:20271, proven) — any `bB` edge EXCEPT the two
     *  soft-key bits (0x20000/0x40000): `bB!=0 && !(bB&0x20000) &&
     *  !(bB&0x40000)`. */
    private fun padAnyEdge(pad: Pad): Boolean =
        pad.bB != 0 && pad.bB and 0x20000 == 0 && pad.bB and 0x40000 == 0

    private fun airFamily(p: Entity, pad: Pad) {
        p.cp = true; p.ct = true; p.cw = true
        // L1ce4 (proven): `cv=1` — only the L1ce4-entry states arm cv:
        // {18,19,23,36} direct + 22 via L1cc5. S20/24/25/157/215 enter at
        // L1ce8 and never arm cv — their wall-grab rides S215 or aF
        // alone.
        if (p.S == 22 || p.S == 23) p.cv = true
        if (p.gI == 4) p.z = true              // L1ce8 — I==4 arms z
        p.aj = 1536
        if (p.S == 22 && p.animFinished()) p.P = p.P or 64
        // L1d21-L1e37 (g.java:4436-4565, proven): the airborne launch
        // block — the ballistic impulse for the air states. Gate A:
        // `T==1 && U==0 && S∉{19,23}` (the second-anim-frame entry tick);
        // gate B: `S==215 && ah==0` (mount fling — instant on entry).
        // ag: S20/S215→av?+2048:-2048; S25→av?+1024:-1024; S36→av?-2048:
        // +2048; S24→0; else→av?-2048:+2048. ah: S20/S36→-5120; S215→
        // -6656; else `g.a.ax==51`→-2560; else -5120.
        if ((p.T == 1 && p.U == 0 && p.S != 19 && p.S != 23) ||
            (p.S == 215 && p.ah == 0)) {
            p.ag = when (p.S) {
                20, 215 -> if (p.av) 2048 else -2048
                25 -> if (p.av) 1024 else -1024
                36 -> if (p.av) -2048 else 2048
                24 -> 0
                else -> if (p.av) -2048 else 2048
            }
            p.ah = when {
                p.S == 215 -> -6656
                p.S == 20 || p.S == 36 -> -5120
                p.ga != null && p.ga!!.ax == 51 -> -2560
                else -> -5120
            }
        }
        // L1e3e/L1e6e (g.java:4578-4630, proven): y() (hitWall) gates ONLY
        // the wall-grab — `cv && aF!=0` (armed by the postTail cv producer
        // on the previous tick's dir/up input) or S215, plus side cell 20
        // → L1e94 grab (snap + aK marker + i(101), goto L353d). Grab not
        // met → L1f84 drift clamp for S25/15/19 under wall contact, then
        // L1fb3 runs av()+land either way (contact or not).
        if (p.hitWall()) {
            // L1e3e/L1e65 vs L1e8b (g.java:4578-4630, proven): the S215
            // corner check is INVERTED vs the cv&&aF path — the normal
            // path grabs the face you're FACING (av→aT, !av→aU) while
            // S215 grabs the face you're FLYING INTO (av→aU, !av→aT),
            // because the mount throws the player backward.
            val grabNormal = p.cv && p.aF != 0 &&
                (if (p.av) p.aT == 20 else p.aU == 20)
            val grab215 = p.S == 215 &&
                (if (p.av) p.aU == 20 else p.aT == 20)
            if ((grabNormal || grab215) && !p.ba) {
                wallGrabSnap(p, flipOn215 = true)
                return
            }
            if ((p.S == 25 || p.S == 15 || p.S == 19) && p.ag != 0) {
                p.ag = if (p.ag > 0) 512 else -512
            }
        }
        p.airWallResolve(world)                         // av()
        // L1fb3 (proven): `ah <= 0 → skip` — the land block runs
        // while FALLING (ah > 0). aR>=12 reaches L1ffb through every
        // path (the aQ/aQ!=23 detours collapse back into it), so the
        // effective condition is the L1fd9 list; d(0) — no ==4 arg
        // (the aR==4/aS==4 variant lives only in the fall arm L2136).
        if (p.ah > 0) {
            if (p.aR >= 12 || p.aS >= 12 || p.aR == 5 || p.aS == 5) {
                if (p.S == 215) { p.enterFall(0, world); p.av = !p.av }
                else p.land(world, false)
            }
        }
        // g.java:1600 (proven): i.f(this) caps the !y() free-air path.
        // The cv/aF-bound climb sites (:1614/:1645) sit in unported
        // branches — those climb states have their own arms.
        world.scrollWallClamp(p)
        if (p.animFinished() && p.S != 215 && p.S != 22) {
            p.enterFall(0, world)
            p.collideSides(world, true)
        }
        // L2046 (proven): `ah<0 && ah+aj>=0` — still rising this tick but
        // the next gravity step reaches the apex → swap to the apex pose.
        if (p.S == 22 && p.ah < 0 && p.ah + p.aj >= 0) p.setAnim(23)
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
                // g.java:5049-5125 (proven) — L2231/L2268 wall-grab check:
                // `cv && aF!=0 && !ba` + side cell 20 → L2298 grab (snap +
                // aK marker + i(101), no S215 flip here); grab conditions
                // failing fall through to L2361 — the ±512 drift clamp.
                if (p.cv && p.aF != 0 && !p.ba &&
                    (if (p.av) p.aT == 20 else p.aU == 20)) {
                    wallGrabSnap(p, flipOn215 = false)
                    return
                }
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
        // e()'s post-switch tail in bytecode order (g.javap.txt e()
        // offsets 14349-15124, proven).
        // 14349 (L2048): `o()` gate → `ao()` weapon cycle
        if (p.groundOrVehicle()) p.cycleEquip(world, pad)
        // 14361-14415 (L2051-L2057): `aA==0 → aA=1`; bit 2 set → cleared
        // and the `ap()` context dispatch skipped this tick; else
        // `z && !i.bn && !E → ap()`.
        if (p.aA == 0) p.aA = 1
        if (p.aA and 4 != 0) p.aA = p.aA and 4.inv()
        else if (p.z && !bn && !world.eFlag) p.contextDispatch(world, pad)
        // 14418 (L3852): head cell 7/9 drops the jump latch
        if (p.aO == 7 || p.aO == 9) p.cq = false
        // 14440 (L3868): the g.A autowalk latch — armed by ax10 script
        // zones (i.aV → g.A=1, g.B=facing, g.l=vel). Settles, faces g.B,
        // zeroes ag, forces the S148 scripted-walk state and RETURNS.
        if (p.gA) {
            p.eSettle(world)                 // E() — settle-sink to footing
            p.av = p.gB
            p.ag = 0
            p.setAnim(148)
            p.gA = false
            return
        }
        // 14474 (L388a): g.f() = isHolding — hands full kills the jump latch.
        if (p.isHolding()) p.cq = false
        // 14485 (L3895): standing on a crate (ax51) or moving platform
        // (ax66) drops the jump latch.
        p.ac?.let { if (it.ax == 51 || it.ax == 66) p.cq = false }
        // 14520: the shared jump tail is `cq && !E` (the ax10-S55 suppress
        // zone holds `g.E` so wall-run-family arms keep their own
        // transitions); the back-dash lives under the same gate.
        if (p.cq && !Entity.gE) {
            if (pad.v(Pad.M_ACTION_FAMILY)) {
                if (pad.v(Pad.M_UP) || pad.v(Pad.M_TAP_L) || pad.v(Pad.M_TAP_R)) {
                    if (pad.v(Pad.M_TAP_L)) p.av = true
                    else if (pad.v(Pad.M_TAP_R)) p.av = false
                    p.ah = 0
                    if (p.S == 79 || p.S == 32 || p.S == 199) {
                        // headroom probe at hitbox top-1 (proven literal)
                        if (p.e(world, ((p.W[0] + p.W[2]) / 2) / 20,
                                p.W[1] / 20 - 1) <= 12) p.setAnim(21)
                    } else {
                        // 14673-14745: riding a moving ax66 (S11/S12) with
                        // its `ac` partner on the facing side → no jump.
                        val a = p.standingOn
                        val ac = p.ac
                        if (a != null && a.ax == 66 && (a.S == 11 || a.S == 12) &&
                            ac != null && p.av == (ac.ak < p.ak)) return
                        if (p.aZ || a != null) {
                            p.setAnim(233)
                            // g.java:805 (proven): landing on an ax51 crate
                            // records the crate-top level `i.bq = al + 20`.
                            if (a?.ax == 51) Entity.entBq = p.al + 20
                        } else if (pad.v(Pad.M_UP)) {
                            p.setAnim(233)
                        } else {
                            p.setAnim(22)
                        }
                    }
                }
                // 14821: every press path ends here
                if (p.S != 233) p.ag = 0
            } else {
                // 14839-14890 back-dash: double-tap `x()` toward facing →
                // S25 while the `k.aA` alert latch is hot.
                if ((p.av && pad.x(Pad.M_RIGHT)) || (!p.av && pad.x(Pad.M_LEFT))) {
                    if (world.kAA > 0) { p.setAnim(25); p.ag = 0; p.ah = 0 }
                }
            }
        }
        // -- L3a2d-L3ad8 flag consumers (g.java:8197-8303, proven) -------
        // L3a2d: `cp && (aO==9||aP==9) && aQ==9` corner-9 gate skips the
        // whole consumer; otherwise `ct` fires the ledge-mount probes —
        // ak()/al() snap + i(60)/i(61) inside — and zeroes motion on a
        // mount (the probe side effects ARE the corner-stop).
        // 14893-14958: requires `cp`; skipped on a 9-corner.
        if (p.cp && !((p.aO == 9 || p.aP == 9) && p.aQ == 9) &&
            p.ct && (p.ledgeLipGrab(world) || p.ledgeHangGrab(world))) {
            p.ag = 0; p.ah = 0; p.aj = 0
        }
        // L3a71: `cu && v(33024)` (DOWN edge) → a(2560) fast-drop; a held
        // ax14 support drops its link via i.H().
        if (p.cu && pad.v(Pad.M_DOWN)) {
            p.flingAirborne(2560, world)
            if (p.ab?.ax == 14) p.dropHeld()
        }
        // L3a9d: `cv && (u|v)(16388|8|2)` → aF=1 — the grab-intent latch
        // the L1e94/L2298 wall-grab sites consume on later ticks. aF is
        // deliberately NOT in the e() head clear — it persists.
        if (p.cv && (pad.u(Pad.M_UP) || pad.v(Pad.M_UP) ||
                     pad.u(Pad.M_TAP_L) || pad.v(Pad.M_TAP_L) ||
                     pad.u(Pad.M_TAP_R) || pad.v(Pad.M_TAP_R))) p.aF = 1
        // L3ad8: `cw && aO==5` → i(280) ceiling grab — zero all motion and
        // snap `al` onto the ceiling grid row. `aO` here is av()'s
        // shifted (al-20) probe — the cell ABOVE the head — so the grab
        // fires when a rise reaches under a '5' lip, not inside it.
        if (p.cw && p.aO == 5) {
            p.setAnim(280)
            p.aj = 0; p.ai = 0; p.ah = 0; p.ag = 0
            p.al = (p.W[1] / 20) * 20 + 10
        }
    }

    /**
     * `L1e94`/`L2298` (g.java:4633-4730 / :5125-5210, proven): the shared
     * wall-grab body — consumes the `aF` intent latch, zeroes motion
     * (`ag=ah=ai=aj=0`), flips `av` when entered from S215 (air family
     * only — the fall site has no S215 check), snaps `ak` onto the
     * wall-edge grid (`av: ((W[0]+20)/20)*20+1`; `!av:`
     * `((W[2]-20)/20)*20+19`), spawns the ax8/clip5/S17/az201 marker via
     * the `aK` child factory (`P=512`, t() before P per the original),
     * `k.b()`-inserts it, then `i(101)` — the S101 cling state.
     */
    private fun wallGrabSnap(p: Entity, flipOn215: Boolean) {
        p.aF = 0
        p.ag = 0; p.ah = 0; p.ai = 0; p.aj = 0
        if (flipOn215 && p.S == 215) p.av = !p.av
        p.ak = if (p.av) (p.W[0] + 20) / 20 * 20 + 1
               else (p.W[2] - 20) / 20 * 20 + 19
        val aK = p.spawnChildFx(world, 8, 5, 17, 201)   // a(8,5,17,201)
        aK.av = p.av
        aK.N = p.ak shl 8; aK.O = p.al shl 8
        aK.ak = p.ak; aK.al = p.al
        aK.aj = 0; aK.ai = 0; aK.ah = 0; aK.ag = 0
        aK.refreshBoxes()                                // aK.t()
        aK.P = 512
        world.queueInsert(aK)                            // k.b(aK)
        p.setAnim(101)
    }

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

    /**
     * Combo arm (S67/68/69 + S112..115, g.javap.txt e() 10217-10616):
     * `aj()` runs the cj/ck matchers every tick — a 65568 tap in
     * `S==row.anim` queues `R = next anim` (before `row.minFrame`) and a
     * queued `R` re-reads the window `cl = T >= minFrame`; `cl || r()`
     * fires `i(R)`, or `l()` when nothing is queued. A tap in S67/68
     * on a weakened lock (`i.aN`: ax11, `Z[0]==2`, `aB <= bw[k.au]`) picks
     * the assassination finisher `R = |j.j.nextInt()| % 2 ? 184 : 183`.
     */
    private fun comboArm(p: Entity, pad: Pad): Boolean {
        // g.javap.txt e() 10217-10616 (proven; slice 369 F5): attack step
        // → wall-stop → footing loss, then the combo logic.
        attackStep(p)                                          // ay()
        if (p.ag != 0 && p.forwardWall()) p.ag = 0            // y()
        // 10240-10258: the footing-loss `a(0)` — `invokevirtual #213
        // g.a:(I)V`, the masked S43 fall (`i.a(43,32)`) — then `return`
        // (slice 365): no L353d tail, no post-tail.
        if (!p.aZ && p.standingOn == null) { p.flingAirborne(0, world); return false }
        p.z = false                                            // 10259
        // 10263-10417: tap 65568 in S67/68 with a weakened lock (ax11,
        // Z0==2, aB<=bw[k.au], a==null) → `cl=0`, R = |rand|%2 ? 184 :
        // 183. No `aB > 0` term — a dead lock is handled by the tail.
        val t = world.lockTarget
        if (pad.v(Pad.M_CONTEXT) && (p.S == 67 || p.S == 68) &&
            p.standingOn == null &&
            t != null && t.ax == 11 && t.Z[0] == 2 && t.aB <= BW) {
            p.cl = false
            if (p.R == -1) p.R = if ((rng?.nextInt() ?: 0) % 2 != 0) 184 else 183
        } else {
            // 10420-10433: every other tick runs `aj()` and `k.E.K()` —
            // with or without a tap: a queued `R` re-reads the window
            // `cl = T >= minFrame` each tick. `aj()` (g.javap.txt aj()
            // 0-24) tries `ck` only when `cj` matched nothing.
            if (!comboMatch(p, CJ, true, pad)) comboMatch(p, CK, false, pad)
            world.kE?.heldRelease(p)                           // k.E.K()
        }
        // 10436-10598: cl||r() tail — crate interrupt, kill-cam freeze,
        // dead-target guard, then R→anim or `l()`.
        if (p.cl || p.animFinished()) {
            // 10450-10485: R==112 on an ax51 crate waits for `r()`
            // (`!r() → goto 13629`, skipping the T==2 sfx too), then drops R.
            if (p.R == 112 && p.standingOn?.ax == 51) {
                if (!p.animFinished()) return true             // goto 13629
                p.R = -1
            }
            p.cl = false
            val t2 = world.lockTarget
            if ((t2 != null && t2.aB > 0 && p.R == 183) || p.R == 184 || p.R == 205) {
                p.lockInput(world); p.ag = 0; p.ah = 0         // k.o()
            } else if (t2 != null && t2.aB <= 0) {
                p.R = -1
            }
            if (p.R != -1) { p.setAnim(p.R); p.R = -1 }
            else l(p, pad)                                     // 10598 l()
        }
        // 10603-10613: `T==2 → k.A(10)` on every tick that gets here.
        if (p.T == 2) world.sfx(10)
        return true                                            // goto 13629
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
     * `az()` (g.javap.txt az() 0-1819, proven; slice 388) — per-tick
     * interact maintenance + the `k.bd[]` scan that produces the three
     * links:
     *   `g`   = interact target (NPC/prop in front, <440 octagonal px),
     *   `ci`  = carry target (NPC-kind candidate at the same spot),
     *   `i.at`= mount/assassination link (ax72 arm; also written at
     *           i.java:6007 by the ax11 grab).
     * Hidden (`aA&8`) or S250 → drop `g`/`at`, bail. Hostage-carry states
     * (S270/271) hold a BOUND `g`; S267/268/291 drop `g` for a fresh
     * rebind. The scan walks `k.bd` — the last paint's draw list.
     */
    fun interactScan(p: Entity) {
        // -- @0-38: hidden or S250 → clear and bail ---------------------------
        if (p.aA and 8 != 0 || p.S == 250) { p.g = null; Entity.at = null; return }
        // -- @39-65: carry anims keep a bound `g` (an unbound one scans on) ----
        if (p.g != null && (p.S == 270 || p.S == 271)) return
        // -- @66-101: S268, S267 (`k.aS.S` — the player's own) and S291
        //    rebind from scratch ---------------------------------------------
        if (p.S == 268 || p.S == 267 || p.S == 291) p.g = null
        // -- @102-182: drop a stale/dead `g` — the whole block skips an ax4
        //    (no aB check, no distance, no |Δal|) ------------------------------
        p.g?.let { g ->
            if (g.ax != 4 && (g.aB <= 0 ||
                    p.h(p.ak - g.ak, p.al - g.al) > 440 ||
                    Math.abs(p.al - g.al) >= 60)) p.g = null
        }
        // -- @183-243: `ci` cleared unconditionally each call (the keep-branch
        //    needs `av` both true and false — dead bytecode). Rebinding
        //    happens in the scan, whose facing + dist<440 gates filter it. ----
        p.ci = null
        // -- @244-319: an ax11 `Z[19]==1` target must stay in front — dx == 0
        //    keeps it (`!av && dx < 0` or `av && dx > 0` drops) ----------------
        p.g?.let { g ->
            if (g.ax == 11 && g.Z[19] == 1) {
                val dx = g.ak - p.ak
                if ((!p.av && dx < 0) || (p.av && dx > 0)) p.g = null
            }
        }
        // -- @320-469: `i.at` — drop when dead, far, behind, or off-level; the
        //    struggle/mount states S277/293/298 keep it ------------------------
        Entity.at?.let { a ->
            val drop = (a.ax == 11 && a.deadRelease()) ||     // P() first (releases `ae`)
                p.h(p.ak - a.ak, p.al - a.al) > 440 ||
                (p.ak - a.ak < 0 && p.av) || (p.ak - a.ak > 0 && !p.av) ||
                p.W[3] < a.W[1]
            if (drop && p.S != 277 && p.S != 293 && p.S != 298) Entity.at = null
        }
        // -- @470-559: dead NPCs (P() releases their `ae`) -----------------------
        p.g?.let { g ->
            if ((g.ax == 11 || g.ax == 17 || g.ax == 73 || g.ax == 9) &&
                g.deadRelease()) p.g = null
        }
        // -- @560-627: at the end of the S295/S303 anim only a faced ax4 S30 stays
        p.g?.let { g ->
            if ((p.S == 303 || p.S == 295) && p.animFinished() &&
                !(g.ax == 4 && g.S == 30 && p.inFrontOf(g))) p.g = null
        }
        // -- @628-679: ax4 off S30 and ax58 never stay bound ---------------------
        p.g?.let { g -> if (g.ax == 4 && g.S != 30) p.g = null }
        p.g?.let { g -> if (g.ax == 58) p.g = null }
        // -- @680-705: all bound + no pending mount bit → done (dead in the
        //    original: `ci` was just cleared) -----------------------------------
        if (p.g != null && (Entity.at != null || p.gJ and 4 == 0) && p.ci != null) return
        // -- @706-1819: scan k.bd[] -----------------------------------------------
        var best = 440                            // r1 — narrowed at @1246
        for (e in world.drawn) {
            if (e.P and 32 != 0) continue         // held
            // @762-904: dead NPCs (P() runs — it releases `ae`) and ax4 off S30
            if (e.ax == 11 && e.deadRelease()) continue
            if (e.ax == 17 && e.deadRelease()) continue
            if (e.ax == 73 && e.deadRelease()) continue
            if (e.ax == 23 && e.deadRelease()) continue
            if (e.ax == 9 && e.deadRelease()) continue
            if (e.ax == 4 && e.S != 30) continue
            val npcKind = e.ax == 11 || e.ax == 17 || e.ax == 23 ||
                          e.ax == 73 || e.ax == 29 || e.ax == 9
            val pathA = npcKind || (e.ax == 4 && e.S == 30) || e.ax == 58
            if (!pathA) {
                // -- @1555: ax72 mount arm only --------------------------------
                if (e.ax != 72) continue
                // g.az() @1591: `bd[i].v()` — the one v() port
                if (p.gJ and 4 == 0 || !p.mountableState() || !e.inPlayV(world) ||
                    e.Z[0] == 3) continue
                if (p.av && e.ak - p.ak >= 0) continue
                if (!p.av && e.ak - p.ak <= 0) continue
                if (p.h(p.ak - e.ak, p.al - e.al) >= 440) continue
                // g.java:5673-5680 — the `p.W[1] >= e.W[3]` gate is
                // Z[0]==1-ONLY in the original: for other configs (uid70's
                // Z0=0 launch counterweight) the player only needs his
                // bottom reaching the box top (`p.W[3] >= e.W[1]`).
                if (e.Z[0] == 1) {
                    if (p.h(p.ak - e.ak, p.al - e.al) >= e.Z[3]) continue
                    if (p.W[1] < e.W[3]) continue
                }
                if (p.W[3] < e.W[1]) continue
                if (p.losBlocked(e, world)) continue          // @1786 `e(bd[i])`
                if (Entity.at != null) continue
                Entity.at = e
                return
            }
            // -- @1023 path: dead-check, I==8 gate for ax4/58, facing, dist ------
            if (e.aB <= 0 && e.ax != 4 && e.ax != 58) continue
            if (p.gI != 8 && (e.ax == 4 || e.ax == 58)) continue
            // @1092-1155: facing gate — av=false needs dx>0; av=true dx<0;
            // S∈{268,291} bypass.
            val dxe = e.ak - p.ak
            val inFront = (p.av && dxe < 0) || (!p.av && dxe > 0)
            if (!inFront && p.S != 268 && p.S != 291) continue
            val d = p.h(p.ak - e.ak, p.al - e.al)
            if (d >= best) continue                                 // @1188
            // @1191-1231: the rebind states take the nearest facing-or-not
            // candidate outright — before `best` narrows and with no LOS test
            if (p.S == 268 || p.S == 267 || p.S == 291) { p.g = e; continue }
            // @1234: `i.e(bd[i])` — a solid cell on the line between the
            // two centres hides the candidate (the ax72 arm has the same test)
            if (p.losBlocked(e, world)) continue
            best = d                                                // @1246-1278
            p.g = null                                              // @1283 boundary
            if (npcKind) {
                // @1299-1383 (proven): the aA gate covers only
                // {11,17,23,73} — an idle/attack-engaged victim without
                // the i() offer skips; ax9/29 fall to @1386 directly.
                if ((e.ax == 11 || e.ax == 17 || e.ax == 23 || e.ax == 73) &&
                    !p.interactEligible(e) && (e.aA == 0 || e.aA == 2)) continue
                // @1386 (proven): the offer bypasses |Δal|; otherwise the
                // victim must sit within ±20px vertically.
                if (!p.interactEligible(e) && Math.abs(p.al - e.al) > 20) continue
                if (p.g == null) p.g = e                            // @1419
                if (p.ci == null) p.ci = e                          // @1433-1522
            } else {
                // @1386-1430: i(e) or |dy|<=20 → g-bind
                if (!p.interactEligible(e) && Math.abs(p.al - e.al) > 20) continue
                if (p.g == null) p.g = e
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
    // The case→label table (simple/g.java:5878-5958, proven) routes every
    // state: S0→L141; S1/9/12/27→L130; S2/24→L325; S3→L137; S4/5/17/18→
    // L146; S20→L331; S21→L55; S22→L76; S23→L97; S25→L118; S26→L121;
    // S28→L124; S29→L127; S30-33→L139; all others→L346. The L141/L146
    // glide tail is extracted as `glideTail` and shared by the L130/L137/
    // L139/L325 fall-throughs exactly as the labels do.
    private fun flightTick(p: Entity, pad: Pad) {
        val zFlags = booleanArrayOf(true, true)         // r6 = zFlags[0], r7 = zFlags[1]
        world.kAI++
        // `i.B()` (g.java:13907, proven): canyon-wall collide — the call's
        // return value is dead in n(); it runs for its side effects only.
        p.canyonCollide(world)
        // (Slice 411: `k.B()` — the mission BGM start — is NOT called from n(): its three
        // callers in the bytes are `k.l(int)`, `k.Q()` and `k.a(boolean)` (k.javap.txt:9011,
        // 19042, 25068), all of which the port reaches through `missionInit()`. The port
        // requested the track again on every flight tick.)
        if (world.iBe) {
            world.kX = 0
            // g.n() @17-46: `if (r() || !v()) k.l(12); return` — the full
            // v() (@35), `u()`/`au>i` guard included (slice 371).
            if (p.animFinished() || !p.inPlayV(world)) { world.stateL(12); return }
            return
        }
        // `i.bh--` (n():5615) runs in tick() for both modes — not repeated.
        // L14-L17 meter drain (g.java:5851-5856, proven): kAG counts the
        // ticks between kAE points (drains ~1/6t whenever kAH<0).
        if (world.kAH < 0) world.kAG--
        if (world.kAG <= 0) { world.kAG = 6; world.kAE-- }
        // L23-L38 (g.java:6317-6333, proven): low-meter conveyor halving —
        // source gates BOTH `aE>25` and `aE>=25` to skip, so the arm fires
        // only at aE<25 (this was `<=` — off-by-one fixed).
        if (world.kAE < 0) world.kAE = 0
        else if (world.kAE < 25 && world.kAF == 0 && world.kAH < 0 && (world.iAJ == 0 || world.kW != 0)) {
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

        // case→label dispatch (simple/g.java:5878-5958, proven): the L141/
        // L146 glide tail is shared — S0 enters at L141, S4/5/17/18 at
        // L146, L130 (S1/9/12/27) / L137 (S3) / L139 (S30-33) / L325-
        // L328 (S2/24) all funnel through it.
        when (p.S) {
            0, 4, 5, 17, 18 -> glideTail(p, pad, zFlags)        // →L141/L146
            1, 9, 12, 27 -> {                                   // L982
                world.kAw = 20
                if (p.animFinished()) {
                    if (world.iBB) { world.iBB = false; world.iBG = -1; p.az = 202 }
                    if (world.kAw == 20) world.kAw = 0          // aC()
                    // @1022 `i(4)` then `goto 2262` (g.javap.txt n(), raw bytes, proven; slice
                    // 411): the friction block only — no `av = 0` and no glide tail (steering
                    // input) on the recovery tick. The port ran both.
                    p.setAnim(4)
                }
            }
            2, 24 -> {                                          // L325-L328
                // g.n() @2117-2160 (bytecode, proven): `ag>>=1; ah>>=1;
                // r6=r7=false; if (r() || !v()) k.l(12)` then → @2262
                // (L346). `!v()` falls through to the SAME `k.l(12)` at
                // @2155 — the simple decompile wires that fall-through
                // into L137 (the S3 arm), which the port had copied
                // (slice 371). v() is the full port (@2149).
                p.ag = p.ag shr 1; p.ah = p.ah shr 1
                zFlags[0] = false; zFlags[1] = false
                if (p.animFinished() || !p.inPlayV(world)) world.stateL(12)
                // v() → @2262 (L346): arm skipped verbatim
            }
            3 -> {                                              // L1028
                // @1032-1043: `r() → i(4); goto 2262` (friction only); otherwise `av = 0` and
                // the glide tail at L1048 (slice 411: the port ran the tail after `i(4)` too).
                if (p.animFinished()) p.setAnim(4)
                else { p.av = false; glideTail(p, pad, zFlags) }
            }
            20 -> {                                             // L331
                if (world.iBk) {
                    if (p.T == 5) repeat(5) { flap(p, true) }
                    world.iBB = true; world.iBE = 999
                    if (p.T <= 10) { world.iBC = true; world.iBD = true }
                    else { world.iBC = false; world.iBD = false; world.iBG = 100 }
                }
                if (p.animFinished()) { world.iBB = false; world.iBG = -1; world.iBk = false; p.setAnim(4) }
            }
            21 -> {                                             // L55
                world.kAw = 20
                if (p.animFinished()) { world.iBB = true; world.iBC = true; world.iBD = true; world.iBE = 999; p.setAnim(22) }
                if (bankSteer(p, pad)) zFlags[0] = false          // r6 = false on steer
            }
            22 -> {                                             // L76
                world.kAw = 20
                if (p.animFinished()) { world.iBB = true; world.iBC = false; world.iBD = false; world.iBE = 999; world.iBG = 100; p.setAnim(23) }
                if (bankSteer(p, pad)) zFlags[0] = false
            }
            23 -> {                                             // L97
                world.kAw = 20
                if (p.animFinished()) { world.iBB = true; world.iBC = false; world.iBD = false; world.iBE = 999; p.setAnim(25) }
                if (bankSteer(p, pad)) zFlags[0] = false
            }
            25 -> {                                             // L118
                world.kAw = 20
                if (p.animFinished()) { world.iBB = false; world.iBG = -1; p.setAnim(4) }
            }
            26 -> {                                             // L121
                world.kAw = 20
                if (p.animFinished()) p.T = (p.clip?.frameCount(p.S) ?: 0) - 2
            }
            28 -> { world.kAw = 20; if (p.animFinished()) p.setAnim(29) }   // L124
            29 -> { world.kAw = 20; if (p.animFinished()) p.setAnim(26) }   // L127
            30, 31, 32, 33 -> {                                 // L139
                p.av = false
                glideTail(p, pad, zFlags)
            }
            else -> {}
        }
        if (zFlags[0]) {
            if (p.ag > 768) p.ag -= 768
            else if (p.ag < -768) p.ag += 768
            else p.ag = 0
        }
        if (zFlags[1]) {
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

    /** `n()` shared glide tail L141→L146 (simple/g.java:6035-6080,
     *  proven): L141 = `S==0 && r() → T=aa.b(S)-2`; L146 = the
     *  `i.bi||g.E` scripted wind arm (L150: `ah = iAH ? kY*iAI : kY`,
     *  r7=false when `kQ >= 230`) else the input block — `aC()`,
     *  `z(28)`, `S18→20`/`S17→4`, `i.bk`-gated `e(false)` flap, the
     *  four `u()` ±768 steering arms (bank anims via `bD`, r6/r7
     *  clears), the `bB==0 && bC==0 && r() && r8 → i(4)` bank-exit
     *  (L253), and the `aq/ar` wisp-marker chase. `z[0]`/`z[1]` are
     *  caller's r6/r7 friction-suppress flags. */
    private fun glideTail(p: Entity, pad: Pad, z: BooleanArray) {
        if (p.S == 0 && p.animFinished()) p.T = (p.clip?.frameCount(0) ?: 0) - 2
        // L146 gate: `i.bi || g.E →` scripted arm (wind `ah = kY*iAI`
        // while the countdown `i.b(r3)` owns the sim); else `aC()` +
        // input. `i.bi` is the "sequence owns player" latch (S10 climb /
        // ax64 grab) — scripted runs only while it or `g.E` is set.
        if (world.iBi || Entity.gE) {
            if (world.kQ >= 230) {
                z[1] = false
                p.ah = if (world.iAH) world.kY * world.iAI else world.kY
            }
        } else {
            if (world.kAw == 20) world.kAw = 0            // aC()
            if (world.iAH && world.kQ >= 230) { p.ah = world.kY shl 1; z[1] = false }
            if (!world.iBB && pad.v(1)) world.sfx(28)     // k.A(28) = z(28)
            if (p.S == 18) { p.av = p.ak > world.kO + 200; p.setAnim(20); world.iBk = true }
            else if (p.S == 17) p.setAnim(4)
            val z4 = p.S != 3 && p.S != 0 && p.S != 18 && p.S != 17 && p.S != 20
            if (!world.iBk && p.Q != 18 && world.kAI >= 10 && z4) { world.kAI = 0; flap(p, false) }
            if (pad.u(4112)) {
                if (p.ag > -2048) p.ag -= 768
                if (p.ag < -2048) p.ag = -2048
                z[0] = false
                if (z4) p.setAnim(if (world.kBD >= 15) 30 else 33)
                p.av = false
            }
            if (pad.u(8256)) {
                if (p.ag < 2048) p.ag += 768
                if (p.ag > 2048) p.ag = 2048
                z[0] = false
                if (z4) p.setAnim(if (world.kBD >= 15) 31 else 32)
                p.av = false                            // verbatim quirk — right-bank also faces left
            }
            if (pad.u(16388) && world.kQ > 117) {
                if (p.ah > -2048 + world.kY) p.ah -= 768
                if (p.ah < -2048 + world.kY) p.ah = -2048 + world.kY
                z[1] = false; p.av = false
                if (z4) p.setAnim(4)
            }
            if (pad.u(33024) && world.kQ < 230) {
                if (p.ah < 2048 + world.kY) p.ah += 768
                if (p.ah > 2048 + world.kY) p.ah = 2048 + world.kY
                z[1] = false; p.av = false
                if (z4) p.setAnim(5)
            }
            if (world.kBB == 0 && world.kBC == 0 && p.animFinished() && z4) { p.av = false; p.setAnim(4) }
            if (p.aq != -1 && p.ar != -1) {
                p.ah = 0; p.ag = 0; z[1] = false; z[0] = false
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

    /** `n()`'s shared steering block (g.java:5900-5916, proven) — the
     *  S21/S22/S23 cases carry the same `u(4112)`/`u(8256)` ±768 arms.
     *  Returns true when a steer consumed input (callers then write
     *  `r6 = false`). */
    private fun bankSteer(p: Entity, pad: Pad): Boolean {
        var steered = false
        if (pad.u(4112)) {
            if (p.ag > -2048) p.ag -= 768
            if (p.ag < -2048) p.ag = -2048
            p.av = false; steered = true
        }
        if (pad.u(8256)) {
            if (p.ag < 2048) p.ag += 768
            if (p.ag > 2048) p.ag = 2048
            p.av = false; steered = true
        }
        return steered
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
        /** `i.bw[k.au]` (i.java:171, proven): `{80,80,80}` — the weaken
         *  threshold is 80 at every difficulty. */
        private const val BW = 80
    }
}
