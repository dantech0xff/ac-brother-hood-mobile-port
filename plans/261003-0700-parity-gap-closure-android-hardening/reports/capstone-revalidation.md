---
title: Capstone re-validation (Phase 2)
date: 2026-10-03
branch: claude/phase2-faithful-ai
head: 1e7bb59d
---

# Capstone re-validation — Phase 2

Báo cáo Phase 2: chạy lại toàn bộ capstone bot trên game đã sửa ở
Phase 1 + G12, phân loại từng chân (leg) theo luật Phase 2: bot chỉ được
đổi route/timing, không được làm yếu/dời enemy hay patch state.

Nhánh tích hợp: `claude/phase2-faithful-ai` = Phase 1 + G5 (slice 357) +
slice 359 + G12 (frame order) + slices 360–363 + bot re-route.

Gate @ 1e7bb59d: verifier `ok:true`; unittest 57/57; `:core:test`
1689/1689; `:gdx:test` 9/9; `:android:assembleDebug` xanh.

## Phân loại

- `re-routed` — bot đổi route/timing; cơ chế game giữ nguyên.
- `port-fix` — re-validate làm lộ một lỗi port mới (không phải fix Phase 1
  sai — không có `fix-regression` nào): sửa ở core thành slice riêng, proven.
- `assert-corrected` — assertion của test chặt hơn hành vi gốc (đã kiểm
  chứng bằng trace + nguồn), nới lại có giải thích; verdict của leg giữ
  nguyên. Không có leg nào là `faithful-dead-end`.

## Bảng theo mission

| Mission | Test | Kết quả | Phân loại | Ghi chú / commit |
|---|---|---|---|---|
| m0 | `Slice245Test` end-to-end | pass (0 death, t=3636; trước đây 13 death, t=7238) | re-routed + port-fix | 747bdcef (6 chân), slice 362 (door `bv()`) |
| m0 | `Slice245Test` spawn→east | pass | re-routed | 01bc3603 — arm `aF` muộn để face-grab ở y≈544 |
| m0 | `Slice245Test` cp6→cp7, cp7 | pass | re-routed | fef76a92, e6bf73a1 (slice 360) |
| m1 | `Slice245Test` mission-1 shaft | pass | assert-corrected | 1e7bb59d — respawn tại stamp của glider (8358), một bước leo trên cp1 |
| m2 | `Slice281Test` leg A | pass | re-routed | 8f70c6df — phá crate ax4 (push-out zero `ag` trước integrate) |
| m3 | `Slice282Test` legs A/F | pass | re-routed | 5337aa4a; leg H vẫn pass |
| m4 | `Slice288Test` capstone | pass | re-routed | b9e44302 — resume route ở leg trên checkpoint respawn |
| m5 | `Slice289Test` legs B/E/G/I/J | pass (11/11) | re-routed | 79d78e19, f1f7c872, 3c4eefcb |
| m6 | `Slice291Test` legs D/F (+E) | pass (6/6) | re-routed | c22cd238 |
| m7 | `Slice303/304/306/309Test` | pass | re-routed + port-fix | d6700296; slice 363 (kill dưới camera chỉ cho flying) |

## Lỗi port lộ ra trong Phase 2 (đã sửa, proven)

| Slice | Nội dung | Plan |
|---|---|---|
| 360 | `g.l()` hold-to-turn, `g.aw()` arms, `e()` head `aA`, thứ tự post-tail; một `k.bD` | `plans/261003-1400-slice360-player-l-aw-posttail/plan.md` |
| 361 | `setAnim` = `i.i(int)` (side effect mọi lần gọi), x-snap của `i.a(int,int)` là chuỗi else-if | `plans/261003-1500-slice361-setanim-i-i/plan.md` |
| 362 | door `bv()`: box của frame trước, polarity mode 1, promotion chỉ ở tick resolve, `i.a`, nhánh crush | `plans/261003-1600-slice362-ax44-door-bv/plan.md` |
| 363 | kill "dưới camera" chỉ nằm trong `i.B()` (flying, case 25) | `plans/261003-1700-slice363-below-camera-kill-flying-only/plan.md` |

## Còn lại của Phase 2

- Step 3 (nối các leg stitched m2/m3/m5/m6/m7 thành run liên tục) — chưa
  làm; các leg hiện vẫn stitched như trước.
- Step 4–7 (Run-31 write-up, device Run-32/Run-33) — cần emulator host;
  container cloud không có `/dev/kvm`. Chưa chạy.

## Step 3 — stitch attempts (P4, branch `devin/land`)

### m2 — `Slice281Test.mission2CapstoneFull` (bounded, documented seam)

Đã viết một bot policy union position/state-keyed gộp legA→legC→legWin,
không teleport. Kết quả: **seam legA→legC không nối được liên tục** —

- Đường trên (mass top y1540 → chimney x2220-2340 → tower top): descent
  của legA luôn đặt player ở x2214-2216 đang rơi (S74 leap-dash +40
  của game tự fling qua mép) — không bao giờ grounded trong gap để bắt
  đầu chuỗi wall-kick; rơi dọc mặt ngoài xuống deck.
- Đường dưới (deck `20`@1940): cặp ax11 S89 pin-guard ở (2215,1938)
  nằm đúng đường rơi — pin sống sót được nhưng mất HP; fight zone
  x2220-2400 QTE được nhưng cộng dồn sát thương.
- Shaft strip-gap x2420-2540 có đáy `02`@2040 — **type-2 = kill-cell**
  (`g.e()` head: `aR==2 || aO==2 || L()` trên player không mount →
  `x1=0`, `i(50)`). Vault môi-động S22/23 của chính game bắn ~40px
  trước mép và luôn đáp x2477-2508 — `02` trần → chết tức thì.
  Door-tops ax44 (2434/2469/2497, mount → né kill) chỉ qua được bằng
  độ chính xác <10px mà policy bot không biểu đạt; ax10 uid22 S43 là
  ledge-mount helper cho route hang-shimmy — cũng ngoài khả năng
  position-policy.
- Các leg riêng lẻ (legA, legC, legWin) vẫn pass — evidence capstone
  giữ nguyên. Test giữ assertion bound `maxAk >= 2450` (spawn → pillar →
  mass top → deck → shaft mouth) thay cho win, kèm comment seam trong
  header.
