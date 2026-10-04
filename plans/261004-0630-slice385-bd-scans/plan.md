---
title: "Slice 385 — sim neighbour scans walk k.bd, the last paint's draw list"
phase: "port"
status: "done"
slice: 385
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:2492-2505 (d(i) sorted insert)
  - reconstructed-project/src/structured/k.java:2861-2902 (b() list build)
  - reconstructed-project/src/structured/k.java:5121-5126 (W() nulls bd[], keeps be)
  - reconstructed-project/bytecode/i.javap.txt (n() @36-558, be() @0-123, c(Z) @703-801, bj() @1163-1338, bl() @2783-2915, br() @0-380, bu() @1151-1712, bQ() @1520-1754)
---

# Slice 385 — `k.bd` is the domain of every sim neighbour scan

## Finding (proven)

`k.bd[]`/`k.be` is filled only by `b()` (`be = 0`, then `d(i)` for each
drawn entity — `(P&128)==0 || ax∈{10,51}`, `v()`, `bh!=3 || ay==-1`, the
`P&16` arms — then the player and its `ae`, k.java:2861-2902). The sim
methods of `i` scan `for (i = 0; i < k.be; i++) … k.bd[i]`, so they see
the list the LAST paint drew: on-screen entities only, the player and the
`ae` children included, `az` order, and an entity removed since keeps its
slot until the next `b()`. `W()` nulls every slot and leaves `be`
(k.java:5121-5126). The port scanned `npcs` (the `bb[]` pool) in all of
them, and the renderer rebuilt the list every frame.

## Changes

- `LevelCellSource.drawn` — `bd[0 until be]`, read live, null slots
  skipped. Scans moved to it: `n()` S4/S6 (ax41), `aj()` S29 (ax4),
  `n(44,0)` (ax73), `bc()`/`bd()` (ax16/ax24), `be()` (ax58), `bj()` +
  `c(Z)` (ax60), `bl()` escape (ax64), `br()` (ax66/ax51), `bu()` (ax15),
  `bx()` (ax40), `bB()` (ax67), `bC()` (ax69), `bL()` (ax27), `bQ()`
  (ax35). Scans of `bb[]` stay on `npcs` (`i.ae()`, `bt()`, `aV()`,
  `k.H()`, the tick loops).
- The renderer draws the list the world's last `b()` pass built — no
  rebuild (it would also change what the next tick's sim reads).
- `W()` (`teardown`) nulls the slots, keeps `drawCount`.

## Bytecode fixes met in the converted methods

- `n()` S4 (@201-237): ax11 → `s == null || s.ax != 51 → i(0)`; the port
  read an `i(7)` arm (the dead case 15).
- `n()` S6: `bb || bc` bounce (@362-382); `i(aS)` returns whether or not
  the prop moves (@404-441); no `this` skip — a drawn moving prop meets
  itself (`i(this)`) and settles (@442-551).
- `be()` (@113): the first overlapping ax11/15 is marked, then `return`
  (the port marked all).
- `c(Z)` (@792): the S13 hand-off stops at the first S11 member.
- `bl()` escape (@2879-2896): `bd[i].cz = this.aa.b(this.S)` — the
  scanner's anim length, not the entry's.
- `br()` (@345-372): every bind reports true except a held ax51 (S8),
  which ends the scan; the port dropped the ax51-not-S8 case.
- `bu()` (@1216-1222): the door collapse `i(7)` ends the sweep.
- `bQ()` S6-9 (@1525-1613): S∈{0,1,7,12} flinch `i(9)`, then `g.d` +
  `g.t = 5` for every pose; the port's S12 branch (VolPaint + `i(3)`,
  no drain) is not in the bytecode.

## Tests

`Slice385Test` (10). Unit tests that place neighbours by hand now
"paint" them (`Level0World.paint(...)`, Slice1Test.kt); the n() S4 ax11
assertion was a misread and follows the bytecode. The Slice245 flight
probe holds the camera on the S19 column until it arms (its teleport left
the drift camera ~600px below; only drawn entities are swept).

## Follow-ups (not in this slice)

- `g.az()` (PlayerFsm `interactScan`) still scans `npcs`.
- Slice 387: `k.c()` runs `p()` at once; `bt()` sweeps the ALIVE
  (`!P()`) enemies — the port's `sweepHostiles` is inverted; `b()` ax21
  S1 `ad.s()` is unconditional; `bu()` S10 falls into the S6/S8 body
  (no `goto` after `bt()`, @54-69).
