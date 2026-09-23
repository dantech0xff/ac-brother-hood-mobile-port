---
title: "Slice 118 — jc6 ABOUT screen + bU[77] $VVV splice + dead states 7/32/33/34"
date: 2026-09-23
status: done
---

# Slice 118 — ABOUT screen (a() case 6) + final dead states

## What was ported

- **`k.a()` case 6** (k.java:844-858, proven) → `menuJc6()`:
  `cb=true`; `b(y,1,d(0,77),200,50,390,155,0,1)` → `scrollPanel(d0(77),
  50, 155, 390, wrap=true)`; exit arm `v(131072) && !dx → l(3); z(30)`.
  The remaining lines (`f(false)`, `d(0,7)`, `bW.l(1)`, `j.a(cd,…)`)
  are draw-side → renderer.
- **`bU[77]`** — the ABOUT credits roll with the verbatim `$VVV` splice
  (k.java:3988-3991, proven): `GloftASBR.b` = `getAppProperty
  ("MIDlet-Version")` = **1.2.7** (META-INF/MANIFEST.MF, proven).
- **`menuFooter()` `6 ->`** — `!dx → a("",d(0,17))` BACK footer;
  `dx` variant has none (:851-853).
- **Dead-consume arms `{7,32,33,34}`** — `a()`'s outer cases are
  {0-6,8-15,17-25,27-30}; no cases for 7/16/26/31/32/33/34 and no
  `l()` callers. States 7/32/33/34 join 26/11/-1 as dead consumers
  (16 excluded — `l(16)` sets `al` → the frozen-state `M_CONTEXT →
  reload` fallback stays its live behavior). This completes the
  `a()` switch coverage matrix.
- **Renderer** — `aboutScreen` (Level0Renderer.kt): `d(0,7)` "ABOUT"
  title on `fontW.l(1)` + `d0(77)` wrapped roll at `kFd` clipped to
  (0,50,400,155).

## Gates

- `verify-static-reconstruction.py` → `"ok": true`
- `python3 -m unittest` → 57 tests OK
- `./gradlew :core:test` → all green incl. 6 new Slice118Test tests
- `./gradlew :android:assembleDebug :gdx:build` → OK
