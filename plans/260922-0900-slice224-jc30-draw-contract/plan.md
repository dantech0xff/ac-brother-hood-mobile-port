---
slice: 224
title: jc30 draw-contract regression test + stale-comment audit
status: shipped
---

# Slice 224 — jc30 draw contract

The earlier demo report listed the mission-browser (`jC == 30`, `af()`)
as "vẽ đen" (renders black). Auditing the current draw path found it
fully wired: `panelVisible` includes jC 30, `menuPanelRect` returns
`(93,46,214)` (af() `d(93,46,214)`, k.java:6254), rows draw via the
shared `b(x,y,w,z2,z3)` panel path with `ey = fQ` mission rows, icon
picks `i13`/`i13+5` on clip-93, footer `a(d(0,79),d(0,17))`. The black
report predates the panel draw landing (slices 87/89/101) — stale.

## Change

One new test (`Slice89Test`): asserts every renderer-facing input for
jc30 — `panelVisible`, panel rect, `menuPanelZ2/Z3` (both false for the
`d()`-routed panel), `menuRowCount == min(8,fQ)`, `menuRowRects`,
`menuI4/I5`, `menuRowText → "LEVEL n"` (`bU[10]`), footer pair. If the
screen ever draws blank again the test pins which input broke.

Also audited remaining `unported`/`stub` markers across core — all are
correctly labeled dead platform code (`f.b()` = Gameloft online
connector spawning a Thread — unportable by design; `E()` teardown
comments; the cv/aF climb branches documented as covered by sibling
arms). No real gaps found.

## Gates

- `Slice89Test` → 8/8 pass; `:core:test` → green
- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover` → 57 tests OK
- `:gdx:build`, `:android:assembleDebug` → green
