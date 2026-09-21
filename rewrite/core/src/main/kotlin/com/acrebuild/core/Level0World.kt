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

    val pad = Pad()
    val playerFsm = PlayerFsm(this)
    val npcFsm = NpcFsm(this)

    var tickIndex: Long = 0L
        private set

    val player = Entity(0, clips[0]).apply { aw = -1 }
    val npcs = ArrayList<Entity>()
    var camX = 0
        private set
    var camY = 0
        private set

    init {
        val spawn = level.playerSpawn() ?: (100 to 200)
        player.setPositionPx(spawn.first, spawn.second)
        player.setAnim(0)
        for (f in level.entities) {
            if (f.size < 7) continue
            val type = f[0]
            val clipIdx = ENTITY_CLIP[type] ?: continue
            val e = Entity(type, clips[clipIdx]).apply {
                aw = f[1]
                setPositionPx(f[2], f[3])
                P = f[6]
                av = (f[6] and 1) != 0
            }
            if (type == 11) npcFsm.initSoldier(e, f.toList())
            else for (i in e.Z.indices) if (7 + i < f.size) e.Z[i] = f[7 + i]
            npcs += e
        }
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

        player.collideSides(this, true)
        playerFsm.tick(player, pad)
        player.integrate()
        player.advanceAnim()

        for (n in npcs) npcFsm.tick(n, player)

        camX = (player.ak - VIEW_W / 2).coerceIn(0, (level.worldW - VIEW_W).coerceAtLeast(0))
        camY = (player.al - VIEW_H * 2 / 3).coerceIn(0, (level.worldH - VIEW_H).coerceAtLeast(0))
        tickIndex++
    }
}
