---
title: slice 233 — a(257,8) ledge vault-drop verdict test
phase: fidelity-verdict
status: done
---

## Summary

Regression test covering the `ledgeDrop257` arm (PlayerFsm.kt:1639,
g.java:2860-2874 L692 verbatim) — the DOWN-at-ledge vault-drop that is
the *live* half of the grounded-block's direction+DOWN dispatcher, now
that slice 232 proved `am()`'s S63 entry dead on shipped content.

## Verdict (real level-0 geometry)

Standing on a thin platform top (`support ≥19`, air below, air at feet
level ahead) with `v(M_DOWN)` → `enterStateMasked(257, 8)`:

- The arm probes at `al+20` (verbatim `al+=20;x();al-=20`): shifted
  `aQ ∈ {20,5}` = the support under the feet, shifted `aR == 0` = the
  cell below it is air (thin/floating platform).
- `e(edgeCx, belowCy) < 12` — the cell two columns out in the facing
  direction one row below is open (room to vault out, not into a wall).
- S257 anim end → `ledgeDropArm`: `al += frameDy+10`, `ak ±= frameDx`
  by facing, `setAnim(0)` — the player ends below the ledge and falls
  naturally on the next tick.

## Semantics recap for the dispatcher (grounded `u(M_DOWN)` tick)

1. `wallClimb`/`am()` — `aV/aW/aR == 19` exact → S63 climb-down.
   PROVEN-DEAD (slice 232): no 19 cells exist in any pack.
2. `ledgeDrop257` — thin platform edge → S257 vault. LIVE (this test).
So `dir+DOWN` on shipped content = "vault down at ledges" only.

## Gates

- verifier `ok:true`, unittest 57 pass
- `:core:test` green (new Slice89 test passes)
- `:android:assembleDebug`, `:gdx:build` green
