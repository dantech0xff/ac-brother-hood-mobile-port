package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 397 — the ax10 trigger FSM `aV()` (i.javap `aV()` @0-7879, the 7.9 KB
 * switch over S 0-55) re-read arm by arm from the raw bytecode, plus the
 * helpers it calls (`ae()`, `aT()`, `at()`, `aS()`, `k(i)`, `bh()`, `bi()`,
 * `a(III)`, `G()`, `i.c/d/V` hand helpers, `k.t/j/k`, `g.b(I)`, `aU()`'s S31
 * draw arm). Everything matched except:
 *
 * - **S16 door teleport** (12 shipped zones in missions 0/3/6):
 *   - the 105 prompt is spawned by the ZONE — `aload_0; bipush 105; …
 *     invokevirtual a:(III)V` @6127-6149 — the port spawned it on the PLAYER's
 *     `ae`, so the door's own `G()`/leave arm never cleared it and a stale
 *     prompt stayed at the door;
 *   - the mid-fade arm is `aS.a((i) null)` @6091-6092 (`g.a:(Li;)V` = the
 *     entity UNBIND, `bindAc`), the port called the int overload `a(0)` — the
 *     S43 fling — so the player never stood up out of the S285 emerge anim;
 *   - the exit-tap gate is `!g.b(aS.S)` = the aerial/action set
 *     `{18-20,22-25,35,36,43,150,157,165,233,242,243,263-266}`, the port used
 *     the attack list.
 * - **S30 pursuer-pool spawner, Z[6] == 3** (the wave flavour): `Z[5] = (col >=
 *   1) ? 1 : 0` @3222-3243 / @4001-4022 — column 0 spawns on the left, the
 *   rest on the right; the port had both inverted. (No shipped record uses
 *   Z[6] == 3: the two S30 zones of mission 3 are flavour 0.)
 */
class Slice397Test {
    private fun doorPair(w: Level0World): Pair<Entity, Entity> {
        val dest = Entity(10, null).apply {
            aw = 77; S = 16
            ak = 920; al = 730
            W[0] = 900; W[1] = 700; W[2] = 940; W[3] = 760
            aD = 1
        }
        val door = Entity(10, null).apply {
            S = 16
            ak = w.player.ak; al = w.player.al
            W[0] = w.player.ak - 30; W[1] = w.player.al - 60
            W[2] = w.player.ak + 30; W[3] = w.player.al + 4
            oId = 77; Z[0] = 77
        }
        w.npcs.add(door); w.npcs.add(dest)
        return door to dest
    }

    private fun doorWorld(): Triple<Level0World, Entity, Pair<Entity, Entity>> {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.g = null; p.ga = null
        return Triple(w, p, doorPair(w))
    }

    // ------------------------------------------------------------ S16
    @Test fun `S16 - the door owns its 105 prompt and leaving the zone releases it`() {
        val (w, p, pair) = doorWorld()
        val door = pair.first
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertNotNull(door.ae, "this.a(105,…) @6127: the zone's marker link")
        assertEquals(105, door.ae!!.S)
        assertNull(p.ae, "the player's own marker slot stays free")
        p.ak = door.W[2] + 200; p.refreshBoxes()                 // walk out of the zone
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertNull(door.ae, "@6224 G() on the leave arm drops exactly this marker")
    }

    @Test fun `S16 - bound mid-fade-out the player is unbound, not flung`() {
        val (w, p, pair) = doorWorld()
        val (door, dest) = pair
        p.bindAc(door)
        p.setAnim(285)                                           // the door-emerge anim
        w.kAo = true; w.kBI = 20
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertEquals(19, dest.S, "r1.i(19)")
        assertNull(p.ac, "aS.a((i) null) @6091-6092 unbinds")
        assertEquals(0, door.P and 256, "…and clears the bind mark")
        assertEquals(285, p.S, "no S43 fling out of the emerge anim")
    }

    @Test fun `S16 - the exit tap is blocked by the aerial-action set, not by the attack list`() {
        // an attack-combo anim on solid ground: the original lets the tap through
        run {
            val (w, p, pair) = doorWorld()
            p.gI = 1; p.setAnim(67); p.aZ = true
            w.pad.commit(16388)
            w.npcFsm.tickTrigger(pair.first, w, p, w.pad)
            assertSame(pair.second, p.ac, "S67 ∉ g.b(I): the exit runs")
            assertEquals(284, p.S)
        }
        // an aerial/action anim (even with aZ set): blocked
        for (s in intArrayOf(43, 243, 157, 22)) {
            val (w, p, pair) = doorWorld()
            p.setAnim(s); p.aZ = true
            w.pad.commit(16388)
            w.npcFsm.tickTrigger(pair.first, w, p, w.pad)
            assertNull(p.ac, "S$s ∈ g.b(I): no exit")
            assertEquals(s, p.S)
        }
    }

    // ------------------------------------------------------------ S30
    private fun waveZone(w: Level0World): Entity {
        val p = w.player
        val z = Entity(10, null)
        z.S = 30
        z.W[0] = p.ak - 60; z.W[1] = p.al - 60; z.W[2] = p.ak + 60; z.W[3] = p.al + 60
        z.Z[6] = 3                                               // the pursuer-wave flavour
        z.Z[3] = 100; z.Z[4] = 200; z.Z[5] = 0; z.Z[7] = 10
        w.npcs.add(z)
        return z
    }

    @Test fun `S30 wave spawner - column 0 spawns left (Z5 = 0), the other columns right`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player; p.refreshBoxes()
        val z = waveZone(w)
        w.npcFsm.tickTrigger(z, w, p, w.pad)
        val row = z.cr!![0]
        assertEquals(3, row.size)
        assertFalse(row[0].av, "col 0: Z[5] = 0 → av = false")
        assertEquals(w.kO - 20, row[0].ak, "col 0 spawns off the left edge")
        assertTrue(row[1].av && row[2].av, "cols >= 1: Z[5] = 1 → av = true")
        assertEquals(w.kO + 400 + 20 * 2, row[1].ak, "col 1 spawns off the right edge")
        assertEquals(w.kO + 400 + 20 * 3, row[2].ak)
        assertEquals(1, row[0].Z[0], "col 0 is the engaged pursuer")
        assertEquals(0, row[1].Z[0])
        assertEquals(1, z.Z[5], "the zone's Z[5] ends at the last column's value")
    }

    @Test fun `S30 wave respawn - the replacement follows the same side rule`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player; p.refreshBoxes()
        val z = waveZone(w)
        w.npcFsm.tickTrigger(z, w, p, w.pad)                     // allocate the grid
        z.cr!![0][1].aB = 0                                      // column 1 dies
        z.cr!![0][1].setAnim(139)
        z.aC = 0
        w.pendingInsert.clear()
        w.npcFsm.tickTrigger(z, w, p, w.pad)
        val m = z.cr!![0][1]
        assertTrue(m.aB > 0, "fixture: replaced")
        assertTrue(m.av, "col 1 respawns on the right: Z[5] = 1")
        assertEquals(w.kO + 400 + 20 * 2, m.ak)
        assertEquals(0, m.Z[0], "column 0 is still engaged → the replacement is not")
    }
}
