package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertNull
import kotlin.test.assertSame

/**
 * Slice 399 — the context-claim channel `k.L / k.co / k.cp`
 * (`k.a(i,int,int[])` k.javap.txt:4990-5062, `k.a(int[])` :5059-5110,
 * `k.m()` :5112-5125, the touch hit-test `k.j(II)` :4465-4750).
 *
 * The port carried TWO copies of this one static: `claim/claimed/clearClaim`
 * (fed only by the ax4 crates) and `registerClaim/kL/claimReset` (everyone
 * else, and the only one the touch hit-test read), so a tap on a crate never
 * reached `k.j` and a crate bid could never be outranked by a soldier. The
 * surviving copy also deviated from the bytecode:
 *
 * - the steal rule is `prio < co || (prio == 1 && co == 1)` @54-70 — the copy
 *   stole on EVERY prio-1 bid, even from a prio-0 holder;
 * - the current owner (matched by `aw`, @6-16) only refreshes `cp`;
 * - `cp` is the bid rect padded ±10, a snapshot (`k.a(int[])`) — the copy
 *   stored the raw rect by reference, so taps ten pixels beside a claimed
 *   entity missed and the rect trailed the entity afterwards.
 *
 * And the ax11 soldier's `I()` case-11 head bid (`aH()` + `k.M` overlap →
 * `k.a(this,0,W)`, else release if ours; i.javap.txt:20029-20050) did not
 * exist at all.
 */
class Slice399Test {
    private fun ent(ax: Int, aw: Int, box: IntArray = intArrayOf(100, 100, 140, 160)): Entity =
        Entity(ax, null).also { it.aw = aw; box.copyInto(it.W); box.copyInto(it.Y) }

    // ------------------------------------------------------------ k.a() rules
    @Test fun `a lower prio steals, an equal or higher one does not`() {
        val w = world()
        w.claimReset()
        val a = ent(11, 1); val b = ent(11, 2)
        w.registerClaim(a, 3, a.W)
        assertSame(a, w.kL); assertEquals(3, w.claimCo)
        w.registerClaim(b, 3, b.W); assertSame(a, w.kL)          // equal: no
        w.registerClaim(b, 4, b.W); assertSame(a, w.kL)          // higher number: no
        w.registerClaim(b, 2, b.W)
        assertSame(b, w.kL, "2 < 3 @54-58"); assertEquals(2, w.claimCo)
    }

    @Test fun `a prio-1 bid steals only from a prio-1 or a worse holder, never from prio 0`() {
        val w = world()
        w.claimReset()
        val a = ent(11, 1); val b = ent(11, 2); val c = ent(11, 3)
        w.registerClaim(a, 0, a.W)
        w.registerClaim(b, 1, b.W)
        assertSame(a, w.kL, "prio 1 vs co 0: `prio == 1 && co == 1` fails @61-70")
        assertEquals(0, w.claimCo)
        w.claimReset()
        w.registerClaim(a, 1, a.W)
        w.registerClaim(b, 1, b.W)
        assertSame(b, w.kL, "prio 1 vs co 1: the one equal-bid steal")
        w.registerClaim(c, 1, c.W)
        assertSame(c, w.kL)
        w.claimReset()
        w.registerClaim(a, 2, a.W)
        w.registerClaim(b, 1, b.W)
        assertSame(b, w.kL, "prio 1 < co 2 steals the ordinary way")
    }

    @Test fun `out-of-range prios are ignored`() {
        val w = world()
        w.claimReset()
        val a = ent(11, 1); val b = ent(11, 2)
        w.registerClaim(a, 4, a.W)
        w.registerClaim(b, -1, b.W); assertSame(a, w.kL)
        w.claimReset()
        w.registerClaim(b, 6, b.W); assertNull(w.kL); assertEquals(6, w.claimCo)
    }

    @Test fun `the owner is matched by aw and only refreshes the rect`() {
        val w = world()
        w.claimReset()
        val a = ent(11, 7)
        w.registerClaim(a, 0, a.W)
        // a different object with the same uid, a worse prio: @6-16 → refresh, no steal
        val twin = ent(11, 7, intArrayOf(300, 310, 340, 360))
        w.registerClaim(twin, 5, twin.W)
        assertSame(a, w.kL, "kL stays the first object")
        assertEquals(0, w.claimCo, "co stays 0 — the refresh path never touches it")
        assertContentEquals(intArrayOf(290, 300, 350, 370), w.claimRect, "cp refreshed from the bid rect")
    }

    @Test fun `cp is a padded snapshot of the bid rect, not a live view`() {
        val w = world()
        w.claimReset()
        val a = ent(11, 1)
        w.registerClaim(a, 3, a.W)
        assertContentEquals(intArrayOf(90, 90, 150, 170), w.claimRect, "±10 (k.a(int[]) @0-62)")
        val cp = w.claimRect
        a.W[0] = 500; a.W[2] = 540
        assertContentEquals(intArrayOf(90, 90, 150, 170), w.claimRect, "a later move of W does not drag cp")
        w.registerClaim(a, 3, a.W)                               // the owner's next-tick re-bid
        assertContentEquals(intArrayOf(490, 90, 550, 170), w.claimRect)
        assertSame(cp, w.claimRect, "the same int[4] is rewritten in place")
    }

    @Test fun `ax51 binds its Y rect instead of the passed one`() {
        val w = world()
        w.claimReset()
        val crate = ent(51, 9, intArrayOf(100, 100, 140, 160))
        intArrayOf(104, 120, 136, 160).copyInto(crate.Y)
        w.registerClaim(crate, 3, crate.W)
        assertContentEquals(intArrayOf(94, 110, 146, 170), w.claimRect, "@90-94: aload_0.Y")
    }

    @Test fun `k_m resets co, L and cp`() {
        val w = world()
        val a = ent(11, 1)
        w.registerClaim(a, 2, a.W)
        w.claimReset()
        assertNull(w.kL); assertEquals(6, w.claimCo)
        assertContentEquals(IntArray(4), w.claimRect, "cp = new int[4]")
        assertNotNull(w.claimRect)
    }

    @Test fun `removing the claimant drops the claim (port safety net)`() {
        val w = world()
        val a = ent(11, 1)
        w.registerClaim(a, 2, a.W)
        w.npcs.add(a)
        w.removeEntity(a)
        assertNull(w.kL)
    }

    // ------------------------------------------------------------ one channel
    @Test fun `an ax4 crate bids on the same channel a soldier does`() {
        val w = world()
        w.claimReset()
        val d = w.npcs.first { it.ax == 4 && it.S == 5 }
        d.refreshBoxes()
        val p = w.player
        p.setAnim(0)
        intArrayOf(-4000, -4000, -3980, -3960).copyInto(p.W)
        intArrayOf(-3000, -3000, -2980, -2960).copyInto(p.X)
        d.W.copyInto(w.kM)
        w.npcFsm.tickDestructible(d, p)
        assertSame(d, w.kL); assertEquals(5, w.claimCo)
        assertContentEquals(intArrayOf(d.W[0] - 10, d.W[1] - 10, d.W[2] + 10, d.W[3] + 10),
            w.claimRect, "the touch layer reads this rect")
    }

    @Test fun `a prio-0 holder keeps the crate's prio-5 bid out`() {
        val w = world()
        w.claimReset()
        val d = w.npcs.first { it.ax == 4 && it.S == 5 }
        d.refreshBoxes()
        val p = w.player
        p.setAnim(0)
        intArrayOf(-4000, -4000, -3980, -3960).copyInto(p.W)
        intArrayOf(-3000, -3000, -2980, -2960).copyInto(p.X)
        val soldier = ent(11, 4000)
        w.registerClaim(soldier, 0, soldier.W)
        d.W.copyInto(w.kM)
        w.npcFsm.tickDestructible(d, p)
        assertSame(soldier, w.kL, "5 < 0 is false @54-58")
        assertEquals(0, w.claimCo)
    }

    // ------------------------------------------------------------ touch hit-test
    /** Play state with the STYLE option on the wheel/tap scheme (`k.cm = 0`,
     *  `!k()`): only there does `k.j(II)` reach the claim rect @285 — with the
     *  mounted pad a tap outside the pad box returns -1 @158-216 first. */
    private fun worldAtPlay(): Level0World {
        val w = world(); w.stateL(8); w.claimReset()
        w.cm = 0
        return w
    }

    @Test fun `a tap on the padded claim rect resolves to action 4 (1 for a prio-1 claim)`() {
        val w = worldAtPlay()
        // a claimed box in the top-left of the view, far from the player's wheel
        val e = ent(11, 5000, intArrayOf(w.camX + 20, w.camY + 20, w.camX + 60, w.camY + 60))
        w.registerClaim(e, 0, e.W)
        assertEquals(4, w.resolvePadZone(40, 40), "inside W")
        assertEquals(4, w.resolvePadZone(15, 40), "5 px left of W: inside the ±10 pad")
        assertEquals(4, w.resolvePadZone(68, 40), "8 px right of W: inside the pad")
        assertNotEquals(4, w.resolvePadZone(5, 40), "15 px left of W: outside the pad")
        w.claimReset()
        w.registerClaim(e, 1, e.W)
        assertEquals(1, w.resolvePadZone(40, 40), "co == 1 → action 1 (@285-358)")
    }

    @Test fun `a tap on an adjacent soldier is an attack, not a wheel direction`() {
        val w = worldAtPlay()
        val s = w.npcs.first { it.ax == 11 }
        s.setAnim(2)
        s.setPositionPx(w.camX + 100, w.camY + 120)              // on screen, clear of the soft keys
        s.refreshBoxes()
        // the soldier in the player's reach box → it bids prio 0 in its I() head
        s.W.copyInto(w.kM)
        w.npcFsm.tick(s, w.player)
        assertSame(s, w.kL)
        val cx = (s.W[0] + s.W[2]) / 2 - w.camX
        val cy = (s.W[1] + s.W[3]) / 2 - w.camY
        assertEquals(4, w.resolvePadZone(cx, cy), "the tap lands on the claimed soldier")
    }

    // ------------------------------------------------------------ ax11 head bid
    private fun soldier(w: Level0World): Entity {
        val s = w.npcs.first { it.ax == 11 }
        s.setAnim(2)                                             // a patrol state (∉ aH)
        s.refreshBoxes()
        return s
    }

    @Test fun `an ax11 in reach bids prio 0 with its own W`() {
        val w = world(); w.claimReset()
        val s = soldier(w)
        s.W.copyInto(w.kM)
        w.npcFsm.tick(s, w.player)
        assertSame(s, w.kL); assertEquals(0, w.claimCo)
        assertContentEquals(intArrayOf(s.W[0] - 10, s.W[1] - 10, s.W[2] + 10, s.W[3] + 10), w.claimRect)
    }

    @Test fun `an ax11 out of reach releases its own claim and leaves a foreign one`() {
        val w = world(); w.claimReset()
        val s = soldier(w)
        s.W.copyInto(w.kM)
        w.npcFsm.tick(s, w.player)
        assertSame(s, w.kL)
        intArrayOf(-2000, -2000, -1980, -1960).copyInto(w.kM)
        w.npcFsm.tick(s, w.player)
        assertNull(w.kL, "k.L.aw == aw → k.m() @1678-1697")
        // a foreign holder is untouched
        val other = ent(4, 4001)
        w.registerClaim(other, 5, other.W)
        w.npcFsm.tick(s, w.player)
        assertSame(other, w.kL)
    }

    @Test fun `an ax11 in an aH state never bids`() {
        for (st in intArrayOf(0, 20, 21, 106, 107, 117, 139, 168, 169, 176)) {
            val w = world(); w.claimReset()
            val s = soldier(w)
            s.setAnim(st); s.refreshBoxes()
            s.W.copyInto(w.kM)
            w.npcFsm.tick(s, w.player)
            assertNotSame(s, w.kL, "S$st ∈ aH() @1647-1650")
        }
    }

    @Test fun `an ax11 in an aH state releases a claim it holds`() {
        val w = world(); w.claimReset()
        val s = soldier(w)
        s.W.copyInto(w.kM)
        w.npcFsm.tick(s, w.player)
        assertSame(s, w.kL)
        s.setAnim(21)
        w.npcFsm.tick(s, w.player)
        assertNull(w.kL)
    }
}
