plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    api(project(":core"))
    api("com.badlogicgames.gdx:gdx:${providers.gradleProperty("gdxVersion").get()}")
}
