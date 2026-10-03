---
title: "Slice 350 — l() for ax17/23/50: camera containment + the full sight test (G8)"
phase: "port"
status: "done"
slice: 350
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/simple/i.java:2255-2406
  - reconstructed-project/src/simple/i.java:665-676
  - reconstructed-project/src/simple/i.java:8742-8745
  - reconstructed-project/src/structured/i.java:1735-1790
---

# Slice 350 — `l()` for ax17/23/50

Phase 1 item 1.6b (G8, found in slice 346) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Findings (proven)

- `l()` (`simple/i.java:2255-2406`): `ai()` fast path → true; `aS.aA&8` →
  false; `case 17/23 → L70`, `case 50 → L77` — both `bn → false; else
  b(this.W, k.ac)`, and `b(int[],int[])` (`:665-676`) is **containment**
  (W fully inside the camera rect); then L88-L101: LOS `e(aS)`
  (`structured/i.java:1735+`) and player ∉ {284, 285}.
- Port `losL` (the general `l()`) used **overlap** in its `17, 23, 50`
  arm. In the port's dispatch only ax11 and ax23 reach `losL` (ax17/47/
  50/73 have their own ticks), so this hit ax23 — which no current record
  spawns.
- The civilian tick `aA()` L12 calls the full `l()`
  (`simple/i.java:8742-8745`), but `tickAx17` had ported only the L70
  camera gate (`!bn && insideOf(W, camRect)`): no `ai()` fast path, no
  `aA&8` blind, no LOS, no S284/S285. This is the part that changes play —
  civilians are common.
- ax50's `aK` used a private duplicate of the L77 arm (`seen50`, slice 346).

## Fix

- `losL` `17, 23, 50` arm → `insideOf(e.W, ac)` (the existing `b()` port).
- `tickAx17` S57 notice → `losL(e, p, w)`; `tickAx50` (`aK`) →
  `losL(e, p, w)`; `seen50` removed. All three types now share one `l()`.

## Test

- `Slice350Test` (6), staged by scanning level0 for an open row (control)
  and a row with a solid cell between civilian and player: ax17 panics
  when fully on camera with clear LOS; a wall blocks it; half on camera
  doesn't see; player S284/S285/`aA&8` is never seen; the `ai()` zone
  alerts off camera; ax23 spots only when fully on camera. Four fail on
  the pre-fix code (wall, S284/285/aA&8, `ai()`, ax23 overlap).
- `Slice17Test` quadrant-pick fixtures fixed (not the code): the "player
  above" placements put the player at y < 0, off the map, where OOB cells
  read solid — under the full `l()` the original would not notice either.
  Moved to level0's open top-left block (civilian y 320, camera kP 100).
- `Slice64Test` / `Slice346Test` pass unchanged with `aK` on `losL`.

## Impact

- Probe over the whole suite: civilian notices in capstone runs 9 → 8
  (m3 legs unchanged at 7; `Slice289Test` 2 → 1). All capstones pass.

## Gates

Verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
`:core:test` 1620/1620; `:gdx:test` 9/9; `:android:assembleDebug` OK.

## Note

Test names with an em dash compile to class files with non-ASCII names
when they contain lambdas/function references; this container's JVM has
`sun.jnu.encoding=ANSI_X3.4-1968` (no `LANG`), so `Slice350Test` uses
ASCII hyphens. CI should set a UTF-8 locale (Phase 4).
