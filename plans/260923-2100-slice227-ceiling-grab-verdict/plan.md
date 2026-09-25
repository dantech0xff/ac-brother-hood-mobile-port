---
title: slice 227 — '5' ceiling-grab verdict test + stale-comment fixes
phase: fidelity-verdict
status: done
---

## Summary

Puts a regression test on the `L3ad8` ceiling catch (g.java:8255,
proven): `cw && aO==5 → i(280)` + `al` snaps to the '5' lip row +10.
This answers the demo-verify report's open question — '5' cells are
NOT pass-through-up; a rising jump reaching under a '5' lip grabs it.

## The subtlety the test exposes

`postTail`'s `aO` is NOT the head-row probe — `airWallResolve`/`av()`
(Entity.kt:1374, verbatim i.java) runs `al -= 20; probeCells; al += 20`
before postTail, so `aO` at the arm reads **the cell 20px above the
head**. Consequences:

- The grab fires when a rise reaches the row *below* a '5' lip (head in
  air, lip above) — not when the head overlaps the '5'.
- If the cell above the head is solid (`aO>=20`), `av()` fires
  `enterFall()` first — the verbatim head-bump fling — and postTail
  never sees the '5'.

`cw` is armed by the air family (S20/22/23/25/215) and `fallArm` every
tick, so any airborne rise has the arm live.

## Verdict

The corridor '5' trap reported by demo-verify Run-3 is the level's own
design — verbatim, not a fidelity bug. The intended route is from above
(balcony/checkpoint path via the shaft carrier + wall-kick chain proven
in slices 210/212).

## Stale-comment fixes

- `menuRowAt` doc now cites the proven layout (k.java:5977-6148 `b()`
  draw proc `i9` walk).
- `menuJc27` doc corrected: verbatim IGP self-exit (`igpTick0()` →
  `stateL(2)`).

## Gates

- verify-static-reconstruction: `ok:true`
- unittest discover: 57 pass
- `:core:test` (incl. new test): green
- `:android:assembleDebug`, `:gdx:build`: green
