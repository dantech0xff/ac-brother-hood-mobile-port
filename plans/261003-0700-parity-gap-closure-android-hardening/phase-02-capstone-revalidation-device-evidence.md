---
phase: 2
title: Capstone Re-validation and Device Evidence
status: pending
priority: P1
dependencies: [1]
---

# Phase 2: Capstone Re-validation and Device Evidence

## Context Links

- [Plan](./plan.md) · [Phase 1](./phase-01-parity-gap-closure.md)
- Capstone legs: `rewrite/core/src/test/kotlin/com/acrebuild/core/Slice1Test.kt`
  (m0 continuous run ~`:24091`; stitched legs e.g. `:25520`, `:26563`,
  `:27251`, `:28914`; m7 HP top-up `:28929`)
- Device log: `plans/260922-0730-demo-verify/reports/REPORT.md` (ends at Run-30)
- Device recipes: `.agents/skills/android-emulator-testing/SKILL.md`

## Overview

Phase 1 makes 20 sentinels/pouncers, 176 soldiers, the wall-kick S18 arc and
the Cesare grab behave like the original. Most capstones were proven against
the old behaviour. This phase re-proves every mission end to end against the
corrected game and refreshes the device evidence that stopped at slice 330.

## Requirements

- A capstone may change its route, timing or waypoint logic; it may never
  disable, slow or reposition enemies, or patch game state, to pass.
- Every capstone outcome is classified with evidence:
  `pass` / `re-routed` / `fix-regression` (Phase 1 fix is wrong → back to
  Phase 1) / `faithful-dead-end` (the original blocks this route too).
- Device runs happen on the emulator host only.

## Implementation Steps

1. Run `:core:test` on the Phase-1 head; list every failing capstone leg with
   mission, leg, last good position and the entity that now interferes.
2. Re-route or re-time each failing leg; record the per-mission verdict table
   in `reports/capstone-revalidation.md`.
3. Re-evaluate the stitched legs (m2/m3/m5/m6/m7) and the m7 "bot-survival
   accommodation" (`Slice1Test.kt:28929`): connect legs where the corrected
   behaviour allows it; otherwise document why each seam remains.
4. "Run-31" (the slice-337 menu audit cited in `Slice337Test.kt:9`) was
   never written up: add it to `REPORT.md` if its artifacts exist, otherwise
   note it as unrecorded.
5. Device Run-32 (menus, slices 337–343): OPTIONS two-column split,
   jc23 sound prompt row/pill chrome, pressed/unpressed chrome, tap across the
   62 ms tick boundary, multi-finger tracking (the old "pointer death"), the
   jC=28 deleted-data screen after G6.
6. Device Run-33 (gameplay, Phase 1): m0 sentinels, m3/m5 pouncers and
   sentinels, ax11 attack variety, S17→S18 wall-kick arc, ax67 springboard
   hit (m1/m4), m7 Cesare grab QTE (no escape without the QTE).
7. Append the runs to `REPORT.md` with screenshots/video and update its
   stale frontmatter (frozen at Run-14).

## Success Criteria

- `:core:test` fully green on the corrected game with no test weakening.
- `reports/capstone-revalidation.md` has a verdict for all 8 missions.
- Run-32 and Run-33 recorded with artifacts; each Phase-1 fix observed on device.

## Risk Assessment

- Some legs may become unreachable for a waypoint bot (precision dodges);
  record them as `faithful-dead-end` only with evidence that the original has
  the same constraint, otherwise keep working the route.
- Device time on swiftshader is slow; follow the SKILL.md recipes (jdb
  suspend-leak, input death) to avoid wasted takes.
