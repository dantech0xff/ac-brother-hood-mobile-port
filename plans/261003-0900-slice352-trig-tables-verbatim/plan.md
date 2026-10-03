---
title: "Slice 352 — j's trig/sqrt tables verbatim from archive /16 (j.b is cos)"
phase: "port"
status: "done"
slice: 352
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:4009
  - reconstructed-project/src/structured/j.java:89-95
  - reconstructed-project/src/structured/j.java:303-307
  - reconstructed-project/src/structured/j.java:334-429
  - reconstructed-project/src/structured/j.java:637-655
  - reconstructed-project/src/structured/j.java:803-814
  - reconstructed-project/src/structured/j.java:988-1062
  - reconstructed-project/resources/archive/16
---

# Slice 352 — `j.b` is cos: the `/16` tables verbatim

Phase 1 item 1.7 (G7, `Trig`) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).
The audit rated this "low impact"; it was the opposite.

## Finding (proven)

- `k` loads the math tables with `j.a("/16", 0, 1)`
  (`structured/k.java:4009`) → `T = f(0); U = f(1)`
  (`structured/j.java:303-307`) — archive `/16`, not pack-2 as slice 21
  assumed.
- Archive `/16` (667 B, sha256 `2c1e6eb6…963e46`): `af=2` entries, one
  part, offsets `ag = {18, 151, 667}`. Each entry is marker `03` (stored)
  plus a typed object (`j.a(InputStream)`, `j.java:988-1062`): header
  `0x12` = `int[]` of 65 LE shorts; header `0x1A` = `int[]` with a u16
  count (256) of LE shorts.
- `T[i] = trunc(256·cos(πi/128))` — a **cosine** quarter table (T[0]=256,
  T[64]=0). `j.b(θ)` (`j.java:334-349`) mirrors it, so `j.b(θ)` = cos θ
  and `j.b(n−θ)` = sin θ. Only a cosine table makes the mirror arms
  consistent (`j.b` even, `−T[W−r]` in quadrant 2). The port generated a
  **sine** table; every literal `j.b(x)` → `Trig.sin(x)` call site was a
  quarter turn off (and the quadrant-2/4 signs were wrong):
  - `at()` (`g.java:4753-4758`) at the rest angle `cy = o` put the rider
    `cB` px **left** of the pivot instead of below it;
  - the ax13 rope integrator `bO −= j.b(n−θ)<<1` (`i.java:13241`) had
    its equilibrium at θ=64, so the port invented `bP = aG<<12` to make
    ropes hang (`NpcFsm.kt` initAx13, "verified empirically"); the
    original only ever writes `bP = 0` (`i.java:662/1916/13348/13372`);
  - throws, the cart arc, the dotted rope, ax35 waves, ax74 polar
    flight, shot fans — all rotated.
- `U[i] = floor(16·sqrt(i))` for all 256 entries, `U[0] = 0`. Slice 205
  read the blob from offset 154 as big-endian and took the count's high
  byte for `U[0] = 256` (`j.d(0)` = 16 instead of 0).
- `j.b(x, y)` (`j.java:371-416`) is an octant atan over
  `V[k] = search(0, n, k)` (`j.java:359-377`, binary search on
  `c(θ) = (b(n−θ)<<8)/b(θ)` = tan). The port's rounded float `atan2`
  differed by ±1 on ~41% of a ±60 grid and wrapped `X − V[0]` (256) to 0.

## Fix

- `Trig.kt`: `T`/`U` verbatim from the archive; `cos` (= `j.b(int)`),
  `tan` (= `j.c`), the `V[]`/binary-search `atan2` (= `j.b(int,int)`),
  `sqrt` (= `j.d`). `Trig.sin` → `Trig.cos` at every call site (all are
  literal `j.b` transliterations — checked against `g.java:4626-4921`,
  `i.java:3268-3271, 6279-6288, 13241-13311, 13379-13396, 18773,
  19478-19479, 19797-19798, 20051-20063`); comments that called them
  sin/cos the old way are corrected.
- `Entity.isqrt`/`SQRT_U` removed — `arcSolve` uses `Trig.sqrt`.
- ax13 init no longer invents `bP = aG<<12`.
- The rope divide guard (`i.java:13311`) comment now says what the
  original does: `12·cos θ = 0` only at θ=±64; the divide throws, `paint()`
  catches it with `c = −1` and the MIDlet exits (`j.java:256-261`) —
  accepted deviation: the port treats it as a segment miss.
- `junit-platform.properties`: 60 s default timeout, separate thread — a
  fixture angle that never ends the ax35 march loop hung the suite.

## Tests

- `Slice352Test` (10): re-parses `archive/16` with the `j` loader rules and
  compares `T`/`U` byte for byte; cosine/sqrt formulas; `cos` even,
  periodic, quadrant values; `tan` poles; 23 `j.b(x,y)` vectors (computed
  by an independent Python transliteration) incl. the 256 return; `V`
  samples; `j.d(0) = 0`; `at()` hangs the rider below the pivot; `c()` +
  `at()` return the rider to its start point; ax13 rests at `bP = 0`.
- Fixtures that encoded the sine table, fixed rather than duplicated:
  `Slice205Test` (`d(0)` = 0), `Slice44Test` (ax35 wave Z[5] = 125° — the
  value 26 of 28 real records carry; angle 0 never ends the original's
  march either), `Slice277Test` (victim due east → drag `ag = +5120`,
  `ah = 0`, not the old "down-drag"), `Level0WorldTest` cart (cart above
  the rider at `cy = o`).

## Impact

- Capstone mission 4 (`Slice288Test`) passes only together with slice 353
  (the escort fans are now aimed where the original aims them); see there.
  Every other capstone passes unchanged.

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test`, `:gdx:test`,
`:android:assembleDebug` green (counts in the commit).
