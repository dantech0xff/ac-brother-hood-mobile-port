package com.acrebuild.spike

import android.os.Bundle
import com.acrebuild.gdx.Level0Game
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration

/** platform-android: lifecycle bridge only. No gameplay branches here. */
class AndroidLauncher : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val config = AndroidApplicationConfiguration().apply {
            useAccelerometer = false
            useCompass = false
            useGyroscope = false
            useImmersiveMode = true
        }
        initialize(Level0Game(), config)
    }
}
