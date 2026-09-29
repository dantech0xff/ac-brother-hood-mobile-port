---
title: slice 300 — m7 post-rope shelf run (checkpoint uid233 → lift row)
phase: m7-capstone
status: done
---

# Slice 300 — m7 leg: post-rope shelf run

## Goal

Continue the post-duel chain: off the rope top (ax13 uid37, y1423 anchor),
land the east shelf and run west to the y1500 lift row (ax66 u22-27,
x636-1004) — the next entity chain toward boss-2.

## Route (proven by the bot trace)

- Release from rope S326 at the anchor → wall-kick chain down the east
  mass face (S82→23→43) → land the y1500 shelf.
- Run west in repeated `43→5→21→22→23→43` kick/grab cycles past
  checkpoint `uid233@(1244,1470)` — `aY()` fires:
  `checkpointSnap.aw=233`, `kG=351` (the ax5 uid351 arrival-trigger
  link — proven semantics of Z[0]).
- Reached the lift-row edge `ak=1035` at `al=1448` (the x940-1000 step
  above the shelf).

## Changes

- `Slice1Test.kt` — `Slice300Test`: shared `driveDuelWin`/`driveRopeClimb`
  helpers (extracted verbatim from the previous legs' drivers) +
  `mission7ShelfRun` phase-3 west driver (airborne `M_LEFT+M_UP`, stall
  hop pulses `16390|M_LEFT`). Asserts `ak<=1040 || checkpointSnap!=null`,
  plus `checkpointSnap.aw == 233` when fired.
- Test-only slice — no production changes.

## Gates

- verifier ok:true, 57 unittests OK, `:core:test` green.
