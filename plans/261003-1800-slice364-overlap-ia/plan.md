---
title: "Slice 364 — every port of i.a(int[],int[]) rejects point boxes (140 sites)"
phase: "port"
status: "done"
slice: 364
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/bytecode/i.javap.txt:4129-4440 (a:(IIII[I)Z, a:([I[I)Z, b:([I[I)Z, a:(II[I)Z bodies)
  - reconstructed-project/src/structured/i.java:520-576
  - reconstructed-project/bytecode/i.javap.txt (every call site in the table, by method@offset)
  - reconstructed-project/bytecode/g.javap.txt:12845 (au() offsets 347, 560)
  - reconstructed-project/bytecode/k.javap.txt:9584 (m(int) offsets 1392, 1675)
  - reconstructed-project/src/structured/i.java:9162-13228 (aV())
---

# Slice 364 — `i.a(int[],int[])` point-box rejects at every port site

## The primitive (proven)

`i.a(int[] a, int[] b)` (structured `i.java:540-558`, bytecode
`a:([I[I)Z` at `i.javap.txt:4228`): `null` on either side → false;
`a[0] > b[2] || a[2] < b[0] || a[1] > b[3] || a[3] < b[1]` → false; then
**`a[0]==a[2] && a[1]==a[3]` → false** and **`b[0]==b[2] && b[1]==b[3]`
→ false** (offsets 56-95); any exception → false. The port has it as
`Entity.overlapStrict`. Its siblings: `i.a(x0,y0,x1,y1,int[])`
(`a:(IIII[I)Z`, structured :520-538, same overlap + both point rejects
→ `Entity.edgeRectOverlap`), `i.b(int[],int[])` (`b:([I[I)Z`, :560-569,
containment → `Entity.containRect`), `i.a(int,int,int[])` (`a:(II[I)Z`,
:571-573, point-in-rect → `Entity.pointInBox`).

The port also had three plain inclusive overlaps **without** the point
rejects — `NpcFsm.rectsOverlap` (48 calls), `Level0World.rectsOverlap`
(1), `Entity.overlapI` (89 main-code calls) — plus two inline copies of
`v()`'s `a(k.ac, Y)` (`Entity.yOverlapsCam`, `PlayerFsm.flightAliveV`).

Point boxes are ordinary clip data: a frame without a rect row gives
`t()` an anchor-point box. Measured on the shipped clips: player S101 T0
(W), player S6 (X, every frame), player S67 T0/T2-T4 (X; only T1 swings),
ax4 clip3 S29 T0/T1/T6/T7 (W and X), ax4 S30 (X), ax11 clip7 S12 T0/T6 (X),
ax16 clip10 S16 T0-T4, clip27 S12/S28 and clip35 S12/S28 (kind-5 decor),
ax68 clip26 S2. The original never reports a contact on those frames;
the port did.

## Method

For each of the 138 helper calls and the 2 inline copies: find the
original the port cites, locate the call in the bytecode (method and
offset), read the operand loads before the `invokestatic`, and confirm
the primitive (`a:([I[I)Z`, `a:(IIII[I)Z`, `b:([I[I)Z`). Structured lines
were matched by operands (the decompiler reorders some ternaries).

## Findings (all proven, bytecode)

1. **135 sites** call `i.a(int[],int[])` with exactly the port's operands
   and order: the primitive was the only defect. Now `overlapStrict`.
2. **3 sites had the wrong operand** as well:
   - `aV()` S48 (offsets 358-381) and S49 (444-467) test the **linked
     target's own `Y`** — `aload_1` = `r8 = k.q(o)` — not the player's.
     The port read `player.Y`. (Level 3 has three S48 records linked to
     the ax9 block uid 650; no level has S49.)
   - `v()`'s ax14 bh3/S69-S71 arm is `i.a(k.ac, W)` (offset 251); the
     duplicate port `Entity.wasHitRecently` read `Y`.
3. **2 sites used the wrong primitive**:
   - `l()` af.S==6 arm (offset 350) is the edges variant
     `i.a(x0,y0,x1,y1,af.W)` (`a:(IIII[I)Z`) — now `edgeRectOverlap`.
   - `k.m()`'s rope camera (offsets 1392 and 1675) is `i.b(aS.Y, k.ac)`,
     **containment** — the port's own comment said so but called
     `overlapI`. Now `containRect`.
4. **No original counterpart**: the 7 test-side `overlapI` uses (bot
   steering in `Slice1Test.kt:630/25763/27052`, fixture assertions in
   `Slice1Test.kt:7640`, `Slice348Test.kt:99`, `Slice350Test.kt:67/93`)
   are test geometry, not ports — left on `overlapI`, which is now
   documented as test-only. The dead private `NpcFsm.overlap()`
   (exclusive edges, no callers) was removed with the two private
   `rectsOverlap` copies.

## Fix

Every site in the table now calls the primitive the bytecode calls, with
the original's operand order. `NpcFsm.rectsOverlap`,
`Level0World.rectsOverlap` and `NpcFsm.overlap` are gone;
`Entity.overlapI` stays only for test geometry.

## Site table

Verdicts: *changed (primitive)* = the original is `i.a(int[],int[])` with
the same operands; *changed (primitive + operand)* = also a wrong box
(finding 2); *changed (wrong primitive)* = finding 3. Lines are the new
code. Bytecode offsets are in `reconstructed-project/bytecode/i.javap.txt`
unless the method is `g.` / `k.`. Totals: 140 sites — 135 primitive, 3
primitive + operand, 2 wrong primitive; 0 already correct (the two `aV()`
S51 calls at `NpcFsm.kt:2178/2180`, @7765/@7790, already used
`overlapStrict` and are not counted); 7 test-only uses without a
counterpart.

| # | Port site (new) | Was | Original call (bytecode method@offset) | Structured | Primitive now | Verdict | Conf. |
|---|---|---|---|---|---|---|---|
| 1 | `core/NpcFsm.kt:392` | `overlapI` | `a(aS.W, X)` — I()@4826 | i.java:4495 | `overlapStrict` | changed (primitive) | proven |
| 2 | `core/NpcFsm.kt:953` | `overlapI` | `a(r7.W, r9)` — a(i,int,int[])@13 | i.java:14847 | `overlapStrict` | changed (primitive) | proven |
| 3 | `core/NpcFsm.kt:1063` | `overlapI` | `a(W, q.W)` — aD()@58 | i.java:7167 | `overlapStrict` | changed (primitive) | proven |
| 4 | `core/NpcFsm.kt:1073` | `overlapI` | `a(W, s.W)` — aD()@191 | i.java:7179 | `overlapStrict` | changed (primitive) | proven |
| 5 | `core/NpcFsm.kt:1236` | `rectsOverlap` | `a(aS.W, W)` — aV()@1462 S0 | i.java:9777 | `overlapStrict` | changed (primitive) | proven |
| 6 | `core/NpcFsm.kt:1255` | `rectsOverlap` | `a(aS.W, W)` — aV()@5055 S2 | i.java:11852 | `overlapStrict` | changed (primitive) | proven |
| 7 | `core/NpcFsm.kt:1288` | `rectsOverlap` | `a(aS.W, W)` — aV()@5317 S3 | i.java:11993 | `overlapStrict` | changed (primitive) | proven |
| 8 | `core/NpcFsm.kt:1315` | `rectsOverlap(player.Y, W)` | `a(r8.Y, W), r8=k.q(o)` — aV()@378 S48 | i.java:9289 | `overlapStrict` | changed (primitive + operand) | proven |
| 9 | `core/NpcFsm.kt:1323` | `rectsOverlap(player.Y, W)` | `a(r8.Y, W), r8=k.q(o)` — aV()@464 S49 | i.java:9329 | `overlapStrict` | changed (primitive + operand) | proven |
| 10 | `core/NpcFsm.kt:1333` | `rectsOverlap` | `a(aS.Y, W)` — aV()@341 S54 | i.java:9269 | `overlapStrict` | changed (primitive) | proven |
| 11 | `core/NpcFsm.kt:1380` | `rectsOverlap` | `a(aS.W, W)` — aV()@1009 S10 | i.java:9581 | `overlapStrict` | changed (primitive) | proven |
| 12 | `core/NpcFsm.kt:1438` | `rectsOverlap` | `a(aS.W, W)` — aV()@491 S46 | i.java:9343 | `overlapStrict` | changed (primitive) | proven |
| 13 | `core/NpcFsm.kt:1441` | `rectsOverlap` | `a(aS.W, W)` — aV()@515 S46 | i.java:9354 | `overlapStrict` | changed (primitive) | proven |
| 14 | `core/NpcFsm.kt:1450` | `rectsOverlap` | `a(W, aS.W)` — aV()@5571 S4 | i.java:12126 | `overlapStrict` | changed (primitive) | proven |
| 15 | `core/NpcFsm.kt:1460` | `rectsOverlap` | `a(W, aS.W)` — aV()@5618 S5 | i.java:12149 | `overlapStrict` | changed (primitive) | proven |
| 16 | `core/NpcFsm.kt:1470` | `rectsOverlap` | `a(W, aS.W)` — aV()@5688 S6 | i.java:12178 | `overlapStrict` | changed (primitive) | proven |
| 17 | `core/NpcFsm.kt:1471` | `rectsOverlap` | `a(W, aS.W)` — aV()@5709 S7 | i.java:12188 | `overlapStrict` | changed (primitive) | proven |
| 18 | `core/NpcFsm.kt:1477` | `rectsOverlap` | `a(aS.W, W)` — aV()@5429 S8 | i.java:12057 | `overlapStrict` | changed (primitive) | proven |
| 19 | `core/NpcFsm.kt:1499` | `rectsOverlap` | `a(W, aS.W)` — aV()@5990 S12 | i.java:12314 | `overlapStrict` | changed (primitive) | proven |
| 20 | `core/NpcFsm.kt:1520` | `rectsOverlap` | `a(aS.af.W, W)` — aV()@5801 S14 | i.java:12230 | `overlapStrict` | changed (primitive) | proven |
| 21 | `core/NpcFsm.kt:1544` | `rectsOverlap` | `a(aS.W, W)` — aV()@7330 S18 | i.java:12957 | `overlapStrict` | changed (primitive) | proven |
| 22 | `core/NpcFsm.kt:1567` | `rectsOverlap` | `a(aS.W, W)` — aV()@6239 S21 | i.java:12437 | `overlapStrict` | changed (primitive) | proven |
| 23 | `core/NpcFsm.kt:1580` | `rectsOverlap` | `a(W, aS.W)` — aV()@5511 S22 | i.java:12095 | `overlapStrict` | changed (primitive) | proven |
| 24 | `core/NpcFsm.kt:1594` | `rectsOverlap` | `a(r1.W, W), r1=k.q(o)` — aV()@6318 S23 | i.java:12476 | `overlapStrict` | changed (primitive) | proven |
| 25 | `core/NpcFsm.kt:1613` | `rectsOverlap` | `a(aS.W, W)` — aV()@7021 S17 | i.java:12813 | `overlapStrict` | changed (primitive) | proven |
| 26 | `core/NpcFsm.kt:1621` | `rectsOverlap` | `a(aS.W, W)` — aV()@7132 S17 | i.java:12869 | `overlapStrict` | changed (primitive) | proven |
| 27 | `core/NpcFsm.kt:1655` | `rectsOverlap` | `a(aS.W, W)` — aV()@631 S24 | i.java:9401 | `overlapStrict` | changed (primitive) | proven |
| 28 | `core/NpcFsm.kt:1709` | `rectsOverlap` | `a(W, aS.W)` — aV()@2684 S30 | i.java:10432 | `overlapStrict` | changed (primitive) | proven |
| 29 | `core/NpcFsm.kt:1847` | `rectsOverlap` | `a(aS.W, W)` — aV()@6851 S28 | i.java:12732 | `overlapStrict` | changed (primitive) | proven |
| 30 | `core/NpcFsm.kt:1853` | `rectsOverlap` | `a(aS.W, W)` — aV()@6925 S28 | i.java:12767 | `overlapStrict` | changed (primitive) | proven |
| 31 | `core/NpcFsm.kt:1901` | `rectsOverlap` | `a(W, aS.W)` — aV()@1575 S31 | i.java:9837 | `overlapStrict` | changed (primitive) | proven |
| 32 | `core/NpcFsm.kt:2033` | `rectsOverlap` | `a(k.aU.Y, W)` — aV()@271 S55 | i.java:9238 | `overlapStrict` | changed (primitive) | proven |
| 33 | `core/NpcFsm.kt:2047` | `rectsOverlap` | `a(aS.W, W)` — aV()@6363 S32 | i.java:12502 | `overlapStrict` | changed (primitive) | proven |
| 34 | `core/NpcFsm.kt:2067` | `rectsOverlap` | `a(g.a.W, W)` — aV()@6686 S41 | i.java:12658 | `overlapStrict` | changed (primitive) | proven |
| 35 | `core/NpcFsm.kt:2072` | `rectsOverlap` | `a(aS.W, W)` — aV()@7551 S42 | i.java:13079 | `overlapStrict` | changed (primitive) | proven |
| 36 | `core/NpcFsm.kt:2076` | `rectsOverlap` | `a(aS.W, W)` — aV()@6755 S44 | i.java:12688 | `overlapStrict` | changed (primitive) | proven |
| 37 | `core/NpcFsm.kt:2086` | `rectsOverlap` | `a(aS.W, W)` — aV()@6787 S45 | i.java:12703 | `overlapStrict` | changed (primitive) | proven |
| 38 | `core/NpcFsm.kt:2092` | `rectsOverlap` | `a(aS.W, W)` — aV()@6444 S33 | i.java:12537 | `overlapStrict` | changed (primitive) | proven |
| 39 | `core/NpcFsm.kt:2100` | `rectsOverlap` | `a(aS.W, W)` — aV()@6547 S36 | i.java:12584 | `overlapStrict` | changed (primitive) | proven |
| 40 | `core/NpcFsm.kt:2111` | `rectsOverlap` | `a(aS.W, W)` — aV()@6707 S43 | i.java:12668 | `overlapStrict` | changed (primitive) | proven |
| 41 | `core/NpcFsm.kt:2143` | `rectsOverlap` | `a(aS.W, W)` — aV()@6115 S16 | i.java:12379 | `overlapStrict` | changed (primitive) | proven |
| 42 | `core/NpcFsm.kt:2161` | `rectsOverlap` | `a(aS.W, W)` — aV()@7609 S50 | i.java:13104 | `overlapStrict` | changed (primitive) | proven |
| 43 | `core/NpcFsm.kt:2183` | `rectsOverlap` | `a(aS.W, W)` — aV()@7817 S53 | i.java:13198 | `overlapStrict` | changed (primitive) | proven |
| 44 | `core/NpcFsm.kt:2271` | `rectsOverlap` | `a(aS.W, W)` — aj()@195 S5/7 | i.java:5486 | `overlapStrict` | changed (primitive) | proven |
| 45 | `core/NpcFsm.kt:2277` | `rectsOverlap` | `a(W, k.M)` — aj()@113 S5/7 | i.java:5492 | `overlapStrict` | changed (primitive) | proven |
| 46 | `core/NpcFsm.kt:2291` | `rectsOverlap` | `a(aS.X, W)` — aj()@227 S5/7 | i.java:5501 | `overlapStrict` | changed (primitive) | proven |
| 47 | `core/NpcFsm.kt:2318` | `rectsOverlap` | `a(aS.W, X)` — aj()@442 S29 | i.java:5532 | `overlapStrict` | changed (primitive) | proven |
| 48 | `core/NpcFsm.kt:2321` | `rectsOverlap` | `a(bd[i].W, X)` — aj()@577 S29 | i.java:5536 | `overlapStrict` | changed (primitive) | proven |
| 49 | `core/NpcFsm.kt:2340` | `rectsOverlap` | `a(aS.X, W)` — aj()@363 S30 | i.java:5563 | `overlapStrict` | changed (primitive) | proven |
| 50 | `core/NpcFsm.kt:2342` | `rectsOverlap` | `a(aS.W, W)` — aj()@397 S30 | i.java:5567 | `overlapStrict` | changed (primitive) | proven |
| 51 | `core/NpcFsm.kt:2343` | `rectsOverlap` | `a(g.a.W, W)` — aj()@419 S30 | i.java:5567 | `overlapStrict` | changed (primitive) | proven |
| 52 | `core/NpcFsm.kt:2390` | `rectsOverlap` | `a(aS.W, W)` — a()@125 | i.java:736 | `overlapStrict` | changed (primitive) | proven |
| 53 | `core/NpcFsm.kt:2422` | `overlapI` | `a(r1.W, W)` — i(i)@89 | i.java:5478 | `overlapStrict` | changed (primitive) | proven |
| 54 | `core/NpcFsm.kt:2438` | `overlapI` | `a(bd[i].W, W)` — n()@131 | i.java:5319 | `overlapStrict` | changed (primitive) | proven |
| 55 | `core/NpcFsm.kt:2507` | `overlapI` | `a(aS.W, W)` — bq()@46 | i.java:15596 | `overlapStrict` | changed (primitive) | proven |
| 56 | `core/NpcFsm.kt:2523` | `overlapI` | `a(aS.W, W)` — br()@12 | i.java:15601 | `overlapStrict` | changed (primitive) | proven |
| 57 | `core/NpcFsm.kt:2562` | `overlapI` | `a(aS.W, W)` — bm()@122 S6/24 | i.java:15313 | `overlapStrict` | changed (primitive) | proven |
| 58 | `core/NpcFsm.kt:2573` | `overlapI` | `a(aS.W, W)` — bm()@273 S7/25 | i.java:15330 | `overlapStrict` | changed (primitive) | proven |
| 59 | `core/NpcFsm.kt:2615` | `overlapI` | `a(aS.W, X)` — bm()@878 S11-13 | i.java:15397 | `overlapStrict` | changed (primitive) | proven |
| 60 | `core/NpcFsm.kt:2620` | `overlapI` | `a(aS.W, W)` — bm()@969 S11-13 | i.java:15404 | `overlapStrict` | changed (primitive) | proven |
| 61 | `core/NpcFsm.kt:2670` | `overlapI` | `a(aS.W, X)` — bm()@1644 S11-13 | i.java:15456 | `overlapStrict` | changed (primitive) | proven |
| 62 | `core/NpcFsm.kt:2677` | `overlapI` | `a(aS.W, W)` — bm()@653 S15 | i.java:15474 | `overlapStrict` | changed (primitive) | proven |
| 63 | `core/NpcFsm.kt:2704` | `overlapI` | `a(aS.W, W)` — bm()@1792 S19-22 | i.java:15506 | `overlapStrict` | changed (primitive) | proven |
| 64 | `core/NpcFsm.kt:2710` | `overlapI` | `a(aS.W, W)` — bm()@1906 S19-22 | i.java:15523 | `overlapStrict` | changed (primitive) | proven |
| 65 | `core/NpcFsm.kt:2733` | `overlapI` | `a(aS.W, W)` — bo()@10 | i.java:15564 | `overlapStrict` | changed (primitive) | proven |
| 66 | `core/NpcFsm.kt:2747` | `overlapI` | `a(aS.W, W)` — bs()@41 | i.java:15633 | `overlapStrict` | changed (primitive) | proven |
| 67 | `core/NpcFsm.kt:2758` | `overlapI` | `a(aS.W, W)` — bs()@259 | i.java:15637 | `overlapStrict` | changed (primitive) | proven |
| 68 | `core/NpcFsm.kt:2781` | `overlapI` | `a(aS.W, W)` — bs()@585 | i.java:15667 | `overlapStrict` | changed (primitive) | proven |
| 69 | `core/NpcFsm.kt:2825` | `overlapI` | `a(aS.W, W)` — bs()@1288 | i.java:15716 | `overlapStrict` | changed (primitive) | proven |
| 70 | `core/NpcFsm.kt:2840` | `overlapI` | `a(aS.W, W)` — bs()@1438 | i.java:15731 | `overlapStrict` | changed (primitive) | proven |
| 71 | `core/NpcFsm.kt:2856` | `overlapI` | `a(aS.W, W)` — aN()@12 | i.java:7911 | `overlapStrict` | changed (primitive) | proven |
| 72 | `core/NpcFsm.kt:2860` | `overlapI` | `a(aS.W, W)` — aN()@82 | i.java:7916 | `overlapStrict` | changed (primitive) | proven |
| 73 | `core/NpcFsm.kt:2924` | `overlapI` | `a(aS.W, W)` — ao()@10 | i.java:5883 | `overlapStrict` | changed (primitive) | proven |
| 74 | `core/NpcFsm.kt:2999` | `overlapI` | `a(aS.W, W)` — aq()@101 S3 | i.java:5947 | `overlapStrict` | changed (primitive) | proven |
| 75 | `core/NpcFsm.kt:3009` | `overlapI` | `a(aS.W, W)` — aq()@806 S4 | i.java:5967 | `overlapStrict` | changed (primitive) | proven |
| 76 | `core/NpcFsm.kt:3018` | `overlapI` | `a(aS.W, W)` — aq()@854 S9 | i.java:6079 | `overlapStrict` | changed (primitive) | proven |
| 77 | `core/NpcFsm.kt:3147` | `overlapI` | `a(r0.W, k.ac)` — h(i)@13 | i.java:19058 | `overlapStrict` | changed (primitive) | proven |
| 78 | `core/NpcFsm.kt:3173` | `overlapI` | `a(r7.W, r9)` — a(i,int,int[])@13 | i.java:14847 | `overlapStrict` | changed (primitive) | proven |
| 79 | `core/NpcFsm.kt:3246` | `overlapI` | `a(aS.W, W)` — bL()@212 S0 | i.java:19091 | `overlapStrict` | changed (primitive) | proven |
| 80 | `core/NpcFsm.kt:3344` | `overlapI` | `a(W, aS.X)` — bL()@932 S15 | i.java:19239 | `overlapStrict` | changed (primitive) | proven |
| 81 | `core/NpcFsm.kt:3467` | `overlapI` | `a(aS.W, W)` — bx()@146 | i.java:16148 | `overlapStrict` | changed (primitive) | proven |
| 82 | `core/NpcFsm.kt:3533` | `overlapI` | `a(aS.W, W)` — bx()@834 | i.java:16215 | `overlapStrict` | changed (primitive) | proven |
| 83 | `core/NpcFsm.kt:3544` | `overlapI` | `a(bd[i].W, W)` — bx()@934 | i.java:16228 | `overlapStrict` | changed (primitive) | proven |
| 84 | `core/NpcFsm.kt:3623` | `overlapI` | `a(W, aS.W)` — bB()@126 | i.java:16435 | `overlapStrict` | changed (primitive) | proven |
| 85 | `core/NpcFsm.kt:3634` | `overlapI` | `a(W, r2.ad.W)` — bB()@268 | i.java:16445 | `overlapStrict` | changed (primitive) | proven |
| 86 | `core/NpcFsm.kt:3655` | `overlapI` | `a(X, aS.W)` — bB()@425 S28 | i.java:16492 | `overlapStrict` | changed (primitive) | proven |
| 87 | `core/NpcFsm.kt:3669` | `overlapI` | `a(W, aS.W)` — bB()@548 S28 | i.java:16493 | `overlapStrict` | changed (primitive) | proven |
| 88 | `core/NpcFsm.kt:3683` | `overlapI` | `a(W, aS.W)` — bB()@647 S12 | i.java:16475 | `overlapStrict` | changed (primitive) | proven |
| 89 | `core/NpcFsm.kt:3745` | `overlapI` | `a(aS.Y, W)` — aX()@18 | i.java:13417 | `overlapStrict` | changed (primitive) | proven |
| 90 | `core/NpcFsm.kt:3764` | `overlapI` | `a(aS.Y, W)` — aX()@228 | i.java:13442 | `overlapStrict` | changed (primitive) | proven |
| 91 | `core/NpcFsm.kt:3772` | `overlapI` | `a(aS.Y, W)` — aX()@288 | i.java:13430 | `overlapStrict` | changed (primitive) | proven |
| 92 | `core/NpcFsm.kt:3796` | `overlapI` | `a(aS.W, W)` — bb()@19 S30 + @80 S38 | i.java:13942/13950 | `overlapStrict` | changed (primitive) | proven |
| 93 | `core/NpcFsm.kt:3806` | `overlapI` | `a(aS.W, W)` — bb()@143 S39 | i.java:13959 | `overlapStrict` | changed (primitive) | proven |
| 94 | `core/NpcFsm.kt:3833` | `overlapI` | `a(X, aS.W)` — bb()@357 S16 | i.java:13983 | `overlapStrict` | changed (primitive) | proven |
| 95 | `core/NpcFsm.kt:3837` | `overlapI` | `a(X, aS.W)` — bb()@392 S16 | i.java:13988 | `overlapStrict` | changed (primitive) | proven |
| 96 | `core/NpcFsm.kt:3856` | `overlapI` | `a(aS.W, X)` — bb()@476 S17 | i.java:14004 | `overlapStrict` | changed (primitive) | proven |
| 97 | `core/NpcFsm.kt:3879` | `overlapI` | `a(W, af.W)` — bb()@756 S17 | i.java:14029 | `overlapStrict` | changed (primitive) | proven |
| 98 | `core/NpcFsm.kt:3916` | `overlapI` | `a(aS.W, W)` — bb()@1122 S22/23 | i.java:14092 | `overlapStrict` | changed (primitive) | proven |
| 99 | `core/NpcFsm.kt:3935` | `overlapI` | `a(aS.W, W)` — bb()@1266 S31 | i.java:14115 | `overlapStrict` | changed (primitive) | proven |
| 100 | `core/NpcFsm.kt:3948` | `overlapI` | `a(aS.W, W)` — bb()@1412 S32/33 | i.java:14137 | `overlapStrict` | changed (primitive) | proven |
| 101 | `core/NpcFsm.kt:4616` | `overlapI` | `a(W, aS.X)` — aP()@138 | i.java:8037 | `overlapStrict` | changed (primitive) | proven |
| 102 | `core/NpcFsm.kt:4624` | `overlapI` | `a(W, aS.X)` — aP()@244 | i.java:8044 | `overlapStrict` | changed (primitive) | proven |
| 103 | `core/NpcFsm.kt:4669` | `overlapI` | `a(W, aS.X)` — aP()@676 | i.java:8075 | `overlapStrict` | changed (primitive) | proven |
| 104 | `core/NpcFsm.kt:4755` | `overlapI` | `a(W, aS.X)` — aP()@1763 S14/35 | i.java:8441 | `overlapStrict` | changed (primitive) | proven |
| 105 | `core/NpcFsm.kt:4794` | `overlapI` | `a(W, aS.X)` — aP()@2403 S6/15/16 | i.java:8596 | `overlapStrict` | changed (primitive) | proven |
| 106 | `core/NpcFsm.kt:4888` | `overlapI` | `a(W, aS.W)` — aP()@3181 S4 | i.java:8303 | `overlapStrict` | changed (primitive) | proven |
| 107 | `core/NpcFsm.kt:4907` | `overlapI` | `a(W, aS.W)` — aP()@3734 S26 | i.java:8551 | `overlapStrict` | changed (primitive) | proven |
| 108 | `core/NpcFsm.kt:4907` | `overlapI` | `a(W, aS.X)` — aP()@3750 S26 | i.java:8551 | `overlapStrict` | changed (primitive) | proven |
| 109 | `core/NpcFsm.kt:4927` | `overlapI` | `a(W, aS.X)` — aP()@2032 S21 | i.java:8521 | `overlapStrict` | changed (primitive) | proven |
| 110 | `core/NpcFsm.kt:5045` | `overlapI` | `a(aS.W, W)` — a()@125 | i.java:736 | `overlapStrict` | changed (primitive) | proven |
| 111 | `core/NpcFsm.kt:5489` | `overlapI` | `a(X, aS.W)` — aR()@293 S10 | i.java:8822 | `overlapStrict` | changed (primitive) | proven |
| 112 | `core/NpcFsm.kt:5504` | `overlapI` | `a(Y, aS.Y)` — aR()@571 S15 | i.java:8761 | `overlapStrict` | changed (primitive) | proven |
| 113 | `core/NpcFsm.kt:5617` | `overlapI` | `a(X, aS.W)` — aR()@360 S2/4/5/17 | i.java:8939 | `overlapStrict` | changed (primitive) | proven |
| 114 | `core/NpcFsm.kt:7790` | `overlapI` | `a(W, aS.X)` — i()@29 | i.java:1279 | `overlapStrict` | changed (primitive) | proven |
| 115 | `core/NpcFsm.kt:7835` | `overlapI` | `a(W, aS.X)` — j()@210 | i.java:1331 | `overlapStrict` | changed (primitive) | proven |
| 116 | `core/NpcFsm.kt:10337` | `overlapI` | `a(aS.W, X)` — aB()@57 | i.java:6974 | `overlapStrict` | changed (primitive) | proven |
| 117 | `core/NpcFsm.kt:10343` | `overlapI` | `a(aS.W, X)` — aB()@139 | i.java:6985 | `overlapStrict` | changed (primitive) | proven |
| 118 | `core/NpcFsm.kt:10413` | `overlapI` | `a(aS.W, W)` — d()@101 | i.java:1120 | `overlapStrict` | changed (primitive) | proven |
| 119 | `core/NpcFsm.kt:10413` | `overlapI` | `a(aS.X, W)` — d()@117 | i.java:1120 | `overlapStrict` | changed (primitive) | proven |
| 120 | `core/NpcFsm.kt:10480` | `overlapI(intArrayOf(x0,y0,x1,y1), af.W)` | `a(x0,y0,x1,y1, af.W) [a:(IIII[I)Z]` — l()@350 | i.java:1658 | `edgeRectOverlap` | changed (wrong primitive) | proven |
| 121 | `core/NpcFsm.kt:10491` | `overlapI` | `a(aS.W, W)` — l()@364 ax11 | i.java:1625 | `overlapStrict` | changed (primitive) | proven |
| 122 | `core/NpcFsm.kt:10501` | `overlapI` | `a(aS.W, W)` — l()@451 ax73 | i.java:1688 | `overlapStrict` | changed (primitive) | proven |
| 123 | `core/NpcFsm.kt:10652` | `overlapI` | `a(W, aS.W)` — aU()@1089 S31 | i.java:8995 | `overlapStrict` | changed (primitive) | proven |
| 124 | `core/NpcFsm.kt:10696` | `overlapI` | `a(aS.W, W)` — aU()@142 S34 | i.java:9036 | `overlapStrict` | changed (primitive) | proven |
| 125 | `core/Entity.kt:788` | `overlapI` | `i.a(W, F.W)` — g.au()@347 | g.java:4807 | `overlapStrict` | changed (primitive) | proven |
| 126 | `core/Entity.kt:809` | `overlapI` | `i.a(W, F.W)` — g.au()@560 | g.java:4907 | `overlapStrict` | changed (primitive) | proven |
| 127 | `core/Entity.kt:1368` | `inline plain overlap` | `a(k.ac, Y)` — v()@331 (via B()@504) | i.java:624 | `overlapStrict` | changed (primitive) | proven |
| 128 | `core/Entity.kt:3306` | `overlapI(camRect, Y)` | `a(k.ac, W)` — v()@251 ax14 | i.java:629 | `overlapStrict` | changed (primitive + operand) | proven |
| 129 | `core/Entity.kt:3307` | `overlapI` | `a(aS.W, W)` — v()@265 ax14 | i.java:629 | `overlapStrict` | changed (primitive) | proven |
| 130 | `core/Entity.kt:3311` | `overlapI` | `a(k.ac, W)` — v()@320 | i.java:624 | `overlapStrict` | changed (primitive) | proven |
| 131 | `core/Entity.kt:3312` | `overlapI` | `a(k.ac, Y)` — v()@331 | i.java:624 | `overlapStrict` | changed (primitive) | proven |
| 132 | `core/Entity.kt:3755` | `overlapI` | `a(bd[i].W, X)` — bd()@43 | i.java:14285 | `overlapStrict` | changed (primitive) | proven |
| 133 | `core/Entity.kt:3788` | `overlapI (hoisted)` | `a(bd[i].W, X)` — bc()@153/237/385/505/584/666 | i.java:14183-14228 | `overlapStrict` | changed (primitive) | proven |
| 134 | `core/Entity.kt:3793` | `overlapI` | `a(bd[i].ad.W, X)` — bc()@86 | i.java:14176 | `overlapStrict` | changed (primitive) | proven |
| 135 | `core/Entity.kt:4586` | `overlapI` | `a(aS.X, i.r)` — F()@835 | i.java:3016 | `overlapStrict` | changed (primitive) | proven |
| 136 | `core/Level0World.kt:2092` | `overlapI` | `i.b(aS.Y, k.ac) [b:([I[I)Z]` — k.m(int)@1392/@1675 | k.java:1971/1984 | `containRect` | changed (wrong primitive) | proven |
| 137 | `core/Level0World.kt:4374` | `rectsOverlap` | `a(W, aS.W)` — aY()@41 | i.java:13473 | `overlapStrict` | changed (primitive) | proven |
| 138 | `core/PlayerFsm.kt:1900` | `overlapI` | `a(r0.W, k.ac)` — h(i)@13 | i.java:19058 | `overlapStrict` | changed (primitive) | proven |
| 139 | `core/PlayerFsm.kt:2722` | `inline plain overlap` | `a(k.ac, Y)` — v()@331 (player, g.n()) | i.java:624 | `overlapStrict` | changed (primitive) | proven |
| 140 | `gdx/Level0Renderer.kt:2243` | `overlapI` | `a(aS.W, W)` — aU()@142 S34 (draw recompute) | i.java:9036 | `overlapStrict` | changed (primitive) | proven |


## Tests

`Slice364Test` (8) — each pairs a point-box case with a real-box control;
all 8 fail on the old code (checked by restoring the five main sources to
HEAD and re-running the class) and pass on the new:

1. player S101 T0 (a real anchor-point W) never latches the aV() S46
   wall-run zone (@491); a real W does;
2. S48 ignores the player's Y and fires on the linked target's Y (@378);
3. the `a(i,int,int[])` side push (`defaultArm` → `pushL897`, @13) leaves
   a point player box alone; a real box is pushed out;
4. `l()` af.S==6: a point zone rect is blind (`edgeRectOverlap`, @350); a
   real span sees;
5. `bd()` rest sweep skips a neighbor whose W is a point (@43);
6. `v()` ax14 S69 reads W through `i.a` (@251) — a point W is off-camera;
7. `v()` generic arm rejects a point Y (@331);
8. `k.m()` rope camera: a player Y straddling the view edge is not "in
   view" (`i.b` containment, @1392) → camera step `Z[1]*50/100`; a
   contained Y keeps `Z[1]*150/100`.

Legacy unit fixtures that sat on point frames (not capstones; every
assertion kept):

- `Slice163Test` ×3 — the ax4 S29 blast now sweeps on T3 (T0/T6 are
  points), the S30 re-arm uses the player's S67 T1 swing box;
- `Level0WorldTest` springboard child — the ax68 child uses its own
  clip 26 (real S0 W) instead of clip27's point;
- `Level0WorldTest` kind-5 S12/S28 ×3 — clipless props with staged boxes
  (clip27/clip35 S12/S28 are points: on real data these arms never fire,
  and no shipped level places kind-5 decor in S12 or S28);
- `Level0WorldTest` S16 rest arm — the ax16 sits on T5 (T0-T4 points);
- `Slice184Test` ×2 — the ax11 S12 strike box exists on T1-T5 only; the
  roll-bind now starts on T2/T4, and the second test adds the faithful
  T6 case (point X: no bind, `r()` → `i(23)`);
- `Slice142Test` S48/S49 ×3 — rewritten to the bytecode (target's Y; the
  player's Y alone does nothing).

The fixture-only edits pass on the old and new code alike; the Slice142
S48/S49 cases and the Slice184 T6 extension encode the new behavior.

## Impact

No capstone bot changed outcome: `:core:test` is green with no route or
timing edits (Slice245/281/282/288/289/291/297/303/304/306/309 all pass).

## Discrepancies found along the way (not fixed here — follow-ups)

All proven from bytecode; none is a point-box issue, so they stay out of
this slice.

1. **aV() S50 lost its else-arms** (`NpcFsm.kt:2157-2176`): offsets
   7737-7745 (`g.a` rope S1/S4 but no overlap → `G()` + `g.C = true`) and
   7746-7750 (no rope → `G()`) are missing; the port never restores
   `g.C` nor drops the marker after leaving the zone.
2. **l() ax11 S117** (`NpcFsm.kt:10461-10477`): offsets 221-231 return
   `true` straight to the LOS tail when `af.Z[0]==0` and `S==117`; the
   port only "primes" `r0` and lets the af.S checks / L54 overwrite it
   (the structured view is garbled here).
3. **`world.inPlay` is not `k.bh[k.aj]==3`**: `Level0World.inPlay` is
   `!k.al` (a stale `bh==3` doc line sits above it, `Level0World.kt:381`),
   but its only two readers port `bh[aj]==3` tests — `v()` offsets
   206-214 (`Entity.wasHitRecently`, `Entity.kt:3305`) and `aX()` offsets
   101-109 (`NpcFsm.tickPickup`, `NpcFsm.kt:3752`).
4. **`Entity.wasHitRecently` is a second, divergent port of `v()`** next
   to the faithful `inPlayV`: it uses `updateAu` (its `aG==4 → /240` arm
   is not gated on ax13, ax21 S0/S1 gets `/120`) instead of `u()`, and its
   W-arm misses `ax==78 && S==3` (offsets 296-313). `inPlayV`'s own
   `W.isEmpty()` "W==null" check never fires on the port's fixed arrays.
   Folding the callers onto one `v()` port is the follow-up.
5. **`Entity.sweepNeighborsB` (`bc()`) hoists `r0.W∩X` above the type
   dispatch**: the ax54 arm fires on `ad.W∩X` alone (offsets 68-142), the
   ax32 armed-wall abort (`S∈21..27 && !cF → return`, offsets 625-646)
   happens before any overlap, and the ax67/ax24 hits end the scan
   (`goto 864` at 555/604) — the port requires the hoisted overlap and
   keeps scanning after ax67/ax24 hits.
6. **`yOverlapsCam` / `flightAliveV`** are partial ports of `v()` (just
   the `a(k.ac, Y)` tail; `i.B()` @504 and `g.n()` call the full `v()`,
   including the `u()`/`au>i` guard).
7. aV() S47 nulls `k.aQ:Image` (offset 538), not a claim owner; the
   port's `kAQ` is write-only, so there is no gameplay effect.

## Gates

`python3 scripts/verify-static-reconstruction.py … reconstructed-project`
→ `ok: true`; `python3 -m unittest discover -s tests` → 57/57;
`:core:test` 1697/1697 (1689 + 8), `:gdx:test` 9/9.
