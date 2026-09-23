---
title: "Slice 119 — i.a(IIIIZ) ax35 eagle-view minimap composite"
date: 2026-09-23
status: done
---

# Slice 119 — the `k.aQ` minimap composite

## What was ported

`i.a(int i, int i2, int i3, int i4, boolean z2)` (i.java:20129-20166,
proven) — the offscreen map surface `k.aQ` that ax35's `bQ()` S==0 arm
fills via `a(ak+Z[0], al+Z[1], Z[2], Z[3], Z[4]==0)` (:19697) and the
`b(z2)` overlay tail blits top-right (`drawImage(aQ, 198-aQ.getWidth(),
5)`, k.java:3140-3141). Our `volPaintRect` already carried the rect; the
bare `outlineAr` placeholder is replaced by the full composite.

## Composite (verbatim transcription)

- **`k.aQ` offscreen** → clipped direct draw at (198-w, 5) — same
  visible result without the copy.
- **`i7 = -(i4-240)`** — vertical bottom-anchor shift, verbatim.
- **eu stamp grid** (:20136-20141): `(w/20+1)×(h/20+1)` cells of layer
  id 2 (`eu`, `bt`=cols) at `(i9*20, i8*20+i7)`.
- **`k.a()` ep front rect** (k.java:4417-4449) + **`k.b()` er bottom2
  rect** (k.java:4453-4498) → `minimapLayer(id1/id3)`: world cells
  `(x/20..(x+w-1)/20) × (y/20..(y+h-1)/20)` stamped at
  `(cx*20-x, cy*20-y+i7)`; `i2<0 → i2-=20` rounding quirk verbatim;
  bh3's `dL` arm is flying-only (dead on level 0).
- **Enemy blips** (:20145-20158): ax ∈ {11,73,35,79}, `(P&128)==0`,
  gate `i.a(i,i2,i+i3,i2+i4,Y)` = rect-vs-Y[4] overlap (i.java:520 —
  `minimapOverlap`). Draw order verbatim: `U>=0 → (S,T)` frame;
  `S>=0 → S` object; `T>=0 → T` object — all at `(ak-x, al-y+i7)` with
  `P&7` transform. ax79 preps `aa.a(Z[1])` palette (ported via the
  palette param; `aa.l(Z[0])` variant select unmodeled — flagged).
- **Border** `drawRect(0,0,i4-1,i3-1)` — verbatim h/w arg swap →
  `outlineAr(ox,oy,h-1,w-1)`.
- **`z2` (`Z[4]==0`) is a dead param** in the composite body — kept in
  the call signature for provenance, flagged in the doc comment.

## Gates

- `verify-static-reconstruction.py` → `"ok": true`
- `python3 -m unittest` → 57 tests OK
- `./gradlew :core:test :gdx:build :android:assembleDebug` → OK

## Fidelity notes

- World-side contract (`tickAx35` S0 → `volPaintRect` = (ak+Z0, al+Z1,
  Z2, Z3)) already pinned by slice-44 tests; the composite is
  renderer-only.
- Exact pixel parity can't be diffed without running the original —
  the transcription is verbatim line-for-line where the semantics were
  recoverable; `aa.a(g,T,…)` 6-arg arm mapped to `drawObject` (the
  anim-level draw), flagged `inferred` in the doc comment.
