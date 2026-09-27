---
title: slice 279 — massif-top door-teleport + capstone mission-0 win
status: done
---

## Result

The capstone bot completes mission-0 end to end: `won=true, deaths=0,
maxAk=11576` — spawn → east corridor → x9000 wall → vault chain →
massif-face grab → W2 catch → S203 lip → massif top → S16 door-teleport
(10587→10789) → y500 terrace → chasm → goal ax5@11448,503 → mission
complete (jC==15).

## The door-leg blocker (root cause)

The posted ax11 pair (10547-10581, al~447) walking the massif top binds
`player.g` on contact via interactScan — and the door's fire gate
requires `player.g == null`. The bind lands *inside* the same tick that
steps the player into range (move → interactScan → postTail's
`isHolding() → p.cq=false`), so the `p.cq && pad.v(M_ACTION_FAMILY)`
vault gate at g.java:L2042 never sees a usable latch on the move.

## What worked

1. Standing ticks (S0) press UP *without* a direction — no step into
   the bind box, `cq` stays armed by groundedTail, vault can launch.
   (Kept as the fallback path.)
2. The actual winning path: keep walking east — the bound victim drops
   as it falls behind the |Δal|/distance gates, `g` clears by ~10550,
   and the doorPulse arm (alternating UP inside door1's W [10587-618])
   fires the teleport at ~10586.
3. door2's exit (~10805): no UP — pressing inside the W re-enters the
   two-way door and ping-pongs back to 10602. The eastward arm walks
   out of the W toward the goal.

## Assert upgrade

`assertTrue(won)` — the capstone now verifies mission completion, not
just the door crossing.
