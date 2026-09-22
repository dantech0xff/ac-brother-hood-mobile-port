---
title: "Port slice 31 — bb() L21 marker-state switch (knife flight + kill prompts)"
phase: slice-31
status: complete
---

# Slice 31 — `i.bb()` L21: request-marker state zoo

Completes the ax16 dispatch (`I()` case 16 → `bb()`, i.java:14088). The
collect arms (S30/38/39) shipped in slice 25; this ports the L21 switch:
knife-projectile FSM (S15/16/17), ceiling props (S22/23/25), the
stealth-kill prompt markers (S31/32/33 → the `v(65568)` press that
teleports the player into `i(216)`/`i(214)`), and the inert L195 default.

## Source → port map

| Original (i.java) | Port |
|---|---|
| `bb()` L21 switch (14113-14395) | `NpcFsm.markerArm` |
| `t()` fill boxes | `Entity.refreshBoxes()` |
| `bd()` rest sweep (14538) | `Entity.sweepNeighbors` |
| `bc()` impact sweep (14396) | `Entity.sweepNeighborsB` |
| `v()`/`u()` ax16 path (730/700) | `Entity.markerVisible` |
| `k.h(int,int)` (k.java:6839) | `Entity.magApprox` |
| `k.e(4-arg)` arc solver (k.java:6860) | `Entity.arcSolve` (+`isqrt`=`j.d`) |
| `k.e(0,aw)` stat tally (k.java:4314) | `LevelCellSource.statTally` → `statTally0` |
| `k.bh[k.aj]` per-mission flag (k.java:8437) | `MISSION_BH` + `missionBh()` |
| `g.b()` attack flag (g.java:346) | `LevelCellSource.playerAttacking` |
| `i.cF` static gauge flag (i.java:184) | `LevelCellSource.cFFlag` |
| `i.cG` int flash counter (i.java:185) | `Entity.cGCount` |
| `g.b = r13` link (op38 L75) | `LevelCellSource.playerLinkB` |
| `i.H`/`i.K` tables (22318/22322) | `WEAPON_H`/`WEAPON_K` |
| `i.bR` bounce flag | `Entity.bR` |
| `a(38,0,0,e)` marker-engage | `applyHit` op `38` |
| `G()` | `Entity.releaseAe` (ae-child p()+null) |
| `a(4,ak,al-60)` / `a(8,ak,al-85)` | `Entity.spawnMarker` |
| `k.c(this)` | `LevelCellSource.removeEntity` |

## Arm semantics

- **S15** `r() → t()+bd()+i(9)` — flight end rest.
- **S16** `T==1&&U==0 → A(12)`; `t()`; `bh[k.aj]==3` (missions 1/4):
  settle `ah=ag=0`, owner `af==aS → bc()` else `X∩aS.W → a(38)`;
  else `bd()` + `X∩aS.W → a(4)`. `af=null; r() → k.c(this)`.
- **S17** knife flight: `aj=512`; horizontal flight emits `a(4,…)` trail
  markers; player X-overlap → attacking (`g.b()`) bounces the knife
  (snap outside the attack box, flip `av`, `ag = k.e(afΔy, ah>>8, 2,
  afΔx)` arc-solve back to the thrower, `bR` set), else `aS.a(4)`.
  `bR && af.ax==23 && overlap → af.aB=0, af.i(79)` (return-kill the
  thrower). `r9 → zero + i(16) + G()`. Wall scan on the facing column
  (`e>=12 → snap + av flip + ag=-ag/2`); floor `e>=12 → G()`, then
  `ah>2048 → bounce`, `k.h(ag,ah)>=1024 → ah=0` slide, else `i(16)`.
- **S22/23** `W==null → k.c`; `W∩aS.W → a(4)+k.c`; S22 under-feet
  ceiling `cell==20 && W[3]<=aS.W[1] → i(25)`; S23 `!v() → k.c`.
- **S25** `r() → k.c`.
- **S31** prompt: out of overlap or S216/50/attacking/`J&2` unset →
  `G()`; else `a(8,ak,al-85)` indicator + `v(65568)` → player teleports
  onto the marker + `i(216)` + `A(29)`.
- **S32/33** same shape → `i(214)`; S32 pins `aS.ak=ak-45,av=false`,
  S33 `ak+45,av=true` (no `g.b()` gate — marked difference from S31).
- **default** inert.

## `bc()`/`bd()` sweep semantics

`bd()` — X-overlap neighbors: ax19 `S2→i(3)`; while the sweeper flies
(`S==17`): ax17 `S!=69` and (for ax23 sweepers) `S!=79` take
`aB -= H[au]` (50); dead → ax17 `i(129)`/ax23 `i(79)`, alive →
`i(68)`/`i(73)`.

`bc()` — same scan, prop-destruction arm: ax54/30/56 volumes (own `af`
protected: `af.ax` in the prop family skips matching types), ax67 gates
`S∈{19,21,23,32,35,38,41,43}` (`aB--` skipped while `this.S==9`; dead →
`i(S+1)`), ax24 `S19→i(20)`, ax32 armed walls (`l&1` parity gate;
`S∈[21,27]` break the whole scan when `cF` unset — full-gauge throws
only; `S20` immune; `aB -= K[au]` then `p`-switch `0→i(15),2→i(19),
3→i(25),4→i(36)` or `cG=6` flash).

## Decisions / flags

- **`g.a()` damage-gate chain** (op38 L75 precondition: `i.bh!=0→false;
  h()→false; g()→true else d(u[au])`) is unported — the arm is gated
  open and flagged `inferred`. It only matters on `bh==3` missions
  (1/4); level 0 runs the `bd()`/`a(4)` arm.
- **`af` null-guard** on the L58 arc-solve (original reads `af.al`
  unconditionally — field is always set by the thrower, `inferred`
  guard for robustness).
- **`d(8,…)` floatie spawns** inside the ax54/30 arms flagged unported
  (visual-only damage numbers).
- `i.p` (bc p-switch) is the existing `pv` field (r8[13] record slot).
- `pad` plumbed through `tickRequestMarker(e, player, pad)` — needed by
  the S31/32/33 `v(65568)` taps.
- `missionIndex`/`statTally`/`cFFlag`/`playerLinkB`/`playerAttacking`/
  `missionBh()` added to `LevelCellSource`; `MISSION_BH`/`WEAPON_H`/
  `WEAPON_K`/`magApprox`/`isqrt`/`arcSolve` on `Entity.companion`.

## Tests (8 new, 139 total green)

- S31 prompt: overlap + `J&2` + 65568 tap → `aS.i(216)` + teleport + A(29).
- S32/33: `aS.i(214)`, `ak∓/±45`, `av` false/true.
- S31 inert without `J&2`.
- S16: X-overlap → `a(4)` meter hit; `af` cleared (`bh[0]==4` else-arm).
- S17: idle overlap pays `a(4)`; attacking player triggers L58 bounce
  (`bR`, no damage).
- S22 zero-W → `k.c` removal; S15 `r()` → `i(9)` via the rest sweep.

## Verification

- `:core:test` 139/139 green; verifier `ok:true`; `python3 -m unittest`
  57 OK; emulator boot `npcs=454` clean.
