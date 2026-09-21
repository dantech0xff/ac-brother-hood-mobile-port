package com.acrebuild.core

/**
 * Minimal deterministic world for the toolchain spike: one actor on the
 * recovered 400x240 logical field, 8.8 fixed-point integration, touch steer,
 * bounce, and ordered deferred commands. Gameplay state mutates only inside
 * [tick].
 */
class SpikeWorld(val rng: DeterministicRandom) {

    companion object {
        const val FIELD_W = 400
        const val FIELD_H = 240

        // Actor bounds in pixels (the converted sprite is 83x155).
        const val ACTOR_W = 83
        const val ACTOR_H = 155

        const val ACCEL = 40          // fixed per-tick acceleration (8.8)
        const val FRICTION = 24       // fixed per-tick decay when no touch
        const val MAX_SPEED = FixedPoint.SCALE * 5 // 5 px/tick in fixed
        const val SFX_SLOT_TOUCH = 0
        const val SFX_SLOT_BOUNCE = 1
    }

    var tickIndex: Long = 0L
        private set
    var posX: Int = FixedPoint.toFixed(FIELD_W / 2)
        private set
    // Spawn low-center so the actor stands clear of the title art on top.
    var posY: Int = FixedPoint.toFixed(FIELD_H - ACTOR_H / 2 - 6)
        private set
    var velX: Int = 0
        private set
    var velY: Int = 0
        private set
    var touchHeld: Boolean = false
        private set
    var touchX: Int = FIELD_W / 2
        private set
    var touchY: Int = FIELD_H / 2
        private set

    fun tick(events: List<InputQueue.Event>, sink: (Command) -> Unit) {
        for (e in events) {
            when (e.type) {
                InputQueue.Type.DOWN -> {
                    touchHeld = true
                    touchX = e.x
                    touchY = e.y
                    sink(Command.PlaySfx(SFX_SLOT_TOUCH))
                }
                InputQueue.Type.MOVE -> {
                    touchX = e.x
                    touchY = e.y
                }
                InputQueue.Type.UP, InputQueue.Type.CANCEL -> touchHeld = false
            }
        }

        if (touchHeld) {
            val dx = FixedPoint.toFixed(touchX) - posX
            val dy = FixedPoint.toFixed(touchY) - posY
            velX = clampSpeed(velX + if (dx > 0) ACCEL else if (dx < 0) -ACCEL else 0)
            velY = clampSpeed(velY + if (dy > 0) ACCEL else if (dy < 0) -ACCEL else 0)
        } else {
            velX = decay(velX)
            velY = decay(velY)
        }

        // Deterministic drift keeps the RNG inside the hashed state.
        if ((tickIndex and 0x1FL) == 0L) {
            velX = clampSpeed(velX + rng.nextRange(-ACCEL, ACCEL + 1))
            velY = clampSpeed(velY + rng.nextRange(-ACCEL, ACCEL + 1))
        }

        posX += velX
        posY += velY
        bounce()

        tickIndex++
    }

    private fun decay(v: Int): Int = when {
        v > FRICTION -> v - FRICTION
        v < -FRICTION -> v + FRICTION
        else -> 0
    }

    private fun clampSpeed(v: Int): Int = v.coerceIn(-MAX_SPEED, MAX_SPEED)

    private fun bounce() {
        val loX = FixedPoint.toFixed(ACTOR_W / 2)
        val hiX = FixedPoint.toFixed(FIELD_W - ACTOR_W / 2)
        val loY = FixedPoint.toFixed(ACTOR_H / 2)
        val hiY = FixedPoint.toFixed(FIELD_H - ACTOR_H / 2)
        var bounced = false
        if (posX < loX) { posX = loX; velX = -velX / 2; bounced = true }
        if (posX > hiX) { posX = hiX; velX = -velX / 2; bounced = true }
        if (posY < loY) { posY = loY; velY = -velY / 2; bounced = true }
        if (posY > hiY) { posY = hiY; velY = -velY / 2; bounced = true }
        if (bounced) pendingBounce = true
    }

    // Bounce commands are collected by TickEngine via drainBounceFlag.
    private var pendingBounce = false
    internal fun consumeBounce(): Boolean {
        val b = pendingBounce
        pendingBounce = false
        return b
    }

    fun snapshot(): SaveSnapshot =
        SaveSnapshot(tickIndex, posX, posY, velX, velY, touchHeld, touchX, touchY, rng.state())

    fun restore(s: SaveSnapshot) {
        tickIndex = s.tickIndex
        posX = s.posX
        posY = s.posY
        velX = s.velX
        velY = s.velY
        touchHeld = s.touchHeld
        touchX = s.touchX
        touchY = s.touchY
        rng.restore(s.rngState)
    }
}
