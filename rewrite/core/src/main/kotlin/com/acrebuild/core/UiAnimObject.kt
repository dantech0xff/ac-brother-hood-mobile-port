package com.acrebuild.core

/**
 * `a` (a.java, proven — the whole 163-line class): the lightweight
 * frame-anim player used by menu/UI procs (`fJ`/`fK` row art on `A[2]`).
 * `j = 40` ms per duration unit (a.java:16 — `d.a(e,f) * j`).
 *
 * State: `a`/`b` = draw x/y, `c` = draw flags, `d` = clip, `e` = anim
 * state (`-1` = none), `f` = frame, `g` = tick accumulator,
 * `h` = loop countdown (armed `loops - 1`, `-1` = infinite), `i` =
 * latched-stopped flag, `k` = palette override slot.
 */
class UiAnimObject(
    var d: Clip? = null,
    var a: Int = 0,
    var b: Int = 0,
) {
    var c = 0                       // draw flags arg (verbatim field name)
    var e = -1                      // anim state
    var currentFrame = 0            // f
    private var g = 0               // tick accumulator
    private var h = 1               // loop countdown (loops-1; -1 = infinite)
    private var iLatched = true     // i — stopped/latched
    private var k = -1              // palette override slot

    private fun reset() {
        a = 0; b = 0; e = -1; currentFrame = 0
        d = null; c = 0; g = 0; h = 1; k = -1; iLatched = true
    }

    /** `a(b)` (:44) — attach a clip; `a(l,-1)` disarms. */
    fun attach(clip: Clip?) {
        d = clip
        if (clip != null) arm(-1, -1) else e = -1
    }

    /** `a(i,i2)` (:53) — arm anim `i` for `i2` loops (`-1` = infinite).
     *  Re-arms only when latched or the state changes (verbatim guard). */
    fun arm(i: Int, i2: Int) {
        if (iLatched || i != e) {
            e = i
            seek(0)
            h = i2 - 1
            iLatched = false
        }
    }

    /** `a(i)` (:62) — seek `i` ticks into the current anim
     *  (`f = i % len`, `g = 0`); returns wrapped position or -1. */
    fun seek(i: Int): Int {
        if (e < 0) return -1
        var t = i
        val len = len()
        while (t > len) t -= len
        currentFrame = t
        g = 0
        return t
    }

    /** `a()` (:78) — current anim's frame count, -1 when unarmed. */
    fun len(): Int = if (e >= 0) d!!.frameCount(e) else -1

    /** `f()` (:84) — current frame's duration in ms (`d.a(e,f) * 40`). */
    private fun frameDur(): Int =
        if (e >= 0) d!!.frameDuration(e, currentFrame) * 40 else 0

    /** `b()` (:93) — done check: true when unarmed or latched-stop
     *  (infinite anims `h<0` report false forever). */
    fun stopped(): Boolean {
        if (e < 0) return true
        if (h < 0) return false
        return iLatched
    }

    /** `b(i)` (:114) — advance `i` ms along frame durations; wraps or
     *  latches at the last frame per `h` (verbatim loop). */
    fun tick(i: Int) {
        if (e < 0 || iLatched) return
        var iF = frameDur()
        if (iF == 0) return
        while (true) {
            if (g < iF) break
            g -= iF
            if (currentFrame < d!!.frameCount(e) - 1) {
                currentFrame++
            } else if (h == 0) {
                iLatched = true
                break
            } else {
                if (h > 0) h--
                currentFrame = 0
            }
            iF = frameDur()
            if (iF == 0) return
        }
        g += i
    }
}
