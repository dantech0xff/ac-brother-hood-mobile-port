---
title: "slice 113 — jC==24 credits scroller + jC==25 outro"
phase: port
status: done
---

## Summary

Port the ending-credits pair: `k.a()` case 24 (k.java:1326-1386) — the
letterboxed credits scroller — and case 25 (:1388-1420) — the outro
redirect. Adds `bU[28]="THE END"`, `bU[55]` (epilogue), `bU[66]` (IGP
prompt), `bU[99]` (hard-mode unlock). `scrollPanel` gains the `w4` arg
(jc24 wraps at 380).

## Original semantics (proven)

- jc24 `dz<120`: letterbox iris +20/frame, re-arms `dw=0, fd=110,
  dy = d(0,28) + "\n"*11 + d(0,55)` every iris frame (:1375-1385).
- `dz>=120 && dw>0`: dw 1-10 title slide (y 220→120), 11-20 hold,
  21-29 wrapped `b()` panel — `dw=30` armed INSIDE `a()` when the text
  scrolls past `fd < -iK+160` (:5664-5667); 30-415 panel + dw++ with
  `dw>=160 → dw+=20; fe=0; fd++` (white-out: gray `dw-160` clamped 255);
  `>415 → dy=null; dz=0; l(25)`. `v(131072)` → `dw=160; z(23)` skip.
- Entry `dz=0` is armed by the l(24) caller (M() finale arm :3433) —
  already wired at Level0World:1816.
- jc25: `!dx → dx=true; l(6); z(0)` (redirect to ABOUT); `dx → !Z() →
  l(2)`. Epilogue tail (dw fade + d(0,66)/d(0,9) panels + l(27)+eJ
  stamp + `f.a(d(0,24),0)` store intent) is IGP-only — ported but dead
  while `menuShopCheck()` is false.

## Port

- `menuJc24`/`menuJc25`, `menuStates += {24,25}`, footer arm
  `24 -> Pair(null, d0(18))` (SKIP pill), `kDy` field.
- `creditsScreen` renderer: iris bars, black/gray field, title slide,
  wrapped text at `kFd` inside `clipScissor(0,33,400,205)`.

## Gates

verifier ok:true · unittest 57/57 · :core:test (4 new) · android/gdx build
