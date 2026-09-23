---
title: "Slice 149 — last aV() arms: S8 alert-toggle + S18 spawn-release"
phase: port-slice-149
status: merged
---

# Slice 149 — the final aV() arms

Ports the last two unported `aV()` arms — with these, every reachable
`aV()` case (56 dispatch cases) is either ported or a documented
bare-`return` dead arm. `aV()` is now fully covered.

## Arms (proven)

- **S8 = L152b** (i.java:12052-12076; dispatch `case 8` i.java:9177):
  alert-toggle zone — overlap flips `i.bn` and self-removes; `bn` now
  true → `i(0)` + `E()` settle + `i(79)` + `g.z=0`; now false → `i(80)`
  + `g.z=1` + `k.v()` latch clear.
- **S18 = L1c98** (i.java:12952-13070; dispatch `case 18` i.java:9187):
  spawn-release zone — overlap + `o`→ax4 at `S==33` with `aA∈{0,2}` →
  `P&=~32`, `P&=~128`, `P|=16`; then teleports the target just offscreen
  on its side of the player (`ak>player.ak` → right edge `camR+width`
  with `ag=-p`,`av=1`; else left edge `camL-width` with `ag=p`,`av=0`),
  `al=camT+70`, `aA=1`. `k.ac[]` = the camera rect (`camRect`).

## Dead arms (verbatim `goto L1ec7` — documented, no code)

Cases 1, 11, 15, 19, 20, 25, 26, 27, 35, 37, 38, 39, 52 — all jump to
the shared bare-return. S9=L1658 also bare-return. The `when` falls
through for all of them; noted in the dispatch comment block.

## Gates

Verifier ok; 57 unittests; `:core:test` 1045 green (5 new Slice149);
`:android:assembleDebug` + `:gdx:build` clean.

## Coverage milestone

`aV()` = 42 real arms ported + 14 bare-return states documented = the
entire 56-case dispatch is now accounted for. Remaining ax10 surface:
any arms outside `aV()` (init/proc helpers) only.
