package com.acrebuild.core

/**
 * Slice-1 world: level 0 (pack-6) tiles + entities + the player locomotion
 * subset, driven at the original 62 ms tick.
 *
 * Ported semantics (confidence labels per repo convention):
 * - `k.g` collision: OOB -> 20 solid sentinel; cell >= 12 && != 255 = solid
 *   (proven); {2,3,5,18} = one-way landing markers (high-confidence).
 * - Player physics literals (proven): run cap ±2560, terminal fall 5120,
 *   gravity 1536/tick, jump impulse -5120.
 * - Player states map 1:1 to clip-0 anims (clip-as-FSM, proven). Which anim
 *   is idle/run/jump/fall is `inferred` for this slice and tuned visually.
 * - Input: proven masks are keypad-derived; the touch bridge maps
 *   hold-left/right zones + tap-upper to LEFT/RIGHT/JUMP.
 */
class Level0World(
    val level: LevelPack,
    val clips: Map<Int, Clip>,
    val rng: DeterministicRandom,
) {

    companion object {
        const val VIEW_W = 400
        const val VIEW_H = 240

        // Proven literals (player-mechanics.md / i.java:3887+).
        const val RUN_CAP = 2560
        const val TERMINAL = 5120
        const val GRAVITY = 1536
        const val JUMP_VEL = -5120
        const val RUN_ACCEL = 768

        // Entity type -> clip index (k.bi[]; only decoded clips carried).
        val ENTITY_CLIP = mapOf(
            11 to 7, 17 to 7, 23 to 7, 47 to 7, 50 to 7, 73 to 7,
        )

        // Player slice states == clip-0 anim indices (inferred picks).
        const val S_IDLE = 0
        const val S_RUN = 15
        const val S_JUMP = 22
        const val S_FALL = 24
    }

    var tickIndex: Long = 0L
        private set

    val player = Entity(0, clips[0]).apply { aw = -1 }
    val npcs = ArrayList<Entity>()
    var camX = 0
        private set
    var camY = 0
        private set

    private val rectScratch = IntArray(4)

    init {
        val spawn = level.playerSpawn() ?: (100 to 200)
        player.setPositionPx(spawn.first, spawn.second)
        for (f in level.entities) {
            if (f.size < 7) continue
            val type = f[0]
            val clipIdx = ENTITY_CLIP[type] ?: continue
            val e = Entity(type, clips[clipIdx]).apply {
                aw = f[1]
                setPositionPx(f[2], f[3])
                P = f[6]
                av = (f[6] and 1) != 0
                for (i in Z.indices) if (7 + i < f.size) Z[i] = f[7 + i]
            }
            npcs += e
        }
    }

    // -- input ------------------------------------------------------------

    private var held = false
    private var holdX = 0
    private var holdY = 0
    private var jumpQueued = false

    /** Raw InputQueue events are screen px in the 400x240 view. */
    private fun consume(events: List<InputQueue.Event>) {
        for (e in events) {
            when (e.type) {
                InputQueue.Type.DOWN -> {
                    if (e.y < VIEW_H / 3) jumpQueued = true
                    else { held = true; holdX = e.x; holdY = e.y }
                }
                InputQueue.Type.MOVE -> if (held) { holdX = e.x; holdY = e.y }
                InputQueue.Type.UP, InputQueue.Type.CANCEL -> held = false
            }
        }
    }

    // -- sim --------------------------------------------------------------

    fun tick(events: List<InputQueue.Event>) {
        consume(events)
        tickPlayer()
        for (n in npcs) n.advanceAnim()
        player.advanceAnim()
        camX = (player.ak - VIEW_W / 2).coerceIn(0, (level.worldW - VIEW_W).coerceAtLeast(0))
        camY = (player.al - VIEW_H * 2 / 3).coerceIn(0, (level.worldH - VIEW_H).coerceAtLeast(0))
        tickIndex++
    }

    private fun onGround(): Boolean {
        playerHitbox(0)
        val feet = player.al + rectScratch[1] + rectScratch[3]
        val x0 = player.ak + rectScratch[0]
        val x1 = x0 + rectScratch[2] - 1
        var cx = x0 / level.cellPx
        while (cx <= x1 / level.cellPx) {
            val v = level.collisionCell(cx, feet / level.cellPx)
            if (level.isSolid(v) || level.isOneWay(v)) return true
            cx++
        }
        return false
    }

    /** `aa.a(S,T,0,W,P&7)` — hitbox in entity-anchor space. */
    private fun playerHitbox(which: Int): IntArray {
        val c = player.clip
        if (c == null || player.S >= c.animCount()) {
            rectScratch.fill(0)
        } else {
            c.rect(player.S, player.T, which, player.drawFlags(), rectScratch)
        }
        return rectScratch
    }

    private fun tickPlayer() {
        val p = player
        val groundedBefore = onGround()

        // --- horizontal intent ---
        val wantDir = when {
            !held -> 0
            holdX < VIEW_W / 2 -> -1
            else -> 1
        }
        val grounded = groundedBefore && p.ah >= 0

        if (grounded) {
            if (wantDir != 0) {
                p.av = wantDir < 0
                if (p.av) p.P = p.P or 1 else p.P = p.P and -2
                if (p.S != S_RUN) p.setAnim(S_RUN)
                p.ag = approach(p.ag, wantDir * RUN_CAP, RUN_ACCEL)
            } else {
                p.ag = 0
                if (p.S != S_IDLE) p.setAnim(S_IDLE)
            }
            if (jumpQueued) {
                p.ah = JUMP_VEL
                p.setAnim(S_JUMP)
            }
        } else {
            // air drift: held direction still steers (half accel — inferred).
            if (wantDir != 0) p.ag = approach(p.ag, wantDir * RUN_CAP, RUN_ACCEL / 2)
        }
        jumpQueued = false

        // --- vertical ---
        if (!grounded || p.ah < 0) {
            p.aj = GRAVITY
            if (p.ah + p.aj > TERMINAL) p.ah = TERMINAL - p.aj
        }

        val prevFeet = p.al + playerHitboxBottom()
        p.integrate()
        resolveCollisions(prevFeet)

        if (!groundedBefore && onGround() && p.ah == 0) {
            p.setAnim(if (wantDir != 0) S_RUN else S_IDLE)
        } else if (!onGround() && p.S != S_JUMP && p.S != S_FALL) {
            p.setAnim(S_FALL)
        }
    }

    private fun playerHitboxBottom(): Int {
        playerHitbox(0)
        return rectScratch[1] + rectScratch[3]
    }

    private fun resolveCollisions(prevFeet: Int) {
        val p = player
        playerHitbox(0)
        val hx = rectScratch[0]; val hy = rectScratch[1]
        val hw = rectScratch[2]; val hh = rectScratch[3]
        val feet = p.al + hy + hh
        val prevFeetAbs = prevFeet
        val x0 = p.ak + hx; val x1 = x0 + hw - 1

        // landing: feet crossed a solid/one-way cell top this tick
        if (p.ah >= 0) {
            val cy = feet / level.cellPx
            var cx = x0 / level.cellPx
            var landed = false
            while (cx <= x1 / level.cellPx) {
                val v = level.collisionCell(cx, cy)
                val top = cy * level.cellPx
                val crossing = feet >= top && prevFeetAbs <= top + level.cellPx
                if (crossing && (level.isSolid(v) || (level.isOneWay(v) && p.ah >= 0))) {
                    landed = true
                    break
                }
                cx++
            }
            if (landed) {
                p.al = cy * level.cellPx - (hy + hh)
                p.O = p.al shl 8
                p.ah = 0
                p.aj = 0
            }
        }

        // horizontal walls (solid only)
        playerHitbox(0)
        val wy0 = p.al + rectScratch[1]
        val wy1 = wy0 + rectScratch[3] - 1
        val wx0 = p.ak + rectScratch[0]
        val wx1 = wx0 + rectScratch[2] - 1
        if (p.ag > 0) {
            val cx = wx1 / level.cellPx
            var cy = wy0 / level.cellPx
            while (cy <= wy1 / level.cellPx) {
                if (level.isSolid(level.collisionCell(cx, cy))) {
                    p.ak = cx * level.cellPx - rectScratch[0] - rectScratch[2]
                    p.N = p.ak shl 8
                    p.ag = 0
                    break
                }
                cy++
            }
        } else if (p.ag < 0) {
            val cx = wx0 / level.cellPx
            var cy = wy0 / level.cellPx
            while (cy <= wy1 / level.cellPx) {
                if (level.isSolid(level.collisionCell(cx, cy))) {
                    p.ak = (cx + 1) * level.cellPx - rectScratch[0]
                    p.N = p.ak shl 8
                    p.ag = 0
                    break
                }
                cy++
            }
        }

        // ceiling
        if (p.ah < 0) {
            val cy = wy0 / level.cellPx
            var cx = wx0 / level.cellPx
            while (cx <= wx1 / level.cellPx) {
                if (level.isSolid(level.collisionCell(cx, cy))) {
                    p.al = (cy + 1) * level.cellPx - rectScratch[1]
                    p.O = p.al shl 8
                    p.ah = 0
                    break
                }
                cx++
            }
        }
    }

    private fun approach(v: Int, target: Int, step: Int): Int = when {
        v < target -> (v + step).coerceAtMost(target)
        v > target -> (v - step).coerceAtLeast(target)
        else -> v
    }
}
