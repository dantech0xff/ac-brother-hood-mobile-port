---
title: "Slice 377 — the restart a(z2), step by step"
phase: "port"
status: "done"
slice: 377
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:1619-1625
  - reconstructed-project/src/structured/k.java:1650-1660
  - reconstructed-project/src/structured/k.java:1730-1745
  - reconstructed-project/src/structured/k.java:3257-3280
  - reconstructed-project/src/structured/k.java:3515-3560
  - reconstructed-project/src/structured/k.java:3776-3795
  - reconstructed-project/src/structured/k.java:4165-4174
  - reconstructed-project/src/structured/k.java:5093-5136
  - reconstructed-project/src/structured/k.java:5139-5232
  - reconstructed-project/src/structured/i.java:1795-1832
  - reconstructed-project/src/structured/i.java:1970-2016
  - reconstructed-project/src/structured/i.java:13484-13487
  - reconstructed-project/src/structured/g.java:4421-4423
---

# Slice 377 — `a(z2)`

Found while mining slice 376 (`K()` in `a(true)`).

## The original (proven)

`a(z2)` (structured k.java:5139-5232), `z2` = the checkpoint retry
`a(true)` (death-screen YES, k.java:3790; the S147 fall, g.java:2903),
`!z2` = the full restart `a(false)` (pause-menu restart, k.java:3785):

1. `i = aS.aA` when `z2`, else `i.bV = 0`; `i.bW = false; i.bX = 0`;
   `e.b()` twice.
2. `V(); d(z2)` — the respawn. `d()` runs `i.D()` first (`k.C/D/E =
   null`, i.java:1824-1826), then builds a fresh `aS`; its ax0 arm sets
   `az = 100`, `g.e(k.ax)`, `aA = 2`, `aB = 3`, `Z = {0,0}` and re-creates
   `k.D`/`k.E` (i.java:1970-2016).
3. `if (z2 && (i & 256 || i & 16)) aS.aA |= 256` — the alert carries.
4. The `g.*` link sweep and `C = D = aD = null`.
5. `a(false)`: `X(); I(aj)`.
6. `a(true)`: `K(); o(1)` — `o(1)` is `ap[1]++` (k.java:3257). With a
   checkpoint (`bA[16] != 0`): the ax5 director re-bind, position, `g.J/
   g.I`, `q()`, `ap[0] = bA[36]`, `ap[3] = bA[38]`, `ap[2] = bA[40] << 4`
   (stored `/16`, i.java:13486), `ap[4] = bA[42]`, `ap[5] = bA[52+2aj]`,
   `ax/ay/az/aN/aL`, `aZ`, `i.bn`, `i.br[]`, and `aJ = 1; aK = -40` while
   `aL != -1`. `ap[1]` and `dg` are not touched. Without a checkpoint:
   `L(); F(aj)` and the stash globals.
7. `a(false)`: `L(); F(aj); a(bA,16,0)` and the stash globals.
8. `g.e(ax)` (`x[1] = ax`, g.java:4421), `C(); T(); l(8)`, `B()` when
   `bG >= 0` (`B()` restarts the mission music, k.java:1619-1625).

`l(12)` has no `ap` write (k.java:1650-1660). `de = false` is `W()`'s
(k.java:5131), as are `i.bV/bW/bX = 0` (k.java:5099-5101). There is one
`k.bG` (k.java:310); the death-menu YES sets it to 0 before `a(z2)`
(k.java:3782).

## The port (before)

- `stateL(12)` did `kAp[1]++`; the snapshot restore copied all of `ap[]`
  (so the count reverted to its checkpoint-time value) with an exact
  `ap[2]`, and `L()` ran on every retry (so `dg`, the stats-screen time,
  restarted at 0 after each checkpoint retry).
- The meter was restored to its checkpoint-time value (or 90) instead of
  `ax`.
- `aA`/`Z` of the reused player object carried over; `i.bW/bX/bV` were
  never cleared (a phase checkpoint kept re-stamping after a respawn).
- No `F(aj)`, `q()`, `T()`, stopwatch re-entry, `l(8)` (input reset) or
  `B()`; the menu's `bG = 0` went to a separate `kBG` field nothing read.
- `reload()` cleared `de` (`W()`'s write).
- The grounded player kept `az = 0`, so it drew under every NPC.

## Changes

`Level0World.reload(z2)` is `a(z2)` in the order above
([loadMission] = `a(false)`, `reloadCheckpoint(true)` = `a(true)`);
`resetPlayerToSpawn()` keeps only the respawn (position, the ax0/ax25
init, now with `az = 100`, `aA = 2`, `aB = 3`, `Z = {0,0}` for ax0);
`teardown()` clears `i.bV/bW/bX` and `de`; `kBG` is gone (the menu writes
`kBg`). `deaths` stays as port instrumentation counted at `l(12)`.

## Tests

`Slice377Test` (11). Corrected to the bytecode: the two "reload refills
the meter" assertions (`ax`, not 90), Slice80 (`l(12)` does not feed
`ap[1]`), Slice95 (`de` survives `a(z2)`). Capstones unchanged.
