---
title: "Slice 163 — ax4 aj() remaining arms: S29 blast sweep, S30 re-arm, S33 fly-out"
phase: port
status: done
---

## What

Ports the last three unported `aj()` arms of the ax4 destructible-volume
FSM verbatim (i.java:5481-5593, proven):

- **S29 blast sweep**: `a(aS.W, X)` → `aS.a(4,0,0,this)` (player op4 hit);
  then the `k.bd[]` draw-list sweep over `ax∈{4,11,17,73,15,23,29} ∩ X`:
  ax4-S30 → `i(29)` chain re-arm, ax29≠S20 → `aB-=50; i(20)`, melee set
  {11,17,73,23} → `aB -= bu[au]<<1`, ax15-S6 → `i(7); Z[3]=1`. `T==6&&U==0`
  → `k.A(12)`; `r()` → `k.c(self)`.
- **S30 re-arm**: `a(aS.X, W)` → `i(29)`; `aS.S==295 && (W∩ || g.a∩)` →
  `i(29)` (grapple-linked entity reaching the volume also re-arms).
- **S33 fly-out**: `b=true`; `aA==1` off-camera exit (`W[0]>ac[2]` right /
  `W[2]<ac[0]` left by `ag` sign) → `aA=2; P&=-17; P|=32|128`; player
  within ±20px && (`af==null || af.S==36`) → spawn `a(24,40,35,200)`
  spark child (`ai=0,ag=0,ah=768,aj=1536,t()`), `af=aK`, `k.b` insert.

## Fidelity fixes folded in

- `k.au` proven to be the **difficulty index** (indexes `bu[]`/`bv[]`/
  `dh[]`/`di[]`, k.java:2945/3325) — Level0World's `kAu` was correct but
  the `weaponSlot` iface accessor claiming `k.au` was never overridden
  (always 0 = easiest difficulty everywhere). Now `weaponSlot get() =
  kAu` so all `bu[au]` consumers (j() damage, this sweep, score tables)
  read the real difficulty.
- `var kAu` added to the `LevelCellSource` iface; Level0World field now
  `override`.

## Gates

verifier `ok:true` · 57 unittests · 1082 `:core:test` (4 new: blast sweep
multi-type effects + sfx12 timing, op4 route, S30 re-arm, S33 spark
spawn) · `:android:assembleDebug` · `:gdx:build`

## Files

- `rewrite/core/.../NpcFsm.kt` — three new `when` arms in `tickDestructible`
- `rewrite/core/.../Entity.kt` — `var kAu` on the source iface
- `rewrite/core/.../Level0World.kt` — `override var kAu`, `weaponSlot`
  getter wired to `kAu`
- `rewrite/core/.../Slice1Test.kt` — `Slice163Test` (4 tests)
