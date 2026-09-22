---
title: "Port slice 19 — ax44 doors/gates (bv() i16927) + clip32"
phase: port-rewrite
status: done
---

# Slice 19 — ax44 doors/gates + crush

## Mined semantics (proven)

`i.bv()` (`simple/i.java:16927`), invoked from `c(true)` when `ax == 44`.

- **Init (L377):** `az = r8[7]`; `Z = {r8[8]>0?r8[8]:0, r8[9], r8[10], 0,
  r8[5], r8[11]}`. Initial `S` comes from the generic arm `i(r8[5])`.
- **S dispatch:** `{0,2,4,6}` → L4 tick arm: `P|=16` (per-frame flag),
  link resolve `ac = k.q(Z[5])`, `ac.ax==58 && ac.S∈{0,5,7}` promotes
  `Z[0]=1`; then the `Z[0]` switch. `{1,5}` → `r()→i(S+1)`, reload
  `Z[3]=Z[1]`. `{3,7}` → `r()→i(S-3)`, reload `Z[3]=Z[2]`, then
  fallthrough into the crush check. `{8..13}` → crush only.
- **Z[0] modes:** `0` = timed auto-cycle (when `ac==null || ac.bf()`:
  `Z[2]<999 && Z[3]!=-1` → decrement; `<0 → i(S+1)`). `1` = slaved to
  link entity (`Z[4]==S ? (!bf→i(S+1)) : (bf→i(Z[4]))`). `2` = proximity
  (`Z[4]==S`: `Z[3]==0 && player∩W → reload Z[3]=(S∈{0,4}?Z[2]:Z[1])`;
  `Z[3]>0` → decrement; `≤0 → i(S+1)`).
- **Crush arm** (L64 + L75): player `W ∩ door W` → `k.aS.aj=0; ah=0;
  i(50)`. Player S50 (`g.java:2630` L447) → `d(999)` → `r()→k.l(12)`
  mission fail.
- **`bf()`** (`i.java:14608`): `ax==58 && S∈{1,3,4,6,8,10,12}`.

## Level-0 data (parse of `level0.aclv`)

61 ax44 records, all ≥12 fields. `f8=-1` → every door runs mode `Z[0]=0`
(timed). `f9=f10=0` → timers. **`f5` splits into two banks:** 44 records
`f5=0` (S0..7 cycling bank) and 17 records `f5=8` (S8..13 static crusher
bank — e.g. the trio at (2383–2479, 657)). `f11=-1` → no links used.

## Ported

- `NpcFsm.initDoor` / `tickDoor` / `bf` / `crush` — verbatim arm
  structure; `tickDoor` calls `refreshBoxes()` each tick (original `t()`
  recomputes `W` on every query; our cached copy needs the refresh since
  doors never hit the cell-probe paths — caught by the crush test).
- `Entity.ac` field (the `k.q(Z[5])` link target).
- `Level0World`: clip table `44→32`, spawn `initDoor`, tick dispatch
  `ax==44→tickDoor`, checkpoint re-materialize uses `Z[4]` for doors.
- Clip 32 (`entry-032-marker-003`, `k.bi[44]=32`): converted by
  `convert_slice1.py` (26 anims, 81 frames, 11 modules, 13 rects);
  renderer module base-dir `32 → clips/clip32/modules`.
- Tests (2 new): spawn bank split + timed-cycle walk through S0→1→2→3
  while S8 stays 8; closed crusher overlaps the player → `i(50)` →
  `k.l(12)` mission fail on the next tick.

## Verified

- `:core:test` — all green (2 new door tests + 44 existing).
- `verify-static-reconstruction.py` → `ok:true`; `unittest` 57/57.
- Emulator: debug-teleport to (2400,640) → crusher bank renders at
  (2383–2479, 657) — see `reports/slice19-doors-emulator.png` (S8 doors
  appear as the blue crate sprite — clip32 module, unpaletted like the
  soldiers' pre-slice-17 look).

## Honest gaps

- ax44 record fields `f8` mode/slave paths (Z[0]=1/2) are ported but
  unreachable in level 0 (all records are mode 0).
- Doors render with clip32's own palette; no palette-variant mapping
  exists for this clip (slice-17 mapping was ax11-specific).
