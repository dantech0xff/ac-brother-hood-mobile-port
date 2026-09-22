---
title: "Port slice 49 — ax15 bu() grapple/hang volume"
phase: port
status: done
---

# Slice 49 — ax15 `bu()` (i.java:16693-16924, proven)

ax15 is the grapple/hang volume family (`bi[15]=25` → clip25
`entry-025-marker-003`). Ported the full `bu()` dispatch verbatim:
S6/S8 run the contact body, S9 the passive marker (`P|=16`), S10 the
falling sweep (`P|=16` + `bt()`), S7 the respawn-or-remove arm.

## What was ported

- `initAx15` (i.java:3553-3581 → L362, proven): `az=r8[7]`;
  `r8[5]∈{9,10}` → `Z=int[8]` + `Z[4]=-1`; `r8[5]∈{6,8}` → `Z=int[4]`;
  both load `Z[0]=r8[8], Z[1]=ak, Z[2]=al, Z[3]=0`; shared
  `i(r8[5])+t()` — W comes from the clip, never the record rect.
- `tickAx15` (proven): `when(S)`: 9→`P|=16`; 10→`P|=16`+`sweepHostiles`;
  7→anim-end→(Z[3]==1 ? `k.c(this)` : `i(6)` + respawn at Z[1]/Z[2]);
  6/8→`ax15Body`; else `b=false`.
- `ax15Body` (proven; L94/L103 goto-loop collapsed to the symmetric
  two-side pushout — `high-confidence`): ax43-overlay collapse,
  `a(true)` collide, `p.S==50` early-out, `P&128` cull, |Δak|<60 gate,
  L34 `pushContact`, L37 hang arm (`e(r1)` → `hangOnEdge`),
  L46 locomotion pushout-or-capture, L59 `ax15Capture`, L119 release,
  L130/137 fall/settle, L141 npc sweep (ax44 collapse / ax66 ride /
  ax11 shove).
- Entity helpers (proven): `supportedByGround` (aM, :10100),
  `hangOnEdge` (e(int), :10119 — i(108), ga claim, edge snap),
  `sweepHostiles` (bt, :16669 — ax∈{17,11,23,50} + `deadRelease()` +
  overlap → `sweepReact`), `sweepReact` (as, :7680 — aB=0, ax11→i(0),
  ax17→i(69), ax23→i(79)), `gC()` (g.c, :404 — locomotion set
  {0,1,7,11,12,26,79}). Reused `releaseAe`/`deadRelease`.
- clip25 converted via `pack_clip` (27 anims/27 frames/17 modules,
  2 palettes) + wired into fixture/ENTITY_CLIP/renderer maps.

## Key provenance finding

clip25's real data gives the S6–S9 arms **empty rect spans**
(`ao_start==ao_end`), while S10–13/16 carry real rects
(`[-31,-50,57,51]` / `[-11,-53,49,53]`). Consequence, faithfully
ported: the entire contact volume (pushContact grapple arm, hang,
capture, span-edge fling, ax11 shove, ax44 collapse) is **dormant** in
the original — `overlapStrict`/`pointInBox` never fire on a point box.
What stays live on S6/8: collideSides, the external-claim release
(`p.ga===e` → null), L130/137 fall-settle, and the ax66 mount transient
(mount snaps `al=n.W[1]+1` then the point-W strict-overlap fails and
`s` releases the same tick). S10 keeps a real 57×51 sweep box above the
anchor for `bt()`. Tests assert exactly this reachable surface.

## Tests (11, all green)

`Slice49Test` — init Z8/Z4 loads, S9 P16 marker, proven-empty W on S6,
S7 respawn at Z[1]/Z[2] vs `pendingRemove` on Z[3]==1, contact-arm
dormancy, external-claim release, L130 fall (ah=4096) vs L137 settle
(bd, ah=0), ax66 mount-transient, S10 real-rect bounds + bt() sweep
(ax11 → i(0)).

## Gates

verifier `ok:true`; `python3 -m unittest` 57 green; `:core:test` 300
green; `:android:assembleDebug` green; emulator boot `npcs=480` clean.
