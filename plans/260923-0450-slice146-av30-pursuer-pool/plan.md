---
slice: 146
date: 2026-09-23
confidence: proven
---

# Slice 146 — ax10 S30 pursuer-pool wave spawner (La72)

Port the `aV()` dispatch `case 30: goto La72` (i.java:9199) arm
(i.java:10427–11736) — the pursuer-pool zone that spawns waves of
humanoid NPCs while the player overlaps its `W` rect. Ported verbatim
into `tickTrigger` at NpcFsm.kt (~:812).

## Semantics (proven)

- **Gate** (La72): `rectsOverlap(e.W, player.W)` else return; `P|=16`.
  `Z[6]==3` forces the infinite-wave config `Z[1]=-1, Z[2]=3`. Then
  `pv=Z[1]` rows (`-1→1`), `aG=Z[2]` cols; `pv<=0 || aG<=0` → return
  (Lad9). `pv/aG` are re-read from `Z` every tick (live config).
- **Initial fill** (`cr==null`): allocates `i[pv][aG]`; every member gets
  `aw=5000+row*pv+col` uid, `au=0`, fresh `Z(22)`, then the `Z[6]` flavor
  switch — 0→ax11 `i(4)`/`bu[au]`, 1→ax17 `i(59)`/`bu[au]`, 2→ax23
  `i(71)`/`bv[au]`, 3→ax11 `i(4)`/`bu[au]` + col0 → `m.Z[0]=1` engaged /
  col≥1 → 0, col0 → `e.Z[5]=1` / col≥1 → 0 (spawn side), `e.aC=Z[7]`;
  `default` → plain entity straight to Lcb7. Then the common tail:
  `az=100`, `P|=16`, `Z[1]=0`, `Z[2]=-1`, `Z[14]=0`, `av=(e.Z[5]!=0)`,
  `Z[15]=-160`, `Z[16]=-64`, `Z[17]=320`, `Z[18]=100`. **Row-0 members
  only** get positioned + registered: `aq=Z[3]+k.O`, `ar=Z[4]`,
  `ak = Z[5]==0 ? k.O-20*(col+1) : k.O+400+20*(col+1)` (offscreen either
  side of the camera), `al=Z[4]`, `ag=ah=0`, `Z[3]=ak`, `Z[4]=al`,
  `k.b()` enqueue. Ends `aA=0`. The field `e.cr` (i.java:111 `i[][]`)
  was added to `Entity` for this arm.
- **Steady state** (`cr!=null`, Le36): `Z[1]!=-1 && aT()` (every member
  `P()` → all dead) → resolve the linked uid `k.q(Z[0])` and clear
  `P&=~32`, plus `P&=~128` when `bi[ax]!=-1`; `k.c(this)` removes the
  zone; `aS()` drains the pool.
- **`Z[6]==3`** → the infinite pursuer wave (Le8e): iterates the current
  row while `aC<=0`; each dead member (`P()`) is replaced by a fresh
  ax11 member — `aw=5000+aA*pv+col`, `i(4)`, `bu[au]`, `Z[0]=1` only
  when no other engaged member, same common tail, `e.Z[5]` side flag
  per column (`>=1→0`, `0→1`), same offscreen `ak`, `k.b()`, and
  `aC=Z[7]` reload. `aC>0` skips the whole pass and decrements `aC` —
  a per-member respawn cooldown.
- **Finite pool** (`Z[6]!=3`, L1185): if the current row is fully dead —
  `Z[1]==-1` → `aS()` + `goto L0` re-dispatch (drains and refills the
  pool); otherwise `aA++` (≥pv → return, pool exhausted) and the L11cf
  block repositions row `aA` exactly like the row-0 init (also zeroing
  `ai/aj`) + `k.b()`. Members of dormant rows sit untouched in `cr`.

## Port notes

- `ax` is `val` → members are constructed `Entity(ax, w.clipFor(7))`
  inside the flavor `when` instead of post-mutating.
- `m.Z = new int[22]` — fresh `Entity` already carries a zeroed
  `IntArray(22)`; noted in-code.
- `goto L0` → `while(true)` + `continue` inside the arm (bounded: the
  re-entry refills `cr` and returns).
- `k.q(Z[0])` → `w.findByAw`; `k.O` → `w.kO` (camX); `k.b` →
  `w.queueInsert`; `k.c` → `w.removeEntity`; `bu` → `Entity.WEAPON_DMG`;
  `bv` → `Entity.NPC_HP_BV`; `k.au` → `w.weaponSlot`; clip always
  `w.clipFor(7)` (bi[11]=bi[17]=bi[23]=7, k.java:8442).
- Added `Entity.AX_CLIP_BI` (k.java:266, proven) for the `bi[ax]!=-1`
  flag clear, and `poolDrain()`/`poolAllDead()` for `aS()`/`aT()`
  (i.java:8963/8975).
- Record coverage: 2 S30 records in the packs — Z[6] flavor per record.

## Tests

`Slice146Test` (8 tests): no-overlap noop, finite 2×2 fill
(grid/uid/flavor/row0 positions), Z6=3 1×3 leader config, dead-member
respawn + identity, `aC` cooldown gating, finite row wipe → `aA`
advance + reposition, all-dead cleanup (`P` clears + `k.c` + `aS`),
`Z[1]==-1` drain → `goto L0` refill.

Gates: verifier `ok:true`, 57 unittests, `:core:test` (603), 
`:android:assembleDebug`, `:gdx:build` — all green.
