---
title: "Slice 240 — i.bk() ax60 lift cable draw + ax43 setClip correction"
phase: port
status: done
source_lines: [i.java:42549-42920, i.java:12769-12860, i.java:13985-14005, j.java:2731]
---

## What

Ports `i.bk()` — the ax60 lift/piston platform's cable renderer
(i.java:42549-42920) — into `Level0Renderer`, and corrects slice 239's
ax43 reading of `j.a(Graphics,…,1)` as a fill: it is `Graphics.setClip`
(j.java:2731, proven).

## `j.a(g,x,y,w,h,bool)` — the signature re-read

The 6-arg `j.a` takes `(g, x, y, w, h, boolean)` and calls
`g.setClip(...)` (right-mirroring x when the flag is set and the target
isn't the screen `j.a` buffer — j.java:2731-2784). So the "cover rect"
calls inside `bk()` are **clip regions**, and the tail
`j.a(g,0,0,400,240,1)` is a full-screen clip restore — not a veil fill.

Consequence: slice 239's `CV_VEIL`/`drawFxRect` emissions for ax43 were
wrong. The ax43 arms (L733/L784) clip the blit to the carrier's side
column — already renderer-owned via `drawEntity`'s `clipScissor` arm —
and L10d8's fullscreen `j.a` is just the restore (a no-op for a
per-call scissor). Removed: the fxRect emits, the tail veil, and the
`CV_VEIL` constant. Slice240Test pins "ax43 → 0 fxRects".

## `bk()` structure (i.java:42549-42920)

`switch(S)`:
- `{9,10,16,17} → L38`; inside, S∈{9,10} → L4a (cable up), S∈{16,17}
  → Lca (cable down)
- `{11,13,14,15} → L157`; inside, S∈{13,15} → L169 (cable left),
  S∈{11,14} → L1e9 (cable right)
- default → L267 (clip reset only)

Every arm: `j.a` setClip over the corridor rect (between `W` box edge
and the `Z[5]` anchor), then a `while` loop of link blits —
`aa.a(bg, 8, 0, …)` for vertical links stepped 28px, `aa.a(bg, 12, 0,
…)` for horizontal links stepped 22px — then L267 restores the clip to
400×240. The Lca arm adds `+28` to the far anchor when `Z[1] > 0`.

## Port shape

- `Entity.liftCableArm()` (Entity.kt) — the proven S→arm dispatch,
  renderer-callable and unit-testable:
  `{9,10}→1 up, {16,17}→2 down, {13,15}→3 left, {11,14}→4 right, else 0`.
- `Level0Renderer.drawLiftCable()` — called from `drawEntity` after the
  standard blit for `ax == 60`; each arm `clipScissor`s the corridor
  rect then loops `drawFrame(pack, 8|12, 0, …)` along the verbatim
  step/offset, ending in `clipReset()` (the L267 restore).
- Sim-side there is nothing else: `bk()` writes no fields.

## Deferred

- `by()` (ax40 zipline rope, i.java:48822+, ~1200 lines of rope-mesh
  interpolation over `k.bd[]` siblings) — next slice; partial mine
  recorded in thread notes.

## Gates

- verifier `ok:true`; unittests 57/57; `:core:test` green incl.
  Slice240Test (2 tests: dispatch table, ax43 no-fill); `:gdx:build`,
  `:android:assembleDebug` green.
