---
phase: 4
title: CI, Docs Sync and Repo Hygiene
status: in_progress
priority: P2
dependencies: []
---

# Phase 4: CI, Docs Sync and Repo Hygiene

## Context Links

- [Plan](./plan.md) · [Baseline audit §1, §5](./reports/baseline-audit.md)
- Gates: `AGENTS.md` ("Verifier phải xanh")
- Toolchain: `rewrite/README.md`, `rewrite/settings.gradle.kts`

## Overview

The four gates run only by hand and only on machines that happen to have JDK
17 and an Android SDK. Most top-level docs still describe the toolchain spike,
and `AGENTS.md` pins two contracts the game no longer uses. This phase
automates the gates, makes the docs describe the real port, and removes
repo debris.

## Progress (2026-10-03)

- 4.1 CI: `.github/workflows/ci.yml` (static / jvm / android jobs, Temurin 17,
  UTF-8 locale). The verifier also passes with JDK 17's `javap` (checked
  locally). Not yet exercised on GitHub — it runs on the next PR or push to
  `main`.
- 4.2 `rewrite/tools/bootstrap-dev-env.sh` (idempotent; `--no-install` audit
  mode); SDK pins fixed in `rewrite/README.md` (`build-tools;35.0.0` is AGP
  8.13.2's default, read from its `ToolsRevisionUtils`).
- 4.3 docs: `rewrite/README.md` rewritten for the port (Vietnamese),
  `README.md` rewrite status, `project-roadmap.md` (game-port track, broken
  tables fixed), `project-overview-pdr.md`, `codebase-summary.md`,
  `system-architecture.md` (§2b implemented architecture),
  `modern-mobile-technical-design.md` (status + ADR pointers), GDD §6,
  `level-atlas.md` (runtime histogram after retype: ax11/17 corrected,
  ax47/ax50 rows, ax72 is in pack 13 not 12, ax37 role) and
  `entity-type-catalog.md` (ax47 `aL()`, ax50 `aK()`, ax72 levels). ADR
  `docs/decisions/tick-cadence-self-clocked.md`.
- 4.4 `AGENTS.md` contracts updated (self-clocked tick, save container,
  BACK, Gradle gates/CI). Call it out in the PR description.
- 4.5/4.6 done: 17 plan statuses, npc-fsm-mining completed, spike plan
  status; `a.out` and `DebugSlice2.kt` removed. The spike assets stay (the
  iOS scaffold launches `SpikeGame`; 37 KB). Remote `devin/*` branch cleanup
  needs explicit user approval — not done. The `Slice1Test.kt` split is
  optional and not done (it would conflict with in-flight bot work).

## Implementation Steps

### 4.1 CI (`.github/workflows/ci.yml`)

- Triggers: `pull_request`, `push` to `main`; one concurrency group per ref.
- Set a UTF-8 locale (`LANG=C.UTF-8`): Kotlin test names with non-ASCII
  characters compile lambdas into class files with non-ASCII names, which
  a JVM under the POSIX locale cannot write (`sun.jnu.encoding` ASCII —
  hit in the 2026-10-03 cloud session, slice 350).
- Job `static`: Python 3 → verifier (`ok:true` required) + `unittest`.
- Job `jvm`: Temurin JDK 17, Gradle cache → `:core:test :gdx:test`; upload
  JUnit XML on failure.
- Job `android`: JDK 17 + SDK (`platforms;android-36`, `build-tools;35.0.0` —
  what AGP 8.13.2 actually resolves) → `:android:assembleDebug`; upload the APK.
- Keep the repository order in `settings.gradle.kts` (GCS mirror first; Maven
  Central rate-limits some runners).

### 4.2 Dev environment bootstrap

- `rewrite/tools/bootstrap-dev-env.sh`: install JDK 17 and the SDK packages
  above when missing (what the 2026-10-03 cloud session did by hand).
  Optionally wire it as a Claude Code `SessionStart` hook for cloud sessions.
- Fix the SDK pins in `rewrite/README.md`.

### 4.3 Docs sync (Vietnamese) — after Phases 1 and 3 land

| File | Change |
|---|---|
| `README.md` | Rewrite the "rewrite track" section (L170-180): the port, its gates, mission status |
| `rewrite/README.md` | Describe the game port (modules, `Level0Game`, log tag `AcLevel0`, 320 B save/envelope, tests); keep the spike as history |
| `docs/project-roadmap.md` | Add the port track (slices, capstones, device runs) and this plan; fix the malformed tables (L64-70, L82) |
| `docs/project-overview-pdr.md`, `docs/codebase-summary.md`, `docs/system-architecture.md` | Reflect `rewrite/` (real module table, out-of-scope lines removed) |
| `docs/modern-mobile-technical-design.md` | Frontmatter `implementation_status`; §8 catch-up wording vs the self-clocked loop |
| `docs/gameplay-design-document.md` §6 | Drop unknowns that are now proven (`au→bu/bw`, `aJ()/aL()` tails) |
| `docs/gameplay-mining/level-atlas.md`, `entity-type-catalog.md:39` | Add ax47/ax50 retypes and ax72 in pack 13; ax37 = camera scroll trigger |

- ADR `docs/decisions/tick-cadence-self-clocked.md`: the game loop runs at
  most one tick per frame with remainder-keep (slices 335–336,
  `Level0Game.kt:15-25`, original `j.java:189-219`), replacing the design's
  "≤4 catch-up" for the game path (the spike `TickEngine` keeps it).

### 4.4 `AGENTS.md`

- Replace "≤4 catch-up" with the self-clocked ≤1 tick/frame contract and
  "save 49-byte ACRS" with the Phase-3 envelope + `kBA` record. Call the
  change out in the PR description for review.

### 4.5 Plan bookkeeping

- Add `status: done` to the 17 `plan.md` files that lack it (list them in the
  slice plan); `plans/260921-0939-npc-fsm-mining/plan.md` → `completed`;
  toolchain spike plan: note "Android complete, iOS deferred to the iOS track".
- Add "docs touched?" to the gates line of new slice plans.

### 4.6 Repo hygiene

- `git rm rewrite/a.out` (empty ELF) and add `a.out` to `.gitignore`.
- `DebugSlice2.kt`: delete, or turn into a real assertion test.
- Move the spike-only assets (`splash.png`, `actor.png`, `sfx.wav`) out of the
  APK's asset root if only `SpikeGame` uses them.
- Remote branches: produce the list of merged/squash-merged `devin/*`
  branches; delete only with explicit user approval.
- Optional: split `Slice1Test.kt` (29,107 lines, 190 classes) into per-slice
  files — mechanical move, no test changes.

## Success Criteria

- CI green on a PR and on `main`; a deliberately failing test turns it red.
- No top-level doc claims the port is "only a spike" or "not started";
  `AGENTS.md` matches the code.
- `a.out` and the assertion-less test are gone.

## Risk Assessment

- CI minutes: the verifier (~45 s) and Gradle (~3–4 min) are fine; cache Gradle.
- `AGENTS.md` steers future agents; review its diff carefully.
