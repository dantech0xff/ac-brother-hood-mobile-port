---
title: "Slice 346 — ax47/ax50 FSM pairing restored (case 47 → aL, case 50 → aK)"
phase: "port"
status: "done"
slice: 346
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:20024-20025
  - reconstructed-project/src/simple/i.java:5181-5194
  - reconstructed-project/src/simple/i.java:9910-10099
  - reconstructed-project/src/simple/i.java:2255-2406
  - reconstructed-project/src/structured/i.java:3437-3447
---

# Slice 346 — ax47/ax50 FSM pairing restored

Phase 1 item 1.1 (G1) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Finding (proven)

- The original entity dispatch sends `case 47 → aL()` and `case 50 → aK()`
  (`simple/i.java:5181-5194`; bytecode `i.javap.txt:20024-20025`:
  47 → 7680 `invokespecial aL`, 50 → 7687 `invokespecial aK`). Neither
  method has another call site.
- `aK()` (`simple/i.java:9910-10006`) is the pouncer: S120 perch
  (ceiling-grab → S119, else `l()` seen → 3×3 pounce pick 121–128),
  S121–128 pounce, S130 despawn. `aL()` (`:10008-10098`) is the ledge
  sentinel: S93 ceiling-grab → S80 + claim, S81 countdown → S82 → S80,
  S83 air-walk → S84 → S0, S94 claim release + despawn.
- Slice 64 transcribed both bodies correctly but bound them the other way
  round: `tickAx47` held the `aK` body and `tickAx50` the `aL` body. The
  retype (`Level0World.kt:842-846`), the `k()` grab-kill arms
  (`NpcFsm.kt` ax47 S80 → 94, ax50 S119 → 130) and the `C()` death anim
  (ax50 → 129) already assumed the original pairing.
- Data: every sentinel record spawns at S93 (m0 4, m2 2, m3 3, m5 3) and
  every pouncer at S120 (m3 3, m5 5) — states the swapped FSMs had no arm
  for, so none of the 20 ever grabbed or pounced.

## Fix

- `NpcFsm.kt`: the `aK` body is now `tickAx50`, the `aL` body `tickAx47`
  (dispatch lines in `Level0World.kt:5432-5433` unchanged). KDoc and the
  slice-64 header now state the original pairing with the bytecode cite.
- `aK`'s `r7 = l()` now uses `seen50`: `l()` switches on `ax`, and for
  ax50 takes `case 50 → L77` — `bn` → false, else `b(W, k.ac)`, which is
  **containment** of W in the camera rect (`simple/i.java:665-676`), then
  the shared LOS / player-S∉{284,285} tail. The old call used the default
  arm L83 (`seen47`, a degenerate point check), which no caller reaches
  after the fix — removed.

## Consequence worth knowing

- With the original pairing the pounce's `aS.a(4,…)` hits the op4 arm with
  an ax50 attacker, which is in the `r13.ax ∉ {17,50,61}` exclusion
  (`structured/i.java:3437-3447`): no `r9.c(r13)` stagger, only the
  `k.A(18)` hurt sfx. The S9 stagger slice 64 asserted only happened
  because aK was driven through ax47.

## Related finding (not fixed here)

- `losL`'s `17, 23, 50` arm (`NpcFsm.kt` ~10472) uses `overlapI(W, ac)`,
  but the original L70/L77 call `b(W, k.ac)` — containment. Affects ax17/23
  sight in `spotB` and the L827 tail. Tracked as G8 in the parent plan.

## Test

- `Slice64Test` rewritten to the original pairing: aK arms through ax50 +
  `tickAx50`, aL arms through ax47 + `tickAx47`; new pins for W-containment
  vs overlap, off-camera, `bn`, player S284/S285, aL S0/S93 returning
  before `k(); j()`, and the op4 exclusion for ax50.
- `Slice346Test` (new): mission-3 retype states; real `Level0World.tick`
  dispatch — an in-view pouncer leaves S120; a sentinel at S93 catches a
  falling player (S80, player S89, claim armed).
- Against the pre-fix `NpcFsm.kt` the new tests fail (14 in `Slice64Test`,
  2 in `Slice346Test`); against the fix they pass.

## Capstone impact (m3 legC / legD)

- With this slice alone, `Slice282Test.mission3CapstoneLegC/LegD` failed:
  the carrier hop at x≈7410 now crosses the three m3 sentinels (aw 923, 251,
  842 at x 7415–7614, y≈690), which catch the player (S89). The bot's
  existing `89 → M_CONTEXT` policy kills each one through the `k()` ax47
  arm (S94, `gP` bonus); the S157 release leaps east into the next, and
  after the third the player lands on the upper path (7744,679) — then the
  ax46 trap aw189 (8090,629) pinned him in S110 forever.
- That pin is a separate port gap, fixed in
  [slice 347](../261003-0730-slice347-ax46-fire-cycle-repin/plan.md). With
  it both legs pass **unchanged**: sentinel chain → upper path → trap
  pin/release/throw → grounded at (8228,761). Before slice 346 the bot
  never met the sentinels and stayed on the carrier line (y≈805).

## Gates

Verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
`:core:test` 1605/1605; `:gdx:test` 9/9; `:android:assembleDebug` OK
(with slice 347). Device check of the sentinel/pouncer behaviour is
Phase 2 work (no emulator in the cloud container).
