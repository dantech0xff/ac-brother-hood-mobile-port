---
title: "port slice 81 — M() stats-screen render surface"
phase: port-slice-81
status: done
---

# Slice 81 — the win-stats screen actually draws

## Scope

Render surface for `M()` (k.java:3280-3445) — the world now emits the
per-row strings the original draws via `bW.a(cd,…)`, and the gdx
renderer draws the screen while `jC == 15`.

## Original facts (proven)

- Title: `a(i2, d(0,60))` at `i2 = 25 + ((177-eF)/2)` (k.java:2328 —
  `f(false)` + `A[3]` ribbon sprites at (200,i)/(120,i) + colored box
  `j.b(87,i+9,228,183)` + centered `bW.a(cd,str,200,i,3)`).
- Row labels `d(0,38+i3)` at x=95, y=55+20i (`bW.a` align 20); values
  right-aligned x=305 (306 for time), align 24.
- Row values are the RAW counters — deaths row draws `i7` unclamped
  even though `i4` uses `min(i7,4)`.
- Time format `mm:ss` — `i8/60` + `:` + `|i8%60|` zero-padded <10.
- Total row `d(0,43)` + `j.c(i4,0)` at y=175, drawn once when
  `j.g>10` (the `cb` one-shot latch).
- `j.c(i,0)` (j.java:1202) — thousands grouping; `<1000` raw, else
  separator (`,` inferred — locale switch decompiled oddly).
- `a(d(0,16),str2)` bottom hint (k.java:2270) — NEXT ▸ + typewriter
  text; box `ce`/`c(-5,198,…)` hit-test → `E(262144)`.
- The stats screen has NO `b()` panel — `b(x,y,w,z,z3)` at :5916 is
  the MENU panel renderer (iterates `m(bv,i13)` rows).
- `dg` does NOT tick under j.c==15 — it lives inside the play block
  `case 15` replaces (fixed dispatch order).

## Ported

- World: `statsRowText[5]` filled inside each `j.g` gate (raw values),
  `fmtJ(i)` grouper, `statsTitleY`/`statsScore`/`statsScoreVisible`/
  `statsTypeNext`/`typewriterText` (slice-80 surface now consumed).
- Dispatch order fixed: `jC==15 → M()` runs BEFORE the `dg++/ap[2]++`
  gate — the counters stay inside the replaced play block.
- Renderer: `jC == 15` block — dim overlay, gold ribbon bar + centered
  title (`A[3]` sprites unported → stand-in, `inferred`), label/value
  rows at orig x=95/305 y=55+20i, total row y=175, NEXT+typewriter
  hint bottom-right.

## Tests (Slice81Test, 5)

Row strings per gate, `mm:ss` pad/abs, hidden rows before gates,
`fmtJ` grouping, negative bonus clamps row+score to 0.

## Gates

`:core:test` 661 green, `:android:assembleDebug` BUILD SUCCESSFUL,
verifier `ok:true`, unittests 57 OK (carried from slice 80 — JAR side
untouched).
