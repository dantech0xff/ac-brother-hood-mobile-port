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

class Slice212Test {

    /**
     * The shaft at x1740-1820 (west face lip y520, east face solid
     * y420-800): hold east, seed one arc -> S101 grab at x1820 -> the
     * L1a46 bounce (ag=-2048 west, ah=-5120 rise) arcs into the WEST
     * face x1740 -> re-grab higher -> bounce back east -> re-grab even
     * higher. The shaft is a zigzag wall-jump ladder (west face lip at
     * y520, east roof y420); the '05' strip at y800 is the catch. */

    @Test fun `kick chain regrabs higher on the x1820 face`() {
        val w = world(); val p = w.player
        var t = 0
        while (p.ah != 0 && t++ < 600) w.tick(emptyList())
        p.setPositionPx(1790, 700)             // mid-shaft, face x1820 spans y420-800
        p.S = 35; p.ag = 1536; p.ai = 0; p.ah = -300; p.aj = 1536
        p.av = false                            // facing east toward x1820
        p.refreshBoxes()                        // a finished frame's t() (G12)
        val q = InputQueue()
        val (rx, ry) = w.cellPoint(2)
        q.post(InputQueue.Type.DOWN, rx, ry)   // hold east the whole time
        val grabs = mutableListOf<Pair<Int, Int>>()   // (tick, al) of each grab
        repeat(300) { tt ->
            w.tick(q.drainTo(q.headSequence()))
            if (p.S == 101 && (grabs.isEmpty() || tt - grabs.last().first > 3))
                grabs += tt to p.al
        }
        assertTrue(grabs.size >= 2, "expected repeated grabs, got ${grabs}")
        val first = grabs.first().second
        assertTrue(grabs.any { it.second < first - 40 },
            "expected a grab well above the first (al=$first): ${grabs}")
    }
}
