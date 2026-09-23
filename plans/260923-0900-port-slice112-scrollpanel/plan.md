---
title: "slice 112 — scrollable text panel a(bVar,i,str,…)"
phase: port
status: done
---

## Summary

Port the scrollable-text-panel proc `a(bVar, i, str, x, y, w, h, flags,
align, wrap)` (k.java:5627-5693) — `fe` scroll velocity + `fd` text
offset semantics — and wire it into the jC==20 story screen's cu5 arm
(:1273, :1282), where the finished text scrolls inside the (85,120)
viewport region. Fixes two slice-110 fidelity bugs found in the same
read: the `z[39]` icon draws for ALL `cu >= 2` (not `cu <= 4`), and
`eZ`'s tall-text adjust is a draw-tail write-back, not an l(20) init.

## Original semantics (proven)

- `fe` velocity, press-EDGE driven: `v(33024)` → fe-- (floor -5);
  `v(16388)` → fe++ (cap +2; dx/jc24 → -1); fe==0 → -1; `wrap=false`
  forces fe < 0 (:5629-5654).
- `fd` offset: `fd < y3 - iK` → off the top → dx hook (bA[69] seen →
  l(25); else stamp 69, e(true), l(1), dw=255) or `fd = 240`
  wrap-restart; `fe>0 && fd >= h5` → fe=-1 (:5664-5685).
- jc24 only: scrolling stops at `fd < -iK + 160`, arms `dw = 30`.
- iK = text block height: `y.a(str,null)` → b.e (unwrapped) or
  `bVar.k(U[0])` (wrapped); ours = linesHeight (inferred).
- cu4→5 arms `fd = eZ` then calls the proc once (:1272-1273); cu5 calls
  it every frame (:1282). The proc draws clipped to (0,y3,400,h5).
- Draw tail (:1285-1297): icon `z[39]` anim-1 at (eY,80) for cu≥2;
  text at eZ for cu≤4 — eZ recomputed `85-(b.e-120)` per frame and
  WRITTEN BACK so `fd=eZ` inherits the adjusted origin.

## Port

- `Level0World.scrollPanel(str,y3,h5,wrap)` — sim side returning fd;
  `kDw` field added (jc24 end-fade, dormant until that screen is mined).
- `menuJc20` cu4→5 arm calls `scrollPanel(kFb,85,120,false)` once;
  cu5 arm calls it every tick.
- `storyScreen` — icon for `cu>=2` (was `<=4`), scissor (0,85,400,120)
  for both text arms, cu5 text at `kFd`, cu≤4 eZ write-back per frame.
- l(20) `eZ` init computation removed (render-side per the original).

## Gates

verifier ok:true · unittest 57/57 · :core:test (6 new) · android/gdx build
