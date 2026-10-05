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

// --------------------------------------------------------------- slice 107
// ax9 S4/S5. Slice 107 ported `aV()`'s hint-banner (L15b9) and context-pad (L15e8) arms
// into ax9; the bytes disagree (slice 409): `bM()`'s tableswitch sends S4 and S5 to the bare
// return @653, `k.aB` is written only by `aV()` (ax10, i.javap @5589/@5604), and `bM()` is
// called only at the I() dispatch of case 9. The four tests below pin the bytes now.
class Slice107Test {

    private fun ax9At(w: Level0World, x: Int, y: Int, s: Int, aF: Int = 0): Entity {
        val e = Entity(9, w.clips[47])
        e.setPositionPx(x, y)
        val f = mutableListOf(9, 0, x, y, 0, s, 0, -1, 0)
        for (i in 9..15) f += 0
        w.npcFsm.initAx9(e, f, w)
        e.aF = aF
        w.npcs.add(e)
        return e
    }

    private fun boxAroundPlayer(w: Level0World, e: Entity) {
        val p = w.player; p.refreshBoxes()
        e.W[0] = p.W[0] - 10; e.W[1] = p.W[1] - 10
        e.W[2] = p.W[2] + 10; e.W[3] = p.W[3] + 10
    }

    @Test fun `S4 overlap changes nothing - bM() sends S4 to the bare return (@653)`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 0, 0, 4, aF = 3)
        boxAroundPlayer(w, e)
        w.kAB = null; w.kAC = 0
        w.npcFsm.tickAx9(e, w, w.player)
        assertNull(w.kAB, "k.aB is aV()'s (ax10), not bM()'s")
        assertEquals(0, w.kAC)
        assertEquals(4, e.S)
    }

    @Test fun `S5 overlap plus every press changes nothing - bM() sends S5 to the bare return`() {
        for (mask in intArrayOf(16388, 2, 8)) {
            val w = world(); w.npcs.clear()
            val e = ax9At(w, 0, 0, 5)
            boxAroundPlayer(w, e)
            w.player.av = mask == 2
            val s0 = w.player.S
            w.pad.commit(mask)
            w.npcFsm.tickAx9(e, w, w.player)
            assertEquals(s0, w.player.S, "mask $mask: no aS.i(22)")
            assertEquals(5, e.S)
        }
    }
}
