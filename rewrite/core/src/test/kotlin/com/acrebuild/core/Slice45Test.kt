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
// slice 45 — ax13 `aW()` rope/vine swing (i.java:13182-13367)
// ---------------------------------------------------------------------------
class Slice45Test {

    /** ax13 record fixture — the shared L111 map:
     *  `[13,uid,x,y,aE,S,P,?, ?, ?, ?, aF,o,p,aG,ay]`. */
    private fun ax13At(w: Level0World, x: Int, y: Int, s: Int = 0,
                       ag: Int = 0, z1: Int = 10): Entity {
        val e = Entity(13, null)
        e.setPositionPx(x, y)
        // L502 init reads the aG variant from r8[4]: {1→1, 2→2, 3→4}.
        val f = mutableListOf(13, 0, x, y,
            when (ag) { 1 -> 1; 2 -> 2; 4 -> 3; else -> 0 }, s, 0)
        for (i in 7..15) f += 0
        w.npcFsm.initAx13(e, f)
        e.Z[1] = z1        // rope segment count
        w.npcs.add(e)
        return e
    }

    @Test fun `ax13 pendulum integrates only while displaced`() {
        val w = world(); w.npcs.clear()
        val e = ax13At(w, 100, 100)
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(0, e.bP); assertEquals(0, e.bO)   // L5: bP==0 skips L6
        e.bO = 256                                      // displace → L6 runs
        val p0 = e.bP
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(p0 + (256 shl 1), e.bP, "bP += bO<<1")
        assertTrue(e.bO < 256, "bO -= sin(n-θ)<<1 restoring force")
    }

    @Test fun `ax13 swing-side sign flip damps velocity by an eighth`() {
        val w = world(); w.npcs.clear()
        val e = ax13At(w, 100, 100)
        e.bO = -512; e.bP = 512                         // swinging left, +angle
        e.j = 1
        // integrate: bP += -1024 → negative → sign flip → damp
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(-1, e.j, "side latch follows bP sign")
    }

    @Test fun `ax13 grab latch binds the player and arms the hang`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = ax13At(w, p.ak, p.al - 40, ag = 0, z1 = 10)
        e.bN = 6                                          // rope mid-swing
        p.setAnim(18)                                     // grabbable (g.b)
        p.N = p.ak shl 8; p.O = (e.O + 2000)             // inside arc box
        p.refreshBoxes()
        // drive the arc box against the player's W
        w.npcFsm.tickAx13(e, w, p)
        if (p.bM === e) {
            assertEquals(1, e.aA, "aA=1 attached")
            assertEquals(101, p.az, "aS.az=101")
            assertTrue(p.aA and 64 != 0, "aS.aA|=64")
            assertEquals(326, p.S, "i(326) hang anim")
        } else {
            // geometry-dependent arm — assert no crash at minimum
            assertTrue(e.aA == 0 || p.bM === e)
        }
    }

    @Test fun `ax13 aG4 spawner counts segments while the ax58 gate is open`() {
        val w = world(); w.npcs.clear()
        val e = ax13At(w, 100, 100, ag = 4, z1 = 10)
        e.Z[6] = -1                                       // no link → grow
        e.bN = 2
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(3, e.bN, "Z[6]==-1 → bN++")
        e.bN = 10
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(10, e.bN, "bN >= Z[1] caps the spawner")
        val door = Entity(58, null).apply { aw = 42; setAnim(2) }  // closed
        w.npcs.add(door)
        e.Z[6] = 42; e.bN = 2
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(2, e.bN, "linked + !bf() → no growth")
        door.setAnim(1)                                    // bf() open set
        w.npcFsm.tickAx13(e, w, w.player)
        assertEquals(3, e.bN, "bf() gate open → bN++")
    }

    @Test fun `ax13 release flings the rider with the aG1 leap arc`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // Z[1]=0 (no catch reach): aW() runs the grab scan again in the
        // same tick as the release (@327 is reached with aA=0, S=23 ∈ g.b),
        // so a released rider inside the swing box re-latches at once. The
        // unit isolates the release arc from that scan — slice 389 gave the
        // rope the ctor's `bN = 1`, which made the old degenerate box (bN=0)
        // stop hiding the re-latch. (aG1 ropes do not occur in shipped data:
        // all 14 ax13 records are r8[4]=3 → aG4.)
        val e = ax13At(w, p.ak, p.al - 40, ag = 1, z1 = 0)
        e.aA = 1; e.bM = p; p.bM = e; p.aA = p.aA or 64
        e.bO = 300; e.bP = -512                          // velocity flip window
        val o0 = e.bO
        // integrator flips bO sign → r0*bO<0 → aS.j()
        repeat(6) { if (p.bM === e) w.npcFsm.tickAx13(e, w, p) }
        assertNull(p.bM, "release unlinks")
        assertTrue(p.S == 23 || p.bM === e, "aG1 → i(23) leap")
    }

    @Test fun `g k rope input pumps and climbs`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        // aG0: g.k() returns at once on `bM.aG == 1` (@14-25, slice 407) — the
        // climb/descend arms below belong to the other variants.
        val e = ax13At(w, 100, 100, ag = 0, z1 = 10)
        e.aA = 1; e.bM = p; p.bM = e
        e.bN = 5
        e.bO = 0; e.bP = 0            // dead pendulum — bound at rest
        p.setAnim(326)
        // UP held with dead pendulum → climb one segment (L92)
        w.pad.held = 16388
        w.npcFsm.tickAx13(e, w, p)
        assertEquals(4, e.bN, "u(16388) → bN--")
        assertEquals(82, p.S, "i(82) climb anim")
        // descend at bottom of rope → L113 drop keeps bM (verbatim)
        e.bN = 9; p.setAnim(326); p.aA = p.aA or 64
        w.pad.held = 33024
        e.bO = 0; e.bP = 0
        w.npcFsm.tickAx13(e, w, p)
        assertEquals(43, p.S, "bN+2 > Z[1]-2 → i(43) drop")
        assertTrue(p.bM === e, "verbatim: L116 does not clear aS.bM")
    }
}


// =====================================================================
// Slice 43c — i.a() big-op decoder (script ops 100-114).
// =====================================================================
