---
title: "Slice 347 — ax46 fire-cycle re-pin (aZ() L37: S5/S6 re-apply op24 110)"
phase: "port"
status: "done"
slice: 347
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/simple/i.java:13634-13640
  - reconstructed-project/src/simple/i.java:13680-13682
  - reconstructed-project/src/structured/i.java:13568-13605
---

# Slice 347 — ax46 fire-cycle re-pin

Found while re-validating slice 346 (G9 in the parent plan's phase 1).

## Finding (proven)

- `aZ()` S5/S6/S328 arm (`simple/i.java:13634-13640` L33/L35, `:13680-13682`
  L37): while the player's W overlaps the trap's X box, every tick re-pins
  him — `aS.a(24, 330, 0, this)` for S328, `aS.a(24, 110, 0, this)` for
  S5/S6 — then checks `r()`. op24 snaps the player onto the trap's
  W[0],W[1], so he rides along with the X box as it sweeps.
- Port `ax46Cycle` re-pinned S328 only. A player pinned by an armed S3/S4
  touch stayed where that first pin left him; clip29's S5 X box sweeps right
  over frames 4–5 (`X=[8130,634,8149,664]`, `[8132,632,8168,661]` for the m3
  trap aw189), the overlap failed on the `r()` tick, the arm returned before
  the release, and the trap looped S5 forever with the player stuck in S110.

## Fix

- `NpcFsm.kt` `ax46Cycle`: `if (S == 328) applyHit(24, 330) else
  applyHit(24, 110)` before the `r()` check. With it, S5's last frame
  releases (`i(3)`, player facing flipped) and the next touch on the unarmed
  S3 throws the player (S165), as in the original.

## Test

- `Slice347Test`: S5 re-pin contract (S110, `ak=W[0]`, `al=W[1]`, velocities
  zeroed); full armed-S4 cycle in `I()` order (`s()` → arm → `t()`): pin →
  S5 → release to S3 → unarmed touch throws (S165, trap S7). Without the fix
  both tests fail — the cycle test with "expected <3> but was <5>".
- `Slice282Test` m3 legC/legD (slice 346 re-route, below) pass through this
  trap: pinned at (8111,656), released, thrown, landing at (8228,761).

## Gates

Verifier `ok:true`; `python3 -m unittest discover -s tests` 57/57;
`:core:test` 1605/1605; `:gdx:test` 9/9; `:android:assembleDebug` OK
(together with slice 346).
