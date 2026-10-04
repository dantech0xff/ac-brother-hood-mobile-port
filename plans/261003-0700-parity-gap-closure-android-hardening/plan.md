---
title: Parity-Gap Closure and Android Hardening
description: >-
  Close the proven gameplay-fidelity gaps found by the 2026-10-03 baseline
  audit (ax47/ax50 FSM swap, applyHit op 40, player S18/S371 arms, ax11 aC()
  attack scheduler, jC=28 ghost rows), re-validate the mission capstones and
  device evidence, then harden save/lifecycle, add CI, sync stale docs and set
  up the Android build for internal distribution. No store publication.
status: in_progress
priority: P1
effort: large
branch: 'claude/phase2-faithful-ai'
tags:
  - rewrite
  - parity
  - hardening
  - android
  - ci
  - docs
blockedBy: []
blocks: []
created: '2026-10-03T07:00:00.000Z'
createdBy: 'claude'
source: user
---

# Parity-Gap Closure and Android Hardening

## Overview

The port in `rewrite/` plays all eight missions and every gate is green
(baseline: [`reports/baseline-audit.md`](./reports/baseline-audit.md)). The
audit also found gameplay that silently diverges from the original — most
visibly 20 ledge sentinels/pouncers in m0/m2/m3/m5 that run each other's FSM
and never attack — and a platform layer that is still the toolchain spike's:
unversioned non-atomic save, no tick quarantine, no BACK key, no CI, stale docs.

This plan fixes fidelity first (because every later capstone and device run
should be measured against correct behaviour), then hardens the Android build
for internal use.

## Decisions (2026-10-03, user)

- Next phase = parity fixes + hardening, in that order.
- Distribution is **internal only — no store publication**. Store items
  (applicationId change for Play, rights ledger, IARC/data-safety, store
  listing) are out of scope. The shipped content is the original
  Gameloft/Ubisoft IP and stays internal.

## Guardrails

- The original JAR is static-only: never run it (no `java -jar`, emulator,
  simulator or device). Only `rewrite/` builds run on emulator/device.
- Every behaviour claim carries `proven` / `high-confidence` / `inferred` /
  `unknown` plus a `file:line` in `reconstructed-project/src/` (or bytecode).
- No silent approximation: a gap that is not yet mined gets mined first
  (`docs/gameplay-mining/`), then ported.
- Capstone/bot tests may change their **routes**, never the game logic, to
  pass. A capstone that now fails because an enemy engages is a finding to
  classify, not a test to weaken.
- Before each PR: verifier `ok:true`, `python3 -m unittest discover -s tests`,
  `./gradlew :core:test :gdx:test :android:assembleDebug` (JDK 17).
- Device evidence comes from an emulator host; the cloud container has no
  `/dev/kvm`.

## Phases

| Phase | Name | Status |
|---|---|---|
| 1 | [Parity-gap closure](./phase-01-parity-gap-closure.md) | done |
| 2 | [Capstone re-validation and device evidence](./phase-02-capstone-revalidation-device-evidence.md) | in_progress — capstones green ([report](./reports/capstone-revalidation.md)); step 3 (connect the stitched legs) and the device runs are open; device runs need an emulator host |
| 3 | [Save and lifecycle hardening](./phase-03-save-lifecycle-hardening.md) | done — device check of BACK/SKIP/pause pending (Phase 2 runs) |
| 4 | [CI, docs sync and repo hygiene](./phase-04-ci-docs-hygiene.md) | in_progress — CI (green on PRs #385 / #386), bootstrap, docs sync, `AGENTS.md` done; open: remote `devin/*` branch cleanup (needs the user's approval), optional `Slice1Test.kt` split |
| 5 | [Android internal-release configuration](./phase-05-android-internal-release.md) | in_progress — manifest, logging, release build, ADR done; open: device checks (cutout, gesture navigation, performance numbers) |

Next implementer: [`../261008-2000-handoff-after-slice416/plan.md`](../261008-2000-handoff-after-slice416/plan.md) — rules, tools, trap
catalogue and backlog for continuing the raw-bytecode parity audit.

## Dependencies

- Phase 2 depends on Phase 1 (re-validation is meaningless before the fixes).
- Phase 3 is independent of 1–2 and may run in parallel on its own branch.
- Phase 4 can start any time; its docs sync must land after Phases 1 and 3 so
  the docs and `AGENTS.md` describe the final contracts.
- Phase 5 depends on Phase 3 (BACK key and lifecycle) and on Phase 4's CI.

## Key risks

| Risk | Mitigation |
|---|---|
| Fixing G1/G5 makes enemies engage and breaks m0/m2/m3/m5 capstones that were proven against inert enemies | Phase 2 re-routes bots and classifies each break; never re-break the fix to go green |
| ax11 `aC()` mining is larger than expected | Mine first with a timebox; if needed split into its own slice plan and keep the rest of Phase 1 moving |
| Save envelope migration loses existing internal saves | Accept raw legacy 320 B files on read and wrap them on the next write; fixture-tested |
| Docs drift again | Phase 4 adds the port track and this plan to the roadmap and adds a "docs touched?" line to the slice-plan gates |

## Out of scope (follow-up tracks)

- iOS gate half (needs a macOS + Xcode + RoboVM session; `IOSLauncher` still
  boots `SpikeGame`).
- Exact-resume save (`CanonicalWorldSnapshot`, design §16) — needs an ADR.
- 60 FPS render interpolation / modern retime (design open decision 5).
- State hash and deterministic replay for `Level0World` (design §23 phase 3 gate).
- Cheat sequence (`k.java:739-790`) — unported by choice.
- Store/rights work (see Decisions).
