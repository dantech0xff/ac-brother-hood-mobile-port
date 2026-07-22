---
phase: 3
title: "Level Record Decoder"
status: completed
effort: ""
priority: P1
dependencies: [1]
---

# Phase 3: Level Record Decoder

## Overview

Turn the already proven slot-0/slot-7 grammars into a reproducible,
machine-readable static decoder for all eight level packs.

## Requirements

- Functional: emit raw and interpreted entity, group, lane, event, instruction,
  operand, offset, and summary records.
- Non-functional: no target-code execution; strict bounds/caps; exact EOF;
  deterministic JSON; staged rollback-capable publication.

## Architecture

`scripts/decode-gameloft-level-records.py` consumes only the extractor's decoded
pack metadata and payload files. It publishes one directory per pack plus a
summary and managed manifest under `resources/levels-decoded/`.

## Related Code Files

- Create: `scripts/decode-gameloft-level-records.py`
- Create/regenerate: `reconstructed-project/resources/levels-decoded/`
- Modify: `scripts/verify-static-reconstruction.py`
- Update: `docs/level-record-formats.md`

## Implementation Steps

1. Implement a bounds-checking LE cursor and pinned pack/slot discovery.
2. Decode slot 0 records with raw fields, retype evidence, offsets, and
   discriminator histograms.
3. Decode slot 7 groups/lanes/events/instructions using the exact opcode-width
   table; retain raw operands and parsed signed/unsigned words.
4. Emit the observed `group_meta == declared event total` invariant with a
   confidence label, not an invented original name.
5. Add safety caps, deterministic ordering, staged publication, shared locking,
   and rerun comparison.

## Success Criteria

- [x] 4,286 entity records and 3,705 instructions are emitted.
- [x] 144 groups, 510 lanes, and 2,366 events reconcile exactly.
- [x] Every one of 16 payloads parses to EOF with original SHA-256 retained.
- [x] A second run produces the identical managed-tree hash.

## Risk Assessment

Decompiler errors around cursor arithmetic could corrupt boundaries. Exact
bytecode offsets, known totals, and full-payload EOF are mandatory guards.
