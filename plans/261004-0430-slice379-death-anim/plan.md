---
title: "Slice 379 — the death screen waits for the death animation"
phase: "port"
status: "done"
slice: 379
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/g.java:529-640
  - reconstructed-project/src/structured/g.java:2200-2219
  - reconstructed-project/src/structured/g.java:4405-4432
  - reconstructed-project/src/structured/g.java:5604-5649
  - reconstructed-project/src/structured/g.java:5833-5841
  - reconstructed-project/src/structured/i.java:240-282
  - reconstructed-project/src/structured/k.java:2516-2650
---

# Slice 379 — no knockout check in `k.I()`

Found while mining slice 376.

## The original (proven)

- `g.g()` = `x[1] <= 0` (g.java:4432). Its only state-changing readers are
  the player's heads: `g.e()` `if (g()) i(50)` (g.java:576-578) on the
  ground and `g.n()` `else if (g()) i(2)` (g.java:5648-5649) in flight.
  `k.I()` has no `x[1]` check.
- `k.l(12)` comes from the death arms: S50/S241 `if (r()) k.l(12)`
  (g.java:2200-2219, after `d(999)` + `k.A(18)` on its second frame), the
  flyer's S2/S24 `if (r() || !v()) k.l(12)` (g.java:5833-5841), the S374
  crush (g.java:4220) and script/`i.B()` sites.
- `i(n)` restarts the anim only when `n != S` (i.java:240-282), so the
  head's per-tick `i(50)` lets S50 run out.
- `g.e()`'s head only runs while no claim holds the player
  (`k.C == null || aS.P & 512`, g.java:537), so a meter that empties
  during a cutscene kills once the cutscene ends.

## The port (before)

The play path ran `if (player.x1 <= 0) stateL(12)` after the sim (from
the slice-151 import): the fail screen opened the frame the meter hit 0
and S50/S2 never played.

## Changes

The check is gone from `simI()`. The existing S50/S241 and S2/S24 arms
open the fail screen.

## Tests

`Slice379Test`: S50 runs to its end on a live world before `l(12)`.
Re-timed to wait for the death screen (`tickUntilFailed`, bounded): the
knockout, reload, checkpoint and crush tests in Slice1Test, ReproDieLoop
(whose meter assertion now reads `ax`, per slice 377), and Slice323's
watcher test, which only passed because the instant death froze the
world just after script 309 bound — it now checks the claim and the
trio's formation while the claim still holds (the script ends by
putting the player in S50). Capstones unchanged.
