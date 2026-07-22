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
| Thiết kế bản mới | Mô tả hướng Android/iOS hiện đại bằng tài liệu, không triển khai runtime mới ở đây. |

## Phạm vi

### Trong phạm vi

- Phân tích tĩnh JAR, manifest, bytecode và resource pack.
- Tái tạo package `reconstructed-project/`.
- Viết tài liệu kỹ thuật, PDR, kiến trúc, chuẩn mã nguồn và lộ trình.
- Ghi rõ phần nào là chứng minh, suy luận, hoặc chưa biết.

### Ngoài phạm vi

- Chạy game hoặc kiểm tra hành vi bằng emulator.
- Sửa nội dung gốc của JAR.
- Giả định tên symbol hoặc schema khi chưa có bằng chứng.
- Khẳng định bản dựng hiện đại là buildable nếu chưa có pipeline tương ứng.

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
| `reconstructed-project/` | Bộ artifact phục hồi và inventory machine-readable. |

## Tiêu chí chấp nhận

- 12/12 class được bao phủ bởi structured, fallback và `javap`.
- 666/666 method có mặt trong inventory và bytecode.
- 37/37 JAR entry và 260/260 pack entry được kiểm kê.
- Tài liệu phải nêu rõ 1 stub structured ở `i.aV()` và đường fallback của nó.
- Tài liệu kiến trúc phải tách rõ:
  - sự thật đã khôi phục từ legacy code,
  - giả thuyết có mức tin cậy,
  - và đề xuất thiết kế mới.

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
  31 method và 41 field; các alias này không phải tên gốc.

## Ghi chú trạng thái

Phần tài liệu này mô tả project-level requirements hiện tại. Nó không được dùng để
đánh dấu các phase chưa xong là hoàn tất.
