package com.acrebuild.spike

import android.content.pm.ApplicationInfo
import android.os.Bundle
import com.acrebuild.gdx.Level0Game
import com.badlogic.gdx.Application
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
        // Debug builds keep the AcLevel0 info lines (device runs read them);
        // a release build logs errors only.
        val debuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        logLevel = if (debuggable) Application.LOG_INFO else Application.LOG_ERROR
    }
}
