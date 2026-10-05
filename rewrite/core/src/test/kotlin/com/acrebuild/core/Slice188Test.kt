package com.acrebuild.core

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 188 — `f.bF` loading overlay + stale-comment cleanup.
 *
 * `f.a(d(0,24),0)` (f.java:879+, proven call-site shape at k.java:1460/
 * 4786/5259) sets the IGP thread's `bF` loadingMsg — `d(0,24)` = "LOADING"
 * (pack-14 entry-000 string 24, proven). The f-loop paints it centered
 * HCENTER|BOTTOM with a white progress outline (f.java:1857-1863) until
 * `bF = null` on IGP exit (f.java:1399). The IGP machinery itself is
 * proven-dead; `kLoading` carries the observable flag.
 */
class Slice188Test {
    /** `f.a(d(0,24),0)` → `bF` set — the flag arms on `loadingShow()`. */
    @Test fun `loadingShow arms kLoading`() {
        val w = world()
        assertFalse(w.kLoading)
        w.loadingShow()
        assertTrue(w.kLoading, "f.a(d(0,24),0) sets bF")
    }

    /** Same `f.a(d(0,24),0)` call at the store/IGP entry (k.java:1410). */
    @Test fun `enterIgp arms kLoading`() {
        val w = world()
        w.enterIgp()
        assertTrue(w.kLoading, "store entry shows the same overlay")
    }

    /** `loadingShow(); l(27)` — the overlay survives the transition to
     *  screen 27 (it is drawn by the f-loop, independent of k.l). */
    @Test fun `stateL27 keeps kLoading`() {
        val w = world()
        w.loadingShow()
        w.stateL(27)
        assertTrue(w.kLoading, "bF outlives the l(27) transition")
    }

    /** `bF = null` on IGP exit (f.java:1399) — leaving jC==27 clears. */
    @Test fun `stateL non27 clears kLoading`() {
        val w = world()
        w.loadingShow()
        w.stateL(27)
        w.stateL(8)
        assertFalse(w.kLoading, "bF clears on exit from the load state")
    }
}
