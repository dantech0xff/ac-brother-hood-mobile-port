---
slice: 225
title: menuRowText arm inversion fix — jc19 LEVEL-n vs eA[bv] table
status: shipped
---

# Slice 225 — row-label arm inversion

Auditing the `b(x,y,w,z2,z3)` row-label proc (k.java:6046-6140) found
`menuRowText` had its two arms **inverted** vs the source:

| state        | source                                  | port (before)     |
|--------------|-----------------------------------------|-------------------|
| `j.c == 19`  | `d(0,10)+" "+(row+1)` = "LEVEL n"        | eA[bv] table      |
| `j.c != 19`  | `d(0, eA[bv][i21])` + suffix switch      | "LEVEL n"         |

Consequence: the level-select screen (jC==19) showed table strings, and
`af()` (jC==30, entered via `bannerK(5)` → `kEA[5]={106,107,108,109}` =
EZIO/EXECUTIONER/DOCTOR/NOBLEMAN) wrongly rendered "LEVEL n" — which
made the earlier "mission-browser" labeling misleading: af() is the
assassination-**target** browser, jC==19 is the level select.

## Changes

- `menuRowText` arms swapped to match source. `i21` = `menuM(kBv,row)`-
  resolved index; the verbatim `i21=0` pin for row0&jC==2 stays as a
  comment (`m` already returns 0 there).
- Suffix arms kept (83/84 → ": "+d(0, bE/bF?21:20); 97 → ": "+d(0,35+au);
  123 → cm suffix; 32/33/34/103 → pal 3). Known-unported draw-side:
  the source also blinks `z[12]` beside 32/33/34 while `!eJ`.
- Tests updated to encode the corrected contract: `jc19 rows render
  LEVEL n`; jc30 asserts `d0(106/107)` mission-target names.

## Gates

- `:core:test` → green; `:gdx:build`, `:android:assembleDebug` → green
- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover` → 57 tests OK
