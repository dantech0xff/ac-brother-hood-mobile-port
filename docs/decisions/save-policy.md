# ADR: Chính sách save và container cho bản port

- Status: Accepted
- Date: 2026-10-03
- Decision owner: rewrite architecture
- Implementation performed: yes (Phase 3 của
  `plans/261003-0700-parity-gap-closure-android-hardening/`)

## Context

Bản gốc ghi đúng một record RMS `/ASBR` id 1: buffer `k.bA` 512 byte, ghi
nguyên khối bằng `setRecord(1, bA, 0, 512)` ở `k.e(true)`, đọc ở `k.e(false)`
lúc boot (`docs/save-format.md` §1). Không có magic, version, checksum hay
backup. Checkpoint giữa mission chỉ xuống RMS khi một sự kiện menu/progression
sau đó gọi `e(true)`; nếu process chết, checkpoint giữa mission mất — đúng như
bản gốc (device Run-26).

Bản port giữ `kBA` là `IntArray(160)`: `kBA[i]` mang giá trị ở offset byte `i`
của `bA` gốc (short ở offset chẵn, byte cờ ở offset lẻ/chẵn đơn lẻ), và
`Level0World.saveFlush` ghi mỗi slot thành LE16 tại byte `2i..2i+1` — record
320 B. Mọi offset gốc đang dùng (0..132, `docs/save-format.md` §2) nằm trong
160 slot. Trước ADR này, `SaveBridge` thay file không atomic (xóa file đích rồi
rename) và không kiểm tra gì khi đọc: một file bị cắt ngang vẫn được load thành
progress.

`docs/modern-mobile-technical-design.md` §16 yêu cầu chọn một contract: resume
chính xác (`CanonicalWorldSnapshot`) hoặc policy thô giống bản gốc.

## Decision

1. **Policy thô, giống bản gốc.** Chỉ progression + settings (record `kBA`)
   được lưu, đúng ở các call site `saveFlush` đã port (`e(true)`); không lưu
   snapshot thế giới. Resume chính xác là track riêng, cần ADR khác.
2. **Payload giữ là record `kBA` 320 B** (160 × LE16), không đổi sang `bA` 512
   byte. Map: offset gốc `i` ↔ byte `2i..2i+1` của payload (giá trị LE16).
3. **Container v1** (`core/SaveEnvelope.kt`), little-endian:

   | offset | size | field |
   |---:|---:|---|
   | 0 | 4 | magic `ACBA` |
   | 4 | 2 | envelope version = 1 |
   | 6 | 2 | payload schema (1 = `kBA` v1; 2 = spike `ACRS`) |
   | 8 | 8 | `saveRevision` (tăng 1 mỗi lần ghi) |
   | 16 | 4 | payload length, ≤ 4096 (kiểm trước khi cấp phát) |
   | 20 | n | payload |
   | 20+n | 32 | SHA-256 trên byte `[0, 20+n)` |

   Decode từ chối: thiếu byte, sai magic, version/schema lạ, length vượt
   giới hạn hoặc không khớp kích thước file, digest sai.
4. **Migration**: file không header dài đúng 320 B là record `kBA` cũ (v0):
   load được (revision 0) và được ghi lại thành v1 ở lần flush kế tiếp. Chỉ áp
   dụng cho schema `kBA`.
5. **Giao thức ghi** (`gdx/SaveStore.kt`): ghi `<name>.<rev>.tmp` → flush →
   `FileChannel.force(true)` → đổi file hiện tại (nếu hợp lệ) thành `.bak` —
   file hỏng bị bỏ, không bao giờ đè lên `.bak` tốt → rename tmp vào tên chính.
   `File.renameTo` là `rename(2)` trên Android/Linux (thay thế atomic).
   `java.nio.file` (`Files.move(ATOMIC_MOVE)`) **không** dùng được vì
   `minSdk = 24` < API 26. Trên desktop Windows, rename không thay được file
   đích → xóa đích rồi rename (không atomic, chỉ desktop). Không fsync thư mục
   (Java/Android không có API ổn định cho việc này).
6. **Giao thức đọc**: file chính hợp lệ thắng; nếu không, chọn ứng viên hợp
   lệ có revision cao nhất trong các tmp hoàn chỉnh và `.bak` (tmp chỉ hoàn
   chỉnh sau `force`, digest loại tmp bị cắt). Tmp được chọn sẽ được rename vào
   chỗ trước khi dọn các tmp còn lại. Không có ứng viên nào → `null` (defaults).
7. Ghi thất bại không làm sập game: save trước đó còn nguyên (giao thức không
   chạm tới nó trước khi bản mới đã `force`); lần `e(true)` sau thử lại.

## Consequences

- Một file bị cắt ngang hoặc của ứng dụng khác không còn được load thành
  progress; thiết bị đang có save raw 320 B vẫn load được.
- Crash ở bất kỳ bước nào của giao thức để lại bản cũ hoặc bản mới, không bao
  giờ là file hỏng (test `SaveStoreTest` chặn ở từng bước).
- Contract "save 49-byte ACRS" trong `AGENTS.md` là của spike (`SpikeGame`);
  game dùng container này — cập nhật ở Phase 4.

## Evidence

- `docs/save-format.md` §1–3; `reconstructed-project/src/structured/k.java`
  (`e(boolean)` RMS codec, `:5557`).
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt`
  (`saveFlush`, `saveLoad`, `writeIX`).
- Tests: `SaveEnvelopeTest` (core), `SaveStoreTest` (gdx).
