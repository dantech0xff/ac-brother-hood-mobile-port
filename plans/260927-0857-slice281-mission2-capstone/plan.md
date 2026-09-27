---
title: "Slice 281 — mission-2 capstone bot legs"
phase: capstone
status: done
---

# Slice 281 — mission-2 capstone

Bot-driven legs on `world(aj = 2)` proving the mission-2 route from spawn
toward the win, same pattern as slices 279/280 (waypoint route + per-leg
logic inside `while`/`for` bot loops, real input only via `w.pad.e(mask)`
+ `w.tick(emptyList())`, no state pinning in the final test; `keepLive`
only on offscreen entities).

## Fidelity fixes this slice (all proven in i.java)

1. **`I()` head integrator** (i.java:3886-3918) — every entity reaching
   the dispatch integrates its velocity *before* the arm switch, with
   the `aH` slow-mo `aI`-divided variant and `bh[k.aj]==3 → bF()` before
   dispatch. Ported at the top of `tickNpc` (Level0World.kt:4941).
   This is what actually moves ax66 platforms and ax43 carriers — they
   have no self-integrate in `bm()`/`bw()`.
2. **`integratedThisTick` flag** (porting aid, Entity.kt) — the dispatch
   head sets false → integrate → true; arm-tail `e.integrate()` calls
   become `if (!e.integratedThisTick)` fallbacks so dispatched entities
   never double-integrate while direct arm calls (tests) still run it.
   Guarded tails: soldier arm (~269), `ay()` runner tail (~6708),
   ax56 tail (~6890), ax24 `ba()` tail (~7167).
3. **`b(true)` relabel** — i.java:5459 `b(boolean)` is the *ad-link
   sync* (`syncAd`), not the integrator. Runner tail now calls
   `e.syncAd(w)` with the `bF()` copy guarded under the flag.
4. **`g.b(k.aS.S)` → `p.gB()` mount gates** (i.java:15393+ / 15503) —
   the ax66 S11/12/13 mount gate and the S19-22 arc-mount arm tested
   the *free-anim set* `g.b(int)` (falls S43 qualifies), not
   `playerAttacking()`. Both call sites corrected — this is what made
   legA's pit crossing possible (falling player mounts aw336/aw669).
5. **ax56 test ordering** — `mode-1 travels` and `arrival stops and
   idles` zeroed velocity before the tick to encode the faithful
   head-integrate order (integrator runs before the arm evaluates).

## Legs

| leg | route | result |
|-----|-------|--------|
| legA | spawn (60,1840) → corridor → ax22 vault → shaft zigzag → pillar mass top | PASS — `S43 @(1972,1550)`, `ak>=1900 && al<=1560` |
| legB | `2`@1340 walkway → mass hop → aw30 vault → aw33 carrier → tower C top | PASS — `S313 @(1056,1185)` |
| legC | pocket tip (2930,1939) → M_LEFT+UP vault → S101 wall-kick → `20`@1880 | PASS — `S21 @(2681,1939)` |
| legWin | full win route: pocket tip → wall-kick → rail aw142 → S164 → S295 ax66 ride → aw306 → script 307 | PASS — `S295 @(4421,1946)`, `jC=15` |

## Gates

- verifier `ok:true`; `python3 -m unittest` 57/57; `:core:test`
  **1502 tests, 0 failures**; `:android:assembleDebug` green.
