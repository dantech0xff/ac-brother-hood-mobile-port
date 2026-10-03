package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Slice 368 — soft-key parity and the BACK key.
 *
 * - `b(false)` draws and hit-tests a SKIP pill over a skippable claim:
 *   `C != null && (C.ab() || u == 9) && C.cd[2]` → `a("", d(0,18))`
 *   (k.java:3163-3165; bytecode k.javap.txt b(Z) 5604-5650). Its
 *   `E(131072)` feeds `i.aa()`'s skip latch in play and the `:944` gate on
 *   the u==9 dialog. The port drew it but only hit-tested a stand-in rect
 *   on jC 21 at press time.
 * - `j(x,y)` excludes the soft-key margins with the `ce/cf` statics that
 *   `a(str,str2)` last wrote (k.java:581, :147-148, :2271-2298); the port
 *   used constants 60/60.
 * - The pause icon is a release hit-test in `J()` after `I()` (k.java:
 *   1040-1063); `pointerPressed` arms only the wheel (k.java:486-494). The
 *   port paused on the press, before the world ran the frame.
 * - BACK: `k.keyPressed/keyReleased` are bare `return`s (k.javap.txt:
 *   26544-26556). The port maps BACK onto the right soft-key pill.
 */
class Slice368Test {
    private fun ev(type: InputQueue.Type, x: Int = -1, y: Int = -1, seq: Long = 0) =
        InputQueue.Event(seq, type, x, y)

    private fun tap(w: Level0World, x: Int, y: Int) =
        w.tick(listOf(ev(InputQueue.Type.DOWN, x, y, 0), ev(InputQueue.Type.UP, x, y, 1)))

    private fun back(w: Level0World) = w.tick(listOf(ev(InputQueue.Type.BACK)))

    /** Center of the right pill's hit rect `(395-cf-10, 198, cf+20, 47)`. */
    private fun rightPill(w: Level0World, label: String): Pair<Int, Int> {
        val cf = w.footerRightDim(label)
        return (395 - cf - 10) + (cf + 20) / 2 to 198 + 47 / 2
    }

    private fun skippableClaim(paused: Boolean = false) = Entity(0, null).apply {
        ca = 0; scriptStep = 0
        cd[0] = paused
        cd[2] = true
    }

    private fun armed(w: Level0World, mask: Int) = (w.pad.eK and mask) != 0

    /** Mission 0 opens on a skippable claim: the ax5 (uid 299) binds `k.C`
     *  on the first frame with `cd[2]` set, so the SKIP pill shows. BACK
     *  skips it: the next frame's `i.aa()` latches `cd[1]` and the
     *  fast-forward runs the script out. */
    private fun afterIntro(): Level0World {
        val w = world()
        w.tick(emptyList())
        back(w)
        w.tick(emptyList())
        assertEquals(null, w.kC, "precondition: the intro claim ran out")
        return w
    }

    // -- claim SKIP pill ---------------------------------------------------

    @Test fun `mission 0 intro — the SKIP pill fast-forwards the opening claim`() {
        val w = world()
        w.tick(emptyList())
        val c = w.kC!!
        assertTrue(c.claimAb() && c.cd[2], "the intro claim is skippable")
        val (x, y) = rightPill(w, w.d0(18)!!)
        tap(w, x, y)
        assertTrue(armed(w, Pad.M_CYCLE))
        w.tick(emptyList())
        assertTrue(c.cd[1], "i.aa(): cd[2] && v(131072) → cd[1]")
        assertEquals(null, w.kC, "the cd[1] fast-forward ran the script out")

        val idle = world()
        repeat(4) { idle.tick(emptyList()) }
        assertTrue(idle.kC === idle.npcs.firstOrNull { it.ax == 5 && it.aw == 299 },
                   "untouched, the intro claim is still bound")
    }

    @Test fun `play — a release on the SKIP pill arms 131072 over a skippable claim`() {
        val w = afterIntro()
        w.kC = skippableClaim()
        val (x, y) = rightPill(w, w.d0(18)!!)
        tap(w, x, y)
        assertTrue(armed(w, Pad.M_CYCLE), "a(\"\",d(0,18)) → c(rect) → E(131072)")
        assertEquals(-1, w.kCe, "a() resets ce; the empty left label keeps -1")
        assertEquals(w.footerRightDim(w.d0(18)!!), w.kCf, "cf = d(0,18) width + 30")
        w.tick(emptyList())
        assertTrue(w.pad.v(Pad.M_CYCLE), "the edge is in bB for the claimer's i.aa()")
    }

    @Test fun `play — no pill without a skippable claim`() {
        val w = afterIntro()
        val cf = w.kCf
        val (x, y) = rightPill(w, w.d0(18)!!)
        tap(w, x, y)
        assertFalse(armed(w, Pad.M_CYCLE), "no a() call → no right pill")
        assertEquals(cf, w.kCf, "ce/cf keep their last value when a() is not called")
        w.kC = skippableClaim().apply { cd[2] = false }
        tap(w, x, y)
        assertFalse(armed(w, Pad.M_CYCLE), "cd[2] false → no pill")
    }

    @Test fun `play — a paused claim shows the pill only while u == 9`() {
        val w = afterIntro()
        w.kC = skippableClaim(paused = true)              // ab() false
        val (x, y) = rightPill(w, w.d0(18)!!)
        w.dlgU = 0
        tap(w, x, y)
        assertFalse(armed(w, Pad.M_CYCLE), "!ab() && u != 9 → no pill")
        w.dlgU = 9                                        // u persists after a dialog
        tap(w, x, y)
        assertTrue(armed(w, Pad.M_CYCLE), "(C.ab() || u == 9) && cd[2]")
    }

    @Test fun `play — BACK over a skippable claim is the SKIP pill`() {
        val w = afterIntro()
        w.kC = skippableClaim()
        back(w)
        assertTrue(armed(w, Pad.M_CYCLE))
        w.tick(emptyList())
        assertTrue(w.pad.v(Pad.M_CYCLE))
    }

    @Test fun `play — BACK with no right pill does nothing`() {
        val w = afterIntro()
        val weapon = w.player.gI
        back(w)
        assertFalse(armed(w, Pad.M_CYCLE), "no soft key → BACK is inert")
        w.tick(emptyList())
        assertFalse(w.pad.v(Pad.M_CYCLE))
        assertEquals(8, w.jC)
        assertEquals(weapon, w.player.gI, "no g.ao() weapon cycle from BACK")
        assertEquals(0, w.actionLock, "k.at untouched")
    }

    private fun u9Dialog(): Pair<Level0World, Entity> {
        val w = world()
        w.autoDismissDialog = false
        w.stateL(21)
        w.dlgU = 9; w.dlgV = 0; w.dlgW = 2
        listOf("p1", "p2", "p3").forEachIndexed { i, s -> w.dlgBM[i] = s }
        w.dlgBT = -1
        val c = Entity(5, null).apply { cd[2] = true; cd[0] = true }
        w.kC = c
        return w to c
    }

    @Test fun `u9 dialog — BACK skips the claim like the SKIP pill`() {
        val (w, c) = u9Dialog()
        back(w)
        assertEquals(21, w.jC, "release frame: E(131072) armed")
        w.tick(emptyList())
        assertEquals(8, w.jC, "v(131072) && u==9 && C.cd[2] → C.Z(); l(8)")
        assertTrue(c.cd[1])
        assertFalse(c.cd[0])
    }

    @Test fun `u9 dialog — the left edge of the pill is j()'s, not the pill's`() {
        // ce=-1, cf=36: a release at x <= 400-cf with 203 < y < 240 makes
        // j() true, and its E(65568) replaces the pill's E(131072).
        val (w, _) = u9Dialog()
        tap(w, 355, 220)
        assertFalse(armed(w, Pad.M_CYCLE), "E(65568) after E(131072) wins")
        assertTrue(armed(w, Pad.M_CONTEXT))
        w.tick(emptyList())
        assertEquals(21, w.jC, "a context press pages the dialog instead")
    }

    // -- j(x,y) margins ----------------------------------------------------

    @Test fun `wheel margins follow the widths a() last wrote`() {
        val w = world()
        w.cm = 0                                          // the player-relative wheel
        assertEquals(-1, w.resolvePadZone(50, 220), "init ce=60")
        assertEquals(-1, w.resolvePadZone(350, 220), "init cf=60")
        w.stateL(4)                                       // F(): a("", d(0,17))
        w.tick(emptyList())
        assertEquals(-1, w.kCe)
        assertEquals(36, w.kCf)
        w.stateL(8); w.cm = 0
        assertNotEquals(-1, w.resolvePadZone(50, 220), "ce=-1: no left margin")
        assertNotEquals(-1, w.resolvePadZone(350, 220), "cf=36: 350 < 364")
        assertEquals(-1, w.resolvePadZone(370, 220), "cf=36: 370 >= 364")
    }

    // -- pause icon ----------------------------------------------------------

    @Test fun `pause — press on the icon and release elsewhere does not pause`() {
        val w = afterIntro()
        w.tick(listOf(ev(InputQueue.Type.DOWN, 370, 10, 0)))
        w.tick(listOf(ev(InputQueue.Type.UP, 200, 120, 1)))
        w.tick(emptyList())
        assertEquals(8, w.jC, "c() reads the release point only")
    }

    @Test fun `pause — release on the icon pauses after the world ran the next frame`() {
        val w = afterIntro()
        val dg0 = w.kDg
        w.tick(listOf(ev(InputQueue.Type.DOWN, 200, 120, 0)))
        w.tick(listOf(ev(InputQueue.Type.UP, 370, 10, 1)))
        assertEquals(8, w.jC, "release frame: E(262144) armed after I()")
        w.tick(emptyList())
        assertEquals(14, w.jC, "J(): v(262144) → l(14)")
        assertEquals(dg0 + 3, w.kDg, "I() ran on all three frames, the pause frame included")
    }

    // -- BACK on the menu screens -------------------------------------------

    /** Screens whose footer has a right pill, set up so that pill shows. */
    private val menuCases: List<Pair<String, (Level0World) -> Unit>> = listOf(
        "jc4 help" to { w -> w.stateL(4) },
        "jc5 about" to { w -> w.stateL(5) },
        "jc6 credits" to { w -> w.kDx = false; w.stateL(6) },
        "jc14 pause" to { w -> w.stateL(14) },
        "jc19 level select" to { w -> w.stateL(19) },
        "jc22 medals" to { w -> w.stateL(22); w.kEx = 3 },
        "jc30 browser" to { w -> w.stateL(30) },
    )

    @Test fun `BACK equals a release on the right pill on every menu family`() {
        for ((name, setup) in menuCases) {
            val viaTap = world().also(setup)
            val viaBack = world().also(setup)
            val idle = world().also(setup)
            for (w in listOf(viaTap, viaBack, idle)) w.tick(emptyList())
            val right = viaTap.menuFooter().second
            assertTrue(!right.isNullOrEmpty(), "$name: precondition — a right pill")
            val (x, y) = rightPill(viaTap, right!!)
            tap(viaTap, x, y)
            back(viaBack)
            idle.tick(emptyList())
            assertTrue(armed(viaTap, Pad.M_CYCLE), "$name: the pill tap arms 131072")
            assertTrue(armed(viaBack, Pad.M_CYCLE), "$name: BACK arms 131072")
            repeat(3) { viaTap.tick(emptyList()); viaBack.tick(emptyList()); idle.tick(emptyList()) }
            assertEquals(viaTap.jC, viaBack.jC, "$name: same screen after BACK and after the tap")
            assertEquals(viaTap.kBv, viaBack.kBv, "$name: same banner")
        }
    }

    @Test fun `BACK on a screen without a right pill does nothing`() {
        val w = world()
        w.stateL(23); w.kEc = 13                          // yes/no prompt: right ""
        w.tick(emptyList())
        assertEquals("", w.menuFooter().second)
        val before = w.jC
        back(w)
        assertFalse(armed(w, Pad.M_CYCLE))
        repeat(3) { w.tick(emptyList()) }
        assertEquals(before, w.jC)
    }
}
