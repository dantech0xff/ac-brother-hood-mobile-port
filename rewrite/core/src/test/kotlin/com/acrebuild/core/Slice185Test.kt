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

class Slice185Test {

    /** L78 (i.java:4886-4898, proven): the integrator runs at the TOP of
     *  I() every tick — `N += ag; ag += ai; ai = 0` — independent of the
     *  arms. The `ai` consumption is the clean witness. */
    @Test fun `L78 integrate runs at head every tick`() {
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setAnim(20)                        // early-return arm — no ag/ai writes
        g.ag = 512; g.ai = 128
        val x0 = g.ak
        w.npcFsm.tick(g, w.player)
        assertEquals(0, g.ai, "integrate consumed ai")
        assertTrue(g.ak != x0, "N += ag moved x0=$x0 → ${g.ak}")
    }

    /** L72→L75 (i.java:6370-6384, proven): under `aH` slow-mo the
     *  integrator divides every velocity/acceleration by `aI`. */
    @Test fun `slow-mo divides integrator`() {
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setAnim(20)
        g.ag = 512; g.ai = 0
        w.iAH = true; w.iAI = 4
        val x0 = g.ak
        w.npcFsm.tick(g, w.player)
        assertEquals(x0, g.ak, "512/4/256 < 1px — no whole-pixel move")
        w.iAH = false
    }

    /** L849→L897 (i.java:5197, :6355-6372, proven): early-return arms
     *  (`goto L849`) still run the shared tail — here the facing bit
     *  resyncs even though the arm skipped the L777 checks. */
    @Test fun `early-return arm still runs L849 tail`() {
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setAnim(20)                        // S20's arm always returns
        g.av = true; g.P = g.P and -2        // facing flag ≠ bit0
        w.npcFsm.tick(g, w.player)
        assertEquals(1, g.P and 1, "av=true → P|=1 at L900-902")
    }

    /** The L777 shared checks are skipped by `goto L849`: an airborne
     *  S20 soldier keeps its state, while an airborne arm-less state
     *  falls to `!h&&!h → i(25)` (i.java:6219). */
    @Test fun `goto L849 skips the L777 fall arm`() {
        val w = world()
        val list = w.npcs.filter { it.ax == 11 }
        val a = list[0]; val b = list[1]
        a.setPositionPx(a.ak, a.al - 40); a.refreshBoxes()
        b.setPositionPx(b.ak, b.al - 40); b.refreshBoxes()
        a.setAnim(20); b.setAnim(50)         // S50 has no arm → tail runs
        w.npcFsm.tick(a, w.player)
        w.npcFsm.tick(b, w.player)
        assertEquals(20, a.S, "early-return arm skipped !h&&!h")
        assertEquals(25, b.S, "fall-through path fired i(25)")
    }

    /** L897's `a(k.aS,P,W)` push (i.java:15324-15380, proven): a falling
     *  player overlapping a 4096-flagged entity lands on its top edge. */
    @Test fun `pushL897 lands falling player on entity top`() {
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setAnim(20)
        g.P = g.P or 4096
        val p = w.player
        p.setPositionPx(g.ak, g.al - 10)     // overlap, slightly above
        p.ah = 256                           // falling
        p.refreshBoxes()
        w.npcFsm.tick(g, w.player)
        assertEquals(g.W[1] - 5, p.al, "p.al = r9[1]-5")
        assertEquals(0, p.ah)
    }

    /** s() gates (i.java:6397-6411, proven): `cu`-held entities skip the
     *  anim advance entirely. */
    @Test fun `cu holds the anim advance`() {
        val w = world()
        val g = w.npcs.first { it.ax == 11 }
        standOn(w, g)
        g.setAnim(20)
        g.cu = true
        w.npcFsm.tick(g, w.player)
        assertEquals(0, g.U); assertEquals(0, g.T)
        assertEquals(20, g.S)
    }
}
