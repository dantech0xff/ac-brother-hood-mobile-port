---
title: "Port slice 85 — menu/stats screens onto the real bitmap fonts"
phase: rewrite
status: done
slice: 85
---

# Slice 85 — menus on `bW`/`y` glyph text + `l()` clamp fix

Swaps the last placeholder `BitmapFont` sites to the slice-84 font
pipeline. Menu/stats screens now draw real glyphs:

- Menu (`menuVisible`): title + prompt on `bW` (pack 91) centered
  (200,76)/(200,93) align 3 with `bW.l(1)` palette (verbatim —
  `bW.l(1); bW.a(cd, d(0,eC), ..., 200, 93, ...,3,-1)` at
  k.java:1128); rows `bW.l(0)` centered align 3, 33px rows
  (`bW.a(cd, strA, i14-ez, i9+(i4>>1), 3)`, `i4=30`, k.java:6020-6045).
- Stats (`statsVisible`): `d(0,bx)` text + `jG%6` "TOUCH THE SCREEN"
  blink on `y` at (200,173), align 3 (k.java:1788).
- Medal title also `bW` + `l(1)` (corrected to verbatim `bW.l(1)` +
  `bW.a(cd,str,210,43,17)`, k.java:6397).

Honest gaps kept `inferred`: `b()` panel sprite, `A[2]` row-icon /
selection-pill procs, `a(strD,zD,w)` fit-scroll — unmined; our
procedural panel + selection strip stand in.

## FontClip fix

`l(i)` (:1964, proven) ignores out-of-range palettes — ported as a
clamped setter; `\<digit>` escape now routes through `l()` (verbatim:
`l((c3&255)-48)`). Renderer calls `f.l(i)` for palette variants.

## Gates

verifier ok:true · 57 unittests · :core:test incl. Slice84Test +
oob-palette case green · :android:assembleDebug green.
