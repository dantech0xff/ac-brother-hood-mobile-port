---
slice: 327
title: Inferred-audit apply — verdict fixes + test corrections
date: 2026-10-01
status: done
---

## Scope

Apply the 10-batch verdict table from the inferred-claims audit
(workflow wfr-7badbfaae4064d778072271ddcef10b5). Every `fix` site is either
already faithful or corrected this pass; 6 `keep` sites documented as
verified-intentional; ~40 claims upgraded to `proven` in comments.

## Fixes applied

- `spawnStatic` caller inheritance (`Entity.kt` + `Level0World.kt`):
  original `i.a(IIII)` inherits the caller's `ak/al/av` — signature now
  carries `src: Entity?` end-to-end.
- ax64 S2 marker 3-way restore (`NpcFsm.kt`): `ae==null → respawn`,
  `ae.aa != k.z[74] → same`, `ae.S == 0 → hold`, else ax64S345 despawn —
  proven at simple/i.java:15660-15730 (an earlier b5-only edit was
  reverted).
- Provenance label upgrades (proven with file:line): NpcFsm pushOut,
  ax40 park, ax35VolPaint, rope `r93==0` divide guard, arena-arm
  `k.ac = camRect` (k.java:171/2085/2203); `Entity.kt` `cy = j.b(-cz, cA)`,
  `bq != 0` guard, gDrain, kDialog Boolean-return plumbing,
  wrapDialogText/dialogAdvance metric; `Level0World.kt` M() win-stats,
  fmtJ, dlgU, touchRect, 0/25 record, menuFkArm, bU table, kJ/kK,
  menu-frame, case-11 EXIT, refreshScrollBounds, scrollPanel iK via bW.

## Test corrections (asserted port-added behavior disproven by decompile)

- `Slice109Test` — pause edge skipping logo dwells was a non-verbatim
  nicety: k.java:3949-4100 `R()` has no input check; dwells advance only
  on the ~3000ms timer. Test now asserts unskippable.
- `Slice204Test` `hands full drops the jump latch` — `ci` clears
  unconditionally in `az()` (fallback g.java:12917-12942, keep-branch
  unreachable), rebound in the L144+ scan which admits npcKind only; the
  held entity must be an ax11 victim in `w.npcs`, not a local ax0.
- `Slice112Test`/`Slice113Test` regression — scrollPanel's `iK` font is
  the per-call `bW` (k.java:5627-5693); when clip-91 is absent (harness
  worlds) `menuFont` is null and `iK=0` made `fd < y3` wrap every tick.
  Falls back to `footerFont` so the metric is still a real font.

## Verification

- `verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest` → 57/57
- `:core:test` → all green (was 5 failures before fixes)
- `:android:assembleDebug` → green
