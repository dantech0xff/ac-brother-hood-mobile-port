---
title: "Slice 91 — F() jc4 high-scores screen"
phase: port
status: done
---

# Slice 91 — the `F()` high-scores screen (jc4)

Ports `k.F()` (k.java:2338-2408, proven) — the high-scores table:
`a(30,d(0,5))` title bar, `cU` difficulty page (EASY/NORMAL/HARD via
`d(0,35+cU)`), chevron cycling, `bw` scroll, 8 rows + TOTAL read from
the `bA` save array, footer + back.

## Semantics (proven, file:line)

- `cb=true`; `a(30,d(0,5))` (:2325 — A[3] frames 1@cx200/2@cx120 at
  y=30, fill `j.b(87,39,228,183)` color -14274509, centered bW text).
- `bW.a(cd,d(0,35+cU),200,55,3)` page subtitle (:2342).
- Chevrons: `d(110,15,50,80)?40:36` at (160,55);
  `d(240,15,50,80)?39:35` at (240,55) — `d()` = J,K point test.
- Rows (:2351-2361): `"LEVEL "+(bw+i+1)` at (107,75+i*14) anchor20;
  `a(bA,81+(cU<<4)+((bw+i)<<1))` LE-16 short at (293,...) anchor24;
  `sA<=0 → "-"`.
- TOTAL (:2362-2370): `d(0,23)` at (107,197), sum of the 8 shorts.
- Footer `a("",d(0,17))`; `v(131072)` → `l(3);K(4);z(30)` (:2404).
- Nav: `v(16388)` → `bw>0?bw--:noop`+`z(23)`; `v(33024)` → `bw<0?bw++`
  (**verbatim dead arm** — bw clamps ≥0 so it never fires);
  `v(8256)||c(240,15,50,80)` → `cU=(cU+1)%3`; `v(4112)||c(110,15,50,80)`
  → `cU--; <0→2`. `c()` = H,I press-point test.

## Port

- `Level0World.kt`: `scoreAt(i)` LE-16 read on `kBA`; `menuF()` routed
  for jC==4 (replaces generic menuStates path); `menuFooter` 4 →
  `("", BACK)`; `kCU`/`kCb` fields already existed (l(4) stash).
- `convert_slice1.py`: `clip95` = pack-2 entry-003 (`A[3]` title bar,
  45 anims/96 frames/61 modules — proven via `A[3]=J(3)` k.java:4068).
- `Level0Game.kt`: `clips[95]` loaded.
- `Level0Renderer.kt`: `scoreScreen()` — full F() surface (title bar,
  subtitle, chevron pressed-frames via `pointerDownIn`, rows, TOTAL);
  `d()` J,K-test mapped to the same press-point helper (`inferred` —
  touch has no hover).
- `Slice1Test.kt`: `Slice91Test` (8 tests).

## Gates

- verifier `ok:true`; 57 unittests; `:core:test` green;
  `:android:assembleDebug` green.
