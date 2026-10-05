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

// =========================================================================
// Slice 103 — jC==21 dialog render state (k.java:350-450, 899-1017):
// b(9,1+aj,str,str) load → bM[] 3-line pages + bN icon propagation,
// typewriter bR/bS/bT, u==9 press tail (reveal → dismiss, no page adv).
// =========================================================================
class Slice103Test {
    @Test fun `kDialog loads u-9 state and wraps pages at 300`() {
        val w = world()
        assertTrue(w.kDialog(5, 26, 1))
        assertEquals(9, w.dlgU)
        assertEquals(1, w.bO); assertEquals(5, w.bN0); assertEquals(5, w.dlgBN[0])
        assertTrue(w.dlgW >= 1)
        assertNotNull(w.dlgBM[0]); assertTrue(w.dlgBM[0]!!.isNotEmpty())
        assertEquals(0, w.dlgV); assertEquals(0, w.dlgBT)
        assertEquals(0, w.dlgBR); assertEquals(30, w.dlgBS); assertTrue(w.dlgBQ)
        // bN[i] propagates to every page slot (k.java:392-398, proven)
        for (i in 1 until w.dlgW) assertEquals(5, w.dlgBN[i])
    }

    @Test fun `kBMark is the verbatim k-b-IIII panel initializer`() {
        val w = world()
        assertFalse(w.kBMark(8, 1, -1, 0), "i3==-1 → false (:353)")
        // multi-row arm (u8): u=slot; per-row a(d,·,i!=6,i) expansion;
        // D(0); bQ; z()  (k.java:358-371, proven)
        assertTrue(w.kBMark(8, 1, 26, 26))
        assertEquals(8, w.dlgU)
        assertTrue(w.dlgW >= 1); assertEquals(0, w.dlgV)
        assertTrue(w.dlgBQ); assertEquals(30, w.dlgBS); assertEquals(0, w.dlgBT)
        assertNotNull(w.dlgBM[0])
        // single-text arm (kinds 0/4/5/7): a(d,0,false,i)+1 at 220px —
        // no bN digit writes (k.java:356-357, proven)
        val w2 = world()
        assertTrue(w2.kBMark(0, 1, 26, 26))
        assertEquals(0, w2.dlgU); assertTrue(w2.dlgW >= 1); assertTrue(w2.dlgBQ)
    }

    @Test fun `typewriter counts bT up then pins -1`() {
        val w = world()
        w.kDialog(1, 27, 0)
        val len = w.dlgBM[0]!!.length
        w.dlgTypeTick(len)
        assertEquals(1, w.dlgBR); assertEquals(30 / 16, w.dlgBT)   // (bR*bS)/16
        repeat(400) { w.dlgTypeTick(len) }
        assertEquals(-1, w.dlgBT)                                // A() done
        val after = w.dlgBR
        w.dlgTypeTick(len)
        assertEquals(after, w.dlgBR, "bT==-1 stops the counter")
    }

    @Test fun `typing press reveals and revealed press dismisses`() {
        val w = scriptedWorld(
            scriptBlock(0, 0, scriptGroup(0, op105(3, 42, 1))))
        val e = claimer(w)
        e.runClaimScript(w)
        assertTrue(w.dialogModal)
        w.autoDismissDialog = false
        w.kC = e
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertTrue(w.dialogModal, "press while typing reveals only (:955)")
        assertEquals(-1, w.dlgBT)
        // the revealed press runs the shared advance arm `x=48; D(v+1)`
        // (bytecode 3604-3629 — u∈{1,2,3,6,9} all reach it); v 0→1=w
        // → `C.Z(); l(8)` on the same tick.
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList())
        assertFalse(w.dialogModal,
            "revealed press → D(v+1) → v==w → C.Z(); l(8) (:3604/:985-989)")
        assertFalse(e.cd[0], "C.Z() resumed the claim")
        assertEquals(0, w.pad.edge)
    }

    @Test fun `cd2 claimer + 131072 edge suppresses the dialog press`() {
        val w = world()
        w.kDialog(1, 18, 0)
        w.screenL(21)
        val e = Entity(5, null); e.cd[2] = true; w.kC = e
        w.pad.edge = 131072                      // v(131072) edge latched
        assertTrue(w.dlgSuppressed(), "(:946) — claim script eats the press")
        e.cd[2] = false
        assertFalse(w.dlgSuppressed())
    }
}
