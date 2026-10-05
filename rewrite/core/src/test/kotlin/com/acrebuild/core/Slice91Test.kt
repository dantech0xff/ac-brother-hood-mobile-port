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

/** Slice 91 — `F()` jc4 high-scores screen (k.java:2338-2408, proven). */
class Slice91Test {

    @Test fun `F page cycles right and wraps via mod 3`() {
        val w = world()
        w.stateL(4)
        w.pad.queuePress(Pad.M_RIGHT)
        w.tick(emptyList())
        assertEquals(1, w.kCU)
        w.pad.queuePress(Pad.M_RIGHT)
        w.tick(emptyList())
        assertEquals(2, w.kCU)
        w.pad.queuePress(Pad.M_RIGHT)
        w.tick(emptyList())
        assertEquals(0, w.kCU)                      // `(cU+1)%3`
    }

    @Test fun `F page cycles left and clamps to 2`() {
        val w = world()
        w.stateL(4)
        w.pad.queuePress(Pad.M_LEFT)
        w.tick(emptyList())
        assertEquals(2, w.kCU)                      // `cU--; <0 → 2`
    }

    @Test fun `F chevron taps cycle the page`() {
        val w = world()
        w.stateL(4)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 260, 40),
                      InputQueue.Event(0, InputQueue.Type.UP, 260, 40)))
        assertEquals(1, w.kCU)                      // right chevron (240,15,50,80)
        w.tick(listOf(InputQueue.Event(0, InputQueue.Type.DOWN, 130, 40),
                      InputQueue.Event(0, InputQueue.Type.UP, 130, 40)))
        assertEquals(0, w.kCU)                      // left chevron (110,15,50,80)
    }

    @Test fun `F up scrolls bw but down is verbatim dead`() {
        val w = world()
        w.kBw = 1
        w.stateL(4)
        w.pad.queuePress(Pad.M_UP)
        w.tick(emptyList())
        assertEquals(0, w.kBw)                      // `bw>0 → bw--`
        w.kBw = -1
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(0, w.kBw)                      // `bw<0 → bw++` fires only
                                                  // when bw is negative
        w.kBw = 0
        w.pad.queuePress(Pad.M_DOWN)
        w.tick(emptyList())
        assertEquals(0, w.kBw)                      // bw=0 → dead arm
    }

    @Test fun `F back routes to jc3`() {
        val w = world()
        w.stateL(4)
        w.pad.queuePress(Pad.M_CYCLE)
        w.tick(emptyList())
        assertEquals(3, w.jC)
    }

    @Test fun `scoreAt reads LE-16 shorts from kBA`() {
        val w = world()
        w.kBA[81] = 0x34; w.kBA[82] = 0x12
        assertEquals(0x1234, w.scoreAt(81))
        w.kBA[83] = 0xFF; w.kBA[84] = 0xFF
        assertEquals(-1, w.scoreAt(83))
    }

    @Test fun `F footer is empty-left plus BACK`() {
        val w = world()
        w.stateL(4)
        assertEquals("" to w.d0(17), w.menuFooter())
    }
}
