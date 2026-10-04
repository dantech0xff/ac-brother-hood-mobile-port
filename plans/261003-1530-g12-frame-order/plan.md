---
title: "G12 — k.I() ticks every bb[] entity before aS.I(); the player's I() runs in its own order"
phase: "port"
status: "done"
slice: "G12"
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:2529-2600
  - reconstructed-project/src/structured/i.java:3853-3925
  - reconstructed-project/src/structured/i.java:5293-5295
  - reconstructed-project/src/structured/g.java:346-360
---

# G12 — frame order

Found in the 2026-10-03 baseline audit (Phase 1 table, row G12); landed on
the Phase 2 integration branch `claude/phase2-faithful-ai` together with
G5 (slice 357) and slice 359, because all three change how the capstone
bots' routes play out.

## Finding (proven)

`k.I()` (structured k.java:2529-2600) runs, per frame:

1. every `bb[]` entity: `u()` then `I()`, then its `ac`/`ab` links
   (the bh3 arm adds the copy-group consumption);
2. `aS.I()` — the player's own `I()` (i.java:3853-3925): L34 anim advance
   `s()`, `b = true`, the claim gate (ax0: `k.E.P |= 128`), the `i.cu`
   freeze, the head integrator, `g.d()` (`b(a)`: the ax43 ride snap,
   g.java:346-360), `bh3 → bF()`, `case 0 → g.e()` / `case 25 → g.n()`,
   then the dispatch tail `if (b) t()` (i.java:5293-5295);
3. `aS.ac`, `aS.ab`, `aS.ad` (ax -999) link ticks.

The port ran `g.e()` → integrate → `s()` for the player **before** the
entity loop.

## Fix

`Level0World` ticks the entity loop first, then `tickPlayerI()` in the
order above, then the player's links (commit feb4caa8, merged in
65c3c3fa).

## Consequences (all verified, none weakened)

- Every entity now reads the player's previous-frame position and boxes;
  the player reacts one frame later relative to the world. Unit fixtures
  that teleport the player call `refreshBoxes()` so the first entity pass
  reads a finished frame's boxes (63b7e8de, 5d62d583).
- Slice 215's "wedge" verdict was an artefact of the old order (erratum in
  `plans/260924-1818-slice215-ax7-wedge-verdict/plan.md`).
- The capstone bots were re-routed or re-timed mission by mission — see
  `../261003-0700-parity-gap-closure-android-hardening/reports/capstone-revalidation.md`.
- Re-validation surfaced four more parity bugs, fixed as slices 360-363.

## Gates

On `claude/phase2-faithful-ai` @ 1e7bb59d: verifier `ok:true`, unittest
57/57, `:core:test` 1689/1689, `:gdx:test` 9/9, `:android:assembleDebug`
green.
