package com.acrebuild.gdx

import com.acrebuild.core.Clip
import com.acrebuild.core.DeterministicRandom
import com.acrebuild.core.InputQueue
import com.acrebuild.core.Level0World
import com.acrebuild.core.LevelPack
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
    private var accumulatorMs = 0L

    override fun create() {
        val levelBytes = Gdx.files.internal("level0/level0.aclv").readBytes()
        val level = LevelPack.load(levelBytes)
        val clips = HashMap<Int, Clip>()
        clips[0] = Clip.load(Gdx.files.internal("clips/clip0/clip.acpk").readBytes())
        clips[1] = Clip.load(Gdx.files.internal("clips/clip1/clip.acpk").readBytes())
        clips[7] = Clip.load(Gdx.files.internal("clips/clip7/clip.acpk").readBytes())
        clips[32] = Clip.load(Gdx.files.internal("clips/clip32/clip.acpk").readBytes())
        clips[3] = Clip.load(Gdx.files.internal("clips/clip3/clip.acpk").readBytes())
        clips[9] = Clip.load(Gdx.files.internal("clips/clip9/clip.acpk").readBytes())
        clips[54] = Clip.load(Gdx.files.internal("clips/clip54/clip.acpk").readBytes())
        clips[64] = Clip.load(Gdx.files.internal("clips/clip64/clip.acpk").readBytes())
        clips[26] = Clip.load(Gdx.files.internal("clips/clip26/clip.acpk").readBytes())
        clips[10] = Clip.load(Gdx.files.internal("clips/clip10/clip.acpk").readBytes())
        // pack-15 tilesets bound via k.ej[0..3]={11,10,12,10}; cells index
        // each tileset clip's composite-object space. Negated keys: entity
        // clips share this map via k.bi[] whose values 10/11 collide with
        // the tileset ids.
        for (ts in intArrayOf(10, 11, 12)) {
            clips[-ts] = Clip.load(
                Gdx.files.internal("level0/tileset-$ts/clip.acpk").readBytes())
        }
        world = Level0World(level, clips, DeterministicRandom(SEED))
        renderer = Level0Renderer()
        renderer.create(world)
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
        if (accumulatorMs >= TICK_MS) accumulatorMs = 0 // drop backlog
        renderer.render(world)
    }

    override fun pause() {
        Gdx.app.log(TAG, "pause at tick=${world.tickIndex}")
        super.pause()
    }

    override fun resume() {
        accumulatorMs = 0
        super.resume()
    }

    override fun dispose() {
        renderer.dispose()
    }
}
