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

/**
 * Slice 191 — `i.f(this)` scroll-wall clamp call sites completed:
 * the remaining 10 of 17 g.java sites now invoke `scrollWallClamp`
 * (S32 head :1895; l() S79 facing-branches :5172/:5182; ax() tail :5319;
 * case199 dir-held :3153/:3160; preJumpArm tail :1740; airFamily
 * !y() block :1600; fallArm head :1406). S8 (:1242), S9/10 (:1293),
 * S375-377 (:4253-4309) and ay() (:5421) were already wired.
 * The 3 cv/aF-bound climb sites (:1614/:1645 family) sit in branches
 * the port does not carry — those climb states have their own arms.
 */
class Slice191Test {

    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S32 arm calls the clamp at the head`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 32
        fsm.tick(p, Pad())
        assertTrue(p in w.clampCalls, "i.f(this) at S32 head — g.java:1895")
    }

    @Test fun `fallArm calls the clamp at the head`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 43
        fsm.tick(p, Pad())
        assertTrue(p in w.clampCalls, "i.f(this) at fallArm head — g.java:1406")
    }

    @Test fun `ax tail calls the clamp on the sustained run`() {
        // feet cell 12 → aZ=true so the sustained-run path stays open;
        // S12 + dir-held already-facing → ax() → clamp tail.
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 1; p.av = false
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertTrue(p in w.clampCalls, "i.f(this) at ax() tail — g.java:5319")
    }

    @Test fun `l S79 facing branch calls the clamp`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 79; p.av = false
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertTrue(p in w.clampCalls, "i.f(this) in l() S79 — g.java:5182")
    }

    @Test fun `case199 dir-held branches call the clamp`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 })
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 199
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertTrue(p in w.clampCalls, "i.f(this) in case199 — g.java:3160")
    }

    @Test fun `preJumpArm calls the clamp at the tail`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 233
        fsm.tick(p, Pad())
        assertTrue(p in w.clampCalls, "i.f(this) at S233 tail — g.java:1740")
    }

    @Test fun `airFamily free-air path calls the clamp`() {
        val w = Slice128Test.MarkerWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 22; p.ah = -100
        fsm.tick(p, Pad())
        assertTrue(p in w.clampCalls, "i.f(this) in !y() block — g.java:1600")
    }

    @Test fun `wall pin stops a run at the bound edge`() {
        // verbatim i.f math (i.java:5382): mask&2 → right-side pin —
        // ag=ai=0 and ak snapped to the bound. Level0World impl is
        // covered by the mode1 test; here we re-stage it on a MarkerWorld
        // override to prove the ax() tail actually gates real motion.
        val wallX = 240
        val w = object : Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 5) 12 else 0 }) {
            override fun scrollWallClamp(e: Entity) {
                super.scrollWallClamp(e)
                // i.f verbatim, right side only (mask 2), bound X[2]=wallX
                if (e.ag >= 0 && (e.Y[2] shl 8) + e.ag >= (wallX shl 8)) {
                    e.ag = 0; e.ai = 0
                    e.ak = (e.ak - e.Y[2]) + wallX
                }
            }
        }
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 1; p.av = false
        // i.f reads world-space Y box coords: Y[2]=235 + ag=2560 (10px)
        // crosses wallX=240 this tick → pin + snap ak=(200-235)+240=205.
        p.Y[2] = 235
        val pad = Pad(); pad.held = Pad.M_RIGHT
        fsm.tick(p, pad)
        assertEquals(0, p.ag, "right-wall pin zeroes the run speed")
        assertEquals(205, p.ak, "ak snapped so Y[2] lands on the wall")
    }
}
