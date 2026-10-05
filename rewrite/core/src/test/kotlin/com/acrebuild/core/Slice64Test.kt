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

// ============================================================================
// Slice 64 — ax50 aK() pouncer + ax47 aL() ledge sentinel + shared i.k()
// stealth-kill driver (i.java:9910/10008/2057). Dispatch is `case 47 → aL()`,
// `case 50 → aK()` (i.java:5181-5194; i.javap.txt:20024-20025, proven).
// Slice 346 restored that pairing — slice 64 drove the aK arms through ax47
// and the aL arms through ax50. Citations inline per AGENTS.md.
// ============================================================================
class Slice64Test {
    private fun ax47At(w: Level0World, x: Int, y: Int, vararg f: Int): Entity {
        val e = Entity(47, w.clips[7])
        e.aw = 300 + w.npcs.size
        val rec = mutableListOf(47, e.aw, x, y)
        rec += f.toList()
        while (rec.size < 20) rec += -1
        e.setPositionPx(x, y)
        w.npcFsm.initAx47(e, rec.toList())
        w.npcs.add(e)
        return e
    }
    private fun ax50At(w: Level0World, x: Int, y: Int, vararg f: Int): Entity {
        val e = Entity(50, w.clips[7])
        e.aw = 400 + w.npcs.size
        val rec = mutableListOf(50, e.aw, x, y)
        rec += f.toList()
        while (rec.size < 20) rec += -1
        e.setPositionPx(x, y)
        w.npcFsm.initAx50(e, rec.toList())
        w.npcs.add(e)
        return e
    }
    private fun finish(e: Entity) {
        val c = e.clip!!
        e.T = c.frameCount(e.S) - 1
        e.U = c.frameDuration(e.S, e.T) - 1
    }
    /** Centre the camera on [e] so its W sits inside `k.ac` — aK's `l()`
     *  takes the ax50 arm L77, `b(W,k.ac)`: W ⊆ camera rect (i.java:665). */
    private fun camOn(w: Level0World, e: Entity) {
        w.kO = e.ak - 200; w.kP = e.al - 120; w.rebuildCamRect()
    }
    /** Player standing in the pouncer's column, mid-point at its anchor. */
    private fun playerUnder(w: Level0World, e: Entity) {
        w.player.setPositionPx(e.ak, e.al + 29)
        w.player.setAnim(0); w.player.refreshBoxes()
    }

    // -- init arms -----------------------------------------------------------
    @Test fun `ax47 init — az=f17, aB=bu, Z0=f4, S=f5`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 300)
        assertEquals(300, e.az, "L134 az=r8[17]")
        assertEquals(300, e.aB, "aB=bu[au=0]=300")
        assertEquals(0, e.Z[0])
        assertEquals(80, e.S, "i(r8[5])")
    }

    @Test fun `ax50 init — az=f13 not f17`() {
        val w = world()
        // record idx 13 = vararg slot 9 (4 header fields precede it)
        val e = ax50At(w, 100, 200, 0, 120, 0, 0, 0, -1, 0, 0, 0, 111)
        assertEquals(111, e.az, "L114 az=r8[13] (DIFFERENT from ax47's 17)")
        assertEquals(120, e.S)
    }

    @Test fun `retype — level-0 ax11 records with S 80 or 93 spawn as ax47`() {
        val w = world()
        assertEquals(4, w.npcs.count { it.ax == 47 },
            "pack-6 carries 4 retype-47 records (atlas + i.java:2644)")
        assertEquals(0, w.npcs.count { it.ax == 50 },
            "no ax17+S120 records in pack-6")
        assertTrue(w.npcs.none { it.ax == 11 && it.S in intArrayOf(80, 93) })
    }

    // -- aK() arms (ax50 pouncer) ----------------------------------------------
    @Test fun `aK S120 — drop-kill set + overlap + player below → i89 + i119`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 120)
        // player in drop-kill anim + overlapping + fully above pouncer bottom
        // pouncer W≈[90,166,113,201]; overlap needs p.W[3]>166, grab arm
        // needs p.W[3]<e.W[3] → p.al in (165,200).
        w.player.setPositionPx(100, 180)
        w.player.setAnim(24)
        w.player.refreshBoxes()
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(89, w.player.S, "aS.i(89) ceiling-grab victim anim")
        assertEquals(119, e.S, "i(119)")
        assertEquals(30, e.aC, "aC=30")
        assertEquals(e.ak, w.player.ak, "aS.ak=ak snap")
        assertEquals(e.W[1], w.player.al, "aS.al=W[1] snap")
    }

    @Test fun `aK S120 — W inside the camera + clear LOS → 3x3 pounce pick`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 120)
        camOn(w, e)
        playerUnder(w, e)
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(128, e.S, "overlapping column + p.W[3]>=W[1] → i(128)")
    }

    @Test fun `aK S120 — W only partly inside the camera stays blind`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 120)
        w.kO = e.W[0] + 1; w.kP = e.al - 120; w.rebuildCamRect() // left edge cuts W
        playerUnder(w, e)
        assertTrue(Entity.overlapI(e.W, w.camRect), "W still overlaps the view")
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(120, e.S, "b(W,k.ac) is containment, not overlap → r7 false")
    }

    @Test fun `aK S120 — off-camera perch stays put`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 120)
        w.kO = 5000; w.kP = 5000; w.rebuildCamRect()
        playerUnder(w, e)
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(120, e.S, "r7=false → no pounce")
    }

    @Test fun `aK S120 — bn and player S284 or S285 blind the pouncer`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 120)
        camOn(w, e); playerUnder(w, e)
        w.iBn = true
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(120, e.S, "L77 bn → r0=false")
        w.iBn = false
        for (s in intArrayOf(284, 285)) {
            w.player.setAnim(s); w.player.refreshBoxes()
            w.npcFsm.tickAx50(e, w, w.player)
            assertEquals(120, e.S, "L92/L94 player S$s → false")
        }
    }

    @Test fun `aK S121-128 pounce — T1 sfx16`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 121)
        camOn(w, e); playerUnder(w, e)
        e.T = 1
        w.npcFsm.tickAx50(e, w, w.player)
        assertTrue(16 in w.sfxLog, "T==1 → k.A(16)")
    }

    @Test fun `aK S121-128 pounce — T3 op4 is hurt-sfx only for ax50, r() reverts to S120`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 121)
        camOn(w, e); playerUnder(w, e)
        e.T = 3
        w.npcFsm.tickAx50(e, w, w.player)
        // a(4,…) (structured/i.java:3437-3447): the `r9.c(r13)` stagger
        // needs r13.ax∉{17,50,61} — the pouncer is ax50, so op4 only plays
        // the k.A(18) hurt sfx (the S9 stagger slice 64 asserted came from
        // driving aK through ax47, which the original never does).
        assertEquals(0, w.player.S, "op4 from ax50 → no r9.c(r13) hit-react")
        assertTrue(18 in w.sfxLog, "k.A(18) fires unconditionally")
        // all pounce anims end at T==3 — the last frame always coincides
        // with the hit arm (i.java:9996-9999: hit BEFORE the r() check).
        val w2 = world()
        val e2 = ax50At(w2, 100, 200, 0, 121)
        camOn(w2, e2); playerUnder(w2, e2)
        finish(e2)                                 // T=3 last frame → hit then r()
        w2.npcFsm.tickAx50(e2, w2, w2.player)
        assertTrue(18 in w2.sfxLog, "T==3 arm ran before r()")
        assertEquals(120, e2.S, "pounce r() → S120")
    }

    @Test fun `aK S121 pounce — lost sight reverts to S120`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 121)
        w.kO = 5000; w.kP = 5000; w.rebuildCamRect() // W off the view → l() false
        playerUnder(w, e)
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(120, e.S, "r7==false → i(120)")
    }

    @Test fun `aK S130 — anim end despawns`() {
        val w = world()
        val e = ax50At(w, 100, 200, 0, 130)
        w.npcFsm.tickAx50(e, w, w.player)
        assertTrue(w.npcs.contains(e), "not removed mid-anim")
        finish(e)
        w.npcFsm.tickAx50(e, w, w.player)
        w.tick(emptyList())                       // drain pendingRemove
        assertFalse(w.npcs.contains(e), "r() → k.c(this)")
    }

    // -- aL() arms (ax47 ledge sentinel) ---------------------------------------
    @Test fun `aL S94 — P|512 + claim release + despawn on anim end`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 94)
        w.kL = e                                  // armed claim
        w.npcFsm.tickAx47(e, w, w.player)
        assertTrue(e.P and 512 != 0, "P |= 512")
        finish(e)
        w.npcFsm.tickAx47(e, w, w.player)
        assertNull(w.kL, "k.m() released the claim")
        w.tick(emptyList())
        assertFalse(w.npcs.contains(e), "k.c(this) despawn")
    }

    @Test fun `aL S94 — no claim → still despawns`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 94)
        finish(e)
        w.npcFsm.tickAx47(e, w, w.player)
        w.tick(emptyList())
        assertFalse(w.npcs.contains(e))
    }

    @Test fun `aL S81 — anim end arms P64, countdown expiry picks i82`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 81, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, -1, 0)
        e.aC = 2
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(1, e.aC, "aC-- runs per tick")
        assertEquals(81, e.S)
        finish(e)
        w.npcFsm.tickAx47(e, w, w.player)          // aC 1→0 → still L53 tail
        assertEquals(0, e.aC)
        w.npcFsm.tickAx47(e, w, w.player)          // aC 0→-1 → still tail
        assertEquals(-1, e.aC)
        assertEquals(81, e.S, "post-decrement: i82 fires one tick late")
        w.npcFsm.tickAx47(e, w, w.player)          // old aC -1 → i82 + P&=-65
        assertEquals(82, e.S)
        assertTrue(e.P and 64 == 0, "P &= -65 on expiry")
    }

    @Test fun `aL S82 — r() returns to perch S80`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 82)
        finish(e)
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(80, e.S)
    }

    @Test fun `aL S83 — floor ahead picks i84 freeze else ah 1536 fall`() {
        val w = world()
        val e = ax47At(w, 40, 40, 0, 83)
        // level-0 row at cy=2 — check the actual cell the probe reads
        // (column e.ak/20±1). Force no-floor: park e over empty space.
        w.player.setPositionPx(500, 600); w.player.refreshBoxes()
        w.npcFsm.tickAx47(e, w, w.player)
        // whatever the outcome, the contract is M() drives i(84) vs ah=1536.
        if (e.S == 84) { assertEquals(0, e.ah); assertEquals(0, e.aj) }
        else assertEquals(1536, e.ah, "M()==false → ah=1536")
    }

    @Test fun `aL S84 — r() → i0 idle`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 84)
        finish(e)
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(0, e.S)
    }

    @Test fun `aL S93 — ceiling-grab → p i89 + i80 + aC30 + claim armed`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 93)
        w.player.setPositionPx(100, 180)
        w.player.setAnim(24); w.player.refreshBoxes()
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(89, w.player.S)
        assertEquals(80, e.S)
        assertEquals(30, e.aC)
        assertTrue(w.kL === e, "k.a(this,0,W) arms the claim")
    }

    @Test fun `aL S93 — player not in drop-kill set → nothing`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 93)
        w.player.setPositionPx(100, 180); w.player.setAnim(0); w.player.refreshBoxes()
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(93, e.S, "L26 non-set → return")
    }

    @Test fun `aL S0 and S93 return before k() and j()`() {
        // L52 (S0) and L26 (S93) `return` without the L53 `k(); j()` tail —
        // a sword hit in reach drains nothing (aK would run j() here).
        for (s in intArrayOf(0, 93)) {
            val w = world()
            val e = ax47At(w, 100, 200, 0, s); e.aB = 300
            w.player.setPositionPx(120, 200)
            w.player.setAnim(67); w.player.refreshBoxes()
            w.player.X[0] = 60; w.player.X[1] = 150; w.player.X[2] = 140; w.player.X[3] = 250
            w.player.gI = 1
            w.npcFsm.tickAx47(e, w, w.player)
            assertEquals(300, e.aB, "S$s returns before j()")
        }
    }

    // -- k() shared driver ------------------------------------------------------
    @Test fun `k() — backstab window shows prompt then op6 kill on edge`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)         // S80 perch → L53 k();j()
        w.player.setPositionPx(160, 198)           // 60px right; y-gate |195…-200|<5
        w.player.setAnim(0); w.player.refreshBoxes()
        w.player.av = true                          // player faces left → faces me
        e.av = true                                 // I face left too → away from player
        w.pad.commit(0)
        w.npcFsm.tickAx47(e, w, w.player)
        assertNotNull(w.kN, "k.c spawned the prompt marker")
        assertEquals(e.aw, w.kCq)
        // press the kill key — edge fires
        w.pad.commit(65568)
        w.npcFsm.tickAx47(e, w, w.player)
        assertNull(w.kN, "k.k(aw) released the marker")
        assertEquals(49, w.player.S, "op6 anim 49 (40<|dx|<80)")
        assertEquals(((e.ak - 20) - 160) / 10 shl 8, w.player.ag,
            "ag=((snapX-ak)/10)<<8 slide toward ak-20")
        assertTrue(w.kAp[3] > 0 && 20 in w.sfxLog, "k.o(3)+k.A(20)")
        assertTrue(w.kAp[5] > 0, "S() wisp burst ticked")
        // ax47's L172 tail: else → return true — no anim on the sentinel.
        assertEquals(80, e.S, "sentinel plays no anim on L81 backstab (verbatim)")
        w.pad.commit(0)
    }

    @Test fun `k() — near backstab |dx| under 40 picks anim 283`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        w.player.setPositionPx(130, 198)           // 30px → 283
        w.player.setAnim(0); w.player.refreshBoxes()
        w.player.av = true; e.av = true
        w.pad.commit(65568)
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(283, w.player.S)
        assertEquals(e.ak + 10, w.player.ak,
            "283 snap: ak=attacker.ak∓10 by facing")
        w.pad.commit(0)
    }

    @Test fun `k() — ax47 grab-kill arm — S80 + player S89 + edge → i94, bonus`() {
        val w = world()
        val e = ax47At(w, 100, 200, 3, 80)         // Z[0]=3 → gP bonus value
        w.player.setPositionPx(104, 198)
        w.player.setAnim(89); w.player.refreshBoxes()
        w.pad.commit(65568)
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(94, e.S, "i(94)")
        assertEquals(0, e.aB, "aB=0 killed")
        assertEquals(2, e.aA, "aA=2")
        assertEquals(3, w.gP, "g.p = Z[0]")
        assertEquals(90, w.player.S, "player plays victim anim 90")
        // k.N.i(55) — marker stays alive playing the success anim (no k.k here).
        assertEquals(55, w.kN?.S, "k.N.i(55) success anim")
        assertTrue(w.kN?.P == 0 || (w.kN!!.P and 64) == 0, "k.N.P &= -65")
        w.pad.commit(0)
    }

    @Test fun `k() — ax50 grab-kill arm — S119 + player S89 + edge → i130`() {
        val w = world()
        val e = ax50At(w, 100, 200, 5, 119)
        w.player.setPositionPx(104, 198)
        w.player.setAnim(89); w.player.refreshBoxes()
        w.pad.commit(65568)
        w.npcFsm.tickAx50(e, w, w.player)
        assertEquals(130, e.S)
        assertEquals(5, w.gP)
        w.pad.commit(0)
    }

    @Test fun `k() — prompt survives across ticks and repositions`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        w.player.setPositionPx(160, 198); w.player.setAnim(0); w.player.refreshBoxes()
        w.player.av = true; e.av = true
        w.pad.commit(0)
        w.npcFsm.tickAx47(e, w, w.player)
        val n = w.kN!!
        assertEquals(54, n.S, "k.c marker anim 54")
        assertEquals(302, n.az)
        assertEquals(e.ak, n.ak); assertEquals(e.al - 85, n.al)
        e.setPositionPx(140, 200); e.refreshBoxes()
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(140, n.ak, "repositioned every tick")
        assertSame(n, w.kN, "created once — same instance")
        w.pad.commit(0)
    }

    @Test fun `k() — player S in exit set releases prompt and exits false`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        w.player.setPositionPx(160, 198); w.player.setAnim(268); w.player.refreshBoxes()
        w.pad.commit(0)
        w.npcFsm.tickAx47(e, w, w.player)
        assertNull(w.kN, "L28 arm: no prompt while player in 268")
        w.pad.commit(0)
    }

    // -- j() shared intake -------------------------------------------------------
    @Test fun `j() — sword hit in reach → aB -= H=50`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        e.aB = 300
        w.player.setPositionPx(120, 200)
        w.player.setAnim(67); w.player.refreshBoxes()
        w.player.X[0] = 60; w.player.X[1] = 150; w.player.X[2] = 140; w.player.X[3] = 250
        w.player.gI = 1
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(250, e.aB, "aB -= H[au=0] = 50")
    }

    @Test fun `j() — finisher anim drains J=100`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        e.aB = 300
        w.player.setPositionPx(120, 200)
        w.player.setAnim(183); w.player.refreshBoxes()
        w.player.X[0] = 60; w.player.X[1] = 150; w.player.X[2] = 140; w.player.X[3] = 250
        w.player.gI = 1
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(200, e.aB, "aB -= J = 100")
    }

    @Test fun `j() — dead ax50 plays i129, dead ax47 plays nothing`() {
        val w = world()
        val e5 = ax50At(w, 100, 200, 0, 119); e5.aB = 1
        val e4 = ax47At(w, 300, 200, 0, 80); e4.aB = 1
        w.player.setPositionPx(120, 200)
        w.player.setAnim(67); w.player.refreshBoxes()
        w.player.X[0] = 0; w.player.X[1] = 100; w.player.X[2] = 400; w.player.X[3] = 300
        w.player.gI = 1
        w.npcFsm.tickAx50(e5, w, w.player)
        w.npcFsm.tickAx47(e4, w, w.player)
        assertEquals(129, e5.S, "ax50 death → i(129)")
        assertEquals(80, e4.S, "ax47 death → no anim change (verbatim C() L86)")
    }

    @Test fun `j() — survive → consumed, no hit-react anim`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80); e.aB = 300
        w.player.setPositionPx(120, 200); w.player.setAnim(67); w.player.refreshBoxes()
        w.player.X[0] = 60; w.player.X[1] = 150; w.player.X[2] = 140; w.player.X[3] = 250
        w.player.gI = 1
        w.npcFsm.tickAx47(e, w, w.player)
        assertEquals(80, e.S, "C() L51: survive → true with no anim")
    }

    @Test fun `j() — dead entity releases its ae marker (P() head)`() {
        val w = world()
        val e = ax47At(w, 100, 200, 0, 80)
        e.aB = 0
        e.ae = Entity(14, w.clips[9])
        w.npcFsm.tickAx47(e, w, w.player)
        assertNull(e.ae, "aB<=0 → G() released the marker")
    }
}
