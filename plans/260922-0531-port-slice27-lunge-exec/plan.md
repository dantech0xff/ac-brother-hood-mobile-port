---
title: "Port slice 27 — as() lunge-arc execution + g.a(int) airborne fling"
phase: port-slice-27
status: complete
---

# Slice 27 — the lunge execution

Ports `g.as()` (g.java:4242, proven) — the per-tick arc driver for the
grab/mount lunge armed by slice-26's `g.c(i)` — plus `g.a(int)` the
airborne fling it falls back to. Wires the three dispatch arms that
call it: `S∈{272..275,292}` → L1863, `S==298` → L1293.

## What was mined this slice (proven, g.java:4242-4376)

`as()` runs while the player is in a lunge state:

1. **F-bind (L7-L14)**: `F = i.at` when a mount is bound and the
   interact target `g` is null or not in front; else `F = g` when `g`
   is an ax11 with `Z[19]==1` and `!P()`. `F == null` → `a(0)`.
2. **Arc tick (L25)**: `cJ=X[0]; cK=X[1]` (launch snapshot),
   `cH += cC; cI += cD; cE--`. While `cE > 0` the tick returns — the
   player's `ak/al` are NOT touched mid-flight (the original draws the
   arc via `g.i()` → `a(cJ,cK,cH,cI,cy)` which emits clip-61 rope
   segments — render-only, deferred).
3. **Arc end**: `cH = F.ak; cI = (F.W[1]+F.W[3])>>1` snaps the arc
   point onto the target, then per target type:
   - `F.ax == 72`: `Z[0]==4` carts skip the land entirely and resolve
     `Z[4]` through `k.q` into `F.ac` + `r0.aq/ar` offsets (the
     cart-track link, L30). Other mounts land `ak=X[0]; al=X[1]` (L37).
     Then L42: `cL = 0`, and `Z[0] != 4` plays `i(277)` mount-on
     (Z[0]==4 stays in the lunge state so next tick `au()` can orbit).
   - `F.ax == 11`: the THROW — `cL=0; i(277)`;
     `F.ag = (cF>>8)·j.b(cy); F.ah = -(cF>>8)·j.b(j.n-cy)` launches the
     victim along the lunge angle at force `cF`; `F.i(181)` when
     `F.g(this)` (victim facing the player) else `F.i(180)`. `S==298`
     instead hands the victim `F.i(168)` — the carry/hostage seat.
   - `F.ax == 17`: silent land (L63). Anything else → `i(0)` (L64).

`g.a(int r5)` (g.java:126, proven): `a(43,32)` masked enter, `al+=10`,
`ah=r5`, `aj=1536`, clears the standing-on + resolved links — the
generic "fling airborne" used by `as()`'s `a(0)` abort.

## Dispatch wiring (proven, g.java:886-906 + call sites)

- `S∈{272,273,274,275,292}` → L1863 (g.java:3415-3427):
  `i.at==null → as(); at.Z[0]!=4 → as(); cE>0 → as(); else au()`.
  The `au()` tail (cart orbit once the arc expires) is **not ported** —
  slice 28; the arm runs `as()` for every case except that one.
- `S==298` → L1293 (g.java:2995-3009): `r() → P|=64`, then
  `i.at!=null && i.at.S!=168 → as()` — the carry state keeps lunging
  until the seat entity plays anim 168.
- `S∈{277,293}` → L1872 → `au()` — the mounted-orbit FSM, slice 28.

## Ported

- `Entity.cL` — mount-on counter field (as() clears; au() uses).
- `Entity.flingAirborne(r5, w)` — `g.a(int)` verbatim.
- `Entity.lungeTick(w)` — `g.as()` verbatim: F-bind priority, arc
  advance, arc-end land/mount/throw/cart-link.
- `PlayerFsm.tick` — L1863 case for `272..275/292`, L1293 case for
  `298` (with `P|=64` on `r()`).

## Deferred (flagged in code)

- `au()` (g.java:4384+) — mounted-orbit FSM (swing/orbit/dismount per
  `F.Z[0]`), plus `at()` orbit-position and `av()` wall-probe. Slice 28.
- `g.i()` → `a(5)` clip-61 rope-segment drawing — render arm.
- `S303` L1876 arm (`u(62430) → i(0)` — cart dismount input).

## Tests (6 new, 102 total green)

- ax72 mount lunge → `S=277`, `cL=0`, lands at `X[0]` attackbox point.
- Mid-arc tick: `cH/cI` advance by `cC/cD`, `cE--`, anim kept.
- `F == null` → `a(0)` fling: `S=43`, `ah=0`, `aj=1536`.
- ax11 throw: `S=277`, victim `ag/ah` along `cy` at `cF>>8`, `i(181)`
  when facing; S298 → victim `i(168)`, player keeps 298.
- ax72 `Z[0]==4`: no `i(277)`, `ac/aq/ar` cart-track link resolved.

## Gates

`:core:test` 102/0 fail · verifier `ok:true` · `python3 -m unittest`
57 pass · emulator `AcLevel0` 637 records, `npcs=454`, clean boot.
