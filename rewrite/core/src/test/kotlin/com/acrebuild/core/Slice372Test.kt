package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 372 — the player slot runs no wall rescan before `g.e()`.
 *
 * `i.I()` (the player's entity tick) goes straight from the integrate into
 * `g.e()`; the only side-collides are the head's `a(an())` (g.javap.txt
 * e() 494) and the arm sites (6328 grounded, 6550 S32, 8259 air,
 * 10143/10187 stagger, 12334 S217, 12556 S242/243, 13425 S311/312, plus
 * `E()`/`au()`) — proven by grepping every `invokevirtual i.a:(Z)V` in
 * g.javap.txt. The slice-2 port ran an extra `a(true)` in `tickPlayerI`,
 * pushing him out of walls in every state, including the ones whose arm
 * leaves him embedded on purpose.
 *
 * Two bytecode misreads surfaced with it:
 *  - the S32 arm (e() 6512-6630) kept only `i.f(this)` and the anim-end
 *    exit: it lacked the airborne fall, `ag != 0 → a(true)`, the wall stop
 *    `y() && ag != 0 → ag = 0` and the `S == 0 → x(); aO > 12 → i(79)`
 *    re-embed after `r()`;
 *  - `x()` cleared `bd` on a '5' cell (the simple decompile prints the
 *    block layout `L9: if (aR != 5) goto L69; L5: bd = false`); the
 *    bytecode (i.javap.txt x() 159-176) falls straight into
 *    `r8 = r02 % 20`, so a '5' cell under a moving entity keeps `bd`, and
 *    `a(true)`'s ground pre-adjust (`al -= x()`) snaps him onto the strip.
 */
class Slice372Test {
    private fun player(ak: Int, al: Int, s: Int): Entity {
        val p = Entity(0, null)
        p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10; p.W[1] = al - 20; p.W[3] = al
        p.S = s
        return p
    }

    // -- x(): '5' keeps bd ----------------------------------------------------

    @Test fun `a one-way cell under the feet keeps bd = ah != 0`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy == 6) 5 else 0 })
        val p = player(200, 125, 43)             // W[3]+1 = 126 → row 6
        p.ah = 100
        p.probeCells(w)
        assertEquals(5, p.aR)
        assertTrue(p.bd, "aR == 5 falls into `r8 = r02 % 20` with bd = (ah != 0)")
        p.ah = 0
        p.probeCells(w)
        assertFalse(p.bd, "no vertical motion: bd stays false")
    }

    @Test fun `a solid head pair still clears bd on a one-way cell`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy ->
            if (cy == 6) 5 else if (cy == 4 || cy == 5) 20 else 0 })
        val p = player(200, 125, 43)
        p.W[1] = 85                              // aO = row 4, aP = row 5: solid
        p.ah = 100
        p.probeCells(w)
        assertEquals(5, p.aR)
        assertFalse(p.bd, "aO >= 12 && aP >= 12 → bd = false (x() 182-205)")
    }

    @Test fun `a(true) snaps a falling entity out of a one-way cell`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy == 6) 5 else 0 })
        val p = player(200, 125, 43)             // feet 6px into the cell
        p.ah = 100
        p.collideSides(w, true)
        assertEquals(119, p.al, "bd && aR == 5 → al -= x() (126 % 20 = 6)")
        val q = player(200, 125, 43)             // ah == 0: bd false, no snap
        q.collideSides(w, true)
        assertEquals(125, q.al)
    }

    // -- the S32 run-start arm --------------------------------------------------

    @Test fun `S32 falls when it has no ground and no ride`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, _ -> 0 })
        val p = player(200, 119, 32)
        PlayerFsm(w).tick(p, Pad())
        assertEquals(43, p.S, "!aZ && g.a == null → a(0)")
    }

    @Test fun `S32 on a ride link stays in S32`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, _ -> 0 })
        val p = player(200, 119, 32)
        val lift = Entity(66, null); lift.S = 6
        p.ga = lift
        PlayerFsm(w).tick(p, Pad())
        assertNotEquals(43, p.S, "g.a != null spares the fall (6516-6526)")
    }

    @Test fun `S32 end under a solid head cell re-embeds into S79`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy ->
            if (cy >= 6 || cy == 4) 20 else 0 })     // ceiling slab at the head row
        val p = player(200, 119, 32)             // clipless: r() is true at once
        p.Q = 0
        PlayerFsm(w).tick(p, Pad())
        assertEquals(79, p.S, "i(0); x(); aO > 12 → i(79) (6603-6627)")
    }

    @Test fun `S32 end with a free head cell settles to S0`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy >= 6) 20 else 0 })
        val p = player(200, 119, 32)
        p.Q = 0
        PlayerFsm(w).tick(p, Pad())
        assertEquals(0, p.S)
    }

    @Test fun `S32 entered from S79 returns to S79`() {
        val w = Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy >= 6) 20 else 0 })
        val p = player(200, 119, 32)
        p.Q = 79
        PlayerFsm(w).tick(p, Pad())
        assertEquals(79, p.S, "i(Q == 79 ? 79 : 0)")
    }

    // -- no pre-dispatch rescan ----------------------------------------------------

    private fun settled(): Pair<Level0World, Entity> {
        val w = world(); settleIntro(w)
        val p = w.player
        p.S = 0; p.ag = 0; p.ah = 0; p.aA = 0
        return w to p
    }

    /** East of the spawn floor's x360 block (cols 18-40, rows 44-46) the
     *  strips read the wall on one side only, for x in ~354-366. */
    private fun oneSidedX(w: Level0World, p: Entity, s: Int): Int {
        p.setAnim(s)
        for (x in 372 downTo 330) {
            p.setPositionPx(x, 940)
            p.refreshBoxes()
            p.collideSides(w, false)
            // one side only, and the box really overlaps the wall column
            if (p.bb != p.bc && (p.W[2] + 1) % 20 != 0) return x
        }
        error("no one-sided wall position beside the x360 block for S$s")
    }

    @Test fun `a state outside an() is not pushed out of a wall`() {
        val (w, p) = settled()
        val x = oneSidedX(w, p, 78)                  // crouch dip: not in an()
        p.setAnim(78)
        p.setPositionPx(x, 940); p.ag = 0; p.ah = 0
        p.refreshBoxes()
        w.tick(emptyList())
        assertEquals(78, p.S, "still in the crouch dip")
        assertEquals(x, p.ak, "no rescan before g.e(): S78 is not in an() " +
            "and its arm has no a(true)")
    }

    @Test fun `a state in an() is pushed out by the head rescan`() {
        val (w, p) = settled()
        val x = oneSidedX(w, p, 0)                   // S0 <= 43 → a(true) in the head
        p.setAnim(0)
        p.setPositionPx(x, 940); p.ag = 0; p.ah = 0
        p.refreshBoxes()
        w.tick(emptyList())
        assertTrue(p.ak < x, "g.e() head a(an()) pushes him out of the wall — ak=${p.ak}")
    }

    @Test fun `S32 stops against a wall`() {
        val (w, p) = settled()
        p.setAnim(32)
        p.setPositionPx(335, 940)                    // +10 integrate → the one-sided band
        p.ag = 2560; p.ah = 0
        p.refreshBoxes()
        w.tick(emptyList())
        assertEquals(32, p.S, "the run-start anim is still live")
        assertEquals(0, p.ag, "y() && ag != 0 → ag = 0 (6553-6569)")
    }

    @Test fun `S32 runs on in the open`() {
        val (w, p) = settled()
        p.setAnim(32)
        p.setPositionPx(120, 940)
        p.ag = 2560; p.ah = 0
        p.refreshBoxes()
        w.tick(emptyList())
        assertEquals(32, p.S)
        assertEquals(2560, p.ag, "no wall: the run-start keeps its speed")
    }

    @Test fun `S32 falls off a ledge without a ride`() {
        val (w, p) = settled()
        p.setAnim(32)
        p.setPositionPx(120, 700)                    // open air over the spawn room
        p.ag = 2560; p.ah = 0
        p.refreshBoxes()
        w.tick(emptyList())
        assertEquals(43, p.S, "!aZ && g.a == null → cq = 0; a(0) (6516-6538)")
    }
}
