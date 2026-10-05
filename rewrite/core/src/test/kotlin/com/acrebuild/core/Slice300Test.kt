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

class Slice300Test {

    /** m7 leg E — post-rope shelf run: off the rope top the bot drops onto
     *  the east shelf (y1500, x940-1520), runs west past checkpoint
     *  uid233@(1244,1470) (aY → checkpointSnap + kG=351) and the ax5 uid351
     *  arrival trigger, down to the y1500 lift row (ax66 u22-27,
     *  x636-1004). The shelf has two gaps (x740-800, x920-940) — falls land
     *  on the '02' platform at y1540 under it. Verdict route, verbatim
     *  entities. */
    @Test fun mission7ShelfRun() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        driveDuelWin300(w, p)
        assertEquals(2, w.iBy, "duel phase-win never reached")
        driveRopeClimb300(w, p)
        assertTrue(p.al < 1460, "never topped out on the rope — al=${p.al}")
        // Phase 3 — shelf run west.
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        val trace = ArrayDeque<String>(60)
        var lastS = p.S
        var jumpCd = 0
        var sawCkpt = false
        for (t in 0..12000) {
            var mask = chaseMask297(p, w)
            when {
                p.bM != null -> mask = Pad.M_UP
                !p.aZ -> mask = Pad.M_LEFT + Pad.M_UP
                else -> mask = Pad.M_LEFT
            }
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                56, 60, 61, 62 -> mask = Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
            }
            // gap-crossing hop pulses while stalled
            if (jumpCd <= 0 && stall > 0 && stall % 20 == 0) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            }
            jumpCd--
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.S != lastS) {
                if (trace.size == 60) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (w.checkpointSnap != null) sawCkpt = true
            if (w.jC == 15) break
            if (w.jC == 21) { if (t % 40 == 0) { w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush() }; continue }
            if (w.jC == 12 || w.jC == 13) {
                var guard = 0
                while (w.jC != 8 && w.jC != 15 && guard++ < 400) {
                    w.pad.e(Pad.M_CONTEXT); w.pad.releaseFlush()
                    w.tick(emptyList())
                }
                settleIntro(w)
                continue
            }
            if (w.jC != 8) break
            if (p.ak == lastAk && p.al == lastAl) stall++ else stall = 0
            lastAk = p.ak; lastAl = p.al
            if (p.ak <= 1040) break
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} ckpt=${w.checkpointSnap?.aw} kG=${w.kG} sawCkpt=$sawCkpt trace=${trace.joinToString(" ")}")
        assertTrue(p.ak <= 1040 || w.checkpointSnap != null,
            "never reached the lift row / checkpoint — ak=${p.ak} al=${p.al}")
        if (w.checkpointSnap != null) assertEquals(233, w.checkpointSnap!!.aw)
    }
}
