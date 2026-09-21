plugins {
    kotlin("jvm")
    application
}

val gdxVersion = providers.gradleProperty("gdxVersion").get()

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("com.acrebuild.lwjgl3.Lwjgl3LauncherKt")
}

sourceSets {
    main {
        // Converted immutable assets live next to the provenance manifest.
        resources.srcDir("../generated")
    }
}

dependencies {
    implementation(project(":gdx"))
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
    implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
}
