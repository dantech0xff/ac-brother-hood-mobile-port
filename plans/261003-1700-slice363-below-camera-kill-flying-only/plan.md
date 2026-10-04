---
title: "Slice 363 — the below-the-camera kill is i.B()'s, reached only by the flying player"
phase: "port"
status: "done"
slice: 363
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:968-1052
  - reconstructed-project/bytecode/g.javap.txt:17392-17406
  - reconstructed-project/bytecode/i.javap.txt:19655-19687
  - reconstructed-project/bytecode/i.javap.txt:19877
  - reconstructed-project/src/structured/i.java:3921-3922
---

# Slice 363 — no below-the-camera kill for a ground player

Reported by the m7 capstone sub-agent (Phase 2): `Slice303Test.mission7UpperWing`
died on a ledge drop the moment the S257 landing happened under a camera
still held by scroll trigger uid218. Re-verified here against the bytecode.

## Findings (proven, bytecode)

1. `!v() && al > k.P + 240 → k.l(12)` is inside `i.B()` (structured
   i.java:1034-1035), next to the flying-mode %260 canyon probes.
2. `i.B()` has one caller: `g.n()` offset 13 (g.javap.txt:17406).
3. `g.n()` has one caller: `i.I()` offset 1256 (i.javap.txt:19877), the
   target of `case 25` in the `tableswitch` on `this.ax` at offset 635
   (i.javap.txt:19661/19687). A ground player is `case 0 → k.aS.e()`
   (structured i.java:3921-3922) and never reaches it.

The port runs `i.B()` for the flying player in `Entity.canyonCollide`
(L1f7) from `flightTick`, and additionally ran the same kill at the end of
every frame in every mission (`Level0World`, "below camera bottom").

## Fix

The end-of-frame check is gone; the flying path is unchanged.

## Tests

`Slice363Test` (2): a ground player left below a bounded camera and out
of view lives on (fails on the old code: `jC == 12`); the flying player
below the camera still dies through `g.n()` → `i.B()` (guard).

## Impact

`Slice303Test.mission7UpperWing` passes with the sub-agent's bot change
(branch `claude/p2-bots-m7`, merged). Other capstones are re-validated on
the Phase 2 branch.

## Follow-up

- `flightAliveV`/`canyonCollide`'s `yOverlapsCam` are a documented subset
  of `i.v()` (no `u()`/`au > i` guard, no point-box reject).
