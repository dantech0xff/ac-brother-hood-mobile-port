package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 392 — `i.ba()` (the ax24 projectile / knife / heal-shrine FSM,
 * i.javap `ba()` @0-1648) re-read from the bytecode. The port's arms came
 * from the simple decompile, which prints block layout and inverts
 * conditions:
 *
 * - **S13/S14** (the lobbed knife): `j--`; `bZ > ap-30` → S45; `k` → S45;
 *   else the `X`-vs-player hit test. The port had `bZ <= ap-30 → S45`, so
 *   every knife left S13 on its first tick (and read `k` from a second,
 *   never-written `projK`). S14 enters at @979 — it does not re-test the
 *   anim end.
 * - **S45**: `bZ > ap` → S15; `k && j <= 0` → `k = false; S15`; else hit
 *   test (the port fired S15 on `bZ <= ap`).
 * - **S16-18** (+ S41-43): contact → `k.c(this)` for S16-18 **and then**
 *   `aS.a(38,…)` — the port removed the shot without damaging the player.
 * - **S20** (heal shrine): ends `i(21)` (the spent, inert state @1534) —
 *   the port left it armed, refilling every tick the player overlapped.
 * - **S31/S40** (pinned child): after following `af`, anything but an
 *   ax24-S19 parent falls through @614 (`ag = ah = 0`, `r() → k.c(this)`)
 *   — the port never expired a popup while `af` was set.
 * - **S19** latches `G` (the port's `projB`), **S35/S36** set `b`.
 * - **Head**: the `!v()` retire is not an else-branch — the code falls
 *   through into the `aG` dispatch, so an off-screen knife that still
 *   overlaps the player lands its hit; `aG != 3` clears `c`.
 */
class Slice392Test {
    private fun ax24(w: Level0World, s: Int, x: Int, y: Int): Entity {
        val e = Entity(24, w.clips[40])
        e.setPositionPx(x, y); e.S = s; e.refreshBoxes()
        w.npcs.add(e); return e
    }

    private fun finish(e: Entity) {
        e.T = e.clip!!.frameCount(e.S) - 1
        e.U = (e.clip!!.frameDuration(e.S, e.T) - 1).coerceAtLeast(0)
    }

    private fun inView(w: Level0World, x: Int, y: Int) {
        w.player.setPositionPx(x, y); w.player.refreshBoxes()
        w.kM(2)                                    // m(ad) snap → in view
    }

    // ------------------------------------------------------------ S13 / S14
    @Test fun `S13 knife keeps flying while it is short of the target band`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 13, 100, 100)
        e.bZ = 0; e.ap = 200; e.j = 10; e.k = false
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(13, e.S, "bZ(0) <= ap-30 → no S45 yet")
        assertEquals(9, e.j, "j--")
    }

    @Test fun `S13 knife reaching the band switches to S45 (bZ gt ap-30)`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 13, 100, 100)
        e.bZ = 171; e.ap = 200; e.j = 10
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(45, e.S)
        val e2 = ax24(w, 14, 100, 100)
        e2.bZ = 170; e2.ap = 200; e2.j = 10                // 170 > 170 is false
        w.npcFsm.tickAx24(e2, w, w.player)
        assertEquals(14, e2.S, "the compare is strict: bZ == ap-30 keeps S14")
    }

    @Test fun `S13 or S14 with the k flag go to S45 even short of the band`() {
        val w = world(); inView(w, 400, 400)
        for (s in intArrayOf(13, 14)) {
            val e = ax24(w, s, 100, 100)
            e.bZ = 0; e.ap = 500; e.j = 10; e.k = true
            w.npcFsm.tickAx24(e, w, w.player)
            assertEquals(45, e.S, "S$s with k")
        }
    }

    @Test fun `S13 steps to S14 when its anim ends and S14 does not test the anim end`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 13, 100, 100)
        e.bZ = 0; e.ap = 500; e.j = 10
        finish(e)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(14, e.S, "r() → i(14)")
        val e2 = ax24(w, 14, 100, 100)
        e2.bZ = 0; e2.ap = 500; e2.j = 10
        finish(e2)
        w.npcFsm.tickAx24(e2, w, w.player)
        assertEquals(14, e2.S, "@979 enters below the r() test")
    }

    @Test fun `S13 knife hitting the player goes S9 and pays op38`() {
        val w = world(); inView(w, 100, 100)
        val e = ax24(w, 13, 100, 100)
        e.bZ = 0; e.ap = 500; e.j = 10
        val pw = w.player.W
        e.X[0] = pw[0]; e.X[1] = pw[1]; e.X[2] = pw[2]; e.X[3] = pw[3]
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(9, e.S)
        assertEquals(3, w.player.aB, "op38 → aB=3")
        assertSame(e, w.playerLinkB)
    }

    // ------------------------------------------------------------ S45
    @Test fun `S45 arrival (bZ gt ap) goes to S15`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 45, 100, 100)
        e.ap = 10; e.bZ = 50; e.j = 99
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(15, e.S)
    }

    @Test fun `S45 short of ap holds until the k flag runs out of j`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 45, 100, 100)
        e.ap = 500; e.bZ = 0; e.j = 5; e.k = true
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(45, e.S); assertTrue(e.k)
        e.j = 0
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(15, e.S); assertFalse(e.k)
        val e2 = ax24(w, 45, 100, 100)
        e2.ap = 500; e2.bZ = 0; e2.j = -5; e2.k = false
        w.npcFsm.tickAx24(e2, w, w.player)
        assertEquals(45, e2.S, "!k never leaves S45 on j alone")
    }

    // ------------------------------------------------------------ S16-18 / S41-43
    @Test fun `S16-18 line shots expire on contact and still hit the player`() {
        for (s in 16..18) {
            val w = world(); inView(w, 100, 100)
            val e = ax24(w, s, 100, 100)
            val pw = w.player.W
            e.X[0] = pw[0]; e.X[1] = pw[1]; e.X[2] = pw[2]; e.X[3] = pw[3]
            w.npcFsm.tickAx24(e, w, w.player)
            assertTrue(w.pendingRemove.contains(e), "S$s: k.c(this)")
            assertEquals(3, w.player.aB, "S$s: aS.a(38,…) after the removal")
            assertSame(e, w.playerLinkB)
        }
    }

    @Test fun `S41-43 shots hit without expiring`() {
        for (s in 41..43) {
            val w = world(); inView(w, 100, 100)
            val e = ax24(w, s, 100, 100)
            val pw = w.player.W
            e.X[0] = pw[0]; e.X[1] = pw[1]; e.X[2] = pw[2]; e.X[3] = pw[3]
            w.npcFsm.tickAx24(e, w, w.player)
            assertFalse(w.pendingRemove.contains(e), "S$s keeps flying")
            assertEquals(3, w.player.aB)
        }
    }

    // ------------------------------------------------------------ S20
    @Test fun `S20 heal shrine is spent after one use`() {
        val w = world()
        w.player.setPositionPx(80, 80); w.player.refreshBoxes()
        w.kAE = 10
        val e = ax24(w, 20, 80, 80); e.aB = 200
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(21, e.S, "@1522 i(21)")
        assertEquals(21, w.player.S)
        w.kAF = 7; w.player.setAnim(0)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(7, w.kAF, "S21 is inert (@1534)")
        assertEquals(0, w.player.S)
    }

    // ------------------------------------------------------------ S31 / S40
    @Test fun `S31 and S40 popups follow an ordinary parent and still expire`() {
        for (s in intArrayOf(31, 40)) {
            val w = world()
            val parent = Entity(11, w.clips[7]); parent.setPositionPx(500, 600)
            val e = ax24(w, s, 0, 0)
            e.af = parent; e.ao = 10; e.ap = 20; e.ag = 300; e.ah = -300
            finish(e)
            w.npcFsm.tickAx24(e, w, w.player)
            assertEquals(510, e.ak); assertEquals(620, e.al)
            assertEquals(0, e.ag); assertEquals(0, e.ah, "@614 zeroes the velocity")
            assertTrue(w.pendingRemove.contains(e), "S$s: r() → k.c(this) even with af set")
        }
    }

    @Test fun `S31 popup on an ax24 S19 parent follows and never expires`() {
        val w = world()
        val parent = ax24(w, 19, 500, 600)
        parent.runnerG = true
        val e = ax24(w, 31, 0, 0)
        e.af = parent; e.ao = 3; e.ap = 4; e.ag = 9; e.ah = 9
        finish(e)
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(503, e.ak); assertEquals(604, e.al)
        assertEquals(0, e.ag); assertEquals(0, e.ah)
        assertFalse(w.pendingRemove.contains(e), "@603-613 returns before the r() test")
    }

    // ------------------------------------------------------------ S19 / S35 / S36
    @Test fun `S19 lays one child and latches G`() {
        val w = world()
        val e = ax24(w, 19, 100, 100)
        assertFalse(e.runnerG)
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(e.runnerG)
        val n = w.pendingInsert.count { it.ax == 24 }
        w.npcFsm.tickAx24(e, w, w.player)
        assertEquals(n, w.pendingInsert.count { it.ax == 24 }, "G != 0 → no second child")
    }

    @Test fun `S35 and S36 pin b`() {
        val w = world(); inView(w, 400, 400)
        val e = ax24(w, 35, 100, 100); e.b = false
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(e.b, "@1535 b = 1")
        val e2 = ax24(w, 36, 100, 100); e2.b = false
        w.npcFsm.tickAx24(e2, w, w.player)
        assertTrue(e2.b, "@1606 b = 1")
    }

    // ------------------------------------------------------------ head
    @Test fun `an off-screen knife retires and still lands its hit on an overlapping player`() {
        val w = world()
        w.player.setPositionPx(-5000, -5000); w.player.refreshBoxes()   // off camera
        val e = ax24(w, 2, -5000, -5000)
        e.af = Entity(56, w.clips[19])
        assertFalse(e.inPlayV(w), "fixture: v() false")
        w.npcFsm.tickAx24(e, w, w.player)
        assertTrue(e.P and 128 != 0, "retired")
        assertEquals(-1, e.aG); assertNull(e.af)
        assertEquals(3, w.player.aB, "the dispatch after the retire (@306) still tests W vs aS.W")
        assertTrue(w.pendingRemove.contains(e), "S0-4 → k.c(this)")
    }

    @Test fun `aG not 3 clears the chain target`() {
        val w = world(); inView(w, 400, 400)
        // beside the player (no overlap: a hit would queue the removal, and
        // `p()` on removal nulls `c` by itself)
        val e = ax24(w, 0, 550, 400)
        assertTrue(e.inPlayV(w), "fixture: in play")
        e.aG = 0; e.c = Entity(56, w.clips[19])
        w.npcFsm.tickAx24(e, w, w.player)
        assertNull(e.c, "@107-117")
        assertFalse(w.pendingRemove.contains(e))
        val e3 = ax24(w, 0, 550, 400)
        e3.aG = 3; e3.c = Entity(56, w.clips[19]).also { it.setPositionPx(5000, 5000) }
        w.npcFsm.tickAx24(e3, w, w.player)
        assertNotNull(e3.c, "aG == 3 keeps it")
    }

    // ------------------------------------------------------------ bG ↔ ba()
    @Test fun `a thrown S18 knife flies on through S13 and S14 until it reaches its band`() {
        val w = world(); w.npcs.clear()
        inView(w, 400, 400)
        val thrower = Entity(32, w.clips[7]); thrower.pv = 2; thrower.aG = 0
        thrower.setPositionPx(300, 100); thrower.refreshBoxes()
        thrower.setAnim(18); thrower.j = 0; thrower.clip = null
        thrower.respawnAttack(w)
        val k = w.pendingInsert.last()
        assertEquals(13, k.S, "a(24,40,13,az-1)")
        assertTrue(k.ap > k.bZ + 59, "ap = bZ + j.a(60, r2)")
        var left13 = -1
        for (t in 0 until 200) {
            val before = k.S
            w.npcFsm.tickAx24(k, w, w.player)
            if (k.S != before && left13 < 0) left13 = t
            if (k.S == 45 || k.S == 9) break
            k.bZ += 5                                        // the arc climbs the band
        }
        assertTrue(left13 > 3, "the knife spends many ticks in S13/S14 (left at $left13)")
    }
}
