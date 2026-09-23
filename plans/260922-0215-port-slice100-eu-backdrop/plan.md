---
title: "Port slice 100 — eu backdrop tile layer (static parallax grid)"
phase: port-slice-100
status: done
---

# Slice 100 — `eu` backdrop tile layer

Ports the pack-15 `eu` backdrop to `Level0Renderer`: the 21×13-cell tile
grid painted into the original's 420×260 `dJ` offscreen buffer and
toroidally blitted behind everything. On level 0 the parallax factors
zero out — the backdrop is a fully static screen-space grid.

## Original semantics (proven — `reconstructed-project/src/structured/k.java`)

- Loader `I(int)` (:5245-5268): `eu = H(8)` cells, `ev = j.e(10)` flags;
  `bt=21`, `bu=13` captured before `et`/`ep`/`er` — so `bt/bu` are the
  **eu dims**, `bp/bq` the level dims (627×55).
- Cell painter `a(g,i,i2,i3)` (:4504-4518): `eu[i3]&255 != 255 →
  dW = eu[i3]&255`; `dX` = 2-bit flag from `ev[i3>>2]` bits
  `(3-(i3&3))<<1`; `dY=(dX&1)?20:0`, `dZ=(dX&2)?20:0`; then
  `bZ.a(g,dW,i+dY,i2+dZ,dX,0,0)` — the same flag/flip-anchor shape as
  `ep`'s painter `b()` (:4529).
- `h()` (:4372-4409): repaints dirty dJ regions at buffer anchor
  `((i9%21)*20, (i6%13)*20-20)`, cell index `i9+i5*bt`. The `aR<0 →
  i5=i6` arm means the `dS/dT/dU/dL` vertical-parallax machinery is
  bh3-flying-only (`aR=-1` on level 0, :1862) — dead here.
- Composite (:2697-2771): `euX = (O * (bt<21 ? 0 : bt-21))/(bp-21)`,
  `euY = (P * (bu-13))/(bq-13)` — level-0: **euX=euY=0**; `i12=euX%420`,
  `i13=euY%260`, `i14=(euX+400)%420`, `i15=(euY+240)%260` → `d()`
  quad-split wrap blit of the 400×240 view.
- `ef[]` (:264) static-init all-false → the `z[58]` `ft/fu` drift
  overlay is a dead arm.
- Draw order (:2800-2819): `eu` composite first; `bh[aj]==4 → e()+f()`
  (ep then er); `bh!=3 → e()` only; `bh3 && P>0 → f()` only. Level-0
  `bh[0]=4` → eu → ep → er → entities.

## Port mapping (`rewrite/gdx/.../Level0Renderer.kt`)

- New block before the ep/er loop: per cell `(cx,cy)` the wrapped
  screen anchor `wx = (cx*20 - sx) mod 420`, `wy = (cy*20-20 - sy) mod
  260`, drawn via the existing `drawTileCell` (skip 255/OOB + flag
  flips); the toroidal blit is reproduced by drawing each cell at
  `{wx, wx-420} × {wy, wy-260}` when inside the 400×240 view — a cell
  crossing a wrap edge splits exactly like the original's `d()` quad.
- `euX/euY` computed from the layer dims (`eu.cols<21 → 0`, else
  `camX*(eu.cols-21)/(level.cols-21)`; `camY*(eu.rows-13)/(level.rows-13)`)
  — keeps the formula for future packs where `eu` out-sizes 21×13;
  on level-0 both are 0 (fully static, camera-independent).
- **No dJ buffer**: the original's dirty-region repaint exists for
  parallax correctness on flying levels; for a static grid a per-frame
  direct draw is pixel-identical.
- Also fixed (drive-by, pre-existing desktop crash): module-texture
  load skips missing files — the `clips[12] = clips[94]` alias shares
  module metadata but resolves a `clips/clip12/` dir that was never
  converted; `aU==2` non-pixel modules already skip empty names, so a
  missing file is the same "draws nothing" case.

## Verification

- `:lwjgl3:run` on the desktop: level-0 boots (637 records, npcs=584),
  the eu backdrop draws as the static colonnade + hanging ornaments
  behind entities — previously black.
- eu layer data check: 273/273 cells non-255 (fully populated
  21×13 grid; mirrored palindrome rows = symmetric architecture).
- Gates: verifier `ok:true`, 57 unittests, `:core:test`,
  `:android:assembleDebug` — all green.
