---
title: "Slice 46 — ax6 an() trigger marker + ax19 aO() meter pickup"
phase: port
status: done
---

# Slice 46 — two small tick arms, both proven

## ax6 `an()` (i.java:7220-7248)

One-shot overlap-trigger marker. Armed `S3`/`S5` wait on
`a(aS.W, this.W)`; overlap fires `i(7)`/`i(6)`; `S6/S7` wind down via
`r()` → `k.c(this)` removal. clip4 (`bi[6]=4`) converted —
21 modules, 8 anims. **Clip-dependent dead arm**: anim3's object carries
zero rects → degenerate `W` → the `i.a()` point-box reject keeps `S3`
markers dormant in the original too; the test pins that behavior rather
than asserting a fire that cannot happen.

## ax19 `aO()` (i.java:10261-10299)

The meter-restore pickup: `b=true` every tick. `S17/19` idle — when the
player is interact-eligible (`g.c(aS.S)` = {0,1,7,11,12,26,79} — new
`INTERACTABLE_STATES` top-level set — or `bh[aj]==3` flying levels) and
W-overlaps: `i(18)` consume anim + `k.A(17)` sfx + 5×
`a(ak,al,5+kO,5+kP,74,54,5,300)` burst sparks. `S18/20` consume —
`r()` → `g.e(k.ax)` (`x[1]=k.ax` meter restore, only while `!g.g()`) →
`k.c(this)` removal.

New port pieces:
- `Entity.spawnFlyBurst` = `i.a(x,y,tx,ty,ax,clip,S,az)`
  (i.java:21212): 4-arg spawn + ax74 customization — `ag/ah =
  j.a(-6,6)<<8`, `Z[8]` arc table `{x-kO,y-kP,tx-kO,ty-kP,
  midX+j.a(-80,80),y-kP,0,j.a(10,16)}`, `P=528`, `k.b` insert.
- `Entity.overlapStrict` = `i.a(int[],int[])` (i.java:632) —
  `overlapI` + degenerate-rect reject (newly needed; also referenced
  by pending ax13 arm).
- `LevelCellSource.kAx` = `k.ax` byte restore value (inferred default
  90 = `x1` init; the `dB` save-restore write is unmined).
- `bi[6]=4`, `bi[19]=11`, `bi[74]=54` clip map entries; both packs
  converted.

## Tests (6, all green — 277 total)

ax6 S5 arm fires on overlap → S6 → `r()` removal; S3 degenerate-W
dormancy pinned; ax19 overlap → `i(18)` + `sfx(17)` + 5 sparks with the
Z-table/`P=528`/`ag` shape; consume → `x1=kAx` → removal; dead-player
no-restore; `g.c` gating.

## Incidental test fix

`NPC family entities spawn at record positions` now filters to the
clip-7 family it was written for — ax19 records legitimately sit
outside the tile grid (spawned verbatim like the original; they were
previously skipped for lack of a clip).

## Gate

`verify-static-reconstruction ok:true` · unittest 57 · :core 277 green ·
`:android:assembleDebug` OK.
