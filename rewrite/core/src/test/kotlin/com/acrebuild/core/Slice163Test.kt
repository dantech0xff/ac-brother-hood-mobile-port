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

class Slice163Test {

    @Test fun `ax4 S29 blast sweep damages melee set and chains sibling`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val d = w.npcs.first { it.ax == 4 }
        // clip3 S29 carries a real blast box only on T2-T5 (T0/T1/T6/T7
        // are anchor points, which `i.a` rejects — slice 364): sweep on T3.
        d.S = 29; d.T = 3; d.refreshBoxes()
        // ax11 melee inside X, aB>0 -> -bu[au]<<1 (kAu=0 -> 600)
        val s = Entity(11, null).apply {
            aB = 100; ak = (d.X[0] + d.X[2]) / 2; al = (d.X[1] + d.X[3]) / 2
            W[0] = ak - 5; W[1] = al - 5; W[2] = ak + 5; W[3] = al + 5
        }
        // ax4 sibling in S30 -> i(29) chain
        val sib = Entity(4, d.clip).apply {
            S = 30; ak = s.ak; al = s.al
            W[0] = s.W[0]; W[1] = s.W[1]; W[2] = s.W[2]; W[3] = s.W[3]
        }
        // ax15-S6 grapple volume -> i(7) + Z[3]=1
        val g15 = Entity(15, null).apply {
            S = 6; ak = s.ak; al = s.al
            W[0] = s.W[0]; W[1] = s.W[1]; W[2] = s.W[2]; W[3] = s.W[3]
        }
        // ax29 boss outside S20 -> -50 + i(20)
        val boss = Entity(29, null).apply {
            S = 10; aB = 200; ak = s.ak; al = s.al
            W[0] = s.W[0]; W[1] = s.W[1]; W[2] = s.W[2]; W[3] = s.W[3]
        }
        w.npcs += s; w.npcs += sib; w.npcs += g15; w.npcs += boss
        // player OUT of X so applyHit doesn't fire
        w.player.setPositionPx(d.X[2] + 500, d.X[3] + 500)
        w.player.refreshBoxes()
        w.paint(d, s, sib, g15, boss)
        w.npcFsm.tickDestructible(d, w.player)
        assertTrue(s.aB <= 100 - 600, "ax11 aB drained by bu[au]<<1, got ${'$'}${'{'}s.aB}")
        assertEquals(29, sib.S)
        assertEquals(7, g15.S); assertEquals(1, g15.Z[3])
        assertEquals(150, boss.aB); assertEquals(20, boss.S)
        // advanceAnim wraps T5->6 at U0 so the arm reads T==6&&U==0 ->
        // sfx12. advanceAnim is explicit — the I() preamble owns s() now
        // (i.java:15232).
        d.T = 5; d.U = 999
        d.advanceAnim()
        w.npcFsm.tickDestructible(d, w.player)
        assertTrue(12 in w.sfxLog)
    }

    @Test fun `ax4 S29 player overlap routes op4 hit`() {
        val w = world()
        val d = w.npcs.first { it.ax == 4 }
        d.S = 29; d.T = 3; d.refreshBoxes()          // T3: real blast box
        w.player.setPositionPx((d.X[0] + d.X[2]) / 2, (d.X[1] + d.X[3]) / 2)
        w.player.refreshBoxes()
        w.npcFsm.tickDestructible(d, w.player)
        assertTrue(18 in w.sfxLog, "op4 tail always plays hurt sfx k.A(18)")
    }

    @Test fun `ax4 S30 proximity re-arms blast on hitbox overlap`() {
        val w = world()
        val d = w.npcs.first { it.ax == 4 }
        d.S = 30; d.refreshBoxes()
        // player.X (attack box) reaching W -> i(29). The S67 swing has a
        // real attack box only on T1 (T0/T2-T4 are anchor points, which
        // `i.a` rejects — slice 364).
        w.player.setPositionPx(d.W[0] + 1, d.W[3] - 1)
        w.player.setAnim(67); w.player.T = 1
        w.player.refreshBoxes()
        w.npcFsm.tickDestructible(d, w.player)
        assertEquals(29, d.S)
    }

    @Test fun `ax4 S33 fly-out sparks child when player near`() {
        val w = world()
        val d = w.npcs.first { it.ax == 4 }
        d.S = 33; d.aA = 0; d.refreshBoxes()
        w.player.setPositionPx(d.ak + 10, d.al)
        w.player.refreshBoxes()
        val before = w.pendingInsert.size
        w.npcFsm.tickDestructible(d, w.player)
        assertTrue(d.b)
        assertNotNull(d.af)
        assertEquals(24, d.af!!.ax); assertEquals(35, d.af!!.S)
        assertEquals(200, d.af!!.az)
        assertTrue(w.pendingInsert.size > before)
    }
}
