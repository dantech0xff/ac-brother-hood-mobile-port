# Port slice 10 — HUD sync meter

## Mining — proven (k.java:5381 `c(boolean)`)

HUD sync meter block:
- `g.f(ax)` caps `x[1]` to cap `ax` (grows 30+15/tier via `s()` score
  thresholds `dE[]` — unmined, cap fixed 90 in port).
- `z[12].a(cd, 2, (ax/15)-1, 2, 30, …)` — meter track art (z[12] clip
  undecoded → skipped).
- `A[4].a(cd, 8+bL, 0, 22, 30, …)` — side icon (skipped).
- `j.a(cd, 43, 6, (x[1]*11)/15, 20, true)` — **setClip** (j.java:1052) —
  reveals bar `z[12]` anim 6; meter width = `x1*11/15` px (66 at x1=90).

## Port (rewrite/gdx)

- `Level0Renderer` draws dark track + fill at screen (43,6), w=`x1*11/15`,
  h=20 — geometry proven; fill color/track **inferred** (real bar is
  clipped z[12] art, pack not yet decoded).

## Kiểm chứng

- 42 `:core:test` xanh; `:lwjgl3:build` + `assembleDebug` clean.
- Emulator: `reports/emulator-hud-meter.png` — gold 66px bar top-left.

## Gaps

- z[12] bar art + A[4] icons; cap growth via `s()`/`dE`; eu backdrop layer.
