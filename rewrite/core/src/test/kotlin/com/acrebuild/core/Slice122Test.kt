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

/** Slice-122 `aU()` case-34 slope-rail contract (i.java:9031-9139, proven). */
class Slice122Test {
    private fun asset(path: String): ByteArray =
        java.io.File("../generated/$path").readBytes()

    @Test fun `S34 rail binds player at slope height and releases on exit`() {
        val w = world()
        val t = w.npcs.first { it.ax == 10 && it.S == 34 &&
                                 it.Z[0] == 0 && it.Z[1] != 1 }
        val slope = ((t.W[3] - t.W[1]) shl 8) / (t.W[2] - t.W[0])
        val px = (t.W[0] + t.W[2]) / 2
        val ry = t.W[1] + ((slope * (px - t.W[0])) shr 8)
        // hang: player's TOP edge (W[1]) within [ry-20, ry+30]
        w.player.setPositionPx(px, ry)
        w.player.refreshBoxes()
        val h = w.player.W[3] - w.player.W[1]
        w.player.setPositionPx(px, ry + h)
        w.player.refreshBoxes()
        w.player.setAnim(0)
        t.drawStyleF(w)                              // rail lives on
        assertSame(t, w.player.af)                   // aU() (draw path)
        assertEquals(ry + 75, w.player.al)          // i13+=10; al=i13+65
        assertEquals((slope * 1024) shr 8, w.player.ah)
        assertEquals(2560, w.player.ag)
        assertFalse(w.player.av)                    // fwd → av=false
        assertEquals(164, w.player.S)
        // ride-release: leave the rail x-range while S164
        w.player.setPositionPx(t.W[2] + 40, ry + 500)
        w.player.refreshBoxes()
        t.drawStyleF(w)
        assertNull(w.player.af)
        assertEquals(43, w.player.S)                // Z[1]!=1 → plain fall
        assertEquals(0, w.player.ag); assertEquals(0, w.player.ah)
    }

    @Test fun `S34 dismount-jump arm flings when Z1==1`() {
        val w = world()
        val t = w.npcs.first { it.ax == 10 && it.S == 34 &&
                                 it.Z[0] == 0 && it.Z[1] == 1 }
        val slope = ((t.W[3] - t.W[1]) shl 8) / (t.W[2] - t.W[0])
        val px = (t.W[0] + t.W[2]) / 2
        val ry = t.W[1] + ((slope * (px - t.W[0])) shr 8)
        w.player.setPositionPx(px, ry)
        w.player.refreshBoxes()
        val h = w.player.W[3] - w.player.W[1]
        w.player.setPositionPx(px, ry + h)
        w.player.refreshBoxes()
        w.player.setAnim(0)
        t.drawStyleF(w)
        assertSame(t, w.player.af)
        // walk off → Z[1]==1 arm: al-=40, i(157), ag=+8192, ah=-2560
        val al0 = w.player.al
        w.player.setPositionPx(t.W[2] + 40, ry + 500)
        w.player.refreshBoxes()
        w.player.al = al0                            // restore hang height
        t.drawStyleF(w)
        assertNull(w.player.af)
        assertEquals(157, w.player.S)
        assertEquals(8192, w.player.ag)
        assertEquals(-2560, w.player.ah)
    }
}
