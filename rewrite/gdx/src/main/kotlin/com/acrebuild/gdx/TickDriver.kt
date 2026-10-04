package com.acrebuild.gdx

import com.acrebuild.core.Command
import com.acrebuild.core.InputQueue

/**
 * The tick-failure boundary (design `docs/modern-mobile-technical-design.md`
 * §8): one committed tick per call, its deferred commands handed back for the
 * adapters. The first exception out of a tick quarantines the world for good —
 * no further ticks, that tick's half-built commands are dropped (no
 * `PersistBA`, no audio), and [onFatal] runs once so the app can stop audio
 * and show the fatal screen. Catch-and-continue would keep simulating a world
 * whose invariants just broke, and could persist it.
 */
class TickDriver(
    private val tick: (List<InputQueue.Event>) -> Unit,
    private val drainCommands: () -> List<Command>,
    private val onFatal: (Throwable) -> Unit,
) {
    var quarantined = false
        private set
    var failure: Throwable? = null
        private set

    /** Run one tick with [events]; returns the commands to execute — empty
     *  once quarantined, and for the tick that failed. */
    fun tick(events: List<InputQueue.Event>): List<Command> {
        if (quarantined) return emptyList()
        try {
            tick.invoke(events)
        } catch (t: Throwable) {
            quarantined = true
            failure = t
            runCatching { drainCommands() }          // drop the failed tick's commands
            onFatal(t)
            return emptyList()
        }
        return drainCommands()
    }

    /** Commands queued outside a tick (lifecycle hooks); none once quarantined. */
    fun drainIdle(): List<Command> = if (quarantined) emptyList() else drainCommands()
}
