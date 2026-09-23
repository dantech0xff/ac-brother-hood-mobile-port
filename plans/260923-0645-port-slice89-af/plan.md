---
title: "Slice 89 — af() jc30 medal/level browse screen"
phase: port
status: done
---

# Slice 89 — the `af()` browse screen (jc30)

Ports `k.af()` (k.java:6230-6320, proven) — the medal/achievement
browser: `fO` init phase, `fC` title fade, `fR` pending-nav, `bL`
cursor, footer + confirm/back dispatch.

## Semantics (proven, file:line)

- `fO==0` init (:6233-6250): `fC=20; fE=0; fQ=0`;
  `da = dt||bA[69]!=0 ? 8 : bA[14]+1`; `fQ` = #{i<4 | `da > fP[i]`},
  `fP={0,2,5,7}` (:344); `ey=fQ; bL=0; fR=-1; fO=1`.
- Per-frame (:6254-6268): `d(93,46,214)`; `fC>0&&!=255 → +20` clamped;
  `fR!=-1 → fE>20 ? fE-=20 : fR=-1`; `a(d(0,79),d(0,17))` footer.
- `v(327712)` (:6269): `fF==20 ? l(20) : l(9); fF=0; z(23)`.
- `v(131072)` (:6279): `fF==19 ? (fO=3; l(19)) : l(2); fF=0; z(30)`.
- `fQ>1` nav (:6292-6320): `v(16388)` up → `fR=bL; bL--`, clamp 0 or
  rearm (`fC=20;fE=255;bw=bL;fH=0;fI=1;fK.a(21,1)`); `v(33024)` down →
  `fR=bL; bL++`, clamp `fQ-1` or same rearm; `e.a()` playing → return
  without `z(23)` blip.

## Port

- `Level0World.kt`: fields `kFC/kFQ/kBL/kFR/kFP/menuFkArm`;
  `menuPanelRect` 30→(93,46,214); `menuFooter` 30→(OK,BACK);
  `menuAf()` routed from `menuFrame` (replaces generic `L()+Q()`);
  `e.a()` → `audioTrack != -1`.
- `Level0Renderer.kt`: `menuFkArm` consumed → `fK.arm(21,1)`.
- `Slice1Test.kt`: `Slice89Test` (7 tests).

## Verbatim quirks kept

- init + first `+20` fade happen in the same frame (`fC` hits 40 on the
  first tick, not 20).
- `bw=bL` only on real movement — clamped taps leave `bw` alone.
- Rows are the generic b() "LEVEL n" + jc30 icon `i13+5` (already
  ported in slice 86).

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` green;
  `:android:assembleDebug` green.
