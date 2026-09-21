# AC Rewrite — Kotlin + LibGDX

First implementation slice of the modern rebuild: the ADR **mandatory toolchain
spike**. It is not the game port — it proves the stack on a minimal
deterministic simulation driven by real converted assets.

## Toolchain pins

| Tool | Version |
|---|---|
| Gradle | 8.14.3 (wrapper) |
| Kotlin | 2.2.21 |
| Android Gradle Plugin | 8.13.2 |
| LibGDX | 1.14.2 |
| JDK | 17 (toolchain) |
| Android SDK | platform android-36.1, build-tools 36.0.0 |
| Emulator image | system-images;android-36;google_apis;x86_64 |

Repositories: `google()` + the GCS Maven Central mirror
(`maven-central.storage-download.googleapis.com/maven2`) first, `mavenCentral()`
as fallback — this egress IP gets HTTP 429 from Maven Central directly.

## Modules

| Module | Role (design doc section 6) |
|---|---|
| `core` | Pure Kotlin: fixed-point 8.8, Java-LCG-parity RNG (`j.a` range contract incl. MIN_VALUE quirk), sequenced `InputQueue`, 62 ms `TickEngine` (max-4 catch-up, dropped backlog, quarantine on tick failure), versioned `SaveSnapshot` + `SavePort`, committed-tick SHA-256 state hash |
| `gdx` | Adapters over LibGDX API: `PixelRenderer` (400x240 FBO, nearest, integer letterbox), `InputQueueBridge`, `AudioBridge` (deferred `PlaySfx`), `SaveBridge` (atomic local file), `SpikeGame` orchestration |
| `android` | Thin `AndroidApplication` launcher (landscape, immersive) |
| `lwjgl3` | Desktop dev launcher at exact 3x viewport |
| `ios` | RoboVM launcher scaffold; module only joins the build on macOS |
| `tools` | `convert_spike_assets.py` — offline converter into `generated/` with `provenance.json` (source+output SHA-256, transform `copy-v1`) |

Dependency rule kept: `core` never imports LibGDX or platform SDKs; adapters
consume committed-tick snapshots and commands only.

## Build and run

```bash
cd rewrite
python3 tools/convert_spike_assets.py   # regenerate generated/ assets
./gradlew :core:test                    # deterministic state-hash suite
./gradlew :android:assembleDebug        # APK -> android/build/outputs/apk/debug/
./gradlew :lwjgl3:run                   # desktop dev window (needs a display)
```

Android emulator on Linux KVM host:

```bash
sdkmanager "emulator" "system-images;android-36;google_apis;x86_64"
avdmanager create avd -n spike -k "system-images;android-36;google_apis;x86_64" -d pixel_6
# user must be in the kvm group: sudo gpasswd -a $USER kvm
emulator -avd spike -no-snapshot-save -gpu swiftshader_indirect &
adb install android/build/outputs/apk/debug/android-debug.apk
adb shell am start -n com.acrebuild.spike/.AndroidLauncher
```

Runtime log tag: `AcSpike` (`adb logcat -s AcSpike:*`).

## Spike behavior

- Actor steers toward held touch; bounce halves velocity; touch-down and bounce
  enqueue deferred `PlaySfx` executed by the audio adapter.
- Pause (HOME) writes `files/spike-save.bin` (49 bytes, schema v1); cold start
  restores it; resume resets the accumulator — suspended time never replays.
- Per-tick SHA-256 covers tick index, fixed positions/velocities, touch state,
  full RNG state and ordered commands.

## iOS status

Gate item 1's iOS half is **pending**: RoboVM builds require a macOS host with
Xcode. The `ios` module is scaffolded and excluded from the Linux build; run
`:ios:...` on a macOS session to complete the gate.
