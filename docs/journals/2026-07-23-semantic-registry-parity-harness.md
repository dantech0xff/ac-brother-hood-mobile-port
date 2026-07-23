# Semantic Registry + Parity Harness

**Date**: 2026-07-23 09:42 +08
**Severity**: High
**Component**: Semantic Registry, gameplay parity contracts
**Status**: Resolved

## What Happened

Chúng ta đã chốt slice static-only cho Semantic Registry và harness parity host-side: registry canonical được đẩy lên mức 12 class / 39 method / 41 field alias, còn `scripts/gameplay_parity_contracts.py` và `tests/test_gameplay_parity_contracts.py` biến các invariant đã suy ra thành contract chạy bằng `unittest`. Điểm quan trọng nhất là tách rõ selector thô khỏi runtime remap: sprite selector phải được quyết định từ raw type trước khi remap entity type. `k.q(int)` là contract lookup riêng; query `-1` phải return trước mọi validation hoặc truy cập player/slot state.

## The Brutal Truth

Phần khó chịu là ở chỗ chúng ta suýt biến một bản khôi phục tĩnh thành câu chuyện “parity runtime” nghe rất đẹp nhưng không đúng. Nếu không giữ ranh giới corpus vs synthetic thật chặt, entry này sẽ thành tài liệu tự huyễn hoặc. May mắn là review đã bắt được các lỗi đó trước khi chúng thành chuẩn giả.

## Technical Details

- 5 sprite registry branch đặc biệt đã được cố định: `bk`, `bl`, `bm`, `bj`, `bn`.
- `raw type 55` là waypoint registry riêng, không đi theo luồng entity slots.
- 4.286 entity records trong corpus đã được quét qua, cùng 16 fixture, 11 test, và toàn bộ basis được gắn nhãn rõ: `corpus`, `synthetic-derived-from-corpus`, `synthetic-source-contract`.
- Review đã phát hiện lỗi ordering của `k.q(-1)` và thiếu coverage cho selector đặc biệt; coverage mới khóa lại các đường đó để không tái phạm.

## What We Tried

Bản contract đầu chỉ giữ raw discriminator như một selector đại diện, nên chưa mô hình năm branch đọc selector từ field riêng. Sau audit, selector source/registry/value/field index được tách rõ; corpus oracle dùng cho dữ liệu đã decode, synthetic contract cho gap có chủ đích, và registry canonical chỉ giữ alias.

## Root Cause Analysis

Sai lầm gốc là muốn đơn giản hóa mô hình quá mức. Selector, remap, lookup precedence và provenance đều khác vai trò; gộp chúng lại sẽ tạo contract đẹp trên giấy nhưng lệch bytecode. Đây không phải lỗi nhỏ, mà là lỗi thiết kế biên độ tin cậy.

## Lessons Learned

- Không được gọi bất kỳ contract nào là “parity runtime” nếu target MIDlet chưa bao giờ chạy.
- Raw selector, runtime remap, và lookup precedence phải là ba bước riêng.
- Synthetic fixture phải được ghi nhãn thẳng tay; không được để nó giả làm corpus.
- Regression coverage phải bám vào defect review đã tìm ra, không chỉ vào happy path.

## Next Steps

Giữ các test này như hàng rào cho các slice sau và không mở rộng scope nếu chưa có corpus hoặc bằng chứng bytecode mới. Chủ sở hữu hiện tại: bộ static harness.
