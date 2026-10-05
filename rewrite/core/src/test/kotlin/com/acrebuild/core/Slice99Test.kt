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

// ============================================================ slice 99
// ax10-S16 door-teleport: S16 arm (i.java:12326-12431) + bh()/bi()
// (i.java:14421/:14437) + i.a(iVar) bind (i.java:231). All proven.
class Slice99Test {
    /** Paired S16 doors: `door` W wraps the player, `dest` (aw=77) is
     *  the oId/Z[0] link target. `door.aD=1` so bi() faces right. */
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

    @Test fun `door overlap marks the player with the 105 prompt`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.g = null; p.ga = null
        val (door, _) = doorPair(w)
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        // slice 397 (i.javap aV() @6127-6149): `this.a(105,…)` — the DOOR owns it
        assertNotNull(door.ae, "this.a(105,…) spawned into the door's ae")
        assertEquals(105, door.ae!!.S, "marker anim 105")
        assertNull(p.ae, "the player's own marker slot stays free")
        assertEquals(300, door.az, "az=300 marker TTL (L1784)")
        assertNull(p.ac, "no bind without the keypress")
    }

    @Test fun `door leave drops the door ae link`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.g = null; p.ga = null
        val (door, _) = doorPair(w)
        door.ae = Entity(14, null)                    // door-side ae link
        p.ak = door.W[2] + 200; p.refreshBoxes()       // out of the zone
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertNull(door.ae, "L1850 G() on no-overlap")
    }

    @Test fun `door exit tap binds player to dest and fades in`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.g = null; p.ga = null
        val (door, dest) = doorPair(w)
        w.pad.commit(16388)
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertSame(dest, p.ac, "bh() k.q(o) → aS.a(dest)")
        assertTrue(dest.P and 256 != 0, "bind marks P|=256")
        assertEquals((door.W[0] + door.W[2]) shr 1, p.ak,
            "player centered on exit door (bh :14428)")
        assertEquals(0, p.ag + p.ah + p.ai + p.aj, "velocity zeroed")
        assertEquals(284, p.S, "aS.i(284)")
        assertTrue(w.kAn && !w.kAo, "k.B(26) fade-in armed")
        assertEquals(0, w.kBI, "bI=0 at fade-in start")
    }

    @Test fun `door exit blocked by live interact target g`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.ga = null
        p.g = Entity(14, null)                          // g.g != null
        val (door, _) = doorPair(w)
        w.pad.commit(16388)
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertNull(p.ac, "L1850 → no bind while g.g lives")
        assertFalse(w.kAn, "no fade")
    }

    @Test fun `door exit blocked while grapple link lives`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.g = null
        p.ga = Entity(43, null)                          // g.a != null
        val (door, _) = doorPair(w)
        door.az = -5
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertEquals(-5, door.az, "L177d early return skips az=300")
        assertNull(p.ae)
    }

    @Test fun `door arrival places player at dest and fades out`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        val (door, dest) = doorPair(w)
        p.ac = dest                                       // bound to dest
        w.kAn = false; w.kAo = false                      // fades idle
        w.npcFsm.tickTrigger(dest, w, p, w.pad)
        assertEquals((dest.W[0] + dest.W[2]) shr 1, p.ak, "bottom-center x")
        assertEquals(dest.W[3], p.al, "al = W[3]")
        assertTrue(p.av, "av = (aD&1)!=0 → right")
        assertEquals(285, p.S, "aS.i(285) arrive anim")
        assertTrue(w.kAo && !w.kAn, "k.C(26) fade-out armed")
        assertEquals(255, w.kBI, "bI=255 at fade-out start")
        assertEquals(300, dest.az, "az=300 still armed each tick")
    }

    @Test fun `door bound mid-fade-out flings player and opens dest`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        val (door, dest) = doorPair(w)
        p.bindAc(door)                                    // bound to EXIT (P|256)
        assertEquals(256, door.P and 256, "fixture: bind mark set")
        w.kAo = true; w.kBI = 20                          // ao && bI>13
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertEquals(19, dest.S, "r8.i(19) dest open anim")
        // slice 397 (@6088-6092): `aS.a((i) null)` is the entity UNBIND, not the
        // int-overload fling the old test pinned
        assertNull(p.ac, "aS.a(null) unbinds")
        assertEquals(0, door.P and 256, "…and releases the door's P|256 bind mark")
        assertTrue(p.S != 43, "no S43 fling")
    }

    @Test fun `door unbound mid-fade-in opens dest`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = false; p.g = null; p.ga = null
        val (door, dest) = doorPair(w)
        w.kAn = true; w.kBI = 20                          // an && bI>13
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertEquals(17, dest.S, "L1837 r8.i(17)")
        assertNull(p.ac, "no bind without the keypress")
    }

    @Test fun `door without o link does nothing unbound`() {
        val w = world(); w.npcs.clear(); w.stateL(8)
        val p = w.player
        p.refreshBoxes(); p.aZ = true; p.g = null; p.ga = null
        val (door, _) = doorPair(w)
        door.oId = -1
        w.pad.commit(16388)
        w.npcFsm.tickTrigger(door, w, p, w.pad)
        assertNull(p.ae, "o==-1 → L17d0 early return")
    }
}
