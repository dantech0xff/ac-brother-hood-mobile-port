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

/** Slice 176 — all-mission pack conversion: `ec[aj]="/6".."/13"` packs
 *  (k.java:260) load through the same `I(i)`/`H(i)` grid layout with
 *  `ej[aj*4..+2]` tilesets (k.java:275) and `bh[aj]` layer gating
 *  (flying missions 1/4 have no ep layer). Asserts each converted pack
 *  parses, spawns its records, and ticks without exceptions. */
class Slice176Test {

    // (aj, cols, rows, entityCount) — converter output vs pack dims.
    private val PACKS = arrayOf(
        intArrayOf(0, 627, 55, 637),
        intArrayOf(1, 44, 600, 225),   // FLYING (bh==3)
        intArrayOf(2, 275, 110, 567),
        intArrayOf(3, 775, 70, 691),
        intArrayOf(4, 44, 625, 254),   // FLYING (bh==3)
        intArrayOf(5, 786, 63, 740),
        intArrayOf(6, 550, 70, 849),
        intArrayOf(7, 100, 102, 323),  // Cesare arena (aj==7 stat-gate)
    )

    @Test fun `all 8 mission packs decode and spawn`() {
        for ((aj, cols, rows, count) in PACKS) {
            val w = world(aj = aj)
            assertEquals(cols, w.level.cols, "level$aj cols")
            assertEquals(rows, w.level.rows, "level$aj rows")
            assertEquals(count, w.level.entities.size, "level$aj entities")
            assertEquals(aj, w.kAj, "level$aj kAj")
            assertTrue(w.npcs.isNotEmpty(), "level$aj spawned nothing")
        }
    }

    @Test fun `bh3 flying flag follows aj`() {
        for ((aj) in PACKS) {
            val w = world(aj = aj)
            assertEquals(aj == 1 || aj == 4, w.bh3, "level$aj bh3")
        }
    }

    @Test fun `each pack ticks 120 frames clean`() {
        // No-collision-exception smoke: flying packs (aj 1/4) run the
        // ground-rule collision until the `dL` stamp grid (k.java:4405)
        // is ported, so entities may legitimately cull — this only
        // asserts the sim advances without throwing.
        for ((aj) in PACKS) {
            val w = world(aj = aj)
            w.stateL(8)
            repeat(120) { w.tick(emptyList()) }
        }
    }

    @Test fun `loadMission swaps pack strings and entities`() {
        val w = world()
        w.stateL(8)
        w.loadMission(7)                                  // Cesare arena
        assertEquals(7, w.kAj)
        assertEquals(7, w.loadedAj)
        assertEquals(7, w.missionIndex)
        assertEquals(100, w.level.cols)
        assertEquals(102, w.level.rows)
        assertEquals(323, w.level.entities.size)
        assertTrue(w.npcs.isNotEmpty(), "level7 spawned nothing")
        assertFalse(w.bh3, "aj7 is grounded")
        assertTrue(w.drainCommands().any {
            it is Command.MissionLoaded && it.aj == 7 },
            "MissionLoaded not emitted")
    }

    @Test fun `loadMission flying pack gates bh3 and drops ep`() {
        val w = world()
        w.stateL(8)
        w.loadMission(1)
        assertEquals(1, w.loadedAj)
        assertTrue(w.bh3, "aj1 is flying (bh==3)")
        assertEquals(setOf(0, 2, 3), w.level.layers.map { it.id }.toSet())
        assertEquals(225, w.level.entities.size)
        assertTrue(w.npcs.isNotEmpty(), "level1 spawned nothing")
    }

    @Test fun `same-pack loadMission is a reload not a swap`() {
        val w = world()
        w.stateL(8)
        w.drainCommands()
        w.loadMission(0)                                  // already level0
        assertEquals(0, w.loadedAj)
        assertTrue(w.drainCommands().none { it is Command.MissionLoaded },
            "same-pack swap must not emit MissionLoaded")
        assertEquals(637, w.level.entities.size)
        assertTrue(w.npcs.isNotEmpty(), "respawn produced no entities")
    }

    @Test fun `stats survive a mission swap`() {
        // `I(aj)` reloads pack+entities but `kBA`/`kAp` are world statics
        // — the original keeps them across F(aj) (save bytes persist).
        val w = world()
        w.stateL(8)
        w.kBA[14] = 6                                     // unlock marker
        val n = w.npcs.size
        w.loadMission(5)
        assertEquals(6, w.kBA[14], "kBA wiped on mission swap")
        assertNotEquals(n, w.npcs.size,
            "level5 entity set should differ from level0")
    }

    @Test fun `strings follow the swapped pack`() {
        val w = world()
        w.stateL(8)
        val before = w.levelStrings.firstOrNull { it.isNotEmpty() }
        w.loadMission(2)
        val after = w.levelStrings.firstOrNull { it.isNotEmpty() }
        assertNotNull(after)
        assertNotEquals(before, after,
            "level2 strings should differ from level0")
    }

    @Test fun `visual layer gating matches bh table`() {
        // Layer ids: 0=et, 1=ep, 2=eu, 3=er. Flying packs (aj 1/4) carry
        // no ep entry (k.java:5244-5264 `I(i)` gate on bh==3); others
        // carry all four.
        for ((aj) in PACKS) {
            val w = world(aj = aj)
            val ids = w.level.layers.map { it.id }.toSet()
            if (aj == 1 || aj == 4) {
                assertEquals(setOf(0, 2, 3), ids,
                             "level$aj flying layers")
            } else {
                assertEquals(setOf(0, 1, 2, 3), ids,
                             "level$aj grounded layers")
            }
        }
    }
}
