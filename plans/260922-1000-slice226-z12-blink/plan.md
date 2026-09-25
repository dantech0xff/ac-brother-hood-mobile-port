---
slice: 226
title: z[12] blink marker beside settings rows 32/33/34
status: shipped
---

# Slice 226 — z[12] blink icon

The `b()` row-label switch (k.java:6080-6092, proven) draws a blinking
`z[12]` marker beside rows whose string index is 32/33/34 — gated
`!eJ` (tutorial flag clear) and phase `j.g % 10 > 5` (visible on the
back half of each 10-tick cycle), at `(rowTextX+86, rowCenterY-7)`,
clip-12 anim 1 frame 0. Our renderer had the palette arm (`bW.l(3)`)
but not the marker.

## Changes

- `Level0World.menuRowEntry(i13)` — new helper returning the resolved
  `eA[bv][i21]` string index (−1 under jC==19, which bypasses the table).
- `menuPanel` row loop: after the text draw, when
  `menuRowEntry(i13) in 32..34 && !kEJ && jG%10 > 5`, draw
  `drawFrame(12, 1, 0, i14+86, i9+(i4>>1)-7, 0)`. `i14` = the same
  `menuI14` text-x the source's `r23` computes.
- Fixed stale comment claiming jc2 side icons unported — they draw at
  the row-icon picks (frames 9/5, 4/0).
- Test: `menuRowEntry resolves eA table indices` (jc30 → 106/107,
  jc19 → −1).

## Gates

- `:core:test` (incl. Slice89Test 9/9), `:gdx:build`,
  `:android:assembleDebug` → green
- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover` → 57 tests OK
