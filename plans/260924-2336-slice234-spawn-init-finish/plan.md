---
title: Slice 234 — spawn-init completion (ax32 Lc15, clipless ax65, L1bea finish, pv unify)
phase: port-slices
status: complete
---

# Slice 234 — spawn-init completion for the last uninit'd entity types

Census of all 8 level packs' record ax histograms showed only **ax32**
(5 records, level4) and **ax65** (1 record each in level1 + level4)
lacked any init dispatch. This slice ports the ax32 init arm and adds
the universal `L1bea` record-anim finish that the else arm was missing,
closing entity-type coverage for every shipped record.

## Source findings (proven)

- **`Lc15` ax32 init** (`i.java:8951`, dispatch `case 32` at
  `i.java:7637`): `aA=0; j=0; aB=r8[7]` (HP), `aC=r8[8]`, `aF=r8[9]`,
  `p=r8[10]` (subtype — drives the `bc()` p-switch anims 15/19/25/36),
  `cG=0`; `p!=3 → i.bU+=aB` (gauge accumulate BEFORE the `bV` gates,
  verbatim); `n=aF`; `i.bV==1&&p==0 → aB=0`; `i.bV==2&&p!=3 → aB=0`;
  tail `i(r8[5])`.
- **ax65 is the "Unknown Actor Type" arm**: every init dispatch routes
  ax65 → `L1bc7` (`i.java:11476`) → `L1bea` (`i.java:11489`) —
  i.e. the original performs no bespoke init at all beyond the common
  `i(r8[5])` finish. The port's default path is equivalent.
- **`L1bea` spawn finish** (`i.java:11489`): every non-{37,70} record
  runs `i(r8[5])`; ax68 takes `i(0)`. This is the common tail of EVERY
  init arm (Ld7f, Ldbc, Lc15, … all end `goto L1bea`), not just the
  unknown-type arm.
- **`bi[65]=-1`** (k.java bi[] table): ax65 is a *clipless spawn* in the
  original — an invisible marker entity. The port's
  `clipIdx == null && type != 42 → continue` was skipping it.
- **`bi[61]=71`**: ax61 multi-tool (2 records, level7) maps clip71 —
  missing from `ENTITY_CLIP` so the records never spawned.
- **`i.p` split-brain**: the port had two fields for one source field —
  `pv` (written by 11 init sites, read by `bc()` + ax24 FSM) and `iP`
  (never written, read once at the bc() p-switch). Unified on `pv`;
  `iP` deleted.

## Port changes

- `NpcFsm.initAx32` — verbatim `Lc15` arm.
- `Level0World` spawn dispatch: `type == 32 → initAx32`; else arm gains
  `if (type != 70) e.setAnim(if (type == 68) 0 else f[5])` (the `L1bea`
  finish — also corrects ax16/ax51/ax58/ax66/ax29/ax21/ax43/etc.
  records that legitimately carry `r8[5] != 0` and used to spawn S=0).
- Clipless gate: `type == 65` allowed through like ax42.
- `ENTITY_CLIP[61] = 71` — ax61 records now spawn with clip71.
- `NpcFsm:6989` `r0.iP` → `r0.pv`; `Entity.iP` field removed.
- Restored `initAx76`'s `Record init (L361…)` doc comment (lost during
  the previous comment fix).

## Tests — `Slice234Test` (5 tests)

- `ax32 records spawn with the Lc15 field init` — all 5 level-4 records:
  per-record `(S, aB, aC, aF, pv, nl=aF)` exact.
- `ax32 init accumulates the gauge for non pv3 records` — `iBU == 690`
  (170+200+150+170; the `pv==3` record's 380 skipped — the only `+=`
  writer is `Lc15`).
- `clipless ax65 record spawns like the original unknown arm`.
- `else arm records take their record anim as spawn S` — level-2 ax16
  markers spawn S∈{31,32,38} (record r8[5]=38) instead of 0.
- `ax21 mission director spawns its record anim` — level-4 record S=1.

## Remaining fidelity notes

- `iBU` reads (`aB` clamp + `q=1` engage at `i.java:52461`) and the
  boss-lose payoff (`i.java:53658`) already ported — the gauge cap now
  has a producer.
- Bespoke init arms for the other else-arm types (ax2 `Ld7f`, ax16
  `L595`, ax21 `Lc8c`, ax29 `L10a8`, ax41 `L1172`, ax43 `L11e8`, ax51
  `Ldbc`, ax58 `L1540`, ax61 `La53`, ax66 `L17cd`) — the generic Z-fill
  approximates most field reads but not all (e.g. ax51 wants `Z[0]=0`,
  generic gives `Z[0]=r8[7]`). Follow-up slice can port the arms that
  diverge.
- ax2 checkpoint records spawn via the separate `checkpoints` path —
  left as-is (their `bi[2]=1` entity spawn is not yet ported).

## Gates

`verify-static-reconstruction.py` ok:true · `unittest` 57 · `:core:test`
161 suites 0 failures · `:android:assembleDebug` · `:gdx:build` — all green.
