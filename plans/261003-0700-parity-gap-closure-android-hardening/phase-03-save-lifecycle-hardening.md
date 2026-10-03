---
phase: 3
title: Save and Lifecycle Hardening
status: pending
priority: P1
dependencies: []
---

# Phase 3: Save and Lifecycle Hardening

## Context Links

- [Plan](./plan.md) · [Baseline audit §4](./reports/baseline-audit.md)
- Design: `docs/modern-mobile-technical-design.md` §8 "Tick failure boundary",
  §16 "Save/persistence" (SaveEnvelope, I/O guarantees, legacy compatibility)
- Legacy record: `docs/save-format.md` (`bA` = 512 bytes, `:21`)
- Code: `rewrite/gdx/src/main/kotlin/com/acrebuild/gdx/{SaveBridge,Level0Game,Level0InputBridge,AudioBridge}.kt`,
  `rewrite/core/src/main/kotlin/com/acrebuild/core/{Level0World,InputQueue,Commands}.kt`

## Overview

The game loop still uses the spike's platform plumbing: a raw 320 B save
replaced non-atomically, exceptions that are logged while the world keeps
ticking, and no BACK key. This phase brings those up to the design's
contracts for an internal Android build. The durable-save **policy** stays
coarse and legacy-like: progression and settings only, and a mid-mission
checkpoint is lost when the process dies, as in the original (Run-26).

## Requirements

- Core stays pure Kotlin; file I/O stays in `gdx` behind `SavePort`.
- No change to what is saved or when (`saveFlush` call sites stay as ported);
  only the container, the write protocol and recovery change.
- Existing raw 320 B `asbr-save.bin` files keep loading.

## Implementation Steps

1. **ADR** `docs/decisions/save-policy.md` (Vietnamese): choose the coarse
   legacy-like policy over exact resume (§16 requires choosing one
   contract); record that the payload stays the port's 160-entry `kBA`
   (320 B LE16) rather than the legacy 512-byte `bA`, with the offset map.
2. **SaveEnvelope v1** in core: magic, envelope/schema version, `saveRevision`,
   payload length (bounds-checked before allocation), SHA-256 digest over
   header + payload. Payload = the `kBA` record.
3. **Migration**: a headerless 320 B file is read as legacy v0 and rewritten
   as v1 on the next flush. Fixtures: raw, truncated, wrong magic,
   oversized length, flipped header/payload/digest byte.
4. **Write protocol** in `SaveBridge`: write `asbr-save.<rev>.tmp` → flush +
   `FileChannel.force(true)` → rotate current main to `.bak` → atomic move
   into place (`Files.move` with `ATOMIC_MOVE` + `REPLACE_EXISTING`, plain
   rename as fallback). Read: valid main → else valid `.bak` → else defaults.
   Remove orphan temp files after selection. Crash-point tests at every step.
5. **Tick quarantine** (§8): an exception in `world.tick` stops all further
   ticks, drops that tick's commands (no `PersistBA`, no audio), stops audio
   once, and shows a minimal fatal screen whose only action restarts from the
   durable save / title. Replace the catch-and-continue in
   `Level0Game.kt:203-207`. Test with an injected throwing world.
6. **BACK key**: first mine the soft-key mapping (the `keyPressed` path in
   `k.java`; `v(131072)` is the back edge, `Level0World.kt:2993`) per screen,
   including gameplay. Then add a sequenced key event to `InputQueue`, consumed
   at the tick boundary, and catch `Input.Keys.BACK` in `Level0InputBridge`.
   Test: BACK equals the right soft-key footer tap on every screen family.
7. **Lifecycle**: `pause()` flushes any pending `PersistBA` synchronously and
   pauses audio; `resume()` resets the tick accumulator (verify) and resumes
   the stashed track; `dispose()` flushes. Move `AudioBridge` off
   `SpikeGame.TAG`.

## Success Criteria

- Save fixtures and crash-point tests green; a legacy raw file loads and is
  rewritten as v1.
- Injected tick failure: no further ticks, no save, no audio, fatal screen shown.
- BACK works on every screen family per the mined mapping; covered by tests,
  then checked in the next device run.
- `AGENTS.md`'s pinned save contract updated in Phase 4.

## Risk Assessment

- `Files.move(ATOMIC_MOVE)` support differs per filesystem; keep the fallback
  and test both paths.
- BACK in gameplay must match the original soft key, not Android habit; if the
  mining shows no back action in a state, BACK does nothing there.
