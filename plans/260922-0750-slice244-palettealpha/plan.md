---
title: "Slice 244 — paletteAlpha consumer + candle aC double-decrement fix"
phase: port
status: done
confidence: proven (i.java:3095; aa.g = palette alpha)
---

# Slice 244 — `aa.g`/`paletteAlpha` renderer wire-up

## Two coupled bugs found

1. `e.paletteAlpha` (`aa.g` — F()'s palette-alpha output, Entity.kt:4606)
   had **no consumer**: `drawEntity` initialized `var alpha = 255`.
2. The renderer's ax9 candle-S5 block **duplicated F()'s state writes**:
   `ak/al` snap, `P|64`, `aC*255/10` alpha, `aC--`, `removeEntity` —
   running alongside F()'s identical block. `aC--` ran twice per frame,
   halving the fade duration vs source (i.java:3095 — ONE decrement).

## Fix (verbatim split)

- `drawEntity`: `var alpha = e.paletteAlpha` — the renderer consumes
  `aa.g` as written by F().
- Renderer candle-S5 block reduced to a comment: sim-side F() owns
  `ak/al` snap, `P|=64`, `aC--`, `removeEntity` — the S4/S5 `kBK` gate
  and the iCe S4/S2 arms stay (harmless idempotent duplicates of the
  source's draw-side guards).

## Tests (Slice244Test, 3)

`drawStyleF` writes `paletteAlpha = aC*255/10` and decrements `aC`
exactly once per call (5→4→3); `aC==0` → `paletteAlpha=255` +
`pendingRemove`; unlit (`aC==0`) keeps 255.

## Gates

:core:test all green; :android:assembleDebug + :gdx:build clean.
