---
phase: 1
title: Promote Timeline Semantic Aliases
status: completed
priority: P1
dependencies: []
---

# Phase 1: Promote Timeline Semantic Aliases

## Context Links

- [Plan](./plan.md)
- [Legacy architecture dossier](../../docs/inferred-legacy-game-architecture.md)
- [Canonical alias source](../../scripts/java-me-semantic-aliases.json)

## Overview

Promote the three remaining timeline candidate aliases after confirming exact
descriptors and machine inventory membership.

## Requirements

- Functional: add `stepTimelineScript`, `isTimelineScriptActive`, and
  `findScriptGroupIndex` with `high-confidence`.
- Non-functional: preserve alias schema, uniqueness, normalized hashing, and
  the disclaimer that aliases are not original names.

## Architecture

Canonical JSON remains the source. Inventory regeneration copies the verified
overlay into the generated reconstruction inventory. The broad verifier pins
the intentional count/hash delta.

## Related Code Files

- Modify: `/Users/dan/Desktop/ac-java-game/scripts/java-me-semantic-aliases.json`
- Regenerate: `/Users/dan/Desktop/ac-java-game/reconstructed-project/inventory/semantic-aliases.json`
- Modify: `/Users/dan/Desktop/ac-java-game/scripts/verify-static-reconstruction.py`
- Regenerate: `/Users/dan/Desktop/ac-java-game/reconstructed-project/verification-report.json`

## Implementation Steps

1. Confirm the three method IDs exist in `inventory/methods.json`.
2. Add descriptor-qualified aliases to the canonical source.
3. Regenerate inventory from static `javap` text only.
4. Recompute normalized alias digest and update deliberate verifier pins.

## Success Criteria

- [x] Registry contains 12 class, 42 method, and 41 field aliases.
- [x] Source and generated overlays are byte-for-byte equal as JSON values.
- [x] All aliases and descriptors resolve to inventory IDs.
- [x] Verifier accepts the new normalized digest.

## Risk Assessment

Main risk is stale count/hash documentation. Mitigate by deriving the digest
from normalized JSON and searching all docs for the old method count.

## Security Considerations

Static JSON and disassembly only. No target class loading or external publish.
