# Tóm Tắt Codebase

Tài liệu này mô tả cấu trúc kho hiện tại sau khi đối chiếu trực tiếp artifact
khôi phục tĩnh. Nội dung chỉ phản ánh trạng thái hiện có;
không có giả định về runtime hay buildability.

## Tổng quan cây thư mục

| Khu vực | Nội dung |
|---|---|
| `README.md` | Entry point cấp workspace. |
| `docs/` | Bộ tài liệu tổng quan và đặc tả resource. |
| `plans/260722-1922-assassins-creed-reconstruction/` | Plan theo phase cho quá trình phục hồi tĩnh. |
| `plans/260722-2335-game-architecture-inference/` | Plan và scout report cho kiến trúc legacy nội suy. |
| `plans/260723-0852-semantic-registry-parity-harness/` | Follow-up track cho Semantic Registry và host-side gameplay parity contracts. |
| `plans/260723-1342-entity-render-timeline-parity/` | Completed Slice 2 cho entity store, render ordering, slow-time và timeline scheduling. |
| `plans/reports/` | Báo cáo kỹ thuật tóm tắt kết quả reverse-engineering. |
| `reconstructed-project/` | Artifact phục hồi, inventory và resource đã giải mã. |
| `scripts/` | Bảy script Python cho pack/sprite/level decode, package build, inventory, verifier và parity contracts. |
| `tests/` | `unittest` static-only cho parity contracts và corpus oracle. |

## Artifact trọng tâm

### `reconstructed-project/`

| Thành phần | Ý nghĩa |
|---|---|
| `src/structured/` | Bản decompile đọc dễ của 12 class gốc. |
| `src/simple/` | Bản tuyến tính, giữ `i.aV()` đầy đủ. |
| `src/fallback/` | Bản fallback cho luồng điều khiển khó. |
| `bytecode/` | `javap -c -p -s -constants -l -verbose` của từng class. |
| `inventory/` | `methods.json`, `calls.json`, `fields.json`, `field-accesses.json`, `dependencies.json`, `string-constants.json`, `semantic-aliases.json`, `summary.json`. |
| `resources/archive/` | Bản trích ZIP/class nguyên trạng từ JAR. |
| `resources/decoded/` | Pack, string table, sprite, audio, PNG IGP, semantic JSON và metadata. |
| `resources/sprites-decoded/` | Module/palette PNG và metadata/range/hash của 84 sprite input. |
| `resources/levels-decoded/` | JSON level records cho 8 pack, cộng summary và managed manifest. |
| `reconstruction-manifest.json` | Hash, counts, coverage, class metrics và resource totals. |
| `final-verification.md` | Audit tĩnh lịch sử, gồm code/resource/sprite managed-manifest checks. |
| `verification-report.json` | Kết quả verifier tĩnh hiện tại, gồm `ok`, `failures`, và cờ `game_execution_performed`. |

### `scripts/`

| Script | Vai trò |
|---|---|
| `extract-java-me-resource-packs.py` | Trích pack, decode LZMA-alone, nhận dạng payload và xuất semantic schema/JSON, audio, PNG IGP, catalog. |
| `decode-gameloft-sprites.py` | Parse 84 sprite binary, giữ section provenance và xuất/validate module PNG; không nhận JAR. |
| `decode-gameloft-level-records.py` | Parse tĩnh slot entity `0` và script `7`, giữ offset/provenance, kiểm exact EOF và xuất JSON/summary. |
| `build-static-reconstruction.py` | Sao chép ba view source, chạy `javap`, và sinh manifest package. |
| `inventory-java-me-bytecode.py` | Đọc output `javap` để sinh inventory call/field/string/dependency. |
| `verify-static-reconstruction.py` | Đối chiếu hash, ZIP entry, code views, inventory và mọi decoded payload mà không load class. |
| `gameplay_parity_contracts.py` | Clean-room Python reference cho materialization/lookup, reference-identity store, render ordering, Java normal/slow integration, script activity và timeline scheduling/abort boundary. |

### `tests/`

| Test | Vai trò |
|---|---|
| `test_gameplay_parity_contracts.py` | Khóa 30-fixture manifest, corpus oracle, synthetic source-contracts, và toàn bộ parity contracts. |

## Số liệu chính

| Chỉ số | Giá trị |
|---|---:|
| Class | 12 |
| Method | 666 |
| Field | 1.009 |
| JVM instruction / `Code` bytes | 114.642 / 237.112 |
| JAR entries | 37 |
| Pack entries | 260 |
| Nonempty / empty entries | 238 / 22 |
| LZMA / raw entries | 24 / 214 |
| String | 265 |
| Sprite binary | 84 |
| Sprite module | 4.376 |
| Pixel module / PNG variant | 4.371 / 12.102 |
| MIDI / WAV | 13 / 18 |
| IGP PNG | 29 |
| Semantic aliases | 12 class / 42 method / 41 field |
| Gameplay parity fixtures | 30: 12 corpus / 1 derived / 17 source-contract |
| Gameplay parity tests | 30/30 pass |
| Static verifier | `ok=true`, 0 failure, target execution false |
| Payload có semantic family | 238/238 non-empty |
| Signature-generic đã phân loại | 114/114 |
| Structured lines | 38.887 |
| Simple lines | 44.988 |
| Fallback lines | 128.038 |
| Structured stubs | 1 |

## Managed outputs

- `reconstructed-project/resources/sprites-decoded/summary.json`: 12.186 files,
  63.940.532 bytes, SHA-256 `bfc2298bbd774b21e75df88f3971d90f2375217bd7d825459b01a93c8f81abd9`.
- `reconstructed-project/resources/levels-decoded/summary.json`: 10 files,
  34.570.387 bytes, tree SHA `d2f71b3fbede3bc29c32df5bb666fcba46cb431b32e18bf17f66f665bbc2ff60`,
  manifest SHA `75ff246a5aa567a1f9cb7b8f2b58978305d88750f8bbef1589fe722c5f0f0648`.
- `reconstructed-project/verification-report.json`: `ok=true`, `failures=[]`,
  `analysis_mode=static-only`, `game_execution_performed=false`.

## Quan sát về nội dung

- `i` là class lớn nhất và chứa một method structured decompile bị nghẽn một phần.
- `k` và `j` là lõi engine/runtime, loader, state machine và render/input.
- `b` xử lý sprite/font/palette/pixel codec.
- `f` phụ trách IGP/cross-promotion.
- `g` và `i` nắm phần actor/entity, AI, collision và gameplay logic nặng nhất.

## Tài liệu liên quan

- [Báo cáo tổng quan dự án và PDR](./project-overview-pdr.md)
- [Chuẩn mã nguồn và tài liệu](./code-standards.md)
- [Kiến trúc hệ thống](./system-architecture.md)
- [Dossier kiến trúc legacy nội suy](./inferred-legacy-game-architecture.md)
- [Lộ trình dự án](./project-roadmap.md)
- [Đặc tả tài nguyên đã dịch ngược](./resource-formats.md)
- [Schema level entity/script records](./level-record-formats.md)
- [Đặc tả save RMS](./save-format.md)
- [Technical analysis](./reverse-engineering-technical-analysis.md)
- [Technical design Android/iOS](./modern-mobile-technical-design.md)
- [Semantic registry và gameplay parity harness](./semantic-registry-and-parity-harness.md)
- [Báo cáo kiểm định cuối](../plans/260722-1922-assassins-creed-reconstruction/reports/final-verification.md)
- [Bản reconstruction package](../reconstructed-project/README.md)

## Lưu ý

Các con số code/resource pack chính thức đọc từ
`reconstructed-project/reconstruction-manifest.json`; call/field/string inventory
từ `reconstructed-project/inventory/summary.json`; sprite-specific coverage và
managed hash từ `reconstructed-project/resources/sprites-decoded/summary.json`.
`plans/260722-1922-assassins-creed-reconstruction/reports/final-verification.md`
là audit result đối chiếu các
nguồn này với pinned JAR, fresh `javap`, fresh inventory và fresh extraction.
`scripts/gameplay_parity_contracts.py` và `tests/test_gameplay_parity_contracts.py`
đóng vai trò harness static-only riêng cho 30 fixture contract đã pin; chúng
không thay thế verifier tĩnh toàn kho và không chứng minh runtime parity.
