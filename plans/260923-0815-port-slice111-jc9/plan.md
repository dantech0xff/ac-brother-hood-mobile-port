---
title: "Port slice 111 — j.c==9 load-screen tick (G() done-check → l(8))"
phase: port
status: done
---

# Slice 111 — `k.a()` case 9 (load screen → play)

## Source (proven)

- `k.a()` case 9 — `reconstructed-project/src/structured/k.java:1067-1088`.
- `G(int)` staged loader — `k.java:4741-5090` (returns `i > 164` = done).
- `w(int)` — `k.java:5593-5595`: `(eM & i) != 0` — the RELEASE edge.
- `N()` draw — `k.java:3472-3513` (already drawn by `loadScreen`).

## Semantics

`G(j.g)` loads one resource per frame milestone: 1 strings+save bytes,
2 `U()`+bh3 `dL`, 3 `I(aj)`, 4-7 tilesets, 8 `K()`+scripts, 9 clip-demand
`el[]`+`dv`, 10-84 per-clip `z[]` loads, 85 palette binds, 87-161 `em[]`
anim masks, 163-164 anim links — done at `j.g > 164`. Our clips/scripts
load at world-init, so `G()` collapses to its counter semantics
(`bootR()` made the same collapse); `j.g` is `jG` — reset to 0 by every
`stateL` (`:1646`, matching `l()`'s `j.g = 0`).

Then `w(65568) || j()` → restore mission state from the save bytes
(`ax=dB; ay=dC; aN=dF`), `dz=120; aw=0`, `l(8)` + `z(23)` + `F(aj)`.
`missionInit()` (already firing on `l(8)`-from-jc9) covers the
`g.e(ax)`/music arm; `bG`/`dl`/`A[]` release are script/render side
(unported).

`w(65568)` is the **release** edge (`eM`) — "let go to play" after the
bar fills; `j()` = `pointerStrip()` (play-area tap-release).

## Reachability

`menuStates += 9` (dispatch gate) — closes the full new-game chain:
boot `R()` → sound prompt (23) → title (18) → menu (2) → NEW GAME →
difficulty (`bv==2`) → browse (30, `fF=20`) → story (20) → **load (9) →
play (8)** — every `j.c` in the chain now ticks ported code.

## Gates

verifier `ok:true`; 57 unittests; `:core:test` (Slice111Test ×4:
early-release ignored, `j.g>164` + release → play, save-byte restore +
`dz`/`aw`/`z(23)`, strip-tap → play); `:android:assembleDebug`;
`:gdx:build` — all green.
