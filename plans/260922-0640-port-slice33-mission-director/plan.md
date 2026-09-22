---
title: "Port slice 33 — ax21 bD() mission director + c-waypoint pool + bG() pursuer attack"
phase: gameplay-port
status: done
---

# Slice 33 — ax21 mission director (`i.bD`)

Second-largest remaining entity FSM (the `~580`-line `bD()` at
`i.java:18054-18629`) plus its support surface: the `c`-class 400-node
waypoint pool, the `d(boolean)` waypoint-chase mover, the `bG()` pursuer
attack script, `bH()` attack-anim picker, `u()`/`v()` offscreen+in-play
predicates, `b(true)` attach sync, `p(x,y)`/`a(4-arg)` child factories,
`g(int)` barrage spawner, and the `i.f(int)` one-shot phase gate.

Level-0 carries no ax21 record (the director exists only in level-index 5 —
Rome escape), so the port is exercised by synthetic entities + waypoints.

## Ported verbatim

- `bD()` phase switch `aA` 0–8: arm/attach (0), single monitor (1),
  pass-through (2), dual-respawn+waypoint (3), dual monitor (4), single
  monitor (5), kill-bitmap router (6), finale (7), waypoint travel (8) —
  then the `L237` every-tick tail: 5-entity anim watcher (`i(13/16/17/20/
  21/23/26/33/36/37)` + `l` kill bits 2/4/8/16/32), 5th-pursuer
  respawn/attack block, attach position sync (`ak=ak+aq`), and the
  `i.q`/`i.cF` charge gauge (`aB` mirror vs sum).
- `d(boolean)` waypoint chase: `|Δ|` vs node tolerance `|cB.f|`, arrived →
  `aC--` + snap + `bE()`; `flag=false` → node-event arm (`j--` → `cB.c`
  remap `(c==0&&l&2)→(cC==5?3:2)` / `(r7==3&&cC!=5)→(cC==2?0:2)` /
  `(r7!=1&&cC==5)→3`, switch 0..3 → `aA` 1/3/4/5 + `p()` kill-popups);
  `j<=0` → advance `cB=c.a(cB.g)`; else diagonal-limited `±(f<<8)` major +
  `(minor<<8)/major·f` minor; `L96` tail `ah+=k.Y; bE()` on ALL paths.
- `c` pool → `WaypointPool`/`WaypointNode` (400 slots, `j=10000` derived
  ids, `f&127` consumed-bit).
- `bG()` pursuer attack script: `l|=1`; `pv`-dispatch — p0 windup
  `i(28)→i(14)+g(0,1,2)`, p2 `S18`+arc-aim homing `a(24,40,13)` (`aG`
  quarter-flip), p3 `!k` gauge-engage (`i(22),i.q=true,i.bU=aB,l&=-2`,
  early return) / `k&&j==2` S27→S24 arm / `k&&j!=2` `aZ`-toggle +
  grid-scatter (60-pt pool, 7 picks via `i.a(x,y,W)` point-in-rect against
  `k.B.W`, `cH[7][2]` targets, `n=30`) → j2 seven homing `a(24,40,11)`;
  p4 `k.B.l&{2,12}→l&=-2`; `S∈[30,32]@T==0` instant-barrage; `S∈[33,35]→
  i(S-3)`; L153 tail `r()→cJ` + dead/alive anim pairs.
- `bH()` — `r02>r0-5||r02>=r0+5→33; ak>aS.ak→34; ak<aS.ak→35; ==→33`.
- `i.v()`/`i.u()` — corrected earlier misreads: `ax29` is `T>=54→true`
  (not `<`), score arm is `au>i→false` (not `<=`), ax13 needs `aG==4`,
  ax14 runs AFTER the score gate, generic = `a(k.ac,Y)`.
- Statics → world fields: `i.bV/bU/bW/bX/bT/bj/q/cC/cD/cE` + `k.ai/R/aE/
  aH/aR/bu/bk[]` + `k.B` + `cB` → `w.dirWp`. `k.l(15)` → `missionWon`
  flag (`inferred` — the screen-state flow is level-flow code).

## Inferred / flagged

- `L225` (`aA=6` monitor) — `S!=22` fall-through lands physically on the
  L197 router chain; ported as reaching `L237` (a literal port would
  re-run the router forever — decompiler drop).
- `r04.l &= -2`-style arms on possibly-null refs (aA=1/4) — ported as
  null-safe (decompiler artifact).
- `r05.aw()` inside the aA=3 respawn — `i.aw()` unmined, skipped.
- `k.bk[]` record-type table — `u()`'s ax67 arms read it; unmined,
  `kBk(i)` returns 0.
- `k.l(15)` — ported as `missionWon` flag; the milestone-screen flow is
  `inferred` (level flow code, not entity FSM).

## Verified

- `:core:test` — 10 new director tests (aA=0 waypoint/kill routing,
  chase velocity + minor snap + `k.Y` drift, last-node → aA=6, l=62 →
  finale, player-freeze + floatie vs `k.l(15)`, L237 watcher S16/l-bit,
  attach sync, cF gauge mirror) — all green, suite unchanged elsewhere.
- verifier `ok:true`; `python3 -m unittest` 57/57.
- Emulator: `:android:installDebug` boots level0 `npcs=454` clean
  (director dormant on level 0 — no ax21 records).
