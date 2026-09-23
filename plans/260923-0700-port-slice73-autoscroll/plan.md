---
title: "slice 73 — k.D() bh3 autoscroll camera + i.X() checkpoint write"
phase: port
status: done
---

# Slice 73 — `k.D()` autoscroll camera + `i.X()` phase-checkpoint write

Port of the `bh[aj]==3` (missions 1/4) camera path, verbatim from
`k.java:2721-2860`, plus the `i.X()` checkpoint writer
(`i.java:18631-18710`) that the bh3 tick arm runs before `D()`.

## What landed

### Tick driver (`k.java:3319-3368` — proven)

- `L135: bh[aj]!=3 → m(cJ)` (`cJ=1`, k.java:8347) stays the default arm.
- `L137: else { if (i.bW) i.X(); D(); }` — new arm in
  `Level0World.tick`: pending director phase-write fires
  `writeIX(checkpointSnap?.aw ?: -1)` and clears `iBW`, then `kD()`.

### `k.D()` — autoscroll camera (`k.java:2721-2860` — proven)

- Snap guards: claimer `kC` with `cd[0]`/`claimActive()` while `kZ` →
  snap targets and return; `dialogModal` (`j.c==21`) → snap+return.
- `ae = aS` (platform under player); `W→X` one-shot wind drain
  (`if (W!=0) {X=W; W=0}`), `kY = X<<8` derived.
- Corridor rows/cols from `ae.W[0]/W[2]/20` etc.
- `k.ai` (scroll-locked): `boundMinX==-1 && iBV>0` → cached `cG/cH`;
  `== -1 && iBV<=0` → row-scan corridor; else keep prior — **inferred**
  for the lockless fallback (dL tunnel wrap not ported: mission-1/4
  pack data not loaded; `collisionCell` used as-is).
- Non-`k.ai`: cell-22 left/right sweep → `R=r6*20`, `S=(r7+1)*20-400`,
  `R>S → R=S`; `cN=ak`; `i.be` skips the `cA` write; `camA=cN-200`
  clamped `[R,S]`.
- `camB += kX` (wind pushes camera); `Q=al-cB` → `Q≤117` and `Q≥230`
  `ah`-clamps to `kY`, skipped when `i.bj`.
- `O += l(cA-O,4)`; `P += l(cB-P,30)`; `ac[]={O,P,+400,+240}`.

### `i.X()` — phase-checkpoint write (`i.java:18631-18710` — proven)

`writeIX(aw)` persists the extended `Snapshot`:
`aw` slot, `k.B.ak/al` (**player** pos at write time — not the
checkpoint's, verified at i.java:18634-18635), `av`, `x1`, `gJ`, `gI`,
`apStats.copyOf()`. `k.ax/ay/az/aN/aL` globals and the `br[]` dead-flag
block remain unmodeled (`unknown` — mission-script state).

### Test-semantics correction

`ax2 checkpoint fires on overlap and reload restores to it` previously
asserted respawn **at the checkpoint entity**. Proven source writes
`k.B.ak/al` — respawn where the player stood; assertion updated.

## Gates

- `verify-static-reconstruction` → `ok: true`
- `python3 -m unittest discover -s tests` → 57 pass
- `:core:test` → 577 pass (4 new `Slice73AutoCamTest`: bh3 drives D(),
  W→X one-shot drain, iBW write+consume, modal snap)
- `:android:assembleDebug` → green

## Confidence

- `proven`: D() body arms, L135/L137 driver, i.X() field writes, X<<8.
- `inferred`: dL tunnel-corridor fallback for scroll-locked levels
  (bh3 pack data not converted yet); `i.be` (no-write flag) semantic.
- `unknown`: `k.ax/ay/az/aN/aL` producers, `bV` phase-tail consumer.
