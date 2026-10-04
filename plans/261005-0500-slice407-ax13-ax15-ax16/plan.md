---
title: "Slice 407 — ax15 block bu(), ax16 knife bb() S17 and the mounted-rope g.k() re-read from the raw bytes"
phase: "port"
status: "done"
slice: 407
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bu() @338-376 / @762-938, bb() @470-719, aW() @0-1162, l(i) @0-105)
  - reconstructed-project/bytecode/g.javap.txt (k() @14261-14640, j() @14135-14184)
---

# Slice 407

The audit loop continues with the three entities that share the "rope / block / knife"
corner: the ax15 grapple block (`bu()`, 1712 B), the ax16 request/knife marker (`bb()`) and
the ax13 swinging rope (`aW()` + the player-side `g.k()` input and `g.j()` release). The
raw bytes (not the decompiler, which prints the block layout of `bu()` and the `g.k()` pump
arms wrongly) are the authority.

Verified equal (no change): `aW()` end to end — the integrator, the aG1 release, the
swing-side latch, the grab scan (box maths, segment pick, latch, `bP/bO` kick), the aG4
growth, the clamps; `l(i)` (`ropeArcPlace`); `g.j()` (`releaseRope`); the rest of `bu()`
and `bb()`.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **ax15 gC arm** | `bu()` 350-376: `a(aS.W, W)` true → 376; false → `aS.ac != this` → 1095 | the claimed-but-not-overlapping player (`p.ac === e`) went into the *capture* arm | pushout + `hangOnEdge` run for an overlap **or** `p.ac === e`; neither → skip (1095) |
| **ax15 outside-span fling** | `bu()` 762-938: both pushout `if`s join at 929 `aS.a(2560)` | `flingAirborne(2560)` sat inside the two pushout arms — a player at the span edge with no velocity into the block was left hanging in mid-air | the fling is unconditional after the two `if`s |
| **ax16 S17 re-bounce** | `bb()` 470-719: `g.b() && !bR` → bounce; every other combination → 707 `aS.a(4,0,0,r1); r2 = 1` | an *attacking* player met a knife that had already been bounced once and **nothing happened** — the knife flew through him | `if (!(playerAttacking && !bR)) hit else bounce` |
| **rope input, aG1** | `g.k()` 0-25: `bM == null \|\| bM.aG == 1` → return | the aG1 rope was driven by the D-pad like the other variants | early return |
| **rope pump arms** | `g.k()` 183-212 / 284-313 | `clamp` **and** `±512` (the clamp then always moved on by 512) | `clamp` **or** `±512`: the pendulum alternates `-1280 / -768 / -1280…` (`1280 / 768…` left) as in the bytes |
| **rope descend zero-test** | `g.k()` 485-499: `bO != 0` tested twice, never `bP` | `bO != 0 \|\| bP != 0` | `bO != 0` only |

Reach: shipped rope data has only aG4 (`r8[4] = 3`, all 14 records), so the three rope fixes
only matter for the other variants (aG0/2/3 never occur in the shipped missions); the
ax15/ax16 fixes are live in the missions that use blocks and thrown knives.

## Tests

`Slice407Test` (14): the gC arm (claimed-only / neither / overlapping), the capture arm
(unconditional fling / side pushout + fling / inside-span claim), the ax16 S17 arm (first
bounce / re-bounced hit / non-attacking hit), `g.k()` (aG1 return / right pump / left pump /
descend / climb zeroing). The old `Slice45Test` rope unit used an aG1 rope and now an aG0 one.
Mutation-checked: 7 mutants (gC claim, fling placement, knife gate, aG1 return, both pump
arms, descend test) — 7 killed.

## Not done (parked)

Everything still unaudited: ax27 `bL()`, ax30/54 `ax()`, ax56 `ay()`, ax5 `aq()`, ax22
`aN()`, ax43 `bw()`, ax60 `bj()`, ax61 `aR()`, ax9 `bM()`, ax2 `aY()`, ax46 `aZ()`, ax14
`aX()`, ax19 `aO()`, ax6 `an()`, ax8 `ar()`, ax37 `al()`, ax41 `n()`, ax42 `bz()`, ax58
`bg()`, ax64 `bl()`, ax67 `bB()`, ax74 `bN()`, ax76 `bO()`, `applyHit` ops 39/40/41, the ax11
script-claim head @1700-1951.
