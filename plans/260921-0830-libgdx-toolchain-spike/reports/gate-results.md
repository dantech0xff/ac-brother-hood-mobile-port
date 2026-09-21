---
report: ADR mandatory decision gate — toolchain spike results
date: 2026-09-21
status: partial-pass (Android + desktop verified; iOS pending macOS)
---

# Toolchain Spike Gate Results

Executed on a Linux x86_64 host with KVM, Android SDK platform 36.1,
Gradle 8.14.3, AGP 8.13.2, Kotlin 2.2.21, LibGDX 1.14.2, JDK 17.

| # | Gate item | Result | Evidence |
|---|---|---|---|
| 1 | Build/launch shared core on Android **and iOS** | **partial** | Android: `android-debug.apk` (4.3 MB, 4 ABI `libgdx.so`) installs and launches on emulator-5554 (AVD `spike`, API 36). Log: `AcSpike: create: core ready`. iOS: **pending** — module scaffolded (`rewrite/ios`, excluded off-macOS), RoboVM needs macOS + Xcode. |
| 2 | Pixel-perfect 400x240 render | **pass** | Scene renders into 400x240 RGBA8888 FBO, nearest-filtered, integer scale x4 letterboxed onto the emulator's 2400x1080 display; same scene on desktop at exact 3x (1200x720). Recovered `splash.png` + `actor.png` drawn from `generated/`. |
| 3 | Touch + suspend/resume | **pass** | `adb shell input tap/swipe` drives `PlaySfx` commands and steering via sequenced `InputQueue` consumed at tick boundary. `KEYCODE_HOME` → `pause` wrote save; foreground relaunch logged `resume: accumulator reset, no catch-up burst`. |
| 4 | Play converted SFX/music sample | **pass** | `generated/sfx.wav` (from decoded pack-17 `entry-020`, `copy-v1`, sha256 `5f951e42…`) played on each touch-down; logcat shows `audio: play slot=0`. |
| 5 | Sandbox save read/write | **pass** | `files/spike-save.bin` = 49 bytes (schema v1 + checksum) written on pause; `pm clear` → relaunch → fresh `create`; prior run restored `restored save tick=896`. |
| 6 | Deterministic state-hash outside UI | **pass** | `:core:test` 11/11 green: identical input script → identical SHA-256 sequence; different seed diverges; save/encode/decode/restore reproduces identical continuation hashes; RNG parity vectors incl. equal-bounds no-draw; input cutoff ordering; 4-tick catch-up + backlog drop; accumulator reset. |

## Findings during the spike

- `gdx-backend-android:1.14.2` is published as `.aar` and depends on
  `androidx.core` → `android.useAndroidX=true` required.
- gdx `natives-*` jars ship `libgdx.so` at jar root; packaged via the
  `extractNatives` copy task into `jniLibs/<abi>/`.
- Emulator required the `ubuntu` user in the `kvm` group
  (`sudo gpasswd -a ubuntu kvm`).

## Gate verdict

LibGDX + Kotlin is **accepted for the Android side**; the ADR's iOS gate half
remains open until a macOS session builds `rewrite/ios` (RoboVM) and repeats
items 2–6 there. Per the ADR fallback clause, if that session fails inside the
approved timebox, the stack moves to Godot 4.x before production gameplay code.

## What this spike deliberately does not prove

- Full gameplay parity — no entity FSM, timeline script, collision or level
  loading was ported.
- Performance under load — a single actor is not a stress test.
- App-store packaging, signing, or distribution readiness.
