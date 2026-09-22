---
title: "Port slice 28 — au() mounted-orbit FSM + at()/av() + riding arms"
phase: port-slice-28
status: complete
---

# Slice 28 — the mounted orbit

Ports `g.au()` (g.java:4389, proven) — the mounted/orbit FSM that runs
while the player sits in the riding states — plus its three helpers
`g.at()` (orbit position), `g.av()` (wall-embed unstick probe) and
`g.aB()` (interact-gauge stepper). Closes the mount loop begun by
slices 26-27: marker → `J|=4` → `az()` binds `i.at` → `c()` lunge →
`as()` arc → `i(277)` → `au()` riding.

## `au()` structure (proven, g.java:4389-4642)

Same F-bind as `as()` (`i.at` wins unless an in-front ax11 `Z[19]==1`
victim is bound). `cJ/cK` = drag anchor: player pos, or the `X` attack
box point for `Z[0]==4` carts. `cH/cI` re-pins to `(F.ak, F.W-mid)`.

Per `F.Z[0]` on ax72 mounts:

- **Z0==0 swing (L35-L51)**: `cB -= cF>>8` radius decay; `cy` pulled
  into the `(j.n, j.o)` band by `±cx`. `at()` repositions each tick.
  When the player's `W` overlaps the mount's → `i(22)` dismount launch
  `ag = -(cF>>8)·j.b(cy)`, `ah = (cF>>8)·j.b(n-cy)`, collide-step,
  wall-check zeroes `ag`, `F = null`; `av()` tail.
- **Z0==3 slide (L53-L66)**: same swing; overlap just unlinks
  `F = i.at = null` — no launch.
- **Z0==1/2 wheel (L69-L151)**: `cM==0` spin-in — `cB` decays floored
  at 75 until `|cy - o| <= cx` → `cM=1`, `cL = 16·m/360` (Z0==1) or
  `35·m/360` (Z0==2, which also pins `cB = 75`). `cM==1` oscillates
  `cy` by `±10` (Z0==2) or `±5` inside `(o±cL)` — a clamped edge
  toggles `cG`, and for **Z0==2 flings the player** off with
  `ag = ±3328` (by facing), `ah = -6656`, `i(243)`. Z0==1 decays
  `cL` to 0 → `i(293)`, `cM=2`, `cy = o`. `S == 293` on Z0==1 rides
  the zipline: `cB+=6, al+=6`, `cB >= Z[3]` → `al=W[3]; a(0)` (L153).
- **Z0==4 cart (L162-L186)**: `d(200+O, 120+P)` moves the hand to view
  center while `!k.k()`; context press/hold — or, unmounted, the
  indicator sitting on the touch — brakes `cB -= (cF>>8)>>2`; the
  cart rides `F.ak = cJ - cB·j.b(cy)`, `F.al = cK + cB·j.b(n-cy)` and
  drags the `F.ac` track entity `aq/ar` behind. Track `S∈{2,3}` or
  `cB < 20` detaches: `ac=null; F.P|=160; Z[4]=-1; F=null; i(0);
  a(false)` + `G()`/`U()` indicator release + `av()`.
- **non-ax72 F (L190-L200)** — a dragged ax11/17 victim: `S==293`
  sinks the player `cB+=6, al+=6` until `cB >= 15` → `al=W[3]; a(0)`;
  otherwise applies `ag/ah = ∓(cF>>8)·j.b` drag velocity per tick.

`cy < n` fall-throughs at L43/L61/L93 are decompiler merges of the
`cy -= cx` extreme-clamp — ported as `cy -= cx` (inferred). `av()`'s
`goto L27` on `bq != 0` is likewise a decompiler merge into a shared
tail — ported as skip (inferred).

## Helpers ported

- `g.at()` (g.java:4376) → `Entity.orbitPosition()` —
  `ak = cH + cB·j.b(cy)>>8`, `al = cI - cB·j.b(j.n-cy)>>8`.
- `g.av()` (g.java:4733) → `Entity.wallProbe()` — probes cells 20px
  up; solid overhead (`aO >= 20`) → `a(0)` fling; falling with
  `bq == 0`, a `>= 20` cell at the feet-side edge zeroes `ai/ag` and
  steps `ak` back 10px.
- `g.aB()` (g.java:5800) → `Entity.interactGauge()` — steps `K`
  through clip-10 anim 41 frames (29 while `S == 295`), `cN`
  sub-ticks per frame, pins `L/M` on `g`'s W box. New fields `K`,`cN`.
- `i.G()` = `releaseAe()`, `i.U()` = `dropIndicator()` — already
  ported; the cart arm picks by `k.k()`.

## Dispatch arms wired (proven, g.java:886-925)

- `S∈{277,293}` → L1872 → `au()`.
- `S∈{294,310}` → L1874/L1888 — anim only (explicit no-ops).
- `S∈{299..302}` → L1885 — windup → `i(303)` on `r()`.
- `S==303` → L1876 — `u(62430)` → `i(0)` dismount; else on `r()`:
  `P|=64` + `aB()` gauge; `v(65568)` → `ar()` — **unported** (the
  standing interact action: levers, destructible S30→29, knife throw
  `i.a(8,5,14,…)`, `aB -= bu[au]·K/6` damage — separate feature).
- `S∈{311,312}` → L1889 — `a(true)` collide-resolve then on `r()` the
  same `as()`/`au()` split as L1863 (now fully wired, replacing the
  slice-27 stub — the L1863 cart tail also calls `au()`).

## Tests (9 new, 111 total green)

- Z0==0 swing: `W`-overlap → `S=22` dismount, launch `ag/ah` along
  `cy`, `F=null`.
- Z0==1 wheel: spin-in → `cM=1` → oscillation → `cL` decays →
  `S=293`, `cM=2`, `cy=o`.
- Z0==2 wheel: clamped edge → `S=243` fling `ag=±3328, ah=-6656`.
- Z0==4 cart: rides `cJ/cK` arc, track drags `aq/ar`; `ac.S==2` →
  detach `P|=160, Z[4]=-1, S=0`.
- ax11 victim drag: `ag/ah` per tick; S293 zipline → `a(0)` at `Z[3]`.
- S299-302 windup → S303; S303 `u(62430)` exits the arm.

## Gates

`:core:test` 111/0 fail · verifier `ok:true` · `python3 -m unittest`
57 pass · emulator `AcLevel0` 637 records `npcs=454` clean.

## Deferred

- `ar()` (g.java:4030) — the 65568 standing interact action.
- `g.i()` → `a(5)` clip-61 rope-segment draw — render arm.
- Mount state 294/310 bodies (anim-only today per source).
