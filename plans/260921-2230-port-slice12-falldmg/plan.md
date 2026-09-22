# Port slice 12 — fall damage (op21) + `g.y` apex marker

## Mining — proven

- `g.y` stamped inside `i()` on the player (ax==0) entering anim
  **43 / 148 / 0** (i.java:265-275); entering **50** zeroes the meter
  (`g.e(0)` — death anim).
- Land gate inside `d(boolean)` (g.java:4690-4700): after the
  S16/Q16/S150/r8/r9 checks, `(al - g.y)/20 >= 20` && `!h()` →
  `a(21,0,0,this)` and early return (land squat skipped).
- op21 arm (i.java:4535 L55): `x[1] -= (105 * ((al - g.y)/20)) / 20`
  clamped ≥0 — **raw drain** bypassing `d()`'s gates (no `t=10`, no
  `bh` flash).
- Damage scale: 105 dmg per 20 cells ≈ 5.25/cell — ≥17 cells can KO
  the 90-meter.

## Port

- `Entity.gy` + `setAnim` hook (43/148/0 → `gy=al`; 50 → `x1=0`).
- `applyHit` op21 → raw drain.
- `land()` gate: `(al-gy)/20 >= 20 && gt==0 && ax==0` → `applyHit(21)`
  then **continue** landing (inferred control flow — original returns
  early; continuing avoids a drain re-fire on the next grounded tick).

## Kiểm chứng

- 43 `:core:test` xanh; `op21 fall damage scales with drop distance`
  pins 20-cell→KO and 10-cell→52.

## Gaps

- `d()`'s r8/r9 fall-context args, `h()` ordering (cutscene flag `s`),
  and the land-squat skip on heavy falls.
