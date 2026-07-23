---
phase: 3
title: Architecture Dossier and Verification
status: completed
priority: P1
dependencies:
  - 2
---

# Phase 3: Architecture Dossier and Verification

## Overview

Integrate the evidence into one navigable dossier, validate every strong claim,
expose uncertainty and follow-up priorities, and reconcile dependent documents
whose architecture contracts conflict with the recovered evidence.

## Requirements

- Functional: class matrix, subsystem boundaries, diagrams, method exemplars,
  confidence ledger, naming proposal, and static-analysis backlog.
- Non-functional: preserve existing public facts and canonical counts; avoid
  duplicate or contradictory architecture narratives.

## Architecture

The dossier is the detailed legacy companion to `docs/system-architecture.md`.
The existing document stays the concise overview and links to the dossier.

## Related Code Files

- Create: `docs/inferred-legacy-game-architecture.md`
- Modify: `docs/system-architecture.md`
- Modify: `docs/codebase-summary.md`
- Modify: `docs/symbol-map.md`
- Modify: `docs/resource-formats.md`
- Modify: `docs/reverse-engineering-technical-analysis.md`
- Modify: `docs/modern-mobile-technical-design.md`
- Modify: `docs/decisions/mobile-game-framework.md`
- Modify: `README.md`
- Create: `reports/architecture-verification.md`

## Implementation Steps

1. Synthesize reports and direct checks into the dossier.
2. Validate Mermaid syntax, internal links, symbols, counts, and confidence
   language.
3. Run the repository's static verifier without executing the MIDlet.
4. Review for overclaiming and reconcile docs, including the modern port's
   parity boundaries and exact-resume extension contract.

## Success Criteria

- [x] Dossier is self-contained and linked from entry points.
- [x] Mermaid blocks parse or pass an equivalent syntax check.
- [x] Static reconstruction verifier passes.
- [x] No unresolved contradiction remains between updated docs.
- [x] Exact-resume state, allocator, input, timing, and identity contracts are
      explicit enough to produce parity fixtures without inventing state.

## Risk Assessment

Documentation can accidentally promote hypotheses into facts or make a modern
extension look like recovered legacy behavior. Mitigation: claim-level
confidence tags, evidence tables, explicit parity/departure labels, static
checks, and an adversarial review pass over all changed contracts.
