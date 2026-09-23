---
title: "Slice 66 — ax74 bN() wisp/pickup FSM"
phase: port
status: done
---

# Slice 66 — ax74 `bN()` wisp/pickup FSM

Ports the collectible-wisp entity that the `m(-1)` burst (slice 21) and
anim-0 level records spawn. Completes the destructible → burst → wisp →
collect chain: `i.aS` proximity → `ap[4|5]` progress counter + `k.s()`
streak meter → spiral-in → attach anim → despawn.

## Original (i.java:21280 `bN()`, proven)

| S | Behavior |
|---|----------|
| 0 | collect scan: `overlapStrict(p.W, e.Y)` OR octagonal dist ≤ 20 → `k.o(4)` (bh==3 flying) else `k.o(5)` + `k.s()`; `k.A(15)` sfx; `i(2)`; `az=p.az+1`; owner `af` released (+extra sfx when `af.aG!=0`) |
| 1 | polar spiral-in: `P|=16`, `az=p.az+1`; `j += 15/tick` until `j≥aE` then `aC--`; `aF = aD·256/360`; `ak=aq+sin(aF)·j>>8`, `al=ar+sin(64−aF)·j>>8`; when `j≥aE && aC≤0` → `i(2)` + af-release + `aC=0, aE=j, j=0` |
| 2 | attach anim: `P&=-17`, pinned `ak=p.ak, al=p.al−30`; `r()` → `k.c(this)` |
| 3, 6 | quadratic-bezier view-space flight: `t = Z[6]·256/Z[7]`, `j.a(7-arg)` blend `a·u + 2b·v + c·w >>16` with `u=t(256−t), v=(256−t)², w=t²`; `ak=x+k.O, al=y+k.P`; `Z[6]≥Z[7]` → `i(4)` (+`T=1` when `Q==6`) |
| 4 | end: `r()` → `k.c(this)` |
| 5 | fall→bezier setup: `r()` → vel0 + dead `j.a(0,40)` RNG draw (verbatim — consumes RNG); `Z[0,1]` = pos−cam, `Z[4] = cx ± ≤80`, `Z[5] = cy` → `i(3)` |

Init (L116, i.java:3029): `P|=512`, `az=r8[7]`, `k.aq++` when `r8[5]==0`,
then shared `i(r8[5]) + t()` tail. `bi[74]=54`.

## New world hooks

- `kCount(slot)` — `k.o(int)` (k.java:4304): `ap[slot]++`; slot 3 gated on
  `k.aj==7`.
- `kCollectStreak()` — `k.s()` (k.java:5338 + `dE` :8403): `az++`; meter
  floor `30+tier·15`; `g.f` caps `x[1]`, `g.e` raises only when the floor
  grew past the previous `kAx`.
- `kAz` — `k.az` streak counter. `kAq` — `k.aq` anim-0 wisp counter.
- `jBezier(...)` — `j.a(7-arg)` (j.java:515) verbatim weight order; **at
  t=0 the blend yields `2·control`, not start** (non-Bernstein row —
  kept, labeled in code).

## Port

`NpcFsm.kt` tail: `jBezier`, `initAx74`, `tickAx74`. `Level0World.kt`:
`kAp` already existed; added `kAz`, `kAq`, `kCount`, `kCollectStreak`,
`ENTITY_CLIP[74]=54`, init/tick dispatch. `Entity.kt`: interface hooks
(`kCount`, `kAz`, `kAq`, `kCollectStreak`) + `kAj` upgraded to `var`.

## Tests — Slice66Test (17 cases, all green)

init flags/`kAq` gating; S0 collect (≤20, far-wait, af-release double sfx);
S1 spiral (radius growth, orbit continuation, polar coordinates);
S2 pin→despawn; S5 endpoint setup; S3/S6 bezier flight + `Q==6→T=1`;
S4 despawn; `kCount` aj7 gate; `kCollectStreak` tier lift + no-drop case.

## Gates

- verifier: `ok:true`
- `python3 -m unittest discover -s tests`: 57 pass
- `./gradlew :core:test`: 533 pass (was 516 + 17 new)
- `./gradlew :android:assembleDebug`: OK
