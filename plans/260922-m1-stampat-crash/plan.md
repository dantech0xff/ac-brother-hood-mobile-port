---
title: Slice 311 — mission-1+ stampAt crash (negative parallax row wrap)
phase: fix
status: done
---

# Slice 311 — mission-1+ gameplay crash at `LevelPack.stampAt`

## The crash (device-verified)

Every flying mission (m1+) crashed ~20 s in:
`ArrayIndexOutOfBoundsException: length=273; index=-1` (then `-57`) at
`LevelPack.stampAt` ← `Level0Renderer.render:1174`. Reproduced on a fully
clean path (boot → CONTINUE → mission-1 → gameplay → crash). All m1+
missions were unrunnable on device.

## Root cause

`stampAt` computed `dl[(cx % 21) * 13 + (cy % 13)]`. Kotlin `%` keeps the
sign like Java, so a negative `parallax-y` (bh3 keeps negative rows for
the wrap band above the world) produced a negative `dL` index → OOB.

The original guards this in the render: `i14 = i13 % 13; if (i14 < 0)
i14 += 13` (k.java:4434-4438, proven) — the negative row folds back
positive. Our port skipped the fold.

## Fix

`stampAt` now folds negative rows/cols back positive before indexing —
matching the original's wrap semantics. Regression test
`stampAt wraps negative rows` proves `cy-13 ≡ cy` and that a wildly
negative `cy` stays in-bounds.

## Provenance

k.java:4434-4438 (structured decompile) — the negative-row fold is proven
source behavior, not a guess.
