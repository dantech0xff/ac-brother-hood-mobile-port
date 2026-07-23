---
title: Timeline Opcode and Completion Parity
description: >-
  Model the bytecode-proven timeline completion transition and extended opcode
  effects without executing the legacy game.
status: completed
priority: P1
effort: large
branch: main
tags:
  - feature
  - reverse-engineering
  - tests
  - critical
blockedBy: []
blocks: []
created: '2026-07-23'
createdBy: 'ck:plan'
source: skill
---

# Timeline Opcode and Completion Parity

## Overview

Extend the static-only host parity harness across `i.bI:()V` and
`i.a:(I[BIII)I`. Preserve every Slice 1/2 API and fixture while adding exact
operand decoding, directly proven state transitions, and ordered intentions for
downstream runtime calls that remain outside this clean-room boundary.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Canonical Aliases and Contract Schema](./phase-01-canonical-aliases-and-contract-schema.md) | Completed |
| 2 | [Timeline Completion Parity](./phase-02-timeline-completion-parity.md) | Completed |
| 3 | [Extended Opcode Effect Parity](./phase-03-extended-opcode-effect-parity.md) | Completed |
| 4 | [Verification and Documentation](./phase-04-verification-and-documentation.md) | Completed |

## Dependencies

- Builds on completed
  [`260723-0852-semantic-registry-parity-harness`](../260723-0852-semantic-registry-parity-harness/plan.md)
  and
  [`260723-1342-entity-render-timeline-parity`](../260723-1342-entity-render-timeline-parity/plan.md).
- Bytecode is authoritative; decoded slot-7 JSON is the corpus oracle.
- Phases are sequential because effects depend on the shared typed schema.

## Scope Boundary

- In: `i.bI:()V`, `i.a:(I[BIII)I`, opcodes `100..114`, operand widths and
  signedness, prompt state, exact/future tick guards, and `108/113` aborts.
- Out: target execution, recursive helper emulation, full UI/audio/AI/collision,
  player FSM, rendering, and modern mobile implementation.

## Acceptance Criteria

- Existing 30 fixtures and current public calls/result shapes remain unchanged.
- Registry reaches 12 class / 44 method / 41 field aliases.
- All 3,705 corpus instructions retain exact layouts; all 480 extended
  occurrences match the pinned opcode matrix.
- Direct mutations and `i.bI()` early-return order are executable contracts;
  external helper effects remain ordered intentions.
- Focused/discovery tests and the broad verifier pass with
  `game_execution_performed=false`.

## Unresolved Questions

None. Recursive downstream runtime emulation is explicitly deferred.
