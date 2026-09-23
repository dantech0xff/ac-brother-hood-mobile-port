---
title: "Slice 157 — dedup i.a(boolean) / i.y() into the verbatim ports"
phase: port
status: done
confidence: proven
---

## What

Removes three duplicate port pairs that accreted across slices — keeping
the verbatim-correct version of each, which also FIXES real fidelity
gaps the leaner copies had been silently skipping.

### `i.a(boolean)` — one function, two ports

- `collideSides(world, resolve)` (13 call sites, slice ~132) was missing:
  1. the `i4 -= 10` crouch-strip extension for `ax==0 && S∈{12,7,32,199}`
     (i.java:836-838);
  2. the entire `resolve && bd` ground-strip `al` pre-adjust
     (`aR>=10|5 → al-=x()`, `aO>=12&&!23 → +cell`, `aQ>=12|5&&!23 →
     +half`, i.java:845-853) and its `t()` recompute.
- `probeSnapSides(z2, w)` (1 call site, eSettle) was the verbatim port.
- Resolution: `collideSides` now carries the verbatim body (param order
  `world, resolve` kept so all 13 callers are untouched);
  `probeSnapSides` deleted, its caller redirected. Every `a(true)` call
  now also runs the ground-strip adjust — original behavior restored.

### `i.t`/`i.u` center — two field pairs

- `centerX`/`centerY` were written once (inside `probeSnapSides`) and
  never read anywhere → deleted.
- Canonical `tc`/`uc` (read by the `mask&4`/`mask&32` center-adjust arms)
  is what `a()` now writes.

### `i.y()` — two ports

- `hitWall()` (older) and slice-156's `wallOnFacingSide()` are the same
  truth table → deleted `wallOnFacingSide`; callers use `hitWall()`.

## Tests

Slice-128-era `probeSnapSides` tests renamed to `collideSides` (they now
assert `tc`/`uc`); all 3 suites still green unchanged — the dedup is
behavior-preserving for tested paths.

## Gates

verifier ok:true | 57 unittests | :core:test all green |
android+gdx builds OK
