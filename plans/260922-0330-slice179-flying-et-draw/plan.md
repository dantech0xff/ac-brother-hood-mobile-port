---
title: "Slice 179 — bh3 et-via-dL draw arm (flying canyon render)"
phase: port
status: done
---

# Slice 179 — `et` drawn through `dL` on flying packs

## Source (proven-shape)

`b(Graphics)`'s flying arm (k.java:4522 area — the `a(dK,…)` cell blit):
grounded packs skip `et` entirely (collision-only); on `bh==3` `et` is the
canyon terrain and is drawn THROUGH the `dL` stamp grid at the parallax
offset — logical cell `(x,y)` → tile `et[dL[x%21][y%13]]` at
`(x*20-i2, y*20-i3)` — so armed copy bands draw where `g()` says they
collide.

## Ported

- `LevelPack.stampAt(cx,cy)`: `dL` index lookup with `g()`'s
  `i4<0||i4>=len → -1` guard — core-testable half of the draw.
- `Level0Renderer`: bh3 arm after the `eu` backdrop — for
  `flyingGrid != null`, scans the parallax window
  `[px/20..(px+400)/20]×[py/20..(py+240)/20]`, resolves each logical
  cell via `stampAt`, blits `et.cells[idx]` with `et.flag(idx%cols,
  idx/cols)`. Grounded path unchanged (layer-0 still skipped).

## Tests (Slice179Test, 2)

`stampAt` resolves live window cells + guards wrap-band indices to -1;
-1 on grounded packs.

## Gates

`:core:test`, `:gdx:build`, `:android:assembleDebug` green; verifier ok.
