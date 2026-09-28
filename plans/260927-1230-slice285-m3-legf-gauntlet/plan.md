---
title: "Slice 285 — mission-3 capstone leg F: street gauntlet → x11800 wall → y820 shelf"
phase: capstone-bot-legs
status: done
---

# Slice 285 — mission-3 capstone leg F

## Goal

Prove the street-level leg from leg E's landing (10969,839) east across
the sealed door-gauntlet corridor, past the two patrolling ax11s, and up
the x11800-12000 wall mass to the y820 shelf at x12100+.

## Route decode (aclv)

- The y840 "street" row is **solid** across x11080-11779 — the y1197
  door-gauntlet corridor below is a sealed sub-route with oneway cells
  capping both ends (x10960-11060 west, x11640-11780 east). The designed
  path stays on the street.
- ax11 patrols: aw833 @(11297,838) and aw834 @(11492,835), Z[11]=-160.
- The street dead-ends at the wall mass: west face x11800-12000 rising
  y840→y680 (top at x12000). The wisp trail (aw501@11865,717 →
  aw785@12101,582) marks the designed diagonal climb.
- East of the crest the mass steps down to the y820 shelf (x12100+,
  solid top y820 through x12499).

## Leg (verified)

Start checkpoint-style at leg E's end (10969,839). Bot: RIGHT held;
RIGHT+UP from x11600 so the vault/climb arms on the wall face. jc21
dismiss + jc12/13 reload loops as usual. Passed — the two guards did not
stop the run; the wall vault + lip traversal reaches the shelf.

Assert: `p.ak >= 12100 && p.al <= 830 && p.aZ`.

## Notes

- No production-code changes this slice — the leg passes on existing
  mechanics (wall run/lip-grab family from slices 137/141/150).
- Next leg (G): y820 shelf → x12500-12699 y540 wall → the x12700 gap
  (deep pit, mid platforms y1080-1120) → x13100-13299 high mass →
  x13400 y340 → drop toward win fuse aw780 @(13628,1045).
