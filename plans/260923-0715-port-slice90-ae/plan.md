---
title: "Slice 90 — ae() jc23/28 screen + eC==121 wipe-confirm"
phase: port
status: done
---

# Slice 90 — the `ae()` screen (jc23/28)

Ports `k.ae()` (k.java:6204-6228, proven) — the shared screen behind
jc23 (options) and jc28 (stats/about): bW-centered title, verbatim
footer formula, `L(ey);Q()` rows, and the `eC==121` wipe-confirm arm.

## Semantics (proven, file:line)

- `eC==121` arm (:6206-6217): title at y=120 (not 80); `v(131072)` →
  `fE=255; fO=3; K(4); bw=-1; l(3); z(30)`; `a("",d(0,17))` footer —
  empty left label; early return.
- Normal path (:6218-6227): `eB>0 && j.c!=23 → d(0,eB)` subline lookup;
  `d(93,120,214)`; `bW.l(1)` +
  `bW.a(cd,d(0,eC),a(bW,d(0,eC),200),200,80,0,100,3,-1)` — centered in a
  200-wide box at y=80; footer
  `a(d(0,79),(bv==0||j.c==23||j.c==13)?"":d(0,17))`; `L(ey); Q()`.

## Port

- `Level0World.kt`: `menuAe()` routed for jC 23/28 (replaces generic
  `menuL+menuQ` — ae() owns the 121 arm); `menuFooter` 23/28 →
  verbatim formula incl. the 121-arm's `("", BACK)` and the `j.c==13`
  literal (unreachable from ae() — kept verbatim).
- `Level0Renderer.kt`: ae-title draw — `fontW.l(1)`, centered
  `200 - measure/2`, y=120 for eC==121 else 80.
- `Slice1Test.kt`: `Slice90Test` (3 tests).

## Verbatim quirks kept

- jc23's right footer label is `""` — the pill draws but ae() has no
  `v(131072)` consumer on the normal path → dead press (orig behavior).
- `eB`'s lookup result is discarded in the decompile — noted inferred;
  field already exists (`kEb`) for the mission setters.

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` green;
  `:android:assembleDebug` green.
