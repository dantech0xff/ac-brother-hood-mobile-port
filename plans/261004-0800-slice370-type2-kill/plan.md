---
title: "Slice 370 — the type-2 kill and the type-6 hit at the head of g.e()'s shared tail (F1), and the ax72 orbit setup"
phase: "port"
status: "done"
slice: 370
date: 2026-10-04
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt (e() 13629-13739; c(i) 346-460)
  - reconstructed-project/bytecode/i.javap.txt (L() 0-25)
  - reconstructed-project/src/structured/g.java:4421 (static e(int): x[1] = i)
---

# Slice 370 — F1 of slice 365

## Finding (proven)

`g.e()` 13662-13736, the head of the shared tail every `goto 13629` arm
reaches:

```
13662  if ((aR == 2 || aO == 2 || L()) && g.a == null) {       // @13667-13685
           ah = 0; aj = 0; g.e(0); i(50); return              // @13691-13711
       }
13712  if (aO == 6 || aR == 6) a(18, 0, 0, this)              // @13713-13736
```

`L()` is `e(ak/20, al/20) == 2` (i.javap.txt L() 0-25); `g.e(0)` is the
static `x[1] = 0` (the sync meter); `g.a` is the platform the player stands
on. A type-2 cell under the feet (`aR`), at the head (`aO`) or at the anchor
kills an unmounted player and the `return` skips the J&4 block and the
post-tail. Type 2 is a pit/water-like hazard: `k.g()` returns the raw `et[]`
(written once at load, k.java:5254-5270), non-support (`aR >= 12 || aR == 5`
is the floor test), so a walker falls through it onto the solid row below —
the anchor then sits in the type-2 row. The `g.a == null` clause is what a
lift or gondola over such a strip is for.

The port had `if (aR != 2 && aO != 2 && L() != 2) { if (aO == 6) hit }` —
the simple decompile (g.java:3465-3473) drops the jumps; slice 187's test
encoded that reading ("type-2 suppresses the aO==6 hit").

## Changes

- `PlayerFsm` L353d: the kill (`ah = aj = 0`, `x1 = 0`, `i(50)`, return
  false = no J&4 block, no post-tail) and the type-6 hit for `aO == 6 ||
  aR == 6` (the port checked `aO` only).
- `grabLunge` (`g.c(i)` 346-460, found while re-routing m7): on an ax72
  target `Z[0] ∈ {0,3,4}` take the `Z[1]`/`Z[2]` `cF`/`cx` overrides
  (355-432), `Z[0] ∈ {1,2}` — both wheel configs — reset the orbit phase
  `cM = 0` (435-457), any other `Z[0]` does neither. The port reset `cM`
  for `Z[0] == 1` only, so a second `Z0 == 2` wheel kept the first one's
  phase and skipped its spin-in.

## Tests

`Slice370Test` (5): `L()` kills on a type-2 strip on the floor, a type-2
head cell kills, a mounted player is spared, `grabLunge` per `Z[0]`
(0..5), a type-6 cell under the feet fires op 18. Four fail without the
fix (the mounted-player guard passes either way). `Slice187Test`'s
"aO6 suppressed on type2 ground" is replaced by "type2 cell under the
feet kills before the aO6 hit".

## Capstones (routes and verdicts only — no enemy, door, timer or state touched)

The shipped type-2 strips sit on top of solid rows in every level (level 0:
127 cells; 2: 361; 3: 341; 5: 109; 6: 182; 7: 94). Before this slice the
bots walked them.

| Test | Verdict | Change |
|---|---|---|
| `Slice245Test` m0 end to end | re-routed | the two 80px gaps in the y760 walk line (x6400-6479, x6700-6779) drop into pits with a type-2 strip (row 42): jump them from the S26 lip walk |
| `Slice281Test` m2 leg C (gap in `20`@1940, x2420-2519) | re-routed | jump the 100px gap (row 102 strip) from the S26 lip walk |
| `Slice281Test` m2 leg B | faithful dead end | its designed start is the r67 strip (cols 102-116) on r68: the leg now pins the strip and the kill |
| `Slice301Test` m7 leg F, `Slice302/304/306Test` | re-routed | the lift-row crossing is the ax72 wheel uid36 (`Z[0]==2`): CONTEXT → `c(i.at)` lunge → mount → band-edge fling → the spring pad (`gJ = 5`, the f0do mask the real play entry applies via `k.F(aj)`) |
| `Slice303Test` m7 upper wing | faithful dead end (the descent) | asserts the wing chain and the strip kill on the descent (the ride ends on r77's strip) |
| `Slice308Test` m7 leg L | faithful dead end | pillar top (r27, cols 15-17) and band (r28, cols 18-51) are type-2 strips: the designed start dies on its first tick; pinned |
| `Slice310Test` m7 leg P | re-entered | starts at the arena floor's west lip (1060,519), 60 camera-settle ticks; the win chain is unchanged |

The wheel crossing is the strongest sign the kill reads the original
right: level 7 gives the player a designed way over the lethal lift row.

## Open (not closed by this slice)

The "faithful dead end" legs no longer prove a route in the port: m2 leg B,
m7's pillar-top approach and the upper-wing descent. The original must
have a non-lethal way (a ride — `g.a != null` is immune — or a safe take-off
point); finding it needs route research or a device play-through. Until
then m2 and m7 are covered by legs, not by a continuous run.
