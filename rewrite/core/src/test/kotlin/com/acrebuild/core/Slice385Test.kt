package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 385 — every sim neighbour scan in `i` walks `k.bd[0 until k.be]`,
 * the list the LAST `b()` pass drew (on-screen entities, the player, the
 * `ae` children, `az` order), never the `bb[]` pool. `W()` nulls the
 * slots and leaves `be`. Bytecode fixes met on the way: n() S4 ax11 arm
 * and S6 `bb||bc` / self-match, be() stops at its first hit, c(Z) hands
 * velocity to one S11 member, bl() stamps `this.aa.b(this.S)`, br()
 * reports every bind but a held crate's, bu()'s door collapse ends the
 * sweep, bQ() S12 flinches like 0/1/7.
 */
class Slice385Test {
    @Test fun `a neighbour scan walks the last paint's list, not the pool`() {
        val w = world()
        settleIntro(w)
        val d = w.npcs.first { it.ax == 4 }
        d.S = 29; d.T = 3; d.refreshBoxes()
        val s = Entity(11, null).apply {
            aB = 100; ak = (d.X[0] + d.X[2]) / 2; al = (d.X[1] + d.X[3]) / 2
            W[0] = ak - 5; W[1] = al - 5; W[2] = ak + 5; W[3] = al + 5
        }
        w.npcs += s
        w.player.setPositionPx(d.X[2] + 500, d.X[3] + 500); w.player.refreshBoxes()
        w.paint(d)
        w.npcFsm.tickDestructible(d, w.player)
        assertEquals(100, s.aB, "not drawn last frame → not in k.bd")
        w.paint(d, s)
        w.npcFsm.tickDestructible(d, w.player)
        assertTrue(s.aB < 100, "drawn → the aj() S29 blast reaches it")
    }

    @Test fun `the tick's b() pass leaves the list the next sim reads`() {
        val w = world()
        settleIntro(w)
        w.tick(emptyList())
        assertTrue(w.player in w.drawn, "the player is a draw-list member")
        val far = w.npcs.firstOrNull {
            !it.inPlayV(w) && (it.P and 16) == 0 && it.aw != 205 &&
                it.ax != 10 && it.ax != 51
        }
        if (far != null) assertFalse(far in w.drawn, "off-screen → undrawn")
        for (e in w.drawn) assertTrue(e === w.player || e in w.npcs ||
            w.npcs.any { it.ae === e } || w.player.ae === e)
    }

    @Test fun `W() empties the slots but keeps be`() {
        val w = world()
        settleIntro(w)
        val n = w.drawCount
        assertTrue(n > 0)
        w.stateL(15)
        var t = 0
        while (w.drawList[0] != null && t++ < 5) w.tick(emptyList())
        assertEquals(n, w.drawCount, "be is not reset (k.java:5121-5126)")
        assertTrue((0 until n).all { w.drawList[it] == null })
        assertFalse(w.drawn.iterator().hasNext())
    }

    @Test fun `a drawn moving ax41 tumble meets itself and settles`() {
        val w = world(); w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = Entity(41, w.clips[7]).apply {
            setPositionPx(300, 150); refreshBoxes(); S = 6; ah = 400
        }
        w.npcs += e
        w.paint()
        w.npcFsm.tickKnockable(e, w, w.player)
        assertEquals(6, e.S, "undrawn: keeps tumbling")
        w.paint(e)
        w.npcFsm.tickKnockable(e, w, w.player)
        assertEquals(4, e.S, "n() @442-551: no this-skip — i(this) holds")
    }

    @Test fun `be() marks only the first drawn overlap`() {
        val w = world(); w.npcs.clear()
        w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
        val e = Entity(58, w.clips[20]).apply {
            S = 0; setPositionPx(300, 150)
            W[0] = 290; W[1] = 140; W[2] = 310; W[3] = 160
        }
        w.npcs.add(0, e)
        val g1 = Entity(11, null).apply { intArrayOf(295, 145, 305, 155).copyInto(W) }
        val g2 = Entity(15, null).apply { intArrayOf(295, 145, 305, 155).copyInto(W) }
        w.npcs += g1; w.npcs += g2
        w.paint(e, g1, g2)
        w.npcFsm.tickAx58(e, w, w.player)
        assertEquals(16, g1.P and 16)
        assertEquals(0, g2.P and 16, "be() @113: goto 122 after the first hit")
        assertEquals(1, e.S)
    }

    @Test fun `c(Z) hands the S13 velocity to the first S11 member only`() {
        val w = world()
        fun ax60(x: Int, s: Int): Entity {
            val e = Entity(60, w.clips[21])
            val rec = mutableListOf(60, 1, x, 200, 4, s, 0, -1, 0, 0)
            while (rec.size < 22) rec += 0
            e.setPositionPx(x, 200)
            w.npcFsm.initAx60(e, rec, w)
            w.npcs.add(e); e.P = e.P or 16
            return e
        }
        val p1 = ax60(115, 11)
        val p2 = ax60(115, 11)
        val e = ax60(100, 13)
        e.ag = 1024
        w.paint(p1, p2, e)
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(1024, p1.ag)
        assertEquals(0, p2.ag, "c(Z) @792: goto 801 after the first")
        assertEquals(-1024, e.ag)
    }

    @Test fun `bl() escape stamps the grabber's own anim length into cz`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // any clip whose anims 2 and 4 differ in length shows whose
        // `aa.b(S)` lands in cz
        val c = w.clips.values.first {
            runCatching { it.frameCount(2) != it.frameCount(4) && it.frameCount(5) > 0 }
                .getOrDefault(false)
        }
        val e = Entity(64, c).apply { aw = 9_401; setAnim(2); aC = 30 }
        val n = Entity(64, c).apply { aw = 9_402; setAnim(2); Z[6] = 1 }
        w.npcs += e; w.npcs += n
        assertNotEquals(c.frameCount(2), c.frameCount(4), "fixture: distinct lengths")
        p.bl = 5; p.bm = 0
        w.pad.commit(4112)
        w.paint(n, e)
        w.npcFsm.tickAx64(e, w, p)
        assertEquals(9, p.S, "mash escape → aS.i(9)")
        assertEquals(5, n.S); assertEquals(4, e.S)
        assertEquals(c.frameCount(2), n.cz, "bl() @2879-2896: this.aa.b(this.S), e still S2")
        assertEquals(c.frameCount(4), e.cz, "its own entry flipped it to S4 first")
    }

    @Test fun `br() reports a bind on a crate that is not held`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setAnim(0); p.av = false
        p.setPositionPx(300, 150); p.refreshBoxes()
        val e = Entity(51, w.clips[7]).apply { setPositionPx(300, 150); refreshBoxes(); S = 0 }
        val o = Entity(51, w.clips[7]).apply { setPositionPx(400, 150); refreshBoxes(); S = 1 }
        w.npcs += e; w.npcs += o
        w.paint(e, o)
        assertTrue(w.npcFsm.platformGrabCheck(e, w, p), "br() @345-372: z2 = true")
        assertSame(o, p.ac)
    }

    @Test fun `bu() door collapse ends the S6 sweep`() {
        fun run(doorFirst: Boolean): Pair<Entity, Entity> {
            val w = world(); w.npcs.clear()
            w.player.setPositionPx(9000, 9000); w.player.refreshBoxes()
            // clipless: clip25's S6 W is an anchor point (no `i.a` hit), so
            // the block's box is staged by hand (refreshBoxes keeps it).
            val e = Entity(15, null)
            e.setPositionPx(300, 150)
            val f = mutableListOf(15, 0, 300, 150, 0, 6, 0, 0, -1)
            for (i in 9..15) f += 0
            w.npcFsm.initAx15(e, f, w)
            intArrayOf(280, 130, 320, 170).copyInto(e.W)
            w.npcs += e
            val door = Entity(44, null).apply { S = 0; intArrayOf(0, 0, 1000, 1000).copyInto(X) }
            val soldier = Entity(11, null).apply {
                S = 3; setPositionPx(300, 150); intArrayOf(0, 0, 1000, 1000).copyInto(W)
            }
            w.npcs += door; w.npcs += soldier
            if (doorFirst) w.paint(e, door, soldier) else w.paint(e, soldier, door)
            w.npcFsm.tickAx15(e, w, w.player)
            return e to soldier
        }
        val (e1, s1) = run(doorFirst = true)
        assertEquals(7, e1.S, "door X ∩ W on S6 → i(7)")
        assertEquals(3, s1.S, "bu() @1222: goto 1707 — the sweep ends")
        val (e2, s2) = run(doorFirst = false)
        assertEquals(7, e2.S)
        assertEquals(2, s2.S, "drawn before the door → shoved")
    }

    @Test fun `bQ() S6-9 flinches an S12 player and drains like the rest`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = Entity(35, w.clips[62])
        e.setPositionPx(300, 200)
        val f = mutableListOf(35, 0, 300, 200, 0, 6, 0, 0)
        repeat(10) { f += 0 }
        w.npcFsm.initAx35(e, f, w)
        w.npcs += e
        e.aG = 0; e.refreshBoxes()
        p.setAnim(12); p.gt = 0
        p.setPositionPx((e.W[0] + e.W[2]) / 2, (e.W[1] + e.W[3]) / 2); p.refreshBoxes()
        assertTrue(Entity.overlapStrict(e.W, p.W), "fixture overlap")
        val x1 = p.x1
        w.paint(e)
        w.npcFsm.tickAx35(e, w, p)
        assertEquals(9, p.S, "bQ() @1583-1599: S12 → aS.i(9)")
        assertEquals(5, p.gt, "@1612: g.t = 5")
        assertTrue(p.x1 < x1, "@1602-1609: g.d(u[au])")
    }
}
