---
title: Slice 53 — ax80 static prop (init-only, null clip)
phase: port
status: done
---

# Slice 53 — ax80

## What the original does

- Init dispatch `case 80 → L389` (i.java:2734 → body :3636, proven):
  `az = r8[7]` + the shared init tail (`setAnim(r8[5])`, box refresh) —
  byte-identical shape to ax7/ax78's L153/L384 tails.
- Tick dispatch: `default → L897` (i.java:4986-4987, proven) — ax80 has
  no case; it is a **static prop that never ticks**.
- Clip: `bi[80] = 57` (k.java:8442, proven) → `k.J(57)` =
  `z[57] = new b().a(j.e(57), 0)` — but decoded pack-3 has **no
  entry-057** (`metadata.json` marks the slot `"empty entry"`; same gap
  as entries 053/055/056). `j.e(57)` fails → `z[57] = null` → the
  entity's `aa` clip is null → **ax80 renders nothing** in the original.

  → Faithful port = init-only entity with `clip == null`; there is no
  asset to convert.

- Records: only 2, both in pack-12 (`records.json`): aw=585 (7537,968)
  and aw=595 (7534,1016), both `S=4, az=300` — vestigial props beside
  the flying-machine level, likely a scrapped detail.

## Port

- `NpcFsm.initAx80(e, f, w)` — verbatim L389: `az = rf(7)`, `setAnim(rf(5))`,
  `refreshBoxes()`.
- `Level0World` init dispatch `type == 80 → initAx80`; `ENTITY_CLIP`
  `80 to 57` with a comment documenting the null-clip resolution
  (`w.clips[57]` → null).
- No `tickAx80` — dispatch has no ax80 arm, matching `default → L897`.
- No clip conversion — entry-057 does not exist.

## Gates

- `Slice53Test` (2 tests): az/S verbatim + null clip; never ticks /
  never moved over 20 world ticks.
- verifier `ok:true`; `python3 -m unittest discover -s tests` green;
  `:core:test` green.

## Confidence

- proven: init semantics, never-ticks, bi[80]=57, null clip.
