---
title: "Slice 392 — ba() (the ax24 projectile / knife / heal-shrine FSM) follows the bytecode"
phase: "port"
status: "done"
slice: 392
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (ba() @0-1648: head @0-362, switch @366, arms @540-1648)
  - reconstructed-project/bytecode/i.javap.txt (bG() @152-585 — the S18 knife that feeds S13/14/45)
---

# Slice 392

Found while chasing the mission-4 capstone after slice 391 made `bG()`'s
knives real. `ba()` drives every ax24 entity: the pool volleys (S0-4,
S22-28), the escort knives (S13/14/45, S11, S16-18, S41-43), the knife
impact states (S9/10/15), the damage-number popups (S31/40), the heal
shrine (S19/20/21), the mines (S35/36). The port's arms came from the simple
decompile, which prints block layout and inverts conditions.

## Divergences fixed (all `proven`, raw `javap -c` offsets)

| arm | bytecode | port had |
|---|---|---|
| S13/S14 @966/@979 | `S13: r() → i(14)`; both: `j--`; **`bZ > ap-30` → i(45); return**; `k` → i(45); return; else `X`∩`aS.W` → i(9) + `aS.a(38,…)` | `bZ <= ap-30 → i(45)` — every knife left S13 on its **first** tick (bZ is always ≤ ap-60 at spawn), flew as S45 and exploded in place (S15); `k` read from a second field `projK` that nothing wrote (bG writes `k`); S14 re-tested the anim end |
| S45 @1065 | `j--`; **`bZ > ap` → i(15)**; `k && j<=0` → `k=false`, i(15); else hit test | `bZ <= ap` → i(15) |
| S16-18 / S41-43 @1232 | `!v() → k.c(this)`; on `X∩aS.W`: S16-18 `k.c(this)` **then** `aS.a(38,…)`; S41-43 just `aS.a(38,…)` | S16-18: removed and returned — **never damaged the player** (the barrage knives of `g(I)` are S16-18) |
| S20 @1399 | heal-shrine contact ends `i(21)` (@1522); S21 is inert (@1534) | no `i(21)`: the shrine stayed armed and refilled the meter and the conveyor on **every tick** the player overlapped it |
| S31 / S40 @540 | follow `af`; **only** an ax24-S19 parent returns; every other parent (and `af == null`) falls to @614: `ag=ah=0`; `r() → k.c(this)` | `af != null` → follow, never expire: popups (`p(II)`, the director's `p(-2,-240)` node markers) stayed in the pool for ever |
| S19 / S35 / S36 | S19 latches `G` (@1380); S35/36 set `b` (@1535, @1606) | a single `projB` field used for all three (and `G` already exists as `runnerG`) |
| head @62-120 | `!v()` retires (`P\|=128, P&=-17, aG=-1, af=c=null`) and the code **falls through** into the `aG` dispatch (@120-362); `aG != 3 → c = null` | `if (!v()) retire else dispatch`: an off-screen knife overlapping the player did not land its hit; `c` survived non-chain types |

Verified equal (no change): the S6 drop line, S7, S8, S9/10 impact (both
`bh == 3` and normal arms), S11 homing leg, S12, S15, S44, the `aG==1` trail
and `aG==3` chain arms, the player-hit arm.

`projB` / `projK` are removed from `Entity`.

## Consequences (observed)

- The escort knives now fly their arc — S13/S14 until `bZ` passes `ap-30`,
  then S45 until `bZ > ap` — and hit the player on the way; with slice 391's
  launch range they are the real threat of mission 4. The capstone bot still
  wins mission 4 unchanged.
- Heal shrines work once (mission 1 / 4 escape shrines).
- Popups expire.

## Tests

`Slice392Test` (17) + the corrected Slice56 units (S19 `G`, S20 spent, S36
`b`, S45 under the arc). Mutation-checked: 15 reverts, 14 killed; the
survivor (S14 re-testing the anim end: `i(14)` at S14 is a no-op) is an
equivalent mutant.
