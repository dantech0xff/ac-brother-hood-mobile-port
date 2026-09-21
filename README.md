# Assassin's Creed Java ME Static Reconstruction

Kho lưu này là bộ tài liệu và artifact khôi phục tĩnh cho JAR `assassins_creed_-_br_320x240_136711.jar`.
MIDlet **không bao giờ được chạy**: không `java -jar`, không emulator, không simulator, không thiết bị thật.

## Mục tiêu

- Khôi phục cấu trúc mã, tài nguyên và hành vi suy ra từ bytecode.
- Giữ nguyên dấu vết provenance, hash và mức tin cậy cho từng kết luận.
- Mô tả một hướng thiết kế mobile hiện đại mà không giả vờ đây là bản build sẵn.

## Điểm vào nhanh

- [Báo cáo tổng quan dự án và PDR](docs/project-overview-pdr.md)
- [Tóm tắt codebase](docs/codebase-summary.md)
- [Chuẩn mã nguồn và tài liệu](docs/code-standards.md)
- [Kiến trúc hệ thống](docs/system-architecture.md)
- [Dossier kiến trúc legacy nội suy và sơ đồ runtime](docs/inferred-legacy-game-architecture.md)
- [Lộ trình dự án](docs/project-roadmap.md)
- [Đặc tả tài nguyên đã dịch ngược](docs/resource-formats.md)
- [Schema entity/script record của level](docs/level-record-formats.md)
- [Đặc tả save RMS `/ASBR`](docs/save-format.md)
- [Semantic symbol map](docs/symbol-map.md)
- [Semantic registry and gameplay parity harness](docs/semantic-registry-and-parity-harness.md)
- [Khôi phục method `i.aV()`](docs/i-av-reconstruction.md)
- [Báo cáo mức độ hoàn chỉnh](docs/reconstruction-completeness.md)
- [Technical analysis và hướng dẫn tái lập](docs/reverse-engineering-technical-analysis.md)
- [Technical design Android/iOS](docs/modern-mobile-technical-design.md)
- [ADR chọn framework mobile](docs/decisions/mobile-game-framework.md)
- [Báo cáo kiểm định tĩnh cuối](plans/260722-1922-assassins-creed-reconstruction/reports/final-verification.md)
- [Bản reconstruction package](reconstructed-project/README.md)
- [Rewrite toolchain spike (Kotlin + LibGDX)](rewrite/README.md) và
  [kết quả gate](plans/260921-0830-libgdx-toolchain-spike/reports/gate-results.md)

## Artifact chính

- `reconstructed-project/`
  - `src/structured/`: bản đọc dễ của 12 class gốc.
  - `src/simple/`: bản tuyến tính/goto, giữ đầy đủ luồng điều khiển.
  - `src/fallback/`: bản fallback theo instruction.
  - `bytecode/`: `javap` chính xác cho từng class.
  - `inventory/`: call graph, field access, dependencies, string constants.
  - `resources/`: archive gốc, pack đã giải mã và 12.102 module/palette PNG.
  - `reconstruction-manifest.json`: hash, counts, coverage, resource totals.
  - `final-verification.md`: snapshot audit tĩnh historical đã pass (`ok: true`).
- `scripts/`
  - extractor resource pack;
  - decoder sprite/module tĩnh;
  - decoder entity/script record của level;
  - builder cho package/source/`javap`;
  - bytecode inventory generator;
  - static integrity verifier;
  - host-side gameplay parity contracts (`scripts/gameplay_parity_contracts.py`) và `unittest` (`tests/test_gameplay_parity_contracts.py`).
  - host-side timeline opcode/completion contracts (`scripts/timeline_opcode_contracts.py`),
    manifest 15 fixture riêng và `unittest` (`tests/test_timeline_opcode_contracts.py`).

## Số liệu đã xác nhận

- 12 class gốc
- 666 method
- 1.009 field
- 114.642 JVM instruction / 237.112 `Code` bytes
- 37 JAR entries
- 260 indexed pack entries
- 24 LZMA entries, 214 raw entries
- 265 string
- 84 sprite binary, 4.376 module
- 4.371 pixel module → 12.102 PNG palette variant; 83 asset full, 1 partial
- 5 non-output sprite modules are accounted separately; `0x27f1` is now a high-confidence data-derived static recovery, not a runtime fill branch.
- 13 MIDI, 18 WAV, 29 PNG IGP
- 238/238 payload không rỗng có semantic family; không còn payload chỉ mang
  nhãn generic sau semantic pass
- 4.286 entity record và 144 script group/510 lane/2.366 event/3.705 instruction
  level parse exact EOF; slot 3 được chứng minh runtime-unused
- Semantic overlay: 12 class / 44 method / 41 field alias; đây là working names,
  không phải tên gốc
- Gameplay parity Slice 1/2 historical: manifest 30 fixture không đổi
  (12 corpus, 1 derived, 17 source-contract) và 30/30 test static-only pass
- Timeline opcode/completion Slice 3: manifest riêng 15 fixture
  (14 corpus, 1 synthetic-source-contract chỉ cho opcode `109` không xuất hiện
  trong corpus), 27/27 focused test và 57/57 full unittest discovery pass
- Extended timeline opcodes: 480 occurrence trên 3.705 instruction đã parse
- level decoder output: 10 files / 34.570.387 byte / tree SHA `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60` / manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`

## Ràng buộc an toàn

- Chỉ phân tích tĩnh.
- Không thêm hướng dẫn chạy MIDlet.
- Không coi decompile hoặc resource decode là bằng chứng buildable cho một game hiện đại.

## Trạng thái plan reconstruction historical

Đây là trạng thái của
[`plans/260722-1922-assassins-creed-reconstruction/`](plans/260722-1922-assassins-creed-reconstruction/plan.md),
không phải số phase của mọi đợt phân tích tiếp theo. Plan kiến trúc nội suy hiện
được theo dõi riêng tại
[`plans/260722-2335-game-architecture-inference/`](plans/260722-2335-game-architecture-inference/plan.md).

- Phase 1: completed
- Phase 2: completed
- Phase 3: completed
- Phase 4: completed
- Phase 5: completed
- Phase 6: completed

## Trạng thái Slice 2

Parity Slice 2 hiện được theo dõi tại
[`plans/260723-1342-entity-render-timeline-parity/`](plans/260723-1342-entity-render-timeline-parity/plan.md).

- Phase 1: completed
- Phase 2: completed
- Phase 3: completed
- Phase 4: completed
- Harness hiện là static-only, với 30/30 tests pass và verifier `ok=true`

## Trạng thái Slice 3

Timeline Opcode and Completion Parity Slice 3 được theo dõi tại
[`plans/260723-1820-timeline-opcode-completion-parity/`](plans/260723-1820-timeline-opcode-completion-parity/plan.md).
Plan đã `completed`; cả bốn phase, kiểm định độc lập và tài liệu bàn giao đều
đã được khóa sổ.

- Contract mô hình hóa completion `i.bI:()V` và extended opcode `100..114`.
- Direct field writes là immutable state transitions; lời gọi downstream được ghi
  thành ordered effect intentions, không được thực thi.
- 15 fixture riêng bao phủ 14 trường hợp corpus và opcode `109` synthetic-only.
- 27/27 focused test, 57/57 full unittest discovery và verifier `ok=true`.
- Toàn bộ kiểm chứng là static-only; không chạy target JAR, MIDlet, class,
  emulator, simulator hay thiết bị.

## Trạng thái rewrite track

Toolchain spike theo mandatory decision gate của
[`docs/decisions/mobile-game-framework.md`](docs/decisions/mobile-game-framework.md),
theo dõi tại
[`plans/260921-0830-libgdx-toolchain-spike/`](plans/260921-0830-libgdx-toolchain-spike/plan.md):

- `rewrite/` chứa Gradle project Kotlin + LibGDX `1.14.2` (core pure-Kotlin,
  adapters `gdx`, launchers `android`/`lwjgl3`, scaffold `ios` chỉ build trên
  macOS, `tools` converter và `generated/` assets có provenance).
- Gate items 2–6 pass trên Android emulator + desktop; core test 11/11.
- Gate item 1 còn nửa iOS: cần session macOS (RoboVM + Xcode) trước khi gọi
  stack là đạt đủ gate cho port gameplay production.
- Spike này không phải bản port game; entity FSM, timeline, collision và level
  loader chưa được chuyển.

## Trạng thái gameplay mining

Đào semantic gameplay phục vụ port, theo dõi tại
[`plans/260921-0929-gameplay-mining/`](plans/260921-0929-gameplay-mining/plan.md):

- [`docs/gameplay-mining/`](docs/gameplay-mining/): string corpus (toàn bộ
  script 8 mission + UI + cheat), level atlas (entity histogram + mission
  order), player mechanics, entity-type catalog, audio/sprite registry.
- [`docs/gameplay-design-document.md`](docs/gameplay-design-document.md): GDD
  tổng hợp — mission list, systems, content inventory cho rewrite.
