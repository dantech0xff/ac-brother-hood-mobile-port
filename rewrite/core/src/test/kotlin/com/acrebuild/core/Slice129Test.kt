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

// ---- Slice 129: ay() attack-step + i.f scroll-wall clamp + combo-case tail ----
class Slice129Test {

    @Test fun `attackStep advances 1792 on flagged combo tick g5363`() {
        // ay() S67: T==1 + V<=0 → ag=+1792 facing right (av=false).
        // Real clip mid-anim: with r() true the combo end runs `l()`
        // (g.javap.txt e() 10598, slice 369), whose aw() zeroes ag.
        val w = Slice128Test.MarkerWorld(cell = 20)
        val fsm = PlayerFsm(w)
        val p = Entity(0, Clip.load(asset("clips/clip0/clip.acpk")))
        p.setAnim(67); p.T = 1; p.U = 0; p.av = false; p.aZ = true
        p.ak = 80; p.al = 100; p.refreshBoxes()
        assertFalse(p.animFinished())
        fsm.tick(p, Pad())
        assertEquals(1792, p.ag, "ay() attack step on T==1 (g.java:5383)")
        assertTrue(p in w.clampCalls, "i.f(this) scroll-wall tail called")
    }

    @Test fun `attackStep ledge guard never steps off edge g5363`() {
        // cells are marker-3 (not 20/5) → z2 false → ai=ag=0, no step
        val w = Slice128Test.MarkerWorld(cell = 3)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 67; p.T = 1; p.av = false; p.aZ = true
        p.W[0] = 0; p.W[2] = 40; p.W[3] = 100
        fsm.tick(p, Pad())
        assertEquals(0, p.ag, "ledge guard kills the step (g.java:5396)")
        assertEquals(0, p.ai)
    }

    @Test fun `attackStep clamps at the lock target edge g5363`() {
        // aN ahead on the right: the +2 approach check trips when
        // ak + (ag>>8) + (W2-ak) + 2 = W2 + 9 reaches aN.W[0]. Real clip
        // mid-anim (r() false) and a live aN — e()'s head drops a dead
        // one (598-614), and r() would end the combo through l() (slice 369).
        val w = Slice128Test.MarkerWorld(cell = 20)
        val fsm = PlayerFsm(w)
        val p = Entity(0, Clip.load(asset("clips/clip0/clip.acpk")))
        p.setAnim(67); p.T = 1; p.U = 0; p.av = false; p.aZ = true
        p.ak = 80; p.al = 100; p.refreshBoxes()
        val t = Entity(11, null); t.aB = 50
        t.W[0] = p.W[2] + 9; t.W[2] = t.W[0] + 35; t.ak = 120
        w.lockTarget = t
        fsm.tick(p, Pad())
        assertEquals(0, p.ag,
            "ak+ag>>8+(W2-ak)+2 = W2+9 >= aN.W[0] → clamped")
    }

    @Test fun `comboArm falls through when footing lost g2469`() {
        // !aZ && a==null after ay() → a(0) — enterFall before combo logic.
        // aZ comes out of the head rescan: only aR==18 keeps it false.
        val w = Slice128Test.MarkerWorld(cell = 18)
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.S = 67; p.T = 1; p.av = false
        p.standingOn = null
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "lost footing → a(0) enterFall (g.java:2473)")
    }

    @Test fun `scrollWallClamp pins the player inside mode1 bounds i5382`() {
        // level-0 ax37 @1627,669: zone (1577,619)-(1877,799),
        // bound (1577,619)-(1927,799), mask=1 → left wall at x=1577.
        val w = world()
        val p = w.player
        p.setPositionPx(1700, 700); p.refreshBoxes()
        w.tick(emptyList())                        // zone overlap → claim
        p.ag = -5120                               // -20px/tick toward wall
        p.Y[0] = 1590; p.Y[1] = 660; p.Y[2] = 1610; p.Y[3] = 700
        w.scrollWallClamp(p)
        assertEquals(0, p.ag)
        assertEquals(0, p.ai)
        assertEquals(1687, p.ak, "ak = (1700-1590)+1577 pinned to bound")
    }
}
