---
slice: 220
title: k.b(boolean) — input-lock veil latch + visible et-cell window
status: landed
---

## Source (proven, reconstructed-project/src/fallback/k.java:9062-9340)

`k.b(boolean)` is called from `stateL(12/13)`, `stateL(14)` while
`jC∈{8,21}`, and the `jC==12/13` tick arm — every port caller passes
`b(true)`. The `b(false)` early-out `jc∈{12,13,31}` (:9064-9072) is
unreachable from those sites; kept as a `full` param for the verbatim
shape.

Arms:
1. `k.am && !k.dd → k.dd=1` + the `j.a` veil ops (:9080-9101). The 3/4/6-arg
   `j.a` forms are unrecovered stubs (j.java:4307/4311); by shape they are
   fill 400×240 → alpha-strip 100 → blit `cd` → reset — inferred
   translucent-black "input locked" dim. Our renderer draws it every frame
   while `kAm` holds (no persistent back-buffer).
2. Visible et-cell window: `camX/20..(camX+399)/20` ×
   `camY/20..(camY+239)/20`. The `(bt-21)/(bp-21)`, `(bu-13)/(bq-13)`
   rescales are proven =1 (`bt=bp`, `bu=bq` at level load :19116-19118).
   Negative camY gets the `-20` floor-division bias (:9084); `vy0<0`
   clamps only when `bh[kAj]!=3` (flying missions scroll above the level).
   `vx1/vy1` are never clamped (verbatim).
3. `dM` full-invalidate + `dN..dQ` prev-window compare + `h()` edge-strip
   marks (:9185-9290, h() at :15935) — proven-dead bookkeeping: the
   original marks ring-buffer slots of its tile back-buffer; our
   immediate-mode renderer draws the window fresh each frame. Compare/flag
   state kept verbatim (`visDirty`, `visX0/Y0/X1/Y1`).
4. Tail (:9306+) = eu 420×260 toroidal blit via `d()` rects — already
   covered verbatim by the renderer's eu arm.

## Port

- `Level0World.scrollBounds(full)` (was a stub) — full port: veil latch,
  window compute, compare/flag bookkeeping.
- `vis*` fields (`visX0/visY0/visX1/visY1/visDirty`) internal for tests.
- `Level0Renderer`: translucent-black veil (0x64000000) after the entity
  pass, under HUD/dialogs, drawn while `world.kAm` holds.

## Tests (Slice220Test, 6)

- input lock latches `kDd` once via `stateL(12)`; unlock re-arms the latch.
- window tracks camera in et cells (cam 420,260 → 21/40/13/24).
- negative camY clamps `visY0=0` on grounded missions; flying (`aj=1`)
  keeps the negative top (`visY0=-2, visY1=9`) — `bh[kAj]==3` arm.
- camera jump across the window sets `visDirty`.

## Fix during review

An over-broad `stateL(14)→stateL(12)` test-edit restore flipped legitimate
`w.stateL(12)` calls (fail-screen tests) to `stateL(14)` — restored the
entire pre-slice file from HEAD and re-appended only the new test block.
All 160 core suites green.

## Gates

verifier ok:true · 57 unittests · :core:test 160 suites 0 failures ·
:android:assembleDebug · :gdx:build
