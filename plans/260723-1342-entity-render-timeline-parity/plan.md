---
title: 'Entity Store, Render, Slow-Time, and Timeline Parity'
description: >-
  Extend the static-only parity harness with bytecode-proven world-store,
  render-order, slow-time, and timeline scheduler contracts.
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
created: '2026-07-23T05:42:49.005Z'
createdBy: 'ck:plan'
source: skill
---

# Entity Store, Render, Slow-Time, and Timeline Parity

## Overview

Extend the existing clean-room Python specification without executing the
legacy game. Preserve current APIs while adding exact JVM arithmetic, entity
slot lifecycle, render/interaction insertion, and the provable timeline
scheduler boundary. Opcode side effects that lack static proof remain deferred.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Promote Timeline Semantic Aliases](./phase-01-promote-timeline-semantic-aliases.md) | Completed |
| 2 | [Model Entity Store and Render Ordering](./phase-02-model-entity-store-and-render-ordering.md) | Completed |
| 3 | [Model Slow-Time and Timeline Scheduling](./phase-03-model-slow-time-and-timeline-scheduling.md) | Completed |
| 4 | [Verify Corpus Contracts and Update Documentation](./phase-04-verify-corpus-contracts-and-update-documentation.md) | Completed |

## Dependencies

- Builds on completed plan
  [`260723-0852-semantic-registry-parity-harness`](../260723-0852-semantic-registry-parity-harness/plan.md).
- Authoritative evidence: structured decompile, `javap`, and pinned decoded
  level corpus already present in the repository.
- Sequential phases: later fixtures and verifier pins depend on earlier API and
  registry decisions.

## Scope Boundary

- In: `k.b(i)`, `k.c(i)`, `k.d(i)`, slow branch of `i.I()`,
  `i.aa()`, `i.ab()`, and `k.s(int)`.
- Out: JAR/MIDlet execution, emulator/device use, full opcode-effect emulation,
  Kotlin/LibGDX runtime, AI/collision/full-frame parity.

## Acceptance Criteria

- Existing public Python calls remain valid.
- Entity free-list, tombstone, global-reset, callback-order, duplicate, and
  silent-drop behavior are fixture-locked.
- Render order is `az` then `al`; exact ties insert newest first; overflow is
  explicitly unsafe.
- Slow-time uses Java division/overflow semantics and preserves operation order.
- Timeline activity, group lookup, pre-increment tick snapshot, per-lane
  current-event dispatch, due cursor advancement, slow gate, signed-byte
  low/extended branching, and negative extended-return boundary are executable
  contracts.
- Full tests, corpus re-decode, inventory regeneration, and static verifier
  pass with `game_execution_performed=false`.

## Unresolved Questions

None. Full opcode side effects are intentionally deferred, not unresolved
inside this slice.
