---
title: "Port slice 26 — mount-entry arm (g.java:3474-3562) + grab lunge g.c(i)"
phase: port
status: done
---

# Slice 26 — the mount-entry arm + `g.c(i)` grab lunge

The `J&4` mount-request consumer inside `g.e()` (g.java:3474-3562, the
L1947 block) — completes the mount loop end-to-end: ax16 request marker
(slice 25) → `J|=4` → `az()` binds `i.at` (slice 24) → **this arm**
lunges onto the mount via `c(i.at)` and sets `cm=1`.

## What was mined + ported (all `proven` unless noted)

**`mountEntry` (PlayerFsm.kt)** — `g.java:3474-3562`, verbatim:

- Gate: `(J & 4) == 0 → skip`; `S == 50 → skip`.
- **Mount arm** (L1960-L2003): `i.at` bound and no interact target in
  front (`i.g(i)` = `r4.ak < ak == av`, i.java:7758) → `aZ || k(S)`
  (mountable-state whitelist) → `i.at.v()` on-screen check → `r104`
  in-range: `r104` starts **true**; gates only clear it —
  `ax72 Z[0]==1 && h(ak-at.ak, W[1]-at.al) >= Z[3]` → false;
  `ax72 Z[0]==4 && (Z[4]<0 || !aZ)` → false; `ax72 Z[0]==3` → false.
  The `|ak-at.ak| >= (W[2]-W[0])<<1` band check at L1980 is **dead
  code** — both exits leave r104 true for Z[0]==4 (corrected from the
  earlier summary reading).
  - Press path (`v(65568)`): `ag=ah=aj=0` → `Z[0]!=4` →
    `k.k()?G():U()` → `c(i.at)` → `z=false` → `k.A(30)`.
- **Assassinate arm** (L2006-L2027): bound ax11 in front with
  `Z[19]==1 && !P()` → `aZ || k(S)` → `r98=true` → same press →
  `k.k()?G():U()` → `c(g)` → `z=false` (no `A(30)` here — `c()` plays
  it via the anim-pick arms).
- **L2029-L2040 tail**: `r98` → `!k.k()` → `!T()→G()` then
  `c(200+k.O,120+k.P); d(200+k.O,120+k.P)` (spawn+move the clip-74
  hand at view center) → `cm=1` (`cm=true` decompiled; k.cm is int).
- **Convergent dead code** (proven by goto-chain trace): L1994's and
  L2020's `k.k()`+`V()` checks only guard the press path — `V()==true`
  loops back through L1974/L2016 to the same `r98` assignment;
  `V()==false`/`mounted` land at L2029 directly with `r98` already
  set. Ported as a comment, not branches.
- **L2051-L2057 tail**: `aA==0 → aA=1`; `aA&4 → aA&=-5`. L2048
  `o()→ao()` (g.java:6369 `o()` = `aZ || a.ax∈{51,15}`; `ao()` =
  g.java:3792 debug/skip prompt) — flagged, unported.

**`g.c(i r8)` grab lunge (Entity.grabLunge)** — `g.java:4115-4240`,
proven:

- `t()`; `i.bq=0`; zero `ag/ah/aj/ai`.
- Victim anchor: ax11/ax17 → target `W`-center-x, `W[3]-45`; else
  `ak/al`.
- Lunge anim pick: `b(S)` (attack table) → `i(292)+A(30)`;
  `S==298` → `A(30)` only (keeps anim); `r03<=0||r04<=0` →
  `r03==0 ? i(275) : i(272)`; else ratio `r07 = r04/r03` (8.8
  rise:run): ≤64→272, ≤256→273, ≤1024→274, else 275 (+A(30)).
- L40: `cF=5120`, `cx=(8·j.m)/360`, `cG=av`; ax72 `Z[0]==1 → cM=0`,
  else `Z[1]>0 → cF=Z[1]`, `Z[2]>0 → cx=(Z[2]·j.m)/360`; ax11/17
  (L60-L66): `S==298 → cF=7680, cx=0` else `cF=5120, cx=0`.
- L67: `cE = Σ aa.a(S,f)` over `aa.b(S)` frames (the anim's total
  duration = lunge length in ticks); `t()`;
  `cz = r8.ak - X[0]` (clamped to 1 when 0), `cA = r05 - X[1]`
  (`r05` = target W-center-y), `cB = h(cz,cA)`,
  `cy = j.b(-cz, cA)`, `cC = cz/cE`,
  `r06 = (|cA|<<8)/|cz|` → `cz==1 → cD = cA/cE` else
  `cD = (|cC|·r06)>>8`; `r05 < X[1] → cD = -cD`;
  `cJ/cK/cH/cI = X[0]/X[1]` snapshots; **`F = r8`** — the grab target
  for the mount/throw consumer arms (g.java:4303+, next slice).

**`i.T/U/V/c/d` clip-74 hand indicator** (i.java:9825-9907, proven):

- `T()` = ae is clip74 at S∈{0,1} — clip74 unconverted → compares via
  `world.clipFor(74)` identity (false until converted — flagged).
- `U()` = `T() → G()` (releaseAe) + `L=M=-1` (clears `i.L/i.M`
  statics, ported on the Entity companion).
- `V()` = `ae != null && k.a(k.H,k.I, ae.ak-k.O, ae.al-k.P, 70)` —
  k.H/k.I = the last touch point in view px; ported as
  `world.touchNearView(e, 70)` (k.java:627 `k.a` = octagonal `h` ≤ r).
- `c(x,y)` = spawn `new i()` ax14/clip74/`i(0)`/`az=302`/`au=0` at
  (x,y) + `o(x,y)` (`i.L=x; i.M=y` statics).
- `d(x,y)` = move ae; `T()` hands re-pin L/M; `i(1)` while within 70px
  of `(k.J,k.K)` — the held-point (reuses the single touch point —
  `inferred`: J2ME tracks down- vs current-touch separately).

**`Trig.atan2`** — `j.b(r7,r8)` (j.java:385, proven via the quadrant
table at 430-460): the original's atan2 measured **clockwise-from-+y**
(screen-down) in angle-256 units — `r8 = h·sin(θ)`, `r7 = h·cos(θ)` —
= standard `atan2(y=r8, x=r7)`. So `j.b(a,b)` ports as
`Trig.atan2(b, a)`. `cy = j.b(-cz, cA)` → `atan2(cA, -cz)`.

**`k.k()`** — `cm == 1` mounted check (k.java:638): `var cm` on
Level0World + `mounted`/`setMounted` on the interface. `k.H/k.I` =
`lastTouchX/Y` set on DOWN events.

## Deferred (flagged in code)

- `cy`'s consumer — the mount throw `F.ag/ah` (g.java:4303+) — next
  slice with the S272-275 arc-tick arms.
- `o()→ao()` debug arm; `k.J/k.K` held-point vs touch-point split;
  clip74 conversion (hand indicator spawns, renders nothing).
- `i.b(r6,r7)` static (L/M 70px query) — ported fields only, no
  caller yet.

## Tests (Slice1Test.kt — 7 new)

- press lunges onto in-range ax72 (`F===m`, `cm==1`, sfx 30, lunge anim).
- `Z[0]==3` gate → no lunge, `cm==0`.
- armed arm spawns clip74 hand at `(200+kO, 120+kP)` with `cm==1`.
- assassinate arm: in-front ax11 `Z[19]==1` → `c(g)` lunge.
- `J` without bit2 → whole arm inert.
- `grabLunge` S==298 → keeps anim, `cF=7680`, `cx=0`.
- (`pad.v` driven via `Pad.queuePress(M_CONTEXT)+commit`.)

## Gates

- `:core:test` — 96 green.
- `verify-static-reconstruction.py` — `ok:true`.
- `python3 -m unittest discover -s tests` — 57 green.
- Emulator: `AcLevel0` boots `level0: 637 records, npcs=454`, no
  crash.
