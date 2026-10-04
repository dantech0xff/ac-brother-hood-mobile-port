---
title: "Slice 362 — the ax44 door FSM is bv(): last frame's box, mode-1 polarity, resolve-tick promotion, i.a, crush arms"
phase: "port"
status: "done"
slice: 362
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bv() offsets 0-590)
  - reconstructed-project/src/structured/i.java:15923-16018
  - reconstructed-project/src/structured/i.java:5293-5295
  - reconstructed-project/src/structured/i.java:2900-2903
  - reconstructed-project/src/structured/i.java:229-239
  - reconstructed-project/src/structured/i.java:540-558
---

# Slice 362 — `bv()` (ax44 doors and crushers)

Found while re-routing the m0 end-to-end capstone on the Phase 2 branch:
under k.I()'s order (G12) the '5'-strip corridor poles crushed the bot on
a frame where, by the original's timing, the player was already clear.

## Findings (proven, bytecode)

1. **The box is last frame's.** `bv()` reads `W` with `getfield` (offsets
   365/369, 471/475, 569/573) and never calls `t()`; the box is refreshed
   by the `I()` dispatch tail `if (b) t()` (structured i.java:5293-5295)
   after the arm, so the crush tests the box of the previous frame's S/T,
   one anim step behind this frame's `s()` advance. The port refreshed it
   at the top of `tickDoor`, after the advance.
2. **Ctor box.** The constructor's tail calls `t()` for doors (structured
   i.java:2900-2903), so the first frame reads the spawn frame's box. The
   port's `initDoor` left `W` zero until the first tick.
3. **ax58 promotion.** `Z[0] = 1` is evaluated only inside `ac == null &&
   Z[5] != -1` — on the tick that resolves the link (offsets 87-175). The
   port re-checked it every tick.
4. **Mode 1 polarity.** `Z[4] == S && ac.bf() → i(S+1)`; `Z[4] != S &&
   !ac.bf() → i(Z[4])` (offsets 281-337). The port had both `bf()` tests
   inverted: a slaved door opened while its ax58 was idle.
5. **Overlap.** Every test is `i.a(aS.W, W)` (i.java:540-558), which
   rejects point boxes; the port used a private plain overlap.
6. **Crush arms.** S0/S4 (offsets 453-506): `aS.aj = aS.ah = 0;
   aS.i(50); return`. S3/S7 and S8-13 (566-587): `aS.i(50)` alone. The
   port zeroed `aj`/`ah` on all of them.

## Fix

`NpcFsm.tickDoor` follows the offsets; `initDoor` ends with
`refreshBoxes()`; the unused `crush` helper is gone.

## Tests

`Slice362Test` (7): last frame's box decides the crush; the ctor box;
mode 1 opens while the ax58 runs and returns once it stops; the promotion
runs only on the resolving tick (and a resting S7 lever promotes); S3
keeps the player's velocity, S0 zeroes it; a point box never crushes. Six
fail on the old code (the S7-lever case is a guard).

## Impact

The m0 '5'-strip corridor: with both boxes one frame behind, the drop
must land at pole phase 0 (press at ph6) — see the capstone's
`dropSafe`. Other capstones are re-validated on the Phase 2 branch.
