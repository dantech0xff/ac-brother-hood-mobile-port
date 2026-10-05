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

// ---------------------------------------------------------------------------
// slice 48 — ax9 `bM()` contact block + `a()` push + `l()` overlay
// (i.java:21069-21196 / :914-993 / :21198 / init L50 :2781)
// ---------------------------------------------------------------------------
class Slice48Test {

    private fun ax9At(w: Level0World, x: Int, y: Int, s: Int,
                      link: Int = -1, kind8: Int = 0): Entity {
        val e = Entity(9, w.clips[47])
        e.setPositionPx(x, y)
        val f = mutableListOf(9, 0, x, y, 0, s, 0, link, kind8)
        for (i in 9..15) f += 0
        w.npcFsm.initAx9(e, f, w)
        w.npcs.add(e)
        return e
    }

    @Test fun `init S0 binds kAV and loads Z`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 100, 100, 0, link = 555, kind8 = 1)
        assertSame(e, w.kAV, "r8[5]==0 → k.aV = this (i.java:2786)")
        assertEquals(10, e.aB)
        assertEquals(99, e.az)
        assertEquals(0, e.Z[0])
        assertEquals(555, e.Z[1], "Z[1] = r8[7] link uid")
        assertEquals(72, e.Z[2], "Z[2] = k.bn[1] = 72 (k.java:8447)")
        assertEquals(0, e.S, "L392 i(r8[5])")
    }

    @Test fun `init S34 variant binds kAV and loads Z like S0`() {
        // slice 389: `if (r8[5]==0) goto L1211; if (r8[5]!=34) goto L1215;
        // L1211: k.aV = this` — anim 34 ALSO claims k.aV, the Z block is
        // reached by every record, and the arm sets no P bit.
        val w = world(); w.npcs.clear()
        w.kAV = null
        val e = ax9At(w, 100, 100, 34, link = 555)
        assertEquals(555, e.Z[1], "S34 loads Z like every ax9 record")
        assertEquals(47, e.Z[2], "Z[2] = k.bn[r8[8]=0]")
        assertEquals(0, e.P and 512, "no P|=512 in the ax9 arm")
        assertSame(e, w.kAV, "S34 claims k.aV (L1211)")
    }

    @Test fun `preamble binds Z-1 link to ax51 and rides it`() {
        val w = world(); w.npcs.clear()
        val crate = Entity(51, w.clips[7])
        crate.aw = 777; crate.setPositionPx(200, 200)
        crate.ag = 2560; crate.refreshBoxes()
        w.npcs.add(crate)
        val e = ax9At(w, 200, 200, 0, link = 777)
        w.npcFsm.tickAx9(e, w, w.player)
        assertSame(crate, e.s, "k.q(Z[1]) ax51 overlap → s link")
        // the link tick sets al (and ak += s.ag>>8); the ride re-pin with az = s.az + 1
        // is the ELSE of the link scan (bM() @139-199) — it starts the tick after
        assertEquals(crate.W[1] - (e.Y[3] - e.Y[1]) + 5, e.al)
        w.npcFsm.tickAx9(e, w, w.player)
        assertEquals(crate.az + 1, e.az, "next tick: s != null → az = s.az + 1")
        assertEquals(crate.W[1] - (e.Y[3] - e.Y[1]) + 5, e.al)
    }

    @Test fun `contact arm presses i-2 on player-X overlap`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, w.player.ak, w.player.al, 0)
        w.player.refreshBoxes()
        val p = w.player                          // give aS.X a real attackbox
        p.X[0] = e.W[0] - 5; p.X[1] = e.W[1] - 5
        p.X[2] = e.W[2] + 5; p.X[3] = e.W[3] + 5
        w.npcFsm.tickAx9(e, w, w.player)
        assertEquals(2, e.S, "W∩aS.X → i(2) then a() (L30)")
    }

    @Test fun `contact arm pushes player off the left side`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // entity to the player's right, overlapping, p moving right or still
        val e = ax9At(w, p.ak + 4, p.al, 0)
        p.ag = 0; p.refreshBoxes(); e.refreshBoxes()
        // make W overlap certain: give e a W around the player
        e.W[0] = p.ak - 40; e.W[1] = p.al - 40
        e.W[2] = p.ak + 40; e.W[3] = p.al + 40
        val before = p.ak
        e.pushContact(w)
        assertTrue(p.ak < before, "L50 left-block snaps player left")
        assertEquals(0, p.ag, "L63 zeroes ag")
    }

    @Test fun `L57 right-block snaps player right and ends on the shared a(true) ag=0 tail`() {
        // slice 393: the right arm joins the left arm at @463 (`aS.a(true);
        // aS.ag = 0`) — the port's "falls into L25" loop was the simple
        // decompile's displaced S131/146 gate, not a branch of this arm.
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax9At(w, p.ak - 4, p.al, 0)
        p.ag = -1; p.refreshBoxes()
        e.W[0] = p.ak - 40; e.W[1] = p.al - 40
        e.W[2] = p.ak + 40; e.W[3] = p.al + 40
        val before = p.ak
        e.pushContact(w)
        assertTrue(p.ak > before, "snapped to the right edge")
        assertEquals(0, p.ag, "@463-470: aS.ag = 0 after a(true)")
    }

    @Test fun `S139 corpse skips push entirely`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax9At(w, p.ak, p.al, 0)
        e.S = 139; p.refreshBoxes()
        val px = p.ak
        e.pushContact(w)
        assertEquals(px, p.ak)
    }

    @Test fun `ax15 grapple arm snaps player onto edge and claims g-a`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = Entity(15, w.clips[7])
        e.setPositionPx(p.ak + 4, p.al); e.refreshBoxes()
        e.S = 6                                             // grapple anim
        e.W[0] = p.ak - 40; e.W[1] = p.al - 40
        e.W[2] = p.ak + 40; e.W[3] = p.al + 40
        p.refreshBoxes(); p.ac = e                          // held claim released
        p.S = 18                                            // g.b() free set
        e.pushContact(w)
        assertSame(e, p.ga, "g.a = this")
        assertEquals(209, p.S, "aS.i(209) grab anim")
        assertNull(p.ac, "aS.a(null) claim released")
        assertEquals(e.W[1] + 1, p.al)
    }

    @Test fun `l overlay spawns clip31 ax43 child and mirrors`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 300, 300, 18)
        e.T = 4
        w.npcFsm.tickAx9(e, w, w.player)
        val ad = e.ad
        assertNotNull(ad, "l(7) spawns the overlay child")
        assertEquals(43, ad!!.ax); assertEquals(7, ad.S)
        assertEquals(e.ak, ad.ak); assertEquals(e.al, ad.al)
        assertEquals(4, ad.T, "ad.T mirrors parent T")
        assertEquals(e.az + 1, ad.az)
        val first = ad
        w.npcFsm.tickAx9(e, w, w.player)
        assertSame(first, e.ad, "ad reused — no respawn")
    }

    @Test fun `S21 drops overlay and anim-end goes S22`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 300, 300, 21)
        w.npcFsm.tickAx9(e, w, w.player)
        assertNull(e.ad, "L61 ad=null")
        // force anim end → i(22)
        e.T = w.clips[47]!!.frameCount(e.S) - 1
        e.U = w.clips[47]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx9(e, w, w.player)
        assertEquals(22, e.S)
    }

    @Test fun `S2 wind-down goes i-3 on anim end`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 300, 300, 2)
        e.T = w.clips[47]!!.frameCount(e.S) - 1
        e.U = w.clips[47]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx9(e, w, w.player)
        assertEquals(0, e.aB, "L34 zeroes aB")
        assertEquals(3, e.S, "L34 r() → i(3)")
    }

    @Test fun `S3 settle arm marks solid passive on anim end`() {
        val w = world(); w.npcs.clear()
        val e = ax9At(w, 300, 300, 3)
        e.T = w.clips[47]!!.frameCount(e.S) - 1
        e.U = w.clips[47]!!.frameDuration(e.S, e.T) - 1
        w.npcFsm.tickAx9(e, w, w.player)
        assertTrue(e.P and 32 != 0, "L38 → P|=32")
        assertTrue(e.P and 16 == 0, "L38 → P&=-17")
    }
}

// =====================================================================
// Slice 49 — ax15 bu() grapple volume (i.java:16693-16924, proven).
// =====================================================================
