---
title: Semantic Registry and Gameplay Parity Harness
description: >-
  Turn recovered semantic aliases and gameplay invariants into offline,
  executable contracts with a decoded-corpus oracle and separately labeled
  synthetic fixtures.
status: completed
priority: P1
branch: main
tags:
  - feature
  - reverse-engineering
  - tests
  - critical
blockedBy: []
blocks: []
created: '2026-07-23T00:52:23.862Z'
createdBy: 'ck:plan'
source: skill
---

# Semantic Registry and Gameplay Parity Harness

## Overview

Promote the existing canonical alias overlay into the first executable Semantic
Registry slice, then add a Python-standard-library parity harness for entity
materialization, legacy ID lookup, dual-coordinate integration, and decoded
level schema/provenance. Static analysis only: the JAR, MIDlet, classes,
emulator, simulator, and device are never executed.

### Expected output

- Expanded canonical method aliases, regenerated inventory copy, and updated
  verifier pins.
- Importable gameplay contract reference functions plus a small fixture manifest
  that points to canonical decoded records instead of copying large artifacts.
- Offline `unittest` coverage and an evergreen contract guide.

### Scope boundary

Out of scope: Kotlin/LibGDX runtime, rendering, UI, audio backend, full opcode or
entity taxonomy, target-code execution, and claims of runtime parity. Synthetic
fixtures are labeled source-contract evidence and never presented as shipped
corpus examples.

## Scope Decision

- Existing code: strict alias schema/generator/verifier and full level decoder
  are reused; no second registry or decoder fork.
- Minimum changes: eight aliases, one importable contract module, one compact
  fixture manifest, one `unittest` module, and integration docs.
- Complexity: three sequential phases; no new external dependency or service.
- Selected mode: **HOLD SCOPE**, following the user's approval of the proposed
  contract-first Phase 4 slice.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Canonical Semantic Registry](./phase-01-canonical-semantic-registry.md) | Completed |
| 2 | [Executable Gameplay Parity Contracts](./phase-02-executable-gameplay-parity-contracts.md) | Completed |
| 3 | [Verification and Documentation](./phase-03-verification-and-documentation.md) | Completed |

## Dependencies

- Evidence baseline: completed
  [`260722-2335-game-architecture-inference`](../260722-2335-game-architecture-inference/plan.md).
- Canonical aliases: `scripts/java-me-semantic-aliases.json`.
- Canonical decoded level data:
  `reconstructed-project/resources/levels-decoded/`.
- Python standard library only; no new package manager or network dependency.

## Acceptance Criteria

- Alias source and generated inventory copy are identical, schema-valid,
  descriptor-valid, and reproducible from static bytecode inventory.
- Corpus oracle covers all eight decoded packs and 4,286 records without writing
  generated outputs or executing target code.
- Entity remap/order, player routing, lookup precedence/`-1`, and dual-coordinate
  integration fixtures pass with explicit evidence basis.
- Existing full static verifier remains `ok=true` with
  `game_execution_performed=false`.
- Documentation names exact commands, provenance boundaries, known corpus gaps,
  and future extension points.

## Completion Evidence

- [Implementation verification report](./reports/implementation-verification.md)
