---
title: "Slice 87 — a(str,str2) footer soft-keys + a(i,i2,i3,z2) pill"
phase: port
status: done
---

# Slice 87 — footer soft-key strip + per-state panel rects

Ports the footer `a(String str, String str2)` (k.java:2270-2310, proven)
plus its `a(i,i2,i3,z2)` pill (:2242), and generalizes the slice-86
`b()` panel to each screen's verbatim rect.

## Ports (proven, file:line)

- `a(i,i2,i3,z2)` pill `:2242`: frames `z2?41,42:43,44`; cached
  `cQ`/`cR` frame-0 module widths; cap + do-while `i5` fill + an inner
  `i5` at `(x+w)-cQ-cR` + mirrored `i4` cap.
- `a(str,str2)` `:2270`: `ce=-1;cf=-1` each frame; left skipped when
  `jc==21||jc==8`; `ce = b.d+30` iff `str==d(0,16)` else 36 (`y` font
  measure); pill `a(5,235,ce,d(-5,198,ce+20,47))`; NEXT text
  `y.l(0); y.a(cd,str,5+(ce>>1),222,3)`; `c()` → `E(262144)`.
  Right: `cf = b.d+30` iff `str2==d(0,18)` else 36; pill
  `a(395-cf,235,cf,zD)`; `y` text iff d(0,18) else A[2] arrow
  `zD?29:24` at `395-(cf>>1),222`; `c()` → `E(131072)`.
- Caller labels (all proven): jc14 `a(bv==2?d(0,16):d(0,79),d(0,17))`
  (:1136); jc19 `a(d(0,79),d(0,17))` (:1181); ae() covers jc23/28 with
  `a(d(0,79),(bv==0||jc==23||jc==13)?"":d(0,17))` (:6225); jc29
  `a(null,(bv==0||bv==3)?"":d(0,17))` (:1444); **jc12/13 have NO
  footer** (verified :1108-1120 — fail/win screens take only rows/tap).
- Per-state panel rects: jc14 bv3 `(93,67,214,z3)` / bv4 `(93,86)` /
  else `(93,30)` (:1124-1129); jc19 `d(14,47,180)`; jc23/28
  `d(93,120,214)` (:6221); jc29 `d(93,86,214)` (:1440); z2=true only
  for 12/13/14 (`d()`→`b(…,false,false)` for the rest).
- `d(i,i2,i3)` wrapper `:5863` → `b(i,i2,i3,false,false)`.

## Corrections

- slice-86 `menuPanelY` mapped jc14-bv3 to 86 — wrong: verbatim 67
  (:1124). Fixed by `menuPanelRect`.
- `bU[79]` was missing — added `"OK"` (proven, table index 79); the
  jc14 left label `d(0,79)` resolved to null without it.

## Files

- `core/.../Level0World.kt`: `menuPanelRect/panelVisible/menuPanelZ2`,
  `menuFooter/footerLeftDim/footerRightDim/footerQ`, `kCe/kCf`,
  `footerFont` (ctor `charmap`), `menuRowRects` generalized to the
  panel rect (column-2 `i=206` kept verbatim even off jc19's 14..194
  panel — orig quirk), `bU[79]`.
- `gdx/.../Level0Renderer.kt`: `softPill`, `footer`, render block now
  covers `panelVisible` states with `menuPanelZ2/Z3` + footer.
- `gdx/.../Level0Game.kt`: `charmap` passed to world.
- `core/.../Slice1Test.kt`: `Slice87Test` (8 tests) + Slice78Test
  recalibration for the verbatim jc14-bv3 rect.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` green;
  `:android:assembleDebug` green.

## Gaps (flagged)

- `E(262144)` armed verbatim on left taps; its per-screen consumers
  live in `m()` arms not all ported (e.g. jc14 advance) — `inferred`.
- `a(bVar,i,str,...)` marquee `fe`/`fd`/`dw` still pending.
- `ae()`/`af()` full body (jc23/28 title placement, `bW.a` clip-center
  variants) — the panel rect is verbatim; title position `inferred`.
