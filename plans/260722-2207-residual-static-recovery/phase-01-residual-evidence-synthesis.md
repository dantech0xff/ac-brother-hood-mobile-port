---
phase: 1
title: Residual Evidence Synthesis
status: completed
effort: ''
priority: P1
dependencies: []
---

# Phase 1: Residual Evidence Synthesis

## Overview

Reconcile three independent read-only investigations into an evidence matrix
for sprite code `0x27f1`, script metadata/opcodes, entity fields, and slot 3.

## Requirements

- Functional: record exact byte ranges, counts, bytecode locations, and corpus
  invariants for each residual.
- Non-functional: separate runtime-proven facts from data-derived recovery and
  negative evidence.

## Related Code Files

- Read: `reconstructed-project/bytecode/{b,i,j,k}.javap.txt`
- Read: `reconstructed-project/resources/decoded/`
- Update: `docs/level-record-formats.md`
- Update: `docs/resource-formats.md`

## Implementation Steps

1. Verify `0x27f1` control-token grammar against both payloads and palettes.
2. Prove whether the EOF-before-palette asset is intentionally non-rendering.
3. Reconcile mode/opcode occurrence counts and `group_meta` invariants.
4. Quantify slot-3 correlations and promote only supported entity field names.
5. Preserve unresolved authoring names as unknown.

## Success Criteria

- [x] Every promoted claim has method/offset or all-corpus evidence.
- [x] No hypothesis is presented as recovered runtime behavior.
- [x] Remaining unknowns are narrower than the prior report.

## Risk Assessment

The largest risk is mistaking a corpus coincidence for runtime semantics.
Mitigate with exact EOF checks, independent reproduction, and explicit
confidence labels.
