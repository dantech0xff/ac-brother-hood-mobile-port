---
phase: 2
title: Core Simulation Slice
status: completed
priority: P1
dependencies: [1]
---

# Phase 2: Core Simulation Slice

## Context Links

- [Plan](./plan.md)
- Timing contract:
  `docs/modern-mobile-technical-design.md` section 8 (62 ms fixed tick,
  accumulator, max 4 catch-up ticks, drop backlog)
- Numeric contract: integer 8.8 fixed-point position/velocity; RNG parity
  helper replicating `j.a(min,max)` semantics (equal bounds return without
  draw; unbounded `nextInt`, `MIN_VALUE` stays negative, modulo `(max-min)`)

## Overview

Implement the smallest honest simulation slice inside `rewrite/core` sufficient
to make the state-hash gate meaningful:

- `FixedPoint`: 8.8 helpers (shift-left conversion, add/mul in `Int`).
- `DeterministicRandom`: Java LCG parity implementation
  (`java.util.Random` 48-bit seed math) with serializable full state.
- `SpikeWorld`: one player entity with fixed-position/velocity on the 400x240
  field, bounce-at-bounds motion, touch-driven steer, tick counter.
- `TickEngine`: accumulator-driven fixed-step loop consuming a scripted or
  live `InputQueue`; emits an immutable `CommittedTick` (snapshot + SHA-256
  state hash + deferred audio/UI commands) per accepted tick.
- `SaveSnapshot`: versioned binary layout written/read through an injected
  `SavePort` interface (no platform file API in core).

## Requirements

- Tick boundary is the only place gameplay state mutates.
- State hash covers tick index, entity positions/velocities, RNG state, and
  published command sequence — enough to detect divergence.
- No wall-clock reads inside the simulation.
- JUnit test replays a recorded input script twice and asserts identical hash
  sequence; a second test covers `j.a(min,max)` parity vectors (zero, positive,
  negative, `MIN_VALUE`, equal bounds).

## Validation

`./gradlew :core:test` green; hash sequence stable across runs and JVMs.
