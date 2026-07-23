# Tổng Quan Dự Án và PDR

## Bối cảnh

Dự án này phục hồi tĩnh JAR Java ME `assassins_creed_-_br_320x240_136711.jar` của
*Assassin's Creed: Brotherhood*. Tất cả kết luận phải xuất phát từ file, bytecode,
resource và script đã kiểm chứng. JAR không được thực thi dưới bất kỳ hình thức nào.

## Mục tiêu dự án

| Mục tiêu | Mô tả |
|---|---|
| Khôi phục mã | Lưu 12 class trong 3 góc nhìn: `structured`, `simple`, `fallback`, kèm `javap`. |
| Khôi phục tài nguyên | Giải mã pack, string table, sprite/font, audio, PNG IGP và catalog. |
| Giữ provenance | Mỗi artifact cần hash, nguồn gốc và mức tin cậy rõ ràng. |
| Khóa contract tĩnh | Kiểm tra một slice behavior bằng Python host-side, không thực thi target. |
| Thiết kế bản mới | Mô tả hướng Android/iOS hiện đại bằng tài liệu, không triển khai runtime mới ở đây. |

## Phạm vi

### Trong phạm vi

- Phân tích tĩnh JAR, manifest, bytecode và resource pack.
- Tái tạo package `reconstructed-project/`.
- Viết tài liệu kỹ thuật, PDR, kiến trúc, chuẩn mã nguồn và lộ trình.
- Ghi rõ phần nào là chứng minh, suy luận, hoặc chưa biết.
- Kiểm tra alias registry, decoded corpus oracle và source-contract fixtures ở
  host side.

### Ngoài phạm vi

- Chạy game hoặc kiểm tra hành vi bằng emulator.
- Sửa nội dung gốc của JAR.
- Giả định tên symbol hoặc schema khi chưa có bằng chứng.
- Khẳng định bản dựng hiện đại là buildable nếu chưa có pipeline tương ứng.
- Gọi source-contract harness là bằng chứng runtime/MIDlet hoặc full-game parity.

## Yêu cầu phi chức năng

- Static-only: không có bước động trong quy trình chuẩn.
- Reproducible: script phải có đầu vào rõ, output ổn định, và có thể lặp lại.
- Traceable: artifact cần hash, đường dẫn nguồn và chú thích provenance.
- Honest confidence: mọi rename hoặc schema map phải gắn mức tin cậy.
- No fake buildability: tài liệu không được mô tả artifact hiện tại như một game hoàn chỉnh có thể chạy ngay.

## Deliverables chính

| Artifact | Vai trò |
|---|---|
| `README.md` | Entry point của workspace. |
| `docs/codebase-summary.md` | Tóm tắt cây thư mục, script và artifact chính. |
| `docs/system-architecture.md` | Phân tách kiến trúc legacy recovered và design tương lai. |
| `docs/code-standards.md` | Quy chuẩn tài liệu, provenance, và mức tin cậy. |
| `docs/resource-formats.md` | Đặc tả format tài nguyên đã khôi phục. |
| `docs/save-format.md` | Byte map và hành vi RMS `/ASBR` đã khôi phục. |
| `docs/reverse-engineering-technical-analysis.md` | Quy trình phân tích tĩnh và tái lập kết quả. |
| `docs/modern-mobile-technical-design.md` | Thiết kế viết lại cho Android/iOS; không phải implementation. |
| `docs/project-roadmap.md` | Lộ trình theo phase hiện có. |
| `docs/semantic-registry-and-parity-harness.md` | Authority, command và extension rules cho contract slice. |
| `tests/` | `unittest` và fixture manifest cho các invariant tĩnh đã chọn. |
| `reconstructed-project/` | Bộ artifact phục hồi và inventory machine-readable. |

## Tiêu chí chấp nhận

- 12/12 class được bao phủ bởi structured, fallback và `javap`.
- Canonical alias overlay hiện có 12 class, 42 method và 41 field.
- 666/666 method có mặt trong inventory và bytecode.
- 37/37 JAR entry và 260/260 pack entry được kiểm kê.
- Corpus oracle re-decode đủ 8 pack, 4.286 record và 16/16 payload exact EOF.
- Parity Slice 2 giữ static-only, 30 fixtures, 30/30 tests pass và Phase 4 đã
  đóng sau verification/review.
- Tài liệu phải nêu rõ 1 stub structured ở `i.aV()` và đường fallback của nó.
- Tài liệu kiến trúc phải tách rõ:
  - sự thật đã khôi phục từ legacy code,
  - giả thuyết có mức tin cậy,
  - và đề xuất thiết kế mới.
- Tài liệu parity harness phải giữ ranh giới static-only, tách corpus oracle
  khỏi synthetic contracts, và không gọi đây là runtime parity.

## Đầu ra hiện có

`reconstructed-project/reconstruction-manifest.json` và
`reconstructed-project/inventory/summary.json` là hai nguồn số liệu chính cho
counts, coverage và resource totals.
`reconstructed-project/resources/levels-decoded/summary.json` và
`reconstructed-project/resources/levels-decoded/manifest.json` là nguồn canonical
cho level slot 0/7 output hiện tại.

## Quyết định kỹ thuật

- Nguồn sự thật cho resource format là bytecode + extractor, không phải suy đoán từ tên file.
- Nguồn sự thật cho class/method/field counts là `javap` và inventory sinh từ `javap`.
- Nguồn sự thật cho resource decode là metadata đã trích cùng hash file đầu ra.
- `scripts/java-me-semantic-aliases.json` là overlay working names cho 12 class,
  42 method và 41 field; các alias này không phải tên gốc.
- `scripts/gameplay_parity_contracts.py` là clean-room offline reference cho
  materialization/lookup, reference-identity entity store, render ordering,
  normal/slow integration, script activity và timeline scheduling/abort
  boundary. Current-event dispatch precedes due cursor advance; opcode branch
  dùng signed byte và exact-tick `108`/`113` có explicit abort outcome.
  `tests/` khóa 30-fixture parity slice bằng `unittest`.

## Ghi chú trạng thái

Phần tài liệu này mô tả project-level requirements hiện tại. Nó không được dùng để
đánh dấu các phase chưa xong là hoàn tất.
