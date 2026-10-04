---
title: "Slice 408 — ax27 fuse barrel bL(), the flying runners ax54/ax56, the ax46 spring pad and the shared push helper re-read from the raw bytes"
phase: "port"
status: "done"
slice: 408
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt (bL() @0-1203, ax() @0-1258, ay() @0-1134, aZ() @0-817, a(i,int,int[]) @51313, I() dispatch tail @8032-8051)
---

# Slice 408

Census of the shipped levels (entity records per `ax`, all eight missions): ax67 1286, ax74
602, ax44 442, ax14 339, ax37 201, ax11 188, ax55 184 (no tick), ax4 180, ax10 124, ax66 114,
ax5 95, ax2 50, ax22 43, ax27 41, ax54 41 (+2 ax30), ax15 35, ax46 34, ax35 28, ax56 27,
ax19 22 … The audit goes down that list; this slice covers the not-yet-verified ones.

Verified equal (no change): ax67 `bB()`, ax74 `bN()`, ax14 `aX()`, ax5 `aq()` (all 20 `Z[1]`
modes), ax2 `aY()`, ax22 `aN()`, ax19 `aO()`, `b(boolean)` (the `ad` mirror).

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **ax27 S1/S21** | `bL()` 366-458: `G(); if (r() \|\| (aS.az == -2 && T == last)) { T = last; U = 0; if (aS.az == -2) { aS.i(269); aS.az = 100 } }` then the shared tail @1119 | the arm continued into the S4 fuse arm: a lifted barrel next to an open ax58 started its fuse (`i(6)`, `k.aD`); entering through the pinned-last-frame door skipped the `i(269)` hand-over | exact arm; the fuse arm is only S4/S23 |
| **ax27 S4/S23** | 745-759 `k.aD = this` | `if (kAD == null)` — an earlier owner was never replaced | unconditional store |
| **ax27 S0 marker** | 283-356 `ae == null \|\| ae.S != 7` → `G(); a(7, ak, al-85)`, then pin `ae` | respawn for a null `ae` only | both |
| ax27 S6 | 801-804 `Z[1] == 0 → return` (no tail) | fell into the tail (a no-op: `P&4096` is cleared one line above) | `return` |
| **shared push helper** | `a(i,int,int[])` 179-191 `if (r1.aZ && ah > 0) return` | the ax27 tail used a private copy (`pushApart`) without that return and fell into the horizontal exits; the dispatch tail had a second, correct copy (`pushL897`) | one helper, both callers |
| **ax54/ax30 walk + attack facing** | `ax()` 440-496 / 595-653: `F == null ? Q() : av = F.a < ak`, then S0/S4 (walk) resp. S5/S9 (attack) force `av = false` | forced S4 only, `F.a < ak` for S0 alone — S1-S3 and S6-S8 never turned toward the companion | exact |
| **ax54 companion pin** | `ax()` 938-1159: the `F.a/b = ak/al + h/i` store sits inside `if (B)` | pinned every tick | inside `if (runnerB)` |
| **ax56 walk + attack facing** | `ay()` 220-276 / 823-881 | the same two-case shape | exact |
| **ax46 spring pad** | `aZ()` 694-739: `if (S == 11) i(12) else if (S == 0) i(1)` | `i(S == 11 ? 12 : 1)` — a sprung S12 pad turned into S1, a springing S1 restarted every tick | exact |

## Tests

`Slice408Test` (19): ax27 S1 (no fuse arm / pinned door / waiting / finished), S4 `k.aD`
takeover + odd ax58 state, S0 marker respawn + keep, the shared helper through both callers
(aZ-rider return, plain lift), ax54 walk/attack facing (all five S per shape), ax54 without
a companion (Q), ax56 both arms, ax54 companion pin travelling / not, ax46 spring.
Mutation-checked: 11 mutants (every fix reverted once) — 11 killed.

## Not done (parked)

ax35 `bQ()` (3.2 KB multi-tool, 28 records), ax72/78/79, ax9 `bM()`, ax58 `bg()`, ax43 `bw()`,
ax60 `bj()`, ax61 `aR()`, ax6 `an()`, ax42 `bz()`, ax8 `ar()`, ax37 `al()`, ax41 `n()`,
ax64 `bl()` (not in any shipped level), ax76 `bO()`; `applyHit` ops 39/40/41; the ax11
script-claim head @1700-1951.
