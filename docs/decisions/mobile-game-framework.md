# ADR: Framework cho bản rewrite mobile

- Status: Accepted for technical design
- Date: 2026-07-22
- Decision owner: rewrite architecture
- Implementation performed: no

## Context

Game recovered là 2D action-platformer offline, logic/state machine lớn, nominal
scheduler cadence khoảng 62 ms nhưng update theo accepted paint với mixed
frame-count/wall-delta semantics, viewport 400×240, custom sprite/font/level
packs, 34 audio slot và save 512 byte. Bản mới phải dùng chung gameplay code
trên Android và iOS, chuyển đổi resource offline và giữ deterministic behavior.
Fixed-step 62 ms là intentional rewrite decision cần parity fixtures, không phải
fact rằng runtime gốc đã có fixed simulation tick.

Đây không phải ứng dụng UI-heavy hoặc project 3D. Framework cần cho phép kiểm
soát loop/render/input/data pipeline trực tiếp, không buộc chuyển gameplay sang
scene editor trước khi parity được chứng minh.

## Decision drivers

1. Shared Android/iOS gameplay code.
2. Fixed-step deterministic simulation.
3. Custom binary asset importer và pixel-accurate 2D rendering.
4. Code-first migration từ Java/bytecode recovered.
5. Offline-first, không network/IGP dependency.
6. License/framework cost đơn giản.
7. Testability ngoài renderer và khả năng replay/state hash.

## Options

| Stack | Fit | Điểm mạnh | Rủi ro chính | Kết luận |
|---|---|---|---|---|
| **LibGDX + Kotlin** | Rất cao | Code-first, loop/input/assets/audio trực tiếp, Android+iOS, Apache 2.0 | iOS backend/toolchain cần spike sớm | **Chọn** |
| Godot 4.x | Cao | MIT, 2D/editor/export tốt, fixed physics process | Migration engine/scene-first; C# iOS không phải đường ưu tiên | Fallback chính |
| Flutter + Flame | Trung bình | Một Dart codebase, tốt nếu có app shell/meta UI lớn | Thêm UI abstraction không cần thiết cho action core | Chỉ chọn nếu product thành hybrid app/game |
| Unity | Trung bình về kỹ thuật, thấp về tổng thể | Tooling/editor/multi-platform mạnh | Runtime/editor nặng, business/licensing surface lớn | Không chọn |
| Native Android + iOS | Thấp | Platform control tối đa | Duplicate logic, parity và maintenance cost cao | Không chọn |

## Decision

Dùng **Kotlin + LibGDX** cho shared game/content/runtime modules. Android và iOS
chỉ có launcher/lifecycle bridge mỏng. Core simulation không tham chiếu API
Android, iOS hoặc renderer.

Không pin vĩnh viễn version trong ADR; implementation kickoff phải chọn một
release ổn định đang được hỗ trợ, khóa version/checksum và ghi lại toolchain. Tại
ngày evidence `2026-07-22`, tài liệu setup chính thức ghi latest stable là
`1.14.2`, tạo module Android/iOS và yêu cầu Xcode trên macOS cho iOS. Vì vậy
`1.14.2` là ứng viên đầu cho spike, không phải version được chấp nhận trước khi
gate chạy qua. Backend iOS chính thức dùng RoboVM; arm64 simulator cần được thử
riêng, gồm cả lựa chọn MetalANGLE được tài liệu nêu.

Tài liệu chính thức tham khảo:

- [LibGDX overview và license](https://libgdx.com/)
- [Application lifecycle](https://libgdx.com/wiki/app/the-life-cycle)
- [Graphics/frame timing](https://libgdx.com/wiki/graphics/graphics)
- [Input handling](https://libgdx.com/wiki/input/input-handling)
- [AssetManager](https://libgdx.com/wiki/managing-your-assets)
- [Audio](https://libgdx.com/wiki/audio/audio)
- [File handling](https://libgdx.com/wiki/file-handling)
- [Android/iOS project setup](https://libgdx.com/wiki/start/import-and-running)
- [Project generation và stable version](https://libgdx.com/wiki/start/project-generation)
- [iOS/RoboVM launcher](https://libgdx.com/wiki/app/starter-classes-and-configuration)

## Consequences

### Positive

- Mapping tự nhiên từ Java ME code-first sang Kotlin modules.
- Core logic có thể unit-test với clock/input/RNG giả lập mà không render.
- Custom converter không bị ràng buộc bởi asset database/scene serialization của
  một editor lớn.
- Một pipeline và một content schema phục vụ cả hai platform.

### Negative

- Nhóm phải tự xây/import animation, level/script và provenance tooling.
- iOS build/debug pipeline có maturity/risk cao hơn Godot export workflow.
- Không có visual scene editor mặc định; editor chỉ được thêm khi schema ổn định.

## Mandatory decision gate

Trước khi triển khai gameplay quy mô lớn, thực hiện một **toolchain spike của bản
rewrite** — không chạy JAR gốc — để chứng minh cùng một sample converted asset có
thể:

1. build/launch bằng shared core trên Android và iOS;
2. render pixel-perfect 400×240 lên viewport hiện đại;
3. nhận touch và suspend/resume đúng;
4. phát một converted SFX/music sample;
5. đọc/ghi save sandbox;
6. chạy deterministic state-hash test ngoài UI.

Nếu iOS backend không vượt gate trong timebox đã duyệt, chuyển sang **Godot 4.x
GDScript** trước khi viết gameplay production. Không chuyển sang hai native
codebase.

Official fallback references:

- [Godot license](https://godotengine.org/license/)
- [Godot 2D](https://docs.godotengine.org/en/stable/tutorials/2d/index.html)
- [Physics/idle processing](https://docs.godotengine.org/en/stable/tutorials/scripting/idle_and_physics_processing.html)
- [Android export](https://docs.godotengine.org/en/latest/tutorials/export/exporting_for_android.html)
- [iOS export](https://docs.godotengine.org/en/stable/tutorials/export/exporting_for_ios.html)

## Revisit conditions

Reopen ADR chỉ khi có bằng chứng mới:

- iOS spike fail do blocker của backend/toolchain;
- yêu cầu product đổi thành UI-heavy launcher/meta app;
- shared Kotlin core không đạt deterministic/performance gate;
- quyền sử dụng content hoặc store requirement làm thay đổi platform scope.
