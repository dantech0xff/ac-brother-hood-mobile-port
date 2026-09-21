---
title: Gameplay Mining — Semantic Extraction for the Rewrite
description: >-
  Mine the already-decompiled JAR artifacts for gameplay semantics: the full
  string-table script, per-level entity atlas, player FSM mechanics, entity
  type catalog, and audio/sprite usage — synthesised into a GDD for the
  LibGDX rewrite. Static-only: the JAR is never executed.
status: completed
priority: P1
effort: large
branch: devin/1789982947-gameplay-mining
tags:
  - reverse-engineering
  - documentation
  - rewrite
blockedBy: []
blocks: []
created: '2026-09-21'
createdBy: 'devin'
source: user
---

# Gameplay Mining — Semantic Extraction for the Rewrite

## Overview

The static reconstruction already achieves full artifact coverage (12/12
classes, 666/666 methods, 260/260 pack slots). What remains for the rebuild
track is *semantic* mining: turning decompiled code and decoded data into
documented gameplay mechanics. This track extracts, with provenance and
confidence labels:

1. The complete in-game text corpus (mission briefs, dialogue, UI, cheats).
2. A level atlas — which entity types live in each of the 8 level packs, and
   the mission order implied by strings + loader code.
3. Player mechanics — the `g.e()` FSM, movement/combat/parkour constants.
4. An entity-type catalog — every `ax` dispatch target and its behaviour.
5. Audio slot usage and sprite/animation usage tables.

The output feeds `docs/` (Vietnamese, confidence-labelled) and a gameplay
design document that becomes the rewrite's content contract.

## Constraints

- Static-only: never execute the JAR, MIDlet, or an emulator of the legacy
  game. All claims come from bytecode/decompiled source/decoded data.
- Every mined fact carries a confidence label (`proven`, `high-confidence`,
  `inferred`, `unknown`) and a source reference (file + line/offset).
- Numeric constants are quoted verbatim from bytecode; no invented values.
- `scripts/verify-static-reconstruction.py` must stay green.

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [String-table corpus](./phase-01-string-table-corpus.md) | Completed |
| 2 | [Level atlas](./phase-02-level-atlas.md) | Completed |
| 3 | [Player mechanics](./phase-03-player-mechanics.md) | Completed |
| 4 | [Entity-type catalog](./phase-04-entity-type-catalog.md) | Completed |
| 5 | [Audio and sprite usage](./phase-05-audio-sprite-usage.md) | Completed |
| 6 | [GDD synthesis + PR](./phase-06-gdd-synthesis.md) | Completed |

## Exit criteria

- `docs/gameplay-mining/` (or equivalent) contains the corpus, atlas, catalog,
  and mechanics documents with provenance.
- `docs/gameplay-design-document.md` summarises systems, missions, entities,
  and content for the rewrite.
- A PR merges the mined documentation; the verifier report stays `ok: true`.
