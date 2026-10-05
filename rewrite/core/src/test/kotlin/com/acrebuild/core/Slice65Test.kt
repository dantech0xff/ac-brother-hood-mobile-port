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
// Slice 65 — ax64 bl() grabber/harrier FSM (i.java:15391-15897, proven).
// Marker-as-clip anim states {42,48,60,66}, pooled k.aX tether/barrage
// shots, waypoint crawl + dive + grab-hold with mash escape.
// ============================================================================
class Slice65Test {
    private fun ax64At(w: Level0World, x: Int, y: Int, anim: Int,
                       z: List<Int> = List(10) { 0 }): Entity {
        val e = Entity(64, w.clips[6])       // bi[64]=-1 → clipless records
        e.aw = 700 + w.npcs.size
        val rec = mutableListOf(64, e.aw, x, y, 0, anim, 0)
        rec += z
        while (rec.size < 17) rec += 0
        e.setPositionPx(x, y)
        w.npcFsm.initAx64(e, rec)
        w.npcs.add(e)
        return e
    }
    private fun wp(w: Level0World, id: Int, x: Int, y: Int, cFlag: Int = 0) {
        w.waypoints.add(intArrayOf(0, id, x, y, cFlag, 0, 0, 0, -1))
    }

    @Test fun `init — Z0 to Z9 from record, Z1+=7, aC=Z8, S=record anim`() {
        val w = world()
        val e = ax64At(w, 100, 100, anim = 0,
            z = listOf(1, 10, 0, 0, 0, 0, 0, 0, 40, 9))
        assertEquals(1, e.Z[0]); assertEquals(17, e.Z[1], "Z1 = r8[8] + 7")
        assertEquals(40, e.aC); assertEquals(0, e.S); assertEquals(301, e.az)
    }

    @Test fun `head arm only runs in S7`() {
        val w = world()
        val e = ax64At(w, 100, 100, anim = 0,
            z = listOf(0, 0, 0, 0, 0, 0, 0, 0, 5, 0))
        w.player.setPositionPx(900, 400)
        w.npcFsm.tickAx64(e, w, w.player)       // S0 → waypoint path, no Z8--
        assertEquals(5, e.Z[8], "Z[8] untouched outside S7")
    }

    // `aS.a(n,x,y)` (`a(III)V` @0-82) never `k.b`-inserts the marker
    // (slice 371) — it only exists as `aS.ae`, so a same-tick spawn +
    // release is observed at the `spawnPickup` call, not the insert
    // buffer.
    private class SpawnSpy(private val inner: Level0World) : LevelCellSource by inner {
        val spawned = ArrayList<Entity>()
        override fun spawnPickup(anim: Int, x: Int, y: Int): Entity =
            inner.spawnPickup(anim, x, y).also { spawned += it }
    }

    @Test fun `stalk — below-right arms, picks S66 and keeps it bound`() {
        val w = world()
        val e = ax64At(w, 260, 150, anim = 7)   // stalk head runs only in S7
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)                     // vulnerable anim set
        w.pad.commit(0)
        val spy = SpawnSpy(w)
        w.npcFsm.tickAx64(e, spy, w.player)
        // @428-509: right+below → anim 66 + bl=512; @744 `goto 816` SKIPS
        // the @747 retract tail on the armed path — the marker stays
        // bound (slice 420: the pre-audit port released it same tick).
        assertTrue(spy.spawned.any { it.ax == 14 && it.S == 66 },
            "S66 marker spawned")
        assertEquals(512, e.bl, "bl latch mask for anim 66")
        assertEquals(66, w.player.ae?.S, "armed marker stays bound to p.ae")
        assertEquals(e.al - 20, w.player.ae?.al,
            "@715 pin: ae.al = e.al - 20")
    }

    @Test fun `stalk — below-left picks S60 and keeps it bound`() {
        val w = world()
        val e = ax64At(w, 160, 150, anim = 7)   // stalk head runs only in S7
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        w.pad.commit(0)
        val spy = SpawnSpy(w)
        w.npcFsm.tickAx64(e, spy, w.player)
        assertTrue(spy.spawned.any { it.ax == 14 && it.S == 60 },
            "S60 marker spawned")
        assertEquals(128, e.bl)
        assertEquals(60, w.player.ae?.S, "armed marker stays bound to p.ae")
        assertEquals(e.al - 20, w.player.ae?.al,
            "@715 pin: ae.al = e.al - 20")
    }

    @Test fun `S0 — waypoint crawl dominant axis at pace Z1 shl 8`() {
        val w = world()
        val e = ax64At(w, 100, 100, anim = 0,
            z = listOf(0, 0, 500))              // Z[2] = waypoint uid
        wp(w, 500, 300, 100)
        w.player.setPositionPx(900, 900)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        // Z[1] = (f&127) - kX = 0 - (-7) = 7 → pace 7<<8 = 1792, x-dominant
        assertEquals(7 shl 8, e.ag)
        assertEquals(0, e.ah, "|wdy|=0 < pace → ah=0")
    }

    @Test fun `S0 — waypoint arrive reads next node cFlag and hops to S1`() {
        val w = world()
        val e = ax64At(w, 100, 100, anim = 0,
            z = listOf(0, 0, 500, 501))         // Z[2]=current, Z[3]=next
        wp(w, 500, 100, 100)                    // inside this square now
        wp(w, 501, 200, 200, cFlag = 1)         // hop flag on the NEXT node
        w.player.setPositionPx(900, 900)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(1, e.S); assertEquals(1, e.cy)
    }

    @Test fun `S0 — aA=4 dives toward the player`() {
        val w = world()
        val e = ax64At(w, 400, 400, anim = 0)
        e.aA = 4
        w.player.setPositionPx(200, 100)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(1, e.S); assertEquals(0, e.cy)
        assertEquals(300, e.aq, "ak>p.ak → aq = max(ak-100, p.ak) = 300")
        // ar = max(al-190, p.al)=210 → +32=242; ady=|400-242|=158;
        // cz = ceil(158/5)=32 → ar = 242 + 32*(-7) = 18
        assertEquals(18, e.ar)
    }

    @Test fun `S1 — W overlap grabs the player into S2`() {
        val w = world()
        val e = ax64At(w, 200, 100, anim = 1)
        e.W[0] = 190; e.W[1] = 90; e.W[2] = 220; e.W[3] = 120
        w.player.setPositionPx(200, 100)
        w.player.W[0] = 190; w.player.W[1] = 90
        w.player.W[2] = 220; w.player.W[3] = 120
        w.player.setAnim(0)
        w.pad.commit(0)
        e.aq = -1; e.ar = -1                    // target cleared → fly arm skipped
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(2, e.S); assertEquals(30, e.aC)
        assertEquals(200, e.ak); assertEquals(100, e.al, "snap onto player")
    }

    @Test fun `S2 — hold ticks down, pins player to S15, sets iBi`() {
        val w = world()
        val e = ax64At(w, 200, 100, anim = 2)
        e.aC = 10
        w.player.setPositionPx(200, 100)
        w.player.setAnim(0)
        w.player.ae = Entity(14, w.clips[9]).apply { setAnim(0) }
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(9, e.aC)
        assertEquals(15, w.player.S)
        assertTrue(w.iBi)
        assertNotNull(w.player.ae, "marker respawned at view centre")
        assertEquals(w.kO + 200, w.player.ae!!.ak)
        assertEquals(w.kP + 120, w.player.ae!!.al)
    }

    @Test fun `S2 — expiry drains the player and releases into S4 or S5`() {
        val w = world()
        val e = ax64At(w, 200, 100, anim = 2, z = listOf(0,0,0,0,0,0,0,0,0,0))
        e.aC = 0                                // expired
        w.player.x1 = 50
        w.player.setPositionPx(200, 100)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(4, e.S, "Z[6]==0 → i(4)")
        assertFalse(w.iBi)
    }

    @Test fun `S7 — stalk budget and despawn when v() fails`() {
        val w = world()
        w.npcFsm.initAx24(Entity(24, null), listOf(0), w)   // seed k.aX pool
        val e = ax64At(w, 100, 100, anim = 7,
            z = listOf(0, 0, 0, 0, 0, 0, 0, 0, 3, 0))
        e.aC = 1                                // barrage due
        w.player.setPositionPx(900, 900)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertEquals(2, e.Z[8], "Z[8]-- per S7 tick")
        assertTrue(w.pooledShots!!.any { it!!.P and 128 == 0 && it.P and 16 != 0 },
            "aC<=0 → barrage arms a pooled slot (i.java:24510)")
    }

    @Test fun `S7 — alive-check off view despawns via k_c`() {
        val w = world()
        val e = ax64At(w, 9000, 9000, anim = 7,
            z = listOf(0, 0, 0, 0, 0, 0, 0, 0, 3, 0))
        w.player.setPositionPx(100, 100)
        w.pad.commit(0)
        w.npcFsm.tickAx64(e, w, w.player)
        assertTrue(w.pendingRemove.contains(e),
            "au limit + off-camera → k.c(this)")
    }
}


// ============================================================= Slice 66 —
// ax74 `bN()` wisp/collectible FSM (i.java:21280, proven). Covers: L116 init
// (P|512, az=f[7], k.aq++ on anim-0), S0 collect (overlap-or-dist≤20, ap[5] +
// streak + sfx + S2), S1 polar spiral (aF param, aq/ar anchor, orbit ticks →
// S2), S2 attach anim → k.c, S5 fall→bezier setup (+dead RNG draw), S3/S6
// quadratic-bezier view-space flight → S4, S4 → k.c. Helpers Trig.bezier +
// kCount/kCollectStreak/kAq/kAz on Level0World.
