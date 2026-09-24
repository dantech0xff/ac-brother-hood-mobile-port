---
title: "Port slice 194 — KO/corpse family tail (S86/89/110/165 + eL fix)"
phase: port
status: done
---

# Slice 194 — remaining KO/corpse arms, verbatim

Sweep of the remaining undispatched states in `g.e()`'s table
(`fallback/g.java`). S90/146/147/297 were already ported (slices 134/159);
this slice finishes the family.

## Mining results

- **S86** (L297b, case 86): `r()` → `i(326)` — corpse-handoff.
- **S89** (L25eb, case 89): `h(1)` for side effects — consumes pending
  `J&1` into `g.I=1`, forces `k.at=1`, releases an ax16 request link via
  `i.at.H()` (result discarded by the arm); `ai=aj=ag=ah=0`; `r()` →
  `P|=64` (body sleep, corpse stays drawn).
- **S110** (L2de0, case 110): `r()` → `P|=64`.
- **S165** (L2df5, case 165): `aj=1536`; `r()` → `a(0)` (masked fling →
  S43 + `al+=10`).
- **S297** (L26a0): bare `goto L353d` — default arm is verbatim (already
  ported as `297 -> {}`).

## Fidelity fix

- S90 arm: `k.v()` = `eL = 0` (structured k.java:5705) — the port wrote
  `pad.edge = 0` which clears `bB` (the `v()` edge word), not `eL`
  (sticky-edge latch). Now `pad.eL = 0`.

## Changes

- Dispatch: `86`, `89` (`p.requestH(1, world)` — `g.h(int)` already
  ported at Entity.kt:2813), `110`, `165`.
- `Slice194Test` (5 tests): S86→326, S89 zero+`P|64`+`h(1)` consume
  (`gI=1`, `actionLock=1`), S89 no-J-bit settles untouched, S110→`P|64`,
  S165→`aj=1536`+`a(0)`→S43+`al+=10`.

## Gates

- `:core:test` full rerun → green
- verifier → `ok:true`; unittests → 57; `:android:assembleDebug`,
  `:gdx:build` → ok
