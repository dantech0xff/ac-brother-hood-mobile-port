package com.acrebuild.gdx

import com.acrebuild.core.Command
import com.acrebuild.gdx.SpikeGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound

/**
 * audio-runtime adapter: executes deferred commands from committed ticks.
 * It never decides slots or timing — core does — and its results never feed
 * back into gameplay within the same tick.
 *
 * Slot→asset map (docs/gameplay-mining/audio-sprite-usage.md, proven):
 * pack-17 entry-0NN ↔ slot N. Slots 10–16,18–20,23–25,29–33 decode to WAV
 * (PCM u8 8kHz mono) and are the playable set here. Slots 0–9 + 17,21,28 are
 * MIDI — no decoder on this pipeline yet, so they log-skip (the core still
 * emits `PlaySfx` + `audioTrack`; the original issues `e.a(n,false)` the
 * same way). Slots 22,26,27 are legitimately empty in the original.
 */
class AudioBridge {

    private val sounds = HashMap<Int, Sound>()

    /** WAV-decoded SFX slots (proven set; 22/26/27 empty in the pack). */
    private val wavSlots = intArrayOf(
        10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 23, 24, 25, 29, 30, 31, 32, 33)

    fun create() {
        for (n in wavSlots) {
            val f = Gdx.files.internal("audio/sfx-$n.wav")
            if (f.exists()) sounds[n] = Gdx.audio.newSound(f)
            else Gdx.app.log(SpikeGame.TAG, "audio: missing audio/sfx-$n.wav")
        }
    }

    fun execute(commands: List<Command>) {
        for (c in commands) {
            when (c) {
                is Command.PlaySfx -> {
                    val s = sounds[c.slot]
                    if (s != null) {
                        s.play(1.0f)
                        Gdx.app.log(SpikeGame.TAG, "audio: play slot=${c.slot}")
                    } else {
                        // MIDI slot (music/stinger) or the empty slots —
                        // undecoded on this pipeline; the command itself
                        // was still issued (verbatim e.a(n,false)).
                        Gdx.app.log(SpikeGame.TAG,
                            "audio: slot=${c.slot} skipped (midi/undecoded)")
                    }
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
