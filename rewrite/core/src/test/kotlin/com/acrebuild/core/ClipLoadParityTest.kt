package com.acrebuild.core

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The app loads its clips from a hand-written list in `Level0Game.create()`
 * (gdx module); the unit tests build their own map in `world()`. The two
 * drifted: clips 4/19/27/35/36/40/71 were converted and listed in the test
 * helper but never loaded by the app, so the flyers, springboards, decor
 * interactives, every `a(24, 40, …)` effect and the ax61 multi-tool spawned
 * clipless on a device while every test passed (slice 390).
 *
 * This guard parses the production loader and checks it against the
 * converter output: every `generated/clips/clipN` must be loaded, and every
 * clip an entity table can bind for a shipped record that exists in pack 3
 * must be among them.
 */
class ClipLoadParityTest {
    private val clipsDir = File("../generated/clips")
    private val loader = File("../gdx/src/main/kotlin/com/acrebuild/gdx/Level0Game.kt")

    private fun converted(): Set<Int> =
        clipsDir.listFiles()!!.filter { it.isDirectory && it.name.startsWith("clip") }
            .map { it.name.removePrefix("clip").toInt() }.toSet()

    private fun loadedByApp(): Set<Int> {
        val src = loader.readText()
        val ids = Regex("""clips\[(\d+)]\s*=\s*Clip\.load""").findAll(src)
            .map { it.groupValues[1].toInt() }.toMutableSet()
        // the `for (id in intArrayOf(…)) clips[id] = Clip.load(…clip$id…)` loop
        Regex("""for \(id in intArrayOf\(([^)]*)\)\)\s*\n\s*clips\[id]""").findAll(src).forEach { m ->
            m.groupValues[1].split(',').map { it.trim().toInt() }.forEach { ids += it }
        }
        return ids
    }

    @Test fun `the app loads every converted clip`() {
        val missing = converted() - loadedByApp()
        assertTrue(missing.isEmpty(), "generated/clips present but never loaded by Level0Game.create(): $missing")
    }

    @Test fun `every clip a shipped record binds and pack 3 has is converted and loaded`() {
        val pack3 = File("../../reconstructed-project/resources/decoded/pack-3").listFiles()!!
            .mapNotNull { Regex("""entry-(\d+)""").find(it.name)?.groupValues?.get(1)?.toInt() }.toSet()
        val bound = HashSet<Int>()
        for (aj in 0..7) {
            val w = world(aj = aj)
            for (f in w.level.entities) {
                if (f.isEmpty() || f.size < 7 || f[0] == 0 || f[0] == 25 || f[0] == 55) continue
                Level0World.entityClipIndex(f[0], f)?.let { bound += it }
            }
        }
        bound += listOf(0, 16, 15, 42, 46)              // player, ax25 flyer + glider, k.D / k.E
        val needed = bound.filter { it in pack3 }.toSet()
        assertTrue(needed.isNotEmpty())
        assertEquals(emptySet(), needed - converted(), "bound + in pack 3 but not converted")
        assertEquals(emptySet(), needed - loadedByApp(), "bound + converted but not loaded by the app")
    }
}
