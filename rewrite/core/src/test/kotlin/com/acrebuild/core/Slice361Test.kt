package com.acrebuild.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertSame

/**
 * Slice 361 — `Entity.setAnim` is `i.i(int)` (bytecode i.javap.txt
 * i(int) offsets 0-299) and `enterStateMasked` is `i.a(int,int)`.
 *
 * The port reset Q/S/T/U/a/P only, and ran the player-slot side effects
 * (`g.y = al`, `g.e(0)`) only on a state change. The original runs those
 * on every call, shifts `al` by `W[3]-W[1]` on `i(43)` from S61, zeroes
 * an ax43's velocity on `i(11)`, maps the phase-3 boss's `i(0)` to
 * `i(36)`, places the ax29 aura on `i(27)`, and clears the `y` anim
 * freeze on a real change. `i.a(int,int)`'s x snaps are one else-if chain.
 */
class Slice361Test {
    @Test fun `a state change clears the y freeze, a re-set does not`() {
        val e = Entity(11, null)
        e.S = 4; e.y = 101
        e.setAnim(4)
        assertEquals(101, e.y, "same S: no reset")
        e.setAnim(5)
        assertEquals(0, e.y, "i != S → y = 0")
    }

    @Test fun `the player stamps g y and clears the meter on every call`() {
        val p = Entity(0, null)
        p.S = 43; p.al = 500; p.gy = 100
        p.setAnim(43)
        assertEquals(500, p.gy, "i(43) re-stamps g.y even when already S43")
        p.S = 50; p.x1 = 30
        p.setAnim(50)
        assertEquals(0, p.x1, "i(50) → g.e(0) even when already S50")
    }

    @Test fun `i 43 from S61 drops the player by the box height`() {
        val p = Entity(0, null)
        p.S = 61; p.al = 300
        intArrayOf(0, 260, 20, 300).copyInto(p.W)
        p.setAnim(43)
        assertEquals(340, p.al, "al += W[3]-W[1]")
        assertEquals(43, p.S)
    }

    @Test fun `ax43 i 11 stops it dead`() {
        val r = Entity(43, null)
        r.ag = 512; r.ah = -300; r.ai = 7; r.aj = 1536
        r.setAnim(11)
        assertEquals(listOf(0, 0, 0, 0), listOf(r.ag, r.ah, r.ai, r.aj))
    }

    @Test fun `the phase 3 boss maps i 0 to i 36`() {
        val w = world()
        val b = Entity(29, w.clips[7])
        b.S = 28
        w.kAU = b; w.iBy = 3
        b.setAnim(0)
        assertEquals(36, b.S)
        val other = Entity(11, w.clips[7]); other.S = 5
        other.setAnim(0)
        assertEquals(0, other.S, "only k.aU")
        w.iBy = 2; b.setAnim(0)
        assertEquals(0, b.S, "only at by 3")
    }

    @Test fun `ax29 i 27 places the ck aura`() {
        val w = world()
        w.iCk = null
        val b = Entity(29, w.clips[7])
        b.setPositionPx(700, 300); b.az = 40; b.av = true
        b.setAnim(27)
        val ck = assertNotNull(w.iCk, "e(15, ak, al, az-1)")
        assertEquals(15, ck.S)
        assertEquals(700, ck.ak); assertEquals(300, ck.al)
        assertEquals(39, ck.az)
        assertEquals(true, ck.av)
        assertSame(ck, w.pendingInsert.last(), "k.b(aK)")
    }

    @Test fun `i a x snaps are one else-if chain`() {
        val w = world()
        val e = Entity(11, null)
        e.ak = 333
        intArrayOf(305, 0, 345, 40).copyInto(e.W)
        e.enterStateMasked(0, 2048 or 8 or 1, w)
        assertEquals(305, e.ak, "2048 wins; the 8 grid snap is skipped")
    }
}
