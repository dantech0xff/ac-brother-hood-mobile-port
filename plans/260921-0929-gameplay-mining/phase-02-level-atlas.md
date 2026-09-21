---
title: Level Atlas
phase: 2
status: completed
---

# Phase 2 — Level Atlas

`levels-decoded/pack-6..pack-13/records.json` hold 4,286 entity records +
3,705 script instructions. Build a per-level atlas.

## Deliverable

`docs/gameplay-mining/level-atlas.md`: per pack — record counts, entity-type
(`ax`) histogram, trigger/waypoint/decor split, script-instruction stats,
map dimensions where the format exposes them; plus the mission order derived
from strings + `k` loader indexing.

## Method

- JSON aggregation script (throwaway or `scripts/` if reusable) over records.
- Cross-check entity-type values against `i.I()` dispatch (Phase 4 input).
