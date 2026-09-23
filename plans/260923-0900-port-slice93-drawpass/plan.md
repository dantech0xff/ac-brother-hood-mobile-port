---
title: "Slice 93 — k.b(z2) entity draw pass: bd[] sort + visibility arms"
phase: port
status: complete
---

# Slice 93 — `k.d(i)` draw-order insert + `bd[]` build (k.java:2492/2861)

## Scope

Ports the entity draw ordering the renderer was missing — previously it
drew `npcs` in spawn order then the player, ignoring `az` depth and the
visibility arms.

## Proven (k.java/i.java)

- `k.d(i)` (:2492-2505): `bd[]` insert — az ASCENDING (insert before the
  first `bd[i].az >= e.az`), ties keep `al` ASCENDING (the
  `iVar.al > bd[i].al` skip). Small `az` draws first = behind.
- `bd[]` build (:2861-2902): `be=0`, then per entity:
  `(P&128)==0 || ax==10 || ax==51` gate; `aw==205 && S==34` force-draw;
  `v()` in-play + (`bh3 || ay==-1`) gate — `ax14 S==38` → `az=301`,
  `ae` child with `(ae.P&128)==0` → `d(ae)` + `ae.s()`; else `P&16`
  arms — ax15 `S==9||S==10`, ax9 `S==5`, ax14 `S==74`, ax66.
  Player appended via the same `aS` block (no `v()` gate).
- Draw iteration (:2904-2934): `ad` child draws BEFORE the parent except
  ax76/ax29 (after, plus `ad.s()`); ax21 `S==1` → `ad.P&=~64` when
  `C==null || u!=9`, `ad.s()`; `ax==0` + `k.E` held entity
  `(E.P&128)==0 && (j.c==8 || (j.c==21 && u==8))` → `E.F()`+`E.s()`;
  `z2==0 && (ax!=11&&ax!=17 || aB>0)` → `ad()` bubble tick; `ab` overlay
  child `(ab.P&128)==0 && ab.v()` → `ab.F()`.

## Already-ported dependencies (verified this slice)

- `i.u()` = `offscreenScore` (Entity.kt:3186), `i.v()` = `inPlayV`
  (:3209), `i.a(int[],int[])` = `overlapI`, `i.s()` = `advanceAnim`,
  `i.ad()` = `npcFsm.tickBubble` (already wired per-sim-tick for
  ax!=11/17 — the draw-pass arm adds the `aB>0` 11/17 increment via
  `drawPassBubble`).

## Inferred / deferred

- `ag()→ah()` ghost-trail arm: `i.cU` is an `a[5]` card array (afterimage
  sprites, i.java:19605) — the `a` card class and its producer are
  unported; `cU` stays null → arm is dead but positioned verbatim.
- Health-bar/`i.h(iVar)`/`bu[au]` prompt overlays (:2934+) — next slice.
- `z2` param: `z2==0` treated as the normal-render path (the flag's
  nonzero mode unmined).

## Files

- `core/…/Level0World.kt` — `drawList`/`drawCount`, `drawInsert`,
  `buildDrawList`, `drawPassBubble`.
- `gdx/…/Level0Renderer.kt` — iterates `world.drawList` with the
  `ad`/`kE`/`ab` ordering instead of raw `npcs` + player.
- `core/…/Slice1Test.kt` — `Slice93Test` (7).

## Gates

verifier `ok:true`; `python3 -m unittest discover -s tests -t .` 57 pass;
`:core:test` green; `:android:assembleDebug` green.
