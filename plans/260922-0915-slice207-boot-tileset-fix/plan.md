---
title: "Slice 207 — boot fix: tileset path doubling + et null-sentinel"
phase: port
status: done
---

## Summary

Golden-path emulator verification (testing agent, `plans/260922-0730-demo-verify/reports/`)
found HEAD crashes on `Level0Game.create()`: two regressions introduced
by the slice-176/177 all-mission conversion:

1. **Path doubling** — `"level${firstPackTilesetDir}/tileset-$ts"` where
   `firstPackTilesetDir = "level0"` produced `levellevel0/tileset-…`
   → `Gdx.files.internal` throws → FATAL before any frame.
2. **`tileset-0` request** — the `et` collision layer (layer id 0)
   carries `tilesetClip == 0` as the converter's null sentinel
   (`convert_slice1.py:326` — `("et", 0, 0, True)`); loading it asked
   for `level0/tileset-0/clip.acpk`, which does not exist.

## Fix

Both tileset-id collections (`create()` and `loadMissionTilesets()`)
now filter `it.id != 0` — the `et` layer is collision-only and never
needs a tileset clip. Correct on every pack: real tileset ids live on
`ep`/`eu`/`er` (ids 1/2/3).

Caveat documented, not changed: level5's `eu` genuinely uses tileset-0
(`ej[21]=0`); under the `clips[-ts]` keying `-0 == 0` collides with the
player-clip slot. Loading it there overwrites `clips[0]` — acceptable
on flying packs (the ax25 player draws via `bi[25]=16`), flagged for
the flying-mission verification pass.

## Verification

- `:gdx:build` + `:android:assembleDebug` green.
- Level0 ACLV layer audit: `{et:0(sentinel), ep:11, eu:10, er:12}` —
  the load set after filtering is exactly `{10,11,12}` = the on-disk
  `tileset-*` dirs.
- Pending: emulator re-verify on the next demo run.
