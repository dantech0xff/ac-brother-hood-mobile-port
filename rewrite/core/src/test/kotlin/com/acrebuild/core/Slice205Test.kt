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

class Slice205Test {
    private fun mk(ak: Int, al: Int): Entity {
        val p = Entity(0, null); p.ak = ak; p.al = al
        p.W[0] = ak - 10; p.W[2] = ak + 10
        p.W[1] = al - 20; p.W[3] = al
        return p
    }

    // -- j.d(int) piecewise table sqrt (structured/j.java:418-429,
    //    proven; U = archive /16 entry 1 verbatim, U[0] = 0 — slice 352
    //    dropped the old `U[0]=256` misread, see Slice352Test) ---------

    @Test fun `table sqrt small inputs`() {
        assertEquals(0, Trig.sqrt(-1))
        assertEquals(0, Trig.sqrt(0), "U[0] = 0 → d(0) = 0")
        assertEquals(1, Trig.sqrt(1))
        assertEquals(1, Trig.sqrt(3))
        assertEquals(15, Trig.sqrt(255))
    }

    @Test fun `table sqrt mid band boundaries`() {
        assertEquals(16, Trig.sqrt(256))
        assertEquals(63, Trig.sqrt(4095))
        assertEquals(255, Trig.sqrt(65535))
        assertEquals(256, Trig.sqrt(65536))
        assertEquals(512, Trig.sqrt(262144))
    }

    @Test fun `table sqrt quantizes large inputs`() {
        assertEquals(1020, Trig.sqrt(1048575),
            "U[255]<<2 = 1020 — table value, not true floor-sqrt 1023")
        assertEquals(32768, Trig.sqrt(0x40000000))
        assertEquals(46080, Trig.sqrt(Int.MAX_VALUE),
            "U[127]<<8 — top of the piecewise window")
    }

    // -- ap() S79 crouch-rope guard reads g.a = ga (g.java:8488-8497) ---

    private fun world(): Slice128Test.MarkerWorld =
        Slice128Test.MarkerWorld(cellFn = { _, cy -> if (cy == 6) 12 else 0 })

    private fun contextPad(): Pad {
        val pad = Pad(); pad.queuePress(Pad.M_CONTEXT); pad.commit(0)
        return pad
    }

    @Test fun `held rope stays crouched on context press`() {
        val w = world()
        val p = mk(200, 100); p.S = 79; p.gI = 1
        val rope = Entity(51, null); rope.aD = 1
        p.ga = rope
        p.contextDispatch(w, contextPad())
        assertEquals(79, p.S, "g.a.ax==51 && aD!=0 → return, stays hung")
    }

    @Test fun `slack rope allows the swing`() {
        val w = world()
        val p = mk(200, 100); p.S = 79; p.gI = 1
        val rope = Entity(51, null); rope.aD = 0
        p.ga = rope
        p.contextDispatch(w, contextPad())
        assertEquals(81, p.S, "aD==0 falls through → i(81) rope swing")
    }

    @Test fun `no vehicle link swings from crouch`() {
        val w = world()
        val p = mk(200, 100); p.S = 79; p.gI = 1
        p.ga = null
        p.contextDispatch(w, contextPad())
        assertEquals(81, p.S)
    }

    @Test fun `non-rope vehicle link swings from crouch`() {
        val w = world()
        val p = mk(200, 100); p.S = 79; p.gI = 1
        p.ga = Entity(43, null)                      // carrier, not rope
        p.contextDispatch(w, contextPad())
        assertEquals(81, p.S)
    }
}
