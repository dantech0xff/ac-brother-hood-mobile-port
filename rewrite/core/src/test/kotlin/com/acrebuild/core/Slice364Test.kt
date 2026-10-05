package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 364 — every rect test that ports the original's
 * `i.a(int[],int[])` (structured i.java:540-558, bytecode `a:([I[I)Z`)
 * rejects point boxes (`[0]==[2] && [1]==[3]` on either side).
 *
 * The port had three plain inclusive overlaps without those rejects —
 * `NpcFsm.rectsOverlap`, `Level0World.rectsOverlap` and `Entity.overlapI` —
 * plus two inline copies (`Entity.yOverlapsCam`, `PlayerFsm.flightAliveV`).
 * Every call site now goes through the primitive the bytecode calls:
 * `Entity.overlapStrict` (`i.a([I[I)Z`), `Entity.edgeRectOverlap`
 * (`i.a(IIII[I)Z`, l()'s af.S==6 arm) or `Entity.containRect`
 * (`i.b([I[I)Z`, k.m()'s rope camera). Two operands were wrong too:
 * aV() S48/S49 test the linked target's own `Y` (`aload_1`), and v()'s
 * ax14 bh3/S69-71 arm tests `W` (offset 251), not `Y`.
 *
 * Point boxes are real clip data: frames without a rect row give `t()`
 * an anchor-point box (player S101 T0, ax4 S29 T0/T1/T6/T7, ax11 S12
 * T0/T6, ...), and the original never reports contacts on them.
 */
class Slice364Test {
    private fun point(r: IntArray) = r[0] == r[2] && r[1] == r[3]

    @Test fun `a wall-cling point frame never latches the S46 wall-run zone`() {
        val w = world()
        val p = w.player
        p.setPositionPx(400, 300)
        p.setAnim(101); p.T = 0; p.refreshBoxes()
        assertTrue(point(p.W), "player S101 T0 has no W rect: t() leaves the anchor point")
        val z = Entity(10, null); z.S = 46
        intArrayOf(p.ak - 30, p.al - 60, p.ak + 30, p.al + 10).copyInto(z.W)
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertFalse(Entity.gq, "aV() @491 i.a(aS.W, W): a point W never overlaps")
        // control: a real W in the same spot latches g.q
        intArrayOf(p.ak - 10, p.al - 40, p.ak + 10, p.al).copyInto(p.W)
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertTrue(Entity.gq)
    }

    @Test fun `S48 tests the linked target's Y, not the player's`() {
        val w = world()
        val z = Entity(10, null); z.S = 48; z.oId = 7_777; z.pv = 9
        intArrayOf(5_000, 100, 5_040, 140).copyInto(z.W)
        val t = Entity(9, null); t.aw = 7_777
        intArrayOf(9_000, 900, 9_020, 930).copyInto(t.Y)         // far away
        w.npcs += t
        val p = w.player
        intArrayOf(5_010, 110, 5_030, 130).copyInto(p.Y)         // player inside
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(0, t.aG, "aV() @378 i.a(r8.Y, W): the player's Y is not read")
        assertFalse(z in w.pendingRemove)
        intArrayOf(5_010, 110, 5_030, 130).copyInto(t.Y)         // target arrives
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(9, t.aG, "target Y ∩ W → aG = p")
        assertTrue(z in w.pendingRemove)
    }

    @Test fun `the a(i, int, int-array) side push ignores a point player box`() {
        val w = world()
        val p = w.player
        val e = Entity(51, null); e.P = 4096
        intArrayOf(1_000, 400, 1_100, 500).copyInto(e.W)
        p.setPositionPx(1_050, 450)
        intArrayOf(1_050, 450, 1_050, 450).copyInto(p.W)         // point
        p.ag = 256; p.ah = 0
        w.npcFsm.defaultArm(e, p)
        assertEquals(1_050, p.ak, "a(i,int,int[]) @13 i.a(r7.W, r9): no push")
        assertEquals(256, p.ag)
        intArrayOf(1_040, 400, 1_060, 450).copyInto(p.W)         // real box
        w.npcFsm.defaultArm(e, p)
        assertNotEquals(1_050, p.ak, "control: a real box is pushed out")
        assertEquals(0, p.ag)
    }

    @Test fun `l() af-S6 zone rect is the edges variant with its point rejects`() {
        val w = world()
        val p = w.player
        val e = Entity(11, null)
        e.setPositionPx(p.ak, p.al)                               // same row
        p.W.copyInto(e.W)                                         // LOS: one cell
        e.av = false
        e.Z[10] = e.ak                                            // x1 == x0
        e.Z[11] = e.al - 30; e.Z[12] = e.al - 30                  // y1 == y0
        val af = Entity(69, null); af.S = 6; af.Z[0] = 1
        intArrayOf(e.ak - 10, e.al - 40, e.ak + 10, e.al - 20).copyInto(af.W)
        p.af = af
        assertFalse(losL(e, p, w), "l() @350 i.a(x0,y0,x1,y1,af.W): a point zone is blind")
        e.Z[10] = e.ak + 20                                       // a real span
        assertTrue(losL(e, p, w))
    }

    @Test fun `bd() rest sweep skips a neighbor whose W is a point`() {
        val w = world(); w.npcs.clear()
        val e = Entity(16, null)
        intArrayOf(100, 100, 140, 140).copyInto(e.X)
        val n = Entity(19, null); n.S = 2
        intArrayOf(120, 120, 120, 120).copyInto(n.W)              // point inside X
        w.npcs += n
        w.paint(n)
        e.sweepNeighbors(w)
        assertEquals(2, n.S, "bd() @43 i.a(bd[i].W, X): a point W is never hit")
        intArrayOf(115, 115, 125, 125).copyInto(n.W)
        e.sweepNeighbors(w)
        assertEquals(3, n.S, "control: ax19 S2 → i(3)")
    }

    @Test fun `v() ax14 S69 arm reads W through i-a`() {
        val w = world()
        val e = Entity(14, null); e.S = 69
        e.setPositionPx(w.camX + 200, w.camY + 120)               // au = 0
        intArrayOf(e.ak - 10, e.al - 10, e.ak + 10, e.al + 10).copyInto(e.W)
        intArrayOf(w.camX + 5_000, 0, w.camX + 5_010, 10).copyInto(e.Y)
        assertTrue(e.inPlayV(w), "v() @251 i.a(k.ac, W) — not Y")
        intArrayOf(e.ak, e.al, e.ak, e.al).copyInto(e.W)          // point W
        assertFalse(e.inPlayV(w), "a point W is off-camera for i.a")
    }

    @Test fun `v() generic arm rejects a point Y`() {
        val w = world()
        val e = Entity(67, null)
        e.setPositionPx(w.camX + 200, w.camY + 120)
        intArrayOf(e.ak, e.al, e.ak, e.al).copyInto(e.Y)
        assertFalse(e.inPlayV(w), "v() @331 i.a(k.ac, Y): point Y")
        intArrayOf(e.ak - 10, e.al - 10, e.ak + 10, e.al + 10).copyInto(e.Y)
        assertTrue(e.inPlayV(w))
    }

    @Test fun `k-m rope camera tests containment, not overlap`() {
        fun step(yBox: IntArray): Int {
            val w = world()
            w.kO = 1_000; w.kP = 500; w.rebuildCamRect()
            yBox.copyInto(w.player.Y)
            val rope = Entity(43, null)
            rope.S = 1; rope.av = true; rope.ak = 1_100; rope.al = 600
            rope.Z[1] = 10
            intArrayOf(1_350, 550, 1_450, 600).copyInto(rope.Y)   // Y[2] > ac[2]
            w.kAe = rope
            val x0 = w.camX
            w.kM(0)
            return w.camX - x0
        }
        // straddling the right edge: overlaps k.ac but is not inside it →
        // `!i.b(aS.Y, k.ac)` → `ae.Y[2] > ac[2]` → cC = Z[1]*50/100
        assertEquals(5, step(intArrayOf(1_390, 600, 1_420, 640)),
            "k.m() @1392 i.b(aS.Y, k.ac): containment")
        // fully inside → in view, dx = 100 < 200 → cC = Z[1]*150/100
        assertEquals(15, step(intArrayOf(1_300, 600, 1_330, 640)))
    }
}
