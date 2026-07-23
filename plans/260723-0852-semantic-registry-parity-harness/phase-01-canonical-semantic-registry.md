---
phase: 1
title: Canonical Semantic Registry
status: completed
priority: P1
dependencies: []
---

# Phase 1: Canonical Semantic Registry

## Overview

Extend the existing canonical alias overlay rather than creating a competing
registry. Keep exact JVM descriptors, confidence, generated-copy equality, and
normalized hash pinning authoritative.

## Requirements

- Functional: add the evidence-backed method aliases already accepted in the
  architecture dossier, regenerate the managed inventory copy, and update only
  the verifier expectations made intentionally stale by this registry revision.
- Non-functional: exact confidence enum; no duplicate original/alias; no target
  class loading; deterministic JSON output.

## Architecture

`scripts/java-me-semantic-aliases.json` remains the sole source. The bytecode
inventory generator validates IDs and copies it to
`reconstructed-project/inventory/semantic-aliases.json`; the static verifier
checks equality, counts, referential integrity, and normalized SHA-256.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/java-me-semantic-aliases.json`
- Modify: `/Users/dan/Desktop/ac-java-game/scripts/verify-static-reconstruction.py`
- Regenerate: `/Users/dan/Desktop/ac-java-game/reconstructed-project/inventory/semantic-aliases.json`
- Read: `/Users/dan/Desktop/ac-java-game/reconstructed-project/inventory/methods.json`
- Read: `/Users/dan/Desktop/ac-java-game/docs/inferred-legacy-game-architecture.md`

## Implementation Steps

1. Verify every proposed method ID/descriptor exists in `methods.json`.
2. Add exactly eight accepted entity-store and animation/bounds aliases with
   `high-confidence` and no original-name claim; method coverage becomes 39.
3. Regenerate inventory from existing `javap` text only.
4. Update verifier method-alias count/hash from the regenerated canonical bytes.
5. Prove a second regeneration is byte-identical.

## Success Criteria

- [x] Every new method ID exists exactly once and every alias is unique.
- [x] Registry counts are exactly 12 classes, 39 methods, and 41 fields.
- [x] Source/generated registry equality passes.
- [x] Fresh static inventory regeneration is byte-identical.
- [x] No existing alias is silently renamed in this slice.

## Risk Assessment

Wrong descriptors or aliases would poison downstream ports. Mitigation: inventory
referential checks, exact descriptors for overloads, evidence anchors, and the
full verifier. Existing user documentation changes remain untouched until the
registry passes.

## Security Considerations

Registry parsing accepts repository-owned JSON only. No class loading, code
execution, network, or untrusted path expansion.
