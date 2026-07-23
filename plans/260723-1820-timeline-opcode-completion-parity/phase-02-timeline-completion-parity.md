---
phase: 2
title: "Timeline Completion Parity"
status: completed
priority: P1
dependencies:
  - 1
---

# Phase 2: Timeline Completion Parity

## Context Links

- [Plan](./plan.md)
- Evidence: `reconstructed-project/bytecode/i.javap.txt:64541` and
  `reconstructed-project/src/structured/i.java:17851`.

## Overview

Model `i.bI()` as an immutable transition over directly written fields and an
ordered intention stream for helper calls.

## Requirements

- Functional: selected-owner resets, `cd[2]` clear, `cd[3]` repeat, `cK=-2`,
  `k.aw=0`, type-5 focus/Y adjustment, chained script, `cd[4]` reset,
  type-58 retention, type-5 subtype flag clear, and removal intention.
- Non-functional: preserve JVM reference-identity decisions and every early
  return boundary.

## Architecture

`TimelineCompletionState` contains the owner/world fields directly mutated by
bytecode. `TimelineCompletionContext` supplies pure observations such as
`ai()` and resolved script index. `TimelineCompletionResult` returns the next
state, ordered intentions, return boundary, and trace.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/timeline_opcode_contracts.py`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/fixtures/timeline-opcode-contracts.json`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/test_timeline_opcode_contracts.py`

## Implementation Steps

1. Encode selected-owner global/attachment cleanup before local completion.
2. Encode repeat path returning before `k.aw=0`.
3. Encode normal completion and type-5 focus/player-Y mutations.
4. Encode chained-script branch and its `cP=-1` early return.
5. Encode reset, retention, flag-clear, and removal endings.
6. Add identity-collision and second-order `g.a` clearing regressions.

## Validation

```bash
python3 -B -m unittest tests.test_timeline_opcode_contracts.TimelineCompletionContractTests -v
```

## Success Criteria

- [x] Every bytecode return PC is represented by a distinct result boundary.
- [x] Repeat, chained, reset, retained, flag-cleared, removed, and non-selected
  paths are fixture-locked.
- [x] Ordered intentions preserve `k.s` lookup duplication and helper order.
- [x] No `i.p()` or downstream helper internals are invented.

## Risk Assessment

Risk: using token equality as JVM identity. Mitigation: state/context carry
reference objects and comparisons use object identity.

Rollback: reverse only completion dataclasses/function and its fixture cases;
the additive module has no caller in existing Slice 1/2 APIs.

## Security Considerations

Pure host transition only; no file, network, game, UI, or audio side effect.
