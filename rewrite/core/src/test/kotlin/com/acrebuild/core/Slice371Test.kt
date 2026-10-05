package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 371 — the NPC-side follow-ups recorded by slice 364, each
 * re-verified against the bytecode (`reconstructed-project/bytecode/
 * i.javap.txt` unless noted) and fixed there:
 *
 *  1. aV() S50 (@7562-7750): the else-arms `G()` + `g.C = true` (rope,
 *     no overlap) and `G()` (no rope).
 *  2. l() (@221-231): ax11 S117 on an ax69 `Z[0]==0` ride is `true`
 *     straight to the @713 LOS tail.
 *  3. v() @206-214 / aX() @101-109 test `k.bh[k.aj]==3`, not `!k.al`.
 *  4. One `v()` port (`inPlayV`): the divergent copies (`wasHitRecently`,
 *     `markerVisible`, `ax64Alive`) are gone; ax14 `W == null` is the
 *     zero rect, and the runtime ax14 markers are no `npcs` members
 *     (`a(III)V` @0-82 / `k.c(III)` never `k.b`-insert).
 *  5. bc() (@0-865): per-type `i.a` tests, the ax54 `ad`-only hit, the
 *     ax32 abort before any overlap, ax67/ax24 hits end the scan — one
 *     port for bb() and ba().
 *  6. B() @504 and g.n() @35/@2149 call the full v() (`u()`/`au>i`
 *     guard); g.n()'s S2/S24 `!v()` is `k.l(12)` (@2152-2157).
 *  7. aV() S47 (@538) / aa() op37[17] (@1715) null the Image `k.aQ` —
 *     the eagle-view inset the port keeps as `volPaintRect`.
 */
class Slice371Test {

    // ------------------------------------------------------------ item 1
    private fun s50Setup(): Triple<Level0World, Entity, Entity> {
        val w = world()
        val p = w.player
        p.setPositionPx(400, 300)
        intArrayOf(390, 260, 410, 300).copyInto(p.W)
        val rope = Entity(43, null); rope.S = 1
        p.ga = rope
        p.cFlag = true                                    // o() bind set it
        val z = Entity(10, null); z.S = 50
        intArrayOf(370, 240, 430, 310).copyInto(z.W)
        return Triple(w, p, z)
    }

    @Test fun `aV S50 rope leaving the zone drops the marker and re-arms g-C`() {
        val (w, p, z) = s50Setup()
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertFalse(p.cFlag, "@7666 inside the zone: g.C = false")
        assertNotNull(z.ae, "@7633 a(7,…) marker")
        intArrayOf(890, 260, 910, 300).copyInto(p.W)        // rope carries him out
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertNull(z.ae, "@7737 G()")
        assertTrue(p.cFlag, "@7742 g.C = true")
        assertFalse(z in w.pendingRemove)
    }

    @Test fun `aV S50 without a rope drops the marker`() {
        val (w, p, z) = s50Setup()
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertNotNull(z.ae)
        p.ga = null                                       // dismounted elsewhere
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertNull(z.ae, "@7746 G()")
        assertFalse(p.cFlag, "no g.C write on this arm")
    }

    // ------------------------------------------------------------ item 2
    private fun s117(eS: Int): Boolean {
        val w = world()
        val p = w.player
        p.setAnim(0)
        val e = Entity(11, null)
        e.setPositionPx(p.ak, p.al)
        p.W.copyInto(e.W)                                 // LOS: one cell
        e.S = eS
        val af = Entity(69, null); af.S = 7; af.Z[0] = 0  // af.S==7 is "blind"
        p.af = af
        return losL(e, p, w)
    }

    @Test fun `l() ax11 S117 on a Z0==0 ride goes straight to the LOS tail`() {
        assertTrue(s117(117), "@221-231 iconst_1 → @713: af.S==7 never read")
        assertFalse(s117(5), "control: other states read af.S==7 → blind")
    }

    // ------------------------------------------------------------ item 3
    @Test fun `aX() linked arm is the bh3 arm only — mission 0 consumes on r5-S == j`() {
        val w = world()                                   // k.aj = 0, bh = 4
        assertFalse(w.missionBh() == 3); assertTrue(w.inPlay)
        val target = Entity(11, w.clips[7]).apply { aw = 55; S = 7 }
        w.npcs.add(0, target)
        val e = Entity(14, w.clips[9])
        e.setPositionPx(500, 500)
        w.npcFsm.initPickup(e, listOf(14, 14, 500, 500, 0, 69, 0, -30, -40, 60, 80, 55, 7))
        w.npcs += e
        w.player.setPositionPx(900, 500); w.player.refreshBoxes()
        w.npcFsm.tickPickup(e, w.player)
        assertTrue(e in w.pendingRemove, "@246-266: r5.S == j && aA != 1 → k.c")
        assertEquals(0, e.P and 16, "the @112-209 countdown arm never ran")
        assertEquals(0, e.aC)
    }

    @Test fun `v() ax14 arm on a non-bh3 mission tests the player's W`() {
        // aV() S2 guard (Z0==2): `k.q(Z[2]).v()` (@5095). An ax14 guard on
        // camera but off the player: v() @206-214 `bh[aj]!=3` → @265
        // `i.a(aS.W, W)` = false → the flags apply.
        val w = world()
        val p = w.player
        val cx = w.camX; val cy = w.camY
        p.setPositionPx(cx + 60, cy + 200)
        intArrayOf(cx + 50, cy + 160, cx + 70, cy + 200).copyInto(p.W)
        val gate = Entity(14, null).apply { aw = 9_101; ak = cx + 200; al = cy + 120 }
        intArrayOf(cx + 180, cy + 100, cx + 220, cy + 140).copyInto(gate.W)
        val target = Entity(11, null).apply { aw = 9_102 }
        w.npcs.add(0, gate); w.npcs.add(0, target)
        val z = Entity(10, null); z.S = 2
        z.Z[0] = 2; z.Z[1] = 9_102; z.Z[2] = 9_101; z.Z[3] = 512
        intArrayOf(cx + 40, cy + 150, cx + 80, cy + 210).copyInto(z.W)
        assertFalse(gate.inPlayV(w), "a(aS.W, W): the guard is off the player")
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(512, target.P and 512, "guard not in play → P |= Z[3]")
        // the same guard on a bh3 mission reads the camera (@244-251)
        val w1 = world(); w1.kAj = 1
        val g1 = Entity(14, null).apply { ak = w1.camX + 200; al = w1.camY + 120 }
        intArrayOf(w1.camX + 180, w1.camY + 100, w1.camX + 220, w1.camY + 140).copyInto(g1.W)
        assertTrue(g1.inPlayV(w1))
    }

    // ------------------------------------------------------------ item 4
    @Test fun `v() u() keeps the aG==4 arm for ax13 only`() {
        // aV() S0 (@1452-1504): `i.a(aS.W, W) && v()` — an ax10 focus zone
        // with aG == 4 scores `|dy|/120` (u() @39-65 gates /240 on ax13).
        val w = world()
        val p = w.player
        val cx = w.camX; val cy = w.camY
        intArrayOf(cx + 190, cy + 150, cx + 210, cy + 200).copyInto(p.W)
        val z = Entity(10, null); z.S = 0; z.aG = 4
        z.ak = cx + 200; z.al = cy + 120 + 250              // |dy| = 250
        intArrayOf(cx + 150, cy + 100, cx + 250, cy + 380).copyInto(z.W)
        w.camAg = 99
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(2, z.au, "250/120 — not 250/240")
        assertEquals(0, w.camAg, "au > i → v() false → the L5e0 wipe")
        z.al = cy + 120 + 100                               // au = 0 → in play
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(4, w.camAg)
    }

    @Test fun `v() sends ax78 S3 to the camera-vs-W arm`() {
        // aV() S2 guard again: ax78 S3 → `i.a(k.ac, W)` (@296-320).
        val w = world()
        val p = w.player
        val cx = w.camX; val cy = w.camY
        intArrayOf(cx + 50, cy + 160, cx + 70, cy + 200).copyInto(p.W)
        val gate = Entity(78, null).apply { aw = 9_201; S = 3; ak = cx + 200; al = cy + 120 }
        intArrayOf(cx + 180, cy + 100, cx + 220, cy + 140).copyInto(gate.W)   // on camera
        intArrayOf(cx + 900, cy + 100, cx + 920, cy + 140).copyInto(gate.Y)   // off camera
        val target = Entity(11, null).apply { aw = 9_202 }
        w.npcs.add(0, gate); w.npcs.add(0, target)
        val z = Entity(10, null); z.S = 2
        z.Z[0] = 2; z.Z[1] = 9_202; z.Z[2] = 9_201; z.Z[3] = 512
        intArrayOf(cx + 40, cy + 150, cx + 80, cy + 210).copyInto(z.W)
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(0, target.P and 512, "guard in play (W on camera) → wait")
        assertFalse(z in w.pendingRemove)
        gate.S = 0                                          // other states: the Y arm
        w.npcFsm.tickTrigger(z, w, p, Pad())
        assertEquals(512, target.P and 512)
    }

    @Test fun `v() ax14 W==null is the zero rect — the rope ab marker is in play`() {
        val w = world()
        val p = w.player
        val ab = p.spawnChildFx(w, 14, 9, 11, 302)          // aW() a(14,9,11,302)
        ab.ak = w.camX + 200; ab.al = w.camY + 120
        assertTrue(ab.W.contentEquals(Entity.ZERO_RECT), "t() never allocates an ax14 W")
        assertTrue(ab.inPlayV(w), "@197-205 W == null → true (k.b(boolean) @2146 draws it)")
        ab.ak = w.camX + 2_000                              // far: au > i first
        assertFalse(ab.inPlayV(w))
    }

    @Test fun `a(III)V markers live only in ae — never in npcs, never drawn after G()`() {
        val w = world()
        w.tick(emptyList())
        val p = w.player
        val e = Entity(11, w.clips[7])
        e.setPositionPx(p.ak + 40, p.al); e.refreshBoxes()
        w.npcs += e
        e.spawnMarker(w, 45, e.ak, e.al - 85)
        val m = e.ae!!
        assertFalse(m in w.pendingInsert, "a(III)V @0-82: no k.b")
        w.tick(emptyList())
        assertFalse(m in w.npcs)
        fun draws(): Int { var n = 0; for (i in 0 until w.drawCount) if (w.drawList[i] === m) n++; return n }
        w.buildDrawList()
        assertSame(m, e.ae)
        assertEquals(1, draws(), "drawn once, through the owner's ae (@1650-1682)")
        e.releaseAe()                                       // G() → p()
        w.buildDrawList()
        assertEquals(0, draws(), "a released marker is gone")
    }

    @Test fun `bb() S23 prop survives while v() holds — the inverse of the old score test`() {
        val w = world()
        val p = w.player
        val cx = w.camX; val cy = w.camY
        intArrayOf(cx + 2_000, cy, cx + 2_020, cy + 40).copyInto(p.W)   // nowhere near
        val e = Entity(16, null); e.S = 23
        e.ak = cx + 200; e.al = cy + 120                    // au = 0
        intArrayOf(cx + 300, cy + 50, cx + 320, cy + 70).copyInto(e.W)
        intArrayOf(cx + 190, cy + 110, cx + 210, cy + 130).copyInto(e.Y)
        w.npcFsm.tickRequestMarker(e, p, Pad())
        assertFalse(e in w.pendingRemove, "@1223-1243: v() true → kept")
        intArrayOf(cx + 900, cy + 110, cx + 910, cy + 130).copyInto(e.Y) // off camera
        w.npcFsm.tickRequestMarker(e, p, Pad())
        assertTrue(e in w.pendingRemove, "!v() → k.c(this)")
    }

    @Test fun `bl() S7 harrier with its Y off camera is removed even at au 0`() {
        val w = world()
        val p = w.player
        p.S = 1                                             // not a stalk-vulnerable anim
        val cx = w.camX; val cy = w.camY
        val e = Entity(64, null); e.S = 7; e.aC = 100; e.Z[8] = 50
        e.ak = cx + 200; e.al = cy + 120                    // au = 0
        intArrayOf(cx + 600, cy + 100, cx + 620, cy + 140).copyInto(e.Y)
        w.npcFsm.tickAx64(e, w, p)
        assertEquals(0, e.au)
        assertTrue(e in w.pendingRemove, "bl() @2499: !v() → k.c (a(k.ac, Y) false)")
    }

    // ------------------------------------------------------------ item 5
    private fun sweepWorld(): Level0World = world().also { it.npcs.clear() }
    private fun sweeper(w: Level0World): Entity = Entity(16, null).apply {
        S = 16; af = w.player
        intArrayOf(100, 100, 140, 140).copyInto(X)
    }

    @Test fun `bc() ax54 hit through its ad alone`() {
        val w = sweepWorld(); w.kAj = 1
        val s = sweeper(w)
        val r3 = Entity(54, null).apply { aw = 77 }
        intArrayOf(500, 500, 540, 540).copyInto(r3.W)        // the body misses
        val ad = Entity(55, null)
        intArrayOf(110, 110, 130, 130).copyInto(ad.W)        // the child hits
        r3.ad = ad
        w.npcs += r3
        w.paint(r3)
        val kills = w.kAp[0]
        assertTrue(s.sweepNeighborsB(w), "@86 i.a(ad.W, X) → hit")
        assertEquals(10, r3.S); assertEquals(2, ad.S); assertEquals(9, s.S)
        assertEquals(kills + 1, w.kAp[0], "k.e(0, aw) → ap[0]++")
    }

    @Test fun `bc() armed ax32 aborts the scan before any overlap`() {
        val w = sweepWorld()
        val s = sweeper(w)
        val wall = Entity(32, null).apply { l = 1; S = 21 }
        intArrayOf(900, 900, 940, 940).copyInto(wall.W)      // nowhere near X
        val deco = Entity(67, null).apply { S = 19; aB = 5 }
        intArrayOf(110, 110, 130, 130).copyInto(deco.W)
        w.npcs += wall; w.npcs += deco
        w.paint(wall, deco)
        w.cFFlag = false
        assertFalse(s.sweepNeighborsB(w), "@625-646: S∈[21,27] && !cF → END")
        assertEquals(5, deco.aB); assertEquals(16, s.S)
        w.cFFlag = true                                      // a full gauge throws through
        assertTrue(s.sweepNeighborsB(w))
        assertEquals(4, deco.aB)
    }

    @Test fun `bc() ax67 and ax24 hits end the scan`() {
        val w = sweepWorld()
        val s = sweeper(w)
        val d1 = Entity(67, null).apply { S = 19; aB = 5 }
        val d2 = Entity(67, null).apply { S = 19; aB = 5 }
        intArrayOf(110, 110, 130, 130).copyInto(d1.W)
        intArrayOf(110, 110, 130, 130).copyInto(d2.W)
        w.npcs += d1; w.npcs += d2
        w.paint(d1, d2)
        assertTrue(s.sweepNeighborsB(w))
        assertEquals(4, d1.aB)
        assertEquals(5, d2.aB, "@555 goto 864: one decor per sweep")
        w.npcs.clear()
        val f1 = Entity(24, null).apply { S = 19 }
        val f2 = Entity(24, null).apply { S = 19 }
        intArrayOf(110, 110, 130, 130).copyInto(f1.W)
        intArrayOf(110, 110, 130, 130).copyInto(f2.W)
        w.npcs += f1; w.npcs += f2
        w.paint(f1, f2)
        assertTrue(s.sweepNeighborsB(w))
        assertEquals(20, f1.S)
        assertEquals(19, f2.S, "@604 goto 864: one shrine per sweep")
    }

    @Test fun `bb() S16 sweep tallies kills in ap 0`() {
        val w = sweepWorld(); w.kAj = 1                     // bh3 arm → bc()
        val s = sweeper(w)
        val r3 = Entity(56, null).apply { aw = 78 }
        intArrayOf(110, 110, 130, 130).copyInto(r3.W)
        w.npcs += r3
        w.paint(r3)
        val kills = w.kAp[0]
        w.npcFsm.tickRequestMarker(s, w.player, Pad())
        assertEquals(10, r3.S)
        assertEquals(kills + 1, w.kAp[0], "k.e(0,aw) is ap[0], not a side counter")
    }

    @Test fun `ba() sweeps through the same bc() — one kill, no i(9) on a survivor`() {
        val w = sweepWorld(); w.kAj = 1
        val p = w.player
        fun shot(): Entity = Entity(24, null).apply {
            S = 10; af = p
            intArrayOf(100, 100, 140, 140).copyInto(X)
        }
        // ax54 whose ad AND body both overlap: @142 ends the scan after
        // the ad hit — one tally, not two.
        val r54 = Entity(54, null).apply { aw = 79 }
        intArrayOf(110, 110, 130, 130).copyInto(r54.W)
        r54.ad = Entity(55, null).also { intArrayOf(110, 110, 130, 130).copyInto(it.W) }
        w.npcs += r54
        w.paint(r54)
        val kills = w.kAp[0]
        w.npcFsm.tickAx24(shot(), w, p)
        assertEquals(kills + 1, w.kAp[0])
        // ax30 survivor: `i(9)` only on the kill branch (@260-289)
        w.npcs.clear()
        val r30 = Entity(30, null).apply { aB = 100 }
        intArrayOf(110, 110, 130, 130).copyInto(r30.W)
        w.npcs += r30
        w.paint(r30)
        val sh = shot()
        w.npcFsm.tickAx24(sh, w, p)
        assertEquals(80, r30.aB); assertEquals(6, r30.cGCount)
        assertEquals(10, sh.S, "survivor → the shot keeps its anim")
    }

    // ------------------------------------------------------------ item 6
    @Test fun `B() kills through the full v() — the au guard counts`() {
        val w = world().also { it.kAj = 1; it.iW = false }
        val p = w.player
        p.setPositionPx(1500, 900); p.refreshBoxes()
        p.canyonCollide(w)                                  // i.w latch
        w.kP = 0; w.rebuildCamRect()
        // a tall Y still overlaps k.ac, but |dy| = 780 → au > i → !v()
        intArrayOf(w.camX + 100, 200, w.camX + 200, 950).copyInto(p.Y)
        assertFalse(p.canyonCollide(w))
        assertEquals(12, w.jC, "B() @504: !v() && al > k.P+240 → k.l(12)")
    }

    @Test fun `g-n() S2 off camera fails the mission (bytecode 2152-2157)`() {
        val w = world(); w.npcs.clear(); w.kAj = 1          // bh[1] == 3 → n()
        w.stateL(8)
        w.tick(emptyList())                                 // i.w first-call latch
        val p = w.player
        p.setAnim(2)
        // right of the view, inside its height: au = 0, Y misses k.ac, and
        // al <= k.P+240 keeps B()'s own kill out of it
        p.setPositionPx(w.camX + 520, w.camY + 120); p.refreshBoxes()
        assertFalse(p.animFinished())
        w.tick(emptyList())
        assertEquals(12, w.jC, "S2: r() || !v() → k.l(12)")
    }

    // ------------------------------------------------------------ item 7
    @Test fun `aV S47 clears the eagle-view inset k-aQ`() {
        val w = world()
        w.volPaintRect = intArrayOf(1, 2, 3, 4)
        val z = Entity(10, null); z.S = 47
        w.npcFsm.tickTrigger(z, w, w.player, Pad())
        assertNull(w.volPaintRect, "@538 putstatic k.aQ:Image = null")
    }

    private class InsetWorld(block: ByteArray) : Slice139Test.ClaimZoneWorld() {
        override var volPaintRect: IntArray? = intArrayOf(1, 2, 3, 4)
        private val blocks = arrayOf(arrayOf(block))
        override val kBy: Array<Array<ByteArray>> get() = blocks
    }

    @Test fun `aa() op37 sub-op 17 clears the eagle-view inset k-aQ`() {
        // one type-0 block: header 4 (1 group), group key 0 / 1 op:
        // `op37 [17]`, then the u16 bz tail
        val blk = byteArrayOf(0, 0, 1, 0, 0, 0, 1, 37, 17, 0, 4, 0)
        val w = InsetWorld(blk)
        val z = Entity(10, null)
        z.ca = 0; z.scriptStep = 0; z.scriptOps = intArrayOf(4)
        z.runClaimScript(w)
        assertNull(w.volPaintRect, "aa() @1714-1715 k.aQ = null")
    }
}
