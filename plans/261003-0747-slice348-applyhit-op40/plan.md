---
title: "Slice 348 — applyHit op 40: the clip-27 prop bump (L96)"
phase: "port"
status: "done"
slice: 348
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:18662-18697
  - reconstructed-project/src/simple/i.java:4733-4760
  - reconstructed-project/src/structured/i.java:3615-3624
  - reconstructed-project/src/structured/i.java:16426-16441
  - reconstructed-project/src/simple/g.java:135-137
---

# Slice 348 — applyHit op 40

Phase 1 item 1.2 (G2) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Finding (proven)

- `i.a(40, 0, 0, r13)` (L96, `simple/i.java:4733-4758`; bytecode
  `i.javap.txt:18662-18697`, switch target 799):
  `S == 3 → return`; `!g.a() → return`; `!o() → return`; then
  `g.b = r13; aB = 3; i(3); av = r13.ak < ak; ag = av ? 512 : -512`.
  `g.a()` is `a(g)` (`simple/g.java:135-137`) — the hit gate that drains the
  player when it passes, so an `o()`-false player (S2, S20..29) still loses
  health but gets no bump. Unlike op38, the `o()` miss exits the arm.
- Sole caller: ax67 `bB()` for `k.bk[Z[0]] == 27` props in
  S19/21/23/32/35/38 (`structured/i.java:16426-16441`): `aS.ah = 768 + k.Y`,
  `aS.a(40,0,0,this)`, `i(S+1)`. The port ported the caller
  (`NpcFsm.kt` `tickDecor`) but `Entity.applyHit` had no `40` arm.
- Data: 32 armed clip-27 props, all in the flight missions — m1 21
  (pack-7), m4 11 (pack-10). In flight, S3 is the L137 glider bump arm
  (`PlayerFsm.flightTick`: anim end → S4, glide tail).

## Fix

- `Entity.applyHit`: `40 ->` arm with the three exits in bytecode order,
  `playerDamageable(g, world)` for `g.a()`, `oState()` for `o()`,
  `world.playerLinkB` for `g.b`.

## Test

- `Slice348Test` (5): positive arm (S3, aB 3, link, facing, ±512 both
  sides, drain + 10 iframes); S3 and invulnerable exits; `o()`-false drains
  without a bump; the real `tickDecor` clip-27 path (ah = 768 + kY, op40
  bump, prop S20); m1/m4 armed-prop counts (21/11).
- Without the arm, 3 of the 5 fail (positive, `o()` drain, `tickDecor`).

## Capstone impact

- m1 capstone (Slice280Test) never touches an armed prop: identical
  player-S3 count (8) and final health (x1 5) with and without op40.
- m4 capstone (Slice288Test) takes one prop bump (S3 transitions 16 → 17,
  `g.b` → the prop); final health unchanged (x1 10). Both still pass.

## Gates

Verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
`:core:test` 1610/1610; `:gdx:test` 9/9; `:android:assembleDebug` OK.
Device check (glider bump in m1/m4) is Phase 2 work.
