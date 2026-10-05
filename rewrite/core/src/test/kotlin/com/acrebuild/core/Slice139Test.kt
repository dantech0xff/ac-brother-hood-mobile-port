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

/** Slice 139 — ax10 S31 claim-QTE zone tail (i.java:9795-10423): the
 *  4-lane sequence QTE — Z[1] nibble lane types → i.cs[] pad masks →
 *  i.ct[] card frames; aB progress vs Z[2]; aA script uid Z[4]→Z[3];
 *  consumed reset P|=8192 → self-remove; claim bind h/k(k.s(aA)). */
class Slice139Test {
    open class ClaimZoneWorld(cell: Int = 0,
                              cellFn: ((Int, Int) -> Int)? = null) :
        Slice128Test.MarkerWorld(cell, cellFn) {
        val removed = mutableListOf<Entity>()
        val sfxCalls = mutableListOf<Int>()
        override fun removeEntity(e: Entity) { removed += e }
        override fun sfx(id: Int) { sfxCalls += id }
        override var kC: Entity? = null
        var mountMode = true
        override val mounted: Boolean get() = mountMode
        override var iBe = false
        override var iAH = false
        override var kAm = false
        override var kAU: Entity? = null
    }

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    private fun zone(z1: Int = 0x1234, z2: Int = 2, z3: Int = 9, z4: Int = 7): Entity {
        val z = Entity(10, null); z.S = 31
        z.W[0] = 190; z.W[2] = 230; z.W[1] = 60; z.W[3] = 140
        z.Z[1] = z1; z.Z[2] = z2; z.Z[3] = z3; z.Z[4] = z4
        return z
    }

    private fun resetStatics() {
        Entity.gE = false; Entity.icu = false
        Entity.scriptPrompts.fill(null)
    }

    @kotlin.test.AfterTest fun cleanupStatics() = resetStatics()

    @Test fun `S31 iBe removes zone i9799`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.iBe = true
        val z = zone(); val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertSame(z, w.removed.single(), "i.be → k.c(this)")
    }

    @Test fun `S31 claimAb ticks bound script i9802`() {
        resetStatics()
        // armed claim (i.cK>=0) opens aa() → kBy[0] must exist — give the
        // world one empty op-block so the bound-script tick no-ops.
        val w = object : ClaimZoneWorld() {
            override val kBy: Array<Array<ByteArray>>
                get() = arrayOf(arrayOf(byteArrayOf(0, 0, 0)))
        }
        val z = zone(); z.ca = 0; z.scriptStep = 0; z.scriptOps = IntArray(1)
        val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertTrue(w.removed.isEmpty(), "aa() tick — no removal")
        assertFalse(Entity.gE, "early return — gE untouched")
    }

    @Test fun `S31 consumed flag self-removes i9807`() {
        resetStatics()
        val w = ClaimZoneWorld()
        val z = zone(); z.P = 8192
        NpcFsm(w).tickTrigger(z, w, mk(200, 100), Pad())
        assertSame(z, w.removed.single(), "P&8192 → k.c(this)")
    }

    @Test fun `S31 arm pass fills lanes and cards i10030`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.mountMode = false        // !k.k() → clip74 cards
        val z = zone(z1 = 0x1234, z2 = 99)
        val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(intArrayOf(1, 2, 3, 4).toList(), z.X.toList(),
            "X lanes = Z[1] nibbles")
        assertEquals(7, z.aA, "aA = Z[4]")
        assertEquals(300, z.az, "az = 300")
        assertEquals(16, z.P and 16, "P |= 16")
        assertEquals(0, z.P and 128, "P &= ~128")
        assertEquals(1, z.aB, "scan tick → aB++")
        assertEquals(0, z.m, "lane cursor = first live lane")
        assertEquals(1, z.pv, "pv = X[0] lane type")
        assertEquals(1, z.j, "j cursor advanced past lane 0")
        assertTrue(Entity.scriptPrompts[0] != null &&
            Entity.scriptPrompts[3] != null, "bA cards spawned per lane")
        assertEquals(74, Entity.scriptPrompts[0]!!.clipIdx,
            "!k.k() → clip74 touch card")
        assertEquals(1, z.aB, "aB progress only (no hit)")
    }

    @Test fun `S31 mounted pass binds clip9 key cards i10148`() {
        resetStatics()
        val w = ClaimZoneWorld()                              // mounted → k.k()
        val z = zone(z1 = 0x0100, z2 = 99)              // lane1 = type1, rest 0
        val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, z.X[0]); assertEquals(1, z.X[1])
        assertNull(Entity.scriptPrompts[0],
            "X==0 before first live lane → skipped (r9!=3 && m==0 && X==0)")
        assertTrue(Entity.scriptPrompts[1] != null &&
            Entity.scriptPrompts[2] != null,
            "once live, later zero lanes still spawn")
        assertEquals(9, Entity.scriptPrompts[1]!!.clipIdx,
            "k.k() → clip9 key card")
        assertEquals(3, z.aD, "3 spawned lanes (r9=3 always spawns)")
    }

    @Test fun `S31 lane hit advances cursor i10224`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.mountMode = false
        val z = zone(z1 = 0x1234, z2 = 99)
        val p = mk(200, 100)
        val fsm = NpcFsm(w)
        val pad = Pad()
        fsm.tickTrigger(z, w, p, pad)                   // arm: m=0,pv=1,j=1
        pad.queuePress(Entity.CS[1]); pad.commit(0)     // cs[1]=2 → v() edge
        fsm.tickTrigger(z, w, p, pad)
        assertEquals(1, z.m, "hit → m = j (next lane)")
        assertEquals(2, z.pv, "pv = X[1]")
        assertEquals(2, z.j, "j++")
        assertEquals(2, z.aB, "aB++ once more")
    }

    @Test fun `S31 last lane hit sets aA sentinel i10255`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.mountMode = false
        val z = zone(z1 = 0x1111, z2 = 99)              // all lanes type1
        val p = mk(200, 100)
        val fsm = NpcFsm(w)
        val pad = Pad()
        fsm.tickTrigger(z, w, p, pad)                   // arm
        repeat(3) {
            pad.queuePress(Entity.CS[1]); pad.commit(0)
            fsm.tickTrigger(z, w, p, pad)
        }
        assertEquals(4, z.j, "3 lane hits → j = 4 (j>3 gates next hit)")
        pad.queuePress(Entity.CS[1]); pad.commit(0)
        fsm.tickTrigger(z, w, p, pad)
        assertEquals(9, z.aA, "j>3 → aA = Z[3] done sentinel")
        assertTrue(w.sfxCalls.contains(25), "k.A(25) on completion")
    }

    @Test fun `S31 full progress auto-passes lane i10395`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.mountMode = false
        val z = zone(z1 = 0x1000, z2 = 1, z3 = 9)       // aB>=Z[2] at scan
        val p = mk(200, 100)
        val fsm = NpcFsm(w)
        fsm.tickTrigger(z, w, p, Pad())                 // arm, aB→1
        fsm.tickTrigger(z, w, p, Pad())                 // aB>=1 → success
        assertEquals(10, z.m, "m = 10 + lane → resolved marker")
    }

    @Test fun `S31 consumed reset binds claim script i9870`() {
        resetStatics()
        val w = ClaimZoneWorld(); w.mountMode = false
        val z = zone()
        z.aB = 3; z.nl = 1                              // busy latch (script-set)
        z.aA = 5                                        // armed script uid
        Entity.scriptPrompts[0] = ScriptPrompt()
        val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(8192, z.P and 8192, "P |= 8192 consumed")
        assertEquals(0, z.aB); assertEquals(0, z.nl)
        assertEquals(0, z.X.sum(), "X cleared")
        assertNull(Entity.scriptPrompts[0], "bA[] cleared")
        assertFalse(Entity.gE, "g.E lifted while consumed")
        assertSame(z, w.kC, "aA>0 → k.C = this")
        assertEquals(512 + 16 + 128, z.P and (512 + 16 + 128), "P|=512|16|128")
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertSame(z, w.removed.last(), "P&8192 → removed next tick")
    }

    @Test fun `S31 busy tick with nl set exits early i9929`() {
        resetStatics()
        val w = ClaimZoneWorld()
        val z = zone(); z.nl = 1                        // n!=0, aB==0 → L737
        val p = mk(200, 100)
        NpcFsm(w).tickTrigger(z, w, p, Pad())
        assertEquals(0, z.aB, "no scan — n!=0 returned")
        assertTrue(Entity.gE, "overlap still latches gE")
    }

    // -- S55 (Lf4, i.java:9227-9253) — boss-reposition zone ----------

    private fun bossAt(ak: Int, s: Int = 13, y1: Int = 60, y2: Int = 140): Entity {
        val b = Entity(29, null); b.S = s; b.ak = ak; b.al = 100
        b.Y[0] = 190; b.Y[2] = 230; b.Y[1] = y1; b.Y[3] = y2
        b.ah = 111; b.ag = 222
        return b
    }

    private fun zone55(ak: Int = 210): Entity {
        val z = Entity(10, null); z.S = 55; z.ak = ak
        z.W[0] = 190; z.W[2] = 230; z.W[1] = 60; z.W[3] = 140
        return z
    }

    @Test fun `S55 repositions S13 boss on overlap i9227`() {
        val w = ClaimZoneWorld()
        val z = zone55(); val boss = bossAt(999); w.kAU = boss
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertEquals(25, boss.S, "k.aU.i(25)")
        assertEquals(210, boss.ak, "aU.ak ← zone.ak")
        assertEquals(100, boss.al, "aU.al untouched")
        assertEquals(0, boss.ah); assertEquals(0, boss.ag)
    }

    @Test fun `S55 no-op without bound boss Lf4`() {
        val w = ClaimZoneWorld()                                 // kAU null
        NpcFsm(w).tickTrigger(zone55(), w, mk(0, 0), Pad())      // bare return
    }

    @Test fun `S55 no-op when boss not S13 Lf4`() {
        val w = ClaimZoneWorld()
        val z = zone55(); val boss = bossAt(999, s = 7); w.kAU = boss
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertEquals(7, boss.S); assertEquals(999, boss.ak)
        assertEquals(111, boss.ah)
    }

    @Test fun `S55 no-op when boss rect misses zone Lf4`() {
        val w = ClaimZoneWorld()
        val z = zone55()
        val boss = bossAt(999, y1 = 500, y2 = 560); w.kAU = boss   // Y far below
        NpcFsm(w).tickTrigger(z, w, mk(0, 0), Pad())
        assertEquals(13, boss.S); assertEquals(999, boss.ak)
    }
}

// ============================================================ slice 140
// ax10 S0 camera-focus zone (L5ac-L5e0) + S2 flag-apply trigger
// (L13a1-L14a2) + initTrigger's per-S record arms (L59/L95).
