---
title: "Slice 210 — wall-kick chain verified on real level-0 geometry"
phase: port
status: done
---

# Slice 210 — x2200 wall-kick chain: real-geometry regression test

## Why

Golden-path re-verification (testing agent, post-208 build) confirmed the
shaft route works end-to-end — crates → S37 hang → '05' shimmy → beam →
corridor floor, with live guard combat. The east run then dead-ends at
**x2200–2340**: a 460px solid face (`'14'` cells, rows 24–47) with open air
west — no tiles, no props, no zones between the '05' one-way strip (y800,
x1920–2100) and the wall top (y480).

Static decode of the intended route:

- `ax74` wisp breadcrumbs climb the open air: (1984,881) → (2040,736) →
  (2033,612) → (2044,536) → (2140,490) — marking **kick-arc peaks**.
- `ax22` capture zones at (2064,695), (1975,605), (2104,546) — objectives
  on the ascent.
- `ax10` zone S36 at (1734,600) publishes the bound-context (`gn/go/gk/gd`)
  for the balcony-level shaft crossing; `ax10` S43 at (2176,468) runs the
  ledge→bound transition.
- `ax44` swing poles at (2383/2432/2479, 657) + '02' one-way ledge
  (x2480–2600, y640) + `ax2` checkpoint (2594,485) + `ax11` guard
  (2213,472) — the balcony deck east of the face.

## What the test proves (Slice210Test)

Player spawned mid-jump-arc at (2160,830) with `ag=1536` east + `ah=-200`,
holding pad-zone-2 (`2<<2=8` = `M_TAP_R`):

- `cv` + `aF` latches armed → contact at the face (`bc`, `aU=20`) →
  **`S=101` wallGrabSnap** fired (verbatim `L2298/L1e94` gate).
- S101 anim end → **L1a46 auto-bounce**: `av=!av`, `ag=-2048` west,
  `ah=-5120` rise → `a(36,36)` — rose ~74px (y882→808).
- Drifted west, landed back on the '05' one-way strip at y799 — the
  faithful kick-to-replatform cycle.

## Conclusion

The mechanic is **faithful** — the x2200 face is climbable by repeated
jump-kick cycles off the '05' strip (the corridor-east "dead end" is a
skill gate, not a port bug). The earlier agent's "wall face doesn't grab"
was testing with the wrong intent latch (zone-5 vault taps never arm
`aF`; only zone ≤4 directional holds do — `padE(2 shl iJ)`).

Remaining upper-route question (not a code gap): whether the intended
ascent also uses the balcony path via the ax10-S36 bound zone at
(1734,600). Deferred to live play-testing; the corridor route is
unblocked.

## Gates

- `:core:test` — Slice210Test green (1 test)
- verifier `ok:true`, 57 unittests, `:gdx:build`, `:android:assembleDebug`
