---
title: Slice 150 — player e() climb arms (27/28-318/29-315/34)
phase: gameplay-port
status: merged
---

# Slice 150 — `g.e()` climb arms

Port the four remaining `e()` player-state arms from
`reconstructed-project/src/fallback/g.java` (structured dump elides
these `if` bodies):

- **case 27 (L27da, :5811-5818)**: `ac == null → a(0)` fling, then the
  shared `e()` tail.
- **case 28 / 318 (L23fa)**: freeze `aj=ai=ah=ag=0`; edge key
  `k.v(33024)` → `a(ah)` fling.
- **case 29 / 315 (L23bb)**: `g.o != 0` → `al > o` upgrades the climb
  anim (315→318, 29→28); else `g.k == -1` → `a(ah)` fling.
- **case 34 (L1a0a, :4012-4100)**: `aA()` wall-kick marker input;
  `(av ? aT : aU) != 20` → `a(0)` fling; `aR >= 19 || aR == 5` → `l()`
  wall-climb input + `k.v()` latch clear.

Helper mapping (all already ported): `a(int)` g.java:126 →
`Entity.flingAirborne`; `aA()` g.java:5545 → `wallJumpKick`; `l()` /
`ax()` / `aw()` g.java:5158/5290/5238 → PlayerFsm `l`/`ax`/`aw`; the
shared `e()` tail (L353d :7568+, the `J&4` mount-offer + `aO==2`
floor-check chain) → `postTail` which `tick()` always calls after
`dispatch`. `g.o`/`g.k` statics live on the player as `go`/`gk`.

## Verification

- `Slice150Test` (7 tests): S27 fling/hold, S28 freeze+fling,
  S29/S315 anim upgrade, S29 gk fling, S34 latch-clear/fling paths.
- `python3 scripts/verify-static-reconstruction.py` → ok:true
- `python3 -m unittest discover -s tests -t .` → 57 pass
- `:core:test`, `:android:assembleDebug`, `:gdx:build` all green
