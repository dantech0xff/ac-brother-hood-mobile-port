package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 410 — the hit intake `i.a(IIILi;)V` (`i.javap.txt:18292`), re-read arm by arm from the
 * raw bytes.
 *
 * - **Head @0-37** (`proven`): `if (op == 4 && g.b(aS.S) && g.t == 0 && !g.s) { op = 18; ag = 0 }`.
 *   `g.b(I)Z` is the static *air / hang / climb* set (g.javap `b(int)`: 18-20, 22-25, 35, 36, 43,
 *   150, 157, 165, 233, 242, 243, 263-266) — NOT the no-arg attack test `g.b()` the port used. A
 *   hit on a player who is off the ground becomes the op-18 knock-down; on a grounded player —
 *   mid-combo included — it takes the op-4 arm (`c(r4)` → the S9 flinch).
 * - **op 34 @204-262**: the parried-hit feedback also spawns `a(8, 5, 14, av, ak, midY + 30, 300)`
 *   and plays `k.A(11)`.
 * - **op 20 / 28 @622**: `g.b = null; i(43)`.
 * - **op 38 @703-798**: `S == 3` returns first, then the `g.a()` damage gate (which drains), and
 *   only then the `S == 6 / 7` exits.
 * - **op 29 @348**: `av = !r4.av` (the victim turns to the attacker), not `av = r4.av`.
 * - ops 8/9/19/25/28/30 — never sent by the shipped code — are ported from the same bytes.
 */
class Slice410Test {
    private val AIR = intArrayOf(18, 19, 20, 22, 23, 24, 25, 35, 36, 43, 150, 157, 165, 233, 242, 243,
        263, 264, 265, 266)
    private val ATTACK = intArrayOf(67, 68, 69, 81, 112, 113, 114, 115, 183, 184, 216, 217, 286, 287)

    private fun player(w: Level0World, s: Int, gt: Int = 0, x1: Int = 30): Entity {
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        p.S = s; p.gt = gt; p.x1 = x1
        p.ag = 777; p.ah = 0; p.aj = 0; p.av = false
        w.iBh = 0; w.playerLinkB = null; w.sfxLog.clear(); w.pendingInsert.clear()
        return p
    }

    private fun soldier(x: Int = 340, av: Boolean = true): Entity =
        Entity(11, null).apply { ak = x; al = 100; this.av = av }

    private fun dmg(w: Level0World) = Entity.PLAYER_DMG[w.weaponSlot]

    // ---------------------------------------------------------------------- head @0-37

    @Test fun `head - every state of the air set turns op 4 into the knock-down and zeroes ag`() {
        for (s in AIR) {
            val w = world(); w.npcs.clear()
            val p = player(w, s)
            p.applyHit(4, 0, soldier(), w)
            assertEquals(43, p.S, "S$s → op 18 → i(43)")
            assertEquals(0, p.ag, "S$s: the upgrade zeroes the victim's vx")
            assertEquals(30 - dmg(w), p.x1, "S$s: op 18 pays g.a()'s drain")
            assertTrue(18 !in w.sfxLog, "the op-18 arm has no hurt sfx (that is the op-4 arm's k.A(18))")
        }
    }

    @Test fun `head - a grounded player flinches instead, in every attack state too`() {
        for (s in ATTACK) {
            val w = world(); w.npcs.clear()
            val p = player(w, s)
            p.applyHit(4, 0, soldier(), w)
            assertEquals(9, p.S, "S$s is not in g.b(int)'s set → the op-4 arm: c(r4) → S9")
            assertTrue(18 in w.sfxLog, "S$s: the op-4 arm ends with k.A(18)")
        }
    }

    @Test fun `head - the flinch pays the drain except in the drain-immune combo anims`() {
        val w = world(); w.npcs.clear()
        var p = player(w, 68)
        p.applyHit(4, 0, soldier(), w)
        assertEquals(30 - dmg(w), p.x1, "S68 pays")
        p = player(w, 67)
        p.applyHit(4, 0, soldier(), w)
        assertEquals(30, p.x1, "S67 is drain-immune (g.d @ S 67/183/184) — it still flinches")
        assertEquals(9, p.S)
    }

    @Test fun `head - the upgrade needs g_t == 0, a running hit lock leaves the op-4 arm`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 22, gt = 5)
        p.applyHit(4, 0, soldier(), w)
        assertEquals(22, p.S, "locked: neither the knock-down nor a counter (g.a() is false)")
        assertEquals(777, p.ag, "ag is only zeroed by the upgrade")
        assertEquals(30, p.x1)
        assertTrue(18 in w.sfxLog)
    }

    @Test fun `head - only op 4 is upgraded, op 20 and a bare op 32 are untouched`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 22)
        p.applyHit(32, 0, soldier(), w)
        assertEquals(22, p.S)
        assertEquals(777, p.ag)
    }

    // ---------------------------------------------------------------------- op 34 @204-262

    @Test fun `op 34 - zeroes the velocities, sparks clip5 anim14 at midY plus 30 and plays sfx 11`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 6); p.ah = 9; p.aj = 8; p.av = true
        val hits0 = p.hitsTaken
        p.applyHit(34, 0, Entity(73, null).apply { ak = 340; al = 100 }, w)
        assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
        assertEquals(hits0 + 1, p.hitsTaken)
        assertTrue(11 in w.sfxLog, "k.A(11)")
        val fx = w.pendingInsert.single()
        assertEquals(8, fx.ax, "a(8, …) — the generic fx entity")
        assertEquals(14, fx.S, "clip 5 anim 14")
        assertEquals(p.ak, fx.ak)
        assertEquals(((p.W[1] + p.W[3]) shr 1) + 30, fx.al)
        assertEquals(300, fx.az)
        assertEquals(true, fx.av, "the spark faces like the victim")
    }

    // ---------------------------------------------------------------------- op 20 / 28 @622

    @Test fun `op 20 and op 28 drop the grab link and knock the player down`() {
        for (op in intArrayOf(20, 28)) {
            val w = world(); w.npcs.clear()
            val p = player(w, 0)
            val marker = soldier()
            w.playerLinkB = marker
            p.applyHit(op, 0, marker, w)
            assertNull(w.playerLinkB, "op $op: g.b = null")
            assertEquals(43, p.S)
            assertEquals(30, p.x1, "no drain: @622 never calls g.a()")
        }
    }

    // ---------------------------------------------------------------------- op 38 @703-798

    @Test fun `op 38 - the damage gate runs and drains BEFORE the S6 and S7 exits`() {
        for (s in intArrayOf(6, 7)) {
            val w = world(); w.npcs.clear()
            val p = player(w, s)
            p.applyHit(38, 0, soldier(), w)
            assertEquals(30 - dmg(w), p.x1, "S$s: g.a() already paid")
            assertEquals(s, p.S, "…and then exits without engaging")
            assertNull(w.playerLinkB)
        }
    }

    @Test fun `op 38 - S3 returns before the gate`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 3)
        p.applyHit(38, 0, soldier(), w)
        assertEquals(30, p.x1)
    }

    @Test fun `op 38 - an engaged player is linked, held in S3 and shoved away from the marker`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 0)
        val m = soldier(x = 340)
        p.applyHit(38, 0, m, w)
        assertSame(m, w.playerLinkB, "g.b = r4")
        assertEquals(3, p.aB)
        assertEquals(3, p.S, "o() → i(3)")
        assertEquals(false, p.av, "av = r4.ak < ak: the marker is east → false")
        assertEquals(-512, p.ag)
    }

    // ---------------------------------------------------------------------- op 29 / 30

    @Test fun `op 29 - the victim turns to the attacker (av = not r4_av)`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 0)
        p.applyHit(29, 0, soldier(av = false), w)
        assertEquals(true, p.av)
        assertEquals(10, p.S)
        assertEquals(1536, p.ag)
        p.applyHit(29, 0, soldier(av = true), w)
        assertEquals(false, p.av)
        assertEquals(-1536, p.ag)
    }

    @Test fun `op 30 - plays arg and keeps the push direction of ag`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 0); p.ag = 5
        p.applyHit(30, 10, soldier(), w)
        assertEquals(10, p.S); assertEquals(1792, p.ag)
        p.ag = -5
        p.applyHit(30, 10, soldier(), w)
        assertEquals(-1792, p.ag)
        p.ag = 0
        p.applyHit(30, 10, soldier(), w)
        assertEquals(-1792, p.ag, "@328 `ifle` — zero takes the negative arm")
    }

    // ---------------------------------------------------------------------- ops 8 / 9 / 19 / 25

    @Test fun `op 8 shares the op 24 arm - pinned at the source's top-left corner`() {
        for (op in intArrayOf(8, 24)) {
            val w = world(); w.npcs.clear()
            val p = player(w, 0); p.ah = 4; p.aj = 5
            val src = soldier()
            src.W[0] = 123; src.W[1] = 77; src.W[2] = 150; src.W[3] = 120
            p.applyHit(op, 10, src, w)
            assertEquals(10, p.S, "i(arg)")
            assertEquals(123, p.ak); assertEquals(77, p.al)
            assertEquals(0, p.ag); assertEquals(0, p.ah); assertEquals(0, p.aj)
        }
    }

    @Test fun `op 9 and op 25 - gated on S equal to arg, slide to the corner, then S43 launch`() {
        for (op in intArrayOf(9, 25)) {
            val w = world(); w.npcs.clear()
            val p = player(w, 22)
            val src = soldier()
            src.W[0] = p.ak + 8; src.W[1] = p.al - 12
            val x0 = p.ak; val y0 = p.al
            p.applyHit(op, 23, src, w)                 // S (22) != arg (23): @492 return
            assertEquals(22, p.S); assertEquals(x0, p.ak)
            p.applyHit(op, 22, src, w)
            assertEquals(43, p.S, "a(43, 32)")
            assertEquals(src.W[0], p.ak, "ak = r4.W[0] (al is then re-seated by a(43, 32)'s settle mask)")
            assertEquals(1536, p.aj)
            assertEquals(8 shl 8, p.ag, "ag = (W[0] - ak) << 8")
            assertEquals((-12) shl 8, p.ah, "ah = (W[1] - al) << 8")
            assertTrue(y0 != p.al, "al moved off its start")
        }
    }

    @Test fun `op 19 clears ah and aj and sets az to 99`() {
        val w = world(); w.npcs.clear()
        val p = player(w, 0); p.ah = 3; p.aj = 4; p.az = 5
        p.applyHit(19, 0, soldier(), w)
        assertEquals(0, p.ah); assertEquals(0, p.aj); assertEquals(99, p.az)
    }
}
