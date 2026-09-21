package com.acrebuild.lwjgl3

import com.acrebuild.gdx.SpikeGame
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration

/** Desktop dev launcher: same shared app, host-side verification only. */
fun main() {
    val config = Lwjgl3ApplicationConfiguration().apply {
        setTitle("AC Rewrite Spike (dev)")
        setWindowedMode(1200, 720) // exact 3x of the 400x240 logical viewport
        useVsync(true)
    }
    Lwjgl3Application(SpikeGame(), config)
}
