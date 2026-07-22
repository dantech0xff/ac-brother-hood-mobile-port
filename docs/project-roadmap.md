# Lộ Trình Dự Án

Lộ trình này bám theo plan hiện có trong
`plans/260722-1922-assassins-creed-reconstruction/` và phản ánh trạng thái bàn
giao cuối đã được kiểm định tĩnh.

## Trạng thái phase

| Phase | Tên | Trạng thái |
|---|---|---|
| 1 | Baseline and Completeness Inventory | completed |
| 2 | Semantic Source Reconstruction | completed |
| 3 | Resource Format Reconstruction | completed |
| 4 | Reverse Engineering Technical Analysis | completed |
| 5 | Modern Mobile Technical Design | completed |
| 6 | Verification and Final Handoff | completed |

## Kết quả thực tế

### Đã hoàn tất

- Khóa artifact gốc ở trạng thái static-only.
- Ghi hash, counts, bytecode coverage và resource inventory.
- Tạo package phục hồi `reconstructed-project/`.
- Gắn semantic alias có evidence cho code và phân loại 238/238 payload không rỗng.
- Đặc tả pack, sprite, level entity/script, save, font/remap và lookup table.
- Giao technical analysis có lệnh tái tạo static-only.
- Giao technical design Android/iOS, không triển khai hoặc chạy game.
- Chạy audit cuối cho hash, counts, links, deterministic outputs và mọi claim;
  verifier đạt `ok: true`, `failures: []`.

## Deliverables theo phase

| Phase | Output chính |
|---|---|
| 1 | Manifest, counts, baseline evidence. |
| 2 | Four-view code package (ba decompile + exact bytecode) và symbol mapping. |
| 3 | Resource extraction, decode metadata, format documentation. |
| 4 | `docs/reverse-engineering-technical-analysis.md`, `docs/codebase-summary.md`, `README.md`. |
| 5 | `docs/modern-mobile-technical-design.md` và decision record liên quan. |
| 6 | Final handoff, kiểm chứng và đối soát toàn bộ artifact. |

## Quy tắc roadmap

- Không đánh dấu hoàn tất cho tài liệu chưa được tạo.
- Không đổi trạng thái phase nếu chưa có artifact tương ứng.
- Không ghi “buildable” khi mới chỉ có reverse-engineering hoặc design note.

## Hậu bàn giao tùy chọn

1. Nếu bắt đầu viết lại game, dùng technical design làm đầu vào cho một plan
   implementation riêng; việc đó nằm ngoài bài tập hiện tại.
2. Chỉ mở thêm vòng static research khi có evidence mới cho ba sprite module,
   opcode/mode còn opaque, slot-3 semantics hoặc JAD gốc.
3. Sau mọi thay đổi artifact, chạy lại toàn bộ static verifier trước khi công bố.
