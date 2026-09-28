---
title: "Slice 287 — m3 capstone legH: finale descent chain → mission-complete"
phase: port-slice-287
status: done
---

# Slice 287 — mission-3 capstone legH (finale)

The last leg of the mission-3 capstone bot chain. legG's zipline lands the
east mass top (~x13080 y540-660); legH carries the player from there to
`screenL(15)` — mission complete.

## Route proven

1. Walk east through aw132's claim box (x13136-13174) → binds descent
   claim 927 → rides the player to the gap, drops on the west mass top y820.
2. Walk west off the mass edge → ax22 zone aw910 captures @(12944,902) →
   `16390` (UP+TAP_L — Z[2]=0 → west vault mask) → S19 vault west.
3. Lands ax10-S17 balance beam aw912 @(12800,899) → S297 pin → `8` east
   side-leap → recaptured by aw910 → second `16390` west vault → lands the
   pillar top (12748,1078).
4. S257 → S29/S28 carrier rides down the pillar with DOWN pulses → releases
   to the pit ledge (12720,1299).
5. Walk east → `8` grounded jump at ~x12790 → lands ax46 spring aw409's pad
   (x12861-914,1326) → launch ag=12800, ah=-20480 east.
6. Arc lands embedded in the lip's west face (13338,1299) → S79/S81
   embedded-creep east with M_CONTEXT → win-zone aw925 (S31, W[13339,1099,
   13555,1272]) overlap → S277 mount → S317 ride east → `screenL(15)`.

## Fixes kept

- `NpcFsm.kt aUDraw` (i.java:32740 `bA[].b(j.f)`, proven): the S31 claim
  zone's draw arm now advances each armed prompt's anim by the frame delta
  (62ms) — without it `stopped()` never latches and the `nl=1` claim
  completion path is unreachable.

## Scratch removed

- `mission3SpringLaunchProbe` test (the decode probe — its chain is now
  encoded in `mission3CapstoneLegH`).
- MOVE-probe debug in `Level0World.kt` (`dbgAk/dbgAl` + position-jump
  println).
- legH printlns/LHD/cam= debug (rewrote the probe into the asserting test).

## Verification

- `mission3CapstoneLegH` → `assertEquals(15, w.jC)` — passes (jc=15).
- Full `:core:test` — 1505 tests, 0 failures.
- `verify-static-reconstruction` — `ok:true`.
- `python3 -m unittest discover -s tests` — 57 pass.
- `:android:assembleDebug` — clean.
