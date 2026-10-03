---
phase: 1
title: Parity-Gap Closure
status: in_progress
priority: P1
dependencies: []
---

# Phase 1: Parity-Gap Closure

## Context Links

- [Plan](./plan.md) · [Baseline audit §3](./reports/baseline-audit.md)
- Dispatch/FSM mining: `docs/gameplay-mining/npc-fsm.md`,
  `docs/gameplay-mining/state-animation-map.md`, `docs/i-av-reconstruction.md`
- Original views: `reconstructed-project/src/{simple,structured}/`, bytecode
  `reconstructed-project/bytecode/{g,i,j,k}.javap.txt`

## Overview

Fix the proven divergences G1–G6 from the baseline audit, one slice each
(slice numbers continue after 345), and give every G7 deviation an explicit
verdict. Each fix cites the original `file:line`, carries a confidence label,
and lands with unit tests that would have caught it.

## Progress

| Item | State | Slice |
|---|---|---|
| 1.1 ax47/ax50 FSM swap (G1) | done | [346](../261003-0723-slice346-ax47-ax50-fsm-pairing/plan.md) |
| G9 ax46 S5/S6 re-pin missing (found re-validating 346) | done | [347](../261003-0730-slice347-ax46-fire-cycle-repin/plan.md) |
| 1.6b `l()` containment + full sight test for ax17/23/50 (G8, found in 346) | done | [350](../261003-0816-slice350-l-sight-containment/plan.md) |
| 1.2 `applyHit` op 40 (G2) | done | [348](../261003-0747-slice348-applyhit-op40/plan.md) |
| 1.3 player S18 arm (G3) | done | [349](../261003-0804-slice349-player-s18-s371/plan.md) |
| 1.4 player S371 arm (G4) | done | [349](../261003-0804-slice349-player-s18-s371/plan.md) |
| 1.6 jC=28 ghost rows (G6) | done | [351](../261003-0827-slice351-ae-121-no-panel/plan.md) |
| 1.7 `Trig` tables (G7 → high impact: `j.b` is cos) | done | [352](../261003-0900-slice352-trig-tables-verbatim/plan.md) |
| G10 volley `i.a(int,boolean)` fan/speed/anim (found auditing `j.b` sites) | done | [353](../261003-0930-slice353-volley-fan-speed-anim/plan.md) |
| 1.5, 1.7 (ax37, ax23, comments) | pending | — |

Slice 346 changed the m3 capstone route (legC/legD now traverse the
sentinel chain and the upper path); both legs pass unchanged once slice 347
is in.

## Requirements

- Mirror the original exactly; where the original is not yet mined, mine
  first and record the mining note before porting.
- Fix test fixtures that encode the wrong behaviour (e.g. `Slice64Test`)
  instead of adding parallel tests that leave the wrong ones green.
- Do not touch capstone bot routes in this phase; breaks are recorded and
  handed to Phase 2.

## Work items

### 1.1 ax47 / ax50 FSM swap (G1) — proven · done (slice 346)

- Original: `case 47 → aL()` (ledge sentinel: S0/80–84/93/94),
  `case 50 → aK()` (pouncer: S119–130) — `bytecode/i.javap.txt:20024-20025`,
  `simple/i.java:5181-5194`, bodies at `simple/i.java:9910` (`aK`) and `:10008` (`aL`).
- Port: dispatch at `Level0World.kt:5432-5433`; `tickAx47` holds the `aK` body
  and `tickAx50` the `aL` body (`NpcFsm.kt:9219-9332`). The retype
  (`Level0World.kt:842-846`), the claim states (`NpcFsm.kt:9154`, `:9166`) and
  the death anim (`:9215`) already assume the correct mapping.
- Steps:
  1. Rename/swap so each type runs its original method and the KDoc names
     match (`aK` = pouncer, `aL` = sentinel); audit every `ax == 47/50`,
     `seen47`, `HDM47`, `damageIntakeSentinel` use for consistency.
  2. Rewrite `Slice64Test` (`Slice1Test.kt:7505+`, header "ax47 aK() + ax50 aL()")
     so pouncer cases use ax50 at S120 and sentinel cases use ax47 at S93/S80–84/S94.
  3. Add a pack-level test: load pack-9 (m3, has both types), find a retyped
     ax47 and ax50, and assert each reacts through its own arms (S93
     ceiling-grab → S80 + claim; S120 quadrant pick → S121–128 pounce).
- Data impact: 12 sentinels (m0 4, m2 2, m3 3, m5 3), 8 pouncers (m3 3, m5 5).

### 1.2 `applyHit` op 40 (G2) — proven · done (slice 348)

- Original `structured/i.java:3615-3624`: `if (S != 3 && g.a() && o())`
  → `g.b = attacker; aB = 3; i(3); av = attacker.ak < ak; ag = av ? 512 : -512`.
- Port caller: ax67 clip-27 springboard, `NpcFsm.kt:3591`; `applyHit` has no
  `40` arm (`Entity.kt:3493+`). Model the arm on the existing op 38 arm
  (`Entity.kt:3581-3590`: `playerDamageable` = `g.a()`, `oState()` = `o()`,
  `world.playerLinkB` = `g.b`) but keep op 40's own gates — `S != 3`,
  `g.a()`, `o()`, each one exits (op38 only skips `i(3)` on `o()`).
- Count the affected springboard records per pack and record it in the slice plan.
- Tests: guard matrix (S==3, not damageable, `o()` false) and the positive
  arm (S3, aB=3, facing, ±512, link set).

### 1.3 Player S18 arm (G3) — proven · done (slice 349)

- `g.e()` switch: 18/19/23/36 → offset 7396 (`L1ce4`: `cv=1`, then the
  `L1ce8` air tail) — `bytecode/g.javap.txt:2537-2555`, `:5344-5346`.
- Port: add 18 to the `19, 36 -> { p.cv = true; airFamily(p, pad) }` arm
  (`PlayerFsm.kt:266`); `airFamily`'s comment (`:1919`) already lists 18.
- Tests: S17 wall-kick → S18 gets `aj=1536`, `cv/cp/ct/cw`, the `T==1`
  launch impulse, and lands through the air tail instead of the default fling.

### 1.4 Player S371 arm (G4) — proven · done (slice 349)

- Bytecode: `371 → 2223: goto 13629` (`L353d`, bare tail) —
  `g.javap.txt:2890`, `:2924`; same as 59/65/164/211/297.
- Port: add 371 to the empty-arm group (`PlayerFsm.kt:381`).
- Tests: S370 → S371 (`:1213-1215`) is never flung on anim end; the ax61 S12
  grab (`NpcFsm.kt:5510-5516`) resolves through its QTE/lose path only.

### 1.5 ax11 `aC()` attack scheduler (G5) — mine, then port

- Gap: ax11's chase arm calls `aC()` (`simple/i.java:5557-5560`); the port
  has only a chase-timeout subset (`NpcFsm.kt:1076-1085`) and nothing sets an
  NPC to S11, so the S11 wind-up arm (`NpcFsm.kt:371`) is dead. ax73 already
  has a verbatim port, `attackScheduler73` (`NpcFsm.kt:8541+`, citing
  `i.java:8893-9144`).
- Steps:
  1. Mining note (append to `docs/gameplay-mining/npc-fsm.md`): walk
     `i.aC()` arm by arm and mark which branches depend on `ax` / `Z[0]` /
     `aq`, which S targets ax11 reaches (distance tiers, `aq/ar` target walk,
     bound ax69 throw), and what `attackScheduler73` hard-codes for ax73.
  2. Port one shared scheduler (parameterised only where the original
     branches) and call it from the ax11 chase arm; keep the ax73 behaviour
     byte-identical (its existing tests must stay green unchanged).
  3. Tests per tier at controlled player distances; S11 → S12 strike reachable.
- Timebox: if step 1 finds unknowns, split into its own slice plan and
  continue with the other items.
- Impact: 176 ax11 records (m0 49, m2 41, m3 27, m5 27, m6 28, m7 4).

### 1.6 jC=28 ghost rows (G6) — proven · done (slice 351)

- Confirm in `k.java:6204-6228` that the `eC==121` arm of `ae()` draws no
  menu panel/rows; if so, stop `panelVisible` (`Level0World.kt:3095-3097`)
  or the renderer's panel pass from drawing rows for `jC==28 && kEc==121`.
- Test: the jC=28 / kEc=121 frame produces no row rects or pills.

### 1.6b `l()` camera gate for ax17/23/50 (G8) — proven, found in slice 346 · done (slice 350)

- Original `l()` (`simple/i.java:2255-2406`): `case 17/23 → L70`,
  `case 50 → L77`; both arms are `bn → false; else b(this.W, k.ac)`, and
  `b(int[],int[])` (`simple/i.java:665-676`) is **containment** (W ⊆ ac).
- Port `losL` (`NpcFsm.kt`, the `17, 23, 50 ->` arm) uses
  `Entity.overlapI(e.W, ac)` — overlap — so a half-visible ax17/23 already
  "sees" the player in `spotB` (alert) and the L827 tail (counter-alert).
  ax50 is unaffected in practice (`aK` uses `seen50`, which is containment).
- Fix: containment in that arm; tests at the camera edge (overlap but not
  contained → blind). Expect ax17 alert timing changes in capstones.

### 1.7 Deviations (G7) — verdict each

- ax37 scroll triggers: decide whether the separate `ScrollTrigger` list
  (`Level0World.kt:592-614`, fired at `:5275`) must honour P&32/P&256 and
  script removals like the entity path; check the two parked records (m2, m7)
  against the data and fix or document.
- ax23 family filter (`NpcFsm.kt:264`): document as unreachable with current
  data, or route `case 23` straight to the tail (`simple/i.java:5180`).
- `Trig` — done in slice 352: the tables come from archive `/16`
  (`j.a("/16",0,1)`, `structured/k.java:4009`), not pack-2, and `T` is a
  **cosine** table, so every `j.b` consumer was a quarter turn off (not a
  low-impact deviation). `V[]` atan2 and `U[0] = 0` ported verbatim.
- Stale comments: `NpcFsm.kt:29-31`, `NpcFsm.kt:9219`, `PlayerFsm.kt:2197`,
  `Level0Game.kt:209-212`.

## Related Code Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/{NpcFsm,PlayerFsm,Entity,Level0World,Trig}.kt`
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` (Slice64Test),
  new `Slice346Test.kt`… per slice
- `docs/gameplay-mining/npc-fsm.md`, `docs/gameplay-mining/level-atlas.md`

## Success Criteria

- G1–G6 fixed, each with a slice plan (`plans/YYMMDD-HHMM-sliceNNN-*/plan.md`,
  `status: done`) and tests citing the original lines.
- Every G7 item has a written verdict (fixed, or accepted deviation with evidence).
- Verifier `ok:true`, 57 unittests, `:core:test`, `:gdx:test`,
  `:android:assembleDebug` green — except capstone tests that now fail
  because enemies engage; those are listed for Phase 2 and annotated, not
  weakened.

## Risk Assessment

- G1, G3 and G5 change combat and traversal timing in most missions; expect
  capstone breaks (handled in Phase 2).
- G5 may reveal more unported `aC()` dependencies; the timebox keeps the
  phase moving.
