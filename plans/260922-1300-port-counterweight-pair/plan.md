---
title: "Port slice 52 — ax72/ax78 counterweight pair + ax79 palette prop"
phase: port
status: done
---

# Slice 52 — the counterweight pair

Level 0's last three unported dispatch types, mined verbatim and ported
together because ax72 and ax78 form one mechanism: a lever/trigger entity
that references a suspended weight by `aw`.

## What was ported (all `proven`, bytecode-anchored)

- **ax72 `initAx72`** — L384 (`i.java:3605`): `az=r8[7]`, `Z=int[5]`:
  `Z[0]=r8[8]`, `Z[1]=r8[9]<<8` (8.8 drop offset), `Z[2]=r8[10]`,
  `Z[3]=r8[11]`, `Z[4]=r8[12]` — the linked ax78's `aw`. Tick dispatch
  `case 72 → L897`: static, never ticks. Clip `bi[72]=51`.
- **ax78 `initAx78` + `tickAx78`** — L153 (`i.java:3114`, `az=r8[7]` only)
  and `bA()` (`i.java:17525-17593`): the suspended counterweight FSM.
  `r0` = support probe `h(ak/20,(al+10)/20)` (`v<12 ? v>=5 : true`).
  - S0 settle: supported → `i(1)`; else `al+=2`.
  - S1 armed hold: unsupported → `aj=1536` + `i(2)`.
  - S2 falling: `bt()` crush-sweep vs hostiles; `T==4&&U==0` → `k.A(14)`
    whoosh; supported → `a(true)` land + `i(3)` + `aj=ah=0`; else
    `aj=1536` plus the halving sub-settle loop to `i(3)`.
  - S3 settled: `bt()` + same land/drop rules.
  Clip `bi[78]=63`.
- **ax79 `initAx79`** — L386 (`i.java:3630`): `Z=int[2]`, `Z[0]=r8[7]`
  (palette), `Z[1]=r8[8]`; `e.palette = Z[0]` pinned each init.
  Static, no tick arm. Clip `bi[79]=0`.

## Records (pack-6)

Two linked pairs in level 0: ax72 `aw=33` (2897,445) → ax78 `aw=34`
(2901,507) via `Z[4]`; ax72 `aw=536` (9798,187) → ax78 `aw=111`
(9799,205). Plus ax79 `aw=249` (6456,510, S=79) and `aw=301` (72,495, S=0).
After this slice every level-0 entity type on main has a real FSM —
the remaining ax histogram gaps are types that only appear in other level
packs (ax6/19 already in PR #55).

## Clips

`clip51` (entry-051: 1 anim / 6 frames / 8 modules) and `clip63`
(entry-063: 7 anims / 17 frames / 24 modules) converted via
`convert_slice1.py`; loaded in fixture map, `Level0Game`, `Level0Renderer`.

## Verification

- `Slice52Test`: 7 tests — init field mapping, S0 settle/arm, S1 hold/drop,
  S2 whoosh + zeroed landing, S3 sweep/re-drop, palette pin, record `aw`
  linkage. All green.
- Gates: `verify-static-reconstruction.py` → `ok:true`;
  `python3 -m unittest discover -s tests` → 57 OK; `:core:test` green.

## Flags

- `inferred`: `h()` was inlined as a local `sup(cx,cy)` because `NpcFsm.h`
  is private — semantics are the proven `i.java:7209` one-liner.
- The ax72→ax78 trigger *actuation* (what flips ax78 S0→S1's support) is
  physical support cells only in `bA()` — no script op observed in this arm.
