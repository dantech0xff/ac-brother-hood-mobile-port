---
phase: 4
title: Integration and Audit
status: completed
effort: ''
priority: P1
dependencies:
  - 2
  - 3
---

# Phase 4: Integration and Audit

## Overview

Integrate new artifacts, expand only proven semantic aliases, update every
affected document/count/hash, and independently audit the static package.

## Requirements

- Functional: verifier regenerates and compares sprite and level outputs.
- Non-functional: all links/JSON/modes valid; no stale counts; no lock/stage;
  `game_execution_performed=false` remains machine-readable.

## Related Code Files

- Modify: `scripts/java-me-semantic-aliases.json`
- Modify: `scripts/inventory-java-me-bytecode.py` only if schema needs change.
- Modify: `scripts/verify-static-reconstruction.py`
- Update: `README.md`, `reconstructed-project/README.md`, `docs/*.md`, reports.

## Implementation Steps

1. Add evidence-backed audio/resource/save/world method and field aliases.
2. Regenerate inventory, sprite, and level managed outputs.
3. Run `py_compile`, deterministic reruns, fresh extraction/inventory checks,
   link/JSON/path/symlink checks, and the full static verifier.
4. Run independent code/data audit; resolve all P0-P2 findings.
5. Record exact gains and remaining irreducible unknowns.

## Success Criteria

- [x] Full verifier returns `ok: true`, `failures: []`.
- [x] Independent review finds no remaining P0/P1/P2 defect.
- [x] Documentation and machine outputs agree on every count/hash/status.
- [x] JAR/MIDlet/classes were never executed.

## Risk Assessment

Generated-tree and hard-coded verifier drift can create a false pass. Fresh
regeneration and exact tree comparison are required before handoff.
