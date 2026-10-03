---
title: "Slice 375 — b(true) is the whole world pass behind pause, death and end screens"
phase: "port"
status: "done"
slice: 375
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/simple/k.java:3450-3760
  - reconstructed-project/src/structured/k.java:1101-1125
  - reconstructed-project/src/structured/k.java:1448-1460
  - reconstructed-project/src/structured/k.java:1650-1660
  - reconstructed-project/src/structured/k.java:1786-1796
  - reconstructed-project/src/structured/k.java:2408-2418
---

# Slice 375 — `b(true)` runs `F()` too

Follow-up of slice 374, which noted that `b(true)` screens freeze the
world's `F()` effects.

## Findings (proven)

- `b(z2)` (simple k.java:3450) is: the early return for jc 12/13/31 only
  when `z2` is false; the window/veil head; the draw-list build with the
  visible NPCs' linked-FX `ae.s()`; every entity's `F()` (structured
  i.java:2957 — cG hit flash, candle fades and removals, parked-entity
  `s()`); and the `ad()` bubbles only when `z2` is false.
- `b(true)` call sites: case 12/13 `if (!j.i()) b(true)` (k.java:1108-1110),
  case 14 every frame (:1123), case 31 `if (bx >= 0 && !j.i())` (:1452-1454),
  `l(12)`/`l(13)` (:1656-1657), `l(14)` from jc 8/21 (:1790-1792), and G()
  when `cy == 14` (help opened from the pause menu, :2414-2415).
- The port mapped `k.b(true)` to `scrollBounds()` — the head only — so
  behind the pause menu, the death screen and the mission-end screen the
  world's `F()` effects stood still while the original kept them running.

`F()`'s player arm (`g.t--`, the RNG sparkle field) is gated on jc 8, so
the `j.j` RNG stream does not move on these screens.

## Changes

`Level0World.backdropB()` = `scrollBounds()` + `drawStylePass()`, used at
every `b(true)` site: the l(12)/l(13) and l(14)-from-play arms of
`stateL`, the jc12/13 menu arm, and `menuBackdrop()` (jc14 every frame,
jc31 while `bx >= 0 && !j.i()`, jc5 when `cy == 14`) ahead of the panel
paint.

## Tests

`Slice375Test`: an ax30 hit flash (cG 6) steps on `l(14)` and on the next
pause frame, and on `l(12)` and the next death frame. Full suite green.
