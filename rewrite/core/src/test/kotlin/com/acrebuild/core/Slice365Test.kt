package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * Slice 365 — which `g.e()` arms reach the shared tail (g.javap.txt e(),
 * proven).
 *
 * The `S` tableswitch (offset 634, cases 0..377, default 13598) sends each
 * state to one of 107 arm entry offsets. Every arm either `return`s or
 * jumps to 13629 (L353d, the head of the shared tail: `ab` release, the
 * L1947 J&4 block); none jumps past it, and 13629 falls into the post-tail
 * at 14349 (equip, `aA` bookkeeping, the `A` latch, the jump tail, the flag
 * consumers). 41 arms (60 states) always return, 58 arms (90 states) always
 * reach 13629, 8 arms (15 states) do both, and the 213 default states fall
 * into 13629 from 13598.
 *
 * The port ran `postTail` after every arm, the returning ones included: 23
 * always-returning arms and S270/S271 returned from `dispatch` but still
 * ran it, and 18 always-returning arms (34 states) plus the return paths
 * of S12, S33, the combo arm, S147, S228 and S303 ran L353d as well.
 * `az()` (offset 617, the last head step) ran in the port's tail, after the
 * arm and never for a returning arm. And the J&4 block's `(J&4)==0 || S==50
 * → goto 14204` was a `return` in the port, which kept `g.cm` armed.
 *
 * The post-tail's `A` latch (14440-14473: `g.A → E(); av=g.B; ag=0;
 * i(148); g.A=0; return`) marks whether a tick reached the post-tail: no
 * arm and nothing in L353d touches `g.A`.
 */
class Slice365Test {
    private fun resetStatics() {
        Entity.grabLatch = false; Entity.gq = false; Entity.gf = null
        Entity.gE = false; Entity.icu = false; Entity.entBq = 0
        Entity.at = null
    }

    private fun playerAt(ak: Int, al: Int, clip: Clip? = null): Entity {
        val p = Entity(0, clip)
        p.ak = ak; p.al = al; p.av = false
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    private fun armLatch(p: Entity) { p.gA = true; p.gB = true }

    private fun assertNoTail(p: Entity, s: Int, what: String) {
        assertTrue(p.gA, "$what: returned before the post-tail — g.A untouched")
        assertEquals(s, p.S, "$what: no S148 from the 14440 latch")
    }

    private fun assertTail(p: Entity, what: String) {
        assertFalse(p.gA, "$what: the post-tail ran — the 14440 latch took g.A")
        assertEquals(148, p.S, "$what: the latch forced S148")
    }

    /** The states whose arm always `return`s (g.javap.txt e() exit map). */
    private val alwaysReturns = setOf(
        6, 49, 50, 74, 91, 152, 156, 204, 209, 214, 216, 217, 225, 235, 236,
        237, 238, 239, 240, 241, 242, 243, 244, 250, 257, 258, 259, 260, 261,
        262, 267, 268, 272, 273, 274, 275, 277, 280, 282, 283, 286, 287, 292,
        293, 294, 295, 299, 300, 301, 302, 304, 305, 306, 310, 311, 312, 313,
        317, 357, 360)

    /**
     * Every state, from a bare start (open air, no input, no links, no
     * clip so `r()` holds): the always-returning arms and the return paths
     * these conditions pick in the mixed arms — the combo footing loss
     * (10258), the S147 reset (11878) and S303 without `u(62430)` (13406)
     * — skip the post-tail; every other state reaches it. S12/S33 (no
     * ladder), S228 (r() → 252), S270/S271 (no `g.g`) and S358 take
     * their L353d paths.
     */
    @Test fun `the post-tail runs exactly for the states that reach L353d`() {
        val returnsBare = alwaysReturns + setOf(67, 68, 69, 112, 113, 114, 115, 147, 303)
        val wrong = mutableListOf<String>()
        for (s in 0..377) {
            resetStatics()
            val w = Slice134Test.PassWorld(cell = 0)
            val fsm = PlayerFsm(w)
            val p = playerAt(200, 100)
            p.S = s
            armLatch(p)
            fsm.tick(p, Pad())
            val reachedTail = !p.gA
            if (reachedTail == (s in returnsBare)) {
                wrong += "S$s(${if (reachedTail) "tail" else "return"})"
            }
        }
        resetStatics()
        assertTrue(wrong.isEmpty(), "exit map mismatches: $wrong")
    }

    /** S6 (2351-2403) always returns: no L353d `ab` release, no latch. */
    @Test fun `S6 holding UP returns before L353d and the post-tail`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 6
        val held = Entity(16, null); held.S = 14
        p.ab = held
        armLatch(p)
        val pad = Pad(); pad.bC = Pad.M_UP
        fsm.tick(p, pad)
        assertNoTail(p, 6, "S6")
        assertSame(held, p.ab, "13629's `ab.S==14 → ab=null` never ran")
    }

    /** The S12 ladder snap `i(74)` returns at 5944; the other S12 exits
     *  `goto 13629`. */
    @Test fun `the S12 ladder snap returns`() {
        resetStatics()
        val w = Slice134Test.PassWorld(cellFn = { cx, cy -> if (cx == 11 && cy == 4) 21 else 0 })
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 12; p.ag = 512; p.aO = 0
        armLatch(p)
        fsm.tick(p, Pad())
        assertNoTail(p, 74, "S12 → A() → i(74)")
    }

    /** S33's `A()` ladder snap `i(74)` returns at 6921. */
    @Test fun `the S33 ladder snap returns`() {
        resetStatics()
        val w = Slice134Test.PassWorld(cellFn = { cx, cy -> if (cx == 11 && cy == 4) 21 else 0 })
        val fsm = PlayerFsm(w)
        val p = playerAt(200, 100)
        p.S = 33
        armLatch(p)
        fsm.tick(p, Pad())
        assertNoTail(p, 74, "S33 → A() → i(74)")
        assertTrue(p.cp, "the arm's cp=1 stays (no consumer ran)")
    }

    /** S228/358 end at 12758: `S != 228 → goto 13629`, else `return`. */
    @Test fun `a held S228 returns, S358 reaches the post-tail`() {
        resetStatics()
        val clip0 = world().clips[0]!!
        for ((s, tail) in listOf(228 to false, 358 to true)) {
            resetStatics()
            val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
            val p = playerAt(200, 100, clip0)
            p.setAnim(s); p.T = 0; p.U = 0
            assertFalse(p.animFinished(), "S$s mid-anim")
            armLatch(p)
            fsm.tick(p, Pad())
            if (tail) assertTail(p, "S$s") else assertNoTail(p, s, "S$s")
        }
        resetStatics()
    }

    /** S303: `u(62430) → i(0); goto 13629` (13356-13369), else `return`
     *  (13406). */
    @Test fun `S303 returns unless u(62430) drops it to S0`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 303
        armLatch(p)
        val pad = Pad(); pad.bC = Pad.M_UP                 // 16388 & 62430 != 0
        fsm.tick(p, pad)
        assertTail(p, "S303 + u(62430)")
    }

    /** S270 with `g.g` returns at 3987 (the port returned from dispatch but
     *  still ran postTail); without `g.g` it is `i(0); goto 13629`. */
    @Test fun `S270 with a grab target skips the post-tail`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 270
        val gg = Entity(11, null); gg.ak = p.ak + 10; gg.al = p.al; gg.aB = 1
        p.g = gg
        armLatch(p)
        fsm.tick(p, Pad())
        assertNoTail(p, 271, "S270 r() → i(271)")
        assertSame(gg, p.g, "az() keeps the S270/271 links")
    }

    /** `az()` is e()'s head step at offset 617 — it runs for a returning
     *  arm too (the port ran it in the tail, so S49 never refreshed `g`). */
    @Test fun `az runs for a returning arm`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 49
        val dead = Entity(11, null); dead.ak = p.ak + 10; dead.al = p.al
        dead.aB = 0
        p.g = dead
        fsm.tick(p, Pad())
        assertNull(p.g, "az(): `ax != 4 && aB <= 0 → g = null`")
    }

    /** ...and before the arm: S291 reads this tick's `g`. A dead target is
     *  dropped first, so the fire edge takes the `k.u()` exit (i(285)), not
     *  the hand-off to S270. */
    @Test fun `az runs before the arm reads g`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 291; p.T = 0
        val t = Entity(10, null); t.S = 0
        p.af = t
        val dead = Entity(11, null); dead.ak = p.ak + 10; dead.al = p.al
        dead.aB = 0
        p.g = dead
        val pad = Pad(); pad.bB = 65568
        fsm.tick(p, pad)
        assertEquals(285, p.S, "g dropped by az() → r9 == 0 → k.u() edge → i(285)")
        assertNull(p.af)
    }

    /** 13741-13755: `(J&4)==0 → goto 14204`; r98 stays 0, so 14322 drains
     *  `g.cm` on an arm that reaches L353d (S59: bare `goto 13629`). */
    @Test fun `the L353d J4 block drains g_cm without a mount request`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 59; p.gJ = 0; p.gcm = true
        armLatch(p)
        fsm.tick(p, Pad())
        assertFalse(p.gcm, "14322: cm=0")
        assertTail(p, "S59 bare L353d")
    }

    /** A tail arm still runs L353d's `ab` release (13629-13659). */
    @Test fun `a tail arm still runs the L353d ab release`() {
        resetStatics()
        val fsm = PlayerFsm(Slice134Test.PassWorld(cell = 0))
        val p = playerAt(200, 100)
        p.S = 59
        val held = Entity(16, null); held.S = 14
        p.ab = held
        fsm.tick(p, Pad())
        assertNull(p.ab, "S != 9 && ab.S == 14 → ab = null")
    }
}
