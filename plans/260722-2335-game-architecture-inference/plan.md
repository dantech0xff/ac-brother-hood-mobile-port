---
title: Assassin's Creed Java ME Architecture Inference
description: >-
  Reconstruct an evidence-backed semantic architecture, runtime flow, and
  gameplay model from the static Java ME artifacts.
status: completed
priority: P1
branch: main
tags:
  - docs
  - reverse-engineering
  - architecture
  - critical
blockedBy: []
blocks: []
created: '2026-07-22T15:36:25.167Z'
createdBy: 'ck:plan'
source: skill
---

# Assassin's Creed Java ME Architecture Inference

## Overview

Produce an evidence-backed legacy architecture dossier for the decompiled
Assassin's Creed Java ME game, then reconcile every existing architecture,
resource, symbol, framework-decision, and modern-port document whose contract is
invalidated or materially refined by that evidence. Separate bytecode-proven
facts, high-confidence semantic inference, weaker hypotheses, and unknowns.
Static analysis only; no MIDlet execution and no claim that inferred aliases
are original source names.

Scope was expanded during adversarial review because the recovered ordering,
identity, save, timing, and entity-state contracts contradicted assumptions in
the existing modern design. Those dependent corrections are part of this plan,
including an explicit exact-resume extension contract; implementation of the
modern port remains out of scope.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Evidence and Semantic Class Map](./phase-01-evidence-and-semantic-class-map.md) | Completed |
| 2 | [Runtime and Gameplay Flow Reconstruction](./phase-02-runtime-and-gameplay-flow-reconstruction.md) | Completed |
| 3 | [Architecture Dossier and Verification](./phase-03-architecture-dossier-and-verification.md) | Completed |

## Dependencies

- Uses completed reconstruction outputs from
  `../260722-1922-assassins-creed-reconstruction/` and
  `../260722-2207-residual-static-recovery/`.
- Canonical evidence: `reconstructed-project/bytecode/`, `inventory/`, decoded
  resources, existing format docs, and structured/simple/fallback source views.

## Acceptance Criteria

- Every semantic claim names an evidence anchor and confidence level.
- All 12 legacy classes receive a role, boundary, proposed alias, and caveat.
- Bootstrap, main loop, game state, level load, gameplay tick, combat/AI,
  render, audio, save/load, and mission script flows are represented.
- Mermaid diagrams distinguish observed edges from inferred architecture.
- Dependent docs use the same terminology, confidence policy, and recovered
  ordering/type/identity contracts as the dossier.
- The modern exact-resume proposal explicitly snapshots every required
  dual-coordinate, runtime-type, identity-allocator, timeline, RNG, input, and
  global authority needed by its determinism claim, and labels departures from
  legacy persistence.
- Static verifier and documentation link checks pass after integration.
