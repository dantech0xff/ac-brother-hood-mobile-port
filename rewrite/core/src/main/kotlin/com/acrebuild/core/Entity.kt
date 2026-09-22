package com.acrebuild.core

/**
 * Port of the `i` entity skeleton: position, facing, clip binding, and the
 * `S/T/U` animation counters with the exact `i()`/`s()`/`r()`/`q()`
 * semantics from `i.java:240-330`.
 *
 * Field names keep the original single-letter names so FSM ports can be
 * transcribed literally:
 * - `ak`/`al` = integer pixel anchor; `N`/`O` = the same in 8.8 fixed.
 * - `ag`/`ah` = velocity (8.8); `ai`/`aj` = per-tick accel (8.8).
 * - `S` = current anim (= FSM state), `T` = frame, `U` = tick-in-frame,
 *   `Q` = previous anim, `P` = flags (bit0 = `av` facing, bit6 = anim-hold).
 * - `V` = playback delay counter; `a` = anim elapsed counter.
 *
 * Slice-2 additions (all `proven` transcriptions of the simple decompile
 * unless noted): hitbox `W`/attackbox `X` (`t()`, i.java `public final
 * int[] t()`), the per-tick cell probe `x()` (i.java `public final int x()`),
 * the wall pass `a(boolean)`, forward-wall check `y()`, air wall-resolve
 * `av()` (g.java `void av()`), cell probe `e(cx,cy)` = `k.g`, landing
 * `d(boolean)` (g.java `private void d(boolean)`), and the fall-entry
 * `a(0)`/`a(int,int)` (i.java `final void a(int,int)` + g.java `void a(int)`).
 */
open class Entity(val ax: Int, var clip: Clip?) {

    var aw: Int = 0                  // uid
    var ak: Int = 0                  // anchor x, px
    var al: Int = 0                  // anchor y, px
    var homeX: Int = 0               // record home (re-materialize target)
    var homeY: Int = 0
    var palette = 0                  // aH — clip palette slot (b.l(int))
    var N: Int = 0                   // 8.8 x
    var O: Int = 0                   // 8.8 y
    var ag: Int = 0                  // vx
    var ah: Int = 0                  // vy
    var ai: Int = 0                  // axel x
    var aj: Int = 0                  // axel y
    var S: Int = 0                   // anim/state index
    var T: Int = 0                   // frame
    var U: Int = 0                   // tick-in-frame
    var Q: Int = 0                   // previous anim
    var P: Int = 0                   // flag word
    var av: Boolean = false          // facing (P bit0); true = faces/mirrors left
    var V: Int = 0                   // playback delay
    var a: Int = 0                   // anim elapsed ticks
    val Z: IntArray = IntArray(22)   // per-type params (record fields[7+])

    // -- probe results, filled by x()/a()/t() each tick --------------------
    var aO = 0; var aP = 0; var aQ = 0; var aR = 0; var aS = 0
    var aT = 0; var aU = 0; var aV = 0; var aW = 0; var aX = 0; var aY = 0
    var aZ = false                   // standing flag (aR != 18 when probing feet)
    var bd = false; var v = true     // x() status flags
    var bb = false; var bc = false   // wall flags left/right (a(boolean))
    var ba = false                   // unused third flag, kept for parity
    val W = IntArray(4)              // hitbox [x,y,w,h] in world px (t())
    val X = IntArray(4)              // attackbox (t())
    val Y = IntArray(4)              // context-bounds rect (t() — the
                                     // stored per-obj bounds row via
                                     // aa.d(av,i&192); path unmined →
                                     // stays zeros; aX() slice fills it)
    var tc = 0; var uc = 0           // hitbox center px (a() writes t/u)
    var co = 0                       // consecutive-run-tick counter (S12 arm)
    var aC = 0                       // generic countdown (patrol leg timer etc.)
    var R = -1                       // queued combo/finisher anim (g.R)
    var cl = false                   // combo window flag (g.cl)
    var gI = 1                       // weapon selector (g.I; 1=sword proven)
    var x1 = 90                      // g.x[1] sync/health meter — init 90
                                     // proven (g.e(90) at entity init L195)
    var aF = 0
    var k = false                    // NPC patrol-active flag
    var cp = true; var cq = true; var ct = true; var cw = true; var cv = true
    var zz = true
    var aA = 0                       // alert level (NPC) / turn-block (player)
    var aB = 0                       // hp-ish stat (az = max)
    var az = 0
    var standingOn: Entity? = null   // `a` — entity stood upon (null in slice 2)
    var platform: Entity? = null     // `s` — linked platform/rope (null here)
    var ac: Entity? = null           // `ac` — resolved link target (ax44 door
                                     // slaves resolve Z[5] via `a(k.q(Z[5]))`)
    var l = 0                        // attack level fed to a(op,l,..)
    var hitsTaken = 0                // slice-3 instrumentation (inferred counter)
    var gt = 0                       // g.t iframe timer: 10 after drain, 5 after
                                     // volume hit, 100 after teleport; --/tick
    var bh = 0                       // i.bh hit-flash counter (visual pending)
    var gy = 0                       // g.y — apex marker: al at S43/148/0 entry
                                     // (i.java:271, proven; ax==0 only)
    // -- ax10 trigger fields (init arm L96, i.java:2882; L111 field map) --
    var aE = 0                       // r8[4]
    var oId = -1                     // i.o (r8[12] link id; `o` clashes
                                     // with fixed-point `O` on the JVM)
    var pv = 0                       // i.p (r8[13]; `p` clashes with `P`)
    var aG = 0                     // r8[14] slope/effect anchor
    var ay = 0                     // r8[15]
    // player-singleton fields published into by ax10 zones (g class statics)
    var gB = false                 // g.B  — S33 effect-zone flag (i.java:12896)
    var gL = 0                     // g.l  — S33 slope factor (i.java:12898)
    var gA = false                 // g.A  — S33 zone active (i.java:12900)
    var gn = 0                     // g.n  — S36 context zone center-x
    var go = 0                     // g.o  — S36 context zone bottom-y
    var gk = -1                    // g.k  — S36 context param (i.java:12910)
    var gd: Entity? = null         // g.d  — S36 owning trigger (i.java:12912)
    var gD = false                 // g.D  — S53 gate (g.java:2054; producer
                                   // arm unported — stays false this slice)
    // -- ax4 destructible fields (init arm L161, i.java:3132) --
    var aD = 0                     // r8[4]
    var nl = 0                     // i.n (r8[8]; `n` clashes with 8.8 `N`
                                   // on the JVM, like o/O)
    var m = 0                      // r8[9] — burst count for the S6/8 arm
    var i = 1                      // i.i — ctor default 1 (i.java:820);
                                   // L166 sets 2 on non-S7 destructibles
    // -- ax74 wisp fields (bN S1, i.java:21341+; spawned via a(74,54,1,az)) --
    var j = 0                      // flight-radius counter (j += 15/tick)
    var aq = 0                     // launch anchor x px (i.aq)
    var ar = 0                     // launch anchor y px (i.ar)
    var af: Entity? = null         // owner — af.aG!=0 → k.A(15) sfx on land
    var ga: Entity? = null         // g.a — grapple/ride link (a() push guard,
                                   // i.java:922/937; producer arms unported)
    // -- ax67 prop fields (init L347, i.java:3530; tick bB i.java:17584) --
    var bZ = 0                     // i.bZ lifecycle counter (aX L23 linked
                                   // arm — used by the ax14/pickup path)
    var ad: Entity? = null         // i.ad child link (bd[] scan, p() cascade)
    var au = 10                    // i.au screen-distance score (u() rewrites
                                   // it per v() call; ctor 10, i.java:819)
    var ae: Entity? = null         // i.ae player's spawned ax14 pickup ref
    var gb: Entity? = null         // g.b grabbed-prop ref (op40 arm)
    var ge: Entity? = null         // g.e hide-spot owner (bB S12 arm)
    var gg: Entity? = null         // g.g hide-spot busy guard
    var ci: Entity? = null         // g.ci carried prop (f() holding check)
    var g: Entity? = null          // g.g interact target (az() scan)
    var gJ = 0                      // g.J action-request bits (g.g(mask));
    var ab: Entity? = null        // i.ab link — mount gate in g.h consume
    var c: Entity? = null         // i.c carry link (released by p())
    // -- g.c(i) grab-lunge state (g.java:4115) -----------------------------
    var F: Entity? = null         // g.F — grab/lunge target (c() sets, as() binds)
    var cE = 0                    // c() — lunge length = the anim's total frames
    var cF = 0                    // c() — impulse (default 5120; Z[1]/298 override)
    var cC = 0                    // c() — per-tick x step (cz/cE)
    var cD = 0                    // c() — per-tick y step (signed cA/cE)
    var cz = 0                    // c() — target.x - X[0] (clamped ≠0)
    var cA = 0                    // c() — target W-center-y - X[1]
    var cB = 0                    // c() — h(cz,cA) straight-line distance
    var cx = 0                    // c() — yaw rate ((Z[2] or 8) · j.m/360)
    var cy = 0                    // c() — launch angle (j.b two-arg)
    var cG = false                // c() — facing snapshot at grab
    var cJ = 0                    // c() — X[0] snapshot
    var cK = 0                    // c() — X[1] snapshot
    var cH = 0                    // c() — X[0] snapshot (target x anchor)
    var cI = 0                    // c() — X[1] snapshot
    var cM = 0                    // c() — ax72 Z[0]==1 clears it (L58)
    var cL = 0                    // mount-on counter (as() clears; au() oscillates)
    var z = false                 // g.z — cleared on grab (c() callers)
    /** `i.H()` (i.java:4847, proven): release the ab-link entity and drop
     *  the reference — the mount consume path (g.h calls i.at.H()). */
    fun consumeH() {
        ab?.releaseCascade()
        ab = null
    }

    // -- clip-74 hand indicator (i.c/d/T/U/V + i.o statics) ---------------

    /** `i.T()` (i.java:9879, proven): the ae indicator is a clip-74 hand
     *  at anim S∈{0,1}. clip74 unconverted → false until converted. */
    fun indicatorIsHand(w: LevelCellSource): Boolean {
        val e = ae ?: return false
        return e.clip === w.clipFor(74) && (e.S == 0 || e.S == 1)
    }

    /** `i.U()` (i.java:9895, proven): if T() — release the ae indicator
     *  and clear the static indicator point L/M. */
    fun dropIndicator(w: LevelCellSource) {
        if (!indicatorIsHand(w)) return
        releaseAe()
        L = -1; M = -1
    }

    /** `i.V()` (i.java:9903, proven): ae exists and sits ≤70px (k.h) from
     *  the touch point k.H/k.I (view-space) — `ae.ak-k.O`/`ae.al-k.P`. */
    fun indicatorNearTouch(w: LevelCellSource): Boolean {
        val e = ae ?: return false
        return w.touchNearView(e, 70)
    }

    /** `i.c(x,y)` (i.java:9840, proven): spawn the clip-74 hand indicator
     *  (ax14, anim 0, az=302, au=0) at (x,y) and pin the static point
     *  L/M via `o(x,y)` (i.java:9825). */
    fun spawnHand(w: LevelCellSource, x: Int, y: Int) {
        if (ae == null) ae = Entity(14, w.clipFor(74)).also {
            it.setAnim(0); it.az = 302; it.au = 0
        }
        ae?.let { it.ak = x; it.al = y; it.refreshBoxes() }
        L = x; M = y
    }

    /** `i.d(x,y)` (i.java:9856, proven): move the hand indicator; T()
     *  hands re-pin L/M; anim 1 while within 70px of (k.J,k.K) — the
     *  held-point; the port reuses the single touch point (inferred —
     *  J2ME tracks down vs current separately). */
    fun moveHand(w: LevelCellSource, x: Int, y: Int) {
        val e = ae ?: return
        e.ak = x; e.al = y
        if (indicatorIsHand(w)) { L = x; M = y }
        e.setAnim(if (w.touchNearView(e, 70)) 1 else 0)
    }

    // -- g.c(i) the grab lunge --------------------------------------------

    /**
     * `g.c(i r8)` (g.java:4115, proven) — zero velocity, pick the lunge
     *  anim (attack state → 292; S==298 keeps anim; else by the 8.8
     *  rise:run ratio r04/r03 → 272/273/274/275 arcs), then compute the
     *  per-tick step (cC,cD) that carries the player onto the target
     *  over the anim's cE frames. `F = r8` links the target for the
     *  mount/grab consumer arms (g.java:4303+, unported).
     *  `cy = j.b(-cz, cA)` keeps the original's arg order verbatim
     *  (inferred sign convention — resolved when the throw arm lands).
     */
    fun grabLunge(t: Entity, w: LevelCellSource) {
        refreshBoxes()
        bq = 0                       // i.bq static — cleared on grab
        aj = 0; ai = 0; ah = 0; ag = 0
        val r0 = (W[0] + W[2]) shr 1
        val r02 = (W[1] + W[3]) shr 1
        var r11 = t.ak; var r12 = t.al
        if (t.ax == 11 || t.ax == 17) {      // L5→L6 victim anchor
            r11 = (t.W[0] + t.W[2]) shr 1
            r12 = t.W[3] - 45
        }
        val r03 = Math.abs(r11 - r0)
        val r04 = Math.abs(r12 - r02) shl 8
        if (PlayerFsm.isAttackState(S)) { setAnim(292); w.sfx(30) }
        else if (S == 298) w.sfx(30)         // L11: keeps current anim
        else if (r03 <= 0 || r04 <= 0) {     // L35/L38
            setAnim(if (r03 == 0) 275 else 272)
            w.sfx(30)
        } else {
            val r07 = r04 / r03
            setAnim(when {                   // steepness pick
                r07 <= 64 -> 272
                r07 <= 256 -> 273
                r07 <= 1024 -> 274
                else -> 275
            })
            w.sfx(30)                        // L33
        }
        // L40
        cF = 5120; cx = (8 * Trig.M) / 360; cG = av
        if (t.ax == 72) {
            if (t.Z[0] == 1) cM = 0          // L58
            else {                           // L49-L52 param overrides
                if (t.Z[1] > 0) cF = t.Z[1]
                if (t.Z[2] > 0) cx = (t.Z[2] * Trig.M) / 360
            }
        } else if (t.ax == 11 || t.ax == 17) {          // L60-L66
            if (S == 298) { cF = 7680; cx = 0 } else { cF = 5120; cx = 0 }
        }
        // L67: cE = the lunge anim's total frames
        cE = 0
        clip?.let { for (f in 0 until it.frameCount(S)) cE += it.frameDuration(S, f) }
        refreshBoxes()                       // t()
        val r05 = (t.W[1] + t.W[3]) shr 1
        cz = t.ak - X[0]; cA = r05 - X[1]
        cB = h(cz, cA)
        if (cz == 0) cz = 1
        cy = Trig.atan2(cA, -cz)             // j.b(-cz, cA) = atan2(r8,r7)
        cC = cz / cE
        val r06 = (Math.abs(cA) shl 8) / Math.abs(cz)
        cD = if (cz == 1) cA / cE else (Math.abs(cC) * r06) shr 8
        if (r05 < X[1]) cD = -cD
        cJ = X[0]; cK = X[1]; cH = X[0]; cI = X[1]
        F = t
    }

    /** `g.a(int)` (g.java:126, proven): airborne fling — `a(43,32)`
     *  masked enter, +10px down, `ah=r5`, `aj=1536`, drops the
     *  standing-on + resolved links. `as()` aborts to this when the
     *  lunge target vanishes. */
    fun flingAirborne(r5: Int, w: LevelCellSource) {
        enterStateMasked(43, 32, w)
        al += 10; ah = r5; aj = 1536
        standingOn = null
        ac = null
    }

    /**
     * `g.as()` (g.java:4242, proven) — the lunge-arc execution tick:
     * pick the target (mount `i.at` wins unless an interact target is
     * in front — L7/L10/L14 — which must be an ax11 `Z[19]==1` window),
     * advance `(cH,cI)` by `(cC,cD)` for `cE` ticks, then land:
     *  - ax72 → `i(277)` mount-on anim (Z[0]==4 skips both the land
     *    and the anim — resolves `Z[4]` via `k.q` into `ac/aq/ar`
     *    cart-track link instead, L30);
     *  - ax11 → `i(277)` + THROW the victim — `F.ag/ah = ±cF·j.b`
     *    along `cy`, `F.i(181/180)` by `F.g(this)` facing; S==298
     *    victims get `F.i(168)`;
     *  - anything else → `i(0)`; ax17 → silent (L63).
     * `F == null` → `a(0)` airborne fling (L22).
     */
    fun lungeTick(w: LevelCellSource) {
        val mount = Entity.at
        val bound = g
        F = when {
            mount != null && (bound == null || !inFrontOf(bound)) -> mount
            bound != null && bound.ax == 11 && bound.Z[19] == 1 &&
                !bound.deadRelease() -> bound
            else -> null
        }
        val f = F
        if (f == null) { flingAirborne(0, w); return }       // L22 a(0)
        cJ = X[0]; cK = X[1]
        cH += cC; cI += cD; cE--
        if (cE > 0) return                                    // L67 mid-arc
        // arc end — snap the arc point onto the target
        cH = f.ak; cI = (f.W[1] + f.W[3]) shr 1
        if (f.ax == 72) {
            if (f.Z[0] == 4) {                               // L30 cart link
                if (f.Z[4] >= 0) {
                    val r0 = w.findByAw(f.Z[4])
                    if (r0 != null) {
                        f.ac = r0
                        r0.aq = f.ak - r0.ak; r0.ar = f.al - r0.al
                    }
                }
            } else { ak = X[0]; al = X[1] }                  // L37 land
        } else { ak = X[0]; al = X[1] }                      // L37 land
        // L39
        if (f.ax == 72) {
            if (f.Z[0] in 0..4) {                            // L42
                cL = 0
                if (f.Z[0] != 4) setAnim(277)                // mount-on
            } else setAnim(0)                                // L64
        } else when (f.ax) {
            11 -> {                                          // L52-L54
                if (S == 298) f.setAnim(168)
                else {
                    cL = 0; setAnim(277)
                    f.ag = (cF shr 8) * Trig.sin(cy)
                    f.ah = -(cF shr 8) * Trig.sin(Trig.N - cy)
                    f.setAnim(if (f.inFrontOf(this)) 181 else 180)
                }
            }
            17 -> { }                                        // L63 silent
            else -> setAnim(0)                               // L64
        }
    }

    /** `i.p()` (i.java:214, proven): full release — clears the W/X/Y
     *  boxes + ab, cascades ad.p(), drops ae/af/c, and flushes the cr
     *  scratch grid (unmodeled — cr is not part of the port). */
    fun releaseCascade() {
        W.fill(0); X.fill(0); Y.fill(0)
        ab = null
        ad?.releaseCascade(); ad = null
        ae = null; af = null; c = null
    }
                                   // bit4 = mount request, producers unported

    /**
     * `i(n)` (`i.java:240`): set anim/state. Out-of-range indices are
     * rejected (state unchanged); when `n != S`: `Q = old S` (unless
     * `S == 35`), `T = U = a = 0`, `P &= ~64`.
     */
    fun setAnim(n: Int) {
        if (n < 0 || (clip != null && n >= clip!!.animCount())) return
        if (n != S) {
            if (S != 35) Q = S
            S = n
            T = 0
            U = 0
            a = 0
            P = P and -65
            // i.java:265-275 (proven): on the player (ax==0), entering
            // anim 43/148/0 stamps g.y = al (apex/fall-origin marker);
            // entering anim 50 zeroes the meter (g.e(0)).
            if (ax == 0 && (n == 43 || n == 148 || n == 0)) gy = al
            if (ax == 0 && n == 50) x1 = 0
        }
    }

    /** `q()` — jump to the last frame. */
    fun jumpToLastFrame() {
        clip?.let { T = it.frameCount(S) - 1 }
    }

    /**
     * `r()` — true on the last tick of the last frame (`i.java:285`).
     * A zero-duration frame counts as finished immediately.
     */
    fun animFinished(): Boolean {
        val c = clip ?: return true
        if (T != c.frameCount(S) - 1) return false
        val dur = c.frameDuration(S, T)
        return dur == 0 || U == dur - 1
    }

    /**
     * `s()` — per-tick anim advance (`i.java:293`): blocked while
     * `P & 64` (hold), `U < 0`, or `V > 0` (decremented). Frames wrap to 0
     * after the last; the special-case wrap side effects on `ax == 67` /
     * flying levels are level-code, not ported here.
     */
    fun advanceAnim() {
        val c = clip ?: return
        if (P and 64 != 0 || U < 0 || V > 0) {
            if (V > 0) V--
            return
        }
        val dur = c.frameDuration(S, T)
        if (dur == 0) return
        U++
        a++
        if (dur > U) return
        U = 0
        T++
        if (T >= c.frameCount(S)) T = 0
    }

    /**
     * Physics integration (`i.java:3887-3916`, proven): position tracks the
     * integer anchor unless code writes `N`/`O` directly, then vel/accel:
     * `N += ag; ag += ai; ai = 0; ak = N >> 8` and same for the vertical.
     */
    fun integrate() {
        N += (ak - (N shr 8)) shl 8
        N += ag
        ag += ai
        ai = 0
        ak = N shr 8
        O += (al - (O shr 8)) shl 8
        O += ah
        ah += aj
        aj = 0
        al = O shr 8
    }

    /** `P & 7` flags for clip queries (bit0 = facing mirror, etc.). */
    fun drawFlags(): Int = P and 7

    fun setPositionPx(x: Int, y: Int) {
        ak = x; al = y; N = x shl 8; O = y shl 8
    }

    // ==================================================================
    // `t()` — i.java `public final int[] t()` (proven transcription of the
    // main path; ax66/13/21/60 special cases and the Y bounds rect omitted).
    // W = clip rect(which=0) shifted by frame dx/dy, then + ak/al.
    // X = same for which=1. Facing flips the dx sign.
    // ==================================================================
    /**
     * `i.t()` (i.java:369, proven). ax∈{14,37,10,5,42} early-return —
     * their W/X/Y come from record fields at init (L419/L427 arms).
     * Order: W rect → X rect (skipped only for ax66 S∈[6,10]∪[24,28]) →
     * Y bounds quad → absolute translate (X,Y then W — ax60's per-edge
     * Z[5] remaps sit between Y-abs and W-abs; ax60 unspawned).
     * The Y quad is folded twice through the frame dx/dy — once as the
     * ±r13/r14 mirror fixup inside quad space, again as the ±av anchor —
     * transcribed verbatim even though it reads like a double shift.
     */
    fun refreshBoxes() {
        if (ax == 14 || ax == 37 || ax == 10 || ax == 5 || ax == 42) return
        val c = clip
        if (c == null || S < 0 || T < 0) { W.fill(0); X.fill(0); Y.fill(0); return }
        val flags = drawFlags()
        val fi = c.frameIndex(S, T)
        val dx = c.frameDx[fi]; val dy = c.frameDy[fi]
        // W = rect row 0 (L29-L41)
        c.rect(S, T, 0, flags, W)
        if (av) W[0] -= dx else W[0] += dx
        W[1] += dy
        // X = rect row 1 (L51) — non-ax66 unconditional
        if (!(ax == 66 && (S in 6..10 || S in 24..28))) {
            c.rect(S, T, 1, flags, X)
            if (av) X[0] -= dx else X[0] += dx
            X[1] += dy
        }
        // Y = bounds quad (L59 ax13 radial / L62 generic)
        if (ax == 13) {
            Y[0] = -Z[1] * 12; Y[1] = 0
            Y[2] = Z[1] * 12; Y[3] = Z[1] * 12
        } else if (U >= 0) {
            val obj = c.frameModuleIndex(S, T)
            val bx = IntArray(4)
            c.objectBounds(obj, bx)
            val r13 = if (flags and 1 != 0) dx else -dx
            val r14 = if (flags and 2 != 0) dy else -dy
            Y[0] = bx[0] - r13; Y[1] = bx[1] - r14
            Y[2] = bx[2] + Y[0]; Y[3] = bx[3] + Y[1]
            if (flags and 1 != 0) { val t = Y[0]; Y[0] = -Y[2]; Y[2] = -t }
            if (flags and 2 != 0) { val t = Y[1]; Y[1] = -Y[3]; Y[3] = -t }
            if (av) { Y[0] -= dx; Y[2] -= dx } else { Y[0] += dx; Y[2] += dx }
            Y[1] += dy; Y[3] += dy
        } else {
            // L82 (U<0): bounds indexed by raw T + flags mirror, no anchor fold
            val bx = IntArray(4)
            c.objectBounds(T, bx)
            Y[0] = bx[0]; Y[1] = bx[1]
            Y[2] = bx[2] + bx[0]; Y[3] = bx[3] + bx[1]
            if (flags and 1 != 0) { val t = Y[0]; Y[0] = -Y[2]; Y[2] = -t }
            if (flags and 2 != 0) { val t = Y[1]; Y[1] = -Y[3]; Y[3] = -t }
        }
        // absolute translate (L102 X, L103 Y, L91 W)
        X[0] += ak; X[1] += al; X[2] += X[0]; X[3] += X[1]
        Y[0] += ak; Y[1] += al; Y[2] += ak; Y[3] += al
        W[0] += ak; W[1] += al; W[2] += W[0]; W[3] += W[1]
    }

    /**
     * `e(cx, cy)` = `k.g` — collision cell at grid coords (OOB -> 20).
     * Delegated to the level via [world].
     */
    fun e(world: LevelCellSource, cx: Int, cy: Int): Int = world.collisionCell(cx, cy)

    /**
     * `x()` — i.java `public final int x()` (proven). Probes the cell column
     * under the anchor: fills aO/aP (top row, +1), aR/aQ/aS (below feet,
     * -1, +1), aV/aW (left/right of the feet cell), `aZ` (aR != 18), `bd`,
     * and returns the pixel snap r8 for slope/edge adjustment. Callers in
     * the original mostly use the side effects; the snap is consumed by the
     * move code paths that re-call x() after mutating ak/al.
     */
    fun probeCells(world: LevelCellSource): Int {
        refreshBoxes()
        var r8 = 0
        val r0 = W[1]
        val r02 = W[3] + 1
        aZ = false
        bd = ah != 0
        val cx = ak / 20
        aO = e(world, cx, r0 / 20)
        aP = e(world, cx, r0 / 20 + 1)
        aR = e(world, cx, r02 / 20)
        aQ = e(world, cx, r02 / 20 - 1)
        aS = e(world, cx, r02 / 20 + 1)
        aV = e(world, cx - 1, r02 / 20)
        aW = e(world, cx + 1, r02 / 20)
        if (aR < 10) {
            // L9: only aR == 5 continues; others return early.
            if (aR != 5) return r8
            bd = false
        }
        r8 = r02 % 20
        if (aO < 12 || aP < 12) {
            // L16: shallow embed — step up a cell into the block above.
            if (aQ >= 10) {
                r8 += 20
                aS = aR
                aR = aQ
                v = false
            }
        } else {
            bd = false
        }
        if (aR != 18) aZ = true
        if (aR in 14..17 || aR in 24..27) {
            // slope family: sub-cell adjust (i.java x() L26-L52)
            val r04 = (ak % 20) shr 1
            when (aR) {
                14, 24 -> r8 -= r04
                15, 25 -> { r8 -= r04 + 10; if (ax == 11) r8 -= 1 }
                16, 26 -> r8 -= (10 - r04) + 10
                17, 27 -> r8 -= (10 - r04)
            }
            if (aR in 24..27) r8 -= 5
            v = false
            bd = true
            return r8
        }
        when (aR) {
            12 -> { r8 -= ak % 20; v = false }
            13 -> { r8 -= 20 - (ak % 20); v = false }
        }
        return r8
    }

    /**
     * `a(boolean)` — i.java `public final void a(boolean)` (proven). Side
     * probes aT/aU (max cell value per hitbox side strip), wall flags bb/bc,
     * and — when resolve=true — pushes `ak` out of walls and re-probes.
     * Iterates hitbox rows from top (W[1]) down to W[3]-10 (feet excluded).
     */
    fun collideSides(world: LevelCellSource, resolve: Boolean) {
        v = true
        probeCells(world)
        var r10 = W[0] - 1
        var r11 = W[2] + 1
        var r12 = W[1]
        var r13 = W[3] - 10
        bb = false; bc = false; ba = false; aT = 0; aU = 0
        if (resolve && bd) {
            // L16/L34: embedded — recompute then continue probes
            refreshBoxes()
            r10 = W[0] - 1; r11 = W[2] + 1; r12 = W[1]; r13 = W[3] - 10
        }
        val r02 = e(world, r10 / 20, r12 / 20 - 1)
        val r03 = e(world, r11 / 20, r12 / 20 - 1)
        var cy = r12 / 20
        while (cy <= r13 / 20) {
            val l = e(world, r10 / 20, cy)
            if (l > aT) {
                aT = l
                if (aT >= 18) {
                    aX = if (r02 < 18) (r13 / 20) - cy + 1
                         else (r13 / 20) - (r12 / 20 - 1) + 1
                    bb = true
                }
            }
            val r = e(world, r11 / 20, cy)
            if (r > aU) {
                aU = r
                if (aU >= 18) {
                    aY = if (r03 < 18) (r13 / 20) - cy + 1
                         else (r13 / 20) - (r12 / 20 - 1) + 1
                    bc = true
                }
            }
            if (bb || bc) break
            cy++
        }
        if (resolve && v) {
            if (bb == bc) {
                bc = false; bb = false
            } else if (ag > 0) {
                // L75/L77: moving right — push left out of a right wall,
                // else right out of a left wall (fallback).
                if (bc) ak -= r11 % 20 else ak += (20 - ((r10 + 20) % 20)) - 1
                probeCells(world)
            } else {
                // L69/L73: mirrored for leftward/still.
                if (bb) ak += (20 - ((r10 + 20) % 20)) - 1 else ak -= r11 % 20
                probeCells(world)
            }
        }
        refreshBoxes()
        tc = (W[0] + W[2]) shr 1
        uc = (W[1] + W[3]) shr 1
    }

    /** `y()` — wall in the direction of motion/facing (i.java, proven). */
    fun hitWall(): Boolean = when {
        ag < 0 -> bb
        ag > 0 -> bc
        else -> if (av) bb else bc
    }

    /**
     * `av()` — g.java `void av()` (proven). Air wall-resolve: probes one cell
     * higher (head region), then pushes ak back ±10 when flying into a solid
     * side cell at the feet row. Rope entity path (i.bq) not ported.
     */
    fun airWallResolve(world: LevelCellSource) {
        al -= 20
        probeCells(world)
        al += 20
        if (aO >= 20) { enterFall(); return }
        if (ah >= 0) return
        refreshBoxes()
        val r0 = W[3]
        val r02 = W[0] - 1
        val r03 = W[2] + 1
        if (ag > 0 && e(world, r03 / 20, r0 / 20) >= 20) {
            ai = 0; ag = 0; ak -= 10
        } else if (ag < 0 && e(world, r02 / 20, r0 / 20) >= 20) {
            ai = 0; ag = 0; ak += 10
        }
    }

    /**
     * `a(int r7, int r8)` — i.java `final void a(int,int)` (proven): enter
     * state r7 with position re-centering per the r8 mask. Only the bits used
     * by slice-2 call sites are transcribed: bit5 (32) recentres `al` on the
     * hitbox midline, and the bit0 ordering rule (i(r7) before adjusts when
     * clear, after when set).
     */
    fun enterStateMasked(n: Int, mask: Int, world: LevelCellSource) {
        if (mask and 1 == 0) { setAnim(n); refreshBoxes() }
        if (mask and 2048 != 0) ak = W[0]
        if (mask and 4096 != 0) ak = W[2]
        if (mask and 4 != 0) ak += tc - ((W[0] + W[2]) shr 1)
        if (mask and 8 != 0) ak -= (ak % 20) - 10
        if (mask and 64 != 0) al = W[1]
        else if (mask and 128 != 0) al = W[3]
        else if (mask and 8192 != 0) al = ((W[3] / 20) * 20) - 1
        else if (mask and 16384 != 0) al = (((W[3] + 10) / 20) * 20) - 1
        else if (mask and 256 != 0) al = (((W[1] + 10) / 20) * 20) - 1
        else if (mask and 512 != 0) al = (((W[1] / 20) + 1) * 20) - 1
        else if (mask and 32 != 0) al += uc - ((W[1] + W[3]) shr 1)
        else if (mask and 1024 != 0) al += al - W[1]
        if (mask and 16 != 0) al -= (al % 20) - 10
        if (mask and 1 != 0) setAnim(n)
    }

    /**
     * `a(int)` — g.java `void a(int r5)` (proven): leave the ground into the
     * shared fall state: `a(43,32); al += 10; ah = r5; aj = 1536`.
     */
    fun enterFall(vy: Int = 0, world: LevelCellSource? = null) {
        if (world != null) enterStateMasked(43, 32, world) else setAnim(43)
        al += 10
        ah = vy
        aj = 1536
        standingOn = null
        // ac = null (climbable ref) — unused in slice 2
    }

    /**
     * `d(boolean)` — g.java `private void d(boolean)` (proven): land on the
     * ground cell top. `al = ((W[3]+1)/20)*20 - 1`; variant enters S102
     * (platform-4 land). r8=false path: S16/Q16 or S150 special-cased, else
     * `i(5)` land-squat; the >20-cell fall-damage hook (`a(21,0,0,this)`)
     * requires the damage-floatie spawner — flagged `inferred` and skipped.
     */
    fun land(world: LevelCellSource, platformVariant: Boolean) {
        refreshBoxes()
        al = ((W[3] + 1) / 20) * 20
        al--
        // g.java:4674-4688 (proven): edge bump pushes the anchor down one
        // cell when the landing came through aS (probe row still in air),
        // so aR re-reads the floor and aZ becomes true. Normal landing:
        // aR<12 && (aS>=12 || aS==5); platform variant: aS==4 && aR!=4.
        if (platformVariant) {
            if (aR != 4 && aS == 4) al += 20
        } else {
            if (aR < 12 && (aS >= 12 || aS == 5)) al += 20
        }
        ah = 1
        // d() fall-damage gate (g.java:4690-4700, proven): falls of
        // >=20 cells (al - g.y >= 400px) without iframes call
        // a(21,0,0,this) → the raw op21 drain. Original `return`s after
        // the op (landing squat skipped); we apply it then continue the
        // land so the transition can't re-fire — inferred control flow.
        if ((al - gy) / 20 >= 20 && gt == 0 && ax == 0) {
            applyHit(21, 0, this, world)
        }
        if (platformVariant) {
            aj = 0; ai = 0; ah = 0; ag = 0; aC = 18; setAnim(102)
        } else {
            when {
                S == 16 || Q == 16 -> { setAnim(5); ag = 0 }
                S == 150 -> setAnim(152)
                else -> { setAnim(5); ag = 0 }
            }
        }
        O = al shl 8
    }

    /**
     * `i.a(int r10, int r11, int r12, i r13)` — shared "effect" dispatcher
     * (i.java:4446+; op subset reachable in slice 3, `proven` bodies,
     * deferrals flagged):
     *   4  melee-contact — rewrites to 18 when the target is mid-attack
     *      (`g.b(S)`); `g.a()` blocking would counter via `c(attacker)` —
     *      deferred (block input unmined); passive target falls through to
     *      the `k.A(18)` hurt-mark (recorded on `hitsTaken`).
     *   18 `i(43)` — hit interrupt into tumble.   20 `i(43)` — knockdown.
     *   26 launch: `av=attacker.av; ag=±4096; ah=-4096; aj=1536; a(43,32)`
     *   29 stumble: `av=attacker.av; i(10); ag=∓1536`
     *   34 damage-mark: zero vel + `a(8,5,14,…)` floatie + `k.A(11)` sfx
     *      (recorded on `hitsTaken`; floatie/sfx spawners deferred).
     * `d(int)` (g.java:3884) drains the meter: gates on busy/lock states,
     * `x[1]-=r5` clamped at 0; at 0 (non-flying `bh[aj]!=3`) the player is
     * knocked out: `bl=0; G(); H()` (detach links) or `E()` ground-snap —
     * then `k.l(12)` mission-fail. Ported as `dead` flag → world respawn.
     */
    /**
     * `c(i attacker)` counter-stagger (proven, i.java:4379): face the
     * attacker, `ag = ±1536` knockback away, floatie `a(8,5,14)` skipped,
     * `i(9)` stagger (i(6) for ax61 omitted).
     */
    fun counteredBy(attacker: Entity) {
        av = attacker.ak < ak
        ag = if (av) 1536 else -1536
        setAnim(9)
    }

    /**
     * `g.d(int)` meter drain (proven, g.java:3884): skips while `s`
     * (cutscene, unported), `t != 0` iframes, `c()` linked-carry, or
     * `S in {67,183,184,205}`; else `i.bh = 8` flash + `x[1] -= amt`
     * clamped at 0; survival sets `t = 10`.
     */
    fun drainMeter(amt: Int) {
        if (gt > 0 || S == 67 || S == 183 || S == 184 || S == 205) return
        bh = 8
        x1 = (x1 - amt).coerceAtLeast(0)
        if (x1 > 0) gt = 10
    }

    /**
     * `i.p()` (i.java:214, proven): deactivate — release collision boxes,
     * cascade to the `ad` child, drop ae/af links. The original's `aS()`
     * tail only flushes the `cr` scratch-grid cache — unmodeled. The entity
     * stays listed but inert (zero W → no overlap arms fire).
     */
    fun deactivate() {
        W.fill(0); X.fill(0); Y.fill(0)
        ad?.deactivate()
        ad = null; ae = null; af = null
    }

    /**
     * `i.G()` (i.java:4792, proven): deactivate the player's live `ae`
     * pickup indicator and drop the reference.
     */
    fun releaseAe() {
        ae?.deactivate()
        ae = null
    }

    /**
     * `i.g(i)` (i.java:7758, proven): true when `r4` is on the side this
     * entity faces — `(r4.ak < ak) == av`.
     */
    fun inFrontOf(r4: Entity): Boolean = (r4.ak < ak) == av

    /**
     * `g.f()` (g.java:3763, proven): player holding/carried check —
     * `ci != null && g(ci) && |ci.ak-ak|<120 && |ci.al-al|<20`. Pickups
     * suppress while the player's hands are occupied.
     */
    fun isHolding(): Boolean {
        val c = ci ?: return false
        if (!inFrontOf(c)) return false
        return Math.abs(c.ak - ak) < 120 && Math.abs(c.al - al) < 20
    }

    /**
     * `i.P()` (i.java:7699, proven): dead check — `aB<=0 → G()` releases
     * the `ae`/`ab` slots and returns true. Alive (`aB>0`) → false.
     */
    fun deadRelease(): Boolean = if (aB > 0) false else { releaseAe(); true }

    /** `g.g(int)` (g.java:5266, proven): `J |= mask` — ORs an action-request
     *  bit. The original also calls `k.q()` (queue refresh — unmined). */
    fun requestAction(mask: Int) { gJ = gJ or mask }

    /** `g.h(int)` (g.java:5270, proven): request/consume — when `r4!=0`
     *  requires `J&r4` pending; sets `I=r4`, forces `k.at=1`, and when the
     *  mount link's `ab` is an ax16 request entity runs its `H()` consume
     *  (internals unmined — recorded via `consumedH`). `r4==1` → true;
     *  `S!=38` → true; `S==38` repeats the consume and returns false. */
    fun requestH(r4: Int): Boolean {
        if (r4 != 0 && (gJ and r4) == 0) return false
        gI = r4
        at?.let { t -> if (t.ab?.ax == 16) t.consumeH() }
        if (r4 == 1) return true
        if (S != 38) return true
        gI = 1
        val t = at ?: return false
        val link = t.ab ?: return false
        if (link.ax != 16) return false
        t.consumeH()
        return false
    }

    /**
     * `g.E()` tail — the consume call after `i(91)`/`A(15)` in ax16's
     *  hurt arms is `k.aS.E()` = `i.E()` = `settleToGround` (i.java:3760),
     *  already ported below. Kept as a named alias for call-site clarity.
     */

    /** `i.E()` (i.java:3760, proven shape): settle loop — sink `al` in
     *  10px steps until the below-feet cell is standable
     *  (`aR >= 12 || aR == 5 || aR == 3`). Guarded against missing floor. */
    fun settleToGround(world: LevelCellSource) {
        refreshBoxes()
        var below = e(world, ak / 20, (W[3] + 1) / 20)
        var guard = 0
        while (below < 12 && below != 5 && below != 3 && guard++ < 400) {
            al += 10
            refreshBoxes()
            below = e(world, ak / 20, (W[3] + 1) / 20)
        }
        aR = below
    }

    /**
     * `k.h(dx,dy)` (k.java:6839, proven): octagonal distance in px —
     * `(a+b) - (min>>1) - (min>>2) + (min>>3)`.
     */
    fun h(dx: Int, dy: Int): Int {
        var x = if (dx >= 0) dx else -dx
        var y = if (dy >= 0) dy else -dy
        if (x == 0 && y == 0) return 0
        val mn = if (x <= y) x else y
        return ((x + y) - (mn shr 1) - (mn shr 2)) + (mn shr 3)
    }

    /**
     * `i.e(i)` (i.java:2407, proven): Bresenham LOS walk in 20px cells
     * between the two W-centers — `cell >= 12` → true (blocked); OOB
     * reads as 20 (blocked) via i.e(x,y) (i.java:15294, ax0-override
     * arms for S37/257 unported — raw cell read here, `inferred` on
     * those arms). End conditions per major axis.
     */
    fun losBlocked(t: Entity, world: LevelCellSource): Boolean {
        if (W.contentEquals(ZERO_RECT) || t.W.contentEquals(ZERO_RECT)) return false
        var cx = ((W[0] + W[2]) shr 1) / 20
        var cy = ((W[1] + W[3]) shr 1) / 20
        val tx = ((t.W[0] + t.W[2]) shr 1) / 20
        val ty = ((t.W[1] + t.W[3]) shr 1) / 20
        var dx = tx - cx; if (dx < 0) dx = -dx
        var dy = ty - cy; if (dy < 0) dy = -dy
        val sx = if (tx < cx) -1 else 1
        val sy = if (ty < cy) -1 else 1
        if (dx > dy) {
            var err = dx / 2
            while (cx != tx) {
                if (cellForLos(cx, cy, world) >= 12) return true
                cx += sx
                err += dy
                if (err > dx) { cy += sy; err -= dx }
            }
        } else {
            var err = dy / 2
            while (cy != ty) {
                if (cellForLos(cx, cy, world) >= 12) return true
                cy += sy
                err += dx
                if (err > dy) { cx += sx; err -= dy }
            }
        }
        return false
    }

    /** `i.e(x,y)` subset (i.java:15294): OOB → 20, else raw cell value. */
    private fun cellForLos(cx: Int, cy: Int, world: LevelCellSource): Int {
        if (cx < 0 || cy < 0) return 20
        return world.collisionCell(cx, cy)
    }

    /**
     * `g.i(i)` (g.java:5465, proven): interact-eligibility of `cand`
     * under this player's current state — `J&4 && cand.ax==11 &&
     * cand.Z[19]==1` → facing + |dx|<=200 window; `S∈{268,291}` or
     * `aS.S==267` → true; `S==303 && r()` → true; `S∈{295,357,358}` →
     * true; `S∈[299,307]` → true; else false.
     */
    fun interactEligible(cand: Entity): Boolean {
        if (gJ and 4 != 0 && cand.ax == 11 && cand.Z[19] == 1) {
            if (av && cand.ak - ak >= 0) return false
            if (!av && cand.ak - ak > 0) return false
            if (Math.abs(cand.ak - ak) > 200) return false
            return true
        }
        if (S == 268 || S == 291) return true
        if (S == 303) return animFinished()
        if (S == 295 || S == 357 || S == 358) return true
        if (S in 299..307) return true
        return false
    }

    /** `g.k(int)` (g.java:5434, proven): mount-eligible state whitelist. */
    fun mountableState(): Boolean = when (S) {
        0, 1, 18, 19, 20, 23, 24, 25, 35, 36, 43, 150, 157, 165,
        242, 243, 263, 264, 265, 266, 358 -> true
        else -> false
    }

    /**
     * `i.u()` (i.java:700, proven): recompute `au` = normalized distance
     * from the view center (k.O+200, k.P+120). Arms: aG==4 → /400,/240;
     * ax67&&bk[Z0]==49 → /400,/240; ax67&&bk[Z0]==27 → /800,/240;
     * generic → /400,/120. (ax21's L14 arm duplicates the aG==4 body.)
     */
    fun updateAu(world: LevelCellSource) {
        var dx = ak - (world.kO + 200); if (dx < 0) dx = -dx
        var dy = al - (world.kP + 120); if (dy < 0) dy = -dy
        au = when {
            aG == 4 -> dx / 400 + dy / 240
            ax == 67 && NpcFsm.decorClip(Z[0]) == 49 -> dx / 400 + dy / 240
            ax == 67 && NpcFsm.decorClip(Z[0]) == 27 -> dx / 800 + dy / 240
            else -> dx / 400 + dy / 120
        }
    }

    /**
     * `i.v()` (i.java:730, proven) — "on-screen / recently-active" check:
     * typed early-true arms, then `u(); au>i → false`; else for ax67 and
     * other non-listed types `a(k.ac, this.Y)` — the Y bounds quad against
     * the camera view rect. ax14's arm (S76/W-null/`a(player.W,W)`) is
     * transcribed too for the pickup path.
     */
    fun wasHitRecently(world: LevelCellSource): Boolean {
        if (ax == 49) return true
        if (ax == 29 && S == 24 && T >= 54) return true
        if (ax == 10 || ax == 40 || ax == 60) {
            if (P and 16 != 0) return true     // L21
        }
        if (ax == 27 && S == 6 && Z[1] > 0) return true
        if (ax == 21 && S >= 2) return true    // L35
        updateAu(world)
        if (au > i) return false               // offscreen score
        if (ax == 60) return true
        if (ax == 11 && Z[8] == 888) return true
        if (ax == 14) {                        // L51 ax14 arm
            if (S == 76) return true
            if (W.contentEquals(ZERO_RECT)) return true
            val view = world.camRect
            if (world.inPlay || S == 69 || S == 70 || S == 71)
                return overlapI(view, Y)
            return overlapI(world.playerRect(), W)
        }
        // L73: ax∈{37,10,60} → a(k.ac, W); ax==78&&S!=3 → a(k.ac,Y)
        if (ax == 37 || ax == 10 || ax == 60) return overlapI(world.camRect, W)
        return overlapI(world.camRect, Y)      // L85 — ax67 lands here
    }

    companion object {
        val ZERO_RECT = IntArray(4)
        /** `i.at` (i.java:42) — static mount/assassination link; set by
         *  az()'s ax72 arm and the ax11 grab arm (i.java:6007). */
        var at: Entity? = null
        /** `i.L`/`i.M` (i.java:9825 `o(x,y)`) — the static indicator
         *  point; -1 = unset (cleared by `U()`). */
        var L = -1
        var M = -1
        /** `i.bq` — static cleared on grab (`c()` head, g.java:4118). */
        var bq = 0
        /** `i.a(int[],int[])` (i.java:632, proven) — inclusive-edge overlap. */
        fun overlapI(a: IntArray, b: IntArray): Boolean =
            a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1]
    }

    fun applyHit(op: Int, arg: Int, attacker: Entity?, world: LevelCellSource) {
        var r10 = op
        // i.java:4446 head (proven): op4 upgraded to op18 when the struck
        // entity is the player mid-attack-anim without iframes/cutscene;
        // the upgrade also zeroes the victim's vx.
        if (r10 == 4 && PlayerFsm.isAttackState(S) && gt == 0) {
            r10 = 18; ag = 0
        }
        when (r10) {
            // i.a(op4) L116-L131: struck while meter payable → c(attacker)
            // (auto-counter; u[au] meter cost), else hurt-mark k.A(18)
            4 -> {
                val canCounter = attacker != null && x1 > 0 && S != 9 &&
                    gt == 0 &&
                    attacker.ax != 17 && attacker.ax != 50 && attacker.ax != 61
                if (canCounter) { drainMeter(5); attacker.counteredBy(this) }
                else hitsTaken++
            }
            // op18 body calls g.a() first → pays u[au]=5 meter then i(43);
            // d() gates apply (e.g. S67 clash drains nothing but still
            // knocks down — proven via the S∈{67,…} guard inside d())
            18 -> { drainMeter(5); setAnim(43) }
            20 -> setAnim(43)
            // op21 fall damage (i.java:4535 L55, proven): raw drain —
            // bypasses d() gates; caller (land) already checked h()+the
            // 20-cell gate
            21 -> x1 = (x1 - (105 * ((al - gy) / 20)) / 20).coerceAtLeast(0)
            26 -> {
                if (attacker != null) av = attacker.av
                ag = if (av) 4096 else -4096
                ah = -4096; aj = 1536
                enterStateMasked(43, 32, world)
            }
            29 -> {
                if (attacker != null) av = attacker.av
                setAnim(10)
                ag = if (av) 1536 else -1536
            }
            34 -> { aj = 0; ah = 0; ag = 0; hitsTaken++ }
        }
    }
}

/** Minimal cell-source interface so `e()`/probes work against the level. */
interface LevelCellSource {
    val cellPx: Int
    fun collisionCell(cx: Int, cy: Int): Int
    fun isSolid(v: Int): Boolean
    fun isOneWay(v: Int): Boolean
    /** `i.aN` — the single weakened-target lock (static field in the
     *  original; scoped to the world here). */
    var lockTarget: Entity?
    /** `k.q(uid)` lookup source (proven: entity list search by `aw`). */
    val npcs: List<Entity>
    /** `k.c(e)` — mark entity removed; applied after the npc tick pass
     *  (the original unlinks dead triggers rather than mutating mid-pass). */
    fun removeEntity(e: Entity)

    // -- k.a(i,prio,rect) context claim (k.java:816): strictly-lower --
    var claimPrio: Int                    // k.co (6 = unclaimed)
    var claimed: Entity?                  // k.L
    fun claim(e: Entity, prio: Int, w: IntArray)
    fun clearClaim()                      // k.m()

    // -- k.c(x,y,aw)/k.k(aw) marker popup (k.java:870) -----------------
    var marker: Entity?
    fun setMarker(x: Int, y: Int, tag: Int)
    fun clearMarker(tag: Int)

    // -- k.aq / k.ap / k.s() / k.A(int) counters ----------------------
    var aq: Int
    val apStats: IntArray
    var shake: Int
    val sfxLog: List<Int>
    fun sfx(id: Int)
    fun shake()

    /** `m(-1)` wisp burst (i.java:21259) spawned through `a(74,54,1,…)`. */
    fun spawnWisp(src: Entity)

    // -- globals read by prop FSMs --------------------------------------
    /** `k.Y` — global fall impulse (k.java:2336 `Y = X<<8`; source writes
     *  at phase transitions are unmined; level-0 arms reading it are
     *  unreachable). */
    val kY: Int get() = 0
    /** `k.O` — camera left edge in world px (subtract operand at
     *  i.java:9837; ax14 pickups pin to `k.O+{20,380}` = the view edges). */
    val kO: Int get() = 0
    /** `k.P` — camera top edge in world px (u() center operand). */
    val kP: Int get() = 0
    /** `k.ac` — camera view rect [x1,y1,x2,y2] world px (v()'s L83/L85). */
    val camRect: IntArray get() = IntArray(4)
    /** `k.bh[k.aj] == 3` — in-play phase (v()'s ax14 arm L61). */
    val inPlay: Boolean get() = true
    /** `k.aS.W` — the player's hitbox (v()'s ax14 tail). */
    fun playerRect(): IntArray = IntArray(4)
    /** `k.q(o)` — resolve a linked entity by its `aw` id. */
    fun findByAw(aw: Int): Entity? = null

    /** `k.r(idx)` — clip-space access for i.T()'s identity check. */
    fun clipFor(idx: Int): Clip? = null
    /** `k.a(k.H,k.I, e.ak-k.O, e.al-k.P, r)` (k.java:627): is the entity
     *  within r px (k.h octagonal) of the view-space touch point? */
    fun touchNearView(e: Entity, r: Int): Boolean = false
    /** `k.k()` (k.java:638) — `cm == 1` mounted flag. */
    val mounted: Boolean get() = false
    /** `cm = true` at g.java:3564 (L2040) — sets `cm = 1`. */
    fun setMounted() {}

    /** `i.a(int,int,int)` (i.java:9810): spawn an ax14 clip9 pickup
     *  indicator (anim `n`, az=302) and return it for `ae` binding. */
    fun spawnPickup(anim: Int, x: Int, y: Int): Entity
}
