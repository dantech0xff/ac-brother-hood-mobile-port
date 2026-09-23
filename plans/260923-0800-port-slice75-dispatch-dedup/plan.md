---
title: "Slice 75 — entity-dispatch dedup + ax74 shadowed-arm fix"
phase: rewrite
status: complete
---

# Slice 75 — ax74 shadowed arm + dead dispatch arms

## What

The `Level0World` entity-tick dispatch had three defects, found while
auditing the chain against `i.I()`'s `switch(ax)`:

1. **ax74 shadowed (the real bug).** The chain ran the stale partial
   `tickWisp` (S1/S2 only, no collect scan, no `P|=16`/`az` update) before
   slice 66's full `bN()` port `tickAx74` — so the proven arm was dead code
   and S0 collect could never fire in-game.
   Fix: deleted `tickWisp`; dispatch now reaches `tickAx74` only.

2. **`tickAx74` never advanced the clip.** `bN()` relies on the universal
   `s()` call, which in the original runs in the shared per-tick **tail**
   (`i.java:6407` L25–L34 — proven: FSM arms `goto` the tail after their
   work, so an arm's `r()` still sees last-frame state before `s()` wraps
   `T`). Ported as `try { … } finally { e.advanceAnim() }`.

3. **Dead duplicate arms.** `ax==42→tickAx42` and `ax==15→tickAx15`
   appeared twice each; the second arm was unreachable. Removed.

## Evidence

- `i.java:6407` L25–L34: `s()` tail after the arm (`cu==false && S>=0 &&
  (aH==false || j.g%aI==0)`), proven.
- `i.java:793`/`r()` checks raw `T==last && U==dur-1` — not a latch —
  which is exactly why the tail ordering is required for end-arms to fire.
- Regression test: `ax74 dispatches to bN collect arm through w tick` —
  wisp at dist<20 collects through `w.tick` (S0→S2, `kAp[5]++`).
  Previously impossible via the real dispatch.

## Gates

- `python3 scripts/verify-static-reconstruction.py` → `ok:true`
- `python3 -m unittest discover -s tests` → 57 pass
- `./gradlew :core:test` → green (incl. updated slice-66 tests: the
  `T/U=last` presets now model "one sub-frame before end" correctly
  because `s()` runs after the arm)
- `./gradlew :android:assembleDebug` → builds
