# Port slice 8 — player knockout: `x[1]` meter = HP, death → respawn

Bối cảnh: slice 7 hiểu nhầm `x[1]` chỉ là stamina — pass này mine sâu
`g.d()`/`k.l()` và sửa lại: `x[1]` là **sync/HP meter**, init = 90.

## Mining — proven (simple/g.java, i.java, k.java)

- `g.x = new int[2]; g.e(90)` tại init (i.java:3201, L195) → **x[1]=90**.
- `d(int r5)` (g.java:3884): gate `s`(cutscene)/`t`(iframes timer? t=10 set
  sau drain)/`c()`/`S∈{67,183,184,205}` → skip; else `i.bh=8` (hit flash),
  `x[1]-=r5`; `x[1]<=0 → x[1]=0`; `k.bh[k.aj]!=3` (không phải flying level)
  → `aZ` grounded ? `bl=0; G(); H(); a=null` : `E()` (rơi tới đất:
  `al+=10` tới khi `aR>=12` hoặc chạm 3/5) rồi detach; `k.l(12)` =
  mission-fail/reload (k.java:2031 — big state switch, chi tiết unmined).
- `g.E()` = ground-snap, `G()/H()` = detach `ae`/`ab` (platform/grab links).
- Drains x[1]: counter pay `d(u[au]=5)`; dispatcher op18 → `g.a()` → `d(5)`;
  damage volumes `d(u[au])` + `aS.i(9)`; grab S375 hold `d(u)`; `d(999)` =
  insta-kill. Fall damage op21: `x[1]-=(105*(al-g.y)/20)/20`.
- Melee strike không trừ HP trực tiếp — op4 → counter hoặc hurt-mark.

## Port (rewrite/core)

- `Entity.x1` init 90 (proven), clamp ≥0; op18 (knockdown w/ meter pay)
  drains 5; op4 counter pays 5 (đã có).
- `Level0World.tick`: `x1<=0` → respawn tại spawn + `x1=90`, `deaths++`
  (inferred — `k.l(12)` checkpoint reload chưa mine; `i.bh=8` hit-flash,
  `t=10` iframe window, E()/G()/H() detach links bỏ qua).

## Kiểm chứng

- 41 `:core:test` xanh; test mới `player knocked out at zero meter respawns
  at spawn` (20× op18 → deaths=1, meter về 90, vị trí về spawn).
- verifier `ok:true`; unittest 57 xanh.

## Gaps

- `k.l(12)` thật = reload checkpoint/mission-fail UI (unmined).
- `g.t` iframe timer, `i.bh` hit-flash, `k.A(18)` hurt-mark visuals,
  HUD meter render chưa port.
- op21 fall-damage và damage-volume ops chưa nối (không có volume entity).
