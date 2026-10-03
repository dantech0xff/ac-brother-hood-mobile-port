package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 356 — `I()`'s family head for ax11/17/23/47/50/73 (structured/
 * i.java:3982-4004; bytecode i.javap.txt I() offsets 1377-1578) and
 * `case 23 → L849`.
 *
 * The port ran each family member's arm without the shared head: no
 * `if (!P()) t()`, no corpse-landing `k.A(24)` / pinned-player release,
 * and `g.h` was never assigned — so the player's S203 ledge kill (11
 * ax10-S43 zones in m0/m2/m5/m7) always fell through to `G()`. ax23
 * also ran the ax11 arms although `case 23` goes straight to L849, and
 * ax73 skipped the L849 `au()` drop.
 */
class Slice356Test {
    private fun soldier(w: Level0World, x: Int, y: Int): Entity {
        val e = Entity(11, w.clips[7])
        e.setPositionPx(x, y); e.aB = 300; e.setAnim(2); e.refreshBoxes()
        w.npcs.add(e)
        return e
    }

    private fun stage(): Pair<Level0World, Entity> {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(400, 300); p.refreshBoxes()
        w.kM(2)
        return w to soldier(w, 400, 260)
    }

    @Test fun `g h is the member right above an S38 player and clears when it leaves`() {
        val (w, e) = stage()
        val p = w.player
        p.S = 38; p.ak = e.ak + 5; p.al = e.al + 30
        w.npcFsm.familyHead(e, p)
        assertSame(e, w.grabHolder)
        assertSame(e, p.gh, "one g.h: the S203 arm reads player.gh")
        p.al = e.al + 60                                   // out of the 40px band
        w.npcFsm.familyHead(e, p)
        assertNull(w.grabHolder)
    }

    @Test fun `g h does not switch while another member holds it`() {
        val (w, e) = stage()
        val p = w.player
        val other = soldier(w, 400, 262)
        p.S = 38; p.ak = e.ak; p.al = e.al + 20
        w.npcFsm.familyHead(e, p)
        w.npcFsm.familyHead(other, p)
        assertSame(e, w.grabHolder, "f() && g.h == null only")
    }

    @Test fun `S203 picks the member whose W meets the player X`() {
        val (w, e) = stage()
        val p = w.player
        p.S = 203
        e.W.copyInto(p.X)                                  // X on the member
        w.npcFsm.familyHead(e, p)
        assertSame(e, w.grabHolder)
    }

    @Test fun `a corpse landing on screen plays sfx 24 and releases a pinned player`() {
        val (w, e) = stage()
        val p = w.player
        e.S = 20; e.Q = 24; e.T = 1; e.U = 0; e.aB = 0; e.bl = 9
        p.S = 90
        w.sfxLog.clear()
        w.npcFsm.familyHead(e, p)
        assertTrue(24 in w.sfxLog, "k.A(24)")
        assertEquals(43, p.S, "aS.a(0) → airborne fling")
        assertEquals(1536, p.aj)
        assertEquals(0, e.bl)
    }

    @Test fun `S20 keeps a player in S157 and other Q only sound`() {
        val (w, e) = stage()
        val p = w.player
        e.S = 20; e.Q = 175; e.T = 1; e.U = 0; e.aB = 0
        p.S = 157
        w.npcFsm.familyHead(e, p)
        assertEquals(157, p.S, "S20 skips the release while the player is in S157")
        e.Q = 5; p.S = 0; w.sfxLog.clear()
        w.npcFsm.familyHead(e, p)
        assertTrue(24 in w.sfxLog)
        assertNotEquals(43, p.S, "not a carry: no release")
    }

    @Test fun `a dead member releases its ae marker instead of refreshing boxes`() {
        val (w, e) = stage()
        e.ae = Entity(14, null)
        e.aB = 0
        val before = e.W.toList()
        e.ak += 50                                          // a refresh would move W
        w.npcFsm.familyHead(e, w.player)
        assertNull(e.ae, "P() → G()")
        assertEquals(before, e.W.toList(), "no t()")
    }

    @Test fun `ax73 drops its linked pickup through the L849 au()`() {
        val w = world()
        settleIntro(w)                                      // no claim suspension
        val drop = w.npcs.first { it.ax == 14 }
        drop.P = drop.P or 32
        val e = Entity(73, w.clips[7])
        e.aw = 7373
        e.setPositionPx(w.player.ak + 30, w.player.al); e.refreshBoxes()
        e.P = e.P or 16                                     // tick at any au
        e.aB = 0; e.Z[21] = drop.aw
        w.npcs += e
        w.tick(emptyList())
        assertEquals(-1, e.Z[21], "au() ran")
        assertEquals(0, drop.P and 32, "the drop activates")
        assertEquals(e.ak, drop.ak)
    }
}
