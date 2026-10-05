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

class Slice217Test {
    /**
     * Slice-217 verdict — the x2147 "hazard" is a verbatim trap shaft, not
     * a bug (probe: player enters S43 fall, `k.v` fail at tick 9).
     * Col 107 = x2140..2159 is `et=-1` top-to-bottom — `k.I()`'s load
     * normalization (k.java:19175 La6, proven) rewrites -1 → 0 = air, so
     * there is no floor from y~400 to the world bottom. Walking off the
     * '02' ledge east of the ax4 crates drops the player in it — the
     * intended route is the wisp arc (ax74 records x2033–2140 marking the
     * jump-over trajectory onto the ax37/ax10 ledge east, uid311/uid241)
     * toward the ax44 poles + checkpoint (2594,485).
     */
    @Test fun `x2147 shaft is verbatim air and falling in fails like the original`() {
        val w = world()
        // et layer verbatim: -1 cells are air (k.java load normalize).
        for (cy in 24..41) assertEquals(0, w.level.collisionCell(107, cy), "col107 row$cy is air")
        // the shaft mouth east edge is walled (col 110+ rows 24+ = cell 20 boundary)
        // and the floor resumes below the pit at row 40 west (cols 104-105 = cell 5)
        assertEquals(5, w.level.collisionCell(104, 40)); assertEquals(5, w.level.collisionCell(105, 40))
        settleIntro(w)
        val p = w.player
        p.setPositionPx(2147, 470)
        w.kO = 2147 - 200; w.kP = 470 - 120; w.rebuildCamRect()
        var fallSeen = false; var failAt = -1
        repeat(240) { t ->
            w.tick(emptyList())
            if (p.S == 43) fallSeen = true
            if (w.failed && failAt < 0) failAt = t
        }
        assertTrue(fallSeen, "player should enter the S43 fall over the shaft")
        assertTrue(failAt in 1..200, "k.v fails the fall like the original (failAt=$failAt)")
    }
}
