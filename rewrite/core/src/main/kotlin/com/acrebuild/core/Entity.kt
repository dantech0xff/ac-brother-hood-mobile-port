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
    var l = 0                        // attack level fed to a(op,l,..)
    var hitsTaken = 0                // slice-3 instrumentation (inferred counter)
    var gt = 0                       // g.t iframe timer: 10 after drain, 5 after
                                     // volume hit, 100 after teleport; --/tick
    var bh = 0                       // i.bh hit-flash counter (visual pending)

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
    fun refreshBoxes() {
        val c = clip
        if (c == null || S < 0 || T < 0) { W.fill(0); X.fill(0); return }
        c.rect(S, T, 0, drawFlags(), W)
        val fi = c.frameIndex(S, T)
        val dx = c.frameDx[fi]
        val dy = c.frameDy[fi]
        if (av) W[0] -= dx else W[0] += dx
        W[1] += dy
        c.rect(S, T, 1, drawFlags(), X)
        if (av) X[0] -= dx else X[0] += dx
        X[1] += dy
        W[0] += ak; W[2] += W[0]
        W[1] += al; W[3] += W[1]
        X[0] += ak; X[2] += X[0]
        X[1] += al; X[3] += X[1]
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
        if (aS == 4 && aR < 12) al += 20   // L15 edge bump
        ah = 1
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
}
