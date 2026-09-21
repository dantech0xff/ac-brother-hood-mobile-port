---
title: Audio and Sprite Usage
phase: 5
status: completed
---

# Phase 5 — Audio and Sprite Usage

## Deliverable

`docs/gameplay-mining/audio-sprite-usage.md`: 34 audio slots → call sites /
screen context (music slots 0–9 vs SFX 10–33 per `k.bE`/`k.bF` gating);
sprite pack → entity/animation mapping for the main cast.

## Method

- `e.a(IZ)V` call sites in `i`/`k`/`g`; pack-17 slot list; `b`/`a` frame
  indices referenced by entity state tables.
