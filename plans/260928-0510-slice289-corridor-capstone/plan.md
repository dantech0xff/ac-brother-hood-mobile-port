---
title: "slice 289 — mission-0 corridor crossing + mission-5 capstone legs"
phase: capstone-frontier
status: merged
slice: 289
---

# Slice 289 — m0 corridor fix + m5 capstone legs

## Scope

Two tracks landed together on the same working tree:

1. **m0 capstone corridor arm rewrite** (`Slice245Test`, world aj=0):
   the 11-crusher corridor x3880-4259 crossed via the probe-proven
   designed chain — S257 vault-drop TELEPORTS under the x3960-4039
   divider east edge into the slot (~x4101), then a held-UP jump climbs
   THROUGH '5' (one-way cell: solid from above via aR=5, climbable from
   below) back to its top, arcs east to the '20' plateau, never touching
   the door kill slivers y748-774.
   Result: deaths 301 → 2, maxAk ~3994 → 9850.

2. **mission-5 capstone legs A-I** (`Slice289Test`, world aj=5):
   Venice chase level legs on real input only — spawn pit staircase,
   plaza tower, dive verdict, carrier chains — all legs green.

## Proven fixes kept (production)

- `NpcFsm` S23 windup: `tail[2] = true` (z7 = weakened-guard damage
  window, i.java:4651).
- `NpcFsm` attackScheduler73: leap-condition rewrite
  (i.java:25336-25391 — leap when marker reached / player out of reach
  / ceiling blocks backing direction / floor ends; the previous shape
  was inverted).
- `Level0World`: `I()` L1f35 shared tail for the player slot
  (i.java:18904-18922 — `b=true; t(); av→P&1` — the player was the only
  entity left stale; shrank catch/mount windows).
- `PlayerFsm`: L1d21-L1e37 airborne launch block (g.java:4436-4565);
  S215 wall-grab face INVERSION vs the normal path (g.java:4578-4630);
  ax11 drop-behind direction fix (g.java:5570-5578); Z[0]==1-only
  height gate (g.java:5673-5680).
- `NpcFsm`: DOOROV debug print removed.

## New frontier (next slice)

x8900 re-drive: after deaths on the far-tower descent, checkpoint
ax2@(8926,757) respawns at the pit floor; the chimney-zigzag entry
(x8872-8990, S22/36/43) does not re-engage. First-pass crossing reached
maxAk=9850 (past the slice-278 frontier 9212).

## Gates

- `python3 scripts/verify-static-reconstruction.py` → ok:true
- `python3 -m unittest discover -s tests` → 57 pass
- `./gradlew :core:test` → 1518 tests, 1 expected failure (m0 capstone
  tracking assertion `won` — frontier advanced to maxAk=9850)
