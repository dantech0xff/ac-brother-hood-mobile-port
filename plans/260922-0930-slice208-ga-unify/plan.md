---
title: "Slice 208 — g.a unification: standingOn delegates to ga"
phase: port
status: done
---

## Summary

Second split-brain fix in the `k.bJ` class: the port had TWO fields for
the single J2ME `g.a` (the support/grapple/ride link):
- `standingOn` (Entity.kt:91, "`a` — entity stood upon") — read by ~20
  proven-cited support checks but **never written non-null** → every
  read dead.
- `ga` (Entity.kt:150, "g.a grapple/ride link") — armed by the real
  `g.a = rN` producers (lunge Entity:1250/1325, ax43/ax66 ride binds,
  ax15 grapple, S41 held-ax51 zone).

Source audit proved they are one field: `g.o()` (g.java:6090) reads
`a.ax ∈ {51,15,43}`; the `i.bq` crate-level arm (g.java:805) and
crate-edge checks (g.java:5354) read `a`; `g.a` binds in i.java:2714
(player placed atop the bound entity `r6`), 28616, 42048, 44789; all
g.java writes are `a = 0` clears.

## Fix

`standingOn` is now a custom-accessor delegate over `ga`:
```kotlin
var standingOn: Entity?
    get() = ga
    set(v) { ga = v }
```
Every prior `standingOn = null` clear (`flingAirborne`, `enterFall`,
damage intake, reload) now correctly lands on `g.a`, and every support
read (`o()`, `e()` feet-band, `aF()/aG()` crate edge, S209 crate-stand,
`i.bq` arm) lights up when the ride/grapple producers bind.

NPC-side reads (`e.standingOn` in NpcFsm:803) delegate to `e.ga` —
never armed on NPCs, identical null semantics.

## Blast-radius check

Every `ga` producer audited → all map to `g.a` write sites
(i.java:2714/28616/42048/44789 + NpcFsm bind arms). No site expects an
independent field; the Level0World:683 reload sweep's double-clear is
harmless.

## Tests

`Slice208Test` (3): field delegation, `o()` gate sees crate via `ga`,
`enterFall` clears the unified slot.

## Verification

verifier `ok:true` · 57 unittests · `:core:test --rerun-tasks` ·
`:gdx:build` · `:android:assembleDebug` — all green.
