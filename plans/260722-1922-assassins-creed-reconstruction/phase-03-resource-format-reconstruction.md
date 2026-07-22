---
phase: 3
title: Resource Format Reconstruction
status: completed
effort: ''
priority: P1
dependencies:
  - 1
---

# Phase 3: Resource Format Reconstruction

## Overview

Deepen the static decoder and document the pack, typed object, string, sprite, audio, IGP, level, catalog, and save formats.

## Requirements

- Functional: account for every JAR and indexed pack entry; decode every supported format.
- Non-functional: deterministic outputs with per-entry hashes and explicit unknown-field notes.

## Architecture

The extractor reads ZIP entries directly, validates split-pack offsets, decodes LZMA-alone payloads, classifies content, and emits metadata beside immutable decoded bytes.

## Related Code Files

- Modify: `scripts/extract-java-me-resource-packs.py`
- Create: `reconstructed-project/resources/**`
- Create: `docs/resource-formats.md`
- Create: `reconstructed-project/reconstruction-manifest.json`

## Implementation Steps

1. Formalize binary structures with offsets, widths, endianness, validation, and confidence.
2. Add typed-object and font-map decoding where statically proven.
3. Extend sprite metadata parsing and document palette/RLE pixel codecs.
4. Map the 14-entry level bundles from `k` load sites and actor constructors.
5. Copy decoded text/audio/PNG/resource metadata into the reconstruction package.

## Success Criteria

- [x] 37/37 archive entries and 260/260 pack entries inventoried.
- [x] 24/24 LZMA entries decode without error.
- [x] 265 strings, 31 audio files, and 29 IGP PNGs retained with hashes.
- [x] Remaining custom binaries have pack/index, consumer call site, and confidence classification.

## Risk Assessment

Sprite and level formats are intertwined with large obfuscated parsers. Do not guess field names; retain raw data and document parse boundaries for future refinement.
