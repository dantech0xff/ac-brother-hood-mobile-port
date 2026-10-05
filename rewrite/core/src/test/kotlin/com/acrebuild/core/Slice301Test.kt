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

class Slice301Test {

    /** m7 leg F — west to the spring u33@(336,1522), whose S0 pad arm
     *  (i.java:13647) turns a falling overlap into `a(11,0,0,this)` —
     *  Z-launch `ag=-50<<8, ah=-90<<8` → thrown up-left into the west
     *  wing. Asserts the launch (al < 1360 = 160px+ above the pad).
     *  Slice 370 route: the '02' floor y1540 under the lift row (r77
     *  cols 7-52, on the r78 floor) is a type-2 strip — landing there is
     *  the L353d kill (g.javap.txt e() 13662-13711), so the bot no
     *  longer walks it. The crossing is the ax72 wheel uid36@(510,1345)
     *  (Z[0]==2): `az()` arms `i.at` once `gJ&4` (k.F(aj) → g.g(f0do[aj])
     *  = 5, k.java:210/3551-3558 — the harness enters play without
     *  F(aj), hence `gJ = 5`), CONTEXT → `c(i.at)` lunge S273 → i(277)
     *  mount → `au()` wheel → band-edge fling `ag=-3328, ah=-6656,
     *  i(243)`; the S43 arc comes down on the pad's W-box (~(341,1519))
     *  and the pad throws him to the '5' underside (S280 @(270,1310)).
     *  The fling arc alone tops out at al~1388 — al < 1360 needs the
     *  pad. */
    @Test fun mission7SpringLaunch() {
        val w = world(aj = 7)
        w.stateL(8); settleIntro(w)
        val p = w.player
        p.gJ = 5                        // f0do held-mask the real play-entry applies (k.F(aj))
        driveDuelWin300(w, p)
        driveRopeClimb300(w, p)
        var stall = 0; var lastAk = p.ak; var lastAl = p.al
        val trace = ArrayDeque<String>(60)
        var lastS = p.S
        var jumpCd = 0; var pressCd = 0
        var minAl = p.al
        var flung = false
        for (t in 0..12000) {
            var mask = chaseMask297(p, w)
            val mountT = Entity.at
            val boundF = p.F
            when {
                p.bM != null -> mask = Pad.M_UP
                // wheel uid36: hands off through the lunge/mount/fling
                // and the arc onto the pad (F lingers post-fling, faithful)
                flung || p.S == 243 -> mask = 0
                boundF != null && boundF.ax == 72 && p.S >= 270 -> mask = 0
                mountT != null && mountT.ax == 72 && mountT.aw == 36 &&
                    pressCd <= 0 && boundF == null && wheelInReach(mountT, p) ->
                    { mask = Pad.M_CONTEXT; pressCd = 12 }        // lunge-mount the wheel
                !p.aZ -> mask = Pad.M_LEFT + Pad.M_UP
                else -> mask = Pad.M_LEFT
            }
            if (!flung) when (p.S) {
                65 -> mask = Pad.M_UP + Pad.M_TAP_L
                63, 318 -> mask = Pad.M_UP
                56, 60, 61, 62 -> mask = Pad.M_LEFT + Pad.M_UP
                27, 28, 29, 30, 31, 34, 35, 90, 315, 316, 319 -> mask = Pad.M_UP + Pad.M_LEFT
            }
            if (!flung && boundF == null && jumpCd <= 0 && stall > 0 && stall % 20 == 0) {
                mask = 16390 or Pad.M_LEFT
                jumpCd = 20
            }
            jumpCd--; pressCd--
            w.pad.e(mask)
            if (p.al - 240 > w.kP) w.kP = p.al - 240; w.rebuildCamRect()
            if (p.al + 120 < w.kP) w.kP = p.al + 120; w.rebuildCamRect()
            w.tick(emptyList())
            if (p.S == 243) flung = true
            if (p.S != lastS) {
                if (trace.size == 60) trace.removeFirst()
                trace.addLast("$t:${lastS}->${p.S}@${p.ak},${p.al}")
                lastS = p.S
            }
            if (p.al < minAl) minAl = p.al
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
            if (p.al < 1360) break
        }
        println("END ak=${p.ak} al=${p.al} S=${p.S} jC=${w.jC} minAl=$minAl trace=${trace.joinToString(" ")}")
        assertTrue(p.al < 1360,
            "never launched off the spring — al=${p.al} ak=${p.ak}")
    }
}
