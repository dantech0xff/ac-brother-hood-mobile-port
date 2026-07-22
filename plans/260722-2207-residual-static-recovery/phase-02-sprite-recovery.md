---
phase: 2
title: "Sprite Recovery"
status: completed
effort: ""
priority: P1
dependencies: [1]
---

# Phase 2: Sprite Recovery

## Overview

Extend the static sprite decoder with the data-derived `0x27f1`
literal/7-bit-run grammar and classify the optional no-payload module by its
actual runtime no-image behavior.

## Requirements

- Functional: recover every derivable pixel without synthesizing missing data.
- Non-functional: bounded parsing, palette validation, exact payload
  consumption, deterministic PNGs, staged publication.

## Architecture

Keep runtime-derived branches and corpus-derived branches distinct in metadata.
The new branch emits one index for controls below `0x80`; otherwise it repeats
the following palette index `control & 0x7f` times.

## Related Code Files

- Modify: `scripts/decode-gameloft-sprites.py`
- Modify: `scripts/verify-static-reconstruction.py`
- Regenerate: `reconstructed-project/resources/sprites-decoded/`
- Update: sprite/resource/completeness documentation.

## Implementation Steps

1. Add branch evidence metadata and bounded `0x27f1` index decoding.
2. Require positive run length, in-range palette indexes, exact pixel count, and
   zero trailing payload bytes for the two observed modules.
3. Reclassify optional EOF-without-palette only if bytecode proves no image is
   constructed and all structure reaches EOF.
4. Regenerate PNGs twice and pin the new managed manifest.
5. Inspect representative PNGs and update residual-risk language.

## Success Criteria

- [x] Both `0x27f1` modules decode to 2,739 and 1,909 pixels respectively.
- [x] Four new palette PNG variants pass CRC/zlib validation.
- [x] Sprite rerun manifest is byte-for-byte deterministic.
- [x] Runtime absence of a `0x27f1` branch remains documented.

## Risk Assessment

Data grammar can recover resources but cannot rewrite historical runtime
behavior. Metadata and docs must retain that distinction.
