package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Slice 337 — menu/UI audit fixes (Run-31).
 *
 * Device audit found the footer soft-key strip diverged from
 * `a(String,String)` (k.java:2906-2955):
 *   - `r10 == d(0,79) → goto L23` skips the left arm WHOLESALE — no
 *     pill, `ce` keeps the -1 the proc resets to, no `c()` hit-test.
 *     The port drew a pill + armed an `E(262144)` (M_PAUSE) zone for
 *     every non-empty left label, so "OK" screens (jc14-else, jc19,
 *     jc23/28, jc3, jc30) showed a phantom left pill fragment and a
 *     phantom back-zone at the bottom-left strip.
 *   - the left pill itself is NEXT-only: `L16`'s `a(5,235,ce,r0)` is
 *     reached solely via the `d(0,16)` path; other non-OK labels take
 *     `L15 → L20` (ce=36, hit-test, no pill). The renderer now draws
 *     the pill only for `left == d0(16)`.
 *
 * (The letterbox-clamp fix lives in the gdx `Level0InputBridge` —
 *  platform-adapter code with no headless seam; verified on-device.)
 */
class Slice337Test {

    private fun tap(w: Level0World, x: Int, y: Int) {
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, x, y),
            InputQueue.Event(1, InputQueue.Type.UP, x, y)))
    }

    /** `padE` inside the state dispatch lands in `eK` AFTER `pad.commit`
     *  already ran at the top of the same tick — the bit is visible via
     *  `v()` only from the next tick. Check `eK | bB` so a one-tick tap
     *  is enough. */
    private fun armed(w: Level0World, mask: Int) =
        (w.pad.eK or w.pad.bB) and mask != 0

    // -- "OK" left arm is inert -----------------------------------------

    @Test
    fun `jc19 OK left zone arms no M_PAUSE`() {
        val w = world(aj = 0)
        w.stateL(19)
        tap(w, 20, 220)                          // left footer strip zone
        assertFalse(armed(w, Pad.M_PAUSE),
                    "a(d(0,79),…) → goto L23: no E(262144) arm")
        assertEquals(-1, w.kCe, "ce stays -1 — a() never assigns it")
    }

    @Test
    fun `jc23 OK left zone arms no M_PAUSE`() {
        val w = world(aj = 0)
        w.stateL(23)
        tap(w, 20, 220)
        assertFalse(armed(w, Pad.M_PAUSE),
                    "sound-prompt left pill must not draw/hit-test")
        assertEquals(-1, w.kCe)
    }

    @Test
    fun `jc14 bv-non2 OK left zone arms no M_PAUSE`() {
        val w = world(aj = 0)
        w.stateL(14); w.kBv = 0                  // a(bv==2?16:79 → 79, 17)
        tap(w, 20, 220)
        assertFalse(armed(w, Pad.M_PAUSE))
        assertEquals(-1, w.kCe)
    }

    // -- real pills still work -------------------------------------------

    @Test
    fun `jc14 bv==2 NEXT left zone arms M_PAUSE`() {
        val w = world(aj = 0)
        w.stateL(14); w.kBv = 2                  // a(d(0,16),d(0,17)) NEXT/BACK
        tap(w, 20, 220)
        assertTrue(armed(w, Pad.M_PAUSE),
                   "NEXT is a real pill — its c() arm fires E(262144)")
        assertTrue(w.kCe > 0, "NEXT measures ce = b.d+30 (or 36 fallback)")
    }

    @Test
    fun `jc19 right BACK zone still arms M_CYCLE`() {
        val w = world(aj = 0)
        w.stateL(19)
        // right strip zone (395-kCf-10,198,kCf+20,47); cf=36 → x∈[349,415]
        tap(w, 370, 220)
        assertTrue(armed(w, Pad.M_CYCLE),
                   "BACK pill keeps its E(131072) arm")
    }

    @Test
    fun `jc3 OK left zone inert but BACK zone live`() {
        val w = world(aj = 0)
        w.stateL(3); w.kBv = 1                   // a(OK, BACK)
        tap(w, 20, 220)
        assertFalse(w.pad.v(Pad.M_PAUSE))
        assertEquals(-1, w.kCe)
    }
}
