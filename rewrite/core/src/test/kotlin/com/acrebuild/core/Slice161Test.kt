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

class Slice161Test {

    @Test fun `S284 rope grab anim end sets P64 no fling`() {
        val w = world()
        w.player.setAnim(284)
        w.player.clip = null               // r() -> animFinished
        w.playerFsm.tick(w.player, w.pad)
        assertTrue((w.player.P and 64) != 0)   // grab-done flag
        assertEquals(284, w.player.S)          // stays bound, no a(0)
        assertNotEquals(1536, w.player.aj)     // no fling arc
    }
}
