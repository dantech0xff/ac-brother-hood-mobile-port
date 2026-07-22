---
title: Residual Java ME Static Recovery
description: >-
  Deepen the completed static reconstruction by recovering the 0x27f1 sprite
  payloads and publishing deterministic machine-readable level records.
status: completed
priority: P1
branch: ''
tags:
  - reverse-engineering
  - java-me
  - static-analysis
blockedBy: []
blocks: []
created: '2026-07-22T14:08:27.339Z'
createdBy: 'ck:plan'
source: skill
---

# Residual Java ME Static Recovery

## Overview

Extend the completed six-phase reconstruction without changing its safety
boundary. Recover only conclusions supported by bytecode or exact corpus
invariants, publish new decoded artifacts deterministically, and never launch
the JAR, MIDlet, classes, emulator, simulator, or device runtime.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Residual Evidence Synthesis](./phase-01-residual-evidence-synthesis.md) | Completed |
| 2 | [Sprite Recovery](./phase-02-sprite-recovery.md) | Completed |
| 3 | [Level Record Decoder](./phase-03-level-record-decoder.md) | Completed |
| 4 | [Integration and Audit](./phase-04-integration-and-audit.md) | Completed |

## Dependencies

- Consumes the completed `260722-1922-assassins-creed-reconstruction` package.
- Phase 2 and Phase 3 consume Phase 1 evidence and may then proceed in parallel.
- Phase 4 requires both new decoder outputs.

## Acceptance Criteria

- Two `0x27f1` module payloads decode exactly to their declared dimensions with
  no trailing bytes or invalid palette indexes; evidence remains explicitly
  data-derived because recovered runtime bytecode has no fill branch.
- All eight slot-0 and slot-7 pairs are available as deterministic JSON, retain
  raw values/offsets, and parse exactly to EOF.
- New outputs are reproducible from the pinned static inputs and independently
  verified without executing target code.

Phase 4 closed after the root exact verifier and link-count review passed.
