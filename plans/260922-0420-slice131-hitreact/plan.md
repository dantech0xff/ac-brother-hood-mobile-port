---
title: "Slice 131 — i.c(iVar) hit-react full port (floaties, guards, camera clamp)"
phase: port
status: done
---

## Scope

`counteredBy` was a stub (face + ±1536 + i(9)); ported the full
`i.c(iVar)` (i.java:3398-3432, proven):

- `g.b = null` (release the player link)
- damage floatie `a(8,5,14,av,ak,midY,300)` + `k.bK`-gated `a(8,59,0,…)`
- `av = iVar.ak < ak` **only for ax∈{11,17,23,50,73}** (other attackers
  leave facing untouched — the stub flipped it unconditionally)
- push `ag=±1536` then the verbatim guard `y() || aF() || e(i,al/20)>=12
  → ag=0` (kept even though aF's free-side polarity cancels on open
  ground — flagged below)
- `i(0); t()` refresh
- `k.R`/`k.S` camera-wall clamp: `ak+ag>>8 ≤ kR+w && kR>0 → ag=0,ak=i3`;
  `≥ i4 && kS>0 → ag=0,ak=i4`
- tail: `iVar.ax==61 && Q==6 → i(6)` else `i(9)` — the stub always
  picked S9.

Signature grew the `w: LevelCellSource` param; both `applyHit` op4
call sites updated.

## Flagged (verbatim quirk)

`aF()` returns *side-free* for the non-crate case — so
`y()||aF()||e>=12` cancels the slide exactly when the destination side
is open. Pushed entities therefore slide only when already backed to a
crate edge or wall. Kept verbatim; the polarity may be a JADX ternary
inversion but nothing disproves it.

## Gate results

- verifier `ok:true`; unittest 57; `:core:test` 895 (+6 Slice131);
  `:android:assembleDebug` + `:gdx:build` green.
