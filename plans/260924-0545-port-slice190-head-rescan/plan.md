---
title: "Port slice 190 — g.e() head fidelity: a(an()) rescan + cn clamp position"
phase: port
status: done
---

# Slice 190 — `a(an())` head rescan + `cn` clamp position

## Findings (mining)

Two `g.e()` head gaps found via the method-surface inventory:

1. **`a(an())` was never ported** (g.java:615, proven). The original runs
   `i.a(z2)` — full `x()` cell probe + side-strip scan + gated wall-push —
   in the `e()` head **every tick for every state**, before the dispatch
   `switch`. The port only called `collideSides` inside specific arms, so
   `bb`/`bc`/`aT`/`aU`/`aX`/`aY`/`aO`/`aR`/`aZ`/`bd` went stale between
   arms (wall-cling/ladder arms were reading last-arm's flags).
2. **`ah>5120` clamp sat post-dispatch** with an unconditional `cn++`.
   The original runs it in the head (g.java:602-607, proven):
   `ah>5120 → ah=5120; cn++` **else** `cn=0` — `cn` counts *consecutive*
   capped ticks and resets on the first tick under the cap. `cn` is
   write-only (debug counter, no live consumer) but is ported verbatim.

`an()` (g.java:335, proven) — the `z2` resolve gate:
`S<=43 || S==150 || {67-69,199,216,217,298} || {20,49,243,259-266}`
(S20/49/243 overlap `<=43` — verbatim redundancy). Side-strip probes run
either way; the flag only gates the `z2` resolve half (bd-snap + `t()` +
`bb`/`bc` wall-push). Ported as `Entity.rescanEligible()`.

## Port

- `PlayerFsm.tick()` head order now: `i(50)` dead → `kAA→aA|=1` →
  **`if (ah>5120) { ah=5120; gCn++ } else gCn=0`** →
  **`collideSides(world, rescanEligible())`** → bh3/dispatch → postTail.
  Post-dispatch clamp removed (original has none there).
- `Entity.rescanEligible()` — `an()` verbatim.
- `Entity.refreshBoxes()` — `clip==null` now returns early **without**
  zeroing `W` (the JAR never runs `t()` clipless — `bi[]` always assigns;
  spawned clipless records have ctor-zeroed `W` anyway). Needed because
  the head rescan calls `t()` every tick and tests stage `W` manually on
  clipless entities.

## Test restaging (fidelity consequence)

The head rescan recomputes the flag space from cells every tick, so
13 tests that *pinned* `aQ`/`aR`/`aO`/`aT`/`aU`/`aY`/`aZ`/`bb`/`bc`
directly became unfaithful (the original can't hold pinned flags either).
Restaged with real geometry instead of weakening assertions:

- `S43 aQ!=3` → `MarkerWorld(cell=0)` (aQ=0).
- `comboArm footing lost` → `cell=18` (aR=18 → aZ=false).
- `S12 wall face` → cellFn columns produce `aU=20`, `aY=7`, `aO=0`
  verbatim on a deep strip; col 11 solid for `z()`'s push column.
- `S102/S332` cling/shimmy → feet cell 4 (aR=4, aZ=false) or 12 (aZ=true).
- `S317 wall-hit` → feet cell 12 (aR≥12 contact).
- `S34 latch` → left strip 20 + feet 5 (aT=20, aR=5).
- `S90 settles` → `standOn` real ground (aR≥20); `S90 mid cell` →
  `MarkerWorld(cell=0)` (aR/aS=0).
- `aO6 fires/suppressed` → head cell 6 (+feet 2 for the aR==2 gate).
- `ClaimZoneWorld`/`S150World` gained `cellFn` passthrough.

## New tests (`Slice190Test`, 4)

- `rescanEligible follows the an whitelist` — both boundary sets.
- `head rescan refreshes strip flags before the arm` — stale `aT`/`bb`
  pin overwritten by the strip column.
- `an whitelist gates the push not the probes` — S34 pushed +10 off a
  left-column wall; S102 not pushed but `bb` still refreshed.
- `terminal clamp caps ah and counts capped ticks` — 6000→5120, cn
  1→2→0 on the below-cap tick.

## Gates

- `verify-static-reconstruction.py` → `ok:true` (no failures)
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → green (1230 tests incl. 4 new)
- `:android:assembleDebug` → ok
- `:gdx:build` → ok
