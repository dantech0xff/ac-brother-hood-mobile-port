---
title: "Slice 39 — ax22 aN() capture zone"
phase: port
status: done
---

# Slice 39 — ax22 `aN()` capture zone

Port of `i.aN()` (i.java:10167, **proven**) into `NpcFsm.tickZoneInteract`,
dispatched at `n.ax == 22` (10 records in level 0).

## Semantics

- Head: `!overlap && S != 0 → i(0)` (zone resets when the player leaves).
- `S0` (`L10`): `g.b(aS.S)` anim gate ({18,19,20,22,23,24,25,35,36,43,
  150,157,165,233,242,243,263–266} — new `Entity.gB()`), then overlap →
  snap `aS.ak/al = this.ak/al`, vel0, `Z[3]` facing (-1 keep, 0→`av=true`,
  else `av=false`), `aS.i(65)`, `i(1)`.
- `S1` (`L22`): `ah=ag=0` pin each tick.
  - `L27` edge vault: `k.v(16390|16396 by Z[2])` → `ak±20`, `al-20`,
    `ag=∓3328`, `ah=-3840`, `av=Z[2]==0`, `aS.i(19)`, `i(0)`, `k.v()`
    (full latch reset — new `clearLatches` interface member).
  - `L42/L44` down-exit (`Z[1]` gated): `k.v(33024)` → `aS.a(0)`
    (`flingAirborne(0)`), `t()`, `al = W[3] + (al - W[1]) + 2`
    floor-snap, `i(0)`.
- `L49` is a decompiler-dropped label → plain return (flagged).

## Verification

- `:core:test` green incl. 9 new ax22 tests (capture/snap, gB gate,
  head reset, vel pin, L/R vault exits + latch clear, down-exit snap,
  `Z[1]` gate, `Z[3]` facing table).
- `verify-static-reconstruction.py` → `ok:true`; 57 unittests OK.
- Emulator boot clean (`npcs=454`).

## Files

- `rewrite/core/src/main/kotlin/com/acrebuild/core/NpcFsm.kt` —
  `tickZoneInteract`.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Entity.kt` —
  `gB()` + `clearLatches` interface member.
- `rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt` —
  `clearLatches` override + `ax==22` dispatch.
- `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt` —
  `zoneAt` fixture + 9 tests.
