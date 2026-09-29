---
title: slice 292 — mission-6 capstone leg B (checkpoint795 → checkpoint113 + chasm descent)
phase: capstone-bot
status: done
---

# Slice 292 — m6 capstone leg B

## Scope
Pantheon (aj6 / pack-12) capstone leg B: from the **checkpoint-795 junction
@(5973,180)** east across the Pantheon's east mass to **checkpoint uid113
@(7096,225)** firing, then over the mass east wall into the designed chasm
descent (S164) and the S361 carry — proven end-to-end by real input.

## Route proven (driver: `Slice291Test.mission6CapstoneLegB`)
upper route ~x6060@y219 → drop to floor y539 → long hop x6430-6510 → S101
grab remnant west face → S65 capture uid102 → west vault → S101 column east
face → uid97/96 vaults → arc over divider → S29 ride chimney x6741 → S89
strike loop → S12 sprint east through uid99's recovery → S22 hop into step
west face → S101 bounce → mid platform → S62 lip → pocket hop west → mass
east face S101 → step west face S101 → **S65 capture uid103** → west vault →
mass east face grab → bounce → **S65 capture uid110** → east vault → block
top (7059,259) → run east → **checkpoint uid113 consumed @(7089,259)** →
S164 slide down-east over the chasm → land (7690,559) → S361 carry to
x7933 → death (below-camera, camera clamped by uid155 ax37 scroll-bound).

## Assertions
- `checkpoints[aw==113].consumed == true` (checkpoint fired at t≈391).
- `maxAk > 7300` (player crossed the mass east wall edge alive; observed
  x7933 before the carry frontier).

## Frontier handed to leg C
The S361 carry deposits the player hovering aZ=false over the type-20
platform (x7620-8100@y560); it never lands before the below-camera kill
(`al > kP+240`, camera clamped by uid155 zone @(7713,391)). The designed
route likely continues via the platform to the ax66 lifts @(8178-8293,304)
and band-level wisp trail (8141-8288@111-262) — next leg's job.

## Provenance
- S164 = empty arm in `59,65,164,211 -> {}` (PlayerFsm.kt:378-381; g.java
  L2b66) — scripted chasm descent, `ag=2560, ah=752`.
- checkpoint uid113 ax2 initAx2 (NpcFsm.kt:9981): Z[0]=linked ax5 uid,
  overlap → `checkpointSnap` + `kG`.
- Below-camera kill: Entity.kt:1281 `al > kP+240 → stateL(12)`.
- uid240 ax5 S8 zone [7667,556,7867,570] Z[2]=-1 → `missionResolve` each
  tick (NpcFsm.kt:2960+) — jC=21 dialog flash on landing.

## Gates
verifier ok:true; unittest 57/57; :core:test 1519 tests 0 failures;
:android:assembleDebug clean.
