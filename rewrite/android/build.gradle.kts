plugins {
    id("com.android.application")
    kotlin("android")
}

val gdxVersion = providers.gradleProperty("gdxVersion").get()

android {
    namespace = "com.acrebuild.spike"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.acrebuild.spike"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
    }

    // Release signing: create a keystore locally (never committed), then
    // pass the creds via env or gradle properties:
    //   RELEASE_STORE_FILE / RELEASE_STORE_PASSWORD /
    //   RELEASE_KEY_ALIAS / RELEASE_KEY_PASSWORD
    // Without them the release build stays unsigned.
    val releaseStoreFile = providers.environmentVariable("RELEASE_STORE_FILE")
        .orElse(providers.gradleProperty("RELEASE_STORE_FILE"))
    val releaseStorePassword = providers.environmentVariable("RELEASE_STORE_PASSWORD")
        .orElse(providers.gradleProperty("RELEASE_STORE_PASSWORD"))
    val releaseKeyAlias = providers.environmentVariable("RELEASE_KEY_ALIAS")
        .orElse(providers.gradleProperty("RELEASE_KEY_ALIAS"))
    val releaseKeyPassword = providers.environmentVariable("RELEASE_KEY_PASSWORD")
        .orElse(providers.gradleProperty("RELEASE_KEY_PASSWORD"))

    signingConfigs {
        if (releaseStoreFile.isPresent) {
            create("release") {
                storeFile = file(releaseStoreFile.get())
                storePassword = releaseStorePassword.orNull
                keyAlias = releaseKeyAlias.orNull
                keyPassword = releaseKeyPassword.orNull
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (releaseStoreFile.isPresent) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    sourceSets["main"].assets.srcDir("../generated")
    sourceSets["main"].jniLibs.srcDir("$buildDir/jniLibs")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

val natives = configurations.create("natives")

dependencies {
    implementation(project(":gdx"))
    implementation("com.badlogicgames.gdx:gdx-backend-android:$gdxVersion")
    listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64").forEach { abi ->
        add("natives", "com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-$abi")
    }
}

// The natives jars ship libgdx.so at the archive root; AGP only packages .so
// files under jniLibs/<abi>/, so unpack each ABI jar there.
tasks.register<Copy>("extractNatives") {
    into("$buildDir/jniLibs")
    natives.files.forEach { jar ->
        val abi = jar.name.removePrefix("gdx-platform-$gdxVersion-natives-").removeSuffix(".jar")
        from(zipTree(jar)) {
            include("*.so")
            into(abi)
        }
    }
}

tasks.named("preBuild") { dependsOn("extractNatives") }
