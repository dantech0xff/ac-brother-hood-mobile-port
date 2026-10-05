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

class Slice179Test {

    private fun tickPlay(w: Level0World, n: Int = 1) {
        repeat(n) {
            w.stateL(8)
            w.player.al = w.camY + 120; w.player.ah = 0
            w.tick(emptyList())
        }
    }

    @Test fun `stampAt resolves view cells and guards wrap band`() {
        val w = world(aj = 1)
        tickPlay(w, 2)
        // in-window logical cell → live et index
        val cx = w.parallaxX / 20 + 1; val cy = w.parallaxY / 20 + 1
        assertTrue(w.level.stampAt(cx, cy) >= 0)
        // wrap-band / unmapped slots guard to -1 like g()'s i4>=len arm
        val et = w.level.layers.first { it.id == 0 }
        val dl = w.level.flyingGrid!!
        val bad = (0 until 21 * 13).firstOrNull { dl[it] >= et.cells.size }
        if (bad != null) {
            val bx = bad / 13; val by = bad % 13
            assertEquals(-1, w.level.stampAt(bx, by))
        }
    }

    @Test fun `stampAt is -1 on grounded packs`() {
        assertEquals(-1, world().level.stampAt(1, 1))
    }

    /** bh3 wraps negative rows above the world back positive
     *  (`i14=i13%13; if(i14<0)i14+=13`, k.java:4434-4438, proven):
     *  a negative parallax-y must fold `cy%13` into [0,13) instead of
     *  indexing `dL` out of bounds (the m1-spawn crash). */
    @Test fun `stampAt wraps negative rows`() {
        val w = world(aj = 1)
        tickPlay(w, 2)
        val dl = w.level.flyingGrid!!
        // pick an in-range positive cell and prove its negative-row
        // alias resolves to the same index instead of throwing.
        val bx = 7; val by = 4
        val want = dl[bx * 13 + by]
        assertEquals(want, w.level.stampAt(bx, by - 13))  // cy-13 ≡ cy mod 13
        // a wildly negative cy stays in-bounds rather than crashing.
        w.level.stampAt(0, -200)
        w.level.stampAt(21, -40)
    }
}
