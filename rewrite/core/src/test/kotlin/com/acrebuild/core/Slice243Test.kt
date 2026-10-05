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

class Slice243Test {
    // slice 243 — `i.C()` live-react arm verbatim fix (i.java:5757-5797):
    // `aS.S ∈ {67,68,69,286,287}` → `g()` push; EVERY swing (incl. 287
    // and unlisted anims) lands `c(6,156,-1,-1)` + `k.A(13)`. Pre-fix the
    // port sent 287 into the dead arm and no-op'd unlisted anims.

    private fun soldier(w: Level0World): Entity {
        val e = Entity(11, null)
        e.aB = 100                               // live: aB > 0
        e.Z[0] = 0                               // not weakened
        e.setPositionPx(w.kO + 240, w.kP + 100)
        w.npcs.add(e)
        w.player.setPositionPx(w.kO + 200, w.kP + 100)
        w.player.refreshBoxes()
        return e
    }

    @Test fun `S287 on a live soldier reacts like any combo swing`() {
        val w = world()
        val e = soldier(w)
        w.player.S = 287
        assertTrue(e.hitReact(w))
        assertEquals(6, e.S, "c(6,156,-1,-1) → ax11 anim 6 (was S0 death arm)")
        assertEquals(0, e.Z[0], "victim stays unwounded in Z[0]")
    }

    @Test fun `S287 on a live ax73 hits the 156 react, not the death arm`() {
        val w = world()
        val e = soldier(w)
        // ax73 port via ctor ax — Entity(73) maps to the heavy-guard type
        val h = Entity(73, null)
        h.aB = 5000; h.Z[0] = 0                // aB > bu[au] → skips weaken
        h.setPositionPx(w.kO + 240, w.kP + 100)
        w.npcs.add(h)
        w.player.S = 287
        assertTrue(h.hitReact(w))
        assertEquals(156, h.S, "c(6,156) → ax73 anim 156 (was S164 death arm)")
        assertEquals(0, h.Z[0])
    }

    @Test fun `unlisted attacker anim still lands the shared react`() {
        val w = world()
        val e = soldier(w)
        e.S = 3                                  // any prior anim
        w.player.S = 112                         // not in {67,68,69,286,287}
        assertTrue(e.hitReact(w))
        assertEquals(6, e.S, "L112 shared react runs for every swing")
    }

    @Test fun `weakened Z0==2 short-circuits to i(6)`() {
        val w = world()
        val e = soldier(w)
        e.Z[0] = 2
        e.S = 144
        w.player.S = 67
        assertTrue(e.hitReact(w))
        assertEquals(6, e.S, "Lc2: Z[0]==2 → i(6), no push/react")
    }
}
