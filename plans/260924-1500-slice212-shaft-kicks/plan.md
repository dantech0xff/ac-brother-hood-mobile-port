---
title: "Slice 212 — shaft wall-kick chain proven on real level-0 geometry"
phase: port
status: done
---

# Slice 212 — zigzag kick chain up the x1740–1820 shaft

`Slice212Test` (regression test, real level-0 et grid): hold east, seed
one airborne arc at x1790/y700 → S101 grab at the x1820 face (aU=20,
`cv && aF` gate) → L1a46 auto-bounce `av=!av, ag=-2048, ah=-5120` arcs
west → re-grabs the WEST face at x1740 while still rising → bounce east
→ re-grab x1820 higher.

Measured: grabs at al 731 → 665 → 599, then landed the west-face lip at
y519 — ~66px net climb per kick-bounce. The shaft is a two-face zigzag
wall-jump ladder: west lip y520, east roof y420, '05' catch strip y800.

No code changes — a test-only slice pinning the traversal mechanic that
gates the golden path east of x2163 (the corridor dead-end is building
B's face x2120–2280 over y480–840; the route goes over the roofs).

Gates: `:core:test` green, verifier ok:true, 57 unittests, `:gdx:build`,
`:android:assembleDebug`.
