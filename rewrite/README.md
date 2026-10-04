# AC Brotherhood — bản port Kotlin + LibGDX

`rewrite/` là bản remake của game J2ME *Assassin's Creed Brotherhood*
(320×240) bằng Kotlin + LibGDX: port core gốc (tick 62 ms, fixed-point 8.8,
clip-as-FSM, dispatch entity ax 0–80, 8 mission, flying/chase/boss) trên
asset đã decode từ JAR. Cả 8 mission chơi được trên Android; kế hoạch hiện
hành là `plans/261003-0700-parity-gap-closure-android-hardening/plan.md`
(sửa sai lệch parity + hardening, **phát hành nội bộ, không lên store**).

JAR gốc không bao giờ được chạy (xem `AGENTS.md`); chỉ code trong `rewrite/`
được build và chạy trên emulator/thiết bị.

## Toolchain

| Công cụ | Phiên bản |
|---|---|
| Gradle | 8.14.3 (wrapper) |
| Kotlin | 2.2.21 |
| Android Gradle Plugin | 8.13.2 |
| LibGDX | 1.14.2 |
| JDK | 17+ (bytecode target 17) |
| Android SDK | `platforms;android-36` (compileSdk/targetSdk 36), `build-tools;35.0.0` (mặc định của AGP 8.13.2), minSdk 24 |
| Emulator image | `system-images;android-36;google_apis;x86_64` |

Dựng môi trường: `rewrite/tools/bootstrap-dev-env.sh` (cài JDK 17 và các gói
SDK còn thiếu, ghi `local.properties`). Repository Maven: `google()` + mirror
GCS của Maven Central trước, `mavenCentral()` sau — một số IP egress bị Maven
Central trả 429.

## Module

| Module | Vai trò |
|---|---|
| `core` | Kotlin thuần, không import LibGDX. Game: `Level0World` (vòng frame `k.a()`/`k.I()`, máy trạng thái màn hình `j.c`, HUD, menu, dialog, camera, save `kBA`), `PlayerFsm` (`g.e()`), `NpcFsm` (`i.I()` và các ax), `Entity` (`i`), `Pad` (các word input `eK/eL/bB/bC/eM`), `InputQueue` (event có sequence: chạm + BACK), `SaveEnvelope` (container v1), `Clip`/`LevelPack`/`MissionPack`/`ScriptTables`/`FontClip` (đọc asset), `DeterministicRandom` (LCG parity Java), `Trig`, `FixedPoint`. Spike: `TickEngine`, `SpikeWorld`, `SaveSnapshot`. |
| `gdx` | Adapter LibGDX. Game: `Level0Game` (vòng render, accumulator µs ≤1 tick/frame, lifecycle `pause/resume/dispose` = `k.c()/k.d()`), `TickDriver` (cách ly tick lỗi → màn hình lỗi, chạm để khởi động lại từ save), `Level0Renderer` (canvas 400×240, letterbox), `Level0InputBridge` (chạm → toạ độ logic; BACK/ESC → event BACK), `AudioBridge` (`PlaySfx`/nhạc), `SaveBridge` + `SaveStore` (ghi atomic, `.bak`, khôi phục). Spike: `SpikeGame`, `PixelRenderer`, `InputQueueBridge`. |
| `android` | Launcher `AndroidLauncher` → `Level0Game` (landscape, immersive). Application id giữ `com.acrebuild.spike`. |
| `lwjgl3` | Launcher desktop → `Level0Game`. |
| `ios` | Scaffold RoboVM (còn chạy `SpikeGame`); chỉ vào build trên macOS. |
| `tools` | `convert_slice1.py` (sinh pack runtime trong `generated/` từ `reconstructed-project/resources/`), `convert_spike_assets.py`, `bootstrap-dev-env.sh`. |

Asset runtime nằm trong `generated/` (đã commit: `clips/`, `level0..7/`,
`audio/`, `fonts/`, `provenance.json` ghi SHA-256 nguồn và đích).

## Contract đã pin

- Tick 62 ms, **tự định nhịp**: tối đa 1 tick mỗi frame, accumulator giữ phần
  dư, không catch-up — ADR `docs/decisions/tick-cadence-self-clocked.md`.
- Thứ tự frame như `k.a()`: input của frame đi qua `InputQueue`, chốt ở biên
  tick; `PlaySfx` hoãn tới sau tick.
- Input gốc chỉ có chạm (`k.keyPressed` rỗng). BACK của Android = chạm vào
  soft-key phải (pill BACK/SKIP/NEXT) trên màn hình có pill đó; nơi khác BACK
  không làm gì và không bao giờ đóng app (slice 368).
- Save: record `kBA` 320 B (160 × LE16) trong container v1 `ACBA` (version,
  schema, revision, độ dài, SHA-256), ghi atomic qua file tạm + `.bak`; file
  raw 320 B cũ vẫn đọc được — ADR `docs/decisions/save-policy.md`.
- Một exception trong tick cách ly world: không tick tiếp, không save, dừng
  audio, hiện màn hình lỗi.

## Build, test, chạy

```bash
cd rewrite
./gradlew :core:test :gdx:test          # ~1.7k test, gồm bot chơi hết các mission
./gradlew :android:assembleDebug        # APK -> android/build/outputs/apk/debug/
./gradlew :lwjgl3:run                   # cửa sổ desktop (cần display)
```

Đặt `LANG=C.UTF-8`: tên test Kotlin có ký tự ngoài ASCII. Cùng với verifier
và `unittest` ở gốc repo, đây là bốn gate CI (`.github/workflows/ci.yml`).

Test trong `core/src/test`: `Slice*Test` cho từng slice port (mỗi cái trích
dòng nguồn gốc), và các capstone — bot chạy route qua từng mission trên world
thật. Bot chỉ được đổi route/thời điểm, không bao giờ chỉnh kẻ địch hay vá
state để qua.

Emulator Android trên host Linux có KVM:

```bash
sdkmanager "emulator" "system-images;android-36;google_apis;x86_64"
avdmanager create avd -n acport -k "system-images;android-36;google_apis;x86_64" -d pixel_6
emulator -avd acport -no-snapshot-save -gpu swiftshader_indirect &
adb install android/build/outputs/apk/debug/android-debug.apk
adb shell am start -n com.acrebuild.spike/.AndroidLauncher
adb logcat -s AcLevel0:*
```

Save nằm ở `files/asbr-save.bin` (+ `.bak`) trong thư mục app.

Bản phát hành nội bộ (không lên store):

```bash
export RELEASE_STORE_FILE=/đường/dẫn/keystore RELEASE_STORE_PASSWORD=… \
       RELEASE_KEY_ALIAS=… RELEASE_KEY_PASSWORD=…   # keystore không bao giờ commit
./gradlew :android:assembleRelease   # thiếu biến ký → android-release-unsigned.apk
```

R8 tắt, app id giữ `com.acrebuild.spike`, release chỉ log lỗi — ADR
`docs/decisions/android-internal-release.md`.

## Lịch sử: toolchain spike

`rewrite/` bắt đầu là spike bắt buộc của ADR
`docs/decisions/mobile-game-framework.md`: một mô phỏng tất định nhỏ
(`SpikeGame`, log tag `AcSpike`, save 49 byte `ACRS`, `TickEngine` ≤4
catch-up) để chứng minh stack. Nửa Android của gate đã xong; nửa iOS cần host
macOS có Xcode và thuộc track iOS. Code spike còn trong `core`/`gdx` cho
scaffold iOS; game không dùng nó.
