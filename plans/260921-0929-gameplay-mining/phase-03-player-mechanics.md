---
title: Player Mechanics
phase: 3
status: completed
---

# Phase 3 — Player Mechanics

`g` (`PlayerActor`, extends `i`) owns the player FSM `g.e()` plus `g.n()`
(type 25). Mine movement/combat/parkour constants and state semantics.

## Deliverable

`docs/gameplay-mining/player-mechanics.md`: FSM state table (state number →
behaviour → animation/sprite evidence), input→action map, physics constants
(speed/accel/gravity verbatim), health/damage/score fields, cheat paths
(god mode, free-fly, FPS).

## Method

- Structured `g.java` + `i.java` read; warning-tagged methods cross-checked
  against simple/bytecode where semantics matter.
