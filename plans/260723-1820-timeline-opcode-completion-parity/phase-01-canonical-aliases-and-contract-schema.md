---
phase: 1
title: Canonical Aliases and Contract Schema
status: completed
priority: P1
dependencies: []
---

# Phase 1: Canonical Aliases and Contract Schema

## Context Links

- [Plan](./plan.md)
- [Semantic registry guide](../../docs/semantic-registry-and-parity-harness.md)
- Evidence: `reconstructed-project/bytecode/i.javap.txt:64541`,
  `reconstructed-project/bytecode/i.javap.txt:66643`,
  `scripts/decode-gameloft-level-records.py:334`,
  `scripts/gameplay_parity_contracts.py:725`, and
  `tests/fixtures/gameplay-parity-contracts.json:1`.

## Overview

Promote the completion/dispatcher working names and define an additive,
JSON-serializable host schema without changing existing scheduler dataclasses.

## Requirements

- Functional: descriptor-qualified aliases; typed operands/state/context/result;
  exact widths `[6,2,4,2,2,4,9,2,4,8,4,9,6,4,4]`.
- Non-functional: Python standard library only; bytecode terminology where
  semantics remain unknown; existing 30-fixture manifest untouched.

## Architecture

Create `timeline_opcode_contracts.py` as a separate clean-room boundary. It
owns completion/opcode dataclasses and ordered typed intentions. The existing
`gameplay_parity_contracts.py` scheduler remains source compatible and can
consume an executor return through `extended_dispatch_results`.

## Related Code Files

- Create: `/Users/dan/Desktop/ac-java-game/scripts/timeline_opcode_contracts.py`
- Create: `/Users/dan/Desktop/ac-java-game/tests/fixtures/timeline-opcode-contracts.json`
- Create: `/Users/dan/Desktop/ac-java-game/tests/test_timeline_opcode_contracts.py`
- Modify: `/Users/dan/Desktop/ac-java-game/scripts/java-me-semantic-aliases.json`
- Generated later:
  `/Users/dan/Desktop/ac-java-game/reconstructed-project/inventory/semantic-aliases.json`

## Implementation Steps

1. Add `i.bI:()V -> completeTimelineScript`.
2. Add `i.a:(I[BIII)I -> executeExtendedTimelineOpcode`.
3. Define exact operand decoder, direct-state projections, typed contexts,
   typed intentions, and result envelopes.
4. Reject booleans as ints, invalid opcodes, wrong widths/context types, and
   out-of-range Java values.
5. Cross-test width/storage against the canonical level decoder.

## Validation

```bash
python3 -B -m py_compile scripts/timeline_opcode_contracts.py
python3 -B -c "import json; d=json.load(open('scripts/java-me-semantic-aliases.json')); assert len(d['methods']) == 44"
python3 -B -m unittest tests.test_timeline_opcode_contracts.TimelineOpcodeSchemaTests -v
```

## Success Criteria

- [x] Source registry contains exactly the two new aliases.
- [x] Schema adds no required field or signature to an existing public type.
- [x] All 15 widths and signed/unsigned layouts match bytecode and decoder.
- [x] New fixture envelope declares static-only/no target execution.

## Risk Assessment

Risk: a second parser becomes an authority. Mitigation: keep the contract
decoder small and assert every width/storage against `OPCODE_LAYOUTS`.

Rollback: remove only the three additive Slice 3 files and reverse the two
registry entries; no existing public contract or fixture requires migration.

## Security Considerations

No target class loading or execution. Inputs are bounded repository bytes/JSON.
