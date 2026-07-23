---
phase: 4
title: Verify Corpus Contracts and Update Documentation
status: completed
priority: P1
dependencies:
  - 3
---

# Phase 4: Verify Corpus Contracts and Update Documentation

## Context Links

- [Plan](./plan.md)
- [Parity guide](../../docs/semantic-registry-and-parity-harness.md)
- [Project standards](../../docs/code-standards.md)

## Overview

Run narrow-to-broad static gates, review public-contract impact, and synchronize
architecture, counts, roadmap, guide, and plan evidence.

## Requirements

- Functional: all tests, full corpus oracle, inventory regeneration, and broad
  verifier pass.
- Non-functional: documentation distinguishes proven behavior, host contract,
  unsafe legacy behavior, and deferred opcode semantics.

## Architecture

Tests remain the executable contract; decoded corpus remains the input oracle;
the verifier remains the repository-wide integrity gate. Documentation links
those layers without treating host execution as game runtime proof.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/README.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/codebase-summary.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/inferred-legacy-game-architecture.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/level-record-formats.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/project-overview-pdr.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/project-roadmap.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/save-format.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/semantic-registry-and-parity-harness.md`
- Modify: `/Users/dan/Desktop/ac-java-game/docs/symbol-map.md`
- Create: `/Users/dan/Desktop/ac-java-game/plans/260723-1342-entity-render-timeline-parity/reports/implementation-verification.md`
- Create: `/Users/dan/Desktop/ac-java-game/plans/260723-1342-entity-render-timeline-parity/reports/code-review.md`
- Create: one timestamped entry under
  `/Users/dan/Desktop/ac-java-game/docs/journals/`.

## Implementation Steps

1. Run focused test module, discovery, syntax compilation, and corpus oracle.
2. Regenerate inventory and run broad static reconstruction verifier.
3. Delegate test, debugging, code-review, documentation, and plan-sync gates.
4. Update counts, contract tables, evidence boundaries, and next-step roadmap.
5. Record commands and results in a plan-scoped report; journal durable
   decisions. Do not commit unless the user separately authorizes it.

## Success Criteria

- [x] Focused and discovered tests pass 100%.
- [x] All 4,286 entity records and 16 payloads still re-decode exactly.
- [x] Verifier reports `ok=true`, empty failures, and
  `game_execution_performed=false`.
- [x] Reviewer finds no acceptance, regression, contract, or static-safety
  blocker.
- [x] Every plan phase and relevant evergreen document reflects actual output.

## Risk Assessment

Risk: stale exact counts or hashes across dirty prior work. Mitigation: preserve
all unrelated changes, derive facts from current artifacts, and inspect scoped
diffs before updating claims.

## Security Considerations

Never execute or load target classes. Do not stage, commit, publish, or send
external messages without explicit user authorization.
