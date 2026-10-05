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

// ===================================================================
class Slice60Test {
    private fun ax60At(w: Level0World, x: Int, y: Int, vararg f: Int): Entity {
        val e = Entity(60, w.clips[21])
        val rec = mutableListOf(60, 1, x, y)
        rec += f.toList()
        while (rec.size < 22) rec += 0
        e.setPositionPx(x, y)
        w.npcFsm.initAx60(e, rec.toList(), w)
        w.npcs.add(e)
        keepLive(e)
        return e
    }

    @Test fun `init S9 lift — P4096, Z3=al, Z5 probed, auto-bounce`() {
        val w = world()
        val e = ax60At(w, 100, 200, 5, 9, 0, -1, 40, 1)
        assertTrue(e.P and 4096 != 0, "P|=4096 for S9")
        assertEquals(200, e.Z[3], "Z[3]=al for S9")
        // `Z[4]==2` joins S6/11/13 in the L5878 hide test (slice 389):
        assertEquals(0, e.az, "auto-bounce → az=0")
        assertTrue(e.P and 16 != 0, "auto-bounce → P|=16")
        assertEquals(2, e.Z[4], "r8[9]==1 → auto-bounce")
        assertTrue(e.Z[5] <= 200, "Z[5] bound probed ≤ spawn al")
        assertEquals(9, e.S, "init tail i(r8[5])")
    }

    @Test fun `init S13 pair member — az=0, P16, aC=Z2`() {
        val w = world()
        val e = ax60At(w, 100, 200, 4, 13, 0, 58, 0, 0)
        assertEquals(0, e.az, "az=0 for S13")
        assertTrue(e.P and 16 != 0, "P|=16")
        assertEquals(13, e.S)
    }

    @Test fun `S9 arm resolves ax58 link → Z4=3 lever mode`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val lever = Entity(58, w.clips[20]); lever.aw = 88888
        w.npcs.add(lever)
        val e = ax60At(w, 100, 200, 5, 9, 0, 88888, 40, 0)
        w.tick(emptyList())
        assertSame(lever, e.s, "s = ax58 link")
        assertEquals(3, e.Z[4], "Z[4]=3")
    }

    @Test fun `S9 arm missing link → i(10) travel + Z0=-1`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val e = ax60At(w, 100, 200, 5, 9, 0, 999999, 40, 0)
        w.tick(emptyList())
        assertEquals(10, e.S, "i(10)")
        assertEquals(-1, e.Z[0])
        assertEquals(39, e.aC, "aC=Z[2] set by arm, then L149 decrements same tick")
    }

    @Test fun `ride — standing player on a moving lift enters S78 first, S50 the next tick`() {
        val w = world()
        val e = ax60At(w, 100, 200, 5, 10, 0, -1, 40, 0)   // S10 vertical mover
        e.refreshBoxes()
        // player's box straddles the lift's bottom edge (W[1]≤W[3]≤W[3])
        w.player.setPositionPx((e.W[0] + e.W[2]) shr 1, e.W[3])
        w.player.refreshBoxes()
        w.player.aZ = true
        e.ah = 256                                         // moving down
        w.npcFsm.tickAx60(e, w, w.player)
        // @361-369 (raw bytes, slice 412): `aS.i(78); goto 529` — the arm ENDS at the S78 entry
        assertEquals(78, w.player.S, "first tick: crouch entry only")
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(50, w.player.S, "already in S78 + lift moving → ride crouch S50")
    }

    @Test fun `ride — a lift that moves only sideways also puts a crouched rider into S50`() {
        val w = world()
        val e = ax60At(w, 100, 200, 5, 10, 0, -1, 40, 0)
        e.refreshBoxes()
        w.player.setPositionPx((e.W[0] + e.W[2]) shr 1, e.W[3])
        w.player.refreshBoxes()
        w.player.aZ = true
        w.player.S = 78                                    // already crouched
        e.ah = 0; e.ag = 256                               // @372-394: `ah != 0 || ag != 0 → i(50)`
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(50, w.player.S)
    }

    // ---- c(boolean) — the horizontal mover helper (i.javap `c(Z)Z`, raw bytes, slice 412)

    @Test fun `c(Z) — a non-locomotion player below the platform's bottom edge is knocked down`() {
        val w = world()
        val e = ax60At(w, 300, 200, 5, 11, 0, -1, 0, 0)    // S11 horizontal mover, c(false)
        e.refreshBoxes()
        val p = w.player
        // S5 (the landing recovery) is neither in g.b(int)'s air set (no mount attempt) nor in
        // g.c(int)'s locomotion set (no side clip) — its box still reaches into the platform
        p.setAnim(5)
        p.setPositionPx(e.W[0], e.W[3] + 6); p.refreshBoxes()
        p.aZ = false; p.ga = null
        assertTrue(Entity.overlapStrict(p.W, e.W), "the player's box reaches into the platform")
        w.npcFsm.tickAx60(e, w, p)
        // @514: `!g.c(S)` and `al > W[3]` (S not 209/50) → `aS.a(0)`
        assertEquals(43, p.S, "head-bonk: a(0) fall")
        assertNull(p.ga)
    }

    @Test fun `c(Z) — the S209 cling is carried with the platform even once the boxes separate`() {
        val w = world()
        val e = ax60At(w, 300, 200, 5, 11, 0, -1, 0, 0)
        e.refreshBoxes()
        val p = w.player
        p.setPositionPx(e.W[2] + 200, e.W[3])              // far away: no W overlap
        p.setAnim(209); p.refreshBoxes()
        p.ga = e
        e.ag = 5 shl 8
        val x0 = p.ak
        w.npcFsm.tickAx60(e, w, p)
        // @562: `g.a == this && S == 209` keeps the link; @585 carries by `ag >> 8`
        assertSame(e, p.ga)
        assertEquals(x0 + 5, p.ak)
    }

    @Test fun `c(Z) — a carry that ends outside the scroll holder is pushed back (aS_b is the corner probe)`() {
        val w = world()
        val e = ax60At(w, 300, 200, 5, 11, 0, -1, 0, 0)
        e.refreshBoxes()
        val p = w.player
        p.setPositionPx(e.W[2] + 200, e.W[3]); p.setAnim(209); p.refreshBoxes()
        p.ga = e; e.ag = 5 shl 8
        val x0 = p.ak
        w.npcFsm.tickAx60(e, w, p)
        assertEquals(x0 + 5, p.ak, "open air: the plain carry")
        // @610-636 `if (aS.b()) aS.ak -= (ag << 1) >> 8` — `b()` is i.b() (corner cells and the
        // scroll holder `k.ah`), NOT the static attack test g.b() the port called
        val holder = Entity(37, null).apply {
            W[0] = p.ak - 5; W[1] = 0; W[2] = p.ak + 500; W[3] = 1000
        }
        w.kAh = holder
        try {
            p.ga = e; p.setAnim(209); p.refreshBoxes()
            val x1 = p.ak
            e.ag = 5 shl 8
            w.npcFsm.tickAx60(e, w, p)
            assertEquals(x1 - 5, p.ak, "blocked: carry + (-2 x carry)")
        } finally { w.kAh = null }
    }

    @Test fun `c(Z) — the carry push-back does not read the static attack test g_b`() {
        // two identical riders on the same platform, one mid-sword-swing (S67 with a sword): the
        // port's `playerAttacking()` pushed the swinging one back, the bytes' `i.b()` does not care
        fun carried(s: Int): Int {
            val w = world()
            val e = ax60At(w, 300, 200, 5, 11, 0, -1, 0, 0)
            e.refreshBoxes()
            val p = w.player
            p.gI = 1
            p.setAnim(s)
            p.setPositionPx(e.ak, e.W[3]); p.refreshBoxes()
            p.ga = e; e.ag = 5 shl 8
            val x0 = p.ak
            w.npcFsm.tickAx60(e, w, p)
            return p.ak - x0
        }
        assertEquals(carried(5), carried(67), "S67 (attack set) is carried exactly like S5")
    }

    @Test fun `i_b — an active scroll holder blocks any box that is not strictly inside it`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        p.setPositionPx(300, 100); p.refreshBoxes()
        val open = p.cornerSupported(w)
        fun holder(dl: Int, dt: Int, dr: Int, db: Int) = Entity(37, null).apply {
            W[0] = p.W[0] + dl; W[1] = p.W[1] + dt; W[2] = p.W[2] + dr; W[3] = p.W[3] + db
        }
        try {
            w.kAh = holder(-50, -50, 50, 50)
            assertEquals(open, p.cornerSupported(w), "strictly inside: falls through to the corner cells")
            w.kAh = holder(0, -50, 50, 50);  assertTrue(p.cornerSupported(w), "W0 <= ah.W0")
            w.kAh = holder(-50, -50, 0, 50); assertTrue(p.cornerSupported(w), "W2 >= ah.W2")
            w.kAh = holder(-50, 0, 50, 50);  assertTrue(p.cornerSupported(w), "W1 <= ah.W1")
            w.kAh = holder(-50, -50, 50, 0); assertTrue(p.cornerSupported(w), "W3 >= ah.W3")
        } finally { w.kAh = null }
    }

    @Test fun `c(Z) — the lever or pair latch is taken on the link tick only`() {
        val w = world()
        val lever = Entity(58, w.clips[20]); lever.aw = 88801
        lever.S = 2
        w.npcs.add(lever)
        val e = ax60At(w, 300, 200, 5, 13, 0, 88801, 0, 0)  // S13 mover linked to an ax58
        w.npcFsm.tickAx60(e, w, w.player)
        assertSame(lever, e.ac)
        assertEquals(3, e.Z[4], "link tick: ax58 → lever mode")
        e.Z[4] = 0                                          // @0: `ac != null` skips the block
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(0, e.Z[4], "no re-latch on later ticks")
    }

    @Test fun `c(Z) — a stationary mover snaps home and starts after its cooldown (ag == 0 clamp)`() {
        val w = world()
        val e = ax60At(w, 300, 200, 5, 13, 0, -1, 0, 0)     // S13, no link, Z[2] = 0
        e.refreshBoxes()
        assertEquals(0, e.ag)
        w.npcFsm.tickAx60(e, w, w.player)
        // @1033 `ifne 1119` falls into @1040 for ag == 0: `ak = Z[3]`, `ag = ±Z[1] << 8`
        assertEquals(e.Z[3], e.ak)
        assertEquals(5 shl 8, Math.abs(e.ag), "the mover started (direction by the probe)")
    }

    @Test fun `pair handoff — runs when the S11 member is past its bound, not before`() {
        val w = world()
        val m = ax60At(w, 160, 200, 5, 11, 0, -1, 0, 0); m.aw = 88802   // the S11 pair member
        val e = ax60At(w, 100, 200, 5, 13, 0, 88802, 0, 0)               // S13 linked to it
        m.refreshBoxes(); e.refreshBoxes()
        m.ak = m.Z[3] + 10                                  // ac.ak > ac.Z[3]
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(1, e.Z[4], "pair latch on the link tick")
        // @1105 `if_icmple 1138` runs the block for `ac.ak > ac.Z[3]`
        assertEquals(m.Z[3] - 50, e.ak, "e snaps 50px behind the member's bound")
        assertEquals(m.Z[3], m.ak, "member re-seated at e.ak + 50")
    }

    @Test fun `auto-bounce Z4=2 — solid probe reverses ah`() {
        val w = world()
        val e = ax60At(w, 100, 200, -3, 10, 0, -1, 40, 1)  // Z[4]=2
        e.refreshBoxes()
        e.ah = -256                                        // moving up
        w.npcFsm.tickAx60(e, w, w.player)
        assertTrue(e.ah != 0)
    }

    @Test fun `lever Z4=3 bf() → travel anim + ah`() {
        val w = world()
        val lever = Entity(58, w.clips[20]); lever.aw = 88888
        lever.S = 10                                       // bf() set member
        w.npcs.add(lever)
        val e = ax60At(w, 100, 200, 5, 9, 0, 88888, 40, 0)
        w.tick(emptyList())                                // binds s, Z4=3
        lever.S = 10                                       // bf() set member
        w.npcFsm.tickAx60(e, w, w.player)                  // L94: bf → move
        assertEquals(10, e.S, "i(10) travel")
        assertTrue(e.runnerBz, "bz moving")
        assertTrue(e.ah > 0, "ah = +Z[1]<<8 downward")
    }

    @Test fun `lever Z4=3 idle lever + latch → unlatch reverse`() {
        val w = world()
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        val lever = Entity(58, w.clips[20]); lever.aw = 88888
        lever.S = 0                                        // not bf()
        w.npcs.add(lever)
        val e = ax60At(w, 100, 200, 5, 9, 0, 88888, 40, 0)
        w.tick(emptyList()); w.tick(emptyList())
        e.k = true                                         // latched
        w.tick(emptyList())
        assertFalse(e.k, "unlatched on idle lever")
        assertTrue(e.ah < 0, "reverse velocity")
    }

    @Test fun `mount — gB player inside S13 box → ga bind + carry`() {
        val w = world()
        // clip21 anim13 W=(0,-36,22,36): box above the anchor; W[0]=Z[5]
        // stretches the left edge to the bound. c(true) runs for S∈{13,15}.
        val e = ax60At(w, 100, 200, 4, 13, 0, -1, 0, 0)
        e.refreshBoxes()
        w.player.setPositionPx((e.W[0] + e.W[2]) shr 1, (e.W[1] + e.W[3]) shr 1)
        w.player.setAnim(19)                               // gB member
        w.player.refreshBoxes()
        assertTrue(Entity.overlapStrict(w.player.W, e.W), "fixture overlap")
        w.npcFsm.tickAx60(e, w, w.player)
        assertSame(e, w.player.ga, "g.a = platform")
        val before = w.player.ak
        e.ag = 1280
        w.npcFsm.tickAx60(e, w, w.player)
        assertTrue(w.player.ak > before, "ride-carry ak += ag>>8")
    }

    @Test fun `S13 pair handoff — X overlap swaps velocity`() {
        val w = world()
        // clip21 anim13 X=(17,-36,16,36) right-extended; anim11
        // X=(-11,-36,16,36) left-extended. Pair parked just left of e's
        // right box ��� the L92 swap fires.
        val pair = ax60At(w, 115, 200, 4, 11, 0, -1, 0, 0) // S11 member
        val e = ax60At(w, 100, 200, 4, 13, 0, -1, 0, 0)
        e.ag = 1024
        w.paint(pair, e)
        w.npcFsm.tickAx60(e, w, w.player)
        assertEquals(1024, pair.ag, "pair gets Z[1]<<8 push")
        assertEquals(-1024, e.ag, "self reverses")
    }

    @Test fun `zone scan — ax10 S39 overlap latches k for Z4=3`() {
        val w = world()
        val zone = Entity(10, w.clips[10]); zone.S = 39
        w.npcs.add(zone)
        val e = ax60At(w, 100, 200, 5, 10, 0, -1, 0, 0)
        e.Z[4] = 3
        e.refreshBoxes()
        // zone inside the lift's real W corridor
        zone.W[0] = e.W[0] + 1; zone.W[1] = e.W[1] + 1
        zone.W[2] = e.W[2] - 1; zone.W[3] = e.W[3] - 1
        w.paint(zone, e)
        w.npcFsm.tickAx60(e, w, w.player)
        assertTrue(e.k, "S10 + Z[4]==3 + zone → latch k")
    }
}
