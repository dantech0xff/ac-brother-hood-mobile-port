---
title: "Slice 390 — per-record clip tables, the pack-3 decor/ejection clips, and the app's clip loader"
phase: "port"
status: "done"
slice: 390
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (i(short[]) @270-470 — the clip binding)
  - reconstructed-project/bytecode/k.javap.txt (static{} 30496 — bi/bj/bk/bl/bm/bn)
  - reconstructed-project/resources/sprites-decoded/pack-3 (entries 24/34/37/41/65/66/67/69/72)
---

# Slice 390

Found by the slice-389 constructor differential (`aa` was the only field
class left once the arms were fixed).

## The binding (proven)

`i(short[])` selects the clip **per record**, before the retype and the
`switch`: ax67 → `k.bk[r8[7]]`, ax46 → `k.bl[r8[10]]`, ax7 → `k.bm[r8[8]]`,
ax56 → `k.bj[r8[7]]`, ax9 → `k.bn[r8[8]]`, every other type → `k.bi[ax]` when
it is not −1; `aa = k.r(n)`, which is null for an entry pack 3 lacks. The
tables (evaluated from `k`'s `static {}`):

```
bi = [0,-1,1,2,3,1,4,60,5,47,6,7,8,61,9,25,10,7,-1,11,-1,13,14,7,40,16,15,48,-1,52,
      36,44,36,-1,42,62,-1,-1,-1,-1,45,30,-1,31,32,33,29,7,13,-1,7,28,-1,-1,19,-1,19,
      -1,20,-1,21,71,-1,-1,22,-1,23,-1,26,38,43,-1,51,7,54,55,56,-1,63,0,57]
bj = [19,68]  bk = [24,27,27,27,34,35,37,41,64,64,65,67,49,69,70]
bl = [29,0]   bm = [60,66]  bn = [47,72]
```

## What was wrong

1. **One clip per type.** `ENTITY_CLIP` mapped ax7 → 60 and ax9 → 47 for every
   record, but `r8[8] == 1` selects 66 / 72. The level-0 ejection mouths
   aw=12 (1397,506) and aw=30 (4801,674) are ax7 `r8[8]==1`; the clip-60
   frame-0 rect sits 80 px west of the anchor, which is where the slice-215
   "verbatim wedge / original softlock" verdict and the slice-252 "wedge box
   `[1318,456..1334,472]`" came from. On clip 66 the capture box is centred
   on the mouth (`[1392,472,1408,488]`) and the throw is clean: swallow at
   (1400,480), release east at ~(1529,500) 8 ticks later, landing (1569,579)
   beyond the x1400 wall. **Slice 215's verdict is superseded** (errata added
   there); `Slice215Test` now pins the real chain.
2. **Pack-3 entries never converted.** `convert_slice1.py` keeps an explicit
   `CLIPS` list; 24/34/37/41/65/66/67/69/72 were missing, so 230 ax67 decor
   records (kinds 0/4/6/7/10/11/13 — 128 scaffolds/props of clip 34 in
   mission 5 alone), the two mouths and the two ax9 pushers spawned clipless:
   invisible, no hitbox. (`bk[12] = 49`, `bk[14] = 70` and `bj[1] = 68` name
   entries pack 3 does not have — null in the original too; the 360 kind-14
   decor volumes of mission 6 are legitimately clipless.)
3. **The app never loaded seven converted clips.** `Level0Game.create()` is a
   hand-written list; the unit tests' `world()` is another. Clips
   4 (ax6), 19 (the ax54/ax56 flyers), 27 (springboards), 35 (decor
   interactives, 249 records), 36 (ax30/ax32 runners), 40 (every
   `a(24, 40, …)` projectile / floatie / burst) and 71 (ax61) were in the
   test map but not in the app, so on a device those entities were invisible
   and box-less while every test passed. They load now.
4. `ENTITY_CLIP[71] = 26` was a phantom (`bi[71] = -1`).

## Change

- `rewrite/tools/convert_slice1.py`: nine entries added; the run is
  deterministic (only the nine new `generated/clips/clipNN/` directories
  appear, 1.1 MB).
- `Level0World`: the verbatim tables + `entityClipIndex(ax, record)`;
  `ENTITY_CLIP` is derived from `bi`; `spawnEntities` binds per record.
- `Level0Game.create()`: loads 4, 19, 24, 27, 34-37, 40, 41, 65-67, 69, 71, 72.
- Tests: `Slice390Test` (3: tables, every shipped record, the mouths),
  `ClipLoadParityTest` (2: parses `Level0Game.kt` and checks it against
  `generated/clips/` and against every clip a shipped record can bind),
  `Slice1Test.world()` carries the nine new clips. Mutation-checked
  (loader list, each table lookup).

## Open

- The renderer has not drawn the nine new clips on a device (rendering is
  generic over the clip map; module PNGs are palette-00 + the eo[] alpha
  masks the converter already carries for 44/46/49/50/54/57/58/69).
- Pack-3 entries 18/50/58 exist but no shipped record binds them.
