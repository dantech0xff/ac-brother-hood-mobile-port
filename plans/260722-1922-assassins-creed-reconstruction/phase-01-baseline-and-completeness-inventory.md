---
phase: 1
title: Baseline and Completeness Inventory
status: completed
effort: ''
priority: P1
dependencies: []
---

# Phase 1: Baseline and Completeness Inventory

## Overview

Freeze the static-analysis baseline and create measurable coverage for classes, methods, JAR entries, pack entries, decompiler failures, and recovered assets.

## Requirements

- Functional: inventory every code and resource artifact with hashes/counts.
- Non-functional: no game execution; all commands reproducible and read-only against the original JAR.

## Architecture

The original JAR is immutable input. Generated evidence flows into `reconstructed-project/`, while reports and format research remain under this plan and evergreen conclusions under `docs/`.

## Related Code Files

- Read: `assassins_creed_-_br_320x240_136711.jar`
- Read: `plans/research/assassins-creed-jar/**`
- Create: `reconstructed-project/reconstruction-manifest.json`
- Create: `docs/reconstruction-completeness.md`

## Implementation Steps

1. Verify JAR hash and ZIP integrity without loading the MIDlet.
2. Count classes, fields, methods, bytecode versions, JAR entries, pack entries, and decoded types.
3. Detect all structured-decompiler stubs/errors and map each to fallback/bytecode evidence.
4. Create a machine-readable manifest and human-readable completeness matrix.

## Success Criteria

- [x] Original artifact hash and integrity recorded.
- [x] All code/resource counts reconcile across tools.
- [x] Every decompiler gap has a fallback path.
- [x] Baseline can be regenerated without executing the game.

## Risk Assessment

Decompiler line counts can look complete while omitting control flow. Mitigation: exact `javap -c -p -s -constants` output is the authoritative fallback.
