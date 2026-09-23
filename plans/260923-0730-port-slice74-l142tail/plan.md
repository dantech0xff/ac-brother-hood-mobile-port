---
title: "slice 74 — k.I() L142 tail: goal-arm win check, claimer step, bh3 snap"
phase: port
status: done
---

# Slice 74 — the `k.I()` L142-L200 tail (`k.java:3321-3358`)

The per-tick tail that runs after BOTH camera arms (`m(cJ)` falls
through to L142; `D()` `goto`s it) — ported as `l142Tail()` +
`iW()` in `Level0World`.

## What landed

### Goal arm (L142-L159, proven)

`k.aV` — the `r8[5]==0` ax9 contact block `initAx9` binds — is the
mission-goal entity. While `Z[0]==1` (script ops L146/L147 arm/disarm,
already wired via `runBigOp` arms 4/5) and `aV.S ∉ {4,5}`:

- `i.w()` (i.java:793-816, `iW()`) classifies the goal's anchor vs the
  camera right edge `ac[2] = O+400`:
  `v()`-onscreen → 0; anchor inside `(ac2, ac2+200)` while offscreen →
  1 (milestone band); `>200` beyond → 2 (win); at/behind → 3. The two
  interior `v()` re-evals are kept verbatim (decompiler-artifact arms —
  `v()` is pure).
- `w()==1` → the `j.f`-even `z[9]` font blit — draw unported; the
  `goalTicker` flag exposes it for the renderer (`inferred` — tickIndex
  stands in for `j.f` parity).
- `w()==2` → `bx = 56; l(13); bw = 0` — the scripted win, routed
  through `screenL(13)` (`won`/`missionWon` latch).
- The `j.c ∈ {13,31}` skip maps to `won`; screen 31 has no analog yet
  (`inferred`).

### Claimer step (L161-L167, proven)

`C.cd[2] && C.cd[1] && C.ab()` → `C.aa()` — one claim-script VM step per
tick on the fast-forwarded claimer, on top of its own tick. Verified:
`cd[5]` run-latch fires and the consumed block releases via `bI()`
(`scriptStep=-2`, `kC=null`, `cd[2]=false`).

### bh3 tail (L172, proven)

`cA=O; cB=P` — camera targets snap to the just-lerped position so an
early-returning `D()` can't drift.

## Gates

- `verify-static-reconstruction` → `ok: true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → 581 pass (5 new `Slice74L142Test`: band win, band
  no-win, disarmed no-fire, claimer step, no-ff no-step)
- `:android:assembleDebug` → green

## Confidence

- `proven`: L142 CFG, `i.w()` band math, `bx=56; l(13); bw=0`,
  `cd[2]&cd[1]&ab()→aa()`, `cA=O; cB=P`.
- `inferred`: `goalTicker` ↔ `j.f`-even font blit (render unported);
  `j.c==31` guard (no screen-31 analog).
