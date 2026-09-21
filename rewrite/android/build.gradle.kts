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
        versionName = "0.1.0-spike"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
