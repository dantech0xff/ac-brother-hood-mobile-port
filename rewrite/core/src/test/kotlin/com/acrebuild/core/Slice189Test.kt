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

/** Slice 189 — bound-interaction family (S37/38 grapple + bound stance,
 *  S228/358 ride-bound, S235-243 push/pull/release, S50/241 crush,
 *  S252→fallArm). Verbatim per structured/fallback g.java. */
class Slice189Test {

    private fun grappleBound(w: Level0World, s: Int): Entity {
        val p = w.player
        p.clip = null                          // state-machine tests: anim ops land + finish
        val g = Entity(10, null); g.S = 32
        p.bindAc(g); p.S = s
        return g
    }

    /** S38 grapple-bound: `u(16388)` → `G(); al-=20; i(23); ah=2560` +
     *  `cq` (structured g.java:2106-2115). M_UP|M_RIGHT keeps RIGHT held
     *  so the same-tick `!u(8256) → i(37)` arm does not overwrite S23. */
    @Test fun `grapple bound up key spring jumps`() {
        val w = world(); val p = w.player
        grappleBound(w, 38)
        p.al = 300
        val pad = Pad(); pad.commit(Pad.M_UP or Pad.M_RIGHT)
        w.playerFsm.tick(p, pad)
        // The arm runs verbatim (G() release, al-=20, i(23), ah=2560) —
        // then the shared L2083 jump tail consumes the same UP edge
        // (ah=0 + i(233)), so the observable end-state is S233.
        assertEquals(280, p.al, "al-=20 on the grapple spring")
        assertEquals(233, p.S, "L2083 tail converts the UP edge to i(233)")
        assertTrue(p.cq, "cq armed")
    }

    /** S38 grapple-bound + `u(33024)` in open air → `al=W[3]+10; i(43)`
     *  drop into the fall state (structured g.java:2137-2145). */
    @Test fun `grapple bound down key drops to fall`() {
        val w = world(); val p = w.player
        grappleBound(w, 38)
        p.setPositionPx(300, 60); p.refreshBoxes()   // open air — probe finds aO==0
        val pad = Pad(); pad.commit(Pad.M_DOWN)
        w.playerFsm.tick(p, pad)
        assertEquals(43, p.S, "down+empty head cell → i(43)")
        assertEquals(p.W[3] + 10, p.al, "al = W[3]+10")
    }

    /** S228: `u(2)` into facing with crate on the facing side keeps the
     *  bind (`av == ac.ak<ak` → no release); crate behind → release into
     *  `i(235)` + `ac=null` (structured g.java:3287-3294). */
    @Test fun `ride bound tap into facing side keeps bind`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null); crate.S = 8
        // real clip mid-anim: S228's `r()` exit is `a(0)` (12746-12748,
        // slice 369), which drops `ac` whatever the tap did
        p.bindAc(crate); p.setAnim(228); p.T = 0; p.U = 0
        p.ak = 400; p.av = true                     // facing left
        crate.ak = 300                              // crate on the facing side
        val pad = Pad(); pad.commit(Pad.M_TAP_L)
        w.playerFsm.tick(p, pad)
        assertSame(crate, p.ac, "facing matches crate side — bind stays")
        assertNotEquals(235, p.S, "release arm did not fire")
    }

    @Test fun `ride bound tap with crate behind releases`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null); crate.S = 8
        p.clip = null; p.bindAc(crate); p.S = 228
        p.ak = 400; p.av = true                     // facing left
        crate.ak = 500                              // crate behind the player
        val pad = Pad(); pad.commit(Pad.M_TAP_L)
        w.playerFsm.tick(p, pad)
        assertNull(p.ac, "facing mismatch → bind dropped via the i(235) arm")
        // The shared L2083 tail then re-arms the tap edge as i(22); S==235
        // is transient inside the same tick.
    }

    /** S235 `T==1` homing: ax51 crate fully outside the 60px band →
     *  `r96=10` → `ag=(160<<8)/10` (structured g.java:3354-3367). */
    @Test fun `push entry homes onto crate far`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null)
        p.clip = null; p.bindAc(crate); p.S = 235; p.T = 1
        p.ak = 400; p.al = 400
        crate.ak = 560; crate.al = 480
        w.playerFsm.tick(p, Pad())
        assertEquals((160 shl 8) / 10, p.ag, "r96=10 when both axes >= 60")
        assertEquals((80 shl 8) / 10, p.ah)
    }

    /** Same but within 60 on one axis → `r96>>=1` = 5 (the ax51 close-in
     *  rate). */
    @Test fun `push entry homes onto crate close`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null)
        p.clip = null; p.bindAc(crate); p.S = 235; p.T = 1
        p.ak = 400; p.al = 400
        crate.ak = 560; crate.al = 400              // dy < 60 → close
        w.playerFsm.tick(p, Pad())
        assertEquals((160 shl 8) / 5, p.ag, "r96=5 for the ax51 close-in")
    }

    /** S235 unbound (`ac==null`) → the `ag=+4864, ah=-6656` dive at T==1
     *  (structured g.java:3368-3388); both unbound exits are `a(0)` =
     *  `g.a(int)`, the S43 fall (g.javap.txt e() 5503-5505/5515-5517 —
     *  `invokevirtual #213`, slice 369; the port had `i(0)`). Real clip
     *  so the dive tick is mid-anim. */
    @Test fun `push entry unbound dives`() {
        val w = world(); val p = w.player
        p.setAnim(235); p.T = 1; p.U = 0; p.av = false
        assertFalse(p.animFinished())
        w.playerFsm.tick(p, Pad())
        assertEquals(4864, p.ag, "unbound dive impulse")
        assertEquals(-6656, p.ah)
        assertEquals(235, p.S, "the dive keeps S235 until r()")
        p.T = p.clip!!.frameCount(235) - 1
        p.U = p.clip!!.frameDuration(235, p.T) - 1
        w.playerFsm.tick(p, Pad())
        assertEquals(43, p.S, "unbound r() → a(0) = g.a(int) → S43")
    }

    /** S236 push-hold: bound crate `S==8` and `al <= ac.al` (bottom at/
     *  above crate) → `a=null; a(0)` (structured g.java:3391-3397) —
     *  `a(0)` is `g.a(int)` (g.javap.txt e() 5774-5776), the S43 fall
     *  that also drops `ac` (slice 369; the port had `i(0)`). */
    @Test fun `push hold releases above crate`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null); crate.S = 8; crate.al = 500
        p.clip = null; p.bindAc(crate); p.S = 236; p.al = 400
        w.playerFsm.tick(p, Pad())
        assertEquals(43, p.S, "al <= ac.al → release through a(0)")
        assertNull(p.ga)
        assertNull(p.ac, "g.a(int) clears ac")
    }

    /** Gate lost (crate no longer S8) → `aj=512` only. */
    @Test fun `push hold gate lost gravity only`() {
        val w = world(); val p = w.player
        val crate = Entity(51, null); crate.S = 4
        p.clip = null; p.bindAc(crate); p.S = 236
        w.playerFsm.tick(p, Pad())
        assertEquals(512, p.aj)
        assertEquals(236, p.S, "state unchanged")
    }

    /** S241 crush: frame-1 `d(999)` + `ab` drop + sfx18, `ag>>=2` damping,
     *  `r()` → `k.l(12)` fail screen (structured g.java:2160-2183). */
    @Test fun `crush death drains meter and fails`() {
        val w = world(); val p = w.player
        p.clip = null; p.S = 241; p.T = 1; p.U = 0
        p.ag = 800; p.ab = Entity(11, null)
        w.playerFsm.tick(p, Pad())
        assertEquals(0, p.x1, "d(999) → x[1] = 0")
        assertNull(p.ab)
        assertTrue(w.sfxLog.contains(18), "k.A(18) crush sfx")
        assertEquals(200, p.ag, "ag>>=2")
        assertEquals(0, p.ah); assertEquals(0, p.aj)
        assertEquals(12, w.jC, "r() → k.l(12) fail screen")
    }

    /** S242 apex → i(243); S243 `r()` → `a(0)` — g.a(int) = enterFall,
     *  S43 with ag preserved (g.java:3428-3437 + L126, proven). Real
     *  clip so `r()` stages the transitions. */
    @Test fun `release anims 242 to 243 to fall`() {
        val w = world(); val p = w.player
        p.S = 242; p.T = 0; p.U = 0; p.ah = 0
        w.playerFsm.tick(p, Pad())
        assertEquals(243, p.S, "S242 + ah>=0 → i(243)")
        p.T = p.clip!!.frameCount(243) - 1
        p.U = p.clip!!.frameDuration(243, p.T) - 1
        w.playerFsm.tick(p, Pad())
        assertEquals(43, p.S, "S243 r() → a(0) = enterFall → S43")
    }
}
