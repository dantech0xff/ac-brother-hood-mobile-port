package com.acrebuild.core

/**
 * java.util.Random LCG parity implementation. The recovered helper
 * `j.a(min,max)` draws with unbounded nextInt(), negates via 32-bit overflow
 * when negative, then applies modulo (max-min). This class keeps that contract
 * exactly, including the Integer.MIN_VALUE quirk where negation stays negative
 * and the modulo result can land below `min`.
 *
 * State is the full 48-bit LCG seed, not the original seed argument, so save
 * and hash capture resume exactly.
 */
class DeterministicRandom(seed: Long) {

    private var state: Long = (seed xor MULTIPLIER) and MASK

    private fun next(bits: Int): Int {
        state = (state * MULTIPLIER + ADDEND) and MASK
        return (state ushr (48 - bits)).toInt()
    }

    fun nextInt(): Int = next(32)

    /** Parity port of legacy `j.a(min, max)`. */
    fun nextRange(min: Int, max: Int): Int {
        if (min == max) return min
        var draw = nextInt()
        if (draw < 0) draw = -draw // MIN_VALUE stays negative on purpose.
        return min + draw % (max - min)
    }

    fun state(): Long = state

    fun restore(newState: Long) {
        state = newState and MASK
    }

    private companion object {
        const val MULTIPLIER = 0x5DEECE66DL
        const val ADDEND = 0xBL
        const val MASK = (1L shl 48) - 1
    }
}
