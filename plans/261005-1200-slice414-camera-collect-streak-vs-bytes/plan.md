---
title: "Slice 414 — the camera (k.m(int), k.D()) and the collect-streak meter (k.s()) re-read from the raw bytes: every bound clamp is an else-if chain"
phase: "port"
status: "done"
slice: 414
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/k.javap.txt (s()V @0-113; D()V; m(I)V @0-2680; g(II)I @25347)
  - reconstructed-project/bytecode/i.javap.txt (ai()Z @74463; the three k.m(ad) callers bi() / aa() / ad())
---

# Slice 414

Slice 413 ended with the `k` static helpers that gameplay code calls. The two camera routines run on
every tick of every mission and decide what the AI sees as "on screen" (`v()` / `inPlayV`), so they
were re-read in full from the raw bytes with `bcdec.py` (pseudo-decompiler over the javap text).

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **`k.s()` collect-streak** | @8-16 `if (ax >= 105) return` right after `az++` | no head return: at the full meter a streak below the top tier re-tiered `ax = 30 + tier * 15` — it could **lower the meter cap** | returns |
| **`k.D()` director corridor scan** (flying / chase missions) | @300 `cG = R; cH = S; goto 440` after the SECOND `22` cell of the player's row | kept scanning and narrowed the corridor with every later `22` | stops at the second |
| **`k.D()` focus** | @93 `ae = aS` every tick | `k.ae` left at whatever a script / grab set | reset to the player |
| **`k.m(int)` scroll-holder bounds** | @1954-2084: `if (cA < W0) cA = W0; else if (cA + 400 > W2) cA = W2 − 400` and the same for `cB` | four independent ifs: a holder narrower than the 400 × 240 view ended on its RIGHT / BOTTOM edge. Level 0's ax37 holder is 350 × 180 — the camera sat 50-60 px off its faithful spot | else-if |
| **`k.m(int)` `R/S` and `T/U` walls** | @2120-2235, same else-if shape | independent ifs | else-if |
| **`k.m(int)` focus box** | @1204-1277 `if (W1 < cB + 40) …; else if (W3 > cB + 200) …` | two ifs (only differs for a box taller than 160 px) | else-if |
| **`k.m(int)` ax43 carrier, mirror branch** (`ae.av == false`) | @1666-1942: 150 % for `dx > 200` or `100 < dx <= 200 && ag == z1 << 8`; `z1` for `50 < dx <= 100 && ag == z1 << 8`; 50 % for `dx <= 50 && ag <= z1 << 8`; else the old speed | `dx <= 200` → 150 % for any `ag`; `dx > 200` needed `ag == z1 << 8` | as the bytes |
| **`k.m(int)` cA pick, rope vs crate** | @315-371: a non-null `g.c` wins even when only `g.a` is the ax43 rope | rope first | `g.c` first |
| **`k.m(int)` `ab` / rope snap-x override** | @2428-2499 is reached from the snap arm too | only after the lerp arm — `ab` stayed set after a snap and fired a camera jump on the next tick | after both |

## Tests

`Slice414Test` (+9): the full-meter return and a normal tier raise; the corridor scan with three `22`s;
`k.ae` reset; a 350 × 180 holder keeps its left / top edge; the `R/S` walls; the seven mirror-branch
speeds; `g.c` vs the rope; the `ab` clear on the snap tick. Mutation-checked: 10 mutants — 10 killed.
No capstone changed: all core tests pass unchanged.

## Verified equal (no change)

`k.m(int)`: the snap head (`aS.t(); ae = aS; n()`), the `ai` freeze, the `aS.c()` freeze (S112-115), the
look-ahead margin `cM`, the `g.g` / `i.at` midpoint targets, the S317 chase margin, the cA / cB centre
state sets, the cB `aZ` / S203 / S204 / S62 gate, the av-true carrier branch, the focus-N watch `X`, the
speed-follow arms, the shake and world-edge clamps, the `av` latch (`i.ai()`); `k.D()` heads, the
two-sided row sweep, the `cA` / `Q` clamps and smoothing; the small helpers `k.e(IIII)`, `k.l(II)`,
`k.n()`, `k.m()`, `k.l()`, `k.r()`; `g.f()`, `g.c()`, `g.d(int)`, `g.e(int)`, `g.f(int)`, `g.g()`, `g.h()`.

## Not done (parked)

`k.l(int)` screen transitions and the UI `k` methods; ax41 `n()`, ax78/45 `bA()`, ax76 `bO()`, ax61 `aR()`
(all but S8), ax34/75 `ak()`, ax64 `bl()`, ax42 `bz()`, ax7 inline, ax5 `aq()`, ax17 `aA()`; the ax11
script-claim head @1700-1951.
