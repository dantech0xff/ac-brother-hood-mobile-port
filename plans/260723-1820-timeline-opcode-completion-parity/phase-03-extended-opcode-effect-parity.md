---
phase: 3
title: "Extended Opcode Effect Parity"
status: completed
priority: P1
dependencies:
  - 2
---

# Phase 3: Extended Opcode Effect Parity

## Context Links

- [Plan](./plan.md)
- [Level script format](../../docs/level-record-formats.md)
- Evidence: `reconstructed-project/bytecode/i.javap.txt:66643`,
  `reconstructed-project/src/structured/i.java:18499`, and
  `scripts/decode-gameloft-level-records.py:360`.

## Overview

Implement bytecode-proven operand decoding, time guards, direct mutations, and
ordered helper intentions for opcodes `100..114`.

## Requirements

- Functional: exact-width little-endian decode; signed words for
  `100/101/102/104`; unsigned words/bytes elsewhere; bytecode-authoritative
  opcode 100 modes; prompt chains `107->108` and `112->113`; abort return.
- Non-functional: helper outcomes are injected typed observations; malformed
  prompt ordering fails explicitly rather than inventing state.

## Architecture

Execution first reads the legacy width, then applies the common non-exact guard.
All handlers mutate immutable host state or emit typed intentions. Opcodes
`108/113` alone poll before the event tick and can switch script/return `-1`
at the exact tick. Opcode `109` remains a labeled synthetic source-contract.

## Corpus Matrix

Counts for `100..114`:
`[204,1,90,7,5,59,16,30,30,0,9,25,1,1,2]` (480 total).

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/timeline_opcode_contracts.py`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/fixtures/timeline-opcode-contracts.json`
- Modify: `/Users/dan/Desktop/ac-java-game/tests/test_timeline_opcode_contracts.py`

## Implementation Steps

1. Decode all 15 layouts and preserve opcode 103 opaque bytes.
2. Implement common guard before operand access for non-`108/113` handlers.
3. Implement opcodes `100..107` including target/context validation.
4. Implement stateful `108`, exact-tick branch switch, and width/abort return.
5. Implement `109..112`, including unreachable post-event cleanup note.
6. Implement stateful `113` and timed-text opcode `114`.
7. Compose executor return with the existing scheduler abort mapping.

## Validation

```bash
python3 -B -m unittest tests.test_timeline_opcode_contracts -v
python3 -B -m unittest tests.test_gameplay_parity_contracts.TimelineSchedulingContractTests -v
```

## Success Criteria

- [x] Each opcode has exact-tick behavior and a non-exact guard regression.
- [x] Signed/high-bit unsigned boundaries and exact width failures are tested.
- [x] Opcode 100 mode/subaction matrix follows raw bytecode, not decompiler
  fallthrough artifacts.
- [x] `108/113` preconditions, success/failure/cancel, cleanup, and `-1`
  ordering are tested.
- [x] Opcode 109 is never described as shipped corpus evidence.

## Risk Assessment

Risks: dynamic subsystem outcomes and legacy null/array faults. Mitigation:
typed injected contexts, ordered intentions, explicit preconditions, and
separate evidence labels.

Rollback: reverse opcode handlers/fixtures as one additive unit; the existing
scheduler continues to use its unchanged `extended_dispatch_results` boundary.

## Security Considerations

Only pure Python over fixture bytes and decoded JSON; target execution forbidden.
