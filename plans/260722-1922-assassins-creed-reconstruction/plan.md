---
title: Assassin's Creed Java ME Static Reconstruction
description: >-
  Static-only recovery of the complete Java ME program and resources,
  reproducible reverse-engineering documentation, and a modern Android/iOS
  rewrite design.
status: completed
priority: P1
branch: ''
tags: []
blockedBy: []
blocks: []
created: '2026-07-22T11:22:37.013Z'
createdBy: 'ck:plan'
source: skill
---

# Assassin's Creed Java ME Static Reconstruction

## Overview

Recover the supplied Java ME game without ever executing the JAR. The deliverable preserves four complementary views of every class (structured decompile, simple/goto decompile, fallback decompile, exact `javap` bytecode), decodes all supported resources, documents confidence and remaining unknowns, and specifies a modern mobile rewrite that reuses the recovered content.

## Guardrails

- Static analysis only. Never invoke the game with `java -jar`, a MIDlet runner, emulator, simulator, or device.
- Decompiler/tool execution is allowed only when the JAR is treated as input data.
- Preserve the original SHA-256 and archive untouched.
- Separate proven facts, high-confidence semantic recovery, and hypotheses.
- Do not claim source equivalence where obfuscation destroyed names or structure.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Baseline and Completeness Inventory](./phase-01-baseline-and-completeness-inventory.md) | Completed |
| 2 | [Semantic Source Reconstruction](./phase-02-semantic-source-reconstruction.md) | Completed |
| 3 | [Resource Format Reconstruction](./phase-03-resource-format-reconstruction.md) | Completed |
| 4 | [Reverse Engineering Technical Analysis](./phase-04-reverse-engineering-technical-analysis.md) | Completed |
| 5 | [Modern Mobile Technical Design](./phase-05-modern-mobile-technical-design.md) | Completed |
| 6 | [Verification and Final Handoff](./phase-06-verification-and-final-handoff.md) | Completed |

## Dependencies

- Phase 1 establishes inventory and acceptance metrics.
- Phases 2 and 3 may proceed in parallel after Phase 1.
- Phase 4 consumes Phases 1–3.
- Phase 5 consumes the resource and behavior contracts from Phases 2–3.
- Phase 6 audits every explicit assignment requirement.

## Deliverables

- `reconstructed-project/`: self-contained static reconstruction package.
- `docs/reverse-engineering-technical-analysis.md`: reproducible analysis guide.
- `docs/modern-mobile-technical-design.md`: Android/iOS rewrite design only.
- `docs/reconstruction-completeness.md`: requirement-by-requirement evidence and gaps.
- `docs/symbol-map.md` and `docs/resource-formats.md`: semantic and binary-format references.

## Acceptance Criteria

- All 12 classes exist in structured, simple, fallback, and exact bytecode form.
- All 666 methods are inventoried; no method is silently omitted.
- Every one of the 37 JAR entries and 260 indexed pack entries is accounted for.
- All currently understood compressed, textual, image, and audio resources are decoded with hashes.
- Technical analysis can reproduce the extraction from the untouched JAR without running it.
- Mobile design covers framework choice, architecture, content conversion, gameplay systems, testing, performance, provenance, and phased delivery.
- Final audit identifies any residual unknown at byte/field/method granularity.
