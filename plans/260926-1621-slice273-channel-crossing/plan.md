---
title: Slice 273 — S38 '5'-lip mantle fix + full channel crossing verified
phase: port-fidelity
status: complete
---

# Slice 273 — S38 shimmy-arm inversion fix; channel crossing proven end-to-end

## Summary

Two things land together because the second proves the first:

1. **Real fidelity bug fixed** — the S38 '5'-lip hang tail had the shimmy
   arm inverted: `!u(facing) → i(37)` so any UP-only input at a '5' edge
   fed the else-chain and overwrote the S54 mantle `enterStateMasked(54,8)`
   the UP arm had just run. '5'-edge mantles were impossible.
2. **The channel crossing (x1734→x2594) is now driven end-to-end by the
   capstone bot** — shaft drop-through → chamber → dip '5'-lip mantle →
   ax22 vault chain → wall-C lip → wall-C top → plateau-face hop → cp2.
   `maxAk=2711, deaths=0` at tick limit (fighting the plateau guard).

## Source

`g.java` S38 hang arm (re-verified line-by-line): L2b7a spring-jump arm →
L2bc0 grapple; `aO != 5` → `al=W[3]; i(43)` drop; `aO == 5` hang →
`al=(W[1]/20)*20+10` snap; `u(16388)` UP → `G(); al-=20; x(); al+=20;
aO==0 → a(54,8)` S54 mantle (open cell above the lip = edge); `u(33024)`
DOWN → probe-below drop `i(43)`; `av ? u(4112) : u(8256)` = facing-dir
HELD → `i(37)` shimmy (L2cb7); `av ? v(8256) : u(4112)` = away-key → flip
`av` (L2cdc — asymmetric edge/held, verbatim).

Buggy port (`PlayerFsm.kt`): `!pad.u(toward) → setAnim(37)` + flip keyed
`pad.u(LEFT)/pad.v(RIGHT)` with the wrong edge/held order.

## Changes

- `PlayerFsm.kt` (~:856): shimmy arm `pad.u(toward) → S37`; flip arm
  `av ? pad.v(RIGHT) : pad.u(LEFT) → av=!av`. 8 lines.
- `Slice1Test.kt` Slice89Test: rewritten `5 lip hang shimmies along the
  bar` for verbatim arm semantics (away-dir → flip only, then facing-dir
  → S37); comment updated to note S54 needs a lip EDGE (open cell above),
  dead on a bar. New regression test `5 lip edge hang mantles via S54 on
  UP` — spawns rising into the dip-gap west lip (x2109, '5' cy40),
  asserts S280→S38→UP→S54/S0/S62 and `assertNotEquals(37, p.S)`.
- `Slice1Test.kt` Slice245Test capstone `bot completes mission-0 end to
  end`: new top-state handler `S in {280,38,54} → pad.e(M_UP)`; the
  x1560-2500 zigzag policy replaced with the proven route (shaft floor
  DOWN drop-through / chamber east + dip UP|LEFT / valley floor west +
  UP|LEFT / wall-C top UP|RIGHT hop); assert raised to `maxAk > 2600`.
- Cleanup: removed 52 scratch dump/probe tests (46 from this segment's
  channel-solving work + 6 that had leaked into slices 271/272 commits).

## Verified

- `bot completes mission-0 end to end` — PASSES: `won=false deaths=0
  maxAk=2711 minAl=358 t=140001`. Player crossed the channel via the dip
  mantle → ax22 chain → wall-C → plateau; fighting ax11@2647 at limit.
- `5 lip edge hang mantles via S54 on UP` — PASSES.
- `5 lip hang shimmies along the bar` — PASSES (verbatim semantics).
- Gates: verifier `ok:true`; `python3 -m unittest discover -s tests`
  57/57; `:core:test` 1485/1485; `:android:assembleDebug`; `:gdx:build`.

## Confidence

proven — every arm cited to `g.java` fallback line ranges above; the
route legs were each observed live in scratch probes before being wired
into the capstone (cell map and zone coords recorded in the summary).
