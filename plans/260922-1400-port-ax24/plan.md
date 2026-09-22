---
title: "Slice 56 — ax24 ba() projectile FSM + bc()/bd() sweeps + k.e() kill counter"
phase: port
status: done
---

# Slice 56 — ax24 `ba()` projectile FSM

Ports the full flying-level projectile system verbatim from
`i.java:13742-14600` + `i.p(int,int)` (`:18031`) + `k.e(int,int)`
(`k.java:4314`, javap-verified `bytecode/k.javap.txt:17202`).

## What lands

- `tickAx24` — `ba()` complete: the L15 flight arm (S∈0..4|22..28,
  `P&128==0`: offscreen pool-retire via `v()`, `aG` trail/chain/player-hit
  arms) + the 45-state per-S table:
  - S6 drop line (`al>ap→i(9)`, `ap+=k.X`, `bc()`), S7/S12 timers,
    S8/S44 anim-end remove.
  - S9/10 impact: `T==1&&U==0→k.A(12)` sfx, `t()`, flying arm zeroes
    velocity + `af==aS→bc()` else `X∩player→op38`; non-flying `bd()` +
    `X∩player→op4`; `r()→af=null+k.c`.
  - S11 homing leg (`aC<=0` → snap to `af.cH[ap]` waypoint +
    `posFromWaypoint` + `i(9)`; else `ag/ah` steer with `+k.Y`).
  - S13/14 + S45 lobbed arcs (`j--`, apex `bZ<=ap-30`/`<=ap`, `k` flag).
  - S19 lay-child (`p(0,0)` → S40 pinned child, `b` latch).
  - S20 heal-shrine arm (`az=100`, `P&=-129`, revive statics
    `bB/bC/bD/bE/bF/bG`, player `i(21)`, `kAF=min(aB,100-kAE)`,
    `kX=kAJ` restore, `k.A(25)`).
  - S31/40 pinned children follow `af+ao/ap` (orphan → stop → `r()`→`k.c`).
  - S35/36 explode (`b=true`, `X∩player` OR cell `e()>=20` → `i(36)` +
    velocities zeroed; op4 on contact).
  - S16-18/41-43 offscreen-remove + contact ops.
- `projSweepBc` — `bc()` (`:14396`): per-ax hit arms — ax54 kill
  (`ad.i(2)` + `k.e`), ax30 damage `aB-=20`/`cG=6`/death chain, ax56 kill,
  ax67 destructibles by S∈{19,21,23,32,35,38,41,43}, ax24-S19→`i(20)`,
  ax32 `K[k.au]` damage with `i.p` anim pick.
- `projSweepBd` — `bd()` (`:14538`): non-flying variant — ax19-S2→`i(3)`,
  ax17/ax23 `H[k.au]` damage → `i(68/73)` or death `i(129/79)`.
- `projLay` — `p(int,int)` (`:18031`): `a(24,40,S=31|40,az+10)` child at
  `+r8/+r9`, `ao/ap` offsets, `P|=16`, `af=this`, `k.b`.
- `countKill` — `k.e(r5,uid)`: `ap[0]++` when `uid>0 && kAj!=7` (javap
  `iconst_0` proves literal index 0; r5 unused).
- World: `kAp` IntArray(6), `iBB/iBC/iBE/iBF/iBG` revive statics, `kAF`
  meter, `iCF` hit-confirm latch; entity `projB`/`projK`/`cHWaypoints`/`iP`.
- Dispatch: `ax==24 → tickAx24`.

## Not ported (flagged)

- `d(8,…)`/`d(9,ak,al)` floatie spawner calls (bc()/S22 trail) — the
  floatie subsystem is still unmined; call sites noted in comments.
- `i.e` static (S20 arm's `e = 30`) — script-var static, unmapped.

## Tests

`Slice56Test` — 20 tests: pool retire, aG 1/3/player-hit arms, S6 drop +
bc() kills (ax54/ax30), S7/S8/S12/44, S9 op4/op38, S11 homing snap,
S19 child latch, S20 shrine statics, S31 pin/orphan, S35/36 explode,
S45 lobbed transition, countKill gates.

## Gates

verifier `ok:true` · `python3 -m unittest` 57 green · `:core:test`
405 green · `:android:assembleDebug` green.

Stacked on `devin/1790136000-port-ax56` (PR #64).
