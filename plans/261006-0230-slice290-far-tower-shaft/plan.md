---
title: Slice 290 — far-tower shaft descent phase-lock solved; mission-0 capstone wins end-to-end
phase: capstone-frontier
status: done
---

## Problem

The mission-0 capstone bot (`bot completes mission-0 end to end`,
Slice1Test.kt) stalled at deaths=301 on the far-tower shaft: every
descent that reached the ax22 capture zone @(9874,514) died on the very
next tick — `died@9854,494 S19 x1=0` — before the zone's vault fired.

## Root cause (this slice)

A deterministic door-crush race, phase-locked by the camera LOD gate:

- `tickDoor` (NpcFsm.kt:147-189) invokes `crush()` only on
  `S ∈ {0,3,4,7,8..13}`. For the two shaft doors (records #26-27,
  free-running 7-tick cycle `S0→S1(×3)→S2→S3(×2)`), only the S0 frame
  overlaps the pinned player (`W[1]=570 ≤ 576`; S3's crush misses by
  1px: `W[1]≥577`).
- The entity tick gate (Level0World.kt:4846-4853, k.java L215) only
  ticks `au<2` entities — the doors' clock starts at a deterministic
  camera-proximity offset before the capture, so `capture+1 mod 7`
  always landed on S0. Door records tick ~364 slots before the zone's
  record #390, so the crush ran before the vault-out arm.
- Fixed arrival timing (deterministic elite-strike/fling descent) meant
  every retry hit the same vuln=S0.

## Fix (test-driver only; the port is faithful)

Two decorrelation sweeps in the capstone policy, mirroring what a real
player does (vary approach timing):

1. Gondola dismount point `9590 + deaths % 28` — sweeps tower landing,
   patrol phase at arrival, strike position.
2. Floor walk `deaths % 7` extra east ticks in `ak 9600..9820` — shifts
   the descent after the door clock has started, sweeping vuln across
   the 7-phase cycle until a retracted frame (S1/S2).

Kill-hop window kept at `20..110` (proven by the win; `35..70` verified
to lose — deaths=2 + stall at x9850).

## Result

`CAPSTONE won=true deaths=1 maxAk=11576 t=3405` — full mission-0
playthrough via real input only: spawn → corridor → door gauntlet →
duel → x9000 wall → gondola/wire → far-tower descent → pocket floor →
crushers → checkpoint → door-teleport → chasm rope → goal ax5@11448.

## Debug removed before commit

- `X1DRAIN` x1 setter instrumentation (Entity.kt) — restored verbatim
- `CRUSH` println in `crush()` (NpcFsm.kt)
- `TMP redrive pit-floor x8900` and `TMP shaft descent x9870` probes
- `CX`/`CY`/`TRC` println traces; door fields in the S-transition trace;
  stale `doorS1Ticks`/`standTicks` vars

Diff is confined to `Slice1Test.kt` (driver arms + comments); no
production change was needed.
