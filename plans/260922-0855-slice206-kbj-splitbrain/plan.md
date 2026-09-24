---
title: "Slice 206 — k.bJ split-brain fix + proven-dead i.bJ sweep"
phase: port
status: done
---

## Summary

Two Kotlin fields (`kBJ` on Level0World, `kBj` on the `LevelCellSource`
iface) both claimed to be J2ME `k.bJ`. The producer (NpcFsm:5512
boss grab-QTE lose, i.java:31851) wrote `kBj`; the consumer (`k.I()`
flash tick, k.java:2522-2526 — `bJ--` driving `df = ARGB(255,120·bJ/8
×3)`) and the renderer's `z[74]` overlay read `kBJ`. They never met:
the boss-lose damage flash was dead code. Fix: single `kBj` field.

## Changes

- `Level0World.kt` — deleted duplicate `var kBJ` (:371-378 doc now
  describes the iface member at :1505); `k.I()` flash arm + flash-shrink
  consumer read `kBj`; renderer `Level0Renderer.kt:1176` reads `world.kBj`.
- `Entity.kt` — iface comment for `kBj` updated (producer IS ported).
- `Slice1Test.kt` — the pre-existing `kBJ` flash test retargeted onto
  `kBj` (same assertions); `Slice206Test` adds the producer-side link
  regression (arm 6 → next tick `kDe`, `kDf=ARGB(255,75,75,75)`).

## Proven-dead documentation sweep (same pass)

- `i.bJ`/`i.bH`/`i.bI`/`i.bL` iface (Entity.kt:4527) — flicker-latch
  machinery in `k.b(z2)` (k.java:12090-12120). PROVEN DEAD end-to-end:
  every `bJ` writer is an init/reset (`i.java:7160`, `i.java:63743`,
  and the state machine's own `bJ=bI`/`bJ=0` exits); `bL` never leaves
  0 (only access is the `getstatic;dup;putstatic` no-op at
  k.javap.txt:16545). The port implements the state machine faithfully
  (Level0World:4238) — the comment now says so instead of "producer
  unported".
- `j.a(0, j.c(0)>>8)` (Entity.kt:4128) — upgraded inferred→proven:
  `j.c(0)` returns `Int.MAX_VALUE` (j.java:798-813, `b(0)` null-arm).
- Case-collision audit: `i.bh`/`i.bj`/`i.bJ`/`i.bi` are four DISTINCT
  J2ME fields (hit-lock / finale freeze / flicker / ax64 hitlag);
  `kBJ`/`kBj` was the only true collision. `i.bL` confirmed static —
  not an entity field — matching the no-op port.

## Still inferred (unchanged)

- `i.bH`/`i.bI` exact arm values (1 and 2 used; the dead path never
  reaches the draws that would disambiguate).

## Verification

- verifier: `ok:true`; unittests 57 pass; `:core:test` --rerun-tasks
  green (incl. Slice205Test rope guard + Slice206Test flash); 
  `:gdx:build` + `:android:assembleDebug` green.
