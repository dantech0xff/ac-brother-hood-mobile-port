package com.acrebuild.gdx

import com.acrebuild.core.Command
import com.acrebuild.core.CommittedTick
import com.acrebuild.core.DeterministicRandom
import com.acrebuild.core.InputQueue
import com.acrebuild.core.SavePort
import com.acrebuild.core.SaveSnapshot
import com.acrebuild.core.SpikeWorld
import com.acrebuild.core.TickEngine
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx

/**
 * game-app role: lifecycle orchestration and event routing only. Simulation
 * lives in core; rendering/input/audio/save stay behind their adapters.
 */
class SpikeGame : ApplicationAdapter() {

    companion object {
        const val TAG = "AcSpike"
        const val SEED = 0xA5B1C2D3L
    }

    private lateinit var renderer: PixelRenderer
    private lateinit var audio: AudioBridge
    private lateinit var save: SavePort
    private lateinit var engine: TickEngine
    private val inputQueue = InputQueue()
    private var latest: CommittedTick? = null

    override fun create() {
        renderer = PixelRenderer()
        renderer.create()
        audio = AudioBridge()
        audio.create()
        save = SaveBridge()
        engine = TickEngine(SpikeWorld(DeterministicRandom(SEED)), inputQueue)

        save.read()?.let { bytes ->
            try {
                val snap = SaveSnapshot.decode(bytes)
                engine.restore(snap)
                Gdx.app.log(TAG, "restored save tick=${snap.tickIndex}")
            } catch (e: IllegalArgumentException) {
                Gdx.app.log(TAG, "save unreadable, starting fresh: ${e.message}")
            }
        }

        Gdx.input.inputProcessor = InputQueueBridge(inputQueue, renderer)
        Gdx.app.log(TAG, "create: core ready, seed=$SEED")
    }

    override fun render() {
        val deltaMs = (Gdx.graphics.deltaTime * 1000f).toLong()
        val committed = engine.advance(deltaMs)
        for (tick in committed) {
            audio.execute(tick.commands)
            latest = tick
        }
        if (engine.isQuarantined()) {
            Gdx.app.log(TAG, "world quarantined after tick failure; halting sim")
        }
        renderer.render(latest)
    }

    override fun pause() {
        val snap = engine.world.snapshot()
        save.write(snap.encode())
        Gdx.app.log(TAG, "pause: saved tick=${snap.tickIndex}")
        super.pause()
    }

    override fun resume() {
        engine.resetAccumulator()
        Gdx.app.log(TAG, "resume: accumulator reset, no catch-up burst")
        super.resume()
    }

    override fun dispose() {
        pause()
        renderer.dispose()
        audio.dispose()
    }
}
