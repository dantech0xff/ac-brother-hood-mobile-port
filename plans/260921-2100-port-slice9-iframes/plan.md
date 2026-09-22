# Port slice 9 — `g.t` iframes + `i.bh` hit-flash + op4→op18 upgrade

## Mining — proven (simple/g.java, i.java)

- `g.t` = static iframe timer; decrement per tick (i.java:4072 `g.t--`).
- `g.d(int)` gates: `s` cutscene, `t!=0`, `c()` linked-carry,
  `S∈{67,183,184,205}` → skip; else `i.bh=8`, `x[1]-=amt`, clamp 0;
  **survival sets `t=10`** (g.java:3925).
- Damage-volume hits set `g.t=5` (i.java:21790 `g.d(u[au]); g.t=5`).
- Teleport/portal arm sets `t=100` (g.java:1139, `af.ax∈{27,10}`).
- `g.h()` = `s || t!=0` → busy flag consumed by `g.a()` (counter pay
  refuses during iframes) — already reflected via `gt==0` in canCounter.
- `i.a(op4,…)` dispatcher head (i.java:4446): `g.b(aS.S)` (player attacking)
  `&& g.t==0 && !g.s` → **`r10=18`, `this.ag=0`** — the attack-clash
  upgrade: player mid-swing struck by op4 gets op18 knockdown instead.

## Port (rewrite/core)

- `Entity.gt` (g.t), `Entity.bh` (i.bh, flash counter — visual pending).
- `drainMeter(amt)` = `g.d()` port; op4 counter pay + op18 now route
  through it (so the `S67` gate naturally makes clash-drains free — proven
  subtlety: `d()` skips while `S==67`, so a mid-combo clash knocks the
  player down via `i(43)` but pays 0 meter).
- applyHit head: `op4 && isAttackState(S) && gt==0 → r10=18; ag=0`.
- PlayerFsm.tick: `gt--`, `bh--` per tick (i.java:4072).

## Kiểm chứng

- 42 `:core:test` xanh; new `iframes block a second drain for 10 ticks`
  (85→blocked→0-gate→80) + knockout test updated to reset `gt`.
- verifier `ok:true`; unittest 57 xanh.

## Gaps

- `bh` visual flash not drawn; `t=5` volume hits (no volume entities yet);
  `t=100` teleports (ax10/27 not spawned); `g.s` cutscene flag unported.
