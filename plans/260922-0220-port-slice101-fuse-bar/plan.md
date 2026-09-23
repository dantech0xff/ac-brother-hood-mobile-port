---
title: "Port slice 101 — k.aD capture/fuse HUD bar draw"
phase: port-slice-101
status: done
---

# Slice 101 — `k.aD` capture/fuse HUD bar

Adds the last undrawn HUD piece: the `k.aD` progress bar that ax22
capture zones and ax27 fuse barrels claim when they enter their S6
countdown — producers were already ported; only the draw was missing.

## Original semantics (proven — `reconstructed-project/src/structured/k.java`)

- `k.aD` (:75) = the HUD progress-bar entity singleton. Producers:
  `k.aD = this` on S6 entry in ax22's `aN()` (i.java:19179) and ax27's
  `bL()`; released to `null` on completion/removal.
- HUD arm (:3133-3139), inside the C-claim gate
  `(C != null && (!C.cd[6] || !C.ab())) || C == null` (the tail is
  skipped while a claim-script overlay owns the screen):
  ```
  if (!g.g() && aD != null && aD.Z != null && aD.S == 6 && aD.Z[1] > 0) {
      z[12].a(cd, 18, 0, 110, 215, 0, 0, 0);                          // outline
      j.a(cd, 125, 0, (120*(aD.Z[1]-aD.Z[2]))/aD.Z[1], 240, true);    // clip
      z[12].a(cd, 19, 0, 110, 215, 0, 0, 0);                          // fill
      j.a(cd, 0, 0, 400, 240, true);                                  // reset
  }
  ```
  `Z[1]` = countdown total, `Z[2]` = elapsed — the fill draws the
  `(Z[1]-Z[2])/Z[1] × 120px` share starting at x=125.

## Port mapping (`rewrite/gdx/.../Level0Renderer.kt`)

- Same gate: `cGate = kC == null || !kC.cd[6] || !kC.claimActive()` and
  `!world.gG()` (player-dead check already on the world).
- `drawFrame(12, 18, …)` outline → `clipScissor(125, 0, pw, 240)` →
  `drawFrame(12, 19, …)` fill → `clipReset()` — `clipScissor` is the
  established `j.a(cd,x,y,w,h,true)` mapping (GL scissor, Y-flipped).
- Placed after the `aO/aP` timed-line block — the original order is
  `c(z2)` HUD → aD bar → `aQ` image → `z[74]` touch overlay.

## Field/mapping confirmations

- `Entity.cd` = `BooleanArray(10)` claim flags; `claimActive()` =
  `i.ab()` (`ca>=0 && !cd[0] && scriptStep>=0`); `kC` = the C claim
  entity; `gG()` = `player.x1 <= 0`.
- `z[12]` resolves through the existing `clips[12]` pack (alias of
  clip94 — same pack the weapon corner's anim8/22 uses).

## Verification

- `:gdx:compileKotlin`, `:core:test`, `:android:assembleDebug` — green.
- The bar only appears while an ax22/ax27 entity holds `kAD` in S6 —
  both producers already tick on level 0 (10×ax22, 6×ax27 records).
