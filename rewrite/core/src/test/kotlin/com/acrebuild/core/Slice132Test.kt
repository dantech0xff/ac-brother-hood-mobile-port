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

// ---- Slice 132: i.A()/i.z()/g.ak() helpers + S12/S33/S34 ledge/ladder ----
class Slice132Test {

    private fun playerAt(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null)
        p.ak = ak; p.al = al; p.av = av
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `ladderCell fires on facing column cell 21 i940`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 11 && cy == 5) 21 else 0 })
        val p = playerAt(200, 100)               // W[2]=210 → col 11; rows 4..5
        assertTrue(p.ladderCell(w), "A() — cell 21 in facing column (i.java:940)")
        val pLeft = playerAt(200, 100, av = true)
        assertFalse(pLeft.ladderCell(w), "av=true probes the left column")
    }

    @Test fun `pushColumnBlocked needs the whole column ≥19 i928`() {
        val w = Slice128Test.MarkerWorld(cell = 20)
        val p = playerAt(200, 100); p.ag = 2560
        assertTrue(p.pushColumnBlocked(w), "z() — all side cells 20 (i.java:928)")
        val w2 = Slice128Test.MarkerWorld(cell = 0)
        assertFalse(p.pushColumnBlocked(w2))
    }

    @Test fun `ledgeLipGrab snaps to the lip and enters S60 g187`() {
        // av=false: i4=(W[1]+10)/20=4, i2=(W[2]+5)/20=10, i3=9.
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 20 else 0 })
        val p = playerAt(200, 100)
        assertTrue(p.ledgeLipGrab(w), "ak() — lip pocket clear (g.java:187)")
        assertEquals(200, p.ak, "ak = i2*20")
        assertEquals(79, p.al, "al = i4*20 - 1")
        assertEquals(60, p.S, "i(60) hang")
    }

    @Test fun `ledgeLipGrab rejects a dirty pocket g187`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 20 else if (cx == 9 && cy == 5) 5 else 0 })
        val p = playerAt(200, 100)
        assertFalse(p.ledgeLipGrab(w), "pocket cell (9,5) non-empty → no grab")
        assertNotEquals(60, p.S)
    }

    @Test fun `ledgeLipGrab is blocked while Q61 g187`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 20 else 0 })
        val p = playerAt(200, 100); p.Q = 61
        assertFalse(p.ledgeLipGrab(w), "Q==61 blocks outright")
    }

    @Test fun `S12 ladder snap enters S74 g1322`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 11 && cy == 4) 21 else 0 })
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 12; p.ag = 512; p.aO = 0
        fsm.tick(p, Pad())
        assertEquals(74, p.S, "A() → i(74) ladder mount (g.java:1322)")
        assertEquals(p.W[1], p.al, "al = W[1] snap")
        assertEquals(220, p.ak, "ak += 20 facing right")
    }

    @Test fun `S12 wall face falls to the S33 rebound g1342`() {
        // head rescan recomputes the strip fields — stage real geometry:
        // right strip col 10 solid from row 3 down (aU=20, aY=9-3+1=7), col
        // 11 solid for z()'s push column; head-row cells 0 → aO==0 gate.
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 11 || (cx == 10 && cy >= 3)) 20 else 0 })
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.W[1] = 0; p.W[3] = 200               // deep strip → aY==7
        p.S = 12; p.ag = 512; p.co = 3
        fsm.tick(p, Pad())
        assertEquals(33, p.S, "co>2 && z() && !ak() → i(33) (g.java:1350)")
        assertEquals(-4096, p.ah)
        assertEquals(0, p.ag)
    }

    @Test fun `S33 direction press vaults into S34 g1956`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 33; p.aR = 0; p.aS = 0
        val pad = Pad(); pad.commit(8256)      // direction toward facing
        fsm.tick(p, pad)
        assertEquals(34, p.S, "dir press → a(34,36) rise (g.java:1965)")
        assertEquals(1536, p.ah)
    }

    @Test fun `S33 direction press on cell 5 falls instead g1956`() {
        val w = Slice128Test.MarkerWorld(cell = 5)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 33                               // probeCells → aR = 5
        val pad = Pad(); pad.commit(8256)
        fsm.tick(p, pad)
        assertEquals(43, p.S, "aR∈{5,20} → a(43,32) (g.java:1961)")
    }

    @Test fun `S34 ticks aA and settles when side cell not 20 g2018`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 34
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "aU!=20 → a(0) → enterFall (g.java:2021)")
    }
}
