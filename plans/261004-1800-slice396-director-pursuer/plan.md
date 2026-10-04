---
title: "Slice 396 — the mission director bD() and the pursuer script bG() follow the bytecode"
phase: "port"
status: "done"
slice: 396
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bD() @0-3115; bG() @0-2210; d(Z)Z; f(I)Z; p(II)V; g(I)V; bH(); aw(); bE(); bF(); a(II[I)Z)
  - reconstructed-project/bytecode/c.javap.txt (c.a(short[]) — `e`/`f` are bytes; c.a(c,i))
  - reconstructed-project/src/simple/i.java (18054-19290 — the misleading listing)
---

# Slice 396

Second half of the director/boss audit (slice 395 did `aP()`/`aQ()`). `bD()`
(the ax21 mission director) and `bG()` (the script every linked ax32 pursuer
runs) were re-read from the raw `javap -c` with the pseudo-decompiler and
compared arm by arm. Verified equal: the chase `d(Z)Z` (arrival, the node-event
`r3` remap, the diagonal-limited velocity), `f(I)Z`, `p(II)V`, `g(I)V`,
`bH()`, `aw()`, `bE/bF`, arm 2/6/7/8, the L2261 tail's cases 0-3, the 5th-pursuer
respawn block, the attach sync, the charge gauge, and `bG`'s pv2 arm (slice 391).

## `bD()` (`proven`)

| where | bytecode | port had |
|---|---|---|
| arm 0 arming tail @496-534 | no `f &= 127` (only arms 3/6/8 and `d()`'s advance clear the consumed bit) | cleared it (the old unit test asserted that from the listing) |
| arms 1/4/5 @775-1701 | `bG()` runs on every pursuer except the exact gone state (`aB <= 0 && S == 16/20/26`): a DYING pursuer keeps scripting | `aB > 0 ? bG : (S == gone)` — dying pursuers were skipped |
| arm 4 `r2` @1412-1583 | set only by the FIRST pursuer being gone, cleared by the second being alive-or-dying; a gone second one leaves it alone → the phase ends only when both are gone | also set by a gone second pursuer: the phase ended as soon as either died |
| arm 3 after a node advance @1169-1289 | S10 or off-screen → skip the `bs` walk; visible → `while (bs < 4 && Z[bs+1] != -1) bs++; bs--`; then EVERY pursuer falls into `P &= -17 & -33; E = 0; aC = 100` | S10 only cleared the flags; the visible walk was a single `if` and cleared nothing; off-screen did nothing |
| tail case 4 @2523-2605 | `S == 37 → i(33)` is the ELSE of the `k && P&128 → i(37)` arm | ran after it: S37 was replaced by S33 in the same call, the S37 anim never played |

## `bG()` (`proven`)

| where | bytecode | port had |
|---|---|---|
| pv0 @60-133 | `(S == 28 \|\| S == 14) && r()` → `i(14)` + `g(0..2)`; otherwise `S != 28 → i(28)` — the S14 fire frame lasts ONE call, the windup restarts: six rapid volleys (`g(2)` counts `j` to 6), and only after `cI` the dispatch is skipped and the 40-tick S14 recoil plays | `S != 28 && S != 14 → i(28)`, fire only from S28: S14 played out between volleys (a ~55-tick cycle instead of ~13) |
| pv3 j==2 @655-712 | a finished S27 → `i(24)` and FALL THROUGH to the `aZ` toggle + the j==2 arm (seven homing `a(24,40,11)` knives, `j = 0; cI = 1`); every other state returns | returned right after `i(24)`: **the seven homing knives were unreachable** |
| pv3 scatter @920-993 | `(nextInt & 1) == 0` stores `W[3]`, else `W[1]` | inverted |
| pv3 scatter @1070-1212 | the pool cell is never marked used (no store) | marked it `-1` → the re-roll loop was live: duplicate picks cost extra RNG draws |
| pv4 @1483-1514 | both `ifne`s jump OVER `l &= -2`: it runs only when `kB.l & 2 == 0 && kB.l & 12 == 0` | inverted: it ran when a pursuer 0/1/2 was dead |

`l & 1` is the "hittable" bit (`sweepNeighborsB` @607 tests it before any
player shot can land), so the pv4 fix is a gameplay change: the gunner (uid154
in mission 4) is shielded until one of the first three pursuers is down. The
pv0 fix is one too: during the six-volley burst the thrower's `W` collapses to
a point (the S28 frames carry no box) — it can be hit only in the recoil.
(`high-confidence` for the clip-data reading; the bytecode flow is `proven`.)

## Capstone bots (route/timing/input only)

`Slice288Test` (mission-4 full-shaft climb) died at the top on the faithful
pv0 burst: the thrower's fan knives (ax24 S16/17/18, tall `X` box hanging 49
px below the knife) arrive every ~13 ticks and the centre knife falls down the
thrower's own x line, exactly where the bot flew. **No enemy was weakened,
moved or patched and no state was written.** Two bot-side changes:

- `knifeDodge288`: box-based projection of the pursuers' own knives
  (S16-18 / S41-43 with `af.ax == 32`) against the player's `W` for 18 ticks;
  the bot keeps its intent unless that collides within 12 ticks, then takes the
  move that postpones the first collision the most.
- burst-vs-recoil parking: while the aw11 thrower's box is a point (burst) the
  bot parks 34 px off its lane and 110-160 px below it; in the recoil it flies
  the lane as before.

`Slice1Test`'s `aA0 arms the director into waypoint chase` was a listing misread
(`f &= 127` in arm 0) and is corrected to the bytecode.

## Tests

`Slice396Test` (15): arm 0 keeps the bit; arms 1/4/5 dying-vs-gone matrices (the
second-gone-alone case included); arm 3's three pursuer kinds (visible walk
loop, S10, off-screen) all clearing flags; tail case 4's S37 → S33 on the next
`r()`; pv0's fire/refire/restart cases; pv3 j==2 (fall-through knives and the
return cases); the scatter parity + "never marked used" (a replica of the draw
sequence on a state that repeats a cell); pv4's `l` matrix. Mutation-checked:
15 reverts, 15 killed.
