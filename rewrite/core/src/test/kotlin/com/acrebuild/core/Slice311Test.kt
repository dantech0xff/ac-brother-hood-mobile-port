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

class Slice311Test {
    /** menuJc9's `G(164)=d(false)` entry arm must respawn the player at the
     *  pack record (k.java:4741+) — the briefing→gameplay path carried the
     *  previous position (level0's 85,940 on a fresh boot) → m3/m6 insta-fail. */
    @Test fun `briefing entry respawns at pack record`() {
        for (aj in listOf(3, 5, 6)) {
            val w = world(aj = aj)
            val p = w.player
            val spawn = w.level.playerSpawn()!!
            // stale carryover position (level0's record — the fresh-boot bug)
            p.setPositionPx(85, 940); p.setAnim(0)
            // drive the briefing LOADING screen (jC=9) past the jG==164 arm
            w.screenL(9)                                // stateL → jC=9 + jG=0
            repeat(170) { w.tick(emptyList()) }         // jG climbs past 164
            assertEquals(spawn.first, p.ak, "m$aj briefing entry lands pack spawn x")
            // ctor tail E() (slice 389): the fresh player settles on the
            // ground line — m3 +0, m5 -3, m6 -1 against the record's y.
            val settle = mapOf(3 to 0, 5 to 3, 6 to 1)
            assertEquals(spawn.second - settle.getValue(aj), p.al, "m$aj briefing entry lands pack spawn y (settled)")
        }
    }
}
