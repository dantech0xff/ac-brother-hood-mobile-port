pluginManagement {
    repositories {
        google()
        maven {
            // Maven Central rate-limits some egress IPs; the GCS mirror is first.
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        maven {
            url = uri("https://maven-central.storage-download.googleapis.com/maven2/")
        }
        mavenCentral()
    }
}

rootProject.name = "ac-rewrite"

include("core")
include("gdx")
include("android")
include("lwjgl3")

// The iOS launcher requires a macOS host (RoboVM + Xcode). It stays in the
// source tree but only joins the build there; see rewrite/ios.
val isMacOs = System.getProperty("os.name").lowercase().contains("mac")
if (isMacOs) {
    include("ios")
}
