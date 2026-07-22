---
phase: 2
title: Semantic Source Reconstruction
status: completed
effort: ''
priority: P1
dependencies:
  - 1
---

# Phase 2: Semantic Source Reconstruction

## Overview

Package every class in three static views and recover semantic architecture, names, call relationships, state machines, and behavioral contracts.

## Requirements

- Functional: preserve every method and provide evidence-backed semantic mappings.
- Non-functional: original obfuscated identifiers remain traceable; uncertain renames carry confidence labels.

## Architecture

`structured/` favors readability, `fallback/` favors complete control flow, and `bytecode/` preserves exact JVM instructions. `docs/symbol-map.md` connects obfuscated and semantic identities.

## Related Code Files

- Create: `reconstructed-project/src/structured/*.java`
- Create: `reconstructed-project/src/fallback/*.java`
- Create: `reconstructed-project/bytecode/*.javap.txt`
- Create: `reconstructed-project/reconstruction-manifest.json`
- Create: `docs/symbol-map.md`

## Implementation Steps

1. Copy normalized structured and fallback sources into the reconstruction package.
2. Generate exact disassembly for all 12 classes using `javap` only.
3. Inventory class/member descriptors and call dependencies.
4. Recover lifecycle, engine, renderer, resource, actor, AI, audio, IGP, save, UI, and input semantics.
5. Document the `i.aV()` structured failure and its complete fallback/bytecode representation.

## Success Criteria

- [x] 12/12 classes present in all three views.
- [x] 666/666 methods represented in inventory and bytecode.
- [x] No structured stub is presented as recovered source without fallback linkage.
- [x] Major classes and public behavioral boundaries have evidence-backed names.

## Risk Assessment

Renaming all 1,009 fields without runtime evidence can create false certainty. Preserve original names and publish semantic aliases with confidence instead of destructive source rewrites.
