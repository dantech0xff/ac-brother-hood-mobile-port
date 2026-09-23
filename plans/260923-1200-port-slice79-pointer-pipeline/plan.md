---
title: "Slice 79 — pointer pipeline: E()/word model + j() wheel + soft keys"
phase: port
status: done
---

# Slice 79 — the verbatim pointer pipeline

Replaces the slice-1 placeholder `zoneFor` thirds mapping with the game's
real pointer pipeline: the six-word pad model (`eK/eL/eN/bB/bC/eM`),
`E()` injection, `j()` wheel-zone resolution, the soft-key literals, and
the `pointerReleased` flush.

## Provenance (structured decompile unless noted)

| fn / word | lines | semantics |
|---|---|---|
| `E(i)` | k.java:553-574 | `v()` clear → `eK=i` → bh3 remap `{2→20,8→68,128→272,512→320}` gated by `j.c==8 && bh[aj]==3 && !k()` → `bC|=eK; eL|=eK` — each call clears all six words so last E() wins |
| `j(x,y)` | :576-621 | tap → wheel cell 0..8 for `E(2<<iJ)`. Gate `j.c==8 || (j.c==21 && u∈{8,10})`; margins `x≤60||x≥340 && y≥207`, pause rect `(354,0,46,37)`, `k()` marker 50×50, `i.b` anchor ±70 radial all consume the tap |
| `c(x,y,…)` | :623 | 3×3 split at rect bounds; mounted widens inner-half → 3/5 (cell4 unreachable — the `==4→-1` guard is dead code verbatim) |
| commit tail | :1594-1609 | `if(bB!=0){eO=bB;eP=0} eP++; if(bC!=eL)bD=-1; if(bC!=0)bD++; bC=eL; bB=eK; eM=eN; eK=eN=0` |
| `u/v/w/x` | :5585-5621 | held `bC`, edge `bB`, released `eM`, double-press `bB==eO && eP<5` |
| `v()`/`y(i)` | :5609/:5620 | clear all six words / consume bits from `bC`+`bB` only |
| `pointerReleased` tail | :486-519 | `eN=eL; eL=0` — the release flush that lands `eM` |
| `E(262144)` | :1054/:2288 | pause icon + left soft-key = `M_PAUSE` |
| `E(131072)` | proven | right soft-key = `M_CYCLE` (menu back) |
| `bh={4,3,4,4,3,4,4,4,4}` | :263 | `bh[aj]==3` → autoscroll on missions 1,4 |
| `ce=60 cf=60 cg=37` | :147-149 | margin insets |
| `k()` mounted wheel | :576-621 | `cn-10,124,116×116` (`cn`=50 bh3 else 5); `!bh3` radial offers `(270,165)→4`, `(320,110)→1` |
| bh3 head-tap | :606 | `aS±10,-45,20×25` → `aq=ar=-1` + cell4 — drop hint |
| `kL` offer rect | :615 | `cp` rect → `co==1?1:4` |
| `i.L/i.M` anchor | i.java:174/:7681 | `o()` write / `U()` clear / `b()` ±70 radial |
| `C.Y()` | i.java:17911 | `cd[0]=true` = port `pauseScript()` |

## Corrections this slice forced

- **`j.c` in-play state is 8, not 10.** `l(10)` is the level-select
  screen; the old inferred sentinel 10 as "play" gated all input out
  under the real `j()`. Both `var jC` init and `reload()` now use 8.
- `stateL(15)`'s verbatim L68-77 redirect
  (`nextUnlocked && !bA[15] && ex!=10` → `i=10`) now fires from real
  play — mission-complete → level select is the genuine original flow.
  `missionWon` latches on the entry arg, not the redirected state.
- `l15 from play` now correctly plays the `z(6)` jingle (`ex!=10/22`).
- Pad compat: `commit(nextHeld)` kept for direct FSM tests (folds raw
  held-masks into the words); `queuePress` stays a pending `E()`
  injection; `edge/held/released` are views onto `bB/bC/eM`.

## In-play tap call sites updated

`hold-right`, `run anim`, `jump chain`, `bottom-zone sword`,
`assassination finisher`, `S295` — all now aim at real wheel cells via
the new `cellPoint(cell)` helper instead of fixed canvas points.

## New coverage — `Slice79Test` (14 tests)

- wheel cells 0..8 → masks via `cellPoint`/`resolvePadZone`
- `bC` sticky held across frames, `eM` release edge (one frame)
- `x()` double-press within `<5` frames, expiry at `≥5`
- margins / pause rect / `-1` / `y≥240` exclusions
- pause-rect tap → `M_PAUSE` edge → `l(14)`
- marker 50×50 and `i.b` ±70 anchor exclusion
- mounted wheel `116×116` at `cn-10`, radial offers, 3/5 inner split
- `bh3` remap `2→20` (mission `kAj=1`)
- `E()` last-wins across all six words
- play-gate: `j.c==12` dead, `j.c==21 && u==8` live

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `./gradlew :core:test` → 641 pass
- `./gradlew :android:assembleDebug` → green
