---
title: slice 232 — S63 climb-down entry proven-dead on shipped content
phase: fidelity-verdict
status: done
---

## Summary

Verdict test: the `am()`/`wallClimb` arm (PlayerFsm.kt:1610, g.java:301
verbatim) gates on `aV == 19` / `aW == 19` / `aR == 19` **exactly** —
but no shipped level pack's `et` collision grid contains a type-19
cell, so the only S63 entry can never fire in the original game either.

## Evidence

- `g.java:663+` — `am()` checks `r1 = 19; if (r0 != r1) goto skip`
  (exact equality, verbatim).
- Cell census across all 8 packs (level0–level7 `et` layer-0 grids):
  zero 19 cells. Level0 values: {2, 5, 20, 21, 24, 25}; level5 adds
  type 3; level1/4 add 22.
- `enterStateMasked(63, 16385)` at `wallClimb` is S63's only entry in
  the port — unreachable.
- Consequence: S60's `Q == 63` hang-wait sub-branch is dead-letter
  (Q can never be 63); S60 stays live via `ledgeLipGrab` (Q=43
  auto-mantle, slice 231).
- Type 19 is likely a "climbable wall face" marker carried over from
  the shared Gameloft engine (used in sibling titles), unused in this
  game's shipped levels — consistent with other proven-dead arms.

## Changes

- Test: scans all 8 packs asserting zero type-19 cells.
- `PlayerFsm.kt` wallClimb doc: PROVEN-DEAD annotation (no behavior
  change — the arm stays verbatim for parity).

## Gates

- verifier `ok:true`, unittest 57 pass
- `:core:test` green (new Slice89 test passes)
- `:android:assembleDebug`, `:gdx:build` green
