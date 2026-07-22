---
phase: 5
title: Modern Mobile Technical Design
status: completed
effort: ''
priority: P1
dependencies:
  - 2
  - 3
---

# Phase 5: Modern Mobile Technical Design

## Overview

Design—without implementing—a modern Android/iOS rewrite that preserves recovered gameplay and reuses converted resources.

## Requirements

- Functional: cover framework selection, modules, game loop, rendering, content, input, audio, save, tests, and migration.
- Non-functional: deterministic behavior, 60 FPS target, offline-first, provenance-aware content pipeline, no code execution.

## Related Code Files

- Create: `docs/modern-mobile-technical-design.md`
- Create: `docs/decisions/mobile-game-framework.md`

## Implementation Steps

1. Compare viable 2D game stacks against this game's recovered requirements.
2. Select a primary stack and record rejected alternatives/tradeoffs.
3. Define engine/domain/content/platform module boundaries and data contracts.
4. Design fixed-timestep simulation, rendering, animation, collision, AI, state, input, audio, and saves.
5. Define the one-way conversion pipeline from recovered resources to modern immutable assets.
6. Specify testing layers, performance budgets, milestones, risks, rollback, and acceptance gates.

## Success Criteria

- [x] Android and iOS share gameplay code and resources by design.
- [x] Every legacy subsystem has a modern target component.
- [x] Resource reuse is deterministic, versioned, and provenance-tracked.
- [x] Plan is implementable in phases without referencing the original JAR at runtime.

## Risk Assessment

General app frameworks may be poor fits for a deterministic action game. Choose based on rendering/game-loop/content needs, not generic mobile popularity.
