---
phase: 3
title: Content Conversion Slice
status: completed
priority: P1
dependencies: [1]
---

# Phase 3: Content Conversion Slice

## Context Links

- [Plan](./plan.md)
- Canonical sprite inventory:
  `reconstructed-project/resources/sprites-decoded/summary.json`
- Audio pack: `reconstructed-project/resources/decoded/pack-17/`
- Provenance rules: `docs/code-standards.md`

## Overview

Add `rewrite/tools/convert_spike_assets.py`: an offline converter that copies
the chosen decoded assets into `rewrite/generated/` and emits
`provenance.json` recording for each output: source relative path, source
SHA-256, output SHA-256, transform name, and transform version.

## Assets chosen for the spike

| Output | Source | Role |
|---|---|---|
| `splash.png` | `sprites-decoded/pack-2/entry-000-marker-003/module-0002-palette-00-232x129.png` | Recovered title art; blit target in the 400x240 scene |
| `actor.png` | `sprites-decoded/pack-2/entry-000-marker-003/module-0000-palette-00-83x155.png` | Recovered character art; the moving entity visual |
| `sfx.wav` | `decoded/pack-17/entry-020-marker-000.wav` | Converted SFX played on touch |

The runtime reads only `rewrite/generated/` outputs plus `provenance.json`;
it never opens the JAR or intermediate decode trees.

## Requirements

- Pure-Python converter, deterministic output ordering, no network.
- Manifest schema versioned (`provenance_version: 1`).
- Byte-for-byte copies for the spike (transform `copy-v1`); resampling or
  atlas packing belong to later slices and must bump the transform version.

## Validation

Re-running the converter reproduces identical file hashes and manifest.
