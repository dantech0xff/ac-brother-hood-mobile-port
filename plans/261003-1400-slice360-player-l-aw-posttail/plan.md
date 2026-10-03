---
title: "Slice 360 — g.l(), g.aw(), the e() head aA raise and e()'s post-tail follow the bytecode; one k.bD"
phase: "port"
status: "in_progress"
slice: 360
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/g.javap.txt:14679-14915
  - reconstructed-project/bytecode/g.javap.txt:2425-2460
  - reconstructed-project/bytecode/g.javap.txt:8505-8830
  - reconstructed-project/src/structured/g.java:5158-5283
  - reconstructed-project/src/structured/g.java:4336-4338
  - reconstructed-project/src/structured/k.java:1590-1610
  - reconstructed-project/src/structured/i.java:240-279
  - reconstructed-project/src/structured/i.java:18245-18262
---

# Slice 360 — the player's grounded input and e()'s tail

Found while re-validating the m0 capstone on the Phase 2 integration
branch `claude/phase2-faithful-ai`: the bot stood in S0 facing a soldier
while holding the other direction and never turned.

## Findings (proven, bytecode)

`g.l()` (g.javap.txt l() offsets 0-588; the structured view agrees, the
simple view drops the jump after L124):

1. `k.aT → return true` (0-7): claim op 8 sets `k.aT` when the camera
   focus target is not the player (i.java:18252-18262). Missing.
2. `bM = null` (8-10). Missing.
3. S79: `i.bn → cq = z = false; return true` (190-205). Missing (bn is
   false today).
4. `g.ax == 73 && g.Z[0] == 3 → cq = false` (232-262). Missing.
5. Opposite direction held: `aA != 0 → turn`, else **`k.bD > 4 &&
   !x(dir) → turn`** (311-332, 432-453), else a double-tap dash. The
   hold-to-turn was missing — and the player's `aA` is 0 whenever a
   soldier's op 32 (stance break, i.java:4639-4652) hits it, so in a fight
   the port's player could never turn by holding a direction.
6. No direction: `(S12 && co >= 4) || S11` → `S11 && r() ? aw() : i(11)`
   (535-583; `i(11)` on an S11 is a no-op, i.java:268). The port sent S11
   into the right-arm tail (a simple-view misread), which ran an extra
   S12 tick facing east.

`g.aw()` (structured g.java:5238-5283, matching the call layout):

7. `S != 6 && u(16388) && f() → i(6)` (UP with a carry target in reach).
   Missing.
8. DOWN: `aS == 9 → a(122, 8)`. Missing.
9. DOWN at a thin edge → `a(257, 8)`; the port fell back to a plain
   `a(0)` fall.

`g.e()`:

10. Head (2425-2460, offsets 449-486): `k.aA > 0 ? (aA > 1 → aA |= 1) :
    (aA <= 1 → aA = 2)`. The else arm was missing.
11. Post-tail (offsets 14349-15124) runs, in order: `o() → ao()`; `aA == 0
    → 1`; `aA & 4` → clear it **and skip `ap()`**, else `z && !bn && !E →
    ap()`; `aO 7/9 → cq = false`; the `A` latch (`i(148)`, return); `f()`
    and `ac` 51/66 → `cq = false`; then the jump tail under `cq && !E`
    with the back-dash in its else; the flag consumers. The port ran the
    jump tail before the equip/`aA`/`ap()` block, called `ap()` even on
    the bit-2 tick, let the back-dash run without `cq && !E`, lacked the
    ax66 S11/S12 facing return (14673-14745) and the `S != 233 → ag = 0`
    after a jump press (14821), and ran the ledge consumer without `cp`
    (14893).
12. `mountEntry` repeated the `aA` bookkeeping; the L1947 block falls
    into the post-tail (14349), so the copy is gone.

`k.bD` is one static: the input commit's hold counter (k.java:1599-1604),
which the flying bank anims also read and bump (g.java:5701-5776). The
port had a second, never-reset `world.kBD`; it now delegates to `pad.bD`.

## Tests

- `Slice360Test` (10): hold-to-turn at `bD` 4/5 with a broken stance;
  the release brake never runs on (both ways); `k.aT` freeze; `bM` clear;
  `aS == 9 → S122`; UP + carry target → S6; thin edge → S257; the head's
  raise to 2 (and none under alert); bit 2 skips `ap()`; one `k.bD`. Nine
  fail on the old code.
- `Slice180Test` "bh3 gate keeps grounded dispatch": `aA == 2` is now the
  grounded default, so the test checks the ax25 init's `az`/clip instead.

## Impact

The m0 end-to-end capstone and m5 leg B pass again on the integration
branch (the chamber guard no longer pins a bot that cannot turn). Other
legs are re-validated with the rest of Phase 2.

## Follow-up

- Task: map g.e()'s ~70 early `return`s to the port's arms — the port
  runs `postTail` after every arm.
