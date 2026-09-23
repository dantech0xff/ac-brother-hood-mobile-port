package com.acrebuild.core

import java.security.MessageDigest

/**
 * Fixed-step driver: 62 ms ticks through an accumulator, at most
 * [MAX_CATCHUP] catch-up ticks per advance, backlog dropped on overflow.
 * Each accepted tick produces one immutable [CommittedTick] published through
 * a single barrier — if [SpikeWorld.tick] throws, nothing is published and the
 * engine is permanently quarantined (design doc "Tick failure boundary").
 */
class TickEngine(
    val world: SpikeWorld,
    private val inputQueue: InputQueue,
) {
    companion object {
        const val TICK_MS = 62L
        const val MAX_CATCHUP = 4
    }

    private var accumulatorMs = 0L
    private var quarantined = false

    var droppedBacklog = 0L
        private set

    fun isQuarantined(): Boolean = quarantined

    /** Returns the ticks committed by this advance (0..MAX_CATCHUP). */
    fun advance(deltaMs: Long): List<CommittedTick> {
        if (quarantined) return emptyList()
        accumulatorMs += deltaMs
        val due = accumulatorMs / TICK_MS
        val toRun = minOf(due, MAX_CATCHUP.toLong())
        // Due ticks beyond MAX_CATCHUP are dropped, not replayed — dropping the
        // backlog is the spiral-of-death guard, so all due time is consumed.
        if (due > MAX_CATCHUP) droppedBacklog += due - MAX_CATCHUP
        accumulatorMs -= due * TICK_MS
        if (toRun == 0L) return emptyList()

        // Drain input once per advance to a fixed cutoff — no event can land
        // mid-batch and retroactively affect an already-committed tick.
        val events = inputQueue.drainTo(inputQueue.headSequence())
        val out = ArrayList<CommittedTick>(toRun.toInt())
        for (i in 0 until toRun) {
            // Drained events are consumed by the first committed tick only —
            // replaying them into every catch-up tick would apply each
            // down/up multiple times.
            val tick = runTick(if (i == 0L) events else emptyList())
            if (tick == null) {
                quarantined = true
                return out // publish only what fully committed before failure
            }
            out.add(tick)
        }
        return out
    }

    /** Resets the accumulator; used on resume so suspended time never replays. */
    fun resetAccumulator() {
        accumulatorMs = 0L
    }

    /** Restores world+engine state from a durable snapshot. */
    fun restore(s: SaveSnapshot) {
        world.restore(s)
        accumulatorMs = 0L
    }

    fun currentTickIndex(): Long = world.tickIndex

    private fun runTick(events: List<InputQueue.Event>): CommittedTick? {
        val commands = ArrayList<Command>()
        return try {
            val index = world.tickIndex
            world.tick(events) { commands.add(it) }
            if (world.consumeBounce()) commands.add(Command.PlaySfx(SpikeWorld.SFX_SLOT_BOUNCE))
            CommittedTick(index, world.snapshot(), hashOf(index, commands), commands)
        } catch (t: Throwable) {
            null
        }
    }

    private fun hashOf(tickIndex: Long, commands: List<Command>): String {
        val s = world.snapshot()
        val md = MessageDigest.getInstance("SHA-256")
        putLong(md, tickIndex)
        putInt(md, s.posX); putInt(md, s.posY)
        putInt(md, s.velX); putInt(md, s.velY)
        md.update(if (s.touchHeld) 1 else 0)
        putLong(md, s.rngState)
        for (c in commands) {
            when (c) {
                is Command.PlaySfx -> { md.update(1); putInt(md, c.slot) }
                is Command.PersistBA -> { md.update(2); md.update(c.record) }
                is Command.RequestSave -> { md.update(2); putInt(md, c.snapshot.posX) }
                is Command.QuitApp -> md.update(4)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }

    private fun putInt(md: MessageDigest, v: Int) {
        md.update((v ushr 24).toByte()); md.update((v ushr 16).toByte())
        md.update((v ushr 8).toByte()); md.update(v.toByte())
    }

    private fun putLong(md: MessageDigest, v: Long) {
        for (i in 7 downTo 0) md.update((v ushr (i * 8)).toByte())
    }
}

data class CommittedTick(
    val tickIndex: Long,
    val snapshot: SaveSnapshot,
    val hashHex: String,
    val commands: List<Command>,
)
