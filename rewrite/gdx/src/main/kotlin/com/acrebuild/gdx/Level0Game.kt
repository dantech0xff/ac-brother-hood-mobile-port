package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.DeterministicRandom
import com.acrebuild.core.InputQueue
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
import com.acrebuild.core.MissionPack
import com.acrebuild.core.SaveEnvelope
import com.acrebuild.core.ScriptTables
import com.acrebuild.core.TickEngine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input

/**
 * game-app role: lifecycle + fixed-step driver for the slice-1 port —
 * level 0 tiles/entities/player locomotion on the original 62 ms tick.
 * The original loop (j.java:189-219, proven) is self-clocked: one tick
 * plus one repaint per iteration, then `Thread.sleep(max(1, B-elapsed))`
 * with B=62 — it never bursts catch-up ticks; a slow frame dilates the
 * sim instead. Mirrored here: at most one tick per render frame. The
 * sub-tick remainder is kept — discarding it quantizes the tick onto
 * the vsync cadence (every 4th frame at 60fps = 66.7ms ticks, ~7%
 * slow) — while backlog credit is clamped to one tick so a lag spike
 * buys at most one early tick, never a burst.
 */
class Level0Game : ApplicationAdapter() {

    companion object {
        const val TAG = "AcLevel0"
        const val SEED = 0xACB0C0DEL
        const val TICK_MS = 62L
        internal const val TICK_US = TICK_MS * 1000L

        /** Advance the µs tick accumulator by [deltaUs]; returns the new
         *  accumulator after resolving at most one pending tick, and
         *  whether a tick fires this frame. Remainder-keep holds the
         *  62ms cadence on a vsync-quantized frame clock; the clamp
         *  caps catch-up credit at a single tick (self-clocked
         *  semantics: the original's post-tick sleep absorbs overshoot
         *  the same way, it just can't under-run). */
        internal fun tickAccStep(accUs: Long, deltaUs: Long): Pair<Long, Boolean> {
            val acc = accUs + deltaUs
            return if (acc >= TICK_US) {
                minOf(acc - TICK_US, TICK_US) to true
            } else {
                acc to false
            }
        }
    }

    private lateinit var world: Level0World
    private lateinit var renderer: Level0Renderer
    private val inputQueue = InputQueue()
    private val firstPackTilesetDir = "level0"
    private val save = SaveBridge("asbr-save.bin", SaveEnvelope.SCHEMA_KBA_V1)
    private val audio = AudioBridge(TAG)
    private var accumulatorUs = 0L
    private lateinit var driver: TickDriver

    /** `I(aj)` pack provider (k.java:5244, proven): one mission pack
     *  = level ACLV + `k.d(1+aj)` strings + `j.e(7)` scripts — the
     *  `G(i)` stages collapsed into one synchronous read (the original
     *  spreads them across load-screen frames). */
    private fun missionPack(aj: Int): MissionPack {
        val level = LevelPack.load(
            Gdx.files.internal("level$aj/level$aj.aclv").readBytes())
        /* k.d(1+aj) → j.g(i): pack string table indexed by slot —
         * empty entries are real (j.g returns null on a 0-length span),
         * so keep them: filtering shifted every later index and made
         * bubble/dialog lookups read the wrong line (fix proven on m0:
         * slot 24 must read the guard's line, not "YES."). Only the
         * trailing newline artifact is dropped. */
        val strings = Gdx.files.internal("level$aj/strings-${aj + 1}.txt")
            .readString("UTF-8").split("\n")
            .let { if (it.last().isEmpty()) it.dropLast(1) else it }
            .map { it.replace("\\n", "\n") }
        val scripts = ScriptTables.load(
            Gdx.files.internal("level$aj/scripts.bin").readBytes())
        return MissionPack(level, strings, scripts)
    }

    /** The `G(4..7)` tileset arms (k.java:4775-4830, proven): on a pack
     *  swap, the mission's `ej` tileset clips replace the old ones in
     *  the shared clip map (negative keys), then the atlas repacks. */
    private fun loadMissionTilesets(clips: HashMap<Int, Clip>) {
        // `et` (id==0) carries the null sentinel 0, not a real tileset —
        // exclude it; a real `tileset-0` exists only on level5's eu.
        val ids = world.level.layers.filter { it.id != 0 }
            .map { it.tilesetClip }.toSet()
        clips.keys.removeAll { it < 0 && -it !in ids }
        for (ts in ids) {
            clips[-ts] = Clip.load(
                Gdx.files.internal(
                    "level${world.loadedAj}/tileset-$ts/clip.acpk")
                    .readBytes())
        }
        renderer.rebuildTilesets()
    }

    /** The shared clip map: base clips load once in [create]; mission
     *  tilesets (negative keys) are swapped by [boot] and [loadMissionTilesets]. */
    private val clips = HashMap<Int, Clip>()

    override fun create() {
        clips[0] = Clip.load(Gdx.files.internal("clips/clip0/clip.acpk").readBytes())
        clips[91] = Clip.load(Gdx.files.internal("clips/clip91/clip.acpk").readBytes())
        clips[92] = Clip.load(Gdx.files.internal("clips/clip92/clip.acpk").readBytes())
        clips[93] = Clip.load(Gdx.files.internal("clips/clip93/clip.acpk").readBytes())
        clips[94] = Clip.load(Gdx.files.internal("clips/clip94/clip.acpk").readBytes())
        clips[12] = clips[94]!!   // z[12] = entry-012 — alias under its z[] index
        clips[95] = Clip.load(Gdx.files.internal("clips/clip95/clip.acpk").readBytes())   // A[3]
        // pack-2 A[] bank (j.a("/2"), k.java:4058-4075): A[0] bg, A[1] title
        // art, A[4] dialog panel/portraits, A[5] N() load icon.
        clips[96] = Clip.load(Gdx.files.internal("clips/clip96/clip.acpk").readBytes())   // A[0]
        clips[97] = Clip.load(Gdx.files.internal("clips/clip97/clip.acpk").readBytes())   // A[1]
        clips[98] = Clip.load(Gdx.files.internal("clips/clip98/clip.acpk").readBytes())   // A[4]
        clips[99] = Clip.load(Gdx.files.internal("clips/clip99/clip.acpk").readBytes())   // A[5]
        clips[1] = Clip.load(Gdx.files.internal("clips/clip1/clip.acpk").readBytes())
        clips[7] = Clip.load(Gdx.files.internal("clips/clip7/clip.acpk").readBytes())
        clips[32] = Clip.load(Gdx.files.internal("clips/clip32/clip.acpk").readBytes())
        clips[3] = Clip.load(Gdx.files.internal("clips/clip3/clip.acpk").readBytes())
        clips[9] = Clip.load(Gdx.files.internal("clips/clip9/clip.acpk").readBytes())
        clips[54] = Clip.load(Gdx.files.internal("clips/clip54/clip.acpk").readBytes())
        clips[11] = Clip.load(Gdx.files.internal("clips/clip11/clip.acpk").readBytes())   // z[11] G() art
        clips[64] = Clip.load(Gdx.files.internal("clips/clip64/clip.acpk").readBytes())
        clips[26] = Clip.load(Gdx.files.internal("clips/clip26/clip.acpk").readBytes())
        clips[10] = Clip.load(Gdx.files.internal("clips/clip10/clip.acpk").readBytes())
        clips[48] = Clip.load(Gdx.files.internal("clips/clip48/clip.acpk").readBytes())
        clips[45] = Clip.load(Gdx.files.internal("clips/clip45/clip.acpk").readBytes())
        clips[47] = Clip.load(Gdx.files.internal("clips/clip47/clip.acpk").readBytes())
        clips[31] = Clip.load(Gdx.files.internal("clips/clip31/clip.acpk").readBytes())
        clips[62] = Clip.load(Gdx.files.internal("clips/clip62/clip.acpk").readBytes())
        clips[25] = Clip.load(Gdx.files.internal("clips/clip25/clip.acpk").readBytes())
        clips[29] = Clip.load(Gdx.files.internal("clips/clip29/clip.acpk").readBytes())
        clips[60] = Clip.load(Gdx.files.internal("clips/clip60/clip.acpk").readBytes())
        clips[61] = Clip.load(Gdx.files.internal("clips/clip61/clip.acpk").readBytes())   // z[61] rope dots
        clips[74] = Clip.load(Gdx.files.internal("clips/clip74/clip.acpk").readBytes())   // z[74] touch pad
        clips[73] = Clip.load(Gdx.files.internal("clips/clip73/clip.acpk").readBytes())   // z[73] medal icons
        clips[51] = Clip.load(Gdx.files.internal("clips/clip51/clip.acpk").readBytes())
        clips[63] = Clip.load(Gdx.files.internal("clips/clip63/clip.acpk").readBytes())
        clips[20] = Clip.load(Gdx.files.internal("clips/clip20/clip.acpk").readBytes())
        clips[21] = Clip.load(Gdx.files.internal("clips/clip21/clip.acpk").readBytes())
        clips[38] = Clip.load(Gdx.files.internal("clips/clip38/clip.acpk").readBytes())
        clips[42] = Clip.load(Gdx.files.internal("clips/clip42/clip.acpk").readBytes())
        clips[46] = Clip.load(Gdx.files.internal("clips/clip46/clip.acpk").readBytes())
        clips[39] = Clip.load(Gdx.files.internal("clips/clip39/clip.acpk").readBytes())   // z[39] jc20 icons
        clips[52] = Clip.load(Gdx.files.internal("clips/clip52/clip.acpk").readBytes())   // ax29 Cesare boss (bi[29]=52)
        clips[30] = Clip.load(Gdx.files.internal("clips/clip30/clip.acpk").readBytes())   // ax41 knockable prop (bi[41]=30)
        clips[6] = Clip.load(Gdx.files.internal("clips/clip6/clip.acpk").readBytes())     // ax10 zones (bi[10]=6 — load-valid, zero-pixel modules)
        clips[5] = Clip.load(Gdx.files.internal("clips/clip5/clip.acpk").readBytes())     // ax8 knife projectile (bi[8]=5)
        clips[12] = Clip.load(Gdx.files.internal("clips/clip12/clip.acpk").readBytes())   // k.dA HUD indicator (T())
        clips[59] = Clip.load(Gdx.files.internal("clips/clip59/clip.acpk").readBytes())   // ax8 boss-knife param (op111)
        clips[13] = Clip.load(Gdx.files.internal("clips/clip13/clip.acpk").readBytes())   // ax21/ax48 (bi=13)
        clips[14] = Clip.load(Gdx.files.internal("clips/clip14/clip.acpk").readBytes())   // ax22 capture zone
        clips[15] = Clip.load(Gdx.files.internal("clips/clip15/clip.acpk").readBytes())   // ax26
        clips[16] = Clip.load(Gdx.files.internal("clips/clip16/clip.acpk").readBytes())   // ax25
        clips[23] = Clip.load(Gdx.files.internal("clips/clip23/clip.acpk").readBytes())   // ax66 platform
        clips[28] = Clip.load(Gdx.files.internal("clips/clip28/clip.acpk").readBytes())   // ax51 crate
        clips[44] = Clip.load(Gdx.files.internal("clips/clip44/clip.acpk").readBytes())   // ax31
        audio.create()
        boot()
    }

    /** Build mission 0 from the durable save and enter the boot screens —
     *  at start-up and from the fatal screen's restart. */
    private fun boot() {
        val firstPack = missionPack(0)
        val level = firstPack.level
        // pack-15 tilesets bound via k.ej[aj*4..+2] (k.java:275); cells
        // index each tileset clip's composite-object space. Negated keys:
        // entity clips share this map via k.bi[] whose values collide
        // with the tileset ids.
        // firstPackTilesetDir already carries the "level0" prefix; the
        // et collision layer (id==0) has tilesetClip=0 = null sentinel —
        // exclude it or the load asks for a nonexistent tileset-0.
        clips.keys.removeAll { it < 0 }
        for (ts in level.layers.filter { it.id != 0 }
                .map { it.tilesetClip }.toSet()) {
            clips[-ts] = Clip.load(
                Gdx.files.internal(
                    "${firstPackTilesetDir}/tileset-$ts/clip.acpk")
                    .readBytes())
        }
        world = Level0World(level, clips, DeterministicRandom(SEED),
            levelStrings = firstPack.levelStrings,
            scripts = firstPack.scripts,
            charmap = Gdx.files.internal("fonts/charmap.bin").readBytes(),
            aj = 0, packLoader = { aj -> missionPack(aj) })
        // e(false) (k.java:4045): load the /ASBR record at boot — nop
        // when no record exists (orig swallows the same path).
        save.read()?.let { world.saveLoad(it) }
        // j.c==0 + cu==0 (k.a() case 0 = R(), k.java:3949): the real
        // boot — splash logos → sound prompt → title → menu → play.
        world.stateL(0)
        if (::renderer.isInitialized) renderer.dispose()
        renderer = Level0Renderer()
        renderer.create(world)
        Gdx.input.inputProcessor = Level0InputBridge(inputQueue, renderer)
        // BACK is the right soft key (slice 368), never "close the app" —
        // the original has no hardware-key path at all.
        Gdx.input.setCatchKey(Input.Keys.BACK, true)
        driver = TickDriver(world::tick, world::drainCommands, ::onTickFailed)
        accumulatorUs = 0
        Gdx.app.log(TAG, "level0: ${level.entities.size} records, " +
            "${level.cols}x${level.rows} cells, world=${level.worldW}x${level.worldH}px, " +
            "npcs=${world.npcs.size}")
    }

    /** The tick-failure boundary's fatal hook: stop the channel once; the
     *  world is quarantined and [render] shows the fatal screen. */
    private fun onTickFailed(t: Throwable) {
        Gdx.app.error(TAG, "tick failed — world quarantined", t)
        audio.stopAll()
    }

    override fun render() {
        if (driver.quarantined) {
            // Fatal screen: its only action restarts from the durable save
            // (boot screens → title). Input still drains so a tap is seen.
            val events = inputQueue.drainTo(inputQueue.headSequence())
            if (events.any { it.type == InputQueue.Type.UP }) { boot(); return }
            renderer.renderFatal()
            return
        }
        // µs accumulator — ms-truncating `deltaTime` lost ~0.3-0.7ms per
        // frame (~40ms/s), which periodically landed a tick one vsync
        // late = the visible micro-hitch. Remainder-keep holds the 62ms
        // cadence; the clamp caps credit at one tick (no catch-up
        // bursts, matching the self-clocked original).
        val step = tickAccStep(accumulatorUs,
            (Gdx.graphics.deltaTime * 1_000_000f).toLong())
        accumulatorUs = step.first
        val commands = if (step.second)
            driver.tick(inputQueue.drainTo(inputQueue.headSequence()))
        else driver.drainIdle()
        execute(commands)
        if (driver.quarantined) renderer.renderFatal() else renderer.render(world)
    }

    /** Run a committed tick's deferred commands through the adapters. */
    private fun execute(commands: List<com.acrebuild.core.Command>) {
        // `z()`/`e.b()` audio commands (e.java:50-87): AudioBridge plays
        // the pack-17 SFX WAVs and the MIDI slots (0–9,17,21,28) from
        // their offline OGG renders (`audio/music-N.ogg`).
        // `audioTrack` mirrors e.e.
        audio.execute(commands)
        for (c in commands) {
            when (c) {
                is com.acrebuild.core.Command.PlaySfx ->
                    Gdx.app.log(TAG, "audio: play track=${c.slot} (e.e=${world.audioTrack})")
                is com.acrebuild.core.Command.StopAudio ->
                    Gdx.app.log(TAG, "audio: e.b() stop channel")
                is com.acrebuild.core.Command.MissionLoaded -> {
                    @Suppress("UNCHECKED_CAST")
                    loadMissionTilesets(world.clips as HashMap<Int, Clip>)
                    Gdx.app.log(TAG, "mission ${c.aj}: ${world.level.entities.size} " +
                        "records, ${world.level.cols}x${world.level.rows}, " +
                        "npcs=${world.npcs.size}")
                }
                is com.acrebuild.core.Command.PersistBA -> {
                    // A failed write keeps the previous durable save (the
                    // protocol never touches it before the new one is
                    // forced); the next e(true) flush retries.
                    try {
                        save.write(c.record)
                        Gdx.app.log(TAG, "save: e(true) → ${c.record.size}B /ASBR")
                    } catch (e: Exception) {
                        Gdx.app.error(TAG, "save: e(true) failed, previous save kept", e)
                    }
                }
                is com.acrebuild.core.Command.QuitApp -> {
                    // j.c==11 → notifyDestroyed (j.java:218)
                    Gdx.app.log(TAG, "quit: notifyDestroyed via EXIT menu")
                    Gdx.app.exit()
                }
                else -> Unit
            }
        }
    }

    /** `hideNotify` → `k.c()` (k.java:5817-5836): input latches cleared, a
     *  `cd[6]` script paused, the channel stopped. Whatever the world has
     *  queued — a `PersistBA` above all — is executed now, synchronously:
     *  the process may not come back. */
    override fun pause() {
        Gdx.app.log(TAG, "pause at tick=${world.tickIndex}")
        if (!driver.quarantined) {
            world.hideNotify()
            execute(driver.drainIdle())
        }
        audio.stopAll()
        super.pause()
    }

    /** `showNotify` → `k.d()` (k.java:5767-5813): in play it opens the pause
     *  menu (`l(14)`). The tick clock restarts from zero — the time spent in
     *  the background is not owed. */
    override fun resume() {
        accumulatorUs = 0
        if (!driver.quarantined) {
            world.showNotify()
            execute(driver.drainIdle())
        }
        super.resume()
    }

    override fun dispose() {
        if (::driver.isInitialized) execute(driver.drainIdle())   // last flush
        audio.dispose()
        renderer.dispose()
    }
}
