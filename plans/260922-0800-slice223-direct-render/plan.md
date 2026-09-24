---
slice: 223
title: direct letterbox render — FBO pass removal
status: shipped
---

# Slice 223 — direct letterbox render

Second half of the software-GL perf work (slice 222 killed per-frame
allocations). The renderer drew the whole scene into a 400×240 FBO then
blitted the FBO texture fullscreen — one extra full-screen textured copy
per frame, a real cost on the emulator's software GL.

## Change

`Level0Renderer.render` now draws straight into the letterboxed GL
viewport:

```kotlin
val sc = minOf(sw / VIEW_W, sh / VIEW_H).coerceAtLeast(1)
Gdx.gl.glViewport(0, 0, sw, sh); ScreenUtils.clear(black)  // bars
Gdx.gl.glViewport(offsetX, offsetY, VIEW_W * sc, VIEW_H * sc)
batch.projectionMatrix.setToOrtho2D(0, 0, VIEW_W, VIEW_H)
// …all scene draws unchanged…
```

**Output equivalence:** `sc` is always an integer ≥ 1 (integer division)
and filtering is nearest, so rasterizing primitives at `dw = 400·sc`
produces the identical pixel image as upscaling the FBO — the FBO only
existed to do that copy.

`clipScissor` was the one FBO-space GL call: now maps world → viewport
(`offsetX + x·scale`, `offsetY + (VIEW_H − y − h)·scale`, `w·scale`,
`h·scale`). `scale`/`offsetX`/`offsetY` still feed `Level0InputBridge`'s
screen→world tap mapping (computed identically, moved to frame top).

`fbo` field, its `FrameBuffer` alloc/filter, `fbo.dispose()`, and the
`FrameBuffer` import all removed. `PixelRenderer` (the spike-harness
renderer) keeps its own FBO path — untouched.

## Gates

- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover` → 57 tests OK
- `:core:test`, `:gdx:build`, `:android:assembleDebug` → green

## Expected

Halves the per-frame fullscreen fill on software GL (scene + blit →
scene only). Rendering engineering only — no sim behavior changed.
