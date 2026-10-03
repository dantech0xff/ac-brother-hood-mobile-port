---
phase: 5
title: Android Internal-Release Configuration
status: in_progress
priority: P2
dependencies: [3, 4]
---

# Phase 5: Android Internal-Release Configuration

## Context Links

- [Plan](./plan.md) · [Baseline audit §4](./reports/baseline-audit.md)
- `rewrite/android/build.gradle.kts`, `rewrite/android/src/main/AndroidManifest.xml`,
  `rewrite/android/src/main/kotlin/com/acrebuild/spike/AndroidLauncher.kt`
- Signing (env-driven, slice 321): `docs/store/store-listing.md` checklist

## Overview

Make the APK behave well on the devices it will actually be sideloaded to —
phones with cutouts and gesture navigation, tablets and foldables under
Android 16 — and produce a signed internal release build. Store work is out
of scope (internal distribution only).

## Progress (2026-10-03)

Container-side steps done; ADR
[`docs/decisions/android-internal-release.md`](../../docs/decisions/android-internal-release.md):

- Step 1 manifest: `appCategory="game"`, `configChanges` +=
  `smallestScreenSize|density|uiMode` (verified in the release APK with
  `aapt dump xmltree`: `appCategory=0x0`, `configChanges=0x1ff0`). Cutout
  check pending a punch-hole AVD.
- Step 2 gesture navigation: pending device measurement (immersive mode is
  on; exclusion rects only if gestures steal input).
- Step 3 logging: release builds log errors only (`AndroidLauncher` sets
  `logLevel` from `FLAG_DEBUGGABLE`); debug keeps the info lines the device
  runs read.
- Step 4: `:android:assembleRelease` builds (unsigned without the signing
  env); R8 off recorded.
- Step 5: app id kept, recorded.
- Step 6: [`reports/perf-baseline.md`](./reports/perf-baseline.md) — APK
  sizes and JVM tick cost per ground mission; device numbers pending.

## Implementation Steps

1. **Manifest**
   - `android:appCategory="game"`: with targetSdk 36, Android 16 ignores
     orientation/resizability locks on large screens unless the app is a game.
   - `configChanges` += `smallestScreenSize|density|uiMode` so fold/unfold or
     theme changes do not recreate the activity mid-game.
   - Cutouts: with targetSdk ≥ 35 the window is edge-to-edge and most cutout
     modes behave as `ALWAYS`. Check on a punch-hole AVD that the integer
     letterbox keeps the 400×240 view clear of the cutout in both landscape
     orientations and that touch mapping stays exact.
2. **Gesture navigation**: the left pad (view x −5..111) and the right-hand
   buttons sit inside the system back-gesture edges. Measure on device; if
   gestures steal input, set `systemGestureExclusionRects` for the control
   zones (within the per-edge budget).
3. **Logging**: route `Gdx.app.log` calls through a debug flag (on for debug
   builds, off for release) — per-sound, per-save and per-frame lines first
   (`AudioBridge.kt:43-78`, `Level0Game.kt:187-244`).
4. **Internal release build**: `:android:assembleRelease` with the env-driven
   signing; document where the internal keystore lives (never committed).
   Keep R8 off and record the decision (internal build; LibGDX keep rules not
   worth the risk now).
5. **App identity**: keep `com.acrebuild.spike` (changing it orphans existing
   internal installs and their saves) unless the user asks otherwise; record
   the decision.
6. **Performance baseline** on the emulator host and one real device if
   available: tick rate (`jG` per second), GLThread CPU, heap after boot and
   after each mission load, APK size. Write `reports/perf-baseline.md`.
   Consider lazy clip loading (`Level0Game.kt:103-157` loads ~50 clips
   eagerly) only if memory is a problem.

## Success Criteria

- Signed internal release APK builds from CI or a documented local command.
- Landscape lock holds on a large-screen AVD (Android 16); no relaunch on
  fold/unfold; controls usable with gesture navigation; no content under the
  cutout.
- Release logcat is quiet during gameplay.
- `reports/perf-baseline.md` committed.

## Risk Assessment

- `appCategory="game"` also changes how the system groups the app (Game
  Dashboard, game mode); harmless for internal builds.
- Exclusion rects are capped per edge; if the pad zone exceeds the budget,
  prefer immersive-sticky behaviour over moving controls (the layout is the
  original's).
