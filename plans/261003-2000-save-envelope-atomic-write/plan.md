---
title: "Phase 3.1-3.4 — save container v1, legacy migration and the atomic write protocol"
phase: "hardening"
status: "done"
date: 2026-10-03
confidence: proven
parent: "../261003-0700-parity-gap-closure-android-hardening/plan.md"
sources:
  - docs/save-format.md
  - docs/decisions/save-policy.md
  - rewrite/core/src/main/kotlin/com/acrebuild/core/Level0World.kt (saveFlush/saveLoad)
---

# Save container v1 + atomic write (Phase 3 steps 1-4)

## What

- ADR `docs/decisions/save-policy.md` (Vietnamese): coarse legacy-like policy
  (progression + settings only, `e(true)` call sites unchanged); payload stays
  the port's 320 B `kBA` record with the offset map (original byte `i` ↔ payload
  bytes `2i..2i+1`, LE16).
- `core/SaveEnvelope.kt`: magic `ACBA`, envelope v1, payload schema, 64-bit
  `saveRevision`, bounded length (checked before allocation), SHA-256 over
  header + payload. A headerless 320 B file decodes as legacy v0 (`kBA` schema
  only).
- `gdx/SaveStore.kt`: temp → flush → `force(true)` → rotate a valid current
  file to `.bak` (a corrupt one is dropped) → `rename(2)` into place. Read:
  valid current, else the highest-revision valid temp/`.bak` (a chosen temp is
  committed), leftovers removed. `java.nio.file` is avoided (minSdk 24).
- `SaveBridge` delegates to `SaveStore`; the game uses the `kBA` schema, the
  spike its own. `Level0Game` treats a failed write as non-fatal.

## Tests

- `SaveEnvelopeTest` (core, 11): round trip, layout, legacy raw, legacy only
  for `kBA`, truncation, wrong magic/sizes, oversized length, flipped
  header/payload/digest byte, version/schema mismatch, encode guards, the
  world's record size.
- `SaveStoreTest` (gdx, 6): empty dir, revisions + `.bak`, legacy → v1
  rewrite, corrupt current → `.bak` (never rotated over it), a crash at each
  of the four steps leaves the old or new save (and a forced temp is
  committed), a torn temp is ignored.

## Gates

Docs touched: `docs/decisions/save-policy.md` (new). See the commit for the
gate run.
