package com.acrebuild.core

/**
 * Slice-2 world: level 0 (pack-6) tiles + entities, driven at the original
 * 62 ms tick through the ported `g.e()`/`i.I()` arm loops.
 *
 * Tick order (matches the original entity tick shape):
 *   input → Pad.commit → player collideSides(probe+resolve) → PlayerFsm
 *   dispatch (arms read the fresh probes) → integrate → s() anim advance →
 *   same for each NPC via NpcFsm.
 *
 * Touch zones map onto the J2ME pad word (inferred mapping — the original
 * used phone keys + an on-screen pad):
 *   top-third press     → v(16388) jump-family edge + hold bit
 *   middle-band halves  → held 4112 (left) / 8256 (right), press edge adds
 *                         the 2/8 tap bits (dash/back-dash windows)
 *   bottom-third        → held 33024 (down/crouch)
 * A second press inside ~8 ticks sets the `x()` double-tap bits via Pad.
 */
class Level0World(
    val level: LevelPack,
    val clips: Map<Int, Clip>,
    val rng: DeterministicRandom,
    /** `j.g` level-string table (pack-14 entry-001 for level 0). */
    val levelStrings: List<String> = emptyList(),
    /** `k.by`/`k.bz`/`k.eH` script tables (`j.e(7)` of the mission pack,
     *  k.java:6196). Null = no scripts (spawn smoke/tests w/o assets). */
    val scripts: ScriptTables? = null,
    /** `j.f(2)` charmap bytes (shared `short[]` font map) — builds the
     *  `y` FontClip used for `a(str,str2)` footer dims (:2276-2296). */
    val charmap: ByteArray? = null,
) : LevelCellSource {

    companion object {
        const val VIEW_W = 400
        const val VIEW_H = 240

        // Entity type -> clip index (k.bi[]; only decoded clips carried).
        // ax67 is special: the record binds `aa = k.r(bk[kind])` per-kind
        // (i.java:2633) — resolved in spawnEntities via NpcFsm.decorClip.
        val ENTITY_CLIP = mapOf(
            11 to 7, 17 to 7, 23 to 7, 47 to 7, 50 to 7, 73 to 7,
            44 to 32,
            4 to 3,       // ax4 destructible volumes (bi[4]=3, proven)
            5 to 1,       // ax5 mission logic (bi[5]=1, invisible clip)
            10 to 6,      // clip6 not converted yet — triggers spawn clipless
            14 to 9,      // ax14 pickups/markers (bi[14]=9; L88 record arm)
            16 to 10,     // ax16 request markers (bi[16]=10; bb() S30/38/39)
            71 to 26,     // generic a(ax) spawner pickups (bi[71]=26)
            27 to 48,     // ax27 fuse/message entity (bi[27]=48, proven)
            40 to 45,     // ax40 gondola/zipline (bi[40]=45, proven)
            9 to 47,      // ax9 push/contact entity (bi[9]=47, proven)
            15 to 25,     // ax15 grapple/hang volume (bi[15]=25, proven)
            46 to 29,     // ax46 spring/trap (bi[46]=29, proven)
            7 to 60,      // ax7 ejection slot (bi[7]=60, proven)
            72 to 51,     // ax72 counterweight platform (bi[72]=51, proven)
            78 to 63,     // ax78 counterweight (bi[78]=63, proven)
            79 to 0,      // ax79 palette prop (bi[79]=0, proven)
            6 to 4,       // ax6 overlap-trigger marker (bi[6]=4, proven)
            19 to 11,     // ax19 meter-restore pickup (bi[19]=11, proven)
            74 to 54,     // ax74 burst spark (bi[74]=54 — ax19's a(74,54,5,300))
            80 to 57,     // ax80 static prop (bi[80]=57 — pack-3 has no
                          // entry-057: J(57)=null → invisible/vestigial, proven)
            54 to 19,     // ax54 waypoint runner (bi[54]=19, proven)
            30 to 36,     // ax30 runner variant (bi[30]=36, proven)
            24 to 40,     // ax24 projectile (bi[24]=40 — pool children only)
            56 to 19,     // ax56 flyer (bi[56]=19 — same clip as ax54)
            58 to 20,     // ax58 lever/counterweight (bi[58]=20, proven)
            60 to 21,     // ax60 lift/piston platform (bi[60]=21, proven)
            43 to 31,     // ax43 ride carrier (bi[43]=31, proven)
            69 to 38,     // ax69 assassination-target zone (bi[69]=38, proven)
            64 to 6,      // ax64 harrier — bi[64]=-1 (clipless record spawn);
                          // unconverted index → null clip, matching the
                          // original's own clipless record path
            74 to 54,     // ax74 wisp (bi[74]=54, proven)
            76 to 56,     // ax76 hazard volume — bi[76]=56 but pack-3 slot 56
                          // is a zero-size entry (metadata, proven) → null
                          // clip, invisible trigger
        )
    }

    override val cellPx: Int get() = level.cellPx
    override fun collisionCell(cx: Int, cy: Int): Int = level.collisionCell(cx, cy)
    override fun isSolid(v: Int): Boolean = level.isSolid(v)
    override fun isOneWay(v: Int): Boolean = level.isOneWay(v)
    override var lockTarget: Entity? = null
    /** `k.ax` — meter-restore byte (k.java:159); save-load writes it,
     *  default is the x1 init value (inferred — dB restore path unmined). */
    override var kAx = 90

    val pad = Pad()
    val playerFsm = PlayerFsm(this, rng)
    val npcFsm = NpcFsm(this)

    var tickIndex: Long = 0L
        private set
    var deaths = 0                     // mission-fail count (instrumentation)
    /** `j.c==12` mission-fail screen (k.l(12) target, k.java:2031). */
    val failed get() = jC == 12
    /** `j.c∈{13,31}` — the win screen: `k.l(13)` lands on 31 when
     *  `kBx >= 0` (k.java:2280-2285, proven); 13 only sticks when bx<0.
     *  Same freeze contract as `failed`; confirm (`v(65568)`,
     *  k.java:1804) → reload(), our only level (`inferred` milestone). */
    val won get() = jC == 13 || jC == 31

    /** ax2 checkpoint record (i.java:13477 aY). `aw` = record id. */
    data class Checkpoint(val aw: Int, val ak: Int, val al: Int, var consumed: Boolean = false)

    /** Snapshot written into bA[16..] by aY()/i.X() — i.java:18631:
     *  pos/facing (18-22), g.J/g.I (24/26), ap[0,3,2/16,4] + ap[5] at
     *  52+aj*2, ax/ay/az/aN/aL (28-34, 50), aZ/bn flags, br[] dead set.
     *  The k.ax/ay/az/aN/aL globals have no producers in our model yet
     *  (mission-script ops, unknown) — modeled fields only. */
    data class Snapshot(val aw: Int, val ak: Int, val al: Int,
                        val av: Boolean, val x1: Int,
                        val gJ: Int, val gI: Int, val ap: IntArray)

    val checkpoints: List<Checkpoint> = level.entities
        .filter { it.size >= 4 && it[0] == 2 }
        .map { Checkpoint(it[1], it[2], it[3]) }
    var checkpointSnap: Snapshot? = null
        private set
    private var checkpointDead: Set<Int> = emptySet()  // br[]-equivalent
                                                       // dead at save time

    override val player = Entity(0, clips[0]).apply { aw = -1 }
    override val npcs = ArrayList<Entity>()
    val pendingRemove = HashSet<Entity>()     // k.c() drain buffer

    // ax55 waypoint pool (c.java:1-120 proven — k.c field): records carry
    // {uid,x,y,param-d,c,f,g} in f[1..7]; ax54/ax30 runners resolve their
    // Z[1..4] uid chain into entity-shifted copies (aw(), uid base 10000).
    // Declared before init{} — spawnEntities() calls waypointPool.clear().
    val waypointPool = Waypoint.Pool()

    // ax24 projectile pool (k.aX = new i[k.aW=50], i.java:2837 proven):
    // seeded by the first S==0 ax24 record; slots with P&128==0 are free.
    var projectilePool: Array<Entity?>? = null

    /** `k.ap[]` progress counters (k.java:4314/L() proven): `k.e(r5,uid)`
     *  registers kills — `ap[0]++` when `uid>0 && kAj!=7` (r5 ignored
     *  verbatim — always slot 0). */
    override val kAp = IntArray(6)
    fun countKill(uid: Int) { if (uid > 0 && kAj != 7) kAp[0]++ }

    var kDg = 0                           // k.dg — mission-frame counter (timer)
    var kAy = 30                          // k.ay — sync byte (bA[46])
    var kAN = 0                           // k.aN — misc stat (bA[48])
    var kDj = 0                           // k.dj — typewriter index
    var kDk = 0                           // k.dk — typewriter countdown
    /** `k.eg[]` (k.java:265, proven) — all-true episode flags; drives
     *  post-mission nav `eg[aj] ? l(30) : l(2)`. */
    val kEgFlags = BooleanArray(9) { true }
    /** `k.dh/k.di` (k.java:199-200, proven) — per-difficulty score
     *  multipliers {100,200,300} for kills / collects. */
    val kDH = intArrayOf(100, 200, 300)
    val kDI = intArrayOf(100, 200, 300)
    // -- M() win-stats surface (k.java:3280-3445, inferred render state)
    var statsTitleY = 0
    var statsScore = 0
    var statsTimeSec = 0
    var statsScoreVisible = false
    var statsTypeNext = -1
    var typewriterText = ""
    /** Row value strings per `d(0,38+i3)` label — filled as each `j.g`
     *  gate passes (`bW.a(cd,…,305,55+i3*20,24)`, proven positions). */
    val statsRowText = Array(5) { "" }

    // --- ag() poster card (k.java:6358) + ah() medal viewer (:6392) ---
    var posterVisible = false               // jC==10 render arm
    var posterFrame = -1                    // A[4] frame i+4 (or 8)
    var posterBrief = ""                    // d(0,110)
    var cardOverlayY = -1                   // i(0,i2) card overlay
    var hintBlink = false                   // j.g%10<5 → d(0,9)
    var hintBack = false                    // a("",d(0,17))
    var medalVisible = false                // jC==22 render arm
    var medalTitle = ""                     // d(0,113)
    val medalRowIcon = IntArray(3) { -1 }   // z[73] frame per slot
    val medalRowText = Array(3) { "" }      // d(0,114+i)
    val medalRowDim = BooleanArray(3)       // y.l(4) locked/dim row
    var medalRowCount = 0                   // drawn slots
    var screenFadeAlpha = 0                 // j.h fade (10-j.g)*25

    /** `j.c(i,0)` (j.java:1202, proven head) — thousands-grouped digits:
     *  `<1000` raw, else separator groups (`,` inferred — the locale
     *  switch decompiled oddly). */
    fun fmtJ(i: Int): String = if (i in -999..999) i.toString()
        else "%,d".format(i)
    /** `i.av()` (i.java:7813 proven): first RESERVED slot (P&128 != 0);
     *  arming clears bit128 (`P &= -129`) marking the slot live again. */
    fun projectileAlloc(): Int {
        val pool = projectilePool ?: return -1
        for (i in pool.indices) if (((pool[i]?.P ?: 0) and 128) != 0) return i
        return -1
    }
    // `k.aK` insert buffer (k.b(i), proven): entities spawned mid-tick join
    // `bb[]` at the drain after the npc pass — never iterate-mutated.
    val pendingInsert = ArrayList<Entity>()     // k.b() drain buffer
    override fun removeEntity(e: Entity) {
        pendingRemove += e
        if (player.gd === e) player.gd = null
        if (lockTarget === e) lockTarget = null
        if (claimed === e) clearClaim()
        if (marker === e) { marker = null; markerTag = -1 }
    }

    // -- k.a(i,prio,rect) context-claim system (k.java:816, proven) -------
    // Strictly-lower priority wins (co starts at 6 = unclaimed); an equal
    // bid only steals when prio==1&&co==1. Claim persists until released
    // via k.m() (clearClaim) — there is no per-frame reset. cp = claimed
    // rect padded ±10 (k.java:850). ax51's Y-swap unexercised (unspawned).
    override var claimPrio = 6       // k.co — written only via claim()/
                                     // clearClaim() (interface exposes set)
    override var claimed: Entity? = null  // k.L
    val claimPad = IntArray(4)       // k.cp
    // Hoisted above `init`: `spawnEntities` reads it via kSIndex during
    // ax5 record init — property order matters (backing field is null
    // until the initializer runs).
    override var kEh = scripts?.eH ?: IntArray(0)  // k.eH — script uids
    override val kBy = scripts?.by ?: emptyArray() // k.by — op blocks
    override val kBz = scripts?.bz ?: emptyArray() // k.bz — group offsets
    override fun claim(e: Entity, prio: Int, w: IntArray) {
        if (prio < 0 || prio >= 6) return
        if (prio >= claimPrio && !(prio == 1 && claimPrio == 1)) return
        claimPrio = prio; claimed = e
        claimPad[0] = w[0] - 10; claimPad[1] = w[1] - 10
        claimPad[2] = w[2] + 10; claimPad[3] = w[3] + 10
    }
    override fun clearClaim() { claimPrio = 6; claimed = null }

    // -- k.c(x,y,aw)/k.k(aw) marker popup (k.java:870-902, proven) --------
    // Singleton ax14 entity S54 on clip9 (r(9)); later k.c calls just move
    // it. k.k(aw) removes it on tag match (or any when tag==-1); the
    // original plays out via N.p() — port removes on the drain.
    override var marker: Entity? = null
    private var markerTag = -1       // k.cq
    override fun setMarker(x: Int, y: Int, tag: Int) {
        val m = marker
        if (m == null) {
            val e = Entity(14, clips[9]).apply {
                setAnim(54); az = 302; au = 0; setPositionPx(x, y)
            }
            marker = e; markerTag = tag; pendingInsert += e   // k.b(aK)
        } else m.setPositionPx(x, y)
    }
    override fun clearMarker(tag: Int) {
        if (marker == null) return
        if (markerTag == tag || tag == -1) {
            marker?.let { pendingRemove += it }
            marker = null; markerTag = -1
        }
    }

    // -- k.aq / k.ap / k.s() / k.A(int) counters --------------------------
    override var aq = 0              // k.aq — global tally (ax4 S5 += m)
    override var shake = 0           // k.az side of k.s() (k.java:5338;
                                     // the dE threshold ladder is unported)
    override val sfxLog = mutableListOf<Int>()  // k.A(int) — audio unported
    override fun sfx(id: Int) { sfxLog += id }
    override fun shake() { shake++ } // k.s()

    /** `k.O` — camera left edge (the pickup pin anchor, i.java:9837;
     *  writable — op11/12 camera arms lerp it). */
    override var kO: Int get() = camX; set(v) { camX = v }
    /** `k.P` — camera top edge (u()'s view-center operand; writable). */
    override var kP: Int get() = camY; set(v) { camY = v }
    /** `k.ac` — camera view rect [x1,y1,x2,y2] (v() on-screen check). */
    override val camRect: IntArray get() =
        intArrayOf(camX, camY, camX + VIEW_W, camY + VIEW_H)
    /** `k.bh[k.aj]==3` — gameplay phase (mission-fail screen is phase 12). */
    /** `k.al == false` (i.I() entity gate): the real world-run condition
     *  — false on states {12,13,16,17,31} and {21 when kMode∉{8,9}}. */
    override val inPlay: Boolean get() = !kAl
    /** `k.cm` — mounted flag (k.k() at k.java:638; `cm=true` writes 1 at
     *  g.java:3564). */
    var cm = 0
    override val mounted: Boolean get() = cm == 1
    override fun setMounted() { cm = 1 }
    /** `k.H`/`k.I` — the pointer-RELEASE point in view px (-1 = none).
     *  Proven k.java:549-553/1874-77: `pointerReleased` is the only
     *  writer of `ch/ci`, and the frame loop re-reads them once then
     *  resets — so `k.c`/`k.j` see a tap for exactly one tick. */
    var lastTouchX = -1
    var lastTouchY = -1
    /** `k.J`/`k.K` — the live pointer point in view px (-1 = none);
     *  `pointerPressed/Dragged/Released` all write `cj/ck` (k.java:543-566)
     *  and `cl` clears them the frame after release. */
    var lastMoveX = -1
    var lastMoveY = -1

    // -- pointer pipeline (slice 79 — k.java:486-519 verbatim) -----------
    private var kCj = -1                     // k.cj — held/drag x
    private var kCk = -1                     // k.ck — held/drag y
    private var kCh = -1                     // k.ch — release x
    private var kCi = -1                     // k.ci — release y
    private var kCl = false                  // k.cl — release latch
    /** `j.t` (j.java:105, proven) — pad-bits-0-4 held latch: `j.a(i)` ∨=
     *  `1<<i` on press, `j.b(i)` clears on release; `j.i()`=`t!=0` lets
     *  the fail/win screens force-flush held input (`j.t=0`, :1119). */
    var kJT = 0
    /** `k.bh[]` (k.java:263, proven): per-mission phase flags — `bh[aj]==3`
     *  = autoscroll/flying on missions 1 and 4. */
    val kBh = intArrayOf(4, 3, 4, 4, 3, 4, 4, 4, 4)
    val bh3: Boolean get() = kAj in kBh.indices && kBh[kAj] == 3
    /** `k.u` — screen-21 dialog sub-state (j() gate needs u∈{8,10};
     *  `inferred` — orig's u is written by script ops). */
    var subU = 0
    /** `i.L`/`i.M` (i.java:174-175, proven): entity-side touch anchor —
     *  `i.o(x,y)` writes it, `i.U()` clears when the anchor entity
     *  deactivates; `i.b(x,y)` hit-tests ±70px radial in view space. */
    var anchorLx = -1
    var anchorLy = -1
    fun setInteractAnchor(x: Int, y: Int) { anchorLx = x; anchorLy = y }  // i.o()
    fun clearInteractAnchor() { anchorLx = -1; anchorLy = -1 }            // i.U() tail
    override fun clipFor(idx: Int): Clip? = clips[idx]
    /** `k.a(k.H,k.I, e.ak-k.O, e.al-k.P, r)` (k.java:627): touch point vs
     *  entity in view space — equivalent to world-space vs (k.H+k.O). */
    override fun touchNearView(e: Entity, r: Int): Boolean =
        lastTouchX >= 0 &&
            e.h(Math.abs(lastTouchX + camX - e.ak), Math.abs(lastTouchY + camY - e.al)) <= r
    /** `k.aS.W` — player hitbox. */
    override fun playerRect(): IntArray = player.W

    /** `k.q(aw)` (k.java:5887, proven): `aw==-1 → null` head guard, then
     *  `aS.aw==aw → aS` else the `bb[]` scan by uid. */
    override fun findByAw(aw: Int): Entity? {
        if (aw == -1) return null
        return if (player.aw == aw) player else npcs.firstOrNull { it.aw == aw }
    }

    /**
     * `i.a(int,int,int)` (i.java:9810) → `a(ax,clip,anim,az)` spawner
     * (i.java:4799, proven): `aw=-1, au=0, ax=14, clip9, i(anim), az=302`,
     * caller pos/facing (overridden to (x,y), av=false by i.a()), `t()` —
     * which early-returns for ax14 leaving the zero-W `aX()` guard.
     * NOTE: no `P|=512` — that's the 7-arg particle spawner's flag.
     */
    override fun spawnPickup(anim: Int, x: Int, y: Int): Entity {
        val e = Entity(14, clips[9]).apply {
            aw = -1; au = 0
            setAnim(anim); az = 302
            setPositionPx(x, y); av = false
            refreshBoxes()          // t() early-returns for ax14 → W zero
        }
        pendingInsert += e                        // k.b(aK)
        return e
    }

    /** `i.a(_,5,14,av,x,y,300)` (i.java:6898, proven): the ax8 knife
     *  projectile — aw=-1, au=0, clip5 anim14, az300, `P|=512`, `k.b`.
     *  First arg unused in the original (ax hardcoded 8). */
    override fun spawnProjectile(av: Boolean, x: Int, y: Int): Entity {
        val e = Entity(8, clips[5]).apply {
            aw = -1; au = 0; az = 300
            setAnim(14); setPositionPx(x, y); this.av = av
            P = P or 512
            refreshBoxes()
        }
        pendingInsert += e                        // k.b(r0)
        return e
    }

    // -- equip/context statics (k.ar/as/at/C/ae + g.a/i/E) --------------------
    override val equipList = IntArray(5) { -1 }   // k.ar[5]
    override var equipCount = 0                 // k.as
    override var actionLock = 0                 // k.at
    override var cEntity: Entity? = null        // k.C
    override var vehicle: Entity? = null        // g.a
    override var cv: Entity? = null             // i.cv — ax10-S51 rail zone
    override var iFlag = true                   // g.i
    override var eFlag = false                  // g.E

    /** `k.q()` (k.java:~4600, proven): rebuild `ar[]`/`as` from `player.gJ`
     *  — iterates bits 0..4, takes set bits except mask-4, first-empty
     *  slot in ascending order, then clears the source bit. */
    override fun rebuildEquip() {
        equipList.fill(-1)
        var rest = player.gJ
        var slot = 0
        equipCount = 0
        for (r5 in 0 until 5) {
            val r0 = 1 shl r5
            if ((rest and r0) == 0 || r0 == 4) continue
            equipList[slot++] = r0
            rest = rest and r0.inv()
            equipCount++
        }
    }

    /** `k.c(x,y,w,h)` — view-space touch-rect test. The original reads the
     *  J2ME pointer state; the port maps it onto the last touch coords
     *  (inferred — pointer plumbing predates the input queue). */
    override fun touchRect(x: Int, y: Int, w: Int, h: Int): Boolean =
        lastTouchX in x..x + w && lastTouchY in y..y + h

    // -- marker/sweep globals -------------------------------------------------
    override var cFFlag = false                 // i.cF gauge-full static
    override var playerLinkB: Entity? = null    // g.b marker-engage link
    override val missionIndex = 0               // k.aj — level 0 = mission 0
    var statTally0 = 0                          // k.ap[0] kill/stat tally
    override var iBh = 0                        // i.bh static hit-lock
    /** `k.e(0,aw)` (k.java:4314, proven): `ap[0]++` when `aw>0 && aj!=7`. */
    override fun statTally(aw: Int) {
        if (aw > 0 && missionIndex != 7) statTally0++
    }

    /** `m(int)` particle burst (i.java:21259, proven): `a(74,54,1,
     *  player.az+1)` via the generic spawner (i.java:4799) — random angle
     *  aD = j.a(0,360), launch radius cap aE = j.a(70,90), aC=2 drift legs,
     *  anchor (aq,ar) = src pos, P=528, af=src, aG=1 → k.b(aK) joins npcs. */
    override fun spawnWisp(src: Entity) {
        val w = Entity(74, clips[54]).apply {
            aw = -1
            setAnim(1); az = player.az + 1
            setPositionPx(src.ak, src.al); av = src.av
            aD = jRand(0, 360); aE = jRand(70, 90)
            aA = 0; j = 0; aC = 2
            aq = src.ak; ar = src.al
            P = 528; af = src; aG = 1
        }
        pendingInsert += w                        // k.b(aK)
    }

    /** `j.a(lo,hi)` (j.java:322, proven): lo + |nextInt| % (hi-lo). */
    override fun jRand(lo: Int, hi: Int): Int {
        if (hi == lo) return hi
        val r = rng.nextInt()
        return lo + (if (r >= 0) r else -r) % (hi - lo)
    }

    var camX = 0                              // k.O — camera x
        private set
    var camY = 0                              // k.P — camera y
        private set
    // -- k.m(int) tracker state (k.java:2346-2703, proven) -------------
    private var camA = 0                      // cA — x target (static, sticky)
    private var camB = 0                      // cB — y target (static, sticky)
    private var camCC = 0                     // cC — x step (lerp out / ax43 speed)
    private var camCD = 0                     // cD — y step
    private var camM = 200                    // cM — look-ahead x margin
    private var camCI = 0                     // cI — focus-N watch
    private var camXw = -7                    // m()'s X watch counter (L300);
                                              // the original reuses k.X — D()
                                              // drains W into it on bh3; kept
                                              // private since m() and D() never
                                              // co-run (bh!=3 vs bh==3 arms)
    private var camCN = 0                     // cN — D()'s player-ak snapshot
    private var camCG = -1                    // cG — cached corridor left
    private var camCH = -1                    // cH — cached corridor right
                                              // (populated by the k.ai row
                                              // scan, read back when iBV>0)
    private var camAf = 0                     // af — lookahead x offset
    private var camAg = 0                     // ag — lookahead y offset
    private var camCF = 0                     // cF — snap-arm scratch
    private var camCE = 0                     // cE — snap-arm scratch
    var gV = false                            // g.v — full camera warp flag
    var kDz = 120                             // k.dz — fade counter

    /** ax37 scroll-bound trigger (i.java:7053 al()).
     *  W = zone rect ak+f7,al+f8,+f9,+f10 ; X = bound rect ak+f11..f14 ;
     *  mask = Z[0]=f15 ; mode = Z[3]=f18 (1: fire while overlap a(),
     *  0: fire on full containment b()). All level-0 records carry
     *  Z[2]==-1 → the linked-entity gate is unexercised and unported. */
    data class ScrollTrigger(val zone: IntArray, val bound: IntArray,
                             val mask: Int, val mode: Int)

    val scrollTriggers: List<ScrollTrigger> = level.entities
        .filter { it.size >= 19 && it[0] == 37 }
        .map { f ->
            ScrollTrigger(
                intArrayOf(f[2] + f[7], f[3] + f[8],
                           f[2] + f[7] + f[9], f[3] + f[8] + f[10]),
                intArrayOf(f[2] + f[11], f[3] + f[12],
                           f[2] + f[11] + f[13], f[3] + f[12] + f[14]),
                f[15], f[18])
        }

    // k.R/k.T/k.S/k.U — camera scroll bounds written by ax37 triggers
    // (consumed at k.java:2430-2486: camX∈[R, S-400], camY∈[T, U-240];
    // <=0 means unset).
    var boundMinX = 0; var boundMinY = 0
    var boundMaxX = 0; var boundMaxY = 0

    init {
        resetPlayerToSpawn()
        spawnEntities()
    }

    private fun resetPlayerToSpawn() {
        // aY() snapshot (bA[18..24]) when a checkpoint fired, else level spawn
        val s = checkpointSnap
        if (s != null) {
            player.setPositionPx(s.ak, s.al)
            player.av = s.av
            player.x1 = s.x1
        } else {
            val spawn = level.playerSpawn() ?: (100 to 200)
            player.setPositionPx(spawn.first, spawn.second)
            player.av = false
            player.x1 = 90
        }
        player.setAnim(0)
        player.ag = 0; player.ah = 0; player.ai = 0; player.aj = 0
        player.gt = 0; player.bh = 0
        // ax10-published player statics (i.java:2492-2512 level-init clears)
        player.gn = 0; player.go = 0; player.gk = -1; player.gd = null
        player.gB = false; player.gL = 0; player.gA = false
    }

    private fun spawnEntities() {
        npcs.clear()
        pendingRemove.clear()
        pendingInsert.clear()
        kD = null; kE = null                     // k.V() (k.java:6640-6641)
        lockTarget = null
        clearClaim()
        marker = null; markerTag = -1
        waypointPool.clear()
        projectilePool = null
        for (f in level.entities) {
            if (f.isEmpty()) continue
            if (f[0] == 55) { waypointPool.load(f.toList()); continue }   // k.java:6049
            if (f.size < 7) continue
            // Retype head (i.java:2640-2651, proven): ax11 records whose
            // spawn anim r8[5]∈{80,93} become ax47 ledge sentinels; ax17
            // records with r8[5]==120 become ax50 pouncers. The switch
            // dispatch sees the retyped ax — apply before clip lookup.
            val type = when {
                f[0] == 11 && f.size > 5 && (f[5] == 80 || f[5] == 93) -> 47
                f[0] == 17 && f.size > 5 && f[5] == 120 -> 50
                else -> f[0]
            }
            // ax67: per-record clip from bk[kind] (i.java:2633); others use
            // the bi[] table. decorClip(-1)/missing clip → record skipped.
            val clipIdx = if (type == 67) NpcFsm.decorClip(if (f.size > 7) f[7] else -1)
                          else ENTITY_CLIP[type]
            if (clipIdx == null || clipIdx < 0) continue
            val e = Entity(type, clips[clipIdx]).apply {
                aw = f[1]
                setPositionPx(f[2], f[3])
                homeX = f[2]; homeY = f[3]
                P = f[6]
                av = (f[6] and 1) != 0
            }
            if (type == 11) npcFsm.initSoldier(e, f.toList())
            else if (type == 44) npcFsm.initDoor(e, f.toList())
            else if (type == 10) npcFsm.initTrigger(e, f.toList())
            else if (type == 4) npcFsm.initDestructible(e, f.toList())
            else if (type == 67) npcFsm.initDecor(e, f.toList())
            else if (type == 14) npcFsm.initPickup(e, f.toList())
            else if (type == 5) npcFsm.initMissionLogic(e, f.toList(), this)
            else if (type == 27) npcFsm.initAx27(e, f.toList(), this)
            else if (type == 40) npcFsm.initAx40(e, f.toList())
            else if (type == 9) npcFsm.initAx9(e, f.toList(), this)
            else if (type == 6) npcFsm.initAx6(e, f.toList())
            else if (type == 19) npcFsm.initAx19(e, f.toList())
            else if (type == 35) npcFsm.initAx35(e, f.toList(), this)
            else if (type == 15) npcFsm.initAx15(e, f.toList(), this)
            else if (type == 46) npcFsm.initAx46(e, f.toList(), this)
            else if (type == 7) npcFsm.initAx7(e, f.toList(), this)

            else if (type == 42) npcFsm.initAx42(e, f.toList())
            else if (type == 35) npcFsm.initAx35(e, f.toList(), this)
            else if (type == 13) npcFsm.initAx13(e, f.toList())

            else if (type == 72) npcFsm.initAx72(e, f.toList(), this)
            else if (type == 78) npcFsm.initAx78(e, f.toList(), this)
            else if (type == 79) npcFsm.initAx79(e, f.toList(), this)
            else if (type == 80) npcFsm.initAx80(e, f.toList(), this)
            else if (type == 54 || type == 30) npcFsm.initAx54(e, f.toList(), this)
            else if (type == 56) npcFsm.initAx56(e, f.toList(), this)
            else if (type == 60) npcFsm.initAx60(e, f.toList(), this)
            else if (type == 69) npcFsm.initAx69(e, f.toList(), this)
            else if (type == 73) npcFsm.initAx73(e, f.toList())
            else if (type == 47) npcFsm.initAx47(e, f.toList())
            else if (type == 50) npcFsm.initAx50(e, f.toList())
            else if (type == 17) npcFsm.initAx17(e, f.toList())
            else if (type == 24) npcFsm.initAx24(e, f.toList(), this)
            else if (type == 64) npcFsm.initAx64(e, f.toList())
            else if (type == 74) npcFsm.initAx74(e, f.toList(), this)
            else if (type == 76) npcFsm.initAx76(e, f.toList())
            else if (type == 15) npcFsm.initAx15(e, f.toList(), this)
            else if (type != 37)
                for (i in e.Z.indices) if (7 + i < f.size) e.Z[i] = f[7 + i]
            // palette slot (proven i.java:4180-4194): ax11 picks aH=1 for
            // Z[0]∈{1,2} (red-uniform variant), aH=0 otherwise; the player
            // uses bo[bL][0]=0 for level 0 (bo={{0,-1},{3,1},{5,2},{6,3}},
            // k.java:8450) — every other spawned type keeps aH=0.
            if (type == 11 && (e.Z[0] == 1 || e.Z[0] == 2)) e.palette = 1
            // br[] parity: entities already dead when the checkpoint fired
            // stay dead through the reload (as==-98 persisted).
            if (checkpointDead.contains(e.aw)) e.setAnim(139)
            npcs += e
        }
        // R() (i.java:8131 proven): after all entities+waypoints load,
        // ax54/ax30 runners resolve their Z[1..4] uid chain via aw().
        for (e in npcs) if (e.ax == 54 || e.ax == 30)
            npcFsm.resolveRunnerWaypoints(e, this)
        spawnCompanions()
        // init-time k.b inserts (k.D, record-arm children) land in the
        // live pool during load in the original — drain immediately.
        if (pendingInsert.isNotEmpty()) { npcs += pendingInsert; pendingInsert.clear() }
    }

    /**
     * Player-init L43 tail (i.java:2748-2779, proven): the player's
     * ctor arm spawns the two companion overlays once each — `k.D`
     * (ax34 follower, clip `k.r(42)`, `P|=16|512`, player's `az`) and
     * `k.E` (ax71 struggle-QTE overlay, clip `k.r(46)`, `az=300`,
     * `P|=16|128` hidden). Both persist until `k.V()` (the clear
     * block above) nulls them; `k.b` = the insert queue.
     */
    private fun spawnCompanions() {
        if (kD == null) {
            kD = Entity(34, clips[42]).apply {
                aw = -1; au = 0
                setAnim(0); az = player.az
                setPositionPx(player.ak, player.al)
                av = false; refreshBoxes()
                P = P or 16 or 512
            }
            queueInsert(kD!!)
        }
        if (kE == null) {
            kE = Entity(71, clips[46]).apply {
                aw = -1; au = 0
                setAnim(0); az = 300
                setPositionPx(player.ak, player.al)
                av = false; refreshBoxes()
                P = P or 16 or 128
            }
        }
    }

    /**
     * `k.l(12)` mission fail (i.java:1389 proven — player below the camera
     * bottom, or `d()` knockout with x[1]<=0): the original swaps to the
     * j.c=12 fail screen via `k.l(12)` = `stateL(12)`; confirm
     * (`v(65568)`, k.java:1804) then runs `f(false)` = reload().
     */

    /** `k.l(15)` mission-complete → `stateL(15)` (stats screen arm:
     *  medal stamps, medal/next-mission redirects). */
    var missionWon = false
    override fun missionComplete() { stateL(15) }

    // -- ax21 director statics (i.bD, i.java:72-184 + k fields) --------------
    override var iBV = 0                       // i.bV — script-written
    override var iBU = 0                       // i.bU — gauge cap
    override var iBW = false                   // i.bW — pending transition
    override var iBX = 0                       // i.bX
    override var iBT = false                   // i.bT — director armed
    override var iBj = false                   // i.bj — finale freeze
    override var iBe = false                   // i.be — D() X-lock
    override var iQ = false                    // i.q — gauge-charge mode
    override var iCC = 0                       // i.cC — waypoint phase
    override var iCD = -1                      // i.cD — active phase
    override var iCE = -1                      // i.cE — f(int) gate
    override val waypoints = WaypointPool()    // c.m/l/j
    override var dirWp: WaypointNode? = null   // i.cB
    override var kB: Entity? = null            // k.B — arena boundary
    override var kAi = false                   // k.ai — director active
    /** `k.R` — the camera left bound: one static reused by ax37 scroll
     *  triggers (boundMinX), the bD director (`k.R=-1` init arm), and the
     *  aP arena clamp — aliased to `boundMinX` (proven same field). */
    override var kR: Int get() = boundMinX; set(v) { boundMinX = v }
    override var kAE = 0                       // k.aE
    override var kAF = 0                         // k.aF — meter fill
    override var kAH = 0                       // k.aH
    override var kAR = 0                       // k.aR — chase row
    override val kBu: Int get() = level.worldH // k.bu — level height px
    /** `k.bk[]` record-type table — unmined (inferred, default 0). */
    override fun kBk(i: Int): Int = 0
    override fun jNextInt(): Int = rng.nextInt()
    override fun queueInsert(e: Entity) { pendingInsert += e }


    // -- ax29 boss FSM (i.aP) statics ----------------------------------
    override var kAU: Entity? = null           // k.aU
    override var kE: Entity? = null            // k.E
    override var kD: Entity? = null            // k.D — ax34 follower
    override var kC: Entity? = null            // k.C
    override var iBy = 0                       // i.by — boss phase tier
    override var iCi: IntArray? = null         // i.ci[5]
    override var iCj = false                   // i.cj
    override var iCk: Entity? = null           // i.ck — aura entity
    override var iCl: Entity? = null           // i.cl — idle add
    override var iCm = false                   // i.cm
    override var iCn = 0                       // i.cn — counter ramp
    override var iCo = 0                       // i.co — saved state
    override var iCp = 0                       // i.cp — exhaust
    override var iAH = false                   // i.aH — slow-mo flag
    override var iAI = 0                       // i.aI
    override var iAJ = 0                       // i.aJ
    override var kX = -7                       // k.X (k.java:2334 init, proven)
    override var kW = 0                        // k.W
    override var kAw = 0                       // k.aw
    override var kAe: Entity? = null           // k.ae — player link entity
    override var kAh: Entity? = null           // k.ah
    override var kZ = false                    // k.Z
    override var kAa = false                   // k.aa
    override var kAb = false                   // k.ab
    override var kAj = 0                       // k.aj — level index 0
    /** Slice-43b claim-script VM state (aa() arms): world bounds for the
     *  op11/12 camera clamp, `j.g` tick, `k.bb/bc` follower scan, and
     *  the `k.*`/`i.*` statics the arg-op sub-switches write. */
    override val kBr: Int get() = level.worldW   // k.br — level width px
    override val kBs: Int get() = level.worldH   // k.bs — level height px
    /** `j.g` — the render-frame counter: separate from `j.f` (tickIndex);
     *  `l()` resets it to 0 on every state entry (k.java:2047, proven)
     *  and the frame loop increments it once per tick. Blink/`%N` checks
     *  in entity code read this, not tickIndex. */
    override var jG = 0L
    override val kBb: List<Entity> get() = npcs  // k.bb follower list
    override val kBc: Int get() = npcs.size      // k.bc
    override var kAd = 2                         // k.ad=2 (k.java:8348 static init)
    override var kAV: Entity? = null             // k.aV
    override var kAQ: Entity? = null             // k.aQ
    override var kAv = false                     // k.av
    override var kAT = false                     // k.aT
    override var kAL = 0                         // k.aL
    override var kBK = false                     // k.bK — HAS-BLOOD JAD
                                                 // flag (GloftASBR:36):
                                                 // manifest lacks it →
                                                 // NPE catch → false
                                                 // (censored anim set)
    override var kBx = -1                        // k.bx — stats text idx;
                                              // -1 = none pending (else the
                                              // `l(13)&&bx>=0→31` remap mis-fires)
    override var kBw = 0                         // k.bw — l(13) sentinel

    // -- k.l() screen-state machine (k.java:2031-2300 simple, structured
    //    :1637 — proven core) -----------------------------------------------
    /** `j.c` — game state: 8 in-play (proven — `j()` gates on j.c==8,
     *  k.java:576; 10 is the level-select screen, NOT play); 12 fail;
     *  13/31 win; 15 complete stats; 16/17 frozen; 21 dialog; 22
     *  medal-unlock; 5 win-stats build; others per `stateL`. */
    var jC = 8                           // j.c — in-play screen state
        private set
    var kCy = 0                        // cy — previous j.c, written at commit
    private var kCz = false            // cz — u==8 dialog-tail flag
    /** `k.al` — world-freeze flag: set by l() for states
     *  {12,13,16,17,31} and {21 when kMode∉{8,9}}; the entity gate
     *  `k.al == false` in `i.I()` (k.java:4860-ish) is the real
     *  tick-suppress condition our `inPlay` derives from. */
    var kAl = false
        private set
    var kMode = 0                      // k.u — screen mode 0-10 (NOT k.U)
    var kEg = 0                        // k.eG
    var kCZ = 0                        // k.cZ — win-stats row counter / G() total
    /** `cV[4]` (k.java:184) — G() help pages. */
    val kCV = arrayOfNulls<String>(4)
    /** `cX[4]` (k.java:185) — G() per-page 8-line-screen counts. */
    val kCX = IntArray(4)
    var kCY = 1                        // k.cY — G() current 8-line screen
    var kCW = 0                        // k.cW — G() page-1 pad
    var kCb = false                    // k.cb
    var kCu = 0                        // k.cu — k-side flag (entity cu is i's)
    var kFd = 0                        // k.fd
    var kFe = 0                        // k.fe
    // k.aD → `kAD` (existing field, HUD fuse entity — same original field)
    var kEc = 0                        // k.eC — screen timer
    var kEb = 0                        // k.eB — banner variant
    var kEe = 0                        // k.eE — stats width
    var kEf = 0                        // k.eF
    var kFo = 0                        // k.fO
    var kFC = 0                        // k.fC — af() title fade (20→255)
    var kFQ = 1                        // k.fQ — af() unlocked row count
    var kBL = 0                        // k.bL — af() browse cursor
    var kFR = -1                       // k.fR — af() pending-nav timer
    /** `k.fP` (k.java:344) — the 4 medal-count thresholds. */
    val kFP = intArrayOf(0, 2, 5, 7)
    /** renderer's `fK` row-anim rearm flag — af() nav `fK.a(21,1)`:
     *  set to a state index, renderer arms+resets to -1 (`inferred`
     *  plumbing — the orig draws+animates in one proc). */
    var menuFkArm = -1
    var kEy = 0                        // k.ey — eA[bv].length
    var kEd = 0                        // k.eD — banner ticker
    var kBv = 0                        // k.bv — banner index (K() arg)
    /** `eA[]` (k.java:8458, proven): menu item-id rows per `bv` — the
     *  game's whole menu corpus: 0 main, 1 pause, 2 difficulty,
     *  3 YES/NO dialog, 4 options, 5 heroes. Items are `bU[]` indices. */
    val kEA = arrayOf(
        intArrayOf(2, 1, 3, 32), intArrayOf(11, 12, 4, 6, 0, 8),
        intArrayOf(35, 36, 37), intArrayOf(14, 15),
        intArrayOf(83, 84, 123, 97, 5, 113, 7, 87),
        intArrayOf(106, 107, 108, 109))
    /** `k.cc` — mission medal flags, stamped into `bA[130+i]` on l(15). */
    val kCc = IntArray(3)
    /** `k.fP` — unlocked-mission list (save system unbuilt): all eight
     *  treated unlocked (`inferred`). */
    val kFp = intArrayOf(0, 2, 5, 7)  // k.fP (k.java:344) — chapter thresholds
    /** `k.bA` — save/snapshot buffer: [15]=checkpoint-exists flag (set in
     *  writeIX), [130..132]=cc medal stamps (k.java:2055-2064 proven). */
    val kBA = IntArray(160)
    var kAu = 0                        // k.au — medal-condition field
    var kDx = false                    // k.dx — cheat-enabled flag
    var kAo = false                    // k.ao — fade-side flag
    var kCU = 0                        // k.cU — l(4) stash
    var kFi = 0                        // k.fi
    var kFb: Any? = null               // k.fb — font measurer (unported)

    // -- menu machine (K()/L()/m()/Q() — k.java:6956/:7084/:5472/:3576) ----
    /** `dp[]/dq[]/dr[]/ds` — the O()/P() state stack (structured
     *  k.java:3561-3573, proven): O() pushes (j.c,bv,bw), P() pops → K(dq). */
    val kDp = IntArray(16); val kDq = IntArray(16); val kDr = IntArray(16)
    var kDs = 0
    var kEx = 0                        // k.ex — Q()'s caller state (menus)
    var kFF = 0                        // k.fF — delayed l() target
    var kFG = false                    // k.fG — wipe-confirm flag
    var kFH = 0                        // k.fH — row-anim phase
    var kFI = 0                        // k.fI — row-anim dir
    var kFE = 0                        // k.fE — fade alpha
    var kDa = 0                        // k.da — menu param (l(19) arg)
    var kDt = false                    // k.dt — difficulty-locked flag
    var kDB = 0                        // k.dB — lives byte (bA[44])
    var kDC = 0                        // k.dC — sync byte (bA[46])
    var kDD = 0                        // k.dD — progress (bA[32]/az)
    var kDF = 0                        // k.dF — misc byte (bA[48])
    var kBG = 0                        // k.bG — score flag (Q case14)
    var kEJ = false                    // k.eJ — tutorial done (bA[10])
    var kDM = false                    // k.dM — resume flag
    var kCm = 0                        // k.cm — control-style flag
                                     // (inferred distinct from mount cm)

    /** `bU[]` (k.java:5166-5175) — the global UI string table `d(0,n)`.
     *  Strings from `docs/gameplay-mining/string-corpus.md` (proven where
     *  listed); guesses marked `inferred`. */
    private val bU = mapOf(
        0 to "MAIN MENU", 1 to "NEW GAME", 2 to "CONTINUE",
        3 to "SELECT LEVEL", 4 to "OPTIONS", 5 to "HIGH SCORES",
        6 to "HELP", 7 to "ABOUT", 8 to "EXIT", 9 to "TOUCH THE SCREEN",
        10 to "LEVEL", 11 to "RESUME", 12 to "RESTART",
        13 to "ARE YOU SURE YOU WANT TO EXIT?",
        14 to "YES", 15 to "NO", 16 to "NEXT", 17 to "BACK", 18 to "SKIP",
        22 to "SOUND", 23 to "TOTAL", 24 to "LOADING",
        25 to "DO YOU WANT TO RESTART?",
        32 to "SLOT 1", 33 to "SLOT 2", 34 to "SLOT 3",
        35 to "EASY", 36 to "NORMAL", 37 to "HARD",
        47 to "TOUCH THE AREA TO THE ASSASSIN'S LEFT/RIGHT: MOVE\n\nTOUCH THE AREA ABOVE THE ASSASSIN: JUMP\n\nTOUCH THE AREA BELOW THE ASSASSIN: CROUCH\n\nTOUCH THE ASSASSIN: ATTACK/HOOK\n\nTOUCH THE WEAPON ICON: CHANGE WEAPON",
        48 to "FIND THE HEALTH POTION TO RECOVER LIFE.",
        49 to "THE GAME CAN ALSO BE PLAYED ENTIRELY WITH THE VIRTUAL PAD.\n\nCORRESPONDING CONTROLS\n\nTOUCH THE ASSASSIN = ATTACK ICON\nTOUCH THE AREA TO THE ASSASSIN'S LEFT = VIRTUAL PAD LEFT\nTOUCH THE AREA TO THE ASSASSIN'S RIGHT = VIRTUAL PAD RIGHT\nTOUCH THE AREA ABOVE THE ASSASSIN = VIRTUAL PAD UP OR JUMP ICON\nTOUCH THE AREA BELOW THE ASSASSIN = VIRTUAL PAD DOWN",
        50 to "\\^ACHIEVEMENTS\\^  \n\\0INCREDIBLE ASSASSIN:\\1 KILL 7 ENEMIES IN ONE LEVEL. \n\\0HARDCORE:\\1 COMPLETE ONE LEVEL IN HARD MODE. \n\\0BLOOD KILLER:\\1 KILL 28 ENEMIES IN LEVEL 2 IN HARD MODE.",
        98 to "COLLECT ENOUGH SOULS TO OBTAIN A LIFE EXTENSION.",

        38 to "ENEMIES KILLED", 39 to "SILENT KILLS", 40 to "RETRIES",
        41 to "SOULS", 42 to "TIME", 43 to "SCORE",
        56 to "MISSION FAILED. YOU DID NOT CATCH YOUR TARGET!",
        57 to "MISSION FAILED. THE GUARDS HAVE SOUNDED THE ALARM!",
        58 to "MISSION FAILED. YOU DIDN'T REACH THE ESCAPE LOCATION IN TIME!",
        59 to "MISSION FAILED", 60 to "MISSION COMPLETE",
        69 to "DO YOU WANT TO DELETE YOUR DATA?",
        71 to "DIFFICULTY", 72 to "IN-GAME SOUND?", 79 to "OK",
        73 to "DO YOU WANT TO QUIT?",
        83 to "MUSIC", 84 to "SFX", 87 to "RESET GAME",
        97 to "CONTROL", 103 to "PLAYER LIST",
        104 to "AC BROTHERHOOD", 105 to "PLAYER LIST",
        106 to "EZIO", 107 to "EXECUTIONER", 108 to "DOCTOR",
        109 to "NOBLEMAN", 113 to "ACHIEVEMENTS", 117 to "NEW GAME",
        121 to "THE GAME DATA HAS BEEN DELETED.", 123 to "CONTROL STYLE")
    /** `d(0,n)` = `bU[n]` (k.java:486, proven). */
    fun d0(n: Int): String? = bU[n]

    // -- audio (`e.a(n,false)`/`e.b()`, e.java:50-87; `z()` k.java:7363) ----
    /** `e.e` — current audio track index (-1 = silent, `e.b()` stop). */
    var audioTrack = -1
        private set
    /** `k.bE`/`k.bF` — the two audio-channel enables (settings-bound;
     *  default on): `z()` plays n<10 only when bE, n>=10 only when bF
     *  (e.java:50-60, proven). */
    var kBE = true
    var kBF = true
    /** `k.ee[]` — per-mission music table (k.java:8436, proven):
     *  `B()` plays `ee[aj]` (or track 9 when `aJ==1`). */
    val kEE = intArrayOf(5, 2, 3, 3, 2, 4, 5, 1)
    /** Deferred audio commands — drained by the game loop each tick
     *  (same pattern as SpikeWorld's Command queue; real samples for
     *  the 34 tracks are not decoded into the app — adapters log). */
    private val pendingCommands = ArrayDeque<Command>()
    fun drainCommands(): List<Command> {
        val out = pendingCommands.toList(); pendingCommands.clear(); return out
    }
    /** `z(int)` (k.java:7363, proven): `n∉[0,34) → nop`, then `e.a(n,false)`
     *  — play iff `a[n]!=null` (assumed available, inferred) &&
     *  `(kBE || n>=10)` && `(kBF || n<10)`. Sets loop count 1 (verbatim). */
    private fun z(n: Int) {
        if (n < 0 || n >= 34) return
        if (!kBE && n < 10) return
        if (!kBF && n >= 10) return
        audioTrack = n
        pendingCommands += Command.PlaySfx(n)
    }
    override var iBn = false                     // i.bn — bA[79] alert flag
    override var kAJ = 0                         // k.aJ — ax42 fuse phase
    override var kAK = 0                         // k.aK — countdown init
    override var kAM = 0                         // k.aM — fuse accumulator
    override var kF: Entity? = null              // k.F
    override var iCe = false                     // i.ce static
    override var iBD = false                     // i.bD static
    override var iBB = false                     // i.bB static (revive arm)
    override var iBC = false                     // i.bC static
    override var iBE = 0                         // i.bE static
    override var iBF = 0                         // i.bF static
    override var iBG = -1                        // i.bG static
    override var iCF = false                     // i.cF static
    override var iBQ = 0                         // i.bQ static
    override var iCO: Entity? = null             // i.cO mount-align link
    override var iCg: Entity? = null             // i.cg
    override var iCh: Entity? = null             // i.ch
    override var iZ = false                      // i.z static (sub-op 24/25)
    override fun kStat(n: Int) {                 // k.o(n) (k.java:4304)
        if (n != 3 || kAj != 7) kAp[n]++
    }
    override fun kStatE(gate: Int) {             // k.e(0,gate) (k.java:4314)
        if (gate > 0 && kAj != 7) kAp[0]++
    }
    override fun spawnStatic(ax: Int, s: Int, x: Int, y: Int): Entity? {
        // i.a(ax,s,5,400) marker arm — arg mapping `inferred`; marker
        // entities are invisible logic nodes so a null clip is fine.
        val e = Entity(ax, null)
        e.S = s
        e.ak = x; e.al = y
        return e
    }
    override var kAD: Entity? = null           // k.aD — HUD fuse entity
    override var kAO = 0                       // k.aO — message countdown
    override var kAP: String? = null           // k.aP — HUD message text
    override fun levelString(level: Int, idx: Int): String? =
        levelStrings.getOrNull(idx)
    override var kT: Int get() = boundMinY; set(v) { boundMinY = v }
    override var kU: Int get() = boundMaxY; set(v) { boundMaxY = v }
    override var kAm = false                   // k.am
    override var kDd = false                   // k.dd
    override var gR = false                    // g.r — grab-QTE lock
    override var kBj = 0                       // k.bJ — grab-QTE lose latch
    /** `k.J`/`k.K` — the held touch point; port aliases the last DOWN
     *  point `lastTouchX/Y` (inferred — J2ME tracks them separately). */
    override var kJ: Int get() = lastMoveX; set(v) { lastMoveX = v }
    override var kK: Int get() = lastMoveY; set(v) { lastMoveY = v }
    override var kAn = false                   // k.an fade flag
    /** `k.aQ` — ax35 vol-paint recorder (debug `Image` in the original;
     *  ported as the last-painted rect, `inferred`). */
    override var volPaintRect: IntArray? = null
    override var bubbleDraw: BubbleDraw? = null      // ad() draw channel
    /** `k.a(k.y, text, 120)` (inferred): greedy word wrap at ~6px/char
     *  (20 chars/line); returns the line table — [0] = line count,
     *  [1..n] = per-line start char offsets. */
    override fun wrapDialogText(text: String, widthPx: Int): IntArray {
        val per = (widthPx / 6).coerceAtLeast(1)
        val starts = ArrayList<Int>(); var pos = 0; var lines = 0
        while (pos < text.length) {
            starts += pos; lines++
            val end = minOf(pos + per, text.length)
            pos = if (end < text.length) {
                val sp = text.lastIndexOf(' ', end - 1)
                if (sp > pos) sp + 1 else end
            } else end
        }
        return intArrayOf(lines) + starts.toIntArray()
    }
    /** `k.ac` — the same camera view rect as `camRect` (aliased;
     *  ax35's off-screen containment test reads it via this name). */
    override val kAc: IntArray? get() = camRect
    override var kAz = 0                         // k.az — collect streak
    override var kAq = 0                         // k.aq — wisp counter
    /** `k.o(int)` (k.java:4304): `ap[slot]++`; slot 3 skipped when aj==7. */
    override fun kCount(slot: Int) {
        if (slot != 3 || kAj != 7) kAp[slot]++
    }
    /** `k.s()` (k.java:5338 + `dE` at :8403, proven). */
    override fun kCollectStreak() {
        kAz++
        if (kAx < 105 && kAx < 30) kAx = 30      // meter floor clamp
        val dE = intArrayOf(0, 100, 200, 400, 600, 800)
        var tier = dE.size - 1
        while (tier > 0 && kAz < dE[tier]) tier--
        if (tier == 0) return                    // streak < 100 → nothing
        val old = kAx
        if (kAx > 105) return                    // L28
        kAx = 30 + tier * 15
        player.x1 = minOf(player.x1, kAx)        // g.f(ax)
        if (old < kAx) player.x1 = kAx           // g.e(ax)
    }
    override var iBi = false                     // i.bi — ax64 grab hitlag
    override val gS = false                      // g.s — cutscene (no producer)
    /** `k.aX` pooled-shot slots (k.java:8423 `aW=50`, proven) — lazily
     *  grown to 50 `new i()`-blank slots (ax=0, clipless — the ax64
     *  tether/barrage spawn config never touches ax/aa; `inferred`); the
     *  spawner's `P &= -129` un-reserves the slot → `P&128` = free. */
    val shotPool = ArrayList<Entity>()
    override fun allocShot(): Entity? {
        if (shotPool.size < 50) shotPool += Entity(0, null).apply { P = P or 128 }
        return shotPool.firstOrNull { (it.P and 128) != 0 }
    }
    override fun tickShotPool() {
        for (s in shotPool) {
            if ((s.P and 128) != 0) continue
            s.aC--
            if (s.aC < 0) { s.P = s.P or 128; continue }   // slot freed
            s.am += s.ag; s.an += s.ah
            s.ah += kY                                     // k.Y bias (0 today)
            s.N = s.am; s.O = s.an
            s.ak = s.am shr 8; s.al = s.an shr 8
            s.refreshBoxes()
        }
    }
    override fun padHeld(mask: Int): Boolean = pad.v(mask)
    override fun padDown(mask: Int): Boolean = pad.u(mask)
    override fun padTap(mask: Int): Boolean = pad.x(mask)           // k.x
    override fun padRelease(mask: Int): Boolean = pad.w(mask)       // k.w
    override fun clearLatches() { pad.clearLatches() }   // k.v()
    override fun padRearm() { pad.edge = pad.held }      // k.v = k.w
    override var kCO = 0                               // k.cO transition count
    override var kCP = false                           // k.cP direction
    override var bO = 0                                // k.bO — dialog flag
    override var bN0 = -1                              // k.bN[0] — dialog idx
    /** `k.b(idx,str,flag)` (k.java:430, head proven): stores `bO`/`bN[0]`
     *  then `b(9, 1+aj, str, str)` — the dialog-display call; `inferred`
     *  accept-return (display pipeline unported). */
    override fun kDialog(idx: Int, strRef: Int, flag: Int): Boolean {
        bO = flag
        bN0 = if (idx > 0) idx else -1
        dialogLine = strRef
        return true
    }
    var dialogLine = -1                                // last b(9,·) str arg
    override fun pointerDownIn(x: Int, y: Int, w: Int, h: Int): Boolean =
        lastTouchX >= x && lastTouchY >= y &&
            lastTouchX <= x + w && lastTouchY <= y + h &&
            (lastTouchX != -1 || lastTouchY != -1)
    override fun pointerMoveIn(x: Int, y: Int, w: Int, h: Int): Boolean =
        lastMoveX >= x && lastMoveY >= y &&
            lastMoveX <= x + w && lastMoveY <= y + h &&
            (lastMoveX != -1 || lastMoveY != -1)
    /** `k.j()` (k.java:579, proven): inside the bottom strip when
     *  `H∈[36,364] && I∈(204,240)`; otherwise true iff `I∈[0,204]`.
     *  (`ce/cf/cg` margins inferred at 36 — the `b.d+30` variant unmined.) */
    override fun pointerStrip(): Boolean {
        val hx = lastTouchX; val hy = lastTouchY
        if (hx == -1 && hy == -1) return false
        return if (hx < 36 || hx > 364 || hy <= 204 || hy >= 240)
            hy in 0..204
        else true
    }
    /** `i.a(8,59,S,facing,x,y,az)` (i.java:6898, proven) — op111's
     *  boss-knife spawn: ax8 param entity, clip 59, `P|=512`. */
    override fun spawnParam(s: Int, facing: Boolean, x: Int, y: Int,
                            az: Int): Entity? {
        val e = Entity(8, clips[59])
        e.aw = -1; e.au = 0
        e.az = az
        e.ak = x; e.al = y
        e.av = facing
        e.P = e.P or 512
        e.setAnim(s)
        queueInsert(e)
        return e
    }
    /** `k.n()` (k.java:2861, proven): `ah=null; R=S=T=U=0`. */
    override fun kN() {
        kAh = null; kR = 0; kT = 0; kSBound = 0; kU = 0
    }

    /** `k.l(int,int)` (k.java:2844, proven): `clamp(d/2, -k, k)` —
     *  the per-axis camera lerp step. */
    private fun lerpStep(d: Int, k: Int): Int {
        val h = d / 2
        return if (h > k) k else if (h < -k) -k else h
    }

    /** `k.m(int)` (k.java:2346-2703, proven): the per-frame camera
     *  tracker — focus entity `ae` (`kAe`), look-ahead margin `cM`,
     *  scroll-wall `ah` (`kAh`), bound walls `R/S/T/U`, shake `cO/cP`.
     *  `r5 & ad` (kAd = 2) snaps the camera back to the player and
     *  clears walls — the dialog-release / level-init path. */
    override fun kM(r5: Int) {
        if (kAh == null) kN()                                       // head
        val p = player
        if ((r5 and kAd) != 0) {                                    // snap arm
            p.refreshBoxes()                                        // aS.t()
            kAe = p; Entity.aL = null                               // ae=aS; i.aL=null
            kN()                                                    // n()
            camCF = 0; camCE = 0; camM = 200
        }
        if (kAi) return                                             // L9: frozen
        val ae = kAe
        if (!kZ) {
            if (ae === p) {
                if (p.aSC()) return                                 // aS.c() combo freeze
                if (p.S !in Entity.GRABBABLE_STATES &&
                    p.S != 101 && p.S != 92 && p.S != 11) {         // L19-L41
                    if (p.av && p.ag < 0) camM = minOf(camM + 20, 266)
                    if (!p.av && p.ag > 0) camM = maxOf(camM - 20, 133)
                }
                // ---- cA target (L41-L132) ----
                val gg = p.gg; val ga = p.ga
                var cAdone = false
                if (gg != null && (ga == null || ga.ax != 43)) {    // L46 midpoint
                    camA = p.ak + ((gg.ak - p.ak) shr 1) - 200
                    cAdone = true
                }
                if (!cAdone) {
                    val at = Entity.at
                    if (at != null && at.ax == 72) {                // L48 i.at rope mid
                        camA = p.ak + ((at.ak - p.ak) shr 1) - 200
                    } else if (gc != null && gc!!.ax == 43) {       // L61 → target rope
                        camA = gc!!.ak - 200
                    } else if (ga != null && ga.ax == 43) {         // L64 → own rope
                        camA = ga.ak - 200
                    } else if (p.S in CAM_CENTER_STATES || gj ||
                        (ga != null && ga.ax == 51) ||
                        (p.S == 38 && p.ac != null && p.ac!!.ax == 22)) {
                        camA = p.ak - 200                           // L116 centered
                    } else {
                        if (p.S == 317) {                           // L120 chase-cam
                            if (p.av && p.ag < 0) camM = 300
                            if (!p.av && p.ag > 0) camM = 100
                        }
                        camA = p.ak - camM                          // L131/L132
                    }
                }
                // ---- cB target (L134-L201) ----
                val ropeAttached = ga != null && ga.ax == 43
                var l149 = false
                if (p.aZ || ropeAttached) {                         // L134→L140
                    if (p.S == 203 || p.S == 204 || p.S == 62) {
                        if ((r5 and kAd) != 0) camB = p.al - 150
                        else l149 = true
                    } else camB = p.al - 150                        // L147
                } else if ((r5 and kAd) != 0) {                     // L146→L147
                    camB = p.al - 150
                } else l149 = true                                  // → L149
                if (l149) {                                         // L149-L201
                    if (p.S in CAM_B_DOWN_STATES) camB = p.al + 60  // L158 hang-look
                    else if (p.S in CAM_B_CENTER_STATES || gj ||
                        (p.S == 38 && p.ac != null && p.ac!!.ax == 22))
                        camB = p.al - 120                           // L199
                    // else: camB keeps its last value (verbatim sticky)
                }
                // L204-L207: keep the focus box 40px inside view
                if (p.W[1] < camB + 40) camB = p.W[1] - 40
                if (p.W[3] > camB + 240 - 40) camB = p.W[3] + 40 - 240
                // L214-L217: lookahead offsets
                if (camAf != 0) camA = p.ak - 200 + camAf
                if (camAg != 0) camB = p.al - 120 + camAg
            } else if (ae != null) {                                // L220 ae!=aS
                if (ae.ax == 43 && (ae.S == 1 || ae.S == 4)) {      // L224-L278
                    val z1 = ae.Z[1]
                    val dx = ae.ak - camX
                    val inView = Entity.overlapI(p.Y, camRect)      // i.b(aS.Y, ac)
                    if (ae.av) {                                    // L228 arm
                        if (!inView)                                // L251
                            camCC = if (ae.Y[2] <= camRect[2]) z1 * 150 / 100
                                    else z1 * 50 / 100
                        else if (dx < 200) camCC = z1 * 150 / 100   // L237
                        else if (dx < 300 && ae.ag == (z1 shl 8))
                            camCC = z1 * 150 / 100
                        else if (dx in 300..349 && ae.ag == (z1 shl 8))
                            camCC = z1                              // L239
                        else if (dx >= 350 &&
                            kotlin.math.abs(ae.ag) >=
                                kotlin.math.abs(z1 shl 8) * 50 / 100)
                            camCC = z1 * 50 / 100                   // L246
                        // else camCC sticky (verbatim L246→L280)
                    } else {                                        // L255 mirror
                        if (!inView)                                // L276
                            camCC = if (ae.Y[0] >= camRect[0]) z1 * 150 / 100
                                    else z1 * 50 / 100
                        else if (dx <= 50 && ae.ag <= (z1 shl 8))
                            camCC = z1 * 50 / 100                   // L271
                        else if (dx <= 100 && ae.ag == (z1 shl 8))
                            camCC = z1                              // L264
                        else if (dx <= 200) camCC = z1 * 150 / 100  // L262
                        else if (ae.ag == (z1 shl 8)) camCC = z1 * 150 / 100
                    }
                    camB = ae.al - 120                              // L280
                } else {                                            // L279
                    camA = ae.ak - 200
                    camB = ae.al - 120                              // L280
                }
            }
            // L282-L297: scroll-wall containment
            val wall = kAh
            if (wall != null && wall.W != null && wall.aF == 1) {
                if (camA < wall.W[0]) camA = wall.W[0]
                if (camA + 400 > wall.W[2]) camA = wall.W[2] - 400
                if (camB < wall.W[1]) camB = wall.W[1]
                if (camB + 240 > wall.W[3]) camB = wall.W[3] - 240
            }
            // L300-L305: focus-N watch → X lerp cap 20..40 (private
            // camXw — the original reuses k.X; see field note)
            if (ae != null) {
                if (camCI != ae.N) { if (camXw < 40) camXw++ } else camXw = 20
                camCI = ae.N
            }
            // L311-L325: R/S/T/U bound walls (>0 = armed)
            if (kR > 0 && camA < kR) camA = kR
            if (kSBound > 0 && camA > kSBound - 400) camA = kSBound - 400
            if (kT > 0 && camB < kT) camB = kT
            if (kU > 0 && camB > kU - 240) camB = kU - 240
        }
        // ---- L325+ settle ----
        val r6 = if (iAH && iAI > 0) iAI else 1
        if ((r5 and kAd) != 0) {                                    // L331 snap
            camX = camA; camY = camB; camCC = 0; camCD = 0
            kU = 0; kSBound = 0; kT = 0; kR = 0                     // snap clears walls
        } else if (ae != null) {
            if (ae.ax == 43 && ae.S != 1) {                         // L339: speed-follow
                camX += camCC / r6
                camCD = lerpStep(camB - camY, 28)
                camY += camCD / r6
            } else {                                                // L340: both lerp
                camCC = lerpStep(camA - camX, camXw)
                camCD = lerpStep(camB - camY, 28)
                camX += camCC / r6; camY += camCD / r6
            }
            // L342-L357: ab / rope cd[3] snap-x override (clears kAb too)
            val gcx = gc; val ga2 = p.ga
            if (kAb || (gcx != null && gcx.ax == 43 && gcx.cd[3]) ||
                (ga2 != null && ga2.ax == 43 && ga2.cd != null && ga2.cd[3])) {
                kAb = false; camX = camA
            }
        }
        // L359: g.v full warp
        if (gV && ae != null) { camX = ae.ak - 200; camY = ae.al - 120 }
        // L362-L371: world-edge floor
        if (camX < 0) camX = 0
        if (camX > level.worldW - 400) camX = level.worldW - 400
        if (camY < 0) camY = 0
        if (camY > level.worldH - 240) camY = level.worldH - 240
        // L374-L378: cO alternating decay shake (cP read is a
        // decompile artifact — the original leaves it unused)
        if (kCO > 0) {
            camY += if ((kCO and 1) == 0) kCO else -kCO
            kCO--
        }
        // L380-L388: av latch → r() once the camera settles on the
        // ax10 end-trigger (i.ai(): ae.ax==10 && ae.S==52)
        if (kAv && camCC == 0 && camCD == 0 && ae != null &&
            ae.ax == 10 && ae.S == 52) {
            kAw = 0; kAv = false; kDz = 120                         // k.r()
        }
    }
    /** `i.X()` (i.java:18631, proven): writes the checkpoint slot into
     *  `bA` — aw/pos/facing, g.J/g.I, ap[0,3,2/16,4] + ap[5] at 52+aj*2,
     *  ax/ay/az/aN/aL (unmodeled — mission-script globals), aZ/bn flags,
     *  br[] dead set, then re-stamps `k.a(bb[i], bb[i].as)` on every
     *  non-consumed entity (skipped: as==-98 corpses, ax==70). */
    private fun writeIX(aw: Int): Snapshot {
        kBA[15] = 1                          // bA[15]=1 — checkpoint-exists
                                             // flag read by l(15)'s r82
                                             // (i.java:2077, proven)
        return Snapshot(aw, player.ak, player.al, player.av, player.x1,
                        player.gJ, player.gI, kAp.copyOf())
    }

    /**
     * `k.D()` (k.java:2721-2860, proven): the bh[aj]==3 autoscroll camera
     *  that REPLACES m(1) on the flying/chase missions — swept cell-22
     *  corridor walls → R/S, director `k.ai` corridor via cG/cH cache or
     *  the row scan, wind `X` drained from `W` once (`Y=X<<8` = the
     *  `ae.ah` clamp via the derived `kY` getter), `Q` keeps the player
     *  117..230px above camB, then `O+=l(cA-O,4); P+=l(cB-P,30)`.
     *  Dead code on level 0 (bh=4); reachable via tests.
     */
    private fun kD() {
        val c = kC                                                       // L7-L12
        if (c != null && (c.cd[0] || c.claimActive()) && kZ) {
            camA = camX; camB = camY                                     // snap
            return
        }
        if (dialogModal) { camA = camX; camB = camY; return }            // j.c==21
        val ae = player                                                  // ae=aS
        if (kW != 0) { kX = kW; kW = 0 }                                 // L17 wind
        // kY = kX << 8 — the derived getter (k.java:2737)
        var r6 = ae.W[0] / 20                                            // L18-L21
        var r7 = ae.W[2] / 20
        val r02 = ae.al / 20
        val r03 = level.worldW / 20                                      // br/20
        if (kAi) {                                                       // L20
            if (boundMinX == -1) {
                boundMaxX = -1
                if (iBV > 0) {                                           // L22-L23
                    boundMinX = camCG; boundMaxX = camCH
                } else {
                    // L24-L36: linear row scan — first 22 opens the
                    // corridor, later 22s narrow it to fit the 400px
                    // view (cache cG/cH).
                    var r92 = 0
                    while (r92 < r03) {
                        if (level.collisionCell(r92, r02) == 22) {
                            if (boundMinX == -1) boundMinX = r92 * 20
                            else {
                                var s = (r92 + 1) * 20
                                if (s - 400 < boundMinX) {
                                    boundMinX -=
                                        (boundMinX - (s - 400)) shr 1
                                    s = boundMinX + 400
                                }
                                boundMaxX = s
                                camCG = boundMinX; camCH = boundMaxX     // L35
                            }
                        }
                        r92++
                    }
                }
            }
        } else {
            // L37-L56: sweep left/right from the player until cell 22.
            var r9 = 0; var r10 = 0
            var leftDone = false; var rightDone = false
            while (!(leftDone && rightDone)) {
                if (r6 <= 0 || r9 == 22) leftDone = true
                else { r6--; r9 = level.collisionCell(r6, r02) }
                if (r7 >= r03 || r10 == 22) rightDone = true
                else { r7++; r10 = level.collisionCell(r7, r02) }
            }
            boundMinX = r6 * 20                                          // L56
            boundMaxX = ((r7 + 1) * 20) - 400
            if (boundMinX > boundMaxX) boundMinX = boundMaxX             // L58
            camCN = ae.ak                                                // L59
        }
        // L61-L70: cA — corridor-center for k.ai else player-relative
        if (kAi) camA = ((boundMinX + boundMaxX) shr 1) - 200
        else if (!iBe) {                                                 // L64
            camA = camCN - 200
            if (camA < boundMinX) camA = boundMinX
            else if (camA > boundMaxX) camA = boundMaxX
        }
        camB += kX                                                       // L71 wind
        val q = ae.al - camB                                             // L72
        if (!iBj) {                                                      // L74-L82
            if (q <= 117) {                                              // L74
                // Q=117 writeback is into k.Q — display-only, unmodeled
                if (ae.ah < kY) ae.ah = kY
            } else if (q >= 230) {                                       // L79
                if (ae.ah > kY) ae.ah = kY
            }
        }
        camX += lerpStep(camA - camX, 4)                                 // L83-L86
        camY += lerpStep(camB - camY, 30)
        // ac[] = {camX, camY, +400, +240} — the camRect getter derives it
    }

    /**
     * `i.w()` (i.java:793-816, proven) — the goal entity's camera-band
     *  query for the L142 win check: 0 = `v()` true (on-screen/active),
     *  1 = anchor inside the (ac2, ac2+200) band past the camera right
     *  edge while offscreen, 2 = anchor >200px beyond it (win), 3 =
     *  anchor at/behind the right edge. Both interior `v()` re-evals are
     *  verbatim — `v()` is pure, so `return 1`/`return 2` hinge on a
     *  `wasHitRecently` re-eval flipping mid-check (decompiler artifact;
     *  kept for fidelity).
     */
    private fun iW(e: Entity): Int {
        if (e.wasHitRecently(this)) return 0                           // L5
        val ac2 = camRect[2]                                           // k.ac[2]=O+400
        if (e.ak > ac2 && e.ak < ac2 + 200) {                          // in band
            return if (e.wasHitRecently(this)) 3 else 1                // L7 tail
        }
        if (e.ak > ac2 + 200)                                          // L15→L17
            return if (e.wasHitRecently(this)) 3 else 2                // L22 / 2
        return 3
    }

    /**
     * `k.I()`'s L142-L200 tail (k.java:3321-3358, proven): runs right
     *  after the per-tick camera call on BOTH bh arms (m(1) falls
     *  through; D() gotos it).
     *  1. Goal arm — `k.aV` (the `r8[5]==0` ax9 block `initAx9` bound)
     *     while `Z[0]==1` (script ops L146/L147 arm/disarm) and
     *     `aV.S∉{4,5}`: `w()==1` → the `j.f`-even milestone font blit
     *     (`z[9].a(cd,38,0,360,120,…)` — draw unported; `goalTicker`
     *     flag only, inferred); `w()==2` → `bx=56; l(13); bw=0` —
     *     the scripted win. The `j.c∈{13,31}` skip maps to `won`;
     *     screen 31 has no analog yet (inferred).
     *  2. Claimer step — `C.cd[2] && C.cd[1] && C.ab()` → `C.aa()`:
     *     one claim-script step per tick on the fast-forwarded claimer
     *     (L161-L167).
     *  3. bh3 tail — `cA=O; cB=P` (L172): targets snap to the lerped
     *     pos so an early-returning D() doesn't drift.
     */
    private fun l142Tail() {
        val aV = kAV
        if (aV != null && aV.Z[0] == 1 && !won && aV.S != 4 && aV.S != 5) {
            when (iW(aV)) {
                1 -> if (tickIndex and 1L == 0L) goalTicker = true     // L157
                2 -> { kBx = 56; screenL(13); kBw = 0 }                // L159
                else -> goalTicker = false
            }
        } else goalTicker = false
        val c = kC                                                     // L161-L167
        if (c != null && c.cd[2] && c.cd[1] && c.claimActive()) {
            c.runClaimScript(this)
        }
        if (Entity.MISSION_BH[kAj] == 3) { camA = camX; camB = camY }  // L172
    }

    /** L157's `z[9]` milestone blit active this tick (render unported). */
    var goalTicker = false
        private set

    /** `k.l(int)` — the screen-state machine (k.java:2031-2300 simple,
     *  structured :1637): remaps the request, runs the entry side-effects
     *  (unported render/audio arms are labeled stubs), then commits
     *  `cy=j.c; j.c=i` plus the `al` world-freeze flag. `i=22` re-enters
     *  the loop once (medal screen after a stamp). */
    override fun screenL(n: Int) = stateL(n)
    fun stateL(iArg: Int) {
        var i = iArg
        while (true) {                               // L2 — re-entry for i=22 only
            kEg = 0; val ex = jC; kCZ = 0; kCb = true; kCu = 0; kFd = -1; kFe = 0
            jG = 0                                   // j.g=0 (k.java:2047)
            if (i == 27) audioStop()                 // e.b() — audio stop (unported)
            when {
                i == 9 -> {                          // L7: renderer teardown
                    kFo = 0                          // ac(); ad(); e.b(); L() unported
                }
                i == 12 || i == 13 -> {              // L12 → L17 tail
                    scrollBounds()                   // b(true) — scroll refresh (unported)
                    kAD = null
                    if (i == 12 && ex != 12) { deaths++; kAp[1]++ }
                    if (i == 13 && kBx >= 0) i = 31  // win → stats screen (proven)
                    kEc = 25; bannerK(3); kEb = 59   // L17 (simple decompile —
                                                     // structured omits; high-confidence)
                    z(7)                             // fail/win sting
                }
                i == 15 -> {                         // mission-complete stats
                    kEe = 0; kEf = 37                // j.g=0 — derived counter, no-op
                    if (ex != 10 && ex != 22) { audioStop(); z(6) }
                    // L26-L61 (proven): medal stamps → `bA[130+i]`
                    if (kAp[0] >= 7 && kCc[0] == 0) kCc[0] = 1           // L35
                    if (kAu == 2 && kCc[1] == 0) kCc[1] = 1              // L40
                    if (kAp[0] > 1 && kAu == 2 && kAj == 1 &&
                        kAp[0] >= 28 && kCc[2] == 0) kCc[2] = 1          // L45
                    for (s in 0 until 3) kBA[130 + s] = kCc[s]           // L55
                    // L56-L64: any stamped medal → medal screen re-entry
                    if ((0 until 3).any { kCc[it] == 1 }) { i = 22; continue }
                    // L68-L77: next-mission redirect (proven)
                    val nextUnlocked = (kAj + 1) in kFp                  // r72
                    val hasCheckpoint = kBA[15] == 1                     // r82
                    if (nextUnlocked && !hasCheckpoint && ex != 10) i = 10
                }
                (i == 8 || i == 21) && jC == 9 -> missionInit()          // B() — unported
                i == 8 && kCy == 17 -> i = 17
                i == 29 -> bannerK(2)
                i == 30 -> { bannerK(5); kFo = 0 }
                i == 4 -> kCU = kAu
                i == 28 -> bannerK(3)
                i == 6 -> kFd = if (kDx) 240 else 60
                i == 2 -> {                            // quit arm
                    kFo = 0                            // E() unported
                    bannerK(0); kDx = false
                    // switch(j.c): non-exempt states run `e.b(); z(0)`
                    if (jC !in intArrayOf(2, 3, 4, 5, 6, 18, 19, 20, 22, 28, 29, 30)) { audioStop(); z(0) }
                }
                i == 14 -> {
                    if (jC == 8 || jC == 21) scrollBounds()
                    bannerK(1); kAo = false; kAn = false
                    kFi = -1                           // `if (!e.a())` — unported (inferred)
                    audioStop()
                }
                i == 23 -> { kEc = 19; bannerK(3); kEb = 70; kBw = -1 }
                i == 5 -> {                                          // (:1806)
                    kEe = 0                                          // eE/eF shared
                    for (i5 in 0 until 4) {                            //   with the
                        kCV[i5] = d0(i5 + 47)                          //   stats procs
                        if (i5 == 1) {
                            kCW = footerFont?.linesHeight(wrapPage(kCV[i5], 261)[0]) ?: 0
                            kCV[i5] = "\n\n" + kCV[i5] + "\n\n\n" + d0(98)
                        }
                        val sLines = wrapPage(kCV[i5], 261)[0]
                        val iK = footerFont?.linesHeight(sLines) ?: 0
                        if (iK > kEe) kEe = iK
                        kCZ += (sLines + 7) / 8                      // ((s+8)-1)/8
                        kCX[i5] = (sLines + 7) / 8
                    }
                    kEe = footerFont?.linesHeight(11) ?: 0
                    kEf = 37 + kEe
                }
                i == 20 -> { /* kFb = y.a(d(0,27),390) — unported */ }
            }
            break
        }
        // L151-L176 tail (proven): `al` freeze flag, u==8 dialog `cz`,
        // commit `cy=j.c; j.c=i`, `v()` input reset unless cz.
        kAl = i == 13 || i == 12 || i == 17 || i == 16 || i == 31 ||
              (i == 21 && kMode != 8 && kMode != 9)
        if (i == 21 && kMode == 8) kCz = true
        kCy = jC; jC = i                              // j.g=0 skipped (derived counter)
        if (!kCz) inputReset()
        kCz = false
        // aggregate flag keyed on the ENTRY state — l(15) may redirect to
        // i=22/10 (medal screen / level select) before this tail runs.
        if (iArg == 15 || iArg == 31 || iArg == 13) missionWon = true
    }

    /** `M()` (k.java:3280-3445, proven) — the j.c==15 win-stats frame
     *  proc. `j.g==1`: `W();ac();ad();E()` teardown + best-time persist.
     *  Panel grows `eE→140` (`eF=37+eE`) then `j.g=1`. Rows reveal by
     *  `j.g` gates; `i4` score = base(aj)+kills·dh[au]+collects·di[au]
     *  −min(deaths,4)·300 +bonus·30 −overtime. `v(458784)` skips the
     *  reveal then persists `dB..dD`+`bA` slots and routes onward. */
    private fun winStatsM() {
        if (jG == 1L) {                                   // first proc frame
            teardown()                                    // W()
            // ac(); ad(); E() — renderer teardowns (unported)
            val bi = 52 + (kAj shl 1)
            if (kAp[5] > kBA[bi]) kBA[bi] = kAp[5]        // best time
        }
        statsTitleY = 25 + ((177 - kEf) / 2)              // a(i2,d(0,60))
        if (kEe < 140) {                                  // panel grow
            kEe += 10; kEf = 37 + kEe
            if (kEe >= 140) { kEe = 140; kEf = 37 + kEe; jG = 1 }
            return
        }
        var i4 = 0
        val i5 = kAp[0]
        if (kAj == 7) i4 = 3000 else if (kAj >= 8) i4 = 5000
        if (jG > 0) { statsRowText[0] = i5.toString(); i4 += i5 * kDH[kAu] }
        if (jG > 2) {
            statsRowText[1] = kAp[3].toString()
            i4 += kAp[3] * kDI[kAu]
        }
        if (jG > 4) {                                   // row shows raw i7
            statsRowText[2] = kAp[1].toString()
            i4 -= minOf(kAp[1], 4) * 300
        }
        if (jG > 6) {
            var i = if (bh3) kAp[4] else kAp[5]           // bonus
            if (i < 0) i = 0
            statsRowText[3] = fmtJ(i)
            i4 += i * 30
        }
        if (jG > 8) {
            val i8 = kDg / 16
            statsTimeSec = i8
            // verbatim mm:ss — i9=i8%60, abs, zero-pad <10
            statsRowText[4] = "%d:%02d".format(i8 / 60, Math.abs(i8 % 60))
            if (i8 > 180) i4 -= minOf(1000, (i8 - 180) shl 1)
        }
        if (i4 < 0) i4 = 0
        statsScore = i4
        if (jG > 10) { statsScoreVisible = true; kCb = false }
        // a(d(0,16), d(0,62)|"") — typewriter next-mission line
        statsTypeNext = if (kAj < 7) 62 else -1
        typewriterStep(if (kAj < 7) "next-mission" else "")
        if (pad.v(458784)) {                              // fire/advance
            z(23)
            if (jG <= 10) { jG = 10; return }
            // persist: best score + dB..dF stash + bA slots
            val si = 81 + (kAu shl 4) + (kAj shl 1)
            if (i4 > kBA[si]) kBA[si] = i4
            kDB = kAx; kDC = kAy; kDF = kAN; kDD = kAz
            kBA[32] = kAz; kBA[8] = kAu; kBA[44] = kAx
            kBA[46] = kAy; kBA[48] = kAN; kBA[36] = 0
            if (pad.v(327712)) {                          // confirm
                if (kAj < 7) {
                    kAj++
                    if (kBA[14] < kAj) kBA[14] = kAj
                    if (kEgFlags[kAj]) stateL(30) else stateL(2)
                } else {                                  // finale
                    kAj = 0; kBA[14] = 0; kBA[15] = 1
                    kBw = 0; teardown(); kDz = 0; stateL(24)
                }
            } else if (pad.v(Pad.M_CYCLE) && kAj < 7) {   // skip
                kAj++
                if (kBA[14] < kAj) kBA[14] = kAj
                stateL(2)
            }
            saveFlush()                                   // e(true) RMS
            if (jC == 2) bannerK(0)
        }
    }

    /** `a(b,str)` typewriter tail (k.java:3447-3470, proven shape):
     *  `dk` counts down; while `dk<=0` either inserts one char at `dj`
     *  (the \0\2 markers are font markup — unported) or resets
     *  `dj=0;dk=15` once `dj` reaches the end — a looping retype. */
    private fun typewriterStep(s: String) {
        while (kDk <= 0) {
            if (kDj < s.length) { typewriterText = s; kDj++; return }
            kDj = 0; kDk = 15
        }
        kDk--
        typewriterText = s
    }

    /** `K(int)` (k.java:6956, proven head) — banner-queue setup:
     *  `bw=-1; bv=n; ey=eA[n].length; eD=0`. Per-n row content and K(0)'s
     *  `Y()/Z()` checks are unmined (`unknown`). */
    private fun bannerK(i: Int) {
        kBw = -1; kBv = i; kEy = kEA[i].size; kEd = 0
        when (i) {
            0 -> {
                kEb = 0
                kEA[0][0] = if (menuHasSave()) 2 else 117   // Y(): CONTINUE?
                if (!menuShopCheck()) kEy--                  // Z(): no shop → no row 3
            }
            1 -> kEb = 72
            2 -> { kEb = 71; kBw = -1; if (kBA[69] == 0) kEy-- }
            3 -> {
                kEb = -1
                if (kEc != -1) kEd = menuTextHeight(d0(kEc))
                // orig: `bx != -1` adds more eD via eF-area math — folded
                // into the panel tail (inferred measure, flagged)
            }
            4 -> kEb = 4
            5 -> { if (jC != 14) { if (!menuHasSave()) kEy-- } else kEy -= 5 }
        }
        kEe = (when (kBv) {
            0 -> 312
            1 -> kEy * 28 + 6
            3 -> kEy * 36 + 6
            else -> kEy * 30 + 2        // bv ∈ {2,4,5} — orig's chain
                                        // keeps a bv==2/0 branch but the
                                        // decompile reaches this last arm
        }) + kEd
        kEf = 37 + kEe
    }

    /** `Y()` = `bA[15]==1 || bA[14]>0` (structured :5465, proven) —
     *  "has save progress" → eA[0][0] shows CONTINUE. */
    private fun menuHasSave() = kBA[15] == 1 || kBA[14] > 0
    /** `Z()` (k.java:5456, proven) → `f.a()` (f.java:755): IGP shop
     *  availability — `(aE && g()>0) ? slot : -1`; `g()` counts `br[]`
     *  available store items (f.java = Gameloft's in-game-purchase
     *  client: `bp[]` item URLs `&ctg=CCTL`, `igp19` RMS). The port has
     *  no shop → always false → `m()` skips row 3 (the shop row) as on
     *  every non-IGP device. When true the orig also rewrites
     *  `eA[0][3]` ∈ {32,33,34} (shop label variant). */
    private fun menuShopCheck() = false
    /** `y.k(a(y,str,206)[0])` — font measure (`inferred` 18px rows). */
    private fun menuTextHeight(s: String?) = if (s == null) 0 else 18

    /** `k.L(i)` (k.java:7084 / structured :5489, proven) — cursor nav:
     *  `v(16388)` up → `bw-1` clamp 0; `v(33024)` down → `bw+1` clamp
     *  `i-1`; each writes `fH=0;fI=1` (anim) and `z(23)` blip unless a
     *  track is already playing (`e.a()`). */
    private fun menuL(i: Int) {
        if (i <= 0) return
        if (pad.v(Pad.M_UP)) {
            kBw--
            if (kBw < 0) kBw = 0 else { kFH = 0; kFI = 1 }
            if (audioTrack >= 0) return
            z(23); return
        }
        if (pad.v(Pad.M_DOWN)) {
            kBw++
            if (kBw >= i) kBw = i - 1 else { kFH = 0; kFI = 1 }
            if (audioTrack >= 0) return
            z(23)
        }
    }

    /** `m(i,i2)` (structured :5472, proven) — cursor resolve:
     *  bv==0 skips the SAVE-LOAD entry (idx 3) when Z() off; clamps to
     *  `eA[i].length-1`. */
    private fun menuM(i: Int, i2: Int): Int {
        var v = i2
        if (i == 0 && i2 >= 3 && !menuShopCheck()) v++
        if (v > kEA[i].size - 1) v = kEA[i].size - 1
        return v
    }

    /** `O()` (structured :3561, proven) — push (j.c,bv,bw) onto the
     *  menu stack. `P()` (:3569) — pop: `j.c=dp[ds]` direct write + K(dq). */
    private fun menuO() {
        if (kDs in 0 until 16) {
            kDp[kDs] = jC; kDq[kDs] = kBv; kDr[kDs] = kBw
        }
        kDs++; kBw = -1
    }
    private fun menuP() {
        if (kDs <= 0) return                        // orig lacks bounds guard
        jC = kDp[kDs - 1]                           // j.c direct write (verbatim)
        bannerK(kDq[kDs - 1])
        kBw = -1; kDs--
    }

    /** `e(true)` — RMS save flush; unported → stub (`inferred`). */
    /** `ag()` (k.java:6358-6389, proven) — jC==10 mission poster card:
     *  `u=8`; `i(0,120)` card overlay (frame12 + black fill, `u==8`
     *  suppresses its hint arm); `A[4]` frame `i+4` at (0,200,119) —
     *  i = `fP` index of `aj+1`, default 4 → frame 8; brief
     *  `a(y,0,d(0,110),200,150,380,240,0,3)`; `v(327712)||j()` →
     *  `l(15);z(23)`; `j.g%10<5` → `y.a(d(0,9),200,220,3)` blink. */
    private fun posterAg(events: List<InputQueue.Event>) {
        var i = 1
        while (i < kFp.size && kAj + 1 != kFp[i]) i++
        subU = 8
        cardOverlayY = 120
        posterVisible = true
        posterFrame = i + 4
        posterBrief = d0(110) ?: ""
        if (pad.v(327712) || sawPressPending(events)) {  // j() ≈ tap (high-confidence)
            z(23); stateL(15); return
        }
        hintBlink = jG % 10 < 5
    }

    /** `ah()` (k.java:6392-6490, proven) — jC==22 medal/unlock viewer:
     *  header `d(0,113)`; dark panel (114,59,172,155) + rows
     *  (114,70+45i,172,40); `ex==3` → 3 fixed rows (`cc==2` → icon i3
     *  + `y.l(2)`; else locked frame3 + `y.l(4)`) + `v(131072)` →
     *  `l(3);K(4);z(30)` + back hint; else compact `cc==1` rows,
     *  `j.g<10` → fade `(10-j.g)*25<<24`, confirm → `cc 1→2` +
     *  `bA[130+i]` then `!fP-member||bA[15]==1 → l(15) else l(10)`. */
    private fun medalAh(events: List<InputQueue.Event>) {
        medalVisible = true
        medalTitle = d0(113) ?: ""
        if (kEx == 3) {
            for (i3 in 0..2) {
                medalRowIcon[i3] = if (kCc[i3] == 2) i3 else 3
                medalRowText[i3] = d0(114 + i3) ?: ""
                medalRowDim[i3] = kCc[i3] != 2
            }
            medalRowCount = 3
            if (pad.v(131072)) {
                z(30); bannerK(4); kBw = -1; stateL(3); return
            }
            hintBack = true                       // a("", d(0,17))
        } else {
            var i2 = 0
            for (i5 in 0..2) if (kCc[i5] == 1) {
                medalRowIcon[i2] = i5
                medalRowText[i2] = d0(114 + i5) ?: ""
                medalRowDim[i2] = false
                i2++
            }
            medalRowCount = i2
            screenFadeAlpha = if (jG < 10) ((10 - jG) * 25).toInt() else 0
            if (jG >= 10 && (pad.v(327712) || sawPressPending(events))) {
                for (i7 in 0..2) {
                    if (kCc[i7] == 1) kCc[i7] = 2
                    kBA[130 + i7] = kCc[i7]
                }
                z(23)
                if (kAj + 1 !in kFp || kBA[15] == 1) stateL(15)
                else stateL(10)
                return
            }
        }
        hintBlink = jG % 10 < 5
    }

    /** Whether the `/ASBR` RMS record exists (orig: `getNumRecords>0`).
     *  Set on `saveLoad`/`saveFlush` (`getNumRecords()>0` proxy). */
    var hasSaveRecord = false

    /** `e(true)` (k.java:5557, proven) — `setRecord(1,bA,0,512)` (or
     *  `addRecord` when empty). Emitted as a deferred command like
     *  every other backend effect; the record is `kBA` little-endian
     *  shorts (orig stores shorts via `a(bA,i,s)` at byte offsets). */
    fun saveFlush() {
        val record = ByteArray(kBA.size * 2)
        for (i in kBA.indices) {
            record[i * 2] = (kBA[i] and 0xFF).toByte()
            record[i * 2 + 1] = (kBA[i] ushr 8 and 0xFF).toByte()
        }
        hasSaveRecord = true
        pendingCommands += Command.PersistBA(record)
    }

    /** `e(false)` (k.java:5557, proven) — `getRecord(1,bA,0)`; a nop
     *  when the store is empty (null/short record → keep defaults).
     *  Boot path also derives `eJ = bA[10]!=0`, `au = bA[8]%3`
     *  (:4045-4052) — both already live in kBA slots here. */
    fun saveLoad(record: ByteArray?) {
        if (record == null || record.size < 2) return
        val n = minOf(kBA.size, record.size / 2)
        for (i in 0 until n) {
            kBA[i] = (record[i * 2].toInt() and 0xFF) or
                ((record[i * 2 + 1].toInt() and 0xFF) shl 8)
        }
        kEJ = kBA[10] != 0
        kAu = kBA[8] % 3
        if (kAu == 2 && kBA[69] == 0) kAu = 0
        hasSaveRecord = true
    }
    /** `f.a(str,0)` — "LOADING" overlay proc; unported → stub. */
    private fun loadingShow() { /* f.a(d(0,24),0) — unported */ }
    /** `W()` (structured :5093) — full game teardown on quit-to-menu:
     *  clips/claims/director/records released. Light port: drop the
     *  entity pools (level reload re-spawns) (`inferred` coverage). */
    private fun teardown() {
        npcs.clear(); pendingInsert.clear()
        kC = null                                   // claimer released
    }
    /** `a(z2)` (structured :5139) — level (re)load: `e.b(); V(); d(z2)`.
     *  `a(true)` = restart-from-checkpoint-ish, `a(false)` = continue.
     *  Maps to our `reload()` (`inferred`). */
    private fun reloadCheckpoint(full: Boolean) { reload() }

    /** `Q()` (structured :3576-3940, proven) — menu back/confirm
     *  dispatch. `v(131072)` = back key (our `M_CYCLE` — no zone emitter
     *  yet); `v(327712)` = confirm-complex — `M_CONTEXT` OR a tap on a
     *  menu row (the orig's touch row-hit in the draw loop sets `bw` +
     *  `E(32)`; folded into `menuRowAt` here, `inferred` mechanism). */
    private fun menuQ(pressY: Int) {
        footerQ()                            // a(str,str2) rects → E()
        // ---- back arm: `v(131072)` ------------------------------------
        if (pad.v(Pad.M_CYCLE) && jC != 23 && jC != 13) {
            kCb = true
            if (kBv == 2) { stateL(2); z(30) }
            if (kBv != 3 && kBv != 4) {
                if (kBv == 1) { kC?.resumeScript(); stateL(8); z(30); return }
                // `bv == 4` dead code in the orig (same guard excludes it)
                return
            }
            z(30); kFF = 0
            if (kEx == 8 || jC == 14) { menuP(); kBw = -1; return }
            if (kEx != 3) { stateL(2); return }      // ex==28 → l(2) too
            kFE = 255; kFo = 3; bannerK(4); kBw = -1; stateL(3); return
        }
        // ---- confirm arm: `v(327712)` ---------------------------------
        val rowTap = if (pressY >= 0) menuRowAt(pressY) else -1
        if (pad.v(Pad.M_CONTEXT) || rowTap >= 0) {
            kCb = true
            if (kBv == 2) {
                kAu = if (kBw < 0) 0 else kBw
                z(23)
                kBA[16] = kAu                        // j.a(bA,16,byte au)
                kBA[16] = 0                          // a(bA,16,short 0) — verbatim
                saveFlush(); kFF = 20; stateL(30); return
            }
            if (rowTap >= 0) kBw = rowTap            // row hit → `bw=i13`+E(32)
            if (kBw == -1) { kBw = 0; return }
            val iM = menuM(kBv, kBw)
            if (kEA[kBv][iM] != 83 && kEA[kBv][iM] != 84) z(23)
            menuItem(kEA[kBv][iM])
        }
    }

    /** Row hit-test for the touch-confirm (`inferred` layout — the
     *  orig's rects live in the unported draw proc at :6020-6120; rows
     *  stack at ~36px inside the `b(93,67,214)` panel). */
    fun menuRowAt(y: Int): Int {
        if (kEy <= 0) return -1
        // `c(i,i9,i3,i4)` per drawn row (k.java:6117 — the b() loop's own
        // hit-test, proven); x comes from the same release point.
        val rects = menuRowRects()
        for (i in rects.indices) {
            if (pointerDownIn(rects[i][0], rects[i][1], rects[i][2], rects[i][3]))
                return i
        }
        return -1
    }

    // -- b(x,y,w,z2,z3) menu panel geometry (k.java:5903-6150, proven) ---
    /** Panel Y: jc12/13 → `b(93,67,214,true,true)` (:1108); jc14 →
     *  bv3/4 → `b(93,86)` else `b(93,30)` (:1124-1129); other screens use
     *  the same 67 (`inferred` — call sites unmined). */
    fun menuPanelY(): Int = menuPanelRect()[1]
    /** Panel rect (x,y,w) verbatim per screen (:1108-1138, :6218):
     *  jc12/13 `b(93,67,214,true,true)`; jc14 bv3 `b(93,67)` / bv4
     *  `b(93,86)` / else `b(93,30)` (:1124-1129); jc19 `d(14,47,180)`;
     *  jc23/28 via ae() `d(93,120,214)` (:6221); jc29 `d(93,86,214)`
     *  (:1440); other states `inferred` (93,67,214). */
    fun menuPanelRect(): IntArray = when (jC) {
        14 -> intArrayOf(93, if (kBv == 3) 67 else if (kBv == 4) 86 else 30, 214)
        19 -> intArrayOf(14, 47, 180)
        23, 28 -> intArrayOf(93, 120, 214)
        29 -> intArrayOf(93, 86, 214)
        30 -> intArrayOf(93, 46, 214)   // af() `d(93,46,214)` (:6254)
        else -> intArrayOf(93, 67, 214)
    }
    /** z3 = the 40px title strip: verbatim true for jc12/13 (`b(…,true,
     *  true)`); jc14 goes through the 4-arg `b()` → z3=false (:1124);
     *  other screens `inferred` true. */
    /** z2 = bordered/filled variant: `b(...,true,·)` for jc12/13/14;
     *  `d(i,i2,i3)`→`b(...,false,false)` for jc19/23/28/29 (:5863-5868). */
    fun menuPanelZ2(): Boolean = jC == 12 || jC == 13 || jC == 14
    /** Panel visible this frame: jc12/13 (kAl'd) plus the footer states
     *  drawn unconditionally each frame (:1124-1185, :6218-6227). */
    val panelVisible: Boolean
        get() = menuVisible || jC == 14 || jC == 19 || jC == 23 ||
            jC == 28 || jC == 29
    fun menuPanelZ3(): Boolean = when {
        jC == 14 -> kBv == 3        // `b(93,67,214,true,true)` only there
        jC == 12 || jC == 13 -> true
        else -> false               // ae()/jc19/jc29 go via d() → z3=false
    }
    /** `i10 = min(8, ey)` — only the first 8 rows ever draw (:5924). */
    fun menuRowCount(): Int = minOf(8, kEy.coerceAtLeast(0))
    /** row height — `i4 = 35` when `i13==0 && j.c==2` else 30 (:5936). */
    fun menuI4(i13: Int): Int = if (i13 == 0 && jC == 2) 35 else 30
    /** text column width — `i5 = 135` when `(bv==4&&j.c!=14)||j.c==19`
     *  else 170 (:5942-5947). */
    fun menuI5(): Int =
        if ((kBv == 4 && jC != 14) || jC == 19) 135 else 170
    /** row-center x — `i14 = i + (i3>>1)`; jc19 re-centers
     *  `i + ((((i3-i5)>>1)+25+145)>>1)` (:6036-6040). */
    fun menuI14(i: Int, i3: Int): Int =
        if (jC == 19) i + (((i3 - menuI5() shr 1) + 25 + 145) shr 1)
        else i + (i3 shr 1)
    /** Row rect list mirroring b()'s `i9` walk: `i9 = y+10` (+40 under
     *  z3), `i9 += i4+3` per row, `i13==1&&j.c==2` → +13 before row 1,
     *  center split `(bv!=4&&j.c!=14)||j.c==19` at `i16 = i10/2` (-1 even)
     *  moves the rest to x=206 restarting at `i12` (:5977-6148). */
    fun menuRowRects(): List<IntArray> {
        val out = ArrayList<IntArray>()
        val pr = menuPanelRect()
        var i = pr[0]
        val i3 = pr[2]
        var i9 = pr[1] + 10
        if (menuPanelZ3()) i9 += 40
        val i12 = i9
        val i10 = menuRowCount()
        for (i13 in 0 until i10) {
            val i4 = menuI4(i13)
            if (i13 == 1 && jC == 2) i9 += 13
            out.add(intArrayOf(i, i9, i3, i4))
            if ((kBv != 4 && jC != 14) || jC == 19) {
                var i16 = i10 / 2
                if (i10 % 2 == 0) i16--
                if (i13 == i16 && i13 < i10 - 1) {
                    i = 206               // verbatim literal (jc19 col-2
                                          // lands off-panel — orig quirk)
                    i9 = i12 - (i4 + 3)
                }
            }
            i9 += i4 + 3
        }
        return out
    }
    /** `strD` verbatim (:6043-6080): jc19 → `d(0,eA[bv][iM])` + the eA
     *  decoration switch (32-34 → `bW.l(3)` + z[12] blink — palette flag
     *  returned in second; 83/84 → `": "+ON/OFF` (`ff=fg={21,20}` :306-307);
     *  97 → `": "+d(0,35+au)`; 103 → `l(3)`; 123 → `": "+d(0,124+k()?0:1)`);
     *  non-19 → `d(0,10)+" "+(i13+1)` = "LEVEL n". */
    /** `ce`/`cf` footer widths (k.java:2271-2296, proven): measured via
     *  the `y` font for d(0,16)/d(0,18) labels (`b.d+30`), else 36. */
    var kCe = -1
    var kCf = -1
    /** `y` font for the footer measure — same clip as renderer's fontY
     *  (pack-1 entry-3 = clip92 + shared charmap). Null in tests without
     *  assets → footer labels still returned, dims fall back to 36. */
    val footerFont: FontClip? = charmap?.let { cm ->
        clips[92]?.let { FontClip(it, FontClip.loadCharmap(cm), 4) } }
    /** `a(str,str2)` left-label width (:2276-2281): `y.a(str,null)` →
     *  `ce = b.d + 30` when str==d(0,16), else `ce = 36`. */
    fun footerLeftDim(str: String): Int =
        if (str == d0(16)) (footerFont?.measure(str)?.first() ?: 6) + 30 else 36
    /** right-label width (:2294-2298): `cf = b.d + 30` when str2==d(0,18)
     *  else 36. */
    fun footerRightDim(str: String): Int =
        if (str == d0(18)) (footerFont?.measure(str)?.first() ?: 6) + 30 else 36
    /** `a(str,str2)` label pair per screen (proven call sites):
     *  jc14 `a(bv==2?d(0,16):d(0,79), d(0,17))` (:1136); jc19
     *  `a(d(0,79), d(0,17))` (:1181); jc23/28 via ae() `a(d(0,79),
     *  (bv==0||jc==23||jc==13) ? "" : d(0,17))` (:6225); jc29
     *  `a(null, (bv==0||bv==3) ? "" : d(0,17))` (:1444); jc12/13 → none. */
    fun menuFooter(): Pair<String?, String?> = when (jC) {
        14 -> Pair(d0(if (kBv == 2) 16 else 79), d0(17))
        19 -> Pair(d0(79), d0(17))
        // ae() `a(d(0,79),(bv==0||j.c==23||j.c==13)?"":d(0,17))` (:6225)
        // + the eC==121 arm's `a("",d(0,17))` (:6214)
        23, 28 -> if (kEc == 121) Pair("", d0(17))
                 else Pair(d0(79), if (kBv == 0 || jC == 23 || jC == 13) "" else d0(17))
        29 -> Pair(null, if (kBv == 0 || kBv == 3) "" else d0(17))
        4 -> Pair("", d0(17))           // F() `a("",d(0,17))` (:2371)
        5 -> Pair("", d0(17))           // G() `a("",d(0,17))` (:2461)
        30 -> Pair(d0(79), d0(17))      // af() `a(d(0,79),d(0,17))` (:6266)
        else -> Pair(null, null)
    }
    /** Footer hit-test inside `a(str,str2)` — `c()` on the two rects
     *  arms `E(262144)` left / `E(131072)` right (:2288/:2309). Called
     *  from menuQ before the v() arms so the armed bits dispatch in the
     *  same frame, matching the orig's a()→L()→Q() order. */
    private fun footerQ() {
        val fl = menuFooter()
        val left = fl.first
        kCe = -1; kCf = -1
        if (left != null && left != "" && jC != 21 && jC != 8) {
            kCe = footerLeftDim(left)
            if (pointerDownIn(-5, 198, kCe + 20, 47)) padE(Pad.M_PAUSE)
        }
        val right = fl.second
        if (!right.isNullOrEmpty()) {
            kCf = footerRightDim(right)
            if (pointerDownIn(395 - kCf - 10, 198, kCf + 20, 47)) {
                padE(Pad.M_CYCLE)
            }
        }
    }
    fun menuRowText(i13: Int): Pair<String, Int> {
        if (jC == 19) {
            val iM = menuM(kBv, i13)
            var strD = d0(kEA[kBv][iM]) ?: "?"
            var pal = 0
            when (kEA[kBv][iM]) {
                32, 33, 34 -> pal = 3
                83 -> strD += ": " + (d0(if (kBE) 21 else 20) ?: "")
                84 -> strD += ": " + (d0(if (kBF) 21 else 20) ?: "")
                97 -> strD += ": " + (d0(35 + kAu) ?: "")
                103 -> pal = 3
                123 -> strD += ": " + (d0(124 + (if (cm == 1) 0 else 1)) ?: "")
            }
            return strD to pal
        }
        return "${d0(10) ?: "LEVEL"} ${i13 + 1}" to 0
    }
    /** jc19 sub-label `d(0, eX[eW[i13]])` drawn on `y` (:6092).
     *  `eW={2,2,1,1,2,0,3,2,2}` `eX={51,52,53,54}` (:299-300). */
    fun menuRowSub(i13: Int): String? {
        if (jC != 19) return null
        val kEw = intArrayOf(2, 2, 1, 1, 2, 0, 3, 2, 2)
        val kEx2 = intArrayOf(51, 52, 53, 54)
        return if (i13 in kEw.indices) d0(kEx2[kEw[i13]]) else null
    }

    /** `Q()`'s item switch (structured :3649-3940, proven arms; callees
     *  `e(true)`/`W()`/`a(bool)`/`f.*`/`j.*` stubbed — see docs). */
    private fun menuItem(item: Int) {
        when (item) {
            0 -> {                                   // MAIN MENU
                menuO(); kEc = 73; bannerK(3); kEb = 0; kBw = -1
            }
            1 -> {                                   // NEW GAME
                if (menuHasSave()) {
                    kEc = 69; bannerK(3); kEb = 87; kFG = true
                    stateL(28); kBw = -1
                } else {
                    kAj = 0; kAz = 0; kDD = 0
                    kBA[32] = kAz; kBA[14] = kAj; kBA[15] = 0
                    saveFlush(); kDB = 30; kDC = 30; kDF = 0
                    stateL(29)
                }
            }
            2 -> {                                   // CONTINUE
                if (kBA[15] == 1) { kBw = 0; kDa = 8; stateL(19) }
                else {
                    kAj = kBA[14]
                    kDB = kBA[44]; if (kDB == 0) kDB = 30
                    kDC = kBA[46]; if (kDC == 0) kDC = 30
                    kDF = kBA[48]
                    kAu = kBA[8]
                    kDD = kBA[32]; kAz = kDD
                    stateL(30)
                }
            }
            3 -> {                                   // SELECT LEVEL
                kBw = -1
                kDa = if (kDt || kBA[69] != 0) 8 else kBA[14] + 1
                stateL(19)
            }
            4 -> {                                   // OPTIONS
                if (jC == 14) menuO() else stateL(3)
                bannerK(4)
            }
            5 -> { stateL(4); kBw = 0 }              // HIGH SCORES
            6 -> { kBw = 0; stateL(5) }              // HELP
            7 -> stateL(6)                           // ABOUT
            8 -> {                                   // EXIT
                if (jC == 14) menuO() else { kFG = false; stateL(28) }
                kEc = 13; bannerK(3); kEb = 8; kBw = -1
            }
            11 -> {                                  // RESUME
                kC?.resumeScript()
                if (kCy == 21) { stateL(21); kX = 48 } else stateL(8)
                when (kFi) {                          // k.fi — music slot
                    1 -> z(1)
                    9 -> z(9)
                    -1 -> { }
                    else -> missionInit()
                }
                kDM = true
            }
            12 -> {                                  // RESTART
                menuO(); kEc = 25; bannerK(3); kEb = 12; kBw = -1
            }
            14 -> {                                  // YES
                when (kEc) {
                    13 -> jC = 11                    // exit-confirm → app
                    25 -> {                          // restart-confirm
                        kBG = 0
                        if (jC != 12 && jC != 13) {
                            menuP(); reloadCheckpoint(false); kAz = kDD
                        } else { kBx = -1; reloadCheckpoint(true); kBv = 0 }
                    }
                    69 -> {                          // wipe-save-confirm
                        kAj = 0; kAz = 0; kDD = 0; kAx = 30
                        kBA[14] = kAj
                        for (i in 0 until 3) { kCc[i] = 0; kBA[130 + i] = 0 }
                        kBA[44] = kAx; kBA[28] = kAx; kBA[32] = kAz
                        kBA[14] = kAj; kBA[15] = 0
                        kDB = 30; kDC = 30; kDF = 0
                        if (kFG) { kFF = 29; stateL(29) }
                        else {
                            kBA[69] = 0; kAu = 1
                            for (i in 0 until 24) kBA[81 + (i shl 1)] = 0
                            kEc = 121
                        }
                        kFG = false; saveFlush()
                    }
                    73 -> { menuP(); teardown(); stateL(2) }
                }
            }
            15 -> {                                  // NO
                kFG = false
                if (jC != 12 && jC != 13) {
                    if (kEx == 2) stateL(2)
                    else if (kEx != 3 || jC != 28) {
                        if (kDs < 16 && kDp[kDs] != 2 && kDp[kDs] != 14) {
                            menuP(); jG = 0
                        } else { menuP(); kBw = -1 }
                    } else {
                        kFE = 255; kFo = 3; bannerK(4); kBw = -1; stateL(3)
                    }
                } else { kBx = -1; teardown(); stateL(2) }
            }
            32, 33, 34 -> {                          // save slots
                loadingShow(); stateL(27)
                if (!kEJ) { kEJ = true; kBA[10] = 1; saveFlush() }
            }
            83 -> {                                  // MUSIC toggle
                kBE = !kBE; jG = 0
                if (kBE) { if (jC == 3) z(0) else z(6) }
                else { audioStop(); kFi = -1 }
            }
            84 -> {                                  // SFX toggle
                kBF = !kBF
                if (kBF) { audioStop(); z(23) }
            }
            87 -> {                                  // RESET GAME
                kEc = 69; bannerK(3); kEb = 87; kFG = false
                stateL(28); kBw = -1
            }
            97 -> {                                  // CONTROL cycle
                kAu = (kAu + 1) % 3
                if (kAu == 2 && kBA[69] == 0) kAu = 0
                kBA[8] = kAu; saveFlush()
            }
            103 -> { /* f.b() unported */ stateL(27) }
            113 -> { jG = 0; stateL(22) }            // ACHIEVEMENTS → medals
            117 -> {                                 // NEW GAME (no-save)
                kAj = 0; kAz = 0; kDD = 0; kAu = 1
                kDB = 30; kDC = 30; kDF = 0
                stateL(9)
            }
            123 -> { kCm = 1 - kCm; kBA[80] = kCm }  // STYLE toggle
        }
    }

    /** The `a()`-proc's frozen-state menu frame (k.java:1775-1800,
     *  proven): `j.i()→j.t=0` input flush skipped (`inferred` — we run
     *  the menu every frozen tick); 12/13 → `L(ey)` nav + `Q()`; 31 →
     *  stats (`bx<0→l(13)`; `v(65568)` → `l(13);bx=-1`). Returns true
     *  when the tick was consumed by a menu screen. */
    /** `a()`'s "others→menus" arm (k.java:1000-1070, proven): every
     *  non-play screen whose proc is the generic `L(ey);Q()` menu frame
     *  — states entered through `l()` + `K(bv)` (level select, options,
     *  score tables...). The world doesn't tick behind them (`inferred`
     *  — orig suspends sim on menu screens). */
    private val menuStates = intArrayOf(2, 3, 4, 5, 6, 14, 19, 28, 29, 30)

    /** `a(bVar, str, w)` (k.java:463-479, proven) — the wrap helper:
     *  ' ' before a `bV` char ({'.','!','?',',',':'} — :142) becomes
     *  '%' (non-break marker), then `bVar.a(string,w,false)`. */
    private fun wrapPage(str: String?, w: Int): IntArray {
        val f = footerFont ?: return intArrayOf(0)
        if (str == null) return f.wrap("", w)
        val sb = StringBuilder(str)
        for (i in str.indices) {
            if (str[i] == ' ' && i + 1 < str.length &&
                str[i + 1] in ".!?,:") sb[i] = '%'
        }
        return f.wrap(sb.toString(), w)
    }

    /** `G()` (k.java:2412-2488, proven) — the jc5 help/instructions
     *  scroller: 4 pages, 8 lines per screen (`cY` counts screens, not
     *  lines), left/right scroll + page wrap mod 4, `v(131072)` back
     *  to `cy`. Chevron taps inject the same pad masks via `E()`.
     *  `iK = 47 + (eE - y.k(1))/2` centers the viewport. */
    fun menuGIK(): Int = 47 + (kEe - (footerFont?.linesHeight(1) ?: 0)) / 2

    /** renderer's wrap of the current G() page — `a(y,str,261)`. */
    fun helpWrap(str: String): IntArray = wrapPage(str, 261)

    private fun menuG() {
        kCb = true
        footerQ()                                       // `a("",d(0,17))` (:2461)
        val iK = menuGIK()
        if (pointerDownIn(45, iK - 15, 50, 30)) padE(Pad.M_LEFT)     // `c()` → E(4112)
        if (pointerDownIn(305, iK - 15, 50, 30)) padE(Pad.M_RIGHT)   // `c()` → E(8256)
        if (pad.v(Pad.M_LEFT)) {                        // `v(4112)` (:2464)
            if (kCY > 1) kCY--
            else { kBw = (kBw - 1 + 4) % 4; kCY = kCX[kBw] }
            z(23)
        } else if (pad.v(Pad.M_RIGHT)) {                // `v(8256)` (:2474)
            if (kCX[kBw] > kCY) kCY++
            else { kCY = 1; kBw = (kBw + 1) % 4 }
            z(23)
        }
        if (pad.v(Pad.M_CYCLE)) {                       // `v(131072)` (:2483)
            kCY = 1; stateL(kCy); z(30)
        }
    }

    /** `a(bA, i)` (k.java:5372, proven) — LE-16 signed-short read on the
     *  `bA` save array; `kBA` stores one byte per slot so this is
     *  `kBA[i] | kBA[i+1]<<8`. */
    fun scoreAt(i: Int): Int =
        ((kBA[i] and 255) or ((kBA[i + 1] and 255) shl 8)).toShort().toInt()

    /** `F()` (k.java:2338-2408, proven) — the jc4 high-scores screen:
     *  `a(30,d(0,5))` title bar (renderer), `cU` difficulty page with
     *  left/right + chevron-tap cycling, `bw` scroll (verbatim quirk —
     *  the down arm tests `bw<0`, dead), footer + `v(131072)` back. */
    private fun menuF() {
        kCb = true
        footerQ()                                     // `a("",d(0,17))` (:2371)
        if (pad.v(Pad.M_UP)) {                        // `v(16388)` (:2373)
            if (kBw > 0) { kBw--; z(23) }
            return
        }
        if (pad.v(Pad.M_DOWN)) {                      // `v(33024)` (:2381)
            if (kBw < 0) { kBw++; z(23) }             // verbatim dead arm
            return
        }
        if (pad.v(Pad.M_RIGHT) || pointerDownIn(240, 15, 50, 80)) {
            kCU = (kCU + 1) % 3; z(23); return        // (:2388)
        }
        if (pad.v(Pad.M_LEFT) || pointerDownIn(110, 15, 50, 80)) {
            if (--kCU < 0) kCU = 2; z(23); return     // (:2395)
        }
        if (pad.v(Pad.M_CYCLE)) {                     // `v(131072)` (:2404)
            stateL(3); bannerK(4); z(30)
        }
    }

    /** `ae()` (k.java:6204-6228, proven) — the jc23/28 screen: the
     *  `eC==121` wipe-confirm arm (own title at y=120 + back-only
     *  dispatch), else `d(93,120,214)` + bW title at y=80 + footer +
     *  `L(ey); Q()`. */
    private fun menuAe(pressY: Int) {
        if (kEc == 121) {                                // wipe-confirm arm
            if (pad.v(Pad.M_CYCLE)) {                    // `v(131072)` (:6209)
                kFE = 255; kFo = 3; bannerK(4); kBw = -1
                stateL(3); z(30); return
            }
            footerQ(); return
        }
        // `eB>0 && j.c!=23 → d(0,eB)` (:6215) — the subline lookup; its
        // result feeds an unported draw slot (inferred — decompiled
        // statement discards it).
        footerQ()
        menuL(kEy); menuQ(pressY)
    }

    /** `af()` (k.java:6230-6320, proven) — the jc30 medal/level browse
     *  screen: `fO==0` init (`da` unlocked count → `fQ` rows, `bL`
     *  cursor), `fC` title fade, `fR` pending-nav, footer + dispatch. */
    private fun menuAf() {
        if (kFo == 0) {                              // init arm (:6233)
            kFC = 20; kFE = 0; kFQ = 0
            kDa = if (kDt || kBA[69] != 0) 8 else kBA[14] + 1
            for (i in 0 until 4) if (kDa > kFP[i]) kFQ++
            kEy = kFQ; kBL = 0; kFR = -1; kFo = 1
        }
        // `d(93,46,214)` draw + `fC` fade (:6254-6261) — renderer reads
        if (kFC > 0 && kFC != 255) { kFC += 20; if (kFC >= 255) kFC = 255 }
        if (kFR != -1) {
            if (kFE > 20) kFE -= 20 else kFR = -1
        }
        // `a(d(0,79),d(0,17))` — footerQ already ran in menuQ? No — af()
        // calls its own a() → arm the footer rects here instead.
        footerQ()
        if (pad.v(Pad.M_CONTEXT)) {                  // `v(327712)` (:6269)
            if (kFF == 20) stateL(20) else stateL(9)
            kFF = 0; z(23); return
        }
        if (pad.v(Pad.M_CYCLE)) {                    // `v(131072)` (:6279)
            if (kFF == 19) { kFo = 3; stateL(19) } else stateL(2)
            kFF = 0; z(30); return
        }
        if (kFQ > 1) {                               // browse nav (:6292)
            if (pad.v(Pad.M_UP)) {
                kFR = kBL
                if (--kBL < 0) kBL = 0
                else { kFC = 20; kFE = 255; kBw = kBL
                       kFH = 0; kFI = 1; menuFkArm = 21 }
                if (audioTrack == -1) z(23)
                return
            }
            if (pad.v(Pad.M_DOWN)) {
                kFC = 20; kFE = 255; kFR = kBL
                if (++kBL >= kFQ) kBL = kFQ - 1
                else { kBw = kBL; kFH = 0; kFI = 1; menuFkArm = 21 }
                if (audioTrack == -1) z(23)
            }
        }
    }

    private fun menuFrame(pressY: Int): Boolean {
        when (jC) {
            12, 13 -> {
                if (kJT != 0) {                      // `j.i()` (:1109) —
                    kJT = 0                          // held pad bits flush
                    return true                      // → `j.t=0`, skip frame
                }
                scrollBounds()                       // b(true)
                kEg = 0
                menuL(kEy)
                menuQ(pressY)
            }
            4 -> menuF()                           // F() (:2338, proven)
            5 -> menuG()                           // G() (:2412, proven)
            23, 28 -> menuAe(pressY)                 // ae() (:6204, proven)
            30 -> menuAf()                         // af() (:6230, proven)
            in menuStates -> { menuL(kEy); menuQ(pressY) }
            31 -> {
                if (kBx < 0) stateL(13)
                else if (pad.v(Pad.M_CONTEXT) || pressY >= 0) {
                    stateL(13); kBx = -1
                }
            }
            else -> return false
        }
        return true
    }

    // -- render view (Level0Renderer overlay reads these) ----------------
    /** Fail/YES-NO dialog up (L462 semantics): panel + title + rows. */
    val menuVisible get() = kAl && (jC == 12 || jC == 13)
    fun menuTitle(): String? = if (kEb >= 0) d0(kEb) else null
    fun menuPrompt(): String? = if (kEc != -1) d0(kEc) else null
    fun menuRows(): List<Pair<String, Boolean>> =
        (0 until kEy.coerceIn(0, kEA[kBv].size)).map { i ->
            (d0(kEA[kBv][menuM(kBv, i)]) ?: "?") to (i == kBw)
        }
    /** Stats screen (L466): `d(0,bx)` + "TOUCH THE SCREEN" blink. */
    val statsVisible get() = kAl && jC == 31 && kBx >= 0
    fun statsText(): String? = if (kBx >= 0) d0(kBx) else null
    /** `e.b()` (e.java:87, proven) — stop the current track. */
    fun audioStop() { audioTrack = -1 }
    private fun scrollBounds() { /* b(true) — scroll refresh, unported */ }
    /** `B()` (k.java:2021, proven) — mission music: `aJ==1 → z(9)`,
     *  else `ee[aj]` when != -1. */
    private fun missionInit() {
        if (kAJ == 1) z(9) else if (kEE[kAj] != -1) z(kEE[kAj])
    }
    private fun inputReset() { pad.edge = 0 }        // v() — input reset (inferred)
    /** `j.c == 21` modal-dialog phase (screen-L target of op105's
     *  `k.l(21)`): world keeps ticking but the claimer is `cd[0]`-halted;
     *  the original's dialog screen dismisses on input → `k.C.Z()`
     *  (i.java:19425 `cd[0]=false`) resumes the script. The visual
     *  `b(9,1+aj,str,str)` draw is unported (`inferred`); the lifecycle
     *  contract — arm on 21, dismiss on next press → `resumeScript` —
     *  is what the claim VM observes. The arming tick's own press can't
     *  dismiss: the modal check runs at the top of the NEXT tick, so the
     *  first fresh press edge is the dismiss — no cooldown needed. */
    val dialogModal get() = jC == 21
    /** Screen-21 dismiss → back to the previous state (`inferred` — the
     *  original's l()-based return target is unmined). */
    private fun leaveDialog() { jC = kCy; kAl = false }
    /** Test-harness flag — when true, a modal dialog resolves the same
     *  tick (emulates the player instantly tapping the screen-21 dismiss
     *  edge). Real gameplay leaves it false: a press is required. */
    var autoDismissDialog = false
    /** `g.g()` (g.java:3939): player dead. */
    override fun gG(): Boolean = player.x1 <= 0
    /** `k.s(int)` (k.java:7149): index of uid in `k.eH[]` or -1. */
    override fun kSIndex(x: Int): Int = kEh.indexOf(x)
    /** `k.bz[ca]` (k.java:20039 seed): first-group offsets per block. */
    override fun claimOps(ca: Int): IntArray? = kBz.getOrNull(ca)
    override fun kT(op: Int): Int = ScriptTables.EI[op - 100]
    override var gj = false                        // g.j context latch
    override var kL: Entity? = null                // k.L claim entity
    override var claimCo = 6                       // k.co
    override var claimRect: IntArray? = null       // k.cp
    override var iBf = false                       // i.bf engage latch
    override var iBx: Entity? = null               // i.bx grab-QTE holder
    override var kAA = 0                           // k.aA
    override var gZ = false                        // g.z
    override var iL = -1                           // i.L
    override var iM = -1                           // i.M
    /** `aS.l()` (g.java:4968) — grab-release; bM=null first per the
     *  original head, then the shared `PlayerFsm.l` resolver. */
    override fun grabResolve(p: Entity): Boolean {
        p.bM = null
        return playerFsm.l(p, pad)
    }
    override var gc: Entity? = null                // g.c crate-top link
    override var iBq = 0                           // i.bq floor-Y latch
    override var kN: Entity? = null                // k.N prompt marker
    override var kCq = -1                          // k.cq bound uid
    override var gP = 0                            // g.p kill-bonus flag
    /** `k.c(int,int,int)` (k.java:870, proven): the ax14/clip9/S54/az302
     *  prompt marker — created once then repositioned every call; `cq` is
     *  bound to the requesting entity's uid. */
    override fun showPrompt(x: Int, y: Int, aw: Int) {
        if (kN == null) {
            kN = Entity(14, clips[9]).apply {
                this.aw = -1; au = 0
                setAnim(54); az = 302
                setPositionPx(x, y); av = false
                refreshBoxes()
            }
            pendingInsert += kN!!
            kCq = aw
        }
        kN?.setPositionPx(x, y)
    }
    /** `k.k(int)` (k.java:888, proven): `cq==aw || aw==-1` → `N.p()` +
     *  `N=null` + `cq=-1`. */
    override fun clearPrompt(aw: Int) {
        val n = kN ?: return
        if (kCq == aw || aw == -1) {
            n.deactivate(); pendingRemove += n; kN = null; kCq = -1
        }
    }
    var gs = false                                 // g.s transition bool
    var gT = 0                                     // g.t transition int
    override fun gH(): Boolean = gs || gT != 0     // g.h() latch

    /** `k.m()` (k.java:863, proven): reset the interact-claim channel. */
    override fun claimReset() {
        claimCo = 6; kL = null; claimRect = IntArray(4)
    }

    /** `k.a(i,int,int[])` (k.java:816, proven): interact-claim registrar —
     *  same-entity refresh, else `prio<co || prio==1` steals the claim
     *  (`co=prio; L=e`); ax51 binds its Y rect not the passed rect. */
    override fun registerClaim(e: Entity, prio: Int, rect: IntArray) {
        if (kL != null && kL === e) {
            claimRect = if (e.ax == 51) e.Y else rect
            return
        }
        if (prio < 0 || prio >= 6) return
        if (prio < claimCo || prio == 1) {
            claimCo = prio; kL = e
            claimRect = if (e.ax == 51) e.Y else rect
        }
    }

    /** `k.S` — camera right bound (`boundMaxX`), also the aP arena
     *  clamp right edge (same static in the original). */
    override var kSBound: Int get() = boundMaxX; set(v) { boundMaxX = v }

    /**
     * ax2 `aY()` (i.java:13477): W-rect overlap (`a(this.W, k.aS.W)`) —
     * modeled as the player crossing the record's cell — saves the
     * checkpoint snapshot into bA, self-removes, and re-materializes
     * every entity to its home slot (`k.a(bb[i], bb[i].as)`).
     * `k.y()` (checkpoint sfx/flash) unported — no audio hook yet.
     */
    private fun fireCheckpoints() {
        for (cp in checkpoints) {
            if (cp.consumed) continue
            if (Math.abs(cp.ak - player.ak) > cellPx) continue
            if (player.al < cp.al - cellPx) continue
            cp.consumed = true
            checkpointSnap = writeIX(cp.aw)
            checkpointDead = npcs.filter { it.S == 139 }.map { it.aw }.toSet()
            // k.a(bb[i], bb[i].as): re-materialize each entity at its home
            // slot — original skips as==-98 (consumed/dead) and ax==70
            // (proven i.java:13535+); our -98 equivalent = S139 corpse.
            for (n in npcs) {
                if (n.ax == 70 || n.S == 139) continue
                n.setPositionPx(n.homeX, n.homeY)
                // doors re-materialize at their record's base bank (Z[4]=f5)
                n.setAnim(if (n.ax == 44) n.Z[4] else 0)
            }
        }
    }

    /** `L()` (k.java:3270, proven) — `dg=0; ap[i]=0` — plus the `a(z2)`
     *  else-arm stash restore `az=0;ax=dB;az=dD;ay=dC;aN=dF`
     *  (:5207-5216; the verbatim `az` double-write kept). */
    private fun statsReset() {
        kDg = 0
        for (i in kAp.indices) kAp[i] = 0
        kAz = 0; kAx = kDB; kAz = kDD; kAy = kDC; kAN = kDF
    }

    private fun reload() {
        statsReset()                        // L() + a(z2) restore arm
        resetPlayerToSpawn()
        spawnEntities()
        jC = 8                                   // back to play (j.c==8)
        kAl = false
        kM(kAd)                                   // C()/f() `m(ad)` snap
    }

    /**
     * ax37 `al()` (i.java:7053) — scroll-bound trigger.
     * mode==1 (Z[3]): fire while player W overlaps the zone (`a()`);
     * mode==0: fire when player W is fully inside (`b()` = contained).
     * Payload per Z[0] bits, each gated on zone∩viewport `a(this.W,k.ac)`:
     * &1→k.R=X[0] (camX floor), &4→k.T=X[1] (camY floor),
     * &2→k.S=X[2] (camX+400 ceiling), &8→k.U=X[3] (camY+240 ceiling).
     * Z[2]==-1 records skip the linked-entity gate (all level-0 data).
     * `k.ah` claim + `k.n()` release and the L8 holder-reset are ported in
     * `fireScrollTriggers` (i.java:7053-7159). The `Z[2]!=-1` linked-entity
     * gate (i.java:7119-7127) is skipped — every level-0 record has -1.
     */
    /** The `k.ah` claimant — the trigger holding the wall slot (k.a(this),
     *  i.java:7176 `b(k.aS.W, this.W)` containment arm). Released via k.n()
     *  when its zone stops firing while it holds the slot (i.java:7068
     *  /:7230). */
    private var scrollHolder: ScrollTrigger? = null

    private fun fireScrollTriggers() {
        val view = intArrayOf(camX, camY, camX + VIEW_W, camY + VIEW_H)
        for (t in scrollTriggers) {
            val pw = player.W
            // i.java:7062 (L8): the k.ah holder clears R/S/T/U at the head
            // of its own al() each tick, then re-writes below — so bounds
            // always reflect the currently-firing set while a holder
            // stands, and vanish entirely on release.
            if (scrollHolder === t) {
                boundMinX = 0; boundMinY = 0; boundMaxX = 0; boundMaxY = 0
            }
            val fired = if (t.mode == 1) rectsOverlap(pw, t.zone)
                        else rectContains(pw, t.zone)
            if (!fired) {
                // i.java:7068/:7230: zone lost while holding k.ah → k.n().
                if (scrollHolder === t) scrollHolder = null
                continue
            }
            // i.java:7131-7143 (L79/L85): full containment claims k.ah —
            // unless a mode-1 (overlap) holder already stands.
            if (rectContains(pw, t.zone) &&
                (scrollHolder == null || scrollHolder === t ||
                 scrollHolder!!.mode != 1)) {
                scrollHolder = t
                // k.a(this) → k.ah = the trigger entity: W = its ZONE rect;
                // aF is the record flag — every level-0 ax37 record carries
                // aF=0, so m()'s L282 wall clamp (k.java:2406 `aF != 1 →
                // skip`) never engages for ax37 — the trigger only drives
                // R/T/S/U bounds. (Previous rev forced aF=1 + W=bound →
                // camA pinned between wall ceiling and R floor.)
                kAh = Entity(37, null).apply {
                    W[0] = t.zone[0]; W[1] = t.zone[1]
                    W[2] = t.zone[2]; W[3] = t.zone[3]
                }
            }
            if (!rectsOverlap(t.zone, view)) continue
            if (t.mask and 1 != 0) boundMinX = t.bound[0]
            if (t.mask and 4 != 0) boundMinY = t.bound[1]
            if (t.mask and 2 != 0) boundMaxX = t.bound[2]
            if (t.mask and 8 != 0) boundMaxY = t.bound[3]
        }
        if (scrollHolder == null) kAh = null
    }

    private fun rectsOverlap(a: IntArray, b: IntArray) =
        a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1]

    private fun rectContains(inner: IntArray, outer: IntArray) =
        inner[0] >= outer[0] && inner[1] >= outer[1] &&
        inner[2] <= outer[2] && inner[3] <= outer[3]

    // -- input ------------------------------------------------------------

    private var pointerDown = false

    /** `k.ce`/`k.cf` (k.java:147-148, proven): safe-area insets for the
     *  400×240 canvas — the soft-key row excludes x≤ce / x≥400-cf below
     *  y207 (`cg`=37 is the top-inset, k.java:149). */
    private val ce = 60
    private val cf = 60

    /** Any DOWN edge in this tick's event list — the screen-21 dialog's
     *  dismiss input (press anywhere, like the original's `k.v` edge). */
    private fun sawPressPending(events: List<InputQueue.Event>): Boolean =
        events.any { it.type == InputQueue.Type.DOWN }

    // -- hit-test helpers (k.java:537-546 + h() :5301, all proven) -------
    /** `b(x,y,x0,y0,w,h)` — rect hit; (-1,-1) never hits. */
    private fun insideRect(x: Int, y: Int, x0: Int, y0: Int, w: Int, h: Int): Boolean =
        !(x == -1 && y == -1) && x >= x0 && x <= x0 + w && y >= y0 && y <= y0 + h
    /** `a(x,y,cx,cy,r)` — radial hit via `h()` octagonal hypot. */
    private fun insideRadial(x: Int, y: Int, cx: Int, cy: Int, r: Int): Boolean =
        !(x == -1 && y == -1) &&
            player.h(kotlin.math.abs(x - cx), kotlin.math.abs(y - cy)) <= r
    /** `k(x,y)` (k.java:722): the `N` marker's 50×50 zone (view space). */
    private fun markerZoneHit(x: Int, y: Int): Boolean {
        val n = kN ?: return false
        return insideRect(x, y, (n.ak - 25) - camX, (n.al - 25) - camY, 50, 50)
    }
    /** `i.b(x,y)` (i.java:7686): the L/M anchor ±70px radial (view space). */
    private fun anchorZoneHit(x: Int, y: Int): Boolean =
        anchorLx != -1 && anchorLy != -1 &&
            insideRadial(x, y, anchorLx - camX, anchorLy - camY, 70)

    /** `k.E(int)` (k.java:553, proven): clear → set → bh3 remap → latch. */
    private fun padE(mask: Int) {
        pad.e(mask, jC == 8 && bh3 && !mounted)
    }

    /** `c(x,y,x1,x2,y1,y2)` (k.java:623, proven): the wheel cell index
     *  0..8 — 3×3 split at the rect bounds; mounted widens the inner
     *  column split (inner halves → 3/5). */
    private fun wheelCell(x: Int, y: Int, x1: Int, x2: Int, y1: Int, y2: Int): Int {
        if (x == -1 && y == -1) return -1
        val i7 = if (y < y1) 0 else if (y > y2) 2 else 1
        val i8 = if (x < x1) 0 else if (x > x2) 2 else 1
        if (mounted && y > y1 && y < y2) {
            val mid = (x1 + x2) / 2
            if (x > x1 && x <= mid) return 3
            if (x > mid && x < x2) return 5
        }
        return i7 * 3 + i8
    }

    /** `j(x,y)` (k.java:576-621, proven): resolve a canvas tap to the
     *  wheel index for `E(2<<iJ)` — only live in play states
     *  (j.c==8 or the u∈{8,10} arms of 21). Returns -1 when the tap is
     *  outside the wheel or hits a consumed zone. */
    fun resolvePadZone(x: Int, y: Int): Int {
        if (!((jC == 21 && subU == 8) || jC == 8 || (jC == 21 && subU == 10)) ||
            x == -1 || y == -1 || y >= 240) return -1
        // margins below the soft-key row + pause-icon rect are not wheel
        if (((x <= ce || x >= VIEW_W - cf) && y >= 207) ||
            insideRect(x, y, 354, 0, 46, 37)) return -1
        val p = player
        if (mounted) {                                          // k()
            if (!bh3) {
                if (insideRadial(x, y, 270, 165, 70)) return 4
                if (insideRadial(x, y, 320, 110, 70)) return 1
            }
            val cn = if (bh3) 50 else 5                          // k.java:3146
            if (!insideRect(x, y, cn - 10, 124, 116, 116)) return -1
            val cell = wheelCell(x, y, (cn - 10) + 38, (cn - 10) + 77, 162, 201)
            return if (cell == 4) -1 else cell
        }
        if (bh3 && insideRect(x, y, (p.ak - camX) - 10, ((p.al - camY) - 20) - 25, 20, 25)) {
            p.aq = -1; p.ar = -1                                 // head-tap = drop hint
            return 4
        }
        val l = kL; val cp = claimRect
        if (l != null && cp != null &&
            x >= cp[0] - camX && x <= cp[2] - camX &&
            y >= cp[1] - camY && y <= cp[3] - camY)
            return if (claimCo == 1) 1 else 4
        if (markerZoneHit(x, y) || anchorZoneHit(x, y)) return -1
        if (p.S == 250 || p.S == 244) {
            val i3 = (p.ak - camX) - 38
            return wheelCell(x, y, i3, i3 + 76,
                (p.al - camY) - 38, (p.al - camY) + 38)
        }
        if (p.W[1] == p.W[3]) { p.W[1] = p.Y[1]; p.W[3] = p.Y[3] }
        val i6 = (p.ak - camX) - 25
        return wheelCell(x, y, i6, i6 + 50, (p.W[1] - camY) - 10, (p.W[3] - camY) + 10)
    }

    /** Canvas point guaranteed inside wheel `cell` for the current player
     *  (test helper — the wheel splits at aS±25 x, W-box ±10 y). */
    fun cellPoint(cell: Int): Pair<Int, Int> {
        val p = player
        val x = when (cell % 3) { 0 -> (p.ak - camX) - 30; 1 -> p.ak - camX; else -> (p.ak - camX) + 30 }
        val y = when (cell / 3) { 0 -> (p.W[1] - camY) - 15; 1 -> (p.W[1] + p.W[3]) / 2 - camY; else -> (p.W[3] - camY) + 15 }
        return x to y
    }

    /** Raw InputQueue events are screen px in the 400x240 view.
     *  `pointerPressed/Dragged/Released` (k.java:486-519, proven). */
    private fun consume(events: List<InputQueue.Event>) {
        for (e in events) {
            when (e.type) {
                InputQueue.Type.DOWN -> {
                    // pause icon (c(354,0,46,37)→E(262144), k.java:1054)
                    if (insideRect(e.x, e.y, 354, 0, 46, 37) && jC == 8)
                        padE(Pad.M_PAUSE)
                    else {
                        val iJ = resolvePadZone(e.x, e.y)
                        if (iJ != -1) { padE(2 shl iJ)         // E(2<<iJ)
                            if (iJ < 5) kJT = kJT or (1 shl iJ) }
                    }
                    pointerDown = true
                    kCj = e.x; kCk = e.y
                }
                InputQueue.Type.MOVE -> {                     // pointerDragged
                    val iJ = resolvePadZone(e.x, e.y)
                    if (iJ != -1 && pad.bB and (2 shl iJ) == 0) padE(2 shl iJ)
                    if (iJ in 0..4) kJT = kJT or (1 shl iJ)
                    pointerDown = true
                    kCj = e.x; kCk = e.y
                }
                InputQueue.Type.UP, InputQueue.Type.CANCEL -> {// pointerReleased
                    kCh = e.x; kCi = e.y
                    kCl = true
                    pad.releaseFlush()                        // eN=eL; eL=0
                    kJT = 0                                    // b(i) — all
                                                                 // held bits
                                                                 // release
                    pointerDown = false
                    kCj = e.x; kCk = e.y
                }
            }
        }
        // input-sample tail (k.java:1509-1519, proven)
        lastMoveX = kCj; lastMoveY = kCk                       // k.J/k.K
        if (kCl) { kCj = -1; kCk = -1; kCl = false }
        lastTouchX = kCh; lastTouchY = kCi                     // k.H/k.I
        kCh = -1; kCi = -1
    }

    // -- sim --------------------------------------------------------------

    fun tick(events: List<InputQueue.Event>) {
        consume(events)
        // k.H/k.I live for one frame (k.java:1874-77 — `H=ch;I=ci;ch=-1;
        // ci=-1`); k.J/k.K persist while touching and clear the frame
        // after release (the `cl` latch, k.java:1869-72).
        try {
        pad.commit()                                          // k.java:1594-1608 tail
        // k.F(aj) (k.java:4644): input events reset g.J to f0do[key]=5
        // (all 9 keys). ef[] is held-state per frame → set while held.
        // `inferred` on cadence; value 5 proven (k.java:8384).
        if (pointerDown) player.gJ = 5
        // pause icon edge (k.java:1056-1063): v(262144) → claimer pause
        // + bw=0 + l(14) — read inside the play arm.
        if (jC == 8 && pad.v(Pad.M_PAUSE)) {
            kC?.pauseScript()                             // C.Y() — cd[0]=true
            kBw = 0
            stateL(14)
        }
        playerFsm.tickCount = tickIndex

        // `k.al` world-freeze (i.I() gate): mission-fail / win / frozen
        // screens — context edge = retry (k.java:1804 `v(65568)` → advance,
        // k.java:2268-2280; the win screen's confirm runs `f(false)`-
        // equivalent — reload() here, `inferred` for the milestone flow).
        // j.c==21 keeps its own block below — it needs the dismiss edge.
        if ((kAl && jC != 21) || jC in menuStates) {
            // menu screens run their own frame (Q()/L() dispatch —
            // structured k.java:3576+); states without a menu proc
            // (16/17) stay fully frozen.
            if (!menuFrame(events.firstOrNull { it.type == InputQueue.Type.DOWN }?.y ?: -1)) {
                if (pad.v(Pad.M_CONTEXT) && kAl) reload()  // non-menu frozen states
            }
            tickIndex++; jG++
            return
        }

        // j.c==21 dialog modal (k.l(21), i.java:20190): screen 21 isn't
        // the play state — the original suspends the entity sim behind
        // the dialog, which is what stops `ao()`/`N()` from re-arming the
        // halted claimer while `cd[0]` holds. A press edge = the screen's
        // dismiss → `k.C.Z()` (cd[0]=false) → back to play next tick.
        if (dialogModal) {
            if (autoDismissDialog) {                 // test harness: instant tap
                kC?.resumeScript(); leaveDialog()
            } else if (sawPressPending(events)) {
                kC?.resumeScript(); leaveDialog()
                pad.edge = 0        // eat the dismiss edge — not a gameplay tap
            } else { tickIndex++; jG++; return }
        }

        // case 15 → M() (k.java:1141) — the win-stats screen proc
        // replaces the entity sim entirely while j.c==15. jG++ runs
        // first (j.java:255 `g++` precedes each `a()` dispatch). The
        // play-frame counters below don't tick — they're inside the
        // play case the dispatch replaces.
        // case 10 → ag() / case 22 → ah() (k.java:1102/:1308) — full-
        //  screen procs replace the entity sim like M() does.
        if (jC == 10) { jG++; posterAg(events); tickIndex++; return }
        if (jC == 22) { jG++; medalAh(events); tickIndex++; return }
        if (jC == 15) { jG++; winStatsM(); tickIndex++; return }

        // mission timer + ap[2] frame counter (k.java:1652-1655,
        // proven): ticks while unpaused and not dialog-suspended.
        if ((player.P and 512) != 0 ||
            (kC?.claimActive() != true && (jC != 21 || subU != 9))) {
            kDg++; kAp[2]++
        }

        player.collideSides(this, true)
        playerFsm.tick(player, pad)
        player.integrate()
        player.advanceAnim()

        for (n in npcs) {
            if (n.ax == 44) npcFsm.tickDoor(n, player)
            else if (n.ax == 10) npcFsm.tickTrigger(n, player)
            else if (n.ax == 4) npcFsm.tickDestructible(n, player)
            else if (n.ax == 67) npcFsm.tickDecor(n, player)
            else if (n.ax == 14) npcFsm.tickPickup(n, player)
            else if (n.ax == 16) npcFsm.tickRequestMarker(n, player, pad)
            else if (n.ax == 21) npcFsm.tickDirector(n, player, pad)
            else if (n.ax == 29) npcFsm.tickBoss(n, player, pad)
            else if (n.ax == 61) npcFsm.tickAx61(n, this, player)
            else if (n.ax == 41) npcFsm.tickKnockable(n, this, player)
            else if (n.ax == 66) npcFsm.tickPlatform(n, this, player)
            else if (n.ax == 51) npcFsm.tickPushable(n, this, player)
            else if (n.ax == 22) npcFsm.tickZoneInteract(n, this, player)
            else if (n.ax == 5) npcFsm.tickMissionLogic(n, this, player)
            else if (n.ax == 27) npcFsm.tickAx27(n, this, player)
            else if (n.ax == 40) npcFsm.tickAx40(n, this, player)
            else if (n.ax == 9) npcFsm.tickAx9(n, this, player)
            else if (n.ax == 6) npcFsm.tickAx6(n, this, player)
            else if (n.ax == 19) npcFsm.tickAx19(n, this, player)
            else if (n.ax == 35) npcFsm.tickAx35(n, this, player)
            else if (n.ax == 15) npcFsm.tickAx15(n, this, player)
            else if (n.ax == 46) npcFsm.tickAx46(n, this, player)
            else if (n.ax == 7) npcFsm.tickAx7(n, this, player)
            else if (n.ax == 42) npcFsm.tickAx42(n, this, player)
            else if (n.ax == 13) npcFsm.tickAx13(n, this, player)

            else if (n.ax == 78) npcFsm.tickAx78(n, this, player)
            else if (n.ax == 54 || n.ax == 30) npcFsm.tickAx54(n, this, player)
            else if (n.ax == 56) npcFsm.tickAx56(n, this, player)
            else if (n.ax == 24) npcFsm.tickAx24(n, this, player)
            else if (n.ax == 58) npcFsm.tickAx58(n, this, player)
            else if (n.ax == 60) npcFsm.tickAx60(n, this, player)
            else if (n.ax == 43) npcFsm.tickAx43(n, this, player)
            else if (n.ax == 69) npcFsm.tickAx69(n, this, player)
            else if (n.ax == 73) npcFsm.tickAx73(n, this, player)
            else if (n.ax == 47) npcFsm.tickAx47(n, this, player)
            else if (n.ax == 50) npcFsm.tickAx50(n, this, player)
            else if (n.ax == 64) npcFsm.tickAx64(n, this, player)
            else if (n.ax == 74) npcFsm.tickAx74(n, this, player)
            else if (n.ax == 76) npcFsm.tickAx76(n, this, player)
            else if (n.ax == 34) npcFsm.tickAx34(n, this, player)
            else if (n.ax == 17) npcFsm.tickAx17(n, this, player)

            else npcFsm.tick(n, player)
            // i.ad() per-frame bubble tick (k.java:3740-3749 proven):
            // every entity except soldiers (11) and civilians (17).
            if (n.ax != 11 && n.ax != 17) npcFsm.tickBubble(n, this)
        }
        if (pendingRemove.isNotEmpty()) {
            npcs.removeAll(pendingRemove)
            pendingRemove.clear()
        }
        tickShotPool()                            // k.aX pool step (inferred)
        if (pendingInsert.isNotEmpty()) {         // k.b(aK) drain
            npcs += pendingInsert
            pendingInsert.clear()
        }
        fireCheckpoints()
        fireScrollTriggers()
        // k.aO message countdown (k.java:5527): `aO -= j.f` per tick.
        if (kAO >= 0) kAO -= 62

        // k.m(cJ) per-tick (k.java:3320 proven, `bh[aj]!=3` gate):
        // the verbatim tracker — lookahead margin, scroll walls, bounds,
        // lerp `l(dx/2, kX|28)`, cO shake. Replaces the placeholder follow.
        if (Entity.MISSION_BH[kAj] != 3) kM(1)
        else {
            // k.java:3363-3368 (L137/L139): bh3 replaces m(1) with
            // `if (i.bW) i.X(); D()` — the director's phase-checkpoint
            // write then the autoscroll camera.
            if (iBW) {
                checkpointSnap = writeIX(checkpointSnap?.aw ?: -1)
                iBW = false                                        // consumed
            }
            kD()
        }
        l142Tail()          // L142-L200 — runs on both camera arms

        // knockout: d() → x[1]<=0 → k.l(12) (proven)
        if (player.x1 <= 0) stateL(12)
        // below camera bottom: i.java:1389 (proven) — al > k.P + 240 → l(12).
        // Fires when the player falls past where the clamped camera can follow.
        else if (player.al > camY + VIEW_H) stateL(12)

        tickIndex++; jG++
        } finally {
            lastTouchX = -1; lastTouchY = -1     // k.H/k.I live one frame
        }
    }

    // Second init block: runs after every property initializer, so the
    // C() init `m(ad)` snap (k.java:2343) sees kAe/kAd in their set state.
    init {
        kM(2)
    }
}


// Camera-state sets for k.m (k.java:2364-2545, proven):
// cA centered-anim list (S60-62/148-150/210/59/65/258-266 + g.j + g.a-51 + S38-ac22)
private val CAM_CENTER_STATES = intArrayOf(
    60, 61, 62, 148, 149, 150, 210, 59, 65,
    258, 259, 260, 261, 262, 263, 264, 265, 266)
// cB hang-look-down list (L158 → +60) and centered list (L199 → -120)
private val CAM_B_DOWN_STATES = intArrayOf(28, 29, 315, 318)
private val CAM_B_CENTER_STATES = intArrayOf(
    148, 149, 150, 210, 59, 65,
    258, 259, 260, 261, 262, 263, 264, 265, 266)
