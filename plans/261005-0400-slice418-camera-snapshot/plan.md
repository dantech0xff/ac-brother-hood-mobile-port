---
title: "Slice 418 — i.v() culls the k.ac snapshot, not the live camera"
phase: parity-gap-closure
status: done
slice: 418
branch: devin/1791172183-slice418-iv-snapshot
---

# Slice 418 — `i.v()` reads the `k.ac` snapshot (P1b)

Handoff item **P1b** from `plans/261008-2000-handoff-after-slice416/plan.md`:
"`i.v()` reads the previous tick's camera rect `k.ac`, the port reads the
live rect — a one-tick cull edge. Prove whether it is observable before
spending a slice on it." Proven **observable**: the port's `camRect` was a
derived getter over `camX/camY`, so any mid-tick camera write moved the
cull edge immediately; the original's `k.ac` is a real `int[4]` only the
camera phase rewrites.

## Byte-proven claims

| Claim | Evidence |
|---|---|
| `i.v()Z` ends `a(k.ac, W)` / `a(k.ac, Y)` for the cull arms | i.javap.txt `v()` @244-334 |
| `k.ac` is a plain `int[4]` field written ONLY by `iastore` inside `k.D()` (@52-91 snap arm, @651-690 tail) and `k.m(int)` (@2680-2719 tail, after the `ai()` settle latch); `k.<clinit>` allocates it | k.javap.txt; `xref.py k.ac` (41 refs, no other stores) |
| `k.I()` (the world tick) runs `i.I()` ×10 + the player `i.v()` at @318 BEFORE `k.D()` @1020 / `k.m(I)` @1029 — entities tick against the PREVIOUS tick's rect; `i.w()` @1089 runs after and sees the fresh one | k.javap.txt `I()` call order |
| Mid-tick camera writes exist that do not touch `k.ac`: the group-script camera lerp `r04==1` arm writes `k.O`/`k.P` directly (Entity-side, proven), the `kDU` world-shift writes `k.P`, claim-camera `k.Z` writes | Entity.kt:2342 (`w.kO += …`), simI :5448 |
| `i.u()` reads live `k.O`/`k.P` (u() @4/@17) — only the `a(k.ac,…)` overlap is stale | i.javap.txt `u()` |

## Was → now

- `camRect` was `get() = intArrayOf(camX, camY, camX+400, camY+240)` — live.
- Now a real `ac = intArrayOf(0,0,400,240)` (the `<clinit>` value);
  `rebuildCamRect()` writes `{camX, camY, +400, +240}` at exactly the
  original `iastore` positions: `kM(r5)` tail, `kD()`'s claim/`j.c==21`
  snap arm, `kD()`'s autoscroll tail.
- `kO`/`kP` setters stay raw `putstatic` — mid-tick writes leave `ac`
  alone (the fix).
- Tests that stage `w.kO`/`w.kP` model "a camera phase already ran" and
  now follow the staging with `w.rebuildCamRect()` (internal; 158 sites
  updated by script). Two intentional non-rebuilds in `Slice418Test`
  prove the stale edge.
- `i.w()`/`iW` ordering already correct (it reads post-phase ac — the
  port calls it from `l142Tail` after `kM`/`kD`).

## Test results

- `Slice418Test` +4 tests (all four failed on the pre-fix code):
  stale cull in both directions, `kD` tail rebuild, `kD` snap-arm rebuild.
- Full `:core:test` green after fix + staging updates (19 staging sites
  initially failed — they staged the camera but never ran a phase).
- Mutants `scripts/mutants/slice418.py` A–E: **5/5 killed**, narrow filter
  AND full suite (mutant C is also killed by 47 pre-existing tests).

## Parked

- `k.ac` is process-static in the original and survives level loads; the
  port's `ac` is per-`Level0World` but the world instance persists across
  missions (`loadMission` reloads in place), so the carry matches. A
  brand-new `Level0World` starts `ac={0,0,400,240}` where the original
  would keep the previous level's rect — first-tick-only, no shipped path
  hits it (spawn `E()` probes use `a()` not `v()`).
- `u()` already used live `kO/kP` — no change needed.
