---
title: "Port slice 17 — clip palette variants (b.l) + ax11 red/slate uniforms"
phase: port
status: done
base: devin/1790044358-port-slice16-checkpoints
---

# Slice 17 — palette variants

Soldiers on the Roman rooftops are not all the same uniform. The original
keeps three palette tables per clip-7 module and swaps them per entity
via `b.l(int)` (the `aH` palette slot). This slice ports the slot +
variant rendering end-to-end.

## Mechanism (proven)

- `b.l(int slot)` (b.java:2436) sets the entity's palette slot `aH`; the
  draw path resolves module images through `aP[aH][module]` (b.java:1463).
- Per-type palette arms, entity draw dispatch (i.java:4180–4194):
  `ax11: Z[0]∈{1,2} → l(1), else l(0)`; ax30/32 flicker l(0)/l(1) on
  `cG` (hit-flash via palette swap); ax17/47/73 → l(0); ax79 → l(Z[0]);
  ax68 → l(af.ax==30 ? 1 : 0); player ax0/9/4/67 → `l(bo[bL][0])`.
- `bo` table (k.java:8450): `{{0,-1},{3,1},{5,2},{6,3}}` = {palette,
  substitution-table `aA`} per scene-variant `i.bL`; level 0 → {0,-1}
  = palette 0, no substitution.
- Level-0 record analysis (this level's real data): ax11 `Z[0]` (= record
  field 10 via `initSoldier`/`aa.w`) histogram = `{0:44, 1:9}` — nine
  soldiers draw the palette-1 uniform (red-trimmed vs slate-grey base).

## Port

- `Entity.palette` = `aH`; `spawnEntities()` sets `palette=1` for ax11
  with `Z[0]∈{1,2}` (proven arm), everyone else 0.
- `convert_slice1.py` now exports every `module-*-palette-NN-*.png`
  (clip7: 996 PNGs = 332 modules × 3 variants; clip0 + tilesets too);
  `clip.acpk` keeps palette-00 names as canonical.
- `Level0Renderer` resolves palette variants lazily by filename
  substitution (`-palette-00-` → `-palette-NN-`), falling back to
  palette-00 when a variant file is absent.

## Labels

- proven: the l(1)/l(0) arm, the bo table, slot→`aP[aH][module]` resolution.
- inferred: palette-NN filename suffix = the `aH` index (decoder names
  variants by palette-table index; visual check on emulator confirms
  the 9 slot-1 soldiers now draw the red uniform).

## Test

- `ax11 palette slot follows proven Z0 arm i4180` — 9 slot-1 / 44 slot-0,
  player 0. 47 core tests green; verifier ok:true; 57 unittests OK.
