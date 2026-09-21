---
phase: 4
title: LibGDX Adapters and Launchers
status: completed
priority: P1
dependencies: [2, 3]
---

# Phase 4: LibGDX Adapters and Launchers

## Context Links

- [Plan](./plan.md)
- Module contract: `docs/modern-mobile-technical-design.md` section 6
- Runtime flow: `docs/modern-mobile-technical-design.md` section 7

## Overview

Implement the `rewrite/gdx` adapters over the core tick engine and the thin
launchers.

- `GdxGame` (`game-app` role): owns lifecycle, content load, and the
  render/tick split. Fixed-step accumulator, max 4 catch-up ticks, reset on
  resume.
- `PixelRenderer` (`render-gdx`): renders into a 400x240
  `FrameBuffer`, then draws it integer-scaled with `Nearest` filtering,
  centered with letterbox. Reads only the last committed tick snapshot; never
  mutates world state.
- `InputQueueBridge` (`input-runtime`): LibGDX input callbacks append
  timestamped events to the core `InputQueue`; render loop shows derived touch
  state without touching gameplay.
- `AudioBridge` (`audio-runtime`): executes deferred `PlaySfx` commands emitted
  by committed ticks; logical slot decisions stay in core.
- `SaveBridge` (`save-runtime` adapter): implements core `SavePort` over
  `Gdx.files.local`, atomic write via temp-file rename.

## Launchers

- `rewrite/android`: `AndroidApplication` launcher, landscape, sensor
  orientation, `uses-sdk` 24+/36.
- `rewrite/lwjgl3`: desktop window launcher for host verification.
- `rewrite/ios`: RoboVM launcher sources only; module excluded from
  `settings.gradle.kts` on non-macOS hosts so the spike stays buildable here.

## Validation

Desktop launcher shows the converted scene; Android APK installs and runs on
the emulator with visible letterboxed 400x240 output.
