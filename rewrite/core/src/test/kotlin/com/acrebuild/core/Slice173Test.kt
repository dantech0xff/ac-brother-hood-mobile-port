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

class Slice173Test {
    // bi[35]=62 (k.java:266) — the ax35 scripted multi-tool spawns with
    // clip 62. The record was silently dropped at spawn only because
    // ENTITY_CLIP lacked the entry (same class of bug as the ax42 win
    // fuse — clipless vs unmapped). One level-0 record exists.
    @Test fun `ax35 record spawns with clip 62`() {
        val w = world()
        w.stateL(8)
        val e35 = w.npcs.filter { it.ax == 35 }
        assertEquals(1, e35.size)
        assertTrue(e35[0].clip != null)
    }

    // The record carries S=6 (grab-wave march arm) at ak=5816 with the
    // camera at kO=0: the L184-186 march arm culls it on its first tick —
    // `av=false && ak > kO+420 -> k.c(this)` (i.java:19813-19817). The
    // pre-cull L160 sweep hits nothing (W=[5832,161,5842,169] overlaps no
    // ax17/11/23/47/50/73 entity). Original behavior is identical: the
    // record exists for one tick then self-removes.
    @Test fun `ax35 self-culls off-camera on first tick like the original`() {
        val w = world()
        val e35 = w.npcs.firstOrNull { it.ax == 35 }!!
        keepLive(e35)               // P|16: the cull arm must run once
        val victims = w.npcs.filter { it.ax in intArrayOf(17, 11, 23, 47, 50, 73) }
        // claim suspension holds its dispatch frozen through the intro
        // phases; the first post-suspension tick runs the L184-186 cull.
        settleIntro(w)                 // I() L108 gate: tests run post-intro
        w.stateL(8)
        assertFalse(w.npcs.contains(e35))
        assertTrue(victims.none { it.deadRelease() })   // sweep hit nothing
    }
}
