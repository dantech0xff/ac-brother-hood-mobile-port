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

// ---- Slice 135: S183/184/205 assassination finisher arms + victim helpers ----
class Slice135Test {
    private val clip by lazy { Clip.load(asset("clips/clip7/clip.acpk")) }

    /** MarkerWorld + tracking for the finisher payoff calls. */
    class FinWorld(cell: Int = 0) : Slice128Test.MarkerWorld(cell) {
        var statE = 0; var stat5 = 0; var streak = 0; var wisps = 0
        override fun countKill(uid: Int) { statE++ }
        override fun kCount(slot: Int) { if (slot == 5) stat5++ }
        override fun kCollectStreak() { streak++ }
        override fun spawnWisp(src: Entity) { wisps++ }
    }

    private fun playerAt(ak: Int, al: Int, av: Boolean = false): Entity {
        val p = Entity(0, clip)               // clip → r() stays false
        p.ak = ak; p.al = al; p.av = av
        p.S = 183
        return p
    }

    @Test fun `S183 drags the locked victim into S106 at 30px g3043`() {
        val w = FinWorld()
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        // weakened but alive: e()'s head drops a dead i.aN (g.javap.txt
        // e() 598-614, `aN.P()`), so the drag needs aB > 0 (slice 369)
        val v = Entity(11, null); v.S = 144; v.al = 105; v.aB = 40
        w.lockTarget = v
        fsm.tick(p, Pad())
        // converted clip7's S183 is single-frame → r() fires the same
        // tick: drag + release both run — assert the combined effects.
        assertEquals(230, v.ak, "av=false → ak+30 (:3048)")
        assertEquals(100, v.al)
        assertEquals(1, w.statE, "k.e(0,aw)")
        assertEquals(3, w.wisps, "i.aN.S() → 3× m(-1)")
        assertEquals(3, w.stat5); assertEquals(3, w.streak)
        assertEquals(0, p.ag); assertEquals(0, p.ah)
        assertEquals(0, v.aB, "release zeroed the victim")
        assertNull(w.lockTarget)
    }

    @Test fun `S183 releases the lock when the anim ends g3073`() {
        val w = FinWorld()
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)               // null clip → r() fires
        p.ak = 200; p.al = 100; p.S = 183
        val v = Entity(11, null); v.S = 106; v.al = 105; v.aB = 40
        w.lockTarget = v
        w.kAm = true
        fsm.tick(p, Pad())
        assertEquals(0, p.S, "!r()||aN==null → i(0)")
        assertEquals(0, v.aB, "victim aB=0")
        assertEquals(0, v.S, "i.d(ax11) → i(0)")
        assertNull(w.lockTarget, "i.aN = null")
        assertFalse(w.kAm, "k.p() input unlock")
    }

    @Test fun `S184 drags the victim into S107 at 35px g3099`() {
        val w = FinWorld()
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100, av = true)
        p.S = 184
        val v = Entity(11, null); v.S = 144; v.al = 105; v.aB = 40   // alive (598-614)
        w.lockTarget = v
        fsm.tick(p, Pad())
        assertEquals(107, v.S, "i.aN.i(107)")
        assertEquals(165, v.ak, "av=true → ak-35 (raw bytes @4440-4457; the port had the signs swapped)")
        assertEquals(1, w.statE); assertEquals(3, w.wisps)
    }

    @Test fun `S184 skips the drag when already in S107 g3099`() {
        val w = FinWorld()
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100); p.S = 184
        val v = Entity(11, null); v.S = 107; v.al = 105
        w.lockTarget = v
        fsm.tick(p, Pad())
        assertEquals(0, w.statE, "aN.S!=107 guard — no second tally")
    }

    @Test fun `S205 shares the arm with no victim drag g3098`() {
        val w = FinWorld()
        val fsm = PlayerFsm(w)
        // S205 exceeds converted clip7's 201 anims — null clip drives the
        // arm like the original's wider k.z[75] bank (flagged in plan).
        val p = Entity(0, null); p.ak = 200; p.al = 100; p.S = 205
        val v = Entity(11, null); v.S = 144; v.al = 105
        w.lockTarget = v
        fsm.tick(p, Pad())
        assertEquals(0, w.statE, "S==184 guard — S205 never drags")
        assertEquals(0, w.wisps)
        assertTrue(v.S != 107, "victim never i(107)")
    }

    @Test fun `releaseAnimReset maps ax11 to S0 and ax23 to S79 i1221`() {
        assertEquals(0, Entity(11, null).also { it.S = 106; it.releaseAnimReset() }.S)
        assertEquals(79, Entity(23, null).also { it.S = 106; it.releaseAnimReset() }.S)
        assertEquals(50, Entity(5, null).also { it.S = 50; it.releaseAnimReset() }.S)
    }
}
