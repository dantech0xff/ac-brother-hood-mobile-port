---
title: "Slice 384 — bB() S28 foreign ae, one i.p(), one u()"
phase: "port"
status: "done"
slice: 384
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bB() @420-537)
  - reconstructed-project/src/structured/i.java:214-227
  - reconstructed-project/src/structured/i.java:8963-8973
---

# Slice 384 — slice-371 leftovers

From the slice 371 report (items 3, 5, 6, 7).

## Findings (proven) and changes

- `bB()` S28 (bytecode @438-537): when the player's `ae` is missing or not
  a 71, `aS.G(); aS.a(71, ak, al)`, then the pin (`ak = O+20` or
  `O+400-20` by side, `al = al`) and `return`. The port fell through to
  the shove check when the `ae` existed but wasn't a 71. Fixed.
- `i.p()` (i.java:214-227): `W = X = Y = null`, `ab = null`, `ad.p()`,
  `ad = ae = af = c = null`, `aS()` (the `cr` pool drop, i.java:8963).
  Two ports existed: `deactivate()` (missing `ab`, `c`, `aS()`) and
  `releaseCascade()` (missing `aS()`). Folded into `deactivate()`.
- `u()`: `offscreenScore(world)` now delegates to `recomputeAu` (the two
  bodies were equivalent).
- The m3/m4 capstone comment named the deleted `projSweepBc`; it names
  `Entity.bc()` (comment only).

## Tests

`Slice384Test` (3).
