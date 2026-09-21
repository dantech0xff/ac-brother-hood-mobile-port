package com.acrebuild.core

/**
 * J2ME pad-word semantics for the port: the original reads three query
 * functions on a bitmask — `k.u(mask)` held, `k.v(mask)` pressed this tick,
 * `k.x(mask)` double-tap (second press inside the `k.aA` window).
 *
 * Mask values mined from `player-mechanics.md` / g.java call sites:
 *   4112  = d-pad LEFT held      8256  = d-pad RIGHT held
 *   16388 = UP/action held       33024 = DOWN held
 *   2     = tap-left edge         8     = tap-right edge
 *   16398 = action-family edge (16388|2|8)
 *   128/512 appear in `l()` as extra held-bits for L/R (diagonal/alt keys).
 *   94324 = "any direction held" group in the S5 arm.
 *
 * `aA` is the double-tap window counter (k.aA in the original): armed on
 * release, decremented per tick, ~8 ticks (inferred).
 */
class Pad {
    var held = 0         // bits currently down
    var edge = 0         // bits that went down this tick
    var tap = 0          // bits qualifying as double-tap this tick
    var aA = 0           // double-tap window (k.aA)

    private var pendingPress = 0

    /** Queue a discrete press for the next commit (zone DOWN events). */
    fun queuePress(mask: Int) { pendingPress = pendingPress or mask }

    /** Call once per tick after collecting raw input for the tick. */
    fun commit(nextHeld: Int) {
        val down = nextHeld and held.inv()
        edge = down or pendingPress
        tap = if (aA > 0) edge else 0
        aA = if (held != 0 && nextHeld == 0) 8 else if (edge == 0 && aA > 0) aA - 1 else aA
        pendingPress = 0
        held = nextHeld
    }

    fun u(mask: Int): Boolean = held and mask != 0
    fun v(mask: Int): Boolean = edge and mask != 0
    fun x(mask: Int): Boolean = tap and mask != 0

    companion object {
        const val M_LEFT = 4112
        const val M_LEFT_ALT = 128
        const val M_RIGHT = 8256
        const val M_RIGHT_ALT = 512
        const val M_UP = 16388
        const val M_DOWN = 33024
        const val M_TAP_L = 2
        const val M_TAP_R = 8
        const val M_ACTION_FAMILY = 16398
        const val M_ANY_DIR = 94324
        /** Context/attack key (proven: `v(65568)` drives combos + assassinate). */
        const val M_CONTEXT = 65568
    }
}
