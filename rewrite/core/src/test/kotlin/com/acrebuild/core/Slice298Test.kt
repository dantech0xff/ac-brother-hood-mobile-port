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

class Slice298Test {

    /** m7 leg D — post-duel climb: once `iBy=2` the boss goes dormant,
     *  the bot crosses the arena floor east, rebounds off the x1500 wall
     *  (S33→92→36) and catches the ax13 rope uid37 (hangs x1423±4,
     *  sliver y1567-1615, catch gate al<=1679 — i.java:37300). Bound
     *  (S326) the rope retracts segments to the y1423 anchor, topping
     *  out above the east shelf (y1500). Verdict: verbatim rope route —
     *  the west barrier (fuse P|4096 + hanging tongue) is not the exit. */
    @Test fun mission7PostDuelClimb() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        // Phase 1 — win the duel (driver identical to mission7BossPhaseWin).
        for (t in 0..12000) {
            val boss = w.npcs.firstOrNull { it.ax == 29 }
            if (w.iBy == 2) break
            var mask = chaseMask297(p, w)
            if (boss != null && boss.aB > 0 && boss.S != 139 &&
                !(boss.S == 7 && boss.T <= 6)) {
                if (boss.S == 26) {
                    mask = (if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT) + Pad.M_CONTEXT
                } else if (w.kC != null && w.kC!!.aw == 280 &&
                    (boss.S == 23 || boss.S == 25 || boss.S == 27)) {
                    mask = Pad.M_LEFT + Pad.M_CONTEXT
                } else if (kotlin.math.abs(boss.ak - p.ak) < 170 &&
                    kotlin.math.abs(boss.al - p.al) < 60) {
                    mask = if (boss.ak < p.ak) Pad.M_LEFT else Pad.M_RIGHT
                    if (kotlin.math.abs(boss.ak - p.ak) < 70) mask += Pad.M_CONTEXT
                }
                bossDodge297(p, w, boss)?.let { mask = it }       // slice 410
            }
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
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
        }
        assertEquals(2, w.iBy, "duel phase-win never reached")
        // Phase 2 — climb. Route (proven by the trace): run east into the
        // arena's x1500 wall → S33 wall-rebound → S36 kick arc catches the
        // ax13 rope uid37 (hangs x1423±4, grab sliver y1567-1615, catch
        // gate al<=1679 — i.java:37300) → bound S326, rope retracts to the
        // y1423 anchor → tops out above the east shelf y1500. The west
        // barrier (fuse P|4096 + hanging tongue face) is a dead end —
        // kicks there have no opposing face to chain.
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        val trace = ArrayDeque<String>(60)
        var lastS = p.S
        var jumpCd = 0
        for (t in 0..12000) {
            var mask = chaseMask297(p, w)
            when {
                // bound to the rope — drive UP to retract segments
                p.bM != null -> mask = Pad.M_UP
                // kick aftermath — drift east onto the crate stack
                !p.aZ -> mask = if (p.ak < 1250) Pad.M_RIGHT + Pad.M_UP else Pad.M_UP
                else -> mask = when {
                    // on crates/shelf — head east toward the rope
                    p.ak >= 1250 -> Pad.M_RIGHT
                    // face approach — hold into the wall with the latch up
                    else -> Pad.M_LEFT + Pad.M_UP
                }
            }
            when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                56, 60, 61, 62 -> mask = Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
            }
            // jump pulses: into the face when stalled west of x1100; up into
            // the rope sliver when stalled under it
            if (jumpCd <= 0 && stall > 0 && stall % 25 == 0) {
                mask = if (p.ak < 1150) 16390 or Pad.M_LEFT
                       else if (p.ak in 1380..1460) 16388
                       else 16398 or Pad.M_RIGHT
                jumpCd = 25
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
            if (p.al < 1460) { println("REACHED rope-top/shelf t=$t ak=${p.ak}"); break }
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} trace=${trace.joinToString(" ")}")
        assertTrue(p.al < 1460,
            "never topped out on the rope/shelf — al=${p.al} ak=${p.ak}")
    }
}
