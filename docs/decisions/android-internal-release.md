# ADR: Cấu hình bản Android phát hành nội bộ

- Status: Accepted
- Date: 2026-10-03
- Decision owner: rewrite architecture
- Implementation performed: yes (Phase 5 của
  `plans/261003-0700-parity-gap-closure-android-hardening/`)

## Context

Bản port chỉ phân phối nội bộ (sideload), không lên store: nội dung là IP gốc
(quyết định của người dùng, 2026-10-03). Máy nhận là điện thoại có tai thỏ và
điều hướng cử chỉ, máy tính bảng và máy gập chạy Android 16 (targetSdk 36).

## Decision

1. **Manifest** (`rewrite/android/src/main/AndroidManifest.xml`):
   `android:appCategory="game"` — với targetSdk 36, Android 16 chỉ giữ khóa
   hướng ngang/kích thước trên màn hình lớn cho app là game;
   `configChanges` thêm `smallestScreenSize|density|uiMode` để gập/mở hay đổi
   theme không tạo lại activity giữa ván. Hướng `sensorLandscape`, immersive
   mode giữ nguyên.
2. **Log**: build debug giữ log info của tag `AcLevel0` (device run đọc các
   dòng save/audio/mission); build release chỉ log lỗi
   (`AndroidLauncher`: `FLAG_DEBUGGABLE` → `LOG_INFO`, ngược lại
   `LOG_ERROR`).
3. **Build release nội bộ**: `./gradlew :android:assembleRelease` trong
   `rewrite/`. Ký bằng biến môi trường hoặc Gradle property
   `RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`,
   `RELEASE_KEY_PASSWORD` (`rewrite/android/build.gradle.kts`); thiếu chúng
   thì APK release không ký (`android-release-unsigned.apk`). Keystore nội bộ
   do người giữ bản build cất ngoài repo (không bao giờ commit, không đưa vào
   CI); máy nhận cài bằng `adb install` hoặc file APK đã ký.
4. **R8 tắt** (`isMinifyEnabled = false`): bản nội bộ không cần thu nhỏ; keep
   rule cho LibGDX/backends là rủi ro không đáng ở giai đoạn này. Xem lại nếu
   kích thước APK thành vấn đề.
5. **App id giữ `com.acrebuild.spike`**: đổi id sẽ làm bản cài nội bộ hiện có
   và file save của chúng thành mồ côi. Chỉ đổi khi người dùng yêu cầu.

## Consequences

- Phần cần thiết bị (tai thỏ, vùng cử chỉ hệ thống chồng lên pad trái/nút
  phải, landscape trên AVD màn hình lớn, gập/mở) chờ lần chạy emulator kế
  tiếp; chỉ đặt `systemGestureExclusionRects` nếu đo thấy cử chỉ hệ thống
  cướp input.
- Baseline hiệu năng phía container:
  `plans/261003-0700-parity-gap-closure-android-hardening/reports/perf-baseline.md`.

## Evidence

- APK release dump (`aapt dump xmltree`): `appCategory=0x0` (game),
  `configChanges=0x1ff0`, minSdk 24, targetSdk 36.
- `rewrite/android/src/main/kotlin/com/acrebuild/spike/AndroidLauncher.kt`.
