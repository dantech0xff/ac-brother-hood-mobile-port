package com.acrebuild.gdx

import com.acrebuild.core.Command
import com.acrebuild.gdx.SpikeGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

/**
 * audio-runtime adapter: executes deferred commands from committed ticks.
 * It never decides slots or timing — core does — and its results never feed
 * back into gameplay within the same tick.
 */
class AudioBridge {

    private val sounds = HashMap<Int, Sound>()

    fun create() {
        sounds[0] = Gdx.audio.newSound(Gdx.files.internal("sfx.wav"))
        sounds[1] = sounds[0]!! // bounce reuses the converted sample for the spike
    }

    fun execute(commands: List<Command>) {
        for (c in commands) {
            when (c) {
                is Command.PlaySfx -> {
                    sounds[c.slot]?.play(1.0f)
                    Gdx.app.log(SpikeGame.TAG, "audio: play slot=${c.slot}")
                }
                is Command.RequestSave -> Unit // handled by save adapter
                is Command.PersistBA -> Unit // handled by save adapter
                is Command.QuitApp -> Unit // handled by Level0Game
            }
        }
    }

    fun dispose() {
        sounds.values.forEach { it.dispose() }
        sounds.clear()
    }
}
