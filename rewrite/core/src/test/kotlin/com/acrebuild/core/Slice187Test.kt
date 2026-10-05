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

// ===========================================================================
// slice 187 — g.e() S8 hit-connect lock arm (g.java:3011-3024 L1301) +
// the L1926 universal tail (g.java:3460-3473): `ab` release at partner
// S14, `aO==6` → op18 (`g.a(); i(43)`) unless in/on a type-2 cell.
// ===========================================================================
class Slice187Test {

    /** `av==false` → `ag=-1280`; `av==true` → `ag=+1280` — the
     *  back-step off facing; `ah`/`aj` zeroed each tick
     *  (g.java:3016-3021). */
    @Test fun `S8 back-steps opposite facing`() {
        val w = world()
        val p = w.player
        p.setAnim(8); p.S = 8
        p.av = false; p.ah = 700; p.aj = 700
        w.playerFsm.tick(p, Pad())
        assertEquals(-1280, p.ag, "av==false → ag=-1280 (g.java:3021)")
        assertEquals(0, p.ah, "ah = 0")
        assertEquals(0, p.aj, "aj = 0")
        p.av = true
        w.playerFsm.tick(p, Pad())
        assertEquals(1280, p.ag, "av==true → ag=+1280 (g.java:3018)")
    }

    /** `T==1 && U==0` → footstep `k.A(11)` once (g.java:3013). */
    @Test fun `S8 footstep at frame one`() {
        val w = world()
        val p = w.player
        p.setAnim(8); p.S = 8; p.T = 1; p.U = 0
        w.playerFsm.tick(p, Pad())
        assertTrue(11 in w.sfxLog, "k.A(11) footstep at T==1&&U==0")
    }

    /** `r()` in S8 → `P|=64` then `aw()` resumes locomotion
     *  (g.java:3021-3024). The `P|64` hold is transient in the original
     *  too — `aw()`'s inner `i()` clears bit6 (`P &= -65`, i.java:290) —
     *  so the observable check is the arm leaving S8 via `aw()`. */
    @Test fun `S8 anim end resumes via aw`() {
        val w = world()
        val p = w.player
        standOn(w, p)
        p.setAnim(8); p.S = 8
        p.T = (p.clip?.frameCount(8) ?: 1) - 1
        p.U = (p.clip?.frameDuration(8, p.T) ?: 1) - 1
        w.playerFsm.tick(p, Pad())
        assertNotEquals(8, p.S, "aw() resumed out of the S8 lock")
    }

    /** L1926 (g.java:3461-3463): `S!=9 && ab!=null && ab.S==14` →
     *  `ab = null` — the linked partner drops once it reaches S14. */
    @Test fun `L1926 releases ab at partner S14`() {
        val w = world()
        val p = w.player
        p.setAnim(0); p.S = 0
        val link = Entity(16, null); link.S = 14
        p.ab = link
        w.playerFsm.tick(p, Pad())
        assertNull(p.ab, "ab=null when ab.S==14")
    }

    /** Same gate: `ab.S!=14` → the link holds (g.java:3463). */
    @Test fun `L1926 keeps ab below S14`() {
        val w = world()
        val p = w.player
        p.setAnim(0); p.S = 0
        val link = Entity(16, null); link.S = 13
        p.ab = link
        w.playerFsm.tick(p, Pad())
        assertSame(link, p.ab, "ab.S==13 → link kept")
    }

    /** `S==9` skips the release outright (g.java:3461) — the carry
     *  state owns `ab` until its own arm ends the anim. */
    @Test fun `L1926 skips release in S9`() {
        val w = world()
        val p = w.player
        p.setAnim(9); p.S = 9
        val link = Entity(16, null); link.S = 14
        p.ab = link
        w.playerFsm.tick(p, Pad())
        assertSame(link, p.ab, "S==9 → ab release skipped")
    }

    /** L1933-L1946 (g.java:3465-3473): `aR!=2 && aO!=2 && !L()` and
     *  `aO==6` → `a(18,0,0,this)` = `g.a()` damage + `i(43)` drop. */
    @Test fun `aO6 head cell fires op18 drop`() {
        // head cell 6 via cellFn — the rescan recomputes aO from the grid
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 6 else 0 })
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.ak = 200; p.al = 100
        p.W[0] = 190; p.W[2] = 210; p.W[1] = 80; p.W[3] = 100
        p.S = 8
        val x1Before = p.x1
        fsm.tick(p, Pad())
        assertEquals(43, p.S, "op18 → i(43) knockdown (i.java:4543)")
        assertTrue(p.x1 < x1Before, "g.a() pays u[au] damage")
    }

    /** g.javap.txt e() 13662-13711 (slice 370): `(aR==2 || aO==2 || L())
     *  && g.a==null → ah=aj=0; g.e(0); i(50); return` runs BEFORE the
     *  13712 `aO==6` hit — a type-2 cell under the feet kills. (Slice 187
     *  read the simple view, which drops the jumps, as "type-2 suppresses
     *  the aO==6 hit".) */
    @Test fun `type2 cell under the feet kills before the aO6 hit`() {
        val w = Slice128Test.MarkerWorld(cellFn = { cx, cy ->
            if (cx == 10 && cy == 4) 6 else if (cx == 10 && cy == 5) 2 else 0 })
        val fsm = PlayerFsm(w)
        val p = Entity(0, null)
        p.ak = 200; p.al = 100
        p.W[0] = 190; p.W[2] = 210; p.W[1] = 80; p.W[3] = 100
        p.S = 8
        fsm.tick(p, Pad())
        // aR==2 → aZ stays false (x() returns early) → the S8 anim-end
        // arm's l()→aw() enters a(0) (S43, aj=1536); L353d then kills.
        assertEquals(50, p.S, "aR==2 && g.a==null → i(50)")
        assertEquals(0, p.x1, "g.e(0): x[1] = 0")
        assertEquals(0, p.ah); assertEquals(0, p.aj, "ah = aj = 0 after the a(0) fall")
    }
}
