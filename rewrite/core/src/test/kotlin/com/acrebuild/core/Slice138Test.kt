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

/** Slice 138 — dismount/leap family: S54 settle, S357 scripted leap,
 *  S360 perch, S370 boss-grab windup, S374 KO-settle, `i.E()` sink. */
class Slice138Test {

    private val clip0 by lazy { Clip.load(asset("clips/clip0/clip.acpk")) }

    private fun mk(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al; p.av = av
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    @Test fun `S54 anim end drops 20 settles and idles g2223`() {
        val w = Slice134Test.PassWorld(cell = 3)   // aR=3 → E() finds footing
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 54
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "i(0) after the settle")
        assertEquals(80, p.al, "al -= 20, E() finds footing at aR==3")
    }

    @Test fun `S357 leap speed entries g4155`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 357
        p.clip = clip0                 // real clip + T=0 → !r()
        p.T = 0; p.U = 0
        fsm.tick(p, Pad())
        assertEquals(3328, p.ag, "facing-right → ag=3328")
        assertEquals(357, p.S, "still leaping")
        val q = mk(200, 100, av = true); q.S = 357
        q.clip = clip0; q.T = 0; q.U = 0
        fsm.tick(q, Pad())
        assertEquals(-1280, q.ag, "av → ag=-1280 (asymmetric, verbatim)")
    }

    @Test fun `S357 anim end K4 i364 then ar-folds g4159`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 357
        fsm.tick(p, Pad())                    // null clip → r()
        assertEquals(0, p.ag, "anim end → ag=0")
        assertEquals(4, p.K, "K=4")
        // i(364) then ar() — g==null → ar() i(0)'s (verbatim: the S!=364
        // guard only keeps 364 when an interact anchor is live).
        assertEquals(0, p.S, "i(364) + ar() no-anchor → i(0)")
    }

    @Test fun `S360 perch forward press relaunches g4174`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100, av = false); p.S = 360
        p.clip = clip0; p.T = 0; p.U = 0   // mid-anim → r() false
        val pad = Pad(); pad.queuePress(Pad.M_RIGHT); pad.commit(0)
        fsm.tick(p, pad)
        assertNull(p.ae, "forward press → G() releases the marker")
        assertEquals(357, p.S, "i(357) relaunch")
    }

    @Test fun `S360 perch marker spawn then anim end idles g4178`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100, av = false); p.S = 360
        // mid-tick: spawnMarker runs before the r() check — ae is set,
        // then r() → G() clears it in the same pass.
        fsm.tick(p, Pad())
        assertNull(p.ae, "r() → G() releases the just-spawned marker")
        assertEquals(0, p.S, "r() → i(0)")
    }

    @Test fun `S370 windup advances to S371 g4185`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 370
        fsm.tick(p, Pad())
        assertEquals(371, p.S, "r() → i(371)")
    }

    @Test fun `S374 with meter recovers to S376 g4211`() {
        val w = Slice134Test.PassWorld(cell = 0)
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 374; p.x1 = 90
        p.ag = 999; p.ah = 999
        fsm.tick(p, Pad())
        assertEquals(376, p.S, "x[1]>0 → i(376)")
        assertEquals(0, p.ag); assertEquals(0, p.ah)
    }

    @Test fun `S374 without meter mission-fails g4216`() {
        val w = object : Slice128Test.MarkerWorld(cell = 0) {
            var lCalls = 0
            override fun screenL(n: Int) { if (n == 12) lCalls++ }
        }
        val fsm = PlayerFsm(w)
        val p = mk(200, 100); p.S = 374; p.x1 = 0
        fsm.tick(p, Pad())
        assertEquals(1, w.lCalls, "x[1]==0 → k.l(12)")
        assertEquals(374, p.S, "state holds")
    }
}
