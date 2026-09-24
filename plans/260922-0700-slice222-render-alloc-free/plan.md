---
slice: 222
title: renderer alloc-free hot path (frameDraw scratch + placement index access)
status: shipped
---

# Slice 222 — render-loop allocation kill

Demo-blocker: the game reports ~16fps on the software-GL emulator. Slice 170
removed the per-frame `glBufferData`/texture-switch storm via the atlas; the
remaining hot-path cost is **per-call allocation churn** — every module draw
allocated a `Clip.FrameDraw` and every composite draw allocated an
`ArrayList<Triple<Int,Int,Pair>>` + iterator. With ~500 live entities →
several thousand allocations per frame → ART GC pauses on Android.

## Changes

`rewrite/core/.../Clip.kt`:
- `FrameDraw` fields `val` → `var`; new `frameDraw(anim, frame, flags, out)`
  overload writes a reusable scratch instead of allocating (cold callers —
  menu/load paths at renderer :722/:766 — keep the allocating 3-arg version).
- New index accessors `placementCount/Module/Flags/X/Y(obj, k)` reading the
  `placements[]` quad pool directly — `placements()` list builder kept for
  the one cold caller.

`rewrite/gdx/.../Level0Renderer.kt`:
- `private val fdScratch = Clip.FrameDraw(0,0,0,0)` — single render-loop
  scratch (render is single-threaded on the GL thread).
- `drawFrame` (every entity+tile), ghost-trail loop (:1778), `drawEntity`
  (:1895) → 4-arg `frameDraw` into `fdScratch`.
- `drawObject` composite loop → `placementCount` + raw index accessors —
  zero `Triple`/`Pair`/`ArrayList` per draw.

Net: ~2–4 allocations eliminated per module drawn per frame.

## Gates

- `verify-static-reconstruction.py` → `ok: true`
- `python3 -m unittest discover` → 57 tests OK
- `:core:test --rerun-tasks` → green
- `:gdx:build`, `:android:assembleDebug` → green

## Expected

Biggest remaining software-GL cost is now fill rate (scene sprites + the one
FBO→screen blit). GC-churn jank removed — fps improvement is renderer
engineering, not sim fidelity (no bytecode behavior changed).
