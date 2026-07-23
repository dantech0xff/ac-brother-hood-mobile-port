---
phase: 2
title: Runtime and Gameplay Flow Reconstruction
status: completed
priority: P1
dependencies:
  - 1
---

# Phase 2: Runtime and Gameplay Flow Reconstruction

## Overview

Reconstruct execution and data flows from MIDlet bootstrap through gameplay,
mission progression, presentation, persistence, and shutdown.

## Requirements

- Functional: identify state owners, transition triggers, tick ordering,
  actor update/combat relationships, and resource lifecycle.
- Non-functional: never infer temporal ordering solely from a dependency edge;
  use method bodies or bytecode offsets for sequence claims.

## Architecture

Build a multi-level model: lifecycle sequence, controller state machine,
per-frame update/render pipeline, actor/combat collaboration, and level-script
data flow.

## Related Code Files

- Read: phase 1 reports and canonical artifacts.
- Create: `docs/inferred-legacy-game-architecture.md`

## Implementation Steps

1. Trace `GloftASBR` lifecycle into `k`/`j` construction and loop ownership.
2. Recover controller states and level/resource load transitions.
3. Trace gameplay tick, input, physics/collision, AI/combat, animation, camera,
   render, audio, and script dispatch.
4. Express proven and inferred flow variants as Mermaid diagrams.

## Success Criteria

- [x] Major runtime flows have source/bytecode anchors.
- [x] Observed ordering and inferred subsystem boundaries are visually distinct.
- [x] Unknown event opcodes/entity subtypes remain in an explicit ledger.

## Risk Assessment

Decompiler restructuring can distort loops and switch tables. Mitigation:
cross-check suspicious methods against `javap`, simple, and fallback views.
