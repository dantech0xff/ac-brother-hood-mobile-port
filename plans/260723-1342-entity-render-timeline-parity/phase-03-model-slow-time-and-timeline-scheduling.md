---
phase: 3
title: Model Slow-Time and Timeline Scheduling
status: completed
priority: P1
dependencies:
  - 2
---

# Phase 3: Model Slow-Time and Timeline Scheduling

## Context Links

- [Plan](./plan.md)
- [Level script format](../../docs/level-record-formats.md)
- Evidence: `reconstructed-project/bytecode/i.javap.txt` methods `i.I:()V`,
  `i.aa:()V`, `i.ab:()Z`; `k.javap.txt` method `k.s:(I)I`.

## Overview

Extend fixed-point integration with Java slow-time arithmetic and add the
smallest honest executable timeline boundary: activity, group lookup, tick
gating, normalized lane cursors, current-event dispatch, due cursor advancement,
and extended-executor abort outcomes.

## Requirements

- Functional: Java truncate-toward-zero division, `MIN/-1` wrap, zero-divisor
  failure, pre-increment evaluated tick, one current event per lane per call,
  `<=` cursor-advance test, signed-byte opcode branching, extended abort, and
  short-circuit slow gate.
- Non-functional: existing normal integration call remains source compatible;
  no undocumented opcode effect is simulated.

## Architecture

Decoded events are normalized into tick/raw-opcode records. Every current lane
event enters the dispatch boundary even when its tick is still in the future;
only its cursor advance is gated by `event_tick <= evaluated_tick`. Dispatch
classification compares the signed `baload` value (`<100` inline, otherwise
extended), so raw bytes `128..255` take the inline branch. Exact-tick opcodes
108/113 require an explicit extended result because a negative result returns
before the current cursor advances. The model does not emulate opcode effects
or `bI()` cleanup.

## Fixture Matrix

| Area | Required cases |
|---|---|
| Slow integration | positive divisor; negative contributions; `MIN/-1` wrap; reachable zero divisor |
| Group ID scan | corpus hit; missing ID; first duplicate match in a synthetic list |
| Activity | active; paused; missing group; negative tick |
| Timeline step | paused return; old-tick evaluation; slow gate hold/advance; latched short-circuit; one current event per lane; future dispatch with held cursor; signed-short wrap |
| Dispatch boundary | representative inline/extended opcodes; signed high-bit bytes; exact-tick 108/113 normal/abort results |

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/gameplay_parity_contracts.py`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/fixtures/gameplay-parity-contracts.json`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/test_gameplay_parity_contracts.py`

## Implementation Steps

1. Add exact Java integer division/remainder plus signed byte/short helpers.
2. Extend coordinate integration with optional slow divisor.
3. Implement script-group first-match lookup and activity predicate.
4. Implement normalized per-lane scheduler with explicit completion boundary.
5. Pin representative slot-7 corpus group/event sources and add synthetic
   slow/paused/error edge cases.
6. Re-run the in-memory oracle across all eight decoded packs.

## Success Criteria

- [x] Old normal-coordinate fixture passes unchanged.
- [x] Slow velocity and acceleration contributions divide before addition.
- [x] Zero divisor fails only on a reachable division/remainder path.
- [x] Group lookup returns first index or `-1`; activity exactly matches
  `ca >= 0 && !cd[0] && cK >= 0`.
- [x] Scheduler evaluates the old tick, conditionally increments the signed-short
  tick, dispatches each lane's current event, and advances at most one due cursor.
- [x] Opcode branching uses signed Java bytes; exact-tick 108/113 can abort
  before cursor advance from an explicit executor result.
- [x] Corpus event opcodes are classified without inventing their effects.
- [x] A reachable zero divisor raises; a short-circuited zero divisor does not.

## Risk Assessment

Risk: claiming full `i.aa()` parity from a scheduler-only model. Mitigation:
name returned actions “dispatch intentions,” expose the negative-return boundary,
document the `bI()` boundary, and keep unsupported effects outside acceptance
criteria.

## Security Considerations

All corpus reads are bounded regular repository files. No JAR, MIDlet,
emulator, simulator, device, or network action.
