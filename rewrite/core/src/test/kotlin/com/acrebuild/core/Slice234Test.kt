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

class Slice234Test {

    @Test fun `ax32 records spawn with the Lc15 field init`() {
        val w = world(aj = 4)
        val byAw = w.npcs.filter { it.ax == 32 }.associateBy { it.aw }
        // Lc15 (i.java:8951) + L1bea finish: (S=r8[5], aB=r8[7], aC=r8[8],
        // aF=r8[9], pv=r8[10], nl=aF) for each level-4 ax32 record.
        val expect = mapOf(
            11 to intArrayOf(13, 170, 30, 20, 0),
            15 to intArrayOf(17, 200, 30, 20, 2),
            16 to intArrayOf(17, 150, 40, 30, 2),
            17 to intArrayOf(21, 380, 30, 30, 3),
            154 to intArrayOf(33, 170, 30, 30, 4))
        assertEquals(expect.size, byAw.size)
        for ((aw, exp) in expect) {
            val e = byAw[aw] ?: error("ax32 aw=$aw was not spawned")
            assertEquals(exp[0], e.S, "aw=$aw S")
            assertEquals(exp[1], e.aB, "aw=$aw aB")
            assertEquals(exp[2], e.aC, "aw=$aw aC")
            assertEquals(exp[3], e.aF, "aw=$aw aF")
            assertEquals(exp[4], e.pv, "aw=$aw pv")
            assertEquals(e.aF, e.nl, "aw=$aw nl")
        }
    }

    @Test fun `ax32 init accumulates the gauge for non pv3 records`() {
        // i.bU += aB when p!=3 (i.java:8985): 170+200+150+170; the pv==3
        // record (aw=17, aB=380) is skipped; i.bU has no other += writer.
        assertEquals(690, world(aj = 4).iBU)
    }

    @Test fun `clipless ax65 record spawns like the original unknown arm`() {
        // bi[65]=-1 — clipless spawn in the original; its init is just
        // L1bc7 ("Unknown Actor Type") + the L1bea i(r8[5]) finish.
        val w = world(aj = 1)
        assertTrue(w.npcs.any { it.ax == 65 },
            "ax65 record should spawn clipless")
    }

    @Test fun `else arm records take their record anim as spawn S`() {
        // L1bea i(r8[5]): ax16 markers (level2, S=38 in the record) used
        // to spawn S=0 — the missing common finish is now applied.
        val w = world(aj = 2)
        val ax16 = w.npcs.filter { it.ax == 16 }
        assertTrue(ax16.isNotEmpty())
        for (e in ax16) assertTrue(e.S == 38 || e.S == 31 || e.S == 32,
            "ax16 aw=${e.aw} spawned S=${e.S}")
    }
}
