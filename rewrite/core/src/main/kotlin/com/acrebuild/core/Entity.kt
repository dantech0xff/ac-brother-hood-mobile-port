package com.acrebuild.core

/**
 * Port of the `i` entity skeleton: position, facing, clip binding, and the
 * `S/T/U` animation counters with the exact `i()`/`s()`/`r()`/`q()`
 * semantics from `i.java:240-330`.
 *
 * Field names keep the original single-letter names so FSM ports can be
 * transcribed literally:
 * - `ak`/`al` = integer pixel anchor; `N`/`O` = the same in 8.8 fixed.
 * - `ag`/`ah` = velocity (8.8); `ai`/`aj` = per-tick accel (8.8).
 * - `S` = current anim (= FSM state), `T` = frame, `U` = tick-in-frame,
 *   `Q` = previous anim, `P` = flags (bit0 = `av` facing, bit6 = anim-hold).
 * - `V` = playback delay counter; `a` = anim elapsed counter.
 */
open class Entity(val ax: Int, var clip: Clip?) {

    var aw: Int = 0                  // uid
    var ak: Int = 0                  // anchor x, px
    var al: Int = 0                  // anchor y, px
    var N: Int = 0                   // 8.8 x
    var O: Int = 0                   // 8.8 y
    var ag: Int = 0                  // vx
    var ah: Int = 0                  // vy
    var ai: Int = 0                  // axel x
    var aj: Int = 0                  // axel y
    var S: Int = 0                   // anim/state index
    var T: Int = 0                   // frame
    var U: Int = 0                   // tick-in-frame
    var Q: Int = 0                   // previous anim
    var P: Int = 0                   // flag word
    var av: Boolean = false          // facing (field6 bit0); true = mirrored
    var V: Int = 0                   // playback delay
    var a: Int = 0                   // anim elapsed ticks
    val Z: IntArray = IntArray(8)    // per-type params (fields[7+])

    /**
     * `i(n)` (`i.java:240`): set anim/state. Out-of-range indices are
     * rejected (state unchanged); when `n != S`: `Q = old S` (unless
     * `S == 35`), `T = U = a = 0`, `P &= ~64`.
     */
    fun setAnim(n: Int) {
        if (n < 0 || (clip != null && n >= clip!!.animCount())) return
        if (n != S) {
            if (S != 35) Q = S
            S = n
            T = 0
            U = 0
            a = 0
            P = P and -65
        }
    }

    /** `q()` — jump to the last frame. */
    fun jumpToLastFrame() {
        clip?.let { T = it.frameCount(S) - 1 }
    }

    /**
     * `r()` — true on the last tick of the last frame (`i.java:285`).
     * A zero-duration frame counts as finished immediately.
     */
    fun animFinished(): Boolean {
        val c = clip ?: return true
        if (T != c.frameCount(S) - 1) return false
        val dur = c.frameDuration(S, T)
        return dur == 0 || U == dur - 1
    }

    /**
     * `s()` — per-tick anim advance (`i.java:293`): blocked while
     * `P & 64` (hold), `U < 0`, or `V > 0` (decremented). Frames wrap to 0
     * after the last; the special-case wrap side effects on `ax == 67` /
     * flying levels are level-code, not ported here.
     */
    fun advanceAnim() {
        val c = clip ?: return
        if (P and 64 != 0 || U < 0 || V > 0) {
            if (V > 0) V--
            return
        }
        val dur = c.frameDuration(S, T)
        if (dur == 0) return
        U++
        a++
        if (dur > U) return
        U = 0
        T++
        if (T >= c.frameCount(S)) T = 0
    }

    /**
     * Physics integration (`i.java:3887-3916`, proven): position tracks the
     * integer anchor unless code writes `N`/`O` directly, then vel/accel:
     * `N += ag; ag += ai; ai = 0; ak = N >> 8` and same for the vertical.
     */
    fun integrate() {
        N += (ak - (N shr 8)) shl 8
        N += ag
        ag += ai
        ai = 0
        ak = N shr 8
        O += (al - (O shr 8)) shl 8
        O += ah
        ah += aj
        aj = 0
        al = O shr 8
    }

    /** `P & 7` flags for clip queries (bit0 = facing mirror, etc.). */
    fun drawFlags(): Int = P and 7

    fun setPositionPx(x: Int, y: Int) {
        ak = x; al = y; N = x shl 8; O = y shl 8
    }
}
