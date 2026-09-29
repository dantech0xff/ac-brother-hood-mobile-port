---
title: slice 301 — m7 west-wing spring launch (u33@336,1522)
phase: m7-capstone
status: done
---

# Slice 301 — m7 leg: '02'-floor spring launch into the west wing

## Goal

From the lift row, run west along the shelf blocks / '02' floor to the
spring `ax46 u33@(336,1522)` — the designed up-throw into the west wing.

## Route (proven by the bot trace)

- West off the shelf blocks: kick/grab cycles `43→5→21→22→23` down the
  shelf edge, `0→233` mounts on the sinking lifts u22-27 (they dip —
  the S9 arm drops 10px/tick until solid below; drop-assists, not
  lifts up).
- Drops to the '02' floor y1559 at x559, keeps west under the shelf.
- Hits spring `u33` at (336,1510): S0 pad arm (i.java:13647) — falling
  overlap → `a(11,0,0,this)` pin + Z-launch `ag=-50<<8, ah=-90<<8` →
  `S280` thrown arc up-left; reached `al=1310` at x270 (212px above the
  pad) — the launch is verified.

## Changes

- `Slice1Test.kt` — `Slice301Test.mission7SpringLaunch`: lifts the shared
  `driveDuelWin300`/`driveRopeClimb300` drivers to file scope (they were
  private inside `Slice300Test`), then the west driver (airborne
  `M_LEFT+M_UP`, stall hop pulses) until `al < 1360`.
- Test-only slice — no production changes.

## Gates

- verifier ok:true, 57 unittests OK, `:core:test` green.
