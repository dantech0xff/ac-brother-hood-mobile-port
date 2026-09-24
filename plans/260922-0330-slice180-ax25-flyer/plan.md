---
title: "slice 180 — ax25 flying player (g.n() full port)"
phase: port
status: done
---

# Slice 180 — `g.n()` flying-player FSM

## What

`I()` entity dispatch case 25 → `k.aS.n()` (i.java:5215): on bh3 packs the
player record IS ax25 and `g.n()` (g.java:5603-6113) IS the entire player
tick — it replaces the grounded `e()`/`dispatch`+`postTail` path.

- `PlayerFsm.tick`: `if (world.bh3) flightTick(p, pad)` — verbatim port of
  `n()` minus the grounded postTail (proven: no case-0 in `I()` for bh3).
- ax25 player-slot init (i.java:2415-2427, proven): `az=202`, `x1=90`,
  `aA=2`, `aB=3`, `ah=-2560`, `aq=ar=-1`, `ad` = retype-26 companion
  (the glider visual `n()` mirrors every tick). Keys off record type —
  `kAj` isn't assigned yet when `init{}` runs (proven bug in first rev:
  `bh3` reads stale 0 inside `init{}`).
- The 0/25 player-slot record now skips the bb[] npc loop (inferred:
  `k.aS` is built from it, not spawned twice).
- New `LevelCellSource`/`Level0World` fields: `iBk`, `iAK`, `iBB`,
  `kAI/kAG/kAE/kBB/kBC/kBD`, `kQ`, `stateL`, `kEE` — the burst/stall
  machinery and the flying scroll counter.
- `flap` (`g.e(z)` g.java:6044-6079): `spawnChildFx(ax24,clip40,6|7,
  az-1)` puff — burst aims `ao/ap` at camX+rand, flap `ah=-3840+kX`,
  `t(); P|=16; af=this; bR=false; k.b(aK)` (world.queueInsert).
- `bankSteer`: the shared `u(4112)/u(8256)` ±768 arm used by S21/S22.
- `flightAliveV` = `i.v()` ax25 arm (`a(k.ac, this.Y)` camera∩Y overlap).

## Verbatim semantics locked by tests (Slice180Test, 7 tests)

- Glide S∈{0,4,5,17,18}: `!iBi||gE` → forced `ah=kY` descent; `iBi&&!gE`
  → steering/flap/homing arm cluster (seat latch gated — the ax65 perch
  arms it via `i.bi=true; aS.az=199`, i.java:15216/:9571).
- Steering: `u(4112)`→`ag-=768` clamp −2048, `u(8256)`→`ag+=768` clamp
  +2048 — bank anims `i(30..33)` by `kBD>=15` (light 33/32 else 30/31),
  both arms end `av=false` (quirk kept). One press = one bank impulse;
  S30-33 has NO exit arm (verbatim `av=false` + dead ifs only,
  g.java:6013-6020) — `ag` decays via the z2 tail.
- Climb `u(16388)&&kQ>117` → `ah-=768` clamp `-2048+kY` → `i(4)`;
  dive `u(33024)&&kQ<230` → `ah+=768` clamp `2048+kY` → `i(5)`
  (kQ==230 → gated off → z3 tail only).
- Stall: `kAE<=0 && kAH<0` → `i(24)` + `x1=0` + `iBB/BC/BD/BF/BE/BG` arm;
  S2/S24 failsafe `r()||!v()` → `k.l(12)` (mission fail).
- `ad` mirror: `av/P/S/T/al/ag/ah/ai/aj` copied, `ak+=20`.
- Waypoint homing (`aq/ar != -1`): `ak/al` ±10 toward target with
  `ar+=k.X; al+=k.X` scroll coupling + `aT/aU>=10` wall clamp +
  arrival → -1 (exact: al goes al0-17 via kX+chase+snap).
- `iBB` consumes the waypoint target each tick (aq=ar=-1).
- flap arm: `!iBk && Q!=18 && kAI>=10 && z4` → `kAI=0` + `e(false)` puff
  queued via `k.b`; wisp gets `ah=-3840+kX` and `P|=16`.

## Stale-test fixes (newly-wired arms collide with old fixtures)

- Slice178 `dU world shift`: flightTick's integrate drift now part of the
  tick — pin `iBi=true` + `ah=kY` → drift exactly −7/tick; assert +800−7.
- Slice97 `alert aH poison`: `n()`'s `aE<0→aE=0` (g.java:5621) now eats
  the poisoned `aE=-1` the next tick (transient by design) — and the
  `aG` divider adds `aE--` to the `aF` trickle (52/55/56 net).

## JADX case-group ambiguity (labeled inferred)

`case 0` contains unreachable-looking `S==18`/`S==17`/`z4` checks —
JADX header warns "Can't fix incorrect switch cases order". Ported the
glide arm for S ∈ {0,4,5,17,18} (inferred group cover) and kept
S3/S20/21-24/26-33 as their own cases verbatim.

## Gates

- `python3 scripts/verify-static-reconstruction.py …` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `./gradlew :core:test` → all green (incl. 7 new Slice180Test cases)
- `:android:assembleDebug`, `:gdx:build` → green
