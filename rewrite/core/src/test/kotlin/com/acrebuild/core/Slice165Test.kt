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

class Slice165Test {

    private fun armDialog(w: Level0World, u: Int, v: Int, w_: Int,
                          vararg lines: String): Level0World {
        w.autoDismissDialog = false
        w.stateL(21)
        w.dlgU = u; w.dlgV = v; w.dlgW = w_
        lines.forEachIndexed { i, s -> w.dlgBM[i] = s }
        w.dlgBT = -1
        return w
    }

    private fun claimWaiting(): Entity {
        val e = Entity(5, null)
        e.cd[2] = true                                 // claim wait-for-user
        e.cd[0] = true                                 // halted by the modal
        return e
    }

    @Test fun `u9 claim-wait dialog exits on a tap in the right soft-key strip`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        val c = claimWaiting(); w.kC = c
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 370, 210),
                      InputQueue.Event(1, InputQueue.Type.UP, 370, 210)))
        // b(false)'s SKIP pill arms E(131072) on the release frame; the
        // :944 gate reads it at the next one (slice 368).
        assertEquals(21, w.jC, "release frame: E(131072) armed")
        w.tick(emptyList())
        assertEquals(8, w.jC, "v(131072) + C.cd[2] → C.Z(); l(8) (:1003-1010)")
        assertTrue(c.cd[1], "C.cd[1]=true on the consume arm")
        assertFalse(c.cd[0], "C.Z() resumes the halted claimer")
        assertEquals(w.dlgW, w.dlgV, "v = w on exit")
    }

    @Test fun `u9 claim-wait ignores a tap outside the soft-key strip`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        w.kC = claimWaiting()
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 200, 100),
                      InputQueue.Event(1, InputQueue.Type.UP, 200, 100)))
        assertEquals(21, w.jC, "playfield tap = context only — no M_CYCLE arm")
    }

    @Test fun `u9 left-of-strip tap does not arm M_CYCLE`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        w.kC = claimWaiting()
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 340, 210),
                      InputQueue.Event(1, InputQueue.Type.UP, 340, 210)))
        assertEquals(21, w.jC, "x=340 sits outside the SKIP pill (cf=36 → 349..405,198..245)")
    }

    @Test fun `pause icon on jC21 routes to the pause menu`() {
        val w = armDialog(world(), 9, 0, 2, "p1", "p2", "p3")
        val c = claimWaiting(); w.kC = c
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 370, 10),
                      InputQueue.Event(1, InputQueue.Type.UP, 370, 10)))
        w.tick(emptyList())                            // E() lands bB at the
                                                       // next frame's commit
        assertEquals(14, w.jC, "v(262144) → C.Y(); bw=0; l(14) (:1057-1061)")
        assertTrue(c.cd[0], "C.Y() halts the claimer")
    }

    @Test fun `af confirm accepts a tap on a level row`() {
        val w = world()
        w.kBA[14] = 3                                  // da=4 → kFQ rows
        w.stateL(30)
        w.tick(emptyList())                            // fO==0 init arm
        assertTrue(w.panelVisible, "af() draws d(93,46,214) — panel must show")
        assertTrue(w.kFQ > 0)
        val r = w.menuRowRects()[0]
        val cx = r[0] + r[2] / 2; val cy = r[1] + r[3] / 2
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, cx, cy),
                      InputQueue.Event(1, InputQueue.Type.UP, cx, cy)))
        assertEquals(9, w.jC, "row tap = v(327712) confirm → l(9) (:6269)")
    }

    @Test fun `jc11 exit emits QuitApp once`() {
        val w = world()
        w.stateL(11)
        w.tick(emptyList())
        assertEquals(-1, w.jC, "case 11 → j.c=-1 (notifyDestroyed)")
        val cmds = w.drainCommands()
        assertEquals(1, cmds.count { it is com.acrebuild.core.Command.QuitApp },
                     "QuitApp emitted once")
        w.tick(emptyList())
        assertTrue(w.drainCommands().none { it is com.acrebuild.core.Command.QuitApp },
                   "j.c==-1 is dead — no re-emit")
    }
}
