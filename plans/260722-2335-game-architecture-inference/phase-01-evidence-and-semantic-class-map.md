---
phase: 1
title: Evidence and Semantic Class Map
status: completed
priority: P1
dependencies: []
---

# Phase 1: Evidence and Semantic Class Map

## Overview

Inventory evidence for the 12 classes and produce a conservative semantic map
at class, subsystem, and selected method level.

## Requirements

- Functional: map ownership, dependencies, lifecycle interfaces, and candidate
  aliases without changing decompiled sources.
- Non-functional: cite bytecode/inventory/source paths; use the project's
  `proven`, `high-confidence`, `inferred`, `unknown` vocabulary.

## Architecture

Triangulate each claim across at least two evidence forms where possible:
bytecode inventory, decompiled control flow, resource consumer, or existing
format proof. Treat existing docs as leads, not independent proof.

## Related Code Files

- Read: `reconstructed-project/src/{structured,simple,fallback}/`
- Read: `reconstructed-project/{bytecode,inventory}/`
- Create: `reports/lifecycle-engine-scout.md`
- Create: `reports/gameplay-ai-scout.md`
- Create: `reports/content-persistence-scout.md`

## Implementation Steps

1. Partition lifecycle/engine, gameplay/entity, and content/platform evidence.
2. Extract interfaces, inheritance, call concentration, field ownership, and
   resource consumers.
3. Propose semantic aliases with rationale and explicit counter-evidence.
4. Record unresolved symbols and the next best static proof target.

## Success Criteria

- [x] All 12 classes mapped.
- [x] Reports cite concrete class/method/field/resource evidence.
- [x] Original-name claims remain explicitly disallowed.

## Risk Assessment

Obfuscation and god classes can make responsibility look cleaner than it was.
Mitigation: model capabilities and call edges first, alias second.
