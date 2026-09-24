---
title: "Slice 176 — all-mission pack conversion (ec[aj] / ej / bh gating)"
phase: port-slice-176
status: done
---

# Slice 176 — all 8 mission packs converted + loadable

## Goal (why this slice)

Until now only `level0/` existed — the original ships 8 missions in packs
`ec[aj]="/6"…"/13"` (k.java:260, proven: mission aj loads pack 6+aj). This
slice converts every mission pack into the same ACLV/ACPK artifacts and
makes `Level0World` mission-aware (`k.aj` ctor param), so missions 1–7 are
loadable content instead of missing data.

## Proven layout mined from k.java

- `ec[aj]` = `"/6"…"/13"` (k.java:260) — mission aj reads pack 6+aj.
- `ej[]` 32-byte table (k.java:275) = 8 missions × 4 tileset clips
  {ep, eu, er, unused-slot}: rows are `{10,11,12,13}`, `{3,4,255,x}`,
  `{5,6,7,x}`, `{5,6,7,x}`, `{3,4,255,x}`, `{0,1,2,x}`, `{8,9,10,x}`,
  `{10,11,12,x}`.
- `bh[]={4,3,4,4,3,4,4,4,4}` (k.java:263): `bh[aj]==3` on aj∈{1,4} — the
  flying/autoscroll missions; it gates `I(i)` (k.java:5244-5264) so those
  packs carry no ep layer (entries 4/5/6 absent on disk, verified).
- Fixed entry indices per pack: records=0, et=1/2/3, ep=4/5/6, scripts=7,
  eu=8/9/10, er=11/12/13 (same layout as pack-6, proven by dims/cells/flags
  triads on disk).
- Strings: `k.d(1+aj, idx)` → pack-14 `entry-(aj+1)-strings.json`.

## Changes

- `tools/convert_slice1.py`: `pack_level(aj)` parameterized — per-mission
  pack dir, tilesets from `EJ[aj*4..+2]`, visual layers gated by `BH[aj]`
  (flying → `[eu, er]`), strings from `entry-{aj+1}`, emits
  `generated/level<aj>/` with `level<aj>.aclv`, `strings-{aj+1}.{json,txt}`,
  `meta.json` (now carries `aj`/`bh`/`flying`), `scripts.bin`, and
  per-level `tileset-<n>` dirs.
- Layer ids are the FIXED entry space `{et:0, ep:1, eu:2, er:3}` — flying
  packs leave a hole at id 1 (regression test found the positional-id bug:
  eu had been emitted as id 1).
- `pack_clip(..., remap=False)` for tilesets: `clip_remaps` tables are
  entity-clip-only (clip-0/52); tileset clip ids collide numerically and
  were wrongly asserted against clip-0's object space.
- `Level0World`: new `aj` ctor param (default 0); `missionIndex` and
  `kAj` now return it, so `bh3`, `k.d(1+aj)`, and the `aj!=7` stat-tally
  gates engage per mission.
- Tests: `Slice176Test` — all 8 packs decode to the mined dims/entity
  counts, spawn records, tick 120 frames without exceptions, `bh3` follows
  aj, and layer-id sets match the `bh` table ({0,2,3} flying /
  {0,1,2,3} grounded).

## Converted packs

| aj | pack | dims      | entities | layers        | tilesets |
|----|------|-----------|----------|---------------|----------|
| 0  | 6    | 627×55    | 637      | et,ep,eu,er   | 10,11,12 |
| 1  | 7    | 44×600    | 225      | et,eu,er FLY  | 3,4      |
| 2  | 8    | 275×110   | 567      | et,ep,eu,er   | 5,6,7    |
| 3  | 9    | 775×70    | 691      | et,ep,eu,er   | 5,6,7    |
| 4  | 10   | 44×625    | 254      | et,eu,er FLY  | 3,4      |
| 5  | 11   | 786×63    | 740      | et,ep,eu,er   | 0,1,2    |
| 6  | 12   | 550×70    | 849      | et,ep,eu,er   | 8,9,10   |
| 7  | 13   | 100×102   | 323      | et,ep,eu,er   | 10,11,12 |

## Known gaps (next slices)

- Flying-mode collision (`dL` stamp grid, k.java:4405) and `g()` remap
  are not ported — flying packs load/spawn/tick but use ground-rule
  collision; entities may cull. Marked in test comments.
- Mission-switch runtime wiring (menu → k.aj → pack provider,
  Level0Game still hardcodes `level0`) is the follow-up slice.
- Tileset clip packs are emitted per-level (some ids shared across
  missions — e.g. {10,11,12} for aj 0/7); disk-cheap duplication kept for
  the uniform `level<aj>/tileset-<n>` convention.

## Gates

- verifier `ok:true` · unittest 57 pass · :core:test 1135 (4 new pass)
- :android:assembleDebug + :gdx:build green
- Confirmed on device earlier that `generated/` is the shared assets
  root — level<aj>/ ships to Android+LWJGL3 automatically.
