---
title: "Slice 351 — no menu panel on the eC=121 \"GAME DATA HAS BEEN DELETED\" screen"
phase: "port"
status: "done"
slice: 351
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - reconstructed-project/src/structured/k.java:6204-6228
  - plans/260922-0730-demo-verify/reports/REPORT.md:1507-1508
---

# Slice 351 — `ae()` eC=121 draws no panel

Phase 1 item 1.6 (G6) of the
[parity-gap closure plan](../261003-0700-parity-gap-closure-android-hardening/plan.md).

## Finding (proven)

`ae()` (`structured/k.java:6204-6228`) serves jc23 and jc28. After
`f(false)`, the `eC == 121` arm draws the wrapped `d(0,121)` message at
y=120, handles BACK (`v(131072)` → `l(3)`), draws the `a("", d(0,17))`
footer and **returns** — it never reaches `d(93,120,214)` (the panel) or
`L(ey)` (the rows). The port's renderer drew `menuPanel` for every
`panelVisible` state, so after confirming the wipe the YES/NO rows stayed
under the message (device Run-27, `REPORT.md:1507-1508`). The world-side
logic (`menuAe`) already mirrored the arm.

## Fix

- `Level0World.menuPanelDrawn` = `panelVisible && !((jC == 23 || jC == 28)
  && kEc == 121)`; `Level0Renderer` calls `menuPanel` only when it is true
  (the message and footer still draw under `panelVisible`).

## Test

- `Slice351Test` (3): the 121 screen keeps message + footer but drops the
  panel; every other `panelVisible` state still draws it; the real wipe
  confirm (eC 69, YES row, M_CONTEXT) lands on the panel-less screen.
- The draw itself is GL-side (no unit harness); device check in Phase 2.

## Gates

Verifier `ok:true`; unittest 57/57; `:core:test` 1623/1623; `:gdx:test` 9/9;
`:android:assembleDebug` OK.
