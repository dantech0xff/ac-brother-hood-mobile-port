// platform-ios scaffold. This module is only included in settings.gradle.kts
// on macOS hosts; the toolchain spike verifies it in a dedicated macOS session
// (see plans/260921-0830-libgdx-toolchain-spike, gate item 1 second half).
plugins {
    kotlin("jvm")
}

val gdxVersion = providers.gradleProperty("gdxVersion").get()

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":gdx"))
    implementation("com.badlogicgames.gdx:gdx-backend-robovm:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-ios")
}
