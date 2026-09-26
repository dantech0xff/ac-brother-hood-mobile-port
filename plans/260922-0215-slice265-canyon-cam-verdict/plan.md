---
title: "slice 265 — bh3 camera fidelity: kQ writeback + C() respawn arm + cp1-unreachable verdict"
phase: done
status: merged
---

# Slice 265 — flying-canyon camera fidelity

Investigated why the ax2 checkpoint at (418,8360) never fired in the
`bot climbs the mission-1 shaft until the alert stall` leg. Found one
real divergence (a skipped writeback), one wrong-sub-arm respawn snap,
and — after fully decoding `C()`/`D()`/`a(boolean)` — proof that the
checkpoint **cannot** fire on level-1 in the original either.

## Changes

1. **`k.Q` unconditional writeback** — `k.D()` (k.java:6890-6960, proven)
   does `k.Q = ae.al - cB` on every bh3 tick BEFORE the band clamps. The
   port had skipped the writeback entirely, freezing `kQ` at its `C()`
   init 230 — the climb gate `pad.u(16388) && kQ>117` and dive gate
   `kQ<230` then never saw the real band offset, letting the player
   free-climb ~3500px above the camera. Ported verbatim
   (`Level0World.kt:2235-2252`); `ah` now tracks `kY` correctly.

2. **`reload()` tail `kM(kAd)` → `camResetC()`** — `a(boolean)`'s tail is
   `g.e(ax); C(); T(); l(8)` on every respawn (k.java:6619 L55). `C()`'s
   bh3 arm (k.java:1851-1875, proven) snaps `cA=O=aS.ak-200`,
   `cB=P=aS.al-230`, re-arms `X=V=-7`, `Q=230`, resets the conveyor
   (`dU/dR/aR=-1/dS=-2/dT`). The port's `kM(kAd)` ran the `!kZ` non-bh3
   tracker (`camB=p.al-150`) — wrong proc, wrong offset. `camResetC()`
   already exists verbatim, so `reload()` now calls it unconditionally
   (`Level0World.kt:4041-4047`).

3. **Test verdict corrected** — `Slice1Test.kt:23357` — the old comment
   modelled "camera grinds up the shaft, cp1 fires mid-leg-2"; proven
   wrong on every point: `X=-7` restores each respawn (not a persisting
   halving), `camY=p.al-230`≈11733 re-snaps each leg (not persistence),
   and the leg is ~600t (`k.aE=100` drains 1/6t while `k.aH=-1` — the
   `k.aH=80` pause arm lives only in ax21 `bD()`, absent on level-1).
   `aY()` ticks only at `au<2` → camY ∈ (8000,8480); the camera bottoms
   out ~9600. The conveyor that could jump the camera is ax21-gated
   (`i.bD()` arms `k.aR = ((bu/20)-1)-(aS.al/400)` only when `aS.al<260`)
   and `i.bW` phase-checkpoints are ax21-only — both dead here.
   Assertion now `deaths >= 2 && minAl < 8360 && !snapFired`.

## Provenance notes

- Leg cap ~600t: `g.n()` head (simple/g.java:5845-5872) —
  `k.aH>=0 → skip; aG--; aG>0 → skip; aG=6; aE--`; stall arm
  `aE<=0 && aH<0 → i(24)` → `x[1]=0` → S2 → `L325: r() → k.l(12)`.
- Camera model: `D()` = `cB+=kX; Q=al-cB; band clamps (i.bj off);
  O+=l(cA-O,4); P+=l(cB-P,30)`; `L172 tail` pins `cA=O; cB=P` each tick
  → effective `camY += kX/2` per tick (-3.5 at X=-7, -2 after the single
  `i.aJ`-gated halving at `aE<=25` — `aJ!=0 && k.W==0` blocks a second).
- `k.aE<=25` halving fires once per leg; `C()` re-arms `X=-7` each
  respawn so every leg has full wind.
- `reload()` marks (post-fix): `cam=11340` at t100 after snap ≈11733;
  smooth descent ~-3.5/t; `cp.au` never below ~15.

## Verification

- `bot climbs the mission-1 shaft until the alert stall` — PASS
  (deaths=19, minAl=8184, snap=false, respawnAl=11963).
- Full `:core:test` suite — green (173 suites).
- `verify-static-reconstruction.py` → `ok:true`; 57 unittests OK;
  `:android:assembleDebug` + `:gdx:build` green.
