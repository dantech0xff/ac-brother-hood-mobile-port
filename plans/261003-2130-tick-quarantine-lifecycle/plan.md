---
title: "Phase 3.5 + 3.7 — tick-failure quarantine and lifecycle flush"
phase: "hardening"
status: "done"
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - docs/modern-mobile-technical-design.md (§8 tick failure boundary)
  - rewrite/gdx/src/main/kotlin/com/acrebuild/gdx/Level0Game.kt
---

# Tick quarantine + lifecycle (Phase 3 steps 5 and 7)

## What

- `gdx/TickDriver`: one committed tick per call; the first exception
  quarantines the world for good — no further ticks, the failed tick's
  commands are dropped (no `PersistBA`, no audio), and the fatal hook runs once.
  Replaces the catch-and-continue in `Level0Game.render()`.
- `Level0Game`: on the fatal hook, stop the channel and show a minimal fatal
  screen (`Level0Renderer.renderFatal`, libGDX default font); its only action
  is a tap, which rebuilds mission 0 from the durable save and re-enters the
  boot screens (`boot()`, shared with `create()`).
- Lifecycle: `pause()` → `world.hideNotify()` (slice 366) + synchronous
  execution of every queued command (a pending `PersistBA` reaches disk before
  the process can die) + channel stop; `resume()` → tick accumulator reset +
  `world.showNotify()`; `dispose()` → last flush.
- `AudioBridge` takes a log tag (the game logs under `AcLevel0`, the spike
  keeps `SpikeGame.TAG`); `stopAll()` is public for the hooks.

## Tests

`TickDriverTest` (2, injected throwing world): healthy ticks return their
commands; the first failure quarantines, drops the failed tick's commands,
stops ticking, fires the hook once, and idle drains return nothing. The fatal
screen and restart are GL-bound and checked on device (next device run).
