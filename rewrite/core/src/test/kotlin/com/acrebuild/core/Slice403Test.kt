package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 403 — the helpers behind the ax11 `I()` arms re-read from the raw bytes
 * (i.javap `j()` @0-881, `C()` @0-514, `P()`, `G()`, `d()`, `l()`, `g()`, `b(i)`,
 * `Q()`, `y()`, `H()`, `aA()` @764-795).
 *
 * Verified equal: `d()`, `l()`, `g(i)`, `b(i)`, `Q()`, `y()`, `H()`, `P()`/`G()`,
 * `j()`'s ax11/ax73 arms (damage table, finisher launch, half-HP engage, weaken
 * tail), `C()`'s ax11/ax73/ax23/ax50 arms.
 *
 * Fixed:
 * - **`C()` weaken marker** @54-74: `aS.a(45, aS.ak, aS.al - 85)` — receiver AND
 *   coordinates are the PLAYER's (`getstatic aS` ×3); the port spawned the
 *   prompt at the soldier's coordinates;
 * - **ax17 civilian intake**: `aA()` @764-795 is `if (j()) return; if (S != 69 &&
 *   S != 129) a()` — the SAME shared `j()` as the soldiers'. The port's inline
 *   copy dropped the ±160/±20 proximity band, the `bf`/`aN` first-swing latch,
 *   and `C()`: a struck civilian never staggered (`i(68)` + `k.A(13)`), and a
 *   killed one waited a tick for the head's dead-check instead of `i(69)` at the
 *   blow. `j()` on a corpse (`P()`) returns false after `G()`;
 * - **`g.b()` weapon gate**: `j()` and `i()` ask `g.b()` = `(gI == 1 || gI == 2)
 *   && S in attacks`; the port only looked at the anim set;
 * - **`k.au` vs the entity's own `au`**: every `bu[...]` index in i.javap is
 *   `getstatic k.au` (the difficulty: NEW GAME / RESET set 1, the CONTROL menu
 *   cycles 0..2). The ax47/ax50 init HP (`@3064` / `@2542`) and the ax73 copy of
 *   `j()`/`C()` indexed with `e.au` — the screen-distance score `u()` rewrites,
 *   default 10 → table slot 0: sentinels always had 300 HP and the heavy guard's
 *   enrage line (`aB <= bu[k.au]`) moved with its distance to the camera. The ax73
 *   tail (`h()`/`i()`/`j()`/`C()`/`g()`) and the ax47/50 `j()` now call the shared
 *   helpers instead of private copies.
 */
class Slice403Test {
    // ------------------------------------------------------------ fixtures
    private fun flatGround(w: Level0World): Pair<Int, Int> {
        for (cy in 4 until 90) for (cx in 5 until 400) {
            if ((-3..8).all { w.collisionCell(cx + it, cy) == 20 &&
                    (1..4).all { r -> w.collisionCell(cx + it, cy - r) < 12 } })
                return (cx * 20 + 10) to (cy * 20 - 1)
        }
        error("no flat ground in the level-0 map")
    }

    private fun soldier(w: Level0World, x: Int, y: Int, s: Int): Entity {
        val e = w.npcs.first { it.ax == 11 }
        e.setPositionPx(x, y)
        e.Z[0] = 0; e.cq = false; e.aB = 300; e.aA = 1; e.s = null; e.ae = null
        e.setAnim(s); e.refreshBoxes()
        return e
    }

    /** 15-field ax17 record (level-records.json packs 8/9/11, proven). */
    private fun civilian(w: Level0World, x: Int, y: Int, anim: Int = 61): Entity {
        val e = Entity(17, w.clips[7])
        val rec = mutableListOf(17, 152, x, y, 0, anim, 0, 0, 0, -1, 0, 0, -1, 100, -1)
        e.setPositionPx(x, y)
        w.npcFsm.initAx17(e, rec)
        w.npcs.add(e)
        return e
    }

    /** The player at `(x, y)` mid-swing `s`, its strike box laid over `e.W`. */
    private fun strike(w: Level0World, e: Entity, s: Int, x: Int = e.ak, y: Int = e.al,
                       gI: Int = 1) {
        val p = w.player
        p.setPositionPx(x, y); p.refreshBoxes()
        p.S = s; p.gI = gI
        for (i in 0..3) p.X[i] = e.W[i]
    }

    private fun civilianWorld(): Level0World {
        val w = world()
        w.kO = 0; w.kP = 0
        return w
    }

    // ------------------------------------------------------------ C() marker
    @Test fun `C() weaken marker - spawned on the player at the player's coordinates`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 0)
        e.Z[0] = 1; e.aB = 100
        val p = w.player
        p.setPositionPx(gx - 90, gy - 10); p.refreshBoxes(); p.ae = null
        assertTrue(e.hitReact(w))
        assertEquals(2, e.Z[0], "Z[0] = 2 @40-46")
        assertEquals(144, e.S, "i(144) @47-51")
        assertNull(e.ae, "the soldier owns no marker")
        val m = assertNotNull(p.ae, "aS.a(45, …) @54-74: the receiver is the player")
        assertEquals(p.ak, m.ak, "x = aS.ak")
        assertEquals(p.al - 85, m.al, "y = aS.al - 85")
    }

    @Test fun `C() weaken marker - an occupied player ae keeps the old marker`() {
        val w = world(); val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, 0)
        e.Z[0] = 1; e.aB = 100
        val p = w.player
        p.setPositionPx(gx - 90, gy); p.refreshBoxes()
        p.ae = null; p.spawnMarker(w, 8, 0, 0)
        val held = p.ae
        e.hitReact(w)
        assertSame(held, p.ae, "i.a(III) @0-3: `if (ae != null) return`")
    }

    // ------------------------------------------------------------ ax17 via j()
    @Test fun `ax17 - a landed swing staggers the civilian, S68 + sfx 13 (C())`() {
        val w = civilianWorld()
        val e = civilian(w, 200, 150, 61)
        strike(w, e, 67)
        w.sfxLog.clear()
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(50, e.aB, "H[0] = 50")
        assertEquals(68, e.S, "C() @293-324: i(68)")
        assertTrue(13 in w.sfxLog, "k.A(13)")
    }

    @Test fun `ax17 - the killing blow collapses at once, S69 without the stagger sound`() {
        val w = civilianWorld()
        val e = civilian(w, 200, 150, 61)
        strike(w, e, 183)
        w.sfxLog.clear()
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(0, e.aB, "J[0] = 100")
        assertEquals(69, e.S, "C() dead arm @461-476: i(69), no wait for the next tick")
        assertFalse(13 in w.sfxLog, "the dead arm plays no k.A(13)")
    }

    @Test fun `ax17 - a blow during the stagger does not restart it (C() returns false)`() {
        val w = civilianWorld()
        val e = civilian(w, 200, 150, 68)
        e.aB = 150
        strike(w, e, 67)
        w.sfxLog.clear()
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(100, e.aB, "the damage still lands")
        assertEquals(68, e.S, "@302-312: S == 68 → return false")
        assertFalse(13 in w.sfxLog, "no second k.A(13)")
    }

    @Test fun `ax17 - the proximity band is +-160 across and +-20 vertically (j() @78-136)`() {
        val w = civilianWorld()
        val e = civilian(w, 400, 150, 61)
        // a strike box over the civilian from a player too far away: no blow
        strike(w, e, 67, x = e.ak + 161)
        w.npcFsm.tickAx17(e, w, w.player)
        assertEquals(100, e.aB, "|dx| = 161 > 160")
        val e2 = civilian(w, 400, 150, 61)
        strike(w, e2, 67, y = e2.al + 21)
        w.npcFsm.tickAx17(e2, w, w.player)
        assertEquals(100, e2.aB, "|dy| = 21 > 20")
        // the band is inclusive
        val e3 = civilian(w, 400, 150, 61)
        strike(w, e3, 67, x = e3.ak + 160, y = e3.al + 20)
        w.npcFsm.tickAx17(e3, w, w.player)
        assertEquals(50, e3.aB, "|dx| = 160, |dy| = 20 are inside")
    }

    @Test fun `ax17 - the first S67 swing claims the lock target and latches bf`() {
        val w = civilianWorld()
        val e = civilian(w, 200, 150, 61)
        w.lockTarget = null; w.iBf = false
        strike(w, e, 67)
        w.npcFsm.tickAx17(e, w, w.player)
        assertSame(e, w.lockTarget, "@137-177: aN = this when free")
        assertTrue(w.iBf, "bf = true")
        // a later swing at another civilian cannot steal the lock
        val e2 = civilian(w, 200, 150, 61)
        w.iBf = false
        strike(w, e2, 67)
        w.npcFsm.tickAx17(e2, w, w.player)
        assertSame(e, w.lockTarget, "aN != null && aN != this → untouched")
    }

    @Test fun `ax17 - a corpse is skipped by j() and releases its marker (P() then G())`() {
        val w = civilianWorld()
        val e = civilian(w, 200, 150, 69)
        e.aB = 0
        e.ae = w.spawnPickup(8, 200, 100)
        strike(w, e, 67)
        w.npcFsm.tickAx17(e, w, w.player)
        assertNull(e.ae, "P() @0-8 → G() frees the marker")
        assertEquals(0, e.aB, "and takes no further blow")
    }

    // ------------------------------------------------------------ g.b() gate
    private fun swingAtSoldier(gI: Int, s: Int = 11): Pair<Entity, Entity> {
        val w = world()
        val (gx, gy) = flatGround(w)
        val e = soldier(w, gx, gy, s)
        val p = w.player
        p.setPositionPx(e.ak + 5, e.al); p.av = true
        p.setAnim(67); p.T = 1; p.gI = gI
        p.refreshBoxes(); e.refreshBoxes()
        assertTrue(Entity.overlapStrict(p.X, e.W), "fixture: the swing reaches the soldier")
        e.av = false                              // facing away from the player
        w.npcFsm.tick(e, p)
        return e to p
    }

    @Test fun `j() - the weapon mode gate (gI 1 or 2) decides whether a swing counts`() {
        for (gI in 1..2) assertTrue(swingAtSoldier(gI).first.aB < 300, "gI=$gI is attacking")
        for (gI in intArrayOf(0, 3, 4)) {
            assertEquals(300, swingAtSoldier(gI).first.aB, "gI=$gI: g.b() is false")
        }
    }

    // ------------------------------------------------------------ k.au tables
    @Test fun `ax47 and ax50 start with the bu table hit points of k-au`() {
        for (d in 0..2) {
            val w = world(); w.kAu = d
            val want = intArrayOf(300, 400, 500)[d]
            val r47 = mutableListOf(47, 300, 100, 200, 0, 80)
            while (r47.size < 20) r47 += -1
            val e47 = Entity(47, w.clips[7]); e47.setPositionPx(100, 200)
            w.npcFsm.initAx47(e47, r47)
            val r50 = mutableListOf(50, 400, 100, 200, 0, 120)
            while (r50.size < 20) r50 += -1
            val e50 = Entity(50, w.clips[7]); e50.setPositionPx(100, 200)
            w.npcFsm.initAx50(e50, r50)
            assertEquals(want, e47.aB, "ax47 @3064: aB = bu[k.au], difficulty $d")
            assertEquals(want, e50.aB, "ax50 @2542: aB = bu[k.au], difficulty $d")
        }
    }

    private fun guard73(w: Level0World, aB: Int, au: Int): Entity {
        val e = Entity(73, w.clips[7])
        e.aw = 9100 + w.npcs.size
        val rec = mutableListOf(73, e.aw, 100, 200, 0, 146)
        while (rec.size < 20) rec += -1
        rec[10] = 0
        e.setPositionPx(100, 200)
        w.npcFsm.initAx73(e, rec)
        w.npcs.add(e)
        e.s = Entity(51, null)                     // no edge probes
        e.aB = aB; e.au = au; e.av = true          // looking away from the player
        return e
    }

    private fun swing68(w: Level0World, e: Entity) {
        val p = w.player
        p.S = 68; p.gI = 1
        p.setPositionPx(e.ak + 10, e.al); p.refreshBoxes()
        p.X[0] = e.W[0] - 10; p.X[1] = e.W[1]; p.X[2] = e.W[2] + 10; p.X[3] = e.W[3]
    }

    @Test fun `ax73 - the enrage line is bu of k-au, not the guard's own au score`() {
        // difficulty 1 → 400: a sword blow to 370 enrages (Z0 = 3, S155)
        val w = world(); w.kAu = 1
        val e = guard73(w, 450, au = 10)
        swing68(w, e)
        w.npcFsm.tickAx73(e, w, w.player)
        assertEquals(370, e.aB, "bw = 80")
        assertEquals(3, e.Z[0], "aB <= bu[1] = 400 → C() @88-175")
        assertEquals(155, e.S)
        // difficulty 0 → 300, whatever the screen-distance score says
        val w2 = world(); w2.kAu = 0
        val e2 = guard73(w2, 450, au = 2)
        swing68(w2, e2)
        w2.npcFsm.tickAx73(e2, w2, w2.player)
        assertEquals(370, e2.aB)
        assertEquals(0, e2.Z[0], "370 > bu[0] = 300: no enrage")
        assertEquals(156, e2.S, "the plain hit react, i(156)")
    }

    @Test fun `ax47 and ax50 - the shared j() reacts like the bytes (C() dead arm)`() {
        val w = world()
        val r50 = mutableListOf(50, 400, 100, 200, 0, 120)
        while (r50.size < 20) r50 += -1
        val e50 = Entity(50, w.clips[7]); e50.setPositionPx(100, 200)
        w.npcFsm.initAx50(e50, r50)
        w.npcs.add(e50)
        w.kO = e50.ak - 200; w.kP = e50.al - 120
        e50.aB = 50
        strike(w, e50, 67)
        w.npcFsm.tickAx50(e50, w, w.player)
        assertEquals(129, e50.S, "C() @479-495: a dead ax50 plays i(129)")
        val r47 = mutableListOf(47, 300, 100, 200, 0, 81)
        while (r47.size < 20) r47 += -1
        val e47 = Entity(47, w.clips[7]); e47.setPositionPx(100, 200)
        w.npcFsm.initAx47(e47, r47)
        w.npcs.add(e47)
        e47.aB = 50
        strike(w, e47, 67)
        w.npcFsm.tickAx47(e47, w, w.player)
        assertEquals(0, e47.aB)
        assertEquals(81, e47.S, "a dead ax47 has no react arm")
    }

    @Test fun `i() - the counter-engage only fires while g-b() holds`() {
        // a weakened soldier in a free state engages the swing (player → S8)
        fun engage(gI: Int): Int {
            val w = world()
            val (gx, gy) = flatGround(w)
            val e = soldier(w, gx, gy, 4)
            e.Z[0] = 2
            val p = w.player
            p.setPositionPx(e.ak + 5, e.al); p.av = true
            p.setAnim(67); p.T = 1; p.gI = gI
            p.refreshBoxes(); e.refreshBoxes()
            w.npcFsm.tick(e, p)
            return p.S
        }
        assertEquals(8, engage(1), "g.b() → aS.i(8)")
        assertTrue(engage(3) != 8, "gI = 3: not an attack, the swing is not caught")
    }
}
