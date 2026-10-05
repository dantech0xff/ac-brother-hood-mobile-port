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

// ---- Slice 128: bs() bp() polarity + S43 aQ==3 dismount + i.D() link sweep ----
class Slice128Test {

    open class MarkerWorld(val cell: Int = 3,
                      val cellFn: ((Int, Int) -> Int)? = null) : LevelCellSource {
        override var gc: Entity? = null
        override var vehicle: Entity? = null
        val clampCalls = mutableListOf<Entity>()
        override val cellPx = 20
        override var lockTarget: Entity? = null
        override val npcs = mutableListOf<Entity>()
        /** Slice 388: the double's "last paint" is exactly its `npcs`, so the
         *  `k.bd` neighbour scans (az, bd, bc, …) see what a test staged. */
        override val drawn: Iterable<Entity> get() = npcs
        override val kAp = IntArray(6)
        override fun kCount(slot: Int) { kAp[slot]++ }
        override fun countKill(uid: Int) { if (uid > 0) kAp[0]++ }
        override val sfxLog = mutableListOf<Int>()
        override val player = Entity(0, null)
        override var equipCount = 0
        override var actionLock = 0
        override var cEntity: Entity? = null
        override var iFlag = false
        override var cv: Entity? = null
        override var cFFlag = false
        override var playerLinkB: Entity? = null
        override var iBh = 0
        override var iBV = 0
        override var iBU = 0
        override var iBW = false
        override var iBX = 0
        override var iBT = false
        override var iBj = false
        override var iQ = false
        override var iCC = 0
        override var iCD = 0
        override var iCE = 0
        override val waypoints = WaypointPool()
        override var dirWp: WaypointNode? = null
        override var kB: Entity? = null
        override var kAi = false
        override var kR = 0
        override var kAE = 0
        override var kAH = 0
        override var kAR = 0
        override fun collisionCell(cx: Int, cy: Int): Int =
            cellFn?.invoke(cx, cy) ?: cell
        override fun isSolid(v: Int): Boolean = v >= 12
        override fun isOneWay(v: Int): Boolean = v == 3
        override fun removeEntity(e: Entity) {}
        override fun sfx(id: Int) {}
        override fun spawnWisp(src: Entity) {}
        override fun spawnPickup(anim: Int, x: Int, y: Int): Entity = Entity(14, null)
        override fun spawnProjectile(av: Boolean, x: Int, y: Int): Entity = Entity(24, null)
        override fun scrollWallClamp(e: Entity) { clampCalls += e }
    }

    @Test fun `S43 aQ==3 dismounts to i147`() {
        // g.java:1421 — feet on marker-3 with no g.c and no i.bq → i(147)
        val w = MarkerWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 43
        p.aQ = 3                                    // entity-written marker
        Entity.entBq = 0
        fsm.tick(p, Pad())
        assertEquals(147, p.S)
        assertEquals(0, Entity.entBq)
    }

    @Test fun `S43 aQ==3 below entBq dismounts`() {
        // second arm: i.bq set + al > i.bq + g.c==null → same i(147)
        val w = MarkerWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 43; p.al = 500
        p.aQ = 3
        Entity.entBq = 100
        fsm.tick(p, Pad())
        assertEquals(147, p.S)
        assertEquals(0, Entity.entBq)
    }

    @Test fun `S43 aQ!=3 keeps the cell ladder`() {
        // head a(an()) rescan recomputes aQ from cells — cell=0 leaves it 0
        val w = MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 43
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "no dismount without marker-3")
        assertEquals(1536, p.aj)
    }

    @Test fun `crate top claim sets gc on falling overlap`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = Entity(51, w.clips[7])
        e.setPositionPx(p.ak, p.al + 40); e.refreshBoxes(); e.S = 0
        w.npcs.add(e)
        // al == W[3]: below the mount threshold (L22 needs al < W[3]) but
        // inside bp()'s falling-over-top span (al <= W[3])
        p.setPositionPx(e.ak, e.al); p.S = 43; p.T = 0
        p.al = e.W[3]; p.refreshBoxes()
        p.ga = null
        p.ah = 2560                                 // falling (ah>0)
        w.gc = null
        w.npcFsm.tickPushable(e, w, p)
        assertTrue(w.gc === e, "bp() claims g.c = this (i.java:15713)")
        w.gc = null
    }

    @Test fun `P1024 latch suppresses the gc claim`() {
        val w = world(); w.npcs.clear()
        val p = w.player
        val e = Entity(51, w.clips[7])
        e.setPositionPx(p.ak, p.al + 40); e.refreshBoxes(); e.S = 0
        w.npcs.add(e)
        p.setPositionPx(e.ak, e.al); p.S = 43; p.T = 0
        p.al = e.W[3]; p.refreshBoxes()
        p.ga = null
        p.ah = 2560
        p.P = p.P or 1024
        w.gc = null
        w.npcFsm.tickPushable(e, w, p)
        assertNull(w.gc, "(aS.P&1024)!=0 keeps g.c null (i.java:15712)")
    }

    @Test fun `mission fail reload sweeps ga ac standingOn and gc`() {
        val w = world()
        val p = w.player
        val link = Entity(51, null)
        p.ga = link; p.ac = link; p.standingOn = link; w.gc = link
        repeat(20) {
            p.applyHit(18, 0, null, w)
            p.gt = 0; w.iBh = 0
        }
        tickUntilFailed(w)
        assertTrue(w.failed)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 200, 130),
                      InputQueue.Event(1, InputQueue.Type.UP, 200, 130)))
        assertFalse(w.failed)
        assertNull(p.ga); assertNull(p.ac); assertNull(p.standingOn)
        assertNull(w.gc, "i.D() link sweep on entity-system reset")
    }
}
