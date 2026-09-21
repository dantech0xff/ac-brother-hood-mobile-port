# Port slice 7 — auto-counter: meter pay → `c(attacker)` stagger → weaken

Bối cảnh: lấp chỗ còn thiếu giữa slice 5/6 — trong bản gốc trạng thái
weakened (`Z0=2`, `i(144)`) đến từ **counter**: đòn của NPC chạm player khi
còn meter → player tự counter → NPC bị hất lùi. Slice này port đúng vòng đó.

## Mining — proven (simple/i.java)

- `i.a(op=4,…,attacker)` L116-L139 (entity dispatcher): struck entity =
  player `k.aS`. Skip khi `k.aS.S ∈ {284,285,50}`; ax61 attacker →
  `g.a(r13)` riêng; `S==9` → hurt-mark `k.A(18)` không counter; còn lại
  `if (g.a())` → `c(r13)`.
- `g.a()` = "counter-meter payable" (g.java): gate `bh!=0→false`,
  `h()` busy→false; `g()` (`x[1]<=0`) → free pass; else `d(u[k.au])` —
  trả `u[au]={5,10,15}` meter mỗi counter. (a() hầu như luôn true khi
  không busy — auto-counter, không phải block-hold.)
- `c(i attacker)` (i.java:4379): `g.b=null`; floatie `a(8,5,14)`;
  `av = attacker.ak < ak` (quay về phía attacker); `ag = av?1536:-1536`
  knockback; wall-check `y()`/`aF()`; `i(9)` (ax61 → `i(6)`).
- `x[1]` = stamina/counter meter (KHÔNG phải HP — g()=`x[1]>0`,
  `d(u)` trừ vào nó; init value chưa mine → `x1=100` inferred).

## Port (rewrite/core)

- `Entity.x1` (meter), `Entity.counteredBy(attacker)` = c() subset.
- `applyHit` op4: `x1>0 && S!=9 && attacker.ax∉{17,50,61}` →
  `x1-=5; attacker.counteredBy(this)`; else `hitsTaken++` (k.A(18)).
- `NpcFsm` S9 stagger arm: decay `ag*3/4`; end → `aB<=50` → `Z0=2;S144`
  (nối vào weakened loop của slice 6) else resume (inferred arm).

## Kiểm chứng

- 40 `:core:test` xanh; test mới `npc strike on a metered player counters
  the attacker` (S12 X-arc chạm player → attacker vào S9, x1 trừ 5).
- verifier `ok:true`; unittest 57 xanh; emulator boot `reports/slice7.png`.

## Gaps

- `d(u)` là điểm duy nhất trừ meter — regen/refill của `x[1]` chưa mine.
- `k.A(18)` hurt-mark thực (HUD icon) + `x[1]` liên kết player death chưa
  rõ — player vẫn chưa chết được.
- `g.b` stealth-target lock (`f()`/`g()` visibility gates) chưa port.
- Floaties `a(8,…)`, sfx `k.A(n)`, `k.E.P|=128` shake chưa nối.
