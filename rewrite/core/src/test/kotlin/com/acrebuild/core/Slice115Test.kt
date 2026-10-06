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

class Slice115Test {

    // -- jC==19 mission select (k.java:1178-1206, proven) ----------------

    private fun armMissionSelect(w: Level0World, da: Int = 8) {
        w.autoDismissDialog = false
        w.kBw = -1; w.kDa = da; w.stateL(19)
    }

    @Test fun `jc19 arms ey=da and shows the panel + OK BACK footer`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        assertEquals(19, w.jC)
        assertEquals(8, w.kEy, "ey=da every frame (:1179)")
        assertTrue(w.panelVisible)
        val f = w.menuFooter()
        assertEquals("OK", f.first, "a(d(0,79) — confirm")
        assertEquals("BACK", f.second, "a(d(0,17)) — back")
    }

    @Test fun `jc19 cursor nav steps bw via L(da) and writes aj`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        w.pad.e(Pad.M_DOWN); w.tick(emptyList())
        assertEquals(0, w.kBw, "bw==-1 → L() clamps bw+1→0 wait — bw=-1 then ++ → 0")
        assertEquals(0, w.kAj, "aj=bw after nav")
        w.pad.e(Pad.M_DOWN); w.tick(emptyList())
        assertEquals(1, w.kBw); assertEquals(1, w.kAj)
        w.pad.e(Pad.M_UP); w.tick(emptyList())
        assertEquals(0, w.kBw); assertEquals(0, w.kAj)
        assertEquals(19, w.jC, "nav stays on the screen")
    }

    @Test fun `jc19 confirm picks mission and routes fF=19 to l-30`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        w.pad.e(Pad.M_DOWN); w.tick(emptyList())     // bw 0 → wait — -1+1=0
        w.pad.e(Pad.M_DOWN); w.tick(emptyList())     // bw 1
        assertEquals(1, w.kBw)
        w.pad.e(327712); w.tick(emptyList())         // M_PAUSE|M_CONTEXT confirm
        assertEquals(1, w.kAj, "aj=bw on confirm (:1186)")
        assertEquals(0, w.kBA[16], "a(bA,16,(short)0)")
        assertEquals(19, w.kFF, "fF=19 return marker (:1192)")
        assertEquals(30, w.jC, "eg[aj] → l(30) — eg all-true")
    }

    @Test fun `jc19 confirm with bw==-1 snaps to 0`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        w.pad.e(327712); w.tick(emptyList())
        // `bw==-1 → bw=0` ran provably: `kAj = kBw` read it AFTER the snap
        // (:1184-1186) so aj==0. `bw` itself lands -1 again because l(30)
        // runs K(5) which writes `bw=-1` (:1847) — original behavior.
        assertEquals(0, w.kAj)
        assertEquals(19, w.kFF); assertEquals(30, w.jC)
    }

    @Test fun `jc19 back exits to l-2`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        w.pad.e(Pad.M_CYCLE); w.tick(emptyList())    // v(131072) BACK
        assertEquals(2, w.jC, "v(131072) → l(2);z(30)")
    }

    @Test fun `jc19 row tap selects and confirms the tapped mission`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        // panel d(14,47,180) → b(14,47,180,false,false): j.c==19
        // keeps the two-column split (k.java:5942-5947) — rows 0-3 at
        // x=14 w=180, rows 4-7 at x=206 w=180,
        // each 30px from y=57. Tap row 5 at (250,100): the draw-loop
        // hit-test `c(i,i9,i3,i4)` → `bw=i13; E(32)` (:7699-7704) →
        // the case-19 `v(327712)` confirms with bw=5.
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 250, 100),
            InputQueue.Event(1, InputQueue.Type.UP, 250, 100)))
        assertEquals(5, w.kAj, "row 5 tap → bw=5 → aj=5 (:1186)")
        assertEquals(19, w.kFF, "fF=19 return marker")
        assertEquals(30, w.jC, "eg[aj] → l(30)")
    }

    @Test fun `jc19 row tap spanning two ticks still confirms on release`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        // A real tap (~50-150ms) straddles the 62ms tick boundary — the
        // rowTap arm hit-tests the release point so it must run on the
        // UP tick too, not only on the DOWN tick.
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 250, 100)))
        w.tick(listOf(InputQueue.Event(1, InputQueue.Type.UP, 250, 100)))
        assertEquals(5, w.kAj, "row 5 release → bw=5 → aj=5")
        assertEquals(19, w.kFF)
        assertEquals(30, w.jC)
    }

    @Test fun `jc19 rows are eA-bv with city sub-labels`() {
        val w = world()
        armMissionSelect(w)
        w.tick(emptyList())
        // eW/eX (:299-300): ROME/ROME/FLORENCE/FLORENCE/ROME/VENICE/PANTHEON/ROME
        assertEquals("ROME", w.menuRowSub(0))
        assertEquals("ROME", w.menuRowSub(1))
        assertEquals("FLORENCE", w.menuRowSub(2))
        assertEquals("FLORENCE", w.menuRowSub(3))
        assertEquals("ROME", w.menuRowSub(4))
        assertEquals("VENICE", w.menuRowSub(5))
        assertEquals("PANTHEON", w.menuRowSub(6))
        assertEquals("ROME", w.menuRowSub(7))
    }

    // -- jC==1 hard-mode unlock toast (k.java:800-811, proven) -----------
    // The toast SELF-SKIPS in the original: l() resets fd=-1 (k.java:1645)
    // so case 1's own a() call sees `fd < i3-iK` on frame 1 and re-fires
    // the dx end-tail (:5666-5678): bA[69]==0 → {bA[69]=1;e(true);l(1)}
    // once, then bA[69]!=0 → l(25) → case 25's `dx && !Z()` → l(2). The
    // "CONGRATULATIONS!" text never draws (a() returns early). The v(262144)
    // press arm is present verbatim but unreachable via this path.

    @Test fun `jc1 toast self-skips — bA-69 persist then l-25 then l-2`() {
        val w = world()
        w.kDx = true                                 // post-credits flow
        w.stateL(1)
        w.tick(emptyList())
        assertEquals(1, w.kBA[69], "a(bA,69,1) + e(true) save-flush arm")
        assertEquals(1, w.jC, "tail re-entered l(1) once")
        w.tick(emptyList())
        assertEquals(25, w.jC, "bA[69]!=0 → l(25);dw=255 — toast skipped")
        w.tick(emptyList())
        assertEquals(2, w.jC, "case 25: dx && !Z() → l(2)")
    }

    @Test fun `jc1 armed from the story scroller — real l-1 path`() {
        // The ONLY non-jc24 a() end-tail path that reaches l(1): the story
        // screen's scroll (case 20, kCu==5 → a(bW,0,fb,5,85,390,120,0,0))
        // fires `fd < 85-iK` → dx && bA[69]==0 → {bA[69]=1;e(true);l(1)}.
        val w = world(charmap = asset("fonts/charmap.bin"))
        w.kDx = true                                 // post-credits
        w.stateL(20)
        // tick-only drive: a pause press at cu==4/5 exits to l(9) verbatim
        // (:1300 `v(262144) && cu==5 → l(9)`) — reach the scroller via the
        // kCT/kFc timeouts instead (~430 ticks).
        var g = 0
        while (w.kCu != 5 && g++ < 1000) w.tick(emptyList())
        assertEquals(5, w.kCu, "cu==5 — scrollable story panel (:1282)")
        w.kFd = -100000                              // scrolled past the end
        w.tick(emptyList())
        assertEquals(1, w.kBA[69], "a(bA,69,1) + e(true) — unlock armed")
        assertEquals(1, w.jC, "end-tail → l(1) — the toast screen")
        w.tick(emptyList())
        assertEquals(25, w.jC, "bA[69]!=0 → toast self-skip → l(25)")
        w.tick(emptyList())
        assertEquals(2, w.jC, "dx && !Z() → l(2) main menu")
    }

    // -- l(9) arm internals (k.java:1685-1690 + L() :3273, proven) --------

    @Test fun `l-9 resets fO dg ap and stops audio`() {
        val w = world()
        w.autoDismissDialog = false
        w.stateL(9)
        assertEquals(9, w.jC)
        assertEquals(0, w.kFo, "fO=0 (l() arm)")
        assertEquals(0, w.kDg, "L() dg=0 (:3274)")
        assertTrue(w.kAp.all { it == 0 }, "L() ap[0..5]=0")
        assertEquals(-1, w.audioTrack, "e.b() audio stop")
    }

    // -- dlgU unification (subU/kMode → dlgU correctness fix) ------------

    @Test fun `u9 dialog does NOT freeze the world — l-21 al gate reads dlgU`() {
        val w = world()
        w.autoDismissDialog = false
        w.dlgU = 9
        w.stateL(21)
        assertFalse(w.kAl, "u8/u9 dialogs tick the sim — kMode bug fixed")
    }

    @Test fun `u8 dialog sets cz and does NOT freeze`() {
        val w = world()
        w.dlgU = 8
        w.stateL(21)
        assertFalse(w.kAl, "u8 → cz arm, no freeze")
    }

    @Test fun `u0 dialog freezes — modal gate`() {
        val w = world()
        w.dlgU = 0
        w.stateL(21)
        assertTrue(w.kAl, "u∉{8,9} → al freeze")
    }
}
