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
        val ENTITY_CLIP = mapOf(
            11 to 7, 17 to 7, 23 to 7, 47 to 7, 50 to 7, 73 to 7,
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

    val player = Entity(0, clips[0]).apply { aw = -1 }
    override val npcs = ArrayList<Entity>()
    var camX = 0
        private set
    var camY = 0
        private set

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
    }

    private fun spawnEntities() {
        npcs.clear()
        lockTarget = null
        for (f in level.entities) {
            if (f.size < 7) continue
            val type = f[0]
            val clipIdx = ENTITY_CLIP[type] ?: continue
            val e = Entity(type, clips[clipIdx]).apply {
                aw = f[1]
                setPositionPx(f[2], f[3])
                homeX = f[2]; homeY = f[3]
                P = f[6]
                av = (f[6] and 1) != 0
            }
            if (type == 11) npcFsm.initSoldier(e, f.toList())
            else for (i in e.Z.indices) if (7 + i < f.size) e.Z[i] = f[7 + i]
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
                n.setAnim(0)
            }
        }
    }

    private fun reload() {
        resetPlayerToSpawn()
        spawnEntities()
        failed = false
    }

    // -- input ------------------------------------------------------------

    private var pointerDown = false
    private var zoneMask = 0

    /** Raw InputQueue events are screen px in the 400x240 view. */
    private fun consume(events: List<InputQueue.Event>) {
        for (e in events) {
            when (e.type) {
                InputQueue.Type.DOWN -> {
                    pointerDown = true
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

        for (n in npcs) npcFsm.tick(n, player)
        fireCheckpoints()

        camX = (player.ak - VIEW_W / 2).coerceIn(0, (level.worldW - VIEW_W).coerceAtLeast(0))
        camY = (player.al - VIEW_H * 2 / 3).coerceIn(0, (level.worldH - VIEW_H).coerceAtLeast(0))

        // knockout: d() → x[1]<=0 → k.l(12) (proven)
        if (player.x1 <= 0) missionFail()
        // below camera bottom: i.java:1389 (proven) — al > k.P + 240 → l(12).
        // Fires when the player falls past where the clamped camera can follow.
        else if (player.al > camY + VIEW_H) missionFail()

        tickIndex++
    }
}
