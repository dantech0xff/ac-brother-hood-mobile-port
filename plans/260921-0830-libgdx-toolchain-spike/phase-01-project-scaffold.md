---
phase: 1
title: Project Scaffold
status: completed
priority: P1
dependencies: []
---

# Phase 1: Project Scaffold

## Context Links

- [Plan](./plan.md)
- [Framework ADR](../../docs/decisions/mobile-game-framework.md)
- [Technical design module contract](../../docs/modern-mobile-technical-design.md)

## Overview

Create `rewrite/` as a Gradle multi-module Kotlin + LibGDX project. The spike
uses four buildable modules that keep the design's dependency rules without
instantiating all twelve design modules.

## Module map

| Module | Design modules covered | Dependency rule enforced |
|---|---|---|
| `rewrite/core` | core-model, legacy-contracts, content-schema, core-sim, save-runtime schema | Pure Kotlin/JVM; no LibGDX, no platform SDK |
| `rewrite/gdx` | render-gdx, input-runtime, audio-runtime, content-runtime, game-app | Depends on `core` + LibGDX API jar only |
| `rewrite/android` | platform-android | Thin launcher: lifecycle bridge + file paths only |
| `rewrite/lwjgl3` | desktop dev launcher | Runs the same `gdx` app for host verification |
| `rewrite/tools` | tools-content | Python converter; not part of the Gradle build |
| `rewrite/ios` | platform-ios | Scaffold only; excluded from settings on non-macOS hosts |

## Requirements

- Gradle wrapper pinned; Kotlin, AGP, and LibGDX versions pinned and recorded.
- Repositories: GCS Maven mirror + `google()`; Maven Central direct is
  rate-limited on this egress.
- `settings.gradle.kts` gates `ios` inclusion behind `os.name == Mac OS X`.
- Chosen versions: LibGDX `1.14.2` (ADR candidate, latest stable at spike
  date), AGP `8.13.x`, Kotlin `2.2.21`, Gradle `8.14.x`.

## Validation

`./gradlew :core:test` resolves and runs; `:android:assembleDebug` produces an
APK; `:lwjgl3:run` launches the desktop window.
