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
            10 to 6,      // clip6 not converted yet — triggers spawn clipless
            14 to 9,      // ax14 pickups/markers (bi[14]=9; L88 record arm)
            16 to 10,     // ax16 request markers (bi[16]=10; bb() S30/38/39)
            71 to 26,     // generic a(ax) spawner pickups (bi[71]=26)
        )
    }

    override val cellPx: Int get() = level.cellPx
    override fun collisionCell(cx: Int, cy: Int): Int = level.collisionCell(cx, cy)
    override fun isSolid(v: Int): Boolean = level.isSolid(v)
    override fun isOneWay(v: Int): Boolean = level.isOneWay(v)
    override var lockTarget: Entity? = null

    val pad = Pad()
    val playerFsm = PlayerFsm(this, rng)
    val npcFsm = NpcFsm(this)

    var tickIndex: Long = 0L
        private set
    var deaths = 0                     // mission-fail count (instrumentation)
    var failed = false                 // j.c==12 mission-fail screen active
        private set

    /** ax2 checkpoint record (i.java:13477 aY). `aw` = record id. */
    data class Checkpoint(val aw: Int, val ak: Int, val al: Int, var consumed: Boolean = false)

    /** Snapshot written into bA[16..] by aY() — subset we model. */
    data class Snapshot(val ak: Int, val al: Int, val av: Boolean, val x1: Int)

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
    override val apStats = IntArray(16)  // k.ap — per-slot counters (k.o)
    override var shake = 0           // k.az side of k.s() (k.java:5338;
                                     // the dE threshold ladder is unported)
    override val sfxLog = mutableListOf<Int>()  // k.A(int) — audio unported
    override fun sfx(id: Int) { sfxLog += id }
    override fun shake() { shake++ } // k.s()

    /** `k.O` — camera left edge (the pickup pin anchor, i.java:9837). */
    override val kO: Int get() = camX
    /** `k.P` — camera top edge (u()'s view-center operand). */
    override val kP: Int get() = camY
    /** `k.ac` — camera view rect [x1,y1,x2,y2] (v() on-screen check). */
    override val camRect: IntArray get() =
        intArrayOf(camX, camY, camX + VIEW_W, camY + VIEW_H)
    /** `k.bh[k.aj]==3` — gameplay phase (mission-fail screen is phase 12). */
    override val inPlay: Boolean get() = !failed
    /** `k.cm` — mounted flag (k.k() at k.java:638; `cm=true` writes 1 at
     *  g.java:3564). */
    var cm = 0
    override val mounted: Boolean get() = cm == 1
    override fun setMounted() { cm = 1 }
    /** `k.H`/`k.I` — the last touch point in view px (-1 = none). */
    var lastTouchX = -1
    var lastTouchY = -1
    override fun clipFor(idx: Int): Clip? = clips[idx]
    /** `k.a(k.H,k.I, e.ak-k.O, e.al-k.P, r)` (k.java:627): touch point vs
     *  entity in view space — equivalent to world-space vs (k.H+k.O). */
    override fun touchNearView(e: Entity, r: Int): Boolean =
        lastTouchX >= 0 &&
            e.h(Math.abs(lastTouchX + camX - e.ak), Math.abs(lastTouchY + camY - e.al)) <= r
    /** `k.aS.W` — player hitbox. */
    override fun playerRect(): IntArray = player.W

    /** `k.q(o)` (k.java:5887, proven): resolve a linked entity by `aw` —
     *  `aS.aw==o → aS` else the `bb[]` scan (our npcs list). */
    override fun findByAw(aw: Int): Entity? =
        if (player.aw == aw) player else npcs.firstOrNull { it.aw == aw }

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
    override var aeRef: Entity? = null          // k.ae
    override var vehicle: Entity? = null        // g.a
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
        lastTouchX in x until x + w && lastTouchY in y until y + h

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

    var camX = 0
        private set
    var camY = 0
        private set

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
        lockTarget = null
        clearClaim()
        marker = null; markerTag = -1
        for (f in level.entities) {
            if (f.size < 7) continue
            val type = f[0]
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
    }

    /**
     * `k.l(12)` mission fail (i.java:1389 proven — player below the camera
     * bottom, or `d()` knockout with x[1]<=0): the original swaps to the
     * j.c=12 fail screen; confirm (`v(65568)`, k.java:1804) then runs
     * `f(false)` = full level reload. Ported as `failed` + reload().
     */
    private fun missionFail() {
        if (!failed) { failed = true; deaths++ }
    }

    /** `k.l(15)` mission-complete — ported as a flag (screen flow
     *  `inferred`, unmined). */
    var missionWon = false
    override fun missionComplete() { missionWon = true }

    // -- ax21 director statics (i.bD, i.java:72-184 + k fields) --------------
    override var iBV = 0                       // i.bV — script-written
    override var iBU = 0                       // i.bU — gauge cap
    override var iBW = false                   // i.bW — pending transition
    override var iBX = 0                       // i.bX
    override var iBT = false                   // i.bT — director armed
    override var iBj = false                   // i.bj — finale freeze
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
    override var kX = 0                        // k.X
    override var kW = 0                        // k.W
    override var kAw = 0                       // k.aw
    override var kAm = false                   // k.am
    override var kDd = false                   // k.dd
    override var gR = false                    // g.r — grab-QTE lock
    override var kBj = 0                       // k.bJ — grab-QTE lose latch
    /** `k.J`/`k.K` — the held touch point; port aliases the last DOWN
     *  point `lastTouchX/Y` (inferred — J2ME tracks them separately). */
    override var kJ: Int get() = lastTouchX; set(v) { lastTouchX = v }
    override var kK: Int get() = lastTouchY; set(v) { lastTouchY = v }
    override val kAc: IntArray? = null         // k.ac[] — unmined
    override fun padHeld(mask: Int): Boolean = pad.v(mask)
    override fun padDown(mask: Int): Boolean = pad.u(mask)
    override var gj = false                        // g.j context latch
    override var kL: Entity? = null                // k.L claim entity
    override var claimCo = 6                       // k.co
    override var claimRect: IntArray? = null       // k.cp

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
            checkpointSnap = Snapshot(cp.ak, cp.al, player.av, player.x1)
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

    private fun reload() {
        resetPlayerToSpawn()
        spawnEntities()
        failed = false
    }

    /**
     * ax37 `al()` (i.java:7053) — scroll-bound trigger.
     * mode==1 (Z[3]): fire while player W overlaps the zone (`a()`);
     * mode==0: fire when player W is fully inside (`b()` = contained).
     * Payload per Z[0] bits, each gated on zone∩viewport `a(this.W,k.ac)`:
     * &1→k.R=X[0] (camX floor), &4→k.T=X[1] (camY floor),
     * &2→k.S=X[2] (camX+400 ceiling), &8→k.U=X[3] (camY+240 ceiling).
     * Z[2]==-1 records skip the linked-entity gate (all level-0 data).
     * `k.ah`/`k.n()` context registration and bound-reset-on-snap
     * (k.java:2447-2449) unported — bounds persist until overwritten
     * (inferred: level-0 data only ever expands the bound forward).
     */
    private fun fireScrollTriggers() {
        val view = intArrayOf(camX, camY, camX + VIEW_W, camY + VIEW_H)
        for (t in scrollTriggers) {
            val pw = player.W
            val fired = if (t.mode == 1) rectsOverlap(pw, t.zone)
                        else rectContains(pw, t.zone)
            if (!fired) continue
            if (!rectsOverlap(t.zone, view)) continue
            if (t.mask and 1 != 0) boundMinX = t.bound[0]
            if (t.mask and 4 != 0) boundMinY = t.bound[1]
            if (t.mask and 2 != 0) boundMaxX = t.bound[2]
            if (t.mask and 8 != 0) boundMaxY = t.bound[3]
        }
    }

    private fun rectsOverlap(a: IntArray, b: IntArray) =
        a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1]

    private fun rectContains(inner: IntArray, outer: IntArray) =
        inner[0] >= outer[0] && inner[1] >= outer[1] &&
        inner[2] <= outer[2] && inner[3] <= outer[3]

    // -- input ------------------------------------------------------------

    private var pointerDown = false
    private var zoneMask = 0

    /** Raw InputQueue events are screen px in the 400x240 view. */
    private fun consume(events: List<InputQueue.Event>) {
        for (e in events) {
            when (e.type) {
                InputQueue.Type.DOWN -> {
                    pointerDown = true
                    lastTouchX = e.x; lastTouchY = e.y   // k.H/k.I
                    zoneMask = zoneFor(e.x, e.y)
                    when (zoneMask) {
                        Pad.M_LEFT -> pad.queuePress(Pad.M_TAP_L)
                        Pad.M_RIGHT -> pad.queuePress(Pad.M_TAP_R)
                        Pad.M_UP -> pad.queuePress(Pad.M_UP)
                        // bottom-third TAP = attack/context key 65568 edge;
                        // holding the zone still maps to DOWN (inferred)
                        Pad.M_DOWN -> pad.queuePress(Pad.M_CONTEXT)
                    }
                }
                InputQueue.Type.MOVE -> if (pointerDown) zoneMask = zoneFor(e.x, e.y)
                InputQueue.Type.UP, InputQueue.Type.CANCEL -> {
                    pointerDown = false; zoneMask = 0
                }
            }
        }
    }

    private fun zoneFor(x: Int, y: Int): Int = when {
        y >= VIEW_H * 2 / 3 -> Pad.M_DOWN
        y < VIEW_H / 3 -> Pad.M_UP
        x < VIEW_W / 2 -> Pad.M_LEFT
        else -> Pad.M_RIGHT
    }

    // -- sim --------------------------------------------------------------

    fun tick(events: List<InputQueue.Event>) {
        consume(events)
        pad.commit(if (pointerDown) zoneMask else 0)
        // k.F(aj) (k.java:4644): input events reset g.J to f0do[key]=5
        // (all 9 keys). ef[] is held-state per frame → set while held.
        // `inferred` on cadence; value 5 proven (k.java:8384).
        if (pointerDown) player.gJ = 5
        playerFsm.tickCount = tickIndex

        // mission-fail screen: world frozen; context edge = retry (reload)
        if (failed) {
            if (pad.v(Pad.M_CONTEXT)) reload()
            tickIndex++
            return
        }

        player.collideSides(this, true)
        playerFsm.tick(player, pad)
        player.integrate()
        player.advanceAnim()

        for (n in npcs) {
            if (n.ax == 44) npcFsm.tickDoor(n, player)
            else if (n.ax == 10) npcFsm.tickTrigger(n, player)
            else if (n.ax == 4) npcFsm.tickDestructible(n, player)
            else if (n.ax == 74) npcFsm.tickWisp(n, player)
            else if (n.ax == 67) npcFsm.tickDecor(n, player)
            else if (n.ax == 14) npcFsm.tickPickup(n, player)
            else if (n.ax == 16) npcFsm.tickRequestMarker(n, player, pad)
            else if (n.ax == 21) npcFsm.tickDirector(n, player, pad)
            else if (n.ax == 29) npcFsm.tickBoss(n, player, pad)
            else if (n.ax == 61) npcFsm.tickAx61(n, this, player)
            else if (n.ax == 41) npcFsm.tickKnockable(n, this, player)
            else if (n.ax == 66) npcFsm.tickPlatform(n, this, player)
            else npcFsm.tick(n, player)
        }
        if (pendingRemove.isNotEmpty()) {
            npcs.removeAll(pendingRemove)
            pendingRemove.clear()
        }
        if (pendingInsert.isNotEmpty()) {         // k.b(aK) drain
            npcs += pendingInsert
            pendingInsert.clear()
        }
        fireCheckpoints()
        fireScrollTriggers()

        camX = (player.ak - VIEW_W / 2).coerceIn(0, (level.worldW - VIEW_W).coerceAtLeast(0))
        camY = (player.al - VIEW_H * 2 / 3).coerceIn(0, (level.worldH - VIEW_H).coerceAtLeast(0))
        // ax37 scroll bounds (k.java:2430-2486 proven): camera target is
        // clamped inside [R, S-400]x[T, U-240] when each bound is set (>0).
        if (boundMinX > 0 && camX < boundMinX) camX = boundMinX
        if (boundMaxX > 0 && camX > boundMaxX - VIEW_W) camX = boundMaxX - VIEW_W
        if (boundMinY > 0 && camY < boundMinY) camY = boundMinY
        if (boundMaxY > 0 && camY > boundMaxY - VIEW_H) camY = boundMaxY - VIEW_H

        // knockout: d() → x[1]<=0 → k.l(12) (proven)
        if (player.x1 <= 0) missionFail()
        // below camera bottom: i.java:1389 (proven) — al > k.P + 240 → l(12).
        // Fires when the player falls past where the clamped camera can follow.
        else if (player.al > camY + VIEW_H) missionFail()

        tickIndex++
    }
}
