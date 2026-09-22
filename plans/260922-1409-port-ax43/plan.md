---
title: "Port slice 59 — ax43 bw() ride/swing carrier + o(i) grapple offer + aV() S50/S51"
phase: port-slice-59
status: done
---

# Port slice 59 — ax43 `bw()` ride carrier

## Goal

Port the ax43 ride/swing carrier verbatim from `i.java` — the gondola/rail
entity the player grabs and rides: bind offer `o(i)`, carrier FSM `bw()`,
and the two `aV()` (ax10) arms that complete the loop: S50 rope-dismount
trigger + S51 rail-zone register (`cv`).

## Mined semantics (all proven, `i.java` cited)

- **Dispatch** `ax==43 → bw()` (i.java:5081). `bi[43]=31` (k.java:8442) → clip31.
- **`o(i)`** (i.java:17038-17085): the grapple-attach offer. Non-43 callsites
  dead (ax40's `o(this)` at :17185 is a no-op). `t()` then `g.a!=null` or
  `g.g()` → `G()`+return; `aS.Y ∩ r6.Y` required; `aS.S∈{243,24,22}` reject;
  `r6.Z[2]>0` auto-binds else `a(8,ak,al-85)` prompt + `v(65568)` tap;
  `r7 && g.i` → `g.a=r6, i(1), k.ae=r6, A(19), ag=Z[1]<<8, G(), g.C=true`.
- **`bw()`** (i.java:17086-17187):
  - `ab()→aa()` claim gate first.
  - **S7 cut** (`:17098`): `ag=ah=0`, `k.ae==this→k.ae=aS`, `g.a==this→g.a=null`,
    `o(this)` re-offer (can instantly re-bind).
  - **S∈{1,4} ride** (`:17110`): `cv` containment arm — own `Y` fully inside
    `cv.W` → `g.a=null` + `aS.a(0)` + `k.ae=aS` + `k.c(this)`; `g.g()&&g.a==this`
    → `g.a=null` + `i(7)`; unbound L75 → `k.ae=aS` + `o(this)`; bound L35 pins
    `aS.ak/al` = own X-center, `aS.ag=ah=0`, `aS.av=av`, hang anims
    `S<304→i(295)`, `S<308` pin, `i(1)` at anim end. Speed `Z[1]<<8`, ×150%
    same-dir / ×50% reverse by held `u(4112)/u(8256)`; `v(16388)&&g.C → i(4)`.
  - `r()→i(1)` elsewhere.
- **`aV()` S50** (i.java:11853→L852): rope-dismount trigger — `g.a.ax==43 &&
  S∈{1,4} && overlap` → `a(7,…)` marker + `ae` sync + `g.C=false`; on
  `v(16388)` tap → launch `aS.i(243)`, `ag=±3328` by `av`, `ah=-6656`,
  `g.a=null`, `g.C=true`, `k.c(this)`.
- **`aV()` S51** (i.java:11854→L874): rail-zone register — `a(k.ac,W)` overlap
  → `cv=this`; `cv==this && !overlap → cv=null`. `cv` = new `LevelCellSource.cv`
  (the static `i.cv`).
- **`g.C`** = new `Entity.cFlag` — hang-allowed latch set by the bind, cleared
  on dismount, re-set after launch (transient per-frame, like `g.D`).

## Ported

- `NpcFsm.kt`: `grappleOffer(r6, w)` + `tickAx43(e, w, p)`; `tickTrigger`
  S50/S51 arms.
- `Entity.kt`: `var cFlag` (g.C), `LevelCellSource.cv`.
- `Level0World.kt`: `43 to 31` ENTITY_CLIP, `override var cv`, dispatch
  `ax==43 → tickAx43`.
- **Pad-name correction**: `padHeld` = `pad.v` = EDGE (`k.v`), `padDown` =
  `pad.u` = HELD (`k.u`) — confirmed Level0World.kt:604-605. `k.v` call sites
  → `padHeld`, `k.u` → `padDown`.

## Gates

- verifier `ok:true`; `:core:test` 430 tests green (Slice59Test: offer
  bind/reject paths, S7 re-offer, cv containment, ride pin, speed mod,
  S50 dismount, S51 register).
