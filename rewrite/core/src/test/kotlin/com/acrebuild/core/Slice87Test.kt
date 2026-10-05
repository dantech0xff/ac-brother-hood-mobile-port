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

/** Slice 87 — `a(str,str2)` footer soft-keys + `a(i,i2,i3,z2)` pill
 *  (k.java:2242-2310, proven) + verbatim per-state panel rects. */
class Slice87Test {

    @Test fun `footer labels per screen state`() {
        val w = world()
        w.stateL(14)                                    // pause menu
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
        w.stateL(19); w.kDa = 0                          // eA menu
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
        w.stateL(28)                                     // ae() path
        assertEquals(w.d0(79) to w.d0(17), w.menuFooter())
        w.stateL(29); w.kBv = 0                          // IGP poster
        val fl29 = w.menuFooter()
        assertNull(fl29.first)
        assertEquals("", fl29.second)                    // bv==0 → ""
        w.stateL(12)
        assertEquals(null to null, w.menuFooter())       // fail screen — none
    }

    @Test fun `panel rect is verbatim per state`() {
        val w = world()
        w.stateL(12)
        assertEquals(listOf(93, 67, 214), w.menuPanelRect().toList())
        w.stateL(14); w.kBv = 3
        assertEquals(listOf(93, 67, 214), w.menuPanelRect().toList())
        assertTrue(w.menuPanelZ3())
        w.kBv = 1
        assertEquals(listOf(93, 30, 214), w.menuPanelRect().toList())
        assertFalse(w.menuPanelZ3())
        w.stateL(19)
        assertEquals(listOf(93, 47, 214), w.menuPanelRect().toList())
        assertFalse(w.menuPanelZ2())                     // d() → z2=false
    }

    @Test fun `footer right pill tap arms the back bit`() {
        val w = world()
        w.stateL(14)                                     // has BACK footer
        // right rect: (395-cf-10, 198, cf+20, 47); cf=36 → (349,198,56,47)
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 370, 210),
            InputQueue.Event(1, InputQueue.Type.UP, 370, 210)))
        // E(131072) on jc14 → back arm: `menuP(); kBw=-1` (:1765)
        assertEquals(-1, w.kBw)
        assertEquals(36, w.kCf)                          // right rect was hit
    }

    @Test fun `footer left pill tap runs its rect`() {
        val w = world()
        w.stateL(14); w.kBv = 2
        // bv==2 → a(d(0,16),d(0,17)): NEXT is a real pill
        // (r10==d(0,79) → L23 skip — an "OK" left has NO zone, so this
        //  test needs NEXT: k.java:2909-2912).
        // left rect (-5,198,ce+20,47), ce=36 → (-5,198,56,47): tap x=20
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 20, 210),
            InputQueue.Event(1, InputQueue.Type.UP, 20, 210)))
        assertEquals(36, w.kCe)             // left rect hit → E(262144) armed
        // (the bit's jc14 consumption lives in the unported m() arms —
        //  flagged `inferred`; the arming itself is verbatim :2288)
        w.lastTouchX = -1; w.lastTouchY = -1
    }

    @Test fun `ce cf reset each frame then set by hit-test`() {
        val w = world()
        w.stateL(14)
        w.kCe = 99; w.kCf = 99
        w.tick(listOf(
            InputQueue.Event(0, InputQueue.Type.DOWN, 370, 210),
            InputQueue.Event(1, InputQueue.Type.UP, 370, 210)))
        // left is "OK" (bv!=2) → L23 skips the arm: ce keeps the -1
        // reset (k.java:2907-2912); right pill assigned + hit.
        assertEquals(-1, w.kCe)
        assertEquals(36, w.kCf)
        w.lastTouchX = -1; w.lastTouchY = -1
    }

    @Test fun `clip93 has the pill frames 41 to 44 and arrows 24 29`() {
        val clip = Clip.load(
            java.io.File("../generated/clips/clip93/clip.acpk").readBytes())
        assertTrue(clip.animCount() >= 45)
        assertTrue(clip.frameCount(41) >= 1)
        assertTrue(clip.frameCount(44) >= 1)
        assertTrue(clip.frameCount(24) >= 1)
        assertTrue(clip.frameCount(29) >= 1)
    }
}
