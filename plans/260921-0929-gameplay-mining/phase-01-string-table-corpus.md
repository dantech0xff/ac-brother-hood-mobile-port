---
title: String-Table Corpus
phase: 1
status: completed
---

# Phase 1 — String-Table Corpus

Mine `decoded/pack-14` (9 tables, 265 strings) into a structured corpus doc.

## Deliverable

`docs/gameplay-mining/string-corpus.md` (Vietnamese annotations, English
string content verbatim): per-table string index → text → inferred consumer
(screen/dialogue/level briefing), cross-referenced against `k`/`f` string
load call sites where bytecode proves the index.

## Method

- Dump every table with stable indexes.
- Classify: UI chrome, level briefings (`LOCATION:/DATE:/OBJECTIVE:`),
  dialogue, tutorial, cheat, error, IGP promo.
- Map briefing tables to level packs (8 level packs ↔ 8 briefing tables).
