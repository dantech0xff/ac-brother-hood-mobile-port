---
title: "Slice 328 — Run-27 rare-menu sweep fixes"
phase: "verify"
status: "done"
---

# Slice 328 — Run-27 rare-menu sweep fixes

Device sweep (Run-27) of the rare menu screens found real defects; this
slice fixes all of them.

## Defects fixed

1. **High-score stamp truncated ≥256** — `kBA[si] = i4` dropped the hi
   byte; `a(bA,81+(au<<4)+(aj<<1),(short)i4)` (k.java:3403) is a LE-16
   write at byte offset si → `baShortPut` slot-pair (kBA is
   index-preserving: slot n = value at original byte n, k.java:291).
2. **Menu dead-ends (jc3/jc15/jc22/jc6)** — `menuFooter()` had no cases
   for those states and jc15/jc22/jc6 bypass `menuQ` → footer pill
   hit-zones never armed. Added footer cases (k.java:833, 3392-3395,
   6450-6452, 851-853) + `footerQ()` inside `winStatsM`, `medalAh`
   (kEx==3), `menuJc6`.
3. **Missing strings** — `bU` now carries 62 "MENU", 70 "SOUND SET",
   114-116 medal names (pack-14 strings.json, proven).
4. **jc6 ABOUT + jc24 credits misdrawn** — rewritten verbatim:
   `b(y,1,d(0,77),200,50,390,155,0,1)` / `b(bW,0,dy,200,33,380,205,0,1)`
   (k.java:5627-5685 signature — `j.a(cd,0,i3,400,i5,true)` clip +
   `bVar.l(i)` + drawWrapped at y=fd).
5. **bv4 options rows 5-7 unreachable** — eA[4]'s 8 rows sit in ONE
   column (`(bv!=4&&j.c!=14)` split exclusion, k.java:5942 — SHIPPED
   QUIRK, keypad-only in the original). Port-added touch affordance:
   `menuScrollDy` drag-scroll on the `i9` walk (world `menuRowRects` +
   renderer `menuPanel`), clamped `menuScrollMax() = lastRowBottom-235`,
   row-viewport scissor, drag-release tap suppression, reset in
   `bannerK`.

## Test

`Slice328Test` — 9 tests: score byte-offset write/round-trip, bU
strings, footer cases, scroll overflow/shift/clamp/non-overflow.
1563+9 core tests green; verifier ok:true; APK builds.
