---
title: "Slice 120 — a(x,y,w,z2) edge strip + fL pause button (k.java:1040-1065)"
date: 2026-09-23
status: done
---

# Slice 120 — the in-game pause button visual

## What was ported

The pause-button block inside the jc8/21 `I()` arm tail
(k.java:1040-1056, proven):

- `if (J())` — `J()` = `!(j.c==12 || j.c==13)` (k.java:2653), verbatim;
  the block lives inside the case-8/21 arm so the button draws under
  both play and dialog-overlay states.
- `fL = new a(A[2], 377, 19)` — lazily-created `UiAnimObject` on the
  A[2] UI bank (our clip93 = pack-2 entry-002).
- `d(354,0,46,37)` held-inside → `a(359,32,36,true)` pressed edge +
  `fL.a(30,1)`; else `a(359,32,36,false)` + `fL.a(25,-1)`.
- `fL.b(j.f)` tick (the fixed 62ms delta, not wall-clock) + `fL.c()`
  draw of the armed anim at (377,19).
- The press chain `c(354,0,46,37)` → `E(262144)` → `v(262144)` →
  `C.Y(); bw=0; l(14)` was already wired in `consume` — only the
  visual was missing.

## `a(int,int,int,boolean)` edge strip (k.java:2242-2268, proven)

- `z2` picks the anim pair {41,42} pressed / {43,44} idle on `A[2]`.
- `cQ`/`cR` = cached `b.e(b.d(anim,0))` pixel widths (our
  `animObjWidth` = frame-0 composite-object `bounds[obj*4+2]`;
  `d(i,j)` = module | flags<<2 object resolution).
- Left corner `i4`, `i5` tiles while `next+cR < x+w`, final `i5`
  right-aligned at `x+w-cQ-cR`, `i4` at `x+w` mirrored (flags=1) —
  the verbatim do/while.

## Audited-already-done backlog items

- `k.l(i)` entry arms (medal route 15→22/10, `K()` pages, `fd`, `eC`,
  `bw`, `e.b()`/`z()` tails) — all live in `stateL`.
- `f.a` IGP triple (Z()/a(0)/a(cd)/enterIGP) — stubs verbatim since
  slice 117 (`f.a()` is always -1 on this target).
- `dA` — dormant clip-12 marker entity, create+null only; dead field.
- `g.w` — `static int[]` weapon-icon bounds written once in jc9,
  never read; dead field (noted, not ported).
- `fS` `d(0,111)` tip marquee — already `kFs`/`tipStr`.
- `A[]` bank — A[0..5] = pack-2 entries 0-5 = clips 96/97/93/95/98/99,
  all loaded and drawn.

## Gates

- `./gradlew :gdx:build :core:test :android:assembleDebug` → OK
- `python3 -m unittest` → 57 OK; verifier → `ok:true`
