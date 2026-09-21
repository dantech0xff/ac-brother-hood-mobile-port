---
title: LibGDX Rewrite Toolchain Spike
description: >-
  Execute the ADR mandatory decision gate: a Kotlin + LibGDX toolchain spike
  proving shared-core build/launch, pixel-perfect 400x240 render, touch and
  suspend/resume, converted audio playback, sandboxed save I/O, and a
  deterministic state-hash test outside the UI.
status: in_progress  # iOS gate half pending a macOS session
priority: P1
branch: ''
tags:
  - rewrite
  - libgdx
  - kotlin
  - toolchain-spike
blockedBy: []
blocks: []
created: '2026-09-21T08:30:00.000Z'
createdBy: 'devin'
source: session
---

# LibGDX Rewrite Toolchain Spike

## Overview

First implementation slice of the rewrite track. Consumes the completed static
reconstruction and the accepted technical design
(`docs/modern-mobile-technical-design.md`,
`docs/decisions/mobile-game-framework.md`) and executes the ADR's mandatory
decision gate before any large-scale gameplay port begins.

The spike is not the game. It ships a minimal deterministic simulation slice, a
converted real asset pair, and the platform plumbing needed to accept or reject
the Kotlin + LibGDX stack with evidence. The legacy JAR remains static-only:
nothing in this plan executes it, and the spike does not parse it at runtime.

## Guardrails

- Never run the target JAR, MIDlet, classes, emulator images of the MIDlet, or
  any device runtime of the legacy game.
- Recovered assets enter the new runtime only through the offline converter,
  with source hash, output hash, and transform version recorded.
- Core simulation stays free of LibGDX/Android/iOS imports.
- Do not claim iOS gate coverage until the macOS spike half runs; this session
  verifies Android plus a desktop dev launcher.

## Decision gate mapping (ADR section "Mandatory decision gate")

| Gate item | Spike evidence |
|---|---|
| 1. build/launch shared core on Android and iOS | APK builds and launches on Android emulator; iOS launcher is scaffolded but requires a macOS machine — tracked as open gate half. |
| 2. render pixel-perfect 400x240 to modern viewport | `render-gdx` draws the logical scene to a 400x240 framebuffer and integer-scales with nearest filtering; verified by emulator screenshot. |
| 3. touch and suspend/resume | Touch callbacks enqueue timestamped input consumed at tick boundary; activity pause/resume halts and restarts the accumulator without catch-up; verified via adb-driven interactions. |
| 4. play converted SFX/music sample | One decoded WAV passes through the converter into `generated/` and plays from a user touch. |
| 5. sandbox save read/write | `save-runtime` writes a versioned snapshot on pause and restores it on launch; verified via adb file inspection. |
| 6. deterministic state-hash outside UI | Core JUnit test replays a fixed input script through the fixed-step loop and asserts an identical SHA-256 state hash. |

## Phases

| Phase | Name | Status |
|-------|------|--------|
| 1 | [Project Scaffold](./phase-01-project-scaffold.md) | Completed |
| 2 | [Core Simulation Slice](./phase-02-core-simulation-slice.md) | Completed |
| 3 | [Content Conversion Slice](./phase-03-content-conversion.md) | Completed |
| 4 | [LibGDX Adapters and Launchers](./phase-04-gdx-adapters-and-launchers.md) | Completed |
| 5 | [Gate Verification and Handoff](./phase-05-gate-verification.md) | Completed (iOS half of gate item 1 pending macOS) |

## Dependencies

- Consumes `260722-1922-assassins-creed-reconstruction` outputs (decoded
  resources, design docs) and the follow-up parity tracks' contract surfaces.
- Blocks: full gameplay port planning (gated on this spike's verdict).

## Risks

- iOS half of gate item 1 cannot run on this Linux machine; requires a macOS
  session with Xcode. Recorded as an explicit open item rather than silently
  dropped.
- Maven Central rate-limits this egress IP; Gradle resolves through the GCS
  mirror init script plus `google()` for AGP artifacts.
