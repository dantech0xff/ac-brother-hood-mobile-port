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

/** Slice 198 — `k.au` difficulty index wired into spawn HP
 *  (`bu`/`bv` tables) + stale-comment sweep (Y bounds, `i.cu`
 *  consumers, `k.l(15)` transition, `k.E` producer, `k.bh[k.aj]`). */
class Slice198Test {
    @Test fun `soldier HP indexes bu by difficulty`() {
        val w = Slice197Test.WarpWorld()
        val fsm = NpcFsm(w)
        val e = Entity(11, null)
        w.kAu = 0; fsm.initSoldier(e, listOf(), w)
        assertEquals(300, e.aB, "bu[0]=300")
        val e2 = Entity(11, null)
        w.kAu = 2; fsm.initSoldier(e2, listOf(), w)
        assertEquals(500, e2.aB, "bu[2]=500 (i.java:2230)")
    }

    @Test fun `ax17 HP indexes bv by difficulty`() {
        val w = Slice197Test.WarpWorld()
        val fsm = NpcFsm(w)
        w.kAu = 1
        val e = Entity(17, null)
        fsm.initAx17(e, listOf(0))
        assertEquals(140, e.aB, "bv[1]=140 (i.java:3011)")
    }
}
