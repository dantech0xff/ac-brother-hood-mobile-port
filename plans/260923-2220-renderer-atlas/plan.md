---
title: "Slice 170 — renderer texture atlas (perf fix)"
phase: port
status: done
---

# Slice 170 — `Level0Renderer` global texture atlas

## Problem (found by the emulator demo run)

Level-0 rendered at ~0.2–0.5 fps on the swiftshader emulator. Stack
sampling put `GLThread` at ~97% CPU inside `glBufferData`: every clip
module was a standalone `Texture`, so every `SpriteBatch.draw` forced a
texture-switch flush + vertex-buffer upload — thousands of flushes per
frame. `drawModule` also allocated a fresh `TextureRegion` per call
(GC churn), and the level draws ~2–5k modules/frame (tiles + composites).

## Change (renderer-only, no sim semantics)

`rewrite/gdx/.../Level0Renderer.kt`:

- `create()` now packs every module `Pixmap` into ONE global
  `PixmapPacker` (2048² pages, pad=2, duplicateBorder) and generates a
  `TextureAtlas`; `clipModules[pack][i]` points at `atlas.findRegion`
  instead of a per-module `Texture`. All module pixels total ~4.5 MB →
  one page covers the whole corpus.
- `white` is now a 1×1 atlas region (`packer.pack("white", ...)`) so HUD
  fills stay on the atlas texture.
- `drawModule` draws through a shared `drawScratch` region
  (`setRegion(src)` resets uv, flips mutate only the scratch) — zero
  per-call allocation.
- Palette variants keep the existing lazy standalone-`Texture` path
  (rare; avoids mutating the hot atlas at runtime). Tracked in
  `paletteTextures` for `dispose()`.
- Aliased pack ids (`clips[12]===clips[94]`) pack once under the
  canonical key via a `Clip`→pack-id identity map.
- `dispose()` releases `atlas`, `packer`, and `paletteTextures`.

## Measured (emulator-5554, swiftshader)

| Metric | before | after |
|---|---|---|
| GLThread CPU | ~97% (stack: `glBufferData`/module) | **~10%** |
| gfxinfo per-frame rows | saturated ~4950 ms | **67–118 ms** |
| unique frames delivered | collapsed ~0.2–0.5 fps | **~16–18 fps = 1/62ms tick** |
| compositor delivery | ~17 fps | **42 fps** |

Visual regression: **none** — pixel-identical side-by-side at the same
location (tiles, composites, HUD, guard sprites). 60 s `atlas-demo.mp4`
recorded under `plans/260923-0800-gameplay-demo/reports/`.

Note: gfxinfo "GPU p50" saturates at 4950 ms and is unreliable for this
app — measure via unique-frame counting + GLThread % (documented in
`.agents/skills/android-emulator-testing/SKILL.md`).

## Gates

`verify-static-reconstruction` ok:true; 57 unittests; `:core:test`;
`:gdx:build`; `:android:assembleDebug`; LWJGL3 boots level0
(637 records, npcs=599) — all green.
