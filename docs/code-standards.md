# Chuẩn Mã Nguồn và Tài Liệu

## Nguyên tắc chung

- Chỉ ghi điều đã kiểm chứng từ code, bytecode, manifest, inventory hoặc script.
- Không nói artifact hiện tại là buildable nếu chỉ mới là tài liệu hoặc decompile.
- Giữ phạm vi thay đổi nhỏ, phù hợp với contract hiện có.
- Ưu tiên KISS, YAGNI và DRY.

## Quy chuẩn bằng chứng

| Loại nội dung | Cần có |
|---|---|
| Count / metric | Nguồn số liệu rõ: manifest, inventory hoặc script output. |
| Rename / semantic map | Mức tin cậy: `proven`, `high-confidence`, `inferred`, `unknown`. |
| Binary format | Offset, width, endianness, điều kiện lỗi và consumer call site. |
| Resource decode | Hash file đầu ra và mô tả codec/marker. |
| Thiết kế tương lai | Ghi rõ là đề xuất, không phải implementation hiện có. |

## Static-only safety

- Không hướng dẫn chạy JAR, MIDlet, emulator hay simulator.
- Không đề xuất runtime động như bước kiểm chứng mặc định.
- Không thêm lối tắt fake data để làm tài liệu trông hoàn chỉnh hơn.

## Provenance và hash

- Mọi artifact trích xuất nên có hash riêng khi khả thi.
- Nên ghi rõ artifact nào là nguồn gốc và artifact nào là dẫn xuất.
- Nếu một kết luận phụ thuộc suy luận, phải ghi là suy luận.

## Cách viết tài liệu

- Dùng tiêu đề ngắn, section rõ và bảng thay vì đoạn văn dài.
- Tách `legacy facts` và `future design` thành hai phần khác nhau.
- Giữ đường link nội bộ tương đối, chỉ trỏ tới file thật trong repo.
- Không để TODO mơ hồ; nếu chưa biết thì ghi rõ `chưa biết` hoặc `inferred`.

## Quy chuẩn script

- Script phải có input rõ và output rõ.
- Script phải tái chạy được từ source hiện có.
- Không phụ thuộc vào side effect ngầm.
- Không mô tả output như bằng chứng runtime nếu script chỉ phân tích tĩnh.

## Quy tắc đặt tên

- File mới dùng kebab-case khi có thể.
- Tên module và artifact phản ánh vai trò thực tế, không phản ánh giả định chưa chứng minh.
- Trong tài liệu kỹ thuật, giữ nguyên ký hiệu obfuscated khi đó là nguồn duy nhất có thật.

## Kiểm tra trước khi chốt

1. Đọc file gốc trước khi cập nhật tài liệu liên quan.
2. So khớp link, path và số liệu với file thật.
3. Kiểm tra lại các con số sau khi thay đổi.
4. Xác nhận không làm tăng độ tin cậy giả cho phần chưa chứng minh.

## Chuẩn cho project này

- `docs/resource-formats.md` là tài liệu chuẩn về format binary.
- `reconstructed-project/reconstruction-manifest.json` là nguồn số liệu canonical cho recovery.
- `reconstructed-project/inventory/summary.json` là nguồn canonical cho call/field/string inventory.
- `scripts/java-me-semantic-aliases.json` là input canonical cho alias; schema máy
  dùng đúng enum confidence `proven`, `high-confidence`, `inferred`, `unknown` và
  bắt buộc khai báo `aliases_are_original_names=false`.
- `reconstructed-project/resources/sprites-decoded/summary.json` là nguồn
  canonical cho coverage/PNG/managed hash riêng của sprite decoder.
- `reconstructed-project/verification-report.json` là kết quả audit; verifier
  ghi trạng thái fail/incomplete trước khi làm việc để không giữ pass cũ.
- `README.md` chỉ là entry point, không thay thế tài liệu kỹ thuật.
