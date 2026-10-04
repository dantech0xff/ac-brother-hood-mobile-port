---
title: "Slice 409 — the j.a/j.b Bézier (ax74 wisp flight, ax61 S8), the ax43 hook, ax9 and ax40 re-read from the raw bytes"
phase: "port"
status: "done"
slice: 409
date: 2026-10-05
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/j.javap.txt (a(IIIIIII)V @0-60, b(IIIIII)I @0-20, clinit i/m/n)
  - reconstructed-project/bytecode/i.javap.txt (bw() @0-534, o(i) , bM() @0-653, bx() @0-1021, bN() @0-729, aR() @104)
---

# Slice 409

Continues the entity audit (slice 408) with the remaining ones whose port comments only
cite the decompiled `i.java:NNNN` and not the bytes.

Verified equal (no change): ax35 `bQ()` end to end (28 records, 3.2 KB), ax58 `bg()`, ax6
`an()`, ax8 `ar()`, ax42 `bz()`, ax7 (inline @973-1250), `g.ay()` (the combo advance step — the
port's "decompiler gap" comment was right), and the numeric helpers `k.h(II)`, `k.e(IIII)`,
`i.b(IIII)`.

## Fixed (`proven`, raw bytes)

| what | bytes | was | now |
|---|---|---|---|
| **Bézier `j.a`/`j.b`** (ax74 wisp flight S3/S6, ax61 S8 knife arc) | `j.b(IIIIII)I`: `p0·p4 + 2·p1·p3 + p2·p5` over `idiv 65536`, `j.a` passes `p3 = (i−t)·t`, `p4 = (i−t)²`, `p5 = t²`, `j.i = 256` | the port read `a·(i−t)t + 2b·(i−t)² + c·t²` ("the weight order is NOT the textbook row" — it is): at t = 0 a flying wisp sat on **twice its control point**, and the curve never started at its start; ax61 S8 had its own 65536-domain copy whose `ti²` overflowed `Int`, throwing the first third of the flight to the screen corner | one `Trig.bezier` (textbook weights, 256 domain, truncating `idiv`) for both |
| **ax43 `bw()` claim step** | @0-11 `if (ab()) aa();` and then the switch | `return` after `aa()` — a scripted hook never ran its arm | falls through |
| **ax43 rider** | @367-492 `if (S∉{295,304..307}) aS.i(295)`; the pin for every S | hang pose only below 304, pin only below 308 | exact |
| **ax43 `o(i)`** | @L185 `g.g() != 0` = player **dead** (`x[1] <= 0`) | `w.playerAttacking()` (that is `g.b()`): an attacking player lost the hook offer, a dead one kept it | death test |
| **ax9 `bM()` preamble** | @0-200 the `az = s.az+1` ride re-pin is the ELSE of the link scan | ran in the link tick (az one tick early) | else |
| **ax9 S4/S5** | tableswitch → bare return @653; `k.aB` is written only by `aV()` (ax10) | slice 107 had ported `aV()`'s hint banner and context pad into ax9 | removed (no shipped ax9 record uses S4/S5); the four slice-107 tests pin the bytes now |
| **ax40 `bx()` far end** | @663-728 `(Z2 > Z3 && ak < Z3) \|\| (Z2 < Z3 && ak > Z3)` | `Z2 <= Z3 → ak > Z3` — equal (unresolved) endpoints counted as "past the end" | exact |

## Tests

`Slice409Test` (12): Bézier endpoints / midpoint / truncation + the ax74 wisp start; ax43 offer
(attacking / dead / idle), rider anim + pin per S, claim step; ax9 link tick + S4/S5; ax40 equal
endpoints. Existing misreads corrected to the bytes: `Slice1Test` ax74 S3 (t=0 → start),
ax61 S8 (256 domain), ax9 preamble (az next tick), Slice107 (S4/S5 are bare returns).
Mutation-checked: 11 mutants — 11 killed.

## Not done (parked)

ax60 `bj()`/`c(Z)` (7 records), ax41 `n()`, ax61 `aR()` (all but S8), ax69/72/78/79/32/25/65,
ax76 `bO()`, ax64 `bl()` (no shipped record); `applyHit` ops 39/40/41; the ax11 script-claim
head @1700-1951; the `k` UI methods (`ah`, `M`, `R`, `Q`, `G(int)`).
