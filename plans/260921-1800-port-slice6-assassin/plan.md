# Port slice 6 — weaken → assassination finisher (183/184) + victim chain

Bối cảnh: slice 5 có combo kiếm nhưng đòn kết liễu (finisher) còn thiếu vì
cần `i.aN` lock-on. Slice này port chuỗi: lính bị đánh xuống ngưỡng →
weakened/block stance → `i.aN` lock → tap → player S183/184 → victim
S106/107 bị kéo → `aB=0` → corpse.

## Mining — proven (simple/g.java, i.java)

- `i.aN` = field static: lock mục tiêu duy nhất. `j()` L7-L18: NPC ax11
  `Z[0]==2` claim lock khi `aN==null || aN.S!=18`. Clear khi chết
  (`aN==this → aN=null` tại L673/L8801).
- `C()` (i.java:1955, gọi trong `j()` sau damage): ax11 `Z0==1 && aB<=bu` →
  `Z0=2; i(144)` + floatie 45. ax73 `Z0==0` → `Z0=3; i(155)` + grab setup
  (`k.aS.i(8)`, `aq = ak ∓60`, `k.E.P|=128`). ax17 → `i(68)`; ax23 → `i(73)`
  + kéo player ±45.
- Player arm S183 (g.java L413): mỗi tick `aN.i(106)`, snap victim `ak ±30`,
  `al = al`; `r()` → `k.p(); i.O(); i(0); aN.aB=0; i.d(aN); aN=null`.
  S184 (L424/L426): `i(107)`, offset ±35.
- Victim S106/107 (i.java L506/L8780): `P|=512; ag=ah=0; aA=2`; `r()` →
  `P&=~16; P|=32|64` + clear `aN`, `g.b`.
- Combo arm L1355: `v(65568)` && `S∈{67,68}` && `aN ax11 Z0==2 aB<=bw` →
  `R = |rand|%2 ? 184 : 183`.

## Quyết định port (ghi nhãn)

- Level-0 soldiers mang `Z[0]=0` (record f10), C() gốc yêu cầu Z0==1 →
  **inferred**: dùng ngưỡng `aB <= H[0]=50` (proven counter line) làm
  trạng thái weakened → `Z0=2; i(144)`.
- `S144` block stance chặn `j()` damage thường — **inferred** (đó là ý
  nghĩa của counter-offer: chỉ finisher mới giết).
- Với `bw=80, bu=300` chip damage không bao giờ dừng trong (0,50] → weakened
  trong bản gốc đến từ counter (`g.a()` block) — chưa port; test seed aB=120
  để 1 slash thật rơi vào vùng weaken.

## Port

- `LevelCellSource.lockTarget` (i.aN); `NpcFsm`: lock claim + clear,
  arms S144 (stance), S106/107 (victim pin → corpse flags), C() weaken.
- `PlayerFsm`: comboArm nhận finisher R=183|184 khi lock weakened;
  `assassinArm` S183/184 drag victim + kill (i.d → i(139)+P flags).
- `PlayerFsm(world, rng)` — rng cho finisher pick.

## Kiểm chứng

- 39 `:core:test` xanh; test mới: `assassination finisher kills a weakened
  locked soldier` (hit → weaken → tap → 183/184 → victim dead).
- verifier `ok:true`; unittest 57 xanh; emulator boot `reports/slice6.png`.

## Gaps

- Counter (`g.a()` block + `aN` qua parry) chưa port — finisher chỉ đạt tới
  qua damage low-HP hiện tại.
- `a(34)/k.e(0,aw)` drops, `k.A(10)` swing sfx, `aS.S()` sfx hook; ax73 grab
  (`Z0=3, i(155)`); floaties `a(8,…)` — chưa port.
