---
title: "Slice 354 — the pickup sparkle field in i.F(): sign gate, g range, radial rays, f advance"
phase: "port"
status: "done"
slice: 354
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:16860-17125
  - reconstructed-project/src/structured/i.java:3175-3283
  - reconstructed-project/src/structured/i.java:13800-13835
  - reconstructed-project/src/structured/j.java:913-915
---

# Slice 354 — the pickup sparkle field

Found while auditing the `j.b` call sites for slice 352.

## Finding (proven)

`i.F()` runs, for `this == k.aS && j.c == 8`, the `i.e` sparkle block
(bytecode `i.javap.txt` F() offsets 2709-3238; structured
`i.java:3233-3283`). `i.e = 30` is armed by the ax24 S20 pickup
(`i.java:13827`; port `NpcFsm.kt` ax24 arm). Per frame:

1. `e--`; lazy `f/g/h = new int[360]`.
2. Spawn loop: `r = j.j.nextInt()`; **`r >= 0`** (`2857: iflt`) `&&
   (r & 127) == 0 && g[i] == 0` → `g[i] = j.j.nextInt() % 40 + 60` (no
   abs: 21..99), `h[i] = |j.j.nextInt() % 60| + 60`, the dead
   `g < 0 → h = −h`, `f[i] = 0`.
3. Draw loop: `g[i] > 15` → `setColor(-1)`;
   `j.a(bg, ox + sin·(g+f), oy + cos·(g+f), ox + sin·(h+f), oy + cos·(h+f))`
   with `sin = j.b(n−θ)`, `cos = j.b(θ)`, `θ = i·m/360` — one white radial
   line (`j.a(Graphics,int,int,int,int)` is `drawLine` in the rotated
   canvas, `j.java:913-915`); then `f[i] += 15`, `f ≥ 100 → f = g = 0`.

The port (since slice 239) skipped the `r >= 0` gate — doubling the spawn
odds and **drawing extra values from the shared `j.j` stream for 30
frames after every pickup** — wrapped `g` in abs, drew an invented
four-dot "rosette" per slot, and never advanced `f`, so a slot, once
spawned, stayed lit forever.

## Fix

- `Entity.drawStyleF`: the block follows the offsets above and emits each
  ray through `drawFxLine(..., -1)`.
- The dot plumbing (`drawFxDot`, `fxDots`, the renderer's 2×2 fills) had
  no other producer and is removed.

## Tests

- `Slice354Test` (2): thirty frames after `i.e = 30` checked against an
  independent LCG replay — same `j.j` state after every frame, same
  `f/g` arrays, same rays (endpoints and colour); at least one `g` from a
  negative `r % 40`. A single ray lives exactly seven frames.

## Impact

- RNG stream: after a pickup the port now consumes the same number of
  `j.j` draws as the original, so later random decisions line up again.
  No capstone changed outcome.

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test`, `:gdx:test`,
`:android:assembleDebug` green (counts in the commit).
