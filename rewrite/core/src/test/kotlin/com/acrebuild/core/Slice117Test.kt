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

// =========================================================================
//  Slice 117 — jc27 IGP offscreen screen + jc11 suspend + jc26 dead state
//  + jc29 hard-mode row cap (k.java:1422-1447, 1104, 1388)
// =========================================================================
class Slice117Test {

    // -- jc27 (k.java:1422-1435, proven) ------------------------------------
    // `f.a(0)` → true when IGP absent (`!aE`, f.java:1185) → the screen
    // exits to `l(2)` on its first tick — the path menuItem 32/33/34
    // (save slots, :3868) and menuJc25's store prompt take.

    @Test fun `jc27 self-exits to main menu on first tick (non-IGP)`() {
        val w = world()
        w.stateL(27)
        w.tick(emptyList())
        assertEquals(2, w.jC, "f.a(0) → true non-IGP → l(2)")
    }

    @Test fun `jc27 stays a dead screen after exit (no re-entry)`() {
        val w = world()
        w.stateL(27)
        w.tick(emptyList())
        w.tick(emptyList())
        assertEquals(2, w.jC)
    }

    @Test fun `jc27 l() arm stops audio`() {
        val w = world()
        w.screenL(12)                                       // arm a track (z(7) sting)
        assertEquals(7, w.audioTrack)
        w.stateL(27)                                        // `i==27 → e.b()` (:1647)
        assertEquals(-1, w.audioTrack, "e.b() audio stop on l(27)")
    }

    // -- jc26 (k.java:1388 → :1422, proven) ---------------------------------
    // `a()`'s j.c-switch has NO case 26 — a dead screen that consumes
    // ticks without dispatching (same semantics as j.c==-1).

    @Test fun `jc26 dead screen consumes ticks without leaving`() {
        val w = world()
        w.stateL(26)
        w.tick(emptyList())
        assertEquals(26, w.jC, "no a() case for 26 — state holds")
        w.tick(emptyList())
        assertEquals(26, w.jC)
    }

    @Test fun `jc26 does not tick the entity sim`() {
        val w = world()
        w.stateL(26)
        val pos = w.player.ak
        repeat(5) { w.tick(emptyList()) }
        assertEquals(pos, w.player.ak, "dead screen — sim suspended")
    }

    // -- jc11 (k.java:1104, proven) -----------------------------------------
    // `case 11: j.c = -1; return` — the suspended/exit state; -1 has no
    // a() case either so it also consumes ticks verbatim.

    @Test fun `jc11 drops to suspended -1 and stays`() {
        val w = world()
        w.stateL(11)
        w.tick(emptyList())
        assertEquals(-1, w.jC, "case 11 → j.c = -1")
        w.tick(emptyList())
        assertEquals(-1, w.jC, "-1 consumes ticks — no exit")
    }

    // -- jc29 i13 row cap (k.java:1439-1447, proven) ------------------------
    // `i13 = bA[69]!=0 ? 3 : 2` — the HARD row (row 2) is neither drawn
    // nor selectable until the unlock byte is set.

    @Test fun `jc29 caps rows at 2 while hard mode locked`() {
        val w = world()
        w.kBA[69] = 0
        w.stateL(29)
        assertEquals(2, w.kEy, "bA[69]==0 → i13=2")
    }

    @Test fun `jc29 unlocks third row when hard mode open`() {
        val w = world()
        w.kBA[69] = 1
        w.stateL(29)
        assertEquals(3, w.kEy, "bA[69]!=0 → i13=3")
    }

    @Test fun `jc29 locked — cursor cannot reach HARD row`() {
        val w = world()
        w.kBA[69] = 0
        w.stateL(29)
        repeat(3) { w.pad.e(Pad.M_DOWN); w.tick(emptyList()); w.pad.releaseFlush() }
        assertTrue(w.kBw in 0..1, "row 2 unreachable with kEy=2 (bw=${w.kBw})")
    }

    @Test fun `jc29 confirm arm persists difficulty and exits to jc30`() {
        val w = world()
        w.stateL(29)
        w.kBw = 1                                          // NORMAL row
        w.pad.e(Pad.M_CONTEXT); w.tick(emptyList()); w.pad.releaseFlush()
        assertEquals(1, w.kAu, "au = bw — difficulty = row index")
        assertEquals(0, w.kBA[16], "verbatim bA[16]=0 second write")
        assertEquals(20, w.kFF)
        assertEquals(30, w.jC, "l(30)")
    }

    @Test fun `jc29 difficulty row tap applies bw before the au write`() {
        val w = world()
        w.kBA[69] = 0                                      // HARD locked
        w.stateL(29)
        w.tick(emptyList())
        // The draw loop writes bw on the row hit BEFORE the confirm arm
        // reads it — a tap on NORMAL must persist au=1, not the stale
        // bw=-1 (which snaps to EASY) left over from bannerK(2).
        val r = w.menuRowRects()[1]                        // NORMAL row
        val cx = r[0] + r[2] / 2; val cy = r[1] + r[3] / 2
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, cx, cy),
            InputQueue.Event(1, InputQueue.Type.UP, cx, cy)))
        assertEquals(1, w.kAu, "tapped NORMAL row → au=1")
        assertEquals(30, w.jC)
    }

    @Test fun `jc19 cancelled tap does not confirm a row`() {
        val w = world()
        w.autoDismissDialog = false
        w.kBw = -1; w.kDa = 8; w.stateL(19)                 // armMissionSelect
        w.tick(emptyList())
        w.kAj = -1                                         // canary
        // DOWN on row 5 then the OS cancels the gesture — un-hold must
        // still run but no release point exists to confirm with.
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 250, 100)))
        w.tick(listOf(InputQueue.Event(1, InputQueue.Type.CANCEL, 250, 100)))
        assertEquals(19, w.jC, "cancelled gesture must not confirm")
        assertEquals(-1, w.kAj, "no aj write on cancel")
    }
}
