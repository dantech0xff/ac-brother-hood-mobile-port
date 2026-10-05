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
// slice 186 — g.e() L33-L88 pre-dispatch block: `k.E.J()` companion follow,
// the `aA|256` alert window / `aA|16` cooldown, dead → i(50), `cn++`,
// `k.aA`-gated `aA|=1`, plus the `b(5-int)` knife spawn + `k.aY` pool
// (proven-dead call paths — ported for parity).
// ===========================================================================
class Slice186Test {

    /** `aA&256` armed + `Z[1]<120` → `Z[1]` decays by one per tick
     *  (g.java:551-555) while the alert isn't blind. */
    @Test fun `aA256 window decays Z1 while not blind`() {
        val w = world()
        val p = w.player
        p.S = 0; p.aA = 256; p.Z[1] = 50
        w.playerFsm.tick(p, Pad())
        assertEquals(49, p.Z[1], "Z[1]-- per tick (g.java:551)")
    }

    /** `Z[1]>=120` → the |256→|16 flip runs even though the knife call
     *  is proven-dead (`k.aY[0]` always null); then the |16 window arms
     *  `Z[0]=3000` and decrements by `j.f`=62 the same tick. */
    @Test fun `aA256 Z1 at 120 flips to aA16 and arms Z0`() {
        val w = world()
        val p = w.player
        p.S = 0; p.aA = 256; p.Z[1] = 120; p.Z[0] = 0
        w.playerFsm.tick(p, Pad())
        assertTrue(p.aA and 256 == 0, "aA &= -257")
        assertTrue(p.aA and 16 != 0, "aA |= 16")
        assertEquals(119, p.Z[1], "Z[1]-- still runs after the flip (L48)")
        assertEquals(2938, p.Z[0], "Z[0]=3000 then -=62 same tick")
    }

    /** `i.bn` blind + player `aA&8` → the `Z[1]` decay is held
     *  (g.java:557-562 L50→L56 skip). */
    @Test fun `aA256 holds Z1 while blind`() {
        val w = world()
        val p = w.player
        p.S = 0; p.aA = 256 or 8; p.Z[1] = 50
        w.iBn = true
        w.playerFsm.tick(p, Pad())
        assertEquals(50, p.Z[1], "blind alert holds the decay")
    }

    /** `aA&16` cooldown: `Z[0]` counts down by `j.f` and at <=0 the
     *  window re-arms `aA&=-17; aA|=256` (g.java:566-570). */
    @Test fun `aA16 cooldown rearms aA256 at Z0 zero`() {
        val w = world()
        val p = w.player
        p.S = 0; p.aA = 16; p.Z[0] = 62             // one tick of window left
        w.playerFsm.tick(p, Pad())
        assertTrue(p.aA and 256 != 0, "aA |= 256 re-arm")
        assertTrue(p.aA and 16 == 0, "aA &= -17")
    }

    /** `i.J()` (i.java:7002): the k.E overlay mirrors player pos + facing
     *  and hides (`P|128`) once its anim finished — a clipless stand-in
     *  reports finished immediately. */
    @Test fun `kE followJ mirrors player and hides on anim end`() {
        val w = world()
        val p = w.player
        p.S = 0; p.ak = 500; p.al = 300; p.av = true
        val ke = Entity(71, null).apply { P = 0 }
        w.kE = ke
        w.playerFsm.tick(p, Pad())
        assertEquals(500, ke.ak)
        assertEquals(300, ke.al)
        assertTrue(ke.P and 1 != 0, "facing bit copied (P|=1 on av)")
        assertTrue(ke.P and 128 != 0, "P|=128 when anim finished")
    }

    /** `i.J()` running-anim path: the real clip-46 `k.E` stays visible
     *  outside the dialog while its anim hasn't finished. */
    @Test fun `kE stays visible while anim runs outside dialog`() {
        val w = world()
        val p = w.player
        p.S = 0
        val ke = w.kE ?: return                     // clip46 present in world()
        ke.P = ke.P and -129
        ke.S = 0; ke.T = 0; ke.U = 0
        w.playerFsm.tick(p, Pad())
        if (!ke.animFinished()) assertTrue(ke.P and 128 == 0,
            "running anim outside jC==21 keeps the overlay visible")
    }

    /** `g()==true` (player `x1<=0`) → `i(50)` crash state
     *  (g.java:576). */
    @Test fun `dead player arms i50`() {
        val w = world()
        val p = w.player
        p.S = 0; p.x1 = 0
        w.playerFsm.tick(p, Pad())
        assertEquals(50, p.S, "dead → i(50)")
    }

    /** `b(5-int)` (i.java:4818-4845): the knife child — `a(5,1,8,300)`
     *  with `aE=0,aF=0,n=1,P|128`, `Z` wiped {2:-1,3:-1}; `aG=-1` → plain
     *  insert (no claim bind). */
    @Test fun `spawnKnife builds the aK child verbatim`() {
        val w = world()
        val p = w.player
        p.spawnKnife(w, -1)
        val knife = w.pendingInsert.firstOrNull { it.ax == 5 }
        assertNotNull(knife, "a(5,1,8,300) child queued")
        assertTrue(knife!!.P and 128 != 0)
        assertEquals(1, knife.nl)
        assertEquals(0, knife.aE); assertEquals(0, knife.aF)
        assertEquals(-1, knife.Z[2]); assertEquals(-1, knife.Z[3])
        assertEquals(-1, knife.aG)
        assertTrue(knife.P and 512 == 0, "aG=-1 → no claim bind")
    }
}
