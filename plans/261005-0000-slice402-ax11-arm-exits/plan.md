---
title: "Slice 402 — the ax11 I() arms re-read from the raw bytecode: exits, flags, S21, the stab window"
phase: "port"
status: "done"
slice: 402
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (I() @1644-7691 — arms 3709-3955 S175, 4526-5027 S5/S23/S4-22/S11/S12/S16, 5030-5227 S18, 5230-5339 S17, 5342-5451 S144, 5454-5603 S6, 5606-5669 S1, 5672-5877 S25, 5880-6138 S24, 6141-6620 S0/106/107/135, 6699-6879 S21, 6882-6920 S20, 6923-6946 S27, 7048-7078 S180-183; k() :25xxx @0-1187; aF() @0-236; aD(); aI(); m(II); g(II))
---

# Slice 402

The audit loop reached the soldier family's own `I()` switch. Every arm was compared
with the raw bytes (jadx and `bcdec` pseudo-code only as a map — `bcdec` prints
`(? ? a : b)` in branch-target order and mis-models `dup_x1` post-decrements; the
ternaries and the exit labels were read from the bytes). Two things went wrong in the
jadx-derived port again and again: **where an arm exits** (`goto 7691` = L849, the
dispatch tail only; `goto 7292` = the shared L777 tail) and **which of `r4/r5/r6` it
sets**.

Verified equal: S168/S169/S142/S143/S138/S145/S133/S134/S99/S85 and their exits, S174,
S176/S177/S140, patrol S2/S3/S92, S5, S11, S16, S1, S117, S179, S184, S139, the shared
tail @7292-7660 (apart from the parked alerted-solid finding of plan 401),
`h()/i()/aB()/aD()/aI()/m(II)/g(II)/aH()`.

## Fixed (`proven`, raw bytes)

| arm | bytes | was | now |
|---|---|---|---|
| **S21** (`k()`'s stealth-kill victim) | 6699-6879 | **no arm** — the stabbed soldier looped its death anim forever at full HP, still targetable | blood puff at `T==2&&U==0`, crate-ride `ag`, at the anim end `aB=0; aA=2; P&=-17; P\|=64\|32` + pool puff → L849 |
| S0/106/107/135 | 6141-6162 | wave members (`aw >= 5000`, ax10 S30 grid) turned corpse + were tallied | `aw>=5000 && r() → k.c(this)`, no tally, no corpse |
| S175 grab hold | 3709-3955 | prompt marker spawned on the **player** (`p.spawnMarker`; nothing ever freed it) and every path returned | `r2.a(8,…)` = the soldier's `ae`; only the `g.g()` dead release returns, every live path falls into the shared tail |
| S17 | 5230-5339 | a landed counter fell on into `r()` and the tail unless the player was already in S8 | the counter always `goto 7691` (S8 or not) |
| S144 | 5342-5451 | a fabricated `hGate/iEngage` ("`h()→i()` runs before the switch") threw an attacked weakened soldier into S17 → S11 → S12 | the arm only: `aS.i(8)` recoil, soldier stays S144, `a()`, `r()` → S23 + `aS.G()`; `h()`/`i()` exist only in the tail @7479 |
| S12 | 4807-4947 | `r6` left at the head default; the `aN=this; g.E=1; b(2)` bind skipped for `Z0==0 && aB<=bu/2` and the arm fell on | `r6=0`; the bind runs for `Z0==2 \|\| (Z0==0 && aB<=bu/2)`; every counter-bind exits |
| S4/22, S23 | 4679-4685, 4601-4607 | `r6` left at the head default | `r6 = 0` (stealth-kill gate off) |
| S24 release | 6077-6125 | both cell compares inverted → the player dropped INTO the wall side | open side first: `aT < 12` → left, else `aU < 12` → right, both solid → stay |
| S25 | 5714-5877 | always fell into the tail: its open-cell gate replayed `k.A(24)` every tick of a fall and `j()/k()/aB()/push` acted on the faller | only `aD()` (crate/carrier) reaches the tail; the free fall `goto 7691` |
| S180-183 | 7048-7078 | fell into the tail | `goto 7691` |
| S27 | 6923-6946 | always returned | tail while the anim plays (clip-7 S27 is 1 frame: unreachable with shipped data) |
| S6 lunge | 5468 | `Entity.aF` — cell test inverted, crate gate on `standingOn`/`bq` | `crateEdge73` (= `aF()`: trailing-side ledge probe); the dead `Entity.aF/aG` copies are gone |
| S18 | 5030-5227 | `a(true)` ran twice; the anim-end block looped back into the offer (a second `nextInt()` on the last tick) | once; the block ends `goto 7292` |
| S20 | 6893 | `if (aB <= 0) aB = 0` (no-op) | `if (aB > 0) aB = 0` |
| `k()` marker link | 260-284 | released only when the player does not face the soldier | `!aS.g(this) \|\| this.g(aS)` |
| `k()` stab window | 344-375 | `(aS.g(this) && !this.g(aS))` only, and S38 + blocked facing sent to the marker release | `(aS.g(this) && !this.g(aS)) \|\| aS.S == 38` — the hanging-ledge kill is offered whatever the facing |

## Capstone legs (bot route / input only; no enemy, state or assertion touched)

- Medal screen: `screenL(15)` stamps medal 0 at `ap[0] >= 7` kills and re-enters the
  medal screen (jC=22) before the stats (k.java L35-L64). The finale and mission-1
  stats bots now confirm it (`pad.v(327712)`) — the faithful stab/finisher arms count
  more kills than the old run did.
- Mission-3 leg A: S89 (pinned over the pole-top guard, S24) keeps its CONTEXT mask
  (the stab edge); the post-intro "press nothing" rule applied to it and, with the S24
  drop now going to the open side, threw him into the kill floor.
- Mission-6 legs B/C (same prefix): S89 stabs floor guard uid99 from above, S310 grab
  holds are mashed, and the player backs off heavy guard uid112 while it blocks / winds
  up (S131 → S146: its damage intake is closed there, `r11` stays false, and its strike
  box outreaches the swing) — the old route walked into every windup and was won on a
  lucky RNG phase.
- Unit tests corrected to the bytes: `Level0WorldTest` weaken chain (S144 blocks, it
  does not counter-engage) and the S12 half-HP bind (`Z0==0` at half HP claims `aN`).

## Tests

`Slice402Test` (17): S175 marker owner + tail; S17 counter exit; S144 block; S12 bind
(half HP vs full) and `r6`; S4/22/23 `k()` skipped; S21 lifecycle; S0 wave removal; S24
open-side drop (3 geometries); S25 no replayed fall sfx; S27; S180-183; S6 ledge probe;
S18 single draw; S20; `k()` marker link and S38 window.
Mutation-checked: 14 reverts of the fix groups, 13 killed; the survivor is S27's
mid-anim tail exit, equivalent for the shipped data (S27 is a single frame).

## Not done (parked, `proven`)

- Alerted soldiers should be solid (`a()` @7644-7657 after BOTH aA branches) — plan 401.
- The port runs a universal `collideSides(true)` before the arm switch for every
  ax11 state (`tick()` line "if (e.S != 25) …"); the bytes have no such call — only
  S6/S23/S4-22/S18/S0-group/S142 call `a(1)` themselves (the patrol/S5/S11/S12/S16/S17/
  S85 states never resolve geometry; the tail's open-cell gate does the falling). The
  player-side superset went in slice 372; this one needs the same bot-by-bot pass.
- ax11 script-claim head @1700-1951 (`ca != -1`, `l()`, `cd[7]`, `aa()`): not ported for
  ax11; the two mission-5 soldiers that bind a script (`Z13` 112/97) carry an empty one,
  so there is no observable difference with the shipped data.
- Still to verify against the bytes: `d()` sight priority, `l()`, `j()` intake, `C()`.
