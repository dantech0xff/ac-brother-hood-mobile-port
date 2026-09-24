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
    var remapTable = -1              // aA — az[] module-remap slot (b.a(int))
    var paletteAlpha = 255           // g(aH,alpha) — palette alpha (b.g)
    var N: Int = 0                   // 8.8 x
    var O: Int = 0                   // 8.8 y
    var ag: Int = 0                  // vx
    var ah: Int = 0                  // vy
    var ai: Int = 0                  // axel x
    var aj: Int = 0                  // axel y
    var S: Int = 0                   // anim/state index
    var T: Int = 0                   // frame
    var asSlot = -98                 // i.as — bf[] save-image slot (record
                                     // order among non-{0,25,55,70}; -98 =
                                     // runtime-spawned, no slot)
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
    val Y = IntArray(4)              // context-bounds rect (t() L22f
                                     // arm, i.java:962-1140 proven —
                                     // `aa.d(S,T)=av|(i&C0)<<2` indexes
                                     // ak_or_al quads; refreshBoxes
                                     // fills it)
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
    // g.cp/cq/ct/cu/cv/cw/z — e() tail latches (g.java:1285-1301): the
    // head clears all of them EVERY tick; state arms re-arm only theirs
    // (`l()` cp/cq/z, air/fall cv/cp/ct/cw, S33 cp/ct, S38 cq, S60 cu,
    // S263/264 cw). `cr`/`cs` have no port fields — dead in the original.
    var cp = false; var cq = false; var ct = false; var cw = false; var cv = false
    var cu = false                      // g.cu — case-60 sets it (ledge-
                                        // hang drop eligibility)
    var aA = 0                       // alert level (NPC) / turn-block (player)
    var aB = 0                       // hp-ish stat (az = max)
    var bR = false                   // i.bR — knife bounced-off-a-swing flag (bb L58)
    var cGCount = 0                  // i.cG int — hit-flash counter on sweep
                                   // targets (distinct from g.cG bool)
    var az = 0
    var standingOn: Entity? = null   // `a` — entity stood upon (null in slice 2)
    var platform: Entity? = null     // `s` — linked platform/rope (null here)
    /** `ac` — resolved link target; writes run `i.a(i)` (i.java:229,
     *  proven): clear `P|256` on the old target, set it on the new. */
    var ac: Entity? = null
        set(o) {
            field?.P = field!!.P and -257
            field = o
            o?.P = o.P or 256
        }
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
    var cFlag = false              // g.C  — ax43 ride-flag (g.java:48;
                                   // o() bind sets true, aV() S50 clears)
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
                                   // i.java:922/937; producers: ax15 bind,
                                   // Entity:1210/1285 lunge, ax66/72 arms)
    var gh: Entity? = null         // g.h — victim link (L2460 S203 arm: the
                                   // marker follows `gh.aw`; v(65568) dumps it)
    // -- ax67 prop fields (init L347, i.java:3530; tick bB i.java:17584) --
    var bZ = 0                     // i.bZ lifecycle counter (aX L23 linked
                                   // arm — used by the ax14/pickup path)
    var ad: Entity? = null         // i.ad child link (bd[] scan, p() cascade)
    var au = 10                    // i.au screen-distance score (u() rewrites
                                   // it per v() call; ctor 10, i.java:819)
    var ae: Entity? = null         // i.ae player's spawned ax14 pickup ref
    /** `i.cr` (i.java:111 `i[][]`, proven) — the ax10-S30 pursuer-pool
     *  matrix: `cr[row][col]` members allocated on first overlap by the
     *  La72 arm; `aS()` drains it (null), `aT()` reports all-dead. */
    var cr: Array<Array<Entity>>? = null
    var gb: Entity? = null         // g.b grabbed-prop ref (op40 arm)
    var ge: Entity? = null         // g.e hide-spot owner (bB S12 arm)
    var gg: Entity? = null         // g.g hide-spot busy guard
    var ci: Entity? = null         // g.ci carried prop (f() holding check)
    var g: Entity? = null          // g.g interact target (az() scan)
    var gJ = 0                      // g.J action-request bits (g.g(mask));
    var ab: Entity? = null        // i.ab link — mount gate in g.h consume
    var bm = 0                     // i.bm — mash-QTE input latch (4112/8256)
    var ca = -1                    // i.ca — bound claim-counter index (-1 = none)
    val cd = BooleanArray(10)      // i.cd[10] — claim-script flags (h() allocs
                                   // in the original; eagerly allocated here)
    private var cdAllocated = false // i.java:19302 — the original sets
                                   // cd[7]=true only on the FIRST cd alloc;
                                   // eager alloc replicates via this flag
    var cb: IntArray? = null       // i.cb[4] — claim-script vars (k() allocs)
    var cc: IntArray? = null       // i.cc[5] — op112/113 multi-choice state
    var cf: IntArray? = null       // i.cf[5] — op109 carrier-track rect+angle
    var cg: Entity? = null         // i.cg — op110 carrier-link A (inst field)
    var ch: Entity? = null         // i.ch — op110 carrier-link B
    var cQ: IntArray? = null       // i.cQ[10] — op106 dialog-line state
    var cR = ""                   // i.cR — wrapped dialog text (bK)
    var cS: IntArray? = null       // i.cS — wrapped-line count (bK)
    var cT: Entity? = null         // i.cT — op106 dialog target entity
    var scriptStep = -1            // i.cK — script key counter (bJ resets -1,
                                   // k() arms 0; the g.cK lunge field is a
                                   // different class member — same letter)
    var scriptOps: IntArray? = null// i.cL — claim-script op buffer copy of
                                   // k.bz[ca] (g.cL is the int orbit field —
                                   // same decompiled letter, different type)
    var y = 0                      // i.y (byte) — pending anim-request;
                                   // `a(int)` writes 101 when y >= 0
    var claimLatchX = -1           // i.cM — claim position-latch x, -1 =
                                   // unbound (g.cM is the int orbit counter)
    var claimLatchY = -1           // i.cN — claim position-latch y (g.cN is
                                   // the int interact-gauge sub-tick)
    var cP = -1                    // i.cP — op101 write / bI pending index
    var eventN = 0                 // i.n — record event id (ax5 r8[8]); `N`
                                   // (8.8 x pos) already owns the JVM name
    var bM: Entity? = null         // i.bM — held-entity two-way link
                                   // (bI's ax13 arm clears it)
    var bN = 0                     // i.bN — ax13 rope segment count
    var bO = 0                     // i.bO — ax13 pendulum angular vel (8.8)
    var bP = 0                     // i.bP — ax13 pendulum angle (8.8)
    var ropeGrabSeg = 0            // i.bN write-target — the r04 grab
                                   // segment chosen by aW()'s L89 chain
    var bl = 0                     // i.bl — foot-contact flag (cleared on
                                   // player death, g.java:3914)
    var s: Entity? = null         // i.s — ax51 side-link read by aF()
    var c: Entity? = null         // i.c carry link (released by p())
    var b = false                  // i.b — bounds-dirty flag: movers set it,
                                 // I()'s default arm L1f35 calls t() on it
                                 // (i.java:18904, proven)
    var scriptBound = false        // i.d — latched when the record's Z[13]
                                   // script claim binds at init
                                   // (i.java:2266); released inside the
                                   // S117 claim-release arm (i.java:5147)
    // -- mission-director + barrage fields (i.java:18003+, bD/bG/d/p/g) ------
    var ao = 0                     // i.ao — floatie aim target x
    var ap = 0                     // i.ap — floatie aim target y
    var am = 0                     // i.am — ax35 carry-scrub start x
    var an = 0                     // i.an — ax35 carry-scrub start y
    var bY = 0                     // i.bY — waypoint prev-x px (bZ reused)
    var bs = 0                     // i.bs — respawn slot index (Z[bs+1])
    var iE = false                 // i.E — director engaged flag
    // -- ax54/ax30 waypoint-runner fields (ax() i.java:8170) ----------------
    var runnerBz = false           // i.bz — trigger-line latch (al>P+Z[7])
    var runnerB = false            // i.B — homing leg active
    var runnerC = 0                // i.C — resolved waypoint chain length
    var runnerD = 0                // i.D — waypoint dwell countdown (bt.d)
    var wpBt: Waypoint? = null     // i.bt — current chain waypoint (c.a)
    var wpF: Waypoint? = null      // i.F — bound companion waypoint (c.a(Z[5]))
    var runnerG = false            // i.G — ax56 pattern-done flag (ay())
    var projB = false              // i.b — explode-on-contact flag (ba())
    var projK = false              // i.k — lobbed-arc phase flag (ba())
    var cHWaypoints: Array<IntArray>? = null  // i.cH — homing waypoint
                                              // table on the owner (af)
    var iP = 0                     // i.p — ax32 sub-type (bc() anim pick)
    var cIDone = false             // i.cI — attack-script done (bool)
    var cJDone = false             // i.cJ — transition ack (bool)
    var cHGrid: Array<IntArray>? = null   // i.cH — int[7][2] knife targets
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
    var cN = 0                    // g.cN — interact-gauge sub-tick (aB())
    var K = 0                     // g.K — interact-gauge anim frame (aB())
    var gQL = 0                   // g.L — grab-QTE display x (k.java:4348)
    var gQM = 0                   // g.M — grab-QTE display y (k.java:4348)
    var z = false                 // g.z — cleared on grab (c() callers)
    /** `g.cm` (g.java:15430 init 0, proven) — the mount-indicator
     *  "refresh pending" latch: set after any r98 mount-event tick
     *  (L37eb), consumed next tick when r9==0 (L37f2) to run
     *  `k.k() ? G() : U()`. Distinct from `k.cm` (touchpad flag). */
    var gcm = false
    /** `i.cU[5]` — the afterimage-trail ring (`a(true,0)`/`bP()`); each
     *  element is an (a,b) pos pair flattened to 10 ints. */
    var cU: IntArray? = null
    var cV = false                // i.cV — trail-fadeout flag
    var cW = 0                    // i.cW — trail clip workspace index
    /** `a.e` (a.java:33-42) — the anim index each trail card carries:
     *  captured `S` at arm time (`cU[i].a(this.S,-1)` i.java:19611/19615). */
    var trailAnim = -1
    /** `a.g` (a.java:110-140, proven) — the trail cards' shared ms
     *  accumulator: `b(j.g)` adds ~1/draw; frames hold `dur*40` units so
     *  dots sit on armed-S frame ~0 for the trail's short life. */
    var trailClock = 0
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

    // -- i.C() victim hit-react + g.ar() the interact action -----------------

    /** `i.Q()` (i.java:7771, proven): face the player (`av = ak > aS.ak`). */
    fun facePlayer(w: LevelCellSource) { av = ak > w.player.ak }

    /** `i.aF()` (i.java:9192, proven): true when the facing side is free —
     *  ak inside an ax51 `s`-link's ±20 edge band, or the facing-side edge
     *  cell is neither 20-solid nor 5-one-way. */
    private fun sideFree(w: LevelCellSource): Boolean {
        s?.let {
            if (it.ax == 51) {
                if (it.W[2] - 20 < ak && ak < it.W[2]) return true
                if (it.W[0] < ak && ak < it.W[0] + 20) return true
            }
        }
        val c = w.collisionCell(
            (if (av) W[2] / 20 + 1 else W[0] / 20 - 1), (W[3] + 10) / 20)
        return c != 20 && c != 5
    }

    /** `i.c(int,int,int,int)` (i.java:9053, proven): hit-anim by type —
     *  ax11 → i(r4), ax73 → i(r5) (-1 = keep); other ax → no-op. */
    fun hitAnimByType(a11: Int, a73: Int) {
        when (ax) {
            11 -> if (a11 >= 0) setAnim(a11)
            73 -> if (a73 >= 0) setAnim(a73)
        }
    }

    /** `i.g()` (i.java:1678, proven): zero h-velocity and push the victim
     *  out of the player's box on the side the player faces; when the
     *  landing cell is 20-solid or 0-void the snap reverts and a diagonal
     *  `ag/ai` knockback fires instead. The `cell >= 12` arm gates on the
     *  above-side cell. */
    fun resolvePush(w: LevelCellSource) {
        ag = 0; ai = 0
        if (sideFree(w)) return
        if (w.collisionCell(ak / 20 + if (av) 1 else -1, al / 20 - 1) >= 12) return
        val p = w.player
        if (p.av) {
            if (ak > p.ak) return
            val r04 = ak
            ak = p.X[0] - (W[2] - ak)
            val c = w.collisionCell(ak / 20, al / 20)
            if (c == 20 || c == 0) { ak = r04; ag = -2560; ai = 1280 }
        } else {
            if (ak < p.ak) return
            val r03 = ak
            ak = p.X[2] + (ak - W[0])
            val c = w.collisionCell(ak / 20, al / 20)
            if (c == 20 || c == 0) { ak = r03; ag = 2560; ai = -1280 }
        }
    }

    /** `i.K()` (i.java:5716, proven): the companion release — while the
     *  player's anim counter `aS.T <= 2`, map player S {67→0, 68→1, 69→2,
     *  106→3} onto this held entity and clear its hide flag (`P&=-129`).
     *  Called by the attack entry (g.java:4370) and the combo chain
     *  (g.java:2495); counterpart to `i()`'s `E.P|=128` hide. */
    fun heldRelease(p: Entity) {
        if (p.T > 2) return
        when (p.S) {
            67 -> { setAnim(0); P = P and -129 }
            68 -> { setAnim(1); P = P and -129 }
            69 -> { setAnim(2); P = P and -129 }
            106 -> { setAnim(3); P = P and -129 }
        }
    }

    /** `i.J()` (i.java:7002-7020, proven): the ax71 companion overlay
     *  (`k.E`) — mirrors the player's `ak`/`al` + facing bit each tick,
     *  then hides (`P|128`) once its anim has finished; while the anim
     *  still runs it stays visible unless the `j.c==21` dialog is up
     *  without the `k.u==8` skip key held. */
    fun followJ(p: Entity, world: LevelCellSource) {
        ak = p.ak
        al = p.al
        if (p.av) P = P or 1 else P = P and -2
        if (!animFinished()) {
            if (world.jC != 21 || world.padHeldWord() == 8) return
        }
        P = P or 128
    }

    /** `i.a(anim,x,y)` (i.java:9810, proven): spawn the clip-9 ax14 marker
     *  `anim` into `ae` (occupied `ae` → no-op); az=302, av=false. */
    fun spawnMarker(w: LevelCellSource, anim: Int, x: Int, y: Int) {
        if (ae != null) return
        ae = w.spawnPickup(anim, x, y).also { it.av = false }
    }

    /**
     * `i.C()` (i.java:1955, proven): the victim hit-react dispatcher,
     *  keyed on the attacker's anim `aS.S`. Dead arm first (`aB<=0`), then
     *  the weaken line `aB <= bu[au]`, then per-type reacts. */
    fun hitReact(w: LevelCellSource): Boolean {
        val p = w.player
        if (aB <= 0) {
            // L61: per-type death-flavor react (proven)
            when (ax) {
                11 -> { ab = null; setAnim(0); releaseAe() }
                73 -> { setAnim(164); ah = 0; ag = 0; aj = 0; ai = 0 }
                17 -> setAnim(69)
                50 -> setAnim(129)
                23 -> setAnim(78)
            }
            return true
        }
        if (ax == 11 && Z[0] == 1 && aB <= WEAPON_DMG[w.weaponSlot]) {
            Z[0] = 2; setAnim(144)
            p.spawnMarker(w, 45, ak, al - 85)   // aS.a(45, ak, al-85)
            return true
        }
        if (ax == 73 && Z[0] == 0 && aB <= WEAPON_DMG[w.weaponSlot]) {
            Z[0] = 3; facePlayer(w); setAnim(155)
            aq = ak + if (av) -60 else 60
            p.setAnim(8)
            w.kE?.let { it.P = it.P or 128 }         // k.E.P |= 128
            return true
        }
        if (ax == 17) {
            if (S == 68) return false
            setAnim(68); w.sfx(13); return true
        }
        if (ax == 23) {
            if (p.S == 69) {
                p.ak = ak + if (av) -45 else 45
                p.al = al
            }
            w.sfx(13); setAnim(73); return true
        }
        if (ax != 11 && ax != 73) return true   // L51→L86: other types no-op
        // L29: ax11/73 live react by attacker anim (proven)
        if (Z[0] == 2) { setAnim(6); return true }
        when (p.S) {
            67 -> { hitAnimByType(6, 156); w.sfx(13) }
            68, 69, 286 -> { resolvePush(w); hitAnimByType(6, 156); w.sfx(13) }
            287 -> {                             // falls into the L61 dead arm
                when (ax) {
                    11 -> { ab = null; setAnim(0); releaseAe() }
                    73 -> { setAnim(164); ah = 0; ag = 0; aj = 0; ai = 0 }
                }
            }
            else -> {}
        }
        return true
    }

    /**
     * `g.ar()` (g.java:4030, proven): the 65568 standing interact action —
     *  no `g` target → `i(0)` + `k.v()`; else `k.A(16)` sfx, zero velocity,
     *  and (unless `S==364`) pick the reach anim by the 8.8 rise:run ratio
     *  `r06/r05`: <=64 → 301, <=256 → 300, <=1024 → 299; target mid ≥20px
     *  below own mid → 302; `r06==0` → 301. Then `K > 3` throws the knife
     *  (`i.a(8,5,14,av,L,M,300)`) and applies the target effect: ax4 armed
     *  `S30` → `i(29)`; ax58 lever `i(S+1)` on {0,5,7,9,11} / `i(3)` on 2;
     *  else `g.aB -= (bu[au]<<1 · K)/6` damage + `g.C()` react. */
    fun interactAction(w: LevelCellSource, pad: Pad) {
        val t = g
        if (t == null) {
            setAnim(0)
            pad.clearLatches()                   // k.v()
            return
        }
        w.sfx(16)                                // k.A(16)
        aj = 0; ai = 0; ah = 0; ag = 0
        val r02 = (Y[1] + Y[3]) shr 1
        val r04 = (t.Y[1] + t.Y[3]) shr 1
        val r05 = kotlin.math.abs(t.ak - ak)
        val r06 = kotlin.math.abs(r04 - r02) shl 8
        if (S != 364) {
            if (r05 <= 0 || r06 <= 0 || r04 - r02 >= 20) {
                if (r04 - r02 > 20) setAnim(302)
                else if (r06 == 0) setAnim(301)
            } else {
                val r08 = r06 / r05
                if (r08 <= 64) setAnim(301)
                else if (r08 <= 256) setAnim(300)
                else if (r08 <= 1024) setAnim(299)
            }
        }
        val r07 = WEAPON_DMG[w.weaponSlot] shl 1 // i.bu[k.au] << 1
        if (K <= 3) return
        w.spawnProjectile(av, L, M)              // i.a(8,5,14,av,L,M,300)
        when {
            t.ax == 4 -> if (t.S == 30) t.setAnim(29)
            t.ax == 58 -> when (t.S) {
                0, 5, 7, 9, 11 -> t.setAnim(t.S + 1)
                2 -> t.setAnim(3)
                else -> {}
            }
            else -> {
                t.aB -= (r07 * K) / 6
                t.hitReact(w)                    // g.C()
            }
        }
    }

    // -- g.c(i) the grab lunge --------------------------------------------

    /**
     * `g.c(i r8)` (g.java:4115, proven) — zero velocity, pick the lunge
     *  anim (attack state → 292; S==298 keeps anim; else by the 8.8
     *  rise:run ratio r04/r03 → 272/273/274/275 arcs), then compute the
     *  per-tick step (cC,cD) that carries the player onto the target
     *  over the anim's cE frames. `F = r8` links the target for the
     *  mount/grab consumer arms (g.java:4303+; ported — mountEntry/lungeTick).
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

    /** `g.at()` (g.java:4376, proven): position on the orbit —
     * `ak = cH + cB·j.b(cy)>>8; al = cI - cB·j.b(j.n-cy)>>8`. */
    fun orbitPosition() {
        ak = cH + ((cB * Trig.sin(cy)) shr 8)
        al = cI - ((cB * Trig.sin(Trig.N - cy)) shr 8)
    }

    /**
     * `g.av()` (g.java:4733, proven) — the wall-embed unstick probe
     * called at the tail of every `au()` arm: probes cells 20px up;
     * `aO >= 20` (solid overhead) → `a(0)` fling then continue;
     * `aO < 20` or `ah >= 0` → return. While falling with `bq == 0`,
     * a ≥20 cell at the feet-side edge zeroes `ai/ag` and steps `ak`
     * back 10px. `bq != 0` skips the unstick (the `goto L27` lands on
     * shared tail code the decompiler merged — inferred).
     */
    fun wallProbe(w: LevelCellSource) {
        al -= 20; probeCells(w); al += 20
        if (aO >= 20) flingAirborne(0, w)
        if (aO < 20) return
        if (ah >= 0) return
        if (bq != 0) return                       // L27 — inferred
        refreshBoxes()
        val r0 = W[3]; val r02 = W[0] - 1; val r03 = W[2] + 1
        if (ag == 0) return
        if (ag > 0) {
            if (e(w, r03 / 20, r0 / 20) >= 20) { ai = 0; ag = 0; ak -= 10 }
        } else {
            if (e(w, r02 / 20, r0 / 20) >= 20) { ai = 0; ag = 0; ak += 10 }
        }
    }

    /** `g.aB()` (g.java:5800, proven): the interact-gauge stepper —
     * advances `K` through clip-10 anim 41 frames (anim 29 while
     * `S == 295`), `cN` sub-ticks per frame, and pins `L/M` onto
     * `g`'s W box. No-op while `g` or clip-10 is missing. */
    fun interactGauge(w: LevelCellSource) {
        val t = g ?: return
        val clip = w.clipFor(10) ?: return          // k.z[10] L22
        if (clip.frameDuration(41, K) == 0) return  // L10
        if (cN < clip.frameDuration(41, K)) { cN++; return }
        K++; cN = 0
        var r6 = clip.frameCount(41)
        if (S == 295) r6 = clip.frameCount(29)
        if (K >= r6) K = 0                          // L17/L19
        L = (t.W[0] + t.W[2]) shr 1
        M = ((t.W[1] + t.W[3]) shr 1) - 10
    }

    // -- g.au() the mounted-orbit FSM (g.java:4389) -------------------------

    /**
     * `g.au()` (g.java:4389, proven) — the mounted/orbit tick for
     * S∈{277,293} and the L1863/L1889 tails. Same F-bind as `as()`.
     * `cJ/cK` = drag anchor (player pos, or the X-box point for
     * Z0==4 carts); `cH/cI` re-pins to the mount center each tick.
     * `cy < n` fall-throughs in the source (L43/L61/L93) are
     * decompiler merges of the `cy -= cx` clamp — ported as such
     * (inferred).
     */
    fun mountOrbitTick(w: LevelCellSource, pad: Pad) {
        val mount = Entity.at
        val bound = g
        F = when {                                    // same bind as as()
            mount != null && (bound == null || !inFrontOf(bound)) -> mount
            bound != null && bound.ax == 11 && bound.Z[19] == 1 &&
                !bound.deadRelease() -> bound
            else -> null
        }
        val f = F ?: run { flingAirborne(0, w); return }  // L22 a(0)
        if (f.ax == 72 && f.Z[0] == 4) { cJ = X[0]; cK = X[1] }  // L28
        else { cJ = ak; cK = al }                       // L30
        cH = f.ak; cI = (f.W[1] + f.W[3]) shr 1         // L31
        if (f.ax != 72) {
            // L190/L194/L200 — a dragged ax11/17 victim: S293 sinks the
            // player 6px/tick until `cB >= 15` then flings off at W[3];
            // otherwise applies the orbit drag velocity each tick.
            if (f.ax != 11 && f.ax != 17) return
            if (S == 293) {
                cB += 6; al += 6
                if (cB >= 15) { al = W[3]; flingAirborne(0, w) }
                cK = al
            } else {
                ag = -(cF shr 8) * Trig.sin(cy)
                ah = (cF shr 8) * Trig.sin(Trig.N - cy)
            }
            return
        }
        when (f.Z[0]) {
            0 -> orbitSwing(w, f)                     // L35
            3 -> orbitSlide(w, f)                     // L53
            1, 2 -> orbitWheel(w, f)                  // L69/L107/L151
            4 -> orbitCart(w, f, pad)                 // L162
            else -> { }                               // L188
        }
    }

    /** `au()` L35-L51 (Z0==0, proven): pendulum swing — `cB` decays
     * `cF>>8`/tick, `cy` pulled into the `(n,o)` band by `±cx`; on
     * W-overlap with the mount → `i(22)` dismount launch
     * `ag = -(cF>>8)·j.b(cy)`, `ah = (cF>>8)·j.b(n-cy)`, collide,
     * wall-zero `ag`, `F = null`; then `av()`. */
    private fun orbitSwing(w: LevelCellSource, f: Entity) {
        cB -= cF shr 8
        if (cy in (Trig.N + 1) until Trig.O) cy += cx else cy -= cx
        orbitPosition()
        if (overlapI(W, f.W)) {
            setAnim(22)
            ag = -(cF shr 8) * Trig.sin(cy)
            ah = (cF shr 8) * Trig.sin(Trig.N - cy)
            val r0 = ak
            ak += ag shr 8
            collideSides(w, false)
            if (hitWall()) ag = 0
            ak = r0
            refreshBoxes()
            F = null
        }
        wallProbe(w)
    }

    /** `au()` L53-L66 (Z0==3, proven): same swing; W-overlap just
     * unlinks `F` and `i.at` (no launch). */
    private fun orbitSlide(w: LevelCellSource, f: Entity) {
        cB -= cF shr 8
        if (cy in (Trig.N + 1) until Trig.O) cy += cx else cy -= cx
        orbitPosition()
        if (overlapI(W, f.W)) { F = null; Entity.at = null }
        wallProbe(w)
    }

    /**
     * `au()` L69/L81/L96/L107/L151 (Z0==1/2, proven) — the wheel orbit:
     * cM==0 spin-in — `cB` decays with a 75 floor until `|cy-o| <= cx`
     * → `cM=1`, `cL = 16·m/360` (Z0==1) or `35·m/360` (Z0==2, which
     * also pins `cB = 75`); cM==1 oscillates `cy` by ±10 (Z0==2) or
     * ±5 inside `(o±cL)` — clamped edges toggle `cG` and Z0==2 flings
     * the player `ag=±3328, ah=-6656, i(243)`; Z0==1 decays `cL` to 0
     * → `i(293)`, `cM=2`, `cy = o`. S==293 + Z0==1 rides the zipline:
     * `cB+=6, al+=6`, `cB >= Z[3]` → `al=W[3]; a(0)` (L153).
     */
    private fun orbitWheel(w: LevelCellSource, f: Entity) {
        if (cM == 0) {
            cB -= cF shr 8                            // L69/L76 floor 75
            if (cB < 75) cB = 75
            if (f.Z[0] == 2) cx = 15 * Trig.M / 360
            if (cy in (Trig.N + 1) until Trig.O) cy += cx else cy -= cx
            if (Math.abs(cy - Trig.O) <= cx) {        // L96 top band
                cL = if (f.Z[0] == 2) 35 * Trig.M / 360 else 16 * Trig.M / 360
                cM = 1
                if (f.Z[0] == 2 && cB > 75) cB = 75   // L101-L103 cap
            }
        } else if (cM == 1) {
            if (cy < Trig.O - cL || cy > Trig.O + cL) {      // L113 band
                cy = if (cy < Trig.O) Trig.O - cL else Trig.O + cL
                cG = !cG                                    // L119-L122
                if (f.Z[0] == 2) {                          // edge fling
                    ag = if (av) -3328 else 3328
                    ah = -6656
                    setAnim(243)
                    return
                }
            }
            if (f.Z[0] == 1) cL--                           // L131
            if (f.Z[0] == 2) cy += if (cG) -10 else 10      // L134
            else cy += if (cG) -5 else 5                    // L140
            if (cL <= 0 && S != 293) {                      // L146
                setAnim(293); cM = 2; cy = Trig.O; orbitPosition()
            }
        }
        // L151/L153 — S293 rides the Z0==1 zipline
        if (S == 293 && f.Z[0] == 1) {
            cB += 6; al += 6
            if (cB >= f.Z[3]) { al = W[3]; flingAirborne(0, w) }
            cK = al
            return
        }
        orbitPosition()                                   // L158
        cJ = ak; cK = al                                  // L159
    }

    /**
     * `au()` L162-L186 (Z0==4, proven) — the track cart: hand indicator
     * to view center when `!k.k()`; context press/hold — or, when not
     * mounted, the indicator sitting on the touch point — applies the
     * `cB -= (cF>>8)>>2` brake; the cart then rides
     * `F.ak = cJ - cB·j.b(cy)`, `F.al = cK + cB·j.b(n-cy)` and drags
     * the `F.ac` track entity along `aq/ar` behind it. Track `S∈{2,3}`
     * or `cB < 20` detaches: `ac=null; F.P|=160; Z[4]=-1; F=null;
     * i(0); a(false)` + `G()`/`U()` indicator release; `av()`.
     */
    private fun orbitCart(w: LevelCellSource, f: Entity, pad: Pad) {
        if (!w.mounted) moveHand(w, 200 + w.kO, 120 + w.kP)      // L162
        val brake = pad.v(Pad.M_CONTEXT) || pad.u(Pad.M_CONTEXT) ||
            (!w.mounted && indicatorNearTouch(w))               // L167→L172
        if (brake) cB -= (cF shr 8) shr 2
        val r03 = (cB * Trig.sin(cy)) shr 8                     // L173
        val r04 = (cB * Trig.sin(Trig.N - cy)) shr 8
        f.ak = cJ - r03
        f.al = cK + r04
        val ac = f.ac ?: run { wallProbe(w); return }           // L186
        if (ac.S == 2 || ac.S == 3 || cB < 20) {                // L178→L181
            f.ac = null
            f.P = f.P or 160
            f.Z[4] = -1
            F = null
            setAnim(0)
            collideSides(w, false)
            if (w.mounted) releaseAe() else dropIndicator(w)    // L184 G()/U()
            wallProbe(w)
            return
        }
        ac.ak = f.ak - ac.aq                                    // L178 drag
        ac.al = f.al - ac.ar
        wallProbe(w)
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

    /** `i.y()` (i.java:1211, proven): wall flag in the motion
     *  direction — `ag<0→bb; ag>0→bc; else facing (av→bb else bc)`. */
    fun forwardWall(): Boolean = when {
        ag < 0 -> bb
        ag > 0 -> bc
        av -> bb
        else -> bc
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
    fun integrate(div: Int = 1) {
        N += (ak - (N shr 8)) shl 8
        N += ag / div
        ag += ai / div
        ai = 0
        ak = N shr 8
        O += (al - (O shr 8)) shl 8
        O += ah / div
        ah += aj / div
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
        // clipless entities keep W as staged at spawn — the JAR never runs
        // t() on one (bi[] always assigns a clip); records/tests set W directly.
        if (c == null) return
        if (S < 0 || T < 0) { W.fill(0); X.fill(0); Y.fill(0); return }
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
        // ax60 tail (i.java:543-581, proven): the lift's travel-bound edge
        // is stretched to Z[5] — the init-probed solid. S9/10 → top,
        // S16/17 → bottom, S13/15 → left, S11/14 → right. Most clip-71
        // anims have an empty base rect, so W becomes a zero-width/-height
        // strip covering the whole travel corridor.
        if (ax == 60) when (S) {
            9, 10 -> W[1] = Z[5]
            16, 17 -> W[3] = Z[5]
            13, 15 -> W[0] = Z[5]
            11, 14 -> W[2] = Z[5]
        }
    }

    /**
     * `e(cx, cy)` = `i.e(int,int)` (i.java:14828, proven): OOB
     * `cx<0 || cx>=k.bp || cy>=k.bq → 20`; ax0 (player) override arms:
     * `k.aS.m()` (standing on an ax51 crate or `i.bq` crate-top level
     * set) → rows outside the feet-band `{i3-1,i3,i3+1}` read empty;
     * `S∈{37,257}` (vault/climb) → solid-20 cells read empty so the
     * player passes through mid-move. Else `k.g`.
     */
    fun e(world: LevelCellSource, cx: Int, cy: Int): Int {
        val i3 = (W[3] + 1) / 20
        if (cx < 0 || cx >= world.kBp || cy >= world.kBq) return 20
        if (ax == 0) {
            if ((standingOn?.ax == 51 || entBq != 0) &&
                cy != i3 && cy != i3 - 1 && cy != i3 + 1) return 0
            if ((S == 37 || S == 257) && world.collisionCell(cx, cy) == 20) return 0
        }
        return world.collisionCell(cx, cy)
    }

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
        val iX = probeCells(world)
        var i = W[0] - 1; var i2 = W[2] + 1
        var i3 = W[1]; var i4 = W[3] - 10
        // L8: player crouch states extend the strip down another 10.
        if (ax == 0 && (S == 12 || S == 7 || S == 32 || S == 199)) i4 -= 10
        bb = false; bc = false; ba = false; aT = 0; aU = 0
        if (resolve) {
            if (bd) {
                // L16-L28: ground-strip pre-adjust when embedded.
                if (aR >= 10 || aR == 5) al -= iX
                else if (aO >= 12 && aO != 23) { ba = true; al += (20 - (i3 % 20)) + 1 }
                else if ((aQ >= 12 || aQ == 5) && aO != 23) { ba = true; al += (10 - (i3 % 20)) + 4 }
            }
            refreshBoxes()                                     // t()
            i = W[0] - 1; i2 = W[2] + 1; i3 = W[1]; i4 = W[3] - 10
        }
        val iE = e(world, i / 20, i3 / 20 - 1)
        val iE2 = e(world, i2 / 20, i3 / 20 - 1)
        var i5 = i3 / 20
        while (i5 <= i4 / 20) {
            val iE3 = e(world, i / 20, i5)
            if (iE3 > aT) {
                aT = iE3
                if (aT >= 18) {
                    aX = if (iE >= 18) (i4 / 20) - (i3 / 20 - 1) + 1 else (i4 / 20) - i5 + 1
                    bb = true
                }
            }
            val iE4 = e(world, i2 / 20, i5)
            if (iE4 > aU) {
                aU = iE4
                if (aU >= 18) {
                    aY = if (iE2 >= 18) (i4 / 20) - (i3 / 20 - 1) + 1 else (i4 / 20) - i5 + 1
                    bc = true
                }
            }
            if (bb || bc) break
            i5++
        }
        if (resolve && v) {
            if (bb == bc) { bc = false; bb = false }
            else if (ag <= 0) {
                if (bb) { ak += (20 - ((i + 20) % 20)) - 1; probeCells(world) }
                else { ak -= i2 % 20; probeCells(world) }
            } else if (bc) {
                ak -= i2 % 20; probeCells(world)
            } else {
                ak += (20 - ((i + 20) % 20)) - 1; probeCells(world)
            }
        }
        refreshBoxes()                                         // t()
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
     * `a()` — i.java `private void a()` :914-993 (proven). The push/contact
     * resolution run by ax9 (and S131/146 callers) against `k.aS`:
     *  - early-outs: S139 corpse; player S6 roll vs ax11; `g.a.ax==43`
     *    grapple claim; S18 without a crouching player (S12);
     *  - S131/146 jump straight to the L25 tail;
     *  - L29: `W` vs `aS.W` overlap gate → `g.a != null` out →
     *  - L34/L40: ax15 grapple anchor — `g.b(aS.S)` free-anim gate, snap
     *    `aS` onto the nearer edge (`ak = W[2]|W[0]`, `al = W[1]+1`),
     *    `aS.i(209)`, release `ac`, `aC=0`, `g.a = this`;
     *  - `aS.S > 43` (airborne) skips the push arms;
     *  - L50 left-block: player at-or-left not moving left and no wall →
     *    snap to the entity's left edge, `ai=0`, `ag=-1`, `a(true)`, `ag=0`;
     *  - L57 right-block mirrors it with `ag=1`, then falls into L25;
     *  - L25/L27: while `aS.S==12 && aS.g(this)` re-eval the L29 body.
     */
    fun pushContact(world: LevelCellSource) {
        val p = world.player
        if (S == 139) return                                   // corpse
        if (p.S == 6 && ax == 11) return
        if (p.ga != null && p.ga!!.ax == 43) return            // g.a claim
        if (S == 18 && p.S != 12) return
        fun body(): Boolean {                                  // L29-L63
            if (!overlapStrict(p.W, W)) return false
            if (p.ga != null) return false                     // L32
            if (ax == 15 && (S == 6 || S == 8) && p.gB()) {    // L40
                p.ag = 0; p.ah = 0
                p.setAnim(209)
                p.ac = null                                    // aS.a(null)
                aC = 0
                p.ak = if (p.ak - ak > 0) W[2] else W[0]       // L44/L45
                p.al = W[1] + 1
                p.ga = this                                    // g.a = this
                return false
            }
            if (p.S > 43) return false                         // L48
            if (p.ak <= ak && p.ag >= 0 && !p.hitWall()) {     // L50
                p.ak = ak - ((p.W[2] - p.W[0]) / 2) - ((W[2] - W[0]) / 2)
                p.ai = 0; p.ag = 0; p.ag = -1
                p.collideSides(world, true); p.ag = 0          // L63
                return false
            }
            if (p.ak > ak && p.ag <= 0 && !p.hitWall()) {      // L57
                p.ak = ak + ((p.W[2] - p.W[0]) / 2) + ((W[2] - W[0]) / 2)
                p.ai = 0; p.ag = 0; p.ag = 1
                return true                                    // → L25
            }
            p.collideSides(world, true); p.ag = 0              // L63
            return false
        }
        val reachedL25 = if (S == 131 || S == 146) true else body()
        while (reachedL25 && p.S == 12 && p.inFrontOf(this)) { // L25/L27
            if (!body()) return
        }
    }

    /**
     * `l(int)` — i.java `private void l(int)` :21198 (proven): the ax9
     *  overlay child — spawns `a(43,31,r8,az+1)` into `ad` on first use,
     *  then mirrors `i(r8)` anim + `ak/al/T` every call. ax43 + clip31.
     */
    fun adChildOverlay(world: LevelCellSource, r8: Int) {
        if (ad == null) ad = spawnChildFx(world, 43, 31, r8, az + 1)
        ad!!.setAnim(r8)
        ad!!.ak = ak; ad!!.al = al; ad!!.T = T
    }

    /**
     * `aM()` (i.java:10100, proven): ground-support probe for the
     * grapple-volume body — `t()` then sample the cells under both
     * bottom corners of `W`; true when either is `>= 12` (solid/one-way
     * family) or `== 5` (soft support).
     */
    fun supportedByGround(world: LevelCellSource): Boolean {
        refreshBoxes()
        val l = world.collisionCell(W[0] / 20, (W[3] + 1) / 20)
        val r = world.collisionCell(W[2] / 20, (W[3] + 1) / 20)
        return l >= 12 || r >= 12 || l == 5 || r == 5
    }

    /**
     * `e(int)` (i.java:10119, proven): hang the player on this entity's
     * nearer edge — used by ax15 (`r1 = S ∈ {6,8}`, so `r8` is always
     * false → `aS.i(108)` hang) and by the `S==10` arm (`r8` → `i(109)`).
     * First pushes the player horizontally clear of the body, then —
     * only when `aC > 4 || r8` and the cell above the block is empty —
     * restarts own anim, kills player velocity, enters the hang anim,
     * releases the held link, snaps `ak` to the nearer edge, claims
     * `g.a = this`.
     */
    fun hangOnEdge(world: Level0World, r7: Int) {
        val r8 = r7 == 10
        val p = world.player
        p.refreshBoxes()
        if (p.ak - ak < 0) {
            p.ak = ak - ((W[2] - W[0]) shr 1) - (p.W[2] - p.ak)
        } else if (p.ak - ak > 0) {
            p.ak = ak + ((W[2] - W[0]) shr 1) + (p.ak - p.W[0])
        }
        if (aC <= 4 && !r8) return
        if (world.collisionCell(ak / 20, W[1] / 20 - 1) != 0) return
        setAnim(r7)
        p.ag = 0; p.ah = 0
        p.setAnim(if (r8) 109 else 108)
        p.ac = null
        aC = 0
        p.ak = if (p.ak - ak > 0) W[2] else W[0]
        p.ga = this
    }

    /**
     * `bt()` (i.java:16669, proven): moving-contact sweep — when `ah != 0`
     * (falling block), every `bb[]` entity of ax ∈ {17,11,23,50} that is
     * `P()`-sweepable and overlaps `W` gets `as()`-ed. (`bb`/`bc` =
     * registration pool = `w.npcs`.)
     */
    fun sweepHostiles(world: Level0World) {
        if (ah == 0) return
        for (n in world.npcs) {
            if (n.ax != 17 && n.ax != 11 && n.ax != 23 && n.ax != 50) continue
            if (!n.deadRelease()) continue                       // P() + G()
            if (!overlapStrict(W, n.W)) continue
            n.sweepReact()
        }
    }

    /** `as()` (i.java:7680, proven): sweep reaction — `aB = 0`, then
     *  ax11 → `i(0)`, ax17 → `i(69)`, ax23 → `i(79)`. */
    fun sweepReact() {
        aB = 0
        when (ax) {
            11 -> setAnim(0)
            17 -> setAnim(69)
            23 -> setAnim(79)
        }
    }

    /**
     * `av()` — g.java `void av()` (i.java:4984, proven). Air wall-resolve:
     * probes one cell higher (head region); `aO>=20 → a(0)` ceiling drop;
     * `aO<20 || ah>=0 || i.bq!=0 → return` — the resolve only applies on
     * the 12..19 partial-platform band while not crate-perched; then
     * pushes ak back ±10 when flying into a solid side cell at the feet
     * row.
     */
    fun airWallResolve(world: LevelCellSource) {
        al -= 20
        probeCells(world)
        al += 20
        if (aO >= 20) enterFall()                // a(0) — no return, verbatim
        if (aO < 20 || ah >= 0 || entBq != 0) return
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
     *      (`g.b(S)`); `g.a()` = `playerDamageable` (pays the meter) and
     *      the victim `r9.c(r13)` hit-reacts on the attacker; `L423 →
     *      k.A(18)` hurt-mark sfx (see the op-4 arm at :3330).
     *   18 `i(43)` — hit interrupt into tumble.   20 `i(43)` — knockdown.
     *   26 launch: `av=attacker.av; ag=±4096; ah=-4096; aj=1536; a(43,32)`
     *   29 stumble: `av=attacker.av; i(10); ag=∓1536`
     *   34 damage-mark: zero vel + `a(8,5,14,…)` floatie + `k.A(11)` sfx
     *      (floatie spawner slice 160 `i.d()`; sfx slice 166 `e.a()`).
     * `d(int)` (g.java:3884) drains the meter: gates on busy/lock states,
     * `x[1]-=r5` clamped at 0; at 0 (non-flying `bh[aj]!=3`) the player is
     * knocked out: `bl=0; G(); H()` (detach links) or `E()` ground-snap —
     * then `k.l(12)` mission-fail. Ported as `dead` flag → world respawn.
     */
    /**
     * `i.c(iVar)` (i.java:3398-3432, proven): hit-react — release `g.b`,
     * spawn the damage floatie `a(8,5,14,av,ak,midY,300)` (plus the
     * `k.bK`-gated `a(8,59,0,...)` flash marker), face the attacker for
     * ax∈{11,17,23,50,73}, push `ag=±1536` away — cancelled by the
     * `y()||aF()||e(next,al/20)>=12` guard chain — then `i(0); t()` and
     * the `k.R`/`k.S` camera-wall clamp, ending `i(9)` (`i(6)` vs ax61
     * while `Q==6`).
     */
    fun counteredBy(attacker: Entity, w: LevelCellSource) {
        w.playerLinkB = null                                  // g.b = null
        val midY = (W[1] + W[3]) shr 1
        spawnFx8(w, 5, 14, av, ak, midY, 300)                 // a(8,5,14,…)
        if (w.kBK) spawnFx8(w, 59, 0, av, ak, midY, 300)    // k.bK flash
        if (attacker.ax == 11 || attacker.ax == 17 || attacker.ax == 23 ||
            attacker.ax == 50 || attacker.ax == 73)
            av = attacker.ak < ak
        ag = if (av) 1536 else -1536
        val i = if (ag < 0) ak / 20 - 1 else ak / 20 + 1
        if (forwardWall() || sideFree(w) || e(w, i, al / 20) >= 12) ag = 0
        setAnim(0)                                          // i(0)
        refreshBoxes()                                      // t()
        val i3 = w.kR + (W[2] - W[0])
        val i4 = w.kSBound - (W[2] - W[0])
        if (ak + (ag shr 8) <= i3 && w.kR > 0) { ag = 0; ak = i3 }
        else if (ak + (ag shr 8) >= i4 && w.kSBound > 0) { ag = 0; ak = i4 }
        if (attacker.ax == 61 && Q == 6) setAnim(6) else setAnim(9)
    }

    /**
     * `g.d(int)` meter drain (proven, g.java:3884): skips while `s`
     * (cheat/debug toggle — deliberately unported), `t != 0` iframes,
     * `c()` linked-carry, or
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

    /** `i.a(int,int,int)` (i.java:9810, proven): bind `ae` to a fresh
     *  ax14/clip9 marker (anim `n`, az=302, (x,y), av=false) — no-op
     *  while an `ae` is already bound. */
    fun spawnAeMarker(w: LevelCellSource, anim: Int, x: Int, y: Int) {
        if (ae != null) return
        ae = w.spawnPickup(anim, x, y)
    }

    /** `i.ab()` (i.java:20564, proven): the k.a claim is actively
     *  working — `ca >= 0` (a counter bound) && `!cd[0]` (the flag bit
     *  clear) && `cK >= 0` (not the -1/-2 terminal latch). */
    fun claimActive(): Boolean = ca >= 0 && !cd[0] && scriptStep >= 0

    /** `i.ab()` (i.java:20564-20577, proven): bound-claim LIVE — a
     *  counter bound (`ca>=0`), the claim flag bit set (`cd[0]==true`),
     *  and a live claim step (`cK>=0`). Read by the L777 tail
     *  (`k.C.ab()`) to suppress aB() melee while a script claim runs —
     *  note cd[0] is INVERTED vs claimActive()'s `!cd[0]`. */
    fun claimLive(): Boolean = ca >= 0 && cd[0] && cK >= 0

    /** `g.c()` (g.java:421, proven): player mid-combo anims
     *  {112, 113, 114, 115} — `k.m` early-returns while true. */
    fun aSC(): Boolean = S == 112 || S == 113 || S == 114 || S == 115

    /** `i.A()` (i.java:940, proven): the facing-side column holds a
     *  cell ==21 (ladder/vine) anywhere in the box's row span — the
     *  S12/S33 arms snap into `i(74)` when this fires. */
    fun ladderCell(w: LevelCellSource): Boolean {
        val i = if (av) W[0] / 20 - 1 else W[2] / 20 + 1
        for (r in W[1] / 20..W[3] / 20) if (e(w, i, r) == 21) return true
        return false
    }

    /** `i.z()` (i.java:928, proven): every cell in the push-direction
     *  side column (`ag<0` left, else right) is ≥19 — a wall face. */
    fun pushColumnBlocked(w: LevelCellSource): Boolean {
        val i = if (ag < 0) W[0] / 20 - 1 else W[2] / 20 + 1
        for (r in W[1] / 20..W[3] / 20) if (e(w, i, r) < 19) return false
        return true
    }

    /** `g.ak()` (g.java:187, proven): wall-ledge lip grab — the facing
     *  column's cell at hand-row `(W[1]+10)/20` is ≥19 AND the 5-cell
     *  pocket beyond it (i3 column, rows i4-1..i4+2 plus i2/i4-1) is
     *  empty → snap onto the lip and enter `i(60)` hang. `Q==61`
     *  blocks outright. */
    /**
     * `i.al()` (g.java:209-239, proven) — the wider ledge-mount probe:
     * column one cell further out than `ledgeLipGrab` (W[0]-20 / W[2]+20)
     * must hold a ≥19 cell at hand row with the 2×4 pocket beyond clear.
     * Snap: `ak = i2*20 (+20 when av)`, `al = i4*20 - 1`. Cell 21
     * (ladder) → bare `true` and no state change (the caller's mount
     * stays deferred); otherwise `k.v()` + `i(61)` hang + `aC=40`.
     */
    /** `i.M()` (i.java:5849, proven): feet-level cell one column into
     *  the facing direction is solid (≥12) or "≥5" (verbatim — the
     *  second conjunct subsumes the first). */
    fun floorAhead(w: LevelCellSource): Boolean {
        val iE = e(w, ak / 20 + (if (av) -1 else 1), al / 20)
        return iE >= 12 || iE >= 5
    }

    fun ledgeHangGrab(w: LevelCellSource): Boolean {
        val i4 = (W[1] + 10) / 20
        val i2: Int; val i3: Int
        if (av) { i2 = (W[0] - 20) / 20; i3 = i2 + 1 }
        else { i2 = (W[2] + 20) / 20 + 1; i3 = i2 - 1 }
        val iE = e(w, i2, i4)
        if (iE < 19 || e(w, i2, i4 - 1) > 0 || e(w, i3, i4) > 0 ||
            e(w, i3, i4 - 1) > 0 || e(w, i3, i4 + 1) > 0 || e(w, i3, i4 + 2) > 0) {
            return false
        }
        ak = if (av) i2 * 20 + 20 else i2 * 20
        al = i4 * 20 - 1
        if (iE == 21) return true
        w.clearLatches()
        setAnim(61)
        aC = 40
        return true
    }

    /** `i.H()` (i.java:3684, proven) — release the `ab` held link
     *  (`ab.p()` + `ab = null`); the drop-half of the `cu && v(33024)`
     *  arm in the grounded tail. */
    fun dropHeld() {
        ab?.releaseCascade()
        ab = null
    }

    fun ledgeLipGrab(w: LevelCellSource): Boolean {
        val i4 = (W[1] + 10) / 20
        val i2: Int; val i3: Int
        if (av) { i2 = (W[0] - 5) / 20; i3 = i2 + 1 }
        else { i2 = (W[2] + 5) / 20; i3 = i2 - 1 }
        if (e(w, i2, i4) < 19 || Q == 61 || e(w, i2, i4 - 1) > 0 ||
            e(w, i3, i4) > 0 || e(w, i3, i4 - 1) > 0 ||
            e(w, i3, i4 + 1) > 0 || e(w, i3, i4 + 2) > 0) return false
        ak = if (av) i2 * 20 + 20 else i2 * 20
        al = i4 * 20 - 1
        setAnim(60)
        return true
    }

    /** `i.f(int,int)` (i.java:6852, proven): the alternating-mash QTE
     *  meter — `bm` latches the last-pressed mask; only the OTHER mask's
     *  edge adds +8 to `bl`, absence decays -1/tick; `bl>=10` wins and
     *  resets. The r5/r6 args are ignored in the original (masks
     *  hardcoded 4112 LEFT / 8256 RIGHT). */
    fun mashQte(w: LevelCellSource): Boolean {
        if (bl <= 0) bl = -1
        if (bm != 4112 && w.padHeld(4112)) { bm = 4112; bl += 8 }
        else if (bm == 8256) bl--
        else if (w.padHeld(8256)) { bm = 8256; bl += 8 }
        else bl--
        if (bl < 0) bl = 0
        if (bl >= 10) { bl = 0; return true }
        return false
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
     * `i.b()` (i.java:1268-1279, proven): corner-support probe — refresh
     * boxes (`t()`) then read the four W-corner cells into the scratch
     * fields `aT` (top-left) / `aU` (top-right) / `aV` (bottom-left) /
     * `aW` (bottom-right); true iff ANY corner cell `>= 12` (solid). The
     * S85 hit-react uses it to skip the freeze+kick when fully over a pit.
     * NOT a LOS/attack probe — misnamed in older notes.
     */
    fun cornerSupported(w: LevelCellSource): Boolean {
        refreshBoxes()
        val left = W[0]; val right = W[2]; val top = W[1]; val bot = W[3]
        aT = e(w, left / 20, top / 20)
        aU = e(w, right / 20, top / 20)
        aV = e(w, left / 20, bot / 20)
        aW = e(w, right / 20, bot / 20)
        return aT >= 12 || aU >= 12 || aV >= 12 || aW >= 12
    }

    /**
     * `i.g(int,int)` (i.java:6881-6896, proven): mash gauge — `bl += 8`
     * on `k.v(mask)` EDGE (a press), `bl--` otherwise; clamps `<0→0` and
     * `>=80→80` returning true at the cap. r6 arg is dead in the original.
     */
    fun mashGauge(mask: Int, w: LevelCellSource): Boolean {
        if (w.padHeld(mask)) bl += 8 else bl--   // k.v = EDGE presses (bB)
        if (bl < 0) bl = 0
        if (bl >= 80) { bl = 80; return true }
        return false
    }

    /**
     * `i.y()` (i.java:1211-1217, proven): travel-side wall flag —
     * `ag<0→bb` (left wall), `ag>0→bc` (right wall), `ag==0→` facing side
     * (`av→bb`, `!av→bc`).
     */
    fun yWall(): Boolean = when {
        ag < 0 -> bb
        ag > 0 -> bc
        else -> if (av) bb else bc
    }

    /**
     * `i.aF()` (i.java:9192-9219, proven): ledge-edge probe — true when
     * `standingOn` is an ax51 crate (`crateEdge`), else the foot cell at
     * the facing edge is `∈{20,5}`: `e(W[2]/20+1,(W[3]+10)/20)` when `av`,
     * `e(W[0]/20-1,·)` when `!av`.
     */
    fun aF(w: LevelCellSource): Boolean {
        if (standingOn?.ax == 51 || entBq != 0) return true
        val cy = (W[3] + 10) / 20
        val cx = if (av) W[2] / 20 + 1 else W[0] / 20 - 1
        val c = e(w, cx, cy)
        return c == 20 || c == 5
    }

    /**
     * `i.aG()` (i.java:9221-9251, proven): `aF()` mirror — the off-facing
     * edge (av polarity flipped).
     */
    fun aG(w: LevelCellSource): Boolean {
        if (standingOn?.ax == 51 || entBq != 0) return true
        val cy = (W[3] + 10) / 20
        val cx = if (av) W[0] / 20 - 1 else W[2] / 20 + 1
        val c = e(w, cx, cy)
        return c == 20 || c == 5
    }

    /**
     * `i.aI()` (i.java:9284-9336, proven): victim throws the player out
     * of the grab — on `a(aS.W, W)` overlap the player flings
     * (`ag=∓3328`, `ah=-6656`, `i(243)`) while self recoils
     * (`ag=0`, `ah=5120`, `aj=1536`), then the cell-20 edge nudge and
     * `i(184)` + `aB=0`. Returns true when the throw fired.
     */
    fun throwFromGrab(w: LevelCellSource): Boolean {
        val p = w.player
        if (!overlapStrict(p.W, W)) return false
        p.ag = if (p.av) -3328 else 3328                 // L7-L8 (aS.av)
        p.ah = -6656
        p.setAnim(243)
        ag = 0; ah = 5120; aj = 1536
        if (e(w, ak / 20, al / 20) == 20) {              // L11-L24 edge nudge
            val leftHalf = (ak % 20) <= 10               // r02
            val side = e(w, ak / 20 + if (leftHalf) -1 else 1, al / 20)
            if (side == 0) ak = (ak / 20) * 20 + if (leftHalf) -1 else 1
        }
        setAnim(184)
        aB = 0
        return true
    }

    /** `i.g(i)` (i.java:7758, proven): is `o` on my facing side —
     *  `r0 = o.ak < ak` (other left) then `r0 == av`; with this port's
     *  `av` = "facing/mirroring left" that reads "other is in front". */
    fun faces(o: Entity): Boolean = (o.ak < ak) == av

    /** `i.T()` (i.java:9879, proven): marker alive — `ae` exists, is the
     *  clip-74 prompt clip, and `S ∈ {0,1}`. Our markers spawn on clip 9
     *  (`spawnPickup`) so the clip-identity check is dropped (`inferred` —
     *  same effect: `ae` is always the marker entity). */
    fun markerAlive(): Boolean = ae != null && ae!!.S in 0..1

    /** `i.o(int,int)` (i.java:9825, proven): park the last marker point —
     *  class statics `L`/`M` consumed by `b(x,y)` (:9829). */
    fun markerPoint(x: Int, y: Int) { markerLx = x; markerLy = y }

    /**
     * `i.d(int,int)` (i.java:9856, proven): move the `ae` marker to (x,y);
     *  when `T()` re-park `o()`; then pick anim 1 when the touch point is
     *  within 70px of the marker on screen (`k.a(J,K, x-O, y-P, 70)`),
     *  else 0.
     */
    fun moveMarker(w: LevelCellSource, x: Int, y: Int) {
        val m = ae ?: return
        m.ak = x; m.al = y
        if (markerAlive()) markerPoint(x, y)
        m.setAnim(if (w.touchNearView(m, 70)) 1 else 0)
    }

    /** `i.V()` (i.java:9903, proven): marker-touch check — `ae` live and
     *  `k.a(k.H,k.I, ae.ak-k.O, ae.al-k.P, 70)`. */
    fun markerTouched(w: LevelCellSource): Boolean {
        val m = ae ?: return false
        return w.touchNearView(m, 70)
    }

    /** `i.as()` (i.java:7680, proven): instant kill — `aB=0` plus the
     *  death-anim map {11→i(0), 17→i(69), 23→i(79)}. Other ax types get
     *  aB=0 with no anim change. */
    fun instantKill() {
        aB = 0
        when (ax) {
            11 -> setAnim(0)
            17 -> setAnim(69)
            23 -> setAnim(79)
        }
    }

    /**
     * `i.l(i)` (i.java:13369, proven): ax13 rope-arc placement — parks `t`
     * at the rope's `bN`-segment arc tip and zeroes its velocity word.
     * `bP` is the pendulum angle in 8.8; `r02 = bP>>8` in angle-256.
     */
    fun ropeArcPlace(t: Entity) {
        val r0 = bN * 3072
        val r02 = bP shr 8
        val r03 = (r0 * Trig.sin(Trig.N - r02)) shr 8   // j.b(j.n-θ) = cos
        val r04 = (r0 * Trig.sin(r02)) shr 8            // j.b(θ) = sin
        t.N = N + r03
        t.O = O + r04
        t.ak = t.N shr 8
        t.al = t.O shr 8
        t.aj = 0; t.ai = 0; t.ah = 0; t.ag = 0
    }

    /**
     * `g.j()` (g.java:4775, proven): rope release — dismount with the
     * aG-variant arc: aG==1|4 → `i(23)` leap (aG4 `Z[3]!=0 → av=true`),
     * facing ±2048 horizontal + `ah=-2560`; other aG → `i(43)` fall
     * (±2048, `ah=-3840`, `y()` climb-check cancels ag). aG==2 also
     * `k.aS.H()`. Unlinks `bM` both ways + `aA &= -65`.
     */
    fun releaseRope(w: LevelCellSource) {
        val b = bM ?: return
        if (b.aG == 1 || b.aG == 4) {
            setAnim(23)
            if (b.aG == 4) av = b.Z[3] != 0
            ag = if (av) -2048 else 2048
            ah = -2560
        } else {
            setAnim(43)
            ag = if (av) -2048 else 2048
            ah = -3840
            if (climbCheck()) ag = 0                     // y()
        }
        if (b.aG == 2) consumeH()                                      // k.aS.H()
        b.aA = 0; b.bM = null; bM = null
        aA = aA and -65
    }

    /**
     * `g.k()` (g.java:4821, proven): mounted-rope input handler — runs
     * from the bound ax13's `aW()` tick (`bM == k.aS`), not the player
     * loop. aG==4 → climb `i(82)`/`bN--` (`bN-2<0 → j()` dismount);
     * else held-D-pad pumps the pendulum (±512 clamp ±1280, `/80` feed)
     * with anim picks {84,85}; `u(16388)` zeroes the swing → `i(82)`→
     * `r()→i(326)` hang; `u(65568) → i(86)` let-go cue; `u(2)/u(8)` →
     * `av` + `j()`.
     */
    fun ropeInput(w: LevelCellSource) {
        val r0 = bM ?: return
        if (r0.aG == 4) {
            if (r0.bN - 2 < 0) { releaseRope(w); return }
            r0.bN--; setAnim(82); return
        }
        if (w.padDown(65568)) { setAnim(86); return }                 // u(65568)
        if (w.padTap(4112)) { av = true; return }                     // x(4112)
        if (w.padTap(8256)) { av = false; return }                    // x(8256)
        if (w.padDown(2)) { av = true; releaseRope(w); return }       // u(2)
        if (w.padDown(8)) { av = false; releaseRope(w); return }      // u(8)
        if (w.padDown(8256)) {                                        // L38 pump
            if (r0.bP <= 0 && r0.bO >= 0) {
                if (r0.bP > -1280) r0.bP = -1280
                r0.bP += 512
                r0.bO += (20480 + r0.bP) / 80
            }
            setAnim(if (av) 85 else 84); return
        }
        if (w.padDown(4112)) {                                        // L57 pump
            if (r0.bP >= 0 && r0.bO <= 0) {
                if (r0.bP < 1280) r0.bP = 1280
                r0.bP -= 512
                r0.bO -= (20480 - r0.bP) / 80
            }
            setAnim(if (av) 84 else 85); return
        }
        if (w.padDown(16388)) {                                       // L76 climb
            if (r0.bO != 0 || r0.bP != 0) { r0.bP = 0; r0.bO = 0; return }
            if (S == 326 || S == 83 || (S == 82 && T == 0)) {
                if (r0.bN - 2 < 0) { setAnim(326); return }           // L95
                r0.bN--; setAnim(82); return                          // L92
            }
            if (S == 82) return                                       // mid-climb
            if (!animFinished()) return
            setAnim(326); return
        }
        if (w.padDown(33024)) {                                       // L103 descend
            if (r0.bO != 0 || r0.bP != 0) { r0.bP = 0; r0.bO = 0; return }
            if (r0.bN + 2 > r0.Z[1] - 2) {
                setAnim(43); ag = 0; ah = 2560                        // L113
                if (r0.aG == 2) consumeH()                                 // k.aS.H()
                r0.aA = 0; r0.bM = null                               // bM kept — verbatim
                aA = aA and -65
                return
            }
            r0.bN++; setAnim(83); return
        }
        if (S == 326) return                                          // L119
        if (!animFinished() && T != 0) return                         // L121
        setAnim(326)                                                  // L124
    }

    /** `i.y()` (i.java:1211, proven): wall-side check — `ag<0→bb`,
     *  `ag>0→bc`, `ag==0→av?bb:bc` (bb/bc = a(boolean) wall flags). */
    fun climbCheck(): Boolean =
        if (ag < 0) bb else if (ag > 0) bc else if (av) bb else bc

    /** `c(boolean)` (g.java:3779, proven): facing-side head cell is open —
     *  `av → (W[0]/20)-1 : (W[2]/20)+1` at `al/20`; cell `<12 → true`.
     *  Gates the S37 grapple-climb step. */
    fun facingCellOpen(w: LevelCellSource): Boolean {
        val cx = if (av) (W[0] / 20) - 1 else (W[2] / 20) + 1
        return e(w, cx, al / 20) < 12
    }

    /** `i.bf()` (i.java:14608, proven): ax58 door-open query —
     *  `S ∈ {1,3,4,6,8,10,12}`. */
    fun isBf(): Boolean =
        ax == 58 && S in intArrayOf(1, 3, 4, 6, 8, 10, 12)

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
     * `i.aa()` (i.java:19429, proven) — the claim-counter script VM.
     * `scriptStep` (`i.cK`) ticks once per call (gated by `cd[1]` fast-
     * forward or the `aH`/`aI`/`j.g` slow-mo beat) and each block's PC
     * (`scriptOps[b]` = `i.cL`, seeded by `bJ` from `k.bz` = the block's
     * first-group byte offset — bytecode `iload_8 - iload_6` at
     * k.javap:23619, both decompilers mislabel it block size) walks the
     * `[key:u16][cnt:u8][ops]` groups of `k.by[ca]`.
     *
     * Per tick, per block: the group at the PC is decoded; its ops run
     * (each op self-gates on `key` vs `step`); the PC consumes the group
     * iff `key <= step` (bytecode 2204-2238: `r06 == r02 || r06 < r02`,
     * `inferred`-verified); a PC past `len-2` leaves `cd[5]` — all blocks
     * done → `bI()` releases the claim.
     *
     * Ops (<100): 11/12/21/25/31 = 4B move-target lerp (op25 adds the
     * `cM/cN` latch when the target is `this`; op11/12 are camera-space —
     * `k.aa=false` + clamp to `k.br-400`/`k.bs-240`, op12 offsets
     * -200/-120); 13 = focus `k.ae` on the uid of the referenced type-2
     * block + `Z=false,aa=true`; 22/32 = `r14.i(anim)` (+256 sign fix;
     * ax11/anim-139 adds `k.e(0,aw)`+`k.o(3)` stat ticks — bytecode
     * i.javap:877-899, unconditional `i()`); 23/24 = `P |= m` / `P &= ~m`
     * where `m = ((u16 + u8) << (16 + u8<<24)) >> 16`, `av` from bit0;
     * 34-39 = arg-ops (`r013 = (op-34)%3+1` u16 args; 34-36 read a lead
     * uid and only run when `k.q(uid)` finds nothing — "if-absent" arms;
     * all fire only `r06 == r02`); 14-20/26-30/33/40/41-44 skip.
     * Ops ≥100 → `a(op,…)` (i.java:20043) — `runBigOp`, slice 43c.
     */
    fun runClaimScript(w: LevelCellSource) {
        if (cd[0]) return                                                  // L5
        if (cd[2] && w.padHeld(0x20000)) {                                 // L14 skip latch
            cd[1] = true
            if (Entity.MISSION_BH[w.kAj] != 3) w.kM(w.kAd)                 // k.m(k.ad)
            w.sfx(23)                                                      // k.A(23)
        }
        if (scriptStep >= 0) {                                             // L14→L22
            cd[5] = true
            val blocks = w.kBy[ca]
            val r02 = scriptStep
            // L18-L21: tick step — cd[1] fast-forward, else the aH slow-mo
            // beat (every aI-th j.g)
            if (cd[1] || !w.iAH || w.jG % w.iAI.toLong() == 0L) scriptStep++
            val ops = scriptOps
            var r13 = 0
            while (ops != null && r13 < blocks.size && r13 < ops.size) {   // L24
                var r14: Entity? = null
                val blk = blocks[r13]
                if (ops[r13] >= blk.size - 2) { r13++; continue }          // L336 done
                val r04 = blk[0].toInt() and 0xFF
                when (r04) {                                               // L26-L40
                    0 -> w.kAb = false
                    1 -> w.kZ = !w.kAa
                    2 -> r14 = if (claimPositionType() && r13 == 0) this
                               else w.findByAw(u16(blk, 2))
                }
                var pc = ops[r13]
                val r06 = u16(blk, pc)                                     // group key
                val r07 = u8(blk, pc + 2)                                  // op count
                pc += 3
                var r18 = 0; var r19 = 0; var r20 = -1
                var r23 = 0
                while (r23 < r07) {                                        // L45 op loop
                    val op = u8(blk, pc); pc++
                    if (op >= 100) {
                        val consumed = runBigOp(op, blk, pc, r02, r06, w)
                        if (consumed < 0) return                           // L50 halt
                        pc += consumed
                    } else when (op) {
                        11, 12, 21, 25, 31 -> {                            // L54 move 4B
                            val r010 = i16(blk, pc)
                            val r011 = i16(blk, pc + 2); pc += 4
                            if (r02 <= r06) {
                                r18 = r010; r19 = r011
                                if (r14 === this && op == 25) {
                                    r18 = r010 + claimLatchX
                                    r19 = r011 + claimLatchY
                                }
                                if (op == 12) { r18 -= 200; r19 -= 120 }
                                if (op == 11 || op == 12) {                // L65-80 camera
                                    w.kAa = false
                                    r18 = r18.coerceIn(0, w.kBr - 400)
                                    r19 = r19.coerceIn(0, w.kBs - 240)
                                }
                                r20 = r06
                            }
                        }
                        13 -> {                                            // L82 focus 5B
                            if (r02 == r06) {
                                val tgt = blocks.getOrNull(u8(blk, pc))
                                if (tgt != null &&
                                    (tgt[0].toInt() and 0xFF) == 2) {
                                    w.kAe = w.findByAw(u16(tgt, 2))
                                    w.kZ = false; w.kAa = true
                                }
                            }
                            pc += 5                                        // idx + 4 dead
                        }
                        22, 32 -> {                                        // L88 anim 2B
                            var r024 = u16(blk, pc); pc += 2
                            if (r06 <= r02) {
                                if (r024 < 0) r024 += 256   // i16→u8 fixup
                                if (r14 != null) {
                                    if (r14.ax == 11 && r024 == 139) {     // kill count
                                        w.kStatE(r14.aw); w.kStat(3)
                                    }
                                    r14.setAnim(r024)
                                }
                            }
                        }
                        23, 24 -> {                                        // L102/L112 mask 4B
                            if (r06 <= r02) {
                                val mask = ((u16(blk, pc) +
                                             u8(blk, pc + 2)) shl
                                            (16 + (u8(blk, pc + 3) shl 24))) shr 16
                                if (r14 != null) {
                                    if (op == 23) {
                                        r14.P = r14.P or mask
                                        r14.av = (r14.P and 1) != 0
                                    } else {
                                        r14.P = r14.P and mask.inv()
                                        r14.av = (mask and 1) != 1
                                    }
                                }
                            }
                            pc += 4
                        }
                        34, 35, 36 -> {                                    // L121 uid-gate
                            val uid = u16(blk, pc); pc += 2
                            val found = w.findByAw(uid) != null
                            pc = readArgOps(blk, pc, op, r02, r06,
                                            execute = !found, w = w)
                        }
                        37, 38, 39 -> {                                    // L122 arg-ops
                            pc = readArgOps(blk, pc, op, r02, r06,
                                            execute = true, w = w)
                        }
                        else -> {}                                         // 14-20/26-30/33/40-44 skip
                    }
                    r23++
                }
                // L249-L255: group consumed when key <= step
                if (r06 <= r02) ops[r13] = pc
                if (ops[r13] < blk.size - 2) cd[5] = false
                // L258-L332: move lerp while r20 >= 0
                if (r20 >= 0) {
                    if (r04 == 1) {                                        // camera
                        w.kO += (r18 - w.kO) / (r20 - r02 + 1)
                        w.kP += (r19 - w.kP) / (r20 - r02 + 1)
                    } else if (r14 != null) {
                        var r232 = (r18 - r14.ak) / (r20 - r02 + 1)
                        var r102 = (r19 - r14.al) / (r20 - r02 + 1)
                        if (w.iAH) {                                       // slow-mo halves
                            r232 = (r232 shl 8) / w.iAI shr 8
                            r102 = (r102 shl 8) / w.iAI shr 8
                        }
                        if ((r14.P and 512) != 0 || r14.claimActive()) {   // carry check
                            if (r14.ax == 51 || r14.ax == 43) {
                                val p = w.player
                                if (p.ga === r14 || w.gc === r14) {        // g.a/g.c
                                    p.ak += r232
                                    if (r14.ax != 43) p.al += r102
                                }
                                for (f in w.kBb) {                         // follower scan
                                    if (f == null) continue
                                    f.offscreenScore(w)                    // u()
                                    if ((f.P and 256) == 0 &&
                                        ((f.au < 2 && (f.P and 32) == 0) ||
                                         (f.P and 16) != 0) &&
                                        (f.ax == 11 || f.ax == 23 ||
                                         f.ax == 17 || f.ax == 9) &&
                                        f.s === r14) {
                                        f.ak += r232; f.al += r102
                                    }
                                }
                            }
                        }
                        if (r14.aw != 205 ||
                            (r14.S != 34 && r14.S != 35)) {                // L321 move
                            r14.ak += r232; r14.al += r102
                        }
                        if (r14.ax == 11) {                                // L330 Z-resync
                            r14.Z[3] = r14.ak; r14.Z[4] = r14.al
                            r14.Z[9] = r14.ak + r14.Z[15]
                            r14.Z[11] = r14.al + r14.Z[16]
                            r14.Z[10] = r14.ak + r14.Z[15] + r14.Z[17]
                            r14.Z[12] = r14.al + r14.Z[16] + r14.Z[18]
                            r14.aA = 0
                        }
                        r14.refreshBoxes(); r14.inPlayV(w)                 // t();v()
                    }
                }
                if (w.iCO != null) w.player.gMountAlign(w.iCO)             // L334 aS.b(cO)
                r13++
            }
            if (cd[5]) releaseClaim(w)                                     // L338
        }
        velClampTail(w)                                                    // L341
    }

    /** `u16`/`i16`/`u8` little-endian readers on a block buffer. */
    private fun u16(b: ByteArray, p: Int): Int =
        (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8)
    private fun i16(b: ByteArray, p: Int): Int =
        ((b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8)).toShort().toInt()
    private fun u8(b: ByteArray, p: Int): Int = b[p].toInt() and 0xFF

    /**
     * `aa()` ops 34-39 — the arg-ops (i.java:19560-19819, proven):
     * `r013 = ((op-34)%3)+1` u16 args; arg1 `r9` = sub-op index, arg2
     * `r10` = `r2` operand (r013<2 leaves it -1); a 3rd arg is read but
     * unused. 34-36 consume a lead uid first (caller) and run only when
     * `k.q(uid)` misses; all run only at `r06 == r02`. Returns the new PC.
     */
    private fun readArgOps(blk: ByteArray, pc0: Int, op: Int, step: Int,
                           key: Int, execute: Boolean,
                           w: LevelCellSource): Int {
        var pc = pc0
        val r013 = (op - 34) % 3 + 1
        var r9 = -1; var r10 = -1
        if (r013 >= 1) { r9 = u16(blk, pc); pc += 2 }
        if (r013 >= 2) { r10 = u16(blk, pc); pc += 2 }
        if (r013 >= 3) pc += 2                                             // third arg unread
        if (execute && key == step) runArgSub(r013, r9, r10, w)            // L135
        return pc
    }

    /**
     * The two arg-op sub-switches (i.java:19587-19819, proven). r013==1 →
     * 26-case switch on `r12` (op37, 15× in data); r013==2 → 12-case on
     * `r12` with `r2` operand (op38, 4× in data); r013==3 unreachable in
     * data (op39 absent — the switch falls through in the original too).
     */
    private fun runArgSub(r013: Int, r9: Int, r10: Int, w: LevelCellSource) {
        val r12 = r9; val r2 = r10
        if (r013 == 1) when (r12) {
            0 -> cd[2] = true                                              // L140
            1 -> { w.screenL(15); if (w.kAj != 7) w.kStat(0) }             // L141
            2 -> { w.kBx = -1; w.screenL(12) }                             // L144
            3 -> cd[3] = true                                              // L145
            4 -> { val v = w.kAV; if (v != null) v.Z[0] = 1 }              // L146
            5 -> { val v = w.kAV; if (v != null) v.Z[0] = 0 }              // L147
            6 -> eventDisarm(w)                                            // L148 i.O()
            7 -> unlockInput(w)                                            // L149 k.p()
            8 -> {                                                         // L152 k.C release
                val c = w.kC
                if (c != null && c.claimActive()) {                        // ab() gate
                    w.kC = null
                    w.removeEntity(c)                                      // k.c — no bI
                }
            }
            9 -> cd[4] = true                                              // L156
            10 -> cd[6] = cd[6] xor true                                   // L157
            13 -> w.iCe = true                                             // L164
            14 -> w.iCe = false                                            // L165
            15 -> if (w.kBK) {                                             // L167 marker spawn
                val m = w.spawnStatic(9, 47, 5, 400)
                if (m != null) {
                    m.P = 16; m.aC = 10
                    m.ak = w.kO; m.al = w.kP
                    m.Z[0] = 0; m.Z[1] = -1; m.Z[2] = 47
                    w.queueInsert(m)
                }
            }
            16 -> { cd[8] = cd[8] xor true; cb?.let { it[3] = 20 } }       // L169
            17 -> w.kAQ = null                                             // L174
            18 -> w.kAv = true                                             // L175
            19 -> { cd[9] = false; cg = null; ch = null }                  // L176 i.cg/ch
            20 -> lockInput(w)                                             // L150 k.o()
            23 -> {                                                        // L180-L185 aV.ad ax43
                val link = w.kAV?.ad
                if (link != null && link.ax == 43) {
                    w.removeEntity(link)
                    w.kAV?.ad = null
                }
            }
            24 -> w.iZ = false                                             // L186 i.z static
            25 -> w.iZ = true                                              // L187
            else -> {}
        } else if (r013 == 2) when (r12) {
            0 -> w.player.P = w.player.P or r2                             // aS.P |= r2
            1 -> w.player.P = w.player.P and r2.inv()                      // aS.P &= ~r2
            2 -> {                                                         // a(101)+ad.a(101)
                w.findByAw(r2)?.let { t -> t.aOp(101); t.ad?.aOp(101) }
            }
            3 -> {                                                         // y=0 + ad.y=0
                w.findByAw(r2)?.let { t -> t.y = 0; t.ad?.y = 0 }
            }
            4 -> w.iBD = r2 != 0                                           // bD = r2 != 0
            5 -> { w.kBw = -1; w.screenL(13) }                             // k.bw=-1; l(13)
            6 -> w.iBQ = r2                                                // bQ = r2
            7 -> { eventArm(r2, w); if (w.iAI <= 0) w.iAI = 1 }            // i.b(r2)+aI floor
            8 -> {                                                         // L215: ae=q + aT=(ax!=0)
                val t = w.findByAw(r2)
                if (t != null) { w.kAe = t; w.kAT = t.ax != 0 }
            }
            9 -> {                                                         // L222 cO arm/clear
                if (r2 <= 0) w.iCO = null
                else {
                    val t = w.findByAw(r2)
                    if (t == null) w.iCO = null
                    else if (t.ax == 43 && (t.S == 1 || t.S == 4)) w.iCO = t
                    // wrong ax/S leaves cO untouched (L247)
                }
            }
            10 -> if (w.kAU != null) {                                     // L235: by=r2 (i static)
                w.iBy = r2
                if (r2 >= 2) w.kAU!!.aB = 300
                if (w.iBy == 3) w.kF?.let { w.removeEntity(it) }           // k.c(k.F)
            }
            11 -> if (w.kF != null) w.kAL = r2                             // k.aL = r2
            else -> {}
        }
    }

    /** `i.a(int)` (i.java:3777, proven): literally `if (y>=0) y=101` —
     *  the r4 argument is ignored (structured i.java confirms). */
    fun aOp(r4: Int) { if (y >= 0) y = 101 }

    /** `g.b(i)` (g.java:480, proven): ax43 mount-align — `r6.t()` then
     *  center `ak/al` on the mount's X-box when its `S ∈ {1,4}`. */
    fun gMountAlign(target: Entity?) {
        if (target == null || target.ax != 43) return
        if (target.S != 1 && target.S != 4) return
        target.refreshBoxes()
        ak = (target.X[0] + target.X[2]) shr 1
        al = (target.X[1] + target.X[3]) shr 1
    }

    /** `aa()`'s L341 tail (i.java:20011, proven): `k.C==this && cK>0 &&
     *  !(aS.P&512)` → zero the player's `ag/ai/ah/aj` velocities. */
    private fun velClampTail(w: LevelCellSource) {
        if (w.kC !== this || scriptStep <= 0) return
        val p = w.player
        if ((p.P and 512) != 0) return
        p.ag = 0; p.ai = 0; p.ah = 0; p.aj = 0
    }

    /**
     * `i.a(int,byte[],int,int,int)` (i.java:20043, proven): the ≥100-op
     * decoder. `k.t(op)` = consumed length (`eI[op-100]`); ops fire only
     * at `step == key` EXCEPT 108/113 which poll every step and return -1
     * (= halt aa()) when they branch via `h(k.s(uid)); k(k.s(uid))`.
     */
    private fun runBigOp(op: Int, blk: ByteArray, pc0: Int, step: Int,
                         key: Int, w: LevelCellSource): Int {
        var pc = pc0
        val consumed = w.kT(op)
        // L11: only 108/113 run their body while step != key
        if (step != key && op != 108 && op != 113) return consumed
        when (op) {
            // L12: [uid i16][sub i16][arg i16] — uid 0 → this
            100 -> {
                val r02 = i16(blk, pc); pc += 2
                val r03 = i16(blk, pc); pc += 2
                val r04 = i16(blk, pc); pc += 2
                val r05 = if (r02 != 0) w.findByAw(r02) else this
                if (r05 != null) when (r03) {
                    0, 1 -> if (r03 == 1 || r04 != 0)
                        bigOp100Arg(r05, r04, w)
                    2 -> w.removeEntity(r05)                             // L62 k.c
                    3 -> { r05.cz = r04; r05.cA = 0 }                    // L63
                    4 -> { w.kAb = true; r05.az = r04 }                  // L64-65
                    5 -> r05.az = r04                                    // L65
                    else -> {}
                }
            }
            101 -> { cP = i16(blk, pc); pc += 2 }                        // L67 i.cP
            // L68: [id u16][flag u16] — k.z/k.A are the same fn (A → z
            // alias, k.java:7370) → sfx either way; flag is dead.
            102 -> { w.sfx(u16(blk, pc)); pc += 4 }
            103 -> { /* L301 — consume only */ }
            104 -> {                                                     // L75 k.n(int)
                w.kNSet(i16(blk, pc)); pc += 2
            }
            // L76: [r9 u8][str u16][r11 u8] dialog — cd[0]=true halt +
            // k.b() → k.l(21) on accept; skip-latch path re-arms edges
            // (k.v = k.w).
            105 -> {
                val r09 = u8(blk, pc); pc++
                val r010 = u16(blk, pc); pc += 2
                val r011 = u8(blk, pc); pc++
                if (cd[1]) w.padRearm()
                else {
                    cd[0] = true
                    if (w.kDialog(r09, r010, r011)) w.screenL(21)
                }
            }
            // L84: [target-uid u16][strA u16][strB u16][r15 u16][r16 u8]
            // dialog-box state on cT.cQ (ad()/bK() consume it later).
            106 -> {
                val r012 = u16(blk, pc); pc += 2
                val r013 = u16(blk, pc); pc += 2
                val r014 = u16(blk, pc); pc += 2
                val r015 = u16(blk, pc); pc += 2
                val r016 = u8(blk, pc); pc++
                cT = if (r012 > 0) w.findByAw(r012) else this
                val t = cT
                if (t != null) {
                    if (t.cQ == null) t.cQ = IntArray(10) { -1 }
                    val q = t.cQ!!
                    q[5] = r013; q[6] = r014; q[2] = -1; q[3] = r015
                    q[8] = if (r016 <= 1) r016 else r016 - 2
                    if (r012 > 0) q[7] = 1
                    if (r016 > 1) q[9] = 1
                }
            }
            // L106: [raw-mask u16] — arm the one-button prompt bA[0];
            // mask normalized 32→65568, 4→16388, 16→4112, 64→8256,
            // 256→33024 (key codes → pad masks); cb[2] = bit position
            // (ct[] frame index).
            107 -> {
                val r019 = u16(blk, pc); pc += 2
                if (cb == null) { cb = IntArray(4); cb!![1] = -1 }
                val c = cb!!
                var r16 = 0
                while ((r019 shr r16) > 1) r16++
                c[0] = when (r019) {
                    32 -> 65568; 4 -> 16388; 16 -> 4112
                    64 -> 8256; 256 -> 33024
                    else -> r019
                }
                c[2] = r16
                val pr = ScriptPrompt()
                if (w.mounted) { pr.attach(9, w.clipFor(9)); pr.setState(CT[r16], -1) }
                else { pr.attach(74, w.clipFor(74)); pr.setState(0, -1) }
                scriptPrompts[0] = pr
                c[1] = 0
            }
            // L133: [pass-uid u16][fail-uid u16] — poll every step while
            // step < key: cb[0]-mask / touch-rect / strip press → cb[1]=1;
            // mounted k.v(1020) → cb[1]=2. At step == key: success →
            // h/k(k.s(r020)); anything else → h/k(k.s(r021)); -1 halt.
            108 -> {
                val r020 = u16(blk, pc); pc += 2
                val r021 = u16(blk, pc); pc += 2
                if (step < key) {
                    val pr = scriptPrompts[0]
                    if (pr != null && !w.mounted && pr.e != -1 &&
                        w.pointerMoveIn(pr.a - 35, pr.b - 35, 70, 70))
                        pr.setState(1, 1)                                // hover
                    val c = cb ?: IntArray(4).also { cb = it }
                    if (w.padHeld(c[0]) ||
                        (!w.mounted && pr != null &&
                         w.pointerDownIn(pr.a - 35, pr.b - 35, 70, 70)) ||
                        (!w.mounted && w.pointerStrip())) {
                        if (c[1] == 0) {
                            c[1] = 1
                            if (pr != null) {
                                if (w.mounted)
                                    pr.setState(CT[c[2]] + 1, 1)
                                else pr.setState(-1, 1)
                            }
                            eventDisarm(w); unlockInput(w)               // O();k.p()
                        }
                    } else if (w.mounted && w.padHeld(1020) && c[1] == 0) {
                        c[1] = 2
                        pr?.setState(CT[c[2]] + 2, 1)
                        eventDisarm(w); unlockInput(w)
                    }
                }
                if (step == key) {
                    eventDisarm(w); unlockInput(w)
                    val decided = cb?.get(1) ?: 0
                    if (decided == 1 && r020 > 0) {
                        val idx = w.kSIndex(r020)
                        bindScript(idx, w); scriptKeyStep(idx, w)
                        return -1
                    }
                    if (decided != 1 && r021 > 0) {
                        val idx = w.kSIndex(r021)
                        bindScript(idx, w); scriptKeyStep(idx, w)
                        return -1
                    }
                }
            }
            // L184: [x0][y0][x1][y1] carrier-track rect — cf[4] = the
            // param-curve angle j.b(-dx, dy); armed on the last step only.
            109 -> {
                if (cf == null) cf = IntArray(5)
                val f = cf!!
                f[0] = i16(blk, pc); f[1] = i16(blk, pc + 2)
                f[2] = i16(blk, pc + 4); f[3] = i16(blk, pc + 6); pc += 8
                var dx = f[2] - f[0]; val dy = f[3] - f[1]
                if (dx == 0) dx = 1
                f[4] = Trig.atan2(dy, -dx)
                if (step == key) cd[9] = true
                else if (step > key) { cd[9] = false; cg = null; ch = null }
            }
            // L196: [uidA u16][uidB u16] — cg/ch entity pair; cd[9] arms
            // at key, clears (with the links) once past it.
            110 -> {
                cg = w.findByAw(u16(blk, pc)); pc += 2
                ch = w.findByAw(u16(blk, pc)); pc += 2
                if (step == key) cd[9] = true
                else if (step > key) { cd[9] = false; cg = null; ch = null }
            }
            // L203: [S u16][x u16][y u16][facing u8][az u16] — spawn the
            // ax8 param projectile (k.bK-gated; the boss knife throw).
            111 -> {
                if (w.kBK) {
                    val r032 = u16(blk, pc); pc += 2
                    val r033 = u16(blk, pc); pc += 2
                    val r034 = u16(blk, pc); pc += 2
                    val r035 = u8(blk, pc); pc++
                    val r036 = u16(blk, pc); pc += 2
                    w.spawnParam(r032, r035 > 0, r033, r034, r036)
                        ?.let { it.P = it.P or 512 }
                }
            }
            // L209: [i0][i1][i2] u16 — choice list (indexes 0-9 kept);
            // spawn one bA[i] prompt per choice; cc[4]=0, cb[1]=0.
            112 -> {
                val vals = intArrayOf(u16(blk, pc), u16(blk, pc + 2),
                                      u16(blk, pc + 4)); pc += 6
                if (cc == null) cc = IntArray(5)
                val cArr = cc!!
                var r162 = 0
                for (i in 0 until 3)
                    if (vals[i] in 0..9) { cArr[r162 + 1] = vals[i]; r162++ }
                cArr[0] = r162
                for (r173 in 0 until r162) {
                    var pr = scriptPrompts[r173]
                    if (pr == null) { pr = ScriptPrompt(); scriptPrompts[r173] = pr }
                    if (w.mounted) {
                        pr.attach(9, w.clipFor(9))
                        pr.setState(CT[cArr[r173 + 1]], -1)
                    } else { pr.attach(74, w.clipFor(74)); pr.setState(0, -1) }
                }
                cArr[4] = 0
                cb?.let { it[1] = 0 }
            }
            // L238: [pass-uid u16][fail-uid u16] — sequential multi-choice
            // poll: press 1<<cc[1+cc[4]] (or touch/strip) → cc[4]++;
            // mounted 1020 → deselect all + cc[4]=-1. At step == key:
            // cc[4]==cc[0] → pass branch r040; cc[4]<cc[0] → fail r041.
            113 -> {
                val cArr = cc
                if (step < key && cArr != null &&
                    cArr[4] > -1 && cArr[4] < cArr[0]) {
                    val r038 = cArr[4]
                    val r037 = 1 shl cArr[1 + cArr[4]]
                    val pr = scriptPrompts[r038]
                    if (pr != null && !w.mounted && pr.e != -1 &&
                        w.pointerMoveIn(pr.a - 35, pr.b - 35, 70, 70))
                        pr.setState(1, 1)
                    if (w.padHeld(r037) ||
                        (!w.mounted && pr != null &&
                         w.pointerDownIn(pr.a - 35, pr.b - 35, 70, 70)) ||
                        (!w.mounted && w.pointerStrip())) {
                        if (pr != null) {
                            if (w.mounted)
                                pr.setState(CT[cArr[1 + r038]] + 1, 1)
                            else pr.setState(-1, 1)
                        }
                        cArr[4]++
                        if (cArr[4] == cArr[0]) {
                            eventDisarm(w); unlockInput(w)
                        }
                    } else if (w.mounted && w.padHeld(1020)) {
                        for (r174 in 0 until cArr[0])
                            scriptPrompts[r174]?.let {
                                if (w.mounted)
                                    it.setState(CT[cArr[1 + r174]] + 2, -1)
                                else it.setState(-1, 1)
                            }
                        cArr[4] = -1
                        eventDisarm(w); unlockInput(w)
                    }
                }
                if (step == key) {
                    eventDisarm(w); unlockInput(w)
                    val r040 = u16(blk, pc); pc += 2
                    val r041 = u16(blk, pc); pc += 2
                    if (cArr != null) {
                        if (cArr[4] == cArr[0] && r040 > 0) {
                            val idx = w.kSIndex(r040)
                            bindScript(idx, w); scriptKeyStep(idx, w)
                            cc = null
                            return -1
                        }
                        if (cArr[4] < cArr[0] && r041 > 0) {
                            val idx = w.kSIndex(r041)
                            bindScript(idx, w); scriptKeyStep(idx, w)
                            cc = null
                            return -1
                        }
                        cc = null
                    }
                }
            }
            // L299: [str-idx u16][countdown u16] — HUD objective line.
            114 -> {
                w.kAP = w.levelString(1 + w.kAj, u16(blk, pc)); pc += 2
                w.kAO = u16(blk, pc); pc += 2
            }
            else -> {}
        }
        return consumed
    }

    /** op100's `r04` sub-switch (i.java L24 dispatch, proven). */
    private fun bigOp100Arg(r05: Entity, r04: Int, w: LevelCellSource) {
        when (r04) {
            0 -> {                                                         // L25
                r05.P = r05.P or 32 or 128
                r05.P = r05.P and 16.inv()
                r05.dropAeLink()                                           // G()
            }
            1 -> { r05.P = r05.P and 32.inv(); r05.P = r05.P and 128.inv() }
            2 -> r05.P = r05.P or 16                                       // L27
            3 -> { r05.P = r05.P and 32.inv(); r05.P = r05.P or 512 }      // L28
            4 -> {                                                         // L29
                r05.P = r05.P and 512.inv()
                r05.ai = 0; r05.ag = 0; r05.aj = 0; r05.ah = 0
            }
            5 -> r05.P = r05.P xor 1024                                    // L31
            6 -> r05.cd[7] = !r05.cd[7]                                    // L34
            7 -> if (r05.ax == 0)                                          // L40 aS.aA^256
                w.player.aA = w.player.aA xor 256
            8 -> if (r05.ax == 11 && r05.Z[19] == 0) r05.Z[20] = 40        // L46
            10 -> {                                                        // L51
                r05.P = 32
                if (r05.ax == 35) {
                    r05.setAnim(0)
                    val af = r05.af
                    if (af != null && af.ax == 73) af.setAnim(152)
                }
            }
            11 -> r05.P = r05.P or 32                                      // L59
            12 -> r05.P = r05.P or 64                                      // L60
            else -> {}
        }
    }

    /** `i.G()` (i.java:4792, proven): deactivate the `ae` linked entity
     *  (i.p() — box clear + child cascade) and drop the link. */
    fun dropAeLink() {
        ae?.deactivate()
        ae = null
    }

    /** `i.a(iVar)` (i.java:231, proven): the `ac` bind/unbind — release
     *  the current `ac` (`P &= ~256`) then bind `other` (`P |= 256`).
     *  `bh()` binds the player to the destination door through this. */
    fun bindAc(other: Entity?) {
        ac?.let { it.P = it.P and -257 }
        ac = other
        other?.let { it.P = it.P or 256 }
    }

    /** `i.ac()` (i.java:20577, proven): ax ∈ {11,17,23,43,40,45,51} gets
     *  the claim-position latch (i.cM/i.cN) written by `h()`. */
    fun claimPositionType(): Boolean =
        ax == 11 || ax == 17 || ax == 23 || ax == 43 || ax == 40 ||
        ax == 45 || ax == 51

    /**
     * `i.bJ()` (i.java:20024, proven): reload the script-op buffer —
     * `cd[0]=cd[2]=false`, `cK=-1`, `cb=null`, `cL` = copy of
     * `k.bz[ca]`. `claimOps` is the k.bz accessor.
     */
    fun reloadScriptOps(w: LevelCellSource) {
        cd[0] = false; cd[2] = false
        scriptStep = -1
        cb = null
        scriptOps = w.claimOps(ca)?.copyOf()
    }

    /** `i.Y()` (i.java:19421, proven): script pause — `cd[0]=true`. */
    fun pauseScript() { cd[0] = true }
    /** `i.Z()` (i.java:19425, proven): script resume — `cd[0]=false`.
     *  The dialog screen's dismiss path calls `k.C.Z()`. */
    fun resumeScript() { cd[0] = false }

    /**
     * `i.h(int)` (i.java:19300, proven): bind claim-script `r5` — no-op
     * unless `cM == -1` (the latch is unbound); allocates `cd` with
     * `cd[7]=true`, stores `ca=r5`, runs `bJ()` when `ca>=0`, then latches
     * `cM/cN` to the current position for `claimPositionType`.
     */
    fun bindScript(r5: Int, w: LevelCellSource) {
        if (claimLatchX != -1) return
        if (!cdAllocated) { cdAllocated = true; cd[7] = true }   // h() alloc
        ca = r5
        if (ca >= 0) reloadScriptOps(w)
        if (claimPositionType()) { claimLatchX = ak; claimLatchY = al }
    }

    /**
     * `i.k(int)` (i.java:19325, proven): script key-step — `r5==-1` no-op;
     * `cK=0`, `k.aw=0`, `cb` alloc (`cb[1]=-1` on first alloc only); the
     * `k.C==this && cK>0` tail is dead code after the `cK=0` write
     * (decompiled form kept verbatim) — `!(aS.P&512)` would → `k.v()`.
     */
    fun scriptKeyStep(r5: Int, w: LevelCellSource) {
        if (r5 == -1) return
        scriptStep = 0; w.kAw = 0
        if (cb == null) { cb = IntArray(4); cb!![1] = -1 }
        if (w.kC !== this) return
        if (scriptStep <= 0) return
        if (w.player.P and 512 == 0) w.clearLatches()
    }

    /**
     * `i.N()` (i.java:7284, proven): claim the `k.C` context slot — a
     * previous holder that still `ab()`-claims is released (`bI()`) and
     * removed (`k.c`); then binds `h/k(k.s(aG))` and arms `P|16`.
     */
    fun bindContext(w: LevelCellSource) {
        val cur = w.kC
        if (cur != null && cur.claimActive()) {
            cur.releaseClaim(w)
            w.removeEntity(cur)
            w.kC = null
        }
        w.kC = this
        bindScript(w.kSIndex(aG), w)
        scriptKeyStep(w.kSIndex(aG), w)
        P = P or 16
    }

    /**
     * `i.bI()` (i.java:19346, proven): full claim release — when `k.C==this`:
     * clears `k.Z/aa/ab`, `g.a`, and the `aS.bM` link (ax13 unlink arm);
     * `k.n()` + `z=true` (non-ax13 path). Then the L13 tail: `cd[2]=false`,
     * `cd[3]` → `cL=null`+`bJ`+`cK=0` fast-path; else `cK=-2`, `k.aw=0`,
     * the ax5 `k.ae` rebind (`aS`, or `g.a` when its ax==43) + bh==3
     * `aS.al-=k.X` when `!k.Z`; the `cP` pending-script handoff;
     * `k.C=null`, `cd[4]` → `bJ`; `ax==58` → return; `ax==5` +
     * `Z[1]∈{20,21}` → `P&=-17`, otherwise `k.c(this)` removal.
     */
    fun releaseClaim(w: LevelCellSource) {
        val p = w.player
        if (w.kC === this) {                          // pre-L13 release arm
            w.kZ = false; w.kAa = false; w.kAb = false
            if (p.ga != null) p.ga = null
            val held = p.bM
            if (held == null || held.ax != 13) {      // L12
                w.kN()
                w.iZ = true                           // i.z static (i.java:110)
            } else {                                  // ax13 unlink arm
                held.aA = 0
                held.bM = null
                p.bM = null
                p.aA = p.aA and -65
            }
        }
        // L13
        cd[2] = false
        if (cd[3]) {                                  // L17 fast-path
            scriptOps = null
            reloadScriptOps(w)
            scriptStep = 0
            return
        }
        scriptStep = -2
        w.kAw = 0
        if (ax == 5) {                                // L17 ax5 ae-rebind
            val ae = w.kAe
            val aeActive = ae != null && ae.ax == 10 && ae.S == 52   // i.ai()
            if (!(ae === p || aeActive))
                w.kAe = if (p.ga != null && p.ga!!.ax == 43) p.ga else p
            if (Entity.MISSION_BH[w.kAj] == 3 && !w.kZ) p.al -= w.kX
        }
        if (w.kC !== this) return                     // L35
        if (cP != -1) {                               // L37 pending handoff
            bindScript(w.kSIndex(cP), w)
            scriptKeyStep(w.kSIndex(cP), w)
            cP = -1
            return
        }
        // L40
        w.kC = null
        if (cd[4]) { reloadScriptOps(w); return }     // L45
        if (ax == 58) return                          // L47
        if (ax == 5) {                                // L49
            if (Z[1] == 20 || Z[1] == 21) { P = P and -17; return }   // L52
            w.removeEntity(this); return              // L54
        }
        w.removeEntity(this)                          // L54
    }

    /**
     * `i.b(int)` (i.java:7604, proven): arm the ax5-S3 countdown event —
     * `aH=true`, `aI=r3`, `k.aw=0`; on bh[k.aj]==3 (missions 1/4) saves
     * `k.X→aJ` (or `k.W` when set) and scales `k.X /= r3`.
     */
    fun eventArm(r3: Int, w: LevelCellSource) {
        w.iAH = true; w.iAI = r3; w.kAw = 0
        if (Entity.MISSION_BH[w.kAj] != 3) return
        w.iAJ = w.kX
        if (w.kW != 0) { w.iAJ = w.kW; w.kX = w.kW; w.kW = 0 }
        w.kX /= r3
    }

    /**
     * `i.O()` (i.java:7623, proven): disarm — `aH=false`, `k.aw=0`; on
     * bh==3 restores `k.X` from `aJ` (or `k.W`) and clears `aJ`.
     */
    fun eventDisarm(w: LevelCellSource) {
        w.iAH = false; w.kAw = 0
        if (Entity.MISSION_BH[w.kAj] != 3) return
        if (w.kW != 0) w.iAJ = w.kW
        if (w.iAJ != 0) w.kX = w.iAJ
        w.iAJ = 0
    }

    /**
     * `i.P()` (i.java:7699, proven): dead check — `aB<=0 → G()` releases
     * the `ae`/`ab` slots and returns true. Alive (`aB>0`) → false.
     */
    fun deadRelease(): Boolean = if (aB > 0) false else { releaseAe(); true }

    /** `i.aS()` (i.java:8963, proven): drop the whole `cr` pool
     *  (original nulls every cell then the matrix — same observable). */
    fun poolDrain() { cr = null }

    /** `i.aT()` (i.java:8975, proven): every `cr` member `P()` — i.e.
     *  all dead (`P()` itself `G()`-deactivates dead members); a null
     *  pool reports dead too. */
    fun poolAllDead(): Boolean {
        val pool = cr ?: return true
        for (row in pool) for (m in row) if (!m.deadRelease()) return false
        return true
    }

    /** `i.at()` (i.java:6174, proven): "settled" — false only for a
     *  live ax11 still at S 0/85/20/176; otherwise `P()` (dead check,
     *  deactivating). Consumed by the ax10 S29 release-zone gate. */
    fun atDone(): Boolean {
        if (ax == 11 && (S == 0 || S == 85 || S == 20 || S == 176))
            return false
        return deadRelease()
    }

    /**
     * `i.S()` (i.java:7276, proven): the victim-payoff tick inside the
     *  S183/184 assassination arm — three bursts of `m(-1)` wisp spawn,
     *  `k.o(5)` stat and `k.s()` streak/shake per iteration.
     */
    fun victimPayoff(w: LevelCellSource) {
        repeat(3) {
            w.spawnWisp(this)               // m(-1) → a(74,54,1,…)
            w.kStat(5)                      // k.o(5)
            w.kCollectStreak()              // k.s()
        }
    }

    /**
     * `i.d(iVar)` (i.java:1221, proven): per-ax anim reset applied to the
     *  released `i.aN` victim when the finisher ends — ax11 → `i(0)`,
     *  ax23 → `i(79)`, anything else untouched.
     */
    fun releaseAnimReset() {
        when (ax) {
            11 -> setAnim(0)
            23 -> setAnim(79)
        }
    }

    /** `i.ab()` (i.java:18914, proven): claim-script parked at an active
     *  marker — bound (`ca>=0`), not flag-0 suspended, the `i.cK` key
     *  counter armed (>=0). Same check as `claimActive` — kept for the
     *  draw-path call sites that name it `ab()`. */
    fun claimAb(): Boolean = claimActive()

    /** `g.g(int)` (g.java:5266, proven): `J |= mask` — ORs an action-request
     *  bit, then `k.q()` rebuilds the `ar[]` equip list. */
    fun requestAction(mask: Int, w: LevelCellSource) {
        gJ = gJ or mask
        w.rebuildEquip()
    }

    /** `g.h(int)` (g.java:5270, proven): request/consume — when `r4!=0`
     *  requires `J&r4` pending; sets `I=r4`, forces `k.at=1`, and when the
     *  mount link's `ab` is an ax16 request entity runs its `H()` consume
     *  (`ab.p()` + `ab = null` — i.java:15150, proven, ported as
     *  `consumeH`). `r4==1` → true;
     *  `S!=38` → true; `S==38` repeats the consume and returns false. */
    fun requestH(r4: Int, w: LevelCellSource): Boolean {
        if (r4 != 0 && (gJ and r4) == 0) return false
        gI = r4
        w.actionLock = 1                       // k.at = 1
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

    /** `g.ao()` (g.java:3792, proven): weapon-cycle — `v(131072)` edge or
     *  the 355,197,30x26 view button → `k.at==0 && k.as>1 &&
     *  (k.C==null || P&512)` → `k.at=1`, `h(ar[(p(I)+1)%as])`, sfx 23.
     *  `k.p(I)` = lowest-set-bit index ≡ position in the sorted-dense
     *  `ar[]`, so `indexOf` is equivalent. */
    fun cycleEquip(w: LevelCellSource, pad: Pad): Boolean {
        if (!pad.v(Pad.M_CYCLE) && !w.touchRect(355, 197, 30, 26)) return false
        if (w.actionLock != 0) return false
        if (w.equipCount <= 1) return false
        if (w.cEntity != null && (P and 512) == 0) return false
        w.actionLock = 1
        val idx = w.equipList.indexOf(gI).let { if (it < 0) 0 else it }
        requestH(w.equipList[(idx + 1) % w.equipCount], w)
        w.sfx(23)
        return true
    }

    /** `i.y()` (i.java:1211, proven): directional edge flag — moving picks
     *  `bb`/`bc` by `sign(ag)`, stationary picks by facing `av`. */
    fun edgeFlag(): Boolean =
        if (ag < 0) bb else if (ag > 0) bc else if (av) bb else bc

    /** `g.o()` (g.java:6090, proven): grounded-or-mounted gate for the
     *  weapon cycle — `aZ` true; `a == null || a.ax == 43` → false;
     *  else `a.ax ∈ {51,15,43}` (the trailing `ax == 43` is unreachable
     *  dead code — kept verbatim). `g.a` = `standingOn`. */
    fun groundOrVehicle(): Boolean {
        if (aZ) return true
        val s = standingOn ?: return false
        if (s.ax == 43) return false
        return s.ax == 51 || s.ax == 15 || s.ax == 43
    }

    /**
     * `g.ap()` (g.java:3817, proven): the 65568 context dispatcher —
     *  `I==4 && aA<2 → h(1)` head; on `v(65568)` (blocked while riding an
     *  ax10 zipline `ac` — inferred: the decompile's r0 flag only arms on
     *  `ac.ax!=10`), dispatch by equip `I`:
     *   1 → zero h-vel; unless crouch-rope (`S==79 && g.a.ax==51 &&
     *       g.a.aD!=0`, g.java:8488-8497) → `i(S==79?81:67)` sword swing;
     *       `k.E.K()` = `heldRelease` (ported slice 125);
     *   8 → `S!=79` → `ai=ag=0; K=0; cN=0; i(303)` standing gauge;
     *   2 → `i(286)` + sfx 29 knife anim.
     *  `g.a` = the grapple/ride link field `ga` (g.java:249+, proven). */
    fun contextDispatch(w: LevelCellSource, pad: Pad) {
        if (gI == 4 && aA < 2) requestH(1, w)
        if (!pad.v(Pad.M_CONTEXT)) return
        // L28-L33 (proven): blocked while riding a zipline OR while an
        // attack anim plays — `b()` is the busy set {67-69,81,112-115,
        // 183-184,216-217,286-287}; combo chaining runs inside the combo
        // arms (`aj()`/`ay()`) instead of re-entering here.
        if (ac?.ax == 10 || PlayerFsm.isAttackState(S)) return
        when (gI) {
            1 -> {
                ag = 0; ah = 0; aj = 0
                val v = ga
                if (S == 79 && v != null && v.ax == 51 && v.aD != 0) return
                setAnim(if (S == 79) 81 else 67)
                w.kE?.heldRelease(this)              // k.E?.K() (g.java:4370)
            }
            8 -> {
                if (S != 79) {
                    ai = 0; ag = 0; K = 0; cN = 0
                    setAnim(303)
                }
            }
            2 -> { setAnim(286); w.sfx(29) }
        }
    }

    /**
     * `g.aq()` (g.java:3958, proven): the MOUNTED 65568 action — `g==null`
     *  → `i(295)` + `k.v()`; else sfx16, pick the mounted reach anim by
     *  W-box steepness `r06/r05` (<=64→306, <=256→305, <=1024→304,
     *  `r06==0`→306 — no below-target arm unlike `ar()`). Then `K=6`
     *  FORCED, `r08=bu[au]` (no ×2) → always throws
     *  `i.a(_,5,14,av,L,M+30,300)` and applies: ax4 `S30→i(29)`, ax58
     *  lever `i(S+1)`/`i(3)`, else `g.aB -= bu[au]` + `g.C()`. */
    fun mountedInteractAction(w: LevelCellSource, pad: Pad) {
        val t = g
        if (t == null) {
            setAnim(295)
            pad.clearLatches()                           // k.v()
            return
        }
        w.sfx(16)
        val r02 = (W[1] + W[3]) shr 1
        val r04 = (t.W[1] + t.W[3]) shr 1
        val r05 = kotlin.math.abs(((t.W[0] + t.W[2]) shr 1) - ((W[0] + W[2]) shr 1))
        val r06 = kotlin.math.abs(r04 - r02) shl 8
        if (r05 <= 0 || r06 <= 0) {
            if (r06 == 0) setAnim(306)
        } else {
            val r07 = r06 / r05
            if (r07 <= 64) setAnim(306)
            else if (r07 <= 256) setAnim(305)
            else if (r07 <= 1024) setAnim(304)
        }
        K = 6                                            // forced full gauge
        val r08 = WEAPON_DMG[w.weaponSlot]               // i.bu[k.au] — no <<1
        if (K <= 3) return
        w.spawnProjectile(av, L, M + 30)                 // i.a(_,5,14,av,L,M+30,300)
        when {
            t.ax == 4 -> if (t.S == 30) t.setAnim(29)
            t.ax == 58 -> when (t.S) {
                0, 5, 7, 9, 11 -> t.setAnim(t.S + 1)
                2 -> t.setAnim(3)
                else -> {}
            }
            else -> {
                t.aB -= (r08 * K) / 6
                t.hitReact(w)                            // g.C()
            }
        }
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
     * `i.E()` (i.java:2929, proven): settle-sink — each pass pins
     * `ah=1`, sets `b`, rescans sides (`a(true)`), unpins `ah`; if
     * `aR∈{3,5,12}` the entity has found footing and returns, else it
     * sinks `al+=10` and loops. Used by the S54 dismount settle
     * (g.java:2226).
     */
    fun eSettle(world: LevelCellSource) {
        var guard = 0
        while (guard++ < 400) {
            ah = 1; b = true
            collideSides(world, true)
            ah = 0
            if (aR >= 12 || aR == 5 || aR == 3) return
            al += 10
        }
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
     * reads as 20 (blocked). Cells read through `i.e(x,y)`
     * (i.java:15294) so the ax0 S37/257 + `m()` overrides apply when
     * the walker is the player. End conditions per major axis.
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

    /** `i.e(x,y)` (i.java:15294): routed through `e()` so the ax0
     *  overrides apply for a player walker (NPC callers get the raw
     *  read back — `ax != 0` skips both arms). */
    private fun cellForLos(cx: Int, cy: Int, world: LevelCellSource): Int =
        e(world, cx, cy)

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

    /** `g.b(int)` (g.java:374, proven): free/interact-eligible anim set —
     *  states a volume may capture: airborne + locomotion + misc. */
    fun gB(): Boolean = when (S) {
        18, 19, 20, 22, 23, 24, 25, 35, 36, 43, 150, 157, 165, 233,
        242, 243, 263, 264, 265, 266 -> true
        else -> false
    }

    /** `g.c(int)` static (g.java:404, proven): locomotion set checked by
     *  the ax15 L46 arm — {0,1,7,11,12,26,79}. Distinct from `g.b()`. */
    fun gC(): Boolean = when (S) {
        0, 1, 7, 11, 12, 26, 79 -> true
        else -> false
    }

    /** `g.k(int)` (g.java:5434, proven): mount-eligible state whitelist. */
    fun mountableState(): Boolean = when (S) {
        0, 1, 18, 19, 20, 23, 24, 25, 35, 36, 43, 150, 157, 165,
        242, 243, 263, 264, 265, 266, 358 -> true
        else -> false
    }

    /** `an()` (g.java:335, proven): the `a(z2)` resolve gate evaluated in
     *  the `e()` head every tick — `S<=43 || S==150 || {67-69,199,216,217,
     *  298} || {20,49,243,259-266}`. (S20/S49/S243 overlap the `<=43`
     *  range — verbatim.) The side-strip probes run either way; this flag
     *  only gates the wall-push resolve inside `a(z2)`. */
    fun rescanEligible(): Boolean =
        S <= 43 || S == 150 || (S in 67..69) || S == 199 || S == 216 ||
            S == 217 || S == 298 || (S in 259..266) || S == 20 || S == 49 ||
            S == 243

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
        /** `i.j(i)` (i.java:5926, proven): static dead check — null →
         *  true; ax ∈ {11,17,29,27} → the member's `P()` (side-effect
         *  release); any other type → false. Consumed by the ax37 Z[2]
         *  linked-entity gate. */
        fun isDeadCheck(other: Entity?): Boolean {
            if (other == null) return true
            return when (other.ax) {
                11, 17, 29, 27 -> other.deadRelease()
                else -> false
            }
        }

        val ZERO_RECT = IntArray(4)
        /** `i.L`/`i.M` (i.java statics, proven) — last parked marker point
         *  (written by `o()`, read by `b(x,y)` :9829). */
        var markerLx = -1
        var markerLy = -1
        /** `i.bu[]` (i.java:166, proven) — dual use: per-weapon damage
         *  by weapon slot `I` in `j()`, and soldier max-HP by difficulty
         *  `k.au` for the draw-pass HP bar (`i.bu[au]`, k.java:2945). */
        val WEAPON_DMG = intArrayOf(300, 400, 500)
        /** `i.bS` (i.java:119, proven) — class-level entity flag: `j(1)`
         *  ORs bit 0, `k.F()` clears it, and nothing in this build ever
         *  reads it — a dead write-only flag kept verbatim. */
        var entBSLatch = 0
        /** `i.bq` (i.java:163, proven) — crate-top level static: set on
         *  the `i(233)` landing while grounded on an ax51 crate
         *  (`i.bq = al + 20`, g.java:805), cleared when the player drops
         *  past it or enters a `c(S)` grounded state {0,1,7,11,12,26,79}
         *  (g.java:594). Read by `g.m()` (crate/crate-top flag → `e()`
         *  cell-override) and the crate-dismount/fall gates
         *  (g.java:1422/:4991). */
        var entBq = 0
        /** `i.bv[]` (i.java:167, proven) — civilian max-HP by
         *  difficulty `k.au` (spawn init i.java:2340/2399 + the
         *  ax17 HP-bar scale k.java:2947). */
        val NPC_HP_BV = intArrayOf(100, 140, 200)
        /** `k.bi[]` (k.java:266, proven) — default clip index per ax;
         *  `-1` = clipless record spawn. bi[11]=bi[17]=bi[23]=7. */
        val AX_CLIP_BI = intArrayOf(
            0, -1, 1, 2, 3, 1, 4, 60, 5, 47, 6, 7, 8, 61, 9, 25, 10, 7,
            -1, 11, -1, 13, 14, 7, 40, 16, 15, 48, -1, 52, 36, 44, 36,
            -1, 42, 62, -1, -1, -1, -1, 45, 30, -1, 31, 32, 33, 29, 7,
            13, -1, 7, 28, -1, -1, 19, -1, 19, -1, 20, -1, 21, 71, -1,
            -1, 22, -1, 23, -1, 26, 38, 43, -1, 51, 7, 54, 55, 56, -1,
            63, 0, 57)
        /** `i.H[]` (i.java:22318, proven) — carried-entity damage (bc()/bd()). */
        val WEAPON_H = intArrayOf(50, 50, 50)
        /** `i.K[]` (i.java:22322, proven) — ax32 wall-break damage (bc()). */
        val WEAPON_K = intArrayOf(6, 4, 2)
        /** `k.bh[]` (k.java:8437, proven) — per-mission behavior flag;
         *  `bh[k.aj] == 3` picks the S16 settle arm (missions 1/4). */
        val MISSION_BH = intArrayOf(4, 3, 4, 4, 3, 4, 4, 4, 4)
        /** `i.ct[]` (i.java:22335, proven) — key-prompt frame indexes for
         *  the raw bit positions 0-9 (op107/112 prompt art). */
        val CT = intArrayOf(39, 42, 45, 48, 51, 54, 57, 60, 63, 66)
        /** `i.bA[]` (i.java:22336 `new a[4]`, proven) — script-QTE prompt
         *  slots shared across claim runs (class `a` = the prompt sprite). */
        val scriptPrompts = arrayOfNulls<ScriptPrompt>(4)
        /** `i.cs[]` (i.java:184, proven) — pad mask per QTE lane type
         *  (S55 zone's `p` selects the mask the player must press). */
        val CS = intArrayOf(1, 2, 16388, 8, 4112, 65568, 8256, 128, 33024, 512)
        /** `g.u[]` (g.java:6400, proven) — player meter cost per weapon
         *  tier, indexed by `k.au`: {5,10,15}. */
        val PLAYER_DMG = intArrayOf(5, 10, 15)
        /** `d.b[]` (d.java:10, proven) — per-weapon {light,heavy} damage
         *  pairs read by `i.W()` (ax61 boss damage). */
        val DMG_B = intArrayOf(10, 20, 20, 40, 35, 70)

        /** `k.h(int,int)` (k.java:6839, proven): Manhattan-ish magnitude —
         *  `(a+b) - min/2 - min/4 + min/8`. */
        fun magApprox(r4: Int, r5: Int): Int {
            if (r4 == 0 && r5 == 0) return 0
            val a = kotlin.math.abs(r4); val b = kotlin.math.abs(r5)
            val m = minOf(a, b)
            return (a + b) - (m shr 1) - (m shr 2) + (m shr 3)
        }

        /** `j.U[]` (pack resource /16 blob offset 154, proven): the 256-entry
         *  Q4 square-root table — `U[i] = floor(16·sqrt(i))`, except the
         *  verbatim quirk `U[0] = 256` (so `j.d(0)` returns 16, not 0).
         *  Blob-verified 255/256 entries against `resources/archive/16`. */
        private val SQRT_U = IntArray(256) { i ->
            if (i == 0) 256 else (16.0 * kotlin.math.sqrt(i.toDouble())).toInt()
        }

        /** `j.d(int)` (j.java:1097, proven): piecewise table square root —
         *  indexes `U` by a shifting window, so for large `x` the result
         *  quantizes in steps of 256 (NOT the true floor-sqrt). */
        fun isqrt(x: Int): Int = when {
            x < 0 -> 0
            x < 0x100 -> SQRT_U[x] shr 4
            x < 0x400 -> SQRT_U[x shr 2] shr 3
            x < 0x1000 -> SQRT_U[x shr 4] shr 2
            x < 0x4000 -> SQRT_U[x shr 6] shr 1
            x < 0x10000 -> SQRT_U[x shr 8]
            x < 0x40000 -> SQRT_U[x shr 10] shl 1
            x < 0x100000 -> SQRT_U[x shr 12] shl 2
            x < 0x400000 -> SQRT_U[x shr 14] shl 3
            x < 0x1000000 -> SQRT_U[x shr 16] shl 4
            x < 0x4000000 -> SQRT_U[x shr 18] shl 5
            x < 0x10000000 -> SQRT_U[x shr 20] shl 6
            x < 0x40000000 -> SQRT_U[x shr 22] shl 7
            else -> SQRT_U[x ushr 24] shl 8
        }

        /** `k.e(int,int,int,int)` (k.java:6860, proven): the arc/lead
         *  solver — roots of `x² + r5·x − r4 = 0` (the `1`-coefficient is
         *  hardcoded; the `r6` arg is dead in the original): returns
         *  `(r7 / max(r62,r42)) << 8`, or -1 when both roots are ≤ 0. */
        fun arcSolve(r4: Int, r5: Int, r7: Int): Int {
            val disc = r5 * r5 - 4 * (-r4)
            val r62 = if (disc >= 0) (isqrt(disc) - r5) / 2 else -1
            val r42 = if (disc >= 0) (-isqrt(disc) - r5) / 2 else -1
            if (r62 <= 0 && r42 <= 0) return -1
            return (r7 / maxOf(r62, r42)) shl 8
        }
        /** `i.at` (i.java:42) — static mount/assassination link; set by
         *  az()'s ax72 arm and the ax11 grab arm (i.java:6007). */
        var at: Entity? = null
        /** `i.aL` (i.java:75) — the claim-script camera-focus entity;
         *  `k.m`'s snap arm (`k.java:2354`) and `n()` clear it. */
        var aL: Entity? = null
        /** `i.L`/`i.M` (i.java:9825 `o(x,y)`) — the static indicator
         *  point; -1 = unset (cleared by `U()`). */
        var L = -1
        var M = -1
        /** `i.bq` — static cleared on grab (`c()` head, g.java:4118). */
        var bq = 0
        /** `g.j` (g.java:60, proven) — static grab/pass latch: set by the
         *  S146/S147 wall-sequence arms (g.java:2865/:2898), cleared by
         *  `i.D()` entity-pool init (i.java:1817/:1977), and read by the
         *  default `e()` arm's `r() && !j` fall guard (g.java:1146) plus
         *  the i.java:7180/:15564 mount/overlap gates. */
        var grabLatch = false
        /** `g.q` (g.java:63, proven) — static wall-run-zone latch: set by
         *  the ax10 S10 zone arm while the player overlaps it
         *  (i.java:9343-9363), read by the S317 arm's `!q` release gate
         *  (g.java:4093). Cleared by `i.D()` pool init (i.java:1818). */
        var gq = false
        /** `g.f` (g.java:10, proven) — static wall-run marker-FX entity
         *  (`i.a(8,30,4,...)` spawned by the S317 arm, g.java:4083);
         *  the marker's own FSM checks `g.f == this` (i.java:6122). */
        var gf: Entity? = null
        /** `g.E` (g.java:36, proven) — static jump-tail suppress latch:
         *  set while the player overlaps an ax10 S55 zone
         *  (i.java:9843-9860), read as `cq && !E` in the shared e()
         *  jump tail (g.java:788). Without it the tail's `i(233/22/21)`
         *  overrides the arm-level `i(17)` wall-kick — the wall-run
         *  family is only reachable inside these zones. */
        var gE = false
        /** `i.cu` (i.java static, proven) — world-freeze flag set by
         *  the ax10 S55 arm's `Z[0]!=0` branch (i.java:9857); the I()
         *  L109 early-out (i.java:15294 — every non-ax10 entity skips
         *  its tick) is wired in `Level0World.tickNpc`. */
        var icu = false
        /** `g.cn` (g.java:35, proven) — per-tick counter incremented in
         *  `e()` (g.java:580) and cleared on the arm at g.java:3757;
         *  NEVER READ anywhere — write-only. Ported for parity. */
        var gCn = 0
        /** `i.a(int[],int[])` (i.java:632, proven) — inclusive-edge overlap. */
        /** `i.a(int,int,int[])` (i.java:684, proven): inclusive
         *  point-in-rect — `x∈[W0,W2] && y∈[W1,W3]`. */
        fun pointInBox(x: Int, y: Int, W: IntArray): Boolean =
            x >= W[0] && x <= W[2] && y >= W[1] && y <= W[3]

        fun overlapI(a: IntArray, b: IntArray): Boolean =
            a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1]

        /** `i.a(int[],int[])` (i.java:632, proven): `overlapI` plus the
         *  point-box rejects — a fully-degenerate rect on either side
         *  (x0==x2 && y0==y3) never overlaps. */
        fun overlapStrict(a: IntArray?, b: IntArray?): Boolean =
            a != null && b != null &&
            a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1] &&
            !(a[0] == a[2] && a[1] == a[3]) &&
            !(b[0] == b[2] && b[1] == b[3])
        /** `i.a(x0,y0,x1,y1,rect)` (i.java:601, proven): the edges-box
         *  vs rect overlap — same overlap + both-side point rejects. */
        fun edgeRectOverlap(x0: Int, y0: Int, x1: Int, y1: Int, r: IntArray): Boolean =
            x0 <= r[2] && x1 >= r[0] && y0 <= r[3] && y1 >= r[1] &&
                (x0 != x1 || y0 != y1) && (r[0] != r[2] || r[1] != r[3])
        /** `i.b(int[],int[])` (i.java:666, proven): `a` fully CONTAINED
         *  in `b` (all four edges inside). */
        fun containRect(a: IntArray, b: IntArray): Boolean =
            a[0] >= b[0] && a[1] >= b[1] && a[2] <= b[2] && a[3] <= b[3]
        /** `g.b(int)` (g.java:374, proven): player states the ax35
         *  grabber may latch onto (idle/walk/jump/land/fall/climb/pickup
         *  family — 20 states, no 21). */
        val GRABBABLE_STATES = intArrayOf(
            18, 19, 20, 22, 23, 24, 25, 35, 36, 43,
            150, 157, 165, 233, 242, 243, 263, 264, 265, 266)
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
            // i.a(op4) (i.java:14722-14772, proven): `k.E.P|=128` head;
            // `aS.S∈{284,285,50}` early-return (aS=player — all callers
            // hit the player so S==aS.S); `r13.ax==61 && g.a(r13) →
            // r9.c(r13)` boss arm; `r9.S!=9 → g.a() && r13.ax∉{17,50,61}
            // → r9.c(r13)` — the VICTIM hit-reacts on the attacker (was
            // inverted: it staggered the attacker on its own landed
            // hits); `L423 → A(18)` hurt sfx fires unconditionally.
            4 -> {
                world.kE?.let { it.P = it.P or 128 }         // k.E.P |= 128
                if (S == 284 || S == 285 || S == 50) return
                if (attacker != null && attacker.ax == 61 &&
                    playerDamageable(attacker, world)) {
                    counteredBy(attacker, world)             // r9.c(r13)
                }
                if (S != 9 && playerDamageable(g, world) && attacker != null &&
                    attacker.ax != 17 && attacker.ax != 50 && attacker.ax != 61) {
                    counteredBy(attacker, world)             // r9.c(r13)
                }
                world.sfx(18)
            }
            // op18 (clash knockdown): `g.a()` pays u[au] inside d() — the
            // gates apply (S67 clash drains nothing) — then `i(43)`
            // unconditionally.
            18 -> { playerDamageable(g, world); setAnim(43) }
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
            // op32 (i.java:4639-4652 L16, proven): stance-break — aA<=1
            // with the attacker within ±150px clears aA to 0; aA>1 with
            // the 16-flag sets Z[0]=3000 (counter-bleed reset). Ops from
            // the aA-branch in I() target the player.
            32 -> {
                if (aA <= 1) {
                    if (attacker != null) {
                        val dx = ak - attacker.ak
                        if (dx > -150 && dx < 150) aA = 0
                    }
                } else if (aA and 16 != 0) Z[0] = 3000
            }
            // op11 (i.java:4761 L167, proven): spring-pad bounce intake —
            // pin to the pad's top edge, launch `ah=src.Z[1]` /
            // `ag=src.Z[0]` (record r8[9]/r8[8]<<8), anim 24 when the pad
            // has no throw dir (Z[0]==0) else 22; facing = !src.av for S11
            // pads else the Z[0] sign.
            11 -> {
                if (attacker != null) {
                    if (attacker.Z[0] != 0) setAnim(22) else setAnim(24)
                    al = attacker.W[1]; ak = attacker.ak
                    ah = attacker.Z[1]
                    if (attacker.S == 11) av = !attacker.av
                    else if (attacker.Z[0] > 0) av = false
                    else if (attacker.Z[0] < 0) av = true
                    ag = attacker.Z[0]
                }
            }
            // op24 (i.java:4520 L49, proven): pin the player at the
            // source's top-left corner playing anim r11, all velocities
            // zeroed — the trap-grabbed pose (ax46 uses 330/110).
            24 -> {
                if (attacker != null) {
                    setAnim(arg); aj = 0; ah = 0; ag = 0
                    ak = attacker.W[0]; al = attacker.W[1]
                }
            }
            // L75 (proven structure): marker-engage — `g.b = r13`, `aB=3`,
            // `o()?i(3)`, face + push ±512 toward the marker. The `g.a()`
            // damage-gate chain ported as `playerDamageable`.
            38 -> {
                if (S == 3 || S == 6 || S == 7) return
                if (!playerDamageable(g, world)) return   // g.a() gate now
                world.playerLinkB = attacker               // ported
                aB = 3
                if (oState()) setAnim(3)
                av = attacker != null && attacker.ak < ak
                ag = if (av) 512 else -512
            }
        }
    }

    /** `i.a(6, anim, snapX, attacker)` victim arm (i.java:4586 L142,
     *  proven): play `anim` then per-anim handling —
     *  49 (mid-range backstab): vel0 + `ag=((snapX-ak)/10)<<8` slide +
     *     `k.e(0,aw)` + `k.o(3)` + `k.A(20)`;
     *  283 (near backstab): `ak=attacker.ak ∓10` (`av==true→+10` —
     *     i.java:4599-4609) + vel0 + same stats/sfx;
     *  other (90 = ceiling kill): `ak=snapX`; `k.bK` → `a(8,59,…)` marker;
     *     anim==90 → `al=attacker.al` + vel0; `attacker.ax==47` adds
     *     `k.e(0,aw)`, `k.o(3)` runs for every 90. */
    fun applyHit6(anim: Int, snapX: Int, attacker: Entity?, world: LevelCellSource) {
        setAnim(anim)
        when (anim) {
            49 -> {
                aj = 0; ah = 0; ag = 0
                ag = ((snapX - ak) / 10) shl 8
                world.kStatE(aw); world.kAp[3]++; world.sfx(20)
            }
            283 -> {
                val ax2 = attacker?.ak ?: ak
                ak = ax2 + if (av) 10 else -10
                aj = 0; ah = 0; ag = 0
                world.kStatE(aw); world.kAp[3]++; world.sfx(20)
            }
            else -> {
                ak = snapX
                if (world.kBK && attacker != null)
                    spawnFx8(world, 59, 1, av, attacker.ak, al, 300)
                if (anim == 90) {
                    // ax47 attacker → k.e(0,aw) stat; every attacker → k.o(3).
                    if (attacker?.ax == 47) world.kStatE(aw)
                    world.kAp[3]++
                } else {
                    al = attacker?.al ?: al
                }
                aj = 0; ah = 0; ag = 0
            }
        }
    }

    /** `i.o()` (i.java:6597, proven): `S∈{2,20..29} → false`, else true. */
    fun oState(): Boolean = !(S == 2 || S in 20..29)

    /**
     * `i.c()` (i.java:1445, proven): left-edge wall guard — scans cells
     *  `W[0]-r10·20 .. W[2]+r10·20` on the `(W[1]%260+260)/20` row;
     *  `aT<10` (a wall cell within 4 left tiles) → true; `aU<10` → false
     *  early. `g.d()` skips damage while this holds. */
    fun nearLeftWall(w: LevelCellSource): Boolean {
        val r0 = (W[1] % 260) + 260
        var r10 = 1
        while (r10 < 5) {
            aT = e(w, (W[0] - r10 * 20) / 20, r0 / 20)
            aU = e(w, (W[2] + r10 * 20) / 20, r0 / 20)
            if (aT < 10) return true
            if (aU < 10) return false
            r10++
        }
        return false
    }

    /** `i.W()` (i.java:10300, proven): the entity's damage value —
     *  `d.b[k.au<<1]` on idle/even states {2,4,10,17}, `d.b[(k.au<<1)+1]`
     *  on `S==13` (windup), else 0. `d.b={10,20,20,40,35,70}` per-weapon
     *  {light,heavy} pairs (d.java:10). */
    fun bossDamage(w: LevelCellSource): Int = when {
        S == 2 || S == 4 || S == 10 || S == 17 -> DMG_B[w.weaponSlot shl 1]
        S == 13 -> DMG_B[(w.weaponSlot shl 1) + 1]
        else -> 0
    }

    /**
     * `g.d(int)` (g.java:3885, proven): the player meter drain — guards
     *  `s` godmode, `t` iframes, `aS.c()` wall-guard, `S∈{67,183,184}`
     *  attack/finisher immunity, `S==205` (assassination victim — the
     *  decompiler's `goto L35` reads as a skip, inferred); then
     *  `i.bh = 8` (global hit-lock), `x[1] -= amt`; `x[1]<=0` → death
     *  release (`bh[k.aj]==3` missions survive at 0; else `aZ` skips the
     *  `E()` settle, `bl=0; G(); H(); a=null`); `x[1]>0 → t=10`.
     *  `w.failed` mirrors the existing k.l(12) KO path (inferred). */
    fun gDrain(amt: Int, w: LevelCellSource) {
        if (w.godMode) return
        if (gt != 0) return
        // `k.aS.c()` (g.java:421) — mid-combo anims 112-115 block the drain;
        // earlier read had this as nearLeftWall — corrected.
        if (S in 112..115) return
        if (S == 67 || S == 183 || S == 184) return
        if (S == 205) return
        w.iBh = 8
        x1 -= amt
        if (x1 <= 0) {
            x1 = 0
            if (w.missionBh() == 3) return
            if (!aZ && standingOn == null) settleToGround(w)
            bl = 0
            releaseAe(); consumeH()                 // G() + H()
            standingOn = null
            return                              // x1==0 → tick missionFail
        }
        gt = 10
    }

    /**
     * `g.a(i r3)` (g.java:139, proven): the "should this hit land" gate —
     *  `i.bh != 0` (hit-lock) → false; `h()` invulnerable → false;
     *  `g()` dead → `return true` (no further drain); alive →
     *  `d(r3?.ax==61 ? r3.W()(/3 when aS.S==6) : u[k.au])` then true.
     *  The `g.a()` no-arg caller passes `r3 = g` (the grab link). */
    fun playerDamageable(attacker: Entity?, w: LevelCellSource): Boolean {
        if (w.iBh != 0) return false
        if (w.playerInvulnerable()) return false
        if (!w.playerDead()) {
            val amt = if (attacker != null && attacker.ax == 61) {
                var v = attacker.bossDamage(w)
                if (S == 6) v /= 3
                v
            } else PLAYER_DMG[w.weaponSlot]
            gDrain(amt, w)
        }
        return true
    }


    /** `i.u()`+`i.v()` (i.java:700/730, ax16 subset, proven): view-proximity
     *  score `au = |ak-(kO+200)|/400 + |al-(kP+120)|/120` — the marker
     *  survives while `au > i`. */
    fun markerVisible(w: LevelCellSource): Boolean {
        au = kotlin.math.abs(ak - (w.kO + 200)) / 400 +
            kotlin.math.abs(al - (w.kP + 120)) / 120
        return au > i
    }

    /**
     * `i.bd()` (i.java:14538, proven): the marker's rest sweep — X-overlap
     *  neighbors: ax19 `S==2 → i(3)`; while THIS flies (`S==17`), ax17
     *  (`S!=69`) and — when the sweeper is ax23 — any `S!=79` take
     *  `aB -= H[au]`; death → ax17 `i(129)`/ax23 `i(79)`, alive →
     *  `i(68)`/`i(73)`. */
    fun sweepNeighbors(w: LevelCellSource): Boolean {
        var r6 = false
        for (r0 in w.npcs) {
            if (r0 === this) continue
            if (!overlapI(r0.W, X)) continue
            if (r0.ax == 19 && r0.S == 2) { r0.setAnim(3); r6 = true }
            if (S != 17) continue
            if (r0.ax == 17 && r0.S != 69) { sweepHit(w, r0); r6 = true; continue }
            if (ax == 23 && r0.S != 79) { sweepHit(w, r0); r6 = true }
        }
        return r6
    }

    /** bd() L28-L42 — `aB -= H[k.au]` then the ax17/23 hit/death anims. */
    private fun sweepHit(w: LevelCellSource, r0: Entity) {
        r0.aB -= WEAPON_H[w.weaponSlot]
        if (r0.aB <= 0) {
            if (r0.ax == 17 && r0.S != 69) r0.setAnim(129)
            if (r0.ax == 23) r0.setAnim(79)
        } else {
            if (r0.ax == 17) r0.setAnim(68)
            else if (r0.ax == 23) r0.setAnim(73)
        }
    }

    /**
     * `i.bc()` (i.java:14396, proven): the flight-impact sweep — X-overlap
     *  neighbors by type. Own `af` (thrower) is protected: `af.ax` in the
     *  prop family skips matching `r0.ax` (e.g. af==54 → ax54s skipped).
     *  `d(8,…)` floatie spawns = `spawnDebris24` (ported slice 125). `cF` is the i-STATIC
     *  gauge-full flag (i.java:184) — ax32 `S∈[21,27]` (armed walls) only
     *  break on full-gauge throws; when unset the scan ABORTS. */
    fun sweepNeighborsB(w: LevelCellSource): Boolean {
        var r6 = false
        val afAx = af?.ax
        scan@ for (r0 in w.npcs) {
            if (r0 === this) continue
            if (!overlapI(r0.W, X)) continue
            when (r0.ax) {
                54 -> {
                    if (afAx == 54 || afAx == 30) continue
                    r0.ad?.let { a ->
                        if (overlapI(a.W, X)) {
                            a.setAnim(2)
                            spawnDebris24(w, 8, a.ak, a.al)   // d(8, ak, al)
                        }
                    }
                    r0.setAnim(10); w.statTally(r0.aw); setAnim(9); r6 = true
                    break@scan
                }
                30 -> {
                    if (afAx == 54 || afAx == 30 || afAx == 56) continue
                    r0.aB -= 20; r0.cGCount = 6
                    if (r0.aB <= 0) {
                        r0.cGCount = 0; r0.setAnim(10)
                        w.statTally(r0.aw); setAnim(9)
                        r0.ad?.let { a ->
                            a.setAnim(2)
                            spawnDebris24(w, 8, a.ak, a.al) // d(8, ak, al)
                        }
                    }
                    r6 = true; break@scan
                }
                56 -> {
                    if (afAx == 54 || afAx == 56 || afAx == 30) continue
                    r0.setAnim(10); w.statTally(r0.aw); setAnim(9); r6 = true
                    break@scan
                }
                67 -> {
                    if (r0.S in intArrayOf(19, 21, 23, 32, 35, 38, 41, 43)) {
                        if (S != 9) r0.aB--          // L71-L78: S9 → skip dec
                        if (r0.aB <= 0) r0.setAnim(r0.S + 1)
                        setAnim(9); r6 = true
                    }
                }
                24 -> {
                    if (r0.S == 19) { r0.setAnim(20); setAnim(9); r6 = true }
                }
                32 -> {
                    if ((r0.l and 1) == 0) continue  // `l` parity gate
                    if (r0.S in 21..27) { if (!w.cFFlag) break@scan }  // L87
                    if (r0.S == 20) continue
                    if (r0.aB > 0) r0.aB -= WEAPON_K[w.weaponSlot]
                    when (r0.pv) {                 // L97 p-switch
                        0 -> if (r0.aB <= 0) { r0.setAnim(15); r0.cGCount = 0 }
                            else r0.cGCount = 6
                        2 -> if (r0.aB <= 0) { r0.setAnim(19); r0.cGCount = 0 }
                            else r0.cGCount = 6
                        3 -> if (r0.aB <= 0) { r0.setAnim(25); r0.cGCount = 0 }
                            else r0.cGCount = 6
                        4 -> if (r0.aB <= 0) { r0.setAnim(36); r0.cGCount = 0 }
                            else r0.cGCount = 6
                        else -> {}
                    }
                    r6 = true
                }
                else -> {}
            }
        }
        return r6
    }

    // ================= mission-director helpers (i.java:18003+) =============
    // `bD()` itself lives in NpcFsm.tickDirector; these are the entity-side
    // helpers it and its linked pursuers call.

    /** `i.b(boolean)` (i.java:6635, proven): `ad` follower mirror — `ad.P|=1`
     *  when `ad.av` else `&=-2`; copies pos/vel/impulse; `ad.t()`. */
    fun syncAd(world: LevelCellSource) {
        val d = ad ?: return
        if (d.av) d.P = d.P or 1 else d.P = d.P and -2
        d.ak = ak; d.al = al
        d.ag = ag; d.ah = ah; d.ai = ai; d.aj = aj
        d.settleToGround(world)
    }

    /** `i.u()` (i.java:575-594, proven): `au` = coarse camera-distance
     *  score — `|ak-(O+200)|/400 + |al-(P+120)|/N`, `N` = 240 for the
     *  ax13-`aG==4`/ax21 family + ax67 `bk[Z0]==49`, 800-x for `bk==27`,
     *  else 120. `k.I()` reads it for the eligibility + copy-group arms. */
    fun recomputeAu(camX: Int, camY: Int, decorClip: (Int) -> Int) {
        var x = ak - (camX + 200); if (x < 0) x = -x
        var y = al - (camY + 120); if (y < 0) y = -y
        au = when {
            (ax == 13 && aG == 4) || ax == 21 -> x / 400 + y / 240
            ax == 67 && decorClip(Z[0]) == 49 -> x / 400 + y / 240
            ax == 67 && decorClip(Z[0]) == 27 -> x / 800 + y / 240
            else -> x / 400 + y / 120
        }
    }

    /** `i.bE()` (i.java:18917): waypoint-coords → world — `ak=bY; al=k.P+bZ`. */
    fun posFromWaypoint(world: LevelCellSource) { ak = bY; al = world.kP + bZ }

    /** `i.bF()` (i.java:18921): world → waypoint-coords — `bY=ak; bZ=al-k.P`. */
    fun posToWaypoint(world: LevelCellSource) { bY = ak; bZ = al - world.kP }

    /** `i.u()` (i.java:700, proven): off-screen distance score into `au` —
     *  `|ak-(k.O+200)|/400 + |al-(k.P+120)|/240` for ax13/ax21 (and ax67
     *  riding `k.bk[Z[0]]==49`), `r6/400+r7/120` otherwise, ax67-on-27 →
     *  `r6/800+r7/240`. Same shape as `markerVisible` (ax16 → /120 arm). */
    fun offscreenScore(world: LevelCellSource) {
        var r6 = ak - (world.kO + 200); if (r6 < 0) r6 = -r6
        var r7 = al - (world.kP + 120); if (r7 < 0) r7 = -r7
        au = when {
            ax == 21 || (ax == 13 && aG == 4) -> (r6 / 400) + (r7 / 240)
            ax == 67 && world.kBk(Z[0]) == 49 -> (r6 / 400) + (r7 / 240)
            ax == 67 -> if (world.kBk(Z[0]) == 27) (r6 / 800) + (r7 / 240)
                        else (r6 / 400) + (r7 / 120)
            else -> (r6 / 400) + (r7 / 120)
        }
    }

    /** `i.v()` (i.java:730, proven): "is-in-play" predicate — ax49 → true;
     *  ax29 `S==24 && T>=54` → true; ax10/40/60 `P&16` → true; ax27 `S==6
     *  && Z[1]>0` → true; ax21 `S>=2` → true else falls to the score arm;
     *  `u()` → `au > i` → false (out of play); in-range → ax60 true,
     *  ax11 `Z[8]==888` true, ax14 `S==76`/`W==null`/`aS.W` overlap,
     *  ax37/10/60/78(S==3) → `a(k.ac,W)`, else `a(k.ac,Y)`. */
    fun inPlayV(world: LevelCellSource): Boolean {
        if (ax == 49) return true
        if (ax == 29 && S == 24 && T >= 54) return true
        if (ax == 10 || ax == 40 || ax == 60) {
            if ((P and 16) != 0) return true
        }
        if (ax == 27 && S == 6 && Z[1] > 0) return true
        if (ax == 21 && S >= 2) return true
        offscreenScore(world)
        if (au > i) return false
        if (ax == 60) return true
        if (ax == 11 && Z[8] == 888) return true
        if (ax == 14) {
            if (S == 76) return true
            if (W.isEmpty()) return true
            if (world.missionBh() == 3 || S == 69 || S == 70 || S == 71)
                return overlapI(world.camRect, W)
            return overlapI(world.playerRect(), W)
        }
        if (ax == 37 || ax == 10 || ax == 60 || (ax == 78 && S == 3))
            return overlapI(world.camRect, W)
        return overlapI(world.camRect, Y)
    }

    /** `i.f(int)` static (i.java:18023, proven): one-shot phase gate —
     *  `cE != x → cE = x; true`. */
    fun directorGate(x: Int, world: LevelCellSource): Boolean =
        if (world.iCE != x) { world.iCE = x; true } else false

    /** `i.a(int,int,int,int)` (i.java:4799, proven): the `aK` child factory —
     *  `ax/clip/anim/az`, pos+facing copied from `this`, `t()`, NOT `k.b` —
     *  the caller mutates `aK` then inserts it (ported: caller adds the
     *  returned entity to `pendingInsert`). */
    fun spawnChildFx(world: LevelCellSource, ax: Int, clip: Int,
                     anim: Int, az: Int): Entity {
        val r0 = Entity(ax, world.clipFor(clip))
        r0.aw = -1; r0.au = 0
        r0.setAnim(anim)
        r0.az = az
        r0.ak = ak; r0.al = al; r0.av = av
        r0.settleToGround(world)
        return r0
    }

    /** `i.d(i,x,y)` (i.java:16745, proven): spawn the ax24/clip40 `S=i`
     *  score floatie at pixel (x,y) — `a(24,40,i,201)` child, then
     *  `av=false`, `N/O` = 8.8 pos, `ak/al` = pos, vel 0, `t()`, `k.b`
     *  insert. (spawnChildFx's settleToGround runs before the pos
     *  re-anchor, so it's a no-op vs the raw a() — flagged.) */
    fun spawnFloatie(world: LevelCellSource, i: Int, x: Int, y: Int) {
        val aK = spawnChildFx(world, 24, 40, i, 201)
        aK.av = false
        aK.N = x shl 8; aK.O = y shl 8
        aK.ak = x; aK.al = y
        aK.aj = 0; aK.ai = 0; aK.ah = 0; aK.ag = 0
        aK.refreshBoxes()                        // t()
        world.queueInsert(aK)                    // k.b(aK)
    }

    /** `i.a(x,y,tx,ty,ax,clip,S,az)` (i.java:21212, proven): 4-arg spawn
     *  + ax74 burst-particle customization — `ag/ah = j.a(-6,6)<<8`,
     *  `Z[8]` arc table `{x-kO, y-kP, tx-kO, ty-kP, midX+rand±80, y-kP,
     *  0, rand(10,16)}`, `P=528`, then `k.b` insert. */
    fun spawnFlyBurst(w: LevelCellSource, x: Int, y: Int, tx: Int, ty: Int,
                      ax: Int, clip: Int, anim: Int, az: Int): Entity {
        val aK = spawnChildFx(w, ax, clip, anim, az)
        if (ax == 74) {
            aK.ag = w.jRand(-6, 6) shl 8
            aK.ah = w.jRand(-6, 6) shl 8
            val z0 = x - w.kO; val z1 = y - w.kP
            val z2 = tx - w.kO; val z3 = ty - w.kP
            aK.Z.fill(0)                                          // Z = new int[8]
            aK.Z[0] = z0; aK.Z[1] = z1; aK.Z[2] = z2; aK.Z[3] = z3
            aK.Z[4] = ((z0 + z2) shr 1) + w.jRand(-80, 80)
            aK.Z[5] = z1
            aK.Z[6] = 0; aK.Z[7] = w.jRand(10, 16)
            aK.P = 528
        }
        w.queueInsert(aK)
        return aK
    }

    /**
     * `i.b(int,int,int,int,int)` (i.java:4818-4845, proven) — the knife/
     * projectile spawn: `a(5,1,8,300)` child, `W/X/Y` boxes re-seeded
     * from the thrower, `n=1`, `aG=r7` (script uid), `P|128`, `Z` wiped
     * with `Z[2]=Z[3]=-1`. `aG==-1` → plain insert; otherwise
     * `h(k.s(aG))` binds the claim script + `P|512` (the `Z[1]>=16`
     * early-out is dead code — `Z[1]==0` here). Args `r8..r11` are
     * accepted but unused verbatim — every caller passes `0,0,-1,-1`.
     * `proven-dead` call path: both callers gate on `k.aY[0] != null`
     * and no bytecode ever assigns `k.aY[i]` a non-null entity.
     */
    fun spawnKnife(world: LevelCellSource, aG: Int) {
        val aK = spawnChildFx(world, 5, 1, 8, 300)
        System.arraycopy(W, 0, aK.W, 0, W.size)
        aK.aE = 0; aK.aF = 0; aK.nl = 1
        aK.aG = aG; aK.aD = 0; aK.m = 0
        aK.P = aK.P or 128
        aK.Z.fill(0); aK.Z[2] = -1; aK.Z[3] = -1
        if (aK.aG != -1) {
            if (aK.Z[1] >= 16) { world.queueInsert(aK); return }   // L5 — dead
            aK.bindScript(world.kSIndex(aK.aG), world)             // h(k.s(aG))
            aK.P = aK.P or 512
        }
        world.queueInsert(aK)                                      // k.b(aK)
    }

    /** `i.a(ax,clip,anim,face,x,y,az)` (i.java:6898, proven): the ax8
     *  counter-spark factory — `aw=-1, au=0, P|=512`, inserted via `k.b`. */
    fun spawnFx8(w: LevelCellSource, clip: Int, anim: Int, face: Boolean,
                 x: Int, y: Int, az2: Int): Entity {
        val r0 = Entity(8, w.clipFor(clip))
        r0.aw = -1; r0.au = 0
        r0.az = az2
        r0.ak = x; r0.al = y
        r0.av = face
        r0.P = r0.P or 512
        r0.setAnim(anim)
        w.queueInsert(r0)
        return r0
    }

    /** `i.d(int,int,int)` (i.java:16745, proven): spawn an ax24
     *  clip-40 debris spark — `a(24,40,anim,201)`, `av=false`, position
     *  `(x,y)` with N/O matching, zeroed velocity, queued via `k.b`. */
    fun spawnDebris24(w: LevelCellSource, anim: Int, x: Int, y: Int) {
        val e = Entity(24, w.clipFor(40))
        e.av = false
        e.N = x shl 8; e.O = y shl 8
        e.ak = x; e.al = y
        e.aj = 0; e.ai = 0; e.ah = 0; e.ag = 0
        e.setAnim(anim); e.az = 201
        e.refreshBoxes()
        w.queueInsert(e)
    }

    /** `i.d(int,x,y,az)` (i.java:11076, proven): ax61/clip71 child —
     *  `a(61,71,anim,99)`, then `ak/al/az` overwritten, `k.b` insert. */
    fun spawnBossFx(w: LevelCellSource, anim: Int, x: Int, y: Int, az2: Int) {
        val aK = spawnChildFx(w, 61, 71, anim, 99)
        aK.ak = x; aK.al = y; aK.az = az2
        w.queueInsert(aK)
    }

    /** `i.a(x0,y0,x1,y1,ax,clip,anim,az)` (i.java:21212, proven): the
     *  param-curve projectile spawn — for ax61 `Z[0..3]` = screen-space
     *  endpoints, `Z[8..9]` = world dest, `Z[4]` = ctrl mid-x, `Z[5]` =
     *  src-y-100, `Z[6..7]` = progress/period 16, `P=528`, pos at (x0,y0),
     *  plus `aK.a(true,0)` trail-arm. `k.O`/`k.P` = camera origin. */
    fun spawnPathFx(w: LevelCellSource, x0: Int, y0: Int, x1: Int, y1: Int,
                    ax2: Int, clip: Int, anim: Int, az2: Int): Entity {
        val aK = spawnChildFx(w, ax2, clip, anim, az2)
        if (ax2 == 61) {
            aK.Z.fill(0, 0, 10)
            aK.Z[0] = x0 - w.kO; aK.Z[1] = y0 - w.kP
            aK.Z[2] = x1 - w.kO; aK.Z[3] = y1 - w.kP
            aK.Z[8] = x1; aK.Z[9] = y1
            aK.Z[4] = (aK.Z[0] + aK.Z[2]) shr 1
            aK.Z[5] = aK.Z[1] - 100
            aK.Z[6] = 0; aK.Z[7] = 16
            aK.P = 528
            aK.ak = x0; aK.al = y0
            aK.startTrail()
        }
        w.queueInsert(aK)
        return aK
    }

    /** `i.a(true,0)` (i.java:21488, proven): arm the 5-dot afterimage
     *  trail — `cU[0]` = self pos, `cU[1..4]` parked at (-200,-120),
     *  `cV=true` fadeout-on, `cW=0`. `cU` occupied → no-op. */
    fun startTrail() {
        if (cU != null || clip == null) return
        val t = IntArray(10)
        t[0] = ak; t[1] = al
        for (i in 1..4) { t[i * 2] = -200; t[i * 2 + 1] = -120 }
        cU = t; cV = true; cW = 0
        trailAnim = S
        trailClock = 0
    }

    /** `i.af()` (i.java:21513, proven): ring-shift the trail dots —
     *  `cU[i+1] = cU[i]`, then `cU[0]` = current pos. The card clock
     *  ticks here (`ah()`→`b(j.g)` in the original runs per draw; j.g is
     *  ~1 in gameplay so +1/push is the same rate — `inferred`). */
    fun pushTrail() {
        val t = cU ?: return
        for (i in 3 downTo 0) { t[(i + 1) * 2] = t[i * 2]; t[(i + 1) * 2 + 1] = t[i * 2 + 1] }
        t[0] = ak; t[1] = al
        trailClock++
    }

    /** `a.b(i)`/`a.f()` (a.java:60,112-142, proven): the trail cards'
     *  frame position — walk `dur*40`ms thresholds through `trailAnim`,
     *  wrapping forever (`h=-2` < 0 → `f=0` loop, never `i=true`). */
    fun trailFrame(): Int {
        val c = clip ?: return 0
        if (trailAnim < 0 || trailAnim >= c.animCount()) return 0
        val n = c.frameCount(trailAnim)
        if (n <= 0) return 0
        var rem = trailClock
        var f = 0
        var guard = 0
        while (guard++ < 1024) {
            val d = c.frameDuration(trailAnim, f) * 40
            if (d <= 0 || rem < d) break
            rem -= d
            f = (f + 1) % n
        }
        return f
    }

    /** `i.ag()` (i.java:21529, proven): trail armed. */
    fun hasTrail(): Boolean = cU != null

    /** `i.bP()` (i.java:21535, proven): release the trail — the `cV`
     *  fade `d.g(cW,255)` / `d.h(cW,1)` pair is render-side (`inferred`,
     *  the port drops the draw calls and clears the buffer). */
    fun endTrail() {
        if (cU != null) cU = null
    }


    /** `i.e(int,x,y,az)` (i.java:11084, proven): the ck-aura manager —
     *  first call spawns `a(61,71,anim,99)` into `i.ck` (`P&=-129&-33`,
     *  `k.b`), later calls re-arm `ck.i(anim)` + repos; tail always
     *  `ck.az=az2, ck.av=av`. */
    fun bossAura(w: LevelCellSource, anim: Int, x: Int, y: Int, az2: Int) {
        val ck = w.iCk
        if (ck == null) {
            val aK = spawnChildFx(w, 61, 71, anim, 99)
            aK.ak = x; aK.al = y
            w.iCk = aK
            aK.P = aK.P and -129
            aK.P = aK.P and -33
            w.queueInsert(aK)
        } else {
            ck.setAnim(anim)
            ck.ak = x; ck.al = y
            ck.P = ck.P and -129
            ck.P = ck.P and -33
        }
        w.iCk?.let { it.az = az2; it.av = av }
    }

    /** `i.b(int)` (i.java:21728, proven): slow-mo driver arm —
     *  `aH=true, aI=r3, k.aw=0`; `k.bh[k.aj]==3` (missions 1/4) saves
     *  `k.X→aJ` (or `k.W` when set) and scales `k.X /= r3`. Same body
     *  as `eventArm` — i.java has one `b(int)`. */
    fun timewarp(w: LevelCellSource, r3: Int) {
        w.iAH = true; w.iAI = r3; w.kAw = 0
        if (Entity.MISSION_BH[w.kAj] != 3) return
        w.iAJ = w.kX
        if (w.kW != 0) { w.iAJ = w.kW; w.kX = w.kW; w.kW = 0 }
        w.kX /= r3
    }

    /** `i.O()` (i.java:21749, proven): slow-mo driver release —
     *  `aH=false, k.aw=0`; `k.bh[k.aj]==3` restores `k.X` from `aJ` (or
     *  `k.W`) and clears `aJ`. Same body as `eventDisarm`. */
    fun timewarpOff(w: LevelCellSource) {
        w.iAH = false; w.kAw = 0
        if (Entity.MISSION_BH[w.kAj] != 3) return
        if (w.kW != 0) w.iAJ = w.kW
        if (w.iAJ != 0) w.kX = w.iAJ
        w.iAJ = 0
    }

    /** `k.o()` (k.java:3429, proven): input-lock arm `am=true,dd=false`. */
    fun lockInput(w: LevelCellSource) { w.kAm = true; w.kDd = false }
    /** `k.p()` (k.java:3434, proven head): release `am=false,dd=false`
     *  plus `j.b(0,false)`/`j.i(0)` UI resets (render-side, `inferred`). */
    fun unlockInput(w: LevelCellSource) { w.kAm = false; w.kDd = false }

    /** `i.p(int,int)` (i.java:18031, proven): damage-number popup —
     *  `a(24,40,31,az+10)` (ax24 `S==19` → 40), `ao/ap` offset, `P|=16,
     *  af=this`. */
    fun popupDmg(world: LevelCellSource, r8: Int, r9: Int) {
        val anim = if (ax == 24 && S == 19) 40 else 31
        val aK = spawnChildFx(world, 24, 40, anim, az + 10)
        aK.av = false
        aK.ak = ak + r8; aK.al = al + r9
        aK.ao = r8; aK.ap = r9
        aK.ag = 0; aK.ah = 0
        aK.settleToGround(world)
        aK.P = aK.P or 16
        aK.af = this
        world.queueInsert(aK)
    }

    /** `i.g(int)` (i.java:18922, proven): 4-way knife barrage — one
     *  `a(24,40,r8+16,az+1)` floatie per call: r8 0→(ah=256,ag=-1280),
     *  1→(256,0), 2→(256,+1280)+j++(j>5→j=0,cI=true), 3→homing arc at
     *  the player (`ag = ±|Δx|·(1280-k.Y)/|Δy|`). `k.A(27)` at tail. */
    fun spawnBarrage(world: LevelCellSource, r8: Int) {
        val aK = spawnChildFx(world, 24, 40, r8 + 16, az + 1)
        aK.av = false
        aK.ak = ak; aK.al = W[3]
        when (r8) {
            0 -> { aK.ah = 256; aK.ag = -1280 }
            1 -> { aK.ah = 256; aK.ag = 0 }
            2 -> {
                aK.ah = 256; aK.ag = 1280
                j++
                if (j > 5) { j = 0; cIDone = true }
            }
            3 -> {
                aK.ah = 1280
                aK.ao = (world.playerRect()[0] + world.playerRect()[2]) shr 1
                aK.ap = (world.playerRect()[1] + world.playerRect()[3]) shr 1
                val r0 = aK.ao - aK.ak
                var r9 = aK.ap - aK.al; if (r9 == 0) r9 = 1
                val r03 = (kotlin.math.abs(r0) * (1280 - world.kY)) /
                    kotlin.math.abs(r9)
                aK.ag = if (r0 >= 0) r03 else -r03
                cIDone = true
            }
        }
        aK.settleToGround(world)
        aK.P = aK.P or 16
        aK.af = this
        aK.bR = false
        world.queueInsert(aK)
        world.sfx(27)
    }

    /** `i.bH()` (i.java:19282, proven): attack-anim pick 33/34/35 by player
     *  geometry — `r02 = |((al+W[3])-W[1]) - aS.al + 44|` vs `r0=|ak-aS.ak|`
     *  (feet-gap vs x-gap): `r02>r0-5 → 33`; `r02>=r0+5 → 33`; `ak>aS.ak →
     *  34`; `ak<aS.ak → 35`; `ak==aS.ak → 33`. */
    fun pickAttackAnim(world: LevelCellSource): Int {
        val r0 = kotlin.math.abs(ak - world.player.ak)
        val r02 = kotlin.math.abs(((al + W[3]) - W[1]) - world.player.al + 44)
        if (r02 > r0 - 5) return 33
        if (r02 >= r0 + 5) return 33
        if (ak > world.player.ak) return 34
        if (ak < world.player.ak) return 35
        return 33
    }

    /** `i.bG()` (i.java:18938-19280, proven): the linked-pursuer attack
     *  script — `l|=1`; when `!cI` runs the `p`-dispatch, then the tail
     *  every-call arm (`r()` → `cJ=true`, alive/dead anim pushes):
     *
     *  - `pv==0`: windup `i(28)` → `r()` → `i(14)` + `g(0..2)` spread
     *  - `pv==1`: (tail only)
     *  - `pv==2`: `S==18→r()→(n=5; homing a(24,40,13) — arc-aim at aS with
     *    `j`-counter barrage (j==3→`ag>>2`, aG 1/2 flips, `ah=k.Y+128`);
     *    j>=4→j=0,n=0,cI=true); else `i(29)`; finishing `r()` → `i(18)`
     *  - `pv==3`: `k` → (j==2&&S27→r()→i(24); S24→wait; else i(27));
     *    else `aZ` toggle + j0 grid-scatter (60-pt pool, 7 picks avoiding
     *    `k.B.W`, `a(24,40,12)` at each, `cH` targets, n=30,j=1) / j1
     *    countdown → j2 / j2 = 7 homing knives `a(24,40,11)` → j=0,cI
     *  - `pv==4`: `k.B.l&{2,12}` → `l&=-2`; `S∈[30,32]@T==0` → spawn
     *    `a(24,40,S+11)` (0→ag0,1→-1280@W0,2→+1280@W2+j++>5→cI,3→homing)
     *    always `cI=true` + second `a(24,40,44)` + `k.A(12)`; `S∈[33,35]`
     *    → `i(S-3)`
     *
     *  tail (`!cI` skipped it — runs always): `r()` → `cJ=true`;
     *  `pv∈{0,1}`: dead→`i(16)` alive→`i(13)`; `pv∈{2,3}`:
     *  dead→`i((pv-1)*4+16)` alive→`i((pv-1)*4+13)`; `pv==4`: dead→`i(36)`
     *  alive→`i(bH())`+`aF=n,cI=false`. */
    fun respawnAttack(world: LevelCellSource) {
        l = l or 1
        if (!cIDone) {
            cJDone = false
            when (pv) {
                0 -> {
                    if (S != 28 && S != 14) setAnim(28)
                    else if (animFinished() && S == 28) {
                        setAnim(14)
                        spawnBarrage(world, 0)
                        spawnBarrage(world, 1)
                        spawnBarrage(world, 2)
                    }
                }
                2 -> {
                    if (S != 18 && S != 29) setAnim(29)
                    else if (animFinished()) {
                        if (S != 18) setAnim(18)
                        else {
                            nl = 5
                            val aK = spawnChildFx(world, 24, 40, 13, az - 1)
                            aK.av = false
                            aK.ak = ak; aK.al = W[3]
                            aK.posToWaypoint(world)
                            aK.ah = -512
                            aK.ao = (world.playerRect()[0] +
                                world.playerRect()[2]) shr 1
                            var r9 = 240 - aK.bZ
                            if (r9 < 60) r9 = 60
                            // inferred: `j.a(0, j.c(0)>>8)` — `j.m` reads as
                            // the constant 0 (decompiler fold) → j.c(0) =
                            // Int.MAX_VALUE → range (0, MAX>>8)
                            val r02 = (kotlin.math.abs(
                                world.jRand(0, Int.MAX_VALUE shr 8)) *
                                ((-512) - world.kY)) / kotlin.math.abs(r9)
                            aK.ap = aK.bZ + world.jRand(60, r9)
                            aK.ag = if ((world.jNextInt() and 1) == 0) r02
                                    else -r02
                            aK.settleToGround(world)
                            aK.P = aK.P or 16
                            aK.af = this
                            aK.bR = false
                            world.queueInsert(aK)
                            aK.j = (aK.ap shl 8) / (aK.ah - world.kY)
                            if (j == 3) {
                                val r03 = aK.ag shr 2
                                aK.k = true
                                aK.ag = r03
                                if (aG == 1 && r03 > 0) aK.ag = -r03
                                if (aG == 2 && r03 < 0) aK.ah = world.kY + 128
                            }
                            j++
                            if (j >= 4) { j = 0; nl = 0; cIDone = true }
                            else aC = 0
                            setAnim(18)
                        }
                    }
                }
                3 -> {
                    if (!k) {
                        // L57-L61 — gauge-charge engage: hold S22, arm the
                        // gauge (`i.q=true`, `i.bU=aB`), clear the `l` parity
                        // bit and RETURN (skips the L153 tail entirely).
                        if (S != 22) {
                            setAnim(22)
                            world.iQ = true
                            world.iBU = aB
                        }
                        l = l and -2
                        return
                    }
                    if (j == 2) {
                        // L64-L73 — knife-fan follow-through
                        if (S == 27 && animFinished()) setAnim(24)
                        if (S == 24) return
                        if (S != 27) setAnim(27)
                        return
                    }
                    run {
                        // L77 — aZ toggles the scatter vs boundary-fill arm
                        aZ = !aZ
                        when (j) {
                            0 -> {
                                val r04 = Array(60) { IntArray(2) }
                                for (r10 in 0 until 6) for (r92 in 0 until 10) {
                                    r04[r10 * 10 + r92][0] = r92 * 40 + 20 +
                                        world.jNextInt() % 20
                                    r04[r10 * 10 + r92][1] = r10 * 40 + 20 +
                                        world.jNextInt() % 20
                                }
                                cHGrid = Array(7) { IntArray(2) }
                                var r93 = 0
                                while (r93 < 7) {
                                    var r05 = kotlin.math.abs(
                                        world.jNextInt() % 60)
                                    while (r04[r05][0] == -1)
                                        r05 = kotlin.math.abs(
                                            world.jNextInt() % 60)
                                    val kB = world.kB
                                    if (aZ) {
                                        if (kB != null && pointInBox(
                                                r04[r05][0], r04[r05][1],
                                                kB.W)) {
                                            if ((world.jNextInt() and 1) == 0)
                                                r04[r05][1] = kB.W[1]
                                            else r04[r05][1] = kB.W[3]
                                        }
                                    } else if (kB == null || !pointInBox(
                                            r04[r05][0], r04[r05][1], kB.W)) {
                                        r04[r05][0] = world.jRand(
                                            kB?.W?.get(0) ?: 0,
                                            kB?.W?.get(2) ?: 400)
                                        r04[r05][1] = world.jRand(
                                            kB?.W?.get(1) ?: 0,
                                            kB?.W?.get(3) ?: 240)
                                    }
                                    val aK = spawnChildFx(
                                        world, 24, 40, 12, az + 1)
                                    aK.ag = 0; aK.ah = world.kY
                                    aK.av = false
                                    aK.bY = r04[r05][0] + world.kO
                                    aK.bZ = r04[r05][1]
                                    cHGrid!![r93][0] = r04[r05][0] + world.kO
                                    cHGrid!![r93][1] = r04[r05][1]
                                    aK.posFromWaypoint(world)
                                    aK.aC = 50
                                    aK.P = aK.P or 16
                                    aK.af = this
                                    world.queueInsert(aK)
                                    r04[r05][0] = -1
                                    r93++
                                }
                                nl = 30; j = 1; aC = 0
                            }
                            1 -> {
                                nl--
                                if (nl < 0) { j = 2; nl = 0 }
                                aC = 0
                            }
                            2 -> {
                                for (r94 in 0 until 7) {
                                    val aK = spawnChildFx(
                                        world, 24, 40, 11, az + 1)
                                    aK.av = false
                                    aK.ak = ak; aK.al = W[1]
                                    aK.posToWaypoint(world)
                                    aK.aC = 20; aK.cz = 20; aK.cA = 0
                                    aK.ap = r94
                                    val r06 = (cHGrid?.get(r94)?.get(0) ?: 0) -
                                        aK.bY
                                    val r07 = (cHGrid?.get(r94)?.get(1) ?: 0) -
                                        aK.bZ
                                    aK.ag = (r06 shl 8) / 20
                                    aK.ah = ((r07 shl 8) / 20) + world.kY
                                    aK.settleToGround(world)
                                    aK.P = aK.P or 16
                                    aK.af = this
                                    aK.bR = false
                                    world.queueInsert(aK)
                                }
                                j = 0; cIDone = true
                            }
                        }
                    }
                }
                4 -> {
                    val kBl = world.kB?.l ?: 0
                    if ((kBl and 2) != 0 || (kBl and 12) != 0)
                        l = l and -2
                    if (S in 30..32 && T == 0) {
                        val r12 = S - 30
                        val aK = spawnChildFx(world, 24, 40, r12 + 41, az + 1)
                        aK.av = false
                        aK.ak = ak; aK.al = W[3]
                        when (r12) {
                            0 -> { aK.ah = 256; aK.ag = 0 }
                            1 -> { aK.ah = 256; aK.ag = -1280; aK.ak = W[0] }
                            2 -> {
                                aK.ah = 256; aK.ag = 1280; aK.ak = W[2]
                                j++
                                if (j > 5) { j = 0; cIDone = true }
                            }
                            3 -> {
                                aK.ah = 1280
                                aK.ao = (world.playerRect()[0] +
                                    world.playerRect()[2]) shr 1
                                aK.ap = (world.playerRect()[1] +
                                    world.playerRect()[3]) shr 1
                                val r08 = aK.ao - aK.ak
                                var r11 = aK.ap - aK.al
                                if (r11 == 0) r11 = 1
                                val r010 = (kotlin.math.abs(r08) *
                                    (1280 - world.kY)) /
                                    kotlin.math.abs(r11)
                                aK.ag = if (r08 >= 0) r010 else -r010
                                cIDone = true
                            }
                        }
                        cIDone = true
                        aK.settleToGround(world)
                        aK.P = aK.P or 16
                        aK.af = this
                        aK.bR = false
                        world.queueInsert(aK)
                        val keep = aK.ak
                        val aK2 = spawnChildFx(world, 24, 40, 44, az + 1)
                        aK2.ak = keep; aK2.al = W[3]
                        aK2.P = aK2.P or 16
                        aK2.af = this
                        aK2.bR = false
                        world.queueInsert(aK2)
                        world.sfx(12)
                    }
                    if (S in 33..35) setAnim(30 + S - 33)
                }
            }
        }
        // L153 tail — runs every call
        when (pv) {
            0, 1 -> if (animFinished()) {
                cJDone = true
                setAnim(if (aB > 0) 13 else 16)
            }
            2, 3 -> if (animFinished()) {
                cJDone = true
                val r012 = (pv - 1) shl 2
                setAnim(if (aB > 0) r012 + 13 else r012 + 16)
            }
            4 -> if (animFinished()) {
                cJDone = true
                if (aB > 0) {
                    setAnim(pickAttackAnim(world))
                    aF = nl
                    cIDone = false
                } else setAnim(36)
            }
        }
    }
}

/** Minimal cell-source interface so `e()`/probes work against the level. */
interface LevelCellSource {
    val cellPx: Int
    fun collisionCell(cx: Int, cy: Int): Int
    /** `k.bp`/`k.bq` (k.java:99-100, proven) — grid dims in 20px cells
     *  (the `e()` OOB check's upper bounds). */
    val kBp: Int get() = Int.MAX_VALUE
    val kBq: Int get() = Int.MAX_VALUE
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
    val kAp: IntArray
    val sfxLog: List<Int>
    fun sfx(id: Int)

    // -- k.bG/k.bH music slot + k.x() + k.bA[32] + k.a(z2) (S147 arm) --
    /** `k.bG` (k.java:310) — one-shot music slot (-1 = nothing queued). */
    var kBg: Int get() = -1; set(_) {}
    /** `k.bH` (k.java:311) — saved music slot the S147 arm restores. */
    var kBH: Int get() = -1; set(_) {}
    /** `k.bA` (k.java:291) — the 512-byte save/ACRS record array. */
    val kBA: IntArray get() = IntArray(0)
    /** `k.x()` (k.java:5711) → `e.a()` (e.java:32, proven): an audio
     *  track is actively playing (orig: index set AND inside its
     *  `h.a[e]` duration window — our port has no real-time expiry,
     *  so a queued track counts as playing; `inferred` mapping). */
    fun musicActive(): Boolean = false
    /** `k.a(z2)` (k.java:5139, proven) — level (re)load: audio stop,
     *  V(), `d(z2)`, g-link clears. `a(true)` = restart-ish reset. */
    fun resetLevel(full: Boolean) {}

    // -- i.a() big-op plumbing (k.b/k.n(int)/k.v=k.w/pointer/spawn) -----
    /** `k.b(idx,str,flag)` (k.java:430): queue the op105 dialog —
     *  `bO=flag`, `bN[0]=idx>0?idx:-1`, then `b(9,1+aj,str,str)`;
     *  `inferred` return = accepted. */
    fun kDialog(idx: Int, strRef: Int, flag: Int): Boolean = false
    /** `i.c(int)` (i.java:7724, proven): the u10 tutorial-hint request —
     *  level-0 only (`k.aj!=0` → skip), one-shot per `br[r6]`; shows
     *  `k.b(10,1,A[r6],A[r6])` (A={30,31,32}) and `k.l(21)`. */
    fun tutorialHint(r6: Int) {}
    var bO: Int get() = 0; set(_) {}
    var bN0: Int get() = 0; set(_) {}
    /** `k.n(int)` (k.java:2869): `cO/cP` screen-transition statics —
     *  `v>0 → cO=v,cP=true; else cO=-v,cP=false`. */
    fun kNSet(v: Int) { kCO = if (v > 0) v else -v; kCP = v > 0 }
    var kCO: Int get() = 0; set(_) {}
    var kCP: Boolean get() = false; set(_) {}
    /** `k.v = k.w` (i.java:20095) — re-arm the edge latch from held. */
    fun padRearm() {}
    /** `k.c(x,y,w,h)` (k.java:575): press-point in rect (`k.H/k.I`). */
    fun pointerDownIn(x: Int, y: Int, w: Int, h: Int): Boolean = false
    /** `k.d(x,y,w,h)` (k.java:599): move-point in rect (`k.J/k.K`). */
    fun pointerMoveIn(x: Int, y: Int, w: Int, h: Int): Boolean = false
    /** `k.j()` (k.java:579): press inside the bottom UI strip —
     *  `H∈[36,364] && I∈(204,240)` inside, else `I∈[0,204]` outside. */
    fun pointerStrip(): Boolean = false
    /** `i.a(8,59,S,facing,x,y,az)` (i.java:6898, proven): spawn the ax8
     *  param projectile (`aw=-1`, `P|=512`, clip `k.r(59)`). */
    fun spawnParam(s: Int, facing: Boolean, x: Int, y: Int, az: Int): Entity? = null

    /** `m(-1)` wisp burst (i.java:21259) spawned through `a(74,54,1,…)`. */
    fun spawnWisp(src: Entity)

    // -- globals read by prop FSMs --------------------------------------
    /** `k.Y` — global fall impulse (k.java:2336/2742 `Y = X << 8`,
     *  proven): derived from `kX` so the wind write `k.X = …` re-prices
     *  every ballistic bias like the original's `Y = X << 8` reload. */
    val kY: Int get() = kX shl 8
    /** `k.O` — camera left edge in world px (subtract operand at
     *  i.java:9837; ax14 pickups pin to `k.O+{20,380}` = the view edges;
     *  writable — `aa()`'s op11/12 camera lerp advances it). */
    var kO: Int get() = 0; set(_) {}
    /** `k.P` — camera top edge in world px (u() center operand; writable —
     *  `aa()` op11/12 lerp). */
    var kP: Int get() = 0; set(_) {}
    /** `k.ac` — camera view rect [x1,y1,x2,y2] world px (v()'s L83/L85). */
    val camRect: IntArray get() = IntArray(4)
    /** `k.af`/`k.ag` — camera focus-point offsets written by ax10 S0 zones
     *  (i.java:9772 L5ac) and consumed by the `k.m()` tracker
     *  (k.java:1959-1965: camA = ak-200+af, camB = al-120+ag). */
    var camAf: Int get() = 0; set(_) {}
    var camAg: Int get() = 0; set(_) {}
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

    /** `k.aS` — the player entity (i.C()/i.g()/i.Q() read aS.S/ak/al/av/X). */
    val player: Entity
    /** `k.au` — current weapon slot, indexes `i.bu[]` = {300,400,500}. */
    val weaponSlot: Int get() = 0
    /** `i.a(_,5,14,av,x,y,300)` (i.java:6898, proven): the ax8 knife
     *  projectile — clip5 anim14 az300, `P|=512`, `aw=-1, au=0`, `k.b`. */
    fun spawnProjectile(av: Boolean, x: Int, y: Int): Entity

    // -- equip/context statics (k.ar/as/at/C/ae + g.a/i/E) --------------------
    /** `k.ar[5]` — equip list built from `player.gJ` bits by `k.q()`
     *  (bit-4 skipped); entries are the J masks {1,2,8,16}, -1 padded. */
    val equipList: IntArray get() = IntArray(5) { -1 }
    /** `k.as` — count of live `equipList` entries. */
    var equipCount: Int
    /** `k.at` — one-shot cycle/action lock (`g.h()` sets it, `ao()` gates). */
    var actionLock: Int
    /** `k.C` — the context entity (scene/dialog owner); unspawned → null. */
    var cEntity: Entity?
    /** `k.ae` — the entity currently holding/pinning the player.
     *  Unified on `kAe` (LevelCellSource member) — alias kept for
     *  source-call sites (`aeRef` and `kAe` were the same `k.ae`). */
    var aeRef: Entity? get() = kAe; set(v) { kAe = v }
    /** `g.a` — the player's vehicle/mount link (ax51/43/60/66 vehicles). */
    var vehicle: Entity?
    /** `g.i` — static control flag cleared by the L204 ledge-drop arm. */
    var iFlag: Boolean
    /** `g.E` — static flag; `ap()` at L2057 requires it false. */
    var eFlag: Boolean
    /** `k.q()` (k.java:~4600, proven): rebuild `equipList`/`equipCount` from
     *  `player.gJ` bits {1,2,8,16} — mask 4 is skipped; first empty slot
     *  fills in ascending bit order. */
    fun rebuildEquip() {}
    /** `k.c(x,y,w,h)` — view-space touch-rect hit test (`ao()`'s 355,197
     *  weapon-cycle button); default false when no touch UI is ported. */
    fun touchRect(x: Int, y: Int, w: Int, h: Int): Boolean = false

    /** `i.cv` — static rail-zone register (i.java:157): the ax10-S51
     *  trigger currently overlapping `k.ac`; read by bw()'s cv arm. */
    var cv: Entity?
    // -- marker/sweep globals (bb() L21 tail) ---------------------------------
    /** `i.cF` — i-STATIC gauge-full flag (i.java:184/18543; set by the
     *  charge-fill arm `aB+=5 → bU` ported at NpcFsm (iQ/iBU/cFFlag)). ax32 armed
     *  walls (`S∈[21,27]`) only break when this is true. */
    var cFFlag: Boolean
    /** `g.b` — the marker-engage link on the player (op38 writes it). */
    var playerLinkB: Entity?
    /** `k.aj` — current mission index; gates `k.e(0,aw)` and `bh[k.aj]`. */
    val missionIndex: Int get() = 0
    /** `k.bh[k.aj]` — per-mission behavior flag (`MISSION_BH` table). */
    fun missionBh(): Int = Entity.MISSION_BH[missionIndex]
    /** `k.e(0,aw)` (k.java:4314, proven): `ap[0]++` kill/stat tally —
     *  gated `aw > 0` and `k.aj != 7`. */
    fun statTally(aw: Int) {}
    /** `g.b()` (g.java:346, proven): player mid-attack — `I∈{1,2} && S` in
     *  the attack set {67,68,69,81,112-115,183,184,216,217,286,287}. */
    fun playerAttacking(): Boolean =
        (player.gI == 1 || player.gI == 2) &&
            player.S in intArrayOf(67, 68, 69, 81, 112, 113, 114, 115,
                                   183, 184, 216, 217, 286, 287)

    // -- g.d() player damage intake (g.java:3885) -----------------------------
    /** `i.bh` STATIC (i.java:104) — global 8-tick hit-lock set by `d()`;
     *  decremented per tick in the `g.e()` tail (g.java:572). */
    var iBh: Int
    /** `i.bJ`/`i.bH`/`i.bI`/`i.bL` (i.java:195-197 + :113, proven) —
     *  the b(z2)-tail flicker latch pair + vestigial counter. `bJ` toggles
     *  between `bH` (=1) and `bI` (=2) states each frame while >0; `bL`
     *  is a verbatim no-op (k.javap.txt:16545-16548 — `getstatic; dup;
     *  putstatic` with no arithmetic). Producer arm unported. */
    var iBJ: Int get() = 0; set(_) {}
    var iBH: Int get() = 1; set(_) {}
    var iBI: Int get() = 2; set(_) {}
    var iBL: Int get() = 0; set(_) {}
    /** `g.s` — godmode flag (g.java:24); default false. */
    val godMode: Boolean get() = false
    /** `g.h()` (g.java:3946, proven): invulnerable — `s` godmode or `t`
     *  iframe timer nonzero. */
    fun playerInvulnerable(): Boolean = godMode || player.gt != 0
    /** `g.g()` (g.java:3939, proven): player dead — `x[1] <= 0`. */
    fun playerDead(): Boolean = player.x1 <= 0
    /** `k.ax` (k.java:159, proven): restore byte for the meter —
     *  `g.e(k.ax)` writes `x[1]=ax` (ax19 pickups, i.java:2740). */
    var kAx: Int get() = 90; set(_) {}

    // -- mission-director statics (bD/d/bG, i.java:72-184 + k fields) --------
    /** `i.bV` — kill-bitmap routing value (level script writes; 0 = pure
     *  waypoint chase, 1 = single-pursuit `l|2`, 2 = dual `l|62&-17`). */
    var iBV: Int
    /** `i.bU` — charge-gauge cap written by the bG p3 `!k` arm. */
    var iBU: Int
    /** `i.bW`/`i.bX` — pending phase transition (aA=6 router arms). */
    var iBW: Boolean
    var iBX: Int
    /** `i.bT` — director armed flag (aA=0 tail / aA=7 clears). */
    var iBT: Boolean
    /** `i.bj` — finale freeze flag (aA=7). */
    var iBj: Boolean
    /** `i.be` — D() camera X-lock: when set the autoscroll keeps `cA` (the
     *  `cN`-relative target write is skipped, k.java:2797). */
    var iBe: Boolean get() = false; set(_) {}
    /** `i.q` — gauge-charge mode: bD L326 mirrors `aB` vs sums it. */
    var iQ: Boolean
    /** `i.cC` — waypoint-phase cursor (0-6). */
    var iCC: Int
    /** `i.cD` — active waypoint-phase (-1 init). */
    var iCD: Int
    /** `i.cE` — `f(int)` one-shot transition gate (-1 init). */
    var iCE: Int
    /** `c.m`/`c.l`/`c.j` — the 400-slot mission-waypoint node pool
     *  (c.java, proven). `find(id)` = `c.a(int)`. */
    val waypoints: WaypointPool
    /** `cB` — the director's current waypoint node (i-static `cB`). */
    var dirWp: WaypointNode?
    /** `k.B` — arena-boundary entity (bG scatter/boundary reads `k.B.W/l`). */
    var kB: Entity?
    /** `k.ai` — director-active flag (aA=0 sets, bD reads). */
    var kAi: Boolean
    /** `k.R` — director int register (-1 at aA=0). */
    var kR: Int
    /** `k.aE`/`k.aH` — director timers (aE>0 → `aH=80` at arming). */
    var kAE: Int
    var kAF: Int get() = 0; set(_) {}
    var kAH: Int
    /** `k.aB`/`k.aC` — c(z2) center banner text + TTL (k.java:4327);
     *  ax9-S4 is the live producer (`aC=-1` while touching). */
    var kAB: String? get() = null; set(_) {}
    var kAC: Int get() = 0; set(_) {}
    /** `k.aZ` (k.java:252 `boolean`, proven) — persisted flag byte 68
     *  (`j.a(k.bA,68,aZ?1:0)` save at i.java:13494/17286; `aZ = bA[68]!=0`
     *  restore at k.java:5197). Written by the ax10 S6/S7 zone arms. */
    var kAZ: Boolean get() = false; set(_) {}
    /** `k.b(int,int,int,int)` (k.java:350, proven) — checkpoint-map
     *  marker: `u=slot` (8 = checkpoint kind), expands `d(level,row)`
     *  entries into the `w` region; `i3==-1 → false` (no start row). */
    fun kBMark(slot: Int, level: Int, row: Int, span: Int): Boolean = false
    /** `k.bQ` (k.java:140 `private static boolean`, proven) — the
     *  checkpoint-map "region dirty" flag `k.b()` sets; consumed by the
     *  map screen (unported there — flag tracked for parity). */
    var kBQ: Boolean get() = false; set(_) {}
    /** `e.b()` (e.java:87, proven) — stop the current audio track.
     *  (`k.w()` 0-arg, k.java:5707.) */
    fun audioStop() {}
    /** `e.a(n,false)` (e.java:50-87, proven) — start audio track n
     *  (`k.z(n)`/`k.A(n)`, k.java:5696-5705; `A` delegates to `z`). */
    fun audioTrackPlay(n: Int) {}
    /** `k.aR` — chase-progress row (pre-switch `aS.al<260` arm). */
    var kAR: Int
    /** `k.bu` — level pixel height used by the `aR` row formula. */
    val kBu: Int get() = 0
    /** `k.bk[]` — per-record clip table read by `u()`'s ax67 arms;
     *  wired to `NpcFsm.decorClip` (Level0World:964). */
    fun kBk(i: Int): Int = NpcFsm.decorClip(i)
    /** `j.a(lo,hi)` (j.java:322, proven): `lo + |nextInt| % (hi-lo)`;
     *  Level0World wires `rng.nextInt` (Java-LCG). */
    fun jRand(lo: Int, hi: Int): Int = lo
    /** `j.j.nextInt()` — raw RNG (bG scatter/homing arms);
     *  Level0World:965 wires `rng.nextInt()`. */
    fun jNextInt(): Int = 0
    /** `k.b(e)` — deferred entity insert (`pendingInsert` in the world). */
    fun queueInsert(e: Entity) {}
    /** `k.l(15)` — mission-complete screen-state → `stateL(15)`
     *  (Level0World:935). */
    fun missionComplete() {}

    // -- ax29 boss FSM (i.aP) statics ---------------------------------
    /** `k.aU` — the active boss entity (aP() re-pins it every tick). */
    var kAU: Entity? get() = null; set(_) {}
    /** `k.E` — held/struggle-UI entity ref; `P|=128` arms at
     *  counter/stagger moments; producer = the ax71 overlay spawn
     *  (Level0World:915). */
    var kE: Entity? get() = null; set(_) {}
    /** `k.D` (i.java:2750, proven): the ax34 player-follower overlay
     *  spawned by the L43 player-init arm; `k.V()`/`D()` null it. */
    var kD: Entity? get() = null; set(_) {}
    /** `ad()` draw channel — the active speech-bubble descriptor this
     *  frame (null when no bubble is presenting). */
    var bubbleDraw: BubbleDraw? get() = null; set(_) {}
    /** `k.a(k.y, text, widthPx)` — dialog text wrap; returns the line
     *  table whose [0] is the wrapped line count (renderer-metric
     *  dependent — `inferred` approximation by char width). */
    fun wrapDialogText(text: String, widthPx: Int): IntArray = intArrayOf(1)
    /** `k.y.k(lines)` — pixel height of `lines` dialog lines
     *  (font metric; `inferred` fixed line height). */
    fun dialogAdvance(lines: Int): Int = lines * 10
    /** `k.C` — HUD-claimed entity (`i.N()`). */
    var kC: Entity? get() = null; set(_) {}
    /** `i.by` — boss phase tier static (0/1/3; 2 = dormant tick). */
    var iBy: Int get() = 0; set(_) {}
    /** `i.ci[5]` — boss cooldown counters (null-init arm inside aP). */
    var iCi: IntArray? get() = null; set(_) {}
    /** `i.cj` — grab-QTE counter-armed flag. */
    var iCj: Boolean get() = false; set(_) {}
    /** `i.ck` — boss aura entity (`e(int)`-managed). */
    var iCk: Entity? get() = null; set(_) {}
    /** `i.cl` — ax61 idle-add entity. */
    var iCl: Entity? get() = null; set(_) {}
    /** `i.cm` — by1 finisher-marker-spawned flag. */
    var iCm: Boolean get() = false; set(_) {}
    /** `i.cn` — counter-chance ramp (S20@T0 +100 cap 800; S21 clears). */
    var iCn: Int get() = 0; set(_) {}
    /** `i.co` — saved-state resume int (stagger stores S). */
    var iCo: Int get() = 0; set(_) {}
    /** `i.cp` — by3 exhaust counter. */
    var iCp: Int get() = 0; set(_) {}
    /** `i.aH`/`i.aI`/`i.aJ` — the slow-mo driver flags (`i.b(int)` /
     *  `i.O()`, i.java:21728/21749 proven); `k.bh[k.aj]==3` =
     *  `Entity.MISSION_BH[kAj]==3` (missions 1/4). */
    var iAH: Boolean get() = false; set(_) {}
    var iAI: Int get() = 0; set(_) {}
    var iAJ: Int get() = 0; set(_) {}
    /** `i.bk` (i.java:158, proven) — wisp-burst latch: `n()`'s S18 arm
     *  sets it on `i(20)`; the S20 case gates the `5×e(true)` burst and
     *  clears it on `r()`; the S0 arm's `e(false)` flap requires `!bk`. */
    var iBk: Boolean get() = false; set(_) {}
    /** `i.aK` — pooled flap-puff child spawned by `g.e(boolean)` (the
     *  `a(24,40,6|7,az-1)` wisp the player leaves while flapping). */
    var iAK: Entity? get() = null; set(_) {}
    /** `k.aI` (k.java:234, proven) — `n()` flap cooldown: `++` per tick,
     *  the `e(false)` auto-flap requires `k.aI >= 10` and resets it. */
    var kAI: Int get() = 0; set(_) {}
    /** `k.aG` — `k.aE` decay divider (n() head: `aH<0 → aG--`;
     *  `aG<=0 → aG=6; aE--`). */
    var kAG: Int get() = 0; set(_) {}
    /** `k.bB`/`k.bC`/`k.bD` (k.java:119-123, proven) — burst-phase ints:
     *  `bB==0 && bC==0` gates the glide-recovery `i(4)`; `bD>=15` picks
     *  the heavy bank anims (30/31) over the light ones (33/32). */
    var kBB: Int get() = 0; set(_) {}
    var kBC: Int get() = 0; set(_) {}
    var kBD: Int get() = 0; set(_) {}
    /** `k.bh[k.aj]==3` — flying level; gates the `g.n()` flight arms. */
    val bh3: Boolean get() = false
    /** `k.ee[]` — per-mission BGM table (`B()` plays `ee[aj]`). */
    val kEE: IntArray get() = IntArray(0)
    /** `k.Q` — flying camera lookahead flag (C() sets 230 on bh3); the
     *  glide arm uses ≥230 = auto-descend, >117/<230 = climb/dive gates. */
    var kQ: Int get() = 0; set(_) {}
    /** `k.l(i)` — screen-state driver (`l(12)` = mission fail). */
    fun stateL(i: Int) {}
    /** `k.bJ` — boss grab-QTE lose latch (armed 6 on the fail path). */
    var kBj: Int get() = 0; set(_) {}
    /** `k.X`/`k.W`/`k.V`/`k.aw` — time-scale statics touched by
     *  b(int)/O(). `k.V` is the camera-watch x (k.java:49 `-7`). */
    var kX: Int get() = 0; set(_) {}
    var kW: Int get() = 0; set(_) {}
    var kV: Int get() = 0; set(_) {}
    var kAw: Int get() = 0; set(_) {}
    /** `k.ae` (k.java:97) — the player's current link entity: the bI ax5
     *  arm rebinds it to `aS` (or `g.a` when that entity's ax==43); `ai()`
     *  reads it (ax==10 && S==52). */
    var kAe: Entity? get() = null; set(_) {}
    /** `k.ah` — entity ref cleared by `k.n()` (k.java:2861). */
    var kAh: Entity? get() = null; set(_) {}
    /** `k.Z`/`k.aa`/`k.ab` (k.java:89-91) — claim-script static flags
     *  cleared by `bI()`; `k.Z` also gates the bh==3 `al-=k.X` shift. */
    var kZ: Boolean get() = false; set(_) {}
    var kAa: Boolean get() = false; set(_) {}
    var kAb: Boolean get() = false; set(_) {}
    /** `k.aj` — level/mission index into `MISSION_BH` (bh[aj]==3 →
     *  missions 1/4 — the k.X time-scale missions). */
    var kAj: Int get() = 0; set(_) {}
    /** `k.T`/`k.U` — camera bounds (boundMinY/boundMaxY aliases). */
    var kT: Int get() = 0; set(_) {}
    var kU: Int get() = 0; set(_) {}
    /** `k.n()` (k.java:2861, proven): `ah=null; R=S=T=U=0` — drops the
     *  link entity and clears all four camera bounds. */
    fun kN() {}
    /** `k.l(int)` — screen transition: 12 mission-fail, 15 mission-
     *  complete (ported as world flags). */
    fun screenL(n: Int) {}
    /** `g.g()` (g.java:3939, proven): player dead — `x[1] <= 0`. */
    fun gG(): Boolean = false
    /** `k.s(int)` (k.java:7149, proven): index of uid `x` inside `k.eH[]`
     *  (the script-handle table) or -1. */
    fun kSIndex(x: Int): Int = -1
    /** `k.eH` — the script-handle uid table (script-loaded; empty until
     *  the table port lands). */
    val kEh: IntArray get() = IntArray(0)
    /** `k.by` — claim-script op blocks per script index. */
    val kBy: Array<Array<ByteArray>> get() = emptyArray()
    /** `k.bz` — first-group byte offset per block (`i.cL` seeds). */
    val kBz: Array<IntArray> get() = emptyArray()
    /** `k.t(int)` (k.java:7162, proven): `eI[op-100]` payload length. */
    fun kT(op: Int): Int = ScriptTables.EI[op - 100]
    /** `k.bz[ca]` — the claim-script op table (`Level0World` supplies the
     *  real table; `null` = no ops for this block). */
    fun claimOps(ca: Int): IntArray? = null

    // -- claim-script VM (`i.aa()` i.java:19429) world surface ------------
    /** `k.br`/`k.bs` — world pixel bounds (op11/12 camera clamps). */
    val kBr: Int get() = 0
    val kBs: Int get() = 0
    /** `j.g` — global tick counter (`j.g % aI` slow-mo step gate). */
    val jG: Long get() = 0
    /** `k.bb[]`/`k.bc` — the live entity array + count (follower scan). */
    val kBb: List<Entity?> get() = emptyList()
    val kBc: Int get() = 0
    /** `k.ad` — camera-return claim mask fed to `k.m` (k.java:2346). */
    var kAd: Int get() = 0; set(_) {}
    /** `k.aV` — vehicle/mount entity singleton (Z[0] arms, `ad` ax43 link). */
    var kAV: Entity? get() = null; set(_) {}
    /** `k.aQ` — script-owned entity ref (sub-op 17 clears). */
    var kAQ: Entity? get() = null; set(_) {}
    /** `k.av` — script flag (sub-op 18 sets). */
    var kAv: Boolean get() = false; set(_) {}
    /** `k.aT` — follow-target kind flag (sub-op 8 writes by ax). */
    var kAT: Boolean get() = false; set(_) {}
    /** `k.aL` — int written when `k.F` exists (sub-op 11). */
    var kAL: Int get() = 0; set(_) {}
    /** `k.bK` — gate for the sub-op-15 marker spawn. */
    var kBK: Boolean get() = false; set(_) {}
    /** `k.aJ`/`k.aK`/`k.aM` — the ax42 fuse phase machine + countdown
     *  init + accumulator (`bz()` i.java:17428). */
    var kAJ: Int get() = 0; set(_) {}
    var kAK: Int get() = 0; set(_) {}
    var kAM: Int get() = 0; set(_) {}
    /** `k.bx`/`k.bw` — script fail-channel ints (sub-ops 2/5 write -1). */
    var kBx: Int get() = 0; set(_) {}
    var kBw: Int get() = 0; set(_) {}
    /** `k.F` — claim-locked entity reference (sub-ops 10/11 gate). */
    var kF: Entity? get() = null; set(_) {}
    /** `i.bn` — level alert flag loaded from checkpoint slot bA[79]
     *  (k.java:6696-6702): gates the ax17/50 `l()` notice arms — while set,
     *  civilians/pouncers never panic (i.java:2385/2396). */
    var iBn: Boolean get() = false; set(_) {}
    /** `k.aY` (k.java:211 + :8425 `new i[3]`, proven) — the 3-slot
     *  projectile/quiver entity pool (`b()`'s `bn` arm fires `aY[0].Z[4]`
     *  via `aS.b(...)`). Pool unported → always null → the arm is inert. */
    fun kAyAt(i: Int): Entity? = null
    /** `i.ce`/`i.bD`/`i.bQ`/`i.cO`/`i.cg`/`i.ch`/`i.z` — `i` statics the
     *  arg-ops write (i.java:153-204). */
    var iCe: Boolean get() = false; set(_) {}
    var iBD: Boolean get() = false; set(_) {}
    var iBB: Boolean get() = false; set(_) {}
    var iBC: Boolean get() = false; set(_) {}
    var iBE: Int get() = 0; set(_) {}
    var iBF: Int get() = 0; set(_) {}
    var iBG: Int get() = -1; set(_) {}
    var iCF: Boolean get() = false; set(_) {}   // i.cF — hit-confirm latch
    var iBQ: Int get() = 0; set(_) {}
    var iCO: Entity? get() = null; set(_) {}
    var iCg: Entity? get() = null; set(_) {}
    var iCh: Entity? get() = null; set(_) {}
    var iZ: Boolean get() = false; set(_) {}
    /** `k.e(int,int)`/`k.o(int)` (k.java:4314/4304, proven) — `ap[n]++`
     *  stat counters; `k.o(3)` and `k.e(0,uid)` skip the increment when
     *  `k.aj==7`. Callers pass the counter index. */
    fun kStat(n: Int) {}
    /** `k.e(0,gate)` (k.java:4314, proven): `ap[0]++` iff `gate>0 &&
     *  k.aj!=7` — the kill-stat arm inside op22's ax11/139 branch. */
    fun kStatE(gate: Int) {}
    /** `k.m(int)` (k.java:2346) — camera return-to-player driver, mask-
     *  gated by `k.ad`; internals are the unported camera system —
     *  `inferred` stub. */
    fun kM(mask: Int) {}
    /** `i.b(int)`/`i.O()` — slow-mo arm/disarm; already ported as
     *  `eventArm`/`eventDisarm` on Entity (no interface entry needed). */
    /** `i.a(ax,S,...)` (i.java:6898, proven spawn form) — static spawn
     *  returning the new entity (`aK` in callers). `a(9,47,5,400)` is the
     *  sub-op-15 marker arm — `(ax,S,x?,y?)` arg mapping `inferred`. */
    fun spawnStatic(ax: Int, s: Int, x: Int, y: Int): Entity? = null
    /** `k.am`/`k.dd` — `k.o()`/`k.p()` input-lock flags (k.java:3429). */
    var kAm: Boolean get() = false; set(_) {}
    var kDd: Boolean get() = false; set(_) {}
    /** `i.f(i)` (i.java:5382, proven) — the scroll-wall clamp called at
     *  the tail of the player's motion arms (16 call sites, all on g):
     *  while the ax37 holder is in overlap mode it pins the entity's Y
     *  box inside the holder's bound rect. */
    fun scrollWallClamp(e: Entity) {}
    /** `g.r` — grab-QTE lock flag on the player (g.java:23). */
    var gR: Boolean get() = false; set(_) {}
    /** `g.u[]` (g.java:6400) — per-weapon damage TO the player; index k.au. */
    val GU: IntArray get() = intArrayOf(5, 10, 15)
    /** `k.J`/`k.K` — held touch point (screen px) for `V()`/ax61 QTE. */
    var kJ: Int get() = 0; set(_) {}
    var kK: Int get() = 0; set(_) {}
    /** `k.ac[4]` — camera/arena rect ints (aP S5 arena clamp; also the
     *  `k.ac` containment rect the ax35 off-screen kill uses). */
    val kAc: IntArray? get() = null
    /** `k.an` (k.java, proven): fade-out flag set by `k.B()` — the ax35
     *  sweep skips the player-hit arm while a fade runs. */
    var kAn: Boolean get() = false; set(_) {}
    /** `k.ao` (k.java, proven): fade-IN-side flag set by `k.C()`; the
     *  ax10-S16 door-teleport arrival arm watches `ao && bI > 13`. */
    var kAo: Boolean get() = false; set(_) {}
    /** `k.bI` (k.java:313, proven): shared fade progress 0..255 ramped
     *  by `k.fk` per `aa()` step — read by the door arms at `> 13`. */
    var kBI: Int get() = 0; set(_) {}
    /** `k.B(26)` (k.java:5738): arm the fade-IN ramp (`an`, `bI=0`). */
    fun fadeIn() {}
    /** `k.C(26)` (k.java:5745): arm the fade-OUT ramp (`ao`, `bI=255`). */
    fun fadeOut() {}
    /** `k.ah?.I()` (i.java:14444, proven): tick the scroll-wall holder
     *  entity — ported as the ax37 bounds refresh (inferred mapping). */
    fun refreshScrollBounds() {}
    /** `k.aQ` — the debug vol-paint surface the ax35 `a(x,y,w,h,bool)`
     *  rasterizer fills (i.java:22224). Debug-only: its sole reader is
     *  the HUD blit at (198-w,5); ported as a rect recorder (`inferred`). */
    var volPaintRect: IntArray? get() = null; set(_) {}
    /** `k.v(mask)` (k.java:7210, proven): edge-input `(bB & mask) != 0`. */
    fun padHeld(mask: Int): Boolean = false
    /** `k.u(mask)` (k.java:7203, proven): held-input `(bC & mask) != 0`
     *  — distinct from `padHeld`/`k.v` which reads the edge set `bB`. */
    fun padDown(mask: Int): Boolean = false
    /** `k.u` raw HELD word `bC` (k.java:119, proven) — `padDown(mask)`
     *  answers masked tests; `k.u == <bits>` comparisons (i.java:7017
     *  `k.u == 8`) need the raw value. */
    fun padHeldWord(): Int = 0
    /** `j.c` (j.java static, proven) — the screen state (21 = dialog).
     *  Read-only interface view; `Level0World` owns the var. */
    val jC: Int get() = 0
    /** `k.x(mask)` (k.java:7224, proven): double-tap-window edge (`eM`). */
    fun padTap(mask: Int): Boolean = false
    /** `k.w(mask)` (k.java:7217, proven): released-input `(eM & mask) != 0`
     *  — `eN` latches the held bits at pointer-release. */
    fun padRelease(mask: Int): Boolean = false
    /** `k.v()` (k.java:7260, proven): full input-latch reset. */
    fun clearLatches() {}

    // -- slice 65: ax64 `bl()` hooks ------------------------------------
    /** `g.s` — in-cutscene flag (g.java static; no producer ported —
     *  always false during gameplay; gates the ax64 S1 grab check at
     *  i.java:15773). */
    val gS: Boolean get() = false
    // -- slice 66: ax74 `bN()` hooks ------------------------------------
    /** `k.o(int)` (k.java:4304, proven): `ap[r5]++` progress slot; `r5==3`
     *  is gated on `k.aj == 7` in the original. */
    fun kCount(slot: Int) {}
    /** `k.e(int,int)` (k.java:12222, proven): `ap[0]++` when `r6>0`
     *  and `k.aj != 7` — the kill credit; `r5` is unused verbatim. */
    fun countKill(uid: Int) {}
    /** `k.az` — collect-streak counter consumed by `k.s()` (k.java:5338,
     *  proven); also the medal-band driver `ax = 30 + tier*15` → `g.f`/`g.e`
     *  refill — see `kCollectStreak`. */
    var kAz: Int get() = 0; set(_) {}
    /** `k.aq` — level wisp/anim-0 record counter (`k.aq++` at
     *  i.java:3033). */
    var kAq: Int get() = 0; set(_) {}
    /** `k.s()` (k.java:5338, proven): `az++` streak → meter floor
     *  `30 + tier·15` via `dE = {0,100,200,400,600,800}` — `g.f(ax)` caps
     *  `x[1]` at the tier then `g.e(ax)` raises it when the floor grew. */
    fun kCollectStreak() {}

    /** `i.bi` — ax64 grab-hitlag flag (set by the S2 hold arm,
     *  i.java:15686). */
    var iBi: Boolean get() = false; set(_) {}
    /** `k.aX[]` pooled ax24 shot slots (i.java:2837 `k.aW=50`, seeded by
     *  an ax24-S0 record's constructor arm and `k.b`-inserted into the
     *  entity list — they tick through the normal ax24 `ba()` FSM, there
     *  is no separate pool step). `P&128` set = free slot. Null until a
     *  level seeds the pool. */
    val pooledShots: Array<Entity?>? get() = null
    /** `i.av()` (i.java:22155, proven): index of the first pool slot
     *  whose `P&128` is set (free), else -1. Arming a slot clears bit
     *  128 (`P&=-129`, i.java:22203). */
    fun allocPooledShot(): Int = -1
    /** `k.aD` (k.java:169) — the HUD fuse-bar entity singleton (drawn at
     *  k.java:4073 as `120*(Z[1]-Z[2])/Z[1]`). ax27 claims/releases it. */
    var kAD: Entity? get() = null; set(_) {}
    /** `k.aO` (k.java:181) — HUD message countdown, `aO -= j.f` per tick. */
    var kAO: Int get() = 0; set(_) {}
    /** `k.aP` (k.java:182) — HUD message string (drawn at 200,23). */
    var kAP: String? get() = null; set(_) {}
    /** `k.d(int,int)` (k.java:486, proven): `r3==0 → bU[r4]` global table;
     *  else `j.g(r4)` — the per-level string table. Port signature takes
     *  the resolved level index (1+k.aj) and record index. */
    fun levelString(level: Int, idx: Int): String? = null
    /** `g.j` (g.java:15) — context latch, set on S145/S147 exits,
     *  cleared at g.java:6393 / k.java:6638. */
    var gj: Boolean get() = false; set(_) {}
    /** `k.L` — the entity currently registered as the interact claim. */
    var kL: Entity? get() = null; set(_) {}
    /** `k.au` — difficulty/medal-condition field (indexes `bu[]` HP table). */
    var kAu: Int get() = 0; set(_) {}
    /** `k.co` — claim priority countdown (reset 6 by `k.m()`). */
    var claimCo: Int get() = 6; set(_) {}
    /** `k.cp` — the claim marker rect (k.java:~820 `a(int[])`). */
    var claimRect: IntArray? get() = null; set(_) {}
    /** `k.m()` (k.java:863): reset the interact-claim channel. */
    fun claimReset() {}
    /** `k.a(i,int,int[])` (k.java:816): interact-claim registrar —
     *  same-entity refresh else `prio<co || prio==1` steals it. */
    fun registerClaim(e: Entity, prio: Int, rect: IntArray) {}
    // -- ax73 aJ() statics (i.java:77/100/132/9825, k.aA, g.z) -----------
    /** `i.bf` (i.java:100) — engage-claim latch for the aN-lock sweep. */
    var iBf: Boolean get() = false; set(_) {}
    /** `i.x` (i.java:94) — the static every-3rd-hit counter shared by
     *  all NPCs (j()'s half-HP engage arm). */
    var iX: Int get() = 0; set(_) {}
    /** `i.bx` (i.java:132) — entity holding the grab-QTE (S147). */
    var iBx: Entity? get() = null; set(_) {}
    /** `g.h` (i.java:5174/5765, proven) — the entity currently holding
     *  the player in a grab (set on grab bind, released at S96 expiry). */
    var grabHolder: Entity? get() = null; set(_) {}
    /** `k.aA` — shared engage/alert countdown (`aC()` zeroes it). */
    var kAA: Int get() = 0; set(_) {}
    /** `g.z` — player-side latch cleared on grab-entry/leap re-arm. */
    var gZ: Boolean get() = false; set(_) {}
    /** `i.L`/`i.M` (i.java:9825) — clip74 marker position latch. */
    var iL: Int get() = -1; set(_) {}
    var iM: Int get() = -1; set(_) {}
    /** `aS.l()` (g.java:4968) — grab-release resolver (`PlayerFsm.l`). */
    fun grabResolve(p: Entity): Boolean = false
    /** `g.c` (g.java:7) — static crate link: the ax51 the player is
     *  claimed on top of (bs() L188/L192 claim, L184 release). */
    var gc: Entity? get() = null; set(_) {}
    /** `g.h()` (g.java:3946, proven): transition latch —
     *  `g.s == true || g.t != 0`. */
    fun gH(): Boolean = false
    /** `k.s(i)` (k.java:7149, proven): `eH[]` display-row lookup (-1 miss). */
    fun kS(i: Int): Int = -1
    /** `k.S` — arena right bound (aP arena clamp). `k.R`/`k.S` pair. */
    var kSBound: Int get() = 0; set(_) {}
    // -- slice 64: k.N/cq prompt-marker + g.p kill-bonus (k.java:58/870/888,
    //    g.java:21) — the stealth-kill driver `k()` (i.java:2057) uses them.
    /** `k.N` — the shared prompt-marker entity (ax14/clip9/S54/az302)
     *  spawned by `k.c(x,y,aw)` and released by `k.k(aw)`. */
    var kN: Entity? get() = null; set(_) {}
    /** `k.cq` — uid bound to `k.N`; `k.k(aw)` releases iff `cq==aw` or
     *  `aw==-1`. -1 when idle. */
    var kCq: Int get() = -1; set(_) {}
    /** `k.c(int,int,int)` (k.java:870, proven): create `k.N` once then
     *  reposition it to (x,y) every call; bind `cq=aw`. */
    fun showPrompt(x: Int, y: Int, aw: Int) {}
    /** `k.k(int)` (k.java:888, proven): `cq==aw || aw==-1` → `N.p()` +
     *  `N=null` + `cq=-1`. */
    fun clearPrompt(aw: Int) {}
    /** `g.p` (g.java:21) — kill-bonus flag the `k()` arms write
     *  (`Z[14]`→1/2 for ax11; own `Z[0]` for ax47/50). */
    var gP: Int get() = 0; set(_) {}
}

/* `g.c(int)` (g.java:404, proven): interact-eligible player states —
 * the grounded/normal set ax19 pickups gate on (or `bh[aj]==3` levels). */
val INTERACTABLE_STATES = intArrayOf(0, 1, 7, 11, 12, 26, 79)

/**
 * Class `a` (the prompt/hint sprite) — the script-QTE prompt ops 107/112
 * spawn into `Entity.scriptPrompts` (`i.bA`), now riding the full
 * `UiAnimObject` port: `clipIdx` = `a.a(k.z[n])` (74 = touch art,
 * 9 = key art); `a`/`b`/`c`/`e`/`f`/`h`/`i`/`k` live on `anim` — the
 * original assigns `a`/`b`/`c` in the render path (k.java:3085-3113),
 * never in the ops.
 */
class ScriptPrompt {
    var clipIdx = -1
    val anim = UiAnimObject()
    var flag = 0

    /** `a.a(b)` (a.java:44) — bind the clip: records the pack index for
     *  the render's `drawFrame` AND hands the Clip to `anim`. */
    fun attach(idx: Int, clip: Clip?) { clipIdx = idx; anim.d = clip }
    var a: Int
        get() = anim.a
        set(v) { anim.a = v }
    var b: Int
        get() = anim.b
        set(v) { anim.b = v }
    val e: Int get() = anim.e

    /** `a.a(i,i2)` (a.java:53) — arm anim `s` for `f` loops
     *  (`-1` = infinite; verbatim `h = i2 - 1` inside `arm`). */
    fun setState(s: Int, f: Int) { flag = f; anim.arm(s, f) }
}

/**
 * Class `c` (c.java, proven) — the waypoint/marker pool. Level records
 * with `ax == 55` load into it (k.java:6049 `c.a(r0)` gated on
 * `r0[0] == 55`), never spawning entities; runners look them up by `k`.
 * `a,b` = position, `c,d,e,f,g` = record params (`f` = fly speed, `g` =
 * chain link used by ax21's director). `h,i` = runtime offsets written
 * by ax()'s companion bind (shorts in the original — kept Int here).
 */
class Waypoint {
    var k = 0          // record uid (spawned copies get j >= 10000)
    var a = 0; var b = 0
    var c = 0; var d = 0; var e = 0; var f = 0; var g = 0
    var h = 0; var i = 0

    /** `c.m[]` + `c.l` + `c.j` (c.java:14-17, proven) — fixed pool of 400;
     *  copies mint uids from `j` (base 10000, `a()` resets). */
    class Pool {
        val slots = arrayOfNulls<Waypoint>(400)
        var count = 0
        var nextUid = 10000

        /** `c.a(short[])` (c.java:26): load a record's 9 fields. */
        fun load(r: List<Int>) {
            if (count >= slots.size) return
            val w = Waypoint()
            w.k = r[1]; w.a = r[2]; w.b = r[3]
            w.c = if (r.size > 4) r[4] else 0
            w.d = if (r.size > 5) r[5] else 0
            w.e = if (r.size > 6) r[6] else 0
            w.f = if (r.size > 7) r[7] else 0
            w.g = if (r.size > 8) r[8] else 0
            slots[count++] = w
        }

        /** `c.a(int)` (c.java:63): first waypoint with `k == uid`;
         *  negative uid → null (verbatim early-out). */
        fun find(uid: Int): Waypoint? {
            if (uid < 0) return null
            for (i in 0 until count) {
                val w = slots[i] ?: continue
                if (w.k == uid) return w
            }
            return null
        }

        /** `c.a(c, i)` (c.java:45): copy `src` shifted by `+e.ak` on `a`
         *  (waypoints are entity-x-relative), mint `k = nextUid++`. */
        fun copyShifted(src: Waypoint, e: Entity): Waypoint {
            val w = Waypoint()
            w.k = nextUid++
            w.a = src.a + e.ak; w.b = src.b
            w.c = src.c; w.d = src.d; w.e = src.e; w.f = src.f; w.g = src.g
            if (count < slots.size) slots[count++] = w
            return w
        }

        /** `c.a()` (c.java:54): clear pool, `j = 10000`. */
        fun clear() {
            slots.fill(null); count = 0; nextUid = 10000
        }
    }
}
