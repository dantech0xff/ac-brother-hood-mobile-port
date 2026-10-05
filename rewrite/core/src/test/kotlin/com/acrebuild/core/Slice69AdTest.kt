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

// ============================================================================
// Slice 69 — i.ad() speech-bubble renderer + bK() refill (op106's consumer)
// ============================================================================
class Slice69AdTest {

    private fun bubbler(ax: Int = 5): Entity {
        val e = Entity(ax, null)
        e.setPositionPx(200, 100); e.refreshBoxes()
        e.cQ = IntArray(10) { -1 }; e.cT = e
        return e
    }

    private fun activeClaimer(): Entity =
        Entity(0, null).apply { ca = 0; cd[0] = false; scriptStep = 0 }

    @Test fun `no cQ or dead page index is a no-op`() {
        val w = world(); val e = bubbler()
        w.npcFsm.tickBubble(e, w)                       // cQ != null but q[5] = -1
        assertNull(w.bubbleDraw)
        e.cQ!![5] = 40; e.cQ!![6] = 6                 // q[5] > q[6]
        w.npcFsm.tickBubble(e, w)
        assertNull(w.bubbleDraw)
        val bare = Entity(5, null)
        w.npcFsm.tickBubble(bare, w)                    // cQ == null
        assertNull(w.bubbleDraw)
    }

    @Test fun `wait mode tears down when claim ends`() {
        val w = world(); val e = bubbler()
        val q = e.cQ!!
        q[5] = 4; q[6] = 6; q[7] = 1; q[3] = 10
        w.kC = null                                  // L19 arm
        w.npcFsm.tickBubble(e, w)
        assertNull(e.cQ); assertNull(e.cT); assertNull(e.cS)
    }

    @Test fun `wait mode holds while claim is active`() {
        val w = world(); val e = bubbler()
        val q = e.cQ!!
        q[5] = 7; q[6] = 8; q[7] = 1; q[3] = 10
        w.kC = activeClaimer()
        w.npcFsm.tickBubble(e, w)
        assertNotNull(e.cQ)                           // survived — filled page
        assertEquals("KEEP YOUR EYES OPEN, SOLDIER!\n", e.cR)
        assertNotNull(w.bubbleDraw)
    }

    @Test fun `refill wraps text and arms the draw descriptor`() {
        val w = world(); val e = bubbler()
        val q = e.cQ!!
        q[5] = 7; q[6] = 7; q[2] = -1; q[3] = 30
        w.kO = 60; w.kP = 40; w.rebuildCamRect()
        w.npcFsm.tickBubble(e, w)
        // bK(): cR = k.d(1+aj, 7) + '\n'; 2 wrapped lines at 20 ch/line →
        // q[4]=2 clamped to q[1]-q[0]=1 (verbatim L10 quirk)
        assertEquals("KEEP YOUR EYES OPEN, SOLDIER!\n", e.cR)
        assertEquals(0, q[0]); assertEquals(1, q[4]); assertEquals(1, q[1])
        assertEquals(29, q[2])                        // q[3]-1 after L28 dec
        val d = w.bubbleDraw!!
        assertEquals(140, d.x)                        // ak - kO
        assertEquals(1, d.lines)
        assertFalse(d.tailUp)
    }

    @Test fun `timer expiry advances the page then next string`() {
        val w = world(); val e = bubbler()
        val q = e.cQ!!
        // long string → >3 lines forces paging (idx 1 is ~300 chars)
        q[5] = 1; q[6] = 2; q[2] = -1; q[3] = 5
        w.npcFsm.tickBubble(e, w)                       // refill + draw
        assertEquals(3, q[4])
        val totalLines = q[1] + 1
        assertTrue(totalLines > 3)
        // run the timer down — page advance (q[0] += q[4])
        repeat(5) { w.npcFsm.tickBubble(e, w) }
        assertEquals(3, q[0])                         // L44 next page
        // drain remaining pages → string idx advances
        var ticks = 0
        while (q[0] + q[4] != q[1] && ticks++ < 200) {
            repeat(5) { w.npcFsm.tickBubble(e, w) }
        }
        repeat(6) { w.npcFsm.tickBubble(e, w) }
        assertTrue(q[5] >= 2)                         // advanced to next string
    }

    @Test fun `last string teardown arms cd flags on active claim`() {
        val w = world(); val e = bubbler()
        val q = e.cQ!!
        q[5] = 7; q[6] = 7; q[2] = -1; q[3] = 2; q[9] = 1
        w.kC = activeClaimer()
        w.npcFsm.tickBubble(e, w)                       // refill (2 lines)
        // drain timer+pages until teardown
        var ticks = 0
        while (e.cQ != null && e.cR.isNotEmpty() && ticks++ < 50) {
            w.npcFsm.tickBubble(e, w)
        }
        assertTrue(e.cT == null || e.cR.isEmpty())
        assertTrue(w.kC!!.cd[1] && w.kC!!.cd[2])      // cd[1..2] = true
    }

    @Test fun `mission bh3 flips style and tail-up when clipped`() {
        val w = world(); w.kAj = 1                     // MISSION_BH[1]==3
        val e = bubbler()
        val q = e.cQ!!
        q[5] = 7; q[6] = 7; q[2] = -1; q[3] = 30
        e.av = true                                   // facing flip (suppressed by bh3)
        w.kO = 0; w.kP = 0; w.rebuildCamRect()
        e.setPositionPx(200, 40)                      // al-kP-70-h < 0 → tailUp
        w.npcFsm.tickBubble(e, w)
        val d = w.bubbleDraw!!
        assertTrue(d.bh3)
        assertTrue(d.tailUp)
        assertFalse(d.flip)                           // av suppressed under bh3
    }
}
