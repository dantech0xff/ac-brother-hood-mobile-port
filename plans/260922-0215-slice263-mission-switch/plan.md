---
title: "Slice 263 — post-win mission switch verified end-to-end"
phase: port
status: complete
---

# Slice 263 — mission-complete → stats → mission-1 entry

## What this slice proves

The full post-win chain through real input, in one bot leg:

1. **Drive to win** (same as slice 262): cp7 → kick well → tower →
   door 87→134 → channel → '41' road → uid115 claim (script 250 ride)
   → uid252 win zone → script 116 `op37` → `screenL(15)` + `kStat(0)`
   → `jC=15` at t454.
2. **Stats screen** (`winStatsM`, Level0World.kt:2432-2501):
   `pad.v(458784)` → first press `jG<=10 → jG=10` (skip reveals),
   second → persist (`kBA[si]` best, `kBA[32/8/44/46/48]`) then
   `pad.v(327712)` → `kAj++` → `kEgFlags[1]` → `stateL(30)`.
3. **af() browse** (jC=30, :3659-3705): `pad.v(M_CONTEXT)` →
   `stateL(9)` (`kFF!=20` arm).
4. **G() loader** (jC=9, :3559-3579): `jG==3 → loadPackI(1)`
   (level1.aclv + scripts + strings swap — `loadedAj: 0→1`), `jG==164 →
   spawnEntities()` (npcs 628 → 219 = mission-1's 225 records minus
   clipless), `jG>164 + pad.w(M_CONTEXT)` release → `camResetC()` +
   `stateL(8)` + `missionF(1)` + `missionInit()`.
5. **Mission 1 gameplay**: `jC=8`, `kAj=1`, `bh3=true` — the flying
   canyon (`kBh[1]==3`, ax25 glider record at (581,11963), 219 entities).

## Evidence

`Slice245Test."bot drives mission-complete stats into mission 1"` —
asserts `jC==8 && kAj==1 && loadedAj==1 && l1` (ax25/uid121 present).
Marks show the timed sequence: `jC=15` at t454 → `kAj=1` → `jC=9` →
`loadedAj=1` at jG≈4 → `npcs=219` at jG=165 → `jC=8`.

`proven` — every transition follows the verbatim input arms cited above;
the loader stage timings are the G() staged constants.

## Gates

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → all pass (incl. the new leg)
- `:android:assembleDebug` + `:gdx:build` → SUCCESSFUL
