package com.acrebuild.gdx

import com.acrebuild.core.Command
import com.acrebuild.gdx.SpikeGame
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound

/**
 * audio-runtime adapter: executes deferred commands from committed ticks.
 * It never decides slots or timing — core does — and its results never feed
 * back into gameplay within the same tick.
 *
 * Slot→asset map (docs/gameplay-mining/audio-sprite-usage.md, proven):
 * pack-17 `entry-0NN` ↔ slot N. WAV slots 10–16,18–20,23–25,29–33 play
 * via `Sound` (`audio/sfx-N.wav`); MIDI slots 0–9,17,21,28 play via
 * `Music` (`audio/music-N.ogg`, Gervill-rendered offline). Empty slots
 * 22,26,27 log-skip.
 *
 * Channel model (e.java, proven): ONE `javax.microedition.media.Player`,
 * `setLoopCount(1)` — a new play preempts whatever was sounding, and
 * `e.b()` stops it. `stopAll()` is that channel's close().
 */
class AudioBridge {

    private val sounds = HashMap<Int, Sound>()
    private val music = HashMap<Int, Music>()

    /** WAV-decoded SFX slots (proven set; 22/26/27 empty in the pack). */
    private val wavSlots = intArrayOf(
        10, 11, 12, 13, 14, 15, 16, 18, 19, 20, 23, 24, 25, 29, 30, 31, 32, 33)
    /** MIDI slots — rendered to OGG offline (slots 6/17/21/28 synthesize
     *  silence under Gervill's default bank — kept for parity). */
    private val midiSlots = intArrayOf(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 21, 28)

    private var currentMusic: Music? = null

    fun create() {
        for (n in wavSlots) {
            val f = Gdx.files.internal("audio/sfx-$n.wav")
            if (f.exists()) sounds[n] = Gdx.audio.newSound(f)
            else Gdx.app.log(SpikeGame.TAG, "audio: missing audio/sfx-$n.wav")
        }
        for (n in midiSlots) {
            val f = Gdx.files.internal("audio/music-$n.ogg")
            if (f.exists()) music[n] = Gdx.audio.newMusic(f)
            else Gdx.app.log(SpikeGame.TAG, "audio: missing audio/music-$n.ogg")
        }
    }

    /** `b()` — close the single channel: stop music + every SFX instance. */
    private fun stopAll() {
        currentMusic?.stop()
        currentMusic = null
        sounds.values.forEach { it.stop() }
    }

    fun execute(commands: List<Command>) {
        for (c in commands) {
            when (c) {
                is Command.PlaySfx -> {
                    stopAll()               // one Player — new play preempts
                    val s = sounds[c.slot]
                    if (s != null) {
                        s.play(1.0f)        // setLoopCount(1) — one shot
                    } else {
                        val m = music[c.slot]
                        if (m != null) {
                            m.isLooping = false
                            m.play()
                            currentMusic = m
                        } else {
                            Gdx.app.log(SpikeGame.TAG,
                                "audio: slot=${c.slot} empty/unloaded")
                        }
                    }
                    Gdx.app.log(SpikeGame.TAG, "audio: play slot=${c.slot}")
                }
                is Command.StopAudio -> stopAll()
                is Command.RequestSave -> Unit // handled by save adapter
                is Command.PersistBA -> Unit // handled by save adapter
                is Command.QuitApp -> Unit // handled by Level0Game
            }
        }
    }

    fun dispose() {
        stopAll()
        sounds.values.forEach { it.dispose() }
        music.values.forEach { it.dispose() }
        sounds.clear()
        music.clear()
    }
}
