---
title: "Slice 241 — i.by() ax40 zipline rope (marker sag + rope lines)"
phase: port
status: done
source_lines: [i.java:48822-49163, j.java:2792]
---

## What

Ports `i.by()` — the ax40 zipline rope's F()-tail proc
(i.java:48822-49163) — completing the last unported Leec renderer
draw. It is not a pure draw: it **writes** marker entities' `al`
before emitting rope lines, so the port lives in `drawStyleF`'s ax40
arm (with `drawFxLine` for the visible lines).

## Verbatim structure

Gate: `S != 2 || Z[3] == 0 → return` (L309).

1. **Rider scan (L2c)**: walk `k.bd[]` for `e.ax==40 && e.s==this`
   entries where `e == k.aS.ac` (the player's bound entity — the
   gondola, itself an ax40 on the same rope). `r8` = found.
   `r9 = r8 && k.aS.S==164` — riding = the player is in the
   zipline-hang anim.
2. **Marker pass (L8f)**: for every `bd[]` ax40 `e` with `e.s==this`:
   - riding && `e.ak < ac.ak` → `e.al = e.Z[1] +
     (e.ak−ak)·(ac.al−ac.Z[1])/(ac.ak−ak)`  (left slope)
   - riding && `e.ak > ac.ak` → `e.al = e.Z[1] +
     (Z[3]−e.ak)·(ac.al−ac.Z[1])/(Z[3]−ac.ak)`  (right slope)
   - `e.ak == ac.ak` → untouched
   - parked (`r9==0`) && `e.ah==0` → `e.al = al+1; e.Z[1] = e.al`
3. **Rope lines**: `j.a(g,x0,y0,x1,y1)` — the 5-arg drawLine
   (j.java:2792), color `0xFFC94F33`:
   - riding → 4 lines: each rope end → `ac.ak/ac.W[1]` (the gondola's
     top edge), duplicated at `+1` y for a 2px rope.
   - parked → 2 lines: `(ak,Z[1]) → (Z[3],Z[1])` straight, plus `+1`.

## Port shape

- `Entity.drawStyleF` ax40 arm — the whole proc including the `al`
  sim writes (`e.s===this` identity compare, `===` not `==`).
- `LevelCellSource` gains `drawList`/`drawCount` (the `k.bd[]`/`k.be`
  the proc scans — already live on `Level0World`, filled by
  `buildDrawList` before `drawStylePass` invokes `F()`).
- Rope lines emit via `w.drawFxLine` → the existing `fxOverlay`
  drain in `Level0Renderer`.

## Tests (Slice241Test, 4)

- parked: markers `al+1`/`Z[1]=al`, 2 straight lines, color pin.
- riding: both slope formulas verified numerically, 4 sag lines to
  `ac.W[1]` and `+1`.
- gate: `S≠2` and `Z[3]==0` both no-op.
- rider bound but `player.S≠164` → falls back to parked path (`r9=0`).

## Gates

verifier `ok:true`; 57 unittests; `:core:test`, `:gdx:build`,
`:android:assembleDebug` green.
