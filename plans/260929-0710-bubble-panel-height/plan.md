---
title: Slice 324 — bubble panel height fix (k.y.k fidelity)
phase: fidelity-fix
status: complete
---

# Slice 324 — speech-bubble panel height (`k.y.k`)

## Defect (found during Run-23 device capture)

The 3-page guard warning bubble ("WHO ARE YOU? YOU ARE PROHIBITED…")
rendered its bottom line half-buried under the panel border — reproduced
on emulator AND desktop (Run-19). Root cause was metric fidelity, not
geometry.

## Root cause

`i.ad()` sizes the panel `r02 = k.y.k(cQ[4]) + 10` (i.java:18986).
`k.y.k(n)` = `n*J + (n-1)*K` (b.java:1609) — the real y-font metric on
clip-92 is J=14, K=1 → k(3)=44. `Level0World` never overrode
`LevelCellSource.dialogAdvance`, so it fell back to the interface default
`n*10` → 30. Panel short by 14px ≈ a full line pitch (J+K=15); the bottom
line of every multi-line page overflowed the border.

## Fix

`Level0World.dialogAdvance` now returns `footerFont.linesHeight(n)` —
the same `n*J + (n-1)*K` formula over the real clip-92 metrics
(FontClip.kt:228) — with the `n*10` default kept for font-less headless
worlds. The panel grows upward (`r16 = r03 - r02`) keeping the tail-wedge
anchor at `r03`, matching the original.

## Evidence

- `Slice324Test`: `k(1/2/3)` = 14/29/44 via real clip metrics; a `cQ`-armed
  guard emits `bubbleDraw.h == dialogAdvance(lines)+10` (never undersized);
  font-less fallback preserved.
- Gates: verifier `ok:true`, 57 unittests, full `:core:test` green,
  `:android:assembleDebug` builds.
