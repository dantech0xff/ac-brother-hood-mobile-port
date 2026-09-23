---
title: "Port slice 123 — ax13 rope-segment draw (i.a(Graphics) last unported arm)"
phase: rewrite-port
status: done
---

# Slice 123 — ax13 rope draw (`i.a(Graphics)` i.java:13391)

## Scope

Port the last unported `i.a(Graphics)` draw arm: ax13's `a(bg)` rope-
segment renderer (i.java:3050-3052 → :13391-13411). The ax13 branch
runs BEFORE the per-ax art-select block (it's the `else` in the
original's structure) and BEFORE the standard `(P&128)==0` blit — so
the port draws the rope segments first inside `drawEntity`, then falls
through to the existing blit unchanged.

## Mined semantics (i.java:13391-13411, proven)

- Screen anchor `i = (N>>8) - k.O`, `i2 = (O>>8) - k.P`.
- Pendulum angle `i3 = bP >> 8` (angle-256 units).
- Segment step: `iB = (3072 * j.b(n - i3)) >> 8` = 12px·cos θ;
  `iB2 = (3072 * j.b(i3)) >> 8` = 12px·sin θ (`j.b` = `Trig.sin`,
  `j.n` = `Trig.N` = 64).
- `i4 = Z[1] - 1` middle segments; `aG == 4` → `i4 = bN - 1` (the
  ax58-gated spawner arm counts into `bN`).
- Draws: anchor `aa.a(g, Z[7], i, i2, P, 0, 0)` — entity P as draw
  flags — then `i4` segments stepping `i5 += iB; i7 += iB2` (with
  `i7` seeded `i6 + 3072`, i.e. first segment 12px below anchor), then
  the end segment — all flags-0 object `Z[7]` blits.

Rope entities are script-spawned, not level-0 records — contract tests
use the same fixture shape as the slice-45 `aW()` tests.

## Tests

`Slice123Test` — `initAx13` populates `Z` ≥ 8 slots with `Z[1]` segment
count; the `aG==4` arm keeps `bN` as the draw count source.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test`, `:gdx:build`,
  `:android:assembleDebug` green.
