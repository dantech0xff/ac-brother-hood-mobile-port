---
title: "Slice 411 — the flying player tick g.n() re-read (S1/9/12/27 + S3 recovery, BGM) and the mount lunge g.c(i) picks its anim with the AIR set; 7 m7 legs + m5 legD/legJ re-routed"
phase: "port"
status: "done"
slice: 411
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (n()V flying tick @0-2300; c(Li;)V @0-470; b(I)Z lookupswitch; b()Z)
  - reconstructed-project/bytecode/k.javap.txt (B()V callers: l(I)V, Q()V, a(Z)V)
---

# Slice 411

Two things came out of re-reading `g.n()` (the flying machine's tick, mission 1 and 4) against the raw
bytes, and one of them was a second instance of the overload confusion that slice 410 found in the
hit intake.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **`n()` S1/9/12/27 recovery** | @1022-1025: anim finished → (`bB = 0; bG = -1; az = 202` if `bB`; `aC()`) `i(4); goto 2262` — straight to the friction block | the same, then `av = false` **and** the glide tail (steering input, chase block) on the recovery tick | `i(4)` and the friction block only |
| **`n()` S3** | @1032-1048: `r() → i(4); goto 2262`; an unfinished S3 takes `av = 0` and the glide tail @1048 | `i(4)` *and then* the glide tail | the glide tail only for the unfinished S3 |
| **`k.B()` per tick** | not called from `n()`: its callers are `k.l(int)`, `k.Q()`, `k.a(boolean)` (all reached by the port's `missionInit()`) | the port re-requested the mission track on every flight tick | removed |
| **`g.c(i)` lunge anim pick** | @138-160: `invokestatic g.b:(I)Z` — the lookupswitch over the **air / hang / climb set** `{18-20, 22-25, 35, 36, 43, 150, 157, 165, 233, 242, 243, 263-266}` → `i(292)`; `S == 298` keeps its anim; anything else picks the 272-275 slope arc | `PlayerFsm.isAttackState(S)` — the no-arg `g.b()Z` attack test: an **airborne** press took a slope arc (hand box ≈ +21,−47 lifted the player and shortened the swing radius) and a **grounded combo state** took S292 | the air set. S292 has no W/X rects: the player stays on the press point and the CURRENT distance to the wheel / pole is the orbit radius `cB` |

Verified equal (diffed against the bytes, no change): the rest of `n()` — head meter / conveyor arms,
the S2/S24 and S20-S23, S25-S29 arms, the glide tail's steering and wisp-chase blocks, `aC()`,
`e(boolean)`, the friction block and the `ad` copy.

## What the lunge fix changed in play — and the capstone re-routes (bot route/input only)

The faithful aerial lunge starts the swing at the press point with the full radius. Every capstone
that used to press the wheel/pole mid-air from far away now orbits through geometry the old arcs
never touched.

| test | what blocked it | now |
|---|---|---|
| `Slice301Test.mission7SpringLaunch`, `Slice302Test.mission7WestWingLanding`, `Slice303Test.mission7UpperWing`, `Slice304Test.mission7UnderChamber`, `Slice306Test.mission7WestTowerCatapult` | the lift-row wheel uid36@(510,1345) was pressed from far away (S22/S43 at 150-250 px): the orbit radius = that distance, the swing sweeps down into the y1500 lift-row platforms and the mount exits (S277 → S0) | press only once the wheel is within 140 px (`wheelInReach`; scan: R 120-150 pass, ≥160 fail) — the hop apex at ≈(604,1446). `chainDone` (the post-u252-carry landing latch) also had to ignore a mounted grounded player: standing on the crates in S277 counted as "landed", the bot froze on an `ax66` lift forever |
| `Slice289Test.mission5CapstoneLegD` | from the hanging S265 the lunge keeps the full radius, so the launch no longer meets the seesaw uid76 (the S297 catch): the arc drops on the WEST slab guard uid77 (stab, S89/S90) and the east guard uid65 wakes — running on took a strike every ~26 ticks (5 strikes, x1 = 5 of 30) | point-blank melee (`M_RIGHT + M_CONTEXT` while uid65 is within 56 px, attack released on the S69 last frame — the leg-I policy): 0 strikes taken, the guard dies at t≈162; range 52-60 and every wall-hop start tested all pass |
| `Slice289Test.mission5CapstoneLegJ` | the pole windows were fitted to the old arcs: the old press point (below-left of pole-1, al ≈ 726-728) is now the swing start, 41 px above the water row, S50 at once | the press is taken while the pole is within 170 px, airborne, on the next integrated point (`N+ag, O+ah`): pole-1 passes for R 140-200, pole-2 for R 100-200 |

## Tests

`Slice411Test` (9): S3 finishing tick = `i(4)` only, S1/9/12/27 recovery (iBB release, `az = 202`, no `av`
write, no steering), the unfinished S3 steers, no per-tick BGM request; the lunge pick for every state
of the air set (S292, sfx 30, `F` linked, `cB` = `k.h(target − press point)`, the arc ends in S277 on
the press point), every grounded state incl. the attack anims → 273, the slope thresholds (64 / 256 /
1024, the `r03 == 0` / `r04 == 0` degenerate picks), S298 keeps its anim, the grounded arc lifts the
player and the aerial one does not.
Mutation-checked: 10 mutants — 10 killed (S1/9/12/27 old tail, S3 old tail, per-tick BGM, old attack-set
pick, S298 branch, the three slope boundaries, the degenerate swap, sfx 30).

## Docs

`docs/gameplay-mining/player-mechanics.md`: the lunge anim pick (S292 vs the slope arcs, radius) and
the `n()` recovery arms.

## Not done (parked)

ax60 `bj()`/`c(Z)` (7 records), ax41 `n()`, ax61 `aR()` (all but S8), ax69/72/78/79/32/25/65, ax76
`bO()`; the ax11 script-claim head @1700-1951; the `k` UI methods (`ah`, `M`, `R`, `Q`, `G(int)`); the
remaining `g` methods (`au()`, `l()`, `as()`, `ar()`, `aq()`, `aw()`, `ax()`, `d(boolean)`, `o()`,
`ap()`, `am()`, `av()`, `i(i)`, `al()`, `j()`, `k(int)`, `ak()`, `aA/aB`).
