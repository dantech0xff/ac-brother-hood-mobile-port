package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.DeterministicRandom
import com.acrebuild.core.InputQueue
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
import com.acrebuild.core.ScriptTables
import com.acrebuild.core.TickEngine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx

/**
 * game-app role: lifecycle + fixed-step driver for the slice-1 port —
 * level 0 tiles/entities/player locomotion on the original 62 ms tick
 * (≤4 catch-up ticks per frame, same contract as TickEngine).
 */
class Level0Game : ApplicationAdapter() {

    companion object {
        const val TAG = "AcLevel0"
        const val SEED = 0xACB0C0DEL
        const val TICK_MS = 62L
        const val MAX_CATCHUP = 4
    }

    private lateinit var world: Level0World
    private lateinit var renderer: Level0Renderer
    private val inputQueue = InputQueue()
    private val save = SaveBridge("asbr-save.bin")
    private val audio = AudioBridge()
    private var accumulatorMs = 0L

    override fun create() {
        val levelBytes = Gdx.files.internal("level0/level0.aclv").readBytes()
        val level = LevelPack.load(levelBytes)
        val clips = HashMap<Int, Clip>()
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
        // pack-15 tilesets bound via k.ej[0..3]={11,10,12,10}; cells index
        // each tileset clip's composite-object space. Negated keys: entity
        // clips share this map via k.bi[] whose values 10/11 collide with
        // the tileset ids.
        for (ts in intArrayOf(10, 11, 12)) {
            clips[-ts] = Clip.load(
                Gdx.files.internal("level0/tileset-$ts/clip.acpk").readBytes())
        }
        // `j.g` level-string table — line-delimited, `\n`-escaped inside.
        val levelStrings = Gdx.files.internal("level0/strings-1.txt")
            .readString("UTF-8").split("\n")
            .filter { it.isNotEmpty() }
            .map { it.replace("\\n", "\n") }
        // `j.e(7)` claim-script table (k.by/bz/eH, k.java:6196).
        val scripts = ScriptTables.load(
            Gdx.files.internal("level0/scripts.bin").readBytes())
        world = Level0World(level, clips, DeterministicRandom(SEED),
            levelStrings = levelStrings, scripts = scripts,
            charmap = Gdx.files.internal("fonts/charmap.bin").readBytes())
        // e(false) (k.java:4045): load the /ASBR record at boot — nop
        // when no record exists (orig swallows the same path).
        save.read()?.let { world.saveLoad(it) }
        // j.c==0 + cu==0 (k.a() case 0 = R(), k.java:3949): the real
        // boot — splash logos → sound prompt → title → menu → play.
        world.stateL(0)
        renderer = Level0Renderer()
        renderer.create(world)
        audio.create()
        Gdx.input.inputProcessor = Level0InputBridge(inputQueue, renderer)
        Gdx.app.log(TAG, "level0: ${level.entities.size} records, " +
            "${level.cols}x${level.rows} cells, world=${level.worldW}x${level.worldH}px, " +
            "npcs=${world.npcs.size}")
    }

    override fun render() {
        accumulatorMs += (Gdx.graphics.deltaTime * 1000f).toLong()
        var ticks = 0
        while (accumulatorMs >= TICK_MS && ticks < MAX_CATCHUP) {
            val events = inputQueue.drainTo(inputQueue.headSequence())
            try {
                world.tick(events)
            } catch (t: Throwable) {
                Gdx.app.error(TAG, "tick failed, quarantining", t)
                accumulatorMs = 0
                break
            }
            accumulatorMs -= TICK_MS
            ticks++
        }
        // `z()`/`e.b()` audio commands (e.java:50-87): pack-17 SFX
        // WAVs (slots 10–33 set) play via AudioBridge; MIDI slots
        // (0–9,17,21,28) log-skip — undecoded on this pipeline.
        // `audioTrack` mirrors e.e.
        val commands = world.drainCommands()
        audio.execute(commands)
        for (c in commands) {
            when (c) {
                is com.acrebuild.core.Command.PlaySfx ->
                    Gdx.app.log(TAG, "audio: play track=${c.slot} (e.e=${world.audioTrack})")
                is com.acrebuild.core.Command.PersistBA -> {
                    save.write(c.record)
                    Gdx.app.log(TAG, "save: e(true) → ${c.record.size}B /ASBR")
                }
                is com.acrebuild.core.Command.QuitApp -> {
                    // j.c==11 → notifyDestroyed (j.java:218)
                    Gdx.app.log(TAG, "quit: notifyDestroyed via EXIT menu")
                    Gdx.app.exit()
                }
                else -> Unit
            }
        }
        if (accumulatorMs >= TICK_MS) accumulatorMs = 0 // drop backlog
        renderer.render(world)
    }

    override fun pause() {
        Gdx.app.log(TAG, "pause at tick=${world.tickIndex}")
        world.suspendAudio()                     // bG = kFi (k.java:5817)
        super.pause()
    }

    override fun resume() {
        accumulatorMs = 0
        world.resumeAudio()                      // bG>=0 → z(bG)/fi (k.java:5795)
        super.resume()
    }

    override fun dispose() {
        audio.dispose()
        renderer.dispose()
    }
}
