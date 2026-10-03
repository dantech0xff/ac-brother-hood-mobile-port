# ADR: Nhịp tick của game port — tự định nhịp, tối đa 1 tick mỗi frame

- Status: Accepted
- Date: 2026-10-03
- Decision owner: rewrite architecture
- Implementation performed: yes (slice 335 `b9fd2f01`, slice 336 `4517f3e0`;
  ghi nhận ở Phase 4 của
  `plans/261003-0700-parity-gap-closure-android-hardening/`)

## Context

`docs/modern-mobile-technical-design.md` §8 thiết kế một accumulator gọi
"zero hoặc nhiều tick" trước mỗi render, tối đa bốn tick catch-up mỗi frame,
phần dư bị drop. `AGENTS.md` pin contract đó ("tick 62ms, ≤4 catch-up").
Spike (`core/TickEngine`, `gdx/SpikeGame`) cài đúng như vậy.

Bản gốc không bắt kịp thời gian (`proven`):

- `j.B = 62` (`reconstructed-project/src/structured/j.java:82`).
- Vòng `j.run()` (`structured/j.java:197-213`): mỗi vòng `repaint();
  serviceRepaints()` — một lần `paint` = một frame game (`k.a()`) — rồi
  `sleep(max(1, B - (now - C)))`. Frame chạy lâu hơn 62 ms thì frame sau
  bắt đầu sau 1 ms; thời gian đã mất không bao giờ được chạy bù. Không có
  vòng nào chạy hai frame game.

Với catch-up, một frame render trễ (GC, vsync bị lỡ) sinh ra hai tick liền
nhau. Device run của slice 335 thấy cú nhảy giật: hai bước tích phân vị trí
trong một khung hình hiển thị.

## Decision

1. **Game path (`Level0Game`) tự định nhịp:** mỗi frame render chạy **tối đa
   một tick**. Accumulator tính bằng µs, giữ phần dư (remainder-keep) để giữ
   nhịp 62 ms trên đồng hồ vsync; credit bị kẹp ở một tick
   (`Level0Game.tickAccStep`: `minOf(acc - TICK_US, TICK_US)`), nên không có
   burst catch-up. Đây là semantics của vòng sleep gốc: vượt giờ bị hấp thụ,
   không bao giờ chạy bù.
2. **Không replay thời gian treo:** `resume()` đặt accumulator về 0 (đã có từ
   trước; lifecycle ở slice 366 + kế hoạch tick quarantine).
3. **Spike giữ nguyên:** `TickEngine` (≤4 catch-up, drop backlog) vẫn là
   thiết bị kiểm thử của toolchain spike; không dùng cho game.
4. Contract trong `AGENTS.md` đổi "≤4 catch-up" thành "tự định nhịp, ≤1
   tick/frame (remainder-keep)".

## Consequences

- Trên thiết bị chậm hơn 62 ms/frame, game chạy chậm lại như bản gốc thay
  vì nhảy cóc. Không có spiral-of-death vì không có catch-up.
- Input vẫn đi qua `InputQueue` có sequence, drain ở biên tick; frame không
  có tick thì event nằm lại hàng đợi (không mất).
- §8 của technical design ("accumulator gọi zero hoặc nhiều tick", "tối đa
  bốn catch-up") chỉ còn đúng với spike; tài liệu đó ghi chú lại.

## Evidence

- Original: `reconstructed-project/src/structured/j.java:82`, `:197-213`.
- Port: `rewrite/gdx/src/main/kotlin/com/acrebuild/gdx/Level0Game.kt`
  (`TICK_MS`, `tickAccStep`, `render()`), test `Level0GameTickTest` (gdx).
- History: slice 335 (`b9fd2f01`, PR #377), slice 336 (`4517f3e0`, PR #379).
