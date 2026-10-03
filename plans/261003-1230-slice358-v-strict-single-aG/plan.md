---
title: "Slice 358 — v()'s overlaps are i.a (strict) and aG() is one function (crate-edge rule for ax11)"
phase: "port"
status: "done"
slice: 358
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/i.java:540-558
  - reconstructed-project/src/structured/i.java:610-631
  - reconstructed-project/src/structured/i.java:7258-7274
---

# Slice 358 — strict `v()` and a single `aG()`

Split out of [slice 357](../261003-1200-slice357-ax11-ac-scheduler/plan.md)
(G5, parked on branch `claude/g5-ac-scheduler`): the bisect there showed
these two helpers do not move any capstone, so they land on the main port
branch on their own.

## Findings (proven)

1. `v()` (structured `i.java:610-631`) ends with `a(k.ac, W)`,
   `a(k.ac, Y)` or `a(k.aS.W, W)` — all `i.a(int[],int[])`
   (`i.java:540-558`), which rejects a point box on either side. The port
   used `overlapI` (plain overlap), so an entity whose `Y` had collapsed to
   a point still counted as in play.
2. `aG()` (`i.java:7258-7274`) is one private method of `i`: while riding
   an ax51 crate (`s.ax == 51`) the edge is "within 20 px of the crate's
   facing edge"; any other support (`s != null`) means no edge; only
   with `s == null` does it test the facing-side cell (`e() == 20/5` →
   no edge). The port had two copies: `edgeAhead73` (verbatim, ax73) and
   NpcFsm's private `edgeAhead` (ax11 patrol `L370` and chase `L451`),
   which had only the cell test — an ax11 on a crate ignored the crate
   edge and could see a tile "edge" under the crate.

## Fix

- `Entity.inPlayV`: the four final overlaps use `overlapStrict` (= `i.a`).
- `NpcFsm.edgeAhead` delegates to `edgeAhead73` (the verbatim `aG()`).

## Tests

- `Slice358Test` (2): a collapsed `Y` is not in play (a normal one is);
  an ax11 chasing east on a crate within 20 px of its east edge drops to
  `i(k?3:2)` = S3, and the same soldier mid-crate keeps chasing (S4) —
  the mid-crate case fails on the old `edgeAhead` (got 3).

## Impact

No capstone changed outcome (full `:core:test` green before and after).

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test` 1658/1658, `:gdx:test`
9/9, `:android:assembleDebug` green.
