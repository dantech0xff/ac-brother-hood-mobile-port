package com.acrebuild.ios

import com.acrebuild.gdx.SpikeGame
import com.badlogic.gdx.backends.iosrobovm.IOSApplication
import com.badlogic.gdx.backends.iosrobovm.IOSApplicationConfiguration
import org.robovm.apple.foundation.NSAutoreleasePool
import org.robovm.apple.uikit.UIApplication

/** platform-ios: lifecycle bridge only. Mirrors the Android launcher. */
class IOSLauncher : IOSApplication.Delegate() {
    override fun createApplication(): IOSApplication {
        val config = IOSApplicationConfiguration().apply {
            orientationLandscape = true
            orientationPortrait = false
            useAccelerometer = false
            useCompass = false
        }
        return IOSApplication(SpikeGame(), config)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val pool = NSAutoreleasePool()
            UIApplication.main(args, null, IOSLauncher::class.java)
            pool.close()
        }
    }
}
