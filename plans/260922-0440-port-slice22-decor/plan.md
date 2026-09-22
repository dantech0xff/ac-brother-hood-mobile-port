---
title: "Slice 22 — ax67 decor/props (bB() + full t() box fill + v()/u())"
phase: port-slice
status: done
---

# Slice 22 — ax67 decor/props

Ports the ax67 prop family (`i.bB()`, i.java:17584) verbatim plus the two
shared subsystems it forced into focus: the complete `i.t()` bounding-box
fill (i.java:369) and the real `i.v()/i.u()` on-screen check
(i.java:700–808). Level 0 gains its 253 kind-9 decor records and the
interactive kind-5/springboard arms; npcs on boot goes 150 → 403.

## Mined (all `proven` unless noted)

- **Tick dispatch**: `case 67 → L885 → bB()` (i.java:17584).
- **Clip binding is per-kind**, not `bi[67]`: `aa = k.r(k.bk[r8[7]])`
  (i.java:2633) with `bk = {24,27,27,27,34,35,37,41,64,64,65,67,49,69,70}`
  (k.java:8444). Level-0 records are all kind 9 → clip64.
- **Init** = L347 arm (i.java:3530): `az=f8`, `Z[0]=f7`,
  `bk==37→P|16`, `bk==35&&f5==12→Z[1]=f4`, `bk==35&&f5==28→P|16`,
  `aB=f5==39?2:0` (aB arm i.java:2634), `i(f5)`.
- **`i.p()`** (i.java:214): zero W/X/Y, cascade `ad`, null ae/af.
  **`i.G()`** (i.java:4792): `ae?.p(); ae=null`.
- **`i.a(int[],int[])`** (i.java:632): INCLUSIVE-edge overlap — note this
  differs from the strict `overlap()` used by earlier arms; bB() calls
  `i.a()`, so `Entity.overlapI` is transcribed and used only here.
- **`i.u()`** (i.java:700): `au` is not a decay counter — it is recomputed
  per `v()` call = normalized distance from view center
  (k.O+200, k.P+120): `/400,/120` generic; `/400,/240` when `aG==4` or
  kind49; `/800,/240` for springboards (kind27). ctor `au=10`, `i=1`
  (i.java:819-820).
- **`i.v()`** (i.java:730): typed early-true arms, then `au>i → false`,
  then per-type tail — for ax67 `a(k.ac, Y)` = Y bounds quad vs the
  camera view rect = the on-screen check.
- **bB() springboard** (`bk==27`): odd S∈{19,21,23,32,35,38} armed —
  `W∩playerW → ah=768+k.Y + aS.a(40,0,0,this) + i(S+1)`; for S∈{19,21,23}
  the `k.bd[]` scan arms ax68-linked children (`ad.i(2)`, `d(8,…)`
  floatie unported, `r0.i(10)`) — and when both fire the same tick S
  double-increments (test pins this). Even S∈{20,22,24,33,36,39} →
  `r() → k.c(this)`. S∈{25..31,34,37} dead.
- **bB() kind-5** (`Z[0]==5`, bk=35): S28 — `X∩playerW && !v()` → spawn or
  re-pin `aS.ae` ax14 S71 indicator at `k.O+20` / `k.O+380` by `r02` sign;
  `W∩playerW` → shove `i(309)`, `aC=5`, `ag=±2560`, `g.a=null`; else
  `aS.G()`. S12 hide spot — `Z[1]==1 && g.g==null && W∩playerW` →
  `aA|=8`, `g.e=this`, `az-1`; the release arm (no longer ours) restores
  `aA&=-9`, `g.e=null`, `az=100`.
- **`i.a(int,int,int)`** (i.java:9810): `a(14,9,anim,302)` ax14 clip9
  pickup — `P|=512, aw=-1` — the offscreen edge-arrow indicator.
- **Full `i.t()`** (i.java:369-591): ax∈{14,37,10,5,42} early-return
  (record-filled boxes, L419); W = `aa.a(S,T,0,W,P&7)` + `±dx` + `+dy`;
  X same via which=1 (skipped only for ax66 S∈[6,10]∪[24,28]); Y = the
  `ak_or_al` per-object bounds quad folded ∓dx/∓dy → mirror-swaps →
  second ±dx anchor fold + dy; then X→Y→W get `+ak/+al` (in that order).
  ax60's per-edge `Z[5]` remaps between Y-abs and W-abs noted, unspawned.
- **Clip `ak_or_al` section proven** (b.java loader + g/h/e/f readers):
  per-object bounds quads [x,y,w,h] i16-le, present in ALL ported clips —
  emitted as a new ACPK block + `Clip.objectBounds()`.
- **`k.O`/`k.P`** = camX/camY (k.java:69-70); `k.ac` = view rect.

## Semantic finding

clip35 (the real kind-5 clip) has an EMPTY `am_or_an` rect pool — its W/X
degenerate to anchor-point rects after translate. With inclusive `i.a()`
the arms are live only when the anchor lands inside the player's box —
proven original behavior, not a port defect. clip64 (kind-9) similarly
has 0 rects + [0,0,1,1]-style bounds: pure decor, correct per bB().

## Ported (rewrite/)

- `Clip.bounds` + `objectBounds(obj)` (new ACPK block; converter emits
  `ak_or_al_records` for all clips — regenerated).
- `Entity.refreshBoxes()` = full `t()`: early-return set, W/X/Y fill,
  ax66 X-skip gate, ax13 radial arm, U<0 L82 arm, X→Y→W translate order.
- `Entity.updateAu()`/`wasHitRecently(world)` = `u()`/`v()` verbatim;
  ctor defaults `au=10`, `i=1`; `deactivate()` = `p()`; `releaseAe()` = `G()`.
- `LevelCellSource`: `kP`, `camRect`, `inPlay`, `playerRect()`, `kO/kY`,
  `spawnPickup()` = `i.a(…)` → `k.b(aK)` insert.
- `NpcFsm.decorClip`, `initDecor` (L347+aB), `tickDecor` = `bB()` with the
  S12 release arm, exact S28 spawn/re-pin/shove/release flow, and the
  bd[]-scan (ax68 links; `d(8,…)` floatie unported — flagged).
- Spawn: ax67 resolves its clip per-record via `decorClip(r8[7])`; level 0
  spawns all 253 kind-9 decor (clip64) plus springboard/kind-5 records.
- gdx: clip64/clip26/clip27/clip35 converted, loaded, palette-routed.

## Verified

- `:core:test` — 67 tests green (incl. ax67 spawn fields, kind-9 inertness,
  springboard bounce + double-i(S+1) ad-scan, S12 bind/release, S28
  spawn-pin/shove/release).
- `verify-static-reconstruction.py` → `ok:true`; `unittest` 57/57.
- Emulator boot: `level0: 637 records … npcs=403`, no crash/quarantine;
  decor sprites render (reports/emulator-boot-decor.png — teal column +
  prop box visible on the spawn rooftop).

## Left unported (flagged in code comments)

- `d(8,…)` floatie spawner inside the bd[] arm; ax68 producers (`ad` is
  never bound in level-0 records); ax14 `aX()` full tick (pickup motion/
  magnetism) — spawn+pin is ported, lifetime tick is a follow-up slice;
  `g.f()`/`ci` pickup producer arms.
