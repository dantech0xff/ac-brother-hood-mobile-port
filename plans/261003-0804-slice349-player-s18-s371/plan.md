---
title: "Slice 349 — player S18 (L1ce4 air arm) and S371 (bare L353d tail)"
phase: "port"
status: "done"
slice: 349
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt:2537-2555
  - reconstructed-project/bytecode/g.javap.txt:5344-5753
  - reconstructed-project/bytecode/g.javap.txt:2890
  - reconstructed-project/bytecode/g.javap.txt:2924
  - reconstructed-project/src/simple/g.java:632-650
  - reconstructed-project/src/simple/g.java:673-985
---

# Slice 349 — player S18 and S371 arms

Phase 1 items 1.3 (G3) and 1.4 (G4) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Findings (proven)

- **S18.** The `g.e()` switch sends 18/19/23/36 to offset 7396 = `L1ce4`
  (`g.javap.txt:2537-2555`): `cv = 1` (`:5344-5345`), then the `L1ce8`
  air tail — `cp/ct/cw`, `aj = 1536`, the `T==1` launch, cv wall-grab,
  land, apex. The tail (offsets 7396–8303, `:5344-5753`) compares `S`
  only against 15/19/20/22/23/24/25/36/215 — never 18 — so S18 takes the
  generic air path. The port routed 19/36 (`PlayerFsm.kt` `19, 36 ->`) and
  23 (`airFamily`, whose own comment lists 18 as an L1ce4 state) there,
  but no arm dispatched 18: the S17 wall-kick flight fell into the default
  arm — no gravity, no air flags, no cv wall-grab — until its anim end.
- **S371.** `371 → 2223: goto 13629` (`:2890`, `:2924`) — 13629 = `L353d`,
  the bare shared tail, exactly like 59/65/164/211/297, which the port
  already mirrors with empty arms. Without an arm, the default arm flung
  the player out of the Cesare grab hold (S370 → S371) as soon as its anim
  ended; ax61's S12 QTE overlay then took its "player broke the grab"
  branch, so a failed mash could never reach the S13 throw / S374 KO.
  (The baseline audit marked this consequence `inferred`; it is now pinned
  by `Slice349Test`.)

## Fix

- `PlayerFsm.dispatch`: `18, 19, 36 -> { cv = true; airFamily() }` and
  `59, 65, 164, 211, 371 -> { }`.

## Test

- `Slice349Test` (4): S18's first tick runs the L1ce4 arm (cv, cp/ct/cw,
  aj 1536); S17's wall-kick hands its flight to it; S371 survives its anim
  end; a held S371 lets the ax61 S12 overlay run out into the lose path
  (overlay S13, player S374, `k.bJ` 6). All 4 fail on the pre-fix code
  (S371 flung to S43; the overlay stuck in S12 via the abort branch).

## Coverage note

- A probe run over the whole `:core:test` suite found no existing test —
  capstones included — that enters S18 or S371: the bots never wall-kick
  and the m7 bot is never grabbed by Cesare. The behaviour change is
  therefore confined to wall-kick flights and the Cesare grab; both need
  the Phase 2 device check.

## Gates

Verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
`:core:test` 1614/1614; `:gdx:test` 9/9; `:android:assembleDebug` OK.
