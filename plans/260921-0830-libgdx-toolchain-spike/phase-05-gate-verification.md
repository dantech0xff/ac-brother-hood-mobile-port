---
phase: 5
title: Gate Verification and Handoff
status: completed
priority: P1
dependencies: [4]
---

# Phase 5: Gate Verification and Handoff

## Context Links

- [Plan](./plan.md)
- Gate definition: `docs/decisions/mobile-game-framework.md`
  "Mandatory decision gate"

## Overview

Collect evidence per gate item and publish `reports/gate-results.md` with an
explicit pass/pending verdict per item. iOS build/launch is a pending half of
gate 1 requiring a macOS session (RoboVM/Xcode); the report states this instead
of implying coverage.

## Checks

1. `:android:assembleDebug` APK artifact + install on the emulator AVD.
2. Screenshot of the 400x240 logical scene integer-scaled on a modern aspect
   display.
3. `adb shell input tap` steers the actor; `input keyevent HOME` + relaunch
   exercises suspend/resume with no catch-up burst (accumulator reset logged).
4. Touch triggers the converted WAV (log line + `dumpsys audio` evidence or
   audible path assertion via command sink).
5. Pause writes the save file; relaunch restores position/tick; file
   inspected via `run-as`.
6. `:core:test` deterministic state-hash suite green.

## Handoff

- Update `README.md` and `docs/project-roadmap.md` with the new track status.
- File one `reports/` summary; keep claims inside what was actually executed.
- Record the exact toolchain pins in `rewrite/README.md` (Gradle, AGP, Kotlin,
  LibGDX, JDK, SDK platform/build-tools, emulator image).
