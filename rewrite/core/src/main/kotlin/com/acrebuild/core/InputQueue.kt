package com.acrebuild.core

/**
 * Thread-safe monotonic sequenced input queue. Platform callbacks append
 * immediately; the core drains events up to a sequence cutoff and consumes
 * them at tick boundary. Render polling never touches gameplay state.
 * (design doc section 8 "Input latency")
 */
class InputQueue {

    enum class Type { DOWN, MOVE, UP, CANCEL }

    data class Event(val sequence: Long, val type: Type, val x: Int, val y: Int)

    private val lock = Any()
    private val events = ArrayList<Event>(64)
    private var nextSequence = 0L

    /** Called from platform input callbacks; returns the assigned sequence. */
    fun post(type: Type, x: Int, y: Int): Long = synchronized(lock) {
        val seq = nextSequence++
        events.add(Event(seq, type, x, y))
        seq
    }

    /** Highest sequence currently visible; the tick cutoff. */
    fun headSequence(): Long = synchronized(lock) { nextSequence }

    /** Atomically removes and returns every event with sequence < cutoff. */
    fun drainTo(cutoff: Long): List<Event> = synchronized(lock) {
        val out = ArrayList<Event>(events.size)
        val it = events.iterator()
        while (it.hasNext()) {
            val e = it.next()
            if (e.sequence < cutoff) {
                out.add(e)
                it.remove()
            }
        }
        out
    }
}
