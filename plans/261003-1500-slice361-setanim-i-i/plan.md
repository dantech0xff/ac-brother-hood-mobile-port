---
title: "Slice 361 — Entity.setAnim is i.i(int) and enterStateMasked is i.a(int,int)"
phase: "port"
status: "done"
slice: 361
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (i(int) offsets 0-299)
  - reconstructed-project/bytecode/i.javap.txt (a(int,int) offsets 0-124)
  - reconstructed-project/src/structured/i.java:240-279
  - reconstructed-project/src/structured/i.java:684-716
  - reconstructed-project/src/structured/i.java:8615-8636
  - reconstructed-project/src/structured/g.java:126-133
  - reconstructed-project/src/structured/g.java:2373-2378
---

# Slice 361 — `i.i(int)` and `i.a(int,int)`

Found while tracing the m3 leg-H S29 hang on the Phase 2 branch.

## Findings (proven, bytecode)

`i.i(int)` (offsets 0-299) — the port's `setAnim` had only the
state-change block:

1. `k.aU == this && i == 0 && by == 3 → i = 36` (0-20): the phase-3 boss
   never idles in S0.
2. ax29 `i(27)` → `e(15, ak, al, az-1)` (88-120): the `ck` aura
   (`bossAura` in the port, `i.java:8615-8636`).
3. ax43 `i(11)` → `ai = ag = aj = ah = 0` (123-155).
4. Player slot, **on every call** (158-233): `i(50) → g.e(0)`;
   `i(43|148|0) → g.y = al`; `i(43)` while S61 → `al += W[3]-W[1]`. The
   port ran the first two only on a state change and lacked the third.
   (The S60/61 drop arm also shifts explicitly before `a(0)` —
   `g.java:2373-2378` — so an S61 drop shifts twice, as in the original.)
5. A real change (236-299) also clears the `y` anim-freeze counter.

`i.a(int,int)` (offsets 16-124): the x snaps (2048 / 4096 / 4 / 8) are one
else-if chain; the port applied them independently (no current literal
mask combines them).

## Fix

- `Entity.setAnim` follows the offsets; `Entity.hostWorld` (bound by
  `Level0World`'s last init block, cleared while the pack loads) supplies
  `k.aU`, `i.by` and the aura's world.
- `enterStateMasked`'s x snaps are an else-if chain.

## Tests

- `Slice361Test` (7): `y` reset only on a change; `g.y`/`g.e(0)` on every
  call; the S61 → S43 shift; ax43 stop; the by-3 boss remap (only `k.aU`,
  only at by 3); the ax29 aura; the x-snap chain. All fail on the old code.
- `Level0WorldTest` "by3 exhaust…": `i(0)` is now `i(36)` for the boss.
- `Slice193Test` "S61 grip expiry…": 150 (two shifts + 10), not 130.

## Gates

`:core:test` 1679/1680 on the Phase 2 branch (the remaining failure is the
m3 leg H, unchanged by this slice).
